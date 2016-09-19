package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.SSubReport;

public class SSubReportDAO extends ObjectDAO{

	public SSubReportDAO(){
		super("S_SubReport",false);
	}
	
	public void insert(SSubReport in) throws SQLException{
		in.setSubreport(in.getSubreport().replaceAll("'", "''"));
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		SSubReport sr= (SSubReport )v;
		
		String stringvalue = "";
	
		if(sr.getIdto() == null)
			stringvalue += "NULL,";
		else
			stringvalue += sr.getIdto()+ ",";	
		
		if(sr.getId()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getId()+ "',";
		
		if(sr.getSubreport() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getSubreport()+ "'";
		
			
		return stringvalue;
		
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "SUBREPORT", "ID_TO LIKE '"+id+"'");
	}
	
	public LinkedList<Object> getAll() throws SQLException{
		// return i.getWhere(this, "*", "1 = 1");
		return i.getAll(this, "1 = 1", false);
	}
	
	public LinkedList<Object> getAllCond(String cond) throws SQLException{
		return i.getAll(this, cond, false);
	}
	
	public SSubReport getSSubReportBySubreport(String subreport) throws SQLException{
		SSubReport sr=new SSubReport();
		try{
			
			LinkedList<Object> lsr=i.getAll(this, "SUBREPORT LIKE '"+ subreport+ "'", distinct);
			if(lsr.size() > 0)
				sr = (SSubReport)lsr.getFirst();
			else
				sr = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return sr;	
	}
	
	
	public String getParams(){
		return " (ID_TO, ID, SUBREPORT) ";
	}
	
	public String getPK(){
		return "ID_TO";
	}
	
	public String getLitterPK() throws SQLException
	{
		LinkedList<Object> o = i.getWhere(this, "MIN(ID_TO)", "1 = 1");
		String result ="0";
		if(o.size() > 0){
			if(o.getFirst()!= null){
				
				Integer nido =(Integer)o.getFirst();
				result=nido.toString();
			}
		}	
		return result;
	}
	
	public String getNewPK() throws NumberFormatException, SQLException{
		Integer id=Integer.parseInt(getLitterPK());
		id--;
		return id.toString();
	}
	
	public String getLastPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_TO)", "ID_TO >= "+rmin+ " AND ID_TO <= "+ rmax);
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido =(Integer)o.getFirst();
				result = nido.toString();
			}else
				result = rmin;
		}else
			result = rmin;
		return result;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		SSubReport sr = new SSubReport();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setIdto(new Integer(rs.getString(i+1)));
			else if (i == 1 && rs.getString(i+1)!=null)
				sr.setId(rs.getString(i+1));
			else if (i == 2 && rs.getString(i+1)!=null)
				sr.setSubreport(rs.getString(i+1));
		}
		return sr;
	}
}
