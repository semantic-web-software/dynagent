package dynagent.common.basicobjects;

public class PropertyForClass implements Cloneable{
	private String prop;
	private Integer idProp;
	private String className;
	private Integer idtoClass;
	private boolean excluded;
	
	public PropertyForClass(){}
	
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
		return "(PROPERTY (PROP "+prop+")(CLASS "+className+")(EXCLUDED "+excluded+"))";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		PropertyForClass propInClass=new PropertyForClass();
		propInClass.setClassName(className);
		propInClass.setIdtoClass(idtoClass);
		propInClass.setProp(prop);
		propInClass.setIdProp(idProp);
		propInClass.setExcluded(excluded);
		return propInClass;
	}

	public boolean isExcluded() {
		return excluded;
	}

	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}
}