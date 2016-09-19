package dynagent.server.ejb;

/*
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

//import java.lang.*;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.RemoveException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.License;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.message;
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
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.server.communication.ChangesListener;
import dynagent.server.dbmap.DBQueries;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.exceptions.ConnectionException;
import dynagent.server.exceptions.MessageException;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.migration.PreprocessMigration;
import dynagent.server.replication.PreprocessReplica;
import dynagent.server.services.FactsAdapter;
import dynagent.server.services.InstanceService;
import dynagent.server.services.QueryService2;
import dynagent.server.services.XMLConstants;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.server.web.paytef;
import dynagent.server.web.paytef_response;
import dynagent.common.knowledge.action;

/*class threadContextData extends Object {
	public contextData oldData = null;

	public contextData currData = null;
}*/

public class InstanceEJB implements SessionBean, Instance/*, IAsigned*/ {

	private static final long serialVersionUID = 1L;

	private InstanceService m_IS;
	
	/** Mapa de toda la base de datos además del modelo */
	private DataBaseMap dataBaseMap;

	private IKnowledgeBaseInfoServer ik;
	
	private String gestorDB;
	
	private String dynaGlobal;
	
	private FactoryConnectionDB factConnDB = null;
	
	private boolean seHaceReplica = false;

	SessionContext ejbc = null;
	
	private Boolean debugMode;
	
	private Context initCtx;
	
	private String databaseIP;
	
	/*public void setMetaData(Element elem, String user, Integer userRol) throws RemoteException, MessageException {
		GetData gdt = new GetData(elem);
		try {
			LinkedList<dynagent.common.basicobjects.Instance> gdtI = gdt.getInstances();
			for (int i=0;i<gdtI.size();i++) {
				dynagent.common.basicobjects.Instance gdtIi = gdtI.get(i);
				String sql = "INSERT INTO INSTANCES(IDTO, IDO, PROPERTY, QMAX, QMIN, " +
						"VALUE, VALUECLS, name, OP) VALUES (" + gdtIi.getIDTO() + "," + 
						gdtIi.getIDO() + "," + gdtIi.getPROPERTY() + "," + gdtIi.getQMAX() + "," + 
						gdtIi.getQMIN() + "," + gdtIi.getVALUE() + "," + gdtIi.getVALUECLS() + "," + 
						gdtIi.getNAME() + "," + gdtIi.getOP() + ")";
				m_IS.dbExecUpdate(sql);
			}
			LinkedList<Properties> gdtP = gdt.getProperties();
			for (int i=0;i<gdtP.size();i++) {
				Properties gdtPi = gdtP.get(i);
				String sql = "INSERT INTO PROPERTIES(PROP, NAME, CAT, VALUE, VALUECLS, OP, MASK, Q_MIN, Q_MAX, " +
						"LENGTH, INV) VALUES (" + gdtPi.getPROP() + "," + gdtPi.getNAME() + "," + 
						gdtPi.getCAT() + "," + gdtPi.getVALUE() + "," + gdtPi.getVALUECLS() + "," + 
						gdtPi.getOP() + "," + gdtPi.getMASK() + "," + gdtPi.getQMIN() + "," + 
						gdtPi.getQMAX() + "," + gdtPi.getLENGTH() + "," + gdtPi.getPROPINV() + ")";
				m_IS.dbExecUpdate(sql);
			}
			LinkedList<T_Herencias> gdtH = gdt.getHierarchies();
			for (int i=0;i<gdtH.size();i++) {
				T_Herencias gdtHi = gdtH.get(i);
				String sql = "INSERT INTO T_HERENCIAS(ID_TO,ID_TO_Padre) VALUES (" + 
						gdtHi.getID_TO() + "," + gdtHi.getID_TO_Padre() + ")";
				m_IS.dbExecUpdate(sql);
			}
			LinkedList<Access> gdtA = gdt.getAccesses();
			for (int i=0;i<gdtA.size();i++) {
				Access gdtAi = gdtA.get(i);
				String sql = "INSERT INTO ACCESS(DENNIED, TASK, USERROL, [USER], ACCESSTYPE, IDTO, IDO, PROP, " +
						"VALUE, VALUECLS) VALUES (" + gdtAi.getDENNIED() + "," + 
						gdtAi.getTASK() + "," + gdtAi.getUSERROL() + "," + gdtAi.getUSER() + "," + 
						gdtAi.getACCESSTYPE() + "," + gdtAi.getIDTO() + "," + 
						gdtAi.getIDO() + "," + gdtAi.getPROP() + "," + gdtAi.getVALUE() + "," + 
						gdtAi.getVALUECLS() + ")";
				m_IS.dbExecUpdate(sql);
			}
			HashMap<Integer,Integer> idoFicticioReal = new HashMap<Integer,Integer>();
			LinkedList<O_Datos_Attrib> gdtBc = gdt.getBusinessClasses();
			for (int i=0;i<gdtBc.size();i++) {
				O_Datos_Attrib gdtBci = gdtBc.get(i);
				//generar ido si es negativo
				Integer oldIdo = gdtBci.getIDO();
				if (oldIdo!=null// && oldIdo<0
	) {
					Integer newIdo = null;
					if (idoFicticioReal.containsKey(oldIdo))
						newIdo = idoFicticioReal.get(oldIdo);
					else {
						try {
							newIdo = preInsertRowObject(gdt, gdtBci, idoFicticioReal);
						} catch (NotFoundException e) {
							e.printStackTrace();
							throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error NotFound:");
						} 
					}
					//comprobacion tambien del value
					Integer oldValue = gdtBci.getVALNUM();
					Integer newValue = null;
					if (oldValue!=null// && oldIdo<0
	) {
						if (idoFicticioReal.containsKey(oldValue))
							newValue = new Integer(idoFicticioReal.get(oldValue));
						else {
							try {
								newValue = new Integer(preInsertRowObject(gdt, gdtBci, idoFicticioReal));
							} catch (NotFoundException e) {
								Auxiliar.errorEJB("InstEJB, setMetaData, error NotFound:", e);
							} 
						}
					}
					if (newIdo!=null && newValue!=null) {
						String sql = "INSERT INTO O_Datos_Atrib(ID_TO, ID_O, PROPERTY, VAL_NUM, VAL_TEXTO, VALUE_CLS, " +
								"Q_MIN, Q_MAX, OP) VALUES (" + gdtBci.getIDTO() + "," + newIdo + "," + 
								gdtBci.getPROPERTY() + "," + newValue + "," + gdtBci.getVALTEXTO() + "," + 
								gdtBci.getVALUECLS() + "," + gdtBci.getQMIN() + "," + gdtBci.getQMAX() + "," + 
								gdtBci.getOP() + ")";
						m_IS.dbExecUpdate(sql);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error SQL:");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error Naming:");
		} catch (SystemException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error System:");
		} catch (RemoteSystemException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error RemoteSystem:");
		} catch (CommunicationException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "InstEJB, setMetaData, error Communication:");
		}
	}
	
	private Integer preInsertRowObject(GetData gdt, O_Datos_Attrib gdtBci, HashMap<Integer,Integer> idoFicticioReal) 
			throws SQLException, NamingException, SystemException, NotFoundException, RemoteSystemException, CommunicationException {
		int oldIdo = gdtBci.getIDO();
		Integer newIdo = null;
		String rdn = null;
		Iterator iterador1 = gdt.getBusinessClasses().iterator();
		while (iterador1.hasNext()) {
			O_Datos_Attrib oda = (O_Datos_Attrib)iterador1.next();
			if (oda.getPROPERTY()!=null) {
				if (oda.getPROPERTY().equals(Constants.IdPROP_RDN) && oda.getVALTEXTO()!=null) {
					rdn = oda.getVALTEXTO();
					break;
				} 
			}
		}
		if (rdn!=null) {
			newIdo = insertRowObject(gdt, gdtBci, rdn);
			//m_IS.log(null, task, null, user, ido, idto, null, null, ins, "NEW_OBJ", action.NEW, empresa);
			if (newIdo!=null)
				idoFicticioReal.put(oldIdo, newIdo);
		} else if (oldIdo>0)
			newIdo = oldIdo;
		return newIdo;
	}
	
	private Integer insertRowObject(GetData gdt, O_Datos_Attrib gdtBci, String rdn) 
			throws NamingException, SQLException, SystemException, NotFoundException, RemoteSystemException, CommunicationException {
		String random = rdn + String.valueOf((new Random()).nextInt());
		Integer id_to = gdtBci.getIDTO();
		Integer ido = null;
		if (id_to!=null) {
			String sql = "insert into O_reg_instancias(id_to, rdn) values ("
					+ id_to + ",'" + random + "')";
			m_IS.dbExecUpdate(sql);
			sql = "Select id_O FROM O_reg_instancias WITH(nolock) WHERE rdn='"
					+ random + "'" + " AND id_to=" + id_to;
			Statement st = null;
			ResultSet rs = null;
			boolean stClosed = false, rsClosed = false;
			System.out.println("INSERT ROW");
			ConnectionDB con = factConnDB.createConnection(false);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				rs.next();
				ido = rs.getInt(1);
				st.close();
				stClosed = true;
				sql = "update o_reg_instancias SET rdn='"
						+ rdn.replaceAll("'", "''") + "' WHERE id_o=" + ido;
				System.out.println("sql3 " + sql);
				m_IS.dbExecUpdate(sql);
				if (ik.isSpecialized(id_to, Constants.IDTO_USER)) {
					sql = "INSERT INTO Usuarios(Login,ido_instance) VALUES('"
							+ rdn.replaceAll("'", "''") + "'," + ido + ")";
					ConnectionDB con2 = factConnDB.createConnection(false);
					try {
						st = con2.getBusinessConn().createStatement();
						st.executeUpdate(sql);
						stClosed = false;
						Iterator itr = gdt.getBusinessClasses().iterator();
						ArrayList<Integer> roles = new ArrayList<Integer>();
						while (itr.hasNext()) {
							O_Datos_Attrib eRol = (O_Datos_Attrib)itr.next();
							if (eRol.getIDTO() == Constants.IDTO_USERROL) {
								System.out.println("ROL USER");
								Integer rol = new Integer(eRol.getIDO());
								roles.add(rol);
								sql = "INSERT INTO UsuarioRoles(USUARIO,ROL) VALUES('"
										+ rdn.replaceAll("'", "''") + "'," + rol + ")";
								st.executeUpdate(sql);
							}
						}
						st.close();
						stClosed = true;
					} finally {
						System.out.println("stClosed " + stClosed);
						if (rs != null && !rsClosed && !stClosed)
							rs.close();
						if (st != null && !stClosed)
							st.close();
						con2.close();
					}
				}
				if (ik.isSpecialized(id_to, Constants.IDTO_USERROL)) {
					sql = "INSERT INTO Roles(ID_ROL,NAME_ROL) VALUES(" + ido
							+ ",'" + rdn.replaceAll("'", "''") + ")";
					System.out.println("ESPECI ROL");
					m_IS.dbExecUpdate(sql);
				}
			} finally {
				System.out.println("stClosed " + stClosed);
				if (rs != null && !rsClosed && !stClosed)
					rs.close();
				if (st != null && !stClosed)
					st.close();
				con.close();
			}
		}
		return ido;
	}*/
	
