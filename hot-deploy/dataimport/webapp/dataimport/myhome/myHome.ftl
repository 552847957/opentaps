<#--
 * Copyright (c) 2006 - 2010 Open Source Strategies, Inc.
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

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<#macro importForm importService label sectionLabel submitLabel processed notProcessed reportHref reportLabel>
    <form name="${importService}Form" method="post" action="setServiceParameters">
      <@inputHidden name="SERVICE_NAME" value="${importService}"/>
      <@inputHidden name="POOL_NAME" value="pool"/>
      <@inputHidden name="sectionHeaderUiLabel" value="${sectionLabel}"/>
      <@displayCell text="${label}:"/>
      <@displayCell text="${processed}"/>
      <@displayCell text="${notProcessed}"/>
      <#if hasDIAdminPermissions?default(false)>
        <@inputSubmitCell title="${submitLabel}"/>
      </#if>
      <@displayLinkCell href="${reportHref}" text="${reportLabel}" />
    </form>
    
</#macro>

<table  class="headedTable">
  <tr class="header">
    <@displayCell text=uiLabelMap.DataImportImporting/>
    <@displayCell text=uiLabelMap.DataImportNumberProcessed/>
    <@displayCell text=uiLabelMap.DataImportNumberNotProcessed/>
    <#if hasFullPermissions?default(false)><td>&nbsp;</td></#if>
  </tr>
  <tr>
    <@importForm importService="importCustomers"
                 sectionLabel="DataImportImportCustomers"
                 label=uiLabelMap.FinancialsCustomers
                 submitLabel=uiLabelMap.DataImportImport
                 processed=customersProcessed notProcessed=customersNotProcessed
                 reportHref="setupReport?reportId=CUST_IMP"
                 reportLabel=uiLabelMap.DataImportCustomersImportReport/>
  </tr>
  <tr>
    <@importForm importService="importSuppliers"
                 sectionLabel="DataImportImportSuppliers"
                 label=uiLabelMap.PurchSuppliers
                 submitLabel=uiLabelMap.DataImportImport
                 processed=suppliersProcessed notProcessed=suppliersNotProcessed
                 reportHref="setupReport?reportId=SUPPL_IMP"
                 reportLabel=uiLabelMap.DataImportSuppliersImportReport/>
  </tr>
  <tr>
    <@importForm importService="importProducts"
                 sectionLabel="DataImportImportProducts"
                 label=uiLabelMap.ProductProducts
                 submitLabel=uiLabelMap.DataImportImport
                 processed=productsProcessed notProcessed=productsNotProcessed
                 reportHref="setupReport?reportId=PROD_IMP"
                 reportLabel=uiLabelMap.DataImportProductsImportReport/>
  </tr>
  <tr>
    <@importForm importService="importProductInventory"
                 sectionLabel="DataImportImportInventory"
                 label=uiLabelMap.ProductInventoryItems
                 submitLabel=uiLabelMap.DataImportImport
                 processed=inventoryProcessed notProcessed=inventoryNotProcessed
                 reportHref="setupReport?reportId=INVENT_IMP"
                 reportLabel=uiLabelMap.DataImportInventoryImportReport/>
  </tr>
  <tr>
    <@importForm importService="importGlAccounts"
                 sectionLabel="DataImportImportGlAccounts"
                 label=uiLabelMap.DataImportGlAccounts
                 submitLabel=uiLabelMap.DataImportImport
                 processed=glAccountsProcessed notProcessed=glAccountsNotProcessed
                 reportHref="setupReport?reportId=GL_ACCTS_IMP"
                 reportLabel=uiLabelMap.DataImportGlAccountsImportReport/>
  </tr>
  <tr>
    <@importForm importService="importOrders"
                 sectionLabel="DataImportImportOrders"
                 label=uiLabelMap.DataImportOrderLines
                 submitLabel=uiLabelMap.DataImportImport
                 processed=orderHeadersProcessed notProcessed=orderHeadersNotProcessed
                 reportHref="setupReport?reportId=ORDER_H_IMP"
                 reportLabel=uiLabelMap.DataImportOrderHeaderLinesImportReport/>
  </tr>
  <tr>
    <@displayCell text="${uiLabelMap.DataImportOrderItemLines}:"/>
    <@displayCell text="${orderItemsProcessed}"/>
    <@displayCell text="${orderItemsNotProcessed}"/>
    <td>&nbsp;</td>
    <@displayLinkCell href="setupReport?reportId=ORDER_I_IMP" text=uiLabelMap.DataImportOrderItemLinesImportReport />
  </tr>
</table>

<br/>

<#if hasDIAdminPermissions?default(false)>
  <@frameSection title=uiLabelMap.DataImportUploadFile>
    <form name="uploadFileAndImport" method="post" enctype="multipart/form-data" action="uploadFileForDataImport">
      <@inputHidden name="POOL_NAME" value="pool"/>
      <@inputHidden name="sectionHeaderUiLabel" value="DataImportImportFromFile"/>
      <table class="twoColumnForm">
        <@inputFileRow title=uiLabelMap.DataImportFileToImport name="uploadedFile" />
        <tr>
          <@displayTitleCell title=uiLabelMap.DataImportUploadFileFormat />
          <td>
            <select name="fileFormat" class="inputBox">
              <option value="EXCEL">${uiLabelMap.DataImportUploadFileFormatExcel}</option>
            </select>
          </td>
        </tr>
        <@inputSubmitRow title="${uiLabelMap.DataImportUpload}"/>
      </table>
    </form>
  </@frameSection>
</#if>
