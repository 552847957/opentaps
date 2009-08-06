package com.opensourcestrategies.financials.accounts;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.order.order.OrderReadHelper;

/*
 * Copyright (c) 2007 - 2009 Open Source Strategies, Inc.
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

//A portion of this file may have come from the Apache OFBIZ project

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

public class BillingAccountWorker {

    public static final String module = BillingAccountWorker.class.getName();
    private static BigDecimal ZERO = new BigDecimal("0");
    private static int decimals = -1;
    private static int rounding = -1;
    static {
        decimals = UtilNumber.getBigDecimalScale("order.decimals");
        rounding = UtilNumber.getBigDecimalRoundingMode("order.rounding");

        // set zero to the proper scale
        if (decimals != -1) ZERO = ZERO.setScale(decimals);
    }

    /**
     * Calculates the "available" balance of a billing account, which is the
     * net balance minus amount of pending (not canceled, rejected, or received) order payments.
     * When looking at using a billing account for a new order, you should use this method.
     * @param billingAccountId
     * @param delegator
     * @return
     * @throws GenericEntityException
     */
    public static BigDecimal getBillingAccountBalance(GenericDelegator delegator, String billingAccountId) throws GenericEntityException {
        GenericValue billingAccount = delegator.findByPrimaryKey("BillingAccount", UtilMisc.toMap("billingAccountId", billingAccountId));
        return getBillingAccountBalance(billingAccount);
    }

    public static BigDecimal getBillingAccountBalance(GenericValue billingAccount) throws GenericEntityException {

        GenericDelegator delegator = billingAccount.getDelegator();
        String billingAccountId = billingAccount.getString("billingAccountId");

        // first get the net balance of invoices - payments
        BigDecimal balance = org.ofbiz.accounting.payment.BillingAccountWorker.getBillingAccountNetBalance(delegator, billingAccountId);

        // now the amounts of all the pending orders (not canceled, rejected or completed)
        List orderHeaders = org.ofbiz.accounting.payment.BillingAccountWorker.getBillingAccountOpenOrders(delegator, billingAccountId);

        if (orderHeaders != null) {
            Iterator ohi = orderHeaders.iterator();
            while (ohi.hasNext()) {
                GenericValue orderHeader = (GenericValue) ohi.next();
                OrderReadHelper orh = new OrderReadHelper(orderHeader);
                balance = balance.add(orh.getOrderGrandTotal());
            }
        }

        // set the balance to BillingAccount.accountLimit if it is greater.  This is necessary because nowhere do we track the amount of BillingAccount
        // to be charged to an order, such as FinAccountAuth entity does for FinAccount.  As a result, we must assume that the system is doing things correctly
        // and use the accountLimit
        BigDecimal accountLimit = new BigDecimal(billingAccount.getDouble("accountLimit").doubleValue());
        if (balance.compareTo(accountLimit) == 1) {
            balance = accountLimit;
        } else {
            balance = balance.setScale(decimals, rounding);
        }
        return balance;

    }


    /**
     * Returns the amount which could be charged to a billing account, which is defined as the accountLimit minus account balance and minus the balance of outstanding orders
     * When trying to figure out how much of a billing account can be used to pay for an outstanding order, use this method
     * @param billingAccount
     * @return
     * @throws GenericEntityException
     */
    public static BigDecimal getBillingAccountAvailableBalance(GenericValue billingAccount) throws GenericEntityException {
        if ((billingAccount != null) && (billingAccount.get("accountLimit") != null)) {
            BigDecimal accountLimit = new BigDecimal(billingAccount.getDouble("accountLimit").doubleValue());
            BigDecimal availableBalance = accountLimit.subtract(getBillingAccountBalance(billingAccount)).setScale(decimals, rounding);
            return availableBalance;
        } else {
            Debug.logWarning("Available balance requested for null billing account, returning zero", module);
            return ZERO;
        }
    }

    public static BigDecimal getBillingAccountAvailableBalance(GenericDelegator delegator, String billingAccountId) throws GenericEntityException {
        GenericValue billingAccount = delegator.findByPrimaryKey("BillingAccount", UtilMisc.toMap("billingAccountId", billingAccountId));
        return getBillingAccountAvailableBalance(billingAccount);
    }

    /**
     * Returns the original amount of a billing account, which is defined as the sum of non invoiced payment applications for
     * given billing account.
     * 
     * @param billingAccount
     * @return Billing account original balance
     * @throws GenericEntityException
     */
    @SuppressWarnings("unchecked")
    public static BigDecimal getBillingAccountOriginalBalance(GenericValue billingAccount) throws GenericEntityException {
        if (billingAccount != null) {
            return getBillingAccountOriginalBalance(billingAccount.getDelegator(), billingAccount.getString("billingAccountId"));
        } else {
            Debug.logWarning("Original balance requested for null billing account, returning zero", module);
            return ZERO;
        }
    }

    /**
     * @see com.opensourcestrategies.financials.accounts.BillingAccountWorker#getBillingAccountOriginalBalance(GenericValue)
     * @param delegator GenericDelegator
     * @param billingAccountId Billing Account unique identifier.
     * @return Billing account original balance.
     * @throws GenericEntityException
     */
    @SuppressWarnings("unchecked")
    public static BigDecimal getBillingAccountOriginalBalance(GenericDelegator delegator, String billingAccountId) throws GenericEntityException {
        if (delegator == null || UtilValidate.isEmpty(billingAccountId))
            throw new IllegalArgumentException("Neither delegator nor billingAccountId can be NULL.");

        List<EntityCondition> conditionList = FastList.newInstance();
        conditionList.add(new EntityExpr("billingAccountId", EntityOperator.EQUALS, billingAccountId));
        conditionList.add(new EntityExpr("invoiceId", EntityOperator.EQUALS, null));

        Set<String> fieldsToSelect = UtilMisc.toSet("amount");

        List<GenericValue> paymentAppls = delegator.findByCondition("PaymentAndApplication", new EntityConditionList(conditionList, EntityOperator.AND), fieldsToSelect, null);
        double originalBalance = 0.0;
        for (GenericValue appl : paymentAppls) {
            originalBalance += appl.getDouble("amount").doubleValue();
        }

        return BigDecimal.valueOf(originalBalance);
    }

}
