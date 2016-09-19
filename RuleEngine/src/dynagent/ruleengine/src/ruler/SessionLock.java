package dynagent.ruleengine.src.ruler;

public class SessionLock extends SessionRecord{
	String USERCHANGEDSTATE;
	String STATE;
	
	public SessionLock(String state,String userChangedState,int idSession) {
		super(idSession);
		USERCHANGEDSTATE = userChangedState;
		STATE = state;
	}

	public String getUSERCHANGEDSTATE() {
		return USERCHANGEDSTATE;
	}

	public void setUSERCHANGEDSTATE(String userchangedstate) {
		USERCHANGEDSTATE = userchangedstate;
	}

	public String getSTATE() {
		return STATE;
	}

	public void setSTATE(String state) {
		STATE = state;
	}

}
