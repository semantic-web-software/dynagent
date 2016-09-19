package dynagent.help;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class Auxiliar {

	public static String getClassDescription(String name) throws SQLException, NamingException{
		String description = null;
		String sql = "select description from helpclasses where name='"+name+"' AND language='es'";
		DataSource ds=null;
		java.sql.Connection conn=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource)ic.lookup("java:jdbc/dynaglobal" );
			conn= ds.getConnection();
			conn.setAutoCommit(true);
			st= conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn!=null)
				conn.close();
		}
		return description;
	}
	
	public static String getPropertyDescription(String name) throws SQLException, NamingException{
		String description = null;
		String sql = "select description from helpproperties where name='"+name+"' AND language='es'";
		DataSource ds=null;
		java.sql.Connection conn=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource)ic.lookup("java:jdbc/dynaglobal" );
			conn= ds.getConnection();
			conn.setAutoCommit(true);
			st= conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn!=null)
				conn.close();
		}
		return description;
	}
	

	public static String getIndividualDescription(String name) throws NamingException, SQLException{
		String description = null;
		String sql = "select description from helpindividuals where name='"+name+"' AND language='es'";
		DataSource ds=null;
		java.sql.Connection conn=null;
		Statement st=null;
		ResultSet rs=null;
		try {
			InitialContext ic = new InitialContext();
			ds = (DataSource)ic.lookup("java:jdbc/dynaglobal" );
			conn= ds.getConnection();
			conn.setAutoCommit(true);
			st= conn.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (conn!=null)
				conn.close();
		}
		return description;
	}


}
