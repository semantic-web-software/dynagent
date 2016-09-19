package dynagent.tools.replica.restore;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ConfigurationValues;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Datos_Attrib_Memo;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.ConfigurationDAO;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.tools.importers.Connect;
import dynagent.tools.importers.configxml.ConfigData;
import dynagent.tools.importers.configxml.ConstantsXML;
import dynagent.tools.importers.configxml.importDefValues;
import dynagent.tools.importers.configxml.importIndex;
import dynagent.tools.replica.installers.Install;

public class Restore {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	private final static long MAXCONTFILE = 1048576;
	//valor por defecto de la variable max_allowed_packet en mySQL

	public static void main(String[] args) throws Exception {
		/*EJEMPLO PARAMETROS DEL MAIN
		 * 
		 * 192.168.1.3
		 * 3306
		 * mySQL
		 * 16
		 * */
		
		String ip=args[0];
		Integer port = Integer.parseInt(args[1]);
		String gestor = args[2];
		int nbusiness = Integer.parseInt(args[3]);
		String pathXmlFile = args[4];
		
		FactoryConnectionDB fcdb=Restore.setConnection(nbusiness, ip, gestor, true);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		fcdb.setPort(port);

		System.out.println("\n======================= INFO: Se va a restaurar en la bbdd=dyna"+nbusiness+" con el gestorBBDD="+gestor+" IP="+ip+" puerto="+port);
		System.out.println("MAXCONTFILE " + MAXCONTFILE);
		
		try {
			new Restore(fcdb, pathXmlFile);
			
			fcdb.removeConnections();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		System.out.println("*****bye******");
		//System.exit(0);
	}
	
	private static void updateReplicaCont(FactoryConnectionDB fcdb, FactoryConnectionDB fcdbSource, String sufixTienda) throws NamingException, SQLException {
		int maxId = 0;
		String sql = "SELECT max(id) FROM Replica_Autonum";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdbSource.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				maxId = rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdbSource.close(con);
		}
		if (maxId!=0) {
			sql = "UPDATE replicated_local_position SET ID=" + maxId + " WHERE TIENDA='" + sufixTienda + "'";
			int rows = AuxiliarQuery.dbExecUpdate(fcdbSource, sql, true);
			if (rows==0) {
				sql = "INSERT INTO replicated_local_position(ID,TIENDA) VALUES(" + maxId + ",'" + sufixTienda + "')";
				AuxiliarQuery.dbExecUpdate(fcdbSource, sql, true);
			}
			sql = "DELETE FROM replicated_remote_position WHERE TIENDA='" + sufixTienda + "'";
			AuxiliarQuery.dbExecUpdate(fcdbSource, sql, true);
			
			sql = "UPDATE replicated_remote_position SET ID=" + maxId;
			rows = AuxiliarQuery.dbExecUpdate(fcdbSource, sql, true);
			if (rows==0) {
				sql = "INSERT INTO replicated_remote_position(ID) VALUES(" + maxId + ")";
				AuxiliarQuery.dbExecUpdate(fcdbSource, sql, true);
			}
		}
	}

