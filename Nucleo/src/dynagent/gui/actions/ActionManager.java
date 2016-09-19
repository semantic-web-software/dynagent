
package dynagent.gui.actions;
/**
 * Se encarga de ejecutar acciones que son solicitadas mediante un commandPath
 * 
 * @author Francisco Javier Martinez
 */
import gdev.gawt.utils.GFocusTraversalPolicy;
import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.GConst;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Stack;

import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

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
import dynagent.common.knowledge.IExecuteActionListener;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.Domain;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.Utils;
import dynagent.gui.WindowComponent;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.*;
import dynagent.gui.forms.DataTransferControl;
import dynagent.gui.forms.ReportControl;
import dynagent.gui.forms.TableForm;
import dynagent.gui.forms.TransitionControlCommon;
import dynagent.gui.forms.filterControl;
import dynagent.gui.forms.transitionControl;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ActionManager implements IMessageListener{

	/*Stack<commandPath> m_actionStack;*/
	/*appControl m_controlMain;*/
	WindowComponent m_rootDialog;
	Dimension m_preferredSize;
	ArrayList<ArrayList<WindowComponent>> listDialogs;
	HashMap<WindowComponent,IStepListener> dialogListenerMap;
	//IChangePropertyListener m_currentCtr;
	
	//HashMap<JComponent,JDialog> dialogHash;
	//ActionIterator currentActionIterator;
	//Session currentSession;
	//HashMap<JDialog,Session> SessionsDialogMap;

	/*Pila para saber el orden de ventanas activas. Lo necesitamos para darle el foco a la ventana correcta ya que cuando se ejecuta en el navegador hay veces que
	el applet no tiene el foco al cerrarse una ventana hija*/
	private Stack<Window> windowsActiveStack;
	
	private boolean multiWindow;

	public ActionManager(Dimension preferredSize,/*appControl controlMain*/WindowComponent rootDialog,boolean multiWindow) {
		/*m_controlMain=controlMain;*/
		m_rootDialog=rootDialog;
		this.multiWindow=multiWindow;
		/*m_actionStack=new Stack<commandPath>();*/
		m_preferredSize=preferredSize;
		listDialogs=new ArrayList<ArrayList<WindowComponent>>();
		dialogListenerMap=new HashMap<WindowComponent, IStepListener>();
		//m_currentCtr=null;
		//dialogHash=new HashMap<JComponent, JDialog>();
		//currentActionIterator=null;
	
		windowsActiveStack=new Stack<Window>();
		windowsActiveStack.add(m_rootDialog.getComponent());
		
		//Hacemos nuestra propia gestion de las ventanas activas ya que no funciona del todo bien cuando utilizamos multiventana
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow",new PropertyChangeListener(){

	    	public void propertyChange(PropertyChangeEvent e) {
				
//					System.err.println("------PropertyChangeEvent:getPropertyName "+e.getPropertyName());
//					System.err.println("--PropertyChangeEvent:getNewValue "+(e.getNewValue()!=null?e.getNewValue().getClass():null));
//					System.err.println("--PropertyChangeEvent:getOldValue "+(e.getOldValue()!=null?e.getOldValue().getClass():null));
					if(e.getOldValue()!=null){
						
							final Window window=(Window)e.getOldValue();
							final Runnable doFinished = new Runnable() {
								public void run() {
									if(!window.isDisplayable())
										windowsActiveStack.remove(window);
									else{
										final Runnable doFinished = new Runnable() {
											public void run() {
												if(!window.isDisplayable())
													windowsActiveStack.remove(window);
											}
										};
										SwingUtilities.invokeLater(doFinished);
									}
								}
							};
							SwingUtilities.invokeLater(doFinished);
					}
					
					if(e.getNewValue()!=null){
						windowsActiveStack.remove((Window)e.getNewValue());
						
						//Quitamos las ventanas de las que se ha hecho dispose ya que a veces no llegan los eventos correctamente para que el codigo de remover el oldValue funcione correctamente
						//Ademas a veces, aunque lleguen bien, por velocidades de gestion de eventos no es capaz de removerlo, por lo que nos aseguramos aqui de hacerlo
						ArrayList<Window> listToDeleted=new ArrayList<Window>(windowsActiveStack);//Creamos listToDeleted para evitar la concurrentException
						for(Window window:listToDeleted){
							if(!window.isDisplayable())
								windowsActiveStack.remove(window);
						}
						
						windowsActiveStack.add((Window)e.getNewValue());
						
					}
					//System.err.println("WindowsActiveStack "+windowsActiveStack.size()+":"+windowsActiveStack);
				
			}
		});
	}

	public Session exeOperation(commandPath command,KnowledgeBaseAdapter kba,ITableNavigation tableNavigation,WindowComponent parentDialog,boolean modalWindow) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"operation","command="+command+" windowParent="+parentDialog.hashCode());
		if(command instanceof AutomaticActionCommandPath){
			exeAutoAction((AutomaticActionCommandPath) command,kba,parentDialog);
			return null;
		}else{
			ActionIterator itrSteps=ActionStepFactory.getInstance().createActionStep(command, parentDialog,kba);
			boolean success=false;
			try{
				Assistant assistant=new Assistant(itrSteps,kba,parentDialog,tableNavigation,modalWindow);
				/*int idtoUserTask=command.getIdtoUserTask();
				Integer userRol=command.getUserRol();
				Session ses=command.getSession();
				//try{
					kba.setUserTaskState(idoParent, Constants.IDO_PREVALIDANDO, userRol, idtoUserTask, ses);
					//System.err.println("Estado:"+kba.getProperty(idoParent, Constants.IdPROP_ESTADOREALIZACION, userRol, idtoUserTask, ses));
				//}finally{
					kba.setUserTaskState(idoParent, Constants.IDO_PENDIENTE, userRol, idtoUserTask, ses);
				//}*/
				assistant.start();
				success=true;
			}finally{
				if(!success)
					itrSteps.endSteps();//Si se produce una excepcion terminamos los pasos de la accion para que complete el dispose del motor creado
			}
			return itrSteps.getSession();
		}
	}

	private void exeAutoAction(AutomaticActionCommandPath command, KnowledgeBaseAdapter kba,WindowComponent parentDialog) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, ParseException, JDOMException, ActionException {
		int idoUserTask=command.getIdoUserTask();
		Integer idtoUserTask=command.getIdtoUserTask();
		String rdn=command.getRdn();//Usado para poder identificar por las reglas que estamos en una accion automatica 
		Integer userRol=null;
		ArrayList<Domain> sources=command.getSources();
		HashMap<String,Object> mapParamValue=new HashMap<String, Object>();
		IExecuteActionListener executeActionListener=command.getExecuteActionListener();
		
		Session parentSession=command.getSession();
		parentSession.setExecuteActionListener(executeActionListener);//Para escuchar, al terminar este paso, los siguientes pasos si los hubiera
		DefaultSession session=kba.createDefaultSession(parentSession,idtoUserTask,true, true, true, true,true);
		
		boolean success=false;
		try {
		
			DataProperty propRdn=kba.getRDN(idoUserTask,idtoUserTask, userRol, idtoUserTask, session);
			
			//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
			kba.setValue(idoUserTask, Constants.IdPROP_RDN, kba.buildValue(rdn, Constants.IDTO_STRING), propRdn.getUniqueValue(), userRol, idtoUserTask, session);
			
			ObjectProperty propSource=kba.getChild(idoUserTask,idtoUserTask,Constants.IdPROP_SOURCECLASS, userRol, idtoUserTask, session);
			
			//Primero desvinculamos los idos que haya ya en source porque solo nos interesa los que nosotros digamos. Si no seria un problema al volver atras en el asistente ya que la sesion sigue siendo la misma
			kba.setValue(idoUserTask, Constants.IdPROP_SOURCECLASS, null, propSource.getValues(), userRol, idtoUserTask, session);
	
			if(sources!=null && !sources.isEmpty()){
				LinkedList<Value> listValue=new LinkedList<Value>();
				HashMap<Integer,HashSet<Integer>> listIdos=new HashMap<Integer, HashSet<Integer>>();
				for(Domain source:sources){
					int value=source.getIdo();
					int valueCls=source.getIdto();
					//kba.loadIndividual(value, 1, userRol, idtoUserTask, sessionParent);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
					if(!listIdos.containsKey(valueCls))
						listIdos.put(valueCls, new HashSet<Integer>());
					listIdos.get(valueCls).add(value);
					listValue.add(kba.buildValue(value, valueCls));
				}
				kba.loadIndividual(listIdos, 1, true, true, userRol, idtoUserTask, session);//Cargarlo con varios niveles puede venir bien en algunos casos pero en otros penalizar
				
				kba.setValue(idoUserTask, Constants.IdPROP_SOURCECLASS, listValue, null, userRol, idtoUserTask, session);
				
				//try{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PREVALIDANDO, userRol, idtoUserTask, session);
				//}finally{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, session);
				//}
			}
			int idProp=Constants.IdPROP_PARAMS;
			int idRange=-1;
			
			boolean hasParams=false;
			try {
				ObjectProperty property=kba.getChild(idoUserTask,idtoUserTask,idProp, userRol, idtoUserTask, session);
				if(property.getTypeAccess().getViewAccess()){
					//System.out.println("Property del asistente action:"+property+" idObject:"+idObject+" idProp:"+idProp);
					idRange=kba.getIdRange(property/*,valueCls*/);//idRange es un idto ya que no se esta creando filtro para Params
					hasParams=true;
				}
			} catch (NotFoundException e) {
				
			}
							
			//boolean create=true;
			if(hasParams){
				
				
				int paramsIdto=idRange;
				int paramsIdo=kba.createPrototype(paramsIdto, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session);
				
				for(String propName:mapParamValue.keySet()){
					int paramsIdProp=kba.getIdProp(propName);
					Property prop=kba.getProperty(paramsIdo,paramsIdto,paramsIdProp, userRol, idtoUserTask, session);
					Integer valueCls=null;
					if(prop instanceof DataProperty){
						valueCls=((DataProperty)prop).getDataType();
					}else{
						valueCls=kba.getIdRange((ObjectProperty)prop);
					}
					
					Value newVal=kba.buildValue(mapParamValue.get(propName),valueCls);
					Value oldVal=prop.getUniqueValue();
					
					kba.setValue(paramsIdo, paramsIdProp, newVal, oldVal, userRol, idtoUserTask, session);
				}
				
			}
			
			//try{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_INFORMADO, userRol, idtoUserTask, session);
			//}finally{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, session);
			//}
				
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_REALIZADO, userRol, idtoUserTask, session);
			//}finally{
				kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, session);
			
				ObjectProperty prop=kba.getChild(idoUserTask, idtoUserTask, Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, session);
				LinkedList<Value> valuesTarget=prop.getValues();
				/*if(valuesTarget.isEmpty()){
					ActionException ex=new ActionException("Para los datos elegidos la acción no ha generado ningún resultado. No hay datos en el targetClass");
					ex.setUserMessage("Para los datos elegidos la acción no ha generado ningún resultado.\nAsegúrese de haber seleccionado un registro válido de la tabla.");
					throw ex;
				}*/
				
			session.commit();
			success=true;
		}finally{
			if(!success){
				session.rollBack();
			}
		}
			
	}

