package dynagent.server.dbmap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dynagent.common.Constants;
import dynagent.server.gestorsDB.GenerateSQL;

public class View implements IQueryInfo, Serializable {

	private static final long serialVersionUID = 558996876855871144L;
	/** Clase abstracta de la que se quiere crear la vista */
	private ClassInfo viewClass;
	/**
	 * número que se le ha asignado a la vista como identificador. Si es una
	 * vista que representa a una clase, sera el mismo que el de la clase.
	 */
	private int viewId;
	/** Clases hijas que se van a incluir en la vista indexadas por su idto. */
	private Map<Integer,ClassInfo> childClasses;
	/** Tablas hijas que se van a incluir en la vista indexadas por su idto. */
	private Map<Integer,Table> childTables;
	/** Identificadores de las propiedades que son dataProperty */
	private Set<Integer> dataProperties;
	/** Identificadores de las propiedades que son objectProperty */
	private Set<Integer> objectProperties;
	/** Conjunto de propiedades comunes a todas las clases incluidas en la vista. */
	private Set<Integer> externalizedProperties;
	/**
	 * Mapa de las columnas de la vista organizadas por la propiedad a la que
	 * hacen referencia. En este mapa no se incluiran las columnas que hacen
	 * referencia al IDTO de una ObjectProperty.
	 */
	private Map<Integer, List<ViewColumn>> viewColumns;
	/**
	 * Mapa de las consultas que hay que hacer para que los datos de una tabla
	 * salgan en la vista.
	 */
	private Set<String> queryData;
	/** Cadena con el nombre de la vista */
	private String viewName;
	/** Indica si la vista es de tipo asociacion. */
	private boolean associationView;
	/** Mapa de los rangos de una columna dado el nombre de dicha columna. */
	private Map<String, Set<Integer>> columnRanges;
	/** Mapa de todas las columnas por su nombre */
	private Map<String, ViewColumn> columnsByName;
	
	private Map<Integer, Set<Integer>> externalizedPropertiesLocations;
	/**
	 * Lista que contiene los nombres de las columnas de la vista, ordenados
	 * igual que cuando se definio la creación de la vista.
	 */
	private List<String> orderedColumnNames;
	/**
	 * Mapa para saber a que clase apuntan las columnas que contienen
	 * ObjectProperties
	 */
	private Map<String, Integer> columnDomains;
	/** Conjunto de las propiedades que se han incluido en esta vista desde otras vistas al externalizarlas */
	private Set<Integer> foreignProperties;
	
	private static int viewIndex = 1;
	
	private Map<Integer, PropertyInfo> properties;

	/**
	 * Constructor para vistas que representan clases. En la creación de una
	 * vista que representa una tabla se incluyen aautomaticamente las columnas
	 * que representan al tableId e idto del objeto representado, asi como la
	 * columna destination.
	 * 
	 * @param abstractClass
	 *            Clase a la que representa la vista.
	 * @param properties
	 *            Propiedades comunes a todas las clases hijas.
	 * @param externalizedProperties
	 *            Propiedades de la lista anterior que no van a estar incluidas
	 *            en la propia vista.
	 */
	public View(ClassInfo abstractClass, List<PropertyInfo> properties, Set<Integer> externalizedProperties) {
		this.viewClass = abstractClass;
		String className = abstractClass.getName();
		this.viewName = "v_"  + className.toLowerCase().replaceAll("\\.", "");
		this.viewId = abstractClass.getIdto();
		initializeStructures(false, externalizedProperties);
		initializeProperties(properties);
		
		// Añadimos las columnas por defecto que va a tener toda vista que no
		// sea de asociacion, es decir, aquelllas que tiene cualquier tabla que
		// no es de asociacion, más la referente al identificador del tipo de
		// objeto de cada fila.
		addViewColumn(Table.COLUMN_NAME_TABLEID, Constants.IDTO_INT, ID_TABLE_ID);
		addViewColumn(Table.COLUMN_NAME_DESTINATION, Constants.IDTO_STRING, ID_DESTINATION);
		addViewColumn(COLUMN_NAME_IDTO, Constants.IDTO_INT, ID_IDTO);
	}

