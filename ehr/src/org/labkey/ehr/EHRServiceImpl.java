/*
 * Copyright (c) 2012-2013 LabKey Corporation
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
package org.labkey.ehr;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.collections.CaseInsensitiveHashMap;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.PropertyManager;
import org.labkey.api.data.TableCustomizer;
import org.labkey.api.ehr.EHRService;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.query.DetailsURL;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.User;
import org.labkey.api.util.Pair;
import org.labkey.api.view.template.ClientDependency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: bimber
 * Date: 9/14/12
 * Time: 4:46 PM
 */
public class EHRServiceImpl extends EHRService
{
    private Set<Module> _registeredModules = new HashSet<Module>();
    private Map<REPORT_LINK_TYPE, List<ReportLink>> _reportLinks = new HashMap<REPORT_LINK_TYPE, List<ReportLink>>();
    private List<Pair<Module, Resource>> _extraTriggerScripts = new ArrayList<Pair<Module, Resource>>();
    private Map<Module, List<ClientDependency>> _clientDependencies = new HashMap<Module, List<ClientDependency>>();
    private Map<String, Map<String, List<Pair<Module, Class<? extends TableCustomizer>>>>> _tableCustomizers = new CaseInsensitiveHashMap<Map<String, List<Pair<Module, Class<? extends TableCustomizer>>>>>();
    private Map<String, String> _dateFormats = new HashMap<String, String>();
    private static final Logger _log = Logger.getLogger(EHRServiceImpl.class);

    private static final String ALL_TABLES = "~~ALL_TABLES~~";
    private static final String ALL_SCHEMAS = "~~ALL_SCHEMAS~~";
    private static final String DATE_CATEGORY = "org.labkey.ehr.dateformat";

    public EHRServiceImpl()
    {

    }

    public void registerModule(Module module)
    {
        _registeredModules.add(module);
    }

    public Set<Module> getRegisteredModules()
    {
        return _registeredModules;
    }

    public void registerTriggerScript(Module owner, Resource script)
    {
        _extraTriggerScripts.add(Pair.of(owner, script));
    }


    public List<Resource> getExtraTriggerScripts(Container c)
    {
        List<Resource> resouces = new ArrayList<Resource>();
        Set<Module> activeModules = c.getActiveModules();

        for (Pair<Module, Resource> pair : _extraTriggerScripts)
        {
            if (activeModules.contains(pair.first))
            {
                resouces.add(pair.second);
            }
        }
        return Collections.unmodifiableList(resouces);
    }

    public void registerTableCustomizer(Module owner, Class<? extends TableCustomizer> customizerClass)
    {
        registerTableCustomizer(owner, customizerClass, ALL_SCHEMAS, ALL_TABLES);
    }

    public void registerTableCustomizer(Module owner, Class<? extends TableCustomizer> customizerClass, String schema, String query)
    {
        Map<String, List<Pair<Module, Class<? extends TableCustomizer>>>> map = _tableCustomizers.get(schema);
        if (map == null)
            map = new CaseInsensitiveHashMap<List<Pair<Module, Class<? extends TableCustomizer>>>>();

        List<Pair<Module, Class<? extends TableCustomizer>>> list = map.get(query);
        if (list == null)
            list = new ArrayList<Pair<Module, Class<? extends TableCustomizer>>>();

        list.add(Pair.<Module, Class<? extends TableCustomizer>>of(owner, customizerClass));

        map.put(query, list);
        _tableCustomizers.put(schema, map);
    }

