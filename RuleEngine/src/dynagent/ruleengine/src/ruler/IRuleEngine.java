package dynagent.ruleengine.src.ruler;

//import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.drools.rule.Package;
//import org.drools.QueryResults;
//import org.drools.compiler.DroolsParserException;
import org.jdom.JDOMException;

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
import dynagent.common.knowledge.IPropertyDef;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;

public interface IRuleEngine {
	
	/**
	 * 
	 * @param idto
	 * @param ido
	 * @param prop
	 * @param valuecls
	 * @param value
	 * @param name
	 * @param qMax
	 * @param qMin
	 * @return
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */public LinkedList<IPropertyDef> getAllInstanceFacts(Object idto, Object ido, Object prop, Object valuecls, Object value, Object name, Object qMax, Object qMin,Object op) throws NotFoundException, IncoherenceInMotorException;
	
	
	 
	 public LinkedList<IPropertyDef> getAllInstanceValuesFacts(int ido, int idProp);
	 	 
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereIdo(int ido);
	 
	 public LinkedList<IPropertyDef> getAllInstanceFactsNoDeletedWithIdo(int idto);
	 
	 
	 
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdProp(int ido,int idProp);
	 	/**
		 * Obtiene los facts que han sido borrados en una sesion y cumplen los parametros que se la pasan
		 * @param idto
		 * @param ido
		 * @param prop
		 * @param valuecls
		 * @param value
		 * @param name
		 * @param qMax
		 * @param qMin
		 * @return
		 */
	// public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Object idto, Object ido, Object prop, Object valuecls,  Object name);
	 
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhere(Integer idto,Integer ido,Integer prop,String value, Integer valuecls, Double qmin,Double qmax,String op);
	 
		
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereIdtoAndIdPropAndIdoNull(int idto, int idProp);
	 
	/**
	 * getAllHierarchyFacts: Devuelve una lista  con los facts de herencia
	 * que satisfacen la condicion que se le pasa como parametro
	 * @param idto
	 * @param idtoSup
	 * @return
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */public LinkedList<FactHierarchy> getAllHierarchyFacts(Object idto, Object idtoSup) throws NotFoundException, IncoherenceInMotorException;
	
	/***************************************************************************
	 * getAllPropertyFacts:  Devuelve una lista con los facts (property..) 
	 * que satisfacen la condicion que se le pasa como parametro
	 * 
	 * @return Iterator
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */
	public LinkedList<FactProp> getAllPropertyFacts(Object prop, Object name) throws NotFoundException, IncoherenceInMotorException;
	
	
	public LinkedList<FactProp> getPropertyFactsWhereIdProp(int idProp);
	
	
	
	 
	 
	 
	public LinkedList<FactAccess> getAccessFactsOfProperty(int idto, Integer ido, int idProp,Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
	
	public LinkedList<FactAccess> getAccessFactsIdoRequired(int idto, int ido, int idProp,Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
	
	public LinkedList<FactAccess> getAccessFactsOfPropertyAndIdtoRequired(int idto, Integer ido, int idProp,Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
	
	public LinkedList<FactAccess> getAccessFactsOverObject(Integer idto,Integer ido,Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException;
	
	public LinkedList<FactAccess> getAllAccessFacts() throws NotFoundException, IncoherenceInMotorException;
	//public LinkedList<FactAccess> getAllAccessFacts(Object utask, Object idto, Object accesstype, Object ido, Object prop, Object value, Object valuecls, Object userrol, Object user) throws NotFoundException, IncoherenceInMotorException;
	  
	 
	 
	
	 /**
	  * 
	  * @param idto
	  * @param ido
	  * @param prop
	  * @param valuecls
	  * @param value
	  * @param qMax
	  * @param qMin
	  * @param op
	  * @return
	  */
	 //public int deleteFactCond(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin, Object op);
	
	/**
	 * 
	 * @param idto
	 * @param ido
	 * @param prop
	 * @param valuecls
	 * @param value
	 * @param qMax
	 * @param qMin
	 * @return
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */
	public int deleteFactCondRETRACT(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin,Object op) throws NotFoundException, IncoherenceInMotorException;
	
	
	 /**
	  * 
	  * @param fact
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	  */public void insertFact(Object fact) throws NotFoundException, IncoherenceInMotorException;
	
	/**
	 * 
	 * @param fact
	 * @param slot
	 * @param value
	 * @return
	 */
	  //public int modify(IPropertyDef fact, String slot, String value);
	 
	 public void printMotor();
	 
	 public void inicializeRules(/*String fileRules*/ArrayList<Package> rulesPackage) throws EngineException;
	 
	 public int setFact(dynagent.common.knowledge.IPropertyDef facttomodify,dynagent.common.knowledge.IPropertyDef newfact, boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public int setFact(dynagent.common.knowledge.IPropertyDef fact, String slot, Object value) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public int deleteFactCond(Object idto, Object ido, Object prop, Object valuecls, Object value, Object qMax, Object qMin, Object op) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public void retractFact(IPropertyDef  f);
	 public void retractIndividual(Individual  f);
	 public void retractFactAccess(FactAccess  f);
	 public void retractLock(Lock  f);
	 public void retractFactDataRules(DataRules  f);
	 
	 public void retract(Object f) ;
	 
	 
	 public void addNewFactToRuler(dynagent.common.knowledge.FactInstance newfact,Integer propertymaxcardinality,boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public int deleteFact(dynagent.common.knowledge.IPropertyDef fact,boolean setSystemValue) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public void  run(String group) throws OperationNotPermitedException, InstanceLockedException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException;
	 
	 public LinkedList<IPropertyDef> getAllInstanceFactsDELETED(Integer ido, Integer prop);
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhere(int idto);
	 
	 public Individual getIndividualFact(int id);
	 
	 public Lock getLockFact(int id);
	 
	 public int getMotorSize();
	 
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdPropAndValueAndValueCls(int ido, int prop,String value,int valueCls);
	 public LinkedList<IPropertyDef> getInstanceFactsWhereIdoAndIdPropAndValueNullAndValueCls(int ido, int prop, int valueCls);
	 

	 public boolean setFactWithContributionValue(IPropertyDef facttomodify, HashMap<String,Number> mapIdoInitialValue, Number value, Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 public void setFactWithDestination(IPropertyDef facttomodify,String destination);
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereValueAndValueCls(String value,int valueCls);
	 
	 public LinkedList<IPropertyDef> getInstanceFactsWhereValue(String value);

	 public void setGlobal(String name,Object value);
	 
	 public LinkedList<IPropertyDef>  getInstanceFactsWhereName(String name);
	 
	 public IRuleEngine doClone(DocDataModel ddm) throws EngineException, NotFoundException, IncoherenceInMotorException;
	 
	 public void dispose();
	 
	 public Fact getFact(IPropertyDef condicion);
	 
	 public void insertFact(Object fact,boolean virtual) throws NotFoundException, IncoherenceInMotorException;

	 public boolean setFactWithIncrementalValue(IPropertyDef fact, double incrementalValue, Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;

	 public void consumirEventoCambio(int ido, int idProp, Integer value, Integer valueCls);
	 
	 public boolean setFactWithContributionValue(IPropertyDef facttomodify, String key, double incrementalValue,Integer nDigRedondeo) throws IncompatibleValueException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	 
	 public Fact getFact(int ido,int prop);
}
