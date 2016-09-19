package dynagent.tools.owl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.drools.FactHandle;

import dynagent.common.basicobjects.IHerencia;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;


public class Herencia implements IPropertyChangeDrools, IHerencia{
	private String CLASS=null;
	private String CLASSSUP=null;
	private FactHandle factHandle;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	
	public Herencia(String className,String classNameUp){
		this.CLASSSUP=classNameUp;
		this.CLASS=className;
	}

	
	public FactHandle getFactHandle()
	{
		return factHandle;
	}
	public String toString(){
		String stringfact = "";
		stringfact += "\n\t (herencia ";
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
	
	public String getCLASSSUP() {
		return CLASSSUP;
	}
	


	public Object clone(IKnowledgeBaseInfo ik) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
