package dynagent.server.dbmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dynagent.common.Constants;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;

/**
 * Esta clase se va a encargar de almacenar todos los datos de una tabla que se
 * encuentre en la base de datos. Para interactuar con la tabla se deben usar
 * los metodos que ofrece esta clase, pues se guardan muchas relaciones
 * internamente y cualquier intento de modificacion directa de dichas
 * estructuras podria obviar alguna de las relaciones que se encarga de mantener
 * el modelo de la tabla.<br>
 * <br>
 * Hay que tener en cuenta que el mapeo de la tabla surge de los datos
 * analizados en las tablas: <b>instances</b>, <b>t_herencias</b> y
 * <b>properties</b>. De esta manera, si se realiza algun cambio durante la vida
 * de la base de datos sin realizar los cambios pertinentes en las tablas de
 * datos, puede que se genere el nuevo mapa de la base de datos tal y como se
 * esperaria, pero ese mapa no reflejara la realidad que se encuentra en la base
 * de datos. La moraleja: <i>"Si se quiere cambiar el modelo hay que importar de nuevo construyendo el nuevo modelo de base de datos"
 * </i><br>
 * <br>
 * En cuanto a como almacena esta clase los datos, imaginemos que tenemos la
 * siguiente tabla:<br>
 * <table border="2">
 * <theader>
 * <th>tableId</th>
 * <th>ido</th>
 * <th>nombre</th>
 * <th>apellidos</th>
 * <th>NIF-CIF</th>
 * <th>dirección</th>
 * </theader> <tbody>
 * <tr>
 * <td>4</td>
 * <td>8795461</td>
 * <td>Manolo</td>
 * <td>Sanchez Hidalgo</td>
 * <td>45679841B</td>
 * <td>5</td>
 * </tr>
 * </tbody>
 * </table>
 * <br>
 * <br>
 * Cada una de las columnas de la tabla va a estar representada por un objeto
 * del tipo <code>TableColumn</code> que guarda informacion relevante sobre como
 * debe crearse una columna y que tipo de datos se usa en base de datos para
 * representar su contenido. Una de las cosas más importantes a tener en cuenta
 * es que, todo campo que sea <b>Foreign Key</b> va a tener como tipo de datos
 * en la base de datos <i>INT UNSIGNED</i>.<br>
 * Las columnas <i>tableId</i> e <i>ido</i> son columnas que se generaran
 * automaticamente en todas las tablas que no sean de asociacion. Dichas
 * columnas no hacen referencia a ninguna property, sino que sirven para
 * identificar un registro de la tabla. La columna <i>ido</i> se mantiene por
 * razones de compatibilidad con versiones anteriores e, internamente para la
 * base de datos, no tiene ninguna logica asociada.<br>
 * Es más, internamente la tabla va a mantener una lista de todas las foreign
 * key que tiene. En principio, esto se hace para facilitar la generacion de la
 * sentencia SQL de creación, pero podria ser de utilidad saber que claves
 * foraneas se han creado.<br>
 * <br>
 * Aunque cada tabla, como representacion de una clase especifica, solo va a
 * tener una forma de relacionarse con otra tabla especifica a traves de una
 * property, una property puede tener rango multiple. Esto significa, que si se
 * pregunta por el rango de una property, este objeto <code>Table</code> va a
 * ser capaz de, dada una property, decir a que clases apunta mediante el metodo
 * <code>getPropertyRange(idProperty : int) : Set< Integer ></code>.<br>
 * Otro dato importante a tener en cuenta es que la columna que guarda
 * informacion de una property de una clase representada por esta tabla, no
 * tiene por que estar necesariamente en esta tabla. El caso más claro es cuando
 * una property tiene cardinalidad mayor que 1. En estos casos se pueden crear
 * tablas asociacion o poner la referencia en la tabla del tipo de objeto al que
 * apunta dicha property. Es por ello por lo que existe el metodo
 * {@link #getExternalizedPropertyLocations(int)}. Dicho metodo nos devolvera el idto de la
 * tabla que contiene la informacion de dicha property.<br>
 * 
 * @see TableColumn
 * @see IQueryInfo
 * @see IQueryInfoColumn
 */
public class Table implements IQueryInfo, Serializable {

	private static final long serialVersionUID = 7397715908454935748L;
	/** Nombre de esta tabla */
	private String tableName;
	/** Idto de la tabla. */
	private int tableIdto;
	/** Si se trata de una tabla asociacion aqui tendremos mas de un valor, los dominios, si no solo uno igual al tableIdto **/
	private HashSet<Integer> originalTableIdtos;
	/**
	 * Mapa de las columnas de la tabla, referenciadas por el nombre de la
	 * columna.
	 */
	private Hashtable<String, TableColumn> columns;
	/**
	 * Mapa de los nombres de las columnas indexados por el idProperty de la que
	 * almacenan datos
	 */
	private Hashtable<Integer, Set<String>> propertyColumnMap;
	/**
	 * Rangos de cada una de las propiedades de la clase a la que representa
	 * esta tabla.
	 */
	private Hashtable<Integer, Set<Integer>> propertyRanges;
	/** Mapa de las propiedades de las que contiene informacion una columna */
	private Map<String, Integer> columnPropertyMap;
	/** Indica si se trata de una tabla asociacion. */
	private boolean isAssociationTable;
	/** Conjunto de tablas que son referenciadas por esta clase. */
	private Set<Integer> referencedTables;
	/** Mapa de la localización de cada propiedad externalizada. */
	private Hashtable<Integer, Set<Integer>> propertyLocation;
	/** Lista de los índices que se tienen que crear para esta tabla. */
	private List<Set<TableColumn>> indexList;
	/**
	 * Mapa de todas las claves foraneas de la tabla indexadas por el nombre de
	 * la columna a la que están vinculadas.
	 */
	private Hashtable<String, ForeignKey> foreignKeys;
	/** Conjunto que contiene las columnas que partipan en la clave primaria. */
	private Set<String> primaryKeyColumns;
	/**
	 * Mapa que dado el nombre de una columna, devuelve el idto de la tabla a la
	 * que apunta.
	 */
	private Map<String, Integer> columnDomainMap;
	/** Conjunto de todas las DataProperties que conoce esta tabla */
	private Set<Integer> dataProperties;
	/** Conjunto de todas las ObjectProperties que conoce esta tabla */
	private Set<Integer> objectProperties;
	/** Conjunto de todas las propiedades que se han incluido en esta tabla y que provienen de externalizarlas de otra tabla. */
	private Set<Integer> foreignProperties;
	/** Conjunto de todas las propiedades que se han externalizado a otra tabla. */
	private Set<Integer> externalizedProperties;
	/**
	 * Indice que se añade como sufijo a aquellos nombres de tabla que son
	 * demasiado largos.
	 */
	private static int tableNameIndex = 0;

