package dynagent.common.exceptions;

public class SystemException extends ServerException {

	int error=0;
	public static final int REINTETOS_EXCEDIDOS = 1;
	public static final int ERROR_JDOM = 2;
	public static final int ERROR_SLEEP = 3;
	public static final int ERROR_DATOS = 4;
	public static final int ERROR_CONFIRM= 5;
	public static final int ERROR_SISTEMA= 6;
	public static final int ERROR_SISTEMA_REMOTO= 7;
	public static final int ERROR_INOUT= 8;
	public static final int ERROR_JASPER= 9;

      public SystemException(int err, String msg) {
            super(msg);
		//((m_msg=String.copyValueOf(msg.toCharArray());
		if(msg==null) System.out.println(error+"COM EXCEP: TRY TO BUILD WITH MSG NULO");
		error= err;
      }

	public int GetCode(){
		return error;
	}
}
