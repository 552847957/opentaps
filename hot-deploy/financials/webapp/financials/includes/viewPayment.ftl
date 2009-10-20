<#--
 * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
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
-->
<#--
If you have come this far, payment should be a valid Payment Object.
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if payment?has_content>

<#assign paymentStatusChangeAction = "">

<#if hasCreatePermission>
  <#assign paymentStatusChangeAction><a class="subMenuButton" href="<@ofbizUrl>editPayment?paymentTypeId=${addPaymentTypeId}</@ofbizUrl>">${uiLabelMap.CommonCreateNew}</a></#assign>
</#if>
<#if hasUpdatePermission>
  <#if payment.isNotPaid()>
    <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<a class="subMenuButton" href="<@ofbizUrl>editPayment?paymentId=${payment.paymentId}</@ofbizUrl>">${uiLabelMap.CommonEdit}</a></#assign>
  </#if>
  <#if isDisbursement && payment.isNotPaid()>
    <@form name="paymentSentAction" url="setPaymentStatus" paymentId=payment.paymentId statusId="PMNT_SENT" />
    <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<@submitFormLink form="paymentSentAction" text=uiLabelMap.FinancialsPaymentStatusToSent class="subMenuButton" /></#assign>
  </#if>
  <#if !isDisbursement && payment.isNotPaid()>
    <@form name="paymentReceivedAction" url="setPaymentStatus" paymentId=payment.paymentId statusId="PMNT_RECEIVED" />
    <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<@submitFormLink form="paymentReceivedAction" text=uiLabelMap.FinancialsPaymentStatusToReceived class="subMenuButton" /></#assign>
  </#if>
  <#if payment.isNotPaid()>
    <@form name="paymentCancelledAction" url="setPaymentStatus" paymentId=payment.paymentId statusId="PMNT_CANCELLED" />
    <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<@submitFormLinkConfirm form="paymentCancelledAction" text=uiLabelMap.FinancialsPaymentStatusToCanceled class="subMenuButtonDangerous" /></#assign>
  </#if>
  <#if (payment.isReceived() || payment.isSent())>
    <@form name="paymentConfirmedAction" url="setPaymentStatus" paymentId=payment.paymentId statusId="PMNT_CONFIRMED" />
    <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<@submitFormLink form="paymentConfirmedAction" text=uiLabelMap.FinancialsPaymentStatusToConfirmed class="subMenuButton" /></#assign>
  </#if>
</#if>
<#if isDisbursement && ! payment.isCancelled() && ! payment.isVoided()>
  <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<a href="<@ofbizUrl>/check.pdf?paymentId=${payment.paymentId}</@ofbizUrl>" class="subMenuButton">${uiLabelMap.AccountingPrintAsCheck}</a></#assign>
</#if>
<#if hasUpdatePermission && (payment.isReceived() || payment.isSent()) && !payment.lockboxBatchItemDetails?has_content>
  <@form name="paymentVoidAction" url="voidPayment" paymentId=payment.paymentId />
  <#assign paymentStatusChangeAction>${paymentStatusChangeAction}<@submitFormLinkConfirm form="paymentVoidAction" text=uiLabelMap.FinancialsPaymentVoidPayment class="subMenuButtonDangerous" /></#assign>
</#if>

<#-- entry form -->

<div class="screenlet">
  <div class="subSectionHeader">
    <div class="subSectionTitle">${uiLabelMap.AccountingPayment} <#if payment?has_content>#${payment.paymentId}</#if></div>
    <div class="subMenuBar">${paymentStatusChangeAction?if_exists}</div>
  </div>

  <div class="screenlet-body">
    <table class="twoColumnForm" style="border:0">
      <@displayRow title=uiLabelMap.AccountingPaymentType text=paymentType.getDescription() />
      <#if isDisbursement>
        <@displayLinkRow title=uiLabelMap.FinancialsPayToParty text="${partyNameTo} (${payment.partyIdTo})" href="${statementLink?default('vendorStatement')}?partyId=${payment.partyIdTo}"/>
      <#else/>
        <@displayLinkRow title=uiLabelMap.FinancialsReceiveFromParty text="${partyNameFrom} (${payment.partyIdFrom})" href="${statementLink?default('customerStatement')}?partyId=${payment.partyIdFrom}"/>
      </#if>
   
      <#if payment?has_content>
        <@inputHidden name="statusId" value="${payment.statusId?if_exists}"/>
        <#assign statusValue = payment.getStatusItem()/>
        <@displayRow title=uiLabelMap.FinancialsStatusId text=statusValue.get("description", locale) />
      </#if>

      <#-- display credit card or eft account or payment method information if payment has a payment method, otherwise just payment method type information -->
      <#if paymentMethod?has_content>
        <#assign creditCard = paymentMethod.getCreditCard()?if_exists/>
        <#assign eftAccount = paymentMethod.getEftAccount()?if_exists/>
        <#assign paymentMethodDesc>
          ${paymentMethod.description?if_exists} 
          <#if creditCard?has_content> 
            ${creditCard.cardType} ${creditCard.cardNumber[creditCard.cardNumber?length-4..creditCard.cardNumber?length-1]} ${creditCard.expireDate}
          <#elseif eftAccount?has_content>
            ${eftAccount.bankName?default("")} ${eftAccount.accountNumber[eftAccount.accountNumber?length-4..eftAccount.accountNumber?length-1]}
          </#if> 
          (${paymentMethod.paymentMethodId})
        </#assign>
        <@displayRow title=uiLabelMap.FinancialsPaymentMethod text=paymentMethodDesc />
      <#else/>
        <@displayRow title=uiLabelMap.FinancialsPaymentMethodType text=(paymentMethodType.get("description", locale))?if_exists />
      </#if>
   
      <@displayCurrencyRow title=uiLabelMap.AccountingAmount amount=payment.amount?default("0.0") currencyUomId=payment.currencyUomId />
      <@displayCurrencyRow title=uiLabelMap.AccountingAmountNotApplied amount=amountToApply?default("0.0") currencyUomId=payment.currencyUomId />

      <@displayDateRow title=uiLabelMap.AccountingEffectiveDate date=payment.effectiveDate />
   
      <@displayRow title=uiLabelMap.CommonComments text=payment.comments?if_exists />
      <@displayRow title=uiLabelMap.FinancialsPaymentRefNum text=payment.paymentRefNum?if_exists />

      <#if tagTypes?has_content>
        <@accountingTagsDisplayRows tags=tagTypes entity=payment />
      </#if>
    </table>
  </div>
