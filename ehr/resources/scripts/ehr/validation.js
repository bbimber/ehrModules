/*
 * Copyright (c) 2012-2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

var console = require("console");
var LABKEY = require("labkey");

var EHR = {};
exports.EHR = EHR;

EHR.Server = {};
EHR.Server.Utils = require("ehr/utils").EHR.Server.Utils;
EHR.Server.Security = require("ehr/security").EHR.Server.Security;

/**
 * This class contains static methods used for server-side validation of incoming data.
 * @class
 */
EHR.Server.Validation = {
    /**
     * A helper that adds an error if restraint is used, but no time is entered
     * @param row The row object
     * @param errors The errors object
     */
    checkRestraint: function(row, errors){
        if(row.restraint && !LABKEY.ExtAdapter.isDefined(row.restraintDuration))
            EHR.Server.Validation.addError(errors, 'restraintDuration', 'Must enter time restrained', 'INFO');

    },
    /**
     * A helper used to process errors generated internally in EHR.Server.Triggers into the errors object returned to LabKey.  Primarily used internally by rowEnd()
     * @param row The row object, provided by LabKey
     * @param errors The errors object, provided by LabKey
     * @param scriptErrors The errors object passed internally in rowEnd()
     * @param errorThreshold Any errors below this threshold will be discarded.  Should match a value from EHR.Server.Validation.errorSeverity
     * @param extraContext The extraContext object provided by LabKey.
     */
    processErrors: function(row, errors, scriptErrors, errorThreshold, scriptContext){
        var error;
        var totalErrors = 0;

        //extraContext will be roundtripped  back to the client
        if(!scriptContext.extraContext.skippedErrors){
            scriptContext.extraContext.skippedErrors = {};
        }

        for(var i in scriptErrors){
            for(var j=0;j<scriptErrors[i].length;j++){
                error = scriptErrors[i][j];

                if (!EHR.Server.Validation.shouldIncludeError(error.severity, errorThreshold, scriptContext)){
                    console.log('error below threshold');
                    if(row._recordid){
                        if(!scriptContext.extraContext.skippedErrors[row._recordid])
                            scriptContext.extraContext.skippedErrors[row._recordid] = [];

                        error.field = i;
                        scriptContext.extraContext.skippedErrors[row._recordid].push(error);
                    }
                    else {
                        console.log('No _recordId provided');
                        console.log(row);
                    }
                    continue;
                }

                if(!errors[i])
                    errors[i] = {};

                errors[i].push(error.severity+': '+error.message);
                totalErrors++;
            }
        }

        return totalErrors;
    },
    /**
     * Assigns numeric values to error severity strings
     * @private
     */
    errorSeverity: {
        DEBUG: 0,
        INFO: 1,
        WARN: 2,
        ERROR: 3,
        FATAL: 4
    },

    shouldIncludeError: function(error, threshold, context){
        if ((context && context.extraContext && context.extraContext.isValidateOnly) || !threshold)
            return true;

        return EHR.Server.Validation.errorSeverity[error] > EHR.Server.Validation.errorSeverity[threshold];
    },

    /**
     * A helper to remove the time-portion of a datetime field.
     * @param row The row object
     * @param errors The errors object
     * @param fieldname The name of the field from which to remove the time portion
     */
    removeTimeFromDate: function(row, errors, fieldname){
        fieldname = fieldname || 'date';
        var date = row[fieldname];

        if(!date){
            return;
        }

        //normalize to a javascript date object
        date = new Date(date.getTime());
        row[fieldname] = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    },
    /**
     * A helper to return a display string based on a SNOMED code.  It will normally display the meaning of the code, followed by the code in parenthesis.
     * @param code The SNOMED code
     * @param meaning The meaning.  This is optional and will be queried if not present.  However, if the incoming row has the meaning, this saves overhead.
     */
    snomedToString: function (code, meaning){
        if(!meaning){
            console.log('QUERYING SNOMED TERM');
            LABKEY.Query.selectRows({
                schemaName: 'ehr_lookups',
                queryName: 'snomed',
                filterArray: [LABKEY.Filter.create('code', code, LABKEY.Filter.Types.EQUALS_ONE_OF)],
                columns: 'meaning',
                scope: this,
                success: function(data){
                    if(data.rows && data.rows.length){
                        meaning = data.rows[0].meaning;
                    }
                },
                failure: EHR.Server.Utils.onFailure
            });
        }

        return meaning ? meaning+(code ? ' ('+code+')' : '') : (code ? code : '');
    },
    /**
     * A helper to convert a date object into a display string.  By default is will use YYYY-mm-dd.
     * @param date The date to convert
     * @returns {string} The display string for this date or an empty string if unable to convert
     */
    dateToString: function (date){
        if(date){
            date = EHR.Server.Utils.normalizeDate(date);
            return (date.getFullYear() ? date.getFullYear()+'-'+EHR.Server.Validation.padDigits(date.getMonth()+1, 2)+'-'+EHR.Server.Validation.padDigits(date.getDate(), 2) : '');
        }
        else
            return '';
    },
    /**
     * A helper to convert a datetime object into a display string.  By default is will use YYYY-mm-dd H:m.
     * @param date The date to convert
     * @returns {string} The display string for this date or an empty string if unable to convert
     */
    dateTimeToString: function (date){
        if(date){
            date = EHR.Server.Utils.normalizeDate(date);
            return date.getFullYear()+'-'+EHR.Server.Validation.padDigits(date.getMonth()+1, 2)+'-'+EHR.Server.Validation.padDigits(date.getDate(), 2) + ' '+EHR.Server.Validation.padDigits(date.getHours(),2)+':'+EHR.Server.Validation.padDigits(date.getMinutes(),2);
        }
        else
            return '';
    },
    /**
     * Converts an input value into a display string.  Used to generate description fields when the input value could potentially be null.
     * @param value The value to convert
     * @returns {string} The string value or an empty string
     */
    nullToString: function(value){
        return (value ? value : '');
    },
    /**
     * A utility that will take an input value and pad with left-hand zeros until the string is of the desired length
     * @param {number} n The input number
     * @param {integer} totalDigits The desired length of the string.  The input will be padded with zeros until it reaches this length
     * @returns {number} The padded number
     */
    padDigits: function(n, totalDigits){
        n = n.toString();
        var pd = '';
        if (totalDigits > n.length){
            for (var i=0; i < (totalDigits-n.length); i++){
                pd += '0';
            }
        }
        return pd + n;
    },
    /**
     * A helper that will find the next available necropsy or biopsy case number, based on the desired year and type of procedure.
     * @param row The labkey row object
     * @param errors The errors object
     * @param table The queryName (Necropies or Biopsies) to search
     * @param procedureType The single character identifier for the type of procedure.  At time of writing, these were b, c or n.
     */
    calculateCaseno: function(row, errors, table, procedureType){
        var year = row.date.getYear()+1900;
        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "SELECT cast(SUBSTRING(MAX(caseno), 6, 8) AS INTEGER) as caseno FROM study."+table+" WHERE caseno LIKE '" + year + procedureType + "%'",
            scope: this,
            success: function(data){
                if(data && data.rows && data.rows.length==1){
                    var caseno = data.rows[0].caseno || 1;
                    caseno++;
                    caseno = EHR.Server.Validation.padDigits(caseno, 3);
                    row.caseno = year + procedureType + caseno;
                }
            },
            failure: EHR.Server.Utils.onFailure
        });

    },
    /**
     * A helper that will verify whether the caseno on the provided row is unique in the given table.
     * @param context The scriptContext object (see rowInit()).  This is used to identify the queryName.
     * @param row The row object, provided by LabKey
     * @param errors The errors object, provided by LabKey
     */
    verifyCasenoIsUnique: function(context, row, errors){
        //find any existing rows with the same caseno
        var filterArray = [
            LABKEY.Filter.create('caseno', row.caseno, LABKEY.Filter.Types.EQUAL)
        ];
        if(row.lsid)
            filterArray.push(LABKEY.Filter.create('lsid', row.lsid, LABKEY.Filter.Types.NOT_EQUAL));

        LABKEY.Query.selectRows({
            schemaName: context.extraContext.schemaName,
            queryName: context.extraContext.queryName,
            filterArray: filterArray,
            scope: this,
            success: function(data){
                if(data && data.rows && data.rows.length){
                    EHR.Server.Validation.addError(errors, 'caseno', 'One or more records already uses the caseno: '+row.caseno, 'INFO');
                }
            },
            failure: EHR.Server.Utils.onFailure
        });
    },
    /**
     * A helper that will flag a date if it is in the future (unless the QCState allows them) or if it is a QCState of 'Scheduled' and has a date in the past.
     * @param row The row object, provided by LabKey
     * @param errors The errors object, provided by LabKey
     * @param scriptContext The scriptContext object created in rowInit()
     */
    verifyDate: function(row, errors, scriptContext){
        var date = EHR.Server.Utils.normalizeDate(row.date);
        if(!date)
            return;

        //find if the date is greater than now
        var cal1 = new java.util.GregorianCalendar();
        var cal2 = new java.util.GregorianCalendar();
        cal2.setTimeInMillis(date.getTime());

        if(!scriptContext.extraContext.validateOnly && cal2.after(cal1) && !EHR.Server.Security.getQCStateByLabel(row.QCStateLabel).allowFutureDates){
            EHR.Server.Validation.addError(errors, 'date', 'Date is in future', 'ERROR');
        }

        if(!cal1.after(cal2) && row.QCStateLabel == 'Scheduled'){
            EHR.Server.Validation.addError(errors, 'date', 'Date is in past, but is scheduled', 'ERROR');
        }
    },
    /**
     * A helper that will flag any dates more than 1 year in the future or 60 days in the past.
     * @param row The row object, provided by LabKey
     * @param errors The errors object, provided by LabKey
     */
    flagSuspiciousDate: function(row, errors){
        var date = EHR.Server.Utils.normalizeDate(row.date);
        if(!date)
            return;

        //flag any dates greater than 1 year from now
        var cal1 = new java.util.GregorianCalendar();
        cal1.add(java.util.Calendar.YEAR, 1);
        var cal2 = new java.util.GregorianCalendar();
        cal2.setTimeInMillis(date.getTime());

        if(cal2.after(cal1)){
            EHR.Server.Validation.addError(errors, 'date', 'Date is more than 1 year in future', 'WARN');
        }

        cal1.add(java.util.Calendar.YEAR, -1); //adjust for the year we added above
        cal1.add(java.util.Calendar.DATE, -60);
        if(cal1.after(cal2)){
            EHR.Server.Validation.addError(errors, 'date', 'Date is more than 60 days in past', 'WARN');
        }
    },
    /**
     * A helper that will verify that the ID located in the specified field is female.
     * @param row The row object, provided by LabKey
     * @param errors The errors object, provided by LabKey
     * @param scriptContext The script context object
     * @param targetField The field containing the ID string to verify.
     */
    verifyIsFemale: function(row, errors, scriptContext, targetField){
        EHR.Server.Validation.findDemographics({
            participant: row.Id,
            scriptContext: scriptContext,
            scope: this,
            callback: function(data){
                if(data){
                    if(data['gender/origGender'] && data['gender/origGender'] != 'f')
                        EHR.Server.Validation.addError(errors, (targetField || 'Id'), 'This animal is not female', 'ERROR');
                }
            }
        });
    },
    /**
     * This is a helper designed to recalculate and update the value for calculatedStatus in study.demographics.  The value of this
     * field depends on the birth, death, arrivals and departures.  It is stored as a quick method to identify which animals are currently
     * alive and at the center.  Because animals can arrive or depart multiple times in their lives, this is more difficult than you might think.
     * Because a given transaction could have more than 1 row modified, we cache a list of modified participants and only run this once during
     * the complete() handler.
     * @param publicParticipantsModified The array of public (based on QCState) participants that were modified during this transaction.
     * @param demographicsRow If called from the demographics query, we pass the current row.  This is because selectRows() will run as a separate transaction and would not pick up changes made during this transaction.
     * @param valuesMap If the current query is death, arrival, departure or birth, the values updated in this transaction will not appear for the calls to selectRows() (this is run in a separate transaction i believe).  Therefore we provide a mechanism for these scripts to pass in the values of their current row, which will be used instead.
     */
    updateStatusField: function(publicParticipantsModified, demographicsRow, valuesMap){
        var toUpdate = [];
        var demographics = {};
        LABKEY.ExtAdapter.each(publicParticipantsModified, function(id){
            demographics[id] = {};
        });

        //NOTE: to improve efficiency when only 1 animal is present, we define the WHERE logic here:
        var whereClause;
        if(publicParticipantsModified.length==1)
            whereClause = "= '"+publicParticipantsModified[0]+"'";
        else
            whereClause = "IN ('"+(publicParticipantsModified.join('\',\''))+"')";


        //we gather the pieces of information needed to calculate the status field
        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "select T1.Id, max(T1.date) as MostRecentArrival FROM study.arrival T1 WHERE T1.id "+whereClause+" AND T1.qcstate.publicdata = true GROUP BY T1.Id",
            scope: this,
            success: function(data){
                if(!data.rows.length){
                    //console.log('No rows returned for mostRecentArrival');
                    return
                }

                LABKEY.ExtAdapter.each(data.rows, function(r){
                    if(r.MostRecentArrival)
                        demographics[r.Id].arrival = new Date(r.MostRecentArrival);
                }, this);
            },
            failure: EHR.Server.Utils.onFailure
        });

        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "select T1.Id, max(T1.date) as MostRecentDeparture FROM study.departure T1 WHERE T1.id "+whereClause+" AND T1.qcstate.publicdata = true GROUP BY T1.Id",
            scope: this,
            success: function(data){
                if(!data.rows.length){
                    //console.log('No rows returned for mostRecentDeparture');
                    return;
                }

                LABKEY.ExtAdapter.each(data.rows, function(r){
                    if(r.MostRecentDeparture)
                        demographics[r.Id].departure = new Date(r.MostRecentDeparture);
                }, this);
            },
            failure: EHR.Server.Utils.onFailure
        });

        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "select T1.Id, T1.birth, T1.death, T1.calculated_status, T1.lsid FROM study.demographics T1 WHERE T1.id "+whereClause,
            scope: this,
            success: function(data){
                if(!data.rows.length){
                    //TODO: maybe throw flag/email to alert colony records?
                    return
                }

                LABKEY.ExtAdapter.each(data.rows, function(r){
                    if(r.birth)
                        demographics[r.Id].birth = new Date(r.birth);
                    if(r.death)
                        demographics[r.Id].death = new Date(r.death);

                    demographics[r.Id].lsid = r.lsid;
                    demographics[r.Id].calculated_status = r.calculated_status;
                }, this);
            },
            failure: EHR.Server.Utils.onFailure
        });

        //NOTE: the ETL acts by deleting / inserting.
        //this means the demographics row does not exist for cases when a update happens.
        //therefore we re-query birth / death directly
        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "select T1.Id, max(T1.date) as date FROM study.birth T1 WHERE T1.id "+whereClause+" AND T1.qcstate.publicdata = true GROUP BY T1.Id",
            scope: this,
            success: function(data){
                if(!data.rows.length){
                    return;
                }

                LABKEY.ExtAdapter.each(data.rows, function(r){
                    if(demographics[r.Id].birth && demographics[r.Id].birth.getTime() != (new Date(r.date)).getTime()){
                        console.error('birth from study.birth doesnt match demographics.birth for: '+r.Id);
                        console.error(demographics[r.Id].birth);
                        console.error(new Date(r.date));
                    }

                    if(r.date)
                        demographics[r.Id].birth = new Date(r.date);
                }, this);
            },
            failure: EHR.Server.Utils.onFailure
        });
        LABKEY.Query.executeSql({
            schemaName: 'study',
            sql: "select T1.Id, max(T1.date) as date FROM study.deaths T1 WHERE T1.id "+whereClause+" AND T1.qcstate.publicdata = true GROUP BY T1.Id",
            scope: this,
            success: function(data){
                if(!data.rows.length){
                    return;
                }
                LABKEY.ExtAdapter.each(data.rows, function(r){
                    //var demographicsDeath = demographics[r.Id].death;
                    if(demographics[r.Id].death && demographics[r.Id].death.getTime() != (new Date(r.date)).getTime()){
                        console.error('death doesnt match for: '+r.Id);
                        console.error(demographics[r.Id].birth);
                        console.error(new Date(r.date));
                    }

                    if(r.date)
                        demographics[r.Id].death = new Date(r.date);
                }, this);
            },
            failure: EHR.Server.Utils.onFailure
        });

        LABKEY.ExtAdapter.each(publicParticipantsModified, function(id){
            var status;
            var forceUpdate = false;

            var r = demographics[id];

            //when called by birth or death, we allow the script to pass in
            //the values of the rows being changed
            //this saves a duplicate insert/update on demographics
            if(valuesMap && valuesMap[id]){
                if(valuesMap[id].birth && valuesMap[id].birth != r.birth){
                    r.birth = valuesMap[id].birth;
                    r.updateBirth = true;
                    forceUpdate = true;
                }

                if(valuesMap[id].death && valuesMap[id].death != r.death){
                    r.death = valuesMap[id].death;
                    r.updateDeath = true;
                    forceUpdate = true;
                }
            }
            //console.log('Id: '+id+'/ status: '+status);

            if (r['death'])
                status = 'Dead';
            else if (r['departure'] && (!r['arrival'] || r['departure'] > r['arrival']))
                status = 'Shipped';
            else if ((r['birth'] || r['arrival']) && (!r['departure'] || r['departure'] < r['arrival']))
                status = 'Alive';
            else if (!r['birth'] && !r['arrival'])
                status = 'No Record';
            else
                status = 'ERROR';

            //console.log('Id: '+id+'/ status: '+status);

            //NOTE: this is only used when called by the demographics validation script,
            // which passes the row object directly, instead of using updateRows below
            if(demographicsRow && id == demographicsRow.Id){
                demographicsRow.calculated_status = status;
                return demographicsRow;
            }

//            console.log('status: '+status);
//            console.log('forceUpdate: '+forceUpdate);
//            console.log('calc status: '+r.calculated_status);
            if(status != r.calculated_status || forceUpdate){
                //the following means no record exists in study.demographics for this ID
                if(!r.lsid){
                    return;
                }

                var newRow = {Id: id, lsid: r.lsid, calculated_status: status};

                if(r.updateBirth)
                    newRow.birth = EHR.Server.Utils.normalizeDate(r.birth);
                if(r.updateDeath)
                    newRow.death = EHR.Server.Utils.normalizeDate(r.death);

                toUpdate.push(newRow);
            }
        }, this);
        //console.log('to update: '+toUpdate.length);
        if(toUpdate.length && !demographicsRow){
            LABKEY.Query.updateRows({
                schemaName: 'study',
                queryName: 'Demographics',
                rows: toUpdate,
                scope: this,
                extraContext: {
                    quickValidation: true
                },
                success: function(data){
                    console.log('Success updating demographics status field');
                },
                failure: EHR.Server.Utils.onFailure
            });
        }
    },
    /**
     * A helper that will return and cache the row in study.demographics for the provided animal.  These rows are stored in scriptContext.demographicsMap,
     * so subsequent calls do not need to hit the server.
     * @param config The configuration object
     * @param [config.participant] The participant (aka animal ID) to return
     * @param [config.callback] The success callback function
     * @param [config.scope] The scope to use for the callback
     * @param [config.scriptContext] The scriptContext object created in rowInit()
     */
    findDemographics: function(config){
        if(!config || !config.participant || !config.callback || !config.scope){
            EHR.Server.Utils.onFailure({
                msg: 'Error in EHR.Server.Validation.findDemographics(): missing Id, scope or callback'
            });
            throw 'Error in EHR.Server.Validation.findDemographics(): missing Id, scope or callback';
        }

        var start = new Date();
        var scriptContext = config.scriptContext || config.scope.scriptContext;

        if(!scriptContext){
            throw "Error in EHR.Server.Validation.findDemographics(): No scriptContext provided";
        }

        if(scriptContext.demographicsMap[config.participant] && !config.forceRefresh){
            console.log('Find demographics time: ' + (((new Date()) - start) / 1000));
            config.callback.apply(config.scope || this, [scriptContext.demographicsMap[config.participant]])
        }
        else if(scriptContext.missingParticipants.indexOf(config.participant) >  -1){
            console.log('Find demographics time: ' + (((new Date()) - start) / 1000));
            config.callback.apply(config.scope || this);
        }
        else {
//            NOTE: switch to this once we actually start caching values
//            var ar = scriptContext.helper.getDemographicRecord(config.participant);
//            console.log('Find demographics time: ' + (((new Date()) - start) / 1000));
//            if(ar){
//                var row = {
//                    Id: ar.getId(),
//                    birth: ar.getBirth() ? new Date(ar.getBirth().getTime()) : null,
//                    death: ar.getDeath() ? new Date(ar.getDeath().getTime()) : null,
//                    species: ar.getSpecies(),
//                    dam: null,
//                    gender: ar.getGender(),
//                    'gender/origGender': ar.getOrigGender(),
//                    calculated_status: ar.getCalculatedStatus(),
//                    sire: null,
//                    'id/curlocation/room': ar.getCurrentRoom(),
//                    'id/curlocation/cage': ar.getCurrentCage()
//                }
//
//                //cache results
//                scriptContext.demographicsMap[row.Id] = row;
//                config.callback.apply(config.scope || this, [scriptContext.demographicsMap[row.Id]]);
//            }
//            else {
//                if(scriptContext.missingParticipants.indexOf(config.participant) == -1)
//                    scriptContext.missingParticipants.push(config.participant);
//
//                config.callback.apply(config.scope || this);
//            }

            LABKEY.Query.selectRows({
                schemaName: 'study',
                queryName: 'demographics',
                columns: 'lsid,Id,birth,death,species,dam,gender,gender/origGender,calculated_status,sire,id/curlocation/room,id/curlocation/cage',
                scope: this,
                filterArray: [LABKEY.Filter.create('Id', config.participant, LABKEY.Filter.Types.EQUAL)],
                success: function(data){
                    console.log('Find demographics time: ' + (((new Date()) - start) / 1000));
                    if(data && data.rows && data.rows.length==1){
                        var row = data.rows[0];

                        //cache results
                        scriptContext.demographicsMap[row.Id] = row;
                        config.callback.apply(config.scope || this, [scriptContext.demographicsMap[row.Id]]);
                    }
                    else {
                        if(scriptContext.missingParticipants.indexOf(config.participant) == -1)
                            scriptContext.missingParticipants.push(config.participant);

                        config.callback.apply(config.scope || this);
                    }
                },
                failure: EHR.Server.Utils.onFailure
            });
        }
    },
    /**
     * A helper designed to simplify appending errors to the error object.  You should exclusively use this to append errors rather than interacting with the error object directly.
     * @param {object} errors The errors object.  In most cases, this is the scriptErrors object passed internally within rowInit(), not the labkey-provided errors object.
     * @param {string}field The name of the field for which to add the error.  Treat as case-sensitive, because client-side code will be case-sensitive.
     * @param {string} msg The message associated with this error
     * @param {string} severity The error severity.  Should match a value from EHR.Server.Validation.errorSeverities.
     */
    addError: function(errors, field, msg, severity){
        if(!errors[field])
            errors[field] = [];

        errors[field].push({
            message: msg,
            severity: severity || 'ERROR'
        });
    },
    /**
     * When an animal dies or leaves the center, this will close any open records (ie. the death/depart time inserted into the enddate field) for any records in assignment, housing, problem list and treatment orders.
     * @param participant The Id of the participant
     * @param date The date of the event.
     */
    onDeathDeparture: function(participant, date){
        //close housing, assignments, treatments
        //org.labkey.ehr.utils.TriggerScriptHelper.closeActiveDatasetRecords(LABKEY.Security.currentContainer.id, LABKEY.Security.currentUser.id, ['Assignment'], participant, date)
        closeRecord('Assignment');
        closeRecord('Housing');
        closeRecord('Treatment Orders');
        closeRecord('Problem List');

        function closeRecord(queryName){
            LABKEY.Query.selectRows({
                schemaName: 'study',
                queryName: queryName,
                columns: 'lsid,Id',
                scope: this,
                extraContext: {
                    quickValidation: true
                },
                filterArray: [
                    LABKEY.Filter.create('Id', participant, LABKEY.Filter.Types.EQUAL),
                    LABKEY.Filter.create('enddate', '', LABKEY.Filter.Types.ISBLANK)
                ],
                success: function(data){
                    if(data && data.rows && data.rows.length){
                        var toUpdate = [];
                        //console.log(data.rows.length);
                        LABKEY.ExtAdapter.each(data.rows, function(r){
                            toUpdate.push({lsid: r.lsid, enddate: date.toGMTString()})
                        }, this);

                        LABKEY.Query.updateRows({
                            schemaName: 'study',
                            queryName: queryName,
                            rows: toUpdate,
                            extraContext: {
                                quickValidation: true
                            },
                            scope: this,
                            failure: EHR.Server.Utils.onFailure,
                            success: function(data){
                                console.log('Success closing '+queryName+' records: '+toUpdate.length);
                            }
                        });
                    }
                },
                failure: EHR.Server.Utils.onFailure
            });
        }
    },
    /**
     * A helper for sending emails from validation scripts that wraps LABKEY.Message.  The primary purpose is that this allows emails to be sent based on EHR notification types (see ehr.notificationtypes table).  Any emails should use this helper.
     * @param config The configuration object
     * {Array} [config.recipients] An array of recipient object to receive this email.  The should have been created using LABKEY.Message.createRecipient() or LABKEY.Message.createPrincipalIdRecipient()
     * {String} [config.notificationType] The notificationType to use, which should match a record in ehr.notificationtypes.  If provided, any users/groups 'subscribed' to this notification type (ie. containing a record in ehr.notificationrecipients for this notification type) will receive this email.
     * {String} [config.msgFrom] The email address from which to send this message
     * {String} [config.msgSubject] The subject line of the email
     * {String} [config.msgContent] The content for the body of this email
     */
    sendEmail: function(config){
        console.log('Sending emails');

        if(!config.recipients)
            config.recipients = [];

        if(config.notificationType){
            LABKEY.Query.selectRows({
                schemaName: 'ehr',
                queryName: 'notificationRecipients',
                filterArray: [LABKEY.Filter.create('notificationType', config.notificationType, LABKEY.Filter.Types.EQUAL)],
                success: function(data){
                    for(var i=0;i<data.rows.length;i++){
                        config.recipients.push(LABKEY.Message.createPrincipalIdRecipient(LABKEY.Message.recipientType.to, data.rows[i].recipient));
                        //console.log('Recipient: '+data.rows[i].recipient);
                    }
                },
                failure: EHR.Server.Utils.onFailure
            });
        }

        console.log('This email has ' + config.recipients.length + ' recipients');
        if(config.recipients.length){
            var siteEmail = config.msgFrom;
            if(!siteEmail){
                //TODO: module property?
                LABKEY.Query.selectRows({
                    schemaName: 'ehr',
                    queryName: 'module_properties',
                    scope: this,
                    filterArray: [LABKEY.Filter.create('prop_name', 'site_email', LABKEY.Filter.Types.EQUAL)],
                    success: function(data){
                        if(data && data.rows && data.rows.length){
                            siteEmail = data.rows[0].stringvalue;
                        }
                    },
                    failure: EHR.Server.Utils.onFailure
                });
            }

            if(!siteEmail){
                console.log('ERROR: site email not found');
                EHR.Server.Validation.logError({msg: 'ERROR: site email not found'});
            }

            LABKEY.Message.sendMessage({
                msgFrom: siteEmail,
                msgSubject: config.msgSubject,
                msgRecipients: config.recipients,
                allowUnregisteredUser: true,
                msgContent: [
                    LABKEY.Message.createMsgContent(LABKEY.Message.msgType.html, config.msgContent)
                ],
                success: function(){
                    console.log('Success sending emails');
                },
                failure: EHR.Server.Utils.onFailure
            });
        }
    }
};