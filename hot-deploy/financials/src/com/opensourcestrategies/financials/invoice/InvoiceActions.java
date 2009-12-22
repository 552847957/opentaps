/*
 * Copyright (c) 2009 - 2009 Open Source Strategies, Inc.
 *
 * Opentaps is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opentaps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Opentaps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.opensourcestrategies.financials.invoice;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;

import com.opensourcestrategies.financials.util.UtilFinancial;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityFunction;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.party.party.PartyHelper;
import org.opentaps.common.builder.EntityListBuilder;
import org.opentaps.common.builder.PageBuilder;
import org.opentaps.common.util.UtilAccountingTags;
import org.opentaps.common.util.UtilCommon;
import org.opentaps.common.util.UtilMessage;
import org.opentaps.domain.DomainsDirectory;
import org.opentaps.base.entities.BillingAccountAndRole;
import org.opentaps.base.entities.GlAccountOrganizationAndClass;
import org.opentaps.base.entities.InvoiceAdjustmentType;
import org.opentaps.base.entities.InvoiceContactMech;
import org.opentaps.base.entities.InvoiceType;
import org.opentaps.base.entities.OrderItem;
import org.opentaps.base.entities.OrderItemBilling;
import org.opentaps.base.entities.PartyContactMechPurpose;
import org.opentaps.base.entities.PaymentApplication;
import org.opentaps.base.entities.PostalAddress;
import org.opentaps.base.entities.StatusItem;
import org.opentaps.base.entities.TaxAuthorityAndDetail;
import org.opentaps.base.entities.TermType;
import org.opentaps.domain.billing.BillingDomainInterface;
import org.opentaps.domain.billing.invoice.Invoice;
import org.opentaps.domain.billing.invoice.InvoiceRepositoryInterface;
import org.opentaps.domain.billing.payment.Payment;
import org.opentaps.domain.organization.Organization;
import org.opentaps.domain.organization.OrganizationRepositoryInterface;
import org.opentaps.foundation.action.ActionContext;
import org.opentaps.foundation.entity.Entity;
import org.opentaps.foundation.exception.FoundationException;
import org.opentaps.foundation.repository.ofbiz.Repository;


/**
 * InvoiceActions - Java Actions for invoices.
 */
public final class InvoiceActions {

    private static final String MODULE = InvoiceActions.class.getName();

    private InvoiceActions() { }