	public Element getMetaData(String user,boolean configurationMode, int business) throws RemoteException, MessageException {
		setDatabase(business);
		Element root = null;
		try {
			root = MetaData.getMetaData(user, configurationMode, factConnDB, business, ik, dataBaseMap);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos de inicialización de la aplicación no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos de inicialización de la aplicación no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de los datos de inicialización de la aplicación");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de los datos de inicialización de la aplicación");
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de los datos de inicialización de la aplicación");
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de los datos de inicialización de la aplicación");
		} catch (ParseException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de los datos de inicialización de la aplicación");
		}
		return root;
	}
	
	public message getUserInfo(String user, String pwd, boolean configurationMode, int business) throws RemoteException, MessageException {
		setDatabase(business);
		Statement st = null;
		ResultSet rs = null;
		message res = new message(dynagent.common.communication.message.MSG_CONFIRM);
		//res.setUser(user);
		System.out.println("LOGIN");
		System.out.println("USER:" + user);
		GenerateSQL gSQL = new GenerateSQL(gestorDB);
		String function = gSQL.getDecryptFunction(InstanceService.keyEncrypt, "us.password");
		
		String sql = "SELECT " + function + ", us." + Constants.PROP_RDN + " , usrol." + Constants.PROP_RDN + " "+
			"from " + gSQL.getCharacterBegin() + "user"  + gSQL.getCharacterEnd() + " as us " +
			"inner join userrol as usrol on(us." + gSQL.getCharacterBegin() + "userRol" + gSQL.getCharacterEnd() + "=usrol." + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ") " + 
			"WHERE us." + Constants.PROP_RDN + "='" + user.replaceAll("'", "''") + "'";
		if (configurationMode)
			sql += " AND usrol." + Constants.PROP_RDN + " " + gSQL.getLike() + " '"+Constants.ADMIN_ROL+"'";
		//String pwdPre = pwd.replaceAll("'", "''");
		//if (!pwdPre.equals(helperConstant.byPassKey))
			//sql += " AND Pwd=" + function;
		ConnectionDB con = null;
		try {
			/*Context initCtx = new InitialContext();
			Context myEnv = (Context) initCtx.lookup("java:comp/env");
			Integer empresa = (Integer) myEnv.lookup("BUSINESS");
			res.setBusiness(empresa);*/
			boolean denegado = true;
			con = factConnDB.createConnection(false);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			String userRols=null;
			while (rs.next()) {
				String pwdDB = gSQL.getDecryptData(rs, 1);
				//System.out.println("PWD_DB:" + pwdDB);
				//System.out.println("PWD:" + pwd);
				if (pwdDB!=null && pwdDB.equals(pwd)) {
					String userDB = rs.getString(2);
					if (userDB.equals(user)) { //para que coincidan las mayusculas
						//Context initCtx = new InitialContext();
						//Context myEnv = (Context) initCtx.lookup("java:comp/env");
						//Integer empresa = (Integer) myEnv.lookup("BUSINESS");
						res.setBusiness(/*empresa*/business);
						res.setUser(user);
						if(userRols!=null){
							userRols+=";"+rs.getString(3);
						}else{
							userRols=rs.getString(3);
						}
						//String nombre = rs.getString(3);
						//if (!rs.wasNull())
							//res.addDirectAttribute(UserConstants.USER_NAME, nombre);
						//res.addPropertie(properties.id, rs.getInt(4));
						System.out.println("EXITO");
						denegado = false;
					}
				}
			}
			
			res.setUserRols(userRols);
			
			if (denegado) {
				System.out.println("DENEGADO");
				res.setResultCode(dynagent.common.communication.message.ERROR_LOGIN);
			}else{
				System.out.println("LOGGIN OK");
			}
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de logueo");
		} catch (IllegalDataException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de logueo");
		} catch (NullPointerException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de logueo");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la consulta de logueo");
		} finally {
			try {
				if (rs!=null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			} catch (SQLException e) {
				e.printStackTrace();
				throw new MessageException(message.ERROR_SYSTEM, "Error cerrando conexion");
			}
		}
	}

	public Element test(Element arg) {
		jdomParser.print("INST EJB, recibido ", arg);
		return new Element("TEST_RES");
	}

    public void lockObject(int ido, int idto, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
    	setDatabase(business);
    	try {
    		m_IS.lockObject(ido, idto, user, false);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		}
    }
    
    public void lockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
    	setDatabase(business);
    	try {
    		m_IS.lockObject(listIdo, user, false);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		}
    }

    public void unlockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
    	setDatabase(business);
    	try {
    		m_IS.unlockObjects(listIdo, user);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		}
    }

    public void unlockObject(int ido, int idto, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
    	setDatabase(business);
    	//try {
    		m_IS.unlockObject(ido, idto, user);
		//} catch (InstanceLockedException e) {
		//	e.printStackTrace();
		//	throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		//}
    }
    
    public void resetLocks(String user, int business) throws RemoteException, MessageException {
    	setDatabase(business);
    	String keyLock = user;
		try {
	    	//busqueda de los bloqueos de este usuario
	    	ArrayList<String> ikeys = m_IS.getLocksByLogin(keyLock);
	    	
	    	if (ikeys.size()>0) {
	    		String sql = "delete from locksid where id in(" + Auxiliar.arrayToStringComillas(ikeys, ",") + ")";
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    public void resetReservations(String user, int business) throws RemoteException, MessageException {
    	setDatabase(business);
    	GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		try {
    		String sql = "delete from reservation " +
    				"where " + gSQL.getCharacterBegin() + "user" + gSQL.getCharacterEnd() + "='" + user.replaceAll("'", "''") + "'";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		} catch (Exception e) {
			e.printStackTrace();
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
    }
    
    public License getLicense(int business) throws RemoteException, SystemException{
    	setDatabase(business);
    	try {
			return m_IS.serverGetLicense();
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
    
    public void setLicense(int business,License license) throws RemoteException, SystemException{
    	setDatabase(business);
    	try{
    		m_IS.serverUpdateLicense(license);
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
    
    public HashSet<String> getNumberOfSessions(int business) throws RemoteException, SystemException{
    	setDatabase(business);
    	try {
			return m_IS.serverGetNumberOfSessions();
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
    /*public boolean isSameIp(String user, String ip) throws RemoteException, SystemException{
    	try {
			return m_IS.serverIsSameIp(user, ip);
    	} catch (NamingException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_SISTEMA, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		} catch (SQLException e) {
			SystemException sysEx = new SystemException(SystemException.ERROR_DATOS, e.getMessage());
			sysEx.setStackTrace(e.getStackTrace());
			throw sysEx;
		}
    }*/
    
	public IndividualData getObj(int ido, int idto, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults, int business) throws RemoteException, MessageException {
		setDatabase(business);
		IndividualData aipd = null;
		try {
			aipd = m_IS.serverGetFactsInstance(ido, idto, user, lock, levels, lastStructLevel, returnResults);
			if (returnResults && aipd.getAIPropertyDef().size()==0) {
				System.out.println("El individuo tiene ido " + ido);
				String errorMessage = "El individuo no existe en base de datos";
				throw new MessageException(message.ERROR_SYSTEM, errorMessage);
			}
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta de obtención de individuos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El individuo no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObj, error NotFound:"*/);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El individuo no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return aipd;
	}
	
	public IndividualData getObj(HashMap<Integer,HashSet<Integer>> ids, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults, int business) throws RemoteException, MessageException {
		setDatabase(business);
		IndividualData aipd = null;
		try {
			aipd = m_IS.serverGetFactsInstance(ids, user, lock, levels, lastStructLevel, returnResults);
			if (returnResults && aipd.getAIPropertyDef().size()==0) {
				System.out.println("Los individuos tienen tableIds " + ids);
				HashSet<Integer> tableIds = Auxiliar.getTableIdsHashSet(ids);
				String errorMessage = "Los individuos " +  tableIds + " no existen en base de datos";
				throw new MessageException(message.ERROR_SYSTEM, errorMessage);
			}
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta de obtención de individuos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El individuo no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObj, error NotFound:"*/);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El individuo no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return aipd;
	}
	
	public IndividualData getObjOfClass(int idto, String user, boolean lock, int levels, boolean lastStructLevel, int business) throws RemoteException, MessageException {
		setDatabase(business);
		IndividualData aipd = null;
		try {
			aipd = m_IS.serverGetFactsInstanceOfClass(idto, user, lock, levels, lastStructLevel);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta de obtención de tipos de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObjOfClass, error NotFound:"*/);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El tipo de datos no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El tipo de datos no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return aipd;
	}

	public IndividualData getObjOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel, int business) throws RemoteException, MessageException {
		setDatabase(business);
		IndividualData aipd = null;
		try {
			aipd = m_IS.serverGetFactsInstanceOfClassSpecialized(idto, user, lock, levels, lastStructLevel);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta de obtención de tipos de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObjOfClass, error NotFound:"*/);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El tipo de datos no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "El tipo de datos no está correcto en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return aipd;
	}
	
	public String query(Element elem, Integer uTask, int mode, int business) throws RemoteException, MessageException {
		setDatabase(business);
		try {
			queryData res = m_IS.query(elem, uTask, mode);
			return res.toString();
		} catch (ConnectionException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "No hay conexion con el servidor"/*"InstEJB, query, error SQL:"*/);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta"/*"InstEJB, query, error SQL:"*/);
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (NoSuchElementException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (IncoherenceInMotorException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (NoSuchColumnException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		}
	}

	public List<List<String>> queryRules(String sql, int business,boolean update) throws RemoteException, MessageException {
		setDatabase(business);
		try {
			return m_IS.queryRules(sql,update);
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "La consulta no es correcta");
		}
	}
	
	public HashMap<String,String> prePrint(String className, int business) throws RemoteException, MessageException {
		setDatabase(business);
		HashMap<String, String> rAttributes = null;
		try {
			rAttributes = m_IS.serverGetPrePrintSequence(className);
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta del report";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObjOfClass, error NotFound:"*/);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return rAttributes;
    }

	public HashMap<String,String> report(Element root, String user, int uTask, String className, /*String nameProject,*/ boolean directImpresion, Integer idoFormat, int business) throws RemoteException, MessageException {
		setDatabase(business);
		HashMap<String, String> rAttributes = null;
		try {
			rAttributes = m_IS.serverGetReport(root, user, uTask, className, /*nameProject,*/ directImpresion, idoFormat, true,false/*Le ponemos true aunque no se utiliza luego en InstanceService, esta logica se hace en el communicator del applet*/);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta del report";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObjOfClass, error NotFound:"*/);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getObjOfClass, error NotFound:"*/);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return rAttributes;
    }

	public IKnowledgeBaseInfo getIk() {
		return ik;
	}

	public void ejbCreate() throws CreateException, RemoteException{
		System.out.println("INSTANCE:CREATE");
		try {
			initCtx = new InitialContext();
			Context myEnv = (Context) initCtx.lookup("java:comp/env");
			databaseIP = (String) myEnv.lookup("databaseHOST"); //no necesario bajo JBoss
			
			dynaGlobal = (String) myEnv.lookup("BUSINESS_GLOBAL");
			
			//obtener modo a partir de ejb-jar.xml
			debugMode = (Boolean) myEnv.lookup("debugMode");
			System.out.println("debugMode " + debugMode);
			if (debugMode)
				System.out.println("Server en modo debug");
			else
				System.out.println("Server en modo de confianza");

			gestorDB = (String) myEnv.lookup("gestorDB");
			System.out.println("gestorDB " + gestorDB);
			String replica = (String) myEnv.lookup("replica");
			seHaceReplica = replica.equals("YES");
//			factConnDB = new FactoryConnectionDB(business, false, databaseIP, gestorDB);
//			/*try {
//				Integer port = (Integer) myEnv.lookup("PORT");
//				System.out.println("port " + port);
//				factConnDB.setPort(port);
//			} catch (NamingException e) {
//				;
//			}*/
//			dataBaseMap = new DataBaseMap(factConnDB, false);
//			m_IS = new InstanceService(factConnDB, initCtx, debugMode);
//			m_IS.setDataBaseMap(dataBaseMap);
//
//			GenerateSQL generateSQL = new GenerateSQL(gestorDB);
//			String gen = generateSQL.getIsolationReadUncommited();
//			if (gen!=null)
//				AuxiliarQuery.dbExecUpdate(factConnDB, gen, false);
//			
//			//inicializar motor
//			//ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(fcdb,business), business, m_IS, Constants.RULER, Constants.USER_SYSTEM, Constants.RULER_SERVER);
//			//ik.setAsigned(m_IS);
//			ik = new ServerEngine(factConnDB);
//			m_IS.setIk(ik);
//			if (seHaceReplica) {
//				IReplication ir = new ReplicationEngine(factConnDB);
//				m_IS.setIr(ir);
//			}
			//almacenar referencia a IKnowledgeBase
			
			//PRUEBA PARA LOS BEAN
			/*try {
				Test11.puebaBeansMensajes(initCtx, factConnDB);
			} catch (CreateException e) {
				InstanceEJBAux.error("InstEJB, ejbCreate, error Create:", e);
			}*/

		} catch (NamingException e) {
			AuxiliarEJB.error("InstEJB, ejbCreate, error Naming:", e);
		}
	}

	public void ejbRemove() {
		System.out.println("INSTANCE:REMOVE");
		/*try {
			GenerateSQL generateSQL = new GenerateSQL(gestorDB);
			String gen = generateSQL.getIsolationRepeatableRead();
			if (gen!=null)
				AuxiliarQuery.dbExecUpdate(factConnDB, gen);
		} catch (SQLException ex) {
			AuxiliarEJB.error("InstEJB, ejbRemove, error SQL:" + ex.getMessage());
		} catch (NamingException ex) {
			AuxiliarEJB.error("InstEJB, ejbRemove, error Naming:" + ex.getMessage());
		}*/
		/*try {
			m_pool.close();
		} catch (SQLException ex) {
			error("unsetEntityContext: " + ex.getMessage());
		}*/
	}

	public void setSessionContext(SessionContext context) {
		System.out.println("INSTANCE: SET CONTEXT");
		this.ejbc = context;
		//this.context = context;
	}

	//public void unsetSessionContext() {
	//	System.out.println("UNSET CTX");
	//}
	public void ejbActivate() {
		System.out.println("INSTANCE:ACTIVATE");
	}

	public void ejbPassivate() {
		System.out.println("INSTANCE:PASIVATE");
	}

	//public void ejbLoad() {
	//	System.out.println("LOAD");
	//}

	//public void ejbStore() {
	//	System.out.println("INSTANCE:STORE");
	//}

	public void ejbPostCreate(String rdn) {
		System.out.println("INSTANCE:POST");

	}

    public selectData getTasks(Integer userRol, String user, int business) throws RemoteException, MessageException {
    	setDatabase(business);
    	selectData sd = null;
    	try {
			sd = m_IS.serverGetTasks(userRol, user);
		} catch (InstanceLockedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_LOCKED, e.getMessage());
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error en la consulta de las tareas";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage/*"InstEJB, getTasks, error NotFound:"*/);
		} catch (ServerException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		} catch (RuleEngineException e) {
			e.printStackTrace();
			String errorMessage = e.getUserMessage();
			if (errorMessage==null)
				errorMessage = "Los datos no son correctos en base de datos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		return sd;
    }

	public void logError(String user, String debug, String error, String subject, int business) throws MessageException {
		setDatabase(business);
		try {
			m_IS.logError(user, debug, error, subject);
		} catch (SystemException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, "Error en la escritura en la tabla de log");
		}
	}

    /*public void reserveIndividual(Integer userRol, String user, IndividualData indData) throws RemoteException, MessageException {
    	//System.out.println("Inicio de la funcion insertReserve");
		ArrayList<IPropertyDef> aipd = indData.getAIPropertyDef();
		try {
			m_IS.reserveIndividual(userRol, user, aipd);
		} catch (SystemException e) {
			e.printStackTrace();
			String errorMessage = "Error al reservar individuos contributivos";
			throw new MessageException(message.ERROR_SYSTEM, errorMessage);
		}
		//System.out.println("Fin de la funcion insertReserve");
	}*/

    public Integer transitionDataMigration(Element xmlData, int business) throws RemoteException, MessageException {
    	setDatabase(business);
    	Integer idRoot = null;
    	try {
			idRoot = m_IS.sendDataTransition(xmlData);
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncoherenceInMotorException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncompatibleValueException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (CardinalityExceedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (JDOMException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (OperationNotPermitedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		}
		return idRoot;
    }
	public HashMap<DomainProp, Double> reserve(ArrayList<Reservation> reservationList, String user, Integer windowSession, int business) throws RemoteException, MessageException {
		setDatabase(business);
		HashMap<DomainProp, Double> hMap;
		try {
			hMap = m_IS.reserve(reservationList, user, windowSession);
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncoherenceInMotorException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncompatibleValueException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (CardinalityExceedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (OperationNotPermitedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		}
		return hMap;
	}
	
	public void deleteReservation(ArrayList<Reservation> reservationList, String user, Integer windowSession, int business) throws RemoteException, MessageException {
		setDatabase(business);
		try {
			m_IS.deleteReservation(reservationList, user, windowSession);
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncoherenceInMotorException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (IncompatibleValueException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (CardinalityExceedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (NamingException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		} catch (OperationNotPermitedException e) {
			e.printStackTrace();
			throw new MessageException(message.ERROR_SYSTEM, e.getMessage());
		}
	}

	/*PROBAR
	 * (non-Javadoc)
	 * @see dynagent.server.ejb.Instance#transitionObject(java.lang.Integer, java.lang.String, java.lang.Object, org.jdom.Document, java.lang.String, java.lang.Integer, int, boolean, boolean, java.lang.String)
	 * Viene por replica delete de un objeto no existe en local
	 * Replica por web (o la vaquera) un elemento que no existe
	 * Replica por web un create ine que no existe
	 * Replica por tienda un elemento no existe
	 * Viene por replica un rdnvalue de objeto no existe
	 * Probar eliminar un ticket desde tienda y desde central
	 * Probar memo incremental de la web
	 */
    public Changes transitionObject(Integer userRol, String user, Object inputData,String msguid, Integer windowSession, 
    		int business, boolean migration, boolean keepTableIds, String replicaSource) throws RemoteException/*, InstanceLockedException, DataErrorException, OperationNotPermitedException*/ {
    	System.out.println("transitionObject que recibe facts con replicaSource "+replicaSource);
    	boolean globalurl=replicaSource!=null && replicaSource.equals(Constants.GLOBAL_URL);
    	if(globalurl) replicaSource=null;
    	setDatabase(business);
		Changes res = new Changes();
		try {
	    	boolean correcto = m_IS.putMsguid(msguid);	    	
	    	if (correcto) {	    						
	    			//Si llega 
	    			Document structuredData=null;
					HashSet<String> destinations=new HashSet<String>();
					ArrayList<Reservation> aReservation=null;
					String debugTxt=null;
																
					if(msguid.contains("initinstall")){		
						ArrayList<IPropertyDef> ipdList=((IndividualData)inputData).getAIPropertyDef(); 
						for (IPropertyDef ipd : ipdList) {
							String actionStr=ipd.getOrder()==action.SET?XMLConstants.ACTION_SET:XMLConstants.ACTION_NEW;							
							QueryService2 queryService2 = new QueryService2(ipd.getIDTO(), false, dataBaseMap, dataBaseMap.getFactoryConnectionDB(), 2,
																			false, false, Constants.USER_SYSTEM, false, m_IS,actionStr);
							//queryService2.setMigration(true);
							structuredData = queryService2.getData();
							System.out.println("initinstall "+ipd.getCLASSNAME());
							String newMsguid=ipd.getDestinationSystem()+"#"+System.currentTimeMillis();
							insertIntoReplica(structuredData,destinations,replicaSource,newMsguid,debugTxt,ipd.getDestinationSystem());
						}
						return res;
					}
					
					if(inputData instanceof IndividualData){
						Element indiv = ((IndividualData)inputData).toElement();																										
						structuredData=new Document(indiv);
						//System.out.println("DBG INT:"+jdomParser.returnXML(structuredData));
						debugTxt=jdomParser.returnXML(structuredData);
						aReservation = messageFactory.buildReservation(indiv);
						structuredData = FactsAdapter.factsXMLToDataXML(dataBaseMap, structuredData, replicaSource!=null,replicaSource!=null);
					}else
						structuredData=(Document)inputData;
					
					Element rootElement=structuredData.getRootElement();						
					Element objectsNode = rootElement.getChild(XMLConstants.TAG_OBJECTS);	
					if(replicaSource!=null||globalurl){
						//viene de otra maquina, hay que resolver los idos
						Changes resTemp=PreprocessReplica.setLocalIdos(objectsNode,dataBaseMap,replicaSource);						
						res.add(resTemp);
						
					}
					
					//Changes resTemp = m_IS.serverTransitionObject(/*userRol, */user, indData, windowSession, false, keepTableIds, replicaSource);
					Changes resTemp = m_IS.serverTransitionObjectImp(user, structuredData, aReservation,  windowSession,migration, false, keepTableIds, replicaSource);
					res.add(resTemp);
					
					if(replicaSource==null && inputData instanceof IndividualData){
						//solo para ordenes directas, es decir desde aplet, que solo llega como facts
						processExternalOrder((IndividualData)inputData,res);						
					}
					
		    		//si tiene algun destination -> se replica		
	    			//System.out.println("PREVIO INSERT REPLICA, STRUCT DATA "+jdomParser.returnXML(structuredData));
	    			//
					
					//Genero facts a partir de document, aunque de entrada pueda llegar como facts, para que asigne rdn y rdn value la funcion de conversion
					//Vuelvo a poner idos originales para no perder la negatividad, que es la que indica si debe ser creado, ya que las ordenes en fact son del propio fact, no del individuo 
					ArrayList<Element> scan=(ArrayList<Element>)jdomParser.elementsWithAt(structuredData.getRootElement(), XMLConstants.ATTRIBUTE_IDO_ORDER, true);
					HashMap<String,Integer> newOrderMap=new HashMap<String,Integer>();
					for(Element tmp:scan){
						int idoorder=Integer.parseInt(tmp.getAttributeValue(XMLConstants.ATTRIBUTE_IDO_ORDER));
						if(idoorder<0){
							String key=tmp.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm)+"#"+tmp.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);
							newOrderMap.put(key, idoorder);
						}
					}
					for(Element tmp:scan){
						int idoorder=Integer.parseInt(tmp.getAttributeValue(XMLConstants.ATTRIBUTE_IDO_ORDER));
						String key=tmp.getAttributeValue(XMLConstants.ATTRIBUTE_IDTOm)+"#"+tmp.getAttributeValue(XMLConstants.ATTRIBUTE_RDN);
						Integer newOrder=newOrderMap.get(key);
						//es posible que en un nodo referencia no exista el atributo ido order, o que erroneamente esté el ya creado en positivo
						if(newOrder!=null){
							if(idoorder>0) System.out.println("UPDATE IDOORDER "+idoorder+">"+newOrder);
							idoorder=newOrder;
						}
						if(idoorder>0) idoorder=QueryConstants.getTableId(idoorder);
						tmp.setAttribute(XMLConstants.ATTRIBUTE_TABLEID,""+idoorder);
					}	
					insertIntoReplica(structuredData,destinations,replicaSource,msguid,debugTxt,null);
	    	} else
	    		res = new Changes();
		} catch (NotFoundException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error NotFound:", e);
		} catch (SQLException e) {
			System.err.println("\n"+e.getMessage()+"\n  "+e.getLocalizedMessage());
			e.printStackTrace();
			AuxiliarEJB.error("InstEJB, transitionObject, error SQL:", e);
		} catch (NamingException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error Naming:", e);
		} catch (SystemException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error System:", e);
		} catch (ApplicationException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error Application:", e);
		} catch (IncoherenceInMotorException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error IncoherenceInMotor:", e);
		} catch (IncompatibleValueException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error IncompatibleValue:", e);
		} catch (CardinalityExceedException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error CardinalityExceed:", e);
		} catch (JDOMException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error JDOM:", e);
		} catch (ParseException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error Parse:", e);
		} catch (RemoteSystemException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error RemoteSystem:", e);
		} catch (CommunicationException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error Communication:", e);
		} catch (OperationNotPermitedException e) {
			AuxiliarEJB.error("InstEJB, transitionObject, error OperationNotPermited:", e);
		}catch (DataErrorException e) {
			System.err.println("Data error exception");
			throw e;			
		}finally {
		}
		//System.out.println("Fin de la funcion transitionObject");
		return res;
	}
    
    private void insertIntoReplica(Document structuredData,HashSet<String> destinations,String replicaSource,String msguid_orig,String debugTxt,String forzarReplicaDest) throws DataErrorException, JDOMException, SQLException, NamingException{
    	boolean partirBloques=false;
    	IndividualData iData = FactsAdapter.DataXMLToFactsXML(dataBaseMap,structuredData,destinations,true);
								
		//Actualizar cambios de indices en fact para replicas
		//processFacts(resTemp, iDataElem);
		//test: imprimir xml de datos
		//Document docData = FactsAdapter.factsXMLToDataXML(dataBaseMap, new Document(iDataElem));

		int pos=0,block=0;
		//System.out.println("ENTRA insertIntoReplica "+iData.getAIPropertyDef().size());
			
		IndividualData iTmp=partirBloques? new IndividualData():iData;
		Iterator<IPropertyDef> itr=partirBloques? iData.getAIPropertyDef().iterator():null;	
		while(!partirBloques||itr.hasNext()){			
			if(partirBloques) iTmp.addIPropertyDef(itr.next());		
			
			if(!partirBloques || partirBloques && pos==10000 || !itr.hasNext()){
				//System.out.println("ALCANZADO POS "+pos+" size:"+iTmp.getAIPropertyDef().size());
				Element replicaMsg = new Element("REPLICA_MSG");				
				Element iDataElem = iTmp.toElement();		
				replicaMsg.addContent(iDataElem);
				String msguid=msguid_orig;
				if(partirBloques) msguid+=block++;//no debo añadir nada si no se parte para no modificar msguid
				//insertar en tabla de replica
				Calendar c = Calendar.getInstance();
				long seg = c.getInstance().getTimeInMillis()/Constants.TIMEMILLIS;
				String date = QueryConstants.secondsToDate(String.valueOf(seg), "dd/MM/yy");						
				String content=jdomParser.returnXML(replicaMsg).replaceAll("'", "''");
				//System.out.println("REPLICA CONTENT "+content);
				String sql = "insert into replica_msg("+(debugTxt!=null?"debug,":"")+"msguid,content,fecha,source,destination) values("+(debugTxt!=null?"'"+debugTxt.replaceAll("'", "''")+"',":"")+"'" + msguid + "','" + content + "','" + date + "'";
				if(replicaSource!=null){
					sql+=",'" + replicaSource+"'";
				}else{
					sql+=",NULL";
				}
				String strDestination=!destinations.isEmpty()?Auxiliar.hashSetStringToString(destinations, ","):"";
				if(forzarReplicaDest!=null&&forzarReplicaDest.length()>0){
					strDestination=forzarReplicaDest;
				}
				if(strDestination.length()>0){
					sql+=",'" +strDestination + "')";
				}else{
					sql+=",NULL)";
				}				
				DBQueries.execute(factConnDB, sql);
				if(!partirBloques) break;
				pos=0;			
				iTmp=new IndividualData();
			}else pos++;
		}
    }
    
    private void processExternalOrder(IndividualData inputData,Changes res) throws DataErrorException, SQLException, NamingException{
    	Element iDataElem = inputData.toElement();
		ArrayList<IPropertyDef> aIpropDefList=inputData.getAIPropertyDef();
		for (int i=0;i<aIpropDefList.size();i++) {
			IPropertyDef ipd = aIpropDefList.get(i);
			if(Auxiliar.equals(dataBaseMap.getPropertyName(ipd.getPROP()),"external_order") && ipd.getCLASSNAME().equals("TICKET_VENTA")){
				String[] order=ipd.getVALUE().split(";");
				int newTableId=0;
				if(order[0].contains("&")){
					//es un rdn temporal							
					ArrayList<ObjectChanged> chlist=res.getAObjectChanged();
					for (ObjectChanged ch:chlist) {								
						if (ch.getProp()!=null && ch.getProp()==2) {
							StringValue svold=(StringValue)ch.getOldValue();
							
							if(svold.getValue_s().equals(order[0])){
								StringValue svnew=(StringValue)ch.getNewValue();
								order[0]=svnew.getValue_s();						
								newTableId=QueryConstants.getTableId(ch.getNewIdo());
								break;
							}
						}
					}
				}
						
				paytef pt=new paytef();
				paytef_response ptr=pt.order(order);
				order[2]=ptr.paytefref.trim();
				
				if(!ptr.fallida&&ptr.aceptada&&newTableId!=0){						
					String newOrder=order[0];
					for(int ind=1;ind<4;ind++)
						newOrder+=";"+order[ind];
					
					String detail=		ptr.tipo+";"+ 					//0
										ptr.nombre_comercio+";"+		//1
										ptr.poblacion+";"+				//2
										ptr.codigo_comercio+";"+		//3
										ptr.ARC+";"+					//4
										ptr.HCP+";"+					//5
										ptr.idapp+";"+					//6
										ptr.app+";"+					//7
										ptr.num_tarjeta+";"+			//8
										ptr.fecha+";"+					//9
										ptr.hora+";"+					//10
										ptr.paytefref+";"+				//11
										ptr.num_autoriz+";"+			//12
										
										ptr.importe+";"+				//13													
										ptr.moneda+";"+					//14
										ptr.firma+";"+					//15
										ptr.contactless;				//16
					
					String sql="Update ticket_venta set \"external_order\"='"+newOrder+"',observaciones_internas='"+detail+"' where \"tableId\"="+newTableId;
					DBQueries.execute(factConnDB, sql);
					
					Iterator<Element> itr=jdomParser.elementsWithAt(iDataElem, "PROP","851", true).iterator();
					while(itr.hasNext()){
						Element el=itr.next();
						if(el.getName().equals("NEW_FACT")){
							el.setText(newOrder);
							break;
						}
					}
					itr=jdomParser.elementsWithAt(iDataElem, "PROP","225", true).iterator();//observaciones internas
					while(itr.hasNext()){
						Element el=itr.next();
						if(el.getName().equals("NEW_FACT")){
							el.setText(detail);
							break;
						}
					}
				}
				if(ptr!=null && ptr.fallida){		
					throw new DataErrorException("Fallo en el pago PAYTEF "+ptr.comentario+" "+ptr.ARC);
				}
				if(ptr!=null && !ptr.aceptada){												
					throw new DataErrorException("Pago DENEGADO PAYTEF "+ptr.comentario+" "+ptr.ARC);
				}
			}
		}
    	

    }
    
    private void processFacts(Changes resTemp, IndividualData iData) {
		//usar changes xa procesarlo
		System.out.println(resTemp);
		ArrayList<ObjectChanged> aOC = resTemp.getAObjectChanged();
		for (ObjectChanged oC : aOC) {
			int newIdo = oC.getNewIdo();
			int oldIdo = oC.getOldIdo();
			//reemplazar
			ArrayList<IPropertyDef> aipd = iData.getAIPropertyDef();
			for (IPropertyDef ipd : aipd) {
				int ido = ipd.getIDO();
				String value = ipd.getVALUE();
				if (ido==oldIdo) {
					((FactInstance)ipd).setIDO(newIdo);
				} else if (value!=null && String.valueOf(oldIdo).equals(value)){
					((FactInstance)ipd).setVALUE(String.valueOf(newIdo));
				}
			}
		}
    }
    
    private void processFacts(Changes resTemp, Element iDataElem) {
		//usar changes xa procesarlo
		System.out.println(resTemp);
		ArrayList<ObjectChanged> aOC = resTemp.getAObjectChanged();
		
		HashMap<Integer,ArrayList<ObjectChanged>> oCByIdo=new HashMap<Integer, ArrayList<ObjectChanged>>();
		for (ObjectChanged oC : aOC) {
			int oldIdo = oC.getOldIdo();
			ArrayList<ObjectChanged> list=oCByIdo.get(oldIdo);
			if(list==null){
				list=new ArrayList<ObjectChanged>();
				oCByIdo.put(oldIdo, list);
			}
			list.add(oC);
		}
		
		for (Integer oldIdo : oCByIdo.keySet()) {
			ArrayList<Element> aList = jdomParser.findElementsByAtOrText(iDataElem, "IDO", String.valueOf(oldIdo), true);
			for(ObjectChanged oC:oCByIdo.get(oldIdo)){
				int newIdo = oC.getNewIdo();
				Integer prop = oC.getProp();
				//reemplazar
				
				for (Element element : aList) {
					String ido = element.getAttributeValue("IDO");
					if (ido.equals(String.valueOf(oldIdo)) || ido.equals(String.valueOf(-newIdo))/*Esto pasara cuando ya se ha cambiado el valor por otro ObjectChanged*/) {
						//element.setAttribute("IDO",String.valueOf(-newIdo));
						
						if (prop!=null) {
							int propElem = Integer.parseInt(element.getAttributeValue("PROP"));
							if (propElem==prop) {
								String value = element.getText();
								Value oldValue = oC.getOldValue();
								if (value.equals(oldValue.getValue_s())) {
									Value newValue = oC.getNewValue();
									element.setText(newValue.getValue_s());
								}
							}
							if(prop==Constants.IdPROP_RDN){
								Attribute rdnAttribute=element.getAttribute("RDN");
								if(rdnAttribute!=null){
									String value = rdnAttribute.getValue();
									Value oldValue = oC.getOldValue();
									if (value.equals(oldValue.getValue_s())) {
										Value newValue = oC.getNewValue();
										rdnAttribute.setValue(newValue.getValue_s());
									}
								}
							}
						}
					} else {
						//element.setText(String.valueOf(-newIdo));
	
						if (prop!=null && prop==Constants.IdPROP_RDN) {
							Integer valueCls=Integer.valueOf(element.getAttribute("VALUECLS").getValue());
							if(!Constants.isDataType(valueCls)){
								Attribute rdnAttribute=element.getAttribute("RDNVALUE");
								if(rdnAttribute!=null){
									String value = rdnAttribute.getValue();
									Value oldValue = oC.getOldValue();
									if (value.equals(oldValue.getValue_s())) {
										Value newValue = oC.getNewValue();
										rdnAttribute.setValue(newValue.getValue_s());
									}
								}
							}
						}
					}
				}
			}
		}
    }
        
	public String getRdn(int ido, int idto, int business) throws RemoteException, SystemException {
		setDatabase(business);
		return m_IS.serverGetRdn(ido, idto);
	}
	
	public HashMap<Integer,String> getRdn(HashMap<Integer,HashSet<Integer>> idos, int business) throws RemoteException, SystemException{
		setDatabase(business);
		return m_IS.serverGetRdn(idos);
	}

	public EJBHome getEJBHome() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Handle getHandle() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public Object getPrimaryKey() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isIdentical(EJBObject arg0) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public void remove() throws RemoteException, RemoveException {
		// TODO Auto-generated method stub
	}

	public String getClassDescription(int idto, int business) throws RemoteException, SystemException {
		setDatabase(business);
		return m_IS.serverGetClassDescription(idto);
	}

	public String getPropertyDescription(int idProp, int business) throws RemoteException, SystemException {
		setDatabase(business);
		return m_IS.serverGetPropertyDescription(idProp);
	}
	
	public HashMap<Integer,String> getPropertiesDescriptionOfClass(int idto, int business) throws SystemException{
		setDatabase(business);
		return m_IS.serverGetPropertiesDescriptionOfClass(idto);
	}
	
	public String getIndividualDescription(int ido, int business) throws RemoteException, SystemException {
		setDatabase(business);
		return m_IS.serverGetIndividualDescription(ido);
	}
	
	public HashMap<Integer,String> getIndividualsDescriptionOfClass(int idto, int business) throws SystemException{
		setDatabase(business);
		return m_IS.serverGetIndividualsDescriptionOfClass(idto);
	}

	/**
	 * metodo que solicita el JRXML a InstanceService para servirselo a httpGaateway
	 */
	public ArrayList<String> getJRXML(String user, Integer reportIdto, int business) throws RemoteException, MessageException {
		setDatabase(business);
		ArrayList<String> reports;
		reports = m_IS.getReportJRXML(reportIdto);
		return reports;
	}

	@Override
	public void reportsClasificator(int business) throws RemoteException, SQLException, NamingException, IOException, JDOMException {
		setDatabase(business);
		m_IS.serverReportsClasificator();
	}

	
	private void setDatabase(int business) {
		System.out.println("set database " + business);
		try{
			if(factConnDB==null){
				System.out.println("Create factConnDB con business " + business+" en instanceEJB "+this.hashCode());
				factConnDB = new FactoryConnectionDB(business, false, databaseIP, gestorDB);
				/*try {
					Integer port = (Integer) myEnv.lookup("PORT");
					System.out.println("port " + port);
					factConnDB.setPort(port);
				} catch (NamingException e) {
					;
				}*/
				dataBaseMap = new DataBaseMap(factConnDB, false);
				m_IS = new InstanceService(factConnDB, initCtx, debugMode);
				m_IS.setDataBaseMap(dataBaseMap);
	
				GenerateSQL generateSQL = new GenerateSQL(gestorDB);
				String gen = generateSQL.getIsolationReadUncommited();
				if (gen!=null)
					AuxiliarQuery.dbExecUpdate(factConnDB, gen, false);
				
				//inicializar motor
				//ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(fcdb,business), business, m_IS, Constants.RULER, Constants.USER_SYSTEM, Constants.RULER_SERVER);
				//ik.setAsigned(m_IS);
				ik = new ServerEngine(factConnDB);
				m_IS.setIk(ik);
//				if (seHaceReplica) {
//					IReplication ir = new ReplicationEngine(factConnDB);
//					m_IS.setIr(ir);
//				}
			}else if(factConnDB.getBusiness()!=business){
				System.out.println("Cambiado factConnDB con business " + factConnDB.getBusiness() + " a business "+business+" en instanceEJB "+this.hashCode());
				factConnDB.removeConnections();
				factConnDB.setBusiness(business);
				
				m_IS.setBusiness(business);
			}
		} catch (IncoherenceInMotorException e) {
			AuxiliarEJB.error("InstEJB, ejbCreate, error IncoherenceInMotor:", e);
		} catch (NamingException e) {
			AuxiliarEJB.error("InstEJB, ejbCreate, error Naming:", e);
		} catch (NotFoundException e) {
			AuxiliarEJB.error("InstEJB, ejbCreate, error NotFound:", e);
		} catch (SQLException e) {
			AuxiliarEJB.error("InstEJB, ejbCreate, error SQL:", e);
		}
	}

	@Override
	public boolean sendEmail(int ido, int idto, String reportFileName, String email, String subject, String body, int idoMiEmpresa, int idoDestinatario, int business) {
		setDatabase(business);
		boolean success=false;
		try{
			success=this.m_IS.sendEmail(ido, idto, reportFileName, email, subject, body, idoMiEmpresa, idoDestinatario, true);
		} catch (NamingException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error Naming:", e);
		} catch (SQLException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error SQL:", e);
		} catch (DataErrorException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error Data:", e);
		}
		
		return success;
	}
	
	@Override
	public boolean sendServerLogEmail(String email, String subject, String body, int business) {
		setDatabase(business);
		boolean success=false;
		try{
			success=this.m_IS.sendEmailWithServerLog(email, subject, body);
		} catch (NamingException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error Naming:", e);
		} catch (SQLException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error SQL:", e);
		} catch (DataErrorException e) {
			AuxiliarEJB.error("InstEJB, sendEmail, error Data:", e);
		}
		
		return success;
	}

	public DataBaseMap getDataBaseMap(int business) {
		setDatabase(business);
		return dataBaseMap;
	}

} // InstanceEJB
