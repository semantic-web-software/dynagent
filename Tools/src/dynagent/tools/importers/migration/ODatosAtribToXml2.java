package dynagent.tools.importers.migration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.transform.TransformerConfigurationException;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.Changes;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.FileException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.knowledge.action;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.common.xml.XMLTransformer;
import dynagent.migration.relational.Migrator;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.FactsAdapter;
import dynagent.server.services.InstanceService;
import dynagent.server.services.XMLConstants;
import dynagent.tools.importers.configxml.AuxiliarMigrator;
import dynagent.tools.importers.configxml.Menu;
import dynagent.tools.importers.owl.OWLParser;

public class ODatosAtribToXml2 {

	private static boolean isAbstract(FactoryConnectionDB fcdbOrigin, int idto, ClassInfo classInfo) throws SQLException, NamingException {
		boolean abstracta = false;
//		String gestor = fcdbOrigin.getGestorDB();
//		if (gestor.equals(GestorsDBConstants.mySQL)) {
//			String sql = "select * from access where idto=" + idto + " and accesstype=" + Constants.ACCESS_ABSTRACT;
//			List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sql);
//			abstracta = lList.size()>0;
//		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			abstracta = classInfo.isAbstractClass();
//		}
		return abstracta;
	}
	public static void transform(DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, FactoryConnectionDB fcdbOrigin, FactoryConnectionDB fcdbDestination, 
			HashSet<String> setRdnsNewC, HashSet<String> setRdnsDelC, HashSet<String> setRdnsNoAbsToAbs, 
			HashSet<String> propsStructToNoStruct, File xsltFile, OWLParser owlParserDestination, 
			boolean importReports, boolean importQuerys, String pathXML, boolean replica, 
			String nameXmlFile, String pathReport, String pathQuery, String pathImportOtherXml, String pathImportReports, 
			boolean insertUserAndRoles, boolean insertIndex) throws Exception {
		// Modificamos la base de datos para que tenga las tablas con las que trabaja el nuevo modelo.
		owlParserDestination.buildDataBase("BusinessFunctions",true,true);
		
		InstanceService instanceServiceOrigin = Menu.setConnection(fcdbOrigin, dataBaseMapOrigin);
		
		Set<ClassInfo> sClassInfo = dataBaseMapOrigin.getAllClasses();
		HashSet<String> rdnsCPreProcesados = new HashSet<String>();
		HashSet<String> rdnsCProcesados = new HashSet<String>();
		//x cada clase
		String pathLogFile = "C:\\DYNAGENT\\logODAToRelational.txt";
		String pathDeletedLogFile = "C:\\DYNAGENT\\logDeletedODAToRelational.txt";
		FileWriter f = null;
		FileWriter deletedLogFile=null;
		try {
			//f = new FileWriter(pathLogFile);
			//deletedLogFile = new FileWriter(pathDeletedLogFile);
			HashMap<Integer,Integer> idoFicticioReal = new HashMap<Integer,Integer>();
			HashSet<Integer> idosProcesadosEnIdtosNoProcesados = new HashSet<Integer>();
			HashSet<String> systemTablesNames = Menu.getSystemTablesNames();
			for (ClassInfo classInfo : sClassInfo) {
				int idto = classInfo.getIdto();
				String className = classInfo.getName();
				if (!isAbstract(fcdbOrigin, idto, classInfo) && !setRdnsNewC.contains(className)) {
					Set<ClassInfo> sClassInfoRef = dataBaseMapOrigin.getClassesReferencingClass(idto);
					
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
						
						dataTransform(f, dataBaseMapOrigin, dataBaseMapDestination, fcdbOrigin, fcdbDestination, 
								classInfo, idoFicticioReal, rdnsCPreProcesados, rdnsCProcesados, 
								systemTablesNames, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, 
								propsStructToNoStruct, xsltFile, true, deletedLogFile);
						//AuxiliarMigrator.dataMigration(f, classInfo, rdnsCPreProcesados, rdnsCProcesados, idosProcesadosEnIdtosNoProcesados, xsltFile, dataBaseMapOrigin, dataBaseMapDestination, fcdbOrigin, fcdbDestination, instanceServiceOrigin, systemTablesNames, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, propsStructToNoStruct, true, deletedLogFile);
					}
				}
			}
			
			//importar con config
			owlParserDestination.insertModelIndividuals();
			owlParserDestination.insertHelp();
			
			//importo solo configuracion
			System.out.println("Antes de importar configuración");
			Menu menu = new Menu();
			menu.importXML(importReports, importQuerys, pathXML, replica, fcdbDestination, 
					nameXmlFile, insertUserAndRoles, insertIndex, pathReport, pathQuery, 
					pathImportOtherXml, pathImportReports);
		} finally {
			if(f!=null){
				f.close();
			}
			fcdbOrigin.removeConnections();
			fcdbDestination.removeConnections();
			if(deletedLogFile!=null){
				deletedLogFile.close();
			}
		}
	}

	private static boolean hasDatasOfIdto(FactoryConnectionDB fcdbOrigin, String tableName, int idto) throws SQLException, NamingException {
		String gestor = fcdbOrigin.getGestorDB();
		GenerateSQL gSQL = new GenerateSQL(gestor);
		String sql = null;
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			sql = "select * from o_datos_atrib where id_to=" + idto + " limit 1";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			sql = "select * from " + gSQL.getCharacterBegin() + tableName + gSQL.getCharacterEnd() + " limit 1";
		}
		List<List<String>> lList = DBQueries.executeQuery(fcdbOrigin, sql);
		return lList.size()>0;
	}
	private static void dataTransform(FileWriter f, DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, 
			FactoryConnectionDB fcdbOrigin, FactoryConnectionDB fcdbDestination, 
			ClassInfo classInfo, HashMap<Integer,Integer> idoFicticioIdoReal, 
			HashSet<String> rdnsCPreProcesados, HashSet<String> rdnsCProcesados, 
			HashSet<String> systemTablesNames, 
			HashSet<String> setRdnsNewC, HashSet<String> setRdnsDelC, HashSet<String> setRdnsNoAbsToAbs, HashSet<String> propsStructToNoStruct, File xsltFile, 
			boolean first,FileWriter deletedLogFile) throws SQLException, NamingException, JDOMException, IOException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NoSuchColumnException, TransformerConfigurationException, FileException {
		
		int idto = classInfo.getIdto();
		String className = classInfo.getName();
		System.out.println("classInfo->idto " + idto + ", name " + className);
		String tableName = dataBaseMapOrigin.getTable(idto).getName();
		System.out.println("tableName " + tableName);
		rdnsCPreProcesados.add(className);
//		boolean hasData = true;
//		if (first) {
//			//comprueba si hay datos
//			hasData = hasDatasOfIdto(fcdbOrigin, tableName, idto);
//		}
//		if (!hasData) {
//			System.out.println("No hay datos de " + className);
//		} else {
			//obtener clases hijas e iterar x ellas
			for (Integer idto2 : classInfo.getReferencedClasses()) {
				ClassInfo classInfo2 = dataBaseMapOrigin.getClass(idto2);
//				boolean iterate = notInverseToIterate(classInfo, idto2, dataBaseMap);
//				if (iterate) {
					Set<Integer> idtosSpec = classInfo2.getChildClasses();
					idtosSpec.add(idto2);
					for (Integer idtoSpec : idtosSpec) {
						String nameSpec=dataBaseMapOrigin.getClass(idtoSpec).getName();
						if (!rdnsCPreProcesados.contains(nameSpec)) {
							ClassInfo classInfoSpec = dataBaseMapOrigin.getClass(idtoSpec);
							if (!isAbstract(fcdbOrigin, idtoSpec, classInfoSpec) && !setRdnsNewC.contains(classInfoSpec.getName())) {
								dataTransform(f, dataBaseMapOrigin, dataBaseMapDestination, fcdbOrigin, fcdbDestination, 
										classInfoSpec, idoFicticioIdoReal, rdnsCPreProcesados, rdnsCProcesados, 
										systemTablesNames, setRdnsNewC, setRdnsDelC, setRdnsNoAbsToAbs, 
										propsStructToNoStruct, xsltFile, false, deletedLogFile);
							}
						}
					}
//				}
			}
			
			//migrar solo si no es del sistema
			if (!systemTablesNames.contains(tableName)) {
				boolean dataTransform = xsltFile!=null;
				
				Document factDocument = constructFactsXML(dataBaseMapOrigin, fcdbOrigin, idto);
				if (factDocument.getRootElement().getChildren().size()>0) {
					Document resultXML = FactsAdapter.factsXMLToDataXML(dataBaseMapOrigin, factDocument);
					
					if (dataTransform) {
						System.out.println("Element antes de transformar en tools " + jdomParser.returnXML(resultXML.getRootElement()));
						if(f!=null){
							f.write("Element antes de transformar en tools " + jdomParser.returnXML(resultXML.getRootElement()) + "\n");
						}
						resultXML = XMLTransformer.getTransformedDocument(resultXML, xsltFile);
					}
					
					Document resultXMLPostProcess = postProcessXML(f, systemTablesNames, dataBaseMapOrigin, 
							dataBaseMapDestination, rdnsCProcesados, setRdnsDelC, setRdnsNoAbsToAbs, 
							resultXML.getRootElement(), propsStructToNoStruct, idoFicticioIdoReal, deletedLogFile);
					rdnsCProcesados.add(className);
					
					if (resultXMLPostProcess!=null) {
						InstanceService instanceService = new InstanceService(fcdbDestination, null, false);
						instanceService.setDataBaseMap(dataBaseMapDestination);
	//					ServerEngine serverEngine = new ServerEngine(fcdbDestination);
	//					instanceService.setIk(serverEngine);
						DBQueries.execute(fcdbDestination, "START TRANSACTION");
						Changes changes = instanceService.serverTransitionObject("migration", resultXMLPostProcess, null, null, true, false, null,true);
						DBQueries.execute(fcdbDestination, "END");
						
						ArrayList<ObjectChanged> aObjectChanged = changes.getAObjectChanged();
						for (ObjectChanged objectChanged : aObjectChanged) {
							Integer oldIdo = objectChanged.getOldIdo();
							if (oldIdo!=null) {
								int newIdo = objectChanged.getNewIdo();
								idoFicticioIdoReal.put(oldIdo, QueryConstants.getTableId(newIdo));
							}
						}
					}
				} else
					System.out.println("No hay datos de " + className);
				factDocument = null;
			}
//		}
	}
	
	private static Document postProcessXML(FileWriter f, HashSet<String> systemTablesNames, 
			DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, HashSet<String> rdnsCProcesados, 
			HashSet<String> setRdnsDelC, HashSet<String> setRdnsNoAbsToAbs, Element data, 
			HashSet<String> propsStructToNoStruct, HashMap<Integer,Integer> idoFicticioIdoReal,
			FileWriter deletedLogFile) throws DataErrorException, SQLException, NamingException, JDOMException, IOException {
		Document docDataPostProcess = null;
		Element dataPostProcess = null;
		Element objectsPostProcess = null;
		
		boolean first = true;
		int contIdoNeg = -1;
		Element objects = data.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> children = objects.getChildren();
		Iterator it = children.iterator();
		while (it.hasNext()) {
			Element clase = (Element)it.next();
			String className = clase.getName();
			ClassInfo classInfo = dataBaseMapDestination.getClass(className);
			
			if (first) {
				System.out.println("Element antes de procesar en tools " + jdomParser.returnXML(data));
				if(f!=null){
					f.write("Element antes de procesar en tools " + jdomParser.returnXML(data) + "\n");
				}
//				if (setIdsDelC.contains(classInfo.getIdto())) {
				if (classInfo==null || setRdnsNoAbsToAbs.contains(classInfo.getName())) {
					System.out.println("ELIMINACION -> No existe la clase " + className);
					if(f!=null){
						f.write("ELIMINACION -> No existe la clase " + className + "\n");
					}
					break;
				} else {
					dataPostProcess = jdomParser.cloneNode(data);
					objectsPostProcess = jdomParser.cloneNode(objects);
					dataPostProcess.addContent(objectsPostProcess);
					
					Set<Integer> deletedIdNodes=AuxiliarMigrator.transformTableIdOfClassAndProperties(objects,dataBaseMapOrigin,dataBaseMapDestination,deletedLogFile,objects);
					AuxiliarMigrator.deleteRefNodes(objects,deletedIdNodes,deletedLogFile,dataBaseMapOrigin);
					
					first = false;
				}
			}
			
			Element clasePostProcess = jdomParser.cloneNode(clase);
			//eliminar dataProperties que para esta clase no esten en el nuevo modelo
			processDataProperties(f, dataBaseMapOrigin, dataBaseMapDestination, className, clase, clasePostProcess);
			
			objectsPostProcess.addContent(clasePostProcess);
			boolean firstNode = true;
			postProcessXMLRec(f, systemTablesNames, dataBaseMapOrigin, dataBaseMapDestination, rdnsCProcesados, 
					objects, objectsPostProcess, clase, clasePostProcess, propsStructToNoStruct, firstNode, idoFicticioIdoReal, setRdnsNoAbsToAbs);
		}
		if (dataPostProcess!=null) {
			docDataPostProcess = new Document(dataPostProcess);
			//System.out.println("Element despues de procesar en tools " + jdomParser.returnXML(dataPostProcess) + "\n\n");
			if(f!=null){
				f.write("Element despues de procesar en tools " + jdomParser.returnXML(dataPostProcess) + "\n\n\n");
			}
		}
		return docDataPostProcess;
	}
	
	private static void processDataProperties(FileWriter f, DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, 
			String className, Element clase, Element clasePostProcess) throws IOException {
		List<Attribute> listAttr = clase.getAttributes();
		for (Attribute attribute : listAttr) {
			System.out.println("className " + className + ", attribute " + attribute);
			String propName = attribute.getName();
			boolean remove = processDataProperty(propName, dataBaseMapOrigin, dataBaseMapDestination, className);
			if (remove) {
				clasePostProcess.removeAttribute(propName);
				System.out.println("ELIMINACION -> No existe la DP para clase " + className + ", dp " + propName);
				if(f!=null){
					f.write("ELIMINACION -> No existe la DP para clase " + className + ", dp " + propName + "\n");
				}
			}
		}
	}
	private static boolean processDataProperty(String propName, DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, 
			String className) {
		boolean remove = false;
		Integer idPropOrig = dataBaseMapOrigin.getPropertyId(propName);
		if (idPropOrig!=null) {
			Integer idProp = dataBaseMapDestination.getPropertyId(propName);
			if (idProp!=null) {
				ClassInfo classInfo = dataBaseMapDestination.getClass(className);
				PropertyInfo propInfo = classInfo.getProperty(idProp);
				if (propInfo==null)
					remove = true;
			} else
				remove = true;
		}
		return remove;
	}
	private static boolean existsObjectProperty(FileWriter f, DataBaseMap dataBaseMapDestination, String classNameParent, String className, String propName) 
			throws IOException {
		boolean exists = true;
		ClassInfo classInfoDomain = dataBaseMapDestination.getClass(classNameParent);
		Integer idProp = dataBaseMapDestination.getPropertyId(propName);
		if (idProp!=null) {
			ClassInfo classInfoRange = dataBaseMapDestination.getClass(className);
			if (classInfoRange!=null) {
				//buscar superiores a classInfoRange
				PropertyInfo propInfo = classInfoDomain.getProperty(idProp);
				if (propInfo!=null) {
					Set<Integer> types = propInfo.getPropertyTypes();
					
					Set<Integer> idtoAndSuperiors = classInfoRange.getParentClasses();
					idtoAndSuperiors.addAll(classInfoRange.getChildClasses());
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
			if(f!=null){
				f.write("ELIMINACION -> No existe la OP para clasePadre " + classNameParent + ", op " + propName + ", clase " + className + "\n");
			}
		}
		return exists;
	}

	private static Integer processTableId(FileWriter f, Element clasePostProcess, int idto, HashMap<Integer,Integer> idoFicticioIdoReal) throws IOException {
		Integer tableIdReturn = null;
		String action = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
		if (action.equals(XMLConstants.ACTION_SET)) {
			Integer tableIdFict = Integer.parseInt(clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID));
			if (tableIdFict<0) { //es un ido negativo, al ser un set tengo que poner el tableId
				tableIdReturn = idoFicticioIdoReal.get(tableIdFict);
				System.out.println("DEBUG -> en un set cambia el ido " + tableIdFict + " por " + tableIdReturn);
				clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(tableIdReturn));
			} else {
				tableIdReturn = tableIdFict;
			}
		} else if (action.equals(XMLConstants.ACTION_NEW)) {
			String tableIdReturnStr = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			if (tableIdReturnStr!=null)
				tableIdReturn = Integer.parseInt(tableIdReturnStr);
			else
				tableIdReturn = null;
		}
		return tableIdReturn;
	}
	private static void postProcessXMLRec(FileWriter f, HashSet<String> systemTablesNames, 
			DataBaseMap dataBaseMapOrigin, DataBaseMap dataBaseMapDestination, 
			HashSet<String> rdnsCProcesados, 
			Element objects, Element objectsPostProcess, Element clase, Element clasePostProcess, HashSet<String> propsStructToNoStruct, boolean firstNode,  
			HashMap<Integer,Integer> idoFicticioIdoReal, HashSet<String> setRdnsNoAbsToAbs) 
			throws DataErrorException, SQLException, NamingException, JDOMException, IOException  {
		//añadir action en nodos
		String className = clase.getName();
		ClassInfo classInfo = dataBaseMapDestination.getClass(className);
		int idto = classInfo.getIdto();
		if (firstNode) {
			clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
			Integer tableId = processTableId(f, clasePostProcess, idto, idoFicticioIdoReal);
			if (tableId!=null) {
				if (idoFicticioIdoReal.get(tableId)!=null) {
					clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
					processTableId(f, clasePostProcess, idto, idoFicticioIdoReal);
				}
			}
		} else {
			ArrayList<String> namesAttr = new ArrayList<String>();
			List<Attribute> lAttr = clasePostProcess.getAttributes();
			for (Attribute attribute : lAttr) {
				String propName = attribute.getName();
				if (	!propName.equals(XMLConstants.ATTRIBUTE_ACTION) &&
						!propName.equals(XMLConstants.ATTRIBUTE_IDNODE) && 
						!propName.equals(XMLConstants.ATTRIBUTE_PROPERTYm) && 
						!propName.equals(XMLConstants.ATTRIBUTE_TABLEID) && 
						!propName.equals(Constants.PROP_RDN) && 
						!propName.equals(XMLConstants.ATTRIBUTE_REFNODE))
					namesAttr.add(propName);
			}
			for (String name : namesAttr) {
				clasePostProcess.removeAttribute(name);
			}
			Attribute actionAttribute = clasePostProcess.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
			System.out.println("IDTO "+idto);
			Table t=dataBaseMapDestination.getTable(idto);
			Integer tableId=null;
			String tableName =null; 
			
			if(t!=null){
				tableName=t.getName();
				tableId = processTableId(f, clasePostProcess, idto, idoFicticioIdoReal);
			}						
			
			if (tableId!=null) {
				if (systemTablesNames.contains(tableName) || rdnsCProcesados.contains(className) || idoFicticioIdoReal.get(tableId)!=null) {
					if(tableId>0 || idoFicticioIdoReal.get(tableId)!=null){
						clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
					}else{
						clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
					}
					processTableId(f, clasePostProcess, idto, idoFicticioIdoReal);
				} else {
					clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_NEW);
					String property = clasePostProcess.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
					if (propsStructToNoStruct.contains(property)) {
						clasePostProcess.setAttribute(Constants.PROP_RDN, Constants.DEFAULT_RDN_CHAR + tableId + Constants.DEFAULT_RDN_CHAR);
					}
				}
			}else{
				String rdn = clasePostProcess.getAttributeValue(Constants.PROP_RDN);
				String action = actionAttribute.getValue();		
				System.out.println("ACTION "+action);
				if (rdn!=null&&!action.equals(XMLConstants.ACTION_NEW)) {
					//buscar en bd a que tableId corresponde ese rdn
					Integer ido=InstanceService.getIdo(dataBaseMapDestination.getFactoryConnectionDB(), dataBaseMapDestination, idto, rdn, false);
					if (ido!=null) {
						Integer tableIdDB = QueryConstants.getTableId(ido);
						System.out.println("TABLE DE RDN "+tableIdDB);
						if(action.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST))		actionAttribute.setValue(XMLConstants.ACTION_SET);
							
						clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(tableIdDB));
						clasePostProcess.removeAttribute(Constants.PROP_RDN);
					} else {
						/*
						if(action.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST))		actionAttribute.setValue(XMLConstants.ACTION_NEW);
						
						clasePostProcess.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(contIdoNeg));
						if(rdn.equals("tableId")) clasePostProcess.setAttribute(Constants.PROP_RDN, "&id"+contIdoNeg+"&");
						contIdoNeg--;
						*/
						throw new DataErrorException("Nodo " + className + " con operation "+ action + " y rdn "+rdn+" no existe en base de datos");
					}
				}
			}
		}
		
		//if (firstNode) { //solo quiero 1 nivel
			//iterar por los hijos
			Iterator it = clase.getChildren().iterator();
			while (it.hasNext()) {
				Element claseChild = (Element)it.next();
				String claseChildName = claseChild.getName();
				if (claseChildName.equals(XMLConstants.TAG_MEMO) || 
						claseChildName.equals(XMLConstants.TAG_DATA_PROPERTY)) {
					String propName = claseChild.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
					boolean remove = processDataProperty(propName, dataBaseMapOrigin, dataBaseMapDestination, className);
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
					ClassInfo classInfoChild = dataBaseMapDestination.getClass(claseChildName);
					//si es nulo es xq no está en la base de datos con el nuevo modelo, no se ha transformado
					boolean eliminacion = true;
					if (classInfoChild!=null) {
						int idtoChild = classInfoChild.getIdto();
						String nameChild = classInfoChild.getName();
						if (!setRdnsNoAbsToAbs.contains(nameChild)) {
							eliminacion = false;
							if (existsObjectProperty(f, dataBaseMapDestination, className, claseChildName, claseChild.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm))) {
								Element claseChildPostProcess = jdomParser.cloneNode(claseChild);
								//eliminar dataProperties que para esta clase no esten en el nuevo modelo
								processDataProperties(f, dataBaseMapOrigin, dataBaseMapDestination, claseChildName, claseChild, claseChildPostProcess);
								
								clasePostProcess.addContent(claseChildPostProcess);
								postProcessXMLRec(f, systemTablesNames, dataBaseMapOrigin, dataBaseMapDestination, rdnsCProcesados, 
										objects, objectsPostProcess, claseChild, claseChildPostProcess, propsStructToNoStruct, false, idoFicticioIdoReal, setRdnsNoAbsToAbs);
							}
						}
					} else
						classInfoChild = dataBaseMapOrigin.getClass(claseChildName);
					if (eliminacion) {
						System.out.println("ELIMINACION -> No existe la clase " + claseChildName);
						if(f!=null){
							f.write("ELIMINACION -> No existe la clase " + claseChildName + "\n");
						}
					}
				}
			}
