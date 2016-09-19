package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.GlobalClases;

public class GlobalClasesDAO extends ObjectDAO{

	public GlobalClasesDAO() {
		super("GlobalClases", true);
	}

	public void insert(GlobalClases in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.GlobalClases in = new dynagent.common.basicobjects.GlobalClases();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setUserTask(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setIdtoRoot(rs.getInt(i+1));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setCentralized(rs.getBoolean(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		GlobalClases ind = (GlobalClases)v;
		
		String stringvalue = "";
		stringvalue += ind.getUserTask() + ",";	
		if(ind.getIdtoRoot() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getIdtoRoot() + ",";
		if(ind.isCentralized() == false)
			stringvalue += "'N'";
		else
			stringvalue += "'Y'";
		return stringvalue;
	}
	
	public String getParams(){
		return " (USER_TASK, IDTO_ROOT, CENTRALIZED) ";
	}
	
	public String getPK(){
		return "USER_TASK";
	}

}
