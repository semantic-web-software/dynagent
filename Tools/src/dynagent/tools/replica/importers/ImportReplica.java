package dynagent.tools.replica.importers;

import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;

//import dynagent.common.basicobjects.O_Datos_Attrib;
//import dynagent.common.basicobjects.O_Datos_Attrib_Memo;
import dynagent.common.basicobjects.ReplicaConfiguration;
//import dynagent.common.utils.Auxiliar;
//import dynagent.server.database.dao.O_Datos_AttribDAO;
//import dynagent.server.database.dao.O_Datos_Attrib_MemoDAO;
import dynagent.server.database.dao.ReplicaConfigurationDAO;
import dynagent.server.database.dao.TClaseDAO;
//import dynagent.server.ejb.FactoryConnectionDB;
//import dynagent.tools.importers.AuxiliarImporters;

public class ImportReplica {

	private boolean soyCentral = false;	
	//private HashSet<Integer> idtos = null;
	//private HashMap<Integer,ArrayList<ReplicaConfiguration>> hReplication = null;
	
	public ImportReplica(/*HashSet<Integer> aIdtos, HashMap<Integer, ArrayList<ReplicaConfiguration>> hReplication*/) {
		//this.idtos = aIdtos;
		//this.hReplication = hReplication;
	}
	
	
	public HashSet<Integer> getIdtosReplicaDB(boolean actual) throws SQLException, NamingException {
		HashSet<Integer> idtosReplica = new HashSet<Integer>();
		
		ReplicaConfigurationDAO rDao = new ReplicaConfigurationDAO();
		rDao.open();
		LinkedList<Object> lRep = null;
		if (actual)
			lRep = rDao.getAllCond("LOCAL_ORIGIN='S'");
		else
			lRep = rDao.getAllCond("LOCAL_ORIGIN='N'");
		Iterator it = lRep.iterator();
		while (it.hasNext()) {
			ReplicaConfiguration rep = (ReplicaConfiguration)it.next();
			idtosReplica.add(rep.getIdTo());
		}
		rDao.close();
		return idtosReplica;
	}
	public HashSet<Integer> getIdtosReplicaXML(Element xml, boolean actual) throws SQLException, NamingException {
		HashSet<Integer> idtosReplica = new HashSet<Integer>();
		Iterator it = xml.getChildren(ConstantsImportReplica.REPLICATION).iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			Iterator it2 = child.getChildren().iterator();
			while (it2.hasNext()) {
				Element child2 = (Element)it2.next();
				if (actual) {
					if (soyCentral && child2.getName().equals(ConstantsImportReplica.CENTRAL))
						subGetIdtosReplicaXML(idtosReplica, child2);
					else if (!soyCentral && child2.getName().equals(ConstantsImportReplica.TIENDAS))
						subGetIdtosReplicaXML(idtosReplica, child2);
				} else {
					if (!soyCentral && child2.getName().equals(ConstantsImportReplica.CENTRAL))
						subGetIdtosReplicaXML(idtosReplica, child2);
					else if (soyCentral && child2.getName().equals(ConstantsImportReplica.TIENDAS))
						subGetIdtosReplicaXML(idtosReplica, child2);
				}
			}
		}
		return idtosReplica;
	}
	public void subGetIdtosReplicaXML(HashSet<Integer> idtosReplica, Element xml) throws SQLException, NamingException {
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		Iterator it = xml.getChildren().iterator();
		while (it.hasNext()) {
			Element clase = (Element)it.next();
			String classesName = clase.getAttributeValue(ConstantsImportReplica.CLASSES_NAME);
			String[] classesNameSpl = classesName.split(",");
			for (int i=0;i<classesNameSpl.length;i++) {
				String className = classesNameSpl[i];
				Integer idto = tdao.getTClaseByName(className).getIDTO();
				idtosReplica.add(idto);
			}
		}
		tdao.close();
	}

	/*public void deleteDataReplica(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		//iterar por replication para coger los idtos que son de este pc
		//no es necesario buscar en odatosatrib por estos idtos porque a este método se 
		//llamará porque se han borrado los idos
		
		ReplicaConfigurationDAO rDAO = new ReplicaConfigurationDAO();
		rDAO.open();
		HashSet<String> tablesName = new HashSet<String>();
		HashSet<Integer> idtos = new HashSet<Integer>();
		Iterator it = rDAO.getAllCond("ACTUAL_PC='Y'").iterator();
		while (it.hasNext()) {
			ReplicaConfiguration rep = (ReplicaConfiguration)it.next();
			idtos.add(rep.getIdTo());
			String tableName = rep.getTableName();
			if (tableName!=null)
				tablesName.add(tableName);
			String tableNameInst = rep.getTableNameInstancias();
			if (tableNameInst!=null)
				tablesName.add(tableNameInst);
		}
		Iterator it2 = tablesName.iterator();
		while (it2.hasNext()) {
			String tableName = (String)it2.next();
			String sql = "delete from " + tableName + " where id_to in(" + Auxiliar.hashSetIntegerToString(idtos, ",")+ ")";
			System.out.println(sql);
			AuxiliarImporters.dbExecUpdate(fcdb, sql);
		}
		rDAO.close();
	}
	public void startUpdateData() throws SQLException, NamingException {
		//array con idtos
		//mapa con replication
		O_Datos_AttribDAO odatDao = new O_Datos_AttribDAO();
		odatDao.open();
		System.out.println("IDTOS " + Auxiliar.hashSetIntegerToString(idtos, ","));
		LinkedList<Object> llo = odatDao.getAllCond("ID_TO IN(" + Auxiliar.hashSetIntegerToString(idtos, ",") + ")");
		Iterator it = llo.iterator();
		while (it.hasNext()) {
			O_Datos_Attrib oda = (O_Datos_Attrib)it.next();
			int idto = oda.getIDTO();
			//System.out.println("IDTO: " + oda.getIDTO());
			ArrayList<ReplicaConfiguration> aRepl = hReplication.get(idto);
			Iterator it2 = aRepl.iterator();
			while (it2.hasNext()) {
				ReplicaConfiguration r = (ReplicaConfiguration)it2.next();
				Integer idtoSup = r.getIdtoSup();
				if (idtoSup==null) {
					O_Datos_AttribDAO odatDaoRep = new O_Datos_AttribDAO(r.getTableName());
					odatDaoRep.insert(oda);
				} else {
					//buscar el superior para ver si son iguales
					LinkedList<Object> llo2 = odatDao.getAllCond("VAL_NUM=" + oda.getIDO() + " AND PROPERTY=" + r.getPropLink());
					Iterator it3 = llo2.iterator();
					while (it3.hasNext()) {
						O_Datos_Attrib oda2 = (O_Datos_Attrib)it3.next();
						if (oda2.getIDTO().equals(idtoSup)) {
							O_Datos_AttribDAO odatDaoRep = new O_Datos_AttribDAO(r.getTableName());
							//si dejo dominio, origin o ajeno a null no pasa nada 
							//porque cuando se haga una actualización se pondrá su valor correcto 
							//ya que no mira valor anterior
							odatDaoRep.insert(oda);
							break;
						}
					}
				}
			}
		}
		O_Datos_Attrib_MemoDAO odatMemoDao = new O_Datos_Attrib_MemoDAO();
		llo = odatMemoDao.getAllCond("ID_TO IN(" + Auxiliar.hashSetIntegerToString(idtos, ",") + ")");
		it = llo.iterator();
		while (it.hasNext()) {
			O_Datos_Attrib_Memo oda = (O_Datos_Attrib_Memo)it.next();
			int idto = oda.getIDTO();
			//System.out.println("IDTO: " + oda.getIDTO());
			ArrayList<ReplicaConfiguration> aRepl = hReplication.get(idto);
			Iterator it2 = aRepl.iterator();
			while (it2.hasNext()) {
				ReplicaConfiguration r = (ReplicaConfiguration)it2.next();
				Integer idtoSup = r.getIdtoSup();
				if (idtoSup==null) {
					O_Datos_Attrib_MemoDAO odatDaoRep = new O_Datos_Attrib_MemoDAO(r.getTableName());
					odatDaoRep.insert(oda);
				} else {
					//buscar el superior para ver si son iguales
					LinkedList<Object> llo2 = odatDao.getAllCond("VAL_NUM=" + oda.getIDO() + " AND PROPERTY=" + r.getPropLink());
					Iterator it3 = llo2.iterator();
					while (it3.hasNext()) {
						O_Datos_Attrib oda2 = (O_Datos_Attrib)it3.next();
						if (oda2.getIDTO().equals(idtoSup)) {
							O_Datos_Attrib_MemoDAO odatDaoRep = new O_Datos_Attrib_MemoDAO(r.getTableName());
							odatDaoRep.insert(oda);
							break;
						}
					}
				}
			}
		}
		odatDao.close();
	}
	*/
}
