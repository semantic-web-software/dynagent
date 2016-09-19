package dynagent.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

import dynagent.common.Constants;
import dynagent.common.basicobjects.IndexFilter;
import dynagent.common.exceptions.DataErrorException;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.replication.ReplicationEngine;
import dynagent.server.services.IndexFilterFunctions;
import dynagent.server.services.querys.AuxiliarQuery;

public class IndividualCreator {

	public static Individual creator(FactoryConnectionDB fcdb, boolean commit, GenerateSQL gSQL, int idto, String rdn, String valuePropPrefix, String sufixIndex, String destination, boolean replica, Integer id) throws SQLException, DataErrorException, NamingException {
		
		int autonum = subInsertRowObject(fcdb, commit, idto);
		Integer ido = null;
		if (sufixIndex!=null) {
			String idoStr = String.valueOf(autonum) + sufixIndex;
			ido = Integer.parseInt(idoStr);
		} else
			ido = autonum;
		
		if (rdn==null)
			System.out.println("rdn es nulo");
		if (rdn==null)
			rdn = getIndex(fcdb, commit, gSQL, ido, idto, valuePropPrefix, destination);
		
		if (rdn==null)
			rdn = String.valueOf(ido);
		
//		String sql = "UPDATE O_Reg_instancias_Index SET rdn='" + rdn.replaceAll("'", "''") + "', id_o=" + ido + " WHERE autonum=" + autonum;
//		AuxiliarQuery.dbExecUpdate(fcdb, sql, commit);
//		if (destination!=null)
//			sql = "INSERT INTO O_Datos_Atrib(ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, Q_MIN, DESTINATION) " +
//				"VALUES(" + idto + "," + ido + "," + Constants.IdPROP_RDN + ",null,'" + rdn.replaceAll("'", "''") + "'," + Constants.IDTO_STRING + ",null,null,'" + destination.replaceAll("'", "''") + "')";
//		else
//			sql = "INSERT INTO O_Datos_Atrib(ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, Q_MIN, DESTINATION) " +
//				"VALUES(" + idto + "," + ido + "," + Constants.IdPROP_RDN + ",null,'" + rdn.replaceAll("'", "''") + "'," + Constants.IDTO_STRING + ",null,null,null)";
//		AuxiliarQuery.dbExecUpdate(fcdb, sql, commit);
//		if (replica) {
//			sql = "INSERT INTO O_Datos_Atrib(ID, ACTION, ID_TO, ID_O, PROPERTY, VAL_NUM, OLD_VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, Q_MIN, DESTINATION, DATE) " +
//				"VALUES(" + id + ",'" + ReplicationEngine.NEW + "'," + idto + "," + ido + "," + Constants.IdPROP_RDN + ",null,null,'" + rdn.replaceAll("'", "''") + "'," + Constants.IDTO_STRING + ",null,null,'" + destination.replaceAll("'", "''") + "'," + System.currentTimeMillis() + ")";
//			AuxiliarQuery.dbExecUpdate(fcdb, sql, commit);
//		}
		
		Individual ind = new Individual(ido, rdn);
		return ind;
	}
	
