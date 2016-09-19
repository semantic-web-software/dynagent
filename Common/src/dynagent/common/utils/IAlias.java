package dynagent.common.utils;

import java.util.ArrayList;


import dynagent.common.basicobjects.Alias;
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


public interface IAlias {
	public String getLabelProp(Integer prop,Integer idto, Integer group,Integer utask) throws NotFoundException;
	
	public String getLabelClass(Integer idto, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;
	
	public String getLabelUtask(Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;
	
	public String getLabelGroup(Integer group, String nameGroup, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;
	
	public void addAliasList(ArrayList<Alias> listAlias);
	
	public void removeAliasList(ArrayList<Alias> listAlias);
	
	public void setIk(IKnowledgeBaseInfo ik);
	
	public IKnowledgeBaseInfo getIk();
	
	public void addAlias(Alias alias);
	
	public boolean removeAlias(Alias alias);
}
