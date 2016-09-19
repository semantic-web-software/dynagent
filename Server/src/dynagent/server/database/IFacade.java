/***
 * IFacade.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 * @description: An interface of a facade
 */

package dynagent.server.database;

import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.knowledge.FactInstance;
import dynagent.server.database.dao.ObjectDAO;

public interface IFacade extends IBusinessConnection{
	public void open() throws SQLException, NamingException;
	public void commit() throws SQLException;
	public void rollback() throws SQLException;
	public String printConection() throws SQLException;
	public void close() throws SQLException;
	public FactInstance get(ObjectDAO o, String object) throws SQLException, NamingException;
	public LinkedList<Object> getAll(ObjectDAO o, String condition, boolean distinct) throws SQLException;
	//(H)
	//public LinkedList<Object> getAllUsers(ObjectDAO o, String condition) throws Exception;
	public LinkedList<Object> gets(ObjectDAO o, String object) throws SQLException, NamingException;
	
	public void set(ObjectDAO o, String object, Object value);
	public void setProp(ObjectDAO o, String object, Object value) throws SQLException;
	//public void setPropCat(int idProp, int cat) throws SQLException;
	public void create(ObjectDAO o, FactInstance value, boolean pk) throws SQLException;
	public void delete(ObjectDAO o, String object) throws SQLException;
	public void deleteCond(ObjectDAO o, String cond) throws SQLException;
	public void deleteAll(ObjectDAO o) throws SQLException;
	//public void insert(ObjectDAO o, Fact value, boolean pk);
	public void insert(ObjectDAO o, Object value, boolean pk) throws SQLException;
	public int update(ObjectDAO o, String set, String where) throws SQLException;
	public LinkedList<Object> getWhere(ObjectDAO o,String object, String condition) throws SQLException;
	public LinkedList<Object> getWhereEncrypt(ObjectDAO o,String object, String condition) throws SQLException;
	public LinkedList<Object> getWhere(ObjectDAO o,String object, String condition, int index) throws SQLException;
	public boolean isCommit();
	public void setCommit(boolean commit);
	public void setClass(ObjectDAO o, String object, Object value2) throws SQLException;

	public void deleteProp(ObjectDAO o, int id) throws SQLException;
}
