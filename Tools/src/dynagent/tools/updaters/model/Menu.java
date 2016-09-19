package dynagent.tools.updaters.model;

import java.sql.SQLException;
import java.util.Calendar;

import dynagent.ruleengine.ConceptLogger;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.FactoryConnectionDB;

public class Menu {
	
	public static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		return fcdb;
	}

	public static void main(String args[]) throws SQLException {
		ConceptLogger.getLogger().cleanFile();
		ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip = args[0];
		int nbusiness = Integer.parseInt(args[1]);
		String gestor = args[2];
		String snbusiness = String.valueOf(nbusiness);
		String path = args[3];
		
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, gestor);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		
		XMLModelUpdate xmlUpdate = new XMLModelUpdate(fcdb);
		xmlUpdate.startUpdate(path);
		
		fcdb.removeConnections();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
}
