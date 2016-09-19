/***
 * IKnowledgeBaseInfoExtended.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api.old;

import java.util.LinkedList;

import dynagent.ruleengine.meta.old.RelationClass;
import dynagent.ruleengine.meta.old.StaticClass;

public interface IKnowledgeBaseInfoExtended extends IKnowledgeBaseInfo{
	
	
	
	// extra functions
	public String getNameByID(int ido);
	public int getIDByName(String n);
	
	// basic functions
	public dynagent.ruleengine.meta.old.Class getClass(int ido);
	public LinkedList<dynagent.ruleengine.meta.old.Class> getClasses();
	public StaticClass getStaticClass(int ido);
	public LinkedList<StaticClass> getStaticClasses();
	public RoleClass getRoleClass(int rola);
	public LinkedList<RoleClass> getRoleClasses();
	public RelationClass getRelationClass(int idopeer);
	public LinkedList<RelationClass> getRelationClasses();
	
	// parent and child functions
	public LinkedList<dynagent.ruleengine.meta.old.Class> getParents(int ido);
	public LinkedList<dynagent.ruleengine.meta.old.Class> getChilds(int ido);
	
	// relationships
	public LinkedList<RoleClass> getRolesOf(int ido);
	public LinkedList<RelationClass> getRelationOf(int idrol);
	public LinkedList<RoleClass> getRolesOfClassInRelation(int ido, int idopeer);
	public LinkedList<StaticClass> getPlayersOf(int rola);
	public LinkedList<RoleClass> getRolesOfRelation(int idopeer);
	
	// Properties functions
	public Property getProperty(int ido, int idprop);
	public LinkedList<Property> getProperties(int ido);
	public Object getPropertyValue(int ido, int idprop);
	
	/* 
	public LinkedList<Property> getOnlyClassProperties(int ido);
	public LinkedList<Property> getOnlyRolClassProperties(int idrol);
	public LinkedList<Property> getOnlyProcessProperties(int idopeer);
	public dynagent.ruleengine.meta.Class getDecoratedRoleClass(int ido, int idrol); */
}
