package dynagent.gui.actions;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;
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
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.WindowComponent;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.ExportCommandPath;
import dynagent.gui.actions.commands.FindCommandPath;
import dynagent.gui.actions.commands.ImportCommandPath;
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

public class ActionImportExportIterator extends ActionIterator {
	private int ido;
	private int idto;
	//private int idProp;
	//private Session session;
	//private Session sessionParent;
	
	private ActionImportExportIterator(int ido,int idto,/*int idProp,*/Session session,Session sessionParent,ArrayList<commandPath> commandList){
		super(commandList,session);
		this.ido=ido;
		this.idto=idto;
		//this.idProp=idProp;
		/*this.session=session;*/
		//this.sessionParent=sessionParent;
	}
	
	public static ActionIterator createInstance(commandPath cPath, KnowledgeBaseAdapter kba, WindowComponent dialog) throws IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, NotFoundException, ActionException{
		ActionImportExportIterator actionImportExport=null;
		
		ArrayList<commandPath> commandList=new ArrayList<commandPath>();
		
		if(cPath instanceof ImportCommandPath || cPath instanceof ExportCommandPath){
			Integer idtoUserTask=cPath.getIdtoUserTask();
			Integer userRol=cPath.getUserRol();
			Session sessionParent=cPath.getSession();
			//int operation=access.NEW;
			
			//int idRange=-1;
			int idObject=-1;
			int idtoObject=-1;
			HashMap<Integer,Integer> values=null;
			boolean success=false;
			//No chequea la coherencia para que cada sesion hija pueda chequear sus propios cambios.
			//Ya que todos los idos estan cargados en esta sesion, por lo que las hijas no la chequearian sabiendo que un ancestro las chequea
			Session session=kba.createDefaultSession(sessionParent,idtoUserTask,false, true, true, true, sessionParent instanceof DDBBSession);
			session.addIMessageListener(Singleton.getInstance().getActionManager());
			//System.err.println("@@@@@@@@@@@@Init:"+session);
			try{
				if(cPath instanceof ImportCommandPath){
					ImportCommandPath importCP=(ImportCommandPath)cPath;
					idObject= importCP.getIdo();
					idtoObject= importCP.getIdto();
					values=new HashMap<Integer,Integer>();
				}else if(cPath instanceof ExportCommandPath){
					ExportCommandPath exportCP=(ExportCommandPath)cPath;
					idObject= exportCP.getIdo();
					idtoObject= exportCP.getIdto();
					values=exportCP.getValues();
				}
				
				buildStepsSourceAndParams(kba,commandList,idObject,idtoObject,values,userRol,idtoUserTask,session);
					
				if(!commandList.isEmpty() && cPath instanceof ImportCommandPath){
					commandList.add(null/*new SetCommandPath(idObject,-1,idtoUserTask,userRol,session)*/);//Metemos un null para que el asistente sepa que hay otro paso mas
				}
				  		
				actionImportExport= new ActionImportExportIterator(idObject,idtoObject,session,sessionParent,commandList);
				success=true;
			}finally{
				if(!success){
					session.setForceParent(false);
					session.rollBack();
				}
			}
		}
		
		return actionImportExport;
				
    }
	
	private static void buildStepsSourceAndParams(KnowledgeBaseAdapter kba,ArrayList<commandPath> commandList,int idoImportExport,int idtoImportExport,HashMap<Integer,Integer> values, Integer userRol, Integer idtoUserTask, Session sessionParent) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException, ActionException{
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
			if(!values.isEmpty()){
				
				ObjectProperty propSource=kba.getChild(idoImportExport,idtoImportExport,Constants.IdPROP_SOURCECLASS, userRol, idtoUserTask, sessionParent);
				
				//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
				kba.setValue(idoImportExport, Constants.IdPROP_SOURCECLASS, null, propSource.getValues(), userRol, idtoUserTask, sessionParent);
	
				Iterator<Integer> itrValues=values.keySet().iterator();
				boolean first=true;
				LinkedList<Value> listValue=new LinkedList<Value>();
				HashSet<Integer> listIdos=new HashSet<Integer>();
				while(itrValues.hasNext()){
					int value=itrValues.next();
					int valueCls=values.get(value);
					//kba.loadIndividual(value, 1, userRol, idtoUserTask, sessionParent);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
					listIdos.add(value);
					listValue.add(kba.buildValue(value, valueCls));
					if(propSource.getTypeAccess().getSetAccess()){//Aunque no tengamos set nos deja hacer el setValue anterior porque es un filtro
						Session sess=null;
						if(first){//Solo le creamos sesion al primero, a los demas se le iran creando dinamicamente
							sessionFirstSource=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,true);
							sess=sessionFirstSource;
							first=false;
						}
						//System.err.println("@@@@@@@@@@@@Source:"+sess);
						commandList.add(new SetCommandPath(idoImportExport,idtoImportExport,value,valueCls,idtoUserTask,userRol,sess));
					}
				}
				//kba.loadIndividual(listIdos, 1, userRol, idtoUserTask, sessionParent);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
				
				kba.setValue(idoImportExport, Constants.IdPROP_SOURCECLASS, listValue, null, userRol, idtoUserTask, sessionParent);
			}
			
			//try{
				kba.setState(idoImportExport, idtoImportExport, Constants.INDIVIDUAL_PREVALIDANDO, userRol, idtoUserTask, sessionParent);
			//}finally{
				kba.setState(idoImportExport, idtoImportExport, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, sessionParent);
			//}
			
			/*if(values.size()==1)
			{
				if(kba.getAccessIndividual(values.get(0), userRol, idtoUserTask).getSetAccess())
					commandList.add(new SetCommandPath(idObject,values.get(0),idtoUserTask,userRol,session));//Con esta sesion no se almacenaria en base de datos. Si es necesario tendria que usar otra
			}*/
			
