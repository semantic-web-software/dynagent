package dynagent.server.dbmap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PropertyInfo implements Serializable{

	private static final long serialVersionUID = -5353048453382878912L;
	private String name;
	private int idProp; // Identificador de la propiedad.
	private int minCardinality;
	private int maxCardinality;
	private Set<Integer> propertyTypes; // Tipos a los que hace referencia la propiedad.

	public PropertyInfo(String name, int idProp) {
		this.name = name;
		this.idProp = idProp;
		this.minCardinality = 0;
		this.maxCardinality = Integer.MAX_VALUE;
		this.propertyTypes = new HashSet<Integer>();
	}

	public PropertyInfo(String name, int idProp, int minCardinality, int maxCardinality) {
		this.name = name;
		this.idProp = idProp;
		this.minCardinality = minCardinality;
		this.maxCardinality = maxCardinality;
		this.propertyTypes = new HashSet<Integer>();
	}

	/**
	 * Devuelve la mínima cardinalidad que puede tener esta propiedad para esta tabla.
	 * 
	 * @return the minCardinality
	 */
	public int getMinCardinality() {
		return minCardinality;
	}

	/**
	 * Establece la mínima cardinalidad que puede tener esta propiedad para esta tabla.
	 * 
	 * @param minCardinality
	 *            the minCardinality to set
	 */
	public void setMinCardinality(int minCardinality) {
		this.minCardinality = minCardinality;
	}

	/**
	 * Devuelve la máxima cardinalidad que puede tener esta propiedad para esta tabla.
	 * 
	 * @return the maxCardinality
	 */
	public int getMaxCardinality() {
		return maxCardinality;
	}

	/**
	 * Establece la máxima la cardinalidad que puede tenr esta propiedad para esta tabla.
	 * 
	 * @param maxCardinality
	 *            the maxCardinality to set
	 */
	public void setMaxCardinality(int maxCardinality) {
		this.maxCardinality = maxCardinality;
	}

	/**
	 * Devuelve el nombre de la propiedad
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Devuelve el número que identifica a la propiedad.
	 * 
	 * @return the idProp
	 */
	public int getIdProp() {
		return idProp;
	}

	/**
	 * @return the propertyType
	 */
	public Set<Integer> getPropertyTypes() {
		return propertyTypes;
	}

	/**
	 * @param propertyType
	 *            the propertyType to set
	 */
	public void addPropertyType(int propertyType) {
		propertyTypes.add(propertyType);
	}

	/**
	 * añade todos los tipos indicados.
	 * 
	 * @param propertyTypes
	 *            Tipos que se quieren añadir al rango de la propiedad.
	 */
	public void addAllPropertyTypes(Set<Integer> propertyTypes){
		for (Integer type : propertyTypes) {
			propertyTypes.add(type);
		}
	}

	public boolean equals(Object obj) {
		return (obj instanceof PropertyInfo) && (((PropertyInfo) obj).getName().equals(name))
				&& (((PropertyInfo) obj).getIdProp() == idProp);
	}
	
	public String toString(){
		return "[Name=" + name + ", idProp=" + idProp + "]";
	}
	
	public int hashCode(){
		return idProp;
	}
	
	public PropertyInfo clone(){
		PropertyInfo clon = new PropertyInfo(name, idProp, minCardinality, maxCardinality);
		clon.propertyTypes = new HashSet<Integer>();
		for (Integer propertyType : this.propertyTypes) {
			clon.propertyTypes.add(propertyType.intValue());
		}
		return clon;
	}

}
