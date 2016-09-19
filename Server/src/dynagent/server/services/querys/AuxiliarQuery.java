package dynagent.server.services.querys;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import dynagent.common.knowledge.PropertyValue;
import dynagent.common.utils.Auxiliar;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.dbmap.TableColumn;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class AuxiliarQuery {
	public static int dbExecUpdate(FactoryConnectionDB fcdb, String sql, boolean commit) throws SQLException, NamingException {
		//System.out.println("DBEXEC:"+sql);
		Statement st = null;
		int rows = 0;
		ConnectionDB con = fcdb.createConnection(commit);
		try {
			/*if (updatable)
				st = con.getBusinessConn().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
			else*/
				st = con.getBusinessConn().createStatement();
			rows = st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return rows;
	}
	
	public static int dbExecUpdate(Connection con, String sql, boolean commit) throws SQLException, NamingException {
		//System.out.println("DBEXEC:"+sql);
		Statement st = null;
		int rows = 0;
		try {
			/*if (updatable)
				st = con.getBusinessConn().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
			else*/
				st = con.createStatement();
			rows = st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
		}
		return rows;
	}
	
	/*public static int dbExecUpdateRemote(FactoryConnectionDB fcdb, String sql, boolean commit) throws SQLException, NamingException {
		int rows = 0;
		if (!fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL)) {
			rows = dbExecUpdate(fcdb, sql, commit);
		} else {
			//esto es pa postgres
			System.out.println("DBEXEC:"+sql);
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = null;
			try {
				con = fcdb.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					String result = rs.getString(1);
					if (result.startsWith("INSERT "))
						rows = 1;
					else
						rows = Integer.parseInt(result.substring(7, result.length())); //quitamos 'UPDATE ' o 'DELETE '
					System.out.println("rows " + rows);
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
		return rows;
	}
	public static void dbExecUpdateNoResult(FactoryConnectionDB fcdb, String sql, boolean commit) throws SQLException, NamingException {
		if (!fcdb.getGestorDB().equals(GestorsDBConstants.postgreSQL)) {
			dbExecUpdate(fcdb, sql, commit);
		} else {
			//esto es pa postgres
			System.out.println("DBEXEC:"+sql);
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = null;
			try {
				con = fcdb.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					String result = rs.getString(1);
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
	}*/
//	public static int dbExecUpdate(FactoryConnectionDB fcdb, String db, String sql, boolean commit) throws SQLException, NamingException {
//		System.out.println("DBEXEC:"+sql);
//		Statement st = null;
//		int rows = 0;
//		ConnectionDB con = fcdb.createConnection(commit);
//		try {
//			/*if (updatable)
//				st = con.getDataBaseConn(db).createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
//						ResultSet.CONCUR_UPDATABLE);
//			else*/
//				st = con.getBusinessConn().createStatement();
//			rows = st.executeUpdate(sql);
//		} finally {
//			if (st != null)
//				st.close();
//			if (con!=null)
//				fcdb.close(con);
//		}
//		return rows;
//	}
	public static void dbExec(FactoryConnectionDB fcdb, String sql, boolean commit) throws SQLException, NamingException {
		//System.out.println("DBEXEC:"+sql);
		Statement st = null;
		ConnectionDB con = fcdb.createConnection(commit);
		try {
			st = con.getBusinessConn().createStatement();
			st.execute(sql);
		} finally {
			if (st != null)
				st.close();
			fcdb.close(con);
		}
	}
	
	public static int dbExecUpdate(FactoryConnectionDB fcdb, ArrayList<String> sqlList, boolean commit) throws SQLException, NamingException {
		//System.out.println("DBEXEC:"+sqlList);
		Statement st = null;
		int rows = 0;
		ConnectionDB con = fcdb.createConnection(commit);
		try {
			/*if (updatable)
				st = con.getBusinessConn().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_UPDATABLE);
			else*/
				st = con.getBusinessConn().createStatement();
			for(String sql:sqlList)
				rows += st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return rows;
	}
	
	public static java.sql.Connection getDataConnection(int empresa ) throws NamingException,SQLException{
		InitialContext ic = new InitialContext();
		DataSource ds = (DataSource)ic.lookup("java:jdbc/dyna" + empresa );
		java.sql.Connection conn= ds.getConnection();
		//conn.setAutoCommit(true);
		return conn;
	}
}
