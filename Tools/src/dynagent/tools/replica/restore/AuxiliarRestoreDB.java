package dynagent.tools.replica.restore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ConfigurationValues;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.O_Datos_Attrib_Memo;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.O_Datos_Attrib_MemoDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class AuxiliarRestoreDB {

	public static ConfigurationValues getConfiguration(FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sufix = null;
		String IPCentral = null;
		Integer portCentral = null;
		int businessCentral = 0;
		String miAlmacen = null;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT " + gSQL.getCharacterBegin() + "value" + gSQL.getCharacterEnd() + 
			", " + gSQL.getCharacterBegin() + "label" + gSQL.getCharacterEnd() +
			" FROM configuration";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String label = rs.getString(2);
				System.out.println("label " + label);
				if (label.equals("sufix_tienda")) {
					System.out.println("sufix_tienda");
					sufix = rs.getString(1);
					System.out.println("sufix " + sufix);
				} else if (label.equals("mi_almacen")) {
					System.out.println("mi_almacen");
					miAlmacen = rs.getString(1);
					System.out.println("miAlmacen " + miAlmacen);
				} else if (label.equals("IP_Central")) {
					System.out.println("IP_Central");
					IPCentral = rs.getString(1);
					System.out.println("IPCentral " + IPCentral);
				} else if (label.equals("port_Central")) {
					System.out.println("port_Central");
					portCentral = Integer.parseInt(rs.getString(1));
					System.out.println("portCentral " + portCentral);
				} else if (label.equals("business_Central")) {
					System.out.println("business_Central");
					businessCentral = Integer.parseInt(rs.getString(1));
					System.out.println("businessCentral " + businessCentral);
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
		ConfigurationValues config = new ConfigurationValues(sufix, miAlmacen, IPCentral, portCentral, businessCentral);
		return config;
	}

	public static ArrayList<O_Datos_Attrib> getO_Datos_Atrib(String almacen, String sufix, 
			ArrayList<O_Datos_Attrib> created, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "SELECT ID_TO,ID_O,PROPERTY,VAL_NUM,VAL_TEXTO,VALUE_CLS,Q_MIN,Q_MAX,SYS_VAL,DESTINATION,'index_replica' " +
				"FROM O_Datos_Atrib WHERE";
			//coger tambien los globales con sufijo 501 (el de central)
				//me traigo stocks de central y los mios
			sql += " substring(id_o,length(id_o)-2, 3)='501'";  //no necesario si se hubieran creado bien, con *
			sql += " or LPAD(substring(id_o,length(id_o)-2, 3)-500,3,'0')='" + sufix + "'";
				//me traigo lo que tenga destino * o sea para mi 
				//y no lo haya creado yo ni sea stock
			sql += " or (DESTINATION='*' or DESTINATION='" + almacen + "')";
			sql += " and substring(id_o,length(id_o)-2, 3)<>'" + sufix + "'" + 
				" and substring(id_o,length(id_o)-2, 3)<>'501'" + 
				" and LPAD(substring(id_o,length(id_o)-2, 3)-500,3,'0')<>'" + sufix + "'";
			
				//union lo creado por mi
			sql += " union " + 
				"SELECT ID_TO,ID_O,PROPERTY,VAL_NUM,VAL_TEXTO,VALUE_CLS,Q_MIN,Q_MAX,SYS_VAL,DESTINATION,'index' " +
				"FROM O_Datos_Atrib where substring(id_o,length(id_o)-2, 3)='" + sufix + "'";

		System.out.println(sql);
		ArrayList<O_Datos_Attrib> dataReplica = new ArrayList<O_Datos_Attrib>();

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				if (rs.wasNull())
					idto = null;
				Integer ido = rs.getInt(2);
				if (rs.wasNull())
					ido = null;
				Integer prop = rs.getInt(3);
				if (rs.wasNull())
					prop = null;
				Integer valNum = rs.getInt(4);
				if (rs.wasNull())
					valNum = null;
				String valTexto = rs.getString(5);
				Integer valueCls = rs.getInt(6);
				if (rs.wasNull())
					valueCls = null;
				Double qMin = rs.getDouble(7);
				if (rs.wasNull())
					qMin = null;
				Double qMax = rs.getDouble(8);
				if (rs.wasNull())
					qMax = null;
				String sysval = rs.getString(9);
				String destination = rs.getString(10);

				O_Datos_Attrib dat = new O_Datos_Attrib(idto,ido,prop,valNum,valTexto,valueCls,qMin,qMax,sysval,destination);
				if (rs.getString(11).equals("index"))
					created.add(dat);
				else
					dataReplica.add(dat);
			}
		} finally {
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			if(con!=null)
				fcdb.close(con);
		}
		return dataReplica;
	}
	public static ArrayList<O_Datos_Attrib_Memo> getO_Datos_Atrib_Memo(String almacen, String sufix, 
			ArrayList<O_Datos_Attrib_Memo> created, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "SELECT ID_TO,ID_O,PROPERTY,MEMO,VALUE_CLS,SYS_VAL,DESTINATION,'index_replica' " +
				"FROM O_Datos_Atrib_Memo WHERE";
		sql += " substring(id_o,length(id_o)-2, 3)='501'";  //no necesario si se hubieran creado bien, con *
		sql += " or LPAD(substring(id_o,length(id_o)-2, 3)-500,3,'0')='" + sufix + "'";
		sql += " or (DESTINATION='*' or DESTINATION='" + almacen + "')";
		sql += " and substring(id_o,length(id_o)-2, 3)<>'" + sufix + "'" + 
			" and substring(id_o,length(id_o)-2, 3)<>'501'" + 
			" and LPAD(substring(id_o,length(id_o)-2, 3)-500,3,'0')<>'" + sufix + "'";
		sql += " union " + 
			"SELECT ID_TO,ID_O,PROPERTY,MEMO,VALUE_CLS,SYS_VAL,DESTINATION,'index' " +
			"FROM O_Datos_Atrib_Memo where substring(id_o,length(id_o)-2, 3)='" + sufix + "'";
		
		System.out.println(sql);
		ArrayList<O_Datos_Attrib_Memo> dataReplica = new ArrayList<O_Datos_Attrib_Memo>();

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				if (rs.wasNull())
					idto = null;
				Integer ido = rs.getInt(2);
				if (rs.wasNull())
					ido = null;
				Integer prop = rs.getInt(3);
				if (rs.wasNull())
					prop = null;
				String memo = rs.getString(4);
				Integer valueCls = rs.getInt(5);
				if (rs.wasNull())
					valueCls = null;
				String sysval = rs.getString(6);
				String destination = rs.getString(7);
				
				O_Datos_Attrib_Memo dat = new O_Datos_Attrib_Memo(idto,ido,prop,memo,valueCls,sysval,destination);
				if (rs.getString(8).equals("index"))
					created.add(dat);
				else
					dataReplica.add(dat);
			}
		} finally {
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			if(con!=null)
				fcdb.close(con);
		}
		return dataReplica;
	}
	
