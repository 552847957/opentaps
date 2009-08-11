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
package org.opentaps.dataimport;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.party.PartyContactHelper;

/**
 * Import orders via intermediate DataImportOrderHeader and DataImportOrderItem entities.
 *
 * @author     <a href="mailto:cliberty@opensourcestrategies.com">Chris Liberty</a>
 * @version    $Rev$
 */
public class OrderImportServices {

    public static String module = OrderImportServices.class.getName();
    public static final int decimals = UtilNumber.getBigDecimalScale("order.decimals");
    public static final int rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");
    public static final BigDecimal ZERO = (new BigDecimal("0")).setScale(decimals, rounding);

    // this constant value is used in various places
    protected static final String defaultShipGroupSeqId = "00001";

    /**
     * Describe <code>importOrders</code> method here.
     *
     * @param dctx a <code>DispatchContext</code> value
     * @param context a <code>Map</code> value
     * @return a <code>Map</code> value
     */
    @SuppressWarnings("unchecked")
    public static Map importOrders(DispatchContext dctx, Map context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        String companyPartyId = (String) context.get("companyPartyId");
        String productStoreId = (String) context.get("productStoreId");
        String prodCatalogId = (String) context.get("prodCatalogId");
        // AG24012008: purchaseOrderShipToContactMechId is needed to get the MrpOrderInfo.shipGroupContactMechId used by the MRP run
        String purchaseOrderShipToContactMechId = (String) context.get("purchaseOrderShipToContactMechId");
        Boolean importEmptyOrders = (Boolean) context.get("importEmptyOrders");
        Boolean calculateGrandTotal = (Boolean) context.get("calculateGrandTotal");
        Boolean reserveInventory = (Boolean) context.get("reserveInventory");
        if (reserveInventory == null) {
            reserveInventory = Boolean.FALSE;
        }

        int imported = 0;

        // main try/catch block that traps errors related to obtaining data from delegator
        try {

            // Make sure the productStore exists
            GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));

            if (UtilValidate.isEmpty(productStore)) {
                String errMsg = "Error in importOrders service: product store [" + productStoreId + "] does not exist";
                Debug.logError(errMsg, OrderImportServices.module);
                return ServiceUtil.returnError(errMsg);
            }

            // Make sure the productCatalog exists
            if (UtilValidate.isNotEmpty(prodCatalogId)) {
                GenericValue productCatalog = delegator.findByPrimaryKey("ProdCatalog", UtilMisc.toMap("prodCatalogId", prodCatalogId));

                if (UtilValidate.isEmpty(productCatalog)) {
                    String errMsg = "Error in importOrders service: product catalog [" + productCatalog + "] does not exist";
                    Debug.logError(errMsg, OrderImportServices.module);
                    return ServiceUtil.returnError(errMsg);
                }
            }

            // Make sure the company party exists
            GenericValue companyParty = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", companyPartyId));

            if (UtilValidate.isEmpty(companyParty)) {
                String errMsg = "Error in importOrders service: company party [" + companyPartyId + "] does not exist";
                Debug.logError(errMsg, OrderImportServices.module);
                return ServiceUtil.returnError(errMsg);
            }

            // Ensure the party role for the company
            List billFromRoles = companyParty.getRelatedByAnd("PartyRole", UtilMisc.toMap("roleTypeId", "BILL_FROM_VENDOR"));
            if (billFromRoles.size() == 0) {
                delegator.create("PartyRole", UtilMisc.toMap("partyId", companyPartyId, "roleTypeId", "BILL_FROM_VENDOR"));
            }

            // need to get an ELI because of possibly large number of records.  productId <> null will get all records
            EntityConditionList conditions = new EntityConditionList(UtilMisc.toList(
                        new EntityExpr("orderId", EntityOperator.NOT_EQUAL, null),
                        new EntityExpr("processedTimestamp", EntityOperator.EQUALS, null)   // leave out previously processed orders
                        ), EntityOperator.AND);
            TransactionUtil.begin();   // since the service is not inside a transaction, this needs to be in its own transaction, or you'll get a harmless exception
            EntityListIterator importOrderHeaders = delegator.findListIteratorByCondition("DataImportOrderHeader", conditions, null, null);
            TransactionUtil.commit();

