package dynagent.business;

import java.io.File;
import java.net.PasswordAuthentication;
import java.util.ArrayList;

import dynagent.business.exception.NoSuchDatabaseManagerException;
import dynagent.business.parser.FileParser;
import dynagent.business.parser.TextFileParser;
import dynagent.business.util.Constants;

public class Main {
	
	/*
	 * Se va a implementar la siguiente pol�tica para los par�metros de entrada:
	 * 
	 * 		� Debe indicarse al menos 1 base de datos a a�adir en los ficheros.
	 * 		� Debe indicarse un solo JBoss sobre el que vamos a trabjar. Esto implica
	 * que si tras el identificador -j se introduce m�s de un n�mero, se considerar�
	 * que es un error.
	 * 		� Solo se puede introducir un par�metro tras -r. Si se pone m�s de uno, se
	 * considerar� un error.
	 * 		� Solo se puede indicar un gestor de base de datos a utilizar. Si se pone
	 * m�s de uno se considera un error.
	 * 		� S�lo se puede indicar un usuario, si se pone m�s de uno se considerar�
	 * un error.
	 * 		� S�lo se puede indicar una contrase�a, si se pone m�s de una se considerar�
	 * un error.
	 * 		� Se debe indicar un s�lo par�metro de reglas. Si no se indica ninguno o se
	 * indica m�s de uno se considerar� error.
	 * 		� S�lo se puede indicar un valor para HideHistoryDDBB, si se indica m�s de
	 * uno se considerar� un error.
	 * 		� Se puede indicar un puerto para la base de datos. Si no se indica ninguno
	 * se tomar� por defecto el 3306. Si se indica m�s de uno se considerar� error.
	 */
	private static boolean JBOSS_SET = false;
	private static boolean REPLICA_SET = false;
	private static boolean DBMAN_SET = false;
	private static boolean DB_SET = false;
	private static boolean USER_SET = false;
	private static boolean PASSWORD_SET = false;
	private static boolean RULES_SET = false;
	private static boolean HIDE_SET = false;
	private static boolean DB_PORT_SET = false;
	private static boolean SHARED_BEAN_SET = false;
	private static boolean LICENSE_CODE_SET = false;
	
	private static boolean ERROR_FOUND = false;

