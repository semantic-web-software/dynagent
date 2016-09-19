//package dynagent.ejb;

/*
 *
 * Copyright 2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

//import java.lang.*;
/*import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Random;

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

import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jdom.IllegalDataException;
import org.jdom.JDOMException;

import dynagent.communication.message;
import dynagent.communication.messageFactory;
import dynagent.communication.properties;
import dynagent.communication.queryData;
import dynagent.knowledge.instance.contextData;
import dynagent.knowledge.instance.selectData;
import dynagent.process.IAsigned;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.data.dao.Access;
import dynagent.ruleengine.src.data.dao.O_Datos_Attrib;
import dynagent.ruleengine.src.data.dao.Properties;
import dynagent.ruleengine.src.data.dao.T_Herencias;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.ruleengine.src.ruler.FactInstance;
import dynagent.ruleengine.src.ruler.IPropertyDef;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.Session;
import dynagent.ruleengine.src.xml.GetData;
import dynagent.services.InstanceService;

class threadContextData extends Object {
	public contextData oldData = null;

	public contextData currData = null;
}

public class InstanceEJB implements SessionBean, Instance, IAsigned {*/

	//private Integer m_id=null;
	//private int m_id_to;
	//    private String m_rdn;
//	private SessionContext context;

	//    private java.sql.Connection openjmsCon =null;
	
//	private poolDB m_pool;
	
//	private Diccionario m_dic;

	/*static final int SCOPE_BUSINESS = 1;

	static final int SCOPE_RULE = 2;

	static final int SCOPE_CURRTASK = 3;

	static final int SCOPE_THIS_CTX_NOT_CURRTASK = 4;*/

	// INI JMS
	/*QueueSession sesionTran = null;

	QueueSender senderToRuler = null;

	TextMessage message = null;

	QueueConnectionFactory queueCntFtry = null;

	javax.jms.Queue buzonRuler = null;

	QueueConnection queueConnection = null;*/

	// END JMS
	
	//int jmsTranIDCount = 0;

	//String jmsTranID = null;

	/*public String Meta_GetTO(int id_to, String user) {
		try {
			int empresa = m_IS.getUserBusiness(user);
			Element MetaT = m_IS.Meta_GetTO(id_to, empresa);
			MetaT.setAttribute("USER", user);
			return jdomParser.returnXML(MetaT);
		} catch (SQLException e) {
			error("IEJB GET META TO:" + e.getMessage() + " EXPERT_ERROR_SQL:"
					+ e.getErrorCode());
		} catch (JDOMException e) {
			error("IEJB GET META TO, error JDOM:" + e.getMessage());
		} catch (Exception e) {
			error("IEJB GET META TO, error Exception:" + e.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return null;
	}*/

	/*public String getFilter(int filter) {
		try {
			String res = m_IS.getStrFilter(filter);
			return res;
		} catch (SQLException e) {
			error("GET FILTER ERROR SQL:" + e.getMessage());
		} catch (NamingException e) {
			error("GET FILTER ERROR Naming:" + e.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return null;
	}*/

	/*public void testLoopBackPro(int dato, boolean check) {
		Statement st = null;
		ResultSet rs = null;
		try {
			String s = "INSERT INTO locksID values(-" + dato
					+ ",'testloopbcak')";
			if (check)
				st = m_pool.getProcessConn().createStatement();
			else
				st = m_pool.getBusinessConn(1).createStatement();
			st.executeUpdate(s);
			s = "SELECT Login FROM Usuarios WITH(NOLOCK)";
			st = m_pool.getBusinessConn(1).createStatement();
			rs = st.executeQuery(s);
			while (rs.next()) {
				String name = rs.getString(1);
			}
			st.close();
			st = m_pool.getBusinessConn(1).createStatement();
			rs = st.executeQuery(s);
			while (rs.next()) {
				String name = rs.getString(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

	}*/

