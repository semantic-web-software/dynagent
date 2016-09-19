package dynagent.business.exception;
/**
 * Excepción que se lanza cuando el gestor de base de datos indicado por parámentros
 * no ha podido ser reconocido como uno de los gestores de base de datos aceptados por
 * el sistema.
 */
public class NoSuchDatabaseManagerException extends Exception {

	private static final long serialVersionUID = -5229585711372850254L;
	
	public NoSuchDatabaseManagerException(){
		super();
	}
	
	public NoSuchDatabaseManagerException(String msg){
		super(msg);
	}

}
