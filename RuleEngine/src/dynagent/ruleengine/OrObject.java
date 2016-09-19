package dynagent.ruleengine;

public class OrObject {
	private Object firstOp;
	private Object secondOp;
	
	public OrObject(Object firstOp, Object secondOp) {
		super();
		this.firstOp = firstOp;
		this.secondOp = secondOp;
	}

	public Object getFirstOp() {
		return firstOp;
	}

	public void setFirstOp(Object firstOp) {
		this.firstOp = firstOp;
	}

	public Object getSecondOp() {
		return secondOp;
	}

	public void setSecondOp(Object secondOp) {
		this.secondOp = secondOp;
	}
	
	public String toString(){
		return "("+firstOp+" OR"+secondOp+" )"; 
	}
	
	
	
	
}
