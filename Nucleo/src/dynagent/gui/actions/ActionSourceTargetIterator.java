package dynagent.gui.actions;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.common.utils.BatchControl;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.Utils;
import dynagent.gui.WindowComponent;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.ExportCommandPath;
import dynagent.gui.actions.commands.FindCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.NewRelCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetMultipleCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewMultipleCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;

public class ActionSourceTargetIterator extends ActionIterator {
	private int ido;
	//private int idProp;
	//private Session session;
	//private Session sessionParent;
	private IBatchListener batchListener;
	
	private ActionSourceTargetIterator(int ido,/*int idProp,*/Session session,Session sessionParent,ArrayList<commandPath> commandList, IBatchListener batchListener){
		super(commandList,session);
		this.ido=ido;
		this.batchListener=batchListener;
		//this.idProp=idProp;
		/*this.session=session;*/
		//this.sessionParent=sessionParent;
	}
	
	public static ActionIterator createInstance(commandPath cPath, KnowledgeBaseAdapter kba, WindowComponent dialog) throws IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, NotFoundException, ActionException{
		
		ActionSourceTargetIterator actionParams=null;
		
		ArrayList<commandPath> commandList=new ArrayList<commandPath>();
		
		if(cPath instanceof ActionCommandPath || cPath instanceof NewCommandPath){
			Integer idtoUserTask=cPath.getIdtoUserTask();
			Integer userRol=cPath.getUserRol();
			Session sessionParent=cPath.getSession();
			//int operation=access.NEW;
			
			//int idRange=-1;
			int idObject=-1;
			int idProp=-1;
			HashMap<Integer,Integer> values=null;
			boolean success=false;
			//No chequea la coherencia para que cada sesion hija pueda chequear sus propios cambios.
			//Ya que todos los idos estan cargados en esta sesion, por lo que las hijas no la chequearian sabiendo que un ancestro las chequea
			Session session=kba.createDefaultSession(sessionParent,idtoUserTask,false, true, true, true, sessionParent instanceof DDBBSession);
			session.addIMessageListener(Singleton.getInstance().getActionManager());
			//System.err.println("@@@@@@@@@@@@Init:"+session);
			boolean batchMode=kba.isSpecialized(idtoUserTask, Constants.IDTO_ACTION_BATCH);
			IBatchListener batchListener=null;
			try{
				boolean create=false;
				if(batchMode){
					batchListener=new BatchControl(kba.getKnowledgeBase());
				}
				if(cPath instanceof NewCommandPath){
					NewCommandPath newCommandPath=(NewCommandPath)cPath;
					//int idoNew=newCommandPath.getIdo();
					idProp=Constants.IdPROP_SOURCECLASS;
					userRol=newCommandPath.getUserRol();
					if(kba.isSpecialized(newCommandPath.getIdto(), Constants.IDTO_ACTION)){
						idObject=newCommandPath.getIdo();
						ObjectProperty propertySource=kba.getChild(idObject,idtoUserTask,idProp, userRol, idtoUserTask, session);
						int idRange=kba.createRange(idObject, idtoUserTask, idProp, kba.getIdRange(propertySource), userRol, idtoUserTask, Constants.MAX_DEPTH_SEARCH_FILTERS, session);
						//System.err.println("PropertySource:"+propertySource);
						commandList.add(new FindCommandPath(propertySource,idRange,idtoUserTask,userRol,session));
						commandList.add(null/*new SetCommandPath(idObject,-1,idtoUserTask,userRol,session)*/);//Metemos un null para que el asistente sepa que hay otro paso mas
						create=true;
					}
				}else{
					ActionCommandPath actionCP=(ActionCommandPath)cPath;
					idObject= actionCP.getIdo();
					values=actionCP.getValues();
					idProp=Constants.IdPROP_PARAMS;
					//int valueCls=actionCP.getValueCls();
					// Obtenemos el idRange pidiendo la property ya que valueCls es una clase y no un filtro
					buildStepsSourceAndParams(kba,commandList,idObject,values,userRol,idtoUserTask,session);
					
					//if(create){
						if(!commandList.isEmpty()){
							commandList.add(null/*new SetCommandPath(idObject,-1,idtoUserTask,userRol,session)*/);//Metemos un null para que el asistente sepa que hay otro paso mas
						}
						else{
							/*int ido=kba.getIdoUserTaskAction(idtoUserTask);
							//try{
								kba.setUserTaskState(ido, Constants.IDO_INFORMADO, userRol, idtoUserTask, session);
							//}finally{
								kba.setUserTaskState(ido, Constants.IDO_PENDIENTE, userRol, idtoUserTask, session);
							//}*/
								buildStepTargetClass(kba,commandList,idObject,userRol,idtoUserTask,session,batchListener);
						}
					//}
					create=true;
				}
				if(create){		     		
					actionParams= new ActionSourceTargetIterator(idObject,session,sessionParent,commandList,batchListener);
					success=true;
				}
			}finally{
				if(!success){
					if(batchListener!=null)
						kba.removeBatchListener(batchListener);
					session.setForceParent(false);
					session.rollBack();
				}
			}
		}
		
		return actionParams;
				
    }
	
