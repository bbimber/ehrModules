/**
 * This is the default metadata applied to records in the context of physical exam of new world monkeys (NWM).
 */
EHR.Metadata.registerMetadata('NWM_PE', {
    allQueries: {
        performedby: {
            parentConfig: {
                storeIdentifier: {queryName: 'Clinical Encounters', schemaName: 'study'},
                dataIndex: 'performedby'
            }
            //,hidden: true
            ,shownInGrid: false
        }
    },
    byQuery: {
        'Clinical Observations': {
            area: {
                allowBlank: false,
                includeNullRecord: false,
                lookup: {
                    schemaName: 'ehr_lookups',
                    queryName: 'pe_region',
                    displayColumn: 'region',
                    keyColumn: 'region',
                    sort: 'region'
                }
                ,colModel: {
                    width: 60
                }

            },
            observation: {
                defaultValue: 'Normal'
                ,colModel: {
                    width: 40
                }
            },
            remark: {
                isAutoExpandColumn: true
            },
            code: {
                hidden: true
//                colModel: {
//                    width: 120
//                }
            }
        },
        'Clinical Encounters': {
            type: {
                defaultValue: 'Physical Exam'
            },
            serviceRequested: {
                hidden: true
            },
            enddate: {
                hidden: true
            },
            major: {
                hidden: true
            },
            remark: {
                height: 100
            },
            performedby: {
                defaultValue: LABKEY.Security.currentUser.displayName,
                parentConfig: null
            }
        },
        'Clinical Remarks': {
            so: {
                shownInGrid: true
            },
            a: {
                shownInGrid: true
            },
            p: {
                shownInGrid: true
            }
        },
        'Body Condition': {
            tattoo_chest: {
                hidden: true
            },
            tattoo_thigh: {
                hidden: true
            }
        }
    }
});