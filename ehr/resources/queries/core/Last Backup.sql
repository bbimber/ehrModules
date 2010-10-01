/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
SELECT
x.LastJob,
x.TimeSinceJob,
x.Days,
hour(x.TimeSinceJob) as Hours,
minute(x.TimeSinceJob) as Minutes,

FROM (

SELECT

max(b.Date) as LastJob,
(now() - max(b.Date)) as TimeSinceJob,
TIMESTAMPDIFF('SQL_TSI_DAY', max(b.Date), now() ) as Days,
FROM "/WNPRC/EHR/Backups/".lists.backup b

WHERE b.status='Success'

GROUP BY b.status

) x