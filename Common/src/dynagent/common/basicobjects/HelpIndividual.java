package dynagent.common.basicobjects;

public class HelpIndividual {
	int ido;
	int idto;
	String className;
	String description;
	String language;
	
	public HelpIndividual(){
		super();
	}
	
	public HelpIndividual(int ido, int idto, String className, String description, String language) {
		super();
		this.ido = ido;
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
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public int getIdo() {
		return ido;
	}
	public void setIdo(int ido) {
		this.ido = ido;
	}
	public int getIdto() {
		return idto;
	}
	public void setIdto(int idto) {
		this.idto = idto;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
}