/*
 * Copyright (c) 2010 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT
d.Id AS Id,

--TODO: I dont think i should have to convert this.  bug??
convert(COALESCE(T4.Total, 0), INTEGER) as HistoricAssignments,
convert(COALESCE(T2.Total, 0), INTEGER) as PendingAssignments,
convert(COALESCE(T1.Total, 0), INTEGER) as HistoricResearchAssignments,
convert(COALESCE(T3.Total, 0), INTEGER) as HistoricVetAssignments,
convert(COALESCE(T10.Total, 0), INTEGER) as HistoricTrainingAssignments,
convert(COALESCE(T9.Total, 0), INTEGER) as HistoricBreedingAssignments,

CASE WHEN T6.Total > 0 THEN 'Y' ELSE null END as HistoricSPF_StockAssignment,
CASE WHEN T7.Total > 0 THEN 'Y' ELSE null END as HistoricConventionalStockAssignment,
CASE WHEN T8.Total > 0 THEN 'Y' ELSE null END as HistoricMarmStockAssignment,
convert(COALESCE(T5.Total, 0), INTEGER) as HistoricStockAssignments,

T4.Total as HistoricAssignments2,
T2.Total as PendingAssignments2,
T1.Total as HistoricResearchAssignments2,
T3.Total as HistoricVetAssignments2,
T10.Total as HistoricTrainingAssignments2,
T6.Total as HistoricSPF_StockAssignments2,
T7.Total as HistoricConventionalStockAssignments2,
T8.Total as HistoricMarmStockAssignments2,
T5.Total as HistoricStockAssignments2,
T9.Total as HistoricBreedingAssignments2,

FROM study.demographics d

--we find the number of Historic research project assignments
LEFT JOIN
    (SELECT T1.Id, count(*) AS Total FROM study.Assignment T1 WHERE (T1.project.avail = 'r' OR T1.project.avail = 'n') GROUP BY T1.Id) T1
    ON (T1.Id = d.Id)

--we find the number of pending project assignments
LEFT JOIN
    (SELECT T2.Id, count(*) AS Total FROM study.Assignment T2 WHERE (T2.project.avail = 'p') GROUP BY T2.Id) T2
    ON (T2.Id = d.Id)

--we find the number of Historic vet project assignments
LEFT JOIN
    (SELECT T3.Id, count(*) AS Total FROM study.Assignment T3 WHERE T3.project.avail = 'v' GROUP BY T3.Id) T3
    ON (T3.Id = d.Id)

--we find the number of Historic breeding project assignments
LEFT JOIN
    (SELECT T9.Id, count(*) AS Total FROM study.Assignment T9 WHERE T9.project.avail = 'b' GROUP BY T9.Id) T9
    ON (T9.Id = d.Id)

--we find the number of Historic breeding project assignments
LEFT JOIN
    (SELECT T10.Id, count(*) AS Total FROM study.Assignment T10 WHERE T10.project.avail = 't' GROUP BY T10.Id) T10
    ON (T10.Id = d.Id)

--we find the number of total Historic project assignments
LEFT JOIN
    (SELECT T4.Id, count(*) AS Total FROM study.Assignment T4 GROUP BY T4.Id) T4
    ON (T4.Id = d.Id)

--we find the number of Historic stock project assignments
--spf stock animals (20020201)
--conventional stock animals (20070202)
--marmoset stock animals (20070801)
LEFT JOIN
    (SELECT T5.Id, count(*) AS Total FROM study.Assignment T5 WHERE (t5.project = '20020201' OR t5.project = '20070202' OR t5.project = '20070801') GROUP BY T5.Id) T5
    ON (T5.Id = d.Id)

--we find the number of Historic spf stock project assignments
--spf stock animals (20020201)
LEFT JOIN
    (SELECT T6.Id, count(*) AS Total FROM study.Assignment T6 WHERE (t6.project = '20020201') GROUP BY T6.Id) T6
    ON (T6.Id = d.Id)

--we find the number of Historic conventional stock project assignments
--conventional stock animals (20070202)
LEFT JOIN
    (SELECT T7.Id, count(*) AS Total FROM study.Assignment T7 WHERE (t7.project = '20070202') GROUP BY T7.Id) T7
    ON (T7.Id = d.Id)

--we find the number of Historic marm stock project assignments
--marmoset stock animals (20070801)
LEFT JOIN
    (SELECT T8.Id, count(*) AS Total FROM study.Assignment T8 WHERE (t8.project = '20070801') GROUP BY T8.Id) T8
    ON (T8.Id = d.Id)

WHERE
d.id.status.status = 'Alive'
AND d.id.species.species != 'Unknown'