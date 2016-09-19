package dynagent.ruleengine.src.ruler;


import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;



import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.IteratorQuery;
import dynagent.common.communication.docServer;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.Domain;
import dynagent.common.properties.IDIndividual;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.utils.Apunte;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.SubCuenta;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.ArticuloProyecto;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.VentasComercial;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;

public abstract class Query {
	
	
   /******************************************************************************************************
	********************************************************************************************************
					QUERYS IMPLEMENTADAS.  -- POR CADA NUEVA CREAR UNA CONSTANTE
	*********************************************************************************************************
	********************************************************************************************************/
	
	public static final String cobroAnticipo="cobroAnticipo";	
	public static final String ultimas3VentasPorCliente="ultimas3VentasPorCliente";
	public static final String cantidadesPrecioArticuloProyecto="cantidadesPrecioArticuloProyecto";
	public static final String cantidadesProgramadasPrecioArticuloProyecto="cantidadesProgramadasPrecioArticuloProyecto";	
	public static final String infoVentasAlbaranArticulo="infoVentasAlbaranArticulo";
	public static final String infoComprasAlbaranArticulo="infoComprasAlbaranArticulo";	
	public static final String infoVentasComercial="infoVentasComercial";	
	public static final String exportarContaPlus="exportarContaPlus";
	
	
	
	/******************************************************************************************************
	********************************************************************************************************
	 					OBTENCION DEL SQL CORRESPONDIENTES A LAS QUERYS
	*********************************************************************************************************
	********************************************************************************************************/
	 public static String getSQLOfQuery(String nameQuery,ArrayList parametros){
		// System.err.println("\n DEBUG inicio getSQLOfQuery:  "+nameQuery+" parametros:"+parametros);
		 if(nameQuery.equals(Query.ultimas3VentasPorCliente)){
			 int idoProducto=(Integer)parametros.get(0);
			 int idoCliente=(Integer)parametros.get(1);
			 return Query.ultimas3VentasPorCliente(idoProducto,idoCliente);
		 }
		 else if(nameQuery.equals(Query.cobroAnticipo)){
			 int idoCliente=(Integer)parametros.get(0);
			 return Query.cobroAnticipo(idoCliente);
		 }
		 else if(nameQuery.equals(Query.cantidadesPrecioArticuloProyecto)||nameQuery.equals(Query.cantidadesProgramadasPrecioArticuloProyecto)){
			 ObjectValue proyecto=(ObjectValue)parametros.get(0);
			 Date fechaMin=(Date)parametros.get(1);
			 Date fechaMax=(Date)parametros.get(2);			 
			 String result=null;
			 if(nameQuery.equals(Query.cantidadesPrecioArticuloProyecto)){
				 result=Query.cantidadesPrecioArticuloProyecto(proyecto, fechaMin, fechaMax);
			 }
			 else{
				 result=Query.cantidadesPrecioArticuloProgramadosProyecto(proyecto, fechaMin, fechaMax);
			 }
			 return result;
		 }
		 else if(nameQuery.equals(Query.infoComprasAlbaranArticulo)){					
			 int idoProducto=(Integer)parametros.get(0);
			 return Query.infoComprasAlbaranArticulo(idoProducto);
		 }
		 else if(nameQuery.equals(Query.infoVentasAlbaranArticulo)){
			 int idoProducto=(Integer)parametros.get(0);
			 return Query.infoVentasAlbaranArticulo(idoProducto);
		 }
		 else if(nameQuery.equals(Query.infoVentasComercial)){
			 String agente=(String)parametros.get(0);
			 Date fechaMin=(Date)parametros.get(1);
			 Date fechaMax=(Date)parametros.get(2);
			 return Query.ventasComercial(agente, fechaMin, fechaMax);
		 }
		 else if(nameQuery.equals(Query.exportarContaPlus)){
			 String rdnMiEmpresa=(String)parametros.get(0);
			 Date fechaMin=(Date)parametros.get(1);
			 Date fechaMax=(Date)parametros.get(2);
			 return Query.exportarContaPlus(rdnMiEmpresa, fechaMin, fechaMax);
		 }
		 else {
			 System.err.println("\n\n ERROR: QUERY.java con nameQuery:"+nameQuery+"  no implementada. Parametros: "+parametros);
			 return null;
		 }
	 }
	
	/**
	 * Construye la consulta para obtener de base de datos los ultimos 3 precios
	 * a los que se le a vendido un determinado producto a un determinado
	 * cliente.<br>
	 * La consulta esta hecha para que la primera columna sea la fecha en la que
	 * se ha encontrado el precio y la segunda columna es el precio al que se
	 * vendio el producto en esa fecha y la tercera la cantidad.
	 * 
	 * @param idoProducto
	 *            Identificador del producto del que queremos los precios.
	 * @param idoCliente
	 *            Identificador del cliente que queremos fijar.
	 * @return Consulta SQL para obtener los datos. Los datos se devuelven en el
	 *         siguiente orden:<br>
	 *         <table border="2">
	 *         <theader>
	 *         <th>fecha (segundos)</th>
	 *         <th>precio (maximo en documento)</th>
	 *         <th>cantidad (total en documento)</th>
	 *         </theader>
	 *         </table>
	 *         
	 */
	private static String ultimas3VentasPorCliente (int idoProducto, int idoCliente){
		int clienteId = QueryConstants.getTableId(idoCliente);
		int clienteIdto = QueryConstants.getIdto(idoCliente);
		int productoId = QueryConstants.getTableId(idoProducto);
		int productoIdto = QueryConstants.getIdto(idoProducto);
		
		String sql = "SELECT max(f.fecha), max(la.precio),max(la.\"precio_iva_incluido\"), sum(la.cantidad) FROM v_factura f " +
			"INNER JOIN \"v_factura_a_cliente#línea_artículos\" f_la ON (f.\"tableId\"=f_la.\"factura_a_clienteId\" AND f.idto=f_la.\"factura_a_clienteIdto\") " +
			"INNER JOIN \"v_línea_artículos\" la ON (f_la.\"línea_artículosId\"=la.\"tableId\" AND f_la.\"línea_artículosIdto\"=la.idto) " +
			"WHERE la.producto=" + productoId + " AND la.\"productoIdto\"=" + productoIdto + " AND f.cliente=" + clienteId + " AND f.\"clienteIdto\"=" + clienteIdto +" "+
			"GROUP BY f.\"tableId\" " +
			"ORDER BY 1 DESC " +
			"LIMIT 3";
		//System.err.println("sql:"+sql);
		return sql;
	}
	
