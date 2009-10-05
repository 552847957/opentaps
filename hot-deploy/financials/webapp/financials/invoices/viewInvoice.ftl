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
-->
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#if invoice?has_content>

<#if invoice.isSalesInvoice()>
  <#assign title = uiLabelMap.FinancialsSalesInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingToParty />
  <#assign whichPartyId = invoice.partyId />
  <#assign partyLink = "customerStatement?partyId=${invoice.partyId}" />
  <#assign sampleInvoiceLink><a href="sampleInvoice.pdf?invoiceId=${invoice.invoiceId}" class="subMenuButton">Sample Invoice PDF</a></#assign>
<#elseif invoice.isPurchaseInvoice()>
  <#assign title = uiLabelMap.FinancialsPurchaseInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
  <#assign whichPartyId = invoice.partyIdFrom />
  <#assign partyLink = "vendorStatement?partyId=${invoice.partyIdFrom}" />
<#elseif invoice.isReturnInvoice()>
  <#assign title = uiLabelMap.FinancialsCustomerReturnInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
  <#assign whichPartyId = invoice.partyIdFrom />
  <#assign partyLink = "customerStatement?partyId=${invoice.partyIdFrom}" />
<#elseif invoice.isCommissionInvoice()>
  <#assign title = uiLabelMap.FinancialsCommissionInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
  <#assign whichPartyId = invoice.partyIdFrom />
  <#assign partyLink = "commissionsStatement?partyId=${invoice.partyIdFrom}" />
<#elseif invoice.isInterestInvoice()>
  <#assign title = uiLabelMap.FinancialsInterestInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingToParty />
  <#assign whichPartyId = invoice.partyId />
  <#assign partyLink = "customerStatement?partyId=${invoice.partyId}" />
<#elseif invoice.isPartnerInvoice()>
  <#assign title = uiLabelMap.OpentapsPartnerInvoice />
  <#assign whichPartyTitle = uiLabelMap.AccountingFromParty />
  <#assign whichPartyId = invoice.partyIdFrom />
</#if>


<#if hasUpdatePermission>
  
<script type="text/javascript">
/*<![CDATA[*/
function notifyInvoiceItemsCount(n) {
  var button = document.getElementById('markAsReadyButton');
  if (n > 0) {
    button.style.visibility = 'visible';
  } else {
    button.style.visibility = 'hidden';
  }
}
/*]]>*/
</script>

  <#assign stateChangeLinks><a href="setInvoiceReady?invoiceId=${invoice.invoiceId}" id="markAsReadyButton" class="subMenuButton" <#if invoiceItems.size() == 0>style="visibility:hidden"</#if>>${uiLabelMap.FinancialsPaymentStatusToReady}</a></#assign>
  <#assign stateChangeLinks>${stateChangeLinks?default("")}<a href="setInvoiceStatus?statusId=INVOICE_CANCELLED&invoiceId=${invoice.invoiceId}" class="subMenuButton">${uiLabelMap.CommonCancel}</a></#assign>
<#elseif (invoice.isReady() || invoice.isPaid()) && (hasWriteoffPermission)>
  <#if (invoice.isSalesInvoice() && invoice.isReady() && invoice.processingStatusId?default("") != "INVOICE_PRINTED")>
    <#assign stateChangeLinks><a href="setInvoiceProcessingStatus?statusId=INVOICE_PRINTED&invoiceId=${invoice.invoiceId}" class="subMenuButton">${uiLabelMap.FinancialsPaymentStatusToPrinted}</a></#assign>
  </#if>
  <#assign stateChangeLinks>${stateChangeLinks?default("")}<@inputConfirm title=uiLabelMap.FinancialsWriteoff href="setInvoiceStatus?statusId=INVOICE_WRITEOFF&invoiceId=${invoice.invoiceId}" class="subMenuButtonDangerous"/></#assign>
</#if>

<#if invoice.isReady()>
  <#assign voidLink><@inputConfirm href="voidInvoice?invoiceId=${invoice.invoiceId}" title=uiLabelMap.FinancialsVoidInvoice class="subMenuButtonDangerous"/></#assign>
  <#assign stateChangeLinks = stateChangeLinks?default("") + voidLink />
</#if>

