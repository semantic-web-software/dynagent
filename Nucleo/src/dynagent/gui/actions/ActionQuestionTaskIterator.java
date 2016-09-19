package dynagent.gui.actions;

import gdev.gen.AssignValueException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

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
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IndividualValues;
import dynagent.common.utils.Utils;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.QuestionTaskCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ActionQuestionTaskIterator extends ActionIterator implements IQuestionListener{
	private int ido;
	private int idto;
	//private int idProp;
	//private Session session;
	//private Session sessionParent;
	private Integer idtoUserTask;
	private Integer userRol;
	private HashMap<commandPath,ArrayList<commandPath>> mapDependencyCommandPath;//Sirve para que al ir para atras en los formularios sepamos que commandPath tenemos que quitar de la lista
	private LinkedHashMap<Integer,ArrayList<IndividualValues>> mapDataQuestionRequest;
	private String rdn;
	private LinkedHashMap<Integer, HashMap<String,String>> mapAliasQuestionRequest;
	
	private ActionQuestionTaskIterator(int ido,int idto,String rdn,Integer userRol,Integer idtoUserTask,Session session,ArrayList<IndividualValues> mapData,HashMap<String,String> alias) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		super(new ArrayList<commandPath>(),session);
		this.idto=idto;
		this.ido=ido;
		this.rdn=rdn;
		this.idtoUserTask=idtoUserTask;
		this.userRol=userRol;
		this.mapDependencyCommandPath=new HashMap<commandPath, ArrayList<commandPath>>();
		this.mapDataQuestionRequest=new LinkedHashMap<Integer, ArrayList<IndividualValues>>();
		this.mapAliasQuestionRequest=new LinkedHashMap<Integer, HashMap<String,String>>();
		
		KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBaseAdapter(session.getKnowledgeBase());
		
		Singleton.getInstance().getQuestionTaskManager().addQuestionTaskListener(this, kba.getKnowledgeBase(),rdn);
		
		buildStepsSource(kba,ido,idto,mapData,alias,userRol,idtoUserTask,session);
		
		if(m_commandList.isEmpty())
			System.err.println("ERROR: Peticion de questionTask sin individuos en sourceClass");
		
		m_commandList.add(null);//Metemos un null para que el asistente sepa que hay otro paso mas
	}
	
	public static ActionIterator createInstance(commandPath cPath, KnowledgeBaseAdapter kba) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		ActionQuestionTaskIterator actionQuestionTask=null;
		
		if(cPath instanceof QuestionTaskCommandPath){
			Integer idtoUserTask=cPath.getIdtoUserTask();
			Integer userRol=cPath.getUserRol();
			Session sessionParent=cPath.getSession();
			//int operation=access.NEW;
			
			QuestionTaskCommandPath questionCP=(QuestionTaskCommandPath)cPath;
			int idObject=-1;
			int idtoObject=-1;
			String rdn=null;
			ArrayList<IndividualValues> dataList=null;
			HashMap<String,String> aliasList=null;
			boolean success=false;
			//No chequea la coherencia para que cada sesion hija pueda chequear sus propios cambios.
			//Ya que todos los idos estan cargados en esta sesion, por lo que las hijas no la chequearian sabiendo que un ancestro las chequea
			Session session=kba.createDefaultSession(sessionParent,idtoUserTask,false, true, true, true, sessionParent instanceof DDBBSession);
			session.addIMessageListener(Singleton.getInstance().getActionManager());
			//System.err.println("@@@@@@@@@@@@Init:"+session);
			try{
				idtoObject=questionCP.getIdto();
				rdn=questionCP.getRdn();
				dataList=questionCP.getData();
				aliasList=questionCP.getAlias();
				
				idObject=kba.createPrototype(idtoObject,Constants.LEVEL_PROTOTYPE, /*new session(),*/ userRol, idtoUserTask,session);
				
				kba.setValue(idObject, Constants.IdPROP_RDN, kba.buildValue(rdn, Constants.IDTO_STRING), null, userRol, idtoUserTask, session);
				
				actionQuestionTask= new ActionQuestionTaskIterator(idObject,idtoObject,rdn,userRol,idtoUserTask,session,dataList,aliasList);
				success=true;
				
			}finally{
				if(!success){
					session.setForceParent(false);
					session.rollBack();
				}
			}
		}
		
		return actionQuestionTask;
				
    }
	
	private void buildStepsSource(KnowledgeBaseAdapter kba,int idoQuestionTask,int idtoQuestionTask,ArrayList<IndividualValues> dataList,HashMap<String,String> auxAliasLists, Integer userRol, Integer idtoUserTask, Session sessionParent) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
				
		int positionToInsert=nextIndex();
		boolean success=false;
		Session sessionFirstSource=null;
		ArrayList<commandPath> listDependencyCommandPath=new ArrayList<commandPath>();
		try{
			boolean first=true;
			for(IndividualValues indValues:dataList){
				int id=indValues.getId();
				int value=id;
				int valueCls=kba.getClass(id);
				Integer level=kba.getLevelObject(id);
				
				Session sess=null;
				if(first){//Solo le creamos sesion al primero, a los demas se le iran creando dinamicamente
					sessionFirstSource=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true,true);
					sess=sessionFirstSource;
					first=false;
				}
				
				if(Auxiliar.equals(level,Constants.LEVEL_MODEL) || Auxiliar.equals(level, Constants.LEVEL_FILTER)){
					value=kba.createPrototype(value, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, sessionFirstSource);
				}
				
				//Damos los valores de los mapas
				for(Integer idProp:indValues.getMapData().keySet()){
					Object data=indValues.getMapData().get(idProp);
					int dataCls;
					Property prop=kba.getProperty(value, valueCls, idProp, userRol, idtoUserTask, sessionFirstSource);
					if(prop instanceof DataProperty){
						dataCls=((DataProperty)prop).getDataType();
						
						Value oldValue=prop.getUniqueValue();
						
						kba.setValue(value, idProp, kba.buildValue(data,dataCls), oldValue, userRol, idtoUserTask, sessionFirstSource);
					}else{
						ArrayList<Integer> list=null;
						if(data instanceof ArrayList){
							list=(ArrayList<Integer>)data;
						}else{
							list=new ArrayList<Integer>();
							list.add((Integer)data);
						}
						//System.err.println("list:"+list);
						for(int ido:list){
							int valueData=ido;
							Integer levelValue=kba.getLevelObject(valueData);
							if(Auxiliar.equals(levelValue,Constants.LEVEL_MODEL) || Auxiliar.equals(levelValue, Constants.LEVEL_FILTER)){
								valueData=kba.createPrototype(valueData, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, sessionFirstSource);
							}else if(!kba.isLoad(valueData)){
								//TODO Esto no siempre es correcto ya que data puede ser de un tipo hijo. En este caso habria que recibir por parametro el idto.
								//De todas maneras no es muy normal que, en este caso, una regla quiera asignar un valor a una property, sin que este valor este cargado en motor
								int idtoRange=kba.getClass(kba.getIdRange((ObjectProperty)prop));
								kba.loadIndividual(valueData, idtoRange, 1, userRol, idtoUserTask, sessionFirstSource);
							}
							
							kba.setValue(value, idProp, kba.buildValue(valueData,kba.getClass(valueData)), null, userRol, idtoUserTask, sessionFirstSource);
						}
					}
					
				}
				
				kba.setValue(idoQuestionTask, Constants.IdPROP_SOURCECLASS, kba.buildValue(value, valueCls), null, userRol, idtoUserTask, sessionFirstSource);
				
				commandPath cPath=new SetCommandPath(idoQuestionTask,idtoQuestionTask,value,valueCls,idtoUserTask,userRol,sess);
				cPath.setAlias(auxAliasLists);
				
				m_commandList.add(positionToInsert,cPath);
				
				listDependencyCommandPath.add(cPath);
				
				positionToInsert++;
			}
			if(m_currentStep!=-1)
				mapDependencyCommandPath.put(m_commandList.get(m_currentStep), listDependencyCommandPath);
			success=true;
		}finally{
			if(!success && sessionFirstSource!=null){
				sessionFirstSource.setForceParent(false);
				sessionFirstSource.rollBack();
			}
		}
		
		
		
	}
	
	public boolean setResultStep(final KnowledgeBaseAdapter kba,final IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException, NumberFormatException, AssignValueException {
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
			//sessionCoherence.addIchangeProperty(this, false);
			int value=form.getResult().keySet().iterator().next();
			try{
				boolean success=false;
				try{
					form.stopEdition(value,sessionCoherence);
					//sessionParent.checkCoherence(true);
					
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
				
				Value val=kba.buildValue(value, kba.getClass(value));
				
				kba.setValue(ido,Constants.IdPROP_CONFIRMED_SOURCE,val,null,userRol,idtoUserTask,sessionParent);
				kba.setValue(ido,Constants.IdPROP_CONFIRMED_SOURCE,null,val,userRol,idtoUserTask,sessionParent);
				
				if(!mapDataQuestionRequest.isEmpty()){
					ArrayList<IndividualValues> auxDataLists=new ArrayList<IndividualValues>();
					Iterator<Integer> itr=mapDataQuestionRequest.keySet().iterator();
					while(itr.hasNext()){
						auxDataLists.addAll(mapDataQuestionRequest.get(itr.next()));
					}
					//Cambiamos el orden ya que los ultimos los consideramos mas prioritarios de rellenar que los primeros ya que se han creado debido a una decision mas tardía y queremos que se gestionen los primeros
					Collections.reverse(auxDataLists);
					HashMap<String,String> auxAliasLists=new HashMap<String,String>();
					itr=mapAliasQuestionRequest.keySet().iterator();
					while(itr.hasNext()){
						auxAliasLists.putAll(mapAliasQuestionRequest.get(itr.next()));
					}
					buildStepsSource(kba, ido, idto, auxDataLists, auxAliasLists, userRol, idtoUserTask, sessionParent);
					mapDataQuestionRequest.clear();
				}
				
				
				if(this.nextIndex()==m_commandList.size()-1){//Si el siguiente es el ultimo paso
										
					//Cogemos el valor antes de marcar como finished la QuestionTask ya que en ese momento una regla cambia el valor de la property modulos_configurados.
					//Nos interesa antes del cambio, para saber si preguntar al usuario o no por la configuracion de los reports. Si es la primera vez los configuramos sin preguntarle ya que no puede haber modificado ningun diseño de report que pueda ser machacado.
					boolean doQuestionReports=false;
					if(rdn.equals(Constants.CLASSIFICATION_QUESTION_TASK_RDN)){
						int idto=kba.getIdClass("APLICACIÓN");
						Iterator<Integer> itr=kba.getIndividuals(idto, Constants.LEVEL_INDIVIDUAL, false);
						
						
						
						if(itr.hasNext()){
							int ido=itr.next();
							int idProp=kba.getIdProp("módulos_configurados");
							
							Object actualValue=kba.getValueData(kba.getField(ido, idto, idProp, null, null, sessionParent));
							
							if(actualValue!=null){
								doQuestionReports=Boolean.valueOf(((String)actualValue).split(":")[0]);
							}
						}else{
							System.err.println("No existe ningún individuo de Aplicación");
						}
					}
					
					kba.setValue(ido,kba.getIdProp("finished"),kba.buildValue(true, Constants.IDTO_BOOLEAN),null,userRol,idtoUserTask,sessionParent);
					form.confirm();
					
					if(rdn.equals(Constants.CLASSIFICATION_QUESTION_TASK_RDN)){
						boolean clasificateReports=true;
						if(doQuestionReports){
							Object[] options = {"Sí", "No"};
							int res = Singleton.getInstance().getMessagesControl().showOptionMessage("¿Desea adaptar los informes existentes a esta nueva configuración?\nTenga en cuenta que si modificó el diseño de algún informe perderá esos cambios.",
									Utils.normalizeLabel("Configuración de informes"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[0],form.getDialog().getParentDialog().getComponent());
		
							if (res != JOptionPane.YES_OPTION){
								clasificateReports=false;
							}
						}
						
						if(clasificateReports){
							//Lo hacemos al final del hilo del Thread para que se el raton aparezca como reloj al permitir que se termine de cerrar el formulario de clasificación 
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									form.getDialog().getParentDialog().disabledEvents();//Lo hacemos en el padre porque form ya esta cerrado cuando se llama a este metodo
									try{
										kba.getServer().serverReportsClasificator();
									}catch(Exception ex){
										ex.printStackTrace();
										Singleton.getInstance().getMessagesControl().showErrorMessage("No se han podido adaptar los informes. Sin embargo podrá utilizar la aplicación correctamente.", form.getDialog().getParentDialog().getComponent());
									}finally{
										form.getDialog().getParentDialog().enabledEvents();
									}
									
									loadBusinessApplication(form);
								}
							});
						}else{
							loadBusinessApplication(form);
						}
					}
					
					
					return false;//Evitamos que Assistant intente ejecutar otro paso
				}else{
					commandPath cPathNext=m_commandList.get(this.nextIndex());
					if(cPathNext.getSession()==null/*Si ha recibido mas de un request*/ || cPathNext.getSession().isFinished()/*Si se ha ido para atras en el asistente*/){
						Session session=kba.createDefaultSession(sessionParent,idtoUserTask,true, true, true, true, true);
						cPathNext.setSession(session);
					}
				}
				
			} catch (CardinalityExceedException e) {
				e.printStackTrace();
				Property prop=e.getProp();
				String message=e.getUserMessage();
				if (prop!=null){
					if (!prop.getIdo().equals(value)){
						message+=": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask) + " de "+kba.getLabelClass(prop.getIdto(), idtoUserTask)+" '"+kba.getValueData(kba.getRDN(prop.getIdo(), prop.getIdto(), userRol, idtoUserTask, sessionParent))+"'";	
					}else{
						message+=": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask);
					}
					
				}
				Singleton.getInstance().getMessagesControl().showErrorMessage(message,form.getDialog().getComponent());
				
				return false;
			}
		//System.err.println("commandList despues de result:"+m_commandList);
		return true;
	}

	private void loadBusinessApplication(IFormData form) {
		Object[] options = {"Sí", "No"};
		int res = Singleton.getInstance().getMessagesControl().showOptionMessage("¿Desea reiniciar la aplicación con los datos de negocio ya configurados?",
				Utils.normalizeLabel("Fin de la configuración"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE,
				null,
				options,
				options[0],form.getDialog().getParentDialog().getComponent());

		if (res == JOptionPane.YES_OPTION){
			try{
				Singleton.getInstance().getApplet().getAppletManager().reload(false);
			}catch(Exception ex){
				ex.printStackTrace();
				Singleton.getInstance().getMessagesControl().showErrorMessage("No se ha podido reiniciar la aplicación. Por seguridad, se recomienda que sea reiniciada por el usuario", form.getDialog().getParentDialog().getComponent());
			}
		}
	}
	
	
	public boolean setCancelStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
		//Si cancelamos el formulario yendo al paso anterior tenemos que hacer rollback de los datos cambiados en ese formulario. Sin embargo
		//esto provocaria el rollback del formulario padre ya que posee forceParent=true. Para evitar esto le modificamos el forceParent.
		
		commandPath cPath=m_commandList.get(m_currentStep);
		
		Session session=cPath.getSession();
	
		session.setForceParent(false);
		if(form instanceof IChangePropertyListener)
			session.removeIchangeProperty((IChangePropertyListener)form);
		session.rollBack();
		
		ArrayList<commandPath> dependency=mapDependencyCommandPath.get(m_commandList.get(previousIndex()));
		if(dependency!=null){
			mapDependencyCommandPath.remove(m_commandList.get(previousIndex()));
			m_commandList.removeAll(dependency);
		}

		mapDataQuestionRequest.clear();
		mapAliasQuestionRequest.clear();
		
		return true;
	}

	@Override
	public void endSteps() throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
		//System.err.println("ENTRAAA EN ENDSTEPS");
		//Singleton.getInstance().getKnowledgeBaseAdapter(session.getKnowledgeBase()).setListenerQuestion(oldQuestionListener);
		Singleton.getInstance().getQuestionTaskManager().removeQuestionTaskListener(session.getKnowledgeBase(),rdn);
		super.endSteps();
	}

	public void cancelled(int id,String rdn,IKnowledgeBaseInfo ik) {
		if(mapDataQuestionRequest.get(id)!=null){
			mapDataQuestionRequest.remove(id);
		}
	}


	@Override
	public void request(int id, String rdn,	ArrayList<IndividualValues> mapData, HashMap<String,String> alias, IKnowledgeBaseInfo ik) {
		if(Auxiliar.equals(rdn, this.rdn)){
			mapDataQuestionRequest.put(id,mapData);
			if(alias!=null){
				mapAliasQuestionRequest.put(id,alias);
			}
		}
	}

	
}

