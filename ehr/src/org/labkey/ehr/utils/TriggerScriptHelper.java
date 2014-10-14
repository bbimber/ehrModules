/*
 * Copyright (c) 2012-2014 LabKey Corporation
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
package org.labkey.ehr.utils;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.collections.CaseInsensitiveHashSet;
import org.labkey.api.data.Aggregate;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.ConvertHelper;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.Results;
import org.labkey.api.data.ResultsImpl;
import org.labkey.api.data.RuntimeSQLException;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.Selector;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.SqlExecutor;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableInfo;
import org.labkey.api.data.TableSelector;
import org.labkey.api.ehr.EHRDemographicsService;
import org.labkey.api.ehr.EHRQCState;
import org.labkey.api.ehr.EHRService;
import org.labkey.api.ehr.dataentry.DataEntryForm;
import org.labkey.api.ehr.demographics.AnimalRecord;
import org.labkey.api.ldk.notification.NotificationService;
import org.labkey.api.query.BatchValidationException;
import org.labkey.api.query.DuplicateKeyException;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.UserSchema;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.Group;
import org.labkey.api.security.MemberType;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.DeletePermission;
import org.labkey.api.security.permissions.InsertPermission;
import org.labkey.api.settings.AppProps;
import org.labkey.api.study.DataSet;
import org.labkey.api.study.Study;
import org.labkey.api.study.StudyService;
import org.labkey.api.util.GUID;
import org.labkey.api.util.JobRunner;
import org.labkey.api.util.MailHelper;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.UnauthorizedException;
import org.labkey.ehr.EHRSchema;
import org.labkey.ehr.dataentry.DataEntryManager;
import org.labkey.ehr.demographics.EHRDemographicsServiceImpl;
import org.labkey.ehr.notification.DeathNotification;
import org.labkey.ehr.security.EHRSecurityManager;

import javax.mail.Address;
import javax.mail.Message;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * User: bbimber
 * Date: 3/1/12
 * Time: 7:23 AM
 */
public class TriggerScriptHelper
{
    private Container _container = null;
    private User _user = null;
    protected final static SimpleDateFormat _dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm");

    //NOTE: consider moving these to SharedCache, to allow them to be shared across scripts, yet reset from admin console
    private Map<Integer, String> _cachedAccounts = new HashMap<>();

    private static final Logger _log = Logger.getLogger(TriggerScriptHelper.class);

    private TriggerScriptHelper(int userId, String containerId)
    {
        _user = UserManager.getUser(userId);
        if (_user == null)
            throw new RuntimeException("User does not exist: " + userId);

        _container = ContainerManager.getForId(containerId);
        if (_container == null)
            throw new RuntimeException("Container does not exist: " + containerId);
    }

    private User getUser()
    {
        return _user;
    }

    private Container getContainer()
    {
        return _container;
    }

    private Study getEHRStudy()
    {
        Container target = EHRService.get().getEHRStudyContainer(_container);
        return StudyService.get().getStudy(target == null ? getContainer() : target);
    }

    public static TriggerScriptHelper create(int userId, String containerId)
    {
        //_log.info("Creating trigger script helper for: " +  userId + ", " + containerId);
        TriggerScriptHelper helper = new TriggerScriptHelper(userId, containerId);

        return helper;
    }

    public void closeActiveDatasetRecords(List<String> queryNames, String id, Date enddate)
    {
        Container container = getContainer();
        User user = getUser();

        for (String queryName : queryNames)
        {
            int datasetId = StudyService.get().getDatasetIdByLabel(container, queryName);
            DataSet dataset = StudyService.get().getDataset(container, datasetId);
            if (dataset == null){
                _log.info("Non existent table: study." + queryName);
                continue;
            }

            //NOTE: this is done direct to the DB for speed.  however we lose auditing, etc.  might want to reconsider
            TableInfo ti = dataset.getTableInfo(user);
            //TODO: move to updateService
            SQLFragment sql = new SQLFragment("UPDATE studydataset." + dataset.getDomain().getStorageTableName() + " SET enddate = ? WHERE participantid = ? AND (enddate IS NULL OR enddate > ?)", enddate, id, enddate);
            new SqlExecutor(ti.getSchema()).execute(sql);
        }
    }

    public void updateProblemsFromCase(String newId, String oldId, String caseId)
    {
        Container container = getContainer();
        User user = getUser();

        int datasetId = StudyService.get().getDatasetIdByLabel(container, "Problem List");
        DataSet dataset = StudyService.get().getDataset(container, datasetId);
        if (dataset == null){
            _log.info("Unable to find problem list dataset");
            return;
        }

        //NOTE: this is done direct to the DB for speed.  however we lose auditing, etc.  might want to reconsider
        TableInfo ti = dataset.getTableInfo(user);
        SQLFragment sql = new SQLFragment("UPDATE studydataset." + dataset.getDomain().getStorageTableName() + " SET participantid = ? WHERE caseid = ? and participantid = ?", newId, caseId, oldId);
        int modified = new SqlExecutor(ti.getSchema()).execute(sql);
        _log.info("updated Id on " + modified + " problems due to Id change on case: " + caseId);
    }

    public void deleteProblemsFromCase(String caseId)
    {
        Container container = getContainer();
        User user = getUser();

        int datasetId = StudyService.get().getDatasetIdByLabel(container, "Problem List");
        DataSet dataset = StudyService.get().getDataset(container, datasetId);
        if (dataset == null){
            _log.info("Unable to find problem list dataset");
            return;
        }

        //NOTE: this is done direct to the DB for speed.  however we lose auditing, etc.  might want to reconsider
        TableInfo ti = dataset.getTableInfo(user);
        SQLFragment sql = new SQLFragment("DELETE FROM studydataset." + dataset.getDomain().getStorageTableName() + " WHERE caseid = ?", caseId);
        int modified = new SqlExecutor(ti.getSchema()).execute(sql);
        _log.info("deleted " + modified + " master problems due to case deletion: " + caseId);
    }

    public void closeActiveProblemsForCase(String id, Date enddate, String caseId)
    {
        Container container = getContainer();
        User user = getUser();

        int datasetId = StudyService.get().getDatasetIdByLabel(container, "Problem List");
        DataSet dataset = StudyService.get().getDataset(container, datasetId);
        if (dataset == null){
            _log.info("Unable to find problem list dataset");
            return;
        }

        //NOTE: this is done direct to the DB for speed.  however we lose auditing, etc.  might want to reconsider
        TableInfo ti = dataset.getTableInfo(user);
        SQLFragment sql = new SQLFragment("UPDATE studydataset." + dataset.getDomain().getStorageTableName() + " SET enddate = ? WHERE participantid = ? AND caseid = ? AND enddate IS NULL", enddate, id, caseId);
        new SqlExecutor(ti.getSchema()).execute(sql);
    }

    public static List<String> getScriptsToLoad(String containerId)
    {
        Container c = ContainerManager.getForId(containerId);

        List<String> scripts = new ArrayList<>();
        for (Resource script : EHRService.get().getExtraTriggerScripts(c))
        {
            scripts.add(script.getPath().toString());
        }

        return Collections.unmodifiableList(scripts);
    }

    public boolean hasPermission(String schemaName, String queryName, String eventName, String originalQCState, String targetQCState)
    {
        return EHRSecurityManager.get().hasPermission(getContainer(), getUser(), schemaName, queryName, EHRSecurityManager.EVENT_TYPE.valueOf(eventName), originalQCState, targetQCState);
    }

    public String getQCStateJson() throws Exception
    {
        try
        {
            Container targetContainer = EHRService.get().getEHRStudyContainer(getContainer());
            Map<String, EHRQCState> qcStates = EHRSecurityManager.get().getQCStateInfo(targetContainer);

            JSONArray json = new JSONArray();
            for (EHRQCState qc : qcStates.values())
            {
                json.put(qc.toJson());
            }
            return json.toString();
        }
        catch (Exception e)
        {
            //NOTE: this is only called by JS triggers, which will bury the exception otherwise
            _log.error(e.getMessage());
            _log.error(e.getStackTrace());
            throw(e);
        }
    }

    public EHRQCState getQCStateForRowId(int rowId)
    {
        Map<String, EHRQCState> qcStates = EHRSecurityManager.get().getQCStateInfo(getContainer());

        for (EHRQCState qc : qcStates.values())
        {
            if (qc.getRowId() == rowId)
                return qc;

        }
        return null;
    }

    public EHRQCState getQCStateForLabel(String label)
    {
        Map<String, EHRQCState> qcStates = EHRSecurityManager.get().getQCStateInfo(getContainer());

        if (qcStates.containsKey(label))
            return qcStates.get(label);
        else
            return null;
    }

    public static void cascadeDelete(int userId, String containerId, String schemaName, String queryName, String keyField, Object keyValue)
    {
        cascadeDelete(userId, containerId, schemaName, queryName, keyField, keyValue, null);
    }

    public static void cascadeDelete(int userId, String containerId, String schemaName, String queryName, String keyField, Object keyValue, String sql)
    {
        User u = UserManager.getUser(userId);
        if (u == null)
            throw new RuntimeException("User does not exist: " + userId);

        Container c = ContainerManager.getForId(containerId);
        if (c == null)
            throw new RuntimeException("Container does not exist: " + containerId);

        DbSchema schema = QueryService.get().getUserSchema(u, c, schemaName).getDbSchema();
        if (schema == null)
            throw new RuntimeException("Unknown schema: " + schemaName);

        TableInfo table = schema.getTable(queryName);
        if (table == null)
            throw new RuntimeException("Unknown table: " + schemaName + "." + queryName);

        if (!c.hasPermission(u, DeletePermission.class))
            throw new UnauthorizedException("User does not have permission to delete from the table: " + table.getPublicName());

        SimpleFilter filter;
        if(sql == null)
        {
            filter = new SimpleFilter(FieldKey.fromString(keyField), keyValue);
        }
        else
        {
            filter = new SimpleFilter();
            filter.addWhereClause(sql, new Object[]{keyValue}, FieldKey.fromParts(keyField));
        }
        Table.delete(table, filter);
    }

