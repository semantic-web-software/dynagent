package dynagent.server.ejb;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.server.services.querys.AuxiliarQuery;

public class Session {
	
	private String id;      //primary key
	private String idclient;
	private int bns;
    
    public Session(String id, String idclient, int bns) {
    	this.id = id;
    	this.idclient = idclient;
    	this.bns = bns;
    }
    
    public int getBns() {
		return bns;
	}
	public String getId() {
		return id;
	}
	public String getIdclient() {
		return idclient;
	}
	
    public static boolean create(String id, String idclient, int bns) throws SQLException {
   		//inserto en tabla sessions
    	boolean add = false;
		Statement st = null;
		java.sql.Connection con = null;
		try {
			String sql = "INSERT INTO sessions(id, idclient) VALUES('" + id.replaceAll("'", "''") + "','" + idclient.replaceAll("'", "''") + "')";
			System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			st.executeUpdate(sql);
			add = true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
		return add;
    }
    
    public static boolean updateIdClient(String id, String idclient, int bns) throws NamingException, SQLException {
   		//actualizo idclient en tabla sessions
		boolean effect = false;
		Statement st = null;
		java.sql.Connection con = null;
		try {
			String sql = "UPDATE sessions SET idclient='" + idclient.replaceAll("'", "''") + "' " +
					"WHERE id='" + id.replaceAll("'", "''") + "'";
			System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			int rows = st.executeUpdate(sql);
			if (rows>0)
				effect = true;
		} finally {
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
		return effect;
    }
    
	public static Session getSession(String id, int bns) throws NamingException, SQLException {
		Session s = null;
		Statement st = null;
		ResultSet rs = null;
		java.sql.Connection con = null;
		try {
			//consulta obteniendo si hay algun msg en owning_msg posterior a esa fecha
			String sql = "SELECT id, idclient FROM sessions WHERE id='" + id.replaceAll("'", "''") + "'";
			System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				s = new Session(id, rs.getString(2), bns);
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
		return s;
	}
	
	public static boolean existsSession(String idClient, int bns) throws NamingException, SQLException {
		boolean exists = false;
		Statement st = null;
		ResultSet rs = null;
		java.sql.Connection con = null;
		System.out.println("Exist session checking");
		try {
			String sql = "SELECT * FROM sessions where idclient='"+idClient+"'";
			
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				System.out.println("Si Existe");
				exists=true;
			}else{
				System.out.println("No Existe");
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
		return exists;
	}

	public static boolean existsSession(String user, int bns, String mode) throws NamingException, SQLException {
		boolean exists = false;
		Statement st = null;
		ResultSet rs = null;
		java.sql.Connection con = null;
		try {
			//consulta obteniendo si hay algun msg en owning_msg posterior a esa fecha
			String sql = "SELECT id FROM sessions";
			//System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String id = rs.getString(1);
				String idParam = user + "/" + bns + "/";
				if (mode!=null)
					idParam += mode;
				if (id.startsWith(idParam))
					exists = true;
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
		return exists;
	}

	public static void remove(String id, int bns) throws NamingException, SQLException {
		Statement st = null;
		java.sql.Connection con = null;
		try {
			//consulta obteniendo si hay algun msg en owning_msg posterior a esa fecha
			String sql = "DELETE FROM sessions WHERE id='" + id.replaceAll("'", "''") + "'";
			System.out.println("sql " + sql);
			con = AuxiliarQuery.getDataConnection(bns);
			st = con.createStatement();
			st.executeUpdate(sql);
		} finally {
			if (st!=null)
				st.close();
			if (con!=null)
				con.close();
		}
	}
	
}
