package dynagent.server.test;

import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.selectData;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test10 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(String nameQuery) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		//HashMap<String,String> hIdValue = new HashMap<String,String>();
		//hIdValue.put("días_entrega_where", "6");
		selectData sd = m_IS.serverGetQueryDB(nameQuery, null);
		System.out.println(sd.toString() + "\n");
	}

	public void start(int business, String gestor, String databaseIP) {
		fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test10 test10 = new Test10();
			System.out.println("dbg0");
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String gestor = args[2];
			test10.start(business, gestor, databaseIP);
			String nameQuery = args[3];
			test10.test(nameQuery);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
