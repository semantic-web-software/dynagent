package dynagent.gui;

import gdev.gawt.utils.botoneraAccion;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

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
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.IdOperationForm;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.Utils;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.gestores.GestorContenedor;
import dynagent.framework.gestores.GestorInterfaz;
import tasks.ITaskCenter;
import dynagent.gui.tasks.monitor;
import dynagent.gui.utils.AboutUs;
import dynagent.gui.utils.BuyComponent;
import dynagent.gui.utils.CloseComponent;
import dynagent.gui.utils.ConfigurationComponent;
import dynagent.gui.utils.DataTransferComponent;
import dynagent.gui.utils.HelpWeb;
import dynagent.gui.utils.ImportComponent;
import dynagent.gui.utils.LicenseComponent;
import dynagent.gui.utils.ReportsComponent;
import dynagent.gui.utils.UserComponent;

public class toolBarControl{

	private communicator m_com=null;
	private monitor m_mon;
	private ITaskCenter m_taskCenter;
	private WindowComponent m_dialog;
	private boolean hideHistoryDDBB;
	//private GestorInterfaz gestorInterfaz;
	//private HashMap<Integer, OperationGUI> m_listaGUI;

	//private Graphics m_graphic;
	private KnowledgeBaseAdapter kba;
	private String subscription;
	private String email;
	private boolean licenseMode;
	
	public toolBarControl(ITaskCenter taskCenter,KnowledgeBaseAdapter kba,WindowComponent dialog,boolean hideHistoryDDBB,String subscription, String email, boolean licenseMode) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		m_com=kba.getServer();
		m_taskCenter=taskCenter;
		m_dialog=dialog;
		//gestorInterfaz=Singleton.getInstance().getGestorInterfaz();
		//m_listaGUI= new HashMap<Integer, OperationGUI>();
	
		//m_graphic=graphic;
		this.hideHistoryDDBB=hideHistoryDDBB;
		this.kba=kba;
		this.subscription=subscription;
		this.email=email;
		this.licenseMode=licenseMode;
		build();
	}

	public monitor getMonitor(){
		return m_mon;
	}

	private void build() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		GestorContenedor gestor=Singleton.getInstance().getGestorInterfaz().getZona(GestorInterfaz.ZONA_MODULOS);