	public Restore(FactoryConnectionDB fcdb, String pathXmlFile) throws Exception {
		ConfigurationValues config = AuxiliarRestoreDB.getConfiguration(fcdb);
		String ipCentral = config.getIPCentral();
		int portCentral = config.getPortCentral();
		int businessCentral = config.getBusinessCentral();
		FactoryConnectionDB fcdbSource = setConnection(businessCentral, ipCentral, fcdb.getGestorDB(), false);
		fcdbSource.setPort(portCentral);
		
		String sufix = config.getSufixTienda();
		//actualizar contadores de replica en central y tienda
		updateReplicaCont(fcdb, fcdbSource, sufix);

		//primero obtener individuos
		String almacen = config.getMiAlmacen();
		ArrayList<O_Datos_Attrib> created = new ArrayList<O_Datos_Attrib>();
		ArrayList<O_Datos_Attrib> listDataReplica=AuxiliarRestoreDB.getO_Datos_Atrib(almacen, sufix, created, fcdbSource);//Metodo que solo devuelve los o_datos_atrib replicables

		ArrayList<O_Datos_Attrib_Memo> createdMemo = new ArrayList<O_Datos_Attrib_Memo>();
		ArrayList<O_Datos_Attrib_Memo> listDataReplicaMemo=AuxiliarRestoreDB.getO_Datos_Atrib_Memo(almacen, sufix, createdMemo, fcdbSource);//Metodo que solo devuelve los o_datos_atrib replicables

		if (created.size()>0) {
			//borrar indices y valores por defecto
			deleteIndexAndDefValues(fcdb);
		}

		//escribir en StringBuffer
		StringBuffer updates = new StringBuffer("");
		StringBuffer allInsert = new StringBuffer("");
		StringBuffer allInsertOReg = new StringBuffer("");
		writeODatosAtrib(listDataReplica, created, fcdb, updates, allInsert, allInsertOReg);
		writeODatosAtribMemo(listDataReplicaMemo, createdMemo, fcdb, updates, allInsert);
		
		String pathFile = "./file.sql";
		//escribir en fichero
		writeFile(pathFile, updates, allInsert, allInsertOReg);
		
		//ejecutar fichero
		File fq = new File(pathFile);
		Install.executeFile(fq, fcdb);
		
		System.out.println("Ejecucion del fichero completada");
		
		if (created.size()>0) {
			BufferedReader in = new BufferedReader(new FileReader(pathXmlFile)); 
			String dataS="", buff="";
			while(buff!= null){
				dataS+=buff;
				buff=in.readLine();
			}
			
			Document configDOC=jdomParser.readXML(dataS);
			Element configXML=configDOC.getRootElement();
			
			Element indexXML=null;//configXML.getChild(ConstantsXML.INDEX_ROOT);
			Element defValuesXML=configXML.getChild(ConstantsXML.DEFAULT_VALUES);
			ConfigData configImport=new ConfigData();
			String sufixIndex = getSufixIndex();
			importIndex ind=new importIndex(indexXML,fcdb,sufixIndex,true,configImport,null);
			InstanceService m_IS = new InstanceService(fcdb, null, false);
			Connect.connectRuler(fcdb, m_IS);
			IKnowledgeBaseInfo ik=m_IS.getIk();
			importDefValues idef=new importDefValues(defValuesXML,fcdb, sufixIndex,configImport,null,ik);
			
			//insertar indices y valores por defecto
			boolean success = true;
			if (indexXML!=null){
				System.out.println("---> Configurando los indices");
				if(!ind.configData())
					success=false;
			}
			if (defValuesXML!=null){
				System.out.println("---> Configurando los valores por defecto");
				if(!idef.configData())
					success=false;
			}
			if (success) {
				ind.importData();
				idef.importData();
			}
		}
	}
	
	private void deleteIndexAndDefValues(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		Integer idtoIndex = dynagent.tools.importers.configxml.Auxiliar.getIdtoClassFromTableClass(Constants.CLS_FORMATO_INDICE, fcdb);
		Integer idtoDefVal = dynagent.tools.importers.configxml.Auxiliar.getIdtoClassFromTableClass(Constants.CLS_DEFAULT_VALUE, fcdb);
		String sql="DELETE FROM O_Datos_Atrib where ID_TO IN(" + idtoIndex + "," + idtoDefVal + ")";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql="DELETE FROM O_Reg_Instancias_Index where ID_TO IN(" + idtoIndex + "," + idtoDefVal + ")";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		System.err.println("....info: se han borrado los indices y los valores por defecto");
	}
	
