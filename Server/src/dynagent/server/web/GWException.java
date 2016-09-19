package dynagent.server.web;

public class GWException extends Exception {

//	public static final int ERROR_GW_XML = 1;
//	public static final int ERROR_GW_JMS = 2;
//	public static final int ERROR_GW_SERVLET = 3;
//	public static final int ERROR_GW_RULER = 4;
	public static final int ERROR_GW_SYSTEM = 5;
//	public static final int ERROR_GW_SQL = 6;
//	public static final int ERROR_DE_FORMATO = 10;
//
	int error=0;
      String msgid=null;
      public GWException(int err, java.lang.String msg, String msgid) {
            super(msg);
            error= err;
            this.msgid=msgid;
      }
	public int GetCode(){
		return error;
	}
        public String getMsgID(){
            return msgid;
        }
}
