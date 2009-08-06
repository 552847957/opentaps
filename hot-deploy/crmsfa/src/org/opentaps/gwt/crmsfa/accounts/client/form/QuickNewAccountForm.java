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

package org.opentaps.gwt.crmsfa.accounts.client.form;

import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.VType;
import org.opentaps.gwt.common.client.UtilUi;
import org.opentaps.gwt.common.client.form.base.ScreenletFormPanel;
import org.opentaps.gwt.common.client.form.field.PhoneNumberField;
import org.opentaps.gwt.common.client.security.Permission;
import org.opentaps.gwt.crmsfa.accounts.client.form.configuration.QuickNewAccountConfiguration;

/**
 * Form for quick creation of new accounts, only providing a few important fields.
 */
public class QuickNewAccountForm extends ScreenletFormPanel {

    private TextField accountNameInput;
    private TextField emailInput;
    private PhoneNumberField phoneInput;

    private static final Integer INPUT_LENGTH = 135;

    /**
     * Constructor.
     */
    public QuickNewAccountForm() {

        super(Position.TOP, UtilUi.MSG.createAccount());

        if (!Permission.hasPermission(Permission.CRMSFA_ACCOUNT_CREATE)) {
            return;
        }

        setUrl(QuickNewAccountConfiguration.URL);     // this sets the action of the form
        accountNameInput = new TextField(UtilUi.MSG.accountName(), QuickNewAccountConfiguration.IN_ACCOUNT_NAME, INPUT_LENGTH);
        addRequiredField(accountNameInput);

        phoneInput = new PhoneNumberField(UtilUi.MSG.phoneNumber(), QuickNewAccountConfiguration.IN_PHONE_COUNTRY_CODE, QuickNewAccountConfiguration.IN_PHONE_AREA_CODE, QuickNewAccountConfiguration.IN_PHONE_NUMBER, INPUT_LENGTH);
        addField(phoneInput);

        emailInput = new TextField(UtilUi.MSG.emailAddress(), QuickNewAccountConfiguration.IN_EMAIL_ADDRESS, INPUT_LENGTH);
        emailInput.setVtype(VType.EMAIL);
        addField(emailInput);

        addStandardSubmitButton(UtilUi.MSG.createAccount());
    }

}
