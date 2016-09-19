delete from s_index

--creacion de tablas temporales
--tabla para guardar las clases que llevaran indices de negocio
CREATE TABLE #business_clases (name nvarchar(100))
INSERT INTO #business_clases VALUES('FACTURA_A_CLIENTE')
INSERT INTO #business_clases VALUES('FACTURA_RECTIFICATIVA_VENTAS')
INSERT INTO #business_clases VALUES('FACTURA_PROVEEDOR')
INSERT INTO #business_clases VALUES('FACTURA_RECTIFICATIVA_COMPRAS')
INSERT INTO #business_clases VALUES('PEDIDO_DE_CLIENTE')
INSERT INTO #business_clases VALUES('PRESUPUESTO_COMPRA')
INSERT INTO #business_clases VALUES('PEDIDO_A_PROVEEDOR')
INSERT INTO #business_clases VALUES('ALBARÁN_PROVEEDOR')
INSERT INTO #business_clases VALUES('ALBARÁN_CLIENTE')
INSERT INTO #business_clases VALUES('PRESUPUESTO_VENTA')

--tabla para clases no indexadas
CREATE TABLE #no_indexes_clases (name nvarchar(100))
INSERT INTO #no_indexes_clases VALUES('PROVINCIA')
INSERT INTO #no_indexes_clases VALUES('PAÍS')
INSERT INTO #no_indexes_clases VALUES('EMPRESA')
INSERT INTO #no_indexes_clases VALUES('GRUPO_CLIENTE')

--tabla para almacenar las clases por las que no heredaran las clases que llevan indice
CREATE TABLE #herencia_clases (name nvarchar(100))
INSERT INTO #herencia_clases VALUES('UTASK')
INSERT INTO #herencia_clases VALUES('ENUMERATED')
INSERT INTO #herencia_clases VALUES('ACTION')

--clases que tendran como prefijo su property rdn(modificable abajo si se quiere otra property)
CREATE TABLE #prefix_clases (name nvarchar(100))
INSERT INTO #prefix_clases VALUES('LOTE')
INSERT INTO #prefix_clases VALUES('ENUMERATED')
INSERT INTO #prefix_clases VALUES('LÍNEA_FEEDING_CONCRETA_IN')
INSERT INTO #prefix_clases VALUES('PLANIFICACIÓN')
INSERT INTO #prefix_clases VALUES('TRABAJO')
INSERT INTO #prefix_clases VALUES('STOCK')
INSERT INTO #prefix_clases VALUES('LÍMITES_STOCK')
INSERT INTO #prefix_clases VALUES('RECURSO')
INSERT INTO #prefix_clases VALUES('LÍNEA_FEEDING_CONCRETA_OUT')
INSERT INTO #prefix_clases VALUES('LÍNEA_DEMANDA')



--creacion de tabla temporal para indices de negocio
--insercion de indices de negocio en s_index
CREATE TABLE #business_index (idto int)

DECLARE @idto int,@name varchar(50)
DECLARE @prop_rdn int
DECLARE @prop_factura int

SELECT @prop_rdn=properties.PROP FROM properties WHERE properties.name='rdn'
SELECT @prop_factura=properties.PROP FROM properties WHERE properties.name='serie'

