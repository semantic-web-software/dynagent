DROP VIEW IF EXISTS s_cardmed;
CREATE VIEW s_cardmed AS
select c1.rdn as "idto_parent_name", c2.rdn as "idto_name", "tama�o" as "cardmed" 
from "tama�o_tabla" as tam
left join clase as c1 on (tam.dominio=c1."tableId")
inner join clase as c2 on (tam.tabla=c2."tableId");

DROP VIEW IF EXISTS s_cardmedfield;
CREATE VIEW s_cardmedfield AS
select c.rdn as "idto_parent_name", prop.rdn as "idprop_name", "tama�o" as "cardmed" 
from "tama�o_campo" as tam
left join clase as c on (tam.dominio=c."tableId")
inner join v_propiedad as prop on(tam."propiedad"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_DATO'));

DROP VIEW IF EXISTS s_alias;
CREATE VIEW s_alias AS
select am.rdn as "utask", cast(null as character varying(100)) as "group", c.rdn as "class", prop.rdn as "prop", ap.alias as "alias" from alias_propiedad as ap
inner join v_propiedad as prop on(ap."propiedadPROPIEDAD_DATO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_DATO') or ap."propiedadPROPIEDAD_OBJETO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_OBJETO'))
left join "v_alias_propiedad#acci�n, informe, men�" as asoc_am on (asoc_am."alias_propiedadId"=ap."tableId")
left join "v_acci�n, informe, men�" as am on(asoc_am."acci�n, informe, men�Id"=am."tableId" and asoc_am."acci�n, informe, men�Idto"=am."idto")
left join "alias_propiedad#clase" as acl on (ap."tableId"=acl."alias_propiedadId")
left join clase as c on (acl."claseId"=c."tableId")
union
select am.rdn as "utask", cast(null as character varying(100)) as "group", c.rdn as "class", cast(null as character varying(100)) as "prop", af.alias as "alias" from alias_formulario as af
left join "v_alias_formulario#acci�n, informe, men�" as asoc_am on (asoc_am."alias_formularioId"=af."tableId")
left join "v_acci�n, informe, men�" as am on(asoc_am."acci�n, informe, men�Id"=am."tableId" and asoc_am."acci�n, informe, men�Idto"=am."idto")
inner join clase as c on (af.dominio=c."tableId")
union
select am.rdn as "utask", cast(null as character varying(100)) as "group", cast(null as character varying(100)) as "class", cast(null as character varying(100)) as "prop", aa.alias as "alias" from "alias_�mbito" as aa
inner join "v_acci�n, informe, men�" as am on(aa."�mbitoMEN�"=am."tableId" and am."idto"=(select id from clase where rdn='MEN�') 
or aa."�mbitoACCI�N"=am."tableId" and am."idto"=(select id from clase where rdn='ACCI�N'))
union
select am.rdn as "utask", gc.rdn as "group", cast(null as character varying(100)) as "class", cast(null as character varying(100)) as "prop", ag.alias as "alias" from alias_grupo as ag
left join "v_alias_grupo#acci�n, informe, men�" as asoc_am on (asoc_am."alias_grupoId"=ag."tableId")
left join "v_acci�n, informe, men�" as am on(asoc_am."acci�n, informe, men�Id"=am."tableId" and asoc_am."acci�n, informe, men�Idto"=am."idto")
inner join grupo_campos as gc on (ag.grupo_campos=gc."tableId");

DROP VIEW IF EXISTS s_columnproperties;
CREATE VIEW s_columnproperties AS
select c1.rdn as "classparent", c2.rdn as "class", ruta_propiedad as "proppath", cast(null as character varying(100)) as "prop", propf.rdn as "propfilter", valor_filtro as "valuefilter", orden as "order" from columnas_tabla as ct
left join clase as c1 on (ct.dominio=c1."tableId")
left join clase as c2 on (ct.tabla=c2."tableId")
left join orden_campo_con_filtro as ocf on(ocf."columnas_tablaId"=ct."tableId")
left join v_propiedad as propf on(ocf."campo_filtroPROPIEDAD_DATO"=propf."tableId" and propf.idto=(select id from clase where rdn='PROPIEDAD_DATO') or ocf."campo_filtroPROPIEDAD_OBJETO"=propf."tableId" and propf.idto=(select id from clase where rdn='PROPIEDAD_OBJETO'));

DROP VIEW IF EXISTS s_orderproperties;
CREATE VIEW s_orderproperties AS
select orc."tableId" as "sec", c.rdn as "class", prop.rdn as "prop", oc.orden as "order" from orden_relativo_campos as orc
left join clase as c on (orc.dominio=c."tableId")
inner join orden_campo as oc on(oc."orden_relativo_camposId"=orc."tableId")
left join v_propiedad as prop on(oc."propiedadPROPIEDAD_DATO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_DATO') or oc."propiedadPROPIEDAD_OBJETO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_OBJETO'));

DROP VIEW IF EXISTS s_essentialproperties;
CREATE VIEW s_essentialproperties AS
select am.rdn as "utask", c.rdn as "class", prop.rdn as "prop" from campo_destacado as cd
left join "v_acci�n, informe, men�" as am on(cd."�mbitoMEN�"=am."tableId" and am."idto"=(select id from clase where rdn='MEN�') 
or cd."�mbitoACCI�N"=am."tableId" and am."idto"=(select id from clase where rdn='ACCI�N'))
left join clase as c on (cd.dominio=c."tableId")
inner join propiedad_dato as prop on(cd."propiedad"=prop."tableId");

DROP VIEW IF EXISTS s_mask;
CREATE VIEW s_mask AS
select am.rdn as "utask", c.rdn as "class", prop.rdn as "prop", mc."expresi�n_regular" as "expresion", mc.longitud as "length" from "m�scara_campo" as mc
left join "v_acci�n, informe, men�" as am on(mc."�mbitoMEN�"=am."tableId" and am."idto"=(select id from clase where rdn='MEN�') 
or mc."�mbitoACCI�N"=am."tableId" and am."idto"=(select id from clase where rdn='ACCI�N'))
left join clase as c on (mc.dominio=c."tableId")
inner join v_propiedad as prop on(mc."propiedadPROPIEDAD_DATO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_DATO') or mc."propiedadPROPIEDAD_OBJETO"=prop."tableId" and prop.idto=(select id from clase where rdn='PROPIEDAD_OBJETO'));

DROP VIEW IF EXISTS s_groups;
CREATE VIEW s_groups AS
select gc.rdn as "name", am.rdn as "utask", c.rdn as "class", prop.rdn as "prop", gc.orden as "order" from "grupo_campos" as gc
left join "v_acci�n, informe, men�" as am on(gc."�mbitoMEN�"=am."tableId" and am."idto"=(select id from clase where rdn='MEN�') 
or gc."�mbitoACCI�N"=am."tableId" and am."idto"=(select id from clase where rdn='ACCI�N'))
left join clase as c on (gc.dominio=c."tableId")
left join "v_grupo_campos#propiedad" as gp on (gc."tableId"=gp."grupo_camposId")
inner join v_propiedad as prop on(gp."propiedadId"=prop."tableId" and gp."propiedadIdto"=prop.idto);

DROP VIEW IF EXISTS s_access;
CREATE VIEW s_access AS
select (case when ap.rdn='Denegar' then 1 else 0 end) as dennied, cast(null as character varying(100)) as "utask", cast(null as integer) as "utaskIdto", ur.rdn as "userrol", u.rdn as "user", 
cast(null as character varying(100)) as "class", cast(null as character varying(100)) as "prop", 
array_to_string(array_agg(tp.rdn),',') as access, 
pri.peso_prioridad as "priority", fa."tableId" as "functionalarea" 
from "permiso_gen�rico" as pa
inner join prioridad as pri on (pa.prioridad=pri."tableId")
inner join "acci�n_permiso" as ap on (pa."acci�n_permiso"=ap."tableId")
left join "permiso_gen�rico#userrol" as pur on (pa."tableId"=pur."permiso_gen�ricoId")
left join "userrol" as ur on (pur."userrolId"=ur."tableId")
left join "permiso_gen�rico#user" as pu on (pa."tableId"=pu."permiso_gen�ricoId")
left join "user" as u on (pu."userId"=u."tableId")
left join "permiso_gen�rico#functional_area" as pfa on (pa."tableId"=pfa."permiso_gen�ricoId")
left join "functional_area" as fa on (pfa."functional_areaId"=fa."tableId")
inner join "permiso_gen�rico#tipo_permiso" as pt on (pa."tableId"=pt."permiso_gen�ricoId")
inner join "tipo_permiso" as tp on (pt."tipo_permisoId"=tp."tableId")
group by pa."tableId", ap.rdn, ur.rdn, u.rdn, pri.peso_prioridad, fa."tableId" 
union
select (case when ap.rdn='Denegar' then 1 else 0 end) as dennied, am.rdn as "utask", am.idto as "utaskIdto", ur.rdn as "userrol", u.rdn as "user", 
cast(null as character varying(100)) as "class", cast(null as character varying(100)) as "prop", 
array_to_string(array_agg(tp.rdn),',') as access, 
pri.peso_prioridad as "priority", cast(null as integer) as "functionalarea" 
from "permiso_�mbito" as pa
inner join prioridad as pri on (pa.prioridad=pri."tableId")
inner join "acci�n_permiso" as ap on (pa."acci�n_permiso"=ap."tableId")
inner join "v_permiso_�mbito#acci�n, informe, men�" as pfaim on (pa."tableId"=pfaim."permiso_�mbitoId")
inner join "v_acci�n, informe, men�" as am on(pfaim."acci�n, informe, men�Id"=am."tableId" and pfaim."acci�n, informe, men�Idto"=am."idto") 
left join "permiso_�mbito#userrol" as pur on (pa."tableId"=pur."permiso_�mbitoId")
left join "userrol" as ur on (pur."userrolId"=ur."tableId")
left join "permiso_�mbito#user" as pu on (pa."tableId"=pu."permiso_�mbitoId")
left join "user" as u on (pu."userId"=u."tableId")
inner join "permiso_�mbito#tipo_permiso" as pt on (pa."tableId"=pt."permiso_�mbitoId")
inner join "tipo_permiso" as tp on (pt."tipo_permisoId"=tp."tableId")
group by pa."tableId", ap.rdn, am.rdn, am.idto, ur.rdn, u.rdn, pri.peso_prioridad
union
select (case when ap.rdn='Denegar' then 1 else 0 end) as dennied, am.rdn as "utask", am.idto as "utaskIdto", ur.rdn as "userrol", u.rdn as "user", c.rdn as "class", 
cast(null as character varying(100)) as "prop", array_to_string(array_agg(tp.rdn),',') as access, pri.peso_prioridad as "priority", cast(null as integer) as "functionalarea"  
from "permiso_formulario" as pf
inner join prioridad as pri on (pf.prioridad=pri."tableId")
inner join "acci�n_permiso" as ap on (pf."acci�n_permiso"=ap."tableId")
left join "v_permiso_formulario#acci�n, informe, men�" as asoc_am on (asoc_am."permiso_formularioId"=pf."tableId")
left join "v_acci�n, informe, men�" as am on(asoc_am."acci�n, informe, men�Id"=am."tableId" and asoc_am."acci�n, informe, men�Idto"=am."idto")
left join "permiso_formulario#userrol" as pur on (pf."tableId"=pur."permiso_formularioId")
left join "userrol" as ur on (pur."userrolId"=ur."tableId")
left join "permiso_formulario#user" as pu on (pf."tableId"=pu."permiso_formularioId")
left join "user" as u on (pu."userId"=u."tableId")
left join "permiso_formulario#clase" as pfc on (pf."tableId"=pfc."permiso_formularioId")
left join "clase" as c on (pfc."claseId"=c."tableId")
inner join "permiso_formulario#tipo_permiso" as pt on (pf."tableId"=pt."permiso_formularioId")
inner join "tipo_permiso" as tp on (pt."tipo_permisoId"=tp."tableId")
group by pf."tableId",ap.rdn, am.rdn, am.idto, ur.rdn, u.rdn, c.rdn, pri.peso_prioridad
union
select (case when ap.rdn='Denegar' then 1 else 0 end) as dennied, am.rdn as "utask", am.idto as "utaskIdto", ur.rdn as "userrol", u.rdn as "user", c.rdn as "class", 
prop.rdn as "prop", array_to_string(array_agg(tp.rdn),',') as access, pri.peso_prioridad as "priority", cast(null as integer) as "functionalarea" 
from "permiso_propiedad" as pp
inner join prioridad as pri on (pp.prioridad=pri."tableId")
inner join "acci�n_permiso" as ap on (pp."acci�n_permiso"=ap."tableId")
left join "v_permiso_propiedad#acci�n, informe, men�" as asoc_am on (asoc_am."permiso_propiedadId"=pp."tableId")
left join "v_acci�n, informe, men�" as am on(asoc_am."acci�n, informe, men�Id"=am."tableId" and asoc_am."acci�n, informe, men�Idto"=am."idto")
left join "permiso_propiedad#userrol" as pur on (pp."tableId"=pur."permiso_propiedadId")
left join "userrol" as ur on (pur."userrolId"=ur."tableId")
left join "permiso_propiedad#user" as pu on (pp."tableId"=pu."permiso_propiedadId")
left join "user" as u on (pu."userId"=u."tableId")
left join "permiso_propiedad#clase" as pc on (pp."tableId"=pc."permiso_propiedadId")
left join "clase" as c on (pc."claseId"=c."tableId")
left join "v_permiso_propiedad#propiedad" as ppp on (pp."tableId"=ppp."permiso_propiedadId")
inner join v_propiedad as prop on(ppp."propiedadId"=prop."tableId" and ppp."propiedadIdto"=prop.idto)
inner join "permiso_propiedad#tipo_permiso" as pt on (pp."tableId"=pt."permiso_propiedadId")
inner join "tipo_permiso" as tp on (pt."tipo_permisoId"=tp."tableId")
group by pp."tableId", ap.rdn, am.rdn, am.idto, ur.rdn, u.rdn, c.rdn, prop.rdn, pri.peso_prioridad;


CREATE AGGREGATE array_agg (anyelement)
(
    sfunc = array_append,
    stype = anyarray,
    initcond = '{}'
);


DROP VIEW IF EXISTS s_utask;
CREATE VIEW s_utask AS
select i.rdn, i.dominio as "dominioId", func."tableId" as "functional_areaId", 
func.rdn as functional_area, crgb.rojo as color_rojo, crgb.verde as color_verde, crgb.azul as color_azul, avi."minutos_actualizaci�n", i."global"
from "men�" as i 
left join functional_area as func on(i."�rea_funcional"=func."tableId")  
left join aviso as avi on(i.aviso_encontrados=avi."tableId")  
left join color_rgb as crgb on(avi.color_aviso=crgb."tableId")
group by i."tableId", i.rdn, i.dominio, func."tableId", func.rdn, crgb.rojo, crgb.azul, crgb.verde, avi."minutos_actualizaci�n", i."global";


DROP VIEW IF EXISTS s_report;
CREATE VIEW s_report AS
select i."tableId", i.rdn, c.id as "dominio", 
array_to_string(array_agg(func.rdn),',') as functional_area, 
i."impresi�n_directa", i.vista_previa, 
array_to_string(array_agg(r_format.rdn),',') as formato_informe, 
i."confirmar_impresi�n", i."copias_impresi�n",i.impresora
 from "informe" as i 
left join clase as c on(c."tableId"=i.dominio)
left join "informe#functional_area" as ifunc on(ifunc."informeId"=i."tableId") 
left join functional_area as func on(ifunc."functional_areaId"=func."tableId") 
left join "informe#report_format" as i_r_format on(i."tableId"=i_r_format."informeId") 
left join report_format as r_format on(i_r_format."report_formatId"=r_format."tableId") 
group by i."tableId",i.rdn, c.id, i."impresi�n_directa", i.vista_previa, i."confirmar_impresi�n", i."copias_impresi�n";


DROP VIEW IF EXISTS s_required;
CREATE VIEW s_required AS
select c.rdn as "class", prop.rdn as "prop" from campo_requerido as cr
left join clase as c on (cr.dominio=c."tableId")
left join "v_campo_requerido#propiedad" as crp on (cr."tableId"=crp."campo_requeridoId")
inner join v_propiedad as prop on(crp."propiedadId"=prop."tableId" and crp."propiedadIdto"=prop.idto);