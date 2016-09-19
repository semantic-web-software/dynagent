package dynagent.common.basicobjects;

public class Roles {
	
	private Integer idoRol;
	private String nameRol;
	private String area;
	
	public Roles() {}
	
	public Roles(Integer idoRol, String nameRol, String area) {
		this.idoRol = idoRol;;
		this.nameRol = nameRol;
		this.area = area;
	}
	
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}

	public Integer getIdoRol() {
		return idoRol;
	}
	public void setIdoRol(Integer idoRol) {
		this.idoRol = idoRol;
	}

	public String getNameRol() {
		return nameRol;
	}
	public void setNameRol(String nameRol) {
		this.nameRol = nameRol;
	}

	public String toString()
	{
		return "(ROLES (IDO_ROL "+this.idoRol+") (NAME_ROL "+ this.nameRol + ") (AREA "+ this.area + "))";
	}

}
