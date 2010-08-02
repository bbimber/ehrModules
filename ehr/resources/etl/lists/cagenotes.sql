/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT concat(room, "-", cage) AS roomCage, room, cage, FixNewLines(note) AS note, FixDateTime(date, time) as date, userid, ts, uuid AS objectid
FROM cagenotes c
WHERE ts > ?