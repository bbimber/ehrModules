/**
 * @param runId
 * @param hideHeader
 */
Ext4.define('EHR.panel.LabworkSummaryPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ehr-labworksummarypanel',

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
            url: LABKEY.ActionURL.buildURL('ehr', 'getLabResultSummary'),
            params: {
                runId: this.runId
            },
            scope: this,
            failure: LABKEY.Utils.getCallbackWrapper(LDK.Utils.getErrorCallback(), this),
            success: LABKEY.Utils.getCallbackWrapper(this.onLoad)
        });
    },

    onLoad: function(results){
        this.removeAll();

        this.add({
            border: false,
            style: 'padding-left: 5px;',
            defaults: {
                border: false
            },
            items: [{
                html : 'Results:<hr>',
                style: 'bottom-left: 5px;',
                hidden: this.hideHeader
            },{
                html: results.results[this.runId] ? results.results[this.runId] : 'No results found',
                style: 'padding-left: 5px;'
            }]
        });
    }
});