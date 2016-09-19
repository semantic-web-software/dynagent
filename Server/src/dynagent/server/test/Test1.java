package dynagent.server.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.queryData;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.utils.jdomParser;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.alias.IAlias;
import dynagent.ruleengine.src.xml.QueryXML;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.QueryService;

public class Test1 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(String filter, String gestor){
		Element query = null;
		queryData qd = null;
		try{
			BufferedReader in = new BufferedReader(new FileReader(filter)); 
			String dataS="", buff="";
			while(buff!= null){
				dataS+=buff;
				buff=in.readLine();
			}
			query = jdomParser.readXML(dataS).getRootElement();
			//monoStandConnection msc= new monoStandConnection(business,login,pwd);
			System.out.println("dbg1");
			query.setAttribute("USER","SYSTEM");

			System.out.println(jdomParser.returnXML(query));
			
			QueryService qs = new QueryService(fcdb, m_IS.getIk());
//			m_IS.Report(query, business);
			qd = qs.Query(query, queryData.MODE_ROW);
			System.out.println("qd " + qd);
//			m_IS.Query(query, business);
		}catch(SQLException e){
			System.out.println("SQLException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(JDOMException e){
			System.out.println("JDOMException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(NamingException e){
			System.out.println("NamingException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(FileNotFoundException e){
			System.out.println("FileNotFoundException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(IOException e){
			System.out.println("IOException:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}catch(Exception e){
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
		try {
			//pruebas parseo en queryData y QueryXML
			selectData sd = queryData.parse(query, qd.toString());
			System.out.println(sd.toString() + "\n");
			ArrayList<SelectQuery> select = new ArrayList<SelectQuery>();
			SelectQuery sq = new SelectQuery("1001",100, null, null);
			select.add(sq);
			SelectQuery sq2 = new SelectQuery("1041",107, null, null);
			select.add(sq2);
			SelectQuery sq3 = new SelectQuery("1041",108, null, null);
			select.add(sq3);
			SelectQuery sq4 = new SelectQuery("1001",400, null, null);
			select.add(sq4);
			SelectQuery sq5 = new SelectQuery("1001",400, null, null);
			select.add(sq5);
			SelectQuery sq6 = new SelectQuery("1003",21, null, null);
			select.add(sq6);
			SelectQuery sq7 = new SelectQuery("1003",100, null, null);
			select.add(sq7);
			SelectQuery sq8 = new SelectQuery("1004",100, null, null);
			select.add(sq8);
			SelectQuery sq9 = new SelectQuery("1010",2, null, null);
			select.add(sq9);
			SelectQuery sq10 = new SelectQuery("1010",100, null, null);
			select.add(sq10);
			
			Iterator iterador = sd.getIterator();
			while (iterador.hasNext()) {
				instance ins = (instance)iterador.next();
				IAlias alias = null;
				QueryXML qxml = new QueryXML(m_IS.getIk(), alias);
				try {
					qxml.setSelect(select);
					Element xml = qxml.toQueryXML(ins, null);
					System.out.println("\n\nXML generado de nuevo con QueryXML: \n" + jdomParser.returnXML(xml));
				} catch (JDOMException e) {
					e.printStackTrace();
				} catch (NotFoundException e) {
					e.printStackTrace();
				} catch (IncoherenceInMotorException e) {
					e.printStackTrace();
				} catch (IncompatibleValueException e) {
					e.printStackTrace();
				} catch (CardinalityExceedException e) {
					e.printStackTrace();
				} catch (SystemException e) {
					e.printStackTrace();
				} catch (RemoteSystemException e) {
					e.printStackTrace();
				} catch (CommunicationException e) {
					e.printStackTrace();
				} catch (InstanceLockedException e) {
					e.printStackTrace();
				} catch (ApplicationException e) {
					e.printStackTrace();
				} catch (OperationNotPermitedException e) {
					e.printStackTrace();
				}
			}
		} catch (DataErrorException e) {
			e.printStackTrace();
		}
	}

	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		fcdb = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(databaseIP, fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test1 test1 = new Test1();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String filter = args[1];
			String gestor = args[2];
			test1.start(business, gestor);
			test1.test(filter, gestor);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
