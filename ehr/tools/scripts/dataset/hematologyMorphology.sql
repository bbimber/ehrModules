/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDate(date) AS Date, morphology AS morphology, lower(account) AS account,
uuid as requestId,

(select UUID FROM hematology t2 WHERE t1.id=t2.id and t1.date=t2.date limit 1) as runId
FROM hemamisc t1

