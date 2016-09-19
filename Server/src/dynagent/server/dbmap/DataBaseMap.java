package dynagent.server.dbmap;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.PropertyUnit;
import dynagent.server.dbmap.builders.ClassBuilder;
import dynagent.server.dbmap.builders.TableBuilder;
import dynagent.server.dbmap.builders.ViewBuilder;
import dynagent.server.ejb.FactoryConnectionDB;

/**
 * Clase que se encarga de mapear las tablas de datos de la base de datos en
 * JAVA de tal manera que es facil de consultar el modelo y se pueden conocer
 * las propiedades del mismo en tiempo de ejecucion.
 */
public class DataBaseMap implements Serializable{

	private static final long serialVersionUID = 6313820343874132926L;

	/** Mapa de todas las tablas creadas indexadas por su identificador */
	private Map<Integer, Table> tables;
	/** Mapa de todas las clases encontradas indexadas por su idto */
	private Map<Integer, ClassInfo> classesByIdto;
	/** Mapa de las clases por su nombre */
	private Map<String, ClassInfo> classesByName;
	/** Objeto que nos da acceso a la base de datos. */
	private FactoryConnectionDB fcdb;
	/**
	 * Conjunto de los identificadores de todas las propiedades que son
	 * estructurales.
	 */
	private Set<Integer> structuralProperties;
	/** Mapa de las propiedades estructurales que son compartidas. */
	private Set<Integer> sharedProperties;
	/**
	 * Mapa de Las propiedades que son inversas, con su propiedad inversa como
	 * valor.
	 */
	private Map<Integer, Integer> inverseProperties;
	/**
	 * Mapa de los identificadores de las propiedades indexandas por sus nombres
	 */
	private Map<String, Integer> propertiesByName;
	/**
	 * Mapa de los nombres de las propiedades indexados por los identificadores.
	 */
	private Map<Integer, String> propertiesById;

	/** Identificador de la primera tabla de asociacion */
	private int firstAssocTable;
	/**
	 * índice que se le tendria que asignar a la siguiente tabla asociacion que
	 * se va a crear.
	 */
	private int nextAssocTableId;
	/** Indica si se ha producido algun error durante el mapeo de datos. */
	private boolean error;
	/**
	 * Le indica a la clase si se está ejecutando el código durante una
	 * importacion o durante una ejecucion normal del servidor.
	 */
	private boolean importing;
	/**
	 * Mapa de las vistas por la clase a la que representan. Para las vistas
	 * asociacion se crearan idtos ficticios como se hace con las tablas
	 * asociacion.
	 */
	private Map<Integer, View> views;
	/** Mapa del conjunto de clases que apuntan a una determinada clase. */
	private Map<Integer, Set<ClassInfo>> classesReferencingClass;
	/** Constructor de las vistas. */
	private ViewBuilder viewBuilder;
	/** Constructor de tablas */
	private TableBuilder tableBuilder;
	/**
	 * Indica si se está en modo de pruebas, lo que implica una serie de
	 * restricciones extra, como los NOT NULL en ciertas columnas de las tablas
	 */
	private final static boolean debugMode = false;
	
	public ArrayList<PropertyUnit> propertyUnits;

	public DataBaseMap(FactoryConnectionDB fcdb, boolean importing) {
		this.importing = importing;
		if (fcdb == null) {
			error = true;
		} else {
			error = false;
			this.fcdb = fcdb;
			init();
			createTables();
			createViews();
		}
	}

	/**
	 * Construye todas las tablas que se han creado a partir del modelo. Si no
	 * se está importando o se ha producido algun error durante el mapeo no se
	 * llevara a cabo ninguna acción.
	 * @param excludedIdtos TODO
	 * @param restriction TODO
	 * 
	 * @return <code>false</code> si no se han creado todas las tablas por
	 *         cualquier motivo.
	 */
	public boolean constructAllTables(Set<Integer> excludedIdtos, boolean restriction) {
		boolean success = tableBuilder.constructTablesOnDB(excludedIdtos, restriction);
		if(restriction)//Si restriction fuera false no deberiamos liberar tableBuilder para poder llamar despues a putRestrictions
			tableBuilder = null;
		return success;
	}

