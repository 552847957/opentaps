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
package org.opentaps.common.domain.party;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.search.FullTextQuery;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.opentaps.domain.base.entities.PartyGroup;
import org.opentaps.domain.base.entities.PartyRole;
import org.opentaps.domain.base.entities.PartyRolePk;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.domain.party.SupplierSearchServiceInterface;
import org.opentaps.domain.search.SearchService;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * The implementation of the Account search service.
 */
public class SupplierSearchService extends SearchService implements SupplierSearchServiceInterface {

    private List<PartyGroup> suppliers = null;

    /** {@inheritDoc} */
    public List<PartyGroup> getSuppliers() {
        return suppliers;
    }

    /** {@inheritDoc} */
    public void makeQuery(StringBuilder sb) {
        PartySearch.makePartyGroupQuery(sb, "SUPPLIER");
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
            suppliers = filterSearchResults(getResults(), partyRepository);
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public List<PartyGroup> filterSearchResults(List<Object[]> results, PartyRepositoryInterface repository) throws ServiceException {

        // get the entities from the search results
        Set<String> supplierIds = new HashSet<String>();
        int classIndex = getQueryProjectedFieldIndex(FullTextQuery.OBJECT_CLASS);
        int idIndex = getQueryProjectedFieldIndex(FullTextQuery.ID);
        if (classIndex < 0 || idIndex < 0) {
            throw new ServiceException("Incompatible result projection, classIndex = " + classIndex + ", idIndex = " + idIndex);
        }

        for (Object[] o : results) {
            Class c = (Class) o[classIndex];
            if (c.equals(PartyRole.class)) {
                PartyRolePk pk = (PartyRolePk) o[idIndex];
                if ("SUPPLIER".equals(pk.getRoleTypeId())) {
                    supplierIds.add(pk.getPartyId());
                }
            }
        }

        try {
            if (!supplierIds.isEmpty()) {
                return repository.findList(PartyGroup.class, new EntityExpr(PartyGroup.Fields.partyId.name(), EntityOperator.IN, supplierIds));
            } else {
                return new ArrayList<PartyGroup>();
            }
        } catch (GeneralException ex) {
            throw new ServiceException(ex);
        }
    }
}
