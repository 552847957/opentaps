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

/* This file has been modified by Open Source Strategies, Inc. */

package org.opentaps.common.product;

import org.ofbiz.base.util.*;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.product.ProductContentWrapper;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.opentaps.common.util.UtilCommon;

import java.math.BigDecimal;
import java.util.*;

/**
 * Utility methods for working with products.
 *
 * @author     <a href="mailto:cliberty@opensourcestrategies.com">Chris Liberty</a>
 * @version    $Rev$
 */
public final class UtilProduct {

    private static final String MODULE = ProductServices.class.getName();

    private UtilProduct() { }

    /**
     * Gets a <code>ProductContentWrapper</code> for the given product.
     *
     * @param delegator a <code>GenericDelegator</code> value
     * @param dispatcher a <code>LocalDispatcher</code> value
     * @param productId the product to get the <code>ProductContentWrapper</code> for
     * @param locale a <code>Locale</code> value
     * @return a <code>ProductContentWrapper</code> value
     * @exception GenericEntityException if an error occurs
     */
    public static ProductContentWrapper getProductContentWrapper(GenericDelegator delegator, LocalDispatcher dispatcher, String productId, Locale locale) throws GenericEntityException {
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        return new ProductContentWrapper(dispatcher, product,  locale, null);
    }

    public static String getProductContentAsText(GenericDelegator delegator, LocalDispatcher dispatcher, String productId, String productContentTypeId, Locale locale) throws GenericEntityException {
        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        return ProductContentWrapper.getProductContentAsText(product, productContentTypeId, locale, dispatcher);
    }

