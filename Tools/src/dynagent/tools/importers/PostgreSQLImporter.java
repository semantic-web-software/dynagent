package dynagent.tools.importers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import dynagent.common.utils.Auxiliar;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.tools.importers.migration.ODatosAtribToXml;

/**
 * Clase que proporciona funcionalidad especifica para importar sobre PostgreSQL.
 */
public class PostgreSQLImporter implements IImporter {
	
	/** Puerto en el que se encuentra el SGBD */
	private int port;
	/** Host en el que se encuentra el SGBD */
	private String host;
	private String pwd;
	
	public PostgreSQLImporter(String pwd){
		this.port = 5432;
		this.host = "localhost";
		this.pwd=pwd;
	}

	@Override
	public boolean copyDataBase(int original, int destination) {
		System.out.println("Copiando la base de datos dyna" + original + " en la base de datos dyna" + destination);
		boolean success = createSchema(destination, original);
		return success;
	}

	@Override
	public boolean createDataBase(int bns) throws SQLException, NamingException, IOException {
		System.out.println("Creando base de datos: dyna" + bns);
		boolean success = createSchema(bns, -1);

		InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/systemTables.sql");
		File scriptFile = File.createTempFile("systemTables", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
		fcdb.setPort(port);
		fcdb.setPwd(pwd);
		DBQueries.executeScript(fcdb, scriptFile);
		scriptFile.delete();
		
		in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/License.sql");
		scriptFile = File.createTempFile("License", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		DBQueries.executeScript(fcdb, scriptFile);
		scriptFile.delete();
					
		in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/SystemCommonTables.sql");
		scriptFile = File.createTempFile("SystemCommonTables", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		Connection con= fcdb.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
		DBQueries.executeScript(con, scriptFile);
		scriptFile.delete();
		con.close();
		
		fcdb.removeConnections();
			
		return success;
	}

	@Override
	public boolean dropDataBase(int bns) throws NamingException, SQLException {
		System.out.println("Cerrando conexiones a la base de datos: dyna" + bns);
		if (! dropConnections(bns)){
			return false;
		}
		System.out.println("Borrando base de datos: dyna" + bns);
		boolean success = true;
		Runtime runtime = Runtime.getRuntime();
		try {
			FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
			fcdb.setPort(port);
			fcdb.setPwd(pwd);
			Connection con= fcdb.createConnection(true).getDataBaseConnNotReusable("postgres");
			Statement st = con.createStatement();
			st.executeUpdate("DROP DATABASE IF EXISTS dyna"+bns);			
			con.close();
			
			fcdb.removeConnections();
			
			/*String command = "dropdb -h " + host + " -U dynagent dyna" + bns;
			System.out.println("Command: "+command);
			Process process = runtime.exec(command);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while((line = reader.readLine()) != null){
				System.err.println(line);
				line = line.toUpperCase();
				if (line.contains("ERROR") && ! line.contains("NO EXISTE LA BASE DE DATOS")){
					success = false;
					break;
				}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		// Borramos la información almacenada para esta base de datos.
		DBQueries.removeBnsInfo(bns,GestorsDBConstants.postgreSQL);
		return success;
	}

	public boolean renameDataBase(int bnsPrev,int bnsNew) throws NamingException, SQLException {
		System.out.println("Cerrando conexiones a la base de datos: dyna" + bnsPrev);
		if (! dropConnections(bnsPrev)){
			return false;
		}
		System.out.println("Renombrando base de datos: dyna" + bnsPrev);
		boolean success = true;
		try {
			FactoryConnectionDB fcdb = new FactoryConnectionDB(bnsPrev, true, host, GestorsDBConstants.postgreSQL);
			fcdb.setPort(port);
			fcdb.setPwd(pwd);
			Connection con= fcdb.createConnection(true).getDataBaseConnNotReusable("postgres");
			Statement st = con.createStatement();
			st.executeUpdate("ALTER DATABASE dyna"+bnsPrev+" RENAME TO dyna"+bnsNew);			
			con.close();
			
			fcdb.removeConnections();					
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		// Borramos la información almacenada para esta base de datos.
		DBQueries.removeBnsInfo(bnsPrev,GestorsDBConstants.postgreSQL);
		return success;
	}
	
	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void createSystemViews(int bns) throws SQLException, NamingException, IOException {
		InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/Views.sql");
		File scriptFile = File.createTempFile("views", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
		fcdb.setPort(port);
		fcdb.setPwd(pwd);
		DBQueries.executeScript(fcdb, scriptFile);
		scriptFile.delete();
		fcdb.removeConnections();
	}
	
	@Override
	public void createBusinessFunctions(int bns) throws SQLException, NamingException, IOException {
		InputStream in = ODatosAtribToXml.class.getResourceAsStream("/dynagent/tools/setup/ddbb/postgres/businessFunctions.sql");
		File scriptFile = File.createTempFile("businessfunctions", "sql");
		Auxiliar.inputStreamToFile(in, scriptFile);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
		fcdb.setPort(port);
		fcdb.setPwd(pwd);
		DBQueries.executeScript(fcdb, scriptFile);
		scriptFile.delete();
		fcdb.removeConnections();
	}

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
	public boolean createSchema(int bns, int template) {
		
		boolean success = true;
		Runtime runtime = Runtime.getRuntime();
		try {
			dropConnections(bns);
			if (template > 0){
				dropConnections(template);
			}
			FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
			fcdb.setPort(port);
			fcdb.setPwd(pwd);
			Connection con= fcdb.createConnection(true).getDataBaseConnNotReusable("postgres");
			Statement st = con.createStatement();
			st.executeUpdate("CREATE DATABASE dyna"+bns);			
			con.close();
			
			fcdb.removeConnections();
			
			/*String command = "createdb -h " + host + " -p " + port + " -U dynagent -D pg_default -E UTF-8";
			if (template > 0){
				command += " -T dyna" + template;
			}
			command +=  " -w dyna" + bns;
			System.out.println("Command: "+command);
			Process process = runtime.exec(command);
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = errorReader.readLine()) != null){
				System.err.println(line);
				line = line.toUpperCase();
				if (line.contains("ERROR")){
					success = false;
				}
			}*/
		} catch (NamingException e) {
			success = false;
			e.printStackTrace();
		} catch (SQLException e) {
			success = false;
			e.printStackTrace();
		}
		return success;
	}

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
	public boolean dropConnections(int bns) throws NamingException, SQLException{
		// Esta query consulta una vista que mantiene la actividad existente en
		// todas las bases de datos.
		String sql = "SELECT pid FROM pg_stat_activity WHERE datname='dyna" + bns + "'";
		// Query con la llamada a la función que 
		String cancelQueryB = "SELECT pg_terminate_backend(";
		String cancelQueryE = ")";
		boolean success = true;
		
		FactoryConnectionDB fcdb = new FactoryConnectionDB(bns, true, host, GestorsDBConstants.postgreSQL);
		fcdb.setPort(port);
		fcdb.setPwd(pwd);
		System.out.println("PWD "+pwd);
		// Vamos a conectarnos a la base de datos que viene por defecto en todo
		// servidor de base de datos PosgreSQL, que mantiene información sobre
		// todas las bases de datos.
		ConnectionDB connectionDB = fcdb.createConnection(true);
		Statement statement = null, dropConnectionStatement = null;
		ResultSet resultSet = null, dropConnectionResultSet = null;
			
		Connection dataBaseConn = connectionDB.getDataBaseConn("postgres");
		statement = dataBaseConn.createStatement();
		resultSet = statement.executeQuery(sql);
		while (resultSet.next() && success) {
			// Identificador numérico del proceso que está conectado a la
			// base de datos.
			int procpid = resultSet.getInt(1);
			if (resultSet.wasNull()){
				// Si es nulo no nos interesa.
				continue;
			}
			// Ahora vamos a mandar la orden de terminar la query que está
			// accediendo a la base de datos.
			String cancelQuerySQL = cancelQueryB + procpid + cancelQueryE;
			dropConnectionStatement = dataBaseConn.createStatement();
			dropConnectionResultSet = dropConnectionStatement.executeQuery(cancelQuerySQL);
			if (dropConnectionResultSet.next()){
				boolean result = dropConnectionResultSet.getBoolean(1);
				// Comprobamos que se haya mandado la señal de terminación
				// correctamente al proceso.
				success = success && result;
			}
		}
		fcdb.removeConnections();
		return success;
	}

	@Override
	public boolean createBackup(int bns, String backupName, Set<String> includedTables, Set<String> excludedTables) {
		boolean success = true;
		Runtime runtime = Runtime.getRuntime();
		try {
			if(includedTables==null)
				includedTables=new HashSet<String>();
			if(excludedTables==null)
				excludedTables=new HashSet<String>();
	
			String command = "pg_dump.exe -h " + host + " -p " + port + " -U dynagent --format custom --file "+backupName;
			
			for(String table:includedTables){
				command += " -t "+ table;
			}
			
			for(String table:excludedTables){
				command += " -T "+ table;
			}
			
			command +=  " dyna" + bns;
			System.out.println("Command: "+command);
			Process process = runtime.exec(command);
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = errorReader.readLine()) != null){
				System.err.println(line);
				line = line.toUpperCase();
				if (line.contains("ERROR")){
					success = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}

	@Override
	public boolean restoreBackup(int bns, String backupName, boolean onlyData) throws NamingException, SQLException {
		
		boolean success = true;
		Runtime runtime = Runtime.getRuntime();
		try {
			String command = "pg_restore.exe -h " + host + " -p " + port + " -U dynagent --single-transaction --dbname dyna"+bns;
			
			if(onlyData){
				command += " --data-only";
			}else{
				dropDataBase(bns);
				createSchema(bns, -1);
			}
			
			command +=  " "+backupName;
			System.out.println("Command: "+command);
			Process process = runtime.exec(command);
			BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String line;
			while ((line = errorReader.readLine()) != null){
				System.err.println(line);
				line = line.toUpperCase();
				if (line.contains("ERROR")){
					success = false;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
}
