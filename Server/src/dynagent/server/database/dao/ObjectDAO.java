/***
 * ObjectDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es	
 */

package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.knowledge.FactInstance;
import dynagent.server.database.DatabaseFacadeFactory;
import dynagent.server.database.IFacade;
import dynagent.server.ejb.FactoryConnectionDB;

public abstract class ObjectDAO implements IDAO{

	protected IFacade i = null;
	private String table;
	private FactoryConnectionDB factConnDB;
	private String business;
	protected boolean distinct;
	

	public ObjectDAO(String table,boolean distinct){
		setTable(table);
		this.distinct=distinct;
		i=DatabaseFacadeFactory.getInstance().createFacade(DAOManager.getInstance().getBusiness(),DAOManager.getInstance().getFactConnDB(),DAOManager.getInstance().isCommit(),"default");
	}
	
	public boolean isCommit() {
		return i.isCommit();
	}
	
	public void setCommit(boolean commit) {
		i.setCommit(commit);
	}

	public FactoryConnectionDB getFactConnDB() {
		return factConnDB;
	}
	public void setFactConnDB(FactoryConnectionDB factConnDB) {
		this.factConnDB = factConnDB;
	}

	public LinkedList<Object> getByID(String id) throws SQLException {
		return null;
	}
	
	public LinkedList<Object> getAll() throws SQLException{
		return i.getAll(this, "1 = 1", distinct);
	}
	
	public LinkedList<Object> getAllCond(String cond) throws SQLException{
		return i.getAll(this, cond, distinct);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}
	
	public Object getDAO(){
		return this;
	}
	
	public String getParams(){
		return null;
	}

	public String getPK(){
		return null;
	}
	
	public String getValues(FactInstance v){
		return null;
	}
	
	
	/* 
	 * Si diera error aqui seria que he puesto el 
	 * getValues generico y no de los hijos de la clase
	 */
	
	public abstract String getValues(Object v);
	
		
	public String getNewPK() throws NumberFormatException, SQLException{
		return null;
	}
	
	public String getLastPK(String rmin, String rmax) throws SQLException{
		return null;
	}
	
	public void open() throws SQLException, NamingException{
		i.open();
	}
	
	public void close() throws SQLException{
		i.close();
	}

	public void commit() throws SQLException{
		i.commit();
	}
	
	public void rollback() throws SQLException{
		i.rollback();
	}
	
	public void setBusiness(String b) {
		this.business = b;
		
	}
	public String printConection() throws SQLException {
		return i.printConection();

	}
	public void delete(String id) throws SQLException{
		i.delete(this, id);
	}
	public void deleteAll() throws SQLException{
		i.deleteAll(this);
	}
	public void deleteCond(String cond) throws SQLException{
		i.deleteCond(this,cond);
	}
	
	public abstract Object buildObject(ResultSet rs) throws NumberFormatException, SQLException;
}