	/**
	 * Constructor para vistas de tipo asociacion. En la creación de una vista
	 * asociacion se crean por defecto las columnas para el dominio y el rango
	 * con sus respectivas columnas de idto (si la clase es abstracta)
	 * 
	 * @param domainClass
	 *            Clase de la que surge la relación entre las dos clases.
	 * @param rangeClass
	 *            Clase apuntada por la propiedad.
	 * @param idProperty
	 *            Propiedad por la que se relacionan las dos clases.
	 * @param viewId
	 *            Identificador que se le ha de asignar a esta vista asociacion.
	 */
	public View(ClassInfo domainClass, ClassInfo rangeClass, Integer idProperty, int viewId){
		this.viewClass = null;
		this.viewId = viewId;
		String domainName = domainClass.getName().toLowerCase().replaceAll("\\.", "");
		String rangeName = rangeClass.getName().toLowerCase().replaceAll("\\.", "");
		String viewName = "v_" + domainName + "#" + rangeName;
		if (viewName.length() > 57){
			viewName = viewName.substring(0, 55) + "~" + viewIndex;
		}
		this.viewName = viewName;
		initializeStructures(true, new HashSet<Integer>());
		objectProperties.add(idProperty);
		objectProperties.add(ID_DOMAIN);
		
		Set<Integer> domainSet = new HashSet<Integer>();
		domainSet.add(domainClass.getIdto());
		domainSet.addAll(domainClass.getChildClasses());
		
		Set<Integer> rangeSet = new HashSet<Integer>();
		rangeSet.add(rangeClass.getIdto());
		rangeSet.addAll(rangeClass.getChildClasses());
		
		addViewColumn(domainName + SUFFIX_ID, Constants.IDTO_INT, ID_DOMAIN, domainSet, true, domainClass.getIdto());
		String rangeColumnName = rangeName;
		if (domainClass.getIdto() == rangeClass.getIdto()){
			rangeColumnName += "Range";
		}
		rangeColumnName += SUFFIX_ID;
		addViewColumn(rangeColumnName, Constants.IDTO_INT, idProperty, rangeSet, true, rangeClass.getIdto());
		
	}
	
	public View(ClassInfo domainClass, PropertyInfo property, int viewId){
		this.viewClass = null;
		this.viewId = viewId;
		String domainName = domainClass.getName().toLowerCase().replaceAll("\\.", "");
		String propertyName = property.getName();
		String viewName = "v_" + domainName + "#" + propertyName;
		if (viewName.length() > 57){
			viewName = viewName.substring(0, 55) + "~" + viewIndex;
		}
		this.viewName = viewName;
		this.associationView = true;
		initializeStructures(true, new HashSet<Integer>());
		dataProperties.add(property.getIdProp());
		objectProperties.add(ID_DOMAIN);
		
		Set<Integer> domainSet = new HashSet<Integer>();
		domainSet.add(domainClass.getIdto());
		domainSet.addAll(domainClass.getChildClasses());
		
		addViewColumn(domainName + SUFFIX_ID, Constants.IDTO_INT, ID_DOMAIN, domainSet, true, domainClass.getIdto());
		properties.put(property.getIdProp(), property);
		Integer propertyType = property.getPropertyTypes().iterator().next();
		addViewColumn(propertyName, propertyType, property.getIdProp());
	}
	
