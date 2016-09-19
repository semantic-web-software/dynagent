package dynagent.common.exceptions;

/**
 * Excepción que se da cuando se produce un error al trabajar con los ficheros.
 */
public class FileException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4134365655500849428L;

	public FileException(){
		super();
	}
	
	public FileException(String msg){
		super(msg);
	}
}
