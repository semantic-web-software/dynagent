package dynagent.server.test;

import dynagent.common.knowledge.selectData;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test15 {

	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(String name){
		try{
			//HashMap<String,String> hIdValue = new HashMap<String,String>();
			//hIdValue.put("días_entrega_where", "6");
			selectData sd = m_IS.serverGetQueryDB(name, null);
			//System.out.println(sd);
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
			Test15 test15 = new Test15();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String name = args[1];
			String gestor = args[2];
			test15.start(business, gestor);
			test15.test(name);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
