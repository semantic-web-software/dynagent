/***
 * FactAccess.java
 * @author: Hassan Sleiman - 
 */

package dynagent.ruleengine.src.ruler;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.drools.FactHandle;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.ruleengine.src.sessions.SessionController;

public class FactAccess implements Sessionable, IPropertyChangeDrools{

	private Integer IDTO = null; // Object Class
	private Integer IDO = null; // Object Identifier
	private Integer PROP = null; // Property identifier
	private String VALUE = null;
	private Integer VALUECLS = null;
	private Integer TASK=null;
	private String USERROL=null;
	private String USER=null;
	private Integer DENNIED;
	private Integer ACCESSTYPE;
	private int PRIORITY;
	private FactHandle factHandle=null;
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private IKnowledgeBaseInfo ik;
	
	public FactAccess(Integer idto,Integer ido,Integer prop,String value,Integer valuecls,String userRol,String user, Integer usertask,Integer dennied,Integer accesstype, int priority, IKnowledgeBaseInfo ik){
		this.IDTO=idto;
		this.IDO=ido;
		this.PROP=prop;
		this.VALUE=value;
		this.VALUECLS=valuecls;
		this.USERROL=userRol;
		this.USER=user;
		this.TASK=usertask;
		this.DENNIED=dennied;
		this.ACCESSTYPE=accesstype;
		this.PRIORITY=priority;

		this.ik=ik;
		if(SessionController.getInstance().getActualSession(ik)!=null){
			SessionController.getInstance().getActualSession(ik).addSessionable(this);
		}

	}

	
	
	public String getACCESSNAME(){
		return Constants.getAccessTypeName(this.getACCESSTYPE());
		
	}
	
	
		
	public FactAccess(IKnowledgeBaseInfo ik) {

		if(SessionController.getInstance().getActualSession(ik)!=null)
			SessionController.getInstance().getActualSession(ik).addSessionable(this);

	}
	public FactHandle getFactHandle()
	{
		return factHandle;
	}
	
	public void setFactHandle(FactHandle factHandle)
	{
		this.factHandle = factHandle;
	}


	public void setACCESSTYPE(Integer accesstype) {
		Integer antig = getACCESSTYPE();
		ACCESSTYPE = accesstype;
		pcs.firePropertyChange("ACCESSTYPE", antig, accesstype);
	}
	public Integer getDENNIED() {
		
		return DENNIED;
	}
	public void setDENNIED(Integer dennied) {
		Integer antig = getDENNIED();
		DENNIED = dennied;
		pcs.firePropertyChange("DENNIED", antig, dennied);
	}
	public Integer getTASK() {
		return TASK;
	}
	public void setTASK(Integer task) {
		Integer antig = getTASK();
		TASK = task;
		pcs.firePropertyChange("TASK", antig, task);
	}
	public String getUSER() {
		return USER;
	}
	public void setUSER(String user) {
		String antig = getUSER();
		USER = user;
		pcs.firePropertyChange("USER", antig, user);
	}
	public String getUSERROL() {
		return USERROL;
	}

	public void setUSERROL(String userrol) {
		String antig = getUSERROL();
		USERROL = userrol;
		pcs.firePropertyChange("USERROL", antig, userrol);
	}
	public Integer getACCESSTYPE() {
		return this.ACCESSTYPE;
	}
	public Integer getIDO() {
		return IDO;
	}

	public void setIDO(Integer ido) {
		Integer antig = getIDO();
			IDO = ido;
		pcs.firePropertyChange("IDO", antig, ido);
	}
	public Integer getIDTO() {
			return IDTO;
	}

	public void setIDTO(Integer idto) {
		Integer antig = getIDTO();
		IDTO = idto;
		pcs.firePropertyChange("IDTO", antig, idto);
	}

	public Integer getPROP() {
			return PROP;
	}

	public void setPROP(Integer property) {
		Integer antig = getPROP();
		PROP = property;
		pcs.firePropertyChange("PROP", antig, property);
	}
	public String getVALUE() {
		
		return VALUE;
	}
	public void setVALUE(String value) {
		String antig = getVALUE();
		VALUE = value;
		pcs.firePropertyChange("VALUE", antig, value);
	}
	public Integer getVALUECLS() {
			return VALUECLS;
	}
	public void setVALUECLS(Integer valuecls) {
		Integer antig = getVALUECLS();
		VALUECLS = valuecls;
		pcs.firePropertyChange("VALUECLS", antig, valuecls);
	}
	
	public String toString() {
		String stringfact = "\n\t (access " +"(DENNIED "+this.getDENNIED() +") (TASK " + this.getTASK() + ")"
				+ "(USERROL " + this.getUSERROL() + ")" + "(USER "
				+ this.getUSER() + ")" + "(ACCESSTYPE " +this.getACCESSTYPE()
				+ ")" + "(IDTO " + this.getIDTO() + ")" + "(IDO " + this.getIDO() + ")"
				+ "(PROP " + this.getPROP() + ")" + "(VALUE "
				+ this.getVALUE() + ")" + "(VALUECLS " + this.getVALUECLS()
				+ ") (PRIORITY "+this.getPRIORITY()+"))";
		return stringfact;
	}
	
