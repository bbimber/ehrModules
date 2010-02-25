SELECT id, FixDate(date) as date, species, sex, weight, dam, sire, room, cage, concat(room, "-", cage) AS roomcage, FixDate(conception) as conception, remark,
     ( CONCAT_WS(',\n',
     CONCAT('Species: ', species),
     CONCAT('Sex: ', sex),
     CONCAT('Weight: ', weight),
     CONCAT('Dam: ', dam),
     CONCAT('Sire: ', sire),
     CONCAT('Room: ', room),
     CONCAT('Cage: ', cage),
     CONCAT('Conception: ', FixDate(conception)),
     CONCAT('remark: ', FixNewlines(remark))
     )) AS Description

FROM prenatal p