<#if hasCreatePermission>
  <#assign createPartnerInvoiceLink><a href="<@ofbizUrl><#if invoice.isPartnerInvoice()>createPartnerInvoiceForm<#else>createInvoiceForm?invoiceTypeId=${invoice.invoiceTypeId}</#if></@ofbizUrl>" class="subMenuButton">${uiLabelMap.CommonCreateNew}</a></#assign>
  <#assign stateChangeLinks = stateChangeLinks?default("") + createPartnerInvoiceLink />
</#if>

<#assign partyName = Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, whichPartyId, false) />
<#if partyLink?exists>
  <#assign partyField><a href='${partyLink}' class='linktext'>${partyName}</a></#assign>
<#else>
  <#assign partyField = partyName>
</#if>

<#if ! invoice.isCancelled() && ! invoice.isWrittenOff() && ! invoice.isVoided()>
  <#assign stateChangeLinks>${stateChangeLinks?default("")}<a href="<@ofbizUrl>invoice.pdf?invoiceId=${invoice.invoiceId}</@ofbizUrl>" class="subMenuButton">${uiLabelMap.AccountingInvoicePDF}</a></#assign>
</#if>
<#assign stateChangeLinks = stateChangeLinks?default("") + sampleInvoiceLink?default("") />
<#if ! invoice.isCancelled() && ! invoice.isWrittenOff() && ! invoice.isVoided()>
  <#assign stateChangeLinks>${stateChangeLinks?default("")}<a href="<@ofbizUrl>writeInvoiceEmail?invoiceId=${invoice.invoiceId}</@ofbizUrl>" class="subMenuButton">${uiLabelMap.CommonEmail}</a></#assign>
</#if>

<#-- invoice details -->
<div class="screenlet">
  <div class="subSectionHeader">
    <div class="subSectionTitle">${title} ${uiLabelMap.OrderNbr}${invoice.invoiceId}</div>
    <div class="subMenuBar">${stateChangeLinks?if_exists}</div>
  </div>

  <div class="screenlet-body">
    <table class="twoColumnForm" style="border:0">
      <@displayRow title=whichPartyTitle text=partyField />
      <#if invoice.isPartnerInvoice()>
        <@displayRow title=uiLabelMap.AccountingToParty text=Static["org.ofbiz.party.party.PartyHelper"].getPartyName(delegator, invoice.partyId, false) />
      </#if>

      <#if hasUpdatePermission>
        <form method="post" action="<@ofbizUrl>updateInvoice</@ofbizUrl>" name="updateInvoice">
          <@inputHidden name="invoiceId" value="${invoice.invoiceId}"/>
          <@inputHidden name="statusId" value="${invoice.statusId}"/> <#-- this is becaues the updateInvoice requires status -->
          <#if parameters.oldRefNum?exists><@inputHidden name="oldRefNum" value=parameters.oldRefNum /></#if>
          <@displayRow title=uiLabelMap.CommonStatus text=invoice.getStatusItem().get("description", "FinancialsEntityLabel", locale) />
          <#if invoice.isAdjustable() || invoice.isModifiable()>
            <tr>
              <@displayTitleCell title=uiLabelMap.OpentapsOpenAmount />
              <td class="tabletext"><@displayCurrency amount=invoice.getOpenAmount() currencyUomId=invoice.currencyUomId /></td>
            </tr>
          </#if>
          <@inputDateTimeRow name="invoiceDate" title=uiLabelMap.AccountingInvoiceDate default=invoice.invoiceDate form="updateInvoice" />
          <@inputDateTimeRow name="dueDate" title=uiLabelMap.AccountingDueDate default=invoice.dueDate form="updateInvoice" />
          <@displayRow title=uiLabelMap.AccountingPaidDate text=invoice.paidDate />
          <@inputSelectRow name="contactMechId" title=uiLabelMap.AccountingBillingAddress list=addresses default=invoiceContactMechId required=false ; address>
            ${address.address1?default("")}, ${address.city?default("")}, ${address.stateProvinceGeoId?default("")} ${address.postalCode?default("")}
          </@inputSelectRow>
          <@inputTextRow name="referenceNumber" title=uiLabelMap.FinancialsReferenceNumber size=60 default=invoice.referenceNumber />
          <@displayRow title="${uiLabelMap.OpentapsOrders}" text=ordersList?if_exists />
          <@inputTextRow name="description" title=uiLabelMap.CommonDescription size=60 default=invoice.description />
          <@inputTextRow name="invoiceMessage" title=uiLabelMap.CommonMessage size=60 default=invoice.invoiceMessage />
          <@inputForceCompleteRow title=uiLabelMap.CommonUpdate forceTitle=uiLabelMap.OpentapsForceUpdate form="updateInvoice" /></td>
        </form>
      <#else>
        <#if invoiceAddress?has_content>
          <#assign displayAddress = invoiceAddress.address1?default("") +", "+ invoiceAddress.city?default("") +", "+ invoiceAddress.stateProvinceGeoId?default("") +" "+ invoiceAddress.postalCode?default("")>
        </#if>
        <@displayRow title=uiLabelMap.CommonStatus text=invoice.getStatusItem().get("description", locale) />
        <@displayRow title=uiLabelMap.FinancialsProcessingStatus text=invoice.getProcessingStatusItem()?default({}).description?default("") />
        <#if invoice.isAdjustable() || invoice.isAdjustable()>
          <@displayCurrencyRow title=uiLabelMap.OpentapsOpenAmount amount=invoice.getOpenAmount() currencyUomId=invoice.currencyUomId />
        </#if>
        <@displayDateRow title=uiLabelMap.AccountingInvoiceDate date=invoice.invoiceDate />
        <@displayDateRow title=uiLabelMap.AccountingDueDate date=invoice.dueDate />
        <@displayDateRow title=uiLabelMap.AccountingPaidDate date=invoice.paidDate />
        <@displayRow title=uiLabelMap.AccountingBillingAddress text=displayAddress?if_exists />
        <@displayRow title=uiLabelMap.FinancialsReferenceNumber text=invoice.referenceNumber />
        <@displayRow title=uiLabelMap.OpentapsOrders text=ordersList?if_exists />
        <@displayRow title=uiLabelMap.CommonDescription text=invoice.description />
        <@displayRow title=uiLabelMap.CommonMessage text=invoice.invoiceMessage />
      </#if>
    </table>
  </div>

