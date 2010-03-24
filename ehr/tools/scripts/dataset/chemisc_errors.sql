/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(date) AS Date, (name) AS name, (value) AS value, (units) AS units,
     ( CONCAT_WS(',\n',
     CONCAT('Test: ', name),
     CONCAT('Value: ', value, ' ', units)
     ) ) AS Description

FROM

(

SELECT id, date, name, value, NULL AS units
FROM chemisc c
WHERE date IS NULL OR date = '0000-00-00'

UNION ALL

SELECT id, date, name, value, units
FROM chemisc2
WHERE date IS NULL OR date = '0000-00-00'
) x
