package org.opentaps.tests.common;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.common.query.Query;
import org.opentaps.common.query.QueryFactory;
import org.opentaps.tests.OpentapsTestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
* Copyright (c) 2008 - 2009 Open Source Strategies, Inc.
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
*/
public class QueryTests extends OpentapsTestCase {

    protected List<String> targetIds = new ArrayList<String>(); // a List of IDs we expect to find in the system
    protected String basicQuery =  "SELECT * FROM TESTING_NODE WHERE TESTING_NODE_ID LIKE 'TEST2%'";  // a basic SQL query
    protected String parameterizedQuery = "SELECT * FROM TESTING_NODE WHERE TESTING_NODE_ID LIKE ?"; // a parameterized SQL query
    protected String ENTITY_NAME = "TestingNode";   // this is the delegator entity we will be working with

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // just to be sure -- if test crashed somehow last time, these might still be hanging around
        removeTestingRecords(delegator);

        // create some records for testing
        for (int i = 1; i < 101; i++) {
            String testId = makeTestId(i);
            delegator.create(ENTITY_NAME, UtilMisc.toMap("testingNodeId", testId, "description", "Test Entity " + i));
            // we want TEST2 and TEST20 through TEST29 in list of targetIds
            if ((i == 2) || (i > 19) && (i < 30)) {
                targetIds.add(testId);
            }
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        // delegator is reset to null by super.tearDown() so we have to get it again
        removeTestingRecords(GenericDelegator.getGenericDelegator(OpentapsTestCase.DELEGATOR_NAME));
    }

    private String makeTestId(int i) {
        return "TEST" + i;
    }

    private void removeTestingRecords(GenericDelegator delegator) throws GenericEntityException {
        delegator.removeByCondition("TestingNode", new EntityExpr("testingNodeId", EntityOperator.LIKE, "TEST%"));
    }

    public void testSelectToList() throws Exception {
        // run the query
        QueryFactory qf = new QueryFactory(delegator);
        Query q = qf.createQuery(basicQuery);
        List<Map<String, Object>> rows = q.list();

        // Check the query returned right number of elements
        assertEquals("Query [" + basicQuery + "] returned right number of rows", new Integer(11), new Integer(rows.size()));

        // now check the rows to make sure the right IDs are present
        // we loop through and remove each ID from the query's rows from the a List of expected IDs, so in the end there should be none.
        List<String> expectedIds = new ArrayList<String>();
        expectedIds.addAll(targetIds);
        for (Map<String, Object> row:rows) {
            // this also tests that the SQL TESTING_NODE_ID was correctly converted to testingNodeId
            if (expectedIds.contains((String) row.get("testingNodeId"))) {
                expectedIds.remove((String) row.get("testingNodeId"));
            }
        }
        assertEquals("Query [" + basicQuery + "] returned the right testingNodeIds", 0, expectedIds.size());
    }

    public void testSelectToEntitiesList() throws Exception {
        // run the query
        QueryFactory qf = new QueryFactory(delegator);
        Query q = qf.createQuery(basicQuery);
        List<GenericValue> rows = q.entitiesList(ENTITY_NAME);

        // Check the query returned right number of elements
        assertEquals("Query [" + basicQuery + "] returned right number of rows", 11, rows.size());

        // now check the rows to make sure the right IDs are present
        // we loop through and remove each ID from the query's rows from the a List of expected IDs, so in the end there should be none.
        List<String> expectedIds = new ArrayList<String>();
        expectedIds.addAll(targetIds);
        for (GenericValue row:rows) {
            assertEquals("Row [" + row + "] returned the correct GenericValue", ENTITY_NAME, row.getModelEntity().getEntityName());
            if (expectedIds.contains(row.getString("testingNodeId"))) {
                expectedIds.remove(row.getString("testingNodeId"));
            }
        }
        assertEquals("Query [" + basicQuery + "] returned the right testingNodeIds", 0, expectedIds.size());
    }

    public void testParameterizedQuery() throws Exception {
        QueryFactory qf = new QueryFactory(delegator);
        Query q = qf.createQuery(parameterizedQuery);

        q.setString(1, "TEST2%");

        List<Map<String, Object>> r1 = q.list();

        // Check the query returned right number of elements
        assertEquals("Parameterized query [" + basicQuery + "] returned right number of rows", new Integer(11), new Integer(r1.size()));

        // now check the rows to make sure the right IDs are present
        // we loop through and remove each ID from the query's rows from the a List of expected IDs, so in the end there should be none.
        List<String> expectedIds = new ArrayList<String>();
        expectedIds.addAll(targetIds);
        for (Map<String, Object> row : r1) {
            // this also tests that the SQL TESTING_NODE_ID was correctly converted to testingNodeId
            if (expectedIds.contains((String) row.get("testingNodeId"))) {
                expectedIds.remove((String) row.get("testingNodeId"));
            }
        }
        assertEquals("Parameterized query [" + basicQuery + "] returned the right testingNodeIds", 0, expectedIds.size());

        q.clearQueryResults();

        q.setString(1, "TEST2%");

        List<GenericValue> r2 = q.entitiesList(ENTITY_NAME);
        // Check the query returned right number of elements
        assertEquals("Parameterized query [" + basicQuery + "] returned right number of rows", 11, r2.size());

        // now check the rows to make sure the right IDs are present
        // we loop through and remove each ID from the query's rows from the a List of expected IDs, so in the end there should be none.
        expectedIds = new ArrayList<String>();
        expectedIds.addAll(targetIds);
        for (GenericValue row : r2) {
            assertEquals("Row [" + row + "] returned the correct GenericValue", ENTITY_NAME, row.getModelEntity().getEntityName());
            if (expectedIds.contains(row.getString("testingNodeId"))) {
                expectedIds.remove(row.getString("testingNodeId"));
            }
        }
        assertEquals("Parameterized query [" + basicQuery + "] returned the right testingNodeIds", 0, expectedIds.size());
    }
    
}
