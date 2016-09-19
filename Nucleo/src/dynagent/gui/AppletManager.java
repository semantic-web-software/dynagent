package dynagent.gui;

import gdev.gawt.utils.botoneraAccion;
import gdev.gbalancer.GViewBalancer;
import gdev.gen.AssignValueException;
import gdev.gen.EditionTableException;
import gdev.gen.GConfigView;
import gdev.gen.GConst;
import gdev.gen.IComponentListener;
import gdev.gen.NotValidValueException;
import gdev.gfld.GValue;

import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import javax.naming.NamingException;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.muntjak.tinylookandfeel.Theme;
import dynagent.common.Constants;
import dynagent.common.communication.UserConstants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.message;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.SwingWorker;
import dynagent.common.utils.UserMessageControl;
import dynagent.common.utils.Utils;
import dynagent.common.utils.jdomParser;
import dynagent.framework.ConstantesGraficas;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.actions.ActionManager;
import dynagent.gui.forms.utils.ColumnsTable;
import dynagent.gui.forms.utils.GroupComponents;
import dynagent.gui.forms.utils.HighlightedComponents;
import dynagent.gui.forms.utils.IColumnProperties;
import dynagent.gui.forms.utils.IEssentialProperties;
import dynagent.gui.forms.utils.IGroupProperties;
import dynagent.gui.forms.utils.IOrderProperties;
import dynagent.gui.forms.utils.OrderComponents;
import dynagent.gui.tasks.TaskCenter;
import dynagent.gui.utils.CardMedComponent;
import dynagent.gui.utils.HelpComponent;
import dynagent.gui.utils.ICardMed;
import dynagent.gui.utils.IListenerUtask;
import dynagent.gui.utils.ListenerMenu;
import dynagent.gui.utils.LoginComponent;
import dynagent.gui.utils.MemoryThread;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.ruleengine.src.sessions.SessionController;

public class AppletManager {
	
	public long startTime=0;
	public communicator m_com =null;
	public communicator m_comGlobal =null;
	public String m_login, m_pwd, m_user, m_userRols;
	public Integer m_mode;
	public JDialog m_loginDialog;
	public WindowComponent m_window;
	public int m_idoUser=0;
	public int m_userRol=0;
	private JLabel m_logo;
	private GestorInterfaz gestorInterfaz;
	private TaskCenter m_taskCenter;
	private LoginComponent loginComponent;
	private WindowComponent windowLogin;
	private dynaApplet applet;
	private boolean hideHistoryDDBB;
	private boolean multiWindow;
	private int business;
	private boolean debug;
	private ArrayList<String> rules;
	private ArrayList<String> configurationRules;
	private String subscription;
	private String email;
	private boolean licenseMode;
	private boolean printRules;
	private URL globalURL;
	private String globalLogin;
	private String globalPassword;
	private String globalDelegacion;
	private String businessName;
	
	public String reportsRoutedToMainServer;
	public int port_backup;
	public String host_backup=null;
	
	private static int CONFIGURATION_MODE=1;
	private static int BUSINESS_MODE=2;


	public AppletManager(dynaApplet applet,boolean hideHistoryDDBB,boolean multiWindow,int business,String businessName, boolean debug,boolean licenseMode,String theme,ArrayList<String> rules,ArrayList<String> configurationRules,URL globalURL, String globalDelegacion) {
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel( new com.nilo.plaf.nimrod.NimRODLookAndFeel());
			//UIManager.setLookAndFeel("com.pagosoft.plaf.PgsLookAndFeel");
			//UIManager.setLookAndFeel("org.jvnet.substance.SubstanceLookAndFeel");
			if(theme!=null)
				Theme.DEFAULT_THEME=theme;
			UIManager.setLookAndFeel("de.muntjak.tinylookandfeel.TinyLookAndFeel"); 
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		this.applet=applet;
		this.hideHistoryDDBB=hideHistoryDDBB;
		this.multiWindow=multiWindow;
		this.business=business;
		this.businessName=businessName;
		this.debug=debug;
		this.rules=rules;
		this.configurationRules=configurationRules;
		this.licenseMode=licenseMode;
		this.printRules=!licenseMode;
		this.globalURL=globalURL;
		this.globalDelegacion=globalDelegacion;
		applet.getRootPane().putClientProperty("defeatSystemEventQueueCheck",
				Boolean.TRUE);
		
		Singleton.getInstance().setMessagesControl(new UserMessageControl(applet,Singleton.getInstance().getGraphics(),applet.getWidth()));
		Singleton.getInstance().setDebugLog(new DebugLog());
		Singleton.getInstance().setQuestionTaskManager(new QuestionTaskManager());
		
		//Ponemos como propiedad el gestor por defecto para evitar que cada vez que se trabaja con xml en la aplicacion, java mire en el servidor si existe un archivo de definición
		System.setProperty( "javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl" );
	}
	
