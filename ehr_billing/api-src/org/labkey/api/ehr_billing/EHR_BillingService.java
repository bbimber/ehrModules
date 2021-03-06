/*
 * Copyright (c) 2019 LabKey Corporation
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
package org.labkey.api.ehr_billing;

import org.labkey.api.data.Container;

abstract public class EHR_BillingService
{
    static EHR_BillingService instance;

    public static EHR_BillingService get()
    {
        return instance;
    }

    static public void setInstance(EHR_BillingService instance)
    {
        EHR_BillingService.instance = instance;
    }

    /**
     * @return the container holding the Billing data, as defined by the passed container's module property 'BillingContainer'
     */
    abstract public Container getEHRBillingContainer(Container c);
}
