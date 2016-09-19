package dynagent.tools.updaters.indices;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.IndexName;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.TClase;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.IndexDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class UpdateIndex {
	private FactoryConnectionDB fcdb;
	private GenerateSQL gSQL;
	
	public UpdateIndex(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
		this.gSQL = new GenerateSQL(fcdb.getGestorDB());
	}
	
	public void startUpdate() {
		//DAOManager.getInstance().setCommit(true);
		try {
			System.err.println("---> Insertando indices como individuos");
			DAOManager.getInstance().setCommit(false); //antes de abrir DAO
			//O_Datos_Attrib_MemoDAO odatMemoDAO = new O_Datos_Attrib_MemoDAO();
			TClaseDAO tdao = new TClaseDAO();
			PropertiesDAO propDAO = new PropertiesDAO();
			IndexDAO indDAO = new IndexDAO();
			indDAO.open();
			//odatDAO.commit();
			
			String sufixIndex = getSufix(fcdb);
			
			String sql = "SELECT id_to, property, " + 
				gSQL.getCharacterBegin() + "index" + gSQL.getCharacterEnd() + ", prefix, " +
				"sufix, property_filter, value_filter, property_prefix, global_sufix, " +
				"MASC_PREFIX_TEMP, PROPERTY_PREFIX_TEMP, CONT_YEAR, LAST_PREFIX_TEMP, MIN_DIGITS, MI_EMPRESA FROM S_index";
			ConnectionDB con = fcdb.createConnection(true);
			Statement st = null;
			ResultSet rs = null;
			System.out.println("sql->"+sql);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					int idto = rs.getInt(1);
					String dominio = tdao.getTClaseById(idto).getName();
					int prop = rs.getInt(2);
					String campoIndexado = propDAO.getPropertyByID(prop).getNAME();
					
					int index = rs.getInt(3);
					String prefix = rs.getString(4);
					String sufix = rs.getString(5);
					Integer propFilter = rs.getInt(6);
					String propFilterStr = null;
					if (rs.wasNull())
						propFilter = null;
					else
						propFilterStr = propDAO.getPropertyByID(propFilter).getNAME();
					
					String valueFilter = rs.getString(7);
					Integer propPrefix = rs.getInt(8);
					String propPrefixStr = null;
					if (rs.wasNull())
						propPrefix = null;
					else
						propPrefixStr = propDAO.getPropertyByID(propPrefix).getNAME();
					
					boolean globalSufix = rs.getBoolean(9);
					if (rs.wasNull())
						globalSufix = false;
					
					String mascPrefixTemp = rs.getString(10);
					Integer propPrefixTemp = rs.getInt(11);
					String propPrefixTempStr = null;
					if (rs.wasNull())
						propPrefixTempStr = null;
					else
						propPrefixTempStr = propDAO.getPropertyByID(propPrefixTemp).getNAME();

					Integer contYear = rs.getInt(12);
					if (rs.wasNull())
						contYear = null;
					String lastPrefixTemp = rs.getString(13);
					Integer minDigits = rs.getInt(14);
					Integer miEmpresa = rs.getInt(15);
					String miEmpresaRdn = null;
					if (rs.wasNull())
						miEmpresa = null;
					else {
						O_Datos_AttribDAO oDAO = new O_Datos_AttribDAO();
						TClaseDAO tDAO = new TClaseDAO();
						int idtoMiEmpresa = (Integer)tDAO.getByName(Constants.CLS_MI_EMPRESA).getFirst();
						LinkedList<Object> loda = oDAO.getAllCond("ID_TO=" + idtoMiEmpresa + " AND PROPERTY=" + Constants.IdPROP_RDN + " AND ID_O=" + miEmpresa);
						if (loda.size()>0)
							miEmpresaRdn = (((O_Datos_Attrib)loda.getFirst()).getVALTEXTO());
					}

					IndexName indName = new IndexName(idto, dominio, prop, campoIndexado, 
							mascPrefixTemp, propPrefixTemp, propPrefixTempStr, contYear, lastPrefixTemp, 
							prefix, propPrefix, propPrefixStr, index, sufix, globalSufix, minDigits, propFilter, propFilterStr, valueFilter, miEmpresa, miEmpresaRdn);
					indDAO.insert(indName, sufixIndex);
				}
				
				indDAO.commit();
				//odatDAO.setCommit(true);
				indDAO.close();
			} catch (Exception e) {
				System.err.println("ERROR: Actualización de índices con errores. No realizada.");
				e.printStackTrace();
				indDAO.rollback();
				System.out.println("hace rollback");
				indDAO.close();
				fcdb.removeConnections();
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con != null) {
					fcdb.close(con);
				}
			}
		} catch (Exception e) {
			System.err.println("ERROR: Actualización de índices con errores. No realizada.");
			e.printStackTrace();
			try {
				fcdb.removeConnections();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}

	private String getSufix(FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sufix = null;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT " + gSQL.getCharacterBegin() + "value" + gSQL.getCharacterEnd() + 
			" FROM configuration" + 
			" where " + gSQL.getCharacterBegin() + "label" + gSQL.getCharacterEnd() + 
			" in('sufix_tienda')";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				System.out.println("sufix_tienda");
				sufix = rs.getString(1);
				System.out.println("sufix " + sufix);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return sufix;
	}

}
