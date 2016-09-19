package dynagent.common.exceptions;


public abstract class RuleEngineException extends Exception implements IUserException{

	private String userMessage;
	
	public RuleEngineException(String msg){
		super(msg);
		userMessage=msg;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
}
