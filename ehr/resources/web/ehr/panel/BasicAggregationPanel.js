Ext4.define('EHR.panel.BasicAggregationPanel', {
    extend: 'Ext.panel.Panel',

    aggregateResults: function(results, fieldName){
        if (!results || !results.rows || !results.rows.length)
            return;

        var object = {
            total: results.rows.length,
            aggregated: {},
            aggregateField: fieldName
        };

        Ext4.each(results.rows, function(row){
            var rs = new LDK.SelectRowsRow(row);
            var val = rs.getDisplayValue(fieldName);
            if (!object.aggregated[val])
                object.aggregated[val] = 0;

            object.aggregated[val]++;
        }, this);

        return object;
    },

    appendSection: function(label, data, filterCol, operator){
        if (!data){
            return null;
        }

        var keys = Ext4.Object.getKeys(data.aggregated).sort();

        var rows = [];
        Ext4.each(keys, function(key){
            var rowLabel = key;
            rowLabel = Ext4.isEmpty(rowLabel) ? 'None' : rowLabel;
            if (rowLabel)
                rowLabel = rowLabel.replace(/\n/g, ' / ');

            rows.push({
                html: rowLabel,
                style: 'padding-right: 10px;'
            });

            var val = data.aggregated[key];
            var displayVal = Ext4.isDefined(val) ? val.toString() : '';
            if (displayVal){
                var url = this.generateUrl(displayVal, key, data, filterCol, operator);
                if (url)
                    displayVal =  '<a href="' + url + '">' + displayVal + '</a>';
            }

            rows.push({
                html:  displayVal,
                style: 'text-align: center;padding-right: 10px;'
            });

            var pct;
            if (Ext4.isDefined(val)){
                pct = val / this.demographicsData.rowCount;
                pct = pct * 100;
                pct = Ext4.util.Format.round(pct, 2);
                pct = pct.toString();
            }
            else {
                pct = '';
            }

            rows.push({
                html: pct,
                style: 'text-align: center;'
            });
        }, this);

        return {
            border: false,
            style: 'padding-left: 10px;',
            defaults: {
                border: false
            },
            items: [{
                html: '<i>' + label + ':</i>',
                style: 'padding-bottom: 5px;'
            },{
                style: 'padding-left: 10px;padding-bottom: 10px;',
                layout: {
                    type: 'table',
                    columns: 3
                },
                border: false,
                defaults: {
                    border: false
                },
                items: [{
                    html: 'Category',
                    style: 'border-bottom: solid 1px;margin-right: 3px;margin-bottom:3px;'
                },{
                    html: 'Total',
                    style: 'border-bottom: solid 1px;margin-right: 3px;margin-left: 3px;margin-bottom:3px;text-align: center;padding-right: 10px;',
                    width: 80
                },{
                    html: '%',
                    style: 'border-bottom: solid 1px;margin-right: 3px;margin-left: 3px;margin-bottom:3px;text-align: center;padding-right: 10px;',
                    width: 80
                }].concat(rows)
            }]
        };
    },

    generateUrl: function(val, key, data, filterCol, operator){
        if (!val)
            return null;

        operator = operator || 'eq';

        var params = {
            schemaName: 'study',
            'query.queryName': 'Demographics',
            'query.viewName': 'By Location'
        };
        params = this.appendFilterParams(params);
        params['query.' + filterCol + '~' + operator] = key;

        return LABKEY.ActionURL.buildURL('query', 'executeQuery', null, params);
    },

    appendFilterParams: function(params){
        Ext4.each(this.filterArray, function(filter){
            params[filter.getURLParameterName()] = filter.getURLParameterValue();
        }, this);
        return params;
    }
})