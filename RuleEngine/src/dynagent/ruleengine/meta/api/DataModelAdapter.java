package dynagent.ruleengine.meta.api;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import javax.naming.NamingException;

import org.apache.commons.lang.time.DateFormatUtils;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.IteratorQuery;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.queryData;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.ResultQuery;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.Domain;
import dynagent.common.properties.DomainProp;
import dynagent.common.properties.IDIndividual;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.*;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.ExecuteActionRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.IndividualValues;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.Utils;
import dynagent.common.xml.QueryXML;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.src.ruler.ContributionValue;
import dynagent.ruleengine.src.ruler.DatValue;
import dynagent.ruleengine.src.ruler.Query;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.Preload;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QueryValue;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QuestionRequest;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.StringChanged;
import dynagent.ruleengine.src.sessions.SessionController;

public class DataModelAdapter{

	
	
	private DocDataModel ddm=null;

	public DataModelAdapter(DocDataModel ddmodel) {
		this.ddm=ddmodel;
	}

	public DataModelAdapter() {
	}
	long t=0;
	public void incT(long inc){
		t+=inc;
	}
	public long getT(){
		return t;
	}
//	OBTENER VALORES(GETVALUES)	
	
	public void logQueryValue(String key,Object v) throws NotFoundException, IncoherenceInMotorException{
		QueryValue qv=new QueryValue(key,v,ddm);
		System.err.println("INSERTANDO QUERY "+key +" valor "+v);
		ddm.getRuleEngine().insertFact(qv);
	}
	
	public void retractQueryLog(QueryValue  f) {
		ddm.retractQueryLog(f) ;
	}
	
