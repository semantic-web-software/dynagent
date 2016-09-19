package dynagent.common.knowledge;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.utils.IndividualValues;

public interface IQuestionListener {

	public void request(int id,String rdn,ArrayList<IndividualValues> mapData,HashMap<String,String> alias, IKnowledgeBaseInfo ik) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException;
	
	public void cancelled(int id,String rdn,IKnowledgeBaseInfo ik);
}
