
/**
 * @author Jose A. Zamora
 * @description: Excepci�n propia que ser� lanzada cuando se intente a�adir a motor
 * un valor no compatible.
 */
package dynagent.common.exceptions;

public class IncompatibleValueException extends RuleEngineException{

	public IncompatibleValueException(String msg){
		super(msg);
	}
	
}
