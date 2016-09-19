package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.PrintSequence;
import dynagent.server.gestorsDB.GenerateSQL;

public class PrintSequenceDAO extends ObjectDAO{

	public PrintSequenceDAO(){
		super("Print_Sequence",false);
	}
	
	public void insert(PrintSequence in) throws SQLException{
		in.setOrder(in.getOrder().replaceAll("'", "''"));
		in.setSequence(in.getSequence().replaceAll("'", "''"));
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		PrintSequence sr= (PrintSequence)v;
		
		String stringvalue = "";
	
		if(sr.getOrder() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getOrder()+ "',";
		
		if(sr.getSequence()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getSequence()+ "',";
		
		if(sr.isPrePrint())
			stringvalue += "'Y'";
		else
			stringvalue += "'N'";
		
		return stringvalue;
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "SEQUENCE","ORDER LIKE '"+id+"'");
	}
	
	public PrintSequence getPrintSequenceBySecuence(String order) throws SQLException{
		PrintSequence sr=new PrintSequence();
		try{
			
			LinkedList<Object> lsr=i.getAll(this, "SEQUENCE LIKE '"+ order+ "'",distinct);
			if(lsr.size() > 0)
				sr = (PrintSequence)lsr.getFirst();
			else
				sr = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return sr;	
	}
	
	public void deleteCond(String cond) throws SQLException{
		i.deleteCond(this,cond);
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " ("+generateSQL.getCharacterBegin()  +"ORDER"+generateSQL.getCharacterEnd()+", SEQUENCE, PREPRINT) ";
	}
	
	public String getPK(){
		return "ORDER";
	}
	
	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		PrintSequence sr = new PrintSequence();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setOrder(rs.getString(i+1));
			else if (i == 1 && rs.getString(i+1)!=null)
				sr.setSequence(rs.getString(i+1));
			else if (i == 2 && rs.getString(i+1)!=null)
				sr.setPrePrint(rs.getBoolean(i+1));
		}
		return sr;
	}
}