</div>

<#-- list the invoice terms -->
<#if invoiceTerms?has_content || (hasUpdatePermission && termTypes?has_content)>
    <div class="subSectionBlock">
      <@sectionHeader title=uiLabelMap.FinancialsInvoiceTerms />
        <@form name="deleteInvoiceTermAction" url="deleteInvoiceTerm" invoiceId=invoice.invoiceId invoiceTermId=""/>
        <table class="listTable">
          <tr class="listTableHeader">
            <#--td>${uiLabelMap.FinancialsInvoiceItemSeqId}</td-->
            <td>${uiLabelMap.OrderOrderTermType}</td>
            <td>${uiLabelMap.CommonValue}</td>
            <td>${uiLabelMap.CommonDays}</td>
            <#--td>${uiLabelMap.ProductUnitOfMeasure}</td-->
            <td>${uiLabelMap.CommonDescription}</td>
            <#if hasUpdatePermission>
              <td>&nbsp;</td> 
              <td>&nbsp;</td>
            </#if>
          </tr>
          <#if invoiceTerms?has_content>
            <#if hasUpdatePermission>
              <form method="post" action="<@ofbizUrl>updateInvoiceTermMulti</@ofbizUrl>" name="updateInvoiceTermMulti">
                <@inputHidden name="invoiceId" value="${invoiceId}" />
                <@inputHiddenRowCount list=invoiceTerms />
                <@inputHiddenUseRowSubmit />
            </#if>
            <#list invoiceTerms as invoiceTerm>
              <tr class="${tableRowClass(invoiceTerm_index)}">
                <#if hasUpdatePermission>
                  <@inputHidden name="invoiceId" value="${invoiceId}" index=invoiceTerm_index/>
                  <@inputHidden name="invoiceTermId" value="${invoiceTerm.invoiceTermId}" index=invoiceTerm_index/>
                  <@inputHiddenRowSubmit submit=false index=invoiceTerm_index/>
                  <@displayCell text=invoiceTerm.getTermType().get("description", "OpentapsEntityLabels", locale) style="white-space: nowrap" />
                  <@inputTextCell name="termValue" default=invoiceTerm.termValue?if_exists size=5 index=invoiceTerm_index onChange="opentaps.markRowForSubmit(this.form, ${invoiceTerm_index})"/>
                  <@inputTextCell name="termDays" default=invoiceTerm.termDays?if_exists size=5 index=invoiceTerm_index onChange="opentaps.markRowForSubmit(this.form, ${invoiceTerm_index})"/>
                  <@inputTextCell name="textValue" default=invoiceTerm.textValue?if_exists size=30 index=invoiceTerm_index onChange="opentaps.markRowForSubmit(this.form, ${invoiceTerm_index})"/>
                  <@inputSubmitIndexedCell title="${uiLabelMap.CommonUpdate}" index=invoiceTerm_index/>
                  <td><@submitFormLinkConfirm form="deleteInvoiceTermAction" text=uiLabelMap.CommonRemove invoiceTermId=invoiceTerm.invoiceTermId /></td>
                <#else>
                  <@displayCell text=invoiceTerm.getTermType().get("description", "OpentapsEntityLabels", locale) style="white-space: nowrap" />
                  <@displayCell text=invoiceTerm.termValue?if_exists blockClass="tabletextright" />
                  <@displayCell text=invoiceTerm.termDays?if_exists blockClass="tabletextright" />
                  <@displayCell text=invoiceTerm.textValue?if_exists/>
                </#if>
              </tr>
            </#list>
            <#if hasUpdatePermission></form></#if>
          </#if>

          <#-- create invoice term -->
          <#if hasUpdatePermission && termTypes?has_content>
            <tr class="${tableRowClass(invoiceTerms?size)}">
                <form method="post" action="<@ofbizUrl>createInvoiceTerm</@ofbizUrl>" name="createInvoiceTerm">
                  <@inputHidden name="invoiceId" value="${invoiceId}"/>
                  <@inputSelectCell name="termTypeId" list=termTypes key="termTypeId" displayField="description"/>
                  <@inputTextCell name="termValue" size=5 />
                  <@inputTextCell name="termDays"size=5 />
                  <@inputTextCell name="textValue" size=30 />
                  <@inputSubmitCell title=uiLabelMap.FinancialsInvoiceAddTerm />
                </form>
                <td/>
            </tr>
          </#if>
        </table>
      </div>
