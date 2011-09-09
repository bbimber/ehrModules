/*
 * Copyright (c) 2011 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

var console = require("console");
//var LABKEY = require("labkey");
//var Ext = require("Ext").Ext;


function beforeInsert(row, errors){
    beforeBoth(errors, row);
}

function beforeUpdate(row, oldRow, errors){
    beforeBoth(errors, row, oldRow);
}


function beforeBoth(errors, row, oldRow){
    if(row.protocol)
        row.protocol = row.protocol.toLowerCase();
}

//function beforeDelete(errors, row, oldRow){
//
//}