	public dynagent.common.basicobjects.Access toAccess() {
		ArrayList<String> user=new ArrayList<String>(Arrays.asList(USER));
		ArrayList<String> userRol=new ArrayList<String>(Arrays.asList(USERROL));
		ArrayList<String> accessType=new ArrayList<String>(Arrays.asList(Constants.getAccessTypeName(ACCESSTYPE)));
		dynagent.common.basicobjects.Access f = new dynagent.common.basicobjects.Access(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),userRol,user,this.getTASK(),this.getDENNIED(),accessType,this.getPRIORITY());
		return f;
	}

	public boolean commit(Session s) throws ApplicationException, NotFoundException {
		//Propagar a la session Madre
		int idMadre = s.getIDMadre();
		if(idMadre!=-1)
		{
			SessionController.getInstance().getSession(idMadre).addSessionable(this);
		}
		return false;
	}

	public void rollBack(Session s) throws ApplicationException, NotFoundException {
		// TODO Auto-generated method stub
		
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

	public void rollBackOfPropagation(Session s) throws ApplicationException, NotFoundException {
		if (!SessionController.getInstance().getSession(s.getID()).getSesionables().contains(this))
		{
			SessionController.getInstance().getSession(s.getID()).addSessionable(this);
		}
		/*else
			System.out.println("NO AÑADIR : Fact ya esta en HIJA, no se añade ");*/
		
		Session sessionPadre=SessionController.getInstance().getSession(s.getIDMadre());
		sessionPadre.getSesionables().remove(this);
	}



	public int getPRIORITY() {
		return PRIORITY;
	}



	public void setPRIORITY(int priority) {
		PRIORITY = priority;
	}
	
	public FactAccess clone (){
		FactAccess fa=new FactAccess(this.getIDTO(),this.getIDO(),this.getPROP() ,this.getVALUE(),this.getVALUECLS(),this.getUSERROL(),this.getUSER(),this.getTASK(),this.getDENNIED(),this.getACCESSTYPE(),this.getPRIORITY(),ik);
		return fa;	
	}
	
	public Object clone (IKnowledgeBaseInfo ik){
		FactAccess fa=new FactAccess(this.getIDTO(),this.getIDO(),this.getPROP() ,this.getVALUE(),this.getVALUECLS(),this.getUSERROL(),this.getUSER(),this.getTASK(),this.getDENNIED(),this.getACCESSTYPE(),this.getPRIORITY(),ik);
		return fa;	
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final FactAccess other = (FactAccess) obj;
		if (ACCESSTYPE == null) {
			if (other.ACCESSTYPE != null)
				return false;
		} else if (!ACCESSTYPE.equals(other.ACCESSTYPE))
			return false;
		if (DENNIED == null) {
			if (other.DENNIED != null)
				return false;
		} else if (!DENNIED.equals(other.DENNIED))
			return false;
		if (IDO == null) {
			if (other.IDO != null)
				return false;
		} else if (!IDO.equals(other.IDO))
			return false;
		if (IDTO == null) {
			if (other.IDTO != null)
				return false;
		} else if (!IDTO.equals(other.IDTO))
			return false;
		if (PRIORITY != other.PRIORITY)
			return false;
		if (PROP == null) {
			if (other.PROP != null)
				return false;
		} else if (!PROP.equals(other.PROP))
			return false;
		if (TASK == null) {
			if (other.TASK != null)
				return false;
		} else if (!TASK.equals(other.TASK))
			return false;
		if (USER == null) {
			if (other.USER != null)
				return false;
		} else if (!USER.equals(other.USER))
			return false;
		if (USERROL == null) {
			if (other.USERROL != null)
				return false;
		} else if (!USERROL.equals(other.USERROL))
			return false;
		if (VALUE == null) {
			if (other.VALUE != null)
				return false;
		} else if (!VALUE.equals(other.VALUE))
			return false;
		if (VALUECLS == null) {
			if (other.VALUECLS != null)
				return false;
		} else if (!VALUECLS.equals(other.VALUECLS))
			return false;
		return true;
	}

//	public boolean equals(Object o){
//		FactAccess f=(FactAccess)o;
//		if(Auxiliar.equals(f.getIDTO(), this.getIDTO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getPROP(), this.getPROP()) ||
//				Auxiliar.equals(f.getVALUE(), this.getVALUE()) ||
//				Auxiliar.equals(f.getVALUECLS(), this.getVALUECLS()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()) ||
//				Auxiliar.equals(f.getIDO(), this.getIDO()));
//	}
	

}
