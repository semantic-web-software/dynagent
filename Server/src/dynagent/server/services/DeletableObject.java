package dynagent.server.services;

import java.io.File;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.DataConversionException;

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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.ObjectIdentifier;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

/**
 * Clase que se va a encargar de implementar toda la logica del borrado de objetos.
 */
public class DeletableObject {

	/** Objeto que contiene el mapa de las clases y tablas */
	private DataBaseMap dataBaseMap;
	/** Objeto que contiene informacion del modelo y del que vamos a sacar los alias. */
	private IKnowledgeBaseInfo knowledgeBaseInfo;
	/** Lista de los objetos que han sido borrados. */
	private List<LinkedObject> deletedObjects;
	/** Objeto que nos permite conectarnos a la base de datos */
	private FactoryConnectionDB fcdb;
	/** Identificador de la clase del objeto  */
	private int idto;
	/** Identificador del objeto de la clase que queremos borrar. */
	private int tableId;
	/** Representación del objeto que se nos ha pedido borrar. */
	private LinkedObject objectToDelete;
	/**
	 * Objetos que son apuntados por el objeto a borrar y que se han de
	 * comprobar para ver si se tienen que borrar tambien.<br>
	 * solo contiene objetos apuntados por estructurales.
	 */
	private Map<ObjectIdentifier, LinkedObject> linkedObjects;
	/** Cadena que contiene la causa del error si se hubiera producido. */
	private String errorCause;
	/**
	 * Contiene, para un objeto que no se ha borrado, los objetos que le
	 * apuntaban que inpedian que se borrase.
	 */
	private Map<ObjectIdentifier, Set<ObjectIdentifier>> linksToNonDeletedObjects;
	/**
	 * Objeto que da funcionalidad para permitir compatibilidad entre distintos
	 * gestores de base de datos
	 */
	private GenerateSQL generateSQL;
	/** código devuelto cuando no se ha producido ningun error. */
	public static final int NO_ERROR = -1;
	/** código devuelto cuando no se puede borrar el objeto indicado. */
	public static final int NO_DELETEABLE = 0;
	public static final int ALREADY_DELETED = 1;
	
	private static final int LIMIT_LINKS_TO_INDELIBLE_OBJECT=10;//Solo mostramos una lista reducida de los objetos que le apuntan y que evitan borrarse 
	
	///////////////////////////////////////////////////////////////////////////
	// metodoS PUBLICOS
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Inicializa el objeto para poder ejecutar el intento de borrado del objeto
	 * representado por el elemento XML dado.
	 * 
	 * @param dataBaseMap
	 *            Objeto que da informacion de como está almacenado el modelo en
	 *            base de datos.
	 * @param knowledgeBaseInfo
	 *            Objeto que contiene informacion del modelo.
	 * @param lockedObjects
	 *            Conjunto de objetos que han sido bloqueados hasta el momento
	 *            representandolos por cadenas con el siguiente formato:
	 *            idto#tableId
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @param elementToDelete
	 *            Elmento que contiene la informacion del objeto que se quiere
	 *            borrar.
	 */
	public DeletableObject(DataBaseMap dataBaseMap, IKnowledgeBaseInfo knowledgeBaseInfo, Set<ObjectIdentifier> lockedObjects, FactoryConnectionDB fcdb, ObjectIdentifier id) {
		this.dataBaseMap = dataBaseMap;
		this.fcdb = fcdb;
		this.idto = id.getIdto();
		this.tableId = id.getTableId();
		this.knowledgeBaseInfo = knowledgeBaseInfo;
		linkedObjects = new Hashtable<ObjectIdentifier, LinkedObject>();
		objectToDelete = null;
		linksToNonDeletedObjects = new Hashtable<ObjectIdentifier, Set<ObjectIdentifier>>();
		generateSQL = new GenerateSQL(fcdb.getGestorDB());
		deletedObjects = new LinkedList<LinkedObject>();
	}

	/**
	 * Intenta borrar el objeto siguiendo las reglas de borrado existentes.
	 * 
	 * @return Cadena de error si se da, o <code>null</code> si no hay ningun
	 *         error.
	 * @throws DataErrorException
	 * @throws DataConversionException Si el XML contiene un formato incorrecto para algun atributo.
	 * @throws NamingException 
	 * @throws SQLException 
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
	public int delete() throws DataErrorException, DataConversionException, SQLException, NamingException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException, NoSuchColumnException {
		boolean deleteable = getObjectToDelete();

		if (objectToDelete == null){
			errorCause = "El objeto no existen en base de datos.";
			return ALREADY_DELETED;
		}
		
		if (! deleteable){
			String rdn = InstanceService.getRdn(fcdb, dataBaseMap, tableId, idto);
			errorCause = "No se ha podido borrar " + knowledgeBaseInfo.getAliasOfClass(idto,null) + " '" + rdn + "' debido a que es usado por:\n" + linksToObjectString(objectToDelete);
			return NO_DELETEABLE;
		}
		// Borramos el objeto.
		deleteObject(objectToDelete, dataBaseMap, fcdb);
		deletedObjects.add(objectToDelete);
		// Buscamos los objetos que eran apuntado mediante una estructural por
		// el objeto borrado e intentamos borrarlos.
		List<LinkedObject> referencedObjects = getReferencedObjects(objectToDelete);
		Set<ObjectIdentifier> parentObjects = new HashSet<ObjectIdentifier>();
		parentObjects.add(new ObjectIdentifier(objectToDelete.getIdto(), objectToDelete.getTableId()));
		deleteReferencedObjects(referencedObjects, parentObjects);
		return NO_ERROR;
	}

	/**
	 * Si se ha producido algun error, devuelve la causa del error.
	 * 
	 * @return Cadena de error o <code>null</code> si no se ha producido ningun
	 *         error.
	 */
	public String getCause(){
		return errorCause;
	}

	/**
	 * Devuelve todos los objetos candidatos a ser borrados de la estructura que
	 * depende del nodo raiz.
	 * 
	 * @return Nunca devolvera <code>null</code>. Si no hay candidatos a ser
	 *         borrados, se devolvera un conjunto vacio.
	 */
	public Set<ObjectIdentifier> getCandidatesToDelete(){
		Set<ObjectIdentifier> result = new HashSet<ObjectIdentifier>();
		// TODO
		return result;
	}

	/**
	 * Consulta los objetos que se han conseguido borrar por no tener
	 * dependencias que lo impidan.
	 * 
	 * @return Lista de los objetos borrados.
	 */
	public List<LinkedObject> getDeletedObjects(){
		return deletedObjects;
	}

	/**
	 * Devuelve los vinculos que impiden que se borre un objeto de la estructura
	 * del root del que se ordenum el borrado.
	 * 
	 * @param object
	 *            Identificador del objeto del que queremos saber que impide que
	 *            se borre.
	 * @return Devuelve el conjunto de identificadores que impiden que se borre
	 *         este objeto o <code>null</code>.<br>
	 *         El conjunto devuelto es el almacenado en esta clase, asi que
	 *         cualquier modificación sobre dicho conjunto afectara a la
	 *         informacion de este objeto.
	 */
	public Set<ObjectIdentifier> getLinksToNonDeletedObject(ObjectIdentifier object){
		return linksToNonDeletedObjects.get(object);
	}

