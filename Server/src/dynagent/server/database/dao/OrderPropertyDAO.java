package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.OrderProperty;
import dynagent.server.gestorsDB.GenerateSQL;

public class OrderPropertyDAO extends ObjectDAO{

	public OrderPropertyDAO(){
		super("OrderProperties",true);
	}
	
	public void insert(OrderProperty in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		OrderProperty op= (OrderProperty)v;
		
		String stringvalue = "";
	
		stringvalue +=  op.getSec()+ ",";	
		
		stringvalue +=  op.getProp()+ ",";
		
		stringvalue +=  op.getOrder()+ ",";
		
		if(op.getGroup()== null)
			stringvalue += "NULL,";
		else
			stringvalue += op.getGroup()+ ",";
		
		if(op.getIdto()== null)
			stringvalue += "NULL";
		else
			stringvalue += op.getIdto();
		
		return stringvalue;
		
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (sec, prop,"+generateSQL.getCharacterBegin()  +"order"+generateSQL.getCharacterEnd()+ ","+generateSQL.getCharacterBegin()  +"group"+generateSQL.getCharacterEnd()+" , idto) ";
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
