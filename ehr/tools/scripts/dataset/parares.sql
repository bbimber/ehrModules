/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date) AS Date, null AS parasitology_objectid, (seq) AS seq, (p.code) AS code, ( CONCAT_WS(', ', 
     meaning,
     CASE WHEN seq IS NULL  THEN NULL ELSE CONCAT('seq: ', CAST(seq AS CHAR))  END, 
     CASE WHEN p.code IS NULL  OR p.code=''  THEN NULL ELSE CONCAT('code: ', p.code)  END) ) AS Description FROM parares p
     LEFT OUTER JOIN snomed on snomed.code=p.code
