package dynagent.server.dbmap;

/**
 * Excepcion que se produce cuando se intentan declarar más de una clave foranea en una columna de origen.
 */
public class ForeignKeyOverlapException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7953664037673683840L;

	public ForeignKeyOverlapException() {
		super();
	}

	public ForeignKeyOverlapException(String msg) {
		super(msg);
	}
}
