package dynagent.server.replication;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.naming.NamingException;

import dynagent.common.basicobjects.AssociatedIndividual;
import dynagent.common.basicobjects.GlobalClases;
import dynagent.common.basicobjects.ReplicaConfiguration;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.utils.Auxiliar;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GenerateSQL;

public class ReplicationEngine implements IReplication {

	private FactoryConnectionDB fcdb;
	
	private String actualSystem;
	private String sufix;
	private String IPCentral;
	private Integer portCentral;
	private int businessCentral;
	
	private ArrayList<Integer> allIdtosReplication = new ArrayList<Integer>();
	private ArrayList<GlobalClases> aGlobalClases = new ArrayList<GlobalClases>();
	//private ArrayList<Integer> aGlobalIndividual = new ArrayList<Integer>();
	private HashMap<Integer,AssociatedIndividual> hAssociatedIdtosKeys = new HashMap<Integer,AssociatedIndividual>();
	private HashMap<Integer,ArrayList<ReplicaConfiguration>> hReplication = new HashMap<Integer,ArrayList<ReplicaConfiguration>>();
	
	public final static String NEW = "NEW";
	public final static String ADD = "ADD";
	public final static String SET = "SET";
	public final static String DEL = "DEL";
	public final static String DEL_IDO = "DEL_IDO";
	
	public ReplicationEngine(FactoryConnectionDB fcdb) throws SQLException, NamingException {
		this.fcdb = fcdb;
		this.createRuler();
	}
	
	public String getActualSystem() {
		return actualSystem;
	}
	public void setActualSystem(String actualSystem) {
		this.actualSystem = actualSystem;
	}

	public String getSufix() {
		return sufix;
	}
	
	public String getIPCentral() {
		return IPCentral;
	}

	public Integer getPortCentral() {
		return portCentral;
	}

	public int getBusinessCentral() {
		return businessCentral;
	}
	
	private void createRuler() throws SQLException, NamingException {
		inicializeConfiguration();
		inicializeGlobalClases();
		inicializeAssociatedIdtos();
		//inicializeGlobalIndividual();
		inicializeAllIdtosReplication();
		inicializeReplication();
	}
	
