/**
 * @cfg taskId
 * @cfg requestId
 */
Ext4.define('EHR.panel.DataEntryFormDetailsPanel', {
    extend: 'Ext.panel.Panel',

    initComponent: function(){
        Ext4.apply(this, {
            border: false,
            defaults: {
                border: false
            },
            items: [{
                html: 'Loading...'
            }]
        });

        this.callParent();

        this.loadData();
    },

    loadData: function(){
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('ehr', 'getDataEntryFormDetails', null, {
                taskId: this.taskId,
                requestId: this.requestId,
                formType: this.formType
            }),
            scope: this,
            failure: LDK.Utils.getErrorCallback(),
            success: LABKEY.Utils.getCallbackWrapper(this.onLoad, this)
        });
    },

    onLoad: function(results){
        var queries = [];
        var queryMap = {};
        Ext4.Array.forEach(results.form.sections, function(section){
            if (section.queries){
                Ext4.Array.forEach(section.queries, function(query){
                    var key = query.schemaName + '.' + query.queryName;
                    if (!queryMap[key]){
                        query.label = section.label;
                        queries.push(query);
                        queryMap[key] = true;
                    }
                }, this);
            }
        }, this);

        var filterArray = [];

        if (this.taskId)
            filterArray.push(LABKEY.Filter.create('taskid', this.taskId, LABKEY.Filter.Types.EQUAL));
        if (this.requestId)
            filterArray.push(LABKEY.Filter.create('requestid', this.requestId, LABKEY.Filter.Types.EQUAL));

        LDK.Assert.assertTrue('No filter array applied in DataEntryFormDetailsPanel', filterArray.length > 0);

        var toAdd = [];
        Ext4.Array.forEach(queries, function(q){
            toAdd.push({
                xtype: 'panel',
                title: q.label,
                border: true,
                bodyStyle: 'padding: 5px;',
                defaults: {
                    border: false
                },
                items: [{
                    xtype: 'ldk-querypanel',
                    style: 'margin-bottom: 20px;',
                    queryConfig: {
                        schemaName: q.schemaName,
                        queryName: q.queryName,
                        filters: filterArray
                    }
                }]
            })
        }, this);

        this.removeAll();
        this.add(toAdd);
    }
});