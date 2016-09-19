package dynagent.server.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.naming.NamingException;

import org.apache.commons.lang.time.DateFormatUtils;

import dynagent.server.ejb.FactoryConnectionDB;

public class Test6 {

	private FactoryConnectionDB factConnDB;

	public void test() {
		long start=System.currentTimeMillis();;
		int liveTime=60000;
		long tiempoInic = start - liveTime;
		System.out.println("tiempoInic " + tiempoInic);
		Date dateInic = new Date(tiempoInic);
		//String dateInic2 = DateFormatUtils.format(dateInic, "MM/dd/yyyy HH:mm:ss");
		String dateInic2 = DateFormatUtils.format(dateInic, "yyyy-MM-dd HH:mm:ss");
		
		//consulta obteniendo si hay algun en owning_msg los posteriores a esa fecha
		System.out.println("date " + dateInic);
		System.out.println("date " + dateInic2);

		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT USUARIO,ROL FROM OWNING_MSG WHERE TIMESTAMP>'" + dateInic2 + "'";
			System.out.println("sql " + sql);
			st = factConnDB.createConnection(true).getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				System.out.println("existen");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NamingException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void start(int business, String gestor) {
		factConnDB = new FactoryConnectionDB(business, true, null, gestor);
	}

	public static void main(String[] args) {
		try{			
			Test6 test6 = new Test6();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String gestor = args[1];
			test6.start(business, gestor);
			test6.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}

}
