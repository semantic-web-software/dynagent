
SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- TOC entry 168 (class 3079 OID 11727)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

--
-- TOC entry 1920 (class 0 OID 0)
-- Dependencies: 168
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--



SET search_path = public, pg_catalog;

-- Completed on 2013-03-09 12:49:02

--
-- PostgreSQL database dump complete
--

DROP TABLE IF EXISTS view_comision CASCADE;
create table view_comision(
	cod_factura character varying,
	fecha_factura bigint,
	cod_cliente character varying,
	nombre_cliente character varying,
	cod_articulo character varying,
	"descripcion_articulo" character varying,
	familia character varying,
	subfamilia character varying,
	marca character varying,
	cod_agente_cliente character varying,
	nombre_agente_cliente character varying,
	cod_agente_venta character varying,
	nombre_agente_venta character varying,
	"cantidad" double precision,
	coste_unitario double precision,
	"precio" double precision,
	"precio_ii" double precision,
	total_coste double precision,
	"importe_base" double precision,
	"importe_base_ii" double precision,
	margen double precision,
	porcentaje double precision,
	comision  double precision,
	agente integer,
	com_tid integer,
	com_idto integer
	);

-- Function: comisiones(integer[], bigint, bigint)

-- DROP FUNCTION comisiones(integer[], bigint, bigint);

CREATE OR REPLACE FUNCTION comisiones(id_prod_liquidar integer,idto_prod_liquidar integer,agente integer[], param_fecha_inicio bigint, param_fecha_fin bigint)
  RETURNS SETOF view_comision AS
$BODY$

BEGIN

return query 
select	"cod_factura"::character varying,
	 "fecha_factura"::bigint,
	 "cod_cliente"::character varying,
	nombre_cliente::character varying,
	 "cod_articulo"::character varying,
	"descripcion_articulo"::character varying,
	familia::character varying,
	subfamilia::character varying,
	marca::character varying,
	cod_agente_cliente::character varying,
	nombre_agente_cliente::character varying,
	cod_agente_venta::character varying,
	nombre_agente_venta::character varying,
	"cantidad"::double precision,
	coste_unitario::double precision,
	"precio"::double precision,
	"precio_ii"::double precision,
	total_coste::double precision,
	"importe_base"::double precision,
	"importe_base_ii"::double precision,

	margen::double precision as margen,

	case 	when importe_base=0 or importe_base is null then 0::double precision
		when idoexclusivo= idocolaborador then  (100*(importe_exclusiva+importe_colaborador)/importe_base)::double precision
		when idocolaborador is null or idoexclusivo<> idocolaborador and idoexclusivo= agList then (100*importe_exclusiva/importe_base)::double precision
		else (100*importe_colaborador/importe_base)::double precision END as porcentaje,

	case 	when idoexclusivo= idocolaborador then  (importe_exclusiva+importe_colaborador)::double precision
		when idocolaborador is null or idoexclusivo<> idocolaborador and idoexclusivo= agList then importe_exclusiva::double precision
		else importe_colaborador::double precision END as comision,
	agList as idoAgente,
	com_tid,
	com_idto

from (

select 	fact.rdn as "cod_factura",
	fact.fecha as "fecha_factura",
	cli.rdn as "cod_cliente",
	max(cli.nombre) as nombre_cliente,
	art.rdn as "cod_articulo",
	art."descripción" as "descripcion_articulo",
	max(familia."descripción") as familia,
	max(subfamilia."descripción") as subfamilia,
	max(marca."descripción") as marca,
	max(agcli.rdn) as "cod_agente_cliente",
	max(agcli.nombre) as "nombre_agente_cliente",
	max(agfact.rdn) as "cod_agente_venta",
	max(agfact.nombre) as "nombre_agente_venta",
	sum(la.cantidad) as "cantidad",
	la.precio as "precio",
	max(la.coste_unitario) as "coste_unitario",
	sum(la.coste_unitario*la.cantidad) as total_coste,
	max(la.precio_iva_incluido) as precio_ii,
	sum(la.importe*(1-factor_descuento_global)) as "importe_base",
	sum(la.importe_con_iva*(1-factor_descuento_global)) as "importe_base_ii",

	sum(case		WHEN exc.porcentaje is null and cli.agente_comercial is not null 	and	(pormar."comisión_porcentualId" is null or la.margen_beneficio  not BETWEEN "margen_mínimo" AND "margen_máximo")
													THEN la.importe*(1-factor_descuento_global)*com."porcentaje_base"/100*(1-cli."porcentaje_exclusividad_defecto"/100)
			WHEN exc.porcentaje is null and cli.agente_comercial is not null 	and 	la.margen_beneficio  BETWEEN "margen_mínimo" AND "margen_máximo"
													THEN la.importe*(1-factor_descuento_global)*pormar."porcentaje"/100*(     1-cli."porcentaje_exclusividad_defecto"/100)

			WHEN exc.porcentaje is null and cli.agente_comercial is null 	and 	(pormar."comisión_porcentualId" is null or la.margen_beneficio  not BETWEEN "margen_mínimo" AND "margen_máximo")
													THEN la.importe*(1-factor_descuento_global)*com."porcentaje_base"/100
			WHEN exc.porcentaje is null and cli.agente_comercial is null 	and 	la.margen_beneficio   BETWEEN "margen_mínimo" AND "margen_máximo"
													THEN la.importe*(1-factor_descuento_global)*pormar."porcentaje"/100

			WHEN exc.porcentaje>=0 and la.margen_beneficio   BETWEEN "margen_mínimo" AND "margen_máximo"
					 								THEN la.importe*(1-factor_descuento_global)*pormar."porcentaje"/100*(1-exc."porcentaje"/100)
			ELSE la.importe*(1-factor_descuento_global)*com."porcentaje_base"/100*(1-exc."porcentaje"/100) END) as importe_colaborador,


	sum(case	WHEN exc.porcentaje is null and (pormar."comisión_porcentualId" is null or la.margen_beneficio not BETWEEN "margen_mínimo" AND "margen_máximo")
				then la.importe*(1-factor_descuento_global)*com."porcentaje_base"/100*cli."porcentaje_exclusividad_defecto"/100

			WHEN exc.porcentaje is null and la.margen_beneficio BETWEEN "margen_mínimo" AND "margen_máximo"
				then la.importe*(1-factor_descuento_global)*pormar."porcentaje"/100*cli."porcentaje_exclusividad_defecto"/100

			WHEN exc.porcentaje>0 and la.margen_beneficio BETWEEN "margen_mínimo" AND "margen_máximo"
				then	la.importe*(1-factor_descuento_global)*pormar."porcentaje"/100*exc."porcentaje"/100

			ELSE la.importe*(1-factor_descuento_global)*com."porcentaje_base"/100*exc."porcentaje"/100  END) as importe_exclusiva,
	max(la.margen_beneficio) as margen,
	max(cli.agente_comercial*1000+cli."agente_comercialIdto") as idoexclusivo,
	max(fact.agente_comercial*1000+fact."agente_comercialIdto") as idocolaborador,
	com."tableId" as com_tid,
	la."comisiónIdto" as com_idto
FROM
	"v_factura" as fact  																																				inner join
	"v_factura#línea_artículos" as fla 	on(fact."tableId"=fla."facturaId" and fact.idto=fla."facturaIdto") 																inner join
	"v_línea_artículos" as la 			on(la."tableId"=fla."línea_artículosId" and la.idto=fla."línea_artículosIdto") 													inner join
	"v_artículo" as art			on(la.producto=art."tableId" and la."productoIdto"=art.idto) 																			inner join
	"comisión_porcentual" as com		on( la."comisión"=com."tableId" and la."comisiónIdto" in (select id from clase where rdn like 'COMISI_N_PORCENTUAL') )			inner join
	"v_cliente" as cli			on(fact."clienteIdto"=cli."idto" and fact."cliente"=cli."tableId") 																		left join
	
	( 	"v_exclusividad_comercial#agente_comercial" as excag 																					inner join
		exclusividad_comercial as exc		on(exc."tableId"=excag."exclusividad_comercialId"))
				on(
					(cli."tableId"=exc."cliente_empresaId" and cli."idto"=346 or
	   				cli."tableId"=exc."cliente_particularId" and cli."idto"=485 or
					cli."tableId"=exc."distribuidorId" and cli."idto"=528) and
	   				fact."agente_comercial"=excag."agente_comercialId" and fact."agente_comercialIdto"=excag."agente_comercialIdto"
	   			)																																						left join
	"porciento_función_margen" as pormar	on( pormar."comisión_porcentualId"=com."tableId")																			left join
	"v_agente_comercial" as agfact		on(fact."agente_comercial"=agfact."tableId" and fact."agente_comercialIdto"=agfact.idto)										left join
	"v_agente_comercial" as agcli		on(cli."agente_comercial"=agcli."tableId" and cli."agente_comercialIdto"=agcli.idto)											left join
	familia				on(familia."tableId"=art.familia)																												left join
	subfamilia				on(subfamilia."tableId"=art.subfamilia)																										left join
	marca				on(marca."tableId"=art.marca)																													inner join
	
	unnest(agente) as agList on(cli.agente_comercial*1000+cli."agente_comercialIdto"=agList or 
								fact.agente_comercial*1000+fact."agente_comercialIdto"=agList)																			inner join
	
	(select case 	when max(la.fecha_fin) is not null then max(la.fecha_fin)+1--devuelvo 1 sg mas porque el filtrado es a partir de mayor o igual, para no repetir ultima venta
       				else param_fecha_inicio end as fecha_inicio,
       				ag."tableId" as ag_id,ag.idto as ag_idto
       		
       from "v_albarán_venta, factura_a_cliente" as fact	inner join  	       		
       		"v_albarán_venta, factura_a_cliente#línea_artículos" as fla on(	fact."tableId"=fla."albarán_venta, factura_a_clienteId" and 
       															fact.idto=fla."albarán_venta, factura_a_clienteIdto")					 	inner join
       		"v_línea_servicio"  as la 	on(la."tableId"=fla."línea_artículosId" and la.idto=fla."línea_artículosIdto")						inner join
       		"v_artículo" as art	on(la.producto=art."tableId" and la."productoIdto"=art.idto and
       								art."tableId"=id_prod_liquidar and art.idto=idto_prod_liquidar) 										right join
       										
       		v_agente_comercial as ag	on(	fact.agente_comercial=ag."tableId" and fact."agente_comercialIdto"=ag.idto)
       		 
       		group by ag."tableId",ag.idto
       	) as fechas on (ag_id*1000+ag_idto=agList )

WHERE
	
	la.cantidad<>0 and
	fact.fecha >= fechas.fecha_inicio	and
	(param_fecha_fin is null or fact.fecha<= param_fecha_fin)

group by cod_factura,fecha_factura,cod_cliente,cod_articulo,descripcion_articulo,precio,com."tableId",la."comisiónIdto"
order by fecha_factura,max(la."número")

) as query inner join
unnest(agente) as agList  on(idoexclusivo= agList and   importe_exclusiva<>0 or idocolaborador=agList and importe_colaborador<>0);

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION comisiones(integer,integer,integer[], bigint, bigint) OWNER TO dynagent;

drop view IF EXISTS  view_liquidacion; 
create view view_liquidacion 
as 

select cliente,doc,precio,precio_iva_incluido,producto from

(select cliente,doc,precio,precio_iva_incluido,producto,rank() OVER (PARTITION BY producto ORDER BY fecha DESC) AS pos

from 
(

select doc.fecha,doc."tableId"*1000+doc.idto as doc,doc.cliente*1000+doc."clienteIdto" as cliente,lin.precio,lin.precio_iva_incluido,lin.producto*1000+lin."productoIdto" as producto

from 

v_factura_a_cliente as doc 																inner join
"v_factura_a_cliente#línea_artículos" as  linDoc 	on(linDoc."factura_a_clienteId"=doc."tableId" and linDoc."factura_a_clienteIdto"=doc."idto") 	inner join
"v_línea_artículos" as lin				on(linDoc."línea_artículosId"=lin."tableId" and linDoc."línea_artículosIdto"=lin.idto)

where lin.albaranado=true and lin.facturado=true

union all

select  doc.fecha,doc."tableId"*1000+doc.idto as doc,doc.cliente*1000+doc."clienteIdto" as cliente,lin.precio,lin.precio_iva_incluido, lin.producto*1000+460 as producto
from

"v_liquidación_por_cambio_precio" as doc										inner join
"línea_cambio_precio" as lin			on(lin."liquidación_cliente_cambio_precioId"=doc."tableId" and doc.idto=584)
where lin.precio_iva_incluido_anterior is not null

) as data) as dataR

where pos=1;

-- para uso en reports
drop view IF EXISTS  view_linea; 

create view view_linea as
(select
	fecha,
	reservado,
	CASE 	when "pedido_traspaso_almacenesId" is not null THEN "pedido_traspaso_almacenesId"			
		    else null END as pedidoid,
	CASE	when "pedido_traspaso_almacenesId" is not null THEN 561
		 	ELSE null END as pedidoidto,		 
	CASE 	when "traspaso_almacenesId" is not null THEN "traspaso_almacenesId"
		    else null END as docid,
	CASE when "traspaso_almacenesId" is not null THEN 272
		 ELSE null END as docidto,
	CASE when "producción_materialIdCONSUMO" is not null THEN "producción_materialIdCONSUMO"
		ELSE null END as proconsumo,
	CASE when "producción_materialIdLÍNEA" is not null THEN "producción_materialIdLÍNEA"
	ELSE null END as prolinea,
	cantidad,
	producto,
	clave_producto,
	talla,
	color,
	medida1,
	medida2,
	CASE 	WHEN "loteLOTE_PERECEDERO" is not null THEN 	"loteLOTE_PERECEDERO"
		WHEN "loteSERIE" is not null THEN 		"loteSERIE"
		WHEN "loteLOTE_PACK" is not null THEN "loteLOTE_PACK" END	as loteid,

	CASE 	WHEN "loteLOTE_PERECEDERO" is not null THEN 469
		WHEN "loteSERIE" is not null  THEN 	 380
		ELSE	355			END			as loteidto,
	estado,
	"tableId" as tableid,
	198 as idto


from
	"línea_materia" 
union all

select
	fecha,
	reservado,
	CASE 	when "pedido_de_clienteId" is not null THEN "pedido_de_clienteId"			
		    else null END as pedidoid,
	CASE	when "pedido_de_clienteId" is not null THEN 159
		 	ELSE null END as pedidoidto,
	CASE when "albarán-factura_clienteId" is not null THEN "albarán-factura_clienteId"
		 when "albarán_clienteId" is not null THEN "albarán_clienteId"
		 when "albarán-factura_proveedorId" is not null THEN "albarán-factura_proveedorId"
		 when "albarán_proveedorId" is not null THEN "albarán_proveedorId" 
		 else null END as docid,
	CASE when "albarán-factura_clienteId" is not null THEN 319
		 when "albarán_clienteId" is not null THEN 184 
		 when "albarán-factura_proveedorId" is not null THEN 116
		 when "albarán_proveedorId" is not null THEN 172 
		 ELSE null END as docidto,	 
		 
	CASE when "producción_materialIdCONSUMO" is not null THEN "producción_materialIdCONSUMO"
		ELSE null END as proconsumo,
	CASE when "producción_materialIdLÍNEA" is not null THEN "producción_materialIdLÍNEA"
	ELSE null END as prolinea,
	cantidad,
	producto,
	clave_producto,
	talla,
	color,
	medida1,
	medida2,
	CASE 	WHEN "loteLOTE_PERECEDERO" is not null THEN 	"loteLOTE_PERECEDERO"
		WHEN "loteSERIE" is not null THEN 		"loteSERIE"
		WHEN "loteLOTE_PACK" is not null THEN "loteLOTE_PACK" END	as loteid,

	CASE 	WHEN "loteLOTE_PERECEDERO" is not null THEN 469
		WHEN "loteSERIE" is not null  THEN 	 380
		ELSE	355			END			as loteidto,		
	estado,
	"tableId" as tableid,
	427 as idto


from
	"línea_artículos_materia" 
);

