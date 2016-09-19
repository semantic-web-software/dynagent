package dynagent.tools.importers.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.server.database.dao.ObjectDAO;

public class QueryDAO extends ObjectDAO{

	public QueryDAO(){
		super("S_Query",false);
	}
	
	public void insert(SQuery in) throws SQLException{
		in.setName(in.getName().replaceAll("'", "''"));
		in.setQuery(in.getQuery().replaceAll("'", "''"));
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		SQuery sr = (SQuery)v;
		
		String stringvalue = "";
	
		if(sr.getName() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getName()+ "',";	
		
		if(sr.getQuery() == null)
			stringvalue += "NULL";
		else
			stringvalue += "'" + sr.getQuery()+ "'";
		
		return stringvalue;
		
	}

	public String getParams(){
		return " (NAME, QUERY) ";
	}
	
	public String getPK(){
		return "NAME";
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
