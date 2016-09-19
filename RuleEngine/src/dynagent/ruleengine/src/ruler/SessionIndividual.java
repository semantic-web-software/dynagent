package dynagent.ruleengine.src.ruler;

public class SessionIndividual extends SessionRecord{
	String STATE;
	boolean ISOLATED;
	
	public SessionIndividual(String state,boolean isolated,int idSession) {
		super(idSession);
		STATE = state;
		ISOLATED = isolated;
	}

	public String getSTATE() {
		return STATE;
	}

	public void setSTATE(String state) {
		STATE = state;
	}

	public boolean isISOLATED() {
		return ISOLATED;
	}

	public void setISOLATED(boolean isolated) {
		ISOLATED = isolated;
	}
	
	public String toString(){
		return super.toString()+" STATE:"+getSTATE()+" ISOLATED:"+isISOLATED();
	}
}
