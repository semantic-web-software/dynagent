package dynagent.server.services.querys;

class ConditionsWhere {
	private StringBuffer condition1;
	private StringBuffer condition2;
	
	public ConditionsWhere(StringBuffer cond1, StringBuffer cond2) {
		condition1 = cond1;
		condition2 = cond2;
	}

	public StringBuffer getCondition1() {
		return condition1;
	}
	
	public void setCondition1(StringBuffer condition1) {
		this.condition1 = condition1;
	}
	
	public StringBuffer getCondition2() {
		return condition2;
	}
	
	public void setCondition2(StringBuffer condition2) {
		this.condition2 = condition2;
	}
	public String toString() {
		return "Condition1: " + condition1 + "\nCondition2: " + condition2;
	}
}