    public List<TableCustomizer> getCustomizers(Container c, String schema, String query)
    {
        List<TableCustomizer> list = new ArrayList<TableCustomizer>();
        Set<Module> modules = c.getActiveModules();

        if (_tableCustomizers.get(ALL_SCHEMAS) != null)
        {
            for (Pair<Module, Class<? extends TableCustomizer>> pair : _tableCustomizers.get(ALL_SCHEMAS).get(ALL_TABLES))
            {
                if (modules.contains(pair.first))
                {
                    TableCustomizer tc = instantiateCustomizer(pair.second);
                    if (tc != null)
                        list.add(tc);
                }
            }
        }

        if (_tableCustomizers.containsKey(schema))
        {
            if (_tableCustomizers.get(schema).get(ALL_TABLES).contains(ALL_TABLES))
            {
                for (Pair<Module, Class<? extends TableCustomizer>> pair : _tableCustomizers.get(schema).get(ALL_TABLES))
                {
                    if (modules.contains(pair.first))
                    {
                        TableCustomizer tc = instantiateCustomizer(pair.second);
                        if (tc != null)
                            list.add(tc);
                    }
                }
            }

            if (_tableCustomizers.get(schema).get(ALL_TABLES).contains(query))
            {
                for (Pair<Module, Class<? extends TableCustomizer>> pair : _tableCustomizers.get(schema).get(query))
                {
                    if (modules.contains(pair.first))
                    {
                        TableCustomizer tc = instantiateCustomizer(pair.second);
                        if (tc != null)
                            list.add(tc);
                    }
                }
            }
        }

        return Collections.unmodifiableList(list);
    }

    private TableCustomizer instantiateCustomizer(Class<? extends TableCustomizer> customizerClass)
    {
        try
        {
            return customizerClass.newInstance();
        }
        catch (InstantiationException e)
        {
            _log.error("Unable to create instance of class '" + customizerClass.getName() + "'", e);
        }
        catch (IllegalAccessException e)
        {
            _log.error("Unable to create instance of class '" + customizerClass.getName() + "'", e);
        }

        return null;
    }

    public void registerClientDependency(ClientDependency cd, Module owner)
    {
        List<ClientDependency> list = _clientDependencies.get(owner);
        if (list == null)
            list = new ArrayList<ClientDependency>();

        list.add(cd);

        _clientDependencies.put(owner, list);
    }

    public Set<ClientDependency> getRegisteredClientDependencies(Container c, User u)
    {
        Set<ClientDependency> set = new HashSet<ClientDependency>();
        for (Module m : _clientDependencies.keySet())
        {
            if (c.getActiveModules().contains(m))
            {
                set.addAll(_clientDependencies.get(m));
            }
        }

        return Collections.unmodifiableSet(set);
    }

    public void setDateFormat(Container c, String format)
    {
        PropertyManager.PropertyMap props = PropertyManager.getWritableProperties(c, DATE_CATEGORY, true);
        props.put("dateFormat", format);
        PropertyManager.saveProperties(props);
        _dateFormats.put(c.getId(), format);
    }

    public String getDateFormat(Container c)
    {
        if (_dateFormats.containsKey(c.getId()))
            return _dateFormats.get(c.getId());

        Map<String, String> props = PropertyManager.getProperties(c, DATE_CATEGORY);
        if (props.containsKey("dateFormat"))
            return props.get("dateFormat");

        return "yyyy-MM-dd HH:mm";
    }

    public User getEHRUser(Container c)
    {
        return EHRManager.get().getEHRUser(c);
    }

    public void registerReportLink(REPORT_LINK_TYPE type, String label, Module owner, DetailsURL url, @Nullable String category)
    {
        List<ReportLink> links = _reportLinks.get(type);

        if (links == null)
            links = new ArrayList<ReportLink>();

        links.add(new ReportLink(label, owner, url, category));

        _reportLinks.put(type, links);
    }

    public List<ReportLink> getReportLinks(Container c, User u, REPORT_LINK_TYPE type)
    {
        List<ReportLink> links = _reportLinks.get(type);
        if (links == null)
            return Collections.emptyList();

        List<ReportLink> ret = new ArrayList<ReportLink>();
        for (ReportLink l : links)
        {
            if (l.isAvailable(c, u))
                ret.add(l);
        }

        return Collections.unmodifiableList(ret);
    }

    public class ReportLink
    {
        private DetailsURL _url;
        private String _label;
        private Module _owner;
        private String _category;

        public ReportLink(String label, Module owner, DetailsURL url, @Nullable String category)
        {
            _url = url;
            _label = label;
            _owner = owner;
            _category = category;
        }

        public boolean isAvailable(Container c, User u)
        {
            return c.getActiveModules().contains(_owner);
        }

        public DetailsURL getUrl()
        {
            return _url;
        }

        public String getLabel()
        {
            return _label;
        }

        public String getCategory()
        {
            return _category;
        }
    }
}