package dynagent.server.importers;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.UserConstants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.flowAction;
import dynagent.common.communication.message;
import dynagent.common.communication.messageFactory;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.jdomParser;

public class IndividualImporter{

	/*public void printHelp(boolean printError){
		if(printError)
				System.out.println("SINTAXIS DE COMANDO ERRONEA:\n");
		System.out.println("FORMATO COMANDO:\nadaptator -IMPORT file");
	}*/

	public static void main(String[] args) {
		try {
			System.out.println("INICIO");
			String host = args[0];
			System.out.println(host);
			String path = args[1];
			String user = args[2];
			int port = 0;
			if (args[3]!=null)
				port = Integer.parseInt(args[3]);
			int bns = 0;
			if (args[4]!=null)
				bns = Integer.parseInt(args[4]);
			String gestor = args[5];
			
			System.out.println("guardados los parametros");
			if(path.equals("pool")) {
				System.out.println("pool");
				communicator com= communicator.getInstance(null, null, null, new URL(host+":"+port), new URL(host+":"+port),6000, 0, user, UserConstants.byPassKey);
				message res= com.serverPolling();
				System.out.println("RES "+res.toString());	
				return;		
			}
			System.out.println("PRE LEER");
			String doc = Auxiliar.readFile(path);
			//System.out.println("fichero: " + doc);
			//documento a Element
			Element elem = jdomParser.readXML(doc).getRootElement();
			//System.out.println("elem: " + jdomParser.returnXML(elem));
			//el xml lo paso a instance
			IndividualParser indParser = new IndividualParser(bns, host, gestor);
			indParser.parserIDs(elem);
			//System.out.println("elem adaptado: " + jdomParser.returnXML(elem));
			//aqui construyo el mensaje
			message msg = new message(message.MSG_OBJECT_TRAN);
			msg.setOrderType(message.ACTION_MODIFY);
			msg.setContent(elem);
			
			//el mensaje lo paso a String y se parsea
			flowAction order = (flowAction)messageFactory.parseMsg(null, msg.toString());
			
			System.out.println("LEIDO");
			communicator com = communicator.getInstance(null, null, null, new URL(host+":"+port), new URL(host+":"+port), 6000, 0, user, UserConstants.byPassKey);
			com.setTestState(com.ACTION_TEST);
	
			Thread.currentThread().sleep(10000);
	
			//System.out.println("SEND "+order.toString());
			message res= com.appLevel(order,false,true);
			System.out.println("RES "+res.toString());
	
			if(res.getSuccess())
				com.commit();
		} catch(SystemException e) {
      		System.out.println("ERROR SystemException:");
			e.printStackTrace();
		} catch(RemoteSystemException e) {
      		System.out.println("ERROR RemoteSystemException:");
			e.printStackTrace();
		} catch(CommunicationException e) {
      		System.out.println("ERROR CommunicationException:");
			e.printStackTrace();
		} catch(JDOMException e) {
      		System.out.println("ERROR JDOMException:");
			e.printStackTrace();
		} catch(ParseException e) {
      		System.out.println("ERROR ParseException:");
			e.printStackTrace();
		} catch(DataErrorException e) {
      		System.out.println("ERROR DataErrorException:");
			e.printStackTrace();
		} catch(InterruptedException e) {
      		System.out.println("ERROR InterruptedException:");
			e.printStackTrace();
	    } catch (InstanceLockedException e) {
      		System.out.println("ERROR InstanceLockedException:");
			e.printStackTrace();
	    } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	}
}
