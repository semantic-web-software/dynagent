package dynagent.server.reports.clasificator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class ReportClasificator {
	
	private FactoryConnectionDB fcdb;
	
	private String pathUserFiles;
	
	private class ReportValues extends Object {
		private String name;
		private String file;//Fichero que se le muesta al usuario. Puede tener cambios de diseño hechos por el
		private String originalFile;//Fichero original importado, sin cambios de diseño del usuario
		private int tableId;

		public ReportValues(String name, String file, String originalFile, int tableId) {
			this.name = name;
			this.file = file;
			this.originalFile = originalFile;
			this.tableId = tableId;
		}

		public String getName() {
			return name;
		}
		public String getFile() {
			return file;
		}
		public String getOriginalFile() {
			return originalFile;
		}
		public int getTableId() {
			return tableId;
		}
	}
	
	public ReportClasificator(FactoryConnectionDB fcdb) {
		this.fcdb = fcdb;
	}
	public void setPathUserFiles(String pathUserFiles) {
		this.pathUserFiles = pathUserFiles;
	}
	
	public void startClasificator() throws SQLException, NamingException, IOException, JDOMException {
		ArrayList<ReportValues> reportsValues = getReportValues();
		HashMap<Integer,ArrayList<ReportValues>> subreportsValuesTableId = getSubreportValues();
		
		Iterator<ReportValues> it = reportsValues.iterator();
		while (it.hasNext()) {
			ReportValues reportValues = it.next();
			int tableIdReport = reportValues.getTableId();
			try{
			clasificator(tableIdReport, reportValues, subreportsValuesTableId.get(tableIdReport));
			}catch(java.io.FileNotFoundException fne){
					continue;
			}
		}
	}
	
	private void clasificator(int tableIdReport, ReportValues reportValues, ArrayList<ReportValues> subreportsValues) throws IOException, JDOMException, SQLException, NamingException {
		HashSet<String> subreportsRemoved = new HashSet<String>();
		HashSet<String> variablesOfSubreportsRemoved = new HashSet<String>();
		String reportName = reportValues.getName();
		if (subreportsValues!=null) {
			Iterator<ReportValues> it2 = subreportsValues.iterator();
			while (it2.hasNext()) {
				ReportValues subreportValues = it2.next();
				String subreportName = subreportValues.getName();
				System.out.println("*****************************************************************");
				System.out.println("INICIO SUBREPORT " + subreportName + " DE " + reportName);
				System.out.println("*****************************************************************");
				String subreportOriginalFile = subreportValues.getOriginalFile();
				String subreportFile = subreportValues.getFile();
				Element designSubReport = readXML(subreportOriginalFile);
				//System.out.println("designSubReport " + jdomParser.returnXML(designSubReport));
				//1er analizar y actualizar subreports
				ReportUpdater subreportUpdater = new ReportUpdater();
				Element newDesignSubReport = subreportUpdater.startUpdater(fcdb, designSubReport);
				if (newDesignSubReport==null)
					subreportsRemoved.add(designSubReport.getAttributeValue(ConstantsReport.name));
				else {
					//System.out.println("newDesignSubReport " + jdomParser.returnXML(newDesignSubReport));
					writeXML(newDesignSubReport, subreportFile);
					variablesOfSubreportsRemoved.addAll(subreportUpdater.getVariablesToRemove());
				}
				System.out.println("*****************************************************************");
				System.out.println("FIN SUBREPORT " + subreportName + " DE " + reportName);
				System.out.println("*****************************************************************");
			}
		}
		String reportOriginalFile = reportValues.getOriginalFile();
		String reportFile = reportValues.getFile();
		System.out.println("*****************************************************************");
		System.out.println("INICIO REPORT " + reportName);
		System.out.println("*****************************************************************");
		Element designReport = readXML(reportOriginalFile);
		//System.out.println("designReport " + jdomParser.returnXML(designReport));
		//1er analizar y actualizar subreports
		ReportUpdater reportUpdater = new ReportUpdater(subreportsRemoved, variablesOfSubreportsRemoved);
		Element newDesignReport = reportUpdater.startUpdater(fcdb, designReport);
		if (newDesignReport!=null) {
			//System.out.println("newDesignReport " + jdomParser.returnXML(newDesignReport));
			writeXML(newDesignReport, reportFile);
		} else {
			System.out.println("EL INFORME " + reportName + " NO SE HA IMPORTADO");
		}
		System.out.println("*****************************************************************");
		System.out.println("FIN REPORT " + reportName);			
		System.out.println("*****************************************************************");
	}
	
	private void writeXML(Element newDesignReport, String fileReport) throws JDOMException, IOException {
		if (pathUserFiles==null) {
			String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
			pathUserFiles = serverHomeDir + "\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\" + Constants.folderUserFiles + "/" + fcdb.getBusiness();
		}
		String pathDesign = pathUserFiles + "/" + fileReport;
		File f = new File(pathDesign);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream fow = new FileOutputStream(f);
		OutputStreamWriter fw = new OutputStreamWriter(fow, "ISO-8859-1");
		String newDesignReportISOStr = jdomParser.returnXML(newDesignReport);
		/*convert from UTF-8 to ISO-8859-1*/
		String newDesignReportStr = new String(newDesignReportISOStr.getBytes("UTF-8"), "ISO-8859-1");
		fw.write(newDesignReportStr);
		fw.close();
	}
	
	private Element readXML(String fileReport) throws IOException, JDOMException {
		if (pathUserFiles==null) {
			String serverHomeDir = System.getProperty(org.jboss.system.server.ServerConfig.SERVER_HOME_DIR);
			pathUserFiles = serverHomeDir + "\\deploy\\jbossweb-tomcat55.sar\\ROOT.war\\dyna\\" + Constants.folderUserFiles + "/" + fcdb.getBusiness();
		}
		String pathDesign = pathUserFiles + "/" + fileReport;
		String designISO = Auxiliar.readFile(pathDesign);
		/*convert to UTF-8*/
		String design = new String(designISO.getBytes(), "UTF-8");
		Element designElem = jdomParser.readXML(design).getRootElement();
		return designElem;
	}
	
	private ArrayList<ReportValues> getReportValues() throws SQLException, NamingException {
		ArrayList<ReportValues> reportsValues = new ArrayList<ReportValues>();
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		String sql = "select rdn," + cB + "tableId" + cE + ", archivo, archivo_original from informe";
		ConnectionDB con = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String name = rs.getString(1);
				int tableId = rs.getInt(2);
				String file = rs.getString(3);
				String originalFile = rs.getString(4);
				ReportValues reportValues = new ReportValues(name, file, originalFile, tableId);
				reportsValues.add(reportValues);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return reportsValues;
	}
	private HashMap<Integer,ArrayList<ReportValues>> getSubreportValues() throws SQLException, NamingException {
		HashMap<Integer,ArrayList<ReportValues>> subReportsValuesTableId = new HashMap<Integer, ArrayList<ReportValues>>();
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		String sql = "select rdn, archivo, archivo_original, " + cB + "informeId" + cE + " from subinforme";
		ConnectionDB con = fcdb.createConnection(true);
		Statement st = null;
		ResultSet rs = null;		
		try {
			st = con.getBusinessConn().createStatement();
			System.out.println(sql);
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String name = rs.getString(1);
				String file = rs.getString(2);
				String originalFile = rs.getString(3);
				int tableIdReport = rs.getInt(4);
				ArrayList<ReportValues> aRV = subReportsValuesTableId.get(tableIdReport);
				if (aRV==null) {
					aRV = new ArrayList<ReportValues>();
					subReportsValuesTableId.put(tableIdReport, aRV);
				}
				ReportValues reportValues = new ReportValues(name, file, originalFile, tableIdReport);
				aRV.add(reportValues);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return subReportsValuesTableId;
	}

}