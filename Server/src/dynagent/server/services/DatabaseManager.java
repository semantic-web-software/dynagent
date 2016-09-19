package dynagent.server.services;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.Changes;
import dynagent.common.communication.ObjectChanged;
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
import dynagent.common.properties.values.StringValue;
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
import dynagent.server.dbmap.RelationalDataBaseException;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.replication.IReplication;
import dynagent.server.services.querys.AuxiliarQuery;

/**
 * En esta clase se va a desarrollar toda la logica de trabajo con la base de
 * datos, de tal manera que toda inserción, borrado o actualización tendra que
 * pasar por esta clase para poder trabajar.
 */
public class DatabaseManager {

	/** Mapeo del modelo y de la base de datos */
	private DataBaseMap dataBaseMap;
	/** Objeto que nos permite acceder a la base de datos */
	private FactoryConnectionDB fcdb;
	/** */
	private InstanceService instanceService;
	/**
	 * Objeto que contiene la informacion sobre la replica. Puede ser
	 * <code>null</code> si no se hace réplica.
	 */
	private IReplication iReplication;
	/** Almacena todos los cambios que se han hecho sobre los datos de entrada */
	private Changes changes;
	/**
	 * Conjunto de los idNodes que ya han sido procesados, es decir, si habia
	 * que crearlos se han creado, si habia que borrarlos se han borrado, etc.
	 */
	private Set<Integer> processedIdNodes;
	/**
	 * Mapa de todos los elementos que contienen informacion indexados por su
	 * idNode
	 */
	private Map<Integer, Element> elementsByIdNode;
	
	/** Conjunto de los elementos que tienen una property que apunta a MiEmpresa */
	private Set<Integer> nodesWithMiEmpresa;
	/**
	 * Mapa de los elementos que tienen una empresa asociada por una property. <br>
	 * <table border>
	 * <theader>
	 * <th>Key</th>
	 * <th>Value</th>
	 * </theader> <tbody>
	 * <tr>
	 * <td><b>id_node</b> del objeto que tiene la property que apunta a
	 * MiEmpresa</td>
	 * <td><b>id_node</b> del elemento que contiene la informacion de MiEmpresa</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 */
	private Map<Integer, Integer> idNodeMiEmpresaMap;
	/**
	 * Conjunto de los idtos sobre los que no se pueden hacer modificaciones.
	 * Puede ser <code>null</code> si no hay réplica.
	 */
	private Set<Integer> idtosNotModifiables;
	/** Identificador para la transacción de la réplica */
	private int idReplica;
	/**
	 * Usuario con el que se está conectado en el cliente y que ha generado los
	 * datos sobre los que se está trabajando.
	 */
	private String user;
	/**
	 * Objeto que permite traducir ciertas cosas segun el gestor de base de
	 * datos que se está usando.
	 */
	private GenerateSQL generateSQL;
	/** Patron para reconocer índices temporales. */
	private Pattern rdnTemporalPattern = Pattern.compile(Constants.DEFAULT_RDN_CHAR + "(id)?" + "(\\-?)[0-9]+" + "(@[0-9]+)?" + Constants.DEFAULT_RDN_CHAR);
	
	private String rdnTemporalDelegation = Constants.DEFAULT_RDN_CHAR +"delegation"+Constants.DEFAULT_RDN_CHAR;;
	/**
	 * Clase que ofrece la funcionalidad asociada a la consulta y trabajo con
	 * índices.
	 */
	private IndexFilterFunctions2 indexFilterFunctions;
	/**
	 * Mapa de los objetos que almacenan la informacion de los cambios indexados
	 * por su id_node.
	 */
	private Map<String, ObjectChanged> changesByIdNode;
	private ArrayList<ObjectChanged> changesNoRdn;
	/**
	 * Conjunto de los índices que han sido usados y de los que se debe propagar
	 * el cambio a base de datos.
	 */
	private Set<IndexFilter2> usedIndexes;
	
	/**
	 * Mapa con clave el indexId y con contenido el conjunto de rdns de individuos eliminados que usan ese índice.
	 */
	private Map<Integer,Set<String>> indexDeleted;
	/** Lista de los rdn decrementados organizados por idto */
	private Map<Integer, Set<String>> rdnDecrementedByIdto;
	
	private Set<Integer> idtosDecremented;

	/** Conjunto de los elementos que se han de modificar tras el borrado para asignarles el rdn */
	private Set<Element> rdnModifyLast;
	/**
	 * Mapa de los emparejamientos idNode-tableId organizados por la clase a la
	 * que pertenece cada objeto.
	 */
	//hacer mapa de tablesId creados, old>new y usarlo cuando hay ref node y cambiaa set
	private Map<Integer, Map<Integer, Integer>> tableIdToIdNodeByIdto;
	private HashMap<String, Integer> newTableIdMap;//key idto#newRdn, value =new TableId
	private HashMap<String, String> newRdnMap;//key idto#oldRdn, value =newRdn
	/** Mapa de los incrementales ordenados por su idNode */
	
	private Map<Integer, IncrementalData> incrementalDataByIdNode;	
	/**
	 * Mapa de ido negativo-id node, util para creación de rdns y dataProperties a partir de rdns de otros individuos.
	 */
	private Map<Integer,Integer> idoNegativeIdNode;
	/**
	 * Conjunto de id node, util para creación de rdns y dataProperties a partir de idos de otros individuos que aun no se han insertado, por lo que no se sabe su tableId. 
	 */
	private Map<Integer,Map<String, String>> fieldsPendingResolution;
	/** Conjunto de los identificadores de los objetos marcados para ser borrados. */
	private Set<ObjectIdentifier> objectsToDelete;
	/** UserTask de la que surgio la comunicación con la base de datos. */
	private Integer uTask;
	/**
	 * Indica si se está ejecutando el servidor en modo debug para hacer más
	 * comprobaciones sobre los datos de entrada.
	 */
	public static boolean debugMode;
	
	private boolean keepTableIds;

	private boolean migration;
	private boolean replication;//Indica si los datos vienen desde una replica
	
	public DatabaseManager(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, InstanceService instanceService, IReplication iReplication, String user, boolean debugMode) {
		if (fcdb == null || dataBaseMap == null) {
			System.err.println("[DatabaseManager] Error al inicializar la clase alguno de los parámetros es nulo.");
		}
		this.fcdb = fcdb;
		this.dataBaseMap = dataBaseMap;
		this.instanceService = instanceService;
		this.iReplication = iReplication;
		this.user = user;
		this.generateSQL = new GenerateSQL(this.fcdb.getGestorDB());
		DatabaseManager.debugMode = debugMode;
		this.indexFilterFunctions = new IndexFilterFunctions2(fcdb, dataBaseMap);
		new HashSet<Integer>();
		new Hashtable<Integer, List<IndexFilter2>>();
		this.objectsToDelete = new HashSet<ObjectIdentifier>();
		this.uTask = null;
		this.rdnDecrementedByIdto = new Hashtable<Integer, Set<String>>();
		this.idtosDecremented = new HashSet<Integer>();
		this.rdnModifyLast = new HashSet<Element>();
		this.replication = false;
	}

	public void setKeepTableIds(boolean keepTableIds) {
		this.keepTableIds = keepTableIds;
	}

	public void setMigration(boolean migration) {
		this.migration = migration;
	}
	
	/**
	 * Realiza las operaciones expresadas en el XML dado, es el unico punto de
	 * entrada posible de esta clase para hacer operaciones con la base de
	 * datos.
	 * 
	 * @param dataDocument
	 *            XML con los datos y las operaciones a realizar. Si es nulo o
	 *            está mal formado no se llevara a cabo ninguna operación.
	 * @return Objeto que contiene toda la informacion sobre los cambios que se
	 *         han realizado sobre los datos pasados para adecuarlos a la logica
	 *         de la aplicación.
	 * @throws DataErrorException
	 *             Si los datos pasados están mal formados.
	 * @throws NamingException
	 * @throws SQLException
	 *             Si se produce un error en la ejecucion de una sentencia SQL
	 * @throws OperationNotPermitedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CardinalityExceedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncompatibleValueException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncoherenceInMotorException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws NotFoundException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws ApplicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CommunicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws RemoteSystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws SystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws InstanceLockedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws NoSuchColumnException
	 * @throws JDOMException 
	 */
	@SuppressWarnings("unchecked")
	public Changes execute(Document dataDocument) throws DataErrorException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, NoSuchColumnException {
		// Antes de empezar a procesar los datos hacemos unas mínimas
		// comprobaciones sobre el parámetro dado.
		if (dataDocument == null) {
			throw new DataErrorException("[DatabaseManager] Se ha intentado ejecutar una operación con un XML nulo.");
		}
		Element rootElement = dataDocument.getRootElement();
		if (rootElement == null || !rootElement.getName().equals("datos")) {
			throw new DataErrorException("[DatabaseManager] El XML que contiene los datos está mal formado.");
		}
		if (iReplication != null) {
			// Se hace réplica, por lo que hay que ver que idtos no son
			// modificables y generar un identificador para el
			// bloque de modificaciones que impolica el XML dado.
			idtosNotModifiables = iReplication.getNoModifyDB();
			idReplica = generateIdReplica();
		}
		Map<Integer, Set<Integer>> lockedObjects = new Hashtable<Integer, Set<Integer>>();
		preprocessXML(dataDocument);
		processedIdNodes = new HashSet<Integer>();
		usedIndexes = new HashSet<IndexFilter2>();
		
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		
		List<Element> children = new LinkedList<Element>(objectsElement.getChildren());
		for (Element child : children) {
			processChild(objectsElement,child, lockedObjects, null);
		}
		postProcess();
		return changes;
	}

	/**
	 * Lee el XML una vez para coger todos los nodos que tienen la declaración
	 * completa y asociarlos en un mapa con su id_node.
	 * 
	 * @param dataDocument
	 *            XML a analizar.
	 * @throws DataErrorException
	 *             Si hay errores en el formato de los elementos.
	 * @throws OperationNotPermitedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CardinalityExceedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncompatibleValueException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncoherenceInMotorException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws NotFoundException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws ApplicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CommunicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws RemoteSystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws SystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws InstanceLockedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 * @throws JDOMException 
	 */
	@SuppressWarnings("unchecked")
	private void preprocessXML(Document dataDocument) throws DataErrorException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, NoSuchColumnException {
		if (dataDocument == null || dataDocument.getRootElement() == null) {
			throw new DataErrorException("El documento XML no está bien formado pues es nulo o no tiene elemento raiz.");
		}

		FactsAdapter.addIdtosToDataNodes(dataBaseMap, dataDocument);
		nodesWithMiEmpresa = new HashSet<Integer>();
		idNodeMiEmpresaMap = new Hashtable<Integer, Integer>();
		tableIdToIdNodeByIdto = new Hashtable<Integer, Map<Integer, Integer>>();
		newTableIdMap =new HashMap<String,Integer>();
		newRdnMap=new HashMap<String,String>();
		idoNegativeIdNode = new HashMap<Integer, Integer>();
		changesByIdNode = new Hashtable<String, ObjectChanged>();
		changesNoRdn = new ArrayList<ObjectChanged>();
		incrementalDataByIdNode = new Hashtable<Integer, IncrementalData>();
		Set<Integer> idtosUsed = new HashSet<Integer>();
		Map<Integer, Element> result = new Hashtable<Integer, Element>();
		Element rootElement = dataDocument.getRootElement();
		Element objectsElement = rootElement.getChild(XMLConstants.TAG_OBJECTS);
		List<Element> elements = new LinkedList<Element>();
		elements.addAll(objectsElement.getChildren());
		for (Element element : elements) {
			preprocessXMLChild(objectsElement,element, result, idtosUsed);
		}
		elementsByIdNode = result;
		fieldsPendingResolution = new HashMap<Integer,Map<String, String>>();
		generateAllRdn(idtosUsed,objectsElement);
	}

	/**
	 * Procesa el elemento pasado y, si cumple las condiciones, lo añade al mapa
	 * de elemntos indexados por su id_node. tambien se encarga de mapear los
	 * nodos que tienen una referencia a mi empresa porque los índices necesitan
	 * saber a que empresa pertenece un objeto. <br>
	 * Además, llama recursivametne a este metodo para analizar sus hijos
	 * tambien.
	 * 
	 * @param element
	 *            Elemento actual sobre el que tenemos que decidir si lo
	 *            añadimos al mapa o no.
	 * @param elementsByIdNode
	 *            Mapa sobre el que se están añadiendo las referencias de los
	 *            elementos indexadolos por su id_node.
	 * @throws DataConversionException 
	 * @throws DataErrorException
	 *             Si existe algun error en el formateo del XML.
	 * @throws JDOMException 
	 */
	private void preprocessXMLChild(Element rootElement,Element element, Map<Integer, Element> elementsByIdNode, Set<Integer> idtosUsed) throws DataErrorException, JDOMException {
		Attribute nodeIdentifierAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		if (nodeIdentifierAttribute == null) {
			if (element.getName().equals(XMLConstants.TAG_MEMO)){
				return;
			}else if (element.getName().equals(XMLConstants.TAG_DATA_PROPERTY)){
				return;
			}
		}
		
		boolean isRefNode=element.getAttribute(XMLConstants.ATTRIBUTE_REFNODE)!=null;
				
		Integer currIdNode=getCurrentIdNode(elementsByIdNode,element);		
		Integer idSourceNode=getSourceIdNode(elementsByIdNode,element);	
		if(currIdNode==null&&idSourceNode==null){
			System.out.print("NODO ERROR "+jdomParser.returnXML(element));
			throw new DataErrorException("Todos los elementos tienen que tener el atributo " + XMLConstants.ATTRIBUTE_IDNODE + " o " + XMLConstants.ATTRIBUTE_REFNODE);
		}
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);

