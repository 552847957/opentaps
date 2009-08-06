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
<#-- Copyright (c) 2005-2006 Open Source Strategies, Inc. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<@sectionHeader title=uiLabelMap.ManufacturingEditRoutingProductLink />

<#if routingProductLink?has_content>
  <form name="UpdateRoutingProductLink" action="<@ofbizUrl>UpdateRoutingProductLink</@ofbizUrl>" method="post">
    <@inputHidden name="workEffortId" value=routingProductLink.workEffortId />
    <@inputHidden name="productId" value=routingProductLink.productId />
    <@inputHidden name="workEffortGoodStdTypeId" value=routingProductLink.workEffortGoodStdTypeId />
    <@inputHidden name="fromDate" value=routingProductLink.fromDate />
    <@inputHidden name="statusId" value="" />
    <table class="twoColumnForm">
      <@displayRow title=uiLabelMap.ProductProductId text=routingProductLink.productId />
      <@displayDateRow title=uiLabelMap.CommonFromDate   date=routingProductLink.fromDate />
      <@inputDateRow title=uiLabelMap.CommonThruDate name="thruDate" default=routingProductLink.thruDate! />
      <@inputTextRow title=uiLabelMap.ManufacturingQuantity name="estimatedQuantity" default=routingProductLink.estimatedQuantity! />
      <@inputTextRow title=uiLabelMap.PurchMinQuantity name="minQuantity" default=routingProductLink.minQuantity! />
      <@inputTextRow title=uiLabelMap.PurchMaxQuantity name="maxQuantity" default=routingProductLink.maxQuantity! />
      <@inputTextRow title=uiLabelMap.FormFieldTitle_estimatedCost name="estimatedCost" default=routingProductLink.estimatedCost! />
      <tr>
        <td>&nbsp;</td>
        <td>
          <@inputSubmit title=uiLabelMap.CommonSave />
          <@displayLink href="EditRouting?workEffortId=${workEffortId}" text=uiLabelMap.CommonCancel class="buttontext" />
        </td>
      </tr>
    </table>
  </form>
<#else>
  <form name="AddRoutingProductLink" action="<@ofbizUrl>AddRoutingProductLink</@ofbizUrl>" method="post">
    <@inputHidden name="workEffortId" value=workEffortId />
    <@inputHidden name="workEffortGoodStdTypeId" value="ROU_PROD_TEMPLATE" />
    <@inputHidden name="statusId" value="" />
    <table class="twoColumnForm">
      <@inputAutoCompleteProductRow title=uiLabelMap.ProductProductId name="productId" id="product_link_productId" form="AddRoutingProductLink" />
      <@inputDateRow title=uiLabelMap.CommonFromDate name="fromDate" size=20 id="AddRoutingProductLinkFromDate" />
      <@inputDateRow title=uiLabelMap.CommonThruDate name="thruDate" size=20 id="AddRoutingProductLinkThruDate" />
      <@inputTextRow title=uiLabelMap.ManufacturingQuantity name="estimatedQuantity" size=10 />
      <@inputTextRow title=uiLabelMap.PurchMinQuantity name="minQuantity" size=10 />
      <@inputTextRow title=uiLabelMap.PurchMaxQuantity name="maxQuantity" size=10 />
      <@inputTextRow title=uiLabelMap.FormFieldTitle_estimatedCost name="estimatedCost" size=10 />
      <@inputSubmitRow title=uiLabelMap.CommonCreate />
    </table>
  </form>
</#if>
<br/>
