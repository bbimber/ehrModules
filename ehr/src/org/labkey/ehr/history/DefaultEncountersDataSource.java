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

import org.labkey.api.data.Results;
import org.labkey.api.query.FieldKey;
import org.labkey.api.util.PageFlowUtil;

import java.sql.SQLException;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: bimber
 * Date: 2/17/13
 * Time: 4:52 PM
 */
public class DefaultEncountersDataSource extends AbstractDataSource
{
    public DefaultEncountersDataSource()
    {
        super("study", "Clinical Encounters", "Encounter");
    }

    @Override
    protected String getHtml(Results rs) throws SQLException
    {
        StringBuilder sb = new StringBuilder();
        //TODO: switch based on type

        sb.append(safeAppend(rs, "Title", "title"));
        sb.append(safeAppend(rs, "Procedure", "procedureid/name"));

        if (rs.hasColumn(FieldKey.fromString("major")) && rs.getObject("major") != null)
        {
            Boolean value = rs.getBoolean("major");
            sb.append("Major Surgery? ").append(value).append("\n");
        }

        sb.append(safeAppend(rs, "Summary", "summaries/summary"));

        if (sb.length() > 0)
        {
            return sb.toString();
        }

        return null;
    }

    @Override
    protected String getCategory(Results rs) throws SQLException
    {
        String category = rs.getString("type");
        return category == null ? "Encounter" : category;
    }

    @Override
    protected Set<String> getColumnNames()
    {
        return PageFlowUtil.set("Id", "date", "enddate", "major", "type", "title", "procedureid", "procedureid/name", "summaries/summary");
    }
}