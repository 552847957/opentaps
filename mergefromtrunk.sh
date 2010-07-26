#!/bin/sh
#####################################################################
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#####################################################################
set -e
prevRev=`expr $1 - 1`
svn merge -r $prevRev:$1 https://svn.apache.org/repos/asf/ofbiz/trunk 
trunkLog=runtime/trunkLog.xml
touch ${trunkLog}
svn log --xml https://svn.apache.org/repos/asf/ofbiz/trunk -r $1> ${trunkLog}
releaseBranchMessage="Applied fix from trunk for revision: $1 \n"
trunkMessage=`grep -e '<msg>' ${trunkLog} | sed 's/<msg>//' | sed 's/<\/msg>//'` 
rm -rf ${trunkLog}
svn commit -m "`echo ${releaseBranchMessage} ${trunkMessage}`"

