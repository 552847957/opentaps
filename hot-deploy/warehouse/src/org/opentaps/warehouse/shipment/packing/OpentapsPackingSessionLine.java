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

package org.opentaps.warehouse.shipment.packing;

import org.ofbiz.shipment.packing.PackingSessionLine;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilNumber;

import java.math.BigDecimal;

/**
 * Extend the OFBiz packing session line so we can add extra information to each line, such as the package value.
 * This will work transparently with the original PackingSession.java as long as the opentaps PackingSession.java
 * overrides the createPackLineItem() method.
 *
 * Note that a line represents an association between a package number and the order item.
 */
public class OpentapsPackingSessionLine extends PackingSessionLine {

    // for BigDecimal arithmetic, we'll just re-use the invoice settings
    public static final BigDecimal ZERO = new BigDecimal("0");
    private static int decimals = UtilNumber.getBigDecimalScale("invoice.decimals");
    private static int rounding = UtilNumber.getBigDecimalRoundingMode("invoice.rounding");

    // cache the values once computed
    protected BigDecimal rawValue = null;

    public OpentapsPackingSessionLine(String orderId, String orderItemSeqId, String shipGroupSeqId, String productId, String inventoryItemId, double quantity, double weight, int packageSeq) {
        super(orderId, orderItemSeqId, shipGroupSeqId, productId, inventoryItemId, quantity, weight, packageSeq);
    }

    /** Clear the value caches when the quantity changes. */
    public void setQuantity(double quantity) {
        super.setQuantity(quantity);
        clearCache();
    }

    /** Clear the value caches when the quantity changes. */
    public void addQuantity(double quantity) {
        super.addQuantity(quantity);
        clearCache();
    }

    // the idea is we don't want to be calculating the value every time some small thing changes
    private void clearCache() {
        rawValue = null;
    }

    /**
     * Gets the raw value of this packing line, which is the value of the order item pro-rated for the quantity
     * that is in this package.  No adjustments are factored in.  The idea is to present the value of each item
     * in the package and the value of a package for purposes of insurance and to help the packer distribute the
     * value evenly across packages.
     */
    public BigDecimal getRawValue(GenericDelegator delegator) throws GenericEntityException {
        if (rawValue != null) return rawValue;

        GenericValue item = delegator.findByPrimaryKey("OrderItem", UtilMisc.toMap("orderId", this.orderId, "orderItemSeqId", this.orderItemSeqId));
        if (item == null) return ZERO;

        // ratio of quantity allocated to quantity ordered
        BigDecimal ratio = (new BigDecimal(this.quantity)).divide(item.getBigDecimal("quantity"), decimals + 3, rounding);

        // prorated value
        rawValue = ratio.multiply(item.getBigDecimal("unitPrice")).multiply(item.getBigDecimal("quantity")).setScale(decimals, rounding);

        return rawValue;
    }

}