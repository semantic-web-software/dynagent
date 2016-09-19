package dynagent.common.basicobjects;

public class SSubReport {
	private Integer idto;
	private String id;
	private String subreport;
	public SSubReport(){
		
	}
	public SSubReport(Integer idto, String id, String subreport){
		this.id=id;
		this.idto=idto;
		this.subreport=subreport;
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	
	public String toString(){
		String result="";
		result="(SSUBREPORT (IDTO "+this.idto+") (IDO "+this.id+") (SUBREPORT "+this.subreport+"))";
		return result;
	}
	public String getSubreport() {
		return subreport;
	}
	public void setSubreport(String subreport) {
		this.subreport = subreport;
	}
}
