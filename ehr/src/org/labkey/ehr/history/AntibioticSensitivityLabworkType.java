/*
 * Copyright (c) 2013 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.ehr.history;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Results;
import org.labkey.api.data.ResultsImpl;
import org.labkey.api.data.Selector;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.util.PageFlowUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * User: bimber
 * Date: 3/6/13
 * Time: 12:26 PM
 */
public class AntibioticSensitivityLabworkType extends DefaultLabworkType
{
    private String _tissueField = "tissue/meaning";
    private String _microbeField = "microbe/meaning";
    private String _antibioticField = "antibiotic/meaning";

    public AntibioticSensitivityLabworkType()
    {
        super("Antibiotic Sensitivity", "study", "Antibiotic Sensitivity");
        _resultField = "resistant";
    }

    @Override
    protected Set<String> getColumnNames()
    {
        return PageFlowUtil.set(_idField, _dateField, _runIdField, _testIdField, _resultField, _antibioticField, _microbeField, _tissueField);
    }

    @Override
    protected String getLine(Results rs) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        String microbe = rs.getString(FieldKey.fromString(_microbeField));
        Boolean result = rs.getBoolean(FieldKey.fromString(_resultField));
        String resultText = "";
        if (result != null)
            resultText = (!result ? "Not " : "") + "Resistant";

        String antibiotic = rs.getString(FieldKey.fromString(_antibioticField));

        String delim = "";

        if (antibiotic != null)
        {
            sb.append("<td>");
            sb.append(delim).append(antibiotic);
            sb.append("</td><td>");

            if (result != null)
                sb.append(delim).append(resultText);

            sb.append("</td>");
        }

        return sb.toString();
    }

    @Override
    protected Map<String, List<String>> getRows(TableSelector ts, final Collection<ColumnInfo> cols)
    {
        final Map<String, Map<String, List<String>>> rows = new HashMap<String, Map<String, List<String>>>();
        ts.forEach(new Selector.ForEachBlock<ResultSet>()
        {
            @Override
            public void exec(ResultSet object) throws SQLException
            {
                Results rs = new ResultsImpl(object, cols);
                String runId = rs.getString(FieldKey.fromString("runId"));

                Map<String, List<String>> runMap = rows.get(runId);
                if (runMap == null)
                    runMap = new TreeMap<String, List<String>>();

                String microbe = rs.getString(FieldKey.fromString(_microbeField));
                if (microbe != null)
                {
                    List<String> list = runMap.get(microbe);
                    if (list == null)
                        list = new ArrayList<String>();

                    String line = getLine(rs);
                    if (line != null)
                        list.add(line);

                    runMap.put(microbe, list);
                }

                rows.put(runId, runMap);
            }
        });

        Map<String, List<String>> sortedRows = new HashMap<String, List<String>>();
        for (String runId : rows.keySet())
        {
            Map<String, List<String>> runMap = rows.get(runId);
            List<String> lines = new ArrayList<String>();

            for (String microbe : runMap.keySet())
            {
                StringBuilder sb = new StringBuilder();
                sb.append("\n").append("Microbe: ").append(microbe).append("\n");
                sb.append("<table>");
                for (String line : runMap.get(microbe))
                {
                    sb.append("<tr>").append(line).append("</tr>");
                }
                sb.append("</table>");
                lines.add(sb.toString());
            }

            sortedRows.put(runId, lines);
        }

        return sortedRows;
    }
}
