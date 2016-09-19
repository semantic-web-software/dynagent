package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.Alias;
import dynagent.server.gestorsDB.GenerateSQL;

public class AliasDAO extends ObjectDAO{
	
	public AliasDAO(){
		super("Alias",true);
	}

	public void insert(Alias in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		Alias a= (Alias)v;
		
		String stringvalue = "";
	
		if(a.getUTaskName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + a.getUTaskName()+ "',";	
		
		if(a.getIdtoName() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + a.getIdtoName()+ "',";
		
		if(a.getPropName()== null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + a.getPropName()+ "',";
		
		if(a.getAlias()== null)
			stringvalue += "NULL";
		else
			stringvalue +="'" + a.getAlias()+ "'";
		
		return stringvalue;
		
	}
	
	public String getParams(){
		return " (UTASK, CLASS, PROP, ALIAS) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}

