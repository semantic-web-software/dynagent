package dynagent.common.basicobjects;

public class IndexFilter {
	private int ido;
	
	private String mascPrefixTemp=null;
	private Integer propPrefixTemp=null;
	private Integer contYear=null;
	private String lastPrefixTemp=null;

	private Integer propPrefix;
	private String prefix;
	private int index;
	private String sufix;
	private boolean globalSufix;
	private Integer propFilter;
	private String valueFilter;
	private boolean is;
	
	private Integer minDigits;
	private Integer miEmpresa=null;
	
	public IndexFilter() {
	}
	
	public IndexFilter(int ido, 
			String mascPrefixTemp, Integer propPrefixTemp, Integer contYear, String lastPrefixTemp,
			Integer propPrefix, String prefix, int index, String sufix, boolean globalSufix, 
			Integer minDigits, Integer propFilter, String valueFilter, Integer miEmpresa) {
		this.ido = ido;
		this.propFilter = propFilter;
		this.valueFilter = valueFilter;
		this.prefix = prefix;
		this.sufix = sufix;
		this.propPrefix = propPrefix;
		this.index = index;
		this.globalSufix = globalSufix;
		this.is = false;
		this.miEmpresa = miEmpresa;
		this.minDigits = minDigits;
		
		this.mascPrefixTemp=mascPrefixTemp;
		this.propPrefixTemp=propPrefixTemp;
		this.contYear=contYear;
		this.lastPrefixTemp=lastPrefixTemp;
	}

	public Integer getContYear() {
		return contYear;
	}
	public void setContYear(Integer contYear) {
		this.contYear = contYear;
	}
	public boolean isGlobalSufix() {
		return globalSufix;
	}
	public void setGlobalSufix(boolean globalSufix) {
		this.globalSufix = globalSufix;
	}
	public int getIdo() {
		return ido;
	}
	public void setIdo(int ido) {
		this.ido = ido;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public boolean getIs() {
		return is;
	}
	public void setIs(boolean is) {
		this.is = is;
	}
	public String getLastPrefixTemp() {
		return lastPrefixTemp;
	}
	public void setLastPrefixTemp(String lastPrefixTemp) {
		this.lastPrefixTemp = lastPrefixTemp;
	}
	public String getMascPrefixTemp() {
		return mascPrefixTemp;
	}
	public void setMascPrefixTemp(String mascPrefixTemp) {
		this.mascPrefixTemp = mascPrefixTemp;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public Integer getPropFilter() {
		return propFilter;
	}
	public void setPropFilter(Integer propFilter) {
		this.propFilter = propFilter;
	}
	public Integer getPropPrefix() {
		return propPrefix;
	}
	public void setPropPrefix(Integer propPrefix) {
		this.propPrefix = propPrefix;
	}
	public Integer getPropPrefixTemp() {
		return propPrefixTemp;
	}
	public void setPropPrefixTemp(Integer propPrefixTemp) {
		this.propPrefixTemp = propPrefixTemp;
	}
	public String getSufix() {
		return sufix;
	}
	public void setSufix(String sufix) {
		this.sufix = sufix;
	}
	public String getValueFilter() {
		return valueFilter;
	}
	public void setValueFilter(String valueFilter) {
		this.valueFilter = valueFilter;
	}
	public Integer getMiEmpresa() {
		return miEmpresa;
	}
	public void setMiEmpresa(Integer miEmpresa) {
		this.miEmpresa = miEmpresa;
	}
	public Integer getMinDigits() {
		return minDigits;
	}
	public void setMinDigits(Integer minDigits) {
		this.minDigits = minDigits;
	}

	public String toString() {
		return "(INDEX_FILTER (MASC_PREFIX_TEMP "+mascPrefixTemp+")(PROP_PREFIX_TEMP "+propPrefixTemp+")(CONT_YEAR "+contYear+")(LAST_PREFIX_TEMP "+lastPrefixTemp+")" +
		"(PREFIX "+prefix+")(PROPPREFIX "+propPrefix+")(INDEX "+index+")(SUFIX "+sufix+")" +
		"( MINDIGITS "+minDigits+")(GLOBALSUFIX "+globalSufix+")(PROPFILTER "+propFilter+")(VALUEFILTER "+valueFilter+")(MI_EMPRESA "+this.miEmpresa+"))";
	}

}