	/**
	 * Inicializa todas las estructuras y atributos comunes a los dos
	 * constructores que usa esta clase.
	 * 
	 * @param associationView
	 *            Indica si vista representa a una vista asociacion.
	 * @param externalizedProperties
	 *            Lista de las propiedades que están externalizadas.
	 */
	private void initializeStructures(boolean associationView, Set<Integer> externalizedProperties) {
		this.properties = new Hashtable<Integer, PropertyInfo>();
		this.dataProperties = new HashSet<Integer>();
		this.objectProperties = new HashSet<Integer>();
		this.childClasses = new Hashtable<Integer, ClassInfo>();
		this.childTables = new Hashtable<Integer, Table>();
		this.associationView = associationView;
		this.viewColumns = new Hashtable<Integer, List<ViewColumn>>();
		this.externalizedProperties = new TreeSet<Integer>(externalizedProperties);
		this.queryData = new HashSet<String>();
		this.columnRanges = new Hashtable<String, Set<Integer>>();
		this.columnsByName = new Hashtable<String, ViewColumn>();
		this.externalizedPropertiesLocations = new Hashtable<Integer, Set<Integer>>();
		this.columnDomains = new Hashtable<String, Integer>();
		this.foreignProperties = new HashSet<Integer>();
	}
	
	/**
	 * Clasifica las propiedades segun si son ObjectProperties o DataProperties
	 * para que posterioremente sea más facil decidir si hay que hacer un
	 * procesamiento más exhaustivo de las mismas o no respectivamente.
	 * 
	 * @param properties
	 *            Lista de las propiedades que son comunes entre todas las
	 *            clases hijas que tienen una representación en tablas
	 */
	private void initializeProperties(List<PropertyInfo> properties) {
		dataProperties.add(ID_TABLE_ID);
		dataProperties.add(ID_IDTO);
		dataProperties.add(ID_DESTINATION);
		for (PropertyInfo property : properties) {
			this.properties.put(property.getIdProp(), property);
			Set<Integer> propertyTypes = property.getPropertyTypes();
			if (isBasicType(propertyTypes)) {
				dataProperties.add(property.getIdProp());
			} else {
				objectProperties.add(property.getIdProp());
			}
		}
	}

	/**
	 * Dado un conjuto de tipos de propiedad, nos dice si se trata de una
	 * propiedad de tipo basico. <br>
	 * Para que una propiedad se considere de tipo basico.
	 * 
	 * @param propertyTypes
	 *            Conjunto de enteros que representa los tipos de la propiedad
	 * @return <code>true</code> si se trata de una propiedad de tipo basico.
	 */
	private boolean isBasicType(Set<Integer> propertyTypes) {
		boolean result = true;
		for (Integer propertyType : propertyTypes) {
			result = result && Constants.isDataType(propertyType);
		}
		return result;
	}

	/**
	 * Establece las clases hijas de la clase que va a representar la vista
	 * actual.
	 * 
	 * @param childClasses
	 *            Solo debe contener las clases hijas no abstractas.
	 * @param childTables
	 *            Tablas que representan a las clases dadas como hijas.
	 */
	public void setChildren(Map<Integer, ClassInfo> childClasses, Map<Integer, Table> childTables){
		this.childClasses = childClasses;
		this.childTables = childTables;
	}

	/**
	 * Consulta si una propiedad está externalizada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad que se quiere consultar.
	 * @return <code>true</code> si la propiedad está externalizada.
	 */
	public boolean isExternalizedProperty(int idProperty){
		return externalizedProperties.contains(idProperty);
	}

	/**
	 * Devuelve la clase hija pedida.
	 * 
	 * @param idto
	 *            Identificador de la clase hija que se quiere obtener.
	 * @return Puede devolver <code>null</code> si la clase pedida no es hija de
	 *         la clase abstracta que representa esta vista.
	 */
	public ClassInfo getChildClass(int idto){
		return childClasses.get(idto);
	}

	/**
	 * Devuelve la tabla hija de la que contiene datos esta vista.
	 * 
	 * @param idto
	 *            Identificador de la tabla que se quiere consultar.
	 * @return Puede devolver <code>null</code> si la tabla indicada no es una
	 *         de las que contiene datos esta vista.
	 */
	public Table getChildTable(int idto){
		return childTables.get(idto);
	}