	private String getSufixIndex() throws SQLException, NamingException {
		String sufixIndex = null;
		ConfigurationDAO cDao = new ConfigurationDAO();
		cDao.open();
		LinkedList<Object> lObj = cDao.getByID("sufix_tienda");
		if (lObj.size()>0) {
			System.out.println("sufix_tienda " + lObj.getFirst());
			sufixIndex = (String)lObj.getFirst();
		}
		cDao.close();
		return sufixIndex;
	}
	public void writeFile(String file, StringBuffer updates, StringBuffer allInsert, StringBuffer allInsertOReg) throws IOException {
		//al escribir en fichero que lo codifique como utf8 si no se come los acentos
		File f = new File(file);
		f.delete();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter
                 (new FileOutputStream(file)/*,"UTF8"*/));
		out.write(updates.toString());
		allInsert.append("\n");
		out.write(allInsert.toString());
		out.write(allInsertOReg.toString());
		out.close();
		System.out.println("Escritura en fichero completada");
	}
	
	private long addDataInsert(long cont, StringBuffer newComplete, StringBuffer newIncomplete, StringBuffer allInsert) {
		int sizeIncomplete = newIncomplete.length() + 1;
		
		//si contODA + size del incompleto es mayor que el maximo -> cerrar fichero y añadir sql completa en un nuevo insert
		//si contODA + size es menor o igual -> añadir e incrementar contODA
		long size = (cont+sizeIncomplete);
		if (cont==0 || size>MAXCONTFILE) {
			if (cont!=0) {
				allInsert.append(";" + "\n");
				System.out.println("superado " + size);
			}
			allInsert.append(newComplete);
			int sizeComplete = newComplete.length();
			cont = sizeComplete;
		} else {
			allInsert.append(newIncomplete);
			cont += sizeIncomplete;
		}
		return cont;
	}
	public void writeODatosAtrib(ArrayList<O_Datos_Attrib> listDataSource, ArrayList<O_Datos_Attrib> creado, 
			FactoryConnectionDB fcdb, StringBuffer updates, StringBuffer allInsertData, StringBuffer allInsertOReg) throws NamingException, SQLException {
		if(!listDataSource.isEmpty()){
			long contODA = 0;
			long contOReg = 0;
			Iterator<O_Datos_Attrib> itrSource=listDataSource.iterator();
			while(itrSource.hasNext()){
				O_Datos_Attrib data=itrSource.next();
				
				StringBuffer newDataComplete = new StringBuffer("");
				StringBuffer newDataIncomplete = new StringBuffer("");
				boolean insert = AuxiliarRestoreDB.insertO_Datos_Atrib(data, fcdb, newDataComplete, newDataIncomplete, updates);
				if (insert)
					contODA = addDataInsert(contODA, newDataComplete, newDataIncomplete, allInsertData);
				
				if(data.getPROPERTY().equals(Constants.IdPROP_RDN)) {
					//si no es mi sufijo o es un individuo contributivo
					if (insert) {
						StringBuffer newORegReplComplete = new StringBuffer("");
						StringBuffer newORegReplIncomplete = new StringBuffer("");
						AuxiliarRestoreDB.insertO_Reg_Instancias_Index_Replica(data.getIDO(), data.getIDTO(), data.getVALTEXTO(), fcdb, newORegReplComplete, newORegReplIncomplete);
						contOReg = addDataInsert(contOReg, newORegReplComplete, newORegReplIncomplete, allInsertOReg);
					} else
						AuxiliarRestoreDB.updateO_Reg_Instancias_Index_Replica(data.getIDO(), data.getIDTO(), data.getVALTEXTO(), fcdb, updates);
				}
			}
			if (contOReg>0) {
				allInsertOReg.append(";" + "\n");
				contOReg = 0;
			}
			itrSource=creado.iterator();
			while(itrSource.hasNext()){
				O_Datos_Attrib data=itrSource.next();
				StringBuffer newDataComplete = new StringBuffer("");
				StringBuffer newDataIncomplete = new StringBuffer("");
				boolean insert = AuxiliarRestoreDB.insertO_Datos_Atrib(data, fcdb, newDataComplete, newDataIncomplete, updates);
				if (insert)
					contODA = addDataInsert(contODA, newDataComplete, newDataIncomplete, allInsertData);
				
				if(data.getPROPERTY().equals(Constants.IdPROP_RDN)) {
					//si es mio y no es un individuo contributivo
					if(insert) {
						StringBuffer newORegComplete = new StringBuffer("");
						StringBuffer newORegIncomplete = new StringBuffer("");
						AuxiliarRestoreDB.insertO_Reg_Instancias_Index(data.getIDO(), data.getIDTO(), data.getVALTEXTO(), fcdb, newORegComplete, newORegIncomplete);
						contOReg = addDataInsert(contOReg, newORegComplete, newORegIncomplete, allInsertOReg);
					}
					else
						AuxiliarRestoreDB.updateO_Reg_Instancias_Index(data.getIDO(), data.getIDTO(), data.getVALTEXTO(), fcdb, updates);
				}
			}
			if (contOReg>0)
				allInsertOReg.append(";" + "\n");
			if (contODA>0)
				allInsertData.append(";" + "\n");
		}else{
			System.err.println("WARNING: Ningún registro obtenido al consultar los datos de la Central");
		}
	}
	public void writeODatosAtribMemo(ArrayList<O_Datos_Attrib_Memo> listDataSource, ArrayList<O_Datos_Attrib_Memo> creado, 
			FactoryConnectionDB fcdb, StringBuffer updates, StringBuffer allInsertData) throws NamingException, SQLException {
		long contODA = 0;
		Iterator<O_Datos_Attrib_Memo> itrSource=listDataSource.iterator();
		while(itrSource.hasNext()){
			O_Datos_Attrib_Memo data=itrSource.next();
			StringBuffer newDataComplete = new StringBuffer("");
			StringBuffer newDataIncomplete = new StringBuffer("");
			AuxiliarRestoreDB.insertO_Datos_Atrib_Memo(data, fcdb, newDataComplete, newDataIncomplete, updates);
			contODA = addDataInsert(contODA, newDataComplete, newDataIncomplete, allInsertData);
		}
		itrSource=creado.iterator();
		while(itrSource.hasNext()){
			O_Datos_Attrib_Memo data=itrSource.next();
			StringBuffer newDataComplete = new StringBuffer("");
			StringBuffer newDataIncomplete = new StringBuffer("");
			AuxiliarRestoreDB.insertO_Datos_Atrib_Memo(data, fcdb, newDataComplete, newDataIncomplete, updates);
			contODA = addDataInsert(contODA, newDataComplete, newDataIncomplete, allInsertData);
		}
		if (contODA>0)
			allInsertData.append(";");
	}

	public static FactoryConnectionDB setConnection(int snbusiness,String ip,String gestor, boolean setDAO){
		FactoryConnectionDB fcdb = new FactoryConnectionDB(snbusiness,true,ip,gestor);
		if (setDAO) {
			System.out.println("setDAO " + snbusiness);
			DAOManager.getInstance().setBusiness(String.valueOf(snbusiness));
			DAOManager.getInstance().setFactConnDB(fcdb);
			DAOManager.getInstance().setCommit(true);
		}
		return fcdb;
	}
	
