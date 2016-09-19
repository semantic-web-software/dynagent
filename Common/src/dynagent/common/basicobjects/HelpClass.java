package dynagent.common.basicobjects;

public class HelpClass {
	int idto;
	String description;
	String language;
	
	public HelpClass(){
		super();
	}
	
	public HelpClass(int idto, String description, String language) {
		super();
		this.idto = idto;
		this.description = description;
		this.language = language;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getIdto() {
		return idto;
	}
	public void setIdto(int idto) {
		this.idto = idto;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
}
