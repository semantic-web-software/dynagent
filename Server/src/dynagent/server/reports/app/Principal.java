package dynagent.server.reports.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingException;

import net.sf.jasperreports.engine.JRException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.IdReport;
import dynagent.server.ejb.jdomParser;
import dynagent.server.services.QueryReportParser;
import dynagent.server.services.ReportService;
import dynagent.server.services.querys.QueryConstants;
import dynagent.server.services.reports.EditReports;
import dynagent.server.services.reports.GenerateJRXML;
import dynagent.server.services.reports.ParQuery;

public class Principal {
	
	private FactoryConnectionDB fcdb = null;
	private int idMaster;
	private boolean nuevo = false;
	
	private void start(int business, boolean nuevo) {
		fcdb = new FactoryConnectionDB(business, true, null);
		this.nuevo = nuevo;
	}

	private Element getQueryDB(int idtoUTask) throws NamingException, SQLException, JDOMException, ParseException {
		Element query = null;
		//select para obtener a partir del idto la query
		String sql = "SELECT QUERY FROM S_REPORT WHERE ID_TO=" + idtoUTask;
		ConnectionDB con = null; 
		Statement st = null;
		ResultSet rs = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String queryStr = rs.getString(1);
				if (queryStr!=null)
					query = jdomParser.readXML(queryStr).getRootElement();
			} else {
				throw new ParseException("El idto " + idtoUTask + " no existe en base de datos",0);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con != null)
				con.close();
		}
		return query;
	}
	
