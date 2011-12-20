/*
 * Copyright (c) 2010-2011 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

var {EHR, LABKEY, Ext, console, init, beforeInsert, afterInsert, beforeUpdate, afterUpdate, beforeDelete, afterDelete, complete} = require("ehr/validation");

/*
function onComplete(event, errors, scriptContext){
    //NOTE: we will stop caching this in demographics
    if(scriptContext.publicParticipantsModified.length){
        //find the most recent TB date per participant
        var toUpdate = [];
        var idsFound = [];
        LABKEY.Query.executeSql({
            schemaName: 'study',
            scope: this,
            sql: 'SELECT a.Id, max(a.date) as maxDate FROM study.tb a WHERE a.id IN (\''+scriptContext.publicParticipantsModified.join(',')+'\') AND a.qcstate.publicdata=TRUE GROUP BY a.id',
            success: function(data){
                if(data.rows && data.rows.length){
                    var row;
                    for (var i=0;i<data.rows.length;i++){
                        row = data.rows[i];
console.log(row)
                        idsFound.push(row.Id);
                        EHR.Server.Validation.findDemographics({
                            participant: row.Id,
                            scriptContext: scriptContext,
                            forceRefresh: true,
                            scope: this,
                            callback: function(data){
                                if(data){
                                    if(row.maxDate != data.tbdate)
                                        toUpdate.push({tbdate: row.maxDate, Id: row.Id, lsid: data.lsid});
                                }
                            }
                        });
                    }
                }
            },
            failure: EHR.Server.Utils.onFailure
        });

        if(toUpdate.length != scriptContext.publicParticipantsModified.length){
            Ext.each(scriptContext.publicParticipantsModified, function(p){
                if(idsFound.indexOf(p) == -1){
                    EHR.Server.Validation.findDemographics({
                        participant: p,
                        scriptContext: scriptContext,
                        forceRefresh: true,
                        scope: this,
                        callback: function(data){
                            if(data){
                                toUpdate.push({tbdate: null, Id: data.Id, lsid: data.lsid});
                            }
                        }
                    });
                }
            }, this);
        }

        if(toUpdate.length){
            LABKEY.Query.updateRows({
                schemaName: 'study',
                queryName: 'demographics',
                rows: toUpdate,
                extraContext: {
                    schemaName: 'study',
                    queryName: 'Demographics'
                },
                success: function(data){
                    console.log('Success updating demographics for TB')
                },
                failure: EHR.Server.Utils.onFailure
            });
        }
    }
};
*/

function onETL(row, errors){
//NOTE: hyphen means 'not going to perform'
//    if (row.result1 == '-') row.result1 = 0;
//    if (row.result2 == '-') row.result2 = 0;
//    if (row.result3 == '-') row.result3 = 0;

    if (row.result1 == '+') row.result1 = 5;
    if (row.result2 == '+') row.result2 = 5;
    if (row.result3 == '+') row.result3 = 5;
}

function setDescription(row, errors){
    //we need to set description for every field
    var description = new Array();

    if(row.eye)
        description.push('Eye: '+row.eye);
    if(row.lot)
        description.push('Lot: '+row.lot);
    if(row.dilution)
        description.push('Dilution: '+row.dilution);
    if(typeof(row.result1)!==undefined)
        description.push('24H: '+row.result1);
    if(typeof(row.result2)!==undefined)
        description.push('48H: '+row.result2);
    if(typeof(row.result3)!==undefined)
        description.push('72H: '+row.result3);

    return description;
}

function onUpsert(context, errors, row, oldRow){
    if(!row.notPerformedAtCenter && (row.result1=='' || row.result2=='' || row.result3=='' || row.result1==null || row.result2==null || row.result3==null)){
        row.missingResults = true;
    }
    else {
        row.missingResults = false
    }

    if(context.extraContext.dataSource != 'etl')
        EHR.Server.Validation.removeTimeFromDate(row, errors);
}


