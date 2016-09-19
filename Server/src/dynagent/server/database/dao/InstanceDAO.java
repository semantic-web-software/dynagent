/***
 * InstanceDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a DAO class for table 'instance'
 */

package dynagent.server.database.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Instance;
import dynagent.server.gestorsDB.GenerateSQL;

public class InstanceDAO extends ObjectDAO{
	
	public InstanceDAO(){
		super("instances",true);
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "name", "IDTO LIKE '"+id+"'");
	}
	
	public Instance getInstanceByName(String id) throws SQLException {
		
		LinkedList inst = this.getByName(id);
		int ident=0;
		Instance ins = new Instance();
		try {
			ident = (((BigDecimal)inst.getFirst()).toBigInteger()).intValue(); 
			
			inst = i.getAll(this," IDTO LIKE '"+ ident + "'",distinct);
			ins = (Instance)inst.getFirst();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return ins;
	}
	
	public LinkedList<Object> getInstancesByName(String id) throws SQLException {
		
		LinkedList<Object> inst = this.getByName(id);
		try {
			int ident = (((BigDecimal)inst.getFirst()).toBigInteger()).intValue(); 
			
			inst = i.getAll(this," IDTO LIKE '"+ ident + "'", distinct);
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return inst;
	}

	public LinkedList<Object> getByName(String n) throws SQLException {
		GenerateSQL generate = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		return i.getWhere(this, "IDTO", "name LIKE '"+n+"'" + generate.getCollateUpperCase());
	}

	public void insert(Instance in) throws SQLException{
		i.insert(this, in, false);
	}

	public void set(Instance in){
		i.set(this, "", in.toFactInstance());
	}
	
	public String getParams(){
		return " (IDTO, IDO, PROPERTY, QMAX, QMIN, VALUE, VALUECLS, name, OP, virtual) ";
	}
	
	public String getPK(){
		return "IDTO";
	}
	
	/*public String getNewPK(){
		String rmin = "0" ,rmax = "0";
		try{
			
			rmin = new Integer(Constants.RANGO2MIN).toString();
			rmax = new Integer(Constants.RANGO2MAX).toString();
		
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return this.getNewPK(rmin, rmax);
	}
	*/
	
	public String getNewPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(IDTO)", "IDTO >= "+rmin+ " AND IDTO <= "+ rmax);
		BigDecimal newido = ((BigDecimal)o.getFirst());
		BigInteger bn = newido.toBigInteger();
		Integer nido = bn.intValue();
		nido = nido + 1;
		return nido.toString();
	}
	
	public String getLastPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(IDTO)", "IDTO >= "+rmin+ " AND IDTO <= "+ rmax);
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
	
	public String getLastIDO(String rMin, String rMax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(IDO)", "IDO >= "+rMin+ " AND IDO <= "+ rMax);
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				BigDecimal newido = ((BigDecimal)o.getFirst());
				BigInteger bn = newido.toBigInteger();
				Integer nido = bn.intValue();
				result = nido.toString();
			}else
				result = rMin;
		}else
			result = rMin;
		return result;
	}
	
	
	public String getMaxPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(IDTO)", "1 = 1");
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
	
	public String getMaxIDO() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(IDO)", "1 = 1");
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
		LinkedList<Object> o = i.getWhere(this, "MAX(IDO)", "IDO <= "+cond);
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
	
	public String getValues(Object value){
		
		Instance ins = (Instance) value;
		
		String stringvalue = "";
	
		if(ins.getIDTO() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ins.getIDTO() + ",";	
		
		if(ins.getIDO() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ins.getIDO() + ",";
		
		if(ins.getPROPERTY() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ins.getPROPERTY() + ",";
		
		if(ins.getQMAX() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ins.getQMAX() + ",";

		if(ins.getQMIN() == null)
			stringvalue += "NULL,";
		else
			stringvalue += ins.getQMIN() + ",";

		if(ins.getVALUE() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ins.getVALUE()+ "',";
		
		if(ins.getVALUECLS() == null)
			stringvalue += "NULL,";
		else{
			stringvalue += ins.getVALUECLS() + ",";
		}

		if(ins.getNAME() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ins.getNAME()+ "',";

		if(ins.getOP() == null)
			stringvalue += "NULL,";
		else
			stringvalue += "'" + ins.getOP()+ "',";
		
		stringvalue += ins.isVIRTUAL();
		
		return stringvalue;
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Instance in = new Instance();
		for(int i = 0; i < rs.getMetaData().getColumnCount(); i++){
			if(i == 0)
				in.setIDTO(rs.getString(i+1));

			else if (i == 1)
				in.setIDO(rs.getString(i+1));
			else if (i == 2)
				in.setPROPERTY(rs.getString(i+1));
			else if (i == 3)
				in.setVALUE(rs.getString(i+1));
			else if (i == 4)
				in.setVALUECLS(rs.getString(i+1));
			else if (i == 5)
				in.setQMIN(rs.getString(i+1));
			else if (i == 6)
				in.setQMAX(rs.getString(i+1));
			else if (i == 7)
				in.setNAME(rs.getString(i+1));
			else if (i == 8)
				in.setOP(rs.getString(i+1));	
			else if (i==9)
				in.setVIRTUAL(Boolean.valueOf(rs.getString(i+1)));
		}
		return in;
	}
}
