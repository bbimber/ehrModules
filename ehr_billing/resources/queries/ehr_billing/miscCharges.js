/*
 * Copyright (c) 2018-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

require("ehr/triggers").initScript(this);

var LABKEY = require("labkey");
var billingHelper = new org.labkey.ehr_billing.query.EHRBillingTriggerHelper(LABKEY.Security.currentUser.id, LABKEY.Security.currentContainer.id);

var CHARGEABLE_ITEMS_ROWID_NAME_MAP = {};
var chargeableItems = LABKEY.Query.selectRows({
    schemaName: 'ehr_billing_public',
    queryName: 'chargeableItems',
    columns: 'rowid, name, active',
    filterArray: [LABKEY.Filter.create('active', true, LABKEY.Filter.Types.EQUAL)],
    scope: this,
    success: function(results) {
        var rows = results.rows;
        for(var index = 0; index < rows.length; index++) {
            CHARGEABLE_ITEMS_ROWID_NAME_MAP[rows[index].name] = rows[index].rowid;
        }
    },
    failure: function(results) {
        console.log("Error getting ehr_billing_public.chargeableItems");
    }
});

function onInit(event, helper){
    helper.setScriptOptions({
        allowAnyId: false,
        allowDeadIds: true,
        allowDatesInDistantPast: true
    });
}

function onUpsert(helper, scriptErrors, row){
    row.objectid = row.objectid || LABKEY.Utils.generateUUID().toUpperCase();

    if(row.chargeId == undefined || row.chargeId == null) {

        if (CHARGEABLE_ITEMS_ROWID_NAME_MAP[row.name]) {
            row.chargeId = CHARGEABLE_ITEMS_ROWID_NAME_MAP[row.name];
        }
    }
}

EHR.Server.TriggerManager.registerHandlerForQuery(EHR.Server.TriggerManager.Events.ON_BECOME_PUBLIC, 'ehr_billing', 'miscCharges', function(scriptErrors, helper, row, oldRow){

    if (!helper.isETL() && row) {
        //force this date to match the current date
        row.billingDate = new Date();
    }
});

EHR.Server.TriggerManager.registerHandlerForQuery(EHR.Server.TriggerManager.Events.BEFORE_DELETE, 'ehr_billing', 'miscCharges', function(helper, errors, row){

    if (!helper.isETL() && row && row.invoiceId) {
        //NOTE: once this item has been billed (ie. invoiceId is not null), fewer people can edit/delete it
        errors.Id = errors.Id || [];
        errors.Id.push('Error: you cannot delete misc charges once they have been invoiced');
    }
});