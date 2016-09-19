package dynagent.server.ejb;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.jdomParser;
import dynagent.common.xml.QueryXML;
import dynagent.server.dbmap.IQueryInfo;
import dynagent.server.dbmap.NoSuchColumnException;
import dynagent.server.exceptions.ConnectionException;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.AuxiliarQuery;

public class Asigned {
	
	private static final int OWNING_NULL=0;
	private static final int OWNING_PREASIGNED=1;
	private static final int OWNING_ASIGNED=2;
	
	public static void reAsign(InstanceService m_IS, int idoUTask, String user, Integer rol, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik) 
			throws NamingException, SQLException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion reAsign");
		//bloquea
		m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);
		//añadir en la tabla s_current_task
		boolean existe = compruebaAsign(factConnDB, idoUTask);
		if (existe)
			setAsignToNull(idoUTask, factConnDB);
		subAsign(idoUTask, factConnDB, user, rol);
		//desbloquea
		m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
		//System.out.println("Fin de la funcion reAsign");
	}
	
	public static void asign(InstanceService m_IS, int idoUTask, String user, Integer rol, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik) 
			throws NamingException, SQLException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion asign");
		//bloquea
		m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);
		boolean existe = compruebaAsign(factConnDB, idoUTask);
		//añadir en la tabla s_current_task
		if (!existe)
			subAsign(idoUTask, factConnDB, user, rol);
		//desbloquea
		m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
		if (existe)
			throw new ApplicationException(ApplicationException.ERROR_ASIGN, "CONFLICTO DE ASIGNAción");
		//System.out.println("Fin de la funcion asign");
	}
	
	public static void close(InstanceService m_IS, int idoUTask, int idtoUTask, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik) 
		throws NamingException, SQLException, SystemException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion close");
		/*DefaultSession ds = new DefaultSession((Session)ik, idoUTask, false, true, false, true,false);
		try {*/
			String gestorDB = factConnDB.getGestorDB();
			GenerateSQL gSQL = new GenerateSQL(gestorDB);
			
			ObjectProperty p = (ObjectProperty)ik.getProperty(null, idtoUTask, Constants.IdPROP_USERROL, null, Constants.USER_SYSTEM, null, /*ds*/null);
			//buscar roles para esa userTask y de ahi sacar los usuarios por la tabla rolesUsuarios
			System.out.println("Property " + p);
			if (p!=null) {
				ConnectionDB con = factConnDB.createConnection(true); 
				Statement st = null;
				ResultSet rs = null;
				for(int i =0;i<p.getEnumList().size();i++){
					Integer rol = p.getEnumList().get(i).getValue();
					String sql = "SELECT " + Constants.PROP_RDN + " FROM " + gSQL.getCharacterBegin() + "user" + gSQL.getCharacterEnd() + 
						" WHERE " + gSQL.getCharacterBegin() + "userRol" + gSQL.getCharacterEnd() + "=" + rol;
					try {
						st = con.getBusinessConn().createStatement();
						rs = st.executeQuery(sql);
						while (rs.next()) {
							String user = rs.getString(1);
				    		subClose(m_IS, idoUTask, factConnDB, user, rol);
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
			}
		/*} finally {
			ds.rollBack();
		}*/
		//System.out.println("Fin de la funcion close");
	}

	public static void preAsign(InstanceService m_IS, int idoUTask, int idtoUTask, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik) 
			throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion preAsign");
		//añadir en la tabla pre_asig
		/*DefaultSession ds = new DefaultSession((Session)ik, idoUTask, false, true, false, true,false);
		try {*/
			String gestorDB = factConnDB.getGestorDB();
			GenerateSQL gSQL = new GenerateSQL(gestorDB);
			
			ObjectProperty p = (ObjectProperty)ik.getProperty(null, idtoUTask, Constants.IdPROP_USERROL, null, Constants.USER_SYSTEM, null, /*ds*/null);
			//buscar roles para esa userTask y de ahi sacar los usuarios por la tabla rolesUsuarios
			System.out.println("Property " + p);
			if (p!=null) {
				ConnectionDB con = factConnDB.createConnection(true); 
				Statement st = null;
				ResultSet rs = null;
				for(int i =0;i<p.getEnumList().size();i++){
					Integer rol = p.getEnumList().get(i).getValue();
					String sql = "SELECT " + Constants.PROP_RDN + " FROM " + gSQL.getCharacterBegin() + "user" + gSQL.getCharacterEnd() + 
						" WHERE " + gSQL.getCharacterBegin() + "userRol" + gSQL.getCharacterEnd() + "=" + rol;
					try {
						st = con.getBusinessConn().createStatement();
						rs = st.executeQuery(sql);
						while (rs.next()) {
							String user = rs.getString(1);
				            subPreAsign(m_IS, idoUTask, factConnDB, user, rol);
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
			}
		/*} finally {
			ds.rollBack();
		}*/
		//System.out.println("Fin de la funcion preAsign");
	}

	public static void release(InstanceService m_IS, int idoUTask, int idtoUTask, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik) 
			throws NamingException, SQLException, SystemException, NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion release");
		//añadir en la tabla s_preasig
		/*DefaultSession ds = new DefaultSession((Session)ik, idoUTask, false, true, false, true,false);
		try {*/
			String gestorDB = factConnDB.getGestorDB();
			GenerateSQL gSQL = new GenerateSQL(gestorDB);
			
			ObjectProperty p = (ObjectProperty)ik.getProperty(null, idtoUTask, Constants.IdPROP_USERROL, null, Constants.USER_SYSTEM, null, /*ds*/null);
			//buscar roles para esa userTask y de ahi sacar los usuarios por la tabla rolesUsuarios
			System.out.println("Property " + p);
			if (p!=null) {
				ConnectionDB con = factConnDB.createConnection(true); 
				Statement st = null;
				ResultSet rs = null;
				for(int i =0;i<p.getEnumList().size();i++){
					Integer rol = p.getEnumList().get(i).getValue();
					String sql = "SELECT " + Constants.PROP_RDN + " FROM " + gSQL.getCharacterBegin() + "user" + gSQL.getCharacterEnd() + 
						" WHERE " + gSQL.getCharacterBegin() + "userRol" + gSQL.getCharacterEnd() + "=" + rol;
					try {
						st = con.getBusinessConn().createStatement();
						rs = st.executeQuery(sql);
						while (rs.next()) {
							String user = rs.getString(1);
				            subRelease(m_IS, idoUTask, factConnDB, user, rol);
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
			}
		/*} finally {
			ds.rollBack();
		}*/
		//System.out.println("Fin de la funcion release");
	}

	private static void subAsign(int idoUTask, FactoryConnectionDB factConnDB, String user, Integer rol) 
			throws NamingException, SQLException {
        //hacer un select para esa idtoUTask e ir insertando en log con asigned null, excepto el actual
		String sql = "SELECT USUARIO,ROL FROM S_PREASIG WHERE ID_TASK=" + idoUTask;
		boolean actualizado = false;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String userC = rs.getString(1);
				Integer rolC = rs.getInt(2);
				if (rs.wasNull()) rolC = null;
				String sql2 = "";
				if (!(StringUtils.equals(user, userC) && (rolC==null && rol==null || rolC!=null && rol!=null && rol.equals(rolC)))) {
					sql2 = "UPDATE OWNING_MSG SET LEVEL=" + Asigned.OWNING_NULL + ",TIMESTAMP='" + Auxiliar.getDate() + "' " +
						"WHERE ID_TASK=" + idoUTask + " AND USUARIO='" + userC + "' AND ROL=" + rolC + " AND LEVEL=" + Asigned.OWNING_PREASIGNED;
				} else {
					sql2 = "UPDATE OWNING_MSG SET LEVEL=" + Asigned.OWNING_ASIGNED + ",TIMESTAMP='" + Auxiliar.getDate() + "' " +
						"WHERE ID_TASK=" + idoUTask + " AND USUARIO='" + userC + "' AND ROL=" + rolC + " AND LEVEL=" + Asigned.OWNING_PREASIGNED;
					actualizado = true;
				}
				AuxiliarQuery.dbExecUpdate(factConnDB, sql2, false);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
        
		//quitar de preasign
        String sql2 = "DELETE FROM S_PREASIG WHERE ID_TASK=" + idoUTask;
        
		String sql3 = "INSERT INTO S_ASIG(ID_TASK,USUARIO,ROL) VALUES(" + 
		idoUTask + ",'" + user + "'," + rol + ")";
		String sql4 = "INSERT INTO OWNING_MSG(LEVEL,TIMESTAMP,ID_TASK,USUARIO,ROL) VALUES(" +
			Asigned.OWNING_ASIGNED + ",'" + Auxiliar.getDate() + "'," + idoUTask + ",'" + user + "'," + rol + ")";
		ConnectionDB con2 = factConnDB.createConnection(false); 
		Statement st2 = null;
		try {
			st2 = con2.getBusinessConn().createStatement();
			st2.executeUpdate(sql2);
			st2.executeUpdate(sql3);
			if (!actualizado)
				st2.executeUpdate(sql4);
		} finally {
			if (st2 != null)
				st2.close();
			if (con2!=null)
				factConnDB.close(con2);
		}
	}
	
	private static void subPreAsign(InstanceService m_IS, int idoUTask, FactoryConnectionDB factConnDB, String user, Integer rol) 
			throws NamingException, SQLException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//bloquea
		m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);

		boolean existe = compruebaTodos(factConnDB, idoUTask, user, rol);
		if (!existe) {
			//añadir en la tabla s_preasig
			String sql2 = "INSERT INTO S_PREASIG(ID_TASK,USUARIO,ROL) VALUES(" +
			idoUTask + ",'" + user + "'," + rol + ")";
			//aqui parece que no hay que hacer un update
			String sql3 = "INSERT INTO OWNING_MSG(LEVEL,TIMESTAMP,ID_TASK,USUARIO,ROL) VALUES(" +
					Asigned.OWNING_PREASIGNED + ",'" + Auxiliar.getDate() + "'," + idoUTask + ",'" + user + "'," + rol + ")";

			AuxiliarQuery.dbExecUpdate(factConnDB, sql2 + "; " + sql3, false);
		}
		//desbloquea
		m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
		if (existe)
			throw new ApplicationException(ApplicationException.ERROR_ASIGN, "CONFLICTO DE PREASIGNAción");
	}

	private static void subClose(InstanceService m_IS, int idoUTask, FactoryConnectionDB factConnDB, String user, Integer rol) 
			throws NamingException, SQLException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//bloquea
		m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);
	    String sql = "DELETE FROM S_ASIG WHERE ID_TASK=" + idoUTask + " AND USUARIO=" + user;
		if (rol!=null)
			sql += " AND ROL=" + rol;
		else
			sql += " AND ROL IS NULL";
	    String sql2 = "DELETE FROM S_PREASIG WHERE ID_TASK=" + idoUTask + " AND USUARIO=" + user;
		if (rol!=null)
			sql2 += " AND ROL=" + rol;
		else
			sql2 += " AND ROL IS NULL";
	    String sql3 = "DELETE FROM OWNING_MSG WHERE ID_TASK=" + idoUTask + " AND USUARIO=" + user;
		if (rol!=null)
			sql3 += " AND ROL=" + rol;
		else
			sql3 += " AND ROL IS NULL";
	    
		AuxiliarQuery.dbExecUpdate(factConnDB, sql + "; " + sql2 + "; " + sql3, false);
		//desbloquea
		m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
	}

	private static void setAsignToNull(int idoUTask, FactoryConnectionDB factConnDB) 
			throws NamingException, SQLException {
		String sql = "DELETE FROM S_ASIG WHERE ID_TASK=" + idoUTask;
		String sql2 = "UPDATE OWNING_MSG SET LEVEL=" + Asigned.OWNING_NULL + ",TIMESTAMP='" + Auxiliar.getDate() + "' " +
			"WHERE ID_TASK=" + idoUTask/* + " AND LEVEL=" + Asigned.OWNING_ASIGNED*/;

		AuxiliarQuery.dbExecUpdate(factConnDB, sql + "; " + sql2, false);
	}
	
	private static void subRelease(InstanceService m_IS, int idoUTask, FactoryConnectionDB factConnDB, String user, Integer rol) 
			throws NamingException, SQLException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//bloquea
		m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);
		
		boolean existe = compruebaAsign(factConnDB, idoUTask);
		if (existe) {
			setAsignToNull(idoUTask, factConnDB);
			
			//volver a preasignar a todos, a el mismo(porque lo he puesto cmo nulo) y a todos los que están en la tabla de log para esa tarea
			String sql = "SELECT USUARIO,ROL FROM OWNING_MSG WHERE ID_TASK=" + idoUTask + " AND OWNING_MSG.LEVEL=" + Asigned.OWNING_NULL;
			ConnectionDB con = factConnDB.createConnection(true); 
			Statement st = null;
			ResultSet rs = null;
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				while (rs.next()) {
					String userC = rs.getString(1);
					Integer rolC = rs.getInt(2);
					if (rs.wasNull()) rolC = null;
					//insertar en preasig
					String sql4 = "INSERT INTO S_PREASIG(ID_TASK,USUARIO,ROL) VALUES(" +
					idoUTask + ",'" + userC + "'," + rolC + ")";
					AuxiliarQuery.dbExecUpdate(factConnDB, sql4, false);
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
			//todos preasignados
			String sql2 = "UPDATE OWNING_MSG SET LEVEL=" + Asigned.OWNING_PREASIGNED + ",TIMESTAMP='" + Auxiliar.getDate() + "' " +
				"WHERE ID_TASK=" + idoUTask/* + " AND LEVEL=" + Asigned.OWNING_NULL*/;
			AuxiliarQuery.dbExecUpdate(factConnDB, sql2, false);
		}
		//desbloquea
		m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
	}

	private static boolean compruebaAsign(FactoryConnectionDB factConnDB, int idoUTask) throws SQLException, NamingException {
        boolean existe = false;
		String sqlComprob = "SELECT USUARIO,ROL FROM OWNING_MSG WHERE ID_TASK=" + idoUTask + " AND LEVEL=" + Asigned.OWNING_ASIGNED;
		ConnectionDB con = factConnDB.createConnection(true); 
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlComprob);
			if (rs.next())
				existe = true;
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		System.out.println(sqlComprob);
		return existe;
	}
	
	private static boolean compruebaTodos(FactoryConnectionDB factConnDB, int idoUTask, String user, Integer rol) throws SQLException, NamingException {
        boolean existe = false;
		String sqlComprob = "SELECT USUARIO,ROL FROM OWNING_MSG WHERE ID_TASK=" + idoUTask + " AND USUARIO='" + user/* + "' AND LEVEL<>" + Asigned.OWNING_NULL*/;
		//Asigned.OWNING_NULL solo se usa temporalmente en release
		if (rol!=null)
			sqlComprob += " AND ROL=" + rol;
		else
			sqlComprob += " AND ROL IS NULL";
		ConnectionDB con = factConnDB.createConnection(true); 
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlComprob);
			if (rs.next())
				existe = true;
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return existe;
	}

	public static selectData getTasks(InstanceService m_IS, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik, 
			Integer userRol, String user) throws NamingException, SQLException, SystemException, 
			NoSuchElementException, JDOMException, ParseException, DataErrorException, NotFoundException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, ConnectionException, NoSuchColumnException {

		selectData sd = subGetTasks(m_IS, factConnDB, ik, userRol, user);
		return sd;
	}
	
	public static selectData subGetTasks(InstanceService m_IS, FactoryConnectionDB factConnDB, IKnowledgeBaseInfo ik, 
			Integer userRol, String user) throws NamingException, SQLException, SystemException, 
			NoSuchElementException, JDOMException, DataErrorException, NotFoundException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, OperationNotPermitedException, ConnectionException, ParseException, NoSuchColumnException {
    	selectData sd = new selectData();
		String sql = "SELECT ID_TASK, LEVEL FROM OWNING_MSG WHERE USUARIO='" + user + "' " +
				"AND (LEVEL=" + Asigned.OWNING_PREASIGNED + " OR LEVEL=" + Asigned.OWNING_ASIGNED + ")";
		if (userRol!=null)
			sql += " AND ROL=" + userRol;
		System.out.println("sql " + sql);
		instance insPreAsign = null;
		instance insAsign = null;
		ArrayList<SelectQuery> asqPreAsign = new ArrayList<SelectQuery>();
		ArrayList<SelectQuery> asqAsign = new ArrayList<SelectQuery>();
		ArrayList<Integer> idosPreAsign = new ArrayList<Integer>();
		ArrayList<Integer> idosAsign = new ArrayList<Integer>();
		
		ConnectionDB con = factConnDB.createConnection(true); 
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int level = rs.getInt(2);
				int idoUTask = rs.getInt(1);
				System.out.println("idoUTask " + idoUTask);
				m_IS.lockObject(idoUTask, Constants.IDTO_UTASK, user);
				int idoTClass = -1;
				if (level==Asigned.OWNING_PREASIGNED) {
					idosPreAsign.add(idoUTask);
					if (insPreAsign==null) {
						insPreAsign = new instance(0,0);

						Property p3 = createOProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_TARGETCLASS, idoTClass, Constants.IDTO_THING);
						insPreAsign.addProperty(idoUTask, p3);
						addSQ(asqPreAsign, idoUTask, idoTClass);
					}
				} else if (level==Asigned.OWNING_ASIGNED) {
					idosAsign.add(idoUTask);
					if (insAsign==null) {
						insAsign = new instance(0, 0);
						
						Property p1 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_OWNER, user);
						insAsign.addProperty(idoUTask, p1);
						if (userRol!=null) {
							Property p2 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_USERROL, userRol.toString());
							insAsign.addProperty(idoUTask, p2);
						}
						Property p3 = createOProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_TARGETCLASS, idoTClass, Constants.IDTO_THING);
						insAsign.addProperty(idoUTask, p3);
					
				    	SelectQuery sqUser = new SelectQuery(String.valueOf(idoUTask), Constants.IdPROP_OWNER, null, null);
				    	asqAsign.add(sqUser);
						addSQ(asqAsign, idoUTask, idoTClass);
					}
				}
				m_IS.unlockObject(idoUTask, Constants.IDTO_UTASK, user);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		if (insAsign!=null) {
			QueryXML qXML = new QueryXML(ik);
			qXML.setSelect(asqAsign);
			Element elem = qXML.toQueryXML(insAsign, idosAsign, Constants.IDTO_UTASK, null); 
			System.out.println("Element uTask asignadas \n" + jdomParser.returnXML(elem));
			queryData qd = m_IS.query(elem, null, queryData.MODE_ROW);
			sd.addAll(qd.toSelectData(elem));
		}
		if (insPreAsign!=null) {
			QueryXML qXML = new QueryXML(ik);
			qXML.setSelect(asqPreAsign);
			Element elem = qXML.toQueryXML(insPreAsign, idosPreAsign, Constants.IDTO_UTASK, null); 
			System.out.println("Element uTask preasignadas \n" + jdomParser.returnXML(elem));
			queryData qd = m_IS.query(elem, null, queryData.MODE_ROW);
			sd.addAll(qd.toSelectData(elem));
		}
		return sd;
    }

    private static void addSQ(ArrayList<SelectQuery> asq, int idoUTask, int idoTClass) {
		SelectQuery sq1 = new SelectQuery(String.valueOf(idoUTask), Constants.IdPROP_RDN, null, null);
		asq.add(sq1);
		SelectQuery sq2 = new SelectQuery(String.valueOf(idoUTask), Constants.IdPROP_ASIGNDATE, null, null);
		asq.add(sq2);
		SelectQuery sq3 = new SelectQuery(String.valueOf(idoUTask), Constants.IdPROP_EJECUTEDATE, null, null);
		asq.add(sq3);
		SelectQuery sq4 = new SelectQuery(String.valueOf(idoTClass), Constants.IdPROP_RDN, null, null);
		asq.add(sq4);
    }
    
    /*private static selectData getAsignedTasks(InstanceService m_IS, IKnowledgeBaseInfo ik, Integer userRol, 
    		String user) throws NamingException, SQLException, NotFoundException, 
    		NoSuchElementException, JDOMException, DataErrorException, IncoherenceInMotorException, 
    		IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, 
    		RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException {
		int idoUTask = -1;
//		m_IS.lockObject(idoUTask, user);
		instance ins = new instance(Constants.IDTO_UTASK, idoUTask);
	
		int idoTClass = -2;

		Property p1 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_OWNER, user);
		ins.addProperty(idoUTask, p1);
		if (userRol!=null) {
			Property p2 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_USERROL, userRol.toString());
			ins.addProperty(idoUTask, p2);
		}
		Property p3 = createOProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_TARGETCLASS, idoTClass, Constants.IDTO_THING);
		ins.addProperty(idoUTask, p3);
	
    	ArrayList<SelectQuery> asq = new ArrayList<SelectQuery>();
    	SelectQuery sqUser = new SelectQuery(String.valueOf(idoUTask), Constants.IdPROP_OWNER);
		asq.add(sqUser);
		addSQ(asq, idoUTask, idoTClass);
		
//		m_IS.unlockObjects(listIdo, user);
		QueryXML qXML = new QueryXML(ik, null);
		qXML.setSelect(asq);
		Element elem = qXML.toQueryXML(ins, null); 
		System.out.println("Element uTask asignadas \n" + jdomParser.returnXML(elem));
		queryData qd = m_IS.query(elem);
		selectData sd = qd.toSelectData(elem);
		return sd;
    }

    private static selectData getPreAsignedTasks(InstanceService m_IS, FactoryConnectionDB factConnDB, 
    		IKnowledgeBaseInfo ik, Integer userRol, String user) throws NamingException, SQLException, 
    		SystemException, NotFoundException, NoSuchElementException, JDOMException, DataErrorException, IncoherenceInMotorException, 
    		IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, 
    		CommunicationException, InstanceLockedException, OperationNotPermitedException {
    	selectData sd = new selectData();
		String sql = "SELECT ID_TASK FROM OWNING_MSG WHERE USUARIO='" + user + "' AND LEVEL=" + Asigned.OWNING_PREASIGNED;
		if (userRol!=null)
			sql += " AND ROL=" + userRol;
		System.out.println("sql " + sql);
		instance ins = null;
		ArrayList<SelectQuery> asq = new ArrayList<SelectQuery>();
		ArrayList<Integer> idos = new ArrayList<Integer>();
		
		ConnectionDB con = factConnDB.createConnection(true); 
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idoUTask = rs.getInt(1);
				System.out.println("idoUTask " + idoUTask);
				idos.add(idoUTask);
				m_IS.lockObject(idoUTask, user);
						
//						<QUERY OID="Prueba6" USER="SYSTEM">
//							<STRUCTURE>
//								<CLASS id_o=idoUTask id_to=Constants.IDTO_UTASK>
//									<ATTRIBUTE prop=rdn id_tm=str/>
//									<ATTRIBUTE prop=fasig id_tm=str/>
//									<ATTRIBUTE prop=ftope id_tm=str/>
//									<CLASS prop=tclass id_to=Constants.IDTO_UTASK>
//										<ATTRIBUTE prop=rdn idtm=str/>
//									</CLASS>
//								</CLASS>
//								<PRESENTATION>
//									<VIEW ID="1"/>
//								</PRESENTATION>
//							</STRUCTURE>
//						</QUERY>
				if (ins==null) {
					ins = new instance(0,0);
				
					int idoTClass = -1;
					//Property p1 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_ASIGNDATE, null);
					//ins.addProperty(idoUTask, p1);
					//Property p2 = createDProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_EJECUTEDATE, null);
					//ins.addProperty(idoUTask, p2);
					Property p3 = createOProperty(idoUTask, Constants.IDTO_UTASK, Constants.IdPROP_TARGETCLASS, idoTClass, Constants.IDTO_THING);
					ins.addProperty(idoUTask, p3);
					//no requerida porque por defecto los que tienen ido negativo no son requeridos
					//Property p4 = createDProperty(idoTClass, Constants.IDTO_UTASK, Constants.IdPROP_RDN, null);
					//ins.addProperty(idoUTask, p4);
					addSQ(asq, idoUTask, idoTClass);
				}
				m_IS.unlockObject(idoUTask, user);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		if (ins!=null) {
			QueryXML qXML = new QueryXML(ik, null);
			qXML.setSelect(asq);
			Element elem = qXML.toQueryXML(ins, idos, Constants.IDTO_UTASK, null); 
			System.out.println("Element uTask preasignadas \n" + jdomParser.returnXML(elem));
			queryData qd = m_IS.query(elem);
			sd = qd.toSelectData(elem);
		}

		return sd;
    }*/

	private static ObjectProperty createOProperty(int ido, int idto, int prop, int value, int valueCls) {
		ObjectProperty op = new ObjectProperty();
		op.setIdo(ido);
		op.setIdto(idto);
		op.setIdProp(prop);

		LinkedList<Integer> li = new LinkedList<Integer>();
		li.add(value);
		op.setRangoList(li);

		return op;
	}

	private static DataProperty createDProperty(int ido, int idto, int prop, String value) {
		DataProperty dp = new DataProperty();
		dp.setIdo(ido);
		dp.setIdto(idto);
		dp.setIdProp(prop);
		if (value!=null) {
			LinkedList<Value> ldv = new LinkedList<Value>();
			StringValue sv = new StringValue();
			ldv.add(sv);
			sv.setValue(value);
			dp.setValues(ldv);
			dp.setDataType(Constants.IDTO_STRING);
		}
		return dp;
	}

}
