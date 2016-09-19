package dynagent.gui;

import gdev.gawt.utils.ITableNavigation;
import gdev.gawt.utils.botoneraAccion;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.jdom.Element;
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
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.Domain;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.ExecuteActionRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.common.utils.jdomParser;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.actions.ActionManager;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.AutomaticActionCommandPath;
import dynagent.gui.actions.commands.ExportCommandPath;
import dynagent.gui.actions.commands.ImportCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.NewRelCommandPath;
import dynagent.gui.actions.commands.ReportCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetCommonCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewCommonCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.FormControl;
import dynagent.gui.forms.filterControl;
import dynagent.gui.forms.transitionControl;
import dynagent.gui.forms.utils.ActionException;
import dynagent.gui.forms.utils.Column;
import dynagent.gui.forms.utils.resultPanel;
import dynagent.gui.utils.ColumnsTableComponent;
import dynagent.gui.utils.EmailForm;
import dynagent.gui.utils.MemoryThread;
import dynagent.gui.utils.Target;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.sessions.DefaultSession;


public class OperationGUI implements ActionListener,KeyListener/*,processServer*/,IExecuteActionListener{
	JPanel m_rightPanel;
	/*JDialog m_currForm;*/
	LinkedHashMap<Integer,Target> m_targets= new LinkedHashMap<Integer,Target>();

	//int m_anchoRight=600;
	int m_anchoSelector;
	//int m_ancho;
	Target m_currTargetFilter=null;
	//Dimension m_preferredSize;
	int m_dividerSize=10;
	int m_minSplitLocation=25;
	int m_scrollBarSize=25;

	
	int m_altoBotonera;
	selectData m_userConfig=null;
	HashMap<Integer, Integer> m_ctxMenuMap= new HashMap<Integer, Integer>();

	//appControl m_appControl;
	//HashMap m_indexRow= new HashMap();
	WindowComponent m_controlDialog;
	Integer m_defaultUserRol;
	Integer m_currProcessGUI=null;
	int m_areaFuncional;
	ActionManager m_actionManager;

	KnowledgeBaseAdapter m_kba;

	private class PanelData extends Object{
		JPanel panel;
		String title;
		PanelData(JPanel panel, String title){
			this.panel=panel;
			this.title=title;
		}
	}
	ArrayList<PanelData> m_processPanelPool=new ArrayList<PanelData>();

	public OperationGUI( KnowledgeBaseAdapter kba,WindowComponent controlDialog,
			int idAreaFunc) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{
		

		m_controlDialog= controlDialog;
		m_areaFuncional=idAreaFunc;		
	
		m_actionManager=Singleton.getInstance().getActionManager();
		m_rightPanel= new JPanel();
		m_kba=kba;
		

		m_rightPanel.setBorder( new EmptyBorder( 0,0,0,0 ) );
		FlowLayout fl=(FlowLayout)m_rightPanel.getLayout();
		fl.setHgap(0);
		fl.setVgap(0);
		fl.setAlignment(FlowLayout.CENTER);
		m_anchoSelector=110;		

		build();
		
		m_rightPanel.setPreferredSize(ConstantesGraficas.dimZonaTrabajo);
		Singleton.getInstance().getDebugLog().setEnableDebug(true);
	}
 
	public Integer getIdCurrTarget(){
		if(m_currTargetFilter!=null)
			return m_currTargetFilter.id;
		return null;
	}

	public boolean showTarget(int idTarget) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SQLException, NamingException, JDOMException{
		Target tg=(Target)m_targets.get(new Integer(idTarget));
		//if(m_currTargetFilter!=tg){
			
		
		if( tg!=null ){
			communicator oldcomm=m_kba.getServer();
			communicator comm=m_kba.configServer(tg.idtoUserTask);
			
			if(comm!=null){
				m_currTargetFilter= tg;
				
				if(tg.form==null){//Formulario incrustado
					replaceCreationForm(tg, tg.idtoUserTask, null);
				}
				
				final FormControl form=tg.form;
				
				
				GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO);
				if(!gestor.setVisiblePanels(/*"Conjunto:"+id*/String.valueOf(idTarget), true)){
					//System.out.println(gestor+" "+tg);
	
					form.getComponent().setPreferredSize(ConstantesGraficas.dimZonaTrabajo);
					
					gestor.addPanel(/*"SubFiltro:"+id*/String.valueOf(idTarget), form.getComponent(), 1, form.getComponent().getPreferredSize(), /*"Conjunto:"+id*/String.valueOf(idTarget));
					gestor.setVisiblePanels(/*"Conjunto:"+id*/String.valueOf(idTarget), true);
					
					//System.err.println("TotalPref:"+m_preferredSize);
					//System.err.println("FiltroPref:"+filterControl.getSelector().getPreferredSize());
					//System.err.println("SubfiltroPref:"+filterControl.getSubFilterView().getPreferredSize());
					//System.err.println("PanelResultadosPref:"+panelResultados.getPreferredSize());
					//System.err.println("FiltroMin:"+filterControl.getSelector().getMinimumSize());
					//System.err.println("SubfiltroMin:"+filterControl.getSubFilterView().getMinimumSize());
					//System.err.println("PanelResultadosMin:"+panelResultados.getMinimumSize());
					/*Le asignamos null como tamaño predefinido para que se asigne el tamaño exacto del panel
				   cuando en filterControl le vayamos asignando distintos formularios a m_subFilterView.
				   De esta manera conseguimos que, en el caso de que uno de los paneles sea mas grande que
				   el espacio disponible, no se muestre el scroll para todos los paneles si no lo necesitan*/
					//filterContro.getSubFilterView().setPreferredSize(null);
				}
				requestDefaultFocus(form);
				if(m_kba.isListenerMenu(tg.idtoUserTask) && tg.form instanceof filterControl){
					exeQueryTarget(idTarget);
				}
				return true;
			}else{
				m_kba.setServer(oldcomm);
				Singleton.getInstance().getMessagesControl().showMessage("No se ha podido conectar con la central.\nSi quiere volver a intentarlo debe reiniciar la aplicación.",m_controlDialog.getComponent());
				return false;
			}
		}
		
