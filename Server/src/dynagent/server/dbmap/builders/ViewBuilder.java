package dynagent.server.dbmap.builders;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.server.dbmap.AssocciationPair;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.dbmap.TypeErrorException;
import dynagent.server.dbmap.View;
import dynagent.server.dbmap.ViewColumn;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

/**
 * Extrae la informacion de base de datos necesaria para poder mapear las vistas y permite crearlas en base de datos.
 */
public class ViewBuilder {

	/** Mapa de las tablas indexadas por su id. */
	private Map<Integer, Table> tables;
	/** Mapa de las clases indexadas por su idto */
	private Map<Integer, ClassInfo> classes;
	/** Mapa de las vistas indexadas por su id */
	private Map<Integer, View> views;
	/** Mapa de las vistas que incluyen una clase. */
	private Map<Integer, List<View>> viewsCointainingClass;
	/** Mapa que contiene los datos de donde se han asociado dos clases que tienen vistas. */
	private Map<AssocciationPair, Integer> assocPairAssignedView;
	/**
	 * Conjunto de los identificadores de vistas que asocian una tabla y una
	 * vista, donde la tabla es la de la clase del dominio
	 */
	private Set<Integer> tableViewAssocs;
	/** Conjunto de las propiedades que son estructurales */
	private Set<Integer> structuralProperties;
	/** Conjunto de las propiedades estructurales que son compartidas. */
	private Set<Integer> sharedProperties;
	/** Mapa de las propiedades inversas. */
	private Map<Integer, Integer> inverseProperties;
	/**
	 * Numero que se tiene que usar como siguiente identificador de una vista
	 * que no esta asociada con una clase.
	 */
	private int nextId;
	/** Objeto que nos permite conectarnos a base de datos. */
	private FactoryConnectionDB fcdb;
	/** Objeto que ofrece compatibilidad SQL con distintos SGBD */
	private GenerateSQL gSQL;
	
	
	public ViewBuilder(FactoryConnectionDB fcdb, int nextId){
		this.fcdb = fcdb;
		this.nextId = nextId;
		gSQL = new GenerateSQL(fcdb.getGestorDB());
		viewsCointainingClass = new Hashtable<Integer, List<View>>();
		views = new Hashtable<Integer, View>();
		assocPairAssignedView = new Hashtable<AssocciationPair, Integer>();
		tableViewAssocs = new HashSet<Integer>();
	}
	
	/**
	 * Consulta las vistas creadas.
	 * 
	 * @return Mapa de las vistas creadas indexadas por su id.
	 */
	public Map<Integer, View> getViews() {
		return views;
	}

	/**
	 * Fija el mapa de las tablas que se van a usar para crear las vistas..
	 * 
	 * @param tables
	 *            Tablas que existen en la base de datos indexadas por su id.<br>
	 *            <b>No puede ser <code>null</code></b>
	 */
	public void setTables(Map<Integer, Table> tables) {
		this.tables = tables;
	}

	/**
	 * Fija las clases del modelo que se van a usar para trabjar.
	 * 
	 * @param classes
	 *            Mapa de las clases idenxadas por su idto.<br>
	 *            <b>No puede ser <code>null</code></b>
	 */
	public void setClasses(Map<Integer, ClassInfo> classes) {
		this.classes = classes;
	}

	/**
	 * Fija el conjunto de propiedades que son estructurales.
	 * 
	 * @param structuralProperties
	 *            Conjunto de los idProperty de las propiedades estructurales.<br>
	 *            <b>No puede ser <code>null</code></b>
	 */
	public void setStructuralProperties(Set<Integer> structuralProperties) {
		this.structuralProperties = structuralProperties;
	}

	/**
	 * Fija el conjunto de propiedades estructurales que son compartidas.
	 * 
	 * @param sharedProperties
	 *            Conjunto de los idProperty de las propiedades compartidas.<br>
	 *            <b>No puede ser <code>null</code></b>
	 */
	public void setSharedProperties(Set<Integer> sharedProperties) {
		this.sharedProperties = sharedProperties;
	}

	/**
	 * Fija el mapa de las propiedades inversas que existen en el modelo.
	 * 
	 * @param inverseProperties
	 *            Mapa de las propiedades inversas donde la clave es el
	 *            idProperty y el valor es el idProperty de la propiedad
	 *            inversa.<br>
	 *            <b>No puede ser <code>null</code></b>
	 */
	public void setInverseProperties(Map<Integer, Integer> inverseProperties){
		this.inverseProperties = inverseProperties;
	}
	
	/**
	 * Crea el mapa de las vistas que se pueden usar.
	 */
	public void createViewsMap(){
		// Creamos vistas vacias.
		Set<Integer> propertiesToRemove = new HashSet<Integer>();
		for (Integer integer : structuralProperties) {
			Integer inverseProperty = inverseProperties.get(integer);
			if (inverseProperty != null){
				propertiesToRemove.add(inverseProperty);
			}
		}
		
		for (ClassInfo classInfo : classes.values()) {
			if (classInfo.isAbstractClass() || hasNonAbstractChildren(classInfo)){
				View view = createVoidView(classInfo);
				if (view != null){
					view.removeProperties(propertiesToRemove);
				}
			}
		}
		
		// Una vez la vista tiene toda la informacion pertinente, se procede a crear las columnas
		List<View> views = new LinkedList<View>(this.views.values());
		for (View view : views) {
			createViewLinks(view);
			fillViewColumns(view);
		}
		
		for (ClassInfo classInfo : classes.values()) {
			if (! classInfo.isAbstractClass()){
				createTableToViewLinks(classInfo);
			}
		}
	}

	private boolean hasNonAbstractChildren(ClassInfo classInfo) {
		boolean result = false;
		for (Integer idto : classInfo.getChildClasses()) {
			if (! idto.equals(classInfo.getIdto())){
				ClassInfo childClass = classes.get(idto);
				if (! childClass.isAbstractClass()){
					result = true;
					break;
				}
			} 
		}
		result &= ! classInfo.isAbstractClass();
		return result;
	}

