package dynagent.common.basicobjects;

public class ReplicaConfiguration {

	private Integer idTo;
	private Integer propLink;
	private Integer idtoSup;
	private boolean localOrigin;
	
	public ReplicaConfiguration() {}
	
	public ReplicaConfiguration(Integer idTo, Integer propLink, Integer idtoSup, boolean localOrigin) {
		this.idTo = idTo;
		this.propLink = propLink;
		this.idtoSup = idtoSup;
		this.localOrigin = localOrigin;
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

	public boolean isLocalOrigin() {
		return localOrigin;
	}

	public void setLocalOrigin(boolean localOrigin) {
		this.localOrigin = localOrigin;
	}

	public String toString() {
		return "idTo " + idTo + ", propLink " + propLink + ", idtoSup " + idtoSup + ", localOrigin " + localOrigin;
	}
}
