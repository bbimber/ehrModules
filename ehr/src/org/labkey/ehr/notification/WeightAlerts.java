package org.labkey.ehr.notification;

import org.labkey.api.data.CompareType;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.TableSelector;
import org.labkey.api.query.FieldKey;
import org.labkey.api.settings.AppProps;
import org.labkey.api.util.PageFlowUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: bbimber
 * Date: 7/23/12
 * Time: 7:41 PM
 */
public class WeightAlerts extends AbstractNotification
{
    public String getName()
    {
        return "Weight Alerts";
    }

    public String getDescription()
    {
        return "This will send an email to alert of any animals with a weight change of +/- 10% in the past 30 days";
    }

    public String getEmailSubject()
    {
        return "Weight Alerts: " + _dateTimeFormat.format(new Date());
    }

    public Set<String> getNotificationTypes()
    {
        return Collections.singleton("Weight Drops"); //TODO: change to match name
    }

    public List<ScheduledFuture> schedule(int delay)
    {
        List<ScheduledFuture> tasks = new ArrayList<ScheduledFuture>();
        tasks.add(NotificationService.get().getExecutor().scheduleWithFixedDelay(this, delay, 60, TimeUnit.MINUTES));
        return tasks;
    }

    public String getScheduleDescription()
    {
        return "every 60 minutes";
    }

    public String getMessage()
    {
        StringBuilder msg = new StringBuilder();

        //Find today's date
        Date now = new Date();
        msg.append("This email contains alerts of weight changes of +/- 10% or greater.  It was run on: " + _dateFormat.format(now) + " at " + _timeFormat.format(now) + ".<p>");

        getLivingWithoutWeight(msg);
        processWeights(msg, 0, 30, CompareType.LTE, -10);
        processWeights(msg, 0, 30, CompareType.GTE, 10);

        return msg.toString();
    }

    private void getLivingWithoutWeight(final StringBuilder msg)
    {
        SimpleFilter filter = new SimpleFilter(FieldKey.fromString("calculated_status"), "Alive");
        filter.addCondition(FieldKey.fromString("Id/MostRecentWeight/MostRecentWeightDate"), null, CompareType.ISBLANK);
        TableSelector ts = new TableSelector(_studySchema.getTable("demographics"), Collections.singleton(_ehrStudy.getSubjectColumnName()), filter, null);

        if (ts.getRowCount() > 0)
        {
	        msg.append("<b>WARNING: There are " + ts.getRowCount() + " living animals without a weight:</b><p>\n");

            ts.forEach(new TableSelector.ForEachBlock<ResultSet>(){
                public void exec(ResultSet rs) throws SQLException
                {
                    msg.append(rs.getString(_ehrStudy.getSubjectColumnName()) + "<br>\n");
                }
            });

            msg.append("<p><a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/query" + _ehrContainer.getPath() + "/executeQuery.view?schemaName=study&query.queryName=Demographics&query.calculated_status~eq=Alive&query.Id/MostRecentWeight/MostRecentWeightDate~isblank\".\"'>Click here to view these animals</a></p>\n");
            msg.append("When you see these problems, it usually happens because the automatic process of calculating this field, which is triggered by births, deaths, departures or arrivals, didnt work right.  To force it to re-calculate, just edit the animal's record on one of these tables, maybe no changes, then hit submit.  That should force a re-calculation of the status field.<p>");
            msg.append("<hr>\n");
        }
    }

