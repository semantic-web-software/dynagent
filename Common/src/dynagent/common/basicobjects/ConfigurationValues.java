package dynagent.common.basicobjects;

public class ConfigurationValues {

	private String sufixTienda;
	private String miAlmacen;
	private String IPCentral;
	private int portCentral;
	private int businessCentral;
	
	public ConfigurationValues(String sufixTienda, String miAlmacen, String IPCentral, int portCentral, int businessCentral) {
		this.sufixTienda = sufixTienda;
		this.miAlmacen = miAlmacen;
		this.IPCentral = IPCentral;
		this.portCentral = portCentral;
		this.businessCentral = businessCentral;
	}
	
	public String getIPCentral() {
		return IPCentral;
	}
	public int getPortCentral() {
		return portCentral;
	}
	public String getSufixTienda() {
		return sufixTienda;
	}
	public int getBusinessCentral() {
		return businessCentral;
	}
	public String getMiAlmacen() {
		return miAlmacen;
	}

	public void setBusinessCentral(int businessCentral) {
		this.businessCentral = businessCentral;
	}
	public void setIPCentral(String central) {
		IPCentral = central;
	}
	public void setMiAlmacen(String miAlmacen) {
		this.miAlmacen = miAlmacen;
	}
	public void setPortCentral(int portCentral) {
		this.portCentral = portCentral;
	}
	public void setSufixTienda(String sufixTienda) {
		this.sufixTienda = sufixTienda;
	}

	public String toString() {
		return "sufixTienda: " + this.sufixTienda + ", miAlmacen: " + this.miAlmacen + ", IPCentral: " +this.IPCentral + ", portCentral: " + this.portCentral + ", businessCentral: " + this.businessCentral;
	}
}
