/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT UCASE(protocol) as protocol, inves, FixDate(approve) as approve, species1, c1, species2, c2, species3, c3, species4, c4, species5, c5  FROM protocol p