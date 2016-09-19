package dynagent.common.exceptions;

public class CommunicationException extends ServerException {

	int error=0;
	public static final int REINTETOS_EXCEDIDOS = 1;
	public static final int ERROR_REMOTO = 2;
	public static final int ERROR_CIERRE_CONEXION = 3;
	public static final int TIME_OUT= 4;
	public static final int ERROR_TRAMA= 5;

      public CommunicationException(int err, String msg) {
            super(msg);
		//m_msg=String.copyValueOf(msg.toCharArray());
		if(msg==null) System.out.println(error+"COM EXCEP: TRY TO BUILD WITH MSG NULO");
		error= err;
      }
      public CommunicationException(String msg) {
            super(msg);
		if(msg==null) System.out.println("COM EXCEP: TRY TO BUILD WITH MSG NULO");
      }

	public int GetCode(){
		return error;
	}
        
}