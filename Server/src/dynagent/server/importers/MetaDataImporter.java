package dynagent.server.importers;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Iterator;

import javax.naming.NamingException;

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
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;


public class MetaDataImporter {

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
			String file = args[1];
			String user = args[2];
			int port = 0;
			if (args[3]!=null)
				port = Integer.parseInt(args[3]);
			int bns = 0;
			if (args[4]!=null)
				bns = Integer.parseInt(args[4]);
			String gestor = args[5];

			System.out.println("guardados los parametros");
			if(file.equals("pool")) {
				System.out.println("pool");
				communicator com= communicator.getInstance(null, null, null, new URL(host+":"+port), new URL(host+":"+port), 6000, 0, user, UserConstants.byPassKey);
				message res= com.serverPolling();
				System.out.println("RES "+res.toString());	
				return;		
			}
			System.out.println("PRE LEER");
			String doc= Auxiliar.readFile(file);
			System.out.println("fichero: " + doc);
			//documento a Element
			Element elem = jdomParser.readXML(doc).getRootElement();
			System.out.println("elem: " + jdomParser.returnXML(elem));
			//el xml lo paso a instance
			adaptaIDs(elem, bns, host, gestor);
			System.out.println("elem adaptado: " + jdomParser.returnXML(elem));
			
			//aqui construyo el mensaje
			message msg = new message(message.MSG_OBJECT_TRAN);
			msg.setOrderType(message.ACTION_MODIFY);
			msg.setContent(elem);
			
			//el mensaje lo paso a String y se parsea
			flowAction order = (flowAction)messageFactory.parseMsg(null, msg.toString());

			System.out.println("LEIDO");
			communicator com;
			
				com = communicator.getInstance(null, null, null, new URL(host+":"+port), new URL(host+":"+port), 6000, 0, user, UserConstants.byPassKey);
			com.setTestState(com.ACTION_TEST);
	
			Thread.currentThread().sleep(10000);
	
			System.out.println("SEND "+order.toString());
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

	private static void setEtiq(Element elem, String etiqNodo, String table, String column, 
			FactoryConnectionDB factConnDB) {
		String valorName = elem.getAttributeValue(etiqNodo);
		if (valorName!=null && !Auxiliar.hasIntValue(valorName)) {
			ConnectionDB conDb = null;
			Statement st = null;
			ResultSet rs = null;
			String sql = "SELECT " + column + " FROM " + table + " WHERE NAME='" + valorName + "'";
			try {
				conDb = factConnDB.createConnection(true);
				st = conDb.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					int prop = rs.getInt(1);
					elem.setAttribute(etiqNodo,(new Integer(prop)).toString());
				}
			} catch(SQLException e) {
	      		System.out.println("ERROR SQLException:");
				e.printStackTrace();
			} catch(NamingException e) {
	      		System.out.println("ERROR NamingException:");
				e.printStackTrace();
			} finally {
				try {
					if (rs!=null)
						rs.close();
					if (st!=null)
						st.close();
					if (conDb!=null)
						conDb.close();
				} catch(SQLException e) {
		      		System.out.println("ERROR SQLException:");
					e.printStackTrace();
				}
			}
		}
	}

	private static void adaptaIDs(Element elem, int business, String databaseIP, String gestor) {
		FactoryConnectionDB factConnDB = new FactoryConnectionDB(business, true, databaseIP, gestor);

		Element ins = elem.getChild("INSTANCES");
		if (ins!=null) {
			Iterator it = ins.getChildren("INSTANCE").iterator();
			while (it.hasNext()) {
				Element itH = (Element)it.next();
				setEtiq(itH, "IDTO", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "PROP", "properties", "PROP", factConnDB);
				setEtiq(itH, "VALCLS", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "CLSREL", "Clases", "IDTO", factConnDB);
			}
		}
		Element prop = elem.getChild("PROPERTIES");
		if (prop!=null) {
			Iterator it2 = prop.getChildren("PROPERTY").iterator();
			while (it2.hasNext()) {
				Element itH = (Element)it2.next();
				setEtiq(itH, "PROP", "properties", "PROP", factConnDB);
				setEtiq(itH, "VALUECLS", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "PROPINV", "properties", "PROP", factConnDB);
			}
		}
		Element herencias = elem.getChild("HIERARCHIES");
		if (herencias!=null) {
			Iterator it3 = herencias.getChildren("HIERARCHY").iterator();
			while (it3.hasNext()) {
				Element itH = (Element)it3.next();
				setEtiq(itH, "ID_TO", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "ID_TO_PARENT", "Clases", "IDTO", factConnDB);
			}
		}
		Element accesses = elem.getChild("ACCESSES");
		if (accesses!=null) {
			Iterator it4 = accesses.getChildren("ACCESS").iterator();
			while (it4.hasNext()) {
				Element itH = (Element)it4.next();
				setEtiq(itH, "IDTO", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "PROP", "properties", "PROP", factConnDB);
				setEtiq(itH, "VALUECLS", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "CLSREL", "Clases", "IDTO", factConnDB);
			}
		}
		Element busClasses = elem.getChild("BUSINESS_CLASSES");
		if (busClasses!=null) {
			Iterator it5 = busClasses.getChildren("BUSINESS_CLASS").iterator();
			while (it5.hasNext()) {
				Element itH = (Element)it5.next();
				setEtiq(itH, "IDTO", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "PROP", "properties", "PROP", factConnDB);
				setEtiq(itH, "VALCLS", "Clases", "IDTO", factConnDB);
				setEtiq(itH, "CLSREL", "Clases", "IDTO", factConnDB);
			}
		}
	}
}
