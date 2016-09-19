package dynagent.ruleengine.src.ruler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.sessions.SessionController;

public abstract class SessionsData implements Sessionable{

	ArrayList<SessionRecord> sessionsRecord;
	HashMap<Integer,SessionRecord> sessionsRecordRemovedPropagation;
	HashMap<Integer,SessionRecord> sessionsRecordChangedPropagation;
	private int lastSession;
	
	public SessionsData() {
		super();
		sessionsRecord=new ArrayList<SessionRecord>();
		sessionsRecordRemovedPropagation=new HashMap<Integer, SessionRecord>();
		sessionsRecordChangedPropagation=new HashMap<Integer, SessionRecord>();
		lastSession=0;
	}
	
	public void addSessionRecord(SessionRecord sessionRecord){
		sessionsRecord.add(sessionRecord);
	}
	
	public SessionRecord getSessionRecord(int idSession){
		SessionRecord sessionRecord=null;
		if (this.sessionsRecord.size() != 0) {
			for (int i = 0; i < sessionsRecord.size(); i++) {
				if (sessionsRecord.get(i).getIdSession() == idSession)
					sessionRecord= sessionsRecord.get(i);
			}
		}
		return sessionRecord;
	}
	
	public SessionRecord removeSessionRecord(int idSession){
		SessionRecord sessionRecord=null;
		if (this.sessionsRecord.size() != 0) {
			for (int i = 0; i < sessionsRecord.size(); i++) {
				if (sessionsRecord.get(i).getIdSession() == idSession)
					sessionRecord= sessionsRecord.remove(i);
			}
		}
		return sessionRecord;
	}
	
	public boolean hasSessionsRecord(){
		return !sessionsRecord.isEmpty();
	}
	
	public int getLastSession()
	{
		for(int i = 0 ; i < sessionsRecord.size(); i++)
			if(this.lastSession == sessionsRecord.get(i).getIdSession())
				return lastSession;
		
		return this.getMaxSession();
	}
	
	public void setLastSession(int lastSess)
	{
		this.lastSession = lastSess;
	}
	

	public int getMaxSession()
	{	
		return getMaxSession(-1);
	}
	
	public int getMaxSession(int excludedSession)
	{	
		int max = -1;
		for(int i = 0; i  <this.sessionsRecord.size();i++) {
			if(sessionsRecord.get(i).getIdSession()>max)
				if(sessionsRecord.get(i).getIdSession()!=excludedSession)
					max = sessionsRecord.get(i).getIdSession();
  		}
		return max;
	}

	public int getPositionInSessionValue(int id)
	{
		for (int i = 0; i < sessionsRecord.size(); i++)
			if (sessionsRecord.get(i).getIdSession() == id)
				return i;

		return -1;
	}
	
	public boolean commit(Session s) throws ApplicationException,NotFoundException {
		
		int idactual = s.getID();
		int idmadre = s.getIDMadre();
		
		propagate(idactual,idmadre,false);
		return true;
	}

	public abstract void rollBack(Session s) throws ApplicationException,NotFoundException;

	public void rollBackOfPropagation(Session s)throws ApplicationException, NotFoundException {
		propagate(s.getIDMadre(), s.getID(),true);
	}
	