	/**
	 * Borra el objeto de base de datos.<br>
	 * Este metodo no comprueba si se puede borrar el objeto, simplemente
	 * elimina los vinculos hacia este objeto y los vinculos desde este objeto,
	 * para finalmente borrar el objeto en si.
	 * 
	 * @param linkedObject
	 *            Representación del objeto a borrar.
	 * @param dataBaseMap
	 *            Mapa de la base de datos.
	 * @param fcdb
	 *            Objeto que nos permite trabajar con la base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public static void deleteObject(LinkedObject linkedObject, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) throws SQLException, NamingException{
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		int idto = linkedObject.getIdto();
		Table table = dataBaseMap.getTable(idto);
		ClassInfo classInfo = dataBaseMap.getClass(idto);
		if (! classInfo.getFileProperties().isEmpty()){
			deleteAllFiles( linkedObject.getTableId(), table, classInfo, dataBaseMap, fcdb);
		}
		int tableId = linkedObject.getTableId();
		
		for (ObjectLink link : linkedObject.getLinksToThisObject()) {
			// Borramos todos los vinculos hacia este objeto.
			deleteLink(link, dataBaseMap, fcdb);
		}
		for (ObjectLink link : linkedObject.getLinksFromThisObject()) {
			// Borramos todos los vinculos desde este objeto.
			deleteLink(link, dataBaseMap, fcdb);
		}
		
		String date = QueryConstants.secondsToDate(String.valueOf(System.currentTimeMillis()/Constants.TIMEMILLIS), QueryConstants.getPattern(Constants.IDTO_DATETIME));
        
		String sql = "INSERT INTO deleted_objects (" + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + ", idto, rdn, date) VALUES ("+tableId+", "+idto+", (SELECT DISTINCT rdn from " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + tableId+"), '"+date+"')";
		DBQueries.execute(fcdb, sql);
		// Borramos el objeto.
		sql = "DELETE FROM " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + tableId;
		DBQueries.execute(fcdb, sql);
	}

	/**
	 * metodo que se encarga de borrar todos los objetos de una clase
	 * indiscriminadamente. Hace un borrado coherente, borrando tambien todas
	 * las referencias a los objetos que se van a borrar.
	 * 
	 * @param idto
	 *            Identificador de la clase de los objetos que se quieren
	 *            borrar.
	 * @param dataBaseMap
	 *            Mapa del modelo y de las tablas de la base de datos.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @return devolvera <code>true</code> solo si ha conseguido borrar
	 *         correctamente todos los objetos de la clase.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NoSuchColumnException 
	 */
	public static void deleteAllObjects(int idto, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) throws SQLException, NamingException, NoSuchColumnException{
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		Table table = dataBaseMap.getTable(idto);
		assert table != null : "No se conoce una tabla para el idto=" + idto;
		// Lo primero que vamos a hacer es comprobar que la tabla tenga datos.
		String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + table.getName() + cE;
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		if (queryResult.isEmpty()){
			// Si no hay resultados en la consulta significa que no hay objetos
			// de la clase indicada.
			return;
		}
		
		// Vamos a buscar las referencias a la clase de los objetos a borrar
		// para desvincular los objetos. Esto nos permitira borrarlos más tarde
		Set<ClassInfo> classes = dataBaseMap.getClassesReferencingClass(idto);
		if(classes!=null){
			for (ClassInfo classInfo : classes) {
				Set<Integer> properties = classInfo.getPropertiesReferencingClass(idto);
				if (properties == null){
					// El rango de la propiedad es una clase padre de idto.
					properties = new HashSet<Integer>();
					ClassInfo rangeClass = dataBaseMap.getClass(idto);
					for (Integer parentClassIdto : rangeClass.getParentClasses()){
						Set<Integer> propertiesToParent = classInfo.getPropertiesReferencingClass(parentClassIdto);
						if (propertiesToParent != null){
							properties.addAll(propertiesToParent);
						}
					}
				}
				Table referencingClassTable = dataBaseMap.getTable(classInfo.getIdto());
				List<String> columns = new LinkedList<String>();
				for (Integer idProperty : properties) {
					if (referencingClassTable.isExternalizedProperty(idProperty)){
						// Si es una propiedad externalizada, tenemos que buscar en
						// que tabla se encuentra el vinculo entre estas dos clases
						// mediante dicha propiedad.
						deleteReferencingAssociantionLinks(idto, referencingClassTable, idProperty, dataBaseMap, fcdb);
					} else {
						// Si no es una propiedad externalizada, tenemos que buscar
						// la columna que apunta a la clase de los objetos a borrar.
						for (String columnName : referencingClassTable.getColumnNamesContainingProperty(idProperty)) {
							if (referencingClassTable.getColumnDomain(columnName).equals(idto)){
								columns.add(columnName);
								break;
							}
						}
					}
				}
				if (! columns.isEmpty()){
					if (columns.size() > 1){
						sql = "UPDATE " + cB + referencingClassTable.getName() + cE + " SET " + cB + Auxiliar.listToString(columns, cE + "=NULL, ") + cE + "=NULL";
					} else {
						sql = "UPDATE " + cB + referencingClassTable.getName() + cE + " SET " + cB + columns.get(0) + cE + "=NULL";
					}
					DBQueries.executeUpdate(fcdb, sql);
				}
			}
		}
		// Ahora tenemos que borrar los vinculos que surgen de nuestra clase.
		// Solo nos interesan las propiedades externalizadas.
		for (Integer idProperty : table.getExternalizedProperties()) {
			Set<Integer> externalizedPropertyLocations = table.getExternalizedPropertyLocations(idProperty);
			//System.out.println("ext_pr: " + externalizedPropertyLocations);
			for (Integer externalizedPropertyTableIdto : externalizedPropertyLocations) {
				Table externalizedPropertyTable = dataBaseMap.getTable(externalizedPropertyTableIdto);
				if (externalizedPropertyTable == null){
					// Se trata del identificador de una vista.
					continue;
				}
				sql = null;
				if (externalizedPropertyTable.isAssociation()){
					// Borramos todos los vinculos salientes de nuestra clase.
					sql = "DELETE FROM " + cB + externalizedPropertyTable.getName() + cE;
					DBQueries.execute(fcdb, sql);
				}else{
					TableColumn[] domainColumns = externalizedPropertyTable.getObjectPropertyColumn(idProperty, idto);
					sql = "UPDATE " + cB + externalizedPropertyTable.getName() + cE + " SET " + cB + domainColumns[0].getColumnName() + cE + "=NULL" ;
					DBQueries.executeUpdate(fcdb, sql);
				}
			}
			
		}
		// Ya podemos borrar todo el contenido de la tabla de los objetos.
		sql = "DELETE FROM " + cB + table.getName() + cE;
		DBQueries.execute(fcdb, sql);
	}

	/**
	 * Borra el vinculo existente entre dos clases mediante una propiedad si se
	 * encuentra en una tabla asociacion.
	 * 
	 * @param rangeIdto
	 *            Identificador de la clase del rango de la propiedad que nos
	 *            interesa.
	 * @param domainTable
	 *            Tabla de la que ha sido externalizada la propiedad.
	 * @param idProperty
	 *            Identificador de la propiedad externalizada.
	 * @param dataBaseMap
	 *            Mapa del modelo y de la base de datos.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @throws NamingException
	 * @throws SQLException
	 */
	private static void deleteReferencingAssociantionLinks(int rangeIdto, Table domainTable, Integer idProperty, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		for (Integer externalizedPropertyTableIdto : domainTable.getExternalizedPropertyLocations(idProperty)) {
			Table externalizedPropertyTable = dataBaseMap.getTable(externalizedPropertyTableIdto);
			if (externalizedPropertyTable == null){
				// Se trata del identificador de una vista.
				continue;
			}
			if (!externalizedPropertyTable.isAssociation()){
				// Se trata de la tabla del rango de la propiedad, de la que
				// hemos decidido no borrar los vinculos, pues se borraran al
				// quitar los registros de dicha tabla.
				continue;
			}
			TableColumn[] rangeColumns = externalizedPropertyTable.getObjectPropertyColumn(idProperty, rangeIdto);
			if (rangeColumns == null){
				// No hay una relación entre las dos clases que buscamos en esta
				// tabla asociacion.
				continue;
			}
			String sql = "DELETE FROM " + cB + externalizedPropertyTable.getName() + cE;
			DBQueries.execute(fcdb, sql);
			// Solo puede haber una tabla asociacion entre dos clases, asi que
			// podemos cortar el bucle.
			break;
		}
	}

