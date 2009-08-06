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

<#assign requiredSelectValues = {"Y": uiLabelMap.CommonRequired, "N": uiLabelMap.PartyOptional} />

<@sectionHeader title=uiLabelMap.FinancialsAccountingTagUsage />
<div class="screenlet-body">
  <#list usages as usage>
    <#-- might be null if the usage is not configured yet -->
    <#assign configuration = usageConfigurations.get(usage)! />
    <div class="screenlet">
      <div class="screenlet-header"><span class="boxhead">${usage.description}</span></div>
      <form method="post" action="<@ofbizUrl>updateAccountingTagUsage</@ofbizUrl>" name="updateAccountingTagUsage">
        <@inputHidden name="acctgTagUsageTypeId" value=usage.acctgTagUsageTypeId />
        <@inputHidden name="organizationPartyId" value=organizationPartyId />
        <table class="listTable" style="border:0">
          <tr class="listTableHeader">
            <@displayCell text="" />
            <@displayCell text=uiLabelMap.CommonType />
            <@displayCell text=uiLabelMap.CommonRequired />
            <@displayCell text="" />
            <@displayCell text=uiLabelMap.CommonType />
            <@displayCell text=uiLabelMap.CommonRequired />
            <@displayCell text="" />
            <@displayCell text=uiLabelMap.CommonType />
            <@displayCell text=uiLabelMap.CommonRequired />
          </tr>
          <#-- layout 3 columns -->
          <#list 1..10 as i>
            <#if (i % 3) == 1>
              <tr class="${tableRowClass(i % 6)}">
            </#if>
            <#assign typeId = "enumTypeId" + i />
            <@displayTitleCell title=i />
            <@inputSelectCell name=typeId default=(configuration.get(typeId))! list=tagTypes key="enumTypeId" required=false ; type>
              ${type.description}
            </@inputSelectCell>
            <@inputSelectHashCell name="isTagEnum${i}Required" default=(configuration.get("isTagEnum${i}Required"))!"N" hash=requiredSelectValues />
            <#if (i % 3) == 0>
              </tr>
            </#if>
          </#list>
          <@inputSubmitRow title=uiLabelMap.CommonUpdate />
        </table>
      </form>
    </div>
  </#list>
</div>
