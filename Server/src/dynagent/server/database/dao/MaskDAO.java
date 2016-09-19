package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.Mask;

public class MaskDAO extends ObjectDAO{
	
	public MaskDAO(){
		super("Masks",true);
	}

	public void insert(Mask in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		Mask m= (Mask)v;
		
		String stringvalue = "";
	
		if(m.getUTaskName()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + m.getUTaskName()+ "',";	
		
		if(m.getIdtoName() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + m.getIdtoName()+ "',";
		
		if(m.getPropName()== null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + m.getPropName()+ "',";
		
		if(m.getExpression()== null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + m.getExpression()+ "',";
		
		if(m.getLength()== null)
			stringvalue += "NULL";
		else
			stringvalue += m.getLength();
		
		return stringvalue;
		
	}
	
	public String getParams(){
		return " (UTASK, CLASS, PROP, EXPRESSION, LENGTH) ";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}