SELECT
g.id,
g.groupId.name as name,
CAST('yes' AS VARCHAR) as valueField

FROM ehr.animal_group_members g

WHERE (g.enddate IS NULL OR COALESCE(g.enddate, curdate()) >= curdate())

GROUP BY g.id, g.groupId.name

PIVOT valueField by name IN (select name FROM ehr.animal_groups)

