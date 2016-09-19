package dynagent.tools.replica.monitorizador;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.utils.Auxiliar;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.querys.AuxiliarQuery;

public class MonitorizadorReplica {
	
	private class MasterData {
		private String masterHost;
		private Integer masterPort;
		private String masterLogFile;
		private Integer masterLogPos;
		
		public MasterData(String masterHost, Integer masterPort, String masterLogFile, Integer masterLogPos) {
			this.masterHost = masterHost;
			this.masterPort = masterPort;
			this.masterLogFile = masterLogFile;
			this.masterLogPos = masterLogPos;
		}
		
		public String getMasterHost() {
			return masterHost;
		}
		public String getMasterLogFile() {
			return masterLogFile;
		}
		public Integer getMasterLogPos() {
			return masterLogPos;
		}
		public Integer getMasterPort() {
			return masterPort;
		}
	}

	public MonitorizadorReplica() {
	}
	
	public void stopSlave(HashMap<Integer,SlaveData> hSlaveData, Integer serverId) throws SQLException, NamingException {
		//max idServer
		//max Tienda
		//mapa por serverId = datos(String database, int business)
		//FactoryConnectionDB fcdb = setConnection(snbusiness,ip);
		SlaveData sd = hSlaveData.get(serverId);
		FactoryConnectionDB fcdb = setConnection(sd.getBusiness(),sd.getDatabaseIP());
		fcdb.setPort(sd.getPort());
		String sql = "stop slave";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		fcdb.removeConnections();
	}
	public void startSlave(HashMap<Integer,SlaveData> hSlaveData, Integer serverId) throws SQLException, NamingException {
		SlaveData sd = hSlaveData.get(serverId);
		FactoryConnectionDB fcdb = setConnection(sd.getBusiness(),sd.getDatabaseIP());
		fcdb.setPort(sd.getPort());
		String sql = "start slave";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		fcdb.removeConnections();
	}
	public void jumpSlave(HashMap<Integer,SlaveData> hSlaveData, Integer serverId) throws SQLException, NamingException {
		SlaveData sd = hSlaveData.get(serverId);
		FactoryConnectionDB fcdb = setConnection(sd.getBusiness(),sd.getDatabaseIP());
		fcdb.setPort(sd.getPort());
		jumpSlave(fcdb);
		fcdb.removeConnections();
	}
	private void jumpSlave(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String sql = "stop slave";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql = "SET GLOBAL SQL_SLAVE_SKIP_COUNTER = 1";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql = "start slave";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
	}
	public void showMastersPositions(HashMap<Integer,SlaveData> hSlaveData) throws SQLException, NamingException {
		Iterator it = hSlaveData.keySet().iterator();
		while (it.hasNext()) {
			Integer serverId = (Integer)it.next();
			SlaveData sd = hSlaveData.get(serverId);
			Integer masterId = sd.getMasterId();
			if (masterId==null) {
				FactoryConnectionDB fcdb = setConnection(sd.getBusiness(),sd.getDatabaseIP());
				fcdb.setPort(sd.getPort());
				showMasterPositions(fcdb, serverId);
				fcdb.removeConnections();
			}
		}
	}
	private void showMasterPositions(FactoryConnectionDB fcdb, Integer masterId) throws SQLException, NamingException{
		Statement st = null;
		ResultSet rs = null;
		String sql = "show master status";
		//Integer masterId = hSlaveData.get(slaveId).getMasterId();
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getDataBaseConn("mysql").createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String file = rs.getString(1);
				Integer position = rs.getInt(2);
				System.out.println("      Archivo de log para la instancia con SERVER-ID=" + masterId);
				System.out.println("         MasterLogFile en master -> " + file);
				System.out.println("         MasterLogPos en master -> " + position);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
	}

	public void showStatusReplica(HashMap<Integer,SlaveData> hSlaveData) throws SQLException, NamingException {
		boolean hayError = false;
		
		Iterator it = hSlaveData.keySet().iterator();
		while (it.hasNext()) {
			Integer serverId = (Integer)it.next();
			SlaveData sd = hSlaveData.get(serverId);
			String databaseIP = sd.getDatabaseIP();
			Integer business = sd.getBusiness();
			System.out.println("databaseIP -> " + databaseIP + ", business -> " + business);
			FactoryConnectionDB fcdb = setConnection(business, databaseIP);
			String name = sd.getName();
			Integer port = sd.getPort();
			System.out.println("   " + name + ": server_id -> " + serverId + ", port -> " + port);
			Integer masterId = sd.getMasterId();
			boolean showStatus = masterId!=null;
			if (showStatus) {
				hayError = testStatus(fcdb, serverId, masterId, name, port, hSlaveData) || hayError;
			}
			if(!name.equals(ConstantsMonitorizadorReplica.CENTRAL_FEDERADA)) {
				String sufix = sd.getSufix();
				fcdb.removeConnections();
				hayError = checkConfiguration(fcdb, sufix) || hayError;
			} else {
				//ver si las federadas tienen ENGINE=FEDERATED
				//SHOW CREATE TABLE o_reg_instancias_index_replica_slave;
			}
			fcdb.removeConnections();
		}
		if (hayError)
			System.out.println("\nHay errores en la réplica");
		else
			System.out.println("\nRéplica OK");
	}
	