		return false;
	}
	
	public void exeQueryTarget(int idTarget) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SQLException, NamingException, JDOMException{
		Target tg=(Target)m_targets.get(new Integer(idTarget));
		filterControl f=(filterControl)tg.form;
		f.confirm();
	}
	
	private void requestDefaultFocus(final FormControl form){
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				FocusTraversalPolicy focusTraversal=form.getComponent().getFocusTraversalPolicy();
				if(focusTraversal!=null){
					Component component=focusTraversal.getDefaultComponent(form.getComponent());
					if(!component.requestFocusInWindow()){
						component.requestFocus();
					}
				}
			}
		});
		
	}
	
	/*   public boolean isLive(){return true;}
   public void contextActionEvent(  contextAction pp, ArrayList listaCtxFix )
		throws DataErrorException,RemoteSystemException,CommunicationException{;}
	 */
	private void build() throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{

		/*Iterator itr=Singleton.getInstance().getUserTasks().getUserTasksReadOnly(idAreaFunc);*/
		Iterator<Integer> itr=m_kba.getIdoUserTasks(m_areaFuncional);
		ArrayList<Integer> targetAdded=new ArrayList<Integer>();
		while(itr.hasNext()){

			Integer idoUserTask=(Integer)itr.next();
			int idtoUserTask=m_kba.getIdtoUserTask(idoUserTask);
			
			/*ArrayList<Integer> userRols=m_kba.getUserRols(idoUserTask,m_kba.getDefaultSession());
			Integer userRol=m_kba.getUserRolRestrictive(userRols);*/
			Integer userRol=null;
			
			//Ponemos el communicator correcto
			communicator m_com = m_kba.configServer(idtoUserTask);
			if(m_com==null){
				m_com=Singleton.getInstance().getComm();
				m_kba.setServer(m_com);
			}
			
			ObjectProperty property=m_kba.getTarget(idoUserTask,idtoUserTask, null,m_kba.getDefaultSession());
			//Creamos el filtro del targetClass
			if(m_kba.getKnowledgeBase().isGenericFilter(m_kba.getIdRange(property))){//Si es un filtro generico tenemos que crear el targetClass. No es filtro generico para las usertasks que tienen avisos ya que les hemos creado filtro al arrancar
				m_kba.createRange(idoUserTask,m_kba.getIdtoUserTask(idoUserTask), Constants.IdPROP_TARGETCLASS, m_kba.getIdRange(property), userRol, idtoUserTask, Constants.MAX_DEPTH_SEARCH_FILTERS, m_kba.getDefaultSession());
				property=m_kba.getTarget(idoUserTask,idtoUserTask, null,m_kba.getDefaultSession());//DefaultSession ya que es informacion de inicializacion
			}
			if(property.getRangoList().isEmpty())
				System.err.println("ERROR:La property no tiene rangoList: "+property);
			Integer idTarget=m_kba.getIdRange(property);

			//System.out.println("operationGUI.build:   getTarget(idoUserTask="+idoUserTask);			
			//System.out.println("Target OperationGui:"+property);
			//System.out.println("operationGUI.build:"+property.toString());
			//System.out.println(idTarget+" "+userRol+" "+idtoUserTask);

			int idClassTarget=m_kba.getClass(idTarget);//m_kba.getIdtoFilter(property);
			//if(!targetAdded.contains(idClassTarget)/* && m_kba.getAccessIndividual(idTarget, userRol, idtoUserTask).getViewAccess()*/){
				targetAdded.add(idClassTarget);

				Iterator<Integer> itrIdos=m_kba.getIdoUserTasksReport(idClassTarget,m_areaFuncional);
				HashSet<Integer> reports=new HashSet<Integer>();
				HashSet<Integer> reportsDirectPrint=new HashSet<Integer>();
				while(itrIdos.hasNext()){
					int idoReport=itrIdos.next();
					int idtoReport=m_kba.getIdtoUserTaskReport(idoReport);
					if(m_kba.getAccessIndividual(idoReport, userRol, idtoReport).getViewAccess()){
						reports.add(idtoReport);
						if(m_kba.isDirectPrint(idoReport))
							reportsDirectPrint.add(idtoReport);
					}

				}
				
				itrIdos=m_kba.getIdoUserTasksReportWithParam(idClassTarget,m_areaFuncional);
				while(itrIdos.hasNext()){
					int idoReport=itrIdos.next();
					int idtoReport=m_kba.getIdtoUserTaskReport(idoReport);
					if(m_kba.getAccessIndividual(idoReport, userRol, idtoReport).getViewAccess()){
						reports.add(idtoReport);
					}

				}
				
				itrIdos=m_kba.getIdoUserTasksAction(idClassTarget,m_areaFuncional);
				ArrayList<Integer> actions=new ArrayList<Integer>();
				while(itrIdos.hasNext()){
					int idoAction=itrIdos.next();
					int idtoAction=m_kba.getIdtoUserTaskAction(idoAction);
					
					ObjectProperty propertyTarget=m_kba.getChild(idoAction,idtoAction,Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, m_kba.getDefaultSession());
					int idRangeTarget=m_kba.getIdRange(propertyTarget);
					int idtoRangeTarget=m_kba.getClass(idRangeTarget);
					
					if(m_kba.hasShowAction(idoAction, idtoAction, null, idtoRangeTarget, idtoUserTask, userRol))
						actions.add(idtoAction);
					//else System.err.println("WARNING: La acción "+m_kba.getLabelUserTask(idtoAction)+" ha sido excluida en "+m_kba.getLabelClass(idClassTarget,idtoUserTask)+" por falta de permisos");
				}
				
				itrIdos=m_kba.getIdoUserTasksAction(idClassTarget,true,m_areaFuncional);
				ObjectProperty propertySource=null;
				ArrayList<Integer> creationActions=new ArrayList<Integer>();
				while(itrIdos.hasNext()){
					int idoAction=itrIdos.next();
					int idtoAction=m_kba.getIdtoUserTaskAction(idoAction);
					propertySource=m_kba.getChild(idoAction,idtoAction,Constants.IdPROP_SOURCECLASS, userRol, idtoUserTask, m_kba.getDefaultSession());
					int idRangeSource=m_kba.getIdRange(propertySource);
					int idtoRangeSource=m_kba.getClass(idRangeSource);
					//Si son iguales no interesa lanzar el asistente ya que estamos en el formulario de esa clase y podriamos elegir las acciones directamente
					//Ademas si no hay ninguna userTask que apunte a ese sourceClass no deberiamos darle la posibilidad ya que podría no tener permisos
					if(idtoRangeSource!=idClassTarget && !m_kba.isSpecialized(idClassTarget, idtoRangeSource)/* && !m_kba.getIdtoUserTasks(idtoRange, null, true).isEmpty()*/){
						//int idoRange=m_kba.createPrototype(idRange,Constants.LEVEL_FILTER, userRol, idtoUserTask, m_kba.getDefaultSession());//Tenemos que crear un filtro porque inicialmente no se crea uno para el sourceClass
						//m_kba.setValue(idoAction,Constants.IdPROP_SOURCECLASS, m_kba.getValueOfString(String.valueOf(idoRange),idtoRange),propertySource.getValue(),userRol,idtoUserTask,m_kba.getDefaultSession());	
						ObjectProperty propertyTarget=m_kba.getChild(idoAction,idtoAction,Constants.IdPROP_TARGETCLASS, userRol, idtoUserTask, m_kba.getDefaultSession());
						int idRangeTarget=m_kba.getIdRange(propertyTarget);
						int idtoRangeTarget=m_kba.getClass(idRangeTarget);
						
						if(m_kba.hasShowAction(idoAction, idtoAction, idtoRangeSource, idtoRangeTarget, idtoUserTask, userRol))
							creationActions.add(idtoAction);
						//else System.err.println("WARNING: La acción "+m_kba.getLabelUserTask(idtoAction)+" ha sido excluida en la creación de "+m_kba.getLabelClass(idClassTarget,idtoUserTask)+" por falta de permisos");
					}
				}
				itrIdos=m_kba.getIdoUserTasksImport(idClassTarget,true);
				ArrayList<Integer> imports=new ArrayList<Integer>();
				while(itrIdos.hasNext()){
					imports.add(m_kba.getIdtoUserTaskImport(itrIdos.next()));
				}
				itrIdos=m_kba.getIdoUserTasksExport(idClassTarget,m_areaFuncional);
				ArrayList<Integer> exports=new ArrayList<Integer>();
				while(itrIdos.hasNext()){
					exports.add(m_kba.getIdtoUserTaskExport(itrIdos.next()));
				}
				
				//itrIdos=m_kba.getIdosFromServer(Constants.IDTO_REPORT_FORMAT,idtoUserTask).iterator();
				itrIdos=m_kba.getIndividuals(Constants.IDTO_REPORT_FORMAT,Constants.LEVEL_INDIVIDUAL,false);
				ArrayList<Integer> reportFormats=new ArrayList<Integer>();
				while(itrIdos.hasNext()){
					reportFormats.add(itrIdos.next());
				}

				boolean email=m_kba.getNotification(idClassTarget)!=null;
				
				String label=m_kba.getLabelUserTask(idtoUserTask);//m_kba.getLabelClass(idClassTarget,idtoUserTask);

//				String idImage=/*idClassFilter*/idTarget+":"+property.getIdProp();
//				ArrayList<Integer> userTasksTarget=m_kba.getIdoUserTasks(/*m_areaFuncional,*/idClassFilter,null);
//				System.out.println("OperationGUI:idClass del IdTarget:"+idClassFilter+"->"+m_kba.getIdoUserTasks(idClassFilter, null).toString());
				/*int operations=m_kba.getOperationsTarget(idTarget,m_areaFuncional);*/
				/*ArrayList<Integer> userRols=m_kba.getUserRols(id);
			   	m_defaultUserRol=userRols.get(0);// Deberia hacer algo para cogerse el mas restrictivo
				OperationsObject operationsObject=new OperationsObject(userTasksTarget);*/

				LinkedHashMap<Integer,ArrayList<UserAccess>> accessUserTasks=m_kba.getAllAccessIndividual(idTarget, userRol, property.getTypeAccess(), idtoUserTask);
				AccessAdapter tableAccess=new AccessAdapter(accessUserTasks,property,false,true);
				buildTarget(property,idTarget,label,/*idImage*/label,/*operations,*/idtoUserTask/*,userTasksTarget,userRol*/,userRol,/*operationsObject*//*accessUserTasks*/tableAccess,reports,reportsDirectPrint,actions,creationActions,imports,exports,reportFormats,email);
			//}
		}
		
		//Restauramos como communicator el original para evitar algun tipo de problema por que se quede el communicator global
		m_kba.setServer(Singleton.getInstance().getComm());
	}


	private void buildTarget(ObjectProperty property,Integer id,String label,String idImage,int idtoUserTask,Integer userRol,AccessAdapter tableAccess,HashSet<Integer> reports, HashSet<Integer> reportsDirectPrint, ArrayList<Integer> actions, ArrayList<Integer> creationActions, ArrayList<Integer> imports, ArrayList<Integer> exports, ArrayList<Integer> reportFormats, boolean email) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, DataErrorException, InstanceLockedException, SQLException, NamingException, JDOMException{


		/*m_com.m_userRol = userRol;*/
		/*access myAccess=new access(operations);*/
		/*int idFilter=m_kb.createPrototype(id, userRol.intValue(), m_com.m_user, idtoUserTask);*/
		ImageIcon icono=null;//m_com.getIcon(idImage);//m_com.getDbImage(null, idImage,0,0 );//null;//m_com.getDbImage( null, idImagen, 100,70 );
		// System.out.println("ACCESO DE
		// ROL:"+label+","+userRol+","+ctx.getAcceso( userRol ));
		/*botoneraAccion botonera = new botoneraAccion(m_colorFondo, m_com, m_md,
				0, tg, null, true, false, null, this, new Integer(id),
				userRol, true, false, true, false, false);*/

		IdObjectForm idObjectOperation=new IdObjectForm();
		// Luego en actionPerformed se usa el ido de la fila elegida, pero aqui le paso como ido
		// el del target ya que lo necesito
		idObjectOperation.setIdo(id);
		String idString=idObjectOperation.getIdString();

		botoneraAccion botonera=null;
		FormControl form=null;
		ArrayList<Integer> directReports=null;
		
		boolean add=false;
		if(!tableAccess.getUserTasksAccess(AccessAdapter.VIEW).isEmpty()){
			if(!tableAccess.getUserTasksAccess(AccessAdapter.FIND).isEmpty()){//Si tenemos permiso de buscar mostramos el filterControl
				HashMap<Integer, String> idtoReports = new HashMap<Integer, String>();
				HashMap<Integer, String> idtoReportsDirectPrint = new HashMap<Integer, String>();
				HashMap<Integer, String> idtoNameActions = new HashMap<Integer, String>();
				HashMap<Integer, String> idtoNameCreationActions = new HashMap<Integer, String>();
				HashMap<Integer, String> idtoImports=new HashMap<Integer, String>();
				HashMap<Integer, String> idtoExports=new HashMap<Integer, String>();
				HashMap<Integer, String> idoReportFormats=new HashMap<Integer, String>();
				for(int idtoReport:reports){
					String value = m_kba.getLabelUserTask(idtoReport);
					idtoReports.put(idtoReport, value);
				}
				for(int idtoReport:reportsDirectPrint){
					String value = m_kba.getLabelUserTask(idtoReport);
					idtoReportsDirectPrint.put(idtoReport, value);
				}
				for(int i=0;i<actions.size();i++){
					String value = m_kba.getLabelUserTask(actions.get(i));
					idtoNameActions.put(actions.get(i), value);													
				}
				for(int i=0;i<creationActions.size();i++){
					String value = m_kba.getLabelUserTask(creationActions.get(i));
					idtoNameCreationActions.put(creationActions.get(i), value);													
				}
				for(int i=0;i<imports.size();i++){
					String value = m_kba.getLabelUserTask(imports.get(i));
					idtoImports.put(imports.get(i), value);													
				}
				for(int i=0;i<exports.size();i++){
					String value = m_kba.getLabelUserTask(exports.get(i));
					idtoExports.put(exports.get(i), value);													
				}
				for(int i=0;i<reportFormats.size();i++){
					String value = Utils.normalizeLabel((String)m_kba.getValueData(m_kba.getRDN(reportFormats.get(i), Constants.IDTO_REPORT_FORMAT, userRol, idtoUserTask, m_kba.getDefaultSession())));
					idoReportFormats.put(reportFormats.get(i), value);													
				}
				
				botonera = new botoneraAccion( /*m_com, m_md,*/
						idString, null, idtoReports, idtoReportsDirectPrint, idtoNameActions, idtoNameCreationActions, idtoImports, idtoExports, idoReportFormats, email, botoneraAccion.SEARCH_TYPE ,null,null,null, this,/* myAccess,*//*operationsObject*//*accessUserTasks,*/tableAccess,
						false, true, Singleton.getInstance().getGraphics(), m_kba.getServer(),null,m_kba.canSetUpColumnProperty());
				/*tg.setBotonera(botonera);*/
	
				final Integer idtoUserTaskFinal = idtoUserTask;
				//final Integer userRolFinal = userRol;
				MouseAdapter mouseListenerOperatioGUI = new MouseAdapter(){
					public void mouseClicked(MouseEvent e){
						try{	
							if (e.getClickCount() == 2 && !e.isConsumed()){
								exeEvent(e.getSource(), botoneraAccion.OPERATION_ACTION, m_currTargetFilter.form.getId(), idtoUserTaskFinal, botoneraAccion.CONSULTAR, null);	
								//exeEvent(e.getSource(), botoneraAccion.OPERATION_ACTION, m_currTargetFilter.form.getId(), idtoUserTaskFinal, tableAccess.getUserTasksAccess(AccessAdapter.SET).isEmpty()?botoneraAccion.CONSULTAR:botoneraAccion.EDITAR, null);
							}
						} catch (Exception ex) {
							Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),ex,"Error al consultar");
							ex.printStackTrace();						
						}
					}
				};
				form=new filterControl(
						m_kba.getDefaultSessionWithoutRules(),
						null,
						idtoUserTask,userRol,
						property,/*m_kba.getIdoFilter(property),*/id,m_kba.getClass(id),
						false, false, botonera, true,
						-1, true, isBusinessClassFilter(m_kba.getIdRange(property),userRol,idtoUserTask),mouseListenerOperatioGUI, this, m_kba, m_controlDialog/*,null*/);
				
				add=true;
			}else if(!tableAccess.getUserTasksAccess(AccessAdapter.NEW_AND_REL).isEmpty()){//Si no tenemos permiso de buscar pero si de crear ponemos directamente el formulario de creación
				
				directReports=m_kba.getIdtoUserTasksDirectReport(m_kba.getClass(id),m_areaFuncional);
				botonera=new botoneraAccion(
						idString,null,null,null,null,null,null,null,null,false,
						botoneraAccion.RECORD_TYPE,
						null, null,
						null,
						this,
						null,
						false,
						true,
						Singleton.getInstance().getGraphics(),
						m_kba.getServer(),null,m_kba.canSetUpColumnProperty());

//				DefaultSession session=m_kba.createDefaultSession(m_kba.getDDBBSession(),idtoUserTask,true,true,true,true,true);
//				boolean success=false;
//				try{
//					int idRange=m_kba.getIdRange(property);
//					form= new transitionControl(
//							session,
//							userRol,
//							property.getIdo(),
//							m_kba.createPrototype(idRange, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session),
//							m_kba.getClass(idRange),
//							/*command,*/idtoUserTask,
//							access.NEW,
//							(Dimension)ConstantesGraficas.dimZonaTrabajo.clone(),
//							/*null*/botonera.getComponent(), m_kba, m_controlDialog, null, directReports, false, true);
//					success=true;
//				}finally{
//					if(!success){
//						session.setForceParent(false);
//						session.rollBack();
//					}
//				}
				
				add=true;
			}
		}
		
		if(add){
			Target tgf = new Target(id,label,icono,form,botonera,directReports,idtoUserTask);
			/*Integer userRol=userRolList.get(0);*/
			m_targets.put(new Integer(id), tgf);

			/*return tgf;*/
		}
	}
	

	private boolean isBusinessClassFilter(int ido,Integer userRol,Integer idtoUserTask) throws NotFoundException, IncoherenceInMotorException{
		/*if(m_kba.isAbstractClass(ido)){

			Session session=m_kba.createDefaultSession(m_kba.getDefaultSession(),idtoUserTask);
			ArrayList<Integer> specialized=m_kba.getSpecializedFilters(ido, userRol, idtoUserTask, session);
			if(!specialized.isEmpty()){
				return true;
			}
		}*/
		return false;
	}

