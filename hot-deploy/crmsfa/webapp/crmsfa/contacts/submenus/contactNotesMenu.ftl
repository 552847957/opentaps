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

<#-- TODO: this can be refactored for all note creation (put in includes/, use ${uri} to call) -->
<div class="subSectionHeader">
    <div class="subSectionTitle">${uiLabelMap.CrmNotes}</div>
    <#if hasUpdatePermission?exists>
    <div class="subMenuBar"><a class="subMenuButton" href="createContactNoteForm?partyId=${partySummary.partyId}">${uiLabelMap.CrmCreateNew}</a></div>
    </#if>
</div>
