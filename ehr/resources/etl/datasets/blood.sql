/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as Id, FixDateTime(date, time) AS Date, quantity, done_by, done_for, pno as project, p_s, a_v, (b.code) AS code, caretaker, tube_type, null as parentid,
     s1.meaning,
     b.ts, b.uuid AS objectid

FROM blood b

LEFT OUTER JOIN snomed s1 ON s1.code=b.code

WHERE b.ts > ?
AND length(b.id) > 1