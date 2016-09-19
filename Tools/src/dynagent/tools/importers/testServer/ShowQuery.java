package dynagent.tools.importers.testServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.knowledge.selectData;
import dynagent.common.communication.queryData;
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
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.QueryService;
import dynagent.tools.importers.Connect;

public class ShowQuery {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void showQuery(String filter, String gestor){
		Element query = null;
		try{
			BufferedReader in = new BufferedReader(new FileReader(filter)); 
			String dataS="", buff="";
			while(buff!= null){
				dataS+=buff;
				buff=in.readLine();
			}
			query = jdomParser.readXML(dataS).getRootElement();

			System.out.println(jdomParser.returnXML(query));
			DataBaseMap dataBaseMap = new DataBaseMap(fcdb, false);
			QueryService qs = new QueryService(fcdb, m_IS.getIk(), dataBaseMap);
			
			//Necesario si solo se quiere devolver la query
			qs.dbFindQuery(query,true);
			
			//Necesario si se quiere devolver el selectData
			//selectData sd = qs.QuerySD(query, queryData.MODE_ROOT);
			//System.out.println(sd + "\n");
			
		}catch(JDOMException e){
			System.out.println("JDOMException:"+e.getMessage());
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

	public void start(int business, String gestor, String databaseIP) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException {
		fcdb = new FactoryConnectionDB(business, true, databaseIP, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(fcdb, m_IS);
	}

	public static void main(String[] args) {
		try{			
			ShowQuery showQ = new ShowQuery();
			String databaseIP = args[0];
			int business = Integer.parseInt(args[1]);
			String filter = args[2];
			String gestor = args[3];
			showQ.start(business, gestor, databaseIP);
			showQ.showQuery(filter, gestor);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