			boolean hasParams;
			try {
				ObjectProperty property=kba.getChild(idoImportExport,idtoImportExport,idProp, userRol, idtoUserTask, sessionParent);
				//System.out.println("Property del asistente action:"+property+" idObject:"+idObject+" idProp:"+idProp);
				idRange=kba.getIdRange(property/*,valueCls*/);//idRange es un idto ya que no se esta creando filtro para Params
				hasParams=true;
			} catch (NotFoundException e) {
				hasParams=false;
			}
							
			//boolean create=true;
			if(hasParams){
				DefaultSession session=null;
				if(sessionFirstSource==null)//Si no se ha creado sesion para el source, los parametros seran el primer paso asi que necesita session
					session=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,true);
				//Lo creamos con level filter porque no nos interesa que vaya a base de datos
				commandList.add(new NewRelCommandPath(idoImportExport,idtoImportExport,idProp,idRange,/*Constants.LEVEL_FILTER*/Constants.LEVEL_PROTOTYPE,idtoUserTask,userRol,session));
			}
			success=true;
		}finally{
			if(!success && sessionFirstSource!=null){
				sessionFirstSource.setForceParent(false);
				sessionFirstSource.rollBack();
			}
		}
		
		
		
	}
	
	//Igual que en ActionSourceTargetIterator
	private static void buildStepTargetClass(KnowledgeBaseAdapter kba,ArrayList<commandPath> commandList,int idoImportExport,int idtoImportExport,Integer userRol,Integer idtoUserTask,Session sessionParent) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, OperationNotPermitedException, ActionException{
		Session sessionNew=kba.createDefaultSession(/*sessionParent*/sessionParent,idtoUserTask,true, true, true, true, true);
		sessionNew.addIMessageListener(Singleton.getInstance().getActionManager());
		boolean success=false;
		try{
			//System.out.println("Value seleccionado asistente:"+value);
			//Pedimos el rdn para que el motor cargue el individuo de base de datos si no lo tuviera
			//kba.getRDN(value, userRol, idtoUserTask, sessionNew);

			//try{
				kba.setState(idoImportExport, idtoImportExport, Constants.INDIVIDUAL_INFORMADO, userRol, idtoUserTask, sessionNew);
			//}finally{
				kba.setState(idoImportExport, idtoImportExport, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, sessionNew);
			//}
		
			ObjectProperty prop=kba.getChild(idoImportExport, idtoImportExport, Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, sessionNew);
			LinkedList<Value> valuesTarget=prop.getValues();
			if(!valuesTarget.isEmpty()){
				if(valuesTarget.size()==1){
					if(prop.getTypeAccess().getSetAccess())
						commandList.add(new SetCommandPath(idoImportExport,idtoImportExport,((ObjectValue)valuesTarget.get(0)).getValue(),((ObjectValue)valuesTarget.get(0)).getValueCls(),idtoUserTask,userRol,sessionNew));
					else commandList.add(new ViewCommandPath(idoImportExport,idtoImportExport,((ObjectValue)valuesTarget.get(0)).getValue(),((ObjectValue)valuesTarget.get(0)).getValueCls(),idtoUserTask,userRol,sessionNew));
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
				ex.setUserMessage("Para los datos elegidos la acción no ha generado ningún resultado");
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
		
			//Creamos una nueva sesion para la comprobacion de coherencia ya que al parar la edicion se pueden disparar reglas para hacer operaciones finales. Estas modificaciones
			//tenemos que ser capaces de quitarlas si se produce un fallo en la coherencia. Teniendo una sesion hija como esta, podemos hacerlo sin perder datos anteriores.
			DefaultSession sessionCoherence=kba.createDefaultSession(sessionParent,idtoUserTask,sessionParent.isCheckCoherenceObjects(),sessionParent.isRunRules(),sessionParent.isLockObjects(),sessionParent.isDeleteFilters(),false);
			if(form instanceof IChangePropertyListener)
				sessionCoherence.addIchangeProperty((IChangePropertyListener)form, false);
			int value=form.getResult().keySet().iterator().next();
			try{
				boolean success=false;
				try{
					//if((cPath instanceof NewRelCommandPath))//Si son parametros hay que comprobar la coherencia directamente ya que la session no comprueba la coherencia al ser un filtro
					//	kba.checkCoherenceObject(form.getResult().get(0), userRol, Singleton.getInstance().getComm().getUser(), idtoUserTask, sessionParent);
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
				
				if(this.nextIndex()==m_commandList.size()-1 && kba.isSpecialized(idtoUserTask,Constants.IDTO_IMPORT)){//Si el siguiente es el ultimo paso y es targetClass
					m_commandList.remove(m_commandList.size()-1);//Borramos el null añadido
					boolean successBuild=false;
					try{
						buildStepTargetClass(kba,m_commandList,ido,idto,userRol,idtoUserTask,sessionParent);
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
		//System.err.println("commandList despues de result:"+m_commandList);
		return true;
	}
	
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
		Integer idtoUserTask=cPath.getIdtoUserTask();
		
		session.setForceParent(false);
		if(form instanceof IChangePropertyListener)
			session.removeIchangeProperty((IChangePropertyListener)form);
		session.rollBack();
		
		if(m_currentStep==m_commandList.size()-1 && kba.isSpecialized(idtoUserTask,Constants.IDTO_IMPORT)){//Si es el ultimo paso añadimos un null ya que necesitamos que exista para que el asistente sepa que hay un siguiente paso
			m_commandList.remove(m_currentStep);
			m_commandList.add(null);
		}
		
		return true;
	}


}