//	private JPanel buildBotoneraAHeredar( int idTarget, int idtoUserTask){
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
//                                         this,(int) botoneraAccion.getButtonHeight(),(int) botoneraAccion.getButtonHeight(), true);
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
//                                             this,(int) botoneraAccion.getButtonHeight(),(int) botoneraAccion.getButtonHeight(), true);
//
//		/*}*/
//		
//		return botonera;
//	}


	/*   public void cancelProcessEvent(  int proType, int currPro ){;}
   public void freeOwningTaskEvent(  int currTask ){;}
	 */
	public void actionPerformed( ActionEvent ae ){
		try{
			//if(!Singleton.getInstance().isGuiLock()){
			//Singleton.getInstance().setGuiLock(true);
			//System.out.println("OPERATIONGUI:ACTIONPERFORMED");
			String command = ae.getActionCommand();
			//System.out.println("OPGUI:COMMAND:" + command);

			/*       String operationType=buf[0];
		       Integer idTargetFilter = !buf[1].equals("null")?Integer.parseInt(buf[1]):null;
		       Integer idtoUserTask = !buf[2].equals("null")?Integer.parseInt(buf[2]):null;
		       int buttonType = Integer.parseInt(buf[3]);
			 */       /*String id2 = buf.length > 4 ? buf[4] : null;*/

			IdOperationForm idOperation=new IdOperationForm(command);
			Integer operationType=idOperation.getOperationType();
			Integer buttonType = idOperation.getButtonType();

			IdObjectForm target=idOperation.getTarget();
			Integer idTarget = target.getIdo();
			Integer idtoUserTask = target.getIdtoUserTask();
			Integer value= target.getValue();

			Object boton=ae.getSource();

			exeEvent(boton,operationType,idTarget,idtoUserTask,buttonType,value);
			
		}catch(Exception e){
			Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),e,"Error al intentar ejecutar la operación");
			e.printStackTrace();
		}
	}
	
	private void exeEvent(final Object source,final Integer operationType,final Integer idTarget,final Integer idtoUserTask,final int buttonType,final Integer value){
		//System.err.println("AE="+ae);
		
		//Pedimos el dialog del source y no utilizamos m_controlDialog ya que en este metodo tambien se escucha la botonera heredada al dialogo modal
		final WindowComponent dialog;
		Object wind=SwingUtilities.getWindowAncestor((Component)source);
		if(wind instanceof WindowComponent)
			dialog=(WindowComponent)wind;
		else dialog=m_controlDialog;
		
		dialog.disabledEvents();
		//m_controlDialog.disabledEvents();
		
//		if(source instanceof AbstractButton){
//			if(((AbstractButton)source).getToolTipText()!=null){
//				Singleton.getInstance().getStatusBar().setLocalizacion(((AbstractButton)source).getToolTipText(),2);
//				
//			}
//		}
		if (buttonType != botoneraAccion.RESET_ALL && buttonType != botoneraAccion.HELP && buttonType != botoneraAccion.CONFIG_COLUMNPROPERTIES) {
			Singleton.getInstance().getStatusBar().setBarraProgreso();
			
		}
		
		// Hacemos que se ejecute en un nuevo hilo para mantener libre el hilo AWT-EventQueue
		SwingWorker worker=new SwingWorker(){
			public Object construct(){
				boolean success=false;
				try{
					Object result=doWorkEvent(operationType,idTarget,idtoUserTask,buttonType/*,id2This*/,value);
					success=true;
					return result;
				}finally{
					if(!success){
						StatusBar statusBar=Singleton.getInstance().getStatusBar();
						statusBar.setFinishBarraProgreso("Error al intentar ejecutar la operación",true);
						doFinished();
					}
				}
			}

			public void finished(){
				//System.err.println("Vuelve a habilitar");
				StatusBar statusBar=Singleton.getInstance().getStatusBar();
				if(statusBar.hasBarraProgreso())
					statusBar.setFinishBarraProgreso((String)getValue(),true);
				else if(!statusBar.isError() && getValue()!=null)
					statusBar.setAccion((String)getValue());
				
				//m_controlDialog.enabledEvents();
				dialog.enabledEvents();
				/*if((String)getValue()==null)
						statusBar.upNivelLocalizacion();*/

				//Singleton.getInstance().setGuiLock(false);
				
			}
		};
		
		
		worker.start();
	}

	private synchronized Object doWorkEvent(Integer operationType,final Integer idTarget,final Integer idtoUserTask,int buttonType/*,String id2*/,Integer value){
		try{
			String mensajeRespuesta="";

			Integer userRol=null;
			/*ArrayList<Integer> userRols=new ArrayList<Integer>();
			if(idtoUserTask!=null){
				userRols=m_kba.getUserRols(idtoUserTask,m_kba.getDefaultSession());
				userRol = m_kba.getUserRolRestrictive(userRols);
			}*/

			if (operationType==botoneraAccion.OPERATION_ACTION && m_currTargetFilter != null) {
				//System.out.println("ENTRA EN ACTION DE OPERATION GUI");
				/*Target tg = menuIndex.intValue() >=0 ?  (Target) m_targetsFilter.get(menuIndex):null;*/
				final Target tgf = idTarget !=null ?  (Target) m_targets.get(idTarget):null;

				filterControl filterControl=null;
				transitionControl transitionControl=null;
				
				if(tgf.form instanceof filterControl)
					filterControl=(filterControl)tgf.form;
				else if(tgf.form instanceof transitionControl)
					transitionControl=(transitionControl)tgf.form;
				
				if (buttonType == botoneraAccion.ELIMINAR) {
					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					if (parList.isEmpty()) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
						return null;
					}
					/*if (parList.size() > 1) {
                	   m_actionManager.showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
                       return null;
                   }*/
					String textNumElementos="el elemento";
					if (parList.size() > 1)
						textNumElementos="los "+parList.size()+" elementos";
					Object[] options = {"Sí", "No"};
					int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
							"¿Está seguro que desea borrar "+textNumElementos+" en base de datos?",
							Utils.normalizeLabel("CONFIRMACIÓN DE BORRADO"),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[1],m_controlDialog.getComponent());

					if (res != JOptionPane.YES_OPTION)return "Eliminación de registro/s cancelada";
					if(filterControl.deleteObjects(filterControl,filterControl.getKnowledgeBase().getDDBBSession(),parList,idtoUserTask,userRol,true))
						mensajeRespuesta="Registro/s eliminado/s";
					else mensajeRespuesta="No se puede eliminar el/los registro/s";
				}
				if (buttonType == botoneraAccion.EJECUTAR) {
					if(filterControl!=null){
						//Singleton.getInstance().setEnableDebug(false);
						/*try {*/
						/*ObjectProperty property=m_kba.getTarget(idtoUserTask,userRol);
	                	   access access=new access(property.getTypeAccess());*/
	
//						Element query = filterControl.buildQuery();
//						int uTask = tgf.idtoUserTask;
//						selectData response = m_com.serverGetQuery(/*m_com.getUser(),m_com.getBusiness(),*/query, uTask, queryData.MODE_ROW);
//	
//						//jdomParser.print("TXCTX USERVENT ", response.toElement());
//						filterControl.setRows(response,resultPanel.PANEL_RESULTADOS);
//	
//						if(response!=null){
//							mensajeRespuesta=response.size()+" registros encontrados";
//						}
	
						int numRegistros=filterControl.exeQuery();
						mensajeRespuesta=numRegistros+" registros encontrados";
	
						/*} catch (SystemException se) {
	                       Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),se);
	                       return "Error";
	                   }finally{
			       			Singleton.getInstance().setEnableDebug(true);
			   			}*/
					}else if(transitionControl!=null){
						int idtoUTask=transitionControl.getIdtoUserTask();
						boolean success=false;
						try{
							transitionControl.confirm();
							success=true;
						}catch(CardinalityExceedException e){
							System.err.println(e.getProp());
							e.printStackTrace();
							Property prop=e.getProp();
							String message=e.getUserMessage();
							if (prop!=null){
								try{
									if (prop.getIdo()!=transitionControl.getId()){
										message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), idtoUTask)+ " de "+m_kba.getLabelClass(prop.getIdto(), idtoUTask);
										String rdn=(String)m_kba.getValueData(m_kba.getRDN(prop.getIdo(), prop.getIdto(), userRol, idtoUTask, transitionControl.getSession()));
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
												options[0],m_controlDialog.getComponent());

										if(res == JOptionPane.YES_OPTION){
											try{
												transitionControl.editInForm(transitionControl.getId(), prop.getIdo());
											} catch (EditionTableException exc) {
												Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),exc, "Error al intentar editar fila en formulario");
												return null;
											}
										}
									}else{
										message+=": "+m_kba.getLabelProperty(prop, prop.getIdto(), idtoUTask);
										Singleton.getInstance().getMessagesControl().showErrorMessage(message,m_controlDialog.getComponent());
									}
								}catch(Exception ex){
									Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),e,/*"Error al intentar mostrar mensaje de cardinalidad"*/null);
									e.printStackTrace();
								}
								
							}else{
								Singleton.getInstance().getMessagesControl().showErrorMessage(message,m_controlDialog.getComponent());
							}

							return null;
						}catch(OperationNotPermitedException e){
							Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),m_controlDialog.getComponent());
							return null;
						}finally{
							if(success){
								replaceCreationForm(tgf,idtoUTask,userRol);
							}
						}
					}
				}
				if (buttonType == botoneraAccion.CANCEL) {
					int idtoUTask=transitionControl.getIdtoUserTask();
					boolean oldForceParent=transitionControl.getSession().isForceParent();
					boolean success=false;
					boolean cancelled=false;
					try{
						transitionControl.getSession().setForceParent(false);
						cancelled=transitionControl.cancel();
						success=true;
					}finally{
						if(!success){
							transitionControl.getSession().setForceParent(oldForceParent);
						}
					}
					if(cancelled){
						replaceCreationForm(tgf,idtoUTask,userRol);
					}else{
						transitionControl.getSession().setForceParent(oldForceParent);
					}
					
				}
				
				if (buttonType == botoneraAccion.HELP){
					/*JPopupMenu menu=new JPopupMenu();
					JEditorPane pane=new JEditorPane();
					pane.setBackground(UIManager.getColor("ToolBar.background"));
					pane.setContentType("text/html");
					pane.setEditable(false);
					//pane.addHyperlinkListener(this);
					pane.setBorder(BorderFactory.createEmptyBorder());
					pane.setText("<html><a href='www.google.es'>Este tipo de objeto es un "+m_kba.getLabelUserTask(idtoUserTask)+"</a><img src='dyna/images/Calidad.gif'/></html>");
					menu.add(pane);
					menu.pack();
					menu.show(tgf.form.getComponent(), 200, 200);*/
					//Singleton.getInstance().getMessagesControl().showMessage("<html><a href='www.google.es'>Este tipo de objeto es un "+m_kba.getLabelUserTask(idtoUserTask)+"</a></html>");
					Singleton.getInstance().getHelpComponent().showHelp(idtoUserTask,idtoUserTask,userRol,m_controlDialog.getComponent());
				}
				
				if (buttonType == botoneraAccion.RESET_ALL) {
					filterControl.resetAll();
					mensajeRespuesta="Parámetros de búsqueda reseteados";
				}
				
				if (buttonType == botoneraAccion.CONFIG_COLUMNPROPERTIES) {
					ObjectProperty property=m_kba.getTarget(m_kba.getIdoUserTask(tgf.idtoUserTask),tgf.idtoUserTask,userRol,m_kba.getDefaultSession());
					
					int idObject=idTarget;
					int idtoObject=m_kba.getClass(idTarget);
					int idtoTable=idtoObject;
					int idtoParent=property.getIdto();
					boolean filterMode=true;
					boolean structural=false;
					boolean onlyFirstLevel=false;
					HashMap<String,String> aliasMap=null;
					
					DefaultSession sess=new DefaultSession(m_kba.getKnowledgeBase(), m_kba.getDDBBSession(), idtoUserTask, true, true, true, true, true);
					ArrayList<Column> columnList=m_kba.getColumnsObject(idObject, idtoObject, idtoTable, idtoParent, null, idtoUserTask, sess, filterMode, structural, onlyFirstLevel, aliasMap);
					
					ColumnsTableComponent component=new ColumnsTableComponent(m_kba, this.m_controlDialog, idtoUserTask, property, sess, columnList);
					mensajeRespuesta="Configuración de tabla mostrada";
				}
				
				if (buttonType == botoneraAccion.EMAIL) {
					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					if (parList.isEmpty()) {
						//System.out.println("OPERATIONGUI:ActionPerformed:Editar:Debe seleccionar al menos un objeto");
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
						return null;
					}
					/*if (parList.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN ÚNICO REGISTRO",m_controlDialog.getComponent());
						return null;
					}*/
					
					if (parList.size() > 1) {
						Object[] options = {"Sí", "No"};
						int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
								"Se van a enviar "+parList.size()+" email. ¿Está seguro?",
								Utils.normalizeLabel("CONFIRMACIÓN DE EMAIL"),
								JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE,
								null,
								options,
								options[0],m_controlDialog.getComponent());

						if (res == JOptionPane.YES_OPTION){
							int emailsSent=0;
							for(GIdRow idRow:parList){
								int ido=idRow.getIdo();
								int idto=idRow.getIdto();
								
								int idRange=m_kba.getClass(idTarget);
								int idoNotification=m_kba.getNotification(idRange);
								
								EmailRequest emailRequest=new EmailRequest(idoNotification, ido, idto, "MANUAL", m_kba.getKnowledgeBase());
								emailRequest.replaceBodyCode(new HashMap<Integer, HashMap<Integer,Value>>());
								
								if(m_kba.sendEmail(emailRequest,false)){
									emailsSent++;
								}
							}
							if(emailsSent==parList.size()){
								Singleton.getInstance().getMessagesControl().showMessage("Se han enviado todos los email correctamente",m_controlDialog.getComponent());
							}else{
								Singleton.getInstance().getMessagesControl().showErrorMessage("Error al enviar "+(parList.size()-emailsSent)+" email. Enviados correctamente "+emailsSent+" email. \nRevíselo en configuración-log email",m_controlDialog.getComponent());
							}
						}
					}else{
						GIdRow idRow=parList.get(0);
						int ido=idRow.getIdo();
						int idto=idRow.getIdto();
						
						int idRange=m_kba.getClass(idTarget);
						int idoNotification=m_kba.getNotification(idRange);
						
						EmailRequest emailRequest=new EmailRequest(idoNotification, ido, idto, "MANUAL", m_kba.getKnowledgeBase());
						emailRequest.replaceBodyCode(new HashMap<Integer, HashMap<Integer,Value>>());
						
						EmailForm emailManager=new EmailForm(emailRequest, this.m_controlDialog, m_kba);
						emailManager.show();
					}
					//mensajeRespuesta="Parámetros de búsqueda reseteados";
				}
				
				if (buttonType == botoneraAccion.EDITAR) {
					//System.out.println("ENTRA EN ACTION-EDITAR DE OPERATION GUI");

					/*ObjectProperty property=m_kba.getTarget(idtoUserTask,userRol);*/
					/*access access=new access(property.getTypeAccess());*/

					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					if (parList.isEmpty()) {
						//System.out.println("OPERATIONGUI:ActionPerformed:Editar:Debe seleccionar al menos un objeto");
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
						return null;
					}
					/*if (parList.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN ÚNICO REGISTRO");
						return null;
					}*/
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					mensajeRespuesta="Formulario de modificación mostrado";
					if (parList.size() > 1) {
						//System.out.println("OPERATIONGUI:ActionPerformed:Editar:Seleccion multiple");
						
						HashMap<Integer,Integer> listIdo = new HashMap<Integer,Integer>();
						Iterator<GIdRow> it = parList.iterator();
						while(it.hasNext()){
							GIdRow idRow = it.next();
							listIdo.put(idRow.getIdo(),idRow.getIdto());
						}	
						SetCommonCommandPath commandPath=new SetCommonCommandPath(m_kba.getIdoUserTask(idtoUserTask),listIdo, idtoUserTask, userRol,m_kba.getDDBBSession());
						commandList.add(commandPath);
						exeActions(commandList,/*buildBotoneraAHeredar(idTarget, idtoUserTask)*/filterControl.getResultPanel());

					}else{
						//System.out.println("OPERATIONGUI:ActionPerformed:Editar:Mu bien");
						GIdRow parRow = parList.iterator().next();

						//System.out.println("OPERATIONGUI:ActionPerformed:Editar:ES IGUAL A 0");

						/*Lo creamos en otro hilo ya que switchObject espera a que el formulario modal
						 * que crea se cierre*/
						/*final TargetFilter tgfThis=tgf;
						final Integer userRolThis=userRol;
						final ArrayList <Integer> userRolsThis=userRols;
						final instance parRowThis=parRow;
						final Integer menuIndexThis=menuIndex;
						final Integer idTargetThis=idTarget;
						final Integer idtoUserTaskThis=idtoUserTask;

						final access accessThis=access;
						SwingWorker worker=new SwingWorker(){
               			public Object construct(){

						ArrayList<commandPath> commandList = new ArrayList<commandPath>();
						commandList.add(new commandPath(parRowThis.getType(),
						parRowThis.getIDO(),
						tgThis.getId(),idtoUserTaskThis,
						m_currMode,accessnew access(m_currMode)));*/

						SetCommandPath commandPath=new SetCommandPath(m_kba.getIdoUserTask(idtoUserTask),idtoUserTask,parRow.getIdo(),parRow.getIdto(),idtoUserTask,userRol,m_kba.getDDBBSession());
						commandList.add(commandPath);
						exeActions(commandList,/*buildBotoneraAHeredar(idTarget, idtoUserTask)*/filterControl.getResultPanel());
						
						/*if(m_kba.isSpecialized(idTarget,Constants.IDTO_SOLICITUD)){
							setSolicitudAsRead(parRow.getIDO(),parRow.getIdTo(),true,userRol,idtoUserTask);
						}*/
					}
				}
				if (buttonType == botoneraAccion.REPORT || buttonType == botoneraAccion.PRINT_SEARCH || buttonType == botoneraAccion.PRINT) {
					Element query;
					if (buttonType == botoneraAccion.PRINT_SEARCH) {
						query = filterControl.getLastQuery();
						if(query==null){
							Singleton.getInstance().getMessagesControl().showMessage("DEBE REALIZAR UNA BUSQUEDA PARA IMPRIMIRLA",m_controlDialog.getComponent());
							return null;
						}

						/*Element limitNode=jdomParser.findElementByAt(query, "LIMIT", true);
						if(limitNode!=null) limitNode.removeAttribute("LIMIT");*/
						HashMap<String,String> oidReport=m_kba.getReport(query, false,null, idtoUserTask, null, false, value, userRol, idtoUserTask, m_kba.getDefaultSession(),0,false);
						if(oidReport!=null){
							m_kba.getServer().showReport("Listado de "+m_kba.getLabelClass(idTarget,idtoUserTask),oidReport,false,true);
							mensajeRespuesta="Listado mostrado";
						}else mensajeRespuesta="Listado no encontrado";
					}else if(buttonType == botoneraAccion.PRINT){
						
						resultPanel rp = filterControl.getResultPanel();
						ArrayList<GIdRow> parList = rp.getDataSelectedRows();
						if (parList.isEmpty()) {
							//System.out.println("OPERATIONGUI:ActionPerformed:Editar:Debe seleccionar al menos un objeto");
							Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
							return null;
						}
						
						int idoUserTask=m_kba.getIdoUserTaskReport(idtoUserTask);
						
						HashMap<Integer,Integer> mapIdosIdtos = new HashMap<Integer, Integer>();
						Iterator<GIdRow> it = parList.iterator();
						while(it.hasNext()){
							GIdRow idRow = it.next();
							mapIdosIdtos.put(idRow.getIdo(),idRow.getIdto());
							//valueCls=ins.getIdTo();	
						}
						m_kba.print(mapIdosIdtos,new HashSet<Integer>(), idoUserTask, idtoUserTask, userRol, idtoUserTask);
						
					}else{
						int idoUserTask=m_kba.getIdoUserTaskReport(idtoUserTask);
						Property property=null;
						try{
							property=m_kba.getProperty(idoUserTask, idtoUserTask, Constants.IdPROP_PARAMS, userRol, idtoUserTask, m_kba.getDefaultSession());
						} catch (NotFoundException e) {
							//System.out.println("Report sin parametros");
						}
						if(property!=null){
							resultPanel rp = filterControl.getResultPanel();
							ArrayList<GIdRow> parList = rp.getDataSelectedRows();
							
							HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
							Iterator<GIdRow> itr=parList.iterator();
							while(itr.hasNext()){
								GIdRow idRow=itr.next();
								values.put(idRow.getIdo(),idRow.getIdto());
							}
							
							ArrayList<commandPath> commandList = new ArrayList<commandPath>();
							/*commandList.add(new commandPath(parRowThis.getIDO(),m_currMode,idtoUserTaskThis));*/
							ReportCommandPath commandPath=new ReportCommandPath(property,values,idtoUserTask,userRol,m_kba.getDefaultSession());
							commandList.add(commandPath);

							exeActions(commandList,null);
						}else{
							//resultPanel rp = filterControl.getResultPanel();
							//selectData parList = rp.getDataSelectedRows();
							/*if (!parList.hasData()) {
								Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN OBJETO");
								return null;
							}
							if (parList.size() > 1) {
								Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
								return null;
							}*/
							HashMap<String,String> oidReport=m_kba.getReport(/*query*/null, false,idoUserTask, idtoUserTask, m_kba.getLabelClassWithoutAlias(idtoUserTask), false, null, userRol, idtoUserTask, m_kba.getDefaultSession(),0,false);
							if(oidReport!=null){
								m_kba.getServer().showReport("Informe "+m_kba.getLabelClass(idTarget,idoUserTask),oidReport,false,true);
								mensajeRespuesta="Informe mostrado";
								DataProperty propConfirm=m_kba.getField(idoUserTask, idtoUserTask, Constants.IdPROP_CHECKPRINTING, userRol, idtoUserTask, m_kba.getDefaultSession());
								if(m_kba.getValueData(propConfirm)!=null){
									Object[] options = {"Sí", "No"};
									int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
											"¿El informe ha sido impreso en papel?",
											Utils.normalizeLabel("CONFIRMACIÓN DE IMPRESIÓN"),
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE,
											null,
											options,
											options[1],m_controlDialog.getComponent());
	
									if (res == JOptionPane.YES_OPTION){
										m_kba.setState(idoUserTask,idtoUserTask,Constants.INDIVIDUAL_REALIZADO,userRol,idtoUserTask,m_kba.getDDBBSession());
									}
								}
								
							}else mensajeRespuesta="Informe no encontrado";
						}
					}
				}

				if (buttonType == botoneraAccion.CONSULTAR) {
					if(consultar(idtoUserTask, idTarget, mensajeRespuesta, userRol, filterControl)==null)
						return null;
				}
				if (buttonType == botoneraAccion.CREAR) {
					/*System.out.println("CREAR "+tg.ctx.process+","+tg.ctx.to);*/
					Integer idoUserTask=m_kba.getIdoUserTask(idtoUserTask);
					if(idoUserTask==null)//Esto ocurrira si se intenta crear a partir de una accion
						idoUserTask=m_kba.getIdoUserTaskAction(idtoUserTask);


					mensajeRespuesta="Formulario de inserción mostrado";
					/*Lo creamos en otro hilo ya que switchObject espera a que el formulario modal
					 * que crea se cierre*/
					//final TargetFilter tgfThis=tgf;
//					final int idClassThis=idClass;
//					final int idObjectThis=idObject;
//					final int operationThis=access.NEW;
//					final access accessThis=/*access*/new access(access.NEW);
					final int idtoUserTaskThis=idtoUserTask;
					final Integer userRolThis=userRol;
					final int idoUserTaskThis=idoUserTask;
					//final int idTargetThis=m_kba.getClass(idTarget);

					/*	   			SwingWorker worker=new SwingWorker(){
	    			public Object construct(){*/
//					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
//					commandList.add(new commandPath(idClassThis,idObjectThis,idtoUserTaskThis,operationThis,accessThis));
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					/*commandList.add(new commandPath(idObjectThis,operationThis,idtoUserTaskThis));*/
//					ObjectProperty property=m_kba.getChild(idoUserTaskThis,Constants.IdPROP_TARGETCLASS,userRolThis,idtoUserTaskThis,null);
//					/*int idClass=property.getRangoList().getFirst();*/
//					int idoFilter=m_kba.getIdoFilter(property);
//					int idtoFilter=m_kba.getIdtoFilter(property);

//					ObjectProperty property=m_kba.getChild(idoUserTaskThis,Constants.IdPROP_TARGETCLASS,userRolThis,idtoUserTaskThis,m_kba.getDDBBSession());
//					int idRange=m_kba.getIdRange(property, idTarget);
					int idRange=m_kba.getClass(idTarget);//idTarget;Nos quedamos con la clase ya que si no la creacion estara afectada por el filtro de busqueda
					NewCommandPath commandPath=new NewCommandPath(idoUserTaskThis,idtoUserTaskThis,Constants.IdPROP_TARGETCLASS,/*idtoFilter*//*idTargetThis*/idRange,idtoUserTaskThis,userRolThis,m_kba.getDDBBSession());
					commandList.add(commandPath);

					exeActions(commandList,null);

					return null;
					/*	    			}
	    		};
	    		worker.start();
					 */            
				}

				if (buttonType == botoneraAccion.ACTION) {

					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					/*if (parList.isEmpty()) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
						return null;
					}*/
					/*if (parList.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
						return null;
					}*/
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					
					mensajeRespuesta="Formulario de accion mostrado";
					//final instance parRowThis=parRow;
					final Integer userRolThis=userRol;

					HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
					Iterator<GIdRow> itr=parList.iterator();
					while(itr.hasNext()){
						GIdRow idRow=itr.next();
						values.put(idRow.getIdo(),idRow.getIdto());
					}
					ActionCommandPath commandPath=new ActionCommandPath(m_kba.getIdoUserTaskAction(idtoUserTask),values,idtoUserTask,userRolThis,m_kba.getDDBBSession());
					commandList.add(commandPath);

					exeActions(commandList,null);

				}
				
				if (buttonType == botoneraAccion.IMPORT) {

					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					/*if (!parList.hasData()) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO");
						return null;
					}*/
					/*if (parList.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
						return null;
					}*/
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					
					mensajeRespuesta="Formulario de importación mostrado";
					//final instance parRowThis=parRow;
					final Integer userRolThis=userRol;

					ImportCommandPath commandPath=new ImportCommandPath(m_kba.getIdoUserTaskImport(idtoUserTask),idtoUserTask,idtoUserTask,userRolThis,m_kba.getDDBBSession());
					commandList.add(commandPath);

					exeActions(commandList,null);

				}
				
				if (buttonType == botoneraAccion.EXPORT) {

					resultPanel rp = filterControl.getResultPanel();
					ArrayList<GIdRow> parList = rp.getDataSelectedRows();
					if (parList.isEmpty()) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
						return null;
					}
					/*if (parList.size() > 1) {
						Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN SOLO OBJETO");
						return null;
					}*/
					ArrayList<commandPath> commandList = new ArrayList<commandPath>();
					
					mensajeRespuesta="Formulario de exportación mostrado";
					//final instance parRowThis=parRow;
					final Integer userRolThis=userRol;

					HashMap<Integer,Integer> values=new HashMap<Integer,Integer>();
					Iterator<GIdRow> itr=parList.iterator();
					while(itr.hasNext()){
						GIdRow idRow=itr.next();
						values.put(idRow.getIdo(),idRow.getIdto());
					}
					ExportCommandPath commandPath=new ExportCommandPath(m_kba.getIdoUserTaskExport(idtoUserTask),idtoUserTask,values,idtoUserTask,userRolThis,m_kba.getDDBBSession());
					commandList.add(commandPath);

					exeActions(commandList,null);

				}

			}
