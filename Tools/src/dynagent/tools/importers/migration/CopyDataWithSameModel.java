package dynagent.tools.importers.migration;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.ServerEngine;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;
import dynagent.server.services.XMLConstants;
import dynagent.tools.importers.configxml.Menu;

public class CopyDataWithSameModel {

	/**
	 * 
	 * <b>EJEMPLO PARAMETROS DEL MAIN</b><br>
	 * 
	 * -sourceip 127.0.0.1<br>
	 * -sourceport 3310<br>
	 * -sourcebns 9999<br>
	 * -sourcegestor mySQL<br>
	 * <br>
	 * 
	 * -ip 192.168.1.3<br>
	 * -port 3306<br>
	 * -bns 60<br>
	 * -gestor postgreSQL<br>
	 * <br>
	 * 
	 * -owlpath E:/DESARROLLO/Workspace/Francisco/Knowledge/src/owl/<br>
	 * -xmlpath E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/config/<br>
	 * -owlfile MODELO_V1.0.owl<br>
	 * -xmlfile ConfigCELOP.xml<br>
	 * -replica true<br>
	 * -reportpath E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/reports/<br>
	 * -querypath E:/DESARROLLO/Workspace/Francisco/CelopJoven/src/queries/
	 * 
	 * @param args
	 *            Parámetros para el lanzamiento de la utilidad.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Host de origen de los datos a copiar
		String sourceIp = null;
		// Puerto en el que está el servidor de base de datos en el host de origen.
		Integer sourcePort = null;
		// Número de la base de datos de la que tenemos que coger los datos.
		Integer sourceBns = null;
		// Gestor de base de datos que almacena los datos de origen.
		String sourceGestor = null;
		// Host donde tenemos que crear la nueva base de datos.
		String ip = null;
		// Puerto donde tenemos que conectar en el host de la nueva base de datos.
		Integer port = null;
		// Número de la nueva base de datos que tenemos que crear.
		Integer bns = null;
		// Gestor de la nueva base de datos.
		String gestor = null;
		
		String tag = "";
		String menuParams = "";
		
		for (String arg : args){
			if (arg.startsWith("-")){
				tag = arg;
			}else{
				if (tag.equalsIgnoreCase("-sourceip")){
					sourceIp = arg;
				}else if (tag.equalsIgnoreCase("-sourceport")){
					sourcePort = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcebns")){
					sourceBns = Integer.parseInt(arg);
				}else if (tag.equalsIgnoreCase("-sourcegestor")){
					sourceGestor = arg;
				}else if (tag.equalsIgnoreCase("-ip")){
					ip = arg;
					menuParams += " -ip " + arg;
				}else if (tag.equalsIgnoreCase("-port")){
					port = Integer.parseInt(arg);
					menuParams += " -port " + arg;
				}else if (tag.equalsIgnoreCase("-bns")){
					bns = Integer.parseInt(arg);
					menuParams += " -bns " + arg;
				}else if (tag.equalsIgnoreCase("-gestor")){
					gestor = arg;
					menuParams += " -gestor " + arg;
				}else{
					menuParams += " " + tag + " " + arg;
				}
			}
		}
		
		// Una vez hemos sacado los parámetros del array de entrada, procedemos a crear la base de datos
		Menu.main(menuParams.trim().split(" "));
		// Una vez creada la base de datos, sacamos los datos de la base de datos antigua.
		Document document = ODatosAtribToXml.transform(sourceIp, sourcePort, sourceGestor, sourceBns);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, ip, gestor);
		fcdb.setPort(port);
		DataBaseMap dataBaseMap = new DataBaseMap(fcdb, false);
		preprocessDocument(document, fcdb, dataBaseMap);
		InstanceService instanceService = new InstanceService(fcdb, null, false);
		ServerEngine serverEngine = new ServerEngine(fcdb);
		instanceService.setDataBaseMap(dataBaseMap);
		instanceService.setIk(serverEngine);
		DBQueries.execute(fcdb, "START TRANSACTION");
		instanceService.serverTransitionObject("migration", document, null, null, true, false, null,true);
		DBQueries.execute(fcdb, "END");
		
	}

	/**
	 * Pre-procesa el XML de o_datos_atrib para ver si alguno de los elementos
	 * que vienen de la otra base de datos ya existe en la nueva por ser
	 * individuos del sistema.
	 * 
	 * @param document
	 *            XML a analizar.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param dataBaseMap
	 *            Mapa de la nueva base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private static void preprocessDocument(Document document, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws SQLException, NamingException {
		// TODO Auto-generated method stub
		Map<Integer, Element> elementsByIdNode = new Hashtable<Integer, Element>();
		Element objectsElement = document.getRootElement().getChild(XMLConstants.TAG_OBJECTS);
		@SuppressWarnings("unchecked")
		List<Element> children = new LinkedList<Element>(objectsElement.getChildren());
		for (Element child : children) {
			processChild(elementsByIdNode, child);
		}
		searchInDB(elementsByIdNode, fcdb, dataBaseMap);
	}

	/**
	 * Busca para los objetos del mapa, a ver si ya existe alguno con el mismo
	 * rdn. Esta comprobación solo se hace para los objetos no apuntados por
	 * estructurales.
	 * 
	 * @param elementsByIdNode
	 *            Mapa con los elementos que contienen datos indexados por su
	 *            id_node
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param dataBaseMap
	 *            Mapa de la nueva base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private static Set<Integer> searchInDB(Map<Integer, Element> elementsByIdNode, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws SQLException, NamingException {
		Set<Integer> modifiedIdNodes = new HashSet<Integer>();
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		for (Integer idNode : elementsByIdNode.keySet()) {
			Element element = elementsByIdNode.get(idNode);
			Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
			if (rdnAttribute == null){
				// No podemos comprobar si hay un objeto con el mismo rdn si el
				// elemento no define uno.
				continue;
			}
			
			ClassInfo classInfo = dataBaseMap.getClass(element.getName());
			Table table = dataBaseMap.getTable(classInfo.getIdto());
			if (table.hasForeignProperties()){
				// Es apuntada por estructurales.
				continue;
			}
			
			TableColumn column = table.getDataPropertyColumn(Constants.IdPROP_RDN);
			// Realizamos la consulta para saber si hay objetos de la clase del elemento con el mismo rdn.
			String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + column.getColumnName() + cE + "=" + gSQL.parseStringToInsert(rdnAttribute.getValue());
			List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
			String tableId = null;
			for (List<String> objectData : queryResult) {
				tableId = objectData.get(0);
			}
			if (tableId != null){
				// Si entramos aquí, es que había un objeto en base de datos con
				// el mismo rdn, modificamos el elemento para que la acción sea
				// set y le asignamos con tableId el que hemos encontrado.
				// Además, añadimos el id_node al conjunto de identificadores que
				// existen.
				element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableId);
				element.setAttribute(XMLConstants.ATTRIBUTE_ACTION, XMLConstants.ACTION_SET);
				modifiedIdNodes.add(idNode);
			}
		}
		return modifiedIdNodes;
	}

	/**
	 * Comprueba recusivamente un elemento y todos sus hijos para ver si son un
	 * elemento de datos, es decir, tienen el atributo id_node. Si un elemento
	 * tiene el atributo id_node, se explorarán sus hijos también.
	 * 
	 * @param elementsByIdNode
	 *            Mapa donde vamos almacenando los elementos con id_node.
	 * @param element
	 *            Elemento a analizar.
	 */
	private static void processChild(Map<Integer, Element> elementsByIdNode, Element element) {
		Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		try {
			if (idNodeAttribute != null){
				int idNode = idNodeAttribute.getIntValue();
				elementsByIdNode.put(idNode, element);
				@SuppressWarnings("unchecked")
				List<Element> children = new LinkedList<Element>(element.getChildren());
				for (Element child : children) {
					processChild(elementsByIdNode, child);
				}
			}
		} catch (DataConversionException e) {
			System.err.println(element);
			e.printStackTrace();
			System.exit(1);
		}
	}

}
