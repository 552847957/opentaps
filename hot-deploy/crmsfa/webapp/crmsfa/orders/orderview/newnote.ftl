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
<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#-- This file has been modified by Open Source Strategies, Inc. -->

<@import location="component://opentaps-common/webapp/common/includes/lib/opentapsFormMacros.ftl"/>

<div class="subSectionBlock">
  <@sectionHeader title=uiLabelMap.OrderAddNote />
    <table class="twoColumnForm">
    <#if security.hasEntityPermission("ORDERMGR", "_VIEW", session)>
      <form method="post" action="<@ofbizUrl>createordernote/${donePage}</@ofbizUrl>" name="createnoteform">
        <@inputTextareaRow title=uiLabelMap.OrderNote name="note" cols=70 />
        <@inputIndicatorRow title=uiLabelMap.OrderInternalNote name="internalNote" />
        <@displayRow title="" text="<i>${uiLabelMap.OrderInternalNoteMessage}</i>" />
        <tr>
          <td/>
          <td>
            <@inputSubmit title=uiLabelMap.CommonSave />
            <@displayLink text=uiLabelMap.CommonGoBack href="authview/${donePage}" class="buttontext" />
          </td>
        </tr>
      </form>
  
    <#else/>
      <tr><@displayCell text=uiLabelMap.OrderViewPermissionError/></tr>
    </#if>
  </table>
</div>