    /**
     * Action for the view invoice screen.
     * @param context the screen context
     * @throws GeneralException if an error occurs
     */
    public static void viewInvoice(Map<String, Object> context) throws GeneralException {

        ActionContext ac = new ActionContext(context);

        HttpServletRequest request = ac.getRequest();
        GenericDelegator delegator = ac.getDelegator();
        Locale locale = ac.getLocale();

        String organizationPartyId = UtilCommon.getOrganizationPartyId(request);
        if (organizationPartyId == null) {
            return;
        }
        ac.put("organizationPartyId", organizationPartyId);

        // get the view preference from the parameter
        String useGwtParam = ac.getParameter("useGwt");
        // get it from the database
        String useGwtPref = UtilCommon.getUserLoginViewPreference(request, "financials", "viewInvoice", "useGwt");
        boolean useGwt;
        if (useGwtParam != null) {
            useGwt = "Y".equals(useGwtParam);
            // persist the change if any
            if (useGwt) {
                useGwtParam = "Y";
            } else {
                useGwtParam = "N";
            }
            if (!useGwtParam.equals(useGwtPref)) {
                UtilCommon.setUserLoginViewPreference(request, "financials", "viewInvoice", "useGwt", useGwtParam);
            }
        } else if (useGwtPref != null) {
            useGwt = "Y".equals(useGwtPref);
        } else {
            // else default to true
            useGwt = true;
        }
        ac.put("useGwt", useGwt);

        // get the invoice from the domain
        String invoiceId = (String) ac.get("invoiceId");
        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        BillingDomainInterface billingDomain = dd.getBillingDomain();
        InvoiceRepositoryInterface invoiceRepository = billingDomain.getInvoiceRepository();
        OrganizationRepositoryInterface organizationRepository = dd.getOrganizationDomain().getOrganizationRepository();

        Invoice invoice = null;
        try {
            invoice = invoiceRepository.getInvoiceById(invoiceId);
        } catch (FoundationException e) {
            Debug.logError("No invoice found with ID [" + invoiceId + "]", MODULE);
            // let the invoice == null check deal with this
        }
        if (invoice == null) {
            ac.put("decoratorLocation", "component://opentaps-common/widget/screens/common/CommonScreens.xml");
            return;
        }
        ac.put("invoice", invoice);

        // put to history
        InvoiceType invoiceType = invoice.getInvoiceType();
        ac.put("history", UtilCommon.makeHistoryEntry(UtilMessage.expandLabel("FinancialsNavHistoryInvoice", locale, UtilMisc.toMap("invoiceId", invoiceId, "invoiceTypeName", invoiceType.get("description", locale))), "viewInvoice", UtilMisc.toList("invoiceId")));

        // get the invoice items
        ac.put("invoiceItems", invoice.getInvoiceItems());

        // get the application payments, we need to fetch the payment entity too
        List<? extends PaymentApplication> paymentApplications = invoice.getPaymentApplications();
        List<Map<String, Object>> payments = new FastList<Map<String, Object>>();
        List<Map<String, Object>> creditPayments = new FastList<Map<String, Object>>();
        for (PaymentApplication pa : paymentApplications) {
            Payment payment = billingDomain.getPaymentRepository().getPaymentById(pa.getPaymentId());
            Map<String, Object> p = payment.toMap();
            p.put("paymentApplicationId", pa.getPaymentApplicationId());
            p.put("amountApplied", pa.getAmountApplied());
            StatusItem status = payment.getStatusItem();
            p.put("statusDescription", status.get(StatusItem.Fields.description.name(), locale));
            if (invoice.isReturnInvoice() && payment.isCustomerRefund() && payment.isBillingAccountPayment()) {
                // the billing account comes from the payment's other payment application
                List<? extends PaymentApplication> applications = payment.getRelated(PaymentApplication.class);
                for (PaymentApplication app : applications) {
                    if (app.getBillingAccountId() != null) {
                        p.put("billingAccountId", app.getBillingAccountId());
                        creditPayments.add(p);
                        break;
                    }
                }
            } else {
                payments.add(p);
            }
        }
        ac.put("payments", payments);
        ac.put("creditPayments", creditPayments);

        // these booleans group the invoices into tabs
        ac.put("isReceipt", invoice.isReceivable());
        ac.put("isDisbursement", invoice.isPayable());
        ac.put("isPartner", invoice.isPartnerInvoice());

        // note Partner invoice are considered receivable invoice, so test for that first
        if (invoice.isPartnerInvoice()) {
            ac.put("decoratorLocation", "component://financials/widget/financials/screens/partners/PartnerScreens.xml");
        } else if (invoice.isPayable()) {
            ac.put("decoratorLocation", "component://financials/widget/financials/screens/payables/PayablesScreens.xml");
        } else if (invoice.isReceivable()) {
            ac.put("decoratorLocation", "component://financials/widget/financials/screens/receivables/ReceivablesScreens.xml");
        }

        // get the accounting tags for the invoice
        if (invoice.isCommissionInvoice()) {
            ac.put("tagTypes", UtilAccountingTags.getAccountingTagsForOrganization(organizationPartyId, UtilAccountingTags.COMMISSION_INVOICES_TAG, delegator));
        } else if (invoice.isSalesInvoice()) {
            ac.put("tagTypes", UtilAccountingTags.getAccountingTagsForOrganization(organizationPartyId, UtilAccountingTags.SALES_INVOICES_TAG, delegator));
        } else if (invoice.isPurchaseInvoice()) {
            ac.put("tagTypes", UtilAccountingTags.getAccountingTagsForOrganization(organizationPartyId, UtilAccountingTags.PURCHASE_INVOICES_TAG, delegator));
        }

        ac.put("billingPartyId", invoice.getTransactionPartyId());

        // the billing address, which can be either the payment or billing location
        // TODO: this should be moved into invoice repository / Invoice
        String invoiceContactMechId = null;
        PostalAddress invoiceAddress = null;
        EntityCondition conditions = EntityCondition.makeCondition(EntityOperator.AND,
                                          EntityCondition.makeCondition(InvoiceContactMech.Fields.contactMechPurposeTypeId.name(), EntityOperator.EQUALS, "BILLING_LOCATION"),
                                          EntityCondition.makeCondition(InvoiceContactMech.Fields.invoiceId.name(), EntityOperator.EQUALS, invoice.getInvoiceId()));
        InvoiceContactMech invoiceContactMech = Entity.getFirst(invoiceRepository.findList(InvoiceContactMech.class, conditions, UtilMisc.toList("lastUpdatedStamp DESC")));
        if (invoiceContactMech != null) {
            invoiceContactMechId = invoiceContactMech.getContactMechId();
            invoiceAddress = invoiceRepository.findOne(PostalAddress.class, invoiceRepository.map(PostalAddress.Fields.contactMechId, invoiceContactMech.getContactMechId()));
        } else {
            // if the address is not in InvoiceContactMech, use the billing address of the party
            GenericValue invoiceAddressGV = UtilFinancial.getBillingAddress(invoice.getTransactionPartyId(), delegator);
            if (invoiceAddressGV != null) {
                invoiceAddress = Repository.loadFromGeneric(PostalAddress.class, invoiceAddressGV);
                invoiceContactMechId = invoiceAddress.getContactMechId();
            }
        }
        ac.put("invoiceAddress", invoiceAddress);
        ac.put("invoiceContactMechId", invoiceContactMechId);

        // update permissions
        boolean hasDescriptiveUpdatePermission = false;
        boolean allowDescriptiveEditOnly = false;

        boolean hasUpdatePermission = false;
        boolean hasAdjustmentPermission = false;
        if ((invoice.isReceivable() && ac.hasEntityPermission("FINANCIALS", "_AR_INUPDT")) || (invoice.isPayable() && ac.hasEntityPermission("FINANCIALS", "_AP_INUPDT"))) {
            hasUpdatePermission = invoice.isInProcess();
            hasAdjustmentPermission = invoice.isAdjustable();
            // allow update descriptive fields
            allowDescriptiveEditOnly = (invoice.isReady() && "edit".equals(ac.getParameter("op")));
            hasDescriptiveUpdatePermission = invoice.isReady();
        }
        ac.put("hasUpdatePermission", hasUpdatePermission);
        ac.put("hasAdjustmentPermission", hasAdjustmentPermission);
        ac.put("allowDescriptiveEditOnly", allowDescriptiveEditOnly);
        ac.put("hasDescriptiveUpdatePermission", hasDescriptiveUpdatePermission);

        // create permission
        boolean hasCreatePermission = ac.hasEntityPermission("FINANCIALS", "_AP_INCRTE") || ac.hasEntityPermission("FINANCIALS", "_AR_INCRTE");
        ac.put("hasCreatePermission", hasCreatePermission);

        // writeoff permission
        boolean hasWriteoffPermission = false;
        if ((invoice.isReceivable() && (ac.hasEntityPermission("FINANCIALS", "_AR_INWRTOF"))
             || !invoice.isReceivable() && (ac.hasEntityPermission("FINANCIALS", "_AP_INWRTOF")))
            && (invoice.isReady() || invoice.isPaid())) {
            hasWriteoffPermission = true;
        }
        ac.put("hasWriteoffPermission", hasWriteoffPermission);

        // update permission implies that the header and items are editable, so get some data for the forms
        if (hasUpdatePermission) {
            List<GlAccountOrganizationAndClass> glAccounts = invoiceRepository.findListCache(GlAccountOrganizationAndClass.class, invoiceRepository.map(GlAccountOrganizationAndClass.Fields.organizationPartyId, organizationPartyId), UtilMisc.toList(GlAccountOrganizationAndClass.Fields.accountCode.name()));
            ac.put("glAccounts", glAccounts);
            ac.put("invoiceItemTypes", invoice.getApplicableInvoiceItemTypes());

            // party's billing and payment locations
            conditions = EntityCondition.makeCondition(EntityOperator.AND,
                                   EntityCondition.makeCondition(EntityOperator.OR,
                                       EntityCondition.makeCondition(PartyContactMechPurpose.Fields.contactMechPurposeTypeId.name(), EntityOperator.EQUALS, "BILLING_LOCATION"),
                                       EntityCondition.makeCondition(PartyContactMechPurpose.Fields.contactMechPurposeTypeId.name(), EntityOperator.EQUALS, "PAYMENT_LOCATION")),
                                   EntityCondition.makeCondition(PartyContactMechPurpose.Fields.partyId.name(), EntityOperator.EQUALS, invoice.getTransactionPartyId()),
                                   EntityUtil.getFilterByDateExpr());
            List<PartyContactMechPurpose> purposes = invoiceRepository.findList(PartyContactMechPurpose.class, conditions);
            List<PostalAddress> addresses = Entity.getRelated(PostalAddress.class, purposes);
            ac.put("addresses", addresses);

            // available tax authorities
            List<TaxAuthorityAndDetail> taxAuthorities = invoiceRepository.findAllCache(TaxAuthorityAndDetail.class, UtilMisc.toList(TaxAuthorityAndDetail.Fields.abbreviation.name(), TaxAuthorityAndDetail.Fields.groupName.name()));
            ac.put("taxAuthorities", taxAuthorities);
        }

        // Invoice terms and term types
        ac.put("invoiceTerms", invoice.getInvoiceTerms());
        List<TermType> termTypes = organizationRepository.getValidTermTypes(invoice.getInvoiceTypeId());
        ac.put("termTypes", termTypes);

        // Prepare string that contains list of related order ids
        List<? extends OrderItemBilling> orderItemBillings = invoice.getOrderItemBillings();
        Set<String> orderIds = new FastSet<String>();
        for (OrderItemBilling billing : orderItemBillings) {
            orderIds.add(billing.getOrderId());
        }
        String ordersList = null;
        for (String id : orderIds) {
            List<OrderItem> orderItems = invoiceRepository.findList(OrderItem.class, invoiceRepository.map(OrderItem.Fields.orderId, id));
            if (orderItems == null) {
                continue;
            }
            if (ordersList == null) {
                ordersList = id;
            } else {
                ordersList += (", " + id);
            }
            if (orderItems != null && orderItems.size() > 0) {
                // collect unique PO id
                Set<String> orderCorrespondingPOs = FastSet.<String>newInstance();
                for (OrderItem orderItem : orderItems) {
                    String correspondingPoId = orderItem.getCorrespondingPoId();
                    if (UtilValidate.isNotEmpty(correspondingPoId)) {
                        orderCorrespondingPOs.add(correspondingPoId);
                    }
                }
                if (UtilValidate.isNotEmpty(orderCorrespondingPOs)) {
                    ordersList += "(";
                    boolean first = true;
                    for (String poId : orderCorrespondingPOs) {
                        if (first) {
                            ordersList += ac.getUiLabel("OpentapsPONumber") + ":";
                        } else {
                            ordersList += ", ";
                        }
                        ordersList += poId;
                        first = false;
                    }
                    ordersList += ")";
                }
            }
        }
        if (ordersList != null) {
            ac.put("ordersList", ordersList);
        }

        // billing accounts of the from party for Accounts Payable invoices
        if (invoice.isPayable()) {
            conditions = EntityCondition.makeCondition(EntityOperator.AND,
                          EntityCondition.makeCondition(BillingAccountAndRole.Fields.partyId.name(), EntityOperator.EQUALS, invoice.getPartyIdFrom()),
                          EntityCondition.makeCondition(BillingAccountAndRole.Fields.roleTypeId.name(), EntityOperator.EQUALS, "BILL_TO_CUSTOMER"),
                          EntityUtil.getFilterByDateExpr());
            List<BillingAccountAndRole> billingAccounts = invoiceRepository.findList(BillingAccountAndRole.class, conditions, UtilMisc.toList(BillingAccountAndRole.Fields.billingAccountId.name()));
            ac.put("billingAccounts", billingAccounts);
        }

        // invoice adjustment types
        if (invoice.isAdjustable()) {
            Organization organization = organizationRepository.getOrganizationById(organizationPartyId);
            List<InvoiceAdjustmentType> types = invoiceRepository.getInvoiceAdjustmentTypes(organization, invoice);
            ac.put("invoiceAdjustmentTypes", types);
        }
    }

