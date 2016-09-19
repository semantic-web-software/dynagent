package dynagent.ruleengine.src.ruler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.drools.FactHandle;

import dynagent.common.basicobjects.IHerencia;
import dynagent.common.knowledge.IKnowledgeBaseInfo;


public class FactHierarchy implements IPropertyChangeDrools,IHerencia{
	private Integer IDTO=null;
	private Integer IDTOSUP=null;
	private String CLASS=null;
	private String CLASSSUP=null;
	private FactHandle factHandle;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	
	public FactHierarchy(int idto,int idtosup,String className,String classNameUp){
		this.setCLASSSUP(classNameUp);
		this.setCLASS(className);
		this.setIDTO(idto);
		this.setIDTOSUP(idtosup);
	}

	public Integer getIDTO() {
		return IDTO;
	}
	public void setIDTO(Integer idto) {
		Integer antig = getIDTO();
		this.IDTO = idto;
		pcs.firePropertyChange("IDTO", antig, idto);
		
		
		
		
	}
	public Integer getIDTOSUP() {
		return IDTOSUP;
	}
	public void setIDTOSUP(Integer idtosup) {
		Integer antig = getIDTOSUP();
		this.IDTOSUP = idtosup;
		pcs.firePropertyChange("IDTOSUP", antig, idtosup);
		
		
	}

	
	public FactHandle getFactHandle()
	{
		return factHandle;
	}
	public String toString(){
		String stringfact = "";
		stringfact += "\n\t (hierarchy ";
		stringfact += "(IDTO " + this.getIDTO() + " )";
		stringfact += "(IDTOSUP " + this.getIDTOSUP() + " )";
		stringfact += "(CLASS " + this.getCLASS() + " )";
		stringfact += "(CLASSSUP " + this.getCLASSSUP() + " ))";
		return stringfact;
	}
	
	public void setFactHandle(FactHandle factHandle)
	{
		this.factHandle = factHandle;
	}
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}
	
	public void removePropertyChangeListeners() {
		PropertyChangeListener[] p=pcs.getPropertyChangeListeners();
		for(int i=0;i<p.length;i++){
			removePropertyChangeListener(p[i]);
		}
	}
	public String getCLASS() {
		return CLASS;
	}
	public void setCLASS(String className) {
		CLASS = className;
	}
	public String getCLASSSUP() {
		return CLASSSUP;
	}
	public void setCLASSSUP(String classsup) {
		CLASSSUP = classsup;
	}
	public Object clone(IKnowledgeBaseInfo ik) {
		return new FactHierarchy(getIDTO(),getIDTOSUP(),getCLASS(),getCLASSSUP());
	}
}
