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
package org.opentaps.common.autocomplete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.entity.util.EntityUtil;
import org.opentaps.common.party.PartyReader;
import org.opentaps.common.util.UtilCommon;
import static org.opentaps.common.autocomplete.UtilAutoComplete.*;

/**
 * Catch all location for static auto complete methods.
 */
public final class AutoComplete {

    private AutoComplete() { }

    private static final String MODULE = AutoComplete.class.getName();

    /**
     * Retrieves the auto complete clients party IDs with a given keyword.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompleteClientPartyIds(HttpServletRequest request, HttpServletResponse response) {
        // get active clients (but can be related to another user)
        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                                   EntityUtil.getFilterByDateExpr(),
                                   ac_clientRoleCondition);
        return autocompletePartyIdsByCondition(condition, "PartyFromSummaryByRelationship", request, response);
    }

    /**
     * Retrieves the auto complete accounts party IDs with a given keyword.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompleteAccountPartyIds(HttpServletRequest request, HttpServletResponse response) {
        // get active accounts (but can be related to another user)
        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                                   EntityUtil.getFilterByDateExpr(),
                                   ac_accountRoleCondition);
        return autocompletePartyIdsByCondition(condition, "PartyFromSummaryByRelationship", request, response);
    }

    /**
     * Retrieves the auto complete suppliers party IDs with a given keyword.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompleteSupplierPartyIds(HttpServletRequest request, HttpServletResponse response) {
        // get suppliers (but can be related to another user)
        EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                    EntityCondition.makeCondition("roleTypeId", "SUPPLIER"));
        return autocompletePartyIdsByCondition(condition, "PartyRoleNameDetail", request, response);
    }

    /**
     * Retrieves the auto complete any party IDs with a given keyword.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompletePartyIds(HttpServletRequest request, HttpServletResponse response) {
        // get any party (but can be related to another user)
        return autocompletePartyIdsByCondition(null, "PartySummaryCRMView", request, response);
    }

    private static String autocompletePartyIdsByCondition(EntityCondition condition, String entityName, HttpServletRequest request, HttpServletResponse response) {

        GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }

        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");

        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        keyword = keyword.trim();

        List<GenericValue> parties = FastList.newInstance();
        if (keyword.length() > 0) try {
            // get result as a list iterator (transaction block is to work around a bug in entity engine)
            TransactionUtil.begin();
            EntityListIterator iterator = delegator.findListIteratorByCondition(entityName, condition, null, AC_PARTY_NAME_FIELDS, AP_PARTY_ORDER_BY, AC_FIND_OPTIONS);

            // perform the search
            parties = searchPartyName(iterator, keyword);

            // clean up
            iterator.close();
            TransactionUtil.commit();
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return "error";
        }

        // write the JSON data to the response stream
        return makeSelectionJSONResponse(response, parties, "partyId", new PartySelectionBuilder());
    }

    /**
     * Search parties which name is matching the search string.
     * @param iterator an <code>Iterator</code> of parties
     * @param searchString a <code>String</code> value
     * @return a <code>List</code> value
     * @exception GenericEntityException if an error occurs
     */
    private static List<GenericValue> searchPartyName(Iterator<GenericValue> iterator, String searchString) throws GenericEntityException {
        ArrayList<GenericValue> parties = new ArrayList<GenericValue>();

        // format the search string for matching
        searchString = searchString.toUpperCase();

        int results = 0;
        GenericValue party = null;
        String compositeName;

        while (((party = iterator.next()) != null) && (results <= AC_DEFAULT_RESULTS_SIZE)) {

            compositeName = PartyReader.getPartyCompositeName(party).toUpperCase();

            // search the composite name which matches partyId, groupName, firstName and lastName
            if (compositeName.indexOf(searchString) > -1) {
                parties.add(party);
                results++;
                continue;
            }
        }

        return parties;
    }

