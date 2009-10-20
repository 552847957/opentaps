/*
 * Copyright (c) 2009 - 2009 Open Source Strategies, Inc.
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
 */
package org.opentaps.common.domain.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.search.FullTextQuery;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.domain.base.entities.PartyRole;
import org.opentaps.domain.base.entities.PartyRolePk;
import org.opentaps.domain.party.Lead;
import org.opentaps.domain.party.LeadSearchServiceInterface;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.domain.search.SearchService;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * The implementation of the Lead search service.
 */
public class LeadSearchService extends SearchService implements LeadSearchServiceInterface {

    private List<Lead> leads = null;

    /** {@inheritDoc} */
    public List<Lead> getLeads() {
        return leads;
    }

    /** {@inheritDoc} */
    public void makeQuery(StringBuilder sb) {
        PartySearch.makePersonQuery(sb, "PROSPECT");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public Set<Class> getClassesToQuery() {
        return PartySearch.PARTY_CLASSES;
    }

    /** {@inheritDoc} */
    public void search() throws ServiceException {
        StringBuilder sb = new StringBuilder();
        makeQuery(sb);
        searchInEntities(getClassesToQuery(), sb.toString());

        try {
            PartyRepositoryInterface partyRepository = getDomainsDirectory().getPartyDomain().getPartyRepository();
            leads = filterSearchResults(getResults(), partyRepository);
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<Lead> filterSearchResults(List<Object[]> results, PartyRepositoryInterface repository) throws ServiceException {

        // get the entities from the search results
        Set<String> leadIds = new HashSet<String>();
        int classIndex = getQueryProjectedFieldIndex(FullTextQuery.OBJECT_CLASS);
        int idIndex = getQueryProjectedFieldIndex(FullTextQuery.ID);
        if (classIndex < 0 || idIndex < 0) {
            throw new ServiceException("Incompatible result projection, classIndex = " + classIndex + ", idIndex = " + idIndex);
        }

        for (Object[] o : results) {
            Class c = (Class) o[classIndex];
            if (c.equals(PartyRole.class)) {
                PartyRolePk pk = (PartyRolePk) o[idIndex];
                if ("PROSPECT".equals(pk.getRoleTypeId())) {
                    leadIds.add(pk.getPartyId());
                }
            }
        }

        try {
            if (!leadIds.isEmpty()) {
                return repository.findList(Lead.class, EntityCondition.makeCondition(Lead.Fields.partyId.name(), EntityOperator.IN, leadIds));
            } else {
                return new ArrayList<Lead>();
            }
        } catch (GeneralException ex) {
            throw new ServiceException(ex);
        }
    }
}
