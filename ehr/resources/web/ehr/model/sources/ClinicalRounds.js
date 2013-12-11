/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
EHR.model.DataModelManager.registerMetadata('ClinicalRounds', {
    allQueries: {

    },
    byQuery: {
        'study.clinremarks': {
            category: {
                defaultValue: 'Clinical',
                hidden: true
            },
            s: {
                hidden: true
            },
            o: {
                hidden: true
            },
            a: {
                hidden: true
            },
            p: {
                hidden: true
            }
        },
        'study.blood': {
            reason: {
                defaultValue: 'Clinical'
            },
            instructions: {
                hidden: true
            }
        },
        'study.encounters': {
            instructions: {
                hidden: true
            }
        }
    }
});