	/**
	 * Metodo para generar la query que consulta los COBRO_ANTICIPO asignados a
	 * un cliente concreto.
	 * 
	 * @param idoCliente
	 *            Identificador del cliente en la aplicacion.
	 * @return Consulta para conocer los COBRO_ANTICIPO vinculados al cliente
	 *         especificado.
	 */
	
	private static String cobroAnticipo (int idoCliente){
		int tableId = QueryConstants.getTableId(idoCliente);
		int idto = QueryConstants.getIdto(idoCliente);
		String sql = "SELECT cobro.\"tableId\", c.id " + 
			"FROM cobro_anticipo AS cobro " +
			"INNER JOIN \"v_cobro_anticipo#cliente\" AS cobrocliente ON (cobro.\"tableId\"=cobrocliente.\"cobro_anticipoId\") " +
			"INNER JOIN v_cliente AS cliente ON (cobrocliente.\"clienteId\"=cliente.\"tableId\" and cobrocliente.\"clienteIdto\"=cliente.idto) " +
			"INNER JOIN clase AS c ON (c.rdn='COBRO_ANTICIPO') " +
			//"WHERE cobro.importe_anticipado>=cobro.importe AND cliente.\"tableId\"=" + tableId + " AND cliente.idto=" + idto;
			"WHERE  cliente.\"tableId\"=" + tableId + " AND cliente.idto=" + idto + " AND cobro.importe_pendiente_asignar>0";
		return sql;
	}
	
	private static String infoComprasAlbaranArticulo (int idoProducto){
		int productoId = QueryConstants.getTableId(idoProducto);
		int productoIdto = QueryConstants.getIdto(idoProducto);		
		
		String sql ="select ent.rdn, ent.fecha, sum(lin.cantidad),max(lin.precio) "+
					"from 	\"v_entrada_artículos\" as ent	inner join "+
					"\"v_entrada_artículos#línea_financiera, línea_materia, lí~1\" as ent_lin 	on(	ent.\"tableId\"=ent_lin.\"entrada_artículosId\" and "+
													"ent.idto=ent_lin.\"entrada_artículosIdto\")						inner join "+ 
			"\"v_línea_artículos\" as lin 		on(	lin.\"tableId\"=ent_lin.\"línea_financiera, línea_materia, línea_servicioId\" and "+
													"lin.idto=ent_lin.\"línea_financiera, línea_materia, línea_servicioIdto\") "+	
		"WHERE lin.producto="+productoId+" AND lin.\"productoIdto\"=" + productoIdto+" "+

		"group by ent.fecha, ent.rdn "+
		"order by ent.fecha, ent.rdn "+
		"LIMIT 20";
		//System.err.println("sql:"+sql);
		return sql;
	}
		
	private static String infoVentasAlbaranArticulo (int idoProducto){
		int productoId = QueryConstants.getTableId(idoProducto);
		int productoIdto = QueryConstants.getIdto(idoProducto);		
		
		String sql= "select fact.fecha,fact.rdn, sum(la.cantidad), max(la.precio) "+
		"from "+
		"\"v_línea_artículos\" as la "+
		"inner join \"v_factura_a_cliente#línea_artículos\" as fla on(la.\"tableId\"=fla.\"línea_artículosId\" and la.idto=fla.\"línea_artículosIdto\") "+
		"inner join v_factura_a_cliente as fact on(fact.\"tableId\"=fla.\"factura_a_clienteId\" and fact.idto=fla.\"factura_a_clienteIdto\") "+
		"WHERE la.producto="+productoId+" AND la.\"productoIdto\"=" + productoIdto+" "+

		"group by fact.fecha, fact.rdn "+
		"order by fact.fecha, fact.rdn "+
		"LIMIT 20";	
		
		//System.err.println("sql:"+sql);
		return sql;
	}
	
	private static String cantidadesPrecioArticuloProyecto (ObjectValue proyecto,Date fehaInicio,Date  fechaFin){
		int proyectoId = QueryConstants.getTableId(proyecto.getValue());
		int proyectoIdto = proyecto.getValueCls();
		long segundosFechaInicio=(long)fehaInicio.getTime()/Constants.TIMEMILLIS;
		long segundosFechaFin=(long)fechaFin.getTime()/Constants.TIMEMILLIS;		

		//TODO FALTA AÑADIRLE AL SQL QUE TOME TAMBIEN LOS PRODUCTOS CONSUMIDOS (ESTAN EN TAREAS DE ESE PROYECTO BAJO LA OBJECTPROPERTY CONSUMO
		String sql= "select la.producto,max(la.\"productoIdto\"), sum(la.cantidad), max(la.precio) "+
		"from "+
		"\"v_línea_artículos\" as la "+
		"inner join \"v_factura_proveedor#línea_artículos\" as fla on(la.\"tableId\"=fla.\"línea_artículosId\" and la.idto=fla.\"línea_artículosIdto\") "+
		"inner join v_factura_proveedor as fact on(fact.\"tableId\"=fla.\"factura_proveedorId\" and fact.idto=fla.\"factura_proveedorIdto\") "+
		"WHERE la.proyecto="+proyectoId+"  AND fact.fecha >= "+segundosFechaInicio+"  AND fact.fecha <= "+segundosFechaFin +" AND la.\"proyectoIdto\"=" + proyectoIdto+" "+
		"group by la.producto "+
		"order by la.producto";
		//System.err.println("sql:"+sql);
		return sql;
	}
		