	/**
	 * Se encarga de crear las vistas en la base de datos.<br>
	 * solo se ejecutara si se está importando, en caso contrario se ignorara la
	 * llamada a este metodo.
	 */
	public void constructAllViews(Set<Integer> excludedIdtos){
		if (!importing || error){
			System.out.println("[DATABASEMAP INFO] No se pueden construir las vistas porque no se está importando el modelo.");
			return;
		}
		
		viewBuilder.constructViewsOnDB(excludedIdtos);
		viewBuilder = null;
	}

	/**
	 * Devuelve el conjunto de todas las tablas que se han creado.
	 * 
	 * @return Conjunto con todas las tablas existentes.
	 */
	public Set<Table> getAllTables() {
		Set<Table> tables = new HashSet<Table>();
		tables.addAll(this.tables.values());
		return tables;
	}

	/**
	 * Consulta el identificador numero de una propiedad.
	 * 
	 * @param propertyName
	 *            Nombre de la propiedad.
	 * @return Identificador numero de la propiedad o <code>null</code> si no
	 *         se tiene un identificador asociado al nombre indicado.
	 */
	public Integer getPropertyId(String propertyName) {
		return propertiesByName.get(propertyName);
	}

	/**
	 * Consulta el nombre de una propiedad.
	 * 
	 * @param idProperty
	 *            Identificador numero de la propiedad.
	 * @return Nombre de la propiedad o <code>null</code> si no se tiene ningun
	 *         nombre asociado al identificador dado.
	 */
	public String getPropertyName(Integer idProperty) {
		return propertiesById.get(idProperty);
	}

	/**
	 * Devuelve la tabla con el identificador indicado.
	 * 
	 * @param tableIdto
	 *            Identificador de la tabla. Si se trata de una tabla que
	 *            representa a una clase, sera el mismo que el de dicha clase.
	 * @return Devuelve la tabla o <code>null</code> si no se tiene ninguna
	 *         tabla con el identificador indicado.
	 */
	public Table getTable(int tableIdto) {
		return tables.get(tableIdto);
	}

	/**
	 * Devuelve el conjunto de todas las clases del modelo que se han mapeado
	 * 
	 * @return Conjunto de todas las clases mapeadas, en ningun caso puede ser
	 *         nulo, pero si puede estar vacio.
	 */
	public Set<ClassInfo> getAllClasses() {
		Set<ClassInfo> result = new HashSet<ClassInfo>();
		result.addAll(classesByIdto.values());
		return result;
	}

	/**
	 * Devuelve la clase con el identificador indicado.
	 * 
	 * @param classIdto
	 *            Identificador de la clase.
	 * @return Devuelve la imformacion de la clase o <code>null</code> si no se
	 *         tiene ninguna clase para dicho idto.
	 */
	public ClassInfo getClass(int classIdto) {
		return classesByIdto.get(classIdto);
	}

	/**
	 * Busca una clase por su nombre.
	 * 
	 * @param className
	 *            Nombre de la clase que se está buscando.
	 * @return Informacion de la clase buscada o <code>null</code> si no se ha
	 *         encontrado una clase que concuerde con el nombre dado.
	 */
	public ClassInfo getClass(String className) {
		return classesByName.get(className);
	}