    private void processWeights(final StringBuilder msg, int min, int max, CompareType ct, double pct)
    {
        try
        {
            SimpleFilter filter = new SimpleFilter(FieldKey.fromString("Id/DataSet/Demographics/calculated_status"), "Alive");
            filter.addCondition(FieldKey.fromString("PctChange"), pct, ct);
            filter.addCondition(FieldKey.fromString("IntervalInDays"), min, CompareType.GTE);
            filter.addCondition(FieldKey.fromString("IntervalInDays"), max, CompareType.LTE);

            Calendar date = Calendar.getInstance();
            date.add(Calendar.DATE, -3);
            filter.addCondition(FieldKey.fromString("LatestWeightDate"), _dateFormat.format(date.getTime()), CompareType.DATE_GTE);
            Sort sort = new Sort();
            sort.insertSortColumn(FieldKey.fromString("Id/curLocation/area,Id/curLocation/room,Id/curLocation/cage,Id"), Sort.SortDirection.ASC);
            Set<String> columns = PageFlowUtil.set(_ehrStudy.getSubjectColumnName());
            columns.add("Id/curLocation/area");
            columns.add("Id/curLocation/room");
            columns.add("Id/curLocation/cage");
            columns.add("LatestWeightDate");
            columns.add("LatestWeight");
            columns.add("date");
            columns.add("weight");
            columns.add("PctChange");
            columns.add("IntervalInDays");
            TableSelector ts = new TableSelector(_studySchema.getTable("weightRelChange"), columns, filter, sort);

            msg.append("<b>Weights since " + _dateFormat.format(date.getTime()) + " representing changes of " + pct + "% in the past " + max + " days:</b><p>");

            if (ts.getRowCount() > 0)
            {
                final Map<String, Map<String, List<ResultSet>>> summary = new TreeMap<String, Map<String, List<ResultSet>>>();

                ts.forEach(new TableSelector.ForEachBlock<ResultSet>(){
                    public void exec(ResultSet rs) throws SQLException
                    {
                        String area = rs.getString("Id/curLocation/area");
                        Map<String, List<ResultSet>> areaMap = summary.get(area);
                        if (areaMap == null)
                        {
                            areaMap = new TreeMap<String, List<ResultSet>>();
                            summary.put(area, areaMap);
                        }

                        String room = rs.getString("Id/curLocation/room");
                        List<ResultSet> roomList = areaMap.get(room);
                        if (roomList == null)
                        {
                            roomList = new ArrayList<ResultSet>();
                            summary.get(area).put(room, roomList);
                        }

                        roomList.add(rs);
                    }
                });

                msg.append("<table border=1><tr><td>Id</td><td>Area</td><td>Room</td><td>Cage</td><td>Current Weight (kg)</td><td>Weight Date</td><td>Previous Weight (kg)</td><td>Date</td><td>Percent Change</td><td>Days Between</td></tr>");
                for (String area : summary.keySet())
                {
                    Map<String, List<ResultSet>> areaValue = summary.get(area);
                    for (String room : areaValue.keySet())
                    {
                        List<ResultSet> roomValue = areaValue.get(room);
                        for (ResultSet rs : roomValue)
                        {
                            msg.append("<tr><td><a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/ehr" + _ehrContainer.getPath() +
                                "animalHistory.view?#_inputType:renderSingleSubject&_showReport:1&subject:" +
                                rs.getString("Id") + "&combineSubj:true&activeReport:abstract'>" + rs.getString("Id") +
                                "</a></td><td>" + area + "</td><td>" + room + "</td><td>" + rs.getString("Id/curLocation/cage") + "</td><td>" +
                                rs.getString("LatestWeight") + "</td><td>" + rs.getString("LatestWeightDate") + "</td><td>" +
                                rs.getString("weight") + "</td><td>" + rs.getString("date") + "</td><td>" + rs.getString("PctChange") +
                                "</td><td>" + rs.getString("IntervalInDays") + "</td></tr>");
                        }
                    }
                }
                msg.append("</table><p>\n");
                //msg.append("<p><a href='" + AppProps.getInstance().getBaseServerUrl() + AppProps.getInstance().getContextPath() + "/query" + _ehrContainer.getPath() + "executeQuery.view?schemaName=study&query.queryName=weightRelChange&query.Id/DataSet/Demographics/calculated_status~eq=Alive&query.PctChange~" + ct.getPreferredUrlKey() + "=" + pct + "&query.IntervalInDays~gte=" + min + "&query.IntervalInDays~lte=" + max + "&query.LatestWeightDate~dategte=" + _dateFormat.format(date) + "'>Click here to view these animals</a></p>");
                msg.append("<hr>");
            }
            else
            {
                msg.append("There are no changes during this period.<hr>");
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
}