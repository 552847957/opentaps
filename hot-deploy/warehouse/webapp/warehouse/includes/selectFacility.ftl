<#--
 * Copyright (c) 2007 - 2009 Open Source Strategies, Inc.
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

<div class="subSection">

  <div class="subSectionBlock">
    <div class="subSectionHeader">
      <div class="subSectionTitle">${uiLabelMap.WarehouseChooseWarehouse}</div>
    </div>
    
    <div class="form">
    <#if facilities.size() != 0>
      <form method="post" action="<@ofbizUrl>setFacility</@ofbizUrl>">
        <select name="facilityId" class="selectBox">
          <#list facilities as facility>
          <option value="${facility.facilityId}">${facility.facilityName}</option>
          </#list>
        </select>
        <input type="submit" class="smallSubmit" value="${uiLabelMap.CommonSelect}"/>
      </form>
    </#if>
  
    <#if hasCreateWarehousePermission>
    <p><a href="<@ofbizUrl>createWarehouseForm</@ofbizUrl>" class="tabletext">${uiLabelMap.WarehouseCreateNewWarehouse}</a></p>
    </#if>
   </div>
    
  </div>
  
</div>
