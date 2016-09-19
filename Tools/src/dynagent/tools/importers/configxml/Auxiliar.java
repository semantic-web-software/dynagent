package dynagent.tools.importers.configxml;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.AreaFunc;
import dynagent.common.basicobjects.Cardinality;
import dynagent.common.basicobjects.Class;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.IndexName;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.O_Datos_Attrib;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.PropertyForClass;
import dynagent.common.basicobjects.Range;
import dynagent.common.basicobjects.TClase;
import dynagent.common.basicobjects.T_Herencias;
import dynagent.common.basicobjects.UTask;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.QueryConstants;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.database.dao.GroupsDAO;
import dynagent.server.database.dao.IDAO;
import dynagent.server.database.dao.InstanceDAO;
import dynagent.server.database.dao.ListenerUtaskDAO;
import dynagent.server.database.dao.O_Datos_AttribDAO;
import dynagent.server.database.dao.PropertiesDAO;
import dynagent.server.database.dao.RolesDAO;
import dynagent.server.database.dao.TClaseDAO;
import dynagent.server.database.dao.T_HerenciasDAO;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;


public class Auxiliar {
	public static Integer getIdtoClass(String className,FactoryConnectionDB fcdb) throws NamingException, SQLException{
		//String sql = "SELECT IDTO FROM Clases WHERE NAME LIKE '"+className+"'";
		String sql = "SELECT IDTO FROM instances WHERE NAME iLIKE '"+className+"'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static Integer getIdtoClassFromTableClass(String className,FactoryConnectionDB fcdb) throws NamingException, SQLException{
		String sql = "SELECT id FROM clase WHERE rdn iLIKE '"+className+"'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	//Devolvemos una lista ya que puede haber individuos de distintas clases con el mismo rdn
	//Comentado ya que es muy ineficiente debido al cambio de los idtos y no deberia usarse
//	public static ArrayList<Integer> getIdos(String rdn, FactoryConnectionDB fcdb) throws NamingException, SQLException {
//		//antes buscaba en O_Reg_Instancias
//		String sql = "SELECT Id_O FROM O_Datos_Atrib WHERE VAL_TEXTO LIKE '"+rdn+"' AND PROPERTY=" + Constants.IdPROP_RDN;
//		Statement st = null;
//		ResultSet rs = null;
//		ConnectionDB con = null;
//		ArrayList<Integer> list=new ArrayList<Integer>();
//		try{
//			
//			con = fcdb.createConnection(true);
//			st = con.getBusinessConn().createStatement();
//			rs = st.executeQuery(sql);
//			while (rs.next()) {
//				list.add(rs.getInt(1));
//			}
//			
//			return list;
//		}finally{
//			if(rs!=null)
//				rs.close();
//			if(st!=null)
//				st.close();
//			//if(con!=null)
//				//con.close();
//		}
//	}
	
	/*Comentado para que no se use ya que es muy poco eficiente esta consulta a base de datos
	 public static Integer getIdto(int ido, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "SELECT Id_To FROM O_Datos_Atrib WHERE Id_O="+ido;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}*/
	
	public static Integer getIdProp(String propName, FactoryConnectionDB fcdb) throws NamingException, SQLException{
		String sql = "SELECT id FROM v_propiedad WHERE rdn LIKE '"+propName+"'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static Integer getValueClsProp(String propName, FactoryConnectionDB fcdb) throws NamingException, SQLException{
		String sql = "SELECT VALUECLS FROM propiedad_dato WHERE rdn LIKE '"+propName+"'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static Integer getIdProp(String propName, Integer idtoClass, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		Integer idProp=getIdProp(propName, fcdb);
		if (idProp!=null){
			if (idtoClass==null){
				return idProp;
			}
			String sql = "SELECT * FROM instances WHERE PROPERTY ="+idProp+" AND IDTO="+idtoClass;
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = null;
			try{
				
				con = fcdb.createConnection(true);
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					return idProp;					
				}else{
					return null;
					
				}
			}finally{
				if(rs!=null)
					rs.close();
				if(st!=null)
					st.close();
				//if(con!=null)
					//con.close();
			}
		}else{
			return null;
		}
	}
	
