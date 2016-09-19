package dynagent.common.communication;

public class AttribValue {
	private String attribute;
	private String value;
	
	public AttribValue (String attribute, String value) {
		this.attribute = attribute;
		this.value = value;
	}
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
