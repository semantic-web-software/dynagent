package dynagent.server.ejb;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.communication.docServer;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.process.IAsigned;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.xml.QueryXML;
import dynagent.common.utils.AliasComponents;
import dynagent.common.utils.IAlias;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.QueryConstants;
import dynagent.server.gestorsDB.GenerateSQL;

public class ServerEngine implements IKnowledgeBaseInfoServer, Serializable {

	private static final long serialVersionUID = 7774473564451799726L;
	//cargar:
		//mapa dataProperties idProp ServerDataProperty(tipo)
		//mapa objectProperties idProp ServerObjectProperty(isStruct inversa)
		//mapa properties name
		//mapa clases idto name
		//mapa idto mapa prop cardMax
		//mapa specializados idto arrayIdtos
		//mapa padresSpecializados idto arrayIdtos
	
	private HashMap<Integer,ServerDataProperty> hmDataPropXIdProp=new HashMap<Integer,ServerDataProperty>();
	private HashMap<Integer,ServerObjectProperty> hmObjectPropXIdProp=new HashMap<Integer,ServerObjectProperty>();
	private HashMap<Integer,String> hmNamePropXIdProp=new HashMap<Integer,String>();
	private HashMap<Integer,Integer> hmCategoryXIdProp=new HashMap<Integer,Integer>();
	private HashMap<String,Integer> hmCardMaxXIdtoProp=new HashMap<String,Integer>();
	private HashMap<String,Integer> hmCardMinXIdtoProp=new HashMap<String,Integer>();
	private HashMap<Integer,String> hmNameClassXIdto=new HashMap<Integer,String>();
	private HashMap<Integer,ArrayList<Integer>> hmSpecialized=new HashMap<Integer,ArrayList<Integer>>();
	private HashMap<Integer,HashSet<Integer>> hmIdsPropXClass=new HashMap<Integer,HashSet<Integer>>();
	private HashMap<Integer,ArrayList<Integer>> hmSuperiors=new HashMap<Integer,ArrayList<Integer>>();
	//private HashSet<Integer> hClassesIndex=new HashSet<Integer>();
	//private HashSet<Integer> hClassesIndexWithSpecialized=new HashSet<Integer>();
	//private HashSet<Integer> hManualClasses=new HashSet<Integer>();
	//private HashMap<Integer,MiEmpresa> hMiEmpresa= new HashMap<Integer, MiEmpresa>();
	//private Integer idoMiEmpresa = null;
	private IAlias alias;
	//private HashMap<Integer,String> hReportFormat= new HashMap<Integer,String>();

	private FactoryConnectionDB fcdb;
	
