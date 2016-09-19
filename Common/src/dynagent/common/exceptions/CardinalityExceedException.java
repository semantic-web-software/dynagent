/**
 * @author Jose A. Zamora
 * @description: Excepci�n propia que ser� lanzada cuando se intente a�adir a motor
 * un valor que no permite la cardinalidad.
 */

package dynagent.common.exceptions;

import dynagent.common.knowledge.PropertyValue;
import dynagent.common.properties.Property;
	
	

public class CardinalityExceedException  extends RuleEngineException{
	private Property prop=null;
	
	public CardinalityExceedException(String msg, Property prop){
		super(msg);
		this.prop=prop;
	
	}
	public Property getProp() {
		return prop;
	}
	public void setProp(Property prop) {
		this.prop = prop;
	}
	

}