	private static String cantidadesPrecioArticuloProgramadosProyecto (ObjectValue proyecto,Date fehaInicio,Date  fechaFin){
		int proyectoId = QueryConstants.getTableId(proyecto.getValue());
		int proyectoIdto = proyecto.getValueCls();
		long segundosFechaInicio=(long)fehaInicio.getTime()/Constants.TIMEMILLIS;
		long segundosFechaFin=(long)fechaFin.getTime()/Constants.TIMEMILLIS;		

		//TODO FALTA AÑADIRLE AL SQL QUE TOME TAMBIEN LOS PRODUCTOS PROGRAMADOS EN TAREAS  TAREAS DE ESE PROYECTO BAJO LA OBJECTPROPERTY PROGRAMACION
		String sql= "select la.producto,max(la.\"productoIdto\"), sum(la.cantidad), max(la.precio) "+
		"from "+
		"\"v_línea_artículos\" as la "+
		"inner join \"v_presupuesto_compra#línea_artículos\" as fla on(la.\"tableId\"=fla.\"línea_artículosId\" and la.idto=fla.\"línea_artículosIdto\") "+
		"inner join presupuesto_compra as fact on(fact.\"tableId\"=fla.\"presupuesto_compraId\" ) "+
		"WHERE la.proyecto="+proyectoId+"  AND fact.fecha >= "+segundosFechaInicio+"  AND fact.fecha <= "+segundosFechaFin +" AND la.\"proyectoIdto\"=" + proyectoIdto+" "+
		"group by la.producto "+
		"order by la.producto";
		//System.err.println("sql:"+sql);
		return sql;
	}
	
