package dynagent.server.services.reports;



public class PropertyJrxml {
	private String name=null;
	private String value=null;
	
	public PropertyJrxml(String name, String value){
		this.value=value;
		this.name=name;
	}
	public PropertyJrxml(){
		
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public String toString(){
		return "<property name=\""+this.name+"\" value=\""+this.value+"\" />";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
