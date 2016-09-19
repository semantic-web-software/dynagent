package dynagent.common.basicobjects;

public class Required {
	private String prop;
	private Integer idProp;
	private String className;
	private Integer idtoClass;
	
	public Required(){}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	public Integer getIdtoClass() {
		return idtoClass;
	}
	public void setIdtoClass(Integer idtoClass) {
		this.idtoClass = idtoClass;
	}
	public String getProp() {
		return prop;
	}
	public void setProp(String prop) {
		this.prop = prop;
	}
	public String toString(){
		return "(REQUIRED (PROP "+prop+")(IDPROP "+idProp+")(CLASS "+className+")(IDTO "+idtoClass+"))";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Required req=new Required();
		req.setClassName(className);
		req.setIdtoClass(idtoClass);
		req.setProp(prop);
		req.setIdProp(idProp);
		return req;
	}
}
