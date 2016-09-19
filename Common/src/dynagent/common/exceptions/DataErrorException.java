package dynagent.common.exceptions;

public class DataErrorException extends ServerException {

	int error=0;
	public static final int ERROR_DATA=1;

    public DataErrorException(int err, java.lang.String msg) {
		super(msg);
		error = err;
	}

	public DataErrorException(java.lang.String msg) {
		super(msg);
	}

	public int GetCode() {
		return error;
	}

}
