/*
 * Copyright (c) 2016-2019 LabKey Corporation
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
package org.labkey.api.ehr.history;

import org.labkey.api.data.Container;
import org.labkey.api.data.Results;
import org.labkey.api.module.Module;
import org.labkey.api.query.FieldKey;

import java.sql.SQLException;

/**
 * User: bimber
 * Date: 2/17/13
 * Time: 4:52 PM
 */
public class DefaultBirthDataSource extends AbstractDataSource
{
    public DefaultBirthDataSource(Module module)
    {
        super("study", "Birth", "Birth", "Births", module);
    }

    @Override
    protected String getHtml(Container c, Results rs, boolean redacted) throws SQLException
    {
        StringBuilder sb = new StringBuilder();

        if(rs.hasColumn(FieldKey.fromString("conception")) && rs.getObject("conception") != null)
            sb.append("Conception: " + rs.getString("conception"));

        if(rs.hasColumn(FieldKey.fromString("gender")) && rs.getObject("gender") != null)
            sb.append("Gender: " + rs.getString("gender"));

        return sb.toString();
    }
}
