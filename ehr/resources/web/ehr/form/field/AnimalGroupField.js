/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 *
 * @cfg pairedWithRoomField.  Note: if true, you must implement getRoomField(), which returns the cognate ehr-roomfield
 */
Ext4.define('EHR.form.field.AnimalGroupField', {
    extend: 'Ext.form.field.ComboBox',
    alias: 'widget.ehr-animalgroupfield',

    fieldLabel: 'Animal Group',
    expandToFitContent: true,
    nullCaption: '[Blank]',
    editable: false,
    typeAhead: true,

    initComponent: function(){
        LABKEY.ExtAdapter.apply(this, {
            displayField: 'name',
            valueField: 'rowid',
            queryMode: 'local',
            store: Ext4.create('LABKEY.ext4.Store', {
                schemaName: 'ehr',
                queryName: 'animal_groups',
                sort: 'name',
                filterArray: [LABKEY.Filter.create('enddate', null, LABKEY.Filter.Types.ISBLANK)],
                autoLoad: true
            })
        });

        this.callParent(arguments);
    }
});