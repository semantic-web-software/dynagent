package dynagent.business.util;

import java.io.File;
import java.util.List;

import dynagent.business.exception.NoSuchDatabaseManagerException;
/**
 * Clase que va almacenar las constantes del sistema. Además de ello, ofrecerá
 * algunos métodos para que el manejo de dichas constantes sea más sencillo, a la
 * vez que se intenta que los posibles valores de las mismas no tenga por qué ser
 * conocido de antemano por el resto del progrma.
 *
 */
public class Constants {
	
	public final static String DBM_POSTGRESQL = "postgreSQL";
	public final static String DBM_MYSQL = "mySQL";
	public final static String DBM_SQLSERVER = "SQLServer";
	public static File JBOSS;
	public static List<Integer> DATABASES = null;
	public static boolean REPLICA = false;
	public static String DBMANAGER = "mySQL";
	public static File ROOT;
	public static String USER;
	public static String PASSWORD;
	public static String RULES;
	public static boolean HIDEHISTORYDDBB;
	public static Integer DB_PORT = 3306;
	public static boolean SHARED_BEAN;
	public static boolean LICENSE_CODE;

	public static enum DatabaseManager {
		mySQL,SQLServer,PostgreSQL
	}

	/**
	 * Se encarga de dar valor a la constante que refleja el gestor de base de datos seleccionado.
	 * Si la cadena no es reconocida como un identificador de gestor de base de datos aceptado, se
	 * producirá un error.
	 * @param dbIdentifier Cadena a parsear. Al parsearla no se tendrán en cuenta mayúsculas o minúsculas
	 * @throws NoSuchDatabaseManagerException En caso de que la cadena pasada no sea reconocida como
	 * identificador de base de datos reconocido
	 */
	public static void parseDBM(String dbIdentifier) throws NoSuchDatabaseManagerException{

		if (dbIdentifier.equalsIgnoreCase("mysql")){
			DBMANAGER = DBM_MYSQL;
		}else if(dbIdentifier.equalsIgnoreCase("sqlserver")){
			DBMANAGER = DBM_SQLSERVER;
		}else if (dbIdentifier.equalsIgnoreCase("postgresql")){
			DBMANAGER = DBM_POSTGRESQL;
		}else{
			throw new NoSuchDatabaseManagerException("La cadena " + dbIdentifier + " no es un identificador de gestor de base de datos valido.");
		}
	}
}
