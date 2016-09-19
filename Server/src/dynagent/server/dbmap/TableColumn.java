package dynagent.server.dbmap;

import java.io.Serializable;

public class TableColumn implements IQueryInfoColumn, Serializable {

	private static final long serialVersionUID = 8103969839269697254L;
	private String columnName;
	private int dataType;
	private int idProperty;
	private boolean notNull;
	private boolean unsigned;
	private boolean unique;
	private boolean autoInc;

	/**
	 * Crea una columna con el nombre y el tipo indicado, donde el resto de los parámetros se ponen a falso. Es decir,
	 * los datos de la columna podran ser nulos, tendran signo, no tendran porque ser unicos y no se utilizara el auto
	 * incremento.
	 * 
	 * @param columnName
	 *            Nombre que se le quiere poner a la columna.
	 * @param dataType
	 *            Tipo de datos que contendra la columna.
	 */
	public TableColumn(String columnName, int dataType, int idProperty) {
		this(columnName, dataType, false, false, false, false, idProperty);
	}

	public TableColumn(String columnName, int dataType, boolean notNull, boolean unsigned, boolean unique,boolean autoInc, int idProperty) {
		this.columnName = columnName;
		this.dataType = dataType;
		this.notNull = notNull;
		this.unsigned = unsigned;
		this.unique = unique;
		this.autoInc = autoInc;
		this.idProperty = idProperty;
	}

	public String getColumnName() {
		return columnName;
	}

	public Integer getColumnDataType() {
		return dataType;
	}
	
	public int getIdProperty(){
		return idProperty;
	}

	/**
	 * @return the notNull
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * @param notNull
	 *            the notNull to set
	 */
	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	/**
	 * @return the unsigned
	 */
	public boolean isUnsigned() {
		return unsigned;
	}

	/**
	 * @param unsigned
	 *            the unsigned to set
	 */
	public void setUnsigned(boolean unsigned) {
		this.unsigned = unsigned;
	}

	/**
	 * @return the unique
	 */
	public boolean isUnique() {
		return unique;
	}

	/**
	 * @param unique
	 *            the unique to set
	 */
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	/**
	 * @return the autoInc
	 */
	public boolean isAutoInc() {
		return autoInc;
	}

	/**
	 * @param autoInc
	 *            the autoInc to set
	 */
	public void setAutoInc(boolean autoInc) {
		this.autoInc = autoInc;
	}

	public boolean equals(Object obj) {
		return (obj instanceof TableColumn) && (((TableColumn) obj).getColumnName() != null)
				&& (((TableColumn) obj).getColumnName().equals(columnName))
				&& (((TableColumn) obj).getColumnDataType() == dataType);
	}

	public int hashCode() {
		Integer colType = new Integer(dataType);
		return colType.hashCode() * columnName.hashCode() - dataType;
	}
	
	public String toString(){
		return columnName;
	}

	/**
	 * Cambia el nombre de la columna
	 * 
	 * @param newName
	 *            Nuevo nombre de la columna
	 */
	public void setName(String newName) {
		this.columnName = newName;
	}
}
