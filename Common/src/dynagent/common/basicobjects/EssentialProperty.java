package dynagent.common.basicobjects;

public class EssentialProperty implements Cloneable{
	private Integer uTask=null;
	private Integer idto=null;
	private Integer prop=null;
	
	private String uTaskName=null;
	private String idtoName=null;
	private String propName=null;
	
	public EssentialProperty(Integer uTask, Integer idto, Integer prop){
		this.uTask=uTask;
		this.idto=idto;
		this.prop=prop;
	}
	public EssentialProperty(){
		
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer aliasClass) {
		this.idto = aliasClass;
	}
	public Integer getProp() {
		return prop;
	}
	public void setProp(Integer prop) {
		this.prop = prop;
	}
	public Integer getUTask() {
		return uTask;
	}
	public void setUTask(Integer task) {
		uTask = task;
	}
	public String toString(){
		return "(ESSENTIALPROPERTY (IDTO "+this.idto+") (PROP "+this.prop+") (UTASK "+this.uTask+") "+
		"(IDTO NAME "+ this.idtoName+") (PROP NAME "+this.propName+") (UTASK NAME "+this.uTaskName+"))";
	}
	public String getIdtoName() {
		return idtoName;
	}
	public void setIdtoName(String idtoName) {
		this.idtoName = idtoName;
	}
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	public String getUTaskName() {
		return uTaskName;
	}
	public void setUTaskName(String taskName) {
		uTaskName = taskName;
	}
	@Override
	public Object clone() throws CloneNotSupportedException {
		EssentialProperty a=new EssentialProperty();
		a.setIdto(idto);
		a.setIdtoName(idtoName);
		a.setProp(prop);
		a.setPropName(propName);
		a.setUTask(uTask);
		a.setUTaskName(uTaskName);
		return a;
	}
}
