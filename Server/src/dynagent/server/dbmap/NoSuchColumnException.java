package dynagent.server.dbmap;

/**
 * Excepcion que salta cuando se intenta referenciar a una columna que no existe.
 */
public class NoSuchColumnException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7059306664405728739L;

	public NoSuchColumnException() {
		super();
	}

	public NoSuchColumnException(String msg) {
		super(msg);
	}
}
