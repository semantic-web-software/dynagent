package dynagent.server.gestorsDB.converters;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;

public class RulerApplet {
		
	private FactoryConnectionDB fcdb = null;
	
	public void test(String path) throws SQLException, NamingException, IOException{
		StringBuffer sqlmySQL = new StringBuffer("insert into RulerApplet(`version`,`group`,`content`,active) values");
		StringBuffer sqlmySQLTmp = new StringBuffer("");
		
		String sql = "SELECT [version], [group], [content], active FROM RulerApplet";
		ConnectionDB con = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String version = rs.getString(1).replaceAll("'", "''");
				String group = rs.getString(2).replaceAll("'", "''");
				String content = rs.getString(3).replaceAll("'", "''");
				String active = rs.getString(4).replaceAll("'", "''");
				if (sqlmySQLTmp.length()>0)
					sqlmySQLTmp.append(",");
				sqlmySQLTmp.append("('" + version + "','" + group + "','" + content + "','" + active + "')");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				fcdb.close(con);
		}
		sqlmySQL.append(sqlmySQLTmp + ";");
		FileWriter fw = new FileWriter(path+"\\RulerApplet.sql");
		
		fw.write(sqlmySQL.toString());
		fw.close();
	}

	public void start(int business, String ip, String gestor) {
		fcdb = new FactoryConnectionDB(business, true, ip, gestor);
	}

	public static void main(String[] args) {
		try{			
			RulerApplet ruler = new RulerApplet();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String ip = args[1];
			String gestor = args[2];
			String path = args[3];
			ruler.start(business, ip, gestor);
			ruler.test(path);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
