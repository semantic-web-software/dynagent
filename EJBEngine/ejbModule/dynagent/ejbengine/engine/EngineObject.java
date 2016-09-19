package dynagent.ejbengine.engine;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EngineObject {

	/** Identificador del objeto. */
	private int id;
	/** Identificador de la clase del objeto. */
	private int idto;
	/** Mapa de las propiedades del objeto de las que se ha almacenado un valor. */
	private Map<Integer, List<String>> properties;
	/** Conjunto de los identificadores de propiedades que son ObjectProperty. */
	private Set<Integer> objectProperties;
	
	/**
	 * Constructor b�sico
	 * @param id Identificador del objeto.
	 * @param idto Identificador de la clase del objeto.
	 */
	public EngineObject(int id, int idto){
		this.id = id;
		this.idto = idto;
		objectProperties = new HashSet<Integer>();
		properties = new Hashtable<Integer, List<String>>();
	}

	/**
	 * Consulta el identificador del objeto.
	 * 
	 * @return Identificador del objeto del que se guarda informaci�n.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Consulta el identificador de la clase del objeto.
	 * 
	 * @return Identificador la clase del objeto del que se guarda informaci�n.
	 */
	public int getIdto() {
		return idto;
	}

	/**
	 * A�ade un valor a una propiedad del objeto.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad de la que se quiere indicar un
	 *            valor.
	 * @param value
	 *            Valor a a�adir.
	 */
	public void addPropertyValue(int idProperty, String value){
		List<String> values = properties.get(idProperty);
		if (values == null){
			values = new LinkedList<String>();
			properties.put(idProperty, values);
		}
		values.add(value);
	}
	
	/**
	 * Consulta las propiedades de las que se ha guardado informaci�n.
	 * @return Conjunto de los identificadores de las propiedades.
	 */
	public Set<Integer> getProperties(){
		return properties.keySet();
	}

	/**
	 * Consulta los valores almacenados para una propiedad.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad a consultar.
	 * @return Lista de los valores de la propiedad consultada. Devolver�
	 *         <code>null</code> si no se tiene informaci�n de dicha propiedad.
	 */
	public List<String> getPropertyValues(int idProperty){
		return properties.get(idProperty);
	}

	/**
	 * Indica que la propiedad indicada es ObjectProperty
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad.
	 */
	public void addObjectProperty(int idProperty) {
		objectProperties.add(idProperty);
	}
	
	/**
	 * Consulta si una propiedad es ObjectProperty.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad a consultar.
	 * @return Devuelve <code>true</code> si la propiedad indicada es
	 *         ObjectProperty.<br>
	 *         Que devuelva <code>false</code> no implica que sea DataProperty
	 *         ya que tambi�n devolver� <code>false</code> si se trata de una
	 *         propiedad de la que no se tiene conocimiento.
	 */
	public boolean isObjectProperty(int idProperty){
		return objectProperties.contains(idProperty);
	}
}
