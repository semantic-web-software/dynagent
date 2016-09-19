package dynagent.tools.parsers.access;


public class Permiso {
	private String permiso;
	private boolean denegado=false;
	
	public boolean getIsDenegado() {
		return denegado;
	}
	public void setIsDenegado(boolean denegado) {
		this.denegado = denegado;
	}
	public String getPermiso() {
		return permiso;
	}
	public void setPermiso(String permiso) {
		this.permiso = permiso;
	}
	
	public String toString(){
		return permiso +":"+denegado; 
	}
	
}
