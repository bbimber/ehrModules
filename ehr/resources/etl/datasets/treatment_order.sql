/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDate(date) AS Date, (pno) AS pno, (t.code) AS code, (t.meaning) AS meaning, (volume) AS volume, (vunits) AS vunits, (conc) AS conc, (cunits) AS cunits, (amount) AS amount, (units) AS units, (route) AS route, FixDate(enddate) AS enddate, (frequency) AS frequency, FixNewlines(remark) AS remark, (userid) AS userid,
s1.meaning,
t.ts, t.uuid AS objectid

FROM treatments t
WHERE ts > ?
LEFT OUTER JOIN snomed s1 ON s1.code=t.code
