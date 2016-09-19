package dynagent.ruleengine.src.factories;

import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;

public interface IRulerFactory {
	public IKnowledgeBaseInfo getIKnowledgeBaseInfo() throws EngineException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException;
}
		
