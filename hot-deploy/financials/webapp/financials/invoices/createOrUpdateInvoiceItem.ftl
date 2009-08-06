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

<#if hasUpdatePermission>
           
<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>
<#if invoiceItem?exists><#assign formAction = "updateInvoiceItem"><#else><#assign formAction = "createInvoiceItem"></#if>

<form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="${formAction}">
  <@inputHidden name="invoiceId" value="${invoice.invoiceId}"/>
  <@inputHidden name="validateAccountingTags" value="True"/>

<#if invoiceItem?exists>
  <@inputHidden name="invoiceItemSeqId" value="${invoiceItem.invoiceItemSeqId}"/>
  <div class="screenlet">
    <div class="screenlet-header">
      <div class="boxhead">
        Update Invoice #<a href="<@ofbizUrl>viewInvoice?invoiceId=${invoice.invoiceId}</@ofbizUrl>" class="buttontext">${invoice.invoiceId}</a> ${uiLabelMap.FinancialsInvoiceItemSeqId} ${invoiceItem.invoiceItemSeqId}
      </div>
    </div>
    <div class="screenlet-body">
      <table>
        <@inputSelectRow name="invoiceItemTypeId" title=uiLabelMap.CommonType list=invoiceItemTypes displayField="description" default=invoiceItem.invoiceItemTypeId />
        <@inputTextRow name="description" title=uiLabelMap.CommonDescription size="60" default=invoiceItem.description />
        <tr>
          <@displayTitleCell title=uiLabelMap.FinancialsOverrideGlAccount />
          <td ><@inputAutoCompleteGlAccount name="overrideGlAccountId" id="overrideGlAccountId" default=invoiceItem.overrideGlAccountId/></td>
          </tr>
        <#if invoice.invoiceTypeId == "SALES_INVOICE" || invoice.invoiceTypeId == "PURCHASE_INVOICE">
          <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId default=invoiceItem.productId />
        </#if>
      </table>
      <table>
        <tr>
          <@displayCell text=uiLabelMap.CommonQuantity blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
          <@inputTextCell name="quantity" size=4 default=invoiceItem.quantity />
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext"/>
          <@inputCurrencyCell name="amount" currencyName="uomId" default=invoiceItem.amount defaultCurrencyUomId=parameters.orgCurrencyUomId disableCurrencySelect=true/>
        </tr>
        <tr>
          <@displayCell text=uiLabelMap.FinancialsIsTaxable blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
          <@inputIndicatorCell name="taxableFlag" default=invoiceItem.taxableFlag />
          <@displayCell text=uiLabelMap.AccountingTaxAuthority blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext"/>
          <@inputSelectTaxAuthorityCell list=taxAuthorities required=false defaultGeoId=invoiceItem.taxAuthGeoId defaultPartyId=invoiceItem.taxAuthPartyId />
        </tr>

        <#if tagTypes?has_content>
          <@accountingTagsSelectRows tags=tagTypes prefix="acctgTagEnumId" entity=invoiceItem />
        </#if>
        <@inputSubmitRow title=uiLabelMap.CommonUpdate />
      </table>
    </div>
  </div>
<#else>
  <div class="screenlet">
    <div class="screenlet-header"><div class="boxhead">${uiLabelMap.FinancialsNewInvoiceItem}</div></div>
    <div class="screenlet-body">
      <table border="0" cellpadding="2" cellspacing="0" width="100%">
        <@inputSelectRow name="invoiceItemTypeId" title=uiLabelMap.CommonType list=invoiceItemTypes displayField="description" />
        <@inputTextRow name="description" title=uiLabelMap.CommonDescription size="60" />
        <tr>
          <@displayTitleCell title=uiLabelMap.FinancialsOverrideGlAccount />
          <td ><@inputAutoCompleteGlAccount name="overrideGlAccountId" id="overrideGlAccountId" default=glAccountId/></td>
        </tr>
        <#if invoice.invoiceTypeId == "SALES_INVOICE" || invoice.invoiceTypeId == "PURCHASE_INVOICE">
          <@inputAutoCompleteProductRow name="productId" title=uiLabelMap.ProductProductId />
        </#if>
      </table>
      <table>
        <tr>
          <@displayCell text=uiLabelMap.CommonQuantity blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext" />
          <@inputTextCell name="quantity" size=4 />
          <@displayCell text=uiLabelMap.CommonAmount blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
          <@inputCurrencyCell name="amount" currencyName="uomId" defaultCurrencyUomId=parameters.orgCurrencyUomId disableCurrencySelect=true/>
        </tr>
        <tr>
          <@displayCell text=uiLabelMap.FinancialsIsTaxable blockClass="titleCell" blockStyle="width: 200px" class="tableheadtext"/>
          <@inputIndicatorCell name="taxableFlag" default="N"/>
          <@displayCell text=uiLabelMap.AccountingTaxAuthority blockClass="titleCell" blockStyle="width: 100px" class="tableheadtext" />
          <@inputSelectTaxAuthorityCell list=taxAuthorities required=false/>
        </tr>

        <#if !disableTags?exists && tagTypes?has_content>
          <@accountingTagsSelectRows tags=tagTypes prefix="acctgTagEnumId" />
        </#if>
        <@inputSubmitRow title=uiLabelMap.CommonAdd/>
      </table>
    </div>
  </div>
</#if>

</form>

<#else>
<#-- TODO The error here depends on what happened:  either invoice, invoiceItem or hasUpdatePermission was missing/false -->
</#if>
