/***
 * RulerFactoryXML
 * @author: Jose A. Zamora Aguilera- jazamora@ugr.es
 */

package dynagent.ruleengine.src.factories;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.drools.rule.Package;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.Mask;
import dynagent.common.basicobjects.OrderProperty;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.Required;
import dynagent.common.basicobjects.T_Herencias;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.instance;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.FactHierarchy;
import dynagent.ruleengine.src.ruler.IRuleEngine;
//import dynagent.ruleengine.src.ruler.JessEngine;
import dynagent.ruleengine.src.sessions.SessionController;

public class RulerFactoryCommon implements IRulerFactory{
	private docServer server;
	private int bussiness;
	private IRuleEngine ruleEngine;
	private HashSet<Integer>individuos=new HashSet<Integer>();
	private HashSet<Integer>completedclases=new HashSet<Integer>();
	private HashSet<String>idsprocesados=new HashSet<String>();
	private HashMap<Integer,Integer> hmIDTOxIDO=new HashMap<Integer,Integer>();
	protected LinkedList<Instance> instances;
	protected HashSet<String> arrayURoles;
	protected ArrayList<OrderProperty> arrayOrderProperties;
	protected ArrayList<Alias> arrayAlias;
	protected ArrayList<CardMed> arrayCM;
	protected ArrayList<ColumnProperty> arrayColumnProperties;
	protected ArrayList<Groups> arrayGroups;
	protected ArrayList<EssentialProperty> arrayEssentialProperties;
	protected ArrayList<ListenerUtask> arrayListenerUtasks;
	protected ArrayList<Integer> arrayGlobalUtasks;
	protected ArrayList<Mask> arrayMasks;
	protected ArrayList<Required> arrayRequireds;
	protected HashSet<Integer> arrayIndexes;
	protected LinkedList<Properties> properties;
	protected LinkedList<T_Herencias> herencias;
	protected LinkedList<Access> accesses ;
	protected ArrayList<String> rulesFiles;
	protected ArrayList<Package> rulesPackages;
	
	private String engine;
	private String user;
	private HashMap<Integer,String> hmRdnxIDO=new HashMap<Integer,String>();
	private HashMap<Integer,String> hmClassNamexIDTO=new HashMap<Integer,String>();
	private HashMap<Integer,String> hmNamePropXIdProp=new HashMap<Integer,String>();
	private ArrayList <Instance> instancesOfIndividual=new ArrayList<Instance>();
	private ArrayList <Instance> instancesObjectPropertiesWithRdnValue=new ArrayList<Instance>();
	private boolean checkCoherence;
	private DebugLog debugLog;
	private IMessageListener messageListener;
	private IQuestionListener questionListener;
	private boolean printRules;
	
	
	protected RulerFactoryCommon(int bussiness, docServer server, String engine,String user, boolean checkCoherence, DebugLog debugLog, IMessageListener messageListener, IQuestionListener questionListener, boolean printRules){
		this.bussiness=bussiness;
		this.server=server;
		this.engine=engine;
		this.user=user;	
		this.checkCoherence=checkCoherence;
		this.debugLog=debugLog;
		this.messageListener=messageListener;
		this.questionListener=questionListener;
		this.printRules=printRules;
	}
		
	
	
