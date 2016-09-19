
/**
 * @author Jose A. Zamora
 * @description: Excepción propia que será lanzada cuando se intente añadir a motor
 * un valor no compatible.
 */
package dynagent.common.exceptions;

public class IncompatibleValueException extends RuleEngineException{

	public IncompatibleValueException(String msg){
		super(msg);
	}
	
}
