package dynagent.gui.utils;


import gdev.gen.AssignValueException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.queryData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.action;
import dynagent.common.knowledge.instance;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.sessions.Session;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.MenuControl;
import dynagent.gui.Singleton;

public class DemandPooler extends Thread implements IHistoryDDBBListener{

   
	Element queryCfg;

	int period = 0;
	final static int active=1;
	final static int shutdown=2;
	int state=active;
	KnowledgeBaseAdapter kba;
	int ido;
	int idto;
	int idtoUserTask;
	
	MenuControl menuControl;
	
	DemandPoolerQuery pendingDemand;

	public DemandPooler(int ido, int idtoUserTask, int period, KnowledgeBaseAdapter kba, MenuControl menuControl) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException {
		this.period = period;
		this.ido = ido;
		this.idtoUserTask = idtoUserTask;
		this.menuControl = menuControl;
		this.kba=kba;
		this.idto = kba.getClass(ido);
		instance inst=kba.getTreeObject(ido, null, idtoUserTask, kba.getDefaultSession(), true);
		 queryCfg=kba.getQueryXML(inst, new ArrayList<SelectQuery>(), null, idtoUserTask,null);
		 /*if(hasDemands())
			notifyDemands(true);*/
		 new DemandPoolerQuery().start();
		 
		 kba.addHistoryDDBBListener(this);
		start();
	}
	public void shutdown(){
		state=shutdown;
	}

	protected void finalize(){
		state=shutdown;
	}
	
	public void run() {
		try{
			while (state==active) {
				try {
					sleep(period);
					//System.out.println("PREDAEMON");
					if( state==shutdown ) continue;
					
					//Lo hacemos tanto para true como para false por si hay algun individio que ha dejado de cumplir el filtro por otro usuario
					notifyDemands(hasDemands());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception ex){
			 Singleton.getInstance().getComm().logError(null,ex,"Error al preguntar si hay nuevos avisos en el servidor");
			 ex.printStackTrace();
		}
	}
	
	public boolean hasDemands() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		return Singleton.getInstance().getComm().serverGetQuery( /*m_com.getUser(),m_com.getBusiness(),*/queryCfg, idtoUserTask, queryData.MODE_ROW).size()>0;
	}
	
	public void notifyDemands(boolean show) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException, AssignValueException{
		menuControl.notifyNewEvent(ido, idtoUserTask, show);
	}
	
	public void initChangeHistory(){}
	
	public void endChangeHistory(){}
	
	public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
		//System.err.println("ChangeHistoryyyAvisos siendo this.idto:"+this.idto+" idto:"+idto);
		try{
			if(/*operation==action.DEL_OBJECT && idtoUserTask!=null && (idtoUserTask==this.idtoUserTask ||*/( this.idto==idto || kba.isSpecialized(idto, this.idto))){
				if(pendingDemand==null){//Si ya esta pendiente una comprobacion de si hay solicitudes, no creamos otra para evitar hacer la query mas de una vez
					pendingDemand=new DemandPoolerQuery();
					pendingDemand.start();
				}
			}
		} catch (Exception e) {
			Singleton.getInstance().getComm().logError(null,e, "Error al gestionar avisos en menu");
			e.printStackTrace();
		}
	}
	
	//Thread utilizada para hacer las consultas cuando no queremos que pierda tiempo en el hilo actual
	class DemandPoolerQuery extends Thread{

		public void run() {
			try{
				if(/*kba.isListenerMenu(idtoUserTask) &&*/ !hasDemands()){
					notifyDemands(false);
				}else{
					notifyDemands(true);
				}
			}catch(Exception ex){
				 Singleton.getInstance().getComm().logError(null,ex,"Error al preguntar si hay nuevos avisos en el servidor");
				 ex.printStackTrace();
			}
			pendingDemand=null;
			shutdown();
		}
	}
}
