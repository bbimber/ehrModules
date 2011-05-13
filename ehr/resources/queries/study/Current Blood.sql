/*
 * Copyright (c) 2010-2011 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
SELECT
	bq.*,
	round(bq.weight*0.2*60, 1) AS MaxBlood,
	round((bq.weight*0.2*60) - bq.BloodLast30 - bq.BloodNext30 - coalesce(bq.quantity, 0), 1) AS AvailBlood
FROM
(
	SELECT
	  b.*,
	  (
	    CONVERT (
	    	(SELECT AVG(w.weight) AS _expr
	    	FROM study.weight w
		    WHERE w.id=b.id AND w.date=b.lastWeighDate
		    AND w.qcstate.publicdata = true
		   ), double )
	  ) AS weight
	FROM
	 	(
			 SELECT bi.*
			    ,timestampadd('SQL_TSI_DAY', -30, bi.date) as minDate
  			    ,timestampadd('SQL_TSI_DAY', 30, bi.date) as maxDate
	 		    ,( CONVERT(
                      (SELECT MAX(w.date) as _expr
                        FROM study.weight w
                        WHERE w.id = bi.id
                        --AND w.date <= bi.date
                        AND CAST(CAST(w.date AS DATE) AS TIMESTAMP) <= bi.date
                        AND w.qcstate.publicdata = true
                      ), timestamp )
                  ) AS lastWeighDate
	 		    , ( COALESCE (
	    			(SELECT SUM(draws.quantity) AS _expr
	    		      FROM study."Blood Draws" draws
	    			  WHERE draws.id=bi.id
                          AND draws.date BETWEEN TIMESTAMPADD('SQL_TSI_DAY', -30, bi.date) AND bi.date
                          AND (draws.qcstate.metadata.DraftData = true OR draws.qcstate.publicdata = true)
                          --when counting backwards, dont include this date
                          AND draws.date != bi.date
                     ), 0 )
	  		      ) AS BloodLast30
	 		    , ( COALESCE (
	    			(SELECT SUM(draws.quantity) AS _expr
	    		      FROM study."Blood Draws" draws
	    			  WHERE draws.id=bi.id
                          AND draws.date BETWEEN bi.date AND TIMESTAMPADD('SQL_TSI_DAY', 30, bi.date)
                          AND (draws.qcstate.metadata.DraftData = true OR draws.qcstate.publicdata = true)
                          --when counting forwards, dont include this date
                          AND draws.date != bi.date
                     ), 0 )
	  		      ) AS BloodNext30
            from (
              SELECT
                  b.id,
                  b.date,
                  --b.lsid,
                  --b.qcstate,
                  b.qcstate.label as status,
                  sum(b.quantity) as quantity
              FROM study.blood b
	     	  WHERE b.date >= TIMESTAMPADD('SQL_TSI_DAY', -30, now())
	     	  AND (b.qcstate.metadata.DraftData = true OR b.qcstate.publicdata = true)
	     	  group by b.id, b.date, b.qcstate.label

	     	  UNION ALL
              SELECT
                  b.id,
                  TIMESTAMPADD('SQL_TSI_DAY', 31, b.date) as date,
                  --null as lsid,
                  --null as qcstate,
                  null as status,
                  0 as quantity
              FROM study.blood b
	     	  WHERE b.date >= TIMESTAMPADD('SQL_TSI_DAY', -30, now())
	     	  AND (b.qcstate.metadata.DraftData = true OR b.qcstate.publicdata = true)
	     	  GROUP BY b.id, b.date, b.qcstate.label

              --add one row per animal, showing todays date
	     	  UNION ALL
              SELECT
                  b.id,
                  now() as date,
                  --null as lsid,
                  --null as qcstate,
                  null as status,
                  0 as quantity
              FROM study.demographics b
              --WHERE b.id.status.status = 'Alive'
              ) bi
	    	) b
	) bq