	/**
	 * Crea las vistas en base de datos.<br>
	 * <b>Esto borrara todas las vistas que pudieran existir en base de datos
	 * con los mismos nombres de las que se van a crear.</b>
	 * 
	 * @param excludedIdtos
	 *            Conjunto de los identificadores de vistas que no queremos que
	 *            se contruyan.
	 */
	public void constructViewsOnDB(Set<Integer> excludedIdtos){
		if (excludedIdtos == null){
			excludedIdtos = new HashSet<Integer>();
		}
		
		List<View> views = new LinkedList<View>(this.views.values());
		for (View view : views) {
			if(view.isAssociation()){
				// De hacer las consultas para cada vista asociacion se
				// encargara cada una de las vistas no asociacion, salvo de
				// aquellas en el que el dominio es una clase especifica.
				if (tableViewAssocs.contains(view.getId())){
					try {
						constructTableToViewAssoc(view);
					} catch (NoSuchColumnException e) {
						System.err.println("[DATABASEMAP ERROR] Error mientras se construia la vista asociacion " + view.getName());
						e.printStackTrace();
					}
				}
				continue;
			}
			List<Integer> viewChildrenIdtos = view.getChildrenIdtos();
			List<Integer> viewProperties = view.getAllProperties();
			List<String> viewColumns = view.getOrderedColumnNames();
			
			for (Integer childIdto : viewChildrenIdtos) {
				String sql;
				try {
					sql = getTableQueryForView(view, childIdto, viewProperties, viewColumns);
				} catch (NoSuchColumnException e) {
					System.err.println("[DATABASEMAP ERROR] Error mientras se construia la sentencia de consulta de la tabla " + childIdto + " para la vista " + view.getName());
					e.printStackTrace();
					continue;
				}
				view.setTableQuery(childIdto, sql);
			}
		}
		
		String characterBegin = gSQL.getCharacterBegin();
		String characterEnd = gSQL.getCharacterEnd();
//		int i = 1;
		for (View view : views) {
			if(!excludedIdtos.contains(view.getId())){
				String sql = view.getCreationViewStatement(fcdb.getGestorDB());
				
				try {
					DBQueries.execute(fcdb, "DROP VIEW IF EXISTS " + characterBegin + view.getName() + characterEnd + ";");
					DBQueries.execute(fcdb, sql);
				} catch (SQLException e) {
					e.printStackTrace();
					System.err.println("[DATABASEMAP ERROR] Error al ejecutar la consulta: " + sql);
				} catch (NamingException e) {
					e.printStackTrace();
					System.err.println("[DATABASEMAP ERROR] Error al ejecutar la consulta: " + sql);
				}
//				System.out.println( i + " : " + sql);
//				i ++;
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Metodos privados
	///////////////////////////////////////////////////////////////////////////
	
	/**
	 * Crea una vista que representa la clase abstracta dada, aunando todas las
	 * clases hijas suyas que tengan una tabla que representen sus datos.
	 * 
	 * @param classInfo
	 *            Clase de la que se quiere crear la vista. <b>No se
	 *            comprueba que sea abstracta</b>.
	 */
	private View createVoidView(ClassInfo classInfo){
		List<ClassInfo> childClasses = new LinkedList<ClassInfo>();
		// El primer nivel no abstracto de hijos.
		getAllNonAbstractChildren(classInfo, childClasses);
		
		// Si no hemos encontrado ninguna clase hija que no sea abstracta, no tiene sentido continuar la busqueda.
		if (childClasses.isEmpty() && classInfo.isAbstractClass()){
			return null;
		}
		
		List<PropertyInfo> commonProperties = new LinkedList<PropertyInfo>();
		Set<Integer> externalizedProperties = new HashSet<Integer>();
		getViewProperites(classInfo, childClasses, commonProperties, externalizedProperties);
		
		View view = new View(classInfo, commonProperties, externalizedProperties);
		
		Map<Integer, ClassInfo> childClassesByIdto = new Hashtable<Integer, ClassInfo>();
		Map<Integer, Table> childTablesByIdto = new Hashtable<Integer, Table>();
		
		if (! classInfo.isAbstractClass()){
			childClassesByIdto.put(classInfo.getIdto(), classInfo);
			childTablesByIdto.put(classInfo.getIdto(), tables.get(classInfo.getIdto()));
		}
		
		views.put(classInfo.getIdto(), view);
		
		// Modifica el mapa para que refleje que todas las clases hijas
		// indicadas estan contenidas en la vista actual.
		for (ClassInfo childClass : childClasses) {
			int idto = childClass.getIdto();
			List<View> viewsContainingChildClass = viewsCointainingClass.get(idto);
			if(viewsContainingChildClass == null){
				viewsContainingChildClass = new LinkedList<View>();
				viewsCointainingClass.put(idto, viewsContainingChildClass);
			}
			viewsContainingChildClass.add(view);
			childClassesByIdto.put(idto, childClass);
			childTablesByIdto.put(idto, tables.get(idto));
		}
		
		view.setChildren(childClassesByIdto, childTablesByIdto);
		return view;
	}

	/**
	 * Encuentra todos los hijos no abstractos de la clase dada.
	 * 
	 * @param abstractClass
	 *            Clase abstracta a la que le queremos buscar el primer nivel de
	 *            hijos no abstractos.
	 * @param childClasses
	 *            Lista donde vamos acumulando las clases no abstractas que
	 *            vamos encontrando.
	 */
	private void getAllNonAbstractChildren(ClassInfo abstractClass, List<ClassInfo> childClasses) {
		for (Integer idto : abstractClass.getChildClasses()) {
			if (! idto.equals(abstractClass.getIdto())){
				ClassInfo childClass = classes.get(idto);
				if (! childClass.isAbstractClass()){
					childClasses.add(childClass);
				}
			}
		}
	}

	/**
	 * Crea los vinculos entre las vistas.
	 * 
	 * @param view
	 *            Vista de la que queremos crear los vinculos que pudieran
	 *            existir con otras vistas.
	 */
	private void createViewLinks(View view){
		List<Integer> externalizedProperties = view.getExternalizedProperties();
		ClassInfo viewClass = view.getViewClass();
		for (Integer idProperty : externalizedProperties) {
			Integer inverseProperty = inverseProperties.get(idProperty);
			if (inverseProperty != null && (structuralProperties.contains(inverseProperty))){
				// Si la propiedad actual tiene inversa, y dicha inversa es
				// estructural. Nos saltamos el tratamiento de la propiedad
				// actual porque sera la estructural la que se trate.
				continue;
			}
			PropertyInfo property = view.getPropertyInfo(idProperty);
			if (view.isObjectProperty(idProperty)){
				Set<Integer> rangeIdtos = property.getPropertyTypes();
				for (Integer rangeIdto : rangeIdtos) {
					ClassInfo rangeClass = classes.get(rangeIdto);
					if (rangeClass.isAbstractClass() && structuralProperties.contains(idProperty) && ! sharedProperties.contains(idProperty)){
						// Se trata de una propiedad estructural que no esta
						// compartida, con lo cual va reflejada en la tabla del
						// rango.
						putPropertyInRangeView(viewClass, rangeClass, property);
					}else{
						createAssociationView(viewClass, rangeClass, property);
					}
				}
			}else{
				createExternalizedDataPropertyView(viewClass, property);
			}
		}	
	}
	
	/**
	 * Metodo que se encarga de rellenar las columnas de una vista indicada. Las
	 * propiedades externalizadas se suponen tratadas y no se controlan en este
	 * metodo. Es decir, si una propiedad aparece como externalizada en una
	 * vista, en este metodo simplmente se ignorara.
	 * 
	 * @param view
	 *            Vista de la que se quieren crear las columnas.
	 */
	private void fillViewColumns(View view){
		List<Integer> properties = view.getAllProperties();
		for (Integer idProperty : properties) {
			if (idProperty < 0 || view.isExternalizedProperty(idProperty) || view.isForeignProperty(idProperty)){
				// Se supone que el tratamiento de las propiedades
				// externalizadas se hace a parte. Y las propiedades con
				// identificador ficticio ya tienen construida su columna.
				continue;
			}
			PropertyInfo property = view.getPropertyInfo(idProperty);
			if (view.isObjectProperty(idProperty)){
				Set<Integer> propertyRange = property.getPropertyTypes();
				boolean multiRangedProperty = propertyRange.size() > 1;
				for (Integer rangeIdto : propertyRange) {
					ClassInfo rangeClass = classes.get(rangeIdto);
					String columnName = property.getName();
					if (multiRangedProperty){
						// Si es una propiedad de rango multiple, tenemos que diferenciar las columnas.
						columnName += rangeClass.getName().toUpperCase();
					}
					Set<Integer> rangeSet = new HashSet<Integer>();
					rangeSet.add(rangeIdto);
					rangeSet.addAll(rangeClass.getChildClasses());
					view.addViewColumn(columnName, Constants.IDTO_INT, idProperty, rangeSet, true, rangeIdto);
				}
			}else{
				Integer propertyType = property.getPropertyTypes().iterator().next();
				view.addViewColumn(property.getName(), propertyType, idProperty);
			}
		}
	}
	
	/**
	 * Analiza la clase no abstracta dada para comprobar si tiene propiedades
	 * que apunten a clases abstractas, es decir, vinculos con las vistas
	 * creadas.
	 * 
	 * @param nonAbstractClass
	 *            Clase que se quiere analizar.
	 */
	private void createTableToViewLinks(ClassInfo nonAbstractClass){
		for (PropertyInfo property : nonAbstractClass.getAllProperties()) {
			Integer inverseProperty = inverseProperties.get(property.getIdProp());
			if (inverseProperty != null && structuralProperties.contains(inverseProperty)){
				continue;
			}
			Set<Integer> propertyTypes = property.getPropertyTypes();
			Set<Integer> abstractRanges = new HashSet<Integer>();
			for (Integer type : propertyTypes) {
				if (! Constants.isDataType(type)){
					ClassInfo rangeClass = classes.get(type);
					if (views.get(rangeClass.getIdto()) != null){
						abstractRanges.add(type);
					}
				}
			}
			for (Integer referencedClass : abstractRanges) {
				ClassInfo rangeClass = classes.get(referencedClass);
				createAssociationView(nonAbstractClass, rangeClass, property);
			}
		}
	}

	/**
	 * Encuentra todas las propiedades del las clases hijas dadas y monta las
	 * listas de propieades que hay que poner en la vista y cuales de dichas
	 * propiedades estan externalizadas.
	 * 
	 * @param classInfo
	 *            Clase padre abstracta de la que se quiere crear la vista.
	 * @param childClasses
	 *            De las que vamos a sacar las propiedades que hay que añadir a
	 *            la vista.
	 * @param commonPropierties
	 *            Lista en la que se van a incluir todas las propiedades que se
	 *            han de incluir en la vista.
	 * @param externalizedProperties
	 *            Conjunto de propiedades que estan externalizadas.
	 */
	private void getViewProperites(ClassInfo classInfo, List<ClassInfo> childClasses, List<PropertyInfo> commonProperties, Set<Integer> externalizedProperties) {
		Map<Integer, PropertyInfo> propertiesToAdd = new Hashtable<Integer, PropertyInfo>();
		ListIterator<PropertyInfo> iterator = classInfo.getAllProperties().listIterator();
		while (iterator.hasNext()) {
			PropertyInfo property = iterator.next();
			Integer idProperty = property.getIdProp();
			if(inverseProperties.containsKey(idProperty)){
				Integer inverseProperty = inverseProperties.get(idProperty);
				if (structuralProperties.contains(inverseProperty)){
					// Las propiedades que tienen una inversa estructural las borramos.
					iterator.remove();
					continue;
				}
			}
			PropertyInfo clonedProperty = property.clone();
			for (ClassInfo childClass : childClasses){
				// Comparamos la propiedad de la clase abstracta con la de la
				// clase hija para que se conozcan todos los rangos que se
				// pueden dar. 
				PropertyInfo childProperty = childClass.getProperty(idProperty);
				if (childProperty == null){
					// Esta clase hija no tiene la propiedad, seguramente porque
					// sea una propiedad de una clase hermana.
					continue;
				}
				checkRangesCompatibility(clonedProperty, childProperty);
				if (clonedProperty.getMaxCardinality() < childProperty.getMaxCardinality()){
					clonedProperty.setMaxCardinality(childProperty.getMaxCardinality());
				}
				if (clonedProperty.getMinCardinality() > childProperty.getMinCardinality()){
					clonedProperty.setMinCardinality(childProperty.getMinCardinality());
				}
				// Si la propiedad esta externalizada en uno de los hijos, para
				// nosotros estara externalizada tambien en la vista.
				Table childTable = tables.get(childClass.getIdto());
				if (childTable.isExternalizedProperty(idProperty)){
					externalizedProperties.add(idProperty);
				}
			}
			
			propertiesToAdd.put(clonedProperty.getIdProp(), clonedProperty);
		}
		// Si la clase no es abstracta, se tiene que comprobar la clase en busca
		// de propiedades externalizadas tambien.
		if (! classInfo.isAbstractClass()){
			Table table = tables.get(classInfo.getIdto());
			for (PropertyInfo property : classInfo.getAllProperties()){
				int idProp = property.getIdProp();
				if (table.isExternalizedProperty(idProp)){
					externalizedProperties.add(idProp);
				}
			}
		}
		commonProperties.addAll(propertiesToAdd.values());
	}

	/**
	 * Comprueba los rangos de dos propiedades para hacer la interseccion.
	 * Siempre se queda con el el mas amplio si uno hereda del otro.
	 * 
	 * @param existingProperty
	 *            Propiedad que ya esta mapeada.<br>
	 *            Es una copia de una de las propiedades originales y es la
	 *            propiedad que se va a usar en la vista.
	 * @param property
	 *            Propiedad con el mismo id que la ya existente y de la que
	 *            queremos comprobar la compatibilidad del rango.
	 */
	private void checkRangesCompatibility(PropertyInfo existingProperty, PropertyInfo property) {
		Set<Integer> originalRanges = existingProperty.getPropertyTypes();
		Set<Integer> rangesToAdd = new HashSet<Integer>();
		if (Constants.isDataType(originalRanges.iterator().next())){
			// Si es una DataProperty, salimos.
			return;
		}
		
		Set<Integer> candidateRanges = new HashSet<Integer>(property.getPropertyTypes());
		candidateRanges.removeAll(originalRanges);
		if (candidateRanges.isEmpty()){
			/* Todos los rangos candidatos ya estaban incluidos en el rango de
			 * la propiedad existente.
			 */
			return;
		}
		
		Iterator<Integer> originalIterator = originalRanges.iterator();
		while (originalIterator.hasNext()){
			Integer originalIdto = originalIterator.next();
			ClassInfo originalClass = classes.get(originalIdto);
			
			Set<Integer> originalParents = originalClass.getParentClasses();
			Iterator<Integer> candidatesIterator = candidateRanges.iterator();
			while(candidatesIterator.hasNext()){
				Integer candidateIdto = candidatesIterator.next();
				ClassInfo candidateClass = classes.get(candidateIdto);
				
				Set<Integer> candidateParents = candidateClass.getParentClasses();
				
				if (candidateParents != null && candidateParents.contains(originalIdto)){
					/* La clase candidata es hija de la clase original. No nos
					 * interesa este rango.
					 */
					candidatesIterator.remove();
					continue;
				}
				
				if (originalParents != null && originalParents.contains(candidateIdto)){
					/* La clase original es hija de la candidata. Hay que borrar
					 * la clase original del rango de la propiedad y añadir la
					 * candidata.
					 */
					originalIterator.remove();
					rangesToAdd.add(candidateIdto);
					break;
				}
				
				if (originalParents != null && candidateParents != null){
					Integer commonParent = searchCommonParents(originalIdto, originalParents, candidateIdto, candidateParents);
					if (commonParent != null){
						// Tienen un padre en comun, con lo cual quitamos los dos
						// elementos de sus respectivos conjuntos y marcamos que
						// debemos añadir el padre que tienen en comun al rango.
						rangesToAdd.add(commonParent);
						candidatesIterator.remove();
						originalIterator.remove();
					}
				}
			}
		}
		originalRanges.addAll(rangesToAdd);
		// Todos los rangos candidatos que queden en la lista no tienen ninguna
		// relacion con ninguno de los rangos originales y debemos añadirlos.
		originalRanges.addAll(candidateRanges);
	}

	/**
	 * Busca un posible padre en comun entre las dos clases.
	 * 
	 * @param originalIdto
	 *            Identificador de la clase del rango original de la propiedad
	 *            que estamos procesando.
	 * @param originalParents
	 *            Padres de la clase del rango original.
	 * @param candidateIdto
	 *            Identificador de la clase del rango candidato que queremos
	 *            analizar.
	 * @param candidateParents
	 *            Padres de la clase del rango candidato.
	 * @return Identificador del padre en comun entre las dos clases si existe o
	 *         <code>null</code> si no tienen ninguna relacion el uno con el
	 *         otro.
	 */
	private Integer searchCommonParents(Integer originalIdto, Set<Integer> originalParents, Integer candidateIdto, Set<Integer> candidateParents) {
		Integer result = null;
		Set<Integer> intersection = new HashSet<Integer>(originalParents);
		intersection.retainAll(candidateParents);
		if (! intersection.isEmpty()){
			ClassInfo originalClass = classes.get(originalIdto);
			List<Integer> parents = originalClass.getInmediateParents();
			result = searchNearestParent(intersection, parents);
		}
		return result;
	}

	/**
	 * Busca el padre mas cercano en el conjunto interseccion de los padres de
	 * dos clases.
	 * 
	 * @param intersection
	 *            Conjunto que contiene todos los padres que tienen como hijas a
	 *            las dos clases.
	 * @param parents
	 *            Padres inmediatos de una de las clases que hay que ir
	 *            comparando con los contenidos en la interseccion.
	 * @return Identificador de la clase padre mas cercana a la clase de
	 *         partida.<br>
	 *         Devolvera <code>null</code> si no encuentra ningun padre que
	 *         concuerde con alguno de conjunto interseccion
	 */
	private Integer searchNearestParent(Set<Integer> intersection, List<Integer> inmediateParents) {
		if (intersection == null || inmediateParents == null){
			return null;
		}
		Integer result = null;
		// Lista donde vamos a ir acumulando los padres de los padres que
		// estamos analizando, para poder buscar en el siguiente nivel si este
		// no arroja resultados
		Set<Integer> nextParentLevel = new HashSet<Integer>();
		for (Integer idto : inmediateParents) {
			if (intersection.contains(idto)){
				result = idto;
				break;
			}
			ClassInfo classInfo = classes.get(idto);
			nextParentLevel.addAll(classInfo.getInmediateParents());
		}
		// Buscamos un nivel mas arriba.
		if (result == null){
			searchNearestParent(intersection, new LinkedList<Integer>(nextParentLevel));
		}
		return result;
	}

	/**
	 * Deduce si se tienen que incluir columnas en la tabla del rango para
	 * representar la propiedad indicada. Si ya existen un par de columnas en la
	 * vista del rango que representan a la propiedad y que pueden contener
	 * informacion de la clase del dominio, no se incluira ninguna columna nueva
	 * en la tabla del rango.
	 * 
	 * @param domainClass
	 *            Clase de la que surge la propiedad que apunta a la clase del
	 *            rango.
	 * @param rangeClass
	 *            Clase apuntada por la propiedad.
	 * @param property
	 *            Propiedad por la que se relacionan las dos clases.
	 */
	private void putPropertyInRangeView(ClassInfo domainClass, ClassInfo rangeClass, PropertyInfo property) {
		View rangeView = views.get(rangeClass.getIdto());
		if (rangeView == null){
			System.err.println("[DATABASEMAP ERROR] La propiedad " + property.getName() + " apunta a una vista que no existe para la clase " + rangeClass.getName());
			return;
		}
		ViewColumn[] columns = rangeView.getObjectPropertyColumn(property.getIdProp(), domainClass.getIdto());
		if (columns != null){
			// Ya existe un par de columnas para representar la relacion
			// mediante esta propiedad entre las dos clases, con lo cual
			// salimos, indicandole a la vista del dominio donde esta
			// externalizada la propiedad
			View domainView = views.get(domainClass.getIdto());
			domainView.addPropertyLocation(property.getIdProp(), rangeView.getId());
			return;
		}
		// Creamos el conjunto del rango de las nuevas columnas que vamos a insertar en la vista de la clase del rango. 
		Set<Integer> rangeSet = new HashSet<Integer>();
		rangeSet.add(domainClass.getIdto());
		rangeSet.addAll(domainClass.getChildClasses());
		
		rangeView.addObjectProperty(property);
		String columnName = domainClass.getName().toLowerCase().replaceAll("\\.", "") + View.SUFFIX_ID;
		rangeView.addViewColumn(columnName, Constants.IDTO_INT, property.getIdProp(), rangeSet, true, domainClass.getIdto());
		View domainView = views.get(domainClass.getIdto());
		domainView.addPropertyLocation(property.getIdProp(), rangeView.getId());
	}
	
	/**
	 * Crea una vista asociacion entre las dos clases existentes si no existe
	 * ya. Si existe, se le tiene que añadir la columna para las propiedades por
	 * las que se vinculan.
	 * 
	 * @param domainClass
	 *            Clase propietaria de la propiedad y la que apunta a la clase
	 *            del rango.
	 * @param rangeClass
	 *            Clase que es apuntada por la propiedad.
	 * @param property
	 *            Propiedad mediante la cual se relacionan las dos clases.
	 */
	private void createAssociationView(ClassInfo domainClass, ClassInfo rangeClass, PropertyInfo property) {
		Integer viewId = assocPairAssignedView.get(new AssocciationPair(domainClass.getIdto(), rangeClass.getIdto()));
		if (viewId != null){
			// Significa que estas dos clases ya se relacionan por otra
			// propiedad y que tenemos que incluir la columna propiedad para
			// saber de los registros que salen, cual se corresponde a cada
			// propiedad.
			View assocView = views.get(viewId);
			if (! assocView.isObjectProperty(property.getIdProp())){
				// Si nos dice que no es ObjectProperty para la vista
				// asociacion, significa que todavia no tiene conocimiento de
				// esta propiedad y que tenemos que añadirla.
				assocView.addPropertyColumn();
				Set<Integer> rangeSet = new HashSet<Integer>();
				Set<Integer> propertyTypes = property.getPropertyTypes();
				rangeSet.addAll(propertyTypes);
				for (Integer rangeIdto : propertyTypes) {
					ClassInfo rangePropertyClass = classes.get(rangeIdto);
					rangeSet.addAll(rangePropertyClass.getChildClasses());
				}
				assocView.addPropertyToRange(property.getIdProp(), rangeSet);
			}
		}else{
			// No existe todavia una vista asociacion que relacione las dos
			// clases.
			viewId = nextId;
			nextId ++;
			View assocView = new View(domainClass, rangeClass, property.getIdProp(), viewId);
			assocPairAssignedView.put(new AssocciationPair(domainClass.getIdto(), rangeClass.getIdto()), viewId);
			views.put(viewId, assocView);
		}
		View domainView = views.get(domainClass.getIdto());
		if (domainView != null){
			domainView.addPropertyLocation(property.getIdProp(), viewId);
		
		} 
		if (! domainClass.isAbstractClass()){
			Table domainTable = tables.get(domainClass.getIdto());
			domainTable.addViewExternalizedProperty(property.getIdProp(), viewId);
			tableViewAssocs.add(viewId);
		}
	}
	
	/**
	 * Crea una vista que va a almacenar la informacion de la DataProperty
	 * externalizada. Dicha vista solo tendra cuatro columnas: el autonumerico
	 * de la clave primaria, el campo del dato y el tableId y el IDTO que
	 * identifican al objeto padre.
	 * 
	 * @param domainClass
	 *            Clase de la que surge la DataProperty que ha sido
	 *            externalizada.
	 * @param property
	 *            DataPorperty que ha sido externalizada.
	 */
	private void createExternalizedDataPropertyView(ClassInfo domainClass, PropertyInfo property) {
		int viewId = nextId;
		nextId ++;
		View view = new View(domainClass, property, viewId);
		views.put(viewId, view);
		View domainView = views.get(domainClass.getIdto());
		domainView.addPropertyLocation(property.getIdProp(), viewId);
	}
	
	/**
	 * Añade a la vista dada las consultas que necesita para tener datos.
	 * 
	 * @param view
	 *            Vista que relaciona una tabla con una vista, donde la clase de
	 *            la tabla es el dominio.
	 * @throws NoSuchColumnException Si se pregunta por una columna que no exite a una tabla.
	 */
	private void constructTableToViewAssoc(View view) throws NoSuchColumnException {
		ViewColumn[] domainColumns = null;
		ViewColumn[] rangeColumns = null;
		ViewColumn propertyColumn = view.getDataPropertyColumn(View.ID_PROPERTY);
		Integer domainIdto = null;
		Integer rangeIdto = null;
		// Lista que contiene las propiedades a las que hace referencia el rango de la asociacion.
		List<Integer> rangeProperties = new LinkedList<Integer>();
		for (Integer idProperty : view.getAllProperties()) {
			if (view.isObjectProperty(idProperty)){
				/*
				 * En las vistas asociacion solo hay 4 columnas a lo sumo que
				 * contengan una ObjectProperty, dos para dominio y dos para el
				 * rango. Si le preguntamos a la vista por las columnas de una
				 * ObjectProperty, siempre devolvera almenos un nombre, el de la
				 * columna que contiene la informacion del tableId
				 */
				String columnContainingProperty = view.getColumnNamesContainingProperty(idProperty).get(0);
				if (idProperty.equals(View.ID_DOMAIN)){
					domainIdto = view.getColumnDomain(columnContainingProperty);
					domainColumns = view.getObjectPropertyColumn(View.ID_DOMAIN, domainIdto);
				}else{
					rangeProperties.add(idProperty);
					if (rangeColumns == null){
						rangeIdto = view.getColumnDomain(columnContainingProperty);
						rangeColumns = view.getObjectPropertyColumn(idProperty, rangeIdto);
					}
				}
			}
		}
		
		Table domainTable = tables.get(domainIdto);
		for (Integer idProperty : rangeProperties) {
			if (! domainTable.isExternalizedProperty(idProperty)){
				String sql = queryForNonExternalizedProperty(domainColumns, rangeColumns, propertyColumn, view.getOrderedColumnNames(), domainTable, idProperty);
				view.setTableQuery(0, sql);
			}else{
				queryForExternalizedProperty(view, domainColumns, rangeColumns, propertyColumn, domainTable, idProperty);
			}
		}
	}
	
	/**
	 * Dada una vista y una clase, construye la consulta necesaria para encajar
	 * los datos de la tabla en la vista.<br>
	 * Tambien se ocupa de analizar las propiedades externalizadas de la vista
	 * para ver si tiene que añadir a otras vistas alguna consulta.<br>
	 * Las consultas en las vistas asociacion solo se crearon como derivado de
	 * este procesamiento.
	 * 
	 * @param view
	 *            Vista sobre la que se reflejaran los datos de la consulta que
	 *            tenemos que construir.
	 * @param childIdto
	 *            Identificador de la clase de la que tenemos que generar la
	 *            consulta.
	 * @param viewProperties
	 *            Identificadores de las propiedades de las que tiene
	 *            conocimiento la vista.
	 * @param viewColumns
	 *            Lista de las columnas de las vistas, ordenadas tal y como se
	 *            tienen que devolver los valores.
	 * @return Cadena que hace la consulta a la clase indicada.
	 * @throws NoSuchColumnException Si se le ha preguntado a una tabla por una columna que no contenia.
	 * @throws NoSuchColumnException 
	 */
	private String getTableQueryForView(View view, Integer childIdto, List<Integer> viewProperties, List<String> viewColumns) throws NoSuchColumnException, NoSuchColumnException {
		// Este mapa va a vincular con cada columna como se obtiene los datos para rellenarla.
		Map<String, String> queryByColumnName = new Hashtable<String, String>();
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		Table childTable = tables.get(childIdto);
		for (Integer idProperty : viewProperties) {
			PropertyInfo property = view.getPropertyInfo(idProperty);
			if (view.isExternalizedProperty(idProperty)){
				// Es una propiedad que se ha externalizado a otra tabla.
				if (view.isObjectProperty(idProperty)){
					Set<Integer> propertyLocations = view.getExternalizedPropertyLocations(idProperty);
					for (Integer rangeIdto : property.getPropertyTypes()) {
						Integer viewId = assocPairAssignedView.get(new AssocciationPair(view.getId(), rangeIdto));
						if (viewId == null){
							// No hay vista asociacion entre las dos clases. Saltamos al siguiente rango.
							continue;
						}
						if (propertyLocations.contains(viewId)){
							View assocView = views.get(viewId);
							addQueryForAssociationView(childIdto, rangeIdto, property, assocView);
						}
					}
				}else{
					// Es una DataProperty externalizada.
					Integer propertyLocation = view.getExternalizedPropertyLocations(idProperty).iterator().next();
					View externalView = views.get(propertyLocation);
					addQueryForExternalizedDataProperty(childIdto, property, externalView);
				}
			}else if (view.isForeignProperty(idProperty)){
				// Es una propiedad que ha venido externalizada de otra tabla.
				List<String> columnNames = view.getColumnNamesContainingProperty(idProperty);
				for (String columnNameForProperty : columnNames) {
					// Conseguimos el idto de la clase a la que apunta la columna
					Integer columnDomainIdto = view.getColumnDomain(columnNameForProperty);
					ClassInfo propertyDomainClass = classes.get(columnDomainIdto);
					// Construimos un conjunto de todos los hijos mas el padre del dominio obtenido, porque tenemos que coger los datos
					Set<Integer> domainSet = new HashSet<Integer>();
					domainSet.add(columnDomainIdto);
					domainSet.addAll(propertyDomainClass.getChildClasses());
					ViewColumn[] columns = view.getObjectPropertyColumn(idProperty, columnDomainIdto);
					List<String> childTableColumnNames = childTable.getColumnNamesContainingProperty(idProperty);
					String tableIdSQL = "";
					String idtoSQL = "";
					if (childTableColumnNames != null){
						boolean segmentar=false;
						int queriesAdded = 0;
						for (String childTableColumnName : childTableColumnNames) {
							Integer childTableColumnDomain = childTable.getColumnDomain(childTableColumnName);
							if (domainSet.contains(childTableColumnDomain)){
								if (queriesAdded == 0){
									tableIdSQL = characterBegin + childTableColumnName + characterEnd;
									idtoSQL = childTableColumnDomain.toString();
									queriesAdded ++;
								}else if (queriesAdded == 1){
									String auxId = "CASE WHEN " + tableIdSQL + " IS NOT NULL THEN " + tableIdSQL + " WHEN " + characterBegin + childTableColumnName + characterEnd + " IS NOT NULL THEN " + characterBegin + childTableColumnName + characterEnd + " ";
									String auxIdto = "CASE WHEN " + tableIdSQL + " IS NOT NULL THEN " + idtoSQL + " WHEN " + characterBegin + childTableColumnName + characterEnd + " IS NOT NULL THEN " + childTableColumnDomain.toString() + " ";
									tableIdSQL = auxId;
									idtoSQL = auxIdto;
									queriesAdded ++;
								}else{
									tableIdSQL += "WHEN " + characterBegin + childTableColumnName + characterEnd + " IS NOT NULL THEN " + characterBegin + childTableColumnName + characterEnd + " ";
									idtoSQL += " WHEN " + characterBegin + childTableColumnName + characterEnd + " IS NOT NULL THEN " + childTableColumnDomain.toString() + " ";
								}
							}
						}
						if (tableIdSQL.startsWith("CASE")){
							tableIdSQL += "ELSE NULL END";
							idtoSQL += "ELSE NULL END";
						}
					}
					else{
						tableIdSQL = gSQL.getNullValueWithDataType(Constants.IDTO_INT);
						idtoSQL = gSQL.getNullValueWithDataType(Constants.IDTO_INT);
					}
					queryByColumnName.put(columns[0].getColumnName(), tableIdSQL);
					if (columns[1] != null){
						queryByColumnName.put(columns[1].getColumnName(), idtoSQL);
					}
				}
			}else if (idProperty < 0){
				// Es una de las propiedades ficticias.
				ViewColumn column = view.getDataPropertyColumn(idProperty);
				if (idProperty.equals(View.ID_TABLE_ID)){
					queryByColumnName.put(column.getColumnName(), characterBegin + Table.COLUMN_NAME_TABLEID + characterEnd);
				}else if (idProperty.equals(View.ID_IDTO)){
					queryByColumnName.put(column.getColumnName(), childIdto.toString());
				}else if (idProperty.equals(View.ID_DESTINATION)){
					queryByColumnName.put(column.getColumnName(), characterBegin + Table.COLUMN_NAME_DESTINATION + characterEnd);
				}
			}else{
				// Es una propiedad normal
				List<String> tableColumnsForProperty = childTable.getColumnNamesContainingProperty(idProperty);
				if (tableColumnsForProperty == null || tableColumnsForProperty.isEmpty()){
					// Esta tabla no tiene informacion sobre dicha propiedad,
					// con lo cual tenemos que rellenar a null esas columnas.
					fillColumnsWithNullValue(view, queryByColumnName, idProperty, property.getPropertyTypes().iterator().next());
					continue;
				}
				if (view.isObjectProperty(idProperty)){
					// Mapa auxiliar donde vamos a ir construyendo las sentencias SQL.
					Map<String, String> auxiliarQueryByColumnName = new Hashtable<String, String>();
					for (String tableColumnName : tableColumnsForProperty) {
						Integer tableColumnDomain = childTable.getColumnDomain(tableColumnName);
						ViewColumn[] columns = view.getObjectPropertyColumn(idProperty, tableColumnDomain);
						String oldTableIdSQL = auxiliarQueryByColumnName.get(columns[0].getColumnName());
						String newTableIdSQL = "";
						if (oldTableIdSQL == null){
							newTableIdSQL = characterBegin + tableColumnName + characterEnd;
						}else if (oldTableIdSQL != null && oldTableIdSQL.indexOf("CASE") == -1){
							newTableIdSQL = "CASE WHEN " + oldTableIdSQL + " IS NOT NULL THEN " + oldTableIdSQL + " WHEN " + characterBegin + tableColumnName + characterEnd + " IS NOT NULL THEN " + characterBegin + tableColumnName + characterEnd + " ";
						}else{
							newTableIdSQL = oldTableIdSQL + "WHEN " + characterBegin + tableColumnName + characterEnd + " IS NOT NULL THEN " + characterBegin + tableColumnName + characterEnd + " ";
						}
						auxiliarQueryByColumnName.put(columns[0].getColumnName(), newTableIdSQL);
						if (columns[1] != null){
							String idtoSQL = auxiliarQueryByColumnName.get(columns[1].getColumnName());
							if (oldTableIdSQL == null){
								idtoSQL = tableColumnDomain.toString();
							}else if (oldTableIdSQL != null && oldTableIdSQL.indexOf("CASE") == -1){
								idtoSQL = "CASE WHEN " + oldTableIdSQL + " IS NOT NULL THEN " + idtoSQL + " WHEN " + characterBegin + tableColumnName + characterEnd + " IS NOT NULL THEN " +  tableColumnDomain + " ";
							}else{
								idtoSQL += "WHEN " + characterBegin + tableColumnName + characterEnd + " IS NOT NULL THEN " +  tableColumnDomain + " ";
							}
							auxiliarQueryByColumnName.put(columns[1].getColumnName(), idtoSQL);
						}
					}
					// Una vez tenemos todas las sentencias construidas con
					// todos los casos, le incluimos la clausula de 'en
					// cualquier otro caso' que pondra la columna a NULL si no
					// se ha cumplido ninguna condicion, y cerramos el CASE.
					for (String columnName : auxiliarQueryByColumnName.keySet()) {
						String sql = auxiliarQueryByColumnName.get(columnName);
						if (sql.indexOf("CASE") != -1) {
							sql += "ELSE NULL END";
						}
						// Añadimos la sentencia a la lista real.
						queryByColumnName.put(columnName, sql);
					}
				} else {
					ViewColumn viewColumn = view.getDataPropertyColumn(idProperty);
					if (tableColumnsForProperty == null || tableColumnsForProperty.isEmpty()){
						fillColumnsWithNullValue(view, queryByColumnName, idProperty, property.getPropertyTypes().iterator().next());
					}else if (tableColumnsForProperty.size() > 1){
						System.err.println("[DATABASEMAP ERROR] Hay una DataProperty con mas de una columna en la tabla " + childTable.getName());
						continue;
					}else{
						String tableColumnName = tableColumnsForProperty.get(0);
						queryByColumnName.put(viewColumn.getColumnName(), characterBegin + tableColumnName + characterEnd);
					}
				}
			}
		}
		String sql = "SELECT ";
		boolean first = true;
		for (String viewColumnName : viewColumns) {
			if (! first){
				sql += ", ";
			}
			String columnQuery = queryByColumnName.get(viewColumnName);
			if (columnQuery == null){
				ViewColumn column = view.getColumnByName(viewColumnName);
				columnQuery = gSQL.getNullValueWithDataType(column.getColumnDataType());
			}
			sql += columnQuery;
			first = false;
		}
		sql += " FROM " + characterBegin + childTable.getName() + characterEnd;
		return sql;
	}

	/**
	 * Rellena con NULL el mapa para todas las columnas de la vistas asociadas
	 * con la propiedad dada.
	 * 
	 * @param view
	 *            Vista sobre la que estamos trabajando.
	 * @param queryByColumnName
	 *            Mapa que almacena que valor se le dara a cada columna.
	 * @param idProperty
	 *            Identificador de la propiedad de las que queremos que sus
	 *            columnas tengan NULL.
	 * @param dataType TODO
	 */
	private void fillColumnsWithNullValue(View view, Map<String, String> queryByColumnName, Integer idProperty, int dataType) {
		for(String columnName : view.getColumnNamesContainingProperty(idProperty)){
			Integer columnDomain = view.getColumnDomain(columnName);
			if (columnDomain == null){
				// Se trata de una DataProperty.
				ViewColumn column = view.getDataPropertyColumn(idProperty);
				queryByColumnName.put(column.getColumnName(), gSQL.getNullValueWithDataType(dataType));
			}else{
				// Se trata de una ObjectProperty.
				ViewColumn[] columns = view.getObjectPropertyColumn(idProperty, columnDomain);
				queryByColumnName.put(columns[0].getColumnName(), gSQL.getNullValueWithDataType(Constants.IDTO_INT));
				queryByColumnName.put(columns[1].getColumnName(), gSQL.getNullValueWithDataType(Constants.IDTO_INT));
			}
		}
	}

	/**
	 * Para una asociacion entre una tabla y una vista, donde el dominio es la
	 * tabla, construye la consulta/s que añade/n los datos si la propiedad a
	 * analizar esta externalizada.
	 * 
	 * @param view
	 *            Vista asociacion entre las dos clases para la que tenemos que
	 *            crear las consultas.
	 * @param domainColumns
	 *            Columnas de la vista asociacion que contienen los datos del
	 *            dominio de la propiedad.
	 * @param rangeColumns
	 *            Columnas de la vista asociacion que contienen los datos del
	 *            rango de la propiedad.
	 * @param propertyColumn
	 *            Columna de la vista asociacion que contiene la informacion de
	 *            la propiedad que vincula las dos clases.<br>
	 *            Puede ser <code>null</code> si solo se relacionan por una
	 *            propiedad.
	 * @param domainTable
	 *            Tabla que se encuentra en el dominio de la propiedad.
	 * @param idProperty
	 *            Propiedad por la que estamos relacionando una tabla con una
	 *            vista.
	 * @throws NoSuchColumnException 
	 */
	private void queryForExternalizedProperty(View view, ViewColumn[] domainColumns, ViewColumn[] rangeColumns, ViewColumn propertyColumn, Table domainTable, Integer idProperty) throws NoSuchColumnException {
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		List<String> viewColumns = view.getOrderedColumnNames();
		for (Integer propertyTableIdto : domainTable.getExternalizedPropertyLocations(idProperty)) {
			Map<String, String> queryByColumnName = new Hashtable<String, String>();
			// La propiedad esta en una localizacion externa. Solo nos
			// interesan las localizaciones que son tablas.
			Table propertyTable = tables.get(propertyTableIdto);
			if (propertyTable == null){
				// Significa que el identificador obtenido es de una
				// vista.
				continue;
			}
			if (propertyTable.isAssociation()){
				// La informacion de la propiedad se encuentra en una
				// tabla asociacion.
				String domainColumnName = domainTable.getName() + IQueryInfo.SUFFIX_ID;
				String rangeColumnName = propertyTable.getColumnNamesContainingProperty(idProperty).get(0);
				Integer rangeColumnDomain = propertyTable.getColumnDomain(rangeColumnName);
				String propertyColumnName = propertyTable.getColumnByName(IQueryInfo.COLUMN_NAME_PROPERTY) != null ? IQueryInfo.COLUMN_NAME_PROPERTY : null;
				queryByColumnName.put(domainColumns[0].getColumnName(), cB + domainColumnName + cE);
				if (domainColumns[1] != null){
					queryByColumnName.put(domainColumns[1].getColumnName(), domainTable.getId().toString());
				}
				queryByColumnName.put(rangeColumns[0].getColumnName(), cB + rangeColumnName + cE);
				if (rangeColumns[1] != null){
					queryByColumnName.put(rangeColumns[1].getColumnName(), rangeColumnDomain.toString());
				}
				if (propertyColumn != null){
					queryByColumnName.put(propertyColumn.getColumnName(), idProperty.toString());
				}
				String sql = "SELECT ";
				boolean first = true;
				for (String viewColumnName : viewColumns) {
					if (! first){
						sql += ", ";
					}
					sql += queryByColumnName.get(viewColumnName);
					first = false;
				}
				sql += " FROM " + cB + propertyTable.getName() + cE;
				if (propertyColumnName != null){
					sql += " WHERE " + cB + propertyColumnName + cE + "=" + idProperty.toString();
				}
				view.setTableQuery(0, sql);
			}else{
				// La informacion de la propiedad se encuentra en la
				// tabla que representa a la clase del rango.
				String domainColumnName = null;
				// Buscamos la columna que contiene las referencias a nuestra clase.
				for (String columnName : propertyTable.getColumnNamesContainingProperty(idProperty)) {
					Integer columnDomain = propertyTable.getColumnDomain(columnName);
					if (columnDomain.equals(domainTable.getId())){
						domainColumnName = columnName;
						break;
					}
				}
				if (domainColumnName == null){
					ClassInfo classInfo = classes.get(domainTable.getId());
					PropertyInfo property = classInfo.getProperty(idProperty);
					System.err.println("No se ha encontrado en la tabla " + propertyTable.getName() + " una columna que paunte a " + classInfo.getName() + " asociada a la propiedad " + property.getName());
					continue;
				}
				// El tableId del rango es el TableId de la tabla
				// propertyTable que hemos obtenido, y el tableId de el
				// dominio lo obtenemos de la columna de la que acabamos
				// de obtener el nombre.
				queryByColumnName.put(domainColumns[0].getColumnName(), cB + domainColumnName + cE);
				if (domainColumns[1] != null){
					queryByColumnName.put(domainColumns[1].getColumnName(), domainTable.getId().toString());
				}
				queryByColumnName.put(rangeColumns[0].getColumnName(), cB + Table.COLUMN_NAME_TABLEID + cE);
				if (rangeColumns[1] != null){
					queryByColumnName.put(rangeColumns[1].getColumnName(), String.valueOf(propertyTable.getId()));
				}
				if (propertyColumn != null){
					queryByColumnName.put(propertyColumn.getColumnName(), idProperty.toString());
				}
				String sql = "SELECT ";
				boolean first = true;
				for (String viewColumnName : viewColumns) {
					if (! first){
						sql += ", ";
					}
					sql += queryByColumnName.get(viewColumnName);
					first = false;
				}
				sql += " FROM " + cB + propertyTable.getName() + cE + " WHERE " + cB + domainColumnName + cE + " IS NOT NULL";
				view.setTableQuery(0, sql);
			}
		}
	}

	private String queryForNonExternalizedProperty(ViewColumn[] domainColumns, ViewColumn[] rangeColumns, ViewColumn propertyColumn, List<String> viewColumns, Table domainTable, Integer idProperty) throws NoSuchColumnException {
		Map<String, String> queryByColumnName = new Hashtable<String, String>();
		String cB = gSQL.getCharacterBegin(), cE = gSQL.getCharacterEnd();
		// La propiedad esta en la tabla del dominio.
		queryByColumnName.put(domainColumns[0].getColumnName(), cB + IQueryInfo.COLUMN_NAME_TABLEID + cE);
		if (domainColumns[1] != null){
			queryByColumnName.put(domainColumns[1].getColumnName(), domainTable.getId().toString());
		}
		List<String> columnNamesContainingProperty = domainTable.getColumnNamesContainingProperty(idProperty);
		if (columnNamesContainingProperty == null){
			System.out.println("ViewBuilder.queryForNonExternalizedProperty() : No se han encontrado columnas que contengan la propiedad " + idProperty + " en la tabla " + domainTable.getName());
			return null;
		}
		if (columnNamesContainingProperty.size() > 1){
			for(String columnName : columnNamesContainingProperty){
				Integer columnRangeIdto = domainTable.getColumnDomain(columnName);
				String rangeIdSQL = queryByColumnName.get(rangeColumns[0].getColumnName());
				if (rangeIdSQL == null){
					rangeIdSQL = "CASE";
				}
				rangeIdSQL += " WHEN " + cB + columnName + cE + " IS NOT NULL THEN " + cB + columnName + cE;
				queryByColumnName.put(rangeColumns[0].getColumnName(), rangeIdSQL);
				if (rangeColumns[1] != null){
					String rangeIdtoSQL = queryByColumnName.get(rangeColumns[1].getColumnName());
					if (rangeIdtoSQL == null){
						rangeIdtoSQL = "CASE";
					}
					rangeIdtoSQL += " WHEN " + cB + columnName + cE + " IS NOT NULL THEN " + columnRangeIdto.toString();
					queryByColumnName.put(rangeColumns[1].getColumnName(), rangeIdtoSQL);
				}
			}
			String rangeIdSQL = queryByColumnName.get(rangeColumns[0].getColumnName());
			rangeIdSQL += " ELSE NULL END";
			queryByColumnName.put(rangeColumns[0].getColumnName(), rangeIdSQL);
			if (rangeColumns[1] != null){
				String rangeIdtoSQL = queryByColumnName.get(rangeColumns[1].getColumnName());
				rangeIdtoSQL += " ELSE NULL END";
				queryByColumnName.put(rangeColumns[1].getColumnName(), rangeIdtoSQL);
			}
		}else{
			String rangeColumnName = columnNamesContainingProperty.get(0);
			Integer rangeColumnIdto = domainTable.getColumnDomain(rangeColumnName);
			queryByColumnName.put(rangeColumns[0].getColumnName(), cB + rangeColumnName + cE);
			queryByColumnName.put(rangeColumns[1].getColumnName(), rangeColumnIdto.toString());
		}
		if (propertyColumn != null){
			queryByColumnName.put(propertyColumn.getColumnName(), idProperty.toString());
		}
		String sql = "SELECT ";
		boolean first = true;
		for (String columnName : viewColumns) {
			if (! first){
				sql += ", ";
			}
			sql += queryByColumnName.get(columnName);
			first = false;
		}
		sql += " FROM " + cB + domainTable.getName() + cE + " WHERE";
		first = true;
		for (String columnName : columnNamesContainingProperty) {
			if (! first){
				sql += " OR";
			}
			sql += cB + columnName + cE + " IS NOT NULL";
			first = false;
		}
		return sql;
	}
	
	/**
	 * Añade a la vista asociacion entre las dos clases dadas, si existe, la
	 * consulta para obtener los datos que relacionan las dos clase mediante la
	 * propiedad indicada.
	 * 
	 * @param domainIdto
	 *            Identificador de la clase del dominio de la propiedad. Tiene
	 *            que ser el identificador de una clase no abstracta.
	 * @param rangeIdto
	 *            Identificador de la clase del rango de la propiedad.
	 * @param property
	 *            Objeto que contiene toda la informacion de la propiedad.
	 * @param assocView
	 *            Vista asociacion donde tenemos que reflejar el vinculo entre
	 *            las dos clases indicadas.
	 * @throws NoSuchColumnException
	 *             Si se pregunta a una tabla por una columna que no contiene.
	 */
	private void addQueryForAssociationView(int domainIdto, int rangeIdto, PropertyInfo property, View assocView) throws NoSuchColumnException {
		Table domainTable = tables.get(domainIdto);
		ClassInfo rangeClass = classes.get(rangeIdto);
		int idProperty = property.getIdProp();
		// Construimos este conjunto porque en las tablas no se apunta a clases
		// abstractas, y si rangeIdto es el idto de una clase abstracta, lo que
		// tenemos que buscar es como apuntan las tablas a las clases
		// especificas cuyo padre es rangeIdto.
		Set<Integer> rangeSet = new HashSet<Integer>();
		rangeSet.add(rangeIdto);
		rangeSet.addAll(rangeClass.getChildClasses());
		if (! domainTable.isExternalizedProperty(idProperty)){
			// La propiedad esta en la tabla que representa la clase dominio.
			List<String> columnNames = domainTable.getColumnNamesContainingProperty(idProperty);
			if (columnNames == null || columnNames.isEmpty()){
				// Esta clase no tiene conocimiento de esta propiedad.
				return;
			}
			for (String columnName : columnNames) {
				Integer columnDomain = domainTable.getColumnDomain(columnName);
				// Miramos si la clase a la que apunta la columna es una de las
				// del conjunto construido de rangos.
				if (rangeSet.contains(columnDomain)){
					String sql = getQueryForAssociationViewFromDomainTable(assocView, domainTable, idProperty, columnName, columnDomain);
					assocView.setTableQuery(0, sql);
				}
			}
		}else{
			// La propiedad esta en una tabla externa. Es mas, puede estar en
			// mas de una tabla externa.
			Set<Integer> propertyTableLocation = domainTable.getExternalizedPropertyLocations(idProperty);
			for (Integer externalTableIdto : propertyTableLocation) {
				Table rangeTable = tables.get(externalTableIdto);
				if (rangeTable == null){
					// Es el idto de una vista, no hacemos nada.
					continue;
				}
				String sql;
				if (rangeTable.isAssociation()){
					String rangeColumnName = rangeTable.getColumnNamesContainingProperty(idProperty).get(0);
					Integer externalTableRangeIdto = rangeTable.getColumnDomain(rangeColumnName);
					if (! rangeSet.contains(externalTableRangeIdto)){
						// El rango de la columna no es de la familia de la clase abstracta que relaciona la vista asociacion.
						continue;
					}
					sql = getQueryForAssociationViewFromAssociationTable(domainIdto, assocView, idProperty, rangeTable, rangeColumnName, externalTableRangeIdto);
				}else{
					// La informacion de la propiedad esta en la tabla del rango.
					if (!rangeSet.contains(rangeTable.getId())){
						// No es una relacion con una de las clases que nos interesa.
						continue;
					}
					sql = getQueryForAssociationViewFromRangeTable(domainIdto, rangeTable, assocView, idProperty);
					if(sql==null) continue;
				}
				assocView.setTableQuery(0, sql);
			}
			
		}
	}
	
	/**
	 * Añade a la vista de la dataproperty externalizada la consulta para sacar
	 * los datos de la tabla que representa a la clase indicada.
	 * 
	 * @param idto
	 *            Identificador de la clase a la que pertenece la DataProperty
	 * @param property
	 *            Objeto con toda la informacion de la propiedad externalizada.
	 * @param view
	 *            Vista en la que tenemos que poner los datos.
	 */
	private void addQueryForExternalizedDataProperty(int idto, PropertyInfo property, View view) {
		Table domainTable = tables.get(idto);
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		int idProp = property.getIdProp();
		ViewColumn[] viewDomainColumns = view.getObjectPropertyColumn(View.ID_DOMAIN, idto);
		ViewColumn viewPropertyColumn = view.getDataPropertyColumn(idProp);
		
		if (! domainTable.isExternalizedProperty(idProp)){
			// Esta en la tabla del dominio.
			TableColumn column = domainTable.getDataPropertyColumn(idProp);
			if (column == null){
				// Esta clase no tiene conocimiento de esta propiedad.
				return;
			}
			String columnName = column.getColumnName();
			Map<String, String> queryByColumnName = new Hashtable<String, String>();
			queryByColumnName.put(viewDomainColumns[0].getColumnName(), characterBegin + Table.COLUMN_NAME_TABLEID + characterEnd);
			if (viewDomainColumns[1].getColumnName() != null){
				queryByColumnName.put(viewDomainColumns[1].getColumnName(), String.valueOf(idto));
			}
			queryByColumnName.put(viewPropertyColumn.getColumnName(), characterBegin + columnName + characterEnd);
			
			String sql = "SELECT ";
			boolean first = true;
			for (String viewColumnName : view.getOrderedColumnNames()) {
				if (! first){
					sql += ", ";
				}
				sql += queryByColumnName.get(viewColumnName);
				first = false;
			}
			sql += " FROM " + characterBegin + domainTable.getName() + characterEnd;
			view.setTableQuery(0, sql);
		}else{
			// Esta en una tabla auxiliar.
			Integer propertyLocation = domainTable.getExternalizedPropertyLocations(idProp).iterator().next();
			Table externalTable = tables.get(propertyLocation);
			TableColumn[] domainColumns = externalTable.getObjectPropertyColumn(IQueryInfo.ID_DOMAIN, domainTable.getId());
			TableColumn propertyColumn = externalTable.getDataPropertyColumn(idProp);
			String domainColumnName = domainColumns[0].getColumnName();
			String propertyColumnName = propertyColumn.getColumnName();
			
			Map<String, String> queryByColumnName = new Hashtable<String, String>();
			queryByColumnName.put(viewDomainColumns[0].getColumnName(), characterBegin + domainColumnName + characterEnd);
			if (viewDomainColumns[1].getColumnName() != null){
				queryByColumnName.put(viewDomainColumns[1].getColumnName(), String.valueOf(idto));
			}
			queryByColumnName.put(viewPropertyColumn.getColumnName(), characterBegin + propertyColumnName + characterEnd);
			
			String sql = "SELECT ";
			boolean first = true;
			for (String viewColumnName : view.getOrderedColumnNames()) {
				if (! first){
					sql += ", ";
				}
				sql += queryByColumnName.get(viewColumnName);
				first = false;
			}
			sql += " FROM " + characterBegin + externalTable.getName() + characterEnd;
			view.setTableQuery(0, sql);
		}
	}
	
	/**
	 * Construye la consulta para sacar los datos de una tabla del rango de una
	 * propiedad para poder trasladarlos a la vista asociacion que se nos
	 * indica.
	 * 
	 * @param domainIdto
	 *            Identificador de la clase dominio de la propiedad que nos
	 *            interesa analizar.
	 * @param rangeTable
	 *            Tabla donde se encuentra la informacion sobre el vinculo entre
	 *            las dos clases mediante la propiedad.
	 * @param assocView
	 *            Vista en la que se tienen que representar los datos de la
	 *            asociacion.
	 * @param idProperty
	 *            Identificador de la propiedad que nos interesa analizar y de
	 *            la que queremos trasladar la informacion a la vista
	 *            asociacion.
	 * @return Consulta SQL que saca los datos de la tabla del rango y nos
	 *         permite introducirlos en la vista asociacion que se nos ha
	 *         indicado.
	 * @throws NoSuchColumnException
	 *             Si se le pregunta a una tabla por una columna que no tiene.
	 */
	private String getQueryForAssociationViewFromRangeTable(int domainIdto, Table rangeTable, View assocView, int idProperty) throws NoSuchColumnException{
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		// Buscamos la columna que apunta al dominio que se nos ha indicado.
		TableColumn[] domainTableColumns =  rangeTable.getObjectPropertyColumn(idProperty, domainIdto);
		String domainColumnName = domainTableColumns[0].getColumnName();
		
		ViewColumn[] domainColumns = assocView.getObjectPropertyColumn(View.ID_DOMAIN, domainIdto);
		ViewColumn[] rangeColumns = assocView.getObjectPropertyColumn(idProperty, rangeTable.getId());
		if (rangeColumns == null){
			System.err.println("View=" + assocView.getName());
			System.err.println("PROPERTY=" + idProperty + "; RANGE=" + rangeTable.getId());
			return null;
		}
		ViewColumn propertyColumn = assocView.getDataPropertyColumn(View.ID_PROPERTY);
		Map<String, String> queryByColumnName = new Hashtable<String, String>();
		queryByColumnName.put(domainColumns[0].getColumnName(), characterBegin + domainColumnName + characterEnd);
		if (domainColumns[1] != null){
			queryByColumnName.put(domainColumns[1].getColumnName(), String.valueOf(domainIdto));
		}
		queryByColumnName.put(rangeColumns[0].getColumnName(), characterBegin + Table.COLUMN_NAME_TABLEID + characterEnd);
		if (rangeColumns[1] != null){
			queryByColumnName.put(rangeColumns[1].getColumnName(), String.valueOf(rangeTable.getId()));
		}
		if (propertyColumn != null){
			queryByColumnName.put(propertyColumn.getColumnName(), String.valueOf(idProperty));
		}
		// Construimos la consulta
		String sql = "SELECT ";
		boolean first = true;
		for (String viewColumnName : assocView.getOrderedColumnNames()) {
			if (! first){
				sql += ", ";
			}
			sql += queryByColumnName.get(viewColumnName);
			first = false;
		}
		sql += " FROM " + characterBegin + rangeTable.getName() + characterEnd + " WHERE " + characterBegin + domainColumnName + characterEnd + " IS NOT NULL";
		return sql;
	}

	/**
	 * Construye la sentencia SQL que consulta los datos de la tabla asocacion
	 * dada para incluirlos en la vista correspondiente.
	 * 
	 * @param domainIdto
	 *            Identificador de la clase dominio de la propiedad que une las
	 *            dos clases.
	 * @param assocView
	 *            Vista en la que se tienen que representar los datos de la
	 *            tabla asociacion.
	 * @param idProperty
	 *            Identificador de la propiedad por la que se relacionan las dos
	 *            clases.
	 * @param assocTable
	 *            Tabla asociacion de la que tenemos que sacar los datos.
	 * @param rangeColumnName
	 *            Columna en la que se encuentra el identificador de los objetos
	 *            del rango de la propiedad.
	 * @param rangeIdto
	 *            Identificador del rango de la propiedad que nos interesa
	 *            mostrar en la vista asociacion.
	 * @return Consulta SQL que saca los datos de la tabla asociacion para que
	 *         puedan mostrarse correctamente en la vista asociacion.
	 * @throws NoSuchColumnException
	 *             Si se le pregunta a una tabla por una columna que no
	 *             contiene.
	 */
	private String getQueryForAssociationViewFromAssociationTable(int domainIdto, View assocView, int idProperty, Table assocTable, String rangeColumnName, Integer rangeIdto) throws NoSuchColumnException {
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		String domainColumnName = tables.get(domainIdto).getName() + IQueryInfo.SUFFIX_ID;
		// Obtenemos las columnas de la vista a las que les vamos a tener que
		// dar valor.
		ViewColumn [] domainColumns = assocView.getObjectPropertyColumn(View.ID_DOMAIN, domainIdto);
		ViewColumn [] rangeColumns = assocView.getObjectPropertyColumn(idProperty, rangeIdto);
		ViewColumn propertyColumn = assocView.getDataPropertyColumn(View.ID_PROPERTY);
		// Mapa donde se van a ir almacenando como dar valor a cada una de las
		// columnas de la vista, ordenadas por el nombre de la columna de la
		// vista donde tienen que terminar los datos.
		Map<String, String> queryByColumnName = new Hashtable<String, String>();
		queryByColumnName.put(domainColumns[0].getColumnName(), characterBegin + domainColumnName + characterEnd);
		if (domainColumns[1] != null){
			queryByColumnName.put(domainColumns[1].getColumnName(), String.valueOf(domainIdto));
		}
		queryByColumnName.put(rangeColumns[0].getColumnName(), characterBegin + rangeColumnName + characterEnd);
		if (rangeColumns[1] != null){
			queryByColumnName.put(rangeColumns[1].getColumnName(), rangeIdto.toString());
		}
		if (propertyColumn != null){
			queryByColumnName.put(propertyColumn.getColumnName(), String.valueOf(idProperty));
		}
		// Construimos la consulta. Como se puede observar no se hace la
		// comprobacion de no nulo porque, en teoria, las tablas asociacion no
		// tienen que contener ningun vinculo incompleto.
		String sql = "SELECT ";
		boolean first = true;
		for (String viewColumnName : assocView.getOrderedColumnNames()) {
			if (! first){
				sql += ", ";
			}
			sql += queryByColumnName.get(viewColumnName);
			first = false;
		}
		sql += " FROM " + characterBegin + assocTable.getName() + characterEnd;
		return sql;
	}

	/**
	 * Construye una consulta sql que permite sacar los datos de la propiedad
	 * indicada que relaciona dos clases distintas, de manera que pueda ser
	 * añadida como consulta a la vista asociacion dada.
	 * 
	 * @param assocView
	 *            Vista asociacion donde se tienen que representar los datos de
	 *            la propiedad.
	 * @param domainTable
	 *            Tabla del dominio de la propiedad.
	 * @param idProperty
	 *            Identificador de la propiedad que se tiene que representar en
	 *            la vista.
	 * @param columnName
	 *            Nombre de la columna de la tabla dominio de la que tenemos que
	 *            sacar los datos de la propiedad. Esta columna lo que contendra
	 *            sera el tableId del objeto apuntado.
	 * @param columnDomain
	 *            Identificador de la clase a la que pertenecen los objetos de
	 *            los que almacena los identificadores la columna.
	 * @return Sentencia SQL que sera de la forma:<br>
	 *         <code>SELECT domainTableId, [domainIdto,] rangeTableId, [rangeIdto,] [idProperty] FROM domainTableName WHERE rangeTableId IS NOT NULL</code>
	 */
	private String getQueryForAssociationViewFromDomainTable(View assocView, Table domainTable, int idProperty, String columnName, Integer columnDomain) {
		int domainIdto = domainTable.getId();
		List<String> assocViewColumns = assocView.getOrderedColumnNames();
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String characterBegin = generateSQL.getCharacterBegin();
		String characterEnd = generateSQL.getCharacterEnd();
		Map<String, String> queryByColumnName = new Hashtable<String, String>();
		ViewColumn [] domainColumns = assocView.getObjectPropertyColumn(View.ID_DOMAIN, domainIdto);
		ViewColumn [] rangeColumns = assocView.getObjectPropertyColumn(idProperty, columnDomain);
		// El dominio lo sacamos del tableId de la tabla dominio y
		// el idto lo sabemos porque sabemos el idto de la tabla
		// dominio
		queryByColumnName.put(domainColumns[0].getColumnName(), characterBegin + Table.COLUMN_NAME_TABLEID + characterEnd);
		if (domainColumns[1] != null){
			queryByColumnName.put(domainColumns[1].getColumnName(), String.valueOf(domainIdto));
		}
		// Para el rango, el tableId es el contenido de la columna que estamos tratando
		queryByColumnName.put(rangeColumns[0].getColumnName(), characterBegin + columnName + characterEnd);
		if (rangeColumns[1] != null){
			queryByColumnName.put(rangeColumns[1].getColumnName(), columnDomain.toString());
		}
		// Consultamos si exite la columna para indicar la propiedad en la vista asociacion y la rellenamos si es asi.
		ViewColumn propertyColumn = assocView.getDataPropertyColumn(View.ID_PROPERTY);
		if (propertyColumn != null){
			queryByColumnName.put(propertyColumn.getColumnName(), String.valueOf(idProperty));
		}
		
		String sql = "SELECT ";
		boolean first = true;
		for (String viewColumnName : assocViewColumns) {
			if (! first){
				sql += ", ";
			}
			sql += queryByColumnName.get(viewColumnName);
			first = false;
		}
		sql += " FROM " + characterBegin + domainTable.getName() + characterEnd + " WHERE " + characterBegin + columnName + characterEnd + " IS NOT NULL";
		return sql;
	}
}
