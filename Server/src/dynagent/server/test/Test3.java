package dynagent.server.test;

import dynagent.common.communication.IndividualData;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test3 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB factConnDB;
	
	public void test(){
		try{
			System.out.println("dbg1");
			
			IndividualData aipd = m_IS.serverGetFactsInstance(11001, null, false, 1);
			System.out.println(aipd.toString());
			
		}catch(Exception e){
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		factConnDB = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(factConnDB, null, false);
		Connect.connectRuler(databaseIP, factConnDB, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test3 test3 = new Test3();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test3.start(business, gestor);
			test3.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
