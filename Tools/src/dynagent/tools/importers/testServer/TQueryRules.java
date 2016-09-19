package dynagent.tools.importers.testServer;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.naming.NamingException;

import org.jdom.CDATA;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.communication.IteratorQuery;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.tools.importers.Connect;

public class TQueryRules {

	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException {
		String sql = "select f.rdn, f.serie, c.rdn, c.idto, c.nombre from factura_a_cliente as f " + 
			"left join v_cliente as c on(f.\"clienteCLIENTE_PARTICULAR\"=c.\"tableId\")";
		List<List<String>> rows = m_IS.queryRules(sql,false);
		
		System.out.println("RESULTADO_LISTA");
		Iterator it = rows.iterator();
		while (it.hasNext()) {
			List<String> columns = (List<String>)it.next();
			Iterator it2 = columns.iterator();
			while (it2.hasNext()) {
				String value = (String)it2.next();
				System.out.println(value);
			}
		}
		Element root = new Element("CONTENT");
		it = rows.iterator();
        while(it.hasNext()) {
        	Element row = new Element("R");
        	root.addContent(row);
        	List<String> columns = (List<String>)it.next();
            Iterator it2 = columns.iterator();
            while(it2.hasNext()) {
            	Element column = new Element("C");
            	row.addContent(column);
            	String value = (String)it2.next();
           		column.addContent(new CDATA(value));
            }
        }
		System.out.println("RESULTADO_ELEMENT");
        try {
			System.out.println(jdomParser.returnXML(root));
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		IteratorQuery iq = new IteratorQuery(root);
		
		System.out.println("RESULTADO_ITERATOR");
		while (iq.hasNextRow()) {
			iq.nextRow();
			String value = iq.nextColumnValue();
			System.out.println("f.rdn " + value);
			value = iq.nextColumnValue();
			System.out.println("f.serie " + value);
			value = iq.nextColumnValue();
			System.out.println("c.rdn " + value);
			value = iq.nextColumnValue();
			System.out.println("c.idto " + value);
			value = iq.nextColumnValue();
			System.out.println("c.nombre " + value);
		}
	}
	
	public void start(int business, String gestor, String databaseIP, Integer port) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
		fcdb.setPort(port);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			TQueryRules test = new TQueryRules();
			System.out.println("dbg0");
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String gestor = args[2];
			System.out.println("dbg1");
			int port = Integer.parseInt(args[3]);
			test.start(business, gestor, databaseIP, port);
			System.out.println("dbg2");
			test.test();
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