	public Boolean getBooleanValue(IDIndividual dom,String prop,boolean nullAsFalse) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		Object resDV=getValue(new Domain(dom),prop);
		if(resDV==null) return nullAsFalse? false:null;
		if(!(resDV instanceof BooleanValue)) throw new IncompatibleValueException("El tipo de datos no es Booleano");
		return ((BooleanValue)resDV).getBvalue();
	}
	
	public String getStringValue(IDIndividual dom,String prop) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		Object resDV=getValue(new Domain(dom),prop);
		if(resDV==null) return null;
		if(!(resDV instanceof StringValue)) throw new IncompatibleValueException("El tipo de datos no es Booleano");
		return ((StringValue)resDV).getValue();
	}
	
	public String getStringValue(Integer ido,Integer idto,String prop) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		Object resDV=getValue(new Domain(ido,idto),prop);
		if(resDV==null) return null;
		if(!(resDV instanceof StringValue)) throw new IncompatibleValueException("El tipo de datos no es Booleano");
		return ((StringValue)resDV).getValue();
	}
	
	public Double getDoubleValue(IDIndividual dom,String prop,boolean zeroBased) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		if(dom.getIDOIndividual()==0||getIdProperty(prop)==null) return zeroBased? 0.0:null;
		
		Object resDV=getValue(new Domain(dom),prop);
		if(resDV==null){
			return zeroBased? 0.0:null;
		}
		if(!(resDV instanceof DoubleValue)) throw new IncompatibleValueException("El tipo de datos no es Double");
		return ((DoubleValue)resDV).getValue();
	}

	public Long getTimeInSeconds(IDIndividual dom,String prop,boolean zeroBased) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		if(dom.getIDOIndividual()==0) return zeroBased? 0L:null;
		
		Object resDV=getValue(new Domain(dom),prop);
		if(resDV==null){
			return zeroBased? 0L:null;
		}
		if(!(resDV instanceof TimeValue)) throw new IncompatibleValueException("El tipo de datos no es Fecha");
		
		return ((TimeValue)resDV).getRelativeSecondsMax();
	}
	
	public Integer getIdoObjectValue(IDIndividual dom,String prop,boolean zeroBased) throws NumberFormatException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{		
		Object resDV=getValue(new Domain(dom),prop);
		if(resDV==null){
			return zeroBased? 0:null;
		}
		if(!(resDV instanceof ObjectValue)) throw new IncompatibleValueException("El tipo de datos no es Object");
		return ((ObjectValue)resDV).getValue();
	}
	
	
	public void setDestinationRecursively(HashSet<Integer> idosProcesados, Integer ido,String destination,HashSet<String> exclusions,String destinoDescarte) throws NotFoundException{
		getDDM().setDestinationRecursively(idosProcesados,ido,destination,exclusions,destinoDescarte);
	}
	
	
	public Value getValue(int ido,int idto,String nameProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			return this.getValue(ido, idto,this.getIdProperty(nameProp) );
		}else return null;
	}

	public Value getValue(int ido,int idto,int idprop) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Value val=null;
		if(this.checkWithModel(idto, idprop)){
			PropertyValue pv=ddm.SystemGetPropertyValue(ido,idto, idprop);
			if(pv!=null)
				val=pv.getUniqueValue();
		}
		return val;
	}

	public Value getValue(Domain dom,int idprop) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Value val=null;
		if(this.checkWithModel(dom.getIdto(), idprop)){
			PropertyValue pv=ddm.SystemGetPropertyValue(dom.getIdo(),dom.getIdto(), idprop);
			if(pv!=null)
				val=pv.getUniqueValue();
		}
		return val;
	}
	public StringChanged getInitialValue(int ido, String prop){	
		LinkedList<StringChanged> lista=ddm.getInitialValue(ido, this.getIdProperty(prop));
		if(lista.size()>0) return lista.get(0);
		else return new StringChanged(null,false);		
	}
	
	public Value getValue(Domain dom,String nameProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Value val=null;
		if(this.checkWithModel(dom.getIdto(), nameProp)){
			PropertyValue pv=ddm.SystemGetPropertyValue(dom.getIdo(),dom.getIdto(), this.getIdProperty(nameProp));
			if(pv!=null)
				val=pv.getUniqueValue();
		}return val;
	}
	public Value getValue(IDIndividual dom,String nameProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Value val=null;
		if(this.checkWithModel(dom.getIDTOIndividual(), nameProp)){
			PropertyValue pv=ddm.SystemGetPropertyValue(dom.getIDOIndividual(),dom.getIDTOIndividual(), this.getIdProperty(nameProp));
			if(pv!=null)
				val=pv.getUniqueValue();
		}return val;
	}
	
	public ObjectValue getObjectValue(Integer ido,int idto,String nameProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			return (ObjectValue)this.getValue(ido, idto, nameProp);
		}else return null;
	}

	public LinkedList<Value> getValues(int ido, int idto,int idProp) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		LinkedList<Value> values=new LinkedList<Value>();
		if(this.checkWithModel(idto, idProp)){
			PropertyValue pv=ddm.SystemGetPropertyValue(ido, idto,idProp);
			if(pv!=null)
				values=pv.getValues();
		}
		return values;
	}

	public LinkedList<Value> getValues(int ido, int idto,String nameProp) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		LinkedList<Value> values=new LinkedList<Value>();		
		if(this.checkWithModel(idto, nameProp)){		
			PropertyValue pv=ddm.SystemGetPropertyValue(ido, idto,this.getIdProperty(nameProp));
			if(pv!=null)
				values=pv.getValues();

		}
		return values;
	}	
	
	public LinkedList<Value> getValues(Domain dom,String nameProp) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		LinkedList<Value> values=new LinkedList<Value>();		
		if(this.checkWithModel(dom.getIdto(), nameProp)){		
			PropertyValue pv=ddm.SystemGetPropertyValue(dom.getIdo(),dom.getIdto(),this.getIdProperty(nameProp));
			if(pv!=null)
				values=pv.getValues();

		}
		return values;
	}	

	public LinkedList<Value> getValues(Domain dom,int idProp) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, NotFoundException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		LinkedList<Value> values=new LinkedList<Value>();	
		if(this.checkWithModel(dom.getIdto(), idProp)){		
			PropertyValue pv=ddm.SystemGetPropertyValue(dom.getIdo(),dom.getIdto(),idProp);
			if(pv!=null)
				values=pv.getValues();

		}
		return values;
	}	

	//	MANEJO DE VALORES (SET,ADD Y DEL)	
	private void setValue(int ido,int idto,int idProp,Value val, boolean checkSystemValue) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, idProp)){ 
			if(!ddm.existInMotor(ido)){
				ddm.getFromServer(ido,idto, 1,null,Constants.USER_SYSTEM, null);
			}	
			if(!ddm.hasProperty(idto, idProp)){
				System.err.println("\n\n=============  WARNING INFO: se intenta hacer un setValue a un objeto con una propiedad que no tiene su clase: idprop="+idProp+"  nameclass="+ddm.getClassName(ido)+"  ido="+ido);
			}
			else{
				Property pr=ddm.SystemGetProperty(ido, idto,idProp);
				//System.err.println("\n DEBUG setValue IDO:"+ido+" IDTO:"+idto+" idprop:"+idProp+"  val:"+val+"  prval:"+pr+" checkSystemValue:"+checkSystemValue);

				if(pr.getNumeroValores()==0){ //NO TIENE VALOR--->SE HACE UN NEW			 
					ddm.SystemSetValue(pr, null, val,checkSystemValue);
				}
				else if(pr.getNumeroValores()==1){//SI TIENE EXACATAMENTE UN VALOR, LO SUSTIMOS POR EL NUEVO 
					ddm.SystemSetValue(pr, pr.getUniqueValue(), val,checkSystemValue);
				}else {
					//SI TIENE M�S DE UNA VALOR, LOS BORRAMOS TODOS Y SE PONE EL NUEVO
					//Borramos los otros valores
					ddm.SystemDelValues(pr);
					ddm.SystemSetValue(pr, null, val,checkSystemValue);
				}
			}
		}
	}

	public void setValue(IDIndividual dominio, String nameProp, Value value) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dominio.getIDTOIndividual(), nameProp)){	
			this.setValue(dominio.getIDOIndividual(), dominio.getIDTOIndividual(), this.getIdProperty(nameProp), value, false);
		}
	}	 

	public void setStringValue(int ido,int idto,String nameProp, String valor) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		 setDataValue(ido, idto,nameProp,valor);
	}
	public void setStringValue(Domain dom,String nameProp, String valor) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		 setDataValue(dom,nameProp,valor);
	}
	
	public void setValue(Domain dominio, Integer idProp, Value value) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(dominio.getIdo(), dominio.getIdto(), idProp, value, false);
	}

	public void setValue(int ido,int idto,int idprop,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(ido, idto, idprop, val,false);
	}
	
	
	/**
	 * M�todo para hacer setValue a filtros. De momento la implementaci�n interna es la misma que la del setValue pero en un futuro debe hacerse el setvalue de otra forma (a�adiendo operador para indicar
	 * que es un filtro. Hacer este metodo mientras tanto es para ir desacoplando las reglas de ese cambio futuro.
	 * @param idoFilter
	 * @param idtoFilter
	 * @param idprop
	 * @param val
	 * @throws NotFoundException
	 * @throws CardinalityExceedException
	 * @throws OperationNotPermitedException
	 * @throws IncompatibleValueException
	 * @throws IncoherenceInMotorException
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws dynagent.common.exceptions.CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws DataErrorException
	 * @throws NumberFormatException
	 * @throws ParseException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 */public void setFilterValue(int idoFilter,int idprop,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
//		if(!this.getDDM().isIDFilter(idoFilter)){
//			throw new OperationNotPermitedException("\n\n No se permite hacer un setFilterValue a un ido que no es un filtro. idoFilter:"+idoFilter+" clsFiltro:"+ddm.getClassName(idoFilter)+" level:"+this.getDDM().getLevelOf(idoFilter));
//		}
		 this.setValue(idoFilter, ddm.getClassOf(idoFilter), idprop, val);
	}
	 
	 
	 public void setFilterValue(String sidoFilter,int idprop,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 this.setFilterValue(Integer.parseInt(sidoFilter), idprop, val);
	 }
	 
	 public void setFilterValue(int idoFilter,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 this.setFilterValue(idoFilter, ddm.getIdProperty(nameProp), val);
	 }
	 
	 public void setFilterValue(String sidoFilter,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 this.setFilterValue(Integer.parseInt(sidoFilter),  this.getIdProperty(nameProp), val);
	 }
	 
	public void setValue(int ido, int idto, String nameProp, Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp = this.getIdProperty(nameProp);
		if(idProp!=null)
			this.setValue(ido, idto, idProp, val,false);
		else{
			System.err.println("\n ============== WARNING: setValue prop="+nameProp+" no hace nada pq no existe esa propiedad: ido:"+ido+" idto:"+idto);
		}
	}
	
	public void setValue(String sid,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp = this.getIdProperty(nameProp);
		if(idProp!=null)
			this.setValue(Integer.parseInt(sid), idto, idProp, val,false);
	}

	public void setObjectValue(int id,int idto,int idprop,int value,int valuecls) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(id, idto, idprop, new ObjectValue(value,valuecls),false);
	}

	public void setObjectValue(int ido,int idto,String nameProp,int value,int valuecls) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp=this.getIdProperty(nameProp);
		if(idProp!=null){
			this.setValue(ido, idto, idProp, new ObjectValue(value,valuecls),false);
		} else{
			System.err.println("\n ============== WARNING: setObjectValue prop="+nameProp+" no hace nada pq no existe esa propiedad: ido:"+ido+" idto:"+idto);
		}
	}

	public void setDataValue(int id,int idto,int idprop,String valor_s) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(id, idto, idprop, this.buildDataValue(idprop, valor_s),false);
	}

	public void setDataValue(int id,int idto,String nameProp,String valor_s) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp=this.getIdProperty(nameProp);
		if(idProp!=null){
			this.setValue(id, idto, idProp, this.buildDataValue(idProp, valor_s),false);
		}
	}

	public void setDataValue(Domain dom,int idprop,String valor_s) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.setValue(dom.getIdo(), dom.getIdto(), idprop, this.buildDataValue(idprop, valor_s),false);
	}

	public void setDataValue(Domain dom,String nameProp,String valor_s) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp=this.getIdProperty(nameProp);
		if(idProp!=null){
			this.setValue(dom.getIdo(), dom.getIdto(), idProp, this.buildDataValue(idProp, valor_s),false);
		}else{
			System.err.println("\n ============== WARNING: setDataValue prop="+nameProp+" no hace nada pq no existe esa propiedad: dom:"+dom);
		}
	}


	public void setTimeValue(int ido, int idto, int idProp, Long segundos, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{		
		setDataValue(ido,idto,idProp,segundos,lock);
	}
	
	public void setIntegerValue(int ido, int idto, int idProp, Integer valor, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		setDataValue(ido,idto,idProp,valor,lock);
	}
	
	public void setDoubleValue(Domain dom, String prop, Double valor) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		setDataValue(dom.getIdo(),dom.getIdto(),getIdProperty(prop),valor,true);
	}
	
	public void setDoubleValue(int ido, int idto, int idProp, Double valor, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		setDataValue(ido,idto,idProp,valor,lock);
	}
	
	public void setBooleanValue(int ido, int idto, int idProp, Boolean valor, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		setDataValue(ido,idto,idProp,valor,lock);
	}
	public void setStringValue(int ido, int idto, int idProp, String valor, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		setDataValue(ido,idto,idProp,valor,lock);
	}
		
	public void setDataValue(int ido, int idto, int idProp, Object valor, boolean lock) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		DatValue dv=(DatValue)ddm.getFact(ido,idProp);
		if(dv!=null){
			if(valor instanceof Integer){				
				dv.setQ(((Integer)valor).doubleValue(),((Integer)valor).doubleValue());
			}
			if(valor instanceof Long){				
				dv.setQ(((Long)valor).doubleValue(),((Long)valor).doubleValue());
			}
			if(valor instanceof Double){				
				dv.setQ(((Double)valor),((Double)valor));
			}
			if(valor instanceof Boolean){	
				boolean b=((Boolean)valor).booleanValue();
				dv.setQ(b?1.0:0.0,b?1.0:0.0);
			}
			if(valor instanceof String)	dv.setVALUE(((String)valor));
		}else{
			if(!lock){
				Session actualSession=SessionController.getInstance().getActualSession(ddm);
				boolean oldLock=actualSession.isLockObjects();
				actualSession.setLockObjects(false);
				try{
					setDataValue( ido,idto,idProp,""+valor);
				}finally{
					actualSession.setLockObjects(oldLock);
				}
			}else{
				setDataValue( ido,idto,idProp,""+valor);
			}						
		}
	}
	public void setContributionValue(int ido,int idto,int idProp,ContributionValue contValue, Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{	 
		ddm.setContributionValue(ido, idto, idProp, contValue, nDigRedondeo);
	}
	
	
	public void setContributionValue(int ido,int idto,String nameProp,ContributionValue contValue, Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{	 
		this.setContributionValue(ido, idto, this.getIdProperty(nameProp), contValue, nDigRedondeo);
	}
	

	public void setIncrementalValue(int ido,int idto,int idProp,double incrementalValue,Integer nDigRedondeo ) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{	 
		ddm.setIncrementalValue(ido, idto, idProp, incrementalValue, nDigRedondeo);
	}
	
	
	public void addContributionValue(int ido,int idto,int idProp,String key,double incrementalValue,Integer nDigRedondeo) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{	 
		ddm.addContributionValue(ido, idto, idProp, key, incrementalValue, nDigRedondeo);
	}
	
	public DataValue buildDataValue(int idprop,String value) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return ddm.buildDataValue(idprop, value);
	}

	public DataValue buildDataValue(String nameProp,String value) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return ddm.buildDataValue(this.getIdProperty(nameProp), value);
	}


	public ObjectValue buildObjectValue(int idprop,String value,int valuecls) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return ddm.buildObjectValue(idprop, value,valuecls);
	}

