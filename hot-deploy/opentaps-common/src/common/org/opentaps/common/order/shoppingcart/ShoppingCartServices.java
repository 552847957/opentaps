/*
 * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Honest Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Honest Public License for more details.
 *
 * You should have received a copy of the Honest Public License
 * along with this program; if not, write to Funambol,
 * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
 */
/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

/* This file has been modified by Open Source Strategies, Inc. */

package org.opentaps.common.order.shoppingcart;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.order.shoppingcart.CartItemModifyException;
import org.ofbiz.order.shoppingcart.ItemNotFoundException;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.order.shoppingcart.ShoppingCartItem;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilMessage;

/**
 * Shopping Cart Services.
 */
public final class ShoppingCartServices {

    private ShoppingCartServices() { }

    private static final String MODULE = ShoppingCartServices.class.getName();

    /**
     * Loads a shopping cart from an existing.
     * Overrides the Ofbiz service to handle the new Quote entities / fields.
     * @param dctx a <code>DispatchContext</code> value
     * @param context a <code>Map</code> value
     * @return a <code>Map</code> value
     */
    @SuppressWarnings("unchecked")
    public static Map loadCartFromQuote(DispatchContext dctx, Map context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String quoteId = (String) context.get("quoteId");
        String applyQuoteAdjustmentsString = (String) context.get("applyQuoteAdjustments");
        Locale locale = (Locale) context.get("locale");

        boolean applyQuoteAdjustments = applyQuoteAdjustmentsString == null || "true".equals(applyQuoteAdjustmentsString);

        // get the quote header
        GenericValue quote = null;
        try {
            quote = delegator.findByPrimaryKey("Quote", UtilMisc.toMap("quoteId", quoteId));
        } catch (GenericEntityException e) {
            return UtilMessage.createAndLogServiceError(e, MODULE);
        }

        // initial require cart info
        String productStoreId = quote.getString("productStoreId");
        String currency = quote.getString("currencyUomId");

        // create the cart
        OpentapsShoppingCart cart = new OpentapsShoppingCart(delegator, productStoreId, locale, currency);
        // set shopping cart type
        if (quote.getString("quoteTypeId").equals("PURCHASE_QUOTE")) {
            cart.setOrderType("PURCHASE_ORDER");
            cart.setBillFromVendorPartyId(quote.getString("partyId"));
        }
        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            return UtilMessage.createAndLogServiceError(e, MODULE);
        }

        cart.setQuoteId(quoteId);
        cart.setOrderName(quote.getString("quoteName"));
        cart.setChannelType(quote.getString("salesChannelEnumId"));

        List quoteItems = null;
        List quoteAdjs = null;
        List quoteRoles = null;
        List quoteAttributes = null;
        try {
            quoteItems = quote.getRelated("QuoteItem", UtilMisc.toList("quoteItemSeqId"));
            quoteAdjs = quote.getRelated("QuoteAdjustment");
            quoteRoles = quote.getRelated("QuoteRole");
            quoteAttributes = quote.getRelated("QuoteAttribute");
        } catch (GenericEntityException e) {
            return UtilMessage.createAndLogServiceError(e, MODULE);
        }
        // set the role information
        cart.setOrderPartyId(quote.getString("partyId"));
        if (quoteRoles != null) {
            Iterator quoteRolesIt = quoteRoles.iterator();
            while (quoteRolesIt.hasNext()) {
                GenericValue quoteRole = (GenericValue) quoteRolesIt.next();
                String quoteRoleTypeId = quoteRole.getString("roleTypeId");
                String quoteRolePartyId = quoteRole.getString("partyId");
                if ("PLACING_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setPlacingCustomerPartyId(quoteRolePartyId);
                } else if ("BILL_TO_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setBillToCustomerPartyId(quoteRolePartyId);
                } else if ("SHIP_TO_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setShipToCustomerPartyId(quoteRolePartyId);
                } else if ("END_USER_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setEndUserCustomerPartyId(quoteRolePartyId);
                } else if ("BILL_FROM_VENDOR".equals(quoteRoleTypeId)) {
                    cart.setBillFromVendorPartyId(quoteRolePartyId);
                } else {
                    cart.addAdditionalPartyRole(quoteRolePartyId, quoteRoleTypeId);
                }
            }
        }

