package dynagent.common.basicobjects;

public class Index extends IndexFilter {
	private int ido;
	private Integer idto=null;
	private String domain=null;
	private Integer property=null;
	
	private boolean single=false;
	
	public Index() {
	}
	
	public Index(int ido, Integer idto, Integer property, 
			String mascPrefixTemp, Integer propPrefixTemp, Integer contYear, String lastPrefixTemp,
			String prefix, Integer propPrefix, int index, 
			String sufix, boolean globalSufix, Integer minDigits, 
			Integer propFilter, String valueFilter, Integer miEmpresa) {
		super(ido, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp,
				propPrefix, prefix, index, sufix, globalSufix, minDigits, 
				propFilter, valueFilter, miEmpresa);
		this.idto=idto;
		this.property=property;
	}
	
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public Integer getProperty() {
		return property;
	}
	public void setProperty(Integer property) {
		this.property = property;
	}
	public void setSingle(boolean b) {
		this.single=b;
	}
	public boolean isSingle() {
		return single;
	}

	public String toString() {
		String stringIndexFilter = super.toString();
		
		return "(INDEX (IDO "+this.ido+")(IDTO "+this.idto+")(PROP "+this.property+")" +
		stringIndexFilter + "(SINGLE "+this.single+"))";
	}
}
