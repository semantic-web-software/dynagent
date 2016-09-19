/***
 * Facade.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 * @description: Represents a generic facade
 */

package dynagent.server.database;

import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.knowledge.FactInstance;
import dynagent.server.database.dao.ObjectDAO;

/*
 * Facade represents a generic facade
 */
public class Facade implements IFacade{

	public void close() {}

	public void create(ObjectDAO o, FactInstance value, boolean pk) {}

	public void delete(ObjectDAO o,String object) {}
	
	public FactInstance get(ObjectDAO o,String object){ return null; }

	public LinkedList<Object> gets(ObjectDAO o,String object){ return null; }

	public void open() {}

	public void set(ObjectDAO o,String object, Object value) {}
	
	public void setProp(ObjectDAO o,String object, Object value) {}
	
	public void insert(ObjectDAO o,Object value, boolean pk){}
	
	public LinkedList<Object> getWhere(ObjectDAO o, String object, String condition) { return null; }
	
	public LinkedList<Object> getAll(ObjectDAO o, String condition, boolean distinct) { return null; }
	
	public LinkedList<Object> getWhere(ObjectDAO o,String object, String condition, int index) { return null; }

	public LinkedList<Object> getWhereEncrypt(ObjectDAO o,String object, String condition) { return null; }
	
	public void setPropCat(int idProp, int cat) {
		// TODO Auto-generated method stub
		
	}

	public void deleteCond(ObjectDAO o, String cond) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public int update(ObjectDAO o, String set, String where) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public void commit() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public void rollback() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public String printConection() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteAll(ObjectDAO o) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public boolean isCommit() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCommit(boolean commit) {
		// TODO Auto-generated method stub
		
	}

	public void setClass(ObjectDAO o, String object, Object value2) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteProp(ObjectDAO o, int id) throws SQLException {
		// TODO Auto-generated method stub
		
	}
}
