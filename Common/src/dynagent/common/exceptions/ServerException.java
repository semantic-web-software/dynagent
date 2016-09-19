package dynagent.common.exceptions;

import java.rmi.RemoteException;

public class ServerException extends RemoteException implements IUserException{
	private String userMessage;
	
	public ServerException(String msg){
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