//	public void setSuggestedValue(Domain dom,String nameProp,Value value) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
//		Integer idProp=this.getIdProperty(nameProp);
//		if(idProp!=null){
//			this.setValue(dom.getIdo(), dom.getIdto(),idProp, value, true);
//		}
//		else{
//			System.err.println("\n ============== WARNING: setSuggestedValue prop="+nameProp+" no hace nada pq no existe esa propiedad: dom:"+dom);
//		}
//	}	 
//
//	public void setSuggestedValue(int id,int idto,int idProp,Value value) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
//		this.setValue(id, idto,idProp, value, true);
//	}
//
//
//	public void setSuggestedValue(int ido,int idto,String nameProp,Value value) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
//		Integer idProp=this.getIdProperty(nameProp);
//		if(idProp!=null){
//			this.setValue(ido, idto,idProp, value, true);
//		}
//		else{
//			System.err.println("\n ============== WARNING: setSuggestedValue prop="+nameProp+" no hace nada pq no existe esa propiedad: ido:"+ido);
//		}
//	}
//
//	public void setSuggestedValue(String sid,int idto,String nameProp,Value value) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
//		Integer idProp=this.getIdProperty(nameProp);
//		if(idProp!=null){			
//			this.setValue(new Integer(sid), idto,idProp, value, true);
//		}
//		else{
//			System.err.println("\n ============== WARNING: setSuggestedValue prop="+nameProp+" no hace nada pq no existe esa propiedad: ido:"+sid);
//		}
//	}	 

	public void  addValue(int ido,int idto,int idProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, idProp)){
			ddm.SystemSetValue(ido, idto, idProp, null, val);
		}
	}

	public void  addValue(int ido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		Integer idProp=this.getIdProperty(nameProp);
		if(idProp!=null&&this.checkWithModel(idto, idProp)){		
			ddm.SystemSetValue(ido, idto, idProp, null, val);
		}else{
			System.err.println("\n ============== WARNING: addValue prop="+nameProp+" no hace nada idProp:"+idProp);
		}
	}
	public void  addValue(String sido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){	
				ddm.SystemSetValue(Integer.parseInt(sido), idto, idProp, null, val);
			}
		}
	}
	
	public void  addValue(IDIndividual dom,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dom.getIDTOIndividual(), nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){	
				ddm.SystemSetValue(dom.getIDOIndividual(), dom.getIDTOIndividual(), idProp, null, val);
			}
		}
	}
	
	
	public void  addFilterValue(String sidofiltro,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		this.addValue(sidofiltro, ddm.getClassOf(sidofiltro), nameProp, val);
	}
	

	public void  addSuggestedValue(int ido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){		
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){
				ddm.SystemSetSuggestedValue(ido, idto, idProp, null, val);
			}
		}
	}
	public void  addSuggestedValue(String sido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){			
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){
				ddm.SystemSetSuggestedValue(Integer.parseInt(sido), idto, idProp, null, val);
			}
		}

	}
	public void  addSuggestedValue(Domain dom,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dom.getIdto(), nameProp)){			
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){			
				ddm.SystemSetSuggestedValue(dom.getIdo(), dom.getIdto(), idProp, null, val);
			}
		}
	}


	public void  delValue(int ido,int idto,int idProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, idProp)){				
			ddm.SystemSetValue(ido, idto, idProp, val,null);
		}
	}

	public void  delValue(int ido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){			 
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){			
				ddm.SystemSetValue(ido, idto, idProp, val,null);
			}
		}
	}
	public void  delValue(String sido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){		
				ddm.SystemSetValue(Integer.parseInt(sido), idto,idProp,  val,null);
			}
		}
	}
	public void  delValue(Domain dom,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dom.getIdto(), nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){
				ddm.SystemSetValue(dom.getIdo(), dom.getIdto(),idProp,  val,null);
			}
		}
	}	

	public void delValues(int ido, int idto,int idProp)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, idProp)){			 			 
			Property p=ddm.SystemGetProperty(ido,idto, idProp);
			ddm.SystemDelValues(p);
		}
	}

	public void delValues(int ido,int idto,String nameProp)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null)//este metodo se usa para borrar posibles valores, incluso de propiedades que podr�a no tener un modelo concreto
				this.delValues(ido,idto,idProp);
		}
	}

	public void delValuesNotLock(int ido,int idto,String nameProp)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(idto, nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){//este metodo se usa para borrar posibles valores, incluso de propiedades que podr�a no tener un modelo concreto
				Session actualSession=SessionController.getInstance().getActualSession(ddm);
				boolean oldLockObjects=actualSession.isLockObjects();
				actualSession.setLockObjects(false);								
				try{
					this.delValues(ido,idto,idProp);
				}finally{
					 actualSession.setLockObjects(oldLockObjects);
				}				 
			}
		}
	}



	 

	public void delValues(Domain dom,int idProp)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dom.getIdto(), idProp)){				
			Property p=ddm.SystemGetProperty(dom.getIdo(),dom.getIdto(), idProp);
			ddm.SystemDelValues(p);
		}
	}
	public void delValues(Domain dom,String nameProp)throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		if(this.checkWithModel(dom.getIdto(), nameProp)){
			Integer idProp=this.getIdProperty(nameProp);
			if(idProp!=null){
				Property p=ddm.SystemGetProperty(dom.getIdo(),dom.getIdto(), idProp);
				ddm.SystemDelValues(p);
			}
		}
	}

//	ELIMINACION DE INDIVIDUOS

	public void deleteObject(int id,int idto) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		ddm.SystemDeleteObject(id, idto);
	}
	
	public void deleteObject(IDIndividual idind) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		ddm.SystemDeleteObject(idind.getIDOIndividual(), idind.getIDTOIndividual());
	}
	

//	CREACI�N DE INDIVIDUOS:

	public Domain creaIndividualOfClass(int idto) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		int ido = ddm.createPrototype(idto, Constants.LEVEL_PROTOTYPE, null, Constants.USER_SYSTEM, null, false);
	
		
		return new Domain(ido,idto);
	}	 

	public Domain creaIndividualOfClass(String nameClass) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		int idto=ddm.getIdClass(nameClass); 
		int ido = ddm.createPrototype(idto, Constants.LEVEL_PROTOTYPE, null, Constants.USER_SYSTEM, null, false);
		return new Domain(ido,idto);
	}	 


//	CREACI�N DE FILTROS:

	public Domain creaFilter(int idto) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		int ido = ddm.createPrototype(idto, Constants.LEVEL_FILTER, null, Constants.USER_SYSTEM, null, true);
		return new Domain(ido,idto);
	}	 

	public Domain creaFilter(String  nameClass) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		int idto=ddm.getIdClass(nameClass); 
		int ido = ddm.createPrototype(idto, Constants.LEVEL_FILTER, null, Constants.USER_SYSTEM, null, true);
		return new Domain(ido,idto);
	}	 


