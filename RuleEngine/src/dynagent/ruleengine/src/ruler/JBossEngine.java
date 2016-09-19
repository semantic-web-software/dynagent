package dynagent.ruleengine.src.ruler;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import javax.naming.NamingException;

import org.drools.FactHandle;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.RuntimeDroolsException;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.base.DefaultConsequenceExceptionHandler;
import org.drools.rule.Package;
import org.drools.rule.Rule;
import org.drools.spi.Activation;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.common.basicobjects.Properties;
import dynagent.common.basicobjects.T_Herencias;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.meta.api.DataModelAdapter;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QueryValue;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QuestionRequest;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.StringChanged;
import dynagent.ruleengine.src.ruler.queryDef.QueryAccessDef;
import dynagent.ruleengine.src.ruler.queryDef.QueryHierarchyDef;
import dynagent.ruleengine.src.ruler.queryDef.QueryInstanceDef;
import dynagent.ruleengine.src.ruler.queryDef.QueryPropertyDef;

public class JBossEngine extends CommonEngine{
	
	private ArrayList<String> rules;
	private RuleBase ruleBase;
	//private PackageBuilder builder;
	private WorkingMemory workingMemory;
	private IKnowledgeBaseInfo ik;
	
	public JBossEngine(IKnowledgeBaseInfo ik){
		this.ik=ik;
		rules = new ArrayList<String>();
//		ruleBase = RuleBaseFactory.newRuleBase();
		//Thread.currentThread().setContextClassLoader( ClassLoader.getSystemClassLoader() );
		//PackageBuilderConfiguration pb=new PackageBuilderConfiguration(this.getClass().getClassLoader());		
		//builder = new PackageBuilder(pb);
		
		RuleBaseConfiguration conf = new RuleBaseConfiguration();
		conf.setConsequenceExceptionHandler(new DefaultConsequenceExceptionHandler(){

			public void handleException(Activation activation, WorkingMemory workingMemory, Exception exception) {
				throw new MyConsequenceException(exception,workingMemory,activation);
			}
			
		});

		ruleBase = RuleBaseFactory.newRuleBase( conf );

		workingMemory = ruleBase.newStatefulSession();
		((StatefulSession)workingMemory).dispose();
		//workingMemory.setFocus(Constants.QUERYGROUP_rules);
//		workingMemory.addEventListener(new DefaultAgendaEventListener(){
//
//			@Override
//			public void activationCancelled(ActivationCancelledEvent arg0, WorkingMemory arg1) {
//				/*System.out.println("ActivacionCancelled activation:"+arg0);*/
//				super.activationCancelled(arg0, arg1);
//			}
//
//			@Override
//			public void activationCreated(ActivationCreatedEvent arg0, WorkingMemory arg1) {
//				System.err.println("activationCreated activation:"+arg0);
//				super.activationCreated(arg0, arg1);
//			}
//			
//		});
	}

	public ArrayList<String> getRules() {
		return rules;
	}

	public void setRules(ArrayList<String> rules) {
		this.rules = rules;
	}

	
	
	public WorkingMemory getWorkingMemory() {
		return workingMemory;
	}

	public void setWorkingMemory(WorkingMemory workingMemory) {
		this.workingMemory = workingMemory;
	}
		

	public synchronized int deleteFactCondRETRACT(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin,Object op) throws NotFoundException, IncoherenceInMotorException{
	
		int cont = 0;
		Iterator<IPropertyDef> it =  this.getAllInstanceFactsNoRevert(idto, ido, prop, valuecls, value, null, qMax, qMin, op).iterator();
		while(it.hasNext()){
			Fact f = (Fact) it.next();
			workingMemory.retract(f.getFactHandle());
			cont++;
		}
		return cont;
	}



	public LinkedList<FactHierarchy> getAllHierarchyFacts(Object idto, Object idtoSup) throws NotFoundException, IncoherenceInMotorException {
		LinkedList<FactHierarchy> hierarchy = new LinkedList<FactHierarchy>();
		if(idtoSup == null && idto != null && !(idto instanceof Null))
			hierarchy = getHierarchyFactsWhereIdto(((Integer) idto).intValue());
		else if(idto == null && idtoSup != null && !(idtoSup instanceof Null))
			hierarchy = getHierarchyFactsWhereIdtoSup(((Integer) idtoSup).intValue());
		else if(idto != null && !(idto instanceof Null) && idtoSup != null && !(idtoSup instanceof Null))
			hierarchy = getHierarchyFactsWhereIdtoAndIdtoSup(((Integer) idto).intValue(),((Integer) idtoSup).intValue());
		
		else{
			//LA REGLAS DE QUERY SE DEBEN DISOCNTINUAR POR MULTIPLES PROBLEMAS(problemas focos, rendimiento,.."+instances);
			System.err.println("\n\n jbossEngine.getAllHierarchyFacts() :QUERY NO IMPLEMENTADA! No esta definida la  query; idto="+idto+"idtoSup="+idtoSup);
			throw new NotFoundException("\n\n  jbossEngine.getAllHierarchyFacts() QUERY NO IMPLEMENTADA! \n\n QUERY NO IMPLEMENTADA: No esta definida la  query idto="+idto+"idtoSup="+idtoSup);
		}
		
		return hierarchy;
		
	}

	public LinkedList<IPropertyDef> getAllInstanceFacts(Object idto, Object ido, Object prop, Object valuecls, Object value,Object name, Object qMax, Object qMin,Object op) throws NotFoundException, IncoherenceInMotorException {
		
		
		LinkedList<IPropertyDef> res = new LinkedList<IPropertyDef>();
		LinkedList<IPropertyDef> instances = this.getAllInstanceFactsNoRevert(idto, ido, prop, valuecls, value, name, qMax, qMin, op);
			
		Iterator it = instances.iterator();
		while(it.hasNext()){
			Fact  f2 = (Fact) it.next();
			FactInstance f = f2.toFactInstance();
			res.add(f);
		}
	
		return res;
	}
	
	public synchronized LinkedList<FactProp> getAllPropertyFacts(Object prop, Object name) throws NotFoundException, IncoherenceInMotorException {
		
		LinkedList<FactProp> properties = new LinkedList<FactProp>();
		
		if(name == null && prop != null && !(prop instanceof Null)){
			//System.out.println("queryProp");
			properties = getPropertyFactsWhereIdProp(((Integer)prop).intValue());
		}
		else if(name == null && prop == null){
			//System.out.println("queryProp");
			properties = getAllPropertyFacts();
		}
		else if(name!=null&&prop==null){
			Object[] atribs = {name}; 
			QueryResults queryRes = workingMemory.getQueryResults("getPropertyFactsWhereName", atribs);
			properties= convertToLinkedListProp(queryRes);
		}
		else{	//LA REGLAS DE QUERY SE DEBEN DISOCNTINUAR POR MULTIPLES PROBLEMAS(problemas focos, rendimiento,.."+instances);
			System.err.println("\n\n jbossEngine.getAllPropertyFacts() :QUERY NO IMPLEMENTADA! No esta definida la  query; prop="+prop+"name="+name);
			throw new NotFoundException("\n\n  jbossEngine.getAllPropertyFacts() QUERY NO IMPLEMENTADA! \n\n QUERY NO IMPLEMENTADA: prop="+prop+"name="+name);

		
		}
		return properties;
	}
	