</#if> <#-- end list invoice terms -->

<#-- list the invoice items -->
<#if useGwt>
  <div style="float:right"><span class="toggleButtonDisabled">${uiLabelMap.OpentapsGridView}</span><a class="toggleButton" href="viewInvoice?invoiceId=${invoice.invoiceId}&amp;useGwt=N">${uiLabelMap.OpentapsFullView}</a></div>
<#else>
  <div style="float:right"><a class="toggleButton" href="viewInvoice?invoiceId=${invoice.invoiceId}&amp;useGwt=Y">${uiLabelMap.OpentapsGridView}</a><span class="toggleButtonDisabled">${uiLabelMap.OpentapsFullView}</span></div>
  <br/>
</#if>
<div style="clear:right;height:2px">&nbsp;</div>
<#if useGwt>
  <@gwtWidget id="invoiceItems" organizationPartyId=organizationPartyId invoiceId=invoice.invoiceId invoiceTypeId=invoice.invoiceTypeId />
  <br/>
<#else>
  <#if invoiceItems.size() != 0>
    <div class="subSectionBlock">
      <@sectionHeader title=uiLabelMap.AccountingInvoiceItems />
      <table class="listTable">
        <tbody>
          <tr class="listTableHeader">
            <td></td>
            <td>${uiLabelMap.CommonType}</td>
            <td>${uiLabelMap.ProductProductId}</td>
            <td>${uiLabelMap.CommonDescription}</td>
            <td>${uiLabelMap.CommonQuantity}</td>
            <td align="right">${uiLabelMap.CommonAmount}</td>
            <#if hasUpdatePermission>
              <td align="right">${uiLabelMap.CommonTotal}</td>
              <td>&nbsp;</td>
              <td>&nbsp;</td>
            <#else>
              <td align="right">${uiLabelMap.CommonTotal}</td>
            </#if>
          </tr>

          <#assign total = 0/>
          <#if hasUpdatePermission>
            <form method="post" action="<@ofbizUrl>updateInvoiceItemMulti</@ofbizUrl>" name="updateInvoiceItemMulti">
              <@inputHidden name="invoiceId" value="${invoiceId}" />
              <@inputHiddenRowCount list=invoiceItems />
              <@inputHiddenUseRowSubmit />
          </#if>
          <#list invoiceItems as item>
            <#assign rowTotal = item.quantity?default(1) * item.amount?default(0) />
            <tr class="${tableRowClass(item_index)}">
              <#if hasUpdatePermission>
                <@inputHidden name="invoiceId" value="${item.invoiceId}" index=item_index/>
                <@inputHidden name="invoiceItemSeqId" value="${item.invoiceItemSeqId}" index=item_index/>
                <@inputHiddenRowSubmit submit=false index=item_index/>
                <@displayLinkCell href="updateInvoiceItemForm?invoiceId=${item.invoiceId}&invoiceItemSeqId=${item.invoiceItemSeqId}" text=item.invoiceItemSeqId />
                <@displayCell text=item.getInvoiceItemType().get("description", locale) />
                <@inputLookupCell name="productId" default=item.productId?default("") lookup="LookupProduct" form="updateInvoiceItemMulti" size="10" index=item_index onChange="opentaps.markRowForSubmit(this.form, ${item_index})" />
                <@inputTextCell name="description" default=item.description?default(" ") size=60 index=item_index onChange="opentaps.markRowForSubmit(this.form, ${item_index})" />
                <@inputTextCell name="quantity" default=item.quantity?default(" ") size=4 index=item_index onChange="opentaps.markRowForSubmit(this.form, ${item_index})" />
                <@inputTextCell name="amount" default=item.amount?default(" ") size=6 index=item_index onChange="opentaps.markRowForSubmit(this.form, ${item_index})" />
                <@displayCurrencyCell amount=rowTotal currencyUomId=item.uomId?default(invoice.currencyUomId) />
                <@inputSubmitIndexedCell title="${uiLabelMap.CommonUpdate}" index=item_index/>
                <@displayLinkCell href="removeInvoiceItem?invoiceId=${item.invoiceId}&invoiceItemSeqId=${item.invoiceItemSeqId}" class="buttontext" text="${uiLabelMap.CommonRemove}"/>
              <#else>
                <@displayCell text=displayItemSeqId blockClass="tabletextright" />
                <@displayCell text=item.getInvoiceItemType().get("description", locale) style="white-space: nowrap" />
                <@displayCell text=item.productId />
                <@displayCell text=item.description blockStyle="width: 50%" />
                <@displayCell text=item.quantity blockClass="tabletextright" />
                <@displayCurrencyCell amount=item.amount currencyUomId=item.uomId?default(invoice.currencyUomId) />
                <@displayCurrencyCell amount=rowTotal currencyUomId=item.uomId?default(invoice.currencyUomId) />
              </#if>
            </tr>
            <#-- display accounting tags associated with this invoice item -->
            <#if tagTypes?has_content>
              <tr class="${tableRowClass(item_index)}">
                <td colspan="2">&nbsp;</td>
                <td colspan="<#if hasUpdatePermission>7<#else>5</#if>">
                  <i><@accountingTagsDisplay tags=tagTypes entity=item /></i>
                </td>
              </tr>
            </#if>
            <#assign total = total + rowTotal />
          </#list>
          <#if hasUpdatePermission></form></#if>

          <#if invoiceItems.size() != 0>
            <tr>
              <td colspan="6"></td>
              <@displayCurrencyCell amount=total currencyUomId=invoice.currencyUomId class="tableheadtext" />
            </tr>
          </#if>
        </tbody>
      </table>
    </#if> <#-- end list invoice items -->
    <#-- put the new invoice item in the same subSectionBlock as list, which means we have to be careful about the div closing -->
    <#if invoiceItems.size() == 0><div class="subSectionBlock"></#if>
    <#include "createOrUpdateInvoiceItem.ftl">
  </div>
