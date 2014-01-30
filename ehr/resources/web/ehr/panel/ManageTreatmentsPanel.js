/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * @cfg {String} animalId
 */
Ext4.define('EHR.panel.ManageTreatmentsPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.ehr-managetreatmentspanel',

    statics: {
        getOrderTreatmentButtonConfig: function(owner){
            return [{
                xtype: 'button',
                text: 'Order Treatment',
                disabled: !EHR.Security.hasPermission(EHR.QCStates.COMPLETED, 'insert', [{schemaName: 'study', queryName: 'Treatment Orders'}]),
                handler: function(btn){
                    EHR.panel.ManageTreatmentsPanel.createTreatmentWindow(btn, {
                        listeners: {
                            scope: this,
                            save: function(){
                                var win = btn.up('window');
                                var panel;
                                if (win){
                                    panel = win.down('ehr-managetreatmentspanel');
                                }
                                else {
                                    panel = btn.up('ehr-managetreatmentspanel');
                                }

                                panel.down('grid').store.load();
                            }
                        }
                    }, (owner ? owner.animalId : null));
                }
            }]
        },

        createTreatmentWindow: function(btn, config, animalId){
            var cfg = LABKEY.ExtAdapter.apply({
                schemaName: 'study',
                queryName: 'treatment_order',
                pkCol: 'objectid',
                pkValue: LABKEY.Utils.generateUUID(),
                extraMetaData: {
                    Id: {
                        defaultValue: animalId,
                        editable: false
                    }
                }
            }, config);

            Ext4.create('EHR.window.ManageRecordWindow', cfg).show();
        },

        getActionBtnMenu: function(rec){
            return Ext4.create('Ext.menu.Menu', {
                items: [{
                    text: 'Change End Date',
                    disabled: !EHR.Security.hasPermission(EHR.QCStates.COMPLETED, 'update', [{schemaName: 'study', queryName: 'Treatment Orders'}]),
                    handler: function(btn){
                        Ext4.create('Ext.window.Window', {
                            modal: true,
                            closeAction: 'destroy',
                            bodyStyle: 'padding: 5px',
                            width: 420,
                            items: [{
                                xtype: 'xdatetime',
                                itemId: 'dateField',
                                width: 400,
                                fieldLabel: 'End Date',
                                minValue: new Date(),
                                value: rec.get('enddate')
                            }],
                            buttons: [{
                                text: 'Submit',
                                handler: function(btn){
                                    var win = btn.up('window');
                                    var val = win.down('#dateField').getValue();
                                    if (!val){
                                        Ext4.Msg.alert('Error', 'Must choose a date');
                                        return;
                                    }

                                    rec.set('enddate', val);
                                    rec.store.sync();

                                    win.close();
                                }
                            },{
                                text: 'Cancel',
                                handler: function(btn){
                                    btn.up('window').close();
                                }
                            }]
                        }).show();
                    }
                },{
                    text: 'Edit Treatment',
                    disabled: !EHR.Security.hasPermission(EHR.QCStates.COMPLETED, 'update', [{schemaName: 'study', queryName: 'Treatment Orders'}]),
                    handler: function(btn){
                        EHR.panel.ManageTreatmentsPanel.createTreatmentWindow(btn, {
                            pkCol: 'objectid',
                            pkValue: rec.get('objectid'),
                            listeners: {
                                scope: this,
                                save: function(){
                                    rec.store.load();
                                }
                            }
                        }, this.animalId);
                    }
                }]
            });
        }
    },

    initComponent: function(){
        Ext4.apply(this, {
            border: false,
            items: [this.getGridConfig()],
            buttons: this.hideButtons ? null : this.getButtonConfig(this)
        });

        this.callParent();
    },

    getStore: function(){
        if (this.store)
            return this.store;

        this.store = Ext4.create('LABKEY.ext4.data.Store', {
            schemaName: 'study',
            queryName: 'treatment_order',
            columns: 'lsid,objectid,Id,date,enddate,project,category,remark,performedby,code,route,amountAndVolume',
            filterArray: [
                LABKEY.Filter.create('Id', this.animalId, LABKEY.Filter.Types.EQUAL),
                LABKEY.Filter.create('isActive', true, LABKEY.Filter.Types.EQUAL)
            ],
            autoLoad: true,
            listeners: {
                scope: this,
                synccomplete: function(store){
                    var grid = this.down('grid');
                    if (grid)
                        grid.getView().refresh();

                    EHR.DemographicsCache.clearCache(this.animalId);
                }
            }
        });

        return this.store;
    },

    getGridConfig: function(){
        return {
            xtype: 'grid',
            border: true,
            store: this.getStore(),
            columns: [{
                xtype: 'actioncolumn',
                width: 40,
                icon: LABKEY.ActionURL.getContextPath() + '/_images/editprops.png',
                tooltip: 'Edit',
                handler: function(view, rowIndex, colIndex, item, e, rec){
                    EHR.panel.ManageTreatmentsPanel.getActionBtnMenu(rec).showAt(e.getXY());
                }
            },{
                header: 'Date',
                xtype: 'datecolumn',
                width: 160,
                format: 'Y-m-d H:i',
                dataIndex: 'date'
            },{
                header: 'End Date',
                xtype: 'datecolumn',
                width: 160,
                format: 'Y-m-d H:i',
                dataIndex: 'enddate'
            },{
                header: 'Code',
                width: 300,
                dataIndex: 'code',
                tdCls: 'ldk-wrap-text',
                renderer: function(value, cellMetaData, record){
                    if(record && record.raw && record.raw['code']){
                        if(Ext4.isDefined(record.raw['code'].displayValue))
                            return record.raw['code'].displayValue;
                    }

                    return value;
                }
            },{
                header: 'Frequency',
                width: 160,
                dataIndex: 'frequency'
            },{
                header: 'Route',
                width: 60,
                dataIndex: 'route'
            },{
                header: 'Amount',
                width: 120,
                dataIndex: 'amountAndVolume'
            },{
                header: 'Ordered By',
                width: 160,
                dataIndex: 'performedby'
            }]
        }
    }
});