	public static Integer getIdo(Integer idto, String rdn, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws NamingException, SQLException {
		//antes buscaba en O_Reg_Instancias
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		
		String sql = "SELECT "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" FROM "+gSQL.getCharacterBegin()+dataBaseMap.getTable(idto).getName()+gSQL.getCharacterEnd()+" WHERE rdn ILIKE '"+rdn+"'";

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return QueryConstants.getIdo(rs.getInt(1),idto);				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static ArrayList<Integer> getIdos(Integer idto, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws NamingException, SQLException {
		//antes buscaba en O_Reg_Instancias
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		
		String sql = "SELECT "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" FROM "+gSQL.getCharacterBegin()+dataBaseMap.getTable(idto).getName()+gSQL.getCharacterEnd();

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		ArrayList<Integer> list=new ArrayList<Integer>();
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while(rs.next()) {
				list.add(QueryConstants.getIdo(rs.getInt(1),idto));	
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
		return list;
	}
	
	public static Integer getIdoOfInstances(Integer idto, String rdn, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql = "SELECT ido FROM instances WHERE value LIKE '"+rdn+"' AND idto="+idto+" AND property=" + Constants.IdPROP_RDN;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1);
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static boolean containsProp(int prop, Integer idtoClass,FactoryConnectionDB fcdb) throws NamingException, SQLException {
		
		String sql = "SELECT * FROM instances WHERE PROPERTY ="+prop+" AND IDTO="+idtoClass;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return true;					
			}else{
				return false;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
	}
	public static LinkedList<Integer> getAllUTask(Integer ido_area_func, FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String sql = "SELECT IDTO FROM instances WHERE VALUE LIKE '"+ido_area_func+"' GROUP BY IDTO";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		LinkedList<Integer> result=new LinkedList<Integer>();
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				result.add(rs.getInt(1));				
			}
			return result;
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	public static void deleteUtask(Integer ut, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(false);
			st = con.getBusinessConn().createStatement();
			
			String sql = "DELETE FROM instances WHERE IDTO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM Clase WHERE ID="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM O_Reg_Instancias_Index WHERE Id_TO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM O_Datos_Atrib_Memo WHERE ID_TO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM O_Datos_Atrib WHERE ID_TO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM OrderProperties WHERE IDTO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM T_Herencias WHERE ID_TO="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM T_Herencias WHERE ID_TO_Padre="+ut;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM Access WHERE IDTO="+ut;
			
			st.executeUpdate(sql);
			
			con.getBusinessConn().commit();
			
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
	}
	
	public static void changePropertyForClass(PropertyForClass p, FactoryConnectionDB fcdb) throws Exception {
		if(p.isExcluded())
			deleteProperty(p,fcdb);
		//else insertPropertyToClass();
		
	}
	
	public static void deleteProperty(PropertyForClass p, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(false);
			st = con.getBusinessConn().createStatement();
			
			String sqlIdtoClass=p.getIdtoClass()!=null?" AND IDTO="+p.getIdtoClass():"";
			String sqlClassName=p.getClassName()!=null?" AND CLASS LIKE '"+p.getClassName()+"'":"";
			
			String sql = "DELETE FROM Access WHERE PROP="+p.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Alias WHERE PROP LIKE '"+p.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM ColumnProperties WHERE Prop LIKE '"+p.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Groups WHERE PROP LIKE '"+p.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM OrderProperties WHERE PROP="+p.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM instances WHERE PROPERTY="+p.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			String sqlId_toClass=p.getIdtoClass()!=null?" AND ID_TO="+p.getIdtoClass():" AND ID_TO IN (SELECT IDTO FROM INSTANCES WHERE PROPERTY="+p.getIdProp()+")";
			
			sql = "DELETE FROM O_Datos_Atrib WHERE PROPERTY="+p.getIdProp()+sqlId_toClass;
			
			st.executeUpdate(sql);
			
			String sqlClass=p.getIdtoClass()!=null?" AND CLASS="+p.getIdtoClass():"";
			
			sql="DELETE FROM EssentialProperties WHERE PROP="+p.getIdProp()+sqlClass;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM Masks WHERE PROP LIKE '"+p.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			con.getBusinessConn().commit();
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
	}
	
	public static void updateCardinality(Cardinality c,FactoryConnectionDB fcdb) throws Exception {
		if(c.getCardMax()!=null && c.getCardMax().equals(0) && c.getCardMin()!=null && c.getCardMin().equals(0)){
			throw new Exception("Cardinalidad 0 no soportada en "+c);
			//deletePropertyCard00(c,fcdb);
			//changeCardinality(c,fcdb);
		}else{
			changeCardinality(c,fcdb);
		}
		
	}
	
	public static void changeCardinality(Cardinality c, FactoryConnectionDB fcdb) throws Exception {
		boolean b=existCardinality(c,fcdb);
		if (b){
			updateCardinalityDB(c,fcdb);
		}else{
			insertCardinalityDB(c,fcdb);
		}
		
	}
	public static void insertCardinalityDB(Cardinality c, FactoryConnectionDB fcdb) throws Exception {
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.open();
		LinkedList li;
		if(c.getIdtoClass()!=null){
			li=iDAO.getAllCond(" PROPERTY ="+c.getIdProp()+" AND IDTO="+c.getIdtoClass());	
		}else{
			li=iDAO.getAllCond(" PROPERTY ="+c.getIdProp());
		}
		
		iDAO.close();
		Instance i=null;
		if (li.isEmpty()){
			System.err.println("Error: No existe el instance para: property="+c.getIdProp()+" idto="+c.getIdtoClass());
			throw new Exception();
		}else{
			i=(Instance) li.getFirst();
		}
		Statement st = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			String sql = "INSERT INTO instances (IDTO,IDO,PROPERTY, VALUE,VALUECLS,QMIN,QMAX,name,OP) VALUES" +
					" ("+i.getIDTO()+","+i.getIDO()+","+i.getPROPERTY()+","+i.getVALUE()+","+i.getVALUECLS()+","+c.getCardMin()+","+c.getCardMax()+",'"+i.getNAME()+"','CAR')";
			System.out.println(sql);
			st.executeUpdate(sql);
			
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	public static void updateCardinalityDB(Cardinality c, FactoryConnectionDB fcdb) throws Exception{
		
		Statement st = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			String sql;
			if(c.getIdtoClass()!=null){
				/*if(c.getCardMax().equals(0) && c.getCardMin().equals(0))
					sql = "DELETE FROM instances WHERE PROPERTY ="+c.getIdProp()+" AND IDTO="+c.getIdtoClass();
				else*/
					sql = "UPDATE instances SET QMIN="+c.getCardMin()+",QMAX="+c.getCardMax()+" WHERE PROPERTY ="+c.getIdProp()+" AND IDTO="+c.getIdtoClass()+" AND OP LIKE 'CAR'";
			}else{
				/*if(c.getCardMax().equals(0) && c.getCardMin().equals(0))
					sql = "DELETE FROM instances WHERE PROPERTY ="+c.getIdProp();
				else*/
					sql = "UPDATE instances SET QMIN="+c.getCardMin()+",QMAX="+c.getCardMax()+" WHERE PROPERTY ="+c.getIdProp()+" AND OP LIKE 'CAR'";
			}
			
			st.executeUpdate(sql);

		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	public static boolean existCardinality(Cardinality c, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		String sql;
		if(c.getIdtoClass()!=null){
			sql = "SELECT * FROM instances WHERE PROPERTY ="+c.getIdProp()+" AND IDTO="+c.getIdtoClass()+" AND OP LIKE 'CAR'";
		}else{
			sql = "SELECT * FROM instances WHERE PROPERTY ="+c.getIdProp()+" AND OP LIKE 'CAR'";
		}
		Statement st = null; 
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return true;					
			}else{
				return false;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	public static void deletePropertyCard00(Cardinality c, FactoryConnectionDB fcdb) throws NamingException, SQLException {
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(false);
			st = con.getBusinessConn().createStatement();
			
			String sqlIdtoClass=c.getIdtoClass()!=null?" AND IDTO="+c.getIdtoClass():"";
			String sqlClassName=c.getClassName()!=null?" AND CLASS LIKE '"+c.getClassName()+"'":"";
			
			String sql = "DELETE FROM Access WHERE PROP="+c.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Alias WHERE PROP LIKE '"+c.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM ColumnProperties WHERE Prop LIKE '"+c.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Groups WHERE PROP LIKE '"+c.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM OrderProperties WHERE PROP="+c.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM instances WHERE PROP="+c.getIdProp()+sqlIdtoClass;
			
			st.executeUpdate(sql);
			
			String sqlId_toClass=c.getIdtoClass()!=null?" AND ID_TO="+c.getIdtoClass():" AND ID_TO IN (SELECT IDTO FROM INSTANCES WHERE PROPERTY="+c.getIdProp()+")";
			
			sql = "DELETE FROM O_Datos_Atrib WHERE PROP="+c.getIdProp()+sqlId_toClass;
			
			st.executeUpdate(sql);
			
			String sqlClass=c.getIdtoClass()!=null?" AND CLASS="+c.getIdtoClass():"";
			
			sql="DELETE FROM EssentialProperties WHERE PROP="+c.getIdProp()+sqlClass;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM Masks WHERE PROP LIKE '"+c.getProp()+"'"+sqlClassName;
			
			st.executeUpdate(sql);
			
			con.getBusinessConn().commit();
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
	}
	public static void createUTask(UTask ut,HashMap<String,Integer> hRoles,FactoryConnectionDB fcdb) throws SQLException, NamingException {

		InstanceDAO iDAO = new InstanceDAO();
		LinkedList<Instance> litosave=new LinkedList<Instance>();
		iDAO.open();
		
		Integer idtoUtask=getIdtoClassFromTableClass(ut.getUtaskName(), fcdb);
		if(idtoUtask==null){
			TClaseDAO cDAO=new TClaseDAO();
			idtoUtask=new Integer(cDAO.getLastPK(String.valueOf(Constants.MIN_ID_NO_SPECIALCLASS), String.valueOf(Constants.MAX_ID_CLASS)).toString())+1;
			//idtoUtask=/*Integer.valueOf(cDAO.getMaxPK())*/getNewIdto(fcdb);
			TClase c=new TClase();
			c.setIDTO(idtoUtask);
			c.setName(ut.getUtaskName());
			cDAO.insert(c);
		}
		
		
		LinkedList<Object> linstance=iDAO.getAllCond("IDTO="+Constants.IDTO_UTASK);
		Iterator itinstance=linstance.iterator();
		
		while(itinstance.hasNext()){
			Instance i=(Instance) itinstance.next();
			i.setIDTO(idtoUtask.toString());
			i.setNAME(ut.getUtaskName());
			
			boolean insert=true;
			
			if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_TARGETCLASS)))){
				if(ut.getTargetClass()!=null){//Es null(por lo menos de momento) para las userTask tipo EXPORT, ya que no generan un resultado en motor
					if(i.getOP()==null){
						i.setOP(Constants.OP_INTERSECTION);
						i.setVALUECLS(ut.getTargetClass().toString());
					}
					if(i.getOP().equals(Constants.OP_CARDINALITY)){
						if (ut.getCmaxTGC()!=null)
							i.setQMAX(ut.getCmaxTGC().toString());
						if (ut.getCminTGC()!=null)
							i.setQMIN(ut.getCminTGC().toString());
					}
				}else insert=false;
			}
			
			
			if(i.getPROPERTY().equals(String.valueOf((Constants.IdPROP_MYFUNCTIONALAREA)))){
				if(i.getOP().equals(Constants.OP_CARDINALITY)){
					if (ut.getCmaxTGC()!=null)
						i.setQMAX(ut.getCmaxTGC().toString());
					if (ut.getCminTGC()!=null)
						i.setQMIN(ut.getCminTGC().toString());
				}else{
					i.setVALUECLS(String.valueOf(Constants.IDTO_FUNCTIONAL_AREA));
					i.setOP(Constants.OP_ONEOF);
					i.setVALUE(ut.getIdtoAreaFunc().toString());
				}
			}
			
			if(insert)
				litosave.add(i);
		}
		
		if (ut.getAURoles()!=null) {
			Iterator it = ut.getAURoles().iterator();
			while (it.hasNext()) {
				String uRol = (String)it.next();
				Instance i= new Instance();
				i.setIDTO(idtoUtask.toString());
				i.setNAME(ut.getUtaskName());
				i.setPROPERTY(String.valueOf(Constants.IdPROP_USERROL));
				//TODO obtener el ido
				
				i.setVALUE(String.valueOf(hRoles.get(uRol)));
				//
				i.setVALUECLS(String.valueOf(Constants.IDTO_USERROL));
				i.setOP(Constants.OP_ONEOF);
				litosave.add(i);
			}
		}
		
		if (ut.getSourceClass()!=null){
			Instance i= new Instance();
			i.setIDTO(idtoUtask.toString());
			i.setNAME(ut.getUtaskName());
			i.setPROPERTY(String.valueOf((Constants.IdPROP_SOURCECLASS)));
			i.setVALUECLS(ut.getSourceClass().toString());
			i.setOP(Constants.OP_INTERSECTION);
			litosave.add(i);
			Instance i2=i.clone();
			i2.setOP(Constants.OP_CARDINALITY);
			i2.setVALUECLS(null);
			if (ut.getCmaxSC()!=null)
				i2.setQMAX(ut.getCmaxSC().toString());
			if (ut.getCminSC()!=null)
				i2.setQMIN(ut.getCminSC().toString());
			litosave.add(i2);
		}
		
		if(ut.getType()==UTask.IMPORT){
			Instance i= new Instance();
			i.setIDTO(idtoUtask.toString());
			i.setNAME(ut.getUtaskName());
			i.setPROPERTY(String.valueOf((Constants.IdPROP_PARAMS)));
			i.setVALUECLS(String.valueOf(Constants.IDTO_IMPORTEXPORT_PARAMS));
			i.setOP(Constants.OP_INTERSECTION);
			litosave.add(i);
			Instance i2=i.clone();
			i2.setOP(Constants.OP_CARDINALITY);
			i2.setVALUECLS(null);
			if (ut.getCmaxSC()!=null)
				i2.setQMAX(ut.getCmaxSC().toString());
			if (ut.getCminSC()!=null)
				i2.setQMIN(ut.getCminSC().toString());
			litosave.add(i2);
		}else if(ut.getType()==UTask.EXPORT){
			Instance i= new Instance();
			i.setIDTO(idtoUtask.toString());
			i.setNAME(ut.getUtaskName());
			i.setPROPERTY(String.valueOf((Constants.IdPROP_PARAMS)));
			i.setVALUECLS(String.valueOf(Constants.IDTO_IMPORTEXPORT_PARAMS));
			i.setOP(Constants.OP_INTERSECTION);
			litosave.add(i);
			Instance i2=i.clone();
			i2.setOP(Constants.OP_CARDINALITY);
			i2.setVALUECLS(null);
			if (ut.getCmaxSC()!=null)
				i2.setQMAX(ut.getCmaxSC().toString());
			if (ut.getCminSC()!=null)
				i2.setQMIN(ut.getCminSC().toString());
			litosave.add(i2);
		}
		
		iDAO.setCommit(false);
		Iterator<Instance> iti=litosave.iterator();
		while(iti.hasNext()){
			Instance i=iti.next();
			iDAO.insert(i);
		}
		iDAO.commit();
		iDAO.close();
		
		T_Herencias th=new T_Herencias();
		th.setID_TO(idtoUtask);
		th.setID_TO_Padre(Constants.IDTO_UTASK);
		T_HerenciasDAO thDAO=new T_HerenciasDAO();
		thDAO.open();
		thDAO.insert(th);
		
		/*if(ut.getSourceClass()!=null && ut.getType()==UTask.ACTION){
			T_Herencias th2=new T_Herencias();
			th2.setID_TO(idtoUtask);
			th2.setID_TO_Padre(Constants.IDTO_ACTION);
			thDAO.insert(th2);
		}*/
		
		if(ut.getType()==UTask.IMPORT){
			T_Herencias th2=new T_Herencias();
			th2.setID_TO(idtoUtask);
			th2.setID_TO_Padre(Constants.IDTO_IMPORT);
			thDAO.insert(th2);
		}else if(ut.getType()==UTask.EXPORT){
			T_Herencias th2=new T_Herencias();
			th2.setID_TO(idtoUtask);
			th2.setID_TO_Padre(Constants.IDTO_EXPORT);
			thDAO.insert(th2);
		}
		
		
		if(ut.isListener()){
			ListenerUtask lu=new ListenerUtask();
			lu.setUtask(idtoUtask);
			ListenerUtaskDAO luDAO=new ListenerUtaskDAO();
			luDAO.open();
			luDAO.insert(lu);
			luDAO.close();
		}
		
		Iterator<String> help=ut.getHelp().keySet().iterator();
		while(help.hasNext()){
			String language=help.next();
			Iterator<String> itrDescription=ut.getHelp().get(language).iterator();
			while(itrDescription.hasNext()){
				String description=itrDescription.next();
				insertClassDescription(idtoUtask, description, language, fcdb);
			}
		}
		
		thDAO.close();
	}
	
	public static void insertClassDescription(int idto,String description,String language,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			String sql = "INSERT INTO HelpClasses (IDTO, DESCRIPTION, LANGUAGE) VALUES ("+idto+",'"+description+"','"+language+"')";
			System.out.println(sql);
			st.executeUpdate(sql);
			
		}finally{			
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
	}
	
	public static void insertPropertyDescription(int idProp,String description,String language,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			String sql = "INSERT INTO HelpProperties (IDPROP, DESCRIPTION, LANGUAGE) VALUES ("+idProp+",'"+description+"','"+language+"')";
			System.out.println(sql);
			st.executeUpdate(sql);
			
		}finally{			
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
	}
	
	/*public static Integer getNewIdto(FactoryConnectionDB fcdb) throws NamingException, SQLException{
		String sql;
		sql = "SELECT max(IDTO) FROM clases where idto<"+Constants.MAX_ID_CLASS;
		
		Statement st = null; 
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return rs.getInt(1)+1;					
			}else{
				return 0;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}*/
	
	
	public static Integer getIdGroup(String nameGroup,ConfigData configImport) throws SQLException, NamingException{
		Integer id=configImport.getIdGroup(nameGroup);
		if(id==null){
			GroupsDAO gpDAO=new GroupsDAO();
			gpDAO.open();
			LinkedList<Object> list=gpDAO.getAllCond("NAME LIKE '"+nameGroup+"'");
			if(!list.isEmpty())
				id=(((Groups)list.getFirst()).getIdGroup());
			gpDAO.close();
		}
		return id;
	}
	
	public static int getNextIdGroup(ConfigData configImport) throws SQLException, NamingException{
		int id=configImport.maxIdGroups()+1;
		GroupsDAO gpDAO=new GroupsDAO();
		gpDAO.open();
		String idPK=gpDAO.getMaxPK();
		if(idPK!=null)
			id=Math.max(id, Integer.valueOf(idPK)+1);
		gpDAO.close();
		return id;
	}
	
	public static void createAreaFunc(AreaFunc af,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		
		
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.open();
		Instance i= new Instance();
		
		Integer newIdo=null;
		Integer newIdto=Integer.valueOf(Constants.IDTO_FUNCTIONAL_AREA);
		
		
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			String sql = "INSERT INTO O_Reg_Instancias_Index (Id_TO, RDN) VALUES ("+newIdto+", '"+ af.getName()+"')";
			System.out.println(sql);
			st.executeUpdate(sql);
			
		}finally{			
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
		ResultSet rs = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			
			
			String sql = "SELECT Autonum FROM O_Reg_Instancias_Index WHERE RDN LIKE '"+ af.getName()+"' AND Id_TO="+newIdto;
			System.out.println(sql);
			
			rs = st.executeQuery(sql);
			if (rs.next()) {
				newIdo=rs.getInt(1);
				String sql2 = "UPDATE O_Reg_Instancias_Index SET ID_O="+newIdo+" WHERE RDN LIKE '"+ af.getName()+"' AND Id_TO="+newIdto;
				System.out.println(sql2);
				st.executeUpdate(sql2);

			}
		}finally{
			
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
		
		
		i.setIDO(newIdo.toString());
		i.setIDTO(newIdto.toString());
		i.setNAME(Constants.CLS_FUNCTIONAL_AREA);
		i.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
		i.setVALUE(af.getName());
		i.setVALUECLS(String.valueOf(Constants.IDTO_STRING));
		iDAO.insert(i);
		iDAO.close();
		
		O_Datos_AttribDAO oDAO= new O_Datos_AttribDAO();
		oDAO.open();
		O_Datos_Attrib od= new O_Datos_Attrib();
		od.setIDO(newIdo);
		od.setIDTO(newIdto);
		od.setPROPERTY(Constants.IdPROP_RDN);
		od.setVALTEXTO(af.getName());
		od.setVALUECLS(Constants.IDTO_STRING);
		oDAO.insert(od);
		oDAO.close();
		
		
		
	}
	
	public static void changeClass(Class c, FactoryConnectionDB fcdb) throws Exception {
		if(c.isExcluded())
			deleteClass(c,fcdb);
		//else createClass(c, fcdb);
		
	}
	
	public static void deleteClass(Class cls,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		deleteClass(cls.getIdtoClass(),cls.getClassName(), fcdb);
	}
	
	public static void deleteClass(int idtoClass,String className,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(false);
			st = con.getBusinessConn().createStatement();
			
			String sql = "DELETE FROM Access WHERE IDTO="+idtoClass+" OR VALUECLS="+idtoClass+" OR TASK="+idtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Alias WHERE CLASS LIKE '"+className+"' OR UTASK LIKE '"+className+"'";
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM ColumnProperties WHERE CLASS LIKE '"+className+"' OR CLASSPARENT LIKE '"+className+"'";
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM Groups WHERE CLASS="+idtoClass+" OR UTASK="+idtoClass;
			
			st.executeUpdate(sql);
			
			sql = "DELETE FROM OrderProperties WHERE IDTO="+idtoClass;
			
			st.executeUpdate(sql);
			
			//sql = "DELETE FROM instances WHERE IDTO="+idtoClass+" OR VALUECLS="+idtoClass;
			
			sql = "DELETE FROM instances WHERE IDTO="+idtoClass;
			
			st.executeUpdate(sql);
			
			InstanceDAO iDAO= new InstanceDAO();
			iDAO.setCommit(false);
			iDAO.open();
			LinkedList li=iDAO.getAllCond(" VALUECLS="+idtoClass);	
			Iterator itr=li.iterator();
			while(itr.hasNext()){
				Instance inst=(Instance)itr.next();
				if(iDAO.getAllCond(" IDTO="+inst.getIDTO()+" AND PROPERTY="+inst.getPROPERTY()+" AND (OP LIKE '"+Constants.OP_INTERSECTION+"' OR OP LIKE '"+Constants.OP_UNION+"')").size()==1)
					iDAO.deleteCond(" IDTO="+inst.getIDTO()+" AND PROPERTY="+inst.getPROPERTY()+" AND (OP LIKE '"+Constants.OP_CARDINALITY+"' OR OP LIKE '"+Constants.OP_DEFAULTVALUE+"')");
				
				//Si hay un rango con OR lo convertimos en AND ya que ahora apunta a un solo rango
				LinkedList lInst=iDAO.getAllCond(" IDTO="+inst.getIDTO()+" AND PROPERTY="+inst.getPROPERTY()+" AND VALUECLS IS NOT NULL AND VALUECLS!="+idtoClass+" AND OP LIKE '"+Constants.OP_UNION+"'");
				if(lInst.size()==1)
					sql = "UPDATE instances SET OP='"+Constants.OP_INTERSECTION+"' WHERE IDTO="+inst.getIDTO()+" AND PROPERTY="+inst.getPROPERTY()+" AND VALUECLS IS NOT NULL AND VALUECLS!="+idtoClass+" AND OP LIKE '"+Constants.OP_UNION+"'";
				st.executeUpdate(sql);

			}
			iDAO.commit();
						
			//Si se trata de una clase miramos si hay alguna accion que la apunte, y en ese caso, la borramos ya que estaria incompleta 
			if(!getIdtoParents(idtoClass).contains(Constants.IDTO_ACTION)){
				Iterator<Integer> itrSpec=getIdtoSpecialized(Constants.IDTO_ACTION).iterator();
				String idtos="";
				while(itrSpec.hasNext()){
					if(!idtos.isEmpty())
						idtos+=" OR";
					else idtos=" (";
					idtos+=" IDTO="+itrSpec.next();
				}
				if(!idtos.isEmpty()){
					idtos+=")";
					Iterator itrAction=iDAO.getAllCond(idtos+" AND VALUECLS="+idtoClass).iterator();
					
					ArrayList<Integer> idtosDeleted=new ArrayList<Integer>();
					while(itrAction.hasNext()){
						Instance instance=(Instance)itrAction.next();
						int idtoAction=Integer.valueOf(instance.getIDTO());
						String classNameAction=instance.getNAME();
						if(!idtosDeleted.contains(idtoAction)){
							deleteClass(idtoAction,classNameAction,fcdb);
							idtosDeleted.add(idtoAction);
							System.err.println("Accion borrada:"+classNameAction);
							//TODO Las acciones que tienen parametros dejan estos como basura, pero de momento no importa porque nada le apunta
						}
					}
				}
			}
			
			// Si se trata de una clase miramos si hay alguna userTask que la apunte, y en ese caso, la borramos ya que estaria incompleta 
			if(!getIdtoParents(idtoClass).contains(Constants.IDTO_UTASK)){
				Iterator<Integer> itrSpec=getIdtoSpecialized(Constants.IDTO_UTASK).iterator();
				String idtos="";
				while(itrSpec.hasNext()){
					if(!idtos.isEmpty())
						idtos+=" OR";
					else idtos=" (";
					idtos+=" IDTO="+itrSpec.next();
				}
				if(!idtos.isEmpty()){
					idtos+=")";
					Iterator itrUtask=iDAO.getAllCond(idtos+" AND VALUECLS="+idtoClass).iterator();
					
					ArrayList<Integer> idtosDeleted=new ArrayList<Integer>();
					while(itrUtask.hasNext()){
						Instance instance=(Instance)itrUtask.next();
						int idtoAction=Integer.valueOf(instance.getIDTO());
						String classNameUTask=instance.getNAME();
						if(!idtosDeleted.contains(idtoAction)){
							deleteClass(idtoAction,classNameUTask,fcdb);
							idtosDeleted.add(idtoAction);
							System.err.println("Utask borrada:"+classNameUTask);
						}
					}
				}
			}
			
			iDAO.close();
			
			sql = "DELETE FROM instances WHERE VALUECLS="+idtoClass;
			
			st.executeUpdate(sql);
			
			
			sql="DELETE FROM T_Herencias WHERE ID_TO="+idtoClass+" OR ID_TO_Padre="+idtoClass;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM CardMed WHERE IDTO_NAME LIKE '"+className+"' OR IDTO_PARENT_NAME LIKE '"+className+"'";
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM EssentialProperties WHERE CLASS="+idtoClass+" OR UTASK="+idtoClass;
			
			st.executeUpdate(sql);
			
			sql="DELETE FROM Masks WHERE CLASS LIKE '"+className+"' OR UTASK LIKE '"+className+"'";
			
			st.executeUpdate(sql);

			con.getBusinessConn().commit();
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		
	}
	
	public static void createClass(Class cls,FactoryConnectionDB fcdb) throws SQLException, NamingException {

		//Creamos la clase
		TClaseDAO cDAO=new TClaseDAO();
		int idto=Integer.valueOf(cDAO.getMaxPK())+1;
		TClase c=new TClase();
		c.setIDTO(idto);
		c.setName(cls.getClassName());
		cDAO.insert(c);
		cDAO.close();

		//Recorremos las properties para ir creando los instances
		LinkedList<Instance> litosave=new LinkedList<Instance>();
		Iterator<Properties> itr=cls.getProperties().iterator();
		while(itr.hasNext()){
			Properties prop=itr.next();
			Instance i= new Instance();
			i.setIDTO(String.valueOf(idto));
			i.setNAME(cls.getClassName());
			
			if(prop.getPROP()==null){//Si la property no existe la creamos
				PropertiesDAO propertiesDao = new PropertiesDAO();
				propertiesDao.open();
				prop.setPROP(new Integer(propertiesDao.getLastPK(Constants.MIN_IdPROP_MODEL)).intValue()+1);
				propertiesDao.insert(prop);
				propertiesDao.close();
			}
			
			i.setPROPERTY(String.valueOf(prop.getPROP()));
			i.setVALUECLS(String.valueOf(prop.getVALUECLS()));
			i.setOP(Constants.OP_INTERSECTION);
			litosave.add(i);
			Instance i2=i.clone();
			i2.setOP(Constants.OP_CARDINALITY);
			i2.setVALUECLS(null);
//			if (prop.getQMIN()!=null)
//				i2.setQMIN(prop.getQMIN().toString());
//			if (prop.getQMAX()!=null)
//				i2.setQMAX(prop.getQMAX().toString());
			litosave.add(i2);
		}
		
		InstanceDAO iDAO = new InstanceDAO();
		iDAO.open();
		Iterator<Instance> iti=litosave.iterator();
		while(iti.hasNext()){
			Instance i=iti.next();
			iDAO.insert(i);
		}
		
		
		//Si tiene padre insertamos la herencia
		if(cls.getClassNameParent()!=null){
			T_Herencias th=new T_Herencias();
			th.setID_TO(idto);
			int idtoParent=getIdtoClass(cls.getClassNameParent(), fcdb);
			th.setID_TO_Padre(idtoParent);
			T_HerenciasDAO thDAO=new T_HerenciasDAO();
			thDAO.open();
			thDAO.insert(th);
			thDAO.close();
		}
		
		iDAO.close();
		
	}
	
	public static void createIndivididual(AreaFunc af,FactoryConnectionDB fcdb) throws SQLException, NamingException {
		
		
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.open();
		Instance i= new Instance();
		
		Integer newIdo=null;
		Integer newIdto=Integer.valueOf(Constants.IDTO_FUNCTIONAL_AREA);
		
		
		Statement st = null;
		
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			String sql = "INSERT INTO O_Reg_Instancias_Index (Id_TO, RDN) VALUES ("+newIdto+", '"+ af.getName()+"')";
			System.out.println(sql);
			st.executeUpdate(sql);
			
			
			
		
		}finally{			
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
		ResultSet rs = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			
			
			
			String sql = "SELECT Autonum FROM O_Reg_Instancias_Index WHERE RDN LIKE '"+ af.getName()+"' AND Id_TO="+newIdto;
			System.out.println(sql);
			
			rs = st.executeQuery(sql);
			if (rs.next()) {
				newIdo=rs.getInt(1);
				String sql2 = "UPDATE O_Reg_Instancias_Index SET ID_O="+newIdo+" WHERE RDN LIKE '"+ af.getName()+"' AND Id_TO="+newIdto;
				System.out.println(sql2);
				st.executeUpdate(sql2);
			}
		}finally{
			
			if (rs!=null)
				rs.close();
			if (st!=null)
				st.close();
			//if (con!=null)
				//con.close();
			
		}
		
		
		i.setIDO(newIdo.toString());
		i.setIDTO(newIdto.toString());
		i.setNAME(Constants.CLS_FUNCTIONAL_AREA);
		i.setPROPERTY(String.valueOf(Constants.IdPROP_RDN));
		i.setVALUE(af.getName());
		i.setVALUECLS(String.valueOf(Constants.IDTO_STRING));
		iDAO.insert(i);
		iDAO.close();
		
		O_Datos_AttribDAO oDAO= new O_Datos_AttribDAO();
		oDAO.open();
		O_Datos_Attrib od= new O_Datos_Attrib();
		od.setIDO(newIdo);
		od.setIDTO(newIdto);
		od.setPROPERTY(Constants.IdPROP_RDN);
		od.setVALTEXTO(af.getName());
		od.setVALUECLS(Constants.IDTO_STRING);
		oDAO.insert(od);
		oDAO.close();
	}
	

	public static Integer getCompatibilityRange(Integer idtoRange, Integer idtoClass, Integer idProp) throws Exception {
		
			InstanceDAO instanceDAO=new InstanceDAO();
			instanceDAO.open();
			LinkedList lIns=instanceDAO.getAllCond((idtoClass!=null?" IDTO="+idtoClass+" AND":"")+" PROPERTY="+idProp+" AND VALUECLS IS NOT NULL AND (OP LIKE '"+Constants.OP_INTERSECTION+"' OR OP LIKE '"+Constants.OP_UNION+"' OR OP LIKE '"+Constants.OP_ONEOF+"')");
			instanceDAO.close();
			Iterator itIns=lIns.iterator();
			while(itIns.hasNext()){
				Instance inst=(Instance)itIns.next();
				int valueCls=Integer.valueOf(inst.getVALUECLS());
				if(valueCls==idtoRange)
					return valueCls;
				else{
					Iterator<Integer> itsup=getIdtoParents(idtoRange).iterator();
					while(itsup.hasNext()){
						int sup=itsup.next();
						if(valueCls==sup)
							return valueCls;
					}
				}
			}
			
		return null;
	}
	
	public static HashSet<Integer> getRanges(Integer idtoClass, Integer idProp) throws Exception {
		HashSet<Integer> listRanges=new HashSet<Integer>();
		InstanceDAO instanceDAO=new InstanceDAO();
		instanceDAO.open();
		LinkedList lIns=instanceDAO.getAllCond((idtoClass!=null?" IDTO="+idtoClass+" AND":"")+" PROPERTY="+idProp+" AND VALUECLS IS NOT NULL AND (OP LIKE '"+Constants.OP_INTERSECTION+"' OR OP LIKE '"+Constants.OP_UNION+"' OR OP LIKE '"+Constants.OP_ONEOF+"')");
		instanceDAO.close();
		Iterator itIns=lIns.iterator();
		while(itIns.hasNext()){
			Instance inst=(Instance)itIns.next();
			int valueCls=Integer.valueOf(inst.getVALUECLS());
			listRanges.add(valueCls);
		}
		
		return listRanges;
	}
	
	
	public static void insertRange(Range rg,FactoryConnectionDB fcdb) throws NamingException, SQLException {
			
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.setCommit(false);
		iDAO.open();
		
		String op=Constants.OP_INTERSECTION;
		LinkedList lInst=iDAO.getAllCond(" IDTO="+rg.getIdtoClass()+" AND PROPERTY="+rg.getIdProp()+" AND VALUECLS IS NOT NULL AND (OP LIKE '"+Constants.OP_INTERSECTION+"' OR OP LIKE '"+Constants.OP_UNION+"')");
		if(!lInst.isEmpty() || rg.getRanges().size()>1)
			op=Constants.OP_UNION;
		
		Iterator<Integer> itr=rg.getRanges().keySet().iterator();
		while(itr.hasNext()){
			Integer range=itr.next();
			Instance i=new Instance();
			i.setIDTO(rg.getIdtoClass().toString());
			i.setPROPERTY(rg.getIdProp().toString());
			i.setVALUECLS(range.toString());
			i.setNAME(rg.getClassName());
			i.setOP(op);
			iDAO.insert(i);
		}
		iDAO.commit();
		iDAO.close();
	}
	
	public static void deleteRange(Range rg,FactoryConnectionDB fcdb) throws Exception {
		Statement st = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			String sql;
			
			Iterator<Integer> itr=rg.getRanges().keySet().iterator();
			String sqlDelete=" AND (VALUECLS="+itr.next();
			while(itr.hasNext())
				sqlDelete+=" OR VALUECLS="+itr.next();
			sqlDelete+=")";
			
			sql = "DELETE FROM instances WHERE PROPERTY ="+rg.getIdProp()+" AND IDTO="+rg.getIdtoClass()+sqlDelete+" AND (OP LIKE '"+Constants.OP_INTERSECTION+"' OR OP LIKE '"+Constants.OP_UNION+"')";
			
			
			int num=st.executeUpdate(sql);
			
			if(num==0)
				throw new Exception("WARNING: Posiblemente el range actual de la property no es el que se esperaba. No se ha podido borrar el range "+rg);
			
			InstanceDAO iDAO= new InstanceDAO();
			iDAO.open();
			
			LinkedList lInst=iDAO.getAllCond(" IDTO="+rg.getIdtoClass()+" AND PROPERTY="+rg.getIdProp()+" AND VALUECLS IS NOT NULL AND OP LIKE '"+Constants.OP_UNION+"'");
			if(lInst.size()==1)
				sql = "UPDATE instances SET OP='"+Constants.OP_INTERSECTION+"' WHERE IDTO="+rg.getIdtoClass()+" AND PROPERTY="+rg.getIdProp()+" AND VALUECLS IS NOT NULL AND OP LIKE '"+Constants.OP_UNION+"'";
			st.executeUpdate(sql);
			iDAO.close();
			
		}finally{
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
	public static ArrayList<Integer> getIdtoSpecialized(int idto) throws SQLException, NamingException{
		ArrayList<Integer> list=new ArrayList<Integer>();
		T_HerenciasDAO herenciasDAO = new T_HerenciasDAO();
		herenciasDAO.open();
		Iterator<Object> itr=herenciasDAO.getAllCond(" ID_TO_Padre="+idto).iterator();
		while(itr.hasNext()){
			T_Herencias herencia=(T_Herencias)itr.next();
			if(herencia.getID_TO()!=idto)//Evitamos añadirse a si mismo ya que en la tabla herencias tambien esta ese registro
				list.add(herencia.getID_TO());
		}
		herenciasDAO.close();
		return list;
	}
	
	public static ArrayList<Integer> getIdtoParents(int idto) throws SQLException, NamingException{
		ArrayList<Integer> list=new ArrayList<Integer>();
		T_HerenciasDAO herenciasDAO = new T_HerenciasDAO();
		herenciasDAO.open();
		Iterator<Object> itr=herenciasDAO.getAllCond(" ID_TO="+idto).iterator();
		while(itr.hasNext()){
			T_Herencias herencia=(T_Herencias)itr.next();
			list.add(herencia.getID_TO_Padre());
		}
		herenciasDAO.close();
		return list;
	}
	
	public static String getClassName(int idto) throws SQLException, NamingException{
		TClaseDAO cDAO=new TClaseDAO();
		cDAO.open();
		Iterator<Object> itr=cDAO.getAllCond(" ID="+idto).iterator();
		TClase c=(TClase)itr.next();
		cDAO.close();
		
		return c.getName();
	}
	
	public static void insertInstance(Instance inst) throws SQLException, NamingException{
		InstanceDAO iDAO=new InstanceDAO();
		iDAO.open();
		iDAO.insert(inst);
		iDAO.close();
	}
	
	
	public static void insertODatosAtrib(ArrayList<O_Datos_Attrib> listaod) throws SQLException, NamingException{
		//System.out.println("[Auxiliar.insertODatosAtrib odlistaod="+listaod);
		IDAO idao = DAOManager.getInstance().getDAO("O_Datos_Atrib");
		idao.setCommit(false);
		idao.open();
		O_Datos_AttribDAO insdao = (O_Datos_AttribDAO)idao.getDAO();
		for(int i=0;i<listaod.size();i++){
			insdao.insert(listaod.get(i));	
		}
		idao.commit();
		idao.close();
	}
	
	
	public static boolean existIdto(int idto) throws SQLException, NamingException{
		InstanceDAO iDAO= new InstanceDAO();
		iDAO.open();
		LinkedList li=iDAO.getAllCond(" IDTO="+idto);	
		iDAO.close();
		
		return !li.isEmpty();
	}
	
	public static boolean specializedFrom(TClase c,int idtoSuperior) throws SQLException, NamingException{
		boolean specialized=false;
		T_HerenciasDAO herencDAO=new T_HerenciasDAO();
		herencDAO.open();
		Iterator itrParents=herencDAO.getParents(String.valueOf(c.getIDTO())).iterator();
		while(!specialized && itrParents.hasNext()){
			if(idtoSuperior==(Integer)itrParents.next())
				specialized=true;
		}
		herencDAO.close();
		return specialized;
	}
	
	public static HashSet<Integer> getIdtosWithIndex(FactoryConnectionDB fcdb) throws SQLException, NamingException{
		HashSet<Integer> list=new HashSet<Integer>();
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT DISTINCT id FROM clase where " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() +" in (SELECT dominio FROM " + gSQL.getCharacterBegin() + Constants.CLS_INDICE.toLowerCase() + gSQL.getCharacterEnd() +")";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				list.add(rs.getInt(1));
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		return list;
	}
	
	public static boolean hasIndex(IndexName ind,FactoryConnectionDB fcdb) throws SQLException, NamingException{
		boolean hasIndex;
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cond="dominio=(select "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" from clase where id="+ind.getIdto()+")" /*and campo_indexado=(select "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" from propiedad_dato where id="+ind.getProperty()+")"*/;
		
		if (ind.getPropFilter()!=null)
			cond += " and campo_filtro=(select "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" from propiedad_dato where id="+ind.getPropFilter()+") and valor_filtro='"+ind.getValueFilter()+"'";
		
		if (ind.getMiEmpresa()!=null)
			cond += " and mi_empresa=" + QueryConstants.getTableId(ind.getMiEmpresa());
		else
			cond += " AND MI_EMPRESA is null";
		
		//select * from "índice" where dominio=(select "tableId" from clase where id=1) and campo_indexado=(select "tableId" from propiedad_dato where id=1) and campo_filtro=(select "tableId" from propiedad_dato where id=1) and valor_filtro='hola' and mi_empresa=1;
		
		String sql = "SELECT * FROM "+gSQL.getCharacterBegin()+Constants.CLS_INDICE.toLowerCase()+gSQL.getCharacterEnd()+" where " + cond;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				hasIndex=true;
			}else{
				hasIndex=false;
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
		return hasIndex;
	}
	
	public static Integer getIdoOfPriority(int pesoPrioridad, FactoryConnectionDB fcdb, DataBaseMap dataBaseMap) throws NamingException, SQLException {
		//antes buscaba en O_Reg_Instancias
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		
		String sql = "SELECT "+gSQL.getCharacterBegin()+"tableId"+gSQL.getCharacterEnd()+" FROM "+Constants.CLS_PRIORITY.toLowerCase()+" WHERE peso_prioridad="+pesoPrioridad;

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try{
			
			con = fcdb.createConnection(true);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				return QueryConstants.getIdo(rs.getInt(1),getIdtoClass(Constants.CLS_PRIORITY, fcdb));
				
			}else{
				return null;
				
			}
		}finally{
			if(rs!=null)
				rs.close();
			if(st!=null)
				st.close();
			//if(con!=null)
				//con.close();
		}
	}
	
}
