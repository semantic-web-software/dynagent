package dynagent.gui.adapter.old;

public class InfoInstance {
	private Integer idto;
	private Integer ido;

	private Integer idProp;
	private Integer idRol;
	
	public InfoInstance(Integer ido,Integer idto,Integer idProp,Integer idRol){
		this.idto=idto;
		this.idProp=idProp;
		this.idRol=idRol;
		this.ido=ido;

	}
	
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public boolean equals(Object o){
		if(this.idto.equals(((InfoInstance)o).getIdto()) &&
				this.ido.equals(((InfoInstance)o).getIdo()) &&
				this.idProp.equals(((InfoInstance)o).getIdProp()) &&
				this.idRol.equals(((InfoInstance)o).getIdRol())){
			return true;
		}else{
			return false;
		}
	}

	public Integer getIdRol() {
		return idRol;
	}

	public void setIdRol(Integer idRol) {
		this.idRol = idRol;
	}

	public Integer getIdo() {
		return ido;
	}

	public void setIdo(Integer ido) {
		this.ido = ido;
	}
	
}
