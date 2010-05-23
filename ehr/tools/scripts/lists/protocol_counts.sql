/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT UCASE(protocol) as protocol, uuid AS parentId, ts,
FixSpecies(species1) AS species, c1 as count,
null as StartDate, null as EndDate
FROM protocol p
WHERE species1 != ""

UNION ALL

SELECT UCASE(protocol) as protocol, uuid AS parentId, ts,
FixSpecies(species2) AS species, c2 as count,
null as StartDate, null as EndDate
FROM protocol p
WHERE species2 != ""

UNION ALL

SELECT UCASE(protocol) as protocol, uuid AS parentId, ts,
FixSpecies(species3) AS species, c3 as count,
null as StartDate, null as EndDate
FROM protocol p
WHERE species3 != ""

UNION ALL

SELECT UCASE(protocol) as protocol, uuid AS parentId, ts,
FixSpecies(species4) AS species, c4 as count,
null as StartDate, null as EndDate
FROM protocol p
WHERE species4 != ""

UNION ALL

SELECT UCASE(protocol) as protocol, uuid AS parentId, ts,
FixSpecies(species5) AS species, c5 as count,
null as StartDate, null as EndDate
FROM protocol p
WHERE species5 != ""