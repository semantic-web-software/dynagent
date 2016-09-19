package dynagent.server.dbmap;

/**
 * Excepcion que se da cuando el modelo de base de datos relacional existente no
 * puede resolver una relacion o caracteristica de los datos dados.
 */
public class RelationalDataBaseException extends Exception {

	private static final long serialVersionUID = 2790614200031244093L;

	public RelationalDataBaseException() {
		super();
	}

	public RelationalDataBaseException(String arg0) {
		super(arg0);
	}

	public RelationalDataBaseException(Throwable arg0) {
		super(arg0);
	}

	public RelationalDataBaseException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
