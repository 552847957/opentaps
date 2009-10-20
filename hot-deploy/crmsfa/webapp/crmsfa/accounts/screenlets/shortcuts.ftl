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

<div class="screenlet">
    <div class="screenlet-header"><div class="boxhead">${uiLabelMap.CrmShortcuts}</div></div>
    <div class="screenlet-body">
      <ul class="shortcuts">
        <#if viewPreferences.get("MY_OR_TEAM_ACCOUNTS")?default("TEAM_VALUES") == "MY_VALUES">
        <li><a href="<@ofbizUrl>myAccounts</@ofbizUrl>">${uiLabelMap.CrmMyAccounts}</a></li>
        <#else>
        <li><a href="<@ofbizUrl>myAccounts</@ofbizUrl>">${uiLabelMap.CrmTeamAccounts}</a></li>
        </#if>
        <#if (security.hasEntityPermission("CRMSFA_ACCOUNT", "_CREATE", session))>
        <li><a href="<@ofbizUrl>createAccountForm</@ofbizUrl>">${uiLabelMap.CrmCreateAccount}</a></li>
        </#if>
        <li><a href="<@ofbizUrl>findAccounts</@ofbizUrl>">${uiLabelMap.CrmFindAccounts}</a></li>
        <li><a href="<@ofbizUrl>mergeAccountsForm</@ofbizUrl>">${uiLabelMap.CrmMergeAccounts}</a></li>
      </ul>
    </div>
</div>
