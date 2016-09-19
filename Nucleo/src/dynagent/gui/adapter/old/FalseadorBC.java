package dynagent.gui.adapter.old;

/*import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;


import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.Category;

import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.DoubleValue;
import dynagent.ruleengine.meta.api.IChangeServerListener;
import dynagent.ruleengine.meta.api.IHistoryDDBBListener;

import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.IntValue;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.UnitValue;
import dynagent.ruleengine.meta.api.Value;

import dynagent.ruleengine.src.data.dao.OrderProperty;
import dynagent.ruleengine.src.ruler.FactProp;
import dynagent.ruleengine.src.ruler.IPropertyDef;
<<<<<<< .mine
//import dynagent.ruleengine.src.sessions.DefaultSession;
//import dynagent.ruleengine.src.sessions.IChangePropertyListener;
//
////mport dynagent.ruleengine.src.ruler.Ruler;
//import dynagent.ruleengine.src.sessions.DefaultSession;
//import dynagent.ruleengine.src.sessions.IChangePropertyListener;
//import dynagent.ruleengine.src.sessions.Session;
//import dynagent.ruleengine.src.sessions.Sessionable;
//import dynagent.ruleengine.src.xml.QueryXML;
//import dynagent.server.communication.docServer;
//import dynagent.server.exceptions.ApplicationException;
//import dynagent.server.knowledge.UserAccess;
//import dynagent.server.knowledge.access;
//import dynagent.server.knowledge.instance.instance;
//import dynagent.server.process.IAsigned;
//
//public class FalseadorBC implements IKnowledgeBaseInfo,Session{
//	private IKnowledgeBaseInfo kb;
//	private Session sessionFal;
//	public FalseadorBC(IKnowledgeBaseInfo kb){
//		this.kb=kb;
//		this.sessionFal=((DocDataModel)kb).getRootSession();
//	}
//	
//	public Integer createPrototype(int idto, int level, Integer userRol, String user, Integer usertask, Session sess) throws NotFoundException {
//		return kb.createPrototype(idto, level, userRol, user, usertask, sess);
//	}
//
//	public access getAccessOverObject(Integer id, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
//		access ac=kb.getAccessOverObject(id, userRol, user, usertask);
//		System.err.println("clase por la que pregunta:" + kb.getClassOf(id));
//		if((kb.getClassOf(id)!=null && kb.getClassOf(id).equals(79)) || id.equals(79)){
//			System.err.println("dynagent.gui.adapter.FalseadorBC.getAccessOverObject");
//			ac= new access(ac.getViewAccess(),ac.getNewAccess(),ac.getSetAccess(),ac.getRelAccess(),ac.getUnrelAccess(),ac.getDelAccess(),ac.getConcreteAccess(),true,ac.getAbstractAccess(),ac.getCommentAccess());
//			
//		}
//		return ac;
//	}
//
//	
//	public void addHistoryDDBBListener(IHistoryDDBBListener historyDDBBListener) {
//		// TODO Auto-generated method stub
//		kb.addHistoryDDBBListener(historyDDBBListener);
//	}
//
//	
//	public void deleteObject(int id, Session sessionPadre)
//			throws NotFoundException {
//		// TODO Auto-generated method stub
//		kb.deleteObject(id, sessionPadre);
//	}
//
//	
//	public Iterator<Property> getAllPropertyIterator(int ido, Integer userRol,
//			String user, Integer usertask, Session sessionPadre)
//			throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getAllPropertyIterator(ido, userRol, user, usertask, sessionPadre);
//	}
//
//	
//	public Integer getAtributteGroup(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.getAtributteGroup(idProp);
//	}
//
//	 
//	public Category getCategory(int idProp) throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getCategory(idProp);
//	}
//
//	 
//	public Iterator<Integer> getClassIterator() throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getClassIterator();
//	}
//
//	 
//	public String getClassName(int id) throws NotFoundException,
//			IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.getClassName(id);
//	}
//
//	 
//	public Integer getClassOf(int ido) {
//		// TODO Auto-generated method stub
//		return kb.getClassOf(ido);
//	}
//
//	 
//	public Integer getDatatype(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.getDatatype(idProp);
//	}
//
//	 
//	public FactProp getFactProp(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.getFactProp(idProp);
//	}
//
//	 
//	public Iterator<Integer> getIndividualsOfLevel(int idto, int level) {
//		// TODO Auto-generated method stub
//		return kb.getIndividualsOfLevel(idto, level);
//	}
//
//	 
//	public ArrayList<OrderProperty> getOrderProperties() {
//		// TODO Auto-generated method stub
//		return kb.getOrderProperties();
//	}
//
//	 
//	public Property getProperty(int id, int idProp, Integer userRol,
//			String user, Integer usertask, Session s) throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getProperty(id, idProp, userRol, user, usertask, s);
//	}
//
//	 
//	public Integer getPropertyInverse(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.getPropertyInverse(idProp);
//	}
//
//	 
//	public String getPropertyName(int idProp) throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getPropertyName(idProp);
//	}
//
//	 
//	public QueryXML getQueryXML() {
//		// TODO Auto-generated method stub
//		return kb.getQueryXML();
//	}
//
//	 
//	public String getRdn(int ido) {
//		// TODO Auto-generated method stub
//		return kb.getRdn(ido);
//	}
//
//	 
//	public Iterator<Integer> getSpecialized(int id, Integer userRol,
//			String user, Integer usertask, Session session)
//			throws NotFoundException, IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.getSpecialized(id, userRol, user, usertask, session);
//	}
//
//	 
//	public ArrayList<Integer> getSpecializedFilters(int ido, Integer userRol,
//			String user, Integer usertask, Session session)
//			throws NotFoundException, IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.getSpecializedFilters(ido, userRol, user, usertask, session);
//	}
//
//	 
//	public Iterator<Integer> getSuperior(int idto) throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.getSuperior(idto);
//	}
//
//	 
//	public instance getTreeObject(int id, Integer userRol, String user,
//			Integer userTask, Session sess) throws NotFoundException,
//			IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.getTreeObject(id, userRol, user, userTask, sess);
//	}
//
//	 
//	public HashMap<Integer, ArrayList<UserAccess>> getUsertaskOperationOver(
//			int id, String user) throws NotFoundException,
//			IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.getUsertaskOperationOver(id, user);
//	}
//
//	 
//	public boolean isDataProperty(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.isDataProperty(idProp);
//	}
//
//	 
//	public boolean isIDClass(int id) {
//		// TODO Auto-generated method stub
//		return kb.isIDClass(id);
//	}
//
//	 
//	public boolean isObjectProperty(int idProp) {
//		// TODO Auto-generated method stub
//		return kb.isObjectProperty(idProp);
//	}
//
//	 
//	public boolean isSpecialized(int idto, int posSuperior) {
//		// TODO Auto-generated method stub
//		return kb.isSpecialized(idto, posSuperior);
//	}
//
//	 
//	public boolean isUnit(int cls) throws NotFoundException {
//		// TODO Auto-generated method stub
//		return kb.isUnit(cls);
//	}
//
//	 
//	public void loadMetaData() throws NotFoundException,
//			IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		kb.loadMetaData();
//	}
//
//	 
//	
//
//
//
//
//	 
//	public void setServer(docServer server) {
//		// TODO Auto-generated method stub
//		kb.setServer(server);
//	}
//
//	 
//	public void setValue(int ido, int idProp, Value oldValue, Value newValue,
//			Integer userRol, String user, Integer usertask, Session s)
//			throws CardinalityExceedException, OperationNotPermitedException,
//			IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException {
//		// TODO Auto-generated method stub
//		kb.setValue(ido, idProp, oldValue, newValue, userRol, user, usertask, s);
//	}
//
//	 
//	public int specializeIn(int id, int idtoSpecialized) {
//		// TODO Auto-generated method stub
//		return kb.specializeIn(id, idtoSpecialized);
//	}
//
//<<<<<<< .mine
//	public void setAsigned(IAsigned arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public int loadNewData(ArrayList<IPropertyDef> arg0, Integer arg1, String arg2, Integer arg3, Session arg4) throws NotFoundException, IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.loadNewData(arg0, arg1, arg2, arg3, arg4);
//	}
//
//	public void addIchangeProperty(IChangePropertyListener a) {
//		sessionFal.addIchangeProperty(a);
//		
//	}
//
//	public void addSessionable(Sessionable s) {
//		this.sessionFal.addSessionable(s);
//		
//	}
//
//	public void changeValue(int ido, int idto, int idProp, int valueCls, Value value, int operation) {
//		this.sessionFal.changeValue(ido, idto, idProp, valueCls, value, operation);
//		
//	}
//
//	public void childSessionClosed(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
//		try {
//			if (this.getSesionables().size() != 0)
//				this.commit();
//		} catch (ApplicationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
//		
//		return sessionFal.commit();
//	}
//
//	public int getID() {
//		return sessionFal.getID();
//	}
//
//	public int getIDMadre() {
//		return sessionFal.getIDMadre();
//	}
//
//	public ArrayList<Sessionable> getSesionables() {
//		return sessionFal.getSesionables(); 
//	}
//
//	public Integer getUtask() {
//		return sessionFal.getUtask();
//	}
//
//	public void rollBack() throws ApplicationException, NotFoundException {
//		this.sessionFal.rollBack();
//		
//	}
//
//	public void setID(int id) {
//	this.sessionFal.setID(id);
//		
//	}
//
//	public void setIDMadre(int madre) {
//		this.sessionFal.setIDMadre(madre);		
//	}
//
//	public void setUtask(Integer utask) {
//		// TODO Auto-generated method stub
//		sessionFal.setUtask(utask);
//	}
//
//	public void setRuler(Ruler ruler) {
//		// TODO Auto-generated method stub
//		kb.setRuler(ruler);
//	}
//	
//
//
//
//
//
//=======
//	public void setAsigned(IAsigned arg0) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public int loadNewData(ArrayList<IPropertyDef> arg0, Integer arg1, String arg2, Integer arg3, Session arg4) throws NotFoundException, IncoherenceInMotorException {
//		// TODO Auto-generated method stub
//		return kb.loadNewData(arg0, arg1, arg2, arg3, arg4);
//	}
//
//	public void addIchangeProperty(IChangePropertyListener a) {
//		sessionFal.addIchangeProperty(a);
//		
//	}
//
//	public void addSessionable(Sessionable s) {
//		this.sessionFal.addSessionable(s);
//		
//	}
//
//	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int operation) {
//		this.sessionFal.changeValue(ido, idto, idProp, valueCls, value, valueOld, operation);
//		
//	}
//
//	public void childSessionClosed(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
//		try {
//			if (this.getSesionables().size() != 0)
//				this.commit();
//		} catch (ApplicationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
//		
//		return sessionFal.commit();
//	}
//
//	public int getID() {
//		return sessionFal.getID();
//	}
//
//	public int getIDMadre() {
//		return sessionFal.getIDMadre();
//	}
//
//	public ArrayList<Sessionable> getSesionables() {
//		return sessionFal.getSesionables(); 
//	}
//
//	public Integer getUtask() {
//		return sessionFal.getUtask();
//	}
//
//	public void rollBack() throws ApplicationException, NotFoundException {
//		this.sessionFal.rollBack();
//		
//	}
//
//	public void setID(int id) {
//	this.sessionFal.setID(id);
//		
//	}
//
//	public void setIDMadre(int madre) {
//		this.sessionFal.setIDMadre(madre);		
//	}
//
//	public void setUtask(Integer utask) {
//		// TODO Auto-generated method stub
//		sessionFal.setUtask(utask);
//	}
//
//
//	public void addChangeServerListener(IChangeServerListener changeServerListener) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	public boolean checkCoherenceObject(int ido, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	public Integer getLevelOf(int ido) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public int getCategory() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	public void setCategory(int category) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//
//
//
//
//
//>>>>>>> .r2284
//}
=======
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.IChangePropertyListener;
import dynagent.ruleengine.src.sessions.Session;
import dynagent.ruleengine.src.sessions.Sessionable;
import dynagent.ruleengine.src.xml.QueryXML;
import dynagent.server.communication.docServer;
import dynagent.server.exceptions.ApplicationException;
import dynagent.server.knowledge.UserAccess;
import dynagent.server.knowledge.access;
import dynagent.server.knowledge.instance.instance;
import dynagent.server.process.IAsigned;*/