//	METODOS DE CLONACION 		  

	public Domain cloneIndividual(int idoOrigen,int idtoOrigen, int idtoDestino,String excluPropertiesSeparAlmohadilla) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		return cloneIndividual(idoOrigen, idtoOrigen, idtoDestino, excluPropertiesSeparAlmohadilla, false);
	}
	
	public Domain cloneIndividual(int idoOrigen,int idtoOrigen, int idtoDestino,String excluPropertiesSeparAlmohadilla, boolean consumeChanges) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		LinkedList<String> excluProp=new LinkedList<String>();
		if(excluPropertiesSeparAlmohadilla!=null){
			String[] vpropiedades=excluPropertiesSeparAlmohadilla.split("#");
			for(int i=0;i<vpropiedades.length;i++){
				excluProp.add(vpropiedades[i]);
			}
		}
		return ddm.SystemCloneIndividual(idoOrigen, idtoOrigen, idtoDestino,excluProp,consumeChanges);
	}


//	BLOQUEOS EN SERVER DE INDIVIDUOS

	public void ruleSetLock(int ido) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ApplicationException, CardinalityExceedException {
		ddm.setLock(ido, true, Constants.USER_SYSTEM, true);
	}

	public void ruleSetLock(String sido) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, IncoherenceInMotorException, ApplicationException, NumberFormatException, CardinalityExceedException {
		ddm.setLock(Integer.parseInt(sido), true, Constants.USER_SYSTEM, true);
	}

	public String getIndividualState(int ido){
		return ddm.getIndividualState(ido);		
	}
