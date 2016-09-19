package dynagent.server.dbmap;

/**
 * Excepcion que salta cuando se ha intentado obtener el nombre de una clase mediante su idto y dicha clase no se tiene
 * mapeada como una clase existente o es una clase que no tiene asociado un nombre.
 */
public class UnrecognizedClass extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -334882112866663828L;

	public UnrecognizedClass() {
		super();
	}

	public UnrecognizedClass(String msg) {
		super(msg);
	}
}