	private static String exportarContaPlus (String rdnMiEmpresa, Date fehaInicio, Date fechaFin){
		long segundosFechaInicio=(long)fehaInicio.getTime()/Constants.TIMEMILLIS;
		long segundosFechaFin=(long)fechaFin.getTime()/Constants.TIMEMILLIS;		

		//no es necesario enlazar con desglose de iva (caso venta de varias mi empresa) porque el asiento ya tiene el parcial por empresa y solo necesita de la factura su codigo y cliente que es comun
		//cuando la factura no lleva recargo contplus exige no se rellene porcentaje recargo o se ponga cero. 
		//cuando tiene recargo pero se desglosa 
		String sql="select 	asiento.rdn as rdn_asiento, \n"+
							"asiento.fecha, \n" +
							"apunte.rdn as rdn_apunte,\n"+
							"apunte.debe, \n"+
							"apunte.haber, \n"+
							"apunte.base, \n"+
							"apunte.concepto,\n"+
							"asiento.\"documento_contableIdto\",\n"+
							"fact.rdn,\n" +
							"iva.porcentaje_iva, \n"+
							"case when fact.recargo <>0 then iva.porcentaje_recargo else 0 end as porcentaje_recargo, \n"+
							"apunte.idto, \n" + 
							"cc_subcuenta.codigo_cuenta,\n"+
							"cc_subcuenta.detalle,\n"+
							"cc_contrapartida.codigo_cuenta,\n"+
							"cc_contrapartida.detalle, \n" + 
							"suj.nombre as razonsoc,\n"+
							"suj.\"NIF-CIF-VAT\" as nif,\n"+
							"suj.\"dirección\" as direccion, \n"+
							"suj.\"código_postal\" as cp,\n"+
							"loc.rdn as localidad,\n"+
							"prov.rdn as provincia  \n"+
		
		"	from \n"+
		"	v_asiento_base as asiento 																								left join			 	\n" +
		"   v_factura as fact 						on(	fact.\"tableId\"=asiento.\"documento_contableId\" and 												\n" +
		"												fact.idto=asiento.\"documento_contableIdto\")								inner join 				\n"+
		"	mi_empresa as me 						on(asiento.mi_empresa=me.\"tableId\") 											inner join				\n" + 
		" 	\"v_asiento#apunte\" as aa 				on(asiento.\"tableId\"=aa.\"asientoId\") 										inner join				\n" + 
		"   \"v_apunte\" as apunte 					on(aa.\"apunteId\"=apunte.\"tableId\" and aa.\"apunteIdto\"=apunte.idto) 		left join				\n" + 
		"	tipo_iva as iva 						on(apunte.iva=iva.\"tableId\") 													left join				\n" +
		"	\"cuenta_contable\" as cc_subcuenta 	on(apunte.subcuenta=cc_subcuenta.\"tableId\") 									left join				\n" + 
		"	\"cuenta_contable\" as cc_contrapartida on(apunte.contrapartida=cc_contrapartida.\"tableId\") 							inner join				\n" +
		"	v_sujeto_comercial as suj 				on((suj.\"tableId\"=fact.cliente or suj.\"tableId\"=fact.proveedor) and" +
		"											   (suj.idto=fact.\"clienteIdto\" or suj.idto=fact.\"proveedorIdto\"))			left join				\n"+
		"	localidad as loc 						on(loc.\"tableId\"=suj.localidad)												left join				\n"+
		"	provincia as prov 						on(prov.\"tableId\"=suj.\"provincia\" and suj.\"provinciaIdto\"=201)									\n"+
		
		"where (asiento.fecha >= ("+segundosFechaInicio+"::varchar)::bigint) " + 
		"and (asiento.fecha <= ("+segundosFechaFin+"::varchar)::bigint) " + 
		"and '"+rdnMiEmpresa+"'=me.rdn "+
		"order by asiento.fecha asc, asiento.rdn, apunte.idto asc";//casualmente el idto apunte cargo es el mayor, que es el que interesa el ultimo para redondear y cuadrar
		//Charset iso88591charset = Charset.forName("ISO-8859-1");
		//byte[] res=sql.getBytes(iso88591charset);
		//return new String(res,iso88591charset);
		//System.err.println("sql:"+sql);
		return sql;
	}
	
	
	/**
	 * Construye el sql para consultar las ventas de un agente comercial obteniendo informacion agregada sobre los productos vendidos por ese comercial, que cantidad de cada producto, y el importe de venta 
	 * @param comercial
	 * @param fehaInicio
	 * @param fechaFin
	 * @return
	 */
	private static String ventasComercial(String listaComercial,Date fehaInicio,Date  fechaFin){
		//int comercialId = QueryConstants.getTableId(comercial.getValue());
		//int comercialIdto = comercial.getValueCls();
		long segundosFechaInicio=(long)fehaInicio.getTime()/Constants.TIMEMILLIS;
		long segundosFechaFin=(long)fechaFin.getTime()/Constants.TIMEMILLIS;		

		
		String sql= "select agente,agenteidto,colaborador,colaboradoridto,comision,comisionidto,sum(cantidad),sum(importebase),sum(importecol),sum(importeexclusiva) from \n"+
				"		(select cli.\"agente_comercial\" as agente,cli.\"agente_comercialIdto\" as agenteidto,\n"+
				"fact.\"agente_comercial\" as colaborador,fact.\"agente_comercialIdto\" as colaboradoridto,la.\"comisión\" as comision,la.\"comisiónIdto\" as comisionidto, la.cantidad as cantidad, \n" +
				"la.importe*(1-factor_descuento_global) as importebase,\n"+
				"0 as importecol,\n" +
				"case when la.margen_beneficio BETWEEN \"margen_mínimo\" AND \"margen_máximo\"  then la.importe*(1-factor_descuento_global)*pormar.porcentaje/100 \n"+
				"		else la.importe*(1-factor_descuento_global)*com.porcentaje_base/100 END as importeexclusiva \n"	+	

		"from \n"+
		"		\"v_línea_artículos\" as la 																																		inner join \n"+ 
		"		\"comisión_porcentual\" as com				on( la.\"comisión\"=com.\"tableId\" and la.\"comisiónIdto\" in (select id from clase where rdn='COMISIÓN_PORCENTUAL') ) inner join \n"+
		"		\"v_factura_a_cliente#línea_artículos\" as fla 	on(la.\"tableId\"=fla.\"línea_artículosId\" and la.idto=fla.\"línea_artículosIdto\")								inner join \n"+ 
		"		\"v_factura_a_cliente\" as fact 			on(fact.\"tableId\"=fla.\"factura_a_clienteId\" and fact.idto=fla.\"factura_a_clienteIdto\")  							inner join \n"+
		"		\"v_cliente\" as cli				on(	fact.\"clienteIdto\"=cli.\"idto\" and fact.\"cliente\"=cli.\"tableId\" and \n"+
		"												fact.\"agente_comercialIdto\"=cli.\"agente_comercialIdto\" and \n"+
		"												fact.\"agente_comercial\"=cli.\"agente_comercial\")																			left join \n"+
		"		\"porciento_función_margen\" as pormar	on( pormar.\"comisión_porcentualId\"=com.\"tableId\" )								\n"+
		
		"WHERE 	fact.agente_comercial*1000+fact.\"agente_comercialIdto\" IN("+listaComercial+") AND  \n"+
		"		fact.fecha >= "+segundosFechaInicio+"  AND fact.fecha <= "+segundosFechaFin+86400 + " \n"+		

		"UNION \n"+

		"select cli.\"agente_comercial\" as agente,cli.\"agente_comercialIdto\" as agenteidto, \n"+
		"case when fact.\"agente_comercial\" is null then 0 \n"+
		"	  else fact.\"agente_comercial\" END as colaborador, \n"+
		"case when fact.\"agente_comercialIdto\" is null then 0 \n"+
		"     else fact.\"agente_comercialIdto\" END as colaboradoridto, \n"+
		"la.\"comisión\" as comision,la.\"comisiónIdto\" as comisionidto, la.cantidad as cantidad, \n"+
		"la.importe*(1-factor_descuento_global) as importebase,\n"+
		"	case	WHEN exc.porcentaje is null and cli.agente_comercial is not null then 0 \n"+
		"			WHEN exc.porcentaje is null and cli.agente_comercial is null then la.importe*(1-factor_descuento_global)*com.\"porcentaje_base\"/100  \n"+
		"			WHEN exc.porcentaje is not null and la.margen_beneficio BETWEEN \"margen_mínimo\" AND \"margen_máximo\" THEN \n"+
		"														la.importe*(1-factor_descuento_global)*pormar.\"porcentaje\"/100*(1-exc.\"porcentaje\"/100)   \n"+
		"			ELSE la.importe*(1-factor_descuento_global)*com.\"porcentaje_base\"/100*(1-exc.\"porcentaje\"/100) END as importecol,\n"+

		"	case	\n"+
		"		WHEN exc.porcentaje is null and (pormar.\"comisión_porcentualId\" is null or \n"+
		"										 la.margen_beneficio not BETWEEN \"margen_mínimo\" AND \"margen_máximo\") then \n"+
		"																		la.importe*(1-factor_descuento_global)*com.porcentaje_base/100*cli.porcentaje_exclusividad_defecto/100 \n"+ 
		"		WHEN exc.porcentaje is null and la.margen_beneficio BETWEEN \"margen_mínimo\" AND \"margen_máximo\" then \n"+
		"																		la.importe*(1-factor_descuento_global)*pormar.porcentaje/100*cli.porcentaje_exclusividad_defecto/100 \n"+ 
		"		WHEN exc.porcentaje is not null and  la.margen_beneficio BETWEEN \"margen_mínimo\" AND \"margen_máximo\" then \n"+
		"											la.importe*(1-factor_descuento_global)*pormar.porcentaje/100*exc.porcentaje/100  \n"+
		"		ELSE la.importe*(1-factor_descuento_global)*com.porcentaje_base/100*exc.porcentaje/100  END as importeexclusiva \n"+

		"from  \n"+
		"		\"v_línea_artículos\" as la 																																					inner join \n"+ 
		"		\"comisión_porcentual\" as com					on( la.\"comisión\"=com.\"tableId\" and la.\"comisiónIdto\" in (select id from clase where rdn='COMISIÓN_PORCENTUAL') ) 		inner join \n"+		
		"		\"v_factura_a_cliente#línea_artículos\" as fla 	on(la.\"tableId\"=fla.\"línea_artículosId\" and la.idto=fla.\"línea_artículosIdto\")											inner join \n"+ 
		"		\"v_factura_a_cliente\" as fact 				on(fact.\"tableId\"=fla.\"factura_a_clienteId\" and fact.idto=fla.\"factura_a_clienteIdto\")  									inner join \n"+
		"		\"v_cliente\" as cli							on(fact.\"clienteIdto\"=cli.\"idto\" and fact.\"cliente\"=cli.\"tableId\")														left join \n"+
		"		\"exclusividad_comercial\" as exc			on((cli.\"tableId\"=exc.\"cliente_empresaId\" and cli.\"idto\" in (select id from clase where rdn='CLIENTE_EMPRESA') or \n"+
		"								   						cli.\"tableId\"=exc.\"cliente_particularId\" and cli.\"idto\" in (select id from clase where rdn='CLIENTE_PARTICULAR')) and \n"+
		"								   						(exc.\"agente_comercialAGENTE_COMERCIAL_EXTERNO\"= fact.\"agente_comercial\" and  \n"+
		"														fact.\"agente_comercialIdto\" in (select id from clase where rdn='AGENTE_COMERCIAL_EXTERNO') or \n"+ 
		"								    					exc.\"agente_comercialAGENTE_COMERCIAL_FIJO\"= fact.\"agente_comercial\" and  \n"+
		"														fact.\"agente_comercialIdto\" in (select id from clase where rdn='AGENTE_COMERCIAL_FIJO'))) 									left join  \n"+		
		"		\"porciento_función_margen\" as pormar	on( pormar.\"comisión_porcentualId\"=com.\"tableId\" ) \n"+
			
		 
		"WHERE  \n"+
		"cli.\"agente_comercial\" is null or fact.\"agente_comercial\" is null or fact.\"agente_comercial\" <> cli.\"agente_comercial\" or fact.\"agente_comercialIdto\" <> cli.\"agente_comercialIdto\"  and \n"+
		 
		
		"cli.agente_comercial is null or cli.agente_comercial*1000+cli.\"agente_comercialIdto\" IN("+listaComercial+") AND \n"+
		"fact.fecha >= "+segundosFechaInicio+"  AND fact.fecha <= "+segundosFechaFin+86400 +" ) as ventas \n"+
		"group by comision,comisionidto ,agente,agenteidto,colaborador, colaboradoridto \n"+ 
		"having agente<>0 and agente is not null or colaborador<>0 and colaborador is not null";
		
		//System.err.println("sql:"+sql);
		return sql;
	}
	

