package dynagent.tools.replica.importers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.basicobjects.NoModifyDB;
import dynagent.common.basicobjects.ReplicaConfiguration;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.dao.ConfigurationDAO;
import dynagent.server.database.dao.NoModifyDBDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.ReplicaConfigurationDAO;
import dynagent.server.database.dao.TClaseDAO;

public class ParserReplicaXML {
	private boolean soyCentral = false;	
	//private HashSet<Integer> idtos = null;
	//private HashMap<Integer,ArrayList<ReplicaConfiguration>> hReplication = null;
	
	public ParserReplicaXML() {
		//idtos = new HashSet<Integer>();
		//hReplication = new HashMap<Integer, ArrayList<ReplicaConfiguration>>();
	}

	/*public HashSet<Integer> getIdtos() {
		return idtos;
	}
	public HashMap<Integer, ArrayList<ReplicaConfiguration>> getHReplication() {
		return hReplication;
	}*/
	public boolean isSoyCentral() {
		return soyCentral;
	}

	public Element readXML(String path) throws IOException, JDOMException {
		BufferedReader in = new BufferedReader(new FileReader(path)); 
		StringBuffer dataS = new StringBuffer("");
		String buff = "";
		while(buff!= null){
			dataS.append(buff);
			buff = in.readLine();
		}
		Element xml = jdomParser.readXML(dataS.toString()).getRootElement();
		//System.out.println(jdomParser.returnXML(xml));
		soyCentral(xml);
		return xml;
	}
	private void soyCentral(Element xml) {
		try {
			String rdnCentral = xml.getAttributeValue(ConstantsImportReplica.ALMACEN_CENTRAL);
			//coger de configuration el almacen
			ConfigurationDAO cDao = new ConfigurationDAO();
			cDao.open();
			LinkedList<Object> lObj = cDao.getByID("mi_almacen");
			cDao.close();
			if (lObj.size()==0)
				throw new Exception("Debe indicar en la tabla Configuration de que almacén se trata");
			else {
				System.out.println(lObj.getFirst());
				String actualAlmacen = (String)lObj.getFirst();
				if (actualAlmacen.equals(rdnCentral))
					soyCentral = true;
				System.out.println("soyCentral " + soyCentral);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startUpdateConfiguration(Element xml) throws SQLException, NamingException, IOException, JDOMException {
		System.err.println("Iniciando configuración");
		try {
			deleteAllConfiguration();
			parserImportXML(xml);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void deleteAllConfiguration() throws SQLException, NamingException {
		System.out.println("Borrando antigua configuración");
		ReplicaConfigurationDAO rDao = new ReplicaConfigurationDAO();
		NoModifyDBDAO nmDao = new NoModifyDBDAO();
		rDao.open();
		rDao.deleteAll();
		nmDao.deleteAll();
		rDao.close();
	}
	private void parserImportXML(Element xml) throws SQLException, NamingException, DataErrorException, JDOMException {
		Iterator it = xml.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(ConstantsImportReplica.REPLICATION))
				parserReplication(child);
			else if (child.getName().equals(ConstantsImportReplica.NOMODIFYDB))
				parserNoModifyDB(child);
		}
	}

	private void parserReplication(Element replication) throws SQLException, NamingException, DataErrorException, JDOMException {
		Iterator it = replication.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(ConstantsImportReplica.CENTRAL))
				parserReplication(child, true);
			else if (child.getName().equals(ConstantsImportReplica.TIENDAS))
				parserReplication(child, false);
		}
	}
	
	private void parserReplication(Element centralTiendas, boolean configuracionCentral) throws SQLException, NamingException, DataErrorException, JDOMException {
		Iterator it = centralTiendas.getChildren().iterator();
		while (it.hasNext()) {
			Element clase = (Element)it.next();
			parserReplicationClass(clase, configuracionCentral);
		}
	}
	
	private void parserReplicationClass(Element clase, boolean configuracionCentral) throws SQLException, NamingException, DataErrorException, JDOMException {
		String classesName = clase.getAttributeValue(ConstantsImportReplica.CLASSES_NAME);
		String classesNameSup = clase.getAttributeValue(ConstantsImportReplica.CLASSES_NAME_SUP);
		String propLinkStr = clase.getAttributeValue(ConstantsImportReplica.PROP_LINK);
		
		TClaseDAO tdao = new TClaseDAO();
		tdao.open();
		PropertiesDAO pdao = new PropertiesDAO();
		pdao.open();
		
		Integer propLink = null;
		if (propLinkStr!=null)
			propLink = pdao.getIdPropByName(propLinkStr);
		
		ReplicaConfigurationDAO rDao = new ReplicaConfigurationDAO();
		String[] classesNameSpl = classesName.split(",");
		for (int i=0;i<classesNameSpl.length;i++) {
			String className = classesNameSpl[i];
			Integer idto = tdao.getTClaseByName(className).getIDTO();
			
			if (classesNameSup!=null) {
				String[] classesNameSupSpl = classesNameSup.split(",");
				for (int j=0;j<classesNameSupSpl.length;j++) {
					String classNameSup = classesNameSupSpl[j];
					Integer idtoSup = tdao.getTClaseByName(classNameSup).getIDTO();
					ReplicaConfiguration r = null;
					if (configuracionCentral)
						r = new ReplicaConfiguration(idto,propLink,idtoSup,soyCentral);
					else
						r = new ReplicaConfiguration(idto,propLink,idtoSup,!soyCentral);
					rDao.insert(r);
					
					/*if (r.isLocalOrigin() && (destinationIsActual==null || destinationIsActual)) {
						//configuracion del PC actual, excluyendo contribuciones
						idtos.add(idto);
						ArrayList<ReplicaConfiguration> aRepl = hReplication.get(idto);
						if (aRepl==null)
							aRepl = new ArrayList<ReplicaConfiguration>();
						aRepl.add(r);
						hReplication.put(idto, aRepl);
					}*/
				}
			} else {
				if (propLink!=null)
					throw new DataErrorException("Property de enlace rellena, pero clase superior no en nodo: " + jdomParser.returnNodeXML(clase));
				else {
					ReplicaConfiguration r = null;
					if (configuracionCentral)
						r = new ReplicaConfiguration(idto,propLink,null,soyCentral);
					else
						r = new ReplicaConfiguration(idto,propLink,null,!soyCentral);
					rDao.insert(r);
				}
			}
		}
		tdao.close();
	}
	
	private void parserNoModifyDB(Element replication) throws SQLException, NamingException {
		Iterator it = replication.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			if (child.getName().equals(ConstantsImportReplica.CENTRAL))
				parserNoModifyDB(child, true);
			else if (child.getName().equals(ConstantsImportReplica.TIENDAS))
				parserNoModifyDB(child, false);
		}
	}
	
	private void parserNoModifyDB(Element centralTiendas, boolean configuracionCentral) throws SQLException, NamingException {
		Iterator it = centralTiendas.getChildren().iterator();
		while (it.hasNext()) {
			Element clase = (Element)it.next();
			String classesName = clase.getAttributeValue(ConstantsImportReplica.CLASSES_NAME);
			
			TClaseDAO tdao = new TClaseDAO();
			tdao.open();
			
			NoModifyDBDAO rDao = new NoModifyDBDAO();
			String[] classesNameSpl = classesName.split(",");
			for (int i=0;i<classesNameSpl.length;i++) {
				String className = classesNameSpl[i];
				Integer idto = tdao.getTClaseByName(className).getIDTO();
				NoModifyDB r = null;
				if (configuracionCentral)
					r = new NoModifyDB(idto,soyCentral);
				else
					r = new NoModifyDB(idto,!soyCentral);
				rDao.insert(r);
			}
			tdao.close();
		}
	}

}
