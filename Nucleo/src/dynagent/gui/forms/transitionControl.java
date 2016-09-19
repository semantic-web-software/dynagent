package dynagent.gui.forms;

import gdev.gawt.utils.GFocusTraversalPolicy;
import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;
import gdev.gen.DictionaryWord;
import gdev.gen.EditionTableException;
import gdev.gen.IDictionaryFinder;
import gdev.gen.NotValidValueException;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.communication.communicator;
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
import dynagent.common.knowledge.IChangeServerListener;
import dynagent.common.knowledge.IEmailListener;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.action;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.common.utils.jdomParser;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.Singleton;
import dynagent.gui.StatusBar;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.ExportCommandPath;
import dynagent.gui.actions.commands.FindRelCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.NewRelCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetCommonCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewCommonCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.forms.utils.Column;
import dynagent.gui.forms.utils.resultPanel;
import dynagent.gui.utils.ColumnsTableComponent;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;

public class transitionControl extends FormControl implements ActionListener, IDictionaryFinder, IHistoryDDBBListener{
	
	FormManager m_formManager;
	HashSet m_rdns = new HashSet();
	JPanel m_form;
	int m_processType = 0;
	int m_taskType = 0;
	int m_tran = 0;
	int m_actionID = 0;
	int m_operation = 0;
	String m_idPressedButton = null;
	int m_level;
	
	Integer m_idoParent;
	Integer m_idtoParent;
	//ActionManager m_actionManager;
	/* JDialog m_currentModalForm = null;*/

//	int m_ido = 0;
	int m_correlativeVirtualEstructuralIndex = 1;
	Dimension m_preferredSize;
	IdObjectForm idTableEditionToDDBB;
	Session sessionOperationDDBB;//Apunta a la session que esta siendo utilizada para almacenar un individuo directamente en base de datos. Es util para saber si un aviso del historial se refiere a este formulario
	
	boolean hasUserModified;
	boolean hasSystemModified;
	
	ITableNavigation tableNavigation;
	boolean askCancel;//Si hay datos modificados pregunta o no si esta seguro al pulsar cancelar

	ArrayList<Integer> idtoDirectReports;

	boolean onlyFirstLevelColumnsTable;//Indica si al construir las tablas solo se muestran las columnas con datos de primer nivel
	/*ArrayList<Integer> m_userRols;*/
	

	/*listAddTableChangeValue es utilizado para almacenar los avisos de changeValue que se reciben sobre tablas y luego procesarlos juntos
	 Almacena como clave el Id de la tabla, y como valor un mapa de idos e idtos*/
	public HashMap<String,HashMap<Integer,Integer>> listAddTableChangeValue;
	public HashMap<String,HashMap<Integer,GIdRow>> listDelTableChangeValue;
	
	HashMap<String,String> aliasMap;

	/** Creates a new instance of transitionControl 
	 * @param alias 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws AssignValueException 
	 * @throws ParseException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException */

	public transitionControl(Session ses,
			Integer userRol,
			Integer idoParent,
			int ido,
			int idto,
			Integer idtoUserTask,
			int operation,
			Dimension dim,
			/*JPanel botoneraExterna,*/JPanel botonera,KnowledgeBaseAdapter kba,
			WindowComponent dialog,ITableNavigation tableNavigation,ArrayList<Integer> idtoDirectReports,boolean popup,boolean scroll, HashMap<String,String> alias) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		super(kba,dialog,idtoUserTask,userRol,ses,ido,idto);
	
		idTableEditionToDDBB=null;
		
		hasUserModified=false;
		askCancel=true;
		
		this.tableNavigation=tableNavigation;
		
		//m_kba=Singleton.getInstance().getKnowledgeBase();
		//m_actionManager=Singleton.getInstance().getActionManager();
		/*m_userRol = m_kba.getUserRolRestrictive(userRols);
        m_userRols=userRols;*/
	
		m_preferredSize = dim;

		m_session.addIMessageListener(m_actionManager);
		/*        commandPath cpath = (commandPath) commandPathList.get(0);*/
		
//		m_ido = ido;

		m_operation=operation;
		m_idoParent=idoParent;
		m_idtoParent=m_idoParent!=null?m_kba.getClass(m_idoParent):null;
		m_level=m_kba.getLevelObject(ido);
		
		onlyFirstLevelColumnsTable=false;
		
		aliasMap=alias;
		/*if( botoneraExterna!=null ){
            Component[] childs = botoneraExterna.getComponents();
            for (int i = 0; i < childs.length; i++) {
                Component child = childs[i];
                if (child instanceof Button)
                    ((Button) child).addActionListener(this);
                if (child instanceof AbstractButton)
                    ((AbstractButton) child).addActionListener(this);
            }
        }*/
		//m_session.setRunRules(false);
//		Date now=new Date(System.currentTimeMillis());
//		System.err.println("\n        ************Antes build *******    "+now);
		build(/*botoneraExterna*/botonera,operation,idtoUserTask,idoParent,m_ido,m_idto,false, popup, scroll, dialog, alias);
		
//		now=new Date(System.currentTimeMillis());
//		System.err.println("\n        ************Despues build *******    "+now);
		
		//m_session.setRunRules(true);
		//m_kba.setLockObject(m_ido, true, m_session);
		
		Object changes=new IChangePropertyListener(){

			public void initChangeValue() {
				// TODO Auto-generated method stub
				
			}
			
			public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException {
				//System.err.println("Cambia valor el sistema en ido:"+ido+" idto:"+idto+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+oldValue);
				//if(!Auxiliar.equals(ido,m_kba.getIdoUserTask(m_idtoUserTask)) && level!=Constants.LEVEL_FILTER){//Nos sirve para saber si alguna regla ha hecho modificaciones al inicio
				if(m_idtoUserTask!=null && !Auxiliar.equals(ido,m_kba.getIdoUserTask(m_idtoUserTask)) && level!=Constants.LEVEL_FILTER){//Nos sirve para saber si alguna regla ha hecho modificaciones al inicio
					hasSystemModified=true;
				}
			}

			public void endChangeValue() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		this.idtoDirectReports=idtoDirectReports;
		
		m_session.addIchangeProperty((IChangePropertyListener)changes, false);
		startEdition(m_ido,m_session);
		
//		if(m_kba.getClass(idoParent).equals(idtoUserTask) && m_level!=Constants.LEVEL_FILTER){
//			if(!m_kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION))//Evitamos que se añada porque en las acciones ya esta añadido por reglas
//				m_kba.setValue(/*property,*/idoParent,Constants.IdPROP_TARGETCLASS, m_kba.getValueOfString(String.valueOf(ido),/*idtoFilter*/m_kba.getClass(ido)),null/*, new session()*/,/*operation*/userRol,idtoUserTask,m_session);
//			//try{
//				m_kba.setUserTaskState(idoParent, Constants.IDO_INICIALIZANDO, userRol, idtoUserTask, m_session);
//				//System.err.println("Estado:"+m_kba.getProperty(idoParent, Constants.IdPROP_ESTADOREALIZACION, userRol, idtoUserTask, ses));
//			//}finally{
//				m_kba.setUserTaskState(idoParent, Constants.IDO_PENDIENTE, userRol, idtoUserTask, m_session);
//			//}
//		}
		
		if(m_kba.getIdtoUserTasks(m_idto,null,false,true,false).contains(idtoUserTask) && m_level!=Constants.LEVEL_FILTER){
			int idoUserTask=m_kba.getIdoUserTask(idtoUserTask);
			if(!m_kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION))//Evitamos que se añada porque en las acciones ya esta añadido por reglas
				if(Auxiliar.equals(idoUserTask,m_idoParent)){//Si el padre es la userTask lo enganchamos a la userTask
					m_kba.setValue(/*property,*/idoUserTask,Constants.IdPROP_TARGETCLASS, m_kba.buildValue(ido,/*idtoFilter*/m_kba.getClass(ido)),null/*, new session()*/,/*operation*/userRol,idtoUserTask,m_session);
				}
			//try{
				m_kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_INICIALIZANDO, userRol, idtoUserTask, m_session);
				//System.err.println("Estado:"+m_kba.getProperty(idoParent, Constants.IdPROP_ESTADOREALIZACION, userRol, idtoUserTask, ses));
			//}finally{
				m_kba.setState(idoUserTask, idtoUserTask, Constants.INDIVIDUAL_PENDIENTE, userRol, idtoUserTask, m_session);
			//}
		}
		
		m_session.removeIchangeProperty((IChangePropertyListener)changes);
		
//		now=new Date(System.currentTimeMillis());
//		System.err.println("\n        ************Despues estados *******    "+now);
		
		if(m_operation !=  access.VIEW)
			m_session.addIchangeProperty(this,true);//Se hace al final porque hasta que no este construido no tiene sentido que cambie valores graficos
		
		//Ordenamos las filas de las tablas basandonos en la primera columna
		Iterator<String> itr=m_formManager.getIdTables().iterator();
		while(itr.hasNext()){
			String idTable=itr.next();
			m_formManager.orderRows(idTable);
		}			
//		now=new Date(System.currentTimeMillis());
//		System.err.println("\n        ************Despues session *******    "+now);
	}


	protected void build(/*JPanel botoneraExterna*/JPanel botonera,int operation,Integer idtoUserTask,Integer idObjectParent,int idObject,int idtoObject,boolean multipleMode, boolean popup, boolean scroll, WindowComponent dialog, HashMap<String,String> alias) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		
		//Dimension dimFormulario = m_preferredSize;
		//dimFormulario.height=dimFormulario.height-botonera.getPreferredSize().height;
		//Dimension dim = UtilsDimension.ajustScrollDimensions(max, real)
		/*Integer userRol=m_kba.getUserRolRestrictive(userRols);*/
		 
		Dimension dimMax = (Dimension)m_preferredSize.clone();
		dimMax.width=dimMax.width-new JScrollPane().getVerticalScrollBar().getPreferredSize().width;
		
		JPanel panelAux=new JPanel();
		panelAux.add(botonera);
		
		int borderY = 3;
		dimMax.height=dimMax.height-panelAux.getPreferredSize().height-borderY;
		
		formFactory m_formFtry = new formFactory(m_kba,m_session,
				dimMax,
				operation,
				idtoUserTask,
				m_userRol,/*userRols,*/
				idObjectParent,
				idObject,/*m_kba.getIdObject(m_kba.getTarget(idtoUserTask,userRol)),*/
				idtoObject,
				true,
				false,
				multipleMode,this,alias,isAllowedConfigTables());
		
