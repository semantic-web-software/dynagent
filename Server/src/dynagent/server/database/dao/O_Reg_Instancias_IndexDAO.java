package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import dynagent.common.basicobjects.O_Reg_Instancias_Index;

public class O_Reg_Instancias_IndexDAO extends ObjectDAO{

	public O_Reg_Instancias_IndexDAO() {
		super("O_Reg_Instancias_Index", true);
	}

	public void insert(O_Reg_Instancias_Index in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.O_Reg_Instancias_Index in = new dynagent.common.basicobjects.O_Reg_Instancias_Index();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setAutonum(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setId_o(new Integer(rs.getString(i+1)));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setId_to(new Integer(rs.getString(i+1)));
			if(i == 3 && rs.getString(i+1)!=null)
				in.setRdn(rs.getString(i+1));
		}
		return in;
	}

	@Override
	public String getValues(Object v) {
		O_Reg_Instancias_Index ind = (O_Reg_Instancias_Index)v;
		
		String stringvalue = "";
	
		if(ind.getId_to() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ind.getId_to()+ ",";	
		if(ind.getRdn() == null)
			stringvalue += "NULL";
		else
			stringvalue += "'" + ind.getRdn().replaceAll("'", "''") + "'";
		
		return stringvalue;
	}
	
	public String getParams(){
		return " (ID_TO, RDN) ";
	}
	
	public String getPK(){
		return "AUTONUM";
	}
	

}
