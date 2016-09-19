/***
 * T_HerenciasDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a DAO class for table 'T_Herencias'
 */

package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.T_Herencias;

public class T_HerenciasDAO extends ObjectDAO{
	
	public T_HerenciasDAO(){
		super("T_Herencias",true);
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "*", "ID_TO LIKE '"+id+"'");
	}
	
	public LinkedList<Object> getParents(String id) throws SQLException{
		return i.getWhere(this, "ID_TO_Padre", "ID_TO LIKE '"+id+"'",1);
	}
	
	public LinkedList<Object> getFirstParent(String id) throws SQLException{
		return i.getWhere(this, "MIN(ID_TO_Padre)", "ID_TO LIKE '"+id+"'");
	}

	public void insert(T_Herencias in) throws SQLException{
		i.insert(this, in, false);
	}
	
	/*public void set(T_Herencias in){
		i.set(this, "", in.toFactHierarchy());
	}
	*/
	
	public String getParams(){
		return " (ID_TO, ID_TO_Padre) ";
	}
	
	public String getPK(){
		return null;
	}
	
	public String getNewPK(){
		return null;
	}
	
	public String getLastPK(){
		return null;
	}
	
	public String getValues(Object value){
		
		T_Herencias value2 = (T_Herencias) value;
		
		//TODO Comprobar si es asi o al reves: Primero idto y luego idto_padre
		return value2.getID_TO() + ","
			+  value2.getID_TO_Padre() ;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.T_Herencias in = new dynagent.common.basicobjects.T_Herencias();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0)
				in.setID_TO(new Integer(rs.getString(i+1)).intValue());
			else if (i == 1)
				in.setID_TO_Padre(new Integer(rs.getString(i+1)).intValue());
		}
		return in;
	}
}