</div>


<#-- list of payment applications -->
<#if paymentApplications?has_content && (paymentApplicationsList?has_content || paymentApplicationsListGlAccounts?has_content)>
  <div class="screenlet">
    <@sectionHeader title=uiLabelMap.FinancialsPaymentApplications headerClass="screenlet-header" titleClass="boxhead" />
    <div class="screenlet-body">
      <#if paymentApplicationsList?has_content>
        <#if hasApplyPermission>
          <#if isTaxPayment>
            ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#EditPaymentApplicationsTax")}
          <#else>
            ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#EditPaymentApplications")}
          </#if>
        <#else>
          <#if isTaxPayment>
            ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#ViewPaymentApplicationsTax")}
          <#else>
            ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#ViewPaymentApplications")}
          </#if>
        </#if>
      </#if>
      <#if paymentApplicationsList?has_content && paymentApplicationsListGlAccounts?has_content><br/></#if>
      <#if paymentApplicationsListGlAccounts?has_content>
        <#if hasApplyPermission>
          ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#EditPaymentApplicationsGl")}
        <#else>
          ${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#ViewPaymentApplicationsGl")}
        </#if>
      </#if>
    </div>
  </div>
</#if>

<#if adjustments?exists && adjustments.size() != 0>
  <div class="screenlet">
      <@sectionHeader title="Adjustments" headerClass="screenlet-header" titleClass="boxhead" />
      <div class="screenlet-body">
     <table class="listTable" cellspacing="0">
        <tbody>
          <tr class="boxtop">
            <td class="boxhead">${uiLabelMap.OpentapsAdjustmentId}</td>
            <td class="boxhead">${uiLabelMap.CommonType}</td>
            <td class="boxhead">${uiLabelMap.FinancialsInvoiceId}</td>
            <td class="boxhead">${uiLabelMap.CommonDate}</td>
            <td class="boxhead">${uiLabelMap.CommonComment}</td>
            <td class="boxhead" align="right">${uiLabelMap.CommonAmount}</td>
          </tr>

          <#list adjustments as adjustment>
            <tr>
              <@displayCell text=adjustment.getInvoiceAdjustmentId() />
              <@displayCell text=adjustment.getInvoiceAdjustmentType().get("description", locale) />
              <#if adjustment.paymentId?has_content>
                <@displayLinkCell text=adjustment.invoiceId href="viewInvoice?invoiceId=${adjustment.invoiceId}" />
              <#else>
                <td></td>
              </#if>
              <@displayDateCell date=adjustment.effectiveDate />
              <@displayCell text=adjustment.comment />
              <@displayCurrencyCell amount=adjustment.amount currencyUomId=payment.currencyUomId />
            </tr>
          </#list>
        </tbody>
      </table>
      </div>
  </div>
</#if>


<#if hasAmountToApply && hasApplyPermission>
  <div class="screenlet">
    <#if isTaxPayment>
      <@sectionHeader title=uiLabelMap.FinancialsApplyPaymentToTaxAuth headerClass="screenlet-header" titleClass="boxhead" />
      <div class="screenlet-body">${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#AddPaymentApplicationTax")}</div>
    <#else>
      <#if hasChildAccountsToShow >
        <@sectionHeader title=uiLabelMap.FinancialsApplyPaymentToInvoice headerClass="screenlet-header" titleClass="boxhead-left" >
          <div class="subMenuBar">
            <#if showChildAccountsInvoices >
              <@displayLink href="viewPayment?paymentId=${payment.paymentId}&amp;showChildAccountsInvoices=N" text="${uiLabelMap.FinancialsHideChildAccountsInvoices}" class="subMenuButton"/>
            <#else />
              <@displayLink href="viewPayment?paymentId=${payment.paymentId}&amp;showChildAccountsInvoices=Y" text="${uiLabelMap.FinancialsShowChildAccountsInvoices}" class="subMenuButton"/>
            </#if>
          </div>
        </@sectionHeader>
      <#else/>
        <@sectionHeader title=uiLabelMap.FinancialsApplyPaymentToInvoice headerClass="screenlet-header" titleClass="boxhead" />
      </#if>
      <div class="screenlet-body">${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#InvoicePaymentApplicationList")}</div>
    </#if>  
  </div>
</#if>  
<#if hasAmountToApply && hasApplyPermission>
  <div class="screenlet">  
    <@sectionHeader title=uiLabelMap.FinancialsApplyPaymentToGlAccount headerClass="screenlet-header" titleClass="boxhead" />
    <div class="screenlet-body">${screens.render("component://financials/widget/financials/screens/common/PaymentScreens.xml#AddPaymentApplicationGl")}</div>
  </div>
</#if>

</#if>