//			if (operationType==botoneraAccion.OPERATION_SCROLL && m_currTargetFilter != null) {
//				if (buttonType == botoneraAccion.NEXT || buttonType == botoneraAccion.PREV) {
//					//eliminaVentana();
//					
//					TargetFilter tgf = (TargetFilter) m_targetsFilter.get(idTarget);
//
//					/*ObjectProperty property=m_kba.getTarget(idtoUserTask,userRol);*/
//					/*access access=new access(property.getTypeAccess());*/
//					/*Integer userRol=tg.getUserRol();*/
//
//					/*m_com.m_userRol = userRol;*/
//					final resultPanel rp = tgf.filterControl.getResultPanel();
//					selectData parList = rp.getDataSelectedRows();
//					if (!parList.hasData())
//						return null;
//					//System.err.println("ENTRA POR NEXT O PREV");
//					//parRow = parList.getFirst();
//					//int currIdo = parRow.getIDO();
//					//int currIdTo = parRow.getIdTo();
//
//					instance lista = buttonType == botoneraAccion.NEXT ?
//							rp.getNextRow() : rp.getPrevRow();
//							if (lista == null) {
//								JOptionPane.showMessageDialog(m_controlDialog.getComponent(),Utils.normalizeMessage("NO HAY MAS REGISTROS EN LA BUSQUEDA"));
//								return null;
//							}
//							/*try {
//                       m_appControl.unlockObjects();
//                   } catch (SystemException se) {
//                       Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),se);
//                       return "Error";
//                   }*/
//							int ido = lista.getIDO();
//							//int idto = lista.getIdTo();
//
//							rp.selectRow(ido);
//							boolean editEnabled = tgf.botonera.getEnabled(botoneraAccion.EDITAR);
//							int mode = m_currMode;
//							if (mode == access.SET && !editEnabled)
//								mode = access.VIEW;
//////							ArrayList commandList = new ArrayList();
//////							commandList.add(new commandPath(idto, ido, /*tgf.id*/idtoUserTask, mode,/*access*/new access(mode)));
////							ArrayList<commandPath> commandList = new ArrayList<commandPath>();
////							/*commandList.add(new commandPath(ido,m_currMode,idtoUserTask));*/
////							commandPath commandPath;
////							if(mode==access.VIEW)
////								commandPath=new ViewCommandPath(m_kba.getIdoUserTask(idtoUserTask),ido,idtoUserTask,userRol,m_kba.getDefaultSession());
////							else commandPath=new SetCommandPath(m_kba.getIdoUserTask(idtoUserTask),ido,idtoUserTask,userRol,m_kba.getDDBBSession());
////							commandList.add(commandPath);
//							
////							exeActions(commandList,buildBotoneraAHeredar(idTarget, idtoUserTask));
//							m_actionManager.closeEvent();
//							
//							//Lo hacemos en el hilo AWTEvent para que el closeEvent anterior realice antes el coverContainer en Assistant 
//							final int modeThis=mode;
//							final Runnable doFinished = new Runnable() {
//								public void run() {
//									final Runnable doLock = new Runnable() {
//										public void run() {
//											m_controlDialog.lockChangeStateEvents(false);//Permitimos el cambio de estado de los eventos:habilitado/deshabilitado
//										}
//									};
//									SwingUtilities.invokeLater(doLock);//Invocandolo despues nos aseguramos que no este permitido un cambio de evento antes de tiempo por el cual el finished del exeEvent anterior lo habilitaria
//									exeEvent(rp.getComponent(),botoneraAccion.OPERATION_ACTION,idTarget,idtoUserTask,modeThis==access.VIEW?botoneraAccion.CONSULTAR:botoneraAccion.EDITAR);
//								}
//							};
//							m_controlDialog.lockChangeStateEvents(true);//Bloqueamos el cambio de estado de eventos para que el finished de exeEvent no lo habilite ya que aun no hemos terminado la operacion hasta que se ejecute el doFinished 
//							SwingUtilities.invokeLater(doFinished);
//						//	System.err.println("ENTRA POR NEXT O PREV");
//				}
//			}
			return mensajeRespuesta;
		}catch(OperationNotPermitedException e){
			//m_control.closeEvent();
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),m_controlDialog.getComponent());
			return null;
		}catch(InstanceLockedException e){
			//m_actionManager.closeEvent();
			Singleton.getInstance().getMessagesControl().showErrorMessage(e.getUserMessage(),m_controlDialog.getComponent());
			return null;
		}catch(ActionException e){
			//m_actionManager.closeEvent();
			e.printStackTrace();
			Singleton.getInstance().getMessagesControl().showMessage(e.getUserMessage(),m_controlDialog.getComponent());
			return null;
		}catch(Exception e){
			e.printStackTrace();
			Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),e,"Error al ejecutar la operación");
			return "Error";
		}
	}

	private void replaceCreationForm(Target tg,Integer idtoUTask, Integer userRol) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, AssignValueException, SQLException, NamingException, JDOMException{
		GestorContenedor gestor=(GestorContenedor)Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_TRABAJO);
		gestor.removePanels(String.valueOf(tg.id));

		DefaultSession session=m_kba.createDefaultSession(m_kba.getDDBBSession(),idtoUTask,true,true,true,true,true);	
		boolean success=false;
		try{
			tg.form= new transitionControl(
					session,
					userRol,
					m_kba.getIdoUserTask(idtoUTask),
					m_kba.createPrototype(tg.id, Constants.LEVEL_PROTOTYPE, userRol, idtoUTask, session),
					m_kba.getClass(tg.id),
					/*command,*/idtoUTask,
					access.NEW,
					(Dimension)ConstantesGraficas.dimZonaTrabajo.clone(),
					tg.botonera.getComponent(), m_kba, m_controlDialog, null, tg.directReports, false, true, null);
			success=true;
		}finally{
			if(!success){
				session.setForceParent(false);
				session.rollBack();
			}
		}
		
		gestor.addPanel(/*"SubFiltro:"+id*/String.valueOf(tg.id), tg.form.getComponent(), 1, tg.form.getComponent().getPreferredSize(), /*"Conjunto:"+id*/String.valueOf(tg.id));
		gestor.setVisiblePanels(/*"Conjunto:"+id*/String.valueOf(tg.id), true);
		
		requestDefaultFocus(tg.form);
	}
	
	private Object consultar(Integer idtoUserTask, int idTarget, String mensajeRespuesta, Integer userRol, filterControl filterControl) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		
		//System.out.println("Ami - Consultar: IDTOUTASK="+idtoUserTask+" IDTARGET="+idTarget+" MENSAJERESPUESTA="+mensajeRespuesta+" USERROL="+userRol+" filterControl="+filterControl);
		resultPanel rp = filterControl.getResultPanel();
		ArrayList<GIdRow> parList = rp.getDataSelectedRows();
		//System.err.println("OperationGUI:consultar "+parList);
		if (parList.isEmpty()) {
			Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR AL MENOS UN REGISTRO",m_controlDialog.getComponent());
			return null;
		}
		/*if (parList.size() > 1) {
			Singleton.getInstance().getMessagesControl().showMessage("DEBE SELECCIONAR UN ÚNICO REGISTRO");
			return null;
		}*/
		ArrayList<commandPath> commandList = new ArrayList<commandPath>();
		if (parList.size() > 1) {
			//System.out.println("OPERATIONGUI:ActionPerformed:Consultar:Seleccion multiple");
						
			HashMap<Integer,Integer> listIdo = new HashMap<Integer,Integer>();
			Iterator<GIdRow> it = parList.iterator();
			while(it.hasNext()){
				GIdRow idRow = it.next();
				listIdo.put(idRow.getIdo(),idRow.getIdto());
			}	
			ViewCommonCommandPath commandPath=new ViewCommonCommandPath(m_kba.getIdoUserTask(idtoUserTask),listIdo, idtoUserTask, userRol,m_kba.getDDBBSession());
			commandList.add(commandPath);
			exeActions(commandList,/*buildBotoneraAHeredar(idTarget, idtoUserTask)*/filterControl.getResultPanel());

		}else{
			GIdRow parRow = parList.iterator().next();
			mensajeRespuesta="Formulario de consulta mostrado";
			ViewCommandPath commandPath=new ViewCommandPath(m_kba.getIdoUserTask(idtoUserTask),idtoUserTask,parRow.getIdo(),parRow.getIdto(),idtoUserTask,userRol,m_kba.getDefaultSession());
			commandList.add(commandPath);
			exeActions(commandList,/*buildBotoneraAHeredar(idTarget, idtoUserTask)*/filterControl.getResultPanel());
		}
		
		return mensajeRespuesta;
	}
	
	