	/**
	 * Devuelve el conjunto de clases que están apuntando a la clase del idto
	 * indicado.
	 * 
	 * @param idto
	 *            Identificador de la clase de la que queremos saber quien la
	 *            apunta.
	 * @return devolvera <code>null</code> si nadie apunta a dicha clase o si el
	 *         idto indicado no se corresponde con ninguna clase.
	 */
	public Set<ClassInfo> getClassesReferencingClass(int idto){
		return classesReferencingClass.get(idto);
	}
	/**
	 * funcion que se va a encargar de iniciar todos los elementos necesarios
	 * para la creación de tablas de forma segura, comprobando que no hay
	 * errores en los datos obtenidos de base de datos
	 */
	private void init() {
		tables = new Hashtable<Integer, Table>();
		// Se construye el mapa de todas las clases. Si se da algun error lo
		// notificamos y no continuamos pues no tiene
		// sentido trabajar con datos erroneos.
		ClassBuilder classBuilder = new ClassBuilder(fcdb);
		try {
			classesByIdto = classBuilder.buildClasses();
			classesByName = indexClassesByName();
			structuralProperties = DBQueries.getStructuralProperties(fcdb);
			sharedProperties = DBQueries.getSharedProperties(fcdb);
			inverseProperties = DBQueries.getInversePropertiesMap(fcdb);
			propertiesById = DBQueries.getPropertiesNames(fcdb);
			propertiesByName = new Hashtable<String, Integer>();
			new HashSet<Integer>();
			classesReferencingClass = new Hashtable<Integer, Set<ClassInfo>>();
			for (Integer idProperty : propertiesById.keySet()) {
				propertiesByName.put(propertiesById.get(idProperty), idProperty);
			}

		} catch (SQLException e) {
			error = true;
			e.printStackTrace();
		} catch (NamingException e) {
			error = true;
			e.printStackTrace();
		} catch (UnrecognizedClass e) {
			error = true;
			e.printStackTrace();
		} finally {
			if (error) {
				return;
			}
		}

		// Lo inicializmos al índice de l primera tabla asociacion aunque de
		// momento no haya ninguna, pues es más facil
		// hacerlo asi que tener que comprobar en la creación de cada tabla
		// asociacion si ya se le habia dado valor a
		// esta variable.
		initializeReferencedClassesMap();
		firstAssocTable = findLastExistingClass() + 1;
		nextAssocTableId = firstAssocTable;
		tables = new Hashtable<Integer, Table>();
		new LinkedList<Object[]>();
		new Hashtable<AssocciationPair, Integer>();
		new Hashtable<AssocciationPair, Integer>();
		views = new Hashtable<Integer, View>();
		new Hashtable<Integer, List<View>>();
		
	}

	/**
	 * Rellena el mapa de las referencias a clases basandose en la informacion de 
	 */
	private void initializeReferencedClassesMap(){
//		long begin = System.currentTimeMillis();
		for (ClassInfo classInfo : classesByIdto.values()) {
			if (classInfo.isAbstractClass()){
				continue;
			}
			Set<ClassInfo> referencedClasses = new HashSet<ClassInfo>();
			// Acumulamos todas las clases apuntadas, más las hijas de estas.
			for (Integer referencedClassIdto : classInfo.getReferencedClasses()) {
				ClassInfo referencedClass = classesByIdto.get(referencedClassIdto);
				if (! referencedClass.isAbstractClass()){
					referencedClasses.add(referencedClass);
				}
				for (Integer referencedChildClassIdto : referencedClass.getChildClasses()) {
					//System.out.println("referencedChildClassIdto "+referencedChildClassIdto);
					ClassInfo childClass = classesByIdto.get(referencedChildClassIdto);
					//System.out.println("childClass "+childClass);
					if (! childClass.isAbstractClass()){
						referencedClasses.add(childClass);
					}
				}
			}
			// Una vez hemos rellenado con todos los datos el conjunto, tenemos
			// que iterar sobre el conjunto y montar la relacion inversa, es
			// decir, cada una de las clases del conjunto es apuntada por
			// classInfo.
			for (ClassInfo referencedClass : referencedClasses) {
				Set<ClassInfo> referencingClasses = classesReferencingClass.get(referencedClass.getIdto());
				if (referencingClasses == null){
					referencingClasses = new HashSet<ClassInfo>();
					classesReferencingClass.put(referencedClass.getIdto(), referencingClasses);
				}
				referencingClasses.add(classInfo);
			}
		}
//		long end = System.currentTimeMillis();
//		System.out.println("Se ha tardado: " + new Double((end-begin)/Constants.TIMEMILLIS) + " segundos en construir el mapa de las referencias a clases");
	}
	
	/**
	 * Crea el mapa de las clases indexandolas por su nombre.
	 * 
	 * @return Mapa de las clases indexadas por su nombre. Se basa en
	 *         classesByIdto, asi que si dicho mapa no está inicializado o es
	 *         nulo, devolvera un mapa vacio.
	 */
	private Map<String, ClassInfo> indexClassesByName() {
		Map<String, ClassInfo> result = new Hashtable<String, ClassInfo>();
		if (classesByIdto == null) {
			return result;
		}
		for (ClassInfo classInfo : classesByIdto.values()) {
			result.put(classInfo.getName(), classInfo);
		}
		return result;
	}

