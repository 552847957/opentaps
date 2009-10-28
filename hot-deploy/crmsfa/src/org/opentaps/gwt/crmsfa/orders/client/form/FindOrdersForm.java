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
package org.opentaps.gwt.crmsfa.orders.client.form;

import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.layout.FormLayout;
import org.opentaps.gwt.common.client.UtilUi;
import org.opentaps.gwt.common.client.form.FindEntityForm;
import org.opentaps.gwt.common.client.form.base.SubFormPanel;
import org.opentaps.gwt.common.client.form.field.DateField;
import org.opentaps.gwt.common.client.listviews.OrderListView;
import org.opentaps.gwt.common.client.suggest.CustomerAutocomplete;
import org.opentaps.gwt.common.client.suggest.LotAutocomplete;
import org.opentaps.gwt.common.client.suggest.OrderStatusAutocomplete;
import org.opentaps.gwt.common.client.suggest.ProductStoreAutocomplete;

/**
 * Form class for find order in crmsfa.
 */
public class FindOrdersForm extends FindEntityForm<OrderListView> {

    private final SubFormPanel filterPanel;
    // Order Id
    private final TextField orderIdInput;
    // External ID
    private final TextField externalIdInput;
    // Order Name
    private final TextField orderNameInput;
    // Customer
    private final CustomerAutocomplete customerInput;
    // Lookup  Product Store
    private final ProductStoreAutocomplete productStoreInput;
    // Status
    private final OrderStatusAutocomplete orderStatusInput;
    // PO #
    private final TextField correspondingPoIdInput;
    // From Date
    private final DateField fromDateInput;
    // Thru Date
    private final DateField thruDateInput;
    // Created By
    private final TextField createdByInput;
    // Lot ID
    private final LotAutocomplete lotInput;
    // Serial Number
    private final TextField serialNumberInput;


    private final OrderListView orderListView;

    /**
     * Default constructor.
     */
    public FindOrdersForm() {
        this(true);
    }

    /**
     * Constructor with autoLoad parameter, use this constructor if some filters need to be set prior to loading the grid data.
     * @param autoLoad sets the grid autoLoad parameter, set to <code>false</code> if some filters need to be set prior to loading the grid data
     */
    public FindOrdersForm(boolean autoLoad) {
        super(UtilUi.MSG.crmFindOrders());

        // change the form dimensions to accommodate two columns
        setLabelLength(100);
        setInputLength(180);
        setFormWidth(675);

        orderIdInput = new TextField(UtilUi.MSG.orderOrderId(), "orderId", getInputLength());
        externalIdInput = new TextField(UtilUi.MSG.orderExternalId(), "externalId", getInputLength());
        orderNameInput = new TextField(UtilUi.MSG.orderOrderName(), "orderName", getInputLength());
        customerInput = new CustomerAutocomplete(UtilUi.MSG.crmCustomer(), "partyIdSearch", getInputLength());
        productStoreInput = new ProductStoreAutocomplete(UtilUi.MSG.orderProductStore(), "productStoreId", getInputLength());
        orderStatusInput = new OrderStatusAutocomplete(UtilUi.MSG.commonStatus(), "statusId", getInputLength());
        correspondingPoIdInput = new TextField(UtilUi.MSG.opentapsPONumber(), "correspondingPoId", getInputLength());
        fromDateInput = new DateField(UtilUi.MSG.commonFromDate(), "fromDate", getInputLength());
        thruDateInput = new DateField(UtilUi.MSG.commonThruDate(), "thruDate", getInputLength());
        createdByInput = new TextField(UtilUi.MSG.commonCreatedBy(), "createdBy", getInputLength());
        lotInput = new LotAutocomplete(UtilUi.MSG.productLotId(), "lotId", getInputLength());
        serialNumberInput = new TextField(UtilUi.MSG.productSerialNumber(), "serialNumber", getInputLength());

        // Build the filter tab
        filterPanel = getMainForm().addTab(UtilUi.MSG.crmFindOrders());
        // hide the tab bar since we only use one tab
        getMainForm().hideTabBar();

        Panel mainPanel = new Panel();
        mainPanel.setLayout(new ColumnLayout());

        Panel columnOnePanel = new Panel();
        columnOnePanel.setLayout(new FormLayout());
        Panel columnTwoPanel = new Panel();
        columnTwoPanel.setLayout(new FormLayout());

        mainPanel.add(columnOnePanel, new ColumnLayoutData(.5));
        mainPanel.add(columnTwoPanel, new ColumnLayoutData(.5));

        columnOnePanel.add(orderIdInput);
        columnTwoPanel.add(externalIdInput);

        columnOnePanel.add(orderNameInput);
        columnTwoPanel.add(customerInput);

        columnOnePanel.add(productStoreInput);
        columnTwoPanel.add(orderStatusInput);

        columnOnePanel.add(correspondingPoIdInput);
        columnTwoPanel.add(createdByInput);

        columnOnePanel.add(fromDateInput);
        columnTwoPanel.add(thruDateInput);

        columnOnePanel.add(lotInput);
        columnTwoPanel.add(serialNumberInput);

        filterPanel.add(mainPanel);

        orderListView = new OrderListView();
        orderListView.setAutoLoad(autoLoad);
        orderListView.init();
        addListView(orderListView);
    }

    @Override protected void filter() {
        getListView().clearFilters();
        getListView().filterByOrderId(orderIdInput.getText());
        getListView().filterByExternalId(externalIdInput.getText());
        getListView().filterByOrderName(orderNameInput.getText());
        getListView().filterByCustomerId(customerInput.getText());
        getListView().filterByProductStoreId(productStoreInput.getText());
        getListView().filterByStatusId(orderStatusInput.getText());
        getListView().filterByCorrespondingPoId(correspondingPoIdInput.getText());
        getListView().filterByFromDate(fromDateInput.getText());
        getListView().filterByThruDate(thruDateInput.getText());
        getListView().filterByCreatedBy(createdByInput.getText());
        getListView().filterByLotId(lotInput.getText());
        getListView().filterBySerialNumber(serialNumberInput.getText());
        getListView().applyFilters();
    }

}