	private boolean testStatus(FactoryConnectionDB fcdb, Integer serverId, Integer masterId, String name, Integer port, 
			HashMap<Integer,SlaveData> hSlaveData) throws SQLException, NamingException{
		HashMap<Integer,MasterData> hMasterData = new HashMap<Integer,MasterData>();
		boolean hayError = false;
		String sql = "show slave status";
		Statement st = null;
		ResultSet rs = null;
		fcdb.setPort(port);
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getDataBaseConn("information_schema").createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String status = rs.getString(1);
				String slave_io_running = rs.getString(11);
				String slave_sql_running = rs.getString(12);
				System.out.println("      status -> " + status + ", slave_io_running -> " + slave_io_running + ", slave_sql_running -> " + slave_sql_running);
				if (name.equals(ConstantsMonitorizadorReplica.TIENDA)){
					hayError = !countTriggers(fcdb);
				}
				if (slave_io_running.equals("No") || slave_sql_running.equals("No")) {
					String error = rs.getString(20);
					System.out.println("      error -> " + error);
					hayError = true;
					
					//guardar en mapa
					String masterHost = rs.getString(2);
					Integer masterPort = rs.getInt(4);
					String masterLogFile = rs.getString(6);
					Integer masterLogPos = rs.getInt(7);
					MonitorizadorReplica.MasterData ss = new MonitorizadorReplica.MasterData(masterHost, masterPort, masterLogFile, masterLogPos);
					hMasterData.put(serverId, ss);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		//QUERY SHOW MASTER STATUS
		//VER SI EN EL SLAVE SE GUARDO ESA POSICION
		if (hayError) {
			
			//mostrar valores de todo si no coincide
			Iterator it = hMasterData.keySet().iterator();
			while (it.hasNext()) {
				Integer slaveId = (Integer)it.next();
				MonitorizadorReplica.MasterData md = hMasterData.get(slaveId);
				sql = "show master status";
				//Integer masterId = hSlaveData.get(slaveId).getMasterId();
				SlaveData sd = hSlaveData.get(masterId);
				FactoryConnectionDB fcdb2 = setConnection(sd.getBusiness(), sd.getDatabaseIP());
				fcdb2.setPort(port);
				con = fcdb2.createConnection(false); 
				try {
					st = con.getDataBaseConn("mysql").createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
						String file = rs.getString(1);
						Integer position = rs.getInt(2);
						
						if (!file.equals(md.getMasterLogFile()) || !position.equals(md.getMasterLogPos())) {
							System.out.println("      Archivo de log para la instancia con SERVER-ID=" + serverId);
							System.out.println("         MasterLogFile en slave -> " + md.getMasterLogFile());
							System.out.println("         MasterLogFile en master -> " + file);
							System.out.println("         MasterLogPos en slave -> " + md.getMasterLogPos());
							System.out.println("         MasterLogPos en master -> " + position);
						}
						//comparar tambien el hostName y el puerto
						String resp = Auxiliar.leeTexto("Para la instancia con ID_SERVER=" + serverId + " se ha producido un error descrito arriba, \n" +
								"¿quiere saltar una posición?");
			        	if(resp.equalsIgnoreCase("S")||resp.equalsIgnoreCase("SI")){
			        		jumpSlave(fcdb);
			        	}
					}
				} finally {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con!=null) {
						fcdb2.close(con);
						fcdb2.removeConnections();
					}
				}
			}
		}
		return hayError;
	}
	
	private String getAlmacenMiEmpresa(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String almacen = null;
		String sql = "select val_texto from o_datos_atrib " +
				"where id_to=(select idto from clases where name='mi_empresa') " +
				"and property=(select prop from properties where name='almacén')";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next())
				almacen = rs.getString(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return almacen;
	}
		
	private boolean checkConfiguration(FactoryConnectionDB fcdb, String sufix) throws SQLException, NamingException {
		boolean configFail = false;
		
		String almacen = getAlmacenMiEmpresa(fcdb);
		String sql = "SELECT `label`,`value` FROM CONFIGURATION";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			boolean almacenOk = false;
			boolean sufixOk = false;
			String almacenDB = null;
			String sufixDB = null;
			while (rs.next()) {
				String label = rs.getString(1);
				String value = rs.getString(2);
				if (label.equals("mi_almacen")) {
					almacenDB = value;
					if (almacen==null || value.equals(almacen))
						almacenOk = true;
				} else if (label.equals("sufix_tienda")) {
					sufixDB = value;
					if (value.equals(sufix))
						sufixOk = true;
				}
			}
			if (almacenOk && sufixOk) {
				if (almacen==null)
					System.out.println("      WARNING! El almacén de mi empresa no está relleno");
				System.out.println("      configuración ok");
			} else {
				configFail = true;
				if (!almacenOk)
					System.out.println("      almacén incorrecto. En tabla Configuration " + almacenDB + ", en Mi empresa " + almacen);
				if (!sufixOk)
					System.out.println("      sufijo incorrecto. En tabla Configuration " + sufixDB + ", en xml de configuration " + sufix);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return configFail;
	}

	private boolean countTriggers(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		boolean hayTr = false;
		String sql = "SELECT count(*) FROM TRIGGERS T";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(false); 
		try {
			st = con.getDataBaseConn("information_schema").createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				Integer count = rs.getInt(1);
				if (count==0)
					System.out.println("      no hay triggers insertados");
				else {
					System.out.println("      triggers insertados -> " + count);
					hayTr = true;
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return hayTr;
	}
	private FactoryConnectionDB setConnection(Integer business,String ip){
		FactoryConnectionDB fcdb = new FactoryConnectionDB(business,true,ip,GestorsDBConstants.mySQL);
		return fcdb;
	}

}
