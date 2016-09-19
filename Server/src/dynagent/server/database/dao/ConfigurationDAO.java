package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Configuration;
import dynagent.server.gestorsDB.GenerateSQL;

public class ConfigurationDAO extends ObjectDAO{

	public ConfigurationDAO() {
		super("Configuration", true);
	}

	public void insert(Configuration in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	public LinkedList<Object> getByID(String id) throws SQLException {
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return i.getWhere(this, "*", generateSQL.getCharacterBegin() + "LABEL" + generateSQL.getCharacterEnd() + " LIKE '"+id+"'",2);
	}
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.Configuration in = new dynagent.common.basicobjects.Configuration();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setLabel(rs.getString(i+1));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setValue(rs.getString(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		Configuration ind = (Configuration)v;
		
		String stringvalue = "";
	
		if(ind.getLabel() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ind.getLabel().replaceAll("'", "''") + "',";	
		if(ind.getValue() == null)
			stringvalue += "NULL";
		else
			stringvalue += "'" + ind.getValue().replaceAll("'", "''") + "'";
		return stringvalue;
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (" + generateSQL.getCharacterBegin() + "LABEL" + generateSQL.getCharacterEnd() + "," + 
			generateSQL.getCharacterBegin() + "VALUE" + generateSQL.getCharacterEnd() + ") ";
	}
	
	public String getPK(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return generateSQL.getCharacterBegin() + "LABEL" + generateSQL.getCharacterEnd();
	}

}