	public void loginPrompt(String valueLogin,String valuePassword,String imageName,String modes) throws ParseException, AssignValueException, MalformedURLException{
		this.m_com=null;
		this.m_comGlobal=null;
		m_login=valueLogin;
		m_pwd=valuePassword;
		ActionListener handler = new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					//System.err.println("Event key");
					java.util.StringTokenizer stk = new StringTokenizer(e. getActionCommand(), ":");
					//String targetType = "";
					//int target = 0;
					int botonType = 0;
					/*try {*/
					//targetType = stk.nextToken();
					stk.nextToken();
					String idButton = stk.nextToken();
					//System.out.println("IDBUTT " + idButton);
					/*String[] res = idButton.split(",");
                        if (res.length == 2) {
                            botonType = Integer.parseInt(res[1]);
                        } else
                            botonType = Integer.parseInt(res[0]);*/
					botonType=Integer.parseInt(idButton);

					/*} catch (NoSuchElementException ne) {
                        Singleton.getInstance().getComm().logError(ne);
                    }*/
					if (botonType == botoneraAccion.CANCEL) {
						/*m_loginDialog.setVisible(false);
						m_loginDialog.dispose();
						m_loginDialog = null;
						return;*/
						try {
							//Esto dara excepcion cuando no estemos en un navegador. Lo hacemos para solo cerrar la aplicacion si estamos usando el main ya que en el navegador no podemos cerrarselo y se queda pillado.
							//Asi que si estamos en el navegador hacemos que la funcionalidad sea borrarle los datos escritos, recargando la pagina
							applet.getAppletContext().showDocument(new URL("javascript:window.location.reload();"));
						} catch (Exception e2) {
							System.exit(0);
						}
						
					}else{
						doUserValidation();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
					Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor(applet),ex,"Error al iniciar la aplicación");
				}
			}
		};

		IComponentListener controlValues=new IComponentListener(){
			public boolean addValueField(String id,Object value,int valueCls){
				IdObjectForm idObjectForm=new IdObjectForm(id);
				Integer ido=idObjectForm.getIdo();
				//Solo tratamos mode ya que el resto no entra nunca por aqui
				if(ido==3){
					m_mode=(Integer)value;
					return true;
				}
				return true;
			}

			public boolean removeValueField(String id,Object value,int valueCls){
				IdObjectForm idObjectForm=new IdObjectForm(id);
				Integer ido=idObjectForm.getIdo();
				//Solo tratamos mode ya que el resto no entra nunca por aqui
				if(ido==3){
					m_mode=null;
					return true;
				}
				return false;
			}

			public boolean setValueField(String id,Object value,Object valueOld,int valueCls,int valueOldCls){
				IdObjectForm idObjectForm=new IdObjectForm(id);
				Integer ido=idObjectForm.getIdo();

				if(ido==1)
					m_login=(String)value;
				else if(ido==2)
					m_pwd=(String)value;
				else if(ido==3)
					m_mode=(Integer)value;
				return true;
			}

			public Integer newRowTable(String id,Integer idtoRow){
				return null;
			}

			public boolean removeRowTable(String id,int idoRow,int idtoRow){
				return false;
			}

			public void startEditionTable(String idTable,Integer idoRow,boolean pastingRow) {
				// TODO Auto-generated method stub
				
			}

			public void stopEditionTable(String idTable,Integer idoRow,HashSet<Integer> idosEdited,boolean pastingRow) {
				// TODO Auto-generated method stub
			}

			public void cancelEditionTable(String idTable, Integer idoRow) throws EditionTableException {
				// TODO Auto-generated method stub
				
			}

			public void editInForm(int idoParent,int idoToEdit) throws EditionTableException {
				// TODO Auto-generated method stub
				
			}

			public Integer newSubRowTable(String idParentColumn,Integer idtoRow) throws AssignValueException, NotValidValueException {
				return null;
			}

			public boolean isNewCreation(int ido) {
				// TODO Auto-generated method stub
				return false;
			}

			public LinkedHashMap<String, Integer> getPossibleTypeForValue(String idParent, Object value, Integer valueCls) {
				// TODO Auto-generated method stub
				return null;
			}

			public Boolean isNullableForRow(Integer idoParent, Integer ido, String idColumn) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void showInformation(Integer ido, Integer idProp) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setProcessingCopyRowTable(boolean processing) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isAllowedConfigTables() {
				// TODO Auto-generated method stub
				return true;
			}
		};

		KeyAdapter handlerKey=new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if (ke.getKeyCode() == KeyEvent.VK_ENTER && !(ke.getSource() instanceof AbstractButton)/*Ya se encarga el action performed en este caso, si no se logaria dos veces*/)
					try{
						doUserValidation();
					} catch (Exception ex) {
						ex.printStackTrace();
						Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor(applet),ex,"Error al iniciar la aplicación");
					}
		}};
		windowLogin=new WindowComponent(applet,null);
		windowLogin.setTitle(Utils.normalizeLabel("SISTEMA DYNAGENT®"));
		ArrayList<GValue> modesList=new ArrayList<GValue>();
	    if(modes.contains("Configuración")){
	    	modesList.add(new GValue(CONFIGURATION_MODE,"Configuración"));
	    	m_mode=CONFIGURATION_MODE;
	    }
	    if(modes.contains("Negocio")){
	    	modesList.add(new GValue(BUSINESS_MODE,"Negocio"));
	    	m_mode=BUSINESS_MODE;
	    }
		loginComponent=new LoginComponent(applet,m_login,m_pwd,m_mode,modesList,imageName,controlValues,handler,handlerKey); 
		GConst.addShortCut(null, loginComponent, GConst.RULES_DEBUG, GConst.RULES_DEBUG_MODIFIERS, "RulesDebug", JComponent.WHEN_IN_FOCUSED_WINDOW, new AbstractAction(){

			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent arg0) {
				printRules=!printRules;
				System.err.println("Depuracion reglas:"+printRules);
			}
			
		});
		applet.setContentPane(loginComponent);
		applet.validate();
		applet.repaint();

		// Pedimos el foco para el applet ya que en Chrome por ejemplo, a veces aparece en blanco si se abre directamente entry.jsp, teniendo que hacer dobleclick para que se viera
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();//Esto en vez de las dos lineas siguientes ya que en stand-alone no cogia el foco el primer campo
				//applet.requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
				//applet.requestFocusInWindow();
			}
			
		});
	}

	private void doUserValidation() throws RemoteSystemException, CommunicationException, SystemException, NotFoundException, IncoherenceInMotorException, ParseException, DataErrorException, EngineException, ApplicationException, IncompatibleValueException, CardinalityExceedException, InstanceLockedException, SQLException, NamingException, JDOMException, OperationNotPermitedException, MalformedURLException{
		windowLogin.disabledEvents();
		boolean success=false;
		try{
			if(m_login==null || m_login.length()==0 || m_pwd==null || m_pwd.length()==0 || m_mode==null || m_mode==0){
				Singleton.getInstance().getMessagesControl().showMessage("DEBE RELLENAR TODOS LOS CAMPOS",applet);		
				return;
			}
	
			loginComponent.startProgressBar("Validando usuario");
			
			buildConnection(m_login,m_pwd,m_mode==1);
			if(validarUsuario(m_com,m_login,m_pwd,false)){
	//			m_loginDialog.setVisible(false);
	//			m_loginDialog.dispose();
	//			m_loginDialog=null;
				
				loginComponent.startProgressBar("Iniciando la aplicación");
				//m_com.start();
		    	SwingWorker barWorker=new SwingWorker(){
		 			public Object construct(){
		 				try{
		 					initApplication();
		 				} catch (Exception ex) {
		 					ex.printStackTrace();
							Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor(applet),ex,"Error al iniciar la aplicación");
						} finally{
							loginComponent.stopProgressBar();
							windowLogin.enabledEvents();
						}
		 				return null;
		 			}
		    	};
				barWorker.start();
				success=true;
			}
		}finally{
			if(!success){
				loginComponent.stopProgressBar();
				windowLogin.enabledEvents();
			}
		}


	}

	public Point getGUICenter(){
		GraphicsConfiguration gc = applet.getGraphicsConfiguration();
		Rectangle marco = gc.getBounds();
		return new Point((int)(marco.getWidth()/ 2),(int) (marco.getHeight() / 2));
	}
