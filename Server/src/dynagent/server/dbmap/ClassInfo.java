package dynagent.server.dbmap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dynagent.common.Constants;

/**
 * Almacena toda la informacion referente a una clase obtenida de la base de datos.
 */
public class ClassInfo implements Serializable{

	private static final long serialVersionUID = 2698594543044413716L;
	/** Nombre de la clase */
	private String name;
	/** Identificador de la clase */
	private int idto;
	/** Mapa de la informacion de las propiedades ordenadas por su identificador */
	private Hashtable<Integer, PropertyInfo> properties;
	/** Mapa de las propiedades que apuntan a clases, ordenadas por la clase a la que apunta */
	private Hashtable<Integer, HashSet<Integer>> propertiesReferencingClasses;
	/** Conjunto de las clases padres de la actual */
	private Set<Integer> parentClasses;
	/** Conjunto de las clases hijas de la actual */
	private Set<Integer> childClasses;
	/** Indica si la tabla actual es abstracta o no */
	private boolean isAbstractClass;
	/** Lista de los padres inmediatos de esta clase */
	private List<Integer> inmediateParents;
	/** Lista de los hijos inmediatos de esta clase */
	private List<Integer> inmediateChildren;
	/** Lista de las propiedades que son de tipo fichero o imagen. */
	private Set<Integer> fileProperties;
	
	public ClassInfo(String name, int idto) {
		this.name = name;
		this.idto = idto;
		this.properties = new Hashtable<Integer, PropertyInfo>();
		this.propertiesReferencingClasses = new Hashtable<Integer, HashSet<Integer>>();
		this.parentClasses = new HashSet<Integer>();
		this.childClasses = new HashSet<Integer>();
		this.isAbstractClass = false;
		this.inmediateChildren = new LinkedList<Integer>();
		this.fileProperties = new HashSet<Integer>();
	}

	/**
	 * añade todas las propiedades de la lista pasada a la clase.
	 * 
	 * @param listOfProperties
	 *            Lista de propiedades a añadir. Si alguna de las propiedades ya existia, se modifica la existente con
	 *            los datos de la pasada por parametros.
	 */
	public void addAllProperties(List<PropertyInfo> listOfProperties) {
		for (PropertyInfo propertyInfo : listOfProperties) {
			addProperty(propertyInfo);
		}
	}

	/**
	 * añade una clase padre a la clase actual.
	 * 
	 * @param idto
	 *            Identificador de la clase padre.
	 */
	public void addParentClass(int idto) {
		if (idto == this.idto){
			return;
		}
		parentClasses.add(idto);
	}

	/**
	 * añade una propiedad a la clase.
	 * 
	 * @param propertyInfo
	 *            Informacion de la propiedad a añadir. Si la propiedad ya existia, modifica los datos de la ya
	 *            existente.
	 */
	public void addProperty(PropertyInfo propertyInfo) {
		PropertyInfo storedPropertyInfo;
		if ((storedPropertyInfo = properties.get(propertyInfo.getIdProp())) != null) {
			storedPropertyInfo.setMaxCardinality(propertyInfo.getMaxCardinality());
			storedPropertyInfo.setMinCardinality(propertyInfo.getMinCardinality());
			storedPropertyInfo.addAllPropertyTypes(propertyInfo.getPropertyTypes());
		} else {
			properties.put(propertyInfo.getIdProp(), propertyInfo);
		}
		boolean isBasicType = true;
		for (Integer type : propertyInfo.getPropertyTypes()) {
			isBasicType = isBasicType && Constants.isDataType(type);
			if (type.equals(Constants.IDTO_FILE) || type.equals(Constants.IDTO_IMAGE)){
				fileProperties.add(propertyInfo.getIdProp());
			}
		}
		// Si la propiedad no es de tipo basico, añadimos la referencia a la lista de clases referenciadas.
		if (!isBasicType) {
			for (Integer referencedClass : propertyInfo.getPropertyTypes()) {
				HashSet<Integer> properties = propertiesReferencingClasses.get(referencedClass);
				if (properties == null) {
					properties = new HashSet<Integer>();
					propertiesReferencingClasses.put(referencedClass, properties);
				}
				properties.add(propertyInfo.getIdProp());
			}
		}
	}

	/**
	 * Devuelve el identificador numero de esta clase.
	 * 
	 * @return número que identifica a esta clase.
	 */
	public int getIdto() {
		return idto;
	}

	/**
	 * Devuelve los padres de los que esta clase es hija inmediata, es decir, no hay ninguna clase entre las dos en la
	 * jerarquia.
	 * 
	 * @return Lista con los padres mas inmediatos. Si no tiene padres devuelve una lista vacia.
	 */
	public List<Integer> getInmediateParents() {
		return inmediateParents;
	}

	/**
	 * Cambia la lista de padres inmediatos definida para esta clase, si todavia no existia ninguna lista de padres
	 * inmediatos, establece la pasada como valor de dicha lista.
	 * 
	 * @param inmediateParents
	 *            Lista de los idtos de los padres inmediatos.
	 */
	public void setInmediateParents(List<Integer> inmediateParents) {
		this.inmediateParents = inmediateParents;
	}

	/**
	 * Devuelve la lista de los hijos más inmediatos.
	 * 
	 * @return Lista de los hijos más inmediatos. Nunca puede ser <code>null</code>.
	 */
	public List<Integer> getInmediateChildren() {
		return inmediateChildren;
	}