/*	public String getThreadData(int processType, int currPro, int currTask) {
		try {
			selectData sd = m_IS.getThreadData(processType, currPro, currTask);
			message msg = new message(
					dynagent.communication.message.MSG_SELECTION);
			msg.setContent(sd);
			return msg.toString();
		} catch (SystemException e) {
			error(e.getMessage());
			return null;
		} catch (NamingException e) {
			error("IEJB GET ThreadData, ERROR Naming:" + e.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return null;
	}
*/
	/*public String findTask(int ctxId, int idto, int ido) {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = m_pool.getProcessConn().createStatement();
			rs = st.executeQuery("SELECT ID_CURR_TASK "
					+ "FROM S_CURRENT_CTX WITH(NOLOCK) " + "WHERE ID_CTX_TYPE="
					+ ctxId + " AND ID_OBJ=" + ido);
			Element res = new Element("FINDTASK");
			while (rs.next()) {
				int currTask = rs.getInt(1);
				Element task = new Element("ITEM");
				task.setAttribute("ID", String.valueOf(currTask));
				res.addContent(task);
			}
			return jdomParser.returnXML(res);
		} catch (JDOMException e) {
			error(e.getMessage());
			return null;
		} catch (SQLException e) {
			error("INSTANCE FIND TASK:ERROR SQL:" + e.getMessage() + ":"
					+ e.getErrorCode());
			return null;
		} catch (NamingException e) {
			error("INSTANCE FIND TASK:ERROR Naming:" + e.getMessage());
			return null;
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

	}*/

	/*public String getInstanceReport(String user, String idReport, int ido) {
		Statement st = null;
		ResultSet rs = null;
		boolean rsClosed = false, stClosed = false;
		try {
			st = m_pool.getProcessConn().createStatement();
			rs = st
					.executeQuery("SELECT F.FILTER, D.PLANTILLA "
							+ " FROM S_DYNAMICS_REPORTS D WITH(NOLOCK) INNER JOIN S_FILTER F WITH(NOLOCK) "
							+ " ON D.FILTER=F.ID_FILTER WHERE D.OID='"
							+ idReport + "'");
			Element filter = null, content;
			String plantilla = null;
			if (rs.next()) {
				filter = jdomParser.readXML(rs.getString(1)).getRootElement();

				int idto = Integer.parseInt(filter.getAttributeValue("ID_TO"));
				filter.setAttribute("USER", user);
				filter.setAttribute("ID_O", String.valueOf(ido));
				filter.setAttribute("ID_TO", String.valueOf(idto));
				plantilla = rs.getString(2);
				System.out.println("PLANTILLA:" + plantilla);
			} else {
				return mensageHTML("ERROR EL REPORT NO EXISTE");
			}
			rs.close();
			rsClosed = true;
			rs = st.executeQuery("SELECT CONTENT FROM S_PLANTILLAS WITH(NOLOCK) WHERE OID='"
							+ plantilla + "'");
			rsClosed = false;
			if (rs.next())
				content = jdomParser.readXML(rs.getString(1)).getRootElement();
			else {
				return mensageHTML("ERROR LA PLANTILLA NO EXISTE");
			}
			rs.close();
			rsClosed = true;
			st.close();
			stClosed = true;
			//int empresa = m_IS.getUserBusiness(user);
			//metaData md = m_dic.getMetaData(empresa);
			queryData data = m_IS.Query(filter, business);
			System.out.println("IEJB, Ok Query");

			Element response = null;
			if (content.getAttributeValue("diseño").equals("AUTOMATIC")) {
				instanceReportAdaptor ir = new instanceReportAdaptor(m_dic,
						filter, data.toSelectData(filter), content, empresa);
				response = ir.generateReport();
			}
			if (content.getAttributeValue("diseño").equals("DETERMINADO")) {
				templateReport tr = new templateReport(m_dic, filter, data
						.toSelectData(filter), content, empresa);
				response = tr.generateReport();
			}
			return jdomParser.returnXML(response);
		} catch (SQLException e) {
			String msg = "INSTANCE INST REPORT:ERROR SQL:" + e.getMessage()
					+ ":" + e.getErrorCode();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (JDOMException e) {
			String msg = "INSTANCE INST REPORT:ERROR JDOM:" + e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (ParseException e) {
			String msg = "INSTANCE INST REPORT:ERROR JDOM:" + e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (DataErrorException e) {
			String msg = "INSTANCE INST REPORT:ERROR JDOM:" + e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (NoSuchElementException e) {
			String msg = e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (NamingException e) {
			String msg = e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} catch (NumberFormatException e) {
			String msg = e.getMessage();
			System.out.println(msg);
			e.printStackTrace();
			return mensageHTML(msg);
		} finally {
			try {
				if (rs != null && !rsClosed && !stClosed)
					rs.close();
				if (st != null && !stClosed)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

	}*/

	/*private String mensageHTML(String msg) {
		return "<HTML><HEAD><TITLE>MENSAJE</TITLE></HEAD>" + "<BODY><P>" + msg
				+ "</BODY></HTML>";
	}*/
	
	/*private String SetObj(Integer user, Integer userRol, instance ins, int task) {
		try {
//			scope sc = buildRootScope(act, access.SET);
			String res = m_IS.SetObj(user, userRol, ins, task);
			return res;
		} catch (Exception e) {
			error("SETOBJ:" + e.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return null;
	}*/
	
	/*private scope buildRootScope(contextAction root, int operation)
			throws NamingException, SQLException, DataErrorException {
		return buildRootScope(root.getAccess().toString(), root.getUser(),
				new Integer(root.getUserRol()), operation);
	}

	private scope buildRootScope(String accessStr, String user,
			Integer userRol, int operation) throws DataErrorException,
			SQLException, NamingException {
		int empresa = m_IS.getUserBusiness(user);
		metaData md = m_dic.getMetaData(empresa);
		return new scope(md, userRol, new access(accessStr), operation);
	}*/

	/*private String NewObj(Integer user, Integer userRol, instance ins, int task) {
		try {
			Element res = m_IS.NewObjAtom(user, userRol, ins, task);// -1 indica que no tiene padre
			return jdomParser.returnXML(res);
		} catch (Exception e) {
			error(e.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return null;
	}

	private void DelObj(Integer user, Integer userRol, instance ins, int task) {
		try {
			m_IS.DelObj(user, userRol, ins, task);
		} catch (Exception ex) {
			error("ejbRemove: " + ex.getMessage());
		} finally {
			try {
				m_pool.close();
			} catch (SQLException ex) {
				error("INSTANCE ERROR CLOSING POOLDB" + ex.getMessage());
			}
		}
	}*/
	
	/*public ArrayList<IPropertyDef> GetBC(int id) {
		try {
			ArrayList<IPropertyDef> aipd = m_IS.serverGetBusinessClass(id);
			return aipd;
		} catch (Exception e) {
			InstanceEJBAux.error(" EXPERT_ERROR:" + e.getMessage(), e);
		}
		return null;
	}*/

	/*public boolean checkDN(String dn, int idTo, String user) {
		Statement st = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = factConnDB.createConnection().getBusinessConn();
			st = con.createStatement();
			rs = st.executeQuery("	SELECT * FROM 	O_Datos_Atributos ODA WITH(NOLOCK) INNER JOIN "
					+ "	T_INDEX_TO TT WITH(NOLOCK) ON ODA.ID_TO=TT.ID_TO "
					+ "	WHERE 		TA_POS="
					+ helperConstant.TAPOS_RDN
					+ " 	AND Val_Texto='"
					+ dn.replaceAll("'", "''")
					+ "'" + "	AND TT.ID_TO_H=" + idTo);
			boolean exito = !rs.next();
			return exito;
		} catch (SQLException e) {
			error("INSTANCE, error en checkDN:" + e.getMessage());
		} catch (NamingException e) {
			error("INSTANCE, error en checkDN:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				con.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return false;
	}*/

	/*private Iterator getSubItems(Element atom, String root, String item){
	 esto es para el tipico caso en que un subnodo hijo alberga
	 a su vez como hijos una lista de items
	 Iterator tempItr= atom.getChildren(root).iterator(), itr=null;
	 Element lista=null;
	 if(tempItr.hasNext()){
	 lista=(Element)tempItr.next();
	 itr= lista.getChildren(item).iterator();
	 if(itr.hasNext()) return itr;
	 }
	 return null;
	 }*/

	/*public message getCurrentTran(String user) {
		ResultSet rs = null;
		Statement st = null;
		try {
			Element response = new Element("PROCESS");
			response.setAttribute("USER", user);
			System.out.println("PASO 1");

			String sql = "SELECT * FROM V_CURRENT_TASK WITH (NOLOCK) WHERE OWNER= '"
					+ user + "' order by exe_date asc";
			st = m_pool.getProcessConn().createStatement();
			rs = st.executeQuery(sql);
			System.out.println("PASO 2");
			int proID = 0, oldPro = 0, oldCurrTask = 0;
			Element ePro = null;
			HashMap currTasks = new HashMap();
			while (rs.next()) {
				proID = rs.getInt(1);
				int proType = rs.getInt(2);
				String proName = rs.getString(3);
				if (proID != oldPro) {
					ePro = new Element("ITEM");
					ePro.setAttribute("TYPE", String.valueOf(proType));
					ePro.setAttribute("NAME", proName);
					ePro.setAttribute("ID", String.valueOf(proID));
					response.addContent(ePro);
				}
				Element eTask = new Element("CURR_TASK");
				eTask.setAttribute("TASK_TYPE", String.valueOf(rs.getInt(4)));
				int currTask = rs.getInt(5);
				currTasks.put(new Integer(currTask), eTask);
				if (proID == oldPro && currTask == oldCurrTask)
					continue;
				ePro.addContent(eTask);
				eTask.setAttribute("ID", String.valueOf(currTask));
				eTask.setAttribute("CURRENT_STATE", String
						.valueOf(rs.getInt(6)));
				eTask.setAttribute("OWNER", rs.getString(7));
				eTask.setAttribute("USER_ROL", rs.getString(8));
				eTask.setAttribute("TASK_NAME", rs.getString(9));
				int otAsig = rs.getInt(10);
				eTask.setAttribute("OT_ASIGNMENT", otAsig == 0 ? "FALSE"
						: "TRUE");
				java.sql.Date fecha = rs.getDate(11);
				if (!rs.wasNull()) {
					SimpleDateFormat f = new SimpleDateFormat(
							"dd-MM-yyyy hh:mm:ss");
					eTask.setAttribute("EXE_DATE", f.format(fecha));
				}
				int owLevel = rs.getString(12).equals("PREASIGNED") ? owningAction.OWNING_PREASIGNED
						: owningAction.OWNING_APROPIATED;
				eTask.setAttribute("OWNING_LEVEL", String.valueOf(owLevel));
				oldPro = proID;
				oldCurrTask = currTask;
			}
			System.out.println("PASO 3");
			//m_pool.commit();
			System.out.println("PASO 5");
			message msg = new message(
					dynagent.communication.message.MSG_CONFIRM);
			msg.setContent(response);
			msg.setUser(user);
			msg.setActionWay(false);
			return msg;
		} catch (SQLException e) {
			e.printStackTrace();
			error("INSTANCE: Error SQL:" + e.getErrorCode() + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			error("INSTANCE:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return null;
	}*/

	/*public String getConsultas(String user) {
		Element response = new Element("CONSULTAS");
		Document resDoc = new Document(response);
		response.setAttribute("USER", user);

		String sql = "SELECT FILTER FROM S_FILTER  WITH(NOLOCK) INNER JOIN CONSULTAS_PERMISOS CP  WITH(NOLOCK) ON  "
				+ "S_FILTER.ID_FILTER= CP.ID_FILTER INNER JOIN Usuarios  WITH(NOLOCK) ON CP.ROL= Usuarios.Rol WHERE "
				+ " Usuarios.Login= '" + user + "'";
		ResultSet rs = null;
		Statement st = null;
		try {
			st = m_pool.getProcessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				response.addContent((Element) jdomParser.readXML(
						rs.getString(1)).getRootElement().clone());
			}
		} catch (SQLException e) {
			error("INSTANCE:GET CONSULTAS:Error SQL:" + e.getErrorCode()
					+ e.getMessage());
		} catch (JDOMException e) {
			error("INSTANCE:GET CONSULTAS:Error JDOM:" + e.getMessage());
		} catch (NamingException e) {
			error("INSTANCE:GET CONSULTAS:Error Naming:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return returnXML(resDoc);
	}

	public String getReports(String user) {
		Element response = new Element("REPORTS");
		Document resDoc = new Document(response);
		response.setAttribute("USER", user);

		String sql = "SELECT REPORT, DYN_NAME, DYN_FILTER FROM V_REPORTS WITH(NOLOCK) "
				+ " WHERE Login= '" + user + "'";
		ResultSet rs = null;
		Statement st = null;
		try {
			st = m_pool.getProcessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Element report = new Element("REPORT");
				String oid = rs.getString(1), name = rs.getString(2);
				if (rs.wasNull()) {
					report.setAttribute("NAME", oid);
					report.setAttribute("TYPE", "FILE");
				} else {
					report.setAttribute("NAME", name);
					report.setAttribute("TYPE", "DYNAMIC");
					report.addContent((Element) jdomParser.readXML(
							rs.getString(3)).getRootElement().clone());
				}
				response.addContent(report);
			}
		} catch (SQLException e) {
			error("INSTANCE:GET REPORTS:Error SQL:" + e.getErrorCode()
					+ e.getMessage());
		} catch (JDOMException e) {
			error("INSTANCE:GET REPORTS:Error JDOM:" + e.getMessage());
		} catch (NamingException e) {
			error("INSTANCE:GET REPORTS:Error Naming:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return returnXML(resDoc);
	}*/

	/*public message getOutTaskTrans(String user, int currentTask) {
		ResultSet rs = null;
		Statement st = null;
		boolean stClosed = false;
		try {
			//int empresa = m_IS.getUserBusiness(user);
			System.out.println("IN GET OUT TRAN:" + user + "," + currentTask);

			String sql = "SELECT EXTENDED,TASK_TYPE, CURRENT_TRAN,TRAN_NAME,ID_EST,ID_EST_NEW,"
					+ "TASK_DOM,ACT_NAME,ID_ACTION,ACTION_TYPE,MAIN_OPTYPE,ACT_FILTER,ACT_ID_DOM,ACT_DOM,ACT_DETAIL,"
					+ "CURRENT_STATE, ID_AT_STATE,ID_TO_TRAN, "
					+ "ID_CTX,ID_O_TRAN,RDN_CTX_OBJ,FILTER_NOD_FIX,OBJ_FIX,RDN_FIX,OT_ASIGNMENT,OPERATION_DETAIL"
					+ " FROM V_OUT_TRANS  WITH(NOLOCK) WHERE OWNER= '"
					+ user
					+ "' AND "
					+ " ID_CURRENT_TASK = "
					+ currentTask
					+ " AND (USER_ACTION IS NULL OR "
					+ "USER_ACTION = 1) AND (CURR_TASK_FIX IS NULL OR CURR_TASK_FIX= "
					+ currentTask + " )";

			rs = null;
			st = m_pool.getProcessConn().createStatement();

			rs = st.executeQuery(sql);
			int tran = 0, oldtran = 0, act = 0, oldact = 0;
			HashMap dominios = new HashMap();
			//metaData md = m_dic.getMetaData(empresa);

			Element eTask = new Element("TASK");
			Document resDoc = new Document(eTask);
			eTask.setAttribute("USER", user);
			Element eTran = null, eAction = null, eFilter = null;
			System.out.println("IN GET OUT TRAN:2");
			while (rs.next()) {
				System.out.println("IN GET OUT TRAN:3");
				eTask.setAttribute("EXTENDED", rs.getInt(1) == 1 ? "TRUE"
						: "FALSE");
				eTask.setAttribute("TYPE", String.valueOf(rs.getInt(2)));
				tran = rs.getInt(3);
				if (tran != oldtran) {
					eTran = new Element("TASK_TRANSITION");
					eTask.addContent(eTran);
					eTran.setAttribute("ID", String.valueOf(tran));
					eTran.setAttribute("NAME", rs.getString(4));

					eTask.setAttribute("CURRENT_STATE", String.valueOf(rs
							.getInt(5)));

					eTran.setAttribute("END_STATE", String
							.valueOf(rs.getInt(6)));
				}

				String taskDom = rs.getString(7);
				if (!rs.wasNull()) {
					Element eTaskDom = (Element) jdomParser.readXML(taskDom)
							.getRootElement().clone();
					eTran.addContent(eTaskDom);
				}
				String actionName = rs.getString(8);

				if (!rs.wasNull()) {
					act = rs.getInt(9);
					int actType = rs.getInt(10);
					if (tran != oldtran || act != oldact) {
						eAction = new Element("ACTION");
						eTran.addContent(eAction);
						eAction.setAttribute("NAME", actionName);
						eAction.setAttribute("ID", String.valueOf(act));
						eAction.setAttribute("TYPE", String.valueOf(actType));
						int mainop = rs.getInt(11);
						if (!rs.wasNull())
							eAction.setAttribute("OPERATION_TYPE", String
									.valueOf(mainop));
						String filter = rs.getString(12);
						if (!rs.wasNull()) {
							eFilter = (Element) jdomParser.readXML(filter)
									.getRootElement().clone();
						} else {
							eFilter = new Element("FILTER");
							eFilter.setAttribute("REF", "1");
						}
						eAction.addContent(eFilter);
						int idDom = rs.getInt(13);
						String actDom = rs.getString(14);
						if (!rs.wasNull()) {
							dominios.put(new Integer(idDom), eAction);
						}
						String actDetail = rs.getString(15);
						if (!rs.wasNull()) {
							Element eDetail = (Element) jdomParser.readXML(
									actDetail).getRootElement().clone();
							eAction.addContent(eDetail);
						}
					}
				}
				if (tran != oldtran) {
					//entonces esta transición es la asignada por defecto
					eTran.setAttribute("ASIGNED", "TRUE");
					int currSt = rs.getInt(16);
					eTask.setAttribute("CURRENT_STATE", String.valueOf(currSt));
					//int idatSt = rs.getInt(17);
					//eTask.setAttribute("CURRENT_STATE_NAME", getEnumName(idatSt, currSt, user));
				}
				int idtoInstance = rs.getInt(18);
				if (eFilter != null)
					eFilter.setAttribute("ID_TO", String.valueOf(idtoInstance));
				if (eAction != null && idtoInstance != 0 && act != oldact)
					eAction.setAttribute("ID_TO_TRAN", String
							.valueOf(idtoInstance));
				if (eAction != null) {
					int ctx = rs.getInt(19), objtran = rs.getInt(20);

					if (ctx != 0 && act != oldact)
						eAction.setAttribute("CONTEXT", String.valueOf(ctx));

					if (objtran != 0) {
						eAction.setAttribute("ID_O_TRAN", String
								.valueOf(objtran));
						eAction.setAttribute("RDN", rs.getString(21));
					}

					if (eFilter != null) {

						Element subNodeF = jdomParser.findElementByAt(eFilter,
								"FILTER", "OID", rs.getString(22), true);
						if (subNodeF != null) {
							subNodeF.setAttribute("ID_O", String.valueOf(rs
									.getInt(23)));
							subNodeF.setAttribute("RDN", rs.getString(24));
						}
					}
				}
				int otAsig = rs.getInt(25);
				if (otAsig != 0) {
					eTask.setAttribute("OT_ASIGNMENT", "TRUE");
					Element detail = (Element) jdomParser.readXML(
							rs.getString(26)).getRootElement().clone();
					eTask.addContent(detail);
				}

				oldtran = tran;
				oldact = act;
			}
			st.close();
			stClosed = true;
			
			message msg = new message(
					dynagent.communication.message.MSG_CONFIRM);
			msg.setContent(resDoc.getRootElement());
			msg.setUser(user);
			msg.setActionWay(false);
			return msg;
		} catch (SQLException e) {
			error("INSTANCE GETOUTTRAN: Error SQL:" + e.getErrorCode()
					+ e.getMessage(), e);
		} catch (JDOMException e) {
			error("INSTANCE GETOUTTRAN: Error JDOM:" + e.getMessage(), e);
		} catch (Exception e) {
			error("INSTANCE GETOUTTRAN:" + e.getMessage(), e);
		} finally {
			try {
				if (st != null && !stClosed)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return null;
	}*/

