<#--
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
 *  
-->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#-- for the view PDF link -->
<@form name="prepareBillingAccountCreditMemoAction" url="prepareBillingAccountCreditMemo" billingAccountId=billingAccount.billingAccountId organizationPartyId=parameters.organizationPartyId/>

<div class="subSectionHeader">
  <div class="subSectionTitle">${uiLabelMap.FinancialsCustomerBillingAccount} ${uiLabelMap.OrderNbr}${billingAccount.billingAccountId}</div>
  <div class="subMenuBar"><@submitFormLink form="prepareBillingAccountCreditMemoAction" class="subMenuButton" text=uiLabelMap.AccountingInvoicePDF/><a class="subMenuButton" href="<@ofbizUrl>payInvoiceWithBillingAccountForm?billingAccountId=${billingAccount.billingAccountId}</@ofbizUrl>">${uiLabelMap.FinancialsPayInvoice}</a></div>
</div>