//	public IFormData exeNextStep(ActionIterator itrStep,JDialog dialog,ActionListener listener){
//	IFormData formData=null;
//	if(itrStep.hasNext()){
//	commandPath command=itrStep.next();
//	System.err.println(command);

//	exeStep(command,dialog,listener);
//	}
//	return formData;
//	}

//	public IFormData exePreviousStep(ActionIterator itrStep,JDialog dialog,ActionListener listener){
//	IFormData formData=null;
//	if(itrStep.hasPrevious()){
//	commandPath command=itrStep.previous();
//	System.err.println(command);

//	exeStep(command,dialog,listener);
//	}
//	return formData;
//	}

	protected IFormData exeStep(commandPath command,final KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,ITableNavigation tableNavigation,JPanel botoneraExtInicio,JPanel botoneraExtFin,MouseListener mouseListener) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException{
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"step","command="+command+" window="+dialog.hashCode());
		IFormData formData=null;
		if(command instanceof FindRelCommandPath || command instanceof FindCommandPath)
			formData=showFilter(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin,mouseListener);		
		else if(command instanceof SelectCommandPath || command instanceof SetMultipleCommandPath || command instanceof ViewMultipleCommandPath)
			formData=showTable(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin, mouseListener);		
		else if(command instanceof RelCommandPath)
			formData=relObject(command,kba,dialog);		
		else if(command instanceof SetCommonCommandPath || command instanceof ViewCommonCommandPath)
			formData=showCommonObject(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin);
		else if(command instanceof ReportCommandPath)
			formData=showReport(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin);
		else if(command instanceof DataTransferCommandPath)
			formData=showDataTransfer(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin);
		else
			formData=showObject(command,kba,dialog,stepListener,botoneraExtInicio,botoneraExtFin,tableNavigation);

		GConst.addShortCut(null, formData.getComponent(), GConst.RULES_DEBUG, GConst.RULES_DEBUG_MODIFIERS, "RulesDebug", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				kba.getKnowledgeBase().setPrintRules(!kba.getKnowledgeBase().isPrintRules());
				System.err.println("Depuracion reglas:"+kba.getKnowledgeBase().isPrintRules());
			}
			
		});
		dialogListenerMap.put(dialog, stepListener);
		return formData;
	}
	
//	private JPanel buildBotoneraTableNavigation( int idTarget, int idtoUserTask, ActionListener listener){
//		JPanel botonera= null;
//
//		/*JMenu menuClone=tg.cloneAcciones();
//	if( menuClone!=null ){
//            JMenuBar menu = new JMenuBar();
//            menu.add(menuClone);
//	    botonera.add( menu );
//        }*/
//		/*if( idTarget>=0 ){*/
//		botonera= new JPanel();
//		IdObjectForm idObject=new IdObjectForm();
//		idObject.setIdtoUserTask(idtoUserTask);
//		idObject.setIdo(idTarget);
//
//		IdOperationForm idOperation=new IdOperationForm();
//		idOperation.setOperationType(botoneraAccion.OPERATION_SCROLL);
//		idOperation.setTarget(idObject);
//		idOperation.setButtonType(botoneraAccion.PREV);
//
//		String idString=idOperation.getIdString();
//
//		botoneraAccion.subBuildBoton(botonera,
//				null,
//				"prev",
//				/*"SCROLL:" + idTarget+":" + idtoUserTask + ":" +
//                                         botoneraAccion.PREV,*/
//				/*"SCROLL:-1:" + idtoUserTask + ":" +
//                                         botoneraAccion.PREV*/idString,
//                                         Utils.normalizeLabel("IR AL ANTERIOR"),
//                                         listener,(int) botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()),(int) botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()), true, Singleton.getInstance().getComm());
//
//		idOperation.setButtonType(botoneraAccion.NEXT);
//		idString=idOperation.getIdString();
//
//		botoneraAccion.subBuildBoton(botonera,
//				null,
//				"next",
//				/*"SCROLL:" + idTarget+":" + idtoUserTask + ":" +
//                                         botoneraAccion.NEXT,*/
//				/*"SCROLL:-1:" + idtoUserTask + ":" +
//                                             botoneraAccion.NEXT*/idString,
//                                             Utils.normalizeLabel("IR AL SIGUIENTE"),
//                                             listener,(int) botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()),(int) botoneraAccion.getButtonHeight(Singleton.getInstance().getGraphics()), true, Singleton.getInstance().getComm());
//
//		/*}*/
//		
//		return botonera;
//	}

	private IFormData relObject(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog) throws NotFoundException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, CardinalityExceedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		RelCommandPath relCommandPath=(RelCommandPath)command;
		Integer idtoUserTask=relCommandPath.getIdtoUserTask();
		Integer userRol=relCommandPath.getUserRol();
		int ido=relCommandPath.getIdo();
		int idProp=relCommandPath.getIdProp();
		int value=relCommandPath.getValue();
		int valueCls=relCommandPath.getValueCls();
		Session session=relCommandPath.getSession();

		try {
			kba.getRDN(value, valueCls, userRol, idtoUserTask, session);	
			kba.setValue(/*property,*/ido,idProp, kba.buildValue(value,/*idtoFilter*/valueCls),null/*, new session()*/,/*operation*/userRol,idtoUserTask,session);
			//System.err.println("Despues de relacionar full:"+kba.getProperty(ido,idProp, userRol, idtoUserTask, null));
			session.commit();
			closeForm(dialog,kba,true);

			Singleton.getInstance().getMessagesControl().showMessage("Objeto relacionado directamente ya que no tiene campos editables",dialog.getComponent());
		} catch (CardinalityExceedException e) {
			Property prop=e.getProp();
			if (prop!=null){
				if (prop.getIdo()!=ido){
					Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage()+": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask) + " de "+kba.getLabelClass(prop.getIdto(), idtoUserTask)+" '"+kba.getValueData(kba.getRDN(prop.getIdo(), prop.getIdto(), userRol, idtoUserTask, session))+"'",dialog.getComponent());
				}else{
					Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage()+": "+kba.getLabelProperty(prop, prop.getIdto(), idtoUserTask),dialog.getComponent());
				}
			}else{
				Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage(),dialog.getComponent());
			}

		}

		return null;
	}
	
	private IFormData showReport(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException{
		/*int ido=command.ido;
			int idtoUserTask=command.idtoUserTask;
			int operation=command.operation;
			int idProp=command.idProp;*/
		Session session = command.getSession();
		session.setRulesGroup(Constants.REPORTS_RULES);
		
		//currentSession=session;
		//SessionController.getInstance().setActual(session);

		int ido=-1;
		int idto=-1;
		int operation=-1;
		int buttonsType=-1;
		Integer idtoUserTask=null;
		Integer userRol=null;
		String title=null;
		String objectTitle=null;
		/*String message=null;*/
		int idoParent=-1;
		ObjectProperty property=null;
		boolean endStep=stepListener.isLastStep();

		//if(command instanceof ReportCommandPath){
			ReportCommandPath reportCommandPath=(ReportCommandPath)command;
			property=(ObjectProperty)reportCommandPath.getProperty();
			//value=kba.getIdRange(property);
			idoParent=property.getIdo();
			/*Siendo de nivel prototipe reportControl escucha los cambios ya que hereda de transitionControl*/
			ido=kba.createPrototype(kba.getIdRange(property), /*Constants.LEVEL_FILTER*/Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session);
			idto=kba.getClass(ido);
			HashMap<Integer,Integer> values=reportCommandPath.getValues();
			
			
			if(values!=null && !values.isEmpty()){
				//Cargamos solo el primer nivel y sin estructurales ya que para la tabla solo necesitamos los de primer nivel y asi evitamos una perdida de rendimiento
				HashMap<Integer,HashSet<Integer>> idosToLoad=new HashMap<Integer, HashSet<Integer>>();
				Iterator<Integer> itrIdos=values.keySet().iterator();
				while(itrIdos.hasNext()){
					int idoToLoad=itrIdos.next();
					int idtoToLoad=values.get(idoToLoad);
					if(!idosToLoad.containsKey(idtoToLoad))
						idosToLoad.put(idtoToLoad, new HashSet<Integer>());
					idosToLoad.get(idtoToLoad).add(idoToLoad);
				}
				
				if(!idosToLoad.isEmpty()){
					kba.loadIndividual(idosToLoad, 1, false, false, userRol, idtoUserTask, session);
				}
				
				int valueFirst=values.keySet().iterator().next();
				Iterator<ObjectProperty> itr=kba.getChildren(ido, idto, userRol, idtoUserTask, session);
				Property prop=null;
				int valueClsFirst=values.get(valueFirst);
				while(itr.hasNext() && prop==null){
					ObjectProperty oProperty=itr.next();
					int valueCls=kba.getClass(kba.getIdRange(oProperty));
					if(valueCls==valueClsFirst || kba.isSpecialized(valueClsFirst, valueCls)){
						prop=oProperty;
					}
				}
				if(prop!=null){
					Iterator<Integer> itrValues=values.keySet().iterator();
					int i=0;
					while(itrValues.hasNext() && (prop.getCardMax()==null || prop.getCardMax()>i)){
						int value=itrValues.next();
						int valueCls=values.get(value);
						kba.setValue(ido, prop.getIdProp(), kba.buildValue(value, valueCls), null, userRol, idtoUserTask, session);
						i++;
					}
				}
			}
		
			
			idtoUserTask=reportCommandPath.getIdtoUserTask();
			userRol=reportCommandPath.getUserRol();
			operation=access.SET;
			buttonsType=botoneraAccion.RECORD_TYPE;
			//selectionMode=true;
			title="Parámetros del informe";
			objectTitle=kba.getLabelUserTask(idtoUserTask);
		//}
		
		
		/*			if(m_currentCtr!=null)
				session.addIchangeProperty(m_currentCtr);
		 */			

		//TODO Hay que decidir si son necesarios los accesos de property en la que estamos ya que se lo estoy pasando a null, ahora mismo no se tienen en cuenta
		//HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(ido, userRol, null, idtoUserTask);
		AccessAdapter accessAdapter=null;//new AccessAdapter(accessUserTasks,null);

		IdObjectForm idObjectOperation=new IdObjectForm();
		idObjectOperation.setIdo(ido);//Luego no es usado, de momento, en actionPerformed. Lo dejo por si acaso
		idObjectOperation.setIdtoUserTask(idtoUserTask);
		String idString=idObjectOperation.getIdString();

		botoneraAccion botonera=new botoneraAccion(
				idString,null,null,null,null,null,null,null,null,false,
				buttonsType,
				botoneraExtInicio, botoneraExtFin,
				null,
				null,
				accessAdapter,
				(operation==access.VIEW),
				endStep,
				Singleton.getInstance().getGraphics(),
				kba.getServer(),null,kba.canSetUpColumnProperty());

		ReportControl rCtr= new ReportControl(
				session,
				userRol,
				idoParent,
				/*tableIndex,*/ido,
				idto,
				/*command,*/idtoUserTask,
				operation,
				m_preferredSize,
				/*null*/botonera.getComponent(), kba, dialog);

		botonera.addListener(rCtr);
		rCtr.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));
		
		/*int x=100,y=100;
			if(m_parentDialog!=null){
				x=(int)m_form.getLocationOnScreen().getX()+40;
				y=(int)m_form.getLocationOnScreen().getY()+40;
			}
		 	dlg.setLocation(x,y);*/
