package dynagent.gui.forms.utils;

public class ActionException extends Exception{
	
	private static final long serialVersionUID = 1L;
	private String userMessage;
	
	public ActionException(String msg){
		super(msg);
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
}
