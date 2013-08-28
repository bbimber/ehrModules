/**
 * @param fieldConfigs
 */
Ext4.define('EHR.data.HousingClientStore', {
    extend: 'EHR.data.DataEntryClientStore',

    //TODO: automatically populate roommates column

    getExtraContext: function(){
        var map = {};
        var allRecords = this.getRange();
        for (var idx = 0; idx < allRecords.length; ++idx){
            var record = allRecords[idx];

            var date = record.get('date');
            var id = record.get('Id');
            var room = record.get('room');
            if (!id || !date || !room)
                continue;

            date = date.format('Y-m-d');

            if (!map[id])
                map[id] = [];

            map[id].push({
                objectid: record.get('objectid'),
                date: date,
                enddate: record.get('enddate'),
                qcstate: record.get('QCState'),
                room: record.get('room'),
                cage: record.get('cage')
            });
        }

        if (!LABKEY.Utils.isEmptyObj(map)){
            map = LABKEY.ExtAdapter.encode(map);

            return {
                housingInTransaction: map
            }
        }

        return null;
    }
});