package dynagent.server.services.reports;

public class ParameterJrxml {
	private String name=null;
	private String parameterClass=null;
	private boolean isForPrompting=false;
	
	
	public ParameterJrxml(String name, String parameterClass, boolean isForPrompting){
		this.isForPrompting=isForPrompting;
		this.name=name;
		this.parameterClass=parameterClass;
	}
		
	public boolean isForPrompting() {
		return isForPrompting;
	}
	public void setForPrompting(boolean isForPrompting) {
		this.isForPrompting = isForPrompting;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParameterClass() {
		return parameterClass;
	}
	public void setParameterClass(String parameterClass) {
		this.parameterClass = parameterClass;
	}
	
	public String toString(){
		if (this.isForPrompting){
			return "<parameter name=\""+this.name+"\" isForPrompting=\"true\" class=\""+this.parameterClass+"\" />";
		}else{
			return "<parameter name=\""+this.name+"\" isForPrompting=\"false\" class=\""+this.parameterClass+"\" />";
		}
	}
}
