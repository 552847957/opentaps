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

<#-- This file controls the settings of keyboard shortcuts, they should be loaded in the main-decorator.bsh -->

<!-- keyboard shortcuts -->
<#if keyboardShortcuts?has_content>
<script type="text/javascript">
/*<![CDATA[*/

  <#list keyboardShortcuts as kb>
    ${kb.handler}('${kb.shortcut}', '${kb.actionTarget}');
  </#list>

    dojo.require("dijit.form.Button");
    dojo.require("dijit.Dialog");

    function showKeyboardShortcutsHelp() {
        dijit.byId("keyboardShortcutsHelpDlg").show();
    }
/*]]>*/
</script>

<div id="keyboardShortcutsHelpDlg" dojoType="dijit.Dialog" title="${uiLabelMap.OpentapsKeyboardShortcuts}" class="tundra" style="position:absolute;display:none">
    <table class="shortcutHelp">
      <#assign thisScreenOnly = 0/>
      <#assign thisAppOnly = 0/>
      <#assign globalShortcut = 0/>
      <#list keyboardShortcuts as kb>
        <#if kb.screenName?has_content>
          <#if thisScreenOnly == 0>
            <tr><td colspan="2">Specific to this page</td></tr>
          </#if>
          <#assign thisScreenOnly = thisScreenOnly + 1 />
        <#else>
          <#if kb.applicationName?has_content>
            <#if thisAppOnly == 0>
              <tr><td colspan="2">Specific to this application:</td></tr>
            </#if>
            <#assign thisAppOnly = thisAppOnly + 1 />
          <#else>
            <#if globalShortcut == 0>
              <tr><td colspan="2">Global</td></tr>
            </#if>
            <#assign globalShortcut = globalShortcut + 1 />
          </#if>
        </#if>
        <tr><td class="shortcutKey">${kb.shortcut}</td><td class="shortcutDescription">${kb.description!}</td></tr>
      </#list>
    </table>
</div>

</#if>

