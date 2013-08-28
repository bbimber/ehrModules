/**
 * @cfg targetGrid
 * @cfg records
 **/
Ext4.define('EHR.window.GuessProjectWindow', {
    extend: 'Ext.window.Window',

    initComponent: function(){
        LABKEY.ExtAdapter.applyIf(this, {
            modal: true,
            width: 750,
            closeAction: 'destroy',
            title: 'Guess Projects',
            bodyStyle: 'padding: 5px;',
            defaults: {
                border: false
            },
            items: [{
                html: 'This helper allows you to set the project on the selected records, based on the current assignments for the animal.  If the animal is currently assigned to a single research project, it will pick that project.  If assigned to both a research project and a center resource, it will pick the research project.  Otherwise it will allow you to choose.',
                style: 'margin-bottom: 10px;'
            },{
                itemId: 'projects',
                html: 'Loading...'
            }],
            buttons: [{
                text: 'Submit',
                itemId: 'submitBtn',
                disabled: true,
                scope: this,
                handler: this.onSubmit
            },{
                text: 'Close',
                handler: function(btn){
                    btn.up('window').close();
                }
            }]
        });

        this.callParent();

        this.getAssignments(this.getIds())
    },

    getRecords: function(){
        if (this.records)
            return this.records;
        else
            return this.targetGrid.getRange();
    },

    getIds: function(){
        var ids = [];
        Ext4.Array.forEach(this.getRecords(), function(r){
            ids.push(r.get('Id'));
        }, this);

        ids = Ext4.Array.unique(ids);
        ids = ids.sort();

        return ids;
    },

    getAssignments: function(ids){
        EHR.DemographicsCache.getDemographics(ids, this.onLoad, this);
    },

    onLoad: function(ids, resultMap){
        var animalIds = this.getIds();
        var items = [];
        Ext4.Array.forEach(animalIds, function(id){
            if (!resultMap[id]){
                items.push({
                    xtype: 'displayfield',
                    fieldLabel: id,
                    value: 'Animal not found'
                });
            }
            else {
                var projects = resultMap[id].getActiveAssignments();
                if (!projects || !projects.length){
                    items.push({
                        xtype: 'displayfield',
                        fieldLabel: id,
                        value: 'No assignments found'
                    });
                }
                else {
                    var researchProjects = [];
                    Ext4.Array.forEach(projects, function(p){
                        if (p['project/use_category'] == 'Research'){
                            researchProjects.push(p);
                        }
                    }, this);

                    var value = null;
                    if (researchProjects.length == 1){
                        value = researchProjects[0].project;
                    }
                    else if (projects.length == 1){
                        value = projects[0].project;
                    }

                    items.push(this.getProjectComboCfg(id, projects, value));
                }
            }
        }, this);

        var target = this.down('#projects');
        target.removeAll();
        target.add(items);

        this.down('#submitBtn').setDisabled(false);

    },

    getProjectComboCfg: function(id, data, value){
        return {
            xtype: 'labkey-combo',
            fieldLabel: id,
            animalId: id,
            displayField: 'project/displayName',
            valueField: 'project',
            queryMode: 'local',
            forceSelection: true,
            listConfig: {
                innerTpl: ['<span style="white-space:nowrap;">{[values["project/displayName"] + " " + (values["project/investigatorId/lastName"] ? "(" + values["project/investigatorId/lastName"] + ")" : "")]}&nbsp;</span>']
            },
            allowBlank: true,
            store: {
                xtype: 'store',
                fields: ['project', 'project/displayName', 'project/investigatorId/lastName'],
                data: data
            },
            value: value
        }
    },

    onSubmit: function(){
        var projectMap = {};
        this.down('#projects').items.each(function(item){
            if (item.isXType('combo')){
                projectMap[item.animalId] = item.getValue();
            }
        }, this);

        Ext4.Array.forEach(this.getRecords(), function(r){
            var id = r.get('Id');
            if (projectMap[id]){
                r.set('project', projectMap[id]);
            }
        }, this);

        this.close();
    }
});