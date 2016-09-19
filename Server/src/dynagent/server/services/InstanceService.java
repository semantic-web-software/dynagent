package dynagent.server.services;

import java.awt.Window;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.swing.ImageIcon;

import net.sf.jasperreports.engine.JRException;

import org.apache.commons.lang.StringUtils;
import org.jdom.CDATA;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;

import dynagent.common.Constants;
import dynagent.common.basicobjects.License;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.IteratorQuery;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.docServer;
import dynagent.common.communication.messageFactory;
import dynagent.common.communication.queryData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.selectData;
import dynagent.common.process.IAsigned;
import dynagent.common.properties.DomainProp;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Email;
import dynagent.common.utils.Email.TipoConexionSegura;
import dynagent.common.utils.EmailConfiguration;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.Zip;
import dynagent.common.utils.jdomParser;
import dynagent.server.database.IndividualCreator;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.dbmap.PropertyInfo;
import dynagent.server.dbmap.Table;
import dynagent.server.ejb.Asigned;
import dynagent.server.ejb.AuxiliarModel;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.IKnowledgeBaseInfoServer;
import dynagent.server.ejb.IdReport;
import dynagent.server.ejb.Session;
import dynagent.server.exceptions.ConnectionException;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.replication.IReplication;
import dynagent.server.reports.clasificator.ReportClasificator;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.server.services.querys.DataInfo;
import dynagent.server.services.reports.JrxmlParser;
import dynagent.server.services.reports.ViewReports;
import dynagent.server.web.httpGateway;

///END: para_JDOM

public class InstanceService implements docServer, IAsigned, Serializable {

	private static final long serialVersionUID = 2596600729280057502L;

	public static final String keyEncrypt = "dynamicIntelligent";

	private FactoryConnectionDB factConnDB = null;

	// private String databaseIP = null;
	// private Integer port = null;
	private int business = 0;

	private IKnowledgeBaseInfo ik;
	
	private IReplication ir;

	private boolean debugMode;

	private GenerateSQL generateSQL;
	
	private DataBaseMap dataBaseMap;

	private class QueryReport extends Object {
		private String idMaster;
		private ArrayList<IdReport> report;
		private int nCopies;
		private boolean displayPrintDialog;
		private String printSequence;
		private String idReportExcel;
		private String impresora=null;

		public QueryReport(String idMaster, ArrayList<IdReport> report, 
				int nCopies, boolean displayPrintDialog, String printSequence, String idReportExcel,String impresora) {
			this.idMaster = idMaster;
			this.report = report;
			this.nCopies = nCopies;
			this.displayPrintDialog = displayPrintDialog;
			this.printSequence = printSequence;
			this.idReportExcel = idReportExcel;
			this.impresora=impresora;
		}
		
		public String getIdMaster() {
			return idMaster;
		}
		public ArrayList<IdReport> getReport() {
			return report;
		}
		public boolean getDisplayPrintDialog() {
			return displayPrintDialog;
		}
		public int getNCopies() {
			return nCopies;
		}
		public String getPrintSequence() {
			return printSequence;
		}
		public String getIdReportExcel() {
			return idReportExcel;
		}
		public String getImpresora() {
			return impresora;
		}
	}

	public InstanceService() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private void jbInit() throws Exception {
	}
	public InstanceService(FactoryConnectionDB factConnDB, Context EJBContext, boolean debugMode) {
		this.factConnDB = factConnDB;
		//this.databaseIP = factConnDB.getDatabaseIP();
		//this.port = factConnDB.getPort();
		this.business = factConnDB.getBusiness();
		this.debugMode = debugMode;
		String gestorDB = factConnDB.getGestorDB();
		this.generateSQL = new GenerateSQL(gestorDB);
	}

	public IKnowledgeBaseInfo getIk() {
		return ik;
	}
	public void setIk(IKnowledgeBaseInfo ik) {
		this.ik = ik;
	}

	public void setIr(IReplication ir) {
		this.ir = ir;
	}

    public IndividualData serverGetFactsInstanceOfQuery(/*String user, int empresa, */Element root, Integer uTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
    	return null;
    }
	//aqui no se llega mediante comunicaciones
	public selectData serverGetQuery(Element root, Integer uTask, int mode) throws SystemException, DataErrorException, NotFoundException, IncoherenceInMotorException, JDOMException, SQLException, NamingException {
		try {
			return query(root, uTask, mode).toSelectData(root);
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ConnectionException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NoSuchColumnException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	//aqui se llega mediante comunicaciones
	public queryData query(Element root, Integer uTask, int mode) throws JDOMException, SQLException, NoSuchElementException, NamingException, NotFoundException, IncoherenceInMotorException, ConnectionException, NoSuchColumnException, DataErrorException {
		QueryService qd = new QueryService(factConnDB, ik, dataBaseMap);
		return qd.Query(root, ir, uTask, mode,true);
	}

	/*public void serverDeleteQuery(Integer userRol, String user, Element root) 
			throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, 
			IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, 
			ApplicationException, OperationNotPermitedException {
		try {
			//filter.setAttribute("USER", user);
			deleteQuery(userRol, user, root);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS, e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
		} catch (DataErrorException pe) {
			pe.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS, pe.getMessage());
		} catch (JDOMException pe) {
			pe.printStackTrace();
			throw new SystemException(SystemException.ERROR_JDOM, pe.getMessage());
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS, e.getMessage());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS, e.getMessage());
		}
	}
	private void deleteQuery(Integer userRol, String user, Element root) throws JDOMException, SQLException, NoSuchElementException, 
			NamingException, DataErrorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, 
			IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, 
			ApplicationException, ParseException, OperationNotPermitedException {
		QueryService qS = new QueryService(factConnDB, ik);
		String sql = qS.dbFindQuery(root, "", true);
		ArrayList<IPropertyDef> facts = getFacts(sql);
		IndividualData id = new IndividualData(facts);
		this.serverTransitionObject(userRol, user, id);
	}
	private ArrayList<IPropertyDef> getFacts(String query) throws SQLException, NamingException, IncompatibleValueException, 
			CardinalityExceedException, NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		ArrayList<IPropertyDef> facts = new ArrayList<IPropertyDef>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(false); 
		try {
			st = con.getBusinessConn().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(query);
			while (rs.next()) {
				Integer idoRoot = rs.getInt(1);
				if (!rs.wasNull()) {
					//la property no se consultara, estos facts solo se usan para borrar individuos
					FactInstance fi = new FactInstance(ik.getClassOf(idoRoot),idoRoot,0,null,null,null,null,null,null);
					fi.setOrder(action.DEL_OBJECT);
					facts.add(fi);
				}
			}
		} finally {
			if (rs!=null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		//System.out.println("Fin de la funcion dbFindQueryData");
		return facts;
	}*/

	/*public static Integer getClassOf(FactoryConnectionDB fcdb, int ido) throws DataErrorException {
		Integer idto = null;
		//antes buscaba en O_Reg_Instancias
		String sql = "select id_to from o_datos_atrib where id_o=" + ido + " AND property=" + Constants.IdPROP_RDN;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				idto = rs.getInt(1);
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER CLASE DE " + ido);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER CLASE DE " + ido);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return idto;
	}*/

	private String getLoginLock(String ikey) throws SQLException, NamingException {
		String login = null;
		String sql = "select login from locksid where id='" + ikey.replaceAll("'", "''") + "'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			//System.out.println(sql);
			rs = st.executeQuery(sql);
			if (rs.next())
				login=rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return login;
	}
	private void processLock(String ikey, int id, Integer idto, String user, boolean lockDB) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		String userLock = null;
		try {
			userLock = getLoginLock(ikey);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InstanceLockedException("FALLO BLOQUEO");
		}
		if (userLock!=null) {
			if (!userLock.equals(user)) {
				//ver si esta en uso
				try {
					boolean existsSession = Session.existsSession(user, factConnDB.getBusiness(), null);
					if (!existsSession) {
						String sqlUpdate = "update locksid set login='" + user.replaceAll("'", "''") + "' where id='" + ikey.replaceAll("'", "''") + "'";
						int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sqlUpdate, false);
						if (rows==0) //el otro usuario se ha conectado y desconectado, quitando sus bloqueos
							processLock(ikey, id, idto, user, lockDB);
					} else {
						boolean isUserTrans = httpGateway.usersTransition.contains(userLock);
						if (isUserTrans)
							System.out.println("usuario contenido en usersTransition");
						System.out.println("ID BLOQUEO " + id);
						String message = "";
						if (lockDB) {
							message = "El objeto "+ id +" está bloqueado por el usuario " + userLock;
						} else {
							if (id>=Constants.MIN_ID_INDIVIDUAL_BDDD) { //implica que idto!=null
								String rdn = getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(id), idto);
								String clase = ik.getClassName(idto);
								message = "El objeto "+ rdn +" de "+ clase +" está bloqueado por el usuario " + userLock;
							} else {
								String clase = ik.getClassName(id);
								message = "El objeto "+ clase +" está bloqueado por el usuario " + userLock;
							}
						}
						throw new InstanceLockedException(message);
					}
				} catch (InstanceLockedException e) {
					throw e;
				} catch (Exception e) {
					e.printStackTrace();
					throw new InstanceLockedException("FALLO BLOQUEO");
				}
			}
		} else {
			String sqlInsert = "insert into locksid(id, login) values('" + ikey.replaceAll("'", "''") + "','" + user.replaceAll("'", "''") + "')";
			try {
				AuxiliarQuery.dbExecUpdate(factConnDB, sqlInsert, false);
			} catch (SQLException e) {
				//algun usuario ha introducido un bloqueo -> itero
						
				//e.printStackTrace();
				boolean exito=true;
				for(int i=1;i<10;i++){
					try{
					java.lang.Thread.sleep(500);
					}catch(Exception te){;}
					try {
						AuxiliarQuery.dbExecUpdate(factConnDB, sqlInsert, false);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						exito=false;
					} 
					//processLock(ikey, id, idto, user, lockDB);
					if(!exito) throw new InstanceLockedException("FALLO BLOQUEO");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new InstanceLockedException("FALLO BLOQUEO");
			}
		}
	}
	
	public void lockObject(int id, Integer idto, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		lockObject(id, idto, user, true);
	}
	