/*	private static HashSet<Integer> getAllIndexClass() throws SQLException, NamingException {
		HashSet<Integer> allIndexClass = new HashSet<Integer>();
		IndexDAO iDAO = new IndexDAO();
		iDAO.open();
		LinkedList<Object> llo = (LinkedList<Object>)iDAO.getAll();
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			Index ind = (Index)it.next();
			allIndexClass.add(ind.getIdto());
		}
		iDAO.close();

		return allIndexClass;
	}
	private static void updateIndex(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		HashSet<Integer> allIndexClass = getAllIndexClass();
		ArrayList<Index> aIndex = new ArrayList<Index>();
		IndexDAO indDAO = new IndexDAO();
		indDAO.open();
		LinkedList<Object> llo = indDAO.getAll();
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			Index in = (Index)it.next();
			Integer indexint = in.getIndex();
			
			String lastPrefixTemp = IndexFilterFunctions.getLastPrefixTemp(in.getIdto(), in.getMascPrefixTemp(), in.getPropPrefixTemp(), in.getContYear(), in.getPropFilter(), in.getValueFilter(), fcdb);
			if (lastPrefixTemp!=null)
				in.setLastPrefixTemp(lastPrefixTemp);
			int actualIndex = IndexFilterFunctions.getActualIndex(in.getIdto(),in.getPrefix(),in.getSufix(),in.getPropPrefix(),null, in.getMascPrefixTemp(), in.getPropPrefixTemp(), in.getContYear(), lastPrefixTemp, fcdb, allIndexClass);
			if (actualIndex>=indexint)
				aIndex.add(new Index(in.getIdo(), in.getIdto(), in.getProperty(), in.getMascPrefixTemp(), in.getPropPrefixTemp(), in.getContYear(), in.getLastPrefixTemp(), in.getPrefix(), in.getPropPrefix(), actualIndex+1, in.getSufix(), in.isGlobalSufix(), in.getPropFilter(), in.getValueFilter()));
		}
		System.out.println("cambios " + aIndex.size());
		//String gestor = fcdb.getGestorDB();
		//GenerateSQL gSQL = new GenerateSQL(gestor);
		it = aIndex.iterator();
		while (it.hasNext()) {
			Index in = (Index)it.next();
			System.out.println("actualizando " + in.getIdto() + " " + in.getIndex());
//			String where = "id_to="+in.getIdto() + " and property=" + in.getProperty();
//			if (in.getPropertyFilter()==null)
//				where += " and property_filter is null";
//			else
//				where += " and property_filter=" + in.getPropertyFilter();
//			if (in.getValueFilter()==null)
//				where += " and value_filter is null";
//			else
//				where += " and value_filter='" + in.getValueFilter() + "'";
//			
//			indDAO.update(gSQL.getCharacterBegin() + "index" + gSQL.getCharacterEnd() + "=" + in.getIndex(), where);
			String nameProp = Constants.PROP_INICIO_CONTADOR;
			String sqlPropIndex = "(SELECT PROP FROM PROPERTIES WHERE NAME='" + nameProp + "')";
			O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
			String set = "Q_MIN=" + in.getIndex() + ",Q_MAX=" + in.getIndex();
			String where = "ID_O=" + in.getIdo() + " AND PROPERTY=" + sqlPropIndex;
			odatDAO.update(set, where);
		}
		indDAO.close();
	}*/
}