	/*Si undoPropagation=true se basa en los valores anteriores de las sesiones para restaurar. Es decir en los valores que tenian las sesiones antes de hacer commit en cada hija.
	De esta manera podemos restaurar el estado original de las sesiones antes de hacer un commit o un rollback de una sesion forceParent=true.*/
	public void propagate(int idSessionOrigen,int idSessionDestino,boolean undoPropagation){
		
		//boolean modificado = getOP() != OP || getQMAX() != QMAX || getQMIN() != getQMIN()|| getVALUE() != VALUE || getVALUECLS() != VALUECLS ||getIDO()!=IDO || getIDTO()!=IDTO;
		
		Session sessionActual = SessionController.getInstance().getSession(idSessionOrigen);
		Iterator <SessionRecord> it = sessionsRecord.iterator();
		LinkedList<SessionRecord> borrables = new LinkedList<SessionRecord>();
//		while(it.hasNext())
//		{
//			SessionRecord sv = it.next();
//			Session s = SessionController.getInstance().getInstance().getSession(sv.getIdSession());
//			if(sessionActual.somosHermanos(s.getID()))
//			{
//				//System.err.println("NUMERO DE SESSIONVALUES ANTES = "+sessionValues.size()+ " son:"+sessionValues);
//				s.getSesionables().remove(this);
//				borrables.add(sv);
//				System.err.println("SESIONES HERMANAS :"+s+" y "+sessionActual+" fact:"+this);
//				//System.err.println("NUMERO DE SESSIONVALUES DESPUES = "+sessionValues.size()+ " son:"+sessionValues);
//			}
//		}	
		sessionsRecord.removeAll(borrables);
		int pos = this.getPositionInSessionValue(idSessionOrigen);
		if (pos != -1 /*&& modificado*/)
		{
			SessionRecord sv = null;
			int posmadre = this.getPositionInSessionValue(idSessionDestino);
			if (posmadre == -1) {// si la madre no existe : cambiamos el numero de session y añadimos el this
				if(undoPropagation){
					//System.err.println("AntesUndo:\n"+sessionsRecord);
					if(sessionsRecordChangedPropagation.containsKey((idSessionDestino))){
						sv = sessionsRecordChangedPropagation.remove(idSessionDestino);
						sv.setIdSession(idSessionDestino);
						if(sessionsRecordRemovedPropagation.containsKey(idSessionOrigen)){
							SessionRecord svRemoved = sessionsRecordRemovedPropagation.remove(idSessionOrigen);
							sessionsRecord.add(pos, svRemoved);
						}else{
							Session sessionOrigen=SessionController.getInstance().getSession(idSessionOrigen);
							sessionOrigen.getSesionables().remove(this);
						}
					}
					//System.err.println("DespuesUndo:\n"+sessionsRecord);
				}else{
					sv = this.sessionsRecord.get(pos);
					sv.setIdSession(idSessionDestino);
					sessionsRecordChangedPropagation.put(idSessionOrigen,sv);
				}
			} else {// la madre ya esta, cambiamos y borramos
				if(undoPropagation){
					//System.err.println("AntesUndo:\n"+sessionsRecord);
					if(sessionsRecordChangedPropagation.containsKey((idSessionDestino))){
						sv = sessionsRecordChangedPropagation.remove(idSessionDestino);
						sv.setIdSession(idSessionDestino);
						sessionsRecord.remove(posmadre);
						if(sessionsRecordRemovedPropagation.containsKey(idSessionOrigen)){
							SessionRecord svRemoved = sessionsRecordRemovedPropagation.remove(idSessionOrigen);
							sessionsRecord.add(pos, svRemoved);
						}else{
							Session sessionOrigen=SessionController.getInstance().getSession(idSessionOrigen);
							sessionOrigen.getSesionables().remove(this);
						}
					}
					//System.err.println("DespuesUndo:\n"+sessionsRecord);
				}else{
					SessionRecord ref = sessionsRecord.get(pos);
					ref.setIdSession(idSessionDestino);
					sessionsRecordChangedPropagation.put(idSessionOrigen,ref);
					sessionsRecordRemovedPropagation.put(idSessionDestino,sessionsRecord.remove(posmadre));
				}
			}
			setLastSession(idSessionDestino);
			if(SessionController.getInstance().getSession(idSessionDestino)==null)
				System.err.println("ERROR:Data sin destino:"+this+" \n siendo sesionDestino:"+idSessionDestino);
			//Registrarse en la madre
			if (!SessionController.getInstance().getSession(idSessionDestino).getSesionables().contains(this))
			{
				SessionController.getInstance().getSession(idSessionDestino).addSessionable(this);
			}
			/*else
				System.out.println("NO AñADIR : Data ya esta en MADRE, no se añade ");*/			
		}
		else
		{
			//TODO: Error ya que si el Fact esta registrado en una sesion, es porque ha sido modificado y tiene una sessionValuede esa sesion
			System.out.println("WARNING:Commit de Un Record que no ha sido modificado");
		}
		
//		if(SessionController.getInstance().getSession(idSessionDestino) instanceof DocDataModel)
//		{
//			ArrayList<SessionRecord> sesToRemoveList = new ArrayList<SessionRecord>();
//			for(int i = 0 ; i < sessionsRecord.size();i++)
//				if(sessionsRecord.get(i).getIdSession() != idSessionDestino)
//				{
//						sesToRemoveList.add(sessionsRecord.get(i));
//						SessionController.getInstance().getSession(sessionsRecord.get(i).getIdSession()).getSesionables().remove(this);
//				}
//		
//			this.sessionsRecord.removeAll(sesToRemoveList);
//		}
		
		
		
	}

	public ArrayList<SessionRecord> getSessionsRecord() {
		return sessionsRecord;
	}
	
	
	
	

}
