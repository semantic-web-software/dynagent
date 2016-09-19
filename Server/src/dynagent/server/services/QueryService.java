package dynagent.server.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.GlobalClases;
import dynagent.common.communication.AttribValue;
import dynagent.common.communication.queryData;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.selectData;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.exceptions.ConnectionException;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.replication.IReplication;
import dynagent.server.services.querys.DataInfo;
import dynagent.server.services.querys.QueryBasic;
import dynagent.server.services.querys.StructureQuery;

public class QueryService {
	private FactoryConnectionDB factConnDB = null;
	private IKnowledgeBaseInfo ik;
	private DataBaseMap dataBaseMap;
	private HashMap<String,DataInfo> dataInfoXName = null;
	//variables para queryData
	private int[] def;
	private int[] defColSup;
	private int countDef;
	
	public QueryService(FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik, DataBaseMap dataBaseMap) {
		//System.out.println("Inicio de la funcion QueryService");
		this.factConnDB = factConnDB;
		this.ik = ik;
		this.dataBaseMap = dataBaseMap;
		//System.out.println("Fin de la funcion QueryService");
	}

	public HashMap<String, DataInfo> getDataInfoXName() {
		return dataInfoXName;
	}

	private GlobalClases getGlobalClass(Element root, IReplication ir, Integer uTask) {
		//buscar nodos where fijados
		Element structure = root.getChild(QueryConstants.STRUCTURE);
		Element clase = structure.getChild(QueryConstants.CLASS);
		String idtoRootStr = clase.getAttributeValue(QueryConstants.ID_TO);
		System.out.println("idtoRoot " + idtoRootStr);
		HashSet<Integer> hIdtoRoot = Auxiliar.stringToHashSetInteger(idtoRootStr, ",");
		return ir.getGlobalClass(uTask, hIdtoRoot);
	}
	public queryData Query(Element root, IReplication ir, Integer uTask, int mode,boolean idosCompresed) 
			throws JDOMException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException, ConnectionException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion Query(root)");
		String sqlCompleta = dbFindQuery(root,idosCompresed);
		//String databaseIP = null;
		//Integer port = null;
		
		int business = 0;
		System.out.println("userTask " + uTask);
		boolean change = false;
		boolean centralized = false;
		boolean seHaceReplicaSoyTienda = ir!=null && ir.getIPCentral()!=null;
		if (seHaceReplicaSoyTienda && uTask!=null) {
			GlobalClases gc = getGlobalClass(root, ir, uTask);
			change = gc!=null;
			if (change)
				centralized = gc.isCentralized();
		}
		
		queryData response = null;
		if (centralized) {
			//si es centralizado no busca en local
			System.out.println("change " + change + " sin busqueda previa local");
		} else {
			System.out.println("change " + change + " si no hay resultados");
			response = dbFindQueryData(root, sqlCompleta, mode);
		}
			