//		Date now=new Date(System.currentTimeMillis());
//		System.err.println("\n        ************Despues formFactory *******    "+now);
		
		//int maxid = 0; //maxID almacenará los ids de los botones que deben ser
		/*Element view = m_formFtry.getViewDoc();*/
		ArrayList listViewForm=m_formFtry.getListViewForm();
		m_form = new JPanel();
		m_form.setBorder(BorderFactory.createEmptyBorder());
		m_form.setLayout(new BorderLayout());
		
		m_formManager = new FormManager(
				
				this,
				this,
				this,
				m_actionID,
				"TRANSITION_FORM",
				listViewForm,
				(operation ==  access.NEW ),
				false,
				(operation ==  access.VIEW),
				popup,
				scroll,
				null, dimMax, m_kba, dialog, m_idtoUserTask, m_userRol, m_session);

		m_form.add(m_formManager.getComponent(), BorderLayout.CENTER);
		
		if(m_formManager.isHasScroll())
			panelAux.setBorder(BorderFactory.createMatteBorder(borderY, 0, 0, 0, Color.white));
		
		m_form.add(panelAux,BorderLayout.SOUTH);
		
		if(dialog!=null){
			dialog.setFocusTraversalPolicy(new GFocusTraversalPolicy(m_form,dialog.getComponent(),m_formManager.getViewPort(),m_formManager.getFormComponents(),m_com,this,Singleton.getInstance().getMessagesControl(),false,popup));
		}
	}

	public JPanel getComponent() {
		return m_form;
	}

//	protected void setElementRelationed(String tableIndex, selectData lista, boolean replace) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
//		if (lista == null || lista.size() == 0)
//			return;
//		Iterator itr= lista.getIterator();
//		//boolean first=true;
//		while(itr.hasNext()){
//			instance obj=(instance)itr.next();
//			/*if(first){
//    	    	setValueField(tableIndex, String.valueOf(obj.getIDO()),null, obj.getIdTo());
//    	    	first=false;
//    	    }else addValueField(tableIndex, String.valueOf(obj.getIDO()), obj.getIdTo());*/
//			setValue(tableIndex, String.valueOf(obj.getIDO()),null, obj.getIdTo());
//		}
//		if (replace)
//			m_formManager.replaceRows(tableIndex, lista);
//		else
//			m_formManager.addRows(tableIndex, lista, false);
//	}

	protected void removeElementRelationed(String tableIndex, ArrayList<GIdRow> lista, boolean forceDeleted, Integer idtoUserTask) throws NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, AssignValueException {
		if (lista == null || lista.size() == 0)
			return;
		Iterator<GIdRow> itr= lista.iterator();
		while(itr.hasNext()){
			GIdRow obj=itr.next();
			if(/*Constants.isIDTemporal(obj.getIDO()) ||*/ forceDeleted)//if(delete)
				m_kba.deleteObject(obj.getIdo(),obj.getIdto(),obj.getRdn(),m_userRol,idtoUserTask,m_session);
			else{
				setValue(tableIndex, null, obj.getIdo(), obj.getIdto(), obj.getIdto());
			}
		}

		//No lo eliminamos de la lista ya que el aviso de la operacion llega a changeValue y alli se encarga de eliminar
		//if (lista.hasData()) m_formManager.delRows(tableIndex,lista);/*m_simpleForm.delRows(tableIndex,lista);*/
	}

	public boolean cancel() throws ApplicationException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException{
		if(askCancel && ((hasSystemModified && m_operation==access.SET)/*Con esto evitamos que se haga por la asignacion de valores por defecto al crear*/ || hasUserModified)){
			Object[] options = {"Sí", "No"};
			int res;
			String aux=hasUserModified?"":" Hay datos modificados por el sistema";
			res = Singleton.getInstance().getMessagesControl().showOptionMessage("¿Está seguro que desea cancelar?"+aux,
						Utils.normalizeLabel("CONFIRMACIÓN DE CANCELACIÓN"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[1],dialog.getComponent());

				if (res != JOptionPane.YES_OPTION){
					return false;
				}
		}
		
		//04/04/06 cancelaCambiosEstaSesion(m_doc.getInstance(), m_sesion);
		//System.out.println("TXCTXROLLBACK");
//		System.out.println("Antes de cancel");
//		m_kba.getSizeMotor();
		//m_kba.setLockObject(m_ido, false, m_session);
		m_session.removeIchangeProperty(this);
		m_session.rollBack();
		if(m_sessionMaster!=m_session)//Esto ocurre si se estaba editando una fila y se pulsa sobre la x de la ventana
			m_sessionMaster.rollBack();
		m_actionManager.closeForm(dialog,m_kba,false);
		
//		System.out.println("Despues de cancel");
//		m_kba.getSizeMotor();
		
		return true;
	}

	private boolean hasSetUserTaskState() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		boolean setUserTaskState=false;
		if(m_idoParent!=null && Auxiliar.equals(m_kba.getClass(m_idoParent),m_idtoUserTask)){
			if(m_kba.isSpecialized(m_idtoUserTask, Constants.IDTO_EXPORT))//Los exports no tienen TargetClass pero nos interesa que se ponga el estado a realizado
				setUserTaskState=true;
			else{
				ObjectProperty propertyTarget=m_kba.getTarget(null, m_idtoUserTask, m_userRol, m_session);
			
				int idto=m_kba.getClass(m_ido);
				boolean isRange=m_kba.getIdRange(propertyTarget,idto)!=null;
				if(!isRange){
					Iterator<Integer> itr=m_kba.getAncestors(idto);
					while(itr.hasNext() && !isRange){
						int idtoParent=itr.next();
						isRange=m_kba.getIdRange(propertyTarget,idtoParent)!=null;
					}
				}
				setUserTaskState=isRange;
			}
		}
		return setUserTaskState;
	}
	
	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
