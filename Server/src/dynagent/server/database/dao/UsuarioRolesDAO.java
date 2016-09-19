package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.UsuarioRoles;


public class UsuarioRolesDAO extends ObjectDAO{

	public UsuarioRolesDAO(){
		super("UsuarioRoles",false);
	}
	
	public void insert(UsuarioRoles in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		UsuarioRoles sr= (UsuarioRoles)v;
		
		String stringvalue = "";
	
		if(sr.getIdoUsuario()== null)
			stringvalue += "NULL,";
		else
			stringvalue += sr.getIdoUsuario() + ",";
		
		if(sr.getIdoRol() == null)
			stringvalue += "NULL";
		else
			stringvalue += sr.getIdoRol();
		return stringvalue;
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "ROL","USUARIO = "+id);
	}
	
	public void deleteCond(String cond) throws SQLException{
		i.deleteCond(this,cond);
	}
	
	public String getParams(){
		return " (USUARIO, ROL) ";
	}
	
	public String getPK(){
		return "USUARIO";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		UsuarioRoles sr = new UsuarioRoles();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setIdoUsuario(rs.getInt(i+1));
			else if (i == 1 && rs.getString(i+1)!=null)
				sr.setIdoRol(rs.getInt(i+1));
		}
		return sr;
	}
}