	/**********************************************************************************************************
	***********************************************************************************************************
	 					CARGA EN MOTOR DE LOS INDIVIDUOS COMPLETOS QUE SON RESULTADOS DE LA QUERY (SE CARGAN
	 					EN EL FORMATO DE UN FACT POR CADA PROPIEDAD, DATVALUE o OBJVALUE Y SE CARGA
	 					TODA LA INFORMACION DEL INDIVIDUO.
	 					
	 					!!IMPLEMENTACION INTERNA DEBERIA SER MEJORADA: FUNCIONA LANZANDO QUERY A BBDD SERVER PARA 
	 					OBTENER LOS IDOS-IDTOS DE LOS INDIVIDUOS QUE CUMPLEN LA QUERY Y LUEGO LANZA A SERVER UNA PETICION
	 					DE CARGA DE ESOS IDOS---> SERVER DEBE PROPORCIONAR UN METODO QUE RECIBA EL SQL Y DEVUELVA LOS FACTS
	 					DE LOS INDIVIDIDUOS!! 
	************************************************************************************************************
	***********************************************************************************************************/
	public static HashSet<IDIndividual> loadIndividualsWithSatisficedQuery(DocDataModel ddm, String sql,int profundidad) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
	
		HashSet<IDIndividual> idosToLoad=new HashSet<IDIndividual>();
		IteratorQuery iq = ddm.getServer().serverGetIteratorQuery(sql,false);
		while(iq.hasNextRow()) {
			iq.nextRow();
			int tableId = Integer.parseInt(iq.nextColumnValue());
			int idto = Integer.parseInt(iq.nextColumnValue());
			int ido = QueryConstants.getIdo(tableId, idto);
			idosToLoad.add(new Domain(ido,idto));
			
		}
		
		
		
