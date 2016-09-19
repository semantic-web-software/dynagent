package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Replica_Data;

public class Replica_DataDAO extends ObjectDAO{
	
	public Replica_DataDAO(){
		super("Replica_Data",true);
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getAll(this, "ID_O="+id, distinct);
		//return i.getWhere(this, "*", "ID_O="+id);
	}
	
	public void insert(Replica_Data in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public void update(String set, String where) throws SQLException{
		i.update(this, set, where);
	}
	
	public String getParams(){
		return " (ID, ACTION, ID_O, ID_TO, PROPERTY, Q_MAX, Q_MIN, VAL_NUM, OLD_VAL_NUM, VAL_TEXTO, VALUE_CLS, DESTINATION, DATE) ";
	}

	public String getPK(){
		return "ID_O";
	}
	
	public String getValues(Object value){
		
		String stringvalue = "";
		
		//FactODatosAttrib factv = (FactODatosAttrib)value;
		
		Replica_Data factv = (Replica_Data) value;
		
		stringvalue += factv.getId() + ",";
		stringvalue += "'" + factv.getAction() + "',";
		stringvalue += factv.getIDO() + ",";
		stringvalue += factv.getIDTO() + ",";
		
		if(factv.getPROPERTY() != null)
			stringvalue += factv.getPROPERTY() + ",";
		else
			stringvalue += "NULL ,";
		
		if(factv.getQMAX() != null)
			stringvalue += factv.getQMAX() + ",";
		else
			stringvalue += "NULL ,";
		
		if(factv.getQMIN() != null)
			stringvalue += factv.getQMIN() + ",";
		else
			stringvalue += "NULL ,";		
		if(factv.getVALNUM() != null)
			stringvalue += factv.getVALNUM() + ",";
		else
			stringvalue += "NULL ,";
		if(factv.getOLDVALNUM() != null)
			stringvalue += factv.getOLDVALNUM() + ",";
		else
			stringvalue += "NULL ,";
		if(factv.getVALTEXTO() != null) {
			stringvalue += "'" + factv.getVALTEXTO().replaceAll("'", "''") + "',";
		} else
			stringvalue += "NULL ,";
		
		if(factv.getVALUECLS() != null)
			stringvalue += factv.getVALUECLS() + ",";
		else
			stringvalue += "NULL ,";
		
		if(factv.getDESTINATION() != null)
			stringvalue += "'" + factv.getDESTINATION().replaceAll("'", "''") + "',";
		else
			stringvalue += "NULL ,";

		stringvalue += factv.getDate();
		
		return stringvalue;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.Replica_Data in = new dynagent.common.basicobjects.Replica_Data();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setId(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setAction(rs.getString(i+1));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setIDTO(new Integer(rs.getString(i+1)));
			if(i == 3 && rs.getString(i+1)!=null)
				in.setIDO(new Integer(rs.getString(i+1)));
			if(i == 4 && rs.getString(i+1)!=null)
				in.setPROPERTY(new Integer(rs.getString(i+1)));
			if(i == 5 && rs.getString(i+1)!=null){
				Double d=new Double(rs.getString(i+1));
				in.setVALNUM(d.intValue());
			}
			if(i == 6 && rs.getString(i+1)!=null){
				Double d=new Double(rs.getString(i+1));
				in.setOLDVALNUM(d.intValue());
			}
			if(i == 7 && rs.getString(i+1)!=null)
				in.setVALTEXTO(rs.getString(i+1));
			if(i == 8 && rs.getString(i+1)!=null)
				in.setVALUECLS(new Integer(rs.getString(i+1)));
			if(i == 9 && rs.getString(i+1)!=null)
				in.setQMIN(new Double(rs.getString(i+1)));
			if(i == 10 && rs.getString(i+1)!=null)
				in.setQMAX(new Double(rs.getString(i+1)));
			if(i == 11 && rs.getString(i+1)!=null)
				in.setDESTINATION(rs.getString(i+1));
			if(i == 12 && rs.getString(i+1)!=null)
				in.setDate(new Long(rs.getString(i+1)));
		}
		return in;
	}
}