	public synchronized void insertFact(Object fact) throws NotFoundException, IncoherenceInMotorException {
		insertFact(fact,false);
	}
	
	
	public synchronized void insertFactDatasRule( ArrayList datosToRuler) throws NotFoundException, IncoherenceInMotorException {
	 
		for(int i=0;i<datosToRuler.size();i++){
		 this.insertFactDataRules((DataRules)datosToRuler.get(i));
		 //System.err.println("\n DEBUG JBOSSENGINE.inserFactDataRules:\n"+datosToRuler.get(i));
	 }
	}

	
	public synchronized void insertFact(Object fact,boolean virtual) throws NotFoundException, IncoherenceInMotorException {
		try{

			 if(fact instanceof IPropertyDef)
				insertFactInstance((IPropertyDef) fact, virtual);
			else if(fact instanceof  FactAccess)
				insertFactAccess((FactAccess)fact);
			else if(fact instanceof FactProp)
				insertFactProp((FactProp) fact);
			else if(fact instanceof FactHierarchy)
				insertFactHierarchy((FactHierarchy) fact);
			else if(fact instanceof Access)
				insertFactAccess((Access) fact);
			else if(fact instanceof  Individual){
				insertFactIndividual((Individual)fact);
			}
			else if(fact instanceof  Lock){
				insertFactLock((Lock)fact);
			}
			/*else if(fact instanceof  Deleted){
				workingMemory.insert(fact);
			}*/
			else if(fact instanceof  DataRules){
				insertFactDataRules((DataRules)fact);
			}
			else if((fact instanceof  DocDataModel)||(fact instanceof QueryInstanceDef) || (fact instanceof QueryPropertyDef) || (fact instanceof QueryAccessDef) || (fact instanceof QueryHierarchyDef)){
				//System.out.println("insertFact:"+fact);
				workingMemory.insert(fact);
			}/*else if(fact instanceof DocDataModel)
				insertFactDocDataModel((DocDataModel)fact);*/
			else if(fact instanceof  JBossEngine){
				workingMemory.insert(fact);
			}
			else if(fact instanceof Properties)
				insertFactProp((Properties) fact);
			else if(fact instanceof Instance)
				insertFactIns((Instance) fact, virtual);
			else if(fact instanceof T_Herencias){
				System.err.println("JbossEngine.insertFact con THerencias");
				Auxiliar.printCurrentStackTrace();
				//insertFactHierarchy((T_Herencias) fact);
			}
			else{ //System.err.println("JBossEngine.insertFact:Tipo de Fact no conocido:"+fact+"   fact.class="+fact.getClass());
				workingMemory.insert(fact);
			}
		}catch(RuntimeDroolsException ex){
			IncoherenceInMotorException e=new IncoherenceInMotorException(ex.getMessage());
			e.setStackTrace(ex.getStackTrace());
			e.setUserMessage("ERROR GRAVE. Los datos de la aplicacion han quedado corruptos. Reinicie la aplicacion.");
			throw e;
		}
		//System.out.println("El tamaño de la agenda es: "+workingMemory.getAgenda().agendaSize());
	}
	
	private synchronized void insertFactInstance(IPropertyDef def, boolean virtual) throws NotFoundException, IncoherenceInMotorException {
		Fact f = toFact(def,virtual);
		//System.err.println("\n\n debug inserFactInstace:\n"+f);
		FactHandle fh = workingMemory.insert(f,true);
		f.setFactHandle(fh);
	}
	
	private synchronized void insertFactIndividual(Individual ind) throws NotFoundException
	{		
		if(ind.getLEVEL()!=Constants.LEVEL_INDIVIDUAL&&ind.getIDO()>0){
			System.err.println("\n WARNING FACT INDIVIDUAL CON IDO >0 Y LEVEL NO DE INDIVIDUAL. IND: "+ind);
			Auxiliar.printCurrentStackTrace();
		}
		FactHandle fh = workingMemory.insert(ind,true);
		ind.setFactHandle(fh);
	}
	
	private void insertFactLock(Lock lock) throws NotFoundException
	{		
		FactHandle fh = workingMemory.insert(lock,true);
		lock.setFactHandle(fh);
	}
	
	private synchronized void insertFactAccess(Access access) {
		
		//FactAccess fa = factCreator.toFactAccess(access);
		Iterator<FactAccess> itr=toFactAccess(access).iterator();
		while(itr.hasNext()){
			FactAccess fa=itr.next();
			FactHandle fh = workingMemory.insert(fa,true);
			fa.setFactHandle(fh);
			//System.out.println("Insert factaccess..."+access);
			//System.out.println("Insert factaccess..."+fa);
		}
	}
	
	private synchronized void insertFactAccess(FactAccess fa) {
		
		FactHandle fh = workingMemory.insert(fa,true);
		fa.setFactHandle(fh);
		//System.out.println("Insert factaccess..."+access);
		
	}
	
	
	private synchronized void insertFactProp(FactProp f) {
		FactHandle fh = workingMemory.insert(f,true);
		f.setFactHandle(fh);
		
	}
	
	private synchronized void insertFactDataRules(DataRules d) throws NotFoundException
	{		
		FactHandle fh = workingMemory.insert(d,true);
		d.setFactHandle(fh);
	}

	/*private void insertFactHierarchy(T_Herencias herencias) {
		this.insertFactHierarchy(toFactHierarchy(herencias));
	}*/

	
	
	
	
	private synchronized void insertFactHierarchy(FactHierarchy fh) {
		FactHandle fhand=workingMemory.insert(fh,true);
		fh.setFactHandle(fhand);
	}
	
	
	
	private synchronized void insertFactIns(Instance instance, boolean virtual) throws NotFoundException, IncoherenceInMotorException {
		
		Fact f = toFact(instance.toFactInstance(),virtual);
		FactHandle fh = workingMemory.insert(f,true);
		f.setFactHandle(fh);
		//System.out.println("Insert factinstance..."+instance);
	}
	
	
	
	private void insertFactProp(Properties properties) throws NotFoundException {
		this.insertFactProp(toFactProp(properties));
	}

//	public void insertRules(String ruleFile) throws Exception{
//		
//		System.out.println( "Loading file: " + ruleFile );            
//		InputStream inStream = this.getClass().getResourceAsStream(ruleFile);
//		InputStreamReader inReader = new InputStreamReader(inStream);
//
//		builder.addPackageFromDrl(inReader );
//
//		if(builder.hasErrors())
//			System.out.println(builder.getErrors());
//
//		Package pkg = builder.getPackage();
//		ruleBase.addPackage( pkg );		
//	}
	
	
	
	
	/*public void fireRules(){
		System.err.println("--------Fire rules con el filtro!!!");
		//workingMemory.fireAllRules(new RuleNameStartsWithAgendaFilter("RULE:"));
		workingMemory.setFocus("reglas grupo 1");
		workingMemory.fireAllRules();
		workingMemory.clearActivationGroup("reglas grupo 1");
		//workingMemory.fireAllRules();
	}
	*/
	public void fireRules(String grupo){
		//System.err.println("--------Fire rules en modo: "+grupo);
		boolean success=false;
		try{
			//Auxiliar.printCurrentStackTrace();
			workingMemory.setFocus(grupo);
			workingMemory.fireAllRules();
			success=true;
		}finally{
			if(!success){
				workingMemory.clearActivationGroup(grupo);
				workingMemory.clearRuleFlowGroup(grupo);
				workingMemory.clearAgendaGroup(grupo);
				//Con esto obligamos a que quite este grupo de la focusStack. Pero estamos seguros que no volvera a dar la misma exception porque hemos limpiado las activaciones
				workingMemory.fireAllRules();
			}
		}
	}

	

		
	
	
	public int getMotorSize() {
		Iterator s = workingMemory.iterateObjects();
		int n=0;
		int naccess=0;
		int ninstances=0;
		int filter=0;
		while(s.hasNext()){
			
			try{
				//Fact f=(Fact)s.next();
				Object o=s.next();
				if(o instanceof FactAccess)
					naccess++;
				else if(o instanceof Fact){
					ninstances++;
					if(((Fact)o).getLEVEL().equals(Constants.LEVEL_FILTER)){
						filter++;
						//System.err.println(o);
					}
				}
				n++;
			}catch(Exception ex){
			//	System.err.println("No fact:"+s);
			}
		}
		System.err.println("*************************TAMAÑO DEL MOTOR:"+n+" factsinstances: "+ninstances+"  factnaccess:"+naccess+"   filter:"+filter);
		return n;
	}
		