        // set the attribute information
        if (quoteAttributes != null) {
            Iterator quoteAttributesIt = quoteAttributes.iterator();
            while (quoteAttributesIt.hasNext()) {
                GenericValue quoteAttribute = (GenericValue) quoteAttributesIt.next();
                cart.setOrderAttribute(quoteAttribute.getString("attrName"), quoteAttribute.getString("attrValue"));
            }
        }

        // Convert the quote adjustment to order header adjustments and
        // put them in a map: the key/values pairs are quoteItemSeqId/List of adjs
        Map orderAdjsMap = new HashMap();
        Iterator quoteAdjsIter = quoteAdjs.iterator();
        while (quoteAdjsIter.hasNext()) {
            GenericValue quoteAdj = (GenericValue) quoteAdjsIter.next();
            List orderAdjs = (List) orderAdjsMap.get(quoteAdj.get("quoteItemSeqId"));
            if (orderAdjs == null) {
                orderAdjs = new LinkedList();
                orderAdjsMap.put(quoteAdj.get("quoteItemSeqId"), orderAdjs);
            }
            // convert quote adjustments to order adjustments
            GenericValue orderAdj = delegator.makeValue("OrderAdjustment");
            orderAdj.put("orderAdjustmentId", quoteAdj.get("quoteAdjustmentId"));
            orderAdj.put("orderAdjustmentTypeId", quoteAdj.get("quoteAdjustmentTypeId"));
            orderAdj.put("orderItemSeqId", quoteAdj.get("quoteItemSeqId"));
            orderAdj.put("comments", quoteAdj.get("comments"));
            orderAdj.put("description", quoteAdj.get("description"));
            orderAdj.put("amount", quoteAdj.get("amount"));
            orderAdj.put("productPromoId", quoteAdj.get("productPromoId"));
            orderAdj.put("productPromoRuleId", quoteAdj.get("productPromoRuleId"));
            orderAdj.put("productPromoActionSeqId", quoteAdj.get("productPromoActionSeqId"));
            orderAdj.put("productFeatureId", quoteAdj.get("productFeatureId"));
            orderAdj.put("correspondingProductId", quoteAdj.get("correspondingProductId"));
            orderAdj.put("sourceReferenceId", quoteAdj.get("sourceReferenceId"));
            orderAdj.put("sourcePercentage", quoteAdj.get("sourcePercentage"));
            orderAdj.put("customerReferenceId", quoteAdj.get("customerReferenceId"));
            orderAdj.put("primaryGeoId", quoteAdj.get("primaryGeoId"));
            orderAdj.put("secondaryGeoId", quoteAdj.get("secondaryGeoId"));
            orderAdj.put("exemptAmount", quoteAdj.get("exemptAmount"));
            orderAdj.put("taxAuthGeoId", quoteAdj.get("taxAuthGeoId"));
            orderAdj.put("taxAuthPartyId", quoteAdj.get("taxAuthPartyId"));
            orderAdj.put("overrideGlAccountId", quoteAdj.get("overrideGlAccountId"));
            orderAdj.put("includeInTax", quoteAdj.get("includeInTax"));
            orderAdj.put("includeInShipping", quoteAdj.get("includeInShipping"));
            orderAdj.put("createdDate", quoteAdj.get("createdDate"));
            orderAdj.put("createdByUserLogin", quoteAdj.get("createdByUserLogin"));
            orderAdjs.add(orderAdj);
        }