//	CARGA/CONSULTAS AL SERVIDOR	

	// 0 CARGA DE INDIVIDUO SI NO EXISTE EN MOTOR

	public boolean loadIndividualIfNotExists(int ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		boolean prevcargado=ddm.existInMotor(ido);


		if(prevcargado){
			return true;
		}
		else{
			HashSet<Integer> idos=this.ruleGetFromServer(ido, idto, 1, false);
			return (idos!=null&&idos.size()>0);
		}
	}			

	
	public boolean loadIndividuals(HashMap<Integer,HashSet<Integer>> idtoIdos) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		HashSet<Integer> idos=ddm.getFromServer(idtoIdos, null, Constants.USER_SYSTEM, null, false, 1, false,true, true,false,false);
		return (idos!=null&&idos.size()>0);
	}
	
	public boolean loadIndividuals(HashMap<Integer,HashSet<Integer>> idtoIdos,int depth) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		HashSet<Integer> idos=ddm.getFromServer(idtoIdos, null, Constants.USER_SYSTEM, null, false, depth, false,true, true,false,false);
		return (idos!=null&&idos.size()>0);
	}
	
	public boolean preload(int ido,int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		if(!ddm.existInMotor(ido)){
			Preload preload=new Preload(ido,idto,ddm);
			ddm.getRuleEngine().insertFact(preload);
		}
		
		return true;
	}
	
	public boolean preload(ObjectValue objectValue) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		return preload(objectValue.getValue(), objectValue.getValueCls());
	}
	
	//1) CARGAS DIRECTAS INDICANDO EL/LOS IDENTIFICADORES

	public  HashSet<Integer> ruleGetFromServer(int id, int idto, int profundidad,boolean lock) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		return ddm.getFromServer(id,idto, null, Constants.USER_SYSTEM, null,false,profundidad,lock,true,true);
	}	
	
	public  HashSet<Integer> ruleGetFromServer(int id, int idto, int profundidad,boolean lock,boolean lastStructLevel) throws NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		return ddm.getFromServer(id,idto, null, Constants.USER_SYSTEM, null,false,profundidad,lock,lastStructLevel,true);
	}

	/**
	 * Funci�n intermedia para adecuar los datos de entrada de las reglas a lo que pide el servidor.
	 * 
	 * @param objValues Lista de instancias de la clase Value que contienen el ID_O y el ID_TO de cada individuo.
	 */
	public HashSet<Integer> ruleGetFromServer(HashSet<IDIndividual> idsIndividuals, int profundidad, boolean lock,boolean requeridaCoherencia) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		return ruleGetFromServer(idsIndividuals, profundidad, lock,requeridaCoherencia, true); 
	}	

	public HashSet<Integer> ruleGetFromServer(HashSet<IDIndividual> idsIndividuals, int profundidad, boolean lock,boolean requeridaCoherencia,boolean descartaIncoherente) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		HashMap<Integer, HashSet<Integer>> mapaClaseIndividuos = new HashMap<Integer, HashSet<Integer>>();
		//System.err.println("\n DEBUG ruleGetFromServer objValues:\n"+objValues);
		for (IDIndividual idindividual : idsIndividuals) {
			Integer clase = idindividual.getIDTOIndividual();
			HashSet<Integer> individuos = mapaClaseIndividuos.get(clase);
			if(idindividual.getIDOIndividual()<0) continue;
			
			if(individuos == null){
				//no existe la clase en el mapa -> ningun individuo -> lo creamos
				individuos = new HashSet<Integer>();
				individuos.add(idindividual.getIDOIndividual());
				mapaClaseIndividuos.put(clase, individuos);
			}else{
				//ya existe -> a�adimos el nuevo individuo
				individuos.add(idindividual.getIDOIndividual());
				mapaClaseIndividuos.put(clase, individuos);
			}
		}
		return ddm.getFromServer(mapaClaseIndividuos, null, Constants.USER_SYSTEM, SessionController.getInstance().getActualSession(ddm).getUtask(), false, profundidad, lock,true, true,requeridaCoherencia,descartaIncoherente);
	}
	
	//2 CARGA INDIVIDUOS INDICANDO CONDICIONES QUE DEBEN CUMPLIR (CONDICIONES DEBEN ESTABLECERSE EN EL INSTANCE, FIJANDO VALORES, FIJANDO EXCLUVALORES,... SEG�N CORRESPONDA

	
	public HashSet<IDIndividual>  loadIndividualsWithSatisficedFilter( instance filter ) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		return this.loadIndividualsWithSatisficedFilter(filter,1,false);
	}

	
	
	public HashSet<IDIndividual>  loadIndividualByRdn( String classStr,String rdn, int niveles ) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
		dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		Domain ido = creaFilter(classStr);
		instance filter = getTreeObject(ido.getIdo());		
		Property pRdn=filter.getProperty(ido.getIdo(), Constants.IdPROP_RDN);
		Value value=ddm.buildDataValue(Constants.IdPROP_RDN, rdn);
		pRdn.getValues().add(value); 
		return loadIndividualsWithSatisficedFilter(filter,niveles,false);				
	}
	
	
	
	public HashSet<IDIndividual>  loadIndividualsWithSatisficedFilter( instance filter,int profundidad, boolean requeridaCoherencia ) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		return this.loadIndividualsWithSatisficedFilter(filter,1,false,requeridaCoherencia);
	}
	
	public HashSet<IDIndividual>  loadIndividualsWithSatisficedFilter( instance filter,int profundidad,boolean lock,boolean requeridaCoherencia ) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
	
		return loadIndividualsWithSatisficedFilter( filter,profundidad,lock,requeridaCoherencia,true );
	}

	public HashSet<IDIndividual>  loadIndividualsWithSatisficedFilter( instance filter,int profundidad,boolean lock,boolean requeridaCoherencia,boolean descartaIncoherente ) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
	
		QueryXML query=ddm.getQueryXML();
		//System.err.println("\n------------------ DEBUG loadIndividualsWithSatisficedFilter2 param filter:\n"+filter);
		Element queryxml = query.toQueryXMLWithRealIdos(filter,null);
		//FALLA, TODO DEPURAR MACARENAreturn ddm.getFromServer(queryxml, null, Constants.USER_SYSTEM, null, false);
		//RODEO PARA PODER SEGUIR HACIENDO PRUEBAS: OBTENER IDOS DE SERVERGETVALUES Y LUEGO HACER GETFROM SERVER POR IDOS CONCRETOS
		ArrayList <ResultQuery> resulqueries=serverGetValuesWhichSatisfaceFilter( filter,"rdn");
		//System.err.println("\n DEBUG loadIndividualsWithSatisficedFilter filter.ido="+filter.getIDO()+"  filterClase:"+this.getClassName(filter.getIdTo())+" filter:\n"+filter.toStringPropertiesWithValues()+"\n.. resulqueries: "+resulqueries);
				
				
		HashSet<IDIndividual> objValues=new HashSet<IDIndividual>();
		System.err.println(" LOAD "+resulqueries.size());
		for(int i=0;i<resulqueries.size();i++){
			objValues.add(new ObjectValue(resulqueries.get(i).getIdo(),resulqueries.get(i).getIDTO()));
		}
		if(objValues.size()>0)	ruleGetFromServer(objValues, profundidad, lock,requeridaCoherencia,descartaIncoherente);
		return objValues;
	}

	// 3 OBTENER VALORES EN SERVIDOR DE LOS INDIVIDUOS QUE SATISFACEN CIERTAS CONDICIONES  

	public ArrayList <ResultQuery> serverGetValuesWhichSatisfaceFilter(instance filter,String propiedadesSeparadasAlmohadilla) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException
	{
		ArrayList<ResultQuery> resultado = new ArrayList<ResultQuery>();
		ArrayList <SelectQuery> sq = new ArrayList <SelectQuery>();
		String[] vpropiedades=propiedadesSeparadasAlmohadilla.split("#");
		for(int i=0;i<vpropiedades.length;i++){
			int idprop=new Integer(this.getIdProperty(vpropiedades[i]));
			SelectQuery sq1 = new SelectQuery(String.valueOf(filter.getIDO()),idprop, null, null);
			sq.add(sq1);
		}
		QueryXML query=ddm.getQueryXML();
		query.setSelect(sq);
		Element xml = query.toQueryXMLWithRealIdos(filter,null);
		selectData sd = ddm.getServer().serverGetQuery( xml, SessionController.getInstance().getActualSession(ddm).getUtask(), queryData.MODE_ROOT);

		//System.err.println("\n\n DEBUG  serverGetValuesWhichSatisfaceFilter  PARAMETROS: filter:"+filter+"  "+"\n propiedades pedidas:"+propiedadesSeparadasAlmohadilla);
		//System.err.println("\n\n DEBUG  serverGetValuesWhichSatisfaceFilter  SELECT DATA OBTENIDO:\n"+sd);

		if(sd!=null){
			Iterator itsd = sd.getIterator();
				while (itsd.hasNext())
			{
				instance instance = (instance)itsd.next();
				HashMap<String,LinkedList<Value>> mapaResultquery=new HashMap<String,LinkedList<Value>>();
	
				for(int i=0;i<sq.size();i++){
					int idProp=sq.get(i).getIdProp();
					String propName=ddm.getPropertyName(idProp);
					
					PropertyValue p=instance.getProperty(instance.getIDO(), idProp);
					//System.err.println("\n\n DEBUG DDM.serverGetValuesWhichSatisfaceFilter: INSTANCE:"+instance+"\n\n.... PROPNAME:"+propName+"\n propertyValue:"+p);
					
					if(p!=null){
						//chekeamos que la property que devuelve el servidor es correcta (que sea  completa y con valores coherentes)
						//para seguir haciendo pruebas a pesar de que metodo server devuelve incorrecamnte valores duplicados
						p.checkPropertyWellDefined(ddm, null);
						//System.err.println(" DEBUG DDM.serverGetValuesWhichSatisfaceFilter a�ade a maparesultquery prop:"+propName+"  valores:"+p.getValues());
						mapaResultquery.put(propName, p.getValues());
					}
				}
				ResultQuery resulQuery=new  ResultQuery(instance.getIDO(),instance.getIdTo(),mapaResultquery);
				resultado.add(resulQuery);
			}
		}else{
			System.err.println("\n\n WARNING serverGetValuesWhichSatisfaceFilter no devuelve nada porque selectData es null");
			
		}
						/*TODO COMENTAR, SOLO PARA PRUEBA
								String idosSinOrdenar="";
								for(int i=0;i<resultado.size();i++){
									idosSinOrdenar+=resultado.get(i).getIdo();
								}
						*/
		Collections.sort(resultado);
					/*TODO COMENTAR, SOLO PARA PRUEBA		
							String idosOrdenados="";
							for(int i=0;i<resultado.size();i++){
								idosOrdenados+=resultado.get(i).getIdo();
							}
							System.err.println("\n... debug ddm.serverGetValuesWhichSatisfaceFilter ordena el resultado tomando como criterio los idos (para quitar aletoriedad a las tests");
							System.err.println("idosSinOrdenar:"+idosSinOrdenar+"  idosOrdenados: "+idosOrdenados);
							if(!idosOrdenados.equals(idosSinOrdenar)){
								System.err.println("idosSinOrdenar:"+idosSinOrdenar+"  idosOrdenados: "+idosOrdenados);
								System.err.println("la ordenaci�n ha cambiado la lista");
							}
					*/
//		System.err.println("\n\n DEBUG DDM.serverGetValuesWhichSatisfaceFilter devuelve: "+resultado);
		return resultado;
	}
	
	
	
	
	
	
	
	

	// 4 OBTENER VALORES DE SERVIDOR DE INDIVIDUOS CONCRETOS DE LOS QUE SE CONOCE SU IDENTIFICADOR.

	public PropertyValue  serverGetProperty(int ido,int idto,int idprop) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, ApplicationException, CommunicationException, ParseException, SQLException, NamingException
	{
		this.checkWithModel(idto, idprop);	    	
		Property result=null;
		ArrayList <SelectQuery> sq = new ArrayList <SelectQuery>();
		SelectQuery sq1 = new SelectQuery(String.valueOf(ido),idprop, null, null);
		sq.add(sq1);
		instance filter = new instance(idto, ido);
		QueryXML query=ddm.getQueryXML();
		query.setShowIdos(true);
		query.setSelect(sq);
		Element xml = query.toQueryXMLWithRealIdos(filter,null);
		Integer uTask = SessionController.getInstance().getActualSession(getDDM()).getUtask();
		selectData sd = ddm.getServer().serverGetQuery( xml, uTask, queryData.MODE_ROOT);
		Iterator itsd = sd.getIterator();
		instance i = null;
		if (itsd.hasNext()) {
			i = (instance)itsd.next();
			result = i.getProperty(ido, idprop);
			if(result==null){
				//System.err.println("\n WARNING: serverGetProperty OBTIENE PROPERTY NULL EN UN selectDAta con elementos. ido="+ido+" idto:"+idto+" idprop="+idprop);
				//System.err.println("\n\n  filter:"+filter+"  "+"\n propiedades pedida:"+ddm.getPropertyName(idprop)+" clase:"+ddm.getClassName(idto));
				//System.err.println("\n\n DEBUG SELECT DATA \n"+sd);
				//System.err.println("\n\n CONSULTAMOS AL SERVIDOR (SIN CARGAR EN MOTOR INFO COMPLETA DE ESE INDIVIDUO PARA DEPURAR ESTE METODO\n"+this.getDDM().agruparValoresIndividuos(this.getDDM().getFactValuesInBD(idto, ido)));
			}

		}else{			System.err.println("\n WARNING: SERVERGETPROPERTY OBTIENE INSTANCE VACIO");}
		return result;
	}

	public LinkedList<Value> serverGetValues(int  ido,int idto,int idProp) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException,  DataErrorException, InstanceLockedException, ApplicationException, CommunicationException, NumberFormatException, ParseException, SQLException, NamingException
	{
		this.checkWithModel(idto, idProp);
		LinkedList<Value> result= new LinkedList<Value>();
		PropertyValue pv= this.serverGetProperty(ido,idto, idProp);
		if(pv!=null)
			result=pv.getValues();
		return result;
	}

	public LinkedList<Value> serverGetValues(int  ido,int idto,String nameProp) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException,  DataErrorException, InstanceLockedException, ApplicationException, CommunicationException, NumberFormatException, ParseException, SQLException, NamingException
	{
		this.checkWithModel(idto, nameProp);
		LinkedList<Value> result= new LinkedList<Value>();
		PropertyValue pv= this.serverGetProperty(ido,idto,this.getIdProperty(nameProp));
		if(pv!=null)
			result=pv.getValues();
		return result;
	}

	public Value  serverGetValue(int   ido,int idto,int idProp) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException,  DataErrorException, InstanceLockedException, ApplicationException, CommunicationException, NumberFormatException, ParseException, SQLException,NamingException{
		this.checkWithModel(idto, idProp);
		Value val=null;
		PropertyValue pv=serverGetProperty(ido, idto, idProp);
		if(pv!=null)
			val=pv.getUniqueValue();
		return val;
	}

	public Value  serverGetValue(int   ido,int idto,String nameProp) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException,  DataErrorException, InstanceLockedException, ApplicationException, CommunicationException, NumberFormatException, ParseException, SQLException,NamingException{
		this.checkWithModel(idto, nameProp);
		Value val=null;
		PropertyValue pv=serverGetProperty(ido, idto, this.getIdProperty(nameProp));
		if(pv!=null)
			val=pv.getUniqueValue();
		return val;
	}
	
	public instance  buildInstanceWith(String clase,HashMap<String, Value> propiedadValor,HashMap<String, ArrayList<Value>> propiedadValores,HashMap<String, Value> excluValueS,ArrayList<String>excluproperties) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		return this.buildInstanceWith(ddm.getIdClass(clase), propiedadValor, propiedadValores, excluValueS, excluproperties);
	}
	
	
	public instance  buildInstanceWith(int idto, HashMap<String, Value> propiedadValor,HashMap<String, ArrayList<Value>> propiedadValores,HashMap<String, Value> excluValueS,ArrayList<String>excluproperties) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, JDOMException, SystemException, RemoteSystemException, 
	dynagent.common.exceptions.CommunicationException, DataErrorException, InstanceLockedException, ApplicationException, ParseException, SQLException, NamingException{
		Integer ido = ddm.createPrototype(idto, Constants.LEVEL_FILTER, null, Constants.USER_SYSTEM, null, false);
		instance filter = ddm.getTreeObject(ido, null, Constants.USER_SYSTEM, null,false);
		//System.err.println("\n .... debugbuildInstanceWith filter:\n"+filter); 
		//System.err.println("\n------------------ DEBUG buildInstanceWith: idto="+idto+" propiedadValor="+propiedadValor+" propiedadValores="+propiedadValores+"  excluValueS:"+excluValueS+"  excluproperties="+excluproperties);
		//A�ADIMOS AL FILTRO LAS RESTRICCIONES DE VALORES  EXCLUIDOS
		if(excluValueS!=null){
			Iterator propertiesIterator = excluValueS.keySet().iterator();
			while(propertiesIterator.hasNext())
			{
				String prop = propertiesIterator.next().toString();
				Integer idprop=this.getIdProperty(prop);
				Value excluvalue=excluValueS.get(prop);
				Property p=filter.getProperty(ido, idprop);
				if(p instanceof DataProperty){
					((DataProperty)p).getExcluList().add((DataValue)excluvalue);
				}
				else if(p instanceof ObjectProperty){
					((ObjectProperty)p).getExcluList().add((ObjectValue)excluvalue);
				}
				//System.err.println("\n Property tras excluirle valor:  p="+p);
			}
		}
		if(propiedadValor!=null){//A�ADIMOS AL FILTRO LOS VALORES DESEADOS
			Iterator<String> valuesIterator = propiedadValor.keySet().iterator();
			while(valuesIterator.hasNext())
			{
				String prop = valuesIterator.next().toString();
				Integer idprop=this.getIdProperty(prop);
				Value value=propiedadValor.get(prop);
				Property p=filter.getProperty(ido, idprop);
				//System.err.println("\n------------------ DEBUG buildInstanceWith: ido:"+ido+" (clase:)"+this.getClassName(ido)+" idprop:"+idprop+" p:"+p);
				p.getValues().add(value);
			}
		}
		if(propiedadValores!=null){
			Iterator<String> propertiesIterator = propiedadValores.keySet().iterator();
			while(propertiesIterator.hasNext()){
				String prop = (String)propertiesIterator.next();
				Integer idprop=this.getIdProperty(prop);
				Iterator<Value> valuesIter  = propiedadValores.get(prop).iterator();
				Property p=filter.getProperty(ido, idprop);
				while(valuesIter.hasNext())
				{
					Value value= valuesIter.next();
					p.getValues().add(value);
				}
				//System.err.println("\n--------------- DEBUG buildInstanceWith bucle propiedadValores: p:"+p);

			}
		}
		if(excluproperties!=null){
			for(int i=0;i<excluproperties.size();i++){//LAS PROPIEDADES QUE NO QUEREMOS QUE TENGAN VALOR SE LE INDICA AL SERVER CON CARDMAX=0
				//antes comprobamos que existe esa propiedad, pues podr�a ser una PROPIEDAD QUE SE HA EXCLUIDO A ESE CLIENTE, EN CUYO CASO
				//NO ES NECESARI EXCLUIRLA
				Integer idprop=this.getIdProperty(excluproperties.get(i));
				if(idprop!=null&&ddm.hasProperty(idto,idprop)){
					Property p=filter.getProperty(ido, idprop);
					p.setCardMax(0);
					p.setCardMin(0);  

				}else{
					System.err.println("\n  INFO  ddm.loadInRulerIdosWith: idto="+idto+" excluValueS="+excluValueS+" propiedadValor="+propiedadValor+" excluproperties="+excluproperties+"\n  NO VA A A EXLUIR la propiedad "+excluproperties.get(i)+" pq no est� en el modelo");  
				}
			}
		}
		ddm.SystemDeleteObject(ido, idto);
		// System.err.println("\n------------------ DEBUG buildInstanceWith: INSTANCE:\n"+filter);

		return filter;
	}

	public void setCardinality(int ido,int idto, int idProp, int cardExacta) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		this.setCardinality(ido, idto,idProp, cardExacta,cardExacta);
	}
	
	public void setCardinality(int ido,int idto, int idProp, Integer cardMin, Integer cardMax) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Property p =  ddm.SystemGetProperty(ido,idto,idProp);
		if(p.getCardMin()!=null&&p.getCardMin().intValue()!=cardMin&&p.getCardMax()!=null&&p.getCardMax().intValue()!=cardMax){
			System.err.println("\n\n INFO: DDM.setCardinality no hace nada pq ya tiene esa cardinalidad: ido="+ido+"  prop="+idProp+"  cardMin="+cardMin);  
		}
		else{
			//valuecls no importa, pero no puede ser nulo por problemas sesion (ponemos 0)
			IPropertyDef fi=new FactInstance(p.getIdto(),p.getIdo(),p.getIdProp(),null,0,null,cardMin!=null?new Double(cardMin):null,cardMax!=null?new Double(cardMax):null,Constants.OP_CARDINALITY,ddm.getClassName(p.getIdto()));
			ddm.getRuleEngine().insertFact(fi);
		}				 	 
	}
	
	
	public void setCardinality(int ido,int idto, String nameProp, int cardExacta) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		this.setCardinality(ido, idto, this.getIdProperty(nameProp), cardExacta,cardExacta);
	}
	
	public void setCardinality(int ido,int idto, String nameProp, Integer cardMin,Integer cardMax) throws NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		this.setCardinality(ido, idto, this.getIdProperty(nameProp), cardMin,cardMax);
	}
	
	public instance getTreeObject(int ido) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return ddm.getTreeObject(ido,null,Constants.USER_SYSTEM ,null);
	}

	/*public Double numValue(instance tree,String path){
		Double val=0.0;
		DataProperty dp=(DataProperty)this.getPropertyWithPath(tree,path);
		if(dp.getValues().size()==1){
			DataValue dv=(DataValue)dp.getValues().getFirst();
			if(dv==null)
				return null;
			else
				val=dv.getNumericValue();
		}
		return val;
	}

	public ArrayList<Double> numValues(instance tree,String path){
		ArrayList<Double> values=new ArrayList<Double>();
		ArrayList<Property> properties=this.getPropertiesWithPath(tree,path);
		Double val=0.0;
		for(int i=0;i<properties.size();i++){
			DataProperty dp=(DataProperty)properties.get(i);
			if(dp.getValues().size()==1){
				DataValue dv=(DataValue)dp.getValues().getFirst();
				val=dv.getNumericValue();
				values.add(val);
			}
		}
		return values;
	}*/

	public Double numValue(Property p){
		Double val=0.0;
		DataProperty dp=(DataProperty)p;
		//System.err.print("\n\n DEBUG NUMVALUE P="+p);
		if(dp.getValues().size()==1){
			DataValue dv=(DataValue)dp.getValues().getFirst();
			val=dv.getNumericValue();
		}
		return val;
	}


	public ArrayList<Double> numValues(Property p){
		ArrayList<Double> values=new ArrayList<Double>();
		Double val=0.0;
		for(int i=0;i<p.getValues().size();i++){
			DataValue v=(DataValue)p.getValues().get(i);
			val=v.getNumericValue();
			values.add(val);
		}
		return values;
	}
	
	
	public Integer getFilterWithPath(int idobegin,int idtobegin,String path){
		Property p=null;
		String  [] propiedades=path.split("[.]");
		int ido=idobegin;
		int idto=idtobegin;
		for(int i=0;i<propiedades.length;i++){
			String propiedad=propiedades[i];
			int idProp;
			try {
				idProp = this.getIdProperty(propiedad);
				p=this.getDDM().SystemGetProperty(ido, idto, idProp);
				if(p instanceof ObjectProperty&&((ObjectProperty)p).getRangoList().size()==1){
					ido=((ObjectProperty)p).getRangoList().getFirst();
					idto=this.getClassOf(ido);
				}else{
					System.out.println("  WARNING: ddm.getFilterWithPath path="+path+"  tree="+path+"  return null al llegar a prop="+p);
					return null;
				}
			} catch (Exception e) {
				System.out.println(" WARNING: ddm.getFilterWithPath: No se encontr� en motor la propiedad="+propiedad);
				return null;
			}
		}
		return ((ObjectProperty)p).getRangoList().getFirst();
	}
	
	public boolean printRule(String message){
		return ddm.printRule(message);

	}

	public DocDataModel getDDM() {
		return ddm;
	}

	public Integer getIdClass(String name)  {
		return ddm.getMetaData().getIdClass(name);
	}

	public Integer getIdProperty(String nameProp)  {
		//System.err.println("\n DEBUG ddm.getIdProperty prop:"+nameProp);
		//notar que una propiedad excluida retorna null
		 return ddm.getMetaData().getIdProp(nameProp);
	 }

	 public String getClassName(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException {								
		 return ddm.getClassName(id);
	 }

	 public MetaData getMetaData() {
		 return ddm.getMetaData();
	 }								

	 public Integer getClassOf(int ido) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		 return ddm.getClassOf(ido);
	 }


	 public boolean isSpecialized(String  nameclase,  String namePosSuperior) throws NotFoundException, IncoherenceInMotorException  {
		 if(nameclase.equals(namePosSuperior)) return true;
		 return ddm.isSpecialized(nameclase, namePosSuperior);
	 }

	 public boolean isSpecialized(int idto, int idtoPosSuperior) throws  IncoherenceInMotorException  {
		 if(idtoPosSuperior==idto) return true;
		 return ddm.isSpecialized(idto, idtoPosSuperior);
	 }

	 public void showMessage(String message) {
		 ddm.showMessage(message);
	 }
	 
	 public Boolean showQuestion(String message) {
		 return ddm.showQuestion(message,true);
	 }
	 
	 public Boolean showQuestion(String message, boolean initialSelectionIsYes) {
		 return ddm.showQuestion(message,initialSelectionIsYes);
	 }

	 public boolean checkWithModel(int idto,int idprop){
		 if( ddm.hasProperty(idto, idprop)){
			 return true;
		 }else{
			 System.err.println("\n\n\n ----------- WARNING: idprop:"+idprop+" no es de la clase: idto:"+idto);
			 Auxiliar.printCurrentStackTrace();
			 int a=1/0;
			 return false;

		 }
	 }

	 public boolean checkWithModel(int idto,String nameProp){
		 boolean result=false;
		 Integer idprop=this.getIdProperty(nameProp);
		 if(idprop!=null){
			 result=ddm.hasProperty(idto, idprop);
		 }else{
			 System.err.println("\n\n\n ----------- WARNING: nameProp:"+nameProp+" no esta en este modelo");
			 Auxiliar.printCurrentStackTrace();	
			 int a=1/0;
		 }
		 return result;
	 }
	 

	 public void ruleLiquidacion(DataModelAdapter ddm,Domain domLiquidacion,ObjectValue proveedor) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		   
		  //mapa producto-->vector(precio,cantidad)
		   HashMap<ObjectValue,Vector<Double>> precioCantidadXproducto=new HashMap <ObjectValue,Vector<Double>> ();
		   int idtostock=ddm.getIdClass("STOCK");
		   Domain  domFilterstock= ddm.creaFilter(idtostock);
		   
		    //nuevo: si se indica una tienda(origen), entonces se filtra los art�culos de solo de ese almac�n
		   Value origen=ddm.getValue(domLiquidacion,"origen");
		   if(origen!=null){
			    ddm.setValue(domFilterstock,"almac�n_stock",origen);
			    ddm.printRule(".....info: se ha fijado un origen para la liquidaci�n, solo se tendran en cuenta productos que est�n en ese almacen idalmacen="+origen);
		   }
		   int idofilterPrecioscompra=ddm.getFilterWithPath(domFilterstock.getIdo(), domFilterstock.getIdto(), "producto.precios_compra");
		   //le fijamos el proveedor
		   ddm.setValue(idofilterPrecioscompra,ddm.getClassOf(idofilterPrecioscompra), "proveedor",proveedor);
		   instance insstock=ddm.getTreeObject(domFilterstock.getIdo());
			
			ArrayList<ResultQuery>  stocksValues=ddm.serverGetValuesWhichSatisfaceFilter(insstock, "cantidad#producto#almac�n_stock");
			ddm.printRule("\n... NUMERO DE STOCKS ENCONTRADOS: "+stocksValues.size());
			
			 String mensajeErrorStockNeg="";		
			for (int i=0;i<stocksValues.size();i++){
					ddm.printRule("\n\n---- DEBUG STOCK ENCONTRADO NUMERO "+i+"  : "+stocksValues.get(i));
				    ObjectValue producto=(ObjectValue)stocksValues.get(i).getValue("producto");
				    Value cantidad=stocksValues.get(i).getValue("cantidad");
				  // System.err.println("\n  producto="+producto+"  cantidad="+cantidad);
				   Double dcantidad=((DoubleValue)cantidad).getNumericValue();
				   if(dcantidad<=0){
					    ObjectValue almacen=(ObjectValue)stocksValues.get(i).getValue("almac�n_stock");
					    String rdnalmacen=ddm.getValue(almacen.getValue(),almacen.getValueCls(),Constants.PROP_RDN).getValue_s();
					    String rdnProducto=ddm.getValue(producto.getValue(),producto.getValueCls(),Constants.PROP_RDN).getValue_s();
					    ddm.printRule(".... en el almacen "+rdnalmacen+" hay stock negativo : "+dcantidad+" del producto "+rdnProducto+"  no se tiene en cuenta en la liquidaci�n");
					   mensajeErrorStockNeg+="\n No se ha a�adido a la liquidaci�n el producto "+rdnProducto+" en el almacen "+rdnalmacen+" porque tiene stock  ("+dcantidad+")";
				   }
				   else{
					   if(precioCantidadXproducto.get(producto)!=null){
						   //System.err.println("\n  ya existe en el mapa ese producto");
						   //sumamos a la cantidad anterior (guardada en el vector la cantidad del stock actual
						  Double nuevacantidad=precioCantidadXproducto.get(producto).get(1)+dcantidad;
						//  System.err.println("\n  calculamos la nueva cantidad="+nuevacantidad+"  y la indicamos en elmapa");
						  precioCantidadXproducto.get(producto).set(1, nuevacantidad);
					   }
					   else{
						  // System.err.println("\n  A�N NO existe en el mapa ese producto");
						   Vector<Double> precioCantidad= new Vector<Double>();
						   ObjectValue obPreciosCompra=(ObjectValue)ddm.getValue(producto.getValue(), producto.getValueCls(), "precios_compra");
						   DataValue dvprecioCompra=(DataValue)ddm.getValue(obPreciosCompra.getValue(),obPreciosCompra.getValueCls(),"precio");
						   Double precio=dvprecioCompra.getNumericValue();

						if( precio!=null){
						   precioCantidad.add(0,precio);
						   precioCantidad.add(1,dcantidad);	
						     precioCantidadXproducto.put(producto, precioCantidad);
						}else{
							String codigoproducto=ddm.getValue(producto.getValue(),producto.getValueCls(),"rdn").getValue_s();
							ddm.printRule("WARNING: no se ha encontrado precio para el producto idproducto="+producto+" codigo producto="+codigoproducto+" cantidad="+cantidad+" no se tendra en cuenta en la liquidacion");
						}
				   }
			 }
		   }
		   if(!mensajeErrorStockNeg.isEmpty()){
			    ddm.showMessage(mensajeErrorStockNeg);
		   }
		 //  System.err.println("\n ruleLiquidacion: MAPA CONSTRUDIDO =\n"+precioCantidadXproducto);
		   Iterator<ObjectValue> it=precioCantidadXproducto.keySet().iterator();
		   while(it.hasNext()){
			   ObjectValue producto=it.next();
			   Domain idPRODUCTOYPRECIOS=ddm.creaIndividualOfClass("L�NEA_LIQUIDACI�N_CON_PVP");
			   Double precio=precioCantidadXproducto.get(producto).get(0);
			   Double cantidad=precioCantidadXproducto.get(producto).get(1);
			   ddm.setValue(idPRODUCTOYPRECIOS,"precio_antiguo",new DoubleValue(Auxiliar.redondea(precio,2)));
			   ddm.setValue(idPRODUCTOYPRECIOS,"cantidad",new DoubleValue(cantidad));
			   ddm.setValue(idPRODUCTOYPRECIOS,"producto",producto);
			   ddm.addValue(domLiquidacion, "productos_y_precios",new ObjectValue(idPRODUCTOYPRECIOS));
		   }  
	 }
	 
	 /**
	  * SetValue sin bloquear en base de datos
	  */
	 public void setValueNotLock(int ido,int idto,int idProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 Session actualSession=SessionController.getInstance().getActualSession(ddm);
		 boolean oldLockObjects=actualSession.isLockObjects();
		 actualSession.setLockObjects(false);
		 try{
			 setValue(ido, idto, idProp, val);
		 }finally{
			 actualSession.setLockObjects(oldLockObjects);
		 }
	 }
	 
		public void  delValueNotLock(int ido,int idto,int idProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
			 Session actualSession=SessionController.getInstance().getActualSession(ddm);
			 boolean oldLockObjects=actualSession.isLockObjects();
			 actualSession.setLockObjects(false);
			 try{
				 this.delValue(ido, idto, idProp, val);
			 }finally{
				 actualSession.setLockObjects(oldLockObjects);
			 }
		 }
		
	 
	 
	 public void setValueNotLock(int ido,int idto,String nameProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 this.setValueNotLock(ido, idto, this.getIdProperty(nameProp), val);
	 }
	 

	 /**
	  * AddValue sin bloquear en base de datos
	  */
	 public void addValueNotLock(int ido,int idto,int idProp,Value val) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, ParseException, SQLException, NamingException, JDOMException{
		 Session actualSession=SessionController.getInstance().getActualSession(ddm);
		 boolean oldLockObjects=actualSession.isLockObjects();
		 actualSession.setLockObjects(false);
		 try{
			 addValue(ido, idto, idProp, val);
		 }finally{
			 actualSession.setLockObjects(oldLockObjects);
		 }
	 }
	  
	 public void questionRequest(String rdn,ArrayList<IndividualValues> dataList,HashMap<String,String> alias) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		//Auxiliar.printCurrentStackTrace();
		QuestionRequest request=new QuestionRequest(rdn,dataList,ddm);
		ddm.getRuleEngine().insertFact(request);
		ddm.getQuestionListener().request(request.getID(), request.getRDN(), request.getINDIVIDUALSDATA(), alias, ddm);
	}
	 
	 public void questionRequest(String rdn,IndividualValues data,HashMap<String,String> alias) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException{
		//Auxiliar.printCurrentStackTrace();
		ArrayList<IndividualValues> dataList=new ArrayList<IndividualValues>();
		dataList.add(data);
		QuestionRequest request=new QuestionRequest(rdn,dataList,ddm);
		ddm.getRuleEngine().insertFact(request);
		ddm.getQuestionListener().request(request.getID(), request.getRDN(), request.getINDIVIDUALSDATA(), alias, ddm);
	}
	 
	 
	 
	 
		
	 /**
	  * Hace una reserva de una cantidad para un ido, idProp en base de datos
	  * @param reservationList
	  * @return mapa de DomainProp e integer, que se corresponde con DomainProp-->ido,idto,idProp e Integer-->Diferencia entre el stock conocido por nosotros(Reservation.available) y el stock en base de datos menos reservas de otros usuarios
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws ApplicationException 
	 * @throws CommunicationException 
	 * @throws InstanceLockedException 
	 * @throws DataErrorException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	  */
	 public HashMap<DomainProp,Double> reserve(LinkedHashMap<String,Reservation> reservationList) throws SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException{
		 Session session =SessionController.getInstance().getActualSession(ddm);
		 return session.sendReservations(reservationList);
	 }
	 
	 
	 public void executeAction(String nameAction,ArrayList<Domain> sources) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		 int idoAction=this.getDDM().createPrototype(this.getIdClass(nameAction), Constants.LEVEL_FILTER, null,Constants.USER_SYSTEM, this.getIdClass(nameAction), false);
		 int idtoAction=ddm.getClassOf(idoAction);
		 for(int i=0;i<sources.size();i++){
			 this.addValue(new Domain(idoAction,idtoAction),"sourceClass",new ObjectValue(sources.get(i)));
		 }
		 int idoInformado=ddm.getID_OF(Constants.INDIVIDUAL_INFORMADO);
		 int idtoInformado=ddm.getClassOf(idoInformado);
		 this.setValue(idoAction,ddm.getClassOf(idoAction),Constants.IdPROP_ESTADOREALIZACION,new ObjectValue(idoInformado,idtoInformado));
	 }
	 
	 public void setSourceBatch(ArrayList<HashMap<Integer,Integer>> sources){
		 for(IBatchListener batchListener:ddm.getBatchListeners()){
			 batchListener.setSources(sources);
		 }
	 }
	 
	 public void showNoticeMessage(String message,boolean whenSaved){
		 if(!whenSaved){
			 SessionController.getInstance().getActualSession(ddm).notifyNoticeMessage(message);
		 }else{
			 SessionController.getInstance().getActualSession(ddm).addNoticeMessage(message);
		 }
	 }
	 
	 public long getServerDate() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NoSuchFieldException{
		 return ddm.getServer().serverGetCurrentTimeMillis()/Constants.TIMEMILLIS;
	 }
	 
	 public void consumirEventoCambio(int ido,String propName) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{		 
		 Integer idProp=getIdProperty(propName);
		 if(idProp==null) return; //puede estar excluida
		 consumirEventoCambio(ido, idProp);		 
	 }
	 
	 public void consumirEventoCambio(int ido,int idProp) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		 consumirEventoCambio(ido, idProp, null, null);
	 }
	 
	 public void consumirEventoCambio(int ido,String propName,Integer value,Integer valueCls) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		 consumirEventoCambio(ido, getIdProperty(propName), value, valueCls);
	 }
	 
	 public void consumirEventoCambio(int ido,int idProp,Integer value,Integer valueCls) throws NotFoundException, IncoherenceInMotorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		 ddm.getRuleEngine().consumirEventoCambio(ido,idProp,value,valueCls);
	 }
	 
	 public boolean isSubscriptionStandard(){
		 String subscription=ddm.getServer().getSubscription();
		 return Auxiliar.equals(Constants.BASIC_SUBSCRIPTION_6_MONTHS,subscription) || Auxiliar.equals(Constants.BASIC_SUBSCRIPTION_12_MONTHS,subscription); 
	 }
	 
	 public void requestEmail(int idoNotification, int idoTarget, int idtoTarget, String notificationType) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException {
	 //public void requestEmail(int ido, int idto, Integer idtoReport, String email, String subject, String body, int idoMiEmpresa, int idoDestinatario) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, NotFoundException, ParseException, SQLException, NamingException, JDOMException {
		 //EmailRequest emailRequest=new EmailRequest(ido, idto, idtoReport, email, subject, body, idoMiEmpresa, idoDestinatario, ddm);
		 EmailRequest emailRequest=new EmailRequest(idoNotification, idoTarget, idtoTarget, notificationType, ddm);
		 SessionController.getInstance().getActualSession(ddm).addEmailRequest(emailRequest);
	}
	 
	public void requestExecutionAction(String actionName, String rdn, ArrayList<Domain> sources, HashMap<String,Object> mapParamValue) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		int idtoUserTask=ddm.getIdClass(actionName);
		ExecuteActionRequest executionActionRequest=new ExecuteActionRequest(idtoUserTask,rdn,sources,mapParamValue);
		 SessionController.getInstance().getActualSession(ddm).requestExecuteAction(executionActionRequest);
	}
}