DECLARE curs CURSOR
FOR SELECT idto,name FROM clases
where name in(select name from #business_clases)
OPEN curs;
FETCH NEXT FROM curs INTO @idto, @name
WHILE @@FETCH_STATUS = 0
BEGIN
	INSERT INTO #business_index VALUES(@idto)
	IF @name='FACTURA_A_CLIENTE' 
		BEGIN
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'A',@prop_factura,'A')
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'B',@prop_factura,'B')
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'E',@prop_factura,'E')
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'G',@prop_factura,'G')
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'O',@prop_factura,'O')
			INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'Q',@prop_factura,'Q')
		END
	ELSE IF @name='FACTURA_RECTIFICATIVA_VENTAS'
		INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,'R',@prop_factura,'R')
	ELSE
		INSERT INTO s_index([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,NULL,NULL,NULL)
	-- Get the next
	FETCH NEXT FROM curs INTO @idto, @name
END
CLOSE curs;
DEALLOCATE curs;

DROP TABLE #business_clases


--codigo que excluye clases especiales(<30), clases de negocio no indexadas y las que heredan de enumerado, uTask y action
DECLARE curs2 CURSOR FOR 
select idto,[name] from instances
where property=2 and (OP='AND' or OP='OR')
and idto not in(select id_to from t_herencias where id_to_padre in(select idto from clases where name in(select name from #herencia_clases))) --herencia
and idto not in(select idto from clases where name in(select name from #no_indexes_clases)) --clases no indexadas
and idto not in(select idto from #business_index) --los de la tabla de antes (tabla de indices de negocio)
and idto>30 --clases especiales
OPEN curs2;
FETCH NEXT FROM curs2 INTO @idto, @name
WHILE @@FETCH_STATUS = 0
BEGIN
	INSERT INTO S_INDEX([ID_TO],[PROPERTY],[INDEX],[PREFIX],[PROPERTY_FILTER],[VALUE_FILTER]) VALUES(@idto,@prop_rdn,1,NULL,NULL,NULL)
	-- Get the next
	FETCH NEXT FROM curs2 INTO @idto, @name
END
CLOSE curs2;
DEALLOCATE curs2;

DROP TABLE #business_index
DROP TABLE #herencia_clases
DROP TABLE #no_indexes_clases

--ahora se actualizan los que llevan [PROPERTY_PREFIX]
UPDATE S_INDEX SET [PROPERTY_PREFIX]=@prop_rdn
WHERE ID_TO IN(SELECT IDTO FROM CLASES WHERE NAME IN (select name from #prefix_clases))

DROP TABLE #prefix_clases


-------------------------------------script 2
DECLARE @idtoODT int,@valText nvarchar(100), @index int
DECLARE curs3 CURSOR FOR 
select id_to,max(val_texto) from o_datos_atrib
where id_to in(select id_to from s_index)
and property=2
group by id_to
order by id_to
OPEN curs3;
FETCH NEXT FROM curs3 INTO @idtoODT, @valText

DECLARE @inicio int, @inicioTmp int
DECLARE @valTextTmp nvarchar(100)
WHILE @@FETCH_STATUS = 0
BEGIN
	IF isnumeric(@valText)=1
		UPDATE S_INDEX SET [INDEX]=@valText+1 WHERE [ID_TO]=@idtoODT
	ELSE 
--		BEGIN  --comentado porque se hace abajo con otro cursor
--			SET @valTextTmp = substring(@valText, 2, len(@valText))
--			IF isnumeric(@valTextTmp)=1
--				UPDATE S_INDEX SET [INDEX]=@valTextTmp+1 WHERE [ID_TO]=@idtoODT
--			ELSE
				BEGIN
					SET @inicioTmp = CHARINDEX('-', @valText, 0)
					IF @inicioTmp<>0 --caso guion
						BEGIN
							WHILE (@inicioTmp<>0)
							BEGIN
								SET @inicio = @inicioTmp
								SET @inicioTmp = CHARINDEX('-', @valText, @inicio+1)
							END
							SET @valTextTmp = substring(@valText, @inicio+1, len(@valText)-@inicio)
							IF isnumeric(@valTextTmp)=1
								UPDATE S_INDEX SET [INDEX]=@valTextTmp+1 WHERE [ID_TO]=@idtoODT
						END
					ELSE
						BEGIN
							SET @inicioTmp = CHARINDEX('_', @valText, 0)
							IF @inicioTmp<>0 --caso guion bajo
								BEGIN
									WHILE (@inicioTmp<>0)
									BEGIN
										SET @inicio = @inicioTmp
										SET @inicioTmp = CHARINDEX('_', @valText, @inicio+1)
									END
									SET @valTextTmp = substring(@valText, @inicio+1, len(@valText)-@inicio)
									IF isnumeric(@valTextTmp)=1
										UPDATE S_INDEX SET [INDEX]=@valTextTmp+1 WHERE [ID_TO]=@idtoODT
								END
						END
				END
--		END
	-- Get the next
	FETCH NEXT FROM curs3 INTO @idtoODT, @valText
END
CLOSE curs3;
DEALLOCATE curs3;


DECLARE curs4 CURSOR FOR 
select id_to,max(substring(val_texto, 2, len(val_texto))) from o_datos_atrib
where id_to in(select id_to from s_index)
and property=2 and isnumeric(substring(val_texto, 1, 1))=0 and isnumeric(substring(val_texto, 2, len(val_texto)))=1
group by id_to--,substring(val_texto, 1, 1)
order by id_to
OPEN curs4;
FETCH NEXT FROM curs4 INTO @idtoODT, @valText

WHILE @@FETCH_STATUS = 0
BEGIN
	--IF isnumeric(@valText)=1 AND isnumeric(@caracter)=0 
	SELECT @index=s_index.[INDEX] FROM s_index WHERE [ID_TO]=@idtoODT
	IF @index<@valText+1
		UPDATE S_INDEX SET [INDEX]=@valText+1 WHERE [ID_TO]=@idtoODT
	-- Get the next
	FETCH NEXT FROM curs4 INTO @idtoODT, @valText
END
CLOSE curs4;
DEALLOCATE curs4;
