
/***
 * DocDataModel.java
 * 
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description Implementaci�n de la interfaz IKnowledgeBaseInfo 		
 */
package dynagent.ruleengine.meta.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.Mask;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.communication.docServer;
import dynagent.common.communication.queryData;
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
import dynagent.common.knowledge.Clase;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IEmailListener;
import dynagent.common.knowledge.IExecuteActionListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.process.IAsigned;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.Domain;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AliasComponents;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.IAlias;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.QueryConstants;
import dynagent.common.xml.QueryXML;
import dynagent.ruleengine.ConceptLogger;
import dynagent.ruleengine.CreateIdo;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.masks.IMasks;
import dynagent.ruleengine.masks.MaskComponents;
import dynagent.ruleengine.src.ruler.AccessEngine;
import dynagent.ruleengine.src.ruler.ContributionValue;
import dynagent.ruleengine.src.ruler.DatValue;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.ruler.FactAccess;
import dynagent.ruleengine.src.ruler.FactProp;
import dynagent.ruleengine.src.ruler.IRuleEngine;
import dynagent.ruleengine.src.ruler.Individual;
import dynagent.ruleengine.src.ruler.JBossEngine;
import dynagent.ruleengine.src.ruler.Lock;
import dynagent.ruleengine.src.ruler.ObjValue;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.FactInfo;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QueryValue;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.StringChanged;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;


public class DocDataModel implements IKnowledgeBaseInfo, IAsigned {

//	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
//	
//	private FactHandle factHandle = null;
//	
//	private boolean ACTIVE;


	private MetaData metaData = null;

	private docServer server = null;
	
	private docServer localServer = null;

	private IAsigned asigned = null;

	private KnowledgeAdapter ka = new KnowledgeAdapter(this);
	
	private DataModelAdapter dataModelAdapter = new DataModelAdapter(this);

	private DDBBSession session;
	
	private DefaultSession sessionTemporal;
	
	private String user = null;

	private HashSet<String> userRoles;

	private ArrayList<OrderProperty> orderProperties;
	
	private ArrayList<CardMed> listCM;
	
	private ArrayList<ColumnProperty> columnProperties;
	
	private ArrayList<Groups> groupsProperties;
	
	private ArrayList<EssentialProperty> essentialProperties;
	
	private ArrayList<ListenerUtask> listenerUtasks;

	private int nbusiness = 0;
 
	private IRuleEngine ruleEngine;
	
	private HashMap<Integer,Integer> hmLEVELxID=new HashMap<Integer,Integer>(); 

	private HashMap<Integer,Integer> hmIDTOxIDO=new HashMap<Integer,Integer>();
	
	
	private IQuestionListener questionListener;
	
	private IEmailListener emailListener;
	
	
	/*
	 * 
	 * mapa con los idtos de los idos obtenidos a partir del value-valuecls. Este no impolica que este en motor cargado el individuo, es suficiente con que sea apuntado
	 */
//private HashMap<String,Integer> hmVALUECLSxVALUE=new HashMap<String,Integer>();

	private HashMap<Integer,String> hmRdnxIDO=new HashMap<Integer,String>();

	private HashMap<Integer,HashSet<Integer>> hmIDOSxIDTO=new HashMap<Integer,HashSet<Integer>>();

	//private HashMap<Integer,String> hmNamePropXIdProp=new HashMap<Integer,String>();
	
	private HashMap<Integer,String> hmNameClassXIdto=new HashMap<Integer,String>();
	
	private DebugLog debugLog;
	
	public DebugLog getDebugLog() {
		return debugLog;
	}


	private IAlias alias;
	
	private IMasks masks;
	
	private HashMap<Integer,Integer> hmGenericFilterXIdto=new HashMap<Integer,Integer>();
	
	private boolean enabledRules=true;
	
	private boolean enabled=false;
	
	//Almacena los fact que tienen que quitarse de motor tras hacer el commit. Esto se utiliza cuando createNewMotor=true en el commit para evitar crear esos facts en el nuevo motor
	public HashSet<Object> listFactRetractable=new HashSet<Object>();
	
	public HashSet<Integer> abstractClasses=new HashSet<Integer>();
	
	private AccessEngine accessEngine;
	
	public void testRule(DocDataModel ddm,Double porcentajeObjetivo,HashMap<String,Vector<Double>> VporcentajesCantidadesXlinea2){
	}
	
	private Integer getMaxIdto(){
		Integer idto=null;
		ArrayList<Integer> idtos=new ArrayList(this.getHmIDOSxIDTO().keySet());
		java.util.Collections.sort(idtos);
		
		return idtos.get(idtos.size()-1);
	}
	
	
	private HashMap<String,String> cache=new HashMap<String,String>();

	public boolean depthObjectProperty;

	private HashSet<Integer> indexes;

	private HashSet<IBatchListener> batchListenerList=new HashSet<IBatchListener>();

	private boolean printRules=true;

	private ArrayList<Integer> globalUtasks;

	private HashMap<String,String> groupsByClassAndProperty=new HashMap<String, String>();
	
	private long creationDate;

	private HashSet<INoticeListener> noticeListenerList=new HashSet<INoticeListener>();
	
	public boolean classIndexed(Integer idto){
		return indexes.contains(idto);
	}

	public DocDataModel(DebugLog debugLog,boolean checkCoherenceObjects, boolean runRules, boolean lockObjects, boolean retractFilters, boolean printRules) {
		//System.out	.println("DDM CREADO---------------------------------------------DDM CREADO1");
		this.session = new DDBBSession(this,checkCoherenceObjects, runRules, lockObjects, retractFilters, true);
		//SessionController.getInstance().remove(this.session.getID(),this);
		//SessionController.getInstance().getSessionsList(this).add(this);
		this.sessionTemporal = new DefaultSession(this,null,null,checkCoherenceObjects, runRules, lockObjects, retractFilters,false);
		depthObjectProperty=true;
		this.debugLog=debugLog;
		this.printRules=printRules;
		//this.ACTIVE=true;
		creationDate=System.currentTimeMillis();
	}
	
	
	

	public DocDataModel(String user, int nbusiness) {
		this.user = user;
		this.nbusiness = nbusiness;
		//TODO hay que cambiar los parametros del defaultSession;
		this.session = null;//new DefaultSession(this,null,null,categorySession, Session.SET_MODE);		
		//SessionController.getInstance().getSessionsList(this).add((Session) this);
		this.sessionTemporal = null;//new DefaultSession(this,null,null,categorySession, Session.SET_MODE);
		depthObjectProperty=true;
		//this.ACTIVE=true;
		creationDate=System.currentTimeMillis();
	}

	private DocDataModel() {
		depthObjectProperty=true;
		//this.ACTIVE=true;
		creationDate=System.currentTimeMillis();
	}

	public docServer getServer() {
		return server;
	}

	public void setServer(docServer server) {
		this.server = server;
	}

	public void setLocalServer(docServer lserver) {
		this.localServer = lserver;
	}
	
	public docServer getLocalServer() {
		if(localServer==null) return server;//parece ser nulo en motor en servidor para acciones shceduler
		return localServer;
	}
	
	public IAsigned getAsigned() {
		return asigned;
	}

	public void setAsigned(IAsigned asigned) {
		this.asigned = asigned;
	}

	/**
	 * Devuelve un iterador con todos los identificadores de las propiedades que
	 * tiene el objeto con identificador num�rico id
	 * 
	 * @param id
	 * @return
	 * @throws NotFoundException 
	 */
	
	public void setDestinationRecursively(HashSet<Integer> idosProcesados, Integer ido,String destination,HashSet<String> exclusions,String destinoDescarte) throws NotFoundException{
		IRuleEngine jboss=getRuleEngine();
		idosProcesados.add(ido);
		Individual ind=jboss.getIndividualFact(ido);
		if(ind!=null){
			ind.setDestinationSystem(destination);
			printRule("Destino individual "+ido+" "+destination);
		}
		Iterator<IPropertyDef> it = jboss.getAllInstanceFactsNoDeletedWithIdo(ido).iterator();
        while(it.hasNext()){
        	Fact f2 = (Fact)it.next();
        	
        	if(f2.getOP()==null){	        	
	        	boolean isInExclusion=(exclusions!=null && exclusions.contains(f2.getPROPNAME()));
	        	
	        	boolean esEstructural=false;
	        	if(f2 instanceof ObjValue){
	        		ObjValue ov=(ObjValue)f2;
	        		esEstructural= isStructural(ov.getPROP());
	        		boolean yaProcesado= idosProcesados!=null && idosProcesados.contains(ov.getIDOVALUE());
	        		
	        		if(esEstructural){
	        			if(isInExclusion){
	        				printRule("DBGDEST ESTRUCT EXCL "+f2.getIDO()+" "+f2.getPROPNAME()+" "+destination+" "+destinoDescarte);

	        				if(destinoDescarte==null){
	        					//Si la estructura esta excluida pero tiene destino, y no se especifica un destino descarte, aplicamos destino del rango a la object property 
	        					String idRango=f2.getVALUE();
	        					if(idRango==null) idRango=f2.getINITIALVALOR();
	        					if(idRango!=null){
	        						Fact fact=jboss.getFact(Integer.valueOf(idRango),Constants.IdPROP_RDN);
	        						if(fact!=null){//Nulo si no esta el individuo en motor
	        							String childDestination=fact.getDESTINATIONSYSTEM();
	        							if(childDestination!=null){
	        								f2.setDestinationSystem(childDestination,false);
	        							}
	        						}
	        					}	
	        				}else f2.setDestinationSystem(destinoDescarte,false);
	        			}else{
	        				printRule("DBGDEST ESTRUCT NO EXCL "+f2.getIDO()+" "+f2.getPROPNAME()+" "+destination+" "+destinoDescarte+" procesado:"+yaProcesado+" value:"+ov.getIDOVALUE());
	        				//No esta excluido y es estructural, pongo mismo destino que root    
	        				f2.setDestinationSystem(destination,false);	    	
	        				if(ov.getIDOVALUE()==null){
	        					Integer value=ov.getINITIALRANGE();
	        					if(value!=null){
	       							Individual rango=jboss.getIndividualFact(value);
	       							if(rango!=null){
										rango.setDestinationSystem(destination);
									}
								}
	        				}		
	        				if(ov.getIDOVALUE()!=null && !yaProcesado){        		
	        					printRule("Propagar recursivo");     		
		        				setDestinationRecursively(idosProcesados,ov.getIDOVALUE(),destination,exclusions,destinoDescarte);
		        			}else{
		        				printRule("Ya procesado");
		        			}
	        			}
	        		}else{
	        			//no es recursivo, solo modifico la object prop
	        			f2.setDestinationSystem(destination,false);	
	        		}			      		
	        	}else{
	        		f2.setDestinationSystem(destination,false);	   	
	        	}	        		        		        	
        	}   	
        }
}
	
	public HashSet<Integer> getIDsExplicitPropertiesOf(int id) {
		HashSet<Integer> ids = new HashSet<Integer>();
		Iterator it = null;
		if (this.isIDCompleteClass(id)&&this.getMetaData() != null) {
			ids = this.getMetaData().getClases().get(id).getIdsPropiedades();
		}
		else  if(this.isIDClass(id)){
			it = ruleEngine.getInstanceFactsWhere(id).iterator();
		}
		else {
			it = ruleEngine.getInstanceFactsWhereIdo(id).iterator();
		}
		if (it != null) {
			while (it.hasNext()) {
				dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it.next();
				ids.add(f.getPROP());
			}
		}
		return ids;
	}

	
	
	

	/**
	 * Devuelve una lista con los identificadores de las propiedades est�ticas
	 * de un objeto o de un tipo de objeto.
	 * @param id:
	 *            identificador del objeto o del tipo de objeto
	 * @return: LinkedList <Integer> con los identificadores de sus propiedades.
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 */
	public HashSet<Integer> getAllIDsPropertiesOfClass(int idto) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException {
		HashSet<Integer> allids = new HashSet<Integer>();
		if (this.isIDCompleteClass(idto)) {
			if (this.getMetaData() != null) {
				allids = this.getMetaData().getClases().get(idto).getIdsPropiedades();
			} else {
				allids = this.getIDsExplicitPropertiesOf(idto);
			}
		}
		return allids;
	}

	public Iterator<dynagent.common.knowledge.IPropertyDef> getFactsPropertyIterator(Integer id, int idProp) throws NotFoundException, IncoherenceInMotorException {
		Iterator<dynagent.common.knowledge.IPropertyDef> itfp = null;
		if (this.isIDCompleteClass(id)) {
			itfp = ruleEngine.getAllInstanceFacts(id, null, idProp, null,null, null, null, null, null).iterator();
		} else {
			itfp = ruleEngine.getAllInstanceFacts(null, id, idProp, null,null, null, null, null, null).iterator();
		}
		return itfp;
	}


	/**
	 * Devuelve un iterador con todos los facts (instance ..) que tienen
	 * informaci�n sobre la property idProp del objeto cuyo identificador
	 * num�rico es id
	 * 
	 * @param id
	 * @param idProp
	 * @return: Lista con los facts instance
	 * @throws IncoherenceInMotorException 
	 */
	public LinkedList<dynagent.common.knowledge.IPropertyDef> getInstanceFacts(Integer id, Integer idProp) throws NotFoundException, IncoherenceInMotorException {
		LinkedList<dynagent.common.knowledge.IPropertyDef> instancefacts = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		if (this.isIDClass(id)) {// Se trata de una consulta de una	clase
			instancefacts = ruleEngine.getAllInstanceFacts(id, new Null(Null.NULL), idProp, null, null, null, null, null, null);
		} else {
			instancefacts = ruleEngine.getAllInstanceFacts(null, id, idProp,null, null, null, null, null, null);

		}
		if (instancefacts.size() == 0) {
			throw new NotFoundException("   No existe informacion en motor sobre id=" + id+ "  con la propiedad idProp=" + idProp);
		}
		return instancefacts;
	}
	
	
//	 public HashMap<Integer, String> getHmNamePropXIdProp() {
//		 return hmNamePropXIdProp;
//	 }
//
//	 public void setHmNamePropXIdProp(HashMap<Integer, String> hmNamePropXIdProp) {
//		 this.hmNamePropXIdProp = hmNamePropXIdProp;
//	 }
	 
	 


	 public HashMap<Integer, String> getHmNameClassXIdto() {
		return hmNameClassXIdto;
	}

	public void setHmNameClassXIdto(HashMap<Integer, String> hmNameClassXIdto) {
		this.hmNameClassXIdto = hmNameClassXIdto;
	}

	

	 /**
	  * Obtiene el identificador correspondiente a un rdn consultado el mapa de rdns.
	  * @param rdn
	  * @return
	  */public Integer getID_OF(String rdn){
		  Integer ido=null;
		  Iterator<Integer> it=this.getHmRdnxIDO().keySet().iterator();	
		  while(it.hasNext()){
			  int idov=it.next();
			  String vrdn=this.getHmRdnxIDO().get(idov);
			  if(vrdn.equals(rdn))
				  ido=idov;
		  }
		  if(ido==null){
			 // System.err.println("\n WARNING: llamada a getID_OF con rdn="+rdn+" no lo encunentra en el mapa. \n Tengase en cuenta que este metodo solo funciona con rdn de enumerados.\nMapa de enumerados=" +this.getHmRdnxIDO());  
		  }
		  return ido;
	  }

	  public boolean hasMetaDataLoad(){
		  return this.getMetaData()!=null;
	  }

	/**
	 * Obtiene el nombre de clase a partir de su identificador num�rico
	 * @param: id identificador de la clase o del individuo
	 * @return: nombre de la clase
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 */
	  
	public String getClassName(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException 
	{
		//TODO: Revisar si en alguna llamada podria pasarse un ido de individuo
		  return getClassName(id,false);
	}
	//IMPORTANTE SE AMPLIA PARA QUE TB DEVULVA EL LABEL DE UN TIPO DE DATO
	public String getClassName(int id,boolean ignoreCheckDataType) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		//System.out.println("  getClassname id="+id);
		String name=null;
		//TODO: Hay un problema en tratar idtos como idos, por ejemplo en valuecls en facts datatype, porque puede colisionar con idos codificados compactados (ej 7945) 
		String datatype=ignoreCheckDataType?null:Constants.getDatatype(id);
		if(datatype==null){
		
			if(this.isIDClass(id)){
				name=this.getHmNameClassXIdto().get(id);
			}
			else{
				name=this.getClassName(this.getClassOf(id));
			}
				if (name != null)
					return name;
				else
					throw new NotFoundException("  No existe nombre para id="+id);
				
		}	else{name=datatype;}
		return name;
	}
	
	public String getClassName(String sid) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		return this.getClassName(new Integer(sid));
	}
		
	public FactProp getFactProp(int idProp){
		if(this.getMetaData()!=null){
			return this.getMetaData().getHmPropiedades().get(idProp);
		}
		else
		{
			LinkedList<dynagent.ruleengine.src.ruler.FactProp> lfactp = ruleEngine.getPropertyFactsWhereIdProp(idProp);
			if(lfactp.size()==1){
				return lfactp.get(0);
			}
			else if(lfactp.size()==0){
			//	System.out.println("  Warning: geFactProperty idProp="+idProp+" no encuentra ese factprop:\n"+lfactp);
			//	Auxiliar.printCurrentStackTrace();
				return null;

			}
			else {
				System.err.println("  WARNING: getFactProp idProp="+idProp+" devuelve m�s de un fact property:\n"+lfactp);
				Auxiliar.printCurrentStackTrace();
				return null;				}
			}
	
	}

	/**
	 * Obtiene el nombre de una propiedad a partir de su identificador num�rico
	 * 
	 * @param idProp:
	 *            identificador de la propiedad
	 * @return: nombre de la propiedad
	 */
	public String getPropertyName(int idProp) throws NotFoundException {
		//PARA QUE DESDE EL PRINCIPIO EL METODO PROPNAME DE FACT CARGE BIEN LOS NOMBRES ES NECESARIO TENER LOS NOMBRES DESDE EL PRINCIPIO. SE PASAN LOS LABELS 
		//DESDE RULERFACTORYCOMMON A DDM
		//ASI SE HACIA ANTES:
		FactProp fp=this.getFactProp(idProp);
		if(fp!=null)
			return  fp.getNAME();
		else 
			return null;


		//return this.getHmNamePropXIdProp().get(idProp);
	}


	/**
	 * Obtiene el identificador de la propiedad a partir de su nombre
	 * 
	 * @param nameProp:
	 *            nombre de la propiedad
	 * @return: Integer con el identificador de la propiedad
	 * @throws IncoherenceInMotorException 
	 */
	public Integer getIdProperty(String nameProp) throws NotFoundException, IncoherenceInMotorException {
		Integer idprop=null;
		if(this.getMetaData()!=null){
			idprop=this.getMetaData().getHmIDPropiedades().get(nameProp);
		}
		else
		{
			Iterator itp = ruleEngine.getAllPropertyFacts(null, nameProp).iterator();
			Integer idProp = null;
			if (itp.hasNext() && idProp == null) {
				dynagent.ruleengine.src.ruler.FactProp f = (dynagent.ruleengine.src.ruler.FactProp) itp
				.next();
				idProp = f.getPROP();
			}
		}
		if (idprop != null){
			return idprop;
		}else{
			System.err.println("\n WARNING POSIBLE ERROR: SE HA LLAMADO A ddm.getIdProperty con nameProp="+nameProp+"  que no se encuentra en el modelo:");
			//Auxiliar.printCurrentStackTrace();
			return null;
		}
		
	}

	/**
	 * Obtiene la categoria (su representaci�n num�rica) de una propiedad a
	 * partir de su identificador num�rico
	 * 
	 * @param idProp:
	 *            identificador de la propiedad
	 * @return categoria de la propiedad. Si la propiedad no tiene ninguna
	 *         categoria definida devolver� null
	 */
	public Integer getCat(int idProp) throws NotFoundException {
		FactProp fp=this.getFactProp(idProp);
		if(fp!=null)
			return  fp.getCAT();
		else 
			return null;

	}

	/**
	 * Obtiene la categoria de una propiedad a partir de su identificador
	 * num�rico
	 * 
	 * @param idProp:
	 *            identificador de la propiedad
	 * @return categoria de la propiedad. Si la propiedad no tiene ninguna
	 *         categoria definida devolver� null
	 */
	public Category getCategory(int idProp) throws NotFoundException {
		Category category;
		Integer cat = this.getCat(idProp);
		if (cat != null)
			category = new Category(cat);
		else
			category = new Category();
		return category;
	}


	/**
	 * Obtiene el identificador de una clase a partir de su nombre
	 * 
	 * @param name:
	 *            nombre de la clase
	 * @return: Integer con el identificador de la clase (null si no se
	 *          encuentra ninguna en motor con ese nombre)
	 * @throws IncoherenceInMotorException 
	 */
	public Integer getIdClass(String name) throws NotFoundException, IncoherenceInMotorException {
		Integer idto=null;
		if(this.getMetaData()!=null){
			idto=this.getMetaData().getHmIDClases().get(name);
		}
		else{
			Iterator it = ruleEngine.getInstanceFactsWhereName( name).iterator();
			while (it.hasNext()&&idto==null) {
				dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it.next();
				idto = f.getIDTO();
			}
		}
		if (idto != null)
			return idto;
		else
			throw new NotFoundException("  No se encuentra la clase "+name);
	}

	/**
	 * Obtiene un iterador de enteros con las clases superiores de las que
	 * deriva una clase.
	 * 
	 * @param: idto identificador de la clase.
	 * @return: Iterator con los identificadores de las clases superiores.
	 */
	public Iterator<Integer> getSuperior(int idto) throws NotFoundException, IncoherenceInMotorException  {
		return this.getSuperiorHS(idto).iterator();
	}

	public HashSet<Integer> getSuperiorHS(int idto) throws NotFoundException, IncoherenceInMotorException{
		HashSet<Integer> idtossup = new HashSet<Integer>();
		if (this.getMetaData() != null&& this.isIDCompleteClass(idto) ) {
			idtossup= this.getMetaData().getClases().get(idto).getSuperiors();
		}
		else
		{
			if(isDispose()){
				System.err.println("ERROR: DocDataModel ya dispose hashCode:"+this.hashCode());
			}
			Iterator it = ruleEngine.getAllHierarchyFacts(idto, null).iterator();
			while (it.hasNext()) {
				dynagent.ruleengine.src.ruler.FactHierarchy f = (dynagent.ruleengine.src.ruler.FactHierarchy) it.next();
				if (f.getIDTOSUP().intValue() != idto){// excluimos de la lista de
					// hijos a la misma clase
					idtossup.add(f.getIDTOSUP());
				}
			}
		}
		idtossup.remove(idto);
		return idtossup;
	}


	public HashSet<Integer> getSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException  {
		return this.getSpecializedHS(idto);
	}


	/**
	 * getSpecialized Obtiene los identificadores de las clases que son
	 * especializadas de una dada.
	 * 
	 * @param: idto identificador de la clase
	 * @return: Lista de enteros con los identificadores de las clases que son
	 *          especializadas de la dada. Obtiene las clases que son derivadas
	 *          de i. Excluyendose ella misma.
	 * @throws NotFoundException 
	 */
	public HashSet<Integer> getSpecializedHS(int idto) throws NotFoundException, IncoherenceInMotorException  {
		if (this.getMetaData() != null&& this.isIDCompleteClass(idto) ) {
			return this.getMetaData().getClases().get(idto).getSpecializeds();
		}
		else{
			Iterator it = ruleEngine.getAllHierarchyFacts(null, idto).iterator();
			HashSet<Integer> specializeds = new HashSet<Integer>();
			while (it.hasNext()) {
				dynagent.ruleengine.src.ruler.FactHierarchy f = (dynagent.ruleengine.src.ruler.FactHierarchy) it		.next();
				if (f.getIDTO().intValue() != idto)// excluimos de la lista de
					// hijos a la misma clase
					specializeds.add(f.getIDTO());
			}
			specializeds.remove(idto);
			return specializeds;
		}
	}
	
	private Integer getClaseOfValue(String value)  {
		Iterator it = ruleEngine.getInstanceFactsWhereValue(value).iterator();
		while (it.hasNext()) {
			dynagent.ruleengine.src.ruler.Fact f = (dynagent.ruleengine.src.ruler.Fact) it		.next();
			if(f.getVALUECLS()!=null&&f.getVALUECLS().intValue()!=Constants.IDTO_MEMO&&f.getVALUECLS().intValue()!=Constants.IDTO_STRING){//para no obtener por error string de un datavalor que coincida con el ido del indivuo
				return f.getVALUECLS();
			}
		}
		//SI NO SE ENCUENTRA, DEVUELVE NULL
			return null;
	}
	
	public HashSet<Integer>getDirectSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException {
		HashSet<Integer>allchildren=this.getSpecializedHS(idto);
		HashSet<Integer>directchildren=new HashSet<Integer>();
		Iterator<Integer> it=allchildren.iterator();
		while(it.hasNext()){
			int idhijo=it.next();
			Iterator<Integer> it2=this.getSuperior(idhijo);
			boolean direct=true;
			while(it2.hasNext()&&direct){
				int idsuperior_of_specialized=it2.next();
				direct=!allchildren.contains(idsuperior_of_specialized);
			}
			if(direct)
				directchildren.add(idhijo);
		}
		return directchildren;
	}

	public HashSet<Integer>getDirectSuperior(int idto) throws NotFoundException, IncoherenceInMotorException{
		HashSet<Integer>allsuperiors=this.getSuperiorHS(idto);
		HashSet<Integer>directsuperiors=new HashSet<Integer>();
		Iterator<Integer> it=allsuperiors.iterator();
		while(it.hasNext()){
			int idpadre=it.next();
			Iterator<Integer> it2=this.getSpecialized(idpadre).iterator();
			boolean direct=true;
			while(it2.hasNext()&&direct){
				int idspecialized_of_superior=it2.next();
				direct=!allsuperiors.contains(idspecialized_of_superior);
			}
			if(direct)
				directsuperiors.add(idpadre);
		}
		return directsuperiors;
	}


	/**
	 * isSpecialized: Devuelve si una clases es especializada de otra
	 * 
	 * @param: idto: identificador de la clase
	 * @param: idtoPosSuperior: identificador de la posible clase superior
	 * @return: boolean indicando si idto es una clase especializda de
	 *          idtoPosSuperior
	 * @throws NotFoundException 
	 */
	public boolean isSpecialized(int idto, int idtoPosSuperior) throws IncoherenceInMotorException  {
		boolean isSpec = false;
		if (this.getMetaData() != null&& this.isIDCompleteClass(idto) ) {
			HashSet<Integer>padres= this.getMetaData().getClases().get(idto).getSuperiors();
			if(padres.contains(idtoPosSuperior))
				isSpec= true;
			else
				isSpec= false;
		}
		else{
			Iterator it;
			try {
				it = ruleEngine.getAllHierarchyFacts(idto, idtoPosSuperior).iterator();
			} catch (NotFoundException e) {
				//SI NO EXISTE ALGUNAS DE LAS CLASES EN ESTE MODELO, LA RESPUESTA ES FALSE.
				return false;
			}
			while (it.hasNext() && !isSpec) {
				isSpec = true;
			}
		}
		return isSpec;
	}

	/***************************************************************************
	 * isDatatype: Devuelve si una clase representa un tipo de dato o un tipo de
	 * objeto
	 * 
	 * @param valuecls:
	 *            identificador de la clase
	 * @return: true si es datatype y false en caso contrario
	 * @throws NotFoundException
	 */
	private boolean isDatatype(int valuecls) throws NotFoundException, IncoherenceInMotorException  {
		if (isUnit(valuecls)
				|| (valuecls > Constants.MIN_ID_DATACLS && valuecls < Constants.MAX_ID_DATACLS)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public Property SystemGetProperty(Integer ido,int idto,int idProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{ 
		return this.getProperty(ido,idto,idProp, null, Constants.USER_SYSTEM, null, false);
	}
	
	public PropertyValue SystemGetPropertyValue(Integer ido,int idto,int idProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{ 
		return this.getPropertyValue(ido,idto,idProp, null, Constants.USER_SYSTEM, null);
	}
	
	public void SystemSetValue(int ido, int idto,int idProp,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(ido, idto, idProp, oldVal, newVal, null, Constants.USER_SYSTEM, null,false);
	}
	
	public void SystemSetSuggestedValue(int ido, int idto,int idProp,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(ido, idto, idProp, oldVal, newVal, null, Constants.USER_SYSTEM, null,true);
	}
	
	
	public void SystemDeleteObject(int ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException{
		this.deleteObject(ido, idto, null, null, Constants.USER_SYSTEM, null);
	}
	
	public void setInfoCompletedClases(MetaData metadat) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		HashMap<String, Integer> hmIDClases = new HashMap<String, Integer>();
		HashMap<Integer, String> hmNameClases = new HashMap<Integer, String>();
		HashMap<Integer, HashSet<Integer>>hmIdsPropertiesXidto=new HashMap<Integer, HashSet<Integer>>();
		
		//mapa para las clases abstractas virtuales string -->clave: representaci�n del rango compuesto #clase1#clase2#.....valor:  
		HashMap<String,Clase> hmVirtualAbstracts=new HashMap <String,Clase>();
		//consultamos facts sobre completed class (ido null idto!=null)
		Iterator it = ruleEngine.getAllInstanceFacts(new Null(Null.NOTNULL),new Null(Null.NULL), null, null, null, null, null, null, null).iterator();
		if (!it.hasNext()) {  throw new NotFoundException("  No existe fact ((instance (IDTO notNull)(IDO nil)))");
		}
		while (it.hasNext()) {
			dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it.next();
			if(!hmIdsPropertiesXidto.containsKey(f.getIDTO())){
				HashSet<Integer>idsprops=new HashSet<Integer>();
				//if(f.getPROP()!=null){
				idsprops.add(f.getPROP());
				//}
				hmIdsPropertiesXidto.put(f.getIDTO(), idsprops);
				if(!hmIDClases.containsKey(f.getCLASSNAME())){
					hmIDClases.put(f.getCLASSNAME(), f.getIDTO());//guardamos idtoXname
					hmNameClases.put(f.getIDTO(),f.getCLASSNAME());//guardamos NameXidto
				}
			}
			//else if(f.getPROP()!=null){
			hmIdsPropertiesXidto.get(f.getIDTO()).add(f.getPROP());
			//}
		}
		//recorremos todas las clases
		Iterator<Integer> itidsclases=hmIdsPropertiesXidto.keySet().iterator();
		while(itidsclases.hasNext()){
			int idto=itidsclases.next();
			Clase clase=createClass(idto, hmNameClases, hmIdsPropertiesXidto, hmVirtualAbstracts);
			metadat.getClases().put(idto,clase);
		}
		
		//Creamos las abstractas virtuales a partir de los mapas que nos indican que abstractas virtuales tenemos que crear
		//Para las que vamos a crear hacemos un clone ya que el mapa de virtualAbstract es modificado dentro del m�todo con las nuevas abstractas virtuales que se crean en los rangos de sus properties
		createVirtualAbstracts(hmVirtualAbstracts, (HashMap<String,Clase>)hmVirtualAbstracts.clone(), hmIDClases, metadat);
		metadat.setHmIDClases(hmIDClases);
	}
	
	/**
	 * Crea una clase y la configura adecuadamente. Ademas en los mapas pasados por par�metros mete los datos relativos a esa configuracion
	 */
	private Clase createClass(int idto,HashMap<Integer, String> hmNameClases, HashMap<Integer, HashSet<Integer>>hmIdsPropertiesXidto,HashMap<String,Clase> hmVirtualAbstracts) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		Clase clase=new Clase();
		clase.setIdto(idto);
		clase.setName(hmNameClases.get(idto));
		// clases de las que hereda
		HashSet<Integer> padres=this.getSuperiorHS(idto);
		clase.setSuperiors(padres);
		// clases que hereden de ella
		HashSet<Integer> hijos=this.getSpecializedHS(idto);
		clase.setSpecializeds(hijos);
		// PROPIEDADES DE LA CLASE
		
		HashMap<Integer,HashSet<Integer>> listPropXidto=new HashMap<Integer, HashSet<Integer>>();
		HashSet<Integer> listIdProp=new HashSet<Integer>();
		listPropXidto.put(idto,hmIdsPropertiesXidto.get(idto));
		listIdProp.addAll(hmIdsPropertiesXidto.get(idto));
		
		//Si es abstracta completamos con las propiedades de los especializados, solo en los mapas
		if(isAbstract(idto)){
			clase.setAbstract(true);
			abstractClasses.add(idto);
// LO COMENTAMOS PQ DE MOMENTO EVITAMOS QUE SE CREEN PROPERTIES DE TODOS LOS ESPECIALIZADOS, SOLO DE LOS HIJOS DIRECTOS DESCENDIENDO POR ABSTRACTAS. SI VEMOS QUE HACE FALTA LO DESCOMENTAMOS Y COMENTAMOS EL DE ABAJO
//			Iterator itSpecialized = ruleEngine.getAllHierarchyFacts(null, idto).iterator();
//			while (itSpecialized.hasNext()) {
//				dynagent.ruleengine.src.ruler.FactHierarchy f = (dynagent.ruleengine.src.ruler.FactHierarchy) itSpecialized.next();
//				if (f.getIDTO().intValue() != idto){// excluimos de la lista de hijos a la misma clase
//					HashSet<Integer> listIdPropSpecialized=new HashSet<Integer>();
//					listIdPropSpecialized.addAll(hmIdsPropertiesXidto.get(f.getIDTO()));
//					listIdPropSpecialized.removeAll(listIdProp);//Quitamos los que ya esten en la lista para que no esten repetidos
//					listPropXidto.put(f.getIDTO(),listIdPropSpecialized);
//					listIdProp.addAll(listIdPropSpecialized);
//				}
//			}
			/*Iterator<Integer> itSpecialized = getSpecializedDescendByAbstract(idto).iterator();
			while (itSpecialized.hasNext()) {
				Integer idtoS = itSpecialized.next();
				if (idtoS != idto){// excluimos de la lista de hijos a la misma clase
					HashSet<Integer> listIdPropSpecialized=new HashSet<Integer>();
					listIdPropSpecialized.addAll(hmIdsPropertiesXidto.get(idtoS));
					listPropXidto.put(idtoS,(HashSet<Integer>)listIdPropSpecialized.clone());
					listIdPropSpecialized.removeAll(listIdProp);//Quitamos los que ya esten en la lista para que no esten repetidos
					listIdProp.addAll(listIdPropSpecialized);
				}
			}*/
		}else clase.setAbstract(false);
		
		
//		le asignamos sus ids de propiedades
		clase.setIdsPropiedades(listIdProp);
		Iterator<Integer> itrIdtos=listPropXidto.keySet().iterator();
		HashMap<Integer, Property> hmProperties = new HashMap<Integer, Property>();
		int num_properties=0;
		while(itrIdtos.hasNext()){
			int idtoToProcess=itrIdtos.next();
			for(int idProp:listPropXidto.get(idtoToProcess)){
				Property propertyActual=hmProperties.get(idProp);
				
				Property propertySpec=this.SystemGetProperty(null, idtoToProcess, idProp);
				
				if(propertyActual==null){
					propertyActual=propertySpec;
					
					// PARA TRANSFORMAR LOS RANGOS COMPUESTOS EN RANGOS A CLASES ABSTRACTAS VIRTUALES
					if(propertyActual instanceof ObjectProperty){
						ObjectProperty op=(ObjectProperty) propertyActual;
						if(op.getRangoList().size()>1){
							modifyRangeToAbstractVirtual(op, hmVirtualAbstracts, hmVirtualAbstracts/*Pasamos el mismo mapa ya que no vamos a procesar aqui las nuevas VirtualAbstract. Solo queremos que este en el mapa para luego procesarlo fuera de este metodo*/);
						}
					}
										
					propertyActual.setIdto(idto);
					hmProperties.put(idProp, propertyActual);
					num_properties++;
				}
				
				changeCardinalityRestriction(propertyActual, propertySpec);
				
				if(propertySpec instanceof ObjectProperty){
					changeRangeRestriction((ObjectProperty)propertyActual, (ObjectProperty)propertySpec);
				}
				
			}
		}
		clase.setHmProperties(hmProperties);
		clase.setNum_properties(num_properties);	
		return clase;
	}
	
	
	/**
	 * CLASES VIRTUALES ABSTRACTAS. LE A�ADIMOS LAS PROPIEDADES COMUNES DE LAS CLASES DEL RANGO COMPUESTO.
	 * Es recursivo para ir creando abstractas virtuales tambien de los rangos de las abstractas virtuales
	 * @param hmVirtualAbstracts Mapa de las abstractas virtuales ya encontradas. Sirve para no crear nuevas abstractas virtuales de algo que ya ha sido creado o va a ser creado en este metodo. Por lo que tiene que tener tambien los datos que hay en hmNewVirtualAbstracts
	 * @param hmNewVirtualAbstracts Mapa de las abstractas virtuales a crear
	 * @param hmIDClases Mapa donde se a�aden las nuevas clases creadas
	 * @param metadat
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws DataErrorException 
	 */
	private void createVirtualAbstracts(HashMap<String,Clase> hmVirtualAbstracts,HashMap<String,Clase> hmNewVirtualAbstracts,HashMap<String, Integer> hmIDClases,MetaData metadat) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		HashMap<String,Clase> hmNewVirtualAbstractsAux=new HashMap<String, Clase>();
		Iterator<String> itvirt=hmNewVirtualAbstracts.keySet().iterator();
		while (itvirt.hasNext()){
			String index=itvirt.next();
			Clase claseaVirtualAbstract= hmNewVirtualAbstracts.get(index);

			configVirtualAbstract(claseaVirtualAbstract, metadat, hmIDClases);
			
			//a�admimos el objeto clase que representa a la virtualabstract a metadata
			hmIDClases.put(claseaVirtualAbstract.getName(), claseaVirtualAbstract.getIdto());
			//System.err.println("\n\n DEBUG VIRTUAL ABSTRACT: \n"+claseaVirtualAbstract);
			metadat.getClases().put(claseaVirtualAbstract.getIdto(),claseaVirtualAbstract);
			
			Iterator<Property> itr=claseaVirtualAbstract.getAllProperties();
			while(itr.hasNext()){
				Property property=itr.next();
				if(property instanceof ObjectProperty){
					ObjectProperty op=(ObjectProperty) property;
					if(op.getRangoList().size()>1){
						modifyRangeToAbstractVirtual(op, hmVirtualAbstracts, hmNewVirtualAbstractsAux);
						hmVirtualAbstracts.putAll(hmNewVirtualAbstractsAux);
					}
				}
			}
		}
		
		if(!hmNewVirtualAbstractsAux.isEmpty()){
			//System.err.println(hmNewVirtualAbstractsAux);
			createVirtualAbstracts(hmVirtualAbstracts, hmNewVirtualAbstractsAux, hmIDClases, metadat);
		}
	}
	
	/**
	 * Se encarga de asignar una virtual abstract al rango de una property
	 * @param op Property con rangolist mayor que 1. Funciona con rango=1 pero no tiene sentido crear una abstract virtual para eso
	 * @param hmVirtualAbstracts Datos de virtual abstracts existentes
	 * @param hmNewVirtualAbstracts Mapa en el que se a�adir� la nueva virtual abstract asignada a este rango
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws DataErrorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 */
	private void modifyRangeToAbstractVirtual(ObjectProperty op,HashMap<String,Clase> hmVirtualAbstracts,HashMap<String,Clase> hmNewVirtualAbstracts) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		java.util.Collections.sort(op.getRangoList());
		
		
		//System.err.println("\n DEBUG RANGO COMPUETO: prop=."+op.getName()+"  idto="+op.getIdto());						
		String index=null;
		for(int i=0;i<op.getRangoList().size();i++){
			index+="#"+op.getRangoList().get(i);
		}
		Integer newidto=null;
		if(hmVirtualAbstracts.get(index)!=null){
			newidto=hmVirtualAbstracts.get(index).getIdto();
		}
		else{
			newidto=this.getMaxIdto()+1;
			this.getHmIDOSxIDTO().put(newidto, new HashSet<Integer>());
			Clase claseaVirtualAbstract=new Clase();
			//String namevirtualabstract="VIRTUALABSTRACT"+index;
			claseaVirtualAbstract.setIdto(newidto);
			
			// clases de las que hereda ninguna
			claseaVirtualAbstract.setSuperiors(new HashSet<Integer>());
			// clases que hereden de ella-->
				//- los elemenos del rango compuesto
				//-y sus hijos.
			HashSet<Integer> hijosDeClaseVirtual=new HashSet<Integer>();
			for(int i=0;i<op.getRangoList().size();i++){
				int idto=op.getRangoList().get(i);
				if(getAccessOverObject(idto, null, user, null).getAbstractAccess())
					hijosDeClaseVirtual.addAll(getSpecializedDescendByAbstractToAbstractVirtual(idto));
				else hijosDeClaseVirtual.add(idto);
				
			}
			
			claseaVirtualAbstract.setSpecializeds(hijosDeClaseVirtual);
			
			claseaVirtualAbstract.setAbstract(true);
			abstractClasses.add(newidto);
			
			hmNewVirtualAbstracts.put(index, claseaVirtualAbstract);
		
			
			///a�adimos el permiso abstracto
			ruleEngine.insertFact(ka.buildAbstractAccess(newidto));
			
			this.addIDtoLevel(newidto, Constants.LEVEL_MODEL);
			
		}
		
		
		//sustituimnos el rango compuesto por el abstracto
		op.getRangoList().remove();
		LinkedList<Integer> newrango=new LinkedList<Integer>();
		newrango.add(newidto);
		op.setRangoList(newrango);
	}
	
	private HashSet<Integer> getSpecializedDescendByAbstractToAbstractVirtual(int idto) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		HashSet<Integer> directSpecialized=getDirectSpecialized(idto);
		HashSet<Integer> auxDirectSpecialized=new HashSet<Integer>();
		for(int idtoS:directSpecialized){
			if(idtoS!=idto){
				if(isAbstract(idtoS)){
					auxDirectSpecialized.addAll(getSpecializedDescendByAbstractToAbstractVirtual(idtoS));
				}else auxDirectSpecialized.add(idtoS);	
			}
		}
		return auxDirectSpecialized;
	}
	
	/**
	 * Configura los datos de una abstracta virtual creandole su nombre, propiedades...
	 * @param claseaVirtualAbstract
	 * @param metadat
	 * @param hmIDClases
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 */
	private void configVirtualAbstract(Clase claseaVirtualAbstract,MetaData metadat,HashMap<String, Integer> hmIDClases) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		LinkedList<LinkedList> listasprops=new LinkedList<LinkedList>();
		Iterator<Integer> ithijos=claseaVirtualAbstract.getSpecializeds().iterator();
		//mejoraremos el nombre de la clase abstracta llamandola nombreclase1 y nombreclase2 y .....
		String namevirtualclase=null;
		
		while(ithijos.hasNext()){
			int idtohijo=(Integer)ithijos.next();
			
			//a cada "hijo" (elemento del rango compuesto) le indicamos como superior la clase abstracta
			metadat.getClase(idtohijo).getSuperiors().add(claseaVirtualAbstract.getIdto());
			if(namevirtualclase==null){
				namevirtualclase=metadat.getClase(idtohijo).getName();
				
			}else{
				namevirtualclase+=", "+metadat.getClase(idtohijo).getName();
				
			}
			LinkedList<Integer> idprops=new LinkedList(metadat.getClase(idtohijo).getIdsPropiedades());
			listasprops.add(idprops);
		}
		claseaVirtualAbstract.setName(namevirtualclase);
		
		this.getHmNameClassXIdto().put(claseaVirtualAbstract.getIdto(),namevirtualclase);
		///obtenemos las propiedades comunes a todas las clases que forman el rango compuesto 
		LinkedList<Integer> propscomunes=Auxiliar.getCommonsElementsOfArrays(listasprops);
		HashSet<Integer> propsunion=Auxiliar.getUnionElementsOfArrays(listasprops);
		
		
		//System.err.println("\n DEBUG VIRTUAL: namevirtualclase="+namevirtualclase+"\n propscomunes="+propscomunes+"  propsunion="+propsunion);
		
		HashSet<Integer>propiedadesclasevirtual=new HashSet(propscomunes);
		/*claseaVirtualAbstract.setIdsPropiedades(propiedadesclasevirtual);
		HashMap<Integer, Property> hmPropertiesComunes=new HashMap<Integer, Property>(); 
		for(int i=0;i<propscomunes.size();i++){
			boolean propiedada�adida=false;
			Iterator<Integer> ithijos2=claseaVirtualAbstract.getSpecializeds().iterator();
			while(ithijos2.hasNext()&&!propiedada�adida){
				Integer idtohijo=ithijos2.next();
				if(metadat.getClase(idtohijo).getIdsPropiedades().contains(propscomunes.get(i))){
					Property pclone=metadat.getClase(idtohijo).getProperty(propscomunes.get(i)).clone();
					pclone.setIdto(claseaVirtualAbstract.getIdto());
					hmPropertiesComunes.put(pclone.getIdProp(),pclone);
					propiedada�adida=true;
				}
			}
		}
		claseaVirtualAbstract.setHmProperties(hmPropertiesComunes);//a�adimos todas las propiedades comunes a la clase
		claseaVirtualAbstract.setNum_properties(propscomunes.size());
		*/
		
		claseaVirtualAbstract.setIdsPropiedades(propsunion);
		HashMap<Integer, Property> hmPropertiesUnion=new HashMap<Integer, Property>(); 
		Iterator<Integer> itPrUn=propsunion.iterator();
		while(itPrUn.hasNext()){
			int propUnion=itPrUn.next();
			boolean propiedadañadida=false;
			Iterator<Integer> ithijos2=claseaVirtualAbstract.getSpecializeds().iterator();
			while(ithijos2.hasNext()){
				Integer idtohijo=ithijos2.next();
				if(metadat.getClase(idtohijo).getIdsPropiedades().contains(propUnion)){

					ObjectProperty propertyActual=(ObjectProperty)hmPropertiesUnion.get(propUnion);
					ObjectProperty propertySpec=(ObjectProperty)metadat.getClase(idtohijo).getProperty(propUnion);
					
					if(propertyActual==null){
						propertyActual=propertySpec.clone();
						
						
						propertyActual.setIdto(claseaVirtualAbstract.getIdto());
						hmPropertiesUnion.put(propertyActual.getIdProp(),propertyActual);
					}
					
					changeCardinalityRestriction(propertyActual, propertySpec);	
					changeRangeRestriction(propertyActual, propertySpec);
				}
			}
		}
		
		claseaVirtualAbstract.setHmProperties(hmPropertiesUnion);//a�adimos todas las propiedades comunes a la clase
		claseaVirtualAbstract.setNum_properties(propsunion.size());
		
		//la claseVirtualAbstracata debe tener como rango en las objectProperty el rango uni�n de sus clases hijas
		HashMap<Integer,HashSet<Integer>> rangoXpropiedad=new HashMap<Integer,HashSet<Integer>>(); 
		HashSet<Integer> hijos= claseaVirtualAbstract.getSpecializeds();
		Iterator<Integer> itHijos=hijos.iterator();
		access acc=new access(0);
		while ( itHijos.hasNext()){
			Integer hijo=itHijos.next();
			Iterator<Property> itOp=metadat.getClase(hijo).getObjectProperties();
			while (itOp.hasNext()){
				ObjectProperty op=(ObjectProperty)itOp.next();
				if(rangoXpropiedad.get(op.getIdProp())==null)
					rangoXpropiedad.put(op.getIdProp(),new HashSet<Integer>(op.getRangoList()));
				else{
					rangoXpropiedad.get(op.getIdProp()).addAll(op.getRangoList());
				}
			}
		}
		
		Iterator itrangoXpropiedad=rangoXpropiedad.keySet().iterator();
		while (itrangoXpropiedad.hasNext()){
			Integer prop=(Integer)itrangoXpropiedad.next();
//			sustiimos los rangos union simplificados en la virtualAbstract
			ObjectProperty op=(ObjectProperty)claseaVirtualAbstract.getProperty(prop);
			op.setRangoList(this.ka.getSimplifyUnion(new LinkedList<Integer>(rangoXpropiedad.get(prop))));
		}
		
	}

	/**
	 * Se encarga de modificar property poniendo la cardinalidad menos restrictiva entre la que tiene definida y la que hay en propertySpecialized
	 * @param property que ser� cambiada
	 * @param propertySpecialized se utilizar� para comparar con el par�metro property
	 */
	private void changeCardinalityRestriction(Property property, Property propertySpecialized) {
		if(propertySpecialized!=property){
			if(!Auxiliar.equals(propertySpecialized.getCardMin(),property.getCardMin())){
				if(propertySpecialized.getCardMin()!=null){
					if(property.getCardMin()!=null){
						property.setCardMin(Math.max(property.getCardMin(), propertySpecialized.getCardMin()));
					}
				}else{
					if(property.getCardMin()!=null){
						property.setCardMin(null);
					}
				}
						
			}
			
			if(!Auxiliar.equals(propertySpecialized.getCardMax(),property.getCardMax())){
				if(propertySpecialized.getCardMax()!=null){
					if(property.getCardMax()!=null){
						property.setCardMax(Math.min(property.getCardMax(), propertySpecialized.getCardMax()));
					}
				}else{
					if(property.getCardMax()!=null){
						property.setCardMax(null);
					}
				}
						
			}
		}
	}
	
	private void changeRangeRestriction(ObjectProperty property, ObjectProperty propertySpecialized) throws IncoherenceInMotorException, NotFoundException {
		if(propertySpecialized!=property && !property.getRangoList().isEmpty() && !propertySpecialized.getRangoList().isEmpty()){
			//System.err.println("Property:"+property+" propertySpecialized:"+propertySpecialized);
			int idRange=property.getRangoList().get(0);
			int idRangeSpecialized=propertySpecialized.getRangoList().get(0);
			if(!Auxiliar.equals(idRange,idRangeSpecialized)){
				if(isSpecialized(idRange,idRangeSpecialized)){
					property.getRangoList().set(0, idRangeSpecialized);
				}else if(isSpecialized(idRangeSpecialized,idRange)){
					property.getRangoList().set(0, idRange);
				}else{
					//System.err.println("Property:"+property+" propertySpecialized:"+propertySpecialized);
					//Integer idRangeCommon=getDirectSuperiorOfBoth(idRange, idRangeSpecialized);
					HashSet<Integer> specialized1=getDirectSpecialized(idRange);
					HashSet<Integer> specialized2=getDirectSpecialized(idRangeSpecialized);
					HashSet<Integer> specialized=new HashSet<Integer>();
					if(!specialized1.isEmpty())
						specialized.addAll(specialized1);
					else specialized.add(idRange);
					if(!specialized2.isEmpty())
						specialized.addAll(specialized2);
					else specialized.add(idRangeSpecialized);
					Integer idRangeCommon=getDirectSuperior(specialized);
					if(idRangeCommon==null){
						System.err.println("ERROR: Properties con rango sin padre com�n al intentar crear property para abstracta usando property1:"+property+"\n y property2:"+propertySpecialized);
					}
					property.getRangoList().set(0,idRangeCommon);
				}
			}
		}
	}
	
//	private Integer getDirectSuperiorOfBoth(int idto1,int idto2) throws IncoherenceInMotorException, NotFoundException{
//		HashSet<Integer> parents=getDirectSuperior(idto1);
//		Iterator<Integer> itrParents=parents.iterator();
//		Integer idtoDirectSuperior=null;
//		while(idtoDirectSuperior==null && itrParents.hasNext()){
//			int idtoParent=itrParents.next();
//			if(isSpecialized(idto2, idtoParent)){
//				idtoDirectSuperior=idtoParent;
//			}
//		}
//		
//		if(idtoDirectSuperior==null){
//			itrParents=getSuperior(idto1);
//			while(idtoDirectSuperior==null && itrParents.hasNext()){
//				int idtoParent=itrParents.next();
//				if(isSpecialized(idto2, idtoParent)){
//					idtoDirectSuperior=idtoParent;
//				}
//			}
//		}
//		
//		if(idtoDirectSuperior==null){
//			Iterator<Integer> itrSpecialized=getDirectSpecialized(idto1).iterator();
//			boolean specialized=itrSpecialized.hasNext()?true:false;
//			while(itrSpecialized.hasNext()){
//				int idtoSpec=itrSpecialized.next();
//				if(!isSpecialized(idtoSpec, idto2))
//					specialized=false;
//			}
//			if(specialized){
//				idtoDirectSuperior=idto2;
//			}else{
//				itrSpecialized=getDirectSpecialized(idto2).iterator();
//				specialized=itrSpecialized.hasNext()?true:false;
//				while(itrSpecialized.hasNext()){
//					int idtoSpec=itrSpecialized.next();
//					if(!isSpecialized(idtoSpec, idto1))
//						specialized=false;
//				}
//				
//				if(specialized)
//					idtoDirectSuperior=idto1;
//			}
//		}
//		
//		return idtoDirectSuperior;
//	}
	
	private Integer getDirectSuperior(HashSet<Integer> idtos) throws IncoherenceInMotorException, NotFoundException{
		if(!idtos.isEmpty()){
			HashSet<Integer> listDirect=null;
			for(Integer idto:idtos){
				HashSet<Integer> parents=getDirectSuperior(idto);
				if(listDirect==null){
					listDirect=new HashSet<Integer>();
					listDirect.addAll(parents);
				}else{
					listDirect.retainAll(parents);
				}
			}
			if(listDirect.isEmpty()){
				for(Integer idto:idtos){
					HashSet<Integer> parents=getSuperiorHS(idto);
					if(listDirect.isEmpty()){
						listDirect.addAll(parents);
					}else{
						listDirect.retainAll(parents);
					}
				}
			}
			return listDirect.isEmpty()?null:listDirect.iterator().next();
		}else return null;
	}
	
	public void setInfoProperties(MetaData metadat) throws NotFoundException, IncoherenceInMotorException{
		HashMap<Integer,FactProp>hmallproperties = new HashMap<Integer,FactProp>();
		HashMap<String, Integer> hmIDPropiedades = new HashMap<String, Integer> ();
		Iterator it =ruleEngine.getAllPropertyFacts(null,null).iterator();
		while (it.hasNext()) {
			dynagent.ruleengine.src.ruler.FactProp fp = (dynagent.ruleengine.src.ruler.FactProp) it.next();
			hmallproperties.put(fp.getPROP(),fp);
			if(fp.getNAME()!=null&&!hmIDPropiedades.containsKey(fp.getNAME())){
				hmIDPropiedades.put(fp.getNAME(), fp.getPROP());
			}
		}
		metadat.setHmIDPropiedades(hmIDPropiedades);
		metadat.setHmPropiedades(hmallproperties);
	} 


		
	
	
	/**
	 * getAllIdPropertiesIterator: Obtiene todas las propiedades que hay en
	 * motor
	 * 
	 * @return: Iterador con los identificadores de las propiedades
	 * @throws IncoherenceInMotorException 
	 */
	public Iterator<Integer> getAllIdPropertiesIterator()
	throws NotFoundException, IncoherenceInMotorException {
		if(this.getMetaData()!=null){
			return this.getMetaData().getHmPropiedades().keySet().iterator();
		}
		else
		{
			Iterator it =ruleEngine.getAllPropertyFacts(null,null).iterator();
			HashSet<Integer> idsProp = new HashSet<Integer>();
			if (!it.hasNext()) {
				throw new NotFoundException(
				"  No se encontr� ninguna propiedad en motor: no existen facts: property (PROP ?p&~nil)))");
			}
			while (it.hasNext()) {
				dynagent.ruleengine.src.ruler.FactProp f = (dynagent.ruleengine.src.ruler.FactProp) it
				.next();
				idsProp.add(f.getPROP());
			}
			return idsProp.iterator();
		}
	}

	

	/**
	 * getIndividualsIterator: Obtiene los individuos (en amplio sentido:
	 * individuos, prototipos,..) adscritos a una clase.
	 * 
	 * @param: idto: identificador de la clase
	 * @param:level: Level en motor del "individuo"
	 * @return: Iterador con los identificadores de los individuos
	 */
	public HashSet<Integer> getIndividualsOfLevel(int idto, int level) {
		Iterator<Integer> itidos=this.hmIDOSxIDTO.get(idto).iterator();
		HashSet<Integer> idos = new HashSet<Integer>();
		boolean existinmotor=false;
		if (itidos != null){
			while (itidos.hasNext()) {
				existinmotor=true;
				int ido=itidos.next();
				if(this.hmLEVELxID.get(ido)==level){
					String state=getIndividualState(ido);
					if(state==null/*Si es filtro*/ || !state.equals(Constants.INDIVIDUAL_STATE_DELETED))
						idos.add(ido);
				}		
			}
		}
		return idos;
	}

	/**
	 * getIndividualsIterator: Obtiene los individuos (en amplio sentido:
	 * individuos, prototipos,..) adscritos a una clase.
	 * 
	 * @param: idto: identificador de la clase
	 * @param:level: Level en motor del "individuo"
	 * @return: Iterador con los identificadores de los individuos
	 */
	public HashSet<Integer> getIndividuals(int idto) {
		return this.getIndividualsOfLevel(idto, Constants.LEVEL_INDIVIDUAL);
	}
	
	public HashSet<Integer> getPrototypes(String nameClass) throws NotFoundException, IncoherenceInMotorException {
		return this.getIndividualsOfLevel(this.getIdClass(nameClass), Constants.LEVEL_PROTOTYPE);
	}

		private String prepareName(String s) {
		String output = "";
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) != '"' && s.charAt(i) != '\\')
				output += s.charAt(i);
		}
		return output;
	}



	/***************************************************************************
	 * getAllPropertyIterator Devuelve un iterator de objetos Property con todas
	 * las propiedades del objeto id o del tipo de objeto de identificado (id)
	 * Si se trata de un objeto o de un tipo de objeto se resuelve gracias a que
	 * hay rangos de identificadores reservados (ver clase Constantes).
	 * 
	 * @param: id identificador del objeto o del tipo de objeto
	 * @ param: idto: identificador de la clase o de la clase del objeto
	 * @return: Un Iterador de Objetos Property.
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
		
	public Iterator<Property> getAllPropertyIterator(Integer ido, int idto,Integer userRol,String user, Integer usertask, boolean forceFilter) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		Iterator<Property> itpropiedades = null;
		//JSystem.out.println("   getallpropertyiterator id="+id);
		
		HashSet n=null;
		HashSet n2=null;
		if (ido!=null&&!this.existInMotor(ido)) {
			this.getFromServer(ido, idto,1,userRol, user, usertask);
		}
		this.checkRangesIdoIdto(ido, idto);
		this.checkIncorrectParamsIdoIdtoWhenIdoInRuler(ido, idto);

		
		// MEJORA RENDIMIENTO SI ES CLASE Y METADATA SE HA CARGADO
		if (ido==null&&this.getMetaData() != null&&this.isIDCompleteClass(idto)) {
			itpropiedades = Auxiliar.cloneIterProperty(this.getMetaData().getAllPropertyIterator(idto));
		} else {
			LinkedList<Property> allProperties = new LinkedList<Property>();
			HashSet<Integer> sidsProperties = this.getAllIDsPropertiesOfClass(idto);
			Object[] idsordenadas = sidsProperties.toArray();
			Arrays.sort(idsordenadas);
			for (int i = 0; i < idsordenadas.length; i++) {
				Integer idProp = (Integer) idsordenadas[i];
				Property p=this.getProperty(ido,idto, idProp, userRol, user, usertask, forceFilter);
				if(p!=null){//excluimos las nulas pq cuando access no es view o cardmax=0 getProperty devuelve null
					allProperties.add(p);
				}
			}
			itpropiedades = allProperties.iterator();
		}
		return itpropiedades;
	}

	public synchronized Iterator<Property> getAllPropertyIterator(Integer ido, int idto,Integer userRol,
			String user, Integer usertask, Session sessionPadre)
			throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Iterator it = null;
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			SessionController.getInstance().setActual(sess,this);
			//System.out.println("DENTRO DE getAllPropertyIterator public");
			it = getAllPropertyIterator(ido, idto,userRol, user, usertask, true);

			// al final hacer un commit. si falla el commit -> lanzar una
			// exepcion para deshacer lo que estaba haciendo el commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.getAllPropertyIterator cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.getAllPropertyIterator no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}

		return it;
	}

	/**
	 * getPropertyIterator Devuelve un iterador sobre las propiedades del
	 * individuo (� clase) cuya id se le pasa como par�metro. Si la property es
	 * de una static class o solo est� participando en un proceso devuelve solo
	 * un elemento, si no es as� proporciona una property por cada proceso (y �
	 * rol) distinto. Par�metros:
	 * 
	 * @param: int id: identificador de la clase o del individuo
	 * @param: int idProp: identificador de la propiedad
	 * @param: int userRol: identificador del userRol bajo el que se encuentra
	 *         el usuario
	 * @param: String user: nombre(login) del usuario logado
	 * @param: int usertask: identificador de la usertask
	 * @return Un Iterador de Objetos Property
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 */
	public synchronized Property getProperty(Integer ido, int idto,int idProp, Integer userRol,String user, Integer usertask, Session sessionPadre)
	throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Property p = null;
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
	
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
//			System.out.println("DENTRO DE getPropertyIterator public");
			SessionController.getInstance().setActual(sess,this);
			p = this.getProperty(ido, idto, idProp, userRol, user, usertask, true);
			// si todo ha ido bien. commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.getProperty cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.getProperty no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
		/*
		 * TODO	A DEPURAR: SALE UNA EXCEPCI�N AL DIVIDIR POR CERO EL NUMERO DE SESI�N
		 * if(p instanceof ObjectProperty){////NUEVO, SI ASE APUNTA A UNA CLASE ABSTRACTA Y SOLO TIENE UN HIJO, SE FACILITA AL USUARIO ASIGNANDOLE YA LA HIJA
			ObjectProperty op=(ObjectProperty)p;
				if(op.getRangoList().size()==1&&this.getLevelOf(op.getRangoList().getFirst()).equals(Constants.LEVEL_FILTER)){
					int rangetype=this.getClassOf(op.getRangoList().getFirst());
					if(this.getAccessOverObject(rangetype, userRol, user, usertask).getAbstractAccess()){
						int rangohijo=this.createPrototype(op.getRangoList().getFirst(), Constants.LEVEL_FILTER, userRol, user, usertask, sess);
						op.getRangoList().remove();
						op.getRangoList().add(rangohijo);
					}
				}
		}
////	NUEVO, SI TIENE CARDINALIDAD M�XIMA 0 NO LA DEVOLVEMOS
		if(p.getCardMax()!=null&&p.getCardMax()==0){
			p=null;
		}
		*/
		return p;
	}
	
	public LinkedList<StringChanged> getInitialValue(int ido, int idProp){	
		if(ruleEngine instanceof JBossEngine){
			JBossEngine jb=(JBossEngine)ruleEngine;
			return jb.getInitialValue(ido,idProp);
		}
		return new LinkedList<StringChanged>();
	}
	
	public void retractQueryLog(QueryValue  f) {
		if(ruleEngine instanceof JBossEngine){
			JBossEngine jb=(JBossEngine)ruleEngine;
			jb.retractQueryLog(f);
		}
	}
	
	public PropertyValue getPropertyValue(int ido, int idto,int idProp,Integer userRol,String user, Integer usertask) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		
		this.checkRangesIdoIdto(ido, idto);
		
		PropertyValue p = null;	
		if (!this.existInMotor(ido)) {  
			this.getFromServer(ido,idto,1, userRol, user, usertask);
		}
		this.checkIncorrectParamsIdoIdtoWhenIdoInRuler(ido, idto);
		
		// Obtenemos todos los facts (instance....) del motor con
		// informaci�n sobre la propiedad idProp en el objeto id
		LinkedList<dynagent.common.knowledge.IPropertyDef> instances;
		instances =  ruleEngine.getAllInstanceValuesFacts(ido, idProp);
		p = ka.buildPropertyValueOf(instances);
		return p;
	}
	
	public Fact getFact(int ido, int prop){
		if(ruleEngine instanceof JBossEngine){
			JBossEngine jb=(JBossEngine)ruleEngine;
			return jb.getFact(ido, prop);
		}
		return null;
	}
	
	public void consumirEventoCambio(Domain dom,String nameProp) throws NotFoundException, IncoherenceInMotorException{
		int idProp=this.getIdProperty(nameProp);
		LinkedList<dynagent.common.knowledge.IPropertyDef> instances =  ruleEngine.getAllInstanceValuesFacts(dom.getIdo(), idProp);
		if(!instances.isEmpty()){
			Fact f=(Fact)instances.get(0);
			//System.err.println("\n\n DEBUG consumirEventoCambio nameProp:"+nameProp+" instances.size()="+instances.size()+" fact antes consumirEventoCAmbio:"+f);
			f.consumirEventoCambio();
			//System.err.println("\n\n DEBUG consumirEventoCambio nameProp:"+nameProp+"  fact TRAS CONSUMIREVENTOCAMBIO:"+f);
		}
		else{
			//System.err.println("\n\n DEBUG consumirEventoCambio nameProp:"+nameProp+" instances.size()="+instances.size()+ "domain:"+dom);
		}
	}
	
	
	public PropertyValue SystemGetPropertyValue(Integer ido, int idto,String  nameProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Integer idprop=this.getIdProperty(nameProp);
		if(idprop==null)
			 throw new NotFoundException("No se encuentra en motor la propiedad "+nameProp);
		 return this.getPropertyValue(ido, idto,idprop,null,Constants.USER_SYSTEM,null);
	}
	
	
	
	public PropertyValue getPropertyValueAnteriores(int id, int idto,int idProp) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		PropertyValue p = null;
		//System.out.println("  getPropertyValue id="+id+"  idProp="+idProp);
		if (!this.existInMotor(id)) {  
			this.getFromServer(id, idto,1,null, this.getUser(), null);
		}
		// Obtenemos todos los facts (instance....) del motor con
		// informaci�n sobre la propiedad idProp en el objeto id
		LinkedList<dynagent.common.knowledge.IPropertyDef> instances;
		instances =  ruleEngine.getAllInstanceValuesFacts(id, idProp);
		LinkedList<dynagent.common.knowledge.IPropertyDef> instancesAnteriores = new LinkedList<IPropertyDef>();
		Iterator <dynagent.common.knowledge.IPropertyDef> itr  = instances.iterator();
		while(itr.hasNext())
		{
			dynagent.common.knowledge.IPropertyDef actual = itr.next();
			if(actual instanceof Fact)
			instancesAnteriores.add(((Fact)actual).getValoresAnteriores());
		}
		
		p = ka.buildPropertyValueOf(instancesAnteriores);
			
		return p;
	}


	public Iterator <PropertyValue> getAllPropertiesValues(int id,int idto) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		//TODO GETALLPROPERTYVALUES SE PUEDE MEJORAR RENDIMIENTO HACIENDO UNA CONSULTA POR IDO, Y AGRUPANDO LOS FACTS POR DISTINTOS IDPROP

		Iterator<PropertyValue> itpropiedades = null;
		if (!this.existInMotor(id)) {
			this.getFromServer(id,  idto,1,null, this.getUser(), null);
		}
		// MEJORA RENDIMIENTO SI ES CLASE Y METADATA SE HA CARGADO

		LinkedList<PropertyValue> allProperties = new LinkedList<PropertyValue>();
		HashSet<Integer> sidsProperties = this.getIDsExplicitPropertiesOf(id);
		Object[] idsordenadas = sidsProperties.toArray();
		Arrays.sort(idsordenadas);
		for (int i = 0; i < idsordenadas.length; i++) {
			Integer idProp = (Integer) idsordenadas[i];
			PropertyValue p=this.SystemGetPropertyValue(id,idto, idProp);
			if(p!=null){//excluimos las nulas pq cuando access no es view o cardmax=0 getProperty devuelve null
				allProperties.add(p);
			}
		}
		itpropiedades = allProperties.iterator();
		return itpropiedades;
	}
	
	
	public Property getProperty(Integer ido,int idto, int idProp, Integer userRol,String user, Integer usertask, boolean forceFilter) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
			Property p = null;
			//System.out.println("  getProperty id="+id+"  idProp="+idProp);
			if (ido!=null&&!this.existInMotor(ido)) {
				int niveles=1;
				if(idto==629){
					//TODO quitar este parche para no cargar lineas de rotacion
					System.out.print("Anulando niveles rotacion");
					niveles=-1;
				}
				this.getFromServer(ido,idto, niveles,userRol, user, usertask, false);
			}
			this.checkRangesIdoIdto(ido, idto);
			this.checkIncorrectParamsIdoIdtoWhenIdoInRuler(ido, idto);
			
			// MEJORA RENDIMIENTO SI ES CLASE Y METADATA SE HA CARGADO
			if (ido==null&&this.getMetaData() != null&&this.isIDCompleteClass(idto)) {
				Property pMetaData=this.getMetaData().getClases().get(idto).getHmProperties().get(idProp);
				if(pMetaData!=null){
					p = pMetaData.clone();
					if(p instanceof ObjectProperty){
						//Si se trata de un enumerado actualizados enumList porque pueden haber cambiado. Usamos rangoList(0) porque sabemos seguro que se trata de un idto de clase y no un filtro
						if(((ObjectProperty)p).getRangoList().size()==1&&isSpecialized(((ObjectProperty)p).getRangoList().get(0),Constants.IDTO_ENUMERATED)){
							((ObjectProperty)p).getEnumList().removeAll(((ObjectProperty)p).getEnumList());
							Iterator<Integer> itindv=getIndividualsOfLevel(((ObjectProperty)p).getRangoList().get(0), Constants.LEVEL_INDIVIDUAL).iterator();
							while (itindv.hasNext()){
								ObjectValue ove=new ObjectValue(itindv.next(),((ObjectProperty)p).getRangoList().get(0));
								((ObjectProperty)p).getEnumList().add(ove);
							}	
						}
					}
				}else{
					throw new NotFoundException("La clase:"+getClassName(getClassOf(idto))+ " no tiene la propiedad:"+ this.getPropertyName(idProp));
				}
			} 
			else {
				// Obtenemos todos los facts (instance....) del motor con informaci�n sobre la propiedad idProp en el objeto id
				LinkedList<dynagent.common.knowledge.IPropertyDef> instancefacts;
				try {
					instancefacts=getInstanceFacts(ido!=null?ido:idto, idProp);
					p = getPropertyOf(instancefacts, userRol, user, usertask);
				} 
				catch (NotFoundException e)
				{
					// SI NO ENCUENTRA CARACTER�STICAS DE LA PROPIEDAD Y NO SE TRATA
					// DE UNA CLASE CON LA INFO LA CONSEGUI DE LA CLASE
						if(ido!=null){
							p=this.getProperty(null,idto, idProp, userRol, user, usertask, forceFilter);
							if(p!=null){
								p.setIdo(ido);
							}
						}
					
				}
				if(p==null){
					System.err.println("\n WARNING PROPERTY NULL EN getProperty idto="+idto+"  idProp="+idProp);
					System.err.println("\n ..clase:"+this.getMetaData().getClase(idto)+"\n propiedad:"+this.getMetaData().getHmPropiedades().get(idProp));
					Auxiliar.printCurrentStackTrace();
				}
				//if(enabled){
					access accesstype = getPropertyAccessOf(p, userRol, user, usertask);
					p.setTypeAccess(accesstype);
				//}
					
				if(p instanceof ObjectProperty && forceFilter && p.getIdo()!=null/* && !isIDFilter(p.getIdo())*/){
					Iterator<Integer> itrRange=((LinkedList<Integer>)((ObjectProperty)p).getRangoList().clone()).iterator();
					while(itrRange.hasNext()){
						int idRange=itrRange.next();
						if(!isIDFilter(idRange)){
							//System.err.println("---ANTES CREAR SETRANGE con prop:"+p);
							//int idoRange=setRange(p.getIdo(), p.getIdProp(), idRange);
							int idoRange=getGenericFilter(idRange);
							((ObjectProperty)p).getRangoList().remove(new Integer(idRange));
							((ObjectProperty)p).getRangoList().add(new Integer(idoRange));
	/*						Integer idoRange=getGenericFilter(idRange);
							if(idoRange!=null){
								((ObjectProperty)p).getRangoList().remove(new Integer(idRange));
								((ObjectProperty)p).getRangoList().add(new Integer(idoRange));
								}
	*/
							
							//System.err.println("---DESPUES CREAR SETRANGE con prop:"+p);
						}
					}
				}
			}
			/*if(p!=null&&!user.equals(Constants.USER_SYSTEM)&&((p.getCardMax()!=null&&p.getCardMax()==0)||(p.getTypeAccess()!=null&&!p.getTypeAccess().getViewAccess()))){
				p=null;
				System.out.println("  info: getPRoperty calcula pr="+p+"   que ser� ocultada (null) al usuario"+user);
			}*/

			return p;
		}
	
	
	public boolean isIDFilter(int id) {
		return this.hmLEVELxID.containsKey(id)&& hmLEVELxID.get(id).intValue()==Constants.LEVEL_FILTER;
	}

	public boolean isIDPrototype(int id) {
		return this.hmLEVELxID.containsKey(id)&& hmLEVELxID.get(id).intValue()==Constants.LEVEL_PROTOTYPE;
	}


	
	public boolean isIDIndividual(int id) {
		return this.hmLEVELxID.containsKey(id)&&hmLEVELxID.get(id).intValue()==Constants.LEVEL_INDIVIDUAL;

	}

	public boolean isIDCompleteClass(int id) {
		return this.hmLEVELxID.containsKey(id)&&hmLEVELxID.get(id).intValue()==Constants.LEVEL_MODEL&&this.hmIDOSxIDTO.containsKey(id);
	}


	public boolean isIDClass(int id) {
		return this.hmLEVELxID.containsKey(id)&&hmLEVELxID.get(id).intValue()==Constants.LEVEL_MODEL;
	}

	
	/**
	 * Obtiene los datos de un individuo del servidor y lo carga en motor. La
	 * carga de motor va acompa�ada de la creaci�n de filtros auxiliares.
	 * 
	 * @param ido:
	 *            Identificador del individuo
	 * @param user:
	 *            usuario logado.
	 * @return: int numero de facts a�adidos a motor
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public HashSet<Integer> getFromServer(int id,int idto, int profundidad,Integer userrol,String user,Integer utask) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		//si tiene o no que traer informaci�n estructural se saca de isRunrules
		return this.getFromServer(id, idto, profundidad, userrol, user, utask, SessionController.getInstance().getActualSession(this).isRunRules());
	}
	
	public HashSet<Integer> getFromServer(int id,int idto, int profundidad,Integer userrol,String user,Integer utask,boolean lastStructLevel) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		return this.getFromServer(id, idto,userrol, user, utask,user.equals(Constants.USER_SYSTEM)?false:true,profundidad,false,lastStructLevel,true);
	}
	
	
	

	


	/**
	 * Crea un prototipo de la clase cuyo idto se le pasa como par�metro. Si
	 * esta clase participa en una Relation (puede deducirse a trav�s de la
	 * definici�n de userTask correspondiente) tambi�n se creara un prototipo de
	 * esa Relation y se le asociar�n a las clases tambi�n las propiedades de
	 * rol correspondiente. Por cada objectProperty que se tenga tambi�n creara
	 * un prototipo de la clase a la que apunta. Par�metros: * int idtto:
	 * identificador de la clase de la que se quiere crear el prototipo. * int
	 * userRol: p�rfil con el que est� logado el usuario. * int user: usuario
	 * logado * int userTask: identificador de la tarea en la que se encuentra.
	 * Return: * int: con el id de prototipo que ha creado
	 * 
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 */

	public synchronized Integer createPrototype(int idto, int level, Integer userRol,
			String user, Integer usertask, Session sessionPadre)
	throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Integer re = null;
		DefaultSession sess = new DefaultSession(this, sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
		
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			SessionController.getInstance().setActual(sess,this);
			try{
			re = createPrototype(idto, level, userRol, user, usertask, true);
			// si todo ha ido bien, commit
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			}catch(Exception e){
				System.err.println("DEBUG "+e.toString());
				e.printStackTrace();
			}
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.createPrototype cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.createPrototype no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}

		return re;

	}

	/**
	 * Crea un prototipo de la clase cuyo idto se le pasa como par�metro.
	 *  Por cada objectProperty que se tenga tambi�n creara
	 * un prototipo de la clase a la que apunta. Par�metros: * int idtto:
	 * identificador de la clase de la que se quiere crear el prototipo. * int
	 * userRol: p�rfil con el que est� logado el usuario. * int user: usuario
	 * logado * int userTask: identificador de la tarea en la que se encuentra.
	 * Return: * int: con el id de prototipo que ha creado
	 * 
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */

	public Integer createPrototype(int idto, int level, Integer userRol,String user, Integer usertask, boolean filters) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		boolean allPermited=user.equals(Constants.USER_SYSTEM)||level==Constants.LEVEL_FILTER;
		if(!allPermited && !getAccessOverObject(idto, userRol, user, usertask).getNewAccess()){
			OperationNotPermitedException ex= new OperationNotPermitedException("No tiene permiso para crear "+this.getClassName(idto));
			ex.setUserMessage("No tiene permiso para crear '"+getAliasOfClass(getClassOf(idto), usertask)+"'");
			throw ex;
		}
		
		int levels=filters?Constants.MAX_DEPTH_FILTERS:0;
		
		Integer ido = createPrototype(idto, level, new HashMap<Integer, ArrayList<Integer>>(), userRol, user,usertask,levels);
		return ido;
	}
	
	public QueryXML getQueryXML(){
		return new QueryXML(this);
	}

	/**
	 * completeProperty: Complementa la informaci�n de una property de
	 * individuo, filtro o prototipo no especificada explicitamente para ese
	 * objeto con la informaci�n gen�rica de la clase.
	 * 
	 * 
	 * @param idto
	 * @param pr
	 * @return
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public Property completeProperty(int idto, Property pr, Integer userRol,
			String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {

		//Property prClase = this.getProperty(idto, null, pr.getIdProp(),userRol, user, usertask);
		Property prClase = this.getProperty(null,idto, pr.getIdProp(),userRol, user, usertask, false);

		if (prClase != null) {
			if(prClase.isValuesFixed()/*&&pr.values.size()==0*/){
				pr.setValuesFixed(true);
				pr.setValues(prClase.getValues());
			}
			if (pr instanceof DataProperty) {
				DataProperty dpr = (DataProperty) pr;
				DataProperty dprClase = (DataProperty) prClase;
				if (dpr.getEnumList().size() == 0) {
					dpr.setEnumList(dprClase.getEnumList());
				}
				if (dpr.getExcluList().size() == 0) {
					dpr.setExcluList(dprClase.getExcluList());
				}
				if (dpr.getCardMin() == null) {
					dpr.setCardMin(dprClase.getCardMin());
				}
				if (dpr.getCardMax() == null) {
					dpr.setCardMax(dprClase.getCardMax());
				}

				dpr.setDataType(dprClase.getDataType());

				//pr = dpr;

			} else if (pr instanceof ObjectProperty) {
				ObjectProperty opr = (ObjectProperty) pr;
				ObjectProperty oprClase = (ObjectProperty) prClase;
				if (opr.getEnumList().size() == 0) {
					opr.setEnumList(oprClase.getEnumList());
				}
				if (opr.getExcluList().size() == 0) {
					opr.setExcluList(oprClase.getExcluList());
				}
				if (opr.getRangoList().size() == 0) {
					opr.setRangoList(oprClase.getRangoList());
				}
				if (opr.getCardMin() == null) {
					opr.setCardMin(oprClase.getCardMin());
				}
				if (opr.getCardMax() == null) {
					opr.setCardMax(oprClase.getCardMax());
				}

				//pr = opr;
			}
		}
		return pr;
	}



	/**
	 * Deduce el objeto Property correspondiente a un objeto o tipo de objeto a
	 * partir de los facts (instances...) que hay en motor sobre �l. Tambi�n
	 * obtiene el access correspondiente de esa property en funci�n del contexto
	 * (userRok,user,usertask)
	 * 
	 * @param: LinkedList <dynagent.ruleengine.src.ruler.Fact> lista de
	 *         instances
	 * @param: Integer userRol: identificador num�rico del userRol (puede ser
	 *         null)
	 * @param: String user: login del usuario (puede ser null)
	 * @param: Integer usertask: identificador num�rico del utask (puede ser
	 *         null)
	 * @return: Property: objeto Property.java construido
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public Property getPropertyOf(LinkedList<dynagent.common.knowledge.IPropertyDef> instances,Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		Property pr = null;
		if (instances.size() > 0) {
			dynagent.common.knowledge.IPropertyDef fp = (dynagent.common.knowledge.IPropertyDef) instances.get(0);
			if ((fp.getVALUECLS()!=null && Constants.isDataType(fp.getVALUECLS())) || this.isDataProperty(fp.getPROP())) {
				 FactProp propertyfact=getFactProp(fp.getPROP());
				 if(propertyfact==null){
					 System.err.println("   WARNING: ka.buildDataPropertyOf no encuentra propertyfact para instances="+instances);
				 }
				pr = ka.buildDataPropertyOf(instances,propertyfact.getNAME(),getPropertyMask(fp.getPROP(), fp.getIDTO(), usertask),getPropertyLength(fp.getPROP(), fp.getIDTO(), usertask));
			} else{// if (this.isObjectProperty(fp.getPROP())) {
				pr = ka.buildObjectPropertyOf(instances);
				if(!user.equals(Constants.USER_SYSTEM)){
					ObjectProperty opr=(ObjectProperty)pr;
					if( opr.getIdo()!=null && opr.getRangoList().size()>0){
						int range= opr.getRangoList().getFirst();
						if(isIDClass(range)){
							range=getGenericFilter(range);
							LinkedList<Integer> lrange=new  LinkedList<Integer>();
							lrange.add(range);
							opr.setRangoList(lrange);
						}
					}
				}
			}
			dynagent.common.knowledge.IPropertyDef factIns = (dynagent.common.knowledge.IPropertyDef) instances.get(0);
			Integer idto = factIns.getIDTO();
			Integer ido = factIns.getIDO();
			// Si es un individuo (individuo,filtro , o prototipo) completamos
			// la informaci�n que no se haya construido por no haber facts al
			// respecto con lo gen�rico para esa clase:
			if (ido != null&& !this.isIDCompleteClass(instances.get(0).getIDO())) {
				pr = this.completeProperty(idto, pr, userRol, user, usertask);
			}
			

			if(pr==null)
				System.out.println("  getPropertyOf instances="+instances+"  devuelve null");
			
		}
		return pr;
	}



	/**
	 * Dado un objeto java ruler.fact construye una representaci�n String de los
	 * valores de los slots que se les pasa como par�metro
	 * 
	 * @param slots:
	 *            LinkedList con los nombres de los slots
	 * @param fact:
	 *            objeto fact
	 * @return: representaci�n de los valores en un String.
	 */
	private String getStringRepresentationOfSlotValues(LinkedList<String> slots,dynagent.common.knowledge.IPropertyDef fact) {String result = null, slotvalue = null;
	for (int i = 0; i < slots.size(); i++) {
		if (slots.get(i).equals("IDTO"))
			slotvalue = fact.getIDTO() + "#";
		else if (slots.get(i).equals("IDO"))
			slotvalue = fact.getIDO() + "#";
		else if (slots.get(i).equals("PROP"))
			slotvalue = fact.getPROP() + "#";

		else if (slots.get(i).equals("OP"))
			slotvalue = fact.getOP() + "#";
		else {
			System.out.println("     WARNING:  Caso no implementado en getStringRepresentationOfSlotValues  para el slot="	+ slots.get(i));
		}
		if (result != null)
			result = result + slotvalue;
		else
			result = slotvalue;

	}
	return result;

	}

	/***************************************************************************
	 * isUnit
	 * 
	 * @param valuecls
	 * @return
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException
	 */
	public boolean isUnit(int valuecls) throws IncoherenceInMotorException {
		Iterator it;
		try {
			it = this.getSuperior(valuecls);
		} catch (NotFoundException e) {
			return false;
		}
		boolean flag = false;
		while (it.hasNext() && flag == false) {
			Integer f = (Integer) it.next();
			if (f.intValue() == 6)
				flag = true;
		}
		return flag;
	}



	/**
	 * Modifica, a�ade o elima seg�n la operation deducida un Value a una
	 * Property. La operation que se deducir� ser�: <br>
	 * &nbsp; NEW (si oldValue=null) <br>
	 * &nbsp; DEL (si newValue=null) <br>
	 * &nbsp; SET (si oldValue!=null y newValue!=null) <br>
	 * 
	 * @param: int ido -identificador del objeto
	 * @param: int idProp -identificador de la propiedad
	 * @param Integer
	 *            rol: Identificador (si procede) del rol
	 * @param: Integer idoRel: identificador (si procede) de la relaci�n
	 * @param: Value: viejo valor
	 * @param Value:
	 *            nuevo valor
	 * @param: Integer userRol: identificador del userRol bajo el que se
	 *         encuentra el usuario
	 * @param: String user: nombre(login) del usuario logado
	 * @param: Integer usertask: identificador de la usertask
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws NumberFormatException 
	 * @throws DataErrorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException,
	 *             OperationNotPermitedException,IncompatibleValueException
	 * @throws NotFoundException
	 * @throws ApplicationException 
	 * @throws IncoherenceInMotorException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * 
	 */
	

	public synchronized void setValue(int ido,int idto, int idProp,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal, Integer userRol,
			String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		//System.err.print("\n DEBUG INICIO setValue propiedad: "+this.getPropertyName(idProp)+"  "+Auxiliar.getFechaActual());
		//Auxiliar.beginCrono();
		// TODO: Falta Exclusion mutua
		//System.out.println("DENTRO DE SETVALUE PUBLICO");
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			//System.err.println("ido:"+ido+" idProp:"+idProp+" oldVal:"+oldVal+" newVal:"+newVal);
			SessionController.getInstance().setActual(sess,this);
			setValue(ido, idto,idProp, oldVal, newVal, userRol, user, usertask, false);
			// al final hacer un commit. si falla el commit -> lanzar una
			// excepcion para deshacer lo que estaba haciendo el commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
			
			
			
		}finally{
			if(!success){

				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.setValue cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.setValue no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
			
		}
		//System.err.print("\n DEBUG FIN setValue propiedad: "+this.getPropertyName(idProp)+" MILISECONDS="+Auxiliar.stopCrono()); 

	}

	public void setValue(int ido, int idto,int idProp,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal, Integer userRol,String user, Integer usertask, boolean checkSystemValue)
	throws CardinalityExceedException,OperationNotPermitedException, IncompatibleValueException,NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException {
		if (!this.existInMotor(ido)) {  
			this.getFromServer(ido,idto, 1,userRol, user, usertask);
		}
		Integer level = this.getLevelOf(ido);
		Property pr = null;
		// Este m�todo no est� dise�ado para cambiar los valores de los filtros EXCEPTO Las utask que se est�n tratando temporalmente como filtros
		// los valores de los filtros solo deber�n ser cambiados por reglas, que usar�n user=SYSTEM

		//boolean posibleModify=user.equals(Constants.USER_SYSTEM)||this.isSpecialized(this.getClassOf(ido), Constants.IDTO_UTASK)||(this.getLevelOf(ido)!=Constants.LEVEL_FILTER);

		
		//TODO REVISAR IMPORTANTE: CAMBIO TEMPORAL PARA QUE SE PUEDAN HACER SET A FILTROS SIEMPRE. ESTO SER� QUITADO POSTERIORMENTE CUANDO EN NULEO Y EN SERVER SE INTERPRETEN ADECUADAMENTE
		//LAS RESTRICCIONES 
		boolean allPermited=user.equals(Constants.USER_SYSTEM)||level==Constants.LEVEL_FILTER;
//		if (posibleModify) {
//			int operation = 0;
			pr = this.getProperty(ido,idto, idProp, userRol, user, usertask, false);
			access acc = pr.getTypeAccess();
			
			
			if(!allPermited && !acc.getSetAccess()){
				OperationNotPermitedException ex= new OperationNotPermitedException("No tiene permiso para modificar el valor de la propiedad "+this.getPropertyName(idProp)+" de "+this.getClassName(idto));
				ex.setUserMessage("No tiene permiso para modificar el valor de la propiedad '"+getAliasOfProperty(idto,idProp)+"' de '"+getAliasOfClass(idto, usertask)+"'");
				throw ex;
			}
//			if (newVal == null && oldVal != null) {
//				operation = dynagent.server.application.action.DEL;
//				/*if(!allPermited && !acc.getDelAccess()){
//					throw new OperationNotPermitedException("No tiene permiso para borrar el valor de la propiedad "+this.getPropertyName(idProp),pr);
//				}*/
//
//			} else if (newVal != null && oldVal != null) {
//				operation = dynagent.server.application.action.SET;
//			}
//			if (newVal != null && oldVal == null) {
//				operation = dynagent.server.application.action.NEW;
//			}
			this.setValue(pr, oldVal, newVal, user, checkSystemValue);

/*		}*//* else if (level == Constants.LEVEL_FILTER) {
			throw new OperationNotPermitedException("WARNING: Usuario no puede modificar los valores de un filtro DDM.setValue ido="+ido+" level="+this.getLevelOf(ido)+"  user="+user+"   oldval="+oldVal+"  newVal="+newVal,pr);
		}*/
//		else{
//			System.err.println(" WARNING:DDM.setValue  no hay permiso para modificar ido="+ido+" level="+this.getLevelOf(ido)+"  user="+user+"   oldval="+oldVal+"  newVal="+newVal);
//		}
	}

	/**
	 * Modifica, a�ade o elima (seg�n la operation indicada) un DataValue a una
	 * DataProperty.
	 * 
	 * @param DataProperty:
	 *            property a la que se le har� la operation
	 * @param DataValue:
	 *            valor
	 * @param int
	 *            operation: operaci�n a realizar.
	 * @throws NotFoundException
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 * @throws DataErrorException 
	 */
	
	private void setValue(Property pr,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal,String user,boolean checkSystemValue)
	throws NotFoundException, CardinalityExceedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException {
		//System.err.print("\n DEBUG INICIO setValue propiedad: "+pr.getName()+"  "+Auxiliar.getFechaActual()+"  oldValue="+oldVal+",newVal="+newVal);
		boolean domainIsParam=isSpecialized(pr.getIdto(),getIdClass("PARAMS"));		
		
		if(Auxiliar.equals(oldVal,newVal)){
			//System.out.println("\n\n\n  INFO:  ddm.setValue(oldValue="+oldVal+"newValue="+newVal+"  no har� nada");
			return;
		}
		
		int numeroValores = pr.getNumeroValores();
		int operation=0;
		if (newVal == null && oldVal != null) {
			operation = dynagent.common.knowledge.action.DEL;
		}
		else if (newVal != null && oldVal != null) {
			operation = dynagent.common.knowledge.action.SET;
		}
		else if (newVal != null && oldVal == null) {
			operation = dynagent.common.knowledge.action.NEW;
		}

		//=======================COMPROBACIONES CHECKS=======================

		//ANTES DE HACER UN NEW O UN SET COMPROBAMOS QUE EL VALOR ES
		// COMPATIBLE CON LAS RESTRICCIONES DEL MODELO/Y O REGLAS.

		//TODO SE ESTA PERMITIENDO ASIGNAR MAS VALORES EN UN FILTRO DE LOS QUE DICE SU CARDINALIDAD MAXIMA YA QUE SE USAN TAMBIEN PARA
		//BUSQUEDAS Y DEBE SER PERMITIDO. SIN EMBARGO SI AL FINAL LAS BUSQUEDAS ASIGNAN VALORES CON ENUMLIST NO HARIA FALTA ESTE IF
		
		if(getLevelOf(pr.getIdo())!=Constants.LEVEL_FILTER){
			if(pr.getCardMax()!=null){//COMPROBACION DE QUE NO VIOLA CARDINALIDAD M�XIMA
				boolean violaCardMax=false;
				if(operation == dynagent.common.knowledge.action.NEW&&numeroValores>=pr.getCardMax().intValue()){
					violaCardMax=true;
					System.out.println("Violacion card caso 1");
				}
				else if(operation == dynagent.common.knowledge.action.SET&&numeroValores>pr.getCardMax().intValue()){
					violaCardMax=true;
					System.out.println("Violacion card caso 2");
				}
				if(violaCardMax){
					CardinalityExceedException ceE=new CardinalityExceedException("docDataModel.setValue(oldValue="+oldVal+",newVal="+newVal+", pr="+pr,pr);
					ceE.setUserMessage("No puede a�adir m�s valores a la propiedad "+getAliasOfProperty(pr.getIdto(),pr.getIdProp())); 
					throw ceE;
				}
			}
		}
		
		//Comprobacion de que no se asigna dos veces el mismo valor
		if(newVal!=null && pr.getValues().contains(newVal) && (oldVal==null || !newVal.equals(oldVal))){
			IncompatibleValueException ivE=new IncompatibleValueException ("Ese valor ya existe. docDataModel.setValue(oldValue="+oldVal+",newVal="+newVal+",operation="+operation+"toSystem-->  pr="+pr);
			String value="";
			if(newVal instanceof ObjectValue){
				
				DataProperty prop=(DataProperty)getProperty(((ObjectValue)newVal).getValue(), ((ObjectValue)newVal).getValueCls(),Constants.IdPROP_RDN,null,user,null,false);
				if(prop.getUniqueValue()!=null)
					value=" "+prop.getUniqueValue().getValue_s();
			}else value=" "+newVal.getValue_s();
			ivE.setUserMessage("El valor"+value+" ya existe en el campo "+getAliasOfProperty(pr.getIdto(),pr.getIdProp())+". No puede ser asignado otra vez.");
			throw ivE;
		}
			

		//COMPROBACI�N VALOR CORRECTO (SOLO ES NECESARIO PAR NEW O SET)
		if (operation == dynagent.common.knowledge.action.NEW|| operation == dynagent.common.knowledge.action.SET)
		{
			if(!newVal.checkIsCompatibleWith(pr,this,SessionController.getInstance().getActualSession(this).getUtask())){ 
				IncompatibleValueException ivE=new IncompatibleValueException ("docDataModel.setValue(oldValue="+oldVal+",newVal="+newVal+",operation="+operation+"toSystem-->  pr="+pr);
				ivE.setUserMessage("El valor asignado al campo "+getAliasOfProperty(pr.getIdto(),pr.getIdProp())+" no es valido");
				throw ivE;
			}
		}
		
		String systemValue=null;
		boolean applySystemValue=false;
		if(getLevelOf(pr.getIdo())!=Constants.LEVEL_FILTER){
			int valueCls;
			String valueOldString=null;
			if(pr instanceof DataProperty){
				valueCls=((DataProperty)pr).getDataType();
				if(oldVal!=null)
					valueOldString=oldVal.getValue_s();
			}else{
				if(oldVal!=null){
					valueCls=((ObjectValue)oldVal).getValueCls();
					valueOldString=oldVal.getValue_s();
				}else valueCls=((ObjectValue)newVal).getValueCls();
			}
			
			LinkedList<IPropertyDef> list=getInstanceFacts(pr.getIdo(), pr.getIdProp(), valueOldString, valueCls);
			if(!list.isEmpty()){
				IPropertyDef f=list.getFirst();
				applySystemValue=f.isAppliedSystemValue();
				systemValue=f.getSystemValue();
				if(f.isIncremental()){
					//TODO Descomentar el lanzar la excepcion cuando las reglas gestionen bien el setContribution 
					
					/*IncoherenceInMotorException e=new IncoherenceInMotorException("Intento de hacer un setValue a un fact incremental f:"+f);
					e.setUserMessage("Se ha intentado hacer un cambio de valor a una propiedad que no lo permite");
					throw e;*/
					System.err.println("WARNING:Se ha hecho un setValue a un fact incremental. Si un fact es incremental no debe recibir un setValue directamente. f:"+f);
				}
			}	
		}
		
		//Si es el sistema el que asigna valor siendo el valor igual al valor del sistema no tenemos que hacer nada ya que ese valor ha sido reemplazado por el usuario
		//Es decir, si un usuario cambia un valor del sistema entonces ese cambio permanecera vigente a menos que se le asigne un valor de sistema distinto del que se reemplazo
		boolean isObjPropCardHigher1=(pr instanceof ObjectProperty && !Auxiliar.equals(pr.getCardMax(),1));//Si es objectProperty con cardinalidad>1 no tendra systemValue
		if(!checkSystemValue || /*!user.equals(Constants.USER_SYSTEM) ||*/ /*isObjPropCardHigher1 || systemValue==null*/!applySystemValue || !Auxiliar.equals(systemValue, newVal!=null?newVal.getValue_s():null)){
			
			Integer level=this.getLevelOf(pr.getIdo());
						
			//Si se trata de un individuo tenemos que bloquearlo en base de datos. El metodo de la sesion ya se encarga de comprobar si ya lo tenemos bloqueado de una anterior modificacion
			//Bloqueamos en base de datos antes de hacerlo en motor ya que si no nos permite bloquearlo no hay nada que hacer
			if(getLevelOf(pr.getIdo())==Constants.LEVEL_INDIVIDUAL && !Auxiliar.equals(user, Constants.USER_SYSTEM))
				SessionController.getInstance().getActualSession(this).lockObject(pr.getIdo(),pr.getIdto());
			
			if (operation == dynagent.common.knowledge.action.DEL) {
				dynagent.common.knowledge.FactInstance facttodelete = (FactInstance) ka.traslateValueToFact(pr, oldVal);
				if(user.equals(Constants.USER_SYSTEM)){
					if(checkSystemValue){
						if(!facttodelete.getVALUECLS().equals(Constants.IDTO_MEMO)){
							facttodelete.setSystemValue(null);
							facttodelete.setAppliedSystemValue(true);
						}
					}else{//Si no aplica systemValue borramos el valor que hubiera
						facttodelete.setSystemValue(null);
						facttodelete.setAppliedSystemValue(false);
					}
				}
				ruleEngine.deleteFact(facttodelete,user.equals(Constants.USER_SYSTEM));

				
//novedad impte: Hay que restaurar la l�gica de que si se borra un valor estructural, hay que borrar el objeto valor
				
				if( level!=Constants.LEVEL_FILTER && this.isStructuralExclusive(facttodelete.getIDO(),facttodelete.getPROP(),facttodelete.getVALUE(),facttodelete.getVALUECLS())){
					 //System.err.println("\n  INFO: setValue caso delvalue y prop estructural(!=linea)-->hay que borrar objeto valor: prop="+this.getPropertyName(facttodelete.getPROP())+"  objeto a borrar="+facttodelete.getVALUE());
					 this.deleteObject(Integer.parseInt(facttodelete.getVALUE()), facttodelete.getVALUECLS(),null,null,user,null);
				 }
				
				Integer idpropInv=domainIsParam?null:this.getPropertyInverse(pr.getIdProp());
				if(idpropInv!=null&&this.hasProperty(facttodelete.getVALUECLS(), idpropInv) && level!=Constants.LEVEL_FILTER){//nuevo: comprobamos que el rango no tiene la propiead inversa n
					//TIENE INVERSA+OP DEL--> ADEMAS HAY QUE BORRAR LA INVERSA
					
					dynagent.common.knowledge.FactInstance factinverse=new FactInstance(facttodelete.getVALUECLS(),new Integer(facttodelete.getVALUE()),idpropInv, String.valueOf(facttodelete.getIDO()),facttodelete.getIDTO(),facttodelete.getCLASSNAME(), null,null,null,facttodelete.getRANGENAME());
					if(user.equals(Constants.USER_SYSTEM)){
						if(checkSystemValue){
							if(!factinverse.getVALUECLS().equals(Constants.IDTO_MEMO)){
								factinverse.setSystemValue(null);
								factinverse.setAppliedSystemValue(true);
							}
						}else{//Si no aplica systemValue borramos el valor que hubiera
							factinverse.setSystemValue(null);
							factinverse.setAppliedSystemValue(false);
						}
					}
				
					if(!this.existInMotor(factinverse.getIDO())){
						this.getFromServer(factinverse.getIDO(), factinverse.getIDTO(),1,null, user, null);
					}
					ruleEngine.deleteFact(factinverse,user.equals(Constants.USER_SYSTEM));
					//this.setLock(factinverse.getIDO(), true, user, true);
					//no es el usuario el que esta modificando la inversa sino el sistema (14/7/11)
					this.setLock(factinverse.getIDO(), true, Constants.USER_SYSTEM, true);					
				}
				if(pr instanceof ObjectProperty){
					//si la propiedad es estructural o si el value es un prototipo y no esta apuntado por otro objeto a parte de este hay que borrar el objeto que era su valor
					
					/*if(this.isStructural(pr.getIdProp()) || (facttodelete.getVALUE()!=null && getLevelOf(Integer.valueOf(facttodelete.getVALUE()))==Constants.LEVEL_PROTOTYPE && ruleEngine.getInstanceFactsWhereValueAndValueCls(facttodelete.getVALUE()+"", facttodelete.getVALUECLS()).isEmpty())){
						this.deleteObject(facttodelete.getVALUE(), facttodelete.getVALUECLS());
					}*/
				}
			}
			else if (operation == dynagent.common.knowledge.action.SET) {
				//COMPROBAMOS QUE NO SE EST� INTENTANDO HACER UN SET CON UN VALOR IGUAL AL ANTERIOR
				if(oldVal.equals(newVal)){
					//System.out.println("\n\n\n  INFO:  ddm.setValue(oldValue="+oldVal+"newValue="+newVal+"  no har� nada");
				}
				else{
					//si va  a hacer un set a un objeto (objectProperty), cargamos tambi�n ese individuo en motor para no tener problemas con reglas que usen esa informaci�n:
					if(newVal instanceof ObjectValue && level!=Constants.LEVEL_FILTER){//Importante que no sea filtro ya que si no se cargaria para el borrado y para los setValues de busqueda
	
						int value=((ObjectValue)newVal).getValue().intValue();
						int valuecls=((ObjectValue)newVal).getValueCls();
						//System.err.println("\n\n DEBUGING:   SETVALUE, ivalue="+value+"  VALUE="+newVal);  
	
//if(!this.existInMotor(value)){
//	this.getFromServer(value,valuecls,1, null, user, null);
//		}
					}
					dynagent.common.knowledge.FactInstance facttoset = (dynagent.common.knowledge.FactInstance) ka.traslateValueToFact(pr, oldVal);
					//TIENE INVERSA+OP SET--> ADEMAS HAY QUE HACER EL SET DE LA INVERSA:
					//!ESA L�GICA SE HA ASIGNADO AL METODO  MODIFYWITHVALUE
					//A--p1--->B B--p1'---->A. Al cambiar /A--p1--->B por /A--p1--->C hay que borrar B B--p1'---->A y crear C B--p1'---->A
					
					String valorQueEsSustituido=facttoset.getVALUE();
					int valueClsDevalorQueEsSustituido=facttoset.getVALUECLS();
					int idtovalorSustido=facttoset.getVALUECLS();
					int idoroot=facttoset.getIDO();
					
					if( level!=Constants.LEVEL_FILTER && this.isStructuralExclusive(idoroot,facttoset.getPROP(),valorQueEsSustituido,valueClsDevalorQueEsSustituido)){
						 //System.err.println("\n  INFO:  setValue caso SETVALUE y prop estructural(!=linea)-->hay que borrar objeto valor: prop="+this.getPropertyName(facttoset.getPROP())+"  objeto a borrar="+valorQueEsSustituido);
						 this.deleteObject( Integer.parseInt(valorQueEsSustituido),idtovalorSustido,null,null,user,null);
					 }
					
					
					this.modifyWithValue(facttoset, newVal, user, checkSystemValue && !isObjPropCardHigher1  && !facttoset.getVALUECLS().equals(Constants.IDTO_MEMO), level);
					//si apuntabaEstructuralmenteEn exclusiva a un valor que se sustituye por otro--->hay que borrar el objeto valor
					
				}
			}
			else if (operation == dynagent.common.knowledge.action.NEW) {
				//COMPROBAMOS SI NO ES UN VALOR QUE YA EXISTIA Y SE BORR� DENTRO DE UNA SESI�N
				if(newVal instanceof ObjectValue && level!=Constants.LEVEL_FILTER){//Importante que no sea filtro ya que si no se cargaria para el borrado y para los setValues de busqueda
					int value=((ObjectValue)newVal).getValue().intValue();
					int valuecls=((ObjectValue)newVal).getValueCls();
					//System.err.println("\n\n DEBUGING:   SETVALUE, ivalue="+value+"  VALUE="+newVal);  
//if(!this.existInMotor(value)){
//this.getFromServer(value,valuecls,1, null, user, null);
//}
				}
				dynagent.common.knowledge.FactInstance newfact = (FactInstance) ka.traslateValueToFact(pr, newVal);
				if(user.equals(Constants.USER_SYSTEM) && !isObjPropCardHigher1){
					if(checkSystemValue){
						if(!newfact.getVALUECLS().equals(Constants.IDTO_MEMO)){
							newfact.setSystemValue(newVal.getValue_s());
							newfact.setAppliedSystemValue(true);
						}
					}else{//Si no aplica systemValue borramos el valor que hubiera
						newfact.setSystemValue(null);
						newfact.setAppliedSystemValue(false);
					}
				}
				
				//System.err.println("\nnew fact "+newfact);
				ruleEngine.addNewFactToRuler(newfact,pr.getCardMax(),user.equals(Constants.USER_SYSTEM));
	
				//TIENE INVERSA+OP NEW--> ADEMAS HAY QUE CREAR EL VALOR DE LA INVERSA
				//TODO HACERLO VERRIFICANDO QUE SOLO HAYA UN �NICO VALOR
				Integer idpropInv=domainIsParam?null:this.getPropertyInverse(pr.getIdProp());

				if(idpropInv!=null&&this.hasProperty(newfact.getVALUECLS(), idpropInv) && level!=Constants.LEVEL_FILTER){
					dynagent.common.knowledge.FactInstance newfactInverse=new FactInstance(newfact.getVALUECLS(),new Integer(newfact.getVALUE()),idpropInv,newfact.getIDO().toString(),newfact.getIDTO(),newfact.getCLASSNAME(),null,null,null,newfact.getRANGENAME());
					if(user.equals(Constants.USER_SYSTEM) && !isObjPropCardHigher1){
						if(checkSystemValue){
							if(!newfactInverse.getVALUECLS().equals(Constants.IDTO_MEMO)){
								newfactInverse.setSystemValue(newfact.getIDO().toString());
								newfactInverse.setAppliedSystemValue(true);
							}
						}else{//Si no aplica systemValue borramos el valor que hubiera
							newfactInverse.setSystemValue(null);
							newfactInverse.setAppliedSystemValue(false);
						}
					}
					if(!this.existInMotor(newfactInverse.getIDO())){
						this.getFromServer(newfactInverse.getIDO(), newfactInverse.getIDTO(),1,null, user, null);
					}
					this.addFactToRuler(newfactInverse);
					//this.setLock(newfactInverse.getIDO(), true, user, true);
					//no es el usuario el que esta modificando la inversa sino el sistema (14/7/11)
					this.setLock(newfactInverse.getIDO(), true, Constants.USER_SYSTEM, true);
					//System.err.println("\n...se est� haciendo un new de un valor a una propiedad con inversa. valor="+newfact+"\n...tb se crea el fact inverso="+newfactInverse);
				}	
				//System.err.println("\nproperty    "+getProperty(pr.getIdo(), pr.getIdProp()));
			}
			
			//Bloqueamos en motor despues de haber cambiado el valor ya que si no pueden dispararse reglas antes de tiempo y trabajar sobre unos valores incompletos
			if(level!=null&&level.intValue()!=Constants.LEVEL_FILTER){
				this.setLock(new Integer(pr.getIdo()),true,user,true);
			}
			
			//Actualizamos el mapa de rdns si se trata de esa property
			if(pr.getIdProp()==Constants.IdPROP_RDN && getLevelOf(pr.getIdo())!=Constants.LEVEL_FILTER){
				getHmRdnxIDO().put(pr.getIdo(), newVal!=null?newVal.getValue_s():null);
			}
			
			//TRAS EL SET SIEMPRE HAY QUE HACER UN RUN AL MOTOR
			ruleEngineRun();
		}else{
			//System.err.println("\n**********************************\n debug SETVALUE NO HACE NADA USER="+user+"  systemValue="+systemValue+"\noldValue="+oldVal+"   newVal="+newVal+", pr="+pr);
		}
	}
		
	public void SystemSetValue(Property pr,dynagent.common.properties.values.Value oldVal,dynagent.common.properties.values.Value newVal,boolean checkSystemValue) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, IncompatibleValueException, IncoherenceInMotorException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(pr, oldVal, newVal, Constants.USER_SYSTEM, checkSystemValue);
		
	}
	
	
	private void ruleEngineRun() throws OperationNotPermitedException, InstanceLockedException, IncompatibleValueException, IncoherenceInMotorException, CardinalityExceedException, RemoteSystemException{
		String rulesGroup=SessionController.getInstance().getActualSession(this).getRulesGroup();
		ruleEngineRun(rulesGroup);
	}
	
	public void ruleEngineRun(String group) throws InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException{
		if(SessionController.getInstance().getActualSession(this).isRunRules() && enabledRules){
			
			//Con esto evitamos que se disparen reglas de otros grupos cuando estamos procesando
			//las reglas de antes de envio a base de datos ya que son las ultimas que deben dispararse
			if(group.equals(Constants.BDDRULESGROUP))
				enabledRules=false;
			try{
				ruleEngine.run(group);
			}finally{
				enabledRules=true;
			}
		}
	}


	/**
	 * Modifica el valor de un fact por un nuevo valor. Para saber donde va
	 * guardado cada campo de los distintos value consultar m�todo
	 * KnowledgeAdapter.buildDataValue(FAct)
	 * 
	 * @param fact:
	 *            Fact cuyo valor se quiere modificar
	 * @param newVal:
	 *            valor a asignarle al fact.
	 * @throws NotFoundException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NumberFormatException 
	 * @throws DataErrorException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 */
	public void modifyWithValue(dynagent.common.knowledge.FactInstance fact,dynagent.common.properties.values.Value val,String user,boolean setSystemValue,int level) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		dynagent.common.knowledge.FactInstance f=fact.clone();
		
		boolean hasSetSystemValue=false;
		//Si el valor es asignado por el usuario del sistema lo reflejamos en el fact
		if(user.equals(Constants.USER_SYSTEM)){
			if(setSystemValue){
				f.setSystemValue(val!=null?val.getValue_s():null);
				f.setAppliedSystemValue(true);
			}else{//Si no aplica systemValue borramos el valor que hubiera
				f.setSystemValue(null);
				f.setAppliedSystemValue(false);
			}
			hasSetSystemValue=true;
		}
		
		if (val instanceof ObjectValue) {
			ObjectValue ov=(ObjectValue) val;
			Integer idpropInv=this.getPropertyInverse(fact.getPROP());
			if(idpropInv!=null&&this.hasProperty(fact.getVALUECLS(),idpropInv)  && level!=Constants.LEVEL_FILTER){
				//SI TIENE INVERSAS: A--p1--->B B--p1'---->A. Al cambiar /A--p1--->B por /A--p1--->C hay que borrar B B--p1'---->A y crear C B--p1'---->A
				//borramos el inverso
				int ivalueToDel=Integer.parseInt(fact.getVALUE());
				int nborrados=ruleEngine.deleteFactCond(fact.getVALUECLS(),ivalueToDel,idpropInv, fact.getIDTO(), fact.getIDO(),null, null, new Null(Null.NULL));
				if(nborrados>0){
					if(!this.existInMotor(ivalueToDel)){
						this.getFromServer(ivalueToDel, fact.getVALUECLS(), 1, null, user, null);
					}
					this.setLock(ivalueToDel, true, user, true);
				}
				//creamos el nuevo inverso
				dynagent.common.knowledge.FactInstance newfactInverse=new FactInstance(ov.getValueCls(),new Integer(ov.getValue()),idpropInv,fact.getIDO().toString(),fact.getIDTO(),this.getClassName(fact.getIDTO()),null,null,null,this.getClassName((ov.getValue())));
				// Si el valor es asignado por el usuario del sistema lo reflejamos en el fact
				if(user.equals(Constants.USER_SYSTEM)){
					if(setSystemValue){
						newfactInverse.setSystemValue(fact.getIDO().toString());
						newfactInverse.setAppliedSystemValue(true);
					}else{
						newfactInverse.setSystemValue(null);
						newfactInverse.setAppliedSystemValue(false);
					}
				}
				
				if(!this.existInMotor(newfactInverse.getIDO())){
					this.getFromServer(ivalueToDel, fact.getVALUECLS(), 1, null, user, null);
				}
				this.addFactToRuler(newfactInverse);
				//this.setLock(newfactInverse.getIDO(), true, user, true);
				//no es el usuario el que esta modificando la inversa sino el sistema (14/7/11)
				this.setLock(newfactInverse.getIDO(), true, Constants.USER_SYSTEM, true);
				
			}
			//modificamos su valor
			f.setVALUE(ov.getValue().toString());
			f.setVALUECLS(ov.getValueCls());//Modificamos tambien el valueCls porque podria ser de una clase hermana o hija
			f.setRANGENAME(getClassName(ov.getValueCls()));
			//IMPORTANTE: SI LA PROPIEDAD ES ESTRUCTURAL HAY QUE BORRAR EL VALOR ANTERIOR. TAMBIEN LO BORRAMOS SI ES UN PROTOTIPO Y NO ESTA SIENDO APUNTADO POR OTRO FACT
			
			
			/*if(this.isStructural(fact.getPROP()) || (fact.getVALUE()!=null && getLevelOf(Integer.valueOf(fact.getVALUE()))==Constants.LEVEL_PROTOTYPE && ruleEngine.getInstanceFactsWhereValueAndValueCls(fact.getVALUE(), fact.getVALUECLS()).size()==1)){
				this.deleteObject(fact.getVALUE(), fact.getVALUECLS());
			}*/
			
			
		} else if (val instanceof StringValue) {
			f.setVALUE(((StringValue) val).getValue().toString());
		} else if (val instanceof IntValue) {
			IntValue iv=(IntValue)val;
			if(iv.getValueMax()!=null)
				f.setQMAX(iv.getValueMax().doubleValue());
			else
				f.setQMAX(null);
			if(iv.getValueMin()!=null)
				f.setQMIN(iv.getValueMin().doubleValue());
			else
				f.setQMIN(null);
		} else if (val instanceof DoubleValue) {
			DoubleValue fv=(DoubleValue)val;
			if(fv.getValueMax()!=null)
				f.setQMAX(fv.getValueMax().doubleValue());
			else
				f.setQMAX(null);
			if(fv.getValueMin()!=null)
				f.setQMIN(fv.getValueMin().doubleValue());
			else
				f.setQMIN(null);
		} else if (val instanceof UnitValue) {
			UnitValue uv=(UnitValue)val;
			if(uv.getValueMax()!=null)
				f.setQMAX(uv.getValueMax().doubleValue());
			else
				f.setQMAX(null);
			if(uv.getValueMin()!=null)
				f.setQMIN(uv.getValueMin().doubleValue());
			else
				f.setQMIN(null);
			if(uv.getUnit()!=null)
				f.setVALUECLS(uv.getUnit().intValue());
			else
				f.setVALUECLS(null);
		} else if (val instanceof BooleanValue) {
			BooleanValue bv=(BooleanValue) val;
			Integer ibvalue=null;
			if(bv.getBvalue()==null){
				ibvalue=null;
			}
			else if(bv.getBvalue()){//booleanvalue true
				ibvalue=Constants.ID_BOOLEAN_TRUE;
			}
			else if(!bv.getBvalue()){//booleanvalue false
				ibvalue=Constants.ID_BOOLEAN_FALSE;
			}
			Double q=null;
			if(ibvalue!=null)
				q=ibvalue.doubleValue();
			f.setQMAX(q);
			f.setQMIN(q);
			if(bv.getComment()!=null)
				f.setVALUE(bv.getComment());
			else
				f.setVALUE(null);
			
		} else if (val instanceof TimeValue) {
			TimeValue tv=(TimeValue)val;
			Double qMin=null;
			Double qMax=null;
			if(tv.getRelativeSecondsMin()!=null)
				qMin=tv.getRelativeSecondsMin().doubleValue();
			if(tv.getRelativeSecondsMax()!=null)
				qMax=tv.getRelativeSecondsMax().doubleValue();
			f.setQMAX(qMax);
			f.setQMIN(qMin);
		} else {
			System.out.println(" WARNING: Caso no contemplado en docDataModel.modifyValue(fact="+ fact + "   val=" + val);
		}
		
		String op=val.isEqualToValue()?null:Constants.OP_NEGATION;
		f.setOP(op);
		
		//hacemos el set
		//System.err.println("Fact:"+fact+" f:"+f);
		ruleEngine.setFact(fact, f, hasSetSystemValue);
	}






	public void addIDtoLevel(int id, int level) {
		this.hmLEVELxID.put(id,level);
	}



	public boolean addIDOtoIDTO(int ido,int idto){
		this.hmIDTOxIDO.put(ido, idto);
		if(this.hmIDOSxIDTO.containsKey(idto)){
			this.hmIDOSxIDTO.get(idto).add(ido);
			//System.out.println("  despu�s de addIDOTOidto whit ido="+ido+"  and"+"idto="+idto+"    the hasset of idos is="+this.hmIDOSxIDTO.get(idto));
			return true;
		}
		else{
			return false;
		}
	}

	public void addIDOToIDTOWithLevel(int ido,int idto,int level){
		this.addIDtoLevel(ido, level);
		this.addIDOtoIDTO(ido, idto);
	}




	/**
	 * Crea en motor un prototipo de una clase concreta asign�ndole el level que
	 * se le indica (prototipo o filtro). Devuelve el identificador del objeto
	 * que ha creado. Observaci�n: El m�todo es recursivo porque un filtro puede
	 * tener objetProperties de las que sea necesario tambi�n crear filtros y en
	 * ocasiones podr�a ocurrrir que se entrara en un bucle c�clico, para evitar
	 * esto se va a�adiendo en path cada uno de los identificadores de los
	 * objetos creados y en el momento que se completa un ciclo cerrado se deja
	 * de llamar a la misma funci�n recursivamente una recursi�n infinita.
	 * 
	 * @param idto:
	 *            identificador de la clase de la que se quiere crear un objeto
	 * @param level:
	 *            indica el tipo de objeto a crear (prototipo,filtro)
	 * @param path:
	 *            Lista donde se recogen los identificadores de los objetos
	 *            creados desde que se llamo por primera vez
	 * @return: identificador del prototipo-filtroc creado.
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */

	public Integer createPrototype(int ID, int level,HashMap<Integer, ArrayList<Integer>> path, Integer userRol,String user, Integer usertask,int profundidad)
	throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// System.err.println("\n INICIO createProtoype ID="+ID+"   con level="+level);
		//System.err.println("createPrototype clase:"+id);
		int idto=0;
		Integer ido=null;
		if(!isIDClass(ID)){//si se le pasa un id de filtro o de proto, obtenemos las caracter�sticas de su clase
			idto=this.getClassOf(ID);
			ido=ID;
		}else{
			idto=ID;
		}
		
		boolean llamadarecursiva = false;
		LinkedList<dynagent.common.knowledge.IPropertyDef> newfacts = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		dynagent.common.knowledge.FactInstance enlace;
		Integer idProto = null;
		// System.out.println(" .........BEGIN createPrototype whith params idto="+idto+" path="+path+" level="+level);
		String value = null;
		Integer valueCls = null;
		// le asignamos un identificador num�rico
		idProto = QueryConstants.getIdo(newIdo(level),idto);

		// Si es un prototipo le damos un valor inicial al rdn
		if(level==Constants.LEVEL_PROTOTYPE && hasProperty(idto, Constants.IdPROP_RDN)){
			access access=getPropertyAccessOf(getProperty(ido,idto,Constants.IdPROP_RDN,userRol,user,usertask,false), userRol, user, usertask);
			if(/*!access.getSetAccess() || */indexes.contains(idto)){//Si no tiene permiso set le asignamos valor. Si tiene permiso set no se lo asignamos, asi hacemos que a la fuerza el usuario tenga darle valor
				String classname=this.getClassName(idto);
				String rdnaux=Constants.DEFAULT_RDN_CHAR+idProto+Constants.DEFAULT_RDN_CHAR;
				FactInstance rdnfact=new FactInstance(idto,idProto,Constants.IdPROP_RDN,rdnaux,Constants.IDTO_STRING,Constants.DATA_STRING,null,null,null,classname);
				newfacts.add(rdnfact);
			}else{
				//System.err.println("\n INFO: ddm createPrototype ID:"+ID+"  idto:"+idto+" NO ASIGNA VALOR A RDN pq access:"+access);
				
			}
		}
		
		for (Iterator itP = this.getAllPropertyIterator(ido,idto, userRol, user, usertask, false); itP.hasNext();) {
			Property pr = (Property) itP.next();
			//SI LA CARDINALIDAD M�XIMA COINCIDE CON EL NUMERO DE VALORES DEL ENUMERADO Y ESTOS SON VALORES (NO RANGOS DE DATAPROPERTY), LE DAMOS ESOS VALORES
			if(level!=Constants.LEVEL_FILTER && pr.getCardMin()!=null&&pr.getCardMax()!=null&&pr.getCardMin().intValue()==pr.getCardMax().intValue()&&pr.getEnums().size()==pr.getCardMax().intValue()){
				for(int i=0;i<pr.getEnums().size();i++){
					boolean isValue=true;
					if(this.isDataProperty(pr.getIdProp())){
						Value itemEnumValue=pr.getEnums().get(i);
						if(itemEnumValue instanceof DoubleValue){
							DoubleValue dv=(DoubleValue)itemEnumValue;
							isValue=dv.getValueMin()!=null&&dv.getValueMax()!=null&&dv.getValueMin().doubleValue()==dv.getValueMax().doubleValue();
						}else if(itemEnumValue instanceof IntValue){
							IntValue iv=(IntValue)itemEnumValue;
							isValue=iv.getValueMin()!=null&&iv.getValueMax()!=null&&iv.getValueMin().intValue()==iv.getValueMax().intValue();
						}
							
					}
					if(isValue){
						FactInstance vf=(FactInstance)ka.traslateValueToFact(pr,pr.getEnums().get(i) );
						vf.setIDO(idProto);
						boolean isvalue=(vf.getQMAX()==null&&vf.getQMIN()==null)||(vf.getQMAX()!=null&&vf.getQMIN()!=null&&vf.getQMAX().floatValue()==vf.getQMIN().floatValue());
						if(isvalue){///PARA NO ASGINAR COMO VALOR UN RANGO, EJEMPLO DESCUENTO ENTREO 0 Y 100 0:100
							newfacts.add(vf);
						}
					}
				  }
			}
			
			if ((pr instanceof ObjectProperty/*&& ((ObjectProperty) pr).getEnumList().isEmpty()*/)){
				/*Los enumerados no necesitan filtro para reglas., sin embargo en la aplicaci�n no funciona si no tienen filtro en las consultas query
				 //TODO MACARENA REVISAR SI ES FUNDAMENTAL ESOS FILTROS CUANDO SE TRATA DE ENUMERADOS. NO DEBERIAMOS CREAR FILTROS SI NO SON ESTRICTAMENTE NECESRIOS
				 */
				ObjectProperty opr = (ObjectProperty) pr;
				Integer propInv = this.getPropertyInverse(opr.getIdProp());
				if (opr.getRangoList().size() > 0) {
					LinkedList<Integer> ranges=new LinkedList<Integer>();
					for(int i=0;i<opr.getRangoList().size();i++){
						ranges.add(opr.getRangoList().get(i));
					}
					opr.setRangoList(ranges);
					//ESTABLECEMOS UNA PROFUNDIDAD MAXIMA DE NAVEGACI�N QUE HAY QUE ALCANZAR
					boolean superoMaxProfund=(profundidad<=0);
					if (superoMaxProfund || propInv!=null&&path.containsKey(opr.getIdProp())&& path.get(opr.getIdProp()).contains(profundidad)) {
						if (propInv!=null&&path.containsKey(opr.getIdProp())&& path.get(opr.getIdProp()).contains(profundidad)) {
							//System.out.println(" INVERSA YA TENIDA EN CUENTA  propINv="+propInv);
							//System.out.println("Intenta borrar:"+path+"con idProp:"+opr.getIdProp()+" clase:"+getClassName(id)+"profundidad:"+profundidad);

							//Lo borramos del path para que no influya en otras inversas del arbol
							path.get(opr.getIdProp()).remove(new Integer(profundidad));
							if(path.get(opr.getIdProp()).isEmpty())
								path.remove(opr.getIdProp());
						}

						//System.out.println(" INFO: No se creara filtro de "+valueCls+" desde property="+opr+"\n pq el path="+path);
					} else {
						for (int i = 0; i < opr.getRangoList().size(); i++) {
							//addToPath(path, id, opr.getIdProp());//solo interesa (cuando tenemos el control de profundidad llevar el control de ciclos no deseados en las propiedades inversas.
							valueCls = getClassOf(opr.getRangoList().get(i));
							// importante a�adir al path  el camino que no se tiene que seguir por ser inversa que se tendra en cuenta
							if (propInv != null && Auxiliar.equals(opr.getCardMax(),1)/*Con cardinalidad max 1 ya que es en ese caso cuando lo a�ade este m�todo, mirar abajo*/) {
								//solo en el caso de filtro-->filtro. Proto-filtro no tiene sentido enlazarlos mutuamente 
								if(level==Constants.LEVEL_FILTER){
									//addToPath(path, valueCls, propInv);
									addToPath(path, propInv, new Integer(profundidad-1));//para evitar que en el siguiente nivel navege por la inversa 
								}
							}
							// System.out.println("IDTO:"+idto+"--->VALUECLS:"+valueCls);
							Integer idoRango = createPrototype(valueCls,Constants.LEVEL_FILTER, path, userRol,user, usertask,profundidad-1);
							//System.out.println(" "+idProto+"--->"+idoRango);
							llamadarecursiva = true;
							if (idoRango != null) {
								value = idoRango.toString();
								enlace = new dynagent.common.knowledge.FactInstance(idto, idProto, opr.getIdProp(), value,valueCls, this.getClassName(valueCls),null, null,Constants.OP_UNION, this.getClassName(idto));
								newfacts.add(enlace);// A�adimos el enlace creado
								if (propInv != null  && Auxiliar.equals(opr.getCardMax(),1) && level==Constants.LEVEL_FILTER) {//A�adimos el enlace de la inversa si es card max 1
									//System.out.println("Inverso 1:"+enlace);
									//solo en el caso de filtro-->filtro. Proto-filtro no tiene sentido enlazarlos mutuamente 
									enlace = new dynagent.common.knowledge.FactInstance(valueCls, idoRango, propInv, idProto.toString(), idto, this.getClassName(idto),null, null,Constants.OP_UNION, this.getClassName(valueCls));
									//System.out.println("Inverso 2:"+enlace);
									newfacts.add(enlace);// A�adimos el enlace creado
								}
							}
						}
					}
				}
			}
		}
		registerInfoObject(idProto,idto,level);
		this.addFactsToRuler(newfacts);
		if (!llamadarecursiva) {
			// System.out.println(" fin createPrototype id="+id+" path="+path);

		}
		// 
		///NOVEDAD SI EL ID QUE SE PASA ES UN INDIVIDUO O FILTRO SE ENTIENDE QUE SE QUIERE CREAR UN PROTOTIPO CON LAS CARACTERISTICAS (ENTRE ELLAS VALORES) DE ESE ID
		if(!isIDClass(ID)){
			//System.err.println("\n DEBUG CREATEPROTOTIPE SE HA LLAMADO CON ID QUE NO ES DE CLASE,ID:="+ID+"  se llamar� a coyCommonProperties");
			this.copyCommonProperties(idProto,idto, ido,idto, null, false);
		}
		
//		Bloqueamos el prototipo (SI AUN NO ESTA BLOQUEADO)para que se disparen las reglas que sean necesarias
		if(level==Constants.LEVEL_PROTOTYPE ){
			//nuevo: siempre interesa bloquear un objeto que se crea para que las reglas puedan razonar sobre el   
			/*Individual i = this.ruleEngine.getIndividualFact(idProto);if(!i.getSTATE().equals(Constants.INDIVIDUAL_STATE_LOCK)){*/
				setLock(idProto, true, user, true);
			//}
		}
		return idProto;

	}
	
	
	
//	TODO Tener en cuenta que el systemValue no se esta copiando cuando el value es null. Deberia copiarse ya que el usuario ha podido borrar algo que dijera la regla
	  // Ademas si el value de idoOrigen es null no esta machacando el value de idoDestino. Deberia machacarlo�?. 
	  private void copyCommonProperties(int idoDestino,int idtoDestino,int idoOrigen, int idtoOrigen,LinkedList<String> exceptProperties, boolean consumeChanges)throws NotFoundException, OperationNotPermitedException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException {
		 // System.err.println("\n DEBUG INICIO copyCommonProperties: idoDestino:"+idoDestino+"  idtoDestino:"+idtoDestino+"  idoOrigen:"+idoOrigen+"  idtoOrigen:"+idtoOrigen+"  exceptProperties:"+exceptProperties);
		  /***********************************************************************
		   * Obtenemos todos los ids de las propiedades con valor del individuo que queremos copiar
		   * y los ids de las propiedades de la clase del objeto al que se le van a copiar 
		   */
		  if (!this.existInMotor(idoOrigen)) {
				this.getFromServer(idoOrigen,idtoOrigen, 1,null, this.getUser(), null);
			}
		  
		  HashSet<Integer> propertiesA = this.getIDsExplicitPropertiesOf(idoOrigen);
		  HashSet<Integer> propertiesB = this.getAllIDsPropertiesOfClass(this.getClassOf(idoDestino));
		  /***********************************************************************
		   * Procesamos las dos listas anteriores para obtener los ids comunes,
		   * los viejos y los nuevos que guardaremos en 3 listas (<Integer>) : -
		   * newPropList: las propiedades (identificadores) que est�n en el nuevo
		   * tipo pero no las tiene el objeto - oldPropList: las propiedades
		   * (identif..) que ten�a el objeto pero que no pertecen al nuevo tipo. -
		   * commonPropList: las propiedades (identif.) comunes a los dos tipos.
		   */
		  HashMap<String, ArrayList> listas = Auxiliar.getChangesInArrays(new ArrayList(propertiesA), new ArrayList(propertiesB));
		  Integer level = null;
		  /***********************************************************************
		   * Propiedades comunes: Por cada una de las propiedades comunes le asignamos el valor en esa propieda al objeto 
		   * al que se le est�n copiando los datos 
		   */
		  ArrayList commonPropList = listas.get("commons");

		  if (commonPropList != null) {
			  Iterator itcommon = commonPropList.iterator();
			  while (itcommon.hasNext()) {
				  Integer idProp = (Integer) itcommon.next();
				  String nameprop=getPropertyName(idProp);
				  
				  if(idProp!=Constants.IdPROP_RDN && (exceptProperties==null || !exceptProperties.contains(nameprop)))
				  {
					 // System.err.println("origen:"+idoOrigen+"Copiar la propiedad : "+idProp +":"+this.getPropertyName(idProp) + "al individuo:"+ido_Destino);
					  PropertyValue propertyOrigen=SystemGetPropertyValue(idoOrigen,idtoOrigen, idProp);
					  PropertyValue propertyDestino=this.SystemGetPropertyValue(idoDestino,idtoDestino, idProp);
					  
					  
					  	//System.err.println("\n\n DEBUG DDM.copyCommonProperties. nameprop="+nameprop+"  propertyOrigen:"+propertyOrigen+"  propertyDestino:"+propertyDestino);
					  
					  if(propertyOrigen!=null&&propertyOrigen.getValues().size()>0){//si el origen tiene valores hay que copiarlos al destino
						  //System.err.println("\n\n DEBUG DDM.copyCommonProperties. nameprop="+nameprop+"  hay que copiar valores de origen a destino");							 
						  if(propertyDestino!=null&&propertyDestino.getValues().size()>0){
								//si el destino tiene valores hay que borrarlos y copiarles los del origen
								  //System.err.println("\n\n  **************************************\n**************************** INFO: DDM.COPYCOMMONPROPERTIES va a borrar valores de "+propertyDestino+"  para copiarles los de origen: "+propertyOrigen);
								  this.SystemDelValues(propertyDestino);
								  
							  }
						  //COPIAR VALORES DE ORIGEN A DESTINO
						   Iterator<Value> itValoresOrigen = propertyOrigen.getValues().iterator();
						   while(itValoresOrigen.hasNext())
						   {
							  Value value=itValoresOrigen.next();
							  if(this.getCategory(idProp).isStructural()){ //Si la propiedad es estructural no se puede apuntar al mismo valor, hay que clonarlo
								  Domain domainCopyValue = this.SystemCloneIndividual(((ObjectValue)value).getValue(),((ObjectValue)value).getValueCls(),((ObjectValue)value).getValueCls(),null,consumeChanges);
								  this.SystemSetValue(idoDestino, idtoDestino, idProp, null, new ObjectValue(domainCopyValue));
								  //DE MOMENTO NO SIRVE DE NADA COPIAR SYSTEMVALUE EN VALORES CLONADOS, PQ LA LOGICA DE SYSTEM VALUE EN SETVALUE ES INCOMPLETA EN EL CASO DE PROPIEDADES ESTRUCTURALES
								  //TODO COPIARSYSTEMVALUE SI SE MEJORA LA LOGICA SYSTEM VALUE PARA QUE SOPORTE ESTRUCTURALIDAD
							  }
							  else{//dataproperties y objectPropeties no estructurales
								  this.SystemSetValue(idoDestino, idtoDestino, idProp, null, value);
							  }
							  
							  if(consumeChanges){
								  consumirEventoCambio(new Domain(idoDestino, idtoDestino), nameprop);
							  }
						}
					  }
				  }
			  }
		  }
		  //System.err.println("\n DEBUG FIN copyCommonProperties. mostramos valores del destino");
		  //this.mostrarInfoSobreIdxConsola(idoDestino, idtoDestino, null, "SYSTEM", null);
	  }
	  
	  
	  public void SystemDelValues(PropertyValue p)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		  //System.err.println("\n ---------xxxxxxxxx----- DEBUG INICIO SYSTEMDELVALUES pv="+p);
		  
		  this.setValue(p.getIdo(), p.getIdto(), p.getIdProp(), p.getValues(), null, null, Constants.USER_SYSTEM, null);
	}
	

	private void addToPath(HashMap<Integer, ArrayList<Integer>> path, int idto,int idProp) {

		if (path.containsKey(idto)) {
			ArrayList<Integer> l = path.get(idto);
			if(!l.contains(idProp)){
				l.add(idProp);
			}
		} else {
			ArrayList<Integer> lrangos = new ArrayList<Integer>();
			lrangos.add(idProp);
			path.put(idto, lrangos);
		}
	}

	public void addFactsToRuler(LinkedList<dynagent.common.knowledge.IPropertyDef> facts) throws NotFoundException, IncoherenceInMotorException {
		if (facts.size() > 0) {
			//System.out.println(".......... addFactsToRuler. Se van a a�adir a motor:");
			for (int i = 0; i < facts.size(); i++) {
				//System.out.println(facts.get(i));
				addFactToRuler(facts.get(i));
			}

		}
	}

	public void addFactsToRuler(ArrayList<dynagent.common.knowledge.IPropertyDef> facts) throws NotFoundException, IncoherenceInMotorException {
		if (facts.size() > 0) {
			//System.out.println(".......... addFactsToRuler. Se van a a�adir a motor:");
			for (int i = 0; i < facts.size(); i++) {
				//System.out.println(facts.get(i));
				addFactToRuler(facts.get(i));
			}
		}
	}

	public void addFactToRuler(dynagent.common.knowledge.IPropertyDef facti) throws NotFoundException, IncoherenceInMotorException {
		ruleEngine.insertFact(facti);
	}

	/**
	 * createAllUtaskPrototypes: crea un prototipo de cada usertask
	 * 
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public void createAllUtaskPrototypes(Integer userRol, String user)throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		double inicio,fin,tiempos;
		//System.out.println("\n\n DEBUG BEFORE CREATEALLUTASKPROTIPES MOTORSIZE:"+ruleEngine.getMotorSize());

//		INDIVIDUOS QUE SON DE CLASES TIPO ENUMERADO TIENEN QUE ESTAR CACHEADOS EN MOTOR
		//this.loadAllEnumeratedIndividual(null, user, null);

		//////////////PARA BORRAR LOS FACTS INSTANCE EST�TICOS (ESA INFO YA EST� EN METADATA)
		/*NO BORRAREMOS LA INFO DE LAS CLASES PARA PODER USARLAS DESDE LAS REGLAS
		 * inicio=System.currentTimeMillis();
		int ndeleteds=ruleEngine.deleteFactCondRETRACT(new Null(Null.NOTNULL), new Null(Null.NULL), null, null, null, null, null, null);
		fin=System.currentTimeMillis();
		tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		System.out.println("   createAllUtaskPrototypes borra los facts est�ticos de las clases,  numero instance borrados="+ndeleteds+" se borraron en (s)"+tiempos);
		*/
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		inicio=System.currentTimeMillis();
		HashSet<Integer>  utasks=this.getSpecialized(Constants.IDTO_UTASK);
		//System.err.println(" \n INFO: inicio  createAllUtaskPrototypes n. utasks="+utasks.size());
		
		Iterator<Integer> itallUtasks = utasks.iterator();
		while (itallUtasks.hasNext()) {
			this.createUTaskPrototype(itallUtasks.next(),Constants.LEVEL_FILTER, userRol, user);
		}
		fin=System.currentTimeMillis();
		tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		Date now=new Date(System.currentTimeMillis());
		//System.out.println(" *****FIN CREATEALLUTASKPROTOTYPE "+now+"    Se crearon todos los protos/filtros en (segundos)"+tiempos);
		//System.err.println("\n => FIN CREATEALLUTASKPROTOTYPE "+now+"    Se crearon todos los protos/filtros en (segundos)"+tiempos);
		//System.out.println("\n\n DEBUG AFTER CREATEALLUTASKPROTIPES MOTORSIZE:"+ruleEngine.getMotorSize());
		//System.out.println("\numero profundindad filtros:"+Constants.MAX_DEPTH_FILTERS);
		 
	}
	 
	  public Domain cloneIndividual(int idoOrigen,int idtoOrigen,int idtoDestino,LinkedList<String> excluProperties,Integer userRol,String user, Integer usertask, boolean filters, boolean consumeChanges) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		  if(!this.existInMotor(idoOrigen)){
				  this.getFromServer(idoOrigen, idtoOrigen, 1,userRol, user, usertask);
				 
		  }
		  int idodestino=this.createPrototype(idtoDestino, Constants.LEVEL_PROTOTYPE, userRol, user, usertask, filters);
		  this.copyCommonProperties(idodestino,idtoDestino, idoOrigen,idtoOrigen, excluProperties, consumeChanges);
			return new Domain(idodestino,idtoDestino);
		  }
	  
	  
	  public Domain SystemCloneIndividual(int idoOrigen,int idtoOrigen,int idtoDestino,LinkedList<String> excluProperties, boolean consumeChanges) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		  return this.cloneIndividual(idoOrigen, idtoOrigen,idtoDestino, excluProperties, null, Constants.USER_SYSTEM, null, false, consumeChanges);
	  }
	 

	/**
	 * 
	 * createUTaskPrototype: Crea un prototipo de una usertask
	 * 
	 * @param idtoUtask:
	 *            identificador del tipo de usertask
	 * @param level:
	 *            level que se le asignar�n a los facts que se creen en motor
	 *            relativos a este objeto creado
	 * @param ses
	 * @return: Integer con el identificador (ido) de la ustask creada. Ser�
	 *          nulo si no se llega a crear el prototipo de la usertask.
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */
	public Integer createUTaskPrototype(int idtoUtask, int level,Integer userRol, String user) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		boolean ERRORGRAVE=false;
		//System.out.println("<----BEGIN CREATEUTASKPROTOTYPE IDTOUTASK="+idtoUtask+"("+this.getClassName(idtoUtask));
		double inicio,fin,tiempos;
		inicio=System.currentTimeMillis();
		Integer idoUtask;
		Integer idtoToFilter = null, idPropToFilter = null;
		HashMap<Integer, ArrayList<Integer>> path = new HashMap<Integer, ArrayList<Integer>>();
		LinkedList<dynagent.common.knowledge.IPropertyDef> newfacts = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		String nameUtask = this.getClassName(idtoUtask);
		String nameIdo;

		//System.err.println("\n\n DEBUG createUTaskPrototype idtoUtask:"+idtoUtask);
		boolean isReport=this.isSpecialized(idtoUtask, Constants.IDTO_REPORT);
		boolean isAction=this.isSpecialized(idtoUtask, Constants.IDTO_ACTION);
		// Comprobamos que sea una usertask QUE NO SEA REPORT no es report
		if (this.isSpecialized(idtoUtask, Constants.IDTO_UTASK)) {
			idoUtask = newIdo(level);
			nameIdo = nameUtask;
			// SIEMPRE A�ADIMOS LAS PROPIEDAD LEVEL QUE LE ASIGNA EL LEVEL
			// ESPECIFICADO
			this.addIDtoLevel(idoUtask, level);
			this.addIDOtoIDTO(idoUtask, idtoUtask);
			Iterator itp = this.getAllPropertyIterator(null,idtoUtask, userRol,user, idtoUtask, false);
			while (itp.hasNext()) {
				dynagent.common.properties.Property prop = (dynagent.common.properties.Property) itp.next();
				if(prop.getIdProp().equals(Constants.IdPROP_RDN) && isReport){
					// LE ASIGNAMOS VALOR AL RDN PARA QUE HAYA ALGO EN MOTOR EN BASE A LO QUE PODER DEDUCIR QUE HAY UN FILTRO DE REPORT (RECUERDESE QUE A LOS FILTROS NO SE LES CREA INDIVIDUAL)
					Value valrdn=this.buildDataValue(Constants.IdPROP_RDN,"rdnTmp_"+idoUtask);	
					this.setValue(idoUtask,idtoUtask,Constants.IdPROP_RDN,null,valrdn,userRol,user,idtoUtask,false);
				}else if (prop.getIdProp().equals(Constants.IdPROP_TARGETCLASS) /*|| prop.getIdProp().equals(Constants.IdPROP_PARAMS)*/ && !isReport && !isAction) {
//					idPropToFilter = prop.getIdProp();
//					ObjectProperty pTc = (ObjectProperty) prop;
//					if (pTc.getRangoList().size() == 1) {
//						idtoToFilter = pTc.getRangoList().get(0);
//						String value;
//						if (idtoToFilter != null) {
//							value = this.createPrototype(idtoToFilter,Constants.LEVEL_FILTER, path, userRol, user, idtoUtask,0).toString();
//							dynagent.common.knowledge.IPropertyDef link = new dynagent.common.knowledge.FactInstance(
//									idtoUtask, idoUtask, idPropToFilter, value,
//									idtoToFilter,this.getClassName(idtoToFilter), null, null, Constants.OP_UNION,
//									nameIdo);
//							newfacts.add(link);
//						} else {
//							System.err.println("   WARNING: No se puede crear prototype de la utask="	+ idtoUtask+ "  porque no tiene targetClass ");
//							ERRORGRAVE=true;
//						}
//					} else {
//						System.err.println("   WARNING: Propiedad targetClass que apunta a " +pTc.getRangoList().size()+ "  objetos en el m�todo createUTaskPrototype");
//						if(!this.isSpecialized(idtoUtask, Constants.IDTO_EXPORT))//Estas no tienen targetClass actualmente(16-03-10)
//							ERRORGRAVE=true;
//					}
				} else if (prop.getIdProp().equals(Constants.IdPROP_SOURCECLASS) /*|| prop.getIdProp().equals(Constants.IdPROP_PARAMS)*/ && !isAction) {
//					idPropToFilter = prop.getIdProp();
//					ObjectProperty pTc = (ObjectProperty) prop;
//					if (pTc.getRangoList().size() == 1) {
//						idtoToFilter = pTc.getRangoList().get(0);
//						String value;
//						if (idtoToFilter != null) {
//							value = this.createPrototype(idtoToFilter,Constants.LEVEL_FILTER, path, userRol, user, idtoUtask,0).toString();
//							dynagent.common.knowledge.IPropertyDef link = new dynagent.common.knowledge.FactInstance(
//									idtoUtask, idoUtask, idPropToFilter, value,
//									idtoToFilter,this.getClassName(idtoToFilter), null, null, Constants.OP_UNION,
//									nameIdo);
//							newfacts.add(link);
//						} else {
//							System.err.println(" ==========  WARNING: No se puede crear prototype de la utask="	+ idtoUtask+ "  porque no tiene sourceClass ");
//							ERRORGRAVE=true;
//						}
//					} else {
//						System.err.println("\n\n========   WARNING: Propiedad sourceClass que apunta a " +pTc.getRangoList().size()+ "  objetos en el m�todo createUTaskPrototype");
//					}
				} else if (prop.getIdProp().equals(
						Constants.IdPROP_MYFUNCTIONALAREA)) {
					ObjectProperty myfa = (ObjectProperty) prop;
					for (int i = 0; i < myfa.getEnumList().size(); i++) {
						ObjectValue ov=(ObjectValue)myfa.getEnumList().get(i);
						String value = ov.getValue().toString();
						int valuecls = ((ObjectValue)myfa.getEnumList().get(i)).getValueCls();
						dynagent.common.knowledge.IPropertyDef link = new dynagent.common.knowledge.FactInstance(
								idtoUtask, idoUtask, prop.getIdProp(), value,
								valuecls,this.getClassName(valuecls), null, null, null, nameIdo);
						newfacts.add(link);
						
					}
				} else if (prop.getIdProp().equals(Constants.IdPROP_USERROL)) {
					ObjectProperty userrol = (ObjectProperty) prop;
					for (int i = 0; i < userrol.getEnumList().size(); i++) {
						ObjectValue ov=(ObjectValue)userrol.getEnumList().get(i);
						String value = ov.getValue().toString();
						int valuecls = ov.getValueCls();
						dynagent.common.knowledge.IPropertyDef link = new dynagent.common.knowledge.FactInstance(
								idtoUtask, idoUtask, prop.getIdProp(), value,
								valuecls, this.getClassName(valuecls),null, null, null, nameIdo);
						newfacts.add(link);
					}
				}
			}
		} else {
			System.err.println("   WARNING: " + idtoUtask + "  is not an uTask");
			ERRORGRAVE=true;
			return null;
		}
		this.addFactsToRuler(newfacts);
		fin=System.currentTimeMillis();
		tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		//System.out.println(" *****FIN CREATEUTASKPROTOTYPE IDTOUTASK="+idtoUtask+" Se cargo en (segundos)"+tiempos);
		//ruleEngineRun();
		if(ERRORGRAVE){
			//
			System.err.println("\n\n  ==========ERROR GRAVE EN DDM.createUtaskProptotype con los par�metros: IDTOUTASK="+idtoUtask+"("+this.getClassName(idtoUtask)+")\n ...provocamos excepci�n para obligar a corregir problema");
			int a=1/0;
			
		}
		return idoUtask;

	}

	/**
	 * Obtiene el pr�ximo identificador num�rico para una instant del tipo
	 * indicado en level.
	 * 
	 * @param level:
	 *            entero que representa el nivel del que se quiere crear un
	 *            objeto: 0 model;1 filter;2prototype;3individual
	 * @return: el identificador.
	 */
	public synchronized int newIdo(int level) {
		//return this.prototype--;
		return CreateIdo.getInstance().newIdoPrototype();
	}




	public int specializeIn(int id, int idtoSpecialized) {
		// TODO Auto-generated method stub
		return 0;
	}




	public synchronized instance getTreeObject(int id, Integer userRol, String user,Integer userTask, Session sessionPadre ) 
	throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {

		dynagent.common.knowledge.instance i=null;
		//System.out.println("DENTRO DE getTreeObjectPUBLICO");
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			SessionController.getInstance().setActual(sess,this);
			i= getTreeObject(id,  userRol, user, userTask);
			// al final hacer un commit. si falla el commit -> lanzar una
			// exepcion para deshacer lo que estaba haciendo el commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.getTreeObject cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.getTreeObject no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
		return i;
	}




	


	/*public instance getTreeObject(int ido,boolean buscarServer) throws NotFoundException, IncoherenceInMotorException{
		return this.getTreeObject(ido,null,Constants.USER_SYSTEM ,null,buscarServer);
	}


	public instance getTreeObject(int id, Integer userRol, String user,Integer usertask,boolean buscarServer) throws NotFoundException,IncoherenceInMotorException {

		int idto;
		if (this.isIDCompleteClass(id))
			idto = id;
		else {
			// Si no existe en motor hay que traerlo del servidor SI AS� SE INDICA EN EL PAR�METRO
			if (buscarServer&&this.getLevelOf(id) == null) {
				this.getFromServer(id, userRol, user, usertask);
			}
			idto = this.getClassOf(id);
		}
		dynagent.server.knowledge.instance.instance i = new dynagent.server.knowledge.instance.instance(idto, id);
		addPropertyTreeObject(i, id, userRol, user, usertask,new HashSet<Integer>());
		return i;
	}
	 */

	public instance getTreeObject(int id, Integer userRol, String user,Integer usertask) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {

		return getTreeObject(id, userRol, user, usertask, false);
	}
	
	public instance getTreeObject(int id, Integer userRol, String user,Integer usertask, boolean forceFilters) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {

		int idto;
		if (this.isIDCompleteClass(id))
			idto = id;
		else {
			// Si no existe en motor hay que traerlo del servidor
			/*if (this.getLevelOf(id) == null) {
				this.getFromServer(id, userRol, user, usertask);
			}*/
			idto = this.getClassOf(id);
		}
		dynagent.common.knowledge.instance i = new dynagent.common.knowledge.instance(idto, id);
		addPropertyTreeObject(i, id,idto, userRol, user, usertask,new HashSet<Integer>(),forceFilters);
		return i;
	}

	private void addPropertyTreeObject(instance i, Integer ido,int idto, Integer userRol,String user, Integer userTask,HashSet<Integer> listProcessedChildren, /*int depth, */boolean forceFilters) throws NotFoundException,
	IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		if (ido!=null/*&& depth>0*/) {
			listProcessedChildren.add(ido);
			Iterator<Property> itp = this.getAllPropertyIterator(ido,idto, userRol, user, userTask, forceFilters);///*depth<Constants.MAX_DEPTH_FILTERS?*/true/*:false*/);

			while (itp.hasNext()) {
				Property prop = (Property) itp.next();
				if (prop instanceof ObjectProperty) {

					//RANGOLIST
					LinkedList<Integer> rangoList = ((ObjectProperty) prop).getRangoList();
					Iterator<Integer> itrRangoList = rangoList.iterator();
					while (itrRangoList.hasNext()) {
						int range = itrRangoList.next();
						int idtoRange = this.getClassOf(range);
						/*if (listProcessedChildren.contains(range) && isGenericFilter(range)){//Solo le creamos otro filtro si se trata de uno repetido generico. Solo vendra generico si es el usuario el que llama
							//No nos interesa que se apunte al mismo filtro en varias properties ya que cada uno debe tener su propio filtro. Si no lo tuviera habria problema con las queries y con temas graficos
							rangoList.remove(new Integer(range));
							range=createPrototype(range, Constants.LEVEL_FILTER, userRol, user, userTask, false);
							//apuntamos a ese individuo
						    int valueCls=this.getClassOf(range);
						    IPropertyDef fi=new FactInstance(prop.getIdto(),prop.getIdo(),prop.getIdProp(),""+range,valueCls,this.getClassName(valueCls),null,null,Constants.OP_UNION,this.getClassName(prop.getIdto()));
						   	ruleEngine.insertFact(fi);
							rangoList.add(range);
						}*/
						if (!listProcessedChildren.contains(range))
							addPropertyTreeObject(i, range!=idtoRange?range:null, idtoRange, userRol, user,
									userTask, listProcessedChildren, /*depth-1,*/forceFilters);
					}
					
					if(getLevelOf(ido)!=Constants.LEVEL_FILTER){
						//VALUELIST
						LinkedList<Value> valueList =  prop.getValues();
						Iterator<Value> itrValueList = valueList.iterator();
						while (itrValueList.hasNext()) {
							ObjectValue ov = (ObjectValue)itrValueList.next();
							int value = ov.getValue();
							int valuecls=ov.getValueCls();
							if (!listProcessedChildren.contains(value))
								addPropertyTreeObject(i, value,valuecls, userRol, user,
										userTask, listProcessedChildren, /*depth-1,*/forceFilters);
						}
					}	//ENUMLIST
						LinkedList<ObjectValue> enumList = ((ObjectProperty) prop).getEnumList();
						Iterator<ObjectValue> itrEnumList = enumList.iterator();
						while (itrEnumList.hasNext()) {
							ObjectValue ov = itrEnumList.next();
							int value = ov.getValue();
							int valuecls=ov.getValueCls();
							if (!listProcessedChildren.contains(value))
								addPropertyTreeObject(i, value, valuecls,userRol, user,
										userTask, listProcessedChildren,/*depth-1,*/forceFilters);
						}
					
				}
				if(prop!=null)
					i.addProperty(ido, prop);
				else{
					System.out.println(" WARNING: Property nula en addPropertyTreeObject instance="+i);
				}
			}
		}

	}


	/**
	 * Cambia un individuo/filtro/proto de clase.
	 * @param ido
	 * @param newType
	 * @throws NotFoundException
	 * @throws OperationNotPermitedException
	 * @throws IncoherenceInMotorException
	 * @throws IncompatibleValueException
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws dynagent.common.exceptions.CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws NumberFormatException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	 */public void changeObjectType(Integer ido, String newType)throws NotFoundException, OperationNotPermitedException,IncoherenceInMotorException, IncompatibleValueException, NumberFormatException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 this.changeTypeObjectTo(ido, this.getIdClass(newType), null, Constants.USER_SYSTEM, null);
	 }



	 /***************************************************************************
	  * changeTypeObjectTo
	  * 
	  * @throws NotFoundException
	  * @throws OperationNotPermitedException
	  * @throws IncoherenceInMotorException
	  * @throws IncompatibleValueException 
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws NumberFormatException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 public void changeTypeObjectTo(Integer ido, Integer newIdto,Integer userRol, String user, Integer usertask)throws NotFoundException, OperationNotPermitedException,IncoherenceInMotorException, IncompatibleValueException, NumberFormatException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 if (this.isIDIndividual(ido)) {
			 throw new OperationNotPermitedException("   changeTypeObjetTo(ido="
					 + ido + "   newIdto=" + newIdto
					 + "   ) no har� nada porque se le paso un ido de Individuo");
		 }
		 /***********************************************************************
		  * Obtenemos todos los ids de las propiedacdes est�ticas de: - del
		  * objeto ido oldIdsStaticProperty - de la clase newidto--<newIdsStaticProperty
		  */
		 HashSet<Integer> oldIdsStaticProperty = this.getIDsExplicitPropertiesOf(ido);
		 HashSet<Integer> newIdsStaticProperty = this.getIDsExplicitPropertiesOf(newIdto);
		 /***********************************************************************
		  * Procesamos las dos listas anteriores para obtener los ids comunes,
		  * los viejos y los nuevos que guardaremos en 3 listas (<Integer>) : -
		  * newPropList: las propiedades (identificadores) que est�n en el nuevo
		  * tipo pero no las tiene el objeto - oldPropList: las propiedades
		  * (identif..) que ten�a el objeto pero que no pertecen al nuevo tipo. -
		  * commonPropList: las propiedades (identif.) comunes a los dos tipos.
		  */
		 HashMap<String, ArrayList> listas = Auxiliar.getChangesInArrays(new ArrayList(oldIdsStaticProperty), new ArrayList(newIdsStaticProperty));
		 Integer level = null;

		 /***********************************************************************
		  * 1) Nuevas Propiedades. Recogemos cada uno de los identificadores de
		  * propiedad de la newPropList y por cada uno de ellos: Integer
		  * idProp=newPropList.get(i) - levelObject -<level del objeto ido.
		  * Obtenemos la informaci�n
		  */
		 ArrayList newPropList = listas.get("new");
		 if (newPropList != null) {
			 Iterator itnew = newPropList.iterator();
			 while (itnew.hasNext()) {
				 Integer idProp = (Integer) itnew.next();
				 // Obtener el level del objeto que se apunta con el ido
				 level = this.getLevelOf(ido);
				 /***************************************************************
				  * Obtenemos la informaci�n de esa propiedad en la nueva clase,
				  * esta informaci�n nos la proporciona getPropertyIterator
				  * (newIdto, idProp) (devuelve una lista de
				  * dynagent.api.Property). por cada uno de estos objetos
				  * dynagent.api.Property (al tratarse de una propiedad est�tica
				  * el iterador deber�a devolver un solo elemento Property) hay
				  * que comprobar si es necesario crear un valor de la propiedad
				  * (este valor ser� un filtro) y solo tendra sentido crearlo
				  * cuando se verifican las siguientes 2 condiciones: - se trate
				  * de una objectProperty que tenga como rango a un conjunto o
				  * m�s - el objeto que estamos cambiando de clase sea un
				  * prototipo (prototipo o filtro).
				  */
				 Property p = this.getProperty(null,newIdto, idProp, userRol, user,usertask, false);
				 if (p instanceof ObjectProperty) {
					 if (level != null && level == Constants.LEVEL_FILTER
							 || level == Constants.LEVEL_PROTOTYPE) {
						 ObjectProperty op = (ObjectProperty) p;
						 LinkedList<Integer> rangoprop = op.getRangoList();
						 if (rangoprop.size() > 0) {
							 this.createFilterPropertyValues(newIdto, ido, op,
									 userRol, user, usertask);
						 }
					 }
				 }

			 }
		 }
		 /***********************************************************************
		  * 2) Viejas Propiedades. Recogemos cada uno de los identificadores de
		  * propiedad de oldPropList y por cada uno de ellos borramos sus valores
		  * Integer idProp= oldPropList.get(i). Eliminamos el valor (� valores)
		  * de la propiedad a nuestro objeto. deletePropertyValuesOfIDO
		  * (ido,idProp)
		  */
		 ArrayList oldPropList = listas.get("olds");
		 if (oldPropList != null) {
			 Iterator itold = oldPropList.iterator();
			 while (itold.hasNext()) {
				 Integer idProp = (Integer) itold.next();
				 this.deletePropertyValuesOfIDO(ido, idProp);
			 }
		 }
		 // No debe perder la propiedad level que indica el nivel con el que el
		 // individuo est� en motor.
		 this.addIDtoLevel(ido, level);

		 /***********************************************************************
		  * 3) Propiedades comunes: Recogemos cada uno de los identificadores de
		  * propiedad de commonPropList y por cada uno de ellos: -Integer idProp=
		  * commonPropList.get(i). -Obtenemos el objeto property con la
		  * informaci�n de la property en la nueva clase: Property
		  * prModel=getPropertyIterator(newIdto)// (al tratarse de una propiedad
		  * est�tica el iterador solo devolver� una propiedad)
		  */
		 ArrayList commonPropList = listas.get("commons");
		 if (commonPropList != null) {
			 Iterator itcommon = commonPropList.iterator();
			 while (itcommon.hasNext()) {
				 Integer idProp = (Integer) itcommon.next();
				 this.doPropertyValuesCompatibleWith(ido, idProp, newIdto,userRol, user, usertask);
			 }
		 }
	 }

	 private void doPropertyValuesCompatibleWith(Integer ido, Integer idProp,Integer newIdto, Integer userRol, String user, Integer usertask)
	 throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, NumberFormatException, OperationNotPermitedException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 /***********************************************************************
		  * - Obtenemos la informaci�n sobre las caracter�sticas que debe de
		  * tener los valores de la propiedad para ser coherentes con la nueva
		  * clase newIdto. - Obtenemos todos los facts Instances con valores de
		  * la propiedad en el objeto ido. Por cada uno de estos fact: se
		  * construye un ObjectValue o DataValue a partir del fact para
		  * posteriormente chekear compatibilidad con las caracter�sticas del
		  * modelo. (buildDataValue(fact),buildObjectValue(fact)) Si es
		  * compatible: - Se modifica el fact, cambiando el antiguo idto por
		  * newIDTO Si no es compatible: - Ser� necesario eliminar ese valor.
		  * Previamente a eliminar el fact se comprueba si es necesario eliminar
		  * el objeto que consittuye el valor, si es as� se elimina tambi�n ese
		  * objeto.
		  */
		 Property prModel = this.getProperty(null,newIdto, idProp, userRol, user, usertask, false);

		 Iterator<dynagent.common.knowledge.IPropertyDef> it = this
		 .getFactsPropertyIterator(ido, idProp);
		 while (it.hasNext()) {
			 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it
			 .next();
			 boolean compatible = false;
			 if (this.isDataProperty(idProp)) {
				 DataValue dv = ka.buildDataValue(fact);
				 if (prModel instanceof DataProperty)
					 compatible = dv.checkIsCompatibleWith(prModel,this,usertask);
			 } else {
				 ObjectValue ov = ka.buildObjectValue(fact);
				 if (prModel instanceof ObjectProperty)
					 compatible = ov.checkIsCompatibleWith(prModel,this,usertask);
			 }
			 if (!compatible) {
				 if (this.hasDependentValue(fact)) {
					 this.SystemDeleteObject(Integer.parseInt(fact.getVALUE()),fact.getVALUECLS());
					 this.createFilterPropertyValues(newIdto, ido,
							 (ObjectProperty) prModel, userRol, user, usertask);
				 }
				 ruleEngine.deleteFact(fact,true);
			 } else {
				 // Como el valor es compatible es suficiente con cambiar en el
				 // fact el identificador de
				 // clase que tuviera por el de la nueva clase newIdto
				 //this.modify(fact, "IDTO", newIdto.toString());
				 //this.modify(fact, "NAME", this.getClassName(newIdto) + ido);

				 ruleEngine.setFact(fact, "IDTO", newIdto);

			 }
		 }
	 }

	 /**
	  * WARNING: El borrado de los valores de las properties de un individiduo
	  * puede eliminar al individuo del motor. Decidir si este es es
	  * comportamiento deseado y si no es as� a�adir una comprobaci�n al final
	  * del m�todo
	  * 
	  * @param ido
	  * @param idProp
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException 
	  * @throws NumberFormatException 
	  * @throws OperationNotPermitedException 
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 private void deletePropertyValuesOfIDO(Integer ido, int idProp)
	 throws NotFoundException, IncoherenceInMotorException, NumberFormatException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 /**
		  * Este borrado de valores implica adem�s en el caso de que la propiedad
		  * sea structural o de que tanto el objeto IDO como el objeto que tiene
		  * como valor de la propiedad (idProp) sean filtros o prototipos el
		  * borrado tambi�n del objeto que constituye el valor sea un prototipo
		  * (proto o filtro) la eliminaci�n tambi�n del objeto que constituye el
		  * valor.
		  * 
		  * 1) Obtenemos todos los facts instance where (IDO=ido,PROP=idProp). Y
		  * 2) por cada uno de esos fact comprobamos si el borrado del fact
		  * requiere el borrado de alg�n objeto para mantener el motor coherente.
		  * 
		  * Si es as�: - Se borra el objeto que constituye el valor - Se borra el
		  * fact SI NO: - Se borra el fact.
		  * 
		  * Posible implementaci�n Iterator<�ruler.Fact>
		  * it=this.getFactsPropertyIterator(ido,idProp) For(Iterator
		  * it;it.hasNex();){ Dynagent.ruler.Fact fact=it.next() ; Integer value;
		  * //Solo si value es un entero, caso contrario value se quedar� con el
		  * valor por defecto null; value=new Integer(fact.getValue()); //Antes
		  * de borrar el fact comprobamos si tb es necesari� borrar el objeto que
		  * constituye el valor if(hasDependentValue(fact)){ deleteObject(value);
		  * deleteFact(fact); }
		  */
		 Iterator it = this.getFactsPropertyIterator(ido, idProp);
		 while (it.hasNext()) {
			 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it
			 .next();
			 if (fact.getVALUE() != null
					 && Auxiliar.hasIntValue(fact.getVALUE())) {
				 Integer value = new Integer(fact.getVALUE());
				 if (this.hasDependentValue(fact)) {
					 this.SystemDeleteObject(value,fact.getVALUECLS());
				 }
			 }
			 ruleEngine.deleteFact(fact,true);

		 }
	 }


	 public synchronized void deleteObject(int id, int idto,String rdn,Integer userRol,String user, Integer usertask, Session sessionPadre) throws NotFoundException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
		 DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

		 Session oldSession =SessionController.getInstance().getActualSession(this);
		 boolean success=false;
		 try {
			 SessionController.getInstance().setActual(sess,this);
			 deleteObject(id,idto,rdn,userRol,user, usertask);
			 // al final hacer un commit. si falla el commit -> lanzar una
			 // exepcion para deshacer lo que estaba haciendo el commit.
			 ruleEngineRun();
			 sess.sendPendingLocks();
			 sess.commit();
			 success=true;
		 }finally{
			 if(!success){
				 try{
					 /*System.out.println("Antes de rollback:");
					 getRuleEngine().getMotorSize();*/
					 sess.rollBack();//cancelSession(sess);
					 /*System.out.println("Despues de rollback:");
					 getRuleEngine().getMotorSize();*/
					 System.err.println("WARNING:Sesion interna de DocDataModel.deleteObject cancelada");
				 }catch (Exception e) {
					 System.err.println("WARNING:Sesion interna de DocDataModel.deleteObject no ha podido cancelarse");
					 e.printStackTrace();
				 }finally{
					 SessionController.getInstance().setActual(oldSession,this);
				 }
			 }else SessionController.getInstance().setActual(oldSession,this);
		 }
	 }




	 /**
	  * deleteObject: Elimina un objeto del motor, esta eliminaci�n puede
	  * requerir en algunos casos (propiedades estructurales y filtros) borrar
	  * tambi�n algunos de los objetos con los que est� relacionado.
	  * @deprecated: Utilizece en su lugar deleteObject(id,false);
	  * @param id:
	  *            identificador del objeto
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException 
	  * @throws OperationNotPermitedException 
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 
	 
	 public void deleteObject(int id,int idto,String rdn,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 this.deleteObject(id, idto, rdn, userRol, user, usertask, false);
	 }
	 
	 public void deleteObject(int ido,int idto,String rdn,Integer userRol,String user, Integer usertask,boolean retract) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		// System.err.println("\n DEBUG DELETEOBJECT:  ido="+ido+" ,CLASE:"+this.getClassName(idto)+"  user="+user+"  rdn:"+rdn+" retract:"+retract);
		 HashSet<Integer> listRetractIdo=new HashSet<Integer>();;
		 HashSet<Fact> listRetractFact=new HashSet<Fact>();
		 
			this.checkRangesIdoIdto(ido, idto);

		 
		 //Los retract los hacemos al final para evitar que si alguna regla no permite borrar algo o se
		 //produce algun problema no queden algunos fact ya retractados que serian imposibles de recuperar
		 subDeleteObject(ido, idto, rdn, userRol, user, usertask, retract, listRetractIdo, listRetractFact);
		 
		 Iterator<Fact> itrFact=listRetractFact.iterator();
		 while(itrFact.hasNext()){
			 retractInfoFact(itrFact.next(),true);
		 }
		 Iterator<Integer> itrIdo=listRetractIdo.iterator();
		 while(itrIdo.hasNext()){
			 retractInfoObject(itrIdo.next(),false,true);
		 }
	 }
	 
	 public void subDeleteObject(int id,int idto,String rdn,Integer userRol,String user, Integer usertask,boolean retract,HashSet<Integer> listRetractIdo,HashSet<Fact> listRetractFact) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 //System.err.println("\n...DEBUG subDeleteObject:  id="+id+" ,clase="+this.getClassName(idto)+"  user="+user);
		 ///muy importante solicitar al motor si se puede borrar pq puede haber reglas que impidan borrarlo.
			
		// borramos toda la info directa sobre ese objeto
		 if(!retract && !isIDFilter(id)){
			 deleteInfoObject(id,idto,rdn);
		 }
		 else///se quiere eliminar con retract en ese momento del motor todo lo del objeto
			 listRetractIdo.add(id);//this.retractInfoObject(id,false);
		 
		 //Si se trata de un individuo tenemos que bloquearlo en base de datos. El metodo de la sesion ya se encarga de comprobar si ya lo tenemos bloqueado de una anterior modificacion
		 if((!existInMotor(id) || getLevelOf(id)==Constants.LEVEL_INDIVIDUAL) && !Auxiliar.equals(user, Constants.USER_SYSTEM))//Hay que mirar si existe o no en motor ya que, a no ser que lo cargue una regla, el individuo no esta en motor
			 SessionController.getInstance().getActualSession(this).lockObject(id,idto);
		 
		 
		 if(user.equals(Constants.USER_SYSTEM)){
			 //System.err.println("\n INFO deleteObject: borrado por system id="+id+" , no se calculara permiso sobre �l");
		 }
		 else  if(!this.existInMotor(id)){
			 //System.err.println("\n INFO deleteObject: id="+id+"  no existe en motor, no se calculara permiso sobre �l");
		 }else{
		 	 access accOverobject=this.getAccessOverObject(id, userRol, user, usertask);
			 if(!accOverobject.getDelAccess()){
					//System.err.println("\n\n INFO: se ha llamado a ddm.deleteObject id="+id+"  pero no tiene permiso para borrarlo");
					OperationNotPermitedException op= new OperationNotPermitedException("No se permite borrar el "+this.getClassName(idto)+"  de c�digo="+this.SystemGetPropertyValue(id, idto,Constants.IdPROP_RDN).getValues().getFirst().getValue_s());
					String auxRdn=rdn!=null?" '"+rdn+"'":"";
					op.setUserMessage("No se puede eliminar el "+getAliasOfClass(idto,usertask)+auxRdn);
					throw op;
			 }
		 }
				 
		 HashSet<Integer> ldependientes = new HashSet<Integer>();
		 String idvalue = String.valueOf(id);
		 Integer idtovalue = Integer.valueOf(idto);
		
		 
		

/* 
*PROCESAMOS LOS FACT QUE APUNTAN AL OBJETO ID QUE SE VA A BORRAR: para ver cuales hay que borrar y cuales hay que hacer retract
*
*  A) Fact que apuntan al objeto ID que se va a BORRAR: 
*			- aquellos que apuntan a ID con una inversa a la que ID
*	B) Fact que apuntan al objeto ID y de los que se va  a hacer RETRACT
*/
		 
		 Iterator itApuntado = ruleEngine.getInstanceFactsWhereValueAndValueCls(idvalue, idtovalue).iterator();
		 while (itApuntado.hasNext()) {
			 dynagent.ruleengine.src.ruler.Fact factPointMe = (dynagent.ruleengine.src.ruler.Fact) itApuntado.next();
			 if(retract ||this.isIDFilter(id)){//Si se apunta a un filtro se hace retract
				 listRetractFact.add(factPointMe);
			 }
			 else{
				 /*//Fran: Tiene sentido que si un filtro esta apuntando al objeto a borrar se haga retract de ese fact. Pero si se hace no se enterarian los filtros de busqueda que estan apuntandole. Pensar una solucion
				 if(this.isIDFilter(factPointMe.getIDO()))
					 listRetractFact.add(factPointMe);*/
				 
				 /* COMPROBAR SI ESTO SERIA MAS CORRECTO QUE LO DE ABAJO.
				 Individual ind=this.getRuleEngine().getIndividualFact(factPointMe.getIDO());
				 if(ind!=null&&!ind.getSTATE().equals(Constants.INDIVIDUAL_STATE_DELETED)){
					 ruleEngine.deleteFact(factPointMe,user.equals(Constants.USER_SYSTEM));
					 if(this.hasInverse(factPointMe.getPROP())){
						 Iterator<IPropertyDef> itrInv=ruleEngine.getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls(id, getPropertyInverse(factPointMe.getPROP()), String.valueOf(factPointMe.getIDO()), factPointMe.getIDTO()).iterator();
						 ruleEngine.deleteFact(itrInv.next(),user.equals(Constants.USER_SYSTEM));
					 }
				 }*/
				 Individual ind=this.getRuleEngine().getIndividualFact(factPointMe.getIDO());
				 if(ind!=null&&!ind.getSTATE().equals(Constants.INDIVIDUAL_STATE_DELETED)&&this.hasInverse(factPointMe.getPROP())){  
					 ruleEngine.deleteFact(factPointMe,user.equals(Constants.USER_SYSTEM)); 
			 	 }
			 }
		}
		 
/* 
*PROCESAMOS LOS FACT QUE APUNTAN AL OBJETO ID QUE SE VA A BORRAR: para ver cuales hay que borrar y cuales hay que hacer retract
* A) Los valores dependientes de ID  que tambi�n habr� que borar son: los valores estructurales en exclusiva y los filtros del objeto ID
* B) Si ID es un filtro, se hace retract de todos sus facts
*/
		 Iterator it = ruleEngine.getInstanceFactsWhereIdo(id).iterator();
		 while (it.hasNext()) {
			 dynagent.ruleengine.src.ruler.Fact fact = (dynagent.ruleengine.src.ruler.Fact) it.next();
			 if ( fact.getVALUE() != null	&& Auxiliar.hasIntValue(fact.getVALUE())&& this.isObjectProperty(fact.getPROP())) {
				 Integer ivalue = new Integer(fact.getVALUE());
				 if (this.isIDFilter(ivalue)|| this.isStructuralExclusive(id,fact.getPROP(),fact.getVALUE_s(),fact.getVALUECLS())) {
					 ldependientes.add(ivalue.intValue());
				 }
				 //NUEVO: los filtros si borramos sus valores, para evitar que correlen en otras reglas
				 if(this.isIDFilter(id)){
					 //Realmente no haria falta ya que luego al hacer retractInfoObject sobre el id se borran sus facts tambien.
					 //Pero lo dejo por si en el futuro cambiara esa logica
					 listRetractFact.add(fact);
				 }
			 }
		 }
		 
		 //System.out.println( "    han sido borrados "+nfactsdirectos+"  facts propios del objeto id="+id);
		 //System.out.println( "    sus valores dependientes son="+ldependientes);
		 Iterator<Integer> itdependientes = ldependientes.iterator();
		 // this.listaDeleteds.add(id);

		 while (itdependientes.hasNext()) {
			 int dep = itdependientes.next();
			 // if(!this.listaDeleteds.contains(dep))
			 if(existInMotor(dep) && !listRetractIdo.contains(dep))
				
				 /////TODO IMPORTANTE, OBTENER ANTES LOS IDTOS DE CDA OBJEETO DEPENDIENTE A BORRAR
				 //Hacemos que sea el sistema para no tener problemas de permisos ya que esto tiene que ser borrado
				 this.subDeleteObject(dep,this.getClassOf(dep),null,userRol,Constants.USER_SYSTEM,usertask,retract,listRetractIdo,listRetractFact);
			 // //System.out.println(" ya borrado "+dep);
			 // }
		 }
		
		  //ruleEngineRun();
	 }
	 
	 
	 
	 
	 
	 
	 public boolean hasInverse(int idProp){
		 boolean result=false;
		 Integer posInversa=this.getPropertyInverse(idProp);
		 if(posInversa!=null)
			 result=true;
		 return result;
	 }
	 
	

	 private void createFilterPropertyValues(Integer newIdto, Integer ido,
			 ObjectProperty op, Integer userRol, String user, Integer usertask)
	 throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 /***********************************************************************
		  * - rangoProp=pr.getRangoList(); - Integer valueCls //rango al que se
		  * adscribir� el value de la property
		  * 
		  * if(pr instanceOf ObjectProperty &&rangoProp.size>0) then{
		  * if(rangoProp.size=1) then{ //es un rango simple
		  * -valueCls=rangoProp.get(0) Creamos un prototipo(con
		  * level=levelFilter) de la clase valueCls
		  * :idoRango=createPrototype(valueCls,Constants.levelFilter); } else
		  * if(rangoProp.size>1){ valueCls=Constants.idtoThing;
		  * idoRango=newIdo(levelFilter); } A�adimos al motor un fact Instante
		  * con este nuevo valor de la propiedad. Este fact tendra los valores:
		  * idto=idto; ido=ido; value=idoRango; valueCls=valueCls; el resto de
		  * slots(rol,rolB,idoRel,clsRel) coincidir�n con los de la property:
		  * rol=pr.getRol, rolB=pr.getRolB, idoRel=pr.getIdoRel,
		  * clsRel=pr.getClsRel; } else{ //informar en el log de que en
		  * createFilterPropertyValue no se creo ningun valor de la property
		  * pr.toString()}
		  */
		 LinkedList<Integer> rangoProp = op.getRangoList();
		 Integer valueCls, idoRango;
		 if (op instanceof ObjectProperty && rangoProp.size() > 0) {
			 if (rangoProp.size() == 1) {
				 valueCls = rangoProp.getFirst();
				 idoRango = this.createPrototype(valueCls,Constants.LEVEL_FILTER,new HashMap<Integer, ArrayList<Integer>>(), userRol,user, usertask,0);
			 } else {
				 valueCls = Constants.IDTO_THING;
				 idoRango = this.newIdo(Constants.LEVEL_FILTER);
			 }
			 dynagent.common.knowledge.IPropertyDef newFact = new dynagent.common.knowledge.FactInstance(
					 newIdto, ido, op.getIdProp(), idoRango.toString(),
					 valueCls,this.getClassName(valueCls), null, null, null, this.getClassName(newIdto)
					 + ido);
			 LinkedList<dynagent.common.knowledge.IPropertyDef> newfacts = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
			 newfacts.add(newFact);
			 this.addFactsToRuler(newfacts);
		 } else
			 System.out.println("[WARNING]:" + op.toString());

	 }

	 /**
	  * Obtiene consultando al motor si una propiedad es DataProperty, para ello
	  * Consultara el fact (property (�.idprop)
	  * 
	  * @param: identificador de la propiedad
	  * @return: boolean indicando si es dataProperty o no
	  */
	 public boolean isDataProperty(int idProp) {
		 boolean resultado = false;
		 FactProp fp=this.getFactProp(idProp);
		 if(fp!=null){
			 Category category = new Category(fp.getCAT());
			 if (category.isDataProperty())
				 resultado = true;
			 else
				 resultado = false;
		 }
		 else{
			 resultado=false;
		 }	 			
		 return resultado;
	 }

	 /**
	  * Obtiene consultando al motor si una propiedad es ObjectProperty, para
	  * ello Consultara el fact (property (�.idprop)
	  * 
	  * @param: identificador de la propiedad
	  * @return: boolean indicando si es objectProperty o no
	  */
	 public boolean isObjectProperty(int idProp) {
		 boolean resultado = false;
		 FactProp fp=this.getFactProp(idProp);
		 if(fp!=null){
			 Category category = new Category(fp.getCAT());
			 if (category.isObjectProperty())
				 resultado = true;
			 else
				 resultado = false;
		 }
		 else{
			 resultado=false;
		 }	 			
		 return resultado;
	 }



	 /***************************************************************************
	  * getLevelOf(int ido): Obtiene el level asignado a un objeto
	  * (individuo,filtro, prototipo) con que est� en motor.
	  * 
	  * @param ido:
	  *            identificador del objeto
	  * @return: level del objeto (caso de no tener ningun level o tener m�s de
	  *          uno devuelve nulo y avisa por consola en depuraci�n)
	  */
	 public Integer getLevelOf(int ido) {
		 if(this.hmLEVELxID.containsKey(ido)){
			 return hmLEVELxID.get(ido);
		 }
		 else{
			 //System.out.println("   Info: The object with ido=" + ido+ "   does not exist in motor");
			 return null;
		 }
	 }

	 private boolean isDirectMemberOf(Integer ido, int idto) throws NotFoundException, IncoherenceInMotorException  {
		 if (ido != null) {
			 // Devuelve si el objeto de identificador esta adscrito a la clase
			 // que tiene identificador Idto
			 boolean resultado = false;
			 // LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> instances
			 // = this.getAllInstanceFacts("(instance (IDTO "+idto+")(IDO
			 // "+ido+")))");
			 LinkedList<dynagent.common.knowledge.IPropertyDef> instances = ruleEngine.getAllInstanceFacts(idto, ido, null, null, null, null,null, null, null);
			 if (instances.isEmpty())
				 resultado = false;
			 else
				 resultado = true;
			 return resultado;
		 } else
			 return false;
	 }



	 public boolean hasDependentValue(
			 dynagent.common.knowledge.IPropertyDef fact)
	 throws NotFoundException, IncoherenceInMotorException  {
		 /*
		  * Obtiene si el objeto (value) que constituye el valor de la propiedad
		  * de un objeto es solo dependiente del objeto (ido) del que dice algo
		  * ese fact. De momento consideraremos dependientes los valores de las
		  * propiedades estructurales y los valores que son filtros o prototipos
		  * de un objeto que tambi�n es filtro o prototipo. Posible
		  * implementaci�n: Integer idProp=fact.getProperty(); Si fact.getValue()
		  * tiene un valor entero Integer ivalue=fact.getValue(); boolean
		  * isLikbetweenProtos=isIdPrototype(ido)&&isIdPrototype(ivalue); if
		  * (isStructural(idProp) || isLikbetweenProtos) return true; else return
		  * false
		  * 
		  */
		 Integer idProp = fact.getPROP();
		 Category category = this.getCategory(this.getCat(idProp));
		 if (fact.getVALUE() != null && !this.isDatatype(fact.getVALUECLS())) {
			 Integer ivalue = new Integer(fact.getVALUE());
			 boolean isLikbetweenProtos = this.isIDPrototype(fact
					 .getIDO())
					 && this.isIDPrototype(ivalue);
			 if (this.getCategory(idProp).isStructural()
					 || isLikbetweenProtos)
				 return true;
			 else
				 return false;
		 } else
			 return false;

	 }

	
	 /***************************************************************************
	  * Integer getClassOfObject(Integer ido) Obtiene la clase (idto) a la que
	  * est� adscrito un objeto o la clase de la que amplia informaci�n.
	  * Si recibe un id de clase devuelve la misma.
	  * 
	  * @param ido:
	  *            identificador del objeto/clase
	  * @return: Identificador de su clase. - NULL en caso de de no estar
	  *          adscrito.
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	  * @throws IncoherenceInMotorException 
	  * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	  */
	 public Integer getClassOf(int id){
		 //TODO REVISAR SI DEBE HACERSE AQUI UN GETFROMSERVER, DE MOMENTO ES NECESARIO PORQUE EN SETDEFAULTVALUE EN REGLAS SE HACE SET A VALORES QUE NO EXISTEN EN MOTOR
		 Integer idto=null;
		 /*IMPORTANTE YA NO VAMOS A CARGAR DE SERVIDOR SI NO EST�. DEPURESE LOS POSIBLES PROBLEMAS Q PUEDAN SURGIR DE ESTE CAMBIO
		  * if(!this.existInMotor(id)){
			 int nf=this.getFromServer(id,null,this.getUser(),null);
		 }*/
		 if(this.isIDClass(id))
			 idto=id;
		 else{
			 idto=this.hmIDTOxIDO.get(id);
		 }
		 if(idto==null){
			 System.err.println("\n\nWARNING  GETCLASSOF id="+id+"  devuelve null");
			 Auxiliar.printCurrentStackTrace();
		 }
		 return idto;
	 }
	 
	
	 
	 public Integer getClassOf(String sid) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException  {
		 return this.getClassOf(new Integer(sid));
	 }
	 
	 /**
	  * Devuelve true si el individuo de identificad id es de la clase "clase" o herede de ella 
	  * @param id
	  * @return
	 * @throws OperationNotPermitedException 
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	  */public boolean isOne(int id,String clase) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		  int idto=this.getClassOf(id);
		  int idclassup=this.getIdClass(clase);
		  boolean result=this.isSpecialized(idto, idclassup)||idto==idclassup;
		  return result;
	  }
	  
	  /**
		  * Devuelve true si el individuo de identificad id es de la clase "clase" o herede de ella 
		  * @param id
		  * @return
		 * @throws OperationNotPermitedException 
		 * @throws IncoherenceInMotorException 
		 * @throws CardinalityExceedException 
		 * @throws IncompatibleValueException 
		 * @throws NotFoundException 
		 * @throws ApplicationException 
		 * @throws InstanceLockedException 
		 * @throws CommunicationException 
		 * @throws RemoteSystemException 
		 * @throws SystemException 
		  */
	  public boolean isOne(String sid,String clase) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
			  return this.isOne(new Integer(sid), clase);
		  }
	 
	 
	 
		 public String getNameClassOf(int id) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException  {
			 return this.getClassName(this.getClassOf(id));
		 }
		 
		 public String getNameClassOf(String sid) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException  {
			 return this.getClassName(this.getClassOf(new Integer(sid)));
		 }







	 /**
	  * Obtiene los permisos que tiene el usuario hay sobre las instancias de
	  * cada clase desde cada usertask
	  * 
	  * @param id
	  * @param userRol
	  * @param user
	  * @return: HashMap<Integer,dynagent.knowledge.access> mapa
	  *          usertask-accesso sobre el objeto.
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	  */
	 public HashMap<Integer, dynagent.common.knowledge.access> getAllAccessOverObject(Integer id, Integer userRol, String user) throws NotFoundException,
	 IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		 HashMap<Integer, dynagent.common.knowledge.access> hmObjectAccess = new HashMap<Integer, dynagent.common.knowledge.access>();
		 Iterator<Integer> itallUtasks = this.getSpecialized(Constants.IDTO_UTASK).iterator();
		 while (itallUtasks.hasNext()) {
			 int usertask = itallUtasks.next();
			 dynagent.common.knowledge.access acc = this.getAccessOverObject(id,
					 userRol, user, usertask);
			 hmObjectAccess.put(usertask, acc);
		 }
		 return hmObjectAccess;
	 }

	 /**
	  * Obtiene los permisos que hay sobre un objeto desde las usertask que
	  * actuan directamente sobre ese objeto (targetClass al objeto). La
	  * informaci�n de los permisos que devuelve est� especializada en
	  * userRol-access. Hay que consultar que usertask apuntan en targetClass al
	  * tipo de objeto, y en esas obtener sus accesos sobre el objeto.
	  * 
	  * @param id
	  * @param user
	  * @return
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 public HashMap<Integer, ArrayList<dynagent.common.knowledge.UserAccess>> getUsertaskOperationOver(
			 int id, String user) throws NotFoundException,
			 IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 HashMap<Integer, ArrayList<dynagent.common.knowledge.UserAccess>> hmObjectAccess = new HashMap<Integer, ArrayList<dynagent.common.knowledge.UserAccess>>();
		 Integer idto = null;
		 Iterator<dynagent.common.knowledge.IPropertyDef> it;
		 if (this.isIDCompleteClass(id)) {
			 it = ruleEngine.getAllInstanceFacts(null, null,Constants.IdPROP_TARGETCLASS, id, null, null, null, null, null).iterator();
		 } else {
			 idto = this.getClassOf(id);
			 it = ruleEngine.getAllInstanceFacts(null, null,Constants.IdPROP_TARGETCLASS, idto, null, null, null, null, null).iterator();

		 }
		 while (it.hasNext()) {
			 Integer usertask = it.next().getIDTO();
			 ObjectProperty pUserRol = (ObjectProperty) this.getProperty(null,usertask, Constants.IdPROP_USERROL, null,Constants.USER_SYSTEM, usertask, false);
			 HashSet<String> userRolesUsuario = this.getUserRoles();
			 ArrayList<dynagent.common.knowledge.UserAccess> listauseraccess = new ArrayList<dynagent.common.knowledge.UserAccess>();
			 for (int i = 0; i < pUserRol.getEnumList().size(); i++) {
				 Integer userRol = ((ObjectValue)pUserRol.getEnumList().get(i)).getValue();
				 dynagent.common.knowledge.access acc;
				 // if(userRolesUsuario.contains(userRol)){
				 acc = this.getAccessOverObject(id, userRol, user, usertask);
				 UserAccess usAcc = new UserAccess(userRol, acc);
				 listauseraccess.add(usAcc);
				 // }
			 }
			 hmObjectAccess.put(usertask, listauseraccess);
		 }
		 return hmObjectAccess;
	 }

	 /**
	  * Agrupa de una serie de objetos FactsAcces aquellos que tienen el mismo
	  * valor en su varible PROP y devuelve el resultado de la agrupaci�n en un
	  * mapa PROP-->Lista de FactAccess sobre esa propiedad.
	  * 
	  * @param it
	  *            Iterator<FactAccess>
	  * @return HashMap <Integer,ArrayList<FactAccess>
	  */
	 public HashMap<Integer, ArrayList<FactAccess>> groupByProp(
			 Iterator<FactAccess> it) {
		 HashMap<Integer, ArrayList<FactAccess>> hm = new HashMap<Integer, ArrayList<FactAccess>>();
		 FactAccess fa;
		 ArrayList<FactAccess> lfa;
		 while (it.hasNext()) {
			 fa = (FactAccess) it.next();
			 if (!hm.containsKey(fa.getPROP())) {
				 lfa = new ArrayList<FactAccess>();
				 lfa.add(fa);
				 hm.put(fa.getPROP(), lfa);
			 } else {
				 lfa = hm.get(fa.getPROP());
				 lfa.add(fa);
				 hm.put(fa.getPROP(), lfa);
			 }
		 }
		 return hm;
	 }

	 /**
	  * Agrupa de una serie de objetos FactsAcces aquellos que tienen el mismo
	  * valor en su variable ACCESSTYPE y devuelve el resultado de la agrupaci�n
	  * en un mapa PROP-->Lista de FactAccess sobre esa propiedad.
	  * 
	  * @param it
	  *            Iterator<FactAccess>
	  * @return HashMap <Integer,ArrayList<FactAccess>
	  */
	 public HashMap<Integer, ArrayList<FactAccess>> groupByAccessType(
			 Iterator<FactAccess> it) {
		 HashMap<Integer, ArrayList<FactAccess>> hm = new HashMap<Integer, ArrayList<FactAccess>>();
		 FactAccess fa;
		 ArrayList<FactAccess> lfa;
		 while (it.hasNext()) {
			 fa = (FactAccess) it.next();
			 if (!hm.containsKey(fa.getACCESSTYPE())) {
				 lfa = new ArrayList<FactAccess>();
				 lfa.add(fa);
				 hm.put(fa.getACCESSTYPE(), lfa);
			 } else {
				 lfa = hm.get(fa.getACCESSTYPE());
				 lfa.add(fa);
				 hm.put(fa.getACCESSTYPE(), lfa);
			 }
		 }
		 return hm;
	 }

	 public dynagent.common.knowledge.access getAccessOverObject(Integer id,Integer userRol, String user, Integer usertask)throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException {
		 dynagent.common.knowledge.access acc = null;
		 Integer idto = null;
		 Integer ido = null;
		 if (this.isIDCompleteClass(id)) {
			 idto = id;
		 } 
		 else if(!this.existInMotor(id)){
			 Auxiliar.printCurrentStackTrace();
			 System.err.println("\n\n  WARNING: LLAMADA A ddm.getAccessOverObject id="+id+" de un objeto que no esta en motor, no puede calcularle por tanto el permiso");
		 }
		 else{
			 ido = id;
			 idto = this.getClassOf(ido);
		 }
		 
		 Iterator<FactAccess> itFactAccess = ruleEngine.getAccessFactsOverObject(idto,ido, userRol, user, usertask).iterator();
		 HashMap<Integer, ArrayList<FactAccess>> hmPropXfactsAccess = this.groupByProp(itFactAccess);
		 Iterator itP = hmPropXfactsAccess.keySet().iterator();

		 /*
		  * Deducimos el permiso resultante: - VIEW y SET SE DEDUCIR�N SI LO
		  * TIENEN EN ALGUNA DE SUS PROPIEDADES ( O EN TODAS) - NEW, DEL, CONCRT
		  * SE DEDUCIR�N SI EXPLICITAMENTE EXISTE CON IDPROP=NULL ESOS PERMISOS
		  */
		 boolean someVIEW = false;
		 boolean someSET = false;
		 dynagent.common.knowledge.access accp = null;
		 boolean exit=false;
		 int sum_accesstypes = 0;
		 while (!exit && itP.hasNext()) {
			 Integer prop = (Integer) itP.next();
			 ArrayList<FactAccess> lfa = hmPropXfactsAccess.get(prop);
			 accp = this.deduceAccess(lfa.iterator());
			 if(prop!=null){
				//QUITADA LA LOGICA DE SI UNA PROPERTY TIENE PERMISO DE VIEW O SET SU DOMINIO YA TIENE VIEW O SET
//				 if(enabled && hasProperty(idto, prop)){
//					 if (accp.getViewAccess() && hasAddAccessByProperty(hmPropXfactsAccess.get(null), lfa, access.VIEW)) {
//						 someVIEW = true;
//						 if(someSET)
//							 exit=true;
//					 }
//					 if (accp.getSetAccess() && hasAddAccessByProperty(hmPropXfactsAccess.get(null), lfa, access.SET)) {
//						 someSET = true;
//						 if(someVIEW)
//							 exit=true;
//					 }
//				 }
			 }else{
				 sum_accesstypes = accp.getOperation();
				 if(accp.getSetAccess() && accp.getViewAccess())
					 exit=true;//No nos hace falta mirar si alguna de sus properties tiene set o view ya que ya lo tiene
			 }
		 }

		 acc = new access(sum_accesstypes);
		 //TODO Permisos FALSEADOS. ESTO HAY QUE QUITARLO!!!!!!!!!!!!!!!!!!!!!!!!!!!
		 //WARNAING FALSEO
		 //todo quitar falseo permisos
		 //acc = new access(access.DEL+access.VIEW+access.SET+access.NEW+access.REL+(new access(sum_accesstypes).matches(access.ABSTRACT)?access.ABSTRACT:0));
		 if (someVIEW && !acc.getViewAccess()) {
			 acc.setViewAccess(true);
		 }
		 if (someSET && !acc.getSetAccess()) {
			 acc.setSetAccess(true);
		 }
		 return acc;
		
	 }
	 
	 /*Comprueba que en la lista de accesos para la property haya un acceso de prioridad mayor a un acceso denegado que exista en la lista de accesos del objeto.
	 En ese caso el metodo que haya hecho la llamada tiene que a�adir ese nuevo acceso como acceso del objeto*/
	 private boolean hasAddAccessByProperty(ArrayList<FactAccess> listFactAccessObject,ArrayList<FactAccess> listFactAccessProperty,int access){
		 Iterator<FactAccess> itrObject=listFactAccessObject.iterator();
		 int maxPriorityObject=-1;
		 while(itrObject.hasNext()){
			 FactAccess f=itrObject.next();
			 if(f.getACCESSTYPE().equals(access) && f.getDENNIED().equals(1))
				 maxPriorityObject=Math.max(f.getPRIORITY(),maxPriorityObject);
		 }
		 
		 Iterator<FactAccess> itrProperty=listFactAccessProperty.iterator();
		 int maxPriorityProperty=-1;
		 while(itrProperty.hasNext()){
			 FactAccess f=itrProperty.next();
			 if(f.getACCESSTYPE().equals(access) && f.getDENNIED().equals(0))
				 maxPriorityProperty=Math.max(f.getPRIORITY(),maxPriorityProperty);
		 }
		 
		 if(maxPriorityProperty>maxPriorityObject)
			 return true;
		 return false;
	 }
	 
	 
	 public dynagent.common.knowledge.access getUserAccessOverObject(Integer id)throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, 
	 RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException {
		 return this.getAccessOverObject(id,null,this.getUser(),this.getActualUtask());
	 }
	 
	 
	 public dynagent.common.knowledge.access getUserAccessOverObject(String sid)throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException {
		 return this.getUserAccessOverObject(new Integer(sid));
	 }
	 

	 public void mostrarValoresDeID(Integer ido,int idto) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		 Iterator<PropertyValue> it=this.getAllPropertiesValues(ido,idto);
		 if (ido==null) {
			 System.out.println("La clase con idto=" + idto);
		 } 
		 else if (this.isIDFilter(ido)) {
			 System.out.println("El filtro con ido=" + ido);
		 } else if (this.isIDPrototype(ido)) {
			 System.out.println("El prototipo con ido=" + ido);
		 } else if (this.isIDIndividual(ido)) {
			 System.out.println("El individuo con ido=" + ido);
		 }
		 while(it.hasNext()){
			 System.out.println(it.next());
		 }
	 }
	 
	 public void mostrarInfoSobreIdxConsola(Integer ido,int idto, Integer userRol, String user,Integer usertask) throws NotFoundException,
	 IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 Iterator itp = this.getAllPropertyIterator(ido,idto,userRol, user, usertask, false);
		 
		 if(ido==null){
			 System.err.println("La clase con idto=" + idto);
		 }
		 else if (this.isIDFilter(ido)) {
			 System.err.println("El filtro con id=" + ido);
		 } else if (this.isIDPrototype(ido)) {
			 System.err.println("El prototipo con id=" + ido);
		 }
		  else if (this.isIDIndividual(ido)) {
			 System.err.println("El individuo con id=" + ido);
		 }
		 System.err.println("    tiene las propiedades:");
		 while (itp.hasNext()) {
			 Property pr = (Property) itp.next();
			 System.err.println("     " + pr);
		 }
	 }
	 
	 
	 public void mostrarInfoSobreIdRecursive(Integer ido,int idto, Integer userRol, String user,Integer usertask) throws NotFoundException,
	 IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 HashSet<Integer> lvalues = new HashSet<Integer>();
		 HashSet<Integer> lfiltros = new HashSet<Integer>();
		 Iterator itp = this.getAllPropertyIterator(ido,idto, userRol, user, usertask, false);
		
		if (ido==null) {
			 System.err.println("La clase con idto=" + idto);
		 } 
		else if (this.isIDFilter(ido)) {
			 System.err.println("El filtro con id=" + ido);
		 } else if (this.isIDPrototype(ido)) {
			 System.err.println("El prototipo con id=" + ido);
		 } else if (this.isIDIndividual(ido)) {
			 System.err.println("El individuo con id=" + ido);
		 }
		 System.out.println("    tiene las propiedades:");
		 while (itp.hasNext()) {
			 Property pr = (Property) itp.next();
			 System.err.println("     " + pr);
			 if (pr instanceof ObjectProperty
					 && this.getPropertyInverse(pr.getIdProp()) == null) { // para evitar recursi�n infinita excluimos volver dsde la inversa
				 ObjectProperty opr = (ObjectProperty) pr;
				 //OBTENEMOS LOS FILTROS 
				 if (opr.getRangoList().size() > 0) {
					 for (int j = 0; j < opr.getRangoList().size(); j++) {
						 Integer levelvalue=this.getLevelOf(opr.getRangoList().get(j));
						 if (opr.getRangoList().get(j) != null&&levelvalue!=null&&levelvalue.intValue()==Constants.LEVEL_FILTER) {
							 lfiltros.add(opr.getRangoList().get(j));
						 }
					 }
				 }
				 if (opr.getValues().size() > 0) {
					 for (int i = 0; i < opr.getValues().size(); i++) {
						 if (((ObjectValue)opr.getValues().get(i)).getValue() != null) {
							 lvalues.add(((ObjectValue)opr.getValues().get(i)).getValue());
						 }
					 }
				 }

			 }
		 }
		 // imprimimos la informaci�n sobre los valores y filtros que tiene
		 Iterator<Integer> itfiltros = lfiltros.iterator();
		 Iterator<Integer> itvalues = lvalues.iterator();


		 while (itfiltros.hasNext()) {
			 int idofiltro=itfiltros.next();
			 int idtofiltro=this.getClassOf(idofiltro);
			 this.mostrarInfoSobreIdxConsola(idofiltro,idtofiltro, userRol, user, usertask);
		 }
		 while (itvalues.hasNext()) {
			 int ivalue=itvalues.next();
			 int idtovalue=this.getClassOf(ivalue);
			 this.mostrarInfoSobreIdxConsola(ivalue, idtovalue,userRol, user, usertask);
		 }
		 /*
		  * while(itrangos.hasNext()){
		  * this.mostrarInfoSobreId(itrangos.next(),userRol,user,usertask); }
		  */
	 }




	 /**
	  * METODO MOSTRAR INFO PARA SER USADO POR EL SISTEMA.
	  * @param id
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 public void mostrarInfoSobreId(Integer ido,int idto) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 this.mostrarInfoSobreIdxConsola(ido,idto, null, Constants.USER_SYSTEM, null);
	 }
	 
	 
	 	 /**
	  * Construye una lista con todos los enlaces (objectProperties) que hay en
	  * motor entre prototipos y/o filtros. Cada enlace se guarda en un vector
	  * donde su primer elemento tiene el origen y el segundo el destino.
	  * 
	  * @return: LinkedList<Vector> lista de todos los enlaces entre prototipos
	  *          y/o filtros.
	  * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	  */
	 public LinkedList<Vector> getLinksBetweenIdos() throws NotFoundException, IncoherenceInMotorException {

		 LinkedList<Vector> links = new LinkedList<Vector>();

		 Iterator it = ruleEngine.getAllInstanceFacts(null, new Null(Null.NOTNULL), null, null, new Null(Null.NOTNULL), null, null,null, null).iterator();
		 while (it.hasNext()) {
			 dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it
			 .next();

			 Vector<Integer> enlace = new Vector<Integer>();
			 enlace.setSize(2);
			 if (this.isObjectProperty(f.getPROP())) {
				 enlace.set(0, f.getIDO());
				 enlace.set(1, new Integer(f.getVALUE()));
				 links.add(enlace);
			 }
		 }
		 return links;
	 }

	 /**
	  * Construye una lista con todos los enlaces (objectProperties) que hay en
	  * motor entre prototipos y/o filtros. Cada enlace se guarda en un vector
	  * donde su primer elemento tiene el origen y el segundo el destino.
	  * 
	  * @return: LinkedList<Vector> lista de todos los enlaces entre prototipos
	  *          y/o filtros.
	  * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	  */
	 public LinkedList<Vector> getLinksBetweenIdtos() throws NotFoundException, IncoherenceInMotorException {
		 LinkedList<Vector> links = new LinkedList<Vector>();
		 LinkedList<IPropertyDef>  lfacts=ruleEngine.getAllInstanceFacts(new Null(Null.NOTNULL),null, null, new Null(Null.NOTNULL), null, null, null, null, null);
		 //System.out.println("    getLinksBetweenIdtos encuentra:\n"+lfacts);
		 Iterator it = lfacts.iterator();
		 while (it.hasNext()) {
			 dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it.next();
			 Vector<Integer> enlace = new Vector<Integer>();
			 enlace.setSize(2);
			 if (this.isObjectProperty(f.getPROP())) {
				 enlace.set(0, f.getIDTO());
				 enlace.set(1, new Integer(f.getVALUECLS()));
				 links.add(enlace);
			 }
		 }
		 return links;
	 }
	 
	 public LinkedList<Vector> getLinksNameBetweenIdtos()throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {
		 LinkedList<Vector> links = new LinkedList<Vector>();
		 Iterator it =ruleEngine.getAllInstanceFacts(new Null(Null.NOTNULL),null, null, new Null(Null.NOTNULL), null, null, null, null, null).iterator();
		 while (it.hasNext()) {
			 dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) it.next();
			 Vector<String> enlace = new Vector<String>();
			 enlace.setSize(2);
			 if (this.isObjectProperty(f.getPROP())) {
				 enlace.set(0, this.getClassName(f.getIDTO()));
				 enlace.set(1, this.getClassName(f.getVALUECLS()));
				 links.add(enlace);
			 }
		 }
		 return links;
	 }

	 /**
	  * Construye e imprime por consola el arbol de relaciones creados al crear
	  * los prototipos y los filtros.
	  * 
	  * @param links
	  */

	 public void printLinks(LinkedList<Vector> links) {
		 HashSet<Integer> inicios;
		 HashSet<Integer> destinos;
		 HashSet<Integer> beginLinks = new HashSet<Integer>();
		 HashSet<Integer> endLinks = new HashSet<Integer>();
		 HashMap<Integer, HashSet<Integer>> hmLinksFrom = new HashMap<Integer, HashSet<Integer>>();
		 HashMap<Integer, HashSet<Integer>> hmLinksTo = new HashMap<Integer, HashSet<Integer>>();
		 for (int i = 0; i < links.size(); i++) {
			 Vector link = links.get(i);
			 Integer x = (Integer) link.get(0);
			 Integer y = (Integer) link.get(1);
			 beginLinks.add(x);
			 endLinks.add(y);
			 if (hmLinksFrom.get(x) == null) {
				 destinos = new HashSet<Integer>();
				 destinos.add(y);
				 hmLinksFrom.put(x, destinos);
			 } else {
				 destinos = hmLinksFrom.get(x);
				 destinos.add(y);
				 hmLinksFrom.put(x, destinos);
			 }
			 if (hmLinksTo.get(y) == null) {
				 inicios = new HashSet<Integer>();
				 inicios.add(x);
				 hmLinksTo.put(y, inicios);
			 } else {
				 inicios = hmLinksTo.get(y);
				 inicios.add(x);
				 hmLinksTo.put(y, inicios);
			 }
		 }
		 System.out.println("beginLinks=" + beginLinks);
		 System.out.println("endLinks=" + endLinks);
		 System.out.println("hmLinksFrom=" + hmLinksFrom);
		 System.out.println("hmLinksTo=" + hmLinksTo);
	 }

	 public void printLinks2(LinkedList<Vector> links) {
		 HashSet inicios;
		 HashSet destinos;
		 HashSet beginLinks = new HashSet();
		 HashSet<Object> endLinks = new HashSet<Object>();
		 HashMap<Object, HashSet<Object>> hmLinksFrom = new HashMap<Object, HashSet<Object>>();
		 HashMap<Object, HashSet<Object>> hmLinksTo = new HashMap<Object, HashSet<Object>>();
		 for (int i = 0; i < links.size(); i++) {
			 Vector link = links.get(i);
			 Object x = (Object) link.get(0);
			 Object y = (Object) link.get(1);
			 beginLinks.add(x);
			 endLinks.add(y);
			 if (hmLinksFrom.get(x) == null) {
				 destinos = new HashSet();
				 destinos.add(y);
				 hmLinksFrom.put(x, destinos);
			 } else {
				 destinos = hmLinksFrom.get(x);
				 destinos.add(y);
				 hmLinksFrom.put(x, destinos);
			 }
			 if (hmLinksTo.get(y) == null) {
				 inicios = new HashSet<Object>();
				 inicios.add(x);
				 hmLinksTo.put(y, inicios);
			 } else {
				 inicios = hmLinksTo.get(y);
				 inicios.add(x);
				 hmLinksTo.put(y, inicios);
			 }
		 }
		 System.out.println("beginLinks=" + beginLinks);
		 System.out.println("endLinks=" + endLinks);
		 System.out.println("hmLinksFrom=" + hmLinksFrom);
		 System.out.println("hmLinksTo=" + hmLinksTo);
		 ArrayList<String> sramas = new ArrayList<String>();
		 System.out.println(Auxiliar.hashMapToString(hmLinksFrom, "LINK:",
		 "----->"));
		 Iterator<Object> it = hmLinksFrom.keySet().iterator();

	 }

	 public DocDataModel(docServer ds) {
		 super();
		 this.server = ds;
	 }

	 public boolean existInMotor(Integer id) {
		 boolean result=false;
		 
		 if(this.hmLEVELxID.containsKey(id))
			 result=true;
		 else result= false;
		 //System.err.println("\n DEBUG existInMotor IDO:"+id+" return: "+result);
		 return result;
	 }
	 
	 public boolean existInMotor(String sid) {
		 return this.existInMotor(new Integer(sid));
	 }

	 /**
	  * M�todo que realiza todas las acciones comunes necesarias
	  * 
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 public void init(Integer userRol, String user) throws NotFoundException,
	 IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 this.createAllUtaskPrototypes(userRol, Constants.USER_SYSTEM);
	 }

	 /**
	  * M�todo que realiza todas las acciones comunes necesarias
	  * 
	  * @throws NotFoundException
	  * @throws IncoherenceInMotorException
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 public void init() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 this.createAllUtaskPrototypes(null, Constants.USER_SYSTEM);
	 }

	 public LinkedList<dynagent.common.knowledge.IPropertyDef> getAllNotModelInstanceFacts() throws NotFoundException, IncoherenceInMotorException  {
		 return ruleEngine.getAllInstanceFacts(null, new Null(Null.NOTNULL), null,null, null, null, null, null,null);
	 }


	 public Integer getDatatype(int idProp) throws NotFoundException, IncoherenceInMotorException  {
		 boolean resultado = false;
		 FactProp fp=this.getFactProp(idProp);
		 if(fp!=null){
			 int valuecls = fp.getVALUECLS();
			 // Si VALUECLS es de unidad--> el tipo de dato es FLOAT
			 if (this.isSpecialized(valuecls, Constants.IDTO_UNIDADES))
				 valuecls = Constants.IDTO_DOUBLE;
			 return valuecls;
		 }else 
			 return null;
	 }
	 
	 
	 

	 public LinkedList<Integer> getIdoRelationsInUTask(Integer userRol,
			 String user, Integer usertask) {
		 // TODO Auto-generated method stub
		 return null;
	 }

	 /**
	  * Obtiene el accesso que se tiene sobre una propiedad
	  * 
	  * @param factInst:
	  *            Fact instance
	  * @param userRol:
	  *            Integer
	  * @param user:
	  *            String usuario logado
	  * @param usertask:
	  *            identificador de la usertask desde la que se consulta el
	  *            accesso
	  * @return
	  * @throws NotFoundException
	 * @throws IncoherenceInMotorException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	  */
	 public access getPropertyAccessOf(Property property,Integer userRol, String user, Integer usertask)throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		 int idto=property.getIdto();
		 Integer ido=property.getIdo();
		 int idProp=property.getIdProp();
		 boolean valuesFixed=property.isValuesFixed();
		 
		 access accesstype = null;
		 if (user != null && user.equals(Constants.USER_OWNERALLACCESS)){
			 accesstype = access.getAllAccess();
		 } else {
			 // Consultamos los facts access que satisfacen la condici�n pasada
			 // (por cada slot tb consultamos por slot nil)

			 Iterator<FactAccess> itFactAccess=null;
			 if(accessEngine==null)
				 itFactAccess = ruleEngine.getAccessFactsOfProperty(idto, ido, idProp, userRol, user, usertask).iterator();
			 else{
				 LinkedList<FactAccess> list=new LinkedList<FactAccess>();
				 if(ido!=null){
					 list.addAll(ruleEngine.getAccessFactsIdoRequired(idto, ido, idProp, userRol, user, usertask));
				 }
				 list.addAll(accessEngine.getAccessOfProperty(idto, idProp, usertask));
				 itFactAccess = list.iterator();
			 }
			 // Deducimos el access resultante.
			 accesstype = this.deduceAccess(itFactAccess);
			 if (accesstype == null) {
				 System.err.println("\n\n    WARNING: m�todo getPropertyAccessOf con los par�metros idto="+ idto+ "  ido="+ ido+ "  idProp="+ idProp+ " no ha deducido ningun access ");
			 }
			 
			 if(valuesFixed)
				accesstype.setSetAccess(false);
			 
		 }

		 return accesstype;
	 }

	 /*public String buildPropertyConsultCondition(int idto, Integer ido,
			int idProp, Integer value, Float qmin, Float qmax, String op) {
		String condicion = "(instance (IDTO " + idto + " )(PROP " + idProp
				+ "|nil)";
		if (ido != null)
			condicion += "(IDO " + ido + ")";

		else {
			condicion += "(IDO nil)";
		}
		if (value != null)
			condicion += "(VALUE " + value + ")";

		else {
			condicion += "(VALUE nil)";
		}
		if (qmin != null)
			condicion += "(QMIN " + qmin + ")";

		else {
			condicion += "(QMIN nil)";
		}
		if (qmax != null)
			condicion += "(QMAX " + qmin + ")";

		else {
			condicion += "(QMAX nil)";
		}
		if (op != null)
			condicion += "(OP " + op + ")";

		else {
			condicion += "(OP nil)";
		}

		condicion += "))";
		return condicion;

	}


	  */

	 public access deduceAccess(	Iterator<dynagent.ruleengine.src.ruler.FactAccess> it) {
		 access access = null;
		 int suma_accesstype = 0;
		 /*
		  * PRIORIDADES: 1 nivel: Los facts que tengan IDO distinto de nil 2
		  * nivel: Los facts que tengan IDO nil Dentro de cada nivel prevalecen
		  * las denegaciones.
		  */
		 boolean dennied = false;
		 boolean allowed = false;
		 HashMap<Integer, ArrayList<FactAccess>> hmAccessXFactAccess;
		 ArrayList<FactAccess> lfa;
		 FactAccess fa;
		 Integer accesstype = null;

		 // Construimos el mapa con todos los factsAccess que hay por cada tipo
		 // de accesso
		 hmAccessXFactAccess = this.groupByAccessType(it);
		 Iterator itA = hmAccessXFactAccess.keySet().iterator();
		 while (itA.hasNext()) {
			 accesstype = (Integer) itA.next();
			 lfa = hmAccessXFactAccess.get(accesstype);
			 dennied = false;
			 allowed = false;
			 int maxPriority=-1;
			 for (int i = 0; i < lfa.size(); i++) {
				 fa = lfa.get(i);
				//A igualdad de prioridad las denegaciones tienen son mas prioritarias
				 if (fa.getPRIORITY()>=maxPriority && fa.getDENNIED().equals(1)) {
					 dennied = true;
				 } else if (fa.getPRIORITY()>maxPriority && fa.getDENNIED().equals(0)) {
					 dennied=false;
					 allowed = true;
				 }
				 
				 maxPriority=Math.max(maxPriority, fa.getPRIORITY());
			 }
			 if(!dennied && allowed){
				 suma_accesstype += accesstype;
			 }
		 }
		 access = new access(suma_accesstype);
		 return access;
	 }

	

	public Integer getPropertyInverse(int idProp) {
		 FactProp fp=this.getFactProp(idProp);
		 if(fp!=null)
			 return  fp.getPROPIN();
		 else 
			 return null;
	 }

	public boolean isAbstract(int id) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, OperationNotPermitedException{
		if (this.getMetaData() != null){
			Integer idto = null;
			Integer ido = null;
			if (this.isIDCompleteClass(id)) {
				idto = id;
			} 
			else if(!this.existInMotor(id)){
				Auxiliar.printCurrentStackTrace();
				System.err.println("\n\n  WARNING: LLAMADA A isAbstract id="+id+" de un objeto que no esta en motor, no puede saber si es abstracta");
			}
			else{
				ido = id;
				idto = this.getClassOf(ido);
			}
			return this.getMetaData().getClases().get(idto).isAbstract();

		}else{
			return getAccessOverObject(id, null, user, null).getAbstractAccess();
		}
	}

	 /*	 public boolean pointToFilter(IPropertyDef f){
		 boolean result=false;

		 if(this.isObjectProperty(f.getPROP())&&f.getVALUE()!=null){

			 if(Auxiliar.hasIntValue(f.getVALUE())){
				 int ivalue=new Integer(f.getVALUE());
				 result=this.isIDFilter(ivalue);
			 }
		 }
		 return result; 
	 }
	  */
	 
	 /*private ArrayList<IPropertyDef> selectNotStructurals(ArrayList<IPropertyDef> facts){
		 ArrayList<IPropertyDef> factsNotStructurals=new ArrayList<IPropertyDef>();
		 Iterator<IPropertyDef> itr=facts.iterator();
		 HashSet<Integer> idoFacts=new HashSet<Integer>();
		 HashSet<Integer> idoStructuralFacts=new HashSet<Integer>();
		 while(itr.hasNext()){
			 IPropertyDef fact=itr.next();
			 idoFacts.add(fact.getIDO());
			 if(getCategory(fact.getPROP()).isStructural())
				 idoStructuralFacts.add(Integer.valueOf(fact.getVALUE()));
		 }
	 }*/

//	 private IPropertyDef getInitialValueOfFactDeleted(int ido,int idProp){
//		 IPropertyDef fi=null;
//		 LinkedList<IPropertyDef> facts=ruleEngine.getAllInstanceFactsDELETED(ido, idProp);
//		 //System.err.println("facts:"+facts);
//		 if(!facts.isEmpty()){
//			 fi=facts.getFirst().getInitialValues();
//			// System.err.println("Se coge:"+facts.getFirst());
//		 }
//		 return fi;
//	 }

	 public int getNbusiness() {
		 return nbusiness;
	 }

	 public void setNbusiness(int nbusiness) {
		 this.nbusiness = nbusiness;
	 }

	 public String getUser() {
		 return user;
	 }
	 
	 
	 public String getUSER() {//duplicado metodo pq las reglas necesitan que el campo este en mayuscula para poder usarlo, todo eliminar uso getUser por getUSER()
		 return user;
	 }

	 public void setUser(String user) {
		 this.user = user;
	 }

	 public HashSet<String> getUserRoles() {
		 //System.err.println("\n INFO USERROLES ddm.getUserRoles return :"+userRoles);
		 return userRoles;
	 }
	 
	 public String getUSERROL() {
		 HashSet<String>  roles=this.getUserRoles();
		 if(roles.size()==1){
			 Iterator it=roles.iterator();
			 return (String)it.next();
		 }
		 else 
			 return null;
	 
	 }

	 public void setUserRoles(HashSet<String> userRoles) {
		 this.userRoles = userRoles;
	 }

	 public void loadMetaData() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		
		 Date now=new Date(System.currentTimeMillis());
		
		 //Para depuracion 
		 //ruleEngine.printMotor();
		 /*System.out.println("ruleEngine="+ruleEngine.getClass());
		
		 System.out.println("\n        ************INICIO loadMetaData *******    "+now);
		 
		 System.out.println("ruleEngine="+ruleEngine.getClass());*/
		
		 double inicio,fin,tiempos;
		 inicio=System.currentTimeMillis();
		 MetaData metadat=new MetaData();
		 metadat.setNbusiness(this.getNbusiness());
		 this.setInfoCompletedClases(metadat) ;
		 

		 // PROPIEDADES.
		 this.setInfoProperties(metadat);
		 this.setMetaData(metadat);
		 this.createGenericFilters(metadat);
		 fin=System.currentTimeMillis();
		 tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		 //System.out.println("--time-->  SE CARGO METADADATA EN TIEMPO(segundos)="+tiempos+"\n");
		 //impresion solo para pruebas, comentar
		 if(Constants.printLog){
			 ConceptLogger.getLogger().writeln("\n\n  RULER="+Constants.RULER);
			 //System.out.println(this.getMetaData());
			 ConceptLogger.getLogger().writeln(this.getMetaData().toString());
		 }
		 //imprime metadata
		 //System.out.println(this.getMetaData().toString());
		 //imprime grupos atributos.
		 //System.out.println("===============\n==========================GRUPOS ATRIBUTOS====================\n"+this.hmGrupoAtribxAtrib+"===============\n==========================");

		 //INDIVIDUOS QUE SON DE CLASES TIPO ENUMERADO TIENEN QUE ESTAR CACHEADOS EN MOTOR
		 //this.loadAllEnumeratedIndividual(null, this.getUser(), null);
		 //System.out.println(" INFO DE ENUMERADOS CAMBIADA, MAPA="+this.getHmRdnxIDO());
	 }

	private void createGenericFilters(MetaData metadat)
			throws IncoherenceInMotorException, NotFoundException,
			IncompatibleValueException, CardinalityExceedException,
			SystemException, RemoteSystemException, CommunicationException,
			InstanceLockedException, ApplicationException,
			OperationNotPermitedException, DataErrorException, ParseException,
			SQLException, NamingException, JDOMException {
		Iterator<Integer> itr=metadat.getClases().keySet().iterator();
			while(itr.hasNext()){
				Integer idto=itr.next();
				if(idto!=Constants.IDTO_UTASK && !isSpecialized(idto, Constants.IDTO_UTASK)){
					int idoFilter=createPrototype(idto, Constants.LEVEL_FILTER, null, getUser(), null, false);
					hmGenericFilterXIdto.put(idto, idoFilter);
				}
			}
	}

	 private void loadClases(MetaData md){
	 }

	 public MetaData getMetaData() {
		 return this.metaData;
	 }

	 public void setMetaData(MetaData metaData) {
		 this.metaData = metaData;
	 }


	 /**
	  * @throws IncoherenceInMotorException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws DataErrorException 
	  * @throws ParseException 
	  * @throws JDOMException 
	  * @throws DataErrorException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	  * @throws NamingException 
	  * @throws SQLException 
	  * 
	  */
	 /*
	  * TODO REVISAR SI HACE FALTA QUE TENGA LA SESION, PROBABLMENTE NO
	  */
	 public synchronized HashSet<Integer>  loadNewData(ArrayList<IPropertyDef>instances, Integer userRol, String user,	Integer usertask,Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, DataErrorException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		 
		 DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
		 
		 Session oldSession =SessionController.getInstance().getActualSession(this);
		 boolean success=false;
		 try {
			 SessionController.getInstance().setActual(sess,this);
//			 System.out.println("DENTRO DE newEvent public");
			 
			 
			 //////el publico no va a provocar creaci�n de filtros
			 HashSet<Integer> idos=this.loadNewData(instances, userRol, user,usertask,false,false,false);

			 ruleEngineRun();
			 sess.sendPendingLocks();
			 sess.commit();
			 success=true;
			 return idos;
		 }finally{
			 if(!success){
				 try{
					 sess.rollBack();//cancelSession(sess);
					 System.err.println("WARNING:Sesion interna de DocDataModel.loadNewData cancelada");
				 } catch (Exception e) {
					 System.err.println("WARNING:Sesion interna de DocDataModel.loadNewData no ha podido cancelarse");
					 e.printStackTrace();
				 }finally{
					 SessionController.getInstance().setActual(oldSession,this);
				 }
			 }else SessionController.getInstance().setActual(oldSession,this);
		 }
	 }

	





	
	 public IRuleEngine getRuleEngine() {
		 return ruleEngine;
	 }

	 public void setRuleEngine(IRuleEngine ruleEngine) {
		 this.ruleEngine = ruleEngine;
	 }

	 public void asign(int idoUTask, String user, Integer rol) throws SystemException,InstanceLockedException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		 asigned.asign(idoUTask, user, rol);
	 }

	 public void preAsign(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		 asigned.preAsign(idoUTask, idtoUTask);
	 }

	 public void release(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		 asigned.release(idoUTask, idtoUTask);
	 }

	 public void reAsign(int idoUTask, String user, Integer rol) throws SystemException, InstanceLockedException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		 asigned.reAsign(idoUTask, user, rol);
	 }

	 public void close(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException {
		 asigned.close(idoUTask, idtoUTask);
	 }

	 public HashMap<Integer, HashSet<Integer>> getHmIDOSxIDTO() {
		 return hmIDOSxIDTO;
	 }
	 
	
	 public void setHmIDOSxIDTO(HashMap<Integer, HashSet<Integer>> hmIDOSxIDTO) {
		 this.hmIDOSxIDTO = hmIDOSxIDTO;
	 }

	 public HashMap<Integer, Integer> getHmIDTOxIDO() {
		 return hmIDTOxIDO;
	 }

	 /*public Integer getVALUECLSxVALUE(String valor)  throws NotFoundException{
		 if(this.hmVALUECLSxVALUE.get(valor)!=null)
			 return this.hmVALUECLSxVALUE.get(valor);
		 else{
			 Integer idtovalue=null;
			 idtovalue=this.getClaseOfValue(valor);
			 if(idtovalue!=null){
				 this.hmVALUECLSxVALUE.put(valor,idtovalue);
				 return idtovalue;
			 }
			 else{
				 throw new NotFoundException(" NO SE ENCUENTRA VALUECLS PARA VALOR="+valor);
			 }
		 }
	 }*/
	 
	  
	 public void setHmIDTOxIDO(HashMap<Integer, Integer> hmIDTOxIDO) {
		 this.hmIDTOxIDO = hmIDTOxIDO;
	 }

	 public HashMap<Integer, Integer> getHmLEVELxID() {
		 return hmLEVELxID;
	 }

	 public void setHmLEVELxID(HashMap<Integer, Integer> hmLEVELxID) {
		 this.hmLEVELxID = hmLEVELxID;
	 }


	 public Session getDefaultSession() {

		 return this.sessionTemporal;
	 }

	 public Session getRootSession() {
		 return session;
	 }


	 


	 /**
	  * Crea  filtros especializados de uno dado 
	  * @param ido: identificador del filtro que se quiere especializar
	  * @return: ArrayList<Integer>: Lista con el/los identificadores de los filtros especializados creados.
	  * @throws NotFoundException 
	  * @throws IncoherenceInMotorException 
	  * @throws ApplicationException 
	  * @throws InstanceLockedException 
	  * @throws dynagent.common.exceptions.CommunicationException 
	  * @throws RemoteSystemException 
	  * @throws SystemException 
	  * @throws CardinalityExceedException 
	  * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws DataErrorException 
	  */
	 private ArrayList<Integer> getSpecializedFilters (int ido,Integer userRol,String user,Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		 ArrayList<Integer> filtrosspecialized=new ArrayList<Integer>();
		 //COMPROBAMOS SI PREVIAMENTE YA SE LE CREARON SUS FILTROS ESPECIALIZADOS EN CUYO CASO LOS DEVOLVEMOS
		 /*if(this.hmFiltrosSpecializadosxFiltro.get(ido)!=null)
			return this.hmFiltrosSpecializadosxFiltro.get(ido);

		else{
		  */
		 int idto=this.getClassOf(ido);//obtenemos su tipo
		 //HashSet<Integer> specializeds=this.getSpecializedHS(idto);
		 //SOLO INTERESAN LOS SPECIALIZADOS DIRECTOS
		 HashSet<Integer> specializeds=this.getDirectSpecialized(idto);
		 Iterator<Integer> itClasesSpecialized=specializeds.iterator();
		 if(itClasesSpecialized.hasNext()){
			 while(itClasesSpecialized.hasNext()){
				 int idtospec=itClasesSpecialized.next();
				 //int idospect=createPrototype(idtospec, Constants.LEVEL_FILTER, userRol, user, usertask, false).intValue();
				 int idospect=getGenericFilter(idtospec);
				 filtrosspecialized.add(idospect);
			 }
		 }
		 //this.hmFiltrosSpecializadosxFiltro.put(ido, filtrosspecialized);
		 //MEMORIAZAMOS EN UN MAPA LOS FILTROS SPECIALIZADOS QUE LE HE CREADO POR SI VOLVIERA A PREGUNTAR POR EL MISMO POSTERIORMENTE 

		 //}
		 return filtrosspecialized;
	 }
	 
	 /**
	  * Devuelve la utask actual en la que est� el usuario.
	  * @return
	  */
	 private Integer getActualUtask() {
		 Integer utask= SessionController.getInstance().getActualSession(this).getUtask();
		 //System.err.println("\n INFO: GETACTUALUTASK DEVUELVE="+utask);
		 return utask;
	 }

	 public synchronized ArrayList<Integer> getSpecializedFilters (int ido,Integer userRol,String user,Integer usertask,Session session) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		 DefaultSession sess = new DefaultSession(this,session,session.getUtask(),false, session.isRunRules(),session.isLockObjects(),session.isDeleteFilters(),false);
		 ArrayList<Integer>idos=null;

		 Session oldSession =SessionController.getInstance().getActualSession(this);
		 boolean success=false; 
		 try {
			 //System.out.println("DENTRO DE getPropertyIterator public");
			 SessionController.getInstance().setActual(sess,this);
			 idos = this.getSpecializedFilters(ido, userRol, user, usertask);
			 // si todo ha ido bien. commit.
			 ruleEngineRun();
			 sess.sendPendingLocks();
			 sess.commit();
			 success=true;
		 }finally{
			 if(!success){
				 try{
					 sess.rollBack();//cancelSession(sess);
					 System.err.println("WARNING:Sesion interna de DocDataModel.getSpecializedFilters cancelada");
				 }catch (Exception e) {
					 System.err.println("WARNING:Sesion interna de DocDataModel.getSpecializedFilters no ha podido cancelarse");
					 e.printStackTrace();
				 }finally{
					 SessionController.getInstance().setActual(oldSession,this);
				 }
			 }else SessionController.getInstance().setActual(oldSession,this);
		 }
		 return idos;

	 }

	 public synchronized boolean checkCoherenceObject(int ido,Integer userRol,String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{

		 DefaultSession sess = new DefaultSession(this,session,session.getUtask(),false, session.isRunRules(),session.isLockObjects(),session.isDeleteFilters(),false);
		 boolean welldefined=false;
	
		 Session oldSession =SessionController.getInstance().getActualSession(this);
		 boolean success=false; 
		 try {
			 //System.out.println("DENTRO DE getPropertyIterator public");
			 SessionController.getInstance().setActual(sess,this);
			 welldefined = this.checkCoherenceObject(ido, userRol, user, usertask);
			 // si todo ha ido bien. commit.
			 if(sess.getSesionables().size()>0){//Si no tiene ningun fact no sirve de nada hacer un run porque no se ha traido nada
				 ruleEngineRun();//TODO No es muy logico tener que hacer un run porque checkCoherence no deberia traerse individuos de base de datos,pero entonces deberiamos quitar tambien la sesion
				 sess.sendPendingLocks();
			 }
			 sess.commit();
			 success=true;
		 }finally{
			 if(!success){
				 try{
					 sess.rollBack();//cancelSession(sess);
					 System.err.println("WARNING:Sesion interna de DocDataModel.checkCoherenceObject cancelada");
				 }catch (Exception e) {
					 System.err.println("WARNING:Sesion interna de DocDataModel.checkCoherenceObject no ha podido cancelarse");
					 e.printStackTrace();
				 }finally{
					 SessionController.getInstance().setActual(oldSession,this);
				 }
			 }else SessionController.getInstance().setActual(oldSession,this);
		 }
		 return welldefined;
	 }

	 public boolean checkCoherenceObject(int ido,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		 boolean welldefined=false;
		 //System.err.println("\n debug checkCoherenceObject id="+ido);
		 Iterator<Property> itp=this.getAllPropertyIterator(ido,this.getClassOf(ido), userRol, user, usertask, false);
		 while(itp.hasNext()){
			 Property p=itp.next();
//			 //Si la property fuera rdn y no tiene permiso de edicion no la chequeamos ya que estara vacia porque en el servidor se le dara valor
//			 if(!p.getIdProp().equals(Constants.IdPROP_RDN) || getPropertyAccessOf(p, userRol, user, usertask).getSetAccess())
				 welldefined=p.checkPropertyWellDefined(this,usertask);		
		 }
		 return welldefined;
	 }


	 


	 public ArrayList<OrderProperty> getOrderProperties() {
		 return orderProperties;
	 }

	 public void setOrderProperties(ArrayList<OrderProperty> arrayOrderProperties) {
		 this.orderProperties = arrayOrderProperties;
	 }


	 public String getRdnIfExistInRuler(int ido) {
		 return this.hmRdnxIDO.get(ido);
	 }

	 public HashMap<Integer, String> getHmRdnxIDO() {
		 return hmRdnxIDO;
	 }

	 public void setHmRdnxIDO(HashMap<Integer, String> hmRdnxIDO) {
		 this.hmRdnxIDO = hmRdnxIDO;
	 }   
		   
		   public  HashSet<Integer>  loadInRulerFactsOfIndividualSpecializedOfClass(int idto,int levels) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	   		dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
			 ArrayList instances=server.serverGetFactsInstanceOfClassSpecialized(idto, this.getUser(), false, levels, true).getAIPropertyDef();
			// System.err.println("\n DEBUG loadInRulerFactsOfIndividualSpecializedOfClass "+this.getClassName(idto)+" n. facts dev. server="+instances.size());
			 //for(int i=0;i<instances.size();i++){
			//	 System.err.println("\n"+instances.get(i));
			 //}
			 return this.loadNewData(instances, null, Constants.USER_SYSTEM, null, false,false,false);
	   }
		   
	   public  HashSet<Integer>  loadInRulerFactsOfIndividualOfClass(int idto,int levels) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
   		dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		 ArrayList instances=server.serverGetFactsInstanceOfClass(idto, this.getUser(), false, levels, true).getAIPropertyDef();
		// System.err.println("\n DEBUG loadInRulerFactsOfIndividualSpecializedOfClass "+this.getClassName(idto)+" n. facts dev. server="+instances.size());
		 //for(int i=0;i<instances.size();i++){
		//	 System.err.println("\n"+instances.get(i));
		 //}
		 return this.loadNewData(instances, null, Constants.USER_SYSTEM, null, false,false,false);
	  }
	   
	   

	  public void retractInfoObjects(HashSet<Integer> idos,boolean recursive,boolean retractInMotor) throws NumberFormatException, NotFoundException, IncoherenceInMotorException {
		  Iterator it=idos.iterator();
		  while(it.hasNext()){
			  this.retractInfoObject((Integer)it.next(), recursive, retractInMotor);
		  }
	  }
	  
	  
	  public void retractInfoObject(int id,boolean recursive,boolean retractInMotor) throws NumberFormatException, NotFoundException, IncoherenceInMotorException {
		  //1. Se borra(retract) toda los facts de ese objeto
		  Iterator<IPropertyDef> it=ruleEngine.getInstanceFactsWhereIdo(id).iterator();
		  while(it.hasNext()){
			  Fact f=(Fact)it.next();
			  retractInfoFact(f,retractInMotor);
			  if(recursive){
				  if(this.isObjectProperty(f.getPROP()) && f.getVALUE()!=null &&f.getLEVEL()>Constants.LEVEL_FILTER && !isSpecialized(f.getVALUECLS(),Constants.IDTO_ENUMERATED))
				  {
					  Iterator itApuntado = ruleEngine.getAllInstanceFacts(null, null,null, f.getVALUECLS(), f.getVALUE(), null, null, null, null).iterator();
					  if(!itApuntado.hasNext()){
						  retractInfoObject(new Integer(f.getVALUE()),recursive,retractInMotor);
						  //idosBorrables.add(new Integer(f.getVALUE()));
					  }/*else{
						  System.err.println("No borra:"+itApuntado.next());
					  }*/
				  }
			  }
		  }
		  if(this.getRuleEngine().getIndividualFact(id)!=null){
				

			  
			  Individual i=this.getRuleEngine().getIndividualFact(id);
			  retractInfoIndividual(i,retractInMotor);
			  
			  Lock l=this.getRuleEngine().getLockFact(id);
			  retractInfoLock(l,retractInMotor);
		  }
		  //2. Se da de baja de todos los mappas de nivel, su clase,...
		  Integer idto=  this.getHmIDTOxIDO().get(id);
		  if(idto!=null)
		  {
			  this.getHmIDTOxIDO().remove(id);
			  HashSet<Integer> idos=this.getHmIDOSxIDTO().get(idto);
			  if(idos!=null && idos.size()>0)
				  idos.remove(id);
			  else{
				  //System.out.println("No encuentra idto:"+idto+" del ido:"+id+" en:"+this.getHmIDOSxIDTO());
				  /*idos=this.getHmIDOSxIDTO().get(id);
				  if(idos!=null && idos.size()>0)
					  idos.remove(idto);
				  else{
					  System.out.println("Tampoco encuentra idto:"+id+" del ido:"+idto+" en:"+this.getHmIDOSxIDTO());
				  }*/
			  }
			  this.getHmLEVELxID().remove(id);
		  }
	  }

	  /*public void retractFromRuler(Iterator<Integer> idos) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		  while(idos.hasNext()){
			  int ido=idos.next();
			  if(existInMotor(ido))
				  this.deleteObject(ido,true);
		  }

	  }*/

	  public void retractInfoFact(Fact f, boolean retractInMotor){
		  //Se hace clone para evitar que haya un fallo de ConcurrentModification
		  Iterator<Session> ses = ((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(this).clone()).values().iterator();
		  while(ses.hasNext())
		  {
			  Session sess=ses.next();
			  if(sess.getSesionables()==null){
				  //System.err.println("RetractInfoFact de session dispose:"+sess+" sessionList:"+SessionController.getInstance().getSessionsList(this));
				  Exception ex=new Exception();
				  ex.setStackTrace(((DefaultSession)sess).trace);
				  ex.printStackTrace();
			  }
			  /*boolean remove=*/sess.getSesionables().remove(f);
			  /*if(remove)
				  System.err.println("Remueve:"+f);*/
		  }
		  if(retractInMotor)
			  ruleEngine.retractFact(f);
		  else listFactRetractable.add(f);
	  }

	  
	  public void retractInfoFactDataRules(DataRules f, boolean retractInMotor){
		  //Se hace clone para evitar que haya un fallo de ConcurrentModification
		  Iterator<Session> ses = ((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(this).clone()).values().iterator();
		  while(ses.hasNext())
		  {
			  Session sess=ses.next();
			  if(sess.getSesionables()==null){
				  //System.err.println("RetractInfoFact de session dispose:"+sess+" sessionList:"+SessionController.getInstance().getSessionsList(this));
				  Exception ex=new Exception();
				  ex.setStackTrace(((DefaultSession)sess).trace);
				  ex.printStackTrace();
			  }
			  /*boolean remove=*/sess.getSesionables().remove(f);
			  /*if(remove)
				  System.err.println("Remueve:"+f);*/
		  }
		  if(retractInMotor)
			  ruleEngine.retractFactDataRules(f);
		  else listFactRetractable.add(f);
	  }
	  
	  
	  
	  
	  
	  public void retractInfoIndividual(Individual f,boolean retractInMotor){
		//Se hace clone para evitar que haya un fallo de ConcurrentModification
		  Iterator<Session> ses = ((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(this).clone()).values().iterator();
		  while(ses.hasNext())
		  {
			  /*boolean remove=*/ses.next().getSesionables().remove(f);
			  /*if(remove)
				  System.err.println("Remueve:"+f);*/
		  }
		  if(retractInMotor)
			  ruleEngine.retractIndividual(f);
		  else listFactRetractable.add(f);
	  }
	  
	  private void retractInfoLock(Lock f,boolean retractInMotor){
		  Iterator<Session> ses = ((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(this).clone()).values().iterator();
		  while(ses.hasNext())
		  {
			  /*boolean remove=*/ses.next().getSesionables().remove(f);
			  /*if(remove)
				  System.err.println("Remueve:"+f);*/
		  }
		  
		  if(retractInMotor)
			  ruleEngine.retractLock(f);
		  else listFactRetractable.add(f);
	  }
	  
	  public void retractInfoFactAccess(FactAccess f,boolean retractInMotor){
		  Iterator<Session> ses = ((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(this).clone()).values().iterator();
		  while(ses.hasNext())
		  {
			  /*boolean remove=*/ses.next().getSesionables().remove(f);
			  /*if(remove)
				  System.err.println("Remueve:"+f);*/
		  }
		  
		  if(retractInMotor)
			  ruleEngine.retractFactAccess(f);
		  else listFactRetractable.add(f);
	  }
	  
	/*  
	  public void retractPropertiesAislado(int ido, boolean borrarTodo) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException
	  {
		  System.err.println("RETRACT DEL INDIVIDUO CON IDO "+ ido+ "borrar todo = "+borrarTodo);
		
		  Iterator <IPropertyDef> factsItr=this.getRuleEngine().getInstanceFactsWhereIdo(ido).iterator();
		 ArrayList<Integer> idosBorrables = new ArrayList<Integer>();
		 while(factsItr.hasNext())
		 {
			 Fact f =(Fact)factsItr.next();
			 
			 
			 boolean isObjProp = this.isObjectProperty(f.getPROP());
			 
			 
			 if(!borrarTodo && isObjProp && f.getVALUE()!=null)
			 {
				 f.setVALUE(null);//aislo el individuo apuntado para que se dispare la regla de nuevo con el indiv. apuntado.
				 //System.err.println("RETRACT DEL INDIVIDUO CON IDO "+ ido+ " PROP = "+f.getPROP());
			 }
			 else{
				 if(isObjProp&&f.getVALUE()!=null)
					 idosBorrables.add(new Integer(f.getVALUE())); 
				 System.err.println("2-retract PROP = "+getPropertyName(f.getPROP()));
				 f.retract();
				 
			 }
		 }
		 
		 Iterator<Integer> borrablesItr = idosBorrables.iterator();
		 while(borrablesItr.hasNext())
		 {
			 Integer idoBorrable = borrablesItr.next();
			 if(this.getRuleEngine().getInstanceFactsWhereIdo(idoBorrable).isEmpty())
				 retractInfoObject(idoBorrable);
		 }
			 
		  
	  }*/
	  
	  /*public void retractIdo(int ido) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException
	  {
		  System.err.println("llamada a retractIdo(int "+ido+")");
		 Iterator <IPropertyDef> factsItr=this.getRuleEngine().getInstanceFactsWhereIdo(ido).iterator();
		 LinkedList <IPropertyDef> factsApuntan =  new LinkedList<IPropertyDef>();
		 ArrayList<Integer> idosBorrables = new ArrayList<Integer>();
		 while(factsItr.hasNext())
		 {
			 Fact f =(Fact)factsItr.next();		 
			 boolean isObjProp = this.isObjectProperty(f.getPROP());
			 		 
			 if(isObjProp && f.getVALUE()!=null &&f.getLEVEL()>Constants.LEVEL_FILTER)
			 {
				 idosBorrables.add(new Integer(f.getVALUE()));
				 f.retract();//elimino el fact
			 }
			 else
			 {				
				 f.retract();
			 }
		 }
		 removeFromMaps(ido,true);
		 factsApuntan = this.getRuleEngine().getInstanceFactsWhereIdo(ido);
		 if(!factsApuntan.isEmpty())
			 System.err.println("NO TODOS LOS FACTS DEL IDO : "+ido+" HAN SIDO BORRADOS"+factsApuntan);
		 Iterator<Integer> borrablesItr = idosBorrables.iterator();
		 while(borrablesItr.hasNext())
		 {
			 Integer idoBorrable = borrablesItr.next();
			  this.getRuleEngine().getAllInstanceFacts(null,null,null,null,idoBorrable.toString(),null,null,null,null);
			 System.err.println("HAY : "+factsApuntan.size() +" que apuntan a " +idoBorrable);
			 if(factsApuntan.isEmpty())
				retractIdo(idoBorrable);
		 }
			 
		  
	  }*/
	  

	  public ArrayList<Integer>getAllIdosOfClassFromServer(int idto) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException
	  {
		  ArrayList<Integer> idos=new ArrayList<Integer>();
		  Iterator<instance> instances=this.getAllObjectsofClassFromServer(this.getClassName(idto)).iterator();
		  while (instances.hasNext()){
			  idos.add(instances.next().getIDO());
		  }
		  return idos;

	  }

	  public LinkedList<instance> getAllObjectsofClassFromServer(String clase) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException
	  {
		  int idto = this.getIdClass(clase);
		  LinkedList<instance> resultado = new LinkedList<instance>();
		  Integer ido = this.createPrototype(idto, Constants.LEVEL_FILTER, null, "SYSTEM", null, false);
		  ArrayList <SelectQuery> sq = new ArrayList <SelectQuery>();
		  SelectQuery sq1 = new SelectQuery(ido.toString(),Constants.IdPROP_RDN, null, null);
		  sq.add(sq1);


		  //qxml.setSelect(sq);
		  instance filter = this.getTreeObject(ido, null, Constants.USER_SYSTEM, null);
		  QueryXML query=this.getQueryXML();
		  query.setShowIdos(true);
		  query.setSelect(sq);
		  Element xml = query.toQueryXML(filter,null);
		  //System.out.println("=======XML========\n"+jdomParser.returnXML(xml)+"\n=========/XML========");
		  Integer uTask = SessionController.getInstance().getActualSession(this).getUtask();
		  selectData sd = server.serverGetQuery( xml, uTask, queryData.MODE_ROOT);
		  Iterator itsd = sd.getIterator();
		  while (itsd.hasNext())
		  {
			  instance i = (instance)itsd.next();
			  resultado.add(i);

		  }
		  //System.err.println("getAllObjectsofClassFromServer resultado="+ resultado);

		  return resultado;
	  }

	  public ArrayList<CardMed> getListCM() {
		  return listCM;
	  }

	  public void setListCM(ArrayList<CardMed> listCM) {
		  this.listCM= listCM;
	  }
	  
	  public void setListAlias(ArrayList<Alias> listAlias) {
		  alias=new AliasComponents(this,listAlias);
	  }
	  
	  public void setListMask(ArrayList<Mask> listMask) {
		  masks=new MaskComponents(this,listMask);
	  }

	  public ArrayList<ColumnProperty> getColumnProperties() {
		  return columnProperties;
	  }
	  
	  public ArrayList<Groups> getGroupsProperties() {
		  return groupsProperties;
	  }

	  public void setColumnProperties(ArrayList<ColumnProperty> columnProperties) {
		  this.columnProperties = columnProperties;		
	  }
	  
	  public void setGroupsProperties(ArrayList<Groups> groupsProperties) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		  this.groupsProperties = groupsProperties;
		  for(Groups group:this.groupsProperties){
			  String nameGroup=group.getNameGroup();
			  this.groupsByClassAndProperty.put(group.getIdtoClass()+":"+group.getIdProp(),nameGroup);
		  }
	  }
	  
	   public void denniedAccessOverObject(int ido, String access) throws NotFoundException, IncoherenceInMotorException {  
		   FactAccess fa=new FactAccess(this);
		   Integer idaccess=null;
		   fa.setIDO(ido);

		   fa.setDENNIED(1);
		   if(access.equals(Constants.ACCESS_NEW_NAME)){
			   idaccess=Constants.ACCESS_NEW;
		   }
		   else if(access.equals(Constants.ACCESS_DEL_NAME)){
			   idaccess=Constants.ACCESS_DEL;
		   }
		   else{
			   System.err.println("WARNING:  Dddm.denniedAccessOverObject se ha llamado con una access que no es NEW, ni DEL. Se ignorar� la llamada.\nPar�metros: ido="+ido+" access="+access);
		   }
		   if(idaccess!=null){
			   fa.setACCESSTYPE(idaccess);
			   ruleEngine.insertFact(fa);
		   }
		   //System.err.println("\n deniedAcessobjectobject insrta factacc="+fa);
	   }


	   public void denniedAccessInProperty(int ido, String nameProp,String access) throws NotFoundException, IncoherenceInMotorException {

		   FactAccess fa=new FactAccess(this);
		   Integer idaccess=null;
		   fa.setIDO(ido);
		   fa.setDENNIED(1);
		   if(nameProp !=null)//si se pasa nameProp=nulo significa que se quiere denegar en todas las propiedades de ese ido
			   fa.setPROP(this.getIdProperty(nameProp));
		   fa.setACCESSTYPE(Constants.getAccessType(access));
		   ruleEngine.insertFact(fa);
	   }



	   public void denniedAccess(Integer idto,Integer ido,Integer prop,String access,Integer usertask,String userRol,String user) throws NotFoundException, IncoherenceInMotorException {
		   FactAccess fa=new FactAccess(idto,ido,prop,null,null,userRol,user,usertask,1,Constants.getAccessType(access),0,this);
		   ruleEngine.insertFact(fa);
	   }


	   
	   public synchronized Integer setRange(int ido, int idto,int idProp, int valueCls,Integer userRol,String user,Integer userTask, int depth, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{

			// TODO: Falta Exclusion mutua
			//System.out.println("DENTRO DE SETVALUE PUBLICO");
			DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

			Session oldSession =SessionController.getInstance().getActualSession(this);
			boolean success=false;
			Integer idRange=null;
			try {
				//System.err.println("ido:"+ido+" idProp:"+idProp+" oldVal:"+oldVal+" newVal:"+newVal);
				SessionController.getInstance().setActual(sess,this);
				idRange=setRange(ido, idto,idProp, valueCls, userRol, user, userTask, depth);
				// al final hacer un commit. si falla el commit -> lanzar una
				// excepcion para deshacer lo que estaba haciendo el commit.
				ruleEngineRun();
				sess.sendPendingLocks();
				sess.commit();
				success=true;
			}finally{
				
				if(!success){

					try{
						sess.rollBack();//cancelSession(sess);
						System.err.println("WARNING:Sesion interna de DocDataModel.setValue cancelada");
					} catch (Exception e) {
						System.err.println("WARNING:Sesion interna de DocDataModel.setValue no ha podido cancelarse");
						e.printStackTrace();
					}finally{
						SessionController.getInstance().setActual(oldSession,this);
					}
				}else SessionController.getInstance().setActual(oldSession,this);
				
			}

			return idRange;
		}
	   
	   
	    /**
		    * Este metodo crea un filtro al ido pasado en la propiedad pasada, si ya existiese un filtro anterior en esa propiedad lo elimina y lo sustituye por el nuevo
		    * @param ido
		    * @param nameProp
		    * @param nameClass
		    * @return
		    * @throws NotFoundException
		    * @throws IncoherenceInMotorException
		    * @throws OperationNotPermitedException
		    * @throws CardinalityExceedException
		    * @throws IncompatibleValueException
		    * @throws SystemException
		    * @throws RemoteSystemException
		    * @throws CommunicationException
		    * @throws InstanceLockedException
		    * @throws ApplicationException
		    * @throws DataErrorException
		    * @throws ParseException
		    * @throws SQLException
		    * @throws NamingException
		    * @throws JDOMException
		    */
	    public Integer setRange(int ido, int idto,int idProp, int valueCls,Integer userRol,String user,Integer userTask,int depth) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		   Integer newFilterIdo  =null;
		   Property p=this.getProperty(ido, idto,idProp, null, Constants.USER_SYSTEM, null, false);
		   if ( p instanceof ObjectProperty)
		   {
			   //Obtenemos el filtro al que se apunta ahora mismo
			   ObjectProperty op = (ObjectProperty)p;
			   for(int i=0;i<op.getRangoList().size();i++){
				   int idRange=op.getRangoList().get(i);
				   if(this.isIDFilter(idRange) && !isGenericFilter(idRange)){
					   this.deleteObject(idRange,this.getClassOf(idRange),null,userRol,user,userTask,true);
				   }
			   }
			   /*
			   Le damos la vuelta a la profundidad dicha por el usuario ya que createPrototype lo entiende de esa manera.
			   Suponiendo que la constante MAX_DEPTH_FILTERS valiera 4, las transformaciones que hariamos ser�an:
			   4 --> 0
			   3 --> 1
			   2 --> 2
			   1 --> 3
			   0 --> 4
			   */
			   
			   //Ya no es necesario porque createPrototype ahora lo entiende de la misma manera
//			   if(depth>=Constants.MAX_DEPTH_FILTERS)
//				   depth=0;
//			   else if(depth<=0)
//				   depth=Constants.MAX_DEPTH_FILTERS;
//			   else{
//				   depth=(Constants.MAX_DEPTH_FILTERS)-depth;
//			   }
			   
			   //Creamos un filtro nuevo
			    //newFilterIdo = this.createPrototype(valueCls, Constants.LEVEL_FILTER, null, Constants.USER_SYSTEM, Constants.USER_SYSTEM, false);
			   //nuevo: filtro unico (sin produndidad de filtros por debajo) para mejorar rendimiento
			   newFilterIdo=createPrototype(valueCls, Constants.LEVEL_FILTER, new HashMap<Integer, ArrayList<Integer>>(), null, Constants.USER_SYSTEM,null,depth-1/*El -1 es porque ya createPrototype crea el primer nivel de ese filtro*/);
			   
			   //apuntamos a ese individuo
			    int valuecls=this.getClassOf(newFilterIdo);
			   IPropertyDef fi=new FactInstance(op.getIdto(),op.getIdo(),op.getIdProp(),newFilterIdo.toString(),valuecls,this.getClassName(valuecls),null,null,Constants.OP_UNION,this.getClassName(op.getIdto()));
			   ruleEngine.insertFact(fi);

			   //newFilterIdo=getGenericFilter(valueCls);
					
		   }
		   else
			   System.err.println("WARNING:- Intento de cambiar el rango de una property que no es una objectProperty");
		   return newFilterIdo;
	   }
	    
	    
	    public Integer SystemSetRange(int ido, int idto,int idProp, int valueCls, int depth) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
	    	return this.setRange(ido, idto, idProp, valueCls, null, Constants.USER_SYSTEM, null, depth);
	    }
	    
	    
	    public void setExcluValueToFilter(int idoFilter, int idProp, String excluValue, int valueClsOfExcluValue) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
			   if(this.isIDFilter(idoFilter)){
				   int idto=this.getClassOf(idoFilter);
			   	   IPropertyDef fi=new FactInstance(idto,idoFilter,idProp,excluValue,valueClsOfExcluValue,this.getClassName(valueClsOfExcluValue),null,null,Constants.OP_NEGATION,this.getClassName(idto));
				   ruleEngine.insertFact(fi);
			   }
	    	   else
				   System.err.println("\n\n========   WARNING:-llamada a ddm.setExcluValueToFilter no hace nada porque se llamo con ido que no es de filtro: ido="+idoFilter);
		}
	    
	    
	    public void setExcluValueToFilter(String sidoFilter, String nameProp, String excluValue, int valueClsOfExcluValue) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
	    	this.setExcluValueToFilter(new Integer(sidoFilter), this.getIdProperty(nameProp), excluValue, valueClsOfExcluValue);
	    }

	/**
	 * Para mostrar una ventana gr�fica al usuario con el mensaje indicao como par�metro
	 * @param message
	 */public void showMessage(String message) {	
		SessionController.getInstance().getActualSession(this).sendMessage(message);
	}
	 
	 public Boolean showQuestion(String message, boolean initialSelectionIsYes) {	
		 return SessionController.getInstance().getActualSession(this).sendQuestion(message, initialSelectionIsYes);
	 }
	
	public boolean isRangeCompatible(int iddominio,int idProp,int idrange) throws IncoherenceInMotorException {
	//	System.err.println("  DEBUG ISRANGECOMPATIBLE iddominio="+iddominio+"   idprop="+idProp+"  range="+idrange);
//		System.err.println("=============================== DEBUG ISRANGECOMPATIBLE dominio="+this.getClassName(iddominio)+" propiedad="+this.getPropertyName(idProp)+"  range="+this.getClassName(idrange));//System.err.println("=============================== DEBUG ISRANGECOMPATIBLE dominio="+this.getClassName(iddominio)+" propiedad="+this.getPropertyName(idProp)+"  range="+this.getClassName(idrange));
		boolean compatible=false;
		if(!this.getMetaData().getClase(iddominio).hasProperty(idProp)){
			//System.err.println("                  RETURN FALSE:  la propiedad no pertenece al dominio");
			compatible= false;
		}
		else{
			Property p=this.getMetaData().getClase(iddominio).getProperty(idProp);
			if(p instanceof ObjectProperty){
				ObjectProperty op=(ObjectProperty)p;
				Iterator<Integer> it=op.getRangoList().iterator();
				while(it.hasNext()&&!compatible){
					int idrange2=it.next().intValue();
					//try {
						//if(idrange2==idrange||this.isSpecialized(idrange2, idrange)){
						if(idrange2==idrange||this.isSpecialized( idrange,idrange2)){
							compatible= true;
							
						}
					/*} catch (NotFoundException e) {
						compatible= false;
					}*/
				}
			}
		}
		return compatible;
		
	}
	
	
	/**
	 * Indica si una clase tiene una propiedad dada
	 * @param idto: identificador de la clase
	 * @param idprop: identificador de la propiedad que se quiere saber si existe en la clase idto
	 * @return: true si  si es propiedad de esa clase
	 */public boolean hasProperty(int idto,int idprop){
		return this.getMetaData().hasProperty(idto, idprop);
	}
	 
	 public boolean hasProperty(String nameClase,String nameProp){
		 return this.getMetaData().hasProperty(nameClase, nameProp);
	 }
	 
	 
		//////////////M�TODOS DEL FICHERO DE REGLAS////////////////////////
	//ELIMINENSE DE AQUI---SOLO EN DESARROLLO///////////////////
	
	 public Integer getMaxPropertyCardinalityOfClass(int idto,int idprop){
		 if(this.getMetaData()!=null){
			 Property p=this.getMetaData().getClases().get(idto).getProperty(idprop);
			 if(p!=null)
				 return p.getCardMax();
			 else return new Integer(0);
		 }
		 else{
			 
			try {
				return this.getProperty(null,idto, idprop,null, Constants.USER_SYSTEM, null, false).getCardMax();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		 }
	 }
				
	 public Integer getMinPropertyCardinalityOfClass(int idto,int idprop){
		
		 if(this.getMetaData()!=null){
			 return this.getMetaData().getClases().get(idto).getProperty(idprop).getCardMin();
		 }
		 else{
			 
			try {
				return this.getProperty(null,idto, idprop,null,null,null,false).getCardMin();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		 }
	 }

	public HashMap<String, String> getCache() {
		return cache;
	}

	public void setCache(HashMap<String, String> cache) {
		this.cache = cache;
	}
	
	/**
	 * DEVUELVE EL RANGO DE UNA OBJECTPROPERTY DE UNA CLASE
	 * @param idto
	 * @param idProp
	 * @return
	 */
	public LinkedList<Integer> getObjectRange(int idto,int idProp ){
		ObjectProperty op=(ObjectProperty)this.getMetaData().getClase(idto).getProperty(idProp);
		return op.getRangoList();
	}



	/*public boolean userModifyObject(int ido, boolean modify) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
		LinkedList <IPropertyDef> facts = this.getInstanceFacts(ido,Constants.IdPROP_USERMODIFY);
		if(facts.size()>1)
			throw new OperationNotPermitedException("El individuo"+ido+" no puede tener mas de un fact indicando su estado de modificacion",null);
		
		if(modify){
			if(facts.isEmpty())
			{
			FactInstance f =  new FactInstance(getClassOf(ido),ido,Constants.IdPROP_USERMODIFY,null,null,new Double(1),new Double(1),"AND",getClassName(getClassOf(ido)));
			this.getRuleEngine().insertFact(f);
			}
			else
			{
				facts.getFirst().setQMAX(new Double(1));
				facts.getFirst().setQMIN(new Double(1));
			}
		}
		else
		{
			if(!facts.isEmpty())
				{
				facts.getFirst().setQMAX(new Double(0));
				facts.getFirst().setQMIN(new Double(0));
				}
		}
		return false;
	}
	 */


	public void checkCoherence(boolean force) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		// TODO Auto-generated method stub
		
	}

	/* Adscribe el ido a una clase y con el level indicado, creando un fact individual si no es un filtro*/
	public void registerInfoObject(int ido,int idto,int level) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		this.addIDOToIDTOWithLevel(ido, idto, level);
		if(level==Constants.LEVEL_PROTOTYPE || level==Constants.LEVEL_INDIVIDUAL)
			this.insertIndividual(ido);
	}
	
	public void deleteInfoObject(int ido,int idto,String rdn) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		//removeFromMaps(ido);//No lo quitamos de los mapas ya que hasta que no sea eliminado del todo nos podria interesar de que tipo es
		Individual ind=this.getRuleEngine().getIndividualFact(ido);
		Lock lock=null;
		if(ind==null){
			Integer level=getLevelOf(ido);
			if(level==null)
				level=Constants.LEVEL_INDIVIDUAL;
			
			ind= new Individual(ido,idto,rdn,getClassName(idto),level,this);
			lock=new Lock(ido,idto,getClassName(idto),level,this);
			this.getRuleEngine().insertFact(ind);
			this.getRuleEngine().insertFact(lock);
		}else{
			lock=this.getRuleEngine().getLockFact(ido);
		}
		
		ind.setSTATE(Constants.INDIVIDUAL_STATE_DELETED);
		lock.setSTATE(Constants.INDIVIDUAL_STATE_DELETED);
		
		ruleEngineRun();
	}
	
	public void isolatedInfoObject(int ido,boolean isolated) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		//removeFromMaps(ido);//No lo quitamos de los mapas ya que hasta que no sea eliminado del todo nos podria interesar de que tipo es
		Individual ind=this.getRuleEngine().getIndividualFact(ido);
		if(ind!=null)
			ind.setISOLATED(isolated);
	}
	
	private void insertIndividual(int ido) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException
	{
		Individual i = this.ruleEngine.getIndividualFact(ido);
		if(i==null){
			int idto=getClassOf(ido);
			String className=getClassName(ido,true);
			int level=getLevelOf(ido);
			Individual f = new Individual(ido,idto,getRdnIfExistInRuler(ido),className,level,this);
			Lock l=new Lock(ido,idto,className,level,this);
			this.getRuleEngine().insertFact(f);
			this.getRuleEngine().insertFact(l);
			f.addPropertyChangeListener(new PropertyChangeListener(){

				public void propertyChange(PropertyChangeEvent pce) {
					//Si se hace rollback de una sesion para el borrado de un individuo habria que volver a registrar la info del objeto
					if(pce.getPropertyName().equals("STATE")){
						if(pce.getOldValue().equals(Constants.INDIVIDUAL_STATE_DELETED)){
							Individual ind=(Individual)pce.getSource();
							//System.err.println("Ind recuperado:"+ind);
							addIDOToIDTOWithLevel(ind.getIDO(), ind.getIDTO(), ind.getLEVEL());
						}
					}
				}
				
			});
		}else{
			System.err.println("WARNING ddm.insertIndividual(ido= "+ido+") que ya est� en motor");
		}
		
	}
	
	public synchronized void setLock(int ido, boolean lock, String user, Session sessionPadre) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		DefaultSession sess = new DefaultSession(this, sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
		
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			SessionController.getInstance().setActual(sess,this);
			
			setLock(ido,lock,user,true);
			sess.sendPendingLocks();
			// si todo ha ido bien, commit
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.setLock cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.setLock no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
	}
	
	public synchronized void setLock(ArrayList<Integer> idos, boolean lock, String user, Session sessionPadre) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		DefaultSession sess = new DefaultSession(this, sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
		
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
			SessionController.getInstance().setActual(sess,this);
			for(Integer ido:idos){
				setLock(ido,lock,user,true);
			}
			sess.sendPendingLocks();
			// si todo ha ido bien, commit
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.setLock cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.setLock no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
	}

	public void setLock(int ido, boolean lock, String user, boolean allowLockDB) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException {
		Lock l = this.ruleEngine.getLockFact(ido);
		if(l!=null){
			//Cambiamos userChangedState cuando realmente se hace el bloqueo o el desbloqueo. Ademas, si no se hace pero el usuario NO es el sistema tambien lo cambiamos
			if(lock){
				if(l.getSTATE().equals(Constants.INDIVIDUAL_STATE_READY)){
					l.setUSERCHANGEDSTATE(user);
					//if(!user.equals(Constants.USER_SYSTEM)){
						setState(l,Constants.INDIVIDUAL_STATE_INIT_LOCK,allowLockDB,user);
					//}
					l.setUSERCHANGEDSTATE(user);
					setState(l,Constants.INDIVIDUAL_STATE_LOCK,allowLockDB,user);
					//System.err.println("SetLock: Bloqueado individual:"+ido+" por user:"+i.getUSERCHANGEDSTATE());
				}else{
					if(!user.equals(Constants.USER_SYSTEM)){
						l.setUSERCHANGEDSTATE(user);
					}
					//System.err.println("WARNING:Intento de bloqueo de "+ido+" sin efecto por user:"+user+" porque el estado del individuo es "+i.getSTATE());
					ruleEngineRun();
				}
			}else if(l.getSTATE().equals(Constants.INDIVIDUAL_STATE_LOCK)){
				l.setUSERCHANGEDSTATE(user);
				//if(!user.equals(Constants.USER_SYSTEM)){
					setState(l,Constants.INDIVIDUAL_STATE_END_LOCK,allowLockDB,user);
				//}
				l.setUSERCHANGEDSTATE(user);
				setState(l,Constants.INDIVIDUAL_STATE_READY,allowLockDB,user);
				//System.err.println("SetLock: Desbloqueado individual:"+ido+" por user:"+i.getUSERCHANGEDSTATE());
			}else{
				if(!user.equals(Constants.USER_SYSTEM)){
					l.setUSERCHANGEDSTATE(user);
				}
				System.err.println("WARNING:Intento de desbloqueo de "+ido+" sin efecto por user:"+user+" porque el estado del individuo es "+l.getSTATE());
				ruleEngineRun();
			}
		}else{
			NotFoundException exception=new NotFoundException("No se encuentra en motor el individuo "+ido+" con level:"+getLevelOf(ido));
			exception.setUserMessage("No se encuentra en motor el individuo");
			throw exception;
		}
	}
	
	private void setState(Lock l, String individualState, boolean allowlockDB, String user) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException {
		//System.err.println("setSTATE:individualState:"+individualState+" siendo individual:"+i);
		if(existInMotor(l.getIDO())){
			l.setSTATE(individualState);
			if(individualState.equals(Constants.INDIVIDUAL_STATE_LOCK)){
				// Si se trata de un individuo tenemos que bloquearlo en base de datos. El metodo de la sesion se encarga de comprobar si ya lo tenemos bloqueado de una anterior modificacion
				// Para desbloquearlo de base de datos ya se encargan las sesiones.
				if(getLevelOf(l.getIDO())==Constants.LEVEL_INDIVIDUAL && allowlockDB && !Auxiliar.equals(user, Constants.USER_SYSTEM))
					SessionController.getInstance().getActualSession(this).lockObject(l.getIDO(),l.getIDTO());
			}
			ruleEngineRun();
		}else{
			System.err.println("WARNING: Intento de setState "+individualState+" de un ido:"+l.getIDO()+" que no existe en motor");
		}
		
	}

	
	public String getIndividualState(int ido){
		String state=null;
		Individual i = this.ruleEngine.getIndividualFact(ido);
		if(i!=null){
			state=i.getSTATE();
		}
		
		return state;
	}
	
	public String getLockState(int ido){
		String state=null;
		Lock l = this.ruleEngine.getLockFact(ido);
		if(l!=null){
			state=l.getSTATE();
		}
		
		return state;
	}
	
	public Boolean isIsolated(int ido){
		Boolean isolated=null;
		Individual i = this.ruleEngine.getIndividualFact(ido);
		if(i!=null){
			isolated=i.isISOLATED();
		}
		
		return isolated;
	}
	

	
	
	public HashSet<Integer> getFromServer(int ido, int idto, Integer userRol, String user, Integer usertask, boolean crearFiltros, int depth, boolean lock, boolean lastStructLevel,boolean needSomeResult) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {		
		int nfacts=0;
		 HashSet<Integer> result=null;
		 if(ido<0) return result;
		 
		this.checkRangesIdoIdto(ido, idto);
		//Long inicio,fin; 
		//inicio=System.currentTimeMillis();
		
		
		if(ido<0){
			System.err.println("\n WARNING LLAMADA A GETFROMSERVER INICIO CON IDO NEGATIVO, TRAZA LLAMADAS");
			Auxiliar.printCurrentStackTrace();
		}
		if(this.existInMotor(ido)){
			//System.err.println("\n\n   WARNING: SE EST� LLAMANDO A GETFROMSERVER ID="+id+"  QUE YA EXISTE EN MOTOR");
			result= null;
		}
		else{
			ArrayList<IPropertyDef> lfactsserver=null;
			lfactsserver=server.serverGetFactsInstance(ido, idto,this.getUser(), lock/*SessionController.getInstance().getActualSession(this).isLockObjects()*/, depth,lastStructLevel, needSomeResult).getAIPropertyDef();
			if(lock){
				Session sess=SessionController.getInstance().getActualSession(this);
				HashMap<Integer, HashSet<Integer>> locks=new HashMap<Integer, HashSet<Integer>>();
				HashSet<Integer> idos=new HashSet<Integer>();
				idos.add(ido);
				locks.put(idto, idos);
				sess.addLocks(locks);
			}
			//System.err.println("\n DEBUG LLAMADA GETFROMSERVER. ido:"+ido+" idto:"+idto+" user:"+user+" PROFUNDIDAD:"+depth+"  N.FACTS:"+lfactsserver.size());
			//System.err.println("\n lfactsserver:\n"+this.agruparValoresIndividuos(lfactsserver));
			//System.err.println("\n DEBUG TRAZA LLAMADAS A GETFROMSERVER int ido,..");
			//Auxiliar.printCurrentStackTrace();
			if(lfactsserver.size()==0){
				System.err.println("\n  WARNING getFromServer(ido="+ido+", idto="+idto+") no encuentra nada en server");
				Auxiliar.printCurrentStackTrace();
			}
			if(lfactsserver!=null&&lfactsserver.size()>0){
				result=this.loadNewData(lfactsserver, userRol, user, usertask,crearFiltros,false,false);
			}
			else{
				result= null;
			}
		}
		//fin=System.currentTimeMillis();
		//Double tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		//System.err.println("\n TIME miliSeconds getFromServer ido:"+ido+" tiempoMsecons"+tiempos);
		return result;
	}
	
	/**
	 * 
	 * @param idtoIdos Mapa que contiene pares de: <identificador de la clase, conjunto de los individuos de la clase>
	 * @param userRol
	 * @param user
	 * @param usertask
	 * @param crearFiltros
	 * @param depth
	 * @param lock
	 * @param lastStructLevel
	 * @param needSomeResult
	 * @return
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws dynagent.common.exceptions.CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws OperationNotPermitedException
	 * @throws DataErrorException
	 * @throws ParseException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 */

	
public HashSet<Integer> getFromServer(HashMap<Integer,HashSet<Integer>> idtoIdos, Integer userRol, String user, Integer usertask, boolean crearFiltros, int depth, boolean lock, boolean lastStructLevel,
		boolean needSomeResult,boolean requeridaCoherencia,boolean descartaIncoherente) throws 

	NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, 

	dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, 

	ParseException, SQLException, NamingException, JDOMException {
				
//	System.err.println("debug DDM.getFromServer inicio llamada: idtoIdos:"+idtoIdos);		
	ArrayList<IPropertyDef> lfactsserver=null;	
			HashSet<Integer> result=null;
			Long inicio,fin; 
			inicio=System.currentTimeMillis();
			lfactsserver=server.serverGetFactsInstance(idtoIdos, this.getUser(), lock, depth, lastStructLevel, needSomeResult).getAIPropertyDef();// A DOCSERVER HAY QUE PASARLE EL USUARIO LOGADO EN DDM
			if(lock){
				Session sess=SessionController.getInstance().getActualSession(this);
				sess.addLocks(idtoIdos);
			}
//			 System.err.println("debug DDM.getFromServer server devuelve numero facts:\n"+lfactsserver.size());
			//System.err.println("\n lfactsserver:\n"+this.agruparValoresIndividuos(lfactsserver));
			//System.err.println("\n DEBUG TRAZA LLAMADAS A GETFROMSERVER int ido,..");
			//Auxiliar.printCurrentStackTrace();
			 if(lfactsserver!=null&&lfactsserver.size()>0){
				result= this.loadNewData(lfactsserver, userRol, user, usertask,crearFiltros,requeridaCoherencia,descartaIncoherente);
			}
			else{
				result=null;
			}
			fin=System.currentTimeMillis();
			long tiempos=(fin-inicio);
			//System.err.println("\n TIME miliSeconds getFromServer idtoIdos:"+idtoIdos+" tiempoMsecons"+tiempos);
			return result;
	}
	
	
	 public HashSet<Integer> getFromServer(Element xmlquery, Integer userRol,String user,Integer usertask,boolean crearFiltros) throws  NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 HashSet<Integer> result=new HashSet<Integer>();
		 ArrayList<IPropertyDef> lfactsserver=null;	
		lfactsserver=server.serverGetFactsInstanceOfQuery(xmlquery, usertask).getAIPropertyDef();// A DOCSERVER HAY QUE PASARLE EL USUARIO LOGADO EN DDM
		//System.err.println("\n DEBUG  getFromServer obtiene numero facts: "+lfactsserver.size()+"\n FACTSERVER:\n "+lfactsserver);		
		
		if(lfactsserver!=null&&lfactsserver.size()>0){
			//System.err.println("\n lfactsserver:\n"+this.agruparValoresIndividuos(lfactsserver));
			result=this.loadNewData(lfactsserver,userRol , user, usertask,crearFiltros,false,false);
		}else{
			//System.err.println("\n lfactsserver:\n"+lfactsserver);
			
			
		}
		return result;
	}
	
	
	public HashSet<Integer> loadNewData(ArrayList<IPropertyDef>instances, Integer userRol, String user,Integer usertask,boolean crearFiltros,boolean requeridaCoherencia,boolean descartaIncoherente) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		/*System.out.println("Antes de loadNewData:");
		 getRuleEngine().getMotorSize();*/
		
		//System.err.println("\n DEBUG INICIO loadNewData:");		
		LinkedList<dynagent.common.knowledge.IPropertyDef> factsToRuler = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
		 HashSet<Integer>idsIndividuos=new HashSet<Integer>();
		 HashSet<Integer>idsYaExistianEnMotor=new HashSet<Integer>();
		 HashMap<Integer,Integer> idtoxido=new HashMap<Integer,Integer>();
		 ArrayList<IPropertyDef> instancesToInsert=new ArrayList<IPropertyDef>();
		 
		 if(!instances.isEmpty()){
			 for(int i=0;i<instances.size();i++){
				 FactInstance instance=(FactInstance)instances.get(i);
				 if(instance.getIDO()==7945){
					 System.err.println("DBG INSTANCE "+instance);
				 }
				 //Si viene alguna clase o property excluida no la insertamos en el motor
				 if(metaData.getHmPropiedades().get(instance.getPROP())==null || !hmNameClassXIdto.containsKey(instance.getIDTO()) || (!Constants.isDataType(instance.getVALUECLS()) && !hmNameClassXIdto.containsKey(instance.getVALUECLS()))){
					 //System.err.println("loadNewData. Excluido:"+instance);
					 continue;
				 }
					
 				 if(instance.getIDO()!=null && !existInMotor(instance.getIDO())){
					 if(instance.getOP()==null&&instance.getIDO()!=null){
						 idsIndividuos.add(instance.getIDO());
						 //IMPORTANTE  ASIGNARLE EL RANGENAME Y EL CLASSNAME PQ EL SERVER NO SIEMPRE LOS ASIGNA
						 if(instance.getCLASSNAME()==null){
							// System.err.println("\n\n ========= WARNING WARNING ----------: loadNewData recibe Fact que viene de bbdd no tiene classname:"+instance);
							 instance.setCLASSNAME(this.getClassName(instance.getIDTO()));
						 }
						 if(this.isDataProperty(instance.getPROP())){
							 instance.setRANGENAME(Constants.getDatatype(instance.getVALUECLS()));
						 }
						 else{
							 instance.setRANGENAME(this.getClassName(instance.getVALUECLS()));
						 }
						 
						 //mapeamos su rdn TODO No se deberia mapear esto ya que si cambia luego no es cambiado en el mapa
						 if(instance.getPROP()==Constants.IdPROP_RDN){
							 this.hmRdnxIDO.put(instance.getIDO(), instance.getVALUE());
						 }
					 }
					 if(!idtoxido.containsKey(instance.getIDO())){
						 idtoxido.put(instance.getIDO(),instance.getIDTO());
					 }
					 instancesToInsert.add(instance);
					 
					 
/*//debuG
					 int idoent=instance.getIDO();
					 String claseDebug=this.getClassName(instance.getIDTO());
					 if(claseDebug.contains("STOCK")){
						 System.err.println("\n DEBUG LOADNEWDATA VA A CARGAR STOCK EN MOTOR  IDO="+idoent);
Auxiliar.printCurrentStackTrace();

 }
*/
					 
					 
					 
				 }else{
					 idsYaExistianEnMotor.add(instance.getIDO());
					 //System.err.println("\n DEBUG: loadNewData no cargarara ido:"+instance.getIDO()+" porque ya est� en motor: "+this.getInfoIndividuoEnMotor(instance.getIDO()));					 
					 
				 }
			 }
			 
			 if(idsYaExistianEnMotor.size()>0){
				 //System.err.println("\n\n ===============INFO!! ddm.loadNewDAta No va a cargar en motor factinstances de ids="+ idsYaExistianEnMotor+"  porque ya existen en motor");
				 /*DEBUG Iterator itEncontrados=idsYaExistianEnMotor.iterator();
				 while(itEncontrados.hasNext()){
					 int idoent=(Integer)itEncontrados.next();
					 String claseDebug=this.getClassName(idoent);
					 if(claseDebug.contains("STOCK")){
						 System.err.println("\n DEBUG LOADNEWDATA NO CARGA FACTS STOCK IDO="+idoent+" porque ya existe en motor, sus facts en motor:\n"+this.getInfoIndividuoEnMotor(idoent));
					 }
					 
				 }*/								 				
			 }
			 
			 //loadIndividual No cargar (descartar) individuos que les falta alguna propiedad obligatoria, ya sea corruptos o porque vienen como referencias en menu externo
			 if(descartaIncoherente && !getGlobalUtasks().contains(usertask) && SessionController.getInstance().getActualSession(this).isRunRules()){
				 //puede haber campos obligatorios en el modelo local, excluido en el modelo externo
				 Iterator<Integer> ids=idsIndividuos.iterator();
				 ArrayList<Integer> idosIncompleted=new ArrayList<Integer>();
				 while(ids.hasNext()){
					 Integer ido=ids.next();
					 int idto=QueryConstants.getIdto(ido);
					 HashSet<Integer> props=this.getAllIDsPropertiesOfClass(idto);
					 Iterator<Integer> itprops=props.iterator();
					 while(itprops.hasNext()){
						 int idprop=itprops.next();
						 Property p=getProperty(null,idto, idprop, userRol, user, usertask, false);
						 boolean existeRequired=false;
						 if(p.cardMin!=null&&p.cardMin>0){
							 for(IPropertyDef ipd:instances){
								 if(ipd.getIDO().equals(ido)&&idprop==ipd.getPROP()){								 
									 existeRequired=true;
									 break;
								 }
							 }
							 if(!existeRequired){
								 if(ido==68803122){
								 System.err.println("DESCARTAR IDO:"+ido+" para prop:"+idprop+" utask:"+usertask);
								 }
								 if(requeridaCoherencia){
									 throw new OperationNotPermitedException("DESCARTAR IDO:"+ido+" para prop:"+idprop+" utask:"+usertask);
								 }
								 idosIncompleted.add(ido);
							 }
						 }
					 }
				 }
				 for(Integer ido:idosIncompleted){
					 idsIndividuos.remove(ido);
					 for(IPropertyDef ipd:instances){
						 if(ipd.getIDO().equals(ido)){
							 instancesToInsert.remove(ipd);
						 }
					 }
				 }
			}

			 factsToRuler.addAll(instancesToInsert);
			 for(Iterator<Integer> it=idsIndividuos.iterator();it.hasNext();){
				 int ido=it.next();
				 int idto=idtoxido.get(ido);
				 if(crearFiltros){
					 HashSet<Integer> props=this.getAllIDsPropertiesOfClass(idto);
					 Iterator<Integer> itprops=props.iterator();
					 while(itprops.hasNext()){
						 int idprop=itprops.next();
						
						 if(this.isObjectProperty(idprop)){//Creaci�n filtros necesarios	
							 ObjectProperty p=(ObjectProperty)this.getProperty(null,idto, idprop, userRol, user, usertask, false);
							 String nameClass=this.getClassName(idto);
		
							 for(int i=0;i<p.getRangoList().size();i++){
								 int range=p.getRangoList().get(i).intValue();
								 int idorango=this.createPrototype(range,Constants.LEVEL_FILTER,userRol, user,usertask,/*Ponemos false porque solo queremos este nivel que ya es filtro*/false);
								 //Lo enlazamos con el filtro creado.
								 FactInstance enlaceafiltro=new FactInstance(idto,ido,idprop,String.valueOf(idorango),range,this.getClassName(range),null,null,Constants.OP_UNION,nameClass);
								 factsToRuler.add(enlaceafiltro);
							 }
						 }
					 }
				 }
			 }	
			 //Registramos los datos de los objetos y a�adimos los facts al final para evitar que se disparen reglas antes de que est� todo cargado
			 Iterator<Integer> itrIndividual=idsIndividuos.iterator();
			 while(itrIndividual.hasNext()){
				 Integer ido=itrIndividual.next();
				 // adscribimos el individuos a su clase y level
				 if(ido==7945){
					 System.err.println("DBG INSTANCE "+ido);
				 }
				 registerInfoObject(ido, idtoxido.get(ido),  Constants.LEVEL_INDIVIDUAL);
			 }
			 boolean success=false;
			 try{
				 
				 //System.err.println("\n DEBUG loadnewdata numero de factsToRuler:"+factsToRuler.size());				 
				 this.addFactsToRuler(factsToRuler);
				 success=true;
			 }finally{
				 if(!success){
					 Iterator<Integer> itrInd=idsIndividuos.iterator();
					 while(itrInd.hasNext()){
						 int ido=itrInd.next();
						 // Si no se han llegado a insertar fact borramos la informacion del objeto. Si tiene facts ya se encarga la sesion al hacer rollback
						 if(getRuleEngine().getInstanceFactsWhereIdo(ido).isEmpty()){
							 retractInfoObject(ido, false, true);
						 }
					 }
				 }
			 }
		 }
		 return idsIndividuos;
	 }
	 
	 public void loadFromServerIfNotExist(int ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		 if(!this.existInMotor(ido)){
			 this.getFromServer(ido,idto,1, null, Constants.USER_SYSTEM, null);
		 }
	 }
	 
		 
	 public ArrayList<Integer> getIdosNoFiltersOfClass(int idto){
		 ArrayList<Integer> result=new ArrayList<Integer>(); 
		 Iterator<Integer>  allidos=this.getHmIDOSxIDTO().get(idto).iterator();
		 while (allidos.hasNext()){
			 int ido=allidos.next();
			 if(this.getLevelOf(ido).intValue()!=Constants.LEVEL_FILTER){
				 result.add(ido);
			 } 
		 }
		 return result; 
	 }
	 
	 public ArrayList<Integer> getIdosNoFiltersOfClass(String nameclass) throws NotFoundException, IncoherenceInMotorException {
		 return this.getIdosNoFiltersOfClass(this.getIdClass(nameclass)); 
	 }

	public Integer getAtributteGroup(int idProp) {
		// TODO Auto-generated method stub
		return null;
	}
		
	
	public boolean isSpecialized(String  nameclase,  String namePosSuperior) throws NotFoundException, IncoherenceInMotorException  {
		return this.isSpecialized(this.getIdClass(nameclase), this.getIdClass(namePosSuperior));
	}
	
	private LinkedList<IPropertyDef> getInstanceFacts(int ido,int idProp,String value,int valueCls){
		LinkedList<IPropertyDef> list=new LinkedList<IPropertyDef>();
		if(value!=null)
			list=ruleEngine.getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls(ido, idProp, value, valueCls);
		else list=ruleEngine.getInstanceFactsWhereIdoAndIdPropAndValueNullAndValueCls(ido, idProp, valueCls);
		
		return list;
	}
	
	
	public synchronized void loadIndividual(int ido,int idto, int depth, boolean lock, boolean lastStructLevel, Integer userRol,String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
	
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
//			System.out.println("DENTRO DE getPropertyIterator public");
			SessionController.getInstance().setActual(sess,this);
			getFromServer(ido,idto, userRol, user, usertask, true, depth, lock,lastStructLevel,true);
			// si todo ha ido bien. commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.loadIndividual cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.loadIndividual no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
	} 
	
	public synchronized void loadIndividual(HashMap<Integer,HashSet<Integer>> idtoIdos, int depth, boolean lock, boolean lastStructLevel, Integer userRol,String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
	
		Session oldSession =SessionController.getInstance().getActualSession(this);
		boolean success=false;
		try {
//			System.out.println("DENTRO DE getPropertyIterator public");
			SessionController.getInstance().setActual(sess,this);
			//System.err.println("\n DEBUG DDM.loadIndividual CON SESIONES: idtoIdos:"+idtoIdos);
		

			
			//NO SUBIR, CAMBIO POR FALLO EN GUI PARA PODER SEGUIR CON PRUEBAS:			
			//getFromServer(idtoIdos, userRol, user, usertask, true, depth, false, lastStructLevel,true);
			getFromServer(idtoIdos, userRol, user, usertask, true, depth, lock, lastStructLevel,false,false,false);





			// si todo ha ido bien. commit.
			ruleEngineRun();
			sess.sendPendingLocks();
			sess.commit();
			success=true;
		}finally{
			if(!success){
				try{
					sess.rollBack();//cancelSession(sess);
					System.err.println("WARNING:Sesion interna de DocDataModel.loadIndividual cancelada");
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.loadIndividual no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}else SessionController.getInstance().setActual(oldSession,this);
		}
	}
	
	
		
	
		public boolean isStructural(int idProp) throws NotFoundException{
			return this.getCategory(idProp).isStructural();
		}
		
	public boolean isStructuralExclusive(int idoRoot, int idProp, String valor,int valueCls) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		boolean result =false;
		if(this.getCategory(idProp).isStructural()){
			//si NO tiene inversa-->es estrucural exclusiva
			Integer idpropInv=this.getPropertyInverse(idProp);
			if(idpropInv==null){
				result=true;
			}else{
				//aunque tenga una inversa, puede ocurrir que esta instancia concreta de valorEstructural solo este siendo usado desde un unico root (la inversa lo dir�)
				PropertyValue pv=this.SystemGetPropertyValue(new Integer(valor),valueCls, idpropInv);
				if(pv==null){
					System.err.println("\n\n ==== WARNING isStructuralExclusive: propertyInversa null. idorrot="+idoRoot+" idtoRoot:"+this.getClassOf(idoRoot)+" idProp="+idProp+"  valor="+valor+" valueCls:"+valueCls+"\n  propertyInversa:"+pv);
				}
				LinkedList<Value> valoresDeLaEstructura = pv.getValues();
				
				if(valoresDeLaEstructura.size()>1){
						result=false;	
				}
				else if(valoresDeLaEstructura.size()==1){
					result = false;
					Value value=valoresDeLaEstructura.get(0);
						if(value.getValue_s().equals(String.valueOf(idoRoot))){
							result = true;
						}else{
							System.err.println("\n\n ================ WARNING\n======= WARNING ====WARNING: caso no espeerado 1 en DDM.isStructuralExclusive() idorrot="+idoRoot+" idtoRoot:"+this.getClassOf(idoRoot)+" idProp="+idProp+"  valor="+valor+" valueCls:"+valueCls+"\n  propertyInversa:"+pv+ " valor inv:"+value);
							result=false;
						}
				}
				else if(valoresDeLaEstructura.size()==0){
					 result =true;
				}
			}
		}
		
	return result;
	}
		
		public LinkedList<Integer> getRangeOfObjectPropertyInClass(int idtodominio,int idProp){
			LinkedList<Integer> rango=null;
			if(this.isObjectProperty(idProp)){
				ObjectProperty op=(ObjectProperty)this.getMetaData().getClase(idtodominio).getProperty(idProp);
				rango=op.getRangoList();
			}else{
				System.err.println("\n\n:=======DDM.getRangeOfObjectPropertyInClass ERROR: SOLO ADMITE OBJECTPROPERTY");
			}
			return rango;
		}
		
		/**
		 * Solo se debe usar cuando se sepa que el rango es �nico, si hay un rango compuesto devuelve
		 * null
		 * @param idtodominio
		 * @param idProp
		 * @return
		 */public Integer  getRangeOfObjectProperty(int idtodominio,int idProp){
			LinkedList<Integer> rango=null;
			if(this.isObjectProperty(idProp)){
				ObjectProperty op=(ObjectProperty)this.getMetaData().getClase(idtodominio).getProperty(idProp);
				rango=op.getRangoList();
			}else{
				System.err.println("\n\n:=======ERROR. ERROR:  ERROR DE PROGRAMACION. ddm.getRangeOfObjectPropertyInClass SOLO ADMITE OBJECTPROPERTY");
			}
			if(rango.size()==1)
				return rango.getFirst();
			else return null;
		}
		 
		 public Integer  getRangeOfObjectProperty(String nameClass,String nameProp) throws NotFoundException, IncoherenceInMotorException{
			 	return this.getRangeOfObjectProperty(this.getIdClass(nameClass), this.getIdProperty(nameProp));
		 
		 }
		
		
		public LinkedList<Integer> getRangeOfObjectPropertyInClass(String nameclassdominio,String nameProp) throws NotFoundException, IncoherenceInMotorException{
			return this.getRangeOfObjectPropertyInClass(this.getIdClass(nameclassdominio), this.getIdProperty(nameProp));
		}
		
	
		
		public boolean printRule(String message){
			if(printRules){
				System.err.println(message);
			}
			
			if(debugLog!=null){
				Exception e=new Exception();
				StackTraceElement[] stctraza=e.getStackTrace();
				boolean stop=false;
				String ruleName=null;
				for(int i=0;i<stctraza.length&&!stop;i++){
					stop=stctraza[i].getFileName().startsWith("Rule_");
					if(stop){
						String aux=stctraza[i].getFileName();
						String []v=aux.split("_0.java");
						aux=v[0];
						v=aux.split("Rule_");
						ruleName=v[1];
					}
				}
				int a=0;
				debugLog.addDebugData(DebugLog.DEBUG_RULES, "fire", message);
				if(ruleName!=null)
					debugLog.addRuleFired(ruleName);
			}
			
			//System.err.println("<<< RULENAME="+ruleName+"\n"+message);
			
			return true;
		}
		
	

		

		public void setDestination(int ido,int idProp,String destination) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
			 Iterator it = this.getFactsPropertyIterator(ido, idProp);
			  if (it.hasNext()) {
				 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it.next();
				 ruleEngine.setFactWithDestination(fact, destination);
			 }else{
				 System.err.println("ERROR:No se ha asignado destination al fact al no existir este en setDestination siendo ido:"+ido+" idProp:"+idProp+" destination:"+destination);
			 }
		}
		
		public void setDestination(String ido,String nameProp,String destination) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
			
				int iido=new Integer(ido);
				int iidprop=getIdProperty(nameProp);
				setDestination(iido,iidprop,destination);
			}
		
		public Integer getIdPropertyNotException(String nameProp) throws IncoherenceInMotorException{
			Integer idprop=null;
			if(this.getMetaData()!=null){
				idprop=this.getMetaData().getIdProp(nameProp);	
			}
			else{
				try {
					idprop=this.getIdProperty(nameProp);
				} catch (NotFoundException e) {
					System.err.println("\n\n ===== WARNING WARNING:ddm.getIdPropertysNotException se ha llamado con una propiedad que no existe: propiedad="+nameProp);
				
				}
			}
			return idprop;
		}

		public ArrayList<EssentialProperty> getEssentialProperties() {
			return essentialProperties;
		}

		public void setEssentialProperties(ArrayList<EssentialProperty> essentialProperties) {
			this.essentialProperties = essentialProperties;
		}
		
		public ArrayList<ListenerUtask> getListenerUtasks() {
			return listenerUtasks;
		}

		public void setListenerUtasks(ArrayList<ListenerUtask> listenerUtasks) {
			this.listenerUtasks = listenerUtasks;
		}
		
		public String getAliasOfGroup(int group, String nameGroup, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
			return this.alias.getLabelGroup(group, nameGroup, usertask);
		}
		
		public String getAliasOfProperty(int idto, int idProp, Integer usertask) throws NotFoundException{
			return this.alias.getLabelProp(idProp,idto,null, usertask);
		}
		
		public String getAliasOfClass(int idto,Integer usertask) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
			if(isSpecialized(idto, Constants.IDTO_UTASK))
				return this.alias.getLabelUtask(idto);
			else return this.alias.getLabelClass(idto,usertask);
		}
		
		public String getAliasOfProperty(int idto, int idProp) throws NotFoundException{
			return getAliasOfProperty(idto,idProp,SessionController.getInstance().getActualSession(this).getUtask());
		}
		
		public String getAliasOfClass(int idto) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
			return getAliasOfClass(idto,SessionController.getInstance().getActualSession(this).getUtask());
		}
		
		public String getAliasOfProperty(String nameClass, String nameProp) throws NotFoundException, IncoherenceInMotorException{
			return getAliasOfProperty(this.getIdClass(nameClass),this.getIdProperty(nameProp));
		}
		
		public String getAliasOfClass(String nameClass) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
			return getAliasOfClass(this.getIdClass(nameClass));
		}

		public synchronized void setValue(int ido,int idto, int idProp, LinkedList<Value> oldValues, LinkedList<Value> newValues, Integer userRol,
				String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{

			DefaultSession sess = new DefaultSession(this,sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);

			Session oldSession =SessionController.getInstance().getActualSession(this);
			boolean success=false;
			try {
				//System.err.println("ido:"+ido+" idProp:"+idProp+" oldVal:"+oldVal+" newVal:"+newVal);
				SessionController.getInstance().setActual(sess,this);
				setValue(ido, idto,idProp, oldValues, newValues, userRol, user, usertask);
				// al final hacer un commit. si falla el commit -> lanzar una
				// excepcion para deshacer lo que estaba haciendo el commit.
				ruleEngineRun();
				sess.sendPendingLocks();
				sess.commit();
				success=true;
			}finally{
				
				if(!success){

					try{
						sess.rollBack();//cancelSession(sess);
						System.err.println("WARNING:Sesion interna de DocDataModel.setValue cancelada");
					} catch (Exception e) {
						System.err.println("WARNING:Sesion interna de DocDataModel.setValue no ha podido cancelarse");
						e.printStackTrace();
					}finally{
						SessionController.getInstance().setActual(oldSession,this);
					}
				}else SessionController.getInstance().setActual(oldSession,this);
				
			}

		}


		public void setValue(int ido, int idto,int idProp, LinkedList<Value> oldValues, LinkedList<Value> newValues, Integer userRol,String user, Integer usertask)
		throws CardinalityExceedException,OperationNotPermitedException, IncompatibleValueException,NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException {
			//System.err.println("setValue multiple: newValues:"+newValues+" oldValues:"+oldValues);
			int sizeOld=0;
			int sizeNew=0;
			if(oldValues!=null)
				sizeOld=oldValues.size();
			if(newValues!=null)
				sizeNew=newValues.size();
			
			int size=Math.max(sizeOld, sizeNew);
			for(int i=0;i<size;i++){
				Value oldVal=null;
				Value newVal=null;
				
				if(i<sizeOld)
					oldVal=oldValues.get(i);
				if(i<sizeNew)
					newVal=newValues.get(i);
				setValue(ido,idto, idProp, oldVal, newVal, userRol, user, usertask, false);
			}
		}
		
		public Integer getGenericFilter(int id){
			int idto=getClassOf(id);
			return hmGenericFilterXIdto.get(idto);
		}
		
		public boolean isGenericFilter(int ido){
			int idto=getClassOf(ido);
			Integer idoFilter=hmGenericFilterXIdto.get(idto);
			
			return idoFilter!=null && idoFilter==ido;
		}

		public boolean isPointed(int ido, int idto) throws NotFoundException, IncoherenceInMotorException {
			String idvalue = String.valueOf(ido);
			Integer idtovalue = Integer.valueOf(idto);
			return !ruleEngine.getAllInstanceFacts(null, null,null, idtovalue, idvalue, null, null, null, null).isEmpty();
		}
			
//		public boolean isACTIVE(){
//			return ACTIVE;
//		}
//		
//		public void setACTIVE(boolean active){
//			System.out.println("SETACTIVE:"+active);
//			boolean old=ACTIVE;
//			ACTIVE=active;
//			System.err.println("old:"+old+" active:"+ACTIVE);
//			pcs.firePropertyChange("ACTIVE", old, ACTIVE);
//		}
//		
//		public FactHandle getFactHandle()
//		{
//			return factHandle;
//		}
//			
//		public void setFactHandle(FactHandle factHandle)
//		{
//			this.factHandle = factHandle;
//		}
//		
//		public void addPropertyChangeListener(PropertyChangeListener pcl) {
//			pcs.addPropertyChangeListener(pcl);
//		}
//
//		public void removePropertyChangeListener(PropertyChangeListener pcl) {
//			pcs.removePropertyChangeListener(pcl);
//		}

		public int getState() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		public void setGlobalOfRules(String name,Object value){
			getRuleEngine().setGlobal(name, value);
		}
		
	 
		
		
		//Comprueba si el individuo con ido es compatible con el instance instFilter
		public synchronized boolean isCompatibleWithFilter(int ido,instance instFilter,Integer userRol, String user, Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			boolean compatible = true;
			//Creamos una sesion por si hay que traerse algo de base de datos para terminar la comprobacion. Al terminar dejamos el motor como estaba haciendo rollback
			DefaultSession sess = new DefaultSession(this,getDefaultSession(),userTask,false,false,false,true,false);
		
			Session oldSession =SessionController.getInstance().getActualSession(this);
			try {
				SessionController.getInstance().setActual(sess,this);
				
				int idoOther=instFilter.getIDO();				
				compatible = processCompatibilityFilter(ido, this.getClassOf(ido),idoOther, instFilter, userRol, user, userTask, new HashSet<Integer>());
			}finally{
				try{
					sess.rollBack();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}
			
			return compatible;
		}
		
		private boolean processCompatibilityFilter(int idoThis,int idtoThis,int idoFilter,instance instFilter,Integer userRol, String user, Integer userTask, HashSet<Integer> listProcessedChildren) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			boolean compatible=true;
			//Primero comprobamos si en esta rama hay algo modificado en el filtro respecto a la clase original.
			HashSet<Integer> auxListProcessedChildren=new HashSet<Integer>();
			auxListProcessedChildren.addAll(listProcessedChildren);
			if(hasModifiedBranch(idoFilter,instFilter,userRol,user,userTask,auxListProcessedChildren)){
				listProcessedChildren.add(idoFilter);
				Iterator<Property> itr=instFilter.getAllPropertyIterator(idoFilter);
				while(compatible && itr.hasNext()){
					Property propertyFilter=itr.next();
					//System.err.println("propertyFilter:"+propertyFilter);
					Property prop=null;
					try{
						prop=getProperty(idoThis, idtoThis,propertyFilter.getIdProp(),userRol,user,userTask,false);
					}catch(NotFoundException e){
						System.err.println("WARNING: En processCompatibilityFilter el ido:"+idoThis+" no tiene la property "+propertyFilter.getName());
					}
					if(prop!=null){//Puede no tener la property si la configuracion la ha eliminado ya que idoThis puede ser un hijo de idoFilter
						compatible=!isCardinalityAssigned(propertyFilter, userRol, user, userTask) ||
									( (propertyFilter.getCardMin()==null || prop.getValues().size()>=propertyFilter.getCardMin()) && (propertyFilter.getCardMax()==null || prop.getValues().size()<=propertyFilter.getCardMax()) );					
						if(compatible){
							Iterator<Value> itrValues=prop.getValues().iterator();
							boolean someValueCompatible=false;
							while(!someValueCompatible && itrValues.hasNext()){
								//Solo necesitamos que uno de los valores cumpla el filtro, ya que se trata de un OR
								Value value=itrValues.next();
								someValueCompatible=value.checkIsCompatibleWithNotException(propertyFilter,this,userTask) && (propertyFilter.getValues().isEmpty() || propertyFilter.getValues().contains(value));
								if(someValueCompatible){
									if(value instanceof ObjectValue && propertyFilter.getEnums().isEmpty()/*No descendemos por los enumerados*/){
										int idoValue=((ObjectValue)value).getValue();
										int idtoValue=((ObjectValue)value).getValueCls();
										int idoRangeFilter=((ObjectProperty)propertyFilter).getRangoList().getFirst();
										if(!listProcessedChildren.contains(idoRangeFilter))
											someValueCompatible=processCompatibilityFilter(idoValue, idtoValue,idoRangeFilter, instFilter, userRol, user, userTask, listProcessedChildren);
									}
								}
							}
							//TODO Pensar si habr�a que usar isRestrictionAssigned
							if(!prop.getValues().isEmpty() || !propertyFilter.getValues().isEmpty())//Si el filtro o el individuo tienen valores nos fijamos en el valor someValueCompatible
								compatible=someValueCompatible;
						}
					}else{
						//Si no tiene la property y tiene algo fijado significa que no cumple el filtro
						compatible=!isRestrictionAssigned(propertyFilter, userRol, user, userTask);
						if(compatible && propertyFilter instanceof ObjectProperty && propertyFilter.getEnums().isEmpty()){
							int idoRangeFilter=((ObjectProperty)propertyFilter).getRangoList().getFirst();
							HashSet<Integer> auxListProcessedChildrenObjectProperty=new HashSet<Integer>();
							auxListProcessedChildrenObjectProperty.addAll(listProcessedChildren);
							compatible=!hasModifiedBranch(idoRangeFilter, instFilter, userRol, user, userTask, auxListProcessedChildrenObjectProperty);
						}
						
						//System.err.println("NO TIENE LA PROPERTY "+propertyFilter.getName()+". filterIsCompatible:"+compatible);
					}
				}
			}
			return compatible;
		}
		
		private boolean isCardinalityAssigned(Property property, Integer userRol, String user, Integer idtoUserTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			Property prop=getProperty(property.getIdo(),property.getIdto(), property.getIdProp(), userRol, user, idtoUserTask, false);
			return !Auxiliar.equals(prop.getCardMin(),property.getCardMin()) || !Auxiliar.equals(prop.getCardMax(),property.getCardMax());
		}
		
		/* Comprueba que la rama que comienza por idoFilter de instFilter tiene algo modificado respecto a la clase original. Si fuera igual que la clase no hay nada modificado*/
		private boolean hasModifiedBranch(int idoFilter,instance instFilter, Integer userRol, String user, Integer userTask, HashSet<Integer> listProcessedChildren) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			boolean modified=false;
			listProcessedChildren.add(idoFilter);
			Iterator<Property> itr=instFilter.getAllPropertyIterator(idoFilter);
			while(!modified && itr.hasNext()){
				Property propertyFilter=itr.next();
				//modified=isCardinalityAssigned(propertyFilter, userRol, user, userTask) || !propertyFilter.getValues().isEmpty();
				modified=isRestrictionAssigned(propertyFilter, userRol, user, userTask);
				if(!modified && propertyFilter instanceof ObjectProperty && propertyFilter.getEnums().isEmpty()){
					int idoRangeFilter=((ObjectProperty)propertyFilter).getRangoList().getFirst();
					if(!listProcessedChildren.contains(idoRangeFilter))
						modified=hasModifiedBranch(idoRangeFilter, instFilter, userRol, user, userTask, listProcessedChildren);
				}
			}
			
			return modified;
		}
		
		private boolean isRestrictionAssigned(Property property, Integer userRol, String user, Integer idtoUserTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			boolean restriction=false;
			//No pido la property a motor porque ya la tengo, ademas es llamado por propertyFilter que puede no estar en este motor ya que viene del instance, asi que nos basamos en la property recibida
			Property prop=property;//getProperty(property.getIdo(),property.getIdto(), property.getIdProp(), userRol, user, idtoUserTask, false);
			restriction=(!Auxiliar.equals(prop.getCardMin(),property.getCardMin()) || !Auxiliar.equals(prop.getCardMax(),property.getCardMax())) ||
					!prop.getValues().equals(property.getValues()) || !prop.getEnums().equals(property.getEnums()) || !prop.getExclus().equals(property.getExclus());
			
			if(!restriction && property instanceof DataProperty){
				restriction=!Auxiliar.equals(getPropertyMask(property.getIdProp(),property.getIdto(),idtoUserTask),getPropertyMask(prop.getIdProp(),prop.getIdto(),idtoUserTask));
			}
			/*if(restriction){
				System.err.println("Tiene restrictionnnnnnnnnnnnnn");
			}*/
			return restriction;
		}

		public Integer getPropertyLength(int prop, Integer idto, Integer utask) {
			return masks!=null?masks.getLength(prop, idto, utask):null;
		}

		public String getPropertyMask(int prop, Integer idto, Integer utask) throws NotFoundException {
			return masks!=null?masks.getExpression(prop, idto, utask):null;
		}

		public String getRulesGroup() {
			return this.session.getRulesGroup();
		}

		public void setRulesGroup(String rulesGroup) {
			this.session.setRulesGroup(rulesGroup);
		}	
		
				
			 
			 /*public void mapIDTOofIDO(String  value,int idto){
				 this.hmVALUECLSxVALUE.put(value,idto);
			 }
			 */
			 
			 
			
			 /*public Integer ruleAuxiliarGetIDTO(int ido) throws NotFoundException{
					Integer idto=null;
					if(this.getHmIDTOxIDO().get(ido)!=null){
						idto=this.getHmIDTOxIDO().get(ido);
					}
					else{
						idto=this.getVALUECLSxVALUE(String.valueOf(ido));
					}
					return idto;
				}
				*/
			 
			
			 
			 public DataValue buildDataValue(int idprop,String value) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
				 DataValue val=null;
				 if(this.isDataProperty(idprop)){
					 int valueCls=this.getDatatype(idprop);
					 val=(DataValue)ka.buildValue(value, valueCls);
				 }else{
					 System.err.println("ERROR PROGRAMACION: Se ha llamada a buildDataValue con una prop que no es dataproperty: idProp="+idprop+"  value="+value);
				 }
				 return val;
			 }


			 
			 
			 
			 
			 public ObjectValue buildObjectValue(int idprop,String value,int valuecls) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
				 ObjectValue val=null;
				 if(this.isObjectProperty(idprop)){
					 val=(ObjectValue)ka.buildValue(value, valuecls);
				 }
				 else{
					System.err.println("ERROR PROGRAMACION: Se ha llamada a buildObjectValue con una prop que no es objectproperty: idProp="+idprop+"  value="+value);
				}
				 return val;
			 }
			 
			 
			 
			 public void  setAbstractAccess(String nameclass) throws NotFoundException, IncoherenceInMotorException {
				  int idto=this.getIdClass(nameclass);
				  ruleEngine.insertFact(ka.buildAbstractAccess(idto));
			  }


			  public void  setSetDenniedAccess(String nameclass,String nameProp) throws NotFoundException, IncoherenceInMotorException {
				  Integer idto=null;
				  if(nameclass!=null)
					  idto=this.getIdClass(nameclass);
				  int idProp=this.getIdProperty(nameProp);
				  ruleEngine.insertFact(ka.buildAccess(idto, idProp, Constants.ACCESS_SET_NAME, true, 0));
			  }
			  
			  
			  public void setContributionValue(int ido,int idto,int idProp,ContributionValue contValue, Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
				  if (!this.existInMotor(ido)) {  
						this.getFromServer(ido,idto,1, null, Constants.USER_SYSTEM, null);
					}
					Iterator it = this.getFactsPropertyIterator(ido, idProp);
					 HashMap<String,Number> mapIdoInitialValue=contValue.getINITIALVALUE();
					 boolean newFact=false;
					 if(!it.hasNext()){
						 double initialValue=0;
						 if(mapIdoInitialValue!=null){
							 Iterator<Number> itr=mapIdoInitialValue.values().iterator();
							 while(itr.hasNext()){
								 Number value=itr.next();
								 if(value!=null){
									 initialValue+=value.doubleValue();
								 }
							 }
							 if(nDigRedondeo!=null)
								 initialValue=Auxiliar.redondea(initialValue, nDigRedondeo.intValue());
						 }
						 //setValue(ido,getPropertyName(idProp),initialValue);
						 DataProperty prop=(DataProperty)getProperty(ido,idto,idProp,null,null,null,false);
						 dynagent.common.knowledge.FactInstance newfact = (FactInstance) ka.traslateValueToFact(prop, ka.buildValue(String.valueOf(initialValue), prop.getDataType()));
						 //System.err.println("\nnew fact "+newfact);
						 ruleEngine.addNewFactToRuler(newfact,prop.getCardMax(),true);
						 newFact=true;
						 it = this.getFactsPropertyIterator(ido, idProp);
					 }
					 if (it.hasNext()) {
						 Number currentvalue=contValue.getCURRENTVALUE();
						 if(currentvalue==null){
							 currentvalue=new Double(0); 
						 }
						 
						 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it.next();
						 
						 if(ruleEngine.setFactWithContributionValue(fact, mapIdoInitialValue,currentvalue,nDigRedondeo) || newFact){//Solo bloqueamos si realmente se hace un cambio
							 Integer level=this.getLevelOf(ido);
							 if(level!=null&&level.intValue()!=Constants.LEVEL_FILTER){
								 Lock l = this.ruleEngine.getLockFact(ido);
								 if(l!=null&&l.getSTATE().equals(Constants.INDIVIDUAL_STATE_READY))//SOLO BLOQUEAMOS SI NO ESTA BLOQUEADO
									 this.setLock(new Integer(ido),true,Constants.USER_SYSTEM,false);//Bloqueamos solo localmente, no se bloquea en BD
							 }
						 }
					 }else{
						 System.err.println("WARNING:No se ha creado correctamente el fact al no existir este en setContributionValue siendo ido:"+ido+" idProp:"+idProp+" contValue:"+contValue);
					 }
			  }
				
			  public void setIncrementalValue(int ido,int idto,int idProp,double incrementalValue, Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
				DatValue dv=(DatValue)getFact(ido,idProp);
				if(dv!=null){		
					dv.setIncrement(incrementalValue,nDigRedondeo);
					return;
				}
					
				if (!this.existInMotor(ido)) {  
						this.getFromServer(ido,idto,1, null, Constants.USER_SYSTEM, null);
					}
					Iterator it = this.getFactsPropertyIterator(ido, idProp);
					 boolean newFact=false;
					 if(!it.hasNext()){
						 double initialValue=0;
						 //setValue(ido,getPropertyName(idProp),initialValue);
						 DataProperty prop=(DataProperty)getProperty(ido,idto,idProp,null,null,null,false);
						 dynagent.common.knowledge.FactInstance newfact = (FactInstance) ka.traslateValueToFact(prop, ka.buildValue(String.valueOf(initialValue), prop.getDataType()));
						 //System.err.println("\nnew fact "+newfact);
						 ruleEngine.addNewFactToRuler(newfact,prop.getCardMax(),true);
						 newFact=true;
						 it = this.getFactsPropertyIterator(ido, idProp);
					 }
					 if (it.hasNext()) {
						 
						 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it.next();
						 
						 if(ruleEngine.setFactWithIncrementalValue(fact, incrementalValue, nDigRedondeo) || newFact){//Solo bloqueamos si realmente se hace un cambio
							 Integer level=this.getLevelOf(ido);
							 if(level!=null&&level.intValue()!=Constants.LEVEL_FILTER){
								 Lock l = this.ruleEngine.getLockFact(ido);
								 if(l!=null&&l.getSTATE().equals(Constants.INDIVIDUAL_STATE_READY))//SOLO BLOQUEAMOS SI NO ESTA BLOQUEADO
									 this.setLock(new Integer(ido),true,Constants.USER_SYSTEM,false);//Bloqueamos solo localmente, no se bloquea en BD
							 }
						 }
					 }else{
						 System.err.println("WARNING:No se ha creado correctamente el fact al no existir este en setIncrementalValue siendo ido:"+ido+" idProp:"+idProp+" incrementalValue:"+incrementalValue);
					 }
			  }
			  
			  public void addContributionValue(int ido,int idto,int idProp,String key,double incrementalValue, Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
				  if (!this.existInMotor(ido)) {  
						this.getFromServer(ido,idto,1, null, Constants.USER_SYSTEM, null);
					}
					Iterator it = this.getFactsPropertyIterator(ido, idProp);
					 boolean newFact=false;
					 if(!it.hasNext()){
						 double initialValue=0;
						 //setValue(ido,getPropertyName(idProp),initialValue);
						 DataProperty prop=(DataProperty)getProperty(ido,idto,idProp,null,null,null,false);
						 dynagent.common.knowledge.FactInstance newfact = (FactInstance) ka.traslateValueToFact(prop, ka.buildValue(String.valueOf(initialValue), prop.getDataType()));
						 //System.err.println("\nnew fact "+newfact);
						 ruleEngine.addNewFactToRuler(newfact,prop.getCardMax(),true);
						 newFact=true;
						 it = this.getFactsPropertyIterator(ido, idProp);
					 }
					 if (it.hasNext()) {
						 
						 dynagent.common.knowledge.IPropertyDef fact = (dynagent.common.knowledge.IPropertyDef) it.next();
						 
						 if(ruleEngine.setFactWithContributionValue(fact, key, incrementalValue, nDigRedondeo) || newFact){//Solo bloqueamos si realmente se hace un cambio
							 Integer level=this.getLevelOf(ido);
							 if(level!=null&&level.intValue()!=Constants.LEVEL_FILTER){
								 Lock l = this.ruleEngine.getLockFact(ido);
								 if(l!=null&&l.getSTATE().equals(Constants.INDIVIDUAL_STATE_READY))//SOLO BLOQUEAMOS SI NO ESTA BLOQUEADO
									 this.setLock(new Integer(ido),true,Constants.USER_SYSTEM,false);//Bloqueamos solo localmente, no se bloquea en BD
							 }
						 }
					 }else{
						 System.err.println("WARNING:No se ha creado correctamente el fact al no existir este en setIncrementalValue siendo ido:"+ido+" idProp:"+idProp+" incrementalValue:"+incrementalValue);
					 }
			  }

			public DataModelAdapter getDataModelAdapter() {
				return dataModelAdapter;
			}
			
			
			/**
			 * SOLO PUEDE USARSE CUANDO EL IDO YA ESTE EN MOTOR 
			 * @param ido
			 * @param idto
			 * @throws OperationNotPermitedException 
			 * @throws IncoherenceInMotorException 
			 * @throws NotFoundException 
			 * @throws CardinalityExceedException 
			 * @throws IncompatibleValueException 
			 * @throws ApplicationException 
			 * @throws InstanceLockedException 
			 * @throws CommunicationException 
			 * @throws RemoteSystemException 
			 * @throws SystemException 
			 */public void checkIncorrectParamsIdoIdtoWhenIdoInRuler(Integer ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
				if(ido!=null){
					 
					 boolean noClase=!this.isIDClass(idto);
					 boolean noIndividuo=!this.existInMotor(ido);
					 int idtoRuler=this.getClassOf(ido);
					 boolean noEsDeClase=idtoRuler!=idto;
					 if(noClase||noIndividuo||noEsDeClase){
						 System.err.println("\n\n\n ERROR:");
						 System.err.println("checkIncorrectParamsIdoIdtoWhenIdoInRuler PROBLEMAS  ido:"+ido+" idto:"+idto+"  noClase:"+noClase+" noIndividuo:"+noIndividuo+"  noEsDeClase:"+noEsDeClase);
						 if(noEsDeClase){
							 System.err.println("idtoRuler:"+idtoRuler+" idto:"+idto);
						 }
						 Auxiliar.printCurrentStackTrace();
						 throw new OperationNotPermitedException("INCORRECTOS PARAMETROS ido,idto. ido:"+ido+" idto:"+idto);
					 }
				}
				else if(idto>0 && !this.isIDClass(idto)){
						System.err.println("\n\n\n ERROR:");
						 System.err.println("checkIncorrectParamsIdoIdtoWhenIdoInRuler PROBLEMAS ido:"+ido+" idto:"+idto);
						 Auxiliar.printCurrentStackTrace();
						 throw new OperationNotPermitedException("INCORRECTOS PARAMETROS ido,idto. ido:"+ido+" idto:"+idto);
						
				}
			}
			 
			 
			 public void checkRangesIdoIdto(Integer ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
//				 if (idto>0) {
//					if(ido!=null){
//						 boolean noClase=idto<Constants.MIN_ID_CLASS||idto>Constants.MAX_ID_CLASS;
//						 boolean noIndividuo=(ido<0&&(this.getLevelOf(ido)==null||this.getClassOf(ido)==null))||(ido>0&&ido<10000);
//						 if(noClase||noIndividuo){
//							 System.err.println("\n\n\n WAARNINGGGGGGGGGGGGGGGGGGGG WARNINGGGGGGGGGGGGGGGGG");
//							 System.err.println("checkRangesIdoIdto PROBLEMAS ido:"+ido+" idto:"+idto+" noClase:"+noClase+"  noIndividuo:"+noIndividuo+" this.getLevelOf(ido):"+this.getLevelOf(ido)+"  this.getClassOf(ido):"+this.getClassOf(ido));
//							 Auxiliar.printCurrentStackTrace();
//							 throw new OperationNotPermitedException("INCORRECTOS PARAMETROS ido,idto. ido:"+ido+" idto:"+idto);
//						 }
//					}
//					else if(idto<Constants.MIN_ID_CLASS||idto>Constants.MAX_ID_CLASS){
//						 System.err.println("\n\n\n WAARNINGGGGGGGGGGGGGGGGGGGG WARNINGGGGGGGGGGGGGGGGG");
//						 System.err.println("checkRangesIdoIdto PROBLEMAS ido:"+ido+" idto:"+idto);
//						 Auxiliar.printCurrentStackTrace();
//						 throw new OperationNotPermitedException("INCORRECTOS PARAMETROS ido,idto. ido:"+ido+" idto:"+idto);
//						
//					}
//				 }
			}
			 
			 
			 public String agruparValoresIndividuos (ArrayList<IPropertyDef> lfactsserver) throws NotFoundException, IncoherenceInMotorException{
				 try{ 
					 String result=null;
					   HashMap<String, ArrayList<String>> valoresXIndividuo=new HashMap<String ,ArrayList<String>>();
					   for(int i=0;i<lfactsserver.size();i++){
						   IPropertyDef f=lfactsserver.get(i);
						   String dominio="  "+f.getCLASSNAME()+" IDO: "+f.getIDO();
						   ArrayList<String> valores=null;
						   if(valoresXIndividuo.get(dominio)==null){
							   valores=new ArrayList<String>();
							   
						   }else{
							   valores=valoresXIndividuo.get(dominio);
						   }
						   Value val=ka.buildValue(f);
	
						   String represValor=null;
						   if(val!=null){
							   represValor=val.getValue_s();
						   }
						   valores.add(" "+this.getPropertyName(+f.getPROP()).toUpperCase()+"  "+represValor);
						   valoresXIndividuo.put(dominio, valores);
					   }
					   if(valoresXIndividuo.size()>0){
						   result=Auxiliar.hashMapToString(valoresXIndividuo,"dominio","valores");
					   }
					   return result;
				 }
				 catch(Exception e){
					 System.err.println("\n EXCEPCION EN DDM.AGRUPARVALORESINDIVIDUOS PARA FACTS: "+lfactsserver);
					 e.printStackTrace();
					 return null;
					 
				 }
			   }
			 
			 
			 ArrayList<IPropertyDef> getFactValuesInBD(int idto,int ido) throws SystemException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{	
				 return server.serverGetFactsInstance(ido, idto,this.getUser(), false/*SessionController.getInstance().getActualSession(this).isLockObjects()*/, 1,true, true).getAIPropertyDef();
			 }
				 
		public IKnowledgeBaseInfo doClone() throws NotFoundException, IncoherenceInMotorException, EngineException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
			DocDataModel ddm=new DocDataModel();
			
			ddm.session = new DDBBSession(ddm,session.isCheckCoherenceObjects(), session.isRunRules(), session.isLockObjects(), session.isDeleteFilters(), session.isReusable());
			//ddm.session.getHistoryDDBBListeners().addAll(session.getHistoryDDBBListeners());
			//ddm.session.getChangeServerListeners().addAll(session.getChangeServerListeners());
			/*SessionController.getInstance().setDDM(this);
			SessionController.getInstance().remove(this.session.getID());
			SessionController.getInstance().getSessionsList().add(this);*/
			ddm.sessionTemporal = new DefaultSession(ddm,null,null,sessionTemporal.isCheckCoherenceObjects(), sessionTemporal.isRunRules(), sessionTemporal.isLockObjects(), sessionTemporal.isDeleteFilters(),false);//TODO: dar un nombre
			
			ddm.setMetaData(metaData);
			ddm.setServer(server);
			ddm.setAsigned(asigned);
			ddm.setUser(user);
			ddm.setUserRoles(userRoles);
			ddm.setOrderProperties(orderProperties);
			ddm.setListCM(listCM);
			ddm.setColumnProperties(columnProperties);
			ddm.setGroupsProperties(groupsProperties);
			ddm.setEssentialProperties(essentialProperties);
			ddm.setListenerUtasks(listenerUtasks);
			ddm.setGlobalUtasks(globalUtasks);
			ddm.setNbusiness(nbusiness);
			
			//ddm.hmNamePropXIdProp=hmNamePropXIdProp;
			ddm.hmNameClassXIdto=hmNameClassXIdto;
			
			ddm.debugLog=debugLog;
			ddm.alias=alias;
			ddm.masks=masks;
			ddm.indexes=indexes;
			
			ddm.hmGenericFilterXIdto=hmGenericFilterXIdto;
			
			ddm.hmRdnxIDO.putAll(hmRdnxIDO);
			for(Integer idto:hmIDOSxIDTO.keySet())
				ddm.hmIDOSxIDTO.put(idto,(HashSet<Integer>)hmIDOSxIDTO.get(idto).clone());

			ddm.hmIDTOxIDO.putAll(hmIDTOxIDO);
			ddm.hmLEVELxID.putAll(hmLEVELxID);
			
			ddm.questionListener=questionListener;
			
			ddm.printRules=printRules;
			
			ddm.noticeListenerList=noticeListenerList;
			
			SessionController.getInstance().setActual(ddm.getDefaultSession(),ddm);
			
			ddm.setRuleEngine(ruleEngine.doClone(ddm));
			
			SessionController.getInstance().setActual(null,ddm);
			
			ddm.buildAccessEngine();
			
			ddm.enabled=true;
			
			return ddm;
		}

		public IKnowledgeBaseInfo getKnowledgeBase() {
			return this;
		}

		public HashSet<Object> getListFactRetractable() {
			return listFactRetractable;
		}

		public void dispose() {
			//System.err.println("*****Dispose de DocDataModel "+this.hashCode());
			//Auxiliar.printCurrentStackTrace();
			ruleEngine.dispose();
			ruleEngine=null;
			this.ka=null;
			this.dataModelAdapter=null;
			this.sessionTemporal.dispose();
			this.session.dispose();
			this.sessionTemporal=null;
			this.session=null;
		}

		public boolean isDispose() {
			return (ruleEngine==null);
		}

		
		public String getInfoIndividuoEnMotor(int ido){
			LinkedList list=this.getRuleEngine().getInstanceFactsWhereIdo(ido);
			String result="\n\n NUMERO DE FACTS EN MOTOR DE IDO:"+ido+"  n:"+list.size();
			for(int i=0;i<list.size();i++){
				result+="\n"+list.get(i).toString()+"\n";
			}
			return result;
			
			
			
		}
		
		public boolean isEnabled() {
			return enabled;
		}
		
		public void setEnabled(boolean enabled) {
			this.enabled=enabled;
		}

		public IQuestionListener getQuestionListener() {
			return questionListener;
		}

		public void setQuestionListener(IQuestionListener questionListener) {
			this.questionListener = questionListener;
		}
		
		public IEmailListener getEmailListener() {
			return emailListener;
		}

		public void setEmailListener(IEmailListener emailListener) {
			this.emailListener = emailListener;
		}
		
		public void buildAccessOfAbstractClasses() throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			ArrayList<FactAccess> listFactAccess=new ArrayList<FactAccess>();
			Date now=new Date(System.currentTimeMillis());
			for(int idto:abstractClasses){
				access access=getAccessOverObject(idto, null, user, null);
				boolean viewAccess=false;
				boolean setAccess=false;
				boolean delAccess=false;
				boolean newAccess=false;
				boolean findAccess=false;
				if(!viewAccess || !setAccess || !newAccess || !delAccess || !findAccess){
					Iterator<Integer> itr=getSpecializedDescendByAbstract(idto).iterator();//No nos interesan las abstractas ya que si hay algun permiso de todos modos estara en los hijos por la regla de permisos heredados
					while(itr.hasNext()){
						int idtoSpec=itr.next();
						if(idto!=idtoSpec){
							if(!viewAccess || !setAccess || !newAccess || !delAccess || !findAccess){
								access accessSpec=getAccessOverObject(idtoSpec, null, user, null);
								if(accessSpec.getViewAccess() && !viewAccess){
									viewAccess=true;
								}
								if(accessSpec.getSetAccess() && !setAccess){
									setAccess=true;
								}
								if(accessSpec.getNewAccess() && !newAccess){
									newAccess=true;
								}
								if(accessSpec.getDelAccess() && !delAccess){
									delAccess=true;
								}
								if(accessSpec.getFindAccess() && !findAccess){
									findAccess=true;
								}
							}
						}
					}
				}
				
				//Solo nos interesan los access que no posea ya la clase padre
				
				if(!Auxiliar.equals(access.getViewAccess(),viewAccess)){
					listFactAccess.add(new FactAccess(idto,null,null,null,null,null,user,null,viewAccess?0:1,access.VIEW,Constants.MAX_ACCESS_PRIORITY-1,this));
				}
				if(!Auxiliar.equals(access.getSetAccess(),setAccess)){
					listFactAccess.add(new FactAccess(idto,null,null,null,null,null,user,null,setAccess?0:1,access.SET,Constants.MAX_ACCESS_PRIORITY-1,this));
				}
				if(!Auxiliar.equals(access.getNewAccess(),newAccess)){
					listFactAccess.add(new FactAccess(idto,null,null,null,null,null,user,null,newAccess?0:1,access.NEW,Constants.MAX_ACCESS_PRIORITY-1,this));
				}
				if(!Auxiliar.equals(access.getDelAccess(),delAccess)){
					listFactAccess.add(new FactAccess(idto,null,null,null,null,null,user,null,delAccess?0:1,access.DEL,Constants.MAX_ACCESS_PRIORITY-1,this));
				}
				if(!Auxiliar.equals(access.getFindAccess(),findAccess)){
					listFactAccess.add(new FactAccess(idto,null,null,null,null,null,user,null,findAccess?0:1,access.FIND,Constants.MAX_ACCESS_PRIORITY-1,this));
				}
			}
			
			for(FactAccess factAccess:listFactAccess){
				ruleEngine.insertFact(factAccess);
				accessEngine.addFactAccess(factAccess);
			}
			
			listFactAccess.clear();
			
			for(int idto:abstractClasses){
				HashSet<Integer> listProp=getIDsExplicitPropertiesOf(idto);
				for(int idProp:listProp){
					Iterator<FactAccess> itFactAccess = accessEngine.getAccessOfProperty(idto, idProp, null).iterator();
					boolean hasAccess=itFactAccess.hasNext();
					// Deducimos el access resultante.
					access access=this.deduceAccess(itFactAccess);
					boolean viewAccess=false;
					boolean setAccess=false;
					Iterator<Integer> itr=getSpecializedDescendByAbstract(idto).iterator();//No nos interesan las abstractas ya que si hay algun permiso de todos modos estara en los hijos por la regla de permisos heredados
					while(itr.hasNext()){
						int idtoSpec=itr.next();
						if(idto!=idtoSpec && hasProperty(idtoSpec, idProp)){
							if(!viewAccess || !setAccess){
								itFactAccess = accessEngine.getAccessOfProperty(idtoSpec, idProp, null).iterator();
								 // Deducimos el access resultante.
								access accessSpec=this.deduceAccess(itFactAccess);
								if(accessSpec.getViewAccess() && !viewAccess){
									viewAccess=true;
									//System.out.println("VIEW idto:"+idto+" de idtoSpec:"+idtoSpec+" e idProp:"+idProp);
								}
								if(accessSpec.getSetAccess() && !setAccess){
									setAccess=true;
									//System.out.println("SET idto:"+idto+" de idtoSpec:"+idtoSpec+" e idProp:"+idProp);
								}
							}
						}
					}
					
					//Solo nos interesan los access que no posea ya la clase padre
					
					if(!Auxiliar.equals(access.getViewAccess(),viewAccess)){
						listFactAccess.add(new FactAccess(idto,null,idProp,null,null,null,user,null,viewAccess?0:1,access.VIEW,Constants.MAX_ACCESS_PRIORITY-1,this));
					}
					if(!Auxiliar.equals(access.getSetAccess(),setAccess)){
						listFactAccess.add(new FactAccess(idto,null,idProp,null,null,null,user,null,setAccess?0:1,access.SET,Constants.MAX_ACCESS_PRIORITY-1,this));
					}
				}
			}
			
			for(FactAccess factAccess:listFactAccess){
				ruleEngine.insertFact(factAccess);
				accessEngine.addFactAccess(factAccess);
			}
		}

		/**
		 * Devuelve los especializados directos no abstractos. Si encuentra un abstracto desciende a sus especializados hasta llegar a un nivel en el que ninguno sea abstracto o ya no existan hijos.
		 */
		private HashSet<Integer> getSpecializedDescendByAbstract(int idto) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
			HashSet<Integer> directSpecialized=getDirectSpecialized(idto);
			HashSet<Integer> auxDirectSpecialized=new HashSet<Integer>();
			for(int idtoS:directSpecialized){
				if(idtoS!=idto){
					if(abstractClasses.contains(idtoS)){
						auxDirectSpecialized.addAll(getSpecializedDescendByAbstract(idtoS));
					}else auxDirectSpecialized.add(idtoS);	
				}
			}
			return auxDirectSpecialized;
		}
		
		
		
		public void buildAccessSpecialized() throws NotFoundException, IncoherenceInMotorException{
			ArrayList<FactAccess> mapAccess=new ArrayList<FactAccess>();
			mapAccess.addAll(ruleEngine.getAllAccessFacts());
			Iterator<FactAccess> itAllFactAccess = ruleEngine.getAllAccessFacts().iterator();

			
			while(itAllFactAccess.hasNext()){
				FactAccess factAccess=itAllFactAccess.next();
				Integer idto=factAccess.getIDTO();
				if(idto!=null && !factAccess.getACCESSTYPE().equals(Constants.ACCESS_ABSTRACT)){
					Iterator<Integer> itr=getSpecialized(idto).iterator();
					while(itr.hasNext()){
						int idtoSpec=itr.next();
						if(idto!=idtoSpec){
							FactAccess newFactAccess=factAccess.clone();
							newFactAccess.setIDTO(idtoSpec);
							if(!mapAccess.contains(newFactAccess))
								ruleEngine.insertFact(newFactAccess);
								mapAccess.add(newFactAccess);
							}
						}	
					}
				}
			
			//System.err.println("\n ...debug buildAccessSpecialized deduce "+mapAccess.size()+" por herencia");
		}
		
		public void buildIndexSpecialized() throws NotFoundException, IncoherenceInMotorException{
			if(indexes!=null){
				Iterator<Integer> itr=((HashSet<Integer>)indexes.clone()).iterator();
				while(itr.hasNext()){
					int idto=itr.next();
					indexes.addAll(getSpecialized(idto));
				}
			}
		}
		
		public void buildAccessEngine() throws NotFoundException, IncoherenceInMotorException{
			accessEngine=new AccessEngine(ruleEngine.getAllAccessFacts());
		}

		public void setIndexes(HashSet<Integer> arrayIndexes) {
			indexes=arrayIndexes;
		}
		
		public synchronized void requestInformation(Integer ido,Integer idProp,Session session) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
			DefaultSession sess = new DefaultSession(this,session,session.getUtask(),false, session.isRunRules(),session.isLockObjects(), session.isDeleteFilters(),false);
			
			Session oldSession =SessionController.getInstance().getActualSession(this);
			boolean success=false;
			try {
//				System.out.println("DENTRO DE getPropertyIterator public");
				SessionController.getInstance().setActual(sess,this);
				FactInfo factInfo=new FactInfo(ido,idProp,this);
				ruleEngine.insertFact(factInfo);
				// si todo ha ido bien. commit.
				ruleEngineRun();
				sess.sendPendingLocks();
				sess.commit();
				success=true;
			}finally{
				if(!success){
					try{
						sess.rollBack();//cancelSession(sess);
						System.err.println("WARNING:Sesion interna de DocDataModel.requestInformation cancelada");
					} catch (Exception e) {
						System.err.println("WARNING:Sesion interna de DocDataModel.requestInformation no ha podido cancelarse");
						e.printStackTrace();
					}finally{
						SessionController.getInstance().setActual(oldSession,this);
					}
				}else SessionController.getInstance().setActual(oldSession,this);
			}
		}

		@Override
		public void addBatchListener(IBatchListener batchListener) {
			batchListenerList.add(batchListener);
		}

		@Override
		public void removeBatchListener(IBatchListener batchListener) {
			batchListenerList.remove(batchListener);
		}
		
		public HashSet<IBatchListener> getBatchListeners() {
			return batchListenerList;
		}
		
		@Override
		public void addNoticeListener(INoticeListener noticeListener) {
			noticeListenerList.add(noticeListener);
		}

		@Override
		public void removeNoticeListener(INoticeListener batchListener) {
			noticeListenerList.remove(batchListener);
		}
		
		public HashSet<INoticeListener> getNoticeListeners() {
			return noticeListenerList;
		}
		
		public HashSet<Integer> getClassifiedIdtos(int idto,int idProp, Value value, String user, Integer userRol, Integer idtoUserTask, Session ses) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
			HashSet<Integer> list=new HashSet<Integer>();
			boolean isAbstract=isAbstract(idto);
			if(isAbstract){
				list=getSubClassifiedIdtos(idto, idProp, value, user, userRol, idtoUserTask, ses);
//				if(getCategory(idProp).isObjectProperty()){
//					//Si es objectProperty intentamos encontrar los que tienen como rangoList exactamente el mismo que el valueCls de value ya que ahora mismo tendriamos tambien los especializados.
//					//En caso de no encontrar solo uno que case con el valueCls entonces devolvemos todas las posibilidades
//					Iterator<Integer> itr=list.iterator();
//					HashSet<Integer> listAux=new HashSet<Integer>();
//					while(itr.hasNext()){
//						int classifiedIdto=itr.next();
//						ObjectProperty prop=(ObjectProperty)getProperty(null, classifiedIdto, idProp, userRol, user, idtoUserTask, ses);
//						if(value==null || prop.getRangoList().contains(((ObjectValue)value).getValueCls())){
//							listAux.add(classifiedIdto);
//						}/*else{//TODO Comentado hasta que no se active el clasificador
//							//Si no casa directamente, miramos si casa con el primer nivel. Esto de momento nos sirve pero no sabemos si mas adelante necesitaremos mirar mas niveles y quedarnos con el nivel mas cercano
//							Iterator<Integer> itrParent=getParents(((ObjectValue)value).getValueCls()).iterator();
//							while(itrParent.hasNext()){
//								int idtoParent=itrParent.next();
//								if(prop.getRangoList().contains(idtoParent))
//									listAux.add(classifiedIdto);
//							}
//						}*/
//					}
//					if(listAux.size()==1)
//						list=listAux;
//				}
			}else list.add(idto);
			
			return list;
		}
		
		private HashSet<Integer> getSubClassifiedIdtos(int idto,int idProp, Value value, String user, Integer userRol, Integer idtoUserTask, Session ses) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
			HashSet<Integer> listIdtos=new HashSet<Integer>();
			boolean isAbstract=isAbstract(idto);
			if(isAbstract){
				Iterator<Integer> itr=getDirectSpecialized(idto).iterator();
				while(itr.hasNext()){
					int idtoFilter=itr.next();
					
					listIdtos.addAll(getSubClassifiedIdtos(idtoFilter, idProp, value, user, userRol, idtoUserTask, ses));
				}
			}else{
				if(hasProperty(idto, idProp)){
					Property prop=getProperty(null,idto, idProp, userRol, user, idtoUserTask, ses);
					if(value==null || value.checkIsCompatibleWithNotException(prop, this, idtoUserTask))
						listIdtos.add(idto);
				}
//				Iterator<Integer> itr=getSpecializedFilters(idto, userRol, idtoUserTask, ses).iterator();//TODO Comentado hasta que no se active el clasificador
//				while(itr.hasNext()){
//					int idoFilter=itr.next();
//					int idtoFilter=getClass(idoFilter);
//					
//					listIdtos.addAll(getSubClassifiedIdtos(idoFilter,idtoFilter,idProp, value, userRol, idtoUserTask, ses));
//				}
			}
			
			return listIdtos;
		}
		
		/**
		 * Completa un filtro hasta el numero de niveles indicado, respetando los filtros que ya estan colgando de este filtro.
		 * Si el ido que se pasa como parametro es un filtro, ese es considerado primer nivel. Si es un prototipo o individuo el primer nivel es el filtro que cuelga de sus objectProperty
		 */
		public synchronized void completeFilterLevels(int ido,int idto, Integer userRol, Integer userTask, int levels, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			DefaultSession sess = new DefaultSession(this, sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
			
			Session oldSession =SessionController.getInstance().getActualSession(this);
			boolean success=false;
			try {
				SessionController.getInstance().setActual(sess,this);
				completeFilterLevels(ido, idto, userRol, userTask, levels);
				// si todo ha ido bien, commit
				ruleEngineRun();
				sess.sendPendingLocks();
				sess.commit();
				success=true;
			}finally{
				if(!success){
					try{
						sess.rollBack();//cancelSession(sess);
						System.err.println("WARNING:Sesion interna de DocDataModel.completeFilterLevels cancelada");
					} catch (Exception e) {
						System.err.println("WARNING:Sesion interna de DocDataModel.completeFilterLevels no ha podido cancelarse");
						e.printStackTrace();
					}finally{
						SessionController.getInstance().setActual(oldSession,this);
					}
				}else SessionController.getInstance().setActual(oldSession,this);
			}
		}
				
		public void completeFilterLevels(int ido,int idto, Integer userRol, Integer userTask, int levels) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			//System.err.println("completeFilterLevels "+levels);
			int initialLevel=isIDFilter(ido)?1:0;
			completeLevelFilter(ido, idto, userRol, userTask, levels, initialLevel, new HashSet<Integer>());
		}
		
		private void completeLevelFilter(int ido, int idto, Integer userRol, Integer userTask, int levels, int depth, HashSet<Integer> processed) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			Iterator<Property> itr=getAllPropertyIterator(ido, idto, userRol, user, userTask, false);
			while(itr.hasNext()){
				Property property=itr.next();
				if(property instanceof ObjectProperty && ((ObjectProperty) property).getEnumList().isEmpty()){
					Iterator<Integer> ranges=((ObjectProperty)property).getRangoList().iterator();
					while(ranges.hasNext()){
						int idRange=ranges.next();
						int idtoRange=getClassOf(idRange);
						
						if(!processed.contains(idRange)){
							if(!isIDFilter(idRange) && depth<levels){
								//System.err.println("Crea filtro a "+property);
								setRange(ido, idto, property.getIdProp(), idtoRange, userRol, user, userTask, levels-depth);
							}else{
								processed.add(idRange);
								completeLevelFilter(idRange, idtoRange, userRol, userTask, levels, depth+1, processed);
							}
						}
					}
				}
			}
		}
		
		/**
		 * Crea un filtro con el numero de niveles indicado. El filtro principal creado ya se cuenta como nivel 1.
		 */
		public synchronized Integer createFilter(int idto, Integer userRol, String user, Integer usertask, int depth, Session sessionPadre)
		throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
			Integer re = null;
			DefaultSession sess = new DefaultSession(this, sessionPadre,sessionPadre.getUtask(),false, sessionPadre.isRunRules(),sessionPadre.isLockObjects(), sessionPadre.isDeleteFilters(),false);
			
			Session oldSession =SessionController.getInstance().getActualSession(this);
			boolean success=false;
			try {
				SessionController.getInstance().setActual(sess,this);
				re = createPrototype(idto, Constants.LEVEL_FILTER, new HashMap<Integer, ArrayList<Integer>>(), userRol, user, usertask, depth-1/*-1 ya que contamos el primer filtro que crea createPrototype*/);
				// si todo ha ido bien, commit
				ruleEngineRun();
				sess.sendPendingLocks();
				sess.commit();
				success=true;
			}finally{
				if(!success){
					try{
						sess.rollBack();//cancelSession(sess);
						System.err.println("WARNING:Sesion interna de DocDataModel.createPrototype cancelada");
					} catch (Exception e) {
						System.err.println("WARNING:Sesion interna de DocDataModel.createPrototype no ha podido cancelarse");
						e.printStackTrace();
					}finally{
						SessionController.getInstance().setActual(oldSession,this);
					}
				}else SessionController.getInstance().setActual(oldSession,this);
			}

			return re;

		}

		public void setPrintRules(boolean printRules) {
			this.printRules = printRules;
		}
		
		public boolean isPrintRules(){
			return this.printRules;
		}
		
		
		public boolean sendEmail(Integer idoUserTaskReport, Integer idtoUserTaskReport, EmailRequest emailRequest, boolean showError) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
			boolean sended=false;
			
			int ido=emailRequest.getIdo();
			int idto=emailRequest.getIdto();
			
			Integer userRol=null;
			String reportFileName=null;
			
			Session session=new DefaultSession(this,getDefaultSession(),idtoUserTaskReport,false,true,false,true,false);
			session.setRulesGroup(Constants.REPORTS_RULES);
			Session oldSession =SessionController.getInstance().getActualSession(this);
			try {
				SessionController.getInstance().setActual(session,this);
				if(idtoUserTaskReport!=null){
					ObjectProperty propertyParams=null;
					try{
						propertyParams=(ObjectProperty) getProperty(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_PARAMS, userRol, this.getUser(), idtoUserTaskReport, true);
					} catch (NotFoundException e) {
						System.err.println("Report sin parametros. No se imprime.");
					}
					if(propertyParams!=null){
						int idoParams=createPrototype(propertyParams.getRangoList().getFirst(), /*Constants.LEVEL_FILTER*/Constants.LEVEL_PROTOTYPE, userRol, this.getUser(), idtoUserTaskReport, true);					
						int idtoParams=getClassOf(idoParams);
						Iterator<Property> itr=getAllPropertyIterator(idoParams, idtoParams, userRol, this.getUser(), idtoUserTaskReport, true);
						Property prop=null;
						while(itr.hasNext() && prop==null){
							Property property=itr.next();
							if(property instanceof ObjectProperty){
								ObjectProperty oProperty=(ObjectProperty)property;
								int valueCls=getClassOf(oProperty.getRangoList().getFirst());
								if(valueCls==idto || isSpecialized(idto, valueCls)){
									prop=oProperty;
								}
							}
						}
						if(prop!=null){
							setValue(prop.getIdo(), prop.getIdto(), prop.getIdProp(), null, new ObjectValue(ido, idto), userRol, getUser(), idtoUserTaskReport, false);
							
							//Creamos el treeobject solo cogiendo las objectProperty del primer nivel
							instance inst=new instance(prop.getIdto(),prop.getIdo());
							//System.err.println("idto:"+idto+" idObject:"+idObject);
							ArrayList<Integer> usedRanges=new ArrayList<Integer>();
							Iterator<Property> itp=getAllPropertyIterator(prop.getIdo(), prop.getIdto(), userRol, getUser(), idtoUserTaskReport, true);
							while(itp.hasNext()){
								Property p=itp.next();
								inst.addProperty(prop.getIdo(), p);
								if(p instanceof ObjectProperty){
									Integer idRange=null;
									LinkedList<Integer> list=((ObjectProperty) p).getRangoList();
									if(list.isEmpty()){
										idRange=((ObjectProperty)p).getEnumList().getFirst().getValueCls();
									}
									else{
										idRange=list.getFirst();
										if(usedRanges.contains(idRange))
											idRange=setRange(p.getIdo(), p.getIdto(),p.getIdProp(), idRange, userRol, getUser(), idtoUserTaskReport, 1);
										usedRanges.add(idRange);
									}
									inst.addProperty(idRange, getProperty(idRange, getClassOf(idRange), Constants.IdPROP_RDN, userRol, getUser(), idtoUserTaskReport, true));
								}
							}
							
							ObjectProperty property=(ObjectProperty)getProperty(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_REPORT_FORMAT, userRol, this.getUser(), idtoUserTaskReport, true);
							Integer idoFormat=null;
							if(property.getValues().size()==1)
								idoFormat=((ObjectValue)property.getValues().getFirst()).getValue();
							
							QueryXML query=getQueryXML();
							query.setSelect(new ArrayList<SelectQuery>());
							HashMap<String,String> oidReport=server.serverGetReport(query.toQueryXML(inst, idtoUserTaskReport), this.getUser(), idtoUserTaskReport, getClassName(idtoUserTaskReport), false, idoFormat, false,false);
							String [] workingStrings = oidReport.get(QueryConstants.PATH_FILE).split("/");
							reportFileName = workingStrings[workingStrings.length - 1];
							/*if(oidReport!=null){
								server.showReport("_blank",oidReport);
							}*/
						}
					}
				}
				
				sended=server.sendEmail(emailRequest.getIdo(),emailRequest.getIdto(),reportFileName,emailRequest.getEmail(), emailRequest.getSubject(), emailRequest.getBody(), emailRequest.getIdoMiEmpresa(), emailRequest.getIdoDestinatario(), showError);
			}finally{
				try{
					session.rollBack();//cancelSession(sess);
				} catch (Exception e) {
					System.err.println("WARNING:Sesion interna de DocDataModel.createPrototype no ha podido cancelarse");
					e.printStackTrace();
				}finally{
					SessionController.getInstance().setActual(oldSession,this);
				}
			}
			
			return sended;
			
		}
		
		public ArrayList<Integer> getGlobalUtasks() {
			return globalUtasks;
		}

		public void setGlobalUtasks(ArrayList<Integer> globalUtasks) {
			this.globalUtasks = globalUtasks;
		}
		
		public String getGroup(String className,String propName) throws NotFoundException, IncoherenceInMotorException{
			return this.groupsByClassAndProperty.get((className!=null?getIdClass(className):null)+":"+getIdProperty(propName));
		}

		public long getCreationDate() {
			return creationDate;
		}

		public IAlias getAlias() {
			return alias;
		}
}

