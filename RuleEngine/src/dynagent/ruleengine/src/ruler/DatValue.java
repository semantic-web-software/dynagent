package dynagent.ruleengine.src.ruler;


import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;


import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.knowledge.FactInstance;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.src.sessions.SessionController;


public class DatValue extends Fact implements IndividualValue{
		
	private String firstValue;
	
	public DatValue(Integer idto, Integer ido, Integer prop, String value, Integer valueCls, String rangename,Double qmin, Double qmax, String op, String classname, boolean existeBD, String systemValue, boolean appliedSystemValue, String destinationSystem, IKnowledgeBaseInfo ik){
		super(idto, ido, prop, value, valueCls, rangename,qmin, qmax, null, classname, existeBD, systemValue, appliedSystemValue, destinationSystem,ik);//OP siempre es nulo en este caso
		firstValue=KnowledgeAdapter.getVALUE_s(this);

	}
	
	public Boolean getBOOLEANVALOR() {
		Boolean result=null;
		if(this.getVALUE_s()!=null&&this.getVALUE_s().equals(Constants.BOOLEAN_TRUE)){
			result=true;
		}
		else if(this.getVALUE_s()!=null&&this.getVALUE_s().equals(Constants.BOOLEAN_FALSE)){
			result=false;
		}
		return result;
	}

	public void setBOOLEANVALUE(boolean booleanvalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		setQ(booleanvalue?new Double(1):new Double(0), booleanvalue?new Double(1):new Double(0));
	}

	public void setDATETIMEVALUE(Date datetimevalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(datetimevalue);
		setQ(new Double(cal.getTimeInMillis()),new Double(cal.getTimeInMillis()));
	}


	public void setDATEVALUE(Date datevalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(datevalue);
		super.setQ(new Double(cal.getTimeInMillis()),new Double(cal.getTimeInMillis()));
	}

	public Double getDOUBLEVALUE() {
		return getQMAX();
	}
	
	public Double getPREVIODOUBLEVALUE() {
		if(this.getPREVALOR()!=null){
			return new Double (this.getPREVALOR());
		}else return null;
	}


	public void setDOUBLEVALUE(Double doublevalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		setQ(doublevalue,doublevalue);
		
	}


	public Integer getINTVALUE() {
		return getQMAX()==null?null:getQMAX().intValue();
	}
	
	public Double getSTRINGTODOUBLEVALUE() {
		if(Auxiliar.hasDoubleValue(this.getVALOR())){
			return Double.parseDouble(this.getVALOR());
		}else return null;
		
	}


	public void setINTVALUE(Integer intvalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		setQ(new Double(intvalue),new Double(intvalue));

	}


	public String getSTRINGVALUE() {
		return getVALUE();
	}


