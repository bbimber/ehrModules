/*
 * Copyright (c) 2013-2019 LabKey Corporation
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
package org.labkey.ehr.security;

import org.labkey.api.ehr.security.EHRDataEntryPermission;
import org.labkey.api.ehr.security.EHRRequestPermission;
import org.labkey.api.security.permissions.DeletePermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.SeeGroupDetailsPermission;
import org.labkey.api.security.permissions.SeeUserDetailsPermission;
import org.labkey.api.security.permissions.UpdatePermission;

/**
 * User: bimber
 * Date: 1/17/13
 * Time: 7:42 PM
 */
public class EHRDataEntryRole extends AbstractEHRRole
{
    public EHRDataEntryRole()
    {
        super("EHR Data Entry", "This role is required in order to submit data into any EHR table; however, having this role alone is not sufficient for data entry.  Per-table permissions are set through study admin.",
            ReadPermission.class,
            InsertPermission.class,
            UpdatePermission.class,
            DeletePermission.class,
            EHRDataEntryPermission.class,
            EHRRequestPermission.class,
            SeeUserDetailsPermission.class,
            SeeGroupDetailsPermission.class
        );
    }
}
