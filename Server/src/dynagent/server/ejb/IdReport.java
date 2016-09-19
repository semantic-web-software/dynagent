package dynagent.server.ejb;

public class IdReport {

	private String id;
	private String report;

	public IdReport(String id, String report) {
		this.id = id;
		this.report = report;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getReport() {
		return report;
	}
	public void setReport(String report) {
		this.report = report;
	}
	public String toString(){
		String result="";
		result+="<IDREPORT ID="+id+" REPORT="+report+">";
		return result;
	}
}
