Ext4.define('EHR.panel.TaskDataEntryPanel', {
    extend: 'EHR.panel.DataEntryPanel',
    alias: 'widget.ehr-taskdataentrypanel',

    taskId: null,

    initComponent: function(){
        this.taskId = this.taskId || LABKEY.Utils.generateUUID();

        this.callParent();
    },

    configureStore: function(cfg){
        cfg = this.callParent(arguments);
        cfg.filterArray = cfg.filterArray || [];
        cfg.filterArray.push(LABKEY.Filter.create('taskId', this.taskId, LABKEY.Filter.Types.EQUALS));

        return cfg;
    }
});