package dynagent.server.reports.clasificator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashSet;

import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import dynagent.common.utils.jdomParser;
import dynagent.server.ejb.FactoryConnectionDB;

public class MainReport {
	
	/**Main provisional que recibe la ruta con los diseños originales.
	 * Primero analiza y actualiza el subreport, posteriormente el report.
	 * Crea nuevos ficheros en la ruta de destino sin hacer cambios a los originales.*/
	public static void main(String args[]) throws SQLException, IOException, JDOMException, NamingException {
		System.out.println("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip = null;
		String gestor = null;
		Integer port = null;
		Integer nbusiness = null;
		String snbusiness = null;
		String pathOriginDesign = null;
		String pathDestinationDesign = null;
		String namesSubreports = null;
		String nameReport = null;
		
		String id=null;
		for(int i=0;i<args.length;i++) {
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-ip")) {
					ip=args[i];
				} else if(id.equalsIgnoreCase("-bns")) {
					nbusiness=Integer.parseInt(args[i]);
					snbusiness= String.valueOf(nbusiness);
				} else if(id.equalsIgnoreCase("-gestor")) {
					gestor=args[i];
				} else if(id.equalsIgnoreCase("-port")) {
					port=Integer.parseInt(args[i]);
				} else if(id.equalsIgnoreCase("-pathOriginDesign")) {
					pathOriginDesign=args[i];
				} else if(id.equalsIgnoreCase("-pathDestinationDesign")) {
					pathDestinationDesign=args[i];
				} else if(id.equalsIgnoreCase("-namesSubreports")) {
					namesSubreports=args[i];
				} else if(id.equalsIgnoreCase("-nameReport")) {
					nameReport=args[i];
				}
			}
		}

		FactoryConnectionDB fcdb = setConnection(snbusiness, ip, gestor, port);
		
		HashSet<String> subreportsRemoved = new HashSet<String>();
		HashSet<String> variablesOfSubreportsRemoved = new HashSet<String>();
		
		if (namesSubreports!=null) {
			String[] namesSubreportsSpl = namesSubreports.split("#");
			for (int i=0;i<namesSubreportsSpl.length;i++) {
				String nameSubreport = namesSubreportsSpl[i];
				System.out.println("INICIO " + nameSubreport);
				Element designSubReport = readXML(pathOriginDesign, nameSubreport);
				//System.out.println("designSubReport " + jdomParser.returnXML(designSubReport));
				//1er analizar y actualizar subreports
				ReportUpdater subreportUpdater = new ReportUpdater();
				Element newDesignSubReport = subreportUpdater.startUpdater(fcdb, designSubReport);
				if (newDesignSubReport==null)
					subreportsRemoved.add(designSubReport.getAttributeValue(ConstantsReport.name));
				else {
					//System.out.println("newDesignSubReport " + jdomParser.returnXML(newDesignSubReport));
					writeXML(newDesignSubReport, pathDestinationDesign, nameSubreport);
					variablesOfSubreportsRemoved.addAll(subreportUpdater.getVariablesToRemove());
				}
				System.out.println("FIN " + nameSubreport);
			}
		}
		System.out.println("INICIO " + nameReport);
		Element designReport = readXML(pathOriginDesign, nameReport);
		//System.out.println("designReport " + jdomParser.returnXML(designReport));
		//1er analizar y actualizar subreports
		ReportUpdater reportUpdater = new ReportUpdater(subreportsRemoved, variablesOfSubreportsRemoved);
		Element newDesignReport = reportUpdater.startUpdater(fcdb, designReport);
		if (newDesignReport!=null) {
			//System.out.println("newDesignReport " + jdomParser.returnXML(newDesignReport));
			writeXML(newDesignReport, pathDestinationDesign, nameReport);
		} else {
			System.out.println("EL INFORME " + nameReport + " NO SE HA IMPORTADO");
		}
		System.out.println("FIN " + nameReport);
		
		fcdb.removeConnections();
		System.out.println("*****bye******");
		System.out.println("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	private static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor, int port) {
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		fcdb.setPort(port);
		return fcdb;
	}
	/**Lee el xml de diseño dada una ruta.*/
	private static Element readXML(String path, String name) throws JDOMException {
		Document xml = jdomParser.readXML(new File(path + name + ".jrxml"));
		return xml.getRootElement();
	}
	/**Escribe en un fichero un xml de diseño dada una ruta.*/
	private static void writeXML(Element xml, String path, String name) throws IOException {
		//String pathNoExtension = path.substring(0,path.length()-6);
		BufferedWriter bufferedWriter = null;
		try {
			XMLOutputter xmlOutputter = new XMLOutputter("\t", true, "UTF-8");
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(path + name + ".jrxml")), Charset.forName("UTF-8")));
			xmlOutputter.output(new Document(xml), bufferedWriter);
		} finally {
			if (bufferedWriter!=null)
				bufferedWriter.close();
		}
	}
}