//	public static void test(String almacen,FactoryConnectionDB fcdb) throws NamingException, SQLException {
//		String sql = "SELECT * FROM replica_master WHERE DESTINATION IS NULL OR DESTINATION='"+almacen+"'";
//		
//		Statement st = null;
//		ResultSet rs = null;
//		ConnectionDB con = null;
//		try{
//			System.err.println("SQL:"+sql);
//			con = fcdb.createConnection(true);
//			st = con.getBusinessConn().createStatement();
//			 Date now=new Date(System.currentTimeMillis());
//			 System.err.println("\n        ************Antes query *******    "+now);
//			rs = st.executeQuery(sql);
//			now=new Date(System.currentTimeMillis());
//			 System.err.println("\n        ************despues query *******    "+now);
//			 
//			 int i=0;
//			 while(rs.next())
//				 i++;
//			 System.err.println("Numero de registros:"+i);
//		}finally{
//			if(rs!=null)
//				rs.close();
//			if(st!=null)
//				st.close();
//			//if(con!=null)
//				//con.close();
//		}
//	}
	
	public static boolean insertO_Datos_Atrib(O_Datos_Attrib data,FactoryConnectionDB fcdb, 
			StringBuffer newDataComplete, StringBuffer newDataIncomplete, StringBuffer updates) throws NamingException, SQLException {
		boolean insert = true;
		O_Datos_AttribDAO odatDAO = new O_Datos_AttribDAO();
		odatDAO.open();
		if (data.getIDO()<Constants.MIN_ID_INDIVIDUAL_BDDD) {
			//si id_o<11000 comprobar si esta o no
			//si esta actualizar
			LinkedList<Object> llo = odatDAO.getAllCond("PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO());
			if (llo.size()>0) {
				insert = false;
				//System.out.println("existe");
				String set = "VAL_NUM="+data.getVALNUM();
				if (data.getVALTEXTO()!=null)
					set += ",VAL_TEXTO='"+data.getVALTEXTO().replaceAll("'", "''") + "'";
				else
					set += ",VAL_TEXTO=null";
				if (data.getDESTINATION()!=null)
					set += ",DESTINATION='"+data.getDESTINATION().replaceAll("'", "''") + "'";
				else
					set += ",DESTINATION=null";
				set += ",Q_MIN="+data.getQMIN();
				set += ",Q_MAX="+data.getQMAX();
				String sql = "UPDATE O_Datos_Atrib SET " + set + " WHERE " + "PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO() + ";";
				updates.append(sql + "\n");
				//odatDAO.update(set, "PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO());
			}
		}
		if (insert) {
			String sqlComplete = "INSERT INTO O_Datos_Atrib" + odatDAO.getParams()+ "VALUES";
			String sqlIncomplete = "," + "\n";
			sqlComplete += " (" + odatDAO.getValues(data) + ")";
			sqlIncomplete += " (" + odatDAO.getValues(data) + ")";
			newDataComplete.append(sqlComplete);
			newDataIncomplete.append(sqlIncomplete);
			//odatDAO.insert(data);
		}
		odatDAO.close();
		return insert;
	}
	public static boolean insertO_Datos_Atrib_Memo(O_Datos_Attrib_Memo data,FactoryConnectionDB fcdb, 
			StringBuffer newDataComplete, StringBuffer newDataIncomplete, StringBuffer updates) throws NamingException, SQLException {
		boolean insert = true;
		O_Datos_Attrib_MemoDAO odatDAO = new O_Datos_Attrib_MemoDAO();
		odatDAO.open();
		if (data.getIDO()<Constants.MIN_ID_INDIVIDUAL_BDDD) {
			//si id_o<11000 comprobar si esta o no
			//si esta actualizar
			LinkedList<Object> llo = odatDAO.getAllCond("PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO());
			if (llo.size()>0) {
				insert = false;
				String set = null;
				if (data.getMEMO()!=null)
					set += "MEMO='"+data.getMEMO().replaceAll("'", "''") + "'";
				else
					set += "MEMO=null";
				if (data.getDESTINATION()!=null)
					set += ",DESTINATION='"+data.getDESTINATION().replaceAll("'", "''") + "'";
				else
					set += ",DESTINATION=null";
				
				String sql = "UPDATE O_Datos_Atrib_Memo SET " + set + " WHERE " + "PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO() + ";";
				updates.append(sql + "\n");
				//odatDAO.update(set, "PROPERTY=" + data.getPROPERTY() + " and ID_O=" + data.getIDO());
			}
		}
		if (insert) {
			String sqlComplete = "INSERT INTO O_Datos_Atrib_Memo" + odatDAO.getParams()+ "VALUES";
			String sqlIncomplete = "," + "\n";
			sqlComplete += " (" + odatDAO.getValues(data) + ")";
			sqlIncomplete += " (" + odatDAO.getValues(data) + ")";
			newDataComplete.append(sqlComplete);
			newDataIncomplete.append(sqlIncomplete);
			//odatDAO.insert(data);
		}
		odatDAO.close();
		return insert;
	}
	/*public static void deleteO_Datos_Atrib(O_Datos_Attrib data,FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "DELETE FROM O_Datos_Atrib WHERE ID_TO=" + data.getIDTO() + " and ID_O=" + data.getIDO() + " and PROPERTY=" + 
			data.getPROPERTY() + " and VAL_NUM=" + data.getVALNUM() + " and VAL_TEXTO='" + data.getVALTEXTO() + "' and VALUECLS=" + 
			data.getVALUECLS() + " and Q_MIN=" + data.getQMIN() + " and Q_MAX=" + data.getQMAX() + " and OP is null" + 
			" and SYS_VAL='"+data.getSYS_VAL()+"' and DESTINATION='"+data.getDESTINATION()+"'";
		
		AuxiliarImporters.dbExecUpdate(fcdb, sql);
	}*/
	
	public static void insertO_Reg_Instancias_Index_Replica(int ido, int idto, String rdn, 
			FactoryConnectionDB fcdb, StringBuffer newORegReplComplete, StringBuffer newORegReplIncomplete) throws NamingException, SQLException {
//		O_Reg_InstanciasDAO oReg = new O_Reg_InstanciasDAO();
//		LinkedList<Object> llo = oReg.getAllCond("ID_O=" + ido);
//		if (llo.size()==0) {
			String sqlComplete = "INSERT INTO O_Reg_Instancias_Index_Replica(ID_O, ID_TO, RDN) VALUES";
			String sqlIncomplete = "," + "\n";
			sqlComplete += " (" + ido + "," + idto + ",'" + rdn.replaceAll("'", "''") +"')";
			sqlIncomplete += " (" + ido + "," + idto + ",'" + rdn.replaceAll("'", "''") +"')";
			newORegReplComplete.append(sqlComplete);
			newORegReplIncomplete.append(sqlIncomplete);
			//AuxiliarImporters.dbExecUpdate(fcdb, sql);
//		}
	}
	public static void updateO_Reg_Instancias_Index_Replica(int ido, int idto, String rdn, 
			FactoryConnectionDB fcdb, StringBuffer updates) throws NamingException, SQLException {
		String sql = "UPDATE O_Reg_Instancias_Index_Replica SET RDN='" + rdn.replaceAll("'", "''") +"'" +
				" WHERE ID_O=" + ido + " AND ID_TO=" + idto +";"; 
		updates.append(sql + "\n");
		//AuxiliarImporters.dbExecUpdate(fcdb, sql);
	}
	public static void insertO_Reg_Instancias_Index(int ido, int idto, String rdn, 
			FactoryConnectionDB fcdb, StringBuffer newORegComplete, StringBuffer newORegIncomplete) throws NamingException, SQLException {
//		O_Reg_InstanciasDAO oReg = new O_Reg_InstanciasDAO();
//		LinkedList<Object> llo = oReg.getAllCond("ID_O=" + ido);
//		if (llo.size()==0) {
			String idoStr = String.valueOf(ido);
			String autonum = idoStr.substring(0,idoStr.length()-3);
			String sqlComplete = "INSERT INTO O_Reg_Instancias_Index(AUTONUM, ID_O, ID_TO, RDN) VALUES";
			String sqlIncomplete = "," + "\n";
			sqlComplete += " (" + autonum + "," + ido + "," + idto + ",'" + rdn.replaceAll("'", "''") +"')";
			sqlIncomplete += " (" + autonum + "," + ido + "," + idto + ",'" + rdn.replaceAll("'", "''") +"')";
			newORegComplete.append(sqlComplete);
			newORegIncomplete.append(sqlIncomplete);
			//AuxiliarImporters.dbExecUpdate(fcdb, sql);
//		}
	}
	public static void updateO_Reg_Instancias_Index(int ido, int idto, String rdn, 
			FactoryConnectionDB fcdb, StringBuffer updates) throws NamingException, SQLException {
		String sql = "UPDATE O_Reg_Instancias_Index SET RDN='" + rdn.replaceAll("'", "''") +"'" +
				" WHERE ID_O=" + ido + " AND ID_TO=" + idto +";"; 
		updates.append(sql + "\n");
		//AuxiliarImporters.dbExecUpdate(fcdb, sql);
	}
	
}
