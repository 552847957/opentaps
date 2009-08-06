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
package org.opentaps.common.agreement;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.util.EntityUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

/**
 * Agreement Reader.  This object follows the Reader pattern, which provides a convenience API
 * for accessing information about an agreement.
 * @author <a href="mailto:leon@opensourcestrategies.com">Leon Torres</a>
 */
public class AgreementReader {

    public static String module = AgreementReader.class.getName();

    protected GenericValue agreement = null;
    protected String agreementId = null;
    protected List<GenericValue> agreementTerms = null;
    protected GenericDelegator delegator = null;

    protected AgreementReader() {}

    public AgreementReader(GenericValue agreement) throws GenericEntityException {
        if (agreement == null) throw new GenericEntityException("Agreement not found.");

        this.delegator = agreement.getDelegator();
        if ("Agreement".equals(agreement.getEntityName())) {
            this.agreement = agreement;
        } else {
            this.agreement = delegator.findByPrimaryKey("Agreement", UtilMisc.toMap("agreementId", agreement.get("agreementId")));
        }
        this.agreementId = agreement.getString("agreementId");
        this.agreementTerms = EntityUtil.filterByDate( this.agreement.getRelated("AgreementTerm") );
    }

    public AgreementReader(String agreementId, GenericDelegator delegator) throws GenericEntityException {
        this(delegator.findByPrimaryKey("Agreement", UtilMisc.toMap("agreementId", agreementId)));
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getPartyIdFrom() {
        return agreement.getString("partyIdFrom");
    }

    public String getPartyIdTo() {
        return agreement.getString("partyIdTo");
    }

    /** Gets a term from the agreement, if it exists.  Otherwise returns null. */
    public GenericValue getTerm(String termTypeId) {
        if (UtilValidate.isEmpty(termTypeId)) throw new IllegalArgumentException("Called AgreementReader.hasTerm(termTypeId) with null or empty termTypeId.");
        for (GenericValue term : agreementTerms) {
            if (termTypeId.equals(term.get("termTypeId"))) return term;
        }
        return null;
    }

    /** Gets the term by termTypeId, otherwise throws IllegalArgumentException if it doesn't exist. */
    public GenericValue getTermOrFail(String termTypeId) {
        GenericValue term = getTerm(termTypeId);
        if (term == null) throw new IllegalArgumentException("No such agreement term ["+termTypeId+"] exists in agreement ["+agreementId+"].");
        return term;
    }

    public boolean hasTerm(String termTypeId) {
        GenericValue term = getTerm(termTypeId);
        return term != null;
    }

    public BigDecimal getTermValueBigDecimal(String termTypeId) {
        GenericValue term = getTermOrFail(termTypeId);
        return term.getBigDecimal("termValue");
    }

    public Double getTermValueDouble(String termTypeId) {
        GenericValue term = getTermOrFail(termTypeId);
        return term.getDouble("termValue");
    }

    public String getTermCurrency(String termTypeId) {
        GenericValue term = getTermOrFail(termTypeId);
        return term.getString("currencyUomId");
    }

    /**
     * Finds agreement for any status
     */
    public static AgreementReader findAgreement(String partyIdFrom, String partyIdTo, String agreementTypeId, String termTypeId, GenericDelegator delegator) throws GenericEntityException {
        return findAgreement(partyIdFrom, partyIdTo, agreementTypeId, termTypeId, null, delegator);
    }

    /**
     * Finds an agreement with the given type and term and returns a reader for it.  If more than one agreement exists, then
     * this will return the earliest defined agreement and log a warning about there being conflicting terms.
     * TODO: this doesn't enforce agreement roles.
     */
    public static AgreementReader findAgreement(String partyIdFrom, String partyIdTo, String agreementTypeId, String termTypeId, String statusId, GenericDelegator delegator) throws GenericEntityException {
        List conditions = UtilMisc.toList( 
                new EntityExpr("agreementTypeId", EntityOperator.EQUALS, agreementTypeId),
                new EntityExpr("partyIdFrom", EntityOperator.EQUALS, partyIdFrom),
                new EntityExpr("partyIdTo", EntityOperator.EQUALS, partyIdTo),
                new EntityExpr("termTypeId", EntityOperator.EQUALS, termTypeId),
                EntityUtil.getFilterByDateExpr()
        );
        if (statusId != null) {
            conditions.add(new EntityExpr("statusId", EntityOperator.EQUALS, statusId));
        }
        List agreements = delegator.findByAnd("AgreementAndItemAndTerm", conditions, UtilMisc.toList("fromDate ASC"));
        if (agreements.size() == 0) return null;
        if (agreements.size() > 1) {
            Debug.logWarning("Duplicate agreements found:  Agreement type ["+agreementTypeId+"] from ["+partyIdFrom+"] to ["+partyIdTo+"] and term type ["+termTypeId+"]", module);
        }
        return new AgreementReader(EntityUtil.getFirst(agreements));
    }
}