/*	private String getEnumName(int at, int val, String user)
			throws DataErrorException, SQLException, NamingException {
		int empresa = m_IS.getUserBusiness(user);
		metaData md = m_dic.getMetaData(empresa);
		return md.getEnumLabel(new Integer(at), val);
	}*/

	/*public String getFlowConfigs(String msg) {
		ResultSet rs = null;
		Statement st = null;
		try {
			jdomParser root = new jdomParser(msg);
			String user = root.getAttributeValue("USER");
			Element response = new Element("CONFIGURACIONES");
			String sql = "SELECT DISTINCT CONFIG_OID,CONFIG_ID FROM V_META_FILTER_DOM  WITH(NOLOCK) WHERE TO_CURRENT= "
					+ Integer.parseInt(root.getAttributeValue("ID_TO"))
					+ " AND "
					+ "ID_ACTION= "
					+ Integer.parseInt(root
							.getAttributeValue("CURR_TRAN_ACTION")) + " ";

			if (root.getAttributeValue("ID_REL") != null) {
				sql += " AND ID_REL= "
						+ Integer.parseInt(root.getAttributeValue("ID_REL"))
						+ " AND ID_ROL_CTX= "
						+ Integer.parseInt(root
								.getAttributeValue("ID_ROL_CONTEXT"))
						+ " AND ID_ROL_CURRENT= "
						+ Integer.parseInt(root
								.getAttributeValue("ID_ROL_CURRENT")) + " ";
			}
			st = m_pool.getProcessConn().createStatement();

			rs = st.executeQuery(sql);
			while (rs.next()) {
				String oid = rs.getString(1);
				if (rs.wasNull())
					continue;
				Element cfg = new Element("CONFIG");
				response.addContent(cfg);
				cfg.setAttribute("OID", oid);
				cfg.setAttribute("ID", String.valueOf(rs.getInt(2)));
			}
			response.setAttribute("USER", user);
			return jdomParser.returnXML(response);

		} catch (SQLException e) {
			error("INSTANCE:GET DOM:Error SQL:" + e.getErrorCode()
					+ e.getMessage());
		} catch (JDOMException e) {
			error("INSTANCE:GET DOM:Error XML:" + e.getMessage());
		} catch (DataErrorException e) {
			error("INSTANCE:GET DOM:Error XML:" + e.getMessage());
		} catch (NamingException e) {
			error("INSTANCE:GET DOM:Error Naming:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return null;
	}

	public String getConfig(int config) {
		ResultSet rs = null;
		Statement st = null;
		try {
			Element response = null;
			String sql = "SELECT CONFIG FROM T_CONFIGURACIONES  WITH(NOLOCK) WHERE ID= "
					+ config;

			st = m_pool.getProcessConn().createStatement();
			rs = st.executeQuery(sql);

			if (rs.next()) {
				String res = rs.getString(1);
				return res;
			} else {
				error("TRAN CTX,GET CONGIG,error no existe la config:" + config);
			}
		} catch (SQLException e) {
			error("INSTANCE:GET CONGIG:Error Naming:" + e.getErrorCode()
					+ e.getMessage());
		} catch (NamingException e) {
			error("INSTANCE:GET CONGIG:Error Naming:" + e.getMessage());
		} finally {
			try {
				if (st != null)
					st.close();
				m_pool.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}

		return null;
	}*/

	/**************************************/
	/*private String returnXML(org.jdom.Document doc) {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", true);
		res = outputter.outputString(doc);

		return res;
	}*/

	/*private String returnXML(org.jdom.Element doc) {
		String res = "error";
		XMLOutputter outputter = new XMLOutputter("  ", true);
		res = outputter.outputString(doc);
		return res;
	}

	private Document readXML(String str) {
		try {
			SAXBuilder builder = new SAXBuilder();
			StringReader sr = new StringReader(str);
			return builder.build(sr);
		} catch (JDOMException e) {
			throw new EJBException(e.getMessage() + " EXPERT_ERROR_JDOM");
		}
	}*/

	/*********************** Database Routines *************************/

	/*********************/

	/*private void addAttribute(Element item, String label, String value)
			throws SQLException {
		if (value != null)
			item.setAttribute(new Attribute(label, value.trim()));
	}

	private void addAttribute(Element item, String label, int value,
			boolean wasnull) throws SQLException {
		if (!wasnull)
			item.setAttribute(new Attribute(label, String.valueOf(value)));
	}*/

	/*private String getIntAttributeValue(Element nodo, String label) {
		String val = nodo.getAttributeValue(label);
		if (val == null)
			return "null";
		return val;
	}

	private String getBoolAttributeValue(Element nodo, String label) {
		String val = nodo.getAttributeValue(label);
		if (val == null)
			return "0";
		if (val.equals("SI"))
			return "1";
		if (val.equals("NO"))
			return "0";
		return "0";
	}

	private String getNotBoolAttributeValue(Element nodo, String label) {
		String val = nodo.getAttributeValue(label);
		if (val == null)
			return "0";
		if (val.equals("SI"))
			return "0";
		if (val.equals("NO"))
			return "1";
		return "1";
	}*/

	/*private void DbExecUpdate(String sql, java.sql.Connection con)
			throws SQLException {
		//System.out.println("DBEXEC:"+sql);
		Statement st = null;
		try {
			st = con.createStatement();
			st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
		}
	}*/

	//////// ***************************************** PROCESOS  *******************************************************
	/*public void onProcessMessage(String msgUID,
			dynagent.communication.message propar) {
		String user = "", msg = "";
		try {
			inicializaJMSConnection();
			user = propar.getUser();
			jmsTranID = msgUID;
			jmsTranIDCount = 0;

			if (propar.getType() == dynagent.communication.message.MSG_OBJECT_TRAN)
				onMessageObjectTransition((contextAction) propar);*/
			/*if (propar.getOrderType() == dynagent.communication.message.FLOW_OWNING) {
				System.out.println("PROCESS,DENTRO OWNING");
				owningAction oa = (owningAction) propar;
				if (oa.getOwningLevel() == oa.OWNING_PREASIGNED)
					onMessagePreasignTran(oa, false);
				if (oa.getOwningLevel() == oa.OWNING_APROPIATED) {
					onMessageApropiateTran(oa, false);
				}
			}*/
			/*if (propar.getOrderType() == dynagent.communication.message.FLOW_OWNING_NEW_TASK) {
				System.out.println("PROCESS,DENTRO OWNING AND TASK");
				owningAction oa = (owningAction) propar;
				createProcess(oa.getCurrProcess(), oa.getProcessType(), user);

				if (oa.getOwningLevel() == oa.OWNING_PREASIGNED)
					onMessagePreasignTran(oa, true);
				if (oa.getOwningLevel() == oa.OWNING_APROPIATED) {
					onMessageApropiateTran(oa, true);
				}
			}*/
			/*if (propar.getOrderType() == dynagent.communication.message.FLOW_GROW) {
				System.out.println("PROCESS,DENTRO GROWN");
				onMessageGrowOwLevel((owningAction) propar);
			}
			if (propar.getOrderType() == dynagent.communication.message.FLOW_DECREASE) {
				System.out.println("PROCESS,DENTRO GROWN");
				onMessageDecreaseOwLevel((owningAction) propar);
			}
			if (propar.getOrderType() == dynagent.communication.message.FLOW_NEW_TASK) {
				onMessageNewTask((flowAction) propar);
			}
			if (propar.getOrderType() == dynagent.communication.message.FLOW_END_PRO) {
				endProcess((flowAction) propar);
				// m_state = FINALIZANDO;
			}
			if (propar.getOrderType() == dynagent.communication.message.FLOW_TRANSITION) {
				onMessageTransition((flowAction) propar);
			}
			if (propar.getType() == dynagent.communication.message.MSG_EXE_TRAN_ACTION) {
				String buf = exeTranAction((flowAction) propar);
			}*/
			/*System.out.println("PROCESS ONMSG END");
		} catch (IllegalDataException e) {
			error("PROCESS ERROR PARSEO JDOM" + e.getMessage(), e);
		} catch (NullPointerException e) {
			error("PROCESS ERROR Captura Id Pro", e);
		} catch (JDOMException e) {
			error("PROCESS ERROR PARSEO JDOM" + e.getMessage(), e);
		} catch (ParseException e) {
			error("PROCESS ERROR PARSEO " + e.getMessage(), e);
		} catch (DataErrorException e) {
			error("PROCESS ERROR DATOS " + e.getMessage(), e);
		} catch (Throwable te) {
			error("Process.onMessage: " + "Exception: " + te.getClass() + " "
					+ te.toString(), te);
		} finally {
			try {
				//m_pool.close();
				queueConnection.close();
			//} catch (SQLException e) {
				//error("ERROR CLOSING CONNECTION", e);
			} catch (JMSException ex) {
				error("ERROR CLOSING CONNECTION", ex);
			}
		}
	}*/

	/*void createProcess(int id, int idto, String user) throws NamingException,
			SQLException {
		Statement st = null;
		ResultSet rs = null;
		boolean stClosed = false;
		try {
			st = m_pool.getProcessConn().createStatement();
			String sql = "SELECT * from S_Current_process WITH(NOLOCK) WHERE id_instance="
					+ id;
			rs = st.executeQuery(sql);
			//boolean nuevoProceso = true;
			System.out.println("PRE CHECK PROCESO " + id);
			if (rs.next()) {
				System.out.println("EL PROCESO " + id + " YA EXISTIA");
				//nuevoProceso = false;
			} else {
				System.out.println("PROCESO NUEVO " + id);
				st.close();
				stClosed = true;
				insertNewProcessRow(id, idto);
				DbExecUpdate(
						"INSERT INTO LOG_PROCESS(fecha,ID_CURRENT_PRO, ID_USER,LOG_ACTION) VALUES("
								+ "GETDATE()," + id + ",'"
								+ user.replaceAll("'", "''") + "','NEW_PRO')",
						m_pool.getProcessConn());
			}
		} finally {
			if (st != null && !stClosed)
				st.close();
		}
	}*/

	/*void insertNewProcessRow(int id, int idto) throws NamingException,
			SQLException {
		String sql = "insert into S_Current_process(id_tipo, id_instance) values ("
				+ idto + "," + id + ")";
		Statement st = null;
		try {
			st = m_pool.getProcessConn().createStatement();
			st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
		}
	}*/

