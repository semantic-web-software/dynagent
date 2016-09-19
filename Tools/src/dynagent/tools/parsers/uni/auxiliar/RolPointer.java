package dynagent.tools.parsers.uni.auxiliar;

public class RolPointer {
	
	private String nombre;
	private String rol;
	private String relacion;
	int qMin = -1;
	int qMax = -1;
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getRelacion() {
		return relacion;
	}
	public void setRelacion(String relacion) {
		this.relacion = relacion;
	}
	public String getRol() {
		return rol;
	}
	public void setRol(String rol) {
		this.rol = rol;
	}
	public int getQMax() {
		return qMax;
	}
	public void setQMax(int max) {
		qMax = max;
	}
	public int getQMin() {
		return qMin;
	}
	public void setQMin(int min) {
		qMin = min;
	}
	
	public String toString(){
		return "Rol Pointer: "+nombre+" -> RolB "+rol+" , Relacion "+relacion;
	}
	
}