	/**
	 * añade un hijo a la lista de hijos inmediatos de esta clase.
	 * 
	 * @param inmediateChild
	 *            Si el elemento es nulo o ya está contenido en la lista de hijos inmediatos, no hace nada.
	 */
	public void addInmediateChild(Integer inmediateChild) {
		if (inmediateChild == null || inmediateChildren.contains(inmediateChild)) {
			return;
		}
		inmediateChildren.add(inmediateChild);
	}

	/**
	 * Devuelve el nombre de esta clase.
	 * 
	 * @return Nombre de la clase.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Devuelve el conjunto de las clases padre de esta clase todos los niveles.
	 * 
	 * @return Conjunto de clases padre. <b>No modificar nunca este conjunto</b> pues se alteraria la informacion de la
	 *         clase y podria quedar incoherente.
	 */
	public Set<Integer> getParentClasses() {
		return parentClasses;
	}

	/**
	 * Devuelve el conjunto de las propiedades que referencian a la clase especificada.
	 * 
	 * @param idto
	 *            Identificador de la clase por la que se está preguntando.
	 * @return Conjunto de identificadores de las propiedades que apuntan a dicha clase. Devuelve <code>null</code> si
	 *         la clase especificada no está referenciada por esta clase.
	 */
	public Set<Integer> getPropertiesReferencingClass(int idto) {
		return propertiesReferencingClasses.get(idto);
	}

	/**
	 * Devuelve toda la informacion referente a una propiedad perteneciente a esta clase.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que se quieren consultar los datos.
	 * @return <code>null</code> si la propiedad no existe en esta clase.
	 */
	public PropertyInfo getProperty(int idProperty) {
		return properties.get(idProperty);
	}

	/**
	 * Devuelve el conjunto de las clases que son referenciadas por esta clase.
	 * 
	 * @return Si no hay clases referencidas, el conjunto estara vacio.
	 */
	public Set<Integer> getReferencedClasses() {
		return propertiesReferencingClasses.keySet();
	}

	/**
	 * Consulta si esta clase tiene una determinada propiedad
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad consultar.
	 * @return <code>true</code> si esta clase posee dicha propiedad.
	 */
	public boolean hasProperty(int idProperty) {
		return properties.containsKey(idProperty);
	}

	/**
	 * Consulta si una clase es referenciada por esta clase.
	 * 
	 * @param idto
	 *            Identificador de la clase de la que queremos saber si es referenciada.
	 * @return <code>true</code> si la clase identificada por el parámetro es referenciada por esta clase.
	 */
	public boolean referencesClass(int idto) {
		return propertiesReferencingClasses.keySet().contains(idto);
	}

	/**
	 * Consulta si esta clase es abstracta.
	 * 
	 * @return <code>true</code> si la clase es abstracta.
	 */
	public boolean isAbstractClass() {
		return isAbstractClass;
	}

	/**
	 * Establece el valor de isAbstractClass para indicarle a la clase si es abstracta o no.
	 * 
	 * @param isAbstractClass
	 *            <code>true</code> si se trata de una clase abstracta.
	 */
	public void setIsAbstractClass(boolean isAbstractClass) {
		this.isAbstractClass = isAbstractClass;
	}

	/**
	 * añade una clase escpecializada de esta.
	 * 
	 * @param childClassIdto
	 *            Identificador numero de la clase hija.
	 */
	public void addChildClass(int childClassIdto) {
		if (childClassIdto==idto){
			return;
		}
		this.childClasses.add(childClassIdto);
	}

	/**
	 * añade todos las clases indicadas como especilizadas de la actual.
	 * 
	 * @param childClasses
	 *            Conjunto de las clases especializadas de la actual referenciadas por su identificador numero.
	 */
	public void addAllChildClasses(Set<Integer> childClasses) {
		for (Integer childClassIdto : childClasses) {
			addChildClass(childClassIdto);
		}
	}

	/**
	 * Devuelve el conjunto de todas las clases especializadas de la actual.
	 * 
	 * @return
	 */
	public Set<Integer> getChildClasses() {
		return this.childClasses;
	}

	public List<PropertyInfo> getAllProperties() {
		List<PropertyInfo> result = new LinkedList<PropertyInfo>();
		result.addAll(properties.values());
		return result;
	}

	/**
	 * Consulta el conjunto de propiedades de tipo fichero
	 * 
	 * @return Devuelve el conjunto de las propiedades de tipo
	 *         {@link Constants#IDTO_FILE} o {@link Constants#IDTO_IMAGE}<br>
	 *         Nunca devuelve <code>null</code>. Si no hay propiedades de dichos
	 *         tipos, devuelve un conjunto vacio.
	 */
	public Set<Integer> getFileProperties() {
		return fileProperties;
	}

	public String toString() {
		String result = name + "[isAbstract=" + isAbstractClass + ", numberOfChildren=" + childClasses.size()
				+ ", properties={";
		boolean first = true;
		for (PropertyInfo propertyInfo : properties.values()) {
			if (first) {
				result += "'" + propertyInfo.getName() + "'";
				first = false;
			} else {
				result += ", '" + propertyInfo.getName() + "'";
			}
		}
		result += "]";
		return result;
	}
}
