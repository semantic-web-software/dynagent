package dynagent.common.basicobjects;

public class PrintSequence {

	private String order;
	private String sequence;
	private boolean prePrint;

	public PrintSequence(){}

	public String getOrder() {
		return order;
	}
	public void setOrder(String order) {
		this.order = order;
	}

	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	public boolean isPrePrint() {
		return prePrint;
	}
	public void setPrePrint(boolean prePrint) {
		this.prePrint = prePrint;
	}

	public String toString(){
		return "(PRINT_SEQUENCE (ORDER "+order+")(SEQUENCE "+sequence+")(PREPRINT"+prePrint+"))";
	}
}