//		final transitionControl transitionCtrThis=tCtr;
//		/*final ActionIterator itrStepThis=itrStep;*/
//		//final String titleThis=Utils.normalizeWindowTitle(title,objectTitle);
//		final DialogModal dialogThis=dialog;
//		/*final String mensajeThis=message;*/
//		SwingWorker worker=new SwingWorker(){
//			public Object construct(){
//				/*final Runnable doFinished = new Runnable() {
//	    				public void run() { Singleton.getInstance().getStatusBar().setAccion(mensajeThis); }
//	    		    };
//	                SwingUtilities.invokeLater(doFinished);*/
//				showDialog(dialogThis,transitionCtrThis.getComponent()/*,titleThis*/);
//				System.out.println("FIN SHOW");
//				return null;
//			}
//
//			public void finished(){
//				StatusBar statusBar=Singleton.getInstance().getStatusBar();
//				if(statusBar.getNivelLocalizacion()==2)
//				//if(statusBar.getNivelLocalizacion()>0)
//					statusBar.upNivelLocalizacion();
//
//				//exeStep(itrStepThis);
//			}
//		};
//		worker.start();

		return rCtr;
	}

	private IFormData showTable(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin,MouseListener mouseListener) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException {
		Session session=command.getSession();
		Integer ido=-1;
		Integer operation=null;
		int buttonsType=botoneraAccion.RECORD_TYPE;
		Integer idtoUserTask=null;
		Integer userRol=null;
		String title=null;
		String objectTitle=null;
		ObjectProperty property=null;
		Integer valueCls=null;
		AccessAdapter accessAdapter=null;
		boolean selectionMode=false;
		boolean tablaConBotonera=true;
		HashMap<Integer,Integer> values = new HashMap<Integer,Integer>();
		boolean endStep=stepListener.isLastStep();
		
		HashMap<Integer,ArrayList<Integer>> mapDirectReports=null;//Mapa IdtoTargetClass,ArrayList de idtoDirectReport
		HashMap<Integer, String> idtoNameActions=null;
		HashMap<Integer, String> idtoExports=null;
		
		if(command instanceof ViewMultipleCommandPath){

			ViewMultipleCommandPath viewMultipleCommandPath=(ViewMultipleCommandPath)command;
			values=viewMultipleCommandPath.getValues();
			idtoUserTask=viewMultipleCommandPath.getIdtoUserTask();
			userRol=viewMultipleCommandPath.getUserRol();
			operation=access.VIEW;
			property=viewMultipleCommandPath.getProperty();
			ido=property.getIdo();
			valueCls=kba.getIdRange(property);

			HashMap<Integer,HashSet<Integer>> idosToLoad=new HashMap<Integer, HashSet<Integer>>();
			Iterator<Integer> itrIdos=values.keySet().iterator();
			while(itrIdos.hasNext()){
				int idoToLoad=itrIdos.next();
				if(!kba.isLoad(idoToLoad)){
					int idtoToLoad=values.get(idoToLoad);
					if(!idosToLoad.containsKey(idtoToLoad))
						idosToLoad.put(idtoToLoad, new HashSet<Integer>());
					idosToLoad.get(idtoToLoad).add(idoToLoad);
				}
			}

			if(!idosToLoad.isEmpty()){
				// Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
				kba.loadIndividual(idosToLoad, userRol, idtoUserTask, session);
			}

			title="Consulta de ";
			objectTitle=kba.getLabelClass(valueCls,idtoUserTask);

			HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(valueCls, userRol, property.getTypeAccess(), idtoUserTask);
			accessAdapter=new AccessAdapter(accessUserTasks,property,false,false);
			//if(!stepListener.isMultiStep())//Si no se trata de una accion de varios pasos no hay que confirmar nada
			if(idtoUserTask==null/*Ocurre si estamos en una QuestionTask*/ || !kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION))
				buttonsType=botoneraAccion.VIEW_TYPE;

		}else if(command instanceof SetMultipleCommandPath){

			SetMultipleCommandPath setMultipleCommandPath=(SetMultipleCommandPath)command;
			values=setMultipleCommandPath.getValues();
			idtoUserTask=setMultipleCommandPath.getIdtoUserTask();
			userRol=setMultipleCommandPath.getUserRol();
			operation=access.SET;
			property=setMultipleCommandPath.getProperty();
			ido=property.getIdo();
			valueCls=kba.getIdRange(property);

			HashMap<Integer,HashSet<Integer>> idosToLoad=new HashMap<Integer, HashSet<Integer>>();
			Iterator<Integer> itrIdos=values.keySet().iterator();
			while(itrIdos.hasNext()){
				int idoToLoad=itrIdos.next();
				if(!kba.isLoad(idoToLoad)){
					int idtoToLoad=values.get(idoToLoad);
					if(!idosToLoad.containsKey(idtoToLoad))
						idosToLoad.put(idtoToLoad, new HashSet<Integer>());
					idosToLoad.get(idtoToLoad).add(idoToLoad);
				}
			}
			if(!idosToLoad.isEmpty()){
				// Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
				kba.loadIndividual(idosToLoad, userRol, idtoUserTask, session);
			}
			title="Edición de ";
			objectTitle=kba.getLabelClass(valueCls,idtoUserTask);

			HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(valueCls, userRol, property.getTypeAccess(), idtoUserTask);
			accessAdapter=new AccessAdapter(accessUserTasks,property,false,false);
			
			Integer idoUserTask=kba.getIdoUserTask(idtoUserTask);
			if(idoUserTask==null)//Si es null podría ser una accion
				idoUserTask=kba.getIdoUserTaskAction(idtoUserTask);
			if(idoUserTask==null)//Si es null podría ser una exportacion
				idoUserTask=kba.getIdoUserTaskExport(idtoUserTask);
			
			if(Auxiliar.equals(dialog.getParentDialog(),dialog.getMainDialog()) && idoUserTask!=null && endStep){//Si es null significa que no es ni una usertask ni una accion
				Integer areaFuncional=kba.getIdoValue(kba.getChild(idoUserTask, idtoUserTask, Constants.IdPROP_MYFUNCTIONALAREA, userRol, idtoUserTask, session));
							
				idtoNameActions = new HashMap<Integer, String>();
				mapDirectReports= new HashMap<Integer, ArrayList<Integer>>();
				idtoExports=new HashMap<Integer, String>();
				HashSet<Integer> valueClsList=new HashSet<Integer>();
				for(int idoValue:values.keySet()){
					int valueClsOfValue=values.get(idoValue);
					ArrayList<Integer> directReports=kba.getIdtoUserTasksDirectReport(valueClsOfValue,areaFuncional);//TODO En este metodo parece que no se miran especializados, cuidado
					if(!directReports.isEmpty())
						mapDirectReports.put(valueClsOfValue, directReports);
					//Comentada logica que buscaba en los individuos que tiene la tabla para saber que acciones mostrar. Ahora miramos en el rango de la property 	
//					valueClsList.add(values.get(idoValue));
				}
				valueClsList.add(kba.getClass(valueCls));
				
				for(int valueClsOfValue:valueClsList){
//					ArrayList<Integer> directReports=kba.getIdtoUserTasksDirectReport(valueClsOfValue,areaFuncional);//TODO En este metodo parece que no se miran especializados, cuidado
//					if(!directReports.isEmpty())
//						mapDirectReports.put(valueClsOfValue, directReports);
					Iterator<Integer> itrIdosUTask=kba.getIdoUserTasksAction(valueClsOfValue,areaFuncional);
					while(itrIdosUTask.hasNext()){
						int idoAction=itrIdosUTask.next();
						int idtoAction=kba.getIdtoUserTaskAction(idoAction);
						
						ObjectProperty propertyTarget=kba.getChild(idoAction,idtoAction,Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, kba.getDefaultSession());
						int idRangeTarget=kba.getIdRange(propertyTarget);
						int idtoRangeTarget=kba.getClass(idRangeTarget);
						
						if(kba.hasShowAction(idoAction, idtoAction, null, idtoRangeTarget, idtoUserTask, userRol)){
							String value = kba.getLabelUserTask(idtoAction);
							idtoNameActions.put(idtoAction, value);
						}//else System.err.println("WARNING: La acción "+kba.getLabelUserTask(idtoAction)+" ha sido excluida en "+kba.getLabelClass(kba.getClass(ido),idtoUserTask)+" por falta de permisos");
					}
					
					itrIdosUTask=kba.getIdoUserTasksExport(valueClsOfValue,areaFuncional);
					while(itrIdosUTask.hasNext()){
						int idoExport=itrIdosUTask.next();
						int idtoExport=kba.getIdtoUserTaskExport(idoExport);
						
						String value = kba.getLabelUserTask(idtoExport);
						idtoExports.put(idtoExport, value);
					}
				}
			}
			
		}else if(command instanceof SelectCommandPath){

			SelectCommandPath selectCommandPath=(SelectCommandPath)command;
			values = selectCommandPath.getRows();
			idtoUserTask=selectCommandPath.getIdtoUserTask();
			userRol=selectCommandPath.getUserRol();
			operation=access.VIEW;
			property=selectCommandPath.getProperty();
			ido=property.getIdo();
			valueCls=kba.getIdRange(property);
			
			title="Selección del tipo de";
			objectTitle=kba.getLabelClass(valueCls,idtoUserTask);

			//accessAdapter=null;
			selectionMode=true;
			tablaConBotonera=false;
			//actualizarButton=true;
		}

		IdObjectForm idObjForm = new IdObjectForm();
		idObjForm.setIdProp(property.getIdProp());
		idObjForm.setIdo(ido);
		idObjForm.setIdtoUserTask(idtoUserTask);
		idObjForm.setValueCls(valueCls);

		String idTable=idObjForm.getIdString();

		botoneraAccion botoneraTabla=null;
		if(tablaConBotonera)
			botoneraTabla=new botoneraAccion(
					
					idTable,property.getName(),null,null,null,null,null,null,null,false,
					/* null, */
					botoneraAccion.TABLE_TYPE,
					null,
					null,
					null,
					null,
					/* myAccess, *//* operations, *//* accessUserTasks, */accessAdapter,
					(operation==access.VIEW),
					endStep,
					Singleton.getInstance().getGraphics(), kba.getServer(),null,kba.canSetUpColumnProperty());

		botoneraAccion botonera = new botoneraAccion(
				idTable, null, null, null, idtoNameActions, null,null,idtoExports,null,false,buttonsType, botoneraExtInicio, botoneraExtFin, null, null,
				/*accessAdapter*/null,(operation==access.VIEW),endStep,
				Singleton.getInstance().getGraphics(), kba.getServer(),null,kba.canSetUpColumnProperty());

		TableForm tableForm=new TableForm(session,property,values,Singleton.getInstance().getMessagesControl(),idtoUserTask,userRol,operation,m_preferredSize,botoneraTabla!=null?botoneraTabla.getComponent():null,botonera.getComponent(), selectionMode, kba, dialog, mouseListener, mapDirectReports);
		botonera.addListener(tableForm);
		tableForm.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));
		
		if(tablaConBotonera)
			botoneraTabla.addListener(tableForm);

		//final JComponent componentThis = tableForm.getComponent();
		//final String titleThis=Utils.normalizeWindowTitle(title,objectTitle);
		//final DialogModal dialogThis=dialog;