</#if> <#-- end of if useGwt -->

<#include "convertToBillingAccount.ftl">

<#-- invoice payments -->

<div class="subSectionBlock">
<@sectionHeader title=uiLabelMap.FinancialsPaymentsAppliedToInvoice />
  <#if payments.size() == 0>
      <table class="listTable">
         <tr><@displayCell text=uiLabelMap.FinancialsInvoiceNoPaymentApplications /></tr>
      </table>
  <#else>
      <table class="listTable" cellspacing="0">
        <tbody>
          <tr class="listTableHeader">
            <td>${uiLabelMap.OpentapsPaymentId}</td>
            <td>${uiLabelMap.CommonDate}</td>
            <td>${uiLabelMap.FinancialsReferenceNumber}</td>
            <td>${uiLabelMap.CommonStatus}</td>
            <td align="right">${uiLabelMap.CommonAmount}</td>
            <td align="right">${uiLabelMap.AccountingAmountApplied}</td>
          </tr>

          <#list payments as payment>
            <tr class="${tableRowClass(payment_index)}">
              <@displayLinkCell text=payment.paymentId href="viewPayment?paymentId=${payment.paymentId}" />
              <@displayDateCell date=payment.effectiveDate />
              <@displayDateCell date=payment.paymentRefNum />
              <@displayCell text=payment.statusDescription />
              <@displayCurrencyCell amount=payment.amount currencyUomId=payment.currencyUomId />
              <@displayCurrencyCell amount=payment.amountApplied currencyUomId=payment.currencyUomId />
            </tr>
          </#list>
        </tbody>
      </table>
  </#if>