	public IKnowledgeBaseInfo getIKnowledgeBaseInfo() throws EngineException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException{
		//System.out.println(".....inicio constructor RulerFactoryXML");
		double inicio=System.currentTimeMillis();
		
		//System.out.println("Antes inicializacion");
		//ruleEngine.getMotorSize();
		//System.out.println("   <time constructor RulerFactoryXML="+Auxiliar.getSecondsExecucionFrom(inicio));
		
		
		//System.out.println(".....inicio init RulerFactoryXML");
		inicio=System.currentTimeMillis();
			//DefaultSession s = new DefaultSession(null,null,categorySession, Session.SET_MODE);//TODO: dar un nombre
			//SessionController.getInstance().setActual(s);
			//IKnowledgeBaseInfo ik;//INTRODUCCION INTERFAZ AL MOTOR PARA SER ACCEDIDA DESDE REGLAS
			
			DocDataModel ddm=new DocDataModel(debugLog,checkCoherence, true, false, true, printRules);
			
			ruleEngine=EngineFactory.getInstance().createRuleEngine(engine,ddm);
			
			if(messageListener!=null){
				ddm.getDefaultSession().addIMessageListener(messageListener);
				ddm.getRootSession().addIMessageListener(messageListener);
			}
			if(questionListener!=null){
				ddm.setQuestionListener(questionListener);
			}
			SessionController.getInstance().setActual(ddm.getDefaultSession(),ddm);
			
			ddm.setNbusiness(bussiness);
			ddm.setUser(user);
			ddm.setRuleEngine(ruleEngine);
			//ddm.setRuler(ruler);
			this.notifyToMotor(ddm);
			if(!user.equals(Constants.USER_SYSTEM))
				ddm.setUserRoles(this.arrayURoles);
			ddm.setOrderProperties(this.arrayOrderProperties);
			ddm.setListAlias(this.arrayAlias);
			ddm.setListCM(this.arrayCM);
			if(this.arrayColumnProperties!=null)
				ddm.setColumnProperties(this.arrayColumnProperties);
			if(this.arrayGroups!=null)
				ddm.setGroupsProperties(this.arrayGroups);
			if(this.arrayEssentialProperties!=null)
				ddm.setEssentialProperties(arrayEssentialProperties);
			if(this.arrayListenerUtasks!=null)
				ddm.setListenerUtasks(arrayListenerUtasks);
			if(this.arrayMasks!=null)
				ddm.setListMask(arrayMasks);
			if(this.arrayIndexes!=null)
				ddm.setIndexes(arrayIndexes);
			if(this.arrayGlobalUtasks!=null)
				ddm.setGlobalUtasks(arrayGlobalUtasks);
			/*this.ruler.setR(new RulerShellUI());	
			this.setRuler(ruler);
			if(ruleEngine instanceof JessEngine){
				((JessEngine) ruleEngine).setRuler(ruler.getR().getR());
			}*/
			this.createInitialRules();
			
			this.createGlobalParameters(ddm);
			
			this.createInitialProperties();
			//importante que se comunique los labels de propiedades despues de inicializar las propiedades
			//this.notifyPropertyLabelToMotor(ddm);
			
			HashMap<Integer,HashSet<Integer>> mapRequiredDomProps=new HashMap<Integer, HashSet<Integer>>();
			if(this.arrayRequireds!=null){
				for(Required r:arrayRequireds){
					HashSet<Integer> props=mapRequiredDomProps.get(r.getIdtoClass());
					if(props==null){
						props=new HashSet<Integer>();
						mapRequiredDomProps.put(r.getIdtoClass(),props);
					}
					props.add(r.getIdProp());
				}
			}
			
			this.createInitialFacts(mapRequiredDomProps);
			
			
			//IMPORTANTE QUE LA INICIALI DE HERENCIAS SE REALICE DESPUES DE LA DE FACTS PQ LEE ETIQUETAS DE CLASES QUE SE DEDUCEN EN LA INIC DE FACTS!!!
			this.createInitialHierarchy();
			this.createInitialAccess();
			
			
			initIKnowledgeBaseInfo(ddm,user);
			SessionController.getInstance().setActual(null,ddm);
			//System.out.println("   <time init RulerFactoryXML="+Auxiliar.getSecondsExecucionFrom(inicio));
//			INTRODUCCION INTERFAZ AL MOTOR PARA SER ACCEDIDA DESDE REGLAS
			//ik=ddm;
			//ruleEngine.insertFact(ddm);
//			System.err.println("Final inicializacion");
//			ruleEngine.getMotorSize();
			//ruleEngine.printMotor();
			
			ddm.setEnabled(true);
			
			return ddm;
	}
	

	
	public void loadMetaData(IKnowledgeBaseInfo ik) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException{
		if(ik instanceof DocDataModel)
			((DocDataModel)ik).loadMetaData();
			
	}
	