        long nextItemSeq = 0;
        if (quoteItems != null) {
            Iterator i = quoteItems.iterator();
            while (i.hasNext()) {
                GenericValue item = (GenericValue) i.next();

                // get the next item sequence id
                String orderItemSeqId = item.getString("quoteItemSeqId");
                try {
                    long seq = Long.parseLong(orderItemSeqId);
                    if (seq > nextItemSeq) {
                        nextItemSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    return UtilMessage.createAndLogServiceError(e, MODULE);
                }

                boolean isPromo = item.get("isPromo") != null && "Y".equals(item.getString("isPromo"));
                if (isPromo && !applyQuoteAdjustments) {
                    // do not include PROMO items
                    continue;
                }

                // not a promo item; go ahead and add it in
                BigDecimal amount = item.getBigDecimal("selectedAmount");
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }
                BigDecimal quantity = item.getBigDecimal("quantity");
                if (quantity == null) {
                    quantity = BigDecimal.ZERO;
                }
                BigDecimal quoteUnitPrice = item.getBigDecimal("quoteUnitPrice");
                if (quoteUnitPrice == null) {
                    quoteUnitPrice = BigDecimal.ZERO;
                }
                if (amount.signum() > 0) {
                    // If, in the quote, an amount is set, we need to
                    // pass to the cart the quoteUnitPrice/amount value.
                    quoteUnitPrice = quoteUnitPrice.divide(amount);
                }
                int itemIndex = -1;
                if (item.get("productId") == null) {
                    // non-product item
                    String desc = item.getString("comments");
                    try {
                        // note that passing in null for itemGroupNumber as there is no real grouping concept in the quotes right now
                        itemIndex = cart.addNonProductItem(null, desc, null, null, quantity, null, null, null, dispatcher);
                    } catch (CartItemModifyException e) {
                        return UtilMessage.createAndLogServiceError(e, MODULE);
                    }
                } else {
                    // product item
                    String productId = item.getString("productId");
                    try {
                        itemIndex = cart.addItemToEnd(productId, amount, quantity, quoteUnitPrice, null, null, null, null, dispatcher, new Boolean(!applyQuoteAdjustments), new Boolean(quoteUnitPrice.signum() == 0));
                    } catch (ItemNotFoundException e) {
                        return UtilMessage.createAndLogServiceError(e, MODULE);
                    } catch (CartItemModifyException e) {
                        return UtilMessage.createAndLogServiceError(e, MODULE);
                    }
                }

                // flag the item w/ the orderItemSeqId so we can reference it
                ShoppingCartItem cartItem = cart.findCartItem(itemIndex);
                cartItem.setOrderItemSeqId(orderItemSeqId);
                // attach additional item information
                cartItem.setItemComment(item.getString("comments"));
                cartItem.setQuoteId(item.getString("quoteId"));
                cartItem.setQuoteItemSeqId(item.getString("quoteItemSeqId"));
                cartItem.setIsPromo(isPromo);
                // set the cart item name as the quote item description if it is not empty
                if (UtilValidate.isNotEmpty(item.getString("description"))) {
                    cartItem.setName(item.getString("description"));
                }
            }

        }

        // If applyQuoteAdjustments is set to false then standard cart adjustments are used.
        if (applyQuoteAdjustments) {
            // The cart adjustments, derived from quote adjustments, are added to the cart
            List adjs = (List) orderAdjsMap.get(null);
            if (adjs != null) {
                cart.getAdjustments().addAll(adjs);
            }
            // The cart item adjustments, derived from quote item adjustments, are added to the cart
            if (quoteItems != null) {
                Iterator i = cart.iterator();
                while (i.hasNext()) {
                    ShoppingCartItem item = (ShoppingCartItem) i.next();
                    adjs = (List) orderAdjsMap.get(item.getOrderItemSeqId());
                    if (adjs != null) {
                        item.getAdjustments().addAll(adjs);
                    }
                }
            }
        }
        // set the item seq in the cart
        if (nextItemSeq > 0) {
            try {
                cart.setNextItemSeq(nextItemSeq);
            } catch (GeneralException e) {
                return UtilMessage.createAndLogServiceError(e, MODULE);
            }
        }

