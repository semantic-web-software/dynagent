package dynagent.common.basicobjects;

public class UsuarioRoles {
	
	private String usuario;
	private String rol;
	private Integer idoUsuario;
	private Integer idoRol;
	
	public UsuarioRoles() {}
	
	public UsuarioRoles(Integer idoUsuario, Integer idoRol) {
		this.idoUsuario = idoUsuario;
		this.idoRol = idoRol;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Integer getIdoRol() {
		return idoRol;
	}
	public void setIdoRol(Integer idoRol) {
		this.idoRol = idoRol;
	}
	
	public Integer getIdoUsuario() {
		return idoUsuario;
	}
	public void setIdoUsuario(Integer idoUsuario) {
		this.idoUsuario = idoUsuario;
	}
	
	public String toString()
	{
		return "(USUARIO_ROLES (USUARIO "+this.usuario+") (ROL "+ this.rol + ") (IDO_USUARIO "+this.idoUsuario+") (IDO_ROL "+ this.idoRol + "))";
	}

}