	public void notifyToMotor(DocDataModel dm){
		 this.deduceLevels();
		//recorremos todas las clases completas
		Iterator<Integer> itclases=this.completedclases.iterator();
		while(itclases.hasNext()){
			int idto=itclases.next();
			dm.getHmLEVELxID().put(idto, Constants.LEVEL_MODEL);
			HashSet<Integer>idos=new HashSet<Integer>();
			dm.getHmIDOSxIDTO().put(idto, idos);
		}
		
		//INDIVIDUOS
		Iterator<Integer> itindividuos=this.individuos.iterator();
		while(itindividuos.hasNext()){
			int ido=itindividuos.next();
			int idto=this.hmIDTOxIDO.get(ido);
			dm.addIDOToIDTOWithLevel(ido, idto, Constants.LEVEL_INDIVIDUAL);
		}
		//notificamos los RDNs de los individuos:
		dm.setHmRdnxIDO(hmRdnxIDO);
		
		//NOMBRES DE LAS CLASES
		dm.setHmNameClassXIdto(this.hmClassNamexIDTO);
		
		
		
		
		//System.out.println(".....final notifyLevelsToMotor");
		//System.out.println("....IdosXIDTO="+dm.getHmIDOSxIDTO()+"\nIDTOxIDO"+dm.getHmIDTOxIDO()+"\nLEVELxID="+dm.getHmLEVELxID());
		
	}
	
	
	

	private void createInitialHierarchy() throws NotFoundException, IncoherenceInMotorException  {
		Iterator<T_Herencias> it = this.herencias.iterator();
		while(it.hasNext()){
			T_Herencias h=it.next();
			FactHierarchy fh=new FactHierarchy(h.getID_TO(),h.getID_TO_Padre(),this.hmClassNamexIDTO.get(h.getID_TO()),this.hmClassNamexIDTO.get(h.getID_TO_Padre()));
			ruleEngine.insertFact(fh);
		}
	}
	
	

	private void createInitialProperties() throws NotFoundException, IncoherenceInMotorException {
		Iterator<Properties> it =this.properties.iterator();
		while(it.hasNext()){
			Properties p=it.next();
			this.hmNamePropXIdProp.put(p.getPROP(),p.getNAME());
			ruleEngine.insertFact(p);
		} 
	}
	
	
	public void setLevel(IPropertyDef i){	
		if(i.getIDTO()!=null&&i.getIDO()==null/*&&i.getOP()!=null*/){//completedclass
			if(!this.idsprocesados.contains(i.getIDTO())){
				int idto=Integer.valueOf(i.getIDTO());
				this.completedclases.add(Integer.valueOf(i.getIDTO()));
				this.idsprocesados.add(String.valueOf(i.getIDTO()));
				/*if(i.getPROP()==Constants.IdPROP_RDN){
					this.hmClassNamexIDTO.put(idto,i.getVALUE());
				}*/
				if(this.hmClassNamexIDTO.get(idto)==null){
					this.hmClassNamexIDTO.put(idto,i.getCLASSNAME());
				}
			}	
		}
		
		else if(i.getIDO()!=null&&i.getOP()==null&&i.getPROP()==Constants.IdPROP_RDN){//individuos
			if(!this.idsprocesados.contains(i.getIDO())){
				int ido=Integer.valueOf(i.getIDO());
				this.hmRdnxIDO.put(ido,i.getVALUE());
				int idto=Integer.valueOf(i.getIDTO());
				this.individuos.add(Integer.valueOf(i.getIDO()));
				if(!this.hmIDTOxIDO.containsKey(ido)){
					this.hmIDTOxIDO.put(ido, idto);
				}
				this.idsprocesados.add(String.valueOf(i.getIDO()));
			}
		}
	} 
	
	
	public void setLevel(Instance i){	
		if(i.getIDTO()!=null&&i.getIDO()==null/*&&i.getOP()!=null*/){//completedclass
			if(!this.idsprocesados.contains(i.getIDTO())){
				int idto=Integer.valueOf(i.getIDTO());
				this.completedclases.add(Integer.valueOf(i.getIDTO()));
				this.idsprocesados.add(i.getIDTO());
				/*NO SE PUEDE SUPONER QUE TODAS LAS CLASES TENGAN RDN
				 * if(i.getPROPERTY().equals(String.valueOf(Constants.IdPROP_RDN))){
					this.hmClassNamexIDO.put(idto,i.getVALUE());
				}*/
				if(this.hmClassNamexIDTO.get(idto)==null){
					this.hmClassNamexIDTO.put(idto,i.getNAME());
				}
			}	
		}
		else if(i.getIDO()!=null&&i.getOP()==null&&i.getPROPERTY().equals(String.valueOf(Constants.IdPROP_RDN))){//individuos
			if(!this.idsprocesados.contains(i.getIDO())){
				int ido=Integer.valueOf(i.getIDO());
				this.hmRdnxIDO.put(ido,i.getVALUE());
				int idto=Integer.valueOf(i.getIDTO());
				this.individuos.add(Integer.valueOf(i.getIDO()));
				if(!this.hmIDTOxIDO.containsKey(ido)){
					this.hmIDTOxIDO.put(ido, idto);
				}
				this.idsprocesados.add(i.getIDO());
			}
		}
	} 
	
	
	private void deduceLevels(){
		Iterator<Instance> it = this.instances.iterator();	
		while(it.hasNext()){
			Instance i = (Instance)it.next();
			this.setLevel(i);
		}	 
	}