	private static void buildStepsSourceAndParams(KnowledgeBaseAdapter kba,ArrayList<commandPath> commandList,int idoUserTaskAction,HashMap<Integer,Integer> values, Integer userRol, Integer idtoUserTask, Session sessionParent) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException, ActionException{
		/*if(values.isEmpty()){
			ActionException ex=new ActionException("Para ejecutar la accion debe seleccionar algun individuo");
			ex.setUserMessage("Para ejecutar la accion debe seleccionar algun individuo");
			throw ex;
		}*/
		boolean success=false;
		Session sessionFirstSource=null;
		try{
			int idProp=Constants.IdPROP_PARAMS;
			int idRange=-1;
			ObjectProperty propSource=kba.getChild(idoUserTaskAction,idtoUserTask,Constants.IdPROP_SOURCECLASS, userRol, idtoUserTask, sessionParent);
			
			//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
			kba.setValue(idoUserTaskAction, Constants.IdPROP_SOURCECLASS, null, propSource.getValues(), userRol, idtoUserTask, sessionParent);
	
			if(!values.isEmpty()){
				Iterator<Integer> itrValues=values.keySet().iterator();
				boolean first=true;
				LinkedList<Value> listValue=new LinkedList<Value>();
				HashMap<Integer,HashSet<Integer>> listIdos=new HashMap<Integer, HashSet<Integer>>();
				while(itrValues.hasNext()){
					int value=itrValues.next();
					int valueCls=values.get(value);
					//kba.loadIndividual(value, 1, userRol, idtoUserTask, sessionParent);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
					if(!listIdos.containsKey(valueCls))
						listIdos.put(valueCls, new HashSet<Integer>());
					listIdos.get(valueCls).add(value);
					listValue.add(kba.buildValue(value, valueCls));
					if(propSource.getTypeAccess().getSetAccess()){//Aunque no tengamos set nos deja hacer el setValue anterior porque es un filtro
						Session sess=null;
						if(first){//Solo le creamos sesion al primero, a los demas se le iran creando dinamicamente
							sessionFirstSource=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,true);
							sess=sessionFirstSource;
							first=false;
						}
						//System.err.println("@@@@@@@@@@@@Source:"+sess);
						commandList.add(new SetCommandPath(idoUserTaskAction,idtoUserTask,value,valueCls,idtoUserTask,userRol,sess));
					}
				}
				int niveles=1;
				//anulamos traer niveles inferiores para clase Generar_ordenes
				//TODO hacerlo configurable
				if(idtoUserTask.intValue()==636) niveles=-1;
				
				kba.loadIndividual(listIdos, niveles, true, true, userRol, idtoUserTask, sessionParent);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
				
				kba.setValue(idoUserTaskAction, Constants.IdPROP_SOURCECLASS, listValue, null, userRol, idtoUserTask, sessionParent);
				
				//try{
					kba.setState(idoUserTaskAction, idtoUserTask, Constants.INDIVIDUAL_PREVALIDANDO, userRol, idtoUserTask, sessionParent);
				//}finally{
					kba.setState(idoUserTaskAction, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, sessionParent);
				//}
				
				/*if(values.size()==1)
				{
					if(kba.getAccessIndividual(values.get(0), userRol, idtoUserTask).getSetAccess())
						commandList.add(new SetCommandPath(idObject,values.get(0),idtoUserTask,userRol,session));//Con esta sesion no se almacenaria en base de datos. Si es necesario tendria que usar otra
				}*/
			}
			
			boolean hasParams=false;
			try {
				ObjectProperty property=kba.getChild(idoUserTaskAction,idtoUserTask,idProp, userRol, idtoUserTask, sessionParent);
				if(property.getTypeAccess().getViewAccess()){
					//System.out.println("Property del asistente action:"+property+" idObject:"+idObject+" idProp:"+idProp);
					idRange=kba.getIdRange(property/*,valueCls*/);//idRange es un idto ya que no se esta creando filtro para Params
					hasParams=true;
				}
			} catch (NotFoundException e) {
				
			}
							
			//boolean create=true;
			if(hasParams){
				DefaultSession session=null;
				if(sessionFirstSource==null)//Si no se ha creado sesion para el source, los parametros seran el primer paso asi que necesita session
					session=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,true);
				//Lo creamos con level filter porque no nos interesa que vaya a base de datos
				commandList.add(new NewRelCommandPath(idoUserTaskAction,idtoUserTask,idProp,idRange,/*Constants.LEVEL_FILTER*/Constants.LEVEL_PROTOTYPE,idtoUserTask,userRol,session));
			}
			success=true;
		}finally{
			if(!success && sessionFirstSource!=null){
				sessionFirstSource.setForceParent(false);
				sessionFirstSource.rollBack();
			}
		}
		
		
		
	}

	private static void buildStepTargetClass(KnowledgeBaseAdapter kba,ArrayList<commandPath> commandList,int idoUserTask,Integer userRol,Integer idtoUserTask,Session sessionParent, IBatchListener batchListener) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, OperationNotPermitedException, ActionException{
		Session sessionNew=kba.createDefaultSession(/*sessionParent*/sessionParent,idtoUserTask,true, true, true, true, true);
		sessionNew.addIMessageListener(Singleton.getInstance().getActionManager());
		boolean success=false;
		try{
			//System.out.println("Value seleccionado asistente:"+value);
			//Pedimos el rdn para que el motor cargue el individuo de base de datos si no lo tuviera
			//kba.getRDN(value, userRol, idtoUserTask, sessionNew);

			//try{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_INFORMADO, userRol, idtoUserTask, sessionNew);
			//}finally{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, sessionNew);
			//}
		
			if(batchListener!=null){
				LinkedHashMap<String, String> mapResult=processBatch(idoUserTask, idtoUserTask, userRol, kba, batchListener);
								
				for(String rdn:mapResult.keySet()){
					int ido=kba.createPrototype(Constants.IDTO_RESULT_BATCH, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, sessionNew);
					kba.setValue(ido, Constants.IdPROP_RDN, kba.buildValue(rdn, Constants.IDTO_STRING), null, userRol, idtoUserTask, sessionNew);
					kba.setValue(ido, Constants.IdPROP_RESULT, kba.buildValue(mapResult.get(rdn), Constants.IDTO_MEMO), null, userRol, idtoUserTask, sessionNew);
					
					kba.setValue(idoUserTask, Constants.IdPROP_TARGETCLASS, kba.buildValue(ido, Constants.IDTO_RESULT_BATCH), null, userRol, idtoUserTask, sessionNew);
				}
			}
			ObjectProperty prop=kba.getChild(idoUserTask, idtoUserTask, Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, sessionNew);
			LinkedList<Value> valuesTarget=prop.getValues();
			if(!valuesTarget.isEmpty()){
				if(valuesTarget.size()==1){
					if(prop.getTypeAccess().getSetAccess())
						commandList.add(new SetCommandPath(idoUserTask,idtoUserTask,((ObjectValue)valuesTarget.get(0)).getValue(),((ObjectValue)valuesTarget.get(0)).getValueCls(),idtoUserTask,userRol,sessionNew));
					else commandList.add(new ViewCommandPath(idoUserTask,idtoUserTask,((ObjectValue)valuesTarget.get(0)).getValue(),((ObjectValue)valuesTarget.get(0)).getValueCls(),idtoUserTask,userRol,sessionNew));
				}else{
					HashMap<Integer,Integer> valuesMap=new HashMap<Integer, Integer>();
					Iterator<Value> itr=valuesTarget.iterator();
					//int idto=kba.getClass(kba.getIdRange(prop));
					while(itr.hasNext()){
						ObjectValue ov=(ObjectValue)itr.next();
						valuesMap.put(ov.getValue(),ov.getValueCls());
					}
					if(prop.getTypeAccess().getSetAccess())
						commandList.add(new SetMultipleCommandPath(prop,valuesMap,idtoUserTask,userRol,sessionNew));
					else commandList.add(new ViewMultipleCommandPath(prop,valuesMap,idtoUserTask,userRol,sessionNew));
				}
				//System.err.println("@@@@@@@@@@@@Target:"+sessionNew);
			}else{
				ActionException ex=new ActionException("Para los datos elegidos la acción no ha generado ningún resultado. No hay datos en el targetClass");
				ex.setUserMessage("Para los datos elegidos la acción no ha generado ningún resultado.\nAsegúrese de haber seleccionado un registro válido de la tabla.");
				throw ex;
			}
			
			/*ArrayList<Integer> valuesInt=new ArrayList<Integer>();
			valuesInt.add(kba.createPrototype(kba.getIdRange(prop), Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session));
			valuesInt.add(kba.createPrototype(kba.getIdRange(prop), Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session));
			if(valuesInt.size()==1)
				commandList.add(new SetCommandPath(ido,valuesInt.get(0),idtoUserTask,userRol,session));
			else{
				
				commandList.add(new SetMultipleCommandPath(prop,valuesInt,idtoUserTask,userRol,session));
			}*/
			success=true;
		}finally{
			if(!success){
				sessionNew.setForceParent(false);
				sessionNew.rollBack();
			}
		}
	}

	public boolean setResultStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
		//System.err.println("commandList antes de result:"+m_commandList);
		
		commandPath cPath=m_commandList.get(m_currentStep);
		
		Integer idtoUserTask=cPath.getIdtoUserTask();
		Integer userRol=cPath.getUserRol();
		Session sessionParent=cPath.getSession();
		
		LinkedHashMap<Integer,Integer> result=form.getResult();
		
		if((cPath instanceof FindCommandPath)){
			if(result.isEmpty()){
				ActionException ex=new ActionException("Debe realizar una selección antes de avanzar al siguiente paso");
				ex.setUserMessage("Debe realizar una selección antes de avanzar al siguiente paso");
				throw ex;
			}
			//int value=result.keySet().iterator().next();
			
			boolean success=false;
			Session session=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true, true);
			session.addIMessageListener(Singleton.getInstance().getActionManager());
			try{
				//FindCommandPath findCommandPath=(FindCommandPath)cPath;
				//property=(ObjectProperty)findCommandPath.getProperty();
				//Property propRDN=kba.getRDN(value, userRol, idtoUserTask, session);//Cargamos el primer individuo para saber el idto
				//int valueCls=propRDN.getIdto();
				HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
				values.putAll(result);
				
				//System.err.println("@@@@@@@@@@@@Intermed:"+session);
				m_commandList.remove(m_commandList.size()-1);//Borramos el null metido en el constructor
				try{
					buildStepsSourceAndParams(kba,m_commandList,ido,values,userRol,idtoUserTask,session);
					
					//if(buildSuccess){
						if(m_commandList.size()>1){//Entra si se ha creado un paso para el source o para parametros
							m_commandList.add(null/*new SetCommandPath(idObject,-1,idtoUserTask,userRol,session)*/);//Metemos un null para que el asistente sepa que hay otro paso mas
						}else{
							buildStepTargetClass(kba,m_commandList,ido,userRol,idtoUserTask,session,batchListener);
						}
					//}
					//return buildSuccess;
						success=true;
				}finally{
					if(!success)
						m_commandList.add(null);//Restauramos el null
				}
				
			}finally{
				if(!success){
					session.setForceParent(false);
					session.rollBack();
				}
			}
		}else{
			//Creamos una nueva sesion para la comprobacion de coherencia ya que al parar la edicion se pueden disparar reglas para hacer operaciones finales. Estas modificaciones
			//tenemos que ser capaces de quitarlas si se produce un fallo en la coherencia. Teniendo una sesion hija como esta, podemos hacerlo sin perder datos anteriores.
			DefaultSession sessionCoherence=kba.createDefaultSession(sessionParent,idtoUserTask,sessionParent.isCheckCoherenceObjects(),sessionParent.isRunRules(),sessionParent.isLockObjects(),sessionParent.isDeleteFilters(),false);
			if(form instanceof IChangePropertyListener)
				sessionCoherence.addIchangeProperty((IChangePropertyListener)form, false);
			int value=result.keySet().iterator().next();
			try{
				boolean success=false;
				try{
					//if((cPath instanceof NewRelCommandPath))//Si son parametros hay que comprobar la coherencia directamente ya que la session no comprueba la coherencia al ser un filtro
					//	kba.checkCoherenceObject(form.getResult().get(0), userRol, kba.getServer().getUser(), idtoUserTask, sessionParent);
	
					form.stopEdition(value,sessionCoherence);
					sessionParent.checkCoherence(true);
					sessionCoherence.commit();
					success=true;
				}finally{
					if(!success){
						try{
							sessionCoherence.rollBack();
						}catch(Exception ex){
							System.err.println("No se ha podido hacer rollback de la session de coherencia");
							ex.printStackTrace();
						}
					}
				}
				
				if(this.nextIndex()==m_commandList.size()-1){;//Si el siguiente es el ultimo paso
					m_commandList.remove(m_commandList.size()-1);//Borramos el null añadido
					boolean successBuild=false;
					try{
						buildStepTargetClass(kba,m_commandList,ido,userRol,idtoUserTask,sessionParent,batchListener);
						successBuild=true;
					}finally{
						if(!successBuild)
							m_commandList.add(null);//Restauramos el null
					}
					//if(!buildSuccess)
					//	m_commandList.add(null);//Restauramos el null ya que no se ha construido el siguiente paso, asi se sabe que hay un paso mas
					//return buildSuccess;
				}else{
					Session session=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true, true);
					m_commandList.get(this.nextIndex()).setSession(session);
				}
				
			} catch (CardinalityExceedException e) {
				Property prop=e.getProp();
				String message=e.getUserMessage();
				if (prop!=null){
					if (!prop.getIdo().equals(value)){
						message+=": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask) + " de "+kba.getLabelClass(prop.getIdto(), idtoUserTask)+" '"+kba.getValueData(kba.getRDN(prop.getIdo(), prop.getIdto(), userRol, idtoUserTask, sessionParent))+"'";	
					}else{
						message+=": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);
					}
					
				}
				Singleton.getInstance().getMessagesControl().showErrorMessage(message,SwingUtilities.getWindowAncestor(form.getComponent()));
				
				return false;
			}
		}
		//System.err.println("commandList despues de result:"+m_commandList);
		return true;
	}
	
	
	@Override
	// Lo sobreescrimos ya que si vamos para atras debemos borrar el paso actual ya que este debe recalcularse a partir de la seleccion del paso anterior.