    /**
     * Action for the find / list invoices screen.
     * @param context the screen context
     * @throws GeneralException if an error occurs
     * @throws ParseException if an error occurs
     */
    public static void findInvoices(Map<String, Object> context) throws GeneralException, ParseException {

        final ActionContext ac = new ActionContext(context);

        final Locale locale = ac.getLocale();
        final TimeZone timeZone = ac.getTimeZone();

        // Finds invoices based on invoiceTypeId and various input parameters
        String invoiceTypeId = ac.getString("invoiceTypeId");

        // set the find form title here because of limitations with uiLabelMap, along with other variables
        String findFormTitle = "";
        boolean isReceivable = false;
        boolean isPayable = false;
        boolean isPartner = false;
        boolean enableFindByOrder = false;
        if ("SALES_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindSalesInvoices");
            isReceivable = true;
            enableFindByOrder = true;
        } else if ("PURCHASE_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindPurchaseInvoices");
            isPayable = true;
            enableFindByOrder = true;
        } else if ("CUST_RTN_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindCustomerReturnInvoices");
            isPayable = true;
        } else if ("COMMISSION_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindCommissionInvoices");
            isPayable = true;
        } else if ("INTEREST_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindFinanceCharges");
            isReceivable = true;
        } else if ("PARTNER_INVOICE".equals(invoiceTypeId)) {
            findFormTitle = ac.getUiLabel("FinancialsFindPartnerInvoices");
            isPartner = true;
        }
        ac.put("findFormTitle", findFormTitle);
        ac.put("isReceivable", isReceivable);
        ac.put("isPayable", isPayable);
        ac.put("isPartner", isPartner);
        ac.put("enableFindByOrder", enableFindByOrder);

        DomainsDirectory dd = DomainsDirectory.getDomainsDirectory(ac);
        BillingDomainInterface billingDomain = dd.getBillingDomain();
        InvoiceRepositoryInterface repository = billingDomain.getInvoiceRepository();

        // get the list of statuses for the parametrized form ftl
        List<StatusItem> statuses = repository.findListCache(StatusItem.class, repository.map(StatusItem.Fields.statusTypeId, "INVOICE_STATUS"), UtilMisc.toList(StatusItem.Fields.sequenceId.name()));
        List<Map<String, Object>> statusList = new FastList<Map<String, Object>>();
        for (StatusItem s : statuses) {
            Map<String, Object> status = s.toMap();
            status.put("statusDescription", s.get(StatusItem.Fields.description.name(), locale));
            statusList.add(status);
        }
        ac.put("statuses", statusList);

        // get the list of processing statuses for the parametrized form ftl
        List<StatusItem> processingStatuses = repository.findListCache(StatusItem.class, repository.map(StatusItem.Fields.statusTypeId, "INVOICE_PROCESS_STTS"), UtilMisc.toList(StatusItem.Fields.sequenceId.name()));
        List<Map<String, Object>> processingStatusList = new FastList<Map<String, Object>>();
        // add None filter for the processing status
        processingStatusList.add(UtilMisc.<String, Object>toMap(StatusItem.Fields.statusId.name(), "_NA_", "statusDescription", ac.getUiLabel("CommonNone")));
        for (StatusItem s : processingStatuses) {
            Map<String, Object> status = s.toMap();
            status.put("statusDescription", s.get(StatusItem.Fields.description.name(), locale));
            processingStatusList.add(status);
        }
        ac.put("processingStatuses", processingStatusList);

        // now check if we want to actually do a find, which is triggered by performFind = Y
        if (!"Y".equals(ac.getParameter("performFind"))) {
            return;
        }

        // get the search parameters
        String partyId = ac.getParameter("partyId");
        String partyIdFrom = ac.getParameter("partyIdFrom");
        String invoiceId = ac.getParameter("invoiceId");
        String statusId = ac.getParameter("statusId");
        String processingStatusId = ac.getParameter("processingStatusId");
        String invoiceDateFrom = ac.getParameter("invoiceDateFrom");
        String invoiceDateThru = ac.getParameter("invoiceDateThru");
        String dueDateFrom = ac.getParameter("dueDateFrom");
        String dueDateThru = ac.getParameter("dueDateThru");
        String paidDateFrom = ac.getParameter("paidDateFrom");
        String paidDateThru = ac.getParameter("paidDateThru");
        String referenceNumber = ac.getParameter("referenceNumber");
        String orderId = ac.getParameter("orderId");

        // build search conditions
        List<EntityCondition> search = new FastList<EntityCondition>();
        if (partyId != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.partyId.name(), EntityOperator.EQUALS, partyId.trim()));
        }
        if (partyIdFrom != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.partyIdFrom.name(), EntityOperator.EQUALS, partyIdFrom.trim()));
        }
        if (invoiceId != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.invoiceId.name(), EntityOperator.EQUALS, invoiceId.trim()));
        }
        if (statusId != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.statusId.name(), EntityOperator.EQUALS, statusId.trim()));
        }
        if (processingStatusId != null) {
            // this is a special case where we want an empty status
            if ("_NA_".equals(processingStatusId)) {
                search.add(EntityCondition.makeCondition(Invoice.Fields.processingStatusId.name(), EntityOperator.EQUALS, null));
            } else {
                search.add(EntityCondition.makeCondition(Invoice.Fields.processingStatusId.name(), EntityOperator.EQUALS, processingStatusId.trim()));
            }
        }
        String dateFormat = UtilDateTime.getDateFormat(locale);
        if (invoiceDateFrom != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.invoiceDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(invoiceDateFrom, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (dueDateFrom != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.dueDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(dueDateFrom, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (paidDateFrom != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.paidDate.name(), EntityOperator.GREATER_THAN_EQUAL_TO, UtilDateTime.getDayStart(UtilDateTime.stringToTimeStamp(paidDateFrom, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (invoiceDateThru != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.invoiceDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(invoiceDateThru, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (dueDateThru != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.dueDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(dueDateThru, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (paidDateThru != null) {
            search.add(EntityCondition.makeCondition(Invoice.Fields.paidDate.name(), EntityOperator.LESS_THAN_EQUAL_TO, UtilDateTime.getDayEnd(UtilDateTime.stringToTimeStamp(paidDateThru, dateFormat, timeZone, locale), timeZone, locale)));
        }
        if (referenceNumber != null) {
            search.add(EntityCondition.makeCondition(EntityFunction.UPPER_FIELD(Invoice.Fields.referenceNumber.name()), EntityOperator.LIKE, EntityFunction.UPPER("%" + referenceNumber + "%")));
        }

        if (enableFindByOrder && orderId != null) {
            List<OrderItemBilling> orderItemBillings = repository.findList(OrderItemBilling.class, repository.map(OrderItemBilling.Fields.orderId, orderId));
            if (UtilValidate.isNotEmpty(orderItemBillings)) {
                Set<String> invoiceIds = Entity.getDistinctFieldValues(String.class, orderItemBillings, OrderItemBilling.Fields.invoiceId);
                search.add(EntityCondition.makeCondition(Invoice.Fields.invoiceId.name(), EntityOperator.IN, invoiceIds));
            }
        }

        // required conditions
        search.add(EntityCondition.makeCondition(Invoice.Fields.invoiceTypeId.name(), EntityOperator.EQUALS, invoiceTypeId.trim()));


        // Pagination
        EntityListBuilder invoiceListBuilder = new EntityListBuilder(repository, Invoice.class, EntityCondition.makeCondition(search, EntityOperator.AND), UtilMisc.toList(Invoice.Fields.invoiceDate.desc()));
        PageBuilder<Invoice> pageBuilder = new PageBuilder<Invoice>() {
            public List<Map<String, Object>> build(List<Invoice> page) throws Exception {
                GenericDelegator delegator = ac.getDelegator();
                List<Map<String, Object>> newPage = FastList.newInstance();
                for (Invoice invoice : page) {
                    Map<String, Object> newRow = FastMap.newInstance();
                    newRow.putAll(invoice.toMap());

                    StatusItem status = invoice.getStatusItem();
                    newRow.put("statusDescription", status.get(StatusItem.Fields.description.name(), locale));
                    StatusItem processingStatus = invoice.getProcessingStatusItem();
                    if (processingStatus != null) {
                        newRow.put("processingStatusDescription", processingStatus.get(StatusItem.Fields.description.name(), locale));
                    }

                    newRow.put("partyNameFrom", PartyHelper.getPartyName(delegator, invoice.getPartyIdFrom(), false));
                    newRow.put("partyName", PartyHelper.getPartyName(delegator, invoice.getPartyId(), false));

                    newRow.put("amount", invoice.getInvoiceTotal());
                    newRow.put("outstanding", invoice.getOpenAmount());

                    newPage.add(newRow);
                }
                return newPage;
            }
        };
        invoiceListBuilder.setPageBuilder(pageBuilder);

        ac.put("invoiceListBuilder", invoiceListBuilder);
    }

}
