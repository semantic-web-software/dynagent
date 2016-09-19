package dynagent.tools.importers.testServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.UserConstants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.message;
import dynagent.common.communication.messageFactory;
import dynagent.common.communication.properties;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.jdomParser;

public class TiendaOnline{

	public static void main(String[] args) throws IOException {
		try {
			System.out.println("INICIO");
			String path = args[0];
			int bns = 0;
			if (args[1]!=null)
				bns = Integer.parseInt(args[1]);
			/*String host = args[2];
			System.out.println(host);
			String user = args[3];
			int portJBoss = 0;
			if (args[4]!=null)
				portJBoss = Integer.parseInt(args[4]);*/
			
			System.out.println("guardados los parametros");
			System.out.println("PRE LEER");
			String doc = Auxiliar.readFile(path);
			//System.out.println("fichero: " + doc);
			//documento a Element
			Element elem = jdomParser.readXML(doc).getRootElement();
			
			Integer idRoot = sendDataXML(elem, bns);
		} catch(JDOMException e) {
      		System.out.println("ERROR JDOMException:");
			e.printStackTrace();
		} catch (IOException e) {
      		System.out.println("ERROR IOException:");
			e.printStackTrace();
		}
  	}
	
	public static Integer sendDataXML(Element elem, int bns) {
		String host = "192.168.1.3";
		int portJBoss = 18080;
		String user = "admin";
		
		Integer idRoot = null;
		try {
			message msg = new message(message.MSG_MIGRATION);
			msg.setContent(elem);
			
			//el mensaje lo paso a String y se parsea
			message order = (message)messageFactory.parseMsg(null, msg.toString());
			
			System.out.println("LEIDO");
			DebugLog debugLog = new DebugLog();
			URL url = new URL("http://" + host + ":" + portJBoss + "/dyna/bin/");
			communicator com = new communicator(null, null, debugLog, url, url, 6000, 0, bns, user, UserConstants.byPassKey, false);
			com.setTestState(com.ACTION_TEST);
	
			Thread.currentThread().sleep(10000);
	
			//System.out.println("SEND "+order.toString());
			message res = com.appLevel(order,false,true);
			System.out.println("RES "+res.toString());
	
			if(res.getSuccess()) {
				com.commit();
				if(res.hasPropertie(properties.id)){
					try {
						idRoot = res.getIntPropertie(properties.id);
						System.out.println("idRoot " + idRoot);
					} catch (NoSuchFieldException e) {
						;
					}
				}
			}
		} catch(SystemException e) {
      		System.out.println("ERROR SystemException:");
			e.printStackTrace();
		} catch (MalformedURLException e) {
      		System.out.println("ERROR MalformedURLException:");
			e.printStackTrace();
		} catch (InstanceLockedException e) {
      		System.out.println("ERROR InstanceLockedException:");
			e.printStackTrace();
		} catch (InterruptedException e) {
      		System.out.println("ERROR InterruptedException:");
			e.printStackTrace();
		} catch (RemoteSystemException e) {
      		System.out.println("ERROR RemoteSystemException:");
			e.printStackTrace();
		} catch (CommunicationException e) {
      		System.out.println("ERROR CommunicationException:");
			e.printStackTrace();
		} catch (DataErrorException e) {
      		System.out.println("ERROR DataErrorException:");
			e.printStackTrace();
		} catch (ParseException e) {
      		System.out.println("ERROR ParseException:");
			e.printStackTrace();
		}
		return idRoot;
	}
}
