/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date) AS Date, (userid) AS userid, (feces) AS feces, (menses) AS menses, (behavior) AS behavior, (breeding) AS breeding, (other) AS other, (tlocation) AS tlocation, FixNewlines(remark) AS remark, (otherbehavior) AS otherbehavior, Timestamp(Date('1970-01-01'), time) AS time, ( CONCAT_WS(', ', 
     CASE WHEN userid IS NULL  OR userid=''  THEN NULL ELSE CONCAT('userid: ', userid)  END, 
     CASE WHEN feces=''  THEN NULL ELSE CONCAT('feces: ', feces)  END, 
     CASE WHEN menses IS NULL  OR menses=''  THEN NULL ELSE CONCAT('menses: ', menses)  END, 
     CASE WHEN behavior=''  THEN NULL ELSE CONCAT('behavior: ', behavior)  END, 
     CASE WHEN breeding IS NULL  OR breeding=''  THEN NULL ELSE CONCAT('breeding: ', breeding)  END, 
     CASE WHEN other=''  THEN NULL ELSE CONCAT('other: ', other)  END, 
     CASE WHEN tlocation IS NULL  OR tlocation=''  THEN NULL ELSE CONCAT('tlocation: ', tlocation)  END, 
     CASE WHEN otherbehavior IS NULL  OR otherbehavior=''  THEN NULL ELSE CONCAT('otherbehavior: ', otherbehavior)  END) ) AS Description FROM obs
