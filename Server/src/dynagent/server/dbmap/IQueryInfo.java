package dynagent.server.dbmap;

import java.util.List;
import java.util.Set;

/**
 * metodos que tienen que contener todas aquellas clases que representen una
 * tabla o una vista, para permitir la consulta sobre los mismos.
 */
public interface IQueryInfo {

	/**
	 * Identificador de propiedad que se le asignara a la columna de nombre
	 * {@link IQueryInfo#COLUMN_NAME_TABLEID}.<br>
	 * Se considera que representa una DataProperty.
	 */
	public final static int ID_TABLE_ID = -100;
	/**
	 * Identificador de propiedad que se le asignara a la columna de nombre
	 * {@link IQueryInfo#COLUMN_NAME_DESTINATION}.<br>
	 * Se considera que representa una DataProperty.
	 */
	public final static int ID_DESTINATION = -101;
	/**
	 * Identificador de propiedad que se le asignara a la columna de nombre
	 * {@link IQueryInfo#COLUMN_NAME_IDTO}. Se considera que representa una
	 * DataProperty.
	 */
	public final static int ID_IDTO = -102;
	/**
	 * Identificador de propiedad que se le asignara en las columnas de una
	 * asociacion que contengan datos del dominio del vinculo.<br>
	 * Se considera que representa a una ObjectProperty.
	 */
	public final static int ID_DOMAIN = -103;
	/**
	 * Identificador de la propiedad que se le asignara en las columnas de una
	 * asociacion que contengan datos del identificador de la propiedad por la
	 * que se relacionan las dos clases.<br>
	 * Se considera que representa una DataProperty.
	 */
	public final static int ID_PROPERTY = -104;
	/**
	 * Sufijo que se añade en las vistas a las columnas que contienen datos del
	 * idto de una ObjectProperty.
	 */
	public final static String SUFFIX_IDTO = "Idto";
	/**
	 * Sufijo que se añade a los nombres de columna que contienen datos de una
	 * ObjectProperty.<br>
	 * Este sufijo solo se añade si la columna toma el nombre de la clase a la
	 * que apunta la ObjectProperty
	 */
	public final static String SUFFIX_ID = "Id";
	/**
	 * Nombre que se le asigna a la columna que contendra, en las vistas, idto
	 * de la clase de la que han surgido los datos.
	 */
	public final static String COLUMN_NAME_IDTO = "idto";
	/**
	 * Nombre que se le asigna en las asociaciones a la columna que contiene la
	 * propiedad por la que se relacionan.<br>
	 * Hay que tener en cuenta que esta columna solo aparecera si dos clases se
	 * relacionan por más de una propiedad y dicha informacion debe ser
	 * externalizada a una asociacion.
	 */
	public final static String COLUMN_NAME_PROPERTY = "idProperty";
	/**
	 * Nombre de la columna donde aparecera el identificador del objeto en base
	 * de datos.<br>
	 * Estos identificadores son unico por clase, pero no unicos en toda la base
	 * de datos.
	 */
	public final static String COLUMN_NAME_TABLEID = "tableId";
	/** Nombre de la columna donde se guardara el destino de replica del objeto. */
	public final static String COLUMN_NAME_DESTINATION = "r_destination";
	
	/**
	 * Consulta el nombre del objeto actual.
	 * @return Nombre que tiene en base de datos el objeto actual
	 */
	public String getName();

	/**
	 * Identificador que tiene asignado este objeto para poder consultarlo de
	 * manera rapida.
	 * 
	 * @return Identificador numero de este objeto
	 */
	public Integer getId();

	/**
	 * Devuelve todos los nombres de columnas que contienen la propiedad
	 * indicada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que se quiere consultar.
	 * @return Nunca devolvera <code>null</code>.
	 * @throws TypeErrorException 
	 */
	public List<String> getColumnNamesContainingProperty(int idProperty) throws NoSuchColumnException;
	
	/**
	 * Obtiene la columna con el nombre indicado.
	 * @param columName Nombre de la columna que se quiere obtener.
	 * @return devolvera <code>null</code> si no se tiene informacion sobre una columna con dicho nombre.
	 */
	public IQueryInfoColumn getColumnByName(String columName);

	/**
	 * Devuelve el identificador de la clase sobre la cual se almacena
	 * informacion en la columna indicada.
	 * 
	 * @param columnName
	 *            Nombre de la columna sobre la que se quiere consultar.
	 * @return Devuelve el identificador de la clase sobre la que contiene
	 *         informacion la columna. Puede devolver <code>null</code> si se
	 *         trata de una columna que contiene informacion de una DataProperty
	 *         o directamente el nombre dado no se corresponde a ninguna
	 *         columna.
	 */
	public Integer getColumnDomain(String columnName);

	/**
	 * Devuelve la columna que contiene la informacion de la DataProperty
	 * indicada.
	 * 
	 * @param idProperty
	 *            Identificador de la DataProperty sobre la que queremos
	 *            consultar.
	 * @return Objeto que tiene toda la informacion de la columna o
	 *         <code>null</code> si no se trata de una DataProperty.
	 */
	public IQueryInfoColumn getDataPropertyColumn(int idProperty);

	/**
	 * Consulta las columnas que contienen informacion de la propiedad dada y
	 * que apuntan al rango indicado.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que se quiere consultar.
	 * @param idRange
	 *            Identificador de la clase de la que se quiere que contenga
	 *            informacion la columna.
	 * @return Siempre devuelve un par de columnas. La primera de ellas contiene
	 *         informacion sobre el tableId del objeto referenciado y la
	 *         segunda, que puede ser nula, contendra la informacion del idto
	 *         del objeto referenciado.<br>
	 *         Puede ser <code>null</code> si la propiedad indicada es
	 *         desconocida, no es objectProperty o directamnte no se tiene
	 *         informacion de ella. tambien puede devolver <code>null</code> si
	 *         no se conoce ninguna columna que apunte al rango indicado.
	 */
	public IQueryInfoColumn [] getObjectPropertyColumn(int idProperty, int idRange);

	/**
	 * Devuelve el conjunto de indentificadores de otros IQueryInfo que
	 * contienen la informacion de la propiedad externalizada indicada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que se quiere consultar.
	 * @return Puede devolver <code>null</code> si la propiedad indicada no está
	 *         externalizada.
	 */
	public Set<Integer> getExternalizedPropertyLocations(int idProperty);
	
	/**
	 * Consulta si el objeto actual representa una asociacion.
	 * 
	 * @return <code>true</code> si se trata de una asociacion.
	 */
	public boolean isAssociation();

	/**
	 * Consulta si la propiedad indicada está externalizada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que queremos consultar.
	 * @return devolvera <code>true</code> si y solo si es una propiedad que se
	 *         sabe que ha sido externalizada.
	 */
	public boolean isExternalizedProperty(int idProperty);

	/**
	 * Consulta si una propiedad ha sido insertada en este objeto desde otro
	 * IQueryInfo al ser externalizada.
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que queremos consultar.
	 * @return <code>true</code> si la propiedad procede de otra clase.
	 */
	public boolean isForeignProperty(int idProperty);

	/**
	 * Consulta si una propiedad es ObjectProperty
	 * 
	 * @param idProperty
	 *            Identificador de la propiedad por la que queremos consultar.
	 * @return <code>true</code> si es ObjectProperty. Si devuelve
	 *         <code>false</code> tambien puede ser porque no se tiene
	 *         conocimiento de la propiedad.
	 */
	public boolean isObjectProperty(int idProperty);

}