	/**
	 * Devuelve una copia de la lista de identifadores de las clases hijas
	 * representadas por esta vista.
	 * 
	 * @return Copia de la lista de indentificadores de las clases hijas.
	 *         <b>Nunca</b> sera <code>null</code>.
	 */
	public List<Integer> getChildrenIdtos(){
		List<Integer> childrenIdtos = new LinkedList<Integer>(childClasses.keySet());
		return childrenIdtos;
	}

	/**
	 * Establece la sentencia que hemos de usar si queremos obtener los datos de
	 * la tabla identificada por el idto dado para que se muestren en la vista.<br>
	 * Si el idto indicado no se corresponde con una clase hija existente, se
	 * ignora la inserción
	 * 
	 * @param idto
	 *            Identificador de la tabla de la que se está estableciendo como
	 *            se ajustan los datos de la tabla con los de la vista.
	 * @param sql
	 *            Sentencia que consulta los datos de la tabla para adaptarlos a
	 *            la vista.
	 */
	public void setTableQuery(int idto, String sql){
		if (! associationView && childClasses.get(idto) == null){
			return;
		}
		queryData.add(sql);//Al ser queryData un HashSet no se van a crear uniones repetidas
	}

	/**
	 * Construye la sentencia que se ha de usar para construir esta vista.
	 * 
	 * @param gestor
	 *            Cadena con el gestor de base de datos con el que se está
	 *            trabajando.
	 * @return Cadena con la creación de la vista.
	 */
	public String getCreationViewStatement(String gestor){
		GenerateSQL generateSQL = new GenerateSQL(gestor);
		boolean first = true;
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String result = "CREATE VIEW " + characterBegin + viewName + characterEnd + " (";
		// Ponemos la lista de los nombres de las columnas que vamos a utilizar.
		List<String> orderedColumnNames = getOrderedColumnNames();
		for (String columnName : orderedColumnNames) {
			if (! first){
				result += ", ";
			}
			result += characterBegin + columnName + characterEnd;
			first = false;
		}
		result += ") AS ";
		// Añadimos la captura de datos de las distintas tablas.
		first = true;
		for (String sql : queryData) {
			if (! first){
				result += " UNION ALL ";
			}
			result += sql;
			first = false;
		}
		result += ";";
		return result;
	}

	/**
	 * añade una columna que hace referencia a una DataProperty. Si ya existe
	 * una columna representando a la propiedad indicada, se borra la que habia
	 * anteriormente y se sustituye por la nueva columna que se va a crear.<br>
	 * Si la propiedad indicada no es una DataPropierty, no se hace nada. A
	 * efectos practicos los identificadores ficticios de IDTO, tableId y
	 * Destination se consideran identifcadorees de DataProperties.
	 * 
	 * @param columnName
	 *            Nombre de la columna que se va a añadir.
	 * @param dataType
	 *            Tipo de datos que va a contener la columna.
	 * @param idProperty
	 *            Identificador de la propiedad a la que hacen referencia los
	 *            datos.
	 * @return Devuelve la columna que hubiera anteriormente asignada a la
	 *         propiedad o <code>null</code> si la propiedad no tenia ninguna
	 *         columna asignada anteriormente.
	 */
	public ViewColumn addViewColumn(String columnName, int dataType, int idProperty){
		if (! dataProperties.contains(idProperty)){
			// No se tiene constancia de que la propiedad indicada se una DataProperty incluida en esta vista.
			return null;
		}
		ViewColumn previousColumn = null;
		if (viewColumns.containsKey(idProperty)){
			List<ViewColumn> previousColumns = viewColumns.remove(idProperty);
			if (previousColumn != null && ! previousColumns.isEmpty()){
				previousColumn = previousColumns.get(0);
			}
		}
		this.addViewColumn(columnName, dataType, idProperty, null, true, null);
		return previousColumn;
	}

