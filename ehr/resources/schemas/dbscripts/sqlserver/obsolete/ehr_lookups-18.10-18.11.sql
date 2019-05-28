/*
 * Copyright (c) 2018 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
-- add Container column to the ehr/ehr_lookups tables (values to be populated in Java upgrade script)
ALTER TABLE ehr.protocolProcedures ADD Container ENTITYID;
ALTER TABLE ehr.scheduled_task_types ADD Container ENTITYID;
GO

ALTER TABLE ehr.protocolProcedures ADD CONSTRAINT FK_ehr_protocolProcedures_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr.scheduled_task_types ADD CONSTRAINT FK_ehr_scheduled_task_types_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
GO

EXEC core.executeJavaUpgradeCode 'setEhrContainerFirstSet';
GO

ALTER TABLE ehr_lookups.ageclass ADD Container ENTITYID;
ALTER TABLE ehr_lookups.amount_units ADD Container ENTITYID;
ALTER TABLE ehr_lookups.areas ADD Container ENTITYID;
ALTER TABLE ehr_lookups.billingtypes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.blood_draw_services ADD Container ENTITYID;
ALTER TABLE ehr_lookups.blood_draw_tube_type ADD Container ENTITYID;
ALTER TABLE ehr_lookups.blood_tube_volumes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.cage ADD Container ENTITYID;
ALTER TABLE ehr_lookups.cage_positions ADD Container ENTITYID;
ALTER TABLE ehr_lookups.cage_type ADD Container ENTITYID;
ALTER TABLE ehr_lookups.cageclass ADD Container ENTITYID;
ALTER TABLE ehr_lookups.calculated_status_codes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.clinpath_status ADD Container ENTITYID;
ALTER TABLE ehr_lookups.clinpath_tests ADD Container ENTITYID;
ALTER TABLE ehr_lookups.conc_units ADD Container ENTITYID;
ALTER TABLE ehr_lookups.death_remarks ADD Container ENTITYID;
ALTER TABLE ehr_lookups.disallowed_medications ADD Container ENTITYID;
ALTER TABLE ehr_lookups.divider_types ADD Container ENTITYID;
ALTER TABLE ehr_lookups.dosage_units ADD Container ENTITYID;
ALTER TABLE ehr_lookups.drug_defaults ADD Container ENTITYID;
ALTER TABLE ehr_lookups.flag_categories ADD Container ENTITYID;
ALTER TABLE ehr_lookups.full_snomed ADD Container ENTITYID;
ALTER TABLE ehr_lookups.gender_codes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.lab_test_range ADD Container ENTITYID;
ALTER TABLE ehr_lookups.lab_tests ADD Container ENTITYID;
ALTER TABLE ehr_lookups.labwork_panels ADD Container ENTITYID;
ALTER TABLE ehr_lookups.labwork_services ADD Container ENTITYID;
ALTER TABLE ehr_lookups.labwork_types ADD Container ENTITYID;
ALTER TABLE ehr_lookups.note_types ADD Container ENTITYID;
ALTER TABLE ehr_lookups.parentageTypes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedure_default_charges ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedure_default_codes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedure_default_comments ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedure_default_flags ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedure_default_treatments ADD Container ENTITYID;
ALTER TABLE ehr_lookups.procedures ADD Container ENTITYID;
ALTER TABLE ehr_lookups.project_types ADD Container ENTITYID;
ALTER TABLE ehr_lookups.relationshipTypes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.request_priority ADD Container ENTITYID;
ALTER TABLE ehr_lookups.restraint_type ADD Container ENTITYID;
ALTER TABLE ehr_lookups.routes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.snomap ADD Container ENTITYID;
ALTER TABLE ehr_lookups.source ADD Container ENTITYID;
ALTER TABLE ehr_lookups.species ADD Container ENTITYID;
ALTER TABLE ehr_lookups.species_codes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.treatment_frequency ADD Container ENTITYID;
ALTER TABLE ehr_lookups.treatment_frequency_times ADD Container ENTITYID;
ALTER TABLE ehr_lookups.usda_codes ADD Container ENTITYID;
ALTER TABLE ehr_lookups.usda_levels ADD Container ENTITYID;
ALTER TABLE ehr_lookups.volume_units ADD Container ENTITYID;
ALTER TABLE ehr_lookups.weight_ranges ADD Container ENTITYID;
GO

-- add the FK for those Container columns
ALTER TABLE ehr_lookups.ageclass ADD CONSTRAINT FK_ehr_lookups_ageclass_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.amount_units ADD CONSTRAINT FK_ehr_lookups_amount_units_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.areas ADD CONSTRAINT FK_ehr_lookups_areas_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.billingtypes ADD CONSTRAINT FK_ehr_lookups_billingtypes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.blood_draw_services ADD CONSTRAINT FK_ehr_lookups_blood_draw_services_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.blood_draw_tube_type ADD CONSTRAINT FK_ehr_lookups_blood_draw_tube_type_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.blood_tube_volumes ADD CONSTRAINT FK_ehr_lookups_blood_tube_volumes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.cage ADD CONSTRAINT FK_ehr_lookups_cage_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.cage_positions ADD CONSTRAINT FK_ehr_lookups_cage_positions_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.cage_type ADD CONSTRAINT FK_ehr_lookups_cage_type_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.cageclass ADD CONSTRAINT FK_ehr_lookups_cageclass_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.calculated_status_codes ADD CONSTRAINT FK_ehr_lookups_calculated_status_codes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.clinpath_status ADD CONSTRAINT FK_ehr_lookups_clinpath_status_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.clinpath_tests ADD CONSTRAINT FK_ehr_lookups_clinpath_tests_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.conc_units ADD CONSTRAINT FK_ehr_lookups_conc_units_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.death_remarks ADD CONSTRAINT FK_ehr_lookups_death_remarks_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.disallowed_medications ADD CONSTRAINT FK_ehr_lookups_disallowed_medications_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.divider_types ADD CONSTRAINT FK_ehr_lookups_divider_types_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.dosage_units ADD CONSTRAINT FK_ehr_lookups_dosage_units_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.drug_defaults ADD CONSTRAINT FK_ehr_lookups_drug_defaults_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.flag_categories ADD CONSTRAINT FK_ehr_lookups_flag_categories_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.full_snomed ADD CONSTRAINT FK_ehr_lookups_full_snomed_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.gender_codes ADD CONSTRAINT FK_ehr_lookups_gender_codes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.lab_test_range ADD CONSTRAINT FK_ehr_lookups_lab_test_range_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.lab_tests ADD CONSTRAINT FK_ehr_lookups_lab_tests_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.labwork_panels ADD CONSTRAINT FK_ehr_lookups_labwork_panels_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.labwork_services ADD CONSTRAINT FK_ehr_lookups_labwork_services_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.labwork_types ADD CONSTRAINT FK_ehr_lookups_labwork_types_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.note_types ADD CONSTRAINT FK_ehr_lookups_note_types_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.parentageTypes ADD CONSTRAINT FK_ehr_lookups_parentageTypes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedure_default_charges ADD CONSTRAINT FK_ehr_lookups_procedure_default_charges_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedure_default_codes ADD CONSTRAINT FK_ehr_lookups_procedure_default_codes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedure_default_comments ADD CONSTRAINT FK_ehr_lookups_procedure_default_comments_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedure_default_flags ADD CONSTRAINT FK_ehr_lookups_procedure_default_flags_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedure_default_treatments ADD CONSTRAINT FK_ehr_lookups_procedure_default_treatments_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.procedures ADD CONSTRAINT FK_ehr_lookups_procedures_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.project_types ADD CONSTRAINT FK_ehr_lookups_project_types_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.relationshipTypes ADD CONSTRAINT FK_ehr_lookups_relationshipTypes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.request_priority ADD CONSTRAINT FK_ehr_lookups_request_priority_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.restraint_type ADD CONSTRAINT FK_ehr_lookups_restraint_type_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.routes ADD CONSTRAINT FK_ehr_lookups_routes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.snomap ADD CONSTRAINT FK_ehr_lookups_snomap_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.source ADD CONSTRAINT FK_ehr_lookups_source_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.species ADD CONSTRAINT FK_ehr_lookups_species_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.species_codes ADD CONSTRAINT FK_ehr_lookups_species_codes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.treatment_frequency ADD CONSTRAINT FK_ehr_lookups_treatment_frequency_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.treatment_frequency_times ADD CONSTRAINT FK_ehr_lookups_treatment_frequency_times_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.usda_codes ADD CONSTRAINT FK_ehr_lookups_usda_codes_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.usda_levels ADD CONSTRAINT FK_ehr_lookups_usda_levels_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.volume_units ADD CONSTRAINT FK_ehr_lookups_volume_units_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
ALTER TABLE ehr_lookups.weight_ranges ADD CONSTRAINT FK_ehr_lookups_weight_ranges_Container FOREIGN KEY (Container) REFERENCES core.Containers(EntityId);
GO

-- Java upgrade script to populate the Container column from site-level EHRStudyContainer module property
EXEC core.executeJavaUpgradeCode 'setEhrLookupsContainerSecondSet';
GO

-- remove any NULL rows for Container
DELETE FROM ehr.protocolProcedures WHERE Container IS NULL;
DELETE FROM ehr.scheduled_task_types WHERE Container IS NULL;
DELETE FROM ehr_lookups.ageclass WHERE Container IS NULL;
DELETE FROM ehr_lookups.amount_units WHERE Container IS NULL;
DELETE FROM ehr_lookups.areas WHERE Container IS NULL;
DELETE FROM ehr_lookups.billingtypes WHERE Container IS NULL;
DELETE FROM ehr_lookups.blood_draw_services WHERE Container IS NULL;
DELETE FROM ehr_lookups.blood_draw_tube_type WHERE Container IS NULL;
DELETE FROM ehr_lookups.blood_tube_volumes WHERE Container IS NULL;
DELETE FROM ehr_lookups.cage WHERE Container IS NULL;
DELETE FROM ehr_lookups.cage_positions WHERE Container IS NULL;
DELETE FROM ehr_lookups.cage_type WHERE Container IS NULL;
DELETE FROM ehr_lookups.cageclass WHERE Container IS NULL;
DELETE FROM ehr_lookups.calculated_status_codes WHERE Container IS NULL;
DELETE FROM ehr_lookups.clinpath_status WHERE Container IS NULL;
DELETE FROM ehr_lookups.clinpath_tests WHERE Container IS NULL;
DELETE FROM ehr_lookups.conc_units WHERE Container IS NULL;
DELETE FROM ehr_lookups.death_remarks WHERE Container IS NULL;
DELETE FROM ehr_lookups.disallowed_medications WHERE Container IS NULL;
DELETE FROM ehr_lookups.divider_types WHERE Container IS NULL;
DELETE FROM ehr_lookups.dosage_units WHERE Container IS NULL;
DELETE FROM ehr_lookups.drug_defaults WHERE Container IS NULL;
DELETE FROM ehr_lookups.flag_categories WHERE Container IS NULL;
DELETE FROM ehr_lookups.full_snomed WHERE Container IS NULL;
DELETE FROM ehr_lookups.gender_codes WHERE Container IS NULL;
DELETE FROM ehr_lookups.lab_test_range WHERE Container IS NULL;
DELETE FROM ehr_lookups.lab_tests WHERE Container IS NULL;
DELETE FROM ehr_lookups.labwork_panels WHERE Container IS NULL;
DELETE FROM ehr_lookups.labwork_services WHERE Container IS NULL;
DELETE FROM ehr_lookups.labwork_types WHERE Container IS NULL;
DELETE FROM ehr_lookups.note_types WHERE Container IS NULL;
DELETE FROM ehr_lookups.parentageTypes WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedure_default_charges WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedure_default_codes WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedure_default_comments WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedure_default_flags WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedure_default_treatments WHERE Container IS NULL;
DELETE FROM ehr_lookups.procedures WHERE Container IS NULL;
DELETE FROM ehr_lookups.project_types WHERE Container IS NULL;
DELETE FROM ehr_lookups.relationshipTypes WHERE Container IS NULL;
DELETE FROM ehr_lookups.request_priority WHERE Container IS NULL;
DELETE FROM ehr_lookups.restraint_type WHERE Container IS NULL;
DELETE FROM ehr_lookups.routes WHERE Container IS NULL;
DELETE FROM ehr_lookups.snomap WHERE Container IS NULL;
DELETE FROM ehr_lookups.source WHERE Container IS NULL;
DELETE FROM ehr_lookups.species WHERE Container IS NULL;
DELETE FROM ehr_lookups.species_codes WHERE Container IS NULL;
DELETE FROM ehr_lookups.treatment_frequency WHERE Container IS NULL;
DELETE FROM ehr_lookups.treatment_frequency_times WHERE Container IS NULL;
DELETE FROM ehr_lookups.usda_codes WHERE Container IS NULL;
DELETE FROM ehr_lookups.usda_levels WHERE Container IS NULL;
DELETE FROM ehr_lookups.volume_units WHERE Container IS NULL;
DELETE FROM ehr_lookups.weight_ranges WHERE Container IS NULL;
GO

--set NOT NULL constraint for the Container columns
ALTER TABLE ehr.protocolProcedures ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr.scheduled_task_types ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.ageclass ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.amount_units ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.areas ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.billingtypes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.blood_draw_services ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.blood_draw_tube_type ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.blood_tube_volumes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.cage ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.cage_positions ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.cage_type ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.cageclass ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.calculated_status_codes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.clinpath_status ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.clinpath_tests ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.conc_units ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.death_remarks ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.disallowed_medications ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.divider_types ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.dosage_units ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.drug_defaults ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.flag_categories ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.full_snomed ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.gender_codes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.lab_test_range ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.lab_tests ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.labwork_panels ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.labwork_services ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.labwork_types ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.note_types ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.parentageTypes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedure_default_charges ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedure_default_codes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedure_default_comments ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedure_default_flags ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedure_default_treatments ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.procedures ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.project_types ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.relationshipTypes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.request_priority ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.restraint_type ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.routes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.snomap ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.source ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.species ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.species_codes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.treatment_frequency ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.treatment_frequency_times ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.usda_codes ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.usda_levels ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.volume_units ALTER COLUMN Container ENTITYID NOT NULL;
ALTER TABLE ehr_lookups.weight_ranges ALTER COLUMN Container ENTITYID NOT NULL;
GO

-- add the INDEX for those Container columns
CREATE INDEX IX_ehr_protocolProcedures_Container ON ehr.protocolProcedures (Container);
CREATE INDEX IX_ehr_scheduled_task_types_Container ON ehr.scheduled_task_types (Container);
CREATE INDEX IX_ehr_lookups_ageclass_Container ON ehr_lookups.ageclass (Container);
CREATE INDEX IX_ehr_lookups_amount_units_Container ON ehr_lookups.amount_units (Container);
CREATE INDEX IX_ehr_lookups_areas_Container ON ehr_lookups.areas (Container);
CREATE INDEX IX_ehr_lookups_billingtypes_Container ON ehr_lookups.billingtypes (Container);
CREATE INDEX IX_ehr_lookups_blood_draw_services_Container ON ehr_lookups.blood_draw_services (Container);
CREATE INDEX IX_ehr_lookups_blood_draw_tube_type_Container ON ehr_lookups.blood_draw_tube_type (Container);
CREATE INDEX IX_ehr_lookups_blood_tube_volumes_Container ON ehr_lookups.blood_tube_volumes (Container);
CREATE INDEX IX_ehr_lookups_cage_Container ON ehr_lookups.cage (Container);
CREATE INDEX IX_ehr_lookups_cage_positions_Container ON ehr_lookups.cage_positions (Container);
CREATE INDEX IX_ehr_lookups_cage_type_Container ON ehr_lookups.cage_type (Container);
CREATE INDEX IX_ehr_lookups_cageclass_Container ON ehr_lookups.cageclass (Container);
CREATE INDEX IX_ehr_lookups_calculated_status_codes_Container ON ehr_lookups.calculated_status_codes (Container);
CREATE INDEX IX_ehr_lookups_clinpath_status_Container ON ehr_lookups.clinpath_status (Container);
CREATE INDEX IX_ehr_lookups_clinpath_tests_Container ON ehr_lookups.clinpath_tests (Container);
CREATE INDEX IX_ehr_lookups_conc_units_Container ON ehr_lookups.conc_units (Container);
CREATE INDEX IX_ehr_lookups_death_remarks_Container ON ehr_lookups.death_remarks (Container);
CREATE INDEX IX_ehr_lookups_disallowed_medications_Container ON ehr_lookups.disallowed_medications (Container);
CREATE INDEX IX_ehr_lookups_divider_types_Container ON ehr_lookups.divider_types (Container);
CREATE INDEX IX_ehr_lookups_dosage_units_Container ON ehr_lookups.dosage_units (Container);
CREATE INDEX IX_ehr_lookups_drug_defaults_Container ON ehr_lookups.drug_defaults (Container);
CREATE INDEX IX_ehr_lookups_flag_categories_Container ON ehr_lookups.flag_categories (Container);
CREATE INDEX IX_ehr_lookups_full_snomed_Container ON ehr_lookups.full_snomed (Container);
CREATE INDEX IX_ehr_lookups_gender_codes_Container ON ehr_lookups.gender_codes (Container);
CREATE INDEX IX_ehr_lookups_lab_test_range_Container ON ehr_lookups.lab_test_range (Container);
CREATE INDEX IX_ehr_lookups_lab_tests_Container ON ehr_lookups.lab_tests (Container);
CREATE INDEX IX_ehr_lookups_labwork_panels_Container ON ehr_lookups.labwork_panels (Container);
CREATE INDEX IX_ehr_lookups_labwork_services_Container ON ehr_lookups.labwork_services (Container);
CREATE INDEX IX_ehr_lookups_labwork_types_Container ON ehr_lookups.labwork_types (Container);
CREATE INDEX IX_ehr_lookups_note_types_Container ON ehr_lookups.note_types (Container);
CREATE INDEX IX_ehr_lookups_parentageTypes_Container ON ehr_lookups.parentageTypes (Container);
CREATE INDEX IX_ehr_lookups_procedure_default_charges_Container ON ehr_lookups.procedure_default_charges (Container);
CREATE INDEX IX_ehr_lookups_procedure_default_codes_Container ON ehr_lookups.procedure_default_codes (Container);
CREATE INDEX IX_ehr_lookups_procedure_default_comments_Container ON ehr_lookups.procedure_default_comments (Container);
CREATE INDEX IX_ehr_lookups_procedure_default_flags_Container ON ehr_lookups.procedure_default_flags (Container);
CREATE INDEX IX_ehr_lookups_procedure_default_treatments_Container ON ehr_lookups.procedure_default_treatments (Container);
CREATE INDEX IX_ehr_lookups_procedures_Container ON ehr_lookups.procedures (Container);
CREATE INDEX IX_ehr_lookups_project_types_Container ON ehr_lookups.project_types (Container);
CREATE INDEX IX_ehr_lookups_relationshipTypes_Container ON ehr_lookups.relationshipTypes (Container);
CREATE INDEX IX_ehr_lookups_request_priority_Container ON ehr_lookups.request_priority (Container);
CREATE INDEX IX_ehr_lookups_restraint_type_Container ON ehr_lookups.restraint_type (Container);
CREATE INDEX IX_ehr_lookups_routes_Container ON ehr_lookups.routes (Container);
CREATE INDEX IX_ehr_lookups_snomap_Container ON ehr_lookups.snomap (Container);
CREATE INDEX IX_ehr_lookups_source_Container ON ehr_lookups.source (Container);
CREATE INDEX IX_ehr_lookups_species_Container ON ehr_lookups.species (Container);
CREATE INDEX IX_ehr_lookups_species_codes_Container ON ehr_lookups.species_codes (Container);
CREATE INDEX IX_ehr_lookups_treatment_frequency_Container ON ehr_lookups.treatment_frequency (Container);
CREATE INDEX IX_ehr_lookups_treatment_frequency_times_Container ON ehr_lookups.treatment_frequency_times (Container);
CREATE INDEX IX_ehr_lookups_usda_codes_Container ON ehr_lookups.usda_codes (Container);
CREATE INDEX IX_ehr_lookups_usda_levels_Container ON ehr_lookups.usda_levels (Container);
CREATE INDEX IX_ehr_lookups_volume_units_Container ON ehr_lookups.volume_units (Container);
CREATE INDEX IX_ehr_lookups_weight_ranges_Container ON ehr_lookups.weight_ranges (Container);
GO

-- add new RowId PK column and per-container unique constraints
ALTER TABLE ehr_lookups.calculated_status_codes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.calculated_status_codes DROP CONSTRAINT PK_calculated_status_codes;
ALTER TABLE ehr_lookups.calculated_status_codes ADD CONSTRAINT PK_calculated_status_codes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.calculated_status_codes ADD CONSTRAINT UQ_calculated_status_codes UNIQUE (Container,Code);
ALTER TABLE ehr_lookups.gender_codes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.gender_codes DROP CONSTRAINT PK_gender_codes;
ALTER TABLE ehr_lookups.gender_codes ADD CONSTRAINT PK_gender_codes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.gender_codes ADD CONSTRAINT UQ_gender_codes UNIQUE (Container,Code);
ALTER TABLE ehr_lookups.routes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.routes DROP CONSTRAINT PK_routes;
ALTER TABLE ehr_lookups.routes ADD CONSTRAINT PK_routes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.routes ADD CONSTRAINT UQ_routes UNIQUE (Container,Route);
ALTER TABLE ehr_lookups.blood_draw_tube_type ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.blood_draw_tube_type DROP CONSTRAINT PK_blood_draw_tube_type;
ALTER TABLE ehr_lookups.blood_draw_tube_type ADD CONSTRAINT PK_blood_draw_tube_type PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.blood_draw_tube_type ADD CONSTRAINT UQ_blood_draw_tube_type UNIQUE (Container,Type);
ALTER TABLE ehr_lookups.blood_tube_volumes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.blood_tube_volumes DROP CONSTRAINT PK_blood_tube_volumes;
ALTER TABLE ehr_lookups.blood_tube_volumes ADD CONSTRAINT PK_blood_tube_volumes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.blood_tube_volumes ADD CONSTRAINT UQ_blood_tube_volumes UNIQUE (Container,Volume);
ALTER TABLE ehr_lookups.blood_draw_services ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.blood_draw_services DROP CONSTRAINT PK_blood_draw_services;
ALTER TABLE ehr_lookups.blood_draw_services ADD CONSTRAINT PK_blood_draw_services PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.blood_draw_services ADD CONSTRAINT UQ_blood_draw_services UNIQUE (Container,Service);
ALTER TABLE ehr_lookups.dosage_units ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.dosage_units DROP CONSTRAINT PK_dosage_units;
ALTER TABLE ehr_lookups.dosage_units ADD CONSTRAINT PK_dosage_units PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.dosage_units ADD CONSTRAINT UQ_dosage_units UNIQUE (Container,Unit);
ALTER TABLE ehr_lookups.conc_units ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.conc_units DROP CONSTRAINT PK_conc_units;
ALTER TABLE ehr_lookups.conc_units ADD CONSTRAINT PK_conc_units PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.conc_units ADD CONSTRAINT UQ_conc_units UNIQUE (Container,Unit);
ALTER TABLE ehr_lookups.volume_units ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.volume_units DROP CONSTRAINT PK_volume_units;
ALTER TABLE ehr_lookups.volume_units ADD CONSTRAINT PK_volume_units PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.volume_units ADD CONSTRAINT UQ_volume_units UNIQUE (Container,Unit);
ALTER TABLE ehr_lookups.amount_units ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.amount_units DROP CONSTRAINT PK_amount_units;
ALTER TABLE ehr_lookups.amount_units ADD CONSTRAINT PK_amount_units PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.amount_units ADD CONSTRAINT UQ_amount_units UNIQUE (Container,Unit);
ALTER TABLE ehr_lookups.flag_categories ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.flag_categories DROP CONSTRAINT PK_flag_categories;
ALTER TABLE ehr_lookups.flag_categories ADD CONSTRAINT PK_flag_categories PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.flag_categories ADD CONSTRAINT UQ_flag_categories UNIQUE (Container,Category);
ALTER TABLE ehr_lookups.weight_ranges ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.weight_ranges DROP CONSTRAINT PK_weight_ranges;
ALTER TABLE ehr_lookups.weight_ranges ADD CONSTRAINT PK_weight_ranges PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.weight_ranges ADD CONSTRAINT UQ_weight_ranges UNIQUE (Container,Species);
ALTER TABLE ehr_lookups.species ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.species DROP CONSTRAINT PK_species;
ALTER TABLE ehr_lookups.species ADD CONSTRAINT PK_species PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.species ADD CONSTRAINT UQ_species UNIQUE (Container,Common);
ALTER TABLE ehr_lookups.species_codes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.species_codes DROP CONSTRAINT PK_species_codes;
ALTER TABLE ehr_lookups.species_codes ADD CONSTRAINT PK_species_codes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.species_codes ADD CONSTRAINT UQ_species_codes UNIQUE (Container,Code);
ALTER TABLE ehr_lookups.parentageTypes ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.parentageTypes ADD CONSTRAINT PK_parentageTypes PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.parentageTypes ADD CONSTRAINT UQ_parentageTypes UNIQUE (Container,Label);
ALTER TABLE ehr_lookups.labwork_services ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.labwork_services DROP CONSTRAINT PK_labwork_services;
ALTER TABLE ehr_lookups.labwork_services ADD CONSTRAINT PK_labwork_services PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.labwork_services ADD CONSTRAINT UQ_labwork_services UNIQUE (Container,ServiceName);
ALTER TABLE ehr_lookups.source ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.source DROP CONSTRAINT PK_source;
ALTER TABLE ehr_lookups.source ADD CONSTRAINT PK_source PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.source ADD CONSTRAINT UQ_source UNIQUE (Container,Code);
ALTER TABLE ehr_lookups.usda_levels ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.usda_levels DROP CONSTRAINT PK_usda_levels;
ALTER TABLE ehr_lookups.usda_levels ADD CONSTRAINT PK_usda_levels PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.usda_levels ADD CONSTRAINT UQ_usda_levels UNIQUE (Container,usda_level);
ALTER TABLE ehr_lookups.areas ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.areas DROP CONSTRAINT PK_areas;
ALTER TABLE ehr_lookups.areas ADD CONSTRAINT PK_areas PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.areas ADD CONSTRAINT UQ_areas UNIQUE (Container,Area);
ALTER TABLE ehr_lookups.cage_positions ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.cage_positions DROP CONSTRAINT PK_cage_positions;
ALTER TABLE ehr_lookups.cage_positions ADD CONSTRAINT PK_cage_positions PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.cage_positions ADD CONSTRAINT UQ_cage_positions UNIQUE (Container,Cage);
ALTER TABLE ehr_lookups.clinpath_tests ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.clinpath_tests DROP CONSTRAINT PK_testname;
ALTER TABLE ehr_lookups.clinpath_tests ADD CONSTRAINT PK_clinpath_tests PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.clinpath_tests ADD CONSTRAINT UQ_clinpath_tests UNIQUE (Container,TestName);
ALTER TABLE ehr_lookups.death_remarks ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.death_remarks DROP CONSTRAINT PK_death_remarks;
ALTER TABLE ehr_lookups.death_remarks ADD CONSTRAINT PK_death_remarks PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.death_remarks ADD CONSTRAINT UQ_death_remarks UNIQUE (Container,Title);
ALTER TABLE ehr_lookups.request_priority ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.request_priority DROP CONSTRAINT PK_request_priority;
ALTER TABLE ehr_lookups.request_priority ADD CONSTRAINT PK_request_priority PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.request_priority ADD CONSTRAINT UQ_request_priority UNIQUE (Container,Priority);
ALTER TABLE ehr_lookups.restraint_type ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.restraint_type DROP CONSTRAINT PK_restraint_type;
ALTER TABLE ehr_lookups.restraint_type ADD CONSTRAINT PK_restraint_type PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.restraint_type ADD CONSTRAINT UQ_restraint_type UNIQUE (Container,Type);
ALTER TABLE ehr_lookups.labwork_types ADD RowId INT IDENTITY(1, 1);
ALTER TABLE ehr_lookups.labwork_types DROP CONSTRAINT PK_labwork_types;
ALTER TABLE ehr_lookups.labwork_types ADD CONSTRAINT PK_labwork_types PRIMARY KEY (RowId);
ALTER TABLE ehr_lookups.labwork_types ADD CONSTRAINT UQ_labwork_types UNIQUE (Container,Type);
GO