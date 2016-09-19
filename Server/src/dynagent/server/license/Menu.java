package dynagent.server.license;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;

public class Menu {
	
	public static void main(String[] args) {
		
		Connection connection = null;
		String key = InstanceService.keyEncrypt;
		int business = Integer.parseInt(args[0]);
		String ip = args[1];
		String gestor = args[2];//"SQLServer";
		Long date = Long.parseLong(args[3]);
		Integer users = Integer.parseInt(args[4]);
		String functionUsers = new GenerateSQL(gestor).getEncryptFunction(key, users.toString());
		String functionDate= new GenerateSQL(gestor).getEncryptFunction(key, date.toString());
		
		String sql = "INSERT INTO License (Users,FechaMax) VALUES (" + functionUsers + ", " + functionDate + ")";			
		//System.out.println(sql);
		try {
			FactoryConnectionDB fcdb = new FactoryConnectionDB(business, true, ip, gestor);		
			ConnectionDB con = fcdb.createConnection(true);
			connection = con.getBusinessConn();
			Statement st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			//connection.commit();
		} catch (NamingException e) {
			try {
				e.printStackTrace();
				if (connection!=null)
					connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} catch (SQLException e) {
			try {
				e.printStackTrace();
				if (connection!=null)
					connection.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
}
