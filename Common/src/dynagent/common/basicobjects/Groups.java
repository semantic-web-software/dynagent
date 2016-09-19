package dynagent.common.basicobjects;

public class Groups {
	private Integer idGroup;/*Mantenido por compatibilidad con codigo anterior a modelo relacional. Se podria gestionar con el name ya que es único*/
	private Integer idProp;
	private String propName;
	private Integer uTask;
	private String uTaskName;
	private Integer idtoClass;
	private String className;
	private String groupName;
	private Integer order;
	
	public Groups(){
		
	}
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	
	public Integer getUTask() {
		return uTask;
	}
	public void setUTask(Integer task) {
		uTask = task;
	}
	public String toString(){
		return "(GROUP (GROUP "+this.groupName+")(CLASS "+this.idtoClass+")(PROPERTY "+this.idProp+")(UTASK "+this.uTask+")(ORDER "+this.order+"))";
	}
	public Integer getIdtoClass() {
		return idtoClass;
	}
	public void setIdtoClass(Integer idtoClass) {
		this.idtoClass = idtoClass;
	}
	
	public Groups clone(){
		Groups groups=new Groups();
		groups.setIdProp(idProp);
		groups.setPropName(propName);
		groups.setIdtoClass(idtoClass);
		groups.setClassName(className);
		groups.setNameGroup(groupName);
		groups.setUTask(uTask);
		groups.setUTaskName(uTaskName);
		groups.setOrder(order);
		
		return groups;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getNameGroup() {
		return groupName;
	}
	public void setNameGroup(String groupName) {
		this.groupName = groupName;
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

	public Integer getIdGroup() {
		return idGroup;
	}
	public void setIdGroup(Integer idGroup) {
		this.idGroup = idGroup;
	}
	public Integer getOrder() {
		return order;
	}
	
	public void setOrder(Integer order) {
		this.order=order;
	}
}
