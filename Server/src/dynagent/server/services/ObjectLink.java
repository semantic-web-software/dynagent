package dynagent.server.services;

/**
 * Clase para representar vinculos entre dos objetos en base de datos.
 */
public class ObjectLink{
	
	int domain, range, idProperty, tableIdto, domainId, rangeId;

	/**
	 * Construye un objeto que representa un vinculo entre dos objectos
	 * 
	 * @param domain
	 *            Idto de la clase del dominio de la relación.
	 * @param domainId
	 *            tableId del objeto objeto que está en el dominio del
	 *            vinculo.
	 * @param range
	 *            Idto de la clase a la que apunta la relación.
	 * @param rangeId
	 *            tableId del objeto apuntado por la propiedad.
	 * @param idProperty
	 *            Identificador de la propiedad que relaciona los dos
	 *            objetos.
	 * @param tableIdto
	 *            Identificador de la tabla donde se ve reflejada la
	 *            relación en base de datos.
	 */
	public ObjectLink(int domain, int domainId, int range, int rangeId, int idProperty, int tableIdto){
		this.domain = domain;
		this.range = range;
		this.idProperty = idProperty;
		this.tableIdto = tableIdto;
		this.domainId = domainId;
		this.rangeId = rangeId;
	}

	/**
	 * @return Devuelve el idto de la clase del dominio de la relación.
	 */
	public int getDomain() {
		return domain;
	}

	/**
	 * @return Devuelve el idto del rango de la relación.
	 */
	public int getRange() {
		return range;
	}

	/**
	 * @return Devuelve el identificador de la propiedad que relaciona los
	 *         objetos.
	 */
	public int getIdProperty() {
		return idProperty;
	}

	/**
	 * @return Devuelve el identificador de la tabla donde se produce la
	 *         relación entre los dos objetos.
	 */
	public int getTableIdto() {
		return tableIdto;
	}

	/**
	 * @return Devuelve el tableId del objeto en el dominio de la propiedad.
	 */
	public int getDomainId() {
		return domainId;
	}

	/**
	 * @return Devuelve el tableId del objeto en el rango de la propiedad.
	 */
	public int getRangeId() {
		return rangeId;
	}
	
	public boolean equals (Object obj){
		boolean result = obj != null && obj instanceof ObjectLink;
		if (result){
			ObjectLink objectLink = (ObjectLink) obj;
			result = objectLink.domain == this.domain && objectLink.domainId == this.domainId && objectLink.range == this.range && objectLink.rangeId == this.rangeId && objectLink.idProperty == this.idProperty && objectLink.tableIdto == this.tableIdto;
		}
		return result;
	}
	
	public int hashCode(){
		String id = domain + "#" + domainId + "#" + range + "#" + rangeId + "#" + idProperty + "#" + tableIdto;
		return id.hashCode();
	}
	
	public String toString(){
		return "DOMAIN [IDTO=" + domain + ", TABLE_ID=" + domainId +"]; RANGE [IDTO=" + range + ", TABLE_ID=" + rangeId + "]; PROPERTY=" + idProperty + "; TABLE=" + tableIdto; 
	}
}