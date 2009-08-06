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
 *  
-->
<#if acctgTrans?exists>
    <div class="screenlet-header">
        <div style="float: right;"><#t/>
            <#if accountingTransaction.isPosted()><#t/>
                <a class="buttontext" href="reverseAcctgTrans?acctgTransId=${acctgTrans.acctgTransId}">${uiLabelMap.FinancialsReverseTransaction}</a><#t/>
            <#else><#t/>
                <a class="buttontext" href="updateAcctgTransForm?acctgTransId=${acctgTrans.acctgTransId}">${uiLabelMap.CommonEdit}</a><#t/>
                <#if canDeleteTrans><a class="buttontext" href="deleteAcctgTrans?acctgTransId=${acctgTrans.acctgTransId}">${uiLabelMap.CommonDelete}</a></#if><#t/>          
                <#if accountingTransaction.canPost()><a class="buttontext" href="postAcctgTrans?acctgTransId=${acctgTrans.acctgTransId}">${uiLabelMap.AccountingPostTransaction}</a></#if><#t>
            </#if><#t/>
        </div><#t/>
        <div class="boxhead">
            ${uiLabelMap.FinancialsTransaction}
        </div>
    </div>
</#if>