	public void lockObject(int id, Integer idto, String user, boolean isLockFromDB) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		System.out.println("LOCK_TRANS ID " + id + ", USER " + user);
		String ikey = id + "/" + business;
		processLock(ikey, id, idto, user, isLockFromDB);
	}
	
	public void lockObject(HashMap<Integer,HashSet<Integer>> idtoIdos, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		lockObject(idtoIdos, user, true);
	}
	
	public void lockObject(HashMap<Integer,HashSet<Integer>> idtoIdos, String user, boolean isLockFromDB) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		Iterator<Integer> it = idtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = it.next();
			HashSet<Integer> idos = idtoIdos.get(idto);
			Iterator<Integer> it2 = idos.iterator();
			while (it2.hasNext())
				lockObject(it2.next(), idto, user, isLockFromDB);
		}
	}
	
	private void subLockObjectTransDB(int ido, int idto, int prop, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		System.out.println("LOCK_TRANS ID_O " + ido + ", ID_TO " + idto + ", PROP " + prop + ",USER " + user);
		String ikey = ido + "/" + prop + "/" + business;
		boolean lockDB = true;
		processLock(ikey, ido, idto, user, lockDB);
	}
	private void lockObjectDB(int ido, int idto, int prop, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		try{	
			subLockObjectTransDB(ido, idto, prop, user);
		}catch(InstanceLockedException e){
			final int idoThis=ido;
			final int idtoThis=idto;
			final int propThis=prop;
			final String userThis=user;
			// Thread utilizada para bloquear el ido si se ha producido algun error al intentarlo, ya que a veces se produce error de concurrencia de varios usuarios
			Thread unlockThread=new Thread(){
	
				public void run() {
					boolean repeat = true;
					int cont = 1;
					while (repeat) {
						try{
							repeat = false;
							System.err.println("Intento " + cont + " de bloqueo del ido:"+idoThis+", idto:"+idtoThis+", propId:"+propThis+" user:"+userThis);
							sleep(1000);
							subLockObjectTransDB(idoThis, idtoThis, propThis, userThis);
						}catch(Exception e){
							repeat = true;
						}
					}
				}
			};
			unlockThread.start();
		}
	}
	
	public static String getPropertyValue(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int tableId, int idto, String property) throws DataErrorException {
		String value = null;
		Table table = dataBaseMap.getTable(idto);
		//System.out.println("DBG:tableId "+tableId+" idto:"+idto);
		
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select " + generateSQL.getCharacterBegin() + property + generateSQL.getCharacterEnd() +
			" from " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " where " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + tableId;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				value = rs.getString(1);
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER "+property+" DE " + tableId + " de " + idto);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER "+property+" DE " + tableId + " de " + idto);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		if(value!=null) value=value.trim();
		return value;
	}
	
	public static String getRdn(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int tableId, int idto) throws DataErrorException {
		return getPropertyValue(fcdb, dataBaseMap, tableId, idto, Constants.PROP_RDN);
	}
	public static String getRdn(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int tableId, HashSet<Integer> idtos) throws DataErrorException {
		String rdn = null;
		Iterator<Integer> it = idtos.iterator();
		while (rdn==null && it.hasNext()) {
			Integer idto = it.next();
			rdn = getRdn(fcdb, dataBaseMap, tableId, idto);
		}
		return rdn;
	}
	
	public static HashMap<Integer,String> getRdn(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, HashSet<Integer> tableIds, int idto) throws DataErrorException {
		HashMap<Integer,String > rdns = new HashMap<Integer, String>();
		Table table = dataBaseMap.getTable(idto);
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select " + generateSQL.getCharacterBegin() + "tableId" + generateSQL.getCharacterEnd() + "," + Constants.PROP_RDN + 
			" from " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " where " + generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "in (";
		
		Iterator<Integer> itr=tableIds.iterator();
		while(itr.hasNext()){
			Integer tableId=itr.next();
			sql+=+tableId;
			if(itr.hasNext()){
				sql+=",";
			}
		}
		sql+=")";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				int ido = QueryConstants.getIdo(rs.getInt(1), idto);
				String rdn = rs.getString(2);
				rdns.put(ido, rdn);
			}
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER RDN DE " + tableIds + " de " + idto);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER RDN DE " + tableIds + " de " + idto);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return rdns;
	}
	
	public static String getDeletedObjectRdn(FactoryConnectionDB fcdb, int tableId, int idto) throws DataErrorException {
		String value = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select rdn from deleted_objects where " +
			generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd() + "=" + tableId + " AND idto="+idto;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				value = rs.getString(1);
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER OBJECTDELETEDRDN DE " + tableId + " de " + idto);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER OBJECTDELETEDRDN DE " + tableId + " de " + idto);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return value;
	}
	
	public static Integer getDeletedObjectIdo(FactoryConnectionDB fcdb, int idto, String rdn) throws DataErrorException {
		Integer ido = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select "+generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd()+" from deleted_objects where rdn='" + rdn + "' AND idto="+idto;
		System.err.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				ido = QueryConstants.getIdo(rs.getInt(1),idto);
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER OBJECTDELETEDIDO DE " + rdn + " de " + idto);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER OBJECTDELETEDIDO DE " + rdn + " de " + idto);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return ido;
	}
	
	public void unlockObjects(HashSet<Integer> idtos, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		Iterator<Integer> it = idtos.iterator();
		while (it.hasNext()) {
			Integer idto = it.next();
			unlockObject(idto, null, user);
		}
	}
	
	//este metodo no sirve para bloquear clases, solo bloquea individuos
	public void unlockObjects(HashMap<Integer,HashSet<Integer>> idtoIdos, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		Iterator<Integer> it = idtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = it.next();
			HashSet<Integer> idos = idtoIdos.get(idto);
			Iterator<Integer> it2 = idos.iterator();
			while (it2.hasNext())
				unlockObject(it2.next(), idto, user);
		}
	}
	
	public void unlockObject(Integer id, Integer idto, String user) throws SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		try{	
			subUnlockObjectTrans(id, idto, user);
		}catch(InstanceLockedException e){
			final int idThis=id;
			final Integer idtoThis=idto;
			final String userThis=user;
			// Thread utilizada para desbloquear el ido si se ha producido algun error al intentarlo, ya que a veces se produce error de concurrencia de varios usuarios
			Thread unlockThread=new Thread(){
	
				public void run() {
					try{
						System.err.println("Intento 1 de desbloqueo del id:"+idThis+" user:"+userThis);
						sleep(1000);
						subUnlockObjectTrans(idThis, idtoThis, userThis);
					}catch(Exception e){
						try{
							System.err.println("Intento 2 de desbloqueo del id:"+idThis+" user:"+userThis);
							sleep(1000);
							subUnlockObjectTrans(idThis, idtoThis, userThis);
						}catch(Exception ex){
							try{
								logError(userThis, null, ex.getMessage(), null);
							}catch(Exception ex1){
								e.printStackTrace();
								ex.printStackTrace();
								ex1.printStackTrace();
							}
						}
					}
				}
			};
			unlockThread.start();
		}
	}
	private void subUnlockObjectTrans(Integer id, Integer idto, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("UNLOCK ID " + id + ", USER " + user);
		String ikey = id + "/" + business;
		processUnLock(ikey, id, idto, user);
	}
	
	public void unlockObjectDB(int ido, int idto, int prop, String user) throws SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		try{	
			subUnlockObjectTransDB(ido, idto, prop, user);
		}catch(InstanceLockedException e){
			final int idoThis=ido;
			final int idtoThis=idto;
			final int propThis=prop;
			final String userThis=user;
			// Thread utilizada para desbloquear el ido si se ha producido algun error al intentarlo, ya que a veces se produce error de concurrencia de varios usuarios
			Thread unlockThread=new Thread(){
	
				public void run() {
					boolean repeat = true;
					int cont = 1;
					while (repeat) {
						try{
							repeat = false;
							System.err.println("Intento " + cont + " de desbloqueo del ido:"+idoThis+", idto:"+idtoThis+", propId:"+propThis+" user:"+userThis);
							sleep(1000);
							subUnlockObjectTransDB(idoThis, idtoThis, propThis, userThis);
						}catch(Exception e){
							repeat = true;
						}
					}
				}
			};
			unlockThread.start();
		}
	}
	private void subUnlockObjectTransDB(int ido, int idto, int prop, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("UNLOCK ID_O " + ido + ", ID_TO " + idto + ", PROP " + prop + ",USER " + user);
		String ikey = ido + "/" + prop + "/" + business;
		processUnLock(ikey, ido, idto, user);
	}

	private void processUnLock(String ikey, int id, Integer idto, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		String userLock = null;
		try {
			userLock = getLoginLock(ikey);
		} catch (Exception e) {
			e.printStackTrace();
			throw new InstanceLockedException("FALLO DESBLOQUEO");
		}
		if (userLock!=null) {
			if (!userLock.equals(user)) {
				//Esta bloqueado por otro usuario. Compruebo si está vivo
				//String keySession = userLock + "/" + factConnDB.getBusiness();
				boolean existsSession = false;
				try {
					existsSession = Session.existsSession(user, factConnDB.getBusiness(), null);
				} catch (Exception e) {
					throw new InstanceLockedException("FALLO DESBLOQUEO");
				}
				if (existsSession) {
					boolean isUserTrans = httpGateway.usersTransition.contains(userLock);
					if (isUserTrans)
						System.out.println("usuario contenido en usersTransition");
					throw new InstanceLockedException("NO ES POSIBLE DESBLOQUEAR. ESTA BLOQUEADO POR EL USUARIO" + userLock);
				}
			}
			
    		String sqlRemove = "delete from locksid where id='" + ikey.replaceAll("'", "''") + "'";
			try {
				AuxiliarQuery.dbExecUpdate(factConnDB, sqlRemove, false);
				//System.out.println("ENCONTRADO LOCK "+id + ", desbloqueado por "+user);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					if (id>=Constants.MIN_ID_INDIVIDUAL_BDDD) {
						String rdn = getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(id), idto);
						throw new InstanceLockedException("FALLO AL DESBLOQUEAR EL INDIVIDUO " + rdn + ". Vuelva a intentarlo");
					} else {
						String nameClass = ik.getClassName(id);
						throw new InstanceLockedException("FALLO AL DESBLOQUEAR " + nameClass + ". Vuelva a intentarlo");
					}
				} catch (DataErrorException e1) {
					throw new InstanceLockedException("FALLO DESBLOQUEO");
				}
			}
		} else {
			//System.out.println("NO ENCONTRADO LOCK "+id);
			//Si no lo ha encontrado no hace nada
		}
	}

	public boolean putMsguid(String msguid) throws SQLException, NamingException {
		boolean put = false;
		String sqlSelect = "SELECT msguid FROM operationsid where msguid='" + msguid + "'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlSelect);
			if (!rs.next()) {
				try {
					String sql = "INSERT INTO operationsid(msguid) values('" + msguid + "')";
					AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
					put = true;
				} catch (Exception e) {
					;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return put;
	}
	
	public Changes serverTransitionObject(Integer userRol, String user, IndividualData dIndiv, Integer windowSession,
			boolean migration, boolean keepTableIds, String replicaSource, String msgguid)
		throws NotFoundException, SystemException, ApplicationException,
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException,
			CommunicationException, InstanceLockedException, OperationNotPermitedException {
		Changes changes = null;
		try {
			changes = serverTransitionObjectImp(user, dIndiv, null,windowSession, migration, false,keepTableIds, replicaSource);
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		return changes;
	}


	/*public void deleteObligated(Integer userRol, String user, ArrayList<Integer> aIdos) 
	throws SQLException, NamingException, DataErrorException, NotFoundException, SystemException, 
	ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, 
	RemoteSystemException, CommunicationException, InstanceLockedException, JDOMException, ParseException, OperationNotPermitedException, InterruptedException {
		//System.out.println("Inicio de la funcion deleteObligated");
		TransitionObject tObj = new TransitionObject(factConnDB, ik, ir, this, userRol, user, debugMode, generateSQL);
		tObj.subDelAllFactsInstanceObligated(aIdos);
		//System.out.println("Fin de la funcion deleteObligated");
	}*/

	/*private int getSigID(Element root) {
		String idRoot = root.getAttributeValue(QueryConstants.INDEX);
		int sigIDRoot = 0;
		if (idRoot!=null)
			sigIDRoot = Integer.parseInt(idRoot);
		Iterator it = root.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			int sigIDResto = getSigID(child);
			if (sigIDRoot<sigIDResto)
				sigIDRoot = sigIDResto;
		}
		return sigIDRoot;
	}

	private boolean replaceWhere(Element elem, Element where) {
		boolean existe = false;
		String valueWhere = where.getAttributeValue(QueryConstants.VALUE) == null ? where.getText() : where.getAttributeValue(QueryConstants.VALUE);
		Iterator it = elem.getChildren(QueryConstants.WHERE).iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			String valueChild = child.getAttributeValue(QueryConstants.VALUE) == null ? child.getText() : child.getAttributeValue(QueryConstants.VALUE);
			if (valueChild.contains("(VALUE)")) {
				String valueChildReplace = valueChild.replaceAll("(VALUE)", valueWhere);

				Interpreter i=new Interpreter();
				Integer valueChildReplaceInt = null;
				try {
					valueChildReplaceInt = (Integer)i.eval(valueChildReplace);
				} catch (EvalError e) {
					e.printStackTrace();
				}
				if (child.getAttributeValue(QueryConstants.VALUE) == null)
					child.setText(String.valueOf(valueChildReplaceInt));
				else
					child.setAttribute(QueryConstants.VALUE,String.valueOf(valueChildReplaceInt));
				existe = true;
			}
		}
		return existe;
	}

	private void addWhere(Element query, ArrayList<Element> pathWhere, HashMap<Integer,Integer> idsWhere) {
		//findElementsByAts de jdomParser por cada class superior a los ava
		//si el tamaño mayor que uno comprueba la ruta entera
		int sigID = getSigID(query)+1;
		for (int i=0;i<pathWhere.size();i++) {
			Element where = pathWhere.get(i);
			Element child = null;
			if (where.getChild(QueryConstants.CLASS)!=null)
				child = where.getChild(QueryConstants.CLASS);
			else if (where.getChild(QueryConstants.XOR)!=null)
				child = where.getChild(QueryConstants.XOR);
			Iterator it = query.getChildren().iterator();
			while (it.hasNext()) {
				Element structure = (Element)it.next();
				String[] names = new String[2];
				names[0] = QueryConstants.CLASS;
				names[1] = QueryConstants.XOR;
				ArrayList<Element> coincidentes = jdomParser.findElementsByAts(structure, names, child.getAttributes(), true);
				if (coincidentes.size()==1) {
					Element cloneWhere = jdomParser.cloneNode(where);
					idsWhere.put(Integer.parseInt(cloneWhere.getAttributeValue(QueryConstants.INDEX)), sigID);
					cloneWhere.setAttribute(QueryConstants.INDEX,String.valueOf(sigID));
					sigID++;
					if (!replaceWhere(coincidentes.get(0), cloneWhere))
						coincidentes.get(0).addContent(cloneWhere);
				} else if (coincidentes.size()>1) {
					for (int j=0;j<coincidentes.size();j++) {
						Element coincidente = coincidentes.get(j);
						boolean coincide = coincideRuta(coincidente, child);
						if (coincide) {
							Element cloneWhere = jdomParser.cloneNode(where);
							idsWhere.put(Integer.parseInt(cloneWhere.getAttributeValue(QueryConstants.INDEX)), sigID);
							cloneWhere.setAttribute(QueryConstants.INDEX,String.valueOf(sigID));
							sigID++;
							if (!replaceWhere(coincidente, cloneWhere))
								coincidente.addContent(cloneWhere);
						}
					}
				}
			}
		}
	}

	private boolean coincideRuta(Element coincidente, Element pathWhereChild) {
		Element coincidenteP = coincidente.getParent();
		Element childC = null;
		if (pathWhereChild.getChild(QueryConstants.CLASS)!=null)
			childC = pathWhereChild.getChild(QueryConstants.CLASS);
		else if (pathWhereChild.getChild(QueryConstants.XOR)!=null)
			childC = pathWhereChild.getChild(QueryConstants.XOR);
		boolean allAtrib = true;
		if (coincidenteP!=null && childC!=null) {
			allAtrib = jdomParser.allAtribs(coincidenteP.getAttributes(), childC) && 
							coincideRuta(coincidenteP,childC);
		}
		return allAtrib;
	}

	private Element getPathInverseWhere(Element root) {
		Element clone = jdomParser.cloneNode(root);
		Element parent = root.getParent();
		if (parent!=null && (parent.getName().equals(QueryConstants.CLASS) || parent.getName().equals(QueryConstants.XOR)))
			clone.addContent(getPathInverseWhere(parent));
		return clone;
	}

	private void getPaths(Element classQueryWhere, ArrayList<Element> pathWhere) {
		//guarda en el ArrayList los paths de cada ava
		Iterator it = classQueryWhere.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			String name = child.getName();
			if (name.equals(QueryConstants.WHERE)) {
				Element path = getPathInverseWhere(child);
				pathWhere.add(path);
			} else if (name.equals(QueryConstants.CLASS) || name.equals(QueryConstants.XOR)) {
				getPaths(child, pathWhere);
			}
		}
	}

	private void preAddOPs(Element query, Element viewQueryWhere, HashMap<Integer,Integer> idsWhere) {
		//buscar un id en viewQueryWhere
		Element wherePresentation = jdomParser.firstElementWithAt(viewQueryWhere, QueryConstants.ID_CONDITION, true);
		if (wherePresentation!=null) {
			//mirar en el mapa idsWhere
			String idWhere = wherePresentation.getAttributeValue(QueryConstants.ID_CONDITION);
			Integer idQuery = idsWhere.get(Integer.parseInt(idWhere));
			if (idQuery!=null) {
				Iterator it = query.getChildren().iterator();
				while (it.hasNext()) {
					Element structureQuery = (Element)it.next();
					//y si coincide con algun id(de nodo where) de structure de query estoy en el structure correcto
					Element attributeID = jdomParser.findElementByAt(structureQuery, QueryConstants.INDEX, String.valueOf(idQuery), true);
					if (attributeID!=null) {
						Element presentationQuery = structureQuery.getChild(QueryConstants.PRESENTATION);
						Element viewQuery = presentationQuery.getChild(QueryConstants.VIEW);
						addOPs(viewQuery, viewQueryWhere, idsWhere);
						break;
					}
				}
			}
		}
	}
	private void addOPs(Element viewQuery, Element viewQueryWhere, HashMap<Integer,Integer> idsWhere) {
		Iterator itR = viewQueryWhere.getChildren().iterator();
		while (itR.hasNext()) {
			Element childQueryWhere = (Element)itR.next();
			String name = childQueryWhere.getName();
			if (name.equals(QueryConstants.LOGIC_WHERE)) {
				//clona nodo OP y sus hijos cambiando los IDs
				Element childQueryOp = childQueryWhere.getChild(QueryConstants.OP);
				Element childRootClone = cloneOP(childQueryOp, idsWhere);
				viewQuery.addContent(childRootClone);
			} else if (name.equals(QueryConstants.VIEW)) {
				Iterator itQ = viewQuery.getChildren(QueryConstants.VIEW).iterator();
				while (itQ.hasNext()) {
					Element childQuery = (Element)itQ.next();
					if (StringUtils.equals(childQueryWhere.getAttributeValue(QueryConstants.INDEX), 
							childQuery.getAttributeValue(QueryConstants.INDEX))) {
						addOPs(childQuery, childQueryWhere, idsWhere);
						break;
					}
				}
			}
		}
	}

	private Element cloneOP(Element root, HashMap<Integer,Integer> idsWhere) {
		Element rootClone = new Element(root.getName());
		List atributos = root.getAttributes();
		for (int i=0;i<atributos.size();i++) {
			Attribute atributo = (Attribute)atributos.get(i);
			String nameAtrib = atributo.getName();
			if (nameAtrib.equals(QueryConstants.ID_CONDITION))
				rootClone.setAttribute(QueryConstants.ID_CONDITION, String.valueOf(idsWhere.get(Integer.parseInt(atributo.getValue()))));
			else
				rootClone.setAttribute(nameAtrib, atributo.getValue());
		}
		Iterator it = root.getChildren().iterator();
		while(it.hasNext()) {
			Element elem = (Element)it.next();
			rootClone.addContent(cloneOP(elem,idsWhere));
		}
		return rootClone;
	}*/

	public HashMap<String,String> serverGetPrePrintSequence(String className) throws NotFoundException, IncoherenceInMotorException, SystemException {
		HashMap<String,String> res = null;
		try {
			res = getPrePrintSequence(className);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NumberFormatException e) {			
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		return res;
	}
	
	public HashMap<String,String> getPrePrintSequence(String className) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException, JDOMException {
		HashMap<String,String> reportAttributes = null;
		
		//if (ik.isSpecialized(userTask, Constants.IDTO_REPORT))
		if (className!=null) {
			String rdnInforme = className.substring(3, className.length());
			reportAttributes = getPrePrintSequenceDB(rdnInforme);
		}
		System.out.println("reportAttributesPrePrint " + reportAttributes);
		return reportAttributes;
	}

	public HashMap<String,String> serverGetReport(Element queryWhere, String user, Integer userTask, String className, /*String nameProject,*/ boolean directImpresion, Integer idoFormat, boolean printSequence,boolean ejecuta_preseq) throws SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException {
		HashMap<String,String> res = null;
		try {
			String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
			//String serverHomeDir = "D:\\Archivos de programa\\jboss-4.0.5.GA-2\\server\\default";
			String path = serverHomeDir + "\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\";// + nameProject;
			res = report(queryWhere, user, userTask, className, path, directImpresion, idoFormat);
		} catch (SQLException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IOException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_INOUT, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NoSuchColumnException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JRException e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		return res;
	}

	private void adaptaIdos(Element root){
		Iterator itr = root.getChildren().iterator();
		while (itr.hasNext()) {
			org.jdom.Element child = (org.jdom.Element) itr.next();
			if (child.getAttributeValue("ID_O") != null){
				String[] idos=child.getAttributeValue("ID_O").split(",");
				String newStr="";
				for(int i=0;i<idos.length;i++){
					int ido=new Integer(idos[i]);
					long newIdo=QueryConstants.getTableId(ido)*1000L+QueryConstants.getIdto(ido);
					newStr+=(i>0?",":"")+newIdo;
				}
				child.setAttribute("ID_O",newStr);
			}
			adaptaIdos(child);
		}
	}

	public HashMap<String,String> report(Element queryWhere, String user, int userTask, String className, String path, boolean directImpresion, Integer idoFormat) throws ParseException, JDOMException, 
			SQLException, NamingException, IOException, NotFoundException, IncoherenceInMotorException, NumberFormatException,
			IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, OperationNotPermitedException, NoSuchColumnException, JRException {
		
		if(queryWhere!=null){
			//PARCHE idos codificados. decodificar idos
			adaptaIdos(queryWhere);
		}
		boolean dynamic = false;
		HashMap<String,String> params = new HashMap<String,String>();
		ArrayList<String> paramsExcel = new ArrayList<String>();
		String titleExcel = null;
		//System.out.println("Entra en report");

		System.out.println(userTask);
		String rdnInforme = null;
		//if (!ik.isSpecialized(userTask, Constants.IDTO_REPORT))
		if (className==null)
			dynamic = true;
		else
			rdnInforme = className.substring(3, className.length());

		HashMap<String,String> reportAttributes = null;
		String idMaster = "1";
		String nameMaster = "1";
		int nCopies = 1;
		boolean displayPrintDialog = false;
		String printSequence = null;
		String idReportExcel = null;
		String impresora=null;
		
		if (queryWhere!=null)
			System.out.println("SYSOUAMI: QUERY WHERE->"+jdomParser.returnXML(queryWhere));
		else
			System.out.println("SYSOUAMI: QUERY WHERE es nulo");
		System.out.println("idoFormat " + idoFormat);
		String format = null;
		if (idoFormat!=null){
			//format = ((IKnowledgeBaseInfoServer)ik).getFormat(idoFormat);
			format = getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(idoFormat), QueryConstants.getIdto(idoFormat));
		}
		
		boolean generateExcel = format!=null && format.equals(Constants.EXCEL);
		//obtener los reports y guardarlo en jrxmls
		Map<String, String> jrxmls = new HashMap<String, String>();
		boolean havePathsJrxmls = false;
		if(dynamic) {
			System.out.println("Entra en dinamico");
			String nameClassRoot = null;
			Element classRoot = jdomParser.findFirstOf(queryWhere, QueryConstants.CLASS, true);
			if (classRoot!=null) {
				nameClassRoot = classRoot.getAttributeValue(QueryConstants.NAME);
				if (nameClassRoot!=null)
					nameMaster = "listado_" + nameClassRoot.replaceAll("_", " ").toLowerCase();
			}
			//de queryWhere obtener sql usando QueryBasic
			if (generateExcel) {
				ArrayList<Element> classes = jdomParser.elements(queryWhere, QueryConstants.CLASS, true);
				String titleReport = nameClassRoot.replaceAll("_", " ").toUpperCase();
				JrxmlParser jrParserMaster = new JrxmlParser();
				jrParserMaster.setTitleReport(titleReport);
				paramsExcel = jrParserMaster.fillConditions(classes, ik, factConnDB, dataBaseMap);
				titleExcel = "LISTADO DE " + titleReport;
			}
		} else {
			//coger de bd el diseño correspondiente a la userTask
			QueryReport queryReport = getQueryReportDB(rdnInforme);
			impresora=queryReport.getImpresora();
			idReportExcel = queryReport.getIdReportExcel();
			boolean formatData = false;
			if (generateExcel) {
				//para obtener idExcel buscar en bd el que tiene generateExcel=true
				if (idReportExcel==null) {
					generateExcel = false; //porque ya esta en formato excel
					format = Constants.EXCEL;
					formatData = true;
				}
			}
			System.out.println("format " + format);
			System.out.println("generateExcel " + generateExcel);
			
			//obtener tambien el id del master
			idMaster = queryReport.getIdMaster();
			nameMaster = rdnInforme;

			if (queryWhere!=null) {
				System.out.println("SYSOUAMI: QUERY WHERE->"+jdomParser.returnXML(queryWhere));
				//System.out.println("SYSOUAMI: MAP DB->"+jdomParser.returnXML(map));
				//if (!generateExcel)
					params = getParams(queryWhere, userTask, user, formatData);
				if (generateExcel) {
					ArrayList<Element> classes = jdomParser.elements(queryWhere, QueryConstants.CLASS, true);
					JrxmlParser jrParserMaster = new JrxmlParser();
					paramsExcel = jrParserMaster.fillConditions(classes, ik, factConnDB, dataBaseMap);
					//titleExcel=jrParserMaster.getTitleReport(); devuelve PARAMETROS (que es la clase root) porque se obtiene de queryWhere
					titleExcel = rdnInforme.replaceAll("_", " ");
				}
				//System.out.println("SYSOUAMI: QUERY CON VALORES->"+jdomParser.returnXML(query));
			}
			
			ArrayList<IdReport> reports = queryReport.getReport();
			for (int i=0;i<reports.size();i++) {
				IdReport idr = reports.get(i);
				//System.out.println("SYSOUAMI: reports:"+ idr);
				jrxmls.put(idr.getId(), idr.getReport());
			}
			havePathsJrxmls = true;
			if (!generateExcel) {
				String nCopiesStr = params.get("n_copies");
				if (nCopiesStr!=null)
					nCopies = Integer.parseInt(nCopiesStr);
				else
					nCopies = queryReport.getNCopies();
				printSequence = queryReport.getPrintSequence();
				displayPrintDialog = queryReport.getDisplayPrintDialog();
			}
		}
		nameMaster = Auxiliar.removeStringAccents(nameMaster); 

		ConnectionDB conDb = null;
		try {
			conDb = factConnDB.createConnection(false);
			Connection con = conDb.getBusinessConn();
			
			if (generateExcel) {
				ResultSet data = null;
				HashMap<String,DataInfo> infoResult = null;
				if (dynamic) {
					Element query = queryWhere;
					QueryService qs = new QueryService(factConnDB, ik, dataBaseMap);
					String sql = qs.dbFindQuery(query,false);
					infoResult = qs.getDataInfoXName();
					System.out.println(sql);
					Iterator<String> it = infoResult.keySet().iterator();
					while (it.hasNext()) {
						String name = it.next();
						DataInfo dInfo = infoResult.get(name);
						System.out.println(name + "->" + dInfo);
					}
					Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
					data = st.executeQuery(sql);
				} else {
					//hay que obtener la query del diseño
					//si se generaExcel de un subreport pasar parametro a query hija
					
					//idReportExcel -> obtener query que tenga ese id
					String pathDesign = jrxmls.get(idReportExcel);
//					File file = new File(pathDesign);
//					String designISO = jdomParser.readXMLToString(file);
					String designISO = Auxiliar.readFile(pathDesign);
					/*convert from UTF-8 to ISO-8859-1*/
					String design = new String(designISO.getBytes("ISO-8859-1"), "UTF-8");
					Element designElem = jdomParser.readXML(design).getRootElement();
					infoResult = new HashMap<String, DataInfo>();
					
					//obtener query del diseño
					//los parametros en paramsExcel, si es subreport buscar subreport y ver con que parametro del principal coincide
					//si llamo igual a los parametros del informe principal que del subreport no es necesario hacer eso, simplemente paso paramsExcel
					//construir infoResults -> Tengo que ir mirando fields xa ver el tipo de dato 
					//y el numero de columna coincide con el orden de fields
					String sql = getQueryOfReport(designElem, infoResult);
					sql = replaceParams(sql, params);
					Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
					data = st.executeQuery(sql);
				}
				reportAttributes = ViewReports.viewExcel(business,user, infoResult, data, nameMaster, path, titleExcel, paramsExcel);
			} else {
				if (dynamic) {
					Statement st = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
					QueryService qs = new QueryService(factConnDB, ik, dataBaseMap);
					String sql = qs.dbFindQuery(queryWhere,false);
					HashMap<String,DataInfo> infoResult = qs.getDataInfoXName();
					ResultSet data = st.executeQuery(sql);
					//quiere data para dar anchos de columna de forma dinamica;
					IdReport idr = modifyDynamicJrxml(queryWhere, sql, infoResult, data, userTask);
					jrxmls.put(idr.getId(), idr.getReport());
					//System.out.println(jrxmls.get(idr.getId()).toString());
				}
				String idioma=params.get("idioma_rdn");
				reportAttributes = ViewReports.view(business,user,jrxmls,havePathsJrxmls,userTask,con,idMaster,nameMaster,path,params,directImpresion,nCopies,impresora,displayPrintDialog,printSequence,format,idioma);
			}
		} finally {
			if (conDb!=null)
				factConnDB.close(conDb);
		}
		//devolver ruta del fichero junto con sus caracteristicas de impresion
		System.out.println("reportAttributes " + reportAttributes);
		return reportAttributes;
	}
	private String replaceParams(String sql, HashMap<String,String> params) {
		Iterator<String> it = params.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = params.get(key);
			System.out.println("antes -> $P{" + key + "}");
			System.out.println("despues -> '" + value + "'");
			
			Pattern p = Pattern.compile("\\$P\\!\\{" + key + "\\}");
			Matcher m = p.matcher(sql);
			sql = m.replaceAll(value);
			p = Pattern.compile("\\$P\\{" + key + "\\}");
			m = p.matcher(sql);
			sql = m.replaceAll("'"+value.replaceAll(",", "','")+"'");
			//StringUtils.replace(sql, key, "'" + value + "'");
			//sql.replaceAll("\\$P\\{" + key + "\\}", "'" + value + "'");
		}
		Pattern p = Pattern.compile("\\$P\\!?\\{[^}]*\\}");
		Matcher m = p.matcher(sql);
		while (m.find()){
			System.out.println("Group -> " + m.group());
		}
		sql = m.replaceAll("null");
		System.out.println("new sql " + sql);
		return sql;
	}
	private String getQueryOfReport(Element design, HashMap<String,DataInfo> infoResult) throws UnsupportedEncodingException {
		Namespace nameSpace = Namespace.getNamespace("http://jasperreports.sourceforge.net/jasperreports");
		
		Element queryElem = design.getChild("queryString", nameSpace);
		org.jdom.CDATA cData = (org.jdom.CDATA)queryElem.getContent().get(1);
		String sql = cData.getTextTrim();
		
		int columnQuery = 1;
		int column = 1;
		//iterar x fields para obtener infoResults
		Iterator it = design.getChildren("field", nameSpace).iterator();
		//System.out.println("readDesign");
		while (it.hasNext()) {
			Element fieldElem = (Element)it.next();
			String name = fieldElem.getAttributeValue("name");
			String type = fieldElem.getAttributeValue("class");
			//System.out.println("name " + name + ", type " + type + ", column " + column);
			type = type.substring(type.lastIndexOf(".")+1, type.length());
			if (!name.contains("tableId")) {
				DataInfo dataInfo = new DataInfo(type,columnQuery,column);
				infoResult.put(name, dataInfo);
				column++;
			}
			columnQuery++;
		}
		return sql;
	}
	
	private HashMap<String,String> getPropertiesOfQuery(Element queryWhere) throws SQLException, NamingException {
		HashMap<String,String> properties = new HashMap<String,String>();
		Element structure = queryWhere.getChild(QueryConstants.STRUCTURE);
		if (structure!=null) {
			Element clase = structure.getChild(QueryConstants.CLASS);
			if (clase!=null)
				getPropertiesOfQueryReq(clase, properties);
		}
		return properties;
	}
	private void getPropertiesOfQueryReq(Element clase, HashMap<String,String> properties) throws SQLException, NamingException {
		Iterator it = clase.getChildren().iterator();
		while (it.hasNext()) {
			Element child = (Element)it.next();
			Integer prop = null;
			String propStr = child.getAttributeValue(QueryConstants.PROP);
			if (propStr!=null) {
				prop = Integer.parseInt(propStr);
				String propName = child.getAttributeValue(QueryConstants.NAME_PROP);
				if (prop<0)
					properties.put(propStr, propName);
			}
			if (child.getName().equals(QueryConstants.CLASS)){
				
				getPropertiesOfQueryReq(child, properties);
			}
		}
	}
	
	private HashMap<String,String> getParams(Element queryWhere,int userTask, String user, boolean formatData) throws NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, InstanceLockedException, OperationNotPermitedException{
		System.out.println("Inicio de getParams");
		HashMap<String,String> result = new HashMap<String, String>();
		//buscar en queryWhere los nodos que tienen algo fijado
		//pongo name_prop como nombre del parametro, ya que cuando se importo el informe se puso el parametro con este nombre
		
		//iterar por los nodos de queryWhere que tengan prop negativa
		HashMap<String,String> properties = getPropertiesOfQuery(queryWhere);
		Iterator<String> itprop = properties.keySet().iterator();
		while(itprop.hasNext()){
			String idProp=(String)itprop.next();
			String nameProp = (String)properties.get(idProp);
			String name = nameProp.substring(0, nameProp.indexOf("@"));

			System.out.println(jdomParser.returnXML(queryWhere));
			Element where=jdomParser.findElementByAt(queryWhere, QueryConstants.WHERE, QueryConstants.PROP, idProp, true);
			Element classWhere=null;
			if (where==null){
				classWhere=jdomParser.findElementByAt(queryWhere, QueryConstants.CLASS, QueryConstants.PROP, idProp, true);
			}
			System.out.println("SYSOUAMI:GETPARAMS - IDPROP="+idProp);
			if (where!=null){
				System.out.println("SYSOUAMI:GETPARAMS - ENTRA POR WHERE EN EL QWHERE CON IDPROP="+idProp);

				/*Integer idto = Integer.parseInt(where.getAttributeValue(QueryConstants.ID_TM_RULEENGINE));
				if (idto.equals(Constants.IDTO_DATE) || idto.equals(Constants.IDTO_TIME) || idto.equals(Constants.IDTO_DATETIME))
					idto = Constants.IDTO_DATE;
				System.out.println("SYSOUAMI:GETPARAMS - IDTO="+idto);*/
				
				if (where.getAttributeValue(QueryConstants.VAL_MIN)!=null){
					String value_min=where.getAttribute(QueryConstants.VAL_MIN).getValue();
					String value_max=where.getAttribute(QueryConstants.VAL_MAX).getValue();
					/*String pattern = QueryConstants.getPattern(idto);
					if (pattern.length()>0){
						value_min=QueryConstants.secondsToDate(value_min, pattern);
						value_max=QueryConstants.secondsToDate(value_max, pattern);
					}*/
					result.put(name, value_min+","+value_max);
					//result.put(whereDB.getAttributeValue(QueryConstants.NAME), JrxmlParser.normalizeLabel(value_min+","+value_max));					
				}else if(where.getAttributeValue(QueryConstants.VALUE)!=null){
					String value=where.getAttribute(QueryConstants.VALUE).getValue();
					/*String pattern = QueryConstants.getPattern(idto);
					if (pattern.length()>0){
						value=QueryConstants.secondsToDate(value, pattern);
					}*/				
					result.put(name, value);
					//result.put(attrDB.getAttributeValue(QueryConstants.NAME), JrxmlParser.normalizeLabel(value));
				}else{
					String textcontains=where.getText();
					/*String pattern = QueryConstants.getPattern(idto);
					if (pattern.length()>0){
						textcontains=QueryConstants.secondsToDate(textcontains, pattern);
					}*/
					result.put(name, textcontains);
					//result.put(whereDB.getAttributeValue(QueryConstants.NAME), JrxmlParser.normalizeLabel(textcontains));
				}
			}else if (classWhere!=null){
				System.out.println("SYSOUAMI:GETPARAMS - ENTRA POR CLASS EN EL QWHERE CON IDPROP="+idProp);
				String values = classWhere.getAttributeValue(QueryConstants.ID_O);
				System.out.println("PARAM previo"+name+" "+values);
				if(name.contains("idioma")&&values.matches("\\d.+")){
					int ido=Integer.parseInt(values);
					//los idos ya han sido descomprimidos en "adaptaIdos", no debo aplicar metodos con descompresion
					String idioma_label=getValueRdn(""+QueryConstants.getTableIdNoCompress(ido), QueryConstants.getIdtoNoCompress(ido));
					result.put("idioma_rdn", idioma_label);
					System.out.println("IDIOMA PARAM"+values+" "+idioma_label);
				}
				//result.put(classDB.getAttributeValue(QueryConstants.NAME), JrxmlParser.normalizeLabel(valuesRdn));
				result.put(name, values);
			}
		}
		
		result.put("user", user);
		result.put("path_userFiles", "dyna/" + Constants.folderUserFiles + "/" + business + "/");
		if (formatData)
			result.put("format_data", "true");
		
		System.out.println("SYSOUAMI:GETPARAMS");
		Set<String> keysString=result.keySet();
		Iterator<String> its=keysString.iterator();
		while(its.hasNext()){
			String key=its.next();
			String value=result.get(key);
			System.out.println("SYSOUAMI: KEY->"+key+" VALUE->" + value);
		}
		System.out.println("Fin de getParams");
		return result;
	}

	private String getValueRdn(String values, int idto) throws DataErrorException, NumberFormatException, SQLException, NamingException {
		System.out.println("SYSOUAMI:GETPARAMS - VALUES="+values);
		String valuesRdn=null;
		String[] valuesSplit=values.split(",");
		for(int j =0; j<valuesSplit.length;j++){
			
			int tableId = Integer.parseInt(valuesSplit[j]);
			//obtener clases a las que puedo apuntar
			if (valuesRdn!=null){
				String rdn = getRdn(factConnDB, dataBaseMap, tableId, idto);
				//Property pr=ik.getProperty(Integer.parseInt(valuesSplit[j]), Constants.IdPROP_RDN, null, Constants.USER_SYSTEM, userTask, ds);
				//TODO Mirar porque coge el rdn con dos stringvalue
				//valuesRdn=valuesRdn+", "+((StringValue) pr.getValue()).getValue();
				valuesRdn=valuesRdn+", "+rdn;//getValues().getFirst()).getValue();
			}else{
				//Property pr=ik.getProperty(Integer.parseInt(valuesSplit[j]), Constants.IdPROP_RDN, null, Constants.USER_SYSTEM, userTask, ds);
				//valuesRdn=((StringValue) pr.getValue()).getValue();//getValues().getFirst()).getValue();
				String rdn = getRdn(factConnDB, dataBaseMap, tableId, idto);
				valuesRdn=rdn;//getValues().getFirst()).getValue();
			}
			System.out.println("SYSOUAMI:GETPARAMS - VALUERDN="+valuesRdn);
		}
		return valuesRdn;
	}
	
	private IdReport modifyDynamicJrxml(Element queryWhere, String sql, HashMap<String,DataInfo> infoResult, ResultSet data, int uTask) throws NotFoundException, 
	IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
	SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, 
	DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		System.out.println("Entra en modify");
		IdReport idr=new IdReport("1","");
		JrxmlParser jrParserMaster=new JrxmlParser();
		jrParserMaster.createJrxmlParser(queryWhere, sql, infoResult, data, ik, factConnDB, dataBaseMap);
		idr.setReport(jrParserMaster.toString());
		//System.out.println(jrParserMaster.toString());
		return idr;
	}
	private QueryReport getQueryReportDB(String rdnInforme) throws SQLException, NamingException, JDOMException, IOException {
		Integer tableIdMaster = null;
		String idMaster = null;
		int nCopies=1;
		String impresora=null;
		Boolean fImpresion=null;
		String secPostImpresion=null;
		String idReportExcel=null;

		String cB = generateSQL.getCharacterBegin();
		String cE = generateSQL.getCharacterEnd();
		
		String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
		String userFiles = serverHomeDir + "\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\" + Constants.folderUserFiles + "/" + business + "/";
		
		ArrayList<IdReport> aReport = new ArrayList<IdReport>();
		String sql = "select " + cB + "tableId" + cE + ", archivo, " + cB + "formulario_impresión" + cE + ", " + 
			cB + "copias_impresión" + cE + ", " + cB + Constants.PROP_REPORT_POSTPRINT + cE + ", " + cB + "importación_excel" + cE + ",impresora from informe " +
			"where " + cB + "rdn" + cE + "='" + rdnInforme + "'";
		
		ConnectionDB con = null; 
		Statement st = null;
		ResultSet rs = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			System.out.println(sql);
			if (rs.next()) {
				tableIdMaster = rs.getInt(1);
				idMaster = rdnInforme;
				String pathReport = userFiles + rs.getString(2);
				fImpresion = rs.getBoolean(3);
				nCopies = rs.getInt(4);
				secPostImpresion = rs.getString(5);
				Boolean impExcel = rs.getBoolean(6);
				if (impExcel)
					idReportExcel = rdnInforme;
				impresora=rs.getString(7);
				IdReport idR = new IdReport(rdnInforme,pathReport);
				aReport.add(idR);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		sql = "SELECT " + cB + "rdn" + cE + ", archivo, " + cB + "importación_excel" + cE + " FROM subinforme " +
				"WHERE " + cB + "informeId" + cE + "=" + tableIdMaster;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			System.out.println(sql);
			while (rs.next()) {
				String id = rs.getString(1);
				String pathSubReport = userFiles + rs.getString(2);
				Boolean impExcel = rs.getBoolean(3);
				if (impExcel)
					idReportExcel = id;

				IdReport idR = new IdReport(id,pathSubReport);
				aReport.add(idR);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		System.out.println("idReportExcel " + idReportExcel);
		return new InstanceService.QueryReport(idMaster,aReport,nCopies,fImpresion,secPostImpresion,idReportExcel,impresora);
	}
	
	/**
	 * Obtiene el report cuyo id_to se pasa por parámetro y todos los subreports asociados
	 * @param reportIdto Identificador del report a extraer de la base de datos.
	 * @return Report obtenido al consultar la base de datos o <code>null</code> si no se ha
	 * encontrado ninguno.
	 */
	public ArrayList<String> getReportJRXML(Integer reportIdto){
		ArrayList<String> reports = new ArrayList<String>();
		
		//Consultas que se van a realizar
		String sqlMainReport = "SELECT report FROM s_report WHERE id_to=" + reportIdto;
		String sqlSubReports = "SELECT subreport FROM s_subreport WHERE id_to=" + reportIdto;
		
		// Objetos que van a permitir consultar a base de datos
		ConnectionDB connectionDB = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try{
			//Abrimos la conexión e intentamos obtener el report al que hace referencia el idto pasado.
			connectionDB = factConnDB.createConnection(true);
			statement = connectionDB.getBusinessConn().createStatement();
			
			resultSet = statement.executeQuery(sqlMainReport);			
			if (resultSet.next()){
				String report = resultSet.getString(1);
				reports.add(report);
			}			
			resultSet.close();
			
			// Ahora intentamos obtener los subreports que tengan tambien ese idto
			resultSet = statement.executeQuery(sqlSubReports);
			while(resultSet.next()){
				String subReport = resultSet.getString(1);
				reports.add(subReport);
			}
		} catch (SQLException e) {
			System.err.println("Error al intentar obtener el report de la base de datos.");
			e.printStackTrace();
		} catch (NamingException e) {
			System.err.println("Error al intentar obtener el report de la base de datos.");
			e.printStackTrace();
		} finally {
			if (resultSet != null)
				try {
					resultSet.close();
				} catch (SQLException e) {
					System.err.println("Error al intentar cerrar el ResultSet");
					e.printStackTrace();
				}
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println("Error al intentar cerrar el Statement");
					e.printStackTrace();
				}
			if (connectionDB!=null)
				try {
					factConnDB.close(connectionDB);
				} catch (SQLException e) {
					System.err.println("Error al intentar cerrar la conexión");
					e.printStackTrace();
				}
		}
		
		return reports;
	}
	
	private HashMap<String,String> getPrePrintSequenceDB(String rdnInforme) throws SQLException, NamingException, JDOMException {
		//Element query = null;
		HashMap<String,String> reportAttributes = new HashMap<String, String>();

		//select para obtener a partir del idto la query
		String sql = "SELECT " + generateSQL.getCharacterBegin() + Constants.PROP_REPORT_PREPRINT + generateSQL.getCharacterEnd() + 
			" FROM informe WHERE rdn='" + rdnInforme + "'";
		ConnectionDB con = null; 
		Statement st = null;
		ResultSet rs = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String prePrintSequence = rs.getString(1);
				if (prePrintSequence!=null)
					reportAttributes.put(QueryConstants.PREPRINT_SEQUENCE, prePrintSequence);
			}
			
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return reportAttributes;
	}

	public selectData serverGetTasks(Integer userRol, String user) throws NotFoundException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		System.out.println("Inicio de la funcion serverGetTasks");
		selectData sd = null;
		try {
			sd = Asigned.getTasks(this, factConnDB, ik, userRol, user);
		} catch (ConnectionException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NoSuchElementException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NoSuchColumnException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		//System.out.println("Fin de la funcion serverGetTasks");
		return sd;
	}
	
	public IndividualData serverGetFactsInstance(int ido, int idto/*, boolean ligthView*/, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) 
		throws SystemException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		IndividualData aipd = null;
		int tableId = QueryConstants.getTableId(ido);
		try {
			System.out.println("DEBUG1:TABLE_ID:" + tableId + ", ID_TO:" + idto + ", levels " + levels + ", lastStructLevel " + lastStructLevel + ", lock " + lock);
			QueryService2 qs2 = new QueryService2(idto, tableId, dataBaseMap, factConnDB, 
					levels, lastStructLevel, lock, user, this);
			Document dataDocument = qs2.getData();
			try{
				System.out.print("DBGGETDATA:" +jdomParser.returnXML(dataDocument));
			}catch(Exception e){
				
			}
			aipd = FactsAdapter.DataXMLToFactsXML(dataBaseMap, dataDocument,null,false);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataConversionException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		/*} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;*/
		}
		return aipd;
	}
	
	/*public IndividualData serverGetFactsInstanceQuery(String query, String user, int levels, boolean lastStructLevel) throws SystemException, 
			NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		IndividualData dIndiv = new IndividualData();
		try {
			System.out.println("DEBUG1:QUERY:" + query + " levels " + levels + " lastStructLevel " + lastStructLevel);
			String condition = "ID_O IN(" + query + ")";// AND PROPERTY<>" + Constants.IdPROP_BUSINESSCLASS;
			dbGetLock(condition, null, dIndiv, user, false, levels, lastStructLevel); 
			System.out.println("DEBUG2");
			//System.out.println(dIndiv);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		return dIndiv;
	}*/

	public IndividualData serverGetFactsInstance(HashMap<Integer,HashSet<Integer>> idObjects/*, boolean ligthView*/, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) 
		throws SystemException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		IndividualData aipd = null;
		try {
			HashSet<Integer> tableIds = Auxiliar.getTableIdsHashSet(idObjects);
			String tableIdsStr = Auxiliar.hashSetIntegerToString(tableIds, ",");
			System.out.println("DEBUG1:TABLE_IDS:" + tableIdsStr + ", levels " + levels + ", lastStructLevel " + lastStructLevel + ", lock " + lock);
			Map<Integer, Set<Integer>> idtoTableIds = Auxiliar.convertToIdtoTableIds(idObjects);
			QueryService2 qs2 = new QueryService2(idtoTableIds, dataBaseMap, factConnDB, 
					levels, lastStructLevel, lock, user, this);
			Document dataDocument = qs2.getData();
			aipd = FactsAdapter.DataXMLToFactsXML(dataBaseMap, dataDocument,null,false);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataConversionException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		/*} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;*/
		}
		return aipd;
	}
	
	public IndividualData serverGetFactsInstanceOfClass(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		IndividualData dIndiv;
		try {
			System.out.println("DEBUG1:IDTO:" + idto + ", levels " + levels + ", lastStructLevel " + lastStructLevel + ", lock " + lock);
			QueryService2 queryService2 = new QueryService2(idto, false, dataBaseMap, factConnDB, levels, lastStructLevel, lock, user, false, this,null);
			Document dataDocument = queryService2.getData();
			dIndiv = FactsAdapter.DataXMLToFactsXML(dataBaseMap, dataDocument,null,false);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataConversionException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} 
		return dIndiv;

	}

	public IndividualData serverGetFactsInstanceOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, NotFoundException, IncoherenceInMotorException, 
	IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		return serverGetFactsInstanceOfClassSpecialized(idto, user, lock, levels, lastStructLevel, false);
	}
	
	public IndividualData serverGetFactsInstanceOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel, boolean configurationMode) throws SystemException, NotFoundException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		IndividualData dIndiv;
		try {
			System.out.println("DEBUG1:IDTO_SPEC:" + idto + ", levels " + levels + ", lastStructLevel " + lastStructLevel + ", lock " + lock);
			QueryService2 queryService2 = new QueryService2(idto, true, dataBaseMap, factConnDB, levels, lastStructLevel, lock, user, configurationMode, this,null);
			Document dataDocument = queryService2.getData();
			dIndiv = FactsAdapter.DataXMLToFactsXML(dataBaseMap, dataDocument,null,false);
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataConversionException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		return dIndiv;
	}

	private void addStruct(StringBuffer sql, boolean last, boolean lastStructLevel) {
		if (last) {
			if (lastStructLevel)
				sql.append("\n and property in(select prop from properties where cat%"+Category.iStructural+"=0)");
		}
	}
	private void getIdos(String condition, int levels, boolean lastStructLevel, boolean first, 
			HashMap<Integer,HashSet<Integer>> allIdtoIdos, HashSet<Integer> allIdos) throws SQLException, NamingException {
		System.out.println("levels " + levels);
		StringBuffer sql = new StringBuffer("");
		boolean last = levels==1;
		if (first) {
			sql.append("select distinct id_o, id_to from o_datos_atrib where "+condition);
			first = false;
		} else
			sql.append("select distinct val_num, value_cls from o_datos_atrib where "+condition);
		addStruct(sql, last, lastStructLevel);
		System.out.println("sqlGetIdos " + sql);

		HashMap<Integer,HashSet<Integer>> hIdtoIdos = new HashMap<Integer, HashSet<Integer>>();
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql.toString());
			while (rs.next()) {
				Integer ido = rs.getInt(1);
				if (!rs.wasNull()) {
					allIdos.add(ido);
					
					int idto = rs.getInt(2);
					
					HashSet<Integer> hIdos = hIdtoIdos.get(idto);
					if (hIdos==null) {
						hIdos = new HashSet<Integer>();
						hIdtoIdos.put(idto, hIdos);
					}
					hIdos.add(ido);
					
					HashSet<Integer> hAllIdos = allIdtoIdos.get(idto);
					if (hAllIdos==null) {
						hAllIdos = new HashSet<Integer>();
						allIdtoIdos.put(idto, hAllIdos);
					}
					hAllIdos.add(ido);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		levels--;
		if (levels>0 && hIdtoIdos.size()>0) {
			condition = Auxiliar.createSqlIdtoIdos(hIdtoIdos, null, false);
			getIdos(condition, levels, lastStructLevel, false, allIdtoIdos, allIdos);
		}
	}
	private HashMap<Integer,HashSet<Integer>> getIdos(String condition, int levels, boolean lastStructLevel, HashSet<Integer> allIdos) throws SQLException, NamingException {
		HashMap<Integer,HashSet<Integer>> allIdtoIdos = new HashMap<Integer,HashSet<Integer>>();
		if (lastStructLevel)
			levels++;
		getIdos(condition, levels, lastStructLevel, true, allIdtoIdos, allIdos);
		return allIdtoIdos;
	}

	//version anterior al 29/04/2010
	//hace un get local y si no encuentra nada hace la busqueda en central
	//esto no era correcto al hacer una solicitud de stock
	//porque puede que los stocks no estuvieran actualizados
	//hay que volver a traerse los datos
	private void dbGetLock(String condition, Integer id, Integer idto, boolean isIdo, IndividualData afi, String user, boolean lock, int levels, boolean lastStructLevel) throws SQLException, NamingException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		boolean globalSearch = false;
		//ver donde tengo que hacer la busqueda
		boolean seHaceReplica = ir!=null;
		System.out.println("isIdo " + isIdo);

//		if (isIdo) {
//			if (seHaceReplica && ir.getIPCentral()!=null) {
//				//mirar sufijo
//				String idStr = String.valueOf(id);
//				String sufix = idStr.substring(idStr.length()-3, idStr.length());
//				System.out.println("GET:sufix " + sufix);
//				Integer sufixInt = Integer.parseInt(sufix);
//				if (sufixInt>500) {
//					String mySufix = ir.getSufix();
//					Integer mySufixGlobal = Integer.parseInt(mySufix) + 500;
//					if (!sufixInt.equals(mySufixGlobal)) {
//						globalSearch = true;
//					}
//				}
//			}
//		}
		System.out.println("GET:globalSearch " + globalSearch);
		//TODO cuando es stock se trae tambien el articulo y el almacen
		//optimizar para que solo se traiga la cantidad?
		
		if (globalSearch) {
			factConnDB.setStandAloneApp(true);
			factConnDB.setDatabaseIP(ir.getIPCentral());
			factConnDB.setPort(ir.getPortCentral());
			factConnDB.setBusiness(ir.getBusinessCentral());
		}
		if (lock)
			lockObject(id, idto, user, true);
		HashSet<Integer> allIdos = new HashSet<Integer>();
		HashMap<Integer,HashSet<Integer>> allIdtoIdos = getIdos(condition, levels, lastStructLevel, allIdos);
		boolean result = allIdos.size()>0;
		
		//si no hay resultado y no globalSearch -> buscar en Central
		if (isIdo) {
			if (!result && !globalSearch && seHaceReplica && ir.getIPCentral()!=null) {
				System.out.println("GET:nohay resultado -> globalSearch " + globalSearch);
				globalSearch = true;
				if (lock)
					unlockObject(id, idto, user);
				factConnDB.setStandAloneApp(true);
				factConnDB.setDatabaseIP(ir.getIPCentral());
				factConnDB.setPort(ir.getPortCentral());
				factConnDB.setBusiness(ir.getBusinessCentral());
				if (lock)
					lockObject(id, idto, user, true);

				allIdtoIdos = getIdos(condition, levels, lastStructLevel, allIdos);
				result = allIdos.size()>0;
			}
		}
		
		if (result) {
			if (globalSearch) {
				dbGetAtr(afi, levels, allIdtoIdos, null);
				dbGetMemo(afi, levels, allIdtoIdos, null);
				//processSystemValues(afi, allIdos, null);
			} else {
				dbGetAtr(afi, levels, allIdtoIdos, ik);
				dbGetMemo(afi, levels, allIdtoIdos, ik);
				//processSystemValues(afi, allIdos, ik);
			}
		}
		if (lock && !result)
			unlockObject(id, idto, user);
		if (globalSearch) {
			factConnDB.setStandAloneApp(false);
//			factConnDB.setDatabaseIP(databaseIP);
//			factConnDB.setPort(port);
			factConnDB.setBusiness(business);
			
			//bloquear en local antes de insertar
			if (result) {
				lockObject(id, idto, user, true);
				insertInDB(user, afi, allIdtoIdos);
				unlockObject(id, idto, user);
			}
		}
	}
	
	private void insertInDB(String user, IndividualData dIndiv, HashMap<Integer,HashSet<Integer>> allIdtoIdos) throws DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		//TODO deberia insertar solo el 1er nivel, no da problemas porque para ticket solo se pide 1
