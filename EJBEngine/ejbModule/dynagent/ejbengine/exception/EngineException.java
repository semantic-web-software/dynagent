package dynagent.ejbengine.exception;

import dynagent.common.exceptions.IUserException;

/**
 * Excepción usada para indicar que se ha producido algún tipo de fallo mientras
 * se trabajaba con el motor de reglas.
 */
public class EngineException extends Exception implements IUserException{

	private static final long serialVersionUID = 1047178089392996275L;
	
	private String userMessage;
	
	public EngineException(){
		super();
	}
	
	public EngineException(String msg){
		super(msg);
		userMessage=msg;
	}
	
	public EngineException(Throwable err){
		super(err);
		userMessage=err.getMessage();
	}
	
	public EngineException(Throwable err,String userMessage){
		super(err);
		this.userMessage=userMessage;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

}