//	public void setSolicitudAsRead(int ido,int idto,boolean read,Integer userRol,int idtoUserTask) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
//		//Creamos una nueva sesion para la confirmacion final ya que al parar la edicion y al poner el estado de realizado se pueden disparar reglas para hacer operaciones finales. Estas modificaciones
//		//tenemos que ser capaces de quitarlas si se produce un fallo en la coherencia o en base de datos. Teniendo una sesion hija como esta, podemos hacerlo sin perder datos anteriores. Ademas la
//		//sesion hija creada es del tipo forceParent=true para forzar a nuestra m_session a que haga commit.
//		DefaultSession sessionRead=m_kba.createDefaultSession(m_kba.getDDBBSession(),idtoUserTask,true,true,true,true,true);
//		boolean success=false;
//		try{
//			int idProp=Constants.IdPROP_SOLICITUDLEIDA;
//			DataProperty property=m_kba.getField(ido, idProp, userRol, idtoUserTask, sessionRead);
//			Value oldValue=property.getUniqueValue();
//			Value newValue=m_kba.getValueOfString("true", Constants.IDTO_BOOLEAN);
//			
//			if(!newValue.equals(oldValue)){
//			
//				int idoUserTask=m_kba.getIdoUserTask(idtoUserTask);
//				m_kba.setValue(/*property,*/idoUserTask,Constants.IdPROP_TARGETCLASS, m_kba.getValueOfString(String.valueOf(ido),idto),null/*, new session()*/,/*operation*/userRol,idtoUserTask,sessionRead);
//				//try{
//					m_kba.setUserTaskState(idoUserTask, Constants.IDO_INICIALIZANDO, userRol, idtoUserTask, sessionRead);
//					//System.err.println("Estado:"+m_kba.getProperty(idoParent, Constants.IdPROP_ESTADOREALIZACION, userRol, idtoUserTask, ses));
//				//}finally{
//					m_kba.setUserTaskState(idoUserTask, Constants.IDO_PENDIENTE, userRol, idtoUserTask, sessionRead);
//					
//					m_kba.setValue(ido, idProp, newValue, oldValue, userRol, idtoUserTask, sessionRead);
//					
//					m_kba.setUserTaskState(idoUserTask, Constants.IDO_REALIZADO, userRol, idtoUserTask, sessionRead);
//					
//					sessionRead.commit();
//			}else{
//				sessionRead.rollBack();
//			}
//			
//			success=true;
//		}finally{
//			if(!success){
//				sessionRead.setForceParent(false);
//				try{
//					sessionRead.rollBack();
//				}catch(Exception ex){
//					System.err.println("No se ha podido hacer rollback de la session");
//					ex.printStackTrace();
//				}
//			}
//		}
//	}



	/*   public boolean supportIndex( Integer index ){
	return m_ctxMenuMap.containsValue( index );
   }
	 */
	/*   public void newProcessEvent(  flowAction pp, ArrayList listaCtxFix ) throws DataErrorException{
       //pp.addPropertie(properties.GUI_id,index);
       m_indexRow.put( new Integer( pp.getCurrTask() ), pp );
   }
	 */
