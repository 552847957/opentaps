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

<#--
 *  Copyright (c) 2003 The Open For Business Project - www.ofbiz.org
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a 
 *  copy of this software and associated documentation files (the "Software"), 
 *  to deal in the Software without restriction, including without limitation 
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 *  and/or sell copies of the Software, and to permit persons to whom the 
 *  Software is furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included 
 *  in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 *  OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY 
 *  CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT 
 *  OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 *  THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *@author     Andy Zeneski (jaz@ofbiz.org)
 *@author     Olivier Heintz (olivier.heintz@nereide.biz) 
 *@version    $Rev: 314 $
 *@since      2.1
-->
<#assign previousParams = sessionAttributes._PREVIOUS_PARAMS_?if_exists/>
<#if previousParams?has_content>
  <#assign previousParams = "?" + previousParams/>
</#if>

<#assign username = requestParameters.USERNAME?default((sessionAttributes.autoUserLogin.userLoginId)?default(""))>
<#if username != "">
  <#assign focusName = false/>
<#else/>
  <#assign focusName = true/>
</#if>

<#assign greetingLabel = opentapsApplicationName?default("opentaps")?cap_first + "LoginGreeting"/>
<#include "keyboard-shortcuts.ftl"/>
<div class="form" style="padding-top: 125px; padding-bottom: 125px;">

  <div align="center">
    <div class="screenlet" style="width: 300px; margin-left: auto; margin-right: auto;">
      <div class="screenlet-header" style="background-color: #000099; text-align: center;">
        <div class="boxhead">${uiLabelMap.get(greetingLabel)}</div>
      </div>
      <div class="screenlet-body" style="text-align: center;">
        <form method="post" action="<@ofbizUrl>login${previousParams?if_exists}</@ofbizUrl>" name="loginform" style="margin: 0;">
          <table width="100%" border="0" cellpadding="0" cellspacing="2">
            <tr>
              <td align="right">
                <span class="tabletext">${uiLabelMap.CommonUsername}&nbsp;</span>
              </td>
              <td align="left">
                <input type="text" class="inputBox" name="USERNAME" value="${username}" size="20"/>
              </td>
            </tr>
            <tr>
              <td align="right">
                <span class="tabletext">${uiLabelMap.CommonPassword}&nbsp;</span>
              </td>
              <td align="left">
                <input type="password" class="inputBox" name="PASSWORD" value="" size="20"/>
              </td>
            </tr>
            <tr>
              <td colspan="2" align="center">
                <input type="submit" value="${uiLabelMap.CommonLogin}" class="loginButton"/>
              </td>
            </tr>
          </table>
        </form>
      </div>
    </div>
  </div>

  <div align="center">
    <div class="screenlet" style="width: 300px; margin-left: auto; margin-right: auto; margin-top: 20px;">
      <div class="screenlet-header" style="background-color: #000099; text-align: center;">
        <div class="boxhead">${uiLabelMap.CommonForgotYourPassword}?</div>
      </div>
      <div class="screenlet-body" style="text-align: center;">
        <form method="post" action="<@ofbizUrl>forgotpassword${previousParams}</@ofbizUrl>" name="forgotpassword" style="margin: 0;">
          <span class="tabletext">${uiLabelMap.CommonUsername}&nbsp;</span><input type="text" size="20" class="inputBox" name="USERNAME" value="<#if requestParameters.USERNAME?has_content>${requestParameters.USERNAME}<#elseif autoUserLogin?has_content>${autoUserLogin.userLoginId}</#if>"/>
          <div style="margin-top: 3px;"><input type="submit" name="EMAIL_PASSWORD" class="loginButton" value="${uiLabelMap.CommonEmailPassword}"/></div>
        </form>
      </div>
    </div>
  </div>

  <script type="text/javascript">
  /*<![CDATA[*/
    <#if focusName>
      document.loginform.USERNAME.focus();
    <#else/>
      document.loginform.PASSWORD.focus();
    </#if>
  /*]]>*/
  </script>
</div>
