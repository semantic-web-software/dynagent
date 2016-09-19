package dynagent.common.exceptions;

public class RemoteSystemException extends ServerException{
    int error=0;
    public RemoteSystemException(int error,String msg) {
    	super(msg);
    	this.error=error;
    }
    public int getError(){
	return error;
    }
}
