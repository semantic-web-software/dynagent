package dynagent.server.dbmap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import dynagent.common.Constants;

/**
 * Clase para representar la configuración de una columna de una vista.
 */
public class ViewColumn implements IQueryInfoColumn, Serializable {
	
	private static final long serialVersionUID = -583564144356059352L;
	/** Nombre de la columna de la vista de la que se está guardando informacion */
	private String columnName;
	/**
	 * número que representa el tipo de datos que almacena la columna. Los tipos
	 * de datos pueden ser los de {@link Constants}
	 */
	private int columnDataType;
	/**
	 * número que identifica a la propiedad a la que hace referencia el dato
	 * almacenado. Los números negativos representan propiedades ficticias
	 * creadas para identificar otros datos importantes que se han de almacenar
	 * en una vista.
	 */
	private Set<Integer> idProperties;

	/**
	 * unico constructor de esta clase.
	 * 
	 * @param columnName
	 *            Nombre que se le va a dar a la columna de la vista.
	 * @param columnDataType
	 *            Tipo de datos que va a almacenar la columna de la vista.
	 * @param idProperty
	 *            Identificador de la propiedad que va a almacenar la columna de
	 *            la vista.
	 */
	public ViewColumn(String columnName, int columnDataType, int idProperty) {
		this.columnName = columnName;
		this.columnDataType = columnDataType;
		this.idProperties = new HashSet<Integer>();
		idProperties.add(idProperty);
		
	}

	/**
	 * Consulta el nombres de la columna de la vista representada por este objeto.
	 * @return Nombre de la columna de la vista.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Consulta el tipo de los datos almacenados en base de datos.
	 * 
	 * @return Los tipos de datos soportados son los de {@link Constants};
	 *         IDTO_INT, IDTO_STRING, etc. Por poner algunos ejemplos.
	 */
	public Integer getColumnDataType() {
		return columnDataType;
	}

	/**
	 * Entero que reprsenta una de las propiedades de las que se almacenan
	 * datos. Solo las ObjectProperties tienen más de una propiedad por columna
	 * en las vistas.<br>
	 * En la mayoria de los casos esta sera la única propiedad de la que
	 * contendra datos la columna, pues solo las columnas del rango de una vista
	 * asociacion podran tener más de una propiedad.
	 * 
	 * @return Puede ser positivo o negativo. Si es negativo, las constantes
	 *         están definidas en {@link View}.<br>
	 *         Si no se tienen propiedades asociadas a esta columna, se
	 *         devolvera Integer.MIN_VALUE
	 */
	public int getIdProperty() {
		if (idProperties.isEmpty()){
			return Integer.MIN_VALUE;
		}
		return idProperties.iterator().next();
	}

	/**
	 * Le indica a la columna que puede contener datos de una determinada
	 * propiedad.<br>
	 * Solo una columna de una vista asociacion que hace referencia al rango
	 * contendra más de una propiedad.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad que se quiere indicar como
	 *            propietaria de los datos.
	 */
	public void addIdProperty(int idProperty){
		idProperties.add(idProperty);
	}

	/**
	 * Consulta si esta columna guarda datos de una determinada propiedad.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que queremos consultar.
	 * @return <code>true</code> si la propiedad por la que se ha consultado es
	 *         una de las que contiene datos esta columna.
	 */
	public boolean referencesProperty(int idProperty){
		return idProperties.contains(idProperties);
	}
	
	/**
	 * Consulta todas las propiedades a las que hace referencia esta columna.<br>
	 * Solo las columnas del rango de una vista asociacion contendran datos de
	 * más de una propiedad.
	 * 
	 * @return Conjunto de todas las propiedades referenciadas por esta columna.
	 */
	public Set<Integer> getAllIdProperties(){
		return idProperties;
	}
}