	/**
	 * Crea una tabla para representar los datos de la clase dada basindose en
	 * la informacion de la clase.
	 * 
	 * @param classInfo
	 *            Clase de la cual va a representar datos la tabla.
	 */
	public Table(ClassInfo classInfo, Map<Integer, ClassInfo> classes) {
		initializeCommonAttributes(false);
		this.tableName = classInfo.getName();
		initializeProperties(classInfo, classes);
		this.tableName = this.tableName.toLowerCase().replaceAll("\\.", "");
		this.tableIdto = classInfo.getIdto();
		originalTableIdtos=new HashSet<Integer>();
		originalTableIdtos.add(tableIdto);
		
		try {
			TableColumn column  = addDataPropertyColumn(COLUMN_NAME_TABLEID, Constants.IDTO_INT, ID_TABLE_ID);
			column.setAutoInc(true);
			column.setNotNull(true);
			column.setUnsigned(true);
			addDataPropertyColumn(COLUMN_NAME_DESTINATION, Constants.IDTO_STRING, ID_DESTINATION);
		} catch (ColumnOverlapException e) {
		}
		// Las excepciones no se van a dar en la creación pues no va a haber
		// conflicto de nombres aque
		primaryKeyColumns.add(COLUMN_NAME_TABLEID);
	}

	/**
	 * Inicializa el objeto de tal manera de que si se le pasan dos clases y se
	 * le dice que es una tabla asociacion, construye la tabla asignandole el
	 * nombre de las dos clases separadas por una '#'.<br>
	 * Si se dice que no es asociacion, no se mira el segundo elmento del array
	 * de classes y se construye una tabla normal con la informacion del primer
	 * elemento del array.
	 * 
	 * @param classes
	 *            Array de dos elementos que contiene las clases en las que se
	 *            va a basar la tabla para conseguir la informacion para
	 *            construirse.<br>
	 *            En una asociacion, la primera del array sera la clase del
	 *            dominio y la segunda sera la del rango.
	 * @param id
	 *            Identificador numero que tiene la tabla.
	 */
	public Table(ClassInfo [] classes, int id) {
		initializeCommonAttributes(true);
		
		this.tableName = classes[0].getName() + "#" + classes[1].getName();
		if (this.tableName.length() > 60){
			tableNameIndex ++;
			this.tableName = this.tableName.substring(0, 55) + "~" + tableNameIndex;
		}
		this.tableName = this.tableName.toLowerCase().replaceAll("\\.", "");
		dataProperties.add(ID_TABLE_ID);
		objectProperties.add(ID_DOMAIN);
		this.tableIdto = id;
		originalTableIdtos=new HashSet<Integer>();
		originalTableIdtos.add(classes[0].getIdto());
		originalTableIdtos.add(classes[1].getIdto());

		try {
			TableColumn column = addDataPropertyColumn(COLUMN_NAME_TABLEID, Constants.IDTO_INT, ID_TABLE_ID);
			column.setAutoInc(true);
			column.setUnsigned(true);
			column.setNotNull(true);
		} catch (ColumnOverlapException e) {
		}
		// Las excepciones no se van a dar en la creación pues no va a haber
		// conflicto de nombres aque
		primaryKeyColumns.add(COLUMN_NAME_TABLEID);
	}

	/**
	 * Constructor para las tablas donde se externalizan DataProperties de
	 * cardinalidad mayor que 1.<br>
	 * Este tipo de tablas se considerara asociacion por defecto. tambien, no
	 * habra que modificar las columnas de esta tabla pues este constructor se
	 * encargara de poner toda la informacion necesaria de la tabla, asi como
	 * incluir las columnas e inicializar las estructuras con los datos listos
	 * para ser usados o construir la tabla.
	 * 
	 * @param domainClass
	 *            Clase que tiene la DataProperty que se ha externalizado.
	 * @param property
	 *            Informacion de la propiedad que se ha externalizado.
	 * @param id
	 *            Identificador numero que se le ha de dar a la tabla.
	 */
	public Table(ClassInfo domainClass, PropertyInfo property, int id){
		initializeCommonAttributes(true);
		String domainClassName = domainClass.getName();
		String propertyName = property.getName();
		tableName = domainClassName + "#" + propertyName;
		tableName = tableName.replaceAll("\\.", "");
		if (tableName.length() > 60){
			tableNameIndex ++;
			tableName = tableName.substring(0,55) + "~" + tableNameIndex;
		}
		tableName = tableName.toLowerCase();
		objectProperties.add(ID_DOMAIN);
		dataProperties.add(property.getIdProp());
		dataProperties.add(ID_TABLE_ID);
		
		Integer propertyType = property.getPropertyTypes().iterator().next();
		try {
			TableColumn column = addDataPropertyColumn(COLUMN_NAME_TABLEID, Constants.IDTO_INT, ID_TABLE_ID);
			column.setAutoInc(true);
			column.setUnsigned(true);
			column.setNotNull(true);
			addDataPropertyColumn(propertyName, propertyType, property.getIdProp());
			addObjectPropertyColumn(domainClassName + SUFFIX_ID, ID_DOMAIN, domainClass.getIdto());
		} catch (ColumnOverlapException e) {
		} catch (NoSuchColumnException e) {
		}
		// Las excepciones no se van a dar en la creación pues no va a haber
		// conflicto de nombres aque
		primaryKeyColumns.add(COLUMN_NAME_TABLEID);
		this.tableIdto = id;
		originalTableIdtos=new HashSet<Integer>();
		originalTableIdtos.add(tableIdto);
	}

