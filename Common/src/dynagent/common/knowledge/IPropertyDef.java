package dynagent.common.knowledge;

import java.sql.SQLException;
import java.text.ParseException;

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

public interface IPropertyDef {

	 
	
	public Integer getIDO();	
	public Integer getIDTO();
	public String getCLASSNAME();
	public String getOP() ;
	public void setOP(String op) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	public int getPROP();
	public Double getQMAX();
	public Double getQMIN();	
	public void setQMAX(Double qmax)throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	public void setQMIN(Double qmin)throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	public String getVALUE();
	public Integer getVALUECLS();
	public String toInstanceString();
	public String toString();
	public FactInstance toFactInstance();
	public String toQueryString();
	public int getOrder();
	public void setOrder(int order);
	public boolean getExistia_BD();
	public boolean initialValuesChanged();
	public IPropertyDef getInitialValues();
	//public boolean isTemporalDeleted();
	public void setInitialValues(IPropertyDef f);
	public String getRANGENAME();
	public String getSystemValue();
	public void setSystemValue(String systemValue);
	public void setAppliedSystemValue(boolean appliedSystemValue);
	public boolean isAppliedSystemValue();
	public boolean isIncremental();
	public String getDestinationSystem();
	//public String getRdnValue();
	public void setExistia_BD(boolean existiaBD);
	public String getRdn();
	public void setIDO(Integer ido);
	public String getRdnValue();
	public void setVALUE(String value) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
}
