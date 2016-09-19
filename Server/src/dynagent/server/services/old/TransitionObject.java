package dynagent.server.services.old;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Index;
import dynagent.common.basicobjects.IndexFilter;
import dynagent.common.basicobjects.O_Reg_Instancias_Index;
import dynagent.common.basicobjects.Roles;
import dynagent.common.basicobjects.UsuarioRoles;
import dynagent.common.basicobjects.Usuarios;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.properties.values.StringValue;
import dynagent.common.utils.Auxiliar;
import dynagent.server.ejb.Asigned;
import dynagent.server.ejb.AuxiliarModel;
import dynagent.server.ejb.ConnectionDB;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.IKnowledgeBaseInfoServer;
import dynagent.server.gestorsDB.GenerateSQL;
import dynagent.server.replication.IReplication;
import dynagent.server.replication.ReplicationEngine;
import dynagent.server.services.IndexFilterFunctions;
import dynagent.server.services.InstanceService;
import dynagent.server.services.querys.AuxiliarQuery;

public class TransitionObject {

	private FactoryConnectionDB factConnDB = null;
	
	private IKnowledgeBaseInfo ik;
	
	private IReplication ir;
	
	private Changes changes;
	
	private InstanceService m_IS;
	
	private Integer userRol;
	
	private String user;
	
	private boolean debugMode;
	
	private GenerateSQL generateSQL;
	
	private int id = 0;
	
	private final static int maxIntentosLock = 3;
	
	private HashMap<Integer,Integer> idoFPropStructQueLoApunta = new HashMap<Integer,Integer>();
	private HashMap<Integer,IdoObject> idoFicticioReal = new HashMap<Integer,IdoObject>();
	private HashMap<Integer,String> idoRealRdn = new HashMap<Integer,String>();
	private HashSet<Integer> idosContribNoNew = new HashSet<Integer>();
	
	private Set<Integer> idtosNoModify = null;
	
	private ArrayList<Roles> aNewRoles = new ArrayList<Roles>();
	private HashMap<Integer,Usuarios> aNewUsuarios = new HashMap<Integer,Usuarios>();
	private ArrayList<UsuarioRoles> aNewUsuarioRoles = new ArrayList<UsuarioRoles>();
	
	private HashMap<Integer,Index> aNewIndex = new HashMap<Integer,Index>();
	
	//variables para controlar la cardinalidad
	private HashMap<Integer,HashMap<Integer,Integer>> countCardinality = new HashMap<Integer,HashMap<Integer,Integer>>();
	private HashMap<Integer,Integer> idoXIdto = new HashMap<Integer,Integer>();
	private HashMap<Integer,String> propXTable = new HashMap<Integer,String>();
	
	//para replica
	//private ArrayList<Integer> idtosReplica = new ArrayList<Integer>();
	private boolean seHaceReplica;
	private HashMap<Integer,Boolean> hIdosReplication = new HashMap<Integer,Boolean>();

	private Integer idtoMiEmpresa;
	private Integer idPropMiEmpresa;
	private HashMap<Integer,Integer> hIdoMiEmpresa = new HashMap<Integer, Integer>();
	
	public class IdoObject extends Object {
		private int ido;
		private Object object;
		
		public IdoObject(Integer ido, Integer object) {
			this.ido = ido;
			this.object = object;
		}
		public IdoObject(Integer ido, String object) {
			this.ido = ido;
			this.object = object;
		}
		public IdoObject(Integer ido, O_Reg_Instancias_Index object) {
			this.ido = ido;
			this.object = object;
		}
		public Integer getIdo() {
			return ido;
		}
		public Object getObject() {
			return object;
		}
		
		public void setObject(String object) {
			this.object = object;
		}
		public boolean containedByIdoObject(ArrayList<IdoObject> aIdoObject) {
			boolean contains = false;
			Iterator it = aIdoObject.iterator();
			while (it.hasNext()) {
				IdoObject idoObject = (IdoObject)it.next();
				if (this.object!=null && idoObject.getObject()!=null && 
						this.object.equals(idoObject.getObject()) && this.ido==idoObject.getIdo()) {
					contains = true;
					break;
				}
			}
			return contains;
		}
		/*public boolean containedByIdo(ArrayList<IdoObject> aIdoObject) {
			boolean contains = false;
			Iterator it = aIdoObject.iterator();
			while (it.hasNext()) {
				IdoObject idoObject = (IdoObject)it.next();
				if (this.ido==idoObject.getIdo()) {
					contains = true;
					break;
				}
			}
			return contains;
		}*/
		public String toString() {
			String dev = "IDO: " + ido + ", OBJECT: " + object;
			return dev;
		}
	}
	private class IdoObjectDelete extends Object {
		private Integer idto;
		private HashSet<Integer> idosSuperiores;
		private Boolean delete;
		
		public IdoObjectDelete(Integer idto, HashSet<Integer> idosSuperiores) {
			this.idto = idto;
			this.idosSuperiores = idosSuperiores;
			this.delete = null;
		}
		public HashSet<Integer> getIdosSuperiores() {
			return idosSuperiores;
		}
		public void setIdosSuperiores(HashSet<Integer> idosSuperiores) {
			this.idosSuperiores = idosSuperiores;
		}
		public Integer getIdto() {
			return idto;
		}
		public Boolean isDelete() {
			return delete;
		}
		public void setDelete(Boolean delete) {
			this.delete = delete;
		}
		public String toString() {
			String dev = "IDTO: " + idto;
			if (idosSuperiores!=null) {
				String tmpIdosSup = "";
				Iterator it = idosSuperiores.iterator();
				while (it.hasNext()) {
					if (tmpIdosSup.length()>0)
						tmpIdosSup += ",";
					tmpIdosSup += (Integer)it.next();
				}
				dev += ", IDOS_SUPERIORES: " + tmpIdosSup; 
			}
			dev += ", DELETE: " + delete;
			return dev;
		}
	}

	public TransitionObject(FactoryConnectionDB factConnDB,	IKnowledgeBaseInfo ik, IReplication ir, InstanceService m_IS, 
			Integer userRol, String user, boolean debugMode, GenerateSQL generateSpecialCode) {
		this.factConnDB = factConnDB;
		this.ik = ik;
		this.ir = ir;
		this.m_IS = m_IS;
		this.userRol = userRol;
		this.user = user;
		this.debugMode = debugMode;
		this.generateSQL = generateSpecialCode;
		changes = new Changes();
	}
	
	public Changes getChanges() {
		return changes;
	}