    public AnimalRecord getDemographicRecord(String id)
    {
        return EHRDemographicsServiceImpl.get().getAnimal(getContainer(), id);
    }

    public String validateAssignment(String id, Integer projectId, Date date) throws SQLException
    {
        if (id == null || projectId == null || date == null)
            return null;

        String protocol = getProtocolForProject(projectId);
        if (protocol == null)
        {
            return "This project is not associated with a valid protocol";
        }

        TableInfo ti = getTableInfo("study", "Assignment");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);

        filter.addCondition(FieldKey.fromString("date"), date, CompareType.DATE_LTE);
        filter.addClause(new SimpleFilter.OrClause(new CompareType.EqualsCompareClause(FieldKey.fromString("project"), CompareType.EQUAL, projectId), new CompareType.CompareClause(FieldKey.fromString("project/protocol"), CompareType.EQUAL, protocol)));
        filter.addClause(new SimpleFilter.OrClause(new CompareType.EqualsCompareClause(FieldKey.fromString("enddate"), CompareType.DATE_GTE, date), new CompareType.CompareClause(FieldKey.fromString("enddate"), CompareType.ISBLANK, null)));
        filter.addCondition(FieldKey.fromString("qcstate/publicdata"), true, CompareType.EQUAL);

        TableSelector ts = new TableSelector(ti, PageFlowUtil.set("project"), filter, null);
        if (!ts.exists())
        {
            return "Not assigned to the protocol on this date";
        }