	public void printMotor() {
		
		Iterator s = workingMemory.iterateObjects();
		System.out.println(workingMemory.getAgenda());
		
		System.out.println("******************************************CONTENIDO DEL MOTOR");
		while(s.hasNext()){
			Object fa=s.next();
			/*if(fa instanceof Fact && ((Fact)fa).getCLASSNAME()!=null){
				if(((Fact)fa).getCLASSNAME().equals("Cambiar_estado_emision"))
					System.out.println("******************************************"+fa);
			}
			else if(fa instanceof ObjValue && ((ObjValue)fa).getCLASSNAME()!=null){
				if(((ObjValue)fa).getCLASSNAME().equals("Cambiar_estado_emision"))
					System.out.println("******************************************"+fa);
			}*/
			System.out.println("******************************************"+fa);
			/*if(fa instanceof Individual){
				System.out.println("******************************************"+fa);
			}*/
		}
		
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdProp(int ido, int idProp, boolean delete){
		Object[] atribs = {ido, idProp};
        QueryResults queryRes;
        if(!delete)
        	queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdoAndIdProp", atribs);
        else
        	queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdoAndIdProp", atribs);
        return convertToLinkedListInstance(queryRes);
        
         
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdProp(int ido, int idProp){
		Object[] atribs = {ido, idProp};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdoAndIdProp", atribs);
        return convertToLinkedListInstance(queryRes);
        
         
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndIdPropAndIdoNull(int idto, int idProp, boolean delete){
		Object[] atribs = {idto, idProp};
        QueryResults queryRes;
        if(!delete)
        	queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoAndIdPropAndIdoNull", atribs);
        else
        	queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdtoAndIdPropAndIdoNull", atribs);
        return convertToLinkedListInstance(queryRes);
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndIdPropAndIdoNull(int idto, int idProp){
		Object[] atribs = {idto, idProp};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoAndIdPropAndIdoNull", atribs);
        return convertToLinkedListInstance(queryRes);
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereValueAndValueCls(String value,int valuecls) {
		String value2 = value.toString(); 
		Object[] atribs = {valuecls, value2}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWhereValueAndValueCls", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndIdPropNotNullAndIdoNull(int idto, boolean delete){
		Object[] atribs = {idto};
		QueryResults queryRes;
		if(!delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoAndIdPropNotNullAndIdoNull", atribs);
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdtoAndIdPropNotNullAndIdoNull", atribs);
        return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdo(int ido, boolean delete){
		Object[] atribs = {ido}; 
		QueryResults queryRes;
		if(!delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdo", atribs);
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdo", atribs);
		return convertToLinkedListInstance(queryRes);
        
	}
	
	
	public LinkedList<IPropertyDef> getInstanceFactsWhereIdo(int id){
		//TODO HACER HACIENDO UNA UNICA QUERY A MOTOR DONDE SOLO SE PASE EL IDO A LA QUERY
		LinkedList<IPropertyDef> allfacts=new LinkedList<IPropertyDef> ();	
		allfacts.addAll(this.getInstanceFactsWhereIdo(id,true));
		allfacts.addAll(this.getInstanceFactsWhereIdo(id,false));
		return allfacts;
		
	
	}
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoWithIdoNull(int idto, boolean delete){
		Object[] atribs = {idto}; 
		QueryResults queryRes;
		if(delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoWithIdoNull", atribs);
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdtoWithIdoNull", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndPropNotNull(boolean delete){
		QueryResults queryRes;
		if(!delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdoAndPropNotNull");
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdoAndPropNotNull");
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndPropNotNullAndIdoNull(boolean delete){
		QueryResults queryRes;
		if(!delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoAndPropNotNullAndIdoNull");
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdtoAndPropNotNullAndIdoNull");
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdtoNotNullAndIdoNull(boolean delete){
		QueryResults queryRes;
		if(!delete)
			queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdtoNotNullAndIdoNull");
		else
			queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedWhereIdtoNotNullAndIdoNull");
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereName(String name){
		Object[] atribs = {name}; 
		QueryResults queryRes = workingMemory.getQueryResults("getInstanceFactsWhereName", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<FactProp> getPropertyFactsWhereIdProp(int idProp){
		Object[] atribs = {idProp}; 
		QueryResults queryRes = workingMemory.getQueryResults("getPropertyFactsWhereIdProp", atribs);
		return convertToLinkedListProp(queryRes);
	}
	
	
	
	public synchronized LinkedList<FactProp> getAllPropertyFacts(){
		Object[] atribs = {}; 
		QueryResults queryRes = workingMemory.getQueryResults("getAllPropertyFacts", atribs);
		return convertToLinkedListProp(queryRes);
	}
	
	public synchronized LinkedList<FactHierarchy> getHierarchyFactsWhereIdto(int idto){
		Object[] atribs = {idto}; 
		QueryResults queryRes = workingMemory.getQueryResults("getHierarchyFactsWhereIdto", atribs);
		return convertToLinkedListHierarchy(queryRes);
	}
	
	public synchronized LinkedList<FactHierarchy> getHierarchyFactsWhereIdtoSup(int idtoSup){
		Object[] atribs = {idtoSup}; 
		QueryResults queryRes = workingMemory.getQueryResults("getHierarchyFactsWhereIdtoSup", atribs);
		return convertToLinkedListHierarchy(queryRes);
	}
	
	public synchronized LinkedList<FactHierarchy> getHierarchyFactsWhereIdtoAndIdtoSup(int idto, int idtoSup){
		Object[] atribs = {idto,idtoSup}; 
		QueryResults queryRes = workingMemory.getQueryResults("getHierarchyFactsWhereIdtoAndIdtoSup", atribs);
		return convertToLinkedListHierarchy(queryRes);
	}
	
	
	private LinkedList<IPropertyDef> convertToLinkedListInstance(QueryResults queryRes){
		LinkedList<IPropertyDef> res = new LinkedList<IPropertyDef>(); 
		 Iterator it = queryRes.iterator();
         while(it.hasNext()){
         	QueryResult qr = (QueryResult) it.next();
         	Fact f2 = (Fact) qr.get("f");
         	//FactInstance f = factCreator.revertFact(f2);
         	res.add(f2);
         }
         
        return res;
	}
	
	public LinkedList<StringChanged> getInitialValue(int ido, int prop){
		Object[] atribs = {ido,prop}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceValuesFacts", atribs);		
			
		LinkedList<StringChanged> res = new LinkedList<StringChanged>(); 
		 Iterator it = queryRes.iterator();
         while(it.hasNext()){
         	QueryResult qr = (QueryResult) it.next();
         	Fact f2 = (Fact) qr.get("f");
         	//FactInstance f = factCreator.revertFact(f2);
         	res.add(new StringChanged(f2.getINITIALVALOR(),f2.initialValuesChanged()));
         }
         
        return res;
	}
	
	private LinkedList<FactProp> convertToLinkedListProp(QueryResults queryRes){
		LinkedList<FactProp> res = new LinkedList<FactProp>(); 
		 Iterator it = queryRes.iterator();
         while(it.hasNext()){
         	QueryResult qr = (QueryResult) it.next();
         	FactProp f = (FactProp) qr.get("f");
         	res.add(f);
         }
         
        return res;
	}
	
	
	private LinkedList<FactAccess> convertToLinkedListAccess(QueryResults queryRes){
		LinkedList<FactAccess> res = new LinkedList<FactAccess>(); 
		 Iterator it = queryRes.iterator();
         while(it.hasNext()){
         	QueryResult qr = (QueryResult) it.next();
         	FactAccess f = (FactAccess) qr.get("f");
         	res.add(f);
         }
         //System.err.println("debu convertTolinkedlistaccess   res="+res);
        return res;
	}
	
	private LinkedList<FactHierarchy> convertToLinkedListHierarchy(QueryResults queryRes){
		LinkedList<FactHierarchy> res = new LinkedList<FactHierarchy>(); 
		 Iterator it = queryRes.iterator();
         while(it.hasNext()){
         	QueryResult qr = (QueryResult) it.next();
         	FactHierarchy f = (FactHierarchy) qr.get("f");
         	res.add(f);
         }
         
        return res;
	}
	


	public void inicializeRules(/*String fileRules*/ArrayList<Package> rulesPackage) throws EngineException{
		try{
			int numeroreglas=0;
			Iterator<Package> itr=rulesPackage.iterator();
			while(itr.hasNext()){
				Package pck=itr.next();
				ruleBase.addPackage(pck);
				//System.out.println("\n\n ==============  INFO INICIALIZERULES:==========================");
				Rule[] rulesp=pck.getRules();
				//System.out.println("\n.............. RULE PACKAGE LOADED: "+pck.getName()+"  with numRules:"+rulesp.length);
				for(int j=0;j<rulesp.length;j++){
					Rule rul=rulesp[j];
					//System.out.println("--->rule loaded "+rul.getName());
				}
			}

		}catch(Exception ex){
			EngineException e=new EngineException("Excepcion al inicializar las reglas"+ex);
			e.setStackTrace(ex.getStackTrace());

			throw e;
		}
	}
	
	
	/*
	 * NO FUNCIONA HECHAS VARIAS PRUEAS Y NO TIENE EFECTO UNA VEZ CONSTRUIDO WORKING MEMORY AÑADIR REGLAS, HACEMOS RUN TRAS AÑADIR PAKQUETE NUEVO Y NO SE DISPARAN
	 * public void addPackageRules(Package rulesPackage) throws EngineException{
		try{
			int numeroreglas=0;
			this.getWorkingMemory().getRuleBase().addPackage(rulesPackage);
				System.out.println("\n\n ==============  INFO addPackageRules:==========================");
				Rule[] rulesp=rulesPackage.getRules();
				System.out.println("\n.............. RULE PACKAGE LOADED: "+rulesPackage.getName()+"  with numRules:"+rulesp.length);
				for(int j=0;j<rulesp.length;j++){
					Rule rul=rulesp[j];
					System.out.println("--->rule loaded "+rul.getName());
				}
		}catch(Exception ex){
			EngineException e=new EngineException("Excepcion al inicializar las reglas"+ex);
			e.setStackTrace(ex.getStackTrace());
			throw e;
		}
	}
	*/
	

	public synchronized LinkedList<FactAccess> getAccessFactsOfProperty(int idto, Integer ido, int idProp, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
	
		Object[] atribs = {idto,ido, idProp, userRol, user, usertask};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getAccessFactsOfProperty", atribs);
        LinkedList<FactAccess> access=convertToLinkedListAccess(queryRes);
        //System.err.println("busqueda con....:\n"+"{idto,ido, idProp, userRol, user, usertask}:{"+idto+" "+ido+" "+idProp+" "+userRol+" "+user+" "+usertask+"}");
        //System.err.println("....encuentra:\n"+access);
        //System.err.println("....fin debug getAccessFactsOfProperty");
        return access;
		
	}
	
	public synchronized LinkedList<FactAccess> getAccessFactsIdoRequired(int idto, int ido, int idProp, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		
		Object[] atribs = {idto,ido, idProp, userRol, user, usertask};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getAccessFactsIdoRequired", atribs);
        LinkedList<FactAccess> access=convertToLinkedListAccess(queryRes);
        //System.err.println("busqueda con....:\n"+"{idto,ido, idProp, userRol, user, usertask}:{"+idto+" "+ido+" "+idProp+" "+userRol+" "+user+" "+usertask+"}");
        //System.err.println("....encuentra:\n"+access);
        //System.err.println("....fin debug getAccessFactsOfProperty");
        return access;
		
	}

	public synchronized LinkedList<FactAccess> getAccessFactsOfPropertyAndIdtoRequired(int idto, Integer ido, int idProp, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		
		Object[] atribs = {idto,ido, idProp, userRol, user, usertask};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getAccessFactsOfPropertyAndIdtoRequired", atribs);
        LinkedList<FactAccess> access=convertToLinkedListAccess(queryRes);
        //System.err.println("busqueda con....:\n"+"{idto,ido, idProp, userRol, user, usertask}:{"+idto+" "+ido+" "+idProp+" "+userRol+" "+user+" "+usertask+"}");
        //System.err.println("....encuentra:\n"+access);
        //System.err.println("....fin debug getAccessFactsOfProperty");
        return access;
		
	}
	
	public synchronized LinkedList<FactAccess> getAccessFactsOverObject(Integer idto, Integer ido, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		Object[] atribs = {idto,ido, userRol, user, usertask};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getAccessFactsOverObject", atribs);
        LinkedList<FactAccess> access=convertToLinkedListAccess(queryRes);
        //System.err.println("busqueda con....:\n"+"{idto,ido, userRol, user, usertask}:{"+idto+" "+ido+" "+userRol+" "+user+" "+usertask+"}");
        //System.err.println("....encuentra:\n"+access);
        //System.err.println("....fin debug getAccessFactsOverObject");
        return access;
		//System.err.println("\n\n  1.2 DEBUG Jbossengine.getAccessFactsOverObject encuentra access=\n"+accs);
		//System.out.println("\n\n ..1.3................FIN DEBUG Jbossengine.getAccessFactsOverObject");
	}

	public synchronized LinkedList<FactAccess> getAllAccessFacts() throws NotFoundException, IncoherenceInMotorException {
		
		Object[] atribs = {};
        QueryResults queryRes;
        queryRes = workingMemory.getQueryResults("getAllAccessFacts", atribs);
        LinkedList<FactAccess> access=convertToLinkedListAccess(queryRes);
        //System.err.println("busqueda con....:\n"+"{idto,ido, idProp, userRol, user, usertask}:{"+idto+" "+ido+" "+idProp+" "+userRol+" "+user+" "+usertask+"}");
        //System.err.println("....encuentra:\n"+access);
        //System.err.println("....fin debug getAccessFactsOfProperty");
        return access;
		
	}

	protected LinkedList<IPropertyDef> getAllInstanceFactsNoRevert(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin,Object op ) throws NotFoundException, IncoherenceInMotorException {

		LinkedList<IPropertyDef> res = new LinkedList<IPropertyDef>();
		LinkedList<IPropertyDef> instances = new LinkedList<IPropertyDef>();
			if(ido != null && (ido instanceof Null) && ((Null) ido).getSearchBy() == Null.NULL && prop != null && (prop instanceof Null) && ((Null) prop).getSearchBy() == Null.NOTNULL && idto != null && !(idto instanceof Null) && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				res=getInstanceFactsWhereIdtoAndIdPropNotNullAndIdoNull(((Integer) idto).intValue(),false);
			}
			else if(ido != null && (ido instanceof Null) && ((Null) ido).getSearchBy() == Null.NULL && prop != null && !(prop instanceof Null) && idto != null && !(idto instanceof Null) && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryPropAndIdtoANdIdoNull");
				res=getInstanceFactsWhereIdtoAndIdPropAndIdoNull(((Integer) idto).intValue(), ((Integer) prop).intValue(),false);
			}
			else if(ido != null && !(ido instanceof Null) && prop != null && !(prop instanceof Null) && idto == null && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryPropAndIdo");
				res=getInstanceFactsWhereIdoAndIdProp(((Integer) ido).intValue(), ((Integer) prop).intValue(),false);
			}			
			else if(ido != null && !(ido instanceof Null) && prop == null && idto == null && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryIdo");
				res=getInstanceFactsWhereIdo(((Integer) ido).intValue(),false);
			}
			else if(idto != null && !(idto instanceof Null) && prop == null && (ido instanceof Null) && ((Null) ido).getSearchBy() == Null.NULL && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryIdtoAndIdoNull");
				res=getInstanceFactsWhereIdtoWithIdoNull(((Integer) idto).intValue(),false);
			}
			else if(idto == null && ido instanceof Null && ((Null) ido).getSearchBy() == Null.NOTNULL && prop instanceof Null && ((Null) prop).getSearchBy() == Null.NOTNULL && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryIdoAndPropNotNull");
				res=getInstanceFactsWhereIdoAndPropNotNull(false);
			}
			else if((ido instanceof Null) && ((Null) ido).getSearchBy() == Null.NULL && idto instanceof Null && ((Null) idto).getSearchBy() == Null.NOTNULL && prop instanceof Null && ((Null) prop).getSearchBy() == Null.NOTNULL && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryIdtoAndPropNotNullAndIdoNull");
				res=getInstanceFactsWhereIdtoAndPropNotNullAndIdoNull(false);
			}
			else if((ido instanceof Null) && ((Null) ido).getSearchBy() == Null.NULL && idto instanceof Null && ((Null) idto).getSearchBy() == Null.NOTNULL && prop == null && valuecls == null && value == null && name == null && qMax == null && qMin == null){
				//System.out.println("queryIdtoNotNullAndIdoNull");
				res=getInstanceFactsWhereIdtoNotNullAndIdoNull(false);
			}
			else if((prop instanceof Integer) && (valuecls instanceof Integer) && (value instanceof Integer) && idto==null && ido==null && qMax==null && qMin==null && op==null && name==null)
				res=getInstanceFactsWherePropAndValueAndValueCls(((Integer) prop).intValue(), ((Integer) valuecls).intValue(), (Integer) value);
			else if(idto instanceof Integer &&ido instanceof Integer &&prop instanceof Integer && valuecls instanceof Integer && value instanceof String && qMax instanceof Double && qMin instanceof Double  && op instanceof String)
					res=getInstanceFactsWhere((Integer) idto, (Integer) ido,(Integer)  prop, (String)value, (Integer) valuecls, (Double)qMin, (Double)qMax, (String)op);
			
			else if(idto == null && ido==null&&prop==null&&(value instanceof String)&& valuecls == null &&qMax == null && qMin == null&&op==null){
				System.err.println("\n\n-------------WARNING!! getAllInstanceFactsNoRevert: Se esta usando la query getInstanceFactsWhereValue deberia usarse getInstanceFactsWhereValueAndValueCls para evitar errores");
				Auxiliar.printCurrentStackTrace();
				res=this.getInstanceFactsWhereValue((String)value);
			}else if(idto == null && ido==null&&prop==null&&(value instanceof String)&& (valuecls instanceof Integer) &&qMax == null && qMin == null&&op==null){
				res=getInstanceFactsWhereValueAndValueCls((String)value,(Integer)valuecls);
			}
	
				
			else{
				System.err.println("\n\n QUERY NO IMPLEMENTADA: No esta definida la  query; idto="+idto+"ido="+ido+"prop="+prop+"valuecls="+valuecls+"value="+value+"  name="+name+"   qMax="+qMax+"  qMin="+qMin+"  op="+op);
				//LA REGLAS DE QUERY SE DEBEN DISOCNTINUAR POR MULTIPLES PROBLEMAS(problemas focos, rendimiento,.."+instances);
				throw new NotFoundException("\n\n QUERY NO IMPLEMENTADA: \n\n QUERY NO IMPLEMENTADA: No esta definida la  query; idto="+idto+"ido="+ido+"prop="+prop+"valuecls="+valuecls+"value="+value+"  name="+name+"   qMax="+qMax+"  qMin="+qMin+"  op="+op);
				
			}
			return res;	
	}

	private synchronized LinkedList<IPropertyDef> getInstanceFactsWherePropAndValueAndValueCls(int prop, int valuecls, Integer value) {
		String value2 = value.toString(); 
		Object[] atribs = {prop, valuecls, value2}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWherePropAndValueAndValueCls", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhere(Integer idto,Integer ido,Integer prop,String value, Integer valuecls, Double qmin,Double qmax,String op) {
		Object[] atribs = {idto,ido,prop,value,valuecls,qmin,qmax, op}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWhere", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	
	private synchronized LinkedList<IPropertyDef> getInstanceFactsDeletedOptimized(Integer ido,Integer prop) {
		Object[] atribs = {ido,prop}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsDeletedOptimized", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	

	public void run(String group) throws OperationNotPermitedException, InstanceLockedException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException{
		try{
			//System.err.println("\n\n ------->....DEBUG LLAMADA RUN GROUP="+group);
			//Auxiliar.printCurrentStackTrace();
			
			this.fireRules(group);
		}/*catch(ConsequenceException ex){
					
			String message=ex.getMessage();
			String[] buf=message.split(":");
			if(buf[0].equals(OperationNotPermitedException.class.getName())){
				String mssg=message.replaceAll(buf[0]+": ", "");//Asi evitamos que si hay algun ':' en el mensaje se corte
				OperationNotPermitedException e = new OperationNotPermitedException(mssg);
				e.setUserMessage(mssg);
				throw e;
			}else if(buf[0].equals(InstanceLockedException.class.getName())){
				String mssg=message.replaceAll(buf[0]+": ", "");//Asi evitamos que si hay algun ':' en el mensaje se corte
				InstanceLockedException e = new InstanceLockedException(mssg);
				e.setUserMessage(mssg);
				throw e;
			}else if(buf[0].equals(IncompatibleValueException.class.getName())){
				String mssg=message.replaceAll(buf[0]+": ", "");//Asi evitamos que si hay algun ':' en el mensaje se corte
				IncompatibleValueException e = new IncompatibleValueException(mssg);
				e.setUserMessage(mssg);
				throw e;
			}else{
				System.err.println("\n********CONSEQUENCEEXCEPCION NO TRATADA EN LA REGLA "+ex.getRule()+" message:"+message+" buf:"+buf+"\n*******");
			}
			throw ex;	
		}*/catch(ClassCastException ex){
			System.err.println("\n CASTEXCEPTION LOCALIZEMESSAGE="+ex.getLocalizedMessage());
			System.err.println("\n CASTEXCEPTION MESSAGE="+ex.getMessage());
			throw ex;	
		}catch(MyConsequenceException ex){
			Exception originalException=ex.getException();
			if(originalException instanceof OperationNotPermitedException)
				throw (OperationNotPermitedException)originalException;
			else if(originalException instanceof InstanceLockedException)
				throw (InstanceLockedException)originalException;
			else if(originalException instanceof IncompatibleValueException)
				throw (IncompatibleValueException)originalException;
			else if(originalException instanceof CardinalityExceedException)
				throw (CardinalityExceedException)originalException;
			else if(originalException instanceof RemoteSystemException)
				throw (RemoteSystemException)originalException;
			else
				System.err.println("\n********CONSEQUENCEEXCEPCION NO TRATADA EN LA REGLA "+ex.getRule()+" message:"+ex.getMessage()+"\n*******");
			
			throw ex;
		}
}
	
	

	 public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Integer ido, Integer prop){
		LinkedList<IPropertyDef> res = new LinkedList<IPropertyDef>();
		res=getInstanceFactsDeletedOptimized(ido, prop); 
		return res;
	}

	

	 public int deleteFact(IPropertyDef f,boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		 int cont=0;
		 	LinkedList<IPropertyDef> facts=this.getAllInstanceFacts(f.getIDTO().intValue(), f.getIDO(), f.getPROP(), f.getVALUE(), f.getVALUECLS() ,f.getQMIN(),f.getQMAX(),  f.getOP());
			if(facts.size()>1){
				System.err.println("WARNING: deleteFact(f="+f+")   encuentra mas de un fact:\n"+facts);
			}
			Iterator it=facts.iterator();
			while(it.hasNext()){
					Fact factFound=((Fact)it.next());
					factFound.deleteFactSessionable();
					if(setSystemValue){
						factFound.setSystemValue(f.getSystemValue());
						factFound.setAppliedSystemValue(f.isAppliedSystemValue());
					}
					cont++;
			}
			if(cont==0){
				System.err.println(" WARNING: deleteFact del Fact="+f+"   no  elimina de motor nada.\n la traza de llamadas fue:");
				Auxiliar.printCurrentStackTrace();
				
			}
			return cont;
		}

	@Override
	protected LinkedList<IPropertyDef> getAllInstanceFacts(int idto, Integer ido, int prop,  String value, Integer valuecls,Double qMin, Double qMax, String op) {
		LinkedList<IPropertyDef> res =this.getInstanceFactsWhere(idto, ido, prop, value, valuecls, qMin, qMax, op);
		return res;
	}

	
	/**
	 * A ESTE METODO SE LLAMA DESDE EL COMMIT CON FACTS, POR ESO OPTIMIZO HACIENDO UN RETRACT DIRECTAMENTE AL FACT EN ELUGAR
	 * DE BUSCARLO OTRA VEZ Y BORRARLO.
	 * @param f
	 */
	public synchronized void retractFact(IPropertyDef  f) {
		Fact fa;
		if(f instanceof Fact)
		 fa= (Fact)f;
		else
			fa=getFact(f);
		workingMemory.retract(fa.getFactHandle());
	}
	
	public synchronized void retractQueryLog(QueryValue  f) {
		workingMemory.retract(f.getFactHandle());
	}
	
	
	public synchronized void retractIndividual(Individual  f) {
		//System.err.println("Retract:"+f);
		workingMemory.retract(f.getFactHandle());
	}
	
	public void retractLock(Lock  f) {
		//System.err.println("Retract:"+f);
		workingMemory.retract(f.getFactHandle());
	}
	
	public Fact getFact(IPropertyDef condicion)
	{
		LinkedList<IPropertyDef> lfacts=this.getInstanceFactsWhere(condicion.getIDTO(), condicion.getIDO(),condicion.getPROP(),condicion.getVALUE(),condicion.getVALUECLS(), condicion.getQMIN(),condicion.getQMAX(), condicion.getOP());
		
		if(lfacts.size() > 1)
		{
			System.err.println("WARNING:- Existen varios facts como el fact instance pasado para hacer la busqueda en getFact");
		}else if(lfacts.isEmpty()){
			//System.err.println("WARNING: JBossEngine.getFact(): No existe en motor el Fact del factInstance: "+condicion);
			return null;
		}
		
		return (Fact)lfacts.getFirst();
	}

	public synchronized LinkedList<IPropertyDef> getAllInstanceValuesFacts(int ido, int prop) {
		Object[] atribs = {ido,prop}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceValuesFacts", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized Fact getFact(int ido,int prop){
		Object[] atribs = {ido,prop}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceValuesFacts", atribs);		
		Iterator it = queryRes.iterator();
        while(it.hasNext()){
        	QueryResult qr = (QueryResult) it.next();
        	Fact f2 = (Fact) qr.get("f");
        	return f2;
        }
		return null;
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls(int ido, int prop, String value, int valueCls) {
		Object[] atribs = {ido,prop,value,valueCls}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdPropAndValueNullAndValueCls(int ido, int prop, int valueCls) {
		Object[] atribs = {ido,prop,valueCls}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWhereIdoAndIdPropAndValueNullAndValueCls", atribs);
		return convertToLinkedListInstance(queryRes);
	}

	public synchronized LinkedList<IPropertyDef> getAllInstanceFactsNoDeletedWithIdo(int ido) {
		Object[] atribs = {ido}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWithIdo", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhere(int idto) {
		Object[] atribs = {idto}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWithIdto", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	
	public synchronized LinkedList<IPropertyDef> getInstanceFactsWhereValue(String value) {
		Object[] atribs = {value}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getInstanceFactsWhereValue", atribs);
		return convertToLinkedListInstance(queryRes);
	}
	

	public synchronized void retractFactAccess(FactAccess f) {
		workingMemory.retract(f.getFactHandle());
	}
	
	public synchronized void retractFactDataRules(DataRules f) {
		workingMemory.retract(f.getFactHandle());
	}
	
	public synchronized void retract(Object f) {
		if(f instanceof FactHierarchy){
			FactHierarchy fh=(FactHierarchy)f;
			workingMemory.retract(fh.getFactHandle());
		}
		else if(f instanceof FactAccess){
			FactAccess fa=(FactAccess)f;
			workingMemory.retract(fa.getFactHandle());
		}
		else if(f instanceof Fact){
			Fact fa=(Fact)f;
			workingMemory.retract(fa.getFactHandle());
		}
		else if(f instanceof FactProp){
			FactProp fp=(FactProp)f;
			workingMemory.retract(fp.getFactHandle());
		}
		else if(f instanceof Individual){
			Individual fp=(Individual)f;
			workingMemory.retract(fp.getFactHandle());
		}
		else if(f instanceof DataRules){
			DataRules fp=(DataRules)f;
			workingMemory.retract(fp.getFactHandle());
		}
		else System.err.print("\n\n WANING: CASO NO CONTEMPLADO EN JBOSSENGINE.retract f="+f);
		
	}

	

	public synchronized Individual getIndividualFact(int id) {
		Object[] atribs = {id}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getIndividualFact", atribs);
		Iterator it = queryRes.iterator();
		if (it.hasNext())
		{
			QueryResult qr = (QueryResult) it.next();
     		Individual f = (Individual) qr.get("f");
     		return f;
		}
		else
			return null;
		
	}
	
	public Lock getLockFact(int id) {
		Object[] atribs = {id}; 
		QueryResults queryRes;
		queryRes = workingMemory.getQueryResults("getLockFact", atribs);
		Iterator it = queryRes.iterator();
		if (it.hasNext())
		{
			QueryResult qr = (QueryResult) it.next();
     		Lock f = (Lock) qr.get("f");
     		return f;
		}
		else
			return null;
		
	}
	

	
	public  FactAccess denniedAccess(int idto, int prop, int idaccess) throws NotFoundException, IncoherenceInMotorException{
		FactAccess fa=new FactAccess(ik);
		fa.setIDTO(idto);
		fa.setDENNIED(1);
		fa.setACCESSTYPE(idaccess);
		fa.setPROP(prop);
		this.insertFact(fa);
		return fa;
	}

	public boolean setFactWithContributionValue(IPropertyDef facttomodify, HashMap<String,Number> mapIdoInitialValue, Number value,Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		boolean modified=false;
		Iterator<IPropertyDef> lfacts=this.getInstanceFactsWhere(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(),facttomodify.getVALUE(),facttomodify.getVALUECLS(), facttomodify.getQMIN(), facttomodify.getQMAX(), facttomodify.getOP()).iterator();
		if(lfacts.hasNext()){
			Fact f=(Fact)lfacts.next();
			if(f instanceof DatValue){
				DatValue datV=(DatValue)f;
				modified=datV.addContributionValue(mapIdoInitialValue,value,nDigRedondeo);
				//System.err.println("IsIncremental:"+datV.isIncremental());
			}else{
				IncompatibleValueException e=new IncompatibleValueException("No se puede incrementar el valor de un fact que no es dataValue. f:"+f);
				throw e;
			}
		}else{
			System.err.println("WARNING: El fact a modificar por setFactWithContributionValue no existe en motor fact:"+facttomodify);
		}
		
		return modified;
		
	}
	
	public boolean setFactWithIncrementalValue(IPropertyDef facttomodify, double incrementalValue,Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		boolean modified=false;
		Iterator<IPropertyDef> lfacts=this.getInstanceFactsWhere(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(),facttomodify.getVALUE(),facttomodify.getVALUECLS(), facttomodify.getQMIN(), facttomodify.getQMAX(), facttomodify.getOP()).iterator();
		if(lfacts.hasNext()){
			Fact f=(Fact)lfacts.next();
			if(f instanceof DatValue){
				DatValue datV=(DatValue)f;
				modified=datV.setIncrement(incrementalValue,nDigRedondeo);
				//System.err.println("IsIncremental:"+datV.isIncremental());
			}else{
				IncompatibleValueException e=new IncompatibleValueException("No se puede incrementar el valor de un fact que no es dataValue. f:"+f);
				throw e;
			}
		}else{
			System.err.println("WARNING: El fact a modificar por setFactWithContributionValue no existe en motor fact:"+facttomodify);
		}
		
		return modified;
		
	}
	
	public boolean setFactWithContributionValue(IPropertyDef facttomodify, String key, double incrementalValue,Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		boolean modified=false;
		Iterator<IPropertyDef> lfacts=this.getInstanceFactsWhere(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(),facttomodify.getVALUE(),facttomodify.getVALUECLS(), facttomodify.getQMIN(), facttomodify.getQMAX(), facttomodify.getOP()).iterator();
		if(lfacts.hasNext()){
			Fact f=(Fact)lfacts.next();
			if(f instanceof DatValue){
				DatValue datV=(DatValue)f;
				modified=datV.addContributionValue(key,incrementalValue,nDigRedondeo);
				//System.err.println("IsIncremental:"+datV.isIncremental());
			}else{
				IncompatibleValueException e=new IncompatibleValueException("No se puede incrementar el valor de un fact que no es dataValue. f:"+f);
				throw e;
			}
		}else{
			System.err.println("WARNING: El fact a modificar por setFactWithContributionValue no existe en motor fact:"+facttomodify);
		}
		
		return modified;
		
	}
	
	public void setFactWithDestination(IPropertyDef facttomodify,String destination){
		Iterator<IPropertyDef> lfacts=this.getInstanceFactsWhere(facttomodify.getIDTO(), facttomodify.getIDO(),facttomodify.getPROP(),facttomodify.getVALUE(),facttomodify.getVALUECLS(), facttomodify.getQMIN(), facttomodify.getQMAX(), facttomodify.getOP()).iterator();
		if(lfacts.hasNext()){
			Fact f=(Fact)lfacts.next();
			f.setDestinationSystem(destination,true);
		}
		
	}
	
	public synchronized void setGlobal(String name,Object value){
		workingMemory.setGlobal(name, value);
	}
	
	public Fact toFact(IPropertyDef f,boolean virtual) throws NotFoundException, IncoherenceInMotorException
	{
		
	//	System.err.println("\n DEBUG JBOSSENGINE.toFACT:"+f);
		Fact fact=null ;
		String nameProp=ik.getPropertyName(f.getPROP());//TODO DISCONTINUAR QUE PROPNAME SEA DEDUCIBLE CADA VEZ Y QUE PERSISTA EN TODOS LOS OBJETOS FACT, siempre se tiene informacion cd se crea el fact?
		if(nameProp==null){
			System.err.println("\n\n---------------------- WARNING JBOSSENGINE.toFact no puede obtener nameProp para prop:"+f.getPROP()+" f:"+f);
			Auxiliar.printCurrentStackTrace();
		}
		if(virtual){
			fact=new Virtual(f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(),f.getVALUE(),f.getRANGENAME(),f.getVALUECLS(),f.getQMIN(),f.getQMAX(),f.getOP(),ik);
		}else if(f.getIDO()!=null){
			int level=ik.getLevelOf(f.getIDO());

			if(f.getOP()==null){
				//hacemos una excepcion con las ACCIONES que se instancian como filtros (en lugar de como prototipos que seria mas correcto, pero hay que mantener como filtros de momento para no cambiar mucho otros proyectos)
				
				//VALORES DE INDIVIDUOS
				if(level==Constants.LEVEL_PROTOTYPE||level==Constants.LEVEL_INDIVIDUAL){
					if(ik.isDataProperty(f.getPROP())){
						fact = new DatValue(f.getIDTO(),f.getIDO(),f.getPROP(),f.getVALUE(),f.getVALUECLS(),f.getRANGENAME(),f.getQMIN(),f.getQMAX(),f.getOP(),f.getCLASSNAME(),f.getExistia_BD(),f.getSystemValue(),f.isAppliedSystemValue(),f.getDestinationSystem(),ik);
					}else{
						fact= new ObjValue(f.getIDTO(),f.getIDO(),f.getPROP(),f.getVALUE(),f.getVALUECLS(),f.getRANGENAME(),f.getQMIN(),f.getQMAX(),f.getOP(),f.getCLASSNAME(),f.getExistia_BD(),f.getSystemValue(),f.isAppliedSystemValue(),f.getDestinationSystem(),ik);
					}
				}
				else{
					boolean isUtask=ik.isSpecialized(f.getIDTO(), Constants.IDTO_UTASK);
					//VALORES DE ACCIONES 
					if(isUtask){
						if(ik.isDataProperty(f.getPROP())){
							fact = new DatValue(f.getIDTO(),f.getIDO(),f.getPROP(),f.getVALUE(),f.getVALUECLS(),f.getRANGENAME(),f.getQMIN(),f.getQMAX(),f.getOP(),f.getCLASSNAME(),f.getExistia_BD(),f.getSystemValue(),f.isAppliedSystemValue(),f.getDestinationSystem(),ik);
						}else{
							fact= new ObjValue(f.getIDTO(),f.getIDO(),f.getPROP(),f.getVALUE(),f.getVALUECLS(),f.getRANGENAME(),f.getQMIN(),f.getQMAX(),f.getOP(),f.getCLASSNAME(),f.getExistia_BD(),f.getSystemValue(),f.isAppliedSystemValue(),f.getDestinationSystem(),ik);
						}
					}
					//VALOR EN FILTRO
					else{
						//FilterValue(int ido,String clase,int idto,String propiedad, int idprop,String value,String rangename,int valuecls,double qmin,double qmax,IKnowledgeBaseInfo ik){
						fact=new FilterValue(f.getIDO(),f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(),f.getVALUE(),f.getRANGENAME(),f.getVALUECLS(),f.getQMIN(),f.getQMAX(),f.getOP(),ik);
					}
				}
					
			}
			//RANGEFILTER, ENLACE CON UN FILTRO (independientemente del level del dominio)
			else if(f.getIDO()!=null&&f.getOP().equals(Constants.OP_UNION)&&f.getVALUE()!=null&&ik.getLevelOf(Integer.parseInt(f.getVALUE()))==Constants.LEVEL_FILTER)
			{
				// RangeFilter(String classname,int idto,int ido, String propiedad,int idprop,String sidofiltervalue, String rangename,int valueCls,IKnowledgeBaseInfo ik) {
				fact=new RangeFilter(f.getCLASSNAME(),f.getIDTO(),f.getIDO(),nameProp,f.getPROP(),f.getVALUE(),f.getRANGENAME(),f.getVALUECLS(),ik);
			}
			//RESTRICCION DE CARDINALIDAD EN UN FILTRO
			else if(level==Constants.LEVEL_FILTER&&f.getOP()==Constants.OP_CARDINALITY){
				// FilterCardinality(int ido,String clase,int idto,String propiedad, int idprop,Double  qmin, Double qmax){
				fact=new FilterCardinality(f.getIDO(),f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(),f.getQMIN(),f.getQMAX(), ik);
			}
			//RESTRICCION DE NOTVALUE EN UN FILTRO
			else if(level==Constants.LEVEL_FILTER&&f.getOP()==Constants.OP_NEGATION){
				// FilterNotValue(int ido,String clase,int idto,String propiedad, int idprop,String value,String rangename,int valuecls,double qmin,double qmax,IKnowledgeBaseInfo ik){
				fact=new FilterNotValue(f.getIDO(),f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(),f.getVALUE(),f.getRANGENAME(),f.getVALUECLS(),f.getQMIN(),f.getQMAX(),ik);
			}
			//RESTRICCION DE CARDINALIDAD EN UNA INSTANCIA			
			else if(level==Constants.LEVEL_PROTOTYPE||level==Constants.LEVEL_INDIVIDUAL&&f.getOP()==Constants.OP_CARDINALITY){
				// IndividualCardinality(int ido,String clase,int idto,String propiedad, int idprop,Double  qmin, Double qmax){
				fact=new IndividualCardinality(f.getIDO(),f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(), f.getVALUECLS(), f.getQMIN(),f.getQMAX(), ik);
			}
		}else{//IDO==null--->definiciones de clases
			//public Model(String classname,int idto,String propiedad,int idprop, String value,String rangename, int valueCls,Double qmin, Double qmax, String op,IKnowledgeBaseInfo ik) {			
			fact=new Model(f.getCLASSNAME(),f.getIDTO(),nameProp,f.getPROP(),f.getVALUE(),f.getRANGENAME(),f.getVALUECLS(),f.getQMIN(),f.getQMAX(),f.getOP(),ik);
		}
		if(fact==null){//TODO QUITAR, SOLO TEMPORALMENTE PARA PRUEBAS
			throw new NotFoundException("\n\n: CASO NO CONTEMPLADO EN JBOSSENGINE.toFact() PARA: f:"+f);
		}
		return fact;		
	}
	
	public ArrayList<FactAccess> toFactAccess(Access a) {
		ArrayList<FactAccess> list=new ArrayList<FactAccess>();
		Iterator<String> itr=a.getACCESSTYPENAME().iterator();
		while(itr.hasNext()){
			String accessTypeName=itr.next();
			if(a.getUSER()!=null && !a.getUSER().isEmpty()){
				Iterator<String> itrUser=a.getUSER().iterator();
				while(itrUser.hasNext()){
					String user=itrUser.next();
					if(a.getUSERROL()!=null && !a.getUSERROL().isEmpty()){
						Iterator<String> itrUserRol=a.getUSERROL().iterator();
						while(itrUserRol.hasNext()){
							String userRol=itrUserRol.next();
							Integer[] arr=a.getPROP()==null?new Integer[1]:a.getPROP();							
							for(Integer aItemProp:arr){
								dynagent.ruleengine.src.ruler.FactAccess f = new dynagent.ruleengine.src.ruler.FactAccess(a.getIDTO(),a.getIDO(),aItemProp,a.getVALUE(),a.getVALUECLS(),userRol,user,a.getTASK(),a.getDENNIED(),Constants.getAccessType(accessTypeName),a.getPRIORITY(),ik);
								list.add(f);
							}
						}
					}else{
						Integer[] arr=a.getPROP()==null?new Integer[1]:a.getPROP();						
						for(Integer aItemProp:arr){
							dynagent.ruleengine.src.ruler.FactAccess f = new dynagent.ruleengine.src.ruler.FactAccess(a.getIDTO(),a.getIDO(),aItemProp,a.getVALUE(),a.getVALUECLS(),null,user,a.getTASK(),a.getDENNIED(),Constants.getAccessType(accessTypeName),a.getPRIORITY(),ik);
							list.add(f);
						}
					}
				}	
			}else if(a.getUSERROL()!=null && !a.getUSERROL().isEmpty()){
				Iterator<String> itrUserRol=a.getUSERROL().iterator();
				while(itrUserRol.hasNext()){
					String userRol=itrUserRol.next();
					Integer[] arr=a.getPROP()==null?new Integer[1]:a.getPROP();					
					for(Integer aItemProp:arr){
						dynagent.ruleengine.src.ruler.FactAccess f = new dynagent.ruleengine.src.ruler.FactAccess(a.getIDTO(),a.getIDO(),aItemProp,a.getVALUE(),a.getVALUECLS(),userRol,null,a.getTASK(),a.getDENNIED(),Constants.getAccessType(accessTypeName),a.getPRIORITY(),ik);
						list.add(f);
					}
				}
			}else{
				Integer[] arr=a.getPROP()==null?new Integer[1]:a.getPROP();
				
				for(Integer aItemProp:arr){
					dynagent.ruleengine.src.ruler.FactAccess f = new dynagent.ruleengine.src.ruler.FactAccess(a.getIDTO(),a.getIDO(),aItemProp,a.getVALUE(),a.getVALUECLS(),null,null,a.getTASK(),a.getDENNIED(),Constants.getAccessType(accessTypeName),a.getPRIORITY(),ik);
					list.add(f);
				}
			}
		}
		return list;
	}
	
	public FactProp toFactProp(Properties p) throws NotFoundException{
		String inversa=null;
		if(p.getPROPINV()!=null)
			inversa=ik.getPropertyName(p.getPROPINV());
		FactProp f = new FactProp(p.getNAME(),p.getPROP(),p.getVALUECLS(),p.getCAT(),inversa,p.getPROPINV(),ik);
		return f;
	}
	
	/*public FactHierarchy toFactHierarchy(T_Herencias h){
		FactHierarchy f = new FactHierarchy(h.getID_TO(),h.getID_TO_Padre());
		return f;
	}*/
	
	
	
	public Package getPackage(String pack){
		return this.ruleBase.getPackage( pack);
		
	}
	
	
	public synchronized void cloneMotor(Set factNotValid, DataModelAdapter dma) throws NotFoundException, IncoherenceInMotorException {
//		Date now=new Date(System.currentTimeMillis());
//		 System.err.println("\n        ************Inicio clone *******    "+now);
		Iterator s = workingMemory.iterateObjects();
		
		//((StatefulSession)workingMemory).clearAgenda();
		//((StatefulSession)workingMemory).dispose();
		/*Package[] packages=ruleBase.getPackages();
		ruleBase = RuleBaseFactory.newRuleBase();
		for(int i=0;i<packages.length;i++)
			try {
				ruleBase.addPackage(packages[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		workingMemory=ruleBase.newStatefulSession();
		((StatefulSession)workingMemory).dispose();
		setGlobal("dma", dma);
		boolean isPropertyChangeDrools;
		while(s.hasNext()){
			Object fa=s.next();
			isPropertyChangeDrools=(fa instanceof IPropertyChangeDrools);
			//Removemos los listener del antiguo motor ya que si no el recolector de basura no libera la memoria
			if(isPropertyChangeDrools)
				((IPropertyChangeDrools)fa).removePropertyChangeListeners();
			else System.err.println("WARNING: El fact "+fa+" no implementa IPropertyChangeDrools");
			if(!factNotValid.contains(fa)){
				/*if((fa instanceof Fact) && (((Fact)fa).getSesion()==0)){
					Exception ex=new Exception("INSERTADO FACT SESION=0 EN NEW MOTOR: fa:"+fa+"\n factNotValid:"+factNotValid);
					SessionController.getInstance().getDDM().getServer().logError(ex, null);
				}*/
				FactHandle fh = workingMemory.insert(fa,isPropertyChangeDrools);
				if(isPropertyChangeDrools){
					((IPropertyChangeDrools)fa).setFactHandle(fh);
				}
			}
		}
		
		workingMemory.clearActivationGroup(Constants.ADVANCEDCONFIGURATION_RULES);
		workingMemory.clearRuleFlowGroup(Constants.ADVANCEDCONFIGURATION_RULES);
		workingMemory.clearAgendaGroup(Constants.ADVANCEDCONFIGURATION_RULES);
		workingMemory.clearActivationGroup(Constants.INICIALIZERULESGROUP);
		workingMemory.clearRuleFlowGroup(Constants.INICIALIZERULESGROUP);
		workingMemory.clearAgendaGroup(Constants.INICIALIZERULESGROUP);
		//workingMemory.clearAgenda();Comentado ya que si hay una regla que se va a disparar en ddbbrules aun no se habra disparado y lo perdemos(pasa con mi_empresa en replicas)
//		now=new Date(System.currentTimeMillis());
//		 System.err.println("\n        ************Fin clone *******    "+now);
	}
	
	
	/*public Rule getRule(String ruleName){
		Package[] packages=ruleBase.getPackages();
		Rule rule=null;
		for(int i=0;i<packages.length && rule==null;i++){
			rule=ruleBase.getPackages()[i].getRule(ruleName);
		}
		return rule;
	}*/
	
	public synchronized IRuleEngine doClone(DocDataModel ik) throws EngineException, NotFoundException, IncoherenceInMotorException{
		JBossEngine jboss=new JBossEngine(ik);
		//try {
			jboss.inicializeRules(new ArrayList(Arrays.asList(ruleBase.getPackages())));
		
			jboss.setGlobal("dma", ik.getDataModelAdapter());
			
			int i=0;
			int j=0;
			Iterator s = workingMemory.iterateObjects();
			while(s.hasNext()){
				Object fa=s.next();
				if(fa instanceof IPropertyChangeDrools && !(fa instanceof QuestionRequest)){//No inserta DocDataModel, DataModelAdapter y JBossEngine. Ni QuestionRequest
					/*FactHandle fh = workingMemory.insert(fa,true);
					((IPropertyChangeDrools)fa).setFactHandle(fh);*/
					
/*
 * TODO FRAN REVISAR PROBLEMAS ISINCREMENTAL Y CUENTA CONTABLE PROPAGACION
 * if(fa instanceof Individual){
						// Si es individual necesitamos clonarlo ya que insertFact lo inserta directamente
						fa=((Individual)fa).clone(ik);
					}*/
//					if(!(fa instanceof DatValue && ((DatValue)fa).isIncremental())){	
//						FactHandle fh = jboss.workingMemory.insert(((IPropertyChangeDrools)fa).clone(ik),true);
//						((IPropertyChangeDrools)fa).setFactHandle(fh);
//						//Los facts los crea haciendole un clone, como nos interesa. FactHierarchy, FactProp... se insertan directamente, por lo que se comparten
//						//cosa que puede llegar a ser un problema si son dinamicos. De momento no es un problema.
//						//jboss.insertFact(fa);
//					}else{
//						System.err.println("INFO: No se esta clonando por ser contributivo "+fa);
//						DatValue dv=(DatValue)fa;
//						
//						if(dv.initialValuesChanged()){
//							System.err.println("WARNING: No se esta clonando por ser contributivo "+fa+" y ha cambiado su valor respecto BBDD");
//						}
//					}
					
					boolean insert=false;
					//Insertamos todos los facts y descartamos casi todos los filtros con alguna excepcion
					if( !(fa instanceof Fact && ((Fact)fa).getLEVEL().equals(Constants.LEVEL_FILTER))){
						insert=true;
					}else{
						//Tenemos que mantener los filtros genericos y los filtros de userTask ya que son necesarios para la aplicacion
						Fact filterFact=(Fact)fa;
						//Realmente no encontraremos filtros genericos porque no tienen facts si no solo una reserva de ido, pero por si esto cambiara lo dejamos implementado
						if(ik.isGenericFilter(filterFact.getIDTO())){
							insert=true;
						}else{
							//De userTask solo nos interesan los valores asignados (area funcional, userRol, rdn en reports...)
							if(ik.isSpecialized(filterFact.getIDTO(), Constants.IDTO_UTASK)){
								if(filterFact.getOP()==null){
									insert=true;
								}
							}
						}
					}
					
					IPropertyChangeDrools factClone=null;
					if(insert){
						factClone=(IPropertyChangeDrools)((IPropertyChangeDrools)fa).clone(ik);
						FactHandle fh = jboss.workingMemory.insert(factClone,true);
						factClone.setFactHandle(fh);
						//Los facts los crea haciendole un clone, como nos interesa. FactHierarchy, FactProp... se insertan directamente, por lo que se comparten
						//cosa que puede llegar a ser un problema si son dinamicos. De momento no es un problema.
						//jboss.insertFact(fa);
						i++;
					}else{
						j++;
					}
				}
			}
			
			jboss.insertFact(ik);
			jboss.insertFact(ik.getDataModelAdapter());
			jboss.insertFact(jboss);
			
			//System.err.println("***Clone JBossEngine con "+i+" facts insertados y "+j+" facts descartados");
			jboss.workingMemory.clearActivationGroup(Constants.ADVANCEDCONFIGURATION_RULES);
			jboss.workingMemory.clearRuleFlowGroup(Constants.ADVANCEDCONFIGURATION_RULES);
			jboss.workingMemory.clearAgendaGroup(Constants.ADVANCEDCONFIGURATION_RULES);
			jboss.workingMemory.clearActivationGroup(Constants.INICIALIZERULESGROUP);
			jboss.workingMemory.clearRuleFlowGroup(Constants.INICIALIZERULESGROUP);
			jboss.workingMemory.clearAgendaGroup(Constants.INICIALIZERULESGROUP);
			//jboss.workingMemory.clearAgenda();Comentado ya que si hay una regla que se va a disparar en ddbbrules aun no se habra disparado y lo perdemos(pasa con mi_empresa en replicas)
		/*} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		return jboss;
	}
	
	public void dispose(){
//		Date now=new Date(System.currentTimeMillis());
//		 System.err.println("\n        ************Inicio clone *******    "+now);
		Iterator s = workingMemory.iterateObjects();
		
		boolean isPropertyChangeDrools;
		while(s.hasNext()){
			Object fa=s.next();
			isPropertyChangeDrools=(fa instanceof IPropertyChangeDrools);
			//Removemos los listener del antiguo motor ya que si no el recolector de basura no libera la memoria
			if(isPropertyChangeDrools)
				((IPropertyChangeDrools)fa).removePropertyChangeListeners(); 
		}
		workingMemory=null;
		ruleBase=null;
		ik=null;
//		now=new Date(System.currentTimeMillis());
//		 System.err.println("\n        ************Fin clone *******    "+now);
	}
	
	
	public void removeRule(String pack,String nameRule){
		Package p=ruleBase.getPackage(pack);
		p.getRule(nameRule).setEnabled(false);
		//ruleBase.removeRule(pack,nameRule);
	}
	
	/**
	 * Consume evento del fact. Si value es null busca todos los fact con ese ido e idProp, si es distinto de null filtra. Para dataProperty solo tiene sentido que sea null
	 * @param ido
	 * @param idProp
	 * @param value
	 * @param valueCls
	 */
	public void consumirEventoCambio(int ido,int idProp,Integer value,Integer valueCls){
		LinkedList<IPropertyDef> list=null;
		if(value!=null){
			list=getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls(ido, idProp, String.valueOf(value), valueCls);
		}else{
			list=getInstanceFactsWhereIdoAndIdProp(ido, idProp);
		}
		
		for(IPropertyDef fact:list){
			if(((Fact)fact).getOP()==null){
				((Fact)fact).consumirEventoCambio();
			}
		}
		
	}
}