	public boolean isInstaceOFIndividual(Instance i){
		return  i.getIDO()!=null&&i.getOP()==null&&i.getPROPERTY()!=null;
	}
	
	public boolean isInstanceObjectPropertyWithRdnValue(Instance i){
		return  i.getVALUE()!=null && (i.getOP().equals(Constants.OP_ONEOF)
										||(i.getOP().equals(Constants.OP_DEFAULTVALUE) && !Constants.isDataType(Integer.valueOf(i.getVALUECLS()))));
	}
	
	private void createInitialFacts(HashMap<Integer, HashSet<Integer>> mapRequiredDomProps) throws NotFoundException, IncoherenceInMotorException  {
		Iterator<Instance> it =this.instances.iterator();
		while(it.hasNext()){
			Instance ins=it.next();
			if(this.isInstaceOFIndividual(ins)){
				this.instancesOfIndividual.add(ins);
			}
			else if(this.isInstanceObjectPropertyWithRdnValue(ins)){
				this.instancesObjectPropertiesWithRdnValue.add(ins);
			}
			else
			{	
				FactInstance factActual = ins.toFactInstance();
				
				if(this.getHmClassNamexIDTO().get(factActual.getVALUECLS())!=null){
					factActual.setRANGENAME(this.getHmClassNamexIDTO().get(factActual.getVALUECLS()));
				}
				else if(factActual.getVALUECLS()!=null&&Constants.isDataType(factActual.getVALUECLS())){
					factActual.setRANGENAME(Constants.getDatatype(factActual.getVALUECLS()));
				}
				else if(!Auxiliar.equals(factActual.getOP(), Constants.OP_CARDINALITY)){
					int idto=factActual.getIDTO();
					if(idto!=Constants.IDTO_IMPORT&&idto!=Constants.IDTO_EXPORT&&idto!=Constants.IDTO_ACTION&&idto!=Constants.IDTO_ACTION_PARAMS&&idto!=Constants.IDTO_UTASK&&idto!=Constants.IDTO_QUESTION_TASK&&idto!=Constants.IDTO_ACTION_BATCH&&idto!=Constants.IDTO_ACTION_BATCH_PARAMS){
						System.err.println("\n\n -------- WARNING: CASO NO CONTEMPLADO EN RULERFACTORYCOMMON\n factActual="+factActual+"\n------------");
					}
				}else{
					if(mapRequiredDomProps.containsKey(factActual.getIDTO()) && mapRequiredDomProps.get(factActual.getIDTO()).contains(factActual.getPROP())){
						factActual.setQMIN(1.0);
						mapRequiredDomProps.get(factActual.getIDTO()).remove(factActual.getPROP());
					}
				}
				factActual.setExistia_BD(true)	;
				ruleEngine.insertFact(factActual,ins.isVIRTUAL());
			}
		}
		//En los requeridos que quedan por procesar, creamos un fact cardinalidad para los que no hemos encontrado porque tienen cardinalidad null null
		for(int idto:mapRequiredDomProps.keySet()){
			for(int idProp:mapRequiredDomProps.get(idto)){
				FactInstance fact=new FactInstance(idto, null, idProp, null, null, null, 1.0, null, Constants.OP_CARDINALITY, this.hmClassNamexIDTO.get(idto));
				ruleEngine.insertFact(fact);
			}
		}
	}


