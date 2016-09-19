package dynagent.tools.importers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

import javax.naming.NamingException;

/**
 * Interfaz que representa la funcionalidad que deben dar los objetos encargados
 * del manejo de las bases de datos durante la importación.
 */
public interface IImporter {

	/**
	 * Intenta eliminar la base de datos identificada por el número dado.
	 * 
	 * @param bns
	 *            Identificador de la base de datos que queremos eliminar.
	 * @return <code>true</code> si se ha conseguido eliminar la base de datos
	 *         con éxito.
	 * @throws SQLException
	 *             Si hay algún error en las consultas SQL.
	 * @throws NamingException
	 *             Si hay algún error en la conexión con la base de datos.
	 */
	public boolean dropDataBase(int bns) throws NamingException, SQLException;

	/**
	 * Intenta crear la base de datos con el identificador especificado.
	 * 
	 * @param bns
	 *            Identificador de la base de datos que queremos crear.
	 * @return <code>true</code> si se ha conseguido crear la base de datos con
	 *         éxito.
	 * @throws NamingException
	 *             Si se produce un error en la conexión con la base de datos.
	 * @throws SQLException
	 *             Si hay algún error en la consulta SQL.
	 * @throws IOException 
	 */
	public boolean createDataBase(int bns) throws SQLException, NamingException, IOException;

	/**
	 * Intenta crear una base de datos con las mismas características y datos
	 * que otra que ya existe
	 * 
	 * @param original
	 *            Identificador de la base de datos original de la que hemos de
	 *            copiar la estructura.
	 * @param destination
	 *            Identificador de la base de datos que hemos de crear.
	 * @return <code>true</code> si conseguimos copiar la base de datos con
	 *         éxito.
	 */
	public boolean copyDataBase(int original, int destination);

	/**
	 * Indica el puerto donde se encuentra a la escucha el servicio del gestor
	 * de bases de datos.
	 * 
	 * @param port
	 *            Número al que hemos de mandar las órdenes.
	 */
	
	public boolean renameDataBase(int original, int destination) throws SQLException, NamingException;;
	
	public void setPort(int port);

	/**
	 * Modifica la dirección IP o dominio donde se encuentra el host donde se
	 * quieren crear las bases de datos, que por defecto se puso como localhost.
	 * 
	 * @param host
	 *            Localización donde se tienen que crear/eliminar/copiar las
	 *            bases de datos.
	 */
	public void setHost(String host);

	/**
	 * Crea las vistas del sistema necesarias para poder trabajar con la
	 * aplicación.<br>
	 * <b>Importante:</b> Este método debe ser llamado <b>después</b> de haber
	 * importado el modelo a la base de datos.
	 * 
	 * @param bns
	 *            Base de datos sobre la que se quieren crear las vistas de
	 *            datos del sistema.
	 * @throws NamingException
	 *             Si se produce un error en las comunicaciones con la base de
	 *             datos.
	 * @throws SQLException
	 *             Si hay algún error en las sentencias SQL.
	 * @throws IOException 
	 */
	public void createSystemViews(int bns) throws SQLException, NamingException, IOException;

	/**
	 * Crea un backup de la base de datos que le indiquemos
	 * @param bns
	 *			Base de datos sobre la que se quiere hacer el backup
	 * @param name
	 *			Nombre que se pondrá al backup
	 * @param includedTables
	 * 			Tablas a incluir en el backup. Si es null o vacia se incluyen todas
	 * @param excludedTables
	 * 			Tablas a excluir en el backup. Si es null o vacia no se excluye ninguna
	 * @return <code>true</code> si conseguimos crear el backup con éxito.
	 */
	public boolean createBackup(int bns, String backupName, Set<String> includedTables, Set<String> excludedTables);
	
	/**
	 * Restaura un backup
	 * @param bns
	 *			Base de datos sobre la que se quiere hacer el backup
	 * @param backupName
	 * 			Nombre del backup a restaurar
	 * @param onlyData
	 * 			Restaurar solo los datos de ese backup, no el esquema
	 * @return
	 * @throws SQLException 
	 * @throws NamingException 
	 */
	public boolean restoreBackup(int bns, String backupName, boolean onlyData) throws NamingException, SQLException;
	
	/**
	 * Manda una señal de terminación a todos los procesos que estén usando la
	 * base de datos indicada para que esta quede libre y se pueda borrar.
	 * 
	 * @param bns
	 *            Número de la base de datos a la que se tienen que interrumpir
	 *            las conexiones.
	 * @return Devuelve <code>true</code> si se ha conseguido con éxito terminar
	 *         las conexiones a la base de datos.
	 * @throws SQLException 
	 * @throws NamingException 
	 */
	public boolean dropConnections(int bns) throws NamingException, SQLException;

	/**
	 * Crea la base de datos vacía.
	 * 
	 * @param bns
	 *            Identificador de la base de datos a crear.
	 * @param template
	 *            Plantilla sobre la que se ha de basar la base de datos nueva
	 *            al crearse.<br>
	 *            Para no utilizar plantilla, el valor tiene que ser menor que
	 *            0.
	 * @return <code>true</code> si se consigue crear la base de datos con
	 *         éxito.
	 */
	public boolean createSchema(int bns, int template);

	void createBusinessFunctions(int bns) throws SQLException, NamingException, IOException;
}
