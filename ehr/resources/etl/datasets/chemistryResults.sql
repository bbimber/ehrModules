/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDate(date) AS Date, TestID, Results, units, remark,
ts, objectid, runId

FROM

(

SELECT
id,
date,
'Glucose' as TestID,
glucose as Results,
null as Units,
null as remark,
concat(uuid,'Glucose') as objectid, ts,
uuid as runId
FROM chemistry
where glucose is not null and glucose != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'bun' as TestID,
bun as Results,
null as Units,
null as remark,
concat(uuid,'bun') as objectid, ts,
uuid as runId
FROM chemistry
where bun is not null and bun != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'creatinine' as TestID,
creatinine as Results,
null as Units,
null as remark,
concat(uuid,'creatinine') as objectid, ts,
uuid as runId
FROM chemistry
where creatinine is not null and creatinine != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'ck_cpk' as TestID,
ck_cpk as Results,
null as Units,
null as remark,
concat(uuid,'ck_cpk') as objectid, ts,
uuid as runId
FROM chemistry
where ck_cpk is not null and ck_cpk != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'uricacid' as TestID,
uricacid as Results,
null as Units,
null as remark,
concat(uuid,'uricacid') as objectid, ts,
uuid as runId
FROM chemistry
where uricacid is not null and uricacid != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'cholesterol' as TestID,
cholesterol as Results,
null as Units,
null as remark,
concat(uuid,'cholesterol') as objectid, ts,
uuid as runId
FROM chemistry
where cholesterol is not null and cholesterol != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'triglyc' as TestID,
triglyc as Results,
null as Units,
null as remark,
concat(uuid,'triglyc') as objectid, ts,
uuid as runId
FROM chemistry
where triglyc is not null and triglyc != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'sgot_ast' as TestID,
sgot_ast as Results,
null as Units,
null as remark,
concat(uuid,'sgot_ast') as objectid, ts,
uuid as runId
FROM chemistry
where sgot_ast is not null and sgot_ast != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'tbili' as TestID,
tbili as Results,
null as Units,
null as remark,
concat(uuid,'tbili') as objectid, ts,
uuid as runId
FROM chemistry
where tbili is not null and tbili != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'ggt' as TestID,
ggt as Results,
null as Units,
null as remark,
concat(uuid,'ggt') as objectid, ts,
uuid as runId
FROM chemistry
where ggt is not null and ggt != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'sgpt_alt' as TestID,
sgpt_alt as Results,
null as Units,
null as remark,
concat(uuid,'sgpt_alt') as objectid, ts,
uuid as runId
FROM chemistry
where sgpt_alt is not null and sgpt_alt != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'tprotein' as TestID,
tprotein as Results,
null as Units,
null as remark,
concat(uuid,'tprotein') as objectid, ts,
uuid as runId
FROM chemistry
where tprotein is not null and tprotein != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'albumin' as TestID,
albumin as Results,
null as Units,
null as remark,
concat(uuid,'albumin') as objectid, ts,
uuid as runId
FROM chemistry
where albumin is not null and albumin != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'phosphatase' as TestID,
phosphatase as Results,
null as Units,
null as remark,
concat(uuid,'phosphatase') as objectid, ts,
uuid as runId
FROM chemistry
where phosphatase is not null and phosphatase != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'calcium' as TestID,
calcium as Results,
null as Units,
null as remark,
concat(uuid,'calcium') as objectid, ts,
uuid as runId
FROM chemistry
where calcium is not null and calcium != ""

UNION ALL

SELECT
id,
date,
'phosphorus' as TestID,
phosphorus as Results,
null as Units,
null as remark,
concat(uuid,'phosphorus') as objectid, ts,
uuid as runId
FROM chemistry
where phosphorus is not null and phosphorus != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'iron' as TestID,
iron as Results,
null as Units,
null as remark,
concat(uuid,'iron') as objectid, ts,
uuid as runId
FROM chemistry
where iron is not null and iron != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'sodium' as TestID,
sodium as Results,
null as Units,
null as remark,
concat(uuid,'sodium') as objectid, ts,
uuid as runId
FROM chemistry
where sodium is not null and sodium != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'potassium' as TestID,
potassium as Results,
null as Units,
null as remark,
concat(uuid,'potassium') as objectid, ts,
uuid as runId
FROM chemistry
where potassium is not null and potassium != ""
AND ts > ?

UNION ALL

SELECT
id,
date,
'chloride' as TestID,
chloride as Results,
null as Units,
null as remark,
concat(uuid,'chloride') as objectid, ts,
uuid as runId
FROM chemistry
where chloride is not null and chloride != ""
AND ts > ?

UNION ALL

SELECT id, date, name as TestID, value as Results, NULL AS units, NULL as remark, 
uuid as objectId, ts,
COALESCE((select UUID FROM chemistry t2 WHERE t1.id=t2.id and t1.date=t2.date limit 1), uuid) as runId
FROM chemisc t1
AND ts > ?

UNION ALL

SELECT id, date, name as TestID, value as results, units AS units, NULL as remark,
uuid as objectId, ts,
COALESCE((select UUID FROM chemistry t2 WHERE t1.id=t2.id and t1.date=t2.date limit 1), uuid) as runId
FROM chemisc2 t1
AND ts > ?

) x

WHERE date != '0000-00-00' and TestID != '' and results != ''