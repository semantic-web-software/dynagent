/***
 * DatabaseAdapter.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.server.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.TClase;
import dynagent.common.knowledge.FactInstance;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.ObjectDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;

public class DatabaseAdapter implements IFacade{

	private static FactoryConnectionDB fcdb;
	private String business;
	private static java.sql.Connection connection;
	private /*static */boolean commit;
	private ConnectionDB con;


	public DatabaseAdapter(FactoryConnectionDB fcdb,String business, boolean commit)
	{
		this.fcdb = fcdb;
		this.business = business;
		this.commit = commit;
	}

	public String getBusiness() {
		return business;
	}
	public void setBusiness(String business) {
		this.business = business;
	}

	public DatabaseAdapter(String business){
		this.setBusiness(business);

	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public void rollback() throws SQLException {
		connection.rollback();
	}
	
	public void close() throws SQLException {
		if (con!=null) {
			if (commit) {
				connection.commit();
			}
			connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			fcdb.close(con);
		}
	}
	public void create(ObjectDAO object, FactInstance value, boolean pk) throws SQLException {
		insert(object, value, pk);
	}

	public void insert(ObjectDAO o, Object value, boolean pk) throws SQLException{
		Statement s = null;
		try {
			s = connection.createStatement();

			String sql = "INSERT INTO "+o.getTable()   + o.getParams() + "VALUES (";
			
			if(pk)
				sql += o.getNewPK() + ",";

			sql += o.getValues(value) + ")";

			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.out.println("sqlinsert="+sql);
			s.executeUpdate(sql);
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (s!=null)
				s.close();
			throw ex;
		}
	}

	public int update(ObjectDAO o, String set, String where) throws SQLException{
		int rows = 0;
		Statement s = null;
		try {
			s = connection.createStatement();

			String sql = "UPDATE "+o.getTable() + " SET " + set + " WHERE " + where;
			
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.out.println("sqlupdate="+sql);
			rows = s.executeUpdate(sql);
			
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){			
			if (s!=null)
				s.close();
			throw ex;
		}
		return rows;
	}

	public void delete(ObjectDAO o, String ido) throws SQLException {
		Statement s = null;		
		try {
			s = connection.createStatement();
			String sql = "DELETE FROM "+o.getTable()+ " WHERE "+o.getPK()+" LIKE '"+ido+"'";
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			s.executeUpdate(sql);
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (s!=null)
				s.close();
			throw ex;
		}
	}


	/**
	 * @param o : PropertiesDAO PropiedadDAO usada para devolver un string con los valores que no estan a null
	 * @param object : String Por seguir con la forma de implementar la fachada. Aqui no nos sirve
	 * @param value2 : Object Properties Property que vamos a modificar
	 * 
	 * El metodo selecciona todas las propiedades cuyo id es el de la propiedad que le pasamos (si no se la pasara idProp
	 * tomaria el nombre de dicha propiedad) y actualiza todos los campos que no esten a nulo de la propiedad
	 * @throws SQLException 
	 * 
	 */

	public void setProp(ObjectDAO o, String object, Object value2) throws SQLException {
		Properties value = (Properties) value2;
		PropertiesDAO pdao = (PropertiesDAO) o;
		String sql = "UPDATE "+pdao.getTable()+" SET "+pdao.getValuesNotNull(value);
		sql+=" WHERE rdn='"+value.getNAME()+"'";
		//System.out.println("sql setProp="+sql);
		Statement st = null;

		try {
			st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (st!=null)
				st.close();
			throw ex;
		}
	}

	public void deleteProp(ObjectDAO o, int id) throws SQLException {
		Statement s = null;		
		try {
			s = connection.createStatement();
			String sql = "DELETE FROM "+o.getTable()+ " WHERE id="+id;
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			s.executeUpdate(sql);
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (s!=null)
				s.close();
			throw ex;
		}
	}
	
	public void setClass(ObjectDAO o, String object, Object value2) throws SQLException {
		TClase value = (TClase) value2;
		TClaseDAO cdao = (TClaseDAO) o;
		String sql = "UPDATE clase SET id="+value.getIDTO()+",rdn='"+value.getName()+"',abstracta="+value.isAbstractClass();
		sql+=" WHERE rdn='"+value.getName()+"'"; 
		
		//System.out.println("sql setClass="+sql);
		Statement st = null;

		try {
			st = connection.createStatement();
			st.executeUpdate(sql);
			st.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (st!=null)
				st.close();
			throw ex;
		}
	}

	public void deleteCond(ObjectDAO o, String cond) throws SQLException {
		Statement s = null;
		try {
			s = connection.createStatement();
			String sql = "DELETE FROM "+o.getTable()+ " WHERE "+cond;
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.out.println("sqldeleteCond="+sql);
			s.executeUpdate(sql);
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (s!=null)
				s.close();
			throw ex;
		}
	}

	public void deleteAll(ObjectDAO o) throws SQLException {
		Statement s = null;
		try {
			s = connection.createStatement();
			String sql = "DELETE FROM "+o.getTable();
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.out.println("sqldeleteCond="+sql);
			s.executeUpdate(sql);
			s.close();
			if (commit)
				connection.commit();
		}catch(SQLException ex){
			if (s!=null)
				s.close();
			throw ex;
		}
	}
	
//	public void setPropCat(int idProp, int cat) throws SQLException {
//
//		String sql = "UPDATE properties SET CAT=" + cat + " WHERE PROP=" + idProp;
//
//		Statement st = null;
//		try {
//			st = connection.createStatement();
//			st.executeUpdate(sql);
//			st.close();
//			if (commit)
//				connection.commit();
//		}catch(SQLException ex){
//			if (st!=null)
//				st.close();
//			throw ex;
//		}
//	}

	public FactInstance get(ObjectDAO o, String object) throws SQLException, NamingException{
		InstanceDAO idao = null;
		if(o != null)
			idao = (InstanceDAO)o;
		else{
			idao = (InstanceDAO)DAOManager.getInstance().getDAO("instances").getDAO();
			idao.open();
		}

		Instance i = idao.getInstanceByName(object);
		idao.close();
		return i.toFactInstance();
	}

	public LinkedList<Object> gets(ObjectDAO o, String object) throws SQLException, NamingException {

		LinkedList<Object> sqlresultset = new LinkedList<Object>();
		sqlresultset.add(this.get(o,object));
		return sqlresultset;
	}

	public LinkedList<Object> getAll(ObjectDAO o, String condition, boolean distinct) throws SQLException{
		LinkedList<Object> sqlresultset=new LinkedList<Object>();
		Statement s = null;
		ResultSet rs = null;
		try {
			/*GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
			s = connection.createStatement();
			String gen = generateSQL.getIsolationReadUncommited();
			s.executeUpdate(gen);
			s.close();*/
			
			s = connection.createStatement();
			String sql = "SELECT ";
			if(distinct)
				sql+="DISTINCT ";
			sql+="* FROM "+o.getTable()+ " WHERE "+ condition;
			
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.out.println("sqlallCond="+sql);
			rs = s.executeQuery(sql);
			while(rs.next())
				sqlresultset.add(o.buildObject(rs));
			rs.close();
			s.close();
			
			/*s = connection.createStatement();
			gen = generateSQL.getIsolationRepeatableRead();
			s.executeUpdate(gen);
			s.close();*/
			
			return sqlresultset;
			
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (s!=null)
				s.close();
			throw ex;

		}
	}


	public LinkedList<Object> getWhere(ObjectDAO o,String object, String condition) throws SQLException{
		LinkedList<Object> sqlresultset = new LinkedList<Object>();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			String sql = "SELECT "+object+" FROM "+o.getTable()+" WHERE "+condition;
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.err.println(sql);
			rs = s.executeQuery(sql);
			int i = 1;
			while(rs.next()){
				sqlresultset.add(rs.getObject(i));
				i++;
			}
			rs.close();
			s.close();
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (s!=null)
				s.close();
			throw ex;
		}
		return sqlresultset;
	}

	public LinkedList<Object> getWhereEncrypt(ObjectDAO o,String object, String condition) throws SQLException{
		LinkedList<Object> sqlresultset = new LinkedList<Object>();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
			String function = generateSQL.getDecryptFunction(InstanceService.keyEncrypt, object);
			String sql = "SELECT "+function+" FROM "+o.getTable()+" WHERE "+condition;
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			//System.err.println(sql);
			rs = s.executeQuery(sql);
			int i = 1;
			while(rs.next()){
				String pwd = generateSQL.getDecryptData(rs, 1);
				sqlresultset.add(pwd);
				i++;
			}
			rs.close();
			s.close();
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (s!=null)
				s.close();
			throw ex;
		}
		return sqlresultset;
	}
	
	public LinkedList<Object> getWhere(ObjectDAO o,String object, String condition, int index) throws SQLException{
		LinkedList<Object> sqlresultset = new LinkedList<Object>();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			String sql = "SELECT "+object+" FROM "+o.getTable()+" WHERE "+condition;
			//RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			rs = s.executeQuery(sql);
			while(rs.next()){
				sqlresultset.add(rs.getObject(index));
			}
			rs.close();
			s.close();
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (s!=null)
				s.close();
			throw ex;
		}
		return sqlresultset;
	}



	public LinkedList getMultiple(String object) {
		return null;
	}
	
	public String printConection() throws SQLException {
		String dev = "";
		int con = connection.getTransactionIsolation();
		if (con==Connection.TRANSACTION_READ_UNCOMMITTED)
			dev = "TRANSACTION_READ_UNCOMMITTED";
		else if (con==Connection.TRANSACTION_READ_COMMITTED)
			dev = "TRANSACTION_READ_COMMITTED";
		else if (con==Connection.TRANSACTION_REPEATABLE_READ)
			dev = "TRANSACTION_REPEATABLE_READ";
		else if (con==Connection.TRANSACTION_SERIALIZABLE)
			dev = "TRANSACTION_SERIALIZABLE";
		else if (con==Connection.TRANSACTION_NONE)
			dev = "TRANSACTION_NONE";
		return dev;
	}
	private void open(String business) throws SQLException, NamingException {

		Statement s = null;
		try {
			//m_pool = new poolDB("192.168.0.3", true, true, null);
			// java.sql.Connection proconnection = m_pool.getProcessConn();
			//(H)
			fcdb= DAOManager.getInstance().getFactConnDB();

			con = fcdb.createConnection(false);
			connection = con.getBusinessConn();
			if(!con.isReused())//Para hacerlo solo la primera vez ya que postgre no permite cambiarlo si estamos en medio de una transaccion
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			//if (commit)
				//connection.commit();
			
		} catch (SQLException e) {
			if (s!=null)
				s.close();
			fcdb.close(con);
			throw e;
		} catch (NamingException e) {
			if (s!=null)
				s.close();
			fcdb.close(con);
			throw e;
		}
	}

	//(H)
	/**
	 * Consulta que devuelve todos los usuarios registrados en la base de datos, ese 
	 * resultado sera una lista que contiene objetos del Tipo User que se le puede hacer funciones 
	 * del tipo get para saber que valores contiene y asi poder comparar con los datos introducidos.
	 * @return Lista de objetos del tipo User registrados en la base de datos. 
	 * @param An ObjectDAO and a name, if name ="", return all users, else returns only users with the
	 * specified name.
	 * @throws SQLException 
	 */
	/*public LinkedList<Object> getAllUsers (ObjectDAO o,String name) throws Exception
	{
		LinkedList <Object> result = new LinkedList<Object>();
		//User user = new User();
		Statement s = null;
		ResultSet rs = null;
		try {
			s = connection.createStatement();
			//String sql = "SELECT * FROM "+o.getTable()+" WHERE "+condition;
			String sql;
			if(name.compareTo("")!=0)
				sql = "SELECT * FROM ["+o.getTable()+"]"+" WHERE ([USER] = \'"+ name+ "\')";
			else
				sql = "SELECT * FROM ["+o.getTable()+"]";
			System.out.println("Consulta: "+ sql);
			RuleEngineLogger.getLogger().write("[DatabaseAdapter]:_SQLSTATEMENT="+sql);
			rs = s.executeQuery(sql);
			while(rs.next()){
				User us = new User();			
				us.setName(rs.getString(1));
				us.setUserRol(rs.getString(2));
				us.setPassw(rs.getString(3));			
				result.add(us);
			}
			rs.close();
			s.close();
		}catch(SQLException ex){
			if (rs!=null)
				rs.close();
			if (s!=null)
				s.close();
			if (connection!=null)
				connection.close();
			throw ex;
		}
		return result;
	}*/

	/* METODO MAL IMPLEMENTADO!!! */
	public void set(ObjectDAO o, String object,Object value) {
		/*this.deleteCond(o, "IDTO LIKE "+value.getIDTO()+ " AND PROPERTY LIKE "+value.getPROP());
		this.create(o, value, false);
		 */
	}


	public void open() throws SQLException, NamingException {
		this.open(this.getBusiness());
	}

	public boolean isCommit() {
		return commit;
	}

	public void setCommit(boolean commit) {
		this.commit=commit;
	}

	
}
