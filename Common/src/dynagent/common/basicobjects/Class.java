package dynagent.common.basicobjects;

import java.util.ArrayList;

public class Class {
	private Integer idtoClass;
	private String className;
	private Integer idtoClassParent;
	private String classNameParent;
	private ArrayList<Properties> properties;
	private boolean excluded;
	
	public boolean isExcluded() {
		return excluded;
	}
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public ArrayList<Properties> getProperties() {
		return properties;
	}
	public void setProperties(ArrayList<Properties> properties) {
		this.properties = properties;
	}
	public String getClassNameParent() {
		return classNameParent;
	}
	public void setClassNameParent(String classNameParent) {
		this.classNameParent = classNameParent;
	}
	@Override
	public String toString() {
		return "(CLASS (NAME "+className+")(CLASSPARENT "+classNameParent+")(EXCLUDED "+excluded+"))";
	}
	public Integer getIdtoClass() {
		return idtoClass;
	}
	public void setIdtoClass(Integer idtoClass) {
		this.idtoClass = idtoClass;
	}
	public Integer getIdtoClassParent() {
		return idtoClassParent;
	}
	public void setIdtoClassParent(Integer idtoClassParent) {
		this.idtoClassParent = idtoClassParent;
	}
	
	

}
