
package dynagent.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import dynagent.common.utils.DebugLog;
import dynagent.common.utils.UserMessageControl;

public class dynaApplet extends JApplet/* implements appControl*/{

	private static final long serialVersionUID = 1L;

	AppletManager appletManager;
	String login;
	String password;
	String imageName;
	String modes;
	static String reportsRoutedToMainServer;
	static String port_backup;
	static String host_backup;
	
	@Override
	public void destroy() {
		//System.err.println("Destroy");
		if(appletManager!=null) 
			appletManager.sendMessageDisconnection();
		super.destroy();
		System.exit(0);
	}

	// This is a hack to avoid an ugly error message in 1.1.
	public dynaApplet(JFrame f,int business,String businessName,boolean debug,ArrayList<String> rules,ArrayList<String> configurationRules,URL globalURL,String globalDelegacion,boolean multiWindow) {
		appletManager=new AppletManager(this,false,multiWindow,business,businessName,debug,false,null,rules,configurationRules,globalURL,globalDelegacion);
		if(reportsRoutedToMainServer!=null&&reportsRoutedToMainServer.length()>0 && port_backup!=null && port_backup.length()>0 && host_backup!=null && host_backup.length()>0){
			System.out.println("DENTRO PROPERTIES reportsRoutedToMainServer:"+reportsRoutedToMainServer);
			appletManager.reportsRoutedToMainServer=reportsRoutedToMainServer;
			appletManager.port_backup=Integer.parseInt(port_backup);
			appletManager.host_backup=host_backup;					
		}
	}
	
	public dynaApplet() {
	}

	/*public void setMainDialog(JDialog dlg){
	m_dialog= dlg;
    }*/

