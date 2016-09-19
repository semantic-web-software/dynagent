package dynagent.common.process;

import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;

public interface IAsigned {

	public void reAsign(int idoUTask, String user, Integer rol) throws SystemException, InstanceLockedException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	public void asign(int idoUTask, String user, Integer rol) throws SystemException, InstanceLockedException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	public void preAsign(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	public void release(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	public void close(int idoUTask, int idtoUTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;

}