	private void insertIndividualInitialFacts(DocDataModel kb) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException  {
		Iterator<Instance> it =this.instancesOfIndividual.iterator();
		ArrayList<Integer> registeredIdos=new ArrayList<Integer>();
		while(it.hasNext()){
			Instance ins=it.next();
			FactInstance factActual = ins.toFactInstance();
			if(this.getHmClassNamexIDTO().get(factActual.getVALUECLS())!=null){
				factActual.setRANGENAME(this.getHmClassNamexIDTO().get(factActual.getVALUECLS()));
			}
			else if(factActual.getVALUECLS()!=null&&Constants.isDataType(factActual.getVALUECLS())){
				factActual.setRANGENAME(Constants.getDatatype(factActual.getVALUECLS()));
			}
			else{
				System.err.println("\n\n WARNING RULERFACTORYCOMMON: CASO NO CONTEMPLADO EN QUERULERFACTORYCOMMON.insertIndividualInitialFacts  \n factActual="+factActual);
			}
			if(!registeredIdos.contains(factActual.getIDO())){
				kb.registerInfoObject(factActual.getIDO(), factActual.getIDTO(), Constants.LEVEL_INDIVIDUAL);
				registeredIdos.add(factActual.getIDO());
			}
			
			factActual.setExistia_BD(true)	;
			ruleEngine.insertFact(factActual);
		}
	}
	
	/**
	 * Inserta los instances que tienen como OP=ONEOF y con VALUE relleno. Antes de insertarlos, cambiamos el value que llega desde server ( rdn de un individuo) por su ido (como lo entiende el applet)
	 * @param kb
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws OperationNotPermitedException
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 */
	private void insertOneOfInitialFacts(DocDataModel kb) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, JDOMException, ParseException, SQLException, NamingException  {
		
		//Hacemos una primera iteracion para saber de que clases necesitamos saber los individuos
		HashSet<Integer> idtoList=new HashSet<Integer>();
		Iterator<Instance> it =this.instancesObjectPropertiesWithRdnValue.iterator();
		while(it.hasNext()){
			Instance ins=it.next();
			idtoList.add(Integer.valueOf(ins.getVALUECLS()));
		}
		
		//Creamos un mapa con los individuos que necesitaremos para reemplazar los rdn por idos
		HashMap<Integer,HashMap<String,Integer>> enumeratedMap=new HashMap<Integer, HashMap<String,Integer>>();
		for(int idto:idtoList){
			HashSet<Integer> individuals=kb.getIndividualsOfLevel(idto, Constants.LEVEL_INDIVIDUAL);
			/*Comentado ya que mejor en el siguiente bucle hacemos Auxiliar.getIdoFromServer y asi solo obtenemos el ido que queremos (sin cargarlo en motor ni nada), no vaya a ser que sea de una clase con muchos individuos
			 if(individuals==null || individuals.isEmpty()){//Si no existe en motor cargamos sus individuos
				individuals=kb.loadInRulerFactsOfIndividualOfClass(idto, Constants.LEVEL_INDIVIDUAL);
			}*/
			
			for(int ido:individuals){
				String rdn=kb.getRdnIfExistInRuler(ido);
				HashMap<String,Integer> list=enumeratedMap.get(idto);
				if(list==null){
					list=new HashMap<String, Integer>();
					enumeratedMap.put(idto, list);
				}
				list.put(rdn, ido);
			}
		}
		
		//Modificamos los facts y los insertamos en motor
		it =this.instancesObjectPropertiesWithRdnValue.iterator();
		while(it.hasNext()){
			Instance ins=it.next();
			FactInstance factActual = ins.toFactInstance();
			
			int idto=factActual.getVALUECLS();
			String rdn=factActual.getVALUE();
			HashMap<String,Integer> map=enumeratedMap.get(idto);
			Integer ido=null;
			if(map==null || map.get(rdn)==null){
				System.err.println("Buscando en base de datos idto:"+idto+" rdn:"+rdn+ " fact: "+factActual.toString());
				instance fi=Auxiliar.getIdoFromServer(idto, rdn, null, kb);
				if(fi==null) continue;//TODO, dar error si el continue no se debe a un fact que hereda de clase report, en cuyo caso 
				//es normal que no este cacheado en un modelo multiempresa
				ido=fi.getIDO();
			}else{
				ido=map.get(rdn);
			}
			
			factActual.setVALUE(String.valueOf(ido));
			ruleEngine.insertFact(factActual);
		}
	}
	