//		SwingWorker worker = new SwingWorker() {
//			public Object construct() {
//				showDialog(dialogThis,componentThis/*,titleThis*/);
//				System.out.println("FIN SHOW");
//				return null;
//			}
//			public void finished() {}
//		};
//		worker.start();

		return tableForm;
	}

	private IFormData showFilter(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin,MouseListener mouseListener) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{

		Integer nMaxCurr = null;
//		OperationsObject operations = new OperationsObject(/* access,m_idtoUserTask */);
		/* operations.addOperations(UserTasksTarget); */

		/*
		 * IdObjectForm idObjForm=new IdObjectForm(idTable); int
		 * idPropTable=idObjForm.getIdProp();
		 */
		Session session = command.getSession();
		//listSessions.add(session);
		//currentSession=session;

		int ido=-1;
		int idProp=-1;
		int value=-1;
		int valueCls=-1;
		int operation=access.SET;
		int buttonsType=-1;
		Integer idtoUserTask=command.getIdtoUserTask();
		Integer userRol=command.getUserRol();
		ObjectProperty property=null;
		AccessAdapter accessAdapter=null;
		//instance instance=null;
		boolean selectionMode=false;
		String title=null;
		String objectTitle=null;
		MouseListener mouseList=null;
		boolean endStep=stepListener.isLastStep();
		
		if(command instanceof FindCommandPath){
			FindCommandPath findCommandPath=(FindCommandPath)command;
			value=findCommandPath.getValue();
			property=(ObjectProperty)findCommandPath.getProperty();
			ido=property.getIdo();
			idProp=property.getIdProp();
			nMaxCurr = property.getCardMax();
			valueCls=kba.getClass(value);

			kba.completeFilterLevels(value, valueCls, userRol, idtoUserTask, Constants.MAX_DEPTH_SEARCH_FILTERS,session);
			HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(value,userRol,property.getTypeAccess(),idtoUserTask);
			accessAdapter=new AccessAdapter(accessUserTasks,property,false,true);
			selectionMode=false;//true; //TODO True se usaba para las businessClass
			mouseList=mouseListener;//Para que el asistente escuche el doble click sobre la tabla y avance al paso siguiente
			title="Selección de";
			objectTitle=kba.getLabelClass(valueCls,idtoUserTask);
			buttonsType=botoneraAccion.LINKING_TYPE;//SEARCH_TYPE;
		}else if(command instanceof FindRelCommandPath){
			FindRelCommandPath findRelCommandPath=(FindRelCommandPath)command;
			ido=findRelCommandPath.getIdo();
			idProp=findRelCommandPath.getIdProp();
			valueCls=kba.getClass(findRelCommandPath.getValueCls());
			int idto=kba.getClass(ido);
			property=kba.getChild(ido, idto, idProp, userRol, command.getIdtoUserTask(), session);
			nMaxCurr = property.getCardMax();
			//instance=findRelCommandPath.getInstance();

			value=kba.getIdRange(property, valueCls);
			
			kba.completeFilterLevels(value, valueCls, userRol, idtoUserTask, Constants.MAX_DEPTH_SEARCH_FILTERS,session);
			HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(value,userRol,property.getTypeAccess(),idtoUserTask);
			accessAdapter=new AccessAdapter(accessUserTasks,property,false,true);
			title="Selección de";
			objectTitle=kba.getLabelClass(valueCls,idtoUserTask);
			buttonsType=botoneraAccion.LINKING_TYPE;
			
			if(kba.getLevelObject(ido)==Constants.LEVEL_FILTER)
				operation=access.VIEW;
		}/*else if(command instanceof ReportCommandPath){
			ReportCommandPath reportCommandPath=(ReportCommandPath)command;
			property=(ObjectProperty)reportCommandPath.getProperty();
			nMaxCurr = property.getCardMax();
			//value=kba.getIdRange(property);
			value=kba.createPrototype(kba.getIdRange(property), Constants.LEVEL_FILTER, userRol, idtoUserTask, session);
			actionMode=filterControl.REPORT_MODE;
			//selectionMode=true;
			title="Parámetros del informe";
			objectTitle=null;
		}*/

		IdObjectForm idObjForm = new IdObjectForm();
		idObjForm.setIdProp(idProp);
		idObjForm.setIdo(ido);
		idObjForm.setIdtoUserTask(idtoUserTask);
		idObjForm.setValueCls(valueCls);

		String idTable=idObjForm.getIdString();

		/*			Integer userRol=kba.getUserRolRestrictive(userRols);*/

		/*int idto=kba.getIdtoFilter(property);*/

		//System.out.println("showFilter: Property:"+property);

		/*JPanel botoneraAssistant=null;
			if(itrStep.hasNext())
				botoneraAssistant=buildBotoneraAssistant();*/

		botoneraAccion botonera = new botoneraAccion(
				idTable, null, null, null, null, null,null,null,null,false,buttonsType, botoneraExtInicio, botoneraExtFin, null, null,
				/*operations,*//*accessUserTasks*/accessAdapter,(operation == access.VIEW),endStep,
				Singleton.getInstance().getGraphics(), kba.getServer() ,null,kba.canSetUpColumnProperty());

		//Dimension dim = m_preferredSize;

		filterControl filterCtr = new filterControl(session, idTable, 
				idtoUserTask, userRol, property, value, valueCls, true, false, botonera,
				false, filterControl.defaultAnchoSelector,true, nMaxCurr, selectionMode, mouseList, null, kba, dialog/*, instance*/);
		botonera.addListener(filterCtr);
		filterCtr.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));
		
		/* showDialog((Container)filterCtx.getComponent()); */
