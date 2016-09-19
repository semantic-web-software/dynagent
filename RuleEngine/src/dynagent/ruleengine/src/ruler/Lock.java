package dynagent.ruleengine.src.ruler;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.src.sessions.SessionController;

public class Lock extends IndividualState {
			
	private String USERCHANGEDSTATE;
	
	public Lock(int ido, int idto, String classname, int level, IKnowledgeBaseInfo ik) {
		super();
		IDO = ido;
		IDTO = idto;
		CLASSNAME = classname;
		LEVEL = level;
		USERCHANGEDSTATE = null;
		STATE = Constants.INDIVIDUAL_STATE_READY;
		
		this.ik=ik;
		
		int idSession=SessionController.getInstance().getActualSession(ik).getID();
		SessionLock sessState = new SessionLock(STATE,USERCHANGEDSTATE,idSession);
		this.addSessionRecord(sessState);
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
	}
	
	public void rollBack(Session s) throws ApplicationException,NotFoundException {
		int idSession = s.getID();
		//int pos = this.getPositionInSessionValue(idSession);
		SessionRecord sessionRecord=removeSessionRecord(idSession);
		if (sessionRecord != null)
		{		
			SessionLock sessionLock=(SessionLock)sessionRecord;
			if(!Auxiliar.equals(sessionLock.getUSERCHANGEDSTATE(), getUSERCHANGEDSTATE())){
				pcs.firePropertyChange("USERCHANGEDSTATE", sessionLock.getUSERCHANGEDSTATE(), getUSERCHANGEDSTATE());
			}if(!Auxiliar.equals(sessionLock.getSTATE(), getSTATE()))
				pcs.firePropertyChange("STATE", sessionLock.getSTATE(), getSTATE());
		} else
			System.out.println("RollBack de un Individual que no tiene una session Record o sea que no ha sido tocado durante esa session");

		this.setLastSession(getMaxSession());
	}
	
	public boolean rollBackRetractable(Session s) throws ApplicationException,NotFoundException {
		boolean retractable=false;
		int idSession = s.getID();
		//int pos = this.getPositionInSessionValue(idSession);
		SessionRecord sessionRecord=removeSessionRecord(idSession);
		if(!initialValuesChanged()){
			if(getSessionsRecord().isEmpty()){
				retractable=true;
			}
		}
		if(sessionRecord!=null)
			addSessionRecord(sessionRecord);
		
		return retractable;
	}
	
	public boolean initialValuesChanged() 
	{
		if (!Auxiliar.equals(STATE,getSTATE()) ||
			!Auxiliar.equals(USERCHANGEDSTATE,getUSERCHANGEDSTATE()))
				return true;
			else
				return false;
	}
	
	public boolean commit(Session s) throws ApplicationException,NotFoundException {
		return super.commit(s);
	}

	public String toString()
	{
		return "(Locked (IDO "+getIDO()+")(IDTO "+getIDTO()+")(STATE "+this.getSTATE()+")(USERCHANGEDSTATE "+this.getUSERCHANGEDSTATE()+")(CLASSNAME "+getCLASSNAME()+")(LEVEL "+this.getLEVEL()+")(LOCKEDBYUSER "+this.isLOCKEDBYUSER()+")(LOCKEDBYSYSTEM "+this.isLOCKEDBYSYSTEM()+"))";
	}
	
	public String getUSERCHANGEDSTATE() {
		String user=USERCHANGEDSTATE;
		int idSession = getLastSession();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		if(sessionRecord!=null){
			SessionLock sessionInd=(SessionLock)sessionRecord;
			user=sessionInd.getUSERCHANGEDSTATE();
		}
		return user;
	}

	public void setUSERCHANGEDSTATE(String user) {
		String ant=getUSERCHANGEDSTATE();
		boolean antUC=isLOCKEDBYUSER();
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int idSession = sessionActual.getID();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		
		if(sessionRecord!=null){// caso de tener un SessioRecord  de esa  session, modificarlo-
			((SessionLock)sessionRecord).setUSERCHANGEDSTATE(user);
		}else{
			SessionLock s = new SessionLock(getSTATE(),user,idSession);
			addSessionRecord(s);
		}
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//avisaSession(operacion);
		
		//pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		//System.err.println( "SET USER CHANGED STATE ant, curr "+ant+","+getUSERCHANGEDSTATE());
		pcs.firePropertyChange("USERCHANGEDSTATE", ant, getUSERCHANGEDSTATE());
		pcs.firePropertyChange("LOCKEDBYUSER", antUC, isLOCKEDBYUSER());		
	}

	public boolean isLOCKEDBYSYSTEM(){
		if((getSTATE().equals(Constants.INDIVIDUAL_STATE_LOCK) || getSTATE().equals(Constants.INDIVIDUAL_STATE_INIT_LOCK) || getSTATE().equals(Constants.INDIVIDUAL_STATE_END_LOCK))&& getUSERCHANGEDSTATE()!=null && getUSERCHANGEDSTATE().equals(Constants.USER_SYSTEM))
			return true;
		return false;
	}

	
	public boolean isLOCKEDBYUSER(){
		if((getSTATE().equals(Constants.INDIVIDUAL_STATE_LOCK) || getSTATE().equals(Constants.INDIVIDUAL_STATE_INIT_LOCK) || getSTATE().equals(Constants.INDIVIDUAL_STATE_END_LOCK))&& getUSERCHANGEDSTATE()!=null && (!getUSERCHANGEDSTATE().equals(Constants.USER_SYSTEM)))
			return true;
		return false;
	}
	
	
	
	public String getSTATE() {
		String state=STATE;
		int idSession = getLastSession();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		if(sessionRecord!=null){
			SessionLock sessionInd=(SessionLock)sessionRecord;
			state=sessionInd.getSTATE();
		}
		return state;
	}

	
	public void setSTATE(String state) {
		String ant=getSTATE();
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int idSession = sessionActual.getID();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		
		if(sessionRecord!=null){// caso de tener un SessioRecord  de esa  session, modificarlo-
			((SessionLock)sessionRecord).setSTATE(state);
		}else{
			SessionLock s = new SessionLock(state,getUSERCHANGEDSTATE(),idSession);
			addSessionRecord(s);
		}
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//avisaSession(operacion);
		
		//pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("STATE", ant, getSTATE());
	}

	public Object clone(IKnowledgeBaseInfo ik){
		return new Lock(getIDO(),getIDTO(),getCLASSNAME(),getLEVEL(),ik);
	}
}
