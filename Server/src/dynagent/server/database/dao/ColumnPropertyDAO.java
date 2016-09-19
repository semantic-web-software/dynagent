package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.ColumnProperty;

public class ColumnPropertyDAO extends ObjectDAO{

	public ColumnPropertyDAO(){
		super("ColumnProperties",true);
	}
	
	public void insert(ColumnProperty in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		ColumnProperty cp= (ColumnProperty)v;
		
		String stringvalue = "";
		
		if(cp.getIdtoParentName()== null)
			stringvalue += "NULL,";
		else
			stringvalue +="'"+  cp.getIdtoParentName()+ "',";	
		
		if(cp.getIdtoName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cp.getIdtoName()+ "',";
		
		if(cp.getIdPropName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cp.getIdPropName() +"',";
		
		if(cp.getIdPropPath()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cp.getIdPropPath() +"',";
		
		if(cp.getPropFilter()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cp.getPropFilter()+"',";
		
		if(cp.getValueFilter()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + cp.getValueFilter()+"',";
		if(cp.getPriority()== null)
			stringvalue += "NULL";
		else
			stringvalue += cp.getPriority();
		
		
		return stringvalue;
		
	}
	
	public String getParams(){
		return " (ClassParent, Class, Prop, PropPath, PropFilter, ValueFilter, Priority) ";
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
