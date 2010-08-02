/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date) AS Date,
(SELECT group_concat(uuid) FROM necropsyhead n2 WHERE n.id = n2.id AND n.date = n2.date) AS parentid,
(seq1) AS seq1, (seq2) AS seq2, (n.code) AS code,
     CONCAT('Code: ', s1.meaning, ' (', n.code, ')') AS Description, n.ts, n.uuid AS objectid
FROM necropsydiag n
LEFT JOIN snomed s1 on n.code =s1.code

HAVING parentid LIKE "%,%"