	/**
	 * Inicializa todos los atributos de la clase que no necesitan saber el tipo
	 * de Tabla sobre el que estamos trabajando.
	 * 
	 * @param isAssociationTable
	 *            Inidica si la tabla actual es asociacion.
	 */
	private void initializeCommonAttributes(boolean isAssociationTable) {
		this.columns = new Hashtable<String, TableColumn>();
		this.propertyRanges = new Hashtable<Integer, Set<Integer>>();
		this.referencedTables = new HashSet<Integer>();
		this.propertyLocation = new Hashtable<Integer, Set<Integer>>();
		this.indexList = new LinkedList<Set<TableColumn>>();
		this.foreignKeys = new Hashtable<String, ForeignKey>();
		this.primaryKeyColumns = new HashSet<String>();
		this.columnDomainMap = new Hashtable<String, Integer>();
		this.propertyColumnMap = new Hashtable<Integer, Set<String>>();
		this.columnPropertyMap = new Hashtable<String, Integer>();
		this.isAssociationTable = isAssociationTable;
		this.objectProperties = new HashSet<Integer>();
		this.dataProperties = new HashSet<Integer>();
		this.foreignProperties = new HashSet<Integer>();
		this.externalizedProperties = new HashSet<Integer>();
	}

	/**
	 * Inicializa los valores conjuntos {@link #dataProperties} y
	 * {@link #objectProperties}, basindose en la informacion que se puede
	 * obtener de la clase dada.<br>
	 * Este metodo solo se puede usar en aquellas tablas que representan a una
	 * clase, es decir, una tabla que no es asociacion.
	 * 
	 * @param tableClass
	 *            Clase de la que contiene informacion esta tabla.
	 * @param classes
	 *            Mapa de todas las clases conocidas.
	 */
	private void initializeProperties(ClassInfo tableClass, Map<Integer, ClassInfo> classes){
		dataProperties.add(ID_DESTINATION);
		dataProperties.add(ID_TABLE_ID);
		for (PropertyInfo property : tableClass.getAllProperties()) {
			boolean dataProperty = true;
			for (Integer propertyType : property.getPropertyTypes()) {
				if (! Constants.isDataType(propertyType)){
					dataProperty = false;
					break;
				}
			}
			if (dataProperty){
				dataProperties.add(property.getIdProp());
			}else{
				objectProperties.add(property.getIdProp());
				Set<Integer> propertyRanges = getPropertyRanges(property, classes);
				this.propertyRanges.put(property.getIdProp(), propertyRanges);
			}
		}
	}

	/**
	 * añade una columna que representa una DataProperty.<br>
	 * Si la propiedad indicada no está reconocida como DataProperty en esta
	 * tabla, no se añadira la columna.<br>
	 * Se consiera que {@link IQueryInfo#ID_DESTINATION},
	 * {@link IQueryInfo#ID_TABLE_ID} e {@link IQueryInfo#ID_PROPERTY} son
	 * DataProperties, aunque esta última solo puede aparecer en las tablas
	 * asociacion.
	 * 
	 * @param columnName
	 *            Nombre que se le ha de dar a la columna.
	 * @param dataType
	 *            Tipo de datos que tiene que contener la columna como, por
	 *            ejemplo, {@link Constants#IDTO_STRING}.
	 * @param idProperty
	 *            Identificador numero de la propiedad con la que se ha de
	 *            vincular la columna
	 * @return Devuelve la columna añadida o <code>null</code> si no se inserta
	 *         ninguna columna.
	 * @throws ColumnOverlapException
	 *             Si se intenta añadir una columna con el mismo nombre que una
	 *             ya existente en la tabla.
	 */
	public TableColumn addDataPropertyColumn(String columnName, int dataType, int idProperty) throws ColumnOverlapException{
		if (! dataProperties.contains(idProperty)){
			System.err.println("[TABLE STRUCT ERROR] La propiedad " + idProperty + " que se ha intentado asociar con la columna " + columnName + " no se reconoce como una DataProperty en la tabla " + tableName);
			return null;
		}
		//System.out.println("TABLA "+tableName+" property:"+idProperty+" columna:"+columnName);
		if (columns.get(columnName) != null){
			throw new ColumnOverlapException("La tabla " + tableName + " ya contiene una columna denominada " + columnName);
		}
		TableColumn column = new TableColumn(columnName, dataType, idProperty);
		
		columns.put(columnName, column);
		columnPropertyMap.put(columnName, idProperty);
		
		Set<String> columnNames = new HashSet<String>();
		columnNames.add(columnName);
		propertyColumnMap.put(idProperty, columnNames);
		return column;
	}

