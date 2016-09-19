package dynagent.common.basicobjects;

import dynagent.common.Constants;

public class Alias implements Cloneable{
	public static String[] ALIAS_TYPES={Constants.CLS_ALIAS_PROPERTY,Constants.CLS_ALIAS_CLASS,Constants.CLS_ALIAS_GROUP,Constants.CLS_ALIAS_UTASK};
	
	private Integer uTask=null;
	private Integer group=null;
	private Integer idto=null;
	private Integer prop=null;
	
	private String uTaskName=null;
	private String groupName=null;
	private String idtoName=null;
	private String propName=null;
	
	private String alias=null;
	public Alias(Integer uTask, Integer group, Integer idto, Integer prop, String alias){
		this.uTask=uTask;
		this.group=group;
		this.alias=alias;
		this.idto=idto;
		this.prop=prop;
	}
	public Alias(){
		
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer aliasClass) {
		this.idto = aliasClass;
	}
	public Integer getGroup() {
		return group;
	}
	public void setGroup(Integer group) {
		this.group = group;
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
		return "(ALIAS (ALIAS "+this.alias+") (IDTO "+this.idto+") (GROUP "+this.group+") (PROP "+this.prop+") (UTASK "+this.uTask+") "+
		"(IDTO NAME "+ this.idtoName+") (GROUP NAME "+this.groupName+") (PROP NAME "+this.propName+") (UTASK NAME "+this.uTaskName+"))";
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
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
		Alias a=new Alias();
		a.setAlias(alias);
		a.setGroup(group);
		a.setGroupName(groupName);
		a.setIdto(idto);
		a.setIdtoName(idtoName);
		a.setProp(prop);
		a.setPropName(propName);
		a.setUTask(uTask);
		a.setUTaskName(uTaskName);
		return a;
	}
	
	public String getAliasType(){
		if(getProp()!=null || getPropName()!=null)
			return Constants.CLS_ALIAS_PROPERTY;
		else if(getIdto()!=null || getIdtoName()!=null)
			return Constants.CLS_ALIAS_CLASS;
		else if(getGroup()!=null || getGroupName()!=null)
			return Constants.CLS_ALIAS_GROUP;
		else if(getUTask()!=null || getUTaskName()!=null)
			return Constants.CLS_ALIAS_UTASK;
		
		System.err.println("WARNING: El alias "+this+" no casa con ninguno de los tipos predefinidos. Se devuelve el genérico "+Constants.CLS_ALIAS);
		return Constants.CLS_ALIAS;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Alias other = (Alias) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (idto == null) {
			if (other.idto != null)
				return false;
		} else if (!idto.equals(other.idto))
			return false;
		if (idtoName == null) {
			if (other.idtoName != null)
				return false;
		} else if (!idtoName.equals(other.idtoName))
			return false;
		if (prop == null) {
			if (other.prop != null)
				return false;
		} else if (!prop.equals(other.prop))
			return false;
		if (propName == null) {
			if (other.propName != null)
				return false;
		} else if (!propName.equals(other.propName))
			return false;
		if (uTask == null) {
			if (other.uTask != null)
				return false;
		} else if (!uTask.equals(other.uTask))
			return false;
		if (uTaskName == null) {
			if (other.uTaskName != null)
				return false;
		} else if (!uTaskName.equals(other.uTaskName))
			return false;
		return true;
	}
}
