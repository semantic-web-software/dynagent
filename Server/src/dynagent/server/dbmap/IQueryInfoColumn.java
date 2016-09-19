package dynagent.server.dbmap;

import dynagent.common.Constants;


/**
 * Interfaz para trabajar con las columnas de Tablas y Vistas.
 */
public interface IQueryInfoColumn {

	/**
	 * Devuelve el nombre que tiene asignado la columna
	 * 
	 * @return Nombre de la columna tal y como aparece en base de datos.
	 */
	public String getColumnName();

	/**
	 * Devuelve el identificador de la propiedad sobre la que almacena datos la
	 * columna.
	 * 
	 * @return Identificador numero de la propiedad sobre la que contiene
	 *         datos la columna.
	 */
	public int getIdProperty();

	/**
	 * Devuelve el identificador del tipo de datos que contiene la columna. Los
	 * identificadores de los tipos de datos son los que aparecen en
	 * {@link Constants}.
	 * 
	 * @return Tipo de datos contenido en la columna.
	 */
	public Integer getColumnDataType();

}
