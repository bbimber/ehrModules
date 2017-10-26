package org.labkey.ehr_billing;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;

public class EHR_BillingUserSchema extends SimpleUserSchema
{
    public EHR_BillingUserSchema(String name, @Nullable String description, User user, Container container, DbSchema dbschema)
    {
        super(name, description, user, container, dbschema);
    }

    public enum TableType
    {
        Aliases
        {
            @Override
            public TableInfo createTable(EHR_BillingUserSchema schema)
            {
                SimpleUserSchema.SimpleTable<EHR_BillingUserSchema> table =
                        new SimpleUserSchema.SimpleTable<>(
                                schema, EHR_BillingSchema.getInstance().getAliasesTable()).init();

                return table;
            }
        },
        ChargeRates
        {
            @Override
            public TableInfo createTable(EHR_BillingUserSchema schema)
            {
                SimpleUserSchema.SimpleTable<EHR_BillingUserSchema> table =
                        new SimpleUserSchema.SimpleTable<>(
                                schema, EHR_BillingSchema.getInstance().getChargeRatesTable()).init();

                return table;
            }
        };

        public abstract TableInfo createTable(EHR_BillingUserSchema schema);
    }
}