		// Comprobamos si se trata de un nodo mi empresa que está asociado a
		// otro objeto y de ser asi guardamos esa relación.
		if (Constants.CLS_MI_EMPRESA.equals(element.getName())) {
			if (element.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm) != null) {
				Element parentElement = element.getParent();
				if (parentElement != null) {					
					Attribute parentIdNodeAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
					if (parentIdNodeAttribute == null){						
						parentIdNodeAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
					}
					
					Integer parentIdNode = parentIdNodeAttribute.getIntValue();
					nodesWithMiEmpresa.add(parentIdNode);
					idNodeMiEmpresaMap.put(parentIdNode, idSourceNode);
				}
			}
		}
		if (idtoAttribute != null) {			
						
			int idto = idtoAttribute.getIntValue();
			String action = element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
			if (action.equals(XMLConstants.ACTION_NEW)) {
				String rdn = element.getAttributeValue(Constants.PROP_RDN);
				if (rdn!=null) {
					Matcher matcher = rdnTemporalPattern.matcher(rdn);
					while (matcher.find()) {
						String group = matcher.group();
						String[] idoNegIdto = group.split("@");
						if (idoNegIdto.length>1) {
							int idtoFixed = Integer.parseInt(idoNegIdto[1].substring(0, idoNegIdto[1].length()-1));
							idtosUsed.add(idtoFixed);
						}
					}
				}
			}
			idtosUsed.add(idto);
			
				int tableId =getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);
				
				Map<Integer, Integer> tableIdToIdNode = tableIdToIdNodeByIdto.get(idto);
				if (tableIdToIdNode == null) {
					tableIdToIdNode = new Hashtable<Integer, Integer>();
					tableIdToIdNodeByIdto.put(idto, tableIdToIdNode);
				}
				
				tableIdToIdNode.put(tableId, idSourceNode);
				//int ido = QueryConstants.getIdo(tableId, idto);
				if (tableId<0) {
					//System.out.println("AñadIENDO IDO NEGATIVO " + tableId + ", idNode " + idNode);
					idoNegativeIdNode.put(QueryConstants.getIdo(tableId, idto),idSourceNode);
				}
				IncrementalData incrementalData = getIncrementalData(rootElement,elementsByIdNode,element);
				if (incrementalData != null){
					incrementalDataByIdNode.put(currIdNode==null?idSourceNode:currIdNode, incrementalData);
				}
				if (action != null && action.equals(XMLConstants.ACTION_DELOBJECT)){
					objectsToDelete.add(new ObjectIdentifier(idto, tableId));
				}
			//}
		}

		List<Element> children = new LinkedList<Element>();
		for (Object obj : element.getChildren()) {
			if (obj instanceof Element) {
				children.add((Element) obj);
			}
		}

		for (Element child : children) {
			preprocessXMLChild(rootElement,child, elementsByIdNode, idtosUsed);
		}
	}

	/**
	 * Busca en todas las DataProperties del elemnento, es decir, sus atributos,
	 * para ver si alguno es incremental, y de ser asi, genera un objeto con la
	 * informacion de los distintos incrementos que hay que realizar sobre el
	 * objeto
	 * 
	 * @param element
	 *            Elemento que se quiere inspeccionar en busca de propiedades
	 *            incrementales.
	 * @return devolvera <code>null</code> si no existen incrementos para este
	 *         objeto. En cualquier otro caso devuelve un objeto con toda la
	 *         informacion sobre todos los incrementos que hay que realizar
	 *         sobre el objeto al que representa el Element.<br>
	 *         El tableId del IncrementalData solo se rellenara si en el Element
	 *         el tableId que viene es positivo, es decir, es el de un objeto
	 *         que existe en base de datos.
	 * @throws JDOMException 
	 */
	@SuppressWarnings("unchecked")
	private IncrementalData getIncrementalData(Element rootElement,Map<Integer, Element> elementsByIdNode,Element element) throws JDOMException {
		IncrementalData result = null;
		Integer idto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
		Integer tableId = getTableId(idto,newRdnMap,newTableIdMap,rootElement, elementsByIdNode,element);
		Table elementTable = dataBaseMap.getTable(idto);
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		for (Attribute attribute : new LinkedList<Attribute>(element.getAttributes())) {
			if (attribute.getName().equals(XMLConstants.ATTRIBUTE_TABLEID)){
				// El tableId puede venir negativo, lo que podria contar como
				// incremental, pues es numero y puede empezar pues '-'
				continue;
			}
			Integer idProperty = dataBaseMap.getPropertyId(attribute.getName());
			if (idProperty == null){
				continue;
			}
			PropertyInfo property = classInfo.getProperty(idProperty);
			//System.out.println("PROPERT ID:"+idProperty+" clase "+classInfo.getName());
			if (property==null||!isNumericType(property.getPropertyTypes())){
				// La propiedad no es de tipo numerico con lo cual no puede ser un incremental.
				continue;
			}
			
			String attributeValue = attribute.getValue();
			// Los valores de propiedades incrementales siempre empiezan por '+'
			// . Si el numero es negativo aparece como '+-numero' y son de tipo numero. POr tanto siempre quito el '+' para que no de fallo de parseo
			if (isIncremental(attributeValue)){
				if (result == null){
					// Si entramos aque es porque todavia no se habia encontrado
					// un incremento para este objeto, y tenemos que inicializar
					// el IncrementalData.
					String rdn = element.getAttributeValue(Constants.PROP_RDN);
					result = new IncrementalData(idto,rdn);
					if (tableId.intValue() > 0){
						result.setTableId(tableId);
					}
				}
				//Quitamos el '+' inicial con substring
				Double value = Double.parseDouble(attributeValue.substring(1));
				TableColumn propertyColumn = elementTable.getDataPropertyColumn(idProperty);
				result.addIncrementValue(propertyColumn, value);
			}
		}
		return result;
	}

	/**
	 * Indica si una propiedad es de tipo basico y dicho tipo basico es de tipo
	 * numero.
	 * 
	 * @param propertyTypes
	 *            Tipos de los datos de la propiedad.
	 * @return Devuelve <code>true</code> si y solo si es de tipo
	 *         {@link Constants#IDTO_INT} o de tipo
	 *         {@link Constants#IDTO_DOUBLE}
	 */
	private boolean isNumericType(Set<Integer> propertyTypes) {
		boolean result;
		if (propertyTypes.size() <= 0  || propertyTypes.size() > 1){
			result = false;
		}else{
			Integer dataType = propertyTypes.iterator().next();
			result = dataType.equals(Constants.IDTO_INT) || dataType.equals(Constants.IDTO_DOUBLE);
		}
		return result;
	}

	/**
	 * Indica si la cadena dada puede interpretarse como el valor de una
	 * propiedad incremental.
	 * 
	 * @param attributeValue
	 *            Cadena a analizar.
	 * @return <code>true</code> si es un incremental.
	 */
	private boolean isIncremental(String attributeValue) {
		boolean result = false;
		if (/*attributeValue.startsWith("-") ||*/ attributeValue.startsWith("+")) {
			try {
				// Comprobamos si se puede transformar a entero, si no salta
				// la excepción es que es un incremental.
				Double.parseDouble(attributeValue.substring(1));
				result = true;
			} catch (NumberFormatException e) {
			}
		}
		return result;
	}

	/**
	 * Procesamiento que se hace justo antes de finalizar, despues de todas las
	 * inserciones y modificaciones.
	 * 
	 * @throws DataErrorException
	 * @throws InstanceLockedException
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws ApplicationException
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws OperationNotPermitedException
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataConversionException
	 * @throws NoSuchColumnException 
	 */
	private void postProcess() throws DataErrorException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataConversionException, SQLException, NamingException, NoSuchColumnException{
		resolveFieldsPendingResolution();
		processIncrementals();
		addAllChanges();
		deleteObjects();
		rdnModifyLast();
	}

	/**
	 * Procesa recursivamente un elemento y todos sus hijos.
	 * 
	 * @param element
	 *            Elemento desde el que se empieza el procesamiento
	 * @param lockedObjects
	 *            Conjunto de los identificadores de objetos que han sido
	 *            bloquedos durante la ejecucion de las acciones expresadas por
	 *            el XML de entrada.
	 * @throws DataErrorException
	 *             Si hay errores en el formato del elemento XML.
	 * @throws DataConversionException
	 *             Si el formato en el que se escribieron los valores de los
	 *             atributos no es correcto.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 * @throws CardinalityExceedException 
	 * @throws IncoherenceInMotorException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws NotFoundException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 */
	@SuppressWarnings("unchecked")
	private void processChild(Element rootElement,Element element, Map<Integer, Set<Integer>> lockedObjects, Integer idNodeParent) throws DataErrorException, DataConversionException, SQLException, NamingException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException {
		Attribute nodeIdentifierAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
		boolean isRefNode=element.getAttribute(XMLConstants.ATTRIBUTE_REFNODE)!=null;
		//si es refnode debe ser un nodo vinculo que referencia a otro y el identificador
		// del nodo referenciado tiene que estar en el atributo ref_node.

		Integer currIdNode=element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE)==null?null:element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE).getIntValue();
		
		Integer idSourceNode=getSourceIdNode(elementsByIdNode,element);	
		
		if (nodeIdentifierAttribute == null) {
			if (element.getName().equals(XMLConstants.TAG_MEMO)) {
				// Un elemento MEMO nunca va a tener hijos, con lo cual salimos
				// del metodo tras procesarlo.
				processMemoElement(element,idNodeParent/*Sabemos que en los memos idNodeParent nunca es null*/);
			} else if (element.getName().equals(XMLConstants.TAG_DATA_PROPERTY)){
				processDataPropertyElement(element);
			} else {
				if(idSourceNode==null) throw new DataErrorException("[DatabaseManager] El elemento " + element.getName() + " no tiene id_node y, por tanto, está mal formado.");
			}			
		}
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Integer idto = null;
		// Se hace un tratamiento para el idto, porque si es un nodo vinculo, no
		// tiene idto, pero se necesita el idto al
		// que apunta el elemento referenciado para la comprobación de la
		// réplica
		if (idtoAttribute != null) {
			idto = idtoAttribute.getIntValue();
		} else {			
			try{// try para que no falle en migracion mysql
				idto = elementsByIdNode.get(idSourceNode).getAttribute(XMLConstants.ATTRIBUTE_IDTOm).getIntValue();
				}catch(Exception e){
					System.err.println("ERROR EN NODO,idNode: "+idSourceNode);
					try {
						System.out.println(jdomParser.returnXML(element)) ;
					} catch (JDOMException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					//String resp=Auxiliar.leeTexto("continuar? ");
					//e.printStackTrace();
					//if(!(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S"))){
					//	return;
					//}
					return;
				}
		}

		if (idtosNotModifiables != null && idtosNotModifiables.contains(idto)) {
			Attribute tableIdAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
			String alias;
			try{
				alias = instanceService.getIk().getAliasOfClass(idto, uTask);
			}catch (Exception e) {
				throw new DataErrorException("Error al intentar obtener el alias de la clase : " + element.getName());
			}
			throw new DataErrorException(DataErrorException.ERROR_DATA, "No es posible modificar el objeto " + tableIdAttribute.getValue() + " de " + alias);
		}

		Attribute propertyAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
		boolean processLink = propertyAttribute != null;
		if (processLink) {
			Integer idProperty = dataBaseMap.getPropertyId(propertyAttribute.getValue());
			Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
			if (inverseProperty != null && dataBaseMap.isStructuralProperty(inverseProperty)) {
				processLink = false;
			}
		}
		if (processLink) {
			// Si se entra aque solo nos podemos encontrar con nodos que son
			// hijos de otros y representan a objetos.
			Element parentElement = element.getParent();
			
			//INI DEBUG
			String stableId=element.getAttributeValue("tableId");
			
			if(stableId!=null&&( 
					stableId.equals("17653")||stableId.equals("-14258")
				)){
				try {
					System.out.println("CHILD NODE "+jdomParser.returnXML(element));
					if(parentElement!=null) System.out.println("PARENT NODE "+jdomParser.returnXML(parentElement));
				} catch (JDOMException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//END DEBUG
			
			Attribute parentIdtoAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
			Integer parentTableId = getTableId( parentIdtoAttribute.getIntValue(),newRdnMap,newTableIdMap,rootElement,elementsByIdNode,parentElement);
			
			
			Integer parentIdto = parentIdtoAttribute.getIntValue();
			Integer tableId;
			//if (!processedIdNodes.contains(idNode)) {
			if(true){
				// Se trata de un nodo hijo de otro elemento que no ha sido
				// procesado todavia.
				if (idtoAttribute == null) {
					// Hay que buscar el elemento que tiene realmente los datos,
					// porque este es un nodo vinculo.
					Element elementWithData = elementsByIdNode.get(currIdNode);
					processElementWithData(idto,rootElement,elementWithData);
					Attribute tableIdAttribute = elementWithData.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
					tableId = tableIdAttribute.getIntValue();
					idtoAttribute = elementWithData.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
					idto = idtoAttribute.getIntValue();
				} else {
					// El elemento que estamos tratando es el que tiene los
					// datos.
					processElementWithData(idto,rootElement,element);
					
					tableId = getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);
				}
				String action = element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				boolean checkIsolated = true;
				if (action.equals(XMLConstants.ACTION_DELOBJECT)){
					action = parentElement.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
					checkIsolated = false;
				}
				processLink(parentIdto, parentTableId, idto, tableId, propertyAttribute.getValue(), action, checkIsolated);
			} /*else {
				// Se trata de un nodo que depende de otro mediante una
				// propiedad, pero que ya ha sido procesado, con lo
				// cual solo hay que trabajar sobre el vinculo. Hay que buscar
				// el nodo que contiene los datos si no es
				// este.
				if (idtoAttribute == null) {
					// Hay que buscar el elemento que tiene realmente los datos,
					// porque este es un nodo vinculo.
					Element elementWithData = elementsByIdNode.get(idNode);
					Attribute tableIdAttribute = elementWithData.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
					tableId = tableIdAttribute.getIntValue();
					idtoAttribute = elementWithData.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
					idto = idtoAttribute.getIntValue();
				} else {					
					tableId = getTableId( rootElement,elementsByIdNode,element);
				}
				String action = element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				boolean checkIsolated = true;
				if (action.equals(XMLConstants.ACTION_DELOBJECT)){
					action = parentElement.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
					checkIsolated = false;
				}
				processLink(parentIdto, parentTableId, idto, tableId, propertyAttribute.getValue(), action, checkIsolated);

			}*/
		} else if (!processLink ){//&& !processedIdNodes.contains(idNode)) {
			// Se trata de un elemento que no es hijo de ninguno es hijo de uno
			// mediante una propiedad cuya inversa es estructural.
			if (idtoAttribute == null){
				processElementWithData(idto,rootElement,elementsByIdNode.get(currIdNode));
			}else{
				processElementWithData(idto,rootElement,element);
			}
		}
		// El caso no contemplado es que el nodo está en la parte más superior
		// pero que ya haya sido procesado
		// anteriormente, en ese caso no tenemos que hacer nada, porque al
		// haberse procesado, no tenemos nada más que
		// hacer salvo procesar los hijos que pudiera tener.

		// Una vez finalizado el procesamiento del nodo actual, procedemos a
		// analizar sus nodos hijos.
		List<Element> children = (List<Element>) element.getChildren();
		for (Element child : children) {
			processChild(rootElement,child, lockedObjects, currIdNode);
		}
	}

	/**
	 * Recorre los valores del mapa changesByIdNode y los va añadiendo al objeto
	 * Changes.<br>
	 * Cada vez que se ejecuta este metodo se elimina el contenido antiguo del
	 * changes y vuelve a calcular todos los objetos que tiene que contener
	 * siguiendo el procedimiento especificado.
	 */
	private void addAllChanges() {
		changes = new Changes();
		for (ObjectChanged objectChanged : changesByIdNode.values()) {
			changes.addObjectChanged(objectChanged);
		}
		for (ObjectChanged objectChanged : changesNoRdn) {
			changes.addObjectChanged(objectChanged);
		}
		//System.out.println(changes);
	}

	/**
	 * añade la informacion del nodo memo al objeto del que cuelga el objeto
	 * actual.
	 * 
	 * @param element
	 *            Elemento con TAG memo, que contiene el texto que se debe poner
	 *            en la propiedad que se indique en este elemento.
	 * @throws DataConversionException
	 *             Si el formato del dato de un atributo no coincide con el
	 *             esperado.
	 * @throws NamingException
	 *             Si se produce algun fallo en la comunicación con la base de
	 *             datos.
	 * @throws SQLException
	 *             Si se produce algun fallo en la comunicación con la base de
	 *             datos.
	 * @throws DataErrorException Si el elemento no está bien formado.
	 */
	private void processMemoElement(Element element, int idNodeParent) throws DataConversionException, SQLException, NamingException, DataErrorException {
		// Obtenemos el nodo padre al que pertenece la propiedad de tipo memo que estamos analizando, y obtenemos su idto.
		Element parentElement = element.getParent();
		Attribute parentIdtoAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Attribute actionAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		
		Integer parentIdto = parentIdtoAttribute.getIntValue();
		// Obtenemos el tableId del objeto padre al que le tenemos que actualizar la propiedad de tipo memo
		Attribute parentTableIdAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Integer parentTableId = parentTableIdAttribute.getIntValue();
		String action = actionAttribute.getValue();
		// Cogemos los valores que tenga el memo
		Element memoValueElement = element.getChild(XMLConstants.TAG_VALUE);
		//Element memoOldValueElement = element.getChild(XMLConstants.TAG_VALUE + XMLConstants.OLD_PROPERTY);
		String memoValue = memoValueElement==null ? null:memoValueElement.getText();
		//String memoOldValue = memoOldValueElement != null ? memoOldValueElement.getText() : null;
		// Obtenemos la clase a la que pertenece el objeto padre.
		//ClassInfo parentClass = dataBaseMap.getClass(parentIdto);
		// Conseguimos la propiedad por la que estamos vinculando.
		Attribute propertyAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
		int idProperty = dataBaseMap.getPropertyId(propertyAttribute.getValue());
		// Conseguimos la tabla que representa los datos de la clase.
		Table parentTable = dataBaseMap.getTable(parentIdto);
		// Construimos la consulta dependiendo de la ubicación de la propiedad.
		if (parentTable.isExternalizedProperty(idProperty)){
//			String action = element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
//			if (action == null){
//				throw new DataErrorException("El nodo memo que apunta a [CLASE=" + parentElement.getName() + "; TABLE_ID=" + parentTableId + "] no tiene action.");
//			}
//			// Comprobamos si tenemos que actualizar o crear el registro.
//			if (action.equals(XMLConstants.ACTION_NEW)){
//				// TODO
//				sql = "INSERT INTO " + cB + cE;
//			}else if (action.equals(XMLConstants.ACTION_SET)){
//				// TODO
//				sql = null;
//			}else{
//				throw new DataErrorException("Sobre DataProperties solo se permite la acción NEW o SET. acción enviada=" + action);
//			}
			Exception e = new Exception("Caracteristica no implementada: MEMOs con cardinalidad multiple");
			e.printStackTrace();
			return;
		}else{
			if (action.equals(XMLConstants.ACTION_DEL)){
				String cB = generateSQL.getCharacterBegin();
				String cE = generateSQL.getCharacterEnd();
				String sql = "UPDATE " + cB + parentTable.getName() + cE + " SET " + cB + propertyAttribute.getValue() + cE + "=NULL WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + parentTableId;
				DBQueries.executeUpdate(fcdb, sql);
			}else{
				if(memoValue!=null) {
					Map<String, String> propertyValue = new HashMap<String, String>();
					propertyValue.put(propertyAttribute.getValue(), memoValue);
					
					Attribute idNodeAttribute = parentElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
					String keyInd = getKeyIndividual(null,newRdnMap,parentElement);
					ObjectChanged objectChanged = changesByIdNode.get(keyInd);
					if(objectChanged==null){
						objectChanged=new ObjectChanged();
						objectChanged.setNewIdo(parentTableId);
						objectChanged.setOldIdo(parentTableId);
						//No lo añadimos a la lista de changesByIdNode ya que solo se usa como plantilla dentro de applyMatcherAndUpdateAllFields
					}
					applyMatcherAndUpdateAllFields(parentIdto,true, null, propertyValue, parentTable, parentTableId, parentTableId, objectChanged,keyInd, idNodeParent);
					
//					sql = "UPDATE " + cB + parentTable.getName() + cE + " SET " + cB + propertyAttribute.getValue() + cE
//					+ "=" + formatValue(memoValue, Constants.IDTO_MEMO) + " WHERE " + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + parentTableId + ";";
				}
			}
		}
	}

	/**
	 * Procesa una DataProperty de cardinalidad multiple.
	 * 
	 * @param dataPropertyElement
	 *            Elemento que contiene la informacion de la DataProperty de
	 *            cardinalidad multiple.
	 * @throws DataConversionException
	 *             Si el formato de los datos del XML no es correcto.
	 * @throws DataErrorException
	 *             Si hay algun error en los datos.
	 * @throws NamingException
	 *             Si hay algun error en la comunicación con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la sentencia.
	 */
	private void processDataPropertyElement(Element dataPropertyElement) throws DataConversionException, DataErrorException, SQLException, NamingException{
		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		// Sacamos la informacion del objeto al que pertenece la propiedad.
		Element classElement = dataPropertyElement.getParent();
		Attribute idtoAttribute = classElement.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Attribute tableIdAttribute = classElement.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		// Sacamos la informacion del nodo dataproperty
		String propertyName = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
		String value = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUE);
		Attribute actionAttribute = dataPropertyElement.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		
		int idto = idtoAttribute.getIntValue();
		int tableId = tableIdAttribute.getIntValue();
		int idProperty = dataBaseMap.getPropertyId(propertyName);
		String action = actionAttribute.getValue();
		
		Table classTable = dataBaseMap.getTable(idto);
		// Las DataProperties externalizadas solo pueden estar en una tabla.
		//System.out.println("propiedad [" + idProperty + " classTable "+classTable.getName());
		int dataPropertyTableIdto = classTable.getExternalizedPropertyLocations(idProperty).iterator().next();
		Table dataPropertyTable = dataBaseMap.getTable(dataPropertyTableIdto);
		// Conseguimos la columna donde tenemos que almacenar la informacion de la propiedad.
		TableColumn propertyColumn = dataPropertyTable.getDataPropertyColumn(idProperty);
		if (propertyColumn == null){
			throw new DataErrorException("No se ha encontrado una columna asociada a la propiedad [" + idProperty + "=" + propertyName + "] en la tabla " + dataPropertyTable.getName() + " donde se creia externalizada.");
		}
		// Conseguimos la columna que tiene que almacenar el tableId del objeto al que pertenece la dataproperty.
		TableColumn[] parentIdColumns = dataPropertyTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, idto);
		if (parentIdColumns == null || parentIdColumns[0] == null) {
			throw new DataErrorException("No se ha encontrado ninguna columna en la tabla " + dataPropertyTable.getName() + " que apunte a " + classTable.getName() 
					+ ". Lo cual es un error en tablas que contienen la informacion de las DataProperties externalizadas.");
		}
		// Guardamos en base de datos los datos de la propiedad haciendo las
		// acciones pertinentes dependiendo del tipo de acción que indicase el
		// nodo.
		if (action.equals(XMLConstants.ACTION_NEW)) {
			String sql = "INSERT INTO " + cB + dataPropertyTable.getName() + cE + " (" + cB + parentIdColumns[0].getColumnName() + cE + ", " + cB + propertyColumn.getColumnName() + cE + ") VALUES (" + tableId + ", " 
						 + formatValue(value, propertyColumn.getColumnDataType()) + ");";
			DBQueries.execute(fcdb, sql);
		} else if (action.equals(XMLConstants.ACTION_SET)) {
			String oldValue = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUE + XMLConstants.OLD_PROPERTY);
			if (oldValue == null){
				throw new DataErrorException("No se ha especificado el valor antiguo de la DataProperty de cardinalidad multiple a modificar: " + propertyName);
			}
			String sql = "UPDATE " + cB + dataPropertyTable.getName() + cE + " SET " + cB + propertyColumn.getColumnName() + cE + "=" + formatValue(value, propertyColumn.getColumnDataType()) + " WHERE " +
					cB + parentIdColumns[0].getColumnName() + cE + "=" + tableId + " AND " + cB + propertyColumn.getColumnName() + cE + "=" + formatValue(oldValue, propertyColumn.getColumnDataType());
			DBQueries.executeUpdate(fcdb, sql);
		} else if (action.equals(XMLConstants.ACTION_DEL) || action.equals(XMLConstants.ACTION_DELOBJECT)){
			String sql = "DELETE FROM " + cB + dataPropertyTable.getName() + cE + " WHERE " + cB + parentIdColumns[0].getColumnName() + cE + "=" + tableId + " AND " + cB + propertyColumn.getColumnName() + cE + "=" +
					formatValue(value, propertyColumn.getColumnDataType());
			DBQueries.execute(fcdb, sql);
		}
	}

	/**
	 * Procesa un elemento que se sabe que tiene datos dependiendo de la acción
	 * que tenga el elemento. Una vez realizadas todas las actuaciones
	 * necesarias, lo marca como procesado.
	 * 
	 * @param element
	 *            Elemento del archivo que se va a procesar y que contiene toda
	 *            la informacion necesaria para trabajar.
	 * @throws DataErrorException
	 *             Si hay algun error en el formato del XML.
	 * @throws DataConversionException
	 *             Si el formato del valor de un atributo no es correcto.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 * @throws CardinalityExceedException 
	 * @throws IncoherenceInMotorException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws NotFoundException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 */
	private void processElementWithData(Integer idto,Element rootElement,Element element) throws DataErrorException, DataConversionException, SQLException, NamingException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException {
		Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
				
		if (actionAttribute == null) {
			throw new DataErrorException("Todos los elementos que representan a una clase tienen que tener una acción asociada en el XML");
		}
		String actionValue = actionAttribute.getValue();
		
		Integer newTableId=getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);			
		
		String refNode=element.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);
		if(refNode!=null && refNode.equals("53")){
			System.out.println("PR");
		}
			//en algun caso llega un nodo originalmente createIne, con refNode, pero aparece como set, ya se ha procesado el nuevo tableId y no esta actualiado 
		element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, newTableId.toString());		
		
		if(newTableId!=null && newTableId.intValue()>0 && (actionValue.equals(XMLConstants.ACTION_NEW)||actionValue.equals(XMLConstants.ACTION_CREATE_IF_NOT_EXIST))){
			actionAttribute.setValue(XMLConstants.ACTION_SET);
			element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, newTableId.toString());
			actionValue=XMLConstants.ACTION_SET;
		}
		
		if(newTableId!=null && newTableId.intValue()<0 && actionValue.equals(XMLConstants.ACTION_SET) && element.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE)!=null ){
			//puede pasar que proceso un ref node antes que el nodo de creacion source, y viene con accion set, en ese caso debo crear ya con new
			Element srcNode=elementsByIdNode.get(Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE)));
			if(srcNode!=null){
				String actionSrc=element.getAttributeValue(XMLConstants.ATTRIBUTE_ACTION);
				if(actionSrc!=null && actionSrc.equals(XMLConstants.ACTION_NEW)){
					actionAttribute.setValue(XMLConstants.ACTION_NEW);
					actionValue=XMLConstants.ACTION_NEW;
				}
			}
		}
				
		if (actionValue.equals(XMLConstants.ACTION_DEL)) {
			processDelElementWithData(element);
		} else if (actionValue.equals(XMLConstants.ACTION_DELOBJECT)) {
			// Se procesa al final del todo
		} else if (actionValue.equals(XMLConstants.ACTION_NEW)) {
			processNewElementWithData(element);
		} else if (actionValue.equals(XMLConstants.ACTION_SET)) {
			processSetElementWithData(rootElement,element);
		} else {
			throw new DataErrorException("El valor " + actionValue + " no está entre los valores esperados para el atributo action.");
		}
	}

	/**
	 * Borra todas las DataProperties del elemento dejandolas a NULL en base de
	 * datos.
	 * 
	 * @param element
	 *            Elemento que contiene los datos que tenemos que modificar.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException 
	 * @throws DataConversionException 
	 */
	@SuppressWarnings("unchecked")
	private void processDelElementWithData(Element element) throws SQLException, NamingException, DataErrorException, DataConversionException {
		// TODO
		Map<Integer, String> deletedDataProperties = new Hashtable<Integer, String>();
		List<Attribute> attributes = element.getAttributes();
		for (Attribute attribute : attributes) {
			// Reunimos los identificadores de propiedades que encontremos
			Integer idProperty = dataBaseMap.getPropertyId(attribute.getName());

			//en un nodo del, no puede eliminarse rdn puesto que es necesario para identificar en replicas, es una limitacion que afecta
			//puesto que no tiene sentido dejar vacio el rdn
			if(idProperty!=null && idProperty.equals(Constants.IdPROP_RDN)) continue;
			
			if (idProperty == null && attribute.getName().equals(XMLConstants.ATTRIBUTE_DESTINATIONm)){
				idProperty = IQueryInfo.ID_DESTINATION;
			}
			
			if (idProperty != null){
				deletedDataProperties.put(idProperty, attribute.getValue());
			}
		}
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		if (idtoAttribute == null) {
			throw new DataErrorException("Los elementos que representan objetos de una clase deben indicar mediante el atributo " + XMLConstants.ATTRIBUTE_IDTOm + " el identificador de la clase a la que pertenecen.");
		}
		int idto = idtoAttribute.getIntValue();
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		Set<Integer> fileProperties = classInfo.getFileProperties();
		if (! fileProperties.isEmpty()){
			processElementWithFileProperties(element, classInfo, XMLConstants.ACTION_DEL);
		}

		deleteDataProperties(element, deletedDataProperties);
	}

	/**
	 * Elimina el valor de las dataProperties indicadas en el mapa, ponindolo a
	 * nulo.
	 * 
	 * @param element
	 *            Elemento que contiene la informacion del objeto, del que se
	 *            extraern los datos identificativos del mismo.
	 * @param deletedDataProperties
	 *            Mapa de las propiedades a borrar y el valor que deberaa tener
	 *            antes de ser borradas.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataConversionException 
	 */
	private void deleteDataProperties(Element element, Map<Integer, String> deletedDataProperties) throws SQLException, NamingException, DataConversionException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		// Extraemos los datos identificativos del objeto.
		Integer idto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
		Table table = dataBaseMap.getTable(idto);
