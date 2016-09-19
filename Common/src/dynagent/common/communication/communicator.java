
package dynagent.common.communication;


import java.awt.Desktop;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.ImageObserver;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.naming.NamingException;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdom.CDATA;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IUserException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;
import dynagent.common.utils.CipherUtils;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IUserMessageListener;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.ReportPrinter;
import dynagent.common.utils.RowItem;
import dynagent.common.utils.jdomParser;

class outputStringAdaptor extends OutputStream {
	String data = "";
	public void close() {
		;
	}

	public void flush() {
		;
	}

	public void write(byte[] buf) {
		data = new String(buf);
	}

	public void write(byte[] b, int off, int len) {
		for (int i = off; i < len; i++) {
			data += String.valueOf((char) b[i]);
		}
	}

	public void write(int b) {
		data += String.valueOf((char) b);
	}
}
/*
class liveNotifDaemon extends Thread {
	int period = 0;
	final static int active=1;
	final static int shutdown=2;
	int state=active;
	communicator comm;

	public liveNotifDaemon(communicator comm,int period) {
		this.period = period;
		this.comm = comm;
		start();
	}
	public void shutdown(){
		state=shutdown;
	}

	public void run() {
		while (state==active) {
			try {
				sleep(period);
				//System.out.println("PREDAEMON");
				if( state==shutdown ) continue;
				comm.serverLiveTest();
				//Si no la invoco en la thread GUI, al ocultar la ventana de envio no retorna
				//System.out.println("POSTDAEMON");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
*/

class crackState {
	long ini;
	long timeRestore = 20000;
	crackState() {
		this.ini = System.currentTimeMillis();
	}

	public boolean isOk() {
		long curr = System.currentTimeMillis();
		return curr - ini > timeRestore;
	}
}


public class communicator implements docServer {
	int defaultTimeOut;
	public boolean useUrlBackup=false;
	//ImageIcon m_iconNulo = null;
	crackState crak = null;
	boolean encriptar=false;
	long lastTime = 0;
	String m_host = null, m_pwd = null;
	int m_port,m_port_backup;
	String m_host_backup=null;
	int m_MaxReintentos;
	int m_globalReint;
	String m_transactionID = null;
	String m_user = null, m_userSession = null;
	HashMap m_metaTOs = new HashMap();
	HashMap<Integer, HashMap> m_cacheOBJ_ats = new HashMap<Integer, HashMap>();
	public JApplet m_cliente;
	//String m_msgUID=null;
	ArrayList<HashSet<Integer>> m_locksControls = new ArrayList<HashSet<Integer>>();
	ArrayList<HashSet<Integer>> m_locksControlUsadas = new ArrayList<HashSet<Integer>>();
	HashMap m_lockedIdo = new HashMap();
	/*public metaComClient m_metaCom;*/
	/*private metaData m_md;*/
	/*private KnowledgeBaseMetaData m_md;*/
	public int m_business;
	public String m_businessName;
	public int m_idoUser = 0;
	/*public Integer m_userRol;*/
	public static final int NO_TEST = 0;
	public static final int ACTION_TEST = 1;
	int m_testState = NO_TEST;
	//cacheInstance m_cacheInstance;
	String m_ver = "V37";
	String monitorTitle = null;
	String monitorString = null;
	//updateManager m_updateManager = new updateManager();
	public final static int OFFLINE = 1;
	public final static int ONLINE = 2;
	private int m_state = ONLINE;
	HashMap<String,ImageIcon> iconsCache;

	//liveNotifDaemon  m_liveDaemon=null;
	IUserMessageListener messagesControl;
	DebugLog debugLog;
	static communicator com;
	URL serverURL;
	URL jarURL;//Direccion donde se encuentran los jar y las reglas genericas
	ArrayList<ISubmitDDBBListener> listSubmitDDBBListener; 
	
	Integer smallImageHeight;
	Integer maxKbImageSize;
	
	String mode;
	String subscription;
	
	private String staticMsguid;
	private String m_protocolToSendAndReceive;
	
	/*    class communicatorThread extends Thread {
        Object msg;
        message eRes = null;
        boolean sendConfirm, hasResponse;
	Object param=null;
        Exception exc = null;
        /*timeMonitor tm;*/
	/*        boolean reenviar = false;
	boolean working=true;

        public communicatorThread(/*timeMonitor tm,*/
	/*                                  Object msg,
                                  boolean sendConfirm,
                                  boolean reenviar,
                                  Object param,
                                  boolean hasResponse) {
            this.msg = msg;
            this.sendConfirm = sendConfirm;
            this.param = param;
            /*this.tm = tm;*/
	/*            this.reenviar = reenviar;
            this.hasResponse = hasResponse;
        }

        public Exception getException() {
            return exc;
        }

        public message getResponse() {
            return eRes;
        }

        public void run() {
	    try {
		System.out.println("PREENVIO");
		if (msg instanceof message) {
		    eRes = Singleton.getComm().linkLevel(param,
			    ((message) msg).toString(),
			    sendConfirm,
			    reenviar,
			    hasResponse);
		}
		if (msg instanceof String) {
		    eRes = Singleton.getComm().linkLevel(param,
			    (String) msg,
			    sendConfirm,
			    reenviar,
			    hasResponse);
		}
	    } catch (Exception se) {
		se.printStackTrace();
		exc = se;
            }
	    working=false;
	    //Caso de que appLevel haya sido llamado desde la threadGUI, la accion tm.showDialog bloquea el flujo de
	    // appLevel, así que por si acaso llamo a dispose desde aqui
/*	    Runnable invocaEnGUI = new Runnable() {
		public void run() {
		    if(tm!=null)tm.dispose();
		}
	    };
	    SwingUtilities.invokeLater(invocaEnGUI);
	 *//*        }
	public boolean isWorking(){
	    return working;
	}
    }
	  */

	String reportsRoutedToMainServer=null;
	public void setUrlBackup(String host,int port,String rep){
		System.out.println("COMM setUrlBackup host:"+host+" "+rep);
		if(rep!=null&&rep.length()>0 && port!=0 && host!=null && host.length()>0){
			
			m_host_backup=host;
			m_port_backup=port;
			reportsRoutedToMainServer=rep;
		}
	}
	
	public communicator(JApplet cliente, IUserMessageListener messagesControl, DebugLog debugLog, URL serverURL, URL jarURL, int to, int reintentos, int business, String login, String pwd, boolean configurationMode)/* throws SystemException*/ {
		this(cliente, messagesControl, debugLog, serverURL, jarURL, to, reintentos, business, null, login, pwd, configurationMode);
	}

	
	public communicator(JApplet cliente, IUserMessageListener messagesControl, DebugLog debugLog, URL serverURL, URL jarURL, int to, int reintentos, int business, String businessName, String login, String pwd, boolean configurationMode)/* throws SystemException*/ {
		//m_key = encode(login + ":" + pwd);
		m_user = login;
		m_pwd = pwd;
		if (configurationMode)
			mode = Constants.CONFIGURATION_MODE;
		else
			mode = Constants.BUSINESS_MODE;
		m_cliente = cliente;
		m_host = serverURL.getHost();
		m_port = serverURL.getPort();
		m_protocolToSendAndReceive = serverURL.getProtocol();
		if(m_port==-1){
			m_port=80;
		}
		m_business=business;
		m_businessName=businessName;
		this.serverURL=serverURL;
		this.jarURL=jarURL;
		defaultTimeOut = to;
		m_MaxReintentos = 2; // reintentos;
		this.messagesControl=messagesControl;
		this.debugLog=debugLog;
		//m_cacheInstance = new cacheInstance(10000);
		setMonitorDefault();
		nextUserSession();
		iconsCache=new HashMap<String, ImageIcon>();
		listSubmitDDBBListener=new ArrayList<ISubmitDDBBListener>();
		maxKbImageSize=512;
	}

	public void setStaticMsguid(String staticMsguid) {
		this.staticMsguid = staticMsguid;
	}

	public void setTestState(int state){
		m_testState=state;
	}

	void nextUserSession(){
		if (m_userSession==null)
			m_userSession = m_user + ":" + System.currentTimeMillis() + ":" +   (new Random()).nextInt(10);
	}

	/*public void start() {
		if( m_liveDaemon!=null ){
			m_liveDaemon.shutdown();
			//nextUserSession();
		}
		m_liveDaemon= new liveNotifDaemon(this,60000);
	}*/

	public void setUser(String user) {
		m_user = user;
	}

	private void setPwd(String pwd) {
		m_pwd = pwd;
	}

	public String getUser() {
		return m_user;
	}
	public boolean equals(docServer other){
		if(!(other instanceof communicator)) return false;
		return ((communicator)other).m_host.equals(m_host);
	}
	public int getBusiness() {
		return m_business;
	}
	
	public void setBusiness(int business) {
		m_business=business;
	}

	public void addLockList(HashSet<Integer> ctx) {
		if (m_locksControls.indexOf(ctx) == -1) {
			m_locksControls.add(ctx);
		}
	}

	public void removeLockList(HashSet<Integer> ctx) {
		if (m_locksControls.indexOf(ctx) != -1) {
			m_locksControls.remove(ctx);
		}
	}

	/*public int getDom( Contexto cc ){
     return cc.getDom( m_userRol ).intValue();
     }*/

	private void setMonitorDefault() {
		monitorTitle = "Estado comnunicaciones";
		monitorString = "Envio de datos";
	}

	/*private message linkLevel(Element eMsg, boolean sendConfirm,boolean reenviar, boolean hasResponse)
	    throws CommunicationException, RemoteSystemException, SystemException {
        String msg = null;
        try {
            msg = jdomParser.returnXML(eMsg);
        } catch (JDOMException e) {
            logError(e);
            return null;
        }
        return linkLevel(eMsg, msg, sendConfirm,reenviar, hasResponse);
    }*/

