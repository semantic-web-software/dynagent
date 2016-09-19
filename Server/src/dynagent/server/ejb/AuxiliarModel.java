package dynagent.server.ejb;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.knowledge.Category;
import dynagent.common.utils.Auxiliar;

public class AuxiliarModel {
	
	public static Integer getPropertyByName(String name, FactoryConnectionDB factConnDB) throws NamingException, SQLException {
		String sqlProp = "SELECT id FROM v_propiedad WHERE rdn ILIKE '" + name + "'";
		Integer prop = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlProp);
			if (rs.next()) {
				prop = rs.getInt(1);
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return prop;
	}
	public static Integer getPropertyByNameInInstances(String name, FactoryConnectionDB factConnDB) throws NamingException, SQLException {
		String sqlProp = "SELECT id FROM v_propiedad WHERE rdn ILIKE '" + name + "'" + 
			" and id in(select property from instances)";
		Integer prop = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlProp);
			if (rs.next()) {
				prop = rs.getInt(1);
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return prop;
	}
	
	public static String getPropertyName(int prop, FactoryConnectionDB factConnDB) throws NamingException, SQLException {
		String sqlProp = "SELECT rdn FROM v_propiedad WHERE id=" + prop;
		String name = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlProp);
			if (rs.next()) {
				name = rs.getString(1);
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return name;
	}
	
	public static Integer getClassByName(String name, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sqlClass = "SELECT id FROM clase WHERE rdn ILIKE '" + name + "'";
		Integer idto = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlClass);
			if (rs.next()) {
				idto = rs.getInt(1);
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return idto;
	}
	public static Integer getClassByNameInInstances(String name, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sqlClass = "SELECT id FROM clase WHERE rdn ILIKE '" + name + "'" +
				" and id in(select idto from instances)";
		Integer idto = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlClass);
			if (rs.next()) {
				idto = rs.getInt(1);
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return idto;
	}
	
	public static HashSet<Integer> getSpecialized(int idtoPadre, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sql = "select id_to from t_herencias where id_to_padre=" + idtoPadre;

		HashSet<Integer> hSpec = new HashSet<Integer>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hSpec.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return hSpec;
	}
	
	public static HashSet<Integer> getSpecialized(HashSet<Integer> idtoPadre, FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String sql = "select id_to from t_herencias where id_to_padre in(" + Auxiliar.hashSetIntegerToString(idtoPadre,",") + ")";

		HashSet<Integer> hSpec = new HashSet<Integer>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hSpec.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return hSpec;
	}
	
	public static void addSpecialized(HashSet<Integer> hIdtoPadre, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sql = "select id_to from t_herencias where id_to_padre in(" + Auxiliar.hashSetIntegerToString(hIdtoPadre, ",") + ")";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hIdtoPadre.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
	}
	
	public static boolean isSpecialized(Integer idto, HashSet<Integer> hSpecResto, FactoryConnectionDB fcdb) throws SQLException, NamingException {
		boolean spec = false;
		String sql = "select id_to_padre from t_herencias where id_to=" + idto;

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idtoPadre = rs.getInt(1);
				if (hSpecResto.contains(idtoPadre)) {
					spec = true;
					break;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return spec;
	}

	private static HashSet<Integer> getSpecialized(Connection con, int idtoPadre) throws SQLException, NamingException {
		String sql = "select id_to from t_herencias where id_to_padre=" + idtoPadre;

		HashSet<Integer> hSpec = new HashSet<Integer>();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hSpec.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		return hSpec;
	}
	
	private static HashSet<Integer> getSuperiors(int idtoHijo, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sql = "select id_to_padre from t_herencias where id_to=" + idtoHijo;

		HashSet<Integer> hSup = new HashSet<Integer>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hSup.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return hSup;
	}

	public static String getClassName(int idto, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sql = "select rdn from clase where id=" + idto;

		String claseName = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				claseName = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return claseName;
	}
	public static String getClassNameInInstances(int idto, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String sql = "select rdn from clase where id=" + idto + 
			" and id in(select idto from instances)";
		String claseName = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				claseName = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return claseName;
	}
	
	public static Category getCategory(int idProp, FactoryConnectionDB factConnDB) throws NamingException, SQLException {
		String sql = "SELECT cat FROM v_propiedad WHERE id=" + idProp;

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		Category category = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				Integer cat = rs.getInt(1);
				if (cat!=null) {
					category = new Category(cat);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return category;
	}
	public static HashSet<Integer> getIdtosThatIPoint(FactoryConnectionDB factConnDB, String idtos, Integer idProp) throws SQLException, NamingException {
		HashSet<Integer> hIdtos = new HashSet<Integer>();
		
		String sql = "select valuecls from instances " + 
				"WHERE idto in(" + idtos + ")";
		if (idProp!=null)
			sql += " AND property=" + idProp;
		else
			sql += " AND property<>" + Constants.IdPROP_TARGETCLASS;
			
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int valueCls = rs.getInt(1);
				hIdtos.add(valueCls);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		if (hIdtos.size()>0)
			AuxiliarModel.addSpecialized(hIdtos, factConnDB);
		if (hIdtos.size()==0)
			System.err.println("WARNING SERVER: Individuos a los que no le apunta nadie " + idtos);
		return hIdtos;
	}
	public static HashSet<Integer> getIdtosThatPointTo(Connection con, Integer valueCls) throws SQLException, NamingException {
		HashSet<Integer> hSpec = getSpecialized(con, valueCls);
		hSpec.add(valueCls);
		
		HashSet<Integer> hIdtos = new HashSet<Integer>();
		
		String sql = "select idto from instances " + 
				"WHERE valuecls in(" + Auxiliar.hashSetIntegerToString(hSpec, ",") + ")";
			sql += " AND property<>" + Constants.IdPROP_TARGETCLASS;
			
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hIdtos.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
		}
		if (hIdtos.size()==0) {
			System.err.println("WARNING SERVER: Individuo que no apunta a nadie " + valueCls);
			System.err.println("en query: " + sql);
		}
		return hIdtos;
	}
	public static HashSet<Integer> getIdtosThatPointTo(FactoryConnectionDB factConnDB, Integer valueCls) throws SQLException, NamingException {
		HashSet<Integer> hSups = getSuperiors(valueCls, factConnDB);
		hSups.add(valueCls);
		
		HashSet<Integer> hIdtos = new HashSet<Integer>();
		
		String sql = "select idto from instances " + 
				"WHERE valuecls in(" + Auxiliar.hashSetIntegerToString(hSups, ",") + ")";
			sql += " AND property<>" + Constants.IdPROP_TARGETCLASS;
			
		System.out.println("getIdtosThatPointTo " + sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idto = rs.getInt(1);
				hIdtos.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		if (hIdtos.size()==0)
			System.err.println("WARNING SERVER: Individuo que no apunta a nadie " + valueCls);
		return hIdtos;
	}
	
	public static boolean hasRows(FactoryConnectionDB factConnDB, String sql) throws SQLException, NamingException {
		boolean hasRows = false;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				hasRows = true;
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return hasRows;
	}
}
