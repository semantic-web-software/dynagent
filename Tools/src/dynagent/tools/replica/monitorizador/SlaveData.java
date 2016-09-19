package dynagent.tools.replica.monitorizador;

public class SlaveData {
	private String name;
	private Integer business;
	private String databaseIP;
	private Integer port;
	private String hostname;
	private String sufix;
	private Integer masterId;
	
	public SlaveData(String name, Integer business, String databaseIP, Integer port, String hostname, String sufix, Integer masterId) {
		this.name = name;
		this.business = business;
		this.databaseIP = databaseIP;
		this.port = port;
		this.hostname = hostname;
		this.sufix = sufix;
		this.masterId = masterId;
	}
	
	public String getName() {
		return name;
	}
	public Integer getBusiness() {
		return business;
	}
	public String getDatabaseIP() {
		return databaseIP;
	}
	public String getHostname() {
		return hostname;
	}
	public Integer getMasterId() {
		return masterId;
	}
	public Integer getPort() {
		return port;
	}
	public String getSufix() {
		return sufix;
	}
}
