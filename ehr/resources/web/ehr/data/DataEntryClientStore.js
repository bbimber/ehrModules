/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
Ext4.define('EHR.data.DataEntryClientStore', {
    extend: 'Ext.data.Store',
    alias: 'store.ehr-dataentryclientstore',
    loaded: true,

    constructor: function(){
        this.callParent(arguments);

        this.on('update', this.onUpdate, this);
    },

    getFields: function(){
        return this.model.prototype.fields;
    },

    buildClientToServerRecordMap: function(){
        var map = {};
        this.inheritingFieldMap = {};

        this.getFields().each(function(f){
            if (!f.schemaName)
                return;

            var table = f.schemaName + '.' + f.queryName;
            if (!map[table])
                map[table] = {};

            //NOTE: eventually we could support mapping to alternate field names
            map[table][f.name] = f.name;

            if (f.inheritance){
                if (f.queryName != f.inheritance.storeIdentifier.queryName && f.schemaName != f.inheritance.storeIdentifier.schemaName)
                    this.inheritingFieldMap[f.name] = f;
            }
        }, this);

        this.clientToServerRecordMap = map;
    },

    getClientToServerRecordMap: function(){
        if (this.clientToServerRecordMap)
            return this.clientToServerRecordMap;

        this.buildClientToServerRecordMap();

        return this.clientToServerRecordMap;
    },

    getKeyField: function(){
        if (this.keyFieldName)
            return this.keyFieldName;

        var keyFields = [];
        this.getFields().each(function(f){
            if (f.isKeyField){
                //hack
                if (f.name == 'lsid')
                    keyFields.push('objectid');
                else
                    keyFields.push(f.name);
            }
        }, this);

        LDK.Assert.assertEquality('Incorrect number of key fields: ' + this.storeId + ' / ' + keyFields.join(';'), 1, keyFields.length);

        if (keyFields.length == 1){
            this.keyFieldName = keyFields[0];
            return this.keyFieldName;
        }
    },

    getInheritingFieldMap: function(){
        if (this.inheritingFieldMap)
            return this.inheritingFieldMap;

        this.buildClientToServerRecordMap();

        return this.inheritingFieldMap;
    },

    //return any stores from which the current store inherits values
    getDependencies: function(){
        var map = this.getInheritingFieldMap();
        var dependencies = [];
        Ext4.Array.forEach(Ext4.Object.getValues(map), function(field){
            dependencies.push([(field.schemaName + '.' + field.queryName), (field.inheritance.storeIdentifier.schemaName + '.' + field.inheritance.storeIdentifier.queryName)]);
        }, this);

        return dependencies;
    },

    hasLoaded: function(){
        return this.loaded;
    },

    findRecord: function(fieldName, value){
        var idx = this.find(fieldName, value);
        if (idx != -1){
            return this.getAt(idx);
        }
    },

    //private
    // NOTE: the gridpanel will attempt to cache display values, so we need to clear them on update
    onUpdate : function(store, record, operation) {
        for(var field  in record.getChanges()){
            if(record.raw && record.raw[field]){
                delete record.raw[field].displayValue;
                delete record.raw[field].mvValue;
            }
        }
    },

    createModel: function(data){
        return this.callParent(arguments);
    },

    getMaxErrorSeverity: function(){
        var maxSeverity;
        this.each(function(r){
            r.validate().each(function(e){
                maxSeverity = EHR.Utils.maxError(maxSeverity, (e.severity));
            }, this);
        }, this);

        return maxSeverity;
    },

    //allows subclasses to include data to be passed to the server, such as
    getExtraContext: function(){
        return null;
    },

    safeRemove: function(records){
        Ext4.Array.forEach(records, function(r){
            var recs = [];
            if(r.get('requestid') || r.get('requestId')){
                r.beginEdit();

                //note: we reject changes since we dont want to retain modifications made in this form
                r.reject();

                //reset the date
                if(r.get('daterequested'))
                    r.set('date', r.get('daterequested'));

                r.set('taskid', null);

                r.set('QCState', EHR.Security.getQCStateByLabel('Request: Approved').RowId);
                r.set('qcstate', EHR.Security.getQCStateByLabel('Request: Approved').RowId);

                r.endEdit(true);

                r.isRemovedRequest = true;
            }
        }, this);

        this.remove(records);
    }
});