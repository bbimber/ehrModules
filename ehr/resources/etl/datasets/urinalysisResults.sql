/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDate(date) AS Date, TestID, stringResults, result, units, remark,
     ( CONCAT_WS(',\n',
     CONCAT('Test: ', TestId),
     CONCAT('Value: ', Result, ' ', units)
     ) ) AS Description,
     ts, objectid, runId

FROM

(

SELECT
id,
date,
'bilirubin' as TestID,
bilirubin as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'bilirubin') as objectid, ts,
uuid as runId
FROM urine
where bilirubin is not null and bilirubin != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'ketone' as TestID,
ketone as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'ketone') as objectid, ts,
uuid as runId
FROM urine
where ketone is not null and ketone != ""
AND ts > ?


UNION ALL

SELECT
id,
date,
'sp_gravity' as TestID,
null as stringResults,
sp_gravity as result,
null as Units,
null as remark,
concat(uuid,'sp_gravity') as objectid, ts,
uuid as runId
FROM urine
where sp_gravity is not null and sp_gravity != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'blood' as TestID,
blood as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'blood') as objectid, ts,
uuid as runId
FROM urine
where blood is not null and blood != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'ph' as TestID,
null as stringResults,
ph as result,
null as Units,
null as remark,
concat(uuid,'ph') as objectid, ts,
uuid as runId
FROM urine
where ph is not null and ph != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'protein' as TestID,
protein as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'protein') as objectid, ts,
uuid as runId
FROM urine
where protein is not null and protein != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'urobilinogen' as TestID,
urobilinogen as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'urobilinogen') as objectid, ts,
uuid as runId
FROM urine
where urobilinogen is not null and urobilinogen != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'nitrite' as TestID,
nitrite as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'nitrite') as objectid, ts,
uuid as runId
FROM urine
where nitrite is not null and nitrite != ""
AND ts > ?

UNION ALL

/* field misspelled in mysql */
SELECT
id,
date,
'leukocytes' as TestID,
leucocytes as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'leukocytes') as objectid, ts,
uuid as runId
FROM urine
where leucocytes is not null and leucocytes != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'appearance' as TestID,
appearance as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'appearance') as objectid, ts,
uuid as runId
FROM urine
where appearance is not null and appearance != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'microscopic' as TestID,
microscopic as stringResults,
null as result,
null as Units,
null as remark,
concat(uuid,'microscopic') as objectid, ts,
uuid as runId
FROM urine
where microscopic is not null and microscopic != ""
AND ts > ?

) x