	/**
	 * Borra todos los ficheros que son apuntados por el objeto identificado por
	 * los datos dados.
	 * 
	 * @param tableId
	 *            Identificador del objeto del que queremos borrar los ficheros.
	 * @param table
	 *            Tabla donde están los datos de los objetos de la clase del
	 *            objeto que estamos procesando.
	 * @param classInfo
	 *            Informacion de la clase del objeto a procesar.
	 * @param dataBaseMap
	 *            Mapa de la base de datos.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a la base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public static void deleteAllFiles(int tableId, Table table, ClassInfo classInfo, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) throws SQLException, NamingException {
		// TODO Auto-generated method stub
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		Set<Integer> fileProperties = classInfo.getFileProperties();
		// Lista de las propiedades que se encuentran en la propia tabla de la
		// clase y que pueden contener el nombre de un fichero a borrar.
		List<String> tablePropertyNames = new LinkedList<String>();
		List<String> filesToDelete = new LinkedList<String>();
		for (Integer idProperty : fileProperties){
			if (table.isExternalizedProperty(idProperty)){
				// Son DataProperties de cardinalidad multiple y por tanto el dato está en otras tablas externas.
				Set<Integer> externalizedPropertyLocations = table.getExternalizedPropertyLocations(idProperty);
				assert externalizedPropertyLocations != null : "La propiedad " + idProperty + " identificada como externalizada en la tabla " + table.getName() + " no tiene localizaciones asociadas.";
				Integer propertyLocation = externalizedPropertyLocations.iterator().next();
				Table dataPropertyTable = dataBaseMap.getTable(propertyLocation);
				assert dataPropertyTable != null : "No se ha encontrado la tabla que asocia la clase " + table.getName() + " con su dataProperty " + idProperty;
				// Columna que contiene el nombre del fichero asociado.
				TableColumn column = dataPropertyTable.getDataPropertyColumn(idProperty);
				// Columna que contiene el tableId del objeto al que está asociado la imagen.
				TableColumn parentColumn = dataPropertyTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, classInfo.getIdto())[0];
				String sql = "SELECT " + cB + column.getColumnName() + cE + " FROM " + cB + dataPropertyTable.getName() + cE + " WHERE " + cB + parentColumn.getColumnName() + cE + "=" + tableId;
				List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
				// Sacamos todos los nombres de ficheros que se han de borrar.
				for (List<String> objectData : queryResult){
					String fileName = objectData.get(0);
					if (fileName != null){
						filesToDelete.add(fileName);
					}
				}
			} else {
				TableColumn column = table.getDataPropertyColumn(idProperty);
				tablePropertyNames.add(column.getColumnName());
			}
		}
		// Comprobamos si hay que consultar sobre la tabla de la clase.
		if (! tablePropertyNames.isEmpty()){
			String sql = "SELECT " + cB + Auxiliar.listToString(tablePropertyNames, cE + ", " + cB) + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + tableId;
			List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
			// Sacamos la informacion de los nombres de los ficheros que puedan estar vinculados.
			for (List<String> objectData : queryResult){
				for (String data : objectData){
					if (data != null){
						filesToDelete.add(data);
					}
				}
			}
		}
		
		// Una vez tenemos todos los nombres de los ficheros, los borramos.
		File filesFolder = new File("../server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/" + Constants.folderUserFiles + "/" + fcdb.getBusiness());
		for (String fileName : filesToDelete){
			File file = new File(filesFolder, fileName);
			if (file.exists()){
				file.delete();
			}
			File smallFile = new File(filesFolder, Constants.smallImage + fileName);
			if (smallFile.exists()){
				smallFile.delete();
			}
		}
	}

	/**
	 * Borra en vinculo entre dos objetos.<br>
	 * Si el vinculo existen en la tabla del dominio o la del rango, lo que se
	 * hace es poner a <code>NULL</code> el valor del atributo. En cambio, si el
	 * vinculo está en una tabla asociacion, se borra el registro de la tabla
	 * por completo.
	 * 
	 * @param dataBaseMap
	 *            Mapa de la base de datos.
	 * @param fcdb
	 *            Objeto que nos permite conectarnos a base de datos.
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public static void deleteLink(ObjectLink link, DataBaseMap dataBaseMap, FactoryConnectionDB fcdb) throws SQLException, NamingException{
		int linkTableId = link.getTableIdto();
		Table linkTable = dataBaseMap.getTable(linkTableId);
		String sql = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		// Ignoramos los vinculos de propiedades inversas de estructurales
		// porque solo se tiene que borrar la estructural.
		Integer inverseProperty = dataBaseMap.getInverseProperty(link.getIdProperty());
		if (inverseProperty != null && dataBaseMap.isStructuralProperty(inverseProperty)){
			return;
		}
		if (linkTable.isAssociation()){
			// Se borra el registro que vincula los dos objetos, una vez
			// obtenido el nombre de las columnas.
			TableColumn [] domainColumns = linkTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, link.getDomain());
			TableColumn [] rangeColumns = linkTable.getObjectPropertyColumn(link.getIdProperty(), link.getRange());
			TableColumn propertyColumn = linkTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
			sql = "DELETE FROM " + cB + linkTable.getName() + cE + " WHERE " + cB + domainColumns[0].getColumnName() + cE + "=" + link.getDomainId() + " AND " + cB + rangeColumns[0].getColumnName() + cE + "=" + link.getRangeId();
			if (propertyColumn != null){
				sql += " AND " + cB + propertyColumn.getColumnName() + cE + "=" + link.getIdProperty();
			}
		}else if (linkTableId == link.getDomain()){
			// El vinculo se produce en la tabla del dominio.
			TableColumn [] propertyColumns = linkTable.getObjectPropertyColumn(link.getIdProperty(), link.getRange());
			sql = "UPDATE " + cB + linkTable.getName() + cE + " SET " + cB + propertyColumns[0].getColumnName() + cE + "=NULL WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + link.getDomainId();
		}else{
			// El vinculo se produce en la tabla del rango.
			TableColumn [] propertyColumns = linkTable.getObjectPropertyColumn(link.getIdProperty(), link.getDomain());
			sql = "UPDATE " + cB + linkTable.getName() + cE + " SET " + cB + propertyColumns[0].getColumnName() + cE + "=NULL WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + link.getRangeId();
		}
		DBQueries.execute(fcdb, sql);
	}
	

	/**
	 * Borra de los conjuntos que impiden que se borren otros objetos los
	 * identificados por los elementos de la lista dada.
	 * 
	 * @param deletedLinkedObjects
	 *            Objetos que han sido borrados de la base de datos y de los que
	 *            queremos borrar las referencias que puideran existir desde
	 *            otros objetos.
	 */
	public void deleteReferencesToObjects(List<LinkedObject> deletedLinkedObjects){
		List<ObjectIdentifier> identifiers = new LinkedList<ObjectIdentifier>();
		for (LinkedObject linkedObject : deletedLinkedObjects) {
			identifiers.add(new ObjectIdentifier(linkedObject.getIdto(), linkedObject.getTableId()));
		}
		for (ObjectIdentifier id : linksToNonDeletedObjects.keySet()){
			Set<ObjectIdentifier> objectsReferencing = linksToNonDeletedObjects.get(id);
			boolean modifications = objectsReferencing.removeAll(identifiers);
			if (modifications){
				LinkedObject object = linkedObjects.get(id);
				removeLinks(object, identifiers);
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// metodoS PRIVADOS
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Intenta borrar los objetos apuntados por estructurales recursivamente.
	 * 
	 * @param referencedObjects
	 *            Lista de los objetos que son refernciados mediante una
	 *            propiedad estructural y que deben ser borrados.
	 * @param parentObjects TODO
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NamingException
	 *             Si hay algun error en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la consulta SQL
	 * @throws NoSuchColumnException 
	 */
	private void deleteReferencedObjects(List<LinkedObject> referencedObjects, Set<ObjectIdentifier> parentObjects) throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		int numberOfDeletedObjects = Integer.MAX_VALUE;
		List<LinkedObject> deletedObjects = new LinkedList<LinkedObject>();
		while (numberOfDeletedObjects > 0){
			numberOfDeletedObjects = 0;
			List<LinkedObject> deletedLinkedObjects = new LinkedList<LinkedObject>();
			for (LinkedObject linkedObject : referencedObjects) {
				if (isDeleteableObject(linkedObject)){
					numberOfDeletedObjects ++;
					deletedLinkedObjects.add(linkedObject);
					removeLinksToParents(linkedObject, parentObjects);
					deleteObject(linkedObject, dataBaseMap, fcdb);
					deletedObjects.add(linkedObject);
				}
			}
			referencedObjects.removeAll(deletedLinkedObjects);
			deleteReferencesToObjects(deletedLinkedObjects);
			// Acumulamos los objetos borrados para procesar los vinculos por
			// estructurales que surjan de los mismos.
			deletedObjects.addAll(deletedLinkedObjects);
		}
		// Si se ha conseguido borrar algun objeto, exploramos el siguiente
		// nivel de estructural.
		if (deletedObjects.size() > 0){
			Set<LinkedObject> nextLevel = new HashSet<LinkedObject>();
			Set<ObjectIdentifier> nextParentObjects = new HashSet<ObjectIdentifier>();
			for (LinkedObject linkedObject : deletedObjects) {
				nextLevel.addAll(getReferencedObjects(linkedObject));
				nextParentObjects.add(new ObjectIdentifier(linkedObject.getIdto(), linkedObject.getTableId()));
			}
			deleteReferencedObjects(new LinkedList<LinkedObject>(nextLevel), nextParentObjects);
		}
	}

	/**
	 * Elmina todos los vinculos que pudieran apuntar a objetos de los que
	 * depende estructuralmente este objeto.
	 * 
	 * @param linkedObject
	 *            Objeto del que se quieren borrar los vinculos.
	 * @param parentObjects
	 *            Identificadores de los objetos a los que no queremos que
	 *            queden vinculos.
	 */
	private void removeLinksToParents(LinkedObject linkedObject, Set<ObjectIdentifier> parentObjects) {
		for (ObjectLink link : linkedObject.getLinksFromThisObject()){
			ObjectIdentifier referencedObject = new ObjectIdentifier(link.getRange(), link.getRangeId());
			if (parentObjects.contains(referencedObject)){
				linkedObject.removeLink(link);
			}
		}
	}

	/**
	 * Quita del objeto todos los vinculos que pudieran existir a los objetos de
	 * la lista.
	 * 
	 * @param object
	 *            Objeto del que tenemos que borrar los vinculos.
	 * @param referencingObjects
	 *            Identificadores de los objetos a los que tenemos que borrar
	 *            vinculos.
	 */
	private void removeLinks(LinkedObject object, List<ObjectIdentifier> referencingObjects) {
		for (ObjectLink link : object.getLinksToThisObject()){
			ObjectIdentifier domainIdentifier = new ObjectIdentifier(link.getDomain(), link.getDomainId());
			if (referencingObjects.contains(domainIdentifier)){
				object.removeLink(link);
			}
		}
	}

	/**
	 * Construye una cadena con la cadena a mostrar de los objetos que vinculan
	 * al objeto dado y que impiden que este se borre.
	 * 
	 * @param object
	 *            Objeto del que queremos sacar la lista de los objetos que lo
	 *            apuntan.
	 * @throws SQLException
	 *             Si hay algun error en la consulta SQL
	 * @throws NamingException
	 *             Si hay algun error en la conexión con la base de datos.
	 * @throws DataErrorException 
	 * @throws NoSuchColumnException 
	 */
	private String linksToObjectString(LinkedObject object) throws SQLException, NamingException, DataErrorException, NoSuchColumnException{
		if (object == null){
			// No se puede trabajar con un objeto nulo.
			return "";
		}
		System.err.println(object.toString());
		List<String> linksString = new LinkedList<String>();
		ObjectIdentifier identifier = new ObjectIdentifier(object.getIdto(), object.getTableId());
		Set<ObjectIdentifier> linksToObject = linksToNonDeletedObjects.get(identifier);
		if (linksToObject == null){
			// No se ha construido el conjunto de objetos que impiden que se borre este objeto
			System.out.println("No se ha construido el conjunto de objetos que impiden que se borre este objeto");
			return "";
		}
		Map<Integer, Set<Integer>> linksByClassMap = new Hashtable<Integer, Set<Integer>>();
		for (ObjectIdentifier objectIdentifier : linksToObject) {
			int idto = objectIdentifier.getIdto();
			Table table = dataBaseMap.getTable(idto);
			if (table.hasForeignProperties()){
				// La clase es apuntada por estructurales, asi que cogemos los
				// objetos que apuntan a este objeto de base de datos.
				Map<Integer, Set<Integer>> objectsReferencing = getParentsByStructural(objectIdentifier);
				for (Integer idtoReferencingClass : objectsReferencing.keySet()) {
					Set<Integer> tableIds = linksByClassMap.get(idtoReferencingClass);
					if (tableIds == null){
						tableIds = new HashSet<Integer>();
						linksByClassMap.put(idtoReferencingClass, tableIds);
					}
					tableIds.addAll(objectsReferencing.get(idtoReferencingClass));
				}
			}else{
				Set<Integer> tableIds = linksByClassMap.get(idto);
				if (tableIds == null){
					tableIds = new HashSet<Integer>();
					linksByClassMap.put(idto, tableIds);
				}
				tableIds.add(objectIdentifier.getTableId());
			}
		}
		// Una vez tenemos todos los objetos organizados, buscamos en base de datos sus rdn
		for (Integer idto : linksByClassMap.keySet()){
			linksString.addAll(getErrorWithRdn(idto, linksByClassMap.get(idto)));
		}
		if(linksString.size()==LIMIT_LINKS_TO_INDELIBLE_OBJECT){//Si alcanzamos el numero limite, mostramos puntos suspensivos ya que seguramente hay mas objetos apuntando 
			linksString.add("...");
		}
		return Auxiliar.listToString(linksString, "\n");
	}

	/**
	 * Construye la cadena de las identificaciones de la forma:
	 * "'nombre_clase' : 'rdn" de los objetos identificados por los datos
	 * proporcionados.
	 * 
	 * @param idto
	 *            Clase a la que pertenecen los objetos a buscar.
	 * @param tableIds
	 *            Identificadores de los objetos a buscar.
	 * @return Lista no nula de las cadenas para identificar a los objetos.
	 * @throws SQLException
	 *             Si hay un error en la sentencia SQL
	 * @throws NamingException
	 *             Si hay un error en la conexión.
	 * @throws DataErrorException
	 *             Si hay algun error con los datos.
	 */
	private List<String> getErrorWithRdn(Integer idto, Set<Integer> tableIds) throws SQLException, NamingException, DataErrorException {
		// Lista que contendra cadenas del estilo: "'nombre_clase' : 'rdn'"
		List<String> result = new LinkedList<String>();
		if (tableIds.isEmpty()){
			return result;
		}
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		Table table = dataBaseMap.getTable(idto);
		String tableName = table.getName();
		TableColumn tableIdColumn = table.getDataPropertyColumn(IQueryInfo.ID_TABLE_ID);
		TableColumn rdnColumn = table.getDataPropertyColumn(Constants.IdPROP_RDN);
		String sql = "SELECT " + cB + rdnColumn.getColumnName() + cE + " FROM " + cB + tableName + cE + " WHERE " + cB + tableIdColumn.getColumnName() + cE + " IN (" + Auxiliar.setToString(tableIds, ",") + ")";
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		for (List<String> objectData : queryResult) {
			String rdn = objectData.get(0);
			try {
				result.add(knowledgeBaseInfo.getAliasOfClass(idto, null) + " '" + rdn + "'");
			} catch (Exception e) {
				throw new DataErrorException("Error al intentar encontrar el alias de la clase: " + tableName);
			} 
		}
		return result;
	}

	/**
	 * Busca los objetos que apuntan mediante una estructual al objeto dado.
	 * 
	 * @param objectIdentifier
	 *            Identidicador del objeto del que buscamos quien lo apunta
	 *            mediante una estructural.
	 * @return Mapa de los objetos que lo apuntan organizados por el idto de la
	 *         clase.
	 * @throws NamingException
	 *             Si hay algun error en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la sentencia SQL.
	 * @throws NoSuchColumnException 
	 */
	private Map<Integer, Set<Integer>> getParentsByStructural(ObjectIdentifier objectIdentifier) throws SQLException, NamingException, NoSuchColumnException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		Table table = dataBaseMap.getTable(objectIdentifier.getIdto());
		List<String> columnNames = new LinkedList<String>();
		// Buscamos los nombres de las columnas que contienen propiedades foraneas.
		for (Integer idProperty :  table.getForeignProperties()){
			for (String columnName : table.getColumnNamesContainingProperty(idProperty)) {
				columnNames.add(columnName);
			}
		}
		
		// Mapa donde la clve son los idto y el valor son los tableId de esa clase que apuntan a este objeto.
		Map<Integer, Set<Integer>> referencingObjects = new Hashtable<Integer, Set<Integer>>();
		
		String sql = "SELECT " + cB + Auxiliar.listToString(columnNames, cE + ", " + cB) + cE + " FROM " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + "=" + objectIdentifier.getTableId();
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		// Relleanamos el mapa con los identificadores de los objetos que apuntan a este objeto mediante una estructural.
		for (List<String> objectData : queryResult) {
			Iterator<String> it = columnNames.iterator();
			for (String data : objectData) {
				String columnName = it.next();
				if (data == null){
					continue;
				}
				int referencingClass =  table.getColumnDomain(columnName);
				int referencingTableId = Integer.parseInt(data);
				Set<Integer> referencingObjectsId = referencingObjects.get(referencingClass);
				if (referencingObjectsId == null){
					referencingObjectsId = new HashSet<Integer>();
					referencingObjects.put(referencingClass, referencingObjectsId);
				}
				referencingObjectsId.add(referencingTableId);
			}
		}
		return referencingObjects;
	}

	/**
	 * Construye la consulta para sacar todos los datos de la tabla inidicada,
	 * filtrando por los tableId dados.
	 * 
	 * @param tableIds
	 *            Conjunto de identifidadores de los objetos que queremos sacar
	 *            de la tabla indicada.
	 * @param table
	 *            Tabla de la que tenemos que obtener los datos.
	 * @param columns
	 *            Lista de las columnas que se quieren consultar, en el orden
	 *            que se desea que se devuelvan los datos.<br>
	 *            La lista se modifica para que el primer dato devuelto siempre
	 *            sea el tableId del registro.
	 * @return Cadena que realiza la consulta esperada.
	 * @throws DataErrorException
	 *             Si hay algun error en los datos dados.
	 */
	private String buildQuery(List<Integer> tableIds,  Table table, List<TableColumn> columns) throws DataErrorException {
		if (columns == null || columns.isEmpty()){
			throw new DataErrorException("DeletableObject.buildQuery() [Se ha llegado con una lista de columnas vacía o nula]");
		}
		if (tableIds == null || tableIds.isEmpty()){
			throw new DataErrorException("DeletableObject.buildQuery() [Se ha llegado con una lista de identificadores vacía o nula]");
		}
		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		TableColumn tableIdColumn = null;
		String sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE;
		Iterator<TableColumn> columnsIterator = columns.iterator();
		while (columnsIterator.hasNext()) {
			TableColumn column = columnsIterator.next();
			// Añadimos los nombres de las columnas de las que tenemos que sacar
			// los datos.
			if (column.getColumnName().equals(IQueryInfo.COLUMN_NAME_TABLEID)){
				// La columna del tableId la vamos a trasladar a la primera posición de la lista.
				tableIdColumn = column;
				columnsIterator.remove();
				continue;
			}
			sql += ", " + cB + column.getColumnName() + cE;
		}
		if (tableIdColumn != null){
			columns.add(0, tableIdColumn);
		}
		sql += " FROM " + cB + table.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " IN (";
		sql = addTableIdsList(tableIds, sql);
		sql += ")";
		return sql;
	}

	/**
	 * Borra un vinculo incompleto entre dos clases de una tabla asociacion.
	 * 
	 * @param associationTable
	 *            Tabla de donde tenemos que borrar el registro.
	 * @param domainColumn
	 *            Columna que contiene los datos del dominio
	 * @param rangeColumn
	 *            Columna que contiene los datos del rango (la que está a NULL y
	 *            hace que el vinculo está incompleto).
	 * @param domainValue
	 *            Valor que tiene que tener la columna del dominio.
	 * @param rangeValue
	 *            Valor que tiene que tener la columna del rango.
	 * @throws SQLException
	 *             Si la sentencia SQL tiene algun error.
	 * @throws NamingException
	 *             Si se produce algun error en la conexión con la base de
	 *             datos.
	 */
	private void deleteIncompleteLink(Table associationTable, TableColumn domainColumn, TableColumn rangeColumn, String domainValue, String rangeValue) throws SQLException, NamingException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		String sql = "DELETE FROM " + cB + associationTable.getName() + cE + " WHERE " + cB + domainColumn.getColumnName() + cE + "=" + domainValue + " AND " + cB + rangeColumn.getColumnName() + cE + "=" + rangeValue;
		DBQueries.execute(fcdb, sql);
	}

