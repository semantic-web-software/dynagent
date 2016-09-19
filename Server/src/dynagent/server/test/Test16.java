package dynagent.server.test;

import dynagent.common.utils.QueryConstants;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Test16 {

	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(){
		try{
			Long seconds = QueryConstants.dateToSeconds("dd/MM/yyyy", "01/01/1900");
			System.out.println("seconds " + seconds);
			String date = QueryConstants.secondsToDate("1218668400", "dd/MM/yyyy");
			System.out.println("date de 39672 " + date);
			String date2 = QueryConstants.secondsToDate("1218754800", "dd/MM/yyyy");
			System.out.println("date de 39673 " + date2);
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
			Test16 test16 = new Test16();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test16.start(business, gestor);
			test16.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
