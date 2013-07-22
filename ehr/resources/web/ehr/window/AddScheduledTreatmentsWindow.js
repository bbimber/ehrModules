/**
 * @class
 * This window will allow users to query the treatment schedule and add records to a task based on the scheduled treatments
 * that match their criteria.  It is connected to the 'Add Treatments' button in the treatments form.
 */
Ext4.define('EHR.window.AddScheduledTreatmentsWindow', {
    extend: 'Ext.window.Window',

    initComponent: function(){
        LABKEY.ExtAdapter.applyIf(this, {
            modal: true,
            title: 'Import Scheduled Treatments',
            border: true,
            bodyStyle: 'padding: 5px',
            width: 350,
            defaults: {
                width: 330,
                border: false
            },
            items: [{
                xtype: 'datefield',
                fieldLabel: 'Date',
                value: (new Date()),
                //TODO
                //hidden: !EHR.Security.hasPermission('Completed', 'update', {queryName: 'Blood Draws', schemaName: 'study'}),
                maxValue: (new Date()),
                itemId: 'dateField'
            },{
                xtype: 'ehr-roomfield',
                itemId: 'roomField'
            },{
                xtype: 'ehr-timeofdayfield',
                itemId: 'timeField'
            },{
                xtype: 'checkcombo',
                multiSelect: true,
                fieldLabel: 'Category',
                itemId: 'categoryField',
                displayField: 'category',
                valueField: 'category',
                store: {
                    type: 'array',
                    fields: ['category'],
                    data: [
                        ['Clinical'],
                        ['Surgical'],
                        ['Diet']
                    ]
                }
            },{
                xtype: 'textfield',
                fieldLabel: 'Performed By',
                value: LABKEY.Security.currentUser.displayName,
                itemId: 'performedBy'
            }],
            buttons: [{
                text:'Submit',
                itemId: 'submitBtn',
                scope: this,
                handler: this.getTreatments
            },{
                text: 'Close',
                scope: this,
                handler: function(btn){
                    btn.up('window').close();
                }
            }]
        });

        this.callParent(arguments);
    },

    getFilterArray: function(){
        var area = this.down('#areaField') ? this.down('#areaField').getValue() : null;
        var rooms = EHR.DataEntryUtils.ensureArray(this.down('#roomField').getValue()) || [];
        var times = EHR.DataEntryUtils.ensureArray(this.down('#timeField').getTimeValue()) || [];
        var categories = EHR.DataEntryUtils.ensureArray(this.down('#categoryField').getValue()) || [];

        var date = (this.down('#dateField') ? this.down('#dateField').getValue() : new Date());

        if (!rooms.length){
            alert('Must provide at least one room');
            return;
        }

        var filterArray = [];

        filterArray.push(LABKEY.Filter.create('date', date.format('Y-m-d'), LABKEY.Filter.Types.DATE_EQUAL));

        filterArray.push(LABKEY.Filter.create('treatmentStatus', null, LABKEY.Filter.Types.ISBLANK));

        if (area)
            filterArray.push(LABKEY.Filter.create('Id/curLocation/area', area, LABKEY.Filter.Types.EQUAL));

        if (rooms.length)
            filterArray.push(LABKEY.Filter.create('Id/curLocation/room', rooms.join(';'), LABKEY.Filter.Types.EQUALS_ONE_OF));

        if (categories.length)
            filterArray.push(LABKEY.Filter.create('category', categories.join(';'), LABKEY.Filter.Types.EQUALS_ONE_OF));

        if (times && times.length)
            filterArray.push(LABKEY.Filter.create('TimeOfDay', times.join(';'), LABKEY.Filter.Types.EQUALS_ONE_OF));

        return filterArray;
    },

    getTreatments: function(button){
        var filterArray = this.getFilterArray();
        if (!filterArray || !filterArray.length){
            return;
        }

        Ext4.Msg.wait("Loading...");
        this.hide();

        //find distinct animals matching criteria
        LABKEY.Query.selectRows({
            requiredVersion: 9.1,
            schemaName: 'study',
            queryName: 'treatmentSchedule',
            parameters: {
                NumDays: 1,
                StartDate: date.format('Y-m-d')
            },
            sort: 'date,Id/curlocation/room,Id/curlocation/cage,Id',
            columns: 'primaryKey,lsid,Id,date,project,meaning,code,qualifier,route,concentration,conc_units,amount,amount_units,dosage,dosage_units,volume,vol_units,remark,category',
            filterArray: filterArray,
            scope: this,
            success: this.onSuccess,
            failure: LDK.Utils.getErrorCallback()
        });

    },
    onSuccess: function(results){
        if (!results || !results.rows || !results.rows.length){
            Ext4.Msg.hide();
            Ext4.Msg.alert('', 'No uncompleted treatments were found.');
            return;
        }

        LDK.Assert.assertNotEmpty('Unable to find targetStore in AddScheduledTreatmentsWindow', this.targetStore);

        var records = [];
        var performedby = this.down('#performedBy').getValue();

        Ext4.Array.each(results.rows, function(sr){
            var row = new LDK.SelectRowsRow(sr);

            row.date = row.getDateValue('date');
            var date = new Date();

            //if retroactively entering, we take the time that record was ordered.  otherwise we use the current time
            if(row.date.getDate() != date.getDate() || row.date.getMonth() != date.getMonth() || row.date.getFullYear() != date.getFullYear())
                date = row.date;

            records.push(this.targetStore.createModel({
                Id: row.getValue('Id'),
                date: date,
                project: row.getValue('project'),
                code: row.getValue('code'),
                qualifier: row.getValue('qualifier'),
                route: row.getValue('route'),
                concentration: row.getValue('concentration'),
                conc_units: row.getValue('conc_units'),
                amount: row.getValue('amount'),
                amount_units: row.getValue('amount_units'),
                volume: row.getValue('volume'),
                vol_units: row.getValue('vol_units'),
                dosage: row.getValue('dosage'),
                dosage_units: row.getValue('dosage_units'),
                treatmentid: row.getValue('treatmentid'),
                performedby: performedby,
                remark: row.getValue('remark'),
                category: row.getValue('category')
            }));
        }, this);

        this.targetStore.add(records);

        Ext4.Msg.hide();
    }
});

EHR.DataEntryUtils.registerGridButton('ADDTREATMENTS', function(config){
    return Ext4.Object.merge({
        text: 'Add Scheduled Treatments',
        tooltip: 'Click to add a scheduled treatments',
        handler: function(btn){
            var grid = btn.up('gridpanel');
            if(!grid.store || !grid.store.hasLoaded()){
                console.log('no store or store hasnt loaded');
                return;
            }

            var cellEditing = grid.getPlugin('cellediting');
            if(cellEditing)
                cellEditing.completeEdit();

            Ext4.create('EHR.window.AddScheduledTreatmentsWindow', {
                targetStore: grid.store
            }).show();
        }
    }, config);
});
