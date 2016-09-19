package dynagent.common.basicobjects;

public class AreaFunc {
	private String name;

	
	public AreaFunc(){}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return "(AREA_FUNC (NAME "+this.name+"))";
	}
	
}