/*public class FalseadorBC{/* implements IKnowledgeBaseInfo,Session{
	private IKnowledgeBaseInfo kb;
	private Session sessionFal;
	public FalseadorBC(IKnowledgeBaseInfo kb){
		this.kb=kb;
		this.sessionFal=((DocDataModel)kb).getRootSession();
	}
	
	public Integer createPrototype(int idto, int level, Integer userRol, String user, Integer usertask, Session sess) throws NotFoundException {
		return kb.createPrototype(idto, level, userRol, user, usertask, sess);
	}

	public access getAccessOverObject(Integer id, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		access ac=kb.getAccessOverObject(id, userRol, user, usertask);
		System.err.println("clase por la que pregunta:" + kb.getClassOf(id));
		if((kb.getClassOf(id)!=null && kb.getClassOf(id).equals(79)) || id.equals(79)){
			System.err.println("dynagent.gui.adapter.FalseadorBC.getAccessOverObject");
			ac= new access(ac.getViewAccess(),ac.getNewAccess(),ac.getSetAccess(),ac.getRelAccess(),ac.getUnrelAccess(),ac.getDelAccess(),ac.getConcreteAccess(),true,ac.getAbstractAccess(),ac.getCommentAccess());
			
		}
		return ac;
	}

	
	public void addHistoryDDBBListener(IHistoryDDBBListener historyDDBBListener) {
		// TODO Auto-generated method stub
		kb.addHistoryDDBBListener(historyDDBBListener);
	}

	
	public void deleteObject(int id, Session sessionPadre)
			throws NotFoundException {
		// TODO Auto-generated method stub
		kb.deleteObject(id, sessionPadre);
	}

	
	public Iterator<Property> getAllPropertyIterator(int ido, Integer userRol,
			String user, Integer usertask, Session sessionPadre)
			throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getAllPropertyIterator(ido, userRol, user, usertask, sessionPadre);
	}

	
	public Integer getAtributteGroup(int idProp) {
		// TODO Auto-generated method stub
		return kb.getAtributteGroup(idProp);
	}

	 
	public Category getCategory(int idProp) throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getCategory(idProp);
	}

	 
	public Iterator<Integer> getClassIterator() throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getClassIterator();
	}

	 
	public String getClassName(int id) throws NotFoundException,
			IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.getClassName(id);
	}

	 
	public Integer getClassOf(int ido) {
		// TODO Auto-generated method stub
		return kb.getClassOf(ido);
	}

	 
	public Integer getDatatype(int idProp) {
		// TODO Auto-generated method stub
		return kb.getDatatype(idProp);
	}

	 
	public FactProp getFactProp(int idProp) {
		// TODO Auto-generated method stub
		return kb.getFactProp(idProp);
	}

	 
	public Iterator<Integer> getIndividualsOfLevel(int idto, int level) {
		// TODO Auto-generated method stub
		return kb.getIndividualsOfLevel(idto, level);
	}

	 
	public ArrayList<OrderProperty> getOrderProperties() {
		// TODO Auto-generated method stub
		return kb.getOrderProperties();
	}

	 
	public Property getProperty(int id, int idProp, Integer userRol,
			String user, Integer usertask, Session s) throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getProperty(id, idProp, userRol, user, usertask, s);
	}

	 
	public Integer getPropertyInverse(int idProp) {
		// TODO Auto-generated method stub
		return kb.getPropertyInverse(idProp);
	}

	 
	public String getPropertyName(int idProp) throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getPropertyName(idProp);
	}

	 
	public QueryXML getQueryXML() {
		// TODO Auto-generated method stub
		return kb.getQueryXML();
	}

	 
	public String getRdn(int ido) {
		// TODO Auto-generated method stub
		return kb.getRdn(ido);
	}

	 
	public Iterator<Integer> getSpecialized(int id, Integer userRol,
			String user, Integer usertask, Session session)
			throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.getSpecialized(id, userRol, user, usertask, session);
	}

	 
	public ArrayList<Integer> getSpecializedFilters(int ido, Integer userRol,
			String user, Integer usertask, Session session)
			throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.getSpecializedFilters(ido, userRol, user, usertask, session);
	}

	 
	public Iterator<Integer> getSuperior(int idto) throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.getSuperior(idto);
	}

	 
	public instance getTreeObject(int id, Integer userRol, String user,
			Integer userTask, Session sess) throws NotFoundException,
			IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.getTreeObject(id, userRol, user, userTask, sess);
	}

	 
	public HashMap<Integer, ArrayList<UserAccess>> getUsertaskOperationOver(
			int id, String user) throws NotFoundException,
			IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.getUsertaskOperationOver(id, user);
	}

	 
	public boolean isDataProperty(int idProp) {
		// TODO Auto-generated method stub
		return kb.isDataProperty(idProp);
	}

	 
	public boolean isIDClass(int id) {
		// TODO Auto-generated method stub
		return kb.isIDClass(id);
	}

	 
	public boolean isObjectProperty(int idProp) {
		// TODO Auto-generated method stub
		return kb.isObjectProperty(idProp);
	}

	 
	public boolean isSpecialized(int idto, int posSuperior) {
		// TODO Auto-generated method stub
		return kb.isSpecialized(idto, posSuperior);
	}

	 
	public boolean isUnit(int cls) throws NotFoundException {
		// TODO Auto-generated method stub
		return kb.isUnit(cls);
	}

	 
	public void loadMetaData() throws NotFoundException,
			IncoherenceInMotorException {
		// TODO Auto-generated method stub
		kb.loadMetaData();
	}

	 
	




	 
	public void setServer(docServer server) {
		// TODO Auto-generated method stub
		kb.setServer(server);
	}

	 
	public void setValue(int ido, int idProp, Value oldValue, Value newValue,
			Integer userRol, String user, Integer usertask, Session s)
			throws CardinalityExceedException, OperationNotPermitedException,
			IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException {
		// TODO Auto-generated method stub
		kb.setValue(ido, idProp, oldValue, newValue, userRol, user, usertask, s);
	}

	 
	public int specializeIn(int id, int idtoSpecialized) {
		// TODO Auto-generated method stub
		return kb.specializeIn(id, idtoSpecialized);
	}

	public void setAsigned(IAsigned arg0) {
		// TODO Auto-generated method stub
		
	}

	public int loadNewData(ArrayList<IPropertyDef> arg0, Integer arg1, String arg2, Integer arg3, Session arg4) throws NotFoundException, IncoherenceInMotorException {
		// TODO Auto-generated method stub
		return kb.loadNewData(arg0, arg1, arg2, arg3, arg4);
	}

	public void addIchangeProperty(IChangePropertyListener a) {
		sessionFal.addIchangeProperty(a);
		
	}

	public void addSessionable(Sessionable s) {
		this.sessionFal.addSessionable(s);
		
	}

	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int operation) {
		this.sessionFal.changeValue(ido, idto, idProp, valueCls, value, valueOld, operation);
		
	}

	public void childSessionClosed(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
		try {
			if (this.getSesionables().size() != 0)
				this.commit();
		} catch (ApplicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
		
		return sessionFal.commit();
	}

	public int getID() {
		return sessionFal.getID();
	}

	public int getIDMadre() {
		return sessionFal.getIDMadre();
	}

	public ArrayList<Sessionable> getSesionables() {
		return sessionFal.getSesionables(); 
	}

	public Integer getUtask() {
		return sessionFal.getUtask();
	}

	public void rollBack() throws ApplicationException, NotFoundException {
		this.sessionFal.rollBack();
		
	}

	public void setID(int id) {
	this.sessionFal.setID(id);
		
	}

	public void setIDMadre(int madre) {
		this.sessionFal.setIDMadre(madre);		
	}

	public void setUtask(Integer utask) {
		// TODO Auto-generated method stub
		sessionFal.setUtask(utask);
	}


	public void addChangeServerListener(IChangeServerListener changeServerListener) {
		// TODO Auto-generated method stub
		
	}

	public boolean checkCoherenceObject(int ido, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException {
		// TODO Auto-generated method stub
		return false;
	}

	public Integer getLevelOf(int ido) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getCategory() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setCategory(int category) {
		// TODO Auto-generated method stub
		
	}*/
	