		//System.err.println("\n\n DEBUG loadIndividualsWithSatisficedQuery profundidad:"+profundidad+"  sql="+sql+"\n idos que cumplen query:"+idosToLoad);
		if(idosToLoad.size()>0){
			ddm.getDataModelAdapter().ruleGetFromServer(idosToLoad, profundidad, false,false);
		}
		return idosToLoad;
	}


	public static List<List<String>> executeQuery(DocDataModel ddm,String sql,boolean update) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		 System.err.println("\n..........inicio QUERY.executequery sql\n"+sql);
		
		 docServer s= ddm.getServer();
		 if(s==null){
			 System.err.println("SERVER NULO POSIBLEMENTE POR SER UTASK GLOBAL");
			 return null;
		 }
		 else System.err.println("SERVER CLASS "+s.getClass().getName());
		 IteratorQuery it = s.serverGetIteratorQuery(sql,update);
		 List<List<String>> result = new LinkedList<List<String>>();
		 while (it.hasNextRow()){
			 it.nextRow();
			 List<String> row = new LinkedList<String>();
			 while (it.hasNextColumn()){
				 row.add(it.nextColumnValue());
			 }
			 result.add(row);
		 }
		 //System.err.println("\nfin QUERY.executequery result"+result);
		 return result;
	 }
	 
	 public static List<List<String>> executeQuery(DocDataModel ddm,String nameQuery,ArrayList parametros) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		 return Query.executeQuery(ddm,Query.getSQLOfQuery(nameQuery, parametros),false);
	 }
	 public static ArrayList  getResultsAssociatedToQuery(DocDataModel ddm,String nameQuery,ArrayList parametros) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		 return getResultsAssociatedToQuery(ddm,nameQuery,parametros,null);
	 }
	 
	 private static String getCodeCuenta(int numDig,String cuenta,String detalle) throws IncompatibleValueException{
		 String relleno="00000000000000000";
		 if(cuenta.length()+detalle.length()>numDig)
			 if(detalle.charAt(detalle.length()-1)=='0') detalle=detalle.substring(0,detalle.length()-1);
			 else throw new IncompatibleValueException("La cuenta "+cuenta+" "+detalle +" excede el tamaño configurado");
		 String res= cuenta +relleno.substring(0,numDig - cuenta.length()-detalle.length())+detalle;
		 System.err.println(" DEBUG2 CUENTA:"+cuenta+","+detalle+","+res);
		 return res;
		 //return  cuenta+String.format("%1$0" + (numDig - cuenta.length()) + "d", detalle);
	 }
	 
	 public static ArrayList  getResultsAssociatedToQuery(DocDataModel ddm,String nameQuery,ArrayList parametros,String config) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		 ArrayList<Object> result=new ArrayList<Object> ();
		 List<List<String>> resultadosbbdd=Query.executeQuery(ddm, nameQuery, parametros);
		 int currApunte=0;
		 boolean esApunteVenta=false;
		 double importeTotalCuadreDebe=0.0;
		 double importeTotalCuadreHaber=0.0;
		 
		 for(int i=0;i<resultadosbbdd.size();i++){
			 List<String> columnasfilai=resultadosbbdd.get(i);
			 Object objetoRepresentaFila=null;
			 	if(!columnasfilai.isEmpty()){
			 		
			 		/*******QUERY infoAlbaranes (compras o ventas), se pide codigoAlbaran,fecha y cantidad
			 		 * *************************************************************************************/
			 		if(nameQuery.equals(Query.infoComprasAlbaranArticulo)||nameQuery.equals(Query.infoVentasAlbaranArticulo)){
			 			ArrayList<String> arrayCodigoAlbaranFechaCantidad=new ArrayList<String>();
			 			//columna 0 fecha (se obtiene bbdd segundos 1970 y transformamos a string con frase fecha entendible usuario; 
						Calendar calendar = Calendar.getInstance();
				   		calendar.setTimeInMillis(Long.valueOf(columnasfilai.get(0))*Constants.TIMEMILLIS);
				   		String fechaAmigable=calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
				   		arrayCodigoAlbaranFechaCantidad.add(fechaAmigable);
				   		//columna 1 codigoAlbaran
			 			arrayCodigoAlbaranFechaCantidad.add(columnasfilai.get(1));				   		
				   		//columna 2 cantidad
				   		arrayCodigoAlbaranFechaCantidad.add(columnasfilai.get(2));
				   		//columna 3 precio
				   		arrayCodigoAlbaranFechaCantidad.add(columnasfilai.get(3));				   		
				   		objetoRepresentaFila=arrayCodigoAlbaranFechaCantidad;
			 		}
			 		/*******QUERY ultimas3VentasPorCliente**********************************
			 		 * *********************************************************************/
			 		else if(nameQuery.equals(Query.ultimas3VentasPorCliente)){
			 			ArrayList<String> arrayFechaPrecioCantidad=new ArrayList<String>();
			 			//columna 0 fecha (se obtiene bbdd segundos 1970 y transformamos a string con frase fecha entendible usuario; 
						Calendar calendar = Calendar.getInstance();
				   		calendar.setTimeInMillis(Long.valueOf(columnasfilai.get(0))*Constants.TIMEMILLIS);
				   		String fechaAmigable=calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR);
				   		arrayFechaPrecioCantidad.add(fechaAmigable);
				   		//columna 1 precio venta
				   		arrayFechaPrecioCantidad.add(columnasfilai.get(1));
				   		//columna 2 precio con iva				   		
				   		arrayFechaPrecioCantidad.add(columnasfilai.get(2));
				   		//columna 3 cantidad
				   		arrayFechaPrecioCantidad.add(columnasfilai.get(3));
				   		objetoRepresentaFila=arrayFechaPrecioCantidad;
			 		}
			 		else if(nameQuery.equals(Query.cantidadesPrecioArticuloProyecto)||nameQuery.equals(Query.cantidadesProgramadasPrecioArticuloProyecto)){
			 			//el proyecto es un parametro que se pasa a la query
			 			ObjectValue proyecto=(ObjectValue)parametros.get(0);
			 			int idTableArticulo=Integer.parseInt(columnasfilai.get(0));
			 			int idtoArticulo=Integer.parseInt(columnasfilai.get(1));
			 			int idoArticulo=QueryConstants.getIdo(idTableArticulo, idtoArticulo);
			 			Double cantidad=Double.valueOf(columnasfilai.get(2));
			 			Double precio=Double.valueOf(columnasfilai.get(3));	
			 			boolean incurrido=nameQuery.equals(Query.cantidadesPrecioArticuloProyecto);
			 			objetoRepresentaFila=new ArticuloProyecto(idoArticulo,idtoArticulo,cantidad,precio,proyecto.getValue(),incurrido,ddm); 
			 		}
			 		else if(nameQuery.equals(Query.infoVentasComercial)){
			 			//el proyecto es un parametro que se pasa a la query
			 			//ObjectValue comercial=(ObjectValue)parametros.get(0);			 						 			
			 			Date fechaMin =(Date)parametros.get(1);
			 			Date fechaMax =(Date)parametros.get(2);
			 			
			 			int idoAgente=0;
			 			int idoColaborador=0;
			 			if(columnasfilai.get(0)!=null ){
			 				int idTableAgente=Integer.parseInt(columnasfilai.get(0));
			 				int idtoAgente=Integer.parseInt(columnasfilai.get(1));
			 				idoAgente=QueryConstants.getIdo(idTableAgente,idtoAgente);
			 			}
			 			if(columnasfilai.get(2)!=null ){
			 				int idTableColaborador=Integer.parseInt(columnasfilai.get(2));
			 				int idtoColaborador=Integer.parseInt(columnasfilai.get(3));			 				
			 				idoColaborador=QueryConstants.getIdo(idTableColaborador,idtoColaborador);
			 			}
			 			int idTableComision=Integer.parseInt(columnasfilai.get(4));
			 			int idtoComision=Integer.parseInt(columnasfilai.get(5));
			 			int idoComision=QueryConstants.getIdo(idTableComision, idtoComision);
			 			Double cantidad=Double.valueOf(columnasfilai.get(6));
			 			Double importeBase=Double.valueOf(columnasfilai.get(7));			 			
			 			Double importeColaborador=Double.valueOf(columnasfilai.get(8));
			 			Double importeExclusiva=Double.valueOf(columnasfilai.get(9));
			 			if(importeExclusiva!=0 || importeColaborador!=0  )
			 				objetoRepresentaFila=new VentasComercial( idoComision, idtoComision,  cantidad,importeBase,  importeColaborador,importeExclusiva, idoColaborador,idoAgente,  fechaMin,  fechaMax, ddm); 
			 		}
			 		else if(nameQuery.equals(Query.exportarContaPlus)){
			 			Apunte ap=new Apunte();
			 			//Fecha:
			 			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			 			int  numeroAsiento=Integer.parseInt(columnasfilai.get(0));
			 			if(numeroAsiento!=currApunte){
			 				//reset
			 				importeTotalCuadreDebe=0.0;
			 				importeTotalCuadreHaber=0.0;
			 				currApunte=numeroAsiento;
			 			}			 			
			 			
			 			long milis = (long)Double.parseDouble(columnasfilai.get(1));
			 			Date fecha = new Date(milis*Constants.TIMEMILLIS);
			 			String fechaS = formatter.format(fecha);				
			 			
			 			int numDigitos=new Double(config).intValue();
			 			//Subcuenta:
			 			String ssubCuenta = columnasfilai.get(12);			 					 						 			
			 			ssubCuenta= Query.getCodeCuenta(numDigitos,ssubCuenta, columnasfilai.get(13));			 						 							 				
		 				//Concepto
		 				String concepto = columnasfilai.get(6);
		 					 				
		 				int idtoDocContable=columnasfilai.get(7)==null?0:new Integer(columnasfilai.get(7));
		 			
		 				if(idtoDocContable==124 || idtoDocContable==173 || idtoDocContable==116 ||idtoDocContable==319){
		 					System.err.println(" DEBUG5 dentro");	
		 					ap.modificarCampo("numeroFacturaAlIva", columnasfilai.get(8));
		 				}

			 			ap.modificarCampo("numeroAsiento", numeroAsiento);
			 			ap.modificarCampo("fecha", formatter.format(fecha));
			 			ap.modificarCampo("subCuenta", ssubCuenta);			 			
			 			SubCuenta subCuenta=new SubCuenta();
			 			subCuenta.modificarCampo("codigo", ssubCuenta);
			 			if(columnasfilai.get(16)!=null){//es obligatorio, pero inicialmente no se esta informando al crear las cuentas contables
			 				subCuenta.modificarCampo("titulo", columnasfilai.get(16));	
			 			}
			 			else{
			 				subCuenta.modificarCampo("titulo", "titulo cuenta contable");
			 			}
			 			subCuenta.modificarCampo("nif", columnasfilai.get(17));	
			 			
			 			subCuenta.modificarCampo("domicilio", columnasfilai.get(18));				 			
			 			subCuenta.modificarCampo("codigoPostal", columnasfilai.get(19));
			 			if(columnasfilai.get(20)!=null) subCuenta.modificarCampo("poblacion", columnasfilai.get(20));	
			 			if(columnasfilai.get(21)!=null) subCuenta.modificarCampo("provincia", columnasfilai.get(21));	
			 			
			 			
			 			SubCuenta contrapartida=null;
			 			//Contrapartida 
			 			if(columnasfilai.get(14) != null){
			 				String scontrapartida=columnasfilai.get(14);

			 				scontrapartida= Query.getCodeCuenta(numDigitos,scontrapartida,columnasfilai.get(15));			 							 				
			 				
			 				ap.modificarCampo("contrapartida", scontrapartida);
			 				contrapartida=new SubCuenta();
			 				contrapartida.modificarCampo("codigo", scontrapartida);
				 			if(columnasfilai.get(14)!=null){//es obligatorio, pero inicialmente no se esta informando al crear las cuentas contables
				 				contrapartida.modificarCampo("titulo", columnasfilai.get(14));	
				 			}
				 			else{
				 				contrapartida.modificarCampo("titulo", "titulo cuenta contable");
				 			}
			 			}

			 			Double debe = Auxiliar.redondea(Double.parseDouble(columnasfilai.get(3)),2);
			 			Double haber =Auxiliar.redondea(Double.parseDouble(columnasfilai.get(4)),2);
			 			Double base = Auxiliar.redondea(Double.parseDouble(columnasfilai.get(5)),2);		 			
			 			
			 			ap.modificarCampo("importeDebePts", debe*166.386);
		 				ap.modificarCampo("concepto", concepto);
		 				ap.modificarCampo("importeHaberPts", haber*166.386);		
		 				
		 				importeTotalCuadreDebe+=haber;//solo lo uso en apuntes de ventas
		 				importeTotalCuadreHaber+=debe;//solo lo uso en apuntes de ventas
		 				
		 				//CAMPOS EXCLUSIVOS DE APUNTES DE IVA, RE 
			 			if(columnasfilai.get(9) != null){
			 				esApunteVenta=true;//siempre se llamara antes de llegar al apunte de cargo que cuadra el asiento
			 				
			 				String tipoApunte=ddm.getClassName(Integer.parseInt(columnasfilai.get(11)));
				 			//if(tipoApunte.equals("APUNTE_RE")){
			 				if(columnasfilai.get(10)!=null){
				 				Double porcentajeRecargo =Double.parseDouble(columnasfilai.get(10));
			 					ap.modificarCampo("porcentajeRE", porcentajeRecargo);
				 				subCuenta.modificarCampo("recEquiv",porcentajeRecargo);				 				
				 			}
				 			
				 			//else{				 			
				 				ap.modificarCampo("baseImponibleIVAPts", Auxiliar.redondea(base*166.386,2));
				 				ap.modificarCampo("baseIVAEuros", base);
				 				Double porcentajeIva=Double.parseDouble(columnasfilai.get(9));
				 				ap.modificarCampo("porcentajeIVA", porcentajeIva);
			 					subCuenta.modificarCampo("tpc", porcentajeIva); //tpc es tipo porcentaje cuenta
				 			//}			 						 							 				
		 					subCuenta.modificarCampo("tipoIVA", "G");//ver tabla doc contaplus "REPERCUTIDO - DEVENGADO", G significa Devengado en Régimen General		 							 								 				
			 			}
			 			
			 			System.err.println(" DEBUG6 esventa "+esApunteVenta+" asiento "+numeroAsiento+" debe haber base cuadre "+debe+" "+haber+" "+base);	
			 			//Moneda uso -> Euros 
		 				ap.modificarCampo("monedaUso", "2");
		 				if(esApunteVenta && debe!=0.0 && importeTotalCuadreDebe!=0.0){
		 					//notar que el cuadre evita problemas de reondeo y esta basado en haber ordenado los apuntes por idto que casualmente situa el apunte de cargo con el total en ultima posicion
		 					//en dicho apunte de cargo se da que todo el haber estaba relleno en apuntes ateriores, y es cero en actual y debe distinto de cero que queremos ajustar redondeos
		 					System.err.println("ajustando debe previo "+debe+" post:"+importeTotalCuadreDebe);
		 					debe=importeTotalCuadreDebe;
		 				}
		 				if(esApunteVenta && haber!=0.0 && importeTotalCuadreHaber!=0.0){
		 					//ver nota ajuste caso anterior
		 					System.err.println("ajustando haber previo "+haber+" post:"+importeTotalCuadreHaber);
		 					haber=importeTotalCuadreHaber;
		 				}
		 				ap.modificarCampo("importeDebeEuros", debe);
		 				ap.modificarCampo("importeHaberEuros", haber);
		 				System.err.println(" DEBUG7 esventa "+esApunteVenta+" asiento "+numeroAsiento+" debe haber base cuadre "+debe+" "+haber+" "+base);	
		 				ap.setSubcuenta(subCuenta);
		 				if(contrapartida!=null)
		 					ap.setContrapartida(contrapartida);
			 			
		 				//TODO CAMPOS SOBRE FACTURAS RECTIFICATIVAS O RECTIFICADAS
		 				//33-38
		 				
		 				//TODO CAMPOS TERCEROS 61-64
			 							


			 			//String rdnApunte = columnasfilai.get(3);
			 			
			 			

			 			


			 				//Creamos la linea, la escribimos al final del fichero y devolvemos la lista de rdn's de cuentas contables
			 				objetoRepresentaFila=ap;
			 			}
			 		

			 			
			 		}
			 		
			 		else{
			 			System.err.println("\n\n ERRROR: QUERY.getResultsAssociatedToQuery NO IMPLEMENTA NAMEQUERY:"+nameQuery);
			 			return null;
			 		}
			 		if(objetoRepresentaFila!=null) result.add(objetoRepresentaFila);	
				 }
		 //System.err.println("\n\n DEBUG RESULTADO GETRESULTOFQUERY\n "+Auxiliar.arrayToStringConSaltoLinea(result));
		 return result;
	 }
}


