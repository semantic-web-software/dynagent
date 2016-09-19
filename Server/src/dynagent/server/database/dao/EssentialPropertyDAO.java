package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.EssentialProperty;
import dynagent.server.gestorsDB.GenerateSQL;

public class EssentialPropertyDAO extends ObjectDAO{
	
	public EssentialPropertyDAO(){
		super("EssentialProperties",true);
	}

	public void insert(EssentialProperty in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		EssentialProperty e= (EssentialProperty)v;
		
		String stringvalue = "";
	
		if(e.getUTask()== null)
			stringvalue += "NULL,";
		else
			stringvalue += e.getUTask()+ ",";
		
		if(e.getIdto() == null)
			stringvalue += "NULL,";
		else
			stringvalue += e.getIdto()+ ",";
		
		if(e.getProp()== null)
			stringvalue += "NULL,";
		else
			stringvalue += e.getProp();
		
		return stringvalue;
		
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (UTASK, CLASS, PROP) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}

