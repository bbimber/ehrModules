/**
 *
 */
Ext4.define('EHR.data.PairingClientStore', {
    extend: 'EHR.data.DataEntryClientStore',

    constructor: function(){
        this.callParent(arguments);

        this.on('add', this.onAddRecord, this);
    },

    onAddRecord: function(store, records){
        Ext4.each(records, function(record){
            this.onRecordUpdate(record, ['lowestcage']);
        }, this);
    },

    afterEdit: function(record, modifiedFieldNames){
        this.onRecordUpdate(record, modifiedFieldNames);

        this.callParent(arguments);
    },

    getCageMap: function(toSkip){
        var map = {
            lowest: {},
            pairids: {}
        };
        this.each(function(r){
            if (r == toSkip){
                return;
            }

            var lowest;
            if (r.get('lowestcage') && r.get('room')){
                lowest = r.get('room') + '||' + r.get('room')
            }

            var pairid = r.get('pairid');

            if (lowest && pairid){
                //find discrepancies in either pairIds or lowestCages
                if (!map.lowest[lowest])
                    map.lowest[lowest] = pairid;

                if (pairid != map.lowest[lowest]){
                    LDK.Assert.assertEquality('Mismatched pairIds for cage: ' + lowest, r.get('pairid'), map.lowest[lowest]);

                    r.beginEdit();
                    r.set('pairid', map.lowest[lowest]);
                    r.endEdit(true);
                }

                if (!map.pairids[pairid])
                    map.pairids[pairid] = lowest;

                if (lowest != map.pairids[pairid]){
                    LDK.Assert.assertEquality('Mismatched pairIds for cage: ' + lowest, lowest, map.pairids[pairid]);
                }
            }
        }, this);

        return map;
    },

    onRecordUpdate: function(record, modifiedFieldNames){
        modifiedFieldNames = modifiedFieldNames || [];
        var lowest;
        if (record.get('lowestcage') && record.get('room')){
            lowest = record.get('room') + '||' + record.get('room')
        }

        var params = {};
        if (lowest){
            var map = this.getCageMap(record);
            var pairid = record.get('pairid');

            if (!pairid){
                if (map.lowest[lowest])
                    params.pairid = map.lowest[lowest];
                else
                    params.pairid = LABKEY.Utils.generateUUID();
            }
            else {
                if (map.lowest[lowest] && map.lowest[lowest] != pairid) {
                    params.pairid = map.lowest[lowest];
                }
                //verify we dont have a duplicate objectid
                else if (map.pairids[pairid] && map.pairids[pairid] != pairid) {
                    //if the cage changed, assume we need to update the pairid
                    if (modifiedFieldNames.indexOf('lowestcage') > -1){
                        params.pairid = LABKEY.Utils.generateUUID();
                    }
                }
            }
        }
        else if (pairid && !lowest){
            params.pairid = null;
        }

        if (!LABKEY.Utils.isEmptyObj(params)){
            record.beginEdit();
            record.set(params);
            record.endEdit(true);
        }
    }
});
