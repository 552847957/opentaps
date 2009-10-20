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
<#-- Copyright (c) 2005-2006 Open Source Strategies, Inc. -->

<#if hasUpdatePermission?exists>
<#assign updateLink = "<a class='subMenuButton' href='updateCaseForm?custRequestId=" + case.custRequestId + "'>" + uiLabelMap.CommonEdit + "</a>">
<#if hasClosePermission?exists>
<#assign closeLink = "<a class='subMenuButtonDangerous' href='closeCase?custRequestId=" + case.custRequestId + "'>" + uiLabelMap.CrmCloseCase + "</a>">
</#if>
</#if>

<div class="subSectionHeader">
    <div class="subSectionTitle">${uiLabelMap.CrmCase}
        <#if caseClosed?exists><span class="subSectionWarning">${uiLabelMap.CrmCaseClosed}</span></#if>
    </div>
    <div class="subMenuBar">${updateLink?if_exists}${closeLink?if_exists}</div>
</div>