	private message linkLevel(Object queryParam, String msgString, boolean sendConfirm,	boolean reenviar, boolean hasResponse)
		throws CommunicationException, RemoteSystemException, SystemException {
		//System.out.println(">>> :" + msgString);
		String res = null;
		/*System.err.println("LLAMADA A BD");
		Auxiliar.printCurrentStackTrace();*/
		try {
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"request",msgString);
			}
			res = privSendAndReceive(msgString, false, reenviar, hasResponse);
			//System.out.println("res:"+res);
			/*if (queryParam instanceof Element)
				System.out.println("queryParam:"+jdomParser.returnXML((Element)queryParam));
			else
				System.out.println("queryParam:"+queryParam);*/
			if (hasResponse) {
				if(debugLog!=null){
					debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"response",res);
				}
				//long ini = System.currentTimeMillis();
				//System.out.println("RECIBIDO:" + res);
				//long end = System.currentTimeMillis();
				//System.out.println("Time ESCRITURA:" + (end - ini));
				if (res.indexOf("Unauthorized") >= 0){
					message msgTmp= new message(message.MSG_CONFIRM);
					msgTmp.setResultCode(message.ERROR_PERMISSION);
					res =msgTmp.toString();
				}
				//System.out.println("Time JDOM:" +  (System.currentTimeMillis() - end));
				message eRes = messageFactory.parseMsg(/*m_kb,*/queryParam,res);
				/* if (sendConfirm &&
                    !(eRes.getType() == message.MSG_CONFIRM)) {
                    throw new CommunicationException(CommunicationException.ERROR_REMOTO, null);
                }*/
				return eRes;
			}else {
				return null;
			}
		} catch (ParseException e) {
			e.printStackTrace();
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"Exception",e);
			}
			System.out.println("COM, error parseado msg recibido:SIZE:" +
					res.length() + ":MSG:" + res + ":END_MSG\n:" +
					e.getMessage());
			throw new CommunicationException(CommunicationException.ERROR_TRAMA,"ERROR EN LAS COMUNICACIONES CON EL SISTEMA REMOTO");
		} catch (DataErrorException e) {
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"Exception",e);
			}
			e.printStackTrace();
			System.out.println("COM, error datos msg recibido:SIZE:" +
					res.length() + ":MSG:" + res + ":END_MSG\n:" +
					e.getMessage());
			throw new RemoteSystemException(message.ERROR_DATA,"");
		} catch (IllegalDataException e) {
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"Exception",e);
			}
			System.out.println("COM, error IllegalData msg recibido:SIZE:" +
					res.length() + ":MSG:" + res + ":END_MSG\n:" +
					e.getMessage());
			throw new CommunicationException(CommunicationException.ERROR_TRAMA,"ERROR EN LAS COMUNICACIONES CON EL SISTEMA REMOTO");
		} catch (IOException e) {
			System.out.println("PRIV SEND REC, state on:" + (m_state == ONLINE));
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"Exception",e);
			}
			e.printStackTrace();
			if (m_state == OFFLINE) { //Me ha llamado el propio communication test
				throw new CommunicationException(CommunicationException.ERROR_CIERRE_CONEXION,"ERROR EN LAS COMUNICACIONES CON EL SISTEMA REMOTO");
			}
			m_state = OFFLINE;
			//communicationTest();
			//System.out.println("END TEST");
			m_state = ONLINE;
			throw new CommunicationException(CommunicationException.ERROR_CIERRE_CONEXION, "ERROR EN LAS COMUNICACIONES CON EL SISTEMA REMOTO");
		}catch( Exception e ){
			e.printStackTrace();
			if(debugLog!=null){
				debugLog.addDebugData(DebugLog.DEBUG_COMMUNICATIONS,"Exception",e);
			}
			throw new SystemException(SystemException.ERROR_SISTEMA,/*e.getMessage()*/"ERROR DEL SISTEMA");
		}
	}


	public int getState() {
		return m_state;
	}
	
	public void setState(int state) {
		m_state=state;
	}

	/*private void communicationTest() {
		int intento = 1;
		message res = null;
		message msg= new message(message.MSG_LIVE);
		msg.setMsguid(newMsgUID());
		msg.setVersion(m_ver);
		msg.setBusiness(m_business);
		msg.setUser(m_user);
		msg.setClientSession(m_userSession);
		long tout = 5000;
		while (res == null) {
			try {
				Thread.sleep(tout);
				//System.out.println("TEST "+intento);
				res = communicationTest(intento, msg, tout);
				//System.out.println("TEST NOT EXCEPTION");

				intento++;
			} catch (Exception e) {
				intento++;
				res = null;
				e.printStackTrace();
			}
		}
	}*/

	private message communicationTest(int intento, message msg, long tout) throws Exception {
		/*Point pos = null;
		if(m_cliente!=null )
			pos= ((dynaApplet) m_cliente).getGUICenter();
		else pos= new Point(200,200);
		timeMonitor tm = new timeMonitor(pos,
                                         (int) tout,
                                         "Test comunicaciones (intento nº " +
                                         intento + ")");*/
		/*tm.showDefaultDialog("PROBLEMA DE COMUNICACIONES");*/
		/*        communicatorThread cm = new communicatorThread(/*tm,*//*msg, false, false, null, true);
        cm.start();
	//Si he sido llamado desde una thread diferente a la GUI, saltaré aquí antes de recibir la respuesta
	//Añado el siguiente bucle para esperar.
	try{
	    while (cm.isWorking())
		Thread.currentThread().sleep(100);
	}catch(Exception e){
	    e.printStackTrace();
	}
        if (cm.getException() != null) {
	    System.out.println("TEST Excep");
            throw cm.getException();
        } else {
            return cm.getResponse();
        }*/
		boolean sendConfirm=false;
		boolean reenviar=false;
		Object param=null;
		boolean hasResponse=true;
		message eRes=null;

		//System.out.println("PREENVIO");
		try{
			eRes = linkLevel(param,
					((message) msg).toString(),
					sendConfirm,
					reenviar,
					hasResponse);

			return eRes;
		} catch (Exception se) {
			System.out.println("TEST Excep");
			throw se;
		}
	}

	public message appLevel(Element msg, boolean sendConfirm) throws  SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		msg.setAttribute("MSGUID", newMsgUID());
		msg.setAttribute("VERSION", m_ver);
		msg.setAttribute("BNS", String.valueOf(m_business));
        if(mode!=null){
            msg.setAttribute("MODE",mode);
        }
        if(subscription!=null){
            msg.setAttribute("SUBSCRIPTION",subscription);
        }
		if (msg.getAttributeValue("CNXID") == null) {
			msg.setAttribute("CNXID", m_userSession);
		}
		try {
			//System.out.println("PREAPP " + msg.getName());
			return appLevel(jdomParser.returnXML(msg),sendConfirm,msg,true);
		} catch (JDOMException ex) {
			logError(ex);
			throw new SystemException(SystemException.ERROR_DATOS, "ERROR BUILD RESPONSE");
		}
	}

	public message appLevel(String msgString, boolean sendConfirm,Object param, boolean hasResponse) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//String res = null;
		message eRes = null;
		int errorComunicaciones = 1,errorSystema = 2,errorAccionRemota = 3,errorSLEEP = 4,error = 0;
		for( int i = 0; i < m_MaxReintentos; i++) {
			//System.out.println("i " + i);

			boolean reenviar = i > 0;

			try {
				//System.out.println("PREENVIO");
				eRes = linkLevel(param, msgString, sendConfirm, reenviar, hasResponse);
			} catch (RemoteSystemException rse) {
				rse.printStackTrace();
				System.out.println("Excepcion CM rem");
				throw (RemoteSystemException) rse;
			} catch(CommunicationException ce){
				ce.printStackTrace();
				System.out.println("Excepcion CM com");
				error = errorComunicaciones;
				continue;
			}

			if (hasResponse) {
				/*                    eRes = cm.getResponse();
				 */                   //jdomParser.print("RES:",eRes);
				if( eRes==null ){
					SystemException se=new SystemException(SystemException.ERROR_DATOS,"MENSAJE NULO");
					se.setUserMessage("Error: No se han obtenido datos");
					throw se;
				}
				if (eRes.getResultCode() != message.SUCCESSFULL ) {
					int code = eRes.getResultCode();
					if (code == message.ERROR_SYSTEM) {						
						//Singleton.getMessagesControl().showMessage(
//						JOptionPane.showMessageDialog(m_cliente, "ERROR EN SISTEMA REMOTO. No puede realizarse la operación.");
						String mensajeError = "ERROR EN SISTEMA REMOTO.";
						Element descripcion = (Element)eRes.getContent();
					
						if (descripcion!=null && descripcion instanceof Element) {
							mensajeError = descripcion.getText();
						}
						RemoteSystemException re=new RemoteSystemException(0,mensajeError);
						re.setUserMessage(mensajeError);
						throw re;
					} else if (code == message.ERROR_VERSION) {
//						JOptionPane.showMessageDialog(m_cliente, "VERSION INCORRECTA, CIERRE EL NAVEGADOR Y ENTRE DE NUEVO");
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("VERSION INCORRECTA, CIERRE EL NAVEGADOR Y ENTRE DE NUEVO");
						throw re;
					} else if (code == message.ERROR_CONCURRENT) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("Ya existe otra sesión abierta con el usuario " + m_user + ".\n" +
								"Este error también se produce tras un cierre brusco (en ese caso reintente en 2 minutos).");
						throw re;
					} else if (code == message.ERROR_LICENSE_DATE) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("Se ha excedido la fecha máxima de uso de la aplicación");
						throw re;
					} else if (code == message.ERROR_LICENSE_USER) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						try {
							re.setUserMessage("Se ha excedido el número máximo de usuarios conectados según la licencia ("+eRes.getIntPropertie(0)+").");
						} catch (NoSuchFieldException e) {
							re.setUserMessage("Se ha excedido el número máximo de usuarios conectados según la licencia.");
						}
						throw re;
					} else if (code == message.ERROR_LICENSE_MISSING) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("No existe o se ha borrado la licencia. Consulte con su administrador.");
						throw re;
					} else if (code == message.ERROR_LICENSE_CORRUPT) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("La licencia actual no es válida o está corrupta");
						throw re;
					} else if (code == message.ERROR_REMOTE_IP) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("Ya existe otra sesión abierta en otra máquina con el usuario " + m_user);
						throw re;
					} else if (code == message.ERROR_DISCONNECTED) {
//						JOptionPane.showMessageDialog(m_cliente, "Esta sesión ha sido desconectada por inactividad. Reinicie el navegador.");
						//Singleton.closeApplication();
						RemoteSystemException re=new RemoteSystemException(0,"Esta sesión ha sido desconectada por inactividad. Reinicie el navegador.");
						re.setUserMessage("Esta sesión ha sido desconectada por inactividad. Reinicie el navegador.");
						throw re;
					} else if (code == message.ERROR_TIME_OUT) {
//						JOptionPane.showMessageDialog(m_cliente, "No puede realizarse la operación.");
						RemoteSystemException re=new RemoteSystemException(0,"El tiempo de espera a expirado. No puede realizarse la operación.");
						re.setUserMessage("El tiempo de espera a expirado. No puede realizarse la operación.");
						throw re;
					} else if (code == message.ERROR_LOCKED) {
						String mensajeError = "El recurso esta bloqueado.";
						Element descripcion = (Element)eRes.getContent();
					
						if (descripcion!=null && descripcion instanceof Element) {
							mensajeError = descripcion.getText();
						}
//						JOptionPane.showMessageDialog(m_cliente, "Recurso bloqueado.");
						InstanceLockedException ie=new InstanceLockedException(mensajeError);
						ie.setUserMessage(mensajeError);
						throw ie;
					} else if (code == message.ERROR_PERMISSION) {
//						JOptionPane.showMessageDialog(m_cliente, "No se tienen los permisos necesarios");
						RemoteSystemException re=new RemoteSystemException(0,"No se tienen los permisos necesarios");
						re.setUserMessage("No se tienen los permisos necesarios");
						throw re;
					} else if (code == message.ERROR_LOGIN) {
//						JOptionPane.showMessageDialog(m_cliente, "No se tienen los permisos necesarios");
						RemoteSystemException re=new RemoteSystemException(message.ERROR_LOGIN,"");
						re.setUserMessage("La identificación no es correcta. Recuerde que debe respetar mayúsculas y minúsculas");
						throw re;
					} else if (code == message.ERROR_DATA_REMOTE) {
						String mensajeError = "";
						Element descripcion = (Element)eRes.getContent();
						if (descripcion!=null && descripcion instanceof Element) {
							mensajeError = descripcion.getText();
						}
						//System.out.println(mensajeError);
//						JOptionPane.showMessageDialog(m_cliente, mensajeError);
						RemoteSystemException re=new RemoteSystemException(0,mensajeError);
						re.setUserMessage(mensajeError);
						throw re;
					}else if(code == message.ERROR_SESSION){
						String mensajeError = "Sesión no activa. Para poder trabajar tendrá que volver a identificarse";
						Element descripcion = (Element)eRes.getContent();
					
						if (descripcion!=null && descripcion instanceof Element) {
							mensajeError = descripcion.getText();
						}
						RemoteSystemException ie = new RemoteSystemException(message.ERROR_SESSION,mensajeError);
						ie.setUserMessage(mensajeError);
						throw ie;
					} else if (code == message.ERROR_USER_ALREADY_LOGGED) {
						String mensajeError = "Este usuario ya está identificado en el sistema o su sesión no fue cerrada correctamente";
						Element descripcion = (Element)eRes.getContent();
					
						if (descripcion!=null && descripcion instanceof Element) {
							mensajeError = descripcion.getText();
						}
						RemoteSystemException ie = new RemoteSystemException(message.ERROR_USER_ALREADY_LOGGED,mensajeError);
						ie.setUserMessage(mensajeError);
						throw ie;
					} else if (code == message.ERROR_LICENSE_CODE_UNAVAILABLE) {
						RemoteSystemException re=new RemoteSystemException(0,"");
						re.setUserMessage("El código de licencia no es válido o ya ha sido utilizado");
						throw re;
					}
					return eRes;
				}
				if (eRes.getSuccess()){
					return eRes;
				}
				/*if (!eRes.getSuccess()) {
                        i = m_MaxReintentos + 1;
                        error = errorAccionRemota;
                    } else {
                        return eRes;
                    }*/
			} else {
				return null;
			}

			if (i >= m_MaxReintentos) {
				System.err.println("COM, excedidos reintentos");
				if (error == errorComunicaciones) {
					//Window win = Singleton.getCurrentWin();
					Object[] options = {"REINTENTAR", "CANCELAR"};
					int selection = messagesControl.showOptionMessage(
							"Existe un problema de comunicaciones. ¿Desea reintentar la operación en curso?\n" +
							"Si cancela la operación puede perder los datos!!",
							"Warning",
							JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null,
							options,
							options[0],m_cliente);
					if (selection == 1) {
						throw new CommunicationException(
								"HA OCURRIDO UN ERROR GRAVE DE COMUNICACIONES.\n" +
						"HABLE CON SU ADMINISTRADOR");
					} else {
						i = 0;
					}
				}
				if (error == errorSystema || error == errorSLEEP) {
					throw new SystemException(errorSystema,
							"HA OCURRIDO UN ERROR GRAVE DEL SISTEMA.\n" +
					"HABLE CON SU ADMINISTRADOR");
				}
				if (error == errorAccionRemota) {
					throw new RemoteSystemException(0,"");
				}
			}
			error = 0;
			try {
				Thread.sleep(10*Constants.TIMEMILLIS);
			} catch (InterruptedException e) {
				System.err.println("COM, error durmiendo:" + e.getMessage());
				error = errorSLEEP;
			}
		}
		return null;
	}


	public message appLevel(message msg, boolean sendConfirm) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		return appLevel(msg, sendConfirm, true);
	}

	//llamar a esta funcion solo cuando se llame a appLevel pasándole el mensaje como String
	void prepareMsg( message msg ){
		if( msg.getMsguid()==null )
			msg.setMsguid(newMsgUID());
		msg.setVersion(m_ver);
		if (mode!=null){
			msg.setMode(mode);
		}
		if(subscription!=null){
            msg.setSubscription(subscription);
		}
		if( msg.getUser()==null )
			msg.setUser(m_user);
		if( msg.getBusiness()==0 )
			msg.setBusiness(m_business);
		/*if( m_testState== communicator.ACTION_TEST )
			msg.setClientSession(helperConstant.byPassKey);
		else*/
			if (msg.getClientSession()== null)
				msg.setClientSession( m_userSession);
	}

	public message appLevel(message msg, boolean sendConfirm, boolean hasResponse) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		prepareMsg(msg);
		return appLevel(msg.toString(), sendConfirm, null, hasResponse);
	}

	private synchronized String privSendAndReceive(String msgString, boolean sendConfirm, boolean reenviar, boolean hasResponse)
		throws IOException {
		//No puedo "sincronized" aqui porque si falla comunicationtest volverá a llamar esta función
		//System.out.println("SIZE ENVIO=" + msg.length());
		String urlbase=m_host + ":"+m_port;
		if(useUrlBackup){
			urlbase=m_host_backup+":"+m_port_backup;
			System.out.println("CONMUTADO SERVER BUCKUP=" + urlbase);
		}
		URL url = new URL(m_protocolToSendAndReceive+"://" + urlbase +"/dynagent/HTTPGW");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Sendconfirm", (sendConfirm ? "true" : "false"));
		connection.setRequestProperty("reenviar", (reenviar ? "true" : "false"));
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Accept-Encoding", "gzip");
		connection.setRequestProperty("Content-Encoding", "gzip");
		connection.setConnectTimeout(defaultTimeOut);

		OutputStream out1 = connection.getOutputStream();
		PrintWriter out = new PrintWriter(new GZIPOutputStream(out1), false);
		try {			
			out.write(encriptar?CipherUtils.encrypt(msgString):msgString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		out.close();
		//Hasta que no llamo a getContentEnconding no se envia el mensaje
		String encoding = connection.getContentEncoding();
		if (hasResponse) {
			/*if( msg.indexOf("NEWOBJECT")>=0 && msg.indexOf("LOGERROR")==-1 && crak==null ){
               System.out.println("NEW CRAK");
                       crak = new crackState();
                   }
                  if( crak!=null && !crak.isOk() ){
               System.out.println("CRAK ON");
                       throw new IOException("CRACK");
                   }*/
			InputStream resultingInputStream = null;

			//create the appropriate stream wrapper based on
			//the encoding type
			if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
				resultingInputStream = new GZIPInputStream(connection.getInputStream());
			} else {
				//System.out.println("NOZIP");
				resultingInputStream = connection.getInputStream();
			}

			BufferedReader in = new BufferedReader(new InputStreamReader(resultingInputStream));
			StringBuffer res = new StringBuffer();
			String line = "";
			//System.out.println("COMIENZA RECEPCION");

			while ((line = in.readLine()) != null) {
				res.append(line + "\n");
			}
			String strRes=null;
			//System.out.println("Time UNZIP:" + (end - ini));
			if(encriptar && encoding != null && encoding.equalsIgnoreCase("gzip")){
				try {
					strRes=CipherUtils.decrypt(res.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else
				strRes=res.toString();
			in.close();
			connection.disconnect();
			//System.out.println("END RECEPCION");

			return strRes;
		} else {
			connection.disconnect();
			//System.out.println("END RECEPCION. NO SE ESPERA RESPUESTA.");
			return null;
		}
	}

	/*
    public String encode(String palabra) {
        byte[] buf = palabra.getBytes();
        ByteArrayInputStream ba = new ByteArrayInputStream(buf);
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        BASE64Encoder b64 = new BASE64Encoder();
        try {
            b64.encode(ba, bo);
            //System.out.println(bo);
        } catch (Exception e) {
            System.out.println("ERROR CODIFICACION" + e.getMessage());
            return null;
        }
        return bo.toString();
    }
	 */

	public String newMsgUID() {
		if (staticMsguid!=null)
			m_transactionID = staticMsguid;
		else {
			m_transactionID = "#ID:" + m_user + ":" +
			String.valueOf(System.currentTimeMillis()) + "#";
		}
		return m_transactionID;
	}

	public selectData getThreadData(Integer proType, Integer currPro, Integer currTask)
	throws SystemException, CommunicationException, RemoteSystemException, InstanceLockedException {
		message msg= new message(message.MSG_QUERY);
		msg.setDataType(message.DATA_THREAD);
		msg.addPropertie(properties.processType,proType);
		msg.addPropertie(properties.currPro,currPro);
		msg.addPropertie(properties.currTask,currTask);
		return (selectData)appLevel(msg, false).getContent();
	}


	/*private void setScopeData(proParser pp, scope myScope) {
        pp.setUserRol(myScope.getUserRol().intValue());
        pp.setAccess(myScope.getRootAccess());
    }*/

	/*    private void setScopeData(message msg, /*scope myScope*//*int userRol, access access) {
        msg.setUserRol(/*myScope.getUserRol().intValue()*//*userRol);
	if( msg instanceof contextAction )
	    ((contextAction)msg).setAccess(/*myScope.getRootAccess()*//*access);
    }
	     */
	
	public URL getURL(String name){
		URL url=null;
		try {
			//url = new URL(m_cliente.getDocumentBase()+"images/"+name);
			url = new URL(jarURL+"../images/"+name);
		} catch (MalformedURLException e) {
			logError(e);
		}
		return url;
	}
	
	public ImageIcon getIcon(String name){
		//name = name.replaceAll(" ", "_");		
		ImageIcon icon=null;
		try {
			if(iconsCache.containsKey(name))
				icon=iconsCache.get(name);
			else{
				//icon = new ImageIcon(new URL(m_cliente.getDocumentBase(), "images/" + name + ".gif"));
				icon = new ImageIcon(new URL(jarURL+"../images/" + name + ".gif"));
				iconsCache.put(name, icon);
			}
		} catch (MalformedURLException e) {
			logError(e);
		}
		return icon;
	}
	
	public ImageIcon getIcon(ImageObserver obs, String icon, int maxAncho,
			int maxAlto) {
		/*if (icon.equals("nulo") && m_iconNulo != null) {
			return m_iconNulo;
		}*/
		ImageIcon img=null;
		try {
			if(iconsCache.containsKey(icon))
				img=iconsCache.get(icon);
			else{
				//img = new ImageIcon(new URL(m_cliente.getDocumentBase(), "images/" + icon + ".gif"));
				img = new ImageIcon(new URL(jarURL+"../images/" + icon + ".gif"));
				iconsCache.put(icon, img);
			}
			
		} catch (MalformedURLException e) {
			logError(e);
		}
		if (maxAncho == 0) {
			return img;
		}
		return new ImageIcon(escalarImg(obs, img.getImage(), maxAncho, maxAlto));
	}
	
	public static ImageIcon getIconNotCache(URL jarURL,ImageObserver obs, String icon, int maxAncho, int maxAlto) {
		/*if (icon.equals("nulo") && m_iconNulo != null) {
			return m_iconNulo;
		}*/
		ImageIcon img=null;
		try {
			//img = new ImageIcon(new URL(m_cliente.getDocumentBase(), "images/" + icon + ".gif"));
			img = new ImageIcon(new URL(jarURL+"../images/" + icon + ".gif"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		if (maxAncho == 0) {
			return img;
		}
		int width=img.getImage().getWidth(obs);
     	int height=img.getImage().getHeight(obs);
		ImageIcon i=new ImageIcon(escalarImg(obs, img.getImage(), width>=height?maxAncho:-1, width<=height?maxAlto:-1));
		//System.err.println(i.getIconWidth()+" "+i.getIconHeight());
		return i;
	}

	public Image getImage(String icon) {
		Image img=null;
		if(iconsCache.containsKey(icon))
			img=iconsCache.get(icon).getImage();
		else{
			//m_cliente.getImage(m_cliente.getDocumentBase(), "images/" + icon + ".gif");
			try {
				img = m_cliente.getImage(new URL(jarURL+"../images/" + icon + ".gif"));
				iconsCache.put(icon, new ImageIcon(img));
			} catch (MalformedURLException e) {
				logError(e);
			}
		}
		
		return img;
		
	}
	
	public URL serverGetFilesURL(String filePath){
		try {
			return new URL(jarURL+"../" + Constants.folderUserFiles + "/"+this.getBusiness()+"/"+filePath);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @deprecated Use {@link #getFileUrl} instead.
	 */
	@Deprecated
	public String getDbImgUrl(int id) {
		return "http://" + m_host + ":" + m_port +
		"/dynagent/HTTPGW?TYPE=GETIMAGE&ID=" + id + "&BNS=" +
		m_business;
	}
	
	public String getReportUrl(String reportName){
		/*
		 *  Actualmente reportName surge de rAttributes.get(QueryConstants.PATH_FILE) que devuelve
		 *  reports/nombre_report.pdf con lo cual recortamos la primera parte de la cadena.
		 */
		if (reportName == null){
			return null;
		}
		String [] workingStrings = reportName.split("/");
		String pdfName = workingStrings[workingStrings.length - 1];
		
		String url = "http://" + m_host + ":" + m_port + "/dynagent/HTTPGW?" + Constants.REQUEST_PARAM_TYPE + "=" + Constants.REQUEST_TYPE_GETREPORT + "&" + Constants.REQUEST_PARAM_ID + "=" + pdfName;
		return url;
		
	}
	
	public String getXsltUrl(String xsltName){
		String url = "http://" + m_host + ":" + m_port + "/dyna/xslt/" + xsltName;
		return url;
	}
	
	/**
	 * Devuelve la ruta URL para obtener un fichero.
	 * 
	 * @param filePath
	 *            Nombre del fichero que queremos obtener.
	 * @return Ruta al fichero solicitado.
	 */
	private String getFileUrl(String filePath, boolean forceDownloadDialog){
		if (filePath == null){
			return null;
		}
		String [] workingStrings = filePath.split("/");
		String fileName = workingStrings[workingStrings.length - 1];
		
		String url = "http://" + m_host + ":" + m_port + "/dynagent/HTTPGW?" + Constants.REQUEST_PARAM_TYPE + "=" + Constants.REQUEST_TYPE_GETFILE + "&" + Constants.REQUEST_PARAM_ID + "=" + fileName + "&" +
						Constants.REQUEST_PARAM_BNS + "=" + m_business + "&" + Constants.REQUEST_PARAM_SESSION + "=" + m_userSession + "&" + Constants.REQUEST_PARAM_DOWNLOAD + "=" + forceDownloadDialog;
		return url;
	}

	public static Image escalarImg(ImageObserver obs, Image img, int maxAncho,
			int maxAlto) {
		return img.getScaledInstance(maxAncho, maxAlto, Image.SCALE_SMOOTH);
	}
/*
	public Image getDbImage(ImageObserver obs, String id, int maxAncho,
			int maxAlto) throws SystemException {
		if (id == null) {
			return null;
		}
		try {
			String urlV;
			urlV = "http://" + m_host + ":" + m_port +
			"/dynagent/HTTPGW?TYPE=GETIMAGE&ID=" + URLEncoder.encode(id, "UTF-8") + "&BNS=" +
			m_business;
			Image img =m_cliente.getImage(new URL(urlV));
			if (maxAncho == 0) {
				return img;
			}
			return escalarImg(obs, img, maxAncho,maxAlto);
		} catch (java.net.MalformedURLException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		} catch (UnsupportedEncodingException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		}
		
	}

	public String newImage(int tapos, String tmpcode) throws SystemException {
		try {
			if (tmpcode == null || tmpcode.length() == 0) {
				tmpcode = newMsgUID();
			}

			//System.out.println("MOSTRANDO IMAGEN " + tmpcode);
			String urlV = "http://" + m_host + ":" + m_port +
			"/dynagent/HTTPGW?STEP=1&TYPE=SETIMAGE&TAPOS=" +
			tapos +
			"&BNS=" + m_business +
			"&TMPCODE=" + URLEncoder.encode(tmpcode, "UTF-8");
			//System.out.println("URL " + urlV);

			m_cliente.getAppletContext().showDocument(new URL(urlV),
			"CARGA DE IMAGEN");
			return tmpcode;
		} catch (java.net.MalformedURLException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		} catch (UnsupportedEncodingException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		}
	}

	public void viewImage(String id) throws SystemException {
		try {
			if (id == null || id.length() == 0) {
				return;
			}
			String urlV = "http://" + m_host + ":" + m_port +
			"/dynagent/HTTPGW?TYPE=GETIMAGE&ID=" +
			URLEncoder.encode(id, "UTF-8") + "&BNS=" + m_business;
			m_cliente.getAppletContext().showDocument(new URL(urlV),
			"SELECCION IMAGEN");
		} catch (java.net.MalformedURLException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		} catch (UnsupportedEncodingException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		}
	}

	public void delImage(String id) throws SystemException {
		try {
			if (id == null || id.length() == 0) {
				return;
			}
			String urlV = "http://" + m_host + ":" + m_port +
			"/dynagent/HTTPGW?TYPE=DELIMAGE&ID=" +
			URLEncoder.encode(id, "UTF-8") + "&BNS=" + m_business;
			m_cliente.getAppletContext().showDocument(new URL(urlV),
			"SELECCION IMAGEN");
		} catch (java.net.MalformedURLException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		} catch (UnsupportedEncodingException e) {
			//messagesControl.showErrorMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,
			"Error, la URL no esta bien construida");
		}
	}
*/

	/*    public void showReport(scope myScope, String title, String oidRpt, int ido) throws
            SystemException {
        try {
            String urlV = "http://" + m_host + ":" + m_port +
                          "/dynagent/HTTPGW?TYPE=REPORT&BNS=" + m_empresa +
                          "&USER=" + m_user +
                          "&ROL=" + myScope.getUserRol().toString() +
                          "&ACCESS=" + myScope.getRootAccess().toString() +
                          "&SUB=DET&IDRPT=" + oidRpt + "&IDO=" + ido;
            m_cliente.getAppletContext().showDocument(new URL(urlV), title);
        } catch (java.net.MalformedURLException e) {
            Singleton.showMessageDialog("Error, la URL no esta bien construida");
            throw new SystemException(SystemException.ERROR_DATOS,
                                      "Error, la URL no esta bien construida");
        }
    }
	 */
	
	private void prePrintSequence(HashMap<String,String> rAttributes) throws SystemException {
		printSequence(rAttributes, true);
	}
	private void postPrintSequence(HashMap<String,String> rAttributes) throws SystemException {
		printSequence(rAttributes, false);
	}
	
	private void printSequence(HashMap<String,String> rAttributes, boolean pre) throws SystemException {
		//imprimir secuencia
		ReportPrinter.printSequence(rAttributes, pre);
	}
	
	public void showReport(String title, HashMap<String,String> rAttributes, boolean automatizar_copias,boolean isLastCopy) throws SystemException {
		String directImpresion=null;
		try {

			//String urlV = m_cliente.getDocumentBase()+oidRpt;
			String urlV = getReportUrl(rAttributes.get(QueryConstants.PATH_FILE));
			if(useUrlBackup) urlV=urlV.replaceFirst(m_host+":"+m_port, m_host_backup+":"+m_port_backup);
			useUrlBackup=false;
			//String urlV = serverURL+"../"+rAttributes.get(QueryConstants.PATH_FILE);
			System.err.println("Report:"+urlV);
			directImpresion= rAttributes.get(QueryConstants.DIRECT_IMPRESION);
			if (directImpresion.equals("TRUE")) {
				ReportPrinter.printReport(rAttributes, urlV,automatizar_copias);
				if(isLastCopy) postPrintSequence(rAttributes);
			} else {
				//m_cliente.getAppletContext().showDocument(new URL(urlV), title);
				Desktop.getDesktop().browse(new URL(urlV.replace(" ", "%20")).toURI());
				//String cmd = "rundll32 url.dll,FileProtocolHandler " + new URL(urlV);
				//Runtime.getRuntime().exec(cmd);		
			}
		} catch (java.net.MalformedURLException e) {
			//Singleton.getMessagesControl().showMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_DATOS,"Error, la URL no esta bien construida");
		} catch (IOException e) {
			//Singleton.getMessagesControl().showMessage("Error, la URL no esta bien construida");
			throw new SystemException(SystemException.ERROR_JASPER,directImpresion!=null&&directImpresion.equals("TRUE")?"No se encuentra la impresora, compruebe sus conexiones o reiniciela.":"No es posible mostrar el documento");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS,"Error, la URL no esta bien construida");
		}
	}

	public HashMap<String,String> serverGetReport(Element queryWhere, Integer userTask, String className, boolean directImpresion, Integer idoFormat, boolean printSequence,boolean ejecuta_preseq) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//return serverGetReport(queryWhere, getUser(), userTask, m_cliente.getDocumentBase().getFile().replaceAll("/",""));
		
		if(className!=null&&reportsRoutedToMainServer!=null&&reportsRoutedToMainServer.length()>0){
			String pattern=".*"+className.replaceAll("\\s|_", "").replaceAll(".+@", "")+".*";
			System.out.println("pattern "+pattern);
			boolean printMain=reportsRoutedToMainServer.replaceAll("\\s|_", "").matches(pattern);		
			System.out.println("Definido reports backup "+printMain+" "+reportsRoutedToMainServer.replaceAll("\\s|_", "")+" classname:"+className.replaceAll("\\s|_", "").replaceAll(".+@", ""));
			
			if(!printMain) useUrlBackup=true;
		}		
		
		return serverGetReport(queryWhere, getUser(), userTask, className, /*serverURL.getFile()+"../",*/ directImpresion, idoFormat, printSequence,ejecuta_preseq);
	}
	
	public HashMap<String,String> serverGetReport(Element queryWhere, String user, Integer userTask, String className, /*String nameProject,*/ boolean directImpresion, Integer idoFormat, boolean printSequence,boolean ejecuta_preseq) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.err.println(m_cliente.getDocumentBase().getFile());
		if (className!=null && directImpresion && printSequence && ejecuta_preseq)
			serverGetPrePrintSequence(className);
		
		System.err.println(serverURL.getFile()+"../");
		contextAction root = new contextAction(m_business,
				user,
				/*myScope.getUserRol().intValue(),*/null,
				userTask,
				message.ACTION_REPORT,
				(IndividualData)null);
		root.setContent(queryWhere);
		root.setType(message.MSG_EXE_TRAN_ACTION);
		if (className!=null)
			root.addPropertie(properties.className, className);
		root.addPropertie(properties.directImpresion, directImpresion);
		//root.addPropertie(properties.nameProject, nameProject);
		String idoFormatStr = null;
		if (idoFormat!=null) {
			idoFormatStr = String.valueOf(idoFormat);
			root.addPropertie(properties.format, idoFormatStr);
		}
		System.out.println(root.toString());
		message mes = (message)appLevel(root, false);
		HashMap<String,String> rAttributes = mes.getDirectAttribute();
		
		if(!printSequence){//Si no queremos imprimir secuencia borramos ese atributo por si viniera
			rAttributes.remove(QueryConstants.POSTPRINT_SEQUENCE);
		}
		//System.err.println("rAttributes " + rAttributes);
		return rAttributes;
	}
	
	public HashMap<String,String> serverGetPrePrintSequence(String className) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message root = new message(message.MSG_PREPRINT);
		root.addPropertie(properties.className, className);
		
		message mes = (message)appLevel(root, false);
		HashMap<String,String> rAttributes = mes.getDirectAttribute();
		//System.err.println("rAttributes " + rAttributes);
		prePrintSequence(rAttributes);
		return rAttributes;
	}

    public IndividualData serverGetFactsInstanceOfQuery(Element query, Integer uTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		IndividualData aipd = new IndividualData();
    	message msg= new message();
		//Element query = (Element) filter.clone();
		query.setName("QUERY");
		msg.setContent(query);
		msg.setType(message.MSG_QUERY);
		msg.setDataType(message.DATA_INSTANCE);
		msg.setUserTask(uTask);
		prepareMsg(msg);
		msg.addPropertie(properties.modeQuery, queryData.MODE_FACT);
		
		
		message msgRes = appLevel(msg.toString(), false, query, true);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED){
				InstanceLockedException ie = new InstanceLockedException("");
				ie.setUserMessage("Recurso Bloqueado.");
				throw ie;
			}
		}
		aipd = (IndividualData)((contextAction)msgRes).getIndividualData();
		return aipd;
    }

	public selectData serverGetQuery(Element query, Integer uTask, int mode) throws  SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg= new message();
		//Element query = (Element) filter.clone();
		query.setName("QUERY");
		msg.setContent(query);
		msg.setType(message.MSG_QUERY);
		msg.setDataType(message.DATA_INSTANCE);
		msg.setUserTask(uTask);
		prepareMsg(msg);
		msg.addPropertie(properties.modeQuery, mode); //queryData.MODE_ROW
		/*setScopeData(msg, /*myScope*//*userRol, access);*/
		//	if(userRol!=null)	
		//		msg.setUserRol(userRol);

		/*int idto = Integer.parseInt(filter.getChild("CLASS").getAttributeValue("ID_TO"));*/

		selectData response = (selectData)appLevel(msg.toString(),
				false,
				query,
				true).getContent();
		/*Iterator itr= response.getIterator();
		while(itr.hasNext()){
			instance obj = (instance)itr.next();
			try {
				cacheaInstAt(obj);
			} catch (ParseException e) {
				throw new SystemException(SystemException.ERROR_DATOS,
						e.getMessage());
			}
		}*/
		return response;
	}

	public ArrayList<RowItem> serverGetQueryRowItem(Element query, Integer uTask) throws  SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg= new message();
		//Element query = (Element) filter.clone();
		query.setName("QUERY");
		msg.setContent(query);
		msg.setType(message.MSG_QUERY);
		msg.setDataType(message.DATA_INSTANCE);
		msg.setUserTask(uTask);
		prepareMsg(msg);
		msg.addPropertie(properties.modeQuery, queryData.MODE_ROW_ITEM);
		/*setScopeData(msg, /*myScope*//*userRol, access);*/
		//	if(userRol!=null)	
		//		msg.setUserRol(userRol);

		/*int idto = Integer.parseInt(filter.getChild("CLASS").getAttributeValue("ID_TO"));*/

		ArrayList<RowItem> response = (ArrayList<RowItem>)appLevel(msg.toString(),
				false,
				query,
				true).getContent();
		/*Iterator itr= response.getIterator();
		while(itr.hasNext()){
			instance obj = (instance)itr.next();
			try {
				cacheaInstAt(obj);
			} catch (ParseException e) {
				throw new SystemException(SystemException.ERROR_DATOS,
						e.getMessage());
			}
		}*/
		return response;
	}

	public org.jdom.Element serverGetMetaData() throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg= new message(message.MSG_QUERY);		
		msg.setDataType(message.DATA_META);
		Element a=(Element)appLevel(msg, false).getContent();
		return a;
	}
	
	/**
	 * Método de prueba para conseguir enviar el JRXML del servidor
	 * 
	 * @return Elemento devuelto por el servidor tras atender la petición
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> serverGetJRXML(Integer reportIdto) throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {
		contextAction root = new contextAction(message.ACTION_GET_JRXML);
		root.setDataType(message.DATA_REPORT_JRXML);
		root.setTO_ctx(reportIdto);
		message returnMessage = appLevel(root,false);
		ArrayList<String> reports;
		try {
			reports = (ArrayList<String>) returnMessage.getListPropertie(properties.jrxml);
		} catch (NoSuchFieldException e) {
			throw new SystemException(SystemException.ERROR_DATOS, "ERROR AL INTENTAR OBTERNER EL JRXML");
		}
		return reports;
	}


	/*    public boolean esTipoSuperior(metaData md, int tipo, int tipoSuperior,
                                  int process, int step) throws SystemException {
        Element metaTO = md.getMetaTO(new Integer(tipo));
        Iterator itr = metaTO.getChild("SUPER").getChildren("ITEM").iterator();
        while (itr.hasNext()) {
            Element sup = (Element) itr.next();
            if (sup.getAttributeValue("ID_TO").equals(String.valueOf(
                    tipoSuperior))) {
                return true;
            }
        }
        return false;
    }
	 */
	/*    public int ask_forTO_noAbstracto(metaData md,
                                     Component parent,
                                     boolean permitirAbstract,
                                     int to /*,
                   int process,
                   int step*//*) throws SystemException {
    System.out.println("ask_forTO_noAbstracto TO "+to);
        Element metaTO = md.getMetaTO(new Integer(to));
	try{
            jdomParser.returnXML(metaTO);
        }catch(Exception e){
	    	;
	}
        if (metaTO.getAttributeValue("ABSTRACT") != null &&
            metaTO.getAttributeValue("ABSTRACT").equals("TRUE")) {
            if (permitirAbstract) {
                return ask_forAbstractTree(md, parent, permitirAbstract,
                                           metaTO /*, process, step*//*);
            }
            ArrayList opciones = new ArrayList();
            boolean esArbol = false;
            Iterator itr = metaTO.getChild("SPECIALIZED").getChildren("ITEM").
                           iterator();
            while (itr.hasNext()) {
                Element hijo = (Element) itr.next();
                Integer toSpec = new Integer(hijo.getAttributeValue("ID_TO"));
                Element metaSpec = md.getMetaTO(toSpec);
                if (metaSpec.getAttributeValue("ABSTRACT") != null &&
                    metaSpec.getAttributeValue("ABSTRACT").equals("TRUE")) {
                    return ask_forAbstractTree(md, parent, permitirAbstract,
                                               metaTO /*, process, step*//*);
                }

                itemList it = new itemList(hijo.getAttributeValue("ID_TO"),
                                           null,
                                           hijo.getAttributeValue("NAME"),
                                           false);
                opciones.add(it);
            }
            if (opciones.size() > 0) {
                itemList selectedValue = (itemList) JOptionPane.showInputDialog(
                        parent,
                        "SELECCIONA TIPO:",
                        null,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        opciones.toArray(),
                        null);
                return selectedValue.getIntId();
            }
        }
        return to;
    }
                                                */
	/*private void expandTreeNode(JTree tree, DefaultMutableTreeNode node) {
		tree.makeVisible(new TreePath(node.getPath()));
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.
			getChildAt(i);
			expandTreeNode(tree, child);
		}
	}*/

	/*    private int ask_forAbstractTree(metaData md,
                                    Component parent,
                                    boolean permitirAbstract,
                                    Element metaTO /*,
                   int process,
                   int step *//*) throws SystemException {
        DefaultMutableTreeNode top = buildAbstractTree(md, null, 1,
                metaTO /*, process, step *//*);
        JTree tree = new JTree(top);
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        TreePath tpath = new TreePath(top);
        tree.expandPath(tpath);
        itemList userSelection = null;

        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.
                                                  SINGLE_TREE_SELECTION);
        expandTreeNode(tree, top);

        JOptionPane.showMessageDialog(parent, tree, "SELECIONA TIPO",
                                      JOptionPane.PLAIN_MESSAGE);

        if (tree.getLastSelectedPathComponent() == null) {
            JOptionPane.showMessageDialog(parent, "DEBE SELECCIONAR UN OBJETO");
            return ask_forAbstractTree(md, parent, permitirAbstract,
                                       metaTO /*, process, step *//*);

        }
        itemList iSel = (itemList) ((DefaultMutableTreeNode) tree.
                                    getLastSelectedPathComponent()).
                        getUserObject();

        if (!permitirAbstract && iSel.getId2() != null &&
            iSel.getId2().equals("TRUE")) {
            JOptionPane.showMessageDialog(parent, "SELECCION NO VALIDA");
            return ask_forAbstractTree(md, parent, permitirAbstract,
                                       metaTO /*, process, step*//*);
        }

        return iSel.getIntId();
    }
                                        */
	/*    private DefaultMutableTreeNode buildAbstractTree(metaData md,
            DefaultMutableTreeNode treeRoot,
            int distanciaToAbstractRoot,
            Element metaTO /*,
               int process,
               int step*/
	/*            ) throws SystemException {

        itemList it = new itemList(metaTO.getAttributeValue("ID_TO"),
                                   metaTO.getAttributeValue("ABSTRACT"),
                                   metaTO.getAttributeValue("TO_NAME"),
                                   false);

        int currDistance = distanciaToAbstractRoot;
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(it);
        if (treeRoot == null) {
            treeRoot = node;
        } else {
            treeRoot.add(node);
        }

        if (metaTO.getAttributeValue("ABSTRACT") != null &&
            metaTO.getAttributeValue("ABSTRACT").equals("TRUE")) {

            currDistance = 1;
        } else {
            currDistance++;
        }

        if (currDistance > 1) {
            return treeRoot;
        }

        Iterator itr = metaTO.getChild("SPECIALIZED").getChildren("ITEM").
                       iterator();
        while (itr.hasNext()) {
            Element hijo = (Element) itr.next();
            Integer toSpec = new Integer(hijo.getAttributeValue("ID_TO"));
            Element metaSpec = md.getMetaTO(toSpec);
            buildAbstractTree(md, node, currDistance,
                              metaSpec /*, process, step*//*);
        }
        return treeRoot;
    }
                               */
	public void logError(String user, String debug, String error, String subject) throws RemoteSystemException, CommunicationException, InstanceLockedException {
		try {
			String ext = "";
			if (subject != null && subject.length() > 10) {
				ext = subject.substring(0, 10) + "..";
			}
			monitorTitle = "SE HA PRODUCIDO UN ERROR " + ext;
			//System.out.println("LOGGIN ERROR pre envio");
			dynagent.common.communication.errorTrace order= null;
			//System.out.println("m_liveDaemon " + m_liveDaemon);
			//if (this.m_liveDaemon!=null)
			//	order= new dynagent.common.communication.errorTrace(debug, error, subject, message.MSG_LOG_ERROR );
			//else
				order= new dynagent.common.communication.errorTrace(debug, error, subject, message.MSG_LOG_ERROR_LOGIN );
			
			if(debugLog!=null){
				debugLog.clearDebugData();
			}
			appLevel(order, false);
			if(debugLog!=null){
				debugLog.clearDebugData();
			}
		} catch (SystemException ex) {
			messagesControl.showErrorMessage(
					"Se ha producido un error y no es posible enviarlo.\n" +
					"le rogamos que envie la siguiente descripción al email a soporte@dynagent.net:\n" +
					ex.toString(),m_cliente);
		} finally {
			setMonitorDefault();
		}
	}
	
	/* Si subject=null no muestra ventana con mensaje al usuario.
	 * Si subject!=null muestra ese mensaje seguido del userMessage, si lo tiene, de la excepcion*/
	public void logError(final Window window, Exception e, String subject) {
		try {
			boolean showMessage=subject!=null;
			//System.out.println("LOGGIN ERROR");
		
			//if(!(e instanceof Exception))//Si es una de nuestras excepciones le asignamos el mensaje
				if (e instanceof IUserException){
					if(subject==null || subject.isEmpty())
						subject=((IUserException)e).getUserMessage();
					else subject+=": "/*+e.getMessage()+ " "*/+ ((IUserException)e).getUserMessage();
					
				}else
					subject+=": "+e.getMessage();
					
				
			try {
				if (showMessage) {
					final String message=subject;
					Runnable invocaEnGUI = new Runnable() {
						public void run() {
							messagesControl.showErrorMessage(message,window!=null?window:SwingUtilities.getWindowAncestor(m_cliente));
						}
					};
					//De esta manera evitamos que no se registre el error si al mostrar la ventana ocurre algun error
					SwingUtilities.invokeLater(invocaEnGUI);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			outputStringAdaptor oa = new outputStringAdaptor();
			String error="No excepcion";
			if( e!=null ){
				e.printStackTrace(new PrintStream(oa));
				error= oa.data;
			}
			String debug = debugLog!=null?debugLog.getDebugData():null;
			logError(m_user, debug, error, subject);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	public void logError(Exception e) {
		logError(null,e, "");
	}

	/* public org.jdom.Element serverGetFlowConfigs(int idto,
                                                 int rel,
                                                 int rol_context,
                                                 int rol_current,
                                                 int process,
                                                 int task,
                                                 int taskTransition,
                                                 int action) throws
            SystemException, CommunicationException, RemoteSystemException {

        Element rootOrder = new Element("GETFLOWCONFIGS");
        rootOrder.setAttribute("MSG_TYPE", "GETFLOWCONFIGS");
        rootOrder.setAttribute("ID_TO", String.valueOf(idto));
        rootOrder.setAttribute("CURR_PROCESS", String.valueOf(process));
        rootOrder.setAttribute("USER", m_user);
        if (task > 0) {
            rootOrder.setAttribute("CURR_TASK", String.valueOf(task));
        }
        if (action > 0) {
            rootOrder.setAttribute("CURR_TRAN_ACTION", String.valueOf(action));
        }
        if (taskTransition > 0) {
            rootOrder.setAttribute("TASK_TRANSITION",
                                   String.valueOf(taskTransition));
        }
        if (rel > 0) {
            rootOrder.setAttribute("ID_REL", String.valueOf(rel));
            rootOrder.setAttribute("ID_ROL_CONTEXT", String.valueOf(rol_context));
            rootOrder.setAttribute("ID_ROL_CURRENT", String.valueOf(rol_current));
        }
        return (Element)appLevel(rootOrder, false).getContent();
    }*/

	public org.jdom.Element serverGetConfig(int config) throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {

		Element rootOrder = new Element("GETCONFIG");
		rootOrder.setAttribute("MSG_TYPE", "GETCONFIG");
		rootOrder.setAttribute("CONFIG", String.valueOf(config));
		return (Element)appLevel(rootOrder, false).getContent();
	}

	public message serverPolling() throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {
		pooling msg= new pooling();
		//msg.setUser(m_user);
		/*for (int i = 0; i < m_locksControls.size(); i++) {
			HashSet<Integer> lista = (HashSet<Integer>) m_locksControls.get(i);
			if (lista.size() == 0) {
				m_locksControlUsadas.remove(lista);
				continue;
			} else {
				m_locksControlUsadas.add(lista);
			}
			msg.addLockOrder(lista);
		}*/
		return appLevel(msg, false);
	}

	public message serverPolling(String msgid ) throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {
		pooling msg= new pooling();
		msg.setMsguid(msgid);
		/*for (int i = 0; i < m_locksControls.size(); i++) {
			HashSet<Integer> lista = (HashSet<Integer>) m_locksControls.get(i);
			if (lista.size() == 0) {
				m_locksControlUsadas.remove(lista);
				continue;
			} else {
				m_locksControlUsadas.add(lista);
			}
			msg.addLockOrder(lista);
		}*/
		return appLevel(msg, false);
	}

	public message serverLogin(boolean overwrite) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg= new message(overwrite?message.MSG_LOGIN_OVERWRITTEN:message.MSG_LOGIN);
		msg.addDirectAttribute(UserConstants.USER_PWD,m_pwd);
		if(m_businessName!=null) msg.addDirectAttribute(UserConstants.BUSINESS_NAME,m_businessName);
		//if( m_pwd.equals(helperConstant.byPassKey))
		//	m_testState= ACTION_TEST;
		return appLevel(msg, false);
	}

	public IndividualData serverGetFactsInstance(int ido, int idto, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) throws SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException{
		IndividualData aipd = new IndividualData();
		//Integer iDO = new Integer(ido);

		/*
		 * instance instanceObject = m_cacheInstance.getInstance(iDO); if
		 * (!bloquear && instanceObject != null) { return instanceObject; } else {
		 */
		 // if (idto == helperConstant.TO_METATIPO) {
			 // instance res = new instance( /*null, action.GET,*/
		// helperConstant.TO_METATIPO, ido);
		// /*res.setRdn(null,m_md.getTOLabel(new Integer(idto)));*/
		// res.setRdn(m_kb.getClassName(new Integer(idto)));
		// return res;
		// }
		contextAction root = new contextAction(m_business, user,
				/* myScope.getUserRol().intValue(), */0, 0, message.ACTION_GET, (IndividualData)null);
		// root.setType(message.ACTION_GET);
		root.setIDO_ctx(ido);
		root.setTO_ctx(idto);
		root.setDataType(message.DATA_INDIVIDUAL);
		/* root.setTO_ctx(idto); */
		root.setLock(lock);
		root.addPropertie(properties.levels, levels);
		root.addPropertie(properties.lastStructLevel, lastStructLevel);
		root.addPropertie(properties.returnResults, returnResults);
		/* setScopeData(root, /*myScope *//* userRol, access); */
//		root.setUserRol(userRol);

//		if (lightview)
//		root.addPropetie(properties.lightView, true);

		/*
		 * if (recursive) root.addPropetie(properties.recursive,true);
		 */

		message msgRes = appLevel(root, false);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED){
				InstanceLockedException ie = new InstanceLockedException("");
				ie.setUserMessage("Recurso Bloqueado.");
				throw ie;
			}
		}
		//Element data = (Element)msgRes.getContent();
		aipd = (IndividualData)((contextAction)msgRes).getIndividualData();
		// m_cacheInstance.putInstance(new Integer(ido),instanceObject,
		// bloquear);
		/*
		 * try { cacheaInstAt(instanceObject); } catch (ParseException e) {
		 * throw new SystemException(SystemException.ERROR_DATOS,
		 * e.getMessage()); }
		 */
//		if (bloquear)
//		commit();
		//System.out.println(aipd.toString());
		return aipd;
		// }
	}

	public IndividualData serverGetFactsInstance(HashMap<Integer,HashSet<Integer>> idos, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) throws SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException{
		IndividualData aipd = new IndividualData();
		contextAction root = new contextAction(m_business, user,
				/* myScope.getUserRol().intValue(), */0, 0, message.ACTION_GET, (IndividualData)null);
		
		HashMap<Integer,HashSet<Integer>> idObjects = new HashMap<Integer, HashSet<Integer>>();
		Iterator it = idos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = idos.get(idto);
			idObjects.put(idto, hIdos);
		}
		
		root.setAID_ctx(idObjects);
		root.setDataType(message.DATA_INDIVIDUAL);
		root.setLock(lock);
		root.addPropertie(properties.levels, levels);
		root.addPropertie(properties.lastStructLevel, lastStructLevel);
		root.addPropertie(properties.returnResults, returnResults);

		message msgRes = appLevel(root, false);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED){
				InstanceLockedException ie = new InstanceLockedException("");
				ie.setUserMessage("Recurso Bloqueado.");
				throw ie;
			}
		}
		//Element data = (Element)msgRes.getContent();
		aipd = (IndividualData)((contextAction)msgRes).getIndividualData();
		return aipd;
	}
	
	public IndividualData serverGetFactsInstanceOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, 
			RemoteSystemException, CommunicationException, NotFoundException, InstanceLockedException {
		IndividualData aipd = new IndividualData();
		contextAction root = new contextAction(m_business, user, 0, 0, message.ACTION_GET, (IndividualData)null);
		root.setTO_ctx(idto);
		root.setDataType(message.DATA_INDIVIDUAL_CLASS_SPECIALIZED);
		root.setLock(lock);
		root.addPropertie(properties.levels, levels);
		root.addPropertie(properties.lastStructLevel, lastStructLevel);
		
		message msgRes = appLevel(root, false);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED){
				InstanceLockedException ie=new InstanceLockedException("");
				ie.setUserMessage("Recurso bloqueado.");
				throw ie;
			}
		}
		aipd = (IndividualData)((contextAction)msgRes).getIndividualData();
		//System.out.println(aipd.toString());
		return aipd;
	}

	public IndividualData serverGetFactsInstanceOfClass(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, 
			RemoteSystemException, CommunicationException, NotFoundException, InstanceLockedException {
		IndividualData aipd = new IndividualData();
		contextAction root = new contextAction(m_business, user, 0, 0, message.ACTION_GET, (IndividualData)null);
		root.setTO_ctx(idto);
		root.setDataType(message.DATA_INDIVIDUAL_CLASS);
		root.setLock(lock);
		root.addPropertie(properties.levels, levels);
		root.addPropertie(properties.lastStructLevel, lastStructLevel);
		
		message msgRes = appLevel(root, false);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED){
				InstanceLockedException ie=new InstanceLockedException("");
				ie.setUserMessage("Recurso bloqueado.");
				throw ie;
			}
		}
		aipd = (IndividualData)((contextAction)msgRes).getIndividualData();
		//System.out.println(aipd.toString());
		return aipd;
	}

	/*public ArrayList<IPropertyDef> serverGetBusinessClass(int idto) throws SystemException, RemoteSystemException,
    		CommunicationException, InstanceLockedException {
		ArrayList<IPropertyDef> aipd = new ArrayList<IPropertyDef>();
		contextAction root = new contextAction(m_empresa, m_user,0, 0, 0, 0, message.ACTION_GET, null);
		root.setTO_ctx(idto);
		root.setDataType(message.DATA_BUSINESS_CLASS);

		message msgRes = appLevel(root, false);
		if (!msgRes.getSuccess()) {
			if (msgRes.getResultCode() == message.ERROR_LOCKED)
				throw new InstanceLockedException(idto);
		}
		aipd = messageFactory.buildFactsInstance(((Element)msgRes.getContent()));
		String aipdStr = "";
		for (int i=0;i<aipd.size();i++) {
			if (aipdStr.length()>0)
				aipdStr += ",";
			aipdStr += aipd.get(i);
		}
		System.out.println("ArrayList<IPropertyDef> get" + aipdStr);
		return aipd;
	}*/

	public void cacheaInstAt(instance object) throws ParseException {
		Integer iIdo = new Integer(object.getIDO());
		HashMap ats = null;
		if (!m_cacheOBJ_ats.containsKey(iIdo)) {
			ats = new HashMap();
			m_cacheOBJ_ats.put(iIdo, ats);
		} else {
			ats = (HashMap) m_cacheOBJ_ats.get(iIdo);
		}
		/*        Iterator itr = object.getAttIterator(0);
        while (itr.hasNext()) {
            attribute at = (attribute) itr.next();
            Integer tapos = new Integer(at.getTapos());
            ats.put(tapos, at.getValue());
        }
		 */   
	}

	public Object serverGetCachedInstanceAt(Integer ido,
			Integer tapos) {
		if (!m_cacheOBJ_ats.containsKey(ido)) {
			return null;
		}
		HashMap ats = (HashMap) m_cacheOBJ_ats.get(ido);
		if (ats == null) {
			return null;
		}
		return ats.get(tapos);
	}

	public org.jdom.Element serverGetProcess() throws SystemException,
	RemoteSystemException, CommunicationException, InstanceLockedException {

		Element root = new Element("GETPROCESS");
		root.setAttribute("MSG_TYPE", "GETPROCESS");
		root.setAttribute("USER", m_user);
		return (Element)appLevel(root, false).getContent();
	}

	/* public org.jdom.Element serverGetConsultas() throws SystemException,
            RemoteSystemException, CommunicationException {

        Element root = new Element("GETVIEW");
        root.setAttribute("MSG_TYPE", "GETVIEW");
        root.setAttribute("USER", m_user);
        return (Element)appLevel(root, false).getContent();
    }*/

	/*public org.jdom.Element serverGetReports() throws SystemException,
            RemoteSystemException, CommunicationException {

        Element root = new Element("GETREPORTS");
        root.setAttribute("MSG_TYPE", "GETREPORTS");
        root.setAttribute("USER", m_user);
        return (Element)appLevel(root, false).getContent();
    }*/


	/*public org.jdom.Element serverGetCurrentTran() throws SystemException,
            RemoteSystemException, CommunicationException {
        if (!m_updateManager.mayUpdateCurrentTasks()) {
            return m_updateManager.getCurrTask();
        } else {
	    message msg= new message(message.MSG_QUERY);
	    msg.setDataType(message.DATA_OWNING);
            Element response = (Element)appLevel(msg, false).getContent();
            m_updateManager.setCurrTask(response);
            return response;
        }
    }*/

	public org.jdom.Element serverGetTask(int process, int task) throws
	SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {

		Element rootOrder = new Element("META");
		rootOrder.setAttribute("MSG_TYPE", "GETTASK");
		rootOrder.setAttribute("CURR_TASK", String.valueOf(task));
		rootOrder.setAttribute("CURR_PROCESS", String.valueOf(process));
		return (Element)appLevel(rootOrder, false).getContent();
	}

	public org.jdom.Element serverGetOutTaskTrans(int currentTask) throws
	SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {

		message msg= new message(message.MSG_QUERY);
		msg.setDataType(message.DATA_TASK_STATE);
		msg.addPropertie(properties.currTask,currentTask);
		return (Element)appLevel(msg,false).getContent();
	}

	/* public org.jdom.Element serverTableSet(scope myScope,
                                           Element rootOrder,
                                           int processType,
                                           int taskType,
                                           int process,
                                           int task,
                                           int tran,
                                           int action) throws SystemException,
            RemoteSystemException, CommunicationException {
        if (!rootOrder.hasChildren()) {
            return null;
        }
        proParser pp = null;
        try {

            pp = new proParser(true,
                               new jdomParser(jdomParser.returnXML(rootOrder)));

        } catch (DataErrorException e) {
            throw new SystemException(SystemException.ERROR_DATOS,
                                      "ERROR DATOS:" + e.getMessage());
        } catch (JDOMException e) {
            throw new SystemException(SystemException.ERROR_JDOM,
                                      "ERROR JDOM:" + e.getMessage());
        }
        pp.setType(proParser.OBJECT_TRAN);
        pp.setOperationType("TABLESET");
        pp.setProcessType(processType);
        pp.setIsRootContext(false);
        pp.setTaskType(taskType);
        if (tran != 0) {
            pp.setCurrTaskTrans(tran);
        }
        setScopeData(pp, myScope);

        pp.setCurrTask(task);
        pp.setCurrProcess(process);
        pp.setUser(m_user);
        if (action != 0) {
            pp.setCurrAction(action);
        }
        return (Element)appLevel(pp, false).getContent();
    }*/