        return null;
    }

    public String getAccountForProject(int projectId)
    {
        if (_cachedAccounts.containsKey(projectId))
            return _cachedAccounts.get(projectId);

        TableInfo ti = getTableInfo("ehr", "project");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("project"), projectId);
        TableSelector ts = new TableSelector(ti, Collections.singleton("account"), filter, null);

        String[] ret = ts.getArray(String.class);
        if (ret.length != 1)
            return null;

        _cachedAccounts.put(projectId, ret[0]);

        return ret[0];
    }

    private TableInfo getTableInfo(String schema, String query)
    {
        return getTableInfo(schema, query, false);
    }

    private TableInfo getTableInfo(String schema, String query, boolean suppressError)
    {
        UserSchema us = QueryService.get().getUserSchema(getUser(), getContainer(), schema);
        if (us == null)
        {
            if (!suppressError)
                throw new IllegalArgumentException("Unable to find schema: " + schema);

            return null;
        }

        TableInfo ti = us.getTable(query);
        if (ti == null)
        {
            if (!suppressError)
                throw new IllegalArgumentException("Unable to find table: " + schema + "." + query);

            return null;
        }

        return ti;
    }

    public String getSnomedMeaning(String code)
    {
        if (code == null)
            return null;

        TableInfo ti = EHRSchema.getInstance().getEHRLookupsSchema().getTable(EHRSchema.TABLE_SNOMED);
        TableSelector ts = new TableSelector(ti, Collections.singleton("meaning"), new SimpleFilter(FieldKey.fromString("code"), code), null);
        String[] ret = ts.getArray(String.class);
        if (ret != null && ret.length == 1)
            return ret[0];

        return null;
    }

    public boolean validateHousing(String id, String room, String cage, Date date)
    {
        if (id == null || room == null || date == null)
            return true;

        TableInfo ti = getTableInfo("study", "Housing");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);
        filter.addCondition(FieldKey.fromString("room"), room, CompareType.EQUAL);
        if (cage != null)
            filter.addCondition(FieldKey.fromString("cage"), cage, CompareType.EQUAL);

        filter.addCondition(FieldKey.fromString("date"), date, CompareType.LTE);
        filter.addClause(new SimpleFilter.OrClause(new CompareType.EqualsCompareClause(FieldKey.fromString("enddate"), CompareType.GT, date), new CompareType.CompareClause(FieldKey.fromString("enddate"), CompareType.ISBLANK, null)));
        filter.addCondition(FieldKey.fromString("qcstate/publicdata"), true, CompareType.EQUAL);

        TableSelector ts = new TableSelector(ti, Collections.singleton("Id"), filter, null);
        return ts.getRowCount() > 0;
    }

    public String getProtocolForProject(Integer project)
    {
        if (project == null)
            return null;

        String cacheKey = getProtocolCacheKey();
        Map<Integer, String> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            ret = new HashMap<>();
        }

        if (!ret.containsKey(project))
        {
            TableInfo ti = getTableInfo("ehr", "project");
            TableSelector ts = new TableSelector(ti, Collections.singleton("protocol"), new SimpleFilter(FieldKey.fromString("project"), project), null);
            String[] results = ts.getArray(String.class);
            if (results.length == 1)
            {
                ret.put(project, results[0]);
            }
        }

        DataEntryManager.get().getCache().put(cacheKey, ret);

        return ret.get(project);
    }

    public void updateCachedProtocol(Integer project, String protocol)
    {
        if (project == null)
            return;

        String cacheKey = getProtocolCacheKey();
        Map<Integer, String> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            ret = new HashMap<>();
        }

        ret.put(project, protocol);
        DataEntryManager.get().getCache().put(cacheKey, ret);
    }

    private void cacheAllProtocols()
    {
        TableInfo ti = getTableInfo("ehr", "project");
        TableSelector ts = new TableSelector(ti, PageFlowUtil.set("project", "protocol"), new SimpleFilter(FieldKey.fromString("container"), getContainer().getId(), CompareType.EQUAL), null);
        ts.forEach(new Selector.ForEachBlock<ResultSet>()
        {
            @Override
            public void exec(ResultSet rs) throws SQLException
            {
                updateCachedProtocol(rs.getInt("project"), rs.getString("protocol"));
            }
        });
    }

    private String getProtocolCacheKey()
    {
        return this.getClass().getName() + "||" + getContainer().getId() + "||" + "projectProtocol";
    }

    public String lookupDatasetForService(String service)
    {
        Map<String, Map<String, Object>> map = getLabworkServices();
        if (map.containsKey(service))
            return (String)map.get(service).get("dataset");

        return null;
    }

    public String lookupChargeTypeForService(String service)
    {
        Map<String, Map<String, Object>> map = getLabworkServices();
        if (map.containsKey(service))
            return (String)map.get(service).get("chargetype");

        return null;
    }

    public Map<String, Map<String, Object>> getLabworkServices()
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||" + "labworkServices";
        Map<String, Map<String, Object>> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            _log.info("caching labwork_services in TriggerScriptHelper");
            TableInfo ti = getTableInfo("ehr_lookups", "labwork_services");
            TableSelector ts = new TableSelector(ti);
            ret = new HashMap<>();
            for (Map<String, Object> row : ts.getMapArray())
            {
                ret.put((String)row.get("servicename"), row);
            }

            DataEntryManager.get().getCache().put(cacheKey, ret);
        }

        return ret;
    }

    public Map<String, Object> getWeightRangeForSpecies(String species)
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||weightRangeMap";
        Map<String, Map<String, Object>> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            _log.info("caching weight_ranges in TriggerScriptHelper");
            ret = new HashMap<>();
            TableInfo ti = getTableInfo("ehr_lookups", "weight_ranges");
            TableSelector ts = new TableSelector(ti);
            Map<String, Object>[] result = ts.getMapArray();
            for (Map<String, Object> row : result)
            {
                if (row.containsKey(row.get("species")))
                {
                    _log.warn("More than 1 row found per species in ehr_lookups.weight_ranges");
                }

                ret.put((String)row.get("species"), row);
            }
            DataEntryManager.get().getCache().put(cacheKey, ret);
        }

        return ret.get(species);
    }

    public String verifyWeightRange(String id, Double weight, String species)
    {
        if (species == null)
            return null;

        if (weight == null)
            return null;


        Map<String, Object> row = getWeightRangeForSpecies(species);
        if (row == null)
            return null;

        Double minWeight = (Double)row.get("min_weight");
        Double maxWeight = (Double)row.get("max_weight");
        if (minWeight != null && weight < minWeight)
        {
            return "Weight below the allowable value of " + minWeight + " kg for " + species;
        }

        if (maxWeight != null && weight > maxWeight)
        {
            return "Weight above the allowable value of " + maxWeight + " kg for " + species;
        }

        return null;
    }

    public void announceIdsModified(String schema, String query, List<String> ids)
    {
        //TODO: consider making this run asynchronously in case a deadlock kills it.  this could cause the client request to fail, even when these records do exist
        EHRDemographicsServiceImpl.get().reportDataChange(getContainer(), schema, query, ids, false);
    }

    public void insertWeight(String id, Date date, Double weight) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException
    {
        if (id == null || date == null || weight == null)
            return;

        TableInfo ti = getTableInfo("study", "weight");

        Map<String, Object> row = new CaseInsensitiveHashMap<>();
        row.put("Id", id);
        row.put("date", date);
        row.put("weight", weight);

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);
        BatchValidationException errors = new BatchValidationException();
        ti.getUpdateService().insertRows(getUser(), getContainer(), rows, errors, getExtraContext());

        if (errors.hasErrors())
            throw errors;
    }

    public void createHousingRecord(String id, Date date, @Nullable String enddate, String room, @Nullable String cage) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException
    {
        if (id == null || date == null || room == null)
            return;

        TableInfo ti = getTableInfo("study", "housing");

        //if this animal already has an active housing record, assume that is the current record and skip
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);
        filter.addCondition(FieldKey.fromString("isActive"), true);
        TableSelector ts = new TableSelector(ti, PageFlowUtil.set("lsid"), filter, null);
        if (ts.exists())
        {
            _log.info("animal already has an active housing record, skipping");
            return;
        }

        Map<String, Object> row = new CaseInsensitiveHashMap<>();
        row.put("Id", id);
        row.put("date", date);
        row.put("room", room);
        row.put("objectid", new GUID().toString());

        if (enddate != null)
            row.put("enddate", enddate);

        if (cage != null)
            row.put("cage", cage);

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);
        BatchValidationException errors = new BatchValidationException();
        ti.getUpdateService().insertRows(getUser(), getContainer(), rows, errors, getExtraContext());

        if (errors.hasErrors())
            throw errors;
    }

    public void createBirthRecord(String id, Map<String, Object> props) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException
    {
        if (id == null)
            return;

        TableInfo ti = getTableInfo("study", "birth");
        Map<String, Object> row = new CaseInsensitiveHashMap<>();
        row.putAll(props);
        if (!row.containsKey("objectid"))
        {
            row.put("objectid", new GUID().toString());
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);
        BatchValidationException errors = new BatchValidationException();
        ti.getUpdateService().insertRows(getUser(), getContainer(), rows, errors, getExtraContext());

        if (errors.hasErrors())
            throw errors;
    }

    public void createDemographicsRecord(String id, Map<String, Object> props) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException
    {
        if (id == null)
            return;

        TableInfo ti = getTableInfo("study", "demographics");
        TableSelector ts = new TableSelector(ti, new SimpleFilter(FieldKey.fromString("Id"), id), null);
        if (ts.exists())
        {
            _log.info("Id already exists, no need to create demographics record: " + id);
            return;
        }


        Map<String, Object> row = new CaseInsensitiveHashMap<>();
        row.putAll(props);
        if (!row.containsKey("objectid"))
        {
            row.put("objectid", new GUID().toString());
        }

        EHRQCState qc = getQCStateForLabel("Completed");
        if (qc != null)
            row.put("qcstate", qc.getRowId());

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row);
        BatchValidationException errors = new BatchValidationException();
        ti.getUpdateService().insertRows(getUser(), getContainer(), rows, errors, getExtraContext());
        if (errors.hasErrors())
            throw errors;

        EHRDemographicsServiceImpl.get().getAnimal(getContainer(), id);
    }

    public void updateDemographicsRecord(String id, Map<String, Object> props) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException, InvalidKeyException
    {
        if (id == null || props == null || props.isEmpty())
            return;

        TableInfo ti = getTableInfo("study", "demographics");
        TableSelector ts = new TableSelector(ti, Collections.singleton("lsid"), new SimpleFilter(FieldKey.fromString("Id"), id), null);
        String lsid = ts.getObject(String.class);
        if (lsid != null)
        {
            Map<String, Object> row = new CaseInsensitiveHashMap<>();
            Map<String, Object> keyRow = new CaseInsensitiveHashMap<>();
            row.putAll(props);
            row.put("lsid", lsid);
            keyRow.put("lsid", lsid);

            ti.getUpdateService().updateRows(getUser(), getContainer(), Arrays.asList(row), Arrays.asList(keyRow), getExtraContext());

            EHRDemographicsService.get().getAnimal(getContainer(), id);
        }
        else
        {
            _log.error("Unable to find demographics record for id: " + id);
        }
    }

    public Map<String, Object> getExtraContext()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("quickValidation", true);
        map.put("generatedByServer", true);

        return map;
    }

    public Long findExistingAnimalsInCage(String id, String room, @NotNull String cage)
    {
        if (id == null || room == null || cage == null)
            return null;

        TableInfo ti = getTableInfo("study", "housing");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id, CompareType.NEQ);
        filter.addCondition(FieldKey.fromString("isActive"), true, CompareType.EQUAL);
        filter.addCondition(FieldKey.fromString("room"), room, CompareType.EQUAL);
        filter.addCondition(FieldKey.fromString("cage"), cage, CompareType.EQUAL);

        TableSelector ts = new TableSelector(ti, Collections.singleton("Id"), filter, null);

        return ts.getRowCount();
    }

    public String getCalculatedStatusValue(String id)
    {
        if (id == null)
            return null;

        AnimalRecord ar = EHRDemographicsServiceImpl.get().getAnimal(getContainer(), id);
        if (ar == null)
            return "Unknown";

        if (ar.getCalculatedStatus() != null)
            return ar.getCalculatedStatus();

        if (ar.getDeath() != null && ar.getMostRecentDeparture() != null)
        {
            return ar.getMostRecentDeparture().before(ar.getDeath()) ? "Shipped" : "Dead";
        }

        if (ar.getDeath() != null)
            return "Dead";

        if (ar.getMostRecentDeparture() != null)
            return "Shipped";

        if (ar.getCurrentRoom() != null)
            return "Alive";

        return null;
    }

    public String[] validateBloodAdditionalServices(String services, String tubeType, Double quantity)
    {
        services = StringUtils.trimToNull(services);
        if (services == null)
            return null;

        List<String> testNames = Arrays.asList(StringUtils.split(services, ","));
        if (testNames.size() == 0)
            return null;

        Map<String, Map<String, Object>> serviceMap = getBloodDrawServicesMap();
        Set<String> msgs = new HashSet<>();

        Double accumulatedMinVol = 0.0;
        for (String testName : testNames)
        {
            Map<String, Object> map = serviceMap.get(testName);
            if (map == null)
            {
                msgs.add("Unknown service: " + testName);
                continue;
            }

            if (map.containsKey("requiredtubetype"))
            {
                String requiredType = (String)map.get("requiredtubetype");
                if (requiredType != null && !requiredType.equalsIgnoreCase(tubeType))
                {
                    msgs.add(testName + " requires a tube type of: " + requiredType);
                }
            }

            if (map.containsKey("minvolume"))
            {
                Double minVolume = (Double)map.get("minvolume");
                if (minVolume != null && minVolume > 0)
                {
                    accumulatedMinVol += minVolume;
                    if (quantity < minVolume)
                    {
                        msgs.add("Quantity below volume required for " + testName + ": " + minVolume);
                        continue;
                    }
                }
            }

            if (accumulatedMinVol > 0 && quantity < accumulatedMinVol)
            {
                msgs.add("Quantity below total volume required for services: " + accumulatedMinVol);
            }
        }

        if (msgs.size() == 0)
        {
            return null;
        }
        else
        {
            return msgs.toArray(new String[msgs.size()]);
        }
    }

    private Map<String, Map<String, Object>> getBloodDrawServicesMap()
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||" + "bloodDrawServices";
        Map<String, Map<String, Object>> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            TableInfo ti = getTableInfo("ehr_lookups", "blood_draw_services");
            ret = new HashMap<String, Map<String, Object>>();

            _log.info("caching blood_draw_services in TriggerScriptHelper");
            TableSelector ts = new TableSelector(ti);
            for (Map<String, Object> row : ts.getMapArray())
            {
                ret.put((String)row.get("service"), row);
            }

            DataEntryManager.get().getCache().put(cacheKey, ret);
        }

        return ret;
    }

    public boolean isDefaultProject(Integer project)
    {
        if (project == null)
            return false;

        return getDefaultProjects().contains(project);
    }

    public Set<Integer> getDefaultProjects()
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||" + "defaultProjects";
        Set<Integer> ret = (Set)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            TableInfo ti = getTableInfo("ehr", "project");
            SimpleFilter filter = new SimpleFilter(FieldKey.fromString("alwaysavailable"), true, CompareType.EQUAL);
            filter.addCondition(FieldKey.fromString("enddateCoalesced"), new Date(), CompareType.DATE_GTE);

            _log.info("caching projects in TriggerScriptHelper");
            TableSelector ts = new TableSelector(ti, PageFlowUtil.set("project"), filter, null);
            ret = new HashSet<>();
            ret.addAll(Arrays.asList(ts.getArray(Integer.class)));

            DataEntryManager.get().getCache().put(cacheKey, Collections.unmodifiableSet(ret));
        }

        return ret;
    }

    //NOTE: the interval start/stop are inclusive
    private Double getOtherDrawsQuantity(String id, Date intervalStart, Date intervalStop, List<Map<String, Object>> recordsInTransaction, String rowObjectId, Double rowQuantity)
    {
        intervalStart = DateUtils.truncate(intervalStart, Calendar.DATE);
        intervalStop = DateUtils.truncate(intervalStop, Calendar.DATE);

        //if provided, we inspect the other records in this transaction and add their values
        //first determine which other records from this transaction should be considered
        Set<String> ignoredObjectIds = new HashSet<>();
        double quantityInTransaction = 0.0;

        //NOTE: we expect this will not contain the current row, but check for it anyway
        boolean foundRow = false;
        if (recordsInTransaction != null)
        {
            for (Map<String, Object> origMap : recordsInTransaction)
            {
                Map<String, Object> map = new CaseInsensitiveHashMap<>(origMap);
                if (!map.containsKey("date"))
                {
                    _log.warn("TriggerScriptHelper.verifyBloodVolume was passed a previous record lacking a date");
                    continue;
                }

                //NOTE: it would be useful to consider QCState, but we need to include pending requests in the current transaction, which this would exclude
//                EHRQCState qc = null;
//                if (map.containsKey("QCState"))
//                {
//                    Integer i = ConvertHelper.convert(map.get("QCState"), Integer.class);
//                    qc = getQCStateForRowId(i);
//                }
//                else if (map.containsKey("QCStateLabel"))
//                {
//                    qc = getQCStateForLabel((String)map.get("QCStateLabel"));
//                }
//
//                if (qc != null && (!qc.isPublicData() && !qc.isDraftData()))
//                {
//                    _log.info("skipping blood row due to QCState");
//                    continue;
//                }

                try
                {
                    Date d = ConvertHelper.convert(map.get("date"), Date.class);
                    d = DateUtils.truncate(d, Calendar.DATE);
                    if (d.getTime() >= intervalStart.getTime() && d.getTime() <= intervalStop.getTime())
                    {
                        Double quantity = ConvertHelper.convert(map.get("quantity"), Double.class);
                        if (quantity != null)
                            quantityInTransaction += quantity;
                    }

                    String objectId = ConvertHelper.convert(map.get("objectid"), String.class);
                    if (objectId != null)
                    {
                        ignoredObjectIds.add(objectId);

                        if (objectId.equalsIgnoreCase(rowObjectId))
                        {
                            foundRow = true;
                        }
                    }
                }
                catch (ConversionException e)
                {
                    _log.error("TriggerScriptHelper.verifyBloodVolume was unable to parse date", e);
                    continue;
                }
            }
        }

        if (!foundRow)
        {
            quantityInTransaction += rowQuantity;
            ignoredObjectIds.add(rowObjectId);
        }

        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);
        filter.addCondition(FieldKey.fromString("date"), intervalStart, CompareType.DATE_GTE);
        filter.addCondition(FieldKey.fromString("date"), intervalStop, CompareType.DATE_LTE);
        filter.addCondition(FieldKey.fromString("quantity"), null, CompareType.NONBLANK);

        if (ignoredObjectIds != null && ignoredObjectIds.size() > 0)
            filter.addCondition(FieldKey.fromString("objectid"), ignoredObjectIds, CompareType.NOT_IN);

        filter.addClause(new SimpleFilter.OrClause(new CompareType.EqualsCompareClause(FieldKey.fromString("qcstate/metadata/DraftData"), CompareType.EQUAL, true), new CompareType.EqualsCompareClause(FieldKey.fromString("qcstate/publicdata"), CompareType.EQUAL, true)));

        TableInfo ti = getTableInfo("study", "Blood Draws");
        TableSelector ts = new TableSelector(ti, Collections.singleton("quantity"), filter, null);

        for (Double q : ts.getArray(Double.class))
        {
            quantityInTransaction += q;
        }

        return quantityInTransaction;
    }

    private Double extractWeightForId(String id, List<Map<String, Object>> weightsInTransaction)
    {
        if (weightsInTransaction == null)
            return null;

        Double weight = null;
        Date lastDate = null;

        for (Map<String, Object> origMap : weightsInTransaction)
        {
            Map<String, Object> map = new CaseInsensitiveHashMap<Object>(origMap);
            if (!map.containsKey("date"))
            {
                _log.warn("TriggerScriptHelper.extractWeightForId was passed a previous record lacking a date");
                continue;
            }

            try
            {
                Date d = ConvertHelper.convert(map.get("date"), Date.class);
                if (d == null)
                    continue;

                if (lastDate == null || d.after(lastDate))
                {
                    Double w = ConvertHelper.convert(map.get("weight"), Double.class);
                    if (w != null)
                    {
                        lastDate = d;
                        weight = w;
                    }
                }
            }
            catch (ConversionException e)
            {
                _log.error("TriggerScriptHelper.extractWeightForId was unable to parse date", e);
                continue;
            }
        }

        return weight;
    }

    public String verifyBloodVolume(String id, Date date, List<Map<String, Object>> recordsInTransaction, List<Map<String, Object>> weightsInTransaction, String objectId, Double quantity)
    {
        if (id == null || date == null || quantity == null)
            return null;

        AnimalRecord ar = EHRDemographicsServiceImpl.get().getAnimal(getContainer(), id);
        if (ar == null)
            return null;

        String species = ar.getSpecies();
        if (species == null)
            return "Unknown species, unable to calculate allowable blood volume";

        Double weight = extractWeightForId(id, weightsInTransaction);
        if (weight == null)
            weight = ar.getMostRecentWeight();

        if (weight == null)
            return "Unknown weight, unable to calculate allowable blood volume";

        Map<String, Object> bloodBySpecies = getBloodForSpecies(species);
        if (bloodBySpecies == null)
            return "Unable to calculate allowable blood volume";

        Double bloodPerKg = (Double)bloodBySpecies.get("blood_per_kg");
        Integer interval = ((Double)bloodBySpecies.get("blood_draw_interval")).intValue();
        Double maxDrawPct = (Double)bloodBySpecies.get("max_draw_pct");
        if (bloodPerKg == null || interval == null || maxDrawPct == null)
            return "Unable to calculate allowable blood volume";

        Double maxAllowable = Math.round((weight * bloodPerKg * maxDrawPct) * 100) / 100.0;

        //find draws over the interval
        Calendar intervalMin = Calendar.getInstance();
        intervalMin.setTime(date);
        intervalMin.add(Calendar.DATE, (-1 * interval) + 1);  //draws drop off on the morning of the nth date

        Double bloodPrevious = getOtherDrawsQuantity(id, intervalMin.getTime(), date, recordsInTransaction, objectId, quantity);
        if (bloodPrevious == null)
            bloodPrevious = 0.0;

        if (bloodPrevious > maxAllowable)
        {
            return "Blood volume of " + quantity + " (" + bloodPrevious + " total) exceeds allowable volume of " + maxAllowable + " mL over the previous " + interval + " days (" + weight + " kg)";
        }

        // if we didnt already have an error, check the future interval
        Calendar intervalMax = Calendar.getInstance();
        intervalMax.setTime(date);
        intervalMax.add(Calendar.DATE, interval);

        Double bloodFuture = getOtherDrawsQuantity(id, date, intervalMax.getTime(), recordsInTransaction, objectId, quantity);
        if (bloodFuture == null)
            bloodFuture = 0.0;

        if (bloodFuture > maxAllowable)
        {
            return "Blood volume of " + bloodFuture + " conflicts with future draws. Max allowable is : " + maxAllowable + " mL (" + weight + " kg)";
        }

        return null;
    }

    private Map<String, Object> getBloodForSpecies(String species)
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||" + "bloodBySpecies";
        Map<String, Map<String, Object>> ret = (Map)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            _log.info("caching blood by species in TriggerScriptHelper");
            TableInfo ti = getTableInfo("ehr_lookups", "species");
            TableSelector ts = new TableSelector(ti);
            ret = new HashMap<>();
            for (Map<String, Object> row : ts.getMapArray())
            {
                ret.put((String)row.get("common"), row);
            }

            DataEntryManager.get().getCache().put(cacheKey, ret);
        }

        return ret.get(species);
    }

    public void processDeniedRequests(final List<String> requestIds)
    {
        JobRunner.getDefault().execute(new Runnable(){
            public void run()
            {
                _log.info("processing cancelled/denied request email for " + requestIds.size() + " records");

                TableInfo requestTable = getTableInfo("ehr", "requests");
                SimpleFilter filter = new SimpleFilter(FieldKey.fromString("requestid"), requestIds, CompareType.IN);
                TableSelector ts = new TableSelector(requestTable, filter, null);

                ts.forEach(new Selector.ForEachBlock<ResultSet>()
                {
                    @Override
                    public void exec(ResultSet rs) throws SQLException
                    {
                        String requestid = rs.getString("requestid");
                        Integer notify1 = rs.getInt("notify1");
                        Integer notify2 = rs.getInt("notify2");
                        Integer notify3 = rs.getInt("notify3");
                        boolean sendemail = rs.getObject("sendemail") == null ? false : rs.getBoolean("sendemail");
                        String title = rs.getString("title");
                        String formtype = rs.getString("formtype");

                        if (sendemail)
                        {
                            String subject = "EHR " + formtype + " Cancelled/Denied";
                            Set<UserPrincipal> recipients = getRecipients(notify1, notify2, notify3);
                            if (recipients.size() == 0)
                            {
                                _log.warn("No recipients, unable to send EHR trigger script email");
                                return;
                            }

                            StringBuilder html = new StringBuilder();

                            html.append("One or more records from the request titled " + title + " have been cancelled or denied.  ");
                            html.append("<a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/ehr" + getContainer().getPath() + "/dataEntryFormDetails.view?requestId=" + requestid + "&formType=" + formtype + "'>");
                            html.append("Click here to view them</a>.  <p>");

                            sendMessage(subject, html.toString(), recipients);
                        }

                        DataEntryForm def = DataEntryManager.get().getFormByName(formtype, getContainer(), getUser());
                        if (def != null)
                        {
                            boolean hasRecords = false;
                            for (TableInfo ti : def.getTables())
                            {
                                if (ti.getName().equalsIgnoreCase("requests"))
                                    continue;

                                SimpleFilter filter = new SimpleFilter(FieldKey.fromString("requestId"), requestid, CompareType.EQUAL);
                                filter.addCondition(FieldKey.fromString("qcstate/label"), PageFlowUtil.set(EHRService.QCSTATES.RequestDenied.getLabel(), EHRService.QCSTATES.RequestCancelled.getLabel()), CompareType.NOT_IN);
                                TableSelector ts = new TableSelector(ti, Collections.singleton("requestId"), filter, null);
                                if (ts.exists())
                                {
                                    hasRecords = true;
                                    break;
                                }
                            }

                            if (!hasRecords)
                            {
                                _log.info("cancelling request since all children are cancelled");
                                Map<String, Object> toUpdate = new CaseInsensitiveHashMap<>();
                                toUpdate.put("qcstate", EHRService.QCSTATES.RequestCancelled.getQCState(getContainer()).getRowId());
                                toUpdate.put("requestid", requestid);
                                Table.update(getUser(), EHRSchema.getInstance().getSchema().getTable(EHRSchema.TABLE_REQUESTS), toUpdate, requestid);
                            }
                        }
                    }
                });
            }
        });
    }

    public void processCompletedRequests(final List<String> requestIds) throws Exception
    {
        JobRunner.getDefault().execute(new Runnable(){
            public void run()
            {
                _log.info("processing completed request email for " + requestIds.size() + " records");

                TableInfo requestTable = getTableInfo("ehr", "requests");
                SimpleFilter filter = new SimpleFilter(FieldKey.fromString("requestid"), requestIds, CompareType.IN);
                TableSelector ts = new TableSelector(requestTable, filter, null);

                ts.forEach(new Selector.ForEachBlock<ResultSet>()
                {
                    @Override
                    public void exec(ResultSet rs) throws SQLException
                    {
                        String requestid = rs.getString("requestid");
                        Integer notify1 = rs.getInt("notify1");
                        Integer notify2 = rs.getInt("notify2");
                        Integer notify3 = rs.getInt("notify3");
                        boolean sendemail = rs.getObject("sendemail") == null ? false : rs.getBoolean("sendemail");
                        String title = rs.getString("title");
                        String formtype = rs.getString("formtype");

                        if (sendemail)
                        {
                            String subject = "EHR " + formtype + " Completed";
                            Set<UserPrincipal> recipients = getRecipients(notify1, notify2, notify3);
                            if (recipients.size() == 0)
                            {
                                _log.warn("No recipients, unable to send EHR trigger script email");
                                return;
                            }

                            StringBuilder html = new StringBuilder();

                            html.append("One or more records from the request titled " + title + " have been marked completed.  ");
                            html.append("<a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/ehr" + getContainer().getPath() + "/dataEntryFormDetails.view?requestId=" + requestid + "&formType=" + formtype + "'>");
                            html.append("Click here to view them</a>.  <p>");

                            sendMessage(subject, html.toString(), recipients);
                        }

                        DataEntryForm def = DataEntryManager.get().getFormByName(formtype, getContainer(), getUser());
                        if (def != null)
                        {
                            boolean hasRecords = false;
                            for (TableInfo ti : def.getTables())
                            {
                                if (ti.getName().equalsIgnoreCase("requests"))
                                    continue;

                                SimpleFilter filter = new SimpleFilter(FieldKey.fromString("requestId"), requestid, CompareType.EQUAL);
                                filter.addCondition(FieldKey.fromString("qcstate/label"), EHRService.QCSTATES.Completed.getLabel(), CompareType.EQUAL);
                                TableSelector ts = new TableSelector(ti, Collections.singleton("requestId"), filter, null);
                                if (ts.exists())
                                {
                                    hasRecords = true;
                                    break;
                                }
                            }

                            if (!hasRecords)
                            {
                                _log.info("completed request since all children are completed");
                                Map<String, Object> toUpdate = new CaseInsensitiveHashMap<>();
                                toUpdate.put("qcstate", EHRService.QCSTATES.Completed.getQCState(getContainer()).getRowId());
                                toUpdate.put("requestid", requestid);
                                Table.update(getUser(), EHRSchema.getInstance().getSchema().getTable(EHRSchema.TABLE_REQUESTS), toUpdate, requestid);
                            }
                        }
                    }
                });
            }
        });
    }

    private Set<UserPrincipal> getRecipients(Integer... userIds)
    {
        Set<UserPrincipal> recipients = new HashSet<>();
        for (Integer userId : userIds)
        {
            if (userId > 0)
            {
                UserPrincipal up = SecurityManager.getPrincipal(userId);
                if (up != null)
                {
                    if (up instanceof  User)
                    {
                        recipients.add((User)up);
                    }
                    else
                    {
                        for (UserPrincipal u : SecurityManager.getAllGroupMembers((Group)up, MemberType.ACTIVE_USERS))
                        {
                            if (((User)u).isActive())
                                recipients.add((User)u);
                        }
                    }
                }
            }
        }

        return recipients;
    }

    public void onAnimalArrival(String id, Map<String, Object> row) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException
    {
        Map<String, Object> demographicsProps = new HashMap<String, Object>();
        for (String key : new String[]{"Id", "gender", "species", "dam", "sire", "origin", "source", "geographic_origin", "birth"})
        {
            if (row.containsKey(key))
            {
                demographicsProps.put(key, row.get(key));
            }
        }
        demographicsProps.put("date", row.get("birth"));
        demographicsProps.put("calculated_status", "Alive");
        createDemographicsRecord(id, demographicsProps);

        if (row.containsKey("birth"))
        {
            Map<String, Object> birthProps = new HashMap<>();
            for (String key : new String[]{"Id", "dam", "sire"})
            {
                if (row.containsKey(key))
                {
                    birthProps.put(key, row.get(key));
                }
            }
            birthProps.put("date", row.get("birth"));

            createBirthRecord(id, birthProps);
        }
    }

    private Date findMostRecentDate(String id, Date lastVal, Map<String, List<Date>> otherVals)
    {
        if (otherVals != null && otherVals.containsKey(id))
        {
            for (Object obj : otherVals.get(id))
            {
                try
                {
                    Date d = ConvertHelper.convert(obj, Date.class);
                    if (d != null && (lastVal == null || d.after(lastVal)))
                    {
                        lastVal = d;
                    }
                }
                catch (ConversionException e)
                {
                    _log.warn("Improper date: " + obj, e);
                }
            }
        }

        return lastVal;
    }

    public Set<String> hasDemographicsRecord(List<String> ids)
    {
        TableInfo ti = getTableInfo("study", "Demographics");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), new HashSet<>(ids), CompareType.IN);
        filter.addCondition(FieldKey.fromString("qcstate/publicdata"), true);

        TableSelector ts = new TableSelector(ti, Collections.singleton("Id"), filter, null);

        Set<String> ret = new HashSet<String>();
        ret.addAll(Arrays.asList(ts.getArray(String.class)));

        return ret;
    }

    public void updateStatusField(List<String> ids, Map<String, List<Date>> liveBirths, Map<String, List<Date>> arrivals, Map<String, List<Date>> deaths, Map<String, List<Date>> departures) throws QueryUpdateServiceException, DuplicateKeyException, SQLException, BatchValidationException, InvalidKeyException
    {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        Set<String> idsInDemographics = hasDemographicsRecord(ids);
        for (String id : ids)
        {
            if (!idsInDemographics.contains(id))
            {
                _log.info("ID not in demographics table, cannot update status: " + id);
                continue;
            }

            String existingStatus = getDemographicRecord(id).getCalculatedStatus();

            Date lastArrival = findMostRecentDate(id, getMostRecentDate(id, getTableInfo("study", "Arrival"), null), arrivals);
            Date lastDeath = findMostRecentDate(id, getMostRecentDate(id, getTableInfo("study", "Deaths"), null), deaths);
            Date lastDeparture = findMostRecentDate(id, getMostRecentDate(id, getTableInfo("study", "Departure"), null), departures);

            // NOTE: this behavior around live births is an imperfect way to mesh WNPRC/ONPRC rules.  ONPRC records records in the birth table,
            // including dead infants.  all records in the WNPRC table are of live births.  checking for the column 'birth_condition' column is a crude proxy for this
            TableInfo birthTable = getTableInfo("study", "birth");
            Date lastDeadBirth = birthTable.getColumnNameSet().contains("birth_condition") ? findMostRecentDate(id, getMostRecentDate(id, birthTable, new SimpleFilter(FieldKey.fromString("birth_condition/alive"), false)), null) : null;
            Date lastLiveBirth = findMostRecentDate(id, getMostRecentDate(id, birthTable, (birthTable.getColumnNameSet().contains("birth_condition") ? new SimpleFilter(FieldKey.fromString("birth_condition/alive"), false, CompareType.NEQ_OR_NULL) : null)), liveBirths);

            String status = null;
            if (lastDeath != null || lastDeadBirth != null)
            {
                status = "Dead";
            }
            else if (lastDeparture != null)
            {
                if (lastArrival != null && lastArrival.after(lastDeparture))
                {
                    status = "Alive";
                }
                else
                {
                    status = "Shipped";
                }
            }
            else if (lastLiveBirth != null || lastArrival != null)
            {
                status = "Alive";
            }
            else
            {
                status = "Unknown";
            }

            if (!status.equals(existingStatus))
            {
                Map<String, Object> row = new CaseInsensitiveHashMap<>();
                row.put("Id", id);
                row.put("calculated_status", status);
                rows.add(row);
            }
        }

        //now perform the actual update
        if (!rows.isEmpty())
        {
            TableInfo ti = getTableInfo("study", "Demographics");
            ti.getUpdateService().updateRows(getUser(), getContainer(), rows, rows, getExtraContext());
            EHRDemographicsServiceImpl.get().getAnimals(getContainer(), ids);
        }
        else
        {
            _log.info("No need to update calculated_status");
        }
    }

    //find most recent record date for the passed Id/table
    private Date getMostRecentDate(String id, TableInfo ti, @Nullable SimpleFilter additionalFilter)
    {
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);
        filter.addCondition(FieldKey.fromString("qcstate/publicdata"), true);
        if (additionalFilter != null)
        {
            filter.addAllClauses(additionalFilter);
        }

        TableSelector ts = new TableSelector(ti, Collections.singleton("date"), filter, null);
        Map<String, List<Aggregate.Result>> aggs = ts.getAggregates(Collections.singletonList(new Aggregate(FieldKey.fromString("date"), Aggregate.Type.MAX)));
        for (List<Aggregate.Result> ag : aggs.values())
        {
            for (Aggregate.Result r : ag)
            {
                if (r.getValue() instanceof Date)
                {
                    return (Date)r.getValue();
                }
            }
        }

        return null;
    }

    //we want to catch situations where we insert an opened-ended housing record
    //when there is already another opened ended record with a later date.
    //if we inserted an open ended record and there is a pre-existing one with an earlier start date, this is ok since the latter will be closed out
    private Date getOpenEndedHousingOverlaps(String id, Date date, List<Map<String, Object>> recordsInTransaction, String rowObjectId)
    {
        //if provided, we inspect the other records in this transaction and add their values
        //first determine which other records from this transaction should be considered
        Set<String> ignoredObjectIds = new HashSet<>();
        Date highestOpenEnded = null;

        if (recordsInTransaction != null && recordsInTransaction.size() > 0)
        {
            for (Map<String, Object> origMap : recordsInTransaction)
            {
                Map<String, Object> map = new CaseInsensitiveHashMap<>(origMap);
                if (!map.containsKey("date"))
                {
                    _log.warn("TriggerScriptHelper.getOpenEndedHousingOverlaps was passed a previous record lacking a date");
                    continue;
                }

                //NOTE: we now include all records from any QCState, since housing typically doesnt allow in progress records
                try
                {
                    Date now = new Date();
                    Date start = ConvertHelper.convert(map.get("date"), Date.class);
                    Date end = ConvertHelper.convert(map.get("enddate"), Date.class);

                    String objectId = ConvertHelper.convert(map.get("objectid"), String.class);
                    if (objectId != null)
                    {
                        ignoredObjectIds.add(objectId);

                        //dont let the current row block submission
                        if (objectId.equals(rowObjectId))
                        {
                            continue;
                        }
                    }

                    // if the record has ended in the past, we ignore it.
                    // we dont worry about catching overlapping records here, although perhaps we should
                    if (end != null && end.getTime() < now.getTime())
                    {
                        continue;
                    }

                    if (start.getTime() >= date.getTime())
                    {
                        if (highestOpenEnded == null || date.getTime() > highestOpenEnded.getTime())
                            highestOpenEnded = date;
                    }
                }
                catch (ConversionException e)
                {
                    _log.error("TriggerScriptHelper.getOpenEndedHousingOverlaps was unable to parse date", e);
                    continue;
                }
            }
        }

        if (rowObjectId != null)
        {
            ignoredObjectIds.add(rowObjectId);
        }

        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), id);
        filter.addCondition(FieldKey.fromString("date"), date, CompareType.GTE);
        filter.addCondition(FieldKey.fromString("isActive"), true, CompareType.EQUAL);

        if (!ignoredObjectIds.isEmpty())
            filter.addCondition(FieldKey.fromString("objectid"), ignoredObjectIds, CompareType.NOT_IN);

        filter.addCondition(FieldKey.fromString("qcstate/publicData"), true, CompareType.EQUAL);

        TableInfo ti = getTableInfo("study", "Housing");
        TableSelector ts = new TableSelector(ti, Collections.singleton("Id"), filter, null);
        Map<String, List<Aggregate.Result>> aggs = ts.getAggregates(Arrays.asList(new Aggregate(FieldKey.fromString("date"), Aggregate.Type.MAX)));
        if (aggs.containsKey("date"))
        {
            for (Aggregate.Result r : aggs.get("date"))
            {
                Date d = (Date)r.getValue();
                if (d != null)
                {
                    if (highestOpenEnded == null || d.getTime() > highestOpenEnded.getTime())
                    {
                        highestOpenEnded = d;
                    }
                }
            }
        }

        return highestOpenEnded;
    }

    public String validateFutureOpenEndedHousing(String id, Date date, String objectid, List<Map<String, Object>> recordsInTransaction)
    {
        if (id == null || date == null)
            return null;


        Date highestOverlap = getOpenEndedHousingOverlaps(id, date, recordsInTransaction, objectid);
        if (highestOverlap != null)
            return "You cannot enter an open ended housing while there is another active record starting after this record's start date (" + _dateTimeFormat.format(highestOverlap) + ")";

        return null;
    }

    private void sendMessage(String subject, String bodyHtml, Collection<UserPrincipal> recipients)
    {
        try
        {
            MailHelper.MultipartMessage msg = MailHelper.createMultipartMessage();
            msg.setFrom(NotificationService.get().getReturnEmail(getContainer()));
            msg.setSubject(subject);

            List<String> emails = new ArrayList<>();
            for (UserPrincipal u : recipients)
            {
                List<Address> addresses = NotificationService.get().getEmailsForPrincipal(u);
                if (addresses != null)
                {
                    for (Address a : addresses)
                    {
                        if (a.toString() != null)
                            emails.add(a.toString());
                    }
                }
            }

            if (emails.size() == 0)
            {
                _log.warn("No emails, unable to send EHR trigger script email");
                return;
            }

            msg.setRecipients(Message.RecipientType.TO, StringUtils.join(emails, ","));
            msg.setBodyContent(bodyHtml, "text/html");

            MailHelper.send(msg, getUser(), getContainer());
        }
        catch (Exception e)
        {
            _log.error("Unable to send email from EHR trigger script", e);
        }
    }

    public void sendDeathNotification(final List<String> ids)
    {
        if (!NotificationService.get().isServiceEnabled())
        {
            _log.info("notification service is not enabled, will not send death notification.");
            return;
        }

        JobRunner.getDefault().execute(new Runnable(){
            public void run()
            {
                final User user = getUser();
                final Container container = getContainer();
                for (String id : ids)
                {
                    String subject = "Death notification: " + id;

                    Set<UserPrincipal> recipients = NotificationService.get().getRecipients(new DeathNotification(), getContainer());
                    if (recipients.size() == 0)
                    {
                        _log.warn("No recipients, skipping death notification");
                        return;
                    }

                    final StringBuilder html = new StringBuilder();

                    html.append("Animal " + id + " has been marked as dead.  ");
                    html.append("<a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/ehr" + getContainer().getPath() + "/participantView.view?participantId=" + id + "'>");

                    html.append("Click here to view this animal's clinical history</a>.  <p>");

                    AnimalRecord ar = EHRDemographicsServiceImpl.get().getAnimal(container, id);
                    html.append("Species: ").append(ar.getSpecies()).append("<br>");
                    html.append("Gender: ").append(ar.getGenderMeaning()).append("<br>");
                    html.append("Age: ").append(ar.getAgeInYearsAndDays()).append("<br>");

                    //find housing overlapping date of death
                    TableInfo housing = getTableInfo("study", "demographicsLastHousing");
                    TableSelector housingTs = new TableSelector(housing, PageFlowUtil.set("room", "cage"), new SimpleFilter(FieldKey.fromString("Id"), id), null);
                    housingTs.forEach(new Selector.ForEachBlock<ResultSet>()
                    {
                        @Override
                        public void exec(ResultSet rs) throws SQLException
                        {
                            if (rs.getString("room") != null)
                            {
                                html.append("Location: ").append(rs.getString("room"));

                                if (rs.getString("cage") != null)
                                {
                                    html.append(", ").append(rs.getString("cage"));
                                }

                                html.append("<br>");
                            }
                        }
                    });

                    //find assignments overlapping date of death
                    TableInfo assignment = getTableInfo("study", "assignment");
                    final Map<FieldKey, ColumnInfo> assignmentCols = QueryService.get().getColumns(assignment, PageFlowUtil.set(FieldKey.fromString("project/displayName"), FieldKey.fromString("project/investigatorId/lastName")));
                    SimpleFilter assignmentFilter = new SimpleFilter(FieldKey.fromString("Id"), id);
                    assignmentFilter.addCondition(FieldKey.fromString("enddateCoalesced"), new Date(), CompareType.DATE_GTE);
                    TableSelector assignmentTs = new TableSelector(assignment, assignmentCols.values(), assignmentFilter, null);
                    html.append("<br>Assignments: ").append("<br>");

                    if (assignmentTs.exists())
                    {
                        assignmentTs.forEach(new Selector.ForEachBlock<ResultSet>()
                        {
                            @Override
                            public void exec(ResultSet object) throws SQLException
                            {
                                Results rs = new ResultsImpl(object, assignmentCols);
                                if (rs.getString(FieldKey.fromString("project/displayName")) != null)
                                {
                                    html.append(rs.getString(FieldKey.fromString("project/displayName")));

                                    if (rs.getString(FieldKey.fromString("project/investigatorId/lastName")) != null)
                                    {
                                        html.append(" (").append(rs.getString(FieldKey.fromString("project/investigatorId/lastName"))).append(")");
                                    }

                                    html.append("<br>");
                                }
                            }
                        });
                    }
                    else
                    {
                        html.append("No assignments").append("<br>");
                    }

                    //find groups overlapping date of death
                    TableInfo groups = getTableInfo("study", "animal_group_members", true);
                    if (groups != null)
                    {
                        final Map<FieldKey, ColumnInfo> groupCols = QueryService.get().getColumns(groups, PageFlowUtil.set(FieldKey.fromString("groupId/name")));
                        SimpleFilter groupFilter = new SimpleFilter(FieldKey.fromString("Id"), id);
                        groupFilter.addCondition(FieldKey.fromString("enddateCoalesced"), new Date(), CompareType.DATE_GTE);
                        TableSelector groupTs = new TableSelector(groups, groupCols.values(), groupFilter, null);
                        html.append("<br>Groups: ").append("<br>");

                        if (groupTs.exists())
                        {
                            groupTs.forEach(new Selector.ForEachBlock<ResultSet>()
                            {
                                @Override
                                public void exec(ResultSet object) throws SQLException
                                {
                                    Results rs = new ResultsImpl(object, groupCols);
                                    if (rs.getString(FieldKey.fromString("groupId/name")) != null)
                                    {
                                        html.append(rs.getString(FieldKey.fromString("groupId/name"))).append("<br>");
                                    }
                                }
                            });
                        }
                        else
                        {
                            html.append("No groups").append("<br>");
                        }
                    }

                    sendMessage(subject, html.toString(), recipients);
                }
            }
        });
    }

    public String verifyProtocolCounts(final String id, Integer project, final List<Map<String, Object>> recordsInTransaction)
    {
        if (id == null)
        {
            return null;
        }

        AnimalRecord ar = EHRDemographicsServiceImpl.get().getAnimal(getContainer(), id);
        if (ar.getSpecies() == null)
        {
            return "Unknown species: " + id;
        }

        final String protocol = getProtocolForProject(project);
        if (protocol == null)
        {
            return "Unable to find protocol associated with project: " + project;
        }

        //find the total animals previously used by this protocols/species
        TableInfo ti = QueryService.get().getUserSchema(getUser(), getContainer(), "ehr").getTable("protocolTotalAnimalsBySpecies");
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("species"), PageFlowUtil.set(ar.getSpecies(), "All Species"), CompareType.IN);
        filter.addCondition(FieldKey.fromString("protocol"), protocol);
        TableSelector ts = new TableSelector(ti, filter, null);
        final List<String> errors = new ArrayList<>();
        final String ALL_SPECIES = "All Species";
        ts.forEach(new Selector.ForEachBlock<ResultSet>()
        {
            @Override
            public void exec(ResultSet rs) throws SQLException
            {
                Integer totalAllowed = rs.getInt("TotalAnimals");
                String species = rs.getString("Species");
                Set<String> animals = new CaseInsensitiveHashSet();
                String animalString = rs.getString("Animals");
                if (animalString != null)
                {
                    animals.addAll(Arrays.asList(StringUtils.split(animalString, ",")));
                }

                animals.add(id);

                if (recordsInTransaction != null && recordsInTransaction.size() > 0)
                {
                    for (Map<String, Object> r : recordsInTransaction)
                    {
                        String id = (String)r.get("Id");
                        Number project = (Number)r.get("project");
                        if (id == null || project == null)
                        {
                            continue;
                        }

                        String rowProtocol = getProtocolForProject(project.intValue());
                        if (rowProtocol == null || !rowProtocol.equals(protocol))
                        {
                            continue;
                        }

                        if (!ALL_SPECIES.equals(species))
                        {
                            //find species
                            AnimalRecord ar = getDemographicRecord(id);
                            if (ar.getSpecies() == null || !species.equals(ar.getSpecies()))
                            {
                                continue;
                            }
                        }

                        animals.add(id);
                    }
                }

                Integer remaining = totalAllowed - animals.size();
                if (remaining < 0)
                {
                    errors.add("There are not enough spaces on protocol: " + protocol + ". Allowed: " + totalAllowed + ", used: " + animals.size());
                }
            }
        });

        return StringUtils.join(errors, "<>");
    }

    public void createRequestsForBloodAdditionalServices(String id, Date date, Integer project, String performedby, String services) throws Exception
    {
        try
        {
            if (StringUtils.isEmpty(id) || project == null || StringUtils.isEmpty(services))
                return;

            List<Map<String, Object>> toAutomaticallyCreate = getAdditionalServicesToCreate(services);
            if (toAutomaticallyCreate == null || toAutomaticallyCreate.isEmpty())
                return;

            //test permission first
            if (!EHRService.get().hasPermission("study", "clinpathRuns", getContainer(), getUser(), InsertPermission.class, EHRService.QCSTATES.RequestPending.getQCState(getContainer())))
            {
                _log.warn("User does not have permission to insert requests into Clinpath Runs follow blood draw: " + getUser().getEmail());
                return;
            }

            for (Map<String, Object> rowMap : toAutomaticallyCreate)
            {
                rowMap = new CaseInsensitiveHashMap<>(rowMap);
                GUID requestId = new GUID();
                //NOTE: we want the requested labwork to match the date of the blood draw
                Date dateRequested = date;
                Map<String, Object> row = new CaseInsensitiveHashMap<>();
                row.put("daterequested", dateRequested);
                row.put("requestid", requestId.toString());
                row.put("priority", "Routine");
                row.put("formtype", rowMap.get("formtype"));
                row.put("title", "Labwork Request From Blood Draw: " + rowMap.get("labwork_service"));
                row.put("notify1", getUser().getUserId());

                if (row.get("formtype") == null)
                {
                    _log.error("Unable to determine formtype for automatic lab request for service: " + row.get("service"));
                    continue;
                }

                if (rowMap.get("labwork_service") == null)
                {
                    _log.error("Unable to determine formtype for automatic lab request for service: " + row.get("service"));
                    continue;
                }

                TableInfo requests = getTableInfo("ehr", "requests");
                List<Map<String, Object>> rows = new ArrayList<>();
                rows.add(row);
                Map<String, Object> extraContext = getExtraContext();
                extraContext.put("skipRequestInPastCheck", true);
                BatchValidationException errors = new BatchValidationException();
                requests.getUpdateService().insertRows(getUser(), getContainer(), rows, errors, extraContext);
                if (errors.hasErrors())
                    throw errors;

                TableInfo clinpathRuns = getTableInfo("study", "Clinpath Runs");
                List<Map<String, Object>> clinpathRows = new ArrayList<>();
                Map<String, Object> clinpathRow = new CaseInsensitiveHashMap<>();
                clinpathRow.put("Id", id);
                clinpathRow.put("date", dateRequested);
                clinpathRow.put("project", project);
                clinpathRow.put("requestId", requestId);
                clinpathRow.put("tissue", getTissueForService((String)rowMap.get("service")));
                clinpathRow.put("collectedBy", performedby);
                clinpathRow.put("servicerequested", rowMap.get("service"));
                clinpathRow.put("QCStateLabel", "Request: Pending");

                clinpathRows.add(clinpathRow);
                clinpathRuns.getUpdateService().insertRows(getUser(), getContainer(), clinpathRows, errors, getExtraContext());
                if (errors.hasErrors())
                    throw errors;
            }
        }
        catch (Exception e)
        {
            //Unsure why these are getting swallowed and not logged?
            _log.error(e.getMessage(), e);
            throw e;
        }
    }

    private String getTissueForService(String service)
    {
        Map<String, Map<String, Object>> serviceMap = getLabworkServices();
        if (serviceMap != null && serviceMap.containsKey(service))
        {
            Map<String, Object> row = serviceMap.get(service);
            return (String)row.get("tissue");
        }

        return null;
    }

    private List<Map<String, Object>> getAdditionalServicesToCreate(String services)
    {
        services = StringUtils.trimToNull(services);
        if (services == null)
            return null;

        List<String> testNames = Arrays.asList(StringUtils.split(services, ","));
        if (testNames.size() == 0)
            return null;

        Map<String, Map<String, Object>> serviceMap = getBloodDrawServicesMap();
        List<Map<String, Object>> toCreate = new ArrayList<>();
        for (String service : testNames)
        {
            if (serviceMap.containsKey(service))
            {
                Boolean shouldCreate = (Boolean)serviceMap.get(service).get("automaticrequestfromblooddraw");
                if (shouldCreate)
                {
                    Map<String, Object> row = new HashMap<>();
                    row.put("sevice", service);
                    row.put("formtype", serviceMap.get(service).get("formtype"));
                    row.put("labwork_service", serviceMap.get(service).get("labwork_service"));
                    toCreate.add(row);
                }
            }
        }

        return toCreate;
    }

    public void deleteSnomedTags(String objectid) throws Exception
    {
        objectid = StringUtils.trimToNull(objectid);
        if (objectid == null)
        {
            return;
        }

        TableInfo snomedTags = DbSchema.get(EHRSchema.EHR_SCHEMANAME).getTable(EHRSchema.TABLE_SNOMED_TAGS);
        int deleted = Table.delete(snomedTags, new SimpleFilter(FieldKey.fromString("recordid"), objectid, CompareType.EQUAL));
        _log.info("deleted " + deleted + "snomed tags for record: " + objectid);
    }

    public void updateSNOMEDTags(String id, String objectid, String codes) throws Exception
    {
        codes = StringUtils.trimToNull(codes);
        objectid = StringUtils.trimToNull(objectid);

        if (objectid == null)
        {
            return;
        }

        _log.info("deleting SNOMED tags for: " + objectid);
        TableInfo snomedTags = DbSchema.get(EHRSchema.EHR_SCHEMANAME).getTable(EHRSchema.TABLE_SNOMED_TAGS);
        Table.delete(snomedTags, new SimpleFilter(FieldKey.fromString("recordid"), objectid, CompareType.EQUAL));
        if (codes != null)
        {
            String[] codeList = StringUtils.split(codes, ";");
            int sort = 0;

            _log.info("adding " + codeList.length + " SNOMED tags for: " + objectid);
            for (String code : codeList)
            {
                sort++;
                String[] tokens = code.split("<>");
                if (tokens.length != 2)
                {
                    _log.error("Improper SNOMED code string: " + codes);
                    continue;
                }

                Map<String, Object> toInsert = new CaseInsensitiveHashMap<>();
                toInsert.put("id", id);
                toInsert.put("recordid", objectid);
                toInsert.put("objectid", new GUID());
                toInsert.put("code", tokens[1]);
                toInsert.put("sort", sort);

                toInsert.put("container", getContainer().getId());
                toInsert.put("created", new Date());
                toInsert.put("createdby", getUser().getUserId());
                toInsert.put("modified", new Date());
                toInsert.put("modifiedby", getUser().getUserId());

                Table.insert(getUser(), snomedTags, toInsert);
            }
        }
    }

    public boolean isVet()
    {
        String cacheKey = this.getClass().getName() + "||" + getContainer().getId() + "||" + "vets";
        Set<Integer> ret = (Set)DataEntryManager.get().getCache().get(cacheKey);
        if (ret == null)
        {
            _log.info("caching vets in TriggerScriptHelper");
            TableInfo ti = getTableInfo("ehr_lookups", "veterinarians");
            TableSelector ts = new TableSelector(ti);
            ret = new HashSet<>();
            for (Map<String, Object> row : ts.getMapArray())
            {
                ret.add((Integer)row.get("UserId"));
            }

            DataEntryManager.get().getCache().put(cacheKey, ret);
        }

        return ret.contains(getUser().getUserId());
    }

    public void primeCache()
    {
        isVet();
        getBloodDrawServicesMap();
        getDefaultProjects();
        getBloodDrawServicesMap();
        getWeightRangeForSpecies("Cyno");
        getLabworkServices();
        getBloodForSpecies("Rhesus");
        cacheAllProtocols();
    }

    public String getEHRStudyContainerPath()
    {
        Container ret = EHRService.get().getEHRStudyContainer(_container);

        return ret == null ? null : ret.getPath();
    }

    public void closeHousingRecords(List<Map<String, Object>> records) throws Exception
    {
        TableInfo housing = getTableInfo("study", "housing");
        List<Map<String, Object>> toUpdate = new ArrayList<>();
        List<Map<String, Object>> oldKeys = new ArrayList<>();

        for (Map<String, Object> row : records)
        {
            SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id"), row.get("Id"));
            filter.addCondition(FieldKey.fromString("enddate"), null, CompareType.ISBLANK);
            filter.addCondition(FieldKey.fromString("objectid"), row.get("objectid"), CompareType.NEQ_OR_NULL);

            TableSelector ts = new TableSelector(housing, Collections.singleton("lsid"), filter, null);
            List<String> ret = ts.getArrayList(String.class);
            if (ret.size() > 0)
            {
                for (String lsid : ret)
                {
                    Map<String, Object> r = new CaseInsensitiveHashMap<>();
                    r.put("lsid", lsid);
                    Date date = (Date)row.get("date");
                    if (date.getHours() == 0 && date.getMinutes() == 0)
                    {
                        Exception e = new Exception();
                        _log.error("Attempting to terminate housing records with a rounded date.  This might indicate upstream code is rounding the date", e);
                    }
                    r.put("enddate", date);
                    toUpdate.add(r);

                    Map<String, Object> keyMap = new CaseInsensitiveHashMap<>();
                    keyMap.put("lsid", lsid);
                    oldKeys.add(keyMap);
                }
            }
        }

        if (!toUpdate.isEmpty())
        {
            _log.info("closing housing records: " + toUpdate.size());
            Map<String, Object> context = getExtraContext();
            context.put("skipAnnounceChangedParticipants", true);
            housing.getUpdateService().updateRows(getUser(), getContainer(), toUpdate, oldKeys, context);
        }
    }

    public void ensureSingleFlagCategoryActive(String id, String flag, String objectId, final Date enddate)
    {
        //first resolve flag
        TableInfo flagValuesTable = getTableInfo("ehr_lookups", "flag_values");
        TableSelector ts1 =  new TableSelector(flagValuesTable, Collections.singleton("category"), new SimpleFilter(FieldKey.fromString("objectid"), flag), null);
        String category = ts1.getObject(String.class);
        if (category == null)
        {
            return;
        }

        TableInfo flagCategoriesTable = getTableInfo("ehr_lookups", "flag_categories");
        TableSelector ts2 =  new TableSelector(flagCategoriesTable, Collections.singleton("enforceUnique"), new SimpleFilter(FieldKey.fromString("category"), category), null);
        List<Boolean> ret = ts2.getArrayList(Boolean.class);
        boolean enforceUnique = ret != null && ret.size() == 1 ? ret.get(0) : false;

        if (enforceUnique)
        {
            //find existing active flags of the same category
            SimpleFilter filter = new SimpleFilter(FieldKey.fromString("flag/category"), category);
            filter.addCondition(FieldKey.fromString("isActive"), true);
            filter.addCondition(FieldKey.fromString("Id"), id, CompareType.EQUAL);
            filter.addCondition(FieldKey.fromString("objectid"), objectId, CompareType.NEQ_OR_NULL);

            TableInfo flagsTable = getTableInfo("study", "Animal Record Flags");
            final List<Map<String, Object>> rows = new ArrayList<>();
            final List<Map<String, Object>> oldKeys = new ArrayList<>();
            QueryUpdateService qus = flagsTable.getUpdateService();

            TableSelector ts = new TableSelector(flagsTable, PageFlowUtil.set("lsid", "Id", "enddate"), filter, null);
            ts.forEach(new Selector.ForEachBlock<ResultSet>()
            {
                @Override
                public void exec(ResultSet rs) throws SQLException
                {
                    Map<String, Object> row = new CaseInsensitiveHashMap<>();
                    row.put("enddate", enddate);
                    rows.add(row);

                    Map<String, Object> keys = new CaseInsensitiveHashMap<>();
                    keys.put("lsid", rs.getString("lsid"));
                    oldKeys.add(keys);
                }
            });

            try
            {
                if (rows.size() > 0)
                {
                    Map<String, Object> extraContext = getExtraContext();
                    extraContext.put("skipAnnounceChangedParticipants", true);
                    qus.updateRows(getUser(), flagsTable.getUserSchema().getContainer(), rows, oldKeys, extraContext);
                }
            }
            catch (InvalidKeyException e)
            {
                throw new RuntimeException(e);
            }
            catch (BatchValidationException e)
            {
                throw new RuntimeException(e);
            }
            catch (QueryUpdateServiceException e)
            {
                throw new RuntimeException(e);
            }
            catch (SQLException e)
            {
                throw new RuntimeSQLException(e);
            }
        }
    }

    private Integer _nextFlagCode = null;

    public Integer getNextFlagCode()
    {
        if (_nextFlagCode == null)
        {
            SqlSelector ss = new SqlSelector(DbSchema.get("ehr"), "SELECT COALESCE(max(code), 0) as expr FROM ehr_lookups.flag_values");
            List<Integer> ret = ss.getArrayList(Integer.class);
            _nextFlagCode = ret.isEmpty() ? 0 : ret.get(0);
        }

        _nextFlagCode++;

        return _nextFlagCode;
    }

    public void reportCageChange(List<String> keys)
    {
        if (keys == null || keys.size() == 0)
            return;

        SimpleFilter.OrClause clause = new SimpleFilter.OrClause();
        for (String key : keys)
        {
            String[] tokens = key.split("<>");
            clause.addClause(new SimpleFilter.AndClause(new CompareType.EqualsCompareClause(FieldKey.fromString("room"), CompareType.EQUAL, tokens[0]), new CompareType.EqualsCompareClause(FieldKey.fromString("cage"), CompareType.EQUAL, tokens[1])));
        }

        TableInfo ti = getTableInfo("study", "housing");
        if (ti != null)
        {
            SimpleFilter filter = new SimpleFilter(FieldKey.fromString("isActive"), true);
            filter.addClause(clause);

            TableSelector ts = new TableSelector(ti, PageFlowUtil.set("Id"), filter, null);
            List<String> ids = ts.getArrayList(String.class);
            if (ids != null && !ids.isEmpty())
            {
                EHRDemographicsServiceImpl.get().reportDataChange(getContainer(), "study", "housing", new ArrayList<>(new HashSet<>(ids)));
            }
        }
    }

    /**
     * This is separate from DemographicsService for speed.  The current use is on birth insert, and sometimes the dam isnt cached.
     */
    public String getGeographicOrigin(String id)
    {
        TableInfo ti = getTableInfo("study", "demographics");
        TableSelector ts = new TableSelector(ti, PageFlowUtil.set("geographic_origin"), new SimpleFilter(FieldKey.fromString("Id"), id), null);

        return ts.getObject(String.class);
    }

    /**
     * This is separate from DemographicsService for speed.  The current use is on birth insert, and sometimes the dam isnt cached.
     */
    public String getSpecies(String id)
    {
        TableInfo ti = getTableInfo("study", "demographics");
        TableSelector ts = new TableSelector(ti, PageFlowUtil.set("species"), new SimpleFilter(FieldKey.fromString("Id"), id), null);

        return ts.getObject(String.class);
    }
}
