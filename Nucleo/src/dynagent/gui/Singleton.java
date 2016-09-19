package dynagent.gui;


import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.SwingUtilities;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.communicator;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.jdomParser;
import dynagent.framework.gestores.GestorInterfaz;
import dynagent.gui.actions.ActionManager;
import dynagent.gui.utils.HelpComponent;
import dynagent.ruleengine.src.sessions.SessionController;

public final class Singleton{
	
	public static final String TASK_MANAGER_AREA="DYNATASKMANAGER";
	private ArrayList<Object> m_debugData;
	//private OperationGUI m_guiOp=null;
	private communicator comm=null;
	private communicator globalComm=null;
	private boolean enableDebugLog=true;
	private dynaApplet mainApp=null;
	private long GUI_threadID=0;
	private GestorInterfaz gestorInterfaz;
	private StatusBar m_statusBar;
	private ActionManager m_actionManager;
	private NavigationControl m_navigation;
	private Graphics m_graphics;
	private DebugLog debugLog;
	private URL codeBaseJar;
	private URL codeBaseServer;
	private URL codeBaseAdditionalRules;
	private IUserMessageListener messagesControl;
	private ArrayList<String> lastSendDB;
	private HelpComponent helpComponent;
	private KnowledgeBaseAdapter knowledgeBaseAdapterClone;
	private KnowledgeBaseAdapter knowledgeBaseAdapterOriginal;
	private QuestionTaskManager questionTaskManager;
	
	private HashMap<IKnowledgeBaseInfo,KnowledgeBaseAdapter> mapIkKba;
	private boolean processingCopyRowTable;
	private boolean multiWindow;
	
	private static Singleton instance=null;
	
	private Singleton(){
		mapIkKba=new HashMap<IKnowledgeBaseInfo, KnowledgeBaseAdapter>();
		lastSendDB=new ArrayList<String>();
		m_debugData=new ArrayList<Object>();
		processingCopyRowTable=false;
	}
	
	public static Singleton getInstance() {
		if (instance == null)
			instance =  new Singleton();
		
			return instance;
	}
	
	public static void removeInstance(){
		for(IKnowledgeBaseInfo ik:instance.mapIkKba.keySet()){
			instance.mapIkKba.get(ik).dispose();
		}
		if(instance.knowledgeBaseAdapterClone!=null){
			instance.knowledgeBaseAdapterClone.dispose();
		}
		//instance.knowledgeBaseAdapterOriginal.dispose();
		instance=null;
	}
	
	public NavigationControl getNavigation() {
		return m_navigation;
	}

	public void setNavigation(NavigationControl navigation) {
		m_navigation = navigation;
	}

	public void setApplet( dynaApplet app ){
		mainApp=app;
		GUI_threadID= Thread.currentThread().getId();
		//System.out.println("GUITHREAD "+GUI_threadID);
	}
	
	public dynaApplet getApplet(){
		return mainApp;
	}

	public long getGUI_threadID(){
		return GUI_threadID;
	}
//	
//	public void closeApplication(){
//		if( mainApp!=null ){
//			mainApp.closeApplication();
//		}
//	}

//	public boolean getEnableDebug(){
//		return enableDebugLog;
//	}
//
//	public void setEnableDebug( boolean val ){
//		enableDebugLog=val;
//	}
//
//	public void addDebugData(Exception data){
//		if( data!=null ){
//			outputStringAdaptor oa = new outputStringAdaptor();
//			data.printStackTrace(new PrintStream(oa));
//			addDebugData(oa.data);
//		}
//	}
//
//
//	public void addDebugData(Element data){
//		if( enableDebugLog && data!=null ){
//			try{
//				addDebugData(jdomParser.returnXML(data));
//			}catch(JDOMException e){;}
//		}
//	}
//	
//	public void addDebugData(String data){
//		if( enableDebugLog ){
//			if (m_debugData.size() > 100)
//				m_debugData.remove(0);
//			if (data.length() > 700)
//				data = data.substring(0, 700);
//			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:s-S");
//			java.util.Date tiempo = new Date(System.currentTimeMillis());
//			m_debugData.add(sdf.format(tiempo) + " >> " + data);
//		}
//	}
//
//	public String getDebugData(){
//		String res= m_debugData.size()==0 ? null:"";
//		for(int i=0;i<m_debugData.size();i++){
//			if( i>0 ) res+="\n";
//			res+= (String)m_debugData.get(i);
//		}
//		return res;
//	}
//	
//	public void clearDebugData(){
//		m_debugData.clear();
//	}

	public communicator getComm(){
		return comm;
	}
	
	public void setCommunicator(communicator comm){
		this.comm= comm;
	}

	/*public OperationGUI getCurrentOperationGUI(){
		return m_guiOp;
	}
	
	public void setCurrentOperationGUI(OperationGUI gui){
		m_guiOp=gui;
	}*/

	public void setGestorInterfaz(GestorInterfaz gestorInterfaz){
		this.gestorInterfaz=gestorInterfaz;
	}

	public GestorInterfaz getGestorInterfaz(){
		return gestorInterfaz;
	}

	public void setStatusBar(StatusBar statusBar){
		m_statusBar=statusBar;
	}

	public StatusBar getStatusBar(){
		return m_statusBar;
	}

	public ActionManager getActionManager() {
		return m_actionManager;
	}

	public void setActionManager(ActionManager actionManager) {
		m_actionManager = actionManager;
	}

