/***
 * PropertiesDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a DAO class for table 'properties'
 */

package dynagent.server.database.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import dynagent.common.basicobjects.Properties;
import dynagent.server.gestorsDB.GenerateSQL;

public class PropertiesDAO extends ObjectDAO{
	
	public static String TABLE_DATA_PROPERTIES="propiedad_dato";
	public static String TABLE_OBJECT_PROPERTIES="propiedad_objeto";
	
	public PropertiesDAO(){
		super("v_propiedad",true);
	}
	
	protected PropertiesDAO(String table,boolean distinct){
		super(table,distinct);
	}
	
	public LinkedList<Object> getByID(String id) throws SQLException {
		return i.getWhere(this, "*", "id="+id);
	}
	
	public Integer getIdPropByName(String name) throws SQLException {
		Integer nido = null;
		GenerateSQL generate = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		//BigDecimal newido = (BigDecimal) i.getWhere(this, "PROP", "NAME LIKE '"+name+"'",1).getFirst();
		LinkedList<Object> llo = i.getWhere(this, "id", "rdn = '"+name+"'" + generate.getCollateUpperCase(),1);
		if (llo.size()>0) {
			nido = (Integer)llo.getFirst();
		}
		return nido;
	}


	public void insert(Properties in) throws SQLException{
		String oldTable=getTable();
		if(in.getVALUECLS()!=null)
			setTable(TABLE_DATA_PROPERTIES);
		else setTable(TABLE_OBJECT_PROPERTIES);
		i.insert(this, in, false);
		setTable(oldTable);
	}
	
	public void set(Properties in) throws SQLException{
		String oldTable=getTable();
		if(in.getVALUECLS()!=null)
			setTable(TABLE_DATA_PROPERTIES);
		else setTable(TABLE_OBJECT_PROPERTIES);
		i.setProp(this, "", in);
		setTable(oldTable);
	}

	public void setCat(ObjectDAO o, String object, Properties value) {
	
		if(value.getPROP() != null){
			
		}
		/*	this.deleteCond(o, "PROP LIKE "+value.getPROP()+ " AND NAME LIKE '"+value.getNAME() + "'");
		else
			this.deleteCond(o, "NAME LIKE '"+value.getNAME() + "'");
			this.create(o, value, false);
			*/
			//update
	}
	
//	public void setCatProp(int idProp, int cat) throws SQLException {
//		
//		i.setPropCat(idProp, cat);
//	}
	
	public String getParams(){
		if(getTable().equals(TABLE_DATA_PROPERTIES))
			return " (id, rdn, cat, valuecls) ";
		else if(getTable().equals(TABLE_OBJECT_PROPERTIES))
			return " (id, rdn, cat, id_inversa) ";
		return " (id, rdn, cat, valuecls, id_inversa) ";
	}
	
	public String getPK(){
		return "id";
	}
	
	public Properties getPropertyByName(String ident) throws SQLException {
		Properties in = new Properties();
		try {	
			LinkedList<Object> inst = i.getAll(this," rdn = '"+ ident + "'", distinct);
			if(inst.size() > 0)
				in = (Properties)inst.getFirst();
			else
				in = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return in;	
	}
	
	public Properties getPropertyByID(int id) throws SQLException {
		Properties in = new Properties();
		try {	
			LinkedList<Object> inst = i.getAll(this," id="+ id, distinct);
			if(inst.size() > 0)
				in = (Properties)inst.getFirst();
			else
				in = null;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} 
		return in;	
	}
	
	public String getNewPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(id)", "1 = 1");
		Integer nido = ((Integer)o.getFirst());
		nido = nido + 1;
		return nido.toString();
	}
	
	public Integer getNewPKVirtual() throws NumberFormatException, SQLException{
		Integer id=Integer.parseInt(getLitterPK());
		if (id>0){
			id=0;
		}
		return id-1;
	}
	