//	public void paint(Graphics g){
//		super.paint(g);
//		if(m_dialog instanceof Window ){
//			toFrontTree(m_dialog);
//		}
//	}

	private void buildConnection(String login,String password,boolean configurationMode){
		m_com=new communicator(applet,Singleton.getInstance().getMessagesControl(),Singleton.getInstance().getDebugLog(),Singleton.getInstance().getCodeBaseServer(),Singleton.getInstance().getCodeBaseJar(),60000,3,business,businessName,login,password,configurationMode);
		m_com.setUrlBackup(host_backup, port_backup, reportsRoutedToMainServer);
		m_com.setSmallImageHeight(GConfigView.smallImageHeight);
		Singleton.getInstance().setCommunicator(m_com);
	}
	
	private void buildGlobalConnection(String login,String password,URL serverUrl,boolean configurationMode){
		//TODO global business deberia ser parametrizable, de momento mejor presuponer 1 que el mismo de esta instalacion
		m_comGlobal=new communicator(applet,Singleton.getInstance().getMessagesControl(),Singleton.getInstance().getDebugLog(),serverUrl,Singleton.getInstance().getCodeBaseJar(),60000,3,1,businessName,login,password,configurationMode);
		m_comGlobal.setSmallImageHeight(GConfigView.smallImageHeight);
		Singleton.getInstance().setGlobalCommunicator(m_comGlobal);
	}

	private boolean validarUsuario(communicator comm,String login,String password,boolean overwrite) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		message eRes=null;
		try{
			eRes=comm.serverLogin(overwrite);
		}catch(RemoteSystemException ex){
			ex.printStackTrace();
			if(ex.getError()==message.ERROR_USER_ALREADY_LOGGED){
				Object[] options = {"Aceptar", "Cancelar"};
				int res = Singleton.getInstance().getMessagesControl().showOptionMessage(
						ex.getUserMessage()+"\n¿Desea sobreescribir la sesión existente?",
						Utils.normalizeLabel("Usuario ya existente"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,
						null,
						options,
						options[1],applet);
				if (res == JOptionPane.YES_OPTION)
					eRes=comm.serverLogin(true);
				else return false;
			}else if(ex.getError()==message.ERROR_LOGIN){
				Singleton.getInstance().getMessagesControl().showErrorMessage(ex.getUserMessage(),applet);
				return false;
			}else{
				throw ex;
			}
		}
		if( eRes==null ) return false;
		if(!eRes.getSuccess()){
			Singleton.getInstance().getMessagesControl().showErrorMessage("USUARIO O PASSWORD INCORRECTO",applet);		
			return false;
		}else{
			m_user= eRes.getUser();
			m_userRols= eRes.getUserRols();
			/*try{
		                m_userName = eRes.getStrDirectAttribute(helperConstant.TAPOS_NOMBRE);
			    }catch(NoSuchFieldException fe){
			    	System.out.println("no hay nombre");
			    }*/
			/*try{
			 */        business = eRes.getBusiness();
			 /*        m_idoUser = eRes.getIntPropertie(properties.id);
			    }catch(NoSuchFieldException fe){
			    	Singleton.getInstance().getComm().logError(applet,fe, "Error de datos:" + fe.getMessage());
			    	return false;
			    }*/
			try {
				subscription = eRes.getStrDirectAttribute(UserConstants.SUBSCRIPTION_TYPE);
				comm.setSubscription(subscription);
				email = eRes.getStrDirectAttribute(UserConstants.EMAIL);
			} catch (NoSuchFieldException e) {
				System.err.println("INFO: Sin atributos de subscripción o email asociado en customer");
			}
			try{
				int days=Integer.valueOf(eRes.getStrDirectAttribute(UserConstants.EXPIRED_SUBSCRIPTION_DAYS));
				if(days<=366){//Solo lo mostramos si queda un año o menos de licencia (teniendo en cuenta bisiestos)
					loginComponent.setLicenseDays(days);
				}
			} catch (NoSuchFieldException e) {
				System.err.println("INFO: Sin atributo de fin de subscripción");
			}
			 return true;
		}
		/*}catch(Exception e){
		    e.printStackTrace();
		    Singleton.getInstance().getComm().logError(applet,e, "Error al validar usuario:" + e.getMessage());
		    return false;
		}*/
	}

	public void initApplication() throws RemoteSystemException,CommunicationException, SystemException, NotFoundException, IncoherenceInMotorException, ParseException, DataErrorException, EngineException, ApplicationException, IncompatibleValueException, CardinalityExceedException, InstanceLockedException, SQLException, NamingException, JDOMException, OperationNotPermitedException, MalformedURLException, AssignValueException{
		Singleton.getInstance().setApplet(applet);
		Singleton.getInstance().setCommunicator(m_com);
		
//		AppletContext a = getAppletContext();
//	      URL url = new URL("http://192.168.1.3:8080/dyna/dynagent.html");
//	      a.showDocument(url,"_blank");
	      
		Insets insetsDialog=applet.getInsets();
		//System.err.println("Insets:"+insetsDialog);
		int bordersWidth=insetsDialog.left+insetsDialog.right;
		int bordersHeight=insetsDialog.top+insetsDialog.bottom;
		int split = ConstantesGraficas.sizeDivisorSplitPane;
		
		ConstantesGraficas.intScreenUtilX 		 = applet.getSize().width/*ConstantesGraficas.intScreenX*/ - bordersWidth;
		ConstantesGraficas.intScreenUtilY 		 = applet.getSize().height/*ConstantesGraficas.intScreenY*/ - bordersHeight;
		ConstantesGraficas.intMenuX				 = (int)(ConstantesGraficas.intScreenUtilX * 0.15);
		ConstantesGraficas.intStatusBar			 = (int)GViewBalancer.getRowHeightS(Singleton.getInstance().getGraphics());
		
		//Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		
		ConstantesGraficas.dimScreenJDialog 	 = new Dimension(applet.getSize().width/*ConstantesGraficas.intScreenX*/, 		ConstantesGraficas.intScreenUtilY);
		ConstantesGraficas.dimFormManager 		 = new Dimension(ConstantesGraficas.intScreenUtilX, 	ConstantesGraficas.intScreenUtilY - bordersHeight);
		ConstantesGraficas.dimZonaTrabajoExtended= new Dimension(ConstantesGraficas.intScreenUtilX - split, 	ConstantesGraficas.intScreenUtilY - bordersHeight - ConstantesGraficas.intStatusBar - ConstantesGraficas.intToolY);
		ConstantesGraficas.dimZonaTrabajo 		 = new Dimension(ConstantesGraficas.intScreenUtilX - ConstantesGraficas.intMenuX-split, 	ConstantesGraficas.intScreenUtilY - bordersHeight - ConstantesGraficas.intStatusBar - ConstantesGraficas.intToolY);
		ConstantesGraficas.dimMenu 				 = new Dimension(ConstantesGraficas.intMenuX,		ConstantesGraficas.intScreenUtilY - bordersHeight - ConstantesGraficas.intStatusBar - ConstantesGraficas.intToolY);
		ConstantesGraficas.dimTool 				 = new Dimension(ConstantesGraficas.intScreenUtilX, 	ConstantesGraficas.intToolY);
		ConstantesGraficas.dimStatus 			 = new Dimension(ConstantesGraficas.intScreenUtilX, 	ConstantesGraficas.intStatusBar);
		
		/*System.err.println("ConstantesGraficas.intScreenUtilX "+ConstantesGraficas.intScreenUtilX);
		System.err.println("ConstantesGraficas.intScreenUtilY "+ConstantesGraficas.intScreenUtilY);
		System.err.println("ConstantesGraficas.intMenuX "+ConstantesGraficas.intMenuX);
		System.err.println("ConstantesGraficas.dimScreenJDialog "+ConstantesGraficas.dimScreenJDialog);
		System.err.println("ConstantesGraficas.dimFormManager "+ConstantesGraficas.dimFormManager);
		System.err.println("ConstantesGraficas.dimZonaTrabajoExtended "+ConstantesGraficas.dimZonaTrabajoExtended);
		System.err.println("ConstantesGraficas.dimZonaTrabajo "+ConstantesGraficas.dimZonaTrabajo);
		System.err.println("ConstantesGraficas.dimMenu "+ConstantesGraficas.dimMenu);
		System.err.println("ConstantesGraficas.dimTool "+ConstantesGraficas.dimTool);
		System.err.println("ConstantesGraficas.dimStatus "+ConstantesGraficas.dimStatus);
		*/
		
		UIManager.put("TabbedPane.contentBorderInsets", new Insets( 1, 0, 0, 0) );
		
		//m_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		m_com.setIdoUser(m_idoUser);
		m_com.setBusiness(business);
		
		KnowledgeBaseAdapter kba=buildKnowledgeBaseAdapter();
		
		if(globalURL!=null && kba.hasGlobalUtasks()){
			connectMainServer(kba);
		}
		
		m_window=new WindowComponent(applet,kba);
		m_window.setMainDialog(m_window);
		
		gestorInterfaz=new GestorInterfaz(m_com);
		Singleton.getInstance().setGestorInterfaz(gestorInterfaz);
		
		if(multiWindow){
			Singleton.getInstance().setKnowledgeBaseAdapterOriginal(kba);
		}
		Singleton.getInstance().setKnowledgeBaseAdapter(kba.getKnowledgeBase(), kba);
		
		Singleton.getInstance().setStatusBar(buildStatusBar(ConstantesGraficas.dimStatus));
		Singleton.getInstance().setActionManager(buildActionManager(ConstantesGraficas.dimFormManager));
		Singleton.getInstance().setNavigation(new NavigationControl());
		
		m_taskCenter=null;//new TaskCenter(kba,m_window);
		toolBarControl toolBar= new toolBarControl(m_taskCenter,kba, m_window,hideHistoryDDBB,subscription,email,licenseMode);
		//m_taskCenter.addTasksListener(m_calendar.getEventos());
//		m_taskCenter.addTasksListener(toolBar.getMonitor());
//		m_taskCenter.askServerTasks();

		final MenuControl menu=new MenuControl(kba,m_window);
		
		setConfigParams(kba);
		
		
		//m_dialog.setVisible(true);
		//JPanel panel=new JPanel();
		//panel.setLayout(new BorderLayout(0,0));
		//panel.add(gestorInterfaz.getComponente(),BorderLayout.CENTER);
		//this.setContentPane(panel);
		m_window.setTitle(Utils.normalizeLabel("SISTEMA DYNAGENT®   -   Usuario: "+m_login));
		if(m_logo!=null)
			applet.getContentPane().add(m_logo);
		applet.setContentPane(gestorInterfaz.getComponente());
		//System.err.println(SwingUtilities.getWindowAncestor(gestorInterfaz.getComponente()));
		//this.setMinimumSize(ConstantesGraficas.dimScreenJDialog);
		//this.setPreferredSize(ConstantesGraficas.dimScreenJDialog);
		applet.validate();
		applet.repaint();
		applet.setVisible(true);
		
		if(debug)
			Debug.getInstance().init();
		
		int width=450;
		HelpComponent helpComponent=new HelpComponent(kba,m_com,new Dimension(width,applet.getHeight()),applet);
		Singleton.getInstance().setHelpComponent(helpComponent);
		
		MemoryThread memory=new MemoryThread();
		memory.start();
		
		//Pedimos el foco para el applet ya que si no no funcionan las teclas shortcut hasta que no hacemos click sobre algun componente del applet
		SwingUtilities.invokeLater(new Runnable(){

			public void run() {
				applet.requestFocus();//Aunque es recomendable utilizar requestFocusInWindow, usamos tambien requestFocus ya que requestFocusInWindow no funciona con algunos navegadores porque le quitan el foco al applet
				applet.requestFocusInWindow();
				menu.requestFocusInWindow();//Hacemos que se situe en el menu de la izquierda
			}
			
		});
		
		Singleton.getInstance().getQuestionTaskManager().addCurrent(null,m_window, kba);
		Singleton.getInstance().getQuestionTaskManager().process(kba);
		
		//kba.getSizeMotor();
	}

	private void setConfigParams(KnowledgeBaseAdapter kba) {
		Object value=kba.getConfigParam("UI_sonido_intro");
		if(value!=null){
			GConfigView.beepOnTables=(Boolean)value;
		}
		value=kba.getConfigParam("UI_salto_boton_aceptar_tras_intro");
		if(value!=null){
			GConfigView.enterFocusOnAcceptButton=(Boolean)value;
		}
		value=kba.getConfigParam("UI_redondeo_decimales");
		if(value!=null){
			GConfigView.redondeoDecimales=((Double)value).intValue();
		}
	}

	public void closeApplication(){
		if( m_window!=null ){
			m_window.getComponent().setVisible(false);
			m_window.dispose();
			//m_dialog = null;
		}
		applet.destroy();
	}

	public void sendMessageDisconnection(){
		try {
			//if(m_dialog.isDisplayable()){//Sera displayable si se cierra el navegador, en vez de la aplicacion
				//if(m_empresa!=null)
				if(m_user!=null){
					m_com.serverDisconnection(m_login);
					if(m_comGlobal!=null){
						m_comGlobal.serverDisconnection(globalLogin);
					}
				}
			//}
		} catch (Exception ex) {
			ex.printStackTrace();
			try{
				Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor(applet),ex,"Error al desconectar la aplicación");
			}catch(Exception ex2){
				ex2.printStackTrace();
			}
		} finally{
			m_user=null;
		}
	}


	private void toFrontTree(Window dlg){
		dlg.toFront();
		Window[] ws=dlg.getOwnedWindows();
		for( int i=0; i< ws.length; i++ ){
			Window child=(Window)ws[i];
			toFrontTree( child );
		}
	}