	private void inicializeConfiguration() throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT " + gSQL.getCharacterBegin() + "value" + gSQL.getCharacterEnd() + 
			", " + gSQL.getCharacterBegin() + "label" + gSQL.getCharacterEnd() +
			" FROM configuration";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				String label = rs.getString(2);
				System.out.println("label " + label);
				if (label.equals("mi_almacen")) {
					System.out.println("mi_almacen");
					actualSystem = rs.getString(1);
					System.out.println("actualSystem " + actualSystem);
				} else if (label.equals("sufix_tienda")) {
					System.out.println("sufix_tienda");
					sufix = rs.getString(1);
					System.out.println("sufix " + sufix);
				} else if (label.equals("IP_Central")) {
					System.out.println("IP_Central");
					IPCentral = rs.getString(1);
					System.out.println("IPCentral " + IPCentral);
				} else if (label.equals("port_Central")) {
					System.out.println("port_Central");
					portCentral = Integer.parseInt(rs.getString(1));
					System.out.println("portCentral " + portCentral);
				} else if (label.equals("business_Central")) {
					System.out.println("business_Central");
					businessCentral = Integer.parseInt(rs.getString(1));
					System.out.println("businessCentral " + businessCentral);
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
	}
	private void inicializeAllIdtosReplication() throws SQLException, NamingException {
		String sql = "SELECT distinct id_to FROM replica_configuration";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idTo = rs.getInt(1);
				allIdtosReplication.add(idTo);
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
	
	private void inicializeGlobalClases() throws SQLException, NamingException {
		String sql = "SELECT user_task, idto_root, centralized FROM globalclases";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer uTask = rs.getInt(1);
				Integer idtoRoot = rs.getInt(2);
				boolean centralized = false;
				String centralizedStr = rs.getString(3);
				if (centralizedStr.equals("Y"))
					centralized = true;
				GlobalClases gl = new GlobalClases(uTask, idtoRoot, centralized);
				aGlobalClases.add(gl);
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

	private void inicializeAssociatedIdtos() throws SQLException, NamingException {
		String sql = "SELECT associated_idto, idto_key, idto_sufix FROM associated_individual";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				Integer idtoKey = rs.getInt(2);
				String idtoSufix = rs.getString(3);
				
				//no funciona split por "."
				/*char[] aC = idtosKey.toCharArray();
				String tmp = "";
				ArrayList<Integer> aIdtosKeyTmp = new ArrayList<Integer>();
				for (int i=0;i<aC.length;i++) {
					char b = '.';
					char a = aC[i];
					if (a==b) {
						aIdtosKeyTmp.add(Integer.parseInt(tmp));
						tmp = "";
					} else
						tmp += a;
				}
				aIdtosKeyTmp.add(Integer.parseInt(tmp));*/
				ArrayList<Integer> aIdtoSufix = Auxiliar.stringToArrayInteger(idtoSufix, ",");
				
				AssociatedIndividual assocInd = new AssociatedIndividual(idtoKey,aIdtoSufix,idto);
				hAssociatedIdtosKeys.put(idto, assocInd);
				System.out.println("assocIdto " + assocInd);
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
	/*private void inicializeGlobalIndividual() throws SQLException, NamingException {
		String sql = "SELECT id_to FROM global_individual";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				aGlobalIndividual.add(idto);
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
	public boolean isGlobalIndividual(int idto) {
		return aGlobalIndividual.contains(idto);
	}*/
	
	private void inicializeReplication() throws SQLException, NamingException {
		String sql = "SELECT id_to, prop_link, idto_sup FROM replica_configuration WHERE local_origin='Y'";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idTo = rs.getInt(1);
				Integer propLink = rs.getInt(2);
				if (rs.wasNull())
					propLink = null;
				Integer idtoSup = rs.getInt(3);
				if (rs.wasNull())
					idtoSup = null;
				
				ArrayList<ReplicaConfiguration> aRep = hReplication.get(idTo);
				if (aRep==null) {
					aRep = new ArrayList<ReplicaConfiguration>();
					hReplication.put(idTo, aRep);
				}
				aRep.add(new ReplicaConfiguration(idTo,propLink,idtoSup,true));
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

	public boolean seReplica(int idto) {
		return allIdtosReplication.contains(idto);
	}
	
	public GlobalClases getGlobalClass(int uTask, HashSet<Integer> idtosRoot) {
		GlobalClases gc = null;
		Iterator it = aGlobalClases.iterator();
		while (it.hasNext()) {
			GlobalClases gcIt = (GlobalClases)it.next();
			if (gcIt.getUserTask()==uTask && (gcIt.getIdtoRoot()==null || idtosRoot.contains(gcIt.getIdtoRoot()))) {
				gc = gcIt;
				break;
			}
		}
		return gc;
	}
	
	public boolean isAssociatedIdto(int idto) {
		return hAssociatedIdtosKeys.containsKey(idto);
	}
	
	public Integer getKeyOfAssociatedIdto(int idto) {
		Integer key = null;
		AssociatedIndividual ai = hAssociatedIdtosKeys.get(idto);
		if (ai!=null)
			key = ai.getIdtoKey();
		return key;
	}
	public ArrayList<Integer> getSufixOfAssociatedIdto(int idto) {
		ArrayList<Integer> sufix = null;
		AssociatedIndividual ai = hAssociatedIdtosKeys.get(idto);
		if (ai!=null)
			sufix = ai.getIdtoSufix();
		return sufix;
	}

	/*public boolean isPartOfAssociatedIdto(int idto, int associatedIdto) {
		System.out.println("idto " + idto + ", associatedIdto " + associatedIdto);
		boolean is = false;
		AssociatedIndividual assocInd = hAssociatedIdtosKeys.get(associatedIdto);
		if (assocInd.getIdtoKey().equals(idto))
			is = true;
		else if (assocInd.getIdtoSufix().contains(idto))
			is = true;
		return is;
	}*/
	public boolean isKeyOrSpecOfAssociatedIdto(int idto, int associatedIdto, IKnowledgeBaseInfo ik) throws NotFoundException, IncoherenceInMotorException {
		System.out.println("idto " + idto + ", associatedIdto " + associatedIdto);
		boolean is = false;
		AssociatedIndividual assocInd = hAssociatedIdtosKeys.get(associatedIdto);
		Integer idtoKey = assocInd.getIdtoKey();
		if (idtoKey.equals(idto))
			is = true;
		/*else {
			HashSet<Integer> hSpec = ik.getSpecialized(idtoKey);
			Iterator itSpec = hSpec.iterator();
			while (itSpec.hasNext() && !is) {
				Integer spec = (Integer)itSpec.next();
				System.out.println("spec " + spec);
				if (spec.equals(idto))
					is = true;
			}
		}*/
		return is;
	}
	public boolean isSufixOfAssociatedIdto(int idto, int associatedIdto) {
		System.out.println("idto " + idto + ", associatedIdto " + associatedIdto);
		boolean is = false;
		AssociatedIndividual assocInd = hAssociatedIdtosKeys.get(associatedIdto);
		if (assocInd.getIdtoSufix().contains(idto))
			is = true;
		return is;
	}
	
	public Set<Integer> getNoModifyDB() throws SQLException, NamingException {
		Set<Integer> idtosNoModify = new HashSet<Integer>();
		String sql = "select id_to from nomodifydb WHERE actual_pc='Y'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				if (rs.wasNull()) idto = null;
				idtosNoModify.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return idtosNoModify;
	}
	
	public boolean getIdtosReplication(int ido, Integer idoNeg, int idto, ArrayList<IPropertyDef> aipd) 
			throws SQLException, NamingException {
		//System.out.println("Inicio de getIdtosReplication");
		//System.out.println("con parametros -> ido " + ido + ", idto " + idto + ", memo " + memo);
		boolean repl = false;
		ArrayList<ReplicaConfiguration> aRep = hReplication.get(idto);
		if (aRep!=null) {
			Iterator it = aRep.iterator();
			while(it.hasNext()) {
				ReplicaConfiguration rep = (ReplicaConfiguration)it.next();
				//System.out.println("Itera " + rep);
				repl = compruebaIdtosSup(ido, idoNeg, idto, rep, aipd);
				if (repl)
					break;
			}
		}
		//System.out.println("Fin de getIdtosReplication");
		return repl;
	}
	
	private boolean compruebaIdtosSup(int ido, Integer idoNeg, int idto, ReplicaConfiguration rep, ArrayList<IPropertyDef> aipd) throws SQLException, NamingException {
		boolean repl = false;
		Integer idtoSup = rep.getIdtoSup();
		if (idtoSup==null)
			repl = true;
		else {
			int propLink = rep.getPropLink();
			//buscar en ipd si existe valor para este fact
			Integer idtoSupReal = null;
			Integer idoSup = null;
			if (aipd!=null) {
				IPropertyDef ipd = findIdtoByValNumProp(ido, propLink, idoNeg, aipd);
				if (ipd!=null) {
					idtoSupReal = ipd.getIDTO();
					idoSup = ipd.getIDO();
				}
			}
			if (idtoSupReal!=null) {
				//si coincide valCls del fact
				if (idtoSupReal.equals(idtoSup))
					repl = true;
			} else if (ido>0) {
				//buscarlo en BD
				idoSup = compruebaIdtoSup(ido, idto, propLink, idtoSup);
				repl = idoSup!=null;
			}
			if (repl) {
				ArrayList<ReplicaConfiguration> aRep2 = hReplication.get(idtoSup);
				if (aRep2!=null) {
					Iterator it2 = aRep2.iterator();
					while(it2.hasNext()) {
						ReplicaConfiguration rep2 = (ReplicaConfiguration)it2.next();
						repl = compruebaIdtosSup(idoSup, null, idtoSup, rep2, aipd);
						if (repl)
							break;
					}
				}
			}
		}
		return repl;
	}
	
	
	private Integer compruebaIdtoSup(int ido, int idto, int propLink, int idtoSup) throws SQLException, NamingException {
		Integer idoSup = null;
		String sql2 = "SELECT id_o FROM o_datos_atrib " +
				"WHERE val_num=" + ido + " AND property=" + propLink + " AND id_to=" + idtoSup;
		ConnectionDB con2 = fcdb.createConnection(true);
		Statement st2 = null;
		ResultSet rs2 = null;
		try {
			st2 = con2.getBusinessConn().createStatement();
			//System.out.println("antes de la query");
			//System.out.println(sql2);
			rs2 = st2.executeQuery(sql2);
			if (rs2.next()) {
				idoSup = rs2.getInt(1);
			}
			//System.out.println("despues de la query");
		} finally {
			if (rs2 != null)
				rs2.close();
			if (st2 != null)
				st2.close();
			if (con2!=null)
				fcdb.close(con2);
		}
		return idoSup;
	}
	private IPropertyDef findIdtoByValNumProp(int valNum, int prop, Integer valNumNeg, 
			ArrayList<IPropertyDef> aipd) {
		for (int i=0;i<aipd.size();i++) {
			IPropertyDef ipd = aipd.get(i);
			//System.out.println("ipd " + ipd + ", prop " + prop + ", valNum " + valNum);
			String value = ipd.getVALUE();
			if (ipd.getPROP()==prop && value!=null) {
				Integer valueInt = Integer.parseInt(value);
				if (valueInt.equals(valNum))
					return ipd;
				else if (valNumNeg!=null && valueInt.equals(valNumNeg))
					return ipd;
			}
		}
		return null;
	}
}