//	public commandPath previous(){
//		commandPath command=null;
//		try{
//			// Lo borramos cuando no se trate del ultimo de la lista ya que lo utilizamos en resultStep.
//			// Ademas si lo borraramos el asistente no sabria que hay un paso siguiente
//			/*if(m_commandList.size()!=m_currentStep+1)
//				m_commandList.remove(m_currentStep);
//			else{
//				commandPath cPath=m_commandList.get(m_currentStep);
//				cPath.getSession().rollBack();
//			}*/
//			command=super.previous(); 
//			
//			System.err.println("commandList despues de previous:"+m_commandList);
//		}catch(Exception ex){
//			ex.printStackTrace();
//			Singleton.getInstance().getComm().logError(ex);
//		}
//		
//		return command;
//	}
	
	// Nos vale el de ActionIterator, no hace falta sobreescribirlo
	/*
	public void endSteps() throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException {
		// Si es mayor que 1 y estamos en el ultimo paso hacemos rollback de la sesion de los parametros. Si no ya se encargan los formularios
		if(!session.isFinished())
			session.rollBack();
	}*/
	
	public boolean setCancelStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
		//Si cancelamos el formulario yendo al paso anterior tenemos que hacer rollback de los datos cambiados en ese formulario. Sin embargo
		//esto provocaria el rollback del formulario padre ya que posee forceParent=true. Para evitar esto le modificamos el forceParent.
		
		commandPath cPath=m_commandList.get(m_currentStep);
		/*
		int ido;
		if(cPath instanceof SetCommandPath){
			SetCommandPath setCommandPath=(SetCommandPath)cPath;
			ido=setCommandPath.getValue();
		}
		if(cPath instanceof NewRelCommandPath){
			NewRelCommandPath newRelCommandPath=;
			ido=((NewRelCommandPath)cPath).get();
		}
		Singleton.getInstance().getKnowledgeBase().setLockObject(ido, false, session);
		*/
		
		Session session=cPath.getSession();

		commandPath cPathPrevious=m_commandList.get(this.previousIndex());
		if((cPathPrevious instanceof FindCommandPath)){
			//En este caso debemos permitir que borre la sesion que hay entre el paso previo y el actual
			//que es la sesion que se encarga de cargar en motor los objetos seleccionados de la busqueda.
			//Sin embargo debemos evitar que haga tambien rollback de la sesion del filtro por lo que
			//le modificamos el forceParent.
			Session sessionParent=SessionController.getInstance().getSession(session.getIDMadre());
			sessionParent.setForceParent(false);
			if(form instanceof IChangePropertyListener)
				session.removeIchangeProperty((IChangePropertyListener)form);
			session.rollBack();
			
			//Borramos todos los pasos que sean menos el ultimo y el primero. Se borran ya que el usuario volvera a elegir registros en la tabla de busqueda
			int numDelete=m_commandList.size()-2-(m_currentStep-1);
			for(int i=0;i<numDelete;i++)
				m_commandList.remove(m_commandList.size()-2);
			
		}else{
			session.setForceParent(false);
			if(form instanceof IChangePropertyListener)
				session.removeIchangeProperty((IChangePropertyListener)form);
			session.rollBack();
			
			if(m_currentStep==m_commandList.size()-1){//Si es el ultimo paso añadimos un null ya que necesitamos que exista para que el asistente sepa que hay un siguiente paso
				m_commandList.remove(m_currentStep);
				m_commandList.add(null);
			}
		}
		
		return true;
	}
	
	/**
	 * Procesa cada posición del source del BatchListener enviando a base de datos cada procesamiento de forma separada y devolviendo el resultado de cada uno 
	 * @param idoUserTask
	 * @param idtoUserTask
	 * @param userRol
	 * @param kba
	 * @param batchListener
	 * @return
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws DataErrorException
	 * @throws NumberFormatException
	 * @throws CardinalityExceedException
	 * @throws OperationNotPermitedException
	 * @throws IncompatibleValueException
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ParseException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 */
	private static LinkedHashMap<String,String> processBatch(int idoUserTask, int idtoUserTask, Integer userRol,KnowledgeBaseAdapter kba, IBatchListener batchListener) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException {
		
		LinkedHashMap<String,String> mapResult=new LinkedHashMap<String, String>(); 
		//Session sessionNew=kba.createDDBBSession(true, true, true, false/*TODO Probar que realmente necesitamos false y no true*/, true);
		
		boolean oldReusable=kba.getDDBBSession().isReusable();
		kba.getDDBBSession().setReusable(true);
		
		ArrayList<HashMap<Integer, Integer>> mapSources=batchListener.getSources();
		if(mapSources!=null){
			Iterator<HashMap<Integer,Integer>> itrGroups=mapSources.iterator();
			while(itrGroups.hasNext()){
				HashMap<Integer,Integer> map=itrGroups.next();
				HashMap<Integer,HashSet<Integer>> mapIdtoIdos=new HashMap<Integer, HashSet<Integer>>();
				for(Integer ido:map.keySet()){
					int idto=map.get(ido);
					if(!mapIdtoIdos.containsKey(idto)){
						mapIdtoIdos.put(idto, new HashSet<Integer>());
					}
					mapIdtoIdos.get(idto).add(ido);
				}
				
				for(int idto:mapIdtoIdos.keySet()){
					boolean success=false;
					String rdn=null;
					Session sessionNew=kba.createDefaultSession(kba.getDDBBSession(),idtoUserTask, true, true, true, false/*TODO Probar que realmente necesitamos false y no true*/, true);
					try{
						sessionNew.addIMessageListener(Singleton.getInstance().getActionManager());
						ObjectProperty propIterator=kba.getChild(idoUserTask,idtoUserTask,Constants.IdPROP_ITERATOR, userRol, idtoUserTask, sessionNew);
						
						//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
						kba.setValue(idoUserTask, Constants.IdPROP_ITERATOR, null, propIterator.getValues(), userRol, idtoUserTask, sessionNew);
						
						kba.loadIndividual(mapIdtoIdos, 1, true, true, userRol, idtoUserTask, sessionNew);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
						
						LinkedList<Value> values=new LinkedList<Value>();
						rdn="";
						for(int ido:mapIdtoIdos.get(idto)){
							if(!rdn.isEmpty()){
								rdn+=", ";
							}
							rdn+=kba.getLabelClass(idto, idtoUserTask);
							if(kba.isLoad(ido)){
								rdn+=" "+(String)kba.getValueData(kba.getRDN(ido, idto, userRol, idtoUserTask, sessionNew));
							}else{//Si no esta cargado significa que ha habido algun error al intentar cargarlo
								rdn+="("+ido+")";
								throw new Exception();
							}
							values.add(kba.buildValue(ido, idto));
						}
						
						kba.setValue(idoUserTask, Constants.IdPROP_ITERATOR, values, null, userRol, idtoUserTask, sessionNew);
	
						kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_REALIZADO, userRol, idtoUserTask, sessionNew);
													
						sessionNew.commit();
						success=true;
						mapResult.put(rdn, "Completado");
					}catch(RuleEngineException ex){
						ex.printStackTrace();
						mapResult.put(rdn,ex.getUserMessage());
					}catch(ServerException ex){
						ex.printStackTrace();
						mapResult.put(rdn,ex.getUserMessage());
					}catch(Exception ex){
						ex.printStackTrace();
						mapResult.put(rdn,"Error al intentar procesar la operación");
					}finally{
						if(!success){
							sessionNew.setForceParent(false);
							sessionNew.rollBack();
						}
					}
				}
			}
		}
		kba.getDDBBSession().setReusable(oldReusable);
		
		return mapResult;
	}

}