    /**
     * Retrieves the auto complete GL Account IDs with a given keyword.
     * It will match the keyword against either the GL Account name or the account code.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompleteGlAccounts(HttpServletRequest request, HttpServletResponse response) {
        GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");

        HttpSession session = request.getSession();
        String organizationPartyId = (String) session.getAttribute("organizationPartyId");

        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        keyword = keyword.trim();

        List<GenericValue> accounts = FastList.newInstance();
        if (keyword.length() > 0) try {
            keyword = keyword.toUpperCase();

            // make the condition
            EntityCondition orCondition = EntityCondition.makeCondition(EntityOperator.OR,
                    EntityCondition.makeCondition("glAccountId", EntityOperator.LIKE, "%" + keyword + "%"),
                    EntityCondition.makeCondition("accountCode", EntityOperator.LIKE, "%" + keyword + "%"),
                    EntityCondition.makeCondition(EntityFunction.UPPER("accountName"), EntityOperator.LIKE, "%" + keyword + "%")
                );
            EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                    orCondition,
                    EntityCondition.makeCondition("organizationPartyId", EntityOperator.EQUALS, organizationPartyId),
                    EntityUtil.getFilterByDateExpr());


            // get result as a list iterator (transaction block is to work around a bug in entity engine)
            TransactionUtil.begin();
            EntityListIterator iterator = delegator.findListIteratorByCondition("GlAccountOrganizationAndClass", condition, null, AC_ACCOUNT_FIELDS, AC_ACCOUNT_ORDER_BY, AC_FIND_OPTIONS);

            // the condition search is sufficient, so we'll just get the entire list
            accounts = iterator.getCompleteList();

            // clean up
            iterator.close();
            TransactionUtil.commit();
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return "error";
        }

        // write the JSON data to the response stream
        return makeSelectionJSONResponse(response, accounts, "glAccountId", new GlAccountSelectionBuilder());
    }

    /**
     * Retrieves the auto complete Product IDs with a given keyword.
     * It will match the keyword against either the Product ID or any good ID.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String getAutoCompleteProduct(HttpServletRequest request, HttpServletResponse response) {
        GenericValue userLogin = UtilCommon.getUserLogin(request);
        if (userLogin == null) {
            Debug.logError("Failed to retrieve the login user from the session.", MODULE);
            return "error";
        }
        GenericDelegator delegator = (GenericDelegator) request.getAttribute("delegator");

        String keyword = UtilCommon.getUTF8Parameter(request, "keyword");
        if (keyword == null) {
            Debug.log("Ignored the empty keyword string.", MODULE);
            return "success";
        }
        keyword = keyword.trim();

        List<GenericValue> products = new FastList<GenericValue>();
        if (keyword.length() > 0) try {
            keyword = keyword.toUpperCase();

            // make the condition
            EntityCondition keywordCondition = EntityCondition.makeCondition(EntityOperator.OR,
                    EntityCondition.makeCondition("productId", EntityOperator.LIKE, keyword + "%"),
                    EntityCondition.makeCondition("idValue", EntityOperator.LIKE, keyword + "%"),
                    EntityCondition.makeCondition(EntityFunction.UPPER("internalName"), EntityOperator.LIKE, "%" + keyword + "%")
                );

            EntityCondition dateCondition = EntityCondition.makeCondition(EntityOperator.OR,
                    EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.EQUALS, null),
                    EntityCondition.makeCondition("salesDiscontinuationDate", EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.nowTimestamp())
                );

            EntityCondition condition = EntityCondition.makeCondition(EntityOperator.AND,
                    keywordCondition,
                    dateCondition);

            // get result as a list iterator (transaction block is to work around a bug in entity engine)
            TransactionUtil.begin();
            EntityListIterator iterator = delegator.findListIteratorByCondition("ProductAndGoodIdentification", condition, null, AC_PRODUCT_FIELDS, AC_PRODUCT_ORDER_BY, AC_FIND_OPTIONS);

            // the condition search is sufficient, so we'll just get the entire list
            products = iterator.getCompleteList();

            // clean up
            iterator.close();
            TransactionUtil.commit();
        } catch (GenericEntityException e) {
            Debug.logError(e, MODULE);
            return "error";
        }

        // write the JSON data to the response stream
        return makeSelectionJSONResponse(response, products, "productId", new ProductSelectionBuilder());
    }

    public static class PartySelectionBuilder implements SelectionBuilder {
        public Map<String, Object> buildRow(Object element) {
            GenericValue party = (GenericValue) element;

            // party ID, full name
            String partyId = party == null ? null : party.getString("partyId");
            String compositeName = PartyReader.getPartyCompositeName(party);

            return UtilMisc.<String, Object>toMap("name", compositeName, "partyId", partyId);
        }
    }

    public static class GlAccountSelectionBuilder implements SelectionBuilder {
        public Map<String, Object> buildRow(Object element) {
            GenericValue account = (GenericValue) element;
            return UtilMisc.<String, Object>toMap("name", account.getString("accountCode") + ":" + account.getString("accountName"), "glAccountId", account.getString("glAccountId"));
        }
    }

    public static class ProductSelectionBuilder implements SelectionBuilder {
        public Map<String, Object> buildRow(Object element) {
            GenericValue product = (GenericValue) element;
            return UtilMisc.<String, Object>toMap("name", product.getString("productId") + ":" + product.getString("internalName"), "productId", product.getString("productId"));
        }
    }

}
