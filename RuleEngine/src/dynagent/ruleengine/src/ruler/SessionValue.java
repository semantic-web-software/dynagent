/***
 * TemporalFact.java
 * @author: Hassan Ali Sleiman - hassansleiman@gmail.com
 * @description: A temporal fact is a fact used during sessions
 */
package dynagent.ruleengine.src.ruler;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.sessions.SessionController;


public class SessionValue{

	 
	 String VALUE=null;
	 Integer VALUECLS=null;
	 Double QMIN=null ;
	 Double QMAX=null;
	 String OP=null;
	 Integer IDTO=null;
	 String RANGENAME=null;
	 int idsesion;
	 String systemValue;
	 boolean appliedSystemValue;
	 Contribution contribution;
	 Double initialQ;//Usado para las reservas. Guarda la informacion de el valor de QMIN y QMAX que se tenia en la sesion actual
	 private long changeTime=0;
	 
	public class Contribution{
			private double value;
			private HashMap<String,Double> contribution;
			
			public Contribution(){
				contribution=new HashMap<String, Double>();
				value=0;
			}
			
			 @Override
			protected Contribution clone(){
				 Contribution c=new Contribution();
				 c.setContribution((HashMap<String,Double>)contribution.clone());
				 c.setValue(value);
				 return c;
			}

			public double getValue() {
				return value;
			}

			public void setValue(double value) {
				this.value = value;
			}

			public HashMap<String, Double> getContribution() {
				return contribution;
			}

			public void setContribution(HashMap<String, Double> contribution) {
				this.contribution = contribution;
			}
			
		}

	
	public SessionValue(IKnowledgeBaseInfo ik,Integer idto, String value, Integer valuecls,  Double qmin, Double qmax,String op, String systemValue, boolean appliedSystemValue, Contribution contribution, String RangeName, Double offset) {
		super();
		VALUE = value;
		VALUECLS = valuecls;
		QMIN = qmin;
		QMAX = qmax;
		OP = op;
		IDTO=idto;
		this.systemValue=systemValue;
		this.appliedSystemValue=appliedSystemValue;
		this.contribution=contribution;
		RANGENAME=RangeName;
		this.initialQ=offset;
		setChangeTime();
		if(SessionController.getInstance().getActualSession(ik)!=null)
			this.idsesion=SessionController.getInstance().getActualSession(ik).getID();
		
	}
	/*public SessionValue() {
		// TODO Auto-generated constructor stub
	}*/
	
	public void setChangeTime(){
		this.changeTime=System.currentTimeMillis();
	}
	
	public long getChangeTime(){
		return  this.changeTime;
	}
	
	public String getOP() {
		return OP;
	}
	public void setOP(String op) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		OP = op;
	}
	public Double getQMAX() {
		return QMAX;
	}
	public void setQMAX(Double qmax) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		QMAX = qmax;
	}
	public Double getQMIN() {
		return QMIN;
	}
	public void setQMIN(Double qmin) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		QMIN = qmin;
	}
	public int getSesion() {
		return idsesion;
	}
	public void setSession(int session) {
		this.idsesion = session;
	}
	public String getVALUE() {
		return VALUE;
	}
	public Integer getIDTO() {
		return IDTO;
	}
	public void setIDTO(Integer ido) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		IDTO = ido;
	}
	public void setVALUE(String value) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		
		VALUE = value;
	}
	public Integer getVALUECLS() {
		return VALUECLS;
	}
	public void setVALUECLS(Integer valuecls) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		VALUECLS = valuecls;
	}
	public String toString()
	{
	String resul ="";
		resul += " -> Session :"+this.idsesion 
	+"QMAX: "+QMAX + " QMIN: "+QMIN +" VALUE: "+VALUE + " VALUECLS: "+VALUECLS +" OP: "+OP; 
	return resul;
	}
	public String getRANGENAME() {
		return RANGENAME;
	}
	public void setRANGENAME(String rangename) {
		RANGENAME = rangename;
	}
	
	public String getSystemValue() {
		return systemValue;
	}
	public void setSystemValue(String systemValue) {
		this.systemValue = systemValue;
	}
	public Contribution getContribution() {
		return contribution;
	}
	public void setContribution(Contribution contribution) {
		this.contribution = contribution;
	}
	public boolean isAppliedSystemValue() {
		return appliedSystemValue;
	}
	public void setAppliedSystemValue(boolean appliedSystemValue) {
		this.appliedSystemValue = appliedSystemValue;
	}
	public Double getInitialQ() {
		return initialQ;
	}
	public void setInitialQ(Double offset) {
		this.initialQ = offset;
	}
	
}
