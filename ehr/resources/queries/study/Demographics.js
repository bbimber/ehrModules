/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

//include("/ehr/validation");

function repairRow(row, errors){
    EHR.validation.setSpecies(row, errors);
}

function setDescription(row, errors){
    //we need to set description for every field
    var description = new Array();

    return description;
}
