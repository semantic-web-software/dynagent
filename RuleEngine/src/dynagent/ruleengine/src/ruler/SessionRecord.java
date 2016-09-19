package dynagent.ruleengine.src.ruler;

public abstract class SessionRecord {
	protected int idSession;

	public SessionRecord(int idSession) {
		super();
		this.idSession = idSession;
	}
	
	public int getIdSession(){
		return idSession;
	}
	
	public void setIdSession(int idSession){
		this.idSession=idSession;
	}
	
	public String toString(){
		return "idSession:"+idSession;
	}
}
