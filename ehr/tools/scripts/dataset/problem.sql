/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date_observed) AS Date, (problem_no) AS problem_no, FixNewlines(description) AS descript, FixDate(date_resolved) AS date_resolved, (c.code) AS code,
     ( CONCAT_WS(',\n',
     CONCAT('Problem No: ', CAST(problem_no AS CHAR)),
     CONCAT('Date Observed: ', CAST(date_observed AS CHAR)),
     CONCAT('Date Resolved: ', CAST(date_resolved AS CHAR)),
     CONCAT('Description: ', FixNewlines(description))
     )) AS Description
FROM cases c

