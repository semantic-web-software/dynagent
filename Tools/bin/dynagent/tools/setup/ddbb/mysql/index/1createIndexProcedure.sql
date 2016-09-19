DELIMITER $$

DROP PROCEDURE IF EXISTS `dyna2_dbo`.`createIndex` $$
CREATE PROCEDURE `dyna2_dbo`.`createIndex` ()
BEGIN

DECLARE done INT DEFAULT 0;
DECLARE idtoRes,idtoRes2,prop_rdn,prop_factura int;
DECLARE nameRes varchar(50);
DECLARE i int;

DECLARE curs CURSOR FOR SELECT idto,name FROM clases where name in(select name from business_clases);

DECLARE curs2 CURSOR FOR
select idto from instances
where property=2 and (OP='AND' or OP='OR')
and idto not in(select id_to from t_herencias where id_to_padre in(select idto from clases where name in(select name from herencia_clases))) /*herencia*/
and idto not in(select idto from clases where name in(select name from no_indexes_clases)) /*clases no indexadas*/
and idto not in(select idto from business_index) /*los de la tabla de antes (tabla de indices de negocio)*/
and idto>30; /*clases especiales*/

DECLARE curs3 CURSOR FOR SELECT properties.PROP FROM properties WHERE properties.name='rdn';
DECLARE curs4 CURSOR FOR SELECT properties.PROP FROM properties WHERE properties.name='serie';

DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;


/*limpieza de indices antiguos*/
delete from s_index;

/*TABLAS TEMPORALES*/
/*creacion de tablas temporales*/
/*tabla para guardar las clases que llevaran indices de negocio*/
CREATE TABLE business_clases (name nvarchar(100));
INSERT INTO business_clases VALUES('FACTURA_A_CLIENTE');
INSERT INTO business_clases VALUES('FACTURA_RECTIFICATIVA_VENTAS');
INSERT INTO business_clases VALUES('FACTURA_PROVEEDOR');
INSERT INTO business_clases VALUES('FACTURA_RECTIFICATIVA_COMPRAS');
INSERT INTO business_clases VALUES('PEDIDO_DE_CLIENTE');
INSERT INTO business_clases VALUES('PRESUPUESTO_COMPRA');
INSERT INTO business_clases VALUES('PEDIDO_A_PROVEEDOR');
INSERT INTO business_clases VALUES('ALBARÁN_PROVEEDOR');
INSERT INTO business_clases VALUES('ALBARÁN_CLIENTE');
INSERT INTO business_clases VALUES('PRESUPUESTO_VENTA');

/*tabla para clases no indexadas*/
CREATE TABLE no_indexes_clases (name nvarchar(100));
INSERT INTO no_indexes_clases VALUES('PROVINCIA');
INSERT INTO no_indexes_clases VALUES('PAÍS');
INSERT INTO no_indexes_clases VALUES('EMPRESA');
INSERT INTO no_indexes_clases VALUES('GRUPO_CLIENTE');

/*tabla para almacenar las clases por las que no heredaran las clases que llevan indice*/
CREATE TABLE herencia_clases (name nvarchar(100));
INSERT INTO herencia_clases VALUES('UTASK');
INSERT INTO herencia_clases VALUES('ENUMERATED');
INSERT INTO herencia_clases VALUES('ACTION');

/*clases que tendran como prefijo su property rdn(modificable abajo si se quiere otra property)*/
CREATE TABLE prefix_clases (name nvarchar(100));
INSERT INTO prefix_clases VALUES('LOTE');
INSERT INTO prefix_clases VALUES('ENUMERATED');
INSERT INTO prefix_clases VALUES('LÍNEA_FEEDING_IN');
INSERT INTO prefix_clases VALUES('PLANIFICACIÓN');
INSERT INTO prefix_clases VALUES('BPMN');
INSERT INTO prefix_clases VALUES('JOB');
INSERT INTO prefix_clases VALUES('STOCK');
INSERT INTO prefix_clases VALUES('LÍMITES_STOCK');
INSERT INTO prefix_clases VALUES('RECURSO');
INSERT INTO prefix_clases VALUES('LÍNEA_FEEDING_OUT');

/*creacion de tabla temporal para indices de negocio
insercion de indices de negocio en s_index*/
CREATE TABLE business_index (idto int);



set i=1;
OPEN curs3;
while i<>0 do
fetch curs3 into prop_rdn;
set i = 0;
END while;
CLOSE curs3;

set i=1;
OPEN curs4;
while i<>0 do
fetch curs4 into prop_factura;
set i = 0;
END while;
CLOSE curs4;


OPEN curs;
repeat
fetch curs into idtoRes, nameRes;
if not done then
	INSERT INTO business_index VALUES(idtoRes);
	IF nameRes='FACTURA_A_CLIENTE' then
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'A',prop_factura,'A');
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'B',prop_factura,'B');
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'E',prop_factura,'E');
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'G',prop_factura,'G');
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'O',prop_factura,'O');
			INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'Q',prop_factura,'Q');
	ELSEIF nameRes='FACTURA_RECTIFICATIVA_VENTAS' then
		INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,'R',prop_factura,'R');
  ELSE
		INSERT INTO s_index(ID_TO,PROPERTY,`INDEX`,PREFIX,PROPERTY_FILTER,VALUE_FILTER) VALUES(idtoRes,prop_rdn,1,NULL,NULL,NULL);
  end if;
END IF;
until done END REPEAT;
CLOSE curs;


DROP TABLE business_clases;

END $$

DELIMITER ;