//		final filterControl filterCtrThis = filterCtr;
//		/*final ActionIterator itrStepThis=itrStep;*/
//		//final String titleThis=Utils.normalizeWindowTitle(title,objectTitle);
//		final DialogModal dialogThis=dialog;
//		final boolean selectionModeThis=selectionMode;
////		final int idoThis=ido;
////		final int idPropThis=idProp;
////		final Integer idtoUserTaskThis=idtoUserTask;
////		final Integer userRolThis=userRol;
////		final Session sessionThis=session; 
//		SwingWorker worker = new SwingWorker() {
//			public Object construct() {
//				showDialog(dialogThis,filterCtrThis.getComponent()/*,titleThis*/);
////				if(filterCtrThis.isSelectioned()){
////				selectData selection=filterCtrThis.getResultDataSelection();
////				/*if(itrStepThis.hasNext())
////				itrStepThis.setResultStep(values);
////				else{*/
////				Iterator<instance> itr=selection.getIterator();
////				while(itr.hasNext()){
////				instance inst=itr.next();
////				Value valueObject=kba.getValueOfString(inst.getIDO()+"", inst.getIdTo());
////				System.out.println("Value para rel:"+valueObject);
////				System.out.println("Antes rel:"+kba.getProperty(idoThis, idPropThis, userRolThis, idtoUserTaskThis, sessionThis));
////				try {
////				kba.setValue(idoThis,idPropThis, valueObject,null, /*action.NEW*/userRolThis,idtoUserTaskThis, sessionThis);
////				} catch (CardinalityExceedException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////				} catch (OperationNotPermitedException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////				} catch (IncompatibleValueException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////				} catch (NotFoundException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////				}
////				System.out.println("Resultado rel:"+kba.getProperty(idoThis, idPropThis, null, idtoUserTaskThis, sessionThis));
////				}
//
////				/*}*/
////				}
//				System.out.println("FIN SHOW");
//				/*disposeDialog();*/
//				return null;
//			}
//
//			public void finished() {
//				StatusBar statusBar = Singleton.getInstance().getStatusBar();
//				 if(statusBar.getNivelLocalizacion()==2)
//				//if(statusBar.getNivelLocalizacion()>0)
//					if(!selectionModeThis)
//						statusBar.upNivelLocalizacion();
//
//				//exeStep(itrStepThis);
//			}
//		};
//		worker.start();

		return filterCtr;
	}

	private IFormData showObject(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin,ITableNavigation tableNavigation) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException{
		/*int ido=command.ido;
			int idtoUserTask=command.idtoUserTask;
			int operation=command.operation;
			int idProp=command.idProp;*/

		Session session = command.getSession();
		//currentSession=session;
		//SessionController.getInstance().setActual(session);
		
		int ido=-1;
		int idto=-1;
		int idRange=-1;
		int operation=-1;
		Integer idtoUserTask=null;
		Integer userRol=null;
		String title=null;
		String objectTitle=null;
		/*String message=null;*/
		int idoParent=-1;
		int idtoParent=-1;
		boolean endStep=stepListener.isLastStep();
		int buttonsType=botoneraAccion.RECORD_TYPE;
		ArrayList<Integer> directReports=null;
		HashMap<Integer, String> idtoNameActions=null;
		HashMap<Integer, String> idtoExports=null;
		HashMap<String,String> alias=command.getAlias();

		if(command instanceof NewRelCommandPath){
			NewRelCommandPath newRelCommandPath=(NewRelCommandPath)command;
			idoParent=newRelCommandPath.getIdo();
			idtoParent=newRelCommandPath.getIdto();
			int idProp=newRelCommandPath.getIdProp();
			int level=newRelCommandPath.getLevel();
			idRange=newRelCommandPath.getValue();
			idtoUserTask=newRelCommandPath.getIdtoUserTask();
			userRol=newRelCommandPath.getUserRol();
			operation=access.NEW;

			ObjectProperty property=kba.getChild(idoParent,idtoParent,idProp,userRol,idtoUserTask,session);
			/*int idClass=property.getRangoList().getFirst();*/
			//int idoFilter=kba.getIdoFilter(property);
			//int idtoFilter=kba.getIdtoFilter(property);
			/*int idObject=kba.createPrototype(idClass, userRol, idtoUserTask);*/
			ido=kba.createPrototype(/*idClass*//*idtoFilter*/idRange,level, /*new session(),*/ userRol, idtoUserTask,session);
			//System.err.println("ido:"+ido+" idProp:"+idProp+" valueCls:"+idto+" value:"+kba.getValueOfString(String.valueOf(ido),/*idtoFilter*/kba.getClass(idto)));
			idto=kba.getClass(ido);

			if((property.getCardMax()!=null && property.getCardMax()==1)){
				kba.setValue(/*property,*/idoParent,idProp, kba.buildValue(ido,/*idtoFilter*/kba.getClass(idRange)),property.getUniqueValue()/*, new session()*/,/*operation*/userRol,idtoUserTask,session);	
			}else
				kba.setValue(/*property,*/idoParent,idProp, kba.buildValue(ido,/*idtoFilter*/kba.getClass(idRange)),null/*, new session()*/,/*operation*/userRol,idtoUserTask,session);
//			
			//System.out.println("Antes de relacion:"+property);
//			kba.setValue(/*property,*/idoParent,idProp, kba.getValueOfString(String.valueOf(ido),/*idtoFilter*/kba.getClass(idto)),null/*, new session()*/,/*operation*/userRol,idtoUserTask,session);
			//Property propertyNew=kba.getProperty(idoParent/*m_id*/, idProp, userRol, idtoUserTask, session);
			//System.out.println("Despues de relacion:"+propertyNew);
			title="Creación y selección de";
			objectTitle=kba.getLabelClass(idRange,idtoUserTask);
			/*message="Formulario de inserción y relación creado";*/
		}else if(command instanceof NewCommandPath){
			NewCommandPath newCommandPath=(NewCommandPath)command;
			idoParent=newCommandPath.getIdo();
			idtoParent=newCommandPath.getIdto();
			idRange=newCommandPath.getValue();
			idtoUserTask=newCommandPath.getIdtoUserTask();
			userRol=newCommandPath.getUserRol();
			operation=access.NEW;

			if(kba.getLevelObject(idRange)==Constants.LEVEL_MODEL)//Si se trata de una clase, creamos un filtro a partir del cual crearemos el prototipo por si las reglas dicen algo sobre el filtro que afecte al prototipo
				idRange=kba.createPrototype(idRange,Constants.LEVEL_FILTER, /*new session(),*/ userRol, idtoUserTask,session);
			
			ido=kba.createPrototype(idRange,Constants.LEVEL_PROTOTYPE, /*new session(),*/ userRol, idtoUserTask,session);
			idto=kba.getClass(ido);
				
			Integer idoUserTask=kba.getIdoUserTask(idtoUserTask);
			if(idoUserTask==null)//Si es null podría ser una accion
				idoUserTask=kba.getIdoUserTaskAction(idtoUserTask);
			if(idoUserTask==null)//Si es null podría ser una exportacion
				idoUserTask=kba.getIdoUserTaskExport(idtoUserTask);
			
			if(Auxiliar.equals(dialog.getParentDialog(),dialog.getMainDialog()) && idoUserTask!=null && endStep){//Si es null significa que no es ni una usertask ni una accion
				Integer areaFuncional=kba.getIdoValue(kba.getChild(idoUserTask, idtoUserTask, Constants.IdPROP_MYFUNCTIONALAREA, userRol, idtoUserTask, session));
				directReports=kba.getIdtoUserTasksDirectReport(kba.getClass(ido),areaFuncional);
				
				Iterator<Integer> itrIdos=kba.getIdoUserTasksAction(kba.getClass(ido),areaFuncional);
				idtoNameActions = new HashMap<Integer, String>();
				idtoExports=new HashMap<Integer, String>();
				while(itrIdos.hasNext()){
					int idoAction=itrIdos.next();
					int idtoAction=kba.getIdtoUserTaskAction(idoAction);
					
					ObjectProperty propertyTarget=kba.getChild(idoAction,idtoAction,Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, kba.getDefaultSession());
					int idRangeTarget=kba.getIdRange(propertyTarget);
					int idtoRangeTarget=kba.getClass(idRangeTarget);
					
					if(kba.hasShowAction(idoAction, idtoAction, null, idtoRangeTarget, idtoUserTask, userRol)){
						String value = kba.getLabelUserTask(idtoAction);
						idtoNameActions.put(idtoAction, value);
					}//else System.err.println("WARNING: La acción "+kba.getLabelUserTask(idtoAction)+" ha sido excluida en "+kba.getLabelClass(kba.getClass(ido),idtoUserTask)+" por falta de permisos");
				}
				
				itrIdos=kba.getIdoUserTasksExport(kba.getClass(ido),areaFuncional);
				while(itrIdos.hasNext()){
					int idoExport=itrIdos.next();
					int idtoExport=kba.getIdtoUserTaskExport(idoExport);
					
					String value = kba.getLabelUserTask(idtoExport);
					idtoExports.put(idtoExport, value);
				}
			}
			
			title="Creación de";
			objectTitle=kba.getLabelClass(idRange,idtoUserTask);
		}else if(command instanceof ViewCommandPath){
			ViewCommandPath viewCommandPath=(ViewCommandPath)command;
			idoParent=viewCommandPath.getIdo();
			ido=viewCommandPath.getValue();
			idto=viewCommandPath.getValueCls();
			idtoUserTask=viewCommandPath.getIdtoUserTask();
			userRol=viewCommandPath.getUserRol();
			operation=access.VIEW;
			//if(!stepListener.isMultiStep())//Si no se trata de una accion de varios pasos no hay que confirmar nada
			if(idtoUserTask==null/*Ocurre si estamos en una QuestionTask*/ || !kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION))
				buttonsType=botoneraAccion.VIEW_TYPE;
			
			//Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
			if(!Constants.isIDTemporal(ido))
				kba.loadIndividual(ido, idto, userRol, idtoUserTask, session);
			DataProperty propRDN=kba.getRDN(ido, idto, userRol, idtoUserTask, session);
			//title="Consulta de "+kba.getLabelClass(propRDN.getIdto(),idtoUserTask);
			//objectTitle=(String)kba.getValueData(propRDN);
			title="Consulta de ";
			objectTitle=kba.getLabelClass(propRDN.getIdto(),idtoUserTask);
		}else if(command instanceof SetCommandPath){
			SetCommandPath setCommandPath=(SetCommandPath)command;
			idoParent=setCommandPath.getIdo();
			ido=setCommandPath.getValue();
			idto=setCommandPath.getValueCls();
			idtoUserTask=setCommandPath.getIdtoUserTask();
			userRol=setCommandPath.getUserRol();
			operation=access.SET;
			//System.out.println("Antes set:"+kba.getSizeMotor());
			
			//Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
			if(!Constants.isIDTemporal(ido))
				kba.loadIndividual(ido, idto, userRol, idtoUserTask, session);
			
			Integer idoUserTask=null;
			if(idtoUserTask!=null){
				kba.getIdoUserTask(idtoUserTask);
				if(idoUserTask==null)//Si es null podría ser una accion
					idoUserTask=kba.getIdoUserTaskAction(idtoUserTask);
				if(idoUserTask==null)//Si es null podría ser una exportacion
					idoUserTask=kba.getIdoUserTaskExport(idtoUserTask);
			}
			
			if(Auxiliar.equals(dialog.getParentDialog(),dialog.getMainDialog()) && idoUserTask!=null && endStep){//Si es null significa que no es ni una usertask ni una accion
				Integer areaFuncional=kba.getIdoValue(kba.getChild(idoUserTask, idtoUserTask, Constants.IdPROP_MYFUNCTIONALAREA, userRol, idtoUserTask, session));
				directReports=kba.getIdtoUserTasksDirectReport(kba.getClass(ido),areaFuncional);
				
				Iterator<Integer> itrIdos=kba.getIdoUserTasksAction(kba.getClass(ido),areaFuncional);
				idtoNameActions = new HashMap<Integer, String>();
				idtoExports=new HashMap<Integer, String>();
				while(itrIdos.hasNext()){
					int idoAction=itrIdos.next();
					int idtoAction=kba.getIdtoUserTaskAction(idoAction);
					
					ObjectProperty propertyTarget=kba.getChild(idoAction,idtoAction,Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, kba.getDefaultSession());
					int idRangeTarget=kba.getIdRange(propertyTarget);
					int idtoRangeTarget=kba.getClass(idRangeTarget);
					
					if(kba.hasShowAction(idoAction, idtoAction, null, idtoRangeTarget, idtoUserTask, userRol)){
						String value = kba.getLabelUserTask(idtoAction);
						idtoNameActions.put(idtoAction, value);
					}//else System.err.println("WARNING: La acción "+kba.getLabelUserTask(idtoAction)+" ha sido excluida en "+kba.getLabelClass(kba.getClass(ido),idtoUserTask)+" por falta de permisos");
				}
				
				itrIdos=kba.getIdoUserTasksExport(kba.getClass(ido),areaFuncional);
				while(itrIdos.hasNext()){
					int idoExport=itrIdos.next();
					int idtoExport=kba.getIdtoUserTaskExport(idoExport);
					
					String value = kba.getLabelUserTask(idtoExport);
					idtoExports.put(idtoExport, value);
				}
			}
			
			DataProperty propRDN=kba.getRDN(ido, idto, userRol, idtoUserTask, session);
			//kba.gerServer().lockObject(ido, kba.gerServer().getUser());
			//title="Edición de "+kba.getLabelClass(propRDN.getIdto(),idtoUserTask);
			//objectTitle=(String)kba.getValueData(propRDN);
			title="Edición de ";
			objectTitle=kba.getLabelClass(propRDN.getIdto(),idtoUserTask);
		}