	/**
	 * Encuentra el idto máximo asignado a las clases que se han mapeado.
	 * 
	 * @return número más alto utilizdo como identificador en las clases
	 *         mapeadas.
	 */
	private int findLastExistingClass() {
		int result = Integer.MIN_VALUE;
		for (Integer integer : classesByIdto.keySet()) {
			if (result < integer) {
				result = integer;
			}
		}
		return result;
	}

	/**
	 * funcion que se va a encargar de crear el mapa de todas las tablas a
	 * partir de los datos obtenidos de las clases.<br>
	 * Si se ha encontrado algun error durante la inicializacion de todos las
	 * clases, no se realizara ningun error.
	 */
	private void createTables() {
		tableBuilder = new TableBuilder(fcdb, nextAssocTableId, importing, debugMode);
		tableBuilder.setClasses(classesByIdto);
		tableBuilder.setInverseProperties(inverseProperties);
		tableBuilder.setSharedProperties(sharedProperties);
		tableBuilder.setStructuralProperties(structuralProperties);
		error = tableBuilder.createTables();
		tables = tableBuilder.getTables();
		nextAssocTableId = tableBuilder.getNextId();
		if (! importing){
			// Liberamos la memoria que ocupase el constructor de tablas porque ya no se va a usar más.
			tableBuilder = null;
		}
	}

	/**
	 * Consulta si una propiedad es estructural
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad.
	 * @return <code>true</code> si la propiedad es estructural.
	 */
	public boolean isStructuralProperty(Integer idProperty) {
		return structuralProperties.contains(idProperty);
	}

	/**
	 * Indica si una propiedad es estructural compartida.
	 * 
	 * @param idProperty
	 *            Identifcador de la propiedad.
	 * @return <code>true</code> si la propiedad es estructural compartida.
	 */
	public boolean isSharedProperty(Integer idProperty) {
		return sharedProperties.contains(idProperty);
	}
	
	/**
	 * metodo que se encarga de crear las vistas de todas aquellas clases que
	 * sean abstractas. De ser necesario, tambien se crearan <i>vistas
	 * asociacion.</i>
	 */
	private void createViews(){
		viewBuilder = new ViewBuilder(fcdb, nextAssocTableId);
		viewBuilder.setClasses(classesByIdto);
		viewBuilder.setInverseProperties(inverseProperties);
		viewBuilder.setSharedProperties(sharedProperties);
		viewBuilder.setStructuralProperties(structuralProperties);
		viewBuilder.setTables(tables);
		viewBuilder.createViewsMap();
		views = viewBuilder.getViews();
		if (! importing){
			// Liberamos la memoria del constructor de vistas porque ya no se va a usar más.
			viewBuilder = null;
		}
	}

	/**
	 * Devuelve la propiedad inversa de la indicada si la tiene.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que se quiere saber la
	 *            inversa.
	 * @return devolvera el identificador de la propiedad inversa o
	 *         <code>null</code> si no tiene inversa.
	 */
	public Integer getInverseProperty(int idProperty) {
		return inverseProperties.get(idProperty);
	}
	
	/**
	 * Devuelve la vista con el identificador indicado.
	 * 
	 * @param viewId
	 *            Identificador de la vista que se quiere obtener.
	 * @return Devuelve la vista cuyo identificador es el indicado o
	 *         <code>null</code> si no existe ninguna vista con ese
	 *         identificador.
	 */
	public View getView(int viewId){
		return views.get(viewId);
	}

	public boolean isImporting() {
		return importing;
	}
	
	/**
	 * Se encarga de asignar las restricciones de foreing key sobre todas las tablas
	 */
	public void putRestrictions(){
		tableBuilder.putRestrictions();
	}

	public FactoryConnectionDB getFactoryConnectionDB() {
		return fcdb;
	}
	
	public ArrayList<PropertyUnit> buildUnits() throws SQLException, NamingException{
		propertyUnits=DBQueries.getPropertyUnits(fcdb);
		return propertyUnits;
	}
}
