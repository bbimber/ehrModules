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
package org.labkey.api.ehr_billing.pipeline;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.services.ServiceRegistry;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service to get a list of queries to be processed during a Billing Run. The listing is a collection of
 * BillingPipelineJobProcess objects that define what schema.query to execute and the mapping from that query's
 * columns to the ehr_billing.invoicedItem table's columns.
 * Additionally, get center specific generated invoice number.
 */
public interface InvoicedItemsProcessingService
{
    @Nullable
    static InvoicedItemsProcessingService get()
    {
        return ServiceRegistry.get().getService(InvoicedItemsProcessingService.class);
    }

    default List<BillingPipelineJobProcess> getProcessList()
    {
        return Collections.emptyList();
    }

    /**
     * Generate invoice number for a billing task processed row and billing period date.
     * @param row the billing task processed row to get values from
     * @param billingPeriodDate a date within a billing period
     * @return generated invoice num
     */
    String getInvoiceNum(Map<String, Object> row, Date billingPeriodDate);
}