/**
 * AccessDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 */
package dynagent.server.database.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import dynagent.common.basicobjects.Access;
import dynagent.server.gestorsDB.GenerateSQL;

public class AccessDAO  extends ObjectDAO{
	
	public AccessDAO(){
		super("Access",true);
	}
	
	/* NO HAY COLUMNA IDACESS!
	public LinkedList<Object> getByID(String id) {
		return i.getWhere(this, "*", "IDACCESS = "+id);
	}
	*/
	

	public void insert(Access in) throws SQLException{
		//i.insertFactAccess(this, in.toFactAccess(), false);
		i.insert(this, in, false);
	}
	
	public void insertAccessIterator(Iterator <dynagent.common.basicobjects.Access> it) throws SQLException{
		while(it.hasNext()){
			Access acc = (Access) it.next();
			insert(acc);
		}
			
	}

	public void set(Access in){
		i.set(this, "", in);
	}
	
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return " (DENNIED, TASK, USERROL, " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + ", ACCESSTYPE, IDTO, IDO, PROP, VALUE, VALUECLS, PRIORITY) ";
	}
	
	public String getPK(){
		return "TASK";
	}
	
	/*
	public String getNewPK(){
		LinkedList<Object> o = i.getWhere(this, "MAX(IDACCESS)","1 = 1");
		BigDecimal newido = ((BigDecimal)o.getFirst());
		BigInteger bn = newido.toBigInteger();
		Integer nido = bn.intValue();
		nido = nido + 1;
		return nido.toString();
	}
	*/
	
	/*
	public String getLastPK(String rmin, String rmax){
		LinkedList<Object> o = i.getWhere(this, "MAX(IDACCESS)", "1 = 1");
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				BigDecimal newido = ((BigDecimal)o.getFirst());
				BigInteger bn = newido.toBigInteger();
				Integer nido = bn.intValue();
				result = nido.toString();
			}else
				result = rmin;
		}else
			result = rmin;
		return result;
	}
	*/
	
	public String getValues(Object value){
		String dev = ((Access)value).getDENNIED() + ","
			 + ((Access)value).getTASK() + ",";
//		if (((Access)value).getUSERROL()!=null)
//			dev += "'" + ((Access)value).getUSERROL().replaceAll("'", "''") + "',";
//		else
//			dev += "NULL,";
//		if (((Access)value).getUSER()!=null)
//			dev += "'" + ((Access)value).getUSER().replaceAll("'", "''") + "',";
//		else
//			dev += "NULL,";
//		dev += ((Access)value).getACCESSTYPE() + ","
//			 + ((Access)value).getIDTO() + ","
//			 + ((Access)value).getIDO() + ","
//			 + ((Access)value).getPROP() + ","
//			 + ((Access)value).getVALUE() + ","
//			 + ((Access)value).getVALUECLS()+","
//			 + ((Access)value).getPRIORITY();
		return dev;
	}

	public int getMaxPriority() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.Access ac = new dynagent.common.basicobjects.Access();
//		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
//			if(i == 0 && rs.getString(i+1)!=null)
//				ac.setDENNIED(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 1 && rs.getString(i+1)!=null)
//				ac.setTASK(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 2 && rs.getString(i+1)!=null)
//				ac.setUSERROL(rs.getString(i+1));
//			else if(i == 3 && rs.getString(i+1)!=null)
//				ac.setUSER((rs.getString(i+1)));
//			else if(i == 4 && rs.getString(i+1)!=null)
//				ac.setACCESSTYPE(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 5 && rs.getString(i+1)!=null)
//				ac.setIDTO(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 6 && rs.getString(i+1)!=null)
//				ac.setIDO(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 7 && rs.getString(i+1)!=null)
//				ac.setPROP(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 8 && rs.getString(i+1)!=null)
//				ac.setVALUE(rs.getString(i+1));
//			else if(i == 9 && rs.getString(i+1)!=null)
//				ac.setVALUECLS(new Integer(rs.getString(i+1)).intValue());
//			else if(i == 10 && rs.getString(i+1)!=null)
//				ac.setPRIORITY(new Integer(rs.getString(i+1)).intValue());
//		}
		return ac;
	}

}
