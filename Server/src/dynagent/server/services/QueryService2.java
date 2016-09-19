package dynagent.server.services;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import dynagent.common.Constants;
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
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.ObjectIdentifier;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class QueryService2 {

	/**
	 * Mapa que contiene los objetos que se quieren consultar organizados por
	 * las clases a las que pertenecen.
	 */
	private Map<Integer, Set<Integer>> idObjects;
	/** Mapa de la base de datos y las clases del modelo */
	private DataBaseMap dataBaseMap;
	/** */
	private InstanceService instanceService;
	/** Objeto que nos permite conectarnos a la base de datos */
	private FactoryConnectionDB factoryConnectionDB;
	/** número de niveles que se quieren consultar */
	private int levels;
	/** Indica si se tienen que bloquear los elementos antes de consultarlos */
	private boolean lock;
	/**
	 * Indica si se tienen que coger los objetos que están apuntados por
	 * propiedades estructurales en el último nivel.
	 */
	private boolean lastStructuralLevel;
	/** Usuario que está ejecutando la consulta */
	private String user;
	/** Documento XML que representa los datos obtenidos de la base de datos. */
	private Document document;
	/** Lista de los elementos que no tienen ningun padre. */
	private List<Element> elementsWithoutParent;
	/**
	 * Este mapa nos permite saber si ya hemos creado un elemento para
	 * representar un elemento. De ser ese el caso, nos devuelve el
	 * identificador de nodo que le hemos asignado, lo que nos permite crear un
	 * nodo vinculo sin buscar más.
	 */
	private Map<ObjectIdentifier, Integer> idNodesByIdentifier;
	
	private Map<Element, Integer> elementIdto;
	/** Mapa de todos los eslementos existentes indexados por su idNode. */
	private Map<Integer, Element> elementsByIdNode;
	/** número que indica cual fue el último identificador de nodo usado. */
	private int lastIdNodeUsed;
	/**
	 * Conjunto de los idNode de los padres del elemento con el que se está
	 * trabajando en este momento
	 */
	private Map<Integer, Set<Integer>> elementParents;
	/**
	 * Conjunto de los idNode de los hijos del elemento con el que se está
	 * trabajando en este momento
	 */
	private Map<Element, Set<Integer>> elementChildren;
	
	private Map<ObjectIdentifier, ObjectRepresentation> objectsByIdentifier;
	
	private boolean migration;
	private String xmlActionType;

	/**
	 * Constructor cuando lo que se quiere consultar es <b>un solo elemento</b>,
	 * aunque se quieran consultar varios niveles. Lo importante es el elemento
	 * desde el que se empieza.
	 * 
	 * @param idto
	 *            Identificador de la clase de la que se quiere consultar.
	 * @param tableId
	 *            Identificador del objeto de la clase que se quiere consultar.
	 * @param dataBaseMap
	 *            Mapa de las tablas y las clases existentes.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param levels
	 *            número de niveles que se quieren consultar. En este caso, hay
	 *            que pensar en la consulta como en un arbol. Tenemos la raiz,
	 *            que es el objeto indicado, y a lo mejor nos interesa navegar a
	 *            traves de object properties a los objetos apuntados para
	 *            obtener sus datos tambien.
	 * @param lastStructuralLevel
	 *            <code>true</code> si queremos que se devuelvan los objetos
	 *            estructurales apuntados por el último nivel consultado.
	 * @param lock
	 *            <code>true</code> si queremos que se bloqueen los objetos
	 *            antes de consultarlos.
	 * @param user
	 *            Si se indica <code>lock==true</code>, tenemos que decir el
	 *            usuario que está haciendo la consulta para poder bloquear los
	 *            objetos. Si <code>lock == false</code>, este parámetro puede
	 *            ir a null, pues no se va a consultar para nada.
	 * @param instanceService TODO
	 */
	public QueryService2(int idto, int tableId, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int levels, boolean lastStructuralLevel, boolean lock, String user, InstanceService instanceService) {
		this(null, dataBaseMap, fcdb, levels, lastStructuralLevel, lock, user, instanceService);
		idObjects = new Hashtable<Integer, Set<Integer>>();
		Set<Integer> tableIds = new HashSet<Integer>();
		tableIds.add(tableId);
		idObjects.put(idto, tableIds);
	}

	/**
	 * Constructor para buscar una serie de individuos de una serie de clases
	 * 
	 * @param idObjects
	 *            Mapa de los tableIds organizados por la clase a la que
	 *            pertenecen. Esto identifica a todos los objetos que se quieren
	 *            buscar.
	 * @param dataBaseMap
	 *            Objeto que contiene la informacion de las clases y las tablas.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @param levels
	 *            Numero de niveles a consultar.
	 * @param lastStructuralLevel
	 *            Indica si del último nivel, traemos aquellos objetos apuntados
	 *            por propiedades estructurales.
	 * @param lock
	 *            Indica si se han de bloquear los objetos antes de ejecutar las
	 *            consultas.
	 * @param user
	 *            Usuario que consulta.
	 * @param instanceService TODO
	 */
	public QueryService2(Map<Integer, Set<Integer>> idObjects, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int levels, boolean lastStructuralLevel, boolean lock, String user, InstanceService instanceService) {
		this.idObjects = idObjects;
		this.migration = false;
		this.dataBaseMap = dataBaseMap;
		this.factoryConnectionDB = fcdb;
		this.levels = levels;
		this.lastStructuralLevel = lastStructuralLevel;
		this.lock = lock;
		this.user = user;
		this.idNodesByIdentifier = new Hashtable<ObjectIdentifier, Integer>();
		this.document = null;
		this.elementsWithoutParent = new LinkedList<Element>();
		this.lastIdNodeUsed = 0;
		this.elementsByIdNode = new Hashtable<Integer, Element>();
		this.elementParents = new Hashtable<Integer, Set<Integer>>();
		this.elementChildren = new Hashtable<Element, Set<Integer>>();
		this.elementIdto = new Hashtable<Element, Integer>();
		this.objectsByIdentifier = new Hashtable<ObjectIdentifier, ObjectRepresentation>();
		this.instanceService = instanceService;
	}

	/**
	 * Modifica el comportamiento de la consulta para que funcione correctamente
	 * dependiendo de si se están migrando datos o no.
	 * 
	 * @param value
	 *            Indica si se están consultando los datos para realizar una
	 *            migración.
	 */
	public void setMigration(boolean value){
		this.migration = value;
	}

	/**
	 * Constructor para busquedas de todos los objetos de una clase
	 * 
	 * @param idto
	 *            Identificador de la clase de la que se quieren obtener todos
	 *            los elementos.
	 * @param searchSpecialized
	 *            Indica si tambien queremos que se obtengan en la busqueda
	 *            todos los elementos de las clases hijas de la anteriormente
	 *            identificada.
	 * @param dataBaseMap
	 *            Objeto que tiene un mapa de todas las clases y tablas del
	 *            modelo.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param levels
	 *            número de niveles que queremos consultar.
	 * @param lastStructuralLevel
	 *            Indica si se quiere traer tambien los objetos apuntados por
	 *            propiedades estructurales del último nivel.
	 * @param lock
	 *            Indica si se quieren bloquear los objetos antes de
	 *            consultarlos.
	 * @param user
	 *            Usuario que ejecuta la consulta.
	 */
	public QueryService2(int idto, boolean searchSpecialized, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb, int levels, 
						boolean lastStructuralLevel, boolean lock, String user, boolean configurationMode, InstanceService instanceService,
						String xmlActionType){
		this(null, dataBaseMap, fcdb, levels, lastStructuralLevel, lock, user, instanceService);
		this.idObjects = new Hashtable<Integer, Set<Integer>>();
		Set<Integer> emptySet = new HashSet<Integer>();
		this.xmlActionType=xmlActionType;
		if (searchSpecialized){
			Map<Integer, Set<Integer>> map;
			try {
				map = DBQueries.getSpecializedClassesMap(fcdb,configurationMode);
			} catch (SQLException e) {
				e.printStackTrace();
				System.err.println("ERROR al intentar cargar las clases especializadas de : " + idto);
				return;
			} catch (NamingException e) {
				e.printStackTrace();
				System.err.println("ERROR al intentar cargar las clases especializadas de : " + idto);
				return;
			}
			Set<Integer> specializedClasses = map.get(idto);
			
			
			for (Integer childIdto : specializedClasses) {
				ClassInfo childClass = dataBaseMap.getClass(childIdto);
				if (childClass != null && ! childClass.isAbstractClass()){
					emptySet = new HashSet<Integer>();
					idObjects.put(childIdto, emptySet);
				}
			}
		}else{
			idObjects.put(idto, emptySet);
		}
	}

	/**
	 * Obtiene de base de datos los datos segun se ha indicado en el
	 * constructor.
	 * 
	 * @return Documento segun el modelo de XML en el que las etiquetas de los
	 *         nodos son los nombres de las clases del objeto al que representan
	 *         y los atributos son las DataProperties del mismo, mientras sus
	 *         nodos hijos son las ObjectProperties que tiene.
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws ApplicationException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws InstanceLockedException 
	 * @throws NoSuchColumnException 
	 */
	public Document getData() throws DataErrorException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		// Lo primero que hacemos es mirar si ya hemos construido el XML para no
		// hacer dos veces el trabajo.
		if (document != null) {
			return document;
		}
		if (lock){
			for (Integer idto: idObjects.keySet()) {
				Set<Integer> tableIds = idObjects.get(idto);
				for (Integer tableId : tableIds) {
				instanceService.lockObject(QueryConstants.getIdo(tableId, idto), idto, user);
				}
			}
		}
		// Es la primera vez que se llama a este metodo, por lo que hay que
		// hacer la consulta para conseguir los datos.
		try {
			document = buildDocument();
		} catch (NoSuchColumnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DataErrorException(e.getMessage());
		}
		return document;
	}

	/**
	 * metodo que construye el documento XML que tenemos que devolver.
	 * 
	 * @return devolvera <code>null</code> si se ha producido algun error.
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NoSuchColumnException 
	 */
	private Document buildDocument() throws DataErrorException, SQLException, NamingException, NoSuchColumnException {
		Element rootElement = new Element("datos");
		Document document = new Document(rootElement);
		boolean success = executeQuery();
		if (success) {
			createElements();
			linkElementsToRoot(rootElement);
			XMLOutputter xmlOutputter = new XMLOutputter("\t", true);
			StringWriter stringWriter = new StringWriter();
			try {
				xmlOutputter.output(document, stringWriter);
			} catch (IOException e) {
				e.printStackTrace();
			}
			StringBuffer buffer = stringWriter.getBuffer();
			//System.out.println(buffer);
		} else {
			document = null;
		}
		return document;
	}

	/**
	 * Ejecuta la consulta llamando a addObjectsFromClass por cada clase que hay
	 * en el mapa
	 * 
	 * @return
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NoSuchColumnException 
	 */
	
	private boolean executeQuery() throws DataErrorException, SQLException, NamingException, NoSuchColumnException {
		Map<Integer,Set<Integer>> tableIdsByIdto=new HashMap<Integer, Set<Integer>>();
		for (Integer idto : idObjects.keySet()) {
			Set<Integer> tableIdsSet = idObjects.get(idto);
			
			if (tableIdsSet.isEmpty()){
				// Si es nulo, la busqueda sera de todos los objetos de la clase.
				tableIdsByIdto.put(idto,null);
			}else{
				tableIdsByIdto.put(idto,tableIdsSet);
			}
		}
		
		//Hacemos la query agrupando por cada nivel aunque vengan de objetos diferentes para mejorar el rendimiento haciendo menos queries a base de datos
		try {
				int remainingLevels=levels;
				do{
					List<ObjectRepresentation> objectsFound=addObjectsFromClass(tableIdsByIdto, remainingLevels);
					
					tableIdsByIdto=new HashMap<Integer, Set<Integer>>();
					if(objectsFound!=null){
						Iterator<ObjectRepresentation> objectsIterator = objectsFound.iterator();
						while (objectsIterator.hasNext()) {
							ObjectRepresentation objectRepresentation = objectsIterator.next();
							System.out.println("Buscamos los objetos hijos de " + objectRepresentation.toString());
							
							Map<Integer, List<Integer>> referencedObjects = objectRepresentation.getReferencedObjectsByClass();
							for (Integer referencedIdto : referencedObjects.keySet()) {
								List<Integer> tableIds = referencedObjects.get(referencedIdto);
								if (remainingLevels>=0 && remainingLevels <= 1 && lastStructuralLevel) {
									tableIds = leaveOnlyStructuralProperties(referencedIdto, tableIds, objectRepresentation);
								}
								
								if(!tableIds.isEmpty() && remainingLevels>=0){
									Set<Integer> tableIdsList=tableIdsByIdto.get(referencedIdto);
									if(tableIdsList==null){
										tableIdsList=new HashSet<Integer>();
										tableIdsByIdto.put(referencedIdto, tableIdsList);
									}
									
									tableIdsList.addAll(tableIds);
									
								}
							}
						}
					}
					remainingLevels--;
				}while(!tableIdsByIdto.isEmpty());
			
		} catch (DataErrorException e) {
			e.printStackTrace();
			throw e;
		}

		return true;
	}

	/**
	 * Consulta en base de datos todos los elementos de una clase indicada. Este
	 * metodo es recursivo en el número restante de niveles.
	 * 
	 * @param idto
	 *            Identificador de la clase
	 * @param objectsOfClass
	 *            Lista de los identificadores de los objetos de la clase que
	 *            queremos obtener de base de datos.
	 * @param remainingLevels
	 *            número de niveles que quedan por consultar.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NoSuchColumnException 
	 */
	private List<ObjectRepresentation> addObjectsFromClass(Map<Integer,Set<Integer>> objectsOfClassByIdto, int remainingLevels) throws DataErrorException, SQLException, NamingException, NoSuchColumnException {
		if (remainingLevels <= 0 && (!lastStructuralLevel || (lastStructuralLevel && objectsOfClassByIdto.isEmpty()))) {
			// Si se llama a este metodo sin niveles restantes por analizar
			// significa que lo que se quiere es añadir los
			// elementos apuntados por ObjectProperties estructurales, con lo
			// cual no tiene sentido seguir procesando si
			// no hay que añadir las ObjectProperties estructurales del nivel 1.
			// Si hay que añadir el último nivel de estructurales, pero no nos
			// ha llegado ningun objeto que buscar, tambien salimos.
			return null;
		}
		// Lista de los tableIds no procesados aun y que haya que buscar en base
		// de datos.
		Map<Integer,Set<Integer>> tableIdsToSearchByIdto = null;
		if ( objectsOfClassByIdto != null){
			tableIdsToSearchByIdto = new HashMap<Integer,Set<Integer>>();
			for (Integer idto : objectsOfClassByIdto.keySet()) {
				if(objectsOfClassByIdto.get(idto)!=null){
					HashSet<Integer> list=new HashSet<Integer>();
					for(Integer tableIdSuggested:objectsOfClassByIdto.get(idto)){
						ObjectRepresentation existingObject = objectsByIdentifier.get(new ObjectIdentifier(idto, tableIdSuggested));
						if (existingObject == null) {
							list.add(tableIdSuggested);
						}
					}
					
					if(!list.isEmpty()){
						tableIdsToSearchByIdto.put(idto, list);
					}
					
				}else{
					tableIdsToSearchByIdto.put(idto, null);
				}
				
			}
		}
		List<ObjectRepresentation> objectsFound=new LinkedList<ObjectRepresentation>();
		if (/*tableIdsToSearchByIdto == null || */!tableIdsToSearchByIdto.isEmpty()){
			for(Integer idto:tableIdsToSearchByIdto.keySet()){
				// Obtenemos los datos de base de datos
				Set<Integer> tableIdsToSearch=tableIdsToSearchByIdto.get(idto);
				List<ObjectRepresentation> objectsFoundAux = searchObjectsOnDB(idto, tableIdsToSearch);
				for(ObjectRepresentation obj : objectsFoundAux){
					objectsByIdentifier.put(new ObjectIdentifier(obj.getIdto(), obj.getTableId()), obj);
				}
				objectsFound.addAll(objectsFoundAux);
			}
		}
		
		if (remainingLevels == 1 && !lastStructuralLevel) {
			System.out.println("Entramos por la condición de 'No quedan más niveles'");
			// No hay que bajar más porque no quedan más niveles que explorar.
			return null;
		}

		return objectsFound;

	}

	/**
	 * Comprueba que el elemento con el que se está trabajando en este momento
	 * no tenga como padre un nodo con el mismo idNode que el elemento dado.
	 * 
	 * @param parentElement
	 *            Elemento del que sacamos el idNode a consultar.
	 * @param childElement TODO
	 * @return <code>true</code> si hay un vinculo a cualquier nivel.
	 */
	private boolean hasParent(Element parentElement, Element childElement) {
		String parentNodeIdentifier = parentElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		String childNodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		if (parentNodeIdentifier == null || parentNodeIdentifier.isEmpty()){
			parentNodeIdentifier = parentElement.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		}
		if (childNodeIdentifier == null || childNodeIdentifier.isEmpty()){
			childNodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		}
		Integer parentIdNode = Integer.parseInt(parentNodeIdentifier);
		Integer childIdNode = Integer.parseInt(childNodeIdentifier);
		
		Set<Integer> parents = elementParents.get(childIdNode);
		
		return parents != null && parents.contains(parentIdNode) && ! parentIdNode.equals(childIdNode);
	}

	/**
	 * Consigue el conjunto de elementos padres del elemento dado.
	 * 
	 * @param element
	 *            Elemento del que se quieren saber sus padres.
	 * @return Conjunto de los padres del elemento dado.<br>
	 *         <b>Nunca</b> devuelve <code>null</code>. Si el elemento no tiene
	 *         padres, devuelve un conjunto vacio.
	 */
	private Set<Integer> getParents(Element element){
		String elementIdentifier = element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		if (elementIdentifier == null){
			elementIdentifier = element.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		}
		Integer idNode = Integer.parseInt(elementIdentifier);
		Set<Integer> parents = elementParents.get(idNode);
		return parents == null ? new HashSet<Integer>() : parents;
	}

	/**
	 * Consulta si el nodo actual tiene como hijo un nodo con la misma
	 * referencia del elemento dado.
	 * 
	 * @param childElement
	 *            Elemento del que sacamos la referencia para hacer la consulta.
	 * @param parentElement
	 *            Elemento padre del que queremos saber si tiene un hijo con la
	 *            referencia del hijo que queremos añadir.
	 * @return <code>true</code> si hay algun nodo hijo con la misma referencia
	 *         que el nodo dado.
	 */
	private boolean hasChild(Element childElement, Element parentElement){
		if (elementChildren == null){
			return false;
		}
		String nodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		if (nodeIdentifier == null || nodeIdentifier.isEmpty()){
			nodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		}
		Integer idNode = Integer.parseInt(nodeIdentifier);
		Set<Integer> children = elementChildren.get(parentElement); 
		return  children != null && children.contains(idNode);
	}

	/**
	 * añade la referencia del hijo al conjunto de hijos que se conocen del
	 * elemento actual.
	 * 
	 * @param childElement
	 *            Elemento del que se ha de objtener la referencia.
	 * @param parentElement
	 *            Elemento padre del que queremos saber si tiene como hijo
	 *            alguno con la misma referencia que el hijo dado.
	 */
	private void addChild(Element childElement, Element parentElement){
		String nodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		if (nodeIdentifier == null || nodeIdentifier.isEmpty()){
			nodeIdentifier = childElement.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		}
		Integer idNode = Integer.parseInt(nodeIdentifier);
		Set<Integer> childrenIdNodes = elementChildren.get(parentElement);
		if (childrenIdNodes == null){
			childrenIdNodes = new HashSet<Integer>();
			int parentIdNode = Integer.parseInt(parentElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE));
			childrenIdNodes.add(parentIdNode);
			elementChildren.put(parentElement, childrenIdNodes);
		}
		childrenIdNodes.add(idNode);
	}

	/**
	 * Consulta los datos especificados en la base de datos.
	 * 
	 * @param idto
	 *            Identificador de la clase de la que se quieren sacar los
	 *            datos.
	 * @param tableIdsToSearch
	 *            Lista de los tableId de los objetos que se quieren obtener de
	 *            base de datos
	 * @return Lista de la representación de los objetos obtenidos de la base de
	 *         datos.
	 * @throws NamingException
	 *             Si se produce algun error en la conexión con la base de datos
	 * @throws SQLException
	 *             Si se produce algun error en alguna consulta.
	 * @throws DataErrorException
	 *             Si hay errores en la construcción de las tablas.
	 * @throws NoSuchColumnException 
	 */
	private List<ObjectRepresentation> searchObjectsOnDB(Integer idto, Set<Integer> tableIdsToSearch) throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		GenerateSQL generateSQL = new GenerateSQL(factoryConnectionDB.getGestorDB());
		Map<Integer, ObjectRepresentation> objects = new Hashtable<Integer, ObjectRepresentation>();
		Table table = dataBaseMap.getTable(idto);
		if (table == null){
			throw new DataErrorException(DataErrorException.ERROR_DATA, "No existe una clase para el idto "+idto+"que guarde informacion del objeto solicitado.");
		}
		boolean isClassSearch = tableIdsToSearch == null;
		if (isClassSearch){
			tableIdsToSearch = new HashSet<Integer>();
		}
		getMainTableData(idto, tableIdsToSearch, generateSQL, objects, table, isClassSearch);
		for (Integer referencedTableId : table.getAllReferencedTables()) {
			Table referencedTable = dataBaseMap.getTable(referencedTableId);
			if (referencedTable.isAssociation()) {
				getAssociationTableData(tableIdsToSearch, generateSQL, objects,table, referencedTable);
			} else {
				getRangeTableData(idto, tableIdsToSearch, generateSQL, objects, referencedTable);
			}
		}
		return new ArrayList<ObjectRepresentation>(objects.values());
	}

	/**
	 * Extrae los tableId de la tabla apuntada cuando dicha tabla es la tabla
	 * que contiene los datos del objeto del rango
	 * 
	 * @param idto
	 *            Identificador del tipo de objeto del que surge la propiedad.
	 * @param tableIdsToSearch
	 *            Identificadores de los objetos dentro de la clase dominio de
	 *            los que queremos datos.
	 * @param generateSQL
	 *            Objeto que traduce elementos sql para adaptarse a cada sistema
	 *            gestor de base de datos.
	 * @param objects
	 *            Mapa de los que representan los datos que vamos obteniendo de
	 *            base de datos indexados por su tableId
	 * @param referencedTable
	 *            Tabla de la que tenemos que sacar los datos.
	 * @throws SQLException 
	 * @throws NamingException 
	 * @throws NoSuchColumnException 
	 */
	private void getRangeTableData(Integer idto, Set<Integer> tableIdsToSearch, GenerateSQL generateSQL, Map<Integer, ObjectRepresentation> objects, Table referencedTable) throws SQLException, NamingException, NoSuchColumnException {
		if (tableIdsToSearch.isEmpty()){
			return;
		}
		String beginCharacter = generateSQL.getCharacterBegin();
		String endCharacter = generateSQL.getCharacterEnd();
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		ClassInfo referencedClass = dataBaseMap.getClass(referencedTable.getId());
		// TESTING
		System.out.println("QueryService2.getRangeTableData(IDTO=" + idto + ", TABLE_IDS_TO_SEARCH=" + tableIdsToSearch + ", GENERATESQL, OBJECTS=" + objects + ", REFERENCEDTABLE=" + referencedTable.getName() + ")");
		// END_TESTING
		Set<Integer> properties = classInfo.getPropertiesReferencingClass(referencedTable.getId());
		// TESTING
		System.out.println("QueryService2.getRangeTableData() [Propiedades vinculando " + classInfo.getName() + " -> " + referencedClass.getName() + " = " + properties);
		// END_TESTING
		Iterator<Integer> parentClassIterator = referencedClass.getParentClasses() != null ? referencedClass.getParentClasses().iterator() : null;
		if (properties == null) {
			properties = new HashSet<Integer>();
		}
		while (parentClassIterator != null && parentClassIterator.hasNext()) {
			Integer parentClassIdto = parentClassIterator.next();
			Set<Integer> propertiesFound = classInfo.getPropertiesReferencingClass(parentClassIdto);
			if (propertiesFound != null){
				properties.addAll(propertiesFound);
			}
			// TESTING
			//ClassInfo parentClass = dataBaseMap.getClass(parentClassIdto);
			//System.out.println("QueryService2.getRangeTableData() [Propiedades vinculando " + classInfo.getName() + " -> " + parentClass.getName() + " = " + properties);
			// END_TESTING
		}
		for (Integer idProperty : properties) {
			List<String> columnNames = referencedTable.getColumnNamesContainingProperty(idProperty);
			String columnName = null;
			String sql = "SELECT " + beginCharacter + Table.COLUMN_NAME_TABLEID + endCharacter;
			if(columnNames==null) System.err.println("ERROR para SQL "+sql +" "+idProperty);
			for (String string : columnNames) {
				Integer columnDomain;
				columnDomain = referencedTable.getColumnDomain(string);
				if (columnDomain.equals(idto)) {
					columnName = string;
					break;
				}
			}
			if (columnName == null) {
				System.err.println("No se tiene informacion de " + classInfo.getName() + " en la tabla " + referencedTable.getName());
				continue;
			}
			sql += "," + beginCharacter + columnName + endCharacter + " FROM " + beginCharacter + referencedTable.getName() + endCharacter
					+ " WHERE " + beginCharacter + columnName + endCharacter + " IN (" + Auxiliar.setToString(tableIdsToSearch, ",") + 
					") ORDER BY " + beginCharacter + columnName + endCharacter + ";";
			List<List<String>> queryResult = DBQueries.executeQuery(factoryConnectionDB, sql);
			ObjectRepresentation object = null;
			for (List<String> objectData : queryResult) {
				String rangeTableId = objectData.get(0);
				String domainTableId = objectData.get(1);
				if (rangeTableId == null || domainTableId == null){
					continue;
				}
				if (object == null || ! domainTableId.equals(String.valueOf(object.getTableId()))){
					object = objects.get(Integer.parseInt(domainTableId));
				}
				object.addColumnValue(new ColumnValue(Constants.IDTO_INT, rangeTableId, dataBaseMap.isStructuralProperty(idProperty), idProperty, dataBaseMap.getPropertyName(idProperty), columnName, referencedTable.getId()),migration);
			}
		}
	}

	/**
	 * Consigue y añade los datos de las propiedades que se encuentran en la
	 * tabla asociacion.<br>
	 * Lo unico que se va a hacer es añadir que una propiedad (externalizada a
	 * una tabla asociacion) apunta a un objeto de tipo 'x' con tableId 'y'.
	 * 
	 * @param tableIdsToSearch
	 *            Identificadores de los objetos de los que tenemos que buscar
	 *            vinculos.<br>
	 *            Estos identificadores son de objetos de una <b>sola</b> clase.
	 * @param generateSQL
	 *            Objeto que nos permite obtener formato dependiente de la base
	 *            de datos.
	 * @param objects
	 *            Mapa de objetos donde estamos acumulando la informacion que
	 *            vamos obtenidendo de base de datos.<br>
	 *            <b>Todos son de la misma clase</b>.
	 * @param table
	 *            Tabla del dominio de la propiedad.
	 * @param assocTable
	 *            Tabla donde tenemos que buscar. Tiene que ser
	 *            <b>asociacion</<b>
	 * @throws SQLException
	 *             Si se produce un error en la consulta SQL
	 * @throws NamingException
	 *             Si se produce un error en la conexión con la base de datos.
	 * @throws DataErrorException Si hay errores en la construcción de las tablas.
	 */
	private void getAssociationTableData(Set<Integer> tableIdsToSearch, GenerateSQL generateSQL, Map<Integer, ObjectRepresentation> objects, Table table, Table assocTable) throws SQLException, NamingException, DataErrorException {
		if (tableIdsToSearch.isEmpty()){
			return;
		}
		String beginCharacter = generateSQL.getCharacterBegin();
		String endCharacter = generateSQL.getCharacterEnd();
		Integer rangeIdto = null;
		
		// Buscamos las columnas que contiene los datos dentro de la asociacion.
		TableColumn domainColumn = null;
		TableColumn rangeColumn = null;
		TableColumn propertyColumn = null;
		for (TableColumn column : assocTable.getAllColumns()) {
			Integer columnProperty = column.getIdProperty();
			if (columnProperty < 0){
				if (columnProperty.equals(IQueryInfo.ID_PROPERTY)){
					propertyColumn = column;
				}else if(columnProperty.equals(IQueryInfo.ID_DOMAIN)){
					domainColumn = column;
				}
			}else{
				rangeColumn = column;
				rangeIdto = assocTable.getColumnDomain(column.getColumnName());
			}
		}
		if (domainColumn == null || rangeColumn == null) {
			throw new DataErrorException("No se ha encontrado la columna del rango o del dominio en una tabla asociacion");
		}
		String sql = "SELECT " + beginCharacter + domainColumn.getColumnName() + endCharacter + ", " + beginCharacter + rangeColumn.getColumnName() + endCharacter;
		// Si la tabla asociacion tiene columna para las propiedades, tambien la
		// tenemos que coger para saber a que tenemos que asociar el dato que
		// obtengamos.
		if (propertyColumn != null){
			sql += ", " + beginCharacter + propertyColumn.getColumnName() + endCharacter;
		}
		sql += " FROM " + beginCharacter + assocTable.getName() + endCharacter + " WHERE " + beginCharacter + domainColumn + endCharacter + " IN (" + Auxiliar.setToString(tableIdsToSearch, ",") 
			+ ") ORDER BY " + beginCharacter + domainColumn + endCharacter;
		if (propertyColumn != null){
			sql += ", " + beginCharacter + propertyColumn + endCharacter;
		}
		sql += ";";
		
		// Hacemos la consulta
		List<List<String>> queryResult = DBQueries.executeQuery(factoryConnectionDB, sql);
		ObjectRepresentation object = null;
		for (List<String> objectData : queryResult) {
			Integer domainTableId = Integer.parseInt(objectData.get(0));
			String rangeTableId = objectData.get(1);
			if (rangeTableId == null){
				// El vinculo está incompleto y no nos sirve. El dominio no
				// puede estar vacio porque hemos filtrado por esa propiedad.
				continue;
			}
			Integer idProperty;
			// Si hemos cogido la propiedad de la tabla, tenemos que saber que
			// dato ha devuelto la consulta. Si no, el dato viene de la
			// propiedad que tiene almacenada la columna del rango
			if (propertyColumn != null){
				idProperty = Integer.parseInt(objectData.get(2));
			}else{
				idProperty = rangeColumn.getIdProperty();
			}
			if (object == null || object.getTableId() != domainTableId.intValue()){
				object = objects.get(domainTableId);
			}
			if (assocTable.isObjectProperty(idProperty)){
				object.addColumnValue(new ColumnValue(Constants.IDTO_INT, rangeTableId, dataBaseMap.isStructuralProperty(idProperty), idProperty, dataBaseMap.getPropertyName(idProperty), rangeColumn.getColumnName(), rangeIdto),migration);
			}else{
				// Se trata de una tabla de DataProperty externalizada, con lo cual el tratamiento cambia sensiblemente.
				object.addColumnValue(new ColumnValue(rangeColumn.getColumnDataType(), rangeTableId, dataBaseMap.isStructuralProperty(idProperty), idProperty, dataBaseMap.getPropertyName(idProperty), rangeColumn.getColumnName()),migration);
			}
		}
	}

	/**
	 * Extrae toda la informacion de la tabla cuyo idto se da como parámetro
	 * para los elementos indicados por la lista de tableIds e inicializa los
	 * objetos que representaran a los datos obtenidos de base de datos.
	 * 
	 * @param idto
	 *            Identificador de la tabla de la que se han de sacar los datos.
	 * @param tableIdsToSearch
	 *            Lista de los tableId de los objetos que se quiere consultar en
	 *            base de datos.
	 * @param generateSQL
	 *            Objeto que da funcionalidad para poder operar correctamente
	 *            con distintos gestores de base de datos.
	 * @param objects
	 *            Mapa de las representaciones de objetos que se sacan de base
	 *            de datos indexados por su tableId. Cuando se llama a este
	 *            metodo el mapa está vacio pues es este metodo el que se
	 *            encarga de rellenarlo de los primeros datos.
	 * @param table
	 *            Objeto que representa a la tabla de la que se han de sacar los
	 *            datos.
	 * @param isClassSearch
	 *            Indica si lo que se quiere es realizar una busqueda de todos
	 *            los objetos de una clase.
	 * @throws SQLException
	 * @throws NamingException
	 */
	private void getMainTableData(Integer idto, Set<Integer> tableIdsToSearch, GenerateSQL generateSQL, Map<Integer, ObjectRepresentation> objects, Table table, boolean isClassSearch) throws SQLException, NamingException {
		List<TableColumn> columns = new ArrayList<TableColumn>(table.getAllColumns());
		String sql = constructMainTableSQL(tableIdsToSearch, table, columns, isClassSearch, generateSQL);
		// TESTING
		//System.out.println("QueryService2.getMainTableData() [Cosulta: " + sql + "]");
		// END_TESTING
		// Realizamos la consulta
		List<List<String>> queryResult = DBQueries.executeQuery(factoryConnectionDB, sql);
		for (List<String> objectData : queryResult) {
			// Vamos a crear el objeto ficticio que representara los datos que sacamos de BD
			Iterator<TableColumn> columnIterator = columns.iterator();
			ObjectRepresentation objectRepresentation = new ObjectRepresentation(idto);
			for (String value : objectData) {
				TableColumn column = columnIterator.next();
				if (value == null){
					// Los datos nulos no se van a representar en el XML con lo
					// cual no nos interesan.
					continue;
				}
				
				Integer idProperty = column.getIdProperty();
				ColumnValue columnValue = null;
				if (idProperty.intValue() < 0){
					// Las propiedades ficticias se guardan en esta representación
					// de objetos como si no tuvieran propiedad porque, en realidad,
					// no la tienen
					columnValue = new ColumnValue(column.getColumnDataType(), value, column.getColumnName());
				}else{
					String propertyName = dataBaseMap.getPropertyName(idProperty);
					boolean structuralProperty = dataBaseMap.isStructuralProperty(idProperty);
					if (table.isObjectProperty(idProperty)){
						Integer referencedTable = table.getColumnDomain(column.getColumnName());
						Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
						if (structuralProperty && table.isForeignProperty(idProperty)) {
							if (inverseProperty != null){
								propertyName = dataBaseMap.getPropertyName(inverseProperty);
								structuralProperty = dataBaseMap.isStructuralProperty(inverseProperty);
								columnValue = new ColumnValue(column.getColumnDataType(), value, structuralProperty, inverseProperty, propertyName, column.getColumnName(), referencedTable);
							}
						}else{
							columnValue = new ColumnValue(column.getColumnDataType(), value, structuralProperty, idProperty, propertyName, column.getColumnName(), referencedTable);
						}
					}else{
						if(!migration && propertyName.equals(Constants.PROP_PASSWORD)){//Si es la propiedad password no ponemos los caracteres encriptados obtenidos desde base de datos ya que da problemas luego en el formato xml para replica_msg si editamos el password. Ademas ese password encriptado no sirve para nada.
							columnValue = new ColumnValue(column.getColumnDataType(), "encrypt_password", structuralProperty, idProperty, propertyName, column.getColumnName());
						}else{
							columnValue = new ColumnValue(column.getColumnDataType(), value, structuralProperty, idProperty, propertyName, column.getColumnName());
						}
					}
				}
				if (columnValue != null){
					objectRepresentation.addColumnValue(columnValue,migration);
				}
			}
			if (isClassSearch){
				// Necesitamos los tableIds obtenidos en la busqueda de la tabla
				// principal para poder ejecutar las busquedas en las tablas
				// apuntadas.
				tableIdsToSearch.add(objectRepresentation.getTableId());
			}
			objects.put(objectRepresentation.getTableId(), objectRepresentation);
		}
	}

	/**
	 * Constuye la sentencia SQL que va a sacar todos los datos de una tabla.
	 * 
	 * @param tableIdsToSearch
	 *            Indica si tenemos que restringir la busqueda a ciertos IDTOS
	 * @param table
	 *            Tabla de la que tenemos que sacar los datos
	 * @param columns
	 *            Columnas que queremos consultar, en el orden que tenemos que
	 *            sacarlos
	 * @param isClassSearch
	 *            Indica si se trata de una busqueda de todos los elementos de
	 *            una clase.
	 * @param generateSQL TODO
	 * @return Cadena que contiene la consulta necesaria para obtener los datos
	 *         solicitados.
	 */
	private String constructMainTableSQL(Set<Integer> tableIdsToSearch, Table table, List<TableColumn> columns, boolean isClassSearch, GenerateSQL generateSQL) {
		String sql = "SELECT ";
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		boolean first = true;
		for (TableColumn tableColumn : columns) {
			if (!first) {
				sql += ", ";
			}
			String column = cB + tableColumn.getColumnName() + cE;
			if (migration && tableColumn.getIdProperty()==Constants.IdPROP_PASSWORD) {
				sql += generateSQL.getDecryptFunction(InstanceService.keyEncrypt, column);
			}else{
				sql += column;
			}
			first = false;
		}
		sql += " FROM " + cB + table.getName() + cE;
		
		// Si tableIdsToSearch está vacio, significa que queremos todos los datos de la clase.
		if (! isClassSearch){
			sql += " WHERE " + cB + Table.COLUMN_NAME_TABLEID + cE + " IN (" + Auxiliar.setToString(tableIdsToSearch, ",") +")";
		}
		sql += ";";
		return sql;
	}
	
	private void createElements(){
		List<ObjectRepresentation> values = new LinkedList<ObjectRepresentation>(objectsByIdentifier.values());
		Collections.sort(values, new Comparator<ObjectRepresentation>() {

			/**
			 * Note: this comparator imposes orderings that are inconsistent
			 * with equals.
			 * 
			 * @param oR1
			 * @param oR2
			 * @return Negativo, cero o positivo si or1 es menor, igual o mayor
			 *         que or2 respectivamente.
			 */
			@Override
			public int compare(ObjectRepresentation oR1, ObjectRepresentation oR2) {
				Set<Integer> idtos = idObjects.keySet();
				if (idtos.contains(oR1.getIdto()) && !idtos.contains(oR2.getIdto())){
					return -1;
				}else if(!idtos.contains(oR1.getIdto()) && idtos.contains(oR2.getIdto())){
					return 1;
				}else{
					return 0;
				}
			}
		});
		for (ObjectRepresentation object : values) {
			convertToElement(object);
		}
	}

	/**
	 * Dada una representación de un objeto obtenido de base de datos, construye
	 * el elemento que contiene la misma informacion.
	 * 
	 * @param objectRepresentation
	 *            Representación del objeto que queremos esquematizar con un
	 *            elemento de XML
	 * @param currentParentElement
	 *            Elemento del que ha surgido la consulta para enganchar este
	 *            nodo que se está creando. Puede ser <code>null</code> si no
	 *            hay padre
	 * @return Elemento XML que contiene la informacion del objeto
	 *         esquematizada.
	 */
	private Element convertToElement(ObjectRepresentation objectRepresentation) {
		int idto = objectRepresentation.getIdto();
		int tableId = objectRepresentation.getTableId();
		Integer idNode = idNodesByIdentifier.get(new ObjectIdentifier(idto, tableId));
		Element element = null;
		if (idNode == null) {
			element = constructNewElement(idto, tableId);
			idNode = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE));
			elementsWithoutParent.add(element);
		}else{
			element = elementsByIdNode.get(idNode);
			if (! isEmptyNode(idNode)){
				String elementName = element.getName();
				Element linkElement = new Element(elementName);
				if(xmlActionType!=null) linkElement.setAttribute(XMLConstants.ATTRIBUTE_ACTION,xmlActionType);
				linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, idNode.toString()));
				return linkElement;
			}
		}
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		element.removeChildren();
		for (ColumnValue columnValue : objectRepresentation.getObjectData()) {
			switch (columnValue.getPropertyType()) {
			case ColumnValue.DATA_PROPERTY:
				if (columnValue.getDataType() == Constants.IDTO_MEMO){
					Element memoElement = new Element(XMLConstants.TAG_MEMO);
					if(xmlActionType!=null) memoElement.setAttribute(XMLConstants.ATTRIBUTE_ACTION,xmlActionType);
					memoElement.setAttribute(XMLConstants.ATTRIBUTE_PROPERTYm, columnValue.getPropertyName());
					Element valueElement = new Element(XMLConstants.TAG_VALUE);
					valueElement.addContent(new CDATA(columnValue.getValue()));
					memoElement.addContent(valueElement);
					element.addContent(memoElement);
				}else{
					PropertyInfo property = classInfo.getProperty(columnValue.getIdProperty());
					if (property.getMaxCardinality() == 1){
						element.setAttribute(new Attribute(columnValue.getPropertyName(), columnValue.getValue()));
					}else{
						Element dataPropertyElement = new Element(XMLConstants.TAG_DATA_PROPERTY);
						dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, columnValue.getPropertyName()));
						dataPropertyElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_VALUE, columnValue.getValue()));
						element.addContent(dataPropertyElement);
					}
				}
				break;
			case ColumnValue.OBJECT_PROPERTY:
				int tableIdReferenced = Integer.parseInt(columnValue.getValue());
				int idtoReferenced = columnValue.getReferencedClassIdto();
				Integer idNodeReferenced = idNodesByIdentifier.get(new ObjectIdentifier(idtoReferenced, tableIdReferenced));
				Element linkElement;
				if (idNodeReferenced == null){
					// No hay ningun elemento para representar el objeto apuntado todavia.
					linkElement = constructNewElement(idtoReferenced, tableIdReferenced);
					Set<Integer> linkElementParents = new HashSet<Integer>(getParents(element));
					idNodeReferenced = Integer.parseInt(linkElement.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE));
					linkElementParents.add(idNode);
					elementParents.put(idNodeReferenced, linkElementParents);
				}else{
					// Ya existe un elemento para referenciar al objeto apuntado.
					Element existingElement = elementsByIdNode.get(idNodeReferenced);
					int indexOfElement = elementsWithoutParent.indexOf(existingElement);
					if (! hasParent(existingElement, element) && indexOfElement != -1) {
						// Si el elemento existente no es padre del que se está creando y el elemento existente no tiene padre
						linkElement = processRefNode(element, existingElement);
//						linkElement = existingElement;
						Set<Integer> linkElementParents = new HashSet<Integer>();
						linkElementParents.add(idNode);
						elementsWithoutParent.remove(indexOfElement);
						elementParents.put(idNodeReferenced, linkElementParents);
					} else {
						String elementName = existingElement.getName();
						linkElement = new Element(elementName);
						linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, idNodeReferenced.toString()));
						if(xmlActionType!=null) linkElement.setAttribute(XMLConstants.ATTRIBUTE_ACTION,xmlActionType);
					}
				}
				element.addContent(linkElement);
				linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, columnValue.getPropertyName()));
				break;
			case ColumnValue.NO_PROPERTY:
				if (columnValue.getColumnName().equals(IQueryInfo.COLUMN_NAME_TABLEID)){
					int tableIdCol=Integer.parseInt(columnValue.getValue());
					//if(xmlActionType.equals(XMLConstants.ACTION_NEW)) tableIdCol=-Math.abs(tableIdCol);
					
					element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_TABLEID, ""+tableIdCol));
					
				}else if (columnValue.getColumnName().equals(IQueryInfo.COLUMN_NAME_DESTINATION)){
					element.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_DESTINATIONm, columnValue.getValue()));
				}
				break;
			default:
				System.err.println("El tipo de la propiedad para el " + columnValue + " no está contemplado.");
				break;
			}
		}
		return element;
	}
	
	/**
	 * metodo en el que si element está contenido en existingElement 
	 * lo sustituye por un nodo referencia, eliminando sus hijos.
	 * Para este nodo y sus hijos hay que actualizar 
	 * el mapa elementParents y la lista elementsWithoutParent
	 * ya que se ha desenganchado de su padre.
	 */
	private Element processRefNode(Element element, Element existingElement) {
		String val = element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
		Element find = jdomParser.findElementByAt(existingElement, XMLConstants.ATTRIBUTE_IDNODE, val, true);
		if (find!=null) {
			String elementName = find.getName();
			String property = find.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
			
			Element parentFind = find.getParent();
			parentFind.removeChild(elementName);
			
			Element newNode = new Element(elementName);
			if(xmlActionType!=null) newNode.setAttribute(XMLConstants.ATTRIBUTE_ACTION,xmlActionType);
			newNode.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_REFNODE, val));
			newNode.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, property));
			parentFind.addContent(newNode);
			
			HashSet<Integer> parentsRemove = new HashSet<Integer>();
			String idNodeRootStr = element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
			Integer idNodeRoot = Integer.parseInt(idNodeRootStr);
			processParentsOfElement(element, idNodeRoot, parentFind, parentsRemove);
			processChilds(parentFind, parentsRemove);
		}
		return existingElement;
	}
	/** 
	 * metodo que actualiza el mapa elementParents y la lista elementsWithoutParent 
	 * para los hijos del nodo desenganchado.
	 */
	private void processChilds(Element parent, HashSet<Integer> parentsRemove) {
		Iterator it = parent.getChildren().iterator();
		while (it.hasNext()) {
			Element elem = (Element)it.next();
			String idNodeStr = elem.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
			if (idNodeStr!=null) {
				Integer idNode = Integer.parseInt(idNodeStr);
				Set<Integer> parents = elementParents.get(idNode);
				parents.removeAll(parentsRemove);
			}
			processChilds(elem, parentsRemove);
		}
	}
	/** 
	 * metodo que actualiza el mapa elementParents y la lista elementsWithoutParent 
	 * para el nodo desenganchado.
	 */
	private void processParentsOfElement(Element element, Integer idNodeRootOfElement, Element parentFind, HashSet<Integer> parentsRemove) {
		Set<Integer> parents = elementParents.get(idNodeRootOfElement);
		if (parents!=null) {
			String idNodeParentFindStr = parentFind.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
			if (idNodeParentFindStr!=null) {
				int idNodeParentFind = Integer.parseInt(idNodeParentFindStr);
				parents.remove(idNodeParentFind); //lo desenganchamos del padre
				parentsRemove.add(idNodeParentFind); //aqui almacenamos todos los padres que quitamos
				if (parents.size()==0) { //si no tiene padres
					elementParents.remove(idNodeRootOfElement);
					elementsWithoutParent.add(element);
				} else  {
					//iterar por los padres y eliminar los de esta rama
					if (parentFind.getParent()!=null) {
						processParentsOfElement(element, idNodeRootOfElement, parentFind.getParent(), parentsRemove);
					}
				}
			}
		}
	}

	/**
	 * Crea un nuevo nodo de datos para representar a un objeto de base de
	 * datos.<br>
	 * tambien inserta la informacion necesaria en los distintos mapas para
	 * rastrear datos acerca de este tipo de elementos.
	 * 
	 * @param idto
	 *            Identificador de la clase del objeto que se está
	 *            representando.
	 * @param tableId
	 *            Identificador del objeto.
	 * @return Elemento con la informacion basica que debe tener un nodo de
	 *         datos.
	 */
	private Element constructNewElement(int idto, int tableId) {
		ClassInfo referencedClass = dataBaseMap.getClass(idto);
		Element linkElement = new Element(referencedClass.getName());
		if(xmlActionType!=null){
			linkElement.setAttribute(XMLConstants.ATTRIBUTE_ACTION,xmlActionType);
			//if(xmlActionType.equals(XMLConstants.ACTION_NEW)) tableId=-Math.abs(tableId);
		}
		
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_TABLEID, String.valueOf(tableId)));
		int idNode = getNextIdNode();
		linkElement.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_IDNODE, String.valueOf(idNode)));
		elementsByIdNode.put(idNode, linkElement);
		idNodesByIdentifier.put(new ObjectIdentifier(idto, tableId), idNode);
		elementIdto.put(linkElement, idto);
		return linkElement;
		
	}

	/**
	 * Obtiene una lista de tableIds que solo contiene identificadores de de
	 * aquellos objetos apuntados por una propiedad estructural.
	 * 
	 * @param referencedIdto
	 *            Identificador de la clase a la que pertenecen todos los
	 *            tableIds que se van a tratar.
	 * @param tableIds
	 *            Lista de los identificadores de objetos que se han de
	 *            analizar.
	 * @param objectRepresentation
	 *            Representación del objeto en base de datos del que salen los
	 *            vinculos con los tableIds a analizar.
	 * @return Lista de los identificadores de los objetos que son apuntados por
	 *         una propiedad estructural desde el objeto
	 *         <i>objectRepresentation</i>.
	 */
	@SuppressWarnings("unchecked")
	private List<Integer> leaveOnlyStructuralProperties(Integer referencedIdto, List<Integer> tableIds, ObjectRepresentation objectRepresentation) {
		List<Integer> clonedList = (List<Integer>) ((ArrayList<Integer>) tableIds).clone();
		for (Integer tableId : tableIds) {
			if (!objectRepresentation.isStructural(referencedIdto, tableId)) {
				clonedList.remove(tableId);
			}
		}
		return clonedList;
	}

	/**
	 * Dado un elemento en el que se han incluido todos los hijos que
	 * representan las ObjectProperties que tiene el objeto, recorre dichos
	 * hijos para ir añadir el atributo <i>property</i> que indica por que
	 * propiedad está apuntado el objeto.
	 * 
	 * @param element
	 *            Element padre del que cuelgan los hijos a los que hay que
	 *            añadirles el atributo property.
	 * @param objectRepresentation
	 *            Objeto al que representa el elemento dado.
	 */
	@SuppressWarnings("unchecked")
	private void addPropertiesToChildren(Element element, ObjectRepresentation objectRepresentation) {
		List<Element> children = new ArrayList<Element>(element.getChildren());
		for (Element child : children) {
			Attribute nodeIdentifierAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
			Attribute tableIdAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
			Integer idto;
			if (nodeIdentifierAttribute == null) {
				// Si es un elemento hijo sin idto se puede tratar de dos casos
				// muy distintos: nodo vinculo o nodo MEMO.
				nodeIdentifierAttribute = child.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
				if (nodeIdentifierAttribute == null) {
					// Se puede tratar, por ejemplo, de un nodo MEMO al que se
					// le añadio la propiedad cuando se creo.
					continue;
				}
				// Si llegamos aque, se trata de un nodo vinculo y tenemos que
				// buscar el elemento que contiene la
				// informacion para coger el idto y el tableId.
				Integer idNode;
				try {
					idNode = nodeIdentifierAttribute.getIntValue();
				} catch (DataConversionException e) {
					System.err.println("Error al intentar obtener el valor del idNode de un atributo idNode porque no es un número entero.");
					e.printStackTrace();
					continue;
				}
				Element realElement = elementsByIdNode.get(idNode);
				idto = elementIdto.get(realElement);
				tableIdAttribute = realElement.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
			} else {
				// Es un nodo de datos, con lo cual podemos consultar
				// directamente en el mapa el idto de este objeto.
				idto = elementIdto.get(child);
			}
			// Sacamos los valores de los atributos.
			Integer tableId;
			try {
				tableId = tableIdAttribute.getIntValue();
			} catch (DataConversionException e) {
				System.err.println("Error al intentar parsear idto y tableId del XML porque no tienen el formato de datos adecuado");
				e.printStackTrace();
				continue;
			}
			ColumnValue columnValue = objectRepresentation.getColumnValue(idto, tableId);
			if (columnValue == null) {
				System.err.println("No se ha encontrado la representación del valor para la identificación idto=" + idto + " tableId=" + tableId);
				continue;
			}
			String propertyName = columnValue.getPropertyName();
			child.setAttribute(new Attribute(XMLConstants.ATTRIBUTE_PROPERTYm, propertyName));
		}

	}

	/**
	 * Cada vez que se busca un objeto en base de datos y se transforma a
	 * Element, las ObjectProperty se representan con un nodo que solo tiene
	 * <i>idNode</i>, <i>idto</i> y <i>tableId</i> (puede tener tambien un
	 * atributo <i>property</i>). Si posteriormente se descienden más niveles,
	 * nos va a decir que ya existe un nodo con ese idto y tableId, pero
	 * igualmente hay que buscarlo en base de datos porque este nodo no tiene
	 * toda la informacion del objeto.
	 * 
	 * @param idNode
	 *            Identificador del nodo que se quire buscar.
	 * @return devolvera <code>true</code> si el nodo tiene solo los tres
	 *         atributos indicados y solo esos o si no existe el nodo. En resto
	 *         de los casos devuelve <code>false</code>.
	 */
	@SuppressWarnings("unchecked")
	private boolean isEmptyNode(int idNode) {
		Element element = elementsByIdNode.get(idNode);
		if (element == null) {
			return true;
		}
		List<Attribute> attributes = new ArrayList<Attribute>(element.getAttributes());
		for (Attribute attribute : attributes) {
			String attributeName = attribute.getName();
			if (attributeName.equals(XMLConstants.ATTRIBUTE_IDNODE) || attributeName.equals(XMLConstants.ATTRIBUTE_IDTOm) || attributeName.equals(XMLConstants.ATTRIBUTE_TABLEID)
					|| attributeName.equals(XMLConstants.ATTRIBUTE_PROPERTYm)) {
				continue;
			}
			return false;
		}
		return true;
	}

	/**
	 * Se encarga de coger la lista de elementos que no tienen padre y añadirlo
	 * como contenido del rootElement del documento que se está construyendo.
	 * 
	 * @param rootElement
	 *            Elemento raiz del documento que se está construyendo y al que
	 *            se le van a añadir como contenido el resto de nodos.
	 */
	private void linkElementsToRoot(Element rootElement) {
		Element objectsElement = new Element(XMLConstants.TAG_OBJECTS);
		
		// Añadimos los elementos de datos.
		for (Element element : elementsWithoutParent) {
			objectsElement.addContent(element);
		}
		
		rootElement.addContent(objectsElement);
	}

	/**
	 * Devuelve el valor del idNode que se tiene que usar para el siguiente
	 * elemento que se quiera crear e incrementa el valor del atributo
	 * <code>lastIdNodeUsed</code>.
	 * 
	 * @return número que se tiene que usar como identificador del siguiente
	 *         elemento que se vaya a crear.
	 */
	private int getNextIdNode() {
		lastIdNodeUsed++;
		return lastIdNodeUsed;
	}

	
}
