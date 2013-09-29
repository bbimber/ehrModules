ALTER TABLE ehr_lookups.snomed ADD container ENTITYID;

--upgrade path for WNPRC
UPDATE ehr_lookups.snomed SET container = (SELECT c.entityid from core.containers c LEFT JOIN core.Containers c2 on (c.Parent = c2.EntityId) WHERE c.name = 'EHR' and c2.name = 'WNPRC');