//	public org.jdom.Element serverUnlockObjects(ArrayList unlocks) throws
//	SystemException, RemoteSystemException, CommunicationException {
//	if (unlocks != null && unlocks.size() > 0) {
//	lock msg= new lock(true);
//	msg.addUnlockOrder(unlocks);
//	Element res = (Element)appLevel(msg, false).getContent();
//	m_cacheInstance.instanceUnlock(unlocks);
//	return res;
//	} else {
//	return new Element("NOLOCKS");
//	}
//	}

	public void commit() throws InstanceLockedException {
		try {
			message msg= new message(message.MSG_COMMIT);
			appLevel(msg,false,false);
		} catch (RemoteSystemException re) {
			logError(re);
		} catch (CommunicationException ce) {
			ce.printStackTrace();
		} catch (SystemException se) {
			logError(se);
		}
	}

//	public org.jdom.Element serverUnlockObject(int instance) throws
//	SystemException, RemoteSystemException, CommunicationException {

//	//System.out.println("SOLICITADO UNLOCK:"+instance);
//	if (instance <= 0) {
//	return null;
//	}
//	lock msg= new lock(true);
//	msg.addUnlockOrder(instance);
//	Element res = (Element)appLevel(msg, false).getContent();
//	m_cacheInstance.instanceUnlock(new Integer(instance));
//	return res;
//	}

	public message serverTaskTransition(int process,
			int taskType,
			int currTask,
			int tran) throws
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		flowAction pp = new flowAction();
		pp.setOrderType(message.FLOW_TRANSITION);
		pp.setCurrProcess(process);
		pp.setCurrTask(currTask);
		pp.setTaskType(taskType);
		pp.setCurrTaskTrans(tran);
		return appLevel(pp, false);
	}

