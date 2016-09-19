package dynagent.tools.owl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.drools.FactHandle;

import dynagent.common.basicobjects.IndividualValue;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.values.Value;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;



public class ValorIndividuo implements IndividualValue , IPropertyChangeDrools{
	private String valor=null;
	private String ID=null;
	private String propname=null;
	private String classname=null;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FactHandle factHandle;
	
	public  ValorIndividuo(String ID,String clase,String propiedad,String valor){
		this.ID=ID;
		this.classname=clase;
		this.propname=propiedad;
		this.valor=valor;
		
	}
	
	@Override
	public String getVALOR() {
		return this.valor;
	}

	@Override
	public Integer getIDO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPROPNAME() throws NotFoundException {
		return this.propname;
	}

	@Override
	public String getCLASSNAME() {
		return this.classname;
	}

	@Override
	public boolean initialValuesChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getINITIALVALOR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDESTINATIONSYSTEM() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getLEVEL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toStringAmpliado() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCHANGED() {
		// TODO Auto-generated method stub
		return false;
	}



	@Override
	public String getID() {
		return this.ID;
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
	
	public FactHandle getFactHandle()
	{
		return factHandle;
	}

	public void setFactHandle(FactHandle factHandle)
	{
		this.factHandle = factHandle;
	}

	@Override
	public Object clone(IKnowledgeBaseInfo ik) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString(){
		try {
			return "(ValorIndividuo (ID="+this.getID()+")(CLASSNAME="+this.getCLASSNAME()+")(PROPNAME="+this.getPROPNAME()+")(VALOR="+this.getVALOR()+"))";
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		
	}

	@Override
	public String getPREVALOR() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFIRSTVALUE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getIDTO() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Value getCVALUE() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getChangeTime() {
		// TODO Auto-generated method stub
		return 0;
	}


}
