package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.NoModifyDB;

public class NoModifyDBDAO extends ObjectDAO{

	public NoModifyDBDAO() {
		super("NoModifyDB", true);
	}

	public void insert(NoModifyDB in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.NoModifyDB in = new dynagent.common.basicobjects.NoModifyDB();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setIdTo(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setActualPC(rs.getBoolean(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		NoModifyDB ind = (NoModifyDB)v;
		
		String stringvalue = "";
	
		if(ind.getIdTo() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdTo()+ ",";	
		if(ind.isActualPC() == false)
			stringvalue += "'N'";
		else
			stringvalue += "'Y'";
		return stringvalue;
	}
	
	public String getParams(){
		return " (ID_TO, ACTUAL_PC) ";
	}
	
	public String getPK(){
		return "ID_TO";
	}

}
