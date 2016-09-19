package dynagent.tools.importers.report;

import java.sql.SQLException;
import java.util.Calendar;

import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.ConceptLogger;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.reports.old.MakeJrxmls;
import dynagent.server.services.reports.old.REPORTParser;

public class Menu {
	public static void main(String args[]) throws SQLException /*throws IOException, JDOMException, ParseException, JRException, InterruptedException, SQLException, NamingException, NotFoundException, IncoherenceInMotorException */{
		ConceptLogger.getLogger().cleanFile();
		ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip=args[0];
		String path = args[1];
		String pathIreport=args[3];
		int nbusiness = Integer.parseInt(args[2]);
		String gestor = args[4];
		String nameFileConfiguration = args[5];

		String snbusiness=String.valueOf(nbusiness);
		/*String snbusiness = new Integer(nbusiness).toString();
		resp=Auxiliar.leeTexto("\n  IP="+ip+"   \n Desea que numero_empresa="+snbusiness+"?");
		if(!resp.equalsIgnoreCase("S")&&!resp.equalsIgnoreCase("SI")){
			
		}*/
		/*resp=Auxiliar.leeTexto("POR DEFECTO nbusiness="+nbusiness+"   userRol="+suserRol+"  user="+user+"  ¿DESEA MODIFICARLOS(S/N)?");
		if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
			user=Auxiliar.leeTexto("Introduzca el nombre de usuario");
			suserRol=Auxiliar.leeTexto("Introduzca el userRol");
			susertask=Auxiliar.leeTexto("Introduzca la usertask");
			
			
			do {
				snbusiness = Auxiliar.leeTexto("Introduzca el número de empresa con el que desea trabajar");
			} while (!Auxiliar.hasIntValue(snbusiness));
			nbusiness = new Integer(snbusiness);
			//ip = Auxiliar.leeTexto("Introduzca la IP de la Base de datos: ");
		
		}*/
		
		Integer port = null;
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, gestor);
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			port = Integer.parseInt(args[6]);
			if (port!=3306)
				fcdb.setPort(port);
		}
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
	
		char opcion;
		Menu menu = new Menu();
	
		//System.out.println("INFO:   El numero de empresa es=" + snbusiness);
		String[] str = path.split("/");
		String cliente = str[str.length-1];
		do {
			opcion = menu.dameOpcion(snbusiness, gestor, cliente, port, nameFileConfiguration);
			try {
				switch (opcion) {
				case 'S':
					System.out.println("Salir");
	
					break;
				case 'R':
					
					REPORTParser.importReports(nameFileConfiguration,null,path,fcdb);
	
					break;
				case 'I':
					String queryR=Auxiliar.leeTexto("Introduzca el nombre de la query");
					queryR=queryR.toLowerCase();
					
					
					REPORTParser.importReports(nameFileConfiguration, queryR, path,fcdb);
	
					break;
				case 'M':
					
					MakeJrxmls.createDesign(nbusiness,fcdb, path,pathIreport);
	
					break;
				case 'D':
					String query=Auxiliar.leeTexto("Introduzca el nombre de la query");
					query=query.toLowerCase();
					String respR=Auxiliar.leeTexto("¿DESEA ADEMÁS BORRAR LA RESERVA DE ID EN CLASES/PROPERTIES?");
					REPORTParser.delete(query,respR.equalsIgnoreCase("S")||respR.equalsIgnoreCase("SI"));

					break;
				case 'A':
					String respRAll=Auxiliar.leeTexto("¿DESEA ADEMÁS BORRAR LA RESERVA DE ID EN CLASES/PROPERTIES?");
					REPORTParser.deleteAll(gestor,respRAll.equalsIgnoreCase("S")||respRAll.equalsIgnoreCase("SI"));
					break;
				case 'G':
					
					String queryG=Auxiliar.leeTexto("Introduzca el nombre de la query");
					queryG=queryG.toLowerCase();
					REPORTParser.getDesign(path, queryG);
					break;
				case 'U':
					
					String queryU=Auxiliar.leeTexto("Introduzca el nombre de la query");
					queryG=queryU.toLowerCase();
					REPORTParser.updateDesign(path, queryG,pathIreport);
					break;
	
				default:
					System.out.println("La opcion es incorreta");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} while (opcion != 'S');
		
		fcdb.removeConnections();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln(
				"Fin de sesión: " + Calendar.getInstance().getTime());
	}
	public char dameOpcion(String db, String gestor, String cliente, Integer port, String nameConfigXML) {
		System.out.println("\n\n===========================================================================================");
		System.out.println("\n           DYNAGENT REPORT IMPORT   \n    @autor: David Amian Valle  - david.amian.valle@gmail.com");
		String dbStr = "\nBase de datos: " + db + " Gestor: " + gestor;
		if (port!=null)
			dbStr += " Port:" + port;
		dbStr += " ConfigXML:" + nameConfigXML;
		System.out.println(dbStr);
		System.out.println("Cliente: " + cliente);
		System.out.println("\n OPCIONES:");
		System.out.println("M.-Crear Diseños de Reports");
		System.out.println("I.-Importar un Report concreto");
		System.out.println("R.-Importar todos los Reports ");
		System.out.println("D.-Borrar Report");
		System.out.println("A.-Borrar todos los Reports");
		System.out.println("G.-Recoger Diseño");
		System.out.println("U.-Actualizar Diseños");
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
