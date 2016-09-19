package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Usuarios;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;


public class UsuariosDAO extends ObjectDAO{

	public UsuariosDAO(){
		super("Usuarios",false);
	}
	
	public void insert(Usuarios in) throws SQLException{
		i.insert(this, in, false);
	}
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}

	public String getValues(Object v) {
		Usuarios sr= (Usuarios)v;
		
		String stringvalue = "";
	
		if(sr.getIdoUsuario()== null)
			stringvalue += "NULL,";
		else
			stringvalue += sr.getIdoUsuario() + ",";
		
		if(sr.getLogin()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getLogin().replaceAll("'", "''") + "',";
		
		if(sr.getPwd() == null)
			stringvalue += "NULL,";
		else {
			GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
			stringvalue += generateSQL.getEncryptFunction(InstanceService.keyEncrypt, sr.getPwd()) + ",";
//			stringvalue += "'" + sr.getPwd()+ "'";
		}
		if(sr.getNombre()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getNombre().replaceAll("'", "''") + "',";
		if(sr.getApellidos()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getApellidos().replaceAll("'", "''") + "',";
		if(sr.getOrganizacion()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getOrganizacion().replaceAll("'", "''") + "',";
		if(sr.getGrupo()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getGrupo().replaceAll("'", "''") + "',";
		if(sr.getMail()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getMail().replaceAll("'", "''") + "',";
		if(sr.getDominio()== null)
			stringvalue += "NULL";
		else
			stringvalue += "'" + sr.getDominio().replaceAll("'", "''") + "'";
		
		return stringvalue;
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhereEncrypt(this, "PWD","IDO_USUARIO="+id);
	}
	
	public void deleteCond(String cond) throws SQLException{
		i.deleteCond(this,cond);
	}
	
	public String getParams(){
		return " (IDO_USUARIO, LOGIN, PWD, NOMBRE, APELLIDOS, ORGANIZACION, GRUPO, MAIL, DOMINIO) ";
	}
	
	public String getPK(){
		return "IDO_USUARIO";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Usuarios sr = new Usuarios();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setIdoUsuario(rs.getInt(i+1));
			else if(i == 1 && rs.getString(i+1)!=null)
				sr.setLogin(rs.getString(i+1));
			else if (i == 2 && rs.getString(i+1)!=null)
				sr.setPwd(rs.getString(i+1));
			else if (i == 3 && rs.getString(i+1)!=null)
				sr.setNombre(rs.getString(i+1));
			else if (i == 4 && rs.getString(i+1)!=null)
				sr.setApellidos(rs.getString(i+1));
			else if (i == 5 && rs.getString(i+1)!=null)
				sr.setOrganizacion(rs.getString(i+1));
			else if (i == 6 && rs.getString(i+1)!=null)
				sr.setGrupo(rs.getString(i+1));
			else if (i == 7 && rs.getString(i+1)!=null)
				sr.setMail(rs.getString(i+1));
			else if (i == 8 && rs.getString(i+1)!=null)
				sr.setDominio(rs.getString(i+1));
		}
		return sr;
	}
}
