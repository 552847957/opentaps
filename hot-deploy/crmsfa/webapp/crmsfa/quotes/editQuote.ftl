<#--
 * Copyright (c) 2009 - 2009 Open Source Strategies, Inc.
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

<#if quote?exists><#assign formAction = "updateQuote"><#else><#assign formAction = "createQuote"></#if>

<div class="subSectionBlock">
  <div class="form">
    <form method="post" action="<@ofbizUrl>${formAction}</@ofbizUrl>" name="${formAction}">
      <table class="twoColumn">
        <#if quote?exists>
          <#-- Edit an existing quote -->
          <@inputHidden name="quoteId" value="${quote.quoteId}"/>
          <@inputTextRow name="quoteName" title=uiLabelMap.OrderOrderQuoteName default=quote.quoteName />
          <@inputHidden name="quoteTypeId" value="PRODUCT_QUOTE"/>
          <@inputHidden name="currencyUomId" value=quote.currencyUomId?if_exists/>
          <@inputAutoCompleteAccountRow name="partyId" id="partyId" title=uiLabelMap.CrmAccount titleClass="requiredField" styleClass="inputAutoCompleteQuick" default=quote.partyId?if_exists />
          <@inputLookupRow name="contactPartyId" title=uiLabelMap.CrmContact lookup="LookupContacts" form=formAction default=quote.contactPartyId?if_exists />
          <@inputSelectRow name="productStoreId" title=uiLabelMap.OrderProductStore list=productStores displayField="storeName" titleClass="requiredField" default=quote.productStoreId?default(defaultProductStoreId?if_exists) ignoreParameters=true />
          <@inputSelectRow name="salesChannelEnumId" title=uiLabelMap.OrderSalesChannel list=salesChannels displayField="description" key="enumId" default=quote.salesChannelEnumId titleClass="requiredField" />
          <@inputDateTimeRow name="validFromDate" title=uiLabelMap.CommonValidFromDate default=quote.validFromDate?if_exists />
          <@inputDateTimeRow name="validThruDate" title=uiLabelMap.CommonValidThruDate default=quote.validThruDate?if_exists />
          <@inputTextareaRow name="description" title=uiLabelMap.CommonDescription default=quote.description?if_exists />
          <tr>
            <td/>
            <td>
              <@inputSubmit title=uiLabelMap.CommonSave />
              <@displayLink text=uiLabelMap.CommonCancel href="ViewQuote?quoteId=${quote.quoteId}" class="buttontext" />
          </tr>
        <#else>
          <#-- Create a new quote -->
          <@inputTextRow name="quoteName" title=uiLabelMap.OrderOrderQuoteName />
          <@inputHidden name="createdByPartyId" value="${userLogin.partyId}"/>
          <@inputHidden name="quoteTypeId" value="PRODUCT_QUOTE"/>
          <@inputHidden name="currencyUomId" value=defaultCurrencyUomId?if_exists />
          <@inputHidden name="validFromDate" value=Static["org.ofbiz.base.util.UtilDateTime"].nowTimestamp()/>
          <@inputAutoCompleteAccountRow name="partyId" id="partyId" title=uiLabelMap.CrmAccount titleClass="requiredField" styleClass="inputAutoCompleteQuick" />
          <@inputLookupRow name="contactPartyId" title=uiLabelMap.CrmContact lookup="LookupContacts" form=formAction />
          <@inputSelectRow name="productStoreId" title=uiLabelMap.OrderProductStore list=productStores displayField="storeName" titleClass="requiredField" default=defaultProductStoreId?if_exists ignoreParameters=true />
          <@inputSelectRow name="salesChannelEnumId" title=uiLabelMap.OrderSalesChannel list=salesChannels displayField="description" key="enumId" titleClass="requiredField" />
          <@inputDateTimeRow name="validThruDate" title=uiLabelMap.CommonValidThruDate />
          <@inputTextareaRow name="description" title=uiLabelMap.CommonDescription />
          <@inputSubmitRow title=uiLabelMap.CrmCreateQuote />
        </#if>
      </table>
    </form>
  </div>
</div>
