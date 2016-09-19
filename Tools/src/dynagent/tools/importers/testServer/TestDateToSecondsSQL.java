package dynagent.tools.importers.testServer;

import java.sql.ResultSet;
import java.sql.Statement;

import dynagent.common.utils.QueryConstants;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;

public class TestDateToSecondsSQL {
	
	public void test(FactoryConnectionDB fcdb, String gestor){
		try{
			Long seconds = QueryConstants.dateToSeconds("dd/MM/yyyy", "01/01/2011");
//			Long seconds = QueryConstants.dateToSeconds("yyyy-MM-dd HH:mm:ss", "2010-02-01 12:35:12");
			
			System.out.println("seconds " + seconds);
			
			if (gestor.equals(GestorsDBConstants.mySQL)) {
				String sql = "SELECT FROM_UNIXTIME(" + seconds + ")";
				ConnectionDB con = fcdb.createConnection(true);
				Statement st = null;
				ResultSet rs = null;
				System.out.println("sql->"+sql);
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						String date = rs.getString(1);
						System.out.println("date " + date);
					}
				} finally {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con!=null)
						fcdb.close(con);
				}
			} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
				String sql = "select extract( epoch from cast('1/1/2011' as date))";
				ConnectionDB con = fcdb.createConnection(true);
				Statement st = null;
				ResultSet rs = null;
				System.out.println("sql->"+sql);
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						long secondsSQL = rs.getLong(1);
						System.out.println("secondsSQL " + secondsSQL);
					}
				} finally {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con!=null)
						fcdb.close(con);
				}
			}
		}catch(Exception e){
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		try{			
			TestDateToSecondsSQL testDate = new TestDateToSecondsSQL();
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String gestor = args[2];
			int port = Integer.parseInt(args[3]);
			
			FactoryConnectionDB fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
			fcdb.setPort(port);
			
			testDate.test(fcdb, gestor);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
