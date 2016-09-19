package dynagent.common.basicobjects;

public class Configuration {
	
	private String label;
	private String value;

	public Configuration() {}
	
	public Configuration(String label, String value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
