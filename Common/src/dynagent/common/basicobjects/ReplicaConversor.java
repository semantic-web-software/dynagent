package dynagent.common.basicobjects;

public class ReplicaConversor {

	private Integer idTo;
	private Integer prop;
	private Integer propLink;
	private Integer idtoSup;
	private Integer propPrefix;
	private String prefix;
	private String valueFixed;
	private Integer valueClsFixed;
	private Integer idtoDest;
	private Integer propDest;
	private boolean download;
	
	public ReplicaConversor() {}
	
	public ReplicaConversor(Integer idTo, Integer prop, Integer propLink, Integer idtoSup, 
			Integer propPrefix, String prefix, String valueFixed, Integer valueClsFixed, 
			Integer idtoDest, Integer propDest, boolean download) {
		this.idTo = idTo;
		this.prop = prop;
		this.propLink = propLink;
		this.idtoSup = idtoSup;
		this.propPrefix = propPrefix;
		this.prefix = prefix;
		this.valueFixed = valueFixed;
		this.valueClsFixed = valueClsFixed;
		this.idtoDest = idtoDest;
		this.propDest = propDest;
		this.download = download;
	}

	public Integer getIdTo() {
		return idTo;
	}

	public void setIdTo(Integer idTo) {
		this.idTo = idTo;
	}

	public Integer getIdtoSup() {
		return idtoSup;
	}

	public void setIdtoSup(Integer idtoSup) {
		this.idtoSup = idtoSup;
	}

	public Integer getPropLink() {
		return propLink;
	}

	public void setPropLink(Integer propLink) {
		this.propLink = propLink;
	}

	public boolean isDownload() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download = download;
	}

	public Integer getIdtoDest() {
		return idtoDest;
	}

	public void setIdtoDest(Integer idtoDest) {
		this.idtoDest = idtoDest;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public Integer getProp() {
		return prop;
	}

	public void setProp(Integer prop) {
		this.prop = prop;
	}

	public Integer getPropDest() {
		return propDest;
	}

	public void setPropDest(Integer propDest) {
		this.propDest = propDest;
	}

	public Integer getPropPrefix() {
		return propPrefix;
	}

	public void setPropPrefix(Integer propPrefix) {
		this.propPrefix = propPrefix;
	}

	public Integer getValueClsFixed() {
		return valueClsFixed;
	}

	public void setValueClsFixed(Integer valueClsFixed) {
		this.valueClsFixed = valueClsFixed;
	}

	public String getValueFixed() {
		return valueFixed;
	}

	public void setValueFixed(String valueFixed) {
		this.valueFixed = valueFixed;
	}
	
	public String toString() {
		return "idTo " + idTo + ", prop " + prop + ", propLink " + propLink + ", idtoSup " + idtoSup
		+ ", propPrefix " + propPrefix + ", prefix " + prefix + ", valueFixed " + valueFixed
		+ ", valueClsFixed " + valueClsFixed + ", idtoDest " + idtoDest + ", propDest " + propDest
		+ ", download " + download;
	}

}
