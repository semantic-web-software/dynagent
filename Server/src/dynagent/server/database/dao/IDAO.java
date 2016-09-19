/***
 * IDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.server.database.dao;

import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.server.ejb.FactoryConnectionDB;

public interface IDAO {
	public void setFactConnDB(FactoryConnectionDB fcdb);
	public void setBusiness(String business);
	public void setCommit(boolean commit);
	public boolean isCommit();
	public void open() throws SQLException, NamingException;
	public void close() throws SQLException;
	public void commit() throws SQLException;
	public void rollback() throws SQLException;
	public Object getDAO();
	public LinkedList<Object> getByID(String id) throws SQLException;
	public LinkedList<Object> getAll() throws SQLException;
	public LinkedList<Object> getAllCond(String cond) throws SQLException;
}