	/**
	 * Consulta en base de datos el objeto a borrar y todos los objetos
	 * vinculados al objeto.<br>
	 * Si el objeto está apuntado por propiedades estructurales y esos campos
	 * tienen valores, no se podra borrar el objeto.
	 * 
	 * @return devolvera <code>false</code> si se detecta en primera instancia
	 *         que el objeto no se puede borrar por ser apuntado por
	 *         estructurales con valor.
	 * @throws DataConversionException
	 *             Si alguno de los datos del XML tiene un formato no esperado.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException Si hay algun error con los datos.
	 * @throws NoSuchColumnException 
	 */
	private boolean getObjectToDelete() throws DataConversionException, SQLException, NamingException, DataErrorException, NoSuchColumnException {
		boolean deletable = false;
		List<Integer> tableIdList = new LinkedList<Integer>();
		tableIdList.add(tableId);
		List<LinkedObject> objectsFound = getObjectsFromDB(idto, tableIdList);
		if (objectsFound != null && ! objectsFound.isEmpty()){
			objectToDelete = objectsFound.get(0);
			deletable = isDeleteableObject(objectToDelete);
		}
		return deletable;
	}

	/**
	 * Obtiene una representación de los objetos indicados.
	 * 
	 * @param idto
	 *            Clase a la que pertenecen los objetos que se quieren
	 *            consultar.
	 * @param tableIds
	 *            Identificadores de los objetos dentro de la clase indicada.
	 * @return Lista de las representaciones de los objetos identificados por
	 *         los parámetros dados.
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException Si hay algun error con los datos.
	 * @throws NoSuchColumnException 
	 */
	private List<LinkedObject> getObjectsFromDB(int idto, List<Integer> tableIds) throws SQLException, NamingException, DataErrorException, NoSuchColumnException{
		if (tableIds == null || tableIds.isEmpty()){
			return null;
		}
		Map<Integer, LinkedObject> objectsByTableId = new Hashtable<Integer, LinkedObject>();
		Iterator<Integer> it = tableIds.iterator();
		while (it.hasNext()){
			Integer tableId = it.next();
			LinkedObject existingLinkedObject = linkedObjects.get(new ObjectIdentifier(idto, tableId));
			if (existingLinkedObject != null){
				it.remove();
				objectsByTableId.put(tableId, existingLinkedObject);
			}
		}
		if (! tableIds.isEmpty()){
			getObjectsFromDBClassTable(idto, tableIds, objectsByTableId);
			if (objectsByTableId.values() == null || objectsByTableId.values().isEmpty()){
				return null;
			}
			getObjectsFromDBReferenced(idto, tableIds, objectsByTableId);
			getObjectsFromDBReferencing(idto, tableIds, objectsByTableId);
		}
		LinkedList<LinkedObject> result = new LinkedList<LinkedObject>(objectsByTableId.values());
		// Añadimos los nuevos objetos encontrados al mapa de linkedObjects.
		for (LinkedObject linkedObject : result) {
			if (tableIds.contains(linkedObject.getTableId())){
				linkedObjects.put(new ObjectIdentifier(linkedObject.getIdto(), linkedObject.getTableId()), linkedObject);
			}
		}
		return result;
	}

