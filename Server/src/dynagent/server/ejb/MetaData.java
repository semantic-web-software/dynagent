package dynagent.server.ejb;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.Mask;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.communication.IndividualData;
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
import dynagent.common.utils.QueryConstants;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.services.InstanceService;
import dynagent.server.gestorsDB.GenerateSQL;;

public class MetaData {
	
	//getMetaData se usa en RulerFactoryXML
	public static Element getMetaData(String user, boolean configurationMode, FactoryConnectionDB factConnDB, Integer business, IKnowledgeBaseInfoServer ik, DataBaseMap dataBaseMap) 
			throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException, SystemException, 
			IncompatibleValueException, CardinalityExceedException, ApplicationException, IOException, JDOMException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, ParseException, OperationNotPermitedException {
		//coger de la tabla userRoles los roles del usuario
		HashSet<String> roles = new HashSet<String>();
		HashSet<Integer> idoRoles = new HashSet<Integer>();
		MetaData.getRolesUsuario(user, roles, idoRoles, factConnDB);
		//y las userTask de otra tabla
		//y restringir los datos devueltos en convert

		//pasar la lista de userRol a XMLConverter y que genere un nodo
		//para cargar fichero de reglas se le pasa como String a convert
		//String stRules = MetaData.getRulerFile(factConnDB, Constants.RULER_APPLET);
		ArrayList<OrderProperty> orderPropertyList = MetaData.getOrderProperties(factConnDB,ik,configurationMode);
		//HashMap<Integer,HashMap<String,String>> reportList=MetaData.getReport(factConnDB);
		ArrayList<CardMed> cardmedList = MetaData.getCardMedTable(factConnDB,ik,configurationMode);
		cardmedList.addAll(MetaData.getCardMedField(factConnDB,ik,configurationMode));
		
		IndividualData iData = MetaData.getEnumerated(factConnDB, ik, dataBaseMap, configurationMode);
		XMLConverter xml = new XMLConverter();
		xml.convert(factConnDB, dataBaseMap, business, "ruleengine", user, roles, idoRoles, /*stRules, */orderPropertyList, cardmedList, iData, Constants.MIN_ID_CLASS, Constants.MAX_ID_CLASS, false, ik, configurationMode);
		System.out.println("despues de llamar a convert en InstanceEJB");
		return xml.getRoot();
	}

