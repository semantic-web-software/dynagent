package dynagent.ruleengine.src.ruler;

import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.basicobjects.IndividualValue;
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
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;


public class ObjValue extends Fact implements IndividualValue{
	//private Integer IDOVALUE;//no hace falta ya que se utiliza el del super siempre
	private String firstValue;
	
	public ObjValue(Integer idto, Integer ido, Integer prop, String value, Integer valueCls, String rangename,Double qmin, Double qmax, String op, String classname, boolean existeBD, String systemValue, boolean appliedSystemValue, String destinationSystem, IKnowledgeBaseInfo ik){
		super(idto, ido, prop, value, valueCls, rangename,qmin, qmax, op, classname, existeBD, systemValue, appliedSystemValue, destinationSystem, ik);
		firstValue=KnowledgeAdapter.getVALUE_s(this);
	}
	
	public ObjValue(Integer idto, Integer ido, Integer prop, String value, Integer valueCls, String rangename,Double qmin, Double qmax, String op, String classname, boolean existeBD, String systemValue, boolean appliedSystemValue, String destinationSystem, Integer idovalue, IKnowledgeBaseInfo ik){
		super(idto, ido, prop, value, valueCls, rangename,qmin, qmax, op, classname, existeBD, systemValue, appliedSystemValue, destinationSystem, ik);
		firstValue=KnowledgeAdapter.getVALUE_s(this);
		
	}

	public Integer getIDOVALUE() {
		if(super.getVALUE()!=null)
			return Integer.parseInt(super.getVALUE())
			;
		else
			return null;
	}
	
	public Integer getINITIALRANGE() {
		String init=this.getINITIALVALOR();
        if( init!=null){
        	return new Integer(init);
        }else
        	return null;
	}
	public void setIDOVALUE(Integer idovalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(Auxiliar.equals(idovalue, getIDOVALUE())) return;
		super.setVALUE(idovalue==null?null:idovalue.toString());
	}

	public String getVALOR() {
		return super.getVALUE_s();
	}

	public void setVALUE(String val) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		if(Auxiliar.equals(val, getVALOR())) return;
		super.setVALUE(val);
	}
	public String getVALUE()
	{
		return super.getVALUE();
	}
	
	
	public Integer getIDOPREVALOR(){
		if(this.getPREVALOR()==null)
			return null;
		else return Integer.parseInt(this.getPREVALOR());
	}
	
	
	public String toString()
	{
		String result = null;
		try {
			result = "(ObjValue (LEVEL "+this.getLEVEL()+")(IDTO "+getIDTO()+")(CLASSNAME "+getCLASSNAME()+")(IDO "+getIDO()+")(PROPNAME= "+this.getPROPNAME()+")(DESTINATIONSYSTEM= "+this.getDestinationSystem()+")(VALOR "+getVALOR()+")(INITALVALOR "+this.getINITIALVALOR()+")(IDVALUE "+this.getIDOVALUE()+")(RANGENAME "+this.getRANGENAME()+")(APPLIEDSYSTEMVALUE "+this.isAppliedSystemValue()+")(SYSTEMVALUE "+this.getSystemValue()+"))";
			result += "(INITIALVALOR=" + this.getINITIALVALOR()+")";
			result += "(HASCHANGED=" + this.hasCHANGED()+")";
			result += "\n(prevalor="+this.getPREVALOR()+")";
			result += "\n(existiaBD="+super.getExistia_BD()+")";
			result += "(deleted="+ this.getDeleted() +")";
			result += "(systemValue="+this.getSystemValue()+")";
			result += "(sesiones="+ this.sessionValues +")";
			result += "\n(valoresAnteriores="+super.getValoresAnteriores()+")";
		
		} catch (NotFoundException e) {
			
			e.printStackTrace();
		}
		result+=")";
		return result;
	}

	public ObjectValue getOBJECTVALUE() {
		if(this.getVALUE()==null){
			return null;
		}
		else{
			return new ObjectValue(Integer.parseInt(this.getVALUE()),this.getVALUECLS());
		}
	}
	
	public Object clone(IKnowledgeBaseInfo ik) {
		return new ObjValue(getIDTO(),getIDO(),getPROP(),getVALUE(),getVALUECLS(),getRANGENAME(),getQMIN(),getQMAX(),getOP(),getCLASSNAME(),getExistia_BD(),getSystemValue(),isAppliedSystemValue(),getDestinationSystem(),ik);
	}
	
	
	public String toStringAmpliado() {
		return this.toString()+"\n (OP=="+this.getOP();
	
	}


	public String getFIRSTVALUE() {
		return firstValue;
	}

	@Override
	public Value getCVALUE() {
		return this.getOBJECTVALUE();
	}
}