            GenericValue orderHeader = null;
            while ((orderHeader = (GenericValue) importOrderHeaders.next()) != null) {

                try {

                    List<GenericValue> toStore = OrderImportServices.decodeOrder(orderHeader, companyPartyId, productStore, prodCatalogId, purchaseOrderShipToContactMechId, importEmptyOrders.booleanValue(), calculateGrandTotal.booleanValue(), reserveInventory, delegator, userLogin);
                    if (toStore == null) {
                        Debug.logWarning("Import of orderHeader[" + orderHeader.get("orderId") + "] was unsuccessful.", OrderImportServices.module);
                    }

                    TransactionUtil.begin();

                    delegator.storeAll(toStore);

                    // make reservation if requested
                    if (reserveInventory && !orderHeader.getBoolean("orderClosed") && "SALES_ORDER".equals(orderHeader.getString("orderTypeId"))) {
                        Debug.logInfo("Starting product reservation against order [" + orderHeader.getString("orderId") + "]", module);

                        String reserveOrderEnumId = productStore.getString("reserveOrderEnumId");
                        if (UtilValidate.isEmpty(reserveOrderEnumId)) {
                            reserveOrderEnumId = "INVRO_FIFO_REC";
                        }

                        for (GenericValue currentEntity : toStore) {
                            String entityName = currentEntity.getEntityName();
                            if (!"OrderItem".equals(entityName)) {
                                continue;
                            }

                            // we have order item
                            Debug.logInfo("Reserve order item [" + currentEntity.getString("orderItemSeqId") + "]" , module);
                            Map<String, Object> callCtxt = FastMap.newInstance();
                            callCtxt.put("productStoreId", productStoreId);
                            callCtxt.put("productId", currentEntity.getString("productId"));
                            callCtxt.put("orderId", currentEntity.getString("orderId"));
                            callCtxt.put("orderItemSeqId", currentEntity.getString("orderItemSeqId"));
                            callCtxt.put("shipGroupSeqId", defaultShipGroupSeqId);
                            callCtxt.put("quantity", currentEntity.getDouble("quantity"));
                            callCtxt.put("userLogin", userLogin);

                            Map<String, Object> callResult = dispatcher.runSync("reserveStoreInventory", callCtxt);
                            if (ServiceUtil.isError(callResult)) {
                                Debug.logWarning("reserveStoreInventory returned error " + ServiceUtil.getErrorMessage(callResult), module);
                                TransactionUtil.rollback();
                            }

                            Debug.logWarning("The order item is reserved successfully", module);
                        }
                    }

                    Debug.logInfo("Successfully imported orderHeader [" + orderHeader.get("orderId") + "].", OrderImportServices.module);
                    imported++;

                    TransactionUtil.commit();

                } catch (GenericEntityException e) {
                    TransactionUtil.rollback();
                    Debug.logError(e, "Failed to import orderHeader[" + orderHeader.get("orderId") + "]. Error stack follows.", OrderImportServices.module);
                } catch (Exception e) {
                    TransactionUtil.rollback();
                    Debug.logError(e, "Import of orderHeader[" + orderHeader.get("orderId") + "] was unsuccessful. Error stack follows.", OrderImportServices.module);
                }
            }
            importOrderHeaders.close();

        } catch (GenericEntityException e) {
            String errMsg = "Error in importOrders service: " + e.getMessage();
            Debug.logError(e, errMsg, OrderImportServices.module);
            return ServiceUtil.returnError(errMsg);
        }

        Map results = ServiceUtil.returnSuccess();
        results.put("ordersImported", new Integer(imported));
        return results;
    }

    /**
     * Helper method to decode a DataImportOrderHeader/DataImportOrderItem into a List of GenericValues modeling that product in the OFBiz schema.
     * If for some reason obtaining data via the delegator fails, this service throws that exception.
     * Note that everything is done with the delegator for maximum efficiency.
     * @param externalOrderHeader a <code>GenericValue</code> value
     * @param companyPartyId a <code>String</code> value
     * @param productStore a <code>GenericValue</code> value
     * @param prodCatalogId a <code>String</code> value
     * @param purchaseOrderShipToContactMechId
     * @param importEmptyOrders a <code>boolean</code> value
     * @param calculateGrandTotal a <code>boolean</code> value
     * @param reserveInventory a <code>boolean</code> value
     * @param delegator a <code>GenericDelegator</code> value
     * @param userLogin a <code>GenericValue</code> value
     * @return a <code>List</code> value
     * @exception GenericEntityException if an error occurs
     * @exception Exception if an error occurs
     */
    @SuppressWarnings("unchecked")
    private static List decodeOrder(GenericValue externalOrderHeader, String companyPartyId, GenericValue productStore, String prodCatalogId, String purchaseOrderShipToContactMechId, boolean importEmptyOrders, boolean calculateGrandTotal, boolean reserveInventory, GenericDelegator delegator, GenericValue userLogin) throws GenericEntityException, Exception {
        String orderId = externalOrderHeader.getString("orderId");
        String orderTypeId = externalOrderHeader.getString("orderTypeId");
        if (UtilValidate.isEmpty(orderTypeId)) {
            orderTypeId = "SALES_ORDER";
        }
        Timestamp orderDate = externalOrderHeader.getTimestamp("orderDate");
        if (UtilValidate.isEmpty(orderDate)) {
            orderDate = UtilDateTime.nowTimestamp();
        }
        boolean isSalesOrder = "SALES_ORDER".equals(orderTypeId);
        boolean isPurchaseOrder = "PURCHASE_ORDER".equals(orderTypeId);

        Debug.logInfo("Importing orderHeader[" + orderId + "]", OrderImportServices.module);

        // Check to make sure that an order with this ID doesn't already exist
        GenericValue existingOrderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        if (existingOrderHeader != null) {
            Debug.logError("Ignoring duplicate orderHeader[" + orderId + "]", OrderImportServices.module);
            return FastList.newInstance();
        }

        String orderStatusId = externalOrderHeader.getBoolean("orderClosed").booleanValue() ? "ORDER_COMPLETED" : "ORDER_APPROVED";

        // OrderHeader

        Map orderHeaderInput = FastMap.newInstance();
        orderHeaderInput.put("orderId", orderId);
        orderHeaderInput.put("orderTypeId", orderTypeId);
        orderHeaderInput.put("orderName", orderId);
        orderHeaderInput.put("externalId", orderId);
        orderHeaderInput.put("salesChannelEnumId", "UNKNWN_SALES_CHANNEL");
        orderHeaderInput.put("orderDate", orderDate);
        orderHeaderInput.put("entryDate", UtilDateTime.nowTimestamp());
        orderHeaderInput.put("statusId", orderStatusId);
        orderHeaderInput.put("currencyUom", externalOrderHeader.get("currencyUomId"));
        orderHeaderInput.put("remainingSubTotal", new Double(0));
        orderHeaderInput.put("productStoreId", isPurchaseOrder ? "PURCHASING" : productStore.getString("productStoreId"));

        // main customer and bill to party
        String customerPartyId = externalOrderHeader.getString("customerPartyId");
        String supplierPartyId = externalOrderHeader.getString("supplierPartyId");
        orderHeaderInput.put("billFromPartyId", isPurchaseOrder ? supplierPartyId : companyPartyId);
        orderHeaderInput.put("billToPartyId", isPurchaseOrder ? companyPartyId : customerPartyId);

        List orderAdjustments = new ArrayList();

        // todo:Make orderAdjustments from adjustmentsTotal and taxTotal
        // Record an OrderStatus
        Map orderStatusInput = UtilMisc.toMap("orderStatusId", delegator.getNextSeqId("OrderStatus"), "orderId", orderId, "statusId", orderStatusId, "statusDatetime", orderDate, "statusUserLogin", userLogin.getString("userLoginId"));

        // purchase orders must be assigned to a ship group
        GenericValue oisg = null;
        if (isPurchaseOrder) {
            oisg = delegator.makeValue("OrderItemShipGroup", null);
            oisg.put("orderId", orderId);
            oisg.put("shipGroupSeqId", defaultShipGroupSeqId);
            oisg.put("carrierPartyId", "_NA_");
            oisg.put("carrierRoleTypeId", "CARRIER");
            oisg.put("maySplit", "N");
            oisg.put("isGift", "N");
            if (UtilValidate.isNotEmpty(purchaseOrderShipToContactMechId)) {
                oisg.put("contactMechId", purchaseOrderShipToContactMechId);
            }

        }

        // create a ship group for the sales order
        if (isSalesOrder) {
            // get requested shipping method
            String productStoreShipMethId = externalOrderHeader.getString("productStoreShipMethId");

            GenericValue shipMeth = delegator.findByPrimaryKeyCache("ProductStoreShipmentMeth", UtilMisc.toMap("productStoreShipMethId", productStoreShipMethId));
            if (shipMeth == null) {
                Debug.logWarning("Customer [" + customerPartyId + "] has no shipping method specified.  Assuming No Shipping.", module);
                shipMeth = delegator.makeValue("ProductStoreShipmentMeth", UtilMisc.toMap("partyId", "_NA_", "roleTypeId", "CARRIER", "shipmentMethodTypeId", "NO_SHIPPING"));
            }
            String shipmentMethodTypeId = shipMeth.getString("shipmentMethodTypeId");
            String carrierPartyId = shipMeth.getString("partyId");
            String carrierRoleTypeId = shipMeth.getString("roleTypeId");

            List<GenericValue> shippingAddresses = PartyContactHelper.getContactMechsByPurpose(customerPartyId, "POSTAL_ADDRESS", "SHIPPING_LOCATION", true, delegator);
            if (shippingAddresses.size() > 1) {
                Debug.logWarning("Customer [" + customerPartyId + "] has more than one shipping address.  Using first one.", module);
            }
            if (shippingAddresses.size() == 0) {
                Debug.logInfo("No shipping address found for customer [" + customerPartyId + "].  Not creating ship group for the order.", module);
            } else {
                String contactMechId = EntityUtil.getFirst(shippingAddresses).getString("contactMechId");
                oisg = delegator.makeValue("OrderItemShipGroup", null);
                oisg.put("orderId", orderId);
                oisg.put("shipGroupSeqId", defaultShipGroupSeqId);
                oisg.put("carrierPartyId", carrierPartyId);
                oisg.put("carrierRoleTypeId", carrierRoleTypeId);
                oisg.put("shipmentMethodTypeId", shipmentMethodTypeId);
                oisg.put("maySplit", "N");
                oisg.put("isGift", "N");
                oisg.put("contactMechId", contactMechId);
                Debug.logInfo("Created ship group for order at PostalAddress [" + contactMechId + "]", module);
            }
        }

        // handle the shipping total as a whole order one
        Double shippingTotal = externalOrderHeader.getDouble("shippingTotal");
        if (shippingTotal != null && shippingTotal > 0.0) {
            GenericValue adj = delegator.makeValue("OrderAdjustment", null);
            adj.put("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
            adj.put("orderAdjustmentTypeId", "SHIPPING_CHARGES");
            adj.put("orderId", orderId);
            adj.put("orderItemSeqId", "_NA_");
            adj.put("shipGroupSeqId", defaultShipGroupSeqId);
            adj.put("amount", shippingTotal);
            orderAdjustments.add(adj);
        }

        // whole order tax, which must have orderTax and taxAuthPartyId defined
        Double orderTax = externalOrderHeader.getDouble("orderTax");
        String taxAuthPartyId = externalOrderHeader.getString("taxAuthPartyId");
        if (orderTax != null && orderTax > 0.0 && taxAuthPartyId != null) {
            GenericValue taxAuth = EntityUtil.getFirst(delegator.findByAndCache("TaxAuthority", UtilMisc.toMap("taxAuthPartyId", taxAuthPartyId)));
            if (taxAuth == null) {
                Debug.logWarning("Order [" + orderId + "] has a tax to an unknown tax authority.  No entry for taxAuthPartyId [" + taxAuthPartyId + "] found in TaxAuthority.", module);
            } else {
                GenericValue adj = delegator.makeValue("OrderAdjustment", null);
                adj.put("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                adj.put("orderAdjustmentTypeId", "SALES_TAX");
                adj.put("orderId", orderId);
                adj.put("orderItemSeqId", "_NA_");
                adj.put("shipGroupSeqId", defaultShipGroupSeqId);
                adj.put("taxAuthPartyId", taxAuth.get("taxAuthPartyId"));
                adj.put("taxAuthGeoId", taxAuth.get("taxAuthGeoId"));
                adj.put("amount", orderTax);
                orderAdjustments.add(adj);
            }
        }

        // OrderItems

        List orderItemConditions = UtilMisc.toList(new EntityExpr("orderId", EntityOperator.EQUALS, orderId), new EntityExpr("processedTimestamp", EntityOperator.EQUALS, null));
        List externalOrderItems = delegator.findByCondition("DataImportOrderItem", new EntityConditionList(orderItemConditions, EntityOperator.AND), null, null); //getExternalOrderItems(externalOrderHeader.getString("orderId"), delegator);

        // If orders without orderItems should not be imported, return now without doing anything
        if (UtilValidate.isEmpty(externalOrderItems) && !importEmptyOrders) {
            return FastList.newInstance();
        }

        // item status depends on order status
        String itemStatus = "ORDER_COMPLETED".equals(orderStatusId) ? "ITEM_COMPLETED" : "ITEM_APPROVED";

        List orderItems = new ArrayList();
        List oisgAssocs = new ArrayList();
        for (int count = 0; count < externalOrderItems.size(); count++) {
            GenericValue externalOrderItem = (GenericValue) externalOrderItems.get(count);

            String orderItemSeqId = UtilFormatOut.formatPaddedNumber(count + 1, 5);
            Double quantity = UtilValidate.isEmpty(externalOrderItem.get("quantity")) ? new Double(0) : externalOrderItem.getDouble("quantity");

            Map orderItemInput = FastMap.newInstance();
            orderItemInput.put("orderId", orderId);
            orderItemInput.put("orderItemSeqId", orderItemSeqId);
            orderItemInput.put("orderItemTypeId", "PRODUCT_ORDER_ITEM");
            if (UtilValidate.isNotEmpty(externalOrderItem.get("productId"))) {
                orderItemInput.put("productId", externalOrderItem.getString("productId"));
                GenericValue product = externalOrderItem.getRelatedOneCache("Product");
                orderItemInput.put("itemDescription", product.getString("productName"));
            }
            if (isSalesOrder && UtilValidate.isNotEmpty(prodCatalogId)) {
                orderItemInput.put("prodCatalogId", prodCatalogId);
            }
            orderItemInput.put("isPromo", "N");
            orderItemInput.put("quantity", quantity);
            orderItemInput.put("selectedAmount", new Double(0));
            if (UtilValidate.isNotEmpty(externalOrderItem.get("price"))) {
                orderItemInput.put("unitPrice", externalOrderItem.getDouble("price"));
                orderItemInput.put("unitListPrice", externalOrderItem.getDouble("price"));
            } else {
                orderItemInput.put("unitPrice", new Double(0));
                orderItemInput.put("unitListPrice", new Double(0));
            }
            orderItemInput.put("isModifiedPrice", "N");
            if (UtilValidate.isNotEmpty(externalOrderItem.get("comments"))) {
                orderItemInput.put("comments", externalOrderItem.getString("comments"));
            }
            if (UtilValidate.isNotEmpty(externalOrderItem.get("customerPo"))) {
                orderItemInput.put("correspondingPoId", externalOrderItem.getString("customerPo"));
            }
            orderItemInput.put("statusId", itemStatus);
            orderItems.add(delegator.makeValue("OrderItem", orderItemInput));

            // purchase orders must assign all their order items to the oisg
            if ((isPurchaseOrder && oisg != null) || (isSalesOrder && reserveInventory && oisg != null)) {
                Debug.logInfo("Begin to create OrderItemShipGroupAssoc", module);
                Map oisgAssocInput = FastMap.newInstance();
                oisgAssocInput.put("orderId", orderId);
                oisgAssocInput.put("orderItemSeqId", orderItemSeqId);
                oisgAssocInput.put("shipGroupSeqId", defaultShipGroupSeqId);
                oisgAssocInput.put("quantity", quantity);
                oisgAssocs.add(delegator.makeValue("OrderItemShipGroupAssoc", oisgAssocInput));
                Debug.logInfo("OrderItemShipGroupAssoc is created", module);
            }

            // line item tax, which must have itemTax and taxAuthPartyId defined
            Double itemTax = externalOrderItem.getDouble("itemTax");
            taxAuthPartyId = externalOrderItem.getString("taxAuthPartyId");
            if (itemTax != null && itemTax > 0.0 && taxAuthPartyId != null) {
                GenericValue taxAuth = EntityUtil.getFirst(delegator.findByAndCache("TaxAuthority", UtilMisc.toMap("taxAuthPartyId", taxAuthPartyId)));
                if (taxAuth == null) {
                    Debug.logWarning("Order Item [" + orderId + "," + orderItemSeqId + "] has a tax to an unknown tax authority.  No entry for taxAuthPartyId [" + taxAuthPartyId + "] found in TaxAuthority.", module);
                } else {
                    GenericValue adj = delegator.makeValue("OrderAdjustment", null);
                    adj.put("orderAdjustmentId", delegator.getNextSeqId("OrderAdjustment"));
                    adj.put("orderAdjustmentTypeId", "SALES_TAX");
                    adj.put("orderId", orderId);
                    adj.put("orderItemSeqId", orderItemSeqId);
                    adj.put("shipGroupSeqId", defaultShipGroupSeqId);
                    adj.put("taxAuthPartyId", taxAuth.get("taxAuthPartyId"));
                    adj.put("taxAuthGeoId", taxAuth.get("taxAuthGeoId"));
                    adj.put("amount", itemTax);
                    orderAdjustments.add(adj);
                }
            }

            externalOrderItem.set("processedTimestamp", UtilDateTime.nowTimestamp());
            externalOrderItem.set("orderItemSeqId", orderItemSeqId);
        }

        BigDecimal orderGrandTotal;
        if (calculateGrandTotal) {

            // Get the grand total from the order items and order adjustments
            orderGrandTotal = getOrderGrandTotal(orderAdjustments, orderItems);
            if (orderGrandTotal.compareTo(BigDecimal.ZERO) == 0) {
                Debug.logWarning("Order [" + orderId + "] had a zero calculated total, so we are using the DataImportOrderHeader grand total of [" + externalOrderHeader.getBigDecimal("grandTotal") + "]", module);
                orderGrandTotal = externalOrderHeader.getBigDecimal("grandTotal").setScale(decimals, rounding);
            }
        } else {
            orderGrandTotal = externalOrderHeader.getBigDecimal("grandTotal").setScale(decimals, rounding);
        }

        // updade order header for total
        orderHeaderInput.put("grandTotal", new Double(orderGrandTotal.doubleValue()));

        // OrderRoles

        // create the bill to party order role
        List roles = FastList.newInstance();
        if (isSalesOrder && UtilValidate.isNotEmpty(customerPartyId)) {

            // Make sure the customer party exists
            GenericValue party = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", customerPartyId));

            if (UtilValidate.isEmpty(party)) {
                Debug.logError("CustomerPartyId [" + customerPartyId + "] not found - not creating BILL_TO_CUSTOMER order role for orderId [" + orderId + "]", module);
            } else {
                // Ensure the party roles for the customer
                roles.addAll(UtilImport.ensurePartyRoles(customerPartyId, UtilMisc.toList("PLACING_CUSTOMER", "BILL_TO_CUSTOMER", "SHIP_TO_CUSTOMER", "END_USER_CUSTOMER"), delegator));

                // Create the customer order roles
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", customerPartyId, "roleTypeId", "PLACING_CUSTOMER")));
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", customerPartyId, "roleTypeId", "BILL_TO_CUSTOMER")));
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", customerPartyId, "roleTypeId", "SHIP_TO_CUSTOMER")));
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", customerPartyId, "roleTypeId", "END_USER_CUSTOMER")));
            }
        }
        if (isPurchaseOrder) {
            roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", companyPartyId, "roleTypeId", "BILL_TO_CUSTOMER")));
        }

        // Create the bill from vendor order role
        if (isSalesOrder) {
            roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", companyPartyId, "roleTypeId", "BILL_FROM_VENDOR")));
        }
        if (isPurchaseOrder) {
            // Make sure the supplier party exists
            GenericValue supplier = delegator.findByPrimaryKey("Party", UtilMisc.toMap("partyId", supplierPartyId));
            if (supplier == null) {
                Debug.logError("SupplierPartyId [" + supplierPartyId + "] not found - not creating BILL_FROM_VENDOR order role for orderId [" + orderId + "]", module);
            } else {
                roles.addAll(UtilImport.ensurePartyRoles(supplierPartyId, UtilMisc.toList("SUPPLIER", "SUPPLIER_AGENT", "BILL_FROM_VENDOR", "SHIP_FROM_VENDOR"), delegator));

                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", supplierPartyId, "roleTypeId", "BILL_FROM_VENDOR")));
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", supplierPartyId, "roleTypeId", "SHIP_FROM_VENDOR")));
                roles.add(delegator.makeValue("OrderRole", UtilMisc.toMap("orderId", orderId, "partyId", supplierPartyId, "roleTypeId", "SUPPLIER_AGENT")));
            }
        }

        // Order Notes
        List notes = FastList.newInstance();
        String comments = externalOrderHeader.getString("comments");
        if (UtilValidate.isNotEmpty(comments)) {
            String noteId = delegator.getNextSeqId("NoteData");
            notes.add(delegator.makeValue("NoteData", UtilMisc.toMap("noteId", noteId, "noteInfo", comments, "noteDateTime", UtilDateTime.nowTimestamp(), "noteParty", userLogin.getString("partyId"))));
            notes.add(delegator.makeValue("OrderHeaderNote", UtilMisc.toMap("orderId", orderId, "noteId", noteId, "internalNote", "Y")));
        }

        // Set the processedTimestamp on the externalOrderHeader
        externalOrderHeader.set("processedTimestamp", UtilDateTime.nowTimestamp());

        // Add everything to the store list here to avoid problems with having to derive more values for order header, etc.
        List toStore = FastList.newInstance();
        toStore.add(delegator.makeValue("OrderHeader", orderHeaderInput));
        toStore.add(delegator.makeValue("OrderStatus", orderStatusInput));
        if (oisg != null) {
            toStore.add(oisg);
        }
        toStore.addAll(orderItems);
        toStore.addAll(externalOrderItems);
        toStore.addAll(orderAdjustments);
        toStore.addAll(oisgAssocs);
        toStore.addAll(roles);
        toStore.addAll(notes);
        toStore.add(externalOrderHeader);

        return toStore;
    }

    @SuppressWarnings("unchecked")
    private static BigDecimal getOrderGrandTotal(List orderAdjustments, List orderItems) {
        BigDecimal grandTotal = ZERO;

        Iterator oajit = orderAdjustments.iterator();
        while (oajit.hasNext()) {
            GenericValue orderAdjustment = (GenericValue) oajit.next();
            grandTotal = grandTotal.add(orderAdjustment.getBigDecimal("amount").setScale(decimals, rounding));
        }

        Iterator oiit = orderItems.iterator();
        while (oiit.hasNext()) {
            GenericValue orderItem = (GenericValue) oiit.next();
            BigDecimal quantity = orderItem.getBigDecimal("quantity").setScale(decimals, rounding);
            BigDecimal price = orderItem.getBigDecimal("unitPrice").setScale(decimals, rounding);

            // Order item adjustments are included in orderAdjustments above - no need to consider them here
            BigDecimal orderItemSubTotal = quantity.multiply(price);
            grandTotal = grandTotal.add(orderItemSubTotal);
        }
        return grandTotal;
    }

}
