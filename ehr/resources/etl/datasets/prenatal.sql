/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT lower(id) as id, FixDate(date) as date, FixSpecies(species) as species, sex, weight, lower(dam) as dam, lower(sire) as sire, room, cage, FixDate(conception) as conception, remark,
ts, uuid AS objectid

FROM prenatal p
WHERE ts > ?