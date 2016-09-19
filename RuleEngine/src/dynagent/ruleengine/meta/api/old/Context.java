/***
 * Context.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api.old;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.meta.KnowledgeBase;
import dynagent.ruleengine.meta.PrototypeClass;
import dynagent.ruleengine.meta.RoleClass;
import dynagent.ruleengine.meta.old.Attribute;
import dynagent.ruleengine.meta.old.RelationClass;
import dynagent.ruleengine.meta.old.StaticClass;

public class Context implements IContext{
	
	private KnowledgeBase kb = null;

	public PrototypeClass getDecoratedRoleClass(int ido, int idrol) {
		
		StaticClass cls = new StaticClass();
		RoleClass rcls = new RoleClass();
		IKnowledgeBaseInfoExtended i = (IKnowledgeBaseInfoExtended)KnowledgeBaseInfoFactory.getInstance().createKnowledgeBaseInfoExtended("extended");
		i.setKnowledgeBase(kb);
		cls = i.getStaticClass(ido);
		rcls = i.getRoleClass(idrol);
		
		PrototypeClass pcls = new PrototypeClass();
		pcls.setStaticclass(cls);
		pcls.setRoleclass(rcls);
		
		LinkedList<Attribute> atts = new LinkedList<Attribute>();
		Iterator it = pcls.getStaticclass().getAttributes().iterator();
		while(it.hasNext()){
			Attribute at = (Attribute)it.next();
			atts.add(at);
		}
		it = pcls.getRoleclass().getAttributes().iterator();
		while(it.hasNext()){
			Attribute at = (Attribute)it.next();
			if(!atts.contains(at))
				atts.add(at);
		}
		pcls.setAttributes(atts);
		pcls.setType("OWL");
		pcls.addParent(cls.getName());
		pcls.addParent(rcls.getName());
		pcls.setName(cls.getName()+rcls.getName());
		
		return pcls;
	}

	public LinkedList<Property> getOnlyClassProperties(int ido) {
		
		IKnowledgeBaseInfoExtended i = (IKnowledgeBaseInfoExtended)KnowledgeBaseInfoFactory.getInstance().createKnowledgeBaseInfoExtended("extended");
		i.setKnowledgeBase(kb);
		PrototypeClass pcls = (PrototypeClass)i.getClass(ido);
		
		LinkedList<Property> prop = new LinkedList<Property>();
		Iterator it = pcls.getStaticclass().getAttributes().iterator();
		while(it.hasNext()){
			Property p = (Property)it.next();
			prop.add(p);
		}
		return prop;
	}

	public LinkedList<Property> getOnlyProcessProperties(int idopeer) {
		IKnowledgeBaseInfoExtended i = (IKnowledgeBaseInfoExtended)KnowledgeBaseInfoFactory.getInstance().createKnowledgeBaseInfoExtended("extended");
		i.setKnowledgeBase(kb);
		RelationClass r = (RelationClass)i.getClass(idopeer);
		
		LinkedList<Property> prop = new LinkedList<Property>();
		Iterator it = r.getAttributes().iterator();
		while(it.hasNext()){
			Property p = (Property)it.next();
			prop.add(p);
		}
		return prop;
	}

	public LinkedList<Property> getOnlyRolClassProperties(int idrol) {
		
		IKnowledgeBaseInfoExtended i = (IKnowledgeBaseInfoExtended)KnowledgeBaseInfoFactory.getInstance().createKnowledgeBaseInfoExtended("extended");
		i.setKnowledgeBase(kb);
		PrototypeClass pcls = (PrototypeClass)i.getClass(idrol);
		
		LinkedList<Property> prop = new LinkedList<Property>();
		Iterator it = pcls.getRoleclass().getAttributes().iterator();
		while(it.hasNext()){
			Property p = (Property)it.next();
			prop.add(p);
		}
		return prop;
	}

	public KnowledgeBase getKb() {
		return kb;
	}

	public void setKb(KnowledgeBase kb) {
		this.kb = kb;
	}
	
}