	public ServerEngine(FactoryConnectionDB fcdb) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		this.fcdb = fcdb;
		this.createRuler();
	}
	
	public String toString() {
		String str = "\nSERVER ENGINE\n";
		
		str += "\nhmDataPropXIdProp\n";
		Iterator it = hmDataPropXIdProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			ServerDataProperty sdP = hmDataPropXIdProp.get(key);
			str += "PROP " + key + ", DATATYPE " + sdP.getDataType() + "\n";
		}
		str += "\nhmObjectPropXIdProp\n";
		it = hmObjectPropXIdProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			ServerObjectProperty soP = hmObjectPropXIdProp.get(key);
			str += "PROP " + key + ", PROP_INVERSE " + soP.getIdPropInverse() + "\n";
		}
		str += "\nhmNamePropXIdProp\n";
		it = hmNamePropXIdProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			String name = hmNamePropXIdProp.get(key);
			str += "PROP " + key + ", NAME " + name + "\n";
		}
		str += "\nhmCategoryXIdProp\n";
		it = hmCategoryXIdProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			Integer category = hmCategoryXIdProp.get(key);
			str += "PROP " + key + ", CATEGORY " + category + "\n";
		}
		str += "\nhmCardMaxXIdtoProp\n";
		it = hmCardMaxXIdtoProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			Integer cardMax = hmCardMaxXIdtoProp.get(key);
			str += "IDTO " + key + ", CARD_MAX " + cardMax + "\n";
		}
		str += "\nhmCardMinXIdtoProp\n";
		it = hmCardMinXIdtoProp.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			Integer cardMin = hmCardMinXIdtoProp.get(key);
			str += "IDTO " + key + ", CARD_MIN " + cardMin + "\n";
		}
		str += "\nhmNameClassXIdto\n";
		it = hmNameClassXIdto.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			String nameClass = hmNameClassXIdto.get(key);
			str += "IDTO " + key + ", NAME_CLASS " + nameClass + "\n";
		}
		str += "\nhmSpecialized\n";
		it = hmSpecialized.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			ArrayList<Integer> specialized = hmSpecialized.get(key);
			str += "IDTO " + key + ", SPECIALIZED" + Auxiliar.arrayIntegerToString(specialized, ",") + "\n";
		}
		return str.toString();
	}
	//clases auxiliares//////////////
	private class ServerDataProperty extends Object {
		private Integer dataType;

		public ServerDataProperty(Integer dataType) {
			this.dataType = dataType;
		}

		public Integer getDataType() {
			return dataType;
		}
	}
	private class ServerObjectProperty extends Object {
		private Integer idPropInverse;

		public ServerObjectProperty(Integer idPropInverse) {
			this.idPropInverse = idPropInverse;
		}

		public Integer getIdPropInverse() {
			return idPropInverse;
		}
	}
	////////////////////////////////
	
	

	private void createRuler() throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		inicializeProp();
		inicializeInstances();
		inicializeClasses();
		inicializeSpecialized();
		//inicializeIndex();
		//inicializeManualClasses();
		//inicializeMiEmpresa();
		inicializeAlias();
		//inicializeReportFormat();
	}
	
	private void inicializeAlias() throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		ArrayList<Alias> listAlias = new ArrayList<Alias>();//MetaData.getAlias(fcdb, null); Si se activa esto seria un problema para sharedBean ya que tendria los alias de otra base de datos
		setListAlias(listAlias);
	}
	public void setListAlias(ArrayList<Alias> listAlias) {
		alias=new AliasComponents(this,listAlias);
	}

	/*public void inicializeMiEmpresa() throws SQLException, NamingException {
		hMiEmpresa = new HashMap<Integer, MiEmpresa>();
		String sql = "SELECT id_o, nombre, nif, direccion, codigo_postal, localidad, provincia, pais, " +
				"email, telefono, fax FROM mi_empresa_info";

		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer ido = rs.getInt(1);
				if (!rs.wasNull()) {
					String nombre = rs.getString(2);
					String nif = rs.getString(3);
					String direccion = rs.getString(4);
					String codigoPostal = rs.getString(5);
					String localidad = rs.getString(6);
					String provincia = rs.getString(7);
					String pais = rs.getString(8);
					String email = rs.getString(9);
					String telefono = rs.getString(10);
					String fax = rs.getString(11);
					MiEmpresa empr = new MiEmpresa(nombre, nif, direccion, codigoPostal, telefono, fax, email, localidad, provincia, pais);
					hMiEmpresa.put(ido, empr);
					idoMiEmpresa = ido;
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
	}*/
	/*private void inicializeReportFormat() throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "select " + gSQL.getCharacterBegin() + "tableId" + gSQL.getCharacterEnd() + ", rdn " +
				"from report_format";

		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer tableId = rs.getInt(1);
				String rdn = rs.getString(2);
				hReportFormat.put(QueryConstants.getIdo(tableId, Constants.IDTO_REPORT_FORMAT), rdn);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
	}*/

	/*private void inicializeIndex() throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String sql = "SELECT idto from clases where name in(select distinct dominio FROM " + gSQL.getCharacterBegin() + "formato_índice" + gSQL.getCharacterEnd() + ")";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				hClassesIndex.add(idto);
				hClassesIndexWithSpecialized.add(idto);
				hClassesIndexWithSpecialized.addAll(this.getSpecialized(idto));
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
	}*/
	/*private void inicializeManualClasses() throws SQLException, NamingException {
		String sql = "SELECT distinct idto FROM access where prop=" + Constants.IdPROP_RDN + 
			" AND dennied=0 and accesstype=" + Constants.ACCESS_SET + " and priority>10";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				hManualClasses.add(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
	}*/
	private void inicializeProp() throws SQLException, NamingException {
		String sql = "SELECT id, rdn, cat, valuecls, id_inversa FROM v_propiedad";
		
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idProp = rs.getInt(1);
				String name = rs.getString(2);
				hmNamePropXIdProp.put(idProp, name);
				Integer cat = rs.getInt(3);
				hmCategoryXIdProp.put(idProp, cat);
				Category category = new Category(cat);
				if (category.isDataProperty()) {
					//DataProperty
					Integer valueCls = rs.getInt(4);
					ServerDataProperty dP = new ServerDataProperty(valueCls);
					hmDataPropXIdProp.put(idProp, dP);
				} else {
					//ObjectProperty
					Integer inverse = rs.getInt(5);
					if (rs.wasNull())
						inverse = null;
					ServerObjectProperty oP = new ServerObjectProperty(inverse);
					hmObjectPropXIdProp.put(idProp, oP);
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
	private void inicializeInstances() throws SQLException, NamingException {
		String sql = "SELECT idto, property, qmax, op, qmin FROM instances";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				Integer idProp = rs.getInt(2);
				String op = rs.getString(4);
				
				HashSet<Integer> idsProp = hmIdsPropXClass.get(idto);
				if (idsProp==null) {
					idsProp = new HashSet<Integer>();
					hmIdsPropXClass.put(idto, idsProp);
				}
				idsProp.add(idProp);
				hmIdsPropXClass.put(idto, idsProp);

				if (op!=null && op.equals("CAR")) {
					Integer qMax = rs.getInt(3);
					if (!rs.wasNull())
						hmCardMaxXIdtoProp.put(idto + "_" + idProp, qMax);
					Integer qMin = rs.getInt(5);
					if (!rs.wasNull())
						hmCardMinXIdtoProp.put(idto + "_" + idProp, qMin);
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
	private void inicializeClasses() throws SQLException, NamingException {
		String sql = "SELECT id, rdn FROM clase";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				String name = rs.getString(2);
				hmNameClassXIdto.put(idto, name);
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
	private void inicializeSpecialized() throws SQLException, NamingException {
		String sql = "SELECT id_to, id_to_padre FROM t_herencias";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				Integer idtoParent = rs.getInt(2);
				
				ArrayList<Integer> specialized = hmSpecialized.get(idtoParent);
				if (specialized==null) {
					specialized = new ArrayList<Integer>();
					hmSpecialized.put(idtoParent, specialized);
				}
				specialized.add(idto);
				
				ArrayList<Integer> superiors = hmSuperiors.get(idto);
				if (superiors==null) {
					superiors = new ArrayList<Integer>();
					hmSuperiors.put(idto, superiors);
				}
				superiors.add(idtoParent);
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

	
	
	
	//metodos usados de IKnowledgeBaseInfo///////
	public boolean isObjectProperty(int idProp)  {
		return hmObjectPropXIdProp.get(idProp)!=null;
	}
	public boolean isDataProperty(int idProp)  {
		return hmDataPropXIdProp.get(idProp)!=null;
	}
	public Integer getPropertyInverse(int idProp) {
		Integer inverse = null;
		ServerObjectProperty sop = hmObjectPropXIdProp.get(idProp);
		if (sop!=null)
			inverse = sop.getIdPropInverse();
		return inverse;
			
	}
	public Integer getDatatype(int idProp) throws NotFoundException {
		Integer dataType = null;
		ServerDataProperty sdp = hmDataPropXIdProp.get(idProp);
		if (sdp!=null) {
			dataType = sdp.getDataType();
			if (dataType==null)
				throw new NotFoundException("Property " + idProp + " no encontrada en motor");
		}
		return dataType;
	}
	public Category getCategory(int idProp) throws NotFoundException {
		Category cat = null;
		Integer category = hmCategoryXIdProp.get(idProp);
		if (category!=null) {
			cat = new Category(category);
			if (cat==null)
				throw new NotFoundException("Categoria de property " + idProp + " no encontrada en motor");
		}
		return cat;
	}
	public Integer getIdClass(String name) throws NotFoundException {
		Integer idClass = null;
		boolean encontrado = false;
		Iterator it = hmNameClassXIdto.keySet().iterator();
		while (it.hasNext() && !encontrado) {
			Integer key = (Integer)it.next();
			String nameIter = hmNameClassXIdto.get(key);
			if (nameIter.equals(name)) {
				idClass = key;
				encontrado = true;
			}
		}
		if (!encontrado)
			throw new NotFoundException("Clase " + name + " no encontrada en motor");
		return idClass;
	}
	public Integer getIdProperty(String nameProp) throws NotFoundException {
		Integer idProp = null;
		boolean encontrado = false;
		Iterator it = hmNamePropXIdProp.keySet().iterator();
		while (it.hasNext() && !encontrado) {
			Integer key = (Integer)it.next();
			String nameIter = hmNamePropXIdProp.get(key);
			if (nameIter.equals(nameProp)) {
				idProp = key;
				encontrado = true;
			}
		}
		if (!encontrado)
			throw new NotFoundException("Property " + nameProp + " no encontrada en motor");
		return idProp;
	}
	//al devolver name no se tienen en cuenta los alias
	public String getClassName(int idClass) throws NotFoundException {
		String name = hmNameClassXIdto.get(idClass);
		if (name==null)
			throw new NotFoundException("Clase " + idClass + " no encontrada en motor");
		return name;
	}
	public String getPropertyName(int idProp) throws NotFoundException {
		String name = hmNamePropXIdProp.get(idProp);
		if (name==null)
			throw new NotFoundException("Property " + idProp + " no encontrada en motor");
		return name;
	}
	public boolean isSpecialized(int idto, int posSuperior) {
		ArrayList<Integer> aSpec = hmSpecialized.get(posSuperior);
		boolean encontrado = false;
		if (aSpec!=null) {
			Iterator it = aSpec.iterator();
			while (it.hasNext() && !encontrado) {
				Integer spec = (Integer)it.next();
				if (spec.equals(idto))
					encontrado = true;
			}
		}
		return encontrado;
	}
	public HashSet<Integer> getSpecialized(int id) {
		HashSet<Integer> spec = new HashSet<Integer>();
		if(hmSpecialized.get(id)!=null)
			spec.addAll(hmSpecialized.get(id));
		return spec;
	}
	
	public MiEmpresa getMiEmpresa(Integer tableId) throws SQLException, NamingException {
		GenerateSQL gSQL = new GenerateSQL(fcdb.getGestorDB());
		String cB = gSQL.getCharacterBegin();
		String cE = gSQL.getCharacterEnd();
		
		MiEmpresa empr = null;
		String sql = "select mi.nombre, mi." + cB + "NIF-CIF-VAT" + cE + ", mi." + cB + "dirección" + cE + ", mi." + cB + "código_postal" + cE + ", " +
				"loc.rdn, prov.rdn, pa.rdn, mi.email, mi." + cB + "teléfono" + cE + ", mi.fax from mi_empresa as mi " + 
				"left join localidad as loc on (loc." + cB + "tableId" + cE + "=mi.localidad) " + 
				"left join provincia as prov on (prov." + cB + "tableId" + cE + "=mi." + cB + "provinciaPROVINCIA" + cE + ") " + 
				"left join " + cB + "país" + cE + " as pa on (pa." + cB + "tableId" + cE + "=mi." + cB + "país" + cE + ") " + 
				"where mi." + cB + "tableId" + cE + "=" + tableId;

		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				String nombre = rs.getString(1);
				String nif = rs.getString(2);
				String direccion = rs.getString(3);
				String codigoPostal = rs.getString(4);
				String localidad = rs.getString(5);
				String provincia = rs.getString(6);
				String pais = rs.getString(7);
				String email = rs.getString(8);
				String telefono = rs.getString(9);
				String fax = rs.getString(10);
				empr = new MiEmpresa(nombre, nif, direccion, codigoPostal, telefono, fax, email, localidad, provincia, pais);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		return empr;
	}
	
	/*public MiEmpresa getMiEmpresa(Integer ido) {
		MiEmpresa empr = null;
		if (ido!=null)
			empr = hMiEmpresa.get(ido);
		else if (idoMiEmpresa!=null){
			empr = hMiEmpresa.get(idoMiEmpresa);
		}
		return empr;
	}*/

	public boolean checkCoherenceObject(int ido, Integer userRol, String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		return false;
	}

	public Integer createPrototype(int idto, int level, Integer userRol, String user, Integer usertask, Session sess) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public void deleteObject(int id, int idto, String rdn, Integer userRol, String user, Integer usertask, Session sessionPadre) throws NotFoundException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		// TODO Auto-generated method stub
		
	}

	public access getAccessOverObject(Integer id, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<Property> getAllPropertyIterator(Integer ido, int idto, Integer userRol, String user, Integer usertask, Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getAtributteGroup(int idProp) {
		// TODO Auto-generated method stub
		return null;
	}

	

	public Integer getClassOf(int ido) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<ColumnProperty> getColumnProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashSet<Integer> getDirectSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return null;
	}

	public HashSet<Integer> getDirectSuperior(int idto) throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Groups> getGroupsProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public HashSet<Integer> getIndividualsOfLevel(int idto, int level) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getLevelOf(int ido) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<CardMed> getListCM() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getMaxPropertyCardinalityOfClass(int idto, int idprop) {
		return hmCardMaxXIdtoProp.get(idto + "_" + idprop);
	}

	public Integer getMinPropertyCardinalityOfClass(int idto, int idprop) {
		return hmCardMinXIdtoProp.get(idto + "_" + idprop);
	}

	public ArrayList<OrderProperty> getOrderProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Property getProperty(Integer id, int idto, int idProp, Integer userRol, String user, Integer usertask, Session s) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryXML getQueryXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRdn(int ido) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Integer> getSpecializedFilters(int ido, Integer userRol, String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<Integer> getSuperior(int idto) throws NotFoundException, IncoherenceInMotorException {
		HashSet<Integer> sups = new HashSet<Integer>();
		if(hmSuperiors.get(idto)!=null) {
			sups.addAll(hmSuperiors.get(idto));
		}
		Iterator<Integer> it = sups.iterator();
		return it;
	}

	public instance getTreeObject(int id, Integer userRol, String user, Integer userTask, Session sess) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public HashMap<Integer, ArrayList<UserAccess>> getUsertaskOperationOver(int id, String user) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasProperty(int idto, int idprop) {
		boolean hasProp = false;
		HashSet<Integer> idsProp = hmIdsPropXClass.get(idto);
		if (idsProp!=null) {
			if (idsProp.contains(idprop))
				hasProp = true;
		}
		return hasProp;
	}

	public boolean isIDClass(int id) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRangeCompatible(int iddominio, int idProp, int idrange) throws IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUnit(int cls) throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return false;
	}


	public void loadMetaData() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		// TODO Auto-generated method stub
	}

	public HashSet<Integer> loadNewData(ArrayList<IPropertyDef> instances, Integer userRol, String user, Integer userTask, Session sess) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAsigned(IAsigned asigned) {
		// TODO Auto-generated method stub
	}

	public void setLock(int ido, boolean locked, String user, Session sessionPadre) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
	}

	public void setServer(docServer server) {
		// TODO Auto-generated method stub
	}

	public void setValue(int ido, int idto, int idProp, Value oldValue, Value newValue, Integer userRol, String user, Integer usertask, Session s) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		// TODO Auto-generated method stub
	}

	public int specializeIn(int id, int idtoSpecialized) {
		// TODO Auto-generated method stub
		return 0;
	}
	/////////////////////////////////////////////
	
	public ArrayList<EssentialProperty> getEssentialProperties(){
		return null;
	}
	
	public void setEssentialProperties(ArrayList<EssentialProperty> essentialProperties){
		
	}

	public String getAliasOfGroup(int group, String nameGroup, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		return this.alias.getLabelGroup(group, nameGroup, usertask);
	}
	
	public String getAliasOfClass(int idto, Integer usertask) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, CardinalityExceedException {
		if(isSpecialized(idto, Constants.IDTO_UTASK))
			return this.alias.getLabelUtask(idto);
		else return this.alias.getLabelClass(idto,usertask);
	}

	public String getAliasOfProperty(int idto, int idProp, Integer usertask) throws NotFoundException {
		return this.alias.getLabelProp(idProp,idto,null, usertask);
	}

	public void setValue(int ido, int idto, int idProp, LinkedList<Value> oldValues, LinkedList<Value> newValues, Integer userRol, String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		
	}

	public ArrayList<ListenerUtask> getListenerUtasks() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isGenericFilter(int ido) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return false;
	}

	public void setListenerUtasks(ArrayList<ListenerUtask> listenerUtasks) {
		// TODO Auto-generated method stub
		
	}

	public boolean isPointed(int ido, int idto) throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean existInMotor(Integer id) {
		// TODO Auto-generated method stub
		return false;
	}

	

	public docServer getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isCompatibleWithFilter(int ido, instance instFilter, Integer userRol, String user, Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		return false;
	}

	

	/*public HashSet<Integer> getClassesIndex() {
		return hClassesIndex;
	}
	public void putClassesIndex(Integer clase) {
		hClassesIndex.add(clase);
		//System.out.println("Tras put " + hClassesIndex);
	}*/
	/*public HashSet<Integer> getClassesIndexWithSpecialized() {
		return hClassesIndexWithSpecialized;
	}*/
	/*public void putClassesIndexWithSpecialized(Integer clase) {
		hClassesIndexWithSpecialized.add(clase);
		//System.out.println("Tras put " + hClassesIndex);
	}*/
	/*public void deleteClassesIndex(HashSet<Integer> idtos) throws SQLException, NamingException {
		//solo elimina si no hay indice para esa clase
		String sql = "SELECT distinct id_to FROM s_index " +
				"WHERE id_to IN(" + Auxiliar.hashSetIntegerToString(idtos, ",") + ")";
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = fcdb.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				idtos.remove(idto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				fcdb.close(con);
		}
		hClassesIndex.removeAll(idtos);
		//TODO se puede mejorar 
		//viendo si se puede borrar de la lista de los especializados si no tiene un padre con indice definido
		//y viendo los especializados que se pueden eliminar de la lista
		//no es importante porque este mapa solo se usa para saber si no tiene indice no procesar
		
		//System.out.println("Tras delete " + hClassesIndex);
	}*/

	/*public HashSet<Integer> getManualClasses() {
		return hManualClasses;
	}*/

	public Integer getPropertyLength(int prop, Integer idto, Integer utask) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPropertyMask(int prop, Integer idto, Integer utask) throws NotFoundException {
		// TODO Auto-generated method stub
		return null;
	}
	/*public String getFormat(Integer ido) {
		return hReportFormat.get(ido);
	}*/

	public HashSet<Integer> getAllIDsPropertiesOfClass(int idto) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRdnIfExistInRuler(int ido) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer setRange(int ido, int idto, int idProp, int valueCls, Integer userRol, String user, Integer userTask, int depth, Session sess) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Session getDefaultSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public Session getRootSession() {
		// TODO Auto-generated method stub
		return null;
	}

	public void loadIndividual(int ido, int idto, int depth, boolean lock, boolean lastStructLevel, Integer userRol, String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		
	}

	public void loadIndividual(HashMap<Integer, HashSet<Integer>> idtoIdos, int depth, boolean lock, boolean lastStructLevel, Integer userRol, String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		
	}

	public IKnowledgeBaseInfo doClone() throws NotFoundException, IncoherenceInMotorException, EngineException {
		// TODO Auto-generated method stub
		return null;
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isDispose() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	public String getUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public IQuestionListener getQuestionListener() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setQuestionListener(IQuestionListener listenerQuestion) {
		// TODO Auto-generated method stub
		
	}

	public boolean isAbstract(int id) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void requestInformation(Integer ido, Integer idProp, Session session)
			throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBatchListener(IBatchListener batchListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBatchListener(IBatchListener batchListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public HashSet<Integer> getClassifiedIdtos(int idto, int idProp, Value value, String user, Integer userRol, Integer idtoUserTask, Session ses){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLock(ArrayList<Integer> idos, boolean locked, String user,
			Session sessionPadre) throws NotFoundException,
			ApplicationException, SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException,
			DataErrorException, IncoherenceInMotorException,
			IncompatibleValueException, CardinalityExceedException,
			OperationNotPermitedException, ParseException, SQLException,
			NamingException, JDOMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer createFilter(int idto, Integer userRol, String user,
			Integer usertask, int depth, Session sessionPadre)
			throws NotFoundException, IncoherenceInMotorException,
			ApplicationException, IncompatibleValueException,
			CardinalityExceedException, SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException, SQLException,
			NamingException, DataErrorException, JDOMException, ParseException,
			OperationNotPermitedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void completeFilterLevels(int ido, int idto, Integer userRol,
			Integer userTask, int levels, Session sessionPadre)
			throws SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException,
			ApplicationException, DataErrorException, NotFoundException,
			IncoherenceInMotorException, IncompatibleValueException,
			CardinalityExceedException, OperationNotPermitedException,
			ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPrintRules(boolean printRules) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isPrintRules() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean sendEmail(Integer idoUserTaskReport,
			Integer idtoUserTaskReport, EmailRequest emailRequest, boolean showError)
			throws SystemException, RemoteSystemException,
			CommunicationException, InstanceLockedException,
			ApplicationException, DataErrorException, NotFoundException,
			IncoherenceInMotorException, IncompatibleValueException,
			CardinalityExceedException, OperationNotPermitedException,
			ParseException, SQLException, NamingException, JDOMException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ArrayList<Integer> getGlobalUtasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGlobalUtasks(ArrayList<Integer> globalUtasks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNoticeListener(INoticeListener noticeListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeNoticeListener(INoticeListener noticeListener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLocalServer(docServer server) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public docServer getLocalServer() {
		// TODO Auto-generated method stub
		return null;
	}

}
