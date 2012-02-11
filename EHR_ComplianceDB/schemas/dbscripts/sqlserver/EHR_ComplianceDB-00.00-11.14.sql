/*
 * Copyright (c) 2011-2012 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

EXEC core.fn_dropifexists '*', 'ehr_compliancedb', 'SCHEMA', NULL ;
go
CREATE SCHEMA ehr_compliancedb;
go
CREATE TABLE ehr_compliancedb.CompletionDates (
    RowId INT IDENTITY(1,1) NOT NULL,
    EmployeeId varchar(255) not null,
    RequirementName varchar(255) not null,
    Date datetime,
    result varchar(500),
    comment varchar(4000),

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_CompletionDates PRIMARY KEY (rowid)
);



CREATE TABLE ehr_compliancedb.EmployeeCategory (
    CategoryName varchar(255),

    CONSTRAINT PK_EmployeeCategory PRIMARY KEY (CategoryName)
);


CREATE TABLE ehr_compliancedb.RequirementsPerEmployee (
    RowId INT IDENTITY(1,1) NOT NULL,
    EmployeeId varchar(255) not null,
    RequirementName varchar(255) not null,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_RequirementsPerEmployee PRIMARY KEY (RowId)
);


CREATE TABLE ehr_compliancedb.EmployeeRequirementExemptions (
    RowId INT IDENTITY(1,1) NOT NULL,
    EmployeeId varchar(255) not null,
    RequirementName varchar(255) not null,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_EmployeeRequirementExemptions PRIMARY KEY (RowId)
);


CREATE TABLE ehr_compliancedb.Employees (
    EmployeeId varchar(255) not null,
    LastName varchar(255) not null,
    FirstName varchar(255),
    Email varchar(255),
    Email2 varchar(255),
    PersonId integer,
    Type varchar(255),
    MajorUDDS varchar(255),
    category varchar(255),
    Title varchar(255),
    Unit varchar(255),
    Supervisor varchar(255),
    EmergencyContact varchar(255),
    EmergencyContactDaytimePhone varchar(255),
    EmergencyContactNighttimePhone varchar(255),
    HomePhone varchar(255),
    OfficePhone varchar(255),
    CellPhone varchar(255),
    Location varchar(255),
    StartDate datetime,
    EndDate datetime,
    Notes varchar(255),
    barrier bit,
    animals bit,
    tissue bit,
    isemployee bit,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_Employees PRIMARY KEY (EmployeeId)
);


CREATE TABLE ehr_compliancedb.RequirementsPerCategory (
    RowId INT IDENTITY(1,1) NOT NULL,
    RequirementName varchar(255) not null,
    Category varchar(255),
    Unit varchar(255),

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_RequirementsPerCategory PRIMARY KEY (RowId)
);


CREATE TABLE ehr_compliancedb.Requirements (
    RequirementName varchar(255) not null,
    Type varchar(255),
    ExpirePeriod integer,
    Required bit,
    Access bit,
    Animals bit,
    Tissues bit,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_Requirements PRIMARY KEY (RequirementName)
);


CREATE TABLE ehr_compliancedb.RequirementType (
    Type varchar(255) not null,

    CONSTRAINT PK_RequirementType PRIMARY KEY (Type)
);


CREATE TABLE ehr_compliancedb.SOPByCategory (
    RowId INT IDENTITY(1,1) NOT NULL,
    SOP_ID varchar(255) not null,
    Category varchar(255) not null,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_SOPByCategory PRIMARY KEY (RowId)
);


CREATE TABLE ehr_compliancedb.SOPs (
    SopId varchar(255) not null,
    Name varchar(255) not null,
    PDF integer,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_SOPs PRIMARY KEY (SopId)
);


CREATE TABLE ehr_compliancedb.EmployeeLocations (
    location varchar(255) not null,

    CONSTRAINT PK_EmployeeLocations PRIMARY KEY (location)
);


CREATE TABLE ehr_compliancedb.EmployeeTypes (
    type varchar(255) not null,

    CONSTRAINT PK_EmployeeTypes PRIMARY KEY (type)
);

CREATE TABLE ehr_compliancedb.EmployeeTitles (
   title varchar(255) not null, 

   CONSTAINT PK_EmployeeTitles PRIMARY KEY (title)
);

CREATE TABLE ehr_compliancedb.unit_names (
    unit varchar(255) not null,
    supervisor varchar(255),
    phone varchar(255),
    address varchar(100),

    CONSTRAINT PK_unit_names PRIMARY KEY (unit)
);


CREATE TABLE ehr_compliancedb.SOPDates (
    RowId INT IDENTITY(1,1) NOT NULL,
    EmployeeId varchar(255) not null,
    sopid varchar(255) not null,
    Date datetime not null,

    Container ENTITYID NOT NULL,
    CreatedBy USERID,
    Created datetime,
    ModifiedBy USERID,
    Modified datetime,

    CONSTRAINT PK_SOPDates PRIMARY KEY (rowid)
);