    /**
     * Gets the list of warnings for the given product.
     * Warnings are defined as <code>ProductFeatureAndAppl</code>.
     *
     * @param delegator a <code>GenericDelegator</code> value
     * @param productId the product to get the warnings for
     * @return the <code>List</code> of warnings ordered by sequenceNum
     * @exception GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<String> getProductWarnings(GenericDelegator delegator, String productId) throws GenericEntityException {
        List<String> warnings = new LinkedList<String>();
        List orderBy = UtilMisc.toList("sequenceNum", "productFeatureApplTypeId", "productFeatureTypeId", "description");
        try {
            // get all product warnings
            List productFeatureAndApplList = delegator.findByAnd("ProductFeatureAndAppl",
                    UtilMisc.toList(new EntityExpr("productId", EntityOperator.EQUALS, productId),
                                    new EntityExpr("productFeatureApplTypeId", EntityOperator.EQUALS, "STANDARD_FEATURE"),
                                    new EntityExpr("productFeatureTypeId", EntityOperator.EQUALS, "WARNING"),
                                    EntityUtil.getFilterByDateExpr()), orderBy);
            warnings = EntityUtil.getFieldListFromEntityList(productFeatureAndApplList, "description", true);
        } catch (GenericEntityException ex) {
            throw new GenericEntityException("Cannot find if there are warnings for productId" + productId, ex);
        }
        return warnings;
    }

    /**
     * Helper method that returns the SKU of given product.
     *
     * @param productId the product to get the SKU for
     * @param delegator a <code>GenericDelegator</code> value
     * @return the Product SKU, or <code>null</code> if no SKU is set
     * @throws GenericEntityException if an error occurs
     */
    public static String getProductSKU(String productId, GenericDelegator delegator) throws GenericEntityException {
        GenericValue sku = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("goodIdentificationTypeId", "SKU", "productId", productId));
        if (sku != null) {
            return sku.getString("idValue");
        }
        return null;
    }

    /**
     * Helper method that returns the UPCA of given product.
     *
     * @param productId the product to get the UPCA for
     * @param delegator a <code>GenericDelegator</code> value
     * @return the Product UPCA, or <code>null</code> if no UPCA is set
     * @throws GenericEntityException if an error occurs
     */
    public static String getProductUPCA(String productId, GenericDelegator delegator) throws GenericEntityException {
        GenericValue upca = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("goodIdentificationTypeId", "UPCA", "productId", productId));
        if (upca != null) {
            return upca.getString("idValue");
        }
        return null;
    }

    /**
     * Helper method that returns the UPCE of given product.
     *
     * @param productId the product to get the UPCE for
     * @param delegator a <code>GenericDelegator</code> value
     * @return the Product UPCE, or <code>null</code> if no UPCE is set
     * @throws GenericEntityException if an error occurs
     */
    public static String getProductUPCE(String productId, GenericDelegator delegator) throws GenericEntityException {
        GenericValue upce = delegator.findByPrimaryKey("GoodIdentification", UtilMisc.toMap("goodIdentificationTypeId", "UPCE", "productId", productId));
        if (upce != null) {
            return upce.getString("idValue");
        }
        return null;
    }

    /**
     * Helper method that returns the UPC of given product, which the last entered of UPCA or UPCE.
     *
     * @param productId the product to get the UPC for
     * @param delegator a <code>GenericDelegator</code> value
     * @return the Product UPC, or <code>null</code> if no UPC is set
     * @throws GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static String getProductUPC(String productId, GenericDelegator delegator) throws GenericEntityException {
        String upc = null;
        List cond = UtilMisc.toList(new EntityExpr("productId", EntityOperator.EQUALS, productId),
                                    new EntityExpr("goodIdentificationTypeId", EntityOperator.IN, UtilMisc.toList("UPCA", "UPCE")),
                                    new EntityExpr("idValue", EntityOperator.NOT_EQUAL, ""));
        GenericValue upcValue = EntityUtil.getFirst(delegator.findByCondition("GoodIdentification", new EntityConditionList(cond, EntityOperator.AND), null, Arrays.asList("lastUpdatedStamp DESC")));
        if (UtilValidate.isNotEmpty(upcValue)) {
            upc = upcValue.getString("idValue");
        }
        return upc;
    }

    /**
     * Uses Product.salesDiscontinuationDate to determine if product has been discontinued.
     * @param product the <code>Product</code> to test
     * @return <code>true</code> if the product has been discontinued
     * @throws GenericEntityException if an error occurs
     */
    public static boolean isDiscontinued(GenericValue product) throws GenericEntityException {
        if (product.get("salesDiscontinuationDate") == null) {
            return false;
        } else if (UtilDateTime.nowTimestamp().before(product.getTimestamp("salesDiscontinuationDate"))) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Uses Product.introductionDate to determine if product has been introduced yet.
     * @param product the <code>Product</code> to test
     * @return <code>true</code> if the product's introductionDate is in the past, or if no introductionDate is set
     * @throws GenericEntityException if an error occurs
     */
    public static boolean isIntroduced(GenericValue product) throws GenericEntityException {
        if (product.get("introductionDate") == null) {
            return true;
        } else if (UtilDateTime.nowTimestamp().after(product.getTimestamp("salesDiscontinuationDate"))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the given product is introduced and not discontinued.
     * @param product the <code>Product</code> to test
     * @return <code>true</code> if the product is introduced and not discontinued
     * @throws GenericEntityException if an error occurs
     */
    public static boolean isActive(GenericValue product) throws GenericEntityException {
        return (isIntroduced(product) && !isDiscontinued(product));
    }

    /**
     * Checks if the product is physical.  The product is considered physical as long as its related ProductType.isPhysical is not set to N
     * (ie, isPhysical = Y or is blank).
     * @param product the <code>Product</code> to test
     * @return <code>true</code> if the product is physical
     * @throws GenericEntityException if an error occurs
     */
    public static boolean isPhysical(GenericValue product) throws GenericEntityException {
        return !("N".equals(product.getRelatedOneCache("ProductType").getString("isPhysical")));
    }

    /**
     * Gets the conservative value of a product, which is the Standard Cost if it is founded, if it is not found, then get the
     * lowest price it could be purchased at from the a supplier. Same as below, but uses the system user login.
     * @param productId the product to get the conservative value for
     * @param currencyUomId the currency to get the conservative value for
     * @param dispatcher a <code>LocalDispatcher</code> value
     * @return the conservative value for the given product and currency uom
     */
    public static BigDecimal getConservativeValue(String productId, String currencyUomId, LocalDispatcher dispatcher) {
        try {
            return getConservativeValue(productId, currencyUomId, dispatcher, UtilCommon.getSystemUserLogin(dispatcher.getDelegator()));
        } catch (GenericEntityException ex) {
            Debug.logError("Problem getting conservative value, will return null for for product [" + productId + "]: " + ex.getMessage(), MODULE);
            return null;
        }
    }

    /**
     * Gets the conservative value of a product, which is the Standard Cost if it is founded, if it is not found, then get the
     * lowest price it could be purchased at from the a supplier.
     * @param productId the product to get the conservative value for
     * @param currencyUomId the currency to get the conservative value for
     * @param dispatcher a <code>LocalDispatcher</code> value
     * @param userLogin a <code>GenericValue</code> value
     * @return the conservative value for the given product and currency uom
     */
    @SuppressWarnings("unchecked")
    public static BigDecimal getConservativeValue(String productId, String currencyUomId, LocalDispatcher dispatcher, GenericValue userLogin) {
        BigDecimal cost = null;

        try {
            // first look up the standard costs (the CostComponents with prefix EST_STD_)  If we found a non-zero standard cost, then it becomes a cost
            Map results = dispatcher.runSync("calculateProductCosts", UtilMisc.toMap("productId", productId, "currencyUomId", currencyUomId, "costComponentTypePrefix", "EST_STD", "userLogin", userLogin));
            if (!ServiceUtil.isError(results) && !ServiceUtil.isFailure(results)) {
                Double totalCost = (Double) results.get("totalCost");
                if ((totalCost != null) && (totalCost.doubleValue() != 0.0)) {
                    cost = new BigDecimal(totalCost.doubleValue());
                    Debug.logInfo("Conservative value for product [" + productId + "] in currency [" + currencyUomId + "] is the standard cost of [" + cost + "]", MODULE);
                }
            }
            if ((cost != null) && (cost.signum() != 0)) {
                return cost;
            }

            // otherwise go look for the lowest SupplierProduct: this service returns eligible SupplierProduct with lowest price one first
            results = dispatcher.runSync("getSuppliersForProduct", UtilMisc.toMap("productId", productId, "currencyUomId", currencyUomId, "userLogin", userLogin));
            if (!ServiceUtil.isError(results) && !ServiceUtil.isFailure(results) && results.get("supplierProducts") != null) {
                GenericValue lowestPricedSupplierProduct = EntityUtil.getFirst((List) results.get("supplierProducts"));
                if (lowestPricedSupplierProduct != null) {
                    cost = new BigDecimal(lowestPricedSupplierProduct.getDouble("lastPrice"));
                    Debug.logInfo("Conservative value for product [" + productId + "] in currency [" + currencyUomId + "] is the supplier cost of [" + cost + "] from [" + lowestPricedSupplierProduct + "]", MODULE);
                }
            }

            Debug.logWarning("No conservative value for product [" + productId + "] in currency [" + currencyUomId + "]", MODULE);
            return cost;
        } catch (GenericServiceException ex) {
            Debug.logError("Problem getting conservative value, will return null for for product [" + productId + "]: " + ex.getMessage(), MODULE);
            return null;
        }

    }


    /**
     * Gets the conservative value of a product using the currency of the given organization.
     * Same as below, but uses the system user login.
     * @param productId the product to get the conservative value for
     * @param organizationPartyId the organization to get the conservative value for
     * @param dispatcher a <code>LocalDispatcher</code> value
     * @return the conservative value for the given product and organization
     */
    public static BigDecimal getConservativeValueForOrg(String productId, String organizationPartyId, LocalDispatcher dispatcher) {
        try {
            return getConservativeValueForOrg(productId, organizationPartyId, dispatcher, UtilCommon.getSystemUserLogin(dispatcher.getDelegator()));
        } catch (GeneralException ex) {
            Debug.logError("Problem getting conservative value, will return null for for product [" + productId + "]: " + ex.getMessage(), MODULE);
            return null;
        }
    }

    /**
     * Gets the conservative value of a product using the currency of the given organization.
     * @param productId the product to get the conservative value for
     * @param organizationPartyId the organization to get the conservative value for
     * @param dispatcher a <code>LocalDispatcher</code> value
     * @param userLogin a <code>GenericValue</code> value
     * @return the conservative value for the given product and organization
     * @exception GeneralException if an error occurs
     */
    public static BigDecimal getConservativeValueForOrg(String productId, String organizationPartyId, LocalDispatcher dispatcher, GenericValue userLogin) throws GeneralException {
        GenericDelegator delegator = dispatcher.getDelegator();
        String currencyUomId = UtilCommon.getOrgBaseCurrency(organizationPartyId, delegator);
        return getConservativeValue(productId, currencyUomId, dispatcher, userLogin);
    }

    /**
     * Expands a UPC-E code to a UPC-A code. Works on correct 8-digit UPC-E codes and 6-digit UPC-Es which
     *  have been stripped of their first and last digits.
     * @param upce a UPC-E code
     * @return the corresponding UPC-A code, or <code>null</code> if the given UPC-E code was not correct
     */
    public static String expandUPCE(String upce) {
        if (UtilValidate.isEmpty(upce) || (upce.length() != 6 && upce.length() != 8)) {
            return null;
        }
        StringBuffer sb = new StringBuffer("0");

        // If the code is 6 digits, prepend a 0
        if (upce.length() == 6) {
            upce = "0" + upce;
        }

        int checkDigit = Character.digit(upce.charAt(6), 10);
        switch (checkDigit) {
            case 0: sb.append(upce.substring(1, 3)).append("00000").append(upce.substring(3, 6)); break;
            case 1: sb.append(upce.substring(1, 3)).append("10000").append(upce.substring(3, 6)); break;
            case 2: sb.append(upce.substring(1, 3)).append("20000").append(upce.substring(3, 6)); break;
            case 3: sb.append(upce.substring(1, 4)).append("00000").append(upce.substring(4, 6)); break;
            case 4: sb.append(upce.substring(1, 5)).append("00000").append(upce.substring(5, 6)); break;
            default: sb.append(upce.substring(1, 6)).append("0000").append(upce.substring(6, 7));
        }
        return sb.toString() + calculateUpcChecksum(sb.toString());
    }

    /**
     * Compresses a UPC-A code to a UPC-E code.
     * @param upca a UPC-A code
     * @return the corresponding UPC-E code, or <code>null</code> if the given UPC-A code was not correct
     */
    public static String compressUPCA(String upca) {
        if (UtilValidate.isEmpty(upca) || upca.length() != 12) {
            return null;
        }
        String upce = null;
        if (upca.substring(3, 6).equals("000") || upca.substring(3, 6).equals("100") || upca.substring(3, 6).equals("200")) {
            upce = "0" + upca.substring(1, 3) + upca.substring(8, 11) + upca.substring(3, 4) + upca.substring(11, 12);
        } else if (upca.substring(4, 6).equals("00")) {
            upce = "0" + upca.substring(1, 4) + upca.substring(9, 11) + "3" + upca.substring(11, 12);
        } else if (upca.substring(5, 6).equals("0")) {
            upce = "0" + upca.substring(1, 5) + upca.substring(10, 11) + "4" + upca.substring(11, 12);
        } else if (Integer.parseInt(upca.substring(10, 11)) >= 5) {
            upce = "0" + upca.substring(1, 6) + upca.substring(10, 11) + upca.substring(11, 12);
        }
        return upce;
    }

    /**
     * Handles both 8-digit UPC-E and 12-digit UPC-A codes.
     * @param upc the UPC code to check
     * @return <code>true</code> if the given UPC code is valid
     */
    public static boolean isValidUPC(String upc) {
        if (UtilValidate.isEmpty(upc) || (upc.length() != 8 && upc.length() != 12)) {
            return false;
        }
        if (upc.length() == 8 && upc.charAt(0) != '0') {
            return false;
        }
        return calculateUpcChecksum(upc) == Character.digit(upc.charAt(upc.length() - 1), 10);
    }

    /**
     * Handles 8-digit UPC-E and 12-digit UPC-A codes, and the 7- and 11-digit versions with checksum missing.
     * @param upc the UPC code to check sum
     * @return the check sum
     */
    public static int calculateUpcChecksum(String upc) {

        // Strip the checksum digit, if it exists
        if (upc.length() % 2 == 0) {
            upc = upc.substring(0, upc.length() - 1);
        }

        int evenDigitSum = 0;
        int oddDigitSum = 0;
        for (int pos = 1; pos <= upc.length(); pos++) {
            if (pos % 2 == 0) {
                evenDigitSum += Character.digit(upc.charAt(pos - 1), 10);
            } else {
                oddDigitSum += Character.digit(upc.charAt(pos - 1), 10);
            }
        }
        int checkSum = 10 - (((oddDigitSum * 3) + evenDigitSum) % 10);
        if (checkSum == 10) {
            checkSum = 0;
        }
        return checkSum;
    }


    /**
     * Returns List of productStoreIds from productStoreGroupId.
     * @param productStoreGroupId to product store group
     * @param delegator a <code>GenericDelegator</code> value
     * @return the <code>List</code> of productStoreIds from the given productStoreGroupId
     * @throws GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List getProductStoreIdsFromGroup(String productStoreGroupId, GenericDelegator delegator) throws GenericEntityException {
        List productStoreIds = null;
        if (UtilValidate.isNotEmpty(productStoreGroupId)) {
            List productStoreGroupConditions = UtilMisc.toList(new EntityExpr("productStoreGroupId", EntityOperator.EQUALS, productStoreGroupId));
            List productStoreGroupFieldsToSelect = UtilMisc.toList("productStoreId");
            List productStoreGroups = delegator.findByCondition("ProductStoreGroupAndProductStore",
                                        new EntityConditionList(productStoreGroupConditions, EntityOperator.OR),
                                        null,
                                        productStoreGroupFieldsToSelect,
                                        null,
                                        UtilCommon.DISTINCT_READ_OPTIONS);
            productStoreIds = EntityUtil.getFieldListFromEntityList(productStoreGroups, "productStoreId", true);
        }
        return productStoreIds;
    }

    /**
    * Returns list of productStores from payToPartyId.
    * @param payToPartyId the pay to party
    * @param delegator a <code>GenericDelegator</code> value
    * @return the <code>List</code> of productStoreIds from the given payToPartyId
    * @throws GenericEntityException if an error occurs
    */
    @SuppressWarnings("unchecked")
    public static List getProductStoresFromPayToPartyId(String payToPartyId, GenericDelegator delegator) throws GenericEntityException {
        List searchConditions = UtilMisc.toList(new EntityExpr("payToPartyId", EntityOperator.EQUALS, payToPartyId));
        List fieldsToSelect = UtilMisc.toList("productStoreId", "storeName");
        List productStores = delegator.findByCondition("ProductStore",
                                new EntityConditionList(searchConditions, EntityOperator.AND),
                                null,
                                fieldsToSelect,
                                UtilMisc.toList("storeName"),
                                UtilCommon.DISTINCT_READ_OPTIONS);
        return productStores;
    }

    /**
     * Returns List of open Requirements for given productId and productStoreId.
     * @param productId the product to get the open requirements for
     * @param productStoreId the product store to get the open requirements for
     * @param delegator a <code>GenericDelegator</code> value
     * @return List of open Requirement
     * @throws GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List getOpenRequirements(String productId, String productStoreId, GenericDelegator delegator) throws GenericEntityException {
        GenericValue productStore = delegator.findByPrimaryKey("ProductStore", UtilMisc.toMap("productStoreId", productStoreId));
        String facilityId = productStore.getString("inventoryFacilityId");
        List conditions = UtilMisc.toList(
            new EntityExpr("facilityId", EntityOperator.EQUALS, facilityId),
            new EntityExpr("productId", EntityOperator.EQUALS, productId),
            new EntityExpr("requirementTypeId", EntityOperator.EQUALS, "PRODUCT_REQUIREMENT"),
            new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "REQ_CLOSED"),
            new EntityExpr("statusId", EntityOperator.NOT_EQUAL, "REQ_REJECTED")
        );

        List openRequirements = delegator.findByAnd("Requirement", conditions);
        return openRequirements;
    }

    /**
     * Returns the Total of open Requirements for given productId and productStoreId.
     * @param productId the product to get the total of open requirements for
     * @param productStoreId the product store to get the total of open requirements for
     * @param delegator a <code>GenericDelegator</code> value
     * @return total quantity of all open requirements
     * @throws GenericEntityException if an error occurs
     */
    @SuppressWarnings("unchecked")
    public static Double countOpenRequirements(String productId, String productStoreId, GenericDelegator delegator) throws GenericEntityException {
        List openRequirements = getOpenRequirements(productId, productStoreId, delegator);
        Double count = new Double(0.0);
        for (Iterator iter = openRequirements.iterator(); iter.hasNext();) {
            GenericValue requirement = (GenericValue) iter.next();
            count += (requirement.get("quantity") == null ? 0.0 : requirement.getDouble("quantity"));
        }
        return count;
    }
}
