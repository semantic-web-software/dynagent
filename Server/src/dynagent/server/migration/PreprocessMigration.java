package dynagent.server.migration;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.DatabaseManager;
import dynagent.server.services.XMLConstants;

public final class PreprocessMigration {

	/** Valor que se tiene que usar como id_node para la proxima vez que se requiera un identificador. */
	private static int nextIdNode;
	
	private static int nextTableId;
	
	/**
	 * añade la informacion necesaria al XML dependiendo de las acciones que
	 * tengan los nodos. A los nodos <code>action="new"</code> se les añade un
	 * tableId negativo ficticio. A los nodos con <code>action="set"</code> hay
	 * que buscar el tableId en base de datos.
	 * 
	 * @param document
	 *            Documento al que le tenemos que añadir la informacion.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException 
	 * @throws JDOMException 
	 */
	@SuppressWarnings("unchecked")
	public static void preProcessDocument(Document document,DataBaseMap dataBaseMap,HashSet<String> destinations) throws DataErrorException, SQLException, NamingException, JDOMException {
		Element rootElement = document.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> children = new LinkedList<Element>(objectsElement.getChildren());
		/** Mapa de las asociaciones rdn-id_node que se han ido dando a medida que se construia el XML */
		Map<Integer, Map<String, Integer>> idenfiersByClass=new Hashtable<Integer, Map<String,Integer>>();
		/** Mapa de los elementos que contienen los datos indexados por su id_node. */
		Map<Integer, Element> elementsByIdNode=new Hashtable<Integer, Element>();
		
		nextTableId=-1;
		//Recorremos todos los nodos para encontrar tableIds que ya esten asignados. Quedandonos como unicio de contador con el mas bajo, para evitar que se pisen en los nuevos que asignemos
		//Solo van a venir negativos. Los que nosotros asignemos tambien seran negativos
		for (Element child : children) {
			String elementName = child.getName();
			if(elementName.equals(elementName.toLowerCase())){//parece ser que migrador lo pasa ya como nombre de tabla
				int lasTableId=DatabaseManager.getLastTableId(dataBaseMap.getFactoryConnectionDB(),elementName);
			
				if(lasTableId> Math.abs(nextTableId)){
					nextTableId=Math.min(lasTableId-1,nextTableId);
					System.out.println("REDUCIENDO TABLEID EXISTENTE "+lasTableId);
				}
			}
			if (elementName.equals(XMLConstants.TAG_MEMO) || elementName.equals(XMLConstants.TAG_DATA_PROPERTY)){
				continue;
			}
			
			Attribute tableIdAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
			if(tableIdAttribute!=null){
				String value=tableIdAttribute.getValue();
				int intValue=Integer.valueOf(value);
				if(intValue<=nextTableId){
					nextTableId=intValue-1;
				}
			}
		}
		
		nextIdNode=1;
		boolean destinationGlobal=false;
		for (Element child : children) {
			boolean destG=processChild(child,dataBaseMap,idenfiersByClass,elementsByIdNode,destinations,destinationGlobal);
			if(destG) destinationGlobal=true;
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean processChild(Element element, DataBaseMap dataBaseMap, Map<Integer, Map<String, Integer>> idenfiersByClass, Map<Integer, Element> elementsByIdNode,HashSet<String> destinationList,boolean destinationGlobal) throws DataErrorException, SQLException, NamingException, JDOMException {
		String elementName = element.getName();
		
		if(destinationList!=null&&!destinationGlobal && elementName.equals("NEW_FACT")){
			if(element.getAttribute("DESTINATION_SYSTEM")!=null && !element.getAttribute("DESTINATION_SYSTEM").getValue().equals("*")){
				String destination=element.getAttribute("DESTINATION_SYSTEM").getValue();
				String[] split=destination.split(",");
				for(int i=0;i<split.length;i++){
					destinationList.add(split[i]);
				}
			}else{
				destinationList.clear();
				destinationList.add("*");
				destinationGlobal=true;
			}
		}
		
		if (elementName.equals(XMLConstants.TAG_MEMO) || elementName.equals(XMLConstants.TAG_DATA_PROPERTY)){
			return destinationGlobal;
		}
		
		ClassInfo elementClass = dataBaseMap.getClass(element.getName());
		assert elementClass != null : "No se encuentra la clase de nombre: " + element.getName();
		int idto = elementClass.getIdto();
		Table elementTable = dataBaseMap.getTable(idto);
		Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
		assert actionAttribute != null : "Todos los elementos deben tener un atributo action: " + jdomParser.returnXML(element);
		String rdn = rdnAttribute.getValue();	
		if(rdn!=null){
			String adaptado=DatabaseManager.adaptReplicaValue(dataBaseMap.getFactoryConnectionDB(),rdn);
			if(!rdn.equals(adaptado)){
				rdnAttribute.setValue(adaptado);
				rdn=adaptado;
			}
		}
		
		Integer idNode = null;
		String tableId = null;
		String action = actionAttribute.getValue();

		boolean hasUniqueRdn = ! elementTable.hasForeignProperties();
		Map<String, Integer> idNodeByRdn = idenfiersByClass.get(idto);
		if (hasUniqueRdn){
			// Significa que el elemento no está apuntado por estructurales y,
			// por tanto, el rdn tiene que ser unico.
			assert rdnAttribute != null : "Los elementos que representan objetos deben tener rdn: " + jdomParser.returnXML(element);
			idNode = idNodeByRdn != null ? idNodeByRdn.get(rdn) : null;
		}		
		
		if (action.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST)){
			if (rdnAttribute == null){
				action = XMLConstants.ACTION_NEW;
			}else{
				if (idNode == null){					
					String adaptado=DatabaseManager.adaptReplicaValue(dataBaseMap.getFactoryConnectionDB(),rdn);
					if(!rdn.equals(adaptado)){
						rdnAttribute.setValue(adaptado);
						rdn=adaptado;
					}
					tableId = getTableIdFromDB(elementTable, rdn, dataBaseMap.getFactoryConnectionDB());
				} else{
					Element existingElement = elementsByIdNode.get(idNode);
					String tableIdValue= existingElement.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
					tableId = tableIdValue.startsWith("-") ? null : tableIdValue;
				}
				if (tableId == null){
					action = XMLConstants.ACTION_NEW;
				}else{
					action = XMLConstants.ACTION_SET;
				}
			}
			actionAttribute.setValue(action);
		}
		
		if (idNode != null){
			replaceWithLinkElement(element, idNode);
		}else{
			idNode = nextIdNode;
			nextIdNode ++;
			if (action.equals(XMLConstants.ACTION_NEW)){
				Attribute tableIdAttribute=element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
				int ido=0;
				if(tableIdAttribute==null){//Si no tiene tableId se lo creamos, si lo tiene se lo respetamos
					// Creamos un tableId negativo basindonos en el id_node y el idto.
					ido = nextTableId;//QueryConstants.getIdo(idNode, idto);
					//ido *= -1;
					nextTableId--;
					element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(ido)));
				}else{
					ido = Integer.valueOf(tableIdAttribute.getValue());
				}
				if (! hasUniqueRdn && rdnAttribute == null){
					// Este tipo de elementos pueden venir sin un rdn declarado del XML.
					rdn="&" + ido + "&";
					rdnAttribute = new Attribute(Constants.PROP_RDN,rdn);
					element.setAttribute(rdnAttribute);
				}
			}			
			element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_IDNODE, idNode.toString()));
			elementsByIdNode.put(idNode, element);
			if (hasUniqueRdn){
				// Si no existe el mapa de los rdn asociados con cada id_node
				// para esta clase, lo creamos.
				if (idNodeByRdn == null){
					idNodeByRdn = new Hashtable<String, Integer>();
					idenfiersByClass.put(idto, idNodeByRdn);
				}
				idNodeByRdn.put(rdn, idNode);
			}
			if (action.equals(XMLConstants.ACTION_SET)){
				// Buscamos el tableId en base de datos.
				if (tableId == null){
					tableId = getTableIdFromDB(elementTable, rdn, dataBaseMap.getFactoryConnectionDB());
				}
				if (tableId == null || tableId.isEmpty()){
					throw new DataErrorException("No hay ningun objeto en la tabla " + elementTable.getName() + " cuyo rdn sea " + rdn +" idNode:"+idNode);
				}
				element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableId);
			}
			// Llamar a processChild con cada uno de los hijos.
			List<Element> children = new LinkedList<Element>(element.getChildren());
			for (Element child : children) {
				boolean destGlob=processChild(child, dataBaseMap, idenfiersByClass, elementsByIdNode,destinationList,destinationGlobal);
				if(destGlob) destinationGlobal=true;
			}
		}
		return destinationGlobal;
	}

	/**
	 * Sustituye el elemento dado por un nodo vinculo que represente el enlace
	 * con el objeto que ya existe el el xml que estamos procesando.
	 * 
	 * @param element
	 *            Elemento a sustituir.
	 * @param idNode
	 *            Identificador del nodo que contiene los datos y al que hemos
	 *            de construir el vinculo.
	 */
	private static void replaceWithLinkElement(Element element, int idNode) {
		Attribute propertyAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
		if (propertyAttribute == null){
			// El elemento está a primer nivel y no se pueden dejar nodo vinculo a este nivel.
			element.detach();
			return;
		}
		Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		Element parentElement = element.getParent();
		element.detach();
		Element linkElement = new Element(element.getName());
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, String.valueOf(idNode)));
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_ACTION, actionAttribute.getValue()));
		linkElement.setAttribute(new Attribute(propertyAttribute.getName(), propertyAttribute.getValue()));
		parentElement.addContent(linkElement);
	}

	/**
	 * Busca el tableId del elemento de la tabla representado por el rdn dado.
	 * 
	 * @param table
	 *            Tabla donde hemos de buscar.
	 * @param rdn
	 *            código por el que debemos filtrar.
	 * @return Identificador del objeto asociado a dicho código.
	 * @throws SQLException
	 * @throws NamingException
	 * @throws DataErrorException
	 *             Si no hay ningun objeto con dicho código.
	 */
	private static String getTableIdFromDB(Table table, String rdn, FactoryConnectionDB fcdb) throws SQLException, NamingException, DataErrorException{
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		TableColumn rdnColumn = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		GenerateSQL generate=new GenerateSQL(GestorsDBConstants.postgreSQL);
		String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + rdnColumn.getColumnName() + cE + " "+generate.getLike()+" " + gSQL.parseStringToInsert(rdn);
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		if (queryResult.isEmpty()){
			return null;
		}else{
			return queryResult.get(0).get(0);
		}
	}
}
