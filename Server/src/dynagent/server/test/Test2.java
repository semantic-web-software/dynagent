package dynagent.server.test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.ReportService;

public class Test2 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(int business, String filter){
		try{
			BufferedReader in = new BufferedReader(new FileReader(filter)); 
			String dataS="", buff="";
			while(buff!= null){
				dataS+=buff;
				buff=in.readLine();
			}
			Element query = jdomParser.readXML(dataS).getRootElement();
			System.out.println("dbg1");
			query.setAttribute("USER","SYSTEM");

			System.out.println(jdomParser.returnXML(query));
			
			ReportService rs = new ReportService(fcdb, m_IS.getIk(), true, true);
			rs.Report(query, "1");
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
	}
	
	public void start(int business, String gestor) {
		String databaseIP = "localhost";
		fcdb = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(databaseIP, fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			Test2 test2 = new Test2();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String filter = args[1];
			String gestor = args[2];
			test2.start(business, gestor);
			test2.test(business,filter);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
