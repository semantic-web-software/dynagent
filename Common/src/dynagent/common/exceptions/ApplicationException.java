package dynagent.common.exceptions;

public class ApplicationException extends ServerException {

	int error=0;
	String param1;
	public static final int CONTEXTO_NO_MAPEADO = 1;
    public static final int DATA_DOMAIN_COMPLIANT_ERROR = 2;
    public static final int ERROR_ASIGN = 3;

      public ApplicationException(int err, String msg) {
            super(msg);
		//m_msg=String.copyValueOf(msg.toCharArray());
		System.out.println("APP EXC:"+msg);
		if(msg==null) System.out.println(error+"APP EXCEP: TRY TO BUILD WITH MSG NULO");
		error= err;
      }

	public int GetCode(){
		return error;
	}

        public String getParam(){
		return param1;
	}
        public void setParam(String param){
		param1=param;
	}
}