/*	public message serverActionObject(/int userRol,access access,
			contextAction pp,
			HashMap<Integer,HashSet<Integer>> unlocks) throws
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {

		if (unlocks != null && unlocks.size() > 0)
			pp.addUnlockOrder(unlocks);
//		setScopeData(pp, userRol,access);
		pp.setUserRol(userRol);
		pp.setAccess(access);
		message res =appLevel(pp, false);
//      m_cacheInstance.instanceUnlock(unlocks);
//		Comentado el 08-10-07
//		if( pp.getOrderType()==message.ACTION_DEL ||
//	    pp.getOrderType()==message.ACTION_SET )
//	    m_cacheInstance.freeInstance(new Integer(pp.getIDO_ctx()));
		
		return res;
	}*/

	/*29/03/06 MESSAGE public org.jdom.Element serverObjExtraction(scope myScope,
						int processType,
						int taskType,
						int currProcess,
						int currTask,
						int tran,
						int action,
						Element cuantData) throws
	    SystemException, RemoteSystemException, CommunicationException {

	proParser pp = null;
	try {
	    pp = new proParser(true,
			       new jdomParser(jdomParser.returnXML(cuantData)));
	} catch (DataErrorException e) {
	    throw new SystemException(SystemException.ERROR_DATOS,
				      "ERROR DATOS:" + e.getMessage());
	} catch (JDOMException e) {
	    throw new SystemException(SystemException.ERROR_DATOS,
				      "ERROR DATOS:" + e.getMessage());
	}
	pp.setType(proParser.OBJECT_TRAN);
	pp.setOperationType("EXTRACTION");
	pp.setProcessType(processType);
	pp.setTaskType(taskType);
	if (tran != 0) {
	    pp.setCurrTaskTrans(tran);
	}
	pp.setCurrTask(currTask);
	pp.setCurrProcess(currProcess);
	pp.setIsRootContext(false);
	pp.setUser(m_user);
	setScopeData(pp, myScope);
	if (action != 0) {
	    pp.setCurrAction(action);
	}
	return (Element)appLevel(pp, false).getContent();
    }*/

	public message serverCancelProcess(int processType, int currentPro) throws
	SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		flowAction pp = new flowAction();
		pp.setOrderType(message.FLOW_END_PRO);
		pp.setProcessType(processType);
		pp.setCurrProcess(currentPro);
		//pp.setUser(m_user);
		return appLevel(pp, false);
	}

	public flowAction serverNewProcess(int processType) throws
	SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {

		flowAction pp = new flowAction();
		pp.setOrderType(message.FLOW_NEW_PRO);
		pp.setProcessType(processType);
		//pp.setUser(m_user);
		flowAction res = (flowAction)appLevel(pp, false);
		//if( true ) throw new CommunicationException("preuba");
		return res;
	}

	public message /*serverApropiateTran*/serverApropiateTask(/*scope myScope,*/int userRol,/*access access,*/
			/*int processType,*/
			int taskType,
			int idUserTask,
			/*int processID,*/
			int currentState) throws
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		owningAction pp = new owningAction();
		pp.setOrderType(message.FLOW_GROW);
		/*pp.setProcessType(processType);*/
		pp.setTaskType(taskType);
		/*pp.setCurrProcess(processID);*/
		pp.setCurrTask(idUserTask);
		pp.setCurrTaskState(currentState);
		//pp.setUser(m_user);
		/*setScopeData(pp, /*myScope*//*userRol,access);*/
		pp.setUserRol(userRol);
		return appLevel(pp, false);
	}

	public message serverFreeTask(/*scope myScope,*/int userRol,/*access access,*/
			int processType,
			int taskType,
			int currentTask,
			int processID,
			int currentState) throws
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		owningAction pp = new owningAction();
		pp.setOrderType(message.FLOW_DECREASE);
		/*setScopeData(pp, /*myScope*//*userRol, access);*/
		pp.setUserRol(userRol);
		pp.setProcessType(processType);
		pp.setTaskType(taskType);
		pp.setCurrProcess(processID);
		pp.setCurrTask(currentTask);
		pp.setCurrTaskState(currentState);
		//pp.setUser(m_user);
		return appLevel(pp, true);
	}

	public org.jdom.Element serverFindTask(int contextType,
			int objectType,
			int objectId) throws SystemException,
			RemoteSystemException, CommunicationException, InstanceLockedException {
		Element rootOrder = new Element("FIND_TASK");
		//org.jdom.Document order = new org.jdom.Document(rootOrder);
		rootOrder.setAttribute("MSG_TYPE", "FIND_TASK");
		rootOrder.setAttribute("CTX_ID", String.valueOf(contextType));
		rootOrder.setAttribute("ID_O", String.valueOf(objectId));
		rootOrder.setAttribute("ID_TO", String.valueOf(objectType));
		rootOrder.setAttribute("USER", m_user);

		return (Element)appLevel(rootOrder, false).getContent();
	}
	
	@Override
	public Changes serverTransitionObject(Integer userRol, String user, IndividualData aipd, Integer windowSession,
			boolean migration, boolean keepTableIds, String replicaSource, String msguid) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		//try {
			contextAction root = new contextAction(m_business,
					m_user,
					/*myScope.getUserRol().intValue(),*/userRol,
					0,
					message.ACTION_MODIFY,
					aipd );
			//if (destination!=null)
				//root.addPropertie(properties.destination, destination);
			root.setWindowSession(windowSession);
			root.addPropertie(properties.migration, migration);
			root.addPropertie(properties.keepTableIds, keepTableIds);
			if(replicaSource!=null){
				root.addPropertie(properties.replicaSource, replicaSource);
			}
			
			if(msguid!=null){
				root.setMsguid(msguid);
			}
