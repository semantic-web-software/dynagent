/***
 * XMLConverter.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.server.ejb;


import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.Mask;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.Required;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.communication.IndividualData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;

public class XMLConverter {

	private Element root=null;

	private int idtoFicticio = -1;
	private int propFicticia = -1;
	private HashMap<String,Integer> mapNameUtaskIdto = new HashMap<String, Integer>();
	private HashMap<String,Integer> mapNameReportIdto = new HashMap<String, Integer>();
	private HashMap<Integer,HashSet<String>> mapTargetClassFunctionalArea = new HashMap<Integer, HashSet<String>>();
	private HashMap<String,Integer> mapNameGroups=new HashMap<String, Integer>();

	public Element getRoot() {
		return root;
	}

	public void setRoot(Element root) {
		this.root = root;

	}

	public void convert(FactoryConnectionDB factConnDB, DataBaseMap dataBaseMap, int business, String name, String user, HashSet<String> roles, HashSet<Integer> idoRoles, ArrayList<OrderProperty> orderPropertyList, 

		ArrayList<CardMed> cmList, IndividualData iData, int min, int max, boolean metacomclient, IKnowledgeBaseInfoServer ik, boolean configurationMode) throws IOException, JDOMException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{

		root=new Element("METADATA");
		
		root.setAttribute("BUSINESS", new Integer(business).toString());

		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterBegin();
		
		//System.out.println("idoRoles " + Auxiliar.hashSetIntegerToString(idoRoles, ","));
		//System.out.println("roles " + Auxiliar.hashSetStringToString(roles, ","));
		HashSet<Integer> excludeIdtosUTask = new HashSet<Integer>();
		String sqlInstances = "select idto, " + cB + "value" + cE + 
			" from instances where property=" + Constants.IdPROP_USERROL + " and op='" + Constants.OP_ONEOF + "'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlInstances);
			ArrayList<Integer> removeExcludeIdtosUTask = new ArrayList<Integer>();
			while (rs.next()) {
				Integer value = rs.getInt(2);
				if(value!=null && !idoRoles.contains(value))
					excludeIdtosUTask.add(rs.getInt(1));
				else removeExcludeIdtosUTask.add(rs.getInt(1));
			}
			//Borramos de la lista de excluidas las usertask que hemos encontrado que tienen otro userRol que si cumple con el userRol del usuario. Esto es necesario ya que aceptamos UserTasks con mas de un UserRol
			excludeIdtosUTask.removeAll(removeExcludeIdtosUTask);
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		//System.out.println("excludeIdtosUTask " + Auxiliar.arrayIntegerToString(excludeIdtosUTask, ","));

		Element ins = new Element("INSTANCES");
		root.addContent(ins);
		sqlInstances = "SELECT ins.idto, ins.ido, ins.property, ins.value, ins.valuecls, ins.qmin, ins.qmax, ins.name, ins.op, ins.virtual FROM instances ins ";
		if(!configurationMode){
			sqlInstances+="INNER JOIN v_propiedad prop ON (ins.property=prop.id) " +
			"INNER JOIN clase c1 ON (ins.idto=c1.id) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " exc_class ON (c1." + cB + "tableId" + cE + "=exc_class.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " exc_prop ON ((prop." + cB + "tableId" + cE + "=exc_prop." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND prop.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (prop." + cB + "tableId" + cE + "=exc_prop." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND prop.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " exc_class_prop ON (c1." + cB + "tableId" + cE + "=exc_class_prop.dominio AND ((prop." + cB + "tableId" + cE + "=exc_class_prop." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND prop.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (prop." + cB + "tableId" + cE + "=exc_class_prop." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND prop.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE exc_class." + cB + "tableId" + cE + " IS NULL AND exc_prop." + cB + "tableId" + cE + " IS NULL AND exc_class_prop." + cB + "tableId" + cE + " IS NULL AND (ins.valuecls not in( " +
				"SELECT c2.id FROM clase c2 " +
				"INNER JOIN " + cB + "clase_excluída" + cE + " aux ON (c2." + cB + "tableId" + cE + "=aux.dominio)) OR ins.valuecls IS NULL) " +
			"AND ins.idto not in(" +
				"SELECT idto FROM instances ins_act " +
				"INNER JOIN clase c on (ins_act.valuecls=c.id) " + 
				"INNER JOIN " + cB + "clase_excluída" + cE + " exc_act_class on (exc_act_class.dominio=c." + cB + "tableId" + cE + ") " +
				"WHERE property IN (" + Constants.IdPROP_SOURCECLASS + "," + Constants.IdPROP_TARGETCLASS + ")) " +
			"ORDER BY ins.idto, ins.property";
		}
		System.out.println("INSTANCES QUERY: " + sqlInstances);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlInstances);
			while (rs.next()) {
				Element instance= new Element("INSTANCE");
				boolean add = true;
				Integer idto = rs.getInt(1);
				if (rs.wasNull()) idto = null;
				if(idto!=null) {
					instance.setAttribute("IDTO",String.valueOf(idto));
					if (excludeIdtosUTask.contains(idto)) {
						add = false;
					}
				}
				if (add) {
					Integer ido = rs.getInt(2);
					if (rs.wasNull()) ido = null;
					Integer prop = rs.getInt(3);
					if (rs.wasNull()) prop = null;
					String value = rs.getString(4);
					Integer valueCls = rs.getInt(5);
					if (rs.wasNull()) valueCls = null;
					Integer qMin = rs.getInt(6);
					if (rs.wasNull()) qMin = null;
					Integer qMax = rs.getInt(7);
					if (rs.wasNull()) qMax = null;
					String nameInst = rs.getString(8);
					String op = rs.getString(9);
					boolean virtual=rs.getBoolean(10);
					
					if(ido!=null)
						instance.setAttribute("IDO",String.valueOf(ido));
					if(prop!=null)
						instance.setAttribute("PROP",String.valueOf(prop));
					if(value!=null)
						instance.setAttribute("VAL",value);
					if(valueCls!=null){
						instance.setAttribute("VALCLS",String.valueOf(valueCls));
					}
					if(qMin!=null)
						instance.setAttribute("Q_MIN",String.valueOf(qMin));
					if(qMax!= null)
						instance.setAttribute("Q_MAX",String.valueOf(qMax));
					if(op!=null)
						instance.setAttribute("OP",op);
					if(nameInst!= null)
						instance.setAttribute("NAME",nameInst);
					instance.setAttribute("VIRTUAL",String.valueOf(virtual));
					ins.addContent(instance);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		HashMap<String, Integer> fAreas = new HashMap<String, Integer>();
		sqlInstances = "select " + cB + "tableId" + cE + ", rdn from functional_area";
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlInstances);
			while (rs.next()) {
				Element instance= new Element("INSTANCE");
				
				Integer tableId = rs.getInt(1);
				int idto=Constants.IDTO_FUNCTIONAL_AREA;
				int ido=QueryConstants.getIdo(tableId, idto);
				Integer prop = Constants.IdPROP_RDN;
				String value = rs.getString(2);
				String nameInst = Constants.CLS_FUNCTIONAL_AREA;
				Integer valueCls = Constants.IDTO_STRING;
				fAreas.put(value,tableId);
				
				instance.setAttribute("IDO",String.valueOf(ido));
				instance.setAttribute("IDTO",String.valueOf(idto));
				instance.setAttribute("PROP",String.valueOf(prop));
				instance.setAttribute("VAL",value);
				instance.setAttribute("VALCLS",String.valueOf(valueCls));
				instance.setAttribute("NAME",nameInst);
					ins.addContent(instance);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		Element props = new Element("PROPERTIES");
		root.addContent(props);
		String sqlProp = "SELECT prop.id, prop.rdn, prop.cat, prop.valuecls, prop.id_inversa FROM v_propiedad prop ";
		if(!configurationMode){
			sqlProp+="LEFT JOIN " + cB + "propiedad_excluída" + cE + " exc_prop ON ((prop." + cB + "tableId" + cE + "=exc_prop." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND prop.idto=" + Constants.IDTO_DATA_PROPERTY + ") " +
																			"OR (prop." + cB + "tableId" + cE + "=exc_prop." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND prop.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"WHERE exc_prop." + cB + "tableId" + cE + " IS NULL";
		}
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlProp);
			while (rs.next()) {
				Element propertie = new Element("PROPERTY");
				
				Integer prop = rs.getInt(1);
				if (rs.wasNull()) prop = null;
				String nameProp = rs.getString(2);
				Integer cat = rs.getInt(3);
				Integer valueCls = rs.getInt(4);
				if (rs.wasNull()) valueCls = null;
				Integer propInv = rs.getInt(5);
				if (rs.wasNull()) propInv = null;

				if(prop!=null)
					propertie.setAttribute("PROP",prop.toString());
				if(nameProp!=null)
					propertie.setAttribute("NAME",nameProp);
				if(cat!=null)
					propertie.setAttribute("CAT",cat.toString());
				if(valueCls!=null)
					propertie.setAttribute("VALUECLS",valueCls.toString());
				if(propInv!= null)
					propertie.setAttribute("PROPINV", propInv.toString());
				props.addContent(propertie);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}

		Element hers = new Element("HIERARCHIES");
		root.addContent(hers);
		// Se considera que en la tabla de herencias se indica que una clase es
		// hija de si misma tal y como se trabaja ahora mismo. Si esto dejase de
		// ser asi habria que buscar que la clase padre tampoco está excluida
		// haciendo el mismo procesamiento que se hace ahora para los hijos.
		String sqlHerencias = "SELECT h.id_to, h.id_to_padre FROM t_herencias h ";
		if(!configurationMode){
			sqlHerencias+="INNER JOIN clase c ON (h.id_to=c.id) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + "ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND h.id_to NOT IN (" +
				"SELECT idto FROM instances ins_act " +
				"INNER JOIN clase c_act on (ins_act.valuecls=c_act.id) " + 
				"INNER JOIN " + cB + "clase_excluída" + cE + " ce_act on (ce_act.dominio=c_act." + cB + "tableId" + cE + ") " +
				"WHERE ins_act.property IN (" + Constants.IdPROP_SOURCECLASS + "," + Constants.IdPROP_TARGETCLASS + ")) ";
		}
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlHerencias);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				if (rs.wasNull()) idto = null;
				Integer idtoPadre = rs.getInt(2);
				if (rs.wasNull()) idtoPadre = null;

				if (!excludeIdtosUTask.contains(idto)) {
					Element herencia = new Element("HIERARCHY");
					herencia.setAttribute("ID_TO",idto.toString());
					herencia.setAttribute("ID_TO_PARENT",idtoPadre.toString());
					hers.addContent(herencia);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}

		Element accesses = new Element("ACCESSES");
		root.addContent(accesses);
		
		Element listalias=new Element("LISTALIAS");
		root.addContent(listalias);
		
		Element listGroups = new Element("GROUPS");
		root.addContent(listGroups);
		
		Element listenerUtasks = new Element("LISTENERUTASKS");
		root.addContent(listenerUtasks);
		
		Element globalUtasks = new Element("GLOBALUTASKS");
		root.addContent(globalUtasks);
		
		
		//UTASK
		LinkedList<Instance> lInstUTask = getInstancesByUTask(factConnDB);
		String sqlUtasks = "select ut.rdn, c.id, ut." + cB + "functional_areaId" + cE + ", ut.functional_area, " +
						"ut.color_rojo, ut.color_verde, ut.color_azul, ut." + cB + "minutos_actualización" + cE + ", ut." + cB + "global" + cE + 
						" from " + cB + "s_utask" + cE + " as ut " + 
						"inner join clase as c on(c." + cB + "tableId" + cE + "=ut." + cB + "dominioId" + cE + ") ";
		if(!configurationMode){
			sqlUtasks+="LEFT JOIN " + cB + "clase_excluída" + cE + " exc_class ON (ut." + cB + "dominioId" + cE + "=exc_class.dominio) " +
			"WHERE exc_class." + cB + "tableId" + cE + "IS NULL and ut.functional_area not ilike 'Configuración_Avanzada'";
			
				sqlUtasks+=" AND ut." + cB + "functional_areaId" + cE + " not in (select functionalarea from s_access" +
					" where functionalarea is not null and (" + cB + "user" + cE + " is null or " + cB + "user" + cE + "='"+user+"') and" +
							"(userrol is null or userrol in(";
				Iterator<String> itrUserRol=roles.iterator();
				while(itrUserRol.hasNext()){
					sqlUtasks+="'"+itrUserRol.next()+"'";
					if(itrUserRol.hasNext()){
						sqlUtasks+=",";
					}
				}
				sqlUtasks+=")))";
		}else{
			sqlUtasks+="WHERE ut.functional_area ilike 'Configuración_Avanzada'";
		}
		System.out.println(sqlUtasks);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlUtasks);
			while (rs.next()) {
				String rdn = rs.getString(1);
				String nameUserTask = rdn.contains("Task_")?rdn:("Task_" + rdn);
				Integer dominio = rs.getInt(2);
				Integer idoFunctionalArea = QueryConstants.getIdo(rs.getInt(3),Constants.IDTO_FUNCTIONAL_AREA);
				if (rs.wasNull()) idoFunctionalArea = null;
				String functionalArea = rs.getString(4);
				Integer rColor=rs.getInt(5);
				if (rs.wasNull()) rColor = null;
				Integer gColor=rs.getInt(6);
				Integer bColor=rs.getInt(7);
				Integer color=rColor!=null?new Color(rColor,gColor,bColor).getRGB():null;
				Integer minutes=rs.getInt(8);
				boolean global=rs.getBoolean(9);
				if(global){
					Element globalUtask = new Element("GLOBALUTASK");
					Integer idtoUserTask = idtoFicticio;
					globalUtask.setAttribute("UTASK",String.valueOf(idtoUserTask));
					globalUtasks.addContent(globalUtask);
				}
				
				mapNameUtaskIdto.put(rdn, idtoFicticio);
				HashSet<String> setTargetClassFunctionalArea = mapTargetClassFunctionalArea.get(dominio);
				if(setTargetClassFunctionalArea==null) {
					setTargetClassFunctionalArea = new HashSet<String>();
					mapTargetClassFunctionalArea.put(dominio, setTargetClassFunctionalArea);
				}
				setTargetClassFunctionalArea.add(functionalArea);
				
				ArrayList<Instance> listIns = new ArrayList<Instance>();
				ArrayList<T_Herencias> listHer = new ArrayList<T_Herencias>();
				ArrayList<Access> listAcc = new ArrayList<Access>();
				LinkedList<Groups> listGr = new LinkedList<Groups>();
				LinkedList<Properties> listPr = new LinkedList<Properties>();
				ArrayList<ListenerUtask> listListenerUtask = new ArrayList<ListenerUtask>();
				addInstancesUserTask(factConnDB, lInstUTask, listIns, listHer, listListenerUtask, nameUserTask, dominio, idoFunctionalArea, functionalArea, color, minutes); 
				
				objectsToElements(listIns, listHer, listAcc, listGr, listPr, listListenerUtask, hers, ins, accesses, listGroups, props, listenerUtasks);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		//REPORTS
		if(!configurationMode){
			LinkedList<Instance> lInstReport = getInstancesByReport(factConnDB);
			HashMap<String, Integer> hFormat = getMapFormat(factConnDB);
			String sqlReports = "select rep." + cB + "tableId" + cE + ", rep.rdn, rep." + cB + "dominio" + cE + ", rep.functional_area, rep." +
			cB + "impresión_directa" + cE + ", rep.vista_previa, rep.formato_informe, rep." + cB + "confirmar_impresión" + cE + ", rep." + cB +"copias_impresión" + cE +	
				",impresora from s_report rep ";
			if(!configurationMode){
				sqlReports+= "INNER JOIN clase c ON (rep.dominio=c.id) " +
					" LEFT JOIN " + cB + "clase_excluída" + cE + " exc_class ON (c." + cB + "tableId" + cE + "=exc_class.dominio)" +
					" WHERE exc_class." + cB + "tableId" + cE + " IS NULL";
			}
			System.out.println(sqlReports);
			try {
				con = factConnDB.createConnection(true);
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sqlReports);
				while (rs.next()) {
					Integer tableId = rs.getInt(1);
					String rdn = rs.getString(2);
					String nameRep = rdn.contains("rp@")?rdn:("rp@" + rdn);
					int dominio = rs.getInt(3);
					HashSet<Integer> dominioSpec = AuxiliarModel.getSpecialized(dominio, factConnDB);
					String functionalArea = rs.getString(4);
					if(rs.wasNull()) functionalArea = null;
					Boolean impresionDirecta = rs.getBoolean(5);
					Boolean vistaPrevia = rs.getBoolean(6);
					String formatoInforme = rs.getString(7);
					Boolean confirmarImpresion = rs.getBoolean(8);
					Integer copies = rs.getInt(9);
					String impresora = rs.getString(10);
					mapNameReportIdto.put(rdn, idtoFicticio);
					
					ArrayList<Instance> listIns = new ArrayList<Instance>();
					ArrayList<T_Herencias> listHer = new ArrayList<T_Herencias>();
					ArrayList<Access> listAcc = new ArrayList<Access>();
					LinkedList<Groups> listGr = new LinkedList<Groups>();
					LinkedList<Properties> listPr = new LinkedList<Properties>();
					ArrayList<ListenerUtask> listListenerUtask = new ArrayList<ListenerUtask>();
					addInstancesReport(factConnDB, lInstReport, fAreas, hFormat, listIns, listHer, listAcc, listGr, listPr, tableId, nameRep, 
							dominio, dominioSpec, functionalArea!=null?Auxiliar.stringToHashSetString(functionalArea, ","):null, 
							impresionDirecta, vistaPrevia, Auxiliar.stringToHashSetString(formatoInforme, ","), confirmarImpresion, copies, impresora,configurationMode, factConnDB, dataBaseMap);
					
					objectsToElements(listIns, listHer, listAcc, listGr, listPr, listListenerUtask, hers, ins, accesses, listGroups, props, listenerUtasks);
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}
		
//		//ACCIONES TODO MODIFICAR CUANDO ACCIONES YA NO ESTE EN CLASES. AñadIMOS A mapUTaskNameIdto LOS IDTOS que tienen
//		
//		String sqlAcciones = "select id_to, id_to_padre from t_herencias";
//		try {
//			con = factConnDB.createConnection(true);
//			st = con.getBusinessConn().createStatement();
//			rs = st.executeQuery(sqlHerencias);
//			while (rs.next()) {
//				Integer idto = rs.getInt(1);
//				if (rs.wasNull()) idto = null;
//				Integer idtoPadre = rs.getInt(2);
//				if (rs.wasNull()) idtoPadre = null;
//
//				if (!excludeIdtosUTask.contains(idto)) {
//					Element herencia = new Element("HIERARCHY");
//					herencia.setAttribute("ID_TO",idto.toString());
//					herencia.setAttribute("ID_TO_PARENT",idtoPadre.toString());
//					hers.addContent(herencia);
//				}
//			}
//		} finally {
//			if (rs!=null)
//				rs.close();
//			if (st != null)
//				st.close();
//			if (con!=null)
//				factConnDB.close(con);
//		}
		

		ArrayList<Access> accessList=getAccesses(factConnDB, ik, configurationMode);
		Iterator<Access> itrAccess=accessList.iterator();
		while(itrAccess.hasNext()){
			Element accessElement= new Element("ACCESS");
			Access access=itrAccess.next();
			boolean accessValido = true;
//			if(access.getUSER()!=null && !access.getUSER().contains(user))
//				accessValido = false;
//			else if(access.getUSERROL()!=null && !access.getUSERROL().isEmpty()){
//				accessValido=false;
//				if(roles.size()!=0){
//					Iterator<String> itrUserRol=access.getUSERROL().iterator();
//					while(!accessValido && itrUserRol.hasNext()){
//						String userRol=itrUserRol.next();
//						if(roles.contains(userRol))
//							accessValido=true;
//					}
//				}
//			}
			
//			System.out.println("Access User->"+access.getUSER());
//			System.out.println("Access URol->"+access.getUSERROL());
//			System.out.println("User->"+user);
//			Iterator<String> itR = roles.iterator();
//			while (itR.hasNext()) {
//				String rol = itR.next();
//				System.out.println("Rol->"+rol);
//			}
//			System.out.println("accessValido->"+accessValido);
			if (accessValido) {
				String accessUser=null;
				String accessUserRol=null;
				accessElement.setAttribute("DENNIED",access.getDENNIED().toString());
				if(access.getIDTO()!=null)
					accessElement.setAttribute("IDTO",String.valueOf(access.getIDTO()));
				if(access.getTASK()!=null)
					accessElement.setAttribute("TASK",access.getTASK().toString());
				if(access.getUSERROL() != null && !access.getUSERROL().isEmpty() && !access.getUSERROL().get(0).equals("null")){
					accessUserRol=access.getUSERROL().get(0).toString();
					accessElement.setAttribute("USERROL",accessUserRol);
				}
				if(access.getUSER() != null && !access.getUSER().isEmpty() && !access.getUSER().get(0).equals("null")){
					accessUser=access.getUSER().get(0).toString();
					accessElement.setAttribute("USER",accessUser);
				}
				String accessTypeName=access.getACCESSTYPENAME().get(0);
				for(int i=1;i<access.getACCESSTYPENAME().size();i++)
					accessTypeName+=";"+access.getACCESSTYPENAME().get(i);
				accessElement.setAttribute("ACCESSTYPENAME",accessTypeName);
				if(access.getPROP()!=null && access.getPROP().length>0){
					Integer[] propArr=access.getPROP();
					if(propArr.length>1) throw new IncoherenceInMotorException("ACCESS construido para fact con multiples propiedades");
					if(propArr[0]!=null)	accessElement.setAttribute("PROP",propArr[0].toString());//Aunque esten definidos con N propiedades, el metodo getAccesses crea un acceso por cada prop
				}
				accessElement.setAttribute("PRIORITY",String.valueOf(access.getPRIORITY()));
				
				if(access.getFUNCTIONALAREA()==null){
					accesses.addContent(accessElement);
				}
			}
		}
		//Añadimos los permisos genericos
		Access a=KnowledgeAdapter.buildAllAccess(null, 0);
		ArrayList<String> accestTypeArr=a.getACCESSTYPENAME();
		Iterator<String> itrGeneric=accestTypeArr.iterator();
		while(itrGeneric.hasNext()){
			String accessTypeName=itrGeneric.next();
			Element access= new Element("ACCESS");
			access.setAttribute("DENNIED", "0");
			access.setAttribute("ACCESSTYPENAME", accessTypeName);
			access.setAttribute("PRIORITY", String.valueOf(a.getPRIORITY()));
			accesses.addContent(access);
		}
		
		//Añadimos los permisos de sistema
    	HashSet<Integer> setSystemClass=new HashSet<Integer>(Arrays.asList(Constants.LIST_SYSTEM_CLASS));
		for(int idto:setSystemClass){
			Access accessObj=KnowledgeAdapter.buildAllAccess(idto, Constants.MAX_ACCESS_PRIORITY-1);
			
			String accessTypeName=accessObj.getACCESSTYPENAME().get(0);
			for(int i=1;i<accessObj.getACCESSTYPENAME().size();i++)
				accessTypeName+=";"+accessObj.getACCESSTYPENAME().get(i);
			
			Element access= new Element("ACCESS");
			access.setAttribute("DENNIED", "0");
			access.setAttribute("IDTO",accessObj.getIDTO().toString());
			access.setAttribute("ACCESSTYPENAME", accessTypeName);
			access.setAttribute("PRIORITY", String.valueOf(accessObj.getPRIORITY()));
			accesses.addContent(access);
			
			//Le damos permiso de view y set a todas las propiedades de sistema
			String sqlPropSystem = "SELECT DISTINCT property FROM instances where idto="+accessObj.getIDTO();
			try {
				con = factConnDB.createConnection(true);
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sqlPropSystem);
				while (rs.next()) {
					Integer idProp = rs.getInt(1);
					
					access= new Element("ACCESS");
					access.setAttribute("DENNIED", "0");
					access.setAttribute("IDTO", accessObj.getIDTO().toString());
					access.setAttribute("PROP", String.valueOf(idProp));
					access.setAttribute("ACCESSTYPENAME", Constants.ACCESS_VIEW_NAME+";"+Constants.ACCESS_SET_NAME);
					access.setAttribute("PRIORITY", String.valueOf(Constants.MAX_ACCESS_PRIORITY-1));
					accesses.addContent(access);
				}
			} finally {
				if (rs!=null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}

		Element indexes = new Element("INDEXES");
		root.addContent(indexes);
		
		String sqlIndex = "SELECT DISTINCT c.id FROM clase c "+
			"INNER JOIN " + cB + Constants.CLS_INDICE.toLowerCase() + cE + " fin ON (c." + cB + "tableId" + cE + "=fin.dominio) ";
		if(!configurationMode){
			sqlIndex+="LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (fin.dominio=ce.dominio) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL";
		}
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlIndex);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				
				Element access= new Element("ACCESS");
				access.setAttribute("DENNIED", "1");
				access.setAttribute("IDTO", idto.toString());
				access.setAttribute("PROP", String.valueOf(Constants.IdPROP_RDN));
				access.setAttribute("ACCESSTYPENAME", Constants.ACCESS_SET_NAME);
				access.setAttribute("PRIORITY", String.valueOf(Constants.MAX_ACCESS_PRIORITY-1));
				accesses.addContent(access);
				
				Element index= new Element("INDEX");
				index.setAttribute("IDTO", idto.toString());
				indexes.addContent(index);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		String sqlAbstract = "SELECT c.id FROM clase c ";
		if(!configurationMode){
			sqlAbstract+="LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"WHERE c.abstracta=true";
		}else{
			sqlAbstract+="WHERE c.abstracta=true";
		}
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlAbstract);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				
				Element access= new Element("ACCESS");
				access.setAttribute("DENNIED", "0");
				access.setAttribute("IDTO", idto.toString());
				access.setAttribute("ACCESSTYPENAME", Constants.ACCESS_ABSTRACT_NAME);
				access.setAttribute("PRIORITY", String.valueOf(0));
				accesses.addContent(access);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}

		/*Element rules = new Element("RULES");
		root.addContent(rules);
		rules.addContent(new CDATA(stRules));*/

		if (roles.size()>0) {
			String stRoles = Auxiliar.hashSetStringToString(roles, ",");
			Element userRoles = new Element("USERROLES");
			userRoles.setAttribute("UROLES",stRoles);
			root.addContent(userRoles);
		}
		Element orderProperties = new Element("ORDERPROPERTIES");
		root.addContent(orderProperties);
		Iterator<OrderProperty> itr=orderPropertyList.iterator();
		while(itr.hasNext()){
			Element orderProperty= new Element("ORDERPROPERTY");
			OrderProperty orderP=itr.next();
			orderProperty.setAttribute("SEC",String.valueOf(orderP.getSec()));
			orderProperty.setAttribute("PROP",String.valueOf(orderP.getProp()));
			orderProperty.setAttribute("ORDER",String.valueOf(orderP.getOrder()));
			if(orderP.getGroup()!=null)
				orderProperty.setAttribute("GROUP",String.valueOf(orderP.getGroup()));
			if(orderP.getIdto()!=null)
				orderProperty.setAttribute("IDTO",String.valueOf(orderP.getIdto()));			
			orderProperties.addContent(orderProperty);
		}

		Element listcm=new Element("LISTCARDMED");
		root.addContent(listcm);
		Iterator<CardMed> itcm=cmList.iterator();
		while(itcm.hasNext()){
			Element cardmed=new Element("CARDMED");
			CardMed cm=itcm.next();
			cardmed.setAttribute("CM",String.valueOf(cm.getCardmed()));
			if (cm.getIdto()!=null) cardmed.setAttribute("IDTO", cm.getIdto().toString());
			if (cm.getIdtoParent()!=null) cardmed.setAttribute("IDTOPARENT", cm.getIdtoParent().toString());
			if (cm.getIdtoName()!=null) cardmed.setAttribute("IDTONAME", cm.getIdtoName().toString());
			if (cm.getIdtoParentName()!=null) cardmed.setAttribute("IDTOPARENTNAME", cm.getIdtoParentName().toString());
			if (cm.getIdProp()!=null) cardmed.setAttribute("PROP", cm.getIdProp().toString());
			if (cm.getIdPropName()!=null) cardmed.setAttribute("PROPNAME", cm.getIdPropName().toString());
			
			listcm.addContent(cardmed);
		}
		
		ArrayList<Groups> groupList= getGroups(factConnDB, ik, configurationMode);
		Iterator<Groups> itgroup=groupList.iterator();
		while(itgroup.hasNext()){
			Groups g=itgroup.next();
			Element group = new Element("GROUP");
			if(g.getIdGroup()!=null)
				group.setAttribute("ID",g.getIdGroup().toString());
			if(g.getUTask()!=null)
				group.setAttribute("TASK",g.getUTask().toString());
			if(g.getIdtoClass()!=null)
				group.setAttribute("IDTOCLASS",g.getIdtoClass().toString());
			if(g.getNameGroup()!=null)
				group.setAttribute("NAME",g.getNameGroup());
			if(g.getIdProp()!=null)
				group.setAttribute("PROP",g.getIdProp().toString());
			if(g.getOrder()!=null)
				group.setAttribute("ORDER",String.valueOf(g.getOrder()));
			if(!mapNameGroups.containsKey(g.getNameGroup())){
				mapNameGroups.put(g.getNameGroup(), g.getIdGroup());
			}
			listGroups.addContent(group);
			System.err.println("Entraaaaaaaaaaaaaa "+g);
		}
		
		ArrayList<Alias> aliasList = getAlias(factConnDB,ik, configurationMode);
		Iterator<Alias> italias=aliasList.iterator();
		while(italias.hasNext()){
			Element alias=new Element("ALIAS");
			Alias al=italias.next();
			alias.setAttribute("ALIAS",al.getAlias());
			if (al.getUTask()!=null) alias.setAttribute("UTASK", al.getUTask().toString());
			if (al.getGroup()!=null) alias.setAttribute("GROUP", al.getGroup().toString());
			if (al.getIdto()!=null) alias.setAttribute("CLASS", al.getIdto().toString());
			if (al.getProp()!=null) alias.setAttribute("PROP", al.getProp().toString());
			if (al.getUTaskName()!=null) alias.setAttribute("UTASKNAME", al.getUTaskName());
			if (al.getGroupName()!=null) alias.setAttribute("GROUPNAME", al.getGroupName());
			if (al.getIdtoName()!=null) alias.setAttribute("CLASSNAME", al.getIdtoName());
			if (al.getPropName()!=null) alias.setAttribute("PROPNAME", al.getPropName());
			listalias.addContent(alias);
		}

		Element listProp=new Element("COLUMNS");
		root.addContent(listProp);
		ArrayList<ColumnProperty> columnProperties = getColumnProperties(factConnDB, ik, configurationMode);
		Iterator<ColumnProperty> itprop=columnProperties.iterator();
		while(itprop.hasNext()){
			Element colProp=new Element("COLUMN");
			ColumnProperty cp=itprop.next();
			if (cp.getIdtoParent()!=null)
				colProp.setAttribute("CLASSPARENT", String.valueOf(cp.getIdtoParent()));
			
			if (cp.getIdto()!=null)
				colProp.setAttribute("CLASS", String.valueOf(cp.getIdto()));
			if (cp.getIdProp()!=null) 
				colProp.setAttribute("PROP", String.valueOf(cp.getIdProp()));
			if (cp.getIdPropPath()!=null) 
				colProp.setAttribute("PROPPATH", cp.getIdPropPath());
			if (cp.getValueFilter()!=null) 
				colProp.setAttribute("VALUEFILTER", cp.getValueFilter());
			if (cp.getPropFilter()!=null) 
				colProp.setAttribute("PROPFILTER", cp.getPropFilter());
			if (cp.getIdPropF()!=null) 
				colProp.setAttribute("IDPROPF", String.valueOf(cp.getIdPropF()));
			if (cp.getPriority()!=null) 
				colProp.setAttribute("ORDER", String.valueOf(cp.getPriority()));
			listProp.addContent(colProp);
		}

		
		Element essentialProperties = new Element("ESSENTIALPROPERTIES");
		root.addContent(essentialProperties);
		ArrayList<EssentialProperty> essentialPropertiesList = getEssentialProperties(factConnDB, ik, configurationMode);
		Iterator<EssentialProperty> itrEssential=essentialPropertiesList.iterator();
		while(itrEssential.hasNext()){
			Element essentialProperty= new Element("ESSENTIALPROPERTY");
			EssentialProperty essentialP=itrEssential.next();
			if(essentialP.getUTask()!=null)
				essentialProperty.setAttribute("UTASK",String.valueOf(essentialP.getUTask()));
			if(essentialP.getIdto()!=null)
				essentialProperty.setAttribute("CLASS",String.valueOf(essentialP.getIdto()));
			essentialProperty.setAttribute("PROP",String.valueOf(essentialP.getProp()));
			essentialProperties.addContent(essentialProperty);
		}
		
		Element masks = new Element("MASKS");
		root.addContent(masks);
		ArrayList<Mask> maskList= getMasks(factConnDB, ik, configurationMode);
		Iterator<Mask> itrMask=maskList.iterator();
		while(itrMask.hasNext()){
			Element mask= new Element("MASK");
			Mask mk=itrMask.next();
			if (mk.getUTask()!=null) mask.setAttribute("UTASK", mk.getUTask().toString());
			if (mk.getIdto()!=null) mask.setAttribute("CLASS", mk.getIdto().toString());
			if (mk.getProp()!=null) mask.setAttribute("PROP", mk.getProp().toString());
			if (mk.getUTaskName()!=null) mask.setAttribute("UTASKNAME", mk.getUTaskName());
			if (mk.getIdtoName()!=null) mask.setAttribute("CLASSNAME", mk.getIdtoName());
			if (mk.getPropName()!=null) mask.setAttribute("PROPNAME", mk.getPropName());
			if (mk.getExpression()!=null) mask.setAttribute("EXPRESSION", mk.getExpression());
			if (mk.getLength()!=null) mask.setAttribute("LENGTH", mk.getLength().toString());
			masks.addContent(mask);
		}
		
		Element required = new Element("REQUIREDS");
		root.addContent(required);
		ArrayList<Required> requiredList = getRequireds(factConnDB, ik, configurationMode);
		Iterator<Required> itrRequired=requiredList.iterator();
		while(itrRequired.hasNext()){
			Element essentialProperty= new Element("REQUIRED");
			Required requiredP=itrRequired.next();
			essentialProperty.setAttribute("CLASS",String.valueOf(requiredP.getIdtoClass()));
			essentialProperty.setAttribute("PROP",String.valueOf(requiredP.getIdProp()));
			required.addContent(essentialProperty);
		}

		Element enumeratedClasses = new Element("ENUMERATEDCLASSES");
		enumeratedClasses.addContent(iData.toElement());
		root.addContent(enumeratedClasses);
		
		/*FileWriter fs = new FileWriter(name+".xml");
		fs.write(jdomParser.returnXML(root));
		fs.close();*/
		//System.out.println(jdomParser.returnXML(root));
		if(metacomclient == true){
			// dynagent.common.xml.metaComClient mcclient = new dynagent.common.xml.metaComClient(root);
			// System.out.println(mcclient.toString());
		}
	}
	
	private LinkedList<Instance> getInstancesByUTask(FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		LinkedList<Instance> listIns = new LinkedList<Instance>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlRepInstances = "select idto, ido, property, value, valuecls, qmin, qmax, name, op from instances " +
				"where idto=" + String.valueOf(Constants.IDTO_UTASK);
		System.out.println(sqlRepInstances);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlRepInstances);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				String idoStr = null;
				Integer ido = rs.getInt(2);
				if (!rs.wasNull()) idoStr = String.valueOf(ido);
				Integer property = rs.getInt(3);
				String valueStr = null;
				Integer value = rs.getInt(4);
				if (!rs.wasNull()) valueStr = String.valueOf(value);
				String valueclsStr = null;
				Integer valuecls = rs.getInt(5);
				if (!rs.wasNull()) valueclsStr = String.valueOf(valuecls);
				String qminStr = null;
				Integer qmin = rs.getInt(6);
				if (!rs.wasNull()) qminStr = String.valueOf(qmin);
				String qmaxStr = null;
				Integer qmax = rs.getInt(7);
				if (!rs.wasNull()) qmaxStr = String.valueOf(qmax);
				String nameRI = rs.getString(8);
				String op = rs.getString(9);
				Instance i = new Instance(String.valueOf(idto), idoStr, String.valueOf(property), valueStr, 
						valueclsStr, qminStr, qmaxStr, op, nameRI);
				listIns.add(i);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return listIns;
	}
	private LinkedList<Instance> getInstancesByReport(FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		LinkedList<Instance> listIns = new LinkedList<Instance>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlRepInstances = "select idto, ido, property, value, valuecls, qmin, qmax, name, op from instances " +
				"where idto=" + String.valueOf(Constants.IDTO_UTASK) + " OR " +
				"(idto=" + String.valueOf(Constants.IDTO_REPORT) + " AND property<>" + Constants.IdPROP_RDN + ")";
		System.out.println(sqlRepInstances);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlRepInstances);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				String idoStr = null;
				Integer ido = rs.getInt(2);
				if (!rs.wasNull()) idoStr = String.valueOf(ido);
				Integer property = rs.getInt(3);
				String valueStr = null;
				Integer value = rs.getInt(4);
				if (!rs.wasNull()) valueStr = String.valueOf(value);
				String valueclsStr = null;
				Integer valuecls = rs.getInt(5);
				if (!rs.wasNull()) valueclsStr = String.valueOf(valuecls);
				String qminStr = null;
				Integer qmin = rs.getInt(6);
				if (!rs.wasNull()) qminStr = String.valueOf(qmin);
				String qmaxStr = null;
				Integer qmax = rs.getInt(7);
				if (!rs.wasNull()) qmaxStr = String.valueOf(qmax);
				String nameRI = rs.getString(8);
				String op = rs.getString(9);
				Instance i = new Instance(String.valueOf(idto), idoStr, String.valueOf(property), valueStr, 
				valueclsStr, qminStr, qmaxStr, op, nameRI);
				listIns.add(i);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return listIns;
	}
	private void addInstancesUserTask(FactoryConnectionDB factConnDB, LinkedList<Instance> lInstUTask, ArrayList<Instance> listIns, ArrayList<T_Herencias> listHer, ArrayList<ListenerUtask> listlutask,
			String name, Integer dominio, int idoFunctionalArea, String functionalArea, Integer colorRGB, Integer updateMinutes) throws SQLException, NamingException {

		T_Herencias herenciaUTask = new T_Herencias();
		herenciaUTask.setID_TO_Padre(Constants.IDTO_UTASK);
		herenciaUTask.setID_TO(idtoFicticio);
		listHer.add(herenciaUTask);
		
		if(colorRGB!=null){
			ListenerUtask listenerUtask=new ListenerUtask();
			listenerUtask.setUtask(idtoFicticio);
			listenerUtask.setRgb(colorRGB);
			listenerUtask.setUpdatePeriod(updateMinutes);
			listlutask.add(listenerUtask);
		}
		Iterator<Instance> itT = lInstUTask.iterator();
		while (itT.hasNext()) {
			Instance i = itT.next().clone();
			i.setIDTO(String.valueOf(idtoFicticio));
			i.setNAME(name);
			
			if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_TARGETCLASS)))){
				if(i.getOP()==null){
					i.setOP(Constants.OP_INTERSECTION);
					i.setVALUECLS(dominio.toString());
				}
			}
			if(!i.getOP().equals(Constants.OP_CARDINALITY)){
				if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_MYFUNCTIONALAREA)))){
					i.setVALUECLS(String.valueOf(Constants.IDTO_FUNCTIONAL_AREA));
					i.setOP(Constants.OP_ONEOF);
					i.setVALUE(functionalArea);
				}
			}
			listIns.add(i);
		}
		
		idtoFicticio--;
	}
	
	private void addInstancesReport(FactoryConnectionDB factConnDB, LinkedList<Instance> lInstReport, HashMap<String, Integer> fAreas, 
			HashMap<String, Integer> hFormat, ArrayList<Instance> listIns, ArrayList<T_Herencias> listHer, ArrayList<Access> listAcc, 
			LinkedList<Groups> listGr, LinkedList<Properties> listPr, int informeId, String nameRep, Integer dominio, HashSet<Integer> dominioSpec, 
			HashSet<String> functionalArea, Boolean impresionDirecta, Boolean vistaPrevia, 
			HashSet<String> formatoInforme, Boolean confirmarImpresion, int copiesNumber, String impresora,boolean configurationMode, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws SQLException, NamingException {
		
		//Integer maxIdGroup = null;
		
		T_Herencias herenciaUTask = new T_Herencias();
		herenciaUTask.setID_TO_Padre(Constants.IDTO_UTASK);
		herenciaUTask.setID_TO(idtoFicticio);
		listHer.add(herenciaUTask);

		T_Herencias herenciaReport = new T_Herencias();
		herenciaReport.setID_TO_Padre(Constants.IDTO_REPORT);
		herenciaReport.setID_TO(idtoFicticio);
		listHer.add(herenciaReport);
		
		HashSet<String> aOneOf = new HashSet<String>();
		Iterator<Instance> itIR = lInstReport.iterator();
		while (itIR.hasNext()) {
			Instance i = itIR.next().clone();
			System.out.println("ITERAR REPORT "+i);
			i.setIDTO(String.valueOf(idtoFicticio));
			i.setNAME(nameRep);
			
			if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_TARGETCLASS)))){
				if(i.getOP()==null){
					i.setOP(Constants.OP_INTERSECTION);
					i.setVALUECLS(dominio.toString());
				}
			}
			if(i.getOP().equals(Constants.OP_CARDINALITY)){
				if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_MYFUNCTIONALAREA)))){
					i.setQMAX(null);
				}
			}else{
				if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_MYFUNCTIONALAREA)))){
					if(functionalArea!=null && !functionalArea.isEmpty()){
						Iterator<String> it = functionalArea.iterator();
						boolean first=true;
						while (it.hasNext()) {
							String functionalA = (String)it.next();
							//obtener ido a partir de rdn format
							System.out.println("FUCNTIONAL_AREA=>"+functionalA);
							int tableIdFunctionalArea = fAreas.get(functionalA);
							//int tableIdFunctionalArea = getTableId(factConnDB, Constants.CLS_FUNCTIONAL_AREA.toLowerCase(), functionalA);
							int idoFunctionalArea = QueryConstants.getIdo(tableIdFunctionalArea, Constants.IDTO_FUNCTIONAL_AREA);
							if (!aOneOf.contains(String.valueOf(idoFunctionalArea))) {
								aOneOf.add(String.valueOf(idoFunctionalArea));
								if(first) {
									i.setVALUECLS(String.valueOf(Constants.IDTO_FUNCTIONAL_AREA));
									i.setOP(Constants.OP_ONEOF);
									i.setVALUE(functionalA);
									first=false;
								} else {
									Instance inew= i.clone();
									i.setVALUE(functionalA);
									listIns.add(inew);
								}
							}
						}
					}else{
						//LinkedList<Object> li=getValueClsOf(factConnDB, dominio);
						//LinkedList<Integer> lfuncarea=getFuncArea(factConnDB, li);
						System.out.println("dominio " + dominio);
						HashSet<String> lfuncarea=mapTargetClassFunctionalArea.get(dominio);
						if (lfuncarea==null)
							lfuncarea = new HashSet<String>();
						//añadimos en especializados del dominio
						//System.out.println("domSpecsize " + dominioSpec.size());
						Iterator<Integer> it = dominioSpec.iterator();
						while (it.hasNext()) {
							Integer domSpec = (Integer)it.next();
							System.out.println("domSpec " + domSpec);
							HashSet<String> lfuncareaTmp=mapTargetClassFunctionalArea.get(domSpec);
							if (lfuncareaTmp!=null)
								lfuncarea.addAll(lfuncareaTmp);
						}
						Iterator<String> itfuncarea=lfuncarea.iterator();
						boolean first=true;
						while(itfuncarea.hasNext()){
							String fa=itfuncarea.next();
							System.out.println("AREA FUNCIONAL=>"+fa);
							if (!aOneOf.contains(fa.toString())) {
								aOneOf.add(fa.toString());
								if(first) {
									i.setVALUECLS(String.valueOf(Constants.IDTO_FUNCTIONAL_AREA));
									i.setOP(Constants.OP_ONEOF);
									i.setVALUE(fa);
									first=false;
								} else {
									Instance inew= i.clone();
									i.setVALUE(fa);
									listIns.add(inew);
								}
							}
						}
					}
				} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_DIRECTIMPRESION)))){
					i.setOP(Constants.OP_DEFAULTVALUE);
					String directImpresionStr=impresionDirecta?"1":"0";
					i.setQMIN(directImpresionStr);
					i.setQMAX(directImpresionStr);
				}else if(i.getPROPERTY().equals(String.valueOf(dataBaseMap.getPropertyId(Constants.PROP_REPORT_PRINTER)))){
					System.out.println("REPORT IMPRESORA "+i);
					if(i.getOP()==null){
						i.setOP(Constants.OP_DEFAULTVALUE);
					}
					if(impresora!=null) i.setVALUE(impresora);
					
				} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_REPORT_PREVIEW)))){
					i.setOP(Constants.OP_DEFAULTVALUE);
					String preViewStr=vistaPrevia?"1":"0";
					i.setQMIN(preViewStr);
					i.setQMAX(preViewStr);
				} else if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_REPORT_FORMAT)))){
					
					String defaultFormat = null;
					if (formatoInforme.contains(Constants.PDF))
						defaultFormat = Constants.PDF;
					else if (formatoInforme.contains(Constants.EXCEL))
						defaultFormat = Constants.EXCEL;
					else if (formatoInforme.contains(Constants.RTF))
						defaultFormat = Constants.RTF;
					
					i.setOP(Constants.OP_ONEOF);
					i.setVALUECLS(String.valueOf(Constants.IDTO_REPORT_FORMAT));
					Iterator<String> it = formatoInforme.iterator();
					boolean first=true;
					while (it.hasNext()) {
						String format = it.next();
						//obtener ido a partir de rdn format
						System.out.println("FORMAT=>"+format);
						int tableIdFormat = hFormat.get(format);
						//int tableIdFormat = getTableId(factConnDB, Constants.CLS_REPORT_FORMAT.toLowerCase(), format);
						int idoFormat = QueryConstants.getIdo(tableIdFormat, Constants.IDTO_REPORT_FORMAT);
						
						if (format.equals(defaultFormat)) {
							Instance inew = i.clone();
							inew.setVALUE(format);
							inew.setOP(Constants.OP_DEFAULTVALUE);
							listIns.add(inew);
						}
						if (first) {
							i.setVALUE(format);
							first=false;
						} else {
							Instance inew = i.clone();
							i.setVALUE(format);
							listIns.add(inew);
						}
					}
				}else if(i.getPROPERTY().equals(String.valueOf((dataBaseMap.getPropertyId(Constants.PROP_REPORT_NCOPIES))))){
					i.setOP(Constants.OP_DEFAULTVALUE);
					String copies=String.valueOf(copiesNumber);
					i.setQMIN(copies);
					i.setQMAX(copies);
				}else if(i.getPROPERTY().equals(String.valueOf((dataBaseMap.getPropertyId(Constants.PROP_REPORT_EXEC_PRINTSEQUENCE))))){
					i.setOP(Constants.OP_DEFAULTVALUE);
					i.setQMIN(String.valueOf(1.0));
					i.setQMAX(String.valueOf(1.0));
				}
			}
			listIns.add(i);
		}
		
		//busqueda de parametros
		int idtoFicticioParams = idtoFicticio - 1;
		System.out.println("idtoFicticio " + idtoFicticio);
		System.out.println("idtoFicticioParams " + idtoFicticioParams);
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterBegin();
		boolean first = true;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlParams = "select pd.nombre, td.rdn, pd." + cB + "mínimo_número_valores" + cE + ", pd." + cB + "máximo_número_valores" + cE + ", pd.valor_defecto, 'dp' " +
			"from " + cB + "parámetro_dato" + cE + " as pd " + 
			"left join tipo_dato as td on(td." + cB + "tableId" + cE + "=pd.tipo_dato) " + 
			"where " + cB + "informeId" + cE + "=" + informeId + 
				" union " +  
			"select po.nombre, cast(c.id as varchar), po." + cB + "mínimo_número_valores" + cE + ", po." + cB + "máximo_número_valores" + cE + ", po.valor_defecto, 'op' " +
			"from " + cB + "parámetro_objeto" + cE + " as po " + 
			"left join clase as c on(c." + cB + "tableId" + cE + "=po.tipo_objeto) ";
		if(!configurationMode){
			sqlParams+="left join " + cB + "clase_excluída" + cE + " ce on (po.tipo_objeto=ce.dominio)" + 
			"where " + cB + "informeId" + cE + "=" + informeId + " and ce." + cB + "tableId" + cE + " is null";
		}else{
			sqlParams+="where " + cB + "informeId" + cE + "=" + informeId;
		}
		System.out.println(sqlParams);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlParams);
			while (rs.next()) {
				if (first) {
					Instance i = new Instance();
					i.setIDTO(String.valueOf(idtoFicticio));
					i.setPROPERTY(String.valueOf(Constants.IdPROP_PARAMS));
					i.setNAME(nameRep);
					i.setVALUECLS(String.valueOf(idtoFicticioParams));
					i.setOP(Constants.OP_INTERSECTION);
					listIns.add(i);
					
					T_Herencias herenciaParams = new T_Herencias();
					herenciaParams.setID_TO_Padre(Constants.IDTO_PARAMS);
					herenciaParams.setID_TO(idtoFicticioParams);
					listHer.add(herenciaParams);
					
					i= new Instance();
					i.setIDTO(String.valueOf(idtoFicticioParams));
					i.setNAME("Params_"+nameRep);
					i.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
					i.setOP(Constants.OP_INTERSECTION);
					i.setVALUECLS(String.valueOf(Constants.IDTO_STRING));
					listIns.add(i);
					
					i= new Instance();
					i.setIDTO(String.valueOf(idtoFicticioParams));
					i.setNAME("Params_"+nameRep);
					i.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
					i.setOP(Constants.OP_CARDINALITY);
					i.setQMIN(null);
					i.setQMAX("1");
					listIns.add(i);
					
					first = false;
				}
				
				String nameProp = rs.getString(1);
				String tipoDatoStr = rs.getString(2);
				Integer cardMin = rs.getInt(3);
				Integer cardMax = rs.getInt(4);
				String defaultValue = rs.getString(5);
				boolean isOP = rs.getString(6).equals("op");
				
				Instance i= new Instance();
				i.setIDTO(String.valueOf(idtoFicticioParams));
				i.setNAME("Params_"+nameRep);
				i.setPROPERTY(String.valueOf(propFicticia));
				i.setOP(Constants.OP_INTERSECTION);
				
				boolean setDefaultValue=true;
				int tipoDato = 0;
				if (isOP) {
					tipoDato = Integer.parseInt(tipoDatoStr);
					/*if(defaultValue!=null){
						try{
							defaultValue = String.valueOf(InstanceService.getIdo(fcdb, dataBaseMap, tipoDato, defaultValue, false));
						}catch(Exception ex){
							System.err.println("WARNING: Valor por defecto para los parametros de un report no existe en base de datos");
							ex.printStackTrace();
							setDefaultValue=false;
						}
					}*/
				} else {
					tipoDato = Constants.getIdDatatype(tipoDatoStr.toLowerCase());
					tipoDatoStr = String.valueOf(tipoDato);
				}
				i.setVALUECLS(tipoDatoStr);
				
				System.out.println("instance " + i);
				listIns.add(i);
				
				Properties prop=new Properties();
				prop.setCAT(isOP?Category.iObjectProperty:Category.iDataProperty);
				prop.setNAME(nameProp + "@" + nameRep);
				prop.setPROP(propFicticia);
				if(!isOP)
					prop.setVALUECLS(tipoDato);
				listPr.add(prop);
				
				if(defaultValue!=null && setDefaultValue){
					Instance idefaultValue = new Instance();
					idefaultValue.setIDTO(String.valueOf(idtoFicticioParams));
					idefaultValue.setNAME("Params_"+nameRep);
					idefaultValue.setPROPERTY(String.valueOf(propFicticia));
					idefaultValue.setVALUE(String.valueOf(defaultValue));
					idefaultValue.setOP(Constants.OP_DEFAULTVALUE);
					idefaultValue.setVALUECLS(tipoDatoStr);
					listIns.add(idefaultValue);
				}
					
				if(isOP || cardMin!=null && cardMin.equals(1)) {
					Instance icard = new Instance();
					icard.setIDTO(String.valueOf(idtoFicticioParams));
					icard.setNAME("Params_"+nameRep);
					icard.setPROPERTY(String.valueOf(propFicticia));
					icard.setOP(Constants.OP_CARDINALITY);
					if (cardMin!=null && cardMin.equals(1)) //Cardinalidad minima a 1 solo si es requerido
						icard.setQMIN("1");
					if (cardMax!=null && cardMax.equals(1)) //Cardinalidad maxima != 1 solo si es cardinalidad multiple
						icard.setQMAX("1");
					listIns.add(icard);
				}
				if(isOP) {
					LinkedList<T_Herencias> listh = getT_Herencias(factConnDB, tipoDato, configurationMode);
					if (listh.isEmpty() && tipoDato!=Constants.IDTO_ENUMERATED) {
						Access accSet= new Access();
						accSet.setDENNIED(1);
						accSet.setTASK(idtoFicticio);
						accSet.setIDTO(tipoDato);
						accSet.setACCESSTYPENAME(Constants.ACCESS_SET_NAME);
						listAcc.add(accSet);
						
						Access accNew= new Access();
						accNew.setDENNIED(1);
						accNew.setTASK(idtoFicticio);
						accNew.setIDTO(tipoDato);
						accNew.setACCESSTYPENAME(Constants.ACCESS_NEW_NAME);
						listAcc.add(accNew);
						
					} else {
						boolean enumerado=false;
						Iterator<T_Herencias> ith= listh.iterator();
						while(ith.hasNext() && !enumerado) {
							T_Herencias heren= (T_Herencias) ith.next();
							if(heren.getID_TO_Padre()==Constants.IDTO_ENUMERATED)
								enumerado=true;
						}
						if(!enumerado) {
							Access accSet= new Access();
							accSet.setDENNIED(1);
							accSet.setTASK(idtoFicticio);
							accSet.setIDTO(tipoDato);
							accSet.setACCESSTYPENAME(Constants.ACCESS_SET_NAME);
							listAcc.add(accSet);
							
							Access accNew= new Access();
							accNew.setDENNIED(1);
							accNew.setTASK(idtoFicticio);
							accNew.setIDTO(tipoDato);
							accNew.setACCESSTYPENAME(Constants.ACCESS_NEW_NAME);
							listAcc.add(accNew);
						}
					}
				}
				//TODO No es correcto. Revisar si es necesario, y si lo fuera cambiar las consultas de getGroupsCondicionesBusqueda y getNextIdGroup
//				LinkedList<Groups> lvo = getGroupsCondicionesBusqueda(factConnDB);
//				Groups groupProp = new Groups();
//				if(lvo.isEmpty()) {
//					if (maxIdGroup==null) {
//						maxIdGroup = getNextIdGroup(factConnDB);
//						groupProp.setIdGroup(maxIdGroup);
//					} else {
//						maxIdGroup++;
//						groupProp.setIdGroup(maxIdGroup);
//					}
//				} else {
//					Groups group = (Groups)lvo.getFirst();
//					groupProp.setIdGroup(group.getIdGroup());
//				}
//				groupProp.setUTask(dominio);
//				groupProp.setIdtoClass(idtoFicticioParams);
//				groupProp.setNameGroup("CONDICIONES_DE_BUSQUEDA");
//				groupProp.setIdProp(propFicticia);
//				listGr.add(groupProp);
				
				propFicticia--;
			}
			if(!first)//Significara que al menos ha entrado una vez, si no hubiera entrado no tenemos que decrementar
				idtoFicticioParams--;
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		idtoFicticio = idtoFicticioParams;
	}
	private LinkedList<T_Herencias> getT_Herencias(FactoryConnectionDB factConnDB, int tipoDato, boolean configurationMode) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		LinkedList<T_Herencias> listHer = new LinkedList<T_Herencias>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlHerencias = "select t.id_to, t.id_to_padre from t_herencias t ";
		if(!configurationMode){
			sqlHerencias+="left join " + cB + "clase_excluída" + cE + " ce on (t.id_to=ce.dominio)"	+
			"where t.id_to=" + tipoDato + " and ce." + cB + "tableId" + cE + " is null";
		}else{
			sqlHerencias+="where id_to=" + tipoDato;
		}
		System.out.println(sqlHerencias);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlHerencias);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				Integer idtoPadre = rs.getInt(2);
				T_Herencias her = new T_Herencias();
				her.setID_TO_Padre(idtoPadre);
				her.setID_TO(idto);
				listHer.add(her);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return listHer;
	}
	
	private HashMap<String, Integer> getMapFormat(FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterBegin();

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		HashMap<String, Integer> hFormat = new HashMap<String, Integer>();
		String sql = "select " + cB + "tableId" + cE + ", rdn from " + Constants.CLS_REPORT_FORMAT.toLowerCase();
		System.out.println(sql);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer tableId = rs.getInt(1);
				String value = rs.getString(2);
				hFormat.put(value,tableId);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return hFormat;
	}
	/*
	private LinkedList<Object> getValueClsOf(FactoryConnectionDB factConnDB, int dominio) throws SQLException, NamingException {
		LinkedList<Object> li=new LinkedList<Object>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlRepInstances = "select idto, ido, property, value, valuecls, qmin, qmax, name, op from instances where valuecls=" + dominio;
		System.out.println(sqlRepInstances);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlRepInstances);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				String idoStr = null;
				Integer ido = rs.getInt(2);
				if (!rs.wasNull()) idoStr = String.valueOf(ido);
				Integer property = rs.getInt(3);
				String valueStr = null;
				Integer value = rs.getInt(4);
				if (!rs.wasNull()) valueStr = String.valueOf(value);
				String valueclsStr = null;
				Integer valuecls = rs.getInt(5);
				if (!rs.wasNull()) valueclsStr = String.valueOf(valuecls);
				String qminStr = null;
				Integer qmin = rs.getInt(6);
				if (!rs.wasNull()) qminStr = String.valueOf(qmin);
				String qmaxStr = null;
				Integer qmax = rs.getInt(7);
				if (!rs.wasNull()) qmaxStr = String.valueOf(qmax);
				String nameRI = rs.getString(8);
				String op = rs.getString(9);
				Instance i = new Instance(String.valueOf(idto), idoStr, String.valueOf(property), valueStr, 
						valueclsStr, qminStr, qmaxStr, op, nameRI);
				li.add(i);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return li;
	}
	private LinkedList<Integer> getFuncArea(FactoryConnectionDB factConnDB, LinkedList<Object> li) throws SQLException, NamingException {
		Iterator<Object> it = li.iterator();
		LinkedList<Integer> lfa= new LinkedList<Integer>();
		LinkedList<Integer> lut= new LinkedList<Integer>();
		while(it.hasNext()) {
			Instance i = (Instance) it.next();
			if (i.getPROPERTY().equals(String.valueOf(Constants.IdPROP_TARGETCLASS))) {
				lut.add(Integer.parseInt(i.getIDTO()));				
			}
		}
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			Iterator<Integer> itut = lut.iterator();
			while(itut.hasNext()) {
				String sqlRepInstances = "select idto, ido, property, value, valuecls, qmin, qmax, name, op from instances where idto=" + itut.next().toString() + " AND op NOT LIKE 'CAR'";
				System.out.println(sqlRepInstances);
				con = factConnDB.createConnection(true);
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sqlRepInstances);
				while (rs.next()) {
					Integer idto = rs.getInt(1);
					String idoStr = null;
					Integer ido = rs.getInt(2);
					if (!rs.wasNull()) idoStr = String.valueOf(ido);
					Integer property = rs.getInt(3);
					String valueStr = null;
					Integer value = rs.getInt(4);
					if (!rs.wasNull()) valueStr = String.valueOf(value);
					String valueclsStr = null;
					Integer valuecls = rs.getInt(5);
					if (!rs.wasNull()) valueclsStr = String.valueOf(valuecls);
					String qminStr = null;
					Integer qmin = rs.getInt(6);
					if (!rs.wasNull()) qminStr = String.valueOf(qmin);
					String qmaxStr = null;
					Integer qmax = rs.getInt(7);
					if (!rs.wasNull()) qmaxStr = String.valueOf(qmax);
					String nameRI = rs.getString(8);
					String op = rs.getString(9);
					Instance i = new Instance(String.valueOf(idto), idoStr, String.valueOf(property), valueStr, 
							valueclsStr, qminStr, qmaxStr, op, nameRI);
					if(property.equals(Constants.IdPROP_MYFUNCTIONALAREA)) {
						addLfa(factConnDB, i, lfa);
					}
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return lfa;
	}
	private void addLfa(FactoryConnectionDB factConnDB, Instance iu, LinkedList<Integer> lfa) throws SQLException, NamingException {
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlHerencias = "select id_to, id_to_padre from t_herencias where id_to=" + iu.getIDTO() + " and id_to_padre=" + Constants.IDTO_ACTION;
		System.out.println(sqlHerencias);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlHerencias);
			System.out.println("INSTANCE " + iu);
			if (!rs.next()) {
				lfa.add(Integer.parseInt(iu.getVALUE()));
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
	}
	
	private Integer getTableId(FactoryConnectionDB factConnDB, String table, String rdn) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterBegin();
		
		Integer tableIdFormat = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlFormat = "select " + cB + "tableId" + cE + " from " + cB + table + cE + " where rdn='" + rdn + "'";
		System.out.println(sqlFormat);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlFormat);
			while (rs.next()) {
				tableIdFormat = rs.getInt(1);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return tableIdFormat;
	}
	
	private LinkedList<Groups> getGroupsCondicionesBusqueda(FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		LinkedList<Groups> listGr = new LinkedList<Groups>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlGrupos = "select id, utask, class, name, prop from groups where name LIKE 'CONDICIONES_DE_BUSQUEDA'";
		System.out.println(sqlGrupos);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlGrupos);
			while (rs.next()) {
				Integer id = rs.getInt(1);
				Integer uTask = rs.getInt(2);
				if (rs.wasNull()) uTask = null;
				Integer clase = rs.getInt(3);
				if (rs.wasNull()) clase = null;
				String name = rs.getString(4);
				String prop = rs.getString(5);
				
				Groups gr = new Groups();
				gr.setIdGroup(id);
				gr.setUTask(uTask);
				gr.setIdtoClass(clase);
				gr.setNameGroup(name);
				gr.setIdProp(Integer.parseInt(prop));
				listGr.add(gr);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return listGr;
	}
	private Integer getNextIdGroup(FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		Integer nextId = 1;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		String sqlGrupos = "select max(id) from groups";
		System.out.println(sqlGrupos);
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlGrupos);
			if (rs.next()) {
				nextId = rs.getInt(1);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return nextId;
	}
	*/
	private void objectsToElements(ArrayList<Instance> listIns, ArrayList<T_Herencias> listHer, ArrayList<Access> listAcc, LinkedList<Groups> listGr, 
			LinkedList<Properties> listPr, ArrayList<ListenerUtask> listListenerUTask,
			Element hers, Element ins, Element acs, Element listGroups, Element props, Element listenerUtasks) throws IncoherenceInMotorException {
		//pasar listHer a Element hers
		Iterator<T_Herencias> itHer = listHer.iterator();
		while (itHer.hasNext()) {
			Element herencia = new Element("HIERARCHY");
			T_Herencias tHer = (T_Herencias)itHer.next();
			herencia.setAttribute("ID_TO",String.valueOf(tHer.getID_TO()));
			herencia.setAttribute("ID_TO_PARENT",String.valueOf(tHer.getID_TO_Padre()));
			hers.addContent(herencia);
		}
		
		//pasar listIns a Element ins
		Iterator<Instance> itIns = listIns.iterator();
		while (itIns.hasNext()) {
			Element instance = new Element("INSTANCE");
			Instance inst = (Instance)itIns.next();
			if(inst.getIDTO()!=null)
				instance.setAttribute("IDTO",inst.getIDTO());
			if(inst.getIDO()!=null)
				instance.setAttribute("IDO",inst.getIDO());
			if(inst.getPROPERTY()!=null)
				instance.setAttribute("PROP",inst.getPROPERTY());
			if(inst.getVALUE()!=null)
				instance.setAttribute("VAL",inst.getVALUE());
			if(inst.getVALUECLS()!=null){
				instance.setAttribute("VALCLS",inst.getVALUECLS());
			}
			if(inst.getQMIN()!=null)
				instance.setAttribute("Q_MIN",inst.getQMIN());
			if(inst.getQMAX()!= null)
				instance.setAttribute("Q_MAX",inst.getQMAX());
			if(inst.getOP()!=null)
				instance.setAttribute("OP",inst.getOP());
			if(inst.getNAME()!= null)
				instance.setAttribute("NAME",inst.getNAME());
			ins.addContent(instance);
		}
		
		//pasar listAcc a Element acs
		Iterator<Access> itAcc = listAcc.iterator();
		while (itAcc.hasNext()) {
			Element access = new Element("ACCESS");
			Access acc = (Access)itAcc.next();
			if(acc.getDENNIED()!=null)
				access.setAttribute("DENNIED",String.valueOf(acc.getDENNIED()));
			if(acc.getTASK()!=null)
				access.setAttribute("TASK",String.valueOf(acc.getTASK()));
			if(acc.getACCESSTYPENAME()!=null){
				String accessTypeName=acc.getACCESSTYPENAME().get(0);
				for(int i=1;i<acc.getACCESSTYPENAME().size();i++)
					accessTypeName+=";"+acc.getACCESSTYPENAME().get(i);
				access.setAttribute("ACCESSTYPENAME",accessTypeName);
			}if(acc.getIDTO()!=null)
				access.setAttribute("IDTO",String.valueOf(acc.getIDTO()));
			if(acc.getIDO()!=null)
				access.setAttribute("IDO",String.valueOf(acc.getIDO()));
			if(acc.getPROP()!=null && acc.getPROP().length>0){
				Integer[] propArr=acc.getPROP();
				if(propArr.length>1) throw new IncoherenceInMotorException("ACCESS construido para fact con multiples propiedades");				
				if(propArr[0]!=null) access.setAttribute("PROP",String.valueOf(propArr[0]));
			}
			if(acc.getVALUE()!=null)
				access.setAttribute("VALUE",acc.getVALUE());
			if(acc.getVALUECLS()!=null)
				access.setAttribute("VALUECLS",String.valueOf(acc.getVALUECLS()));
			access.setAttribute("PRIORITY",String.valueOf(acc.getPRIORITY()));
			acs.addContent(access);
		}
		
		//pasar listGr a Element listGroups
		Iterator<Groups> itGr = listGr.iterator();
		while (itGr.hasNext()) {
			Element group = new Element("GROUP");
			Groups gr = (Groups)itGr.next();
			if(gr.getIdGroup()!=null)
				group.setAttribute("ID",String.valueOf(gr.getIdGroup()));
			if(gr.getUTask()!=null)
				group.setAttribute("TASK",String.valueOf(gr.getUTask()));
			if(gr.getIdtoClass()!=null)
				group.setAttribute("IDTOCLASS",String.valueOf(gr.getIdtoClass()));
			if(gr.getNameGroup()!=null)
				group.setAttribute("NAME",gr.getNameGroup());
			if(gr.getIdProp()!=null)
				group.setAttribute("PROP",String.valueOf(gr.getIdProp()));
			if(gr.getOrder()!=null)
				group.setAttribute("ORDER",String.valueOf(gr.getOrder()));
			listGroups.addContent(group);
		}

		// pasar listPr a Element listPr
		Iterator<Properties> itPr = listPr.iterator();
		while (itPr.hasNext()) {
			Element propertie = new Element("PROPERTY");
			Properties pr = (Properties)itPr.next();
			if(pr.getPROP()!=null)
				propertie.setAttribute("PROP",pr.getPROP().toString());
			if(pr.getNAME()!=null)
				propertie.setAttribute("NAME",pr.getNAME());
			if(pr.getCAT()!=null)
				propertie.setAttribute("CAT",pr.getCAT().toString());
			if(pr.getVALUECLS()!=null)
				propertie.setAttribute("VALUECLS",pr.getVALUECLS().toString());
			if(pr.getPROPINV()!= null)
				propertie.setAttribute("PROPINV", pr.getPROPINV().toString());
			props.addContent(propertie);
		}
		
		Iterator<ListenerUtask> itUTask = listListenerUTask.iterator();
		while(itUTask.hasNext()){
			Element listenerUtask = new Element("LISTENERUTASK");
			ListenerUtask listenerU = (ListenerUtask)itUTask.next();
			listenerUtask.setAttribute("UTASK",String.valueOf(listenerU.getUtask()));
			listenerUtask.setAttribute("RGB",String.valueOf(listenerU.getRgb()));
			listenerUtask.setAttribute("MINUTE",String.valueOf(listenerU.getUpdatePeriod()));
			listenerUtasks.addContent(listenerUtask);
		}
	}
	
	public ArrayList<Alias> getAlias(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException {
		String gestorDB = factConnDB.getGestorDB();
		GenerateSQL gSQL = new GenerateSQL(gestorDB);
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT utask, " + cB + "group" + cE + ", class, prop, alias FROM s_alias a ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=a.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=a.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<Alias> aliasList = new ArrayList<Alias>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				boolean success=true;
				String utask=rs.getString(1);
				String group=rs.getString(2);
				String idto=rs.getString(3);
				String prop=rs.getString(4);
				Integer intutask=null;
				Integer intgroup=null;
				Integer intidto=null;
				Integer intprop=null;
				if (utask!=null){
					intutask=mapNameUtaskIdto.get(utask);
					if(intutask==null){
						intutask = AuxiliarModel.getClassByName(utask, factConnDB);
						if(intutask==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El UTASK "+utask+" no existe. Siendo group:"+group+" class:"+idto+" prop:"+prop);
						}
					}
				}
				if (group!=null){
					intgroup=mapNameGroups.get(group);
					if(intgroup==null){
						intgroup = AuxiliarModel.getClassByName(group, factConnDB);
						if(intgroup==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El GROUP "+group+" no existe. Siendo utask:"+utask+" class:"+idto+" prop:"+prop);
						}
					}
				}
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El IDTO "+idto+" no existe. Siendo utask:"+utask+" group:"+group+" prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El PROP "+prop+" no existe. Siendo utask:"+utask+" group:"+group+" class:"+idto);
						}
					}
				}
				
				if (success){
					Alias alias=new Alias();
					alias.setUTask(intutask);
					alias.setGroup(intgroup);
					alias.setIdto(intidto);
					alias.setProp(intprop);
					alias.setAlias(rs.getString(5));
					alias.setGroupName(group);
					alias.setIdtoName(idto);
					alias.setPropName(prop);
					alias.setUTaskName(utask);
					aliasList.add(alias);
					//System.err.println(alias);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return aliasList;
	}
	
	public ArrayList<EssentialProperty> getEssentialProperties(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT utask, class, prop FROM s_essentialproperties ep ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=ep.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=ep.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<EssentialProperty> essentialPropertyList = new ArrayList<EssentialProperty>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				boolean success=true;
				String utask=rs.getString(1);
				String idto=rs.getString(2);
				String prop=rs.getString(3);
				Integer intutask=null;
				Integer intidto=null;
				Integer intprop=null;
				if (utask!=null){
					intutask=mapNameUtaskIdto.get(utask);
					if(intutask==null){
						intutask = AuxiliarModel.getClassByName(utask, factConnDB);
						if(intutask==null){
							success=false;
							System.err.println("WARNING!: MetaData.getEssentialProperties => El UTASK "+utask+" no existe. Siendo class:"+idto+" prop:"+prop);
						}
					}
				}
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getEssentialProperties => El IDTO "+idto+" no existe. Siendo utask:"+utask+" prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getEssentialProperties => El PROP "+prop+" no existe. Siendo utask:"+utask+" class:"+idto);
						}
					}
				}
				
				if (success){
					EssentialProperty essentialP=new EssentialProperty();
					essentialP.setIdto(intidto);
					essentialP.setIdtoName(idto);
					essentialP.setProp(intprop);
					essentialP.setPropName(prop);
					essentialP.setUTask(intutask);
					essentialP.setUTaskName(utask);
					
					essentialPropertyList.add(essentialP);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return essentialPropertyList;
	}
	
	public ArrayList<Mask> getMasks(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT utask, class, prop, expresion, length FROM s_mask m ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=m.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=m.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<Mask> maskList = new ArrayList<Mask>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				boolean success=true;
				String utask=rs.getString(1);
				String idto=rs.getString(2);
				String prop=rs.getString(3);
				String expression=rs.getString(4);
				Integer length=null;
				if(rs.getInt(5)!=0)
					length=rs.getInt(5);
				Integer intutask=null;
				Integer intidto=null;
				Integer intprop=null;
				if (utask!=null){
					intutask=mapNameUtaskIdto.get(utask);
					if(intutask==null){
						intutask = AuxiliarModel.getClassByName(utask, factConnDB);
						if(intutask==null){
							success=false;
							System.err.println("WARNING!: MetaData.getMasks => El UTASK "+utask+" no existe. Siendo class:"+idto+" prop:"+prop);
						}
					}
				}
				if (idto!=null){
					intidto=ik.getIdClass(idto);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getMasks => El IDTO "+idto+" no existe. Siendo utask:"+utask+" prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik.getIdProperty(prop);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getMasks => El PROP "+prop+" no existe. Siendo utask:"+utask+" class:"+idto);
						}
					}
				}
				
				if (success){
					Mask mask=new Mask();
					mask.setUTask(intutask);
					mask.setIdto(intidto);
					mask.setProp(intprop);
					mask.setExpression(expression);
					mask.setLength(length);
					mask.setIdtoName(idto);
					mask.setPropName(prop);
					mask.setUTaskName(utask);
					maskList.add(mask);
					//System.err.println(alias);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return maskList;
	}
	
	public ArrayList<Groups> getGroups(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sqlGroups = "select utask, class, name, prop, " + cB + "order" + cE + " from s_groups g ";
		if(!configurationMode){
			sqlGroups+="LEFT JOIN clase c ON (c.rdn=g.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=g.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<Groups> groupList = new ArrayList<Groups>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlGroups);
			HashMap<String,Integer> nameIdGroups=new HashMap<String,Integer>();
			while (rs.next()) {
				boolean success=true;
				String utask=rs.getString(1);
				String idto=rs.getString(2);
				String name=rs.getString(3);
				String prop=rs.getString(4);
				Integer order=rs.getInt(5);
				Integer intutask=null;
				Integer intidto=null;
				Integer intprop=null;
				if (utask!=null){
					intutask=mapNameUtaskIdto.get(utask);
					if(intutask==null){
						intutask = AuxiliarModel.getClassByName(utask, factConnDB);
						if(intutask==null){
							success=false;
							System.err.println("WARNING!: MetaData.getGroups => El UTASK "+utask+" no existe. Siendo name:"+name+" class:"+idto+" prop:"+prop);
						}
					}
				}
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getGroups => El IDTO "+idto+" no existe. Siendo name:"+name+" utask:"+utask+" prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getGroups => El PROP "+prop+" no existe. Siendo name:"+name+" utask:"+utask+" class:"+idto);
						}
					}
				}
				
				if (success){
					if(!nameIdGroups.containsKey(name))
						nameIdGroups.put(name,nameIdGroups.size()+1);
					
					Groups group=new Groups();
					group.setUTask(intutask);
					group.setIdtoClass(intidto);
					group.setIdProp(intprop);
					group.setNameGroup(name);
					group.setUTaskName(utask);
					group.setClassName(idto);
					group.setPropName(prop);
					group.setIdGroup(nameIdGroups.get(name));
					group.setOrder(order);
					
					groupList.add(group);
					//System.err.println(group);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		return groupList;
	}
	
	public ArrayList<Access> getAccesses(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sqlAccess = "select dennied, utask, "+ cB + "utaskIdto" + cE + ", userrol, " 
			+ cB + "user" + cE + ", access, class, prop, priority, functionalarea from s_access a ";
		if(!configurationMode){
			sqlAccess+="LEFT JOIN clase c ON (c.rdn=a.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=a.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<Access> accessList = new ArrayList<Access>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			System.out.println(sqlAccess);
			rs = st.executeQuery(sqlAccess);
			while (rs.next()) {
				boolean success=true;
				Integer dennied=rs.getInt(1);
				String utask=rs.getString(2);
				Integer utaskIdto=rs.getInt(3);
				if(rs.wasNull()) utaskIdto=null;
				String userRol=rs.getString(4);
				String user=rs.getString(5);
				String accessType=rs.getString(6);
				String idto=rs.getString(7);
				String prop=rs.getString(8);
				Integer priority=rs.getInt(9);
				Integer functionalArea=QueryConstants.getIdo(rs.getInt(10),Constants.IDTO_FUNCTIONAL_AREA);
				if(rs.wasNull()) functionalArea=null;
				Integer intutask=null;
				Integer intidto=null;
				Integer intprop=null;
				if (utask!=null){
					intutask=Auxiliar.equals(Constants.CLS_MENU, ik.getClassName(utaskIdto))?
							mapNameUtaskIdto.get(utask):(Auxiliar.equals(Constants.CLS_ACTION_INDIVIDUAL, ik.getClassName(utaskIdto))?ik.getIdClass(utask):mapNameReportIdto.get(utask));
					if(intutask==null){
						intutask = AuxiliarModel.getClassByName(utask, factConnDB);//
						if(intutask==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAccesses => El UTASK "+utask+" no existe. Siendo dennied:"+dennied+" class:"+idto+" prop:"+prop+" accessType:"+accessType+" priority:"+priority);
						}
					}
				}
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAccesses => El IDTO "+idto+" no existe. Siendo dennied:"+dennied+" utask:"+utask+" prop:"+prop+" accessType:"+accessType+" priority:"+priority);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAccesses => El PROP "+prop+" no existe. Siendo dennied:"+dennied+" utask:"+utask+" class:"+idto+" accessType:"+accessType+" priority:"+priority);
						}
					}
				}
				
				if (success){
					Access access=new Access();
					access.setACCESSTYPENAME(accessType!=null?new ArrayList<String>(Arrays.asList(accessType.split(","/*";"*/)/*Ponemos , en lugar de ; porque la vista de posgree devuelve , */)):null);
					access.setDENNIED(dennied);
					access.setIDTO(intidto);
					access.setPRIORITY(priority);
					access.setPROP(intprop);
					access.setTASK(intutask);
					access.setUSER(user!=null?new ArrayList<String>(Arrays.asList(user)):null);
					access.setUSERROL(userRol!=null?new ArrayList<String>(Arrays.asList(userRol)):null);
					access.setFUNCTIONALAREA(functionalArea!=null?new ArrayList<Integer>(Arrays.asList(functionalArea)):null);	
					accessList.add(access);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		return accessList;
	}
	
	private ArrayList<ColumnProperty> getColumnProperties(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) 
	throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT classparent, class, proppath, prop, propfilter, valuefilter, "+ cB + "order" + cE + " FROM s_columnproperties cp ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c1 ON (c1.rdn=cp.class) " +
			"LEFT JOIN clase c2 ON (c2.rdn=cp.classparent) " +
			"LEFT JOIN v_propiedad p1 ON (p1.rdn=cp.prop) " +
			"LEFT JOIN v_propiedad p2 ON (p2.rdn=cp.propfilter) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce1 ON (c1." + cB + "tableId" + cE + "=ce1.dominio) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce2 ON (c2." + cB + "tableId" + cE + "=ce2.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe1 ON ((p1." + cB + "tableId" + cE + "=pe1." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p1.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p1." + cB + "tableId" + cE + "=pe1." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p1.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe2 ON ((p2." + cB + "tableId" + cE + "=pe2." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p2.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p2." + cB + "tableId" + cE + "=pe2." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p2.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c1." + cB + "tableId" + cE + "=pece.dominio AND ((p1." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p1.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p1." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p1.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce1." + cB + "tableId" + cE + " IS NULL AND ce2." + cB + "tableId" + cE + " IS NULL AND pe1." + cB + "tableId" + cE + " IS NULL AND pe2." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<ColumnProperty> columProperty = new ArrayList<ColumnProperty>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			System.err.println("sqlColumnP "+sql);
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idtoParent=null;
				if(rs.getString(1)!=null)
					idtoParent=ik.getIdClass(rs.getString(1));
				Integer idto=ik.getIdClass(rs.getString(2));
				String idPropPath=rs.getString(3);
				Integer idProp=null;
				if(rs.getString(4)!=null){
					idProp=ik.getIdProperty(rs.getString(4));
				}
				
				Integer idPropF=0;
				if (rs.getString(5)!=null)
					idPropF=ik.getIdProperty(rs.getString(5));
				if(idto!=null && (idPropPath!=null || idProp!=null)){
					
					boolean isValid=false;
					if(idProp!=null && idPropPath==null){
						String sql1 = "SELECT * FROM instances WHERE idto="+idto+" AND property="+idProp;
						Statement st1 = null;
						ResultSet rs1 = null;
						try{
							st1 = con.getBusinessConn().createStatement();
							rs1 = st1.executeQuery(sql1);
							if (rs1.next()) {
								isValid=true;
							}else{
								System.err.println("WARNING!: MetaData.getColumnProperties => Esa clase no tiene la property indicada: idto:"+idto+" siendo "+rs.getString(2)+", idPropPath:"+idPropPath+" siendo "+rs.getString(3));
							}
						}finally{
							if (rs1!=null)
								rs1.close();
							if (st1 != null)
								st1.close();
						}
					}else{
						isValid=true;
					}
					
					if(isValid){
							ColumnProperty colProp=new ColumnProperty();
							if (idtoParent!=null)
								colProp.setIdtoParent(idtoParent);
							colProp.setIdto(idto);
							colProp.setIdPropPath(idPropPath);
							colProp.setIdProp(idProp);
							colProp.setPropFilter(rs.getString(5));
							colProp.setValueFilter(rs.getString(6));
							if (idPropF!=null && idPropF!=0){
								/*String sql2 = "SELECT * FROM instances WHERE idto="+idto+" AND property="+idPropF;//TODO hay que hacer una consulta viendo si el rango de idProp tiene la property idPropF
								Statement st2 = null;
								ResultSet rs2 = null;
								try{
									st2 = con.getBusinessConn().createStatement();
									rs2 = st2.executeQuery(sql2);
									if (rs2.next()) {
								*/		colProp.setIdPropF(idPropF);
										colProp.setPriority(Integer.parseInt(rs.getString(7)));
														
										columProperty.add(colProp);
								/*	}else{
										System.err.println("WARNING!: MetaData.getColumnProperties => Esa clase no tiene la property indicada: idto:"+idto+" siendo "+rs.getString(2)+", idProp:"+idPropF+" siendo "+rs.getString(4));
									}
								}finally{
									if (rs2!=null)
										rs2.close();
									if (st2 != null)
										st2.close();
								}*/
							}else{
								colProp.setPriority(Integer.parseInt(rs.getString(7)));
								
								columProperty.add(colProp);
							}
							
							
					}	

				}else{
					System.err.println("WARNING!: MetaData.getColumnProperties => El IDTO o IDPROP es NULL: idto:"+rs.getString(1)+" siendo "+idto+" "+idPropPath);
				}
		
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return columProperty;
		}
	
	public ArrayList<Required> getRequireds(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT class, prop FROM s_required ep ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=ep.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=ep.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<Required> requiredPropertyList = new ArrayList<Required>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				boolean success=true;
				String idto=rs.getString(1);
				String prop=rs.getString(2);
				Integer intidto=null;
				Integer intprop=null;
				
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: getRequired => El IDTO "+idto+" no existe. Siendo prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: getRequired => El PROP "+prop+" no existe. Siendo class:"+idto);
						}
					}
				}
				
				if (success){
					Required requiredP=new Required();
					requiredP.setIdtoClass(intidto);
					requiredP.setClassName(idto);
					requiredP.setIdProp(intprop);
					requiredP.setProp(prop);
					
					requiredPropertyList.add(requiredP);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return requiredPropertyList;
	}
	
}
