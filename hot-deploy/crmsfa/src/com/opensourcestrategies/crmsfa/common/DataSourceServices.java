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
/* Copyright (c) 2005-2006 Open Source Strategies, Inc. */

/*
 *  Copyright (c) 2001-2005 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.opensourcestrategies.crmsfa.common;

import java.util.Map;
import java.util.Locale;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.security.Security;

import com.opensourcestrategies.crmsfa.security.CrmsfaSecurity;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;

/**
 * DataSource services. The service documentation is in services_datasource.xml.
 *
 * @author     <a href="mailto:leon@opensourcestrategies.com">Leon Torres</a>
 * @version    $Rev: 488 $
 */

public class DataSourceServices {

    public static final String module = DataSourceServices.class.getName();

    public static Map addAccountDataSource(DispatchContext dctx, Map context) {
        return addDataSourceWithPermission(dctx, context, "CRMSFA_ACCOUNT", "_UPDATE");
    }

    public static Map addLeadDataSource(DispatchContext dctx, Map context) {
        return addDataSourceWithPermission(dctx, context, "CRMSFA_LEAD", "_UPDATE");
    }

    /**
     * Parametrized service to add a data source to a party. Pass in the security to check.
     */
    private static Map addDataSourceWithPermission(DispatchContext dctx, Map context, String module, String operation) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Security security = dctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = UtilCommon.getLocale(context);

        String partyId = (String) context.get("partyId");
        String dataSourceId = (String) context.get("dataSourceId");

        // check parametrized security
        if (!CrmsfaSecurity.hasPartyRelationSecurity(security, module, operation, userLogin, partyId)) {
            return UtilMessage.createAndLogServiceError("CrmErrorPermissionDenied", locale, module);
        }
        try {
            // create the PartyDataSource to relate the optional data source to this party
            Map serviceResults = dispatcher.runSync("createPartyDataSource", UtilMisc.toMap("partyId", partyId , "dataSourceId", dataSourceId, "userLogin", userLogin));
            if (ServiceUtil.isError(serviceResults)) {
                return UtilMessage.createAndLogServiceError(serviceResults, "CrmErrorAddDataSource", locale, module);
            }
        } catch (GenericServiceException e) {
            return UtilMessage.createAndLogServiceError(e, "CrmErrorAddDataSource", locale, module);
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map removeAccountDataSource(DispatchContext dctx, Map context) {
        return removeDataSourceWithPermission(dctx, context, "CRMSFA_ACCOUNT", "_UPDATE");
    }

    public static Map removeLeadDataSource(DispatchContext dctx, Map context) {
        return removeDataSourceWithPermission(dctx, context, "CRMSFA_LEAD", "_UPDATE");
    }

    /**
     * Parametrized method to remove a data source from a party. Pass in the security to check.
     * TODO: this isn't implemented until necessary
     */
    private static Map removeDataSourceWithPermission(DispatchContext dctx, Map context, String module, String operation) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Security security = dctx.getSecurity();
        GenericValue userLogin = (GenericValue) context.get("userLogin");
        Locale locale = UtilCommon.getLocale(context);

        String partyId = (String) context.get("partyId");
        String dataSourceId = (String) context.get("dataSourceId");

        // check parametrized security
        if (!CrmsfaSecurity.hasPartyRelationSecurity(security, module, operation, userLogin, partyId)) {
            return UtilMessage.createAndLogServiceError("CrmErrorPermissionDenied", locale, module);
        }

        // don't do anything yet
        return ServiceUtil.returnSuccess();
    }
}
