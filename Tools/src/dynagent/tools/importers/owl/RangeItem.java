package dynagent.tools.importers.owl;

/**
 * 
 * Instance.java
 * @author Jose A. Zamora Aguilera
 * @description It represents one item of info about the range.
 */
public class RangeItem{
	
	private String VALUE;
	private String VALUECLS;
	private String QMIN;
	private String QMAX;
	private String OP;
	
	
	public String getQMAX() {
		return QMAX;
	}
	public void setQMAX(String QMax) {
		QMAX = QMax;
	}
	public String getQMIN() {
		return QMIN;
	}
	public void setQMIN(String QMin) {
		QMIN = QMin;
	}
	public String getVALUE() {
		return VALUE;
	}
	public void setVALUE(String value) {
		VALUE = value;
	}
	
	
	public String getVALUECLS() {
		return VALUECLS;
	}
	public void setVALUECLS(String valuecls) {
		VALUECLS = valuecls;
	}
	public String getOP() {
		return OP;
	}
	public void setOP(String OP) {
		this.OP = OP;
	}
	
	public String toString(){
		String result=null;
		result="OP="+this.getOP()+"   VALUE="+this.getVALUE()+"  VALUECLS="+this.getVALUECLS();
		if((QMIN!=null)||(QMAX!=null)){
			result=result+"  QMIN="+this.getQMIN()+"   QMAX="+this.getQMAX(); 
		}
		return result;
	}
}
