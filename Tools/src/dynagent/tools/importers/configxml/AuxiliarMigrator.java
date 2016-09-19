package dynagent.tools.importers.configxml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.FileException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.knowledge.Category;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.common.xml.XMLTransformer;
import dynagent.migration.relational.Migrator;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.IQueryInfoColumn;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.View;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.InstanceService;
import dynagent.server.services.QueryService2;
import dynagent.server.services.XMLConstants;
import dynagent.tools.importers.IImporter;
import dynagent.tools.importers.ImporterFactory;
import dynagent.tools.importers.owl.OWLParser;
import dynagent.tools.owl.OWLIds;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class AuxiliarMigrator {
	
	
	public AuxiliarMigrator() {
		
	}
	
	private FactoryConnectionDB create_Temporal_DBWithSystemTablesAndIDs(IImporter importer, String ip, Integer port, int nbusiness, String gestorDB,String pwd) throws SQLException, NamingException, IOException {
		//crear nueva base de datos temporal
		int nbusinessTmp = nbusiness*10000;
		importer.setHost(ip);
		importer.dropDataBase(nbusinessTmp);
		importer.createDataBase(nbusinessTmp);

		//crear fabrica nueva
		FactoryConnectionDB fcdbTmp = Menu.setConnection(String.valueOf(nbusinessTmp), ip, port, gestorDB);
		fcdbTmp.setPwd(pwd);
		//copiar clases y properties en esta
		//importer.createSystemViews(fcdbActual.getBusiness());
		/*GenerateSQL gSQL = new GenerateSQL(gestorDB);
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		
		String tableName = "clase";
		String sql = "insert into " + tableName + "(" + cB + "tableId" + cE + ", rdn, id, abstracta)\n" + 
			gSQL.getSelectRemote("dyna" + nbusiness, tableName, cB + "tableId" + cE + ", rdn, id, abstracta", null, 
					"tableid integer, rdn character varying(100), id integer, abs boolean");
		DBQueries.executeUpdate(fcdbTmp, sql);
		alterSequence(fcdbTmp, gSQL, tableName);
		
		tableName = "propiedad_dato";
		sql = "insert into " + tableName + "(" + cB + "tableId" + cE + ", rdn, id, valuecls, cat)\n" + 
			gSQL.getSelectRemote("dyna" + nbusiness, tableName, cB + "tableId" + cE + ", rdn, id, valuecls, cat", null, 
					"tableid integer, rdn character varying(100), id integer, vcls integer, cat integer");
		DBQueries.executeUpdate(fcdbTmp, sql);
		alterSequence(fcdbTmp, gSQL, tableName);
		
		tableName = "propiedad_objeto";
		sql = "insert into " + tableName + "(" + cB + "tableId" + cE + ", rdn, id, id_inversa, cat)\n" + 
			gSQL.getSelectRemote("dyna" + nbusiness, tableName, cB + "tableId" + cE + ", rdn, id, id_inversa, cat", null, 
					"tableid integer, rdn character varying(100), id integer, idinv integer, cat integer");
		DBQueries.executeUpdate(fcdbTmp, sql);
		alterSequence(fcdbTmp, gSQL, tableName);
		
		fcdbTmp.removeConnections();*/
		return fcdbTmp;
	}
	private Integer getLastTableId(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, String tableName) throws SQLException, NamingException {
		String sql = gSQL.getLastTableId(tableName);
		List<List<String>> queryResult = DBQueries.executeQuery(fcdbTmp, sql);
		Integer tableIdValue = Integer.parseInt(queryResult.get(0).get(0));
		return tableIdValue;
	}
	private void alterSequence(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, String tableName) throws SQLException, NamingException {
		Integer lastSeq = getLastTableId(fcdbTmp, gSQL, tableName);
		String nameSeq = tableName + "_tableId_seq";
		String sqlAlter = "ALTER SEQUENCE " + gSQL.getCharacterBegin() + nameSeq + gSQL.getCharacterEnd() + " RESTART WITH " + Integer.valueOf(lastSeq+1);
		DBQueries.executeUpdate(fcdbTmp, sqlAlter);
	}
	
	private boolean compareIds(FactoryConnectionDB fcdbTmp, int nbusiness,String pwd) throws SQLException, NamingException {
		boolean continueImportation = false;
		GenerateSQL gSQL = new GenerateSQL(fcdbTmp.getGestorDB());
		//si instance2 contiene instance1 -> instance1 - instance2 = 0
		//si lo contiene es posible que se hayan añadido clases pero eso no es problema para el importador
		//contenido si la siguiente consulta devuelve 0 registros
		String sql = gSQL.getSelectRemote("dyna" + nbusiness, "instances", "idto, ido, property, value, valuecls, qmin, qmax, name, op, virtual", 
			"virtual=false and " +
			"op<>'ONEOF' and " +
			"idto not in(select id_to from t_herencias " +
				"where id_to_padre in(" + 
				Auxiliar.integerArrayToString(Constants.LIST_SYSTEM_CLASS,",") + "))", 
		"idto numeric(18,0), ido numeric(18,0), property numeric(18,0), value character varying(50), valuecls numeric(18,0), qmin numeric(18,0), qmax numeric(18,0), name character varying(100), op character varying(50), virtual boolean",pwd) + 
			"\n" + gSQL.getSubstract() + "\n" + 
		"select idto, ido, property, value, valuecls, qmin, qmax, name, op, virtual from instances where virtual=false";
	
		List<List<String>> lListInst = DBQueries.executeQuery(fcdbTmp, sql);
		if (lListInst.size()==0)
			continueImportation = true;
		
		if (continueImportation) {
			sql = gSQL.getSelectRemote("dyna" + nbusiness, "v_propiedad", 
			gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, cat, id_inversa", null, 
			"tableid integer, rdn character varying(100), cat integer, idinversa integer",pwd) + 
				"\n" + gSQL.getSubstract() + "\n" + 
			"select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, cat, id_inversa from v_propiedad";
	
			List<List<String>> lListProp = DBQueries.executeQuery(fcdbTmp, sql);
			if (lListProp.size()==0)
				continueImportation = true;
			else
				continueImportation = false;
		}
		
		if (continueImportation) {
			sql = gSQL.getSelectRemote("dyna" + nbusiness, "clase", 
			gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, abstracta", null, 
			"tableid integer, rdn character varying(100), abstracta boolean",pwd) + 
				"\n" + gSQL.getSubstract() + "\n" + 
			"select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn, abstracta from clase";
	
			List<List<String>> lListClase = DBQueries.executeQuery(fcdbTmp, sql);
			if (lListClase.size()==0)
				continueImportation = true;
			else
				continueImportation = false;
		}
		return continueImportation;
	}
	
	public boolean processMigration(String BusinessFunctions,boolean import_help_views,OWLModel owlModel, boolean importReports, boolean importQuerys, String pathOWL, String pathIdsTxt,String pathXML, boolean replica, 
			String nameUpdateModel, String nameUpdateReplica, FactoryConnectionDB fcdb,DataBaseMap dataBaseMap,String nameXmlFile, String pathReport, 
			String pathQuery, String pathImportOtherXml, String pathImportReports, boolean importXmlConfiguration, 
			boolean insertUserAndRoles, boolean insertIndex, String pathLogFile, String pathDeletedLogFile,String pwd) throws Exception {
		boolean menuContinue = true;
		int nbusiness = fcdb.getBusiness();
		String gestorDB = fcdb.getGestorDB();
		
		IImporter importer = ImporterFactory.createImporter(GestorsDBConstants.postgreSQL,pwd);
		importer.setHost(fcdb.getDatabaseIP());
		if (! importer.dropConnections(nbusiness)){
			System.out.println("---> No es posible cerrar conexiones");
			System.out.println("---> Fin de la importación");
			System.exit(0);
		}
		//mismos parametros que fcdb
		FactoryConnectionDB fcdbTmp = create_Temporal_DBWithSystemTablesAndIDs(importer, fcdb.getDatabaseIP(), fcdb.getPort(), nbusiness, gestorDB,pwd);
		int nbusinessTmp = fcdbTmp.getBusiness();
		
		OWLParser owlParserTmp = new OWLParser(owlModel, fcdbTmp, new OWLIds(pathIdsTxt));
		owlParserTmp.buildSystem();
		
		boolean continueImportation =compareIds(fcdbTmp, nbusiness,pwd);
		if (continueImportation) {
			System.out.println("Es posible que se hayan añadido clases al modelo, pero no se han borrado ni modificado");
			//borrar db temporal
			fcdbTmp.removeConnections();
			importer.dropDataBase(nbusinessTmp);
			//importar sin borrar individuos
			System.out.println("Antes de volver a importar manteniendo individuos");
			Menu.setConnectionDAO(fcdb);
			Menu menu = new Menu();
			boolean deleteIndividuals = false;
			OWLParser owlParser = new OWLParser(owlModel, fcdb, new OWLIds(pathIdsTxt));
			menuContinue = menu.importGeneric(BusinessFunctions,import_help_views,owlParser, owlModel, importReports, importQuerys, pathXML, replica, 
					nameUpdateModel, nameUpdateReplica, fcdb, nameXmlFile, pathReport, pathQuery, 
					pathImportOtherXml, pathImportReports, deleteIndividuals, importXmlConfiguration, 
					insertUserAndRoles, insertIndex, nbusiness,pwd);
		} else {
			//si no, tenemos que usar el migrador
			System.out.println("Se han borrado o modificado clases del modelo, " +
					"hay que usar el migrador.");
			
			GenerateSQL gSQL = new GenerateSQL(gestorDB);
			if(dataBaseMap==null){
				dataBaseMap= new DataBaseMap(fcdb, false);
			}
			owlParserTmp.buildDataBase(BusinessFunctions,import_help_views,true);
			DataBaseMap dataBaseMapTmp = new DataBaseMap(fcdbTmp, false);
			
			//si hay clases nuevas y clases borradas -> posible transformacion
			//clases nuevas -> clase2 - clase1
			//clases borradas -> clase1 - instance2
			
			HashSet<String> setRdnsNewC = new HashSet<String>();
			HashSet<String> setRdnsDelC = new HashSet<String>();
			transformByTable(fcdbTmp, gSQL, nbusiness, setRdnsNewC, setRdnsDelC, "clase", "idto",pwd);
			HashSet<String> setRdnsDelCData = onlyDataOfClases(fcdb, gSQL, dataBaseMap, setRdnsDelC);
			
			//compruebo propiedades de la misma forma que con clases
			HashSet<String> setRdnsNewPD = new HashSet<String>();
			HashSet<String> setRdnsDelPD = new HashSet<String>();
			transformByTable(fcdbTmp, gSQL, nbusiness, setRdnsNewPD, setRdnsDelPD, "propiedad_dato", "property",pwd);
			//TODO
			HashSet<String> setRdnsDelPDData = new HashSet<String>();
//			HashSet<String> setRdnsDelPDData = onlyDataOfProps(fcdb, setIdsDelPD, mapDelP);
			
			HashSet<String> setRdnsNewPO = new HashSet<String>();
			HashSet<String> setRdnsDelPO = new HashSet<String>();
			transformByTable(fcdbTmp, gSQL, nbusiness, setRdnsNewPO, setRdnsDelPO, "propiedad_objeto", "property",pwd);
			//TODO
			HashSet<String> setRdnsDelPOData = new HashSet<String>();
//			HashSet<String> setRdnsDelPOData = onlyDataOfProps(fcdb, setIdsDelPO, mapDelP);
			
			//setIdsDelP.addAll(setIdsDelPD);
			//setIdsDelP.addAll(setIdsDelPO);
			
			//propiedades eliminadas para una clase en concreto
			//mostrar registros que estan en instances origin que no estan en instances y no sean de una clase o propiedad borrada
			HashMap<String,HashSet<String>> setIdtosPropsDelP = new HashMap<String,HashSet<String>>();
			//HashSet<String> setIdsDelCP = new HashSet<String>();
			//transformProps(fcdb, gSQL, nbusinessTmp, dataBaseMapTmp, setIdtosPropsDelP, setIdsDelC, setIdsDelP, setIdsDelCP, mapDelC, mapDelP);
			//TODO
			HashMap<String,HashSet<String>> setIdtosPropsDelPData = new HashMap<String, HashSet<String>>();
//			HashMap<String,HashSet<String>> setIdtosPropsDelPData = onlyDataOfClasesProps(fcdb, dataBaseMap, setIdtosPropsDelP, setIdsDelCP, mapDelC, mapDelP);
			
			HashSet<String> setRdnsOPToDP = new HashSet<String>();
			HashSet<String> setRdnsDPToOP = new HashSet<String>();
			transformPropsDPOPByTable(fcdbTmp, gSQL, nbusiness, setRdnsOPToDP, setRdnsDPToOP,pwd);
			//TODO
			HashSet<String> setRdnsOPToDPData = new HashSet<String>();
			HashSet<String> setRdnsDPToOPData = new HashSet<String>();
//			HashSet<String> setRdnsOPToDPData = onlyDataOfProps(fcdb, setIdsOPToDP, mapDelP);
//			HashSet<String> setRdnsDPToOPData = onlyDataOfProps(fcdb, setIdsDPToOP, mapDelP);

			HashSet<String> setRdnsNoAbsToAbs = new HashSet<String>();
			transformNoAbstractToAbstract(fcdbTmp, gSQL, nbusiness, setRdnsNoAbsToAbs,pwd);
			HashSet<String> setRdnsNoAbsToAbsData = onlyDataOfClases(fcdb, gSQL, dataBaseMap, setRdnsNoAbsToAbs);
			
			File xsltFile = null;
			if (setRdnsNewC.size()>0 || setRdnsDelC.size()>0 ||
					setRdnsNewPD.size()>0 || setRdnsDelPD.size()>0 ||
					setRdnsNewPO.size()>0 || setRdnsDelPO.size()>0 ||
					setRdnsOPToDP.size()>0 || setRdnsDPToOP.size()>0 ||
					setRdnsNoAbsToAbs.size()>0 || setIdtosPropsDelP.size()>0) {
				//pedir al usuario que cree X ficheros necesarios para las clases conflictivas y diga la carpeta donde se encuentran
				//cada uno que tenga como nombre de fichero el nombre de la clase
				String str = "\nEs posible que tenga que crear un fichero de transformación.\n";
				if (setRdnsNewC.size()>0 || setRdnsDelC.size()>0) {
					str += "Clases creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewC, ",") + "\n";
					str += "Clases borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelC, ",") + "\n";
					if (setRdnsDelCData.size()>0)
						str += "De las clases borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelCData, ",") + "\n";
					else
						str += "De las clases borradas NO se perderán datos" + "\n";
					str += "\n";
				}
				if (setRdnsNoAbsToAbs.size()>0) {
					str += "Clases instanciables que han pasado a ser abstractas: " + Auxiliar.hashSetToStringComillas(setRdnsNoAbsToAbs, ",") + "\n";
					if (setRdnsNoAbsToAbsData.size()>0)
						str += "De las clases instanciables que han pasado a ser abstractas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsNoAbsToAbsData, ",") + "\n";
					else
						str += "De las clases instanciables que han pasado a ser abstractas NO se perderán datos" + "\n";
					str += "\n";
				}
				if (setRdnsNewPD.size()>0 || setRdnsDelPD.size()>0) {
					str += "Propiedades dato creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewPD, ",") + "\n";
					str += "Propiedades dato borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelPD, ",") + "\n";
					if (setRdnsDelPDData.size()>0)
						str += "De las propiedades dato borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelPDData, ",") + "\n";
//					else
//						str += "De las propiedades dato borradas NO se perderán datos" + "\n";
					str += "\n";
				}
				if (setRdnsNewPO.size()>0 || setRdnsDelPO.size()>0) {
					str += "Propiedades objeto creadas: " + Auxiliar.hashSetToStringComillas(setRdnsNewPO, ",") + "\n";
					str += "Propiedades objeto borradas: " + Auxiliar.hashSetToStringComillas(setRdnsDelPO, ",") + "\n";
					if (setRdnsDelPOData.size()>0)
						str += "De las propiedades objeto borradas se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDelPOData, ",") + "\n";
//					else
//						str += "De las propiedades objeto borradas NO se perderán datos" + "\n";
					str += "\n";
				}
				if (setRdnsOPToDP.size()>0 || setRdnsDPToOP.size()>0) {
					str += "Propiedades objeto que han pasado a ser propiedades dato: " + Auxiliar.hashSetToStringComillas(setRdnsOPToDP, ",") + "\n";
					if (setRdnsOPToDPData.size()>0)
						str += "De las propiedades objeto que han pasado a ser propiedades dato se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsOPToDPData, ",") + "\n";
//					else
//						str += "De las propiedades objeto que han pasado a ser propiedades dato NO se perderán datos" + "\n";
					str += "Propiedades dato que han pasado a ser propiedades objeto: " + Auxiliar.hashSetToStringComillas(setRdnsDPToOP, ",") + "\n";
					if (setRdnsDPToOPData.size()>0)
						str += "De las propiedades dato que han pasado a ser propiedades objeto se perderán datos en: " + Auxiliar.hashSetToStringComillas(setRdnsDPToOPData, ",") + "\n";
//					else
//						str += "De las propiedades dato que han pasado a ser propiedades objeto NO se perderán datos" + "\n";
					str += "\n";
				}
				if (setIdtosPropsDelP.size()>0) {
					str += "Clases con propiedades borradas: " + Auxiliar.hashMapSetStringToStringComillas(setIdtosPropsDelP, " con las propiedades ", ",") + "\n";
					if (setIdtosPropsDelPData.size()>0)
						str += "De las clases con propiedades borradas se perderán datos en: " + Auxiliar.hashMapSetStringToStringComillas(setIdtosPropsDelPData, " con las propiedades ", ",") + "\n";
//					else
//						str += "De las clases con propiedades borradas NO se perderán datos" + "\n";
					str += "\n";
				}
				str += "¿Hay modificaciones en el modelo que afectan a los datos (cambios de nombre de clase o propiedad) " +
						"que no sean sólo creaciones y borrados? S/N";
				
				boolean correcto = false;
				while (!correcto) {
					String resDel = Auxiliar.leeTexto(str);
					if (resDel.equalsIgnoreCase("S") || resDel.equalsIgnoreCase("SI")) {
						str = "Detecte las modificaciones, y una vez creado el fichero de transformación introduzca la ruta:";
						String pathXSLT = Auxiliar.leeTexto(str);
						xsltFile = new File(pathXSLT);
						if (!xsltFile.exists())
							str = "El fichero no existe. ¿Está seguro de que hay modificaciones en el modelo que afectan a los datos? S/N";
						else
							correcto = true;
					} else {
						xsltFile = null;
						correcto = true;
					}
				}
			}
			HashSet<String> propsStructToNoStruct = getPropsStructToNoStruct(fcdbTmp, gSQL, nbusiness,pwd);
			FileWriter f = new FileWriter(pathLogFile);
			FileWriter deletedLogFile=new FileWriter(pathDeletedLogFile);
			
			//obtener clases no apuntadas
			InstanceService m_IS = Menu.setConnection(fcdb, dataBaseMap);
			Set<ClassInfo> sClassInfo = dataBaseMap.getAllClasses();
			HashSet<String> rdnsCPreProcesados = new HashSet<String>();
			HashSet<String> rdnsCProcesados = new HashSet<String>();
			HashSet<Integer> idosProcesadosEnIdtosNoProcesados = new HashSet<Integer>();
			boolean transformation = false;
			//x cada clase
			HashSet<String> systemTablesNames = Menu.getSystemTablesNames();
			for (ClassInfo classInfo : sClassInfo) {
				int idto = classInfo.getIdto();
				String rdn = classInfo.getName();
				if (!classInfo.isAbstractClass() && !setRdnsNewC.contains(rdn)) {
					Set<ClassInfo> sClassInfoRef = dataBaseMap.getClassesReferencingClass(idto);
					
					boolean continuar = false;
					if (sClassInfoRef==null) {
						continuar = true;
					} else {
						//solo se apunta por el mismo
						boolean someDistinct = false;
						for (ClassInfo classInfoRef : sClassInfoRef) {
							Integer idtoClassInfoRef = classInfoRef.getIdto();
							if (!idtoClassInfoRef.equals(idto)) {
								someDistinct = true;
								break;
							}
						}
						if (!someDistinct)
							continuar = true;
					}
					if (continuar) {
						//obtener clases hijas e iterar x ellas
						//llamar a metodo recursivo que se encarga de:
						//hacer get de un nivel sobre la otra bd
						//llamar a migrator sobre la nueva bd
						transformation = dataMigration(f, classInfo, rdnsCPreProcesados, rdnsCProcesados, 
								idosProcesadosEnIdtosNoProcesados, xsltFile, 
								dataBaseMap, dataBaseMapTmp, fcdb, fcdbTmp, m_IS, 
								systemTablesNames, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, 
								propsStructToNoStruct, true, deletedLogFile);
					}
				}
			}
			f.close();
			deletedLogFile.close();
			fcdb.removeConnections();
			
			owlParserTmp.insertModelIndividuals();
			owlParserTmp.insertHelp();
			
			//importo solo configuracion
			if (importXmlConfiguration) {
				System.out.println("Antes de importar configuración");
				Menu menu = new Menu();
				menuContinue = menu.importXML(importReports, importQuerys, pathXML, replica, fcdbTmp, 
						nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, 
						pathImportOtherXml, pathImportReports);
			}
			if (menuContinue && transformation && !importReports) {
				boolean deleteReports = Menu.wantDeleteReports();
				if (deleteReports)
					Menu.deleteReports(fcdbTmp);
			}
			
			//Mantenemos la licencia original
			Menu.copyLicense(fcdb, fcdbTmp);
			
			//paso la dyna temporal a la dyna sobre la que se quiere importar
			fcdbTmp.removeConnections();
			importer.dropDataBase(nbusiness);
//CAMBIO	
			importer.renameDataBase(nbusinessTmp, nbusiness);
			//int nbusinessTmp2 = nbusiness*30000;
			//importer.dropDataBase(nbusinessTmp2);
			//importer.copyDataBase(nbusinessTmp, nbusinessTmp2);
			//borro la dyna temporal
//CAMBIO	importer.dropDataBase(nbusinessTmp);
			//fcdb = Menu.setConnection(String.valueOf(nbusiness), ip, fcdb.getPort(), fcdb.getGestorDB());
		}
		return menuContinue;
	}
	
	private HashSet<String> onlyDataOfClases(FactoryConnectionDB fcdb, GenerateSQL gSQL, DataBaseMap dataBaseMap, 
			HashSet<String> setRdnsDelC) throws SQLException, NamingException {
		HashSet<String> setRdnsDelCData = new HashSet<String>();
		for (String rdn : setRdnsDelC) {
			ClassInfo classInfo=dataBaseMap.getClass(rdn);
			if(classInfo!=null){
				Integer idto = classInfo.getIdto();
				View view = dataBaseMap.getView(idto);
				if (view==null) {
					Table table = dataBaseMap.getTable(idto);
					System.out.println("idto " + idto);
					if (table!=null) {
						String sql = "select * from " + gSQL.getCharacterBegin() + table.getName() + gSQL.getCharacterEnd() + " limit 1";
						List<List<String>> lList = DBQueries.executeQuery(fcdb, sql);
						Iterator it = lList.iterator();
						if (it.hasNext()) {
							setRdnsDelCData.add(rdn);
						}
					}
				}
			}
		}
		return setRdnsDelCData;
	}
//	private HashMap<String,HashSet<String>> onlyDataOfClasesProps(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, 
//			HashMap<String,HashSet<String>> setIdtosPropsDelP, 
//			HashSet<String> setIdsDelCP, HashMap<Integer, String> mapDelC, HashMap<Integer, String> mapDelP) throws SQLException, NamingException {
//		HashMap<String,HashSet<String>> setIdtosPropsDelPData = new HashMap<String, HashSet<String>>();
//		String gestorOrigin = fcdb.getGestorDB();
//		GenerateSQL gSQL = new GenerateSQL(gestorOrigin);
//		for (String className : setIdtosPropsDelP.keySet()) {
//			ClassInfo classInfo = dataBaseMap.getClass(className);
//			int idto = classInfo.getIdto();
//			View view = dataBaseMap.getView(idto);
//			if (view==null) {
//				Table table = dataBaseMap.getTable(idto);
//				//necesito sacar los nombres de las columnas
//				HashMap<String,String> columnProp = new HashMap<String, String>();
//				HashSet<String> setPropsDelP = setIdtosPropsDelP.get(className);
//				HashSet<String> columnNames = new HashSet<String>();
//				for (String propName : setPropsDelP) {
//					int prop = dataBaseMap.getPropertyId(propName);
//					boolean isOP = table.isObjectProperty(prop);
//					if (isOP) {
//						boolean isExternalizedProperty = table.isExternalizedProperty(prop);
//						if (!isExternalizedProperty) {
//							IQueryInfoColumn[] columns = table.getObjectPropertyColumn(prop, idto);
//							String columnNameNoCharacter = columns[0].getColumnName();
//							String columnName = gSQL.getCharacterBegin() + columnNameNoCharacter + gSQL.getCharacterEnd();
//							columnNames.add(columnName);
//							columnProp.put(columnName, propName);
//						} else {
//							Set<Integer> idsTableParent = table.getExternalizedPropertyLocations(prop);
//							Integer propertyLocation = null;
//							Iterator<Integer> it = idsTableParent.iterator();
//							while (it.hasNext() && propertyLocation == null) {
//								Integer propertyLocationTmp = (Integer)it.next();
//								IQueryInfo iqi = dataBaseMap.getTable(propertyLocationTmp);
//								if (iqi.isAssociation()){
//									// Si es asociación, tenemos que comprobar que haya una
//									// columna que contenga información de la propiedad que esté
//									// apuntando al rango deseado.
//									IQueryInfoColumn [] columns = iqi.getObjectPropertyColumn(prop, idto);
//									if (columns != null){
//										propertyLocation = propertyLocationTmp;
//									}
//								} else {
//									// Si no es una asociación, significa que la propiedad es
//									// estructural y su información está externalizada a la
//									// clase del rango de la propiedad. Nos interesa que el idto
//									// sea el del rango deseado.
//									if (propertyLocationTmp.equals(idto))
//										propertyLocation = propertyLocationTmp;
//								}
//							}
//							
//							if (propertyLocation==null && !ik.getCategory(prop).isStructural()) {
//								if (!isInverseStructural)
//									propInverse = ik.getPropertyInverse(prop);
//								//si no es estructural, pero su inversa sí, la que estará en base de datos será su inversa
//								if (propInverse!=null && ik.getCategory(propInverse).isStructural()) {
//									if (!isInverseStructural)
//										prop = propInverse;
//									idsTableParent = table.getExternalizedPropertyLocations(prop);
//									it = idsTableParent.iterator();
//									if (it.hasNext())
//										propertyLocation = (Integer)it.next();
//								}
//							}
//							
//							IQueryInfo referencedTable = dataBaseMap.getTable(propertyLocation);
//							if (referencedTable.isAssociation()) {
//								//tengo que obtener los nombres de las columnas de la tabla/vista asociacion
//								//ColumnNames solo tiene un nombre de columna que es el nombre de la columna que apunta al objeto del rango, tienes que fijar que la otra columna que tenga dominio tenga el tableId del objeto del dominio.
//								//List<String> columnNames = referencedTableView.getColumnNamesContainingProperty(prop);
//								//String rangeColumn = gSQL.getCharacterBegin() + columnNames.get(0) + gSQL.getCharacterEnd();
//								IQueryInfoColumn[] columnsDomain = null;
//								columnsDomain = referencedTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, idto);
//								if (columnsDomain==null || columnsDomain[0]==null)
//									throw new DataErrorException("No está definido el enlace entre " + referencedTable.getName() + " y " + table.getName() + "(" + idto + ") a través de IQueryInfo.ID_DOMAIN");
//	
//								String domainColumn = columnsDomain[0].getColumnName();
//								String domainColumnIdto = null;
//								IQueryInfoColumn columnIdto = columnsDomain[1];
//								if (columnIdto!=null)
//									domainColumnIdto = columnIdto.getColumnName();
//								if (domainColumn == null) {
//									System.err.println("No se ha encontrado la columna del dominio en la tabla/vista " + referencedTable);
//								} else {
//									IQueryInfoColumn[] columnsRange = null;
//										columnsRange = referencedTable.getObjectPropertyColumn(prop, idto);
//										if (columnsRange==null || columnsRange[0]==null)
//											throw new DataErrorException("No está definido el enlace entre " + referencedTable.getName() + " y " + table.getName() + " a través de " + ik.getPropertyName(prop) + "(" + prop + ")");
//	
//									String rangeColumn = columnsRange[0].getColumnName();
//									String rangeColumnIdto = null;
//									IQueryInfoColumn columnIdtoR = columnsRange[1];
//									if (columnIdtoR!=null)
//										rangeColumnIdto = columnIdtoR.getColumnName();
//									if (rangeColumn == null) {
//										System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + referencedTable);
//									} else {
//										//preguntamos x la DataProperty idProperty
//										IQueryInfoColumn columnIdProperty = referencedTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
//										String columnIdPropertyName = null;
//										if (columnIdProperty!=null)
//											columnIdPropertyName = columnIdProperty.getColumnName();
//										
//										//hacer la query en la tabla asociacion
//										
//										
//										
//									}
//								}
//							} else {
//								IQueryInfoColumn[] columns = null;
//								columns = table.getObjectPropertyColumn(prop, idto);
//								if (columns==null || columns[0]==null)
//									throw new DataErrorException("No está definido el enlace para " + table.getName() + "(" + idto + ") a través de " + propName + "(" + prop + ")");
//								String columnName = columns[0].getColumnName();
//								if (columnName == null) {
//									System.err.println("No se ha encontrado la columna del rango en la tabla/vista " + table);
//								} else {
//									// columnName tiene el nombre de la columna que busco y esa columna tiene el tableId del objeto en la otra tabla/vista.
//									
//									//hacer la query en la tabla destino
//									
//									
//									
//								}
//							}
//						}
//					} else {
//						List<String> columnsName = table.getColumnNamesContainingProperty(prop);
//						String columnNameNoCharacter = columnsName.get(0);
//						String columnName = gSQL.getCharacterBegin() + columnNameNoCharacter + gSQL.getCharacterEnd();
//						columnNames.add(columnName);
//						columnProp.put(columnName, propName);
//					}
//				}
//				String sql = "select " + Auxiliar.hashSetStringToString(columnNames, ",") + " from " + gSQL.getCharacterBegin() + table.getName() + gSQL.getCharacterEnd();
//				List<List<String>> lList = DBQueries.executeQuery(fcdb, sql);
//				Iterator it = lList.iterator();
//				if (it.hasNext()) {
//					setRdnsDelCData.add(mapDelC.get(idto));
//				}
//			}
//		}
//		return setIdtosPropsDelPData;
//	}
		
	private HashSet<String> getPropsStructToNoStruct(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, int nbusiness,String pwd) throws SQLException, NamingException {
		HashSet<String> propsStructToNoStruct = new HashSet<String>();
		String sql = "select id, rdn from (" + "\n" + 
			"select id, rdn,'1' from propiedad_objeto where cat%" + Category.iStructural + "<>0 " + "\n" + 
			"union " + "\n" + 
			gSQL.getSelectRemote("dyna" + nbusiness, "propiedad_objeto", "id, rdn, ''2''", "cat%" + Category.iStructural + "=0", 
			"id integer, rdn character varying(100), ident integer",pwd) + 
			"order by id" + "\n" + 
			") as h " + "\n" + 
			"group by id,rdn " + "\n" + 
			"having count(*)>1";
		List<List<String>> lList = DBQueries.executeQuery(fcdbTmp, sql);
		Iterator it = lList.iterator();
		while (it.hasNext()) {
			List<String> list = (List<String>)it.next();
			propsStructToNoStruct.add(list.get(1));
		}
		return propsStructToNoStruct;
	}
	private boolean transformByTable(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, int nbusiness, 
			Set<String> setRdnsNew, Set<String> setRdnsDel, String table, String column,String pwd) throws SQLException, NamingException {
		boolean transform = false;

		String sql = "select id,rdn from "+table+" where rdn in (" +
				"select rdn from " + table + 
				"\n" + gSQL.getSubstract() + "\n" + 
			gSQL.getSelectRemote("dyna" + nbusiness, table, "rdn", null, 
			"rdn character varying(100))",pwd); 
		List<List<String>> lListNew = DBQueries.executeQuery(fcdbTmp, sql);
		
//		if (lListNew.size()>0) {
			Iterator it = lListNew.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				setRdnsNew.add(list.get(1));
			}
			
			/*sql = gSQL.getSelectRemote("dyna" + nbusiness, table, "id, rdn", "id in(select distinct " + column + " from instances)", 
				"id integer, rdn character varying(100)") + 
					"\n" + gSQL.getSubstract() + "\n" + 
				"select id, rdn from " + table + " where id in(select distinct " + column + " from instances)";*/
			
			String condition="rdn in(select rdn from " + table+
			"\n" + gSQL.getSubstract() + "\n" + 
			gSQL.getSelectRemote("dyna" + fcdbTmp.getBusiness(), table, "rdn", null, 
			"rdn character varying(100)",pwd)+")";
			
			sql = gSQL.getSelectRemote("dyna" + nbusiness, table, "id, rdn", condition, 
			"id integer, rdn character varying(100)",pwd);
			

			
			List<List<String>> lListDel = DBQueries.executeQuery(fcdbTmp, sql);
			if (lListDel.size()>0) {
				if (lListNew.size()>0)
					transform = true;
				//al haber creaciones y borrados es posible que sean modificaciones
				//guardamos rdns  en listas
				it = lListDel.iterator();
				while (it.hasNext()) {
					List<String> list = (List<String>)it.next();
					setRdnsDel.add(list.get(1));
				}
			}
//		}
		return transform;
	}
	
	private boolean transformPropsDPOPByTable(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, int nbusiness, 
			Set<String> setRdnsOPToDP, Set<String> setRdnsDPToOP,String pwd) throws SQLException, NamingException {
		boolean transform = false;
		//dataproperties que ahora son objectproperties
		String sql = "select id, rdn from propiedad_objeto " + 
				"\nwhere rdn in(" + 
			gSQL.getSelectRemote("dyna" + nbusiness, "propiedad_dato", "rdn", null, 
			"rdn character varying(100)",pwd) + ")";
		List<List<String>> lListDPToOP = DBQueries.executeQuery(fcdbTmp, sql);
		
		if (lListDPToOP.size()>0) {
			transform = true;
			Iterator it = lListDPToOP.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				int id = Integer.parseInt(list.get(0));
				String rdn = list.get(1);
				setRdnsDPToOP.add(rdn);
			}
		}
		
		//objectproperties que ahora son dataproperties
		sql = "select id, rdn from propiedad_dato " + 
				"\nwhere rdn in(" + 
			gSQL.getSelectRemote("dyna" + nbusiness, "propiedad_objeto", "rdn", null, 
			"rdn character varying(100)",pwd) + ")";
		List<List<String>> lListOPToDP = DBQueries.executeQuery(fcdbTmp, sql);
		if (lListOPToDP.size()>0) {
			transform = true;
			//al haber creaciones y borrados es posible que sean modificaciones
			//guardamos rdns  en listas
			Iterator it = lListOPToDP.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				int id = Integer.parseInt(list.get(0));
				String rdn = list.get(1);
				setRdnsOPToDP.add(rdn);
			}
		}
		return transform;
	}
	
	private boolean transformProps(FactoryConnectionDB fcdb, GenerateSQL gSQL, int nbusinessTmp, DataBaseMap dataBaseMapTmp, 
			HashMap<String,HashSet<String>> setIdtosPropsDelP, HashSet<Integer> setIdsDelC, HashSet<Integer> setIdsDelP, 
			HashSet<String> setIdsDelCP, 
			HashMap<Integer,String> mapDelC, HashMap<Integer,String> mapDelP,String pwd) throws SQLException, NamingException {
		boolean transform = false;
		//mostrar registros que estan en instances origin que no estan en instances y no sean de una clase o propiedad borrada
		String sql = "select idto, property, cast(idto as varchar) || ';' || cast(property as varchar) " +
				"from instances " +
			"\nwhere idto not in(select id_to from t_herencias where id_to_padre  ="+Constants.IDTO_ACTION+") and cast(idto as varchar) || ';' || cast(property as varchar) not in(" +
			gSQL.getSelectRemote("dyna" + nbusinessTmp, "instances", "cast(idto as varchar) || '';'' || cast(property as varchar)", 
					null, "idtoprop character varying(100)",pwd) + ")";
		if (setIdsDelC.size()>0)
			sql += " and idto not in(" + Auxiliar.hashSetIntegerToString(setIdsDelC, ",") + ")";
		if (setIdsDelP.size()>0)
			sql += " and property not in(" + Auxiliar.hashSetIntegerToString(setIdsDelP, ",") + ")";
		List<List<String>> lListProps = DBQueries.executeQuery(fcdb, sql);
		
		if (lListProps.size()>0) {
			transform = true;
			Iterator it = lListProps.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				System.out.println(list.get(0) + ";" + list.get(1) + ";" + list.get(2));
				Integer idto = Integer.parseInt(list.get(0));
				Integer prop = Integer.parseInt(list.get(1));
				String nameClsProp = "'" + list.get(2) + "'";
				
				ClassInfo classInfo = dataBaseMapTmp.getClass(idto);
				String nameCls = classInfo.getName();
				String nameProp = dataBaseMapTmp.getPropertyName(prop);
				
				HashSet<String> props = setIdtosPropsDelP.get(nameCls);
				if (props==null) {
					props = new HashSet<String>();
					setIdtosPropsDelP.put(nameCls, props);
				}
				props.add(nameProp);
				
				mapDelC.put(idto, nameCls);
				mapDelP.put(prop, nameProp);
				setIdsDelCP.add(nameClsProp);
			}
		}
		return transform;
	}
	
	private boolean transformNoAbstractToAbstract(FactoryConnectionDB fcdbTmp, GenerateSQL gSQL, int nbusiness,
			Set<String> setRdnsNoAbsToAbs,String pwd) throws SQLException, NamingException {
		boolean transform = false;
		//instanciables que ahora son abstractas
		String sql = "select id, rdn from clase where abstracta=true and rdn in( " + 
			gSQL.getSelectRemote("dyna" + nbusiness, "clase", "rdn", "abstracta=false", 
			"rdn character varying(100)",pwd) + ")";
		List<List<String>> lListAbstract = DBQueries.executeQuery(fcdbTmp, sql);
		
		if (lListAbstract.size()>0) {
			transform = true;
			Iterator it = lListAbstract.iterator();
			while (it.hasNext()) {
				List<String> list = (List<String>)it.next();
				int idto = Integer.parseInt(list.get(0));
				String rdn = list.get(1);
				setRdnsNoAbsToAbs.add(rdn);
			}
		}
		return transform;
	}
	
	private boolean hasDatasOfIdto(FactoryConnectionDB fcdb, String tableName, int idto) throws SQLException, NamingException {
		String gestor = fcdb.getGestorDB();
		GenerateSQL gSQL = new GenerateSQL(gestor);
		String sql = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			sql = "select * from o_datos_atrib where id_to=" + idto + " limit 1";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			sql = "select * from " + gSQL.getCharacterBegin() + tableName + gSQL.getCharacterEnd() + " limit 1";
		}
		List<List<String>> lList = DBQueries.executeQuery(fcdb, sql);
		return lList.size()>0;
	}
	public static boolean dataMigration(FileWriter f, ClassInfo classInfo, HashSet<String> rdnsCPreProcesados, HashSet<String> rdnsCProcesados, 
			HashSet<Integer> idosProcesadosEnIdtosNoProcesados, File xsltFile, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, FactoryConnectionDB fcdb, 
			FactoryConnectionDB fcdbTmp, InstanceService m_IS, HashSet<String> systemTablesNames, HashSet<String> setRdnsNewC, HashSet<String> setRdnsDelC, HashSet<String> setRdnsNoAbsToAbs, 
			HashSet<String> propsStructToNoStruct, boolean first, FileWriter deletedLogFile) throws SQLException, NamingException, TransformerConfigurationException, FileNotFoundException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, FileException, JDOMException, IOException, NoSuchColumnException {
		
		boolean transformationReturn = false;
		int idto = classInfo.getIdto();
		String className = classInfo.getName();
		String tableName = dataBaseMap.getTable(idto).getName();
		System.out.println("classInfo->idto " + idto + ", name " + className + 
				", tableName " + tableName);
		rdnsCPreProcesados.add(className);
//		boolean hasData = true;
//		if (first) {
//			//comprueba si hay datos
//			hasData = hasDatasOfIdto(fcdb, tableName, idto);
//		}
//		if (!hasData) {
//			System.out.println("No hay datos de " + className);
//		} else {
			//obtener clases hijas e iterar x ellas
			for (Integer idto2 : classInfo.getReferencedClasses()) {
				ClassInfo classInfo2 = dataBaseMap.getClass(idto2);
//				boolean iterate = notInverseToIterate(classInfo, idto2, dataBaseMap);
//				if (iterate) {
					Set<Integer> idtosSpec = classInfo2.getChildClasses();
					idtosSpec.add(idto2);
					for (Integer idtoSpec : idtosSpec) {
						ClassInfo classInfoSpec = dataBaseMap.getClass(idtoSpec);
						String rdnSpec=classInfoSpec.getName();
						if (!rdnsCPreProcesados.contains(rdnSpec)) {
							if (!classInfoSpec.isAbstractClass() && !setRdnsNewC.contains(classInfoSpec.getName())) {
								transformationReturn = dataMigration(f, classInfoSpec, rdnsCPreProcesados, rdnsCProcesados, idosProcesadosEnIdtosNoProcesados, xsltFile, dataBaseMap, dataBaseMapTmp, fcdb, fcdbTmp, m_IS, systemTablesNames, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, propsStructToNoStruct, false, deletedLogFile) || transformationReturn;
							}
						}
					}
//				}
			}
			
			//migrar solo si no es del sistema
			if (!systemTablesNames.contains(tableName)) {
				//hacer get de un nivel sobre la otra bd
				int levels = 2;
				QueryService2 queryService2 = new QueryService2(idto, false, dataBaseMap, fcdb, levels, false, false, Constants.USER_SYSTEM, false, m_IS,null);
				queryService2.setMigration(true);
				Document data = queryService2.getData();
				boolean continuar = false;
				Element obj = data.getRootElement().getChild(XMLConstants.TAG_OBJECTS);
				if (obj!=null) {
					continuar = obj.getChildren().size()>0;
					obj=null;//Liberamos memoria
				}
				if (continuar) {
					boolean dataTransform = xsltFile!=null;
					transformationReturn = dataTransform || transformationReturn;
					
					String classNameOrigin = classInfo.getName();
					if (dataTransform) {
						data = XMLTransformer.getTransformedDocument(data,xsltFile);
						
						//tengo que migrar solo el nodo root para saber en que se convierte
						Element datosElem = new Element(XMLConstants.TAG_DATOS);
						Element objectsElem = new Element(XMLConstants.TAG_OBJECTS);
						datosElem.addContent(objectsElem);
						Element rootElem = new Element(classNameOrigin);
						objectsElem.addContent(rootElem);
						/*Element datosElemTrans*/datosElem = XMLTransformer.getTransformedDocument(new Document(datosElem), xsltFile).getRootElement();
						Element datosObjectsTrans = /*datosElemTrans*/datosElem.getChild(XMLConstants.TAG_OBJECTS);
						if (datosObjectsTrans!=null) {
							if (datosObjectsTrans.getChildren().size()>0) {
								Element rootElemTrans = (Element)datosObjectsTrans.getChildren().get(0);
								classInfo = dataBaseMapTmp.getClass(rootElemTrans.getName());
							} else
								classInfo = null;
						} else
							classInfo = null;
					}
					Element dataElem = data.getRootElement();
					
					/*Document processData*/data = postProcessXML(f, classInfo, classNameOrigin, systemTablesNames, dataBaseMap, dataBaseMapTmp, rdnsCProcesados, idosProcesadosEnIdtosNoProcesados, setRdnsDelC, setRdnsNoAbsToAbs, dataElem, propsStructToNoStruct, deletedLogFile);
					dataElem = null;//Liberamos memoria
					rdnsCProcesados.add(className);
					
					if (/*processData*/data!=null) {
						InstanceService instanceService = new InstanceService(fcdbTmp, null, false);
						instanceService.setDataBaseMap(dataBaseMapTmp);
	//					ServerEngine serverEngine = new ServerEngine(fcdbDestination);
	//					instanceService.setIk(serverEngine);
						DBQueries.execute(fcdbTmp, "START TRANSACTION");
						instanceService.serverTransitionObject("migration", /*processData*/data, null, null, true, true, null,true);
						DBQueries.execute(fcdbTmp, "END");
					}
				} else
					System.out.println("No hay datos de " + className);
			}
//		}
		return transformationReturn;
	}
	
	private boolean notInverseToIterate(ClassInfo classR, int idto2, DataBaseMap dataBaseMap) {
		Set<Integer> properties = classR.getPropertiesReferencingClass(idto2);
		boolean iterate = true;
		Iterator<Integer> it = properties.iterator();
		while (iterate && it.hasNext()){
			Integer idProperty = it.next();
			Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
			if (inverseProperty != null && dataBaseMap.isStructuralProperty(inverseProperty)){
				// Se relacionan por al menos una propiedad que no tiene inversa.
				iterate = false;
			}
		}
		return iterate;
	}

	private static Document postProcessXML(FileWriter f, ClassInfo classInfo, String classNameOrigin, HashSet<String> systemTablesNames, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, 
			HashSet<String> rdnsCProcesados, HashSet<Integer> idosProcesadosEnIdtosNoProcesados, HashSet<String> setRdnsDelC, HashSet<String> setRdnsNoAbsToAbs, Element data, HashSet<String> propsStructToNoStruct,
			FileWriter deletedLogFile) throws SQLException, NamingException, JDOMException, IOException {
		Document docDataPostProcess = null;

		Element objects = data.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> children = objects.getChildren();
		if (children.size()>0) {
			System.out.println("Element antes de procesar en tools " + jdomParser.returnXML(data));
			f.write("Element antes de procesar en tools " + jdomParser.returnXML(data) + "\n");
			if (classInfo!=null && !setRdnsDelC.contains(classInfo.getName()) && !setRdnsNoAbsToAbs.contains(classInfo.getName())) {
				Element dataPostProcess = jdomParser.cloneNode(data);
				Element objectsPostProcess = jdomParser.cloneNode(objects);
				dataPostProcess.addContent(objectsPostProcess);
				
				Set<Integer> deletedIdNodes=transformTableIdOfClassAndProperties(objects,dataBaseMap,dataBaseMapTmp,deletedLogFile,objects);
				deleteRefNodes(objects,deletedIdNodes,deletedLogFile,dataBaseMap);
				
				int idNode = 1;
				
				String className = classInfo.getName();
				boolean firstNode = true;
				ArrayList<Element> clases = jdomParser.findElementsByName(objects, className, true);
				for (Element clase : clases) {
					String refNode = clase.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
					if (refNode==null) {
						Element clasePostProcess = jdomParser.cloneNode(clase);
						clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_IDNODE, String.valueOf(idNode));
						idNode++;
						clasePostProcess.removeAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
						//eliminar dataProperties que para esta clase no esten en el nuevo modelo
						processDataProperties(f, dataBaseMap, dataBaseMapTmp, className, clase, clasePostProcess);
						
						objectsPostProcess.addContent(clasePostProcess);
						idNode=postProcessXMLRec(className, f, systemTablesNames, dataBaseMap, dataBaseMapTmp, rdnsCProcesados, idosProcesadosEnIdtosNoProcesados, 
								objects, clase, clasePostProcess, propsStructToNoStruct, firstNode, setRdnsNoAbsToAbs, idNode);
					}
				}
				docDataPostProcess = new Document(dataPostProcess);
				System.out.println("Element despues de procesar en tools " + jdomParser.returnXML(dataPostProcess) + "\n\n");
				f.write("Element despues de procesar en tools " + jdomParser.returnXML(dataPostProcess) + "\n\n\n");
			} else {
				System.out.println("ELIMINACION -> No existe la clase " + classNameOrigin);
				f.write("ELIMINACION -> No existe la clase " + classNameOrigin + "\n");
			}
		}
		return docDataPostProcess;
	}
	
	/**
	 * Cambia los tableIds de CLASES, PROPIEDAD_OBJETO y PROPIEDAD_DATO de element, poniendole los que hay en dataBaseMapTmp
	 * @param element
	 * @param dataBaseMapTmp
	 * @param logFile 
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static Set<Integer> transformTableIdOfClassAndProperties(Element element, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, FileWriter logFile, Element root) throws IOException, JDOMException {
		Iterator<Element> itr=new ArrayList(element.getChildren()).iterator();
		ArrayList<Element> systemElementToDelete=new ArrayList<Element>();
		Set<Integer> idNodeDeletedList=new HashSet<Integer>();
		while(itr.hasNext()){
			Element child=itr.next();
			if(child.getAttribute(XMLConstants.ATTRIBUTE_REFNODE)==null){
				if(child.getName().equals(Constants.CLS_CLASS) || child.getName().equals(Constants.CLS_DATA_PROPERTY) || child.getName().equals(Constants.CLS_OBJECT_PROPERTY)){
					int tableId=child.getAttribute(XMLConstants.ATTRIBUTE_TABLEID).getIntValue();
					String rdn=null;
					if(child.getAttribute(Constants.PROP_RDN)!=null){
						rdn=child.getAttribute(Constants.PROP_RDN).getValue();
					}else{
						rdn=InstanceService.getRdn(dataBaseMap.getFactoryConnectionDB(), dataBaseMap, tableId, dataBaseMap.getClass(child.getName()).getIdto());
					}
					
					Integer ido=InstanceService.getIdo(dataBaseMapTmp.getFactoryConnectionDB(), dataBaseMapTmp, dataBaseMapTmp.getClass(child.getName()).getIdto(), rdn, true);
					if(ido==null){
						System.err.println("La "+child.getName()+" "+rdn+" no existe en el nuevo modelo");
						systemElementToDelete.add(child);
					}else{
						tableId=QueryConstants.getTableId(ido);
					
						child.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(tableId));
					}
					
				}else{
					idNodeDeletedList.addAll(transformTableIdOfClassAndProperties(child, dataBaseMap, dataBaseMapTmp, logFile, root));
				}
			}
		}
		
		//Borramos las clases y propiedades que ahora no existen en el nuevo modelo. Borramos tambien la rama de la que cuelgan
		for(Element el:systemElementToDelete){
			idNodeDeletedList.addAll(deleteBranchElement(el, logFile, root, dataBaseMap));
		}
		
		return idNodeDeletedList;
	}

	/**
	 * Borra (detach) un elemento, y la rama de la que cuelga
	 * @param el
	 * @param logFile
	 * @return
	 * @throws DataConversionException
	 * @throws IOException
	 * @throws JDOMException
	 */
	private static Set<Integer> deleteBranchElement(Element el, FileWriter logFile, Element root, DataBaseMap dataBaseMap) throws DataConversionException, IOException, JDOMException {
		Set<Integer> idNodeDeletedList=new HashSet<Integer>();
		Element parent=el.getParent();
		el.detach();
		if(el.getAttribute(XMLConstants.ATTRIBUTE_IDNODE)!=null){
			idNodeDeletedList.add(el.getAttribute(XMLConstants.ATTRIBUTE_IDNODE).getIntValue());
		}
		logFile.write("--Borrado "+jdomParser.returnXML(el)+"\n");
		
		String property=el.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm).getValue();
		int idProperty=dataBaseMap.getPropertyId(property);
		ClassInfo classInfo=dataBaseMap.getClass(parent.getName());
		//Solo seguimos borrando subiendo por el padre en el caso de que el nodo borrado sea de una propiedad obligatoria que no tiene ningun valor mas
		if(classInfo!=null && classInfo.getProperty(idProperty).getMinCardinality()>parent.getChildren(el.getName()).size()){
			while(parent!=null && !parent.isRootElement() && !parent.getName().equals("objects")){
				Element currentParent=parent;
				parent=parent.getParent();
				currentParent.detach();
				for(Element childElement:(ArrayList<Element>)jdomParser.elementsWithAt(currentParent, XMLConstants.ATTRIBUTE_IDNODE, true)){
					String childIdNode=childElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE).getValue();
					//Con root en jdomParser.elementsWithAt no busca en currentParent ya que fue detach, asi que no hay problema de que encontrara nodos eliminados
					ArrayList<Element> refNodeList=jdomParser.elementsWithAt(root, XMLConstants.ATTRIBUTE_REFNODE, childIdNode, true);
					if(!refNodeList.isEmpty()){
						Element refNodeElement=refNodeList.get(0);
						refNodeElement.removeAttribute(XMLConstants.ATTRIBUTE_REFNODE);
						for(Attribute attribute:(List<Attribute>)childElement.getAttributes()){
							if(!attribute.getName().equals(XMLConstants.ATTRIBUTE_PROPERTYm)){
								refNodeElement.setAttribute(attribute.getName(),attribute.getValue());
							}
						}
					}else{
						//idNodeDeletedList.add(childElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE).getIntValue());No lo necesitamos ya 
					}
				}
				if(currentParent.getAttribute(XMLConstants.ATTRIBUTE_IDNODE)!=null){
					idNodeDeletedList.add(currentParent.getAttribute(XMLConstants.ATTRIBUTE_IDNODE).getIntValue());
				}
				if(logFile!=null){
					logFile.write("\t--Borrado padre "+jdomParser.returnXML(currentParent)+"\n");
				}
			}
		}
		return idNodeDeletedList;
	}
	
	/**
	 * Borra los nodos (y la rama de la que cuelga) con ref_node igual a algun elemento de la lista idNodes
	 * @param root
	 * @param idNodes
	 * @param logFile
	 * @throws DataConversionException
	 * @throws IOException
	 * @throws JDOMException
	 */
	public static void deleteRefNodes(Element root,Set<Integer> idNodes,FileWriter logFile,DataBaseMap dataBaseMap) throws DataConversionException, IOException, JDOMException {
		for(Integer idNode:idNodes){
			Iterator<Element> itr=jdomParser.elementsWithAt(root, XMLConstants.ATTRIBUTE_REFNODE, idNode.toString(), true).iterator();
			while(itr.hasNext()){
				Element element=itr.next();
				Set<Integer> idNodesAux=deleteBranchElement(element, logFile, root, dataBaseMap);
				deleteRefNodes(root, idNodesAux, logFile, dataBaseMap);
			}
			
		}
	}

	private static void processDataProperties(FileWriter f, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, 
			String className, Element clase, Element clasePostProcess) throws IOException {
		List<Attribute> listAttr = clase.getAttributes();
		for (Attribute attribute : listAttr) {
			//System.out.println("className " + className + ", attribute " + attribute);
			String propName = attribute.getName();
			boolean remove = processDataProperty(propName, dataBaseMap, dataBaseMapTmp, className);
			if (remove) {
				clasePostProcess.removeAttribute(propName);
				System.out.println("ELIMINACION -> No existe la DP para clase " + className + ", dp " + propName);
				f.write("ELIMINACION -> No existe la DP para clase " + className + ", dp " + propName + "\n");
			}
		}
	}
	private static boolean processDataProperty(String propName, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, 
			String className) {
		boolean remove = false;
		Integer idPropOrig = dataBaseMap.getPropertyId(propName);
		if (idPropOrig!=null) {
			Integer idProp = dataBaseMapTmp.getPropertyId(propName);
			if (idProp!=null) {
				ClassInfo classInfo = dataBaseMapTmp.getClass(className);
				PropertyInfo propInfo = classInfo.getProperty(idProp);
				if (propInfo==null)
					remove = true;
			} else
				remove = true;
		}
		return remove;
	}
	private static boolean existsObjectProperty(FileWriter f, DataBaseMap dataBaseMapTmp, String classNameParent, String className, String propName) 
			throws IOException {
		boolean exists = true;
		ClassInfo classInfoDomain = dataBaseMapTmp.getClass(classNameParent);
		Integer idProp = dataBaseMapTmp.getPropertyId(propName);
		if (idProp!=null) {
			ClassInfo classInfoRange = dataBaseMapTmp.getClass(className);
			if (classInfoRange!=null) {
				//buscar superiores a classInfoRange
				PropertyInfo propInfo = classInfoDomain.getProperty(idProp);
				if (propInfo!=null) {
					Set<Integer> types = propInfo.getPropertyTypes();
					
					Set<Integer> idtoAndSuperiors = classInfoRange.getParentClasses();
					idtoAndSuperiors.add(classInfoRange.getIdto());
					boolean contains = false;
					Iterator<Integer> it = idtoAndSuperiors.iterator();
					while (it.hasNext() && !contains) {
						Integer idtoRange = it.next();
						if (types.contains(idtoRange))
							contains = true;
					}
					if (!contains)
						exists = false;
				} else
					exists = false;
			} else
				exists = false;
		} else
			exists = false;
		if (!exists) {
			System.out.println("ELIMINACION -> No existe la OP para clasePadre " + classNameParent + ", op " + propName + ", clase " + className);
			f.write("ELIMINACION -> No existe la OP para clasePadre " + classNameParent + ", op " + propName + ", clase " + className + "\n");
		}
		return exists;
	}
	
	private static Integer processTableId(FileWriter f, Element clasePostProcess, int idto) throws IOException {
		String tableIdReturn = null;
		String action = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
		if (action.equals(XMLConstants.ACTION_SET)) {
			Integer tableIdFict = Integer.parseInt(clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID));
			if (tableIdFict<0) { //es un ido negativo, al ser un set tengo que poner el tableId
				//System.out.println("DEBUG -> en un set cambia el ido " + tableIdFict + " por " + String.valueOf(-tableIdFict));
				tableIdReturn = String.valueOf(-tableIdFict);
				clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableIdReturn);
			} else
				tableIdReturn = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
		} else if (action.equals(XMLConstants.ACTION_NEW)) {
			Integer tableIdFict = Integer.parseInt(clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID));
			tableIdReturn = String.valueOf(-tableIdFict.intValue());
			clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableIdReturn);
		}
		return Integer.parseInt(tableIdReturn);
	}
	private static int postProcessXMLRec(String classNameRoot, FileWriter f, HashSet<String> systemTablesNames, DataBaseMap dataBaseMap, DataBaseMap dataBaseMapTmp, 
			HashSet<String> rdnsCProcesados, HashSet<Integer> idosProcesadosEnIdtosNoProcesados, 
			Element objects, Element clase, Element clasePostProcess, HashSet<String> propsStructToNoStruct, boolean firstNode, HashSet<String> setRdnsNoAbsToAbs, int idNode) 
			throws DataErrorException, SQLException, NamingException, JDOMException, IOException  {
		//añadir action en nodos
		String className = clase.getName();
		int idto = dataBaseMapTmp.getClass(className).getIdto();
		if (firstNode) {
			//sustituir tableId por ido negativo, necesito que se mantengan los tableIds reales
			//para asi no tener que hacer busquedas en los nodos set
			//-> tengo q modificar dataBaseManager
			clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
			Integer tableId = processTableId(f, clasePostProcess, idto);
			Integer ido=QueryConstants.getIdo(tableId, idto);
			if (idosProcesadosEnIdtosNoProcesados.contains(ido)) {
				clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
				processTableId(f, clasePostProcess, idto);
			}
		} else {
			ArrayList<String> namesAttr = new ArrayList<String>();
			List<Attribute> lAttr = clasePostProcess.getAttributes();
			for (Attribute attribute : lAttr) {
				String propName = attribute.getName();
				if (!propName.equals(XMLConstants.ATTRIBUTE_IDNODE) && 
						!propName.equals(XMLConstants.ATTRIBUTE_PROPERTYm) && 
						!propName.equals(XMLConstants.ATTRIBUTE_TABLEID) && 
						!propName.equals(Constants.PROP_RDN))
					namesAttr.add(propName);
			}
			for (String name : namesAttr) {
				clasePostProcess.removeAttribute(name);
			}
			String tableName = dataBaseMapTmp.getTable(idto).getName();
			clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
			Integer tableId = processTableId(f, clasePostProcess, idto);
			Integer ido=QueryConstants.getIdo(tableId, idto);
			if (systemTablesNames.contains(tableName) || rdnsCProcesados.contains(className) || 
					idosProcesadosEnIdtosNoProcesados.contains(ido)) {
				clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
				processTableId(f, clasePostProcess, idto);
			} else {
				String property = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
				if (propsStructToNoStruct.contains(property)) {
					clasePostProcess.setAttribute(Constants.PROP_RDN, Constants.DEFAULT_RDN_CHAR + tableId + Constants.DEFAULT_RDN_CHAR);
				}
				//almacenar tableId porque puede que en 2 xml se apunte a este objeto
				//no estaría como idto procesado, pero sí se ha tratado por lo que para el siguiente no sería new
				idosProcesadosEnIdtosNoProcesados.add(ido);
			}
		}
		
		if (firstNode) { //solo quiero 1 nivel
			//iterar por los hijos
			Iterator it = clase.getChildren().iterator();
			while (it.hasNext()) {
				Element claseChild = (Element)it.next();
				String claseChildName = claseChild.getName();
				if (claseChildName.equals(XMLConstants.TAG_MEMO) || 
						claseChildName.equals(XMLConstants.TAG_DATA_PROPERTY)) {
					String propName = claseChild.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
					boolean remove = processDataProperty(propName, dataBaseMap, dataBaseMapTmp, className);
					if (!remove) {
						Element claseChildPostProcess = jdomParser.cloneNode(claseChild);
						if (claseChildName.equals(XMLConstants.TAG_DATA_PROPERTY))
							claseChildPostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
						else if (claseChildName.equals(XMLConstants.TAG_MEMO)) {
							Iterator it2 = claseChild.getChildren().iterator();
							while (it2.hasNext()) {
								Element childMemo = (Element)it2.next();
								Element childMemoPostProcess = jdomParser.cloneNode(childMemo);
								claseChildPostProcess.addContent(childMemoPostProcess);
							}
						}

						clasePostProcess.addContent(claseChildPostProcess);
					}
				} else {
					ClassInfo classInfoChild = dataBaseMapTmp.getClass(claseChildName);
					//si es nulo es xq no está en la base de datos con el nuevo modelo, no se ha transformado
					boolean eliminacion = true;
					if (classInfoChild!=null) {
						int idtoChild = classInfoChild.getIdto();
						String nameChild = classInfoChild.getName();
						if (!setRdnsNoAbsToAbs.contains(nameChild)) {
							eliminacion = false;
							//si la clase relacionada con el padre a traves de la objectProperty, esta en el nuevo modelo -> continuar
							if (existsObjectProperty(f, dataBaseMapTmp, className, claseChildName, claseChild.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm))) {
								Element claseChildPostProcess = processRefNode(objects, claseChild);
								claseChildPostProcess.setAttribute(XMLConstants.ATTRIBUTE_IDNODE, String.valueOf(idNode));
								idNode++;
								//eliminar dataProperties que para esta clase no esten en el nuevo modelo
								processDataProperties(f, dataBaseMap, dataBaseMapTmp, claseChildName, claseChild, claseChildPostProcess);
								
								clasePostProcess.addContent(claseChildPostProcess);
								idNode=postProcessXMLRec(classNameRoot, f, systemTablesNames, dataBaseMap, dataBaseMapTmp, rdnsCProcesados, idosProcesadosEnIdtosNoProcesados, 
										objects, claseChild, claseChildPostProcess, propsStructToNoStruct, false, setRdnsNoAbsToAbs, idNode);
							}
						}
					} else
						classInfoChild = dataBaseMap.getClass(claseChildName);
					if (eliminacion) {
						System.out.println("ELIMINACION -> No existe la clase " + classInfoChild.getName());
						f.write("ELIMINACION -> No existe la clase " + classInfoChild.getName() + "\n");
					}
				}
			}
		}
		
		return idNode;
	}
	private static Element processRefNode(Element objects, Element clase) throws DataErrorException, SQLException, NamingException, JDOMException {
		//sustituir nodos ref por nodos reales
		Element clasePostProcess = null;
		String refNode = clase.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		if (refNode!=null) {
			String property = clase.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
			clasePostProcess = jdomParser.cloneNode(jdomParser.findElementByAt(objects, XMLConstants.ATTRIBUTE_IDNODE, refNode, true));
			if (property!=null)
				clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_PROPERTYm, property);
		} else {
			clasePostProcess = jdomParser.cloneNode(clase);
		}
		return clasePostProcess;
	}
}