//		crearé un doc de creación/modificación de instancias

		/*System.out.println("Antes de confirm");
		m_kba.getSizeMotor();*/
		
		//Creamos una nueva sesion para la confirmacion final ya que al parar la edicion y al poner el estado de realizado se pueden disparar reglas para hacer operaciones finales. Estas modificaciones
		//tenemos que ser capaces de quitarlas si se produce un fallo en la coherencia o en base de datos. Teniendo una sesion hija como esta, podemos hacerlo sin perder datos anteriores. Ademas la
		//sesion hija creada es del tipo forceParent=true para forzar a nuestra m_session a que haga commit.
		DefaultSession sessionConfirm=m_kba.createDefaultSession(m_session,m_idtoUserTask,m_session.isCheckCoherenceObjects(),m_session.isRunRules(),m_session.isLockObjects(),m_session.isDeleteFilters(),true);
		sessionConfirm.addIchangeProperty(this, false);
		boolean success=false;
		String mensajeRespuesta=null;
		IHistoryDDBBListener historyListener=null;
		try{
			boolean setUserTaskState= hasSetUserTaskState();
			/*if(setUserTaskState){
				m_kba.setState(m_idoParent,m_idtoParent,Constants.INDIVIDUAL_PREREALIZADO,m_userRol,m_idtoUserTask,sessionConfirm);
			}*/
			
			stopEdition(m_ido,sessionConfirm);		
			
			//System.out.println("Ejecutar: Sesion"+m_session.getID()+" Padre:"+m_session.getIDMadre());
			
			boolean isRight=true;
			//Si el ido que hemos modificado es del tipo de la targetClass de la userTask le asignamos a esta el valor
			//de realizado para que se lancen las reglas que verifiquen los datos
			if(setUserTaskState){
				//try{
					m_kba.setState(m_idoParent,m_idtoParent,Constants.INDIVIDUAL_REALIZADO,m_userRol,m_idtoUserTask,sessionConfirm);
				/*}catch(OperationNotPermitedException e){
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
					isRight=false;
				}*//*catch(Exception ex){
					ex.printStackTrace();
					m_control.showMessage("El individuo no está completado correctamente");
					isRight=false;
				}*/
				//isRight=setUserTaskState(/*m_kba.getIdoUserTask(m_idtoUserTask)*/m_idoParent,Constants.IDO_REALIZADO,m_session);
			}
	
			if(isRight){ 
				if(idtoDirectReports!=null && !idtoDirectReports.isEmpty()){
					historyListener=new IHistoryDDBBListener(){

						public void initChangeHistory(){}
						
						public void endChangeHistory(){}
						
						public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
							System.out.println("mido "+m_ido+" ido:"+ido+" operation:"+operation+" idtoUserTask:"+idtoUserTask+" sessionUsed:"+sessionUsed+" oldIdo:"+oldIdo+" rdn:"+rdn+" this:"+this.hashCode());
							if(m_ido==oldIdo || m_ido==ido){
								KnowledgeBaseAdapter kba=null;
								try{
									kba=Singleton.getInstance().getKnowledgeBaseAdapter(sessionUsed.getKnowledgeBase());
									if(idtoDirectReports!=null){
										for(int idtoUserTaskReport:idtoDirectReports){
											int idoUserTaskReport=kba.getIdoUserTaskReport(idtoUserTaskReport);
											kba.print(ido, oldIdo,idto, idoUserTaskReport, idtoUserTaskReport, m_userRol, m_idtoUserTask);
											
										}
									}
								}catch (Exception e) {
									e.printStackTrace();
									communicator com=kba!=null?kba.getLocalComm():m_com;
									com.logError(dialog.getParentDialog()!=null?dialog.getParentDialog().getComponent():dialog.getComponent(),e, "Error al intentar imprimir "+rdn);
								} catch (NoClassDefFoundError e){
									e.printStackTrace();
									communicator com=kba!=null?kba.getLocalComm():m_com;
									com.logError(dialog.getParentDialog()!=null?dialog.getParentDialog().getComponent():dialog.getComponent(),null, "Error al intentar imprimir "+rdn);
								}
							}
						}
					};					
					m_kba.addHistoryDDBBListener(historyListener);					
				}
				boolean exito=false;
//				try{
					exito=sessionConfirm.commit();
					// m_kba.setLockObject(m_ido, false, m_session);
					if(exito){
						/*if(m_kba.getDefaultSession().getID()==m_session.getIDMadre())
							m_kba.getDefaultSession().commit();*/
						//	if(setUserTaskState)//Volvemos a poner la userTask como pendiente ya que la reutilizamos
						//	setUserTaskState(m_kba.getIdoUserTask(m_idtoUserTask),Constants.IDO_PENDIENTE,m_kba.getDefaultSession());
						m_actionManager.closeForm(dialog,m_kba,true);
						//m_com.unlockObjects(, m_com.getUser());
						mensajeRespuesta="Modificación realizada";
					}else{
						m_actionManager.closeForm(dialog,m_kba,true);
						mensajeRespuesta="Ningún campo modificado";
					}
					success=true;
					//System.out.println("Despues del commit:"+m_kba.getSizeMotor());
//				}catch(IncompatibleValueException e){
//					e.printStackTrace();
//					Property prop=e.getProp();
//					String message=e.getUserMessage();
//					if (prop!=null)
//						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_kba.getGroup(prop.getIdProp(),prop.getIdto(),m_idtoUserTask), m_idtoUserTask);
//	
//					m_control.showErrorMessage(message);
//	
//					//sessionConfirm.setForceParent(false);
//					//sessionConfirm.rollBack();
//				}catch(CardinalityExceedException e){
//					System.err.println(e.getProp());
//					e.printStackTrace();
//					Property prop=e.getProp();
//					String message=e.getUserMessage();
//					if (prop!=null){
//						if (prop.getIdo()!=m_ido){
//							message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_kba.getGroup(prop.getIdProp(),prop.getIdto(),m_idtoUserTask), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask)+" '"+m_kba.getValueData(m_kba.getRDN(prop.getIdo(), m_userRol, m_idtoUserTask, sessionConfirm))+"'");
//						}else{
//							message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_kba.getGroup(prop.getIdProp(),prop.getIdto(),m_idtoUserTask), m_idtoUserTask);
//						}
//						
//					}
//						
//	
//					m_control.showErrorMessage(message);
//	
//					//sessionConfirm.setForceParent(false);
//					//sessionConfirm.rollBack();
//				}
			/*System.out.println("Despues de confirm");
			m_kba.getSizeMotor();*/
			}
		}finally{
			if(historyListener!=null)
				m_kba.removeHistoryDDBBListener(historyListener);
			if(!success){
				sessionConfirm.setForceParent(false);
				try{
					sessionConfirm.rollBack();
				}catch(Exception ex){
					System.err.println("No se ha podido hacer rollback de la session");
					ex.printStackTrace();
				}
			}
		}
		return mensajeRespuesta;
	}

	public void actionPerformed(java.awt.event.ActionEvent e) {

		try{
			dialog.disabledEvents();
			//System.out.println("TRANSITIONCONTROL:"+e.getActionCommand());

			String command = e.getActionCommand();
			//System.out.println("OPGUI:COMMAND:" + command);

			IdOperationForm idOperation=new IdOperationForm(command);
			Integer operationType=idOperation.getOperationType();
			Integer buttonType = idOperation.getButtonType();

			IdObjectForm target=idOperation.getTarget();
			Integer idProp = target.getIdProp();
			Integer idtoUserTask = target.getIdtoUserTask();
			target.setIdtoUserTask(null);//Para el id de las tablas
			String idTable=target.getIdString();


			/*java.util.StringTokenizer stk = new StringTokenizer(e.
		                getActionCommand(), ":");
		        String targetType = "";
		        int botonType = 0;
		        int tableIndex = 0, rootIndex = 0;
		        String tableName = "";
		        try {
		            targetType = stk.nextToken();
		            rootIndex = Integer.parseInt(stk.nextToken());
		            tableIndex = Integer.parseInt(stk.nextToken());
		            botonType = Integer.parseInt(stk.nextToken());
		            if(stk.hasMoreTokens())
		            	tableName = stk.nextToken();
		        } catch (NoSuchElementException ne) {
		            m_com.logError(dialog.getComponent(),ne, "TX CONTROL:ACTION PERFORMED:NO MAS TOKEN");
		        }*/

			//No nos sirve mostrar nada en la barra ya que el formulario de abajo no cambia al ser una imagen
//			Object boton=e.getSource();
//			if(boton instanceof AbstractButton){
//				/*   if(((AbstractButton)boton).getToolTipText()!=null){*/
//				if(idProp!=null){
//					Property p=m_kba.getProperty(target.getIdo(), idProp, m_userRol, idtoUserTask, m_session);
//					String tableName = m_kba.getLabelProperty(p,p.getIdto(),m_kba.getGroup(idProp),idtoUserTask);
//					Singleton.getInstance().getStatusBar().setLocalizacion(((AbstractButton)boton).getToolTipText()+"("+Utils.normalizeLabel(tableName)+")",Singleton.getInstance().getStatusBar().getNivelLocalizacion()+1);
//				}
//				/*   }*/
//			}
//
//			if (buttonType!=botoneraAccion.CLOSE)
//				Singleton.getInstance().getStatusBar().setBarraProgreso();
			// Hacemos que se ejecute en un nuevo hilo para mantener libre el hilo AWT-EventQueue
			/*final String targetThis=targetType;
		 	   	final Integer tableIndexThis=tableIndex;
		 	   final Integer rootIndexThis=rootIndex;
		 	   	final int botonTypeThis=botonType;*/


			final Integer operationTypeThis=operationType;
			final Integer idPropThis=idProp;
			final Integer idtoUserTaskThis=idtoUserTask;
			final int buttonTypeThis=buttonType;
			final String idTableThis=idTable;

			SwingWorker worker=new SwingWorker(){
				public Object construct(){										
					boolean success=false;
					try{
						Object result=doWorkEvent(operationTypeThis,idPropThis,idtoUserTaskThis,idTableThis,buttonTypeThis);
						success=true;
						return result;
					}finally{
						if(!success)
							doFinished();
					}
				}

				public void finished(){
					//dialog.setEnabled(true);
					StatusBar statusBar=Singleton.getInstance().getStatusBar();
					if(statusBar.hasBarraProgreso())
						statusBar.setFinishBarraProgreso((String)getValue(),true);
					else if(!statusBar.isError())
						statusBar.setAccion((String)getValue());
					dialog.enabledEvents();
				}
			};
			worker.start();
			
		}catch(Exception ex){
			//m_control.closeEvent();
			m_com.logError(dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}

	private synchronized Object doWorkEvent(Integer operationType,Integer idProp,Integer idtoUserTask,final String idTable,int buttonType){
		String mensajeRespuesta="";

		//System.err.println("transitionControl m_idtoUserTask:"+m_idtoUserTask+" idtoUserTask:"+idtoUserTask);
		//Sesion padre que se le pasara a la nueva accion. No sera hija de la actual cuando se cambie de usertask
		Session sessionParentAction=m_session;
		try{
			if (operationType==botoneraAccion.OPERATION_SCROLL){
				if( buttonType== botoneraAccion.NEXT ||	buttonType == botoneraAccion.PREV ){

					Session sessionFormParent=SessionController.getInstance().getSession(sessionParentAction.getIDMadre());
					final WindowComponent windowParent=dialog.getParentDialog();
					if(windowParent!=dialog.getMainDialog()){//Si es el dialogo principal no nos interesa deshabilitar los eventos ya que la ventana a abrir no sera modal
						windowParent.disabledEvents();
						windowParent.lockChangeStateEvents(true);//No permitimos que el cancelar o confirmar cambien el estado enable/disabled de las ventanas ya que tenemos que gestionarlo aqui
					}
					boolean oldReusable=sessionFormParent.isReusable();
					sessionFormParent.setReusable(true);//Evitamos que la sesion padre sea eliminada ya que provoca el dispose de DocDataModel y nos interesa reutilizarlo
					boolean success=false;
					try{
						if(m_operation==access.VIEW)
							cancel();
						else{
							if(this.hasUserModified){
								confirm();
							}else if(this.hasSystemModified){
								Object[] options = {"Sí", "No"};
								int res;
								String aux="Hay datos modificados por el sistema, ¿desea almacenarlos?";
								res = Singleton.getInstance().getMessagesControl().showOptionMessage(aux,
											Utils.normalizeLabel("CONFIRMACIÓN DE GUARDAR"),
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE,
											null,
											options,
											options[0],dialog.getComponent());
	
									if (res == JOptionPane.YES_OPTION){
										confirm();
									}else{
										askCancel=false;
										cancel();
										askCancel=true;
									}
							}
							else{
								askCancel=false;
								cancel();
								askCancel=true;
							}
						}
						
						success=true;
					}finally{
						sessionFormParent.setReusable(oldReusable);
						if(!success){
							if(windowParent!=dialog.getMainDialog())
								windowParent.lockChangeStateEvents(false);
						}
					}
					//eliminaVentana();
			
					GIdRow lista = buttonType == botoneraAccion.NEXT ?
							tableNavigation.nextRow() : tableNavigation.prevRow();
							if (lista == null) {
								Singleton.getInstance().getMessagesControl().showMessage("NO HAY MAS REGISTROS EN LA BUSQUEDA",dialog.getComponent());
								if(windowParent!=dialog.getMainDialog()){
									windowParent.lockChangeStateEvents(false);
									windowParent.enabledEvents();
								}
								return null;
							}
							/*try {
                       m_appControl.unlockObjects();
                   } catch (SystemException se) {
                       m_com.logError(dialog.getComponent(),se);
                       return "Error";
                   }*/
							int ido = lista.getIdo();
							int idto = lista.getIdto();

							//boolean editEnabled = tgf.botonera.getEnabled(botoneraAccion.EDITAR);
							//if (mode == access.SET && !editEnabled)
							//	mode = access.VIEW;
////									ArrayList commandList = new ArrayList();
////									commandList.add(new commandPath(idto, ido, /*tgf.id*/idtoUserTask, mode,/*access*/new access(mode)));
//									ArrayList<commandPath> commandList = new ArrayList<commandPath>();
//									/*commandList.add(new commandPath(ido,m_currMode,idtoUserTask));*/
//									commandPath commandPath;
//									if(mode==access.VIEW)
//										commandPath=new ViewCommandPath(m_kba.getIdoUserTask(idtoUserTask),ido,idtoUserTask,userRol,m_kba.getDefaultSession());
//									else commandPath=new SetCommandPath(m_kba.getIdoUserTask(idtoUserTask),ido,idtoUserTask,userRol,m_kba.getDDBBSession());
//									commandList.add(commandPath);
							
//									exeActions(commandList,buildBotoneraAHeredar(idTarget, idtoUserTask));
							//m_actionManager.closeEvent();
							
							//Lo hacemos en el hilo AWTEvent para que el closeEvent anterior realice antes el coverContainer en Assistant 
//									final int modeThis=mode;
//									final Runnable doFinished = new Runnable() {
//										public void run() {
//											final Runnable doLock = new Runnable() {
//												public void run() {
//													control.lockChangeStateEvents(false);//Permitimos el cambio de estado de los eventos:habilitado/deshabilitado
//												}
//											};
//											SwingUtilities.invokeLater(doLock);//Invocandolo despues nos aseguramos que no este permitido un cambio de evento antes de tiempo por el cual el finished del exeEvent anterior lo habilitaria
//											exeEvent(getComponent(),botoneraAccion.OPERATION_ACTION,idTarget,idtoUserTask,modeThis==access.VIEW?botoneraAccion.CONSULTAR:botoneraAccion.EDITAR);
//										}
//									};
//									m_controlDialog.lockChangeStateEvents(true);//Bloqueamos el cambio de estado de eventos para que el finished de exeEvent no lo habilite ya que aun no hemos terminado la operacion hasta que se ejecute el doFinished 
//									SwingUtilities.invokeLater(doFinished);
						//	System.err.println("ENTRA POR NEXT O PREV");
							final ArrayList<commandPath> commandList = new ArrayList<commandPath>();
							commandPath commandPath;
							if(m_operation==access.VIEW)
								commandPath=new ViewCommandPath(m_idoParent,m_idtoParent,ido,idto,m_idtoUserTask,m_userRol,sessionFormParent);
							else 
								commandPath=new SetCommandPath(m_idoParent,m_idtoParent,ido,idto,m_idtoUserTask,m_userRol,sessionFormParent);
							
							commandPath.setAlias(aliasMap);
							commandList.add(commandPath);

							// Lo hacemos en el hilo AWTEvent para que con el closeEvent de cancel o confirm se realice antes el coverContainer en Assistant  ya que si no
							// vuelve a oscurecer el formulario padre, viendose cada vez mas oscuro
							final Runnable doFinished = new Runnable() {
								public void run() {
									boolean success=false;
									try{
										exeActions(commandList,tableNavigation,dialog.getParentDialog());
										success=true;
									}catch(OperationNotPermitedException e){
										//idTableEditionToDDBB=null;
										//m_control.closeEvent();
										Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
									}catch(Exception e){
										//idTableEditionToDDBB=null;
										//m_control.closeEvent();
										e.printStackTrace();
										m_com.logError(dialog.getComponent(),e,"Error al ejecutar la operación");
									}finally{
										if(windowParent!=dialog.getMainDialog()){
											windowParent.lockChangeStateEvents(false);
											//Lo comento porque provoca que se traten los eventos de click del raton sobre la ventana. Ademas no hace falta ya que la ventana esta oscurecida,
											//cuando se desoscurezca se pondran los eventos habilitados
											//windowParent.enabledEvents();
											if(!success)
												windowParent.enabledEvents();
										}
									}
								}
							}; 
							final Runnable doFinished1 = new Runnable() {
								public void run() {
									SwingUtilities.invokeLater(doFinished);
								}
							};
							SwingUtilities.invokeLater(doFinished1);
									
						}
					//cancel();
			}else if (operationType==botoneraAccion.OPERATION_ACTION) {
				if (buttonType == botoneraAccion.CLOSE) {
					//System.out.println("CLOSING TX CONTROL");
					//remove();
					if(cancel())
						mensajeRespuesta="Formulario cerrado";
				}

				if (buttonType == botoneraAccion.HELP){
					//Singleton.getInstance().getMessagesControl().showMessage("Este tipo de objeto es un "+m_kba.getLabelClass(m_idto,idtoUserTask));
					Singleton.getInstance().getHelpComponent().showHelp(m_idto,idtoUserTask,m_userRol,dialog.getComponent());
				}

				if (buttonType == botoneraAccion.EJECUTAR){
					boolean confirm=true;
					
					if(m_operation==access.NEW && !m_kba.getAccessIndividual(m_ido, m_userRol, m_idtoUserTask).getSetAccess()){
						Object[] options = {"Sí", "No"};
						int res;
						String aux="¿Está seguro que desea guardar y cerrar?\nEste formulario no podrá volver a editarlo";
						res = Singleton.getInstance().getMessagesControl().showOptionMessage(aux,
									Utils.normalizeLabel("CONFIRMACIÓN DE ACEPTAR"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[0],dialog.getComponent());

						confirm=(res == JOptionPane.YES_OPTION);
					}
					
					if(confirm)
						mensajeRespuesta=confirm();
				}
				
				if (buttonType == botoneraAccion.CONFIG_COLUMNPROPERTIES) {
					ObjectProperty property=m_kba.getChild(m_ido,m_idto,idProp,m_userRol,idtoUserTask,m_kba.getDefaultSession());
					
					IdObjectForm idObjForm=new IdObjectForm(idTable);

					int idObject=m_kba.getIdRange(property, idObjForm.getValueCls());
					int idtoObject=idObjForm.getValueCls();
					int idtoTable=idtoObject;
					int idtoParent=property.getIdto();
					boolean filterMode=false;
					boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
					boolean onlyFirstLevel=false;
					HashMap<String,String> aliasMap=null;
					
					boolean oldReusable=m_kba.getDDBBSession().isReusable();
					try{
						m_kba.getDDBBSession().setReusable(true);
						DefaultSession sess=new DefaultSession(m_kba.getKnowledgeBase(), m_kba.getDDBBSession(), idtoUserTask, true, true, true, true, true);
						ArrayList<Column> columnList=m_kba.getColumnsObject(idObject, idtoObject, idtoTable, idtoParent, null, idtoUserTask, sess, filterMode, structural, onlyFirstLevel, aliasMap);
						
						ColumnsTableComponent component=new ColumnsTableComponent(m_kba, this.dialog, idtoUserTask, property, sess, columnList);
					}finally{
						m_kba.getDDBBSession().setReusable(oldReusable);
					}
					
				}

				if (buttonType == botoneraAccion.ACTION || buttonType == botoneraAccion.EXPORT){
					final HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
					IHistoryDDBBListener newIdoListener=new IHistoryDDBBListener(){

						public void initChangeHistory(){}
						
						public void endChangeHistory(){}
						
						public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
							//System.err.println("ido:"+ido+" operation:"+operation+" idtoUserTask:"+idtoUserTask+" sessionUsed:"+sessionUsed+" oldIdo:"+oldIdo+" rdn:"+rdn+" this:"+this.hashCode());
							if(m_ido==oldIdo || m_ido==ido){
								values.put(ido, idto);
							}
						}
					};
					if(!Constants.isIDTemporal(m_ido))
						values.put(m_ido, m_idto);
					//m_kba.addHistoryDDBBListener(newIdoListener);
					if(!m_kba.isGlobalUtask(m_idtoUserTask)) m_kba.addHistoryDDBBListener(newIdoListener);
					boolean successCommit=false;
					boolean success=false;
					try{
						boolean oldReusable=m_kba.getDDBBSession().isReusable();
						try{
							m_kba.getDDBBSession().setReusable(true);
							mensajeRespuesta=confirm();
							successCommit=true;
						}finally{
							m_kba.getDDBBSession().setReusable(oldReusable);
						}
						ArrayList<commandPath> commandList = new ArrayList<commandPath>();
						
//						Iterator<GIdRow> itr=parList.iterator();
//						while(itr.hasNext()){
//							GIdRow idRow=itr.next();
//							values.put(idRow.getIdo(),idRow.getIdto());
//						}
						
						commandPath commandPath;
						if(m_kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION)){
							commandPath=new ActionCommandPath(m_kba.getIdoUserTaskAction(idtoUserTask),values,idtoUserTask,m_userRol,m_kba.getDDBBSession());
						}else{
							commandPath=new ExportCommandPath(m_kba.getIdoUserTaskExport(idtoUserTask),idtoUserTask,values,idtoUserTask,m_userRol,m_kba.getDDBBSession());
						}
						
						commandList.add(commandPath);

						exeActions(commandList,null,dialog.getParentDialog());
						success=true;
					}finally{
						if(!success && successCommit){//Si se ha guardado el formulario inicial pero no se ha creado el de la acción descartamos el motor ya que no se volvera a usar
							Singleton.getInstance().removeKnowledgeBaseAdapter(m_kba.getKnowledgeBase()).dispose();
						}
						m_kba.removeHistoryDDBBListener(newIdoListener);
					}
				}
				
				if (buttonType == botoneraAccion.CANCEL){
					if(cancel())
						mensajeRespuesta="Acción cancelada";
				}

				if (buttonType == botoneraAccion.BUSCAR) {
					// es el boton de añadir o relacionar de encima de una tabla

					if( m_operation != access.VIEW ){
						IdObjectForm idObjForm=new IdObjectForm(idTable);
						int idPropTable=idObjForm.getIdProp();
						int idoTable=idObjForm.getIdo();
						int value=idObjForm.getValueCls();
						ObjectProperty property=m_kba.getChild(m_ido, m_idto, idPropTable, m_userRol, idtoUserTask,m_session);
						ArrayList<commandPath> commandList = new ArrayList<commandPath>();
						/*commandList.add(new commandPath(idoTable,idPropTable,-1,access.SET,access.OVER_PROPERTY,idtoUserTask));*/
						if(property.getCardMax()!=null && property.getCardMax()==1 && m_kba.getIdoValue(property)!=null){
							ObjectValue valueOP=m_kba.getValue(property);
							if(m_kba.getLevelObject(valueOP.getValue())==Constants.LEVEL_PROTOTYPE){
								DataProperty p=m_kba.getRDN(valueOP.getValue(), valueOP.getValueCls(), m_userRol, m_idtoUserTask, m_session);
								
								Object[] options = {"Sí", "No"};
								int res;
								res = Singleton.getInstance().getMessagesControl().showOptionMessage("Perderá la información de "+m_kba.getLabelProperty(property, property.getIdto(), idtoUserTask)+" "+(((StringValue)p.getUniqueValue())!=null?("'"+((StringValue)p.getUniqueValue()).getValue()+"'"):"") +"\n¿Desea continuar?",
											Utils.normalizeLabel("CONFIRMACIÓN DE BORRADO"),
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE,
											null,
											options,
											options[1],dialog.getComponent());
		
									if (res == JOptionPane.YES_OPTION){
										m_kba.deleteObject(valueOP.getValue(), valueOP.getValueCls(), (String)m_kba.getValueData(p), m_userRol, m_idtoUserTask, m_session);
										FindRelCommandPath commandPath=new FindRelCommandPath(idoTable,idPropTable,value,idtoUserTask,m_userRol,sessionParentAction,null);
										commandList.add(commandPath);
										exeActions(commandList,null,dialog);
										return null;
									}else{
										return null;
									}
											
							}else{
								FindRelCommandPath commandPath=new FindRelCommandPath(idoTable,idPropTable,value,idtoUserTask,m_userRol,sessionParentAction,null);
								commandList.add(commandPath);
								exeActions(commandList,null,dialog);
								return null;
							}
						}else{
							FindRelCommandPath commandPath=new FindRelCommandPath(idoTable,idPropTable,value,idtoUserTask,m_userRol,sessionParentAction,null);
							commandList.add(commandPath);
							exeActions(commandList,null,dialog);
							return null;
						}
						
					}else{
						Singleton.getInstance().getMessagesControl().showMessage("Recuerde que ha elegido el modo \"lectura\", no \"edición\"",dialog.getComponent());
						return null;
					}
				}

//				if (buttonType == botoneraAccion.ABRIR) {
//				// es el boton de añadir o relacionar de una tabla de una sola fila

//				if(m_operation != access.VIEW ){
//				IdObjectForm idObjForm=new IdObjectForm(idTable);
//				int idPropTable=idObjForm.getIdProp();
//				int idoTable=idObjForm.getIdo();
//				int value=idObjForm.getValueCls();
//				ArrayList<commandPath> commandList = new ArrayList<commandPath>();
//				/*commandList.add(new commandPath(idoTable,idPropTable,-1,access.SET,access.OVER_PROPERTY,idtoUserTask));*/
//				FindRelCommandPath commandPath=new FindRelCommandPath(idoTable,idPropTable,value,idtoUserTask,m_userRol,sessionParentAction);
//				commandList.add(commandPath);
//				exeActions(commandList);
//				}else{
//				m_actionManager.showMessage("Recuerde que ha elegido el modo \"lectura\", no \"edición\".");
//				return null;
//				}
//				}
				if (buttonType == botoneraAccion.CREAR) {
					//es el boton de crear y relacionar de un paso.
					IdObjectForm idObjForm=new IdObjectForm(idTable);
					int idPropTable=idObjForm.getIdProp();
					//int ido=idObjForm.getIdo();
					//System.out.println("Este es el idTable:"+idTable);

					ObjectProperty property=m_kba.getChild(m_ido, m_idto, idPropTable, m_userRol, idtoUserTask,m_session);
					Integer NMaxCurr = property.getCardMax();
					//Integer NMinCurr = property.getCardMin();
					if (NMaxCurr!=null && NMaxCurr > 1 && m_formManager.getRowCount(idTable) >= NMaxCurr) {
						Singleton.getInstance().getMessagesControl().showMessage("HA SOBREPASADO EL NUMERO DE OBJETOS,\n"+ "PARA AÑADIR DEBE ELIMINAR UNO PRIMERO",dialog.getComponent());
						return null;
					}
					
					boolean creationToDDBB=false;
					if(!Auxiliar.equals(idtoUserTask,m_idtoUserTask) /*|| (idProp!=null && !m_kba.getCategoryProperty(idProp).isStructural())*/){
						Object[] options = {"Sí", "No"};
						int res;
						res = Singleton.getInstance().getMessagesControl().showOptionMessage("La creación será almacenada directamente en base de datos aunque cancele el formulario actual.\n¿Desea continuar?",
									Utils.normalizeLabel("AVISO DE CREACIÓN DIRECTA"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE,
									null,
									options,
									options[0],dialog.getComponent());

							if (res == JOptionPane.NO_OPTION){	
								mensajeRespuesta="Acción cancelada";
								return mensajeRespuesta;
							}
						sessionParentAction=m_kba.getDDBBSession();//m_kba.createDefaultSession(m_kba.getDDBBSession(),idtoUserTask,true,true,true,true,false);
						creationToDDBB=true;
					}

//					int idClass=property.getRangoList().getFirst();
//					/*int idObject=m_kba.createPrototype(idClass, userRol, idtoUserTask);*/
//					int idObject=m_kba.createPrototype(idClass,Constants.LEVEL_PROTOTYPE, /*new session(),*/ m_userRol, idtoUserTask);
//					m_kba.setValue(/*m_id, *//*property,*/m_ido,idPropTable, m_kba.getValueOfString(String.valueOf(idObject), idClass),/*action.NEW*/m_userRol,m_idtoUserTask/*, new session()*/);

					//int modo=access.NEW;

					
					/*int idoFilter=m_kba.getIdoFilter(property);
 	     	     	   	int idtoFilter=m_kba.getIdtoFilter(property);*/
					int idRange=m_kba.getIdRange(property,m_kba.getClass(idObjForm.getValueCls()));
					
					if(property.getCardMax()!=null && property.getCardMax()==1 && m_kba.getIdoValue(property)!=null){
						ObjectValue valueOP=m_kba.getValue(property);
						if(m_kba.getLevelObject(valueOP.getValue())==Constants.LEVEL_PROTOTYPE){
							DataProperty p=m_kba.getRDN(valueOP.getValue(), valueOP.getValueCls(), m_userRol, m_idtoUserTask, m_session);
							
							Object[] options = {"Sí", "No"};
							int res;
							res = Singleton.getInstance().getMessagesControl().showOptionMessage("Perderá la información del individuo "+ ((StringValue)p.getUniqueValue()).getValue()+"\n¿Desea continuar?",
										Utils.normalizeLabel("CONFIRMACION BORRADO"),
										JOptionPane.YES_NO_OPTION,
										JOptionPane.WARNING_MESSAGE,
										null,
										options,
										options[1],dialog.getComponent());
	
								if (res == JOptionPane.YES_OPTION){
									m_kba.deleteObject(valueOP.getValue(), p.getIdto(), (String)m_kba.getValueData(p), m_userRol, m_idtoUserTask, m_session);
								}else{
									mensajeRespuesta="Formulario de inserción cancelado";
									return mensajeRespuesta;
								}
								
								
								
						}
					}
					
					commandPath commandPath=null;
					if(creationToDDBB){
						commandPath=new NewCommandPath(/*m_ido*/m_kba.getIdoUserTask(idtoUserTask),idtoUserTask,/*idPropTable*/Constants.IdPROP_TARGETCLASS,idRange,idtoUserTask,m_userRol,sessionParentAction);
						//commandPath=new NewRelCommandPath(m_ido,idPropTable,idtoFilter,Constants.LEVEL_PROTOTYPE,idtoUserTask,m_userRol,sessionParentAction);
						idTableEditionToDDBB=idObjForm;
						if(!m_kba.isGlobalUtask(m_idtoUserTask)) m_kba.addHistoryDDBBListener(this);
						//m_kba.addHistoryDDBBListener(this);//Para enterarme de cuando se almacene en base de datos y poder mostrarlo en la tabla
					}else commandPath=new NewRelCommandPath(m_ido,m_idto,idPropTable,idRange,Constants.LEVEL_PROTOTYPE,idtoUserTask,m_userRol,sessionParentAction);
					
					commandPath.setAlias(aliasMap);
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					commandList.add(commandPath);

					Session sessionAux=exeActions(commandList,null,dialog);
					if(creationToDDBB)
						sessionOperationDDBB=sessionAux;

					mensajeRespuesta="Formulario de inserción creado";

				}

				if (buttonType == botoneraAccion.ELIMINAR ||
						buttonType == botoneraAccion.UNLINK) {
					//System.out.println("TBINDEX:" + idProp);
					if (idProp == 0/* || rootIndex != 0*/) {
						m_actionManager.closeForm(dialog,m_kba,false);
						return "";
					}
					/*if (m_simpleForm.selectionIsGroup(tableIndex)) {*/
					if (m_formManager.selectionIsGroup(idTable)) {

						Singleton.getInstance().getMessagesControl().showMessage("Debe seleccionar un elemento desglosado",dialog.getComponent());
						return null;
					}
					/*selectData objSelected = m_simpleForm.getInstanceSelectionData(tableIndex);*/
					ArrayList<GIdRow> objSelected = m_formManager.getIdRowsSelectionData(idTable);

					if (objSelected.isEmpty()) {
						Singleton.getInstance().getMessagesControl().showMessage("Para eliminar un objeto debe seleccionar al menos un registro",dialog.getComponent());
						return null;
					}
					boolean forceDeleted=true;
					Object[] options = {"Sí", "No"};
					int res;
					if(buttonType == botoneraAccion.ELIMINAR){
						res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								"¿Está seguro que desea borrar el elemento?",
								Utils.normalizeLabel("CONFIRMACION BORRADO"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[1],dialog.getComponent());

						if (res != JOptionPane.YES_OPTION) return "Eliminación de registro cancelada";

						mensajeRespuesta="Registro desvinculado y eliminado";
					}else{
						forceDeleted=false;
						boolean askDelete=false;
						boolean askUnrel=false;
						Iterator<GIdRow> itr= objSelected.iterator();
						while(itr.hasNext()){
							GIdRow obj=itr.next();
							/*if(Constants.isIDTemporal(obj.getIDO()))//Comentado ya que ahora siempre desvinculamos y luego la regla de aislados se encarga de borrar
								askDelete=true;
							else*/ askUnrel=true;
						}
						String textAction=askUnrel?"desvincular"+(askDelete?"/eliminar":""):"eliminar";
						String textNumElementos="el elemento";
						if (objSelected.size() > 1)
							textNumElementos="los elementos";
						res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								"¿Está seguro que desea "+ textAction +" "+textNumElementos+"?",
								Utils.normalizeLabel("CONFIRMACIÓN DE "+textAction),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[1],dialog.getComponent());

						if (res != JOptionPane.YES_OPTION)return "Operación cancelada";
					}
					removeElementRelationed(idTable, objSelected, forceDeleted, idtoUserTask);

				}

				if (buttonType == botoneraAccion.EDITAR ||
						buttonType == botoneraAccion.CONSULTAR) {
					//un contexto se confunde con el root de un filter. Si no hay ctx no hay permiso
					//ni siquiera de lectura, por tanto ni siquiera está este botón.

					int modo = buttonType == botoneraAccion.EDITAR ?
							access.SET :
								access.VIEW;
					//System.out.println("EDITANDO EN MODO:"+m_scope.getOperation());
					//es el boton de editar una parte. Hay dos posibilidades, que este en modo crear,
					//con lo cual no debo acceder al servidor, o que ya esté en modo modificación.

					if (m_formManager.selectionIsGroup(idTable))
						Singleton.getInstance().getMessagesControl().showMessage("Debe seleccionar un elemento desglosado.",dialog.getComponent());
					ArrayList<GIdRow> parSelected = m_formManager.getIdRowsSelectionData(idTable);

					if (parSelected.isEmpty()) {
						Singleton.getInstance().getMessagesControl().showMessage("Debe seleccionar al menos un registro",dialog.getComponent());
						return null;
					}
					/*if (parSelected.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN ÚNICO REGISTRO");
						return null;
					}*/
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					if (parSelected.size() > 1) {
      						
						HashMap<Integer,Integer> listIdo = new HashMap<Integer,Integer>();
						Iterator<GIdRow> it = parSelected.iterator();
						while(it.hasNext()){
							GIdRow idRow = it.next();
							listIdo.put(idRow.getIdo(),idRow.getIdto());
						}
						commandPath commandPath=null;

						if(modo==access.SET)                        			
							commandPath=new SetCommonCommandPath(m_ido,listIdo, idtoUserTask, m_userRol,sessionParentAction);        						
						else if(modo==access.VIEW)                        			
							commandPath=new ViewCommonCommandPath(m_ido,listIdo, idtoUserTask, m_userRol,sessionParentAction);

						commandPath.setAlias(aliasMap);
						commandList.add(commandPath);
						exeActions(commandList,null,dialog);

					}else{
						/*editarInstance(idProp, idtoUserTask, parSelected, true, modo);*/
						GIdRow rowSelected= parSelected.iterator().next();

						/*commandList.add(new commandPath(rowSelected.getIDO(),modo,idtoUserTask));*/
						commandPath commandPath;
						if(modo==access.VIEW)
							commandPath=new ViewCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),idtoUserTask,m_userRol,sessionParentAction);
						else 
							commandPath=new SetCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),idtoUserTask,m_userRol,sessionParentAction);
						
						commandPath.setAlias(aliasMap);
						commandList.add(commandPath);

						exeActions(commandList,new ITableNavigation(){

							public boolean hasNextRow() {
								return m_formManager.getNextRow(idTable)!=null;
							}

							public boolean hasPrevRow() {
								return m_formManager.getPrevRow(idTable)!=null;
							}

							public GIdRow nextRow() {
								GIdRow idRow=null;
								RowItem rItem=m_formManager.getNextRow(idTable);
								if(rItem!=null){
									idRow=rItem.getIdRow();
									if(idRow!=null)
										m_formManager.selectRow(idTable, idRow.getIdo());
								}
								return idRow;
							}

							public GIdRow prevRow() {
								GIdRow idRow=null;
								RowItem rItem=m_formManager.getPrevRow(idTable);
								if(rItem!=null){
									idRow=rItem.getIdRow();
									if(idRow!=null)
										m_formManager.selectRow(idTable, idRow.getIdo());
								}
								return idRow;
							}
							
						},dialog);
					}
				}
			}
			return mensajeRespuesta;
		}catch(IncompatibleValueException e){
			e.printStackTrace();
			Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage(),dialog.getComponent());
			return null;
		}catch(CardinalityExceedException e){
			//System.err.println(e.getProp());
			//e.printStackTrace();
			Property prop=e.getProp();
			String message=e.getUserMessage();
			if (prop!=null){
				try{
					if (prop.getIdo()!=m_ido){
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask);
						String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), m_userRol, m_idtoUserTask, m_session));
						if(rdn!=null)
							message+=" '"+rdn+"'";
						
						Object[] options = {"Aceptar", "Cancelar"};
						
						int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								message+"\n¿Desea editarlo abriendo su formulario?",
								"Campo no relleno",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[0],dialog.getComponent());

						if(res == JOptionPane.YES_OPTION){
							try{
								editInForm(m_ido, prop.getIdo());
							} catch (EditionTableException exc) {
								m_com.logError(dialog.getComponent(),exc, "Error al intentar editar fila en formulario");
								return null;
							}
						}
					}else{
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
						Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
					}
				}catch(Exception ex){
					e.printStackTrace();
					m_com.logError(dialog.getComponent(),e,/*"Error al intentar mostrar mensaje de cardinalidad"*/null);
				}
				
			}else{
				Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
			}

			return null;
		}catch(OperationNotPermitedException e){
			idTableEditionToDDBB=null;
			//m_actionManager.closeEvent();
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
			return null;
		}catch(InstanceLockedException e){
			idTableEditionToDDBB=null;
			//m_actionManager.closeEvent();
			Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage(),dialog.getComponent());
			return null;
		}catch(ActionException e){
			idTableEditionToDDBB=null;
			//m_actionManager.closeEvent();
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
			return null;
		}catch(Exception e){
			idTableEditionToDDBB=null;
			//m_actionManager.closeEvent();
			m_com.logError(dialog.getComponent(),e,"Error al ejecutar la operación");
			e.printStackTrace();
			return "Error de la operación";

		}
	}



	/*public void queryDocument( String title, String type, Element filter ) {
	m_control.queryDocument( title, type, filter );
   }*/

	/*public void unlockObjects() throws RemoteSystemException,CommunicationException{
  }

  public void unlockObject(int ido) throws RemoteSystemException,CommunicationException{
      try{
      if( m_locks.isLocked(ido) ){
	  m_com.serverUnlockObject(ido);
	  m_locks.unregLock(ido);
	  m_control.removeLockedInstance(ido);
      }
      }catch(SystemException e){
	  m_com.logError(dialog.getComponent(),e,"TRAN CONTROL, user event CANCEL:Error sistema al desbloquear");
      }
}


  public void removeLockedInstance( int ido ){
      m_control.removeLockedInstance(ido);
  }*/

	
	protected boolean doSetValue(int ido, int idProp, Value valueObject, Value valueOldObject) throws OperationNotPermitedException, NotFoundException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		boolean doSet=super.doSetValue(ido, idProp, valueObject, valueOldObject);
		if(doSet)
			hasUserModified=true;
		
		return doSet;
	}

	public void initChangeValue() {
		//System.err.println("TransitionControl.initChangeValue");
		listAddTableChangeValue=new HashMap<String, HashMap<Integer,Integer>>();
		listDelTableChangeValue=new HashMap<String, HashMap<Integer,GIdRow>>();
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int level, int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException {
		try{

			if(level!=Constants.LEVEL_FILTER && m_session.getState()==Session.USE_STATE){
				//System.err.println("Cambia valor en ido:"+ido+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+valueOld+" level:"+level+" m_session.getState:"+m_session.getState());
				
				IdObjectForm idObjForm=new IdObjectForm();
				idObjForm.setIdo(ido);
				idObjForm.setIdProp(idProp);
				idObjForm.setValueCls(valueCls);
	
				String idString=idObjForm.getIdString();
	
				if(value==null && m_formManager.hasComponent(idString)){
					if(valueOld instanceof DataValue)
						m_formManager.setValueComponent(idString, null, m_kba.getValueData((DataValue)valueOld));
					else if(valueOld instanceof ObjectValue)
						m_formManager.setValueComponent(idString, null, ((ObjectValue)valueOld).getValue());
				}else if(value instanceof DataValue || valueOld instanceof DataValue){
					if(m_formManager.hasComponent(idString)){
						//System.err.println("ChangeValue:DataValue con componente");
						Object oldValue=null;
						if(valueOld!=null)
							oldValue=m_kba.getValueData((DataValue)valueOld);
						m_formManager.setValueComponent(idString,m_kba.getValueData((DataValue)value),oldValue);
					}else{//Se trataria del registro de una de las tablas
						//System.err.println("ChangeValue:DataValue buscando en todas las tablas");
						ArrayList<String> listTables=m_formManager.getIdTables();
						//System.err.println("Esta es la lista de ids de tablas:"+listTables);
						//System.err.println("id a buscar:"+idString);
						Iterator<String> itr=listTables.iterator();
						while(itr.hasNext()){
							String idTable=itr.next();
							//int idtoRange=m_kba.getClass(new IdObjectForm(idTable).getValueCls());
							//boolean structural=m_kba.getCategoryProperty(new IdObjectForm(idTable).getIdProp()).isStructural();
							int numRow=m_formManager.getRowCount(idTable);
							for(int i=0;i<numRow;i++){
								RowItem rowItem=m_formManager.getCompletedDataTableFromIndex(idTable, i);
								if(rowItem.containsIdo(ido) && m_kba.isLoad(rowItem.getIdRow().getIdo()/*Podria no estar cargado ya en motor y quitarse en un aviso despues*/)){//Si contiene el ido significa que en la tabla se muestra ese individuo en una columna
										//selectData selectData=new selectData();
		
										/*---------Gestion del cambio:Modificacion del instance existente en la tabla-------------*/
//										selectData.addInstance(inst);
//										//System.err.println("instanceTable:"+inst);
//										Object newValue=m_kba.getValueData((DataValue)value);
//										Object oldValue=m_kba.getValueData((DataValue)valueOld);//m_kba.getValueData(property);//TODO En la property no es buena idea mirar ya que elecom cuando cambia un valor no modifica el instance
//										//System.err.println("newValue:"+newValue+" oldValue:"+oldValue);
//										if((newValue!=null && oldValue!=null && !newValue.equals(oldValue)) || (newValue==null && oldValue!=null) || (newValue!=null && oldValue==null)){
//											setValue(idString,newValue,oldValue,valueCls,valueCls);//TODO valueOldCls deberia ser pasado en este metodo, ya que no tienen porque ser el mismo, podrian ser hermanos
//											/*if(operation==action.DEL)
//												m_formManager.delRows(idTable, selectData);
//											else*/ m_formManager.addRows(idTable, selectData, true);
//										}
										
										if(!containsInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo())){
											/*-----------Gestion del cambio:Creacion de un nuevo instance que es el que sustituira al de la tabla-----------*/
											//selectData.addInstance(m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(), idtoRange,m_idto, m_userRol, m_idtoUserTask,m_session,false,structural,onlyFirstLevelColumnsTable));
											//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto(), idTable, m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,m_session);
											//System.out.println("registro tabla:"+selectData.getFirst());
											/*if(operation==action.DEL)
												m_formManager.delRows(idTable, selectData);
											else *///m_formManager.addRows(idTable, selectData, true);
											addInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto());
										}
								}
							}
						}
					}
	
				}else{
					if(m_formManager.hasComponent(idString)){
						Object oldValue=null;
						if(valueOld!=null)
							oldValue=((ObjectValue)valueOld).getValue();
						m_formManager.setValueComponent(idString,((ObjectValue)value).getValue(),oldValue);
					}else{// if( (valueOld!=null && m_kba.getLevelObject(((ObjectValue)valueOld).getValue())!=Constants.LEVEL_FILTER) || (value!=null && m_kba.getLevelObject(((ObjectValue)value).getValue())!=Constants.LEVEL_FILTER)){
						//Evitamos que trate los filtros como si fueran filas de las tablas
						
						ArrayList<String> listTables=m_formManager.getIdTables();
						//System.out.println("Esta es la lista de ids de tablas:"+listTables);
						//System.out.println("id a buscar:"+idString);
						if(listTables.contains(idString)){
							//System.err.println("ChangeValue: ObjectValue con tabla");
							if(valueOld!=null){
								if(value==null || (value!=null && !value.equals(valueOld)) ){
									GIdRow idRow=m_formManager.getDataTableFromIdo(idString, ((ObjectValue)valueOld).getValue());//m_kba.getTreeObject(((ObjectValue)valueOld).getValue(), m_userRol, m_idtoUserTask,m_session,KnowledgeBaseAdapter.TABLE_MODE)
									if(idRow!=null && !containsInListDelTableChangeValue(idString, ((ObjectValue)valueOld).getValue()))
										addInListDelTableChangeValue(idString, ((ObjectValue)valueOld).getValue(), idRow);
								}
							}
							if(value!=null){
								if(!containsInListAddTableChangeValue(idString, ((ObjectValue)value).getValue())){
									//selectData selectDataAdd=new selectData();
									//ObjectProperty objectP=m_kba.getChild(ido, idProp, m_userRol, m_idtoUserTask, m_session);
									//int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
									//boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
									//selectDataAdd.addInstance(m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),idtoRange,idto, m_userRol, m_idtoUserTask,m_session,false,structural,onlyFirstLevelColumnsTable));
									//instance inst=m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),((ObjectValue)value).getValueCls(),idString, m_formManager.getColumnTreeOfTable(idString), m_userRol, m_idtoUserTask,m_session);
									//m_formManager.addRows(idString, selectDataAdd, true);
									addInListAddTableChangeValue(idString, ((ObjectValue)value).getValue(), ((ObjectValue)value).getValueCls());
								}
							}
						}else{
							idString=getIdTableParent(idObjForm, valueCls, listTables);
							if(idString!=null){//Si es distinto de null se trataria de un registro de una tabla abstracta
								//System.err.println("ChangeValue: ObjectValue con tabla asbtracta");
								if(valueOld!=null){
									if(value==null || (value!=null && !value.equals(valueOld)) ){
										GIdRow idRow=m_formManager.getDataTableFromIdo(idString, ((ObjectValue)valueOld).getValue());//m_kba.getTreeObject(((ObjectValue)valueOld).getValue(), m_userRol, m_idtoUserTask,m_session,KnowledgeBaseAdapter.TABLE_MODE)
										if(idRow!=null && !containsInListDelTableChangeValue(idString, ((ObjectValue)valueOld).getValue()))
											addInListDelTableChangeValue(idString, ((ObjectValue)valueOld).getValue(), idRow);
									}
								}
								if(value!=null){
									if(!containsInListAddTableChangeValue(idString, ((ObjectValue)value).getValue())){
										//selectData selectDataAdd=new selectData();
										//ObjectProperty objectP=m_kba.getChild(ido, idProp, m_userRol, m_idtoUserTask, m_session);
										//int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));
										//boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
										//selectDataAdd.addInstance(m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),idtoRange,idto, m_userRol, m_idtoUserTask,m_session,false,structural,onlyFirstLevelColumnsTable));
										//instance inst=m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),((ObjectValue)value).getValueCls(),idString, m_formManager.getColumnTreeOfTable(idString), m_userRol, m_idtoUserTask,m_session);
										//m_formManager.addRows(idString, selectDataAdd, true);
										addInListAddTableChangeValue(idString, ((ObjectValue)value).getValue(), ((ObjectValue)value).getValueCls());
									}
								}
							}else{//Se podria tratar del registro de una de las tablas, recorremos todas las tablas buscando ese ido
								//System.err.println("ChangeValue: ObjectValue buscando en todas las tablas");
								//System.out.println("Esta es la lista de ids de tablas:"+listTables);
								//System.out.println("id a buscar:"+idString);
								Iterator<String> itr=listTables.iterator();
								//int idoSearch=ido!=null?ido:idto;//Solo sirve para las businessClass que sea idto
								while(itr.hasNext()){
									String idTable=itr.next();
									int numRow=m_formManager.getRowCount(idTable);
									for(int i=0;i<numRow;i++){
										RowItem rowItem=m_formManager.getCompletedDataTableFromIndex(idTable, i);
										if(rowItem.containsIdo(ido) && m_kba.isLoad(rowItem.getIdRow().getIdo()/*Podria no estar cargado ya en motor y quitarse en un aviso despues*/)){
											if(valueOld!=null){
												if(value==null/* || (value!=null && !value.equals(valueOld))*/ ){
													if(!containsInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo())){
														//selectData selectDataAdd=new selectData();
														//int idtoRange=m_kba.getClass(new IdObjectForm(idTable).getValueCls());
														//boolean structural=m_kba.getCategoryProperty(new IdObjectForm(idTable).getIdProp()).isStructural();
														//selectDataAdd.addInstance(/*inst*/m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),idtoRange,m_idto, m_userRol, m_idtoUserTask,m_session,false,structural,onlyFirstLevelColumnsTable));
														//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),rowItem.getIdRow().getIdto(),idTable, m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,m_session);
														//m_formManager.delRows(idTable, selectDataRemove);
														//m_formManager.addRows(idTable, selectDataAdd, true);
														addInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto());
													}
												}
											}
											if(value!=null){
												if(!containsInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo())){
													//selectData selectDataAdd=new selectData();
													//int idtoRange=m_kba.getClass(new IdObjectForm(idTable).getValueCls());
													//boolean structural=m_kba.getCategoryProperty(new IdObjectForm(idTable).getIdProp()).isStructural();
													//Creamos un nuevo treeObject ya que si el valor es de base de datos el instance anterior esta obsoleto ya que no tiene los datos de ese individuo
													//Para ellos usamos ido y no value porque se trata de un objectProperty que se le ha añadido al ido del instance de una fila
													//selectDataAdd.addInstance(m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),idtoRange/*inst.getIdTo()*/,m_idto, m_userRol, m_idtoUserTask,m_session,false,structural,onlyFirstLevelColumnsTable));
													//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),rowItem.getIdRow().getIdto(),idTable, m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,m_session);
													//System.out.println("registro tabla:"+selectData.getFirst());
													/*if(operation==action.DEL){
														if(value!=null)//TODO Esta comprobacion hay que quitarla cuando las sesiones avisen correctamente y no con null
															m_formManager.delRows(idTable, selectData);
													}else*/ //m_formManager.addRows(idTable, selectDataAdd, true);
													addInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto());
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			//m_formManager.setValueField(idString,m_kba.getValueData(value));
		}catch(AssignValueException e){
			m_com.logError(dialog.getComponent(),e,"Error en un cambio de valor");
			e.printStackTrace();
		}

	}
	
	public void endChangeValue() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		try{
			//System.err.println("TransitionControl.EndChangeValue");
			
			HashMap<String,HashMap<Integer,Integer>> listAddTableChangeValue=new HashMap<String, HashMap<Integer,Integer>>();
			HashMap<String,HashMap<Integer,GIdRow>> listDelTableChangeValue=new HashMap<String, HashMap<Integer,GIdRow>>();
			listAddTableChangeValue.putAll(this.listAddTableChangeValue);
			listDelTableChangeValue.putAll(this.listDelTableChangeValue);
			
			Iterator<String> itr=listDelTableChangeValue.keySet().iterator();
			while(itr.hasNext()){
				String idTable=itr.next();
				HashMap<Integer,GIdRow> listDel=listDelTableChangeValue.get(idTable);
				for(int idoDel:listDel.keySet()){
					//System.err.println("DelRow:"+listDel.get(idoDel));
					m_formManager.delRow(idTable,listDel.get(idoDel));
				}
			}
						
			itr=listAddTableChangeValue.keySet().iterator();
			while(itr.hasNext()){
				String idTable=itr.next();
				selectData source=new selectData();
				HashMap<Integer,GIdRow> listDel=listDelTableChangeValue.get(idTable);
				HashMap<Integer,Integer> listAdd=listAddTableChangeValue.get(idTable);
				for(int idoAdd:listAdd.keySet()){
					if(listDel ==null || !listDel.containsKey(idoAdd)){
						int idtoAdd=listAdd.get(idoAdd);
						//System.err.println("addRow:"+idoAdd);
						instance inst=m_kba.getTreeObjectTable(idoAdd,idtoAdd,idTable, m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,m_session);
						source.addInstance(inst);
					}
				}
				
				if(source.size()>0)
					m_formManager.addRows(idTable, source, true);
			}
		}catch(AssignValueException e){
			m_com.logError(dialog.getComponent(),e,"Error en un cambio de valor");
			e.printStackTrace();
		}
		
	}
	
	private void addInListAddTableChangeValue(String idTable,int ido,int idto){
		if(!listAddTableChangeValue.containsKey(idTable))
			listAddTableChangeValue.put(idTable, new HashMap<Integer, Integer>());
		listAddTableChangeValue.get(idTable).put(ido, idto);
	}
	
	private boolean containsInListAddTableChangeValue(String idTable,int ido){
		return listAddTableChangeValue.containsKey(idTable) && listAddTableChangeValue.get(idTable).containsKey(ido);
	}
	
	private void addInListDelTableChangeValue(String idTable,int ido,GIdRow idRow){
		if(!listDelTableChangeValue.containsKey(idTable))
			listDelTableChangeValue.put(idTable, new HashMap<Integer, GIdRow>());
		listDelTableChangeValue.get(idTable).put(ido, idRow);
	}
	
	private boolean containsInListDelTableChangeValue(String idTable,int ido){
		return listDelTableChangeValue.containsKey(idTable) && listDelTableChangeValue.get(idTable).containsKey(ido);
	}
	
	//TODO LLamar al setUserTaskState de KnowledgeBaseAdapter
//	private boolean setUserTaskState(int idoUserTask,int value,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
//		int valueCls=Constants.IDTO_ESTADOREALIZACION;
//		int idProp=Constants.IdPROP_ESTADOREALIZACION;
//		ObjectProperty property=m_kba.getChild(idoUserTask, idProp, m_userRol, m_idtoUserTask, ses);
//		Integer valueIdo=m_kba.getIdoValue(property);
//		String valueOld=valueIdo!=null?String.valueOf(valueIdo):null;
//		String valueNew=String.valueOf(value);
//		try{
//			m_kba.setValue(/*property,*/idoUserTask,idProp, m_kba.getValueOfString(valueNew,/*idtoFilter*/valueCls),m_kba.getValueOfString(valueOld,/*idtoFilter*/valueCls)/*, new session()*/,/*operation*/m_userRol,m_idtoUserTask,ses);
//		} catch (Exception e) {//TODO Aqui habria que diferenciar entre la excepcion de completado incorrectamente y la de fallos del sistema
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}


	public LinkedHashMap<Integer,Integer> getResult() {
		LinkedHashMap<Integer,Integer> result=new LinkedHashMap<Integer,Integer>();
		result.put(m_ido,m_idto);
		return result;
	}


	public void stopEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW && /*m_level*/m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER)
			m_kba.setLockObject(ido, false, sess);
	}


	public void startEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW && /*m_level*/m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER)
			m_kba.setLockObject(ido, true, sess);
	}
	
	public void stopEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW){
			ArrayList<Integer> idosAux=new ArrayList<Integer>();
			for(Integer ido:idos){
				if(m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER){
					idosAux.add(ido);
				}
			}
			if(!idosAux.isEmpty()){
				m_kba.setLockObject(idosAux, false, sess);
			}
		}
	}


	public void startEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW){
			ArrayList<Integer> idosAux=new ArrayList<Integer>();
			for(Integer ido:idos){
				if(m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER){
					idosAux.add(ido);
				}
			}
			if(!idosAux.isEmpty()){
				m_kba.setLockObject(idosAux, true, sess);
			}
		}
	}

	public void initChangeHistory(){}
	
	public void endChangeHistory(){}
	
	public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
		try {
			//System.err.println("ido:"+ido+" oldIdo:"+oldIdo+" SessionOperation:"+sessionOperationDDBB+" sessionUsed:"+sessionUsed);
			if(idTableEditionToDDBB!=null && sessionOperationDDBB.getID()==sessionUsed.getID()){
				if(idTableEditionToDDBB.getValueCls()==idto || m_kba.isSpecialized(idto, idTableEditionToDDBB.getValueCls())){
					//m_kba.loadIndividual(ido, m_userRol, idtoUserTask, m_session);
					GIdRow idRow=m_formManager.getDataTableFromIdo(idTableEditionToDDBB.getIdString(),oldIdo);
					if(idRow!=null)
						m_formManager.delRow(idTableEditionToDDBB.getIdString(),idRow);
					
					String idoRowLast=null;
					int idtoRowLast=idto;
					ObjectProperty property=m_kba.getChild(idTableEditionToDDBB.getIdo(),m_kba.getClass(idTableEditionToDDBB.getIdo()),idTableEditionToDDBB.getIdProp(), m_userRol, idtoUserTask, m_session);
					if(property.getCardMax()!=null && property.getCardMax()==1){
						if(!property.getValues().isEmpty()){
							ObjectValue value=(ObjectValue)property.getValues().get(0);
							idoRowLast=value.getValue().toString();
							idtoRowLast=value.getValueCls();
						}
					}
					setValue(idTableEditionToDDBB.getIdString(),ido, /*oldIdo+""*//*null*/idoRowLast, idto, idtoRowLast);
					idTableEditionToDDBB=null;
					m_kba.removeHistoryDDBBListener(this);
					sessionOperationDDBB=null;
				}
			}
		} catch (Exception e) {
				e.printStackTrace();
				m_com.logError(dialog.getComponent(),e,"Error al intentar asignar el individuo creado");
		}
	}