//		TransitionObject tObj = new TransitionObject(factConnDB, ik, ir, this, null, user, debugMode, generateSQL);
//		tObj.insertFacts(dIndiv, allIdtoIdos);
	}
	
	private void dbGetLock(String condition, HashMap<Integer,HashSet<Integer>> idtoIdos, HashSet<Integer> ids, IndividualData afi, String user, boolean lock, int levels, boolean lastStructLevel) throws SQLException, NamingException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		boolean seHaceReplica = ir!=null;
		boolean globalSearch = false;
//		if (seHaceReplica && ir.getIPCentral()!=null) {
//			//mirar sufijo y separar los que busco en local y en global
//			Iterator it = ids.iterator();
//			while (it.hasNext() && !globalSearch) {
//				Integer id = (Integer)it.next();
//				String idStr = String.valueOf(id);
//				String sufix = idStr.substring(idStr.length()-3, idStr.length());
//				System.out.println("GET:sufix " + sufix);
//				Integer sufixInt = Integer.parseInt(sufix);
//				if (sufixInt>500) {
//					String mySufix = ir.getSufix();
//					Integer mySufixGlobal = Integer.parseInt(mySufix) + 500;
//					if (!sufixInt.equals(mySufixGlobal)) {
//						globalSearch = true;
//					}
//				}
//			}
//		}
		System.out.println("GET:globalSearch " + globalSearch);
		
		if (globalSearch) {
			factConnDB.setStandAloneApp(true);
			factConnDB.setDatabaseIP(ir.getIPCentral());
			factConnDB.setPort(ir.getPortCentral());
			factConnDB.setBusiness(ir.getBusinessCentral());
		}
		if (lock) {
			Iterator<Integer> it = idtoIdos.keySet().iterator();
			while (it.hasNext()) {
				Integer idto = it.next();
				HashSet<Integer> hIdos = idtoIdos.get(idto);
				Iterator<Integer> it2 = hIdos.iterator();
				while (it2.hasNext()) {
					Integer ido = it2.next();
					lockObject(ido, idto, user);
				}
			}
		}
		HashSet<Integer> allIdos = new HashSet<Integer>();
		HashMap<Integer,HashSet<Integer>> allIdtoIdos = getIdos(condition, levels, lastStructLevel, allIdos);
		boolean result = allIdos.size()>0;
		boolean resultInd = result;
		
		if (result && ids!=null && seHaceReplica && ir.getIPCentral()!=null) {
			Iterator<Integer> it = ids.iterator();
			while (it.hasNext()) {
				Integer id = it.next();
				if (!allIdos.contains(id)) { //si alguno de los individuos no esta en local
					resultInd = false;
					break;
				}
			}
		}
		if (ids!=null) {
			if (!resultInd && !globalSearch && seHaceReplica && ir.getIPCentral()!=null) {
				System.out.println("GET:no hay resultado -> globalSearch " + globalSearch);
				if (lock)
					unlockObjects(idtoIdos, user);
				globalSearch = true;
				factConnDB.setStandAloneApp(true);
				factConnDB.setDatabaseIP(ir.getIPCentral());
				factConnDB.setPort(ir.getPortCentral());
				factConnDB.setBusiness(ir.getBusinessCentral());
				if (lock) {
					Iterator<Integer> it = idtoIdos.keySet().iterator();
					while (it.hasNext()) {
						Integer idto = it.next();
						HashSet<Integer> hIdos = idtoIdos.get(idto);
						Iterator<Integer> it2 = hIdos.iterator();
						while (it2.hasNext()) {
							Integer ido = it2.next();
							lockObject(ido, idto, user);
						}
					}
				}
				allIdos = new HashSet<Integer>();
				allIdtoIdos = getIdos(condition, levels, lastStructLevel, allIdos);
				result = allIdos.size()>0;
			}
		}
		if (result) {
			if (globalSearch) {
				dbGetAtr(afi, levels, allIdtoIdos, null);
				dbGetMemo(afi, levels, allIdtoIdos, null);
				//processSystemValues(afi, allIdos, null);
			} else {
				dbGetAtr(afi, levels, allIdtoIdos, ik);
				dbGetMemo(afi, levels, allIdtoIdos, ik);
				//processSystemValues(afi, allIdos, ik);
			}
		}
		if (lock && !result)
			unlockObjects(idtoIdos, user);
		if (globalSearch) {
			factConnDB.setStandAloneApp(false);
//			factConnDB.setDatabaseIP(databaseIP);
//			factConnDB.setPort(port);
			factConnDB.setBusiness(business);
			if (result) {
				Iterator<Integer> it = idtoIdos.keySet().iterator();
				while (it.hasNext()) {
					Integer idto = it.next();
					HashSet<Integer> hIdos = idtoIdos.get(idto);
					Iterator<Integer> it2 = hIdos.iterator();
					while (it2.hasNext()) {
						Integer ido = it2.next();
						lockObject(ido, idto, user);
					}
				}
				insertInDB(user, afi, allIdtoIdos);
				unlockObjects(idtoIdos, user);
			}
		}
	}

	
	/*private void dbGetAtrLock(String condition, Integer id, IndividualData afi, String user, boolean lock) throws SQLException, NamingException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		if (lock)
			lockObject(id, user);
		boolean result = dbGetAtr(condition, afi, user, lock);
		if (lock && !result)
			unlockObject(id, user);
	}*/
	
	private void dbGetAtr(IndividualData afi, int levels, HashMap<Integer,HashSet<Integer>> allIdtoIdos, IKnowledgeBaseInfo ik) throws SQLException, NamingException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		
		//DefaultSession df = new DefaultSession(null, null, false, true, false, true,false);
		String sql = "select id_o, id_to, property, val_num, val_texto, value_cls, q_min, q_max, sys_val, destination from o_datos_atrib"/*" WITH(NOLOCK)"*/;
		sql += " where ";
		sql += Auxiliar.createSqlIdtoIdos(allIdtoIdos, null, false);
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		
		//HashSet<Integer> aIdos = new HashSet<Integer>();
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				//System.out.println("IN ATRS si has NExt");
				Integer idProp = rs.getInt(3);
				if (rs.wasNull()) idProp = null;
				if (idProp!=null) {
					Integer ido = rs.getInt(1);
					if (rs.wasNull()) ido = null;
					//allIdos.add(ido);
					Integer valueCls = rs.getInt(6);
					if (rs.wasNull()) valueCls = null;
					Double qMin = rs.getDouble(7);
					if (rs.wasNull()) qMin = null;
					Double qMax = rs.getDouble(8);
					if (rs.wasNull()) qMax = null;
					Integer valNum = rs.getInt(4);
					if (rs.wasNull()) valNum = null;
					Integer idto = rs.getInt(2);
					if (rs.wasNull()) idto = null;
					String sysVal = rs.getString(9);
					String destination = rs.getString(10);

					//TODO Solo sirve para que el motor cargue la property si no la tuviera. Si en el futuro getClassName tuviera session no haria falta
					//para business class
					//if(ik!=null)
						//ik.getProperty(idto, idProp, null, user, null, df);

					String className = ik!=null?ik.getClassName(idto):AuxiliarModel.getClassName(idto, factConnDB);
					Category cat = ik!=null?ik.getCategory(idProp):AuxiliarModel.getCategory(idProp, factConnDB);
					FactInstance f = null;
					if (cat.isObjectProperty()) {
						String value = null;
						if (valNum!=null)
							value = String.valueOf(valNum);
						f = new FactInstance(idto,ido,idProp,value,valueCls,qMin,qMax,null,className);
						/*if (cat.isStructural() || levels>1) {
							boolean noEsta = allIdos.add(valNum);
							if (noEsta)
								aIdos.add(valNum);
						}*/
					} else if (cat.isDataProperty()) {
						String value = null;
						if (ik!=null) {
							String propStr = ik.getPropertyName(idProp);
							if (propStr.equals(Constants.PROP_PASSWORD)) {
								value = getDecryptValue(ido,idto,idProp);
							} else
								value = rs.getString(5);
						} else
							value = rs.getString(5);
						f = new FactInstance(idto,ido,idProp,value,valueCls,qMin,qMax,null,className);
						//TODO cndo no sea bclass name preg al motor getClassName(idto)
						//en otro caso otra consulta
						//pensarlo mejor
					} else
						throw new NotFoundException("Property no reconocida como DataProperty ni como ObjectProperty: " + idProp);
					if (sysVal!=null) {
						f.setSystemValue(sysVal);
						f.setAppliedSystemValue(true);
					}
					f.setDestinationSystem(destination);
					f.setExistia_BD(true);
					//System.out.println("Añadiendo fact " + f);
					afi.addIPropertyDef(f);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
			//df.rollBack();
		}
	}

	private String getDecryptValue(int ido, int idto, int idProp) throws SQLException, NamingException {
		String value = null;
//		GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
//		String function = generateSQL.getDecryptFunction(InstanceService.keyEncrypt, "VAL_TEXTO");
//
//		String sql = "select " + function + " from o_datos_atrib"/*" WITH(NOLOCK)"*/ 
//			+ " where id_o=" + ido + " and id_to=" + idto + " and property=" + idProp;
//		
//		Statement st = null;
//		ResultSet rs = null;
//		ConnectionDB con = factConnDB.createConnection(true); 
//		
//		try {
//			st = con.getBusinessConn().createStatement();
//			rs = st.executeQuery(sql);
//			if (rs.next())
//				value= generateSQL.getDecryptData(rs, 1);
//		} finally {
//			if (rs != null)
//				rs.close();
//			if (st != null)
//				st.close();
//			if (con!=null)
//				factConnDB.close(con);
//		}
		return value;
	}

	private boolean dbGetMemo(IndividualData afi, int levels, HashMap<Integer,HashSet<Integer>> allIdtoIdos, IKnowledgeBaseInfo ik) throws NamingException, SQLException, NotFoundException, 
	IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//DefaultSession df = new DefaultSession(null, null, false, true, false, true,false);
		String sql = "select id_o, property, memo, value_cls, id_to, sys_val, destination from o_datos_atrib_memo";// WITH(NOLOCK)";
		sql += " where ";
		sql += Auxiliar.createSqlIdtoIdos(allIdtoIdos, null, false);

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		boolean result = false;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				result = true;
				//System.out.println("IN ATRS si has NExt");

				Integer idProp = rs.getInt(2);
				if (!rs.wasNull()) {
					Integer ido = rs.getInt(1);
					if (rs.wasNull()) ido = null;
					Integer valueCls = rs.getInt(4);
					if (rs.wasNull()) valueCls = null;
					Integer idto = rs.getInt(5);
					if (rs.wasNull()) idto = null;

					//TODO Solo sirve para que el motor cargue la property si no la tuviera. Si en el futuro getClassName tuviera session no haria falta
					//para business class
					//if(ik!=null)
						//ik.getProperty(idto, idProp, null, user, null, df);

					//if (ik.isDataProperty(idProp)) {
					String className = ik!=null?ik.getClassName(idto):AuxiliarModel.getClassName(idto, factConnDB);
					String memo = rs.getString(3);
					String sysVal = rs.getString(6);
					String destination = rs.getString(7);

					FactInstance f = new FactInstance(idto,ido,idProp,memo,valueCls,null,null,null,className);
					if (sysVal!=null) {
						f.setSystemValue(sysVal);
						f.setAppliedSystemValue(true);
					}
					f.setDestinationSystem(destination);
					f.setExistia_BD(true);
					afi.addIPropertyDef(f);
					//}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
			//df.rollBack();
		}
		return result;
	}

	public void incrementValue(int ido, int idto, int idProp, int incr) throws NotFoundException, SystemException, IncoherenceInMotorException{
		try{
			Integer dataType = ik.getDatatype(idProp);
			if (dataType!=null && (dataType==Constants.IDTO_INT || dataType==Constants.IDTO_DOUBLE || dataType==Constants.IDTO_BOOLEAN 
					|| dataType==Constants.IDTO_TIME || dataType==Constants.IDTO_DATETIME || dataType==Constants.IDTO_DATE 
					|| dataType==Constants.IDTO_UNIT)) {
				if (compruebaValues(ido,idto,idProp)) {
					String sql = "UPDATE o_datos_atrib SET q_min=q_min+" + incr + ", q_max=q_max+" + incr + 
					" WHERE id_o=" + ido + " AND id_to=" + idto + " AND property=" + idProp;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
				} else
					throw new DataErrorException("El registro con ID_O " + ido + " y PROPERTY " + idProp + " que se quiere " +
					"incrementar no existe en base de datos, tiene valores nulos para Q_MIN o Q_MAX o Estos no coinciden");
			} else
				throw new DataErrorException("La property " + idProp + " que se quiere incrementar no es de tipo int, double, " +
				"boolean, time, dateTime, date o unit");
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	//se incrementa si:
	//tienen el tipo correcto: int, double, boolean, time, dateTime, date o unit
	//existe en base de datos y los q(qMin y qMax) son no nulos e iguales
	private boolean compruebaValues(int ido, int idto, int idProp) throws SQLException, NamingException {
		boolean correcto = false;
		String sqlComprob = "SELECT q_min, q_max FROM o_datos_atrib WHERE id_to=" + idto + " AND id_o=" + ido + " AND property=" + idProp;
		ConnectionDB con = factConnDB.createConnection(true); 
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlComprob);
			if (rs.next()) {
				Float qMin = rs.getFloat(1);
				Float qMax = rs.getFloat(2);
				if (qMin!=null && qMax!=null && qMin.equals(qMax))
					correcto = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return correcto;
	}

	public void reAsign(int idoUTask, String user, Integer rol) throws SystemException, InstanceLockedException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		try {
			Asigned.reAsign(this, idoUTask, user, rol, factConnDB, ik);
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	public void asign(int idoUTask, String user, Integer rol) throws SystemException, InstanceLockedException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		try {
			Asigned.asign(this, idoUTask, user, rol, factConnDB, ik);
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ApplicationException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	public void preAsign(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		try {
			Asigned.preAsign(this, idoUTask, idtoUTask, factConnDB, ik);
		} catch (NotFoundException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ApplicationException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncoherenceInMotorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncompatibleValueException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (CardinalityExceedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (OperationNotPermitedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	public void release(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		try {
			Asigned.release(this, idoUTask, idtoUTask, factConnDB, ik);
		} catch (NotFoundException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncoherenceInMotorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncompatibleValueException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (CardinalityExceedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ApplicationException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (OperationNotPermitedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	public void close(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		try {
			Asigned.close(this, idoUTask, idtoUTask, factConnDB, ik);
		} catch (NotFoundException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncoherenceInMotorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (IncompatibleValueException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (CardinalityExceedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ApplicationException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (DataErrorException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (JDOMException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (ParseException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (OperationNotPermitedException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}

	public void logError(String user, String debug, String error, String subject) throws SystemException {
		try {
			factConnDB.setStandAloneApp(false);
//			factConnDB.setDatabaseIP(databaseIP);
//			factConnDB.setPort(port);
			factConnDB.setBusiness(business);
			if (error!=null)
				error = "'" + error.replaceAll("'", "''") + "'";
			if (debug!=null)
				debug = "'" + debug.replaceAll("'", "''") + "'";
			if (subject!=null)
				subject = "'" + subject.replaceAll("'", "''") + "'";
			String sql="INSERT INTO log_error(usuario,fecha,descripcion,error,debug) "+
			"VALUES('"+user+"','" + Auxiliar.getDate() + "',"+subject+","+error+","+debug+")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
	}
	
	public Integer serverGetLicenseUsers(String keyEncrypt) throws NamingException, SQLException{
		String function = generateSQL.getDecryptFunction(keyEncrypt, "users");
		String sql = "SELECT " + function + " FROM license";
		Integer licenses = null;
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				licenses = Integer.parseInt(generateSQL.getDecryptData(rs, 1));				
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return licenses;
	}
	
	public Long serverGetLicenseDate(String keyEncrypt) throws NamingException, SQLException{
		String function = generateSQL.getDecryptFunction(keyEncrypt, "fechamax");
		String sql = "SELECT " + function + " FROM license";
		Long rules = null;
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				rules = Long.parseLong(generateSQL.getDecryptData(rs, 1));				
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return rules;
	}
	
	public Integer serverGetLicenseType(String keyEncrypt) throws NamingException, SQLException{
		String function = generateSQL.getDecryptFunction(keyEncrypt, "type");
		String sql = "SELECT " + function + " FROM license";
		Integer licenses = null;
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				licenses = Integer.parseInt(generateSQL.getDecryptData(rs, 1));	
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return licenses;
	}
	
	public License serverGetLicense() throws NamingException, SQLException, SystemException{
		GenerateSQL generate=new GenerateSQL(factConnDB.getGestorDB());
		String sql = "SELECT fecha FROM "+generate.getCharacterBegin()+"aplicación"+generate.getCharacterEnd();
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		String keyEncrypt=null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				keyEncrypt=String.valueOf(rs.getLong(1));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
//		String function = generateSQL.getDecryptFunction(keyEncrypt, "type");
//		String function1 = generateSQL.getDecryptFunction(keyEncrypt, "fechamax");
//		String function2 = generateSQL.getDecryptFunction(keyEncrypt, "user");
//		String sql = "SELECT " + function + ", "+function1+", "+function2+" FROM license";
//		License license = null;
//		ConnectionDB con = factConnDB.createConnection(true);
//		Statement st = null;
//		ResultSet rs = null;		
//		try {
//			st = con.getBusinessConn().createStatement();
//			rs = st.executeQuery(sql);
//			if (rs.next()) {
//				int type = Integer.parseInt(generateSQL.getDecryptData(rs, 1));
//				long expiredDate= Long.parseLong(generateSQL.getDecryptData(rs, 2));
//				int users = Integer.parseInt(generateSQL.getDecryptData(rs, 3));
//				license=new License(expiredDate, users, type);
//			}
//		} finally {
//			if (rs != null)
//				rs.close();
//			if (st != null)
//				st.close();
//			if (con!=null)
//				factConnDB.close(con);
//		}
//		return license;
		
		/*Lo normal seraa hacerlo como arriba para hacer todo en una unica consulta pero parece que hay un bug en postgree q evita que se pueda poner mas de un dato encriptado en el select.
		 * Da ERROR: decrypt error: Data not a multiple of block size*/
		int type = serverGetLicenseType(keyEncrypt);
		long expiredDate= serverGetLicenseDate(keyEncrypt);
		int users = serverGetLicenseUsers(keyEncrypt);
		License license=new License(expiredDate, users, type);
				
		return license; 
	}
	
	public void serverUpdateLicense(License license) throws NamingException, SQLException, SystemException{
		GenerateSQL generate=new GenerateSQL(factConnDB.getGestorDB());
		String sql = "SELECT fecha FROM "+generate.getCharacterBegin()+"aplicación"+generate.getCharacterEnd();
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String keyEncrypt=String.valueOf(rs.getLong(1));
				st.executeUpdate("UPDATE \"aplicación\" SET fecha="+keyEncrypt);
				st.executeUpdate("UPDATE License SET Users=encrypt('"+license.getUsers()+"', '" + keyEncrypt + "','aes'), FechaMax=encrypt('"+license.getExpiredDate()+"', '" + keyEncrypt + "','aes'), Type=encrypt('"+license.getType()+"', '" + keyEncrypt + "','aes')");
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
	}
	
	public HashSet<String> serverGetNumberOfSessions() throws NamingException, SQLException{
		//numero de sesiones en uso
		String sql = "select id from sessions";
		HashSet<String> users = new HashSet<String>();
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				//String keySession = user + "/" + bns + "/" + mode;
				String id = rs.getString(1);
				int indexBnsMode = id.lastIndexOf("/");
				int indexUserBnsTmp = id.indexOf("/");
				int indexUserBns = indexUserBnsTmp;
				boolean salir = false;
				while (!salir) {
					if (indexUserBnsTmp==indexBnsMode)
						indexUserBnsTmp = 0;
					if (indexUserBnsTmp!=0) {
						indexUserBns = indexUserBnsTmp;
						indexUserBnsTmp = id.indexOf("/",indexUserBnsTmp+1);
					} else
						salir = true;
				}
				String user = id.substring(0, indexUserBns);
				System.out.println("user " + user);
				users.add(user);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return users;
	}
	public ArrayList<String> getLocksByLogin(String login) throws SQLException, NamingException {
		ArrayList<String> ikeys = new ArrayList<String>();
		String sql = "select id from locksid where login='" + login + "'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			//System.out.println(sql);
			rs = st.executeQuery(sql);
			while (rs.next())
				ikeys.add(rs.getString(1));
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return ikeys;
	}

	/*public boolean serverIsSameIp(String idSession, String ip) throws NamingException, SQLException{
		String sql = "SELECT RemoteIp FROM Sessions WHERE IdSession='"+idSession+"'";
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String remoteIp = rs.getString(1);
				if(!remoteIp.equals(ip))
					return false;
				else
					return true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				con.close();
		}
		return true;
	}*/

	//aqui no se llega mediante comunicaciones
	public IteratorQuery serverGetIteratorQuery(String sql,boolean update) throws SystemException {
		Element eC = new Element("LIST");
		try{
			List<List<String>> rows=queryRules(sql,false);
	
	        Iterator it = rows.iterator();
	        while(it.hasNext()) {
	        	Element row = new Element("R");
	        	eC.addContent(row);
	        	List<String> columns = (List<String>)it.next();
	            Iterator<String> it2 = columns.iterator();
	            while(it2.hasNext()) {
	            	Element column = new Element("C");
	            	row.addContent(column);
	            	String value = it2.next();
	            	column.addContent(new CDATA(value));
	            }
	        }
		} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
		
		return new IteratorQuery(eC);
	}
	//aqui se llega mediante comunicaciones
	public List<List<String>> queryRules(String sql,boolean update) throws SQLException, NamingException {
		if(!update) return DBQueries.executeQuery(factConnDB, sql);
		else{
			List<List<String>> result = new LinkedList<List<String>>();
			DBQueries.executeUpdate(factConnDB, sql);
			return result;
		}
	}
	
	private selectData getQuery(Element root, int mode) throws SystemException, JDOMException, SQLException, DataErrorException, NotFoundException, IncoherenceInMotorException, NamingException {
		long ini = System.currentTimeMillis();
		QueryService qd = new QueryService(factConnDB, ik, dataBaseMap);
		selectData sd;
		try {
			sd = qd.QuerySD(root, mode,true);
		} catch (NoSuchColumnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		System.out.println("Fin de GetQuery, time: " + (System.currentTimeMillis() - ini));
		return sd;
//		queryData res = query(root);
//		return res.toSelectData(root);
	}

	private Element toQueryXMLDB(String nameQuery, HashMap<String,String> hIdValue) 
	throws NamingException, SQLException, JDOMException, ParseException {
		Element query = null;
		String sql = "SELECT query FROM s_query WHERE name='" + nameQuery + "'";
		ConnectionDB con = factConnDB.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		System.out.println("sql->"+sql);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String queryStr = rs.getString(1);
				if (queryStr!=null) {
					query = jdomParser.readXML(queryStr).getRootElement();
					//System.out.println("QUERY DB->"+jdomParser.returnXML(query));

					changeValuesQuery(query, hIdValue);
					//deleteWhere(query);
				}
			} else
				System.out.println("La query no esta en BD");
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return query;
	}

	private void changeValuesQuery(Element query, HashMap<String,String> hIdValue) throws ParseException, SQLException, NamingException, JDOMException {
		String[] names = new String[2];
		names[0] = QueryConstants.WHERE;
		names[1] = QueryConstants.CLASS;
		String[] ats = new String[4];
		ats[0] = QueryConstants.VALUE;
		ats[1] = QueryConstants.VAL_MIN;
		ats[2] = QueryConstants.VAL_MAX;
		ats[3] = QueryConstants.ID_O;
		ArrayList<Element> nodes=jdomParser.findElementsHasAtsOrText(query, names, ats, "(VALUE)", true);
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			Element node = (Element)it.next();
			String id = node.getAttributeValue(QueryConstants.ID);
			String value = null;
			if (hIdValue!=null) {
				value = hIdValue.get(id);
				String nameNode = node.getName();
				if (value!=null) {
					if (nameNode.equals(QueryConstants.WHERE)) {
						if (node.getAttributeValue(QueryConstants.VAL_MIN)!=null) {
							node.setAttribute(QueryConstants.VAL_MIN,node.getAttributeValue(QueryConstants.VAL_MIN).replaceAll("\\(VALUE\\)", value));
							node.setAttribute(QueryConstants.VAL_MAX,node.getAttributeValue(QueryConstants.VAL_MAX).replaceAll("\\(VALUE\\)", value));
						} else if(node.getAttributeValue(QueryConstants.VALUE)!=null) {
							if(node.getAttribute(QueryConstants.VALUE)!=null)
								node.setAttribute(QueryConstants.VALUE,node.getAttributeValue(QueryConstants.VALUE).replaceAll("\\(VALUE\\)", value));
							else if(node.getText()!=null && node.getText().length()>0)
								node.setText(node.getText().replaceAll("\\(VALUE\\)",value));
							else {
								node.setAttribute(QueryConstants.VAL_MIN,node.getAttributeValue(QueryConstants.VAL_MIN).replaceAll("\\(VALUE\\)", value));
								node.setAttribute(QueryConstants.VAL_MAX,node.getAttributeValue(QueryConstants.VAL_MAX).replaceAll("\\(VALUE\\)", value));
							}
						} else 
							node.setText(node.getText().replaceAll("\\(VALUE\\)",value));
					} else if (nameNode.equals(QueryConstants.CLASS)) {
						node.setAttribute(QueryConstants.ID_O, value);
						if (StringUtils.equals(node.getAttributeValue(QueryConstants.REQUIRED_IF_FIXED),"TRUE")) {
							node.setAttribute(QueryConstants.REQUIRED, "TRUE");
							node.removeAttribute(QueryConstants.REQUIRED_IF_FIXED);
						}
					}
				} else {
					if (nameNode.equals(QueryConstants.WHERE))
						node.setAttribute("DELETE", "TRUE");
					else if (nameNode.equals(QueryConstants.CLASS)) {
						node.removeAttribute(QueryConstants.ID_O);
						if (StringUtils.equals(node.getAttributeValue(QueryConstants.REQUIRED_IF_FIXED),"TRUE")) {
							node.removeAttribute(QueryConstants.REQUIRED_IF_FIXED);
						}
					}
				}
			}
		}
//		QueryReportParser qrp=new QueryReportParser(factConnDB, false, true, true);
//		qrp.parserIDs(query);
		System.out.println("result="+jdomParser.returnXML(query));
	}

	/*public void dbExecUpdate(String sql) throws SQLException, NamingException {
		// System.out.println("DBEXEC:"+sql);
		Statement st = null;
		ConnectionDB con = factConnDB.createConnection(false);
		try {
			st = con.getBusinessConn().createStatement();
			st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
			con.close();
		}
	}*/
	public void logError(Window window, Exception e, String subject) {
		System.err.println("WARNING:Metodo sin implementar. LLamar a logError(String user, String debug, String error, String subject)");
	}
	public ArrayList serverGetRules(ArrayList<String> rulesFiles) {
		Object pkg=null;
		ArrayList listRules=new ArrayList();
		Iterator<String> itrRules=rulesFiles.iterator();
		while(itrRules.hasNext()){
			String rules=itrRules.next();
			try {
				//System.err.println("RUTAAAAAAAAAA:"+System.getProperty("user.dir"));
				File file=new File(rules);
				System.out.println("Fichero de reglas: " + file.getAbsoluteFile().getAbsolutePath());
		        ObjectInputStream in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file)));
		        // Deserialize the object
		        pkg = in.readObject();
			    in.close();
			    
			    listRules.add(pkg);
			} catch (Exception e) {
				System.err.println("ERROR: Error al intentar obtener el fichero de reglas "+rules);
				e.printStackTrace();
			}
		}
	    
	    return listRules;
	}
	public ImageIcon getIcon(ImageObserver obs, String icon, int maxAncho, int maxAlto) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String serverGetRdn(int ido, int idto) throws SystemException {
		String rdn = null;
		int tableId = QueryConstants.getTableId(ido);
		try {
			rdn = getRdn(factConnDB, dataBaseMap, tableId, idto);
		} catch (DataErrorException e) {
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER EL RDN");
		}
		return rdn;
	}
	
	public HashMap<Integer,String> serverGetRdn(HashMap<Integer,HashSet<Integer>> listIdo) throws SystemException {
		HashMap<Integer,String> rdns = new HashMap<Integer, String>();
		try{
			Iterator<Integer> itrIdtos=listIdo.keySet().iterator();
			while(itrIdtos.hasNext()){
				int idto=itrIdtos.next();
				HashSet<Integer> idos=listIdo.get(idto);
				HashSet<Integer> tableIds=new HashSet<Integer>();
				for(Integer ido:idos){
					tableIds.add(QueryConstants.getTableId(ido));
				}
				
				rdns.putAll(getRdn(factConnDB, dataBaseMap, tableIds, idto));
				
			}
		} catch (DataErrorException e) {
			e.printStackTrace();
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER EL RDN");
		}
		return rdns;
	}
	
/*	public void reserveIndividual(Integer userRol, String user, ArrayList<IPropertyDef> aipd) throws SystemException {
		System.out.println("seHaceReplica " + seHaceReplica);
		//if (seHaceReplica) {
			try {
				TransitionObject tObj = new TransitionObject(factConnDB, ik, ir, this, userRol, user, debugMode, generateSQL);
				ArrayList<IdoObject> aNewIdosLock = new ArrayList<IdoObject>();
				HashMap<Integer,HashMap<Integer,Integer>> aDataKeys = new HashMap<Integer,HashMap<Integer,Integer>>();
				HashMap<Integer,TransitionObject.IdoObject> newRdnsLock = tObj.getNewRdnsLock(aipd, aDataKeys);
				
				//abrir transaccion solo si es necesario
				if (newRdnsLock.size()>0) {
					tObj.insertReserve(newRdnsLock, aNewIdosLock, aDataKeys);
//					System.out.println("Conectando a la central para crear individuo incremental");
//					//abre transaccion
//					factConnDB.setStandAloneApp(true);
//					if (ir.getIPCentral()!=null) {
//						System.out.println("estoy en tienda");
//						factConnDB.setDatabaseIP(ir.getIPCentral());
//						factConnDB.setPort(ir.getPortCentral());
//						factConnDB.setBusiness(ir.getBusinessCentral());
//						System.out.println("central: " + factConnDB.getDatabaseIP() + ","  + factConnDB.getPort() + "," + factConnDB.getBusiness());
//					}
//					ConnectionDB conCentralDB = factConnDB.createConnection(false);
//					Connection conCentral = conCentralDB.getBusinessConn();
//					try {
//						tObj.insertRemoteReserve(conCentral, newRdnsLock, aNewIdosLock);
//						conCentral.commit();
//					} catch (SQLException e) {
//						conCentral.rollback();
//						SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
//						sysEx.setStackTrace(e.getStackTrace());
//						throw sysEx;
//					} catch (NamingException e) {
//						conCentral.rollback();
//						SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
//						sysEx.setStackTrace(e.getStackTrace());
//						throw sysEx;
//					} finally {
//						//cierra transaccion
//						conCentral.close();
//						factConnDB.close(conCentralDB);
//	
//						factConnDB.setStandAloneApp(false);
//						if (ir.getIPCentral()!=null) {
//							factConnDB.setDatabaseIP(databaseIP);
//	//						factConnDB.setPort(port);
//	//						factConnDB.setBusiness(business);
//						}
//					}
				}
				tObj.insertLocalLock(aNewIdosLock);
			} catch (DataErrorException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (SQLException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (NamingException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (NotFoundException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (IncoherenceInMotorException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			}
		//}
	}*/
	public String serverGetClassDescription(int idto) throws SystemException {
		String description = null;
		String sql = "select description from helpclasses where name='"+dataBaseMap.getClass(idto).getName()+"' AND language='es'";
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con= factConnDB.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
			st= con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE IDTO="+idto);
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE IDTO="+idto);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
			}
		}
		return description;
	}
	public String serverGetPropertyDescription(int idProp) throws SystemException {
		String description = null;
		String sql = "select description from helpproperties where name='"+dataBaseMap.getPropertyName(idProp)+"' AND language='es'";
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con= factConnDB.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
			st= con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE PROP="+idProp);
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE PROP="+idProp);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
			}
		}
		return description;
	}
	public HashMap<Integer,String> serverGetPropertiesDescriptionOfClass(int idto) throws SystemException {
		
		HashMap<Integer,String> mapPropDescription=new HashMap<Integer, String>(); 
		String sql = "select distinct name, description from helpproperties where language='es' AND name in(";
		
		Iterator<PropertyInfo> itr=dataBaseMap.getClass(idto).getAllProperties().iterator();
		while(itr.hasNext()){
			PropertyInfo p=itr.next();
			sql+="'"+p.getName()+"'";
			if(itr.hasNext()){
				sql+=",";
			}
		}
		sql+=")";
		
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {  
			con= factConnDB.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
			st= con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				String rdn=rs.getString(1);
				String description=rs.getString(2);
				mapPropDescription.put(getIk().getIdProperty(rdn), description);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE LAS PROPERTIES PARA IDTO="+idto);
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción DE LAS PROPIEDADES");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE LAS PROPERTIES PARA IDTO="+idto);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción DE LAS PROPIEDADES");
			}
		}
		return mapPropDescription;
	}
	public Integer sendDataTransition(Element xmlData) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, IOException, JDOMException {
		Integer idRoot = null;
/*		boolean seHaceReplica = ir!=null;
		Element xmlDefinition = getXmlDefinition(xmlData);
		
		if (xmlDefinition!=null) {
			try {
				TransitionMigration tm = new TransitionMigration(factConnDB, ik);
				idRoot = tm.startMigration(xmlData, xmlDefinition, seHaceReplica);
			} catch (ParseException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (JDOMException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_JDOM, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (SQLException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			} catch (NamingException e) {
				SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
				sysEx.setStackTrace(e.getStackTrace());
				throw sysEx;
			}
		} else {
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE DATOS PARA XML=" + jdomParser.returnNodeXML(xmlData));
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL ENVIAR DATOS A APLICAción");
		}*/
		return idRoot;
	}
	
	private Element getXmlDefinition(Element xmlData) throws SystemException {
		Element xmlDefinition = null;
		List<Element> xmlDataList = xmlData.getChildren();
		if (xmlDataList.size()>0) {
			Element elemClase = xmlDataList.get(0);
			String claseName = elemClase.getName().toLowerCase().replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i").
				replaceAll("ó", "o").replaceAll("ú", "u");
			//acceso a BD xa obtener el xml
			String sql = "select XML from XMLDefinition where ID='" + claseName + "'";
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = null;
			try {
				con = factConnDB.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()){
					String xmlString = rs.getString(1);
					xmlDefinition = jdomParser.readXML(xmlString).getRootElement();
				}
			} catch (Exception e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE DATOS PARA CLASE=" + claseName);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL ENVIAR DATOS A APLICAción");
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con!=null)
						factConnDB.close(con);
				} catch (SQLException e) {
					System.out.println("FALLO AL OBTENER LA DESCRIPTION DE DATOS PARA CLASE=" + claseName);
					throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL ENVIAR DATOS A APLICAción");
				}
			}

		}
		return xmlDefinition;
	}

	public String serverUploadFile(String filePath, int valueCls) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDataBaseMap(DataBaseMap dataBaseMap) {
		this.dataBaseMap = dataBaseMap;
	}
	
	public DataBaseMap getDataBaseMap(){
		return dataBaseMap;
	}
	
	public Changes serverTransitionObject(String user, IndividualData iData, Integer windowSession, 
			boolean migration, boolean keepTableIds, String replicaSource) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException {
		return serverTransitionObjectImp(user, iData, null,windowSession, migration, false, keepTableIds, replicaSource);
		
	}
	
	public Changes serverTransitionObject(String user, Document document, ArrayList<Reservation> aReservation, Integer windowSession, 
			boolean migration, boolean keepTableIds, String replicaSource, boolean extractTableId) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException {
		if(extractTableId){
			for(Element node:((ArrayList<Element>)jdomParser.elements(document.getRootElement(), "*", true))){
				String strIdo=node.getAttributeValue(XMLConstants.ATTRIBUTE_TABLEID);
				if(strIdo!=null){
					int tableid=Integer.parseInt(strIdo);
					node.setAttribute(XMLConstants.ATTRIBUTE_TABLEID,""+tableid);
				}
			}
		}
		return serverTransitionObject(user, document, aReservation, windowSession, migration, false, keepTableIds, replicaSource);
		
	}
	public Changes serverTransitionObject(String user, Document data, ArrayList<Reservation> aReservation, Integer windowSession,
			boolean migration, boolean preprocess, boolean keepTableIds, String replicaSource) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException{
		return serverTransitionObjectImp(user, data, aReservation,  windowSession, migration, preprocess, keepTableIds, replicaSource);
	}
			
	public Changes serverTransitionObjectImp(String user, Object data, ArrayList<Reservation> aReservation, Integer windowSession, 
			boolean migration, boolean preprocess, boolean keepTableIds, String replicaSource) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException {
		// TESTING código insertado con fines de depuración, quitar para ahorrar impresiones en el log del server
		Document document=null;
		//solo puede llegar como facts individual data desde una sesion de un motor de servidor
		if(data instanceof IndividualData){
			Element indiv = ((IndividualData)data).toElement();
			Document dataDocument=new Document(indiv);
			
			aReservation = messageFactory.buildReservation(indiv);
			document = FactsAdapter.factsXMLToDataXML(dataBaseMap, dataDocument, replicaSource!=null,replicaSource!=null);
		}
		
		if(data instanceof Document){
			document=(Document)data;
		}
		
		StringWriter stringWriter = new StringWriter();
		XMLOutputter xmlOutputter = new XMLOutputter("\t", true);
		try {
			xmlOutputter.output(document, stringWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Documento que se envia a DataBaseManager\n" + stringWriter);
		
		if(data==null) throw new IncompatibleValueException("Documento de entrada no sigue formato conocido de fact o xml de datos");		

		// XXX Fin del código de depuración.
		DatabaseManager databaseManager = new DatabaseManager(factConnDB, dataBaseMap, this, ir, user, debugMode);
		databaseManager.setMigration(migration);
		databaseManager.setKeepTableIds(keepTableIds);
		databaseManager.setReplication(replicaSource!=null);
		Changes changes=null;
		try{
		changes= databaseManager.execute(document);
		}catch(NoSuchColumnException e){
			throw new DataErrorException(e.getMessage());
		}
		if (aReservation!=null)
			this.deleteReservation(aReservation, user, windowSession);
		return changes;
	}
	
	
	@Override
	public HashMap<DomainProp, Double> reserve(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//devolver la diferencia entre available y lo q hay en bd
		//reservation 2
		//available 5
		//bd 5
		// -> 0
		
		//reservation 2
		//available 5
		//bd 3
		// -> -2
		
		//reservation: solo se usa para almacenarlo en la tabla reservation
		HashMap<DomainProp, Double> hReservation = new HashMap<DomainProp, Double>();
		Iterator<Reservation> it = reservationList.iterator();
		while (it.hasNext()) {
			Reservation reservation = (Reservation)it.next();
			System.out.println(reservation);
			int ido = reservation.getIdo();
			int prop = reservation.getIdProp();
			double available = reservation.getAvailable();
			
			int idto = QueryConstants.getIdto(ido);
			this.lockObjectDB(ido, idto, prop, user);
			double availableDB = getStockDB(ido, idto, prop) - getReserveDB(ido, prop, user, windowSession);
			System.out.println("Hay " + availableDB + " en BD");
			DomainProp dProp = new DomainProp(ido, idto, prop);
			double difference = availableDB;//-available;
			System.out.println("Añadiendo en resultado: " + dProp + ", diferencia entre lo que realmente hay en bd y lo que se cree disponible: " + difference);
			hReservation.put(dProp, difference);
			insertIntoReservation(ido, prop, reservation.getReservation(), user, windowSession);
			this.unlockObjectDB(ido, idto, prop, user);
		}
		return hReservation;
	}
	
	private double getStockDB(int ido, int idto, int prop) throws DataErrorException, NotFoundException, SQLException, NamingException {
		double stockDB = 0;
		IQueryInfo tableView = dataBaseMap.getView(idto);
		if (tableView==null)
			tableView = dataBaseMap.getTable(idto);
		String tableName = generateSQL.getCharacterBegin() + tableView.getName() + generateSQL.getCharacterEnd();
		
		List<String> columnsName = null;
		try {
			columnsName=tableView.getColumnNamesContainingProperty(prop);
		} catch (NoSuchColumnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DataErrorException(e.getMessage());
		}
		if (columnsName==null || columnsName.size()==0 || columnsName.get(0)==null)
			throw new DataErrorException("No existe una columna para la property " + ik.getPropertyName(prop) + " en la tabla/vista " + tableView.getName());
		String columnNameNoCharacter = columnsName.get(0);
		String columnName = generateSQL.getCharacterBegin() + columnNameNoCharacter + generateSQL.getCharacterEnd();
		String tableIdName = generateSQL.getCharacterBegin() + IQueryInfo.COLUMN_NAME_TABLEID + generateSQL.getCharacterEnd();
		
		String sql = "select " + columnName + " as res from " + tableName + 
							" where " + tableIdName + "=" + QueryConstants.getTableId(ido);
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			System.out.println("sql getStockDB " + sql);
			rs = st.executeQuery(sql);
			if (rs.next())
				stockDB = rs.getDouble(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return stockDB;
	}
	
	private double getReserveDB(int ido, int prop, String user, Integer windowSession) throws SQLException, NamingException {
		double reserveDB = 0;
		String sql = "select reservation as res from reservation" + 
							" where ido=" + ido + " and prop=" + prop + 
							" and ";
						if (windowSession!=null)
							sql+= "(";
						sql += generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + "!='" + user.replaceAll("'", "''") + "'";
						if (windowSession!=null)
							sql += " or idsession!=" + windowSession + ")";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			System.out.println("sql getReserveDB " + sql);
			rs = st.executeQuery(sql);
			while (rs.next())
				reserveDB += rs.getDouble(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return reserveDB;
	}
	
	private void insertIntoReservation(int ido, int prop, double reservation, String user, Integer windowSession) throws SQLException, NamingException {
		boolean add = false;
		while (!add) {
			String sql = "UPDATE reservation set reservation=" + reservation + 
					" where ido=" + ido + " and prop=" + prop + " and " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + "='" + user.replaceAll("'", "''") + "'";
			if (windowSession!=null)
				sql += " and idsession=" + windowSession;
			int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			System.out.println("rows " + rows);
			if (rows==0) {
				sql = "INSERT INTO reservation(ido, prop, reservation, " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + ", idsession) " +
					"VALUES(" + ido + ", " + prop + ", " + reservation + ", '" + user.replaceAll("'", "''") + "', " + windowSession + ")";
				if (factConnDB.getGestorDB().equals(GestorsDBConstants.postgreSQL))
					sql += " RETURNING ido";
				try {
					if (factConnDB.getGestorDB().equals(GestorsDBConstants.postgreSQL))
						generateSQL.executeSecurePostgreInsert(factConnDB, sql);
					else
						DBQueries.execute(factConnDB, sql);
					add = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else
				add = true;
		}
	}
	
	public void deleteReservation(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		Iterator<Reservation> it = reservationList.iterator();
		while (it.hasNext()) {
			Reservation reservation = (Reservation)it.next();
			int ido = reservation.getIdo();
			int prop = reservation.getIdProp();
			double reservationVal = reservation.getReservation();
			int idto = QueryConstants.getIdto(ido);
			this.lockObjectDB(ido, idto, prop, user);
			
			boolean delete = false;
			String sql = "select reservation from reservation" + 
				" where ido=" + ido + " and prop=" + prop + 
				" and " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + "='" + user.replaceAll("'", "''") + "'";
			if (windowSession!=null)
				sql += " and idsession=" + windowSession;
				
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = null;
			try {
				con = factConnDB.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					double reserv = rs.getDouble(1);
					if (reserv-reservationVal<=0)
						delete = true;
				}
			} catch (SQLException e) {
				this.unlockObjectDB(ido, idto, prop, user);
				throw e;
			} catch (NamingException e) {
				this.unlockObjectDB(ido, idto, prop, user);
				throw e;
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
			try {
				if (delete) {
					sql = "DELETE FROM reservation " + 
						" where ido=" + ido + " and prop=" + prop + " and " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + "='" + user.replaceAll("'", "''") + "'";
					if (windowSession!=null)
						sql += " and idsession=" + windowSession;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
				} else {
					sql = "UPDATE reservation set reservation=reservation-" + reservationVal + 
						" where ido=" + ido + " and prop=" + prop + " and " + generateSQL.getCharacterBegin() + "user" + generateSQL.getCharacterEnd() + "='" + user.replaceAll("'", "''") + "'";
					if (windowSession!=null)
						sql += " and idsession=" + windowSession;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
				}
			} finally {
				this.unlockObjectDB(ido, idto, prop, user);
			}
		}
	}

	public static Integer getIdo(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int idto, String rdn, boolean caseInsensitive) throws DataErrorException {
		Integer ido=null;
		Table table = dataBaseMap.getTable(idto);
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select " + generateSQL.getCharacterBegin() + "tableId" + generateSQL.getCharacterEnd() + 
			" from " + generateSQL.getCharacterBegin() + table.getName() + generateSQL.getCharacterEnd() + " where " + Constants.PROP_RDN;
		if(caseInsensitive){
			sql+= " "+generateSQL.getLike()+" '" + rdn +"'";
		}else{
			sql+= "='" + rdn +"'";
		}
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				ido = QueryConstants.getIdo(rs.getInt(1),idto);
		} catch (Exception e) {
			System.out.println("FALLO AL OBTENER IDO del idto " + idto + " con rdn " + rdn);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER IDO del idto " + idto + " con rdn " + rdn);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return ido;
	}
	
	public static ArrayList<Integer> getIdos(FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, int idto, Map<String,Object> mapPropertyValue, boolean caseInsensitive) throws DataErrorException {
		ArrayList<Integer> idos=new ArrayList<Integer>();
		String name=null;
		Table table = dataBaseMap.getTable(idto);
		if(table==null){
			name=dataBaseMap.getView(idto).getName();
		}else{
			name=table.getName();
		}
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select " + generateSQL.getCharacterBegin() + "tableId" + generateSQL.getCharacterEnd() + 
			" from " + generateSQL.getCharacterBegin() + name + generateSQL.getCharacterEnd();
		
		if(!mapPropertyValue.isEmpty()){
			 sql+= " where ";
		}
		Iterator<String> itr=mapPropertyValue.keySet().iterator();
		while(itr.hasNext()){
			String prop=itr.next();
			Object value=mapPropertyValue.get(prop);
			if(value instanceof String){
				if(caseInsensitive){
					sql+= prop+" "+generateSQL.getLike()+" '" + mapPropertyValue.get(prop) +"'";
				}else{
					sql+= prop+"='" + mapPropertyValue.get(prop) +"'";
				}
			}else{
				sql+= prop+"=" + mapPropertyValue.get(prop);
			}
			
			if(itr.hasNext()){
				sql+=" and ";
			}
		}
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				idos.add(QueryConstants.getIdo(rs.getInt(1),idto));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER IDOS del idto " + idto + " con property-valor " + mapPropertyValue);
			throw new DataErrorException("FALLO EXISTENCIA");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER IDOS del idto " + idto + " con property-valor " + mapPropertyValue);
				throw new DataErrorException("FALLO EXISTENCIA");
			}
		}
		return idos;
	}

	@Override
	public void serverReportsClasificator() throws SQLException, NamingException, IOException, JDOMException {
		ReportClasificator rClasificator = new ReportClasificator(factConnDB);
		rClasificator.startClasificator();
	}
	
			
	@Override
	public HashMap<Integer, String> serverGetIndividualsDescriptionOfClass(int idto) throws SystemException {
		HashMap<Integer,String> mapIdoDescription=new HashMap<Integer, String>(); 
		String sql = "select distinct name, description from helpindividuals where class='"+dataBaseMap.getClass(idto).getName()+"' AND language='es'";
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con= factConnDB.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
			st= con.createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()){
				String rdn=rs.getString(1);
				String description=rs.getString(2);
				mapIdoDescription.put(getIdo(factConnDB, dataBaseMap, idto, rdn, false), description);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE LOS INDIVIDUALS PARA IDTO="+idto);
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción DE LOS INDIVIDUOS");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE LOS INDIVIDUALS PARA IDTO="+idto);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción DE LOS INDIVIDUOS");
			}
		}
		return mapIdoDescription;
	}
	@Override
	public String serverGetIndividualDescription(int ido)
			throws SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException, DataErrorException {
		String description = null;
		String sql = "select description from helpindividuals where name='"+getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(ido), QueryConstants.getIdto(ido))+"' AND language='es'";
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con= factConnDB.createConnection(true).getDataBaseConnNotReusable("dynaglobal");
			st= con.createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				description=rs.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("FALLO AL OBTENER LA DESCRIPTION DE IDO="+ido);
			throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				System.out.println("FALLO AL OBTENER LA DESCRIPTION DE IDO="+ido);
				throw new SystemException(SystemException.ERROR_DATOS, "FALLO AL OBTENER LA DESCRIPción");
			}
		}
		return description;
	}
	@Override
	public void changeMode(String mode) throws SystemException,
			RemoteSystemException {
	}
	@Override
	public long serverGetCurrentTimeMillis() throws SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException,
			NoSuchFieldException {
		return System.currentTimeMillis();
	}
	@Override
	public String getSubscription() {
		return null;
	}
	public int getBusiness() {
		return business;
	}
	public void setBusiness(int business) {
		this.business = business;
	}
	@Override
	public boolean sendEmail(int ido, int idto, String reportFileName, String email,  String subject, String body, int idoMiEmpresa, int idoDestinatario, boolean showError) throws SQLException, NamingException, DataErrorException {
		
		ArrayList<String> fileNames=new ArrayList<String>();
		
		String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
		String path = serverHomeDir + "\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\reports\\";
		if(reportFileName!=null){
			fileNames.add(path+reportFileName);
		}
			
		String description=null;
		String remitente=null;
		boolean success=false;
		
		EmailConfiguration emailConfiguration=getEmailConfiguration(idoMiEmpresa);
		if(emailConfiguration!=null){
			Email emailObject=new Email(emailConfiguration);//new Email("dynagent@gmail.com","dynaproyect","dynagent@gmail.com","Dynagent", "smtp.gmail.com", 587, true, Email.TipoConexionSegura.TLS, true);
			
			System.out.println("Creada clase\nEnviando...");
			email=email.replaceAll(" ", "");
			String[] correosDestino = email.split(";|,");
			String[] nombresDestino = null;//{name};
			
			if(emailObject.envioCorreoElectronico(correosDestino, nombresDestino, subject, body, fileNames, body.contains("<html>"))){
				System.out.println("Enviado Google");
				success=true;
				description="Enviado correctamente";
			}else{
				System.out.println("NO Enviado Google");
				success=false;
				description=emailObject.getMensajeUltimoEnvio();
			}
			remitente=emailConfiguration.getEmail_remitente();
		}else{
			remitente="-";
			description="No existe un servidor mail saliente activo en Aplicación para Mi Empresa "+getRdn(factConnDB, getDataBaseMap(), QueryConstants.getTableId(idoMiEmpresa), getDataBaseMap().getClass("MI_EMPRESA").getIdto())+".";
		}
		int idtoDestinatario=QueryConstants.getIdto(idoDestinatario);
		
		String documentRdn=getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(ido), idto);
		if(documentRdn==null){
			documentRdn=getDeletedObjectRdn(factConnDB, QueryConstants.getTableId(ido), idto);
		}
		String destinatarioRdn=getRdn(factConnDB, dataBaseMap, QueryConstants.getTableId(idoDestinatario), idtoDestinatario);
		
		insertEmailLog(remitente, dataBaseMap.getClass(idto).getName()+":"+documentRdn, dataBaseMap.getClass(idtoDestinatario).getName()+":"+destinatarioRdn, description, email, success);
		return success;
			/*
			ik.createPrototype(idto, level, userRol, user, usertask, sess);
			ik.setValue(ido, idto, idProp, oldValue, newValue, userRol, user, usertask, s);
			*/
			
	}
	
	@Override
	public boolean sendEmailWithServerLog(String email, String subject, String body) throws SQLException, NamingException, DataErrorException {
		
		ArrayList<String> fileNames=new ArrayList<String>();
		
		String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
		String path = serverHomeDir + "\\..\\..\\logs\\";
		fileNames.add(path+"wrapper.log");
		
		if(new File(path+"wrapper.log.1").exists()){
			fileNames.add(path+"wrapper.log.1");
		}
		
		String zipPath=path+"wrapper.zip";
		Zip.zip(fileNames, zipPath);
			
		ArrayList<String> filesList=new ArrayList<String>();
		filesList.add(zipPath);
		boolean success=false;
		Email.sendEmail(email, subject, body, true, filesList);
		
		return success;
			
	}
	
	private void insertEmailLog(String email_remitente,String documentoEnviado,String destinatario,String descripcion,String email,boolean enviado) throws SQLException, NamingException{
		//Evitamos que la descripcion o el email ocupen mas de 100 ya que es el maximo que permiten esas columnas en base de datos
		if(descripcion.length()>100){
			descripcion=descripcion.substring(0, 100);
		}
		if(email.length()>100){
			email=email.substring(0, 100);
		}
		
		long date=System.currentTimeMillis()/Constants.TIMEMILLIS;
		String sql = "INSERT INTO log_email(rdn,email_remitente,fecha,documentos,destinatario,\"descripción\",email,enviado) values('" + date + "','" + email_remitente + "'," + date + ",'" + documentoEnviado + "','" + destinatario + "','" + descripcion + "','" + email + "'," + enviado + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "UPDATE log_email SET rdn=\"tableId\" WHERE rdn='"+date+"'";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	
	private EmailConfiguration getEmailConfiguration(int idoMiEmpresa) throws SQLException, NamingException{
		EmailConfiguration emailConfiguration=null;
		//GenerateSQL generateSQL = new GenerateSQL(this.factConnDB.getGestorDB());
		String function = generateSQL.getDecryptFunction(InstanceService.keyEncrypt, "password");
		String sql = "select \"puerto_SMTP\",email_remitente,autenticar,protocolo_seguridad.rdn,\"servidor_SMTP\",nombre_remitente,"+function+" from email_smtp, protocolo_seguridad " +
				"where mi_empresa=" + QueryConstants.getTableId(idoMiEmpresa) + " and seguridad_conexion=protocolo_seguridad.\"tableId\" and activo=true";
		
		ConnectionDB con = null; 
		Statement st = null;
		ResultSet rs = null;
		try {
			con = factConnDB.createConnection(false); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			System.out.println(sql);
			if (rs.next()) {
				emailConfiguration=new EmailConfiguration();
				emailConfiguration.setIdoMiempresa(idoMiEmpresa);
				emailConfiguration.setPuerto_SMTP(rs.getInt(1));
				emailConfiguration.setEmail_remitente(rs.getString(2));
				emailConfiguration.setAutenticar(rs.getBoolean(3));
				emailConfiguration.setSeguridad_conexion(Email.TipoConexionSegura.valueOf(rs.getString(4)));
				emailConfiguration.setServidor_SMTP(rs.getString(5));
				emailConfiguration.setNombre_remitente(rs.getString(6));
				emailConfiguration.setPassword(generateSQL.getDecryptData(rs,7));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		return emailConfiguration;
	}
	
	public static String getDelegationRdn(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String rdn = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT d.rdn FROM "+generateSQL.getCharacterBegin()+"delegación"+generateSQL.getCharacterEnd()+" as d,"+generateSQL.getCharacterBegin()+"aplicación"+generateSQL.getCharacterEnd()+" as a WHERE a."+generateSQL.getCharacterBegin()+"delegación"+generateSQL.getCharacterEnd()+"=d."+generateSQL.getCharacterBegin()+"tableId"+generateSQL.getCharacterEnd();
		ConnectionDB conDB = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection con = conDB.getBusinessConn();
			st = con.createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			if (rs.next()) {
				rdn = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			fcdb.close(conDB);
		}
		return rdn;
	}
	
	public static String getBusinessName(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String rdn = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT d.nombre FROM "+generateSQL.getCharacterBegin()+"mi_empresa"+generateSQL.getCharacterEnd()+" as d,"+generateSQL.getCharacterBegin()+"aplicación"+generateSQL.getCharacterEnd()+" as a WHERE a."+generateSQL.getCharacterBegin()+"mi_empresa"+generateSQL.getCharacterEnd()+"=d."+generateSQL.getCharacterBegin()+"tableId"+generateSQL.getCharacterEnd();
		ConnectionDB conDB = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection con = conDB.getBusinessConn();
			st = con.createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			if (rs.next()) {
				rdn = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			fcdb.close(conDB);
		}
		return rdn;
	}
	
	public static String getMainDelegationRdn(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String rdn = null;
		GenerateSQL generateSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT deleg.rdn FROM \"delegación\" as deleg,mi_empresa as me,\"aplicación\" as a WHERE a.mi_empresa=me.\"tableId\" AND deleg.\"tableId\"=me.delegacion_central";
		ConnectionDB conDB = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;
		try {
			Connection con = conDB.getBusinessConn();
			st = con.createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			if (rs.next()) {
				rdn = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			fcdb.close(conDB);
		}
		return rdn;
	}
}