	private void createInitialAccess() throws NotFoundException, IncoherenceInMotorException  {
		
		Iterator<Access> it = this.accesses.iterator();
		while(it.hasNext()){
			ruleEngine.insertFact(it.next());
		} 
	}

	private void createInitialRules() throws EngineException{
		if(this.rulesFiles!=null&&this.rulesFiles.size()>0)
			ruleEngine.inicializeRules(/*this.fileRules,*/(ArrayList<Package>)server.serverGetRules(rulesFiles));
		else if(this.rulesPackages!=null&&this.rulesPackages.size()>0)
			ruleEngine.inicializeRules(this.rulesPackages);
	}
	
	private void createGlobalParameters(DocDataModel ddm) throws EngineException{
		try{
			ruleEngine.setGlobal("dma", ddm.getDataModelAdapter());
		}catch(Exception ex){
			System.err.println("WARNING: La variable global no existe en el archivo de reglas  no es del tipo indicado");
		}
	}
	
	public void cargarProtosUtask(IKnowledgeBaseInfo ik,String user) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException{
			((DocDataModel)ik).createAllUtaskPrototypes(null,user);	
	}
	
	public void initIKnowledgeBaseInfo(IKnowledgeBaseInfo kb,String user) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException{
		kb.setServer(server);
		//IMPORTANTE:  ANTES DE RUNs ESTEN INSERTADOS ruleEngine(JbossEngine) y kb(DocDataModel) 
		ruleEngine.insertFact(ruleEngine);
		DocDataModel ddm=(DocDataModel)kb;
		ruleEngine.insertFact(ddm);
		//insertamos en motor la api de reglas
		ruleEngine.insertFact(ddm.getDataModelAdapter());	
		
		this.insertIndividualInitialFacts((DocDataModel)kb);
		((DocDataModel)kb).buildIndexSpecialized();
		
		this.insertOneOfInitialFacts((DocDataModel)kb);
		
		ruleEngine.run(Constants.ADVANCEDCONFIGURATION_RULES);
		kb.loadMetaData();//impte loadMetaData siempre despues de run advancedconfiguration pq en esas reglas se puede cambiar el modelo.
		ruleEngine.run(Constants.INICIALIZERULESGROUP);
		((DocDataModel)kb).buildAccessSpecialized();
		
		//CUANDO EL USUARIO SEA SYSTEM NO CREARA AUTOMATICAMENTE PROTO DE TODAS LAS UTASK PQ ES UN SOPORTE GRAFICO DADO AL APPLET
		//IMPORTANTE CUANDO SE CREA RULER DSDE BBDD ASIGNAR COMO USUARIO EL SISTEMA PARA QUE NO PIERDA TIEMPO EN CREAR PROTOS DE LAS UTASK
		if(kb instanceof DocDataModel&&!user.equals(Constants.USER_SYSTEM))
			cargarProtosUtask(kb,user);
		
		//Importante que se despues de dispararse las reglas de inicializeRules ya que si no la de herencias actua con los accesos que creamos para las abstractas
		((DocDataModel)ddm).buildAccessEngine();//Importante antes para que buildAccessOfAbstractClasses pueda utilizarlo
		((DocDataModel)kb).buildAccessOfAbstractClasses();
		
		ruleEngine.run(Constants.RULESGROUP_rules);
		
	}
	
//	public void notifyPropertyLabelToMotor(DocDataModel dm){
//		dm.setHmNamePropXIdProp(this.hmNamePropXIdProp);
//	}

	public HashMap<Integer, String> getHmClassNamexIDTO() {
		return hmClassNamexIDTO;
	}



	public void setHmClassNamexIDO(HashMap<Integer, String> hmClassNamexIDTO) {
		this.hmClassNamexIDTO = hmClassNamexIDTO;
	}

	

}
