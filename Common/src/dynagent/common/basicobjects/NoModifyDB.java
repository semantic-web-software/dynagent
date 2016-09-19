package dynagent.common.basicobjects;

public class NoModifyDB {

	private Integer idTo;
	private boolean actualPC;

	public NoModifyDB() {}
	
	public NoModifyDB(Integer idTo, boolean actualPC) {
		this.idTo = idTo;
		this.actualPC = actualPC;
	}

	public boolean isActualPC() {
		return actualPC;
	}
	public void setActualPC(boolean actualPC) {
		this.actualPC = actualPC;
	}

	public Integer getIdTo() {
		return idTo;
	}
	public void setIdTo(Integer idTo) {
		this.idTo = idTo;
	}

}
