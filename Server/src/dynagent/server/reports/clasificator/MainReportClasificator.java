package dynagent.server.reports.clasificator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.server.ejb.FactoryConnectionDB;

public class MainReportClasificator {
	
	/**Main provisional que recibe la ruta con los diseños originales.
	 * Primero analiza y actualiza el subreport, posteriormente el report.
	 * Crea nuevos ficheros en la ruta de destino sin hacer cambios a los originales.*/
	public static void main(String args[]) throws SQLException, IOException, JDOMException, NamingException {
		System.out.println("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip = null;
		String gestor = null;
		String pathImportReports = null;
		Integer port = null;
		Integer nbusiness = null;
		String snbusiness = null;
		
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
				} else if(id.equalsIgnoreCase("-pathImportReports")) {
					pathImportReports=args[i];
				}
			}
		}

		FactoryConnectionDB fcdb = setConnection(snbusiness, ip, gestor, port);
		
		ReportClasificator rClasificator = new ReportClasificator(fcdb);
		String pathUserFiles = pathImportReports + "/" + snbusiness;
		rClasificator.setPathUserFiles(pathUserFiles);
		rClasificator.startClasificator();
		
		fcdb.removeConnections();
		System.out.println("*****bye******");
		System.out.println("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
	private static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor, int port) {
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		fcdb.setPort(port);
		return fcdb;
	}
}