	/**
	 * añade una columna a la vista. Si la propiedad a la que se hace referencia
	 * es una ObjectProperty, se crea, además, la columna para contener los Idto
	 * a los que hacen referencia los datos.
	 * 
	 * @param columnName
	 *            Nombre que se le va a dar a la columna de la vista.
	 * @param dataType
	 *            Tipo de datos que va a contener la columna de la vista. Los
	 *            tipos de datos soportados son los de {@link Constants}.
	 * @param idProperty
	 *            Identificador de la propiedad de la que contiene datos esta
	 *            columna.
	 * @param range
	 *            Si se trata de una ObjectProperty, se ha de especificar el
	 *            rango completo del que puede contener datos esta columna.
	 * @param addIdtoColumn
	 *            Indica si se ha de añadir la columna para el idto si se trata
	 *            de una objectProperty.
	 * @param domainIdto
	 *            Clase de la que se almacena la referencia. El idto almacenado
	 *            sera el que apunta la propiedad en su rango y para la que se
	 *            está creando la propiedad.
	 */
	public void addViewColumn(String columnName, int dataType, int idProperty, Set<Integer> range, boolean addIdtoColumn, Integer domainIdto){
		ViewColumn existingColumn = columnsByName.get(columnName);
		if (existingColumn != null){
			System.err.println("[VIEW STRUCT ERROR] Se ha intentando asociar una columna con más de una propiedad en una vista que no es asociacion: {VISTA=" + viewName + "; COLUMNA=" + columnName + "; PROPIEDAD=" + idProperty);
			Exception e = new Exception();
			e.printStackTrace();
		}else{
			List<ViewColumn> columnsForProperty = viewColumns.get(idProperty);
			if (columnsForProperty == null){
				columnsForProperty = new LinkedList<ViewColumn>();
				viewColumns.put(idProperty, columnsForProperty);
			}
			ViewColumn viewColumn = new ViewColumn(columnName, dataType, idProperty);
			columnsForProperty.add(viewColumn);
			columnsByName.put(columnName, viewColumn);
			if (domainIdto != null) {
				columnDomains.put(columnName, domainIdto);
			}
			if (objectProperties.contains(idProperty) && range != null){
				columnRanges.put(columnName, range);
				if (addIdtoColumn) {
					String columnIdtoName = columnName;
					if (columnName.endsWith(SUFFIX_ID)) {
						columnIdtoName = columnIdtoName.substring(0, columnIdtoName.length() - SUFFIX_ID.length());
					}
					ViewColumn viewColumnIdto = new ViewColumn(columnIdtoName + SUFFIX_IDTO, Constants.IDTO_INT, idProperty);
					columnsByName.put(viewColumnIdto.getColumnName(), viewColumnIdto);
					if (domainIdto != null) {
						columnDomains.put(columnIdtoName + SUFFIX_IDTO, domainIdto);
					}
				}
			}
		}
	}

	/**
	 * Le indica a una vista asociacion que su columna del rango puede hacer
	 * referencia a más de una propiedad y hace todo el procesamiento necesario
	 * para que eso sea posible.<br>
	 * Si no se trata de una vista asociacion, no se hace nada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad que se quiere añadir como
	 *            propiedad del rango.
	 * @param range
	 *            Conjunto de idtos del rango de la propiedad a añadir
	 */
	public void addPropertyToRange(int idProperty, Set<Integer> range){
		if (! associationView){
			// Si no se trata de una vista asociacion, esto no se puede hacer, con lo cual salimos.
			System.out.println("[VIEW INFO] No se puede añadir una propiedad a la columna del rango si no estamos en una vista asociacion. VISTA=" + viewName);
			return;
		}else if(range.isEmpty()){
			System.err.println("[VIEW STRUCT ERROR] Hay que indicar un rango para añadir una propiedad al rango de una vista asociacion. {VISTA=" + viewName + "; PROPERTY=" + idProperty);
		}
		Iterator<Integer> objectPropertiesIterator = objectProperties.iterator();
		Integer property = null;
		while (objectPropertiesIterator.hasNext()){
			Integer currentProperty = objectPropertiesIterator.next();
			if (currentProperty.intValue() > 0){
				// En una tabla asociacion las únicas objectProperties mayores
				// que cero son aquellas de las que guardan datos las columnas
				// del rango.
				property = currentProperty;
				break;
			}
		}
		if (property == null){
			System.err.println("[VIEW STRUCT ERROR] No hay propiedades reales de las que guarden datos las columnas del rango de una vista asociacion. VISTA=" + viewName);
		}else{
			Integer rangeIdto = range.iterator().next();
			ViewColumn[] columns = getObjectPropertyColumn(property, rangeIdto);
			if (columns == null){
				System.err.println("[VIEW STRUCT ERROR] Las columnas del rango de la vista asociacion " + viewName + " no pueden apuntar a la clase " + rangeIdto);
			}else{
				List<ViewColumn> auxList = new LinkedList<ViewColumn>();
				auxList.add(columns[0]);
				viewColumns.put(idProperty, auxList);
				columns[0].addIdProperty(idProperty);
				if (columns[1] != null){
					columns[1].addIdProperty(idProperty);
				}
				objectProperties.add(idProperty);
			}
		}
		
	}

