package dynagent.ruleengine.src.ruler;




import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.src.sessions.SessionController;

public class Individual extends IndividualState{
	
	private String RDN;
	private boolean ISOLATED;
	private String destinationSystem;
	
	private int sessionCreation;
	
	public Individual(Integer ido, Integer idto, String rdn, String classname, int level, IKnowledgeBaseInfo ik) {
		super();
		IDO = ido;
		IDTO = idto;
		STATE = Constants.INDIVIDUAL_STATE_READY;
		CLASSNAME = classname;
		LEVEL = level;
		RDN = rdn;
		ISOLATED=false;
		destinationSystem = null;
		
		this.ik=ik;
		int idSession=SessionController.getInstance().getActualSession(ik).getID();
		SessionIndividual sessState = new SessionIndividual(STATE,ISOLATED,idSession);
		if(idSession==0){
			System.err.println("ERROR:Individual creado en sesion 0");
			Auxiliar.printCurrentStackTrace();
		}
		sessionCreation=idSession;
		this.addSessionRecord(sessState);
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
	}
	
	public String toString()
	{
		return "(Individual (IDO "+getIDO()+")(IDTO "+getIDTO()+")(STATE "+getSTATE()+")(CLASSNAME "+getCLASSNAME()+")(LEVEL "+this.getLEVEL()+")(ISOLATED "+isISOLATED()+"))";
	}


	public String getSTATE() {
		String state=STATE;
		int idSession = getLastSession();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		if(sessionRecord!=null){
			SessionIndividual sessionInd=(SessionIndividual)sessionRecord;
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
			((SessionIndividual)sessionRecord).setSTATE(state);
		}else{
			SessionIndividual s = new SessionIndividual(state,isISOLATED(),idSession);
			addSessionRecord(s);
		}
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//avisaSession(operacion);
		
		//pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		System.out.println("INDIVIDUAL SET STATE "+IDO+" "+state+" "+getSTATE()+" anter:"+ant);
		pcs.firePropertyChange("STATE", ant, state);
	}

	public void rollBack(Session s) throws ApplicationException,NotFoundException {
		int idSession = s.getID();
		//int pos = this.getPositionInSessionValue(idSession);
		SessionRecord sessionRecord=removeSessionRecord(idSession);
		if (sessionRecord != null)
		{		
			SessionIndividual sessionIndividual=(SessionIndividual)sessionRecord;
			if(!Auxiliar.equals(sessionIndividual.getSTATE(), getSTATE()))
				pcs.firePropertyChange("STATE", sessionIndividual.getSTATE(), getSTATE());
			if(!Auxiliar.equals(sessionIndividual.isISOLATED(), isISOLATED()))
				pcs.firePropertyChange("ISOLATED", sessionIndividual.isISOLATED(), isISOLATED());
		} else
			System.out.println("RollBAck de Un Individual que no tiene una session Record, por lo tanto que no ha sido tocado durante esa session");

		this.setLastSession(getMaxSession());
		
		//Si se elimina la sesion que creo este objeto se modifica la sesion de creacion
		if(sessionCreation==idSession)
			sessionCreation=getLastSession();
		
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
	
	public boolean commit(Session s) throws ApplicationException,NotFoundException {
		super.commit(s);
		if(sessionCreation==s.getID())
			sessionCreation=s.getIDMadre();
		
		return true;
	}
	
	public String getRDN() {
		return RDN;
	}

	public void setRDN(String rdn) {
		RDN = rdn;
	}

	public boolean isISOLATED() {
		boolean isolated=ISOLATED;
		int idSession = getLastSession();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		if(sessionRecord!=null){
			SessionIndividual sessionInd=(SessionIndividual)sessionRecord;
			isolated=sessionInd.isISOLATED();
		}
		return isolated;
	}

	public void setISOLATED(boolean isolated) {
		boolean ant=isISOLATED();
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int idSession = sessionActual.getID();
		SessionRecord sessionRecord=getSessionRecord(idSession);
		
		if(sessionRecord!=null){// caso de tener un SessioRecord  de esa  session, modificarlo-
			((SessionIndividual)sessionRecord).setISOLATED(isolated);
		}else{
			SessionIndividual s = new SessionIndividual(getSTATE(),isolated,idSession);
			addSessionRecord(s);
		}
		this.setLastSession(idSession);
		
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//avisaSession(operacion);
		
		//pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("ISOLATED", ant, isISOLATED());
	}

	public int getSessionCreation() {
		return sessionCreation;
	}

	public boolean initialValuesChanged() 
	{
		if (!Auxiliar.equals(STATE,getSTATE()) ||
			!Auxiliar.equals(ISOLATED,isISOLATED()))
				return true;
			else
				return false;
	}

	public Object clone(IKnowledgeBaseInfo ik){
		return new Individual(getIDO(),getIDTO(),getRDN(),getCLASSNAME(),getLEVEL(),ik);
	}

	public void setDestinationSystem(String destinationSystem) {
		this.destinationSystem = destinationSystem;
	}

	public String getDestinationSystem() {
		return destinationSystem;
	}

	
	
	
}
