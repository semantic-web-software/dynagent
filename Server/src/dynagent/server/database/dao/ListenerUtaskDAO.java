package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.ListenerUtask;
import dynagent.server.gestorsDB.GenerateSQL;

public class ListenerUtaskDAO extends ObjectDAO{
	
	public ListenerUtaskDAO(){
		super("ListenerUtasks",true);
	}

	public void insert(ListenerUtask in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		ListenerUtask e= (ListenerUtask)v;
		
		String stringvalue = "";
	
		stringvalue += e.getUtask();
		
		return stringvalue;
		
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (UTASK) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}

