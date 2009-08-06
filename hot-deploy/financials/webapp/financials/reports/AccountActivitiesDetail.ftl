<#--
 * Copyright (c) 2006 - 2009 Open Source Strategies, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Honest Public License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Honest Public License for more details.
 *
 * You should have received a copy of the Honest Public License
 * along with this program; if not, write to Funambol,
 * 643 Bair Island Road, Suite 305 - Redwood City, CA 94063, USA
-->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#macro displayAccountActivityDetail accountActivityDetail index=-1>
</#macro>

<div style="margin-bottom: 15px">
  <#assign findFormState = ("Y" == parameters.performFind?default(""))?string("closed", "open") />
  <@flexArea targetId="findAccountActivitiesDetail" title=uiLabelMap.FinancialsFindAccountActivitiesDetail save=false state=findFormState >
    <form method="post" action="<@ofbizUrl>AccountActivitiesDetail</@ofbizUrl>" name="findAccountActivitiesDetail">
      <table>
        <@inputSelectRow title=uiLabelMap.FinancialsTransactionType name="acctgTransTypeId" list=acctgTransTypes displayField="description" required=false/>
        <tr>
          <@displayTitleCell title=uiLabelMap.FormFieldTitle_glAccountId />
          <td colspan="3"><@inputAutoCompleteGlAccount name="glAccountId" id="glAccountId" default=glAccountId/></td>
        </tr>
        <tr>
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_productId />
          <@inputLookupCell     name="productId" form="findAccountActivitiesDetail" lookup="LookupProduct" />
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_partyId />
          <@inputLookupCell     name="partyId" form="findAccountActivitiesDetail" lookup="LookupPartyName" />
        </tr>
        <tr>
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_paymentId />
          <@inputTextCell       name="paymentId" />
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_invoiceId />
          <@inputTextCell       name="invoiceId" />
        </tr>
        <tr>
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_shipmentId />
          <@inputTextCell       name="shipmentId" />
          <@displayTitleCell    title=uiLabelMap.FormFieldTitle_workEffortId />
          <@inputTextCell       name="workEffortId" />
        </tr>
        <tr>
          <@displayTitleCell    title=uiLabelMap.CommonFromDate />
          <@inputDateTimeCell   name="transactionFromDate" form="findAccountActivitiesDetail" default=transactionFromDate />
          <@displayTitleCell    title=uiLabelMap.CommonThruDate />
          <@inputDateTimeCell   name="transactionThruDate" form="findAccountActivitiesDetail" default=transactionThruDate />
        </tr>
        <tr>
          <@displayTitleCell    title=uiLabelMap.FinancialsIsPosted />
          <@inputIndicatorCell  name="isPosted" required=false default=(parameters.isPosted)?default("Y") />
          <@displayTitleCell    title=uiLabelMap.FinancialsDebitCredit />
          <#assign creditDebitHash = {"C":uiLabelMap.CommonCredit,"D":uiLabelMap.CommonDebit} />
          <@inputSelectHashCell name="debitCreditFlag" required=false hash=creditDebitHash />
        </tr>
        <#-- List possible tags -->
        <#list tagTypes as tag>
          <tr>
            <@displayTitleCell title=tag.description />
            <@inputSelectCell name="tag${tag.index}" list=tag.tagValues key="enumId" required=true default="" ; tagValue>
              ${tagValue.description}
            </@inputSelectCell>
          </tr>
        </#list>
        <@inputHidden          name="performFind" value="Y" />
        <@inputSubmitRow       title=uiLabelMap.CommonFind />
      </table>
    </form>
  </@flexArea>
</div>

<@paginate name="myList" list=accountActivitiesDetailsBuilder>
<#noparse >
<div style="margin-bottom: 15px">
  <div class="subSectionHeader">
    <div class="subMenuBar">
      <@paginationNavContext />
    </div>
  </div>
  <table class="listTable" cellspacing="0" style="margin-top:15px; margin-bottom: 15px">
    <tbody>
    <tr class="listTableHeader" style="background-color:white">
      <@headerCell title=uiLabelMap.AccountingGlAccount            orderBy="glAccountId" />
      <@headerCell title=uiLabelMap.FormFieldTitle_isPosted        orderBy="isPosted" />
      <@headerCell title=uiLabelMap.FinancialsTransactionDate      orderBy="transactionDate" />
      <@headerCell title=uiLabelMap.FinancialsTransaction          orderBy="acctgTransId" />
      <@headerCell title=uiLabelMap.CommonType                     orderBy="acctgTransTypeId" />
      <@headerCell title=uiLabelMap.AccountingInvoice              orderBy="invoiceId" />
      <@headerCell title=uiLabelMap.AccountingPayment              orderBy="paymentId" />
      <@headerCell title=uiLabelMap.FormFieldTitle_shipmentId      orderBy="shipmentId" />
      <@headerCell title=uiLabelMap.ProductItem                    orderBy="inventoryItemId" />
      <@headerCell title=uiLabelMap.WorkEffortWorkEffort           orderBy="workEffortId" />
      <@headerCell title=uiLabelMap.PartyParty                     orderBy="partyId" />
      <@headerCell title=uiLabelMap.ProductProduct                 orderBy="productId" />
      <@headerCell title=uiLabelMap.CommonDebit                    orderBy="debitCreditFlag DESC,amount" orderByReverse="debitCreditFlag DESC,amount DESC" />
      <@headerCell title=uiLabelMap.CommonCredit                   orderBy="debitCreditFlag ASC,amount" orderByReverse="debitCreditFlag ASC,amount DESC" />
    </tr>
    <#-- show all account activities -->
    <#list pageRows as row>
      <tr class="${tableRowClass(row_index)}">
        <td>
          <a href="<@ofbizUrl>AccountActivitiesDetail?glAccountId=${row.glAccountId}&amp;isPosted=</@ofbizUrl>" class="linktext">${row.accountCode}</a>:
          ${row.accountName}
        </td>
        <@displayLinkCell href="AccountActivitiesDetail?isPosted=${row.isPosted?if_exists}" text=row.isPosted />
        <@displayDateCell date=row.transactionDate />
        <@displayLinkCell href="viewAcctgTrans?acctgTransId=${row.acctgTransId?if_exists}" text=row.acctgTransId />
        <@displayCell text=row.transTypeDescription />
        <@displayLinkCell href="AccountActivitiesDetail?invoiceId=${row.invoiceId?if_exists}&amp;isPosted=" text=row.invoiceId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?paymentId=${row.paymentId?if_exists}&amp;isPosted=" text=row.paymentId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?shipmentId=${row.shipmentId?if_exists}&amp;isPosted=" text=row.shipmentId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?inventoryItemId=${row.inventoryItemId?if_exists}&amp;isPosted=" text=row.inventoryItemId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?workEffortId=${row.workEffortId?if_exists}&amp;isPosted=" text=row.workEffortId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?partyId=${row.partyId?if_exists}&amp;isPosted="     text=row.partyId?if_exists />
      	<@displayLinkCell href="AccountActivitiesDetail?productId=${row.productId?if_exists}&amp;isPosted=" text=row.productId?if_exists />
        <#if row.debitCreditFlag = "D" >
          <@displayCurrencyCell amount=row.amount currencyUomId=row.currencyUomId />
          <td align="center">&nbsp;</td>
        <#else >
          <td align="center">&nbsp;</td>
          <@displayCurrencyCell amount=row.amount currencyUomId=row.currencyUomId />
        </#if >
      </tr>
    </#list >
    </tbody>
  </table>
</div>
</#noparse >
</@paginate>