//		else if(command instanceof RelCommandPath){
//			RelCommandPath relCommandPath=(RelCommandPath)command;
//			idtoUserTask=relCommandPath.getIdtoUserTask();
//			userRol=relCommandPath.getUserRol();
//			int idObject=relCommandPath.getIdo();
//			int idProp=relCommandPath.getIdProp();
//			int value=relCommandPath.getValue();
//			int valueCls=relCommandPath.getValueCls();
//			ido=value;
//			operation=access.SET;
//			DataProperty propRDN=kba.getRDN(ido, userRol, idtoUserTask, session);
//			idto=propRDN.getIdto();
//			kba.setValue(/*property,*/idObject,idProp, kba.getValueOfString(String.valueOf(value),/*idtoFilter*/valueCls),null/*, new session()*/,/*operation*/userRol,idtoUserTask,session);
//			title="Edición de "+kba.getLabelClass(idto,idtoUserTask);
//			objectTitle=kba.getValueData(propRDN);
//		}


		/*			if(m_currentCtr!=null)
				session.addIchangeProperty(m_currentCtr);
		 */			

		//TODO Hay que decidir si son necesarios los accesos de property en la que estamos ya que se lo estoy pasando a null, ahora mismo no se tienen en cuenta
		//HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(ido, userRol, null, idtoUserTask);
		AccessAdapter accessAdapter=null;//new AccessAdapter(accessUserTasks,null);

		IdObjectForm idObjectOperation=new IdObjectForm();
		idObjectOperation.setIdo(ido);//Luego no es usado, de momento, en actionPerformed. Lo dejo por si acaso
		idObjectOperation.setIdtoUserTask(idtoUserTask);
		String idString=idObjectOperation.getIdString();
		
		botoneraAccion botonera=new botoneraAccion(
				idString,null,null,null,idtoNameActions,null,null,idtoExports,null,false,
				buttonsType,
				botoneraExtInicio, botoneraExtFin,
				tableNavigation,
				null,
				accessAdapter,
				(operation==access.VIEW),
				endStep,
				Singleton.getInstance().getGraphics(), kba.getServer(),null,kba.canSetUpColumnProperty());

		transitionControl tCtr= new transitionControl(
				session,
				userRol,
				idoParent,
				/*tableIndex,*/ido,
				idto,
				/*command,*/idtoUserTask,
				operation,
				m_preferredSize,
				/*null*/botonera.getComponent(), kba, dialog, tableNavigation, directReports, true, true, alias);

		botonera.addListener(tCtr);
		tCtr.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));
		
		/*int x=100,y=100;
			if(m_parentDialog!=null){
				x=(int)m_form.getLocationOnScreen().getX()+40;
				y=(int)m_form.getLocationOnScreen().getY()+40;
			}
		 	dlg.setLocation(x,y);*/
//		final transitionControl transitionCtrThis=tCtr;
//		/*final ActionIterator itrStepThis=itrStep;*/
//		//final String titleThis=Utils.normalizeWindowTitle(title,objectTitle);
//		final DialogModal dialogThis=dialog;
//		/*final String mensajeThis=message;*/
//		SwingWorker worker=new SwingWorker(){
//			public Object construct(){
//				/*final Runnable doFinished = new Runnable() {
//	    				public void run() { Singleton.getInstance().getStatusBar().setAccion(mensajeThis); }
//	    		    };
//	                SwingUtilities.invokeLater(doFinished);*/
//				showDialog(dialogThis,transitionCtrThis.getComponent()/*,titleThis*/);
//				System.out.println("FIN SHOW");
//				return null;
//			}
//
//			public void finished(){
//				StatusBar statusBar=Singleton.getInstance().getStatusBar();
//				if(statusBar.getNivelLocalizacion()==2)
//				//if(statusBar.getNivelLocalizacion()>0)
//					statusBar.upNivelLocalizacion();
//
//				//exeStep(itrStepThis);
//			}
//		};
//		worker.start();

		return tCtr;
	}

	private IFormData showCommonObject(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException{

		Session session = command.getSession();		
		int idoNew=-1;
		int operation=-1;
		int buttonsType=-1;
		Integer idtoUserTask=null;
		Integer userRol=null;
		String title=null;
		String objectTitle=null;
		int idoParent=-1;
		boolean endStep=stepListener.isLastStep();
		HashMap<Integer,Integer> values = new HashMap<Integer,Integer>();

		idtoUserTask=command.getIdtoUserTask();
		userRol=command.getUserRol();

		if(command instanceof ViewCommonCommandPath){
			ViewCommonCommandPath viewCommonCommandPath=(ViewCommonCommandPath)command;
			idoParent=viewCommonCommandPath.getIdo();
			values.putAll(viewCommonCommandPath.getValues());	
			operation=access.VIEW;
			buttonsType=botoneraAccion.VIEW_TYPE;

			HashMap<Integer,HashSet<Integer>> idosToLoad=new HashMap<Integer, HashSet<Integer>>();
			Iterator<Integer> itrIdos=values.keySet().iterator();
			while(itrIdos.hasNext()){
				int idoToLoad=itrIdos.next();
				int idtoToLoad=values.get(idoToLoad);
				if(!idosToLoad.containsKey(idtoToLoad))
					idosToLoad.put(idtoToLoad, new HashSet<Integer>());
				idosToLoad.get(idtoToLoad).add(idoToLoad);
			}
			
			if(!idosToLoad.isEmpty()){
				// Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
				kba.loadIndividual(idosToLoad, userRol, idtoUserTask, session);
			}
			
			Iterator<Integer> itrValues=values.keySet().iterator();
			int idoValues=itrValues.next();
			int idtoValues=values.get(idoValues);
			DataProperty propRDN=kba.getRDN(idoValues, idtoValues, userRol, idtoUserTask, session);
			int idto=propRDN.getIdto();
			String rdnValues=kba.getValueData(propRDN)+(itrValues.hasNext()?", ":"");;
			while(itrValues.hasNext()){
				idoValues=itrValues.next();
				idtoValues=values.get(idoValues);
				DataProperty propValueRDN=kba.getRDN(idoValues, idtoValues, userRol, idtoUserTask, session);
				rdnValues+=kba.getValueData(propValueRDN)+(itrValues.hasNext()?", ":"");
			}
			title="Consulta de "+kba.getLabelClass(idto,idtoUserTask);
			objectTitle=rdnValues;
		}
		else if(command instanceof SetCommonCommandPath){
			SetCommonCommandPath setCommonCommandPath=(SetCommonCommandPath)command;
			idoParent=setCommonCommandPath.getIdo();
			values.putAll(setCommonCommandPath.getValues());
			operation=access.SET;
			buttonsType=botoneraAccion.RECORD_TYPE;

			HashMap<Integer,HashSet<Integer>> idosToLoad=new HashMap<Integer, HashSet<Integer>>();
			Iterator<Integer> itrIdos=values.keySet().iterator();
			while(itrIdos.hasNext()){
				int idoToLoad=itrIdos.next();
				int idtoToLoad=values.get(idoToLoad);
				if(!idosToLoad.containsKey(idtoToLoad))
					idosToLoad.put(idtoToLoad, new HashSet<Integer>());
				idosToLoad.get(idtoToLoad).add(idoToLoad);
			}
			if(!idosToLoad.isEmpty()){
				// Lo cargamos aqui directamente. LoadIndividual cargara varios niveles
				kba.loadIndividual(idosToLoad, userRol, idtoUserTask, session);
			}
			
			Iterator<Integer> itrValues=values.keySet().iterator();
			int idoValues=itrValues.next();
			int idtoValues=values.get(idoValues);
			DataProperty propRDN=kba.getRDN(idoValues, idtoValues, userRol, idtoUserTask, session);
			int idto=propRDN.getIdto();
			String rdnValues=kba.getValueData(propRDN)+(itrValues.hasNext()?", ":"");;
			while(itrValues.hasNext()){
				idoValues=itrValues.next();
				idtoValues=values.get(idoValues);
				DataProperty propValueRDN=kba.getRDN(idoValues, idtoValues, userRol, idtoUserTask, session);
				rdnValues+=kba.getValueData(propValueRDN)+(itrValues.hasNext()?", ":"");
			}
			title="Edición de "+kba.getLabelClass(idto,idtoUserTask);
			objectTitle=rdnValues;
		}

		//TODO idoNew no se usa despues. Parece que accessAdapter deberia ser null directamente. Comprobar
		//HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(idoNew, userRol, null, idtoUserTask);
		AccessAdapter accessAdapter=null;//new AccessAdapter(accessUserTasks,null);

		boolean modoCreacion=true;

		IdObjectForm idObjectOperation=new IdObjectForm();
		idObjectOperation.setIdo(idoNew);//Luego no es usado, de momento, en actionPerformed. Lo dejo por si acaso
		String idString=idObjectOperation.getIdString();

		botoneraAccion botonera=new botoneraAccion(
				idString,null,null, null, null,null,null,null,null,false,
				buttonsType,
				botoneraExtInicio, botoneraExtFin,
				null,
				null,
				accessAdapter,
				(operation==access.VIEW),
				endStep,
				Singleton.getInstance().getGraphics(), kba.getServer(),null,kba.canSetUpColumnProperty());

		TransitionControlCommon tCtr= new TransitionControlCommon(
				session,
				userRol,
				idoParent,
				/*tableIndex,*/values,
				/*command,*/idtoUserTask,
				operation,
				m_preferredSize,
				/*null*/botonera.getComponent(), kba, dialog);

		botonera.addListener(tCtr);
		tCtr.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));
		
