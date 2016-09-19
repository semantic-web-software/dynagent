package gdev.gen;

import dynagent.common.exceptions.IUserException;

public class NotValidValueException extends Exception implements IUserException{
	
	private static final long serialVersionUID = 1L;
	
	private String userMessage;
	
	public NotValidValueException(String msg){
		super(msg);
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

}
