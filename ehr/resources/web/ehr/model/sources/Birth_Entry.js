/*
 * Copyright (c) 2017-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
EHR.model.DataModelManager.registerMetadata('Birth_Entry', {
    allQueries: {

    },
    byQuery: {
        'study.Birth': {
            room:  {
                xtype: 'ehr-roomfieldsingle',
                itemId: 'roomField',
                name: 'room',
                columnConfig: {
                    fixed: true,
                    width: 180
                }
            }
        }
    }
});

