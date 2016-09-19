package dynagent.ejbengine.util;

import java.sql.SQLException;
import java.util.Hashtable;

import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;

public class DataBaseMapManager {
	
	private static Hashtable<String, Hashtable<Integer, DataBaseMapManager>> dataBaseMapManager = new Hashtable<String, Hashtable<Integer,DataBaseMapManager>>();
	
	private Hashtable<Integer, DataBaseMap> dataBaseMapByBns;
	
	private FactoryConnectionDB fcdb;
	
	public static DataBaseMapManager getInstance(FactoryConnectionDB fcdb){
		DataBaseMapManager result = null;
		Hashtable<Integer, DataBaseMapManager> dataBaseMapManagerByGestor = dataBaseMapManager.get(fcdb.getGestorDB());
		if (dataBaseMapManagerByGestor == null){
			dataBaseMapManagerByGestor = new Hashtable<Integer, DataBaseMapManager>();
			result = new DataBaseMapManager(fcdb);
			dataBaseMapManagerByGestor.put(fcdb.getPort(), result);
			dataBaseMapManager.put(fcdb.getGestorDB(), dataBaseMapManagerByGestor);
		}else{
			result = dataBaseMapManagerByGestor.get(fcdb.getPort());
			if (result == null){
				result = new DataBaseMapManager(fcdb);
				dataBaseMapManagerByGestor.put(fcdb.getPort(), result);
			}
		}
		return result;
	}
	
	private DataBaseMapManager(FactoryConnectionDB fcdb){
		dataBaseMapByBns = new Hashtable<Integer, DataBaseMap>();
		this.fcdb = fcdb;
	}
	
	public DataBaseMap getDataBaseMap (int bns) throws SQLException{
		DataBaseMap dataBaseMap = dataBaseMapByBns.get(bns);
		if (dataBaseMap == null){
			dataBaseMap = new DataBaseMap(fcdb, false);
			dataBaseMapByBns.put(bns, dataBaseMap);
			fcdb.removeConnections();
		}
		return dataBaseMap;
	}
 
}
