package dynagent.server.services;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Clase que guarda toda la informacion de los vinculos que existen con un
 * objeto determinado en base de datos.
 */
public class LinkedObject {
	
	/** Identificador de la clase a la que pertenece el objeto del que se guarda informacion. */
	private int idto;
	/** Identificador del objeto del que se guarda informacion. */
	private int tableId;
	/** Lista de todos los vinculos de este objeto de los que se tiene conocimiento. */
	private List<ObjectLink> links;
	/** vinculos ordenados por la propiedad de a la que pertenecen. */
	private Map<Integer, List<ObjectLink>> linksByProperty;

	/**
	 * Crea un objeto que guardara informacion de los vinculos que existen en
	 * este objeto.
	 * 
	 * @param idto
	 *            Identificador de la clase a la que pertenece el objeto.
	 */
	public LinkedObject(int idto) {
		this.idto = idto;
		tableId = -1;
		links = new LinkedList<ObjectLink>();
		linksByProperty = new Hashtable<Integer, List<ObjectLink>>();
	}

	/**
	 * Devuelve una lista de los vinculos de los que este objeto es el rango.
	 * 
	 * @return Devuelve la lista de los vinculos hacia este objeto.<br>
	 *         Nunca devuelve <code>null</code>.
	 */
	public List<ObjectLink> getLinksToThisObject() {
		List<ObjectLink> result = new LinkedList<ObjectLink>();
		for (ObjectLink link : links) {
			if (link.getRange() == idto && link.getRangeId() == tableId){
				result.add(link);
			}
		}
		return result;
	}

	/**
	 * Devuelve los vinculos de los que este objeto es el dominio.
	 * 
	 * @return Devuelve la lista de los vinculos que surgen de este objeto.<br>
	 *         Nunca devuelve <code>null</code>.
	 */
	public List<ObjectLink> getLinksFromThisObject() {
		List<ObjectLink> result = new LinkedList<ObjectLink>();
		for (ObjectLink link : links) {
			if (link.getDomain() == idto && link.getDomainId() == tableId){
				result.add(link);
			}
		}
		return result;
	}

	/**
	 * Devuelve el identificador de la clase a la que pertenece este objeto.
	 * 
	 * @return Identificador numero de la clase.
	 */
	public int getIdto() {
		return idto;
	}

	/**
	 * Devuelve el identificador del objeto dentro de la clase.
	 * 
	 * @return Identificador numerico del objeto dentro de la clase.<br>
	 *         Devuelve <code>-1</code> si no se ha asignado un tableId.
	 */
	public int getTableId() {
		return tableId;
	}

	/**
	 * añade un vinculo a este objeto.
	 * 
	 * @param link
	 *            vinculo que queremos añadir al objeto.
	 */
	public void addLink(ObjectLink link) {
		links.add(link);
		List<ObjectLink> linkList = linksByProperty.get(link.getIdProperty());
		if (linkList == null){
			linkList = new LinkedList<ObjectLink>();
			linksByProperty.put(link.getIdProperty(), linkList);
		}
		linkList.add(link);
	}

	/**
	 * Especifica el identificador del objeto dentro de la clase del que guarda
	 * informacion este objeto.
	 * 
	 * @param tableId
	 *            Identificador del objeto.
	 */
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	/**
	 * Comprueba si existe un vinculo inverso al dado por la propiedad inversa.
	 * 
	 * @param link
	 *            vinculo del que queremos saber si hay un vinculo inverso.
	 * @param inverseProperty
	 *            Propiedad del vinculo inverso.
	 */
	public boolean hasInverseLink(ObjectLink link, Integer inverseProperty) {
		boolean result = false;
		List<ObjectLink> inversePropertyLinks = linksByProperty.get(inverseProperty);
		if (inversePropertyLinks != null){
			for (ObjectLink inverseLink : inversePropertyLinks) {
				if (inverseLink.getDomain() == link.getRange() 
						&& inverseLink.getDomainId() == link.getRangeId() 
						&& inverseLink.getRange() == link.getDomain() 
						&& inverseLink.getRangeId() == link.getDomainId()){
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Borra el vinculo de las referencias de este objeto.
	 * 
	 * @param link
	 *            vinculo a borrar.
	 */
	public void removeLink(ObjectLink link) {
		this.links.remove(link);
		List<ObjectLink> links = linksByProperty.get(link.getIdProperty());
		links.remove(link);
	}
	
	public boolean equals (Object obj){
		boolean result = obj != null && obj instanceof LinkedObject;
		if (result){
			LinkedObject linkedObject = (LinkedObject) obj;
			result = linkedObject.idto == this.idto && linkedObject.tableId == this.tableId;
		}
		return result;
	}
	
	public int hashCode(){
		String id = idto + "#" + tableId;
		return id.hashCode();
	}
	
	public String toString(){
		return "IDTO=" + idto + "; TABLE_ID=" + tableId + "; LINKS=" + links;
	}

}