/*	boolean checkDN(instance ins, int empresa, Element out)
			throws SQLException, NamingException {
		boolean exito = subCheckDN(ins, empresa, out);
		Iterator itr = ins.getEstructIterator(true);
		while (itr.hasNext()) {
			instance insHijo = (instance) itr.next();
			exito = exito
					& checkDN(insHijo, empresa, out);
		}
		return exito;
	}

	boolean subCheckDN(instance ins, int empresa, Element out)
			throws SQLException, NamingException {
		ResultSet rs = null;
		Statement st = null;
		if (ins == null)
			return true;
		boolean exito = true;
		String ValRdn = ins.getRdn();
		if (ValRdn == null)
			return true;
		Element toDef = null;
		Integer iTO = new Integer(ins.getIdTo());
//		toDef = md.getMetaTO(iTO);
		//TODO buscar equivalente a esta funcion en IKnowledge
		ConnectionDB con = null;
		try {
			con = factConnDB.createConnection(false);
			st = con.getBusinessConn().createStatement();
			if (StringUtils.equals(toDef.getAttributeValue("RDNSCOPE"),
					String.valueOf(helperConstant.SCOPE_DISTINGUIDO))) {
				try {
					int rdnNumeric = Integer.parseInt(ValRdn);
					if (rdnNumeric < 0 && rdnNumeric == ins.getIDO())
						return true;
				} catch (NumberFormatException e) {
					;
				}
				String sql = "SELECT * FROM O_Reg_instancias WITH(NOLOCK) WHERE ID_TO="
						+ iTO
						+ " AND RDN='"
						+ ValRdn.replaceAll("'", "''") + "'";
				rs = st.executeQuery(sql);
				if (rs.next()) {
					Element rdn = new Element("RDN");
					rdn.setAttribute("TEXT", ValRdn);
					rdn.setAttribute("ID_O", String.valueOf(ins.getIDO()));
					rdn.setAttribute("ID_TO", String.valueOf(ins.getIDO()));
					out.addContent(rdn);
					exito = false;
				}
			}
		} catch (SQLException e) {
			error("InstEJB,getRolesUsuario, error SQL:" + e.getMessage());
		} catch (NamingException e) {
			error("InstEJB,getRolesUsuario, error Naming:" + e.getMessage());
		} finally {
			try {
				if (rs!=null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					con.close();
			} catch (SQLException e) {
				error("ERROR CLOSING CONNECTION", e);
			}
		}
		return exito;
	}
*/
	/*void onMessageNewTask(flowAction pp) throws SQLException, NamingException,
			DataErrorException {
		System.out.println("TEST " + pp.toString());
		insertCurrentTask(pp.getCurrProcess(), pp.getCurrTask(), pp
				.getTaskType(), pp.getUser(), pp.getUserRol(), pp
				.getCurrTaskTrans(), pp.getCurrTaskState(), false, 0, 0); //pp.getCurrParentTask());
		pp.setActionWay(false);
	}

	void insertCurrentTask(int idpro, int currTask, int task, String user,
			int userRol, int tran, int state, boolean otAsignment, long fecha,
			int currParentTask) throws NamingException, SQLException,
			DataErrorException {

//		int empresa = m_IS.getUserBusiness(user);
		String sql = "", strSt = "";
		//Element taskInst = new Element("OBJECT");
		
		//TODO modificar esto
//		taskType tt = md.getTask(new Integer(task));
//		if (state == 0) {
//			strSt = String.valueOf(tt.stateStart);
//		} else
			strSt = String.valueOf(state);

		sql += "INSERT INTO S_Current_task (business,id_current_process,Id_current_task,Id_task,"
				+ "ID_CURRENT_TRAN,current_state,OT_ASIGNMENT";
		if (user != null) {
			sql += ",OWNER,ASIGNED_ROL";
		}
		if (fecha > 0)
			sql += ",EXE_DATE";
		if (currParentTask != 0)
			sql += ",ID_CURR_PARENT_TASK";
		sql += ") values (" + business + "," + idpro + "," + currTask + ","
				+ task + "," + tran + "," + strSt + "," + (otAsignment ? 1 : 0);
		if (user != null)
			sql += ",'" + user.replaceAll("'", "''") + "'," + userRol;
		if (fecha > 0)
			sql += ",'" + dateUtil.getSqlExeDate(fecha) + "'";
		if (currParentTask != 0)
			sql += "," + currParentTask;
		sql += ")";
		System.out.println("NEW TASK:user, sql:" + user + "," + sql);
		DbExecUpdate(sql, m_pool.getProcessConn());
	}*/

	/*ArrayList procesaAccion(int idPro, int proType, flowAction pp)
			throws SQLException, JDOMException, NamingException,
			SystemException, ParseException, DataErrorException, Exception {
		Element actionDetail = (Element) pp.getContent();
		Iterator itr = actionDetail.getChildren("RULE").iterator();
		ArrayList<String> res = new ArrayList<String>();
		//int empresa = m_IS.getUserBusiness(pp.getUser());
		//metaData md = m_dic.getMetaData(empresa);

		//Contexto ctx = md.getContext(new Integer(pp.ggetContext()));
		Integer userRol = new Integer(pp.getUserRol());

		int newState = -1;
		ArrayList contextRule = new ArrayList();
		while (itr.hasNext()) {
			Element rule = (Element) itr.next();
			Iterator iAct = rule.getChildren("ACTION").iterator();
			while (iAct.hasNext()) {
				Element action = (Element) iAct.next();
				if (action.getAttributeValue("TYPE").equals("SWITCH")) {
					//accion tipo decision
					//selectObject(userRol, false, rule, pp, contextRule);
					newState = Integer.parseInt(action.getAttributeValue("NEW_ESTADO"));
					System.out.println("TEST OK, new State:" + newState);
					transition(idPro, proType, pp.getUser(), pp
							.getCurrTaskTrans(), pp.getCurrTask(), pp
							.getTaskType(), newState);
					pp.setType(dynagent.communication.message.MSG_FLOW);
					pp.setActionWay(false);
					pp.setOrderType(dynagent.communication.message.FLOW_TRANSITION);
					pp.setCurrTaskState(newState);
					System.out.println("PRE PRO DEC:" + pp.getCurrTaskTrans());
					String s = "SELECT ID_EST FROM TASK_TRANSITION WITH(NOLOCK) WHERE ID_TRAN="
							+ pp.getCurrTaskTrans();
					ResultSet rs = null;
					Statement st = null;
					try {
						st = m_pool.getProcessConn().createStatement();
						rs = st.executeQuery(s);
						if (rs.next()) {
							threadData td = new threadData(pp);
							int oldSt = rs.getInt(1);
							td.setOldState(oldSt);
							System.out.println("PRO DEC:" + oldSt);
							pp = td;
						}
						res.add(pp.toHeaderString());
						return res;
					} finally {
						if (st != null)
							st.close();
					}
				}
				if (action.getAttributeValue("TYPE").equals("SET")) {
					//selectObject(userRol, false, rule, pp, contextRule);
					exeSetAction(idPro, userRol, contextRule, action, pp);
				}

				if (action.getAttributeValue("TYPE").equals("REFRESH")) {
					System.out.println("PROCESA ACCION REFRESH");
					//selectObject(userRol, true, rule, pp, contextRule);
					Iterator iTask = action.getChildren("TASK").iterator();
					while (iTask.hasNext()) {
						Element task = (Element) iTask.next();
						int id = Integer.parseInt(task
								.getAttributeValue("TASK_TYPE"));
						System.out.println("REFRESH TASK TYPE " + id);
						res.addAll(exeRefreshAction(contextRule, id, pp));
					}
				}
			}
		}
		return res;
	}*/

	/*ArrayList<String> exeRefreshAction(ArrayList contextRule,
			int refreshTask, flowAction pp) throws DataErrorException,
			SQLException, JDOMException, NamingException, Exception {

//		int empresa = m_IS.getUserBusiness(pp.getUser());
		HashMap<Integer, ArrayList<Integer>> currTaskList = new HashMap<Integer, ArrayList<Integer>>();
		System.out.println("REFRESH CTXRULE size:" + contextRule.size());
		for (int i = 0; i < contextRule.size(); i++) {
			Object obj = contextRule.get(i);
			if (obj instanceof threadContextData) {
				threadContextData data = (threadContextData) obj;
				Integer to = new Integer(data.currData.getIdTo()); //objeto abierto con proceso/tarea abierta
				Integer objCurrTask = new Integer(data.currData
						.getCurrentTask());
				//Integer objCurrTran = new Integer(data.currData.getTransition());
				Integer objCurrDom = new Integer(data.currData.getIdDom());
				Integer objOldCtx = new Integer(data.currData.getContextID());
				Integer objOldTaskType = new Integer(data.oldData.getTaskType());
				Integer objOldState = new Integer(data.currData.getTaskState());

				// debo saltarme los objetos contextuados por la tarea actual,
				// por que ya están gestionados, para fijarme en otras tareas que no estan dinamizadas ahora
				if (objCurrTask.intValue() == pp.getCurrTask())
					continue;
				//Contexto ccOldCtx = md.getContext(objOldCtx);
				if (objOldTaskType.intValue() != refreshTask)
					continue;
				ArrayList<Integer> datos = new ArrayList<Integer>();
				datos.add(objOldState);
				datos.add(to);
				currTaskList.put(objCurrTask, datos);
				String sql = "UPDATE S_CURRENT_CTX SET ID_DOM=" + objCurrDom
						+ " WHERE ID_OBJ=" + data.currData.getIDO()
						+ " AND ID_CTX_TYPE=" + objOldCtx
						+ " AND ID_CURR_TASK=" + objCurrTask;

				System.out.println("REFRESH, update DOM " + sql);
				DbExecUpdate(sql, m_pool.getProcessConn());
			}
		}
		ArrayList<String> resMsg = new ArrayList<String>(); //nose si funcionara retornar un array
		Iterator iTC = currTaskList.keySet().iterator();
		nextCtx: while (iTC.hasNext()) {
			Integer idCurrTask = (Integer) iTC.next();
			ArrayList datos = (ArrayList) currTaskList.get(idCurrTask);
			Integer idOldSt = (Integer) datos.get(0);
			Integer idTO = (Integer) datos.get(1);

			String strSup = "";
			Iterator iSup = ik.getSuperior(idTO);
			while (iSup.hasNext()) {
				Integer idSup = (Integer) iSup.next();
				if (strSup.length() > 0)
					strSup += ",";
				strSup += idSup.toString();
			}

			String sql = "	SELECT SX.ID_DOM,CTX.ID_DOMINIO,TT.ID_TRAN,ID_CURRENT_PROCESS,STA.ID_PROCESS,STA.ID_ACTION "
					+ "	FROM "
					+ "	S_CURRENT_CTX SX WITH(NOLOCK)					                          INNER JOIN "
					+ "	S_CURRENT_TASK CTAS WITH(NOLOCK)        ON SX.ID_CURR_TASK=CTAS.ID_CURRENT_TASK           INNER JOIN "
					+ "	TASK_TRANSITION TT WITH(NOLOCK)	        ON 	( SX.CURR_STATE=TT.ID_EST AND "
					+ "					                  TT.ID_TASK="
					+ refreshTask
					+ ")         INNER JOIN "
					+ "	S_TASK_ACTION STA WITH(NOLOCK)	        ON STA.ID_TRAN=TT.ID_TRAN	                  INNER JOIN "
					+ "	S_TASK_ACTION_CONTEXT CTX WITH(NOLOCK)	ON ( CTX.ID_ACTION=STA.ID_ACTION	AND "
					+ "                                                  CTX.ID_TO IN("
					+ strSup
					+ "))	          INNER JOIN "
					+ "     dyna"
					+ business
					+ ".dbo.T_INDEX_TO THC  WITH(NOLOCK) ON (THC.ID_TO=SX.ID_TO AND THC.ID_TO_H=CTX.ID_TO)"
					+ "	WHERE ID_CURR_TASK="
					+ idCurrTask
					+ " AND SX.CURR_STATE=" + idOldSt;
			int currPro = 0, currTran = 0, action = 0, proType = 0;
			ResultSet rs = null;
			Statement st = null;

			try {
				st = m_pool.getProcessConn().createStatement();
				System.out.println(sql);
				rs = st.executeQuery(sql);
				while (rs.next()) {
					int currDom = rs.getInt(1);
					int newStDom = rs.getInt(2);
					System.out.println("DOMS " + currDom + "," + newStDom);

//					if (!md.domIsSpecializedFrom(new Integer(currDom),
//							new Integer(newStDom))) {
					if (!ik.isSpecialized(new Integer(currDom), new Integer(newStDom))) {
						continue nextCtx;
					}
					System.out.println("DOMS PASO " + currDom + "," + newStDom);
					currTran = rs.getInt(3);
					currPro = rs.getInt(4);
					proType = rs.getInt(5);
					action = rs.getInt(6);
				}
				// todos los contextos has sido transicionados a otro paso
			} finally {
				if (st != null)
					st.close();
			}
			flowAction res = new flowAction(
					dynagent.communication.message.MSG_FLOW);
			res.setProcessType(proType);
			res.setOrderType(dynagent.communication.message.FLOW_TRANSITION);
			res.setActionWay(true);
			res.setUser(pp.getUser());
			res.setCurrTask(idCurrTask.intValue());
			res.setCurrTaskTrans(currTran);
			res.setCurrProcess(currPro);
			res.setTaskType(refreshTask);
			//res.setsetActionType( action );
			resMsg.add(res.toString());
		}
		return resMsg;
	}*/

	/*void exeSetAction(int idPro, Integer userRol, ArrayList contextRule,
			Element action, flowAction pp) throws SQLException,
			NamingException, JDOMException, DataErrorException, Exception {
		for (int i = 0; i < contextRule.size(); i++) {
			Object obj = contextRule.get(i);
			subSetAtAction(idPro, userRol, obj, pp, action);
		}
	}*/

	/*void subSetAtAction(int idPro, Integer userRol, Object root, flowAction pp,
			Element action) throws DataErrorException, JDOMException,
			NamingException, SQLException, Exception {
//		int empresa = m_IS.getUserBusiness(pp.getUser());
		//metaData md = m_dic.getMetaData(empresa);

		int ido = 0, to = 0;
		if (root instanceof instance) {
			ido = ((instance) root).getIDO();
			to = ((instance) root).getIdTo();
		} else if (root instanceof threadContextData) {
			threadContextData td = (threadContextData) root;
			ido = td.currData.getIDO();
			to = td.currData.getIdTo();
		}
		m_IS.serverOrderObject(userRol, pp.getUser(), (instance)root, pp.getCurrTaskTrans(), business);*/
