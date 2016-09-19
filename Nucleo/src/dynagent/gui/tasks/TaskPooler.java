package dynagent.gui.tasks;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.message;
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
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;

public class TaskPooler extends Thread{

    private static int CANCEL= 1;
    private static int REPOSE=2;
    private static int WAITING=3;
    
	message eRes=null;
	private int state;
	/*appControl control;*/
	communicator m_com;
	JDialog m_currForm;
	boolean stateChange=false;
	int lapso=0;
	selectData tasksChanged;
	KnowledgeBaseAdapter m_kba;
	/*ArrayList<processServer> listGuiListener;*/
	TaskCenter m_taskCenter;

	Runnable invocaEnGUI_Event;

	Runnable invocaEnGUI_Polling;

	public TaskPooler(/*appControl control,*/ TaskCenter taskCenter,KnowledgeBaseAdapter kba,communicator com) {
		/*this.control=control;*/
		m_com=com;
		m_taskCenter=taskCenter;
		m_kba=kba;
		state=REPOSE;
		/*listGuiListener=new ArrayList<processServer>();*/
		//final appControl thisControl=control;
		invocaEnGUI_Event= new Runnable() {
			 public void run() {
				 try{
					 //System.err.println("Obtiene tareas del servidor");
					 m_taskCenter.askServerTasks();
				 }catch(Exception ex){
					 m_com.logError(null,ex,"Error al intentar obtener las tareas del servidor");
			         ex.printStackTrace();
				 }
				/*try{
				thisControl.userEvent(null,eRes,0);
				}catch(SystemException se){
				    m_com.logError(se);
				}catch(RemoteSystemException re){
				    m_com.logError(re);
				}catch(CommunicationException ce){
				    ce.printStackTrace();
				}*/
				
			 }
		 };
		 invocaEnGUI_Polling = new Runnable() {
			  public void run() {
				 try{
					 //System.err.println("Pregunta si hay nuevas tareas en el servidor");
					 eRes= m_com.serverPolling();
					 if(eRes.getResultCode()==message.OWNING_CHANGED/* && eRes instanceof block*/ ){
						 //System.out.println("MESSAGE_RECEIVED:"+res);
						 /*tasksChanged=getTasks();
						 m_kba.updateUserTasks(tasksChanged);*/
						 SwingUtilities.invokeLater(invocaEnGUI_Event);
						 
					 }/*else if(eRes.getResultCode()==message.SUCCESSFULL){
						 System.out.println("No hay nuevas tareas");
					 }*/
				 }catch(Exception ex){
					 m_com.logError(null,ex,"Error al preguntar si hay nuevas tareas en el servidor");
			         ex.printStackTrace();
				 }
			  }
		 };
		setDaemon(true);
		//start();
	}

	/*public void addGuiListener(processServer listener){
		listGuiListener.add(listener);
	}*/
	
	public void stopWaiting(){
    	    //System.out.println("STOP WAIT");
		state=REPOSE;
		stateChange=true;
	}

	public void startWaiting(){
	    //System.out.println("START WAIT");
	    state=WAITING;
	    stateChange=true;
	}

	public void finalize(){
	    state=CANCEL;
	}

	public void run(){
		int quickPeriod=1;
		int slowPeriod=60;
		double mult=1;
		lapso=0;
		try{
		mainBucle:
			while(state!=CANCEL) {
					int period= state==WAITING ? 	quickPeriod:slowPeriod;
					int secondsAdd=(int)(period*mult);
		
					for(int periodProgress=0;periodProgress<secondsAdd;periodProgress++){
		                sleep(1000);
					    if(stateChange){
							stateChange=false;
							lapso=0;
							mult=1;
		                    continue mainBucle;
		                }
		            }
					lapso+=secondsAdd;
					SwingUtilities.invokeLater(invocaEnGUI_Polling);
					if(state==WAITING){
	                    if (lapso > 60) {
	                        stopWaiting();
	                        stateChange=false;
	                        mult = 1;
	                        continue;
					    }
	                    if (mult == 1)
	                        mult = 2;
	                    else
	                        mult = Math.pow(mult, 1.5);
	                }
	
			}
		}catch(InterruptedException e){
		    m_com.logError(null,e, "Error al esperar nuevas tareas del servidor");
		}
	}
	
	// Este metodo sera llamado si se recibe un mensaje de que ha habido una actualizacion de tareas
	public selectData getTasks() throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncompatibleValueException, CardinalityExceedException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException, ParseException, OperationNotPermitedException{
		instance inst=m_kba.getTreeObject(Constants.IDTO_UTASK, null, null,null,false);
		ArrayList<SelectQuery> select=m_kba.getSelectTask(null);
		
		Element queryCfg=m_kba.getQueryXML(inst, select, null, null,null);
		selectData cfg=null;
		cfg= m_com.serverGetQuery( /*m_com.getUser(),m_com.getBusiness(),*/queryCfg, null, queryData.MODE_ROW);
		return cfg;
	}
}
