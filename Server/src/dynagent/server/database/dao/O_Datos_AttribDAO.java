/***
 * O_Datos_AttribDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a DAO class for table 'O_Datos_Attrib'
 */

package dynagent.server.database.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.O_Datos_Attrib;

public class O_Datos_AttribDAO extends ObjectDAO{
	
	public O_Datos_AttribDAO(){
		super("O_Datos_Atrib",true);
	}
	
	public O_Datos_AttribDAO(String name){
		super(name,true);
	}
	
	/*public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getAll(this, "ID_O="+id, distinct);
		//return i.getWhere(this, "*", "ID_O="+id);
	}*/
	
	public void insert(O_Datos_Attrib in) throws SQLException{
		i.insert(this, in, false);
	}
	
	public int update(String set, String where) throws SQLException{
		return i.update(this, set, where);
	}
	
	public void set(O_Datos_Attrib in){
		i.set(this, "", in.toFactInstance());
	}
	
	public String getParams(){
		return " (ID_O, ID_TO, PROPERTY, Q_MAX, Q_MIN, VAL_NUM, VAL_TEXTO, VALUE_CLS, DESTINATION) ";
	}

	public String getPK(){
		return "ID_TO";
	}
	
	/*public String getNewPK(){
		String rmin = "0" ,rmax = "0";
			rmin = new Integer(Constants.RANGO2MIN).toString();
			rmin = new Integer(Constants.RANGO2MAX).toString();
		
		return this.getNewPK(rmin, rmax);
	}*/
	
	public String getNewPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_TO)", "ID_TO >= "+rmin+ " AND ID_TO <= "+ rmax);
		BigDecimal newido = ((BigDecimal)o.getFirst());
		BigInteger bn = newido.toBigInteger();
		Integer nido = bn.intValue();
		nido = nido + 1;
		return nido.toString();
	}
	
	public String getLastPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_TO)", "ID_TO >= "+rmin+ " AND ID_TO <= "+ rmax);
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
	
	/*public String getLastIDO(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_O)", "ID_O >= "+rmin);
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido = (Integer)o.getFirst();
				result = nido.toString();
			}else
				result = rmin;
		}else
			result = rmin;
		return result;
	}*/
	
	
	public String getMaxPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_TO)", "1 = 1");
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				BigDecimal newido = ((BigDecimal)o.getFirst());
				BigInteger bn = newido.toBigInteger();
				Integer nido = bn.intValue();
				result = nido.toString();
			}
		}
		return result;
	}
	
	/*public String getMaxIDO() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_O)", "1 = 1");
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				BigDecimal newido = ((BigDecimal)o.getFirst());
				BigInteger bn = newido.toBigInteger();
				Integer nido = bn.intValue();
				result = nido.toString();
			}
		}
		return result;
	}
	
	public String getMaxIDOCond(String cond) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID_O)", "ID_O <= "+cond);
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				BigDecimal newido = ((BigDecimal)o.getFirst());
				BigInteger bn = newido.toBigInteger();
				Integer nido = bn.intValue();
				result = nido.toString();
			}
		}
		return result;
	}*/
	
	public String getValues(Object value){
		
		String stringvalue = "";
		
		//FactODatosAttrib factv = (FactODatosAttrib)value;
		
		O_Datos_Attrib factv = (O_Datos_Attrib) value;
		
	
		
		if(factv.getIDO() != null)
			stringvalue += factv.getIDO() + ",";
		else
			stringvalue += "NULL ,";
		
		if(factv.getIDTO() != null)
			stringvalue += factv.getIDTO() + ",";
		else
			stringvalue += "NULL ,";
		
		
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
		if(factv.getVALTEXTO() != null) {
			if (factv.isEncrypt())
				stringvalue += factv.getVALTEXTO() + ",";
			else
				stringvalue += "'" + factv.getVALTEXTO().replaceAll("'", "''") + "',";
		} else
			stringvalue += "NULL ,";
		
		if(factv.getVALUECLS() != null)
			stringvalue += factv.getVALUECLS() + ",";
		else
			stringvalue += "NULL ,";
		
		if(factv.getDESTINATION() != null)
			stringvalue += "'" + factv.getDESTINATION().replaceAll("'", "''") + "'";
		else
			stringvalue += "NULL";

		return stringvalue;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		dynagent.common.basicobjects.O_Datos_Attrib in = new dynagent.common.basicobjects.O_Datos_Attrib();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0 && rs.getString(i+1)!=null)
				in.setIDTO(new Integer(rs.getString(i+1)));
			if(i == 1 && rs.getString(i+1)!=null)
				in.setIDO(new Integer(rs.getString(i+1)));
			if(i == 2 && rs.getString(i+1)!=null)
				in.setPROPERTY(new Integer(rs.getString(i+1)));
			if(i == 3 && rs.getString(i+1)!=null){
				Double d=new Double(rs.getString(i+1));
				in.setVALNUM(d.intValue());
			}
			if(i == 4&&rs.getString(i+1)!=null)
				in.setVALTEXTO(rs.getString(i+1));
			if(i == 5 && rs.getString(i+1)!=null)
				in.setVALUECLS(new Integer(rs.getString(i+1)));
			if(i == 6 && rs.getString(i+1)!=null)
				in.setQMIN(new Double(rs.getString(i+1)));
			if(i == 7 && rs.getString(i+1)!=null)
				in.setQMAX(new Double(rs.getString(i+1)));
			if(i == 8 && rs.getString(i+1)!=null)
				in.setDESTINATION(rs.getString(i+1));

		}
		return in;
	}
}
