package dynagent.tools.parsers.xmi;

/***
 * Subclass
 */
public class Subclass extends JClass{
	private String parent;
	private String child;
	
	public String getChild() {
		return child;
	}
	public void setChild(String child) {
		this.child = child;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
}