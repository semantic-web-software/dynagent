package dynagent.tools.updaters.rdn;

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
		String ip = "192.168.1.3";//args[0];
		int nbusiness =6; //Integer.parseInt(args[1]);
		String gestor = "SQLServer";//args[2];
		String snbusiness = String.valueOf(nbusiness);
		
		FactoryConnectionDB fcdb = Menu.setConnection(snbusiness, ip, gestor);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		
		UpdateRdn xmlUpdate = new UpdateRdn();
		xmlUpdate.startUpdate(fcdb);
		fcdb.removeConnections();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
}