	/**
	 * añade la columna de property a la vista asociacion. Si la vista actual no
	 * es una vista asociacion, este metodo no hara nada.<br>
	 * Si ya se habia incluido con anterioridad la columna property a la vista,
	 * este metodo <b>no</b> intentara crearla de nuevo.
	 */
	public void addPropertyColumn(){
		if (! associationView || dataProperties.contains(ID_PROPERTY)){
			return;
		}
		dataProperties.add(ID_PROPERTY);
		addViewColumn(COLUMN_NAME_PROPERTY, Constants.IDTO_INT, ID_PROPERTY);
	}
	
	/**
	 * Consulta si una propiedad dada es objectProperty
	 * @param idProperty Identificador numero de la propiedad a consultar.
	 * @return Devuelve <code>true</code> si se trata de una ObjectProperty.
	 */
	public boolean isObjectProperty(int idProperty){
		return objectProperties.contains(idProperty);
	}

	/**
	 * Devuelve el par de columnas que representan una ObjectProperty. La
	 * primera del par es la que contiene los datos del <code>tableId</code> y
	 * la segunda la que contiene los datos del <code>idto</code>. <br>
	 * A efectos practicos, en las vistas asociacion, la propiedad fictica
	 * ID_DOMAIN se consiera objectProperty.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad que se quiere consultar.
	 * @param range
	 *            Idto del rango del que tienen que contener informacion las
	 *            columnas.
	 * @return devolvera <code>null</code> si la propiedad no es ObjectProperty
	 *         o si no se tiene ninguna columna que apunte al rango indicado.
	 */
	public ViewColumn[] getObjectPropertyColumn(int idProperty, int range){
		if (! objectProperties.contains(idProperty)){
			// No se tiene constancia de que la propiedad indicada sea ObjectProperty
			return null;
		}
		List<ViewColumn> columns = viewColumns.get(idProperty);
		String columnName = null;
		for (ViewColumn viewColumn : columns) {
			String currentColumnName = viewColumn.getColumnName();
			Set<Integer> columnRange = columnRanges.get(currentColumnName);
			if (columnRange.contains(range)){
				columnName = currentColumnName;
				break;
			}
		}
		if (columnName == null){
			// No se ha encontrado ninguna columna cuyo rango sea el indicado.
			return null;
		}
		ViewColumn tableIdColumn = columnsByName.get(columnName);
		String idtoColumnName = columnName.endsWith(SUFFIX_ID) ? columnName.substring(0, columnName.length() - 2) : columnName;
		idtoColumnName += SUFFIX_IDTO;
		ViewColumn idtoColumn = columnsByName.get(idtoColumnName);
		ViewColumn[] result = new ViewColumn[]{tableIdColumn, idtoColumn};
		return result;
	}