//	public void owningTaskEvent( owningAction pp, ArrayList listaCtxFix, boolean iniPro )
//	throws RemoteSystemException,CommunicationException{
//	//planningTaskEntry uo= (planningTaskEntry)m_indexRow.get( new Integer( pp.getCurrTask() ));
//	//29/03/06 Contexto ctx= m_md.getContext( new Integer(pp.getContext()) );
//	//Target tg= (Target)m_targetIndex.get( index );
//	//System.out.println("INDEX "+index);
//	ArrayList commandList= new ArrayList();
//	//29/03/06 commandList.add( new commandPath( pp.getTO_ctx(),pp.getIDO_ctx(),ctx.id,appControl.NEW_REQUEST));
////	Comentado al crear las clases que heredan de commandPath	commandList.add( new commandPath( 0,0,/*0*/pp.getCurrTask(),access.NEW,null));
//	/*	processType pt= m_md.getProcess(new Integer(pp.getProcessType()));
//	if( iniPro ){
//	/*ObjectProperty property=m_kba.getTarget(pp.getCurrTask(),pp.getUserRol());
//	access access=new access(property.getTypeAccess());*/
//	/*		switchObject(	/*new Integer(pp.getUserRol())*//*m_kba.getUserRols(pp.getCurrTask()),
//	null,/*buildBotoneraAHeredar(-1,-1),*/
//	/*				pt.getLabel(),
//	/*pp.getCurrProcess(),*/
//	/*pp.getCurrTask(),*/
//	/*listaCtxFix,*/
//	/*				commandList );
//	}
//	*/   }

	/*   public void taskTransitionEnd( flowAction pp ){
	   //debo actualizar el estado
       ;}
	 */
	/*   public owningAction findCurrentTask( int taskType, int currTask ){
	Iterator itr= m_targetsFilter.keySet().iterator();
	while( itr.hasNext() ){
		Integer id= (Integer)itr.next();
		Target tg= (Target)m_targetsFilter.get( id );
		if( tg.ctx.process!=0 && tg.ctx.task==taskType ){
			resultPanel rp= tg.getfilterControl().m_resultPanel;
			instance pars=rp.getDataRow( rp.PANEL_RESULTADOS, currTask );
			if( pars==null ) return null;
			else
			    return (owningAction)m_indexRow.get( new Integer( currTask ) );
		}
	}
	return null;
   }
	 */
	/* public void queryDocument( String title, String type, Element filter ) {
	m_appControl.queryDocument( title, type, filter );
   }
	 */
	int getMenuIndex( Target tgf ){
		return ((Integer)m_ctxMenuMap.get( new Integer(tgf.id) )).intValue();
	}




	/*private void eliminaVentana(){
		if( m_currForm!=null ){
			int lastPos=0;
			if(m_processPanelPool.size()>0){
				lastPos = m_processPanelPool.size()-1;
				m_processPanelPool.remove(lastPos);
			}
			if(m_processPanelPool.size()>=1){
				System.out.println("GUI REST PANEL "+m_currProcessGUI);
				PanelData pdata= (PanelData)m_processPanelPool.get(lastPos-1);
				m_currForm.setContentPane(pdata.panel);
				m_currForm.setTitle(pdata.title);
				m_currForm.pack();
				Singleton.getInstance().windowOpened(m_currForm);
				m_currForm.setLocationRelativeTo(getControlDialog());
				m_currForm.repaint();
			}else{
				System.out.println("GUI DEL WIN "+m_currProcessGUI);
				m_currForm.setVisible(false);
				m_currForm.dispose();
				m_currForm = null;
			}
		}
	}*/

	/*public void setMainDialog(JDialog dlg){
		m_currForm=dlg;
	}*/

	/* public void unlockObjects() throws SystemException{;}*/

	/*public JDialog getControlDialog(){
		return m_controlDialog;
	}*/

	/*public void addLockedInstance( int ido){;}
   public void removeLockedInstance( int ido ){;}*/

	/*    public void taskFiltering(long ini, long end) {
    }
	 */  
	private void exeActions(ArrayList<commandPath> commandList,ITableNavigation tableNavigation) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, ApplicationException, IncoherenceInMotorException, ParseException, AssignValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, InstanceLockedException, HeadlessException, SQLException, NamingException, JDOMException, AWTException, ActionException{
		//m_actionManager.addOperations(commandList);
//		KnowledgeBaseAdapter kba=m_kba;
//		
//		if(commandList.get(0).getSession() instanceof DDBBSession){
//			kba=Singleton.getInstance().buildKnowledgeBaseAdapterClone();
//			kba.getDDBBSession().setReusable(false);//Cuando se haga commit o rollback se hara dispose de la sesion
//			// Asignamos como sesion la del nuevo KnowledgeBaseAdapter
//			commandList.get(0).setSession(kba.getDDBBSession());
//			Singleton.getInstance().setKnowledgeBaseAdapter(kba.getKnowledgeBase(), kba);
//		}
//		m_actionManager.exeOperation(commandList.get(0),kba,tableNavigation,m_controlDialog,false);
//		
//		final KnowledgeBaseAdapter kbaThis=kba;
//		//Ya que estamos en un hilo distinto del AWT, necesitamos hacerlo en un invokeLater para que salga la ventana de la operacion antes de mostrar la ventana de la QuestionTask, si la hubiera
//		SwingUtilities.invokeLater(new Runnable(){
//
//			public void run() {
//				Singleton.getInstance().getQuestionTaskManager().process(kbaThis);
//			}
//			
//		});
		
		if(MemoryThread.getAvailableMemoryMegabyte()<120){
			Utils.forceGarbageCollector();
			OperationNotPermitedException ex=new OperationNotPermitedException("Se ha alcanzado el maximo de memoria permitido para abrir una nueva ventana");
			ex.setUserMessage("No está permitido abrir una nueva ventana ya que se está llegando al máximo de memoria disponible.\nIntente cerrar antes alguna de las ventanas de la aplicación o resetear las búsquedas para liberar memoria.");
			throw ex;
		}
		
		boolean isMultiWindow=Singleton.getInstance().isMultiWindow();
		KnowledgeBaseAdapter kba=m_kba;
		if(isMultiWindow){
			kba=Singleton.getInstance().buildKnowledgeBaseAdapterClone();
			
			kba.getDDBBSession().setReusable(false);//Cuando se haga commit o rollback se hara dispose de la sesion
			//Asignamos como sesion la del nuevo KnowledgeBaseAdapter
			//if(commandList.get(0).getSession() instanceof DDBBSession)
				commandList.get(0).setSession(kba.getDDBBSession());
			//else commandList.get(0).setSession(cloneKba.getDefaultSession());
			
			Singleton.getInstance().setKnowledgeBaseAdapter(kba.getKnowledgeBase(), kba);
		}
		commandList.get(0).getSession().setExecuteActionListener(this);
		
		kba.configServer(m_currTargetFilter.idtoUserTask);
		
		boolean success=false;
		try{
			m_actionManager.exeOperation(commandList.get(0),kba,tableNavigation,m_controlDialog,false);
			success=true;
		}finally{
			if(!success){
				// Si no se le ha hecho rollback lo hacemos aqui
				//TODO:Esto ocurria al intentar ejecutar una accion y que una regla o un fallo evite cargar el source
				if(!kba.isDispose()){
					Session session=kba.getDDBBSession();
					if(!session.isFinished()){
						session.rollBack();
						
						if(session.getKnowledgeBase().isDispose()){//Si el motor ya no existe hacemos dispose de KnowledgeBaseAdapter quitandolo tambien del Singleton
							Singleton.getInstance().removeKnowledgeBaseAdapter(session.getKnowledgeBase()).dispose();
						}
					}
				}
			}
		}
		
		//Ya que estamos en un hilo distinto del AWT, necesitamos hacerlo en un invokeLater para que salga la ventana de la operacion antes de mostrar la ventana de la QuestionTask, si la hubiera
		final KnowledgeBaseAdapter kbaThis=kba;
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				Singleton.getInstance().getQuestionTaskManager().process(kbaThis);
			}
			
		});
	}
	
	public void keyPressed(KeyEvent ke) {
		try{
			if (ke.getKeyCode() == KeyEvent.VK_ENTER)
				exeEvent(ke.getSource(),botoneraAccion.OPERATION_ACTION,m_currTargetFilter.id,null,botoneraAccion.EJECUTAR,null);
		}catch(Exception ex){
			//m_control.closeEvent();
			Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),ex,"Error al intentar ejecutar la operación");
			ex.printStackTrace();
		}
	}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}

	public Collection<Target> getTargets() {
		return m_targets.values();
	}
	
	public Target getTarget(int idTarget) {
		return m_targets.get(idTarget);
	}

	@Override
	public void requestExecuteAction(final ExecuteActionRequest executeAction) {
		final IExecuteActionListener thisThis=this;
		//En un invoke later para dar tiempo a que se termine lo que se esta ejecutando
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				Thread thread=new Thread(){

					@Override
					public void run() {
						int idtoUserTask=executeAction.getIdtoUserTask();
						String userTaskName=null;
						String rdn=executeAction.getRdn();
						m_controlDialog.disabledEvents();
						try{
							userTaskName=m_kba.getLabelUserTask(idtoUserTask);
							Integer userRol=null;
							ArrayList<Domain> sources=executeAction.getSources();
							HashMap<String,Object> mapParamValue=executeAction.getMapParamValue();
							System.err.println("Ejecutando la accion: idtoUserTask "+idtoUserTask+" userTaskName: "+userTaskName+" mapParamValue "+mapParamValue+" sources:"+sources);
							HashMap<Integer,Object> mapParamValueWithIdProp=new HashMap<Integer,Object>();
							for(String propName:mapParamValue.keySet()){
								mapParamValueWithIdProp.put(m_kba.getIdProp(propName),mapParamValue.get(propName));
							}
							
							ArrayList<commandPath> commandList=new ArrayList<commandPath>();
							AutomaticActionCommandPath commandPath=new AutomaticActionCommandPath(m_kba.getIdoUserTaskAction(idtoUserTask),rdn,sources,mapParamValueWithIdProp,thisThis,idtoUserTask,userRol,m_kba.getDDBBSession());
							commandList.add(commandPath);
				
							exeActions(commandList,null);
						}catch(Exception ex){
							ex.printStackTrace();
							Singleton.getInstance().getComm().logError(m_controlDialog.getComponent(),ex,"Error al intentar ejecutar la acción automática "+userTaskName);
						}finally{
							m_controlDialog.enabledEvents();
						}
					}
					
				};
				thread.run();
			}
		});
	}
}

