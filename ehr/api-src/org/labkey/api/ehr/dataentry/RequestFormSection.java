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
package org.labkey.api.ehr.dataentry;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Form section bound to the ehr.requests table that serves as a parent row to all of the other sections
 * of the form, and tracks the assignee and status of the whole request.
 * User: bimber
 * Date: 6/9/13
 */
public class RequestFormSection extends SimpleFormSection
{
    public RequestFormSection()
    {
        super("ehr", "requests", "Request", "ehr-formpanel");
        setConfigSources(Collections.singletonList("Task"));
        setTemplateMode(TEMPLATE_MODE.NONE);
        setSupportFormSort(false);
    }

    @Override
    public JSONObject toJSON(DataEntryFormContext ctx, boolean includeFormElements)
    {
        JSONObject ret = super.toJSON(ctx, includeFormElements);

        Map<String, Object> formConfig = new HashMap<String, Object>();
        Map<String, Object> bindConfig = new HashMap<String, Object>();
        bindConfig.put("createRecordOnLoad", true);
        formConfig.put("bindConfig", bindConfig);
        ret.put("formConfig", formConfig);

        return ret;
    }
}