	/**
	 * Punto de entrada de nuestro programa
	 * 
	 * @param args 
	 * <br><code>-j</code> indica el n�mero de jboss sobre el que queremos modificar los ficheros.
	 * <b>Par�metro obligatorio</b>
	 * <br><code>-b</code> indica las bases de datos a las que queremos que los ficheros referencien.
	 * <b>Par�metro obligatorio</b>
	 * <br><code>-g</code> indica que gestor de base de datos utilizamos para leer las BD especificadas.
	 * Valor por defecto <i>mySQL</i>
	 * <br><code>-r</code> indica si queremos que haya r�plica o no (posibles valores <code>true</code> o <code>false</code>. 
	 * Valor por defecto a <i>false</i>
	 * <br><code>-u</code> indica el usuario que queremos que aparezca por defecto al cargar la aplicaci�n. 
	 * <i>Par�metro opcional</i>
	 * <br><code>-p</code> indica la contrase�a que queremos que aparezca por defecto al cargar la aplicaci�n. 
	 * <i>Par�metro opcional</i>
	 * <br><code>-rules</code> indica los ficheros de reglas que se van a usar para ejecuttar la aplicaci�n. 
	 * <b>Par�metro obligatorio</b>
	 * <br><code>-hide</code> valor que va a tener <i>hidehistoryDDBB</i>. 
	 * Valor por defecto <i>false</i>.
	 * <br><code>-dbport</code> indica el puerto por el que conectarse a la base de datos.
	 */
	public static void main(String[] args) {
		reset();
		
		
		String id = null;
		for(int i = 0 ; i < args.length && ! ERROR_FOUND ; i ++){
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-b")){
					addDB(args[i]);
				}else if(id.equalsIgnoreCase("-j")){
					setJBoss(args[i]);
				}else if(id.equalsIgnoreCase("-g")){
					setDBManager(args[i]);
				}else if(id.equalsIgnoreCase("-r")){
					setReplica(args[i]);
				}else if(id.equalsIgnoreCase("-h") || id.equalsIgnoreCase("-help")){
					showHelpMessage();
				}else if (id.equalsIgnoreCase("-u")){
					setUser(args[i]);
				}else if (id.equalsIgnoreCase("-p")){
					setPassword(args[i]);
				}else if (id.equalsIgnoreCase("-rules")){
					setRules(args[i]);
				}else if (id.equalsIgnoreCase("-hide")){
					setHideHistoryDDBB(args[i]);
				}else if (id.equalsIgnoreCase("-dbport")){
					setDBPort(args[i]);
				}else if (id.equalsIgnoreCase("-sharedbean")){
					setSharedInstance(args[i]);
				}else if (id.equalsIgnoreCase("-licensecode")){
					setLicenseCode(args[i]);
				}else{
					showError();
				}
			}
		}
		
		if (ERROR_FOUND){
			return;
		}
		
		if (! DB_SET){
			System.err.println("[Business] Error: Se debe indicar al menos una base de datos a referenciar.");
			return;
		}
		
		if (! JBOSS_SET){
			System.err.println("[Business] Error: Se debe indicar sobre que JBoss se quiere trabajar.");
			return;
		}
		
		if (! RULES_SET){
			System.err.println("[Business] Error: Se debe indicar al menos un fichero de reglas.");
			return;
		}
		
		if (! SHARED_BEAN_SET){
			System.err.println("[Business] Error: Se debe indicar si los bean del server son reusables.");
		}
		
		if (! HIDE_SET){
			System.err.println("[Business] Advertencia: No se ha indicado un valor para hidehistoryDDBB. Se pone por defecto a false");
		}
		
		if (! LICENSE_CODE_SET){
			System.err.println("[Business] Advertencia: No se ha indicado un valor para licensecode. Se pone por defecto a false");
		}
		
		if (! REPLICA_SET){
			System.err.println("[Business] Advertencia: No se ha indicado si se desea replica. Se pone por defecto a false.");
		}
		
		if (! DBMAN_SET){
			System.err.println("[Business] Advertencia: No se ha indicado un gestor de base de datos. Se pone por defecto mySQL.");
		}
		
		if (! DB_PORT_SET){
			if (Constants.DBMANAGER.equalsIgnoreCase("postgresql")){
				Constants.DB_PORT = 5432;
				System.err.println("[Business] Advertencia: No se ha indicado un puerto para la base de datos. Se pone por defecto el 5432.");
			}else{
				System.err.println("[Business] Advertencia: No se ha indicado un puerto para la base de datos. Se pone por defecto el 3306.");
			}
		}
		
		//LLEGADOS A ESTE PUNTO TENEMOS TODOS LOS PAR�METROS QUE NECESITAMOS Y SABEMOS QUE SON CORRECTOS.
		
		FileParser execute = new TextFileParser();
		execute.buildFiles();

	}
	
	private static void setSharedInstance(String shared) {
		if (SHARED_BEAN_SET){
			showError();
			return;
		}
		
		boolean sharedBooleanValue = Boolean.parseBoolean(shared);
		Constants.SHARED_BEAN = sharedBooleanValue;
		SHARED_BEAN_SET = true;
	}
	
	private static void setLicenseCode(String licenseCode) {
		if (LICENSE_CODE_SET){
			showError();
			return;
		}
		
		boolean licenseBooleanValue = Boolean.parseBoolean(licenseCode);
		Constants.LICENSE_CODE = licenseBooleanValue;
		LICENSE_CODE_SET = true;
	}

	/**
	 * Establece el puerto por el que se tiene que conectar el sistema a la base de datos.
	 * 
	 * @param port Puerto en el que se encuentra la base de datos. Tiene que ser parseable a entero.
	 */
	private static void setDBPort(String port) {
		if (DB_PORT_SET){
			showError();
			return;
		}
		try {
			Constants.DB_PORT = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			System.err.println("[Business] El valor " + port + " indicado como puerto no es v�lido. Debe ser un n�mero.");
			return;
		}
		DB_PORT_SET = true;
	}

	/**
	 * A�ade una base de datos a la lista de bases de datos que se van a referenciar
	 * @param db n�mero de la base de datos a incluir. Si el n�mero ya est� en la lista
	 * solo ser� tenido en cuenta una vez.
	 */
	private static void addDB(String db){
		db = db.trim();
		if (Constants.DATABASES == null){
			Constants.DATABASES = new ArrayList<Integer>();
		}
		
		int dbNumber;
		try{
			dbNumber = Integer.parseInt(db);
		}catch (NumberFormatException e) {
			System.err.println("[Business] El identificador" + db + " de una base de datos debe ser un numero");
			return;
		}
		DB_SET = true;
		Constants.DATABASES.add(dbNumber);
	}
	
	/**
	 * Establece si se desea configurar con r�plica o sin ella. Solo puede ser llamado una vez.
	 * Si se intenta dar mas de un valor a replica, se mostrar� un mensaje de error y la aplicaci�n
	 * no ser� lanzada.
	 * @param value Solo si corresponde a la cadena "true" (sin importar may�sculas) se pondr� como
	 * <code>replica = true</code>. En cualquier otro caso se pondr� a <code>false</code>.
	 */
	private static void setReplica(String value){
		if (REPLICA_SET){
			showError();
			return;
		}
		Constants.REPLICA = Boolean.parseBoolean(value);
		REPLICA_SET = true;
	}
	
	/**
	 * Establece el n�mero del JBoss sobre el que queremos trabajar. Solo se puede llamar una
	 * vez a este m�todo. Una segunda llamada dar�a como resultado un error y la aplicaci�n no se
	 * lanzar�a.
	 * @param value Tiene que ser parseable como un n�mero entero.
	 */
	private static void setJBoss(String value){
		if (JBOSS_SET){
			showError();
			return;
		}
		
		JBOSS_SET = true;
		value = value.trim();
		File jBossRoot = new File(value);
		if (! jBossRoot.exists()){
			System.err.println("[Business] La ruta " + value + " no existe.");
			ERROR_FOUND = true;
			return;
		}
		Constants.JBOSS = jBossRoot;
	}
	
	/**
	 * Establece el gestor de base de datos que se va a utilizar. S�lo puede ser llamado una vez.
	 * Una segunda llamada dar�a como resultado un error y no se lanzar�a la aplicaci�n.
	 * @param value Tiene que existir en el rango de valores contemplados como posibles gestores
	 * de bases de datos, en caso contrario, dar�a error.
	 */
	private static void setDBManager(String value){
		if (DBMAN_SET){
			showError();
			return;
		}
		
		try {
			Constants.parseDBM(value);
		} catch (NoSuchDatabaseManagerException e) {
			System.err.println("[Business] " + e.getMessage());
			showError();
		}
		
		DBMAN_SET = true;
	}
	
	/**
	 * Funci�n que se encarga de mostrar que se ha producido un error en los par�metros de entrada,
	 * adem�s de dar una orientaci�n de c�mo se tienen que introducir los par�metros y cuales de ellos
	 * pueden ser opcionales.
	 * Por �ltimo, activa una variable de error, que se encargar� de que el programa no se siga ejecutando.
	 */
	private static void showError(){
		ERROR_FOUND = true;
		System.err.println("[Business] Error: Formato de llamada incorrecto.");
		System.err.println("[Business] \t\tbusiness -j jboss -b bd1 [bd2 ... bdN] -rules rules.dpkg [-g gestorBD] [-r true/false] [-u user] [-p password] [-hide true/false]");
	}
	
	/**
	 * Muestra un mensaaje de ayuda indicando para qu� sirve cada identificador de par�metros y cuales
	 * de ellos son opcionales u obligatorios.
	 */
	private static void showHelpMessage(){
		//TODO Escribir mensaje de ayuda.
	}
	
	/**
	 * Guarda el usuario por defecto que se va a mostrar al arrancar la aplicaci�n para su posterior uso en la construcci�n
	 * de los ficheros.
	 * @param user Nombre de usuario a establecer.
	 */
	private static void setUser(String user){
		if (USER_SET){
			showError();
			return;
		}
		
		Constants.USER = user;
		USER_SET = true;
	}
	
	/**
	 * Guarda la contrase�a por defecto que se va a mostrar al arrancar la aplicaci�n para su porterior uso en la construcci�n
	 * de los ficheros.
	 * @param password Contrase�a a establecer.
	 */
	private static void setPassword(String password){
		if (PASSWORD_SET){
			showError();
			return;
		}
		
		Constants.PASSWORD = password;
		PASSWORD_SET = true;
		
	}
	
	/**
	 * Guarda la cadena que contiene los nombres de los ficheros de reglas que ser�n a�adidos al archivo html. Los distintos archivos
	 * de reglas deben ir separados por ';' y sin espacios.
	 * @param rules Ficheros de reglas a ser usados.
	 */
	private static void setRules(String rules){
		if (RULES_SET){
			showError();
			return;
		}
		
		Constants.RULES = rules;
		RULES_SET = true;
	}
	
	/**
	 * Almacena el valor que se le asignar� a hidehistoryDDBB.
	 * @param hide A no ser que la cadena sea igual a 'true' ignorando may�sculas y min�sculas, se tomar� como false.
	 */
	private static void setHideHistoryDDBB(String hide){
		if (HIDE_SET){
			showError();
			return;
		}
		
		boolean hideBooleanValue = Boolean.parseBoolean(hide);
		Constants.HIDEHISTORYDDBB = hideBooleanValue;
		HIDE_SET = true;
	}
	
	/**
	 * Se encarga de poner todos los valores al estado inicial para que una ejecuci�n del c�digo no
	 * influya en la siguiente.
	 */
	private static void reset(){
		JBOSS_SET = false;
		REPLICA_SET = false;
		DBMAN_SET = false;
		DB_SET = false;
		USER_SET = false;
		PASSWORD_SET = false;
		RULES_SET = false;
		HIDE_SET = false;
		ERROR_FOUND = false;
		SHARED_BEAN_SET = false;
		LICENSE_CODE_SET = false;
		
		Constants.DBMANAGER = "mySQL";
		Constants.DATABASES = null;
		Constants.REPLICA = false;
		Constants.JBOSS = null;
		Constants.ROOT = new File(Main.class.getResource("genericFiles").getPath().substring(1));
		Constants.HIDEHISTORYDDBB = false;
		Constants.USER = null;
		Constants.PASSWORD = null;
		Constants.RULES = null;
		Constants.SHARED_BEAN = false;
		Constants.LICENSE_CODE = false;
	}

}
