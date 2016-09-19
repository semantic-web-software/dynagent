package dynagent.gui.forms;

import gdev.gawt.utils.GFocusTraversalPolicy;
import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GProcessedForm;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.Singleton;
import dynagent.gui.StatusBar;
import dynagent.gui.WindowComponent;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.ExportCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetCommonCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewCommonCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.builders.FormManager;
import dynagent.gui.forms.builders.formFactory;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class TableForm extends FormControl implements ActionListener{

	private Dimension m_preferredSize;
	
	private JPanel m_form;
	//private HashMap<String,Integer> m_idRangeRdnMap;
	private IUserMessageListener m_control;
	private ObjectProperty m_property;
	private int m_operation = 0;
	//private ActionManager m_actionManager;
	private FormManager m_formManager;
	private int m_idtoParent;
	private int m_idtoRange;
	private String idTable;
	boolean hasSystemModified;
	private boolean structural;

	/*listAddTableChangeValue es utilizado para almacenar los avisos de changeValue que se reciben sobre tablas y luego procesarlos juntos
	 Almacena como clave el Id de la tabla, y como valor un mapa de idos e idtos*/
	public HashMap<String,HashMap<Integer,Integer>> listAddTableChangeValue;
	public HashMap<String,HashMap<Integer,GIdRow>> listDelTableChangeValue;
	
	HashMap<Integer,ArrayList<Integer>> mapDirectReports;//Mapa IdtoTargetClass,ArrayList de idtoDirectReport
	public TableForm(Session ses,ObjectProperty property,HashMap<Integer,Integer> rows, IUserMessageListener control, Integer idtoUserTask, Integer userRol, int operation,
			Dimension dim, JPanel botoneraTabla, JPanel botoneraForm, boolean selectionMode, KnowledgeBaseAdapter kba,WindowComponent dialog, MouseListener mouseListener,HashMap<Integer,ArrayList<Integer>> mapDirectReports) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		super(kba,dialog,idtoUserTask,userRol,ses,property.getIdo(),property.getIdto());
	
		m_preferredSize=dim;
		//m_kba=Singleton.getInstance().getKnowledgeBase();
		m_idtoParent=property.getIdto();
		m_session.addIMessageListener(m_actionManager);
		m_control=control;
		m_property=property;
		m_operation=operation;
		//m_actionManager=Singleton.getInstance().getActionManager();
		m_idtoRange=m_kba.getClass(m_kba.getIdRange(property));
		structural=m_kba.getCategoryProperty(property.getIdProp()).isStructural();
		

		//Si está enganchado por una targetClass nos interesa que se comporte como una estructural para que permita la edicion y aparezcan todas las columnas. Esto tambien se ha tenido que hacer en FormFactory
		if(property.getIdProp().equals(Constants.IdPROP_TARGETCLASS))
			structural=true;
				
		this.mapDirectReports=mapDirectReports;
		
		IdObjectForm idObjectForm=new IdObjectForm();
		idObjectForm.setIdo(m_property.getIdo());
		idObjectForm.setIdProp(m_property.getIdProp());
		/*					idObjectForm.setIdtoUserTask(idtoUserTask);*/
		idObjectForm.setValueCls(m_idtoRange);
		idTable=idObjectForm.getIdString();
		
		build(rows,botoneraTabla,botoneraForm,selectionMode, mouseListener, operation);
		
		if(idtoUserTask!=null && m_kba.isSpecialized(idtoUserTask,Constants.IDTO_ACTION))
			hasSystemModified=true;
		
		m_session.addIchangeProperty(this,true);//Se hace al final porque hasta que no este construido no tiene sentido que cambie valores graficos		
	}

	private void build(HashMap<Integer,Integer> rows,JPanel botoneraTabla,JPanel botonera, boolean selectionMode, MouseListener mouseListener, int operation) throws NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, OperationNotPermitedException{
		m_form = new JPanel();
		m_form.setLayout(new BorderLayout());		

		boolean filterMode=selectionMode;
		
		int value=m_kba.getIdRange(m_property);
		int widthMin=-1;
		int heightBotoneras=0;
		if(botoneraTabla!=null)
			heightBotoneras+=botoneraTabla.getPreferredSize().getHeight();
		if(botonera!=null){
			heightBotoneras+=botonera.getPreferredSize().getHeight();
			widthMin=botonera.getPreferredSize().width;
		}
		if(botoneraTabla!=null&&botonera!=null){
			heightBotoneras+=40; //alto en estos casos (como target class) no parece tener en cuenta margenes de ventana
		}
		
		Dimension dim=new Dimension((int)m_preferredSize.getWidth(),(int)m_preferredSize.getHeight()-heightBotoneras);
		GProcessedForm viewForm=formFactory.buildPersonalTable(m_kba,m_idtoUserTask,m_property,value,m_userRol,/*new access(access.VIEW)*/m_property.getTypeAccess(),dim,idTable,filterMode,selectionMode,m_session,false,widthMin,isAllowedConfigTables());
		
		ArrayList<GProcessedForm> listaViewForm=new ArrayList<GProcessedForm>();
		listaViewForm.add(viewForm);

		m_formManager= new FormManager(
				this,
				this,
				this,
				0,
				"TABLE_FORM",
				listaViewForm,
				(operation ==  access.NEW ),
				filterMode,
				(operation ==  access.VIEW ),
				true,
				true,
				null, ConstantesGraficas.dimInit, m_kba, dialog,
				m_idtoUserTask,m_userRol,m_session);

		//formManager.addTableFocusListener( idTable, new TableFocusManager( this, PANEL_RESULTADOS ) );
		if(mouseListener!=null)
			m_formManager.addTableMouseListener( idTable, mouseListener);	
		
//		boolean filterMode=selectionMode;
//		rp = new resultPanel(m_session, false, false, selectionMode, m_idtoUserTask,m_property,m_kba.getIdRange(m_property), m_userRol,m_preferredSize,mouseListener, filterMode, -1, null);
		selectData instRows=new selectData();			
		Iterator<Integer> itr=rows.keySet().iterator();
		while(itr.hasNext()){
			int ido=itr.next();
			int idto=rows.get(ido);
			instance instRange;
			if(selectionMode)//Si es selectionMode no nos interesa construir las columnsProperties ya que solo sale el tipo de la clase
				instRange=new instance(m_kba.getClass(ido),ido);
			else instRange=m_kba.getTreeObjectTable(ido,idto,idTable,m_formManager.getColumnTreeOfTable(idTable),m_userRol, m_idtoUserTask, m_session);//new instance(m_kba.getClass(idRange),idRange);
			instRows.addInstance(instRange);
		}	
		
		m_formManager.setRows(idTable,instRows);
		m_formManager.orderRows(idTable);

		JPanel panelResult=new JPanel();
		
		if(botoneraTabla!=null){
			panelResult.setLayout(new BorderLayout());
			JPanel panelBotonera=new JPanel();
			panelBotonera.setLayout(new FlowLayout(FlowLayout.RIGHT,0,0));
			panelBotonera.add(botoneraTabla);
			panelResult.add(panelBotonera,BorderLayout.NORTH);
			panelResult.add(m_formManager.getComponent(), BorderLayout.SOUTH);			
		}else{
			panelResult.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
			panelResult.add(m_formManager.getComponent());			
		}
		m_form.add(panelResult,BorderLayout.NORTH);
		
		int borderY = 3;
		JPanel panelAux=new JPanel();
		panelAux.add(botonera);
		
		if(m_formManager.isHasScroll())
			panelAux.setBorder(BorderFactory.createMatteBorder(borderY, 0, 0, 0, Color.white));
		
		m_form.add(panelAux);
		
		if(dialog!=null/* && !selectionMode*/){
			dialog.setFocusTraversalPolicy(new GFocusTraversalPolicy(m_form,dialog.getComponent(),m_formManager.getViewPort(),m_formManager.getFormComponents(),m_kba.getServer(),this,Singleton.getInstance().getMessagesControl(),selectionMode,true));
		}
	}

	public JComponent getComponent() {
		return m_form;
	}

	public LinkedHashMap<Integer,Integer> getResult() {
		LinkedHashMap<Integer,Integer> result=new LinkedHashMap<Integer,Integer>();
		Iterator<GIdRow> itr=m_formManager.getIdRowsSelectionData(idTable).iterator();
		while(itr.hasNext()){
			GIdRow idRow=itr.next();
			int idRange=idRow.getIdo();
			result.put(idRange,idRow.getIdto());
		}
		return result;
	}

	public void actionPerformed(ActionEvent e) {
		try{
			dialog.disabledEvents();
			//System.out.println("CPLEX EVENTO FILTER CONTROL");
			
			String command = e.getActionCommand();
			//System.out.println("OPGUI:COMMAND:" + command);
			IdOperationForm idOperation = new IdOperationForm(command);
			Integer operationType = idOperation.getOperationType();
			Integer buttonType = idOperation.getButtonType();

			IdObjectForm target = idOperation.getTarget();
			Integer idProp = target.getIdProp();
			Integer idObject = target.getIdo();
			Integer idtoUserTask = target.getIdtoUserTask();
			Integer value= target.getValueCls();

//			No nos sirve mostrar nada en la barra ya que el formulario de abajo no cambia al ser una imagen
//			Object boton = e.getSource();
//			if (boton instanceof AbstractButton) {
//				if (((AbstractButton) boton).getToolTipText() != null)
//					Singleton.getInstance().getStatusBar().setLocalizacion(
//							((AbstractButton) boton).getToolTipText(),
//							Singleton.getInstance().getStatusBar().getNivelLocalizacion() + 1);
//			}
//
//			if (buttonType == botoneraAccion.ABRIR)
//				Singleton.getInstance().getStatusBar().setBarraProgreso();

			final Integer operationTypeThis = operationType;
			final Integer idObjectThis = idObject;
			final Integer idPropThis = idProp;
			final Integer idtoUserTaskThis = idtoUserTask;
			final Integer valueThis=value;
			final int buttonTypeThis = buttonType;

			SwingWorker worker = new SwingWorker() {
				public Object construct() {
					boolean success=false;
					try{
						Object result=doWorkEvent(operationTypeThis, idObjectThis, idPropThis, valueThis,
								idtoUserTaskThis, buttonTypeThis);
						success=true;
						return result;
					}finally{
						if(!success)
							doFinished();
					}
				}

				public void finished() {
					StatusBar statusBar = Singleton.getInstance().getStatusBar();
					if (statusBar.hasBarraProgreso())
						statusBar.setFinishBarraProgreso((String) getValue(), true);
					else if (!statusBar.isError())
						statusBar.setAccion((String) getValue());
					dialog.enabledEvents();
				}
			};
			worker.start();
			
		} catch (Exception ex) {
			
			Singleton.getInstance().getComm().logError(dialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}

	private synchronized Object doWorkEvent(Integer operationType, Integer idObject,Integer idProp, Integer value, Integer idtoUserTask, int buttonType) {
		try {
			String mensajeRespuesta = "";

			if (buttonType == botoneraAccion.CANCEL) {
				cancel();
				mensajeRespuesta="Formulario cerrado";
			}else if(buttonType == botoneraAccion.EJECUTAR){
				mensajeRespuesta=confirm();
			}else if(buttonType == botoneraAccion.CONSULTAR){
				consultEdit(buttonType,idtoUserTask);
			}else if(buttonType == botoneraAccion.EDITAR){
				mensajeRespuesta=consultEdit(buttonType,idtoUserTask);
			}else if(buttonType == botoneraAccion.ELIMINAR || buttonType == botoneraAccion.UNLINK){
				mensajeRespuesta=delete(idtoUserTask);
			}else if (buttonType == botoneraAccion.HELP){
				//Singleton.getInstance().getMessagesControl().showMessage("Este tipo de objeto es un "+m_kba.getLabelClass(m_idto,idtoUserTask));
				Singleton.getInstance().getHelpComponent().showHelp(m_kba.getClass(value),idtoUserTask,m_userRol,dialog.getComponent());
			}else if (buttonType == botoneraAccion.ACTION || buttonType == botoneraAccion.EXPORT){
				final HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
				final HashSet<Integer> idoValues=new HashSet<Integer>(m_kba.getIdoValues(m_property));
				IHistoryDDBBListener newIdoListener=new IHistoryDDBBListener(){

					public void initChangeHistory(){}
					
					public void endChangeHistory(){}
					
					public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
						//System.err.println("ido:"+ido+" operation:"+operation+" idtoUserTask:"+idtoUserTask+" sessionUsed:"+sessionUsed+" oldIdo:"+oldIdo+" rdn:"+rdn+" this:"+this.hashCode());
						
						if(idoValues.contains(oldIdo) || idoValues.contains(ido)){
							values.put(ido, idto);
						}
					}
				};
				
				Iterator<GIdRow> itr=m_formManager.getIdRowsSelectionData(idTable).iterator();
				while(itr.hasNext()){
					GIdRow idRow=itr.next();
					if(!Constants.isIDTemporal(idRow.getIdo()))
						values.put(idRow.getIdo(),idRow.getIdto());
				}
				
				m_kba.addHistoryDDBBListener(newIdoListener);
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
					
//					Iterator<GIdRow> itr=parList.iterator();
//					while(itr.hasNext()){
//						GIdRow idRow=itr.next();
//						values.put(idRow.getIdo(),idRow.getIdto());
//					}
					
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
			return mensajeRespuesta;
		}catch(IncompatibleValueException e){
			m_control.showErrorMessage(e.getUserMessage(),dialog.getComponent());
			return null;
		}catch(CardinalityExceedException e){
			Property prop=e.getProp();
			//e.printStackTrace();
			String message=e.getUserMessage();
			if (prop!=null){
				try{
					if (prop.getIdo()!=m_ido){
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask);
						String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(),prop.getIdto(), m_userRol, m_idtoUserTask, m_session));
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
								Singleton.getInstance().getComm().logError(dialog.getComponent(),exc, "Error al intentar editar fila en formulario");
								return null;
							}
						}
					}else{
						message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
						Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
					}
				}catch(Exception ex){
					e.printStackTrace();
					Singleton.getInstance().getComm().logError(dialog.getComponent(),e,/*"Error al intentar mostrar mensaje de cardinalidad"*/null);
				}
				
			}else{
				Singleton.getInstance().getMessagesControl().showErrorMessage(message,dialog.getComponent());
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			Singleton.getInstance().getComm().logError(dialog.getComponent(),e,"Error al ejecutar la operación");
			return "Error de la operación";
		}
	}

	public boolean cancel() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
		if(hasSystemModified){
			Object[] options = {"Sí", "No"};
			int res;
			res = Singleton.getInstance().getMessagesControl().showOptionMessage("¿Está seguro que desea cancelar?",
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
		
		m_session.removeIchangeProperty(this);
		//m_session.removeIMessageListener(m_actionManager);
		m_session.rollBack();
		if(m_sessionMaster!=m_session)//Esto ocurre si se estaba editando una fila y se pulsa sobre la x de la ventana
			m_sessionMaster.rollBack();
		m_actionManager.closeForm(dialog,m_kba,false);
		
		return true;
	}

	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{

		DefaultSession sessionConfirm=m_kba.createDefaultSession(m_session,m_idtoUserTask,m_session.isCheckCoherenceObjects(),m_session.isRunRules(),m_session.isLockObjects(),m_session.isDeleteFilters(),true);
		sessionConfirm.addIchangeProperty(this, false);
		boolean success=false;
		
		String mensajeRespuesta=null;
		//System.out.println("Ejecutar: Sesion"+m_session.getID()+" Padre:"+m_session.getIDMadre());
		boolean isRight=true;
		boolean setUserTaskState=false;
		IHistoryDDBBListener historyListener=null;
		int idoUserTask=-1;
		try{
			if(m_idtoUserTask!=null){
				setUserTaskState=m_property.getIdProp()==Constants.IdPROP_TARGETCLASS;
				if(m_kba.isSpecialized(m_idtoUserTask, Constants.IDTO_ACTION))
					idoUserTask=m_kba.getIdoUserTaskAction(m_idtoUserTask);
				else if(m_kba.isSpecialized(m_idtoUserTask, Constants.IDTO_IMPORT))
					idoUserTask=m_kba.getIdoUserTaskImport(m_idtoUserTask);
				/*else if ( m_kba.isSpecialized(m_idtoUserTask, Constants.IDTO_EXPORT))//Aqui nunca llega con export
					idoUserTask=m_kba.getIdoUserTaskExport(m_idtoUserTask);*/
				else idoUserTask=m_kba.getIdoUserTask(m_idtoUserTask);
			}
			
			//Si el ido que hemos modificado es del tipo de la targetClass de la userTask le asignamos a esta el valor
			//de realizado para que se lancen las reglas que verifiquen los datos
			if(setUserTaskState){
				try{
					//m_kba.setState(idoUserTask,m_idtoUserTask,Constants.INDIVIDUAL_PREREALIZADO,m_userRol,m_idtoUserTask,sessionConfirm);
					m_kba.setState(idoUserTask,m_idtoUserTask,Constants.INDIVIDUAL_REALIZADO,m_userRol,m_idtoUserTask,sessionConfirm);
				}catch(OperationNotPermitedException e){
					Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),dialog.getComponent());
					isRight=false;
				}/*catch(Exception ex){
					ex.printStackTrace();
					m_control.showMessage("El individuo no está completado correctamente");
					isRight=false;
				}*/
				//isRight=setUserTaskState(idoUserTask,Constants.IDO_REALIZADO,sessionConfirm);
			}
	
			if(isRight){  
				if(mapDirectReports!=null && !mapDirectReports.isEmpty()){
					final HashSet<Integer> idoValues=new HashSet<Integer>(m_kba.getIdoValues(m_property));
					historyListener=new IHistoryDDBBListener(){
	
						HashMap<Integer,HashMap<Integer,Integer>> mapIdtoIdoForPrint;
						HashSet<Integer> newIdos=new HashSet<Integer>();
						public void initChangeHistory(){
							mapIdtoIdoForPrint=new HashMap<Integer, HashMap<Integer,Integer>>();
						}
						
						public void changeHistory(int ido, int idto, String rdn, int oldIdo, int operation, Integer idtoUserTask, Session sessionUsed) {
							//System.err.println("ido:"+ido+" operation:"+operation+" idtoUserTask:"+idtoUserTask+" sessionUsed:"+sessionUsed+" oldIdo:"+oldIdo+" rdn:"+rdn+" this:"+this.hashCode());
							if(mapDirectReports.containsKey(idto) && (idoValues.contains(oldIdo) || idoValues.contains(ido))){
								if(!mapIdtoIdoForPrint.containsKey(idto))
									mapIdtoIdoForPrint.put(idto, new HashMap<Integer,Integer>());
								mapIdtoIdoForPrint.get(idto).put(ido,idto);
								if(oldIdo<0) newIdos.add(ido);
							}
							
						}
						
						public void endChangeHistory(){
							if(!mapIdtoIdoForPrint.isEmpty()){
								for(int idto:mapIdtoIdoForPrint.keySet()){
									try{
										for(int idtoUserTaskReport:mapDirectReports.get(idto)){
											int idoUserTaskReport=m_kba.getIdoUserTaskReport(idtoUserTaskReport);
											m_kba.print(mapIdtoIdoForPrint.get(idto),newIdos,idoUserTaskReport, idtoUserTaskReport, m_userRol, m_idtoUserTask);
										}
									}catch (Exception e) {
										e.printStackTrace();
										try{
											Singleton.getInstance().getComm().logError(dialog.getParentDialog().getComponent(),e, "Error al intentar imprimir "+m_kba.getLabelClass(idto, m_idtoUserTask));
										}catch(Exception ex){
											Singleton.getInstance().getComm().logError(dialog.getParentDialog().getComponent(),e, "Error al intentar imprimir "+mapIdtoIdoForPrint.get(idto));
										}
									} catch (NoClassDefFoundError e){
										e.printStackTrace();
										try{
											Singleton.getInstance().getComm().logError(dialog.getParentDialog().getComponent(),null, "Error al intentar imprimir "+m_kba.getLabelClass(idto, m_idtoUserTask));
										}catch(Exception ex){
											Singleton.getInstance().getComm().logError(dialog.getParentDialog().getComponent(),null, "Error al intentar imprimir "+mapIdtoIdoForPrint.get(idto));
										}
									}
								}
							}
						}
					};
					m_kba.addHistoryDDBBListener(historyListener);
				}
				boolean exito=false;
//				try{
					exito=sessionConfirm.commit();
					success=true;
//				}catch(IncompatibleValueException e){
//					m_control.showErrorMessage(e.getUserMessage());
//					if(setUserTaskState)
//						setUserTaskState(idoUserTask,Constants.IDO_PENDIENTE,m_session);
//					return null;
//				}catch(CardinalityExceedException e){
//					Property prop=e.getProp();
//					e.printStackTrace();
//					String message=e.getUserMessage();
//					if (prop!=null){
//						try{
//							if (prop.getIdo()!=m_ido){
//								message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), m_idtoUserTask);
//								String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(), m_userRol, m_idtoUserTask, m_session));
//								if(rdn!=null)
//									message+=" '"+rdn+"'";
//								
//								Object[] options = {"Aceptar", "Cancelar"};
//								
//								int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
//										message+"\n¿Desea editarlo abriendo su formulario?",
//										"Campo no relleno",
//										JOptionPane.YES_NO_OPTION,
//										JOptionPane.WARNING_MESSAGE,
//										null,
//										options,
//										options[0]);
//	
//								if(res == JOptionPane.YES_OPTION){
//									try{
//										editInForm(m_ido, prop.getIdo());
//									} catch (EditionTableException exc) {
//										Singleton.getInstance().getComm().logError(exc, "Error al intentar editar fila en formulario");
//										return null;
//									}
//								}
//							}else{
//								message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), m_idtoUserTask);
//								Singleton.getInstance().getMessagesControl().showErrorMessage(message);
//							}
//						}catch(Exception ex){
//							Singleton.getInstance().getComm().logError(e,/*"Error al intentar mostrar mensaje de cardinalidad"*/null);
//							e.printStackTrace();
//						}
//						
//					}else{
//						Singleton.getInstance().getMessagesControl().showErrorMessage(message);
//					}
//					
//					if(setUserTaskState)
//						setUserTaskState(idoUserTask,Constants.IDO_PENDIENTE,m_session);
//					return null;
//				}
	
				if(exito){
	//				if(setUserTaskState)
	//					setUserTaskState(idoUserTask,Constants.IDO_PENDIENTE,m_kba.getDefaultSession());
					
					m_actionManager.closeForm(dialog,m_kba,true);
					mensajeRespuesta="Modificación realizada";
				}else{
					m_actionManager.closeForm(dialog,m_kba,true);
					mensajeRespuesta="Ningún campo modificado";
				}
				
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

//	private boolean setUserTaskState(int idoUserTask,int value,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
//		int valueCls=Constants.IDTO_ESTADOREALIZACION;
//		int idProp=Constants.IdPROP_ESTADOREALIZACION;
//		ObjectProperty property=m_kba.getChild(idoUserTask, m_idtoUserTask, idProp, m_userRol, m_idtoUserTask, ses);
//		Integer valueIdo=m_kba.getIdoValue(property);
//		Integer valueOld=valueIdo;
//		Integer valueNew=value;
//		try{
//			m_kba.setValue(idoUserTask,idProp, m_kba.buildValue(valueNew,valueCls),m_kba.buildValue(valueOld,valueCls),m_userRol,m_idtoUserTask,ses);
//		} catch (Exception e) {//TODO Aqui habria que diferenciar entre la excepcion de completado incorrectamente y la de fallos del sistema
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}

	private String consultEdit(int type,Integer idtoUserTask) throws HeadlessException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException, AWTException, ActionException{

		Session sessionParentAction=idtoUserTask==null || idtoUserTask.intValue()==m_idtoUserTask?m_session:m_kba.getDDBBSession();
		if (m_formManager.selectionIsGroup(idTable))
			m_control.showMessage("Debe seleccionar un elemento desglosado.",dialog.getComponent());
		ArrayList<GIdRow> parSelected = m_formManager.getIdRowsSelectionData(idTable);

		if (parSelected.isEmpty()) {
			m_control.showMessage("Para consultar/editar un objeto debe seleccionar al menos un registro",dialog.getComponent());
			return null;
		}
		/*if (!parSelected.hasData()) {
			m_control.showMessage("Para consultar/editar un objeto debe seleccionar un único registro");
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

			if(type==botoneraAccion.EDITAR)                        			
				commandPath=new SetCommonCommandPath(m_ido,listIdo, m_idtoUserTask, m_userRol,sessionParentAction);        						
			else if(type==botoneraAccion.CONSULTAR)                        			
				commandPath=new ViewCommonCommandPath(m_ido,listIdo, m_idtoUserTask, m_userRol,sessionParentAction);
			commandList.add(commandPath);
			exeActions(commandList,null,dialog);

		}else{
			GIdRow rowSelected= parSelected.iterator().next();
			commandPath commandPath=null;
			if(type==botoneraAccion.CONSULTAR)
				commandPath=new ViewCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),m_idtoUserTask,m_userRol,sessionParentAction);
			else if(type==botoneraAccion.EDITAR)   
				commandPath=new SetCommandPath(m_ido,m_idto,rowSelected.getIdo(),rowSelected.getIdto(),m_idtoUserTask,m_userRol,sessionParentAction);
			commandList.add(commandPath);
			exeActions(commandList,new ITableNavigation(){

				public boolean hasNextRow() {
					return m_formManager.getNextRow(idTable)!=null;
				}

				public boolean hasPrevRow() {
					return m_formManager.getPrevRow(idTable)!=null;
				}

				public GIdRow nextRow() {
					GIdRow idRow=m_formManager.getNextRow(idTable).getIdRow();
					if(idRow!=null)
						m_formManager.selectRow(idTable, idRow.getIdo());
					return idRow;
				}

				public GIdRow prevRow() {
					GIdRow idRow=m_formManager.getPrevRow(idTable).getIdRow();
					if(idRow!=null)
						m_formManager.selectRow(idTable, idRow.getIdo());
					return idRow;
				}
				
			},dialog);
		}
		return "";
	}

	private String delete(Integer idtoUserTask) throws NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, AssignValueException{

		if (m_property.getIdProp() == 0) {
			m_actionManager.closeForm(dialog,m_kba,false);
			return "";
		}
		if (m_formManager.selectionIsGroup(idTable)) {

			m_control.showMessage("Debe seleccionar un elemento desglosado",dialog.getComponent());
			return null;
		}
		ArrayList<GIdRow> objSelected = m_formManager.getIdRowsSelectionData(idTable);

		if (objSelected.isEmpty()) {
			m_control.showMessage("Para eliminar un objeto debe seleccionar al menos un registro",dialog.getComponent());
			return null;
		}
		boolean delete=false;
		Object[] options = {"Sí", "No"};
		int res;
		res = m_control.showOptionMessage(
				"¿Está seguro que desea borrar "+(objSelected.size()>1?"los elementos":"el elemento")+"?",
				Utils.normalizeLabel("CONFIRMACION BORRADO"),
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE,
				null,
				options,
				options[1],dialog.getComponent());

		if (res != JOptionPane.YES_OPTION) return "Eliminación de registro/s cancelada";
		removeElementRelationed(idTable, objSelected, delete, idtoUserTask);

		return "Registro desvinculado y eliminado";		
	}

	protected void removeElementRelationed(String tableIndex, ArrayList<GIdRow> lista, boolean delete, Integer idtoUserTask) throws NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, AssignValueException {
		if (lista == null || lista.size() == 0)
			return;
		Iterator itr= lista.iterator();
		while(itr.hasNext()){
			GIdRow obj=(GIdRow)itr.next();
			if(delete)
				m_kba.deleteObject(obj.getIdo(),obj.getIdto(),obj.getRdn(),m_userRol,idtoUserTask,m_session);
			else{
				setValue(tableIndex, null, obj.getIdo(), obj.getIdto(), obj.getIdto());
			}
		}
		if (!lista.isEmpty()) m_formManager.delRows(tableIndex,lista);
	}

	public void initChangeValue() {
		listAddTableChangeValue=new HashMap<String, HashMap<Integer,Integer>>();
		listDelTableChangeValue=new HashMap<String, HashMap<Integer,GIdRow>>();
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int level, int operation) throws ParseException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException {

			/*ido==m_ido cuando se borra o se añade una fila ya que si es el targetClass es filtro*/
			if((level!=Constants.LEVEL_FILTER || ido==m_ido) && m_session.getState()==Session.USE_STATE){
				//System.err.println("Cambia valor en ido:"+ido+" idProp:"+idProp+" siendo la operacion:"+operation+" valueCls:"+valueCls+" el value:"+value+" y el valueold:"+valueOld);
				
				IdObjectForm idObjForm=new IdObjectForm();
				idObjForm.setIdo(ido);
				idObjForm.setIdProp(idProp);
				idObjForm.setValueCls(valueCls);
	
				String idString=idObjForm.getIdString();
	
				if(value instanceof DataValue || valueOld instanceof DataValue){
					//Se trataria del registro de una de las tablas
					//System.err.println("ChangeValue:DataValue buscando en todas las tablas");
					ArrayList<String> listTables=m_formManager.getIdTables();
					//System.err.println("Esta es la lista de ids de tablas:"+listTables);
					//System.err.println("id a buscar:"+idString);
					Iterator<String> itr=listTables.iterator();
					while(itr.hasNext()){
						String idTable=itr.next();
						int numRow=m_formManager.getRowCount(idTable);
						for(int i=0;i<numRow;i++){
							RowItem rowItem=m_formManager.getCompletedDataTableFromIndex(idTable, i);
							if(rowItem.containsIdo(ido) && m_kba.isLoad(rowItem.getIdRow().getIdo()/*Podria no estar cargado ya en motor y quitarse en un aviso despues*/)){//Si contiene el ido significa que en la tabla se muestra ese individuo en una columna
									//selectData selectData=new selectData();
	
									/*---------Gestion del cambio:Modificacion del instance existente en la tabla-------------*/
//									selectData.addInstance(inst);
//									//System.err.println("instanceTable:"+inst);
//									Object newValue=m_kba.getValueData((DataValue)value);
//									Object oldValue=m_kba.getValueData((DataValue)valueOld);//m_kba.getValueData(property);//TODO En la property no es buena idea mirar ya que elecom cuando cambia un valor no modifica el instance
//									//System.err.println("newValue:"+newValue+" oldValue:"+oldValue);
//									if((newValue!=null && oldValue!=null && !newValue.equals(oldValue)) || (newValue==null && oldValue!=null) || (newValue!=null && oldValue==null)){
//										setValue(idString,newValue,oldValue,valueCls,valueCls);//TODO valueOldCls deberia ser pasado en este metodo, ya que no tienen porque ser el mismo, podrian ser hermanos
//										/*if(operation==action.DEL)
//											m_formManager.delRows(idTable, selectData);
//										else*/ m_formManager.addRows(idTable, selectData, true);
//									}
									
								if(!containsInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo())){
										/*-----------Gestion del cambio:Creacion de un nuevo instance que es el que sustituira al de la tabla-----------*/
										//selectData.addInstance(m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(), m_idtoRange,m_idtoParent, m_userRol, m_idtoUserTask,m_session,false,structural,false));
										//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto(), idTable,m_formManager.getColumnTreeOfTable(idTable),m_userRol, m_idtoUserTask,m_session);
										//System.out.println("registro tabla:"+selectData.getFirst());
										/*if(operation==action.DEL)
											m_formManager.delRows(idTable, selectData);
										else *///m_formManager.addRows(idTable, selectData, true);
										addInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto());
								}
							}
						}
					}
				}else{
					
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
								/*ObjectProperty objectP=m_kba.getChild(ido, idProp, m_userRol, m_idtoUserTask, m_session);
								int idtoRange=m_kba.getClass(m_kba.getIdRange(objectP));*/
								//selectDataAdd.addInstance(m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),/*idtoRange*/m_idtoRange,idto, m_userRol, m_idtoUserTask,m_session,false,structural,false));
								//instance inst=m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),((ObjectValue)value).getValueCls(),idString,m_formManager.getColumnTreeOfTable(idString), m_userRol, m_idtoUserTask,m_session);
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
									//selectDataAdd.addInstance(m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),/*idtoRange*/m_idtoRange,idto, m_userRol, m_idtoUserTask,m_session,false,structural,false));
									//instance inst=m_kba.getTreeObjectTable(((ObjectValue)value).getValue(),((ObjectValue)value).getValueCls(),idString,m_formManager.getColumnTreeOfTable(idString), m_userRol, m_idtoUserTask,m_session);
									//m_formManager.addRows(idString, selectDataAdd, true);
									addInListAddTableChangeValue(idString, ((ObjectValue)value).getValue(), ((ObjectValue)value).getValueCls());
								}
							}
						}else{//Se podria tratar del registro de una de las tablas, recorremos todas las tablas buscando ese ido
							//System.err.println("ChangeValue: ObjectValue buscando en todas las tablas");
							//System.out.println("Esta es la lista de ids de tablas:"+listTables);
							//System.out.println("id a buscar:"+idString);
							Iterator<String> itr=listTables.iterator();
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
													//selectDataAdd.addInstance(/*inst*/m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),m_idtoRange,m_idtoParent, m_userRol, m_idtoUserTask,m_session,false,structural,false));
													//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),rowItem.getIdRow().getIdto(),idTable,m_formManager.getColumnTreeOfTable(idTable), m_userRol, m_idtoUserTask,m_session);
													/*m_formManager.delRows(idTable, selectDataRemove);*/
													//m_formManager.addRows(idTable, selectDataAdd, true);
													addInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo(), rowItem.getIdRow().getIdto());
												}
											}
										}
										if(value!=null){
											if(!containsInListAddTableChangeValue(idTable, rowItem.getIdRow().getIdo())){
												//selectData selectDataAdd=new selectData();
												//int idtoRange=m_kba.getClass(new IdObjectForm(idTable).getValueCls());										
												//Creamos un nuevo treeObject ya que si el valor es de base de datos el instance anterior esta obsoleto ya que no tiene los datos de ese individuo
												//Para ellos usamos ido y no value porque se trata de un objectProperty que se le ha añadido al ido del instance de una fila
												//selectDataAdd.addInstance(m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),/*idtoRange*/m_idtoRange,m_idtoParent, m_userRol, m_idtoUserTask,m_session,false,structural,false));
												//instance inst=m_kba.getTreeObjectTable(rowItem.getIdRow().getIdo(),rowItem.getIdRow().getIdto(),idTable,m_formManager.getColumnTreeOfTable(idTable),m_userRol, m_idtoUserTask,m_session);
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
			//m_formManager.setValueField(idString,m_kba.getValueData(value));

	}
	
	public void endChangeValue() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		try{
			
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
			e.printStackTrace();
			Singleton.getInstance().getComm().logError(dialog.getComponent(),e,"Error en un cambio de valor");
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

	public void startEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW && /*m_level*/m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER)
			m_kba.setLockObject(ido, true, sess);
	}
	
	public void stopEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(m_operation !=  access.VIEW && /*m_level*/m_kba.getLevelObject(ido)!=Constants.LEVEL_FILTER)
			m_kba.setLockObject(ido, false, sess);
	}
	
	@Override
	public boolean isAllowedConfigTables(){
		return false;
	}

}
