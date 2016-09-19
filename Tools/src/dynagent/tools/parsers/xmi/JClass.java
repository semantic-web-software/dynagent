package dynagent.tools.parsers.xmi;

/***
 * Class
 */
public class JClass {
	private String type;
	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getPrefix(){
		String prefix = "";
		if(this.getType().equals("owl"))
			prefix = "prefijoMeta";
		else if(this.getType().equals("role"))
			prefix = "prefijoRol";
		else if(this.getType().equals("process"))
			prefix = "prefijoProcess";
		return prefix;
	}
}
