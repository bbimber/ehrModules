/*
 * Copyright (c) 2010-2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

require("ehr/triggers").initScript(this);

function setDescription(row, errors){
    //we need to set description for every field
    var description = new Array();

    if(row.performedby)
        description.push('Entered By: ' + EHR.Server.Validation.nullToString(row.performedby));

    return description;
}