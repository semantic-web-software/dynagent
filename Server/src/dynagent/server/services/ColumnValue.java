package dynagent.server.services;

/**
 * Clase que guarda la informacion de un dato sacado de una columna de una
 * tabla.
 */
public class ColumnValue {

	public final static byte NO_PROPERTY = 0x00;
	public final static byte DATA_PROPERTY = 0x01;
	public final static byte OBJECT_PROPERTY = 0x02;

	private int dataType;
	private String value;
	private byte propertyType;
	private boolean structuralProperty;
	private int idProperty;
	private String propertyName;
	private String columnName;
	private int referencedClassIdto;

	/**
	 * Constructor simple para datos que no están asociados a ninguna
	 * propiedad.
	 * 
	 * @param dataType
	 *            Tipo del dato almacenado por este objeto. Los tipos se
	 *            pueden sacar de Consntants en common.
	 * @param value
	 *            Objeto que contiene el valor que se le asigna a este
	 *            objeto.
	 * @param columnName
	 *            Nombre de la columna de la que surge el dato.
	 */
	public ColumnValue(int dataType, String value, String columnName) {
		this(dataType, value, false, -1, null, columnName);
		this.propertyType = NO_PROPERTY;
	}

	/**
	 * Constructor para datos asociados a una data property.
	 * 
	 * @param dataType
	 *            Tipo del dato almacenado por este objeto. Los tipos se
	 *            pueden sacar de Constants en commnon.
	 * @param value
	 *            Objeto que contiene el valor que se le asigna a este
	 *            objeto.
	 * @param structuralProperty
	 *            Indica si es la propiedad contenida es estructural
	 * @param idProperty
	 *            Identificador numero de la propiedad o -1 si el dato no
	 *            está asociado a ninguna propiedad.
	 * @param propertyName
	 *            Nombre de la propiedad a la que hace referencia el dato o
	 *            null si no se referencia a ninguna.
	 * @param columnName
	 *            Nombre de la columna que contenia el dato.
	 */
	public ColumnValue(int dataType, String value, boolean structuralProperty, int idProperty, String propertyName, String columnName) {
		this(dataType, value, structuralProperty, idProperty, propertyName, columnName, -1);
		this.propertyType = DATA_PROPERTY;
	}

	/**
	 * Constructor para datos asociados a una object property.
	 * 
	 * @param dataType
	 *            Tipo del dato almacenado por este objeto. Los tipos se
	 *            pueden sacar de Constants en commnon.
	 * @param value
	 *            Objeto que contiene el valor que se le asigna a este
	 *            objeto.
	 * @param structuralProperty
	 *            Indica si es la propiedad contenida es estructural
	 * @param idProperty
	 *            Identificador numero de la propiedad o -1 si el dato no
	 *            está asociado a ninguna propiedad.
	 * @param propertyName
	 *            Nombre de la propiedad a la que hace referencia el dato o
	 *            null si no se referencia a ninguna.
	 * @param columnName
	 *            Nombre de la columna que contenia el dato.
	 * @param referencedClassIdto
	 *            Idto de la clase a la que apunta la object property o -1
	 *            si no se apunta a ninguna.
	 */
	public ColumnValue(int dataType, String value, boolean structuralProperty, int idProperty, String propertyName, String columnName, int referencedClassIdto) {
		this.dataType = dataType;
		this.value = value;
		this.propertyType = OBJECT_PROPERTY;
		this.structuralProperty = structuralProperty;
		this.idProperty = idProperty;
		this.propertyName = propertyName;
		this.columnName = columnName;
		this.referencedClassIdto = referencedClassIdto;
	}

	/**
	 * Consulta el tipo del dato contenido por este objeto. Los tipos estan
	 * definidos en Constants de Common.
	 * 
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * Consulta el tipo de la propiedad si la hay. Puede ser data u object
	 * property si se apunta a alguna propiedad o NO_PROPERTY si no se
	 * apunta a ninguna propiedad.
	 * 
	 * @return número que representa el tipo de propiedad
	 */
	public byte getPropertyType() {
		return propertyType;
	}

	/**
	 * Consulta si la propiedad es estructural.
	 * 
	 * @return <code>true</code> si la propiedad es estructural.
	 */
	public boolean isStructuralProperty() {
		return structuralProperty;
	}

	/**
	 * Consulta el nombre de la propiedad de la que se está guardando el
	 * dato.
	 * 
	 * @return devolvera <code>null</code> si el dato no se asocia con
	 *         ninguna propiedad.
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Consulta el valor guardado para este campo. El tipo del objeto se
	 * puede obtener mirando el dataType.
	 * 
	 * @return Objeto que representa el valor guardado.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Consulta el identificador numero de la propiedad a la que está
	 * asociada el dato.
	 * 
	 * @return Identificador numero de la propiedad o -1 si el dato no
	 *         está asociado a ninguna propiedad.
	 */
	public int getIdProperty() {
		return idProperty;
	}

	/**
	 * Consulta el nombre de la columna de la que surgio el dato.
	 * 
	 * @return Nombre de la columna que contenia el dato en base de datos.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Consulta el idto de la clase a la que apunta la object property
	 * 
	 * @return Idto del que tenemos el tableId como valor en este objeto o
	 *         -1 si no se está referenciando a ninguna clase.
	 */
	public int getReferencedClassIdto() {
		return referencedClassIdto;
	}
}