	/**
	 * Obtiene los datos de la tabla de la clase a la que pertenecen los
	 * objetos
	 * 
	 * @param idto
	 *            Identificador de la clase de la que queremos obtener objetos.
	 * @param tableIds
	 *            Identificadores de los objetos de esta clase que nos
	 *            interesan.
	 * @param objects
	 *            Lista donde tenemos que ir añadiendo los objetos que saquemos
	 *            de la tabla.
	 * @throws DataErrorException
	 *             Si hay algun error con los datos o con el formato de las
	 *             tablas.
	 * @throws SQLException
	 *             Si hay un error en la sentencia de consulta a base de datos.
	 * @throws NamingException
	 *             Si hay algun error en las comunicaciones con la base de
	 *             datos.
	 */
	private void getObjectsFromDBClassTable(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects) throws DataErrorException, SQLException, NamingException {
		Table table = dataBaseMap.getTable(idto);
		List<TableColumn> columns = new LinkedList<TableColumn>(table.getAllColumns());
		// Construimos la consulta y sacamos los datos.
		String sql = buildQuery(tableIds, table, columns);
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		for (List<String> objectData : queryResult) {
			LinkedObject object = new LinkedObject(idto);
			Iterator<TableColumn> columnsIterator = columns.iterator();
			for (String data : objectData) {
				TableColumn column = columnsIterator.next();
				if (data == null){
					// Las columnas con valor nulo no nos interesan.
					continue;
				}
				int idProperty = column.getIdProperty();
				if (table.isObjectProperty(idProperty)){
					// solo nos interesan las ObjectProperties porque son las
					// que crean vinculos.
					int tableIdReferenced = Integer.parseInt(data);
					int tableReferenced = table.getColumnDomain(column.getColumnName());
					if (table.isForeignProperty(idProperty)){
						// Se trata de una estructural trasladada a esta tabla.
						// Miramos si tiene inversa, pues esa es la propiedad
						// que de verdad pertenece a esta clase. La estructural
						// como tal no la tratamos pues se tratara cuando se
						// procesen los vinculos a esta clase.
						Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
						if (inverseProperty == null){
							// Si no tiene inversa, esta clase no tiene vinculo
							// con la de la propiedad estructural.
							continue;
						}
						ObjectLink directLink = new ObjectLink(tableReferenced, tableIdReferenced, object.getIdto(), object.getTableId(), idProperty, table.getId());
						ObjectLink inverseLink = new ObjectLink(object.getIdto(), object.getTableId(), tableReferenced, tableIdReferenced, inverseProperty, table.getId());
						object.addLink(inverseLink);
						object.addLink(directLink);
					}else{
						// Se trata de una propiedad de la clase.
						ObjectLink link = new ObjectLink(object.getIdto(), object.getTableId(), tableReferenced, tableIdReferenced, idProperty, table.getId());
						object.addLink(link);
					}
				}else if (idProperty == IQueryInfo.ID_TABLE_ID){
					object.setTableId(Integer.parseInt(data));
				}
			}
			objects.put(object.getTableId(), object);
		}
	}

