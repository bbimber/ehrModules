Ext4.define('EHR.plugin.ClinicalRemarksRowEditor', {
    extend: 'EHR.plugin.RowEditor',

    getObservationPanelCfg: function(){
        var store = this.cmp.dataEntryPanel.storeCollection.getClientStoreByName('Clinical Observations');
        LDK.Assert.assertNotEmpty('Observations store not found', store);

        return {
            xtype: 'ehr-observationssmallgridpanel',
            itemId: 'observationsPanel',
            store: store
        };
    },

    onWindowClose: function(){
        this.callParent(arguments);
        this.getEditorWindow().down('#observationsPanel').store.clearFilter();

    },

    getWindowCfg: function(){
        var ret = this.callParent(arguments);

        var formCfg = ret.items[0].items[1];
        ret.items[0].items.push(this.getObservationPanelCfg());

        return ret;
    },

    loadRecord: function(record){
        this.callParent(arguments);
        this.getEditorWindow().down('#observationsPanel').loadRecord(record);
    }
});