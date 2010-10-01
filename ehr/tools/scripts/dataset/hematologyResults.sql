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
'wbc' as TestID,
wbc as Results,
null as Units,
null as remark,
concat(uuid,'wbc') as objectid, ts,
uuid as runId
FROM hematology
where wbc is not null and wbc != ""


UNION ALL

SELECT
id,
date,
'rbc' as TestID,
rbc as Results,
null as Units,
null as remark,
concat(uuid,'rbc') as objectid, ts,
uuid as runId
FROM hematology
where rbc is not null and rbc != ""


UNION ALL

SELECT
id,
date,
'hgb' as TestID,
hgb as Results,
null as Units,
null as remark,
concat(uuid,'hgb') as objectid, ts,
uuid as runId
FROM hematology
where hgb is not null and hgb != ""


UNION ALL

SELECT
id,
date,
'hct' as TestID,
hct as Results,
null as Units,
null as remark,
concat(uuid,'hct') as objectid, ts,
uuid as runId
FROM hematology
where hct is not null and hct != ""


UNION ALL

SELECT
id,
date,
'mcv' as TestID,
mcv as Results,
null as Units,
null as remark,
concat(uuid,'mcv') as objectid, ts,
uuid as runId
FROM hematology
where mcv is not null and mcv != ""


UNION ALL

SELECT
id,
date,
'mch' as TestID,
mch as Results,
null as Units,
null as remark,
concat(uuid,'mch') as objectid, ts,
uuid as runId
FROM hematology
where mch is not null and mch != ""


UNION ALL

SELECT
id,
date,
'mchc' as TestID,
mchc as Results,
null as Units,
null as remark,
concat(uuid,'mchc') as objectid, ts,
uuid as runId
FROM hematology
where mchc is not null and mchc != ""


UNION ALL

SELECT
id,
date,
'rdw' as TestID,
rdw as Results,
null as Units,
null as remark,
concat(uuid,'rdw') as objectid, ts,
uuid as runId
FROM hematology
where rdw is not null and rdw != ""


UNION ALL

SELECT
id,
date,
'plt' as TestID,
plt as Results,
null as Units,
null as remark,
concat(uuid,'plt') as objectid, ts,
uuid as runId
FROM hematology
where plt is not null and plt != ""


UNION ALL

SELECT
id,
date,
'mpv' as TestID,
mpv as Results,
null as Units,
null as remark,
concat(uuid,'mpv') as objectid, ts,
uuid as runId
FROM hematology
where mpv is not null and mpv != ""


UNION ALL

SELECT
id,
date,
'pcv' as TestID,
pcv as Results,
null as Units,
null as remark,
concat(uuid,'pcv') as objectid, ts,
uuid as runId
FROM hematology
where pcv is not null and pcv != ""


UNION ALL

SELECT
id,
date,
'n' as TestID,
n as Results,
null as Units,
null as remark,
concat(uuid,'n') as objectid, ts,
uuid as runId
FROM hematology
where n is not null and n != ""


UNION ALL

SELECT
id,
date,
'l' as TestID,
l as Results,
null as Units,
null as remark,
concat(uuid,'l') as objectid, ts,
uuid as runId
FROM hematology
where l is not null and l != ""


UNION ALL

SELECT
id,
date,
'm' as TestID,
m as Results,
null as Units,
null as remark,
concat(uuid,'m') as objectid, ts,
uuid as runId
FROM hematology
where m is not null and m != ""


UNION ALL

SELECT
id,
date,
'e' as TestID,
e as Results,
null as Units,
null as remark,
concat(uuid,'e') as objectid, ts,
uuid as runId
FROM hematology
where e is not null and e != ""


UNION ALL

SELECT
id,
date,
'b' as TestID,
b as Results,
null as Units,
null as remark,
concat(uuid,'b') as objectid, ts,
uuid as runId
FROM hematology
where b is not null and b != ""


UNION ALL

SELECT
id,
date,
'bands' as TestID,
bands as Results,
null as Units,
null as remark,
concat(uuid,'bands') as objectid, ts,
uuid as runId
FROM hematology
where bands is not null and bands != ""


UNION ALL

SELECT
id,
date,
'metamyelo' as TestID,
metamyelo as Results,
null as Units,
null as remark,
concat(uuid,'metamyelo') as objectid, ts,
uuid as runId
FROM hematology
where metamyelo is not null and metamyelo != ""


UNION ALL

SELECT
id,
date,
'myelo' as TestID,
myelo as Results,
null as Units,
null as remark,
concat(uuid,'myelo') as objectid, ts,
uuid as runId
FROM hematology
where myelo is not null and myelo != ""


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
FROM hematology
where tprotein is not null and tprotein != ""


UNION ALL

SELECT
id,
date,
'reticulo' as TestID,
reticulo as Results,
null as Units,
null as remark,
concat(uuid,'reticulo') as objectid, ts,
uuid as runId
FROM hematology
where reticulo is not null and reticulo != ""


) x
