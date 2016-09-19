package dynagent.server.applicationMessages;

public class AtomicEventInfo {
	private int id;
	private String type;
	private String nameProp;
	private String value;
	private String oldValue;
	private String operation;
	
	public AtomicEventInfo(int id, String type, String nameProp, String value, String oldValue, String operation) {
		this.id = id;
		this.type = type;
		this.nameProp = nameProp;
		this.value = value;
		this.oldValue = oldValue;
		this.operation = operation;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getNameProp() {
		return nameProp;
	}
	public void setNameProp(String nameProp) {
		this.nameProp = nameProp;
	}

	public String getOldValue() {
		return oldValue;
	}
	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