	/**
	 * añade una columna para representar los datos de una ObjectProperty con un
	 * rango determinado.<br>
	 * Si el identificador de la propiedad no se reconoce como el identificador
	 * de una ObjectProperty, no se hara nada.<br>
	 * 
	 * La columna creada no tendra ninguna restricción en su creación. Cualquier
	 * restricción que se le quiera añadir debera indicarse sobre el objeto
	 * resultante de este metodo.<br>
	 * 
	 * A las columnas que representan objectProperties se les añadira un índice
	 * de manera automatica.
	 * 
	 * @param columnName
	 *            Nombre que se le ha de asignar a la columna.
	 * @param idProperty
	 *            Identificador de la propiedad de la que va a contener datos la
	 *            columna.
	 * @param range
	 *            Identificador numero de la clase a la que va a apuntar la
	 *            columna.
	 * @return Devuelve la columna añadida o <code>null</code> si no se inserta
	 *         ninguna columna.
	 * @throws ColumnOverlapException
	 *             Si se intenta añadir una columna con el mismo nombre que una
	 *             ya existente en la tabla.
	 * @throws NoSuchColumnException
	 *             Si se intenta añadir informacion sobre una columna que no
	 *             existe en la tabla.
	 */
	public TableColumn addObjectPropertyColumn(String columnName, int idProperty, int range) throws ColumnOverlapException, NoSuchColumnException{
		// Comprobamos que todo sea correcto antes de proceder a crear la
		// columna.
		if (! objectProperties.contains(idProperty)){
			System.err.println("[TABLE STRUCT ERROR] La propiedad " + idProperty + " que se ha intentado asociar con la columna " + columnName + " no se reconoce como una ObjectProperty en la tabla " + tableName);
			return null;
		}
		Set<Integer> propertyRanges = this.propertyRanges.get(idProperty);
		if (propertyRanges != null && ! propertyRanges.contains(range)){
			System.err.println("[TABLE STRUCT ERROR] El idto=" + range + " no está dentro de los rangos conocidos para la propiedad " + idProperty + " en la tabla " + tableName);
			return null;
		}
		if (columns.get(columnName) != null){
			throw new ColumnOverlapException("La tabla " + tableName + " ya contiene una columna denominada " + columnName);
		}
		
		TableColumn column = new TableColumn(columnName, Constants.IDTO_INT, idProperty);
		
		// Indicamos que los datos de esta columna apuntan a los de la tabla
		// cuyo idto es range.
		columnDomainMap.put(column.getColumnName(), range);
		// Guardamos las referencias necesarias en los distintos sitios que hace
		// falta.
		columnPropertyMap.put(columnName, idProperty);
		Set<String> columnNames = propertyColumnMap.get(idProperty);
		if (columnNames == null){
			columnNames = new HashSet<String>();
			propertyColumnMap.put(idProperty, columnNames);
		}
		columnNames.add(columnName);
		columns.put(columnName, column);
		// Añadimos el índice sobre esta columna
		Set<String> indexSet = new HashSet<String>();
		indexSet.add(columnName);
		addIndex(indexSet);
		
		return column;
	}

	/**
	 * Le indica a la tabla actual la localización de una property. Dicha
	 * localización puede ser la propia tabla o una localización externa como:
	 * <ul>
	 * <li>Tabla asociacion</li>
	 * <li>Tabla de otra clase</li>
	 * <li>Vista asociacion <i>(única posibilidad pues una propiedad de una
	 * tabla no se va a externalizar a una vista de una clase)</i></li>
	 * </ul>
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad
	 * @param locationId
	 *            Identificador de la localización de la propiedad. Puede ser el
	 *            identificador de una tabla o de una vista asociacion.
	 */
	public void addExternalizedProperty(int idProperty, int locationId) {
		externalizedProperties.add(idProperty);
		referencedTables.add(locationId);
		Set<Integer> propertyLocationSet = propertyLocation.get(idProperty);
		if (propertyLocationSet == null) {
			propertyLocationSet = new TreeSet<Integer>();
			propertyLocation.put(idProperty, propertyLocationSet);
		}
		propertyLocationSet.add(locationId);
	}

	/**
	 * añade un vinculo con una vista asociacion desde la tabla. Esto no marca
	 * la propiedad como externalizada ni añade el id a
	 * {@link #referencedTables}
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que se produce el vinculo
	 * @param locationId
	 *            Identificador de la vista asociacion
	 */
	public void addViewExternalizedProperty(int idProperty, int locationId){
		Set<Integer> propertyLocationSet = propertyLocation.get(idProperty);
		if (propertyLocationSet == null) {
			propertyLocationSet = new TreeSet<Integer>();
			propertyLocation.put(idProperty, propertyLocationSet);
		}
		propertyLocationSet.add(locationId);
	}

	/**
	 * añade una clave foranea que vincula los datos de una columna de la tabla
	 * actual con la columna de la tabla que se indica. Como no se indica
	 * ninguna configuarción especifica para el vinculo, se va a establecer que
	 * cuando se produzca un borrado o una actualización no se haga nada.<br>
	 * Se tiene que tener en cuenta que para vincular dos columnas en tablas
	 * distintas mediante una clave foranea, los tipos de ambas columnas deben
	 * ser iguales.
	 * 
	 * @param columnName
	 *            Columna de la tabla actual que se quiere usar para crear el
	 *            vinculo.
	 * @param targetTable
	 *            Nombre de la tabla a la que se va a apuntar para crear el
	 *            vinculo.
	 * @param targetColumn
	 *            Nombre de la columna a la que se va a apuntar con el vinculo.
	 * @throws NoSuchColumnException
	 *             Si el nombre de la columna de esta tabla sobre la que se
	 *             quiere crear la foreign key no existe.
	 * @throws ForeignKeyOverlapException
	 *             Si se intenta crear una clave foranea sobre una columna que
	 *             ya tiene declarada una columna foranea.
	 */
	public void addForeignKey(String columnName, String targetTable, String targetColumn) throws NoSuchColumnException, ForeignKeyOverlapException {
		this.addForeignKey(columnName, targetTable, targetColumn, ForeignKey.DONOTHING, ForeignKey.DONOTHING);
	}