	/**
	 * Devuelve la columna que contiene informacion de la DataProperty indicada.
	 * A efectos practicos, los identificadores de propiedad ficticios asignados
	 * a TableId, Idto y Destination, se consideraran como identificadores de
	 * DataProperties.<br>
	 * En las vistas asociacion, si existe la columna para la propiedad,
	 * ID_PROPERTY se considerara DataProperty.
	 * 
	 * @param idProperty
	 *            Identificador numero de la propiedad.
	 * @return Devuelve el objeto que representa a la columna que contiene los
	 *         datos pedidos. Puede devolver <code>null</code> si no se tiene
	 *         ninguna columna asociada con la propiedad indicada o si la
	 *         propiedad indicada no es una DataProperty.
	 */
	public ViewColumn getDataPropertyColumn(int idProperty){
		if (! dataProperties.contains(idProperty)){
			return null;
		}
		List<ViewColumn> columnsForProperty = viewColumns.get(idProperty);
		if (columnsForProperty == null || columnsForProperty.isEmpty()){
			// No se tiene informacion sobre la propiedad indicada en esta vista.
			return null;
		}
		return columnsForProperty.get(0);
	}

	/**
	 * Obtiene la lista con los nombres de las columnas de la vista con el mismo
	 * orden con el que se declararon en la creación de la vista.<br>
	 * Si todavia no se han tenido que ordenar nunca los nombres de las
	 * columnas, este metodo inicializa la lista donde se guardara dicho orden.
	 * Una vez inicializada la lista, no volvera a cambiar.
	 * 
	 * @return Lista ordenada de los nombres de las columnas.
	 */
	public List<String> getOrderedColumnNames(){
		if (orderedColumnNames == null){
			orderedColumnNames = new LinkedList<String>(columnsByName.keySet());
		}
		return orderedColumnNames;
	}

	/**
	 * Consulta una columna dado su nombre.
	 * 
	 * @param columnName
	 *            Nombre de la columna que se quiere consultar.
	 * @return devolvera <code>null</code> si no se tiene ninguna columna con el
	 *         nombre indicado.
	 */
	public ViewColumn getColumnByName(String columnName){
		return columnsByName.get(columnName);
	}

	/**
	 * Consulta la lista completa de propiedades de esta clase que están
	 * externalizadas.
	 * 
	 * @return Copia de la lista de propiedades externalizadas. Cualquier acción
	 *         sobre la lista no tendra ningun efecto sobre los datos
	 *         almacenados en esta clase sobre las propieades externalizadas.
	 */
	public List<Integer> getExternalizedProperties() {
		List<Integer> result = new LinkedList<Integer>(externalizedProperties);
		return result;
	}
	
	public ClassInfo getViewClass(){
		return viewClass;
	}

	public Integer getId(){
		return viewId;
	}

	/**
	 * añade una localización para una propiedad que está declarada como
	 * externalizada.<br>
	 * <b>Si la propiedad no está declarada como externalizada, no se hara
	 * nada.</b>
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad a la que se le quiere añadir una
	 *            localización.
	 * @param locationId
	 *            Identificador de la localización de los datos de la propiedad.
	 */
	public void addPropertyLocation(int idProperty, int locationId){
		if (! externalizedProperties.contains(idProperty)){
			return;
		}
		Set<Integer> locations = externalizedPropertiesLocations.get(idProperty);
		if (locations == null){
			locations = new HashSet<Integer>();
			externalizedPropertiesLocations.put(idProperty, locations);
		}
		locations.add(locationId);
	}

	public Set<Integer> getExternalizedPropertyLocations(int idProperty){
		return externalizedPropertiesLocations.get(idProperty);
	}

	/**
	 * Consulta todas las propiedades de las que se tiene informacion en esta
	 * vista, tanto las que están represeentadas en esta vista como las que
	 * están externalizadas.
	 * 
	 * @return Copia de la lista de todas las propiedades de las que se tiene
	 *         informacion.<br>
	 *         Cualquier cambio sobre esta lista no tendra efecto en la
	 *         informacion que guarda la vista en si.
	 */
	public List<Integer> getAllProperties(){
		List<Integer> properties = new LinkedList<Integer>();
		properties.addAll(dataProperties);
		properties.addAll(objectProperties);
		return properties;
	}