	/**
	 * añade la informacion de los vinculos desde esta clase a otras que se
	 * encuentra en tablas externas a las de la clase.
	 * 
	 * @param idto
	 *            Identificador de la clase de la que tienen que surgir los
	 *            vinculos.
	 * @param tableIds
	 *            Identificadores de los objetos de los que nos interesan los
	 *            vinculos.
	 * @param objects
	 *            Objetos donde tenemos que ir guardando la informacion de los
	 *            vinculos.
	 * @throws NamingException
	 *             Si se produce algun error en las comunicaciones con la base
	 *             de datos.
	 * @throws SQLException
	 *             Si la sentencia SQL no está bien construida.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NoSuchColumnException 
	 */
	private void getObjectsFromDBReferenced(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects) throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		Table domainTable = dataBaseMap.getTable(idto);
		for (Integer referencedTableIdto : domainTable.getAllReferencedTables()) {
			Table referencedTable = dataBaseMap.getTable(referencedTableIdto);
			if (referencedTable.isAssociation()){
				getObjectsFromDBReferencedAssociationTable(idto, tableIds, objects, referencedTable);
			}else{
				getObjectsFromDBReferencedRangeTable(idto, tableIds, objects, referencedTable);
			}
		}
	}

	/**
	 * añade a los objetos de una clase los vinculos que pudiera haber en una
	 * tabla asociacion.<br>
	 * La columna del dominio en la tabla asociacion debe apuntar a la clase a
	 * la que pertenecen los objetos.
	 * 
	 * @param idto
	 *            Identificador de la clase a la que pertenecen los objetos.
	 * @param tableIds
	 *            Identificadores de los objetos de la clase de los que nos
	 *            interesa saber los vinculos que puideran existir.
	 * @param objects
	 *            Objetos donde tenemos que ir guardando los vinculos que
	 *            encontremos.
	 * @param associationTable
	 *            Tabla asociacion de donde tenemos que sacar los datos.
	 * @throws SQLException
	 *             Si existe un error en la consulta SQL
	 * @throws NamingException
	 *             Si existe algun error en la conexión a base de datos.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 */
	private void getObjectsFromDBReferencedAssociationTable(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects, Table associationTable) throws SQLException, NamingException, DataErrorException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		TableColumn domainColumn = associationTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, idto)[0];
		TableColumn rangeColumn = null;
		TableColumn propertyColumn = associationTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
		// Buscamos la columna que tiene los datos del rango de la
		// propiedad porque no sabemos por que propiedad se están
		// relacionando las dos clases ni el rango real de la propiedad
		for (TableColumn column : associationTable.getAllColumns()) {
			if (column.getIdProperty() > 0){
				rangeColumn = column;
				break;
			}
		}
		if (domainColumn == null || rangeColumn == null){
			throw new DataErrorException("No se han podido encontrar las columnas del dominio o el rango en la tabla asociacion " + associationTable.getName());
		}
		Integer referencedClassIdto = associationTable.getColumnDomain(rangeColumn.getColumnName());
		//si no referencia a nada es xq es una dataProperty de cardinalidad multiple
		if (referencedClassIdto!=null) {
			// Construimos la consulta
			String sql = "SELECT " + cB + domainColumn.getColumnName() + cE + ", " + cB + rangeColumn.getColumnName() + cE;
			if (propertyColumn != null){
				// Si tiene columna property, significa que en dicha columna
				// está el identificador de la propiedad por la que se
				// relacionan las dos clases.
				sql += ", " + cB + propertyColumn.getColumnName() + cE;
			}
			sql += " FROM " + cB + associationTable.getName() + cE + " WHERE " + cB + domainColumn.getColumnName() + cE + " IN (";
			sql = addTableIdsList(tableIds, sql);
			sql += ") ORDER BY" + cB + domainColumn.getColumnName() + cE;
			List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
			LinkedObject object = null;
			for (List<String> objectData : queryResult) {
				String domainValue = objectData.get(0);
				String rangeValue = objectData.get(1);
				if (rangeValue == null){
					// Es un vinculo donde el rango se ha dejado a null, lo cual es un borrado mal efectuado
					System.err.println("[REGISTRO CORRUPTO: En la tabla " + associationTable.getName() + " hay un enlace corrupto de donde el rango es NULL. Se procede a borrarlo.");
					deleteIncompleteLink(associationTable, domainColumn, rangeColumn, domainValue, "NULL");
					continue;
				}
				String propertyValue = propertyColumn != null ? objectData.get(2) : null;
				int domainTableId = Integer.parseInt(domainValue);
				int rangeTableId = Integer.parseInt(rangeValue);
				int idProperty;
				if (propertyColumn == null){
					idProperty = rangeColumn.getIdProperty();
				}else{
					idProperty = Integer.parseInt(propertyValue);
				}
				if (object == null || object.getTableId() != domainTableId){
					object = objects.get(domainTableId);
				}
				// Construimos el vinculo entre los dos objetos y lo añadimos.
				ObjectLink link = new ObjectLink(idto, domainTableId, referencedClassIdto, rangeTableId, idProperty, associationTable.getId());
				object.addLink(link);
			}
		}
	}

	/**
	 * añade la informacion de los vinculos de los objetos de una clase cuando
	 * los datos del vinculo están en la tabla de la clase del rango de la
	 * propiedad.
	 * 
	 * @param idto
	 *            Identificador de la clase a la que pertenecen los objetos que
	 *            tienen que apuntar a la tabla del rango.
	 * @param tableIds
	 *            Identificadores de los objetos de los que queremos saber si
	 *            tienen vinculos.
	 * @param objects
	 *            Objetos donde vamos almacenado los vinculos que vamos
	 *            encontrando, donde la clave del mapa es el tableId del objeto.<br>
	 *            Todos los objetos de este mapa son de la misma clase y por
	 *            ello no se indica el idto.
	 * @param rangeTable
	 *            Tabla del rango de la propiedad de donde tenemos que sacar los
	 *            datos.
	 * @throws SQLException
	 *             Si hay algun error en la sentencia SQL
	 * @throws NamingException
	 *             Si hay algun error en las comunicaciones con la base de
	 *             datos.
	 * @throws NoSuchColumnException 
	 */
	private void getObjectsFromDBReferencedRangeTable(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects, Table rangeTable) throws SQLException, NamingException, NoSuchColumnException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		ClassInfo domainClass = dataBaseMap.getClass(idto);
		Set<Integer> properties = domainClass.getPropertiesReferencingClass(rangeTable.getId());
		if (properties == null){
			// El rango de la propiedad es una clase padre de idto.
			properties = new HashSet<Integer>();
			ClassInfo rangeClass = dataBaseMap.getClass(rangeTable.getId());
			for (Integer parentClassIdto : rangeClass.getParentClasses()){
				Set<Integer> propertiesToParent = domainClass.getPropertiesReferencingClass(parentClassIdto);
				if (propertiesToParent != null){
					properties.addAll(propertiesToParent);
				}
			}
		}
		List<TableColumn> linkColumns = new LinkedList<TableColumn>();
		TableColumn tableIdColumn = rangeTable.getDataPropertyColumn(IQueryInfo.ID_TABLE_ID);
		for (Integer idProperty : properties) {
			if (! rangeTable.isObjectProperty(idProperty)){
				// Esta tabla no contiene informacion sobre dicha propiedad.
				continue;
			}
			for (String columnName : rangeTable.getColumnNamesContainingProperty(idProperty)) {
				if (rangeTable.getColumnDomain(columnName).equals(idto)){
					// De las columnas que contienen informacion sobre
					// dicha propiedad, solo nos interesan las que
					// apuntan a la clase de los objetos que estamos
					// buscando.
					linkColumns.add(rangeTable.getColumnByName(columnName));
				}
			}
		}
		// Montamos la consulta
		String sql = "SELECT " + cB + tableIdColumn.getColumnName() + cE;
		for (TableColumn column : linkColumns) {
			sql += ", " + cB + column + cE;
		}
		sql += " FROM " + cB + rangeTable.getName() + cE + " WHERE ";
		boolean first = true;
		for (TableColumn column : linkColumns) {
			if (! first){
				sql += " OR ";
			}
			sql += cB + column.getColumnName() + cE + " IN (";
			sql = addTableIdsList(tableIds, sql);
			sql += ")";
			first = false;
		}
		// Obtenemos los datos
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		for (List<String> objectData : queryResult) {
			// La primera columna es el tableId del objeto referenciado.
			int referencedTableId = Integer.parseInt(objectData.get(0));
			int i = 0;
			for (TableColumn tableColumn : linkColumns) {
				i ++;
				String domainIdValue = objectData.get(i);
				if (domainIdValue != null){
					// Si la columna tiene valor, cogemos el valor, que
					// sera el tableId del objeto del dominio de la
					// propiedad, y montamos el vinculo.
					int domainId = Integer.parseInt(domainIdValue);
					LinkedObject object = objects.get(domainId);
					ObjectLink link = new ObjectLink(idto, domainId, rangeTable.getId(), referencedTableId, tableColumn.getIdProperty(), rangeTable.getId());
					object.addLink(link);
				}
			}
		}
	}

	/**
	 * añade la informacion de los vinculos de otras clases que están
	 * referenciando a la nuestra.
	 * 
	 * @param idto
	 *            Identificador de la clase de los objetos a la que tienen que
	 *            apuntar las relaciones.
	 * @param tableIds
	 *            Identificadores de los objetos de la clase que nos interesa
	 *            saber si son apuntados.
	 * @param objects
	 *            Objetos donde tenemos que ir guardando la informacion de los
	 *            vinculos.
	 * @throws NamingException
	 *             Si hay algun error en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la consulta SQL.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NoSuchColumnException 
	 */
	private void getObjectsFromDBReferencing(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects) throws DataErrorException, SQLException, NamingException, NoSuchColumnException {
		Set<ClassInfo> referencingClasses = dataBaseMap.getClassesReferencingClass(idto);
		if (referencingClasses == null){
			return;
		}
		for (ClassInfo domainClass : referencingClasses) {
			// Iteramos sobre todas las clases que están apuntando a la clase
			// del objeto del que queremos saber los vinculos entratntes.
			Table domainTable = dataBaseMap.getTable(domainClass.getIdto());
			Set<Integer> properties = domainClass.getPropertiesReferencingClass(idto);
			if (properties == null){
				// El rango de la propiedad es una clase padre de idto.
				properties = new HashSet<Integer>();
				ClassInfo rangeClass = dataBaseMap.getClass(idto);
				for (Integer parentClassIdto : rangeClass.getParentClasses()){
					Set<Integer> propertiesToParent = domainClass.getPropertiesReferencingClass(parentClassIdto);
					if (propertiesToParent != null){
						properties.addAll(propertiesToParent);
					}
				}
			}
			for (Integer idProperty : properties) {
				// Analizamos cada una de las propieadades que apuntan a nuestra
				// clase desde esta clase dominio.
				if (! domainTable.isObjectProperty(idProperty)){
					// solo nos interesan las objectProperty, que son las que
					// generan los enlaces conflictivos. Las DataProperties
					// externalizadas se borran en cascada.
					continue;
				}
				if (domainTable.isExternalizedProperty(idProperty)){
					if (dataBaseMap.isStructuralProperty(idProperty)){
						// Las estructurales externalizadas se añadieron cuando
						// se proceso la tabla del objeto.
						continue;
					}
					// La propiedad es una object property externalizada.
					for (Integer referencedTableIdto : domainTable.getExternalizedPropertyLocations(idProperty)) {
						Table referencedTable = dataBaseMap.getTable(referencedTableIdto);
						if (referencedTable == null){
							// Es el identificador de una vista, no de una tabla.
							continue;
						}
						if (! referencesClass(idto, idProperty, referencedTable)){
							// Si las columnas que contienen la propiedad no
							// apuntan a la clase de los objetos de los que
							// buscamos vinculos, esta tabla no nos interesa;
							continue;
						}
						if (referencedTable.isAssociation()){
							// Cogemos el vinculo de la tabla asociacion
							getObjectsFromDBReferencingInAssociationTable(idto, domainClass.getIdto(), tableIds, objects, idProperty, referencedTable);
						}else{
							// Cogemos la propiedad de la tabla del rango, que
							// sera realmente la tabla del la clase de los
							// objetos de los que estamos buscando los vinculos
							// entratnes.
							getObjectsFromDBReferencingInRangeTable(idto, domainClass.getIdto(), tableIds, objects, idProperty, referencedTable);
						}
					}
				}else{
					// La propiedad puede estar en la tabla del dominio o ser
					// una inversa de una estructural que no existe como tal.
					getObjectsFromDBReferencingInClassTable(idto, tableIds, objects, domainTable, idProperty);
				}
			}
		}
	}

	/**
	 * Coge el enlace que apunta a la clase de los objetos buscados cuando los
	 * datos del mismo se encuentran en la tabla de la clase del dominio de la
	 * propiedad.
	 * 
	 * @param idto
	 *            Clase a la que apuntan los enlaces y a la que pertenecen los
	 *            objetos.
	 * @param tableIds
	 *            Identificadores de los objetos de los que nos interesan los
	 *            vinculos.
	 * @param objects
	 *            Objetos donde se van guardando los enlaces encontrados.
	 * @param domainTable
	 *            Tabla de la clase del dominio de la propiedad.
	 * @param idProperty
	 *            Propiedad por la que se relacionan las dos clases.
	 * @throws NamingException
	 *             Si hay algun problema en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la consulta SQL
	 * @throws DataErrorException
	 *             Si hay algun problema inesperado con la estructura de los
	 *             datos.
	 * @throws NoSuchColumnException 
	 */
	private void getObjectsFromDBReferencingInClassTable(int idto, List<Integer> tableIds, Map<Integer, LinkedObject> objects, Table domainTable, Integer idProperty) throws SQLException, NamingException, DataErrorException, NoSuchColumnException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		// La primera columna que devuelva la consulta tiene que ser el tableId
		// del dominio de la propiedad y la segunda el del rango (que encajara
		// en alguno de los tableIds dados).
		String sql;
		String propertyColumnName = null;
		// Objeto que contendra el enlace a la tabla donde está la informacion
		// del vinculo.
		Table table;
		int idPropertyToSearch;
		// Deducimos la propiedad por la que tenemos que buscar el vinculo, pues
		// puede que idProperty sea la inversa de una estructural y los datos de
		// dicho tipo de propiedad no se representan en base de datos.
		if (domainTable.isObjectProperty(idProperty)){			
			idPropertyToSearch = idProperty;
		}else{
			
			Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
			if (inverseProperty == null){
				throw new DataErrorException("La tabla " + domainTable.getName() + " que deberaa conenter los datos de la propiedad " + idProperty + " no conoce dicha propiedad y la propiedad no tiene inversa estructural tampoco.");
			}			
			idPropertyToSearch = inverseProperty;
		}
		if (domainTable.isObjectProperty(idPropertyToSearch)){
			// La propiedad o su inversa están en la tabla del dominio de la propiedad.
			table = domainTable;
						
			List<String> cols=domainTable.getColumnNamesContainingProperty(idPropertyToSearch);
			if(cols==null) return;//este return no deberia suceder, excepto en tragus por modificaciones manuales del modelo, falla al buscar property programacion desde clase tarea
			
			for (String columnName : cols) {
				System.out.println("CDBGDEL COL:"+columnName);
				System.out.println("CDBGDEL "+domainTable.getColumnDomain(columnName));
				if (domainTable.getColumnDomain(columnName).equals(idto)){
					propertyColumnName = columnName;
					break;
				}
			}
			if (propertyColumnName == null){
				throw new DataErrorException("La tabla " + domainTable.getName() + " no tiene ninguna columna asociada a la propiedad " + idProperty + " aunque la reconoce como ObjectProperty.");
			}
			sql = "SELECT " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + ", " + cB + propertyColumnName + cE + " FROM " + cB + domainTable.getName() + cE + " WHERE " + cB + propertyColumnName + cE + " IN (";
			sql = addTableIdsList(tableIds, sql);
			sql += ") ORDER BY " + cB + propertyColumnName + cE;
		}else{
			// idProperty es la inversa de una estrucutral y los datos de dicha
			// estructural están en la tabla del rango de la propiedad.
			Table rangeTable = dataBaseMap.getTable(idto);
			table = rangeTable;
			if (! rangeTable.isObjectProperty(idPropertyToSearch)){
				throw new DataErrorException("No se pueden conseguir los datos de la propiedad " + idProperty + ", que tiene como inversa " + idPropertyToSearch + ", porque la inversa no está localizada en ninguna de las tablas de las clases que vincula");
			}
			String domainColumnName = null;
			for (String columnName : rangeTable.getColumnNamesContainingProperty(idPropertyToSearch)) {
				if (rangeTable.getColumnDomain(columnName).equals(domainTable.getId())){
					domainColumnName = columnName;
					break;
				}
			}
			if (domainColumnName == null){
				throw new DataErrorException("No se ha encontrado en la tabla del rango la columna que guarda la informacion de la inversa de la propiedad " + idProperty + ", la propiedad " + idPropertyToSearch);
			}
			sql = "SELECT " + cB + domainColumnName + cE + ", " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " FROM " + cB + rangeTable.getName() + cE + " WHERE " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE + " IN (";
			sql = addTableIdsList(tableIds, sql);
			sql += ") ORDER BY " + cB + IQueryInfo.COLUMN_NAME_TABLEID + cE;
		}
		// Analizamos los datos obtenidos y creamos los vinculos.
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		LinkedObject object = null;
		for (List<String> objectData : queryResult) {
			String domainTableIdValue = objectData.get(0);
			String rangeTableIdValue = objectData.get(1);
			if (domainTableIdValue == null || rangeTableIdValue == null){
				// No existe vinculo.
				continue;
			}
			int domainTableId = Integer.parseInt(domainTableIdValue);
			int rangeTableId = Integer.parseInt(rangeTableIdValue);
			if (object == null || object.getTableId() != rangeTableId){
				object = objects.get(rangeTableId);
			}
			ObjectLink link = new ObjectLink(domainTable.getId(), domainTableId, idto, rangeTableId, idProperty, table.getId());
			object.addLink(link);
		}
	}

	/**
	 * añade al final de la cadena SQL dada, la lista de los tableIds dados
	 * separados por comas.
	 * 
	 * @param tableIds
	 *            Lista de los tableId a añadir.
	 * @param sql
	 *            Cadena SQL a modificar.
	 * @return Cadena SQL resultante tras añadir los tableId.
	 */
	private String addTableIdsList(List<Integer> tableIds, String sql) {
		boolean first = true;
		for (Integer tableId : tableIds) {
			if (! first){
				sql += ", ";
			}
			sql += tableId.toString();
			first = false;
		}
		return sql;
	}

	/**
	 * Coge los enlcaces que apuntan a la clase de los objetos buscados cuando
	 * los datos del mismo se encuentran en una tabla asociacion.
	 * 
	 * @param idto
	 *            Clase a la que apuntan los enlaces y a la que pertenecen los
	 *            objetos.
	 * @param domainIdto
	 *            Identificador de la clase del dominio de la propiedad.
	 * @param tableIds
	 *            Identificadores de los objetos de los que nos interesan los
	 *            vinculos.
	 * @param objects
	 *            Objetos donde se van guardando los enlaces encontrados.
	 * @param idProperty
	 *            Identificador de la propiedad por la que se están relacionando
	 *            las dos clases.
	 * @param associationTable
	 *            Tabla asociacion entre las dos clases.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NamingException
	 *             Si hay algun error al conectar con la base de datos.
	 * @throws SQLException
	 *             Si hay algun error en la consulta SQL.
	 */
	private void getObjectsFromDBReferencingInAssociationTable(int idto, int domainIdto, List<Integer> tableIds, Map<Integer, LinkedObject> objects, int idProperty, Table associationTable) throws DataErrorException, SQLException, NamingException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		// Obtenemos todas las columnas que vamos a utilizar en la consulta.
		TableColumn [] domainColumns = associationTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, domainIdto);
		TableColumn [] rangeColumns = associationTable.getObjectPropertyColumn(idProperty, idto);
		TableColumn propertyColumn = associationTable.getDataPropertyColumn(IQueryInfo.ID_PROPERTY);
		if (domainColumns == null || rangeColumns == null || domainColumns[0] == null || rangeColumns[0] == null){
			throw new DataErrorException("No se han encontrado las columnas necesarias para asociar las clases [Dominio=" + domainIdto + ", Rango=" + idto +"] en la tabla " + associationTable.getName());
		}
		String sql = "SELECT " + cB + domainColumns[0].getColumnName() + cE + ", " + cB + rangeColumns[0].getColumnName() + cE + " FROM " + cB + associationTable.getName() + cE + " WHERE " + cB + rangeColumns[0].getColumnName() + cE + " IN (";
		sql = addTableIdsList(tableIds, sql);
		sql += ")";
		if (propertyColumn != null){
			// Si hay columna propiedad, filtramos para solo obtener los datos
			// de la propiedad que nos han indicado.
			sql += " AND " + cB + propertyColumn.getColumnName() + cE + "=" + idProperty;
		}
		sql += " ORDER BY " + cB + rangeColumns[0].getColumnName() + cE;
		// Extraemos los datos obtenidos al hacer la consulta y generamos los
		// enlaces.
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		LinkedObject object = null;
		for (List<String> objectData : queryResult) {
			String domainTableIdValue = objectData.get(0);
			String rangeTableIdValue = objectData.get(1);
			if (domainTableIdValue == null){
				System.err.println("vinculo incompleto en la tabla " + associationTable.getName() + " donde el dominio es NULL. Se procede a borrarlo.");
				deleteIncompleteLink(associationTable, domainColumns[0], rangeColumns[0], "NULL", rangeTableIdValue);
				continue;
			}
			int domainTableId = Integer.parseInt(domainTableIdValue);
			int rangeTableId = Integer.parseInt(rangeTableIdValue);
			if (object == null || object.getTableId() != rangeTableId){
				object = objects.get(rangeTableId);
			}
			ObjectLink link = new ObjectLink(domainIdto, domainTableId, idto, rangeTableId, idProperty, associationTable.getId());
			object.addLink(link);
		}
	}

	/**
	 * Coge los enlaces que apuntan a la clase de los objetos buscados cuando
	 * los datos del mismo se encuentran en la tabla del rango de la propiedad,
	 * es decir, en la tabla de la clase de los objetos.
	 * 
	 * @param idto
	 *            Clase a la que apuntan los enlaces y a la que pertenecen los
	 *            objetos.
	 * @param domainIdto
	 *            Identificador de la clase del domino de la propiedad.
	 * @param tableIds
	 *            Identificadores de los objetos de los que nos interesan los
	 *            enlaces.
	 * @param objects
	 *            Objetso donde se van guardando los enlaces encontrados.
	 * @param idProperty
	 *            Identificador de la propiedad por la que se están relacionando
	 *            las dos clases.
	 * @param referencedTable
	 *            Tabla del rango de la propiedad.
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NamingException
	 *             Si hay algun error en las comunicaciones con la base de
	 *             datos.
	 * @throws SQLException
	 *             Si hay algun error en la sentecia SQL.
	 */
	private void getObjectsFromDBReferencingInRangeTable(int idto, int domainIdto, List<Integer> tableIds, Map<Integer, LinkedObject> objects, int idProperty, Table referencedTable) throws DataErrorException, SQLException, NamingException {
		String cB = generateSQL.getCharacterBegin(), cE = generateSQL.getCharacterEnd();
		TableColumn rangeColumn = referencedTable.getDataPropertyColumn(IQueryInfo.ID_TABLE_ID);
		TableColumn domainColumn = referencedTable.getObjectPropertyColumn(idProperty, domainIdto)[0];
		if (rangeColumn == null || domainColumn == null){
			throw new DataErrorException("No se han encontrado las columnas para poder vincular mediante la propiedad " + idProperty + " las clases [Dominio=" + domainIdto + ", Rango=" + idto + "] en la tabla " + referencedTable.getName());
		}
		
		String sql = "SELECT " + cB + domainColumn.getColumnName() + cE + ", " + cB + rangeColumn.getColumnName() + cE + " FROM " + cB + referencedTable.getName() + cE + " WHERE " + cB + rangeColumn.getColumnName() + cE + " IN (";
		sql = addTableIdsList(tableIds, sql);
		sql += ") ORDER BY " + cB + rangeColumn.getColumnName() + cE;
		List<List<String>> queryResult = DBQueries.executeQuery(fcdb, sql);
		LinkedObject object = null;
		for (List<String> objectData : queryResult) {
			String domainTableIdValue = objectData.get(0);
			String rangeTableIdValue = objectData.get(1);
			if (domainTableIdValue == null){
				// No existe vinculo por esta propiedad.
				continue;
			}
			int domainTableId = Integer.parseInt(domainTableIdValue);
			int rangeTableId = Integer.parseInt(rangeTableIdValue);
			if (object == null || object.getTableId() != rangeTableId){
				object = objects.get(rangeTableId);
			}
			ObjectLink link = new ObjectLink(domainIdto, domainTableId, idto, rangeTableId, idProperty, referencedTable.getId());
			object.addLink(link);
		}
	}

	/**
	 * Busca en base de datos los objetos referenciados por el objeto a primer
	 * nivel mediante una propiedad estructural, y los añade a la lista de
	 * objetos vinculados.
	 * 
	 * @param object
	 *            Objeto del que tenemos que sacar los vinculos.
	 * @return Lista de los objetos apuntados por el objeto dado mediante una
	 *         propiedad estructural.<br>
	 *         En ningun caso devolvera <code>null</code>. Si no hay ningun
	 *         objeto apuntado por una estructural, se devuelve una lista vacía.
	 * @throws NamingException
	 *             Si hay un error en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay un error en la consulta SQL
	 * @throws DataErrorException
	 *             Si hay algun error en la estructura de los datos.
	 * @throws NoSuchColumnException 
	 */
	private List<LinkedObject> getReferencedObjects(LinkedObject object) throws DataErrorException, SQLException, NamingException, NoSuchColumnException{
		List<LinkedObject> result = new LinkedList<LinkedObject>();
		// Mapeamos todos los objetos referenciados mediante una propiedad estructural, organizandolos por su idto.
		Map<Integer, Set<Integer>> referencedObjectsByIdto = new Hashtable<Integer, Set<Integer>>();
		for (ObjectLink link : object.getLinksFromThisObject()) {
			int idProperty = link.getIdProperty();
			if (dataBaseMap.isStructuralProperty(idProperty)){
				Set<Integer> referencedTableIds = referencedObjectsByIdto.get(link.getRange());
				if (referencedTableIds == null){
					referencedTableIds = new HashSet<Integer>();
					referencedObjectsByIdto.put(link.getRange(), referencedTableIds);
				}
				referencedTableIds.add(link.getRangeId());
			}
		}
		// Recorremos el mapa creado, acumulando los objetos vinculados en la lista resultante.
		for (Integer idto : referencedObjectsByIdto.keySet()) {
			List<Integer> tableIds = new LinkedList<Integer>(referencedObjectsByIdto.get(idto));
			List<LinkedObject> objects = getObjectsFromDB(idto, tableIds);
			if(objects!=null ) result.addAll(objects);
		}
		return result;
	}

	/**
	 * Indica si un objeto es borrable.
	 * 
	 * @param object
	 *            Objecto que se quiere saber si se puede borrar.
	 * 
	 * @return <code>true</code> si se puede borrar el objeto.
	 */
	private boolean isDeleteableObject(LinkedObject object){
		boolean deletable = true;
		ObjectIdentifier objectIdentifier = new ObjectIdentifier(object.getIdto(), object.getTableId());
		// Buscamos el conjunto que almacena los identificadores de objetos que
		// apuntan a este objeto y que impieden que se borre.
		Set<ObjectIdentifier> referencingObjects = linksToNonDeletedObjects.get(objectIdentifier);
		if (referencingObjects == null){
			// Si el conjunto no estaba creado, lo creamos.
			referencingObjects = new HashSet<ObjectIdentifier>();
			linksToNonDeletedObjects.put(objectIdentifier, referencingObjects);
		}
		
		int i=0;
		for (ObjectLink link : object.getLinksToThisObject()) {
			int idProperty = link.getIdProperty();
			Integer inverseProperty = dataBaseMap.getInverseProperty(idProperty);
			if (dataBaseMap.isStructuralProperty(idProperty) || inverseProperty == null || ! object.hasInverseLink(link, inverseProperty)){
				// Hay un vinculo a este objeto sin vinculo inverso.
				referencingObjects.add(new ObjectIdentifier(link.getDomain(), link.getDomainId()));
				deletable = false;
				i++;
			}
			if(i==LIMIT_LINKS_TO_INDELIBLE_OBJECT){
				break;
			}
		}
		return deletable;
	}

	/**
	 * Comprueba si la tabla dada tiene alguna columna que apunte a la clase
	 * indicada mediante una propiedad especifica.<br>
	 * La tabla puede ser una tabla de una clase, en cuyo caso se busca que la
	 * clase de la que representa datos la tabla sea la clase indicada; o bien,
	 * puede ser una tabla asociacion, en cuyo caso la columna que guarda datos
	 * de la clase del rango debe apuntar a la clase indicada.
	 * 
	 * @param idto
	 *            Clase de la que buscamos alguna referencia en la tabla, es
	 *            decir, una columna que contenga datos de los indentificadores
	 *            de dicha clase.
	 * @param idProperty
	 *            Propiedad de la que tienen que guardar datos las columnas que
	 *            apunten a la clase especificada.
	 * @param referencedTable
	 *            Tabla en la que tenemos que buscar los datos.
	 * @return devolvera <code>true</code> si la tabla tiene informacion de la
	 *         clase indicada.
	 * @throws NoSuchColumnException 
	 */
	private boolean referencesClass(int idto, Integer idProperty, Table referencedTable) throws NoSuchColumnException {
		boolean result = false;
		if (referencedTable.isAssociation()){
			for (String columnName : referencedTable.getColumnNamesContainingProperty(idProperty)) {
				Integer columnClass = referencedTable.getColumnDomain(columnName);
				if (columnClass != null && columnClass.equals(idto)){
					result = true;
					break;
				}
			}
		}else{
			result = referencedTable.getId().equals(idto);
		}
		return result;
	}
	
	
}
