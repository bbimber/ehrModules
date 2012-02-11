/*
 * Copyright (c) 2011 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */


alter table ehr.formtypes
  add column permitsSingleIdOnly bool
;



UPDATE ehr.qcStateMetadata
SET DraftData = TRUE
WHERE QCStateLabel = 'Review Requested';