        Map result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        return result;
    }

    /**
     * Loads a shopping cart from an existing order, overrides the Ofbiz service to better handle multiple ship groups.
     * The only difference is on how ship groups are handled, here we create a map of shipGroupSeqId -> index to ensure that whatever assocs exist
     *  the item quantity is set to all ship groups, and the ship groups info doesn't get overwritten by a different ship group info.
     * and add copy order term support
     * @param dctx a <code>DispatchContext</code> value
     * @param context a <code>Map</code> value
     * @return a <code>Map</code> value
     */
    @SuppressWarnings("unchecked")
    public static Map loadCartFromOrder(DispatchContext dctx, Map context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Boolean skipInventoryChecks = (Boolean) context.get("skipInventoryChecks");
        Boolean skipProductChecks = (Boolean) context.get("skipProductChecks");
        Locale locale = (Locale) context.get("locale");

        if (UtilValidate.isEmpty(skipInventoryChecks)) {
            skipInventoryChecks = Boolean.FALSE;
        }
        if (UtilValidate.isEmpty(skipProductChecks)) {
            skipProductChecks = Boolean.FALSE;
        }

        // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return ServiceUtil.returnError(e.getMessage());
        }

        // initial require cart info
        OrderReadHelper orh = new OrderReadHelper(orderHeader);
        String productStoreId = orh.getProductStoreId();
        String orderTypeId = orh.getOrderTypeId();
        String currency = orh.getCurrency();
        String website = orh.getWebSiteId();

        // create the cart
        OpentapsShoppingCart cart = new OpentapsShoppingCart(delegator, productStoreId, website, locale, currency);
        cart.setOrderType(orderTypeId);
        cart.setChannelType(orderHeader.getString("salesChannelEnumId"));
        cart.setInternalCode(orderHeader.getString("internalCode"));
        String orderName = orderHeader.getString("orderName");
        if (UtilValidate.isNotEmpty(orderName)) {
            cart.setOrderName(orderName);
        }

        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            return UtilMessage.createAndLogServiceError(e, MODULE);
        }

        // set the role information
        GenericValue placingParty = orh.getPlacingParty();
        if (placingParty != null) {
            cart.setPlacingCustomerPartyId(placingParty.getString("partyId"));
        }

        GenericValue billFromParty = orh.getBillFromParty();
        if (billFromParty != null) {
            cart.setBillFromVendorPartyId(billFromParty.getString("partyId"));
        }

        GenericValue billToParty = orh.getBillToParty();
        if (billToParty != null) {
            cart.setBillToCustomerPartyId(billToParty.getString("partyId"));
        }

        GenericValue shipToParty = orh.getShipToParty();
        if (shipToParty != null) {
            cart.setShipToCustomerPartyId(shipToParty.getString("partyId"));
        }

        GenericValue endUserParty = orh.getEndUserParty();
        if (endUserParty != null) {
            cart.setEndUserCustomerPartyId(endUserParty.getString("partyId"));
            cart.setOrderPartyId(endUserParty.getString("partyId"));
        }

        // load the payment infos
        List<GenericValue> orderPaymentPrefs = null;
        try {
            List exprs = UtilMisc.toList(new EntityExpr("orderId", EntityOperator.EQUALS, orderId));
            exprs.add(new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_RECEIVED"));
            exprs.add(new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_CANCELLED"));
            exprs.add(new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_DECLINED"));
            exprs.add(new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_SETTLED"));
            EntityCondition cond = new EntityConditionList(exprs, EntityOperator.AND);
            orderPaymentPrefs = delegator.findByCondition("OrderPaymentPreference", cond, null, null);
        } catch (GenericEntityException e) {
            return UtilMessage.createAndLogServiceError(e, MODULE);
        }
        if (orderPaymentPrefs != null && orderPaymentPrefs.size() > 0) {
            Iterator<GenericValue> oppi = orderPaymentPrefs.iterator();
            while (oppi.hasNext()) {
                GenericValue opp = oppi.next();
                String paymentId = opp.getString("paymentMethodId");
                if (paymentId == null) {
                    paymentId = opp.getString("paymentMethodTypeId");
                }
                BigDecimal maxAmount = opp.getBigDecimal("maxAmount");
                String overflow = opp.getString("overflowFlag");

                ShoppingCart.CartPaymentInfo cpi = null;

                if ((overflow == null || !"Y".equals(overflow)) && oppi.hasNext()) {
                    cpi = cart.addPaymentAmount(paymentId, maxAmount);
                    Debug.log("Added Payment: " + paymentId + " / " + maxAmount, MODULE);
                } else {
                    cpi = cart.addPayment(paymentId);
                    Debug.log("Added Payment: " + paymentId + " / [no max]", MODULE);
                }
                // for finance account the finAccountId needs to be set
                if ("FIN_ACCOUNT".equals(paymentId)) {
                    cpi.finAccountId = opp.getString("finAccountId");
                }
            }
        } else {
            Debug.log("No payment preferences found for order #" + orderId, MODULE);
        }

        List<GenericValue> orderItems = orh.getValidOrderItems();

        // Fixed ship group handling
        // list of existing ship groups and keep a map of shipGroupSeqId -> cart index
        List<GenericValue> shipGroups = orh.getOrderItemShipGroups();
        Map<String, Integer> shipGroupsMap = new HashMap<String, Integer>();

        // get the ship groups and their indexes
        for (int g = 0; g < shipGroups.size(); g++) {
            GenericValue sg = shipGroups.get(g);
            shipGroupsMap.put(sg.getString("shipGroupSeqId"), new Integer(g));
        }

        long nextItemSeq = 0;
        for (GenericValue item : orderItems) {

            // get the next item sequence id
            String orderItemSeqId = item.getString("orderItemSeqId");
            try {
                long seq = Long.parseLong(orderItemSeqId);
                if (seq > nextItemSeq) {
                    nextItemSeq = seq;
                }
            } catch (NumberFormatException e) {
                return UtilMessage.createAndLogServiceError(e, MODULE);
            }

            // do not include PROMO items
            if (item.get("isPromo") != null && "Y".equals(item.getString("isPromo"))) {
                continue;
            }

            // not a promo item; go ahead and add it in
            BigDecimal amount = item.getBigDecimal("selectedAmount");
            if (amount == null) {
                amount = BigDecimal.ZERO;
            }
            BigDecimal quantity = item.getBigDecimal("quantity");
            BigDecimal quantityCanceled = item.getBigDecimal("cancelQuantity");
            if (quantity != null) {
                if (quantityCanceled != null) {
                    quantity = quantity.subtract(quantityCanceled);
                }
            }
            int itemIndex = -1;
            if (item.get("productId") == null) {
                // non-product item
                String itemType = item.getString("orderItemTypeId");
                String desc = item.getString("itemDescription");
                try {
                    // TODO: passing in null now for itemGroupNumber, but should reproduce from OrderItemGroup records
                    itemIndex = cart.addNonProductItem(itemType, desc, null, null, quantity, null, null, null, dispatcher);
                } catch (CartItemModifyException e) {
                    return UtilMessage.createAndLogServiceError(e, MODULE);
                    }
            } else {
                // product item
                String prodCatalogId = item.getString("prodCatalogId");
                String productId = item.getString("productId");
                try {
                    itemIndex = cart.addItemToEnd(productId, amount, quantity, null, null, null, prodCatalogId, item.getString("orderItemTypeId"), dispatcher, null, null, skipInventoryChecks, skipProductChecks);
                } catch (ItemNotFoundException e) {
                    return UtilMessage.createAndLogServiceError(e, MODULE);
                } catch (CartItemModifyException e) {
                    return UtilMessage.createAndLogServiceError(e, MODULE);
                }
            }

            // flag the item w/ the orderItemSeqId so we can reference it
            ShoppingCartItem cartItem = cart.findCartItem(itemIndex);
            cartItem.setOrderItemSeqId(item.getString("orderItemSeqId"));

            // attach addition item information
            cartItem.setStatusId(item.getString("statusId"));
            cartItem.setItemType(item.getString("orderItemTypeId"));
            cartItem.setItemComment(item.getString("comments"));
            cartItem.setQuoteId(item.getString("quoteId"));
            cartItem.setQuoteItemSeqId(item.getString("quoteItemSeqId"));
            cartItem.setProductCategoryId(item.getString("productCategoryId"));
            cartItem.setDesiredDeliveryDate(item.getTimestamp("estimatedDeliveryDate"));
            cartItem.setShipBeforeDate(item.getTimestamp("shipBeforeDate"));
            cartItem.setShipAfterDate(item.getTimestamp("shipAfterDate"));
            cartItem.setShoppingList(item.getString("shoppingListId"), item.getString("shoppingListItemSeqId"));
            cartItem.setIsModifiedPrice("Y".equals(item.getString("isModifiedPrice")));
            if (cartItem.getIsModifiedPrice()) {
                cartItem.setBasePrice(item.getBigDecimal("unitPrice"));
            }

            // preserve the order item cancel quantity
            cart.setCancelQuantity(itemIndex, item.getBigDecimal("cancelQuantity"));

            // set the accounting tag as cart item attributes (similar to the order entry)
            for (int i = 1; i <= UtilAccountingTags.TAG_COUNT; i++) {
                cartItem.setAttribute(UtilAccountingTags.TAG_PARAM_PREFIX + i, item.getString(UtilAccountingTags.ENTITY_TAG_PREFIX + i));
            }

            // set the PO number on the cart
            cart.setPoNumber(item.getString("correspondingPoId"));

            // Fixed ship group handling
            // set 0 qty for all ship groups, else it will assume the quantity set later for some reasons
            // also ensures that all cart ship group info are created to avoid index out of bound exceptions
            // Note: we cannot create the ship group infos before adding the item because when adding an item to
            //  the cart, the cart will cleanup empty ship groups which is not what we want here
            for (int g = 0; g < shipGroups.size(); g++) {
                GenericValue sg = shipGroups.get(g);
                cart.setShipAfterDate(g, sg.getTimestamp("shipAfterDate"));
                cart.setShipBeforeDate(g, sg.getTimestamp("shipByDate"));
                cart.setShipmentMethodTypeId(g, sg.getString("shipmentMethodTypeId"));
                cart.setCarrierPartyId(g, sg.getString("carrierPartyId"));
                cart.setSupplierPartyId(g, sg.getString("supplierPartyId"));
                cart.setMaySplit(g, sg.getBoolean("maySplit"));
                cart.setGiftMessage(g, sg.getString("giftMessage"));
                cart.setShippingContactMechId(g, sg.getString("contactMechId"));
                cart.setShippingInstructions(g, sg.getString("shippingInstructions"));
                cart.setItemShipGroupQty(itemIndex, BigDecimal.ZERO, g);
            }

            // set the item's ship group qty
            List<GenericValue> itemShipGroups = orh.getOrderItemShipGroupAssocs(item);
            for (int g = 0; g < itemShipGroups.size(); g++) {
                GenericValue sgAssoc = itemShipGroups.get(g);
                BigDecimal shipGroupQty = OrderReadHelper.getOrderItemShipGroupQuantity(sgAssoc);
                if (shipGroupQty == null) {
                    shipGroupQty = BigDecimal.ZERO;
                }

                GenericValue sg = null;
                try {
                    sg = sgAssoc.getRelatedOne("OrderItemShipGroup");
                } catch (GenericEntityException e) {
                    return UtilMessage.createAndLogServiceError(e, MODULE);
                }

                // Fixed ship group handling
                // use the map to get the proper index, else a missing assoc to the first ship group makes the counter "g" to be misaligned
                // resulting in the second ship group info overwriting the first
                int shipGroupIndex = shipGroupsMap.get(sg.getString("shipGroupSeqId")).intValue();
                cart.setItemShipGroupQty(itemIndex, shipGroupQty, shipGroupIndex);

            }
        }

        // set the item seq in the cart
        if (nextItemSeq > 0) {
            try {
                cart.setNextItemSeq(nextItemSeq);
            } catch (GeneralException e) {
                return UtilMessage.createAndLogServiceError(e, MODULE);
            }
        }

        // load the order term infos
        List<GenericValue> orderTerms = orh.getOrderTerms();
        Debug.logInfo("Load order term for order #" + orderId + ", orderTerms.size()=" + orderTerms.size(), MODULE);
        for (GenericValue term : orderTerms) {
          //put them into cart
            GenericValue orderTerm = GenericValue.create(delegator.getModelEntity("OrderTerm"));
            orderTerm.put("termTypeId", term.get("termTypeId"));
            orderTerm.put("termValue", term.get("termValue"));
            orderTerm.put("termDays", term.get("termDays"));
            orderTerm.put("textValue", term.get("textValue"));
            orderTerm.put("description", term.get("description"));
            orderTerm.put("uomId", term.get("uomId"));
            cart.addOrderTerm(orderTerm);
        }
        Map result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        return result;
    }
}
