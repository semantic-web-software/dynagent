package dynagent.server.dbmap;

public class TypeErrorException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5441172907500101451L;
	
	public TypeErrorException(){
		super();
	}
	
	public TypeErrorException(String msg){
		super(msg);
	}
}