	private boolean containsFact(Integer ido, int idProp, String value, ArrayList<IPropertyDef> aipd) {
		boolean contains = false;
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipd = (IPropertyDef)it.next();
			int idPropFact = ipd.getPROP();
			if (idPropFact==idProp) {
				Integer idoFact = ipd.getIDO();
				String valueFact = ipd.getVALUE();
				if (idoFact!=null && ido!=null && idoFact.equals(ido) && StringUtils.equals(valueFact, value)) {
					contains = true;
					break;
				}
			}
		}
		return contains;
	}
	//procesa inversas
	//deletes
	//crea mapa ido-propQueLoApunta para 
	private boolean preProcess(ArrayList<IPropertyDef> aipd, HashSet<Integer> idosIncr, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, HashMap<Integer,String> idoDelDestination) 
			throws DataErrorException, SQLException, NamingException, NotFoundException {
		boolean hasEvol = false;
		ArrayList<IPropertyDef> aInvipd = new ArrayList<IPropertyDef>();
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipd = (IPropertyDef)it.next();
			Integer ido = ipd.getIDO();
			int idProp = ipd.getPROP();
			if (idPropMiEmpresa!=null && idProp==idPropMiEmpresa)
				hIdoMiEmpresa.put(ido,Integer.parseInt(ipd.getVALUE()));
			
			if (ipd.isIncremental())
				idosIncr.add(ido);
			//poner en todos los facts existia_bd a true
			((FactInstance)ipd).setExistia_BD(true);
			//ver si estan las inversas en los NEW, DEL y SET
			if (ipd.getOrder()==action.NEW || ipd.getOrder()==action.SET || ipd.getOrder()==action.DEL) {
				if (ik.isObjectProperty(idProp)) {
					String value = ipd.getVALUE();
					if (ido!=null && value!=null) {
						Integer idPropInverse = ik.getPropertyInverse(idProp);
						if (ipd.getOrder()==action.NEW || ipd.getOrder()==action.DEL) {
							//ver si está su inversa en el array de Facts, si no está añadirla
							Integer valueCls = ipd.getVALUECLS();
							if (idPropInverse!=null && ik.hasProperty(valueCls, idPropInverse)) {
								createFactInArray(valueCls, Integer.parseInt(value), idPropInverse, String.valueOf(ido), 
										ipd.getIDTO(), ipd.getOP(), ipd.getCLASSNAME(), ipd.getOrder(), aipd, aInvipd);
							}
						} else if (ipd.getOrder()==action.SET) {
							//lanzar una excepcion no es correcto porque puede que no este controlado desde el motor
							//System.err.println("No se puede hacer una operación SET sobre el Fact " + ipd.toString() + 
								//" porque su idProp tiene property inversa");
							if (idPropInverse!=null) {
								IPropertyDef initFact = ipd.getInitialValues();
								if (initFact!=null) {
									//upd AB -> AC con p1
									//1. new CA con p2
									Integer valueCls = ipd.getVALUECLS();
									if (ik.hasProperty(valueCls, idPropInverse))
										createFactInArray(valueCls, Integer.parseInt(value), idPropInverse, String.valueOf(ido), 
											ipd.getIDTO(), ipd.getOP(), ipd.getCLASSNAME(), action.NEW, aipd, aInvipd);
									
									//2. del BA con p2
									String valueInit = initFact.getVALUE();
									Integer idoInit = initFact.getIDO();
									if (idoInit!=null && valueInit!=null) {
										Integer initValueCls = initFact.getVALUECLS();
										if (ik.hasProperty(initValueCls, idPropInverse))
											createFactInArray(initValueCls, Integer.parseInt(valueInit), idPropInverse, 
												String.valueOf(idoInit), initFact.getIDTO(), initFact.getOP(), initFact.getCLASSNAME(), 
												action.DEL, aipd, aInvipd);
									}
								}
							}
						}
						
						boolean structural = ik.getCategory(idProp).isStructural();
						if (structural) {
							Integer valCls = ipd.getVALUECLS();
							Integer valNum = Integer.parseInt(value);
							if (valNum<0)
								idoFPropStructQueLoApunta.put(valNum, idProp);
							if (ipd.getOrder()==action.DEL) {
								//añadir en array de los que se quieren borrar e iterar por los estructurales
								//no comprueba si es borrable, simplemente lo añade en el array para que despues se compruebe.
								//Esto es porque para saber si es borrable podria ser posible mirar si el que le apunta se quiere 
								//borrar y para eso se añade en el array
								preProcessDelete(valNum, valCls, ido, aIdoIdtoDel, idoDelDestination, ipd.getDestinationSystem());
							}
						}
					}
				}
			} else if (ipd.getOrder()==action.DEL_OBJECT) {
				preProcessDelete(ido, ipd.getIDTO(), ido, aIdoIdtoDel, idoDelDestination, ipd.getDestinationSystem());
			} else if (ipd.getOrder()==action.EVOL) {
				hasEvol = true;
			}
		}
		System.out.println("nuevos facts inversas " + aInvipd);
		aipd.addAll(aInvipd);
		return hasEvol;
	}

	private void preProcessDelete(int ido, int idto, int idoSup, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, 
			HashMap<Integer,String> idoDelDestination, String destinationSystem) 
				throws SQLException, NamingException, NotFoundException, DataErrorException {
		IdoObjectDelete idoIdto = aIdoIdtoDel.get(ido);
		if (idoIdto!=null) {
			if (ido!=idoSup) {
				HashSet<Integer> aIdos = idoIdto.getIdosSuperiores();
				if (aIdos==null) {
					aIdos = new HashSet<Integer>();
					idoIdto.setIdosSuperiores(aIdos);
				}
				aIdos.add(idoSup);
			}
		} else {
			if (ido!=idoSup) {
				HashSet<Integer> aIdos = new HashSet<Integer>();
				aIdos.add(idoSup);
				idoIdto = new TransitionObject.IdoObjectDelete(idto, aIdos);
			} else
				idoIdto = new TransitionObject.IdoObjectDelete(idto, null);
			aIdoIdtoDel.put(ido, idoIdto);
			if (destinationSystem!=null)
				idoDelDestination.put(ido, destinationSystem);
			else
				System.out.println("destinationSystem es nulo para ido " + ido);
		}
		
		ArrayList<String> deleteIdos = new ArrayList<String>();
		String sqlSelect = "select PROPERTY, VAL_NUM, VALUE_CLS from O_Datos_Atrib " + //WITH(NOLOCK) " +
				"where ID_O=" + ido + " and ID_TO=" + idto;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlSelect);
			while (rs.next()) {
				Integer idProp = rs.getInt(1);
				if (!rs.wasNull()) {
					putCardinality(ido, idto, idProp, false, action.DEL);
					if (ik.isObjectProperty(idProp) && ik.getCategory(idProp).isStructural()) {
						Integer valNum = rs.getInt(2);
						if (!rs.wasNull()) {
							Integer valCls = rs.getInt(3);
							deleteIdos.add(valNum + "#" + valCls);
						}
					}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		Iterator it = deleteIdos.iterator();
		while (it.hasNext()) {
			String[] datos = ((String)it.next()).split("#");
			Integer d1 = Integer.parseInt(datos[0]);
			Integer d2 = Integer.parseInt(datos[1]);
			if (aIdoIdtoDel.get(d1)==null)
				preProcessDelete(d1, d2, ido, aIdoIdtoDel, idoDelDestination, destinationSystem);
		}
	}
	
	private void createFactInArray(Integer idto, Integer ido, Integer prop, String value, Integer valueCls, String op, 
			String className, int order, ArrayList<IPropertyDef> aipd, ArrayList<IPropertyDef> aInvipd) {
		if (!containsFact(ido, prop, value, aipd) && !containsFact(ido, prop, value, aInvipd)) {
			FactInstance fi = new FactInstance(idto, ido, prop, value, valueCls, null, null, op, className);
			fi.setOrder(order);
			fi.setExistia_BD(true);
//			System.out.println("fact " + fi.toString());
			aInvipd.add(fi);
		}
	}
	
	private ArrayList<IdoObject> preProcessDelete2(HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException {
		ArrayList<IdoObject> idosABorrar = new ArrayList<IdoObject>();
		Iterator it = aIdoIdtoDel.keySet().iterator();
		System.out.println("aIdoIdtoDel");
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			TransitionObject.IdoObjectDelete idoIdto = aIdoIdtoDel.get(ido);
			System.out.println("ido " + ido + ", idto_Superior_Delete -> " + idoIdto);
		}
		boolean seguirProcesando = true;
		while (seguirProcesando) {
			it = aIdoIdtoDel.keySet().iterator();
			seguirProcesando = false;
			while (it.hasNext()) {
				Integer ido = (Integer)it.next();
				TransitionObject.IdoObjectDelete idoIdto = aIdoIdtoDel.get(ido);
				
				boolean processDelete = false;
				boolean structural = false;
				HashSet<Integer> idosSup = idoIdto.getIdosSuperiores();
				if (idosSup!=null) {
					structural = true;
					Iterator itIdos = idosSup.iterator();
					while (itIdos.hasNext()) {
						Integer idoSup = (Integer)itIdos.next();
						TransitionObject.IdoObjectDelete idoIdtoStructSup = aIdoIdtoDel.get(idoSup);
						//es un structural si tiene un idoSup
						//procesar el borrado solo si sus superiores han sido procesados y puestos como borrados
						if (idoIdtoStructSup!=null && idoIdtoStructSup.isDelete()==null) {
							seguirProcesando = true;
							processDelete = false;
							break;
						} else if (idoIdtoStructSup==null || idoIdtoStructSup.isDelete())
							processDelete = true;
						//idoIdtoStructSup puede ser null si se pretende eliminar una linea sin eliminar el documento
					}
				} else
					processDelete = true;
				
				if (processDelete) {
					if (idoIdto.isDelete()==null) {
						orderDelObjectProcess(ido, idoIdto.getIdto(), aIdoIdtoDel, structural, idosABorrar);
						if (idoIdto.isDelete()==null)
							seguirProcesando = true;
							//mirar si se han rellenado todos los boolean
					}
				}
			}
		}
		it = aIdoIdtoDel.keySet().iterator();
		System.out.println("aIdoIdtoDelDespues");
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			TransitionObject.IdoObjectDelete idoIdto = aIdoIdtoDel.get(ido);
			System.out.println("ido " + ido + ", idto_Superior_Delete -> " + idoIdto);
		}
		
		return idosABorrar;
	}
	
	private void checkCardinality() 
			throws SQLException, NamingException, DataErrorException, NotFoundException, SystemException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//comprobar cardinalidad
		ArrayList<TransitionObject.IdoObject> aIdoProp = new ArrayList<TransitionObject.IdoObject>();
		System.out.println("tamaño de cardinalidad " + countCardinality.size());
		Iterator it = countCardinality.keySet().iterator();
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			System.out.println("ido " + ido);
			HashMap<Integer,Integer> hProp = countCardinality.get(ido);
			Iterator it2 = hProp.keySet().iterator();
			while (it2.hasNext()) {
				Integer prop = (Integer)it2.next();
				Integer countDb = null;
				System.out.println("prop " + prop);
				int idto = idoXIdto.get(ido);
				System.out.println("idto " + idto);
				if (ido>0) {
					//mirar en bd cuantas propertys han quedado para ese ido
					countDb = countRows(ido, idto, prop, propXTable.get(prop));
				} else {
					//si el individuo no estaba en base de datos se toma el contador del guardado en el mapa
					//se podria hacer una consulta, pero es innecesario
					countDb = hProp.get(prop);
				}
				System.out.println("cardinalidad en Db " + countDb);
				if (countDb>0) {
					Integer cardMax = ik.getMaxPropertyCardinalityOfClass(idto, prop);
					System.out.println("cardinalidad en Motor " + cardMax);
					if (cardMax!=null && cardMax<countDb) {
						TransitionObject.IdoObject idoProp = new TransitionObject.IdoObject(ido,prop);
						if (!idoProp.containedByIdoObject(aIdoProp))
							aIdoProp.add(idoProp);
					}
				}
			}
		}
		if (aIdoProp.size()>0) {
			String excepcion = "Se ha sobrepasado la cardinalidad máxima en:";
			it = aIdoProp.iterator();
			while (it.hasNext()) {
				TransitionObject.IdoObject idoProp = (TransitionObject.IdoObject)it.next();
				int ido = idoProp.getIdo();
				int prop = (Integer)idoProp.getObject();
				int idto = idoXIdto.get(ido);
				excepcion += "\nla property " + ik.getPropertyName(prop) + " (" + prop + ") " +
						"para el individuo " + ido + " de la clase " + ik.getClassName(idto) + "(" + idto + ")";
			}
			throw new DataErrorException(excepcion);
		}
	}
	
	private int generateIdReplica() throws SQLException, NamingException {
		//coge el siguiente de Replica_Autonum
		int autonum = 0;
		String random = user + "/" + String.valueOf((new Random()).nextInt());
		String sql = "insert into Replica_Autonum(code) values ('" + random + "')";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "Select id FROM Replica_Autonum " + /*WITH(nolock) */ "WHERE code='" + random + "'";
		
		Statement st = null;
		ResultSet rs = null;
		System.out.println("INSERT ROW");
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			rs.next();
			autonum = rs.getInt(1);
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return autonum;
	}
	
	private void setTransaction() throws SQLException, NamingException {
		String sql = "insert into Replica_Transaction(id,date) values (" + id + "," + System.currentTimeMillis() + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}

	private void getNewRdnsLock(ArrayList<IPropertyDef> aipd, HashSet<Integer> idosIncr, 
			HashMap<Integer,O_Reg_Instancias_Index> newRdnsLock,
			HashMap<Integer,HashSet<Integer>> updRdnsLock, HashMap<Integer,HashMap<Integer,Integer>> hDataKeys) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		System.out.println("Inicio de la funcion getNewRdnsLock");
		if (aipd!=null) {
			HashMap<Integer,String> idoSufix = new HashMap<Integer, String>();
			Iterator it = aipd.iterator();
			while (it.hasNext()) {
				IPropertyDef ipd = (IPropertyDef)it.next();
				if (idosIncr.contains(ipd.getIDO())) {
					if (ipd.getIDO()<0) {
						if (ipd.getPROP()==Constants.IdPROP_RDN)
							newRdnsLock.put(ipd.getIDO(),new O_Reg_Instancias_Index(ipd.getIDTO(),null,ipd.getVALUE()));
						else {
							if (seHaceReplica && ir.isAssociatedIdto(ipd.getIDTO()) && 
									ik.isObjectProperty(ipd.getPROP())) {
								boolean isKey = ir.isKeyOrSpecOfAssociatedIdto(ipd.getVALUECLS(), ipd.getIDTO(), ik);
								boolean isSufix = ir.isSufixOfAssociatedIdto(ipd.getVALUECLS(), ipd.getIDTO());
								
								if (isKey || isSufix) { //si pertenece a las key guardarlas
								//codigo nuevo que no haria falta si tuvieramos tabla de reserva
									int valueIntNeg = Integer.parseInt(ipd.getVALUE());
									
									HashMap<Integer,Integer> hData = hDataKeys.get(ipd.getIDO());
									if (hData==null) {
										hData = new HashMap<Integer,Integer>();
										hDataKeys.put(ipd.getIDO(), hData);
									}
									
									boolean isInMap = false;
									if (isSufix) {
										String sufix = idoSufix.get(valueIntNeg);
										if (sufix!=null) {
											hData.put(ipd.getVALUECLS(), Integer.parseInt(sufix));
											isInMap = true;
										}
									}
									if (!isInMap) {
										//puede ser negativo
										IdoObject io = null;
										Integer valueInt = null;
										if (valueIntNeg<0) {
											TransitionObject.IdoObject iobj = null;
											if (idoFicticioReal.containsKey(valueIntNeg))
												iobj = idoFicticioReal.get(valueIntNeg);
											else
												iobj = insertRowObject(valueIntNeg, ipd.getVALUECLS(), ipd.getPROP(), aipd);
											valueInt = iobj.getIdo();
										} else
											valueInt = valueIntNeg;
										
										if (isKey) {
											//si es key coger ido
											hData.put(ipd.getVALUECLS(), valueInt);
										} else {
											//si es sufix coger rdn y obtener el sufijo
											String value = null;
											if (valueIntNeg<0) {
												value = (String)io.getObject();
												//IPropertyDef ipdFind = findFactInstanceByIdoProp(valueInt, Constants.IdPROP_RDN, aipd);
												//value = ipdFind.getVALUE();
											} else {
												String dBValue = TransitionObject.dBValueOfIdoProp(valueInt, ipd.getVALUECLS(), Constants.IdPROP_RDN, factConnDB);
												if (dBValue!=null)
													value = dBValue;
											}
											idoSufix.put(valueIntNeg, value); //mapeado por si, 
											//en la misma sesion, se hacen cambios en stocks de 
											//diferentes productos del mismo almacen
											String sufix = value.substring(value.length()-3, value.length());
											hData.put(ipd.getVALUECLS(), Integer.parseInt(sufix));
										}
										//necesito un hashmap con los almacenes y productos
										//clave ido negativo de stock
										//objeto con idto(almacen o producto), ido real
									}
								}
							}
						}
					} else {
						HashSet<Integer> hIdos = updRdnsLock.get(ipd.getIDTO());
						if (hIdos==null) {
							hIdos = new HashSet<Integer>();
							updRdnsLock.put(ipd.getIDTO(),hIdos);
						}
						hIdos.add(ipd.getIDO());
					}
				}
			}
		}
		System.out.println("Fin de la funcion getNewRdnsLock");
	}

	//insercion de reserva independiente
	/*public void insertRemoteReserve(Connection conCentral, ArrayList<IdoObject> newRdnsLock, ArrayList<IdoObject> aIdosLock) 
			throws SQLException, NamingException {
		System.out.println("Inicio de la funcion insertRemoteReserve");
		//GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		Iterator it = newRdnsLock.iterator();
		while (it.hasNext()) {
			IdoObject io = (IdoObject)it.next();
			int idto = io.getIdo();
			String rdn = (String)io.getObject();
			String sql = "INSERT INTO o_reg_instancias_index_reserve(ID_TO, RDN) VALUES(" + idto + ", '" + rdn.replaceAll("'", "''") + "')";
			AuxiliarQuery.dbExecUpdate(conCentral, sql);
			sql = "Select autonum, concat(autonum,'000'), id_o" +
					" FROM o_reg_instancias_index_reserve " + //WITH(nolock)  
					"WHERE rdn='" + rdn.replaceAll("'", "''") + "' AND id_to=" + idto + " ORDER BY autonum";
			int autonum = 0;
			int idoAutonum = 0;
			Integer ido = null;
			Statement st = null;
			ResultSet rs = null;
			ArrayList<Integer> autonumDelete = new ArrayList<Integer>();
			try {
				st = conCentral.createStatement();
				rs = st.executeQuery(sql);
				boolean first = true;
				while (rs.next()) {
					if (first) {
						first = false;
						autonum = rs.getInt(1);
						idoAutonum = rs.getInt(2);
						ido = rs.getInt(3);
						if (rs.wasNull())
							ido = null;
					} else
						autonumDelete.add(rs.getInt(1));
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
			}

			if (ido==null) {
				sql = "update o_reg_instancias_index_reserve SET id_o=" + idoAutonum + " WHERE autonum=" + autonum;
				AuxiliarQuery.dbExecUpdate(conCentral, sql);
			}
			if (autonumDelete.size()!=0) {
				sql = "delete from o_reg_instancias_index_reserve WHERE autonum in(" + Auxiliar.arrayIntegerToString(autonumDelete, ",") + ")";
				AuxiliarQuery.dbExecUpdate(conCentral, sql);
			}
			O_Reg_Instancias_Index oReg = new O_Reg_Instancias_Index(idto,rdn);
			aIdosLock.add(new IdoObject(idoAutonum,oReg));

//			try {
//				sql = "INSERT INTO o_reg_instancias_index_lock(ID_O, ID_TO, RDN, " + gSQL.getCharacterBegin() + "LOCK" + gSQL.getCharacterEnd() + ") VALUES(" + ido + ", " + idto + ", '" + rdn.replaceAll("'", "''") + "', 'N')";
//				AuxiliarQuery.dbExecUpdate(con, sql);
//			} catch (SQLException e) {
//				;
//			}
		}
		System.out.println("Fin de la funcion insertRemoteReserve");
	}*/
	private void insertReserve(ArrayList<IPropertyDef> aipd, HashMap<Integer,O_Reg_Instancias_Index> newRdnsLock, 
			HashMap<Integer,HashSet<Integer>> idosLock, HashMap<Integer,HashMap<Integer,Integer>> hDataKeys) throws SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, SQLException, JDOMException, ParseException {
		System.out.println("Inicio de la funcion insertReserve");
		HashMap<Integer,IdoObject> idoNegRdn = new HashMap<Integer, IdoObject>();
		//GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
		Iterator it = newRdnsLock.keySet().iterator();
		while (it.hasNext()) {
			Integer keyIdoNeg = (Integer)it.next();
			
			O_Reg_Instancias_Index io = newRdnsLock.get(keyIdoNeg);
			int idto = io.getId_to();
			//este rdn no es valido
			//String rdn = (String)io.getObject();
			
			//construimos ido a partir de los idos de la clave
			boolean crea = false;
			if (seHaceReplica) {
				Integer idtoKey = ir.getKeyOfAssociatedIdto(idto);
				if (idtoKey!=null) {
					System.out.println("idtoKey " + idtoKey);
					
					HashMap<Integer,Integer> hData = hDataKeys.get(keyIdoNeg);
					Integer idoKey = hData.get(idtoKey);
					/*if (idoKey==null) {
						HashSet<Integer> hSpec = ik.getSpecialized(idtoKey);
						Iterator itSpec = hSpec.iterator();
						while (itSpec.hasNext() && idoKey==null) {
							Integer spec = (Integer)itSpec.next();
							System.out.println("spec " + spec);
							idoKey = hData.get(spec);
						}
					}*/
					System.out.println("idoKey " + idoKey);
					String idoKeyStr = String.valueOf(idoKey);
					String idoStr = idoKeyStr.substring(0,idoKeyStr.length()-3); //quitamos el sufijo
		
					int inicio = 500; //+ sufijo global del almacen al que pertenece el stock
					
					Integer sufix = null;
					ArrayList<Integer> aIdtoSufix = ir.getSufixOfAssociatedIdto(idto);
					Iterator it2 = aIdtoSufix.iterator();
					while (it2.hasNext() && sufix==null) {
						Integer idtoSufix = (Integer)it2.next();
						sufix = hData.get(idtoSufix);
					}
					System.out.println("sufix " + sufix);
		
					String newSufix = String.valueOf(inicio + sufix);
					System.out.println("newSufix " + newSufix);
		
					String newIdoStr = idoStr + newSufix;
						
					//String newIdoStr = idoStr + "000";
					Integer idoPos = Integer.parseInt(newIdoStr);
					
					String rdn = newIdoStr; //hacemos rdn=al ido
					//String rdn = getNewRdn(keyIdoNeg, ido, idto, null, aipd);
					idoNegRdn.put(keyIdoNeg, new IdoObject(idoPos,rdn));
		
					TransitionObject.IdoObject idoObj = new TransitionObject.IdoObject(idoPos,rdn);
					idoFicticioReal.put(keyIdoNeg, idoObj);
					
					String sql = "INSERT INTO o_reg_instancias_index_replica(ID_O, ID_TO, RDN) VALUES(" + idoPos + ", " + idto + ", '" + rdn.replaceAll("'", "''") + "')";
					try {
						AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
					} catch (Exception e) {
						e.printStackTrace();
					}
					HashSet<Integer> hIdos = idosLock.get(idto);
					if (hIdos==null) {
						hIdos = new HashSet<Integer>();
						idosLock.put(idto,hIdos);
					}
					hIdos.add(idoPos);
				} else
					crea = true;
			} else
				crea = true;
			
			if (crea) {
				TransitionObject.IdoObject iobj = null;
				if (idoFicticioReal.containsKey(keyIdoNeg))
					iobj = idoFicticioReal.get(keyIdoNeg);
				else {
					//crear mapa en preprocesado
					//mapa ido-propQueLoApunta
					Integer propApunta = idoFPropStructQueLoApunta.get(keyIdoNeg);
					iobj = insertRowObject(keyIdoNeg, idto, propApunta, aipd);
				}
				int idoPos = iobj.getIdo();
				//rdn = (String)iobj.getObject();
				HashSet<Integer> hIdos = idosLock.get(idto);
				if (hIdos==null) {
					hIdos = new HashSet<Integer>();
					idosLock.put(idto,hIdos);
				}
				hIdos.add(idoPos);
			}
		}
		
		//actualizo el ido positivo y el rdn
		//no es necesario porque no lo uso despues
		/*it = idoNegRdn.keySet().iterator();
		while (it.hasNext()) {
			Integer keyIdoNeg = (Integer)it.next();
			IdoObject io = idoNegRdn.get(keyIdoNeg);
			int idoPos = io.getIdo();
			String rdn = (String)io.getObject();
			
			O_Reg_Instancias_Index oreg = newRdnsLock.get(keyIdoNeg);
			oreg.setId_o(idoPos);
			oreg.setRdn(rdn);
		}*/
		
		System.out.println("Fin de la funcion insertReserve");
	}
	
	private HashMap<Integer,HashSet<Integer>> cloneHashMapHashSet(HashMap<Integer,HashSet<Integer>> hashMapHashSet) {
		HashMap<Integer,HashSet<Integer>> hashMapHashSetClone = new HashMap<Integer, HashSet<Integer>>();
		Iterator it = hashMapHashSet.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			HashSet<Integer> hashSet = hashMapHashSet.get(key);
			HashSet<Integer> hashSetClone = (HashSet<Integer>) hashSet.clone();
			hashMapHashSetClone.put(key, hashSetClone);
		}
		return hashMapHashSetClone;
	}
	private void removeHashMapHashSet(HashMap<Integer,HashSet<Integer>> allIdtoIdosLock, HashMap<Integer,HashSet<Integer>> idtoIdosNoLock) {
		HashSet<Integer> idtosRemove = new HashSet<Integer>();
		Iterator it = idtoIdosNoLock.keySet().iterator();
		while (it.hasNext()) {
			Integer key = (Integer)it.next();
			HashSet<Integer> hashSetNoLock = idtoIdosNoLock.get(key);
			HashSet<Integer> hashSetLock = allIdtoIdosLock.get(key);

			hashSetLock.removeAll(hashSetNoLock);
			if (hashSetLock.size()==0)
				idtosRemove.add(key);
		}
		it = idtosRemove.iterator();
		while (it.hasNext())
			allIdtoIdosLock.remove((Integer)it.next());
	}

	private void lockObjects(HashMap<Integer,HashSet<Integer>> allIdtoIdosLock, HashMap<Integer,HashSet<Integer>> idtoIdosNoLock, int intentos) throws SQLException, NamingException, InterruptedException {
		System.out.println("Quedan " + intentos + " intentos");
		if (intentos==0) {
			//si tras X intentos no soy capaz de bloquear -> desbloqueo
			System.out.println("desbloqueando por no ser capaz de bloquear");
			HashMap<Integer,HashSet<Integer>> allIdtoIdosLockTmp = cloneHashMapHashSet(allIdtoIdosLock); //clonar allIdtoIdosLock
			removeHashMapHashSet(allIdtoIdosLockTmp, idtoIdosNoLock);
			unLockObjects(allIdtoIdosLockTmp);
			long millis = 30*1000; //20 segundos
			Thread.currentThread().sleep(millis);
			lockObjects(allIdtoIdosLock, allIdtoIdosLock, maxIntentosLock);
		} else {
			HashMap<Integer,HashSet<Integer>> idtoIdosNoLockTmp = new HashMap<Integer,HashSet<Integer>>();
			Iterator it = idtoIdosNoLock.keySet().iterator();
			while (it.hasNext()) {
				Integer idto = (Integer)it.next();
				HashSet<Integer> idosLock = idtoIdosNoLock.get(idto);
				Iterator it2 = idosLock.iterator();
				while (it2.hasNext()) {
					Integer ido = (Integer)it2.next();
					try {
						m_IS.lockObjectTrans(ido, idto, user);
						HashSet<Integer> idosNoLockTmp = idtoIdosNoLockTmp.get(idto);
						if (idosNoLockTmp!=null) {
							idosNoLockTmp.remove(ido);
							if (idtoIdosNoLockTmp.size()==0)
								idtoIdosNoLockTmp.remove(idto);
						}
					} catch (Exception e) {
						e.printStackTrace();
						HashSet<Integer> idosNoLockTmp = idtoIdosNoLockTmp.get(idto);
						if (idosNoLockTmp==null) {
							idosNoLockTmp = new HashSet<Integer>();
							idtoIdosNoLockTmp.put(idto, idosNoLockTmp);
						}
						idosNoLockTmp.add(ido);
					}
				}
			}
			if (idtoIdosNoLockTmp.size()>0) {
				//si algunos estaban siendo usados
				//tengo que esperar
				long millis = 5*1000; //5 segundos
				Thread.currentThread().sleep(millis);
				if (seHaceReplica) {
					//varios intentos para que no se heche atras si es el scheduler el que lo bloquea
					intentos--;
				} else
					intentos = 0;
				lockObjects(allIdtoIdosLock, idtoIdosNoLockTmp, intentos);
			}
			//debug: espera
			/*if (idosLock.size()>0) {
				long millis1 = 300*20*1000; //300*20 segundos
				Thread.currentThread().sleep(millis1);
			}*/
		}
	}
	/*private void unLockObjects(ArrayList<IPropertyDef> aipd) throws SQLException, NamingException, InterruptedException {
		//bloquear en local
		HashSet<Integer> idosLock = new HashSet<Integer>();
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipd = (IPropertyDef)it.next();
			if (ipd.isIncremental()) {
			//if (ir.isAssociatedIdto(ipd.getIDTO())) {
				if (ipd.getIDO()>0)
					idosLock.add(ipd.getIDO());
				else if (ipd.getPROP()==Constants.IdPROP_RDN) {
					int ido = idoFicticioReal.get(ipd.getIDO()).getIdo();
					idosLock.add(ido);
				}
			}
		}
		//update
		updateUnLock(idosLock);
	}*/
	private void unLockObjects(HashMap<Integer,HashSet<Integer>> allIdtoIdosLock) throws SQLException, NamingException, InterruptedException {
		if (allIdtoIdosLock.size()>0) {
			try {
				m_IS.unlockObjectsTrans(allIdtoIdosLock, user);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private HashMap<Integer,HashSet<Integer>> reserveIndividual(HashSet<Integer> idosIncr, HashMap<Integer,HashSet<Integer>> updRdnsLock, 
			ArrayList<IPropertyDef> aipd) 
			throws SystemException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, DataErrorException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		HashMap<Integer,O_Reg_Instancias_Index> newRdnsLock = new HashMap<Integer, O_Reg_Instancias_Index>();
		HashMap<Integer,HashSet<Integer>> newIdosLock = new HashMap<Integer,HashSet<Integer>>();
		HashMap<Integer,HashMap<Integer,Integer>> hDataKeys = new HashMap<Integer,HashMap<Integer,Integer>>();
		getNewRdnsLock(aipd, idosIncr, newRdnsLock, updRdnsLock, hDataKeys);
		
		if (newRdnsLock.size()>0)
			insertReserve(aipd, newRdnsLock, newIdosLock,hDataKeys);
		return newIdosLock;
	}

	private void preProcessContribution(HashMap<Integer,HashSet<Integer>> newIdosLock) throws SQLException, NamingException {
		//buscar si en db estan los nuevos ind. contribution creados
		if (newIdosLock.size()>0) {
			String sql = "Select id_o FROM o_datos_atrib " + /*WITH(nolock) */ 
					"WHERE property=" + Constants.IdPROP_RDN;
			sql += Auxiliar.createSqlIdtoIdos(newIdosLock, null, true);

			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = factConnDB.createConnection(true);
			try {
				st = con.getBusinessConn().createStatement();
				System.out.println("sql " + sql);
				rs = st.executeQuery(sql);
				while (rs.next()) {
					Integer ido = (Integer)rs.getInt(1);
					//si esta -> añadirlos en un hashSet global
					//y si es un new pero esta en este mapa y ademas es un fact contributivo entrar en update en vez de en new
					idosContribNoNew.add(ido);
					System.out.println("añadiendo ido contributivo no nuevo " + ido);
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}
	}
	
	public Changes serverTransitionObject(IndividualData dIndiv) 
			throws SQLException, NamingException, DataErrorException, NotFoundException, SystemException, 
			ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, JDOMException, ParseException, OperationNotPermitedException, InterruptedException {
		System.out.println("Inicio de la funcion serverTransitionObject");
		
		//System.out.println(dIndiv);
		ArrayList<IPropertyDef> aipd = dIndiv.getAIPropertyDef();
		if (aipd!=null) {

			seHaceReplica = ir!=null;
			if (seHaceReplica) {
				idtosNoModify = ir.getNoModifyDB();
				id = generateIdReplica();
				System.out.println("ID " + id);
			}
			System.out.println("seHaceReplica " + seHaceReplica);
			try {
				idPropMiEmpresa = ik.getIdProperty(Constants.prop_mi_empresa);
			} catch (Exception e) {
				;
			}
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel = new HashMap<Integer, TransitionObject.IdoObjectDelete>();
			HashMap<Integer,String> idoDelDestination = new HashMap<Integer, String>();
			HashSet<Integer> idosIncr = new HashSet<Integer>();
			/*boolean hasEvol = */preProcess(aipd, idosIncr, aIdoIdtoDel, idoDelDestination);
			//boolean hasDelObject = false;
			//al hacer del o del_object guardar idto y prop
			/*if (hasEvol) {
			Iterator it = aipd.iterator();
			while (it.hasNext()) {
				IPropertyDef ipd = (IPropertyDef)it.next();
					if (ipd.getOrder()==action.EVOL)
						orderEvol(aipd, ipd);
				}
			}*/
			
			HashMap<Integer,HashSet<Integer>> updRdnsLock = new HashMap<Integer,HashSet<Integer>>();
			HashMap<Integer,HashSet<Integer>> newIdosLock = reserveIndividual(idosIncr, updRdnsLock, aipd);
			//lockObjects(newRdnsLock, updRdnsLock);
			HashMap<Integer,HashSet<Integer>> idosLock = (HashMap<Integer,HashSet<Integer>>)newIdosLock.clone();

			//Añado updRdnsLock a idosLock
			Iterator itR = updRdnsLock.keySet().iterator();
			while (itR.hasNext()) {
				Integer idto = (Integer)itR.next();
				HashSet<Integer> hIdos = updRdnsLock.get(idto);
				
		    	HashSet<Integer> hIdosLock = idosLock.get(idto);
				if (hIdosLock==null) {
					hIdosLock = new HashSet<Integer>();
					idosLock.put(idto,hIdosLock);
				}
				hIdosLock.addAll(hIdos);
			}
			
			lockObjects(idosLock, idosLock, maxIntentosLock);
			
			//una vez bloqueados
			//ver cuales de los idos creados existe en DB si es que existe alguno, 
			//por si otro usuario esta modificandolo a la vez
			preProcessContribution(newIdosLock);
			
			boolean updateMiEmpresa = false;
			idtoMiEmpresa = ik.getIdClass(Constants.CLS_MI_EMPRESA);

			Iterator it = aipd.iterator();
			while (it.hasNext()) {
				IPropertyDef ipd = (IPropertyDef)it.next();
				if (ipd.getOrder()==action.NEW)
					orderNew(aipd, ipd);
				else if (ipd.getOrder()==action.SET)
					orderSet(aipd, ipd);
				else if (ipd.getOrder()==action.DEL)
					orderDel(aipd, ipd);
				if (!updateMiEmpresa && ipd.getIDTO().equals(idtoMiEmpresa))
					updateMiEmpresa = true;
//				debug: espera
//				if (idosLock.size()>0 && user.equals("miguel")) {
//					long millis1 = 300*20*1000; //300*20 segundos
//					Thread.currentThread().sleep(millis1);
//				}
			}
			HashSet<Integer> idtosIndexABorrar = null;
			if (aIdoIdtoDel.size()>0) {
				ArrayList<IdoObject> idosABorrar = preProcessDelete2(aIdoIdtoDel);
				if (idosABorrar.size()>0)
					idtosIndexABorrar = subDellAllFactsInstance(aipd, idosABorrar, idoDelDestination);
			}
			checkNewIndex();
			processNewUser();
			if (debugMode)
				checkCardinality();
			if (updateMiEmpresa) {
				System.out.println("updateMiEmpresa " + updateMiEmpresa);
				((IKnowledgeBaseInfoServer)ik).inicializeMiEmpresa();
			}
			unLockObjects(idosLock);
			putNewIndex();
			if (idtosIndexABorrar!=null && idtosIndexABorrar.size()>0)
				((IKnowledgeBaseInfoServer)ik).deleteClassesIndex(idtosIndexABorrar);
		}
		if (seHaceReplica)
			setTransaction();
		System.out.println("Fin de la funcion serverTransitionObject");
		if (seHaceReplica)
			System.out.println("ID " + id);
		System.out.println(changes);
		return changes;
	}

	private void putCardinality(int ido, int idto, int prop, boolean isMemo, int order) {
		if (debugMode) {
			idoXIdto.put(ido, idto);
			HashMap<Integer,Integer> hProp = countCardinality.get(ido);
			Integer count = 0;
			System.out.println("put " + ido + " prop " + prop + " order " + order);
			String table = "";
			if (isMemo)
				table = "O_Datos_Atrib_Memo";
			else
				table = "O_Datos_Atrib";
			propXTable.put(prop, table);
			if (hProp==null) {
				hProp = new HashMap<Integer,Integer>();
				countCardinality.put(ido, hProp);
			} else {
				count = hProp.get(prop);
				if (count==null)
					count = 0;
			}
			if (order==action.NEW)
				hProp.put(prop, count+1);
			else if (order==action.DEL)
				hProp.put(prop, count-1);
		}
	}
	
	private void log(IPropertyDef ipd, Integer ido, Integer idto, String action, Integer idAction) 
			throws SQLException, NamingException {
		/*String sql = "INSERT INTO LOG_FACT_INSTANCE(fecha,ID_USER,ID_O,ID_TO,FACT_INSTANCE,LOG_ACTION,ACTION_ID) "
			+ "VALUES('" + Auxiliar.getDate() + "','" + user + "'," + ido + "," + idto + ",";
		if (ipd!=null)
			sql += "'" + ipd.toString().replaceAll("'", "''") + "',";
		else
			sql += "NULL,";
		sql += "'" + action + "'," + idAction + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql);*/
	}
	
	/*private void orderEvol(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws SQLException, NamingException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion orderEvol");

		Integer ido = ipd.getIDO(); // -> positivo
		Integer idto = ipd.getIDTO();
		IPropertyDef ipdOld = ipd.getInitialValues();
		Integer idoOld = ipdOld.getIDO();  // -> negativo
		Integer idtoOld = ipdOld.getIDTO();
		
		if (!seHaceReplica || !idtosNoModify.contains(idto)) {
			if (!seHaceReplica || !idtosNoModify.contains(idtoOld)) {
				if (ido!=null && idoOld!=null) {
					//buscar su rdn
					String rdn = null;
					String sql = "select rdn from o_reg_instancias_index where ID_O=" + ido;
					//System.out.println(sql);
					Statement st = null;
					ResultSet rs = null;
					ConnectionDB con = factConnDB.createConnection(true); 
					try {
						st = con.getBusinessConn().createStatement();
						rs = st.executeQuery(sql);
						if (rs.next()) {
							rdn = rs.getString(1);
						}
					} finally {
						if (rs != null)
							rs.close();
						if (st != null)
							st.close();
						if (con!=null)
							factConnDB.close(con);
					}
					//se pone en el mapa
					TransitionObject.IdoObject idoObj = new TransitionObject.IdoObject(ido,rdn);
					idoFicticioReal.put(idoOld, idoObj);
					
					//actualizar idto
					sql = "UPDATE O_Reg_Instancias_Index SET ID_TO=" + idto + " WHERE ID_TO=" + idtoOld + " AND ID_O=" + ido;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql);
					sql = "UPDATE O_Datos_Atrib SET VALUE_CLS=" + idto + " WHERE VALUE_CLS=" + idtoOld + " AND VAL_NUM=" + ido;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql);
					
					sql = "DELETE FROM O_Datos_Atrib WHERE ID_TO=" + idtoOld + " AND ID_O=" + ido;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql);
					sql = "DELETE FROM O_Datos_Atrib_Memo WHERE ID_TO=" + idtoOld + " AND ID_O=" + ido;
					AuxiliarQuery.dbExecUpdate(factConnDB, sql);
					
					if (seHaceReplica)
						evolReplica(ido, idto, idoOld, idtoOld, aipd);
				}
			} else {
				String rdn = InstanceService.getRdn(factConnDB, idoOld);
				String clase = ik.getClassName(idto);
				throw new DataErrorException(DataErrorException.ERROR_MODIFY, 
						"No es posible modificar el objeto "+ rdn +" de " + clase);
			}
		} else {
			String rdn = InstanceService.getRdn(factConnDB, ido);
			String clase = ik.getClassName(idto);
			throw new DataErrorException(DataErrorException.ERROR_MODIFY, 
					"No es posible modificar el objeto "+ rdn +" de " + clase);
		}
		//System.out.println("Fin de la funcion orderEvol");
	}*/
	
	private void orderNew(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws SQLException, NamingException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion orderNew");
		Integer idto = ipd.getIDTO();
		if (!seHaceReplica || !idtosNoModify.contains(idto)) {
			Integer ido = ipd.getIDO();
			IdoObject io = idoFicticioReal.get(ido);
			if (io!=null) {
				Integer idoPos = io.getIdo();
				if (idosContribNoNew.contains(idoPos)) {
					if (ipd.isIncremental())
						subNewUpdFactInstance(false, aipd, null, ipd, true);
				} else
					subNewUpdFactInstance(true, aipd, null, ipd, true);
			} else
				subNewUpdFactInstance(true, aipd, null, ipd, true);
		} else {
			Integer ido = ipd.getIDO();
			String rdn = InstanceService.getRdn(factConnDB, ido, idto);
			String clase = ik.getClassName(idto);
			throw new DataErrorException(DataErrorException.ERROR_DATA, 
					"No es posible modificar el objeto "+ rdn +" de " + clase);
		}
		//System.out.println("Fin de la funcion orderNew");
	}
	
	private void orderSet(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws SQLException, NamingException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion orderSet");
		Integer idto = ipd.getIDTO();
		if (!seHaceReplica || !idtosNoModify.contains(idto))
			subNewUpdFactInstance(false, aipd, null, ipd, true);
		else {
			Integer ido = ipd.getIDO();
			String rdn = InstanceService.getRdn(factConnDB, ido, idto);
			String clase = ik.getClassName(idto);
			throw new DataErrorException(DataErrorException.ERROR_DATA, 
					"No es posible modificar el objeto "+ rdn +" de " + clase);
		}
		//System.out.println("Fin de la funcion orderSet");
	}
	
	private void orderDel(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion orderDel");
		/*if (ipd.getOrder()==action.DEL) {
			subDelPreFactInstance(aipd, ipd);
		} else if (ipd.getOrder()==action.DEL_OBJECT) {
			if (ipd.getIDO()!=null)
				subDelAllFactsInstance(aipd, ipd.getIDO(), ipd.getIDTO(), false);
		}*/
		Integer idto = ipd.getIDTO();
		if (!seHaceReplica || !idtosNoModify.contains(idto))
			subDelFactInstance(aipd, ipd);
		else {
			Integer ido = ipd.getIDO();
			String rdn = InstanceService.getRdn(factConnDB, ido, idto);
			String clase = ik.getClassName(idto);
			throw new DataErrorException(DataErrorException.ERROR_DATA, 
					"No es posible modificar el objeto "+ rdn +" de " + clase);
		}
		//System.out.println("Fin de la funcion orderDel");
	}
	private void orderDelObjectProcess(int ido, int idto, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, boolean structural, 
			ArrayList<IdoObject> idosABorrar) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion orderDel");
		if (!seHaceReplica || !idtosNoModify.contains(idto))
			subDelAllFactsInstanceProcess(ido, idto, aIdoIdtoDel, structural, idosABorrar);
		else {
			String rdn = InstanceService.getRdn(factConnDB, ido, idto);
			String clase = ik.getClassName(idto);
			throw new DataErrorException(DataErrorException.ERROR_DATA, 
					"No es posible modificar el objeto "+ rdn +" de " + clase);
		}
		//System.out.println("Fin de la funcion orderDel");
	}
	
	private int countRows(int ido, int idto, int prop, String table) throws SQLException, NamingException {
		int rows = 0;
		//query para ver cuantos hay en bd
		String sql = "select count(*) from " + table + /*" WITH(NOLOCK)*/ " where ID_O = " + ido + " and ID_TO=" + idto;
			sql += " and PROPERTY=" + prop;
		//System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				rows = rs.getInt(1);
				//System.out.println("rows " + rows);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		
		return rows;
	}

	public static ArrayList<Integer> specialized(IKnowledgeBaseInfo ik, Integer idto) throws IncoherenceInMotorException {
		ArrayList<Integer> res = new ArrayList<Integer>();
		if (idto!=null) {
			res.add(idto);
			try {
				res.addAll(dynagent.common.utils.Auxiliar.IteratorToArrayList(ik.getSpecialized(idto).iterator()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (NotFoundException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	/*private ArrayList<Integer> hasUnicity(int idto, int prop) throws SQLException, NamingException, IncoherenceInMotorException {
		ArrayList<Integer> idtoParentSpec = null;
		String sql = "select ID_TO from Unicity where PROPERTY=" + prop;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				Integer idtoDB = rs.getInt(1);
				if (idtoDB.equals(idto)) {
					idtoParentSpec = specialized(ik, idtoDB);
					break;
				} else {
					ArrayList<Integer> idtosSpec = specialized(ik, idtoDB);
					if (idtosSpec.contains(idto)) {
						idtoParentSpec = idtosSpec;
						break;
					}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		return idtoParentSpec;
	}*/
	
	private boolean isInDB(int idto, int ido, int prop, String table) throws SQLException, NamingException {
		boolean actualizar = false;
		String sql = "select * from " + table + /*" WITH(NOLOCK)*/ " where ID_O = " + ido + " and ID_TO=" + idto + " and PROPERTY=" + prop;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			actualizar = rs.next(); // se trata de un replace
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return actualizar;
	}
	/*private boolean isInDBWithValue(ArrayList<Integer> idtoParentSpec, int prop, String value, Double qMin, Double qMax, boolean isMemo) 
			throws SQLException, NamingException {
		boolean is = false;
		String table = "";
		if (isMemo)
			table = "O_Datos_Atrib_Memo";
		else
			table = "O_Datos_Atrib";
		//comprobar si el valor esta en bd para ese idto o para alguno de sus especializados
		String sql = "select * from " + table + " where ID_TO IN(" + Auxiliar.arrayIntegerToString(idtoParentSpec, ",") + ")"
			+ " and PROPERTY=" + prop;
		if (isMemo) {
			if (value!=null)
				sql += " and MEMO='" + value.replaceAll("'", "''") + "'";
			else
				sql += " and MEMO is null";
		} else {
			if (ik.isDataProperty(prop)) {
				if (value!=null)
					sql += " and VAL_TEXTO='" + value.replaceAll("'", "''") + "'";
				else
					sql += " and VAL_TEXTO is null";
			} else if (ik.isObjectProperty(prop)) {
				if (value!=null)
					sql += " and VAL_NUM=" + value;
				else
					sql += " and VAL_NUM is null";
			}
			if (qMin!=null)
				sql += " and Q_MIN=" + qMin;
			else
				sql += " and Q_MIN is null";
			if (qMax!=null)
				sql += " and Q_MAX=" + qMax;
			else
				sql += " and Q_MAX is null";
		}
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			is = rs.next();
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		return is;
	}*/
	
	private void putNewIndex() throws DataErrorException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		Iterator it = aNewIndex.keySet().iterator();
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			Index ind = aNewIndex.get(ido);
			int idto = ind.getIdto();
			((IKnowledgeBaseInfoServer)ik).putClassesIndex(idto);
		}
	}
	private void checkNewIndex() throws DataErrorException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		Iterator it = aNewIndex.keySet().iterator();
		while (it.hasNext()) {
			Integer ido = (Integer)it.next();
			Index ind = aNewIndex.get(ido);
			//comprobar que no hay un indice definido con los mismos campo idto, property, property_filter, value_filter, mi_empresa
			if (existsIndex(ind.getIdto(), ido, ind.getProperty(), ind.getPropFilter(), ind.getValueFilter(), ind.getMiEmpresa())) {
				throw new DataErrorException(DataErrorException.ERROR_DATA, 
						"Ya hay un índice creado para " + ind.getDomain() + ". " +
						"Si quiere asignarle nuevos valores debera borrarlo primero");
			}
			if (ind.getMascPrefixTemp()!=null && ind.getPropPrefixTemp()==null ||
					ind.getMascPrefixTemp()==null && ind.getPropPrefixTemp()!=null)
				throw new DataErrorException(DataErrorException.ERROR_DATA, 
						"Si introduce un prefijo temporal los campos máscara Prefijo Temporal y Campo En Prefijo Temporal deben tener valor");
			if (ind.getPropFilter()!=null && ind.getValueFilter()==null ||
					ind.getPropFilter()==null && ind.getValueFilter()!=null)
				throw new DataErrorException(DataErrorException.ERROR_DATA, 
						"Si introduce una condición de filtrado los campos Campo Filtro y Valor Filtro deben tener valor");
				
			checkCountIndex(ind.getIdo(), ind.getIndex(), ind);
		}
	}
	private void checkIndex(int ido, IPropertyDef ipd, int op) throws DataErrorException, NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		boolean addIndex = op==action.NEW;
		Index index = null;
		if (addIndex) {
			index = aNewIndex.get(ido);
			if (index==null) {
				index = new Index();
				index.setIdo(ido);
				index.setIdto(ik.getIdClass(Constants.CLS_FORMATO_INDICE));
				aNewIndex.put(ido, index);
			}
		}
		String error = null;
		String propStr = ik.getPropertyName(ipd.getPROP());
		System.out.println("propStrIndex " + propStr);

		if (op!=action.NEW && !propStr.equals(Constants.PROP_INICIO_CONTADOR) && !propStr.equals(Constants.PROP_MIN_DIGITS)) {
			//solo se puede actualizar el indice, en caso contrario -> excepcion
			error = "Una vez creado un índice solo es posible modificar su contador o los d�gitos mínimos de este";
		} else if (op!=action.DEL) {
			if (propStr.equals(Constants.PROP_DOMINIO)) {
				try {
					Integer idto = ik.getIdClass(ipd.getVALUE());
					if (idto==null)
						error = "El nombre del dominio " + ipd.getVALUE() + " no existe";
					else if (addIndex) {
						index.setDomain(ipd.getVALUE());
						index.setIdto(idto);
					}
				} catch (Exception e) {
					error = "El nombre del dominio " + ipd.getVALUE() + " no existe";
				}
			} else if (propStr.equals(Constants.PROP_CAMPO_INDEXADO)) {
				try {
					Integer prop = ik.getIdProperty(ipd.getVALUE());
					if (prop==null)
						error = "El nombre del campo indexado " + ipd.getVALUE() + " no existe";
					else if (addIndex)
						index.setProperty(prop);
				} catch (Exception e) {
					error = "El nombre del campo indexado " + ipd.getVALUE() + " no existe";
				}
			} else if (propStr.equals(Constants.PROP_INICIO_CONTADOR)) {
				int newIndex = ((int)Double.parseDouble(String.valueOf(ipd.getQMIN())));
				if (addIndex)
					index.setIndex(newIndex);
				else
					checkCountIndex(ipd.getIDO(), newIndex, null);
			} else if (propStr.equals(Constants.PROP_PREFIJO)) {
				if (addIndex)
					index.setPrefix(ipd.getVALUE());
			} else if (propStr.equals(Constants.PROP_SUFIJO)) {
				if (addIndex)
					index.setSufix(ipd.getVALUE());
			} else if (propStr.equals(Constants.PROP_MIN_DIGITS)) {
				if (addIndex) {
					int minDigits = ((int)Double.parseDouble(String.valueOf(ipd.getQMIN())));
					index.setMinDigits(minDigits);
				}
			} else if (propStr.equals(Constants.prop_mi_empresa)) {
				if (addIndex)
					index.setMiEmpresa(Integer.parseInt(ipd.getVALUE()));
			} else if (propStr.equals(Constants.PROP_CAMPO_FILTRO)) {
				try {
					Integer prop = ik.getIdProperty(ipd.getVALUE());
					if (prop==null)
						error = "El nombre del campo filtro " + ipd.getVALUE() + " no existe";
					else if (addIndex)
						index.setPropFilter(prop);
				} catch (Exception e) {
					error = "El nombre del campo filtro " + ipd.getVALUE() + " no existe";
				}
			} else if (propStr.equals(Constants.PROP_VALOR_FILTRO)) {
				if (addIndex)
					index.setValueFilter(ipd.getVALUE());
			} else if (propStr.equals(Constants.PROP_CAMPO_EN_PREFIJO)) {
				try {
					Integer prop = ik.getIdProperty(ipd.getVALUE());
					if (prop==null)
						error = "El nombre del campo en prefijo " + ipd.getVALUE() + " no existe";
					else if (addIndex)
						index.setPropPrefix(prop);
				} catch (Exception e) {
					error = "El nombre del campo en prefijo " + ipd.getVALUE() + " no existe";
				}
			} else if (propStr.equals(Constants.PROP_SUFIJO_GLOBAL)) {
				if (addIndex)
					index.setGlobalSufix(Boolean.parseBoolean(String.valueOf(ipd.getQMIN())));
			} else if (propStr.equals(Constants.PROP_MASC_PREFIX_TEMP)) {
				String mascPrefixTemp = ipd.getVALUE();
				if (!mascPrefixTemp.equals("aamm") && !mascPrefixTemp.equals("aa") && 
						!mascPrefixTemp.equals("a") && !mascPrefixTemp.equals("amm"))
					error = "Las máscaras que puede utilizar son aamm, aa, a, amm o mm";
				if (addIndex)
					index.setMascPrefixTemp(ipd.getVALUE());
			} else if (propStr.equals(Constants.PROP_CAMPO_EN_PREFIJO_TEMP)) {
				try {
					Integer prop = ik.getIdProperty(ipd.getVALUE());
					if (prop==null)
						error = "El nombre del campo en prefijo temporal " + ipd.getVALUE() + " no existe";
					else {
						//que sea tipo fecha
						Integer dataType = ik.getDatatype(prop);
						if (!dataType.equals(Constants.IDTO_DATE) && 
								!dataType.equals(Constants.IDTO_DATETIME))
							error = "El dato introducido en campo en prefijo debe hacer referencia a un fecha";
						if (addIndex)
							index.setPropPrefixTemp(prop);
					}
				} catch (Exception e) {
					error = "El nombre del campo en prefijo temporal " + ipd.getVALUE() + " no existe";
				}
			} else if (propStr.equals(Constants.PROP_CONTADOR_año)) {
				if (addIndex) {
					int contYear = ((int)Double.parseDouble(String.valueOf(ipd.getQMIN())));
					index.setContYear(contYear);
				}
			} else if (propStr.equals(Constants.PROP_ULTIMO_PREFIJO_TEMP)) {
				//aqui no va a entrar porque esta property no se ve desde la aplicacion
				if (addIndex)
					index.setLastPrefixTemp(ipd.getVALUE());
			}
		}
		if (error!=null)
			throw new DataErrorException(DataErrorException.ERROR_DATA,error);
	}

	private void checkCountIndex(int ido, int newIndex, Index in) throws DataErrorException, NotFoundException, SQLException, NamingException {
		//a esta funcion se llama cuando se actualiza el indice
		//el procesado de los nuevos indices se hace a posteriori recorriendo el array construido

		//buscar en bd el tipo de indice que tiene este ido
		//y ver si hay algun indice creado con el valor que se le quiere poner
		if (in==null)
			in = getIndex(ido);
		else {
			if (in.getLastPrefixTemp()!=null)
				throw new DataErrorException(DataErrorException.ERROR_DATA, 
					"No debe introducir ningun valor en el último prefijo temporal al crear un índice");
		}
		//no lleva chequeo para fechas porque al crear factura se puede poner la fecha que se quiera
		//se debe poner cont=20 pensando en crear una factura del año pasado con indice 20
		//aunque en este año el contador vaya por 30
		if (in.getPropPrefixTemp()==null) {
			int actualIndex = IndexFilterFunctions.getActualIndex(in.getIdto(),in.getPrefix(),in.getSufix(),in.getPropPrefix(),null,null,null,null,null, in.getMiEmpresa(), factConnDB, ((IKnowledgeBaseInfoServer)ik).getClassesIndexWithSpecialized());
			System.out.println("actualIndex en db: " + actualIndex);
			System.out.println("newIndex en db: " + newIndex);
			if (actualIndex>=newIndex)
				throw new DataErrorException(DataErrorException.ERROR_DATA, 
						"El índice asignado tiene que ser mayor que " + actualIndex + 
						" porque en base de datos existen individuos con este formato");
		}
	}
	
	private boolean existsIndex(int idto, int ido, int property, Integer propFilter, String valueFilter, Integer miEmpresa) throws SQLException, NamingException {
		boolean exists = false;
		if (miEmpresa>0) {
			String sql = "Select ID_O, ID_TO, PROPERTY," + generateSQL.getCharacterBegin() + "INDEX" + 
				generateSQL.getCharacterEnd() + ",PREFIX, SUFIX, PROPERTY_PREFIX, PROPERTY_FILTER, VALUE_FILTER, GLOBAL_SUFIX FROM S_Index " + //WITH(nolock) " +
				"WHERE ID_TO=" + idto + " AND PROPERTY=" + property + " AND ID_O<>" + ido;
			if (propFilter!=null)
				sql += " AND PROPERTY_FILTER=" + propFilter + " AND VALUE_FILTER='" + valueFilter.replaceAll("'", "''") + "'";
			else
				sql += " AND PROPERTY_FILTER is null AND VALUE_FILTER is null";
			if (miEmpresa!=null)
				sql += " AND MI_EMPRESA=" + miEmpresa;
			else
				sql += " AND MI_EMPRESA is null";
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = factConnDB.createConnection(true);
			try {
				st = con.getBusinessConn().createStatement();
				//System.out.println(sql);
				rs = st.executeQuery(sql);
				if (rs.next())
					exists = true;
				//System.out.println("exists " + exists);
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}
		return exists;
	}
	private Index getIndex(int ido) throws SQLException, NamingException {
		Index ind = null;
		String sql = "Select ID_TO, PROPERTY," + generateSQL.getCharacterBegin() + "INDEX" + 
			generateSQL.getCharacterEnd() + ",PREFIX, SUFIX, PROPERTY_PREFIX, " +
					"PROPERTY_FILTER, VALUE_FILTER, GLOBAL_SUFIX, " +
					"MASC_PREFIX_TEMP, PROPERTY_PREFIX_TEMP, CONT_YEAR, LAST_PREFIX_TEMP, MIN_DIGITS, MI_EMPRESA FROM S_Index " + //WITH(nolock) " +
			"WHERE ID_O=" + ido;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				int idto = rs.getInt(1);
				int prop = rs.getInt(2);
				int index = rs.getInt(3);
				String prefix = rs.getString(4);
				String sufix = rs.getString(5);
				Integer propPrefix = rs.getInt(6);
				if (rs.wasNull())
					propPrefix = null;
				
				Integer propFilter = rs.getInt(7);
				if (rs.wasNull())
					propFilter = null;
				String valueFilter = rs.getString(8);
				Boolean globalSufix = rs.getBoolean(9);
				if (rs.wasNull())
					globalSufix = false;
				String mascPrefixTemp = rs.getString(10);
				Integer propPrefixTemp = rs.getInt(11);
				if (rs.wasNull())
					propPrefixTemp = null;
				Integer contYear = rs.getInt(12);
				if (rs.wasNull())
					contYear = null;
				String lastPrefixTemp = rs.getString(13);
				Integer minDigits = rs.getInt(14);
				Integer miEmpresa = rs.getInt(15);
				//if (valueFilter!=null)
					//valueFilter = parserValueFilter(valueFilter, factConnDB);
				ind = new Index(ido, idto, prop, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, prefix, propPrefix, index, sufix, globalSufix, minDigits, propFilter, valueFilter, miEmpresa);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return ind;
	}

	private void subNewUpdFactInstance(boolean creacion, ArrayList<IPropertyDef> aipd, Integer idoNeg, IPropertyDef ipd, boolean compruebaReIndex)
			throws SQLException, NamingException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subNewUpdFactInstance");
		Integer ido = ipd.getIDO();
		//Integer idoNeg = null;
		Integer idto = ipd.getIDTO();
		String rdn = null;
		if (ido<0) {
			TransitionObject.IdoObject iobj = null;
			if (idoFicticioReal.containsKey(ido))
				iobj = idoFicticioReal.get(ido);
			else {
				//crear mapa en preprocesado
				//mapa ido-propQueLoApunta
				Integer propApunta = idoFPropStructQueLoApunta.get(ido);
				iobj = insertRowObject(ido, idto, propApunta, aipd);
			}
			idoNeg = ido;
			ido = iobj.getIdo();
			rdn = (String)iobj.getObject();
		}
		if (ido!=null) {
			String clase = ik.getClassName(idto);
			int prop = ipd.getPROP();
			//si esta en tabla unicidad:
			//aqui, esta property no va a estar en la tabla indice, si no, no me llegaria
			//comprobar unicidad
			//boolean correcto = true;
			System.out.println("prop " + prop);
			//mirar si esta en la tabla de unicidad
//			ArrayList<Integer> idtoParentSpec = hasUnicity(idto, prop);
/*			boolean isMemo = ipd.getVALUECLS().equals(Constants.IDTO_MEMO);
			if (idtoParentSpec!=null) {
				//mirar si ya esta en base de datos
				if (isInDBWithValue(idtoParentSpec, prop, ipd.getVALUE(), ipd.getQMIN(), ipd.getQMAX(), isMemo))
					correcto = false;
			}
			if (correcto) {*/
				boolean actualizar = false;
				if (!creacion) {
					String table = "";
					boolean memo = false;
					if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO)) {
						table = "O_Datos_Atrib_Memo";
						memo = true;
					} else
						table = "O_Datos_Atrib";
					if (debugMode)
						actualizar = isInDB(idto, ido, prop, table);
					else
						actualizar = true;
					if (actualizar) {
						//antes de hacer update comprobar que la property no se uso para generar indice
						//si se us� -> reindexar generando de nuevo el rdn
						IPropertyDef ipdOld = ipd.getInitialValues();
						if (compruebaReIndex && 
								(ipd.getVALUE()!=null && ipdOld.getVALUE()!=null && !ipd.getVALUE().equals(ipdOld.getVALUE()) 
										|| ipd.getQMIN()!=null && ipdOld.getQMIN()!=null && !ipd.getQMIN().equals(ipdOld.getQMIN()) )) {
							//reindexa solo si hay un cambio con el valor anterior
							if (((IKnowledgeBaseInfoServer)ik).getClassesIndex().contains(idto))
								processReIndex(ido, idto, aipd, prop, ipd.getVALUE(), ipdOld.getVALUE());
						}
						ArrayList<String> aSqlFilter = new ArrayList<String>();
						if (clase.equals(Constants.CLS_FORMATO_INDICE))
							checkIndex(ido, ipd, action.SET);
						String sql = subUpdFactInstance(ido, idoNeg, aipd, ipd, ipdOld, table, memo, aSqlFilter); 
						if (sql!=null) {
							//System.out.println("UPDATE FACT_INSTANCE:SQL" + sql);
							AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
							log(ipd, ido, idto, "SET_FACTINSTANCE", action.SET);
						}
						Iterator it = aSqlFilter.iterator();
						while (it.hasNext()) {
							String sqlFilter = (String)it.next();
							AuxiliarQuery.dbExecUpdate(factConnDB, sqlFilter, false);
						}
					}
				}
				if (!actualizar) { // se trata de ADD
					putCardinality(ido, idto, prop, ipd.getVALUECLS().equals(Constants.IDTO_MEMO), action.NEW);
					
					boolean nuevo = idoNeg!=null && idoNeg<0;
					if (!nuevo && compruebaReIndex && (ipd.getVALUE()!=null || ipd.getQMIN()!=null)) {
						System.out.println("idto " + idto);
						if (((IKnowledgeBaseInfoServer)ik).getClassesIndexWithSpecialized().contains(idto))
							processReIndex(ido, idto, aipd, prop, null, null);
					}
					
					ArrayList<String> aSqlFilter = new ArrayList<String>();
					if (clase.equals(Constants.CLS_FORMATO_INDICE)) {
						int op = action.NEW;
						//boolean nuevo = idoNeg!=null && idoNeg<0;
						if (!nuevo) op = action.SET;
						checkIndex(ido, ipd, op);
					}
					String sql = subNewFactInstance(ido, idoNeg, rdn, aipd, ipd, aSqlFilter);
					if (sql!=null) {
						//System.out.println("NEW FACT_INSTANCE:SQL" + sql);
						AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
						log(ipd, ido, idto, "NEW_FACTINSTANCE", action.NEW);
					}
					Iterator it = aSqlFilter.iterator();
					while (it.hasNext()) {
						String sqlFilter = (String)it.next();
						//System.out.println("sqlFilter " + sqlFilter);
						AuxiliarQuery.dbExecUpdate(factConnDB, sqlFilter, false);
					}
				}
			/*} else {
				throw new DataErrorException(DataErrorException.DUPLICATE_DATA, "El valor para " + ik.getPropertyName(prop) + 
					" debe ser unico y ya se encuentra en base de datos");
			}*/
		}
		//System.out.println("Fin de la funcion subNewUpdFactInstance");
	}
	
	private void processValueTmp(StringBuffer textoReturn, String texto, int idoNeg, int ido, 
			int idto, ArrayList<IPropertyDef> aipd) throws SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, SQLException, JDOMException, ParseException {
		String valueIndex = null;
		System.out.println("texto " + texto);
		boolean salir = false;
		int fin = 0;
		while (!salir) {
			if (fin!=0) fin++;
			int inicio = texto.indexOf(Constants.DEFAULT_RDN_CHAR, fin);
			System.out.println("inicio " + inicio);
			if (inicio!=-1) {
				System.out.println("fin1 " + fin);
				String datos = texto.substring(fin, inicio);
				System.out.println("datos " + datos);
				textoReturn.append(datos);
				fin = texto.indexOf(Constants.DEFAULT_RDN_CHAR, inicio+1);
				System.out.println("fin2 " + fin);
				String idoStr = texto.substring(inicio+1, fin);
				if (Auxiliar.hasIntValue(idoStr)) {
					int idoNegTmp = Integer.parseInt(idoStr);
					if (idoNegTmp<0) {
						//si el ido negativo coincide con el suyo poner el ido, 
						//si no buscar el rdn que corresponde a ese ido negativo
						if (idoNeg==idoNegTmp) {
							if (valueIndex==null) {
								if (((IKnowledgeBaseInfoServer)ik).getClassesIndexWithSpecialized().contains(idto))
									valueIndex = processIndex(idoNeg, ido, idto, aipd);
								if (valueIndex==null)
									valueIndex = String.valueOf(ido);
							}
							textoReturn.append(valueIndex);
						} else {
							//buscar rdn
							IdoObject io = idoFicticioReal.get(idoNegTmp);
							if (io!=null) {
								textoReturn.append((String)io.getObject());
							} else {
								IPropertyDef ipdF = findFactInstanceByIdoProp(idoNegTmp, Constants.IdPROP_RDN, aipd);
								io = insertRowObject(idoNegTmp, ipdF.getIDTO(), null, aipd);
								//indivNew = true;
								textoReturn.append((String)io.getObject());
							}
						}
					}
				}
			} else {
				String datos = texto.substring(fin, texto.length());
				System.out.println("datos " + datos);
				textoReturn.append(datos);
				salir = true;
			}
		}
		System.out.println("texto " + texto);
	}

	private String getValueProp(int ido, int idto, Integer prop, ArrayList<IPropertyDef> aipd, 
			boolean lookDB) throws SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NamingException, SQLException, JDOMException, ParseException {
		String value = null;
		if (prop!=null) {
			//buscar el valor de la property en los facts o en bd si no esta en los facts
			IPropertyDef ipd = findFactInstanceByIdoProp(ido, prop, aipd);
			if (ipd!=null) {
				if (ik.isDataProperty(prop)) {
					value = ipd.getVALUE();
					if (value==null && ipd.getQMIN()!=null)
						value = String.valueOf(ipd.getQMIN());
				} else if (ik.isObjectProperty(prop)) {
					int valueInt = Integer.parseInt(ipd.getVALUE());
					IPropertyDef ipd2 = findFactInstanceByIdoProp(valueInt, Constants.IdPROP_RDN, aipd);
					if (ipd2!=null) {
						//hay que ver que si para este fact se ha procesado su rdn
						//si no, procesarlo ahora
						int idoIpd2 = ipd2.getIDO();
						if (idoIpd2<0 && !idoFicticioReal.containsKey(idoIpd2)) {
							int idtoIpd2 = ipd2.getIDTO();
							Integer propApunta = idoFPropStructQueLoApunta.get(idoIpd2);
							insertRowObject(idoIpd2, idtoIpd2, propApunta, aipd);
						}
						value = ipd2.getVALUE();
					} else {
						value = InstanceService.getRdn(factConnDB, valueInt, ipd.getVALUECLS());
						idoRealRdn.put(valueInt, value);
					}
				}
			} else if (lookDB) {
				//mirar en bd
				String DBValue = TransitionObject.dbValueQminOfIdoProp(ido, idto, prop, factConnDB);
				if (DBValue!=null)
					value = DBValue;
			}
		}
		return value;
	}
	private Integer getSpecificIdto(int idto) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException {
		Integer specificIdto = null;
		HashMap<Integer,Integer> idtoPeso = new HashMap<Integer, Integer>();
		int menorPeso = Integer.MAX_VALUE;
		HashSet<Integer> superiors = getSuperiorsIndexed(idto, idtoPeso);
		if (superiors.size()==0) //este caso no se va a dar, a no se ser que se haya borrado un indice y no se refleje en el mapa de indices
			specificIdto = idto;
		else {
			Iterator it = superiors.iterator();
			while (it.hasNext()) {
				int idtoIndex = (Integer)it.next();
				int peso = idtoPeso.get(idtoIndex);
				System.out.println("idtoIndex " + idtoIndex + ", peso " + peso);
				if (peso<menorPeso) {
					menorPeso = peso;
					specificIdto = idtoIndex;
				}
			}
		}
		System.out.println("specificIdto " + specificIdto);
		return specificIdto;
	}

	private HashSet<Integer> getSuperiorsIndexed(int idto, HashMap<Integer,Integer> idtoPeso) throws NotFoundException, IncoherenceInMotorException {
		HashSet<Integer> superiors = new HashSet<Integer>();
		HashSet<Integer> procesados = new HashSet<Integer>();
		int level = 1;
		getSuperiorsItera(level, idto, superiors, procesados, idtoPeso);
		return superiors;
	}
	private void getSuperiorsItera(int level, int idto, HashSet<Integer> superiors, HashSet<Integer> procesados, HashMap<Integer,Integer> idtoPeso) throws NotFoundException, IncoherenceInMotorException {
		procesados.add(idto);
		if (((IKnowledgeBaseInfoServer)ik).getClassesIndex().contains(idto)) {
			superiors.add(idto);
			idtoPeso.put(idto, level);
		}
		Iterator<Integer> it = ik.getSuperior(idto);
		while (it.hasNext()) {
			Integer idtoSup = (Integer)it.next();
			if (!procesados.contains(idtoSup))
				getSuperiorsItera(level+1, idtoSup, superiors, procesados, idtoPeso);
		}
	}
	
	private Integer getMiEmpresaDocument(int ido, int idto, ArrayList<IPropertyDef> aipd) throws SQLException, NamingException {
		Integer miEmpresaDocument = null;
		if (idPropMiEmpresa!=null) {
			miEmpresaDocument = hIdoMiEmpresa.get(ido);
			if (miEmpresaDocument==null && ido>0) {
				miEmpresaDocument = dbValNumOfIdoProp(ido, idto, idPropMiEmpresa);
				hIdoMiEmpresa.put(ido, miEmpresaDocument);
			}
		}
		System.out.println("miEmpresa del documento-> ido:" + ido + ", empresa: " + miEmpresaDocument);
		return miEmpresaDocument;
	}
	
	private void processReIndex(int ido, int idto, ArrayList<IPropertyDef> aipd, int propIN, String valueFilter, 
			String oldValueFilter) 
			throws InstanceLockedException, SQLException, NamingException, DataErrorException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, 
			OperationNotPermitedException, JDOMException, ParseException {
		System.out.println("Inicio de la funcion processReIndex");
		Integer idtoTmp = getSpecificIdto(idto);
		if (idtoTmp!=null) {
			idto = idtoTmp;
			Integer miEmpresaDocument = getMiEmpresaDocument(ido, idto, aipd);
			m_IS.lockObjectTrans(idto, null, user);
			try {
				ArrayList<IndexFilter> aNewIndexF = new ArrayList<IndexFilter>();
				
				String sql = "Select ID_O, " + generateSQL.getCharacterBegin() + "INDEX" + 
						generateSQL.getCharacterEnd() + ",PREFIX, SUFIX, PROPERTY_PREFIX, PROPERTY_FILTER, VALUE_FILTER, GLOBAL_SUFIX, " +
								"MASC_PREFIX_TEMP, PROPERTY_PREFIX_TEMP, CONT_YEAR, LAST_PREFIX_TEMP, MIN_DIGITS, MI_EMPRESA FROM S_Index " + //WITH(nolock) " +
						"WHERE ID_TO=" + idto;
				//devolver tambien registros en los que miempresa es no nula si propin es miempresa
				if (propIN==idPropMiEmpresa)
					sql += " AND MI_EMPRESA=" + miEmpresaDocument;
				else {
					sql += " AND (PROPERTY_FILTER=" + propIN + " OR PROPERTY_PREFIX=" + propIN + " OR PROPERTY_PREFIX_TEMP=" + propIN + ")";// + " AND VALUE_FILTER='" + valueFilter + "'";
					if (miEmpresaDocument!=null && miEmpresaDocument>0)
						sql += " AND (MI_EMPRESA=" + miEmpresaDocument + " OR MI_EMPRESA is null)";
					else
						sql += " AND MI_EMPRESA is null";
				}

				//System.out.println(sql);
	
				boolean hayFiltro = false;
				String values = "";
				Statement st = null;
				ResultSet rs = null;
				ConnectionDB con = factConnDB.createConnection(true);
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					while (rs.next()) {
						int idoIndex = rs.getInt(1);
						int index = rs.getInt(2);
						String prefix = rs.getString(3);
						String sufix = rs.getString(4);
						Integer propPrefix = rs.getInt(5);
						if (rs.wasNull())
							propPrefix = null;
						Integer propFilter = rs.getInt(6);
						if (rs.wasNull())
							propFilter = null;
						String valueF = rs.getString(7);
						Boolean globalSufix = rs.getBoolean(8);
						String mascPrefixTemp = rs.getString(9);
						Integer propPrefixTemp = rs.getInt(10);
						if (rs.wasNull())
							propPrefixTemp = null;
						Integer contYear = rs.getInt(11);
						if (rs.wasNull())
							contYear = null;
						String lastPrefixTemp = rs.getString(12);
						Integer minDigits = rs.getInt(13);
						if (rs.wasNull())
							minDigits = null;
						Integer miEmpresa = rs.getInt(14);
						if (rs.wasNull())
							miEmpresa = null;
						
						boolean insert = false;
						if(propFilter!=null && propFilter.equals(propIN)) {
							if (valueF.equals(valueFilter)) {
								hayFiltro = true;
								insert = true;
								//se usa un mapa, pero ahora mismo solo permite una propFilter por idto
							} else {
								if (values.length()>0)
									values += ",";
								values += valueF;
							}
						} else {
							//entra en else porque la property cambiada coincide con propPrefix o con propTemp
							
							//si miEmpresa es nula, inserto solo si en la tabla no hay otro con el mismo filtrado
							//query a s_index con filtrado de miEmpresa y propFilter
							
							if (propFilter!=null) {
								//ya que no se ha cambiado propFilter
								//ver que si la tiene la cumple
								String dBValue = TransitionObject.dBValueOfIdoProp(ido, idto, propFilter, factConnDB);
								if (dBValue==null) {
									IPropertyDef ipdRdn = findFactInstanceByIdoProp(ido, propFilter, aipd);
									dBValue = ipdRdn.getVALUE();
								}
								if (dBValue!=null && dBValue.equals(valueF))
									insert = true;
							} else
								insert = true;
						}
						if (insert) {
							IndexFilter indexF = new IndexFilter(idoIndex, mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, propPrefix, prefix, index, sufix, globalSufix, minDigits, propFilter, valueF, miEmpresa);
							System.out.println("indexF " + indexF.toString());
							aNewIndexF.add(indexF);
						}
					}
				} finally {
					if (rs != null)
						rs.close();
					if (st != null)
						st.close();
					if (con!=null)
						factConnDB.close(con);
				}
				//if (propNewIndexFilter.size()==0) {
				if (!hayFiltro && values.length()>0) {
					String error = "";
					if (values.split(",").length>1) {
						error = "Debe introducir uno de los siguientes valores " + values + " para " + ik.getPropertyName(propIN);
					} else
						error = "Debe introducir el valor " + values + " para " + ik.getPropertyName(propIN);
					throw new DataErrorException(DataErrorException.ERROR_DATA,error);
				} else if (aNewIndexF.size()>0) {
					//rdn en DB antes de cambiarse
					String rdn = TransitionObject.dBValueOfIdoProp(ido, idto, Constants.IdPROP_RDN, factConnDB);
					//paso el filtrado(prop, value) para saber que indice tengo que comparar con el valor de DB
					
					Iterator it = aNewIndexF.iterator();
					while (it.hasNext()) {
						IndexFilter indexF = (IndexFilter)it.next();
						int index = indexF.getIndex();
						String prefix = indexF.getPrefix();
						String sufix = indexF.getSufix();
						Integer minDigits = indexF.getMinDigits();
						Integer propPrefix = indexF.getPropPrefix();
						boolean globalSufix = false;
						if (seHaceReplica && ir.getSufix()!=null)
							globalSufix = indexF.isGlobalSufix();
						Integer propPrefixTemp = indexF.getPropPrefixTemp();
	
						Integer propFilter = indexF.getPropFilter();
						String valueF = indexF.getValueFilter();
						Integer miEmpresa = indexF.getMiEmpresa();
						boolean decrement = false;
						//if (prop.equals(Constants.IdPROP_RDN)) {
							if (propFilter!=null) {
								if (propFilter.equals(propIN) && valueF.equals(valueFilter))
									decrement = processLastIndex(idto, ido, rdn, propFilter, oldValueFilter, miEmpresa);
								//si propFilter no es nulo -> valueFilter no ha cambiado, por lo que tomo valueF
								else if (propIN==idPropMiEmpresa)
									decrement = processLastIndex(idto, ido, rdn, propFilter, valueF, Integer.parseInt(oldValueFilter));
								else
									decrement = processLastIndex(idto, ido, rdn, propFilter, valueF, miEmpresa);
							} else
								decrement = processLastIndex(idto, ido, rdn, null, null, miEmpresa);
							if (decrement)
								index--;
						//}
						//IndexFilter indexFilterUpdate = new IndexFilter(idoIndex, propFilter, valueFilter, prefix, sufix, propPrefix, index, globalSufix);
						String change = "";
						//boolean continuar = true;
						//IPropertyDef ipd = null;
						//if (prefix==null && propPrefix!=null)
							//ipd = findFactInstanceByProp(ido, propPrefix, aipd);
						//primero comprobar si requiere unicidad
						//ArrayList<Integer> aIdtos = hasUnicity(idto,prop);
						//while (continuar) {
	
							String valuePropPrefix = getValueProp(ido, idto, propPrefix, aipd, true);
							String valuePropPrefixTemp = getValueProp(ido, idto, propPrefixTemp, aipd, true);
							ArrayList<Object> returned = new ArrayList<Object>();
							int action = getValueTemp(valuePropPrefixTemp, indexF, returned);
							boolean incrementIndex = true;
							String valueTemp = null;
							String masc = null;
							if (returned.size()>0) {
								valueTemp = (String)returned.get(0);
								masc = valueTemp;
								if (indexF.getContYear()!=null)
									valueTemp = (String)returned.get(1);
								//buscar en DB el indice que le corresponde
								String allPrefix = valueTemp;
								if (prefix!=null)
									allPrefix += prefix;
								if (valuePropPrefix!=null)
									allPrefix += valuePropPrefix;
								if (action!=IndexFilterFunctions.NOTHING) {
									index = IndexFilterFunctions.getIndexTempDB(factConnDB, idto, allPrefix, sufix); //para ese idto y esos prefijos obtener el ultimo indice usado
									incrementIndex = false;
								}
							}
							String prefixChange = "";
							String sufixChange = "";
							if (valueTemp!=null) {
								change += valueTemp;
								prefixChange += valueTemp;
							}
							if (valuePropPrefix!=null) {
								change += valuePropPrefix;
								prefixChange += valuePropPrefix;
							}
							if (prefix!=null) {
								change += prefix;
								prefixChange += prefix;
							}
							if (minDigits!=null)
								change += StringUtils.leftPad(String.valueOf(index), minDigits, '0');
							else
								change += index;
							if (sufix!=null) {
								change += sufix;
								sufixChange += sufix;
							}
							if (globalSufix) {
								change += ir.getSufix();
								sufixChange += ir.getSufix();
							}
							/*if (aIdtos!=null) {
								//si esta en bd
								//volver a generar value
								//mirar si ya esta en base de datos
								if (isInDB(aIdtos, prop, change, null, null, false))
									index = index+1;
								else
									continuar = false;
							} else
								continuar = false;*/
							//}
							boolean reIndex = true;
							if(propPrefixTemp!=null && propPrefixTemp.equals(propIN) && (propPrefix==null || !propPrefix.equals(propIN)) && (propFilter==null || !propFilter.equals(propIN))) {
								//si el valor anterior coincide con el nuevo excepto en el contador -> no insertar
								//xej, si xa una mascara 'aa' se cambia la fecha sin cambiar el año -> se puede usar el mismo indice
								//pero si cambia el año s� tengo q reindexar
								String oldValueProp = null;
								//if (prop.equals(Constants.IdPROP_RDN))
									oldValueProp = rdn;
								//else
									//oldValueProp = TransitionObject.dBValueOfIdoProp(ido, idto, prop, factConnDB);
								//tenemos change y oldValueProp
								//ver si coincide el prefijo temporal -> no insertar
								if (valueTemp!=null && oldValueProp.length()>valueTemp.length() && oldValueProp.startsWith(valueTemp)) //si coinciden
									reIndex = false;
							}
							
							if (reIndex) {
								//actualizar property
								IPropertyDef ipd = null;
								boolean updateDB = findFactInstanceByPropFilter(ido, Constants.IdPROP_RDN, ipd, propIN, aipd); //prop a la que hay que indexar
								
								boolean updated = false;
								//hay que conseguir el valor antiguo de prop
								//su antiguo índice xa actualizar
								//Soluc -> buscar en bd
								if (ipd!=null) {
									String actualValue = ipd.getVALUE();
									//System.out.println("actualValue " + actualValue);
									//System.out.println("prefixChange " + prefixChange);
									//System.out.println("sufixChange " + sufixChange);
									if (!(actualValue.startsWith(prefixChange) && actualValue.endsWith(sufixChange))) {
										updated = true;
										//si esta acualizo
										//if (ik.isDataProperty(ipd.getPROP())) { los indices son solo sobre DataProperty
											//si ya se ha insertado este ipd no me vale
											//ponerle el ipdOld al ipd y se soluciona
											if (updateDB) {
												FactInstance ipdOldDB = new FactInstance();
												ipdOldDB.setIDO(ido);
												ipdOldDB.setIDTO(idto);
												ipdOldDB.setPROP(Constants.IdPROP_RDN);
												ipdOldDB.setVALUECLS(Constants.IDTO_STRING);
												//ipdOldDB.setVALUE(dBValue(ido, prop));
												ipdOldDB.setVALUE(ipd.getVALUE());
												ipd.setInitialValues(ipdOldDB);
											}
											updateFactInstance(ipd, aipd, ido, ido, Constants.IdPROP_RDN, change, updateDB); 
										//}
									}
								} else {  //si no esta la creo
									String actualValue = TransitionObject.dBValueOfIdoProp(ido, idto, Constants.IdPROP_RDN, factConnDB);
									//System.out.println("actualValue " + actualValue);
									//System.out.println("prefixChange " + prefixChange);
									//System.out.println("sufixChange " + sufixChange);
									if (!(actualValue.startsWith(prefixChange) && actualValue.endsWith(sufixChange))) {
										updated = true;
										FactInstance ipdOldDB = new FactInstance();
										ipdOldDB.setIDO(ido);
										ipdOldDB.setIDTO(idto);
										ipdOldDB.setPROP(Constants.IdPROP_RDN);
										ipdOldDB.setVALUECLS(Constants.IDTO_STRING);
										ipdOldDB.setVALUE(actualValue);
										createFactInstance(aipd, ido, ido, idto, Constants.IdPROP_RDN, change, ipdOldDB, true);
									}
								}
								//si es rdn actualizar o_reg_instancias
								//if (prop==Constants.IdPROP_RDN)
									//updateRowObject(change, ido, idto, aipd, false);
								
								if (updated) {
									//ahora incremento de indice
									if (valueTemp!=null && (action==IndexFilterFunctions.RESTORE || indexF.getLastPrefixTemp()==null)) {
										System.out.println("guarda mascara");
										keepLastMasc(indexF.getIdo(), masc);
										if (action==IndexFilterFunctions.RESTORE && indexF.getContYear()!=null) {
											System.out.println("incrementa año");
											Integer increment = (Integer)returned.get(2);
											incrementYear(indexF.getIdo(), increment);
										}
									}
				
									//ahora incremento de indice
									//if (action==IndexFilterFunctions.RESTORE) no se va a dar
										//keepLast(change, factConnDB, generateSQL);
									if (incrementIndex) {
										int idoIndex = indexF.getIdo();
										incrementIndex(idoIndex, false);
									}
								}
							}
						//}
					}
				}
			} finally {
				m_IS.unlockObjectTrans(idto, null, user);
			}
		}
		//System.out.println("Fin de la funcion processReIndex");
	}

	public static String dBValueOfIdoProp(int ido, int idto, int prop, FactoryConnectionDB factConnDB) 
			throws SQLException, NamingException {
		String value = null;
		//antes buscaba en O_Reg_Instancias
		String sql = "Select VAL_TEXTO FROM O_Datos_Atrib " + //WITH(nolock) " +
			"WHERE ID_O=" + ido + " AND ID_TO=" + idto + " AND PROPERTY=" + prop;
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				value = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return value;
	}
	/*private String dBValueOfValnumProp(int ido, int prop) throws SQLException, NamingException {
		String value = null;
		String sql = "Select VAL_TEXTO FROM O_Datos_Atrib " + //WITH(nolock) " +
				"WHERE VAL_NUM=" + ido + " AND PROPERTY=" + prop;
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				value = rs.getString(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return  value;
	}*/
	private void updateConfiguration(String label, String value) throws SQLException, NamingException {
		String sql = "update configuration set value='" + value + "' where label='" + label +"'";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		if (label.equals("mi_almacen"))
			ir.setActualSystem(value);
	}
	private void insertConfiguration(String label, String value) throws SQLException, NamingException {
		boolean update = false;
		String sql = "Select value from configuration where label='" + label +"'";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				update = true;
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		if (update)
			updateConfiguration(label, value);
		else {
			sql = "insert into configuration(label,value) values('" + value + "','" + label +"')";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			if (label.equals("mi_almacen"))
				ir.setActualSystem(value);
		}
	}
	private String subUpdFactInstance(int ido, Integer idoNeg, ArrayList<IPropertyDef> aipd, IPropertyDef ipd, IPropertyDef ipdOld, 
			String table, boolean memo, ArrayList<String> aSqlFilter) 
			throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subUpdFactInstance");
		String sql = null;
		int prop = ipd.getPROP();
		int idto = ipd.getIDTO();
		
		String destinationSystem = null;
		boolean replication = false;
		//System.out.println("destinationSystem " + destinationSystem);
		if (seHaceReplica) {
			boolean updateReplica = true;
			if (ik.isObjectProperty(prop)) {
				//para no replicar enlaces que apuntan a idos que puede que no esten en el replicado
				updateReplica = ir.seReplica(ipd.getVALUECLS());
				
				Integer idProp = ik.getIdProperty(Constants.PROP_ALMAC�N);
				if (idtoMiEmpresa!=null && idtoMiEmpresa.equals(idto) && idProp.equals(prop)) {
					String value = null;
					Integer valNum = Integer.parseInt(ipd.getVALUE());
					//IPropertyDef ipdFind = findFactInstanceByIdoProp(valNum, Constants.IdPROP_RDN, aipd);
					//if (ipdFind==null) {
						String dBValue = TransitionObject.dBValueOfIdoProp(valNum, ipd.getVALUECLS(), Constants.IdPROP_RDN, factConnDB);
						if (dBValue!=null)
							value = dBValue;
					//} else
						//value = ipdFind.getVALUE();
					if (value!=null) {
						//si es asi, hacer update en configuration
						insertConfiguration("mi_almacen",value);
					}
				}
			/*} else if (prop==Constants.IdPROP_RDN) {
				String clase = ik.getClassName(idto);
				if (clase.equals("ALMAC�N")) {
					String value = null;
					Integer idProp = ik.getIdProperty(Constants.PROP_ALMAC�N);
					//IPropertyDef ipdFind = findFactInstanceByValNumProp(ido, idProp, aipd);
					//if (ipdFind==null) {
						String dBValue = dBValueOfValnumProp(ido, idProp);
						if (dBValue!=null)
							value = dBValue;
					//} else
						//value = ipdFind.getVALUE();
					if (value!=null) {
						//si es asi, hacer update en configuration
						updateConfiguration("mi_almacen",value);
					}
				}*/
			}
			if (updateReplica) {
				//este ido no es negativo
				Boolean replicationMap = hIdosReplication.get(ido);
				if (replicationMap!=null) {
					replication = replicationMap;
					System.out.println("ido en mapa " + replication);
				} else {
					replication = ir.getIdtosReplication(ido, idoNeg, idto, aipd);
					hIdosReplication.put(ido, replication);
				}
				if (replication) {
					destinationSystem = ipd.getDestinationSystem();
					if (destinationSystem==null)
						destinationSystem="*";
					if (ir.getIPCentral()==null && ir.getActualSystem().equals(destinationSystem))
					//soy central y el destino soy yo
						replication = false;
				}
			}
		}
		String set = "", where = "";
		String action = ReplicationEngine.SET;
		if (ipd.isIncremental())
			action = ReplicationEngine.ADD;
		String columnsReplica = "ID,ACTION";
		String valuesReplica = id + ",'" + action + "'";
		
		if (ik.isObjectProperty(prop)) {
			where = " WHERE PROPERTY=" + prop + " AND ID_O=" + ido + " AND ID_TO=" + idto;
			columnsReplica += ",ID_O,ID_TO,PROPERTY";
			valuesReplica += "," + ido + "," + idto + "," + prop;
			
			if (ipd.isAppliedSystemValue()) {
				if (ipd.getSystemValue()!=null) {
					Integer systemValue = Integer.parseInt(ipd.getSystemValue());
					if (systemValue<0) {
						TransitionObject.IdoObject iobj = null;
						if (idoFicticioReal.containsKey(systemValue))
							iobj = idoFicticioReal.get(systemValue);
						else
							iobj = insertRowObject(systemValue, ipd.getVALUECLS(), prop, aipd);
						//indivNew = true;
						systemValue = iobj.getIdo();
					}
					set += "SYS_VAL=" + systemValue;
					columnsReplica += ",SYS_VAL";
					valuesReplica += "," + systemValue;
				} else {
					set += "SYS_VAL=NULL";
				}
			}
			if (ipd.getVALUE()!=null) {
				Integer valNum = Integer.parseInt(ipd.getVALUE());
				String rdn = null;
				//boolean indivNew = false;
				if (valNum<0) {
					TransitionObject.IdoObject iobj = null;
					if (idoFicticioReal.containsKey(valNum))
						iobj = idoFicticioReal.get(valNum);
					else
						iobj = insertRowObject(valNum, ipd.getVALUECLS(), prop, aipd);
					//indivNew = true;
					valNum = iobj.getIdo();
					rdn = (String)iobj.getObject();
				} else {
					rdn = idoRealRdn.get(valNum);
					if (rdn==null) {
						//buscar su rdn en base de datos a partir del ido
						rdn = InstanceService.getRdn(factConnDB, valNum, ipd.getVALUECLS());
						idoRealRdn.put(valNum, rdn);
					}
					if (rdn==null) {
						System.out.println("El individuo con ido " + valNum + " ha sido borrado de base de datos");
						throw new DataErrorException(DataErrorException.ERROR_DATA, "El valor de la propiedad " + 
								ik.getAliasOfProperty(idto, prop, null) + " ya no existe en base de datos");
					}
				}
				processPropRealizado(ido, idto, prop, valNum);
				/*if (!ipdOld.getVALUECLS().equals(ipd.getVALUECLS())){
					if (set.length()>0)
						set += ",";
					set += "VALUE_CLS=" + ipd.getVALUECLS();
					where += " AND VALUE_CLS=" + ipdOld.getVALUECLS();
					columnsReplica += ",VALUE_CLS";
					valuesReplica += "," + ipd.getVALUECLS();
				}*/
				if (!StringUtils.equals(ipdOld.getVALUE(),ipd.getVALUE())) {
					if (set.length()>0)
						set += ",";
					set += "VAL_NUM=" + valNum + ",";
					set += "VAL_TEXTO='" + rdn.replaceAll("'", "''") + "'";
					where += " AND VAL_NUM=" + ipdOld.getVALUE();
				}
				columnsReplica += ",VAL_TEXTO";
				valuesReplica += ",'" + rdn.replaceAll("'", "''") + "'";
				columnsReplica += ",VAL_NUM";
				valuesReplica += "," + valNum;
				columnsReplica += ",OLD_VAL_NUM";
				valuesReplica += "," + ipdOld.getVALUE();
				/*if (ipdOld.getQMIN()!=null && ipd.getQMIN()!=null && ipdOld.getQMIN()!=ipd.getQMIN() 
						|| !(ipdOld.getQMIN()==null && ipd.getQMIN()==null)) {
					if (set.length()>0)
						set += ",";
					set += "Q_MIN=" + ipd.getQMIN();
					if (ipdOld.getQMIN()!=null)
						where += " AND Q_MIN=" + ipdOld.getQMIN();
					else
						where += " AND Q_MIN IS NULL";
				}
				if (ipdOld.getQMAX()!=null && ipd.getQMAX()!=null && ipdOld.getQMAX()!=ipd.getQMAX() 
						|| !(ipdOld.getQMAX()==null && ipd.getQMAX()==null)) {
					if (set.length()>0)
						set += ",";
					set += "Q_MAX=" + ipd.getQMAX();
					if (ipdOld.getQMAX()!=null)
						where += " AND Q_MAX=" + ipdOld.getQMAX();
					else
						where += " AND Q_MAX IS NULL";
				}*/
				if (destinationSystem!=null) {
					if (set.length()>0)
						set += ",";
					set += "DESTINATION='" + destinationSystem.replaceAll("'", "''") + "'";
					columnsReplica += ",DESTINATION";
					valuesReplica += ",'" + destinationSystem.replaceAll("'", "''") + "'";
				}
			}
		} else if (ik.isDataProperty(prop)) {
			where = " WHERE PROPERTY=" + prop + " AND ID_O=" + ido + " AND ID_TO=" + idto;
			columnsReplica += ",ID_O,ID_TO,PROPERTY";
			valuesReplica += "," + ido + "," + idto + "," + prop;
			if (ipd.isAppliedSystemValue()) {
				if (ipd.getSystemValue()!=null) {
					set += "SYS_VAL='" + ipd.getSystemValue().replaceAll("'", "''") + "'";
					columnsReplica += ",SYS_VAL";
					valuesReplica += ",'" + ipd.getSystemValue().replaceAll("'", "''") + "'";
				} else
					set += "SYS_VAL=NULL";
			}
			String value = ipd.getVALUE();
			if (value!=null || ipd.getQMIN()!=null || ipd.getQMAX()!=null) {
				if (prop==Constants.IdPROP_RDN && ipdOld!=null && !StringUtils.equals(ipdOld.getVALUE(),value))
					updateRowObject(value, ido, idto, aipd, true);
			
				if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO)) {
					if (value!=null) {
						if (!StringUtils.equals(ipdOld.getVALUE(),value)) {
							if (set.length()>0)
								set += ",";
							set += "MEMO='" + value.replaceAll("'", "''") + "'";
						}
						columnsReplica += ",MEMO";
						valuesReplica += ",'" + value.replaceAll("'", "''") + "'";
					} else {
						if (set.length()>0)
							set += ",";
						set += "MEMO=NULL";
					}
					/*if (ipdOld.getVALUE()!=null)
						where += " AND MEMO='" + ipdOld.getVALUE().replaceAll("'", "''") + "'";
					else
						where += " AND MEMO IS NULL";*/
				} else {
					/*if (ipdOld!=null && !ipdOld.getVALUECLS().equals(ipd.getVALUECLS())) {
						if (set.length()>0)
							set += ",";
						set += "VALUE_CLS=" + ipd.getVALUECLS();
						where += " AND VALUE_CLS=" + ipdOld.getVALUECLS();
						columnsReplica += ",VALUE_CLS";
						valuesReplica += "," + ipd.getVALUECLS();
					}*/
					if (ipd.isIncremental()) {
						double incremento = 0;
						if (ipdOld!=null)
							incremento = ipd.getQMIN() - ipdOld.getQMIN();
						else
							incremento = ipd.getQMIN();
						if (set.length()>0)
							set += ",";
						set += "Q_MIN=Q_MIN+(" + incremento + ")";
						columnsReplica += ",Q_MIN";
						valuesReplica += "," + incremento;
					} else {
						if (ipdOld.getQMIN()!=null && ipd.getQMIN()!=null && ipdOld.getQMIN()!=ipd.getQMIN() 
								|| !(ipdOld.getQMIN()==null && ipd.getQMIN()==null)) {
							if (set.length()>0)
								set += ",";
							set += "Q_MIN=" + ipd.getQMIN();
						}
						columnsReplica += ",Q_MIN";
						valuesReplica += "," + ipd.getQMIN();
					}

					if (ipd.isIncremental()) {
						double incremento = 0;
						if (ipdOld!=null)
							incremento = ipd.getQMAX() - ipdOld.getQMAX();
						else
							incremento = ipd.getQMAX();
						if (set.length()>0)
							set += ",";
						set += "Q_MAX=Q_MAX+(" + incremento + ")";
						columnsReplica += ",Q_MAX";
						valuesReplica += "," + incremento;
					}else {
						if (ipdOld.getQMAX()!=null && ipd.getQMAX()!=null && ipdOld.getQMAX()!=ipd.getQMAX() 
								|| !(ipdOld.getQMAX()==null && ipd.getQMAX()==null)) {
							if (set.length()>0)
								set += ",";
							set += "Q_MAX=" + ipd.getQMAX();
						}
						columnsReplica += ",Q_MAX";
						valuesReplica += "," + ipd.getQMAX();
					}

					String propStr = ik.getPropertyName(ipd.getPROP());
					if (value!=null) {
						columnsReplica += ",VAL_TEXTO";
						if (propStr.equals(Constants.PROP_PASSWORD)) {
							GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
							String function = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, value);
							if (ipdOld!=null && !StringUtils.equals(ipdOld.getVALUE(),value)) {
								if (set.length()>0)
									set += ",";
								set += "VAL_TEXTO=" + function;
							}
							valuesReplica += "," + function;
						} else {
							if (ipdOld!=null && !StringUtils.equals(ipdOld.getVALUE(),value)) {
								if (set.length()>0)
									set += ",";
								set += "VAL_TEXTO='" + value.replaceAll("'", "''") + "'";
							}
							valuesReplica += ",'" + value.replaceAll("'", "''") + "'";
						}
					} else {
						if (set.length()>0)
							set += ",";
						set += "VAL_TEXTO=NULL";
					}
					/*if (ipdOld.getVALUE() != null) {
						if (propStr.equals(Constants.PROP_PASSWORD)) {
							GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
							String function = generateSQL.getEncryptFunction(InstanceService.keyEncriptacion, ipdOld.getVALUE());
							where += " AND VAL_TEXTO=" + function;
						} else
							where += " AND VAL_TEXTO='" + ipdOld.getVALUE().replaceAll("'", "''") + "'";
					} else
						where += " AND VAL_TEXTO IS NULL";*/
				}
				if (destinationSystem!=null) {
					if (set.length()>0)
						set += ",";
					set += "DESTINATION='" + destinationSystem.replaceAll("'", "''") + "'";
					columnsReplica += ",DESTINATION";
					valuesReplica += ",'" + destinationSystem.replaceAll("'", "''") + "'";
				}
				if (ipd.getVALUECLS().equals(Constants.IDTO_STRING))
					processPropOwner(ido, idto, prop, user, userRol, value);
			}
		} else
			throw new NotFoundException("Property no reconocida como DataProperty ni como ObjectProperty: " + prop);
		
		String clase = ik.getClassName(idto);
		if (clase.equals(Constants.CLS_USER) || clase.equals(Constants.CLS_USERROL)) {
			if (clase.equals(Constants.CLS_USER)) {
				String propStr = ik.getPropertyName(ipd.getPROP());
				updateUser(ido, propStr, ipd.getVALUE(), ipdOld.getVALUE());
			} else
				updateRol(ido, ipd.getVALUE(), ipdOld.getVALUE());
		}
		
		if (set.length()>0) {
			sql = "UPDATE " + table + " SET " + set + where;
			if (replication) {
				columnsReplica += ",DATE";
				valuesReplica += "," + System.currentTimeMillis();
				String tableName = "Replica_Data";
				if (memo)
					tableName += "_Memo";
				String sqlFilter = "INSERT INTO " + tableName + "(" + columnsReplica + ") VALUES (" + valuesReplica + ")";
				aSqlFilter.add(sqlFilter);
			}
		}
		//System.out.println("Fin de la funcion subUpdFactInstance");
		return sql;
	}
	
	private String subNewFactInstance(int ido, Integer idoNeg, String rdn, ArrayList<IPropertyDef> aipd, IPropertyDef ipd, ArrayList<String> aSqlFilter) 
			throws NotFoundException, NamingException, SQLException, SystemException, ApplicationException, IncoherenceInMotorException, 
			IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subNewFactInstance");
		String sql = null;
		//boolean indivNew = false;
		int prop = ipd.getPROP();
		int idto = ipd.getIDTO();
		
		boolean memo = false;
		if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO))
			memo = true;
		
		String destinationSystem = null;
		boolean replication = false;
		if (seHaceReplica) {
			boolean updateReplica = true;
			if (ik.isObjectProperty(prop)) {
				//para no replicar enlaces que apuntan a idos que puede que no esten en el replicado
				updateReplica = ir.seReplica(ipd.getVALUECLS());
				
				Integer idProp = ik.getIdProperty(Constants.PROP_ALMAC�N);
				if (idtoMiEmpresa!=null && idtoMiEmpresa.equals(idto) && idProp.equals(prop)) {
					String value = null;
					Integer valNum = Integer.parseInt(ipd.getVALUE());
					if (valNum<0) {
						TransitionObject.IdoObject iobj = null;
						if (idoFicticioReal.containsKey(valNum))
							iobj = idoFicticioReal.get(valNum);
						else
							iobj = insertRowObject(valNum, ipd.getVALUECLS(), prop, aipd);
						//indivNew = true;
						valNum = iobj.getIdo();
						value = (String)iobj.getObject();
						
						//IPropertyDef ipdFind = findFactInstanceByIdoProp(valNum, Constants.IdPROP_RDN, aipd);
						//value = ipdFind.getVALUE();
					} else {
						String dBValue = TransitionObject.dBValueOfIdoProp(valNum, ipd.getVALUECLS(), Constants.IdPROP_RDN, factConnDB);
						if (dBValue!=null)
							value = dBValue;
					}
					if (value!=null) {
						//si es asi, hacer insert en configuration
						insertConfiguration("mi_almacen",value);
					}
				}
			}
			if (updateReplica) {
				//este ido es negativo -> busqueda en los facts, del idto superior
				Boolean replicationMap = hIdosReplication.get(ido);
				if (replicationMap!=null) {
					replication = replicationMap;
					System.out.println("ido en mapa " + replication);
				} else {
					replication = ir.getIdtosReplication(ido, idoNeg, idto, aipd);
					hIdosReplication.put(ido, replication);
				}
				if (replication) {
					destinationSystem = ipd.getDestinationSystem();
					System.out.println("destinationSystem " + destinationSystem);
					if (destinationSystem!=null 
							&& destinationSystem.endsWith(Constants.DEFAULT_RDN_CHAR)
							&& destinationSystem.startsWith(Constants.DEFAULT_RDN_CHAR)) {
						String idoRdnStr = destinationSystem.substring(1, destinationSystem.length()-1);
						System.out.println("idoRdnStr " + idoRdnStr);
						if (Auxiliar.hasIntValue(idoRdnStr)) {
							int idoRdn = Integer.parseInt(idoRdnStr);
							if (idoRdn<0) {
								//buscar rdn
								IdoObject io = idoFicticioReal.get(idoRdn);
								if (io!=null) {
									destinationSystem = (String)io.getObject();
								} else {
									//obtener la clase de este ido negativo
									IPropertyDef ipdF = findFactInstanceByIdoProp(idoRdn, Constants.IdPROP_RDN, aipd);
									io = insertRowObject(idoRdn, ipdF.getIDTO(), prop, aipd);
									//indivNew = true;
									destinationSystem = (String)io.getObject();
								}
								System.out.println("destinationSystem " + destinationSystem);
							}
						}
					}
					if (destinationSystem==null)
						destinationSystem="*";
					if (ir.getIPCentral()==null && ir.getActualSystem().equals(destinationSystem))
					//soy central y el destino soy yo
						replication = false;
				}
			}
		}
		String sysVal = null;
		if (ipd.isAppliedSystemValue() && ipd.getSystemValue()!=null) {
			if (ik.isObjectProperty(prop)) {
				Integer systemValue = Integer.parseInt(ipd.getSystemValue());
				if (systemValue<0) {
					TransitionObject.IdoObject iobj = null;
					if (idoFicticioReal.containsKey(systemValue))
						iobj = idoFicticioReal.get(systemValue);
					else
						iobj = insertRowObject(systemValue, ipd.getVALUECLS(), prop, aipd);
					//indivNew = true;
					systemValue = iobj.getIdo();
				}
				sysVal = String.valueOf(systemValue);
			} else if (ik.isDataProperty(prop))
				sysVal = ipd.getSystemValue();
		}
		if (ipd.getVALUE()!=null || ipd.getQMIN()!=null || ipd.getQMAX()!=null || sysVal!=null) {
			Integer valNum = null;
			String rdnValNum = null;
			boolean dp = false, op = false;
			if (ik.isDataProperty(prop)) {
				if (!memo)
					dp = true;
			} else if (ik.isObjectProperty(prop)) {
				op = true;
			} else {
				throw new NotFoundException("Property no reconocida como DataProperty ni como ObjectProperty: " + prop);
			}
			String value = ipd.getVALUE();
			if (value!=null) {
				if (dp || memo /*|| prop==Constants.IdPROP_BUSINESSCLASS*/) {
					if (dp) {
						if (ipd.getVALUECLS().equals(Constants.IDTO_STRING) && prop!=Constants.IdPROP_RDN) {
							Pattern p = Pattern.compile(Constants.DEFAULT_RDN_CHAR+"(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR);
						      Matcher m = p.matcher(value);
						      if (m.find()) {
						    	  StringBuffer textoReturn = new StringBuffer("");
						    	  processValueTmp(textoReturn, value, idoNeg, ido, idto, aipd);
						    	  value = textoReturn.toString();
						      }
						}
					}
					String clase = ik.getClassName(idto);
					if (clase.equals(Constants.CLS_USER) || clase.equals(Constants.CLS_USERROL)) {
						if (idoNeg!=null && idoNeg<0) {
							if (clase.equals(Constants.CLS_USER)) {
								String propStr = ik.getPropertyName(prop);
								//insertUser(ido, rdn, propStr, null, ipd.getVALUE());
								newUser(ido, rdn, propStr, value);
							} else
								newRol(ido, value);
								//insertRol(ido, ipd.getVALUE());
						} else {
							if (clase.equals(Constants.CLS_USER)) {
								String propStr = ik.getPropertyName(prop);
								updateUser(ido, propStr, value, null);
							} else
								updateRol(ido, value, null);
						}
					}
					
					if (!memo) {
						if (ipd.getVALUECLS().equals(Constants.IDTO_STRING))
							processPropOwner(ido, idto, prop, user, userRol, value);
					}
				} else if (op) {
					valNum = Integer.parseInt(value);
					if (valNum<0) {
						TransitionObject.IdoObject iobj = null;
						if (idoFicticioReal.containsKey(valNum))
							iobj = idoFicticioReal.get(valNum);
						else
							iobj = insertRowObject(valNum, ipd.getVALUECLS(), prop, aipd);
						//indivNew = true;
						valNum = iobj.getIdo();
						rdnValNum = (String)iobj.getObject();
					} else {
						rdnValNum = idoRealRdn.get(valNum);
						if (rdnValNum==null) {
							//buscar su rdn en base de datos a partir del ido
							rdnValNum = InstanceService.getRdn(factConnDB, valNum, ipd.getVALUECLS());
							idoRealRdn.put(valNum, rdnValNum);
						}
						if (rdnValNum==null) {
							System.out.println("El individuo con ido " + valNum + " ha sido borrado de base de datos");
							throw new DataErrorException(DataErrorException.ERROR_DATA, "El valor de la propiedad " + 
									ik.getAliasOfProperty(idto, prop, null) + " ya no existe en base de datos");
						}
					}
					processPropRealizado(ido, idto, prop, valNum);
					
					String clase = ik.getClassName(idto);
					if (clase.equals(Constants.CLS_USER) || clase.equals(Constants.CLS_USERROL)) {
						if (idoNeg!=null && idoNeg<0) {
							if (clase.equals(Constants.CLS_USER)) {
								String propStr = ik.getPropertyName(prop);
								//insertUser(ido, rdn, propStr, valNum, rdnValNum);
								newUser(ido, rdn, propStr, String.valueOf(valNum));
							} else
								newRol(ido, value);
								//insertRol(ido, ipd.getVALUE());
						} else {
							if (clase.equals(Constants.CLS_USER)) {
								String propStr = ik.getPropertyName(prop);
								updateUser(ido, propStr, String.valueOf(valNum), null);
							} else
								updateRol(ido, String.valueOf(valNum), null);
						}
					}
				}
			}
			
			String columns = "", values="";
			
			String columnsReplica = "ID";
			String valuesReplica = String.valueOf(id);
			if (!ipd.isIncremental()) {
				columnsReplica += ",ACTION";
				valuesReplica += ",'" + ReplicationEngine.NEW + "'";
			} else {
				columnsReplica += ",ACTION";
				valuesReplica += ",'" + ReplicationEngine.ADD + "'";
			}
			
			columns = "ID_TO,ID_O,PROPERTY,";
			if (memo)
				columns += "MEMO";
			else {
				if (op)
					columns += "VAL_NUM,";
				//else if (dp)
				columns += "VAL_TEXTO";
			}
			columns += ",VALUE_CLS";
			if (dp)
				columns += ",Q_MIN,Q_MAX";
			if (sysVal!=null)
				columns += ",SYS_VAL";
			if (destinationSystem!=null)
				columns += ",DESTINATION";
			//sql += ") VALUES(";
			columnsReplica += "," + columns;
			
			values = idto + "," + ido + "," + prop;
			if (op) {
				if (valNum!=null) {
					values += "," + valNum;
					values += ",'" + rdnValNum.replaceAll("'", "''") + "'";
				} else
					values += ",NULL,NULL";
			} else if (memo || dp) {
				if (value!=null) {
					String propStr = ik.getPropertyName(prop);
					if (propStr.equals(Constants.PROP_PASSWORD)) {
						GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
						String function = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, value);
						values += "," + function;
					} else if (propStr.equals(Constants.PROP_RDN)) {
						values += ",'" + rdn.replaceAll("'", "''") + "'";
					} else
						values += ",'" + value.replaceAll("'", "''") + "'";
				} else
					values += ",NULL";
			}
			values += "," + ipd.getVALUECLS();
			if (dp)
				values += "," + ipd.getQMIN() + "," + ipd.getQMAX();
			if (sysVal!=null) {
				if (op)
					values += "," + sysVal;
				else if (memo || dp)
					values += ",'" + sysVal.replaceAll("'", "''") + "'";
			}
			if (destinationSystem!=null)
				values += ",'" + destinationSystem.replaceAll("'", "''") + "'";
			
			valuesReplica += "," + values;

			String table = "";
			if (memo)
				table = "O_Datos_Atrib_Memo";
			else
				table = "O_Datos_Atrib";
			sql = "INSERT INTO " + table + "(" + columns + ") VALUES (" + values + ")";

			if (replication) {
				columnsReplica += ",DATE";
				valuesReplica += "," + System.currentTimeMillis();
				String tableName = "Replica_Data";
				if (memo)
					tableName += "_Memo";
				String sqlFilter = "INSERT INTO " + tableName + "(" + columnsReplica + ") VALUES (" + valuesReplica + ")";
				aSqlFilter.add(sqlFilter);
			}
		}
		//System.out.println("Fin de la funcion subNewFactInstance");
		return sql;
	}
	
	private void processPropRealizado(int ido, int idto, int prop, Integer value) 
			throws NamingException, SQLException, SystemException, NotFoundException, IncoherenceInMotorException, 
			ApplicationException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		if (prop == Constants.IdPROP_ESTADOREALIZACION && value==Constants.IDO_REALIZADO)
			Asigned.close(m_IS, ido, idto, factConnDB, ik);
	}
	private void processPropOwner(int ido, int idto, int prop, String user, Integer userRol, String value) 
			throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		if (prop == Constants.IdPROP_OWNER) {
			if (value == null)
				Asigned.release(m_IS, ido, idto, factConnDB, ik);
			else
				Asigned.asign(m_IS, ido, user, userRol, factConnDB, ik);
		}
	}
	private IPropertyDef findFactInstanceByIdoProp(int ido, int prop, ArrayList<IPropertyDef> aipd) {
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipd = (IPropertyDef)it.next();
			if (ipd.getPROP()==prop && ipd.getIDO()!=null && ipd.getIDO().equals(ido))
				return ipd;
		}
		System.out.println("no encontrado ido: " + ido + " con prop: " + prop);
		return null;
	}
	
	private boolean findFactInstanceByPropFilter(int ido, int prop, IPropertyDef ipd, int propFilter, ArrayList<IPropertyDef> aipd) {
		//updateDB si propFilter esta despues que prop, es decir, prop ya se ha insertado en bd
		boolean updateDB = true;
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipdTmp = (IPropertyDef)it.next();
			if (ipdTmp.getIDO()!=null && ipdTmp.getIDO().equals(ido)) {
				if (ipdTmp.getPROP()==prop) {
					ipd = ipdTmp;
					break;
				} else if (ipdTmp.getPROP()==propFilter) {
					updateDB = false;
				}
			}
		}
		return updateDB;
	}
	/*private Integer preInsertRowObject(Changes changes, Integer userRol, String user, ArrayList<IPropertyDef> aipd, 
			int ido, int idto, HashMap<Integer,Integer> idoFicticioReal) 
			throws SQLException, NamingException, SystemException, NotFoundException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException {
		Integer newIdo = insertRowObject(changes, userRol, user, ido, idto, idoFicticioReal, aipd);
		return newIdo;
	}*/
		
	private ArrayList<IndexFilter> getPropIndexFilter(int oldIdo, int idto, 
			Integer miEmpresaDocument, ArrayList<IPropertyDef> aipd) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		ArrayList<IndexFilter> aIndexF = new ArrayList<IndexFilter>();
		boolean hasPropIndexFilter = getIndexDB(aIndexF, factConnDB, idto, null, null, null, miEmpresaDocument);
		if (aIndexF.size()>0) {
			//ahora recorremos aipd para buscar las prop del mapa que estan
			if (hasPropIndexFilter) {
				Iterator itIpd = aipd.iterator();
				while (itIpd.hasNext()) {
					IPropertyDef ipd = (IPropertyDef)itIpd.next();
					if (ipd.getIDO()!=null && ipd.getIDO().equals(oldIdo)) {
						Iterator it2 = aIndexF.iterator();
						while (it2.hasNext()) {
							IndexFilter indexF = (IndexFilter)it2.next();
							Integer propFilter = indexF.getPropFilter();
							if (propFilter!=null) {
								String valueF = indexF.getValueFilter();
								if (propFilter.equals(ipd.getPROP()) && (ipd.getVALUE()!=null && ipd.getVALUE().equals(valueF) || 
											Auxiliar.hasDoubleValue(valueF) && (ipd.getQMIN()!=null && ipd.getQMIN().equals(Double.parseDouble(valueF)) || 
															ipd.getQMAX()!=null && ipd.getQMAX().equals(Double.parseDouble(valueF))))) {
									indexF.setIs(true);
								}
							}
						}
					}
				}
			}
		}
		return aIndexF;
	}
	
	private boolean getIndexDB(ArrayList<IndexFilter> aIndexF, FactoryConnectionDB factConnDB, 
			int idto, Integer propI, Integer propFilterI, String valueFilterI, Integer miEmpresaDocument) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		boolean hasPropIndexFilter = false;
		String gestorDB = factConnDB.getGestorDB();
		GenerateSQL generateSQL = new GenerateSQL(gestorDB);
		ConnectionDB con = factConnDB.createConnection(true);
		Integer idtoTmp = getSpecificIdto(idto);
		if (idtoTmp!=null) {
			idto = idtoTmp;
			String sql = "Select ID_O, " + generateSQL.getCharacterBegin() + "INDEX" + 
				generateSQL.getCharacterEnd() + ",PREFIX, SUFIX, PROPERTY_PREFIX, PROPERTY_FILTER, VALUE_FILTER, GLOBAL_SUFIX, " +
				"MASC_PREFIX_TEMP, PROPERTY_PREFIX_TEMP, CONT_YEAR, LAST_PREFIX_TEMP, MIN_DIGITS, MI_EMPRESA FROM S_Index " + //WITH(nolock) " +
				"WHERE ID_TO=" + idto;
			if (propI!=null)
				sql += " AND PROPERTY=" + propI;
			if (propFilterI!=null)
				sql += " AND PROPERTY_FILTER=" + propFilterI;
			if (valueFilterI!=null)
				sql += " AND VALUE_FILTER='" + valueFilterI.replaceAll("'", "''") + "'";
			if (miEmpresaDocument!=null && miEmpresaDocument>0)
				sql += " AND (MI_EMPRESA=" + miEmpresaDocument + " OR MI_EMPRESA is null)";
			else
				sql += " AND MI_EMPRESA is null";
			Statement st = null;
			ResultSet rs = null;
			try {
				st = con.getBusinessConn().createStatement();
				System.out.println(sql);
				rs = st.executeQuery(sql);
				while (rs.next()) {
					int ido = rs.getInt(1);
					int index = rs.getInt(2);
					String prefix = rs.getString(3);
					String sufix = rs.getString(4);
					Integer propPrefix = rs.getInt(5);
					if (rs.wasNull())
						propPrefix = null;
					
					Integer propFilter = rs.getInt(6);
					if (rs.wasNull())
						propFilter = null;
					else
						hasPropIndexFilter = true;
					String valueFilter = rs.getString(7);
					Boolean globalSufix = rs.getBoolean(8);
					if (rs.wasNull())
						globalSufix = false;
					String mascPrefixTemp = rs.getString(9);
					Integer propPrefixTemp = rs.getInt(10);
					if (rs.wasNull())
						propPrefixTemp = null;
					Integer contYear = rs.getInt(11);
					if (rs.wasNull())
						contYear = null;
					String lastPrefixTemp = rs.getString(12);
					Integer minDigits = rs.getInt(13);
					Integer miEmpresa = rs.getInt(14);
					//if (valueFilter!=null)
						//valueFilter = parserValueFilter(valueFilter, factConnDB);
					IndexFilterFunctions.createIndex(aIndexF, ido, index, prefix, sufix, propPrefix, propFilter, valueFilter, globalSufix, 
							mascPrefixTemp, propPrefixTemp, contYear, lastPrefixTemp, minDigits, miEmpresa);
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}
		return hasPropIndexFilter;
	}
	
	/*private String parserValueFilter(String valueFilter, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		if (!Auxiliar.hasIntValue(valueFilter)) {
			Statement st = null;
			ResultSet rs = null;
			String sql = "SELECT ID_TO FROM O_Reg_Instancias WHERE RDN='" + valueFilter + "'";
			ConnectionDB conDb = factConnDB.createConnection(true);
			try {
				st = conDb.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				if (rs.next())
					valueFilter = rs.getString(1);
			} finally {
				if (rs!=null)
					rs.close();
				if (st!=null)
					st.close();
				conDb.close();
			}
		}
		return valueFilter;
	}*/
	
	private String processIndex(int oldIdo, int newIdo, int idto, ArrayList<IPropertyDef> aipd) 
		throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, 
		IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
		CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion processIndex");
		String changeRdn = null;
		Integer miEmpresaDocument = getMiEmpresaDocument(oldIdo, idto, aipd);
		m_IS.lockObjectTrans(idto, null, user);
		try {
			ArrayList<IndexFilter> aIndexF = getPropIndexFilter(oldIdo, idto, miEmpresaDocument, aipd);
			//se le asignara el índice y prefijo de la propertyFilter que está en propIndexFilter, si no hay ninguna el del más generico si existe
			//boolean idoInChanges = false;
			//iteracion porque puede haber varias property de este individuos con indice fijado, no solo el rdn

				//para una keyProp concreta:
				//mapa propFilter - string
				//xej 180 - A,C,F,G o R
					//200 - T o J   //este caso no se va a dar
				HashMap<Integer,StringBuffer> valuesFilter = new HashMap<Integer,StringBuffer>();
				IndexFilter indexFilterUpdate = IndexFilterFunctions.createIndexFilter(aIndexF, valuesFilter);
				if (indexFilterUpdate!=null) {
				//if (index!=null) {
					String change = "";
					//boolean continuar = true;
					//IPropertyDef ipd = null;
					//if (prefix==null && propPrefix!=null)
						//ipd = findFactInstanceByProp(oldIdo, propPrefix, aipd);
					//primero comprobar si requiere unicidad
					//ArrayList<Integer> aIdtos = hasUnicity(idto,keyProp);
					//while (continuar) {
						Integer propPrefix = indexFilterUpdate.getPropPrefix();
						String valuePropPrefix = getValueProp(oldIdo, idto, propPrefix, aipd, false);
						
						String prefix = indexFilterUpdate.getPrefix();
						int index = indexFilterUpdate.getIndex();
						String sufix = indexFilterUpdate.getSufix();
						Integer minDigits = indexFilterUpdate.getMinDigits();

						//prefijo temporal
						//obtener valor de la property que contiene la fecha
						Integer propPrefixTemp = indexFilterUpdate.getPropPrefixTemp();
						System.out.println("propPrefixTemp " + propPrefixTemp);
						String valuePropPrefixTemp = getValueProp(oldIdo, idto, propPrefixTemp, aipd, false);
						System.out.println("valuePropPrefixTemp " + valuePropPrefixTemp);
						ArrayList<Object> returned = new ArrayList<Object>();
						int action = getValueTemp(valuePropPrefixTemp, indexFilterUpdate, returned);
						System.out.println("action " + action);
						boolean reiniciar = false;
						boolean incrementIndex = true;
						String valueTemp = null;
						String masc = null;
						if (returned.size()>0) {
							valueTemp = (String)returned.get(0);
							//System.out.println("valueTemp " + valueTemp);
							masc = valueTemp;  //como ultimo valor lo guardamos con el año real
							System.out.println("masc " + masc);
							//recuperar el valueTempContYear si existe
							if (indexFilterUpdate.getContYear()!=null)
								valueTemp = (String)returned.get(1);
							
							System.out.println("valueTemp " + valueTemp);
							if (action==IndexFilterFunctions.RESTORE) {
								index = 1;
								reiniciar = true;
							} else if (action==IndexFilterFunctions.SEARCH_LAST) {
								String allPrefix = valueTemp;
								if (prefix!=null)
									allPrefix += prefix;
								if (valuePropPrefix!=null)
									allPrefix += valuePropPrefix;
								index = IndexFilterFunctions.getIndexTempDB(factConnDB, idto, allPrefix, sufix); //para ese idto y esos prefijos obtener el ultimo indice usado
								incrementIndex = false;
							}
						}
						
						boolean globalSufix = false;
						if (seHaceReplica && ir.getSufix()!=null)
							globalSufix = indexFilterUpdate.isGlobalSufix();

						if (valueTemp!=null)
							change += valueTemp;
						//System.out.println("valuePropPrefix " + valuePropPrefix);
						if (valuePropPrefix!=null)
							change += valuePropPrefix;
						//System.out.println("prefix " + prefix);
						if (prefix!=null)
							change += prefix;
						//System.out.println("index " + index);
						if (minDigits!=null)
							change += StringUtils.leftPad(String.valueOf(index), minDigits, '0');
						else
							change += index;
						//System.out.println("sufix " + sufix);
						if (sufix!=null)
							change += sufix;
						//System.out.println("globalSufix " + globalSufix);
						if (globalSufix)
							change += ir.getSufix();
						
						/*if (aIdtos!=null) {
							//si esta en bd
							//volver a generar value
							//mirar si ya esta en base de datos
							if (isInDB(aIdtos, keyProp, change, null, null, false))
								index = index+1;
							else
								continuar = false;
						} else
							continuar = false;*/
					//}
					//puede que index sea nulo si en la tabla hay 2 índices con PropertyFilter y ninguna de las 2 está en el aipd 
					//para el individuo actual
					IPropertyDef ipd = findFactInstanceByIdoProp(oldIdo, Constants.IdPROP_RDN, aipd); //prop a la q hay q indexar
					if (ipd!=null) {
						//si esta acualizo
						//if (ik.isDataProperty(ipd.getPROP())) { los indices son solo sobre DataProperty
							updateFactInstance(ipd, aipd, oldIdo, newIdo, Constants.IdPROP_RDN, change, false); 
							//idoInChanges = true;
							//if (keyProp==Constants.IdPROP_RDN)
								changeRdn = change;
						//}
					} else {  //si no esta la creo
						createFactInstance(aipd, oldIdo, newIdo, idto, Constants.IdPROP_RDN, change, null, false);
						//idoInChanges = true;
						//if (keyProp==Constants.IdPROP_RDN)
							changeRdn = change;
					}
					//ahora incremento de indice
					if (valueTemp!=null && (action==IndexFilterFunctions.RESTORE || indexFilterUpdate.getLastPrefixTemp()==null)) {
						System.out.println("guarda mascara");
						keepLastMasc(indexFilterUpdate.getIdo(), masc);
						if (action==IndexFilterFunctions.RESTORE && indexFilterUpdate.getContYear()!=null) {
							System.out.println("incrementa año");
							Integer increment = (Integer)returned.get(2);
							incrementYear(indexFilterUpdate.getIdo(), increment);
						}
					}
					if (incrementIndex)
						incrementIndex(indexFilterUpdate.getIdo(), reiniciar);
				//}
				} else {
					//"Debe introducir un valor para " + " correspondido entre "
													   //" igual a "
					//NO HACE FALTA//"o un valor para " + " correspondido entre "
					//NO HACE FALTA////"para generar"
					Iterator it2 = valuesFilter.keySet().iterator();
					if (it2.hasNext()) {
						Integer propF = (Integer)it2.next();
						String values = valuesFilter.get(propF).toString();
						String rdnTemp = Constants.DEFAULT_RDN_CHAR + oldIdo + Constants.DEFAULT_RDN_CHAR;
						String error = "Para que se genere el código del documento " +  rdnTemp + " a partir de un índice debe introducir en el campo " + ik.getPropertyName(propF);
						if (values.split(",").length>1)
							error += " uno de los siguientes valores " + values;
						else
							error += " el valor " + values;
						throw new DataErrorException(DataErrorException.ERROR_DATA,error);
					}
				}
				//if (!idoInChanges)
					//buildIndexElement(oldIdo, newIdo, null, null, null);
			} finally {
				m_IS.unlockObjectTrans(idto, null, user);
			}
		//System.out.println("Fin de la funcion processIndex");
		return changeRdn;
	}
	
	private void keepLastMasc(int ido, String prefixTemp) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		Integer propLastPrefixTemp = ik.getIdProperty(Constants.PROP_ULTIMO_PREFIJO_TEMP);
		Integer idtoIndex = ik.getIdClass(Constants.CLS_FORMATO_INDICE);
		String sql = null;
		//ver si el registro existe en bd
		String valuePropPrefixTemp = TransitionObject.dbValueQminOfIdoProp(ido, idtoIndex, propLastPrefixTemp, factConnDB);
		if (valuePropPrefixTemp==null) {
			sql = "INSERT INTO O_Datos_Atrib(id_to, id_o, property, val_texto, value_cls) " +
					"VALUES(" + idtoIndex + "," + ido + "," + propLastPrefixTemp + ",'" + prefixTemp + "'," + Constants.IDTO_STRING + ")";
		} else {
			sql = "UPDATE O_Datos_Atrib SET VAL_TEXTO='" + prefixTemp + "' " +
				"WHERE ID_O=" + ido + " AND ID_TO=" + idtoIndex + " AND PROPERTY=" + propLastPrefixTemp;
		}
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}

	private int getValueTemp(String valuePropPrefixTemp, 
			IndexFilter indexFilterUpdate, 
			ArrayList<Object> returned) {
		String mascPrefixTemp = indexFilterUpdate.getMascPrefixTemp();
		Integer contYear = indexFilterUpdate.getContYear();
		String lastPrefixTemp = indexFilterUpdate.getLastPrefixTemp();
		return IndexFilterFunctions.getValueTemp(valuePropPrefixTemp, 
				mascPrefixTemp, contYear, lastPrefixTemp, 
				returned);
	}
	
	private void incrementYear(int ido, int increment) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		//String sql = "UPDATE S_Index SET " + IndexFilter.setIncrementIndex(indexFilterUpdate.getIndex(), gSQL) + " WHERE " + IndexFilter.whereIncrementIndex(indexFilterUpdate, keyProp, idto, compruebaFilter);
		//incremento de indice ahora es sobre O_Datos_Atrib
		Integer idtoIndex = ik.getIdClass(Constants.CLS_FORMATO_INDICE);
		String nameProp = Constants.PROP_CONTADOR_año;
		String sqlPropContYear = "(SELECT PROP FROM PROPERTIES WHERE NAME='" + nameProp + "')";
		String sql = "UPDATE O_Datos_Atrib SET Q_MIN=Q_MIN+" + increment + ",Q_MAX=Q_MAX+" + increment + 
			" WHERE ID_O=" + ido + " AND ID_TO=" + idtoIndex + " AND PROPERTY=" + sqlPropContYear;
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void incrementIndex(int ido, boolean reiniciar) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		//String sql = "UPDATE S_Index SET " + IndexFilter.setIncrementIndex(indexFilterUpdate.getIndex(), gSQL) + " WHERE " + IndexFilter.whereIncrementIndex(indexFilterUpdate, keyProp, idto, compruebaFilter);
		//incremento de indice ahora es sobre O_Datos_Atrib
		Integer idtoIndex = ik.getIdClass(Constants.CLS_FORMATO_INDICE);
		String nameProp = Constants.PROP_INICIO_CONTADOR;
		String sqlPropIndex = "(SELECT PROP FROM PROPERTIES WHERE NAME='" + nameProp + "')";
		String sql = null;
		if (reiniciar)
			sql = "UPDATE O_Datos_Atrib SET Q_MIN=2,Q_MAX=2 " +
					"WHERE ID_O=" + ido + " AND ID_TO=" + idtoIndex + " AND PROPERTY=" + sqlPropIndex;
		else
			sql = "UPDATE O_Datos_Atrib SET Q_MIN=Q_MIN+1,Q_MAX=Q_MAX+1 " +
					"WHERE ID_O=" + ido + " AND ID_TO=" + idtoIndex + " AND PROPERTY=" + sqlPropIndex;
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void decrementIndex(int ido, FactoryConnectionDB factConnDB, GenerateSQL gSQL) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		//String sql = "UPDATE S_Index SET " + IndexFilter.setDecrementIndex(indexFilterUpdate.getIndex(), gSQL) + " WHERE " + IndexFilter.whereIncrementIndex(indexFilterUpdate, keyProp, idto, compruebaFilter);
		//decremento de indice ahora es sobre O_Datos_Atrib
		Integer idtoIndex = ik.getIdClass(Constants.CLS_FORMATO_INDICE);
		String nameProp = Constants.PROP_INICIO_CONTADOR;
		String sqlPropIndex = "(SELECT PROP FROM PROPERTIES WHERE NAME='" + nameProp + "')";
		String sql = "UPDATE O_Datos_Atrib SET Q_MIN=Q_MIN-1,Q_MAX=Q_MAX-1 " +
				"WHERE ID_O=" + ido + " AND ID_TO=" + idtoIndex + " AND PROPERTY=" + sqlPropIndex;
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	
	private void updateFactInstance(IPropertyDef ipd, ArrayList<IPropertyDef> aipd, int oldIdo, int newIdo, int prop, 
			String newValue, boolean updateDB) throws SQLException, NamingException, NotFoundException, 
			SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, 
			RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, OperationNotPermitedException, 
			JDOMException, ParseException {
		String oldValue = ipd.getVALUE();
		((FactInstance)ipd).setVALUE(newValue);
		
		if (updateDB) {
			buildIndexElement(oldIdo, newIdo, prop, oldValue, newValue);
			FactInstance fi = new FactInstance();
			fi.setIDO(newIdo);
			fi.setIDTO(ipd.getIDTO());
			fi.setPROP(prop);
			fi.setVALUECLS(Constants.IDTO_STRING);
			fi.setVALUE(newValue);
			
			Integer idoNeg = null;
			if (oldIdo<0)
				idoNeg = oldIdo;
			subNewUpdFactInstance(false, aipd, idoNeg, fi, false);
			System.out.println("FactInstance Actualizado: " + fi);
		} else {
			System.out.println("FactInstance Actualizado: ido->" + newIdo + ", prop->" + prop + ", value->" + newValue);
		}
	}
	
	private void createFactInstance(ArrayList<IPropertyDef> aipd, int oldIdo, 
			int newIdo, int idto, int prop, String newValue, IPropertyDef ipdOld, boolean addChanges) 
			throws SQLException, NamingException, NotFoundException, SystemException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		FactInstance fi = new FactInstance();
		fi.setIDO(newIdo);
		fi.setIDTO(idto);
		fi.setPROP(prop);
		fi.setVALUECLS(Constants.IDTO_STRING);
		fi.setVALUE(newValue);
		boolean creacion = true;
		boolean compruebaReIndex = true;
		if (ipdOld!=null) {
			fi.setInitialValues(ipdOld);
			creacion = false;
			compruebaReIndex = false;
		}
		if (addChanges)
			buildIndexElement(oldIdo, newIdo, prop, null, newValue);
		//false o true dependiendo del indice
		Integer idoNeg = null;
		if (oldIdo<0)
			idoNeg = oldIdo;
		subNewUpdFactInstance(creacion, aipd, idoNeg, fi, compruebaReIndex);
		System.out.println("FactInstance Creado: " + fi);
	}
	
	private void buildIndexElement(int oldIdo, int newIdo, Integer prop, String oldValue, String newValue) {
		ObjectChanged oi = new ObjectChanged();
		oi.setOldIdo(oldIdo);
		oi.setNewIdo(newIdo);
		if (prop!=null) {  //puede que prop sea null xq no tenga ningun índice asignado
			oi.setProp(prop);
			StringValue sv = new StringValue(newValue);
			oi.setNewValue(sv);
		}
		if (oldValue!=null) {
			StringValue sv = new StringValue(oldValue);
			oi.setOldValue(sv);
		}
		changes.addObjectChanged(oi);
	}
	
	private void updateLink(HashSet<Integer> hIdtos, String rdn, int id_o, int id_to) throws SQLException, NamingException {
		String update = "UPDATE O_Datos_Atrib SET VAL_TEXTO='" + rdn + "' " +
				"WHERE ID_TO IN(" + Auxiliar.hashSetIntegerToString(hIdtos, ",") + ") AND VAL_NUM=" + id_o + " AND VALUE_CLS=" + id_to;
		AuxiliarQuery.dbExecUpdate(factConnDB, update, false);
	}
	private boolean isStructuralPropPointing(HashSet<Integer> hIdtos, int ido, int idto) throws SQLException, NamingException, NotFoundException {
		boolean is = false;
		Statement st = null;
		ResultSet rs = null;
		System.out.println("UPDATE ROW");
		//String sql = "Select id_O FROM O_Reg_Instancias WITH(nolock) WHERE rdn='" + rdn + "'";
		String sql = "Select distinct property FROM O_Datos_Atrib " +
				"WHERE ID_TO IN(" + Auxiliar.hashSetIntegerToString(hIdtos, ",") + ") AND val_num=" + ido;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			while (rs.next()) {
				int idProp = rs.getInt(1);
				if (ik.getCategory(idProp).isStructural()) {
					is = true;
					break;
				}
			}
		} finally {
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}

		return is;
	}
	private void updateRowObject(String rdn, int id_o, int id_to, ArrayList<IPropertyDef> aipd, boolean comprueba) 
			throws NamingException, SQLException, DataErrorException, NotFoundException {
		System.out.println("idto " + id_to);
		HashSet<Integer> hIdtos = AuxiliarModel.getIdtosThatPointTo(factConnDB, id_to);

		if (comprueba) {
			boolean propStruct = false;
			if (hIdtos.size()>0)
				//buscar en bd si alguna property estructural apunta a este ido
				propStruct = isStructuralPropPointing(hIdtos, id_o, id_to);
			
			System.out.println("UPDATE ROW");
			//String sql = "Select id_O FROM O_Reg_Instancias WITH(nolock) WHERE rdn='" + rdn + "'";
			if (!propStruct) {
				Integer miEmpresaDocument = getMiEmpresaDocument(id_o, id_to, aipd);

				//antes buscaba en O_Reg_Instancias
				Statement st = null;
				ResultSet rs = null;
				String sql = "Select distinct a.id_To FROM O_Datos_Atrib as a" /*WITH(nolock) */ ;
				if (miEmpresaDocument!=null) {
					sql += " INNER JOIN O_Datos_Atrib as b";
					sql += " ON(a.id_o=b.id_o and b.property=(select prop from properties where name='" + Constants.prop_mi_empresa + "') and b.val_num=" + miEmpresaDocument + ")";
				}
				sql += " WHERE a.val_texto='" + rdn.replaceAll("'", "''") + "' " +
					"and a.property=" + Constants.IdPROP_RDN + " and a.id_o<>" + id_o + " and a.id_to=" + id_to;
				
				ConnectionDB con = factConnDB.createConnection(true);
				try {
					st = con.getBusinessConn().createStatement();
					rs = st.executeQuery(sql);
					if (rs.next()) {
					//while (rs.next()) {
						//Integer idtoDB = rs.getInt(1);
						//if (id_to==idtoDB /*|| ik.isSpecialized(idtoDB, id_to)*/) {
							throw new DataErrorException(DataErrorException.ERROR_DATA, "El valor " + rdn + " tiene que ser unico y " +
									"ya se encuentra en base de datos");
						//}
					}
				} finally {
					if (st != null)
						st.close();
					if (con!=null)
						factConnDB.close(con);
				}
			}
		}
		String sql = "UPDATE O_Reg_Instancias_Index SET rdn='" + rdn.replaceAll("'", "''") + "' WHERE id_o=" + id_o + " AND id_to=" + id_to;
		int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		if (seHaceReplica && rows==0) { //TODO no se va a dar porque aun no estan implementados los rebotes
			sql = "UPDATE O_Reg_Instancias_Index_Replica SET rdn='" + rdn.replaceAll("'", "''") + "' WHERE id_o=" + id_o + " AND id_to=" + id_to;
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
		if (hIdtos.size()>0)
			updateLink(hIdtos, rdn, id_o, id_to);
		log(null, id_o, id_to, "SET_OBJ", action.SET);
	}
	private TransitionObject.IdoObject insertRowObject(int oldIdo, int id_to, Integer prop, ArrayList<IPropertyDef> aipd) 
			throws NamingException, SQLException, SystemException, NotFoundException, ApplicationException, 
			IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		String random = user + "/" + String.valueOf((new Random()).nextInt());
		String sql = "insert into O_Reg_Instancias_Index(id_to, rdn) values (" + id_to + ",'" + random + "')";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		String sufix = null;
		
		//si es un individuo global
		//el ido es prefijo + numeros de rdn + sufijo 000
		//inserto en O_Reg_Instancias_Index_Replica
		
		int ido = 0;
		int autonum = 0;
		//boolean isGlobalIndividual = false;

		if (seHaceReplica) {
			//isGlobalIndividual = ir.isGlobalIndividual(id_to);
			sufix = ir.getSufix();
		}
		System.out.println(sufix);
		//if (!isGlobalIndividual) {
			if (sufix!=null)
				sql = "Select autonum, concat(autonum,'"+sufix+"')" +
					" FROM O_Reg_Instancias_Index " + /*WITH(nolock) */ "WHERE rdn='" + random + "'" + " AND id_to=" + id_to;
			else
				sql = "Select autonum" +
					" FROM O_Reg_Instancias_Index " + /*WITH(nolock) */ "WHERE rdn='" + random + "'" + " AND id_to=" + id_to;
		
			Statement st = null;
			ResultSet rs = null;
			System.out.println("INSERT ROW");
			ConnectionDB con = factConnDB.createConnection(true);
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sql);
				rs.next();
				autonum = rs.getInt(1);
				if (sufix!=null)
					ido = rs.getInt(2);
				else
					ido = autonum;
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
			String rdn = getNewRdn(oldIdo, ido, id_to, prop, aipd);
		//}
		//if (!isGlobalIndividual) {
			sql = "update O_Reg_Instancias_Index SET id_o=" + ido + ", rdn='" + rdn.replaceAll("'", "''") + "' WHERE autonum=" + autonum;
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		/*} else {
			//TODO de momento se pone asi para quitar el prefijo A/T
			String idoStr = rdn.substring(1,rdn.length()) + "000";
			ido = Integer.parseInt(idoStr);
			sql = "insert into O_Reg_Instancias_Index_Replica(id_o,id_to,rdn) VALUES(" + ido + "," + id_to + ",'" + rdn.replaceAll("'", "''") + "')";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql);
		}*/
		TransitionObject.IdoObject idoObj = new TransitionObject.IdoObject(ido,rdn);
		idoFicticioReal.put(oldIdo, idoObj);
		idoRealRdn.put(ido, rdn);
		log(null, ido, id_to, "NEW_OBJ", action.NEW);
		return idoObj;
	}
	/*private String getNewRdn(int oldIdo, int ido, int id_to, Integer prop, ArrayList<IPropertyDef> aipd) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		String rdn = null;
		IPropertyDef ipd = findFactInstanceByIdoProp(oldIdo, Constants.IdPROP_RDN, aipd);
		//System.out.println(ipd);
		//System.out.println("prop " + prop + " idto " + id_to);
		if (ipd!=null)
			rdn = ipd.getVALUE();
		
		Pattern p = Pattern.compile(Constants.DEFAULT_RDN_CHAR+"(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR);
		Matcher m = p.matcher(rdn);
		if (m.find()) {
			//tiene indice o rdn de otra clase
	    	  StringBuffer textoReturn = new StringBuffer("");
	    	  processValueTmp(textoReturn, rdn, oldIdo, ido, id_to, aipd);
	    	  rdn = textoReturn.toString();
	    	  
	    	  buildIndexElement(oldIdo, ido, Constants.IdPROP_RDN, null, rdn);
		} else {
			buildIndexElement(oldIdo, ido, null, null, null);
			boolean noStructural = prop==null || prop!=null && !ik.getCategory(prop).isStructural();
			if (noStructural)
				compruebaIdto(rdn, id_to, prop);
		}
		return rdn;
	}*/
	private String getNewRdn(int oldIdo, int ido, int id_to, Integer prop, ArrayList<IPropertyDef> aipd) throws SQLException, NamingException, DataErrorException, SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, JDOMException, ParseException {
		String rdn = null;
//		if (prop==null || prop!=null && !ik.getCategory(prop).isStructural())
		if (((IKnowledgeBaseInfoServer)ik).getClassesIndexWithSpecialized().contains(id_to)) {
			rdn = processIndex(oldIdo, ido, id_to, aipd);
			if (rdn!=null)
		    	  buildIndexElement(oldIdo, ido, Constants.IdPROP_RDN, null, rdn);
		}
		if (rdn==null) {
			//boolean manual = ((IKnowledgeBaseInfoServer)ik).getManualClasses().contains(id_to);
			boolean noStructural = prop==null || prop!=null && !ik.getCategory(prop).isStructural();
			
			IPropertyDef ipd = findFactInstanceByIdoProp(oldIdo, Constants.IdPROP_RDN, aipd);
			//System.out.println(ipd);
			//System.out.println("prop " + prop + " idto " + id_to);
			if (ipd!=null)
				rdn = ipd.getVALUE();
			boolean systemRdn = false;
			if (rdn==null) {
				rdn = String.valueOf(ido);
				systemRdn = true;
			} else {
				Pattern p = Pattern.compile(Constants.DEFAULT_RDN_CHAR+"(\\-?)[0-9]+"+Constants.DEFAULT_RDN_CHAR);
				Matcher m = p.matcher(rdn);
				if (m.find()) {
					String rdnTmp = "";
					System.out.println("rdn " + rdn);
					boolean salir = false;
					int fin = 0;
					while (!salir) {
						if (fin!=0) fin++;
						int inicio = rdn.indexOf(Constants.DEFAULT_RDN_CHAR, fin);
						System.out.println("inicio " + inicio);
						if (inicio!=-1) {
							System.out.println("fin1 " + fin);
							String datos = rdn.substring(fin, inicio);
							System.out.println("datos " + datos);
							rdnTmp += datos;
							fin = rdn.indexOf(Constants.DEFAULT_RDN_CHAR, inicio+1);
							System.out.println("fin2 " + fin);
							String idoStr = rdn.substring(inicio+1, fin);
							if (Auxiliar.hasIntValue(idoStr)) {
								int idoNeg = Integer.parseInt(idoStr);
								if (idoNeg<0) {
									//si el ido negativo coincide con el suyo poner el ido, 
									//si no buscar el rdn que corresponde a ese ido negativo
									if (oldIdo==idoNeg) {
										rdnTmp += String.valueOf(ido);
									} else {
										//buscar rdn
										IdoObject io = idoFicticioReal.get(idoNeg);
										if (io!=null) {
											rdnTmp += (String)io.getObject();
										} else {
											IPropertyDef ipdF = findFactInstanceByIdoProp(idoNeg, Constants.IdPROP_RDN, aipd);
											io = insertRowObject(idoNeg, ipdF.getIDTO(), prop, aipd);
											//indivNew = true;
											rdnTmp += (String)io.getObject();
										}
									}
									systemRdn = true;
								}
							}
						} else {
							String datos = rdn.substring(fin, rdn.length());
							System.out.println("datos " + datos);
							rdnTmp += datos;
							salir = true;
						}
					}
					rdn = rdnTmp;
				}
				System.out.println("rdn " + rdn);
			}
			
			if (systemRdn) {
				
				//if (!manual && noStructural || !noStructural) {
					//si no es estructural y no es manual
					//o si es estructural
					//el rdn sera el ido
					//rdn = String.valueOf(ido);
					buildIndexElement(oldIdo, ido, Constants.IdPROP_RDN, null, rdn);
				/*} else {
					if (rdn==null)
						throw new DataErrorException("El rdn para crear el nuevo individuo con ido ficticio " + oldIdo + " no está fijado");
					else { //es no estructural y manual y el usuario ha puesto un gui�n
						//if (noStructural) {
							buildIndexElement(oldIdo, ido, null, null, null);
							 //si es manual y el usuario pone un -, buscar repetidos
							if (noStructural)
								compruebaIdto(rdn, id_to, prop);
						//}
						//sql = "Select id_O FROM O_Reg_Instancias WITH(nolock) WHERE rdn='" + rdn + "'";
						//rs = st.executeQuery(sql);
						//if (rs.next())
							//throw new DataErrorException(DataErrorException.ERROR_RDN, "El valor " + rdn + " tiene que ser unico y " +
								//"ya se encuentra en base de datos");
					}
				}*/
			} else {
				buildIndexElement(oldIdo, ido, null, null, null);
				if (noStructural)
					compruebaIdto(rdn, id_to, prop);
				//sql = "Select id_O FROM O_Reg_Instancias WITH(nolock) WHERE rdn='" + rdn + "'";
				//rs = st.executeQuery(sql);
				//if (rs.next())
					//throw new DataErrorException(DataErrorException.ERROR_RDN, "El valor " + rdn + " tiene que ser unico y " +
						//"ya se encuentra en base de datos");
			}
		}
		return rdn;
	}
	
	private void compruebaIdto(String rdn, int id_to, Integer prop) throws SQLException, NamingException, DataErrorException {
		//antes buscaba en O_Reg_Instancias
		String sql = "Select distinct id_To FROM O_Datos_Atrib " + //WITH(nolock)  
			"WHERE val_texto='" + rdn.replaceAll("'", "''") + "' AND PROPERTY=" + Constants.IdPROP_RDN
			+ " and id_to=" + id_to;
		
		Statement st2 = null;
		ResultSet rs2 = null;
		System.out.println("INSERT ROW");
		ConnectionDB con2 = factConnDB.createConnection(true);
		try {
			st2 = con2.getBusinessConn().createStatement();
			rs2 = st2.executeQuery(sql);
			if (rs2.next()) {
			//while (rs2.next()) {
				//Integer idtoDB = rs2.getInt(1);
				System.out.println("property no estructural " + prop);
				//if (id_to==idtoDB //|| ik.isSpecialized(idtoDB, id_to)
				//) {
					throw new DataErrorException(DataErrorException.ERROR_DATA, "El valor " + rdn + " tiene que ser unico y " +
							"ya se encuentra en base de datos");
				//}
			}
		} finally {
			if (rs2 != null)
				rs2.close();
			if (st2 != null)
				st2.close();
			if (con2!=null)
				factConnDB.close(con2);
		}

	}

	private String getIdosIdoObject(ArrayList<IdoObject> idos, HashMap<Integer,HashSet<Integer>> hIdtoIdos, 
			HashSet<Integer> idtosIndexABorrar, String action, Integer idAction) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		String sIdos = "";
		Iterator it = idos.iterator();
		while (it.hasNext()) {
			IdoObject idoIdto = (IdoObject)it.next();
			if (sIdos.length()>0)
				sIdos += ",";
			int ido = idoIdto.getIdo();
			sIdos += ido;
			int idto = (Integer)idoIdto.getObject();
			if (idto==ik.getIdClass(Constants.CLS_FORMATO_INDICE)) {
				//buscar en base de datos su dominio
				int idtoDomain = getDomain(ido);
				idtosIndexABorrar.add(idtoDomain);
			}
			HashSet<Integer> hIdos = hIdtoIdos.get(idto);
			if (hIdos==null) {
				hIdos = new HashSet<Integer>();
				hIdtoIdos.put(idto,hIdos);
			}
			hIdos.add(ido);
			log(null, idoIdto.getIdo(), (Integer)idoIdto.getObject(), action, idAction);
		}
		return sIdos;
	}
	private int getDomain(int ido) throws SQLException, NamingException {
		int idto = 0;
		String sql = "SELECT ID_TO FROM s_index WHERE ID_O=" + ido;
		System.out.println(sql);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				idto = rs.getInt(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return idto;
	}
	
	private void deleteRowObject(String idos) throws NamingException, SQLException, NotFoundException {
		//String sql = "DELETE FROM O_Reg_Instancias_Index WHERE id_o in(" + getIdosIdoObject(idos, "DEL_OBJ", action.DEL_OBJECT) + ")";
		String sql = "DELETE FROM O_Reg_Instancias_Index WHERE id_o in(" + idos + ")";
		int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		if (seHaceReplica && rows==0) { //TODO no se va a dar porque aun no estan implementados los rebotes
			sql = "DELETE FROM O_Reg_Instancias_Index_Replica WHERE id_o in(" + idos + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
	}
	
	private void processDeleteIndexDecrement(ArrayList<IPropertyDef> aipd, HashMap<Integer,HashSet<Integer>> hIdtoIdos) throws SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException {
		//ver cuales tienen indice
		//antes buscaba en O_Reg_Instancias
		String sqlSelect = "select distinct i.id_to, reg.id_o from s_index as i " +
				"inner join o_datos_atrib as reg on(i.id_to=reg.id_to and reg.property=" + Constants.IdPROP_RDN;
		
		boolean first = true;
		sqlSelect += " AND (";
		Iterator it = hIdtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer idto = (Integer)it.next();
			HashSet<Integer> hIdos = hIdtoIdos.get(idto);
			if (!first)
				sqlSelect += " OR ";
			else
				first = false;
			sqlSelect += "reg.ID_TO=" + idto + " AND reg.ID_O IN(" + Auxiliar.hashSetIntegerToString(hIdos, ",") + ")";
		}
		sqlSelect +=")";
		
		sqlSelect += ") order by 1, 2 desc";
		
		Integer oldIdto = null;
		//System.out.println("sqlSelect " + sqlSelect);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		boolean lock = false;
		try {
			System.out.println(sqlSelect);
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlSelect);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				Integer ido = rs.getInt(2);
				if (oldIdto==null || !oldIdto.equals(idto)) {
					//por cada idto bloqueo
					if (oldIdto!=null) {
						m_IS.unlockObjectTrans(oldIdto, null, user);
						lock = false;
					}
					m_IS.lockObjectTrans(idto, null, user);
					lock = true;
					oldIdto = idto;
				}
				//ver cual es el valor en oRegInstancias
				String rdn = InstanceService.getRdn(factConnDB, ido, idto);
				//System.out.println("rdn " + rdn);
				//ver cual fue el ultimo valor en sIndex
				Integer miEmpresaDocument = getMiEmpresaDocument(ido, idto, aipd);
				processLastIndex(idto, ido, rdn, null, null, miEmpresaDocument);
			}
		} finally {
			if (lock) {
				//if (oldIdto!=null)
					m_IS.unlockObjectTrans(oldIdto, null, user);
			}
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
	}
	
	public static String dbValueQminOfIdoProp(int ido, int idto, int prop, FactoryConnectionDB factConnDB) throws SQLException, NamingException {
		String value = null;

		String sql = "Select VAL_TEXTO, Q_MIN FROM O_Datos_Atrib " + //WITH(nolock) " +
					"WHERE ID_O=" + ido + " AND ID_TO=" + idto + " AND PROPERTY=" + prop;
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				value = rs.getString(1);
				if (rs.wasNull()) {
					value = String.valueOf(rs.getInt(2));
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return value;
	}
	private Integer dbValNumOfIdoProp(int ido, int idto, int prop) throws SQLException, NamingException {
		Integer value = null;

		String sql = "Select VAL_NUM FROM O_Datos_Atrib " + //WITH(nolock) " +
					"WHERE ID_O=" + ido + " AND ID_TO=" + idto + " AND PROPERTY=" + prop;
		
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true);
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sql);
			if (rs.next()) {
				value = rs.getInt(1);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return value;
	}
	
	private ArrayList<IndexFilter> getPropIndexFilterDB(int ido, int idto, 
			Integer propFilterI, String oldValueFilterI, Integer miEmpresaDocument) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		//en este caso la clave de este mapa siempre sera el rdn
		//para no duplicar metodos
		ArrayList<IndexFilter> aIndexF = new ArrayList<IndexFilter>();
		boolean hasPropIndexFilter = getIndexDB(aIndexF, factConnDB, idto, Constants.IdPROP_RDN, propFilterI, oldValueFilterI, miEmpresaDocument);
		if (aIndexF.size()>0) {
			//ahora recorremos aipd para buscar las prop del mapa que estan
			if (hasPropIndexFilter) {
				Iterator it2 = aIndexF.iterator();
				while (it2.hasNext()) {
					IndexFilter indexF = (IndexFilter)it2.next();
					if (propFilterI==null) {
						Integer propFilter = indexF.getPropFilter();
						if (propFilter!=null) {
							String valueF = indexF.getValueFilter();
							String valueDb = dbValueQminOfIdoProp(ido, idto, propFilter, factConnDB);
							if (valueDb!=null && valueDb.equals(valueF)) {
								indexF.setIs(true);
							}
						}
					} else
						indexF.setIs(true);
				}
			}
		}
		return aIndexF;
	}

	private boolean processLastIndex(int idto, int ido, String rdn, Integer propFilter, String oldValueFilter, Integer miEmpresaDocument) throws SQLException, NamingException, NotFoundException, IncoherenceInMotorException {
		boolean decrement = false;
		ArrayList<IndexFilter> aIndexF = getPropIndexFilterDB(ido, idto, propFilter, oldValueFilter, miEmpresaDocument);
		//se le asignara el índice y prefijo de la propertyFilter que está en propIndexFilter, si no hay ninguna el del más generico si existe
		//solo hay una clave porque solo queremos saber el rdn
		if (aIndexF.size()>0) {
			//System.out.println("keyProp " + keyProp);
			//para una keyProp concreta:
			//mapa propFilter - string
			//xej 180 - A,C,F,G o R
				//200 - T o J   //este caso no se va a dar
			HashMap<Integer,StringBuffer> valuesFilter = new HashMap<Integer,StringBuffer>();
			IndexFilter indexFilterUpdate = IndexFilterFunctions.createIndexFilter(aIndexF, valuesFilter);
			if (indexFilterUpdate!=null) {
				String change = "";
				Integer propPrefix = indexFilterUpdate.getPropPrefix();
				String valuePropPrefix = null;
				if (propPrefix!=null) {
					//buscar val_texto (el valor de la DP o del rdn si es OP de la property) en bd
					valuePropPrefix = TransitionObject.dBValueOfIdoProp(ido, idto, propPrefix, factConnDB);
				}
				int index = indexFilterUpdate.getIndex() - 1;
				String prefix = indexFilterUpdate.getPrefix();
				String sufix = indexFilterUpdate.getSufix();
				Integer minDigits = indexFilterUpdate.getMinDigits();
				boolean globalSufix = false;
				if (seHaceReplica && ir.getSufix()!=null)
					globalSufix = indexFilterUpdate.isGlobalSufix();
				
				Integer propPrefixTemp = indexFilterUpdate.getPropPrefixTemp();
				String valuePropPrefixTemp = null;
				if (propPrefixTemp!=null) {
					//buscar val_texto (el valor de la DP o del rdn si es OP de la property) en bd
					valuePropPrefixTemp = TransitionObject.dbValueQminOfIdoProp(ido, idto, propPrefixTemp, factConnDB);
				}
				
				ArrayList<Object> returned = new ArrayList<Object>();
				int action = getValueTemp(valuePropPrefixTemp, indexFilterUpdate, returned);
				if (action==IndexFilterFunctions.NOTHING) {
					//decremento solo si estoy en el mismo tiempo
					String valueTemp = null;
					if (returned.size()>0) {
						valueTemp = (String)returned.get(0);
						if (indexFilterUpdate.getContYear()!=null)
							valueTemp = (String)returned.get(1);
					}
					
					//formarlo tambien con los prefijos temporales
					if (valueTemp!=null)
						change += valueTemp;
					if (prefix!=null)
						change += prefix;
					if (valuePropPrefix!=null)
						change += valuePropPrefix;
					if (minDigits!=null)
						change += StringUtils.leftPad(String.valueOf(index), minDigits, '0');
					else
						change += index;
					if (sufix!=null)
						change += sufix;
					if (globalSufix)
						change += ir.getSufix();
					
					//System.out.println("change " + change);
					//si coincide decrementar el indice en 1
					if (rdn.equals(change)) {
						if (propFilter==null)
							decrement = true;
						decrementIndex(indexFilterUpdate.getIdo(), factConnDB, generateSQL);
					}
				}
			}
		}
		return decrement;
	}
	
	private void deleteRowObjects(ArrayList<Integer> idos) throws NamingException, SQLException, NotFoundException, 
			SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, 
			IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException {
		String sql = "DELETE FROM O_Reg_Instancias_Index WHERE id_o IN(" + Auxiliar.arrayIntegerToString(idos, ",") + ")";
		int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		if (seHaceReplica && rows==0) { //TODO no se va a dar porque aun no estan implementados los rebotes
			sql = "DELETE FROM O_Reg_Instancias_Index_Replica WHERE id_o IN(" + Auxiliar.arrayIntegerToString(idos, ",") + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
	}
	
	/*private void subDelPreFactInstance(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws NamingException, SQLException, NotFoundException, DataErrorException {
		//System.out.println("Inicio de la funcion subDelPreFactInstance");
		subDelFactInstance(aipd, ipd);
		Integer prop = ipd.getPROP();
		if (ik.isObjectProperty(prop)) {
			boolean structural = ik.getCategory(prop).isStructural();
			if (structural) {
				String value = ipd.getVALUE();
				if (value!=null) {
					Integer valCls = ipd.getVALUECLS();
					Integer valNum = Integer.parseInt(value);
					subDelAllFactsInstance(aipd, valNum, valCls, null, structural);
				}
			}
		}
		//System.out.println("Fin de la funcion subDelPreFactInstance");
	}*/
	
	private void subDelFactInstance(ArrayList<IPropertyDef> aipd, IPropertyDef ipd) 
			throws NamingException, SQLException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException {
		String table = "";
		boolean memo = false;
		if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO)) {
			table = "O_Datos_Atrib_Memo";
			memo = true;
		} else
			table = "O_Datos_Atrib";
		int ido = ipd.getIDO();
		int idto = ipd.getIDTO();
		//if (ido<0)
		//	ido = idoFicticioReal.get(ido);
		//este caso no se va a dar, el ido no va a ser negativo
		Integer prop = ipd.getPROP();

		String clase = ik.getClassName(idto);
		if (clase.equals(Constants.CLS_USER)) {
			String propStr = ik.getPropertyName(ipd.getPROP());
			deleteUser(ido, propStr, ipd.getVALUE());
		} else if (clase.equals(Constants.CLS_USERROL))
			deleteRol(ido);
		
		if (clase.equals(Constants.CLS_FORMATO_INDICE)) {
			checkIndex(ido, ipd, action.DEL);
		}
		
		putCardinality(ido, idto, prop, ipd.getVALUECLS().equals(Constants.IDTO_MEMO), action.DEL);
		
		String where = " WHERE PROPERTY=" + prop;
		where += " AND ID_O=" + ido + " AND ID_TO=" + idto;
		String columnsReplica = "ID, ACTION, ID_O, ID_TO, PROPERTY";
		String valuesReplica = String.valueOf(id);
		if (ipd.isAppliedSystemValue())
			valuesReplica += ",'" + ReplicationEngine.SET + "',";
		else
			valuesReplica += ",'" + ReplicationEngine.DEL + "',";
		valuesReplica += ido + "," + idto + "," + prop;
		if (ipd.getVALUECLS()!=null) {
			where += " AND VALUE_CLS=" + ipd.getVALUECLS();
			if (!ipd.isAppliedSystemValue()) {
				columnsReplica += ", VALUE_CLS";
				valuesReplica += ", " + ipd.getVALUECLS();
			}
		} else
			where += " AND VALUE_CLS IS NULL";
		/*if (!memo) {
			if (ipd.getQMIN()!=null) {
				where += " AND Q_MIN=" + ipd.getQMIN();
				columnsReplica += ", Q_MIN";
				valuesReplica += ", " + ipd.getQMIN();
			} else
				where += " AND Q_MIN IS NULL";
			
			if (ipd.getQMAX()!=null) {
				where += " AND Q_MAX=" + ipd.getQMAX();
				columnsReplica += ", Q_MAX";
				valuesReplica += ", " + ipd.getQMAX();
			} else
				where += " AND Q_MAX IS NULL";
		}*/
		if (ik.isObjectProperty(prop)) {
			if (ipd.getVALUE()!=null) {
				Integer valNum = Integer.parseInt(ipd.getVALUE());
				if (valNum<0)
					valNum = idoFicticioReal.get(valNum).getIdo();
				where += " AND VAL_NUM=" + valNum;
				if (!ipd.isAppliedSystemValue()) {
					columnsReplica += ", VAL_NUM";
					valuesReplica += ", " + valNum;
				}
			} else
				where += " AND VAL_NUM IS NULL";
		} else if (ik.isDataProperty(prop)) {
			/*if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO)) {
				if (ipd.getVALUE()!=null) {
					where += " AND MEMO='" + ipd.getVALUE().replaceAll("'", "''") + "'";
					columnsReplica += ", MEMO";
					valuesReplica += ", '" + ipd.getVALUE().replaceAll("'", "''") + "'";
				} else
					where += " AND MEMO IS NULL";
			} else {
				String propStr = ik.getPropertyName(ipd.getPROP());
				if (ipd.getVALUE() != null) {
					columnsReplica += ",VAL_TEXTO";
					if (propStr.equals(Constants.PROP_PASSWORD)) {
						GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
						String function = generateSQL.getEncryptFunction(InstanceService.keyEncriptacion, ipd.getVALUE());
						where += " AND VAL_TEXTO=" + function;
						columnsReplica += ", VAL_TEXTO";
						valuesReplica += ", " + function;
					} else {
						where += " AND VAL_TEXTO='" + ipd.getVALUE().replaceAll("'", "''") + "'";
						columnsReplica += ", VAL_TEXTO";
						valuesReplica += ", '" + ipd.getVALUE().replaceAll("'", "''") + "'";
					}
				} else
					where += " AND VAL_TEXTO IS NULL";
			}*/
		} else
			throw new NotFoundException("Property no reconocida como DataProperty ni como ObjectProperty: " + prop);
		
		String sql = null;
		if (ipd.isAppliedSystemValue()) {
			String set = " SET VAL_TEXTO=NULL, VAL_NUM=NULL, Q_MIN=NULL, Q_MAX=NULL";
			if (ipd.getSystemValue()!=null) {
				set += ", SYS_VAL='" + ipd.getSystemValue().replaceAll("'", "''") + "'";
				columnsReplica += ",SYS_VAL";
				valuesReplica += "," + ipd.getSystemValue().replaceAll("'", "''");
			} else
				set += ", SYS_VAL=NULL";
			
			sql = "UPDATE " + table + set + where;
		} else
			sql = "DELETE FROM " + table + where;
		if (seHaceReplica) {
			
			boolean replication = false;
			Boolean replicationMap = hIdosReplication.get(ido);
			if (replicationMap!=null) {
				replication = replicationMap;
				System.out.println("ido en mapa " + replication);
			} else {
				replication = ir.getIdtosReplication(ido, null, idto, aipd);
				hIdosReplication.put(ido, replication);
			}
			String destinationSystem = null;
			if (replication) {
				destinationSystem = ipd.getDestinationSystem();
				if (destinationSystem==null)
					destinationSystem="*";
				if (ir.getIPCentral()==null && ir.getActualSystem().equals(destinationSystem))
				//soy central y el destino soy yo
					replication = false;
			}

			if (replication) {
				columnsReplica += ",DESTINATION,DATE";
				valuesReplica += ",'" + destinationSystem.replaceAll("'", "''") + "'," + System.currentTimeMillis();
				String tableName = "Replica_Data";
				if (memo)
					tableName += "_Memo";
				String sqlFilter = "INSERT INTO " + tableName + "(" + columnsReplica + ") VALUES (" + valuesReplica + ")";
				AuxiliarQuery.dbExecUpdate(factConnDB, sqlFilter, false);
			}
		}
		if (sql!=null) {
			//System.out.println("DEL:SQL" + sql);
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			log(ipd, ido, idto, "DEL_FACTINSTANCE", action.DEL);
		}
	}
	
	private void setDelete(HashSet<Integer> idosSup, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, 
			ArrayList<IdoObject> idosABorrar) {
		System.out.println("setDelete " + Auxiliar.hashSetIntegerToString(idosSup, ","));
		if (idosSup!=null) {
			Iterator it = idosSup.iterator();
			while (it.hasNext()) {
				Integer idoSup = (Integer)it.next();
				TransitionObject.IdoObjectDelete idoIdtoStructSup = aIdoIdtoDel.get(idoSup);
				if (idoIdtoStructSup!=null) {
					idoIdtoStructSup.setDelete(false);
					idosABorrar.remove(idoSup);
					HashSet<Integer> idosSupS = idoIdtoStructSup.getIdosSuperiores();
					setDelete(idosSupS, aIdoIdtoDel, idosABorrar);
				}
			}
		}
	}

	//un individuo es borrable cuando no es apuntado o cuando la property que le apunte tiene una inversa a la que
	//apunta el objeto
	private boolean canBeDelete(int valNum, int valueCls, HashSet<Integer> idosSup, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, boolean structural, ArrayList<IdoObject> idosABorrar) 
			throws SQLException, NamingException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		boolean canBeDel = true;
		HashMap<Integer,Integer> idoXPropTmp = new HashMap<Integer,Integer>();
		HashMap<Integer,Integer> idoXIdtoTmp = new HashMap<Integer,Integer>();
		HashMap<Integer,HashMap<Integer,HashSet<Integer>>> propXAllIdtoIdoTmp = new HashMap<Integer,HashMap<Integer,HashSet<Integer>>>();
		HashMap<String,HashMap<Integer,HashSet<Integer>>> propIdoXIdtoIdoParentTmp = new HashMap<String,HashMap<Integer,HashSet<Integer>>>();

		ArrayList<Integer> aProperty = new ArrayList<Integer>();
		ArrayList<Integer> aPropertyDudosas = new ArrayList<Integer>();
		//boolean exit = 
			getPropPointing(idosSup, valNum, valueCls, idoXPropTmp, idoXIdtoTmp, propXAllIdtoIdoTmp, 
					propIdoXIdtoIdoParentTmp, aIdoIdtoDel, aProperty, aPropertyDudosas, structural);
		//if (!exit) {
			System.out.println("array de property a comprobar si tienen inversas " + aProperty);
			
			if (aProperty.size()>0) {
					//get inversas
					//si en el nodo principal alguna prop no tiene inversa no es borrable
					//si un estructural apunta a un estructural no es borrable tenga inversa o no
				Iterator it = aProperty.iterator();
				String individuosQueApuntan = "";
				String individuosQueApuntanLogServer = "";
				boolean continuar = true;
				int maxErrors = 10;
				int contErrors = 0;
				while (it.hasNext() && continuar) {
					Integer prop = (Integer)it.next();
					boolean checkInverse = true;
					if (structural) {
						Category cat = ik.getCategory(prop);
						if (cat.isStructural()) {
							checkInverse = false;
							if (canBeDel) {
								canBeDel = false;
								aIdoIdtoDel.get(valNum).setDelete(false);
							}
						}
					}
					if (checkInverse) {
						Integer propInverse = ik.getPropertyInverse(prop);
						System.out.println("inversa " + propInverse);
						if (propInverse==null) {
							if (canBeDel) {
								canBeDel = false;
								IdoObjectDelete idoIdtoStructSup = aIdoIdtoDel.get(valNum);
								idoIdtoStructSup.setDelete(false);
								if (structural) {
									//ir hacia arriba poniendo delete a false
									HashSet<Integer> idosSupS = idoIdtoStructSup.getIdosSuperiores();
									setDelete(idosSupS, aIdoIdtoDel, idosABorrar);
								}
							}
							HashMap<Integer,HashSet<Integer>> aIdtoIdos = propXAllIdtoIdoTmp.get(prop);
							//buscar rdn y clase en oreginstancias de cada ido
							Statement st = null;
							ResultSet rs = null;
							//antes buscaba en O_Reg_Instancias
							String sql = "Select id_To, val_texto, id_o FROM O_Datos_Atrib " +
									"WHERE property=" + Constants.IdPROP_RDN;
							sql += Auxiliar.createSqlIdtoIdos(aIdtoIdos, null, true);
							
							ConnectionDB con = factConnDB.createConnection(true);
							try {
								st = con.getBusinessConn().createStatement();
								rs = st.executeQuery(sql);
								System.out.println(sql);
								while (rs.next() && continuar) {
									Integer idto = rs.getInt(1);
									String rdn = rs.getString(2);
									//if (individuosQueApuntan.length()>0)
										//individuosQueApuntan += "; ";
									individuosQueApuntan += ik.getClassName(idto) + " " + rdn;
									
									Integer ido = rs.getInt(3);
									individuosQueApuntanLogServer += "clase " + ik.getClassName(idto) +", ido " + ido + ", rdn " + rdn;
									String individuosParent = "";
									String key = ido + "#" + prop;
									System.out.println("key " + key);
									HashMap<Integer,HashSet<Integer>> aIdosParent = propIdoXIdtoIdoParentTmp.get(key);
									if (aIdosParent!=null && aIdosParent.size()>0) {
										Statement st2 = null;
										ResultSet rs2 = null;
										//antes buscaba en O_Reg_Instancias
										String sql2 = "Select id_To, val_texto, id_o FROM O_Datos_Atrib " + 
												"WHERE PROPERTY=" + Constants.IdPROP_RDN;
										sql2 += Auxiliar.createSqlIdtoIdos(aIdosParent, null, true);
										System.out.println(sql2);
										
										ConnectionDB con2 = factConnDB.createConnection(true);
										try {
											st2 = con2.getBusinessConn().createStatement();
											rs2 = st2.executeQuery(sql2);
											while (rs2.next()) {
												Integer idtoP = rs2.getInt(1);
												String rdnP = rs2.getString(2);
												if (individuosParent.length()>0)
													individuosParent += ", ";
												individuosParent += ik.getClassName(idtoP) + " " + rdnP;
											}
										} finally {
											if (st2 != null)
												st2.close();
											if (con2!=null)
												factConnDB.close(con2);
										}
									}
									if (individuosParent.length()>0) {
										individuosQueApuntan += " que a su vez es usado por " + individuosParent + "\n";
										individuosQueApuntanLogServer += " que a su vez es usado por " + individuosParent + " con property " + prop + "\n";
									} else {
										individuosQueApuntan += "\n";
										individuosQueApuntanLogServer += "\n";
									}
									contErrors++;
									if (contErrors==maxErrors) {
										individuosQueApuntan += "  .  .  .  ";
										individuosQueApuntanLogServer += "  .  .  .  ";
										continuar = false;
									}
								}
							} finally {
								if (st != null)
									st.close();
								if (con!=null)
									factConnDB.close(con);
							}
						}
					}
				}
				if (individuosQueApuntan.length()>0) {
					String mensajeLogServer = "No se pudo borrar porque es usado por \n" + individuosQueApuntanLogServer;
					System.err.println(mensajeLogServer);
					String mensaje = "No se pudo borrar porque es usado por \n" + individuosQueApuntan;
					throw new DataErrorException(DataErrorException.ERROR_DATA, mensaje);
				}
			}
			System.out.println("array de property dudosas a comprobar si tienen inversas " + aPropertyDudosas);
			if (canBeDel && aPropertyDudosas.size()>0) {
				//get inversas
				//si alguna prop no tiene inversa no es borrable, de momento
				Iterator it = aPropertyDudosas.iterator();
				while (it.hasNext()) {
					Integer prop = (Integer)it.next();
					boolean checkInverse = true;
					if (structural) {
						Category cat = ik.getCategory(prop);
						if (cat.isStructural()) {
							checkInverse = false;
							canBeDel = false;
							break;
						}
					}
					if (checkInverse) {
						Integer propInverse = ik.getPropertyInverse(prop);
						System.out.println("inversa " + propInverse);
						if (propInverse==null) {
							canBeDel = false;
							break;
						}
					}
				}
				//canBeDel = false;
			}
			if (canBeDel && debugMode) {
				Iterator it2 = idoXPropTmp.keySet().iterator();
				while (it2.hasNext()) {
					int ido = (Integer)it2.next();
					int idProp = idoXPropTmp.get(ido);
					int idto = idoXIdtoTmp.get(ido);
					putCardinality(ido, idto, idProp, false, action.DEL);
				}
			}
		//} else
		//	canBeDel = false;
		return canBeDel;
	}
	private void getPropPointing(HashSet<Integer> idosSup, int valNum, int valueCls, 
			HashMap<Integer,Integer> idoXProp, HashMap<Integer,Integer> idoXIdto, 
			HashMap<Integer,HashMap<Integer,HashSet<Integer>>> propXAllIdtoIdoTmp, HashMap<String,HashMap<Integer,HashSet<Integer>>> propIdoXIdtoIdoParentTmp, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, 
			ArrayList<Integer> aProperty, ArrayList<Integer> aPropertyDudosas, boolean structural) throws SQLException, NamingException, NotFoundException {
		
		HashMap<String,HashSet<Integer>> hParentTmp = new HashMap<String,HashSet<Integer>>(); //mapa temporal por idto y property para no hacer una query cada vez
		
		HashSet<Integer> hIdtos = AuxiliarModel.getIdtosThatPointTo(factConnDB, valueCls);
		
		if (hIdtos.size()>0) {
			//boolean exit = false;
			String sqlSelect = "select distinct PROPERTY, ID_O, ID_TO from O_Datos_Atrib " + /*" WITH(NOLOCK)*/ 
				"where VAL_NUM=" + valNum + " and ID_O<>" + valNum +
				" and id_to in(" + Auxiliar.hashSetIntegerToString(hIdtos, ",") + ")";
			if (idosSup!=null)
				sqlSelect += " and ID_O not in(" + Auxiliar.hashSetIntegerToString(idosSup, ",") + ")";
			System.out.println("getPropPointing " + sqlSelect);
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = factConnDB.createConnection(true); 
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sqlSelect);
				while (rs.next()) {
					Integer idProp = rs.getInt(1);
					if (!rs.wasNull()) {
						Integer ido = rs.getInt(2);
						Integer idto = rs.getInt(3);
						//si es apuntado, mirar si el que apunta esta en del_object
						//si no esta, añadirlo para hacer la comprobación de la inversa
						if (!aPropertyDudosas.contains(idProp)) {
							TransitionObject.IdoObjectDelete idoIdto = aIdoIdtoDel.get(ido);
							if (idoIdto==null || idoIdto!=null && idoIdto.isDelete()!=null && idoIdto.isDelete().equals(false)) {
								if (!aProperty.contains(idProp)) {
									aProperty.add(idProp);
								}
								HashMap<Integer,HashSet<Integer>> idtoIdos = propXAllIdtoIdoTmp.get(idProp);
								if (idtoIdos==null) {
									idtoIdos = new HashMap<Integer,HashSet<Integer>>();
									propXAllIdtoIdoTmp.put(idProp, idtoIdos);
								}
								HashSet<Integer> aIdo = idtoIdos.get(idto);
								if (aIdo==null) {
									aIdo = new HashSet<Integer>();
									idtoIdos.put(idto, aIdo);
								}
								if (!aIdo.contains(ido)) {
									aIdo.add(ido);
									//buscar los padres si tiene
									String key = idto + "#" + idProp;
									//agrupar query de busqueda de padres por idto,prop
									HashSet<Integer> aIdosChild = hParentTmp.get(key);
									if (aIdosChild==null) {
										aIdosChild = new HashSet<Integer>();
										hParentTmp.put(key, aIdosChild);
									}
									aIdosChild.add(ido);
								}
							} else if (!aProperty.contains(idProp) && (idoIdto.isDelete()==null)) {
																	//este nodo aun no se sabe si se puede borrar o no
									aPropertyDudosas.add(idProp);   //este nodo se añade por si tiene inversa que tambien se
																	//quiere borrar, para que no entre en un bucle
									//exit = true;
									//break;
							}
						}
						if (debugMode) {
							idoXProp.put(ido, idProp);
							idoXIdto.put(ido, idto);
						}
					}
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
			
			Iterator it = hParentTmp.keySet().iterator();
			while (it.hasNext()) {
				String idtoProp = (String)it.next();
				HashSet<Integer> aIdosChild = hParentTmp.get(idtoProp);
				
				String[] idtoPropSpl = idtoProp.split("#");
				int idto = Integer.parseInt(idtoPropSpl[0]);
				int idProp = Integer.parseInt(idtoPropSpl[1]);
				
				getParent(aIdosChild, idto, idProp, propIdoXIdtoIdoParentTmp);
				
				System.out.println("aIdosParent");
				Iterator it2 = propIdoXIdtoIdoParentTmp.keySet().iterator();
				while (it2.hasNext()) {
					String str = (String)it2.next();
					String[] strSpl = str.split("#");
					int ido = Integer.parseInt(strSpl[0]);
					
					HashMap<Integer,HashSet<Integer>> aIdosParent = propIdoXIdtoIdoParentTmp.get(str);
					Iterator itP = aIdosParent.keySet().iterator();
					while (itP.hasNext()) {
						Integer idtoSup = (Integer)itP.next();
						System.out.println("padres de ido " + ido + ", idto " + idto + " y property " + idProp + ": " 
								+ "idtoSup " + idtoSup + ", idos " + Auxiliar.hashSetIntegerToString(aIdosParent.get(idtoSup), ","));
					}
				}
				
			}
		}
	}
	
	private void getParent(HashSet<Integer> aIdos, int idto, int idProp, 
			HashMap<String,HashMap<Integer,HashSet<Integer>>> propIdoXIdtoIdoParentTmp) throws SQLException, NamingException, NotFoundException {
		HashSet<Integer> hIdtos = AuxiliarModel.getIdtosThatPointTo(factConnDB, idto);
		
		//HashMap<Integer,HashSet<Integer>> aIdtoIdosParent = new HashMap<Integer,HashSet<Integer>>();
		if (hIdtos.size()>0) {
			String sqlSelectParent = "select distinct PROPERTY, ID_O, ID_TO, VAL_NUM from O_Datos_Atrib " + /*" WITH(NOLOCK)*/ 
				"where VAL_NUM IN(" + Auxiliar.hashSetIntegerToString(aIdos, ",") + ")" +
				" and PROPERTY<>" + idProp + 
				" and ID_TO IN(" + Auxiliar.hashSetIntegerToString(hIdtos, ",") + ")";
			System.out.println("sqlSelectParent " + sqlSelectParent);
			Statement st = null;
			ResultSet rs = null;
			ConnectionDB con = factConnDB.createConnection(true); 
			try {
				st = con.getBusinessConn().createStatement();
				rs = st.executeQuery(sqlSelectParent);
				while (rs.next()) {
					Integer idPropSup = rs.getInt(1); //no añadir si tiene inversa y es estructural
					if (!rs.wasNull()) {
						boolean add = true;
						
						Integer propInverse = ik.getPropertyInverse(idPropSup);
						if (propInverse!=null) {
							Category cat = ik.getCategory(propInverse);
							if (cat.isStructural()) {
								add = false;
							}
						}
						if (add) {
							//if (!idPropSup.equals(idProp)) {
								int idoSup = rs.getInt(2);
								int idtoSup = rs.getInt(3);
								int valNum = rs.getInt(4);
								
								String key = String.valueOf(valNum) + "#" + String.valueOf(idProp);
								
								HashMap<Integer,HashSet<Integer>> aIdtoIdosParent = propIdoXIdtoIdoParentTmp.get(key);
								if (aIdtoIdosParent==null) {
									aIdtoIdosParent = new HashMap<Integer, HashSet<Integer>>();
									propIdoXIdtoIdoParentTmp.put(key, aIdtoIdosParent);
								}
								HashSet<Integer> hIdos = aIdtoIdosParent.get(idtoSup);
								if (hIdos==null) {
									hIdos = new HashSet<Integer>();
									aIdtoIdosParent.put(idtoSup,hIdos);
								}
								hIdos.add(idoSup);
							//}
						}
					}
				}
			} finally {
				if (rs != null)
					rs.close();
				if (st != null)
					st.close();
				if (con!=null)
					factConnDB.close(con);
			}
		}
		//return aIdtoIdosParent;
	}
	
	/*private Integer getParentStruct(int ido) throws SQLException, NamingException, NotFoundException {
		String sqlStructSelect = "select distinct PROPERTY, ID_O from O_Datos_Atrib " + 
			"where VAL_NUM =" + ido;
		System.out.println("sqlStructSelect " + sqlStructSelect);
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlStructSelect);
			while (rs.next()) {
				Integer idProp = rs.getInt(1);
				if (!rs.wasNull()) {
					if (ik.getCategory(idProp).isStructural()) {
						int idoSup = rs.getInt(2);
						ido = getParentStruct(idoSup);
						break;
					}
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			con.close();
		}
		return ido;
	}*/

	private HashSet<Integer> subDellAllFactsInstance(ArrayList<IPropertyDef> aipd, ArrayList<IdoObject> idosABorrar, 
			HashMap<Integer,String> idoDelDestination) throws NotFoundException, NamingException, SQLException, DataErrorException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, InterruptedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstance");
		HashSet<Integer> idtosIndexABorrar = new HashSet<Integer>();
		HashMap<Integer,HashSet<Integer>> hIdtoIdos = new HashMap<Integer, HashSet<Integer>>();
		String idos = getIdosIdoObject(idosABorrar, hIdtoIdos, idtosIndexABorrar, "DEL_OBJ", action.DEL_OBJECT);
		processDeleteIndexDecrement(aipd, hIdtoIdos);
		deleteRowObject(idos);
		
		deleteUsers(idos);
		deleteRols(idos);
		
		if (seHaceReplica)
			subDelReplica(aipd, idosABorrar, idoDelDestination);
		//subDelLock(idosABorrar);

		subDelAllFactsInstanceTop(aipd, hIdtoIdos); 
		subDelAllFactsInstanceDown(aipd, idosABorrar, hIdtoIdos);
		return idtosIndexABorrar;
		//System.out.println("Fin de la funcion subDelAllFactsInstance");
	}

	/*private void subDelLock(ArrayList<IdoObject> idos) throws SQLException, NamingException, InterruptedException {
		Iterator it = idos.iterator();
		while (it.hasNext()) {
			IdoObject idoIdto = (IdoObject)it.next();
			Integer ido = idoIdto.getIdo();
			Integer idto = (Integer)idoIdto.getObject();
			
			if (ir.isAssociatedIdto(idto)) {
				GenerateSQL gSQL = new GenerateSQL(factConnDB.getGestorDB());
				String sql = "DELETE FROM o_reg_instancias_index_lock WHERE ID_O=" + ido + " AND " + gSQL.getCharacterBegin() + "LOCK" + gSQL.getCharacterEnd() + "='N'";
				int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql);
				while (rows==0) {
					//si estaba siendo usado
					//tengo que esperar
					long millis = 10*1000; //10 segundos
					Thread.currentThread().sleep(millis);
					rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql);
				}			
			}
		}
	}*/
	private void subDelReplica(ArrayList<IPropertyDef> aipd, ArrayList<IdoObject> idos, 
			HashMap<Integer,String> idoDelDestination) throws SQLException, NamingException {
		Iterator it = idos.iterator();
		while (it.hasNext()) {
			IdoObject idoIdto = (IdoObject)it.next();
			Integer ido = idoIdto.getIdo();
			Integer idto = (Integer)idoIdto.getObject();
			
			boolean replication = false;
			Boolean replicationMap = hIdosReplication.get(ido);
			if (replicationMap!=null) {
				replication = replicationMap;
				System.out.println("ido en mapa " + replication);
			} else {
				replication = ir.getIdtosReplication(ido, null, idto, aipd);
			}
			String destinationSystem = null;
			if (replication) {
				destinationSystem = idoDelDestination.get(ido);
				if (destinationSystem==null)
					destinationSystem="*";
				if (ir.getIPCentral()==null && ir.getActualSystem().equals(destinationSystem))
				//soy central y el destino soy yo
					replication = false;
			}
			if (replication) {
				String columnsReplica = "ID, ACTION, ID_O, ID_TO, DESTINATION, DATE";
				String valuesReplica = id + ",'" + ReplicationEngine.DEL_IDO + "'," + ido + "," + idto + ",'" + destinationSystem.replace("'", "''") + "'," + System.currentTimeMillis();
				String tableName = "Replica_Data";
				String sqlFilter = "INSERT INTO " + tableName + "(" + columnsReplica + ") VALUES (" + valuesReplica + ")";
				AuxiliarQuery.dbExecUpdate(factConnDB, sqlFilter, false);
			}
		}
	}
	
	private void subDelAllFactsInstanceProcess(int ido, int idto, 
			HashMap<Integer, TransitionObject.IdoObjectDelete> aIdoIdtoDel, boolean structural, 
			ArrayList<IdoObject> idosABorrar) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceProcess");
		//si ya no existe ese ido en bd borrar de O_Reg_Instancias
		System.out.println("BORRADO DE IDO " + ido + ", IDTO " + idto + ", STRUCTURAL " + structural);
		boolean delete = false;
		HashSet<Integer> idosSup = null;
		if (structural) {
			System.out.println("idoStructural " + ido);
			//obtener su nodo superior para no tener en cuenta este enlace
			IdoObjectDelete iod = aIdoIdtoDel.get(ido);
			System.out.println("iod " + iod);
			idosSup = aIdoIdtoDel.get(ido).getIdosSuperiores();
		}
		Boolean actualDelete = aIdoIdtoDel.get(ido).isDelete();
		if (actualDelete==null)
			delete = canBeDelete(ido, idto, idosSup, aIdoIdtoDel, structural, idosABorrar);
		else
			delete = actualDelete;
		//porque en nodos estructurales apuntados por una property normal (no estr)
		//no se pueden borrar sus nodos superiores
		System.out.println("DELETE " + delete);
		if (delete) {
			aIdoIdtoDel.get(ido).setDelete(true);
			IdoObject io = new IdoObject(ido, idto);
			idosABorrar.add(io);
		}
		/*else {
			throw new DataErrorException(DataErrorException.ERROR_DELETE_RELATION, "No es posible borrar el individuo con ido " + ido);
		}*/
		//System.out.println("Fin de la funcion subDelAllFactsInstanceProcess");
	}

	private void subDelAllFactsInstanceTop(ArrayList<IPropertyDef> aipd, HashMap<Integer,HashSet<Integer>> hIdtoIdos) 
			throws SQLException, NamingException, NotFoundException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceTop");
		//aqui no se llama a putCardinality xq ya se hizo en canBeDelete
		
		Iterator it = hIdtoIdos.keySet().iterator();
		while (it.hasNext()) {
			Integer valueCls = (Integer)it.next();
			HashSet<Integer> idtos = AuxiliarModel.getIdtosThatPointTo(factConnDB, valueCls);
			//un valueCls, varios valNum, varios idtos
			if (idtos.size()>0) {
				String sql = "DELETE FROM O_Datos_Atrib WHERE ID_TO IN(" + Auxiliar.hashSetIntegerToString(idtos,",") + ") " +
						"and VAL_NUM IN(" + Auxiliar.hashSetIntegerToString(hIdtoIdos.get(valueCls),",") + ") and VALUE_CLS=" + valueCls;
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			}
		}
		//log(null, valNum, valCls, "DEL_VALNUM", action.DEL);
		//System.out.println("Fin de la funcion subDelAllFactsInstanceTop");
	}
	private void subDelAllFactsInstanceDown(ArrayList<IPropertyDef> aipd, ArrayList<IdoObject> idos, 
			HashMap<Integer,HashSet<Integer>> hIdtoIdos) throws SQLException, NamingException, NotFoundException, DataErrorException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceDown");
		//String sIdos = getIdosIdoObject(idos, "DEL_IDO", action.DEL);

		String sql = "DELETE FROM O_Datos_Atrib WHERE ";
		sql += Auxiliar.createSqlIdtoIdos(hIdtoIdos, null, false);
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "DELETE FROM O_Datos_Atrib_Memo WHERE ";
		sql += Auxiliar.createSqlIdtoIdos(hIdtoIdos, null, false);
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		//log(null, ido, idto, "DEL_IDO", action.DEL);
		//System.out.println("Fin de la funcion subDelAllFactsInstanceDown");
	}
	private void newRol(int idoRol, String nameRol) {
		Roles rol = new Roles(idoRol, nameRol, null);
		aNewRoles.add(rol);
	}
	private void newUser(int idoUsuario, String rdn, String propStr, String value) {
		Usuarios usu = aNewUsuarios.get(idoUsuario);
		if (usu==null) {
			usu = new Usuarios(idoUsuario, rdn, null, null, null, null, null, null, null);
			aNewUsuarios.put(idoUsuario, usu);
		}
		//update de la property que sea
		if (propStr.equals(Constants.PROP_USERROL)) {
			UsuarioRoles ur = new UsuarioRoles(idoUsuario, Integer.parseInt(value));
			aNewUsuarioRoles.add(ur);
		} else if (propStr.equals(Constants.PROP_PASSWORD)) {
			usu.setPwd(value);
		} else if (propStr.equals(Constants.PROP_MAIL)) {
			usu.setMail(value);
		}
	}
	private void processNewUser() throws SQLException, NamingException {
		Iterator it = aNewUsuarios.keySet().iterator();
		while (it.hasNext()) {
			Integer idoUsuario = (Integer)it.next();
			Usuarios usu = aNewUsuarios.get(idoUsuario);
			String sql = "insert into usuarios(ido_usuario, login, pwd";
			if (usu.getMail()!=null)
				sql += ",mail";
			GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
			String function = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, usu.getPwd());
			sql += ") values(" + usu.getIdoUsuario() + ",'" + usu.getLogin() +"'," + function;
			if (usu.getMail()!=null)
				sql += ",'" + usu.getMail() + "'";
			sql += ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
		it = aNewRoles.iterator();
		while (it.hasNext()) {
			Roles rol = (Roles)it.next();
			String sql = "insert into roles(ido_rol, name_rol) " +
					"values(" + rol.getIdoRol() + ",'" + rol.getNameRol() +"')";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
		it = aNewUsuarioRoles.iterator();
		while (it.hasNext()) {
			UsuarioRoles ur = (UsuarioRoles)it.next();
			String sql = "insert into usuarioRoles(rol, usuario) " +
					"values(" + ur.getIdoRol() + "," + ur.getIdoUsuario() +")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		}
	}
	private void updateRol(int idoRol, String value, String oldValue) throws SQLException, NamingException {
		String sql = "update roles set name_rol='" + value + "' where ido_rol=" + idoRol;
		if (oldValue!=null)
			sql += " and name_rol='" + oldValue + "'";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void updateUser(int idoUsuario, String propStr, String value, String oldValue) throws SQLException, NamingException {
		String sql = null;
		if (propStr.equals(Constants.PROP_USERROL)) {
			sql = "update usuarioRoles set rol=" + value + " where usuario=" + idoUsuario;
			if (oldValue!=null)
				sql += " and rol=" + oldValue;
			int rows = AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			if (rows==0) {
				sql = "insert into usuarioRoles(rol, usuario) values(" + value + "," + idoUsuario +")";
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			}
		} else {
			sql = "update usuarios set ";
			if (propStr.equals(Constants.PROP_RDN)) {
				sql += "login='" + value + "' where ido_usuario=" + idoUsuario;
				if (oldValue!=null)
					sql += " and login='" + oldValue +"'";
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			} else if (propStr.equals(Constants.PROP_PASSWORD)) {
				GenerateSQL generateSQL = new GenerateSQL(factConnDB.getGestorDB());
				String function = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, value);
				sql += "pwd=" + function + " where ido_usuario=" + idoUsuario;
				if (oldValue!=null) {
					String oldFunction = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, oldValue);
					sql += " and pwd=" + oldFunction;
				}
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			} else if (propStr.equals(Constants.PROP_MAIL)) {
				sql += "mail='" + value + "' where ido_usuario=" + idoUsuario;
				if (oldValue!=null)
					sql += " and mail='" + oldValue + "'";
				AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			}
		}
	}

	private void deleteUser(int ido, String propStr, String value) throws SQLException, NamingException {
		String sql = null;
		System.out.println(propStr);
		if (propStr.equals(Constants.PROP_USERROL)) {
			sql = "delete from usuarioroles where usuario=" + ido;
		} else if (propStr.equals(Constants.PROP_RDN)) {
			sql = "delete from usuarioroles where usuario=" + ido;
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			sql = "delete from usuarios where ido_usuario=" + ido;
		} else if (propStr.equals(Constants.PROP_PASSWORD)) {
			sql = "update usuarios set pwd=NULL where ido_usuario=" + ido;
			String function = generateSQL.getEncryptFunction(InstanceService.keyEncrypt, value);
			sql += " and pwd=" + function;
		} else if (propStr.equals(Constants.PROP_MAIL)) {
			sql = "update usuarios set mail=NULL where ido_usuario=" + ido;
			sql += " and mail='" + value + "'";
		}
		if (sql!=null)
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void deleteRol(int ido) throws SQLException, NamingException {
		String sql = "delete from roles where ido_rol=" + ido;
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "delete from usuarioRoles where rol=" + ido;
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void deleteUsers(String idos) throws SQLException, NamingException {
		String sql = "delete from usuarios where ido_usuario in(" + idos + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "delete from usuarioroles where usuario in(" + idos + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}
	private void deleteRols(String idos) throws SQLException, NamingException {
		String sql = "delete from roles where ido_rol in(" + idos + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
		sql = "delete from usuarioroles where rol in(" + idos + ")";
		AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
	}

	public void insertFacts(IndividualData dIndiv, HashMap<Integer,HashSet<Integer>> allIdtoIdos) throws NotFoundException, SQLException, NamingException {

		Iterator itDel = allIdtoIdos.keySet().iterator();
		while (itDel.hasNext()) {
			Integer idto = (Integer)itDel.next();
			HashSet<Integer> hIdos = allIdtoIdos.get(idto);
			String sqlDelete = "delete from o_datos_atrib where id_to=" + idto + " and id_o in(" + Auxiliar.hashSetIntegerToString(hIdos, ",") + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sqlDelete, false);
			sqlDelete = "delete from o_datos_atrib_memo where id_to=" + idto + " and id_o in(" + Auxiliar.hashSetIntegerToString(hIdos, ",") + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sqlDelete, false);
			sqlDelete = "delete from O_Reg_Instancias_Index_Replica where id_to=" + idto + " and id_o in(" + Auxiliar.hashSetIntegerToString(hIdos, ",") + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sqlDelete, false);
		}
		

		ArrayList<IPropertyDef> aipd = dIndiv.getAIPropertyDef();
		Iterator it = aipd.iterator();
		while (it.hasNext()) {
			IPropertyDef ipd = (IPropertyDef)it.next();
			int prop = ipd.getPROP();
			int ido = ipd.getIDO();
			int idto = ipd.getIDTO();

			boolean memo = false;
			if (ipd.getVALUECLS().equals(Constants.IDTO_MEMO))
				memo = true;
			
			Integer valNum = null;
			String rdn = null;
			boolean dp = false, op = false;
			if (!memo) {
				if (ik.isDataProperty(prop) /*|| prop==Constants.IdPROP_BUSINESSCLASS*/) {
					dp = true;
					if (prop==Constants.IdPROP_RDN) {
						rdn = ipd.getVALUE();
						idoRealRdn.put(ido, rdn);
						//insertar en o_reg_instancias_index
						String sql = "insert into O_Reg_Instancias_Index_Replica(id_o, id_to, rdn) " +
								"values (" + ido + "," + idto + ",'" + rdn + "')";
						AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
						log(null, ido, idto, "NEW_OBJ", action.NEW);
					}
				} else if (ik.isObjectProperty(prop)) {
					op = true;
					valNum = Integer.parseInt(ipd.getVALUE());
					rdn = idoRealRdn.get(valNum);
					if (rdn==null) {
						String dBValue = TransitionObject.dBValueOfIdoProp(valNum, ipd.getVALUECLS(), Constants.IdPROP_RDN, factConnDB);
						rdn = dBValue;
						if (rdn==null) {
							IPropertyDef ipdRdn = findFactInstanceByIdoProp(valNum, Constants.IdPROP_RDN, aipd);
							rdn = ipdRdn.getVALUE();
						}
						idoRealRdn.put(valNum, rdn);
					}
				} else
					throw new NotFoundException("Property no reconocida como DataProperty ni como ObjectProperty: " + prop);
			}
			String columns = "", values="";
			columns = "ID_TO,ID_O,PROPERTY,";
			if (memo)
				columns += "MEMO";
			else {
				if (op)
					columns += "VAL_NUM,";
				columns += "VAL_TEXTO";
			}
			columns += ",VALUE_CLS";
			if (dp)
				columns += ",Q_MIN,Q_MAX";
			values = idto + "," + ido + "," + prop;
			if (op) {
				values += "," + valNum;
				values += ",'" + rdn.replaceAll("'", "''") + "'";
			} else if (memo || dp) {
				if (ipd.getVALUE()!=null)
					values += ",'" + ipd.getVALUE().replaceAll("'", "''") + "'";
				else
					values += ",NULL";
			}
			values += "," + ipd.getVALUECLS();
			if (dp)
				values += "," + ipd.getQMIN() + "," + ipd.getQMAX();
			
			String table = "";
			if (memo)
				table = "O_Datos_Atrib_Memo";
			else
				table = "O_Datos_Atrib";
			String sql = "INSERT INTO " + table + "(" + columns + ") VALUES (" + values + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			log(ipd, ido, idto, "NEW_FACTINSTANCE", action.NEW);
		}
	}

	/*public void subDelAllFactsInstanceObligated(ArrayList<Integer> idos) 
			throws SQLException, NamingException, NotFoundException, DataErrorException, SystemException, RemoteSystemException, 
			CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, 
			CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException, InterruptedException {
		//System.out.println("Inicio de la funcion subDelAllFactsInstanceObligated");
		if (idos!=null && idos.size()>0) {
			String sql = "";
			ArrayList<IdoObject> aIdoIdtos = getIdtos(idos);
			if (seHaceReplica) {
				id = generateIdReplica();
				Iterator it = aIdoIdtos.iterator();
				while (it.hasNext()) {
					IdoObject idoIdto = (IdoObject)it.next();
					Integer ido = idoIdto.getIdo();
					Integer idto = (Integer)idoIdto.getObject();

					boolean replication = false;
					Boolean replicationMap = hIdosReplication.get(ido);
					if (replicationMap!=null) {
						replication = replicationMap;
						System.out.println("ido en mapa " + replication);
					} else {
						replication = ir.getIdtosReplication(ido, null, idto, null);
					}
					if (replication) {
						String columnsReplica = "ID, ACTION, ID_O, ID_TO, DATE";
						String valuesReplica = id + ",'" + ReplicationEngine.DEL_IDO + "'," + ido + "," + idto + "," + System.currentTimeMillis();
						String tableName = "Replica_Data";
						String sqlFilter = "INSERT INTO " + tableName + "(" + columnsReplica + ") VALUES (" + valuesReplica + ")";
						AuxiliarQuery.dbExecUpdate(factConnDB, sqlFilter, false);
					}
				}
			}
			//subDelLock(aIdoIdtos);
			//si ya no existe ese ido en bd borrar de O_Reg_Instancias
			deleteRowObjects(idos);
			String sIdos = Auxiliar.arrayIntegerToString(idos, ",");
			sql = "DELETE FROM O_Datos_Atrib WHERE VAL_NUM IN(" + sIdos + ") OR " + "ID_O IN(" + sIdos + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			sql = "DELETE FROM O_Datos_Atrib_Memo WHERE ID_O IN(" + sIdos + ")";
			AuxiliarQuery.dbExecUpdate(factConnDB, sql, false);
			//en el log ya queda registrado xq en deleteRowObjects ya se ha insertado xa ese ido, y como no itera x structurales 
			//no se encuentran idos nuevos q almacenar en el log
		}
		//System.out.println("Fin de la funcion subDelAllFactsInstanceObligated");
	}
	
	private ArrayList<IdoObject> getIdtos(ArrayList<Integer> idos) throws SQLException, NamingException {
		//select añadido para obtener los idto
		ArrayList<IdoObject> aIdos = new ArrayList<IdoObject>();
		//antes buscaba en O_Reg_Instancias
		String sqlIdto = "SELECT ID_TO, ID_O FROM O_Datos_Atrib WHERE ID_O IN(" + Auxiliar.arrayIntegerToString(idos, ",") + ") AND PROPERTY=" + Constants.IdPROP_RDN;
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlIdto);
			while (rs.next()) {
				Integer idto = rs.getInt(1);
				if (!rs.wasNull()) {
					Integer ido = rs.getInt(2);
					IdoObject idoIdto = new IdoObject(ido, idto);
					aIdos.add(idoIdto);
				}
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return aIdos;
	}*/

	/*private ArrayList<IdoObject> getIdtos(String valNum) throws SQLException, NamingException {
		//select añadido para obtener los idto
		ArrayList<IdoObject> aIdtos = new ArrayList<IdoObject>();
		String sqlIdto = "SELECT distinct ID_O, ID_TO FROM O_Datos_Atrib WHERE VAL_NUM in(" + valNum + ")";
		Statement st = null;
		ResultSet rs = null;
		ConnectionDB con = factConnDB.createConnection(true); 
		try {
			st = con.getBusinessConn().createStatement();
			rs = st.executeQuery(sqlIdto);
			while (rs.next()) {
				Integer ido = rs.getInt(1);
				Integer idto = rs.getInt(2);
				IdoObject idoIdto = new IdoObject(ido, idto);
				if (!rs.wasNull()// && !idoIdto.containedByIdoObject(aIdtos)
	)
					aIdtos.add(idoIdto);
			}
		} finally {
			if (rs != null)
				rs.close();
			if (st != null)
				st.close();
			if (con!=null)
				factConnDB.close(con);
		}
		return aIdtos;
	}*/
}
