package dynagent.ruleengine.masks;

import java.util.ArrayList;


import dynagent.common.basicobjects.Alias;
import dynagent.common.basicobjects.Mask;
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


public interface IMasks {
	public String getExpression(int prop,Integer idto,Integer utask) throws NotFoundException;
	
	public Integer getLength(int prop,Integer idto,Integer utask);
		
	public void addMaskList(ArrayList<Mask> listMask);
	
	public void removeMaskList(ArrayList<Mask> listMask);
	
	public void setIk(IKnowledgeBaseInfo ik);
	
	public IKnowledgeBaseInfo getIk();
}
