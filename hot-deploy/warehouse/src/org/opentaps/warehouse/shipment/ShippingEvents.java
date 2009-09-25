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
package org.opentaps.warehouse.shipment;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.security.Security;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.domain.DomainsLoader;
import org.opentaps.domain.base.entities.Facility;
import org.opentaps.domain.billing.BillingDomainInterface;
import org.opentaps.domain.billing.invoice.Invoice;
import org.opentaps.domain.inventory.InventoryDomainInterface;
import org.opentaps.foundation.entity.EntityNotFoundException;
import org.opentaps.foundation.infrastructure.InfrastructureException;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.warehouse.security.WarehouseSecurity;

public final class ShippingEvents {

    private static final String MODULE = ShippingEvents.class.getName();

    /**
     * Before run invoice report from warehouse we should ensure invoice is sales invoice and put
     * facility owner party as organization party.
     * @param request a <code>HttpServletRequest</code> value
     * @param response a <code>HttpServletResponse</code> value
     * @return a <code>String</code> value
     */
    public static String checkInvoiceReportPreconditions(HttpServletRequest request, HttpServletResponse response) {
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        Locale locale = UtilHttp.getLocale(request);
        Security security = (Security) request.getAttribute("security");

        // ensure warehouse invoice view
        String facilityId = UtilCommon.getParameter(request, "facilityId");
        WarehouseSecurity wsecurity = new WarehouseSecurity(security, userLogin, facilityId);
        if (!wsecurity.hasFacilityPermission("WRHS_INVOICE_VIEW")) {
            return "error";
        }

        String invoiceId = UtilCommon.getParameter(request, "invoiceId");

        try {
            DomainsLoader dl = new DomainsLoader(request);
            DomainsDirectory directory = dl.loadDomainsDirectory();
            BillingDomainInterface billingDomain = directory.getBillingDomain();
            InventoryDomainInterface inventoryDomain = directory.getInventoryDomain();

            Invoice invoice = billingDomain.getInvoiceRepository().getInvoiceById(invoiceId);
            if (!invoice.isSalesInvoice()) {
                return "error";
            }

            Facility facility = inventoryDomain.getInventoryRepository().getFacilityById(facilityId);
            request.getSession().setAttribute("organizationPartyId", facility.getOwnerPartyId());

        } catch (EntityNotFoundException e) {
            UtilMessage.createAndLogEventError(request, e, locale, MODULE);
        } catch (RepositoryException e) {
            UtilMessage.createAndLogEventError(request, e, locale, MODULE);
        } catch (InfrastructureException e) {
            UtilMessage.createAndLogEventError(request, e, locale, MODULE);
        }

        // all tests pass, so allow view
        return "success";
    }

}
