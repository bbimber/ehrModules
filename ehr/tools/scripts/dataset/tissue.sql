/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date) AS Date, (sex) AS sex, Timestamp(Date('1970-01-01'), time) AS time, (livedead) AS livedead, (wbo) AS wbo, (tissue) AS tissue, (source) AS source, (dest) AS dest, (recip) AS recip, (affil) AS affil, FixNewlines(remark) AS remark, ( CONCAT_WS(', ', 
     CASE WHEN sex IS NULL  OR sex=''  THEN NULL ELSE CONCAT('sex: ', sex)  END, 
     CASE WHEN livedead IS NULL  OR livedead=''  THEN NULL ELSE CONCAT('livedead: ', livedead)  END, 
     CASE WHEN wbo IS NULL  OR wbo=''  THEN NULL ELSE CONCAT('wbo: ', wbo)  END, 
     CASE WHEN tissue IS NULL  OR tissue=''  THEN NULL ELSE CONCAT('tissue: ', tissue)  END, 
     CASE WHEN source IS NULL  OR source=''  THEN NULL ELSE CONCAT('source: ', source)  END, 
     CASE WHEN dest IS NULL  OR dest=''  THEN NULL ELSE CONCAT('dest: ', dest)  END, 
     CASE WHEN recip IS NULL  OR recip=''  THEN NULL ELSE CONCAT('recip: ', recip)  END, 
     CASE WHEN affil IS NULL  OR affil=''  THEN NULL ELSE CONCAT('affil: ', affil)  END, 
     CASE WHEN remark IS NULL  OR remark=''  THEN NULL ELSE CONCAT('remark: ', FixNewlines(remark))  END) ) AS Description FROM tissue
