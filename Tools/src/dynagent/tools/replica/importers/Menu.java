package dynagent.tools.replica.importers;

import java.util.Calendar;

import org.jdom.Element;

import dynagent.common.utils.Auxiliar;
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
		Integer port = Integer.parseInt(args[2]);
		String path = args[3];
		String snbusiness = String.valueOf(nbusiness);
		
		String gestor = "mySQL";
		FactoryConnectionDB fcdb = Menu.setConnection(snbusiness, ip, gestor);
		fcdb.setPort(port);
		//System.out.println("---------> " + DAOManager.getInstance().getBusiness());
		String resp1=Auxiliar.leeTexto("Va a sustituir la configuración de réplica mySQL en la base de datos: " + DAOManager.getInstance().getBusiness() + ", puerto: " + port
				+ ". ¿Está seguro?");
		if(resp1.equalsIgnoreCase("SI")||resp1.equalsIgnoreCase("S")){
			ParserReplicaXML parserRepl = new ParserReplicaXML();
			
			Element xmlReplica = parserRepl.readXML(path);
			parserRepl.startUpdateConfiguration(xmlReplica);

			//ImportReplica importRepl = new ImportReplica(parserRepl.getIdtos(), parserRepl.getHReplication());
			//String resp=Auxiliar.leeTexto("¿Quiere eliminar los antiguos datos de las tablas de réplica? Necesario si se ha importado sin mantener individuos");
			//if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
			//	importRepl.deleteDataReplica(fcdb);
			//}
			//si en un pc con datos que no tenia replica se quiere poner
			//resp=Auxiliar.leeTexto("¿Quiere actualizar las tablas de réplica con los datos de o_datos_atrib? Útil para control");
			//if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
			//	importRepl.startUpdateData();
			//}
			/* ESTO SE HARA EN SQL
			 * String resp=Auxiliar.leeTexto("¿Quiere actualizar las tablas de réplica con los datos de réplica de otra base de datos? Útil para tiendas");
			if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
				replica.startUpdateRemoteData();
			}*/
			fcdb.removeConnections();
			System.out.println("*****bye******");
			ConceptLogger.getLogger().writeln("Fin de sesión: " + Calendar.getInstance().getTime());
		}
	}
	
}