//			return (Changes)appLevel(root,true,true).getContent();
			message msg = appLevel(root,true,true);
			//message msgRet = messageFactory.parseMsg(new Object(), msg.toString());
			Changes changes = (Changes)msg.getContent();
			
			notifyLastTransitionObject(aipd);
			
			return changes;
		/*} catch (ParseException se) {
			logError(se);
			se.printStackTrace();
		} catch (DataErrorException se) {
			logError(se);
			se.printStackTrace();
		}*/
		//return null;
	}
	
	@Override
	public Changes serverTransitionObject(String user, Document document,
			ArrayList<Reservation> aReservation, Integer windowSession,
			boolean migration, boolean preprocess, boolean keepTableIds, String replicaSource) throws InstanceLockedException,
			NotFoundException, SystemException, ApplicationException,
			IncoherenceInMotorException, IncompatibleValueException,
			CardinalityExceedException, RemoteSystemException,
			CommunicationException, OperationNotPermitedException,
			DataErrorException, SQLException, NamingException, JDOMException {
		//try {
		contextAction root = new contextAction(m_business,
				m_user,
				/*myScope.getUserRol().intValue(),*/null,
				0,
				message.ACTION_MODIFY,
				document);
		//if (destination!=null)
			//root.addPropertie(properties.destination, destination);
		root.setWindowSession(windowSession);
		root.addPropertie(properties.migration, migration);
		root.addPropertie(properties.preprocess, preprocess);
		root.addPropertie(properties.keepTableIds, keepTableIds);
		if(replicaSource!=null){
			root.addPropertie(properties.replicaSource, replicaSource);
		}

//		return (Changes)appLevel(root,true,true).getContent();
		message msg = appLevel(root,true,true);
		//message msgRet = messageFactory.parseMsg(new Object(), msg.toString());
		Changes changes = (Changes)msg.getContent();
		
		//notifyLastTransitionObject(aipd);
		
		return changes;
	/*} catch (ParseException se) {
		logError(se);
		se.printStackTrace();
	} catch (DataErrorException se) {
		logError(se);
		se.printStackTrace();
	}*/
	//return null;
	}
	
	public void notifyLastTransitionObject(IndividualData aipd){
		Iterator<ISubmitDDBBListener> itrSubmitListener=listSubmitDDBBListener.iterator();
		while(itrSubmitListener.hasNext()){
			itrSubmitListener.next().registerSubmitDDBB(aipd);
		}
	}
	
	public void addSubmitDDBBListener(ISubmitDDBBListener listener){
		listSubmitDDBBListener.add(listener);
	}

	public selectData serverGetTasks(Integer userRol, String user/*, int empresa, int uTask*/) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		/*contextAction act = new contextAction(getBusiness(),
				    user,
				    userRol,
				    0,
				    0,
				    uTask,
				    message.ACTION_GET,
				    null );
			act.setDataType(message.DATA_TASK_STATE);
			message msg = appLevel(act,true,true);*/
		message root = new message(message.MSG_OBJECT_TRAN);
		root.setOrderType(message.ACTION_GET);
		root.setDataType(message.DATA_TASK_STATE);
		//root.setUser(user); se le pone al preparar mensaje
		root.setUserRol(userRol);

		message msg=null;
		//try {
			msg = appLevel(root,true,true);
			//msgRet = messageFactory.parseMsg(new Object(), msg.toString());
			
		/*} catch (ParseException e) {
			Singleton.addDebugData(e);
			e.printStackTrace();
			throw new CommunicationException(CommunicationException.ERROR_TRAMA,"ERROR EN LAS COMUNICACIONES CON EL SISTEMA REMOTO");
		} catch (DataErrorException e) {
			Singleton.addDebugData(e);
			e.printStackTrace();
			
			throw new RemoteSystemException(message.ERROR_DATA,"");
		} */
		return (selectData)msg.getContent();
	}

	public void incrementValue(int ido, int idto, int idProp, int incr) {
		// TODO Auto-generated method stub

	}

	public void lockObject(int ido, Integer idto, String user) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		lock root = new lock(false);
		root.addLockOrder(ido,idto);
		//System.err.println("bloquear en BD ido:"+ido);
		appLevel(root,false,true);
	}
	
	public void lockObject(HashMap<Integer,HashSet<Integer>> listIdo, String user) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		lock root = new lock(false);
		root.addLockOrder(listIdo);
		//System.err.println("bloquear en BD idos:"+listIdo);
		appLevel(root,false,true);
	}

	public void unlockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		lock root = new lock(true);
		root.addUnlockOrder(listIdo);
		//System.err.println("desbloquear en BD listIdo:"+listIdo);
		appLevel(root,false,true);
	}

	public void serverDeleteQuery(Integer userRol, String user, Element root) throws SystemException, RemoteSystemException, CommunicationException {
		// TODO Auto-generated method stub
		
	}

	public void serverDisconnection(String user) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message root = new message(message.MSG_DISCONNECTION);
		appLevel(root,false,true);
	}

	
	public IteratorQuery serverGetIteratorQuery(String sql, boolean update) 
			throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException{
		message msg= new message();
		//Element query = (Element) filter.clone();
		if(!update) msg.setType(message.MSG_QUERY);
		else msg.setType(message.MSG_UPDATE);
		
		msg.setDataType(message.DATA_INSTANCE);
		msg.setOrderType(message.ACTION_GET);
		Element root = new Element("SQL");
		root.addContent(new CDATA(sql));
		msg.setContent(root);
		
		Element response = (Element)appLevel(msg,
				false,
				true).getContent();
		/*try {
			System.err.println(jdomParser.returnXML(response));
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return new IteratorQuery(response);
	}

	public void deleteObligated(Integer userRol, String user, ArrayList<Integer> aIdos) throws SQLException, NamingException, DataErrorException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
	}

	public ArrayList serverGetRules(ArrayList<String> rulesFiles) {
		URL url;
		Object pkg=null;
		ArrayList listPackages=new ArrayList();
		Iterator<String> itrRules=rulesFiles.iterator();
		while(itrRules.hasNext()){
			String rules=itrRules.next();
			System.err.println("Cargando fichero de reglas: "+rules);
			/*if(!rules.endsWith(".dpkg"))
				rules=rules+".dpkg";*/
			try {
				url = new URL(jarURL+rules);
				InputStream is = url.openStream(); 
	            GZIPInputStream gzipInputStream = new GZIPInputStream(is);
			    ObjectInputStream in = new ObjectInputStream(gzipInputStream);
			    // Deserialize the object
			    pkg = in.readObject();
			    in.close();
				listPackages.add(pkg);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println(e.getMessage());
				logError(null,e,"Error al intentar obtener el fichero de reglas "+rules);
				int x=1/0;
			}
		}
	    return listPackages;
	}

	public int getIdoUser() {
		return m_idoUser;
	}

	public void setIdoUser(int user) {
		m_idoUser = user;
	}

	public DebugLog getDebugLog() {
		return debugLog;
	}

	public void setDebugLog(DebugLog debugLog) {
		this.debugLog = debugLog;
	}

	public String serverGetRdn(int ido, int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		System.out.println("mostrar rdn de IDO " + ido);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_GETRDN, (IndividualData)null);
		root.setIDO_ctx(ido);
		root.setTO_ctx(idto);
		
		message mes = (message)appLevel(root, false);
		String rdn = null;
		try {
			rdn = mes.getStrPropertie(Constants.IdPROP_RDN);
		} catch (NoSuchFieldException e) {
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER EL RDN");
		}
		return rdn;
	}
	
	public HashMap<Integer,String> serverGetRdn(HashMap<Integer,HashSet<Integer>> listIdo) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		System.out.println("mostrar rdn de IDOS " + listIdo);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_GETRDNLIST, (IndividualData)null);
		root.setAID_ctx(listIdo);
		
		message mes = (message)appLevel(root, false);
		HashMap<Integer, String> idosrdns = mes.getM_properties();
		return idosrdns;
	}

	public String serverGetClassDescription(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.out.println("mostrar descripcion de clase con IDTO " + idto);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_DESCRIPTION_CLASS, (IndividualData)null);
		root.setTO_ctx(idto);
		
		root.addPropertie(properties.id, idto);
		
		message mes = (message)appLevel(root, false);
		String description = "";
		if(mes.hasPropertie(properties.id)){//Si no la tiene significa que no hay descripción definida para este idto
			try {
				description = mes.getStrPropertie(properties.id);
			} catch (NoSuchFieldException e) {
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPCIÓN");
			}
		}
		return description;

	}

	public String serverGetPropertyDescription(int idProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.out.println("mostrar descripcion de property con PROP " + idProp);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_DESCRIPTION_PROPERTY, (IndividualData)null);
		root.setIDO_ctx(idProp);
		
		message mes = (message)appLevel(root, false);
		String description = "";
		if(mes.hasPropertie(properties.id)){//Si no la tiene significa que no hay descripción definida para este idProp
			try {
				description = mes.getStrPropertie(properties.id);
			} catch (NoSuchFieldException e) {
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPCIÓN");
			}
		}
		return description;
	}

	public HashMap<Integer, String> serverGetPropertiesDescriptionOfClass(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.out.println("mostrar descripciones de properties de CLASS " + idto);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_DESCRIPTION_PROPERTIES, (IndividualData)null);
		root.setTO_ctx(idto);
		
		message mes = (message)appLevel(root, false);
		HashMap<Integer, String> descriptions = mes.getM_properties();
		return descriptions;
	}

	public Integer sendDataTransition(Element xmlData) throws SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, IOException, JDOMException {
		Integer idRoot = null;
		message msg = new message(message.MSG_MIGRATION);
		msg.setContent(xmlData);

		message mes = (message)appLevel(msg, false);
		if(mes.hasPropertie(properties.id)){
			try {
				idRoot = mes.getIntPropertie(properties.id);
			} catch (NoSuchFieldException e) {
				;
			}
		}
		return idRoot;
	}

	public String serverUploadFile(String filePath,int datatype) throws SystemException, RemoteSystemException {
		String name=null;
		if(datatype==Constants.IDTO_IMAGE)
			name=Uploader.uploadImage(filePath, m_host, m_port, m_user, String.valueOf(m_business), maxKbImageSize, smallImageHeight);
		else name=Uploader.uploadFile(filePath, m_host, m_port, m_user, String.valueOf(m_business));
		
		return name;
	}

	public Integer getSmallImageHeight() {
		return smallImageHeight;
	}

	public void setSmallImageHeight(Integer smallImageHeight) {
		this.smallImageHeight = smallImageHeight;
	}
	
	public void showFile(String fileName,boolean forceDownload){
		File file=new File(fileName);
		if(file.canRead())
			try {
				URL url=new URL("file:///"+fileName.replace(" ", "%20"));
				System.err.println("URL:"+url);
				//m_cliente.getAppletContext().showDocument(url, "_blank");
				Desktop.getDesktop().browse(url.toURI());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else{
			URL url;
			try {
				url = new URL(getFileUrl(fileName, forceDownload));//serverGetFilesURL(fileName);
				//m_cliente.getAppletContext().showDocument(url, "_blank");
				Desktop.getDesktop().browse(url.toURI());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Hace una reserva de una cantidad para un ido, idProp en base de datos
	 * @param reservationList
	 * @return mapa de DomainProp y double, que se corresponde con DomainProp-->ido,idto,idProp e Double-->Diferencia entre el stock conocido por nosotros(Reservation.available) y el stock en base de datos menos reservas de otros usuarios
	 */
	@Override
	public HashMap<DomainProp, Double> reserve(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		message msg = new message(message.MSG_RESERVE);
		msg.setOrderType(message.ACTION_NEW_RESERVE);
		msg.setContent(reservationList);
		msg.setWindowSession(windowSession);
		
		HashMap<DomainProp,Double> mapResult = (HashMap<DomainProp,Double>)appLevel(msg,
				false,
				true).getContent();
		
		return mapResult;
	}

	/**
	 * Elimina una reserva de una cantidad para un ido, idProp en base de datos
	 * @param reservationList
	 * @return mapa de DomainProp y double, que se corresponde con DomainProp-->ido,idto,idProp e Double-->Diferencia entre el stock conocido por nosotros(Reservation.available) y el stock en base de datos menos reservas de otros usuarios
	 */
	@Override
	public void deleteReservation(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		message msg = new message(message.MSG_RESERVE);
		msg.setOrderType(message.ACTION_DEL_RESERVE);
		msg.setContent(reservationList);
		msg.setWindowSession(windowSession);

		appLevel(msg, false);
	}
	
	@Override
	public void serverReportsClasificator() throws SystemException,
			RemoteSystemException, CommunicationException, SQLException,
			NamingException, IOException, JDOMException {
		message msg = new message(message.MSG_REPORTS_CLASIFICATOR);
		appLevel(msg, false);
	}
	
	public String getMode() {
		return mode;
	}

	@Override
	public HashMap<Integer, String> serverGetIndividualsDescriptionOfClass(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.out.println("mostrar descripcion de individuos con IDTO " + idto);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_DESCRIPTION_INDIVIDUALS, (IndividualData)null);
		root.setTO_ctx(idto);
		
		root.addPropertie(properties.id, idto);
		
		message mes = (message)appLevel(root, false);
		HashMap<Integer,String> descriptions = mes.getM_properties();
		return descriptions;
	}

	@Override
	public String serverGetIndividualDescription(int ido) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//System.out.println("mostrar descripcion de individuo con IDO " + ido);
		contextAction root = new contextAction(m_business, m_user, 0, 0, message.ACTION_DESCRIPTION_INDIVIDUAL, (IndividualData)null);
		root.setIDO_ctx(ido);
		
		root.addPropertie(properties.id, ido);
		
		message mes = (message)appLevel(root, false);
		String description = "";
		if(mes.hasPropertie(properties.id)){//Si no la tiene significa que no hay descripción definida para este idProp
			try {
				description = mes.getStrPropertie(properties.id);
			} catch (NoSuchFieldException e) {
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPCIÓN");
			}
		}
		return description;
	}

	public void downloadFile(String currValue) {
		getFileUrl(currValue, true);
	}

	@Override
	public void changeMode(String mode) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg = new message(message.MSG_CHANGE_MODE);
		msg.addPropertie(properties.oldMode, this.mode);
		this.mode = mode;
		appLevel(msg, false);
	}


	public void setConfigurationMode(boolean configurationMode) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		if(configurationMode){
			if(!Constants.CONFIGURATION_MODE.equals(mode)){
				changeMode(Constants.CONFIGURATION_MODE);
			}
		}else{
			if(Constants.CONFIGURATION_MODE.equals(mode)){
				changeMode(Constants.BUSINESS_MODE);
			}
		}
		
	}
	
	public void showBuyPage(String subscription, String email){
		URL url=null;
		try {
			//url = new URL("http://" + m_host + ":" + m_port + "/register/controller/CustomerDataAction?email="+email+"&subscription="+subscription);
			url = new URL("http://" + m_host + ":" + m_port + "/register/buy.jsp?email="+email+"&subscription="+subscription+"&type="+Constants.ONLINE_TYPE);
			//m_cliente.getAppletContext().showDocument(url, "_blank");
			Desktop.getDesktop().browse(url.toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
//		String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}
	
	public void showImportHelpPage(){
		URL url=null;
		try {
			url = new URL("http://www.dynagent.es/plantillas.zip");
			//m_cliente.getAppletContext().showDocument(url, "_blank");
			Desktop.getDesktop().browse(url.toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
//		String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}

	public void showHelpPage(){
		URL url=null;
		try {
			//url = new URL("http://" + m_host + ":" + m_port + "/register/controller/CustomerDataAction?email="+email+"&subscription="+subscription);
			url = new URL("http://www.dynagent.es/manual/estandar.html");//new URL("http://" + m_host + ":" + m_port + "/help");
			//m_cliente.getAppletContext().showDocument(url, "_blank");
			Desktop.getDesktop().browse(url.toURI());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
//		String cmd = "rundll32 url.dll,FileProtocolHandler " + url;
//		try {
//			Runtime.getRuntime().exec(cmd);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
	}
	
	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	@Override
	public long serverGetCurrentTimeMillis() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NoSuchFieldException {
		message msg= new message(message.MSG_DATE_NOW);
		long a=(long)appLevel(msg, false, true).getLongPropertie(properties.currentTime);
		return a;
	}
	
	public boolean serverUpdateLicense(String code) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NoSuchFieldException{
		String oldHost=m_host;
		int oldPort=m_port;
		message result=null;
		try{
			m_host="server.dynagent.es";
			m_port=80;
			message msg= new message(message.MSG_CODE_LICENSE);
			msg.addPropertie(properties.licenseCode,code);
			result=appLevel(msg, false);
		}finally{
			m_host=oldHost;
			m_port=oldPort;
		}	
		message msg= new message(message.MSG_UPDATE_LICENSE);
		msg.addPropertie(properties.licenseConcurrentUsers,result.getIntPropertie(properties.licenseConcurrentUsers));
		msg.addPropertie(properties.licenseExpiredDate,result.getLongPropertie(properties.licenseExpiredDate));
		msg.addPropertie(properties.licenseType,result.getStrPropertie(properties.licenseType));
		appLevel(msg, false);
		
		return true;
	}

	@Override
	public boolean sendEmail(int ido, int idto, String reportFileName, String email, String subject, String body, int idoMiEmpresa, int idoDestinatario, boolean showError) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		message msg= new message(message.MSG_SEND_EMAIL);
		msg.addPropertie(properties.email,email);
		msg.addPropertie(properties.emailIdo, ido);
		msg.addPropertie(properties.emailIdto, idto);
		if(reportFileName!=null){
			msg.addPropertie(properties.emailReportFileName, reportFileName);
		}
		msg.addPropertie(properties.emailSubject,subject);
		msg.addPropertie(properties.emailIdoMiEmpresa,idoMiEmpresa);
		msg.addPropertie(properties.emailIdoDestinatario, idoDestinatario);
		
		Element root = new Element("BODY");
		root.addContent(new CDATA(body));
		msg.setContent(root);

		boolean success=false;
		try{
			success=appLevel(msg, false).getBoolPropertie(properties.success);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(!success && showError){
				this.messagesControl.showErrorMessage("El email no se ha enviado correctamente al cliente. Revise servidor mail saliente en Aplicación.\nConsultar log_email en el area configuración para mas detalles",SwingUtilities.getWindowAncestor(m_cliente));
			}
		}
		return success; 
	}
	
	@Override
	public boolean sendEmailWithServerLog(String email, String subject, String body) throws SystemException, RemoteSystemException{
		message msg= new message(message.MSG_SEND_SERVER_LOG_EMAIL);
		msg.addPropertie(properties.email,email);
		msg.addPropertie(properties.emailSubject,subject);
		
		Element root = new Element("BODY");
		root.addContent(new CDATA(body));
		msg.setContent(root);
		
		boolean success=false;
		try{
			success=appLevel(msg, false).getBoolPropertie(properties.success);
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(!success && m_cliente!=null){
				this.messagesControl.showErrorMessage("El email del log no se ha enviado correctamente. Revise servidor mail saliente en Aplicación.\nConsultar log_email en el area configuración para mas detalles",SwingUtilities.getWindowAncestor(m_cliente));
			}
		}
		return success; 
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
