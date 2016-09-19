DELIMITER $$

DROP PROCEDURE IF EXISTS `dyna2_dbo`.`createIndex2` $$
CREATE PROCEDURE `createIndex2`(prop_rdn int)
BEGIN


DECLARE done INT DEFAULT 0;
DECLARE idtoRes,idtoRes2,prop_factura int;
DECLARE nameRes varchar(50);
DECLARE i int;

DECLARE curs2 CURSOR FOR
select idto from instances
where property=2 and (OP='AND' or OP='OR')
and idto not in(select id_to from t_herencias where id_to_padre in(select idto from clases where name in(select name from herencia_clases))) /*herencia*/
and idto not in(select idto from clases where name in(select name from no_indexes_clases)) /*clases no indexadas*/
and idto not in(select idto from business_index) /*los de la tabla de antes (tabla de indices de negocio)*/
and idto>30; /*clases especiales*/




DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;


/*codigo que excluye clases especiales(<30), clases de negocio no indexadas y las que heredan de enumerado, uTask y action*/
OPEN curs2;
repeat
FETCH curs2 INTO idtoRes2;
if not done then
	INSERT INTO S_INDEX(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes2,prop_rdn,1,NULL,NULL,NULL);
END IF;
until done END REPEAT;
CLOSE curs2;

DROP TABLE business_index;
DROP TABLE herencia_clases;
DROP TABLE no_indexes_clases;

/*ahora se actualizan los que llevan PROPERTY_PREFIX*/
UPDATE S_INDEX SET PROPERTY_PREFIX=prop_rdn
WHERE ID_TO IN(SELECT IDTO FROM CLASES WHERE NAME IN (select name from prefix_clases));

DROP TABLE prefix_clases;

END $$

DELIMITER ;