//	public JDialog getControlDialog(){
//	return m_dialog;
//	}

//	private KnowledgeBaseAdapter buildKnowledgeBaseAdapter() throws CommunicationException,RemoteSystemException,SystemException,NotFoundException, IncoherenceInMotorException, EngineException, ApplicationException, IncompatibleValueException, CardinalityExceedException{
//	m_com.buildMetadata(m_empresa);
//	IKnowledgeBaseInfo ik=m_com.getKnowledgeBase();
//	//TODO Ahora orderProperties lo gestionamos en KnowledgeBaseAdapter. Pensar si seria mas logico que estuviera en el Singleton directamente
//	IOrderProperties orderComponents=new OrderComponents(m_com.getKnowledgeBase().getOrderProperties());//buildOrderComponents();
//	return new KnowledgeBaseAdapter(/*new Adapter(*/m_com.getKnowledgeBase()/*)*/,orderComponents,m_com.m_user,((DocDataModel)ik).getDefaultSession(),((DocDataModel)ik).getRootSession());
//	}

	private KnowledgeBaseAdapter buildKnowledgeBaseAdapter() throws CommunicationException,RemoteSystemException,SystemException,NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, ApplicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		//System.out.println("INICIO EMPRESA " + empresa);
		
		loginComponent.changeMessageProgressBar("Inicializando datos");
		
		Element metaDataXML=m_com.serverGetMetaData();
		
		loginComponent.changeMessageProgressBar("Procesando datos de negocio");
		
		//System.out.println(jdomParser.returnXML(metaDataXML));
		ArrayList<String> rules=m_mode==BUSINESS_MODE?this.rules:this.configurationRules;
		IKnowledgeBaseInfo ik = RuleEngineFactory.getInstance().createRuler(metaDataXML,m_com.getBusiness(),m_com,Constants.RULER,m_com.getUser(),rules,Singleton.getInstance().getDebugLog(),
		new IMessageListener(){
			
			@Override
			public void sendMessage(String message) {
				Singleton.getInstance().getMessagesControl().showMessage(message, applet);
			}
			
			@Override
			public Boolean sendQuestion(String message, boolean initialSelectionIsYes) {
				Object[] options={"Sí","No"};
				int res= Singleton.getInstance().getMessagesControl().showOptionMessage(
						message,
						Utils.normalizeLabel("Pregunta"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE,null, options, (initialSelectionIsYes?options[0]:options[1]), applet);
				return res == JOptionPane.YES_OPTION;
			}
			
		},Singleton.getInstance().getQuestionTaskManager(),printRules);
		
		loginComponent.changeMessageProgressBar("Construyendo interfaz gráfica de usuario");
		
		IOrderProperties orderComponents=new OrderComponents(ik.getOrderProperties());//buildOrderComponents();
		IColumnProperties columnsTable=new ColumnsTable(ik.getColumnProperties());
		ICardMed cardmedComp= new CardMedComponent(ik.getListCM());
		IGroupProperties groupComponents=new GroupComponents(ik.getGroupsProperties(),orderComponents);
		IEssentialProperties highlightedComponents=new HighlightedComponents(ik.getEssentialProperties());
		IListenerUtask listenerMenu=new ListenerMenu(ik.getListenerUtasks());
		//IKnowledgeBaseInfo br=new BusinessRestrictionAdapter(m_com.getKnowledgeBase(),orderComponents,m_com,m_com.m_empresa,df,sessRoot);
		//IOrderProperties ior=(IOrderProperties)br;

		KnowledgeBaseAdapter kba=new KnowledgeBaseAdapter(/*br*/ik,orderComponents,cardmedComp, columnsTable/*ior*/,groupComponents,highlightedComponents,listenerMenu,m_com.getUser(),m_userRols,m_com);
		cardmedComp.setKnowledgeBase(kba);
		return kba;
	}

	private StatusBar buildStatusBar(Dimension dim){
		StatusBar statusBar=new StatusBar("("+Utils.normalizeLabel("Inicio")+")",null,dim);
		statusBar.setAccion(Utils.normalizeLabel("Módulos cargados"));
		statusBar.revalidate();
		statusBar.repaint();

		return statusBar;
	}

	private ActionManager buildActionManager(Dimension dim){
		ActionManager actionManager=new ActionManager(dim,m_window,multiWindow);
		return actionManager;
	}
	
	public void setConfigurationMode(boolean configurationMode) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		m_com.setConfigurationMode(configurationMode);
		m_mode=configurationMode?CONFIGURATION_MODE:BUSINESS_MODE;
	}

	public WindowComponent getWindow() {
		return windowLogin;
	}

	/*private OrderComponents buildOrderComponents(){
    	OrderComponents orderComponents=new OrderComponents(m_com.getKnowledgeBase().getOrderProperties());
    	return orderComponents;
    }*/
	
	private void connectMainServer(KnowledgeBaseAdapter kba) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		int idoApplication=kba.getIndividuals(Constants.IDTO_APPLICATION, Constants.LEVEL_INDIVIDUAL, false).next();
		ObjectProperty delegationProperty=kba.getChild(idoApplication, Constants.IDTO_APPLICATION, kba.getIdProp("delegación"), null, null, kba.getDefaultSession());
		if(!delegationProperty.getValues().isEmpty()){
			ObjectValue valueDelegation=kba.getValue(delegationProperty);
			int idoDelegation=valueDelegation.getIDOIndividual();

			ObjectProperty businessProperty=kba.getChild(idoApplication, Constants.IDTO_APPLICATION, kba.getIdProp("mi_empresa"), null, null, kba.getDefaultSession());
			if(!businessProperty.getValues().isEmpty()){
				ObjectValue valueBusiness=kba.getValue(businessProperty);
				int idoBusiness=valueBusiness.getIDOIndividual();
				int idtoBusiness=valueBusiness.getIDTOIndividual();
				ObjectProperty mainDelegationProperty=kba.getChild(idoBusiness, idtoBusiness, kba.getIdProp("delegacion_central"), null, null, kba.getDefaultSession());
				if(!mainDelegationProperty.getValues().isEmpty()){
					ObjectValue valueMainDelegation=kba.getValue(mainDelegationProperty);
					int idoMainDelegation=valueMainDelegation.getIDOIndividual();
					String rdnDelegation=(String)kba.getValueData(kba.getRDN(idoDelegation, kba.getIdClass("DELEGACIÓN"), null, null, kba.getDefaultSession()));
					if(idoDelegation!=idoMainDelegation /*Si es una tienda(no es la central)*/ || !Auxiliar.equals(rdnDelegation, this.globalDelegacion)/*Si es la central de una franquicia*/){
						
						
						this.globalLogin="global_"+rdnDelegation;
						this.globalPassword="scheduler";
						
						boolean success=false;
						try{
							System.out.println("Build Glbal usuario "+globalLogin+" globalPassword "+globalPassword+" "+globalURL);
							buildGlobalConnection(globalLogin,globalPassword,globalURL,m_mode==1);
							if(!validarUsuario(m_comGlobal,globalLogin,globalPassword,true)){
								//RemoteSystemException ex=new RemoteSystemException(0, "El usuario "+globalLogin+" no es correcto en la central");
								//ex.setUserMessage("El usuario "+globalLogin+" no es correcto en la central o no hay conexión");
								
								//Singleton.getInstance().getMessagesControl().showErrorMessage("Usuario "+globalLogin+" No ha podido conectar con la central.\nSus menús no estarán disponibles", this.applet);
								
								//m_comGlobal.setState(communicator.OFFLINE);//Lo ponemos offline para luego saber en MenuControl que esta conexion no esta activa al intentar acceder a un menu global
								success=false;
							}else{
								success=true;
							}
						}catch(Exception ex){
							ex.printStackTrace();
						}finally{
							if(!success){
								m_comGlobal=null;
								Singleton.getInstance().setGlobalCommunicator(m_comGlobal);
							}
						}
					}else{
						//Le ponemos el mismo communicator como global ya que estamos en la central
						Singleton.getInstance().setGlobalCommunicator(Singleton.getInstance().getComm());
					}
				}else{
					System.err.println("WARNING: Se esta pasando como parametro la url global "+globalURL+" pero no se tiene una delegacion_central en mi_empresa");
				}
			}else{
				System.err.println("WARNING: Se esta pasando como parametro la url global "+globalURL+" pero no se tiene una mi_empresa en aplicación");
			}
		}else{
			System.err.println("WARNING: Se esta pasando como parametro la url global "+globalURL+" pero no se tiene una delegación en aplicación");
		}
	}
	
	public void closeSession() throws MalformedURLException, ParseException, AssignValueException{
		boolean reloadedBrowse=false;
		if(applet.isActive()){
			try {
			    //Recargamos el navegador para así coger nuevos jar del servidor si los hubiera y mostrar de nuevo la ventana de login  
				applet.getAppletContext().showDocument(new URL("javascript:window.location.reload();"));
				reloadedBrowse=true;
			}catch (MalformedURLException me) {
				me.printStackTrace();
			}
		}
		
		if(!reloadedBrowse){
			this.sendMessageDisconnection();
			
			//Supuestamente solo entrara aqui cuando ejecutemos desde el AppletViewer ya que no es un navegador.
			//De todas maneras no es recomendable hacer esto ya que hay alguna memoria que no se esta liberando correctamente
			//y ademas no se cogen los jar de nuevo si estos hubieran cambiado en el servidor.
			URL codeBaseJar=Singleton.getInstance().getCodeBaseJar();
			URL codeBaseServer=Singleton.getInstance().getCodeBaseServer();
			Graphics graphics=Singleton.getInstance().getGraphics();
			
			applet.getContentPane().removeAll();
			applet.validate();
			applet.paint(graphics);
			
			Singleton.removeInstance();
			SessionController.removeInstance();
			
			Singleton.getInstance().setCodeBaseJar(codeBaseJar);
			Singleton.getInstance().setCodeBaseServer(codeBaseServer);
			Singleton.getInstance().setGraphics(graphics);
			Singleton.getInstance().setDebugLog(new DebugLog());
			Singleton.getInstance().setMessagesControl(new UserMessageControl(applet,graphics,applet.getWidth()));
			Singleton.getInstance().setQuestionTaskManager(new QuestionTaskManager());
			
			this.loginPrompt(applet.getLogin(), applet.getPassword(), applet.getImageName()!=null?applet.getImageName():"Dynagent",applet.getModes());
		}
	}

	public void reload(boolean configurationMode) throws RemoteSystemException, CommunicationException, SystemException, DataErrorException, ApplicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, MalformedURLException, ParseException, SQLException, NamingException, JDOMException, AssignValueException{
		this.getWindow().disabledEvents();
		try{
			URL codeBaseJar=Singleton.getInstance().getCodeBaseJar();
			URL codeBaseServer=Singleton.getInstance().getCodeBaseServer();
			Graphics graphics=Singleton.getInstance().getGraphics();
			
			applet.getContentPane().removeAll();
			/*JPanel panel=new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
			JLabel label=new JLabel("Reiniciando...");
			label.setFont(label.getFont().deriveFont(label.getFont().getSize()*8));
			label.setForeground(Color.BLUE);
			panel.add(label);
			this.getContentPane().add(label);*/
			applet.validate();
			applet.paint(graphics);
			
			Singleton.removeInstance();
			SessionController.removeInstance();
			
			Singleton.getInstance().setCodeBaseJar(codeBaseJar);
			Singleton.getInstance().setCodeBaseServer(codeBaseServer);
			Singleton.getInstance().setGraphics(graphics);
			Singleton.getInstance().setDebugLog(new DebugLog());
			Singleton.getInstance().setMessagesControl(new UserMessageControl(applet,graphics,applet.getWidth()));
			Singleton.getInstance().setQuestionTaskManager(new QuestionTaskManager());
			
			this.setConfigurationMode(configurationMode);
			this.initApplication();
		}finally{
			this.getWindow().enabledEvents();
		}
	}

}