	/**
	 * añade una clave foranea que vincula los datos de una columna de la tabla
	 * actual con la columna de la tabla que se indica, usando la configuración
	 * que se indica para el borrado y la actualización.
	 * 
	 * @param columnName
	 *            Columna de la tabla actual que se quiere usar para crear el
	 *            vinculo.
	 * @param targetTable
	 *            Nombre de la tabla a la que se va a apuntar para crear el
	 *            vinculo.
	 * @param targetColumn
	 *            Nombre de la columna a la que se va a apuntar con el vinculo.
	 * @param onDelete
	 *            Indica que operación se realiza al borrar. El identificador de
	 *            la operación está definido en la clase ForeignKey.
	 * @param onUpdate
	 *            Indica que operación se realiza al actualizar. El
	 *            identificador de la operación está definido en la clase
	 *            ForeignKey.
	 * @throws NoSuchColumnException
	 *             Si el nombre de la columna de esta tabl sobre la que se
	 *             quiere crear la foreign key no existe.
	 * @throws ForeignKeyOverlapException
	 *             Si se intenta crear una clave foranea sobre una columna que
	 *             ya tiene declarada una columna foranea.
	 * @see ForeignKey
	 */
	public void addForeignKey(String columnName, String targetTable, String targetColumn, int onDelete, int onUpdate) throws NoSuchColumnException, ForeignKeyOverlapException {
		// Comprobamos si existe la columna sobre la que se quiere declarar la
		// foreign key
		if (columns.get(columnName) == null) {
			throw new NoSuchColumnException("La columna " + columnName + " no existe en la tabla " + tableName);
		}

		// Comprobamos que la columna sobre la que queremos declarar la foreign
		// key no tenga ya delarada una clave
		// foranea ya.
		if (foreignKeys.get(columnName) != null) {
			throw new ForeignKeyOverlapException("No se pueden declarar dos claves foraneas sobre la columna " + columnName);
		}

		foreignKeys.put(columnName, new ForeignKey(tableName, columnName, targetTable, targetColumn, onUpdate, onDelete));
	}

	/**
	 * añade un índice a la tabla actual sobre las columnas indicadas. Si el
	 * conjunto está vacio o es nulo, no se llevara ninguna acción a cabo.
	 * 
	 * @param columnNames
	 *            Nombres de las columnas que participan en el índice.
	 * @throws NoSuchColumnException
	 *             Si alguno de los nombres de las columnas indicadas no existe
	 *             en la tabla.
	 */
	public void addIndex(Set<String> columnNames) throws NoSuchColumnException {
		for (String columnName : columnNames) {
			if (columns.get(columnName) == null || columnName == null) {
				throw new NoSuchColumnException("Error al intentar crear un índice con la columna " + columnName + " que no existe en la tabla" + tableName);
			}
		}
		// Comprobamos que el índice no exista ya. Como viene en base a nombres
		// de columnas y el orden en que se indique
		// las columnas no importa, la comprobación es más costosa.
		boolean exists = false;
		for (Set<TableColumn> tableColumnSet : indexList) {
			// El índice que se quiere crear es igual al actual hasta que se
			// demuestre lo contrario.
			boolean isTheSameIndex = true;
			for (TableColumn tableColumn : tableColumnSet) {
				for (String columnName : columnNames) {
					if (columnName.equalsIgnoreCase(tableColumn.getColumnName())) {
						isTheSameIndex = false;
					}
				}
			}
			if (isTheSameIndex) {
				exists = true;
				break;
			}
		}
		// Si no hemos encontrado el índice como uno de los ya existentes en la
		// lista, lo creamos.
		if (!exists) {
			Set<TableColumn> indexSet = new HashSet<TableColumn>();
			for (String columnName : columnNames) {
				indexSet.add(columns.get(columnName));
			}
			indexList.add(indexSet);
		}
	}

	/**
	 * Devuelve la informacion de todas las columnas que se encuentran en la
	 * tabla actual.
	 * 
	 * @return Conjunto con todas las columnas de la tabla actual. El orden en
	 *         que se devuelven las columnas no tiene porque ser el mismo orden
	 *         en que se encuentran en la tabla. El metodo nunca devulve
	 *         <code>null</code>, a lo sumo devolvera un conjunto vacio.
	 */
	public Set<TableColumn> getAllColumns() {
		Set<TableColumn> columnSet = new HashSet<TableColumn>();
		columnSet.addAll(columns.values());
		return columnSet;
	}

	/**
	 * Devuelve el conjunto de todas las tablas que sen referencidas por la
	 * tabla actual.
	 * 
	 * @return Conjunto de todas las tablas que son referenciadas.
	 */
	public Set<Integer> getAllReferencedTables() {
		return referencedTables;
	}

	/**
	 * Obtiene la configuración de una columna de la tabla dado su nombre.
	 * 
	 * @param columnName
	 *            Nombre de la columna de la que se quiere saber su
	 *            configuración.
	 * @return devolvera <code>null</code> si en la tabla actual no existe la
	 *         columna indicada.
	 */
	public TableColumn getColumnByName(String columnName) {
		return columns.get(columnName);
	}

	/**
	 * Devuelve el identificador de la tabla a la que está apuntando la
	 * referencia de la columna indicada
	 * 
	 * @param columnName
	 *            Nombre de la columna sobre la que se quiere consultar.
	 * @return devolvera <code>null</code> si dicha columna no apunta a ninguna
	 *         tabla o si dicha columna no existe.
	 */
	public Integer getColumnDomain(String columnName) {
		return columnDomainMap.get(columnName);
	}

	/**
	 * Obtiene una lista de las columnas que contienen informacion de la
	 * propiedad indicada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que se quieren saber las
	 *            columnas que tienen informacion de la misma..
	 * @return Devuelve la lista de columnas que contienen informacion de la
	 *         columna indicada. Si no se tiene ninguna columna relacionada con
	 *         dicha propiedad, devolvera una lista vacía.
	 * @throws TypeErrorException 
	 */
	public List<String> getColumnNamesContainingProperty(int idProperty) throws NoSuchColumnException {
		if (externalizedProperties.contains(idProperty)){
			System.err.println("[TABLE STRUCT ERROR] Se ha preguntado a la tabla " + tableName + " por las columnas de la propiedad=" + idProperty + " cuando esta está externalizada.");
			return null;
		}
		List<String> result = new LinkedList<String>();
		Set<String> propertyLocations = propertyColumnMap.get(idProperty);
		if (propertyLocations == null){
			if(isObjectProperty(idProperty)){
				String msg="[TABLE STRUCT ERROR] La ObjectProperty "+ idProperty + " seguramente tiene rango abstracto sin hijos especficos. Tabla=" + tableName;
				System.err.println(msg);
				return null;
				//throw new NoSuchColumnException("[TABLE STRUCT ERROR] La ObjectProperty "+ idProperty + " seguramente tiene rango abstracto sin hijos especficos. Tabla=" + tableName);
			}
			else{
				String msg="[TABLE STRUCT ERROR] La DataProperty "+ idProperty + " no tiene columna asociada en la tabla=" + tableName;
				System.err.println(msg);
				return null;
				//throw new NoSuchColumnException("[TABLE STRUCT ERROR] La DataProperty "+ idProperty + " no tiene columna asociada en la tabla=" + tableName);
			}
			
		}else{
			result.addAll(propertyLocations);
		}
		return result;
	}