//	@Override
//	public Integer newRowTable(String id) throws AssignValueException, NotValidValueException {
//		Integer ido=super.newRowTable(id);
//		m_session.setIDMadre(m_kba.getDDBBSession().getID());
//		idTableEditionToDDBB=new IdObjectForm(id);
//		m_kba.addHistoryDDBBListener(this);//Para enterarme de cuando se almacene en base de datos y poder mostrarlo en la tabla
//		return ido;
//	}


	@Override
	protected void setRelationTable(String idTable, int idoRow, int idtoRow, Session sessionParent, Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException, ParseException, EditionTableException, NumberFormatException, AssignValueException {
		IdObjectForm idObjectForm=new IdObjectForm(idTable);
		Integer ido=idObjectForm.getIdo();
		Integer idProp=idObjectForm.getIdProp();
		Integer valueCls=idObjectForm.getValueCls();
		
		ObjectProperty property=m_kba.getChild(ido, m_kba.getClass(ido), idProp, m_userRol, m_idtoUserTask, session);
		int idRange=m_kba.getIdRange(property);
		boolean structural=m_kba.getCategoryProperty(idProp).isStructural();
		AccessAdapter tableAccess=new AccessAdapter(m_kba.getAllAccessIndividual(idRange, m_userRol, property.getTypeAccess(), m_idtoUserTask),property,structural,false);
		//System.err.println("TableAccessssssssssss");
		boolean link=true;
		Iterator<Integer> itr=tableAccess.getUserTasksAccess(AccessAdapter.NEW_AND_REL).iterator();
		while(itr.hasNext() && link){
			Integer idtoUserTask=itr.next();
			//System.err.println("idtoUserTASKKKKKKKKKKKKKKKK:"+idtoUserTask);
			if(!Auxiliar.equals(idtoUserTask,m_idtoUserTask)){
				Object[] options = {"Sí", "No"};
				int res;
				res = Singleton.getInstance().getMessagesControl().showOptionMessage("La creación de "+m_kba.getLabelClass(valueCls, m_idtoUserTask)+" será almacenada directamente en base de datos aunque cancele el formulario actual.\n¿Desea continuar?",
							Utils.normalizeLabel("AVISO DE CREACIÓN DIRECTA"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[0],dialog.getComponent());

				if (res == JOptionPane.NO_OPTION){
					EditionTableException ex=new EditionTableException("",false,null);
					throw ex;
				}
				link=false;
				instance inst=m_kba.getTreeObjectTable(idoRow, idtoRow, idTable, m_formManager.getColumnTreeOfTable(idTable), m_userRol, idtoUserTask, session);
				selectData selectD=new selectData();
				selectD.addInstance(inst);
				m_formManager.addRows(idTable, selectD, true);
				sessionParent.setIDMadre(m_kba.getDDBBSession().getID());
				sessionOperationDDBB=sessionParent;
				idTableEditionToDDBB=idObjectForm;
				//m_kba.addHistoryDDBBListener(this);//Para enterarme de cuando se almacene en base de datos y poder mostrarlo en la tabla
				if(!m_kba.isGlobalUtask(m_idtoUserTask)) m_kba.addHistoryDDBBListener(this);
			}
		}
		if(link)
			super.setRelationTable(idTable, idoRow, idtoRow, sessionParent, session);
	}

	@Override
	public void cancelEditionTable(String idTable,Integer idoRow) throws EditionTableException, AssignValueException{
		super.cancelEditionTable(idTable, idoRow);
		//System.err.println("sessionOperationDB:"+sessionOperationDDBB+" idTableEditionToDDBB");
		if(sessionOperationDDBB!=null){//Si se trata de la creacion directa a base de datos tenemos que quitar la fila añadida en la tabla
			GIdRow idRow=m_formManager.getDataTableFromIdo(idTableEditionToDDBB.getIdString(),idoRow);
			if(idRow!=null)
				m_formManager.delRow(idTableEditionToDDBB.getIdString(), idRow);

			sessionOperationDDBB=null;
			idTableEditionToDDBB=null;
		}
	}


	public boolean hasUserModified() {
		return hasUserModified;
	}

	@Override
	public Session exeActions(ArrayList<commandPath> commandList,ITableNavigation tableNavigation,WindowComponent dialog) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		boolean success=false;
		Session sess=null;
		try{
			sess=super.exeActions(commandList, tableNavigation, dialog);
			success=true;
		}finally{
			//Si abrimos un formulario marcamos hasUserModified a true aunque luego puede que no se hagan cambios
			if(success &&  m_operation!= access.VIEW)
				hasUserModified=true;
		}
		return sess;
	}

}
