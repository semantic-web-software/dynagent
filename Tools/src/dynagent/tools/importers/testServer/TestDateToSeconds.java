package dynagent.tools.importers.testServer;

import dynagent.common.utils.QueryConstants;

public class TestDateToSeconds {

	public void test(){
		try{
			Long seconds = QueryConstants.dateToSeconds("dd/MM/yyyy", "21/01/2009");
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

	public static void main(String[] args) {
		try{			
			TestDateToSeconds testDateToSeconds = new TestDateToSeconds();
			System.out.println("dbg0");
			testDateToSeconds.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
