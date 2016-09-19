package dynagent.server.test;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;

public class Test5 {

	private FactoryConnectionDB factConnDB;

	public void test() {
		FileWriter fs = null;
		String fileS = "";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB conDb = null;
		try {
			fs = new FileWriter("./reglas.clp");
			String sql = "SELECT APPLET_FILE FROM RulerFile";
			conDb = factConnDB.createConnection(true);
			st = conDb.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				fileS = rs.getString(1);
			}
			fs.write(fileS);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
				if (conDb!=null)
					conDb.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			try {
				if (fs!=null)
					fs.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public void start(int business, String gestor) {
		factConnDB = new FactoryConnectionDB(business, true, null, gestor);
	}

	public static void main(String[] args) {
		try{			
			Test5 test5 = new Test5();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test5.start(business, gestor);
			test5.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}

}
