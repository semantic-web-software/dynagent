package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Roles;


public class RolesDAO extends ObjectDAO{

	public RolesDAO(){
		super("Roles",false);
	}
	
	public void insert(Roles in) throws SQLException{
		i.insert(this, in, false);
	}

	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}

	public String getValues(Object v) {
		Roles sr= (Roles)v;
		
		String stringvalue = "";
	
		if(sr.getIdoRol()== null)
			stringvalue += "NULL,";
		else
			stringvalue += sr.getIdoRol() + ",";
		
		if(sr.getNameRol()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getNameRol().replaceAll("'", "''") + "',";
		
		if(sr.getArea() == null)
			stringvalue += "NULL";
		else
			stringvalue += "'" + sr.getArea().replaceAll("'", "''") + "'";
		return stringvalue;
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "NAME_ROL","IDO_ROL="+id);
	}
	
	public LinkedList<Object> getIDOByName(String name) throws SQLException {
		return i.getWhere(this, "IDO_ROL","NAME_ROL='"+name+"'");
	}
	
	public void deleteCond(String cond) throws SQLException{
		i.deleteCond(this,cond);
	}
	
	public String getParams(){
		return " (IDO_ROL, NAME_ROL, AREA) ";
	}
	
	public String getPK(){
		return "IDO_ROL";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Roles sr = new Roles();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setIdoRol(rs.getInt(i+1));
			else if (i == 1 && rs.getString(i+1)!=null)
				sr.setNameRol(rs.getString(i+1));
			else if (i == 2 && rs.getString(i+1)!=null)
				sr.setArea(rs.getString(i+1));
		}
		return sr;
	}
}
