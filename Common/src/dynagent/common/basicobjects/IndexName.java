package dynagent.common.basicobjects;

public class IndexName {
	private Integer idto=null;
	private String domain=null;
	private Integer property=null;
	private String propertyName=null;
	
	private String mascPrefixTemp=null;
	private Integer propPrefixTemp=null;
	private String propPrefixTempName=null;
	private Integer contYear=null;
	private String lastPrefixTemp=null;
	
	private String prefix=null;
	private Integer propPrefix=null;
	private String propPrefixName=null;
	private Integer index=null;
	private String sufix=null;
	private boolean globalSufix=false;
	private Integer minDigits=null;
	private Integer propFilter=null;
	private String propFilterName=null;
	private String valueFilter=null;
	private Integer miEmpresa=null;
	private String miEmpresaRdn=null;

	private boolean single=false;
	
	public IndexName(){
		
	}
	public IndexName( Integer idto, String domain, Integer property, String propertyName, 
			String mascPrefixTemp, Integer propPrefixTemp, String propPrefixTempName, Integer contYear, String lastPrefixTemp,
			String prefix, Integer propPrefix, String propPrefixName, Integer index, 
			String sufix, boolean globalSufix, Integer minDigits, 
			Integer propFilter, String propFilterName, String valueFilter, Integer miEmpresa, String miEmpresaRdn){
		this.idto=idto;
		this.domain = domain;
		this.property=property;
		this.propertyName=propertyName;
		this.index=index;
		this.prefix=prefix;
		this.sufix=sufix;
		this.propFilter=propFilter;
		this.propFilterName=propFilterName;
		this.valueFilter=valueFilter;
		this.propPrefix=propPrefix;
		this.propPrefixName=propPrefixName;
		this.globalSufix=globalSufix;
		this.mascPrefixTemp=mascPrefixTemp;
		this.propPrefixTemp=propPrefixTemp;
		this.propPrefixTempName=propPrefixTempName;
		this.contYear=contYear;
		this.lastPrefixTemp=lastPrefixTemp;
		this.miEmpresa=miEmpresa;
		this.miEmpresaRdn=miEmpresaRdn;
		this.minDigits=minDigits;
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
	public Integer getPropFilter() {
		return propFilter;
	}
	public void setPropFilter(Integer propFilter) {
		this.propFilter = propFilter;
	}
	public String getPropFilterName() {
		return propFilterName;
	}
	public void setPropFilterName(String propFilterName) {
		this.propFilterName = propFilterName;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
	public Integer getPropPrefix() {
		return propPrefix;
	}
	public void setPropPrefix(Integer propPrefix) {
		this.propPrefix = propPrefix;
	}
	public String getPropPrefixName() {
		return propPrefixName;
	}
	public void setPropPrefixName(String propPrefixName) {
		this.propPrefixName = propPrefixName;
	}
	public boolean isGlobalSufix() {
		return globalSufix;
	}
	public void setGlobalSufix(boolean globalSufix) {
		this.globalSufix = globalSufix;
	}
	public Integer getIndex() {
		return index;
	}
	public void setIndex(Integer index) {
		this.index = index;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public boolean isSingle() {
		return single;
	}
	public void setSingle(boolean single) {
		this.single = single;
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
	
	public Integer getContYear() {
		return contYear;
	}
	public void setContYear(Integer contYear) {
		this.contYear = contYear;
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
	public Integer getPropPrefixTemp() {
		return propPrefixTemp;
	}
	public void setPropPrefixTemp(Integer propPrefixTemp) {
		this.propPrefixTemp = propPrefixTemp;
	}
	public String getPropPrefixTempName() {
		return propPrefixTempName;
	}
	public void setPropPrefixTempName(String propPrefixTempName) {
		this.propPrefixTempName = propPrefixTempName;
	}
	public Integer getMiEmpresa() {
		return miEmpresa;
	}
	public void setMiEmpresa(Integer miEmpresa) {
		this.miEmpresa = miEmpresa;
	}
	public String getMiEmpresaRdn() {
		return miEmpresaRdn;
	}
	public void setMiEmpresaRdn(String miEmpresaRdn) {
		this.miEmpresaRdn = miEmpresaRdn;
	}
	public Integer getMinDigits() {
		return minDigits;
	}
	public void setMinDigits(Integer minDigits) {
		this.minDigits = minDigits;
	}
	public String toString(){
		return "(INDEX_NAME (IDTO "+this.idto+")(PROP "+this.property+")" +
				"(MASC_PREFIX_TEMP "+this.mascPrefixTemp+")(PROP_PREFIX_TEMP "+this.propPrefixTemp+")(CONT_YEAR "+this.contYear+")(LAST_PREFIX_TEMP "+this.lastPrefixTemp+")" +
				"(PREFIX "+this.prefix+")(PROPPREFIX "+this.propPrefix+")(INDEX "+this.index+")(SUFIX "+this.sufix+")" +
				"(GLOBALSUFIX "+this.globalSufix+")(MIN_DIGITS "+this.minDigits+")(PROPFILTER "+this.propFilter+")(VALUEFILTER "+this.valueFilter+")(MI_EMPRESA_RDN "+this.miEmpresaRdn+")(MI_EMPRESA "+this.miEmpresa+")(SINGLE "+this.single+"))";
	}
}
