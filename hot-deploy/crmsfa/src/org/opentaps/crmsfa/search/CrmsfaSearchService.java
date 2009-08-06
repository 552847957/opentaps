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
package org.opentaps.crmsfa.search;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.opentaps.domain.base.entities.SalesOpportunity;
import org.opentaps.domain.order.OrderDomainInterface;
import org.opentaps.domain.order.OrderRepositoryInterface;
import org.opentaps.domain.order.SalesOpportunitySearchServiceInterface;
import org.opentaps.domain.party.Account;
import org.opentaps.domain.party.AccountSearchServiceInterface;
import org.opentaps.domain.party.Contact;
import org.opentaps.domain.party.ContactSearchServiceInterface;
import org.opentaps.domain.party.Lead;
import org.opentaps.domain.party.LeadSearchServiceInterface;
import org.opentaps.domain.party.PartyDomainInterface;
import org.opentaps.domain.party.PartyRepositoryInterface;
import org.opentaps.domain.search.SearchService;
import org.opentaps.foundation.repository.RepositoryException;
import org.opentaps.foundation.service.ServiceException;

/**
 * The implementation of the Crmsfa search service.
 * This class does not actually implement any of the search logic, those are implemented in the specific domain
 *  search service eg: <code>AccountSearchService</code>, ...
 */
public class CrmsfaSearchService extends SearchService {

    private boolean searchAccounts = false;
    private boolean searchContacts = false;
    private boolean searchLeads = false;
    private boolean searchSalesOpportunities = false;

    private List<Contact> contacts = null;
    private List<Account> accounts = null;
    private List<Lead> leads = null;
    private List<SalesOpportunity> salesOpportunities = null;

    private OrderRepositoryInterface orderRepository;
    private PartyRepositoryInterface partyRepository;
    private AccountSearchServiceInterface accountSearch;
    private ContactSearchServiceInterface contactSearch;
    private LeadSearchServiceInterface leadSearch;
    private SalesOpportunitySearchServiceInterface salesOpportunitySearch;

    /**
     * Option to return Accounts from a search.
     * @param option a <code>boolean</code> value
     */
    public void setSearchAccounts(boolean option) {
        this.searchAccounts = option;
    }

    /**
     * Option to return Contacts from a search.
     * @param option a <code>boolean</code> value
     */
    public void setSearchContacts(boolean option) {
        this.searchContacts = option;
    }

    /**
     * Option to return Leads from a search.
     * @param option a <code>boolean</code> value
     */
    public void setSearchLeads(boolean option) {
        this.searchLeads = option;
    }

    /**
     * Option to return Sales Opportunities from a search.
     * @param option a <code>boolean</code> value
     */
    public void setSearchSalesOpportunities(boolean option) {
        this.searchSalesOpportunities = option;
    }

    /** {@inheritDoc} */
    public void search() throws ServiceException {
        try {
            OrderDomainInterface orderDomain = getDomainsDirectory().getOrderDomain();
            PartyDomainInterface partyDomain = getDomainsDirectory().getPartyDomain();
            orderRepository = orderDomain.getOrderRepository();
            partyRepository = partyDomain.getPartyRepository();
            accountSearch = partyDomain.getAccountSearchService();
            contactSearch = partyDomain.getContactSearchService();
            leadSearch = partyDomain.getLeadSearchService();
            salesOpportunitySearch = orderDomain.getSalesOpportunitySearchService();
            // make query
            searchInEntities(makeEntityClassList(), makeQuery());
            // note: the filterSearchResults methods expect getResults to return a list of Object[] where the first two fields are {OBJECT_CLASS, ID}
            accounts = accountSearch.filterSearchResults(getResults(), partyRepository);
            contacts = contactSearch.filterSearchResults(getResults(), partyRepository);
            leads = leadSearch.filterSearchResults(getResults(), partyRepository);
            salesOpportunities = salesOpportunitySearch.filterSearchResults(getResults(), orderRepository);
        } catch (RepositoryException e) {
            throw new ServiceException(e);
        }
    }

    /** {@inheritDoc} */
    @Override public Set<String> getQueryProjectedFields() {
        Set<String> fields = new LinkedHashSet<String>();
        fields.addAll(accountSearch.getQueryProjectedFields());
        fields.addAll(contactSearch.getQueryProjectedFields());
        fields.addAll(leadSearch.getQueryProjectedFields());
        fields.addAll(salesOpportunitySearch.getQueryProjectedFields());
        return fields;
    }

    /**
     * Gets the accounts results.
     * @return the <code>List</code> of <code>Account</code>
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Gets the contacts results.
     * @return the <code>List</code> of <code>Contact</code>
     */
    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * Gets the leads results.
     * @return the <code>List</code> of <code>Lead</code>
     */
    public List<Lead> getLeads() {
        return leads;
    }

    /**
     * Gets the sales opportunities results.
     * @return the <code>List</code> of <code>SalesOpportunity</code>
     */
    public List<SalesOpportunity> getSalesOpportunities() {
        return salesOpportunities;
    }

    /**
     * Builds the Lucene query according to the options set and the user given keywords.
     * @return a Lucene query string
     * @throws ServiceException if no search option is set
     */
    private String makeQuery() throws ServiceException {

        StringBuilder sb = new StringBuilder();

        if (searchAccounts) {
            accountSearch.makeQuery(sb);
        }
        if (searchContacts) {
            contactSearch.makeQuery(sb);
        }
        if (searchLeads) {
            leadSearch.makeQuery(sb);
        }

        if (searchSalesOpportunities) {
            salesOpportunitySearch.makeQuery(sb);
        }

        if (sb.length() == 0) {
            throw new ServiceException("Cannot perform search, no search option was set");
        }

        return makeQueryString(sb.toString(), getKeywords());
    }

    /**
     * Builds the list of entity classes that should be searched according to the options set.
     * @return a <code>List</code> of entity classes
     * @throws ServiceException if no search option is set
     */
    @SuppressWarnings("unchecked")
    private Set<Class> makeEntityClassList() throws ServiceException {
        Set<Class> classes = new HashSet<Class>();
        if (searchAccounts) {
            classes.addAll(accountSearch.getClassesToQuery());
        }
        if (searchContacts) {
            classes.addAll(contactSearch.getClassesToQuery());
        }
        if (searchLeads) {
            classes.addAll(leadSearch.getClassesToQuery());
        }
        if (searchSalesOpportunities) {
            classes.addAll(salesOpportunitySearch.getClassesToQuery());
        }

        if (classes.isEmpty()) {
            throw new ServiceException("Cannot perform search, no search option was set");
        }

        return classes;
    }
}