-- trazabilidad
CREATE OR REPLACE FUNCTION trazabilidad(
    IN busca_descendente boolean,
    IN loteid_param integer,
    IN loteidto_param integer)
  RETURNS TABLE(loteidres integer, loteidtores integer) AS
$BODY$
DECLARE
steps integer:=0;
BEGIN

DROP TABLE IF EXISTS tmplotesprev;
CREATE TABLE tmplotesprev(id integer,idto integer);

DROP TABLE IF EXISTS tmplotesnext;
CREATE TABLE tmplotesnext(id integer,idto integer);

DROP TABLE IF EXISTS tmplotesres;
CREATE TABLE tmplotesres(id integer,idto integer);


insert into tmplotesprev(id,idto) values(loteid_param,loteidto_param);

WHILE steps=0 or exists(select * from tmplotesnext) 
LOOP
	RAISE NOTICE 'STEP %',steps;
	delete from tmplotesnext;
	INSERT INTO tmplotesnext(id,idto)

	select
		case when busca_descendente then out_li.loteid
		else in_li.loteid end as id,
		case when busca_descendente then out_li.loteidto
		else in_li.loteidto end as idto

	from
		"view_linea" as out_li												inner join
		"view_linea" as in_li				on(in_li.proconsumo=out_li.prolinea )				inner join
		"producción_material" as produc			on(in_li.proconsumo=produc."tableId")				inner join
		tmplotesprev as prev				on(busca_descendente and 
								   in_li.loteid=prev.id and 
								   in_li.loteidto=prev.idto or 
								   not busca_descendente and 
								   out_li.loteid=prev.id and 
								   out_li.loteidto=prev.idto);
	
	if exists(select * from tmplotesnext)	then	
		RAISE NOTICE 'SI EXISTE';
		--delete from tmplotesres;
		insert into tmplotesres select * from tmplotesnext;		

		delete from tmplotesprev;						   		
		insert into tmplotesprev select * from tmplotesnext;				
	end if;
	steps:=steps+1;
END LOOP;

IF steps=1 THEN
	insert into tmplotesres select * from tmplotesprev;		
END IF;
return query select * from tmplotesres;
end 
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION trazabilidad(boolean, integer, integer)
  OWNER TO dynagent;

  
