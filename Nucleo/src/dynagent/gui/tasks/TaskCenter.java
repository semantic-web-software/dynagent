package dynagent.gui.tasks;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
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
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.ruleengine.src.sessions.DefaultSession;
import tasks.ExecutionTaskException;
import tasks.ITaskCenter;
import tasks.ITaskListener;

public class TaskCenter implements ITaskCenter{
	
	KnowledgeBaseAdapter m_kba;
	communicator m_com;
	TaskPooler taskPooler;
	ArrayList<ITaskListener> listGuiListener;
	WindowComponent dialog;

	public TaskCenter(KnowledgeBaseAdapter kba,WindowComponent dialog){
		m_kba=kba;
		m_com=kba.getServer();
		listGuiListener=new ArrayList<ITaskListener>();
		
		taskPooler=new TaskPooler(this,kba,m_com);
		//taskPooler.start();
		
		this.dialog=dialog;
	}
	
	public void askServerTasks() throws NotFoundException, SystemException, ParseException, DataErrorException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, InstanceLockedException, SQLException, NamingException, JDOMException, OperationNotPermitedException{
		selectData tasks=getServerTasks();
		//System.out.println("selectData tareas preasignadas " + tasks.toString());
		//m_kba.updateUserTasks(tasks);
		updateTasks(tasks);
	}
	
	private selectData getServerTasks() throws SystemException, ParseException, DataErrorException, RemoteSystemException, CommunicationException, InstanceLockedException{
		selectData tasks=null;
		tasks= m_com.serverGetTasks(null,m_com.getUser()/*,m_com.m_empresa*/);
		//System.out.println("TaskCenter.getTaskChanged:Resultado:"+tasks.toString());
		return tasks;
	}
	
	public void addTasksListener(ITaskListener taskListener){
		listGuiListener.add(taskListener);
	}
	
	/*public void updateTasks(selectData tasks){
		Iterator<ITaskListener> itr=listGuiListener.iterator();
		while(itr.hasNext()){
			ITaskListener taskListener=itr.next();
			taskListener.updateTasks(tasks);
		}
	}*/
	
