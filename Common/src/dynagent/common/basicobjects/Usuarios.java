package dynagent.common.basicobjects;

public class Usuarios {
	
	private Integer idoUsuario;
	private String login;
	private String pwd;
	private String nombre;
	private String apellidos;
	private String organizacion;
	private String grupo;
	private String mail;
	private String dominio;
	
	public Usuarios() {}
	
	public Usuarios(Integer idoUsuario, String login, String pwd, String nombre, String apellidos, 
			String organizacion, String grupo, String mail, String dominio) {
		this.idoUsuario = idoUsuario;
		this.login = login;
		this.pwd = pwd;
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.organizacion = organizacion;
		this.grupo = grupo;
		this.mail = mail;
		this.dominio = dominio;
	}
	
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}

	public Integer getIdoUsuario() {
		return idoUsuario;
	}
	public void setIdoUsuario(Integer idoUsuario) {
		this.idoUsuario = idoUsuario;
	}
	
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getApellidos() {
		return apellidos;
	}
	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}
	
	public String getDominio() {
		return dominio;
	}
	public void setDominio(String dominio) {
		this.dominio = dominio;
	}
	
	public String getGrupo() {
		return grupo;
	}
	public void setGrupo(String grupo) {
		this.grupo = grupo;
	}
	
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	
	public String getOrganizacion() {
		return organizacion;
	}
	public void setOrganizacion(String organizacion) {
		this.organizacion = organizacion;
	}
	
	public String toString()
	{
		return "(USUARIOS (IDO_USUARIO "+this.idoUsuario+") (LOGIN "+this.login+") (PWD "+ this.pwd + ") (NOMBRE "+ this.nombre + ") (APELLIDOS "+ this.apellidos + ") (ORGANIZACION "+ this.organizacion + ") (GRUPO "+ this.grupo + ")" +
				" (MAIL "+ this.mail + ") (DOMINIO "+ this.dominio + "))";
	}

}