--
-- TOC entry 179 (class 1255 OID 41565)
-- Name: cdf(double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--
--  control stock
CREATE OR REPLACE FUNCTION erf(x double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ BEGIN return public.erfcore(x,0);
END;
$$;


ALTER FUNCTION public.erf(x double precision) OWNER TO dynagent;

--
-- TOC entry 187 (class 1255 OID 49457)
-- Name: erfc(double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION erfc(x double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ BEGIN return public.erfcore(x,1);
END;
$$;


ALTER FUNCTION public.erfc(x double precision) OWNER TO dynagent;

--
-- TOC entry 182 (class 1255 OID 41562)
-- Name: erfcore(double precision, integer); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION erfcore(x double precision, j integer) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ DECLARE ax double precision;
res double precision;
y double precision;
z double precision;
xnum double precision;
xden double precision;
BEGIN ax:=abs(x);
res:=0.0;
if ax<=0.46875 then y:=ax;
z:=y*y;
xnum:=1.85777706184603153e-1*z;
xden:=z;
xnum:=(xnum+3.16112374387056560e00)*z;
xnum:=(xnum+1.13864154151050156e02)*z;
xnum:=(xnum+3.77485237685302021e02)*z;
xden:=(xden+2.36012909523441209e01)*z;
xden:=(xden+2.44024637934444173e02)*z;
xden:=(xden+1.28261652607737228e03)*z;
res:=x*(xnum+3.20937758913846947e03)/(xden+2.84423683343917062e03);
if j<>0 then res:=1-res;
end if;
if j=2 then res:=exp(z)*res;
end if;
elsif ax<=4.0 then y:=ax;
xnum:=2.15311535474403846e-8*y;
xden:=y;
xnum:=(xnum+5.64188496988670089e-1)*y;
xnum:=(xnum+8.88314979438837594e00)*y;
xnum:=(xnum+6.61191906371416295e01)*y;
xnum:=(xnum+2.98635138197400131e02)*y;
xnum:=(xnum+8.81952221241769090e02)*y;
xnum :=(xnum+1.71204761263407058e03)*y;
xnum:=(xnum+2.05107837782607147e03)*y;
xden:=(xden+1.57449261107098347e01)*y;
xden:=(xden+1.17693950891312499e02)*y;
xden:=(xden+5.37181101862009858e02)*y;
xden:=(xden+1.62138957456669019e03)*y;
xden:=(xden+3.29079923573345963e03)*y;
xden:=(xden+4.36261909014324716e03)*y;
xden:=(xden+3.43936767414372164e03)*y;
res:=(xnum+1.23033935479799725e03)/(xden+1.23033935480374942e03);
if j<>2 then z:=round(y*16)/16;
res:=exp(-z*z)*exp(-1.0*(y-z)*(y+z))*res;
end if;
elsif ax>4.0 then y:=ax;
z:=1/(y*y);
xnum:=1.63153871373020978e-2*z;
xden:=z;
xnum:=(xnum+3.05326634961232344e-1)*z;
xnum:=(xnum+3.60344899949804439e-1)*z;
xnum:=(xnum+1.25781726111229246e-1)*z;
xnum:=(xnum+1.60837851487422766e-2)*z;
xden:=(xden+2.56852019228982242e00)*z;
xden:=(xden+1.87295284992346047e00)*z;
xden:=(xden+5.27905102951428412e-1)*z;
xden:=(xden+6.05183413124413191e-2)*z;
res:=z*(xnum+6.58749161529837803e-4)/(xden+2.33520497626869185e-3);
res:=(5.6418958354775628695E-1-res)/y;
if j<>2 then z:=round(y*16)/16;
res :=exp(-z*z)*exp(-1.0*(y-z)*(y+z))*res;
end if;
end if;
if j=0 then if x>0.46875 then res:=(0.5-res)+0.5;
end if;
if x<-0.46875 then res:=(-0.5+res)-0.5;
end if;
elsif j=1 then if x<-0.46875 then res:=2.0-res;
end if;
elsif j=2 then if x<-0.46875 then z:=round(y*16)/16;
y:=exp(z*z)*exp((x-z)*(x+z));
res:=y+y-res;
end if;
end if;
return res;
END;
$$;


ALTER FUNCTION public.erfcore(x double precision, j integer) OWNER TO dynagent;

--
-- TOC entry 188 (class 1255 OID 49453)
-- Name: loss(double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION loss(z double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$
  BEGIN
return normpdf(z) - z*(1-normcdf(z));
END;
$$;


ALTER FUNCTION public.loss(z double precision) OWNER TO dynagent;

--
-- TOC entry 186 (class 1255 OID 49456)
-- Name: normcdf(double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION normcdf(x double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ BEGIN return public.normcdf(x,0.0,1.0);
END;
$$;


ALTER FUNCTION public.normcdf(x double precision) OWNER TO dynagent;

--
-- TOC entry 185 (class 1255 OID 49455)
-- Name: normcdf(double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION normcdf(x double precision, mu double precision, sigma double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ DECLARE y double precision;
t double precision;
BEGIN t:=x-mu;
y:=0.5*public.erfc(-t/(sigma*sqrt(2.0)));
if y>1.0 then y:=1.0;
end if;
return y;
END;
$$;


ALTER FUNCTION public.normcdf(x double precision, mu double precision, sigma double precision) OWNER TO dynagent;

--
-- TOC entry 184 (class 1255 OID 49454)
-- Name: normpdf(double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION normpdf(x double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ BEGIN return public.normpdf(x,0.0,1.0);
END;
$$;


ALTER FUNCTION public.normpdf(x double precision) OWNER TO dynagent;

--
-- TOC entry 183 (class 1255 OID 49450)
-- Name: normpdf(double precision, double precision, double precision); Type: FUNCTION; Schema: public; Owner: dynagent
--

CREATE OR REPLACE FUNCTION normpdf(x double precision, mu double precision, sigma double precision) RETURNS double precision
    LANGUAGE plpgsql
    AS $$ DECLARE y double precision;
u double precision;
BEGIN u:=(x-mu)/abs(sigma);
y:=(1/(sqrt(2*pi())*abs(sigma)))*exp(-u*u/2);
return y;
END;
$$;


ALTER FUNCTION public.normpdf(x double precision, mu double precision, sigma double precision) OWNER TO dynagent;

CREATE OR REPLACE FUNCTION loss_inversa(p double precision)
  RETURNS double precision AS
$BODY$DECLARE 

z double precision:=-4;
oldz double precision:=-4;

p_curr double precision;

old_error double precision=loss(z);
old_error_empeora double precision=10;
error double precision:=old_error;
inicio boolean:=true;
sentido int:=1;
incr double precision:=0.1;
umbral_error double precision:=0.03;
contador integer:=0;

BEGIN

LOOP

 p_curr=loss(z);
 error=abs(p-p_curr);
 -- RAISE INFO '%','INI,INcr:'||incr||',err:'||error||',olderr:'||old_error||' z:'||z;
 IF error<umbral_error THEN return z;
 END IF;

 IF error>old_error and  not inicio THEN

	IF error<old_error_empeora THEN		
		incr=incr/2;				
		old_error_empeora=error;
	ELSE		
		sentido= -1*sentido;				
		incr=0.1;	
		old_error_empeora=10;
	END IF;
	z=oldz+incr*sentido;	
 ELSE
	oldz=z;
	z=z+incr*sentido;
	old_error=error;
	old_error_empeora=10;
 END IF;

contador:=contador+1;
IF contador>1000 THEN
	RAISE INFO 'LOSS INVERSA NO CONVERGE';
	umbral_error:=0.1;
END IF;
 
 inicio=false;
 -- RAISE INFO '%','END:'||error||' z:'||z;
END LOOP;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION loss_inversa(double precision) OWNER TO dynagent;

-- Table: estadistica_rotacion
DROP FUNCTION IF EXISTS rotacion_reorden(double precision,double precision, double precision, double precision,integer[],character varying);
DROP FUNCTION IF EXISTS rotacion_oferta(integer, integer,integer,integer[],boolean,boolean,boolean);
DROP FUNCTION IF EXISTS rotacion_reserva(integer,integer, integer[],boolean,boolean,boolean);
DROP FUNCTION IF EXISTS ajusta_dia_a_inicio(integer);

DROP TABLE IF EXISTS estadistica_ventas CASCADE;
DROP TABLE IF EXISTS estadistica_rotacion CASCADE;
DROP TABLE IF EXISTS estadistica_ventas_cliente_data CASCADE;

CREATE TABLE estadistica_rotacion
(
  delegacion integer NOT NULL,
  almacen integer NOT NULL,
  producto integer NOT NULL,
  orden double precision NOT NULL,
  minimo double precision,
  peso double precision,
  concepto character varying(100),
  clave_producto character varying(100),
  talla integer,
  color integer
)
WITH (
  OIDS=FALSE
);
ALTER TABLE estadistica_rotacion
  OWNER TO dynagent;

CREATE TABLE estadistica_ventas
(
  delegacion integer,
  almacen integer,
  producto integer,
  talla integer,
  color integer,
  clave_producto character varying(100),
  cantidad_venta_periodo double precision,
  periodo_dias integer,
  ratio_ventas double precision,
  desviacion_std double precision,
  lead double precision,
  tipo character varying(100),
  fecha_inicio bigint,
  fecha_fin bigint,
  fecha_fin_proyeccion bigint,
  regresion_grado_0_curr double precision,
  regresion_grado_1_curr double precision,
  peso_prev_year double precision,
  q_order double precision,
  stock_minimo double precision,
  peso double precision,
  concepto character varying(100)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE estadistica_ventas
  OWNER TO dynagent;

  
CREATE TABLE estadistica_ventas_cliente_data
(
	peso double precision,  
	entregado boolean,
	cliente integer,
	clienteidto integer,
	demand_dia double precision,
	producto integer,
	delegacion integer,
  	almacen integer,  
  	dia bigint,
  	lead double precision,
  	clave_producto character varying(100),
  	talla integer,
  	color integer
)
WITH (
  OIDS=FALSE
);
ALTER TABLE estadistica_ventas_cliente_data
  OWNER TO dynagent;

CREATE OR REPLACE FUNCTION ajusta_dia_a_inicio(fecha bigint)
  RETURNS bigint AS
$BODY$
BEGIN
	return fecha-date_part('hour'::text, to_timestamp(fecha::bigint))::bigint*3600-date_part('minute'::text, to_timestamp(fecha::bigint))::bigint*60-date_part('second'::text, to_timestamp(fecha::bigint))::bigint;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION ajusta_dia_a_inicio(bigint)
  OWNER TO dynagent;

 

-- DROP FUNCTION rotacion_estadistica_cliente(integer, integer, integer[], character varying);

CREATE OR REPLACE FUNCTION rotacion_estadistica_cliente(fecha_min integer, fecha_max integer, delegaciones integer[], segmentoparam character varying)
  RETURNS SETOF estadistica_ventas_cliente_data AS
$BODY$
DECLARE
    query character varying;
BEGIN
-- de momento no distingo por  pesos para no segmentar la demanda por almacen
return query 
select case when max(peso) is null then 0::double precision 
	   else max(peso) end as peso,
	   false as entregado,
	   cliente, 
	   clienteidto,  
	   sum(demand_dia) as demand_dia,
	   producto,
	   delegacion,
	   almacen, 
	   min(ajusta_dia_a_inicio(fecha::bigint)) as dia,
	   max(lead),
	   clave_producto,
	   max(talla) as talla,
	   max(color) as color

from(
        SELECT case when p.peso_prioridad is null then 0
        	else p.peso_prioridad end as peso , 
         	true AS entregado, 
            NULL::integer AS cliente, 
            NULL::integer AS clienteidto,
			linea.cantidad AS demand_dia,
            linea.producto, 
            doc."delegación" as delegacion,
            doc.origen AS almacen, 
			doc.fecha as fecha,
            art.dias_entrega_proveedor AS lead,
            linea.clave_producto, 
            linea.talla AS talla, linea.color AS color
        FROM 
           "v_línea_artículos" linea																									JOIN 
           "v_albarán_venta#línea_artículos" dlin 	ON 	dlin."línea_artículosId" = linea."tableId" AND 
           												dlin."línea_artículosIdto" = linea.idto		   									JOIN 
           "v_albarán_venta" doc 					ON dlin."albarán_ventaIdto" = doc.idto AND dlin."albarán_ventaId" = doc."tableId"  	JOIN 
           "género" art 							ON art."tableId" = linea.producto													JOIN
           unnest(delegaciones) as del				ON doc."delegación"=del																JOIN
           "almacén" alm ON doc.origen=alm."tableId"											    								LEFT JOIN 
            prioridad p ON p."tableId" = alm.prioridad_ventas
   		where 	(segmentoParam is null or art.campo_aux5=segmentoParam) and   		
   				doc.fecha BETWEEN fecha_min AND fecha_max    				
	UNION ALL
         SELECT peso, false AS entregado, doc.cliente, 
            doc."clienteIdto" AS clienteidto, linea.cantidad AS demand_dia, 
            linea.producto, doc."delegación" as delegacion, alm.almacen, 
            doc.fecha as fecha,
            art.dias_entrega_proveedor AS lead, linea.clave_producto, 
            linea.talla AS talla, linea.color AS color
           FROM 
           	"v_línea_artículos" linea																							     				JOIN 
           	"v_pedido_de_cliente#línea_artículos" dlin ON dlin."línea_artículosId" = linea."tableId" AND dlin."línea_artículosIdto" = linea.idto	JOIN 
           	v_pedido_comercial doc ON dlin."pedido_de_clienteIdto" = doc.idto AND dlin."pedido_de_clienteId" = doc."tableId"					 	JOIN
			estado as e on(e."tableId"=doc.estado)																						           	JOIN 
           	unnest(delegaciones) as del					ON doc."delegación"=del																		JOIN
           	"género" art ON art."tableId" = linea.producto																							JOIN 
           	( 	SELECT almp."delegación", almp.almacen,peso
    			FROM ( 	SELECT alm."tableId" AS almacen, alm."delegación", p.peso_prioridad as peso,
    					rank() OVER (PARTITION BY alm."delegación" ORDER BY   CASE	WHEN p.peso_prioridad IS NULL THEN 0::double precision
                     																ELSE p.peso_prioridad  END DESC) AS pos
            			FROM "almacén" alm       								LEFT JOIN 
            			prioridad p ON p."tableId" = alm.prioridad_ventas) almp
   						WHERE almp.pos = 1
   			) alm ON alm."delegación" = doc."delegación"
  			WHERE 	e.rdn<>'Anulado' and 
  					linea.albaranado = false and
  					(segmentoParam is null or art.campo_aux5=segmentoParam) and
  					doc.fecha BETWEEN fecha_min AND fecha_max    					  					
  	UNION ALL
  		SELECT 
  			0 as peso ,-- en produccion no se prioriza almacenes 
         	true AS entregado, 
            NULL::integer AS cliente, 
            NULL::integer AS clienteidto, 
			linea.cantidad AS demand_dia, 
            linea.producto, 
            alm."delegación" as delegacion,
            proIn."origenALMACÉN" AS almacen, 
			proIN.fecha_inicio as fecha,
            g.dias_entrega_proveedor AS lead,
            linea.clave_producto, 
            linea.talla AS talla, 
            linea.color AS color
            
  		FROM view_linea as linea 						inner join
  		"género" as g on(g."tableId"=linea.producto) 	inner join  		
  		"producción_material" as proIN on(proIN."tableId"=linea.proconsumo and proIn.es_despiece=false or proIN."tableId"=linea.prolinea and proIn.es_despiece=true) inner join
  		"almacén" as alm on(alm."tableId"=proIn."origenALMACÉN")		inner join
  		unnest(delegaciones) as del					ON alm."delegación"=del																		
  		WHERE 	(segmentoParam is null or segmentoParam=g.campo_aux5) and 
  				proIN.fecha_inicio BETWEEN fecha_min AND fecha_max      					   		  			
  ) as data
  GROUP BY cliente, clienteidto, delegacion, almacen, producto, clave_producto, date_part('year'::text, to_timestamp(fecha::double precision)),date_part('doy'::text, to_timestamp(fecha::double precision));
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION rotacion_estadistica_cliente(integer, integer, integer[], character varying)
  OWNER TO dynagent;


CREATE OR REPLACE FUNCTION regresion_lineal(datos double precision[], dia_inicio integer,periodo integer) RETURNS TABLE(a double precision, b double precision)
  AS
$BODY$
DECLARE
	decalado_inicio integer:=0;
	x integer:=0;	
	y double precision:=0;		
	
	xS integer:=0;
	yS double precision:=0;	
	
	xyS double precision:=0;	
	
	x2S integer:=0;
	y2S double precision:=0;
	
	denominador double precision:=0;
	
	coef_grado_1  double precision:=0;
	coef_grado_0  double precision:=0;
	
BEGIN
	
	FOREACH y IN ARRAY datos
  	LOOP		
		IF decalado_inicio<dia_inicio-1 THEN
			decalado_inicio:=decalado_inicio+1;
			CONTINUE;
		END IF;
		x:=x+1; -- doy siguiente valor antes para que al acabar x valga el ultimo valor utilizado o periodo		
    	xS := xS + x;
		yS := yS + y;
		
		x2S:=x2S+x*x;
		y2S:=y2S+y*y;
		
		xyS:=xyS+x*y;		
		IF x>=periodo THEN
			EXIT;
		END IF;
  	END LOOP;  
	
	denominador:=x*x2S-xS*xS;												
	
	IF denominador<>0 THEN
		coef_grado_1=(x*xyS-xS*yS)/denominador;
	END IF;				
					
	coef_grado_0:=(yS-coef_grado_1*xS)/x;
					
	return  query select coef_grado_0,coef_grado_1;
END
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION regresion_lineal(double precision[],integer,integer)
  OWNER TO dynagent;

CREATE OR REPLACE FUNCTION desviacion_std(datos double precision[], init_periodo integer,end_periodo integer) RETURNS double precision
  AS
$BODY$
DECLARE	
	media double precision:=0;	
	periodo integer:=end_periodo-init_periodo+1;	
	x integer:=0;
	desv_std double precision:=0;
	dato double precision:=0;	
BEGIN		
	FOR x IN init_periodo..end_periodo 
  	LOOP
		IF datos[x] is null THEN
			-- RAISE NOTICE 'NULO X % INI % END % PER % ',x,init_periodo,end_periodo,periodo;			
		ELSE
			media := media + datos[x];
		END IF;
  	END LOOP;
  	
	media:=media/periodo;  		
	
  	FOR x IN init_periodo..end_periodo 
  	LOOP
		IF datos[x] is null THEN
			-- RAISE NOTICE '2 INI % END % PER % ',init_periodo,end_periodo,periodo;
		ELSE
			desv_std := desv_std + (datos[x]-media)^2;
		END IF;
  	END LOOP;
	IF periodo=1 THEN
		desv_std:=sqrt(desv_std);	
	ELSE
		desv_std:=sqrt(desv_std/(periodo-1));	
	END IF;
  	
					
	return  desv_std;
END
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION desviacion_std(double precision[],integer,integer)
  OWNER TO dynagent;
  
CREATE OR REPLACE FUNCTION demanda_comparada(prioridad_minima integer,porc_per_fut double precision,fecha_min integer, fecha_max integer,delegaciones integer[])
-- porc_per_fut: proyectare datos en el futuro de periodo igual a un porcentaje del periodo input pasado. De 0 a 1, 1 100% que es duplicar el periodo
RETURNS TABLE(dia bigint,demandaCurr double precision,demandaPrev double precision,clave_producto varchar,producto integer,talla integer,color integer,delegacion integer,almacen integer,stock double precision,fecha_alta bigint)
    AS $$
select 	dia::bigint,
							sum(demandaCurr),
							sum(demandaPrev),
							pivotado.clave_producto,
							pivotado.producto,
							pivotado.talla,
							pivotado.color,
							pivotado.delegacion,
							pivotado.almacen,
							CASE 	WHEN sum(stocks.total) is null THEN 0 
									ELSE sum(stocks.total) END as stock,
							min(fecha_alta) as fecha_alta
FROM

(SELECT 
CASE WHEN year='CURR' THEN demand_dia
ELSE 0 END as demandaCurr,
CASE WHEN year='PREV' THEN demand_dia
ELSE 0 END as demandaPrev,
detalle.clave_producto,
detalle.producto,
detalle.talla,
detalle.color,
dia,
detalle.delegacion,
detalle.almacen

FROM
 (
	SELECT 'CURR' as year,demand_dia, dia,data.clave_producto,data.producto,data.talla,data.color,data.delegacion,data.almacen from rotacion_estadistica_cliente(fecha_min,fecha_max,delegaciones,null)  as data
	UNION ALL
	SELECT 'PREV' as year,demand_dia,EXTRACT(EPOCH FROM (to_timestamp(dia::bigint)+ interval '1 year' )), data.clave_producto,data.producto,data.talla,data.color,data.delegacion,data.almacen 
	from rotacion_estadistica_cliente(EXTRACT(EPOCH FROM (to_timestamp(fecha_min::bigint)- interval '1 year' ))::integer,EXTRACT(EPOCH FROM (to_timestamp(fecha_max+(fecha_max-fecha_min)*porc_per_fut::bigint)- interval '1 year' ))::integer,delegaciones,null) as data
	where porc_per_fut>0
	
) as detalle) as pivotado	LEFT JOIN
(
	select sum(cantidad) as total,s.clave_producto,alm."delegación" as delegacion,alm."tableId" as almacen,min(fecha_alta) as fecha_alta 
	FROM
		stock s																								inner join
		"almacén" as alm on(s."almacén_stock"=alm."tableId")												left join	
		prioridad p on(p."tableId"=alm.prioridad_ventas)													
	WHERE p.peso_prioridad>prioridad_minima
	GROUP BY alm."delegación",alm."tableId",s.clave_producto
) as stocks on (stocks.almacen=pivotado.almacen and stocks.clave_producto=pivotado.clave_producto)
GROUP BY pivotado.producto,pivotado.talla,pivotado.color,pivotado.clave_producto,pivotado.delegacion,pivotado.almacen,dia
ORDER BY pivotado.almacen,pivotado.clave_producto,dia asc;
$$
    LANGUAGE SQL
    IMMUTABLE
    RETURNS NULL ON NULL INPUT;
    

CREATE OR REPLACE FUNCTION demanda_comparada_estadistica(prioridad_minima integer,porc_per_fut double precision,productos integer[]) 
RETURNS  TABLE(dia bigint,demandaCurr double precision,demandaPrev double precision,clave_producto varchar,producto integer,talla integer,color integer,delegacion integer,almacen integer,stock double precision,fecha_alta bigint) 
    AS $$
DECLARE
    fini bigint;
    fend bigint;
    prod integer;
    del integer;
    del_list integer[];
BEGIN
    FOR fini,fend,del,prod IN SELECT fecha_inicio,fecha_fin,ev.delegacion,ev.producto FROM estadistica_ventas as ev inner join unnest(productos) as p on ev.producto=p
    LOOP
        del_list:=ARRAY[del];
        RETURN QUERY select * from demanda_comparada(prioridad_minima,porc_per_fut,fini::integer,fend::integer,del_list) as data where data.producto=prod; -- return current row of SELECT
    END LOOP;
    RETURN;
END
$$
LANGUAGE 'plpgsql' ;


CREATE OR REPLACE FUNCTION set_estadistica_ventas(fecha_min integer, fecha_max integer,delegaciones integer[],prioridad_minima int) RETURNS boolean
  AS
$BODY$
DECLARE
  	periodoDiasReales int:=0;
	tmpI int:=0;
	periodo_max_con_stock_i int:=0;	
	alta_stock_i int:=1;
	periodoFuturo int:=0;
	periodo_max_i int:=0;
	cantidad_dias_con_ventas int:=0;	
  	diaPos bigint:=0;
  	diaInicio bigint:=0;
	fecha_alta_stock bigint:=0;
  	diaMax_existente_ajustado bigint:=0;
  	diaMaxFuturoAjustado bigint:=0;
	
  	clave_productoPrev varchar;
	clave_producto varchar;
	
	producto integer:=0;
	productoPrev integer:=0;
	
	color integer:=0;
	colorPrev integer:=0;
	
	talla integer:=0;
	tallaPrev integer:=0;
	
	almacen integer:=0;
	almacenPrev integer:=0;
	
	delegacion integer:=0;
	delegacionPrev integer:=0;
	
	
	c double precision:=0;
	p double precision:=0;
	currList double precision ARRAY;
	prevList double precision ARRAY;
	stock double precision:=0;
	stockPrev double precision:=0;
	
	totalVentasCurr double precision:=0;
	totalVentasPrev double precision:=0;
	
	factor_crecimiento double precision:=1;
	
	tmp1 double precision:=0;
	tmp2 double precision:=0;
	
	inhibir_extrapolacion boolean:=true;
	
	existe_prevYear boolean:=false;
	existe_futuro boolean:=false;
	existe_currYear boolean:=false;
	ponderado_prev_year boolean:=false;
	cerradoProducto boolean:=false;
		
	DemPer double precision:=0; --demanda periodo
	RatioVentas double precision:=0; --demanda periodo
	
	integral double precision:=0;
	
	coef_grado_0_prev_year double precision:=0;
	coef_grado_0_curr_year double precision:=0;
	coef_grado_1_prev_year double precision:=0;
	coef_grado_1_curr_year double precision:=0;
	peso_prev_year double precision:=0;
	
	desviacion_std double precision:=0;
	desviacion_std_pasado double precision:=0;
	desviacion_std_futuro double precision:=0;
	
	nextDiaVenta bigint:=0;	
	denominador double precision:=0;
	numerador double precision:=0;
	porciento_ventas_extrapolacion double precision:=0.25;
	caso varchar;
	--proyectare datos en el futuro de periodo igual a un porcentaje del periodo input pasado. De 0 a 1, 1 100% que es duplicar el periodo
	porc_per_fut double precision:=1;--
	
	umbral_credibilidad_last_year double precision=0.5;
	i int:=0;
	prev_year_min int:=EXTRACT(EPOCH FROM (to_timestamp(fecha_min::bigint)- interval '1 year' ));
	-- fecha maxima suele ser la de hoy, en datos pasados duplicamos la ventana de tiempo añadiendo tiempos futuros (sumando a la fecha maxima el periodo), pero todo ello trasladado a un año antes
	prev_year_max int:=EXTRACT(EPOCH FROM (to_timestamp(fecha_max+(fecha_max-fecha_min)*porc_per_fut::bigint)- interval '1 year' ));
	

curVentas refcursor;

BEGIN
	
-- itero por clave_producto

-- comparar con decalacion 5 dias y queda la media
-- comienzo en el primer dia con resultados
i:=1;

diaMax_existente_ajustado:=ajusta_dia_a_inicio(fecha_max+86399)-10;-- sumo un dia menos un sg para quedarme con inicio dia siguiente que es el fin de hoy
diaMaxFuturoAjustado:=ajusta_dia_a_inicio(86399+fecha_max+(fecha_max-fecha_min)*porc_per_fut::bigint);

diaInicio:=ajusta_dia_a_inicio(fecha_min);
diaPos:=diaInicio;
nextDiaVenta:=0;

periodoDiasReales:=(diaMax_existente_ajustado-diaInicio)/86400 + 1;

select valor_numerico from parametro_numerico where rdn='porciento_umbral_extrapolacion_estadistica' INTO porciento_ventas_extrapolacion;
IF NOT FOUND THEN
	porciento_ventas_extrapolacion:=0.25;
	RAISE NOTICE 'NO CONFIGURADO PORCIENTO UMBRAL EXTRAPOLACION';
ELSE
	RAISE NOTICE 'CONFIGURADO PORCIENTO UMBRAL EXTRAPOLACION % ',porciento_ventas_extrapolacion;
END IF;

OPEN curVentas FOR select * from demanda_comparada(prioridad_minima,0.0,fecha_min, fecha_max,delegaciones);
-- la prioridad del almacen solo deberia tenerse en cuenta cuando una almacen tiene varios almacenes, en caso de tener uno es un problema no asignarla
-- porque es como considerar que siempre tiene stock cero

-- 3 casos:
-- Existen datos este año pero no existe datos del año pasado en fecha adelantada pasada => peso año pasado=0
-- No existe datos este año, pero si adelantados futuros=> peso año pasado=1
-- Correlacion similar, => peso en base a correlacion  
DELETE FROM estadistica_ventas;
LOOP
			
	FETCH curVentas INTO nextDiaVenta,c,p,clave_producto,producto,talla,color,delegacion,almacen,stock,fecha_alta_stock;		
	-- como la query solo devuelve ventas cuando se producen, los dias sin ventas no hay registros, asi que el fecth puede saltarse muchos dias llevando a un dia adelantado
	IF NOT FOUND THEN 
		clave_producto:='-';			
	END IF;			
	
	IF clave_productoPrev is null THEN
		clave_productoPrev:=clave_producto;
		almacenPrev:=almacen;
		stockPrev:=stock;
		tallaPrev:=talla;
		colorPrev:=color;		
		delegacionPrev:=delegacion;
	END IF;
	
	IF clave_producto<>clave_productoPrev OR almacenPrev<>almacen and almacenPrev<>0 THEN 			
		periodoFuturo:=periodoDiasReales*porc_per_fut;
		tmpI:=i;
		WHILE tmpI<=periodoDiasReales+periodoFuturo LOOP		
			currList[tmpI]:=0;
			prevList[tmpI]:=0;	
			tmpI:=tmpI+1;		
		END LOOP;
		i:=i-1;
	
		--RAISE NOTICE 'CAMBIO PRODUCTO i % next % clave % ',i,nextDiaVenta,clave_producto;	
		desviacion_std:=0;
		-- calcular D, periodo, dsv_std			
		-- CASO 1: prev year si tiene peso y hay datos año actual: D es proyeccion futura de año actual ponderada + valores previo años trasladados a futuro y ponderado			
		-- CASO 2: prev year no tiene peso, hallo D a partir de datos pasados más una proyeccion de tendencia para medio periodo futuro, con la integral
		-- CASO 3: prev year tiene peso y no existen datos de este año: el periodo total deberia ser solo en base a datos futuros y no hace falta proyeccion
		DemPer:=0;
		IF existe_currYear THEN	
			IF	porciento_ventas_extrapolacion<=0 OR cantidad_dias_con_ventas<periodoDiasReales*porciento_ventas_extrapolacion/100.0 THEN
				inhibir_extrapolacion:=true;
			ELSE
				inhibir_extrapolacion:=false;
			END IF;
			
			IF peso_prev_year<umbral_credibilidad_last_year OR not existe_futuro THEN
				peso_prev_year:=0;-- lo pongo a cero para no cuente
				caso:='2:CURR';
			ELSE
				caso:='1:CURR_PREV';
			END IF;		
			periodo_max_con_stock_i:=periodoDiasReales;
			-- CASO 1. Demanda es futuro mas proyeccion de pasado
			
			tmpI:=0;
			totalVentasCurr:=0;
			totalVentasPrev:=0;
			cantidad_dias_con_ventas:=0;
			LOOP
				tmpI:=tmpI+1;								
				IF currList[tmpI]>0.0 THEN
					cantidad_dias_con_ventas:= cantidad_dias_con_ventas+1;
				END IF;
				-- voy acumulando futuro en demanda, y despues faltara la proyeccion
				tmp1:=prevList[tmpI]*peso_prev_year;	
				totalVentasCurr:=totalVentasCurr+currList[tmpI];
				totalVentasPrev:=totalVentasPrev+prevList[tmpI];
					
				IF not inhibir_extrapolacion and stockPrev<=0 and currList[tmpI]<>0 and tmpI<=periodoDiasReales THEN -- solo contemplo recortar periodo por stock out en datos este año
					periodo_max_con_stock_i:=tmpI; -- asigna tantas veces como haya, pero al ser creciente quedara se agoto stock en ultima venta. Al asignar estoy recortando periodo
				END IF;					
						
				IF alta_stock_i>tmpI THEN				
					alta_stock_i:=tmpI;
				END IF;
				
				IF tmpI>periodoDiasReales THEN
					IF peso_prev_year=0 THEN
						EXIT;
					END IF;
					DemPer:=DemPer+tmp1; -- sumo el futuro, mas abajo añadire la proyeccion del pasado	
				END IF;
						
				IF tmpI>=i THEN 
					EXIT;
				END IF;
			END LOOP;
			
			IF totalVentasPrev>0 and totalVentasCurr>totalVentasPrev THEN
				factor_crecimiento:=totalVentasCurr/totalVentasPrev;
				DemPer:=DemPer*factor_crecimiento;
				totalVentasPrev:=totalVentasPrev*factor_crecimiento;
			END IF;

			-- la desviacion std se puede calcular ya independientemente de que se proyecte pasado en futuro
			desviacion_std_pasado:=desviacion_std(currList,alta_stock_i,periodo_max_con_stock_i);
							
			IF peso_prev_year>0 THEN
				desviacion_std_futuro:=desviacion_std(prevList,periodoDiasReales+1,i);
			END IF;
			desviacion_std:= desviacion_std_pasado*(1-peso_prev_year)+desviacion_std_futuro*peso_prev_year;										
			
			-- tenga o no peso prev year, la recta se haya en base a pasado para proyectar, el futuro no se proyecta, se presupone
			SELECT a,b INTO coef_grado_0_curr_year,coef_grado_1_curr_year FROM regresion_lineal(currList,alta_stock_i,periodo_max_con_stock_i);		
			
			-- integral futura es la intergal de todo periodo, sobre la curva estimada en el pasado, menos la integral del pasado sobre la misma curva (la recta)
			-- integral todo periodo			

			-- el periodo final nunca depende de tiempos con stock, porque la parte imputable a datos de este año se proyecta al periodo futuro siempre fijo con una recta
			-- y la parte imputable a año pasado supuesto en futuro, tambien es periodo fijo (no se tiene en cuenta si el año pasado se quedo sin stock)
			-- no puede calcularse en base a i porque quizas i no se extiende a futuro		
			-- el periodo futuro de referencia es relativo, solo se utilizara el ratio de ventas que para obtener cantidades absolutas se multiplicara realmente por lead o periodo a aprovisionar
						
			-- ahora resto pasado. EN CASO 2 no necesito sumar futuro porque solo utilizo la proyeccion. En caso 2 DemPer ya incluye sumatorio de datos futuros
			IF inhibir_extrapolacion THEN
				coef_grado_1_curr_year:=0;
			END IF;			
			-- normalizo c0 para que area curr coincida en base a regresion coincida con total ventas curr
			coef_grado_0_curr_year:=totalVentasCurr/periodo_max_con_stock_i-coef_grado_1_curr_year*periodo_max_con_stock_i/2;
			
			DemPer:=totalVentasPrev*peso_prev_year;
			
			
			DemPer:=DemPer+(1-peso_prev_year)*(2*coef_grado_0_curr_year +coef_grado_1_curr_year*(2*periodo_max_con_stock_i+periodoFuturo))*periodoFuturo/2;
							
			RatioVentas:=DemPer/periodoFuturo;
		ELSE
			IF existe_futuro THEN
				-- CASO 3, datos futuros me los traigo al actual (currList), y no proyecto
				caso:='3:PREV FUTURO';
				FOR tmpI IN periodoDiasReales+1..i
				LOOP
					tmp1:=prevList[tmpI];			
					IF tmp1 is null THEN
						tmp1:=0;
					END IF;
					currList[i]=tmp1;
					DemPer:=DemPer+tmp1;
				END LOOP;
				-- al final i almacena periodo futuro				
				periodoFuturo:=i-periodoDiasReales+1;
				RatioVentas:=DemPer/periodoFuturo;
				desviacion_std:=desviacion_std(prevList,periodoDiasReales+1,i);	
			END IF;										
		END IF;
		
		IF existe_currYear OR existe_futuro THEN
			INSERT INTO estadistica_ventas(delegacion,almacen,clave_producto,producto,talla,color,ratio_ventas,desviacion_std,cantidad_venta_periodo,periodo_dias,tipo,fecha_inicio,fecha_fin,regresion_grado_0_curr,regresion_grado_1_curr,peso_prev_year,fecha_fin_proyeccion) 
						VALUES(delegacionPrev,almacenPrev,clave_productoPrev,productoPrev,tallaPrev,colorPrev,RatioVentas,desviacion_std,DemPer,periodoFuturo,caso,fecha_min,fecha_max,coef_grado_0_curr_year,coef_grado_1_curr_year,peso_prev_year,diaMaxFuturoAjustado);
		END IF;

		IF clave_producto='-' THEN
			EXIT;
		END IF;
		-- RESET nuevo clave_producto
		i:=1;
		diaPos:=diaInicio;						
		existe_prevYear:=false;
		existe_currYear:=false;
		existe_futuro:=false;		
		ponderado_prev_year:=false;			
		periodo_max_con_stock_i:=0;
		cerradoProducto:=false;
		peso_prev_year:=0;
		alta_stock_i:=1;
		periodoFuturo:=0;
		factor_crecimiento:=1;
		coef_grado_0_curr_year:=null;
		coef_grado_1_curr_year:=null;
	END IF;-- fin cambio producto
	
	-- si al iterar, todavia en mismo producto, hay salto de dias, debo rellenar con ceros
	WHILE nextDiaVenta>diaPos LOOP

		-- no hay datos y se toma como cero, tambien debo añadirlo al array de datos
		currList[i]:=0;
		prevList[i]:=0;	
		i:=i+1;
		diaPos:=diaPos+86400;			 
	END LOOP;
		
	-- hallo i de alta stock si no he cambiado de producto
	IF fecha_alta_stock>diaInicio and alta_stock_i=1 THEN
		IF fecha_alta_stock<=diaMax_existente_ajustado THEN
			alta_stock_i:=(fecha_alta_stock-diaInicio)/86400 + 1;
		ELSE
			alta_stock_i:=1; -- por seguridad tomo como fecha alta el primer dia
		END IF;
	END IF;
		
	IF cerradoProducto  THEN
		diaPos:=diaPos+86400;	
		
		clave_productoPrev:=clave_producto;	
		productoPrev:=producto;
		tallaPrev:=talla;
		colorPrev:=color;
		almacenPrev:=almacen;
		delegacionPrev:=delegacion;
		stockPrev:=stock;
		IF clave_producto='-' THEN 
					EXIT;
			ELSE 	CONTINUE;
		END IF;
	END IF;		


	IF c is null THEN
		c:=0;
	END IF;
	IF p is null THEN
		p:=0;
	END IF;		
			
	currList[i]:=c;
	prevList[i]:=p;	

	IF diaPos< diaMax_existente_ajustado and c>0 THEN --existe datos futuros al menos 5 dias futuros despues
		existe_currYear=true;
	END IF;
	
	IF diaPos< diaMax_existente_ajustado and p>0 THEN --existe datos futuros al menos 5 dias futuros despues
		existe_prevYear=true;
	END IF;
	
	IF not ponderado_prev_year and diaPos>=diaMax_existente_ajustado+1 THEN -- primer dia futuro		
		-- DESVIACION STND INI PREVIO AÑO
		-- PONDERO CREDIBILIDAD DATOS PASADOS
		--RAISE NOTICE 'PONDERAR i % next % clave % ',i,nextDiaVenta,clave_producto;	
		ponderado_prev_year:=true;
		
		SELECT a,b INTO coef_grado_0_curr_year,coef_grado_1_curr_year FROM regresion_lineal(currList,alta_stock_i,i);
		SELECT a,b INTO coef_grado_0_prev_year,coef_grado_1_prev_year FROM regresion_lineal(prevList,1,i);
		
		IF not existe_currYear and existe_prevYear THEN
			peso_prev_year:=1;
		END IF;
		
		IF existe_currYear and existe_prevYear THEN						
			IF coef_grado_1_prev_year=coef_grado_1_curr_year THEN
				peso_prev_year:=1;
			ELSE						
				numerador:=coef_grado_1_curr_year;
				denominador:=coef_grado_1_prev_year;					
				IF coef_grado_1_prev_year=0 THEN
					numerador:=coef_grado_1_prev_year;
					denominador:=coef_grado_1_curr_year;
				END IF;			
				peso_prev_year:=numerador/denominador;					
				IF peso_prev_year<>0 and (peso_prev_year>1 or peso_prev_year<-1) THEN
					peso_prev_year:=1/peso_prev_year;
				END IF;					
				IF peso_prev_year<0 THEN
					peso_prev_year:=-peso_prev_year;
				END IF;
			END IF;												
		END IF;
		
		IF peso_prev_year<umbral_credibilidad_last_year THEN
			cerradoProducto=true; -- si quedan datos futuros, ya no se capturan mas datos hasta cambiar de clave_producto
		END IF;
	END IF;

	IF not cerradoProducto and diaPos>diaMax_existente_ajustado and p>0 THEN --existe datos futuros al menos 5 dias futuros despues
		existe_futuro=true;
	END IF;
	
	i:=i+1;
	diaPos:=diaPos+86400;	
	clave_productoPrev:=clave_producto;	
	productoPrev:=producto;
	tallaPrev:=talla;
	colorPrev:=color;
	delegacionPrev:=delegacion;
	almacenPrev:=almacen;
	stockPrev:=stock;
END LOOP;

return true;
END

$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION set_estadistica_ventas(integer, integer,integer[],int)
  OWNER TO dynagent;

CREATE OR REPLACE FUNCTION set_segmento_produccion() RETURNS boolean
  AS
$BODY$
BEGIN
  	UPDATE "género" set campo_aux5=null;
	UPDATE "género" set campo_aux5='MATERIA_PRIMA' FROM 
	"línea_materia" as in_li																									inner join					
	"escandallo" as produccIn	on(	in_li."escandalloIdCOMPONENTE"=produccIn."tableId" and produccIn.es_despiece=false or 
									in_li."escandalloIdSALIDA"=produccIn."tableId" and produccIn.es_despiece=true )							
																																left join 																	
	("línea_materia" as out_li						 				inner join
	"escandallo" as produccOut		on(	out_li."escandalloIdSALIDA"=produccOut."tableId" and produccOut.es_despiece=false or
												out_li."escandalloIdCOMPONENTE"=produccOut."tableId" and produccOut.es_despiece=true)							
											) on(out_li.producto=in_li.producto)
	where in_li.producto="género"."tableId" and produccOut.rdn is null;
	
	UPDATE "género" set campo_aux5='FABRICADO' FROM 		
	"línea_materia" as out_li																										inner join					
	"escandallo" as produccOut	on(	out_li."escandalloIdCOMPONENTE"=produccOut."tableId" and produccOut.es_despiece=true or 
											out_li."escandalloIdSALIDA"=produccOut."tableId" and produccOut.es_despiece=false )							
																																	left join 																	
	("línea_materia" as in_li						 				inner join
	"escandallo" as produccIn		on(	in_li."escandalloIdSALIDA"=produccIn."tableId" and produccIn.es_despiece=true or
												in_li."escandalloIdCOMPONENTE"=produccIn."tableId" and produccIn.es_despiece=false)							
											) on(in_li.producto=out_li.producto)

	where out_li.producto="género"."tableId" and produccIn.rdn is null;
	return true;
END 
$BODY$
  LANGUAGE plpgsql VOLATILE;
ALTER FUNCTION set_segmento_produccion()
  OWNER TO dynagent;
  

-- Function: rotacion_reorden(double precision, double precision, double precision, double precision, double precision, integer[], character varying, integer[], character varying)

-- DROP FUNCTION rotacion_reorden(double precision, double precision, double precision, double precision, double precision, integer[], character varying, integer[], character varying);

CREATE OR REPLACE FUNCTION rotacion_reorden(dias_reposicion double precision, periodo_dias_ventas double precision, co double precision, ch double precision, slm double precision, delegaciones integer[], segmentoparam character varying, pedido_cliente_especifico integer[], accion character varying)
  RETURNS SETOF estadistica_rotacion AS
$BODY$
DECLARE
    r estadistica_rotacion%rowtype;    
    query character varying;
BEGIN
-- las ventas entregadas y no entragadas, se tratan igual de cara a la estadistica, salvo que la no entregada tiene mayor peso y se diferencia, en realidad al agrupar por cliente
-- ya se estaba separando. Sin ambargo el periodo de analisis de estadistica no tiene que ver con el periodo a reponer, ni con todo lo reservado no entregado que se descuenta de stock disponible

IF pedido_cliente_especifico is not null THEN
		
		return query 
			SELECT 	doc."delegación" as delegacion, 
					almacen,
					linea.producto,
					sum(linea.cantidad) as q,
					0::double precision as reorden,-- punto de reorden que devuevlo como campo minimo
					1::double precision as peso,
					('pedido '||doc.rdn)::character varying(100) as concepto,
					linea.clave_producto,
					max(linea.talla) as talla,
					max(linea.color) as color
           FROM 
           	"v_línea_artículos" linea														JOIN 
           	"v_pedido_de_cliente#línea_artículos" dlin ON dlin."línea_artículosId" = linea."tableId" AND dlin."línea_artículosIdto" = linea.idto	JOIN 			
           	v_pedido_comercial doc ON dlin."pedido_de_clienteIdto" = doc.idto AND dlin."pedido_de_clienteId" = doc."tableId"			join
			estado as e on(e."tableId"=doc.estado)												LEFT JOIN
           	unnest(delegaciones) as del					ON doc."delegación"=del	 				JOIN 
           	unnest(pedido_cliente_especifico) as pe					ON doc."tableId"=pe			JOIN
           	"género" art ON art."tableId" = linea.producto										JOIN
           	( 	SELECT almp."delegación", almp.almacen,peso
    			FROM ( 	SELECT alm."tableId" AS almacen, alm."delegación", p.peso_prioridad as peso,
    					rank() OVER (PARTITION BY alm."delegación" ORDER BY   CASE	WHEN p.peso_prioridad IS NULL THEN 0::double precision
                     																ELSE p.peso_prioridad  END DESC) AS pos
            			FROM "almacén" alm       								LEFT JOIN 
            			prioridad p ON p."tableId" = alm.prioridad_ventas) almp
   						WHERE almp.pos = 1
   			) alm ON alm."delegación" = doc."delegación"
  			-- puede estar anulado para no contar disponibilidad WHERE  not e.rdn='Anulado'
  			GROUP BY doc."delegación",almacen,doc.rdn,linea.producto,linea.clave_producto;
ELSE	  
	UPDATE estadistica_ventas as  ev set  q_order=q,stock_minimo=reorden,peso=order_estat.peso,concepto=order_estat.concepto 
	FROM 
	(SELECT 
	delegacion,
	almacen,
	producto,
	CASE WHEN dias_reposicion is null and cantidad_venta_periodo is not null and cantidad_venta_periodo>0 THEN sqrt(2*co*ratio_ventas*periodo_dias_ventas/ch) 
	 	WHEN dias_reposicion is not null THEN ratio_ventas*dias_reposicion
	    ELSE 0::double precision END as q,
	CASE WHEN  cantidad_venta_periodo is null or cantidad_venta_periodo<=0 THEN stock_min
		WHEN desviacion_std is null or desviacion_std=0 THEN ratio_ventas*lead+stock_min
		WHEN loss_inversa(sqrt(2*co*ratio_ventas*periodo_dias_ventas/ch)*(1-slm)/desviacion_std)*desviacion_std+ratio_ventas*lead<stock_min THEN stock_min
		ELSE loss_inversa(sqrt(2*co*ratio_ventas*periodo_dias_ventas/ch)*(1-slm)/desviacion_std)*desviacion_std+ratio_ventas*lead END as reorden,	    
	CASE WHEN periodo_dias IS NULL or periodo_dias=0 or ratio_ventas<0 THEN 0::double precision
		 ELSE ratio_ventas END as peso,
	'Prevision'::character varying(100) as concepto,
	clave_producto,
	talla,
	color	FROM(

		SELECT
		delegacion,
		almacen,
		g."tableId" as producto,
		cantidad_venta_periodo,
		desviacion_std,
		periodo_dias,
		ratio_ventas,
		case  	WHEN accion='Provision_demanda_futura' and g.dias_entrega_proveedor is not null then g.dias_entrega_proveedor
				WHEN accion='Provision_demanda_futura' and valor_lr is not null then valor_lr
				WHEN dias_reposicion is not null then dias_reposicion 
				WHEN dias_reposicion is null and g.dias_entrega_proveedor is not null  THEN g.dias_entrega_proveedor
				WHEN dias_reposicion is null and valor_lr is not null and g.dias_entrega_proveedor is null THEN valor_lr
				else 5 END as lead,
		clave_producto,
		talla,
		color,
		"mínimo" as stock_min
				 
	    from 	estadistica_ventas as ev inner join
	    		"género" as g on(ev.producto=g."tableId") inner join
	    		unnest(delegaciones) as del ON ev.delegacion=del left join
	    		(select valor_numerico as valor_lr from parametro_numerico where rdn='STOCK_dias_entrega_proveedor') as leadrepo on(valor_lr is not null) inner join				
				"límite_stock" as lim on(	(lim."almacén" is null or lim."almacén"=almacen) and 
											(lim.producto is null or lim.producto=g."tableId"))	
	    WHERE (descatalogado is null or not descatalogado) and segmentoParam is null or g.campo_aux5=segmentoParam) as data
	    ) as order_estat
	    WHERE order_estat.almacen=ev.almacen and order_estat.clave_producto=ev.clave_producto and (order_estat.talla is null or order_estat.talla=ev.talla) and (order_estat.color is null or order_estat.color=ev.color);
	    
	    return query SELECT delegacion,almacen,producto,q_order as q,stock_minimo as reorden,peso,concepto,clave_producto,talla,color from estadistica_ventas;
END IF;
	    
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION rotacion_reorden(double precision, double precision, double precision, double precision, double precision, integer[], character varying, integer[], character varying)
  OWNER TO dynagent;


-- Function: rotacion_reserva(integer, integer)

-- DROP FUNCTION rotacion_reserva(integer, integer);
-- Function: rotacion_reserva(integer, integer, integer[], boolean, boolean, boolean)

-- DROP FUNCTION rotacion_reserva(integer, integer, integer[], boolean, boolean, boolean);

CREATE OR REPLACE FUNCTION rotacion_reserva(fecha_min integer, fecha_max integer, delegaciones integer[], entregas_parciales_reservan boolean, ignorar_reserva_clientes boolean, entradas_no_suman boolean)
  RETURNS SETOF estadistica_rotacion AS
$BODY$
DECLARE
    r estadistica_rotacion%rowtype;    
    query character varying;
BEGIN

return query 

	-- no tiro de stock reservado de articulo, porque no distinguiría por delegacion
	
	select "delegación",almacen,producto,sum(cantidad) as cantidad,0::double precision as minimo,1::double precision,'reserva'::character varying(100),clave_producto,max(talla),max(color) from (
		select doc."delegación",
			alm.almacen as almacen,
			producto, 
			talla,
			color,
			clave_producto,
			cantidad
		from 
			-- TODO vista antigua v_linea_articulos no tiene campo reservado, por eso no se ha utilizado
			"v_línea_artículos" as linea																														inner join
			"v_pedido_de_cliente#línea_artículos" as dlin	on(dlin."línea_artículosId"=linea."tableId" and dlin."línea_artículosIdto"=linea.idto)				inner join
			v_pedido_comercial as doc		on(	dlin."pedido_de_clienteIdto" = doc.idto and dlin."pedido_de_clienteId" = doc."tableId" )			inner join		
			unnest(delegaciones) as del 	on(doc."delegación"=del)																				inner join
			estado as e on(e."tableId"=doc.estado)																inner join			
			"género" as art					on(art."tableId"=linea.producto)																					inner join
			
			(	select "delegación",almacen from(
					select 	alm."tableId" as almacen,"delegación", rank() OVER (PARTITION BY "delegación" ORDER BY case when peso_prioridad is null then 0 else  peso_prioridad end DESC) as pos
					from 
					"almacén" alm 	LEFT JOIN
					prioridad p on(p."tableId"=alm.prioridad_ventas)) as almp
				where pos=1
			) as alm on(alm."delegación"=doc."delegación")
						
		where 	(ignorar_reserva_clientes is null or ignorar_reserva_clientes=false) and e.rdn<>'Anulado' and linea.albaranado=false 
				-- comentar para no discriminar por fecha para no repetir, para no contar debe anularse en cuyo caso ya no reserva: 
				-- and (fecha_min is null or doc.fecha>=fecha_min) and (fecha_max is null or doc.fecha<=fecha_max) 
	
		union all
	
		select  alm."delegación" as "delegación",
				alm."tableId" as almacen,
				producto, 
				talla,
				color,
				clave_producto,
				case when  origen=alm."tableId" then linea.cantidad -- cantidad reservada se refiere
					 else -linea.cantidad end  as cantidad
		from 
	
		"línea_materia" as linea																		inner join
		"v_solicitud" as ped			on(	linea."pedido_traspaso_almacenesId" =ped."tableId")			left join 
		traspaso_almacenes as ta		on(ta."tableId"=linea."traspaso_almacenesId")					inner join
		-- si tiene destino posterior no computo stock en destino, solo en el posterior
		"almacén" as alm 					on(origen=alm."tableId" and "origenIdto"=3 or destino_posterior is null and destino=alm."tableId" and "destinoIdto"=3 or destino_posterior=alm."tableId" and "destino_posteriorIdto"=3)			inner join										
		unnest(delegaciones) as del 		on(alm."delegación"=del)									inner join
		"género" as art					on(art."tableId"=linea.producto)
		
		where linea.reservado=true and 
			-- comentar para no discriminar por fecha para no repetir, para no contar debe anularse en cuyo caso ya no reserva 
			--(fecha_min is null or ped.fecha>=fecha_min) and (fecha_max is null or ped.fecha<=fecha_max) and
			not (entradas_no_suman=true and  destino=alm."tableId") and -- si las entradas no suman, no contemplo este almacen sea destino
			not( "traspaso_almacenesId" is not null and (origen=alm."tableId" or destino=alm."tableId" and (ta.stock_requiere_recepcion_en_destino=false or ta.recibido=true)))
		-- not (not entregas_parciales_reservan and ped.servido is not null and ped.servido) and "traspaso_almacenesId" is null and 
								
		union all
		
		SELECT 
 			alm."delegación",
			alm."tableId" as almacen,
			lin.producto, 
			talla,
			color,
			clave_producto,
			cantidad
            
  		FROM view_linea as lin 						inner join
  		"género" as g on(g."tableId"=lin.producto) 	inner join  		
  		"producción_material" as proIN on(proIN."tableId"=lin.proconsumo and proIn.es_despiece=false or proIN."tableId"=lin.prolinea and proIn.es_despiece=true) inner join
  		"almacén" as alm on(alm."tableId"=proIn."origenALMACÉN")		inner join
  		unnest(delegaciones) as del					ON alm."delegación"=del																		
  		WHERE (fecha_min is null or fecha_inicio>=fecha_min) and (fecha_max is null or fecha_inicio<=fecha_max)
								
		) as reserva_data
		
		group by "delegación",almacen, producto,clave_producto;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION rotacion_reserva(integer, integer, integer[], boolean, boolean, boolean)
  OWNER TO dynagent;

  
CREATE OR REPLACE FUNCTION rotacion_oferta(fecha_min integer, fecha_max integer,prioridad_min integer, entradas_no_suman boolean,delegaciones integer[],entregas_parciales_reservan boolean, ignorar_reserva_clientes boolean, ignorar_reservas boolean)
  RETURNS SETOF estadistica_rotacion AS
$BODY$
DECLARE
    r estadistica_rotacion%rowtype;    
    query character varying;
BEGIN

return query 


select 	"delegación",almacen,producto,
		sum(cantidad),
		max(minimo),
		case 	when max(peso_almacen) is null then 0::double precision
				when max(peso_almacen) is not null and max(maximo)<max(ocupado) then max(peso_almacen)+1
	 			else max(peso_almacen) end as peso,
	 				
		'oferta'::character varying(100) as concepto,
	 	st.clave_producto as clave_producto,
	 	max(st.talla),max(st.color) from
(
	select 
		"delegación",almacen,producto,clave_producto,cantidad, minimo,maximo, ocupado,talla,color, peso_almacen, rank() OVER (PARTITION BY  almacen,producto ORDER BY prioridad desc) as pos from(
		select "delegación","almacén_stock" as almacen,s.producto, p.peso_prioridad as peso_almacen,s.clave_producto,
			CASE	WHEN sum(reservado.orden) is null  THEN sum(s.cantidad)
					ELSE sum(s.cantidad)-sum(reservado.orden) END as cantidad,
    		max("mínimo") as minimo,
    		max("máximo") as maximo,
    		max(totales.total) as ocupado,
    		max(s.talla) as talla,max(s.color) as color,
			case --notar que el inner join con limite no es left, es porque admite almacen de limite nulo, eso si, es necesario un limite al menos por defecto
				 -- puede haber ambiguedad o mas de un limite que cumpla, pero despues, la PARTITION de arriba se quedara con el limite mas especifico 
				when lim."almacén" is not null and lim.producto is not null then 3
				when lim."almacén" is null and lim.producto is not null  then 2
				when lim."almacén" is not null and lim.producto is null  then 1
				else 0 end as prioridad

		from 	
			stock s																								inner join
			"almacén" as alm on(s."almacén_stock"=alm."tableId")												inner join
			unnest(delegaciones) as del ON alm."delegación"=del													inner join
			"límite_stock" as lim on(	(lim."almacén" is null or lim."almacén"=alm."tableId") and 
										(lim.producto is null or lim.producto=s.producto))						left join
			(	select "almacén_stock" as id,sum(cantidad) as total
				from "stock" 
				group by "almacén_stock"
			) as totales 			on(totales.id=alm."tableId")												LEFT JOIN
			prioridad p on(p."tableId"=alm.prioridad_ventas)													left join	

			(select * from rotacion_reserva(null,null,delegaciones,entregas_parciales_reservan,ignorar_reserva_clientes,entradas_no_suman) where ignorar_reservas is null or ignorar_reservas=false) as reservado	on(	reservado.almacen=s."almacén_stock" and reservado.clave_producto=s.clave_producto)
		
		where
		-- mayor prioridad ventas es ofertar menos a otros porque vendo yo, en particular no oferto novedades. Pero prioridad ventas 0 es ni siquiera oferto 
		-- al ofertar (parametro prioridad min no nulo, frente al reducir la demanda que si llega con nulo), descarto almacenes de prioridad minima. Ademas al ofertar, descarto novedades salvo tengan prioridad ventas menor 5
		-- Al procesar la demanda, cuando hay novedades, puede haber habido ventas que si aumentan demanda y no se descarta, por tanto no puedo descartar su propia oferta
				not (prioridad_min is not null  and s.fecha_alta>fecha_min and peso_prioridad>=5) and (prioridad_min is null or p.peso_prioridad is null or p.peso_prioridad>prioridad_min)	
		group by "delegación",lim."almacén", "almacén_stock",s.producto,lim.producto,s.clave_producto,p.peso_prioridad
		) as data
) as st 		
where pos=1 
group by st."delegación",st.almacen,st.producto,  st.clave_producto;

-- ventas media diaria por cada 1000 productos de capacidad 
--	(select round((sum(cantidad_total)/max(capacidad)/((fecha_max-fecha_min)/86400.0))::numeric,1),almacen
--		from 
--		"v_albarán_venta" as t 			 inner join
-- 		"almacén" as a on(a."tableId"="origen") 	 inner join 
-- 		(select sum(cantidad) as capacidad,s."almacén_stock" as almacen from stock as s  group by s."almacén_stock") as sg on(sg.almacen=a."tableId")
--		where t.fecha>=fecha_min group by a.rdn order by sum(cantidad_total)/max(capacidad) desc;
--		where pos=1 
--	) as media

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION rotacion_oferta(integer, integer, integer,boolean,integer[],boolean,boolean,boolean)
  OWNER TO dynagent;
  

 
CREATE OR REPLACE FUNCTION rotacion_procesado(observaciones character varying, dias_reposicion double precision,co double precision, ch double precision, slm double precision, fecha_min integer, fecha_max integer,origen integer[],destinoList integer[],entregas_parciales_reservan boolean, accion character varying, segmento character varying, init_produccion boolean, init_historico boolean,pedido_cliente_especifico integer[])
  RETURNS TABLE(msg character varying,id int) AS
$BODY$
DECLARE
-- slm service level expresado en porcentaje, co coste por ordenar, ch coste de almacenamiento una unidad durante un año
-- REQUISITOS: definir limite stock para todos los productos (por ej un limite generico y otros mas especificos), asi como definir una prioridad en al menos un almacen de 
-- la delegacion que atiende pedidos de cliente
-- Prioridad perdidas de almacen = minima => ese almacen no oferta productos

-- TODO: no se distingue lead time de aprovisionamiento respecto a traspasos, y es configurable con parametro procesdo STOCK_periodo_reposicion
-- no tiene en cuenta productos de temporada 
-- esta incrustado que prioridad de venta 5 o mas (configurado en almacen), debe tener en cuenta su propia demanda a la hora de ofertar, y prioridad menor oferta sin tener en cuenta el punto de reorden o stock minimo
-- seria necesario que ofertaCur devolviera tanto ese dato para prioridad alta, como diretcamente oferta.orden-oferta.minimo para oferta maxima, y modificar bucle para tomar decisiones al vuelo contrastando prioridades
-- de almacen demanda con oferta: se comienza ofertando con mimima prioridad, y si el almacen que demanda tiene prioridad superior, se oferta todo (sin minimos), pero si tiene prioridad igual o inferior, te oferto
-- con minimos. Si en un bucle sobra oferta, al siguiente bucle puede haberse reducido la prioridad de la demanda, en cuyo caso ya podria tener en cuenta minimos

-- las horas deben ser gmt, porque el codigo para comparar las traslada al inicio del dia 
	demandaRow estadistica_rotacion%rowtype;    
	ofertaRow estadistica_rotacion%rowtype;   
	
	ofertaAlmacen integer;
	
	cantidadRot double precision;
	num_linea integer:=0;
	c_total double precision:=0;
	c_total_viable double precision:=0;
	c_demanda double precision:=0;
	id_rotacion integer;
	
	c_resto_oferta double precision:=0;
	c_provisto_demanda double precision:=0;
	
	umbral_redondeo double precision:=0;
	maximoV double precision:=0;
	ocupadoV double precision:=0;
	peso double precision:=0;
	ignorar_reservas boolean:=false;

	producto integer;
	delegacion integer;
	almacen integer:=0;
	concepto character varying;
	mensaje_text varchar:=null;
	
	crearLinea boolean:=true;
	existeMaximo boolean:=false;
	periodo_dias_ventas int=365;--Debe ser coherente con periodo calculo costes que es anual, y al multiplicarlo por el ratio de ventas diario da la media vendida en el periodo de estimacion de la demanda
	    
	ofertaCur 	CURSOR FOR select 	oferta.delegacion,oferta.almacen,oferta.producto,
									case when oferta.peso<5 or ignorar_reservas or demanda.orden is null then oferta.orden-oferta.minimo
										 when oferta.peso>=5 and demanda.orden is not null and not	ignorar_reservas  and
											  oferta.minimo>=demanda.minimo 												then (oferta.orden-oferta.minimo)-demanda.orden
										 when oferta.peso>=5 and demanda.orden is not null and not	ignorar_reservas  and
											  oferta.minimo<demanda.minimo 													then (oferta.orden-demanda.minimo)-demanda.orden										 										 
										 else 0 end as orden,
									oferta.minimo,
									oferta.peso,oferta.concepto,oferta.clave_producto,oferta.talla,oferta.color
				FROM
					rotacion_oferta(fecha_min,fecha_max,0,true,origen,entregas_parciales_reservan,false,ignorar_reservas) as oferta 																									left join
					(	select max(tmp.delegacion) as delegacion,tmp.almacen,max(tmp.producto) as producto,sum(tmp.orden) as orden,max(minimo) as minimo,max(tmp.peso),'NA',tmp.clave_producto,max(tmp.talla) as talla,max(tmp.color) as color
						from rotacion_reorden(dias_reposicion,periodo_dias_ventas,co,ch,slm,origen,segmento,null,accion) as tmp inner join
						"almacén" as origen on(origen."tableId"=tmp.almacen )			inner join
						prioridad p on(p."tableId"=origen.prioridad_ventas)
						where not (accion='Provision_demanda_futura' or accion='Reposicion_delegacion_optima' or ignorar_reservas)-- en reposicion no cuentan sus necesidades (supone que el origen tiene menor prioridad que los destinos)
						group by tmp.clave_producto, tmp.almacen) as demanda 	on(oferta.almacen=demanda.almacen and oferta.clave_producto=demanda.clave_producto)
				WHERE (case when oferta.peso<5 or ignorar_reservas or demanda.orden is null then oferta.orden-oferta.minimo
										 when oferta.peso>=5 and demanda.orden is not null and not	ignorar_reservas  and
											  oferta.minimo>=demanda.minimo 												then (oferta.orden-oferta.minimo)-demanda.orden
										 when oferta.peso>=5 and demanda.orden is not null and not	ignorar_reservas  and
											  oferta.minimo<demanda.minimo 													then (oferta.orden-demanda.minimo)-demanda.orden										 										 
										 else 0 end)>0
				order by clave_producto,peso desc,demanda.minimo asc, delegacion,almacen;
				
	curRot CURSOR FOR select * from linea_rotacion as rot where "rotacionId"=id_rotacion and peso_prioridad>0 and rot.destino=almacen ORDER BY peso_prioridad ASC FOR UPDATE;
	curLimite refcursor;
BEGIN

SELECT alm.rdn INTO  mensaje_text
FROM 
"delegación" as del 											inner join
unnest(destinoList) as lista on(lista=del."tableId")			inner join
"almacén" as alm on(del."tableId"=alm."delegación")				inner join
prioridad p on(p."tableId"=alm.prioridad_ventas)				left join
"límite_stock" as lim on(lim."almacén"=alm."tableId")

where peso_prioridad>0 and lim.rdn is null;
IF  mensaje_text is not null THEN
	return query select ('el almacen '||mensaje_text || ' no tiene limite stock definido')::varchar,0;
	return;
END IF;

IF segmento is not null THEN
	-- Si utilizo segmentos, cualquier segmento procesado previamente debe haber generado ordenes a partir de rotaciones. 
	-- como el siguiente paso sera generar ordenes, previamente debo limpiar rotaciones ya pasadas a ordenes para no duplicar ordenes
	-- En realidad si proceso segmento materia prima, al generar ordenes, que son pedidos a proveedor, si no hay precio para dicho producto queda pendiente la orden, que no debe eliminars
	-- pero este paso es el ultimo
	delete from linea_rotacion;
	delete from rotacion;
END IF;
-- SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;
IF init_produccion THEN
	PERFORM set_segmento_produccion();
END IF;
IF init_historico THEN
	-- set prev_year incluye set crecimeinto
	PERFORM set_estadistica_ventas(fecha_min, fecha_max,destinoList,0);
END IF;

IF pedido_cliente_especifico is not null THEN	
	ignorar_reservas:=true;
END IF;
--
INSERT INTO rotacion(observaciones,r_destination) values(observaciones,'-') returning "tableId" INTO id_rotacion;
-- RAISE NOTICE 'CREADA ROTACION % ',id_rotacion;
UPDATE rotacion set rdn=id_rotacion::varchar where "tableId"=id_rotacion;

SELECT valor_numerico  from parametro_numerico where rdn='STOCK_umbral_decimales_redondeo_en_demanda'  INTO umbral_redondeo;

RAISE NOTICE 'UMBRAPREL %',umbral_redondeo;

IF umbral_redondeo is null then 
	umbral_redondeo:=0.0;
END IF;

IF umbral_redondeo>=1 then 
	umbral_redondeo:=0.5;
END IF;

IF umbral_redondeo<0 then 
	umbral_redondeo:=0.0;
END IF;


RAISE NOTICE 'UMBRAL %',umbral_redondeo;

OPEN ofertaCur;

-- RAISE NOTICE 'PREVIO co % ch % slm % fmin % fmx %',co,ch,slm,fecha_min,fecha_max;
FOR demandaRow IN 	select 	demanda.delegacion,demanda.almacen,demanda.producto,
							case when oferta.orden is null and not ignorar_reservas then demanda.orden+demanda.minimo
								 when ignorar_reservas then demanda.orden
								 when oferta.orden is not null and oferta.orden BETWEEN 0 and demanda.minimo then demanda.orden+(demanda.minimo-oferta.orden)
								 when oferta.orden is not null and oferta.orden<0 then demanda.orden+demanda.minimo -- posibl el stock esta mal, si se envia prenda pueden devolverla y haber rebotes
								 when oferta.orden is not null and oferta.orden BETWEEN demanda.minimo and (demanda.minimo+demanda.orden) then demanda.orden-(oferta.orden-demanda.minimo)
								 when oferta.orden is not null and oferta.orden>demanda.minimo+demanda.orden then 0
								 else 0 end as orden,-- la orden mas lo que nos hemos pasado del punto de reorden
							demanda.minimo,
							demanda.peso,
							demanda.concepto,demanda.clave_producto,demanda.talla,demanda.color
					FROM 
						rotacion_reorden(dias_reposicion,periodo_dias_ventas,co,ch,slm,destinoList,segmento,pedido_cliente_especifico,accion) as demanda  left join
						rotacion_oferta(fecha_min,fecha_max,null,false,destinoList,entregas_parciales_reservan,true,ignorar_reservas) as oferta on(oferta.almacen=demanda.almacen and oferta.clave_producto=demanda.clave_producto)
					WHERE (case when oferta.orden is null and not ignorar_reservas then demanda.orden+demanda.minimo
								 when ignorar_reservas then demanda.orden
								 when oferta.orden is not null and oferta.orden BETWEEN 0 and demanda.minimo then demanda.orden+(demanda.minimo-oferta.orden)
								 when oferta.orden is not null and oferta.orden<0 then demanda.orden+demanda.minimo
								 when oferta.orden is not null and oferta.orden BETWEEN demanda.minimo and (demanda.minimo+demanda.orden) then demanda.orden-(oferta.orden-demanda.minimo)
								 when oferta.orden is not null and oferta.orden>demanda.minimo+demanda.orden then 0
								 else 0 end)>0
					order by clave_producto, peso desc, demanda.minimo asc, delegacion,almacen
LOOP
RAISE NOTICE 'INI dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
c_provisto_demanda:=0;
	<<loopfeed>>
	LOOP -- loopfeed es para la misma demanda, si la ultima oferta fue insuficiente, vuelvo a buscar una nueva oferta
		RAISE NOTICE 'DEM1 dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;				

		CONTINUE WHEN demandaRow.clave_producto is null or demandaRow.almacen is null;		
		
		cantidadRot:=0;
		
		IF accion='Provision_demanda_futura' or accion='Depreciacion_stock' THEN 
			crearLinea:=false;
		ELSE
			crearLinea:=true;	
		END IF;
		
		IF almacen<>demandaRow.almacen THEN
			almacen:=demandaRow.almacen;
		END IF;
		
		-- si sobra oferta no busco nueva oferta, salvo que estemos en una nueva demanda que casualmente sea de mismo producto y almacen,lo cual no tiene sentido, en ese caso busca una nueva oferta
		IF ofertaRow.clave_producto=demandaRow.clave_producto and demandaRow.almacen=ofertaRow.almacen or c_resto_oferta=0 or ofertaRow.clave_producto<demandaRow.clave_producto THEN
			LOOP
				FETCH ofertaCur INTO ofertaRow;
				IF NOT FOUND  THEN
					-- Not found es que se ha llegado al final, siendo todavia la clave menor que la oferta en terminos de ordenacion, es decir, toda la oferta recorrida sin found no casa con la demanda que queda pos satisfacer
					RAISE NOTICE 'NOT FOUND dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
					c_resto_oferta:=0;
					IF accion='Provision_demanda_futura' THEN 
						EXIT;
					ELSE
						EXIT "loopfeed"; --saldra del loop feed hacia el loop padre, que es loop de la demanda
					END IF;
				END IF;

				CONTINUE WHEN ofertaRow.clave_producto is null or ofertaRow.almacen is null or ofertaRow.orden<=0;
				RAISE NOTICE 'FEED1 dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
				
				c_resto_oferta:=ofertaRow.orden;	
				
				IF ofertaRow.clave_producto>demandaRow.clave_producto  THEN		
					-- al ir iterando por la oferta, ya se pasa en orden, es imposible encontrar ya oferta para esta demanda, paramos aqui la iteracion para hacer un step mas en interacion de la demanda, a ver si casa
					-- no reseteamos todavia c_resto_oferta ya que contiene el valor de la oferta actual que podria casar con la nueva demanda a iterar 
					RAISE NOTICE 'NOOFER dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
					-- Hacemos solo exit, y parara en siguiente IF NO OFERTA por tener claves diferentes
					EXIT;
					--EXIT "loopfeed";
				END IF;
				-- no puedo rotar mismo origen que destino, tampoco tiene sentido si este almacen tiene demanda para esta clave, tenga tambien oferta, por lo que puede saltarse el registro sabiendo que
				-- no sera leido de nuevo por ningun destino debido a que se lee siempre forward
				-- si es mismo almacen y clave continuo saltando este registro de oferta
				IF demandaRow.almacen<>ofertaRow.almacen and demandaRow.clave_producto=ofertaRow.clave_producto THEN	
					RAISE NOTICE 'FOUND dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
					-- si cambia la clave igualmente se saldra en siguiente linea a siguiente demanda, pero ya resto oferta estara inicializada
					EXIT;
				END IF;
				-- oferta sin demanda
				IF accion='Depreciacion_stock' THEN 
					EXIT;
				END IF;		
			END LOOP;		
		END IF;

		-- IF NO OFERTA
		-- aqui resto oferta no ha descontado todavía lo consumido, y coincide con lo que se esta ofertando, por tanto si es cero es que no se ha consumido para esta demanda
		concepto:=demandaRow.concepto;

		IF ofertaRow.clave_producto < demandaRow.clave_producto THEN
			-- oferta sin demanda. Puedo llegar aqui o bien desde interacion oferta, o bien por que hubo una oferta superior a una demanda, salto a iterar a siguiente demanda, y ya no casaba el producto, quedando el resto de oferta sin demanda
			IF accion='Depreciacion_stock' THEN
					concepto:=accion;
					c_provisto_demanda:=0;
					SELECT * INTO demandaRow FROM (select null,demandaRow.almacen,ofertaRow.producto,c_resto_oferta,0,0,'REBAJAR',ofertaRow.clave_producto,ofertaRow.talla,ofertaRow.color) as p;	
					crearLinea:=true;
			ELSE			
				-- esta oferta no satisface ninguna demanda, debo seguir buscando otras ofertas para la demanda actual
				c_resto_oferta:=0;	
				CONTINUE "loopfeed";
			END IF;
		END IF;
		
		ofertaAlmacen:=ofertaRow.almacen;
		
		-- oferta < seria oferta sin demanda
		IF accion<>'Depreciacion_stock' and (ofertaRow.clave_producto > demandaRow.clave_producto or c_resto_oferta=0) THEN
			-- aqui, claves diferentes solo puede ser si la oferta se pasó (es superior en ordenacion), y en siguientes iteraciones de demanda, la demanda sigue siendo inferior
			RAISE NOTICE 'NOOFER2 dem orden % dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',demandaRow.orden,demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;		
			-- demanda sin oferta
			IF accion='Provision_demanda_futura' THEN
				-- simulo una oferta de provision futura igual a la necesidad insatisfecha, y nada de esta demanda esta satisfecha, porque o no ha casado con oferta, o la oferta es de cero
				c_resto_oferta:=demandaRow.orden-c_provisto_demanda;
				-- pongo como origen el mismo almacen
				ofertaAlmacen:=demandaRow.almacen;				

				concepto:=accion;
				crearLinea:=true;
				RAISE NOTICE 'PROVISTO % resto % ',c_provisto_demanda,c_resto_oferta;
			ELSE			
				-- este exit lleva a iterar la demanda. Se descarta esta demanda vacante, asi que itero por la siguiente, que es salir de loop feed					
				EXIT "loopfeed";
			END IF;
		END IF;
				
		-- actualizamos resto oferta
		IF demandaRow.orden-c_provisto_demanda<c_resto_oferta THEN
			cantidadRot:=demandaRow.orden-c_provisto_demanda;
			c_resto_oferta:=c_resto_oferta-cantidadRot;	
			RAISE NOTICE 'ROTA SUFIC q % dem_pro % ofer_pro % dem_alm % ofer_alm % dem_q % ofer_q % ',cantidadRot,demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,demandaRow.orden,ofertaRow.orden;
		ELSE
			cantidadRot:=c_resto_oferta;
			c_resto_oferta:=0;
			RAISE NOTICE 'ROTA PENDING q % dem_pro % ofer_pro % dem_alm % ofer_alm % dem_q % ofer_q % ',cantidadRot,demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,demandaRow.orden,ofertaRow.orden;
		END IF;
		
		IF ofertaRow.clave_producto > demandaRow.clave_producto THEN
			-- desautorizo calculo resto oferta anterior en caso cursor oferta se ha pasado, y por tanto no ha sido consumido
			c_resto_oferta:=ofertaRow.orden;
		END IF;
		
		--redondeo para arriba
		IF cantidadRot-floor(cantidadRot)>umbral_redondeo THEN 
			cantidadRot:=ceiling(cantidadRot);
		ELSE 
			cantidadRot:=floor(cantidadRot);
		END IF;
		
		
		RAISE NOTICE 'UMBRAL % cantidad % suma % redon % ',umbral_redondeo,cantidadRot,cantidadRot+umbral_redondeo,round((cantidadRot+umbral_redondeo)::numeric,0);
		IF cantidadRot>0 THEN
			-- provisto demanda nunca debe ser negativo, por eso se actualiza tras asegurar cantidad rot es positivo
			-- si descarto tras redondeo, debo consumir esta demanda
			c_provisto_demanda:=c_provisto_demanda+cantidadRot;
					
			num_linea:=num_linea+1;
			IF crearLinea THEN 
				-- añadimos 1 para evitar casos que si tiene que crear linea pero no tiene peso
				peso=1+demandaRow.peso;
			ELSE
				-- en provision futura, no creare lineas que puedan ser satisfechas con oferta disponible, se eliminarian al final filtrando por prioridad cero, pero antes las necesito
				-- para poder calcular la ocupacion propuesta total por destino en caso de provision futura, despues las eliminare
				peso=0;
			END IF;
			
			INSERT INTO linea_rotacion("rotacionId",rdn,clave_producto,origen,destino,cantidad,"productoGÉNERO",talla,color,concepto,peso_prioridad,r_destination,viable) VALUES(id_rotacion,id_rotacion||'.'||num_linea,demandaRow.clave_producto,ofertaAlmacen,demandaRow.almacen,cantidadRot,demandaRow.producto,demandaRow.talla,demandaRow.color,concepto,peso,'-',true);		
			
			c_total:=c_total+cantidadRot;
			RAISE NOTICE 'ROTA FEED q % dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',cantidadRot,demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
		ELSE
			EXIT "loopfeed";
		END IF;					
		
		IF c_provisto_demanda>=demandaRow.orden THEN
			RAISE NOTICE 'ROTA END q % dem_pro % ofer_pro % dem_alm % ofer_alm % ofer_q % ',cantidadRot,demandaRow.clave_producto,ofertaRow.clave_producto,demandaRow.almacen,ofertaRow.almacen,ofertaRow.orden;
			-- salgo al loop de siguiente oferta, pero no pongo resto oferta cero porque la oferta residual del producto actual puede servir para la siguiente demanda, quizas del mismo producto pero distinto almacen
			-- provisto demanda debe ponerse a cero, pero eso ya se hace al principio del loop
			EXIT;
		END IF;			
	END LOOP;

END LOOP;


CREATE TEMPORARY TABLE IF NOT EXISTS  limite (id integer,maximo double precision, propuesto double precision);
DELETE FROM limite;
INSERT INTO limite(maximo,id,propuesto) SELECT max(m),max(a),sum(des.cantidad) FROM 

											(select max("máximo") as m,"almacén" as a from "límite_stock" as ls WHERE ls.producto is null GROUP BY "almacén") as lim 	INNER JOIN
											(select destino as alm, cantidad from linea_rotacion where "rotacionId"=id_rotacion
											 union all
											 select res.almacen as alm, orden as cantidad from rotacion_oferta(fecha_min,fecha_max,null,true,destinoList,entregas_parciales_reservan,false,false) as res) as des on(des.alm=lim.a)			

											GROUP BY des.alm;

OPEN curLimite FOR select limite.id,maximo,propuesto from limite WHERE propuesto>maximo;
c_total_viable:=c_total;
<<for_limit>>
LOOP 
	FETCH curLimite INTO almacen,maximoV,ocupadoV;			
	IF NOT FOUND THEN EXIT;
	END IF;
	-- RAISE NOTICE 'FETCH ROT % ALM % ',id_rotacion,almacen;	
	FOR rotRow IN curRot LOOP
		-- RAISE NOTICE 'DEMANDA ID % DEST % CANT % PROD % PESO % ', rotRow."tableId",rotRow.destino,rotRow.cantidad,rotRow."productoGÉNERO",rotRow.peso_prioridad;
		IF ocupadoV<=maximoV THEN			
			CONTINUE for_limit;
		END IF;		
		IF ocupadoV-rotRow.cantidad>maximoV THEN			
			UPDATE linea_rotacion as lin set viable=false WHERE CURRENT OF curRot;
			c_total_viable:=c_total_viable-rotRow.cantidad;
			ocupadoV:=ocupadoV-rotRow.cantidad;	
			RAISE NOTICE 'DEL FOUND % OCUP %',FOUND,ocupadoV;	
		ELSE			
			UPDATE linea_rotacion as lin SET cantidad=lin.cantidad-(ocupadoV-maximoV) WHERE CURRENT OF curRot;
			INSERT INTO linea_rotacion("rotacionId",rdn,clave_producto,origen,destino,cantidad,"productoGÉNERO",talla,color,concepto,peso_prioridad,viable,r_destination) 
						values(rotRow."rotacionId",rotRow.rdn||'_2',rotRow.clave_producto,rotRow.origen,rotRow.destino,ocupadoV-maximoV,rotRow."productoGÉNERO",rotRow.talla,rotRow.color,rotRow.concepto,rotRow.peso_prioridad,false,rotRow.r_destination);
			c_total_viable:=c_total_viable-(rotRow.cantidad-(ocupadoV-maximoV));
			ocupadoV:=maximoV;
			RAISE NOTICE 'UPD FOUND % OCUP %',FOUND,ocupadoV;	
		END IF;								
	END LOOP;
END LOOP;

-- DELETE FROM linea_rotacion WHERE peso_prioridad=0;

UPDATE rotacion set cantidad_total=c_total,"fecha_creación"=extract(epoch FROM now())::int where "tableId"=id_rotacion;
--IF c_total_viable=0 THEN 
--	delete from rotacion where "tableId"=id_rotacion;
--END IF;
return query select ('creada rotacion '||id_rotacion)::varchar,id_rotacion;

END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100
  ROWS 1000;
ALTER FUNCTION rotacion_procesado(character varying, double precision,double precision, double precision, double precision, integer, integer,integer[],integer[],boolean,character varying, character varying, boolean, boolean,integer[])
  OWNER TO dynagent;

CREATE OR REPLACE FUNCTION tableidarray(idos integer[])
  RETURNS integer[] AS
$BODY$
 DECLARE
	resList integer[];
	item integer;
	pos integer:=0;
BEGIN
	FOR item IN SELECT (unnest(idos)::int)/1000
    LOOP
	RAISE NOTICE 'ITERA % ',item ;
	resList:= array_append(resList, item);
    END LOOP;
    RETURN resList;
END
$BODY$
LANGUAGE 'plpgsql' ;


CREATE OR REPLACE VIEW "v_línea_artículos2" AS 
        (         SELECT 
        		   			 
		    "línea_artículos_servicio".rdn,
                    "línea_artículos_servicio"."número",
                    146 AS "productoIdto",
                    "línea_artículos_servicio".fecha_inicio,
                    183 AS "mi_empresaIdto",
                    "línea_artículos_servicio"."porcentaje_realización",
                    NULL::integer AS tarjeta,
                    "línea_artículos_servicio".importe_con_iva,
                        CASE
                            WHEN "línea_artículos_servicio"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN 313
                            WHEN "línea_artículos_servicio"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN 312
                            ELSE NULL::integer
                        END AS "comisiónIdto",
                    "línea_artículos_servicio".fecha,
                        CASE
                            WHEN "línea_artículos_servicio"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN "línea_artículos_servicio"."comisiónCOMISIÓN_FIJA"
                            WHEN "línea_artículos_servicio"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN "línea_artículos_servicio"."comisiónCOMISIÓN_PORCENTUAL"
                            ELSE NULL::integer
                        END AS "comisión",
                    "línea_artículos_servicio"."porcentaje_dedicación",
                    "línea_artículos_servicio".coste_unitario,
                    "línea_artículos_servicio".concepto,
                    "línea_artículos_servicio".precio,
                    "línea_artículos_servicio".proyecto,
                    "línea_artículos_servicio".mi_empresa,
                    "línea_artículos_servicio".estado,
                    NULL::integer AS talla,
                    "línea_artículos_servicio"."tableId",
                    NULL::character varying(100) AS ubicacion3,
                    "línea_artículos_servicio".importe,
                    "línea_artículos_servicio".iva,
                    NULL::character varying(100) AS ubicacion2,
                    NULL::character varying(100) AS ubicacion1,
                    NULL::integer AS "loteIdto",
                    "línea_artículos_servicio"."aplica_retención",
                    NULL::double precision AS stock_disponible,
                    NULL::integer AS "almacén_origen",
                    "línea_artículos_servicio".r_destination,
                    "línea_artículos_servicio".medida2,
                    "línea_artículos_servicio".medida1,
                        CASE
                            WHEN "línea_artículos_servicio"."recursoEMPLEADO" IS NOT NULL THEN "línea_artículos_servicio"."recursoEMPLEADO"
                            WHEN "línea_artículos_servicio"."recursoMAQUINARIA" IS NOT NULL THEN "línea_artículos_servicio"."recursoMAQUINARIA"
                            WHEN "línea_artículos_servicio"."recursoAGENTE_COMERCIAL_FIJO" IS NOT NULL THEN "línea_artículos_servicio"."recursoAGENTE_COMERCIAL_FIJO"
                            WHEN "línea_artículos_servicio"."recursoTRABAJADOR_EXTERNO" IS NOT NULL THEN "línea_artículos_servicio"."recursoTRABAJADOR_EXTERNO"
                            ELSE NULL::integer
                        END AS recurso,
                    NULL::integer AS "almacén_origenIdto",
                    "línea_artículos_servicio".albaranado,
                    "línea_artículos_servicio".fecha_fin,
                    238 AS idto,
                    "línea_artículos_servicio".producto,
                    NULL::integer AS color,
                    NULL::character varying(100) AS campo_aux4,
                    NULL::bigint AS fecha_estimada_entrega,
                    NULL::integer AS lote,
                    "línea_artículos_servicio".descuento,
                    NULL::integer AS "colorIdto",
                    NULL::integer AS "tarjetaIdto",
                    "línea_artículos_servicio".clave_producto,
                        CASE
                            WHEN "línea_artículos_servicio"."recursoEMPLEADO" IS NOT NULL THEN 407
                            WHEN "línea_artículos_servicio"."recursoMAQUINARIA" IS NOT NULL THEN 244
                            WHEN "línea_artículos_servicio"."recursoAGENTE_COMERCIAL_FIJO" IS NOT NULL THEN 281
                            WHEN "línea_artículos_servicio"."recursoTRABAJADOR_EXTERNO" IS NOT NULL THEN 418
                            ELSE NULL::integer
                        END AS "recursoIdto",
                    387 AS "proyectoIdto",
                    NULL::double precision AS cantidad_envase_2,
                    NULL::double precision AS cantidad_envase_1,
                    "línea_artículos_servicio".cantidad,
                    NULL::integer AS "tallaIdto",
                    299 AS "estadoIdto",
                    "línea_artículos_servicio".precio_iva_incluido,
                    "línea_artículos_servicio".facturado,
                    "línea_artículos_servicio"."revisión",
                    "línea_artículos_servicio".margen_beneficio,
                    NULL::integer AS "almacén_destinoIdto",
                    NULL::integer AS "almacén_destino",
                    433 AS "ivaIdto",
                    "línea_artículos_servicio".documentos,

             case 
			 when "albarán_proveedorId" is not null then "albarán_proveedorId"
			 when "albarán-factura_proveedorId" is not null then "albarán-factura_proveedorId"	
			 when "albarán_clienteId" is not null then "albarán_clienteId"
			 when "albarán-factura_clienteId" is not null then "albarán-factura_clienteId"				 
			 else null end as "albarán",
			 		 
			 case when "albarán_proveedorId" is not null then 172
			 when "albarán-factura_proveedorId" is not null then 116
			 when "albarán_clienteId" is not null then 184
			 when "albarán-factura_clienteId" is not null then 319				 
			 else null end as "albaránIdto"
			 
			 
                   FROM "línea_artículos_servicio"
        UNION ALL
                 SELECT 
		   	 
			 
			"línea_artículos_materia".rdn,
                    "línea_artículos_materia"."número",
                    460 AS "productoIdto",
                    NULL::bigint AS fecha_inicio,
                    183 AS "mi_empresaIdto",
                    NULL::double precision AS "porcentaje_realización",
                    NULL::integer AS tarjeta,
                    "línea_artículos_materia".importe_con_iva,
                        CASE
                            WHEN "línea_artículos_materia"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN 313
                            WHEN "línea_artículos_materia"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN 312
                            ELSE NULL::integer
                        END AS "comisiónIdto",
                    "línea_artículos_materia".fecha,
                        CASE
                            WHEN "línea_artículos_materia"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN "línea_artículos_materia"."comisiónCOMISIÓN_FIJA"
                            WHEN "línea_artículos_materia"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN "línea_artículos_materia"."comisiónCOMISIÓN_PORCENTUAL"
                            ELSE NULL::integer
                        END AS "comisión",
                    NULL::double precision AS "porcentaje_dedicación",
                    "línea_artículos_materia".coste_unitario,
                    "línea_artículos_materia".concepto,
                    "línea_artículos_materia".precio,
                    "línea_artículos_materia".proyecto,
                    "línea_artículos_materia".mi_empresa,
                    "línea_artículos_materia".estado,
                    "línea_artículos_materia".talla,
                    "línea_artículos_materia"."tableId",
                    "línea_artículos_materia".ubicacion3,
                    "línea_artículos_materia".importe,
                    "línea_artículos_materia".iva,
                    "línea_artículos_materia".ubicacion2,
                    "línea_artículos_materia".ubicacion1,
                        CASE
                            WHEN "línea_artículos_materia"."loteLOTE_PERECEDERO" IS NOT NULL THEN 469
                            WHEN "línea_artículos_materia"."loteLOTE_PACK" IS NOT NULL THEN 355
                            WHEN "línea_artículos_materia"."loteSERIE" IS NOT NULL THEN 380
                            ELSE NULL::integer
                        END AS "loteIdto",
                    "línea_artículos_materia"."aplica_retención",
                    "línea_artículos_materia".stock_disponible,
                    "línea_artículos_materia"."almacén_origen",
                    "línea_artículos_materia".r_destination,
                    "línea_artículos_materia".medida2,
                    "línea_artículos_materia".medida1,
                    NULL::integer AS recurso,
                    3 AS "almacén_origenIdto",
                    "línea_artículos_materia".albaranado,
                    NULL::bigint AS fecha_fin,
                    427 AS idto,
                    "línea_artículos_materia".producto,
                    "línea_artículos_materia".color,
                    "línea_artículos_materia".campo_aux4,
                    "línea_artículos_materia".fecha_estimada_entrega,
                        CASE
                            WHEN "línea_artículos_materia"."loteLOTE_PERECEDERO" IS NOT NULL THEN "línea_artículos_materia"."loteLOTE_PERECEDERO"
                            WHEN "línea_artículos_materia"."loteLOTE_PACK" IS NOT NULL THEN "línea_artículos_materia"."loteLOTE_PACK"
                            WHEN "línea_artículos_materia"."loteSERIE" IS NOT NULL THEN "línea_artículos_materia"."loteSERIE"
                            ELSE NULL::integer
                        END AS lote,
                    "línea_artículos_materia".descuento,
                    256 AS "colorIdto",
                    NULL::integer AS "tarjetaIdto",
                    "línea_artículos_materia".clave_producto,
                    NULL::integer AS "recursoIdto",
                    387 AS "proyectoIdto",
                    "línea_artículos_materia".cantidad_envase_2,
                    "línea_artículos_materia".cantidad_envase_1,
                    "línea_artículos_materia".cantidad,
                    468 AS "tallaIdto",
                    299 AS "estadoIdto",
                    "línea_artículos_materia".precio_iva_incluido,
                    "línea_artículos_materia".facturado,
                    "línea_artículos_materia"."revisión",
                    "línea_artículos_materia".margen_beneficio,
                    3 AS "almacén_destinoIdto",
                    "línea_artículos_materia"."almacén_destino",
                    433 AS "ivaIdto",
                    "línea_artículos_materia".documentos,
					
             case 
			 when "albarán_proveedorId" is not null then "albarán_proveedorId"
			 when "albarán-factura_proveedorId" is not null then "albarán-factura_proveedorId"	
			 when "albarán_clienteId" is not null then "albarán_clienteId"
			 when "albarán-factura_clienteId" is not null then "albarán-factura_clienteId"				 
			 else null end as "albarán",
			 		 
			 case when "albarán_proveedorId" is not null then 172
			 when "albarán-factura_proveedorId" is not null then 116
			 when "albarán_clienteId" is not null then 184
			 when "albarán-factura_clienteId" is not null then 319				 
			 else null end as "albaránIdto"
			 
                   FROM "línea_artículos_materia")
UNION ALL
         SELECT 
  		    
	    "línea_artículos_financiera".rdn,
            "línea_artículos_financiera"."número",
            193 AS "productoIdto",
            NULL::bigint AS fecha_inicio,
            183 AS "mi_empresaIdto",
            NULL::double precision AS "porcentaje_realización",
            "línea_artículos_financiera".tarjeta,
            "línea_artículos_financiera".importe_con_iva,
                CASE
                    WHEN "línea_artículos_financiera"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN 313
                    WHEN "línea_artículos_financiera"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN 312
                    ELSE NULL::integer
                END AS "comisiónIdto",
            "línea_artículos_financiera".fecha,
                CASE
                    WHEN "línea_artículos_financiera"."comisiónCOMISIÓN_FIJA" IS NOT NULL THEN "línea_artículos_financiera"."comisiónCOMISIÓN_FIJA"
                    WHEN "línea_artículos_financiera"."comisiónCOMISIÓN_PORCENTUAL" IS NOT NULL THEN "línea_artículos_financiera"."comisiónCOMISIÓN_PORCENTUAL"
                    ELSE NULL::integer
                END AS "comisión",
            NULL::double precision AS "porcentaje_dedicación",
            "línea_artículos_financiera".coste_unitario,
            "línea_artículos_financiera".concepto,
            "línea_artículos_financiera".precio,
            "línea_artículos_financiera".proyecto,
            "línea_artículos_financiera".mi_empresa,
            "línea_artículos_financiera".estado,
            NULL::integer AS talla,
            "línea_artículos_financiera"."tableId",
            NULL::character varying(100) AS ubicacion3,
            "línea_artículos_financiera".importe,
            "línea_artículos_financiera".iva,
            NULL::character varying(100) AS ubicacion2,
            NULL::character varying(100) AS ubicacion1,
            NULL::integer AS "loteIdto",
            "línea_artículos_financiera"."aplica_retención",
            NULL::double precision AS stock_disponible,
            NULL::integer AS "almacén_origen",
            "línea_artículos_financiera".r_destination,
            "línea_artículos_financiera".medida2,
            "línea_artículos_financiera".medida1,
            NULL::integer AS recurso,
            NULL::integer AS "almacén_origenIdto",
            "línea_artículos_financiera".albaranado,
            NULL::bigint AS fecha_fin,
            339 AS idto,
            "línea_artículos_financiera".producto,
            NULL::integer AS color,
            NULL::character varying(100) AS campo_aux4,
            NULL::bigint AS fecha_estimada_entrega,
            NULL::integer AS lote,
            "línea_artículos_financiera".descuento,
            NULL::integer AS "colorIdto",
            435 AS "tarjetaIdto",
            "línea_artículos_financiera".clave_producto,
            NULL::integer AS "recursoIdto",
            387 AS "proyectoIdto",
            NULL::double precision AS cantidad_envase_2,
            NULL::double precision AS cantidad_envase_1,
            "línea_artículos_financiera".cantidad,
            NULL::integer AS "tallaIdto",
            299 AS "estadoIdto",
            "línea_artículos_financiera".precio_iva_incluido,
            "línea_artículos_financiera".facturado,
            "línea_artículos_financiera"."revisión",
            "línea_artículos_financiera".margen_beneficio,
            NULL::integer AS "almacén_destinoIdto",
            NULL::integer AS "almacén_destino",
            433 AS "ivaIdto",
            "línea_artículos_financiera".documentos,
			
             case 
			 when "albarán_proveedorId" is not null then "albarán_proveedorId"
			 when "albarán-factura_proveedorId" is not null then "albarán-factura_proveedorId"	
			 when "albarán_clienteId" is not null then "albarán_clienteId"
			 when "albarán-factura_clienteId" is not null then "albarán-factura_clienteId"				 
			 else null end as "albarán",
			 		 
			 case when "albarán_proveedorId" is not null then 172
			 when "albarán-factura_proveedorId" is not null then 116
			 when "albarán_clienteId" is not null then 184
			 when "albarán-factura_clienteId" is not null then 319				 
			 else null end as "albaránIdto"
			 
           FROM "línea_artículos_financiera";