	public void updateTasks(selectData tasks) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		
		Iterator<instance> itr=tasks.getIterator();
		while(itr.hasNext()){
			instance inst=itr.next();
			
			int idoUserTask;
			int idtoUserTask;
			String labelUserTask=null;
			String state=null;
			String asignDate=null;
			String ejecuteDate=null;
			
			m_kba.setInstance(inst);
			try{
				idoUserTask=inst.getIDO();
				idtoUserTask=inst.getIdTo();
				labelUserTask=(String)m_kba.getValueData(m_kba.getRDN(idoUserTask,idtoUserTask,null,null,null));
	
			    /*ObjectProperty propTarget=*/m_kba.getTarget(idoUserTask, idtoUserTask, null,null);
			    //int idtoUserTask=propTarget.getIdto();//Lo obtenemos de aqui ya que idto que nos viene en el instance de BD es de la utask generica
			     //System.out.println("PropTarget:"+propTarget);
			     //DataProperty propRDNTarget=kba.getRDN(kba.getIdoValue(propTarget),null,idtoUserTask,null);
			     
			     //kba.clearInstance();
			     DataProperty propOwner=m_kba.getField(idoUserTask, idtoUserTask, Constants.IdPROP_OWNER, null, /*idtoUserTask*/null, null);
			     if(propOwner==null || propOwner.getValues().isEmpty())
			    	 state=Utils.normalizeLabel("Preasignada");
			     else state=Utils.normalizeLabel("Asignada");
			     
			     DataProperty propAsignDate=m_kba.getField(idoUserTask, idtoUserTask, Constants.IdPROP_ASIGNDATE, null, /*idtoUserTask*/null, null);
			     asignDate=(String)m_kba.getValueData(propAsignDate);
			     DataProperty propEjecuteDate=m_kba.getField(idoUserTask, idtoUserTask, Constants.IdPROP_EJECUTEDATE, null, /*idtoUserTask*/null, null);
			     ejecuteDate=(String)m_kba.getValueData(propEjecuteDate);
			}finally{
				m_kba.clearInstance();
			}
		     /*String ejecuteDate=DateFormat.getDateInstance().format(new java.util.Date());
		     String endDate=DateFormat.getDateInstance().format(new java.util.Date());
		     */
		     
			Iterator<ITaskListener> itrListener=listGuiListener.iterator();
			while(itrListener.hasNext()){
				ITaskListener taskListener=itrListener.next();
				taskListener.updateTasks(idoUserTask,labelUserTask,state,asignDate,ejecuteDate);
			}
		}
	}

	public void exeTask(int idoUserTask) throws ExecutionTaskException{// throws NotFoundException, ParseException, AssignValueException, IncompatibleValueException, ApplicationException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, CardinalityExceedException, OperationNotPermitedException {
		try {
			DefaultSession session = m_kba.createDefaultSession(m_kba.getDDBBSession(),null,true, true, true, true,true);

			int idtoUserTask=m_kba.getIdtoUserTask(idoUserTask);
			ObjectProperty prop = m_kba.getTarget(idoUserTask, idtoUserTask, null, session);
			
			session.setUtask(idtoUserTask);//Quizas a exeTask habria que pasarle el idtoUserTask y asignarselo en el constructor de la sesion
			boolean success=false;
	
			Value valueOld;
			StringValue value;
	
			try {
				//			instance instUserTask=m_kba.getTreeObject(idoUserTask, null, idtoUserTask, null);
				//		m_kba.setInstance(instUserTask);
						
				//TODO Hay que ver si es necesario pedir el owner para que sea el oldValue o siempre oldValue tendria que ser null
				DataProperty propOwner=m_kba.getOwner(idoUserTask, idtoUserTask, null, session);
				
				LinkedList<Value> valueList=propOwner.getValues();
				valueOld=null;
				if(!valueList.isEmpty())
					valueOld=valueList.getFirst();
				
				value=new StringValue();
				String user=m_com.getUser();
				value.setValue(user);
				m_kba.setValue(idoUserTask, Constants.IdPROP_OWNER, value, valueOld, null, idtoUserTask, session);
				
				session.commit();
				success=true;
			} catch (CardinalityExceedException e) {
				PropertyValue prope=e.getProp();
				if(prope!=null){
					
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage()+": "+ m_kba.getLabelProperty(prope, prope.getIdto(), idtoUserTask),dialog.getComponent());
				}else{
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
				}
				
			} catch (OperationNotPermitedException e) {
				Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());

			} finally{
				if(!success){
					session.setForceParent(false);
					session.rollBack();
				}else{
					//System.out.println("Property targetClass de taskCenter:"+prop);
					ObjectValue valueP=m_kba.getValue(prop);
					SetCommandPath setCommandPath=new SetCommandPath(idoUserTask,m_kba.getClass(idoUserTask),valueP.getValue(),valueP.getValueCls(),prop.getIdto(),null,m_kba.getDDBBSession());
					Singleton.getInstance().getActionManager().exeOperation(setCommandPath,m_kba,null,dialog,false);
					
					//System.out.println("TaskCenter.exeTask idoUserTask:"+idoUserTask);	
					// TODO Comprobar si estas excepciones serian las que deberia capturar y mostrar al usuario o registrarlas en errores
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExecutionTaskException("Error al intentar ejecutar la tarea",e);
		}
		
//		m_kba.clearInstance();
//		try {
//		Element el=m_com.serverTransitionObject(new contextAction(
//				m_com.m_empresa, user, null, 0,
//				0, 0, message.ACTION_MODIFY,instUserTask));
//		jdomParser.print("elementOwner", el);
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (NamingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (DataErrorException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (JDOMException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		System.out.println("Property targetClass de taskCenter:"+prop);
//		SetCommandPath setCommandPath=new SetCommandPath(m_kba.getIdoValue(prop),prop.getIdto(),null,m_kba.getDDBBSession());
//		Singleton.getInstance().getActionManager().exeOperation(setCommandPath);
//		System.out.println("TaskCenter.exeTask idoUserTask:"+idoUserTask);	

	}
}