	/**
	 * Crea la sentencia sql para crear esta tabla, teniendo en cuenta todas sus
	 * columnas. Además, añade dos columnas extra, la columna tableId, que sera
	 * la clave primaria de esta tabla, y la columna ido, por motivos de
	 * compatibilidad con modelos antiguos.<br>
	 * Por cada referencia en esta tabla a una object property, se creara un
	 * índice en la tabla.<br>
	 * El motor usado es InnoDB
	 * 
	 * @param bns
	 *            número de la dyna en la que se quiere crear la tabla.
	 * @param gestor
	 *            Nombre del gestor de base de datos con el que estamos
	 *            trabajando.
	 * 
	 * @return Cadena con la sentencia sql de creación lista para usarse.
	 * @throws TypeErrorException
	 *             Si el tipo indicado para una columna no está dentro de los
	 *             tipos contemplados como basicos en Constants.
	 */
	public String getSQLCreationStatement(int bns, String gestor) throws TypeErrorException {
		GenerateSQL generateSQL = new GenerateSQL(gestor);
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String sqlCreationStatement = "CREATE TABLE " + characterBegin + tableName + characterEnd + " (";
		boolean first = true;
		for (TableColumn tableColumn : columns.values()) {
			String columnStatement = "";
			if (!first) {
				columnStatement += ",";
			}
			columnStatement += " " + characterBegin + tableColumn.getColumnName() + characterEnd;
			if (tableColumn.isAutoInc() && gestor.equals(GestorsDBConstants.postgreSQL)){
				columnStatement += " SERIAL";
				sqlCreationStatement += columnStatement;
				first = false;
				continue;
			}
			switch (tableColumn.getColumnDataType()) {
			case Constants.IDTO_DATETIME:
			case Constants.IDTO_DATE:
			case Constants.IDTO_TIME:
				columnStatement += " BIGINT";
				break;
			case Constants.IDTO_BOOLEAN:
				columnStatement += " BOOLEAN";
				break;
			case Constants.IDTO_INT:
				columnStatement += " INT";
				break;
			case Constants.IDTO_DOUBLE:
				if (gestor.equals(GestorsDBConstants.postgreSQL)){
					columnStatement += " FLOAT8";
				}else{
					columnStatement += " DOUBLE";
				}
				break;
			case Constants.IDTO_FILE:
			case Constants.IDTO_IMAGE:
			case Constants.IDTO_STRING:
				columnStatement += " VARCHAR("+Constants.MAX_LENGHT_TEXT+")";
				break;
			case Constants.IDTO_MEMO:
				if (gestor.equals(GestorsDBConstants.postgreSQL)){
					columnStatement += " TEXT";
				}else{
					columnStatement += " LONGTEXT";
				}
				break;
			default:
				throw new TypeErrorException("El número " + tableColumn.getColumnDataType() + " no identifica a ningun tipo vlido.");
			}
			if (tableColumn.isUnsigned() && ! gestor.equals(GestorsDBConstants.postgreSQL)) {
				columnStatement += " UNSIGNED";
			}
			if (tableColumn.isNotNull()) {
				columnStatement += " NOT NULL";
			}
			// TODO Añadir Default Value a 0 para los tipo numeros
			if (tableColumn.isAutoInc()) {
				if (gestor.equals(GestorsDBConstants.mySQL)) {
					columnStatement += " AUTO_INCREMENT";
				} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
					columnStatement += " IDENTITY(1,1)";
				}
			}
			sqlCreationStatement += columnStatement;
			first = false;
		}
		sqlCreationStatement += ", PRIMARY KEY (";
		first = true;
		for (String columnName : primaryKeyColumns) {
			if (!first) {
				sqlCreationStatement += ",";
			}
			sqlCreationStatement += " " + characterBegin + columnName
					+ characterEnd;
			first = false;
		}
		sqlCreationStatement += ")";
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			sqlCreationStatement += ") ENGINE=InnoDB;";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			sqlCreationStatement += ") WITHOUT OIDS;";
		} else {
			sqlCreationStatement += ");";
		}
		return sqlCreationStatement;
	}

	/**
	 * Genera el SQL para añadir todas las restricciones a la tabla.
	 * 
	 * @param bns
	 *            número de la base de datos en la que se encuentra la tabla.
	 * @param gestor
	 * 			  Cadena que identifica al gestor de base de datos con el que se está trabajando.
	 * @return Cadena con la orden para modificar la tabla o <code>null</code>
	 *         si no hay ninguna restricción sobre la tabla.
	 */
	public String getSQLRestrictionsStatement(int bns, String gestor) {
		GenerateSQL generateSQL = new GenerateSQL(gestor);
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String alterStatement = "ALTER TABLE " + characterBegin + tableName + characterEnd;
		int fkIndex = 1;
		// Añadimos la alteración de la tabla para crear las claves foraneas.
		for (ForeignKey foreignKey : foreignKeys.values()) {
			if (fkIndex > 1) {
				alterStatement += ",";
			}
			alterStatement += " ADD " + foreignKey.getForeignKeyDeclaration("FK_" + tableIdto + "_" + fkIndex, gestor);
			fkIndex++;
		}

		if (!isAssociationTable) {
			for (TableColumn column : columns.values()) {
				// No se puede modificar la columna de clave primaria en MySQL
				if (primaryKeyColumns.contains(column.getColumnName())) {
					continue;
				}
				// Si la columna está declarada como nique, significa que sus
				// valores tienen que ser unicos y por ello debemos añadir dicha
				// restricción a la tabla.
				if (column.isUnique()) {
					if (fkIndex > 1) {
						alterStatement += ",";
					}
					alterStatement += " ADD CONSTRAINT " + characterBegin + "U_" + tableIdto + "_"
							+ column.getColumnName() + characterEnd + " UNIQUE (" + characterBegin
							+ column.getColumnName() + characterEnd + ")";
					fkIndex++;
				}
			}
		} else if (isAssociationTable && columns.size() > 1) {
			if (fkIndex > 1) {
				alterStatement += ",";
			}
			// En una tabla asociacion se añade un índice UNIQUE para las
			// columnas distintas de tableId, pues solo puede aparecer un
			// vinculo para una propiedad entre dos objetos.
			alterStatement += " ADD CONSTRAINT " + characterBegin + "U_" + tableIdto + "_group" + characterEnd + " UNIQUE (";
			boolean first = true;
			for (TableColumn column : columns.values()) {
				if (primaryKeyColumns.contains(column.getColumnName())) {
					continue;
				}
				if (!first) {
					alterStatement += ",";
				}
				alterStatement += characterBegin + column.getColumnName() + characterEnd;
				first = false;
			}
			alterStatement += ")";
		}
		alterStatement += ";";
		// Si no se ha puesto ninguna restricción, descartamos la cadena que
		// habiamos empezado a construir y devolvemos null
		if (fkIndex == 1) {
			return null;
		}
		return alterStatement;
	}

	/**
	 * Construye las sentencias de creación de los índices aplicables a esta
	 * tabla.
	 * 
	 * @param gestor
	 *            Gestor de base de datos con el que se está trabajando.
	 * @return Lista de cadenas donde cada una de ellas es la sentencia de
	 *         creación de un índice distinto.
	 */
	public List<String> getSQLIndexesCreationStatements(String gestor) {
		List<String> result = new ArrayList<String>();
		GenerateSQL generateSQL = new GenerateSQL(gestor);
		for (Set<TableColumn> columnSet : indexList) {
			String indexSQL = generateSQL.getCreateIndexStatement(tableName,
					new ArrayList<TableColumn>(columnSet));
			result.add(indexSQL);
		}
		return result;
	}

	/**
	 * Modifica el nombre de la tabla.
	 * 
	 * @param name
	 *            Nombre que se desea que tenga la tabla.<br>
	 *            Hay que tener cuidado pues en MySQL no puede superar los 64
	 *            carcteres.
	 */
	public void setName(String name){
		this.tableName = name;
	}
	
	public String getName() {
		return tableName;
	}

	public Integer getId() {
		return tableIdto;
	}

	/**
	 * Establece las columnas que formarn parte de la clave primaria de esta
	 * tabla. El conjunto debe tener almenos un elemento o no se llevara a cabo
	 * la operación.
	 * 
	 * @param columnNames
	 *            Conjunto de los nombres de las columnas que se quieren
	 *            establecer como parte de la clave primaria.
	 * @throws NoSuchColumnException
	 *             Si alguno de los elementos del conjunto no se corresponde con
	 *             el nombre de una de las columnas de esta tabla.
	 */
	public void setPrimaryKeyColumns(Set<String> columnNames)
			throws NoSuchColumnException {
		if (columnNames == null || columnNames.size() < 1) {
			return;
		}
		for (String columnName : columnNames) {
			if (columns.get(columnName) == null) {
				throw new NoSuchColumnException("No se puede establecer como parte de la clave primaria de la tabla " + tableName + " la columna " + columnName + " pues no existe en esta tabla.");
			}
		}
		primaryKeyColumns = columnNames;
	}

	/**
	 * Consulta si la tabla actual referencia a la tabla indicada.
	 * 
	 * @param tableIdto
	 *            Identificador de la tabla de la que queremos saber si es
	 *            referencida por la tabla actual.
	 * @return <code>true</code> si la tabla actual referencia a la tabla
	 *         indicada.
	 */
	public boolean referencesTable(int tableIdto) {
		return referencedTables.contains(tableIdto);
	}

	public String toString() {
		String result = "[" + tableName + "={";
		for (TableColumn tableColumn : columns.values()) {
			result += ", '" + tableColumn.getColumnName() + "'";
		}
		result += "}]";
		return result;
	}

	public boolean isAssociation() {
		return isAssociationTable;
	}

	public Integer getColumnProperty(String columnName) {
		return columnPropertyMap.get(columnName);
	}

	public TableColumn getDataPropertyColumn(int idProperty) {
		if (! dataProperties.contains(idProperty)){
			return null;
		}
		String columnName = propertyColumnMap.get(idProperty).iterator().next();
		return columns.get(columnName);
	}

	public Set<Integer> getExternalizedPropertyLocations(int idProperty) {
		return propertyLocation.get(idProperty);
	}

	public TableColumn[] getObjectPropertyColumn(int idProperty, int idRange) {
		if (! objectProperties.contains(idProperty)){
			return null;
		}
		
		for (String columnName : propertyColumnMap.get(idProperty)) {
			Integer columnDomain = columnDomainMap.get(columnName);
			if (columnDomain == null){
				System.out.println("Table.getObjectPropertyColumn() + [Tabla: " + tableName + "; IDTO=" + tableIdto + "; columName=" + columnName + "; idProperty=" + idProperty + "; idRange= " + idRange +"]" );
			}
			if (columnDomain.equals(idRange)){
				return new TableColumn[]{columns.get(columnName), null};
			}
		}
		// No hemos encontrado ninguna columna que contenga informacion de la
		// propiedad indicada cuyo rango sea el dado.
		return null;
	}

	public boolean isExternalizedProperty(int idProperty) {
		return externalizedProperties.contains(idProperty);
	}

	public boolean isForeignProperty(int idProperty) {
		return foreignProperties.contains(idProperty);
	}

	public boolean isObjectProperty(int idProperty) {
		return objectProperties.contains(idProperty);
	}

	/**
	 * añade una ObjectProperty a la tabla actual de la cual no se tena
	 * conocimiento con anterioridad y se marca como propiedad foranea, pues
	 * viene de una clase que no es la que representa esta tabla.
	 * 
	 * @param property
	 *            Objecto con la informacion de la propiedad que se quiere
	 *            añadir.
	 * @param ranges
	 *            Mapa de las clases conocidas ordenadas por idto.
	 */
	public void addObjectProperty(PropertyInfo property, Set<Integer> ranges){
		int idProperty = property.getIdProp();
		objectProperties.add(idProperty);
		Set<Integer> existingRanges = propertyRanges.get(property.getIdProp());
		if (existingRanges == null){
			this.propertyRanges.put(idProperty, ranges);
		}else{
			existingRanges.addAll(ranges);
		}
		foreignProperties.add(idProperty);
	}

	/**
	 * Dada una propiedad, busca en profundidad todos los tipos que puede tener
	 * dicha propiedad y no solo los más superficiales con los que fue declarado
	 * la propiedad.
	 * 
	 * @param property
	 *            Propiedad de la que se desea conocer el conjunto de rangos.
	 * @param classes
	 *            Mapa de todas las clases conocidas organizadas por su idto
	 * @return Conjunto de todos los identificadores de clases a los que puede
	 *         apuntar la propiedad
	 */
	private Set<Integer> getPropertyRanges(PropertyInfo property, Map<Integer, ClassInfo> classes) {
		Set<Integer> result = new HashSet<Integer>();
		for (Integer rangeIdto : property.getPropertyTypes()) {
			ClassInfo rangeClass = classes.get(rangeIdto);
			result.add(rangeIdto);
			result.addAll(rangeClass.getChildClasses());
		}
		return result;
	}

	/**
	 * añade la columna para incluir el identificador de la propiedad en una
	 * tabla asociacion.<br>
	 * Si no se trata de una tabla aosciación o la columna ya fue añadida con
	 * anterioridad, no se hara nada.
	 */
	public void addPropertyColumn(){
		if (!isAssociationTable || getDataPropertyColumn(ID_PROPERTY) != null){
			return;
		}
		dataProperties.add(ID_PROPERTY);
		try {
			addDataPropertyColumn(COLUMN_NAME_PROPERTY, Constants.IDTO_INT, ID_PROPERTY);
		} catch (ColumnOverlapException e) {
			e.printStackTrace();
			System.err.println("[TABLE STRUCT ERROR] Ya existe una columna " + COLUMN_NAME_PROPERTY + " en la tabla " + tableName + "pero no está relacionada con la propiedad ficticia correcta");
		}
	}

	/**
	 * Indica si la tabla actual tiene propiedades que hayan sido externalizadas
	 * desde la tabla de otra clase.
	 * 
	 * @return <code>true</code> si tiene propiedades foraneas.
	 */
	public boolean hasForeignProperties() {
		return foreignProperties.size() > 0;
	}
	
	/**
	 * Elimina de los conjuntos de propiedades las del conjunto dado.
	 * 
	 * @param propertiesToRemove
	 *            Identificadores de propiedades a eliminar.
	 */
	public void removeProperties(Set<Integer> propertiesToRemove){
		dataProperties.removeAll(propertiesToRemove);
		objectProperties.removeAll(propertiesToRemove);
	}

	/**
	 * añade la referencia de que la columna del rango puede contener
	 * informacion de otra propiedad.<br>
	 * Este metodo solo funcionar en tablas asociacion.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que puede contener
	 *            informacion la columna del rango.
	 */
	public void addPropertyToRange(int idProperty) {
		if (! isAssociationTable){
			return;
		}
		TableColumn rangeColumn = null;
		for (TableColumn column : columns.values()) {
			if (column.getIdProperty() > 0){
				rangeColumn = column;
				break;
			}
		}
		Set<String> columnNames = new HashSet<String>();
		columnNames.add(rangeColumn.getColumnName());
		propertyColumnMap.put(idProperty, columnNames);
	}

	/**
	 * Cambia el nombre de la columna indicada por el nombre dado
	 * 
	 * @param existingColumn
	 *            Columna a la que se le desea cambiar el nombre
	 * @param newName
	 *            Nuevo nombre que se le debe dar a la columna.
	 */
	public void alterColumnName(TableColumn existingColumn, String newName) {
		String oldName = existingColumn.getColumnName();
		existingColumn.setName(newName);
		// Borramos todas las antiguas referencias.
		columns.remove(oldName);
		Integer idProperty = columnPropertyMap.remove(oldName);
		Set<String> columnNames = propertyColumnMap.get(idProperty);
		columnNames.remove(oldName);
		ForeignKey foreignKey = foreignKeys.remove(oldName);
		Integer columnDomain = columnDomainMap.remove(oldName);
		// Añadimos todas las nuevas referencias.
		columns.put(newName, existingColumn);
		columnPropertyMap.put(newName, idProperty);
		columnNames.add(newName);
		foreignKeys.put(newName, foreignKey);
		foreignKey.setOwnerColumnName(newName);
		if (columnDomain != null){
			columnDomainMap.put(newName, columnDomain);
		}
	}

	/**
	 * Consulta las propiedades foraneas.
	 * 
	 * @return Devuelve el conjunto de propiedades que han sido externalizadas a
	 *         esta tabla.
	 */
	public Set<Integer> getForeignProperties() {
		return foreignProperties;
	}

	/**
	 * Consulta las propiedades externalizadas.
	 * 
	 * @return Conjunto de los identificadores de las propiedades que han sido
	 *         externalizadas.
	 */
	public Set<Integer> getExternalizedProperties(){
		return externalizedProperties;
	}

	/**
	 * Idtos de las tablas que forman esta tabla.
	 * Si se trata de una tabla asociacion aqui tendremos mas de un valor, los dominios, si no solo uno igual al tableIdto.
	 * @return
	 */
	public HashSet<Integer> getOriginalTableIdtos() {
		return originalTableIdtos;
	}
}
