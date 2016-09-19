package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.SReport;
import dynagent.common.utils.Auxiliar;
import dynagent.server.gestorsDB.GenerateSQL;

public class SReportDAO extends ObjectDAO{

	public SReportDAO(){
		super("S_Report",false);
	}
	
	public void insert(SReport in) throws SQLException{
		in.setJrxml(in.getJrxml().replaceAll("'", "''"));
		in.setMap(in.getMap().replaceAll("'", "''"));
		in.setQuery(in.getQuery().replaceAll("'", "''"));
		if (in.getPrintOrder()!=null)
			in.setPrintOrder(in.getPrintOrder().replaceAll("'", "''"));
		i.insert(this, in, false);
	}
	
	public String getValues(Object v) {
		SReport sr= (SReport)v;
		
		String stringvalue = "";
	
		if(sr.getIdto() == null)
			stringvalue += "NULL,";
		else
			stringvalue += sr.getIdto()+ ",";	
		
		if(sr.getId()== null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getId()+ "',";
		
		if(sr.getQuery() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + sr.getQuery()+ "',";
		
		if(sr.getJrxml() == null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + sr.getJrxml()+ "',";
		
		if(sr.getMap() == null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + sr.getMap()+ "',";
		if(sr.getGroup() == null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + sr.getGroup()+ "',";
		if(!sr.getDirectImpresion())
			stringvalue += "'N',";
		else
			stringvalue += "'Y',";
		if(!sr.isPreView())
			stringvalue += "'N',";
		else
			stringvalue += "'Y',";
		stringvalue += sr.getNCopies()+ ",";
		if(!sr.getDisplayPrintDialog())
			stringvalue += "'N',";
		else
			stringvalue += "'Y',";
		if(sr.getFormatList() == null)
			stringvalue += "NULL,";
		else
			stringvalue +="'" + Auxiliar.arrayToString(sr.getFormatList(), ";") + "',";
		if(sr.getPrintOrder() == null)
			stringvalue += "NULL";
		else
			stringvalue +="'" + sr.getPrintOrder()+ "'";
		
		return stringvalue;
		
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "QUERY","ID_TO LIKE '"+id+"'");
	}
	
	public SReport getSReportByQuery(String query) throws SQLException{
		SReport sr=new SReport();
		try{
			
			LinkedList<Object> lsr=i.getAll(this, "QUERY LIKE '"+ query+ "'",distinct);
			if(lsr.size() > 0)
				sr = (SReport)lsr.getFirst();
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
	public SReport getSReportByIDTO(String idto) throws SQLException{
		SReport sr=new SReport();
		try{
			
			LinkedList<Object> lsr=i.getAll(this, "ID_TO LIKE '"+ idto+ "'",distinct);
			if(lsr.size() > 0)
				sr = (SReport)lsr.getFirst();
			else
				sr = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return sr;	
	}
	public SReport getSReportByReport(String report) throws SQLException{
		SReport sr=new SReport();
		try{
			
			LinkedList<Object> lsr=i.getAll(this, "REPORT LIKE '"+ report+ "'", distinct);
			if(lsr.size() > 0)
				sr = (SReport)lsr.getFirst();
			else
				sr = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return sr;	
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (ID_TO, ID, QUERY, REPORT, MAP, "+generateSQL.getCharacterBegin()  +"GROUP"+generateSQL.getCharacterEnd()+", DIRECT_IMPRESION, PREVIEW, N_COPIES, DISPLAY_PRINT_DIALOG, FORMAT, PRINT_ORDER) ";
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
		SReport sr = new SReport();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				sr.setIdto(new Integer(rs.getString(i+1)));
			else if (i == 1 && rs.getString(i+1)!=null)
				sr.setId(rs.getString(i+1));
			else if (i == 2 && rs.getString(i+1)!=null)
				sr.setQuery(rs.getString(i+1));
			else if (i == 3 && rs.getString(i+1)!=null)
				sr.setJrxml(rs.getString(i+1));
			else if (i == 4 && rs.getString(i+1)!=null)
				sr.setMap(rs.getString(i+1));
			else if (i == 6 && rs.getString(i+1)!=null)
				sr.setDirectImpresion(rs.getBoolean(i+1));
			else if (i == 7 && rs.getString(i+1)!=null)
				sr.setPreView(rs.getBoolean(i+1));
			else if (i == 8 && rs.getString(i+1)!=null)
				sr.setNCopies(new Integer(rs.getString(i+1)));
			else if (i == 9 && rs.getString(i+1)!=null)
				sr.setDisplayPrintDialog(rs.getBoolean(i+1));
			else if (i == 10 && rs.getString(i+1)!=null)
				sr.setFormatList(Auxiliar.stringToArray(rs.getString(i+1), ";"));
			else if (i == 11 && rs.getString(i+1)!=null)
				sr.setPrintOrder(rs.getString(i+1));
		}
		return sr;
	}
}
