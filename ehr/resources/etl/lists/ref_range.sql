/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT concat(test, "-", species, "-", sex, "-", age_class) AS keyField, FixSpecies(species) AS species, sex, age_class, test, ref_range_min, ref_range_max, ts, uuid AS objectid FROM ref_range r
WHERE ts > ?