	public boolean isAssociation(){
		return associationView;
	}

	public String getName(){
		return viewName;
	}

	/**
	 * añade una objectProperty al conjunto de las contempladas por esta vista.
	 * Esto se hace cuando se quiere añadir una propiedad externalizada desde
	 * otra clase.
	 * 
	 * @param property
	 *            Objeto que contiene la informacion de la ObjectProperty
	 */
	public void addObjectProperty(PropertyInfo property){
		this.objectProperties.add(property.getIdProp());
		this.foreignProperties.add(property.getIdProp());
	}

	/**
	 * Devuelve el Idto de la clase a la que apunta la propiedad. La clase a la
	 * que apunta sera aquella más alta en la jerarquea de las clases que puede
	 * contener la propiedad.
	 * 
	 * @param columnName
	 *            Nombre de la columna sobre la que se quiere consultar.
	 * @return devolvera <code>null</code> si se ha preguntado por el dominio de
	 *         una columna que contiene una DataProperty
	 */
	public Integer getColumnDomain(String columnName){
		return columnDomains.get(columnName);
	}

	/**
	 * Consulta si una propiedad ha sido incluida desde otra vista en esta
	 * tabla.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que queremos consultar.
	 * @return devolvera <code>true</code> si la propiedad viene desde otra
	 *         vista. <br>
	 *         Esto implica que la clase a la que representa esta vista no tiene
	 *         por que contener informacion sobre esta propiedad. Normalmente la
	 *         informacion de la propiedad la tendremos que sacar de la clase
	 *         del dominio de la columna en la que se representa informaicon de
	 *         esta propiedad.
	 */
	public boolean isForeignProperty(int idProperty){
		return foreignProperties.contains(idProperty);
	}

	/**
	 * metodo que consulta todas las tablas que contienen informacion sobre la
	 * propiedad indicada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que se quieren saber los
	 *            nombres de las columnas que contienen informacion sobre la
	 *            misma.
	 * @return Lista de todas las columnas que contienen informacion de la
	 *         propiedad indicada.<br>
	 *         En el caso de las columnas que representan ObjectProperties se va
	 *         a devolver solo el nombre de la columna que contiene informacion
	 *         sobre el tableId, para que no reciban dos columnas con el mismo
	 *         dominio.<br>
	 *         Nunca devolvera <code>null</code>.
	 */
	public List<String> getColumnNamesContainingProperty(int idProperty){
		List<String> columnNames = new LinkedList<String>();
		if (dataProperties.contains(idProperty) || objectProperties.contains(idProperty)) {
			for (ViewColumn viewColumn : viewColumns.get(idProperty)) {
				columnNames.add(viewColumn.getColumnName());
			}
		}
		return columnNames;
	}
	
	@Deprecated
	public void addIdtoColumnToProperty(int idProperty){
		// TODO
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
		for (Integer idProperty : propertiesToRemove) {
			properties.remove(idProperty);
		}
	}

	/**
	 * Devuelve todas las propiedades de las que tiene conocimiento esta vista.
	 * 
	 * @return Conjuto con la informacion de las propiedades que se han
	 *         utilizado o se deben utilizar para formar esta vista.
	 */
	public List<PropertyInfo> getAllPropertiesInfo(){
		return new LinkedList<PropertyInfo>(properties.values());
	}

	/**
	 * Consulta la informacion de una propiedad especifica.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad sobre la que queremos consultar
	 *            informacion.
	 * @return Devuelve el objeto con la informacion de la propiedad.<br>
	 *         Si no se tiene informacion sobre la propiedad indicada, se
	 *         devolvera <code>null</code>.
	 */
	public PropertyInfo getPropertyInfo(int idProperty){
		return properties.get(idProperty);
	}
}
