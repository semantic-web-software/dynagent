package dynagent.server.dbmap;

/**
 * Excepcion que se da cuando se intenta crear una columna en una tabla con un nombre que ya ha sido usado para crear otra columna.
 */
public class ColumnOverlapException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4499709882980139817L;
	
	
	public ColumnOverlapException(){
		super();
	}
	
	public ColumnOverlapException(String msg){
		super(msg);
	}
}
