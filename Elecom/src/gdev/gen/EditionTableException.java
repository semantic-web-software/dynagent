package gdev.gen;

import dynagent.common.exceptions.IUserException;

public class EditionTableException extends Exception implements IUserException{
	
	private static final long serialVersionUID = 1L;
	
	private String userMessage;
	private boolean notify;
	private Integer ido;
	
	public EditionTableException(String msg,boolean notify,Integer ido){
		super(msg);
		this.notify=notify;
		this.ido=ido;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}

	public boolean isNotify() {
		return notify;
	}

	public void setNotify(boolean notify) {
		this.notify = notify;
	}

	public Integer getIdo() {
		return ido;
	}

	public void setIdo(Integer ido) {
		this.ido = ido;
	}

}