	private static IndividualData getEnumerated(FactoryConnectionDB factConnDB, IKnowledgeBaseInfoServer ik, DataBaseMap dataBaseMap, boolean configurationMode) throws NotFoundException, 
			IncoherenceInMotorException, SystemException, IncompatibleValueException, CardinalityExceedException, 
			ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		InstanceService m_IS = new InstanceService(factConnDB, null, false);
		m_IS.setIk(ik);
		m_IS.setDataBaseMap(dataBaseMap);
		IndividualData iData = m_IS.serverGetFactsInstanceOfClassSpecialized(Constants.IDTO_ENUMERATED, Constants.USER_SYSTEM, false, 1, false);
		return iData;
	}
	
	//las siguientes funciones declaradas como publicas porque se usan en RulerFactoryBBDD.
	//Desde RulerFactoryBBDD el motor no estaria arrancado, desde RulerFactoryXML si
	public static IndividualData getEnumerated(FactoryConnectionDB factConnDB, DataBaseMap dataBaseMap) throws NotFoundException, 
			IncoherenceInMotorException, SystemException, IncompatibleValueException, CardinalityExceedException, 
			ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		InstanceService m_IS = new InstanceService(factConnDB, null, false);
		m_IS.setDataBaseMap(dataBaseMap);
		IndividualData iData = m_IS.serverGetFactsInstanceOfClassSpecialized(Constants.IDTO_ENUMERATED, Constants.USER_SYSTEM, false, 1, false);
		return iData;
	}

	public static void getRolesUsuario(String user, HashSet<String> roles, HashSet<Integer> idoRoles, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String gestorDB = factConnDB.getGestorDB();
		GenerateSQL gSQL = new GenerateSQL(gestorDB);
		
		String nameTableId = gSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + gSQL.getCharacterEnd();
		String sql = "select " + nameTableId + ", rdn from userrol where " + nameTableId + " in(" + 
			"SELECT " + gSQL.getCharacterBegin() + "userRol" + gSQL.getCharacterEnd() + " FROM " + gSQL.getCharacterBegin() + "user" + gSQL.getCharacterEnd() + 
			" WHERE " + Constants.PROP_RDN + " LIKE '" + user.replaceAll("'", "''") + "')";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer tableIdRol = rs.getInt(1);
				int idoRol = QueryConstants.getIdo(tableIdRol, Constants.IDTO_USERROL);
				String rol = rs.getString(2);
				idoRoles.add(idoRol);
				roles.add(rol);
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

	/*public static String getRulerFile(FactoryConnectionDB factConnDB, String sourceRuler) throws SQLException, NamingException {
		String gestorDB = factConnDB.getGestorDB();
		GenerateSQL generateSQL = new GenerateSQL(gestorDB);
		StringBuffer reglas = new StringBuffer("");
		String sql = "SELECT " + generateSQL.getCharacterBegin() + "content" + generateSQL.getCharacterEnd() + " FROM RulerImports";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				reglas.append(rs.getString(1) + "\n");
			}
			//ik.setRulesString(reglas);
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				con.close();
		}
		
		getRulerFile(reglas, generateSQL, factConnDB, "RulerQuerys");
		String table = null;
		if (sourceRuler.equals(Constants.RULER_APPLET))
			table = "RulerApplet";
		else if (sourceRuler.equals(Constants.RULER_SCHEDULER))
			table = "RulerScheduler";
		if (table!=null)
			getRulerFile(reglas, generateSQL, factConnDB, table);
		//System.out.println("reglas -> " + reglas.toString());
		return reglas.toString();
	}
	private static String getRulerFile(StringBuffer reglas, GenerateSQL generateSQL, FactoryConnectionDB factConnDB, String table) 
			throws SQLException, NamingException {
		//concatenar reglas activadas
		String sql = "SELECT " + generateSQL.getCharacterBegin() + "content" + generateSQL.getCharacterEnd() + " FROM " + table 
			+ " WHERE active='Y'";
			
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				reglas.append(rs.getString(1) + "\n");
			}
			//ik.setRulesString(reglas);
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				con.close();
		}
		return reglas.toString();
	}*/
	
	public static ArrayList<OrderProperty> getOrderProperties(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		String gestorDB = factConnDB.getGestorDB();
		GenerateSQL gSQL = new GenerateSQL(gestorDB);
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT sec, class, prop, " + gSQL.getCharacterBegin() + "order" + gSQL.getCharacterEnd() + " FROM s_orderproperties op ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=op.class) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=op.prop) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<OrderProperty> orderPropertyList = new ArrayList<OrderProperty>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				boolean success=true;
				String sec=rs.getString(1);
				String idto=rs.getString(2);
				String prop=rs.getString(3);
				int order=rs.getInt(4);
				Integer intidto=null;
				Integer intprop=null;
				if (idto!=null){
					intidto=ik!=null?ik.getIdClass(idto):AuxiliarModel.getClassByName(idto, factConnDB);
					if(intidto==null){
						intidto = AuxiliarModel.getClassByName(idto, factConnDB);
						if(intidto==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El IDTO "+idto+" no existe. Siendo sec:"+sec+" prop:"+prop);
						}
					}
				}
				if (prop!=null){
					intprop=ik!=null?ik.getIdProperty(prop):AuxiliarModel.getPropertyByName(prop, factConnDB);
					if(intprop==null){
						intprop = AuxiliarModel.getPropertyByName(prop, factConnDB);
						if(intprop==null){
							success=false;
							System.err.println("WARNING!: MetaData.getAlias => El PROP "+prop+" no existe. Siendo sec:"+sec+" class:"+idto);
						}
					}
				}
				
				if (success){
					OrderProperty orderP=new OrderProperty();
					orderP.setIdto(intidto);
					orderP.setIdtoName(idto);
					orderP.setOrder(order);
					orderP.setProp(intprop);
					orderP.setPropName(prop);
					orderP.setSec(Integer.valueOf(sec));
					
					orderPropertyList.add(orderP);
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
		return orderPropertyList;
	}
	
	
	public static ArrayList<CardMed> getCardMedTable(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT idto_parent_name, idto_name, cardmed FROM s_cardmed cm ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c1 ON (c1.rdn=cm.idto_name) " +
			"LEFT JOIN clase c2 ON (c2.rdn=cm.idto_parent_name) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce1 ON (c1." + cB + "tableId" + cE + "=ce1.dominio) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce2 ON (c2." + cB + "tableId" + cE + "=ce2.dominio) " +
			"WHERE ce1." + cB + "tableId" + cE + " IS NULL AND ce2." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<CardMed> cardmedList = new ArrayList<CardMed>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String idtoParent=rs.getString(1);
				String idto=rs.getString(2);
				Integer cardmed=rs.getInt(3);
				Integer intidtoParent=null;
				Integer intidto=null;
				
				if (idtoParent!=null){
					try{
						intidtoParent=ik.getIdClass(idtoParent);
					}catch(NotFoundException ex){
						if(intidtoParent==null)
							intidtoParent = AuxiliarModel.getClassByName(idtoParent, factConnDB);
					}
				}
				
				if (idto!=null){
					try{
						intidto=ik.getIdClass(idto);
					}catch(NotFoundException ex){
						if(intidto==null)
							intidto = AuxiliarModel.getClassByName(idto, factConnDB);
					}
				}
				
				if ((idto!=null && intidto==null)||(cardmed==null)){
					System.err.println("WARNING!: MetaData.getCardMedTable => El IDTO no existen o bien no se ha asignado una cardinalidad media");
				}else{
					CardMed cm=new CardMed(); 
					cm.setCardmed(cardmed);
					cm.setIdto(intidto);
					cm.setIdtoParent(intidtoParent);
					cm.setIdtoName(idto);
					cm.setIdtoParentName(idtoParent);
					//System.out.println("CM AMI - "+cm.toString());
					cardmedList.add(cm);
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
		return cardmedList;
	}
	
	public static ArrayList<CardMed> getCardMedField(FactoryConnectionDB factConnDB,IKnowledgeBaseInfoServer ik, boolean configurationMode) throws SQLException, NamingException,NotFoundException, IncoherenceInMotorException {
		GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		String sql = "SELECT idto_parent_name, idprop_name, cardmed FROM s_cardmedfield cm ";
		if(!configurationMode){
			sql+="LEFT JOIN clase c ON (c.rdn=cm.idto_parent_name) " +
			"LEFT JOIN v_propiedad p ON (p.rdn=cm.idprop_name) " +
			"LEFT JOIN " + cB + "clase_excluída" + cE + " ce ON (c." + cB + "tableId" + cE + "=ce.dominio) " +
			"LEFT JOIN " + cB + "propiedad_excluída" + cE + " pe ON ((p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY + ") OR (p." + cB + "tableId" + cE + "=pe." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + ")) " +
			"LEFT JOIN " + cB + "propiedad_en_clase_excluída" + cE + " pece ON (c." + cB + "tableId" + cE + "=pece.dominio AND ((p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_DATO" + cE + " AND p.idto=" + Constants.IDTO_DATA_PROPERTY +") OR (p." + cB + "tableId" + cE + "=pece." + cB + "propiedadPROPIEDAD_OBJETO" + cE + " AND p.idto=" + Constants.IDTO_OBJECT_PROPERTY + "))) " +
			"WHERE ce." + cB + "tableId" + cE + " IS NULL AND pe." + cB + "tableId" + cE + " IS NULL AND pece." + cB + "tableId" + cE + " IS NULL";
		}
		ArrayList<CardMed> cardmedList = new ArrayList<CardMed>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String idtoParent=rs.getString(1);
				String idProp=rs.getString(2);
				Integer cardmed=rs.getInt(3);
				Integer intidtoParent=null;
				Integer intidprop=null;
				
				if (idtoParent!=null){
					try{
						intidtoParent=ik.getIdClass(idtoParent);
					}catch(NotFoundException ex){
						if(intidtoParent==null)
							intidtoParent = AuxiliarModel.getClassByName(idtoParent, factConnDB);
					}
				}
				
				if (idProp!=null){
					try{
						intidprop=ik.getIdProperty(idProp);
					}catch(NotFoundException ex){
						if(intidprop==null)
							intidprop = AuxiliarModel.getPropertyByName(idProp, factConnDB);
					}
				}
				
				if ((idProp!=null && intidprop==null)||(cardmed==null)){
					System.err.println("WARNING!: MetaData.getCardMedField => El IdProp no existe o bien no se ha asignado una cardinalidad media");
				}else{
					CardMed cm=new CardMed(); 
					cm.setCardmed(cardmed);
					cm.setIdProp(intidprop);
					cm.setIdtoParent(intidtoParent);
					cm.setIdPropName(idProp);
					cm.setIdtoParentName(idtoParent);
					//System.out.println("CM AMI - "+cm.toString());
					cardmedList.add(cm);
				}
			}
		}catch(Exception ex){
			//Capturada la excepcion para permitir compatibilidad con versiones antiguas
			ex.printStackTrace();
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return cardmedList;
	}
	
}
