package dynagent.tools.owl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.drools.FactHandle;


import dynagent.common.basicobjects.IModel;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.ruler.IPropertyChangeDrools;

   public class TripletaPropiedadClase implements IPropertyChangeDrools , IModel {
	private String CLASSNAME= null;
	private String PROPNAME= null;
	private String RANGENAME= null;
	private String VALUE= null;
	private String OP= null;	
	private Double QMIN=null;
	private Double QMAX=null;	

	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FactHandle factHandle = null;
	
	public TripletaPropiedadClase(String clase,String propiedad,String value,String rango,Double qmin, Double qmax,String op) 
	{
		this.CLASSNAME = clase;
		this.RANGENAME=rango;
		this.PROPNAME=propiedad;
		this.OP=op;
		this.VALUE=value;
		this.QMAX=qmax;
		this.QMIN=qmin;
	}
	
	public String getCLASSNAME()
	{
		return CLASSNAME;	
	}

	public void setCLASSNAME(String name)
	{
		String antig = this.CLASSNAME;
		this.CLASSNAME = name;
		pcs.firePropertyChange("CLASSNAME", antig, name);
	}

	public void setOP(String op) {	
		String antig = this.getOP();
		this.OP=op;
		pcs.firePropertyChange("OP", antig, op);
	}

	public String getOP()
	{
		return this.OP;
	}


	public String getVALUE()
	{
		return this.VALUE;
	}

	public void setVALUE(String value) 
	{
		String antig=this.getVALUE();
		this.VALUE=value;
		pcs.firePropertyChange("VALUE",antig,value);
	}

	public String toString()
	{
		String stringfact="";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(PROPNAME=" + this.getPROPNAME()+")";		
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		stringfact += "(QMIN="+this.getQMIN()+")";
		stringfact += "(QMAX="+ this.getQMAX()+")";
		stringfact += "(OP="+this.getOP()+")";
		stringfact += "(VALUE="+this.getVALUE()+")";
		return  stringfact;
	}

		
	public Double getQMIN()
	{
		return this.QMIN;
	}

	public Double getQMAX()
	{
		return this.QMAX;
	}

	public void setQMIN(Double qmin) {
		
		Double antig=this.getQMIN();
		this.QMIN=qmin;
		pcs.firePropertyChange("QMIN",antig,qmin);
	}

	public void setQMAX(Double qmax) {
		Double antig=this.getQMAX();
		this.QMAX=qmax;
		pcs.firePropertyChange("QMAX",antig,qmax);
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

	public String  getPROPNAME()
	{
		return this.PROPNAME;
	}
	
	public String getRANGENAME() 
	{
		return this.RANGENAME;
	}
	
	public void setRANGENAME(String rangeName) {
		String antig = this.getRANGENAME();
		this.RANGENAME=rangeName;
		pcs.firePropertyChange("RANGENAME", antig, rangeName);
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		// TODO Auto-generated method stub
		return null;
	}

}