	public void setGraphics(Graphics graphics) {
		m_graphics=graphics;
	}   
	public Graphics getGraphics() {
		return m_graphics;
	}

	public DebugLog getDebugLog() {
		return debugLog;
	}

	public void setDebugLog(DebugLog debugLog) {
		this.debugLog = debugLog;
	}

	public URL getCodeBaseJar() {
		return codeBaseJar;
	}

	public void setCodeBaseJar(URL codeBase) {
		this.codeBaseJar = codeBase;
	}

	public URL getCodeBaseServer() {
		return codeBaseServer;
	}

	public void setCodeBaseServer(URL codeBase) {
		this.codeBaseServer = codeBase;
	}

	public IUserMessageListener getMessagesControl() {
		return messagesControl;
	}

	public void setMessagesControl(IUserMessageListener messagesControl) {
		this.messagesControl = messagesControl;
	}

	public ArrayList<String> getLastSendDB() {
		return lastSendDB;
	}

	public void setLastSendDB(ArrayList<String> lastSendDB) {
		this.lastSendDB = lastSendDB;
	}

	public HelpComponent getHelpComponent() {
		return helpComponent;
	}

	public void setHelpComponent(HelpComponent helpComponent) {
		this.helpComponent = helpComponent;
	} 
	
	public KnowledgeBaseAdapter getKnowledgeBaseAdapter(IKnowledgeBaseInfo ik){
		return mapIkKba.get(ik);
	}
	
	public void setKnowledgeBaseAdapter(IKnowledgeBaseInfo ik,KnowledgeBaseAdapter kba){
		mapIkKba.put(ik, kba);
	}
	
	public KnowledgeBaseAdapter removeKnowledgeBaseAdapter(IKnowledgeBaseInfo ik){
		return mapIkKba.remove(ik);
	}
	
	/**
	 * Devuelve el clone que ya hay creado y crea uno de repuesto en otro hilo
	 */
	public KnowledgeBaseAdapter buildKnowledgeBaseAdapterClone(){
		while(knowledgeBaseAdapterClone==null){//Esperamos si el clone cacheado no esta aun creado
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		KnowledgeBaseAdapter kba=knowledgeBaseAdapterClone;
		knowledgeBaseAdapterClone=null;
		createClone();//Lo crea en otro hilo
		return kba;
	}
	
	/**
	 * Asigna el KnowledgeBaseAdapter que se utilizará para crear los clones cuando se llame a buildKnowledgeBaseAdapterClone()
	 * @param kba
	 */
	public void setKnowledgeBaseAdapterOriginal(KnowledgeBaseAdapter kba){
		knowledgeBaseAdapterOriginal=kba;
		if(knowledgeBaseAdapterOriginal!=null)
			createClone();//Lo crea en otro hilo
		else knowledgeBaseAdapterClone=null;
	}
	
//	/**
//	 * Asigna el KnowledgeBaseAdapter que se utilizará para crear los clones cuando se llame a buildKnowledgeBaseAdapterClone()
//	 * @param kba
//	 * @throws EngineException 
//	 * @throws IncoherenceInMotorException 
//	 * @throws NotFoundException 
//	 */
//	public void setKnowledgeBaseAdapterOriginal(KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException, EngineException{
//		if(kba!=null){
//			knowledgeBaseAdapterOriginal=kba.doClone();
//			createClone();//Lo crea en otro hilo
//		}else{
//			knowledgeBaseAdapterOriginal=null;
//			knowledgeBaseAdapterClone=null;
//		}
//	}
	
	/**
	 * Crea un clone de KnowledgeBaseAdapter a partir de knowledgeBaseAdapterOriginal
	 */
	private void createClone(){
		Thread createThread=new Thread(){

			@Override
			public void run() {
				try{
					knowledgeBaseAdapterClone=knowledgeBaseAdapterOriginal.doClone();
				}catch(Exception ex){
					ex.printStackTrace();
					if(comm!=null)
						comm.logError(SwingUtilities.getWindowAncestor(mainApp),ex, "No se ha podido crear el soporte para la multiventana");
				}
			}
			
		};
		createThread.setPriority(Thread.MIN_PRIORITY);
		createThread.start();
	}
	
	public boolean hasKnowledgeBaseAdapterClone(){
		return knowledgeBaseAdapterClone!=null;
	}

	public QuestionTaskManager getQuestionTaskManager() {
		return questionTaskManager;
	}

	public void setQuestionTaskManager(QuestionTaskManager questionTaskManager) {
		this.questionTaskManager = questionTaskManager;
	}

	public KnowledgeBaseAdapter getKnowledgeBaseAdapterOriginal() {
		return knowledgeBaseAdapterOriginal;
	}

	public communicator getGlobalComm() {
		return globalComm;
	}
	
	public void setGlobalCommunicator(communicator comm){
		this.globalComm= comm;
	}
	
	public int getNumberOfKnowledgeBaseAdapter(){
		return mapIkKba.size();
	}

	public void setProcessingCopyRowTable(boolean processing) {
		this.processingCopyRowTable=processing;
	}

	public boolean isProcessingCopyRowTable() {
		return processingCopyRowTable;
	}

	public void setMultiWindow(boolean multiWindow) {
		this.multiWindow=multiWindow;
	}
	
	public boolean isMultiWindow() {
		return multiWindow;
	}

}
