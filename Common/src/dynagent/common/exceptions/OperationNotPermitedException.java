
/**
 * @author Jose A. Zamora
 * @description: Excepci�n propia que ser� lanzada cuando se intente realizar una operaci�n
 * no permitida por falta de permisos por ejemplo.
 */
package dynagent.common.exceptions;

import dynagent.common.properties.Property;

public class OperationNotPermitedException extends RuleEngineException {
	/*private Property prop=null;
	public Property getProp() {
		return prop;
	}
	public void setProp(Property prop) {
		this.prop = prop;
	}*/
	public OperationNotPermitedException(String msg/*, Property prop*/){
		super(msg);
		//this.prop=prop;
	}
	

}
