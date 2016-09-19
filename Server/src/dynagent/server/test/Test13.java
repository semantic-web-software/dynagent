package dynagent.server.test;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.knowledge.instance;
import dynagent.common.utils.jdomParser;
import dynagent.ruleengine.alias.IAlias;
import dynagent.ruleengine.src.xml.QueryXML;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test13 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(){

		try{
			instance ins = new instance(116,-1000);
			
			IAlias ia = null;
			QueryXML qXML = m_IS.getIk().getQueryXML(ia);
			Element query = qXML.toQueryXML(ins, null);
			//monoStandConnection msc= new monoStandConnection(business,login,pwd);
			System.out.println("dbg1");
			query.setAttribute("USER","SYSTEM");

			System.out.println(jdomParser.returnXML(query));
			m_IS.serverDeleteQuery(null, "lola", query);
		}catch(JDOMException e){
			System.out.println("JDOMException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(Exception e){
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		fcdb = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(databaseIP, fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test13 test13 = new Test13();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test13.start(business, gestor);
			test13.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
