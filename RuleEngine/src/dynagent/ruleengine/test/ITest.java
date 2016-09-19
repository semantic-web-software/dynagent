/***
 * ITest.java
 * @author: Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.test;

import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;

public interface ITest {
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, IncoherenceInMotorException;
}
