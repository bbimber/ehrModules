/*
 * Copyright (c) 2013-2016 LabKey Corporation
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

import org.labkey.api.data.Container;
import org.labkey.api.data.Results;
import org.labkey.api.ehr.history.AbstractDataSource;
import org.labkey.api.module.Module;
import org.labkey.api.util.PageFlowUtil;

import java.sql.SQLException;
import java.util.Set;

/**
 * User: bimber
 * Date: 2/17/13
 * Time: 4:52 PM
 */
public class DefaultPregnanciesDataSource extends AbstractDataSource
{
    public DefaultPregnanciesDataSource(Module module)
    {
        super("study", "Pregnancy Confirmations", "Pregnancy Confirmation", "Pregnancy Confirmations", module);
    }

    @Override
    protected String getHtml(Container c, Results rs, boolean redacted) throws SQLException
    {
        StringBuilder sb = new StringBuilder();

        sb.append(safeAppend(rs, "Sire", "sire"));
        sb.append(safeAppend(rs, "Confirmation Type", "confirmationType/value"));
        sb.append(safeAppend(rs, "Estimated Delivery Date", "estDeliveryDate"));
        sb.append(safeAppend(rs, "Remark", "remark"));

        return sb.toString();
    }

    @Override
    protected Set<String> getColumnNames()
    {
        return PageFlowUtil.set("Id", "date", "enddate", "sire", "confirmationType", "confirmationType/value", "estDeliveryDate", "remark");
    }
}