//		}
	}
	
	private static void printTimeResults(long beginTime, long mapConstructTime, long beginTimeTransform, 
			long timeAfterFactConstruct, long timeForTraduction, long endTime) {
		float mapTime = (mapConstructTime - beginTime) / 1000.0F;
		float factTime = (timeAfterFactConstruct - beginTimeTransform) / 1000.0F;
		float traductionTime = (timeForTraduction - timeAfterFactConstruct) / 1000.0F;
		float writeFileTime = (endTime - timeForTraduction) / 1000.0F;
		float totalTime = (endTime - beginTime) / 1000.0F;
		
		System.out.println("Se ha tardado " + totalTime + " segundos en todo el proceso");
		System.out.println("\tSe ha tardado " + mapTime + " segundos en construir el mapeo de la base de datos");
		System.out.println("\tSe ha tardado " + factTime + " segundos en traducir o_datos_atrib al XML de Facts");
		System.out.println("\tSe ha tardado " + traductionTime + " segundos en traducir el XML de Facts al nuevo modelo de XML");
		System.out.println("\tSe ha tardado " + writeFileTime + " en construir el fichero resultante del XML del nuevo modelo");
	}
	
	private static Document constructFactsXML(DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int idto) {
		Element factsRootElement = new Element("FACTS");
		Document factsDocument = new Document(factsRootElement);
		constructFactsODAXML(dataBaseMap, fcdb, idto, factsRootElement);
		constructFactsODAMXML(dataBaseMap, fcdb, idto, factsRootElement);
		return factsDocument;
	}
	private static void constructFactsODAXML(DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int idto, Element factsRootElement) {
		String sqlGetODatosAtrib = "SELECT DISTINCT ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, DESTINATION FROM o_datos_atrib " +
				"WHERE id_to =" + idto;

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			statement.setFetchSize(100);
			resultSet = statement.executeQuery(sqlGetODatosAtrib);
			
			while (resultSet.next()) {
				Integer ido = resultSet.getInt(2);
				if (resultSet.wasNull()) {
					ido = null;
				}
				Integer property = resultSet.getInt(3);
				if (resultSet.wasNull()) {
					property = null;
				}
				Integer valNum = resultSet.getInt(4);
				if (resultSet.wasNull()) {
					valNum = null;
				}
				String valText = resultSet.getString(5);
				if (resultSet.wasNull()) {
					valText = null;
				}
				Integer valueCls = resultSet.getInt(6);
				if (resultSet.wasNull()) {
					valueCls = null;
				}
				Double qMax = resultSet.getDouble(7);
				if (resultSet.wasNull()) {
					qMax = null;
				}
				if (valNum == null && valText == null && qMax == null){
					// Entrada que solo tiene system value y que de momento vamos a ignorar.
					continue;
				}
				String destination = resultSet.getString(8);
				Element newElement = constructFactElement(ido, idto, property, valNum, valText, valueCls, qMax, destination, dataBaseMap, fcdb);
				factsRootElement.addContent(newElement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static void constructFactsODAMXML(DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int idto, Element factsRootElement) {
		String sqlGetODatosAtrib = "SELECT DISTINCT ID_TO, ID_O, PROPERTY, MEMO, VALUE_CLS, DESTINATION FROM o_datos_atrib_memo " +
				"WHERE id_to =" + idto;

		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			statement = connectionDB.getBusinessConn().createStatement();
			statement.setFetchSize(100);
			resultSet = statement.executeQuery(sqlGetODatosAtrib);
			
			while (resultSet.next()) {
				Integer ido = resultSet.getInt(2);
				if (resultSet.wasNull()) {
					ido = null;
				}
				Integer property = resultSet.getInt(3);
				if (resultSet.wasNull()) {
					property = null;
				}
				String memo = resultSet.getString(4);
				if (resultSet.wasNull()) {
					memo = null;
				}
				Integer valueCls = resultSet.getInt(5);
				if (resultSet.wasNull()) {
					valueCls = null;
				}
				if (memo == null){
					// Entrada que solo tiene system value y que de momento vamos a ignorar.
					continue;
				}
				String destination = resultSet.getString(6);
				Element newElement = constructFactElement(ido, idto, property, null, memo, valueCls, null, destination, dataBaseMap, fcdb);
				factsRootElement.addContent(newElement);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static Element constructFactElement(Integer ido, Integer idto, Integer property, Integer valNum,
			String valText, Integer valueCls, Double qMax, String destination, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) {
		Element factElement = new Element("FACT");
		Element newFactElement = new Element("NEW_FACT");
		factElement.addContent(newFactElement);
		newFactElement.setAttribute(new Attribute("IDO", "-" + ido.toString()));
		newFactElement.setAttribute(new Attribute("IDTO", idto.toString()));
		newFactElement.setAttribute(new Attribute("PROP", property.toString()));
		newFactElement.setAttribute(new Attribute("VALUECLS", valueCls.toString()));
		newFactElement.setAttribute(new Attribute("ORDER", String.valueOf(action.NEW)));
		if (destination != null && ! destination.isEmpty()){
			newFactElement.setAttribute(new Attribute("DESTINATION_SYSTEM", destination));
		}
		if (qMax != null) {
			newFactElement.setAttribute(new Attribute("QMAX", qMax.toString()));
		} else {
			String content;
			if (valNum != null) {
				content = "-" + valNum.toString();
				if (valText!=null)
					newFactElement.setAttribute(new Attribute("RDNVALUE", valText));
			} else {
				content = valText;
			}
			try {
				// Esto se hace porque las contraseñas no se pueden añadir tal y como vienen de base de datos, hay que
				// desencriptarlas
				newFactElement.addContent(content);
			} catch (IllegalDataException e) {
				GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
				String sqlDecrypt = "select " + generateSQL.getDecryptFunction("dynamicIntelligent", "'" + content + "'");
				
				ConnectionDB connectionDB = fcdb.createConnection(true);
				Statement statement = null;
				ResultSet resultSet = null;

				try {
					statement = connectionDB.getBusinessConn().createStatement();
					resultSet = statement.executeQuery(sqlDecrypt);
					while (resultSet.next()) {
						content = generateSQL.getDecryptData(resultSet, 1);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
				} catch (NamingException e1) {
					e1.printStackTrace();
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
					if (resultSet != null) {
						try {
							resultSet.close();
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
					}
				}

				newFactElement.addContent(content);
			}
		}
		return factElement;
	}

}
