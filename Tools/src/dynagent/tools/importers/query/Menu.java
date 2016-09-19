package dynagent.tools.importers.query;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;

import javax.naming.NamingException;

import net.sf.jasperreports.engine.JRException;

import org.jdom.JDOMException;

import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.ConceptLogger;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.FactoryConnectionDB;

public class Menu {
	
	public static void main(String args[]) throws IOException, JDOMException, ParseException, JRException, InterruptedException, SQLException, NamingException {
		ConceptLogger.getLogger().cleanFile();
		ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip = args[0];
		String path = args[1];
		int nbusiness = Integer.parseInt(args[2]);
		String gestor = args[3];
		String snbusiness = String.valueOf(nbusiness);
		Integer port = Integer.parseInt(args[4]);
		
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, gestor);
		fcdb.setPort(port);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
	
		char opcion;
		Menu menu = new Menu();
	
		System.out.println("INFO:   El numero de empresa es=" + snbusiness);
		do {
			opcion = menu.dameOpcion();
			
			switch (opcion) {
			case 'S':
				System.out.println("Salir");
				break;
			case 'R':
				QUERYParser.importQuery(null,path,fcdb);
				break;
			case 'I':
				String queryR=Auxiliar.leeTexto("Introduzca el nombre de la query");
				queryR=queryR.toLowerCase();
				QUERYParser.importQuery(queryR,path,fcdb);
				break;
			case 'D':
				String query=Auxiliar.leeTexto("Introduzca el nombre de la query");
				query=query.toLowerCase();
				QUERYParser.delete(query);
				break;

			default:
				System.out.println("La opcion es incorreta");
				break;
			}
		} while (opcion != 'S');

		fcdb.removeConnections();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
	public char dameOpcion() {
		System.out
				.println("\n\n===========================================================================================");
		System.out
				.println("\n           DYNAGENT QUERY IMPORT          ");
		System.out.println("\n OPCIONES:");
		System.out.println("R.-Importar todas las Querys ");
		System.out.println("D.-Borrar Query ");
		System.out.println("I.-Importar una Query concreta");
		System.out.println("\nS.-SALIR");
		System.out
				.println("\n===========================================================================================");
		String texto = Auxiliar.leeTexto("SELECCIONE UNA OPCIÓN");
		char opcion;
		if (texto!=null)
			opcion = texto.charAt(0);
		else
			opcion='0';
		return opcion;
	}
	
	public static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		return fcdb;
	}

}
