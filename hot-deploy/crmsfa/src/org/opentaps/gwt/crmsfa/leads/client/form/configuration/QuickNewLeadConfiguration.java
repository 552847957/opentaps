/*
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
 */

package org.opentaps.gwt.crmsfa.leads.client.form.configuration;

/**
 * Defines the interface between the server and client for the QuickNewLeadService
 * Technically not a java interface, but it defines all the constantes needed on both sides
 *  which makes the code more robust.
 */
public abstract class QuickNewLeadConfiguration {

    private QuickNewLeadConfiguration() { }

    public static final String URL = "/crmsfa/control/gwtQuickNewLead";

    public static final String IN_COMPANY_NAME = "companyName";
    public static final String IN_FIRST_NAME = "firstName";
    public static final String IN_LAST_NAME = "lastName";
    public static final String IN_PHONE_COUNTRY_CODE = "primaryPhoneCountryCode";
    public static final String IN_PHONE_AREA_CODE = "primaryPhoneAreaCode";
    public static final String IN_PHONE_NUMBER = "primaryPhoneNumber";
    public static final String IN_EMAIL_ADDRESS = "primaryEmail";

    public static final String OUT_LEAD_PARTY_ID = "partyId";

}
