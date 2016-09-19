package dynagent.server.test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import org.jdom.Element;

import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.reports.MakeQueryWhere;

public class Test9 {
	
	private InstanceService m_IS=null;
	private FactoryConnectionDB fcdb = null;
	
	public void test(String user, String name, String path){
		try{
			Integer idto = null;
			Element queryWhere = null;

			String sql = "SELECT IDTO FROM CLASES WHERE NAME='" + name + "'";
			ConnectionDB con = null; 
			Statement st = null;
			ResultSet rs = null;
			try {
				con = fcdb.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next()) {
					idto = rs.getInt(1);
				} else {
					throw new DataErrorException("No existe en base de datos un idto para la query " + name);
				}
				//select para obtener a partir del idto la query
				sql = "SELECT MAP FROM S_Report WHERE ID_TO=" + idto;
				rs = st.executeQuery(sql);
				if (rs.next()) {
					Element map = jdomParser.readXML(rs.getString(1)).getRootElement();
					if (!map.getChildren().isEmpty()){
						MakeQueryWhere mqw = new MakeQueryWhere(m_IS);
						queryWhere=mqw.makeQWhereWithParamas(idto,map);
					}
					//Adaptador adapt = new Adaptador(fcdb);
//					QueryReportParser qrp = new QueryReportParser(fcdb);
//					qrp.parserIDs(queryWhere);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			} catch (NamingException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con != null)
						con.close();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
			if (queryWhere==null)
				System.out.println("prWhere vacio, no hay restriccines añadidas");
			else
				System.out.println("prWhere " + jdomParser.returnXML(queryWhere));
			String pathFile = m_IS.report(queryWhere, user, idto, path);
		}catch(Exception e){
			System.out.println("Exception:"+e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void start(int business, String gestor, String databaseIP) {
		fcdb = new FactoryConnectionDB(business, true, null, gestor);
		m_IS = new InstanceService(fcdb, null, false);
		Connect.connectRuler(databaseIP, fcdb, m_IS);
	}
	
	public static void main(String[] args) {
		try{			
			Test9 test9 = new Test9();
			System.out.println("dbg0");
			int business = Integer.parseInt(args[0]);
			String databaseIP = args[1];
			String gestor = args[2];
			test9.start(business, gestor, databaseIP);
			String name = args[3];
//			int idto = Integer.parseInt(args[1]);
			String user = args[4];
			String path = args[5];
			test9.test(user, name, path);
			System.exit(0);
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
