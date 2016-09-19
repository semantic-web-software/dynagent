package dynagent.ruleengine.src.ruler;

public class AccessValue {

	private Integer DENNIED;
	private Integer ACCESSTYPE;
	private int session;
	public Integer getACCESSTYPE() {
		return ACCESSTYPE;
	}
	public void setACCESSTYPE(Integer accesstype) {
		ACCESSTYPE = accesstype;
	}
	public Integer getDENNIED() {
		return DENNIED;
	}
	public void setDENNIED(Integer dennied) {
		DENNIED = dennied;
	}
	public int getSesion() {
		return session;
	}
	public void setSession(int sesion) {
		this.session = sesion;
	}	
}
