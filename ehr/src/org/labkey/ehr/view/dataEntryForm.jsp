<%
    /*
    * Copyright (c) 2013 LabKey Corporation
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
%>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.labkey.api.data.Container" %>
<%@ page import="org.labkey.api.data.ContainerManager" %>
<%@ page import="org.labkey.api.security.permissions.ReadPermission" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.api.view.Portal" %>
<%@ page import="org.labkey.api.view.ViewContext" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="org.labkey.api.ehr.dataentry.DataEntryForm" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!

    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<ClientDependency>();
        resources.add(ClientDependency.fromModuleName("ehr"));
        resources.add(ClientDependency.fromFilePath("ehr/panel/DataEntryPanel.js"));
        resources.add(ClientDependency.fromFilePath("ehr/panel/TaskDataEntryPanel.js"));
        return resources;
    }
%>
<%
    ViewContext ctx = getViewContext();
    DataEntryForm def = (DataEntryForm)getModelBean();
    String formClass = def.getJavascriptClass();
    JSONObject json = def.toJSON(ctx.getContainer(), ctx.getUser());

    String renderTarget = "ehrDiv-" + def.getName();
%>
<div id='<%=text(renderTarget)%>'></div>

<script type="text/javascript">

    Ext4.onReady(function(){
        Ext4.create(<%=text(formClass)%>, {
            formConfig: <%=text(json.toString())%>
        }).render('<%=text(renderTarget)%>');
    });

</script>
