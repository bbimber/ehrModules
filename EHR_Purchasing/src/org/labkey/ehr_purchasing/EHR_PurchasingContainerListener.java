/*
 * Copyright (c) 2020 LabKey Corporation
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

package org.labkey.ehr_purchasing;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager.ContainerListener;
import org.labkey.api.data.DbScope;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.security.User;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;

public class EHR_PurchasingContainerListener implements ContainerListener
{
    @Override
    public void containerCreated(Container c, User user)
    {
    }

    @Override
    public void containerDeleted(Container c, User user)
    {
        // This will clean up the ehr_purchasing schema.  For extensible tables, exp module should clean up related exp data.
        DbScope scope = EHR_PurchasingSchema.getInstance().getSchema().getScope();
        SimpleFilter containerFilter = SimpleFilter.createContainerFilter(c);
        try (DbScope.Transaction transaction = scope.ensureTransaction())
        {
            TableInfo userAccountAssociationsTable = EHR_PurchasingSchema.getInstance().getUserAccountAssociationsTable();
            Table.delete(userAccountAssociationsTable, containerFilter);

            TableInfo lineItemsTable = EHR_PurchasingSchema.getInstance().getLineItemsTable();
            Table.delete(lineItemsTable, containerFilter);

            TableInfo lineItemStatusTable = EHR_PurchasingSchema.getInstance().getLineItemStatusTable();
            Table.delete(lineItemStatusTable, containerFilter);

            TableInfo purchasingRequestsTable = EHR_PurchasingSchema.getInstance().getPurchasingRequestsTable();
            Table.delete(purchasingRequestsTable, containerFilter);

            TableInfo unitsTable = EHR_PurchasingSchema.getInstance().getItemUnitsTable();
            Table.delete(unitsTable, containerFilter);

            TableInfo vendorTable = EHR_PurchasingSchema.getInstance().getVendorTable();
            Table.delete(vendorTable, containerFilter);

            TableInfo shippingInfoTable = EHR_PurchasingSchema.getInstance().getShippingInfoTable();
            Table.delete(shippingInfoTable, containerFilter);

            transaction.commit();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
    }

    @Override
    public void containerMoved(Container c, Container oldParent, User user)
    {
    }

    @NotNull @Override
    public Collection<String> canMove(Container c, Container newParent, User user)
    {
        return Collections.emptyList();
    }
}