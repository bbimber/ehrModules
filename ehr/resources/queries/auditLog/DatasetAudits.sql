/*
 * Copyright (c) 2011 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

SELECT *

FROM auditLog.audit a

WHERE a.eventType = 'DatasetAuditEvent'