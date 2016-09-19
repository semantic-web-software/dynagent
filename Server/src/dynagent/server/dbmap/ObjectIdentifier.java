package dynagent.server.dbmap;

/**
 * Clase que establece el metodo para identificar a cada objeto univocamente
 * en la base de datos y redefine el metodo hashCode para que se pueda usar
 * ese idenficador para indexar mapas.
 */
public class ObjectIdentifier {

	private int idto;

	private int tableId;

	private int hashCode;

	public ObjectIdentifier(int idto, int tableId) {
		this.idto = idto;
		this.tableId = tableId;

		String identifier = idto + "#" + tableId;
		this.hashCode = identifier.hashCode();
	}

	public int hashCode() {
		return hashCode;
	}
	
	public boolean equals(Object obj){
		return (obj instanceof ObjectIdentifier) && ((ObjectIdentifier)obj).idto == idto && ((ObjectIdentifier)obj).tableId == tableId;
	}

	public String toString() {
		return ("Clase: " + idto + "; Objeto: " + tableId);
	}

	/**
	 * @return Devuelve el idto de la clase del objeto al que identifica este objeto.
	 */
	public int getIdto() {
		return idto;
	}

	/**
	 * @return Devuelve el tableId del objeto al que identifica este objeto.
	 */
	public int getTableId() {
		return tableId;
	}
}