//		final TransitionControlCommon transitionCtrThis=tCtr;
//		//final String titleThis=Utils.normalizeWindowTitle(title,objectTitle);
//		final DialogModal dialogThis=dialog;
//		SwingWorker worker=new SwingWorker(){
//			public Object construct(){
//				showDialog(dialogThis,transitionCtrThis.getComponent()/*,titleThis*/);
//				System.out.println("FIN SHOW");
//				return null;
//			}
//			public void finished(){
//				StatusBar statusBar=Singleton.getInstance().getStatusBar();
//				if(statusBar.getNivelLocalizacion()==2)
//				//if(statusBar.getNivelLocalizacion()>0)
//					statusBar.upNivelLocalizacion();			
//			}
//		};
//		worker.start();
		return tCtr;

	}
	
	private IFormData showDataTransfer(commandPath command,KnowledgeBaseAdapter kba,WindowComponent dialog,IStepListener stepListener,JPanel botoneraExtInicio,JPanel botoneraExtFin) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException{
		/*int ido=command.ido;
			int idtoUserTask=command.idtoUserTask;
			int operation=command.operation;
			int idProp=command.idProp;*/

		Session session = command.getSession();
		//currentSession=session;
		//SessionController.getInstance().setActual(session);

		int operation=-1;
		Integer idtoUserTask=null;
		Integer userRol=null;
		String title=null;
		String objectTitle=null;
		/*String message=null;*/
		int idto=-1;
		int ido=-1;
		boolean endStep=stepListener.isLastStep();
		int buttonsType=botoneraAccion.RECORD_TYPE;

		if(command instanceof DataTransferCommandPath){
			DataTransferCommandPath dataTransferCommandPath=(DataTransferCommandPath)command;
			idto=dataTransferCommandPath.getIdto();
			idtoUserTask=dataTransferCommandPath.getIdtoUserTask();
			userRol=dataTransferCommandPath.getUserRol();
			operation=access.SET;
			int idoProgram=dataTransferCommandPath.getIdoProgram();
			int idtoProgram=dataTransferCommandPath.getIdtoProgram();
			
			ido=kba.createPrototype(idto,Constants.LEVEL_PROTOTYPE, /*new session(),*/ userRol, idtoUserTask,session);
			Value oldValue=kba.getChild(ido, idto, Constants.IdPROP_TARGETPROGRAM, userRol, idtoUserTask, session).getUniqueValue();
			kba.setValue(ido, Constants.IdPROP_TARGETPROGRAM, kba.buildValue(idoProgram, idtoProgram), oldValue, userRol, idtoUserTask, session);
			/*title="Parámetros de ";
			objectTitle=kba.getLabelClass(idto,idtoUserTask);*/
			title=kba.getLabelClass(idto,idtoUserTask);
			/*message="Formulario de inserción y relación creado";*/
		}
		//TODO Hay que decidir si son necesarios los accesos de property en la que estamos ya que se lo estoy pasando a null, ahora mismo no se tienen en cuenta
		//HashMap<Integer,ArrayList<UserAccess>> accessUserTasks=kba.getAllAccessIndividual(ido, userRol, null, idtoUserTask);
		AccessAdapter accessAdapter=null;//new AccessAdapter(accessUserTasks,null);

		IdObjectForm idObjectOperation=new IdObjectForm();
		idObjectOperation.setIdo(ido);//Luego no es usado, de momento, en actionPerformed. Lo dejo por si acaso
		idObjectOperation.setIdtoUserTask(idtoUserTask);
		String idString=idObjectOperation.getIdString();

		botoneraAccion botonera=new botoneraAccion(
				idString,null,null,null,null,null,null,null,null,false,
				buttonsType,
				botoneraExtInicio, botoneraExtFin,
				null,
				null,
				accessAdapter,
				(operation==access.VIEW),
				endStep,
				Singleton.getInstance().getGraphics(), kba.getServer(),null,kba.canSetUpColumnProperty());

		DataTransferControl dTCtr= new DataTransferControl(
				session,
				userRol,
				ido,
				idto,
				idtoUserTask,
				operation,
				m_preferredSize,
				botonera.getComponent(), kba, dialog);

		botonera.addListener(dTCtr);
		dTCtr.getComponent().setName(Utils.normalizeWindowTitle(title,objectTitle));

		return dTCtr;
	}

	private void disposeDialog(WindowComponent dialog){
		if( dialog!=null ){
			//System.out.println("DLG NOT NULL");
			dialog.getComponent().setVisible(false);
			dialog.dispose();

		}
	}
	protected void showDialog( final WindowComponent dlg,final JComponent panel/*,String title*/ ){
		 final Runnable doFinished = new Runnable() {
	           public void run() {
	        	   Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"open","window="+dlg.hashCode());
	        	   
	        	/*   if(dlg==null){
	       			dlg= new DialogModal(m_currentDialog);
	       			//dlg.setIconImage(m_currentDialog.getIconImages().get(0));
	       			dlg.setTitle(panel.getName());
	       			//dlg.setModal(true);
	       			dlg.setMainDialog(m_currentDialog.getMainDialog());
		       		}
		       		else{*/
	        	   	if(!isMultiWindow()){
		       			dlg.getComponent().setVisible(false);// Ya que si estaba visible no se parara la ejecucion en el setVisible(true) del final, y a nosotros nos interesa que se pare
	        	   	}
	        	   	dlg.setTitle(panel.getName());
		       		/*}*/
		       		dlg.setContentPane(panel);//, BorderLayout.CENTER);
		       		dlg.getComponent().pack();		
	
		       		//Insets insetsDialog=dlg.getInsets();
		       		//int bordersWidth=insetsDialog.left+insetsDialog.right;
		       		//int bordersHeight=insetsDialog.top+insetsDialog.bottom;
		       		//Dimension dim= new Dimension((int)panel.getPreferredSize().width+bordersWidth,(int)(panel.getPreferredSize().height+bordersHeight));//barra de herramientas y bordes
		       		//dlg.setMinimumSize(dim.getSize());
		       		//dlg.setPreferredSize(dim.getSize());
	
		       		
		       		Iterator<ArrayList<WindowComponent>> itrListDialogs=listDialogs.iterator();
		    		boolean found=false;
		    		while(!found && itrListDialogs.hasNext()){
		    			ArrayList<WindowComponent> list=itrListDialogs.next();
		    			if(list.contains(dlg.getParentDialog())){
		    				if(!list.contains(dlg)){
		    					list.add(dlg);
		    				}
		    				found=true;
		    			}
		    		}
		    		if(!found){
		    			ArrayList<WindowComponent> list=new ArrayList<WindowComponent>();
		    			list.add(dlg);
		    			listDialogs.add(list);
		    			
		    		}
		    		//dlg.setLocationRelativeTo(dlg.getParentDialog().getComponent());
		    		dlg.setLocationRelativeTo(null);//Centro de la pantalla
		    		/* TODO ser capaz mostrar ventana agrupacion talla color arriba para no se tape con teclado 
		    		 if(panel.getName().contains("rupaci")){
		    			dlg.getComponent().setLocation(300,50);
		    		}else{
		    			dlg.setLocationRelativeTo(null);//Centro de la pantalla
		    		}*/
		    		
		    		
		    		if(dlg.getComponent().isVisible()){
		    			//Si ya estaba visible es una ventana de varios pasos. Le damos el foco directamente nosotros ya que no al no hacerse visible de primeras no se lo dará automaticamente 
		    			panel.getFocusCycleRootAncestor().transferFocus();
		    		}else{
		    			dlg.getComponent().setVisible(true);
		    		}
		       		
		       		
		       		SwingUtilities.invokeLater(new Runnable(){
		       			
						public void run() {
							//System.err.println("1:"+KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
							
							//Solo lo hacemos en el caso de que la ventana activa sea null. Esto ocurre con los navegadores(con java 7) cuando el foco debería estar en la nueva ventana mostrada
							//Si es nulo le damos el foco a la ventana. Si no es nulo, dejamos que el foco se quede donde está
							if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()==null){
								
								SwingUtilities.invokeLater(new Runnable() {
									int iteration=0;
									
									public void run() {
										Window window=dlg.getComponent();
										if(window.isDisplayable()){
											//System.err.println("Window para dar el foco:"+window);
											window.requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
											window.requestFocusInWindow();
										}else if(iteration<5){//Evitamos que se quede en bucle infinito
											iteration++;
											//System.err.println("Window no valida para dar el foco:"+window);
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											SwingUtilities.invokeLater(this);
										}
										
									}
								});
								
							}
						}
						
					});
	           }
	        };

	    SwingUtilities.invokeLater(doFinished);
	}

	public boolean closeForm(final WindowComponent dlg,KnowledgeBaseAdapter kba,boolean accepted) throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException{
		//System.err.println("CloseEvent de ActionManager");
		Iterator<ArrayList<WindowComponent>> itrListDialogs=listDialogs.iterator();
		boolean found=false;
		while(!found && itrListDialogs.hasNext()){
			ArrayList<WindowComponent> list=itrListDialogs.next();
			if(list.remove(dlg)){
			
				int size=list.size();
				
				Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"close","accepted="+accepted+" window="+dlg.hashCode());
				
				dialogListenerMap.get(dlg).endSteps();
				dialogListenerMap.remove(dlg);
				disposeDialog(dlg);
				/*if(currentSession!=null)
					listSessions.remove(size-1);*/
				if(size==0){
					listDialogs.remove(list);//Quitamos el arrayList ya que esta vacio
					//Pedimos el foco para el applet ya que si no no funcionan las teclas shortcut hasta que no hacemos click sobre algun componente del applet
					SwingUtilities.invokeLater(new Runnable(){
	
						public void run() {
							//System.err.println("1:"+KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
							
							//Solo lo hacemos en el caso de que la ventana activa sea null. Esto ocurre con los navegadores cuando el foco debería volver al applet, aunque a veces tambien con JFrame
							//Si es nulo le damos el foco al applet si no existe otra ventana mostrandose. Si no es nulo, dejamos que el foco se quede donde está
							if(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow()==null){
//								boolean otherTopWindow=false;
//								for(ArrayList<WindowComponent> list:listDialogs){
//									for(WindowComponent dialog:list){
//										if(dialog!=m_rootDialog && dialog.getComponent().isShowing()){
//											System.err.println("visible:"+dialog.getComponent().isVisible()+" valid:"+dialog.getComponent().isValid()+" ancestor:"+dialog.getComponent().isAncestorOf(m_rootDialog.getComponent())+" displayable:"+dialog.getComponent().isDisplayable()+" isFocusableWindow:"+dialog.getComponent().isFocusableWindow()+" isFocusable:"+dialog.getComponent().isFocusable()+" isFocused:"+dialog.getComponent().isFocused());
//											//Si hay mas de una no pedimos foco¿?
//											otherTopWindow=true;
//											System.err.println("OtherTopWindow");
//											if(!dialog.getGlassPane().isVisible()){//Esto lo hacemos para evitar que le de el foco a una ventana que tiene otra ventana modal encima
//												dialog.getComponent().requestFocus();
//												dialog.getComponent().requestFocusInWindow();
//											}
//										}
//									}
//								}
//								if(!otherTopWindow){
//									Component component=m_rootDialog.getComponent().getMostRecentFocusOwner();
//									Singleton.getInstance().getApplet().requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
//									Singleton.getInstance().getApplet().requestFocusInWindow();
//									if(component!=null)
//										component.requestFocusInWindow();
//								}
								
								SwingUtilities.invokeLater(new Runnable() {
									int iteration=0;
									
									public void run() {
										Window window=windowsActiveStack.peek();
										if(dlg.getComponent()!=window && window.isDisplayable()){
											//System.err.println("Window para dar el foco:"+window);
											if(windowsActiveStack.size()>1){
												//System.err.println("Da el foco a window");
												window.requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
												window.requestFocusInWindow();
											}else{
												//System.err.println("Da el foco al applet");
												Component component=window.getMostRecentFocusOwner();
												Singleton.getInstance().getApplet().requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
												Singleton.getInstance().getApplet().requestFocusInWindow();
												if(component!=null)
													component.requestFocusInWindow();
											}
										}else if(iteration<5){//Evitamos que se quede en bucle infinito
											iteration++;
											//System.err.println("Window no valida para dar el foco:"+window);
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												e.printStackTrace();
											}
											SwingUtilities.invokeLater(this);
										}
										
									}
								});
								
							}
						}
						
					});
				}
				found=true;
			}
		}
		return found;
		
		
