package dynagent.common.basicobjects;

public class AreaFuncRes {
	private String activo;
	private String area_func;
	private Integer ido_area_func;
	public AreaFuncRes(){
		
	}
	public String getActivo() {
		return activo;
	}
	public void setActivo(String activo) {
		this.activo = activo;
	}
	public String getArea_func() {
		return area_func;
	}
	public void setArea_func(String area_func) {
		this.area_func = area_func;
	}
	public Integer getIdo_area_func() {
		return ido_area_func;
	}
	public void setIdo_area_func(Integer idto_area_func) {
		this.ido_area_func = idto_area_func;
	}
	public String toString(){
		return "(AREA_FUNC_RES (NAME "+area_func+")(ACTIVE "+activo+"))";
	}
}
