DELIMITER $$

DROP PROCEDURE IF EXISTS `dyna6_dbo`.`permisosERP` $$
CREATE PROCEDURE `dyna6_dbo`.`permisosERP` ()
BEGIN


DECLARE taskL, classL, propL, urolL nvarchar(150);
DECLARE accessL nvarchar(10);
DECLARE neg, prior int;
DECLARE task, class, prop, urol, access int;



CREATE temporary TABLE labels (neg int, prior int, access nvarchar(50), task nvarchar(150),class nvarchar(50),prop nvarchar(150),urol nvarchar(150));
CREATE temporary TABLE codes (label nvarchar(50), id int);

INSERT INTO codes VALUES('VIEW',1);
INSERT INTO codes VALUES('NEW',2);
INSERT INTO codes VALUES('SET',4);
INSERT INTO codes VALUES('REL',8);
INSERT INTO codes VALUES('DEL',32);
INSERT INTO codes VALUES('CONCRETE',64);
INSERT INTO codes VALUES('COMENT',128);
INSERT INTO codes VALUES('SPECIALIZE',256);
INSERT INTO codes VALUES('ABSTRACT',512);

/*-- INI DATOS ------------------------------------------------------------------------



--INSERT INTO labels(neg,prior,access,task,class,prop,urol) VALUES(0,1,null,'Cliente',null,null);


----------------------------------PERMISOS PARA EL ERP------------------------
----------------------------------PERMISOS PARA EL ERP------------------------





----DENEGACIONES DE VIEW A PROPIEDADES----------------------*/

INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null, null,  'porcentaje_retención',		null);
INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null, null,  'aplica_retención',		null);
INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null, null,  'retención',		null);
INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null,  null, 'precio_aux',		null);
INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null,  null, 'descuento_aux',		null);
INSERT INTO labels  VALUES( 1, 0,	'VIEW',	null,  null, 'factor_descuento_global',		null);


/*----DENEGACIONES DE SET A PROPIEDADES QUE APLICAN A TODAS LAS CLASES----------------------*/

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'base',		        null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'total_iva',		    null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'recargo',		    null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'es_rectificada_por',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'rectificada',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'facturado',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'facturada',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'servida',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'deuda',		null);

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,  null,  'sourceClass',		null);










/*----DENEGACIONES DE SET A PROPIEDADES EN CLASES CONCRETAS ----------------------*/


INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'DOCUMENTO_COMERCIAL',  'importe',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'VENCIMIENTO',			null,		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA',		 'vencimientos',	null);

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'LÍNEA_ARTÍCULOS',   'pedido',		null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'LÍNEA_ARTÍCULOS',   'pedido',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'LÍNEA_ARTÍCULOS',   'albarán',		null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'LÍNEA_ARTÍCULOS',   'albarán',		null);

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'LÍNEA_ARTÍCULOS',   'facturada',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'PEDIDO',			'servida',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA',			'servida',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'PRESUPUESTO',		 'servida',		null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA',			'albarán',		null);

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'PEDIDO_COMERCIAL',		'fecha_estimada_entrega',		null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'PEDIDO_COMERCIAL',		'línea',		null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'PRESUPUESTO',		'línea',		null);

INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'LÍNEA_ARTÍCULOS',   'importe',		null);


/*------TODO terminar de añadir permisos set denegados y rel denegados en algunas propiedades de facturas rectificativas que no tiene
------sentido editar al rectificar una factura*/
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA_RECTIFICATIVA_VENTAS',		 'serie',	null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA_RECTIFICATIVA_VENTAS',		 'cliente',	null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'FACTURA_RECTIFICATIVA_VENTAS',		'cliente',		null);


/*------TODO copiar los mismo permisos de 'FACTURA_RECTIFICATIVA_VENTAS' aplicados a 'FACTURA_RECTIFICATIVA_COMPRAS',*/
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA_RECTIFICATIVA_COMPRAS',		 'serie',	null);
INSERT INTO labels  VALUES( 1, 0,	'SET',	null,	'FACTURA_RECTIFICATIVA_COMPRAS',		 'proveedor',	null);
INSERT INTO labels  VALUES( 1, 0,	'REL',	null,	'FACTURA_RECTIFICATIVA_COMPRAS',		'proveedor',		null);









/*-- END DATOS-------------------------------------------------------------------------

-- CODIGO -----------------------------------------------------------------------
---------------------------------------------------------------------------------*/
DELETE FROM ACCESS WHERE BORRAR=1;


WHILE EXISTS(SELECT * FROM labels) do
	SELECT neg=neg,prior=prior,taskL=task,classL=class,propL=prop,urolL=urol,accessL=access FROM labels;


	DELETE FROM labels WHERE	(task=taskL OR taskL is null) and
								(class=classL or classL is null ) and
								(prop=propL or propL is null ) and
								(urol=urolL or urolL is null) and
								(access=accessL or accessL is null);

	IF classL IS NULL then SET class=null;
	ELSE SELECT class=instances.idto FROM instances WHERE name=classL;
  end if;

	IF taskL IS NULL then SET task=null;
	ELSE SELECT task=instances.idto FROM instances WHERE name=taskL;
  end if;

	IF propL IS NULL then SET prop=NULL;
	ELSE SELECT prop=properties.PROP FROM properties WHERE properties.name=propL;
  end if;

	IF urolL IS NULL then SET urol=NULL;
	ELSE SELECT urol=ID_ROL FROM Roles WHERE name_rol=urolL;
  end if;

	IF accessL IS NULL then SET access=NULL;
	ELSE SELECT access=codes.id FROM codes WHERE label=accessL;
  end if;

	INSERT INTO Access(DENNIED,PRIORITY,TASK,USERROL,IDTO,PROP,ACCESSTYPE, BORRAR)
	VALUES(neg,prior,task,urol,class,prop,access,1);


END while;

DROP temporary TABLE labels;
DROP temporary TABLE codes;


END $$

DELIMITER ;