	public String getLastPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(id)", "1 = 1");
		String result ="0";
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido = ((Integer)o.getFirst());
				result=nido.toString();
			}
		}	
		return result;
	}
	public String getLastPK(int  min) throws NumberFormatException, SQLException
	{
				int id= Integer.parseInt(getLastPK());
				if(id<=min)
					id=min;
				return String.valueOf(id);
	}
	public String getLitterPK() throws SQLException
	{
		LinkedList<Object> o = i.getWhere(this, "MIN(id)", "1 = 1");
		String result ="0";
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido = ((Integer)o.getFirst());
				result=nido.toString();
			}
		}	
		return result;
	}
	
	public String getValues(Object value){
		String stringvalue = "";
		
		Properties value2 = (Properties) value;
		
		if(value2.getPROP() == null)
			stringvalue += "NULL,";
		else
			stringvalue += value2.getPROP() + ",";
	
		if(value2.getNAME() == null)
			stringvalue += "NULL,";
		else			
			stringvalue += "'" + value2.getNAME()+ "',";
		
		if(value2.getCAT()==null)
			stringvalue += "NULL,";
		else
			stringvalue += "'"+value2.getCAT()+"',";
		
		if(getTable().equals(TABLE_DATA_PROPERTIES)){
			if(value2.getVALUECLS()==null)
				stringvalue += "NULL";
			else
				stringvalue += "'"+value2.getVALUECLS()+"'";
		}else if(getTable().equals(TABLE_OBJECT_PROPERTIES)){
			if(value2.getPROPINV()==null)
				stringvalue += "NULL";
			else
				stringvalue += "'"+value2.getPROPINV()+"'";
		}
		
		return stringvalue;
	}
	
	public String getValuesNotNull(Properties p){
		String s = "";
		if(p.getPROP()!=null)
			s+="id="+p.getPROP();
		if(p.getNAME()!=null) {
			if (s.length()>0)
				s+=",";
			s+="rdn='"+p.getNAME()+"'";
		}
		if(p.getCAT()!=null){
			if (s.length()>0)
				s+=",";
			s+="cat="+p.getCAT();
		}
		if(getTable().equals(TABLE_DATA_PROPERTIES)){
			if(p.getVALUECLS()!=null){
				if (s.length()>0)
					s+=",";
				s+="valuecls="+p.getVALUECLS();
			}
		}
		if(getTable().equals(TABLE_OBJECT_PROPERTIES)){
			if(p.getPROPINV()!=null){
				if (s.length()>0)
					s+=",";
				s+="id_inversa="+p.getPROPINV();
			}
		}
		
		return s;//s.substring(0, s.length()-1);
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		Properties in = new Properties();
		if(rs.getString("id")!=null)
			in.setPROP(new Integer(rs.getString("id")));
		in.setNAME(rs.getString("rdn"));
		if(rs.getString("cat")!=null)
			in.setCAT(new Integer(rs.getString("cat")));
		
		//TODO Si la vista v_properties tiene estas propiedades tambien no haria falta poner los if para dataProperty u objectProperty
		//if(getTable().equals(TABLE_DATA_PROPERTIES)){
			if(rs.getString("valuecls")!=null)
				in.setVALUECLS(new Integer(rs.getString("valuecls")));
		//}
		//if(getTable().equals(TABLE_OBJECT_PROPERTIES)){
			if(rs.getString("id_inversa")!=null)
				in.setPROPINV(new Integer(rs.getString("id_inversa")));
		//}
		in.setTableId(rs.getInt("tableId"));
		return in;
	}
	
	public void delete(Properties in) throws SQLException{
		String oldTable=getTable();
		if(in.getVALUECLS()!=null)
			setTable(TABLE_DATA_PROPERTIES);
		else setTable(TABLE_OBJECT_PROPERTIES);
		i.deleteProp(this,in.getPROP());
		setTable(oldTable);
	}
}