		//CAMBIO: BUSCA EN LOCAL, SI NO DEVUELVE RESULTADOS BUSCA EN CENTRAL
		if (centralized || (change && response!=null && !response.hasData())) {
			if (!centralized)
				System.out.println("no hay resultado en local y change " + change + "-> busca en Central");
			factConnDB.setStandAloneApp(true);
			//databaseIP = factConnDB.getDatabaseIP();
			//port = factConnDB.getPort();
			business = factConnDB.getBusiness();
			factConnDB.setDatabaseIP(ir.getIPCentral());
			factConnDB.setPort(ir.getPortCentral());
			factConnDB.setBusiness(ir.getBusinessCentral());
			factConnDB.setCountSleep(5);
			
			response = dbFindQueryData(root, sqlCompleta, mode);
			System.out.println("change finally");
			
			factConnDB.restoreCountSleep();
			factConnDB.setStandAloneApp(false);
			//factConnDB.setDatabaseIP(databaseIP);
			//factConnDB.setPort(port);
			factConnDB.setBusiness(business); //bajo JBoss solo es necesaria la empresa
		}
		return response;
	}
	
	/** Este metodo se usa para querys almacenadas en BD. 
	 * @throws NoSuchColumnException */
	public selectData QuerySD(Element root, int mode,boolean idosCompresed) throws JDOMException, SQLException, NamingException, DataErrorException, NotFoundException, IncoherenceInMotorException, NoSuchColumnException {
		//System.out.println("Inicio de la funcion QuerySD(root)");
		String sqlCompleta = dbFindQuery(root,idosCompresed);
		selectData response = dbFindSelectData(root, sqlCompleta, mode);
		//System.out.println("Fin de la funcion QuerySD(root)");
		return response;
	}
	
	/** funcion que se encarga de generar sql a partir de un xml.*/
	public String dbFindQuery(Element root,boolean idosCompresed) throws JDOMException, NotFoundException, IncoherenceInMotorException, NoSuchColumnException, DataErrorException {
		//System.out.println("Inicio de la funcion dbFindQuery");
		System.out.println("XML de entrada: \n" + jdomParser.returnXML(root));
		
		Element nodoStructure = root.getChild(QueryConstants.STRUCTURE);
		StructureQuery sq = null;
		if (nodoStructure!=null) {
			GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
			QueryBasic qBasic = new QueryBasic(ik, gSQL, dataBaseMap);
			sq = qBasic.createQueryBasic(nodoStructure,idosCompresed);
			def = qBasic.getDef();
			defColSup = qBasic.getDefColSup();
			countDef = qBasic.getCountDef();
			dataInfoXName = qBasic.getDataInfoXName();
		}
		//System.out.println("XML de salida: \n" + jdomParser.returnXML(root));
		String sqlCompleta = sq.toString();
		System.out.println("QUERY de salida: \n" + sqlCompleta);
		//System.out.println("Fin de la funcion dbFindQuery");
		return sqlCompleta;
	}
	
	/** funcion que dada una consulta crea un queryData con dbGetVirtualInstanceQueryFormat.*/
	private queryData dbFindQueryData(Element root, String query, int mode) throws SQLException, NamingException {
		//System.out.println("Inicio de la funcion dbFindQueryData");
		queryData response = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		
		try {
			st = con.getBusinessConn().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			long ini = System.currentTimeMillis();
			rs = st.executeQuery(query);
			System.out.println("EJECUTADA SQL, time: " + (System.currentTimeMillis() - ini));
			ini = System.currentTimeMillis();
			response = dbGetVirtualInstanceQueryFormat(rs, root, mode);
			System.out.println("QUERY_DATA:RESPUESTA, time:" + (System.currentTimeMillis() - ini));
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		//System.out.println("Fin de la funcion dbFindQueryData");
		return response;
	}
	
	/** Crea un queryData y llama a guardaEnQuery para que almacene los valores del ResultSet de la consulta en 1.*/
	private queryData dbGetVirtualInstanceQueryFormat(ResultSet rs, Element root, int mode) throws SQLException {
		//System.out.println("Inicio de la funcion dbGetVirtualInstanceQueryFormat");
		rs.last();
		int rows = rs.getRow();
		rs.beforeFirst();
		
		//System.out.println("ROWS " + rows);
		rows = rows > 0 ? rows : 1;

		//columns y columnSize se pueden guardar a la vez que se crea la consulta
		//guardar en un vector los tamaños:
				//si quantity -> 4
				//si string o memo -> 200
				//guardar mejor las constantes TM

		//System.out.println("contadorDef  " + countDef);    //xa 18 filas con 2 enumerados aqui saldria 20
		AttribValue[] aV = new AttribValue[1];
		Element viewRoot = root.getChild(QueryConstants.STRUCTURE).getChild(QueryConstants.PRESENTATION).getChild(QueryConstants.VIEW);
		
		aV[0] = new AttribValue(QueryConstants.SELECT_IDO,viewRoot.getAttributeValue(QueryConstants.SELECT_IDO));
		queryData qd = guardaEnQuery(rs, rows, aV, mode);
		qd.endFile();
		
		//System.out.println("Datos " + qd.toString());
		
		// System.out.println("TIME TOTAL:"+
		// (System.currentTimeMillis()-iniGlobal)+","+"NEWO:"+qd.tO+",TA:"+qd.tA+",INT:"+tI+",STR:"+tS+",MAP:"+tM+"BLOCK:1:"+tB1+"."+tB11+"."+tB12+"."+tB13+"."+tB14+"."+tB15+","+tB2+","+tB3);
		//System.out.println("Fin de la funcion dbGetVirtualInstanceQueryFormat");
		return qd;
	}
	
	/** Almacena los datos del ResultSet pasado como parámetro en un queryData.*/
	private queryData guardaEnQuery(ResultSet rs, int rows, AttribValue[] aV, int mode) throws SQLException {
//		System.out.println("Inicio de la funcion guardaEnQuery");
		/*if (mode==queryData.MODE_ROW)
			System.out.println("mode MODE_ROW");
		else if (mode==queryData.MODE_ROOT)
			System.out.println("mode MODE_ROOT");*/
		
		//System.out.println("countDef " + countDef);
		queryData qd = new queryData(rows, countDef, aV, true, mode);
		while (rs.next()) {
			//int countWithEnum = countDef;
			int c = 1;
			int contDef = 1;
			//System.out.println("DEF");
			//for (int i=0;i<def.length;i++)
			//	System.out.println(i + ": " + def[i]);
			
			for (int countWithEnum = 1; countWithEnum <= countDef; countWithEnum++) {
				//System.out.println("columna " + c);
				int tm = def[contDef-1];
				if (tm == QueryConstants.TM_ID) {
					//System.out.println("int");
					Integer val = rs.getInt(c);
					if (rs.wasNull())
						val = null;
					if (countWithEnum==1) {
						qd.newRow();
						qd.newVal(val);
					} else {
						if (val==null)
							qd.nullColumn();
						else
							qd.newVal(val);
					}
				} else if (tm == QueryConstants.TM_IDO_FICTICIO) {
					//System.out.println("ido");
					int val = c;
					qd.newVal(val);
				} else{
					//System.out.println("int, string o double");
					String val = rs.getString(c);
					if (rs.wasNull())
						qd.nullColumn();
					else {
						if (tm==Constants.IDTO_STRING || tm==Constants.IDTO_MEMO )													
							qd.newVal((String)val);
						else if( tm==Constants.IDTO_IMAGE){
							if(val!=null) qd.newVal(Constants.smallImage+(String)val);
						}else if (tm==Constants.IDTO_BOOLEAN && factConnDB.getGestorDB().equals(GestorsDBConstants.postgreSQL))
							qd.newVal(val.equals("t")?new Double(1):new Double(0));
						else
							qd.newVal(Double.parseDouble(val));
					}
				}
				if (tm != QueryConstants.TM_IDO_FICTICIO)
					c++;
				contDef++;
			}
			qd.endRow();
		}
//		System.out.println("QueryData " + qd);
//		System.out.println("Fin de la funcion guardaEnQuery");
		return qd;
	}
	
	/** funcion que dada una consulta crea un queryData con dbGetVirtualInstanceQueryFormat.*/
	private selectData dbFindSelectData(Element root, String query, int mode) throws SQLException, NamingException, DataErrorException {
		//System.out.println("Inicio de la funcion dbFindQueryData");
		selectData sd = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			//long ini = System.currentTimeMillis();
			rs = st.executeQuery(query);
			//System.out.println("EJECUTADA SQL, time: " + (System.currentTimeMillis() - ini));
			//ini = System.currentTimeMillis();
			sd = queryData.parse(root, rs, ik, mode);
			//System.out.println("VIRTUAL:RESPUESTA, time:" + (System.currentTimeMillis() - ini));
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		//System.out.println("Fin de la funcion dbFindQueryData");
		return sd;
	}
}
