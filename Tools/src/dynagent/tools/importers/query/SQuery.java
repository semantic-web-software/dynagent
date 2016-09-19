package dynagent.tools.importers.query;

public class SQuery {
	private String name;
	private String query;
	
	public SQuery(){
		
	}
	public SQuery(String name, String query){
		this.name=name;
		this.query=query;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	public String toString(){
		String result="";
		result="(SQUERY (NAME "+this.name+") (QUERY "+this.query+"))";
		return result;
	}
}
	
