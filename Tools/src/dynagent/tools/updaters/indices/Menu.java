package dynagent.tools.updaters.indices;

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

	public static void main(String args[]) throws Exception {
		ConceptLogger.getLogger().cleanFile();
		ConceptLogger.getLogger().writeln("Inicio de sesión: " + Calendar.getInstance().getTime());
		String ip = args[0];
		int nbusiness = Integer.parseInt(args[1]);
		String gestor = args[2];
		String snbusiness = String.valueOf(nbusiness);
		
		FactoryConnectionDB fcdb=Menu.setConnection(snbusiness, ip, gestor);
		System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		
		UpdateIndex indexUpdate = new UpdateIndex(fcdb);
		indexUpdate.startUpdate();
		System.out.println("*****bye******");
		ConceptLogger.getLogger().writeln("Fin de sesión: " + Calendar.getInstance().getTime());
	}
	
}
