package dynagent.server.exceptions;

public class MessageException extends Exception {

	int error=0;
	
	public MessageException(java.lang.String msg) {
		super(msg);
	}
	
	public MessageException(int err, java.lang.String msg) {
		super(msg);
		error = err;
	}

	public int getCode() {
		return error;
	}

}
