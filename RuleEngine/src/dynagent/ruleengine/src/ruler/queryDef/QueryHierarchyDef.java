package dynagent.ruleengine.src.ruler.queryDef;

public class QueryHierarchyDef {
	private Integer IDTO=null;
	private Integer IDTOSUP=null;
	private int searchByIdto=-1;
	private int searchByIdtoSup=-1;
	
	public Integer getIDTO() {
		return IDTO;
	}
	public void setIDTO(Integer idto) {
		IDTO = idto;
	}
	public Integer getIDTOSUP() {
		return IDTOSUP;
	}
	public void setIDTOSUP(Integer idtosup) {
		IDTOSUP = idtosup;
	}
	
	public int getSearchByIdto() {
		return searchByIdto;
	}
	public void setSearchByIdto(int searchByIdto) {
		this.searchByIdto = searchByIdto;
	}
	public int getSearchByIdtoSup() {
		return searchByIdtoSup;
	}
	public void setSearchByIdtoSup(int searchByIdtoSup) {
		this.searchByIdtoSup = searchByIdtoSup;
	}
	
	
	public String toString(){
		String stringfact = "";
		
		stringfact += "\n\t (hierarchy ";
		if(this.getIDTO() == null)
			stringfact += "(IDTO nil )";
		else
			stringfact += "(IDTO " + this.getIDTO() + " )";
		
		if(this.getIDTOSUP() == null)
			stringfact += "(IDTOSUP nil ))";
		else
			stringfact += "(IDTOSUP " + this.getIDTOSUP() + " ))";
		return stringfact;
	}
}
