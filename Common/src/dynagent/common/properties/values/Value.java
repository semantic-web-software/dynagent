package dynagent.common.properties.values;


import org.jdom.Element;

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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.Property;

public abstract class Value{

	protected boolean equalToValue=true;

	/**
	 * Comprueba la compatibilidad de dos valores. Este método esta sobreescrito en las clases
	 * DataValue y ObjectValue
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 */
	public abstract boolean checkIsCompatibleWith(Property pr,IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException;
	
	public abstract boolean checkIsCompatibleWithNotException(Property pr,IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException;
	
	public Element toElement() {
		return null;
	}
	
	public Value clone(){
		return null;
	}
	
	public abstract String getValue_s();
	
	public boolean isEqualToValue(){
		return equalToValue;
	}
	
	public void setEqualToValue(boolean equalToValue){
		this.equalToValue=equalToValue;
	}
	
}
