package dynagent.server.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;

public class Test17 {
	
	private FactoryConnectionDB fcdb = null;
	
	public void test() throws SQLException, NamingException{
		
		String sql = "show slave status";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String status = rs.getString(1);
				System.out.println("status -> " + status);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
	}

	public void start(int business, String gestor) {
		String databaseIP = "192.168.1.3";
		fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
	}

	public static void main(String[] args) {
		try{			
			Test17 pr = new Test17();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			pr.start(business, gestor);
			pr.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