</div>


<#-- credit payments -->

<#if invoice.isReturnInvoice()>
<@sectionHeader title=uiLabelMap.FinancialsCreditAppliedToInvoice />
  <div class="subSectionBlock">
    <#if creditPayments.size() == 0>
      <@display text=uiLabelMap.FinancialsInvoiceNoCreditsIssued />
    <#else>
      <table class="listTable" cellspacing="0">
        <tbody>
          <tr class="listTableHeader">
            <td>${uiLabelMap.FinancialsCustomerCreditAccount}</td>
            <td>${uiLabelMap.CommonDate}</td>
            <td align="right">${uiLabelMap.CommonAmount}</td>
          </tr>

          <#list creditPayments as payment>
            <tr class="${tableRowClass(payment_index)}">
              <#if payment.billingAccountId?exists>
                <@displayLinkCell text=payment.billingAccountId href="viewCustomerBillAcct?billingAccountId=${payment.billingAccountId}" />
              <#else>
                <td></td>
              </#if>
              <@displayDateCell date=payment.effectiveDate />
              <@displayCurrencyCell amount=payment.amountApplied currencyUomId=payment.currencyUomId />
            </tr>
          </#list>
        </tbody>
      </table>
    </#if>
  </div>
</#if>


<#-- list invoice adjustments and create new invoice adjustment -->

<#assign adjustments = invoice.getInvoiceAdjustments()>
<div class="subSectionBlock">
<@sectionHeader title=uiLabelMap.FinancialsAdjustmentsAppliedToInvoice />
  <#if adjustments.size() == 0>
    <table class="listTable">
      <tr><@displayCell text=uiLabelMap.FinancialsInvoiceNoInvoiceAdjustments /></tr>
    </table>
  <#else>
      <table class="listTable" cellspacing="0">
        <tbody>
          <tr class="listTableHeader">
            <td>${uiLabelMap.OpentapsAdjustmentId}</td>
            <td>${uiLabelMap.CommonType}</td>
            <td>${uiLabelMap.OpentapsPaymentId}</td>
            <td>${uiLabelMap.CommonDate}</td>
            <td>${uiLabelMap.CommonComment}</td>
            <td align="right">${uiLabelMap.CommonAmount}</td>
          </tr>

          <#list adjustments as adjustment>
            <tr class="${tableRowClass(adjustment_index)}">
              <@displayCell text=adjustment.getInvoiceAdjustmentId() />
              <@displayCell text=adjustment.getInvoiceAdjustmentType().get("description", locale) />
              <#if adjustment.paymentId?has_content>
                <@displayLinkCell text=adjustment.paymentId href="viewPayment?paymentId=${adjustment.paymentId}" />
              <#else>
                <td></td>
              </#if>
              <@displayDateCell date=adjustment.effectiveDate />
              <@displayCell text=adjustment.comment />
              <@displayCurrencyCell amount=adjustment.amount currencyUomId=invoice.currencyUomId />
            </tr>
          </#list>
        </tbody>
      </table>
  </#if>

  <#if hasAdjustmentPermission>
    <form method="get" action="<@ofbizUrl>createInvoiceAdjustmentViewInvoice</@ofbizUrl>" name="createInvoiceAdjustment">
      <@inputHidden name="invoiceId" value="${invoice.invoiceId}"/>
      <@sectionHeader title=uiLabelMap.FinancialsNewInvoiceAdjustment headerClass="screenlet-header" titleClass="boxhead"/>
      <table class="twoColumnForm">
        <@inputSelectRow name="invoiceAdjustmentTypeId" title=uiLabelMap.CommonType list=invoiceAdjustmentTypes displayField="description" />
        <@inputCurrencyRow title=uiLabelMap.CommonAmount name="adjustmentAmount" defaultCurrencyUomId=invoice.currencyUomId disableCurrencySelect=true />
        <@inputTextRow name="comment" title=uiLabelMap.CommonComment size=60 />
        <@inputSubmitRow title=uiLabelMap.CommonAdd />
      </table>
    </form>
  </#if>
</div>



<#else> <#-- if invoice has no content -->
  ${uiLabelMap.FinancialsInvoiceNotFound}
</#if>
