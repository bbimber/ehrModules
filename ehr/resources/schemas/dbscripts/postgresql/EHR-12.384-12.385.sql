ALTER TABLE ehr.snomed_tags DROP CONSTRAINT PK_snomed_tags;
DROP INDEX ehr.snomed_tags_recordid_rowid_id;
DROP INDEX ehr.snomed_tags_code_rowid_id_recordid;
DROP INDEX ehr.snomed_tags_parentid;
DROP INDEX ehr.snomed_tags_caseid;
DROP INDEX ehr.snomed_tags_objectid;

ALTER TABLE ehr.snomed_tags DROP COLUMN rowid;
ALTER TABLE ehr.snomed_tags DROP COLUMN parentid;
ALTER TABLE ehr.snomed_tags DROP COLUMN caseid;
ALTER TABLE ehr.snomed_tags DROP COLUMN schemaName;
ALTER TABLE ehr.snomed_tags DROP COLUMN queryName;
ALTER TABLE ehr.snomed_tags ALTER COLUMN objectid SET NOT NULL;

CREATE INDEX snomed_tags_objectid on ehr.snomed_tags (objectid);
ALTER TABLE ehr.snomed_tags ADD CONSTRAINT PK_snomed_tags PRIMARY KEY (objectid);