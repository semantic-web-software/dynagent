package dynagent.server.services.reports;

public class FieldJrxml {
	private String name=null;
	private String fieldClass=null;
	
	public FieldJrxml(){
		
	}
	public FieldJrxml(String name, String fieldClass){
		
		this.name=name;
		this.fieldClass=fieldClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFieldClass() {
		return fieldClass;
	}

	public void setFieldClass(String fieldClass) {
		this.fieldClass = fieldClass;
	}
	public String toString(){
		return "<field name=\""+this.name+"\" class=\"java.lang."+this.fieldClass+"\"/>";
	}
}
