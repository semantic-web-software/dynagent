package dynagent.tools.replica.installers;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ConfigurationValues;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.querys.AuxiliarQuery;
import dynagent.tools.replica.restore.AuxiliarRestoreDB;
import dynagent.tools.replica.restore.Restore;

public class Install {
	
	/*private final static int errorIO = 1;
	private final static int errorSQL = 2;
	private final static int errorNaming = 3;
	private final static int errorRdnAlmacen = 4;*/
	
	/*private class FilterIdto extends Object {
		private int idto;
		private Integer idtoSup;
		private Integer propLink;
		
		public FilterIdto(int idto, Integer idtoSup, Integer propLink) {
			this.idto = idto;
			this.idtoSup = idtoSup;
			this.propLink = propLink;
		}
		public int getIdto() {
			return idto;
		}
		public Integer getIdtoSup() {
			return idtoSup;
		}
		public Integer getPropLink() {
			return propLink;
		}
		public String toString() {
			return "idto " + idto + ", idtoSup " + idtoSup + ", propLink " + propLink;
		}
	}*/

	public static void main(String args[]) {
		String error = null;
		System.out.println("Num Argumentos:" + args.length);
		System.out.println("---Inicio Argumentos---");
		for (int i=0;i<args.length;i++) {
			System.out.println(i + ": " + args[i]);
		}
		System.out.println("---Fin Argumentos---");
		String gestor = "mySQL";
		boolean replica = args[0].equals("yes");
		String paramConexion = args[1];
		String nameFile = args[2];
		String IPCentral = null;
		Integer portCentral = null;
		Integer businessCentral = null;
		boolean soyCentral = false;
		boolean soyTienda = false;
		String numTienda = null;
		if (replica) {
			String soy = args[3];
			soyCentral = soy.equals("soy_central");
			soyTienda = soy.equals("soy_tienda");
			numTienda = args[4];
			if (soyTienda) {
				String paramConexionCentral = args[5];
				
				String[] paramConexionCentralSpl = paramConexionCentral.split("#");
				IPCentral = paramConexionCentralSpl[0];
				portCentral = Integer.parseInt(paramConexionCentralSpl[1]);
				businessCentral = Integer.parseInt(paramConexionCentralSpl[2]);
			}
		}
		
		String[] paramConexionSpl = paramConexion.split("#");
		int nbusiness = Integer.parseInt(paramConexionSpl[0]);
		String usuario = paramConexionSpl[1];
		String pwd = paramConexionSpl[2];
		Integer port = Integer.parseInt(paramConexionSpl[3]);

		System.out.println("---Inicio Datos---");
		System.out.println("replica " + replica);
		System.out.println("nbusiness " + nbusiness);
		System.out.println("usuario " + usuario);
		System.out.println("pwd " + pwd);
		System.out.println("port " + port);
		System.out.println("nameFile " + nameFile);
		if (replica) {
			System.out.println("soyCentralInstPrinc " + soyCentral);
			System.out.println("soyTienda " + soyTienda);
			if (soyTienda || soyCentral)
				System.out.println("numTienda " + numTienda);
			if (soyTienda) {
				System.out.println("IPCentral " + IPCentral);
				System.out.println("portCentral " + portCentral);
				System.out.println("businessCentral " + businessCentral);
			}
		}
		System.out.println("---Fin Datos---");
		
		//conexion
		FactoryConnectionDB fcdb=Install.setConnection(nbusiness, null, gestor, usuario, pwd);
		System.out.println("conectado");
		//System.out.println("---------> " + DAOManager.getInstance().getBusiness());

		//creacion de usuarios
		try {
			String dbTemp = "mysql";
			String flush = "flush privileges;";
			fcdb.setPort(port);
			
			try {
				String grant1 = "grant all privileges on *.* to 'dynagent'@'localhost' identified by 'domocenter28';";
				AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, grant1, true);
			} catch (Exception e) {
				if (usuario.equals("dynagent")) {
					usuario = "root";
					pwd = "";
				} else {
					usuario = "dynagent";
					pwd = "domocenter28";
				}
				fcdb.setUsuario(usuario);
				fcdb.setPwd(pwd);
				String grant1 = "grant all privileges on *.* to 'dynagent'@'localhost' identified by 'domocenter28';";
				AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, grant1, true);
			}
			String grant2 = "grant all privileges on *.* to 'dynagent'@'%' identified by 'domocenter28';";
			AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, grant2, true);
			String upd = "update mysql.`user` set grant_priv='Y';";
			AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, upd, true);
			AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, flush, true);

			if (!usuario.equals("dynagent")) {
				//si usuario es root y esta hago lo siguiente
				System.out.println("reconectando con el nuevo usuario");
				//reconexion con el nuevo usuario
				String usuarioIn = usuario;
				usuario = "dynagent";
				pwd = "domocenter28";
				fcdb.setUsuario(usuario);
				fcdb.setPwd(pwd);
				System.out.println("reconectado");
				if (usuarioIn.equals("root")) {
					System.out.println("borrando el usuario root");
					String delete = "delete from mysql.`user` where `user`='root' or `user`='';";
					AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, delete, true);
					AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, flush, true);
				}
				System.out.println("fin de configuracion del usuario dynagent");
			}

			String sqlCreate = "CREATE DATABASE IF NOT EXISTS dyna" + nbusiness + "_dbo";
			AuxiliarQuery.dbExecUpdate(fcdb, dbTemp, sqlCreate, true);
			fcdb.removeConnections();
			//A partir de aqui se conecta a la dyna
			//ejecutar script de restauracion de la base de datos
			Install.run(nameFile, fcdb);
			System.out.println("Base de datos restaurada");
			
			if (replica) {
				//boolean centralInstPrinc = nombreCentral!=null;
					//System.out.println("soy la instancia principal de central o una tienda, voy a inicializar los idos en 11000");
					//no hace falta, se hace en el importador del modelo
					//Install.inicializeIdos(fcdb);
					
					if (soyCentral) {
						System.out.println("Insert de sufix_tienda en mi configuration");
						String insert = "insert into configuration(`label`,`value`) values('sufix_tienda',LPAD(" + numTienda + ",3,'0'))";
						AuxiliarQuery.dbExecUpdate(fcdb, insert, true);
					} else {
						System.out.println("Update de sufix_tienda en mi configuration");
						String oldSufix = Install.getOldSufix(fcdb);
						System.out.println("oldSufix " + oldSufix);
						String update = "update configuration set `value`=LPAD(" + numTienda + ",3,'0') where `label`='sufix_tienda'";
						AuxiliarQuery.dbExecUpdate(fcdb, update, true);
												
						Integer ido = idoRdnAlmacen(fcdb, numTienda);
						
						//update de mi_empresa, apuntar a la nueva empresa
						//tiene un ido de la central
						if (ido!=null) {
							System.out.println("Update de mi empresa");
							update = "update o_datos_atrib " +
								"set val_texto=LPAD(" + numTienda + ",3,'0'), " +
								//"val_num=(select id_o from o_reg_instancias where rdn='" + rdnMiEmpresa + "' and id_to=(select idto from clases where name ='almacén'))" +
								"val_num=" + ido +
								" where id_to=(select idto from clases where name ='mi_empresa')" +
								" and property=(select prop from properties where name='almacén')";
							AuxiliarQuery.dbExecUpdate(fcdb, update, true);
							//TODO faltaria revisar el resto de datos de mi_empresa -> WARNING en instalador
						//} else {
							//poner CONFIG a N
							//rdnMiEmpresa = "NO_CONFIG";
							//setNoConfig(fcdb);
						}
						
						//Central en tabla de configuracion
						System.out.println("Update de Central en mi configuration");
						update = "update configuration set `value`='" + IPCentral + "' where `label`='IP_Central'";
						AuxiliarQuery.dbExecUpdate(fcdb, update, true);
						update = "update configuration set `value`='" + portCentral + "' where `label`='port_Central'";
						AuxiliarQuery.dbExecUpdate(fcdb, update, true);
						update = "update configuration set `value`='" + businessCentral + "' where `label`='business_Central'";
						AuxiliarQuery.dbExecUpdate(fcdb, update, true);

						//almacen en tabla de configuracion
						System.out.println("Update de sufix_tienda en mi configuration");
						update = "update configuration set `value`=LPAD(" + numTienda + ",3,'0') where `label`='mi_almacen'";
						AuxiliarQuery.dbExecUpdate(fcdb, update, true);
						
						System.out.println("soy una tienda, voy a obtener datos de Central");
						
						
						if (oldSufix.equals("001")) {
							//backup de Central, se trata de la primera tienda
							Install.updateReplication(fcdb);
						} else {
							//llamar a restaurador
							System.out.println("Va a llamar al restaurador");
							long ini = System.currentTimeMillis();
							String path=System.getProperty("user.dir");
							String pathXmlFile = path+"/files/"+"ConfigCELOPTIENDA.xml";
							new Restore(fcdb, pathXmlFile);
							System.out.println("Se ha traido los datos en: " + (System.currentTimeMillis() - ini) + " milisegundos");
							//Install.getCentralData(fcdb, numTienda, oldSufix, rdnMiEmpresa, nombrePCActual, port, nbusiness, IPCentral, portCentral, businessCentral);
						}
					}
				
				//start replica? Automatico... reiniciar mysql y jboss en instalador
				fcdb.removeConnections();
			}
		} catch (IOException e) {
			error = "IOException";
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			error = "SQLException";
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (NamingException e) {
			error = "NamingException";
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			error = "Exception";
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		if (error==null)
			System.out.println("Terminado sin errores");
		else
			System.out.println("error " + error);
		//return error;
	}
	
	private static String getOldSufix(FactoryConnectionDB fcdb) 
			throws SQLException, NamingException {
		String oldSufix = null;
		String sql = "SELECT `value` FROM configuration WHERE `label`='sufix_tienda'";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				oldSufix = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return oldSufix;
	}

	private static Integer idoRdnAlmacen(FactoryConnectionDB fcdb, String numTienda) 
			throws SQLException, NamingException {
		Integer ido = null;
		//antes buscaba en O_Reg_Instancias
		String sql = "select id_o from O_Datos_Atrib where val_texto=LPAD(" + numTienda + ",3,'0') " +
				"and id_to=(select idto from clases where name ='almacén') and property=" + Constants.IdPROP_RDN;

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				ido = rs.getInt(1);
			} else 
				System.out.println("No existe un individuo creado con ese rdn de almacén");
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return ido;
	}
	
	/*private static void inicializeReplication(FactoryConnectionDB fcdb, 
			HashSet<String> aReplicaMaster, HashSet<String> aReplicaSlave, 
			HashMap<Integer,ArrayList<Replication>> hReplicationMaster, HashMap<Integer,ArrayList<Replication>> hReplicationSlave) 
			throws SQLException, NamingException {
		ReplicationDAO rDAO = new ReplicationDAO();
		rDAO.open();
		Iterator it = rDAO.getAll().iterator();
		while (it.hasNext()) {
			Replication rep = (Replication)it.next();
			String tableName = rep.getTableName();
			boolean actualPc = rep.isActualPC();
			
			Integer idto = rep.getIdTo();
			if (actualPc) {
				aReplicaSlave.add(tableName);
				ArrayList<Replication> aRepl = hReplicationSlave.get(idto);
				if (aRepl==null) {
					aRepl = new ArrayList<Replication>();
					hReplicationSlave.put(idto, aRepl);
				}
				aRepl.add(rep);
			} else {
				aReplicaMaster.add(tableName);
				ArrayList<Replication> aRepl = hReplicationMaster.get(idto);
				if (aRepl==null) {
					aRepl = new ArrayList<Replication>();
					hReplicationMaster.put(idto, aRepl);
				}
				aRepl.add(rep);
			}
		}
		rDAO.close();
	}
	
	private static HashSet<Integer> getIdtosReplica(HashMap<Integer,ArrayList<Replication>> hReplicationSlave) 
			throws SQLException, NamingException {
		HashSet<Integer> aReplica = new HashSet<Integer>();
		Iterator it = hReplicationSlave.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			aReplica.add(idto);
		}
		return aReplica;
	}
	
	private static void getIdtos(HashMap<Integer,ArrayList<Replication>> hReplicationSlave, ArrayList<FilterIdto> all, ArrayList<FilterIdto> aTiendas) 
			throws SQLException, NamingException {
		Iterator it = hReplicationSlave.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			ArrayList<Replication> aRepl = hReplicationSlave.get(idto);
			Iterator it2 = aRepl.iterator();
			while (it2.hasNext()) {
				Replication rep = (Replication)it2.next();
				Install ins = new Install();
				Install.FilterIdto fi = ins.new FilterIdto(idto, rep.getIdtoSup(), rep.getPropLink());
				all.add(fi);
				if (rep.getTableName().equals("replica_a_tiendas_slave"))
					aTiendas.add(fi);
			}
		}
	}
	
	private static HashSet<Integer> getIdosORegInst(int idto) throws SQLException, NamingException {
		HashSet<Integer> idos = new HashSet<Integer>();
		
		O_Reg_InstanciasDAO oRegDAO = new O_Reg_InstanciasDAO();
		oRegDAO.open();
		Iterator it = oRegDAO.getAllCond("id_to=" + idto).iterator();
		while (it.hasNext()) {
			O_Reg_Instancias_Index oReg = (O_Reg_Instancias_Index)it.next();
			idos.add(oReg.getId_o());
		}
		oRegDAO.close();
		return idos;
	}
	private static HashSet<Integer> getIdosORegInstIndex(int idto) throws SQLException, NamingException {
		HashSet<Integer> idos = new HashSet<Integer>();
		
		O_Reg_Instancias_IndexDAO oRegDAO = new O_Reg_Instancias_IndexDAO();
		oRegDAO.open();
		Iterator it = oRegDAO.getAllCond("id_to=" + idto).iterator();
		while (it.hasNext()) {
			O_Reg_Instancias_Index oReg = (O_Reg_Instancias_Index)it.next();
			idos.add(oReg.getId_o());
		}
		oRegDAO.close();
		return idos;
	}
	private static void canBeDeleted(HashSet<Integer> idos, HashSet<Integer> idosABorrar, 
			int idto, Integer idtoSup, Integer propLink) throws SQLException, NamingException {
		O_Datos_AttribDAO oAtrDAO = new O_Datos_AttribDAO();
		oAtrDAO.open();
		Iterator it = oAtrDAO.getAllCond("val_num in(" + Auxiliar.hashSetIntegerToString(idos, ",") + ") and property=" + propLink).iterator();
		while (it.hasNext()) {
			O_Datos_Attrib oDat = (O_Datos_Attrib)it.next();
			Integer idtoSupDB = oDat.getIDTO();
			if (idtoSupDB.equals(idtoSup)) {
				Integer ido = oDat.getVALNUM();
				System.out.println("ido a borrar " + ido + " de idto " + idto + " con idtoSup " + idtoSup);
				idosABorrar.add(ido);
			}
		}
		oAtrDAO.close();
	}
	
	private static void clean(FactoryConnectionDB fcdb, String numTienda, String oldSufix) 
			throws IOException, SQLException, NamingException {
		if (oldSufix.equals("001")) {
			//modificar replication
			Install.updateReplication(fcdb);
		}
		//obtener nombres de tablas de replica
		HashSet<String> aReplicaMaster = new HashSet<String>();
		HashSet<String> aReplicaSlave = new HashSet<String>();
		HashMap<Integer,ArrayList<Replication>> hReplicationMaster = new HashMap<Integer,ArrayList<Replication>>();
		HashMap<Integer,ArrayList<Replication>> hReplicationSlave = new HashMap<Integer,ArrayList<Replication>>();
		
		Install.inicializeReplication(fcdb, aReplicaMaster, aReplicaSlave, hReplicationMaster, hReplicationSlave);
			if (!oldSufix.equals("001")) {
				System.out.println("El backup es de Tienda");
				//borrar todos los idos que tengan el antiguo sufijo
				//no borra datos de la central porque es una copia de otra tienda
				//borrado en o_reg_instancias_index
				String conditionIdo = "ID_O>11000 AND SUBSTRING(ID_O,length(ID_O)-2)=" + oldSufix;
				String conditionValNum = "VAL_NUM>11000 AND SUBSTRING(VAL_NUM,length(VAL_NUM)-2)=" + oldSufix;
				String conditionValue = "VALUE>11000 AND SUBSTRING(VALUE,length(VALUE)-2)=" + oldSufix;
				
				String sql = "DELETE FROM o_reg_instancias_index WHERE " + conditionIdo;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("Borrado de o_reg_instancias_index");
				//borrado en ODA
				sql = "DELETE FROM o_datos_atrib WHERE " + conditionIdo + " OR " + conditionValNum;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("Borrado de o_datos_atrib");
				//borrado en ODAMemo
				sql = "DELETE FROM o_datos_atrib_memo WHERE " + conditionIdo;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("Borrado de o_datos_atrib_memo");
				
				//borrado de tablas de replica
				Iterator it = aReplicaSlave.iterator();
				while (it.hasNext()) {
					String table = (String)it.next();
					sql = "DELETE FROM " + table + " WHERE " + conditionIdo + " OR " + conditionValNum;
					AuxiliarImporters.dbExecUpdate(fcdb, sql);
					System.out.println("Borrado de " + table);
				}
			} else {
				System.out.println("El backup es de Central");
				String tablesReplMaster = dynagent.common.utils.Auxiliar.hashSetToStringComillas(aReplicaMaster, ",");
				//String tablesReplSlave = dynagent.common.utils.Auxiliar.hashSetToStringComillas(aReplicaSlave, ",");
	
				//hay que mirar en replica_a_tiendas_slave para saber lo que no puedo borrar aunque no sea mio
				//tener en cuenta el superior aki
				//borrar idos con superior nulo union los que no tienen el idto superior no nulo en replication
				HashSet<Integer> hIdosATiendas = new HashSet<Integer>();
				ArrayList<FilterIdto> aIdtos = new ArrayList<FilterIdto>();
				ArrayList<FilterIdto> aIdtosAT = new ArrayList<FilterIdto>();
				Install.getIdtos(hReplicationSlave, aIdtos, aIdtosAT);
				
				Iterator itAT = aIdtosAT.iterator();
				System.out.println("Idtos que se replican a tiendas con superior no nulo a comprobar:");
				while (itAT.hasNext()) {
					FilterIdto fIdto = (FilterIdto)itAT.next();
					System.out.println(fIdto);
					Integer idto = fIdto.getIdto();
					Integer idtoSup = fIdto.getIdtoSup();
					HashSet<Integer> idos = Install.getIdosORegInst(idto);
					if (idtoSup!=null) {
						Integer propLink = fIdto.getPropLink();
						Install.canBeDeleted(idos, hIdosATiendas, idto, idtoSup, propLink);
					} else
						hIdosATiendas.addAll(idos);
				}
				String idosATiendas = Auxiliar.hashSetIntegerToString(hIdosATiendas, ",");
				System.out.println("idosATiendas " + idosATiendas);
				
				//recordar que en o_reg_instancias esta lo que he creado en Central
				String sql = "delete from o_reg_instancias_index_replica " +
					" where id_o not in(" + idosATiendas + ")";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql ="insert into o_reg_instancias_index_replica(id_o, id_to, rdn) " +
						"SELECT id_o, id_to, rdn FROM o_reg_instancias_index " +
						"where id_to in(select distinct id_to from replication where table_name in(" + tablesReplMaster + "))";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				
				
				//borro lo creado en Central que no me sirve
				//busqueda de idtos en replication con idto_sup no nulo
				HashSet<Integer> hIdosABorrar = new HashSet<Integer>();
				Iterator it = aIdtos.iterator();
				System.out.println("Idtos creados en central con superior no nulo sin comprobar:");
				while (it.hasNext()) {
					FilterIdto fIdto = (FilterIdto)it.next();
					System.out.println(fIdto);
					Integer idto = fIdto.getIdto();
					Integer idtoSup = fIdto.getIdtoSup();
					HashSet<Integer> idos = Install.getIdosORegInstIndex(idto);
					if (idtoSup!=null) {
						Integer propLink = fIdto.getPropLink();
						Install.canBeDeleted(idos, hIdosABorrar, idto, idtoSup, propLink);
					} else
						hIdosABorrar.addAll(idos);
				}
				String idosABorrar = Auxiliar.hashSetIntegerToString(hIdosABorrar, ",");
				System.out.println("idosABorrar " + idosABorrar);

				sql = "delete from o_datos_atrib where id_o in(" + idosABorrar + ")";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql = "delete from o_datos_atrib where val_num in(" + idosABorrar + ")";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				//borro los datos de las otras Tiendas que se han replicado y que no estan en replica_a_tiendas
				sql = "delete from o_datos_atrib where SUBSTRING(ID_O,length(ID_O)-2)<>'001' and SUBSTRING(ID_O,length(ID_O)-2)<>LPAD(" + numTienda + ",3,'0')" + 
						" and id_o not in(" + idosATiendas + ") and id_to>" + Constants.MIN_ID_NO_SPECIALCLASS;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql = "delete from o_datos_atrib where SUBSTRING(VAL_NUM,length(ID_O)-2)<>'001' and SUBSTRING(VAL_NUM,length(VAL_NUM)-2)<>LPAD(" + numTienda + ",3,'0')" + 
						" and val_num not in(" + idosATiendas + ") and id_to>" + Constants.MIN_ID_NO_SPECIALCLASS;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);

		
				sql = "delete from o_datos_atrib_memo where id_o in(" + idosABorrar + ")";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql = "delete from o_datos_atrib_memo where SUBSTRING(ID_O,length(ID_O)-2)<>'001' and SUBSTRING(ID_O,length(ID_O)-2)<>LPAD(" + numTienda + ",3,'0')" + 
						" and id_o not in(" + idosATiendas + ") and id_to>" + Constants.MIN_ID_NO_SPECIALCLASS;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);

				
				sql = "delete FROM o_reg_instancias_index where id_to>" + Constants.MIN_ID_NO_SPECIALCLASS;
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				//tengo que añadir en o_reg_instancias_index mis datos creados
				sql ="insert into o_reg_instancias_index(id_o, id_to, rdn) " +
						"SELECT id_o, id_to, val_texto FROM o_datos_atrib " +
						"where id_to>" + Constants.MIN_ID_NO_SPECIALCLASS + " and property=" + Constants.IdPROP_RDN + 
						" and SUBSTRING(ID_O,length(ID_O)-2)=LPAD(" + numTienda + ",3,'0')";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
			}
			
			//actualizacion de tablas de replica
			System.out.println("Actualización de tablas de réplica");
			HashSet<Integer> ar3 = Install.getIdtosReplica(hReplicationSlave);
			ImportReplica importRepl = new ImportReplica(ar3, hReplicationSlave);
			importRepl.startUpdateData();
	}*/
	
	private static void updateReplication(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		String sql ="update replica_configuration set actual_pc='O' where actual_pc='Y'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql ="update replica_configuration set actual_pc='Y' where actual_pc='N'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql ="update replica_configuration set actual_pc='N' where actual_pc='O'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		
		sql ="update nomodifydb set actual_pc='O' where actual_pc='Y'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql ="update nomodifydb set actual_pc='Y' where actual_pc='N'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
		sql ="update nomodifydb set actual_pc='N' where actual_pc='O'";
		AuxiliarQuery.dbExecUpdate(fcdb, sql, true);
	}
	
	/*private static void getCentralData(FactoryConnectionDB fcdb, String numTienda, String oldSufix, 
			String rdnMiEmpresa, String nombrePC, Integer port, Integer business, String IPCentral, Integer portCentral, Integer businessCentral) 
			throws IOException, SQLException, NamingException {
		if (oldSufix.equals("001")) {
			//modificar replication
			//backup de Central, se trata de la primera tienda
			Install.updateReplication(fcdb);
		} else {
			//El backup siempre es de una tienda en blanco, no tengo que borrar nada
			//solo traerme datos de Central
			//como los triggers ya estan funcionando copio en las tablas de replica (replica_master y replica_contribuciones)
			//TODO coger nombres de las tablas de replica de replication
			fcdb.setDatabaseIP(IPCentral);
			fcdb.setPort(portCentral);
			fcdb.setBusiness(businessCentral);
			System.out.println(fcdb);
			
			String pathReplicaMaster = "C:/resultReplicaMaster.txt";
			String pathReplicaMasterMemo = "C:/resultReplicaMasterMemo.txt";
			String pathODA = "C:/resultODA.txt";
			String pathODAMemo = "C:/resultODAMemo.txt";
			
			
			System.out.println("CREACION DE FICHEROS");
			//carga de replica_master
			String sql = "SELECT distinct * INTO OUTFILE '" + pathReplicaMaster + "' " +
					"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
					"LINES TERMINATED BY '\n' " +
					"FROM replica_master where id_o>11000;";
			AuxiliarImporters.dbExec(fcdb, sql);
			sql = "SELECT distinct * INTO OUTFILE '" + pathReplicaMasterMemo + "' " +
					"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
					"LINES TERMINATED BY '\n' " +
					"FROM replica_master_memo where id_o>11000;";
			AuxiliarImporters.dbExec(fcdb, sql);
			
			//carga de o_datos_atrib
			sql = "SELECT distinct * INTO OUTFILE '" + pathODA + "' " +
					"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
					"LINES TERMINATED BY '\n' " +
					"FROM o_datos_atrib " +
					"where id_o>=" + Constants.MIN_ID_INDIVIDUAL_BDDD + 
					//" and (SUBSTRING(ID_O,length(ID_O)-2)=LPAD(" + numTienda + ",3,'0') " +
					//" or SUBSTRING(VAL_NUM,length(VAL_NUM)-2)=LPAD(" + numTienda + ",3,'0'))" +
					" and SUBSTRING(ID_O,length(ID_O)-2)=LPAD(" + numTienda + ",3,'0') " +
					" and id_to not in (select id_to from replication where table_name='replica_master');";
			AuxiliarImporters.dbExec(fcdb, sql);
			sql = "SELECT distinct * INTO OUTFILE '" + pathODAMemo + "' " +
					"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
					"LINES TERMINATED BY '\n' " +
					"FROM o_datos_atrib_memo " +
					"where id_o>=" + Constants.MIN_ID_INDIVIDUAL_BDDD + 
					" and SUBSTRING(ID_O,length(ID_O)-2)=LPAD(" + numTienda + ",3,'0')" +
					" and id_to not in (select id_to from replication where table_name='replica_master');";
			AuxiliarImporters.dbExec(fcdb, sql);

			
			File f = new File(pathReplicaMaster);
			if (f.exists()) {
				fcdb.setDatabaseIP(nombrePC);
				fcdb.setPort(port);
				fcdb.setBusiness(business);
				System.out.println("CARGA DE DATOS");
				//poner disable_origin a true
				setDisableOrigin(fcdb, true);
				sql = "LOAD DATA INFILE '" + pathReplicaMaster + "' " +
						"INTO TABLE replica_master " +
						"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
						"LINES TERMINATED BY '\n';";
				AuxiliarImporters.dbExec(fcdb, sql);
				f.delete();
				System.out.println("actualizacion de replica_master realizada");
				
				sql = "LOAD DATA INFILE '" + pathReplicaMasterMemo + "' " +
						"INTO TABLE replica_master_memo " +
						"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
						"LINES TERMINATED BY '\n';";
				AuxiliarImporters.dbExec(fcdb, sql);
				f = new File(pathReplicaMasterMemo);
				f.delete();
				System.out.println("actualizacion de replica_master_memo realizada");
		
				//poner disable_origin a false
				setDisableOrigin(fcdb, false);
				//carga de contribuciones a partir de replica_master
				sql = "insert into replica_contribuciones_master " +
						"select id_to, id_o, property, val_num, val_texto, value_cls, q_min, q_max, op, 'N', destination from replica_master " +
						"where id_to=(select idto from clases where name='stock') and destination='" + rdnMiEmpresa + "';";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql = "delete from replica_contribuciones_master;";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("actualizacion de replica_contribuciones_master realizada");
		
				sql = "insert into replica_contribuciones_master_memo " +
						"select id_to, id_o, property, memo, value_cls, op, 'N', destination from replica_master_memo " +
						"where id_to=(select idto from clases where name='stock') and destination='" + rdnMiEmpresa + "';";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				sql = "delete from replica_contribuciones_master_memo;";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("actualizacion de replica_contribuciones_master_memo realizada");
		
				//carga de mis datos
				//o_reg_instancias_index y o_datos_atrib
				sql = "LOAD DATA INFILE '" + pathODA + "' " +
						"INTO TABLE o_datos_atrib " +
						"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
						"LINES TERMINATED BY '\n';";
				AuxiliarImporters.dbExec(fcdb, sql);
				f = new File(pathODA);
				f.delete();
				System.out.println("actualizacion de o_datos_atrib realizada");
		
				sql = "LOAD DATA INFILE '" + pathODAMemo + "' " +
						"INTO TABLE o_datos_atrib_memo " +
						"FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' " +
						"LINES TERMINATED BY '\n';";
				AuxiliarImporters.dbExec(fcdb, sql);
				f = new File(pathODAMemo);
				f.delete();
				System.out.println("actualizacion de o_datos_atrib_memo realizada");
				
				sql = "insert into o_reg_instancias_index(autonum, id_o, id_to, rdn) " +
						"select SUBSTRING(ID_O,1,length(ID_O)-3),id_o, id_to, val_texto from o_datos_atrib " +
						"where id_o>=" + Constants.MIN_ID_INDIVIDUAL_BDDD +
						" and property=" + Constants.IdPROP_RDN + 
						" and SUBSTRING(ID_O,length(ID_O)-2)=LPAD(" + numTienda + ",3,'0')";
				AuxiliarImporters.dbExecUpdate(fcdb, sql);
				System.out.println("actualizacion de o_reg_instancias_index realizada");
				
				//no es necesario actualizar:
				//o_reg_instancias_index_replica_slave, replica_a_tiendas_slave y replica_slave 
				
			} else {
				System.out.println("Falta traer los ficheros de central y volver a ejecutar el instalador");
			}
		}
	}*/
	
	/*private static void setNoConfig(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		//si se va a deshabilitar ver si esta insertado en configuration
		if (isConfig(fcdb))
			updateNoConfig(fcdb);
		else
			insertNoConfig(fcdb);
	}
	
	private static void insertNoConfig(FactoryConnectionDB fcdb)
			throws SQLException, NamingException {
		String sql = "insert into configuration(`label`, `value`) " +
			"values('config','N')";
		AuxiliarDB.dbExecUpdate(fcdb, sql);
	}
	private static void updateNoConfig(FactoryConnectionDB fcdb)
			throws SQLException, NamingException {
		String sql = "update configuration set `value`='N' where `label`='disable_origin'";
		AuxiliarDB.dbExecUpdate(fcdb, sql);
	}
		
	private static boolean isConfig(FactoryConnectionDB fcdb) 
			throws SQLException, NamingException {
		boolean is = false;
		String sql = "SELECT `value` FROM configuration WHERE `label`='config'";
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				is = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return is;
	}*/

	/*private static void setDisableOrigin(FactoryConnectionDB fcdb, boolean state) throws SQLException, NamingException {
		//si se va a deshabilitar ver si esta insertado en configuration
		if (state) {
			if (isDisableOrigin(fcdb))
				updateDisableOrigin(fcdb, state);
			else
				insertDisableOrigin(fcdb, state);
		} else
			updateDisableOrigin(fcdb, state);
	}
	
	private static void insertDisableOrigin(FactoryConnectionDB fcdb, boolean state)
			throws SQLException, NamingException {
		String sql = "insert into configuration(`label`, `value`) " +
			"values('disable_origin',";
		if (state)
			sql += "'Y'";
		else
			sql += "'N'";
		sql += ")";
		AuxiliarImporters.dbExecUpdate(fcdb, sql);
	}
	private static void updateDisableOrigin(FactoryConnectionDB fcdb, boolean state)
			throws SQLException, NamingException {
		String sql = "update configuration set `value`=";
		if (state)
			sql += "'Y'";
		else
			sql += "'N'";
		sql += " where `label`='disable_origin'";
		AuxiliarImporters.dbExecUpdate(fcdb, sql);
	}
	
	private static boolean isDisableOrigin(FactoryConnectionDB fcdb) 
			throws SQLException, NamingException {
		boolean is = false;
		String sql = "SELECT `value` FROM configuration WHERE `label`='disable_origin'";

		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				is = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return is;
	}*/

	private static void run(String nameFile, FactoryConnectionDB fcdb) 
			throws IOException, SQLException, NamingException {
		String path=System.getProperty("user.dir");
		System.out.println("path " + path);
		File fq = new File(path+"/files/"+nameFile);
		Install.executeFile(fq, fcdb);
	}
	
	public static void executeFile(File fq, FactoryConnectionDB fcdb) throws SQLException, NamingException, IOException {
		if (fq!=null && fq.exists()) {
			Statement st = null;
			ConnectionDB con = fcdb.createConnection(true); 
			try {
				st = con.getBusinessConn().createStatement();
				
				BufferedReader bfq = new BufferedReader(new FileReader(fq));
				StringBuffer script = new StringBuffer("");
				String sLine = "";
				String delimiter = ";";
				while((sLine = bfq.readLine())!=null) {
					if (sLine.length()>0 && !sLine.startsWith("--")) {
						if (sLine.startsWith("DELIMITER")) {
							delimiter = sLine.substring(10,sLine.length());
							//System.out.println("delimiter-> " + delimiter);
							//System.out.println("sLineDelimiter " + sLine);
						} else {
							if (sLine.endsWith(delimiter)) {
								//System.out.println("sLine " + sLine.substring(0,sLine.length()-2));
								script.append(sLine.substring(0,sLine.length()-delimiter.length()));
								//script.append(sLine.substring(0,sLine.length()));
	
								String scriptStr = script.toString();
								//System.out.println("scriptStr " + scriptStr);
								int count = st.executeUpdate(scriptStr);
								//System.out.println("filas modificadas " + count);
								script = new StringBuffer("");
							} else {
								//System.out.println("sLine " + sLine);
								script.append(sLine + "\n");
							}
						}
					} else {
						//System.out.println("sLineOmitida " + sLine);
					}
				}
			} finally {
				if (st != null)
					st.close();
				if (con!=null)
					fcdb.close(con);
			}
		}
	}

	public static FactoryConnectionDB setConnection(int nbusiness, String ip, String gestor, String usuario, String pwd){
		System.out.println("antes de crear fconection");
		DAOManager.getInstance().setBusiness(String.valueOf(nbusiness));
		FactoryConnectionDB fcdb = new FactoryConnectionDB(nbusiness,true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		System.out.println("despues de crear fconection");
		if (usuario!=null) {
			fcdb.setUsuario(usuario);
			fcdb.setPwd(pwd);
		}
		return fcdb;
	}
}
