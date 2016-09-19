package dynagent.server.dbmap;

/**
 * Excepcion que se da cuando se intenta añadir informacion sobre una propiedad en una tabla que no tiene indicado que
 * tiene que poseer informacion sobre la misma.
 */
public class NoSuchPropertyException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1119444160516976517L;
	
	public NoSuchPropertyException(){
		super();
	}
	
	public NoSuchPropertyException(String msg){
		super(msg);
	}

}
