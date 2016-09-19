/***
 * TClaseDAO.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 * @description It represents a DAO class for table 'Clases'
 */

package dynagent.server.database.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;

import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.TPropiedadClase;
import dynagent.common.basicobjects.Usuarios;
import dynagent.server.gestorsDB.GenerateSQL;

public class TPropiedadClaseDAO extends ObjectDAO{ 
	public static String TABLE_DATA_PROPERTIES;
	public static String TABLE_OBJECT_PROPERTIES;
	
	public TPropiedadClaseDAO(){
		super("v_propiedad#clase",true);
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		setTable(generateSQL.getCharacterBegin() + "v_propiedad#clase" +generateSQL.getCharacterEnd());
		TABLE_DATA_PROPERTIES=generateSQL.getCharacterBegin() + "propiedad_dato#clase" + generateSQL.getCharacterEnd();
		TABLE_OBJECT_PROPERTIES=generateSQL.getCharacterBegin() + "propiedad_objeto#clase" + generateSQL.getCharacterEnd();
	}
	
	public LinkedList<Object> getByID(int idto) throws SQLException {
		return i.getWhere(this, "*", "ID="+idto);
	}
	
	public TClase getTClaseByName(String name) throws SQLException {	
		LinkedList<Object> Lclase = new LinkedList<Object>();
		TClase clase;
		GenerateSQL generate = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		Lclase = i.getAll(this," rdn ILIKE '" + name + "'", distinct);
		//Lclase = i.getAll(this," NAME LIKE '" + id + "'", distinct);
		if (Lclase.isEmpty()){
			clase=null;
		}else{
			clase= (TClase)Lclase.getFirst();
		}
		 
		return clase;
	}
	public TClase getTClaseById(int idto) throws SQLException {	
		LinkedList<Object> Lclase = new LinkedList<Object>();
		TClase clase;
		GenerateSQL generate = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		Lclase = i.getAll(this," ID=" + idto, distinct);
		//Lclase = i.getAll(this," NAME LIKE '" + id + "'", distinct);
		if (Lclase.isEmpty()){
			clase=null;
		}else{
			clase= (TClase)Lclase.getFirst();
		}
		 
		return clase;
	}
	
	public LinkedList<Object> getByName(String n) throws SQLException {
		return i.getWhere(this, "id", "rdn ILIKE '" + n + "'",1);
	}
	
	//(H)
	public HashMap<String, Integer> getAllHmIDClases() throws SQLException
	{
		HashMap<String , Integer> result = new HashMap<String, Integer>();
		LinkedList res1 = this.getAll();
		for(int i = 0 ; i < res1.size();i++)
		{
			TClase tclase=(TClase)res1.get(i);
			result.put(tclase.getName(), tclase.getIDTO());
		}
		return result;
	}
	//(H)
	public HashMap<Integer, String> getAllHmNameClases() throws SQLException
	{
		HashMap<Integer , String> result = new HashMap<Integer, String>();
		LinkedList res1 = this.getAll();
		for(int i = 0 ; i < res1.size();i++)
		{
			TClase tclase=(TClase)res1.get(i);
			result.put(tclase.getIDTO(), tclase.getName());
		}				
		return result;
	}
	
	public void insert(TPropiedadClase in) throws SQLException{
		String oldTable=getTable();
		if(in.isPropiedadDato())
			setTable(TABLE_DATA_PROPERTIES);
		else setTable(TABLE_OBJECT_PROPERTIES);
		i.insert(this, in, false);
		setTable(oldTable);
	}
	
	public void set(TClase clase) throws SQLException{
		//i.set(this, "", clase.toFact());
		i.setClass(this, "", clase);
	}
	
	public String getParams(){
		GenerateSQL generateSQL = new GenerateSQL(DAOManager.getInstance().getFactConnDB().getGestorDB());
		if(getTable().equals(TABLE_DATA_PROPERTIES))
			return " (" + generateSQL.getCharacterBegin() + "claseId" + generateSQL.getCharacterEnd() + ", " + generateSQL.getCharacterBegin() + "propiedad_datoId" + generateSQL.getCharacterEnd() + ") ";
		else if(getTable().equals(TABLE_OBJECT_PROPERTIES))
			return " (" + generateSQL.getCharacterBegin() + "claseId" + generateSQL.getCharacterEnd() + ", " + generateSQL.getCharacterBegin() + "propiedad_objetoId" + generateSQL.getCharacterEnd() + ") ";
		return " (" + generateSQL.getCharacterBegin() + "claseId" + generateSQL.getCharacterEnd() + ", " + generateSQL.getCharacterBegin() + "propiedadId" + generateSQL.getCharacterEnd() + ") ";
	}
	
	public String getPK(){
		return "id";
	}
	
	/*
	public String getNewPK(){
		String rmin = "0" ,rmax = "0";
		try{
			
			rmin = new Integer(Constants.RANGO2MIN).toString();
			rmax = new Integer(Constants.RANGO2MAX).toString();
		
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return this.getNewPK(rmin, rmax);
	}*/
	
	public String getNewPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID)", "ID >= "+rmin+ " AND ID <= "+ rmax);
		BigDecimal newido = ((BigDecimal)o.getFirst());
		BigInteger bn = newido.toBigInteger();
		Integer nido = bn.intValue();
		nido = nido + 1;
		return nido.toString();
	}
	
	public String getLastPK(String rmin, String rmax) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID)", "ID >= "+rmin+ " AND ID <= "+ rmax);
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
	}
	public Integer getNewPKVirtual() throws NumberFormatException, SQLException{
		Integer id=Integer.parseInt(getLitterPK());
		if (id>0){
			id=0;
		}
		return id-1;
	}
	
	public String getLitterPK() throws SQLException
	{
		LinkedList<Object> o = i.getWhere(this, "MIN(ID)", "1 = 1");
		String result ="0";
		if(o.size() > 0){
			if(o.getFirst()!= null){
				
				Integer nido = (Integer)o.getFirst();
				result=nido.toString();
			}
		}	
		return result;
	}
	
	public String getMaxPK() throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID)", "1 = 1");
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				Integer nido = (Integer)o.getFirst();
				result=nido.toString();
			}
		}
		return result;
	}
	
	public String getMinIDTOCond(String cond) throws SQLException{
		LinkedList<Object> o = i.getWhere(this, "MAX(ID)", "ID >= "+cond);
		String result = null;
		if(o.size() > 0){
			if(o.getFirst()!= null){
				result = o.getFirst().toString();
			}else
				result = cond;
		}
		return result;
	}
	
	public String getValues(Object value){
		return ((TPropiedadClase) value).getClaseId() + "," + ((TPropiedadClase) value).getPropiedadId();
	}

	@Override
	public Object buildObject(ResultSet rs) throws NumberFormatException, SQLException {
		TPropiedadClase in = new TPropiedadClase();
		in.setPropiedadId(rs.getInt("propiedad_datoId"));
		if(rs.wasNull()){
			in.setPropiedadId(rs.getInt("propiedad_objetoId"));
			in.setPropiedadDato(false);
		}else{
			in.setPropiedadDato(true);
		}
		in.setClaseId(rs.getInt("claseId"));
		in.setTableId(rs.getInt("tableId"));
		return in;
	}

	@Override
	public void deleteAll() throws SQLException {
		String oldTable=getTable();
		setTable(TABLE_DATA_PROPERTIES);
		super.deleteAll();
		setTable(TABLE_OBJECT_PROPERTIES);
		super.deleteAll();
		setTable(oldTable);
	}
	
	
}
