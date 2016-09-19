package dynagent.common.basicobjects;

public class O_Reg_Instancias_Index {

	private Integer autonum;
	private Integer id_o;
	private Integer id_to;
	private String rdn;

	public O_Reg_Instancias_Index() {}
	
	public O_Reg_Instancias_Index(Integer id_to, String rdn) {
		this.id_to = id_to;
		this.rdn = rdn;
	}
	public O_Reg_Instancias_Index(Integer id_to, Integer id_o, String rdn) {
		this.id_to = id_to;
		this.id_o = id_o;
		this.rdn = rdn;
	}
	
	public Integer getAutonum() {
		return autonum;
	}
	public void setAutonum(Integer autonum) {
		this.autonum = autonum;
	}

	public Integer getId_o() {
		return id_o;
	}
	public void setId_o(Integer id_o) {
		this.id_o = id_o;
	}

	public Integer getId_to() {
		return id_to;
	}
	public void setId_to(Integer id_to) {
		this.id_to = id_to;
	}

	public String getRdn() {
		return rdn;
	}
	public void setRdn(String rdn) {
		this.rdn = rdn;
	}

}