//	TODO con modelos incrementales se podra usar aqui el adaptador
	private void adaptaQuery(Element query, int idtoUTask) throws ParseException {
		QueryReportParser qrp = new QueryReportParser(fcdb);
		qrp.parserIDs(query);
		//update en bd
		/*ConnectionDB con2 = null; 
		Statement st2 = null;
		try {
			String sql2 = "UPDATE S_REPORT SET QUERY='" + jdomParser.returnXML(query).replaceAll("'", "''") + "' " +
					"WHERE ID_TO=" + idtoUTask;
			con2 = fcdb.createConnection(true); 
			st2 = con2.getBusinessConn().createStatement();
			st2.executeUpdate(sql2);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch (NamingException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (st2 != null)
					st2.close();
				if (con2 != null)
					con2.close();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}*/
	}
	
	private Map<Integer, String> reportsNew(Element query, int business, int idtoUTask) 
			throws ParseException, JDOMException, SQLException, NamingException, IOException {
		Map<Integer, String> jrxmls = null;
		HashMap<Integer,ParQuery> hrq = null;
		String sql = "DELETE FROM S_SUBREPORT WHERE ID_TO=" + idtoUTask;
		//borrar subreports de base de datos
		ConnectionDB con = null; 
		Statement st = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			st.executeUpdate(sql);
		} finally {
			if (st != null)
				st.close();
			if (con != null)
				con.close();
		}
		nuevo = true;
		ReportService rs = new ReportService(fcdb, null);
		adaptaQuery(query, idtoUTask);
		idMaster = getIdMaster(query);
		hrq = rs.Report(query, idMaster, false);
		jrxmls = GenerateJRXML.make(hrq, idtoUTask, idMaster);
		return jrxmls;
	}
	
	private int getIdMaster(Element query) {
		Integer idMaster = null;
		int children = query.getChildren().size();
		if (children==1) {
			Element union = query.getChild(QueryConstants.UNION);
			if (union==null) {
				Element structure = query.getChild(QueryConstants.STRUCTURE);
				Element presentation = structure.getChild(QueryConstants.PRESENTATION);
				Element view = presentation.getChild(QueryConstants.VIEW);
				String index = view.getAttributeValue(QueryConstants.INDEX);
				if (index!=null)
					idMaster = Integer.parseInt(index);
				else
					idMaster = Integer.parseInt(structure.getAttributeValue(QueryConstants.INDEX));
			} else
				idMaster = Integer.parseInt(union.getAttributeValue(QueryConstants.INDEX));
		} else
			idMaster = 1;
		return idMaster;
	}
	
	private ArrayList<IdReport> getReports(Element query, int business, int idtoUTask, String path) 
			throws ParseException, JRException, IOException, InterruptedException, JDOMException, SQLException, NamingException {
		Map<Integer, String> jrxmls = null;
		ArrayList<IdReport> reports = new ArrayList<IdReport>();
		if (nuevo)
			jrxmls = reportsNew(query, business, idtoUTask);
		else
			//mirar en bd si existe ya el diseño
			//si existe crear fichero con el contenido del String de la bd
			//si no existe crea uno nuevo
			jrxmls = getJRXMLDB(query, business, idtoUTask);
		
		EditReports.edit(jrxmls, path);
	
		Iterator<Integer> it = jrxmls.keySet().iterator();
		while (it.hasNext()){
			int key = it.next();
			//aqui hay una ruta -> "reports\\tmp\\master.jrxml"
			File f = new File(jrxmls.get(key).toString());
			f.deleteOnExit();
			FileReader fr = new FileReader(f);
			BufferedReader in = new BufferedReader(fr);
			String buff = "", report = "";
			while(buff != null){
				report += buff;
				buff = in.readLine();
			}
			in.close();
			fr.close();
			reports.add(new IdReport(key,report));
		}
		return reports;
	}
	
	private Map<Integer, String> getJRXMLDB(Element query, int business, int idtoUTask) 
			throws ParseException, SQLException, NamingException, IOException, JDOMException {
		Map<Integer, String> jrxmls = new HashMap<Integer,String>();
		//select para obtener a partir del idto la query
		String sql = "SELECT ID,REPORT FROM S_REPORT WHERE ID_TO=" + idtoUTask;
		ConnectionDB con = null;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con2 = null; 
		Statement st2 = null;
		ResultSet rs2 = null;
		try {
			con = fcdb.createConnection(true); 
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				idMaster = rs.getInt(1);
				String reportStr = rs.getString(2);
				if (reportStr!=null) {
					GenerateJRXML.putMaster(jrxmls, idtoUTask, reportStr, idMaster);
					String sql2 = "SELECT SUBREPORT,ID FROM S_SUBREPORT WHERE ID_TO=" + idtoUTask;
					con2 = fcdb.createConnection(true); 
					st2 = con2.getBusinessConn().createStatement();
					rs2 = st2.executeQuery(sql2);
					while (rs2.next()) {
						String subReportStr = rs2.getString(1);
						GenerateJRXML.put(jrxmls, idtoUTask, subReportStr, rs2.getInt(2));
					}
				} else
					jrxmls = reportsNew(query, business, idtoUTask);
			} else
				jrxmls = reportsNew(query, business, idtoUTask);
		} finally {
			if (rs != null)
				rs.close();
			if (st2 != null)
				st2.close();
			if (con2 != null)
				con2.close();
			if (rs2 != null)
				rs2.close();
			if (st != null)
				st.close();
			if (con != null)
				con.close();
		}
		return jrxmls;
	}

	private void putReportDB(Element query, int idtoUTask, ArrayList<IdReport> reports) 
			throws SQLException, NamingException {
		//insert en bd para actualizar el diseño
		for (int i=0;i<reports.size();i++) {
			IdReport idR = reports.get(i);
			int id = idR.getId();
			String report = idR.getReport();
			String sql = null;
			//int idMaster = getIdMaster(query);
			if (id==idMaster) {
				sql = "UPDATE S_REPORT SET REPORT='" + report + "', ID=" + id + " WHERE ID_TO=" + idtoUTask;
			} else {
				if (nuevo)
					sql = "INSERT INTO S_SUBREPORT(ID_TO,ID,SUBREPORT) VALUES(" + idtoUTask + "," + id + ",'" + report + "')";
				else {
					//si no es nuevo comprobar si esta o no en la tabla
					String sqlComprobac = "SELECT count(*) FROM S_SUBREPORT WHERE ID_TO=" + idtoUTask + " AND ID=" + id;
					ConnectionDB con = null; 
					Statement st = null;
					ResultSet rs = null;
					try {
						con = fcdb.createConnection(true); 
						st = con.getBusinessConn().createStatement();
						rs = st.executeQuery(sqlComprobac);
						if (rs.next()) {
							int count = rs.getInt(1);
							if (count==0)
								sql = "INSERT INTO S_SUBREPORT(ID_TO,ID,SUBREPORT) VALUES(" + idtoUTask + "," + id + ",'" + report + "')";
							else
								sql = "UPDATE S_SUBREPORT SET SUBREPORT='" + report + "' WHERE ID_TO=" + idtoUTask + " AND ID=" + id;
						}
					} finally {
						if (rs!=null)
							rs.close();
						if (st != null)
							st.close();
						if (con != null)
							con.close();
					}
				}
			}
			ConnectionDB con = null; 
			Statement st = null;
			try {
				con = fcdb.createConnection(true); 
				st = con.getBusinessConn().createStatement();
				st.executeUpdate(sql);
			} finally {
				if (st != null)
					st.close();
				if (con != null)
					con.close();
			}
		}
	}
	
	public static void main(String args[]) throws JRException, IOException, InterruptedException, JDOMException, SQLException, 
			NamingException, ParseException {
		int business = Integer.parseInt(args[0]);
		int idtoUTask = Integer.parseInt(args[1]);
		boolean nuevo = Boolean.parseBoolean(args[2]);
		//path por defecto es "C:\\Archivos de programa\\JasperSoft\\iReport-2.0.0\\iReport.exe"
		String path = args[3];
		if (path==null)
			path="C:\\Archivos de programa\\JasperSoft\\iReport-2.0.0\\iReport.exe";
		//path="C:\\Documents and Settings\\Macarena\\Mis documentos\\PROGRAMAS\\JasperSoft\\iReport-2.0.0\\iReport.exe";
		Principal principal = new Principal();
		System.out.println("dbg0");
		principal.start(business, nuevo);
		Element query = principal.getQueryDB(idtoUTask);
		if (query!=null) {
			System.out.println("dbg1");
			ArrayList<IdReport> reports;
			reports = principal.getReports(query, business, idtoUTask, path);
			System.out.println("dbg2");
			principal.putReportDB(query, idtoUTask, reports);
			System.out.println("dbg3");
		}
	}
}
