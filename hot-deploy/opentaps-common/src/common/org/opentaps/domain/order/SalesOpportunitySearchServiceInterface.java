/*
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
 */
package org.opentaps.domain.order;

import java.util.List;
import java.util.Set;

import org.opentaps.domain.base.entities.SalesOpportunity;
import org.opentaps.domain.search.SearchServiceInterface;
import org.opentaps.foundation.service.ServiceException;

/**
 * This is the interface of the Sales Opportunity search service.
 */
public interface SalesOpportunitySearchServiceInterface extends SearchServiceInterface {

    /**
     * Gets the sales opportunities results.
     * @return the <code>List</code> of <code>SalesOpportunity</code>
     */
    public List<SalesOpportunity> getSalesOpportunities();

    /**
     * Builds part of the query to search for <code>SalesOpportunities</code> according to the set options.
     * @param sb the current <code>StringBuilder</code> instance
     */
    public void makeQuery(StringBuilder sb);

    /**
     * Filters the results of the search service to get the list of matching <code>SalesOpportunity</code>.
     * @param results the list of results from the search service, it must be a list of <code>Object[]</code>, from the projection <code>{OBJECT_CLASS, ID}</code>
     * @param repository a <code>OrderRepositoryInterface</code> instance
     * @return the list of <code>SalesOpportunity</code> found from the results
     * @throws ServiceException if an error occurs
     */
    public List<SalesOpportunity> filterSearchResults(List<Object[]> results, OrderRepositoryInterface repository) throws ServiceException;

    /**
     * Gets the <code>Set</code> of <code>Class</code> to query.
     * @return the <code>Set</code> of <code>Class</code> to query
     */
    @SuppressWarnings("unchecked")
    public Set<Class> getClassesToQuery();

}
