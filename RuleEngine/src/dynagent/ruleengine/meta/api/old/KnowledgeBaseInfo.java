/***
 * KnowledgeBaseInfo.java
 * @author: Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api.old;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import dynagent.application.session;
import dynagent.knowledge.access;
import dynagent.knowledge.instance.instance;
import dynagent.ruleengine.meta.KnowledgeBase;
import dynagent.ruleengine.meta.RoleClass;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.old.TripleInstance;
import dynagent.ruleengine.meta.old.RelationClass;
import dynagent.ruleengine.meta.old.StaticClass;
import dynagent.ruleengine.src.data.dao.DAOManager;
import dynagent.ruleengine.src.data.dao.IDAO;
import dynagent.ruleengine.src.data.dao.InstanceDAO;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.xml.QueryXML;

public class KnowledgeBaseInfo implements IKnowledgeBaseInfoExtended {

	private KnowledgeBase kb;
	
	public KnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}
	
	public String getNameByID(int ido) {
		IDAO idao = DAOManager.getInstance().getDAO("instances");
		// idao.open();
		InstanceDAO insdao = (InstanceDAO)idao.getDAO();
		LinkedList<Object> obj = insdao.getByID((new Integer(ido)).toString());
		// idao.close();
		return (String)obj.getFirst();
	}
	
	public int getIDByName(String n){ // Name es UNIQUEKEY en la BBDD
		IDAO idao = DAOManager.getInstance().getDAO("instances");
		idao.open();
		InstanceDAO insdao = (InstanceDAO)idao.getDAO();
		n = n.substring(n.indexOf("#")+ 1, n.length());
		LinkedList<Object> obj = insdao.getByName(n);
		return (new Integer((obj.getFirst()).toString())).intValue();
	}
	
	public RelationClass getRelationClass(int idopeer) {
		return ((RelationClass)kb.getProcess(this.getNameByID(idopeer)));
	}

	public LinkedList<RelationClass> getRelationClasses() {
		LinkedList<RelationClass> bplist = new LinkedList<RelationClass>();
		Iterator it = kb.getKb().iterator();
		while(it.hasNext()){
			dynagent.ruleengine.meta.old.Class c = (dynagent.ruleengine.meta.old.Class)it.next();
			if(c.getType().equals("PROCESS"))
				bplist.add((RelationClass)c);
		}
		return bplist;
	}

	public LinkedList<dynagent.ruleengine.meta.old.Class> getChilds(int ido) {
		LinkedList<dynagent.ruleengine.meta.old.Class> childs = new LinkedList<dynagent.ruleengine.meta.old.Class>();
		Iterator it = kb.getKb().iterator();
		while(it.hasNext()){
			dynagent.ruleengine.meta.old.Class c = (dynagent.ruleengine.meta.old.Class)it.next();
			if(c.getParent().equals(this.getNameByID(ido)))
				childs.add((dynagent.ruleengine.meta.old.Class)c);
		}
		return childs;
	}

	public dynagent.ruleengine.meta.old.Class getClass(int ido) {
		return ((dynagent.ruleengine.meta.old.Class)kb.getClass(this.getNameByID(ido)));
	}

	public LinkedList<dynagent.ruleengine.meta.old.Class> getClasses() {
		LinkedList<dynagent.ruleengine.meta.old.Class> classes = new LinkedList<dynagent.ruleengine.meta.old.Class>();
		Iterator it = kb.getKb().iterator();
		while(it.hasNext()){
			dynagent.ruleengine.meta.old.Class c = (dynagent.ruleengine.meta.old.Class)it.next();
			classes.add(c);
		}
		return classes;
 	}

	
	public StaticClass getStaticClass(int ido) {
		return ((dynagent.ruleengine.meta.old.StaticClass)kb.getClass(this.getNameByID(ido)));
	}

	public LinkedList<StaticClass> getStaticClasses() {
		LinkedList<StaticClass> owllist = new LinkedList<StaticClass>();
		Iterator it = kb.getKb().iterator();
		while(it.hasNext()){
			dynagent.ruleengine.meta.old.Class c = (dynagent.ruleengine.meta.old.Class)it.next();
			if(c.getType().equals("OWL"))
				owllist.add((StaticClass)c);
		}
		return owllist;
	}

	public LinkedList<dynagent.ruleengine.meta.old.Class> getParents(int ido) {
		dynagent.ruleengine.meta.old.Class c = this.getClass(ido);
		LinkedList<dynagent.ruleengine.meta.old.Class> parents = new LinkedList<dynagent.ruleengine.meta.old.Class>();
		Iterator it = c.getParent().iterator();
		while(it.hasNext()){
			String cname = (String)it.next();
			parents.add(this.getClass(this.getIDByName(cname)));
		}
		return parents;
	}

	public LinkedList<StaticClass> getPlayersOf(int rola) {
		LinkedList<StaticClass> players = new LinkedList<StaticClass>();
		Iterator it = this.getRoleClass(rola).getPlayers().iterator();
		while(it.hasNext()){
			StaticClass o = (StaticClass)it.next();
			players.add(o);
		}
		return players;
	}

	public LinkedList<RelationClass> getRelationOf(int rola) {
		LinkedList<RelationClass> bp = new LinkedList<RelationClass>();
		Iterator it = this.getRelationClasses().iterator();
		while(it.hasNext()){
			RelationClass bc = (RelationClass)it.next();
			if(bc.getPlayers().contains(this.getNameByID(rola)))
				bp.add(bc);
		}
		return bp;
	}

	public LinkedList<Property> getProperties(int ido) {
		return null;
	}

	public Property getProperty(int ido, int idprop) {
		return null;
	}

	public Object getPropertyValue(int ido, int idprop) {
		return null;
	}

	public RoleClass getRoleClass(int rola) {
		return ((RoleClass)kb.getRole(this.getNameByID(rola)));
	}

	public LinkedList<RoleClass> getRoleClasses() {
		LinkedList<RoleClass> rollist = new LinkedList<RoleClass>();
		Iterator it = kb.getKb().iterator();
		while(it.hasNext()){
			RoleClass c = (RoleClass)it.next();
			if(c.getType().equals("ROLE"))
				rollist.add((RoleClass)c);
		}
		return rollist;
	}

	public LinkedList<RoleClass> getRolesOf(int ido) {
		LinkedList<RoleClass> rc = new LinkedList<RoleClass>();
		Iterator it = this.getRoleClasses().iterator();
		while(it.hasNext()){
			RoleClass r = (RoleClass)it.next();
			if(r.getPlayers().contains(this.getNameByID(ido)))
				rc.add(r);
		}
		return rc;
	}

	public LinkedList<RoleClass> getRolesOfClassInRelation(int ido, int idopeer) {
		LinkedList<RoleClass> rc = this.getRolesOf(ido);
		LinkedList<RoleClass> rcp = new LinkedList<RoleClass>();
		Iterator it = rc.iterator();
		while(it.hasNext()){
			RoleClass r = (RoleClass)it.next();
			if(this.getRelationClass(idopeer).getPlayers().contains(r))
				rcp.add(r);
		}
		return rcp;
	}

	public LinkedList<RoleClass> getRolesOfRelation(int idopeer) {
		return this.getRolesOf(idopeer);
	}

	public void setKnowledgeBase(KnowledgeBase kb) {
		this.setKb(kb);		
	}

	public Iterator getAllFactPropertiesIterator(int id_O, int idoRel) {
		return null;
	}

	public Iterator getFactsPropertyIterator(Integer id_O, Integer id_prop) {
		return null;
	}

	public Iterator getFactPropertyRolIterator(int idoRel, int id_O, int idProp, int idRol) {
		return null;
	}
	
	public Iterator<Integer> getClassIterator(){
		return null;
	}
	
	
	public Iterator<Integer> getSuperior(Integer ido){
		return null;
	}
	
	public LinkedList<Integer> getSpecialized(Integer ido){
		return null;
	}

	public boolean isSpecialized(int ido, int idop){
		return false;
	}

	public LinkedList getAccess(String user, int userrol, int prop, int valueCls, int rolB, int clsRel) {
		return null;
	}
	

	public Iterator<Property> getAllPropertyAccessIterator(Integer id_O, Integer userRol, String user, Integer usertask) {
		return null;
	}

	public Iterator<Property> getAllPropertyIterator(Integer id_O) {
		return null;
	}

	public Iterator<Property> getPropertyAccessIterator(Integer id_O, Integer idProp, Integer userRol, String user, Integer usertask) {
		return null;
	}

	

	public int createPrototype(Integer idto,Integer userRol, String user, Integer userTask, session ses) {
		return -1;
	}

	public LinkedList<Integer> getObjectPermision(int id_O, int userRol, String user, int usertask) {
		return null;
	}

	public String getPropertyName(Integer idProp) {
		return null;
	}

	public int getRolPeer(int idRol, int idRel) {
		return -1;
	}

	public void setValueList(Integer id, Property pr, LinkedList valueList, session ses){
	}

	public void specializeIn(int idPrototype, int idto, int idwhere, int clswhere, LinkedList<TripleInstance> l) {
		
	}

	public LinkedList<Fact> buildFactsPlayRol(int idto, int ido, int Rol, int clsRel, int idoRel) {
		return null;
	}

	public int createPrototype(int idto, int level, LinkedList<Integer> path, session ses) {
		return 0;
	}

	public int newIdo(int level) {
		return 0;
	}

	public int specializeIn(int id, int idtoSpecialized) {
		return 0;
	}

	public Integer createPrototype(Integer idto, Integer level, LinkedList<Integer> path, session ses) {
		return null;
	}

	public LinkedList<Fact> getAllFactPrototype() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getClassName(Integer idto) {
		// TODO Auto-generated method stub
		return null;
	}

	public QueryXML getQueryXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public void createAllUtaskPrototypes(session ses) {
		// TODO Auto-generated method stub
		
	}

	public Iterator<Property> getPropertyIterator(Integer id, Integer idProp) {
		// TODO Auto-generated method stub
		return null;
	}

	public instance getTreeObject(Integer idto, Integer userRol, String user, Integer userTask) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getLevel(Integer id) {
		// TODO Auto-generated method stub
		return null;
	}


	public void deletePrototype(Integer ido, session ses) {

	}

	public LinkedList getRolPeer(int rol) {
		return null;
	}

	public void changeTypeObjectTo(Integer ido,Integer newIdto, session ses){
		
	}
	
	public boolean isStructural(Integer idProp){ return false;}

	public HashSet<Integer>  getClsRelationsInWhichPlay(Integer idto){
		return null;
	}

	public void deleteObject(Integer id, session ses) {
		
	}

	public LinkedList<Fact> getAllInstances(String cond) {
		return null;
	}
	
	public HashSet<Integer> getIdoRelationsInWhichPlay(Integer ido){
		return null;
	}
	
	public Integer getClassOfObject(Integer ido){
		return null;
	}

	public boolean isDataProperty(Integer idProp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isObjectProperty(Integer idProp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isDataValueCompatibleWithRange(DataValue datavalue, DataProperty prModel) {
		return false;
	}

	public boolean isObjectValueCompatibleWithRange(ObjectValue value, ObjectProperty prModel) {
		return false;
	}

	
	public boolean hasDependentValue(Fact fact) {
		// TODO Auto-generated method stub
		return false;
	}

	public Integer createPrototype(Integer idto, Integer level, session ses) {
		// TODO Auto-generated method stub
		return null;
	}

	public access getAccessOverObject(Integer ido, String user) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer createPrototype(Integer idto, Integer level, LinkedList<Integer> path) {
		// TODO Auto-generated method stub
		return null;
	}

	public Iterator<Integer> getIndividualsIterator(int idto, int level) {
		// TODO Auto-generated method stub
		return null;
	}

	public LinkedList<Fact> getAllInstanceFacts(String cond) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getIdClass(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
