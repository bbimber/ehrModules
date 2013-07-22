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

import org.apache.commons.lang3.time.DateUtils;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.Results;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.TableInfo;
import org.labkey.api.ehr.HistoryRow;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.util.PageFlowUtil;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * User: bimber
 * Date: 2/17/13
 * Time: 4:52 PM
 */
public class DefaultTreatmentEndDataSource extends AbstractDataSource
{
    public DefaultTreatmentEndDataSource()
    {
        super("study", "Treatment Orders", "Medication Ending", "Clinical");
    }

    @Override
    protected String getHtml(Results rs, boolean redacted) throws SQLException
    {
        StringBuilder sb = new StringBuilder();

        Date start = rs.getDate(FieldKey.fromString("date"));
        Date end = rs.getDate(FieldKey.fromString("enddate"));
        if (!DateUtils.isSameDay(start, end))
        {
            sb.append(snomedToString(rs, FieldKey.fromString("code"), FieldKey.fromString("code/meaning")));

            if (start != null)
            {
                long diff = (end.getTime() - start.getTime());
                diff = diff / (1000 * 60 * 60 * 24);

                sb.append("Date Started: ").append(_dateFormat.format(start)).append(diff > 0 ? " (" + diff + " days ago)" : "").append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    protected String getDateField()
    {
        return "enddate";
    }

    @Override
    protected List<HistoryRow> getRows(Container c, User u, SimpleFilter filter, boolean redacted)
    {
        filter.addCondition(FieldKey.fromString(getDateField()), null, CompareType.NONBLANK);
        filter.addCondition(FieldKey.fromString("enddate"), new Date(), CompareType.DATE_LTE);
        return super.getRows(c, u, filter, redacted);
    }

    @Override
    protected String getCategoryText(Results rs) throws SQLException
    {
        String category = rs.getString("category");
        return category == null ?  "Medication Ending" : category + " Medication Ending";
    }

    @Override
    protected String getCategoryGroup(Results rs) throws SQLException
    {
        String category = rs.getString("category");
        return category == null ?  "Clinical" : category;
    }

    @Override
    protected Set<String> getColumnNames()
    {
        return PageFlowUtil.set("Id", "date", "enddate", "route", "volume", "vol_units", "amount", "amount_units", "code", "code/meaning", "duration", "category");
    }
}

