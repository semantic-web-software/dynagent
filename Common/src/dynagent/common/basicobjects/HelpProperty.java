package dynagent.common.basicobjects;

public class HelpProperty {
	int idProp;
	String description;
	String language;
	
	public HelpProperty(){
		super();
	}
	
	public HelpProperty(int idProp, String description, String language) {
		super();
		this.idProp = idProp;
		this.description = description;
		this.language = language;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getIdProp() {
		return idProp;
	}
	public void setIdProp(int idProp) {
		this.idProp = idProp;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}
