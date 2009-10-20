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

<#-- This is re-used everywhere for pending activities so it is one standard page.  Whichever page calls it should put in the activityValueParams, be they party, case, or opportunity ids -->
<a name="ListCompletedActivities"></a>
<div class="subSectionHeader">
    <div class="subSectionTitle">${uiLabelMap.CrmCompletedActivities}</div><#if hasNewActivityPermission?exists><div class="subMenuBar"><a class="subMenuButton" href="logTaskForm?${activityValueParams}&workEffortPurposeTypeId=WEPT_TASK_PHONE_CALL">${uiLabelMap.CrmLogCall}</a><a class="subMenuButton" href="logTaskForm?${activityValueParams}&workEffortPurposeTypeId=WEPT_TASK_EMAIL">${uiLabelMap.CrmLogEmail}</a></div>
    </#if>
</div>
