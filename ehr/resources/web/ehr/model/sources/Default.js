/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * The default metadata applied to all queries when using getTableMetadata().
 * This is the default metadata applied to all queries when using getTableMetadata().  If adding attributes designed to be applied
 * to a given query in all contexts, they should be added here
 */
EHR.model.DataModelManager.registerMetadata('Default', {
    allQueries: {
        fieldDefaults: {
            ignoreColWidths: true
        },
        Id: {
            xtype: 'ehr-animalfield',
            dataIndex: 'Id',
            nullable: false,
            allowBlank: false,
            lookups: false,
            noSaveInTemplateByDefault: true,
            columnConfig: {
                width: 95,
                showLink: false
            }
        },
        'id/curlocation/location': {
            hidden: true,
            updateValueFromServer: true,
            allowBlank: true,
            nullable: true,
            shownInGrid: true,
            caption: 'Location',
            header: 'Location',
            lookups: false,
            allowDuplicateValue: false,
            columnConfig: {
                width: 75,
                showLink: false
            }
        },
        'id/numroommates/cagemates': {
            hidden: true,
            updateValueFromServer: true,
            allowBlank: true,
            nullable: true,
            shownInGrid: true,
            caption: 'Animals In Cage',
            header: 'Animals In Cage',
            lookups: false,
            allowDuplicateValue: false,
            columnConfig: {
                width: 120,
                showLink: false
            }
        },
        daterequested: {
            xtype: 'xdatetime',
            noDuplicateByDefault: true,
            extFormat: 'Y-m-d H:i'
        },
        date: {
            allowBlank: false,
            nullable: false,
            noSaveInTemplateByDefault: true,
            extFormat: 'Y-m-d H:i',
            editorConfig: {
                dateFormat: 'Y-m-d',
                otherToNow: true,
                timeFormat: 'H:i'
            },
            xtype: 'xdatetime',
            columnConfig: {
                fixed: true,
                width: 180,
                editor: 'xdatetime'
            },
            getInitialValue: function(v, rec){
                return v ? v : new Date()
            }
        },
        objectid: {
            noSaveInTemplateByDefault: true,
            getInitialValue: function(v, rec){
                return v || LABKEY.Utils.generateUUID();
            }
        },
        room: {
            editorConfig: {listWidth: 200}
        },
        resultNumber: {
            hidden: true
        },
        resultInRange: {
            hidden: true
        },
        resultOORIndicator: {
            hidden: true
        },
        quantityNumber: {
            hidden: true
        },
        quantityInRange: {
            hidden: true
        },
        quantityOORIndicator: {
            hidden: true
        },
        testid: {
            columnConfig: {
                showLink: false
            }
        },
        begindate: {
            xtype: 'xdatetime',
            hidden: true,
            noSaveInTemplateByDefault: true,
            extFormat: 'Y-m-d H:i',
            editorConfig: {
                dateFormat: 'Y-m-d',
                timeFormat: 'H:i'
            },
            columnConfig: {
                fixed: true,
                width: 130
            }
        },
        enddate: {
            xtype: 'xdatetime',
            noSaveInTemplateByDefault: true,
            shownInInsertView: true,
            columnConfig: {
                fixed: true,
                width: 130
            },
            extFormat: 'Y-m-d H:i',
            editorConfig: {
                dateFormat: 'Y-m-d',
                timeFormat: 'H:i'
            }
        },
        cage: {
            editorConfig: {
                //TODO
                listeners: {
                    change: function(field, val){
                        if(val && !isNaN(val)){
                            var newVal = EHR.Utils.padDigits(val, 4);
                            if(val != newVal)
                                field.setValue(newVal);
                        }
                    }
                }
            }
        },
        code: {
            xtype: 'ehr-snomedcombo',
            columnConfig: {
                width: 250,
                showLink: false
            }
        },
        tissue: {
            xtype: 'ehr-snomedcombo',
            editorConfig: {
                defaultSubset: 'Organ/Tissue'
            },
            columnConfig: {
                width: 150,
                showLink: false
            }
        },
        performedby: {
            noSaveInTemplateByDefault: true,
            columnConfig: {
                width: 65
            },
            shownInGrid: false
        },
        userid: {
            lookup: {
                schemaName: 'core',
                queryName: 'users',
                displayColumn: 'name',
                keyColumn: 'name',
                sort: 'Email'
            },
            formEditorConfig:{readOnly: true},
            editorConfig: {listWidth: 200},
            defaultValue: LABKEY.Security.currentUser.displayName,
            shownInGrid: false
        },
        CreatedBy: {
            hidden: false,
            shownInInsertView: true,
            xtype: 'displayfield',
            shownInGrid: false
        },
        ModifiedBy: {
            hidden: false,
            shownInInsertView: true,
            xtype: 'displayfield',
            shownInGrid: false
        },
        AnimalVisit: {hidden: true},
        SequenceNum: {hidden: true},
        description: {hidden: true},
        Dataset: {hidden: true},
        QCState: {
            allowBlank: false,
            noDuplicateByDefault: true,
            allowSaveInTemplate: false,
            allowDuplicateValue: false,
            noSaveInTemplateByDefault: true,
            facetingBehaviorType: "AUTO",
            getInitialValue: function(v){
                var qc;
                if(!v && EHR.Security.getQCStateByLabel('In Progress'))
                    qc = EHR.Security.getQCStateByLabel('In Progress').RowId;
                return v || qc;
            },
            shownInGrid: false,
            hidden: false,
            editorConfig: {
                editable: false,
                listWidth: 200,
                disabled: true
            },
            columnConfig: {
                width: 70
            }
        },
        parentid: {
            hidden: true,
            lookups: false
        },
        taskid: {
            lookups: false,
            hidden: true
        },
        requestid: {
            lookups: false,
            hidden: true
        },
        AgeAtTime: {hidden: true},
        Notes: {hidden: true},
        DateOnly: {hidden: true},
        Survivorship: {hidden: true},
        remark: {
            xtype: 'ehr-remarkfield',
            isAutoExpandColumn: true,
            printWidth: 150,
            editorConfig: {
                resizeDirections: 's'
            },
            columnConfig: {
                width: 200
            }
        },
        so: {
            xtype: 'ehr-remarkfield',
            isAutoExpandColumn: true,
            printWidth: 150,
            editorConfig: {
                resizeDirections: 's'
            }
        },
        a: {
            xtype: 'ehr-remarkfield',
            isAutoExpandColumn: true,
            printWidth: 150,
            editorConfig: {
                resizeDirections: 's'
            }
        },
        p: {
            xtype: 'ehr-remarkfield',
            isAutoExpandColumn: true,
            printWidth: 150,
            editorConfig: {
                resizeDirections: 's'
            }
        },
        project: {
            xtype: 'ehr-projectentryfield',
            editorConfig: {

            },
            shownInGrid: true,
            useNull: true,
            lookup: {
                columns: 'project,account'
            },
            columnConfig: {
                width: 120
            }
        },
        account: {
            shownInGrid: false
        }
    },
    byQuery: {
        'ehr.tasks': {
            taskid: {
                getInitialValue: function(v, rec){
                    v = v || rec.dataEntryPanel.taskId || LABKEY.Utils.generateUUID();
                    rec.dataEntryPanel.taskId = v;
                    return v;
                },
                hidden: true
            },
            assignedto: {
                useNull: true,
                facetingBehaviorType: "AUTO",
                getInitialValue: function(val){
                    return val || LABKEY.Security.currentUser.id
                },
                lookup: {
                    sort: 'type,displayname'
                },
                editorConfig: {listWidth: 200}
            },
            duedate: {
                xtype: 'xdatetime',
                editorConfig: {
                    dateFormat: 'Y-m-d',
                    timeFormat: 'H:i'
                },
                getInitialValue: function(val){
                    return val || new Date();
                }
            },
            category: {
                hidden: true
            },
            rowid: {
                xtype: 'displayfield'
            },
            formtype: {
                xtype: 'displayfield',
                hidden: true,
                getInitialValue: function(val, rec){
                    return val || rec.dataEntryPanel.formConfig.name;
                }
            },
            title: {
                getInitialValue: function(val, rec){
                    return val || rec.dataEntryPanel.formConfig.label;
                }
            },
            datecompleted: {
                hidden: true
            }
        },
        'ehr.requests': {
            requestid: {
                getInitialValue: function(v, rec){
                    v = v || rec.dataEntryPanel.formUUID || LABKEY.Utils.generateUUID();
                    rec.dataEntryPanel.formUUID = v;
                    rec.dataEntryPanel.formUUID = v;
                    return v;
                },
                hidden: true
            },
            notify1: {
                defaultValue: LABKEY.Security.currentUser.id,
                lookup: {
                    sort: 'Type,DisplayName'
                },
                listWidth: 250
            },
            notify2: {
                lookup: {
                    sort: 'Type,DisplayName'
                },
                listWidth: 250
            },
            notify3: {
                lookup: {
                    sort: 'Type,DisplayName'
                },
                listWidth: 250
            },
            daterequested: {
                xtype: 'xdatetime',
                hidden: true
            },
            priority: {
                defaultValue: 'Routine'
            },
            rowid: {
                xtype: 'displayfield'
            },
            formtype: {
                xtype: 'displayfield',
                hidden: true,
                getInitialValue: function(val, rec){
                    return val || rec.dataEntryPanel.formConfig.name;
                }
            },
            title: {
                getInitialValue: function(val, rec){
                    return val || rec.dataEntryPanel.formConfig.label;
                }
            }
        },
        'study.Demographics': {
            Id: {
                allowBlank: false,
                editorConfig: {
                    allowAnyId: true
                }
            },
            project: {hidden: true},
            performedby: {hidden: true},
            account: {hidden: true},
            species: {allowBlank: false},
            gender: {allowBlank: false}
        },
        'study.Parasitology Results': {
            organism: {
                xtype: 'ehr-snomedcombo',
                editorConfig: {
                    defaultSubset: 'Parasitology Results'
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            method: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            }
        },
        'study.Tissue Samples': {
            diagnosis: {
                xtype: 'ehr-snomedcombo'
            },
            performedby: {
                hidden: true
            },
            preservation: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            qualifier: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                shownInGrid: false
            },
            quantity: {
                shownInGrid: true
            },
            ship_to : {
                shownInGrid: false
            },
            tissueRemarks : {
                shownInGrid: false
            },
            stain: {
                defaultValue: 'Hematoxylin & Eosin',
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                shownInGrid: false
            },
            recipient: {
                shownInGrid: false
            },
            trimdate: {
                shownInGrid: false
            },
            trim_remarks: {
                shownInGrid: false
            },
            trimmed_by: {
                shownInGrid: false
            },
            remark: {
                hidden: true
            },
            container_type: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                shownInGrid: false
            }
        },
        'study.Histology': {
            diagnosis: {
                xtype: 'ehr-snomedcombo'
            },
            slideNum: {

            },
            performedby: {
                hidden: true
            },
            stain: {
                defaultValue: 'Hematoxylin & Eosin',
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            pathologist: {
                shownInGrid: false
            },
            pathology: {
                shownInGrid: false
            },
            trimdate: {
                shownInGrid: false
            },
            trim_remark: {
                shownInGrid: false
            },
            trimmed_by: {
                shownInGrid: false
            },
            qualifier: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            }
        },
        'study.Housing': {
            date: {
                editorConfig: {
                    allowNegative: false
                }
            },
            enddate: {
                xtype: 'xdatetime',
                extFormat: 'Y-m-d H:i',
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            },
            performedby: {
                shownInGrid: false
            },
            cage: {
                allowBlank: false
            },
            cond: {
                allowBlank: false,
                shownInGrid: false
            },
            reason: {
                shownInGrid: false
            },
            restraintType: {
                shownInGrid: false
            },
            cagesJoined: {
                shownInGrid: false
            },
            isTemp: {
                shownInGrid: false
            },
            project: {
                hidden: true
            },
            room: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            }
        },
        'study.Clinical Encounters': {
            serviceRequested: {
                xtype: 'displayfield',
                editorConfig: {
                    height: 100
                }
            },
            performedby: {
                allowBlank: false
            },
            type: {
                allowBlank: false
            }
        },
        'study.Clinical Remarks': {
            performedby: {
                hidden: false,
                defaultValue: LABKEY.Security.currentUser.displayName
            },
            remark: {
                hidden: true
            },
            date: {
                getInitialValue: function(v, rec){
                    return v ? v : (new Date((new Date().toDateString())));
                },
                noDuplicateByDefault: false
            },
            account: {
                hidden: true
            },
            so: {
                shownInGrid: false,
                //width: 300,
                height: 150
            },
            a: {
                shownInGrid: false,
                height: 150
            },
            p: {
                shownInGrid: false,
                height: 150
            }
        },
        'study.Clinpath Runs': {
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            collectionMethod : {
                shownInGrid: false
            },
            sampleType : {
                shownInGrid: false,
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            condition: {
                hidden: true
            },
            sampleId: {
                shownInGrid: false
            },
            sampleQuantity: {
                shownInGrid: true
            },
            quantityUnits: {
                shownInGrid: false
            },
            units: {
                hidden: true
            },
            serviceRequested: {
                allowBlank: false,
                columnConfig: {
                    width: 180
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            theForm.findField('type').setValue(recs[0].get('dataset'));

                            console.log(recs[0].get('dataset'))
                        }
                    }
                },
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'clinpath_tests',
                    displayColumn: 'testname',
                    keyColumn: 'testname',
                    sort: 'testname',
                    columns: '*'
                }
            },
            project: {
                allowBlank: true,
                nullable: true
            },
            account: {
                allowBlank: false
            },
            source: {
                xtype: 'ehr-snomedcombo',
                editorConfig: {
                    defaultSubset: 'Organisms'
                }
            },
            type: {
                showInGrid: false,
                updateValueFromServer: true,
                xtype: 'displayfield'
            }
        },
        'study.Treatment Orders': {
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d',
                allowBlank: false,
                getInitialValue: function(v, rec){
                    console.log(v)
                    return v ? v : new Date()
                },
                columnConfig: {
                    width: 100
                },
                shownInGrid: true
            },
            enddate: {
                xtype: 'datefield',
                extFormat: 'Y-m-d',
                columnConfig: {
                    //fixed: true,
                    width: 100
                }
                //shownInGrid: false
            },
            project: {
                allowBlank: false
            },
            CurrentRoom: {lookups: false},
            CurrentCage: {lookups: false},
            volume: {
                compositeField: 'Volume',
                xtype: 'ehr-triggernumberfield',
                noDuplicateByDefault: true,
                noSaveInTemplateByDefault: true,
                editorConfig: {
                    triggerCls: 'x4-form-search-trigger',
                    onTriggerClick: function (){
                        //recalculate amount if needed:
                        var theForm = this.findParentByType('ehr-formpanel').getForm();
                        var conc = theForm.findField('concentration').getValue();
                        var val = this.getValue();

                        if(!val || !conc){
                            alert('Must supply volume and concentration');
                            return;
                        }

                        if(val && conc){
                            var amount = conc * val;
                            var amountField = theForm.findField('amount');
                            amountField.setValue(amount);
                            amountField.fireEvent('change', amount, amountField.startValue);
                        }
                    },
                    decimalPrecision: 3
                },
                header: 'Vol',
                columnConfig: {
                    width: 50
                }
            },
            vol_units: {
                compositeField: 'Volume',
                header: 'Vol Units',
                columnConfig: {
                    width: 50
                }
            },
            concentration: {
                shownInGrid: false,
                compositeField: 'Drug Conc',
                editorConfig: {
                    decimalPrecision: 10
                }
            },
            conc_units: {
                shownInGrid: false,
                lookup: {columns: '*'},
                compositeField: 'Drug Conc',
                editorConfig: {
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            theForm.findField('amount_units').setValue(rec.get('numerator'));
                            theForm.findField('conc_units').setValue(rec.get('unit'));
                            theForm.findField('vol_units').setValue(rec.get('denominator'));

                            var doseField = theForm.findField('dosage_units');
                            if(rec.get('numerator'))
                                doseField.setValue(rec.get('numerator')+'/kg');
                            else
                                doseField.setValue('');

                            doseField.fireEvent('change', doseField.getValue(), doseField.startValue);

                        }
                    }
                }
            },
            amount: {
                compositeField: 'Amount',
                noDuplicateByDefault: true,
                noSaveInTemplateByDefault: true,
                //,allowBlank: false
                columnConfig: {
                    width: 110
                },
                editorConfig: {
                    decimalPrecision: 3
                }
            },
            amount_units: {
                compositeField: 'Amount',
                columnConfig: {
                    width: 110
                }
            },
            route: {shownInGrid: false},
            frequency: {
                allowBlank: false,
                lookup: {
                    sort: 'sort_order',
                    columns: '*'
                }
            },
            dosage: {
                xtype: 'ehr-drugdosefield',
                shownInGrid: false,
                compositeField: 'Dosage',
                editorConfig: {
                    decimalPrecision: 3
                }
            },
            dosage_units: {
                shownInGrid: false,
                compositeField: 'Dosage'
            },
            code: {
                //shownInGrid: false,
                editorConfig: {
                    defaultSubset: 'Drugs and Procedures'
                }
            },
            qualifier: {
                shownInGrid: false
            },
            meaning: {
                shownInGrid: false,
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'treatment_codes',
                    displayColumn: 'meaning',
                    keyColumn: 'meaning',
                    sort: 'category,meaning',
                    columns: '*'
                },
                editorConfig: {
                    tpl : function(){
                        var tpl = new Ext.XTemplate(
                                '<tpl for=".">' +
                                        '<div class="x-combo-list-item">{[ values["category"] ? "<b>"+values["category"]+":</b> "  : "" ]}{[ values["meaning"] ]}' +
                                        '&nbsp;</div></tpl>'
                        );
                        return tpl.compile()
                    }(),
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();

                            theForm.findField('route').setValue(rec.get('route'));
                            theForm.findField('qualifier').setValue(rec.get('qualifier'));
                            theForm.findField('code').setValue(rec.get('code'));
                            theForm.findField('frequency').setValue(rec.get('frequency'));

                            theForm.findField('amount_units').setValue(rec.get('amount_units'));
                            theForm.findField('conc_units').setValue(rec.get('conc_units'));
                            theForm.findField('vol_units').setValue(rec.get('vol_units'));
                            theForm.findField('dosage_units').setValue(rec.get('dosage_units'));

                            theForm.findField('amount').setValue(rec.get('amount'));
                            theForm.findField('concentration').setValue(rec.get('concentration'));
                            theForm.findField('volume').setValue(rec.get('volume'));

                            var doseField = theForm.findField('dosage');
                            doseField.setValue(rec.get('dosage'));
                            doseField.fireEvent('change', doseField.getValue(), doseField.startValue);
                        }
                    }
                }
            },
            remark: {
                shownInGrid: false
            },
            performedby: {
                defaultValue: LABKEY.Security.currentUser.displayName
            }
        },
        'ehr.Project': {
            project: {
                xtype: 'textfield',
                lookups: false
            }
        },
        'study.Assignment': {
            project: {
                shownInGrid: true,
                allowBlank: false,
                xtype: 'combo',
                lookup: {
                    filterArray: [LABKEY.Filter.create('protocol/protocol', null, LABKEY.Filter.Types.NONBLANK)],
                    columns: 'project,protocol,account'
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            enddate: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            }
        },
        'study.Chemistry Results': {
            resultOORIndicator: {
                label: 'Result',
                shownInGrid: false,
                compositeField: 'Result',
                width: 80,
                includeNullRecord: false,
                nullCaption: '',
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'oor_indicators',
                    keyColumn: 'indicator',
                    displayColumn: 'indicator'
                }
            },
            result: {
                compositeField: 'Result',
                editorConfig: {
                    decimalPrecision: 4
                }
            },
            testid: {
                lookup: {
                    columns: '*',
                    filterArray: [LABKEY.Filter.create('categories', 'chemistry', LABKEY.Filter.Types.CONTAINS)]
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            var unitField = theForm.findField('units');
                            unitField.setValue(rec.get('units'));
                            unitField.fireEvent('change', unitField.getValue(), unitField.startValue);
                        }
                    }
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            method: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            }
        },
        'study.Hematology Results': {
            testid: {
                lookup: {
                    columns: '*',
                    filterArray: [LABKEY.Filter.create('categories', 'hematology', LABKEY.Filter.Types.CONTAINS)]
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            var unitField = theForm.findField('units');
                            unitField.setValue(rec.get('units'));
                            unitField.fireEvent('change', unitField.getValue(), unitField.startValue);
                        }
                    }
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            method: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            }
        },
        'study.Urinalysis Results': {
            resultOORIndicator: {
                label: 'Result',
                shownInGrid: false,
                compositeField: 'Result',
                width: 80,
                includeNullRecord: false,
                nullCaption: '',
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'oor_indicators',
                    keyColumn: 'indicator',
                    displayColumn: 'indicator'
                }
            },
            result: {
                compositeField: 'Result',
                editorConfig: {
                    decimalPrecision: 4
                }
            },
            testid: {
                lookup: {
                    columns: '*',
                    filterArray: [LABKEY.Filter.create('categories', 'urinalysis', LABKEY.Filter.Types.CONTAINS)]
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            var unitField = theForm.findField('units');
                            unitField.setValue(rec.get('units'));
                            unitField.fireEvent('change', unitField.getValue(), unitField.startValue);
                        }
                    }
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            method: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            }
        },
        'study.Virology Results': {
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            method: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            }
        },
        'study.Arrival': {
            Id: {
                editorConfig: {allowAnyId: true}
            },
            project: {hidden: true},
            account: {hidden: true},
            source: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                allowBlank: false
            },
            performedby: {hidden: true},
            remark: {shownInGrid: false},
            dam: {shownInGrid: false},
            sire: {shownInGrid: false},
            initialRoom: {
                hidden: false,
                allowBlank: false
            },
            initialCage: {
                hidden: false,
                allowBlank: false,
                editorConfig: {
                    listeners: {
                        change: function(field, val){
                            if(val && !isNaN(val)){
                                var newVal = EHR.Utils.padDigits(val, 4);
                                if(val != newVal)
                                    field.setValue(newVal);
                            }
                        }
                    }
                }
            },
            initialCond: {hidden: false}
        },
        'study.Departure': {
            performedby: {hidden: true},
            project: {hidden: true},
            account: {hidden: true},
            authorized_by: {allowBlank: false},
            destination: {
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                allowBlank: false
            }
        },
        'study.Deaths': {
            performedby: {
                hidden: false,
                defaultValue: LABKEY.Security.currentUser.displayName
            },
            project: {
                hidden: false,
                allowBlank: false
            },
            account: {hidden: true},
            necropsy: {lookups: false},
            cause: {allowBlank: false},
            tattoo: {
                editorConfig: {
                    helpPopup: 'Please enter the color and number of the tag and/or all visible tattoos'
                }
            }
            //manner: {allowBlank: false}
        },
        'study.Birth': {
            performedby: {hidden: true},
            project: {hidden: true},
            account: {hidden: true},
            dam: {shownInGrid: false, lookups: false},
            sire: {shownInGrid: false, lookups: false},
            gender: {includeNullRecord: false, allowBlank: false},
            weight: {shownInGrid: false},
            wdate: {shownInGrid: false},
            room: {shownInGrid: false},
            cage: {shownInGrid: false},
            cond: {shownInGrid: false},
            origin: {shownInGrid: false},
            estimated: {shownInGrid: false},
            conception: {shownInGrid: false}
        },
        'study.Blood Draws' : {
            billedby: {shownInGrid: false},
            remark: {shownInGrid: false},
            project: {shownInGrid: false, allowBlank: false},
            requestor: {shownInGrid: false, hidden: true, formEditorConfig:{readOnly: true}},
            performedby: {shownInGrid: false},
            instructions: {shownInGrid: false},
            additionalServices: {
                xtype: 'combo',
                multiSelect: true,
                hasOwnTpl: true,
                includeNullRecord: false,
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'blood_draw_services',
                    displayColumn: 'service',
                    keyColumn: 'service'
                },
                editorConfig: {
                    tpl: null,
                    separator: ';'
                },
                columnConfig: {
                    width: 200
                }
            },
            tube_type: {
                xtype: 'combo',
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'blood_draw_tube_type',
                    displayColumn: 'type',
                    keyColumn: 'type',
                    columns: 'type,volume'
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(field, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = field.up('form').getForm();
                            if (!theForm){
                                console.error('no form found');
                                return;
                            }

                            var tube_vol = theForm.findField('tube_vol');
                            tube_vol.store.filterArray = [LABKEY.Filter.create('tube_types', rec.get('type'), LABKEY.Filter.Types.CONTAINS)];
                            tube_vol.store.load();
                        }
                    }
                },
                columnConfig: {
                    width: 100,
                    showLink: false
                }
            },
            quantity: {
                //xtype: 'displayfield',
                shownInGrid: true,
                allowBlank: false,
                editorConfig: {
                    allowNegative: false,
                    calculateQuantity: function(){
                        var parent = this.findParentByType('ehr-formpanel');
                        if (!parent){
                            return
                        }

                        var form = parent.getForm();
                        if (!form){
                            console.error('no form found');
                            return;
                        }

                        var numTubes = form.findField('num_tubes').getValue();
                        var tube_vol = form.findField('tube_vol').getValue();

                        var quantity = numTubes*tube_vol;
                        this.setValue(quantity);
                        this.fireEvent('change', quantity, this.startValue);
                    }
                }
            },
            num_tubes: {
                xtype: 'ehr-triggernumberfield',
                editorConfig: {
                    allowNegative: false,
                    triggerCls: 'x4-form-search-trigger',
                    onTriggerClick: function(){
                        var parent = this.findParentByType('ehr-formpanel');
                        var theForm = parent.getForm();

                        var tube_vol = theForm.findField('tube_vol');

                        if(!tube_vol.getValue() || !this.getValue()){
                            Ext4.Msg.alert('Error', 'Must enter tube volume and number of tubes');
                            return;
                        }

                        var quantity = tube_vol.getValue() * this.getValue();
                        var quantityField = theForm.findField('quantity');
                        quantityField.setValue(quantity);
                        quantityField.fireEvent('change', quantity, quantityField.startValue);
                    }
                },
                allowBlank: true,
                columnConfig: {
                    width: 100,
                    header: '# Tubes',
                    showLink: false
                }
            },
            tube_vol: {
                shownInGrid: true,
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                },
                includeNullRecord: false,
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'blood_tube_volumes',
                    displayColumn: 'volume',
                    keyColumn: 'volume',
                    columns: '*',
                    sort: 'volume'
                },
                columnConfig: {
                    width: 130,
                    header: 'Tube Vol (mL)',
                    showLink: false
                }
            }
        },
        'study.Drug Administration': {
            enddate: {
                shownInGrid: false,
                hidden: false,
                shownInInsertView: true,
                label: 'End Time'
            },
            category: {
                hidden: true
            },
            code: {
                editorConfig: {
                    defaultSubset: 'Drugs and Procedures'
                }
            },
            date: {
                header: 'Start Time',
                label: 'Start Time',
                hidden: false
            },
            HeaderDate: {
                xtype: 'xdatetime',
                hidden: true,
                shownInGrid: false
            },
            remark: {shownInGrid: false},
            dosage: {
                xtype: 'ehr-drugdosefield',
                shownInGrid: false,
                compositeField: 'Dosage',
                editorConfig: {
                    decimalPrecision: 3
                }
            },
            dosage_units: {
                shownInGrid: false,
                compositeField: 'Dosage',
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            concentration: {
                shownInGrid: false,
                compositeField: 'Drug Conc',
                editorConfig: {
                    decimalPrecision: 10
                }
            },
            conc_units: {
                shownInGrid: false,
                lookup: {columns: '*'},
                compositeField: 'Drug Conc',
                editorConfig: {
                    plugins: ['ehr-usereditablecombo'],
                    listeners: {
                        select: function(combo, recs){
                            if (!recs || recs.length != 1)
                                return;

                            var rec = recs[0];
                            var theForm = this.findParentByType('ehr-formpanel').getForm();
                            theForm.findField('amount_units').setValue(rec.get('numerator'));
                            theForm.findField('conc_units').setValue(rec.get('unit'));
                            theForm.findField('vol_units').setValue(rec.get('denominator'));

                            var doseField = theForm.findField('dosage_units');
                            if(rec.get('numerator'))
                                doseField.setValue(rec.get('numerator')+'/kg');
                            else
                                doseField.setValue('');

                            doseField.fireEvent('change', doseField.getValue(), doseField.startValue);
                        }
                    }
                }
            },
            route: {
                shownInGrid: false,
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            volume: {
                compositeField: 'Volume',
                xtype: 'ehr-triggernumberfield',
                noDuplicateByDefault: true,
                noSaveInTemplateByDefault: true,
                editorConfig: {
                    triggerCls: 'x4-form-search-trigger',
                    onTriggerClick: function (field){
                        //recalculate amount if needed:
                        var theForm = field.up('form');
                        var conc = theForm.down('#concentration').getValue();
                        var val = field.getValue();

                        if(!val || !conc){
                            alert('Must supply volume and concentration');
                            return;
                        }

                        if(val && conc){
                            var amount = conc * val;
                            var amountField = theForm.down('#amount');
                            amountField.setValue(amount);
                            amountField.fireEvent('change', amount, amountField.startValue);
                        }
                    },
                    decimalPrecision: 3
                }
            },
            vol_units: {
                compositeField: 'Volume',
                header: 'Units',
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            amount: {
                compositeField: 'Amount Given',
                noDuplicateByDefault: true,
                noSaveInTemplateByDefault: true,
                //,allowBlank: false
                columnConfig: {
                    width: 90
                },
                editorConfig: {
                    decimalPrecision: 10
                }
            },
            amount_units: {
                compositeField: 'Amount Given',
                columnConfig: {
                    width: 100
                },
                editorConfig: {
                    plugins: ['ehr-usereditablecombo']
                }
            },
            performedby: {
                allowBlank: false
            },
            project: {
                allowBlank: false
            },
            restraint: {
                shownInGrid: false
            },
            restraintDuration: {
                shownInGrid: false
            },
            qualifier: {
                shownInGrid: false
            }
        },
        'study.Notes': {
            performedby: {hidden: true},
            project: {hidden: true},
            account: {hidden: true}
        },
        'study.Problem List': {
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            enddate: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            },
            project: {hidden: true},
            account: {hidden: true},
            performedby: {hidden: true},
            code: {hidden: true},
            problem_no: {shownInInsertView: false}
        },
        'study.Clinical Observations': {
            observation: {
                xtype: 'ehr-remoteradiogroup',
                //defaultValue: 'Normal',
                //allowBlank: false,
                includeNullRecord: false,
                editorConfig: {columns: 2},
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'normal_abnormal',
                    displayColumn: 'state',
                    keyColumn: 'state',
                    sort: '-state'
                }
            },
            date: {
                label: 'Time'
            },
            performedby: {hidden: true}
        },
        'study.TB Tests': {
            lot: {
                shownInGrid: false
            },
            project: {
                hidden: true
            },
            account: {
                hidden: true
            },
            dilution: {
                shownInGrid: false
            },
            remark: {
                shownInGrid: false
            },
            eye: {
                columnConfig: {
                    width: 40
                }
            },
            result1: {
                columnConfig: {
                    width: 40
                }
            },
            result2: {
                columnConfig: {
                    width: 40
                }
            },
            result3: {
                columnConfig: {
                    width: 40
                }
            },
            date: {
                xtype: 'datefield',
                extFormat: 'Y-m-d'
            }
        },
        'study.Weight': {
            project: {
                hidden: true
            },
            account: {
                hidden: true
            },
            performedby: {
                hidden: true
            },
            'id/curlocation/location': {
                shownInGrid: true
            },
            remark: {
                shownInGrid: true
            },
            weight: {
                allowBlank: false,
                useNull: true,
                editorConfig: {
                    allowNegative: false,
                    decimalPrecision: 4
                }
            }
            //,performedby: {allowBlank: true}
        },
        'study.Pairings': {
            pairingtype: {
                columnConfig: {
                    width: 125,
                    showLink: false
                }
            },
            pairingoutcome: {
                columnConfig: {
                    width: 150,
                    showLink: false
                }
            },
            separationreason: {
                columnConfig: {
                    width: 160,
                    showLink: false
                }
            }
        }
    }
});