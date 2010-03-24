/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT id, FixDate(birth) AS Date, (sex) AS sex, (status) AS status, (avail) AS avail, 
(hold) AS hold, (dam) AS dam, (sire) AS sire, (origin) AS origin,
FixDate(birth) AS birth, FixDate(death) AS death,
FixDateTime(arrivedate, arrivetime) AS arrivedate,
FixDateTime(departdate, departtime) AS departdate,
(room) AS room, (cage) AS cage, (cond) AS cond, 
(weight) AS weight, FixDateTime(wdate, wtime) AS wdate, FixDate(tbdate) AS tbdate,
(medical) AS medical, (purchasedby) AS purchasedby, (v_status) AS v_status
  

FROM abstract

