/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

SELECT lower(id) as id, FixDate(date) AS Date, lower(account) AS account, FixNewlines(remark) AS remark, FixNewlines(clinremark),
uuid as requestId,
ts,
uuid AS objectid
FROM hematology
WHERE ts > ?
