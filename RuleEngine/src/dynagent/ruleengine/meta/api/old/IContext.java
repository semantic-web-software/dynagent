/***
 * IContext.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api.old;

import java.util.LinkedList;

import dynagent.ruleengine.meta.PrototypeClass;

public interface IContext {

	public LinkedList<Property> getOnlyClassProperties(int ido);
	public LinkedList<Property> getOnlyRolClassProperties(int idrol);
	public LinkedList<Property> getOnlyProcessProperties(int idopeer);
	public PrototypeClass getDecoratedRoleClass(int ido, int idrol);
	
}
