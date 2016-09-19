package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Groups;
import dynagent.server.gestorsDB.GenerateSQL;

public class GroupsDAO extends ObjectDAO{

	public GroupsDAO(){
		super("Groups",true);
	}
	
	public void insert(Groups in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		Groups gp= (Groups)v;
		
		String stringvalue = "";
		
		if(gp.getNameGroup()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" +gp.getNameGroup()+ "',";
		
		if(gp.getUTask()== null)
			stringvalue += "NULL,";
		else
			stringvalue += gp.getUTask()+ ",";	
		
		if(gp.getIdtoClass()== null)
			stringvalue += "NULL,";
		else
			stringvalue += gp.getIdtoClass()+ ",";
		
		if(gp.getIdProp()== null)
			stringvalue += "NULL";
		else
			stringvalue += gp.getIdProp();
		
		return stringvalue;
		
	}
	
	public String getPK(){
		return "ID";
	}
	
	public String getMaxPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID)", "1 = 1");
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido = (Integer)o.getFirst();
				result=nido.toString();
			}
		}
		return result;
	}
	
	public Integer getNewPKVirtual() throws NumberFormatException, SQLException{
		Integer id=Integer.parseInt(getLitterPK());
		if (id>0){
			id=0;
		}
		return id-1;
	}
	
	public String getLitterPK() throws SQLException
	{
		LinkedList<Object> o = i.getWhere(this, "MIN(ID)", "1 = 1");
		String result ="0";
		if(o.size() > 0){
			if(o.getFirst()!= null){
				
				Integer nido = (Integer)o.getFirst();
				result=nido.toString();
			}
		}	
		return result;
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (ID, UTASK, CLASS, NAME , PROP) ";
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Groups in = new Groups();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if (i == 1 && rs.getString(i+1)!=null)
				in.setNameGroup(rs.getString(i+1));
			else if(i == 2 && rs.getString(i+1)!=null)
				in.setUTask(new Integer(rs.getString(i+1)).intValue());
			else if (i == 3 && rs.getString(i+1)!=null)
				in.setIdtoClass(new Integer(rs.getString(i+1)).intValue());
			else if (i == 4 && rs.getString(i+1)!=null)
				in.setIdProp(new Integer(rs.getString(i+1)).intValue());
		}
		return in;
	}
}