//		m_IS.serverSetObj(userRol, pp.getUser(), (instance)root, pp.getCurrTaskTrans(), empresa);
/*		Iterator iAt = action.getChild("ATRIBUTOS").getChildren("AVA")
				.iterator();
		while (iAt.hasNext()) {
			Element order = (Element) iAt.next();
			m_IS.DbSetAt(false, ido, to, idPro, pp.getCurrTask(), pp.getCurrTaskTrans(), 0, 
					messageFactory.buildAttribute(order, dynagent.application.action.SET), 
					empresa, userRol,null, true);
		}*/
	//}

	/*int transition(int idPro, int proType, String user, int tran, int currTask,
			int taskType, int newSt) throws EJBException, NamingException,
			SQLException {
		ResultSet rs = null;
		Statement st = null;
		boolean stClosed = false;
		try {
			if (newSt == -1) {
				String s = "SELECT ID_EST_NEW FROM TASK_TRANSITION WITH(NOLOCK) WHERE ID_TRAN= "
						+ tran;
				st = m_pool.getProcessConn().createStatement();
				rs = st.executeQuery(s);
				if (rs.next())
					newSt = rs.getInt(1);
				st.close();
				stClosed = true;
			}
			if (newSt != -1) {
				String sql = "UPDATE S_Current_task SET current_state=" + newSt
						+ ",OWNER=NULL,ASIGNED_ROL=NULL "
						+ "WHERE ID_CURRENT_TASK=" + currTask;
				DbExecUpdate(sql, m_pool.getProcessConn());
				sql = "INSERT INTO LOG_PROCESS(fecha,ID_CURRENT_PRO,ID_T_PRO,"
						+ "ID_CURRENT_TASK,ID_T_TASK,ID_USER,LOG_ACTION,TRANS)"
						+ " VALUES(GETDATE()," + idPro + "," + proType + ","
						+ currTask + "," + taskType + ",'"
						+ user.replaceAll("'", "''") + "','TRANSITION'," + tran
						+ ")";
				DbExecUpdate(sql, m_pool.getProcessConn());
			} else {
				throw new EJBException(
						"PROCESS:TRANSITION:No se encontra el nuevo estado");
			}
			return newSt;
			*//*****************//*
		} finally {
			if (st != null && !stClosed)
				st.close();
		}
	}*/

	/*void selectObject(Integer userRol, boolean toRefreshTask,
			Element selectRule, flowAction pp, ArrayList contextRule)
			throws SQLException, JDOMException, NamingException,
			SystemException, ParseException, DataErrorException, Exception {

		int empresa = m_IS.getUserBusiness(pp.getUser());
		//metaData md = m_dic.getMetaData(empresa);

		Iterator iSel = selectRule.getChildren("SELECT").iterator();
		while (iSel.hasNext()) {
			Element select = (Element) iSel.next();
			int scope = toRefreshTask ? SCOPE_THIS_CTX_NOT_CURRTASK
					: SCOPE_RULE;
			if (select.getAttributeValue("SCOPE") != null) {
				if (select.getAttributeValue("SCOPE").equals("TASK"))
					scope = SCOPE_CURRTASK;
				if (select.getAttributeValue("SCOPE").equals("BUSINESS"))
					scope = SCOPE_BUSINESS;
			}

			if (select.getAttributeValue("ID_CONTEXT") != null) {
				System.out.println("PROCESANDO SELECT CONTEXT "
						+ select.getAttributeValue("ID_CONTEXT"));
				ArrayList lista = getInstanceContext(md, true, Integer
						.parseInt(select.getAttributeValue("ID_CONTEXT")), pp,
						empresa, userRol, scope, true);
				contextRule.addAll(lista);
			}
			if (select.getAttributeValue("ID_FILTER") != null)
				selectByFilter(select, Integer.parseInt(select
						.getAttributeValue("ID_FILTER")), pp, contextRule,
						scope);
		}
	}

	void selectByFilter(Element select, int idFilter,
			flowAction pp, ArrayList contextRule, int scope)
			throws SystemException, NamingException, SQLException,
			JDOMException, ParseException, DataErrorException {
		Element query = (Element) md.getFilter(null, new Integer(idFilter))
				.clone();

		selectData sData = m_IS.getThreadData(0, 0, pp.getCurrTask());
		ArrayList fixList = new ArrayList();
		Iterator iFix = select.getChildren("FILTER_FIXING").iterator();
		while (iFix.hasNext()) {
			Element fixItem = (Element) iFix.next();

			int refSource = fixItem.getAttributeValue("REF_SOURCE") == null ? 0
					: Integer.parseInt(fixItem.getAttributeValue("REF_SOURCE"));

			fixProperties fp = new fixProperties(false, fixItem
					.getAttributeValue("OID"), Integer.parseInt(fixItem
					.getAttributeValue("ID_CONTEXT")), refSource, (fixItem
					.hasChildren() ? fixItem : null), 0);
			fixList.add(fp);
		}

		//TODO docDataModel.fixThreadData(md, fixList, query, sData);
		query.setAttribute("USER", pp.getUser());

		selectData sd = m_IS.Query(query, md.getBusiness()).toSelectData(query);
		Iterator itr = sd.getIterator();
		while (itr.hasNext()) {
			instance obj = (instance) itr.next();
			contextRule.add(obj.clone());
		}
	}*/

	/*ArrayList getInstanceContext(boolean queryFormat, int ctx,
			flowAction pp, int empresa, Integer userRol, int scope,
			boolean getCurrTran) throws JDOMException, DataErrorException,
			NamingException, SQLException, Exception {
		ArrayList lista = new ArrayList();
		Object res = null;
		ResultSet rs = null;
		Statement st = null;

		try {
			String sql = " SELECT CTX.CURR_STATE,ID_OBJ, ID_TO, ID_CTX_TYPE,ID_CURR_TASK, ID_DOM ";

			if (getCurrTran)
				sql += ", ID_CURRENT_TRAN,CT.ID_TASK ";
			sql += " FROM S_CURRENT_CTX CTX WITH(NOLOCK) ";

			if (getCurrTran)
				sql += " INNER JOIN S_CURRENT_TASK CT WITH(NOLOCK) ON "
						+ " CTX.ID_CURR_TASK=CT.ID_CURRENT_TASK ";

			sql += " WHERE ID_CTX_TYPE=" + ctx;

			if (scope == SCOPE_CURRTASK)
				sql += " AND ID_CURR_TASK=" + pp.getCurrTask();
			if (scope == SCOPE_RULE || scope == SCOPE_BUSINESS)
				sql += " AND EMPRESA=" + empresa;

			if (scope == SCOPE_THIS_CTX_NOT_CURRTASK) {
				ArrayList listSup = md.getSpecContexts(userRol, ctx);
				String ctxList = "";
				for (int i = 0; i < listSup.size(); i++) {
					if (ctxList.length() > 0)
						ctxList += ",";
					ctxList += ((Integer) listSup.get(i)).toString();
				}
				sql = " SELECT CTXOLD.CURR_STATE, CTXOLD.ID_OBJ,CTXOLD.ID_TO,CTXOLD.ID_CTX_TYPE,CTXOLD.ID_CURR_TASK,"
						+ "CTXNW.ID_DOM,ID_CURRENT_TRAN,CT.ID_TASK "
						+ " FROM 	 S_CURRENT_CTX CTXOLD 	WITH(NOLOCK)						INNER JOIN "
						+ "S_CURRENT_CTX CTXNW 	WITH(NOLOCK) ON ( 	CTXOLD.ID_OBJ=CTXNW.ID_OBJ AND "
						+ "	CTXNW.ID_CURR_TASK="
						+ pp.getCurrTask()
						+ " AND "
						+ "	CTXNW.ID_CTX_TYPE IN("
						+ ctxList
						+ "))		INNER JOIN "
						+ "S_CURRENT_TASK CT 	WITH(NOLOCK) ON (	CTXOLD.ID_CURR_TASK=CT.ID_CURRENT_TASK AND "
						+ "	CT.CURRENT_STATE=CTXOLD.CURR_STATE) "
						+ " WHERE	"
						+ "	CTXOLD.ID_CURR_TASK<>"
						+ pp.getCurrTask()
						+ " AND "
						+ "	CTXOLD.ID_OBJ IN 	(	SELECT ID_OBJ "
						+ "		FROM S_CURRENT_CTX WITH(NOLOCK) "
						+ "		WHERE ID_CURR_TASK=" + pp.getCurrTask() + ")";
			}

			st = m_pool.getProcessConn().createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			int ido = 0, idto = 0;
			while (rs.next()) {
				int currSt = rs.getInt(1);
				ido = rs.getInt(2);
				idto = rs.getInt(3);
				//               scope myScope= new scope( md, userRol, new access(access.VIEW), access.VIEW );
				if (!queryFormat)
					//No necesito cerrar el st anterior porque el st abierto es de conex proceso y getObj
					// es sobre conex business
					res = m_IS.GetObjElement(userRol, pp.getUser(), idto, ido,
							pp.getCurrTask(), false);

				//                        res=m_IS.GetObjElement(	myScope, pp.getUser(), idto, ido, ctx, pp.getCurrTask(), true, false) ;
				else {
					res = new threadContextData();
					threadContextData td = (threadContextData) res;
					int idCtx = rs.getInt(4);
					int currTask = rs.getInt(5);
					int currDom = rs.getInt(6);

					td.oldData = new contextData(idto, ido, idCtx, 0, 0,
							dynagent.application.action.GET);

					td.currData = new contextData(idto, ido, idCtx, 0,
							currTask, dynagent.application.action.GET);
					td.currData.setTaskState(currSt);
					td.currData.setIdDom(currDom);

					if (getCurrTran) {
						td.currData.setTransition(rs.getInt(7));
						td.oldData.setTaskType(rs.getInt(8));
					}
				}
				lista.add(res);
			}
		} finally {
			if (st != null)
				st.close();
		}
		return lista;
	}*/

	/*void preasignTran(int idPro, int proType, owningAction pp)
			throws NamingException, SQLException, DataErrorException {
//		int empresa = m_IS.getUserBusiness(pp.getUser());

		String action = "PREASIGN";
		String sql = "IF NOT EXISTS(	SELECT * FROM S_PREASIG WITH(NOLOCK) "
				+ "WHERE ID_CURR_TASK="
				+ pp.getCurrTask()
				+ " AND USUARIO='"
				+ pp.getUser().replaceAll("'", "''")
				+ "')"
				+ " INSERT INTO S_PREASIG(ID_CURR_TASK,USUARIO,ASIGNED_ROL) VALUES("
				+ pp.getCurrTask() + ",'" + pp.getUser().replaceAll("'", "''")
				+ "'," + pp.getUserRol() + ")";
		System.out.println(sql);
		DbExecUpdate(sql, m_pool.getProcessConn());

		sql = "UPDATE S_Current_task SET OWNER= NULL,ASIGNED_ROL=NULL";

		String exeDate = dateUtil.getSqlExeDate(pp.getExeDate());
		if (pp.getExeDate() != 0) {
			sql += ", EXE_DATE='" + exeDate + "'";
			updateTaskInstanceDate(pp.getCurrTask(), exeDate, business);
		}

		sql += " WHERE ID_CURRENT_TASK=" + pp.getCurrTask();

		DbExecUpdate(sql, m_pool.getProcessConn());

		sql = "INSERT INTO LOG_PROCESS(fecha,ID_CURRENT_PRO,"
				+ "ID_T_PRO,ID_CURRENT_TASK,TASK_STATE,ID_T_TASK,ID_USER,LOG_ACTION";
		sql += ") VALUES(GETDATE()," + idPro + "," + proType + ","
				+ pp.getCurrTask() + "," + pp.getCurrTaskState() + ","
				+ pp.getTaskType() + ",'" + pp.getUser().replaceAll("'", "''")
				+ "','" + action + "'";
		sql += ")";
		System.out.println("SQL:" + sql);
		DbExecUpdate(sql, m_pool.getProcessConn());
	}*/

	/*void updateTaskInstanceDate(int currTask, String fecha, int empresa)
			throws NamingException, SQLException {
		String where = "WHERE ID_O=@ido AND TA_POS="
				+ helperConstant.TAPOS_FECHA_EXE_TASK;
		String sqlInst = "DECLARE @ido int\n"
				+ "SELECT @ido= ID_INSTANCE_TASK FROM S_Current_task WITH(NOLOCK) WHERE ID_CURRENT_TASK="
				+ currTask + "\n" + "IF NOT EXISTS( SELECT * FROM dyna"
				+ empresa + ".dbo.O_DATOS_ATRIBUTOS WITH(NOLOCK) " + where
				+ ")\n" + " INSERT INTO dyna" + empresa
				+ ".dbo.O_DATOS_ATRIBUTOS(ID_O,ID_TO,TA_POS,VAL_FECHA) "
				+ " VALUES(@ido," + helperConstant.TO_TASK + ","
				+ helperConstant.TAPOS_FECHA_EXE_TASK + ",'" + fecha + "')\n"
				+ "ELSE UPDATE dyna" + empresa
				+ ".dbo.O_DATOS_ATRIBUTOS SET VAL_FECHA='" + fecha + "' "
				+ where;
		System.out.println(sqlInst);
		DbExecUpdate(sqlInst, m_pool.getProcessConn());
	}*/

	/*int apropiateTran(int idPro, int proType, boolean newTask, int taskType,
			int current_task, String user, int userRol, int tran, int state,
			long fecha) throws EJBException, NamingException, SQLException,
			DataErrorException {
		ResultSet rs = null;
		Statement st = null;
		boolean stClosed = false;
		try {
			String action = "OWNING", sql = "";

//			int empresa = m_IS.getUserBusiness(user);
			//metaData md=m_dic.getMetaData( empresa );

			String strDate = dateUtil.getSqlExeDate(fecha);
			if (newTask) {
				System.out.println("DEBUG PROCESS: new task");
				action = "OWNING_&_NEW_TASK";
				sql = "select state_start FROM S_TASKS WITH(NOLOCK) "
						+ "WHERE ID_TASK= " + taskType;
				st = m_pool.getProcessConn().createStatement();
				rs = st.executeQuery(sql);
				if (!rs.next()) {
					System.out.println("PROCESS: NO ENCONTRADO START_STATE");
					throw new EJBException("PROCESS: NO ENCONTRADO START_STATE");
				}
				int stateStart = rs.getInt(1);
				st.close();
				stClosed = true;
				insertCurrentTask(idPro, current_task, taskType, user, userRol,
						tran, state, false, fecha, 0);
				//System.out.println("DEBUG2 PROCESS:currTask:"+current_task);
			} else {
				sql = "UPDATE S_Current_task SET OWNER='"
						+ user.replaceAll("'", "''") + "',ASIGNED_ROL="
						+ userRol + ",ID_CURRENT_TRAN=" + tran
						+ ",CURRENT_STATE=" + state;
				if (fecha != 0) {
					sql += ",EXE_DATE='" + strDate + "'";
					updateTaskInstanceDate(current_task, strDate, business);
				}

				sql += " WHERE ID_CURRENT_TASK=" + current_task;
				DbExecUpdate(sql, m_pool.getProcessConn());
			}

			DbExecUpdate("DELETE FROM S_PREASIG WHERE ID_CURR_TASK="
					+ current_task, m_pool.getProcessConn());

			sql = "INSERT INTO LOG_PROCESS(fecha,ID_CURRENT_PRO,"
					+ "ID_T_PRO,ID_CURRENT_TASK,TRANS,TASK_STATE,ID_T_TASK,ID_USER,LOG_ACTION";
			sql += ") VALUES(GETDATE()," + idPro + "," + proType + ","
					+ current_task + "," + tran + "," + state + "," + taskType
					+ ",'" + user.replaceAll("'", "''") + "','" + action + "'";
			sql += ")";
			DbExecUpdate(sql, m_pool.getProcessConn());
		} finally {
			if (st != null && !stClosed)
				st.close();
			//Si cierro el st se cierra solo el rs
		}
		return current_task;
	}*/

	/*void inicializaColas() throws NamingException {
		Context namCtx = new InitialContext();
		queueCntFtry = (QueueConnectionFactory) namCtx
				.lookup("java:/QueueConnectionFactory");
		buzonRuler = (javax.jms.Queue) namCtx
				.lookup("activemq/queue/RulerQueue");
	}

	void inicializaJMSConnection() throws JMSException {
		queueConnection = queueCntFtry.createQueueConnection();
		sesionTran = queueConnection.createQueueSession(true,
				Session.AUTO_ACKNOWLEDGE); //es transaccional
		senderToRuler = sesionTran.createSender(buzonRuler);
		queueConnection.start();
	}*/

	/*void sendMessage(boolean registraMsg, String msg, String msgID)
			throws JMSException {
		TextMessage message = null;
		message = sesionTran.createTextMessage();
		message.setText(msg);
		message.setJMSType("DATA");
		//String msgID=getNextMsgID();
		message.setJMSCorrelationID(msgID);

		if(msg.length() > 55)
		 System.out.println("PROCES:>>:RL:"+msgID+":"+msg.substring(0,54));
		 elseSystem.out.println("PROCESS:>>:RL:" + msgID + ":" + msg);
		senderToRuler.send(message);
	}*/

	/*void testLoopBackPro(int dato, String msguid) throws NamingException,
			CreateException, RemoteException, JMSException {
		testLoopBackPro(dato, false);
		testLoopBackPro(dato + 1, true);
		dynagent.communication.message debug = new message(
				dynagent.communication.message.MSG_CONFIRM);
		debug.setActionWay(false);
		sendMessage(false, debug.toString(), "#loop" + msguid);
	}*/

	/*void onMessageTransition(flowAction pp) throws NamingException,
			SQLException, JDOMException, JMSException {
		int newSt = transition(pp);

		pp.setActionWay(false);
		pp.setCurrTaskState(newSt);

		String    s= "SELECT ID_EST FROM TASK_TRANSITION WHERE ID_TRAN=" + pp.getCurrTaskTrans();
		 Statement st= m_pool.getProcessConn().createStatement();

		 ResultSet rs = st.executeQuery(s);
		 if(rs.next()){
		 int oldSt=rs.getInt(1);//util en dynaApplet para completar tareas
		 threadData td= new threadData(pp);
		 td.setOldState(oldSt);
		 pp=td;
		 }
		 st.close();
		sendMessage(true, pp.toString(), getNextMsgID());
	}*/

	/*void onMessageObjectTransition(contextAction act) throws ParseException,
			RemoteException, CreateException, NamingException, SQLException,
			JDOMException, DataErrorException, Exception {
		//incluye aciones tanto NEWOBJECT,SETOBJECT,DELOBJECT,CONFIRMOT
		System.out.println("Process onMessageObjectTransition "
				+ act.toString());
		Element res = transitionObject(act);
		if (res.getAttributeValue("ID_O") != null) {
			int ido = Integer.parseInt(res.getAttributeValue("ID_O"));
			String index = res.getAttributeValue("INDEX");
			if (index != null) {
				act.setRdn_ctx(index);
				act.setResultCode(dynagent.communication.message.NEW_INDEX);
				System.out.println("INDEX NO NULO");
			}
			act.setIDO_ctx(ido);
		}
		if (res.getAttributeValue("SUCCESS").equals("FALSE")) {
			act.setResultCode(Integer.parseInt(res
					.getAttributeValue("RESULT_CODE")));
			Iterator iRdn = res.getChildren("RDN").iterator();
			ArrayList rdns = new ArrayList();
			while (iRdn.hasNext()) {
				Element child = (Element) iRdn.next();
				rdns.add(new Integer(child.getAttributeValue("ID_O")));
			}
			if (rdns.size() > 0)
				act.addAllPropertie(properties.id, rdns);
		}
		act.setActionWay(false);
		sendMessage(true, act.toHeaderString(), getNextMsgID());
		if (act.getSuccess())
			subNotificaCreacionParte(act);
	}

	void subNotificaCreacionParte(contextAction pp) throws ParseException,
			JMSException {
		instance atom = pp.getInstance();
		if (atom == null)
			return;

		Iterator itr = atom.getEstructIterator(true);
		while (itr.hasNext()) {
			ObjectProperty rel = (ObjectProperty) itr.next();
			// instance parte= rel.getRelationInstance();
			//instance parte = new instance(null, 0, 0, 0);
			instance parte = new instance(0, 0);
			//if (!parte.justCreated())
			//	continue;
			contextAction thisPP = (contextAction) pp.clone();
			thisPP.setInstance((instance) parte.clone());
			thisPP.setActionWay(false);
			thisPP.setIDO_ctx(parte.getIDO());
			thisPP.setTO_ctx(parte.getIdTo());
			thisPP.setContext(rel.getIdoRel());
			thisPP.setRdn_ctx(parte.getRdn());
//			thisPP.setIsRootContext(false);
			//TODO modificar esta parte
			Iterator itr2 = parte.getAllPropertyIterator(0);
			while (itr2.hasNext()) {
				DataProperty p = (DataProperty)itr2.next();
				if(p.getIdProp()==helperConstant.TAPOS_FECHA_EXE_TASK) {
					if (p.getValueList().get(0) instanceof FloatValue) {
						FloatValue dv = (FloatValue)p.getValueList().get(0);
						thisPP.setExeDate(Integer.parseInt(dv.getValueMin()));
					}
				}
			}
			attribute att = parte.getAttribute(helperConstant.TAPOS_FECHA_EXE_TASK, 0);
			if (att != null)
				thisPP.setExeDate(((Integer) att.getValue()).intValue());

			sendMessage(true, thisPP.toHeaderString(), getNextMsgID());
			subNotificaCreacionParte(thisPP);
		}
	}*/

	/*void onMessageApropiateTran(owningAction pp, boolean newTask)
			throws SQLException, JDOMException, RemoteException,
			CreateException, NamingException, DataErrorException, EJBException {
		apropiateTran(pp.getCurrProcess(), pp.getProcessType(), newTask, pp
				.getTaskType(), pp.getCurrTask(), pp.getUser(),
				pp.getUserRol(), pp.getCurrTaskTrans(), pp.getCurrTaskState(),
				pp.getExeDate());
	}

	void onMessageGrowOwLevel(owningAction pp) throws NamingException,
			SQLException {
		String sql = "DELETE FROM S_PREASIG WHERE ID_CURR_TASK="
				+ pp.getCurrTask();
		DbExecUpdate(sql, m_pool.getProcessConn());
		sql = "UPDATE S_CURRENT_TASK SET OWNER='"
				+ pp.getUser().replaceAll("'", "''") + "'" + ",ASIGNED_ROL="
				+ pp.getUserRol() + " WHERE ID_CURRENT_TASK="
				+ pp.getCurrTask();
		DbExecUpdate(sql, m_pool.getProcessConn());
		logProcess(pp, "GROWN");
	}

	void onMessageDecreaseOwLevel(owningAction pp) throws SQLException,
			NamingException, RemoteException, CreateException, NamingException,
			DataErrorException {
		preasignTran(pp.getCurrProcess(), pp.getProcessType(), pp);
		logProcess(pp, "DECREASE");
	}

	void onMessagePreasignTran(owningAction pp, boolean sendConfirm)
			throws RemoteException, CreateException, NamingException,
			SQLException, JMSException, DataErrorException {
		preasignTran(pp.getCurrProcess(), pp.getProcessType(), pp);
		if (sendConfirm) {
			pp.setActionWay(false);
			sendMessage(true, pp.toString(), getNextMsgID());
		}
	}*/

	/*String getNextMsgID() {

		if (jmsTranIDCount++ > 0)
			return "P:" + jmsTranID + ":" + jmsTranIDCount;
		else
			return "P:" + jmsTranID;
	}*/

	/*void endProcess(flowAction pp) {
		try {
			DbExecUpdate("EXEC delete_current_pro " + pp.getCurrProcess(),
					m_pool.getProcessConn());
			logProcess(pp, "END_PRO");
		} catch (SQLException ex) {
			error("PROCESOS, eliminando proceso: " + ex.getMessage(), ex);
		} catch (NamingException ex) {
			error("PROCESOS, eliminando proceso: " + ex.getMessage(), ex);
		}
	}*/

	/*void logProcess(flowAction pp, String action) throws SQLException,
			NamingException {
		String sql = "INSERT INTO LOG_PROCESS(fecha,ID_CURRENT_PRO,"
				+ "ID_T_PRO,ID_CURRENT_TASK,TRANS,TASK_STATE,ID_T_TASK,ID_USER,LOG_ACTION)"
				+ " VALUES(GETDATE()," + pp.getCurrProcess() + ","
				+ pp.getProcessType() + "," + pp.getCurrTask() + ","
				+ pp.getCurrTaskTrans() + "," + pp.getCurrTaskState() + ","
				+ pp.getTaskType() + ",'" + pp.getUser().replaceAll("'", "''")
				+ "','" + action + "')";
		DbExecUpdate(sql, m_pool.getProcessConn());
	}

	int transition(flowAction pp) throws SQLException, NamingException,
			EJBException {
		int newSt = -1;
		return transition(pp.getCurrProcess(), pp.getProcessType(), pp
				.getUser(), pp.getCurrTaskTrans(), pp.getCurrTask(), pp
				.getTaskType(), newSt);
	}

	String exeTranAction(flowAction pp) throws SQLException, JDOMException,
			NamingException, ParseException, DataErrorException, Exception {
		//int empresa = m_IS.getUserBusiness(pp.getUser());
		String otRes = null;
		ArrayList response = procesaAccion(pp.getCurrProcess(), pp
				.getProcessType(), pp);
		if (response != null) {
			for (int i = 0; i < response.size(); i++) {
				if (i == 0)
					otRes = (String) response.get(0);
				sendMessage(true, (String) response.get(i), getNextMsgID());
			}
		}
		return otRes;
	}*/

	/*boolean discriminate(Element instance, Element test, metaData md)
			throws ParseException {
		Iterator iAva = test.getChildren("AVA").iterator();
		while (iAva.hasNext()) {
			Element ava = (Element) iAva.next();
			Integer tapos = new Integer(ava.getAttributeValue("TA_POS"));
			Element insAt = jdomParser.findElementByAt(instance
					.getChild("ATRIBUTOS"), "ITEM", "TA_POS", tapos.toString(),
					false);
			if (insAt == null)
				return false;
			int tm = md.getID_TM(tapos);
			String val = null;
			if (insAt.getAttributeValue("VALUE") != null)
				val = insAt.getAttributeValue("VALUE");
			else if (insAt.getText() != null)
				val = insAt.getText();
			else
				return false;

			if (ava.getAttributeValue("OP").equals("="))
				if (!helperConstant.equals(tm, ava.getAttributeValue("VALUE"),
						val))
					return false;
		}
		return true;
	}*/

	/*String asignaUser(String rol, int taskType, int tran, int policy)
			throws SQLException, NamingException {
		Statement st = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT DISTINCT LOGIN "
					+ " FROM Usuarios U WITH(NOLOCK) INNER JOIN TRANS_PERMISOS P WITH(NOLOCK) ON U.rol= P.rol INNER JOIN"
					+ " TASK_TRANSITION TT WITH(NOLOCK) ON P.ID_TRAN= TT.ID_TRAN INNER JOIN S_TASKS T WITH(NOLOCK)"
					+ " ON TT.ID_TASK= T.ID_TASK WHERE U.ROL='" + rol
					+ "' AND T.ID_TASK =" + taskType + " AND TT.ID_TRAN = "
					+ tran;
			st = m_pool.getProcessConn().createStatement(
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(sql);
			if (!rs.next())
				error("PROCESS, asignando usuario, error no hay ningun usario",
						null);
			if (policy == helperConstant.RAMDOM_POLICY) {
				rs.last();
				int size = rs.getFetchSize();
				//System.out.println("ASIGNANDO USER, size:"+size);
				if (size == 1)
					return rs.getString(1);
				Random rd = new Random();
				int row = (int) ((size - 1) * rd.nextDouble());
				//System.out.println("ASIGNANDO USER, row:"+row);
				rs.first();
				rs.relative(row);
				String user = rs.getString(1);
				return user;
			}
		} finally {
			if (st != null)
				st.close();
		}
		return null;
	}*/

//} // InstanceEJB
