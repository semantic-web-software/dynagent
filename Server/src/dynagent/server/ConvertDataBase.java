package dynagent.server;

import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;

public class ConvertDataBase {
	/** Dirección ip en la que se encuentra la base de datos */
	private final static String DB_IP = "192.168.1.3";
	/** Puerto en el que se encuentra el gestor de base de datos */
	private final static int DB_PORT = 5432;
	/** Gestor de la base de datos */
	private final static String DB_MANAGER = GestorsDBConstants.postgreSQL;
	/** número de la base de datos de la que queremos obtener los datos */
	private final static int DB_BNS = 6;
	
	public static void main(String[] args){
		FactoryConnectionDB fcdb = new FactoryConnectionDB(DB_BNS, true, DB_IP, DB_MANAGER);
		fcdb.setPort(DB_PORT);
		DataBaseMap dataBaseMap = new DataBaseMap(fcdb, true);
//		dataBaseMap.constructAllTables(null);
//		dataBaseMap.createTLinkTable();
		dataBaseMap.constructAllViews(null);
	}
}