	public static int subInsertRowObject(FactoryConnectionDB fcdb, boolean commit, int id_to) throws SQLException, NamingException {
		String random = String.valueOf((new Random()).nextInt());
		String sql1 = "INSERT INTO O_Reg_Instancias_Index(ID_TO, RDN) VALUES(" + id_to + ",'" + random.replaceAll("'", "''") + "')";
		AuxiliarQuery.dbExecUpdate(fcdb, sql1, commit);
		System.out.println("INSERT ROW");
		
		int autonum = 0;
		String sql2 = "SELECT autonum FROM o_reg_instancias_index WHERE rdn='" + random + "'" + " AND id_to=" + id_to;
		ConnectionDB con = fcdb.createConnection(commit);
		Statement st1 = null;
		Statement st2 = null;
		ResultSet rs = null;
		System.out.println("sql->"+sql2);
		try {
			st2 = con.getBusinessConn().createStatement();
			rs = st2.executeQuery(sql2);
			rs.next();
			autonum = rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st1 != null)
				st1.close();
			if (st2 != null)
				st2.close();
			if (con!=null)
				fcdb.close(con);
		}
		return autonum;
	}

	//TODO propPrefix, prefijos temporales, filtros y mi_empresa no soportado
	private static String getIndex(FactoryConnectionDB fcdb, boolean commit, GenerateSQL gSQL, int ido, int idto, String valuePropPrefix, String destination) 
			throws SQLException, NamingException, DataErrorException  {
		String changeRdn = null;
		ArrayList<IndexFilter> aIndexF = new ArrayList<IndexFilter>();
		getIndexDB(fcdb, gSQL, aIndexF, idto, commit);
		
		if (aIndexF.size()==0)
			//throw new DataErrorException("Falta indice para idto " + idto);
			System.out.println("Falta indice para idto " + idto);
		else {
			HashMap<Integer,StringBuffer> valuesFilter = new HashMap<Integer,StringBuffer>();
			IndexFilter indexFilterUpdate = IndexFilterFunctions.createIndexFilter(aIndexF, valuesFilter);
			if (indexFilterUpdate!=null) {
					String change = "";
					Integer propPrefix = indexFilterUpdate.getPropPrefix();
					if (propPrefix!=null)
						change = valuePropPrefix;
					String prefix = indexFilterUpdate.getPrefix();
					//System.out.println("prefix " + prefix);
					if (prefix!=null)
						change += prefix;
					Integer minDigits = indexFilterUpdate.getMinDigits();
					int index = indexFilterUpdate.getIndex();
					//System.out.println("index " + index);
					if (minDigits!=null)
						change += StringUtils.leftPad(String.valueOf(index), minDigits, '0');
					else
						change += index;
					String sufix = indexFilterUpdate.getSufix();
					//System.out.println("sufix " + sufix);
					if (sufix!=null)
						change += sufix;
					
					//if (keyProp==Constants.IdPROP_RDN)
						changeRdn = change;
					//else {
						//String sql = "INSERT INTO O_Datos_Atrib(ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, Q_MAX, Q_MIN, DESTINATION) " +
						//		"VALUES(" + idto + "," + ido + "," + keyProp + ",null,'" + change.replaceAll("'", "''") + "'," + Constants.IDTO_STRING + ",null,null,'" + destination.replaceAll("'", "''") + "')";
						//AuxiliarQuery.dbExecUpdate(fcdb, sql, commit);
					//}
					//ahora incremento de indice
					incrementIndex(fcdb, commit, indexFilterUpdate.getIdo(), gSQL);
					//IndexFilter.incrementIndex(indexFilterUpdate, keyProp, idto, true, fcdb);
				//}
			}
		}
		return changeRdn;
	}
	
	private static boolean getIndexDB(FactoryConnectionDB fcdb, GenerateSQL generateSQL, 
			ArrayList<IndexFilter> aIndexF, int idto, boolean commit) throws SQLException, NamingException {
		boolean hasPropIndexFilter = false;
		String sql = "SELECT id_o, " +
				"masc_prefix_temp, property_prefix_temp, cont_year, last_prefix_temp, " +
				"property_prefix, prefix, " +
				generateSQL.getCharacterBegin() + "index" + generateSQL.getCharacterEnd() + ", " +
				"sufix, global_sufix, property_filter, value_filter, min_digits, mi_empresa " +
				"FROM s_index WHERE id_to=" + idto;
		ConnectionDB con = fcdb.createConnection(commit);
		Statement st = null;
		ResultSet rs = null;
		System.out.println("sql->"+sql);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int ido = rs.getInt(1);
				int index = rs.getInt(8);
				String prefix = rs.getString(7);
				String sufix = rs.getString(9);
				Integer propPrefix = rs.getInt(6);
				if (rs.wasNull())
					propPrefix = null;
				Integer propFilter = rs.getInt(11);
				if (rs.wasNull())
					propFilter = null;
				else
					hasPropIndexFilter = true;
				String valueFilter = rs.getString(12);
				boolean globalSufix = rs.getBoolean(10);
				Integer minDigits = rs.getInt(13);
				Integer miEmpresa = rs.getInt(14);

				String mascPrefixTemp = rs.getString(2);
				Integer propPrefixTemp = rs.getInt(3);
				if (rs.wasNull())
					propPrefixTemp = null;
				Integer contYear = rs.getInt(4);
				if (rs.wasNull())
					contYear = null;
				String lastPrefixTemp = rs.getString(5);
				
				//if (valueFilter!=null)
					//valueFilter = parserValueFilterDAO(valueFilter);
				IndexFilterFunctions.createIndex(aIndexF, ido, index, prefix, sufix, propPrefix, propFilter, valueFilter, globalSufix, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, minDigits, miEmpresa);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return hasPropIndexFilter;
	}
	public static void incrementIndex(FactoryConnectionDB fcdb, boolean commit, int ido, GenerateSQL gSQL) throws SQLException, NamingException {
		//IndexDAO indDAO = new IndexDAO();
		//indDAO.update(IndexFilter.setIncrementIndex(indexFilterUpdate.getIndex(), gSQL), IndexFilter.whereIncrementIndex(indexFilterUpdate, keyProp, idto, true));
		String nameProp = Constants.PROP_INICIO_CONTADOR;
		String sqlPropIndex = "(SELECT prop FROM properties WHERE name='" + nameProp + "')";
		String set = "q_min=q_min+1,q_max=q_max+1";
		String where = "id_o=" + ido + " AND property=" + sqlPropIndex;
		String sql = "UPDATE o_datos_atrib SET " + set + " WHERE " + where;
		AuxiliarQuery.dbExecUpdate(fcdb, sql, commit);
	}
}