//		KnowledgeBaseAdapter kba=Singleton.getInstance().getKnowledgeBase();
//		Iterator itr=kba.getAreasFuncionales();
//		/*Iterator itr=m_md.m_areasFuncionales.keySet().iterator();*/
//		//Iterator itr=new java.util.LinkedHashMap(m_md.m_areasFuncionales).keySet().iterator();
//
////		Si vemos que realmente no tenemos que hacer nada si no hay areas funcionales ponemos un while en lugar del dowhile y quitamos el if
//		if(itr.hasNext()){
//			HashMap<String, Integer> hm = new HashMap<String, Integer>();
//			ArrayList<String> labels = new ArrayList<String>();
//			while(itr.hasNext()){
//				int id=(Integer)itr.next();
//				String label=kba.getLabelAreaFuncional(id);
//				hm.put(label, id);
//				labels.add(label);
//			}
//			Collections.sort(labels);
//			Iterator<String> itl = labels.iterator();
//			do{
//				String label = itl.next();
//				int id=hm.get(label);
//
//				/*String id=(String)itr.next();
//				String label= m_md.getAreaFuncionalLabel( id );*/
//				//System.out.println("ID "+id+" LABEL "+label);
//				ImageIcon icon= m_com.getIcon(label);	
//				gestor.addItem(String.valueOf(id), null, Utils.normalizeLabel(label), icon.getIconWidth()>0?icon:null, null, null);
//
//				/*ImageIcon  icono=null;
//				Image img= m_com.getImage(label);
//				if(img!=null){
//					icono= new ImageIcon(img);
//					gestor.addItem(String.valueOf(id), null, Utils.normalizeLabel(label), icono.getIconWidth()>0?icono:null, null, null);
//				}else{
//					gestor.addItem(String.valueOf(id), null, Utils.normalizeLabel(label), null, null, null);
//				}*/
//
//				IdOperationForm idOperation=new IdOperationForm();
//				idOperation.setOperationType(botoneraAccion.OPERATION_ACTION);
//				idOperation.setButtonType(botoneraAccion.LAUNCH);
//
//				IdObjectForm idTarget=new IdObjectForm();
//				idTarget.setIdo(id);
//				idOperation.setTarget(idTarget);
//				String idStringOperation=idOperation.getIdString();
//
//				gestor.setEventoItem(String.valueOf(id), /*"ACTION:"+id+":"+botoneraAccion.LAUNCH*/idStringOperation, this);// Aqui indicariamos quien se ocupa del listener
//				/*add( botoneraAccion.subBuildBoton( 	null,
//									m_com,
//									null,
//									id,
//									"ACTION:"+id+":"+botoneraAccion.LAUNCH,
//									label,
//									this,
//									50,
//									25,
//									true ));*/
//			}while( itl.hasNext() );
//			gestor.setVisibleItems(null, true);
		/*}*//*else{
		   Singleton.getInstance().frameWorkEvent(this, null, botoneraAccion.LAUNCH);
	   }*/
		
		boolean configurationMode=m_com.getMode().equals(Constants.CONFIGURATION_MODE);
		/*m_mon= new monitor(stm,m_md,m_com,m_menuMap,0);*/
		JPanel navigator = Singleton.getInstance().getNavigation().getComponent();
		AboutUs aboutUs = null;
		CloseComponent close = null;
		ConfigurationComponent configuration = null;
		HelpWeb helpWeb = null;
		DataTransferComponent dataTransferComponent=null;
		UserComponent userComponent=null;
		ReportsComponent reportsComponent = null;
		ImportComponent importComponent = null;
		LicenseComponent licenseComponent = null;
		
		BuyComponent buyComponent = null;

		aboutUs = new AboutUs(m_dialog);
		close = new CloseComponent(m_dialog);
		
		boolean buyMode=!licenseMode && !Auxiliar.equals(subscription,Constants.CUSTOM_SUBSCRIPTION) && !Auxiliar.equals(subscription,Constants.DEMO_SUBSCRIPTION);
			
		if(buyMode){
			buyComponent = new BuyComponent(m_dialog,subscription,email);
		}
		if(licenseMode){
			licenseComponent = new LicenseComponent(m_dialog);
		}
		
		if(configurationMode){
			configuration=new ConfigurationComponent(m_dialog, kba);
		}else{
			dataTransferComponent=new DataTransferComponent(kba,m_dialog);
			reportsComponent = new ReportsComponent(kba,/*new Dimension(ConstantesGraficas.intScreenUtilX,ConstantesGraficas.intScreenUtilY),*/m_dialog);
			importComponent = new ImportComponent(m_dialog);
		}
		helpWeb = new HelpWeb(m_dialog);
		
		userComponent=new UserComponent(m_com.getUser());
		
		int ancho_monitor = ConstantesGraficas.intScreenUtilX - aboutUs.getPreferredSize().width - close.getPreferredSize().width - navigator.getPreferredSize().width - userComponent.getPreferredSize().width- helpWeb.getPreferredSize().width;
		
		if(buyMode){
			ancho_monitor -=  buyComponent.getPreferredSize().width;
		}
		
		if(licenseMode){
			ancho_monitor -=  licenseComponent.getPreferredSize().width;
		}
		
		if(configurationMode){
			ancho_monitor -= configuration.getPreferredSize().width;
		}else{
			ancho_monitor -= (reportsComponent.getPreferredSize().width + dataTransferComponent.getPreferredSize().width + importComponent.getPreferredSize().width);
		}
		m_mon= new monitor(0,m_taskCenter,kba,m_dialog, ancho_monitor, hideHistoryDDBB);
		
		
		/*JButton buttonReports=new JButton();
		buttonReports.addActionListener(this);
		ImageIcon img=m_com.getIcon("viewReports");
		buttonReports.setIcon(img);
		buttonReports.setPreferredSize(new Dimension(ConstantesGraficas.intToolY-3,ConstantesGraficas.intToolY-3));
		JPanel panelButtonReports=new JPanel();
		panelButtonReports.add(buttonReports);*/
		//gestor.addItem("calendar", null, Utils.normalizeLabel("calendario de tareas"), m_com.getIcon(null,"calendarTasks", 20, 20), null, null);
		//gestor.setVisibleItems(null, true);

//		gestor.addPanel("navigator", navigator, -1, null, null);
//		gestor.addPanel("monitor", m_mon, -1, null, null);
//		gestor.addPanel("reports", reportsComponent, -1, null, null);
//		gestor.addPanel("aboutUs", aboutUs, -1, null, null);
			
		JPanel panelButtons=new JPanel();
		panelButtons.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
		if(configurationMode){
			panelButtons.add(configuration);
		}else{
			panelButtons.add(reportsComponent);
			panelButtons.add(dataTransferComponent);
			panelButtons.add(importComponent);
		}
		
		if(buyMode){
			panelButtons.add(buyComponent);
		}
		if(licenseMode){
			panelButtons.add(licenseComponent);
		}
		
		panelButtons.add(helpWeb);
		panelButtons.add(aboutUs);
		//panelButtons.add(close);
		
		JPanel panelAux=new JPanel(new FlowLayout(FlowLayout.CENTER,0,0));
		panelAux.add(panelButtons);
		panelAux.add(userComponent);
		
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout(0,0));
		panel.add(navigator,BorderLayout.WEST);
		panel.add(panelAux,BorderLayout.EAST);
		panel.add(m_mon,BorderLayout.CENTER);
		
		//panel.setPreferredSize(ConstantesGraficas.dimTool);
		
		gestor.addPanel("toolbar", panel, -1, null, null);
		gestor.setVisiblePanels(null, true);
		/*add(m_mon);*/

	}
}
