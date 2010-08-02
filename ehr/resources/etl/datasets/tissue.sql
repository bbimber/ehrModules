/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDateTime(date, time) AS Date, (sex) AS sex, (livedead) AS livedead, (wbo) AS wbo, (tissue) AS tissue, (source) AS source, (dest) AS dest, (recip) AS recip, (affil) AS affil, FixNewlines(remark) AS remark,
ts, uuid AS objectid
FROM tissue
WHERE ts > ?