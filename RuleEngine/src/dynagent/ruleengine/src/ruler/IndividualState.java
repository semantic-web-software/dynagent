package dynagent.ruleengine.src.ruler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;

import org.drools.FactHandle;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public abstract class IndividualState extends SessionsData implements IPropertyChangeDrools {
			
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	protected int IDO;
	protected int IDTO;
	protected String CLASSNAME;
	protected int LEVEL;
	protected String STATE;
	protected FactHandle factHandle = null;
	
	protected IKnowledgeBaseInfo ik;
	
	public Integer getIDO() {
		return IDO;
	}
	
	public String getID(){
		//System.err.println("\n DEBUG INDIVUDAL "+this);
		return String.valueOf(this.getIDO());
	}

	public void setIDO(Integer ido) {
		IDO = ido;
	}

	public Integer getIDTO() {
		return IDTO;
	}

	public void setIDTO(Integer idto) {
		IDTO = idto;
	}

	public FactHandle getFactHandle() {
		return factHandle;
	}

	public void setFactHandle(FactHandle factHandle) {
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
	
	public String getCLASSNAME() {
		return CLASSNAME;
	}
	
	public void setCLASSNAME(String classname) {
		CLASSNAME = classname;
	}

	public int getLEVEL() {
		return LEVEL;
	}

	public void setLEVEL(int level) {
		LEVEL = level;
	}
	
	public abstract String getSTATE();


	public abstract void setSTATE(String state);
	
}
