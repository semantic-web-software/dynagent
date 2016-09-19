package dynagent.server.services.reports;

public class ImportJrxml {
	
	private String value=null;
	
	public ImportJrxml(String value){
		this.value=value;
		
	}
	public ImportJrxml(){
		
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public String toString(){
		return "<import value=\""+this.value+"\" />";
	}

	
}
