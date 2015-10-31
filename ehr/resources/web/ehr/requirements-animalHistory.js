requirejs(["module", "pageapp"], function(module) {

    Ext4.onReady(function (){
        var webpart = webpartContext;
        var ctx = EHR.Utils.getEHRContext(webpart.wrapperDivId, ['DefaultAnimalHistoryReport']);
        if(!ctx)
            return;

        Ext4.create('EHR.panel.AnimalHistoryPanel', {
            defaultReport: ctx.DefaultAnimalHistoryReport,
            defaultTab: 'General',
            renderTo: webpart.wrapperDivId
        });
    });
});