	public void setSTRINGVALUE(String stringvalue) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		super.setVALUE(stringvalue);
	}

	public Long getTIMEMILLIS() {
		return getQMAX()==null?null:getQMAX().longValue()*Constants.TIMEMILLIS;
	}
	
	public Long getTIMESECONDS() {
		return getQMAX()==null?null:getQMAX().longValue();
	}
	
	public Date getDATE() {
		Date date=null;
		if(this.getVALUECLS()!=null&&(this.getVALUECLS().intValue()==Constants.IDTO_DATE||this.getVALUECLS().intValue()==Constants.IDTO_DATETIME||this.getVALUECLS().intValue()==Constants.IDTO_TIME)){
			if(getQMAX()!=null)
				date= new Date(getQMAX().longValue()*Constants.TIMEMILLIS);
		}
		return date;
		
	}

	public void setTIME(Long time) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(Auxiliar.equals(time==null?null:time.doubleValue(), getQMIN())) return;
		
		super.setQ(time==null?null:time.doubleValue(),time==null?null:time.doubleValue());
	}


	public String getVALOR() {
		return this.getVALUE_s();	
	}


	public void setVALOR(String valor) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		if(Auxiliar.equals(this.getVALUE_s(), valor)) return;
		
		switch (super.getVALUECLS()) {
		case Constants.IDTO_INT:
			this.setINTVALUE(valor==null?null:new Integer(valor).intValue());
			break;
		case Constants.IDTO_DOUBLE:
			this.setDOUBLEVALUE(valor==null?null:new Double(valor));
			break;
		case Constants.IDTO_STRING:
			this.setSTRINGVALUE(valor);
			break;
		case Constants.IDTO_BOOLEAN:
			this.setBOOLEANVALUE(valor.equals("true")?true:false);
			break;
		case Constants.IDTO_TIME:
			this.setTIME(valor==null?null:new Long(valor)*Constants.TIMEMILLIS);
			break;
		case Constants.IDTO_DATE:
			this.setDATEVALUE(valor==null?null:new Date(new Long(valor)*Constants.TIMEMILLIS));//TODO�QUITAR LOS MINUTOS,SEGUNDOS,HORAS?
			break;
		case Constants.IDTO_DATETIME:
			this.setDATETIMEVALUE(valor==null?null:new Date(new Long(valor)*Constants.TIMEMILLIS));
			break;		
		default:
			System.err.println("WARNING: TIPO DE DATOS DESCONOCIDO");
			break;
	}
	}
	//reimplementar los metodos que pueden afectar el disparo de las reglas. en vez de llamar a la del super, la llamada 
	// es hacia el objeto especializado y luego el mismo objeto espec. llamaria al super.
	public void setVALUE(String val) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		if(Auxiliar.equals(this.getVALUE_s(), val)) return;
		//this.setVALOR(val);
		super.setVALUE(val);
	}

	
	public void setQMAX(Double qmax) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		//this.setVALOR(qmax==null?null:qmax.toString());
		if(Auxiliar.equals(qmax, getQMAX())) return;
		super.setQMAX(qmax);
	}
	public void setQMIN(Double qmin) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		//this.setVALOR(qmin==null?null:qmin.toString());
		if(Auxiliar.equals(qmin, getQMIN())) return;
		super.setQMIN(qmin);
	}
	public void setQ(Double qmin, Double qmax) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		if(Auxiliar.equals(qmin, getQMIN())&&Auxiliar.equals(qmax, getQMAX())) return;
		
		if(Auxiliar.equals(qmin, qmax) || Auxiliar.equals(getLEVEL(),Constants.LEVEL_FILTER)/*Si es filtro permitimos que sean distintos para permitir buscar con mayor-menor*/)
		{
			if(isIncremental()){//Si hemos hecho un setValue sobre un fact incremental tenemos que borrar las contribuciones que tuviera ya que se trata de un set absoluto
				Contribution antig=getContribution().clone();
				getContribution().getContribution().clear();
				getContribution().setValue(0);
				pcs.firePropertyChange("contributionValues", antig, getContribution());
			}
			//this.setVALOR(qmin==null?null:qmax.toString());
			super.setQ(qmin,qmax);
		}
		else
			throw new IncompatibleValueException("UN DATA VALUE NO PUEDE TENER QMAX DISTINTO DE QMIN");
		
	}
	
	
	public String toString()
	{
		String stringfact = null;
		try {
			stringfact = "(DatValue (LEVEL "+this.getLEVEL()+")(CLASSNAME "+getCLASSNAME()+")(IDO "+getIDO()+")(PROPNAME= "+this.getPROPNAME()+")(DESTINATIONSYSTEM= "+this.getDestinationSystem()+")(VALOR "+getVALOR()+")(VALUE "+getVALUE()+")(QMAX "+getQMAX()+")(QMIN"+getQMIN()+")";
			
			
			//SOLO PARA DEBUG
			
			stringfact += "(INTVALUE=" + this.getINTVALUE()+"))";
//	stringfact += "(isIncremental=" + this.isIncremental()+")";
//			stringfact += "(INITIALVALOR=" + this.getINITIALVALOR()+")";
//			stringfact += "(HASCHANGED=" + this.hasCHANGED()+")";
//			stringfact += "\n(prevalor="+this.getPREVALOR()+")";
//			stringfact += "\n(existiaBD="+super.getExistia_BD()+")";
//			stringfact += "(deleted="+ this.getDeleted() +")";
//			stringfact += "(systemValue="+this.getSystemValue()+")";
//			stringfact += "(sesiones="+ this.sessionValues +")";
//			stringfact += "\n(valoresAnteriores="+super.getValoresAnteriores()+")";
			
			
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stringfact+= "(BOOLENAVALUE "+ this.getBOOLEANVALOR()+")(STRINGVALUE "+ this.getSTRINGVALUE()+")(DATE "+ this.getDATE()+")"+"(TIMESECONDS "+ this.getTIMESECONDS()+")"+ "(TIMEMILLIS "+ this.getTIMEMILLIS()+")"+")";
		return stringfact;
	}
	
	public boolean getEXISTIABD(){
		return super.getExistia_BD();
	}
	
	public Double getINCREMENTODOUBLEVALOR(){
		Double incre=null;
		if(this.getVALUECLS().intValue()==Constants.IDTO_DOUBLE){
			if(this.getDOUBLEVALUE()!=null){
				if(this.getPREVALOR()!=null)
				{
					incre=((Double)(this.getDOUBLEVALUE()-Double.valueOf(this.getPREVALOR()))).doubleValue();
				}else{
					incre=this.getDOUBLEVALUE().doubleValue();
				}
			}
		}
		return incre;
	}
	
	
	
	/**
	 * Sirve para añadir un valor que es contribucion de otro. Esta implementado teniendo una cola en la que si se tienen dos valores retorna el valor que deberia almacenarse como Q en el fact.
	 * Este metodo no se encarga de actualizar Q. En la llamada a este metodo se decide si actualizarlo o no. Usando este metodo conseguimos que si se llama dos veces a este metodo con el mismo
	 * valor no haga nada, ya que el valor que se utiliza es valorActual+(valorActualLlamada-valorAnteriorLlamada).
	 * 
	 * @param value: Un numero que puede ser double o entero
	 * @return El valor que habria que añadirle a este fact en setQ. Retorna null si no hay que actualizar el valor al no haber dos elementos en la cola.
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws DataErrorException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ApplicationException 
	 */
	public boolean addContributionValue(HashMap<String,Number> mapIdoInitialValue,Number value,Integer nDigRedondeo) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{// throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
//		/*Funcionamiento:
//		 * Initial		Current		IncrementoValue		Value
//		 * 	6				6				0				6
//		 * 	6				8				2(8-6)			8
//		 * 	NULL			NULL			-8(0-8)			0
//		 * */
//		//System.err.println("AddContribution: f:"+this+" mapIdoInitialValue:"+mapIdoInitialValue+" value:"+value);
//		//Guardamos los valores iniciales 
//		Contribution antig = getContribution().clone();
//		
//		boolean cambiadoAntig = hasCHANGED();
//		boolean initialCambiadoAntig = initialValuesChanged();
//		
//		FactInstance valoresAnterAntig=null;
//		Object qmaxAntig = this.getQMAX();
//		Object qminAntig = this.getQMIN();
//		
//		boolean changedValues=false;
////		TODO: comprobar con Auxiliar.equals si los nuevos valores son iguales a los antiguos, en cuyo caso no hacer nada
//		if( protegerValorPrev==false )
//		{
//			valoresAnterAntig =getValoresAnteriores()==null?null:getValoresAnteriores().clone();
//			this.setValoresAnteriores(new FactInstance(this.getIDTO(),	this.getIDO(),this.getPROP(),
//									this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME()));
//		}
//		
//		int session = SessionController.getInstance().getActualSession(ik).getID();
//		// primero mirar si hay una session con el mismo nombre.
//		boolean enc = false;
//		Contribution contribution;
//		for (int i = 0; i < sessionValues.size() && !enc; i++) {
//			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
//				enc = true;
//				contribution=sessionValues.get(i).getContribution().clone();
//				ArrayList<Number> contributionVal=contribution.getContributionValues();
//				
//				double initialValue/*vb*/=0;
//				if(!contributionVal.isEmpty())
//					initialValue=contribution.getContributionValues().get(0).doubleValue();
//				
//				if(mapIdoInitialValue!=null){
//					Iterator<String> itr=mapIdoInitialValue.keySet().iterator();
//					while(itr.hasNext()){
//						String ido=itr.next();
//						if(!contribution.getIdosContribution().contains(ido)){
//							initialValue+=mapIdoInitialValue.get(ido).doubleValue();
//							contribution.getIdosContribution().add(ido);
//						}
//					}
//				}
//				if(contributionVal.isEmpty())
//					contributionVal.add(initialValue);
//				else contributionVal.set(0,initialValue);
//				contributionVal.add(value);
//				double currentValue=getDOUBLEVALUE()!=null?getDOUBLEVALUE():0;
//				double newValue=(currentValue+(value.doubleValue()-contributionVal.remove(0).doubleValue()));
//				if(nDigRedondeo!=null)
//					newValue=Auxiliar.redondea(newValue, nDigRedondeo.intValue());
//				
//				if(!Auxiliar.equals(newValue, getDOUBLEVALUE())){
//					sessionValues.get(i).setContribution(contribution);
//					sessionValues.get(i).setQMIN(newValue);
//					sessionValues.get(i).setQMAX(newValue);
//					this.setLastSession(session);
//					changedValues=true;
//				}
//			}
//		}
//		if (!enc) // en caso de no tener una sessionValue para la sessionActual
//		{
//			contribution=getContribution().clone();
//			ArrayList<Number> contributionVal=contribution.getContributionValues();
//			
//			double initialValue=0;
//			if(!contributionVal.isEmpty())
//				initialValue=contribution.getContributionValues().get(0).doubleValue();
//			if(mapIdoInitialValue!=null){
//				Iterator<String> itr=mapIdoInitialValue.keySet().iterator();
//				while(itr.hasNext()){
//					String ido=itr.next();
//					if(!contribution.getIdosContribution().contains(ido)){
//						initialValue+=mapIdoInitialValue.get(ido).doubleValue();
//						contribution.getIdosContribution().add(ido);
//					}
//				}
//			}
//			if(contributionVal.isEmpty())
//				contributionVal.add(initialValue);
//			else contributionVal.set(0,initialValue);
//			contributionVal.add(value);
//			double newValue=(getDOUBLEVALUE()+(value.doubleValue()-contributionVal.remove(0).doubleValue()));
//			if(nDigRedondeo!=null)
//				newValue=Auxiliar.redondea(newValue, nDigRedondeo.intValue());
//			
//			if(!Auxiliar.equals(newValue, getDOUBLEVALUE())){
//				SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), newValue, newValue, getOP(), getSystemValue(), isAppliedSystemValue(), contribution, getRANGENAME(), getInitialQ());
//				this.setLastSession(session);
//				s.setSession(session);
//				this.sessionValues.add(s);
//				oldValuesForNotify=getValoresAnteriores().clone();
//				changedValues=true;
//			}
//		}
//		//if(getSystemValue()==null)
//		//	System.err.println("systemValue es null:"+this);
//		if(changedValues){
//			SessionController.getInstance().getActualSession(ik).addSessionable(this);
//			
//			pcs.firePropertyChange("contributionValues", antig, getContribution());
//			pcs.firePropertyChange("QMAX", qmaxAntig, getQMAX());
//			pcs.firePropertyChange("QMIN", qminAntig, getQMIN());
//			if( protegerValorPrev==false )
//				pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,getValoresAnteriores());
//			pcs.firePropertyChange("hasCHANGED",cambiadoAntig,hasCHANGED());
//			pcs.firePropertyChange("initialValuesChanged",initialCambiadoAntig,initialValuesChanged());
//			
//			return true;
//		}else{
//			if( protegerValorPrev==false )
//			{
//				this.setValoresAnteriores(valoresAnterAntig);
//			}
//			//System.err.println("WARNING: addContributionValue no actualiza nada en "+this.getIDO()+" ya que el valor a asignar es igual al ya almacenado:"+getDOUBLEVALUE());
//			return false;
//		}
		return false;
	}
	
	@Override
	public Contribution getContribution(){
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getContribution();
			}
		}
		//TODO: lanzar una excepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getContribution();
	}


	@Override
	public FactInstance toFactInstance() {
		FactInstance f=super.toFactInstance();
		f.setIncremental(isIncremental());
		return f;
	}
	
	public boolean isIncremental(){
		return !getContribution().getContribution().isEmpty();
	}


	
	public DataValue getDATAVALUE()  {
		DataValue dataVal=null;
		switch (this.getVALUECLS()) {
		case Constants.IDTO_INT:
			if(this.getQMIN()!=null&&this.getQMAX()!=null)
				dataVal=new IntValue(this.getQMIN().intValue(),this.getQMAX().intValue());
			break;
		case Constants.IDTO_DOUBLE:
			if(this.getQMIN()!=null&&this.getQMAX()!=null)
				dataVal=new DoubleValue(this.getQMIN(),this.getQMAX());
			break;
		case Constants.IDTO_STRING:
			if(this.getVALUE()!=null)
				dataVal=new StringValue(this.getVALUE());
			break;
		case Constants.IDTO_MEMO:
			if(this.getVALUE()!=null)
				dataVal=new StringValue(this.getVALUE());
			break;
						
		case Constants.IDTO_BOOLEAN:
			if(this.getQMAX()!=null)
				dataVal=new BooleanValue(this.getQMAX()==1?true:false);			
			break;
		case Constants.IDTO_TIME:
			if(this.getQMAX()!=null)
				dataVal=new TimeValue(this.getQMAX()==null?null:this.getQMAX().longValue());
			break;
		case Constants.IDTO_DATE:
			if(this.getQMAX()!=null)
				dataVal=new TimeValue(this.getQMAX()==null?null:this.getQMAX().longValue());
			break;
		case Constants.IDTO_DATETIME:
			if(this.getQMAX()!=null)
				dataVal=new TimeValue(this.getQMAX()==null?null:this.getQMAX().longValue());
			break;		
		case Constants.IDTO_FILE:
			if(this.getVALUE()!=null)
				dataVal=new StringValue(this.getVALUE());
			break;
		case Constants.IDTO_IMAGE:
			break;
		default:
			System.err.println("WARNING: TIPO DE DATOS DESCONOCIDO: de la clase"+super.getCLASSNAME()+ " y property  = "+this.getPROP()+" rango "+this.getVALUECLS()+"\n"+this.toString());
			Auxiliar.printCurrentStackTrace();
			break;
		}
		return dataVal;//SI NO TIENE VALOR SE DEVUELVE NULL POR MEMORIA---->SOLO SE DEBEN CASTEAR DATAVALUE A DATAVALUE CONCRETOS EN REGLAS CON VALORES REALES NO NULO
		
		
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		DatValue datValue=new DatValue(getIDTO(),getIDO(),getPROP(),getVALUE(),getVALUECLS(),getRANGENAME(),getQMIN(),getQMAX(),getOP(),getCLASSNAME(),getExistia_BD(),getSystemValue(),isAppliedSystemValue(),getDestinationSystem(),ik);
		datValue.setContribution(getContribution());
		return datValue;
	}

	public String toStringAmpliado() {
		return this.toString()+"\n (OP=="+this.getOP();
	
	}
	
	/**
	 * Se encarga de asignar initialQ al QMIN y QMAX de valores iniciales y recalcula el valor actual de QMIN y QMAX. Sirve para aplicar las reservas a los contributivos.
	 * @param initialQ
	 * @throws ApplicationException
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 * @throws DataErrorException
	 * @throws NumberFormatException
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws OperationNotPermitedException
	 * @throws ParseException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 */
	public void addInitialQ(double initialQ) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		switch (super.getVALUECLS()) {
			//QMIN y QMAX serian similar a sesionables ya que initialQ si que es sesionable y se utiliza luego en el rollback para modificar el valor de QMIN y QMAX 
			case Constants.IDTO_INT:
			case Constants.IDTO_DOUBLE:
				Double QAux=this.QMIN.doubleValue();
				this.QMIN=initialQ;
				this.QMAX=initialQ;
				Double q=getDOUBLEVALUE()+initialQ-QAux;//Recalculamos el valor actual ya que se trataria de un contributivo
				super.setQ(q,q);//Hacemos super.setQ ya que no queremos que siendo contributivo se borre la lista de contribuciones como ocurriria si llamamos al this.setQ
				break;
			default:
				System.err.println("ERROR: Intento de offset sobre un tipo de fact incorrecto");
				return;
		}
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setInitialQ(initialQ);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), initialQ);
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
	}
	
	public Double getINITIALDOUBLE() {
		String init=this.getINITIALVALOR();
        if( init!=null){
        	return new Double(init);
        }else
        	return null;
	}

	@Override
	public String getFIRSTVALUE() {
		return firstValue;
	}

	public Double getFIRSTDOUBLE() {
		return new Double(firstValue);
	}
	
	@Override
	public Double getInitialQ() {
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getInitialQ();
			}
		}
		//TODO: lanzar una excepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getInitialQ();
	}
	
	public boolean addContributionValue(String key,double increment,Integer nDigRedondeo) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		Double value=getINITIALDOUBLE();
		//System.err.println("VALUE:"+value);
		if(value==null){
			if(getFIRSTVALUE()!=null){//Entraria cuando el fact es de un prototipo y tiene valor
				value=Double.valueOf(getFIRSTVALUE());
				//System.err.println("FISTVALUE:"+value);
			}else{
				value=0.0;
			}
		}
		
		Contribution currentContribution=getContribution();
		if(currentContribution.getContribution().containsKey(key)){
			value-=currentContribution.getContribution().get(key);
		}
		value+=increment;
		
		if(nDigRedondeo!=null)
			value=Auxiliar.redondea(value, nDigRedondeo.intValue());
		
		if(Auxiliar.equals(value, getDOUBLEVALUE())){
			//System.err.println("WARNING: DatValue.setIncrement no hari nada ya que el valor calculado ya existe:"+value);
			return false;
		}else{
			//Guardamos los valores iniciales 
			Contribution antig = getContribution().clone();
			
			boolean cambiadoAntig = hasCHANGED();
			boolean initialCambiadoAntig = initialValuesChanged();
			
			FactInstance valoresAnterAntig=null;
			Object qmaxAntig = this.getQMAX();
			Object qminAntig = this.getQMIN();
			
			if( protegerValorPrev==false )
			{
				valoresAnterAntig =getValoresAnteriores()==null?null:getValoresAnteriores().clone();
				this.setValoresAnteriores(new FactInstance(this.getIDTO(),	this.getIDO(),this.getPROP(),
										this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME()));
			}
			
			int session = SessionController.getInstance().getActualSession(ik).getID();
			// primero mirar si hay una session con el mismo nombre.
			boolean enc = false;
			Contribution contribution;
			for (int i = 0; i < sessionValues.size() && !enc; i++) {
				if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
					enc = true;
					contribution=sessionValues.get(i).getContribution().clone();
					contribution.setValue(value);
					contribution.getContribution().put(key, value);
					
					sessionValues.get(i).setContribution(contribution);
					sessionValues.get(i).setQMIN(value);
					sessionValues.get(i).setQMAX(value);
					this.setLastSession(session);
				}
			}
			if (!enc) // en caso de no tener una sessionValue para la sessionActual
			{
				contribution=getContribution().clone();
				contribution.setValue(value);
				contribution.getContribution().put(key, value);
				
				SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), value, value, getOP(), getSystemValue(), isAppliedSystemValue(), contribution, getRANGENAME(), getInitialQ());
				this.setLastSession(session);
				s.setSession(session);
				this.sessionValues.add(s);
				oldValuesForNotify=getValoresAnteriores().clone();
			}
			//if(getSystemValue()==null)
			//	System.err.println("systemValue es null:"+this);

			SessionController.getInstance().getActualSession(ik).addSessionable(this);
			
			pcs.firePropertyChange("contributionValues", antig, getContribution());
			pcs.firePropertyChange("QMAX", qmaxAntig, getQMAX());
			pcs.firePropertyChange("QMIN", qminAntig, getQMIN());
			if( protegerValorPrev==false )
				pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,getValoresAnteriores());
			pcs.firePropertyChange("hasCHANGED",cambiadoAntig,hasCHANGED());
			pcs.firePropertyChange("initialValuesChanged",initialCambiadoAntig,initialValuesChanged());
			
			return true;
		}
	}
	
	public boolean setIncrement(double increment,Integer nDigRedondeo) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		Double value=getINITIALDOUBLE();
		//System.err.println("VALUE:"+value);
		if(value==null){
			if(getFIRSTVALUE()!=null){
				//Entraria cuando el fact es de un prototipo y tiene valor
				//Ya no se utiliza, se utilizaba para inicializar al clonar, actuando como si existiera en bd, para evolucion de propiedades dependientes, ahora todo tira de hasCHanged pero fallaría al rectificar factura
				value=Double.valueOf(getFIRSTVALUE());
				//System.err.println("FISTVALUE:"+value);
			}else{
				value=0.0;
			}
		}
		value+=increment;
		
		if(nDigRedondeo!=null)
			value=Auxiliar.redondea(value, nDigRedondeo.intValue());
		
		if(Auxiliar.equals(value, getDOUBLEVALUE()) && isIncremental()/*Necesario para que si el valor incrementado es cero, se envie como incremental y no como absoluto, ya que inicialmente se crea el valor con cero*/){
			//System.err.println("WARNING: DatValue.setIncrement no hari nada ya que el valor calculado ya existe:"+value);
			return false;
		}else{
			
			Contribution antig = getContribution().clone();
			
			boolean cambiadoAntig = hasCHANGED();
			boolean initialCambiadoAntig = initialValuesChanged();
			
			FactInstance valoresAnterAntig=null;
			Object qmaxAntig = this.getQMAX();
			Object qminAntig = this.getQMIN();
			
			if( protegerValorPrev==false )
			{
				valoresAnterAntig =getValoresAnteriores()==null?null:getValoresAnteriores().clone();
				this.setValoresAnteriores(new FactInstance(this.getIDTO(),	this.getIDO(),this.getPROP(),
										this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME()));
			}
			
			//TODO Cuando se quite contribucion cambiar por incremental=true
			Contribution contribution=new Contribution();
			contribution.getContribution().put(null,increment);
			contribution.setValue(increment);			
			
			int session = SessionController.getInstance().getActualSession(ik).getID();
			// primero mirar si hay una session con el mismo nombre.
			boolean enc = false;
			for (int i = 0; i < sessionValues.size() && !enc; i++) {
				if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
					enc = true;
					sessionValues.get(i).setContribution(contribution);
					sessionValues.get(i).setQMIN(value);
					sessionValues.get(i).setQMAX(value);
					this.setLastSession(session);
				}
			}
			if (!enc) // en caso de no tener una sessionValue para la sessionActual
			{
				SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), value, value, getOP(), getSystemValue(), isAppliedSystemValue(), contribution, getRANGENAME(), getInitialQ());
				this.setLastSession(session);
				s.setSession(session);
				this.sessionValues.add(s);
			}
			SessionController.getInstance().getActualSession(ik).addSessionable(this);
			
			pcs.firePropertyChange("contributionValues", antig, getContribution());
			pcs.firePropertyChange("QMAX", qmaxAntig, getQMAX());
			pcs.firePropertyChange("QMIN", qminAntig, getQMIN());
			if( protegerValorPrev==false )
				pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,getValoresAnteriores());
			pcs.firePropertyChange("hasCHANGED",cambiadoAntig,hasCHANGED());
			pcs.firePropertyChange("initialValuesChanged",initialCambiadoAntig,initialValuesChanged());
			return true;
		}
	}

	@Override
	public Value getCVALUE() {
		return this.getDATAVALUE();
	}

	
//	@Override NO PARECE SER NECESARIO. CUANDO EN FACT SE HACE firePropertyChange de PREVALOR PARECE QUE AFECTA TAMBIEN A PREVIODOUBLEVALUE DE ALGUN MODO, QUIZAS PORQUE SU METODO UTILIZA EL VALOR DE PREVIO VALOR
//	public void consumirEventoCambio(){
//		Integer valueCls=this.getVALUECLS();
//		if(Auxiliar.equals(valueCls,Constants.IDTO_INT) || Auxiliar.equals(valueCls,Constants.IDTO_DOUBLE)){
//			Double ant=getPREVIODOUBLEVALUE();
//			super.consumirEventoCambio();
//			pcs.firePropertyChange("PREVIODOUBLEVALUE",ant,getPREVIODOUBLEVALUE());
//		}else{
//			super.consumirEventoCambio(); 
//		}
//	}
}
	
	

