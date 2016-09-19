package dynagent.server.database;

public class Individual {

	private Integer ido;
	private String rdn;
	
	public Individual(Integer ido, String rdn) {
		this.ido = ido;
		this.rdn = rdn;
	}

	public Integer getIdo() {
		return ido;
	}

	public void setIdo(Integer ido) {
		this.ido = ido;
	}

	public String getRdn() {
		return rdn;
	}

	public void setRdn(String rdn) {
		this.rdn = rdn;
	}
	
	
}