//		
//		int size=listDialog.size();
//		if(size>0){
//			WindowComponent removed=listDialog.remove(size-1);
//			
//			Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI,"close","accepted="+accepted+" window="+removed.hashCode());
//			
//			dialogListenerMap.get(removed).endSteps();
//			dialogListenerMap.remove(removed);
//			disposeDialog();
//			/*if(currentSession!=null)
//				listSessions.remove(size-1);*/
//			if(size>1){
//				setCurrentDialog(listDialog.get(size-2));	
//				//Aqui no pedimos el foco como en el else ya que de eso se encarga GFocusTraversalPolicy 
//			}else{
//				setCurrentDialog(m_rootDialog);
//				//Pedimos el foco para el applet ya que si no no funcionan las teclas shortcut hasta que no hacemos click sobre algun componente del applet
//				SwingUtilities.invokeLater(new Runnable(){
//
//					public void run() {
//						Component component=m_rootDialog.getComponent().getMostRecentFocusOwner();
//						Singleton.getInstance().getApplet().requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
//						Singleton.getInstance().getApplet().requestFocusInWindow();
//						if(component!=null)
//							component.requestFocusInWindow();
//					}
//					
//				});
//			}
//			return true;
//		}
//		return false;
	}

	/*private JPanel buildBotoneraAssistant(){
			JPanel panelButtons=new JPanel();
			//panelButtons.setLayout(new BorderLayout());
			JButton previousButton=botoneraAccion.subBuildBoton(panelButtons, "Anterior", null, "previous", null, this, 20, 20, false);
			JButton nextButton=botoneraAccion.subBuildBoton(panelButtons, "Siguiente", null, "next", null, this, 20, 20, false);
			panelButtons.add(previousButton, BorderLayout.WEST);
			panelButtons.add(nextButton, BorderLayout.EAST);

			panelButtons.setBackground(m_colorFondo);

			return panelButtons;
		   }
	 */

	public static void main(String args[]){
		HashMap<Integer, HashMap<Integer, LinkedList<String>>> listIndividuos = new HashMap<Integer, HashMap<Integer,LinkedList<String>>>();
		HashMap<Integer, LinkedList<String>> prop = new HashMap<Integer, LinkedList<String>>();
		LinkedList<String> lk1 = new LinkedList<String>();
		lk1.add("value1");
		prop.put(1, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value2");
		prop.put(2, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value3");
		prop.put(3, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value4");
		prop.put(4, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value5");
		prop.put(5, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value6");
		prop.put(6, lk1);

		listIndividuos.put(1, prop);
		HashMap<Integer, LinkedList<String>> prop2 = new HashMap<Integer, LinkedList<String>>();
		lk1 = new LinkedList<String>();
		lk1.add("value12");
		prop2.put(1, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value22");
		prop2.put(2, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value3");
		prop2.put(3, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value4");
		prop2.put(4, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value52");
		lk1.add("value5");
		prop2.put(5, lk1);
		lk1 = new LinkedList<String>();
		lk1.add("value62");
		prop2.put(6, lk1);
		listIndividuos.put(2, prop2);

		boolean go = true;

		// Comprobar que tienen las mismas properties
		Iterator<Integer> it = listIndividuos.keySet().iterator();
		int id = it.next();
		Iterator<Integer> itp = listIndividuos.get(id).keySet().iterator();
		while(itp.hasNext()){
			int idProp = itp.next();
			it = listIndividuos.keySet().iterator();
			while(it.hasNext()){
				id=it.next();
				if(listIndividuos.get(id).get(idProp)==null){
					go=false;
					JOptionPane.showMessageDialog(null,"Todos los formularios no tiene las mismas properties. IdProp: "+idProp);
					break;
				}				
			}
		}

		if(go){
			// Comprobar que tienen los mismos value
			HashMap<Integer, LinkedList<String>> notEquals = new HashMap<Integer, LinkedList<String>>();
			it = listIndividuos.keySet().iterator();
			int idInit = it.next();
			itp = listIndividuos.get(idInit).keySet().iterator();
			while(itp.hasNext()){
				int idProp = itp.next();
				LinkedList<String> val1 = listIndividuos.get(idInit).get(idProp);
				it = listIndividuos.keySet().iterator();
				it.next();
				boolean equals = true;
				while(it.hasNext()){
					id=it.next();
					LinkedList<String> val2 = listIndividuos.get(id).get(idProp);
					if(val1!=null && val2!=null){
						int i;
						for(i=0;i<val1.size();i++){
							if(val2.contains(val1.get(i))){
								equals = true;
								break;
							}
						}
						if(i==val1.size()){
							equals = false;
							notEquals .put(idProp, val2);
						}
					}else{
						equals = false;
						notEquals.put(idProp, val2);
					}
				}				
				if(equals){
					System.out.println("equals id: "+id+" prop: "+idProp+" value: "+val1);
				}
			}
			Iterator<Integer> itne = notEquals.keySet().iterator();
			while(itne.hasNext()){
				int idP = itne.next();
				LinkedList<String> valuesOld = notEquals.get(idP);
				for(int i=0;i<valuesOld.size();i++){
					System.out.println("not equals id: "+id+" prop: "+idP+" value: "+valuesOld.get(i));	
				}
			}
		}
	}
/*
	public void preEditionEvent(int idObject, Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		kba.setLockObject(idObject, true, sess);
	}
	
	public void postEditionEvent(int idObject, Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		kba.setLockObject(idObject, false, sess);
	}
*/
	@Override
	public void sendMessage(String message) {
		Singleton.getInstance().getMessagesControl().showMessage(message,KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
	}
	
	@Override
	public Boolean sendQuestion(String message, boolean initialSelectionIsYes) {
		Object[] options={"Sí","No"};
		int res= Singleton.getInstance().getMessagesControl().showOptionMessage(
				message,
				Utils.normalizeLabel("Pregunta"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,null, options, (initialSelectionIsYes?options[0]:options[1]), KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
		return res == JOptionPane.YES_OPTION;
	}

	public boolean isMultiWindow() {
		return multiWindow;
	}

	public void setMultiWindow(boolean multiWindow) {
		this.multiWindow = multiWindow;
	}

}
