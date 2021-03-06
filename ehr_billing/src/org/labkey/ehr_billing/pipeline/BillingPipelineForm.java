/*
 * Copyright (c) 2018-2019 LabKey Corporation
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
package org.labkey.ehr_billing.pipeline;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.util.Pair;

import java.util.Date;

/** Captures user's description of the billing to be performed as part of the requested invoice run. */
public class BillingPipelineForm
{
    private String _protocolName;
    private Date _startDate;
    private Date _endDate;
    private String _comment;
    private Pair<String,String> _previousInvoice; // Pair of previous invoice objectId and rowId

    public String getProtocolName()
    {
        return _protocolName;
    }

    public void setProtocolName(String protocolName)
    {
        _protocolName = protocolName;
    }

    public Date getStartDate()
    {
        return _startDate;
    }

    public void setStartDate(Date startDate)
    {
        _startDate = startDate;
    }

    public Date getEndDate()
    {
        return _endDate;
    }

    public void setEndDate(Date endDate)
    {
        _endDate = endDate;
    }

    public String getComment()
    {
        return _comment;
    }

    public void setComment(String comment)
    {
        _comment = comment;
    }

    @Nullable
    public Pair<String, String> getPreviousInvoice()
    {
        return _previousInvoice;
    }

    public void setPreviousInvoice(Pair<String, String> previousInvoice)
    {
        _previousInvoice = previousInvoice;
    }
}
