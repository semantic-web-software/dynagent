package dynagent.common.basicobjects;

import java.util.HashMap;

public class Range implements Cloneable{
	private Integer idtoAncestor;
	private String classNameAncestor;
	private Integer idtoClass;
	private Integer idProp;
	private HashMap<Integer,String> ranges;
	private String className;
	private String propName;
	
	public Range(){
		ranges=new HashMap<Integer, String>();
	}
	
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
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	public String getRangeName(Integer idtoRange) {
		return ranges.get(idtoRange);
	}
	
	public void addRange(Integer idtoRange,String rangeName) {
		ranges.put(idtoRange, rangeName);
	}
	
	public HashMap<Integer,String> getRanges(){
		return ranges;
	}
	
	public void setRanges(HashMap<Integer,String> ranges){
		this.ranges=ranges;
	}
	
	public String toString(){
		return "(RANGE (CLASS "+className+")(PROP "+propName+")(RANGE "+ranges.values()+"))";
	}
	
	public Object clone(){
		Range rg=new Range();
		rg.setIdtoClass(idtoClass);
		rg.setClassName(className);
		rg.setIdProp(idProp);
		rg.setPropName(propName);
		rg.setRanges((HashMap<Integer,String>)ranges.clone());
		
		return rg;
	}

	public String getClassNameAncestor() {
		return classNameAncestor;
	}

	public void setClassNameAncestor(String classNameAncestor) {
		this.classNameAncestor = classNameAncestor;
	}

	public Integer getIdtoAncestor() {
		return idtoAncestor;
	}

	public void setIdtoAncestor(Integer idtoAncestor) {
		this.idtoAncestor = idtoAncestor;
	}
}
