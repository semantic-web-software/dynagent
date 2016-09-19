package dynagent.common.utils;

public class EmailConfiguration {
	
	private int puerto_SMTP;
	private int idoMiempresa;
	private String email_remitente;
	private boolean autenticar;
	private Email.TipoConexionSegura seguridad_conexion;
	private String servidor_SMTP;
	private String nombre_remitente;
	private String password;
	
	public int getPuerto_SMTP() {
		return puerto_SMTP;
	}
	public void setPuerto_SMTP(int puerto_SMTP) {
		this.puerto_SMTP = puerto_SMTP;
	}
	public int getIdoMiempresa() {
		return idoMiempresa;
	}
	public void setIdoMiempresa(int idoMiempresa) {
		this.idoMiempresa = idoMiempresa;
	}
	public String getEmail_remitente() {
		return email_remitente;
	}
	public void setEmail_remitente(String email_remitente) {
		this.email_remitente = email_remitente;
	}
	public boolean isAutenticar() {
		return autenticar;
	}
	public void setAutenticar(boolean autenticar) {
		this.autenticar = autenticar;
	}
	public Email.TipoConexionSegura getSeguridad_conexion() {
		return seguridad_conexion;
	}
	public void setSeguridad_conexion(Email.TipoConexionSegura seguridad_conexion) {
		this.seguridad_conexion = seguridad_conexion;
	}
	public String getServidor_SMTP() {
		return servidor_SMTP;
	}
	public void setServidor_SMTP(String servidor_SMTP) {
		this.servidor_SMTP = servidor_SMTP;
	}
	public String getNombre_remitente() {
		return nombre_remitente;
	}
	public void setNombre_remitente(String nombre_remitente) {
		this.nombre_remitente = nombre_remitente;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