	public static void main(String[] args) throws Exception {		
		String codeBase=null;
		int width=-1;
		int height=-1;
		int business=1;
		String rules=null;
		boolean debug=false;
		String configurationRules="configurationRules.dpkg";
		String login="";
		String password="";
		URL globalURL=null;
		String globalDelegacion=null;
		boolean multiWindow=true;
		String businessName=null;
		
		Properties defaultProps = new Properties();
		try{
		FileInputStream in = new FileInputStream("launch.properties");
		defaultProps.load(in);
		in.close();
		}catch(java.io.FileNotFoundException e){
			
		}
		
		String id="";
		for(int i=0;i<args.length;i++){
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-codebase")){
					codeBase=args[i];
				}else if(id.equalsIgnoreCase("-width")){
					width=Integer.valueOf(args[i]);
				}else if(id.equalsIgnoreCase("-height")){
					height=Integer.valueOf(args[i]);
				}else if(id.equalsIgnoreCase("-bns")){
					business=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-bnsname")){
					businessName=args[i];
				}else if(id.equalsIgnoreCase("-rules")){
					rules=args[i];
				}else if(id.equalsIgnoreCase("-debug")){
					debug=Boolean.valueOf(args[i]);
				}else if(id.equalsIgnoreCase("-configrules")){
					configurationRules=args[i];
				}else if(id.equalsIgnoreCase("-login")){
					login=args[i];
				}else if(id.equalsIgnoreCase("-password")){
					password=args[i];
				}else if(id.equalsIgnoreCase("-globalurl")){
					globalURL=new URL(args[i]);
				}else if(id.equalsIgnoreCase("-globaldelegacion")){
					globalDelegacion=args[i];
				}else if(id.equalsIgnoreCase("-multiwindow")){
					multiWindow = Boolean.valueOf(args[i]);
				}else if(id.equalsIgnoreCase("-reportstomaster")){
					reportsRoutedToMainServer = args[i];
				}else if(id.equalsIgnoreCase("-hostbackup")){
					host_backup = args[i];
				}else if(id.equalsIgnoreCase("-portbackup")){
					port_backup = args[i];
				}
			}
		}

		 
		if(codeBase==null) codeBase=defaultProps.getProperty("codebase");
		if(width==-1 && defaultProps.getProperty("width")!=null) 
				width=Integer.valueOf(defaultProps.getProperty("width"));
		if(height==-1 && defaultProps.getProperty("height")!=null)
				height=Integer.valueOf(defaultProps.getProperty("height"));
		if(business==1 && defaultProps.getProperty("bns")!=null) 
			business=Integer.valueOf(defaultProps.getProperty("bns"));
		if(rules==null) 
			rules=defaultProps.getProperty("rules");		
		if(login=="") 
			login=defaultProps.getProperty("login");
		if(password=="") 
			password=defaultProps.getProperty("password");
		if(globalURL==null&&defaultProps.getProperty("globalurl")!=null) 
			globalURL=new URL(defaultProps.getProperty("globalurl"));
		if(globalDelegacion==null) 
			globalDelegacion=defaultProps.getProperty("globaldelegacion");
		if(defaultProps.getProperty("multiwindow")!=null) 
			multiWindow=Boolean.valueOf(defaultProps.getProperty("multiwindow"));
		
		if(defaultProps.getProperty("reportstomaster")!=null){ 
			reportsRoutedToMainServer=defaultProps.getProperty("reportstomaster");
		}
		if(reportsRoutedToMainServer!=null && reportsRoutedToMainServer.length()>0){
			System.out.println("reportsRoutedToMainServer:"+reportsRoutedToMainServer);
		}
		if(defaultProps.getProperty("portbackup")!=null){ 
			port_backup=defaultProps.getProperty("portbackup");
		}
		if(defaultProps.getProperty("hostbackup")!=null){
			host_backup=defaultProps.getProperty("hostbackup");
		}
		
		
		System.err.println("multiWindow:"+multiWindow+" url:"+codeBase+" rules:"+rules+" reportstomaster:"+reportsRoutedToMainServer);
		
		Singleton.getInstance().setCodeBaseJar(new URL(codeBase));
		Singleton.getInstance().setCodeBaseServer(Singleton.getInstance().getCodeBaseJar());
		Singleton.getInstance().setMultiWindow(multiWindow);
		
		ArrayList<String> rulesList=new ArrayList<String>();
		String[] split=rules.split(";");
		for(int i=0;i<split.length;i++){
			rulesList.add(split[i]);
		}
		
		ArrayList<String> configurationRulesList=new ArrayList<String>();
		split=configurationRules.split(";");
		for(int i=0;i<split.length;i++){
			configurationRulesList.add(split[i]);
		}
		
		JFrame f = new JFrame();
		final dynaApplet dyna=new dynaApplet(f,business,businessName,debug,rulesList,configurationRulesList,globalURL,globalDelegacion,multiWindow);
		f.setContentPane(dyna);
		
		if(width!=-1){
			dyna.setPreferredSize(new Dimension(width,height));
		}else{
			f.setState(JFrame.MAXIMIZED_BOTH);
			f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}
		
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dyna.destroy();
			}
		});
		
		f.pack();
		f.setVisible(true);
		Singleton.getInstance().setGraphics(f.getGraphics());
		Singleton.getInstance().setMessagesControl(new UserMessageControl(f,Singleton.getInstance().getGraphics(),f.getWidth()));
		Singleton.getInstance().setDebugLog(new DebugLog());
		Singleton.getInstance().setQuestionTaskManager(new QuestionTaskManager());
		dyna.login=login;
		dyna.password=password;
		dyna.imageName="Dynagent";
		dyna.modes="Configuración;Negocio";
		dyna.showLogin(dyna);
	}	
	
	public void showLogin(dynaApplet dyna) throws Exception{
		appletManager.loginPrompt(dyna.login,dyna.password,dyna.imageName,dyna.modes);
	}
	
	public void showMessage(String message){
		System.err.println("Mensaje desde JavaScript:"+message);;
	}

	public void init(){
		JPanel panel=new JPanel();
		panel.setLayout(new BorderLayout(0,0));
		panel.setBackground(Color.WHITE);
		JLabel label=new JLabel("<html>Cargando datos iniciales, espere por favor...<br/>(la primera vez podría tardar hasta 1 minuto)<br/><br/><center><img src='"+this.getCodeBase()+"../images/spinner.gif'/></center></html>",JLabel.CENTER);
		
		panel.add(label,BorderLayout.CENTER);
		this.setContentPane(panel);
		this.validate();
		this.repaint();
	}
	
	public void start() {
		try{
			//System.err.println(this.getDocumentBase().toString());
			//System.err.println(this.getCodeBase().toString());
			//Singleton.getInstance().setDocumentBase(this.getDocumentBase());
			
			
			
			Singleton.getInstance().setCodeBaseJar(this.getCodeBase());
			String codeBaseServer=getParameter("codeBaseServer");
			Singleton.getInstance().setCodeBaseServer(codeBaseServer!=null?new URL(codeBaseServer):Singleton.getInstance().getCodeBaseJar());
			String rules=getParameter("rules");
			String configurationRules=getParameter("configurationRules");
			System.err.println("codeBaseJar:"+Singleton.getInstance().getCodeBaseJar());
			System.err.println("codeBaseServer:"+Singleton.getInstance().getCodeBaseServer());
			System.err.println("Rules:"+rules);
			System.err.println("ConfigurationRules:"+configurationRules);
			System.err.println("java.version-->"+System.getProperty("java.version"));
			String javaVersion=System.getProperty("java.version");
			String javaVersionPattern = getParameter("javaVersionPattern");
			URL globalURL=null;
			if(getParameter("globalurl")!=null){
				globalURL=new URL(getParameter("globalurl"));
			}
			
			String reportsRoutedToMainServer=getParameter("reportstomaster");
			if(reportsRoutedToMainServer!=null && reportsRoutedToMainServer.length()>0){
				System.out.println("reportsRoutedToMainServer:"+reportsRoutedToMainServer);
			}
			String port_backup=getParameter("portbackup");
			String host_backup=getParameter("hostbackup");
			
			String globalDelegacion=getParameter("globaldelegacion");
			System.err.println("JavaVersionPattern:"+javaVersionPattern);
			login=getParameter("login");
			password=getParameter("password");
			String business=getParameter("bns");
			String businessName=getParameter("bnsname");
			imageName=getParameter("logo");
			modes=getParameter("modes");
			System.err.println("modes:"+modes);
			System.err.println("bns:"+business);
			System.err.println("bnsName:"+businessName);
			Singleton.getInstance().setGraphics(this.getGraphics());
			Singleton.getInstance().setMessagesControl(new UserMessageControl(this,Singleton.getInstance().getGraphics(),this.getWidth()));
			boolean debug=false;
			//if(!javaVersion.matches("1\\.6\\.((0_[^0].)|([^0]_..))")){
			if(javaVersionPattern==null || javaVersionPattern.isEmpty() || javaVersion.matches(javaVersionPattern)){
				String memoryOptions=getParameter("memoryOptions");
				System.err.println("memoryOptions="+memoryOptions);
				if(memoryOptions!=null){
					long maxMemory=Runtime.getRuntime().maxMemory();
					if((maxMemory/(1024*1024))<350){
						String url=this.getDocumentBase().toString();
						
						String[] split=memoryOptions.split(";");
						String newMemory=null;
						System.err.println("url:"+url);
						if(url.contains("&memory=")){
							
							String oldMemory=url.substring(url.indexOf("&memory=")+8,url.length());
							url=url.substring(0, url.indexOf("&memory"));
							
							int posNewMemory=0;
							for(int i=0;i<split.length && posNewMemory==0;i++){
								System.err.println("split[i]"+split[i]+" posNewMemory:"+posNewMemory+ "oldMemory:"+oldMemory);
								if(split[i].equals(oldMemory)){
									if(i<split.length-1){
										posNewMemory=i+1;
									}else{
										posNewMemory=-1;
									}
								}
									
							}
							System.err.println("posNewMemory:"+posNewMemory);
							if(posNewMemory!=-1){
								newMemory=split[posNewMemory];
							}
						}else{
							newMemory=split[0];
						}
						
						if(newMemory!=null){
							getAppletContext().showDocument(new URL(url+"&memory="+newMemory));
							return;
						}
					}
				}
				
				String hideHistoryDDBB = getParameter("hideHistoryDDBB");
				boolean multiWindow = true;
				if(getParameter("multiWindow")!=null){
					multiWindow=Boolean.valueOf(getParameter("multiWindow"));
				}
				String theme = getParameter("theme");
				String licenseMode = getParameter("licenseMode");
				ArrayList<String> rulesList=new ArrayList<String>();
				String[] split=rules.split(";");
				for(int i=0;i<split.length;i++){
					rulesList.add(split[i]);
				}
				ArrayList<String> configurationRulesList=new ArrayList<String>();
				split=configurationRules.split(";");
				for(int i=0;i<split.length;i++){
					configurationRulesList.add(split[i]);
				}
				System.err.println("hideHistoryDDBB:"+hideHistoryDDBB);
				if(theme!=null)
					System.err.println("theme:"+theme);
				Singleton.getInstance().setDebugLog(new DebugLog());
				Singleton.getInstance().setQuestionTaskManager(new QuestionTaskManager());
				Singleton.getInstance().setMultiWindow(multiWindow);
				
				appletManager=new AppletManager(this,hideHistoryDDBB!=null?new Boolean(hideHistoryDDBB):false,multiWindow,(business!=null?Integer.valueOf(business):0),businessName,debug, licenseMode!=null?new Boolean(licenseMode):false, theme, rulesList, configurationRulesList, globalURL, globalDelegacion);
				appletManager.loginPrompt(login,password,imageName!=null?imageName:"Dynagent",modes);
				if(reportsRoutedToMainServer!=null&&reportsRoutedToMainServer.length()>0 && port_backup!=null && port_backup.length()>0 && host_backup!=null && host_backup.length()>0){
					appletManager.reportsRoutedToMainServer=reportsRoutedToMainServer;
					appletManager.port_backup=Integer.parseInt(port_backup);
					appletManager.host_backup=host_backup;					
				}
				/*JavaScriptCommand javaScript=new JavaScriptCommand(this);
				javaScript.processCommand(null,null);*/
			}else{
				String javaVersionMin = getParameter("javaVersionMin");
				String urlDownloadJava = getParameter("urlDownloadJava");
				System.err.println("javaVersionMin:"+javaVersionMin);
				System.err.println("urlDownloadJava:"+urlDownloadJava);
				Singleton.getInstance().getMessagesControl().showErrorMessage("Es necesaria la versión "+javaVersionMin+" o una superior para ejecutar correctamente la aplicación. Se abrirá la página de descarga.",this);
				getAppletContext().showDocument(new URL(urlDownloadJava));
			}

// Intento de evitar bug de multiventana con AppletViewer
//			KeyboardFocusManager.getCurrentKeyboardFocusManager().addVetoableChangeListener(new VetoableChangeListener(){
//				Window oldWindow;
//				public void vetoableChange(PropertyChangeEvent pe) throws PropertyVetoException {
//					if(pe.getPropertyName().equals("activeWindow")){
//						System.err.println("--vetoableChange:getNewValue "+(pe.getNewValue()!=null?pe.getNewValue().getClass():null));
//						System.err.println("--vetoableChange:getOldValue "+(pe.getOldValue()!=null?pe.getOldValue().getClass():null));
//						System.err.println("oldWindow:"+oldWindow);
//						if(pe.getOldValue()!=null){
//							oldWindow=(Window)pe.getOldValue();
//						}
//						
//						if(pe.getNewValue() instanceof AppletViewer){
//							if(oldWindow instanceof JDialog){
//								System.err.println("1:"+oldWindow);
//								if(((JDialog)oldWindow).isModal()){
//									final Container parent=((JDialog)oldWindow).getOwner();
//									//parent.requestFocus();
//									SwingUtilities.invokeLater(new Runnable(){
//
//										public void run() {
//											System.err.println("PideFoco para parent");
//											parent.requestFocus();
//											parent.requestFocusInWindow();
//											if(((Window)parent).getMostRecentFocusOwner()!=null)
//												((Window)parent).getMostRecentFocusOwner().requestFocusInWindow();
//										}
//										
//									});
//									
//									System.err.println("2:"+parent);
//									throw new PropertyVetoException("Ventana incorrecta",pe);
//								}
//							}
//							
//						}
//					}
//				}
//				
//			});
		}catch(Exception ex){
			ex.printStackTrace();
			Singleton.getInstance().getComm().logError(SwingUtilities.getWindowAncestor(this),ex,"Error al crear el formulario de identificación de usuarios");
		}
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getImageName() {
		return imageName;
	}

	public String getModes() {
		return modes;
	}

	public AppletManager getAppletManager() {
		return appletManager;
	}
	
}