//		ClassInfo classInfo = dataBaseMap.getClass(idto);
//		Set<Integer> fileProperties = classInfo.getFileProperties();
//		List<String> filesToDelete = new LinkedList<String>();
		Attribute tableId = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Integer tableIdValue = tableId == null ? null : tableId.getIntValue();
		if (tableIdValue!=null) {
			for (Integer idProperty : deletedDataProperties.keySet()) {
				TableColumn column = table.getDataPropertyColumn(idProperty);
				assert column != null : "No se ha encontrado la columna asociada a la DataProperty " + idProperty + " en la tabla " + table.getName();
				String propertyValue = deletedDataProperties.get(idProperty);
				String sql = "UPDATE " + cB + table.getName() + cE + " SET " + cB + column.getColumnName() + cE + "=NULL WHERE " + cB + column.getColumnName() + cE + "=" + formatValue(propertyValue, column.getColumnDataType());
				sql += " AND " + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + tableIdValue;
				DBQueries.executeUpdate(fcdb, sql);
	//			if (fileProperties != null && fileProperties.contains(idProperty)){
	//				filesToDelete.add(propertyValue);
	//			}
			}
	//		deleteFiles(filesToDelete);
		}
	}

	/**
	 * Procesa un elemento que tiene como acción "new" y lo crea en base de
	 * datos. Una vez conseguida la inserción en base de datos, marca el id_node
	 * como procesado.
	 * 
	 * @param element
	 *            Elemento que contiene la informacion necesaria para crear el
	 *            objeto.
	 * @throws DataConversionException
	 *             Si el formato del valor de un atributo no es correcto.
	 * @throws DataErrorException
	 *             Si existe alguna incoherencia con el modelo.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 * @throws CardinalityExceedException 
	 * @throws IncoherenceInMotorException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws NotFoundException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 */
	private void processNewElementWithData(Element element) throws DataConversionException, DataErrorException, SQLException, NamingException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException {
		Attribute tableId = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		
		Integer tableIdValue = tableId == null ? null : tableId.getIntValue();

		// Condición insertada para las acciones new de vinculos con objetos.
		// Una acción new en un vinculo con un objeto que tiene tableId positivo
		// significa que ese objeto ya existe y no tenemos que crearlo.
		if (tableIdValue != null && tableIdValue > 0) {
			return;
		}

		Integer idtoValue = idtoAttribute.getIntValue();
		String keyInd = getKeyIndividual(null,newRdnMap,element);
		
		ObjectChanged objectChanged = changesByIdNode.get(keyInd);
		if (objectChanged == null) {
			objectChanged = new ObjectChanged();
			changesByIdNode.put(keyInd, objectChanged);
		}
		Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
		if (rdnAttribute == null) {
			/*if(tableIdValue>0){
					return;
			}else{				
				element.setAttribute(Constants.PROP_RDN,""+tableIdValue+"#"+idtoValue);
				rdnAttribute=element.getAttribute(Constants.PROP_RDN);
			} PARCHE REPLICAS*/
			throw new DataErrorException("El objeto de tableId=" + tableIdValue + " e idto=" + idtoValue + " no tiene asignado un rdn al intentar crearlo.");
		}
		String prevAdapt=rdnAttribute.getValue();
		String adaptado=adaptReplicaValue(fcdb,rdnAttribute.getValue());
		if(!adaptado.equals(prevAdapt)){
			rdnAttribute.setValue(adaptado);
		}
		Integer idNodeValue = getSourceIdNode(elementsByIdNode,element);
		IncrementalData incrementalData = incrementalDataByIdNode.get(idNodeValue);

		Table table = dataBaseMap.getTable(idtoValue);
		//System.out.println("idtoValue " + idtoValue);
		if(table==null){
			/*try {
				//System.out.println(jdomParser.returnXML(element));
				Element root=element.getParent();
				if(root!=null){
					System.out.println("PADRE\n"+jdomParser.returnXML(root));
				}
			} catch (JDOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		//System.out.println("table.getName() " + table.getName());
		//System.out.println("table.getId() " + table.getId());
		Map<String, String> propertyValueFormat = new HashMap<String, String>();
		Map<String, String> propertyValue = new HashMap<String, String>();
		getPairsPropertyValueFromElement(element, table, propertyValueFormat, propertyValue, false);
		String[] fieldsValues = getFieldsAndValuesForInsert(propertyValueFormat, table);

		Integer newTableIdValue = null;
		String sqlQuery = generateSQL.getInsertStatement(table.getName(), fieldsValues[0], fieldsValues[1], !keepTableIds);
		if (keepTableIds) {
			newTableIdValue = -tableIdValue;
			//insertamos
			DBQueries.executeUpdate(fcdb, sqlQuery);
			//modificamos secuencia con el maximo tableId
			Integer lastSeq = getLastTableId(table.getName());
			String nameSeq = table.getName() + "_tableId_seq";
			String sqlAlter = "ALTER SEQUENCE " + generateSQL.getCharacterBegin() + nameSeq + generateSQL.getCharacterEnd() + " RESTART WITH " + Integer.valueOf(lastSeq+1);
			DBQueries.executeUpdate(fcdb, sqlAlter);
		} else {
			// PostgreSQL permite devolver el valor de la secuencia que se ha
			// asignado al registro insertado en el mismo insert, con lo cual, no
			// hace falta llamar a getLastTableId
			try {
				if (fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL)) {
//ESTO YA NO ES NECESARIO YA QUE CUANDO VIENE DESDE REPLICA SE ESTA LLAMANDO A SETLOCALIDOS QUE LE PONE UN IDO POSITIVO SI YA EXISTE EN BASE DE DATOS Y EN ESE CASO LA ACCION SE CONVIERTE A SET AUNQUE VENGA COMO NEW
//					if (incrementalData != null){
//						newTableIdValue = generateSQL.executeSecurePostgreInsert(fcdb, sqlQuery);
//						System.out.println("Ido: " + tableIdValue + ", TableId obtenido: " + newTableIdValue);
//					}else{
						newTableIdValue = DBQueries.executePostgreInsert(fcdb, sqlQuery);
						//System.out.println("Ido: " + tableIdValue + ", TableId obtenido: " + newTableIdValue);
//					}
				} else {
					DBQueries.execute(fcdb, sqlQuery);
					newTableIdValue = getLastTableId(table.getName());
					//System.out.println("Ido: " + tableIdValue + ", TableId obtenido : " + newTableIdValue);
				}
			} catch (SQLException e) {
//TAMPOCO ES NECESARIO POR LO DE ARRIBA
//				boolean isIncremental = incrementalData != null;
//				System.out.println("DatabaseManager.processNewElementWithData() [isIncremental=" + isIncremental + "]");
//				if (isIncremental){
//					newTableIdValue = processIncrementalConflict(element, table);
//				}else{
					e.printStackTrace();
					throw new DataErrorException(DataErrorException.ERROR_DATA,"Error al procesar "+instanceService.getIk().getAliasOfProperty(idtoValue, Constants.IdPROP_RDN, null)+" '"+rdnAttribute.getValue()+"' de "+instanceService.getIk().getAliasOfClass(idtoValue, null)+", "+e.getMessage());
//				}
			}
		}
		if (tableIdValue != null && tableIdValue < 0) {
			objectChanged.setOldIdo(QueryConstants.getIdo(tableIdValue,idtoValue));
			objectChanged.setNewIdo(QueryConstants.getIdo(newTableIdValue, idtoValue));
		}
		
		if (incrementalData != null){
			// Si el objeto contenia un incremental, modificamos el tableId de
			// la informacion del incremental. Si era un new que se ha
			// transformado en UPDATE, el metodo de resolución del conflicto nos
			// devolvera el tableId con el que existia el objeto en base de
			// datos.
			incrementalData.setTableId(newTableIdValue);
		}
		applyMatcherAndUpdateAllFields(idtoValue,false, element, propertyValue, table, tableIdValue, newTableIdValue, objectChanged,keyInd, idNodeValue);
		// Guardamos el identificador que se le ha asignado en la base de
		// datos y marcamos como procesado el id_node
		// que identifica a este elemento.
		tableId.setValue(newTableIdValue.toString());
		processedIdNodes.add(idNodeValue);
		String indivKey=getKeyIndividual(null, newRdnMap, element);//""+idtoValue+"#"+rdnAttribute.getValue();
		newTableIdMap.put(indivKey, newTableIdValue);
		
		element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, newTableIdValue.toString());
	}
	
	private void resolveFieldsPendingResolution() throws DataErrorException, SQLException, NamingException, DataConversionException {
		//System.out.println("Resolucion de campos con idos negativos que an no se habian resuelto");
		Iterator<Integer> it = fieldsPendingResolution.keySet().iterator();
		while (it.hasNext()) {
			int idNodeContained = it.next();
			//System.out.println("idNodeContained " + idNodeContained);
			Element elementContained = elementsByIdNode.get(idNodeContained);
			
			Attribute idtoAttributeContained = elementContained.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
			Integer idtoValueContained = idtoAttributeContained.getIntValue();
			Table tableContained = dataBaseMap.getTable(idtoValueContained);
			HashMap<String,String> propertyValueContained = (HashMap<String,String>)fieldsPendingResolution.get(idNodeContained);

			String keyInd = getKeyIndividual(null,newRdnMap,elementContained);
			
			ObjectChanged objectChangedContained = changesByIdNode.get(keyInd);
					
			Integer newTableIdValueContained = Integer.parseInt(elementContained.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID));
			//System.err.println("keind "+keyInd+" objectChangeContained:"+objectChangedContained+" idNodeContained:"+idNodeContained+" propertyValueContained:"+propertyValueContained+" idtoValueContained:"+idtoValueContained);
			Integer tableIdValueContained = objectChangedContained!=null?objectChangedContained.getOldIdo():null;
			//System.out.println("tableIdValueContained " + tableIdValueContained + ", newTableIdValueContained " + newTableIdValueContained);
			applyMatcherAndUpdateAllFields(idtoValueContained,false, elementContained, propertyValueContained, tableContained, tableIdValueContained, newTableIdValueContained, objectChangedContained, keyInd,null);
		}
	}
	
	private void applyMatcherAndUpdateAllFields(Integer idto,boolean alwaysModify, Element element, Map<String, String> propertyValue, Table table, Integer tableIdValue, Integer newTableIdValue, ObjectChanged objectChanged,String keyInd, Integer curridNode) throws DataErrorException, SQLException, NamingException {
		String set = "";
		for (String field : propertyValue.keySet()) {
			String value = propertyValue.get(field);
			TableColumn tc=table.getColumnByName(field);
			int col_type=tc.getColumnDataType();

			String newValue = applyMatcher(col_type==Constants.IDTO_STRING?Constants.MAX_LENGHT_TEXT:0,field, value, tableIdValue, newTableIdValue, curridNode);
			boolean modify = alwaysModify;
			if (!modify)
				modify = !newValue.equals(value);
			if (modify) {
				//Debe actualizarse element con nuevo valor para que se grab resuelto en tabla de replicas. Element es nulo en nodos memo
				if(element!=null) element.setAttribute(field, newValue);
				int property = table.getColumnProperty(field);
				TableColumn column = table.getDataPropertyColumn(property);
				if (set.length()>0)
					set += ",";
				set += generateSQL.getCharacterBegin() + column.getColumnName() + generateSQL.getCharacterEnd() + "=" + generateSQL.parseStringToInsert(newValue);
				if (property==Constants.IdPROP_RDN) {
					
					if(objectChanged==null){
						objectChanged = new ObjectChanged();
						changesByIdNode.put(keyInd, objectChanged);
					}
					newRdnMap.put(""+idto+"#"+value, newValue);
					objectChanged.setProp(property);
					objectChanged.setOldValue(new StringValue(value));
					objectChanged.setNewValue(new StringValue(newValue));
				}else if(!Auxiliar.equals(value, newValue)){
					ObjectChanged oC=new ObjectChanged();
					oC.setProp(property);
					if(objectChanged!=null){
						oC.setOldIdo(objectChanged.getOldIdo());
						oC.setNewIdo(objectChanged.getNewIdo());
					}else{//Si llega sin objectChanged significa que se trata de un set por lo que oldIdo es igual a newIdo
						int ido=QueryConstants.getIdo(newTableIdValue, idto);
						oC.setOldIdo(ido);
						oC.setNewIdo(ido);
					}
					oC.setOldValue(new StringValue(value));
					oC.setNewValue(new StringValue(newValue));
					changesNoRdn.add(oC);
				}
				
				
			}
		}
		if (set.length()>0) {
			String sqlQuery = "UPDATE " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + 
			" SET " + set + 
			" WHERE " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + newTableIdValue + ";";
			DBQueries.executeUpdate(fcdb, sqlQuery);
		}
	}
	private String applyMatcher(int maxLength,String field, String value, Integer tableIdValue, Integer newTableIdValue, Integer curridNode) throws SQLException, NamingException {
		System.out.println("field " + field + ", value " + value );
		if(field.equals("email")){
			System.out.println("field " + field + ", value " + value );
		}
		String initialValue = value;
		if(value.contains(rdnTemporalDelegation)){
			String delegationRdn=InstanceService.getDelegationRdn(fcdb);
			if(delegationRdn!=null){
				value=value.replaceFirst(rdnTemporalDelegation, delegationRdn);
			}
		}
		
		Matcher matcher = rdnTemporalPattern.matcher(value);
		//System.out.println("Antes de aplicar matcher " + value);
		while (matcher.find()) {
			String group = matcher.group();
			
			String[] idoNegIdto = group.split("@");
			String idoNegativeStr = null;
			if (idoNegIdto.length==1) {
				idoNegativeStr = group.substring(1, group.length()-1);
			} else {
				idoNegativeStr = idoNegIdto[0].substring(1, idoNegIdto[0].length());
			}
			boolean putIdo = false;
			if (idoNegativeStr.startsWith("id")) {
				putIdo = true;
				idoNegativeStr = idoNegativeStr.substring(2,idoNegativeStr.length());
			}
			//System.out.println("idoNegativeStr " + idoNegativeStr);
			Integer idoNegative = Integer.parseInt(idoNegativeStr);
			//System.out.println("prefallo "+idoNegativeIdNode.containsKey(idoNegative));
			if(!idoNegativeIdNode.containsKey(idoNegative)) return value;
			int idNodeContained = idoNegativeIdNode.get(idoNegative);
			//System.out.println("idNodeContained " + idNodeContained);
			Element elementContained = elementsByIdNode.get(idNodeContained);
			/*try {
				System.out.println(jdomParser.returnNodeXML(elementContained));
			} catch (JDOMException e) {
				e.printStackTrace();
			}*/
			String tableIdContained = elementContained.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			Integer idtoContained = Integer.parseInt(elementContained.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
			
			if (field.equals(Constants.PROP_RDN) && tableIdValue!=null && tableIdContained.equals(tableIdValue.toString())) {
				// El rdn temporal llevaba una referencia al tableId del
				// elemento que deba ser sustituida si no se
				// habia hecho ya con un índice.
				
				String idoContained = String.valueOf(QueryConstants.getIdo(newTableIdValue, idtoContained));			
				value = value.replace(group, idoContained);
			} else {
				if (putIdo) {
					Integer tableIdContainedInt = Integer.parseInt(tableIdContained);
					if (tableIdContainedInt<0) {
						HashMap<String,String> propertyValue = (HashMap<String,String>)fieldsPendingResolution.get(curridNode);
						if (propertyValue==null) {
							propertyValue = new HashMap<String, String>();
							fieldsPendingResolution.put(curridNode, propertyValue);
						}
						propertyValue.put(field, initialValue);
						
						value = initialValue;
						break; //llamada desde processNewElementWithData: encuentra uno con un campo con idos negativos aun no resuelto, se resolvera a posteriori
							   //llamada desde resolveFieldsPendingResolution: no entra en esta parte del if
					} else {																
						String idoContained = String.valueOf(QueryConstants.getIdo(Integer.parseInt(tableIdContained), idtoContained));			

						value = value.replace(group, idoContained);
					}
				} else {
					String rdnContained = elementContained.getAttributeValue(Constants.PROP_RDN);
					//System.out.println("rdnContained " + rdnContained);
					value = value.replace(group, rdnContained);
				}
			}
		}
		
		//la propiedad documentos la decide una regla, por lo que no interviene GUI para recortar longitud, pero ademas su contenido puede incluir codigos temporarles
		//por lo que la regla no sabe realmente cual será la longitud final, aqui se comprueba para textos no memo
		if(maxLength>0&&value!=null&&value.length()>maxLength-1) value=value.substring(0,maxLength-3)+"..";
		//System.out.println("Despues de aplicar matcher " + value);
		return value;
	}
	/**
	 * Procesa un elemento que vena como acción nueva pero en realidad lo que
	 * se tiene que hacer es editar el valor de los campos incrementales sobre
	 * un registro ya existente.
	 * 
	 * @param element
	 *            Elemento que contiene la informacion con las DataProperties
	 *            incrementales.
	 * @param table
	 *            Tabla donde se tiene que insertar la informacion del elemento.
	 * @throws DataConversionException
	 *             Si al intentar obtener el valor de un atributo del XML se ha
	 *             producido un error de conversin de tipos.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataErrorException
	 * @return El tableId con el que el objeto existia en base de datos.
	 * @throws NoSuchColumnException 
	 */
	private int processIncrementalConflict(Element rootElement,Element element, Table table) throws DataConversionException, SQLException, NamingException, DataErrorException {
		String rdn = element.getAttributeValue(Constants.PROP_RDN);
		TableColumn rdnColumn = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		String sqlGetTableId = "SELECT " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + " FROM " + generateSQL.getCharacterBegin() + table.getName() 
							   + generateSQL.getCharacterEnd() + " WHERE "  + generateSQL.getCharacterBegin() + rdnColumn.getColumnName() + generateSQL.getCharacterEnd() + "='" + rdn + "';";
		Integer tableId = null;
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sqlGetTableId);
		if (queryResult.isEmpty()){
			throw new DataErrorException("No existe ningun registro en la tabla " + table.getName() + " cuyo RDN=" + rdn + ". No se sabe que elemento se tiene que actualizar por este motivo.");
		}
		// Cogemos el tableId del resultado de la consulta.
		tableId = Integer.parseInt(queryResult.get(0).get(0));
		// Modificamos el tableId del objeto al que hemos obtenido cnon la consulta anterior, para que se pueda comportar como un SET.
		element.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, tableId.toString());
		// Tratamos el objeto como si fuese un update
		processSetElementWithData(rootElement,element);
		
		return tableId;
	}

	private void processSetElementWithData(Element rootElement,Element element) throws DataErrorException, DataConversionException, SQLException, NamingException {		

		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		if (idtoAttribute == null) {
			throw new DataErrorException("Los elementos que representan objetos de una clase deben indicar mediante el atributo " + XMLConstants.ATTRIBUTE_IDTOm + " el identificador de la clase a la que pertenecen.");
		}
		int idto = idtoAttribute.getIntValue();
		
		Integer tableId = getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);
		if (tableId < 0 && element.getAttribute(XMLConstants.ATTRIBUTE_REFNODE)==null) {
			throw new DataErrorException("Las modificaciones de elementos no pueden venir con un tableId negativo. " + element.toString());
		}
		
		
		Table table = dataBaseMap.getTable(idto);
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		Set<Integer> fileProperties = classInfo.getFileProperties();
		if (! fileProperties.isEmpty()){
			processElementWithFileProperties(element, classInfo, XMLConstants.ACTION_SET);
		}
		Map<String, String> propertyValueFormat = new HashMap<String, String>();
		Map<String, String> propertyValue = new HashMap<String, String>();
		getPairsPropertyValueFromElement(element, table, propertyValueFormat, propertyValue, true);

		boolean newIndividual=false;
		String idoorder=element.getAttributeValue(XMLConstants.ATTRIBUTE_IDO_ORDER);
		if(idoorder!=null){
			int idoorderInt=Integer.parseInt(idoorder);
			newIndividual=idoorderInt<0;
		}
		
		String setsString = getFieldsAndValuesForUpdate(propertyValueFormat,newIndividual);
		if (setsString != null && !setsString.isEmpty()) {
			String sql = "UPDATE " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " SET " + setsString + " WHERE " + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + tableId + ";";
			DBQueries.executeUpdate(fcdb, sql);
		}
		
		Integer idNodeValue =  getCurrentIdNode(elementsByIdNode,element);
		if(idNodeValue==null) idNodeValue=getSourceIdNode(elementsByIdNode,element);
		processedIdNodes.add(idNodeValue);
		
		String keyInd = getKeyIndividual(null,newRdnMap,element);
		
		ObjectChanged objectChanged = changesByIdNode.get(keyInd);
		if (objectChanged == null) {
			//si es un cambio de rdn y este es temporal:
			String rdn = element.getAttributeValue(Constants.PROP_RDN);
			if (rdn!=null) {
				Matcher matcher = rdnTemporalPattern.matcher(rdn);
				if (matcher.find()) {
						objectChanged = new ObjectChanged();
						changesByIdNode.put(keyInd, objectChanged);
						objectChanged.setOldIdo(tableId);
						objectChanged.setNewIdo(QueryConstants.getIdo(tableId, idto));
				}
			}
		}
		applyMatcherAndUpdateAllFields(idto,false, element, propertyValue, table, null, tableId, objectChanged, keyInd,idNodeValue);

	}

	/**
	 * Inspecciona el elemento recibido para saber si se modifica alguna de las
	 * propiedades de tipo fichero que puede poseer un objeto de la clase del
	 * elemento.
	 * 
	 * @param element
	 *            Elemento que contiene las modificaciones a realizar sobre el
	 *            objeto.
	 * @param fileProperties
	 *            Propiedades de tipo fichero que puede contener el objeto.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	@SuppressWarnings("unchecked")
	private void processElementWithFileProperties(Element element, ClassInfo classInfo, String action) throws SQLException, NamingException {
		List<String> filesToDelete = new LinkedList<String>();
		List<String> filesToKeep = new LinkedList<String>();
		List<String> propertiesToConsult = new LinkedList<String>();
		boolean filePropertiesEdited = false;
		// Consultamos si se está editando alguna de las propiedades de tipo fichero
		for(Integer idProperty : classInfo.getFileProperties()){
			PropertyInfo property = classInfo.getProperty(idProperty);
			String propertyName = property.getName();
			if (property.getMaxCardinality() == 1){
				Attribute attribute = element.getAttribute(propertyName);
				if (attribute != null){
					// Tendremos que consultar posteriormente el fichero al que
					// referencia la propiedad antes de que la vayamos a editar
					propertiesToConsult.add(propertyName);
					if(propertyName.equals("archivo")) filesToKeep.add(attribute.getValue());//el nuevo valor de archivo no se puede eliminar aunque no haya cambiado
					filePropertiesEdited = true;
				}
			}else{
				// Con las propiedades de cardinalidad multiple, la modificación
				// vendr en un hijo de nombre data_property
				List<Element> dataPropertyElements = element.getChildren(XMLConstants.TAG_DATA_PROPERTY);
				for (Element dataPropertyElement : dataPropertyElements){
					String referencedProperty = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm);
					if (referencedProperty.equals(propertyName)){
						if (action.equals(XMLConstants.ACTION_SET)) {
							String oldValue = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_PROPERTYm + XMLConstants.OLD_PROPERTY);
							if (oldValue != null){
								// Si se está editando una propiedad de cardinalidad
								// multiple hay que decir el valor que tena
								// anteriormente la propiedad en el XML o no se
								// puede procesar. Nos facilita que no tenemos que
								// buscar dicho valor en base de datos.
								filesToDelete.add(oldValue);
								filePropertiesEdited = true;
							}
						} else if (action.equals(XMLConstants.ACTION_DEL)) {
							String value = dataPropertyElement.getAttributeValue(XMLConstants.ATTRIBUTE_VALUE);
							filesToDelete.add(value);
							filePropertiesEdited = true;
						}
					}
				}
			}
		}
		if (filePropertiesEdited){
			if (! propertiesToConsult.isEmpty()){
				// Si hay propiedades en la propia tabla de la clase de tipo
				// fichero que se están editando, tenemos que buscar el valor
				// que tienen dichas propiedades para saber que ficheros son
				// referenciados.
				Table table = dataBaseMap.getTable(classInfo.getIdto());
				String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
				String sql = "SELECT " + cB + Auxiliar.listToString(propertiesToConsult, cE + ", " + cB) + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + element.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
				List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
				for (List<String> objectData : queryResult){
					for (String value : objectData){
						if (value != null){
							// Añadimos el nombre del fichero a la lista de
							// ficheros que han de ser borrados
							filesToDelete.add(value);
						}
					}
				}
			}
			// Mandamos borrar todos los ficheros indicados.
			filesToDelete.removeAll(filesToKeep);
			deleteFiles(filesToDelete);
		}
	}

	/**
	 * Se encarga de borrar los ficheros indicados si existen en el disco duro.
	 * 
	 * @param filesToDelete
	 *            Nombres de los ficheros que han de ser borrados.<br>
	 *            Solo se ha de indicar el nombre del fichero, pues ya se conoce
	 *            en que carpeta están los ficheros.
	 */
	private void deleteFiles(List<String> filesToDelete) {
		File filesFolder = new File("../server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/" + Constants.folderUserFiles + "/" + fcdb.getBusiness());
		for (String fileName : filesToDelete){
			File file = new File(filesFolder, fileName);
			if (file.exists()){
				boolean deleted = file.delete();
				if (deleted){
					System.out.println("Se borra el fichero: " + file.getAbsoluteFile().getAbsolutePath());
				}else{
					System.err.println("No se pudo borrar el fichero " + file.getAbsoluteFile().getAbsolutePath());
				}
			}
			File smallFile = new File(filesFolder, Constants.smallImage + fileName);
			if (smallFile.exists()){
				boolean deleted = smallFile.delete();
				if (deleted){
					System.out.println("Se borra el fichero: " + smallFile.getAbsoluteFile().getAbsolutePath());
				}else{
					System.err.println("No se pudo borrar el fichero " + smallFile.getAbsoluteFile().getAbsolutePath());
				}
			}
		}
	}

	/**
	 * Se encarga de borrar todos los objetos marcados para ser borrados, es
	 * decir, los que aparecen en el conjunto {@link #objectsToDelete}.
	 * 
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataConversionException
	 * @throws DataErrorException
	 * @throws CardinalityExceedException 
	 * @throws IncoherenceInMotorException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws NotFoundException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws NoSuchColumnException 
	 */
	private void deleteObjects() throws DataErrorException, DataConversionException, SQLException, NamingException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException, NoSuchColumnException{
		boolean modifications = true;
		Map<ObjectIdentifier, DeletableObject> deletableObjects = new Hashtable<ObjectIdentifier, DeletableObject>();
		for (ObjectIdentifier id : objectsToDelete) {
			DeletableObject deletableObject = new DeletableObject(dataBaseMap, instanceService.getIk(), null, fcdb, id);			
			deletableObjects.put(id, deletableObject);
		}
		HashSet<ObjectIdentifier> yaEliminados=new HashSet<ObjectIdentifier>();
		
		while (modifications){
			List<LinkedObject> deletedObjects = new LinkedList<LinkedObject>();
			Iterator<ObjectIdentifier> it = objectsToDelete.iterator();
			while (it.hasNext()) {
				ObjectIdentifier id = it.next();
				DeletableObject deletableObject = deletableObjects.get(id);
				int code = deletableObject.delete();
				if (code == DeletableObject.NO_DELETEABLE){
					//A priori no es eliminable porque alguin le apunta, falta ver si ese alguien (el objectsReferencing que sigue) ya ha sido eliminado, en cuyo caso lo obvio
					Set<ObjectIdentifier> objectsReferencing = deletableObject.getLinksToNonDeletedObject(id);
					boolean allMarkedToDelete = true;
					for (ObjectIdentifier objectIdentifier : objectsReferencing) {
						if (!(objectsToDelete.contains(objectIdentifier)||yaEliminados.contains(objectIdentifier))){
							allMarkedToDelete = false;
							break;
						}
					}
					if (! allMarkedToDelete){
						// Alguno de los objetos que necesita que se borren para
						// poder borrarse no se va a borrar.
						throw new DataErrorException(DataErrorException.ERROR_DATA, deletableObject.getCause());
					}else{
						it.remove();
						deletableObjects.remove(id);
						yaEliminados.add(id);
					}
				}else if (code == DeletableObject.NO_ERROR){
					// El objeto se ha borrado o ya estaba borrado antes de llamar al metodo.
					it.remove();
					deletedObjects.addAll(deletableObject.getDeletedObjects());
					deletableObjects.remove(id);
				}else if (code == DeletableObject.ALREADY_DELETED){
					it.remove();
					deletableObjects.remove(id);
					yaEliminados.add(id);
				}
			}
			// Ahora tenemos que elimiar las posibles referencias a los objetos
			// borrados del resto de objetos marcados para borrar.
			for (DeletableObject deletableObject : deletableObjects.values()) {
				deletableObject.deleteReferencesToObjects(deletedObjects);
			}
			modifications = deletableObjects.size() > 0;
		}
		
	}

	/**
	 * Recorre la lista de los elementos que tienen un rdn que estaba en
	 * conflicto con uno de un objeto que se tena que borrar y les pone el rdn
	 * que les corresponde realmente.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private void rdnModifyLast() throws SQLException, NamingException{
		//System.out.println("rdnModifyLast");
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		for (Element element : rdnModifyLast) {
			Integer idto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
			String tableId = element.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
			String rdn = element.getAttributeValue(Constants.PROP_RDN);
			Table table = dataBaseMap.getTable(idto);
			
			String sql = "UPDATE " + cB + table.getName() + cE + " SET rdn=" + generateSQL.parseStringToInsert(rdn) + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + tableId;
			DBQueries.executeUpdate(fcdb, sql);
			
		}
	}

	/**
	 * Obtiene del elemento todos aquellos atributos que representan una
	 * DataProperty de la clase y va creando dos cadenas, una que lleva los
	 * nombres de las columnas donde están los datos y otra con los datos tal y
	 * como se tienen que indicar.
	 * 
	 * @param element
	 *            Elemento del que se consultan los atributos que se incluiran
	 *            en las listas.
	 * @param table
	 *            Tabla en la que tienen que almacenarse los datos
	 * @return La primera de las cadenas lleva los nombres de las columnas y la
	 *         segunda los valores. Ambas cadenas tienen ya incluidos los
	 *         parntesis.
	 * @throws DataErrorException
	 *             Excepcion que se da cuando no hay coherencia entre las tablas
	 *             y el XML.
	 */
	private String[] getFieldsAndValuesForInsert(Map<String, String> propertyValue, Table table) throws DataErrorException {
		String[] result = new String[4];

		// Cadena que va a ir conteniendo la lista de los nombres de las
		// columnas de la base de datos en las que se van
		// a insertar los valores encontrados.
		String fields = "(";
		// Cadena que indica los valores que se van a insertar.
		String values = "(";
		Set<String> propKSet=propertyValue.keySet();
		for (String field : propKSet) {
			String value = propertyValue.get(field);
			//Grabar un rdn temporal es innecesario puesto que se actualiza mas tarde, y puede dar problemas de unicidad con datos basura existentes, pero si es necesario
			//grabar algo cuando el rdn es la unica propiedad. Esto puede pasar tambien con un ref_node que se presenta antes que el nodo referenciado y solo incluye rdn (ejmplo no lote_pag en multiges)
			if(field.equals("rdn") && value.matches(".*&(id)?-?\\d+&.*") && propKSet.size()>1){
				continue;
			}
			
			//System.out.println("PREVIO ADAPT "+field+","+value);
			value=adaptReplicaValue(fcdb,value);
			//System.out.println("POST ADAPT "+field+","+value);
			if (fields.length() != 1) {
				fields += ",";
			}
			fields += generateSQL.getCharacterBegin() + field + generateSQL.getCharacterEnd();
			if (values.length() != 1) {
				values += ",";
			}
			values += value;
		}
		fields += ")";
		values += ")";

		result[0] = fields;
		result[1] = values;
		TableColumn column = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		result[2] = column.getColumnName();
		result[3] = propertyValue.get(column.getColumnName());
		
		return result;
	}

	/**
	 * metodo que construye una cadena que contiene los valores que se van a
	 * modificar de la manera: `column_name`=valor, `column_name2`=valor2, etc.
	 * 
	 * @param propertyValueFormat
	 *            Elemento del que se saca la informacion de los valores que se
	 *            han de incluir.
	 * @return Cadena que cotiene el texto que hay que poner tras el SET en la
	 *         consulta de UPDATE
	 * @throws DataErrorException
	 *             Si hay alguna incoherecia entre el XML y la base de datos.
	 */
	public static String adaptReplicaValue(FactoryConnectionDB fcdbParam,String value) throws DataErrorException{
		String resValue=value;
		if(value.equals("[currenttime]")){
			Calendar c = Calendar.getInstance();				
			resValue=""+c.getTimeInMillis()/Constants.TIMEMILLIS;
		}
		return resValue;
	}
	
	private String getFieldsAndValuesForUpdate(Map<String, String> propertyValueFormat,boolean newIndividual) throws DataErrorException {
		String result = "";
		for (String field : propertyValueFormat.keySet()) {
			
			//no debe actualizarse el campo destination en individuos que ya existen. Ido order es el ido original antes de resolvererse idos temporales al crear un individuo
			if(!newIndividual && field.equals(IQueryInfo.COLUMN_NAME_DESTINATION)) continue;
			
			String value = propertyValueFormat.get(field);
			value=adaptReplicaValue(fcdb,value);
			if (!result.isEmpty()) {
				result += ", ";
			}
			result += generateSQL.getCharacterBegin() + field + generateSQL.getCharacterEnd() + "=" + value;
		}
		return result;
	}

	/**
	 * Construye un mapa que asocia una columna con el valor que se le tiene que
	 * dar ya formateado para insertarlo correctamente en la base de datos.
	 * 
	 * @param element
	 *            Elemento del que se sacan los valores.
	 * @param table
	 *            Tabla en la que se tienen que insertar los datos.
	 * @param isUpdate
	 *            Indica si los valores se van a usar para una actualización de
	 *            valores de la base de datos.
	 * @return Mapa donde las claves son los nombres de los campos de la tabla y
	 *         los valores son los que venan en el XML.
	 * @throws DataErrorException
	 *             Si hay algun fallo en la coherencia entre el XML y la base de
	 *             datos.
	 */
	@SuppressWarnings("unchecked")
	
	public static Integer getSourceIdNode(Map<Integer, Element> elementsByIdNode,Element element){
		String atVal=element.getAttributeValue(XMLConstants.ATTRIBUTE_REFNODE);	
		boolean isRef=true;
		if(atVal==null){
			 atVal=element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);
			 isRef=false;
		}
		if(atVal==null) return null;
		
		Integer idNode=Integer.parseInt(atVal);
		if(!isRef && elementsByIdNode!=null) elementsByIdNode.put(idNode, element);//si es referencia no debo mapear idnode source con este nodo que no es fuente
		
		return idNode;
	}
	
	public static Integer getCurrentIdNode(Map<Integer, Element> elementsByIdNode,Element element){
		String atVal=element.getAttributeValue(XMLConstants.ATTRIBUTE_IDNODE);		
		if(atVal==null) return null;
		Integer currIdNode=Integer.parseInt(atVal);
		if(elementsByIdNode!=null) elementsByIdNode.put(currIdNode, element);
		return currIdNode;
	}
	
	private void getPairsPropertyValueFromElement(Element element, Table table, Map<String, String> propertyValueFormat, Map<String, String> propertyValue, boolean isUpdate) throws DataErrorException {
		
		Integer idNode = getCurrentIdNode(elementsByIdNode,element);
		//Set<String> rdnsDecremented = rdnDecrementedByIdto.get(table.getId());
		// Consultamos por si existe informacion sobre incrementales para este
		// elemento. De ser asi, este objeto sera distinto de nulo.
		
		//puede pasar que sea un ref node y no venga id node. No es logico que incrementales vengan en ref node, salvo en importaciones y solo en caso que vengan por ejemplo dos stock mismo rdn
		//puede pasar por ejemplo al importar dos albaranes ignorando el numero de albaran
		IncrementalData incrementalData = idNode!=null?incrementalDataByIdNode.get(idNode):null;
		
		List<Attribute> attributes = (List<Attribute>) element.getAttributes();
		for (Attribute attribute : attributes) {
			String attributeName = attribute.getName();
			//System.out.println("attributeName " + attributeName);
			Integer idProperty = dataBaseMap.getPropertyId(attributeName);
			if (idProperty == null){
				//Significa que es un atributo no vinculado con una propiedad, como puede ser TableId, idNode, idto, etc... La unica que nos interesa es Destintation
				if (attributeName.equals(XMLConstants.ATTRIBUTE_DESTINATIONm)){
					TableColumn column = table.getDataPropertyColumn(IQueryInfo.ID_DESTINATION);
					propertyValueFormat.put(column.getColumnName(), formatValue(attribute.getValue(), column.getColumnDataType()));
					propertyValue.put(column.getColumnName(), attribute.getValue());
				} else if (keepTableIds && !isUpdate && attributeName.equals(XMLConstants.ATTRIBUTE_TABLEID)){
					TableColumn column = table.getDataPropertyColumn(IQueryInfo.ID_TABLE_ID);
					Integer valueTableId = -Integer.parseInt(attribute.getValue());
					propertyValueFormat.put(column.getColumnName(), formatValue(String.valueOf(valueTableId), column.getColumnDataType()));
				}
				continue;
			}
			// Miramos si la DataProperty está externalizada a otra tabla.
//			System.out.println("table " + table.getName());
//			System.out.println("idProperty " + idProperty);
			if (table.isExternalizedProperty(idProperty)){
				throw new DataErrorException("Las data properties externalizadas como " + attributeName + " se deben indicar en nodos <dataproperty>");
			}else{
				// La propiedad no está externalizada, con lo cual buscamos la
				// columna donde se insertarn los datos.
				TableColumn column = table.getDataPropertyColumn(idProperty);
				if(column == null){
					throw new DataErrorException("La tabla " + table.getName() + " no tiene ninguna columna asociada a la propiedad [" + idProperty + "=" + attributeName + "]");
				}
				if (incrementalData != null  && incrementalData.getColumnIncrement(column) != null){
					// Se trata de una columna que tiene incremental. Si es un
					// UPDATE, directamente no modificamos esta columna.
					if (! isUpdate){
						// Ponemos valor cero. Los incrementales se tratan al
						// final del todo y en ese momento se sumar el valor
						// que vena en la creación del objeto.
						propertyValueFormat.put(column.getColumnName(), "0");
					}
				}else{
					if (idProperty.equals(Constants.IdPROP_PASSWORD) && (attribute.getValue()==null || !attribute.getValue().matches(".*"+rdnTemporalPattern.toString()+".*"))){
						propertyValueFormat.put(column.getColumnName(), generateSQL.getEncryptFunction(InstanceService.keyEncrypt, attribute.getValue()));
					}else{
						Integer idto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
						//System.out.println("element.getName " + element.getName() + ", " + idto);
//						if (idProperty.equals(Constants.IdPROP_RDN) && !isUpdate /*&& rdnsDecremented != null && rdnsDecremented.contains(attribute.getValue())*/) {
						propertyValue.put(column.getColumnName(), attribute.getValue());
						propertyValueFormat.put(column.getColumnName(), formatValue(attribute.getValue(), column.getColumnDataType()));
						if (idProperty.equals(Constants.IdPROP_RDN) && !isUpdate && idtosDecremented.contains(idto)) {
//							String tableId = element.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
//							propertyValue.put(column.getColumnName(), Constants.DEFAULT_RDN_CHAR + tableId + Constants.DEFAULT_RDN_CHAR);
//							propertyValueFormat.put(column.getColumnName(), formatValue(Constants.DEFAULT_RDN_CHAR + tableId + Constants.DEFAULT_RDN_CHAR, column.getColumnDataType()));
							rdnModifyLast.add(element);
//						}else{
//							propertyValue.put(column.getColumnName(), attribute.getValue());
//							propertyValueFormat.put(column.getColumnName(), formatValue(attribute.getValue(), column.getColumnDataType()));
						}
					}
				}
			}
		}
	}

	/**
	 * metodo que se encarga de buscar en que tabla se relacionan dos elementos
	 * dados sus datos más principales y la propiedad por la que se relacionan.<br>
	 * 
	 * @param parentIdto
	 *            Identificador de la clase del padre.
	 * @param parentTableId
	 *            Identificador del padre dentro de la tabla.
	 * @param idto
	 *            Identificador de la clase del nodo hijo.
	 * @param tableId
	 *            Identificador del hijo dentro de su tabla.
	 * @param propertyName
	 *            Nombre de la propiedad por la que el padre apunta al hijo.
	 * @param action
	 *            acción que se tiene que realizar con el vinculo.
	 * @throws NoSuchColumnException
	 *             Si se pregunta a una tabla por una columna que no tiene.
	 * @throws DataErrorException
	 *             Si existe incoherencia entre los datos del XML y los de la
	 *             base de datos.
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void processLink(Integer parentIdto, Integer parentTableId, Integer idto, Integer tableId, String propertyName, String action, boolean checkIsolated) throws DataErrorException, SQLException, NamingException {
		Table parentTable = dataBaseMap.getTable(parentIdto);
		Integer idProperty = dataBaseMap.getPropertyId(propertyName);
		Table linkTable = null;
		if (parentTable.isExternalizedProperty(idProperty)){
			// El vinculo se produce en un tabla externa.
			Set<Integer> propertyLocations = parentTable.getExternalizedPropertyLocations(idProperty);
			// Buscamos la tabla donde se produce el vinculo entre los dos
			// elementos.
			for (Integer propertyLocation : propertyLocations) {
				Table workingTable = dataBaseMap.getTable(propertyLocation);
				// Si workingTable == null => Idto de una vista asociacion.
				if (workingTable == null || (!propertyLocation.equals(idto) && !workingTable.isAssociation())){
					continue;
				}
				if (workingTable.isAssociation()){
					TableColumn[] rangeColumns = workingTable.getObjectPropertyColumn(idProperty, idto);
					if (rangeColumns == null){
						// Significa que esta tabla asociacion no puede apuntar al rango que queremos.
						continue;
					}
				}
				linkTable = workingTable;
			}
		}else{
			// El vinculo se produce en la tabla que representa a la clase de la
			// propiedad.
			linkTable = parentTable;
		}
		if (linkTable == null) {
			throw new DataErrorException("No se ha encontrado la manera de vincular dos objetos de las clases: " + parentTable.getName() + " y " + dataBaseMap.getClass(idto).getName());
		}
		if (action.equals(XMLConstants.ACTION_NEW) || (action.equals(XMLConstants.ACTION_SET) && linkTable.isAssociation())) {
			//System.out.println("tabla " + linkTable + ", prop " + propertyName);
			createLink(parentIdto, parentTableId, idto, tableId, idProperty, linkTable);
		} else if (action.equals(XMLConstants.ACTION_SET) && ! linkTable.isAssociation()) {
			modifyLink(parentIdto, parentTableId, idto, tableId, idProperty, linkTable);
		} else if (action.equals(XMLConstants.ACTION_DEL)) {
			deleteLink(parentIdto, parentTableId, idto, tableId, idProperty, linkTable, checkIsolated);
		} else if (action.equals(XMLConstants.ACTION_DELOBJECT)) {
			// TODO Borrar el objeto y todos los vinculos que tiene.
		}
	}

	/**
	 * Crea un vinculo entre dos objetos usando la tabla indicada sobre la
	 * propiedad indicada.
	 * 
	 * @param parentIdto
	 *            Identificador de la clase padre
	 * @param parentTableId
	 *            Identificador del objeto dentro de la tabla de la clase padre.
	 * @param idto
	 *            Identificador de la clase hija
	 * @param tableId
	 *            Identificador del objeto dentro de la tabla hija
	 * @param idProperty
	 *            Identificador numero de la propiedad por la que se
	 *            relacionan ambos elementos.
	 * @param linkTable
	 *            Tabla en la que se relacionan ambos elementos.
	 * @throws NoSuchColumnException
	 *             Si se pregunta a una tabla por una columna que no contiene.
	 * @throws DataErrorException
	 *             Si hay incoherencia entre el
	 * @throws NamingException
	 * @throws SQLException
	 */
	private void createLink(Integer parentIdto, Integer parentTableId, Integer idto, Integer tableId, Integer idProperty, Table linkTable) throws DataErrorException, SQLException, NamingException {
		Integer linkTableIdto = linkTable.getId();
		String sql;
		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		if (linkTableIdto.equals(parentIdto)) {
			// El vinculo se hace en la tabla origen de la propiedad
			TableColumn [] columns = linkTable.getObjectPropertyColumn(idProperty, idto);
			if (columns == null || columns[0] == null) {
				throw new DataErrorException("No se ha encontrado la columna que referencia a la propiedad " + idProperty + " en la tabla " + linkTable.getName());
			}
			sql = "UPDATE " + cB + linkTable.getName() + cE + " SET " + cB + columns[0].getColumnName() + cE
					+ "=" + tableId + " WHERE " + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + parentTableId + ";";
		} else if (linkTableIdto.equals(idto)) {
			// El vinculo se hace en la tabla a la que apunta la propiedad
			TableColumn [] columns = linkTable.getObjectPropertyColumn(idProperty, parentIdto);
			if (columns == null || columns[0] == null) {
				throw new DataErrorException("No se ha encontrado la columna que referencia a la propiedad " + idProperty + " en la tabla " + linkTable.getName());
			}
			sql = "UPDATE " + cB + linkTable.getName() + cE + " SET " + cB + columns[0].getColumnName() + cE
					+ "=" + parentTableId + " WHERE " + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + tableId + ";";
		} else {
			// Es una tabla asociacion
			TableColumn [] rangeColumns = linkTable.getObjectPropertyColumn(idProperty, idto);
			if (rangeColumns == null || rangeColumns[0] == null) {
				throw new DataErrorException("No se ha encontrado la columna del rango esperado [idto=" + idto +"] en la tabla asociacion " + linkTable.getName() + " para la propiedad [idProp=" + idProperty + "]");
			}
			TableColumn [] domainColumns = linkTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, parentIdto);
			if (domainColumns == null || domainColumns[0] == null){
				throw new DataErrorException("No se ha encontrado la columna del dominio [idto=" + parentIdto + "] en la tabla " + linkTable.getName());
			}
			TableColumn propertyColumn = linkTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
			
			// Query para comprobar que no exista ya dicho vinculo.
			String checkQuery = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + linkTable.getName() + cE + " WHERE " + cB + domainColumns[0].getColumnName() + cE + "=" + parentTableId + " AND " + cB
									+ rangeColumns[0].getColumnName() + cE + "=" + tableId;
			if (propertyColumn != null) {
				checkQuery += " AND " + cB + propertyColumn.getColumnName() + cE + "=" + idProperty;
			}
			List<List<String>> queryResult = DBQueries.executeQuery(fcdb, checkQuery);
			if (queryResult.size() == 0){
				sql = "INSERT INTO " + cB + linkTable.getName() + cE + " (" + cB + domainColumns[0].getColumnName() 
						+ cE + ", " + cB + rangeColumns[0].getColumnName() + cE;
				if (propertyColumn != null) {
					sql += ", " + cB + propertyColumn.getColumnName() + cE;
				}
				sql += ") VALUES (" + parentTableId + ", " + tableId;
				if (propertyColumn != null) {
					sql += ", " + idProperty;
				}
				sql += ")";
			}else{
				sql = null;
			}
		}
		if (sql != null){
			if (sql.startsWith("U")) {
				// Es un update
				DBQueries.executeUpdate(fcdb, sql);
			} else {
				// Es el insert
				DBQueries.execute(fcdb, sql);
			}
		}
	}

	/**
	 * Modifica un vinculo existente para que apunte al nuevo objeto.
	 * 
	 * @param parentIdto
	 *            Idto de la clase dominio de la relación mediante la propiedad
	 *            indicada.
	 * @param parentTableId
	 *            Identificador del objeto de la clase dominio que se quiere
	 *            asociar mediante el vinculo de la propiedad indicada.
	 * @param idto
	 *            Identificador de la clase del rango de la propiedad a la que
	 *            pertenece el objeto con el que queremos crear el vinculo..
	 * @param tableId
	 *            Identificador del objeto de la clase del rango con la que se
	 *            quiere enlazar el objeto
	 * @param idProperty
	 *            Identificador de la propiedad con la que queremos unir los dos
	 *            objetos.
	 * @param linkTable
	 *            Tabla en la que se va a almacenar el vinculo
	 * @throws NoSuchColumnException
	 *             Si se intenta preguntar por una columna que no está en la
	 *             tabla.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataErrorException
	 *             Si hay incoherencia entre el XML y la base de datos.
	 */
	private void modifyLink(Integer parentIdto, Integer parentTableId, Integer idto, Integer tableId, Integer idProperty, Table linkTable) throws SQLException, NamingException, DataErrorException {
		if (linkTable.isAssociation()) {
			TableColumn[] domainColumns = linkTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, parentIdto);
			TableColumn[] rangeColumns = linkTable.getObjectPropertyColumn(idProperty, idto);
			if (domainColumns == null || rangeColumns == null || domainColumns[0] == null || rangeColumns[0] == null){
				throw new DataErrorException("La tabla asociacion " + linkTable.getName() + " no relaciona de manera correcta las clases [idtoDominio=" + parentIdto + ", idtoRango=" + idto + "]");
			}
			String sql = "UPDATE " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " SET " + generateSQL.getCharacterBegin() + rangeColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "="
					+ tableId + " WHERE " + generateSQL.getCharacterBegin() + domainColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + parentTableId + " AND " + generateSQL.getCharacterBegin() 
					+ rangeColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + tableId + ";";
			DBQueries.executeUpdate(fcdb, sql);
		} else {
			if (parentIdto.equals(linkTable.getId())) {
				// La relación se produce en la tabla del objeto del dominio de la propiedad.
				TableColumn [] rangeColumns = linkTable.getObjectPropertyColumn(idProperty, idto);
				if (rangeColumns == null || rangeColumns[0] == null){
					throw new DataErrorException("[DATABASEMANAGER ERROR] No se encuentra la columna que apunta a [idto=" + idto + "] en la tabla que se ha indicado que tendria el vinculo [tableName=" + linkTable.getName() + "]");
				}
				String sql = "UPDATE " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " SET " + generateSQL.getCharacterBegin() + rangeColumns[0].getColumnName() 
						+ generateSQL.getCharacterEnd() + "=" + tableId + " WHERE " + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + parentTableId + ";";
				DBQueries.executeUpdate(fcdb, sql);
			} else if (idto.equals(linkTable.getId())) {
				// La relación se produce en la tabla del rango.
				TableColumn [] domainColumns = linkTable.getObjectPropertyColumn(idProperty, parentIdto);
				if (domainColumns == null || domainColumns[0] == null){
					throw new DataErrorException("[DATABASEMANAGER ERROR] No se ha encontrad la columna que apunta al dominio de la propiedad [idProp=" + idProperty + "] en la tabla del rango [tableName=" + linkTable.getName() +"]");
				}
				String sql = "UPDATE " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " SET " + generateSQL.getCharacterBegin() + domainColumns[0].getColumnName()
						+ generateSQL.getCharacterEnd() + "=" + parentTableId + " WHERE " + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + tableId + ";";
				DBQueries.executeUpdate(fcdb, sql);
			} else {
				throw new DataErrorException("El vinculo entre dos objetos solo se puede hacer en la tabla del dominio, en la tabla del rango o en un tabla asociacion");
			}
		}
	}

	/**
	 * Borra el vinculo entre los dos objetos indicados y, si el elemnto al que
	 * apuntaba era estructural, comprueba si estaba aislado para intentar
	 * borrarlo.
	 * 
	 * @param parentIdto
	 *            Identificador de la clase del dominio del vinculo.
	 * @param parentTableId
	 *            Identificador del objeto de la clase de la que surje el
	 *            vinculo.
	 * @param idto
	 *            Identificador de la clase del rango del vinculo.
	 * @param tableId
	 *            Identificador del objeto de la clase del rango.
	 * @param idProperty
	 *            Propiedad por la que se relacionan los dos objetos.
	 * @param linkTable
	 *            Tabla en la que se encuentra reflejado el vinculo en la base
	 *            de datos.
	 * @throws NoSuchColumnException
	 *             Si se ha preguntado a una tabla por una columna que no
	 *             contiene.
	 * @throws DataErrorException
	 *             Si hay algun error en la base de datos con respecto a los
	 *             datos que han llegado.
	 * @throws NamingException
	 *             Si surge algun error al comprobar si un elemento está
	 *             aislado.
	 * @throws SQLException
	 *             Si surge algun error al comprobar si un elemento está
	 *             aislado.
	 */
	private void deleteLink(Integer parentIdto, Integer parentTableId, Integer idto, Integer tableId, Integer idProperty, Table linkTable, boolean checkIsolated) throws DataErrorException, SQLException, NamingException {
		if (parentIdto == null || idto == null) {
			System.err.println("[DATABASEMANAGER ERROR] Ha llegado a deleteLink parentIdto o idto nulo al metodo deleteLink(Integer, Integer, Integer, Integer, Integer, Table)");
			return;
		}
		if (linkTable.isAssociation()){
			// Hay que borrar la entrada en la tabla asociacion.
			TableColumn [] domainColumns = linkTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, parentIdto);
			TableColumn [] rangeColumns = linkTable.getObjectPropertyColumn(idProperty, idto);
			TableColumn propertyColumn = linkTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
			if (domainColumns == null || rangeColumns == null || domainColumns[0] == null || rangeColumns[0] == null){
				String alias;
				String parentAlias;
				try {
					alias = instanceService.getIk().getAliasOfClass(idto, uTask);
					parentAlias = instanceService.getIk().getAliasOfClass(parentIdto, uTask);
				} catch (Exception e) {
					throw new DataErrorException("No se han encontrado los alias de las clases " + linkTable.getId() + " y " + parentIdto);
				}
				throw new DataErrorException(DataErrorException.ERROR_DATA, "No hay informacion en la tabla asociacion " + linkTable.getName() + " sobre el dominio=" + parentAlias + " y el rango=" + alias);
			}
			String sql = "DELETE FROM " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " WHERE " + generateSQL.getCharacterBegin() + domainColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + parentTableId + " AND " + generateSQL.getCharacterBegin() + rangeColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + tableId;
			if (propertyColumn != null){
				sql += " AND " + generateSQL.getCharacterBegin() + propertyColumn.getColumnName() + generateSQL.getCharacterEnd() + "=" + idProperty; 
			}
			sql += ";";
			DBQueries.execute(fcdb, sql);
		}else if (linkTable.getId().equals(parentIdto)){
			// Poner a nulo la columna que apunta al objeto.
			TableColumn [] propertyColumns = linkTable.getObjectPropertyColumn(idProperty, idto);
			if (propertyColumns == null || propertyColumns[0] == null){
				String alias;
				try {
					alias = instanceService.getIk().getAliasOfProperty(idto, idProperty, uTask);
				} catch (Exception e) {
					throw new DataErrorException("No se ha encontrado el alias de la propiedad : " + idProperty);
				}
				throw new DataErrorException(DataErrorException.ERROR_DATA, "No hay informacion en la tabla " + linkTable.getName() + " sobre la propiedad " + alias);
			}
			String sql = "UPDATE " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " SET " + generateSQL.getCharacterBegin() + propertyColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=NULL WHERE " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + parentTableId + " AND " + generateSQL.getCharacterBegin() + propertyColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + tableId + ";";
			DBQueries.executeUpdate(fcdb, sql);
			//
		}else if (linkTable.getId().equals(idto)){
			// Poner a nulo la columna que apunte a parentIdto.
			TableColumn [] propertyColumns = linkTable.getObjectPropertyColumn(idProperty, parentIdto);
			if (propertyColumns == null || propertyColumns[0] == null){
				String alias;
				try {
					alias = instanceService.getIk().getAliasOfProperty(idto, idProperty, uTask);
				} catch (Exception e) {
					throw new DataErrorException("No se ha encontrado el alias de la propiedad : " + idProperty);
				}
				throw new DataErrorException(DataErrorException.ERROR_DATA, "No hay informacion en la tabla " + linkTable.getName() + " sobre la propiedad " + alias);
			}
			String sql = "UPDATE " + generateSQL.getCharacterBegin() + linkTable.getName() + generateSQL.getCharacterEnd() + " SET " + generateSQL.getCharacterBegin() + propertyColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=NULL WHERE " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + tableId + " AND " + generateSQL.getCharacterBegin() + propertyColumns[0].getColumnName() + generateSQL.getCharacterEnd() + "=" + parentTableId + ";";
			DBQueries.executeUpdate(fcdb, sql);
		}else{
			// Error
			throw new SQLException("La relación entre dos objetos de dos clases debe realizarse en la tabla de una de las clases o en una tabla asociacion", new RelationalDataBaseException("La tabla " + linkTable.getName() + " no puede ser la tabla que almacene la relación [DOMINIO=" + parentIdto + "; RANGO=" + idto + "; ID_PROP=" + idProperty + "]"));
		}
		// Si la propiedad por la que se unan los dos elementos era
		// estructural, comprobamos si el elemento se queda
		// aislado para borrarlo en ese caso.
		if (checkIsolated && dataBaseMap.isStructuralProperty(idProperty)) {
			checkIsolatedObject(idto, tableId);
		}
	}

	/**
	 * Comprubeba que el objeto que está siendo apuntado mediante una propiedad
	 * estructural, no está aislado. Si está aislado, lo intenta borrar de
	 * manera que se captura todo posible fallo, porque es posible que haya
	 * alguna foreign key apuntndolo que no está contemplada en la tabla, es
	 * decir, que la Foreign Key está en una tabla asociacion.
	 * 
	 * @param idto
	 *            Identificador de la tabla de la que hay que revisar el objeto
	 * @param tableId
	 *            Identificador del objeto que hay que comprobar si está
	 *            aislado.
	 * @throws DataErrorException
	 *             Si se produce algun error de datos relacionado con las
	 *             comprobaciones a base de datos.
	 * @throws NoSuchColumnException
	 * @throws NamingException
	 *             Si hay algun problema al conectar con la base de datos.
	 * @throws SQLException
	 *             Si hay algun problema al conectar con la base de datos.
	 */
	private void checkIsolatedObject(Integer idto, Integer tableId) throws DataErrorException, SQLException, NamingException {
		Table table = dataBaseMap.getTable(idto);
		if (table == null) {
			throw new DataErrorException("[DATABASEMANAGER ERROR] El número " + idto + " no se corresponde con ninguna tabla que haya mapeada");
		}
		// Vamos a iterar sobre todas las columnas, escogiendo aquellas que
		// tienen un dominio, es decir, apuntan a otra
		// tabla. stas son las que contienen una Foreign Key.
		List<String> columnNamesWithForeignProperties = new ArrayList<String>();
		Set<TableColumn> columns = table.getAllColumns();
		for (TableColumn tableColumn : columns) {
			if (table.isForeignProperty(tableColumn.getIdProperty())) {
				columnNamesWithForeignProperties.add(tableColumn.getColumnName());
			}
		}
		if (columnNamesWithForeignProperties.isEmpty()) {
			return;
		}
		String sql = "SELECT ";
		boolean first = true;
		// Hacemos un contador de columnas que hemos consultado para despues
		// saber hasta que numero hay que llegar a la
		// hora de analizar el resultset.
		int numberOfColumns = 0;
		for (String string : columnNamesWithForeignProperties) {
			if (!first) {
				sql += ", ";
			}
			sql += generateSQL.getCharacterBegin() + string + generateSQL.getCharacterEnd();
			first = false;
			numberOfColumns++;
		}
		sql += " FROM " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " WHERE " + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd()
				+ "=" + tableId + ";";
		// Comprobamos que todas las columnas contienen un NULL e intentamos
		// borrar. Si alguna contiene algo que no sea
		// NULL, significa que el elemento no está aislado y por tanto no lo
		// tenemos que borrar.
		boolean isolated = true;

		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		if(queryResult.size()>0){				
			for (String value : queryResult.get(0)) {
				if (value != null){
					isolated = false;
					break;
				}
			}
		}

		if (isolated) {
			sql = "DELETE FROM " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " WHERE " + generateSQL.getCharacterBegin() + Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd()
					+ "=" + tableId + ";";
			// Vamos a intentar borrar el elemento capturando las excepciones,
			// pues si alguna tabla asociacion restringe
			// el borrado, no es un error intentar borrar, simplemente no
			// podemos porque sigue relacionado y seguimos la
			// ejecucion.
			try {
				DBQueries.execute(fcdb, sql);
			} catch (SQLException e) {
				System.out.println("Se intento borrar el elemento de la tabla " + table.getName() + " con tableId=" + tableId + " pero se detect que no estaba aislado an.");
			}
		}
	}

	/**
	 * Una vez finalizado el resto de acciones, se procede a incrementar los
	 * valores que viniesen como incrementales, pues ya se habrn resuelto todos
	 * los tableId, y se podran bloquear todos los objetos implicados.
	 * 
	 * @throws DataErrorException
	 *             Si no se encuentra toda la informacion necesaria para poder
	 *             finalizar de manera correcta la actualización/inserción de
	 *             datos.
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
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private void processIncrementals() throws DataErrorException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException{
		/* No hace falta bloquear incrementales, un sql campo=+campo es bloqueado por postgre y se resuelve esperando desbloqueo de otro proceso. La dirty lectura no afetca
		 * if (!migration) {
			boolean locked = tryLockIncrementals();
			while(!locked){
				// Dormimos durante 10 segundos antes de volver a intentar bloquear para dar tiempo a otro proceso a terminar con sus bloqueos.
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
				locked = tryLockIncrementals();
			}
		}*/
		Iterator<Integer> itrNodes=incrementalDataByIdNode.keySet().iterator();
		while(itrNodes.hasNext()){
			Integer idCurrNode=itrNodes.next();
			IncrementalData incrementalData=incrementalDataByIdNode.get(idCurrNode);
			Map<TableColumn, Double> increments = incrementalData.getAllIncrements();
			int idto = incrementalData.getIdto();
			String rdn=incrementalData.getRdn();
			String keyInd=""+idto+"#"+rdn;
			Integer tableId=newTableIdMap.get(keyInd);
			if(tableId==null) tableId = incrementalData.getTableId();
			if (tableId.intValue() < 0){				
				rdn=newRdnMap.get(keyInd);
				if(rdn==null)	throw new DataErrorException("Hay un incremental de la clase [idto=" + idto + "] para el que no se ha encontrado el identificador del objeto.");
				if(!rdn.contains("&"))	throw new DataErrorException("Hay un incremental de la clase [idto=" + idto + "] rdn incorrecto "+rdn);
				//keyInd=""+idto+"#"+rdn;
				//tableId=newTableIdMap.get(keyInd);	
			}
			processIncremental(increments, idto, tableId);
		}
		// Desbloqueamos todos los objetos que habiamos bloquedo para hacer los
		// incrementos cuando terminamos.
		unlockIncrementals(new LinkedList(incrementalDataByIdNode.values()));
	}

	/**
	 * Desbloquea los objetos indicados.
	 * 
	 * @param objects
	 *            Lista de objetos que hay que desbloquear.
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
	 */
	private void unlockIncrementals(List<IncrementalData> objects) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		for (IncrementalData incrementalData : objects) {
			int idto = incrementalData.getIdto();
			int tableId = -incrementalData.getTableId();
			int ido = QueryConstants.getIdo(tableId, idto);
			instanceService.unlockObject(ido, idto, user);
		}
	}

	/**
	 * Hace la actualización del registro de base la base de datos que contiene
	 * el incremental dado.
	 * 
	 * @param increments
	 *            Informacion sobre los incrementales que se tienen que aplicar
	 *            sobre un solo objeto de base de datos.
	 * @param idto
	 *            Identificador de la clase a la que pertenece el objeto sobre
	 *            el que se han de aplicar los incrementales.
	 * @param tableId
	 *            Identificador del objeto de la clase sobre el que se han de
	 *            aplicar los incrementos.
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	private void processIncremental(Map<TableColumn, Double> increments, int idto, int tableId) throws DataErrorException, SQLException, NamingException {
		Table table = dataBaseMap.getTable(idto);
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		String sql = "UPDATE " + cB + table.getName() + cE + " SET ";
		boolean first = true;
		for (TableColumn column : increments.keySet()) {
			if (! first){
				sql += ", ";
			}
			Double incrementValue = increments.get(column);
			switch (column.getColumnDataType()) {
			case Constants.IDTO_INT:
				int increment = incrementValue.intValue();
				//sql += cB + column.getColumnName() + cE + "=" + cB + column.getColumnName() + cE + "+(" + increment + ")" ;
				//Necesario el case para casos en el que el valor es nulo debido a una migracion de un campo que no existia anteriormente
				sql += cB + column.getColumnName() + cE + "=(case when "+ cB + column.getColumnName() + cE + " is null then " + increment +" else " + cB + column.getColumnName() + cE + "+(" + increment + ") end)" ;
				break;
			case Constants.IDTO_DOUBLE:			
//				sql += cB + column.getColumnName() + cE + "=" + cB + column.getColumnName() + cE + "+(" + incrementValue.toString() + ")";
//				set campo=round((campo+x)::numeric,8)
				//sql += cB + column.getColumnName() + cE + "=round((" + cB + column.getColumnName() + cE + "+(" + incrementValue.toString() + ")" + ")::numeric,8)";
				//Necesario el case para casos en el que el valor es nulo debido a una migracion de un campo que no existia anteriormente
				sql += cB + column.getColumnName() + cE + "=(case when "+ cB + column.getColumnName() + cE + " is null then " + incrementValue.toString() +" else round(("+ cB + column.getColumnName() + cE +"+(" + incrementValue.toString() + ")" + ")::numeric,8) end)";
				break;
			default:
				throw new DataErrorException("Se ha intentando hacer un incremento sobre una columna cuyo tipo de datos no es ni INT ni DOUBLE: [Tabla=" + table.getName() + ", Columna=" + column.getColumnName() + "]");
			}
			first = false;
		}
		sql += " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + tableId;
		DBQueries.executeUpdate(fcdb, sql);
	}

	/**
	 * Intenta bloquear todos los objetos que tenan un incremental. Si no
	 * consigue bloquearlos todos, los debloquea y sale.
	 * 
	 * @return Devuelve <code>true</code> si ha conseguido bloquear todos los
	 *         objetos que tenan incrementales.
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
	 */

	/**
	 * Intenta bloquear un objeto con 2 reintentos separados por un corto
	 * intervalo de tiempo.
	 * 
	 * @param tableId
	 *            Identificador del objeto de la clase especificada que queremos
	 *            bloquear.
	 * @param idto
	 *            Identificador de la clase del objeto que queremos bloquear.
	 * @return devolvera <code>true</code> si ha conseguido bloquar el objeto.
	 */
	private boolean lockObject(int tableId, int idto) {
		boolean locked = false;
		int ido = QueryConstants.getIdo(tableId, idto);
		for (int i = 0 ; i < 3 && ! locked ; i ++){
			try {
				instanceService.lockObject(ido, idto, user);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {}
				continue;
			}
			// Si consigue llegar aque es que no ha saltado excepción al
			// intentar bloquear y por tanto se ha bloqueado con exito el
			// objeto.
			locked = true;
		}
		
		return locked;
	}

	/**
	 * Consulta en la base de datos cul ha sido el último índice de tableId
	 * usado para insertar un registro en la tabla especificada.
	 * 
	 * @param tableName
	 *            Nombre de la tabla sobre la que se quiere saber el último
	 *            valor de índice usado.
	 * @return último valor de tableId usado para insertar en la tabla indicada.
	 * @throws NamingException
	 * @throws SQLException
	 */
	private Integer getLastTableId(String tableName) throws SQLException, NamingException {
		return getLastTableId(fcdb,tableName);
	}

	public static Integer getLastTableId(FactoryConnectionDB fcdb,String tableName) throws SQLException, NamingException {
		String sql = GenerateSQL.getLastTableId(tableName);
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		Integer tableIdValue = Integer.parseInt(queryResult.get(0).get(0));
		return tableIdValue;
	}
	/**
	 * Dada una cadena y un tipo de dato, devuelve otra cadena con el formato
	 * que debe usarse para indicar a la base de datos el valor de forma
	 * correcta.
	 * 
	 * @param value
	 *            Cadena a formatear.
	 * @param columnType
	 *            Tipo con el que se ha de formatear el valor.
	 * @return Cadena formateada lista para ser puesta como valor a introducir
	 *         en la base de datos.
	 */
	private String formatValue(String value, int columnType) {
		String result;
		switch (columnType) {
		case Constants.IDTO_STRING:
		case Constants.IDTO_MEMO:
		case Constants.IDTO_IMAGE:
		case Constants.IDTO_FILE:
			result = generateSQL.parseStringToInsert(value);
			break;
		default:
			result = value;
			break;
		}
		return result;
	}
	
	/**
	 * Genera un identificador para la replica.
	 * 
	 * @return número que se va a usar para identificar esta transacción de la
	 *         réplica.
	 * @throws SQLException
	 * @throws NamingException
	 */
	private int generateIdReplica() throws SQLException, NamingException {
		// coge el siguiente de Replica_Autonum
		int autonum = 0;
		String random = user + "/" + String.valueOf((new Random()).nextInt());
		String sql = "insert into Replica_Autonum(code) values ('" + random
				+ "')";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, false);
		sql = "Select id FROM Replica_Autonum "
				+ /* WITH(nolock) */"WHERE code='" + random + "'";

		Statement st = null;
		ResultSet rs = null;
		//System.out.println("INSERT ROW");
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			rs.next();
			autonum = rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}
		return autonum;
	}
	
	/**
	 * Recorre el XML entero, aplicando los filtros a los RDN que lo necesiten y
	 * que tengan definido un índice.
	 * 
	 * @param idtosUsed
	 *            Conjunto de todos los idtos que aparencen en el XML.
	 * @throws DataErrorException
	 *             Si hay errores en el formato del XML.
	 * @throws OperationNotPermitedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CardinalityExceedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncompatibleValueException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws IncoherenceInMotorException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws NotFoundException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws ApplicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws CommunicationException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws RemoteSystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws SystemException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws InstanceLockedException
	 *             Excepcion asociada al bloqueo de un idto.
	 * @throws DataConversionException
	 *             Si el formato del valor de un atributo del XML no es el
	 *             esperado.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 */
	private void generateAllRdn(Set<Integer> idtosUsed,Element rootElement) throws DataErrorException, InstanceLockedException,
			SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException,
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException,
			DataConversionException, SQLException, NamingException, NoSuchColumnException {
		// Bloqueamos en principio todos los idtos que participan en el XML,
		// pues no sabemos cuales de ellos tendran realmente un índice.
		//System.out.println("Inicio DatabaseManager.generateAllRdn(), idtosUsed " + idtosUsed);
		Map<Integer, Integer> hSpecificIdto = new HashMap<Integer, Integer>();
		HashSet<Integer> specificIdtos = getSpecificIdtos(idtosUsed, hSpecificIdto); //en este metodo ya excluimos los que no tienen índice
		//System.out.println("specificIdtos " + specificIdtos);
		if (specificIdtos.size()>0) {


				Map<Integer, List<IndexFilter2>> filters = indexFilterFunctions.getIndexsByIdto(specificIdtos);
				
				usedIndexes = new HashSet<IndexFilter2>();
				indexDeleted = new HashMap<Integer, Set<String>>();
				List<Element> elementsWithData = new ArrayList<Element>(elementsByIdNode.values());
				for (Element element : elementsWithData) {
					/*try {
						System.out.println(jdomParser.returnNodeXML(element));
					} catch (JDOMException e) {
						e.printStackTrace();
					}*/
					Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
					if (actionAttribute == null) {
						throw new DataErrorException("Todos los elementos que representan a un objeto tienen que llevar una acción asociada.");
					}
					
					boolean getIdto = false;
					Integer idto = null;
					String actionValue = actionAttribute.getValue();
					//System.out.println("actionValue " + actionValue);
					boolean continuar = true;
					if (actionValue.equals(XMLConstants.ACTION_NEW)) {
						String rdn = element.getAttributeValue(Constants.PROP_RDN);
						//System.out.println("rdn " + rdn);
						if (rdn!=null) {
							Attribute tableId = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
							Integer tableIdValue = tableId.getIntValue();
							continuar = false;
							Matcher matcher = rdnTemporalPattern.matcher(rdn);
							while (matcher.find()) {
								String group = matcher.group();
								String[] idoNegIdto = group.split("@");
								String idoNegativeStr = null;
								if (idoNegIdto.length==1) {
									idoNegativeStr = group.substring(1, group.length()-1);
								} else {
									idoNegativeStr = idoNegIdto[0].substring(1, idoNegIdto[0].length());
								}
								//System.out.println("idoNegativeStr " + idoNegativeStr);
								if (!idoNegativeStr.startsWith("id")) {
									int idoNegative = Integer.parseInt(idoNegativeStr);
									//int idNodeContained = idoNegativeIdNode.get(idoNegative);
									//System.out.println("idNodeContained " + idNodeContained);
									//Element elementContained = elementsByIdNode.get(idNodeContained);
									//String tableIdContained = elementContained.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
									
									//String tableIdContained= ""+getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);
									//if (tableIdContained.equals(tableIdValue.toString())) {
									String keyInd=getKeyIndividual(idto, newRdnMap, element);
									if(!newRdnMap.containsKey(keyInd)){
										if (idoNegIdto.length==1) {
											getIdto = true;
										} else {
											idto = Integer.parseInt(idoNegIdto[1].substring(0, idoNegIdto[1].length()-1));
										}
										continuar = true;
										break;
									}
								}
							}
						} else
							getIdto = true;
					} else
						getIdto = true;
					
					//si se quiere obtener el rdn del actual -> continuar
					if (continuar) {
						if (getIdto) {
							Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
							idto = idtoAttribute.getIntValue();
						}
						Integer specificIdto = hSpecificIdto.get(idto);
						//System.out.println("specificIdto " + specificIdto);
						if (specificIdto == null) {
							// Significa que este elemento no tiene índice asociado, con lo
							// cual no lo seguimos procesando porque no
							// le vamos a poder cambiar el rdn, aunque sea temporal, hasta
							// más adelante en la ejecucion.
							continue;
						}
						List<IndexFilter2> filtersForIdto = filters.get(specificIdto);
						//List<IndexFilter2> filtersForIdto = getIndexesForIdto(filters, idto);
						if (filtersForIdto == null) {
							// Significa que este elemento no tiene índice asociado, con lo
							// cual no lo seguimos procesando porque no
							// le vamos a poder cambiar el rdn, aunque sea temporal, hasta
							// más adelante en la ejecucion.
							continue;
						}
			
						//Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
						//String rdnValue = rdnAttribute.getValue();
						//Matcher matcher = rdnTemporalPattern.matcher(rdnValue);
						Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
						String rdn = null;
						if (rdnAttribute!=null)
							rdn = rdnAttribute.getValue();
						
						if(!replication){//Si no viene desde una replica aplicamos la logica de indices. Si viene de una replica no tenemos que hacerlo ya que los indices son para datos locales
							
							int decremento=0;
							if (actionValue.equals(XMLConstants.ACTION_NEW) && rdn!=null && rdn.contains("&") || actionValue.equals(XMLConstants.ACTION_DELOBJECT)) { 
								// Se comprueba si el rdn que ha llegado tiene partes
								// temporales. De ser asi, hay que resolverlas.
								// comprobar tambien filtrado (propFilter y miEmpresa)
								if(actionValue.equals(XMLConstants.ACTION_DELOBJECT)) decremento=2;//decremento el que se suma en creacion + el decremento 1 en si
								
								applyFilter(rootElement,element, idto, filtersForIdto,actionValue,decremento);
							}
						}
					}
				}
				

			
		} else
			System.out.println("En esta transacción no se utilizan índices");
		System.out.println("Fin DatabaseManager.generateAllRdn()");
	}
	
	/**Dado un conjunto de idtos usados devuelve un conjunto de los idtos más especficos que tienen índice, es decir, 
	 * por cada idto se obtiene el idto con el índice más cercano a este por herencia, si no tiene índice no se añade al conjunto.*/
	private HashSet<Integer> getSpecificIdtos(Set<Integer> idtosUsed, Map<Integer, Integer> hSpecificIdto) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		HashSet<Integer> specificIdtos = new HashSet<Integer>();
		for (Integer idtoUsed : idtosUsed) {
			Integer specificIdto = getSpecificIdto(idtoUsed);
			//System.out.println("***********idtoUsed " + idtoUsed + ", specificIdto " + specificIdto);
			if (specificIdto!=null) {
				specificIdtos.add(specificIdto);
				hSpecificIdto.put(idtoUsed, specificIdto);
			}
		}
		return specificIdtos;
	}
	private Integer getSpecificIdto(int idto) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		Integer specificIdto = null;
		HashMap<Integer,Integer> idtoPeso = new HashMap<Integer, Integer>();
		int menorPeso = Integer.MAX_VALUE;
		HashSet<Integer> superiors = getSuperiorsIndexed(idto, idtoPeso);
		if (superiors.size()>0) {
			Iterator<Integer> it = superiors.iterator();
			while (it.hasNext()) {
				int idtoIndex = it.next();
				int peso = idtoPeso.get(idtoIndex);
				//System.out.println("idtoIndex " + idtoIndex + ", peso " + peso);
				if (peso<menorPeso) {
					menorPeso = peso;
					specificIdto = idtoIndex;
				}
			}
		}
		//System.out.println("specificIdto " + specificIdto);
		return specificIdto;
	}
	private HashSet<Integer> getSuperiorsIndexed(int idto, HashMap<Integer,Integer> idtoPeso) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		HashSet<Integer> superiors = new HashSet<Integer>();
		HashSet<Integer> procesados = new HashSet<Integer>();
		int level = 1;
		getSuperiorsItera(level, idto, superiors, procesados, idtoPeso);
		return superiors;
	}
	private void getSuperiorsItera(int level, int idto, HashSet<Integer> superiors, HashSet<Integer> procesados, HashMap<Integer,Integer> idtoPeso) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		procesados.add(idto);
		//en migration se importa ontologia e individuos con rdn ya formados y por tanto sin aplicar indices
		if (!migration && indexFilterFunctions.isIndex(idto)) {
			superiors.add(idto);
			idtoPeso.put(idto, level);
			//System.out.println("peso para idto " + idto + ", level " + level);
		}
		List<Integer> parents = dataBaseMap.getClass(idto).getInmediateParents();
		Iterator<Integer> it = parents.iterator();
		while (it.hasNext()) {
			Integer idtoSup = (Integer)it.next();
			if (!procesados.contains(idtoSup))
				getSuperiorsItera(level+1, idtoSup, superiors, procesados, idtoPeso);
		}
	}
	
	/**
	 * Aplica el filtro adecuado al rdn que contiene el elemento pasado. Además,
	 * modifica el XML para que refleje el cambio de rdn, y guarda el cambio en
	 * el mapa pertinente indexndolo por el id_node de este elemento.
	 * 
	 * @param element
	 *            Elemento que contiene toda la informacion del objeto.
	 * @param filters
	 *            Filtros aplicables a esta clase.
	 * @return Cadena a la que se le ha aplicado el filtro si hubiera sido
	 *         necesario, si no se ha aplicado ningun filtro devolvera la cadena
	 *         del rdn que habia llegado en el elemento de entrada.
	 * @throws NoSuchColumnException
	 *             Si se ha producido un error al buscar el filtro porque se ha
	 *             preguntado por una columna que no existe a una tabla mapeada.
	 * @throws NamingException
	 *             Error en la consulta a base de datos.
	 * @throws SQLException
	 *             Error en la consulta a base de datos.
	 * @throws DataConversionException
	 *             El valor de un atributo del elemento no tiene el formato
	 *             esperado.
	 * @throws DataErrorException
	 *             Se a producido algun tipo de error no esperado porque no se
	 *             ha respetado las normas para representar los datos en el XML.
	 */
	private void applyFilter(Element rootElement,Element element, int idto, List<IndexFilter2> filters,String actionValue ,int decremento)
			throws DataErrorException, DataConversionException, SQLException, NamingException, NoSuchColumnException {
		boolean canBeInDB=actionValue.equals(XMLConstants.ACTION_DELOBJECT);
		IndexFilter2 applicableFilter = indexFilterFunctions.getApplicableFilter(element, elementsByIdNode, filters, canBeInDB, false);
		//System.out.println("applicableFilter " + applicableFilter);
		if (applicableFilter == null) {
			throwErrorNoIndexFound(element, filters);
		}
		String[] returned = constructTemporalPrefix(element, idto, applicableFilter, true);
		constructNewIndex(rootElement,element, idto, applicableFilter, returned, true, decremento,actionValue);
	}

	/**
	 * Construye el mensaje de error con el motivo por el que no se ha
	 * encontrado un índice aplicable a un objeto y lanza el error.
	 * 
	 * @param element
	 *            Elemento al que no se le ha podido aplicar un índice.
	 * @param filters
	 *            Lista de filtros aplicables a la clase del elemento
	 * @throws DataErrorException
	 *             Error el el que se especifica por que no se ha podido aplicar
	 *             un índice.
	 */
	private void throwErrorNoIndexFound(Element element, List<IndexFilter2> filters) throws DataErrorException {
		String propertyFilterName = null;
		HashSet<String> propertyFilterValueSet = new HashSet<String>();
		HashSet<String> businessSet = new HashSet<String>();
		HashMap<String, HashSet<String>> businessPropertyFilterValue = new HashMap<String, HashSet<String>>();
		for (IndexFilter2 indexFilter2 : filters) {
			if (propertyFilterName == null) {
				propertyFilterName = indexFilter2.getFilterField();
			}
			String myBusiness = indexFilter2.getRdnMyBusiness();
			if(myBusiness==null) {
				if (propertyFilterName != null) {
					propertyFilterValueSet.add(indexFilter2.getFilterValue());
				}
			} else {
				businessSet.add(myBusiness);
				HashSet<String> businessPropertyFilterValueSet = businessPropertyFilterValue.get(myBusiness);
				if (businessPropertyFilterValueSet==null) {
					businessPropertyFilterValueSet = new HashSet<String>();
					businessPropertyFilterValue.put(myBusiness, businessPropertyFilterValueSet);
				}
				if (propertyFilterName != null) {
					businessPropertyFilterValueSet.add(indexFilter2.getFilterValue());
				}
			}
		}
		String alias;
		String propertyAlias = null;
		try {
			int idto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
			alias = instanceService.getIk().getAliasOfClass(idto, uTask);
			if (propertyFilterName!=null) {
				int idProperty = dataBaseMap.getPropertyId(propertyFilterName);
				propertyAlias = instanceService.getIk().getAliasOfProperty(idto, idProperty, uTask);
			}
		} catch (Exception e) {
			throw new DataErrorException("Error al intentar obtener el alias de la clase: " + element.getName());
		}
		String exception = null;
		if (propertyAlias==null) { //no existen filtros para este índice, pero s q existe un índice y si no lo cumple es xq existe un filtro de empresa
			exception = "No cumple la condición de 'Mi Empresa' de el/los índice/s definidos para " + alias + ". " +
					"Debe tener uno de los siguientes valores: " + Auxiliar.hashSetStringToString(businessSet, ",");
		} else {
			exception = "El campo " + propertyAlias + " de " + alias + " debe tener uno de los siguientes valores: ";
			if (propertyFilterValueSet.size()>0) {
				exception += Auxiliar.hashSetStringToString(propertyFilterValueSet, ",");
				if (businessPropertyFilterValue.size()>0)
					exception += ";";
			}
			String exceptionTmp = "";
			for (String business : businessPropertyFilterValue.keySet()) {
				HashSet<String> businessPropertyFilterValueSet = businessPropertyFilterValue.get(business);
				if (exceptionTmp.length()>0)
					exceptionTmp += ";";
				exceptionTmp += Auxiliar.hashSetStringToString(businessPropertyFilterValueSet, ",") + " para la empresa " + business;
			}
			exception += exceptionTmp;
		}
		throw new DataErrorException(DataErrorException.ERROR_DATA, exception);
	}

	/**
	 * Comprueba si hay que reindexar un elemento que viene con rdn
	 * 
	 * @param element
	 * @param applicableFilter
	 * @throws NamingException
	 * @throws SQLException
	 * @throws NoSuchColumnException
	 * @throws DataConversionException
	 * @throws DataErrorException
	 */

	/**
	 * Modifica el elemento para que refleje el rdn que se ha calculado a partir
	 * del índice dado.
	 * 
	 * @param element
	 *            Elemento al que se tiene que aplicar el índice.
	 * @param index
	 *            índice a aplicar.
	 * @throws DataErrorException
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataConversionException
	 * @throws NoSuchColumnException 
	 */
	private String constructNewIndex(Element rootElement,Element element, int idto, IndexFilter2 index, String[] returned, boolean change, int decrementos,String actionValue)
			throws DataErrorException, DataConversionException, SQLException, NamingException, NoSuchColumnException {
		String newRdn = "";
		String temporalPrefixField = index.getTemporalPrefixField();
		String temporalPrefix = "";
		if (temporalPrefixField != null && !temporalPrefixField.isEmpty()) {
			temporalPrefix = getTemporalValue(returned, index);
			newRdn += temporalPrefix;
		}
		String prefixField = index.getPrefixField();
		String prefixFieldValue = null;
		if (prefixField != null && !prefixField.isEmpty()) {
			prefixFieldValue = findPrefixFieldValue(element, prefixField, change);
		}
		Integer newIndexValue = getNewIndexValue(temporalPrefix, returned, idto, index, prefixFieldValue);
		if (prefixFieldValue != null ) {
			newRdn += prefixFieldValue;
		}
		
		String prefix = index.getPrefixValue();
		if (prefix != null && !prefix.isEmpty()) {
			newRdn += prefix;
		}				
		
		String rdn = getRdn(element);
		
		Attribute tableId = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
		Integer tableIdValue = tableId == null ? null : tableId.getIntValue();
		String rdnTmp = rdn;		
		
		if (!temporalPrefix.isEmpty()) {
			//System.out.println("cambia indice, action " + Integer.parseInt(returned[0]));
			updateIndexOfTemporalPrefix(temporalPrefix, returned, index);
		}
		String sufix = index.getSufixValue()==null?"":index.getSufixValue();
		
		int minDigits = index.getMinDigits()==null?0:index.getMinDigits();
		//actualizar indices para futuras llamadas
		//las decisiones sobre el nuevo valor, o si decrementar o no, incluso cual es el contador generado, deben realizarse en SQL y siempre los valores se modifican 
		//mediante incrementos respecto al valor actual de base de datos, eso evita dirty reads (leer un dato de contador actual incorrecto porque no se ve las modificaciones de otra transaccion
		//procesandose antes que la actual y esta bloqueando, al hacer las modificaciones relativas en SQL el gestor transaccional de postgre retrasa las modificaciones en cascada
		//segun van finalizando las transacciones, y no es necesario gestionar bloqueos desde la aplicacion
		if (change) {												
			String lastTemporalPrefix = index.getLastTemporalPrefix();
			
			String update = "UPDATE \""  + Constants.CLS_INDICE.toLowerCase() + "\" SET inicio_contador=";
				
			
			if(temporalPrefix.isEmpty()) 	update+=" inicio_contador+1-"+decrementos;
			else						update+=" (CASE WHEN \"último_prefijo_temporal\"<>'"+temporalPrefix.replaceAll("'", "''") + "' THEN 2 ELSE inicio_contador+1-"+decrementos+" END) ";
			
			if (lastTemporalPrefix!=null)
				update += ", " + generateSQL.getCharacterBegin() + "último_prefijo_temporal" + generateSQL.getCharacterEnd() + "='" + lastTemporalPrefix.replaceAll("'", "''") + "'";
			update += " WHERE " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID  + generateSQL.getCharacterEnd() + "=" + index.getIndexId();
			
			if(actionValue.equals(XMLConstants.ACTION_DELOBJECT))
				update+=" AND '"+newRdn+"'|| (CASE WHEN "+minDigits+"<>0 THEN lpad((inicio_contador-1)::text,"+minDigits+",'0') ELSE inicio_contador::text END)||'"+sufix+"'='"+rdn+"'";
			
			update+=" RETURNING (CASE WHEN \"último_prefijo_temporal\"<>'"+temporalPrefix.replaceAll("'", "''") + "' THEN 1 ELSE inicio_contador-(1-"+decrementos+") END);";
			
			//retorna valor descontado incremento, de esa manera capturo el valor resultado de otras transacciones que hayan bloqueado
			newIndexValue=DBQueries.executePostgreInsert(fcdb, update);
			if(newIndexValue!=null) index.setIndexValue(newIndexValue);						
		}			
		if(newIndexValue!=null){
			if (minDigits!=0)
				newRdn += StringUtils.leftPad(String.valueOf(newIndexValue), minDigits, '0');
			else
				newRdn += newIndexValue;
		}
		
		//System.err.println("rdnTemp"+rdnTmp+" rdnTemporalPattern"+rdnTemporalPattern);
		Matcher matcher = rdnTemporalPattern.matcher(rdnTmp);
		Integer currIdto=element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm).getIntValue();
		String keyInd=""+currIdto+"#"+rdnTmp;
		//reemplazo sin comprobar, ya que todas las apariciones seran de este ido
		while (matcher.find()) {
			String group = matcher.group();
			String[] idoNegIdto = group.split("@");
			String idoNegativeStr = null;
			if (idoNegIdto.length==1) {
				idoNegativeStr = group.substring(1, group.length()-1);
			} else {
				idoNegativeStr = idoNegIdto[0].substring(1, idoNegIdto[0].length());
			}
			if (!idoNegativeStr.startsWith("id")) {			
				//int idoNegative = Integer.parseInt(idoNegativeStr);
				//int idNodeContained = idoNegativeIdNode.get(idoNegative);
				//Element elementContained = elementsByIdNode.get(idNodeContained);
				//String tableIdContained = elementContained.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
				//String tableIdContained= ""+getTableId(idto,newRdnMap,newTableIdMap,rootElement,elementsByIdNode,element);
				
				//if (tableIdContained!=null && tableIdContained.equals(tableIdValue.toString()))
				if(!newRdnMap.containsKey(keyInd))
					newRdn = rdnTmp.replace(group, newRdn);
			}
		}
								
		newRdn += sufix;				
		
		//System.out.println("newRdn " + newRdn);
		
		//no puedo usar idto porque es el idto que se fuerza para localizar el indice, puede venir forzado en rdn con @idto, que significa en caso de ambiguedad como albarán-factura se fuerza que clase superior utilizar		
		
		if (change && !actionValue.equals(XMLConstants.ACTION_DELOBJECT)) {
			newRdnMap.put(keyInd, newRdn);//no puedo mapear nuevo rdn en caso delobject, que solo se procesa para decrementar indice pero no cambia rdn
			element.setAttribute(Constants.PROP_RDN, newRdn);
			
			
			Attribute idNodeAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
			//String keyInd = getKeyIndividual(null,newRdnMap,element);
			
			ObjectChanged objectChanged = changesByIdNode.get(keyInd);
			if (objectChanged == null) {
				objectChanged = new ObjectChanged();
				changesByIdNode.put(keyInd, objectChanged);
				
				Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
				Integer idtoValue = idtoAttribute.getIntValue();
				objectChanged.setOldIdo(QueryConstants.getIdo(tableIdValue, idtoValue));
				objectChanged.setNewIdo(QueryConstants.getIdo(tableIdValue, idtoValue));
			}
			objectChanged.setProp(Constants.IdPROP_RDN);
			objectChanged.setOldValue(new StringValue(rdn));
			objectChanged.setNewValue(new StringValue(newRdn));			
		}
		
		
		usedIndexes.add(index);
		
		return newRdn;
	}

	/**
	 * Busca, en primera instancia, en el elemento XML el valor que puede tener
	 * la propiedad. Si tiene la propiedad puede aparecer como atributo o como
	 * hijo de primer nivel del elemento actual. Si no está ah, hay que buscar
	 * en base de datos el valor que pueda tener dicha propiedad, teniendo las
	 * mismas consideraciones, es decir, puede ser DataProperty o
	 * ObjectProperty. En el caso de ser ObjectProperty, hay que tener en cuenta
	 * que lo que realmente queremos es el rdn del objeto al que apuntamos.
	 * 
	 * @param element
	 *            Elemento que contiene la informaición del objeto al que se le
	 *            está aplicando el índice.
	 * @param prefixField
	 *            Nombre de la propiedad que a en el prefijo.
	 * @return Texto que tiene la propiedad indicada o <code>null</code> si no
	 *         se ha encontrado la informacion.
	 * @throws DataConversionException
	 *             Si un atributo tiene un formato para su valor que no se esperaba.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException 
	 */
	@SuppressWarnings("unchecked")
	private String findPrefixFieldValue(Element element, String prefixField, boolean useElement) 
			throws DataConversionException, DataErrorException, SQLException, NamingException, NoSuchColumnException {
		String result = null;
		Attribute idtoAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_IDTOm);
		Integer idtoValue = idtoAttribute.getIntValue();
		
		Table elementTable = dataBaseMap.getTable(idtoValue);
		Integer idProperty = dataBaseMap.getPropertyId(prefixField);
		//558 es delegacion, hay indices que la usan sin que exista ese campo en la clase indexada, en cuyo caso la clas IndexFilterFunctions2 tomara la delegacion de la aplicacion
		
		boolean isObjectProperty = elementTable.isObjectProperty(idProperty);
		
		if(idProperty.intValue()==558&&!isObjectProperty){
			return InstanceService.getDelegationRdn(fcdb);
		}
			
		if (!isObjectProperty) {
			Attribute prefixPropertyAttribute = null;
			if (useElement) {
				prefixPropertyAttribute = element.getAttribute(prefixField);
				if (prefixPropertyAttribute == null) {
					// Si no aparece la propiedad como tal, vamos a ver si al menos la
					// propiedad con su valor antiguo, es decir prefixField@old="..."
					prefixPropertyAttribute = element.getAttribute(prefixField + XMLConstants.OLD_PROPERTY);
				}
			}
			if (prefixPropertyAttribute == null) {
				// buscarlo en base de datos
				result = indexFilterFunctions.searchPropertyValueInDb(prefixField, element);
			} else {
				result = prefixPropertyAttribute.getValue();
			}
		} else {
			// Lo que tenemos que poner en el resultado es la propiedad rdn de dicho elemento vinculado. 
			// Como es probable que no venga en el XML, tendremos que buscarla en base de datos
			Attribute prefixPropertyAttribute = null;
			if (useElement) {
				List<Element> children = new LinkedList<Element>(element.getChildren());
				for (Element childElement : children) {
					Element workingElement = childElement;
					Attribute propertyAttribute = workingElement.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm);
					if (propertyAttribute == null) {
						propertyAttribute = workingElement.getAttribute(XMLConstants.ATTRIBUTE_PROPERTYm + XMLConstants.OLD_PROPERTY);
					}
					if (propertyAttribute == null || !Auxiliar.equals(propertyAttribute.getValue(), prefixField)) {
						// Si no lo hemos encontrado ni como valor antiguo ni como
						// nuevo, nos vamos al siguiente hijo.
						continue;
					}
					// Si llegamos aque, significa que hemos encontrado un elemento
					// vinculado por la propiedad indicada en el XML y tenemos 
					// que ver si tenemos el RDN de dicho objeto.
					Attribute childNodeIdentifierAttribute = workingElement.getAttribute(XMLConstants.ATTRIBUTE_IDNODE);
					if (childNodeIdentifierAttribute == null){
						// Debe ser un nodo vinculo que referencia a otro y el identificador
						// del nodo referenciado tiene que estar en el atributo ref_node.
						childNodeIdentifierAttribute = workingElement.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
					}
					Integer childIdNodeValue = childNodeIdentifierAttribute.getIntValue();
					if (workingElement.getAttribute(XMLConstants.ATTRIBUTE_IDTOm) == null) {
						// Se trata de un nodo vinculo y tenemos que obtener el
						// objeto que realmente contiene la informacion.
						workingElement = elementsByIdNode.get(childIdNodeValue);
					}
					prefixPropertyAttribute = workingElement.getAttribute(Constants.PROP_RDN);
					if (prefixPropertyAttribute == null) {
						prefixPropertyAttribute = workingElement.getAttribute(Constants.PROP_RDN + XMLConstants.OLD_PROPERTY);
					}
					if (prefixPropertyAttribute == null) {
						// Hay que buscarlo en base de datos.
						try {
							result = indexFilterFunctions.searchPropertyValueInDb("rdn", workingElement);
						} catch (DataErrorException e) {
							;
						}
					} else {
						result = prefixPropertyAttribute.getValue();
					}
					if (result!=null)
						break;
				}
			}
			if (result == null) {
				// Hay que buscarlo en base de datos.
				// Como no sabemos el idto, hay que buscar esa propiedad en las tablas a las que apunta
				
				Attribute tableIdAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);
				if(tableIdAttribute!=null){
					Integer tableId = tableIdAttribute.getIntValue();
					boolean isExternalizedProperty = elementTable.isExternalizedProperty(idProperty);
					if (!isExternalizedProperty) {
						List<String> columnsNames = elementTable.getColumnNamesContainingProperty(idProperty);
						for (String columnName : columnsNames) {
							Integer idto = elementTable.getColumnDomain(columnName);
							if (idto!=null) {
								Table range = dataBaseMap.getTable(idto);
								//sql con inner join de columnName en mi tabla con el tableId de range
								boolean isInTableDomain = true;
								result = searchPropertyValueInDb(elementTable.getName(), range.getName(), columnName, isInTableDomain, tableId);
								if (result!=null)
									break;
							}
						}
					} else {
						Set<Integer> propertyLocation = elementTable.getExternalizedPropertyLocations(idProperty);
						for (Integer idto : propertyLocation) {
							Table range = dataBaseMap.getTable(idto);
							if (range!=null) {
								if (range.isAssociation()) {
									throw new DataErrorException("La propiedad prefijo de un índice no debe tener rango multiple");
								} else {
									TableColumn[] tableColumn = range.getObjectPropertyColumn(idProperty, elementTable.getId());
									if (tableColumn!=null) {
										String columnName = tableColumn[0].getColumnName();
										//sql con inner join de tableId en mi tabla con columnName de range
										boolean isInTableDomain = false;
										result = searchPropertyValueInDb(elementTable.getName(), range.getName(), columnName, isInTableDomain, tableId);
										if (result!=null)
											break;
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Busca en base de datos el rdn en una tabla rango unida a una tabla dominio.
	 */
	private String searchPropertyValueInDb(String tableDomain, String tableRange, String columnName, boolean isInTableDomain, int tableId)
			throws DataErrorException, SQLException, NamingException {
		String result = null;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		String sql = "SELECT " + cB + tableRange + cE + "." + cB + Constants.PROP_RDN + cE + 
			" FROM " + cB + tableRange + cE + 
			" INNER JOIN " + cB + tableDomain + cE + " ON(";
		
		if (isInTableDomain){
			sql += cB + tableRange + cE + "." + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + cB + tableDomain + cE + "." + cB + columnName + cE + " AND "+ cB + tableDomain + cE + "." + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + tableId;
		} else {
			sql += cB + tableDomain + cE + "." + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + cB + tableRange + cE + "." + cB + columnName + cE + " AND "+ cB + tableRange + cE + "." + cB + Table.COLUMN_NAME_TABLEID + cE + "=" + tableId;
		}
		
		sql += ")";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				result = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}
		return result;
	}

	/**
	 * Construye el texto que se tiene que añadir como prefijo temporal al rdn
	 * del elemento pasado. No se van a hacer comprobaciones de si hay que
	 * aplicar dicho filtro en este metodo, se supone que esas condiciones se
	 * han comprobado antes de intentar construir el prefijo temporal.<br>
	 * Se pueden dar tres casos muy especficos:
	 * <ul>
	 * <li>El valor de la propiedad temporal que se está usando para construir
	 * el prefijo tiene un valor menor que el que aparece como último usado (una
	 * vez aplicada la máscara indicada). -> <i>Hay que buscar en base de datos
	 * cual fue el último índice usado con el mismo prefijo, y poner el
	 * siguiente. Se devuelve <code>null</code> porque se modifica directamente
	 * el element.</i></li>
	 * <li>El valor de la propiedad temporal que se está usando tiene el mismo
	 * valor que el último usado -> <i>Se devuelve el prefijo a usar y ya
	 * está.</i></li>
	 * <li>El valor de la propiedad temporal que se está usando tiene un valor
	 * mayor que el último usado -> <i>Se reincia el contador del índice a 1, se
	 * modifica el contador de aos si es necesario y, por último, se cambia el
	 * último prefijo temporal usado al que hemos calculado ahora.</i></li>
	 * </ul>
	 * 
	 * @param element
	 *            Elemento al que hay que construirle el prefijo temporal.
	 * @param index
	 *            índice usado para construir el prefijo temporal.
	 * @return returned, array de String que almacena el prefijo temporal que hay 
	 * 		   que usar.
	 * @throws DataErrorException
	 *             Si no se encuentra un valor para la propiedad temporal.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataConversionException
	 */
	private String[] constructTemporalPrefix(Element element, int idto, IndexFilter2 index, boolean useElement)
			throws DataErrorException, DataConversionException, SQLException, NamingException {
		String[] returned = new String[3];
		String propertyName = index.getTemporalPrefixField();
		String temporalPrefixField = propertyName;
		if (temporalPrefixField != null && !temporalPrefixField.isEmpty()) {
			// Construimos un objecto que representa la fecha a partir del dato
			// obtenido para trabajar con mayor facilidad.
			long temporalFieldLongValue = findTemporalPrefixFieldValue(element, index, useElement);
			if (temporalFieldLongValue == -1) {
				String alias;
				String propertyAlias;
				try {
					alias = instanceService.getIk().getAliasOfClass(idto, uTask);
					int idProperty = dataBaseMap.getPropertyId(propertyName);
					propertyAlias = instanceService.getIk().getAliasOfProperty(idto, idProperty, uTask);
				} catch (Exception e) {
					throw new DataErrorException("Error al intentar obtener el alias de la clase: " + element.getName() + " o de la propiedad: " + propertyName);
				}
				throw new DataErrorException(DataErrorException.ERROR_DATA, "No se ha encontrado valor para la propiedad " + propertyAlias + " en la clase "
								+ alias + " para el tableId=" + element.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID));
			}
			indexFilterFunctions.getValueTemp(temporalFieldLongValue, index.getTemporalPrefixMask(), index.getYearCount(), 
					index.getLastTemporalPrefix(), returned);
		}
		return returned;
	}
	
	/**
	 * Devuelve el prefijo temporal a partir del parámetro array de String.
	 */
	private String getTemporalValue(String[] returned, IndexFilter2 index) {
		String result = null;
		if (returned[1]!=null) {
			result = returned[1];
			//recuperar el valueTempContYear si existe
			if (index.getYearCount()!=null)
				result = returned[2];
			//System.out.println("result " + result);
		}
		return result;
	}
	
	/**
	 * Obtiene el nuevo contador de índice dependiendo del prefijo temporal.
	 */
	private Integer getNewIndexValue(String temporalPrefix, String[] returned, int idto, IndexFilter2 index, String prefixFieldValue)
			throws DataErrorException, DataConversionException, SQLException, NamingException {
		int indexValue = index.getIndexValue();
		if (temporalPrefix!=null && returned[1]!=null) {
			int action = Integer.parseInt(returned[0]);
			if (action==IndexFilterFunctions2.RESTORE) {
				indexValue = 1;
			} 	//Discontinuado: Search last sería buscar ultimo elemento indexado, en vez de pillar el contador del propio indice
				
		}
		return indexValue;
	}

	/**
	 * Actualiza contadores de índices.
	 */
	private void updateIndexOfTemporalPrefix(String temporalPrefix, String[] returned, IndexFilter2 index)
			throws DataErrorException, DataConversionException, SQLException, NamingException {
		int action = Integer.parseInt(returned[0]);
		if (returned[1]!=null) {
			if (index.getLastTemporalPrefix()==null || action==IndexFilterFunctions2.RESTORE) {
				index.setLastTemporalPrefix(temporalPrefix); //como ultimo valor lo guardamos con el ao real
				//System.out.println("last " + temporalPrefix);
			}
			if (action==IndexFilterFunctions2.RESTORE) {
				if (index.getYearCount()!=null) {
					//System.out.println("incrementa ao");
					Integer increment = Integer.parseInt(returned[3]);
					index.incrementYearCount(increment);
				}
			}
		}
	}

	/**
	 * Busca el entero largo que representa la fecha en milisegundos. Primero la
	 * bsca en el XML y depus la busca en base de datos si se trata de un SET.
	 * 
	 * @param element
	 *            Elemento que tiene que contener la informacion para calcular
	 *            el valor que representa la fecha.
	 * @param index
	 *            índice en el que se indica la propiedad que contiene la
	 *            informacion temporal.
	 * @return Entero largo positivo si se encuentra el número que representa la
	 *         fecha o -1 si no se encuentra dicho valor.
	 * @throws DataErrorException
	 *             Si los datos expresados en el XML no se corresponden con los
	 *             almacenados en la base de datos.
	 * @throws NamingException
	 * @throws SQLException
	 * @throws DataConversionException
	 *             Si el dato no tiene un formato correcto.
	 */
	private long findTemporalPrefixFieldValue(Element element, IndexFilter2 index, boolean useElement) throws DataErrorException, SQLException, NamingException, DataConversionException {
		long result = -1;
		Attribute temporalAttribute = null;
		String propertyName = index.getTemporalPrefixField();
		if (useElement) {
			temporalAttribute = element.getAttribute(propertyName);
			if (temporalAttribute == null) {
				temporalAttribute = element.getAttribute(propertyName + XMLConstants.OLD_PROPERTY);
			}
		}
		Attribute actionAttribute = element.getAttribute(XMLConstants.ATTRIBUTE_ACTION);
		String actionValue = actionAttribute.getValue();
		int elementIdto = Integer.parseInt(element.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
		if (temporalAttribute == null && !actionValue.equals(XMLConstants.ACTION_NEW)) {
			Table elementTable = dataBaseMap.getTable(elementIdto);
			TableColumn column = elementTable.getColumnByName(propertyName);
			if (column == null) {
				throw new DataErrorException("La tabla " + elementTable.getName() + " no tiene ninguna columna denominada: " + propertyName);
			}
			String sql = "SELECT " + generateSQL.getCharacterBegin() + column.getColumnName() + generateSQL.getCharacterEnd()
					+ " FROM " + generateSQL.getCharacterBegin() + elementTable.getName()
					+ generateSQL.getCharacterEnd() + " WHERE " + generateSQL.getCharacterBegin()
					+ Table.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "="
					+ element.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID) + ";";

			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = fcdb.createConnection(true);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				rs.next();
				result = rs.getLong(1) * Constants.TIMEMILLIS;
				if (rs.wasNull()) {
					result = -1;
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con != null)
					fcdb.close(con);
			}
			// Lo añadimos al XML como valor antiguo por si se necesita el dato
			// para cualquier otro procesamiento, que
			// no haya que buscarlo otra vez en base de datos.
			if (useElement)
				element.setAttribute(propertyName + XMLConstants.OLD_PROPERTY, String.valueOf(result));
		} else if (temporalAttribute == null && actionValue.equals(XMLConstants.ACTION_NEW)) {
			// Se trata de un action="new" donde no se está especificando un
			// dato necesario, con lo cual lanzamos un
			// error.
			String alias;
			String propertyAlias;
			try {
				alias = instanceService.getIk().getAliasOfClass(elementIdto, uTask);
				//System.out.println("propertyName " + propertyName);
				int idProperty = dataBaseMap.getPropertyId(propertyName);
				propertyAlias = instanceService.getIk().getAliasOfProperty(elementIdto, idProperty, uTask);
			} catch (Exception e) {
				e.printStackTrace();
				throw new DataErrorException("Error al intentar obtener el alias de la clase: " + element.getName() + " o de la propiedad: " + propertyName);
			}
			throw new DataErrorException(DataErrorException.ERROR_DATA, "El índice de la clase: " + alias + " requiere que la propiedad " + propertyAlias + " tenga valor.");
		} else {
			// temporalAttribute tiene valor, con lo que cogemos el tiempo en
			// milis de ah.
			result = temporalAttribute.getLongValue() * Constants.TIMEMILLIS;
		}
		return result;
	}

	/**
	 * Comprueba si hay que decrementar el índice que se aplicaba al elemento
	 * antes de ser modificado.<br>
	 * Si es posible decrementarlo, lo hace.<br>
	 * Este metodo accede a base de datos.
	 * 
	 * @param element
	 *            Elemento del que hay que comprobar si hay que decrementar el
	 *            índice que se le aplicaba anteriormente.
	 * @param filters
	 *            índice que se aplicaba al objeto antes de ser modificado.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataConversionException 
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException 
	 */
	
	
	private String getRdn(Element element) throws DataErrorException, DataConversionException, SQLException, NamingException {
		Attribute rdnAttribute = element.getAttribute(Constants.PROP_RDN);
		String rdn = null;
		if (rdnAttribute!=null)
			rdn = rdnAttribute.getValue();
		else
			rdn = indexFilterFunctions.searchPropertyValueInDb(Constants.PROP_RDN, element);
		return rdn;
	}
	/**
	 * Comprueba si hay que decrementar el índice que se aplicaba al elemento
	 * antes de ser modificado.<br>
	 * Si es posible decrementarlo, lo hace.<br>
	 * Este metodo accede a base de datos.
	 * 
	 * @param element
	 *            Elemento del que hay que comprobar si hay que decrementar el
	 *            índice que se le aplicaba anteriormente.
	 * @param indexFilter2
	 *            índice que se aplicaba al objeto antes de ser modificado.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataConversionException 
	 * @throws DataErrorException 
	 */
	
	public boolean isReplication() {
		return replication;
	}

	public void setReplication(boolean replication) {
		this.replication = replication;
	}

	/**
	 * Clase que almacena toda la informacion sobre todos los incrementos que
	 * hay que realizarle a un objeto determinado.
	 */
	private class IncrementalData{
		/** Identificador del objeto al que hay que aplicarle el incremento */
		private int tableId;
		/** Identificador de la clase a la que pertenece el objeto */
		private int idto;
		/** Mapa de los valores que hay que incrementar en cada columna */
		private Map<TableColumn, Double> incrementsByColumn;
		private String rdn;

		public IncrementalData(int idto,String rdn){
			this.incrementsByColumn = new Hashtable<TableColumn, Double>();
			this.idto = idto;
			this.tableId = -1;
			this.rdn=rdn;
		}

		public String getRdn(){
			return rdn;
		}
		
		/**
		 * Fija el identificador del objeto de la clase al que corresponde el
		 * incremento.
		 * 
		 * @param tableId
		 *            Identificador que tiene en las tablas el objeto.
		 */
		public void setTableId(int tableId){
			this.tableId = tableId;
		}

		/**
		 * Consulta el identificador del objeto dentro de la clase.
		 * 
		 * @return Si devuelve <code>-1</code> es que en ningun momento se fij
		 *         el identificador del objeto.
		 */
		public int getTableId(){
			return tableId;
		}

		/**
		 * Consulta el identificador de la clase a la que pertenece el objeto
		 * sobre el que se han de realizar los incrementos almacenados en este
		 * objeto.
		 * 
		 * @return Identificador de la clase a la que pertenece el objeto al que
		 *         hacerle los cambios.
		 */
		public int getIdto(){
			return idto;
		}

		/**
		 * añade el incremento que se tiene que realizar sobre una determinada
		 * columna.
		 * 
		 * @param column
		 *            Columna sobre la que se debe aplicar el incremento.
		 * @param value
		 *            Valor que se debe añadir al valor que tenga en ese momento
		 *            la columna.
		 */
		public void addIncrementValue(TableColumn column, double value){
			incrementsByColumn.put(column, value);
		}
		
		/**
		 * Consulta los incrementos que hay que realizarle a este objeto.
		 * 
		 * @return Mapa donde la clave son las columnas de la tabla de la clase
		 *         a la que pertenece el objeto sobre el que hay que realizar
		 *         los incrementos, y donde los valores son los incrementos en
		 *         si.<br>
		 *         Vienen declarados como Double porque es el tipo numero
		 *         donde caben el resto de tipos, pero hay que transformar los
		 *         datos para asimilarlos al tipo de la columna
		 */
		public Map<TableColumn, Double> getAllIncrements(){
			return incrementsByColumn;
		}

		/**
		 * Consulta el valor que hay que incrementar el dato almacenado en la
		 * columna dada.
		 * 
		 * @param column
		 *            Columna de la que se quiere saber el incremento.
		 * @return devolvera <code>null</code> si no existe ningun incremento
		 *         para dicha columna.
		 */
		public Double getColumnIncrement(TableColumn column){
			return incrementsByColumn.get(column);
		}
	}
	
	public static String getKeyIndividual(Integer idto,HashMap<String,String> newRdnMap, Element node){
		if(idto==null) idto=Integer.parseInt(node.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm));
		String currRdn=node.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);
		String keyInd=""+idto+"#"+currRdn;
		if(newRdnMap!=null){
			if(newRdnMap.containsValue(currRdn)){		
				Iterator<String> itr=newRdnMap.keySet().iterator();
				while(itr.hasNext()){
					String key=itr.next();				
					String[] keySplit=key.split("#");
					int idtoKey=Integer.parseInt(keySplit[0]);
					String currValueMap=newRdnMap.get(key);
					if(idtoKey==idto && currValueMap.equals(currRdn)){
						String rdnTmp=key.substring(keySplit[0].length()+1);
						return ""+idto+"#"+rdnTmp;					
					}
				}
			}
			if(newRdnMap.containsKey(keyInd)){
				String newRdn=newRdnMap.get(keyInd);				
				node.setAttribute(XMLConstants.ATTRIBUTE_RDN,newRdn);
			}
		}
		return keyInd;
	}
	
	public static Integer getTableId(Integer idto,HashMap<String,String> newRdnMap,HashMap<String,Integer> newTableIdMap,Element rootElement,Map<Integer, Element> elementsByIdNode,Element node) throws DataConversionException {		
		String keyInd=getKeyIndividual(idto,newRdnMap,node);
		if(newTableIdMap!=null){						
			Integer newTableId=newTableIdMap.get(keyInd);			
			if(newTableId!=null){			
				node.setAttribute(XMLConstants.ATTRIBUTE_TABLEID, ""+newTableId);
				return newTableId;
			}
		}						
				
		Attribute tableIdAttribute=node.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);		
		try{						
			if(tableIdAttribute== null){
				Attribute srcIdAt = node.getAttribute(XMLConstants.ATTRIBUTE_REFNODE);
				Integer idNode=srcIdAt.getIntValue();
				Element srcNode=elementsByIdNode.get(idNode);				
				tableIdAttribute=srcNode.getAttribute(XMLConstants.ATTRIBUTE_TABLEID);	
				if(srcNode== null){
					System.out.println("getTableId:El siguiente nodo no tiene ref node : " + jdomParser.returnXML(node));
				}							
			}
		}catch(JDOMException e){
			e.printStackTrace();
		}
		return tableIdAttribute.getIntValue();
	}
}