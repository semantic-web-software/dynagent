/***
 * FactProp.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.ruler;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;


import org.drools.FactHandle;

import dynagent.common.basicobjects.IPropiedad;
import dynagent.common.basicobjects.Properties;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class FactProp implements IPropertyChangeDrools, IPropiedad{
	
	
	private Integer CAT;
	Category category=null;
	private Integer PROP=null;    // Property identifier
	private String VALUE=null;
	private Integer VALUECLS=null;
	
	private String OP=null;
	private String NAME=null;
	private Float QMIN=null;
	private Float QMAX=null;
	private Integer PROPIN;
	private FactHandle factHandle;
	private IKnowledgeBaseInfo ik;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	
	public FactProp(String name,Integer prop,Integer valuecls,Integer cat,String inversa,Integer propinv,IKnowledgeBaseInfo ik){
		this.PROP=prop;
		this.NAME=name;
		this.VALUECLS=valuecls;
		this.CAT=cat;
		this.PROPIN=propinv;
		this.ik=ik;
		
	}
	
	
	public String getNAME() {
		return NAME;
	}
	
	public FactHandle getFactHandle()
	{
		return factHandle;
	}

	public void setFactHandle(FactHandle factHandle)
	{
		this.factHandle = factHandle;
	}
	
	public Float getQMAX() {
		return QMAX;
	}

	public void setQMAX(Float qmax) {
		QMAX = qmax;
	}

	public Float getQMIN() {
		return QMIN;
	}

	public void setQMIN(Float qmin) {
		QMIN = qmin;
	}

	public void setNAME(String name) {
		NAME = name;
	}

	public String getOP() {
		return OP;
	}

	public void setOP(String op) {
		OP = op;
	}

	public Integer getPROP() {
		return PROP;
	}

	public void setPROP(Integer prop) {
		PROP = prop;
	}

	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String value) {
		VALUE = value;
	}

	public Integer getVALUECLS() {
		return VALUECLS;
	}

	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}

	public Integer getCAT() {
		return CAT;
	}

	public void setCAT(Integer cat) {
		CAT = cat;
	}

	public String toString(){
		String stringfact = "";
		
		stringfact += "\n\t (property ";
		stringfact += "( NAME " + this.getNAME() + " )";
		
	
		if(this.getPROP() == null)
			stringfact += "( PROP nil )";
		else
			stringfact += "( PROP " + this.getPROP() + " )";
			
		if(this.getVALUE()==null)
			stringfact += "( VALUE nil )";
		else
			stringfact += "( VALUE " + this.getVALUE() + " )";
		
		if(this.getVALUECLS() == null)
			stringfact += "( VALUECLS nil )";
		else{
			if(this.getVALUECLS()==null)
				stringfact += "( VALUECLS nil )";
			else
				stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		}
		if(this.getQMIN() == null)
			stringfact += "( QMIN nil )";
		else
			stringfact += "( QMIN " + this.getQMIN() + " )";
		
		if(this.getQMAX() == null)
			stringfact += "( QMAX nil )";
		else
			stringfact += "( QMAX " + this.getQMAX() + " )";	
		
		if(this.getOP() == null)
			stringfact += "( OP nil ))";
		else
			stringfact += "( OP " + this.getOP() + " )";
		
		if(this.getCAT() == null)
			stringfact += "( CAT nil ))";
		else
			stringfact += "( CAT " + this.getCAT() + " ))";
		
		if(this.getPROPIN() == null)
			stringfact += "(PROPIN nil ))";
		else
			stringfact += "(PROPIN " + this.getPROPIN() + " ))";
		
		
		
		return stringfact;
		
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
	
	
	
	public String toPropertiesString(){
		String stringfact = "\n\t (property ";
		
		if(this.getPROP() == null)
			stringfact += "( PROP nil )";
		else
			stringfact += "( PROP " + this.getPROP() + " )";
		
		if(this.getNAME()==null)
			stringfact += "( NAME nil )";
		else
			stringfact += "( NAME " + this.getNAME() + " )";
		
		if(this.getVALUE()==null)
			stringfact += "( VALUE nil )";
		else
			stringfact += "( VALUE " + this.getVALUE() + " )";
		
		if(this.getVALUECLS()==null)
			stringfact += "( VALUECLS nil )";
		else
			stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		
		if(this.getCAT()==null)
			stringfact += "( CAT nil )";
		else{
			if(this.getCAT()==null)
				stringfact += "( CAT nil )";
			else
				stringfact += "( CAT " + this.getCAT() + " )";
		}
		
		if(this.getOP()==null)
			stringfact += "( OP nil )";
		else
			stringfact += "( OP " + this.getOP() + " )";
		
		if(this.getPROPIN() == null)
			stringfact += "(PROPIN nil ))";
		else
			stringfact += "(PROPIN " + this.getPROPIN() + " ))";
		
		if(this.getQMIN() == null)
			stringfact += "( QMIN nil )";
		else
			stringfact += "( QMIN " + this.getQMIN() + " )";
		
		if(this.getQMAX() == null)
			stringfact += "( QMAX nil ))";
		else
			stringfact += "( QMAX " + this.getQMAX() + " ))";
	
		return stringfact;
	}
	
	public Properties toProperties(){
		Properties p = new Properties();
		p.setCAT(this.getCAT());
		p.setNAME(this.getNAME());
//		p.setOP(this.getOP());
		p.setPROP(this.getPROP());
//		p.setQMAX(new Float(this.getQMAX()));
//		p.setQMIN(new Float(this.getQMIN()));
//		p.setVALUE(this.getVALUE());
		p.setVALUECLS(this.getVALUECLS());
		p.setPROPINV(this.getPROPIN());
		return p;
	}

	public Integer getPROPIN() {
		return PROPIN;
	}

	public void setPROPIN(Integer propin) {
		PROPIN = propin;
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		FactProp f=new FactProp(this.getNAME(),this.getPROP(),this.getVALUECLS(),this.getCAT(),this.getINVERSA(),this.getPROPIN(),ik);
		return f;
	}

	@Override
	public boolean isDATAPROPERTY() {
		return Category.isDataProperty(this.getCAT());
	}

	@Override
	public boolean isOBJECTPROPERTY() {
		return Category.isObjectProperty(this.getCAT());	}



	@Override
	public String getINVERSA() {
		try {
			if(this.getPROPIN()!=null)
				return ik.getPropertyName(this.getPROPIN());
			else return null;
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
}
