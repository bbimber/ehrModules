/*
 * Copyright (c) 2010-2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

require("ehr/triggers").initScript(this);

function onInit(event, context){
    context.extraContext.removeTimeFromDate = true;
}

function setDescription(row, errors){
    //we need to set description for every field
    var description = new Array();

    if (row.source)
        description.push('Source: '+EHR.Server.Validation.snomedToString(row.source,  row.sourceMeaning));
    if (row.method)
        description.push('Method: '+row.method);

    if (row.organism)
        description.push('Organism: '+EHR.Server.Validation.snomedToString(row.organism,  row.resultMeaning));
    if(row.result)
        description.push('Result: '+EHR.Server.Validation.nullToString(row.result)+' '+EHR.Server.Validation.nullToString(row.units));
    if(row.qualResult)
        description.push('Qual Result: '+EHR.Server.Validation.nullToString(row.qualResult));

    if (row.antibiotic)
        description.push('Antibiotic: '+EHR.Server.Validation.snomedToString(row.antibiotic, row.antibioticMeaning));

    if (row.sensitivity)
        description.push('Sensitivity: ' + row.sensitivity);

    return description;
}

function onUpsert(context, errors, row, oldRow){
    if (row.sensitivity && row.antibiotic == null){
        EHR.Server.Validation.addError(errors, 'sensitivity', "Must provide an antibiotic to go with sensitivity", 'WARN');
    }
}
