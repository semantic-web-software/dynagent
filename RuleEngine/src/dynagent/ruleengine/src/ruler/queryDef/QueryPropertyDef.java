package dynagent.ruleengine.src.ruler.queryDef;



public class QueryPropertyDef {
	

	private Integer PROP=null; 
	private String NAME=null;
	private int searchByProp=-1;
	private int searchByName=-1;
	
	public String getNAME() {
		return NAME;
	}

	

	public void setNAME(String name) {
		NAME = name;
	}

	

	public Integer getPROP() {
		return PROP;
	}

	public void setPROP(Integer prop) {
		PROP = prop;
	}

	
	public int getSearchByName() {
		return searchByName;
	}

	public void setSearchByName(int searchByName) {
		this.searchByName = searchByName;
	}

	public int getSearchByProp() {
		return searchByProp;
	}

	public void setSearchByProp(int searchByProp) {
		this.searchByProp = searchByProp;
	}
	

	public String toString(){
		String stringfact = "";
		
		stringfact += "\n\t (property ";
		stringfact += "( NAME " + this.getNAME() + " )";
		
	
		if(this.getPROP() == null)
			stringfact += "( PROP nil )";
		else
			stringfact += "( PROP " + this.getPROP() + " )";
	
		return stringfact;
		
	}
	
}
