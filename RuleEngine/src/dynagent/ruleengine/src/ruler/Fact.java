/***
 * Fact.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 * @author: Hassan Ali Sleiman - hassansleiman@gmail.com
 * @description: A fact is a copy of instance like a cache
 */

package dynagent.ruleengine.src.ruler;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.drools.FactHandle;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
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
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.knowledge.action;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.SessionValue;
import dynagent.ruleengine.src.sessions.SessionController;

/*
 * Fact represents a cache class
 */

   public abstract class Fact extends SessionValue implements Serializable, Sessionable, IPropertyDef, IPropertyChangeDrools {

	ArrayList<SessionValue> sessionValues = new ArrayList<SessionValue>();
	HashMap<Integer,SessionValue> sessionValuesRemovedPropagation=new HashMap<Integer, SessionValue>();
	HashMap<Integer,SessionValue> sessionValuesChangedPropagation=new HashMap<Integer, SessionValue>();
	
	private int sesion;
	private String CLASSNAME= null;
	private boolean deleted=false;
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private boolean existia_BD;
	private int lastSession = 0;	
	private Integer IDO=null;
	
	private int PROP=0;
	private Integer utask=null;
	//un atributo que sirve para guardar el estado anterior antes de hacer cualquier modificacion en el Fact
	private FactInstance valoresAnteriores = null;	
	private FactHandle factHandle = null;
	//para recordar de que Level es el fact antes de ser borrado (puesto a null)
	private Integer levelCache;
	protected boolean protegerValorPrev=false;//permite fijar un valor actual sin modificar el valor anterior
	
	private String destinationSystem=null;
	
	//private String rdnValue=null;
	
	//Valor anterior en el momento de hacer un cambio, un commit o un rollback. Es utilizado para avisar de cambios sin que se vea afectada por consumirEvento.
	//No es sesionable pero quizas en el futuro nos interese que lo sea ya que al propagarse entre sesiones se queda con el ultimo valor que lo cambio.
	//Si se produce un doble cambio en una misma sesion, si hay algun listener registrado en ella no avisaria bien del segundo cambio. Sin embargo como las sesiones internas
	//son las que hacen los cambios nunca se producira un doble cambio de valor sin hacer antes commit, por lo que no se avisaria mal. Si las reglas hacen un doble setValue
	//tampoco hay problema porque no hay nadie registrado como listener en esa sesion y al propagarse se propagaria correctamente.
	protected FactInstance oldValuesForNotify=null;
	protected IKnowledgeBaseInfo ik;
	private String initialDestinationSytem=null;
	public Fact(Integer idto, Integer ido,  Integer prop,String value, Integer valueCls,  String rangename, Double qmin, Double qmax, String op, String classname,boolean existeBD,String systemValue,boolean appliedSystemValue,String destinationSystem,IKnowledgeBaseInfo ik)
	{
		super(ik,idto,existeBD?value:null, valueCls, existeBD?qmin:null, existeBD?qmax:null, existeBD?op:null, existeBD?systemValue:null, existeBD?appliedSystemValue:false, null, rangename, existeBD?qmin:null);
		this.IDO=ido;
		this.PROP=prop;
		this.CLASSNAME = classname;
		this.PROP=prop;
		this.existia_BD=existeBD;
		this.ik=ik;
		this.sesion = SessionController.getInstance().getActualSession(ik).getID();
		
		//en caso de no dispararse las reglas de replica (ha sucedido) debe garantizarse que el dato se replica aunque sea de mas
		//se supone que en bddrules no se hace clone con lo cual aplicaria lo ultimo
		if(destinationSystem==null||destinationSystem.length()==0) destinationSystem="*";
			
		this.destinationSystem=destinationSystem;
		this.initialDestinationSytem=existeBD?destinationSystem:null;
		this.setLastSession(this.sesion);
		//creamos un sessionValue para la session que acaba de crear a ese Fact
		SessionValue s = new SessionValue(ik,idto,value, valueCls, qmin, qmax, op, systemValue, appliedSystemValue, new Contribution(),rangename,existeBD?qmin:null);
		s.setSession(SessionController.getInstance().getActualSession(ik).getID());
		this.sessionValues.add(s);
		
		RANGENAME=rangename;
		//this.setExistia_BD(existeBD);
		if(existeBD)
			this.valoresAnteriores = new FactInstance(idto,ido,prop,value,valueCls,rangename,qmin,qmax,op,classname);
		else//Si existe en la base de datos, los valores anteriores son los valores iniciales que son los recibidos por el constructor
			this.valoresAnteriores = new FactInstance(idto,ido,prop,null,valueCls,rangename,null,null,null,classname);						
		
		this.valoresAnteriores.setDestinationSystem(destinationSystem);
		
		oldValuesForNotify=valoresAnteriores.clone();
		
		// una llamada para cachaear el LEVEL		
		this.getLEVEL() ;
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
	}
	
	private void setProtegerValorPrev(boolean valor)
	{
		protegerValorPrev=valor;
	}
	public void setLastSession(int lastSess)
	{
		this.lastSession = lastSess;
	}

	public boolean getDeleted()
	{
		return deleted;
	}

	public void setDeleted(boolean deleted)
	{

//SOLO DEBUG PARA SABER SI SE PUEDE PRESCINDIR DEL CAMPO DELETED		
//System.err.println("\n\n\n ================= DEBUG LLAMADA  A SETDELETED DE FACT");
//Auxiliar.printCurrentStackTrace();


		boolean antig = this.deleted;
		this.deleted=deleted;
		pcs.firePropertyChange("deleted", antig, deleted);
	}
	
	public Integer getIDO()
	{
		return IDO;
	}

	public void setIDO(Integer ido)
	{
		
		int antig = this.IDO;
		IDO = ido;
		pcs.firePropertyChange("IDO", antig, ido.intValue());
		
	}
	
	public Integer getIDTO() 
	{
		
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getIDTO();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getIDTO();
	}

	public void setIDTO(Integer idto) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
			
		//Guardamos los valores iniciales y deducimos la operacion
		int operacion;
		if (getIDTO() == null && idto != null)
			operacion = action.NEW;
		else if (getIDTO() != null && idto == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		Integer antig = getIDTO();
		FactInstance valoresAnterAntig = valoresAnteriores==null?null:valoresAnteriores.clone();
		this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		
		
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setIDTO(idto);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,idto,getVALUE(), getVALUECLS(), getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		pcs.firePropertyChange("IDTO", antig, getIDTO());
		pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		
	}

	public int getPROP()
	{
		return PROP;
	}
	
	public void setPROP(Integer prop)
	{
		if(getPROP()!=prop)
		{
		int antig = this.PROP;
		PROP = prop;
		pcs.firePropertyChange("PROP", antig, prop.intValue());
		}
	}

	public String getCLASSNAME()
	{
		return CLASSNAME;	
	}

	public void setCLASSNAME(String name)
	{
		CLASSNAME = name;
		String antig = this.CLASSNAME;
		CLASSNAME = name;
		pcs.firePropertyChange("CLASSNAME", antig, name);
	}

	public String getOP()
	{
		//Devuelve el ultimo valor del Fact
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getOP();
			}
		}

		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getOP();
		
	}

	public void setOP(String newOP) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{		
		
		//guardamos los valores anteriores y deducimos la operacion que vamos a hacer
		int operacion;
		String antig = getOP();
		if (getOP() == null && newOP != null)
			operacion = action.NEW;
		else if (getOP() != null && newOP == null)
			operacion = action.DEL;
		else
			operacion = action.SET;	
		FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		
		
		//Obtenemos la session Actual	
		int session = SessionController.getInstance().getActualSession(ik).getID();
		this.setLastSession(session);
		
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {
				//caso de tener un SessionValue de esa session,modificarlo (no hace falta una nueva sessionValue)-
				enc = true;
				sessionValues.get(i).setOP(newOP);
				this.setLastSession(session);
			}
		}
		if (!enc) 
		{// en caso de no tener una sessionValue para la sessionActual
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(),getQMIN(), getQMAX(), newOP, getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		pcs.firePropertyChange("OP", antig, getOP());
		pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		avisaSession(operacion);
		
	}
	
	public String getVALUE()
	{
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getVALUE();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getVALUE();
	}
	
	public long getChangeTime(){
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getChangeTime();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getChangeTime();
	}

	public void setVALUE(String value) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		//guardamos los valores iniciales y deducimos la operacion
		int operacion;
		if (getVALUE() == null && value != null)
			operacion = action.NEW;
		else if (getVALUE() != null && value == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		String antig = getVALUE();
		
		boolean cambiado = this.hasCHANGED();
		boolean initialCambiado = this.initialValuesChanged();
		
		FactInstance valoresAnterAntig=null;
		String prevValorStr=null;
		if( protegerValorPrev==false )
		{
			prevValorStr=getPREVALOR();
			valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
			this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		}
		
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int session = sessionActual.getID();
		
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esa session,modificarlo-
				enc = true;
				sessionValues.get(i).setVALUE(value);
				this.setLastSession(session);
			}
		}
		long chTimeOld=getChangeTime();
		long newChtime=chTimeOld;
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),value, getVALUECLS(),getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
			newChtime=s.getChangeTime();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		pcs.firePropertyChange("VALUE", antig, getVALUE());
		pcs.firePropertyChange("VALOR", antig, getVALUE());
		if( protegerValorPrev==false )		
			pcs.firePropertyChange("PREVALOR",prevValorStr,getPREVALOR());
		
		if(newChtime!=chTimeOld) pcs.firePropertyChange("changeTime",chTimeOld,newChtime);
		pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("initialValuesChanged",initialCambiado,initialValuesChanged());
	}
	
	public void setChangeTime(){
		
	}

	public Integer getVALUECLS() 
	{
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getVALUECLS();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getVALUECLS();
	}

	public void setVALUECLS(Integer valuecls) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
	
		//Guardamos los valores iniciales y deducimos la operacion
		int operacion;
		if (getVALUECLS() == null && valuecls != null)
			operacion = action.NEW;
		else if (getVALUECLS() != null && valuecls == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		Integer antig = getVALUECLS();
		FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setVALUECLS(valuecls);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), valuecls, getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		if(getVALUECLS()==null)
			System.err.println("VAlueCls es null:"+this);
		avisaSession(operacion);
		pcs.firePropertyChange("VALUECLS", antig, getVALUECLS());
		pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		
	}
	
	public void setVALUE(String value,Integer valueCls) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
//		guardamos los valores iniciales y deducimos la operacion
		int operacion;
		if (getVALUE() == null && value != null)
			operacion = action.NEW;
		else if (getVALUE() != null && value == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		String antigVal = getVALUE();
		Integer antigValCls = getVALUECLS();
		
		boolean cambiado = this.hasCHANGED();
		boolean initialCambiado = this.initialValuesChanged();
		
		FactInstance valoresAnterAntig=null;
		if( protegerValorPrev==false )
		{
			valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
			this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		}
		
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int session = sessionActual.getID();
		
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esa session,modificarlo-
				enc = true;
				sessionValues.get(i).setVALUE(value);
				sessionValues.get(i).setVALUECLS(valueCls);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),value, valueCls ,getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		pcs.firePropertyChange("VALUE", antigVal, getVALUE());
		pcs.firePropertyChange("VALUECLS", antigValCls, getVALUECLS());
		if( protegerValorPrev==false )
			pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("initialValuesChanged",initialCambiado,initialValuesChanged());
	}
	
	
	
	
	
	public abstract String toString();
	
//	public  String toString();{
//		//TODO ELIMINAR ESTE METODO UNA VEZ ESTEMOS SEGURO SE HA SOBREESCRITO EN TODAS SU ESPECIALIZADOS
//		String stringfact="";
//		stringfact += "(ID= "+this.getID()+")";
//		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
//		try {
//			stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
//		} catch (NotFoundException e) {
//			e.printStackTrace();
//		}
//		stringfact += "(DESTINATIONSYSTEM=" + this.getDestinationSystem()+")";
//		stringfact += "(HASCHANGED=" + this.hasCHANGED()+")";
//		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
//		//stringfact += "(VALUE_s=" + this.getVALUE_s() + ")";
//		stringfact += "(QMIN="+this.getQMIN()+")";
//		stringfact += "(QMAX="+ this.getQMAX()+")";
//		stringfact += "(OP="+this.getOP()+")";
//		stringfact += "(IDO="+this.getIDO()+")";
//		stringfact += "(IDTO="+this.getIDTO()+")";
//		stringfact += "(PROP="+this.getPROP()+")";
//		stringfact += "(VALUE="+this.getVALUE()+")";
//		stringfact += "(VALUECLS="+ this.getVALUECLS()+")";
//		stringfact+="(existiaBBDD= "+this.existia_BD+")";
//		stringfact += "(LEVEL="+ this.levelCache +")";
//		stringfact += "(deleted="+ this.getDeleted() +")";
//		stringfact += "(prevalor="+this.getPREVALOR()+")";
//		stringfact += "(systemValue="+this.getSystemValue()+")";
//		stringfact += "(valoresanteriores="+this.valoresAnteriores+")";
//		stringfact += "(sesiones="+ this.sessionValues +")";
//		return  stringfact;
//	}

	public String toQueryString()
	{
		String stringfact = "";
		stringfact += "(instance ";
		if (this.getIDO() != null)
			stringfact += "( IDO " + this.getIDO() + " )";
		else
			stringfact += "( IDO nil )";
		if (this.getIDTO() != null)
			stringfact += "( IDTO " + this.getIDTO() + " )";
		else
			stringfact += "( IDTO nil )";
		if (this.getVALUE() != null) {
			stringfact += "(VALUE \"" + this.getVALUE() + "\")";
		} else
			stringfact += "( VALUE nil )";
		if (this.getVALUECLS() != null)
			stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		else
			stringfact += "( VALUECLS nil )";
		if (this.getQMIN() != null)
			stringfact += "( QMIN " + this.getQMIN() + " )";
		else
			stringfact += "( QMIN nil )";
		if (this.getQMAX() != null)
			stringfact += "( QMAX " + this.getQMAX() + " )";
		else
			stringfact += "( QMAX nil )";
		if (this.getOP() != null)
			stringfact += "( OP " + this.getOP() + " )";
		else
			stringfact += "( OP nil )";
		return stringfact;
	}

	public String toInstanceString()
	{
		String stringfact = "";
		stringfact += "\n\t (instance ";
		stringfact += "( NAME " + this.getCLASSNAME() + " )";
		if (this.getIDO() == null)
			stringfact += "( IDO nil )";
		else
			stringfact += "( IDO " + this.getIDO() + " )";
		if (this.getIDTO() == null)
			stringfact += "( IDTO nil )";
		else
			stringfact += "( IDTO " + this.getIDTO() + " )";
		stringfact += "( PROP " + this.getPROP() + " )";
		if (this.getVALUE() == null)
			stringfact += "( VALUE nil )";
		else
			stringfact += "( VALUE " + this.getVALUE() + " )";
		if (this.getVALUECLS() == null)
			stringfact += "( VALUECLS nil )";
		else {
			if (this.getVALUECLS() == null)
				stringfact += "( VALUECLS nil )";
			else
				stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		}
		if (this.getQMIN() == null)
			stringfact += "( QMIN nil )";
		else
			stringfact += "( QMIN " + this.getQMIN() + " )";
		if (this.getQMAX() == null)
			stringfact += "( QMAX nil )";
		else
			stringfact += "( QMAX " + this.getQMAX() + " )";
		if (this.getOP() == null)
			stringfact += "( OP nil )";
		else
			stringfact += "( OP " + this.getOP() + " )";
			stringfact += "(deleted " + this.getDeleted() + " )";
			stringfact += "(utask " + this.getUtask() + " )";
		stringfact += ")";
		stringfact += " y es de la session " + this.lastSession;
		stringfact += "session values = " + sessionValues.toString();
		return stringfact;
	}

	public String toInstanceQueryString()
	{
		String stringfact = "";
		stringfact += "(instance ";
		stringfact += "( IDO " + this.getIDO() + " ))";
		return stringfact;
	}

	public Instance toInstance()
	{
		Instance i = new Instance();
		i.setIDTO(new Integer(this.getIDTO()).toString());
		i.setNAME(this.getCLASSNAME());
		i.setOP(this.getOP());
		i.setPROPERTY(new Integer(this.getPROP()).toString());
		i.setQMAX(new Float(this.getQMAX()).toString());
		i.setQMIN(new Float(this.getQMIN()).toString());
		i.setVALUE(this.getVALUE());
		i.setVALUECLS(new Integer(this.getVALUECLS()).toString());
		return i;
	}

	public String toHierarchyString() 
	{
		String stringfact = "\n\t (hierarchy " + "( IDTOSUP " + this.getIDTO()+ " )" + "( IDTO " + this.getIDO() + " ))";
		return stringfact;
	}

	public String toFunctionalAreaFilterString() {
		String stringfilter = "\n\t (functionalarea " + "\n\t\t ( fa "
				+ this.getIDO() + " )\n\t )\n" + "\n\t (filter "
				+ "\n\t\t ( oid " + this.getCLASSNAME() + " )" + "\n\t\t ( to "
				+ this.getIDTO() + " )" + "\n\t\t ( filterNAME filter-"
				+ this.getCLASSNAME() + " )\n\t )\n";
		return stringfilter;
	}

	public String toFilterString(String f) {
		String stringfilter = "\n\t (filter " + "\n\t\t ( oid "
				+ this.getCLASSNAME() + " )" + "\n\t\t ( to " + this.getIDTO()
				+ " )" + "\n\t\t ( filterNAME filter-" + this.getCLASSNAME() + "-"
				+ f + " )\n\t )\n";
		return stringfilter;
	}


	public boolean isNull() 
	{
		return false;
	}

	
	public Double getQMAX() {

		int session = getLastSession();

		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getQMAX();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getQMAX();
	}

	public void setQMAX(Double qmax) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {

		
		int operacion;
		if (getQMAX() == null && qmax != null)
			operacion = action.NEW;
		else if (getQMAX() != null && qmax == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		Double antig = getQMAX();
		FactInstance valoresAnterAntig=null;
		if( protegerValorPrev==false )
		{
			valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
			this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		}
		boolean cambiado = hasCHANGED();
		boolean initialCambiado = initialValuesChanged();
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int session = sessionActual.getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;

		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esa session, modificarlo-
				enc = true;
				sessionValues.get(i).setQMAX(qmax);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la
					// sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), getQMIN(), qmax, getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		pcs.firePropertyChange("QMAX", antig, getQMAX());
		if( protegerValorPrev==false )
			pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("initialValuesChanged",initialCambiado,initialValuesChanged());
	}

	public Double getQMIN() {
		
		
		int session = getLastSession();
		// TODO: leer la session de una variable global.
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getQMIN();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getQMIN();
	}

	public void setQMIN(Double qmin) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		
		int operacion;
		if (getQMIN() == null && qmin != null)
			operacion = action.NEW;
		else if (getQMIN() != null && qmin == null)
			operacion = action.DEL;
		else
			operacion = action.SET;
		Double antig = getQMIN();
		boolean cambiado = hasCHANGED();
		boolean initialCambiado = initialValuesChanged();
		FactInstance valoresAnterAntig=null;
		if( protegerValorPrev==false )
		{
			valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
			this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		}
		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int session = sessionActual.getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue  de esa  session, modificarlo-
				enc = true;
				sessionValues.get(i).setQMIN(qmin);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), qmin, getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			s.setSession(session);
			this.setLastSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		// }else
		// super.setQMIN(qmin);

		pcs.firePropertyChange("QMIN", antig, getQMIN());
		if( protegerValorPrev==false )
			pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("initialValuesChanged",initialCambiado,initialValuesChanged());
	}

	

	/*
	 * public Fact clone() {
	 * 
	 * Fact copia = new Fact(); if(this.getCLSREL()!=null) copia.CLSREL = (new
	 * Integer (this.getCLSREL())); else copia.CLSREL=null;
	 * if(this.getComment()!=null) copia.comment = (new
	 * String(this.getComment())); else copia.comment=null;
	 * if(this.getFactId()!=null) copia.factId = (new
	 * Integer(this.getFactId())); else copia.factId=null; if(this.IDO!=null)
	 * copia.IDO = (new Integer(this.getIDO())); else copia.IDO=null;
	 * if(this.IDOREL!=null) copia.IDOREL = (new Integer(this.getIDOREL()));
	 * else copia.IDOREL=null; if (this.IDTO!=null) copia.IDTO = (new
	 * Integer(this.getIDTO())); else copia.IDTO=null; if (this.LEVEL!=null)
	 * copia.LEVEL = (new Integer(this.getLEVEL())); else copia.LEVEL=null;
	 * if(this.NAME!=null) copia.NAME = (new String(this.getNAME())); else
	 * copia.NAME=null; if(this.OP!=null) copia.OP = (new String(this.getOP()));
	 * else copia.OP=null; if(this.PROP!=null) copia.PROP = (new
	 * Integer(this.getPROP())); else copia.PROP=null; if(this.QMAX!=null)
	 * copia.QMAX = (new Float(this.getQMAX())); else copia.QMAX=null;
	 * if(this.QMIN !=null) copia.QMIN = (new Float(this.getQMIN())); else
	 * copia.QMIN=null; if(this.ROL !=null) copia.ROL = (new
	 * Integer(this.getROL())); else copia.ROL=null; if(this.ROLB!=null)
	 * copia.ROLB = (new Integer(this.getROLB())); else copia.ROLB =null;
	 * if(this.TIMESTP!=null) copia.TIMESTP = (new String (this.getTIMESTP()));
	 * else copia.TIMESTP=null; if(this.VALUE!=null) copia.VALUE= (new String
	 * (this.getVALUE())); else copia.VALUE=null; if(this.VALUECLS!=null)
	 * copia.VALUECLS = (new Integer(this.getVALUECLS())); else
	 * copia.VALUECLS=null; copia.temporalFacts=this.temporalFacts;
	 * 
	 * //TODO //La sesion que se crea tiene que ser solamente una copia //hay
	 * que implementar el clone para la clase session.
	 * 
	 * //copia.setSession(this.getSession());
	 * 
	 * return copia;
	 *  }
	 */

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		pcs.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		pcs.removePropertyChangeListener(pcl);
	}
	
	public void removePropertyChangeListeners() {
		PropertyChangeListener[] p=pcs.getPropertyChangeListeners();
		for(int i=0;i<p.length;i++){
			removePropertyChangeListener(p[i]);
		}
	}

	public void rollBack(Session s) throws ApplicationException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		/*if (s.getID() == this.sesion)
		{
			this.OP = null;
			this.QMAX = null;
			this.QMIN = null;
			this.VALUE = null;
			this.VALUECLS = null;

		}*/
//		FactInstance factAnt = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
//		this.valoresAnteriores=factAnt;
		//System.err.println("Rollback:"+this+" sessionValue:"+this.sessionValues);
		int idactual = s.getID();
		int pos = this.getPositionInSessionValue(idactual);
		
		if (pos != -1)
		{
			dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
			Value vOld=knad.buildValue(this);
			
			SessionValue svAntig = this.sessionValues.remove(pos);
			//System.err.println("svAntig:"+svAntig);
			
			if ((svAntig.getQMAX()==null && this.getQMAX()!=null)||(svAntig.getQMAX()!=null && this.getQMAX()==null)|| ((svAntig.getQMAX() != null) && (!svAntig.getQMAX().equals(getQMAX())))|| ((getQMAX() != null) && (getQMAX().equals(svAntig.getQMAX()))))
			{
				pcs.firePropertyChange("QMAX", svAntig.getQMAX(), getQMAX());
				
			}
			if ((svAntig.getQMIN()==null && this.getQMIN()!=null)||(svAntig.getQMIN()!=null && this.getQMIN()==null)|| ((svAntig.getQMIN() != null) && (!svAntig.getQMIN().equals(getQMIN())))|| ((getQMIN() != null) && (getQMIN().equals(svAntig.getQMIN()))))
			{
				pcs.firePropertyChange("QMIN", svAntig.getQMIN(), getQMIN());
			}
			if ((svAntig.getVALUE()==null && this.getVALUE()!=null)||(svAntig.getVALUE()!=null && this.getVALUE()==null)|| ((svAntig.getVALUE() != null) && (!svAntig.getVALUE().equals(getVALUE())))|| ((getVALUE() != null) && (getVALUE().equals(svAntig.getVALUE()))))
			{
				pcs.firePropertyChange("VALUE", svAntig.getVALUE(), getVALUE());
			}
			if ((svAntig.getVALUECLS()==null && this.getVALUECLS()!=null)||(svAntig.getVALUECLS()!=null && this.getVALUECLS()==null)|| ((svAntig.getVALUECLS() != null) && (!svAntig.getVALUECLS().equals(getVALUECLS())))|| ((getVALUECLS() != null) && (getVALUECLS().equals(svAntig.getVALUECLS()))))
			{
				pcs.firePropertyChange("VALUECLS", svAntig.getVALUECLS(),getVALUECLS());
			}
			if ((svAntig.getOP()==null && this.getOP()!=null)||(svAntig.getOP()!=null && this.getOP()==null)|| ((svAntig.getOP() != null) && (!svAntig.getOP().equals(getOP())))|| ((getOP() != null) && (getOP().equals(svAntig.getOP()))))
			{
				pcs.firePropertyChange("OP", svAntig.getOP(), getOP());
			}
			
			//Lo utilizamos para volver a restaurar el valor que tenia el valor inicial de QMIN y QMAX que se utiliza para cambios de disponibilidad en las reservas
			if(svAntig.getInitialQ()!=null){
				QMIN=this.getInitialQ();
				QMAX=this.getInitialQ();
			}
			
			Value v=knad.buildValue(this);
			
			if(!Auxiliar.equals(v, vOld))//Si al hacer el rollback hay algun cambio en el valor del fact se avisa, si no no hace falta
				avisaSession(vOld,v,getOrder(),s);
			
		} else
			System.out.println("RollBAck de Un Fact que no tiene una session Value, por lo tanto que no ha sido tocado durante esa session");
		
		//YA SE ESTA HACIENDO EN consumirEventoCambio
		//FactInstance factAnt = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		//this.valoresAnteriores=factAnt;
		
		consumirEventoCambio();
		oldValuesForNotify=valoresAnteriores.clone();
		this.setLastSession(getMaxSession());
	}

	public boolean commit(Session s) throws ApplicationException {

		int idactual = s.getID();
		int idmadre = s.getIDMadre();
		
		propagate(idactual,idmadre,false);
		return true;
	}
	
	public void update(int idSessionOrigen,int idSessionDestino) throws ApplicationException {

		int pos = this.getPositionInSessionValue(idSessionOrigen);
		if (pos != -1 /*&& modificado*/)
		{
			SessionValue sv = null;
			int posmadre = this.getPositionInSessionValue(idSessionDestino);
			if (posmadre == -1) {// si en el destino no existe : cambiamos el numero de session y añadimos el this
				sv = this.sessionValues.get(pos);
				sv.setSession(idSessionDestino);
			} else {// si en el destino ya esta, cambiamos y borramos
				SessionValue ref = sessionValues.get(pos);
				ref.setSession(idSessionDestino);
				sessionValues.remove(posmadre);
			}
			setLastSession(idSessionDestino);
			if(SessionController.getInstance().getSession(idSessionDestino)==null)
				System.err.println("ERROR:Fact sin destino:"+this+" \n siendo sesionDestino:"+idSessionDestino);
			//Registrarse en la madre
			if (!SessionController.getInstance().getSession(idSessionDestino).getSesionables().contains(this))
			{
				SessionController.getInstance().getSession(idSessionDestino).addSessionable(this);
			}
			/*else
				System.out.println("NO AÑADIR : Fact ya esta en MADRE, no se añade ");*/			
		}
		else
		{
			//TODO: Error ya que si el Fact esta registrado en una sesion, es porque ha sido modificado y tiene una sessionValue de esa sesion
			System.err.println("WARNING:Intento de Update de un Fact en una sesion en la que no existe");
		}
		
		
			ArrayList<SessionValue> sesToRemoveList = new ArrayList<SessionValue>();
			for(int i = 0 ; i < sessionValues.size();i++)
				if(sessionValues.get(i).getSesion() != idSessionDestino)
				{
					sesToRemoveList.add(sessionValues.get(i));
					SessionController.getInstance().getSession(sessionValues.get(i).getSesion()).getSesionables().remove(this);
				}
		
			this.sessionValues.removeAll(sesToRemoveList);
		
	}
	
	//Si undoPropagation=true se basa en los valores anteriores de las sesiones para restaurar. Es decir en los valores que tenian las sesiones antes de hacer commit en cada hija.
	//De esta manera podemos restaurar el estado original de las sesiones antes de hacer un commit o un rollback de una sesion forceParent=true.
	private void propagate(int idSessionOrigen,int idSessionDestino,boolean undoPropagation){
		
		//boolean modificado = getOP() != OP || getQMAX() != QMAX || getQMIN() != getQMIN()|| getVALUE() != VALUE || getVALUECLS() != VALUECLS ||getIDO()!=IDO || getIDTO()!=IDTO;
		
//		Session sessionActual = SessionController.getInstance().getSession(idSessionOrigen);
//		Iterator <SessionValue> it = sessionValues.iterator();
//		LinkedList<SessionValue> borrables = new LinkedList<SessionValue>();
//		while(it.hasNext())
//		{
//			SessionValue sv = it.next();
//			Session s = SessionController.getInstance().getInstance().getSession(sv.getSesion());
//			if(sessionActual.somosHermanos(s.getID()))
//			{
//				//System.err.println("NUMERO DE SESSIONVALUES ANTES = "+sessionValues.size()+ " son:"+sessionValues);
//				s.getSesionables().remove(this);
//				borrables.add(sv);
//				System.err.println("SESIONES HERMANAS :"+s+" y "+sessionActual+" fact:"+this);
//				//System.err.println("NUMERO DE SESSIONVALUES DESPUES = "+sessionValues.size()+ " son:"+sessionValues);
//			}
//		}	
//		sessionValues.removeAll(borrables);
		int pos = this.getPositionInSessionValue(idSessionOrigen);
		if (pos != -1 /*&& modificado*/)
		{
			SessionValue sv = null;
			int posmadre = this.getPositionInSessionValue(idSessionDestino);
			if (posmadre == -1) {// si en el destino no existe : cambiamos el numero de session y añadimos el this
				if(undoPropagation){
					//System.err.println("AntesUndo:\n"+sessionValues);
					if(sessionValuesChangedPropagation.containsKey((idSessionDestino))){
						sv = sessionValuesChangedPropagation.remove(idSessionDestino);
						sv.setSession(idSessionDestino);
						if(sessionValuesRemovedPropagation.containsKey(idSessionOrigen)){
							SessionValue svRemoved = sessionValuesRemovedPropagation.remove(idSessionOrigen);
							sessionValues.add(pos, svRemoved);
						}else{
							Session sessionOrigen=SessionController.getInstance().getSession(idSessionOrigen);
							sessionOrigen.getSesionables().remove(this);
						}
					}
					//No restauramos oldValuesForNotify ya que no sirve de nada restaurarlo porque no va a haber avisaSession desde session.commit
					
					//System.err.println("DespuesUndo:\n"+sessionValues);
				}else{
					sv = this.sessionValues.get(pos);
					sv.setSession(idSessionDestino);
					sessionValuesChangedPropagation.put(idSessionOrigen,sv);
					
//					FactInstance factAnt = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),null,this.getVALUECLS(),this.getRANGENAME(),null,null,null,this.getCLASSNAME());
//					oldValuesForNotify=factAnt;
				}
			} else {// si en el destino ya esta, cambiamos y borramos
				if(undoPropagation){
					//System.err.println("AntesUndo:\n"+sessionValues);
					if(sessionValuesChangedPropagation.containsKey((idSessionDestino))){
						sv = sessionValuesChangedPropagation.remove(idSessionDestino);
						sv.setSession(idSessionDestino);
						sessionValues.remove(posmadre);
						if(sessionValuesRemovedPropagation.containsKey(idSessionOrigen)){
							SessionValue svRemoved = sessionValuesRemovedPropagation.remove(idSessionOrigen);
							sessionValues.add(pos, svRemoved);
						}else{
							Session sessionOrigen=SessionController.getInstance().getSession(idSessionOrigen);
							sessionOrigen.getSesionables().remove(this);
						}
					}
					//No restauramos oldValuesForNotify ya que no sirve de nada restaurarlo porque no va a haber avisaSession desde session.commit
					
					//System.err.println("DespuesUndo:\n"+sessionValues);
				}else{
					SessionValue ref = sessionValues.get(pos);
					ref.setSession(idSessionDestino);
					SessionValue refMadre = sessionValues.get(posmadre);
					sessionValuesChangedPropagation.put(idSessionOrigen,ref);
					sessionValuesRemovedPropagation.put(idSessionDestino,sessionValues.remove(posmadre));
					
					
//					FactInstance factAnt = new FactInstance(refMadre.getIDTO(),this.getIDO(),this.getPROP(),refMadre.getVALUE(),refMadre.getVALUECLS(),refMadre.getRANGENAME(),refMadre.getQMIN(),refMadre.getQMAX(),refMadre.getOP(),this.getCLASSNAME());
//					oldValuesForNotify=factAnt;
				}
			}
			setLastSession(idSessionDestino);
			if(SessionController.getInstance().getSession(idSessionDestino)==null)
				System.err.println("ERROR:Fact sin destino:"+this+" \n siendo sesionDestino:"+idSessionDestino);
			//Registrarse en la madre
			if (!SessionController.getInstance().getSession(idSessionDestino).getSesionables().contains(this))
			{
				SessionController.getInstance().getSession(idSessionDestino).addSessionable(this);
			}
			/*else
				System.out.println("NO AñADIR : Fact ya esta en MADRE, no se añade ");*/			
		}
		else
		{
			//TODO: Error ya que si el Fact esta registrado en una sesion, es porque ha sido modificado y tiene una sessionValue de esa sesion
			System.err.println("WARNING:Propagacion de Un Fact que no ha sido modificado");
		}
		
		
//		if(SessionController.getInstance().getSession(idSessionDestino) instanceof DocDataModel)
//		{
//			ArrayList<SessionValue> sesToRemoveList = new ArrayList<SessionValue>();
//			for(int i = 0 ; i < sessionValues.size();i++)
//				if(sessionValues.get(i).getSesion() != idSessionDestino)
//				{
//					System.err.println("WARNING:SessionValue no propagado a DocDataModel:"+sessionValues.get(i));
//					sesToRemoveList.add(sessionValues.get(i));
//					SessionController.getInstance().getSession(sessionValues.get(i).getSesion()).getSesionables().remove(this);
//				}
//		
//			this.sessionValues.removeAll(sesToRemoveList);
//		}
		
		
		
	}

	public ArrayList<SessionValue> getSessionValues()
	{
		return sessionValues;
	}

	public int getLastSession()
	{
		for(int i = 0 ; i < sessionValues.size(); i++)
			if(this.lastSession == sessionValues.get(i).getSesion())
				return lastSession;
		
		return this.getMaxSession();
	}
	
	public int getMaxSession()
	{	
		return getMaxSession(-1);
	}
	
	public int getMaxSession(int excludedSession)
	{	
		int max = -1;
		for(int i = 0; i  <this.sessionValues.size();i++) {
			if(sessionValues.get(i).getSesion()>max)
				if(sessionValues.get(i).getSesion()!=excludedSession)
					max = sessionValues.get(i).getSesion();
		}
		
		return max;
	}

	public int getPositionInSessionValue(int id)
	{
		for (int i = 0; i < sessionValues.size(); i++)
			if (sessionValues.get(i).getSesion() == id)
				return i;

		return -1;
	}

	public void setQ(Double qmin, Double qmax) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException  {
		
		int operacion;
		
		if ((getQMAX() == null && qmax != null)
				&& (getQMIN() == null && qmin != null))
			operacion = action.NEW;
		else if ((getQMAX() != null && qmax == null)
				&& (getQMIN() != null && qmin == null))
			operacion = action.DEL;
		else
			operacion = action.SET;
		Object qmaxAntig = this.getQMAX();
		Object qminAntig = this.getQMIN();
		boolean cambiado = hasCHANGED();
		boolean initialCambiado= initialValuesChanged();
		
		FactInstance valoresAnterAntig=null;
//		TODO: comprobar con Auxiliar.equals si los nuevos valores son iguales a los antiguos, en cuyo caso no hacer nada
		if( protegerValorPrev==false )
		{
			valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
			this.valoresAnteriores = new FactInstance(this.getIDTO(),	this.getIDO(),this.getPROP(),
									this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		}

		Session sessionActual = SessionController.getInstance().getActualSession(ik);
		int session = sessionActual.getID();
			
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esa session modificarlo-
				enc = true;
				this.setLastSession(session);
				sessionValues.get(i).setQMAX(qmax);
				sessionValues.get(i).setQMIN(qmin);
				
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la
					// sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), qmin, qmax, getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
			oldValuesForNotify=valoresAnteriores.clone();
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		avisaSession(operacion);
		
		
		pcs.firePropertyChange("QMAX", qmaxAntig, qmax);
		pcs.firePropertyChange("QMIN", qminAntig, qmin);
		pcs.firePropertyChange("DOUBLEVALUE", qminAntig, qmax);
		if( protegerValorPrev==false )
			pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
		pcs.firePropertyChange("hasCHANGED",cambiado,hasCHANGED());
		pcs.firePropertyChange("initialValuesChanged",initialCambiado,initialValuesChanged());
	}

	public void deleteFactSessionable() throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
	 
		FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		FactInstance ant = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		boolean changeoldValuesForNotify = true;
		try
		{
			setProtegerValorPrev(true);	
			
			Session sessionActual = SessionController.getInstance().getActualSession(ik);
			int session = sessionActual.getID();
			for (int i = 0; i < sessionValues.size() && changeoldValuesForNotify; i++) {
				if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esa session,modificarlo-
					changeoldValuesForNotify = false;
				}
			}
				
		String antigop = getOP();
		Double antigqmax = getQMAX();
		Double antigqmin = getQMAX();
		String antigvalue = getVALUE();
		this.setVALUE(null);
		this.setQ(null, null);

		
		//this.valoresAnteriores=ant;
		}
		finally{
			this.valoresAnteriores=ant;
			if(changeoldValuesForNotify){
				oldValuesForNotify=valoresAnteriores.clone();
			}
			setProtegerValorPrev(false);						
		}
		pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
//		avisar al motor de los cambios
		pcs.firePropertyChange("QMAX", valoresAnterAntig.getQMAX(), getQMAX());
		pcs.firePropertyChange("QMIN", valoresAnterAntig.getQMIN(), getQMIN());
		pcs.firePropertyChange("VALUE", valoresAnterAntig.getVALUE(), getVALUE());
	}

	public void avisaSession(int op) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
			//Obtenemos el DocDatamodel para poder construir los Values
			IChangePropertyListener icp = null;
			dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
			Value v = null;
			Value vOld = null;
			//if(this.valoresAnteriores!=null)
				//vOld=knad.buildValue(this.valoresAnteriores); 
			if(this.oldValuesForNotify!=null)
				vOld=knad.buildValue(this.oldValuesForNotify); 
			v=knad.buildValue(this);
			
			//System.out.println("--: cambio de:\n"+this.valoresAnteriores+" a "+this);
			//Avisamos a la session de los cambios mandando los valores anteriores y los valores nuevo
			SessionController.getInstance().getActualSession(ik).changeValue(	this.getIDO(), this.getIDTO(), this.getPROP(),this.getVALUECLS(), v, vOld, this.getLEVEL(), op);
	}

	public void avisaSession(int op, Session s) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException 
	{
			IChangePropertyListener icp = null;
			dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
			Value v = null;
			Value vOld = null;
			//Obtenemos los valores que tenia el Fact en la session Anterior
			//System.err.println("tamaño session values:"+sessionValues.size()); 
//			int pos = -1;
//			for(int i = sessionValues.size()-1 ; i >=0&&pos ==-1;i--)
//			{
//			if(i==0)
//					pos = i;
//			else
//				if(sessionValues.get(i).getSesion() < this.getLastSession())
//					pos =i;			
//			}
			
//			SessionValue sessionValAnt=sessionValuesRemovedPropagation.get(s.getID());
//			System.err.println("Session:"+s.getID()+" SessionValAnterior:"+sessionValAnt);
//			if(sessionValAnt==null){
				//if(this.valoresAnteriores!=null)
				//	vOld=knad.buildValue(this.valoresAnteriores);
				if(this.oldValuesForNotify!=null)
					vOld=knad.buildValue(this.oldValuesForNotify); 
//			}else{
//				FactInstance factSessionAnt=new FactInstance(sessionValAnt.getIDTO(),this.getIDO(),this.getPROP(),sessionValAnt.getVALUE(),sessionValAnt.getVALUECLS(),sessionValAnt.getRANGENAME(),sessionValAnt.getQMIN(),sessionValAnt.getQMAX(),sessionValAnt.getOP(),this.getCLASSNAME());
//				vOld=knad.buildValue(factSessionAnt);
//			}
			v=knad.buildValue(this);
			//System.err.println(SessionController.getInstance().getActualSession(ik)+" "+this.getIDO()+" "+this.getIDTO()+" "+this.getPROP()+" "+this.getVALUECLS()+" new: "+v+" old:"+vOld+" "+op);
			//System.err.println("\n\n DEBUGGGG AVISA SESSION "+this);
			s.changeValue(this.getIDO(), this.getIDTO(), this.getPROP(), this.getVALUECLS(), v, vOld,this.getLEVEL(),op);
	}
	
	public void avisaSession(Value vOld,Value vNew,int op, Session s) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException 
	{
			s.changeValue(this.getIDO(), this.getIDTO(), this.getPROP(), this.getVALUECLS(), vNew, vOld,this.getLEVEL(),op);
	}

	public int getOrder()
	{
		int op = -1;
		//si no esta en Base de datos o los valores iniciales son nulos y los valores nuevos son distintos de nulo, luego es un NEW
		//Tambien miramos si aplica systemValue ya que si el valor de base de datos es nulo pero tiene systemValue tenemos que enviarlo como SET aunque podamos pensar que es NEW ya que al tener systemValue existe ese registro en base de datos. Para DEL no hace falta enviar un SET ya que de eso se encarga el server
		if (	!getExistia_BD()||
				((super.getVALUE() == null )&& /*(super.getVALUECLS() == null) &&*/ (super.getOP() == null)&& (super.getQMAX() == null)&& (super.getQMIN() == null)&& !super.isAppliedSystemValue() && ( getVALUE()!=null||getVALUECLS()!=null||getOP()!=null||getQMAX()!=null||getQMIN()!=null)))
			op = action.NEW;
		else 
			//en caso de que todos los valores nuevos sean nulos y los valores  iniciales  son distintos de nules, luego es un SET
			if(getVALUE() == null  && getOP() == null &&  getQMAX() == null && getQMIN() == null&&( super.getVALUE()!=null ||super.getOP()!=null||super.getQMAX()!=null||super.getQMIN()!=null))
			op = action.DEL;
			else
			//en el resto de los casos, es un set
			op = action.SET;
		return op;
	}
	
	public void setOrder(int order){
		;
	}

	public Fact toFact()
	{
		return this;
	}
	
	public FactInstance toFactInstance() {

		FactInstance fi=new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		fi.setOrder(getOrder());
		fi.setExistia_BD(getExistia_BD());
		fi.setSystemValue(getSystemValue());
		fi.setAppliedSystemValue(isAppliedSystemValue());
		fi.setDestinationSystem(getDestinationSystem());
		return fi;
	}

	public boolean initialValuesChanged() 
	{
		if (!Auxiliar.equals(super.getVALUE(),getVALUE()) ||
			!Auxiliar.equals(super.getVALUECLS(),getVALUECLS())||
			!Auxiliar.equals(super.getOP(),getOP())||
			!Auxiliar.equals(super.getQMAX(),getQMAX())||
			!Auxiliar.equals(super.getQMIN(),getQMIN())||
			!Auxiliar.equals(super.getSystemValue(),getSystemValue())||
			!Auxiliar.equals(super.isAppliedSystemValue(),isAppliedSystemValue())/*||
			!Auxiliar.equals(initialDestinationSytem,getDestinationSystem())*/)
				return true;
			else
				return false;
	}

	public boolean getExistia_BD()
	{		
		return existia_BD;
	}

	public void setExistia_BD(boolean existia_BDD)
	{		
		this.existia_BD = existia_BDD;
	
	}
	public FactInstance getInitialValues()
	{
		 FactInstance fi = new FactInstance(this.IDTO,this.IDO,this.PROP,this.VALUE,this.VALUECLS,this.RANGENAME,this.QMIN,this.QMAX,this.OP,this.CLASSNAME);
		 fi.setExistia_BD(this.existia_BD);
		 fi.setOrder(this.getOrder());
		 fi.setSystemValue(this.systemValue);
		 fi.setAppliedSystemValue(this.appliedSystemValue);
		 fi.setDestinationSystem(destinationSystem);
		 return fi;
	}

//	public boolean isTemporalDeleted()
//	{
//		boolean result=this.getVALUE()==null&&this.getQMAX()==null&&this.getQMIN()==null&&this.getOP()==null&&this.getSystemValue()==null;
//		//System.out.println("isTemporalDeleted fact="+this+"   devuelve "+result  );
//		return result;
//	}

	public FactHandle getFactHandle()
	{
		return factHandle;
	}
		
	public void setFactHandle(FactHandle factHandle)
	{
		this.factHandle = factHandle;
	}

	public void setUtask(Integer utask)
	{
		this.utask = utask;
	}
	public Integer getUtask()
	{
		return SessionController.getInstance().getActualSession(ik)==null?null:SessionController.getInstance().getActualSession(ik).getUtask();
	}
	
	public String  getPROPNAME() throws NotFoundException
	{//pregunta al DocDataModel el nombre de la property
		String name=ik.getPropertyName(this.getPROP()); 
		
		return name;
	}
	public Integer getLEVEL()
	{// TODO: quitar el casting .- pregunta al DocDataModel del nivel del individuo
	 // Wl casting se ha hecho porque el getProperty de la interfaz IKnowledgeBaseInfo pone la sesion actual a NULL
 		Integer level = null;
 		
 		boolean isObjectProp= ik.isObjectProperty(PROP);
 		//	--------------  CASOS DE NIVEL OBJ PROP --------------
 		//	DOM     RANGO     		LEVEL
 		//	IND		CLS,IND			IND
 		//	IND		F				F
 		//	IND 	PRO				PRO
 		//  PRO		CLS,PRO,IND		PRO
 		//	PRO		F				F
 		//	F		*				F
 		//  C						NA
 		
 		Integer levelDom=0;
 		Integer levelRange=0;
 		
 		if(getIDO()==null)
			level = Constants.LEVEL_MODEL;
 		else{
 			boolean success=false;
 			try{
 				levelDom=ik.getLevelOf(this.getIDO());
 				success=true;
 			}finally{
 				if(!success||levelDom==null){
 					System.err.println("\n\n=================================\n=============================     WARNING   -  WARNING:   Fact sin level en Fact.getLevel idto="+this.getIDTO()+"   ido="+this.getIDO()+"  prop="+this.getPROP()+"   value="+this.getVALUE()+"    op="+this.getOP());
 					Auxiliar.printCurrentStackTrace();
 				}
 			}
 		
	 		//if( levelDom==null ) levelRange=Constants.LEVEL_INDIVIDUAL;
	 		if(levelDom!=null && levelDom!=Constants.LEVEL_FILTER && isObjectProp ){
	 			if(getVALUE()==null){
	 				if( hasCHANGED())
	 					level =null;//Mas abajo se actualiza con el cache
	 				else
	 					level= levelDom; 
	 			}else{
	 				levelRange= ik.getLevelOf(new Integer(this.getVALUE()));
	 				if( levelRange==null) 
	 					levelRange=Constants.LEVEL_INDIVIDUAL;
	 				
	 				if(levelRange==Constants.LEVEL_FILTER||levelRange==Constants.LEVEL_PROTOTYPE)
	 					level= levelRange;
	 				else
	 					level= levelDom; 				
	 			}
	 		}else
	 			level= levelDom;
 		}
 				
		if(level  == null)
				level = levelCache;
		else
			levelCache = level;
	

		if(level==null&&this.getOP()==Constants.OP_CARDINALITY)
			level=levelDom;
		
		
		
		return  level;
	}
	
	public String  getVALUE_s()
	{
		return KnowledgeAdapter.getVALUE_s(this);
	}
	
	public String getID()
	{
		if(this.getIDO()!=null)
			return String.valueOf(this.getIDO());
		else 
			return null;
	}
	public void setInitialValues(IPropertyDef f)
	{
		// TODO Auto-generated method stub
	}

	public FactInstance getValoresAnteriores() {
		return valoresAnteriores;
	}
	
	
	public String getPREVALOR() {
		return KnowledgeAdapter.getVALUE_s(this.getValoresAnteriores());
	}
	
	
		public void setValoresAnteriores(FactInstance valoresAnteriores) {
		this.valoresAnteriores = valoresAnteriores;
	}

	public void rollBackOfPropagation(Session s) throws ApplicationException, NotFoundException {
		propagate(s.getIDMadre(), s.getID(), true);
	}
	public boolean hasCHANGED()
	{
		
		return ((!Auxiliar.equals(valoresAnteriores.getVALUE(), this.getVALUE()))
				||(!Auxiliar.equals(valoresAnteriores.getQMAX(), this.getQMAX()))
				||(!Auxiliar.equals(valoresAnteriores.getQMIN(), this.getQMIN()))
				||(!Auxiliar.equals(valoresAnteriores.getOP(), this.getOP())));
		

	}
	/*public void undoLastChange() throws NotFoundException
	{
		if(!Auxiliar.equals(getVALUE(), valoresAnteriores.getVALUE()))
				this.setVALUE(valoresAnteriores.getVALUE());
		else
			if(!Auxiliar.equals(getQMAX(), valoresAnteriores.getQMAX())&&!Auxiliar.equals(getQMIN(), valoresAnteriores.getQMIN())&& Auxiliar.equals(valoresAnteriores.getQMAX(), valoresAnteriores.getQMIN())&& Auxiliar.equals(getQMIN(), getQMAX()))
				this.setQ(valoresAnteriores.getQMIN(), valoresAnteriores.getQMIN());
			else
			if(!Auxiliar.equals(valoresAnteriores.getQMAX(), getQMAX()))
				this.setQMAX(valoresAnteriores.getQMAX());
			else
				if(!Auxiliar.equals(valoresAnteriores.getQMIN(), getQMIN()))
					this.setQMIN(valoresAnteriores.getQMIN());
		
		
	}*/
	
	public void undoLastChange() throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{	
		FactInstance ant = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		try{
		//TODO Modificar setQmin,setQmax, setValue para que soporte ProtegerValorPrev=true;
		setProtegerValorPrev(true);
		if(!Auxiliar.equals(getVALUE(), valoresAnteriores.getVALUE()))
			this.setVALUE(valoresAnteriores.getVALUE());
		else
			if(	!Auxiliar.equals(getQMAX(), valoresAnteriores.getQMAX())&&
				//ALFON: es redundante !Auxiliar.equals(getQMIN(), valoresAnteriores.getQMIN())&& 
				//Si ha cambiado ha cambiado qmax, y no era rango antes ni ahora=> No es ni era rango, y ha cambiado el valor de Q
				 Auxiliar.equals(valoresAnteriores.getQMAX(), valoresAnteriores.getQMIN())&& 
				 Auxiliar.equals(getQMIN(), getQMAX()))

				this.setQ(valoresAnteriores.getQMIN(), valoresAnteriores.getQMIN());
			else
			if(!Auxiliar.equals(valoresAnteriores.getQMAX(), getQMAX()))
				this.setQMAX(valoresAnteriores.getQMAX());
			else
				if(!Auxiliar.equals(valoresAnteriores.getQMIN(), getQMIN()))
					this.setQMIN(valoresAnteriores.getQMIN());
		}
		
		finally{
			this.valoresAnteriores=ant;
			setProtegerValorPrev(false);						
		}
	}
	
	public void consumirEventoCambio(){
		String ant=getPREVALOR();
		   
		valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		//System.err.println(" CONSUMO EVENTO PREVIO, CURR:"+ant+","+getPREVALOR());
		pcs.firePropertyChange("PREVALOR",ant,getPREVALOR());
	}
		
	public Integer getINITIALVALUECLS()
	{
		//Se podria coger directamente this.VALUECLS pero uso el metodo de InitialValues por si esto ya no fuera verdad en el futuro
		return this.getInitialValues().getVALUECLS();
	}
	
	public String getINITIALVALOR()
	{
		return KnowledgeAdapter.getVALUE_s(this.getInitialValues());
	}
	
	
		
	
	/*
	 * este metodo se ha implementado en sessionvalue,
	 * public String getRANGENAME() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException
	{
		DocDataModel ik = (DocDataModel) SessionController.getInstance().getDDM();
		return ik.getClassName(this.getVALUECLS());
	}*/
	
	public String getSystemValue(){
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getSystemValue();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getSystemValue();
	}
	
	public void setSystemValue(String systemValue){
		// Guardamos los valores iniciales 
		String antig = getSystemValue();
		//FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		//this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		//valoresAnteriores.setSystemValue(this.getSystemValue());
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setSystemValue(systemValue);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), getQMIN(), getQMAX(), getOP(), systemValue, isAppliedSystemValue(), getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//if(getSystemValue()==null)
		//	System.err.println("systemValue es null:"+this);
		
		pcs.firePropertyChange("systemValue", antig, getSystemValue());
		//pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
	}
	public void setAppliedSystemValue(boolean appliedSystemValue) {
//		 Guardamos los valores iniciales 
		boolean antig = isAppliedSystemValue();
		//FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		//this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		//valoresAnteriores.setSystemValue(this.getSystemValue());
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setAppliedSystemValue(appliedSystemValue);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), getQMIN(), getQMAX(), getOP(), getSystemValue(), appliedSystemValue, getContribution(), getRANGENAME(), getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//if(getSystemValue()==null)
		//	System.err.println("systemValue es null:"+this);
		
		pcs.firePropertyChange("appliedSystemValue", antig, isAppliedSystemValue());
		//pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
	}
	
	public boolean isAppliedSystemValue() {
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).isAppliedSystemValue();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.isAppliedSystemValue();
	}
	
	public boolean isIncremental() {
		return false;
	}

	public String getDestinationSystem() {
		return this.destinationSystem;
	}
	
	/*public String getRdnValue() {
		return rdnValue;
	}*/
	
	public String getDESTINATIONSYSTEM() {
		//todo importante, eliminar getDestinationSystem y sustituir por este
		return this.destinationSystem;
	}
	
	public void setDestinationSystem(String destinationSystem, boolean firePropertyChange) {
		String actualvalor=this.getDestinationSystem();
		boolean diferentes=(destinationSystem==null&&actualvalor!=null)||(destinationSystem!=null&&actualvalor==null)||(destinationSystem!=null&&actualvalor!=null&&!destinationSystem.equals(actualvalor));
		if(diferentes){
			String antig = this.destinationSystem;
			this.destinationSystem=destinationSystem;
			if(firePropertyChange)
				pcs.firePropertyChange("destinationSystem", antig, destinationSystem);
		}
	}
	
	//Devuelve true en el caso de que si hicieramos rollback este fact no habria cambiado respecto a sus valores iniciales y no habria ninguna otra sesion que lo contuviera por lo que podriamos hacer retract directamente si quisieramos. 
	//En el caso de que sea retractable directamente, simula un rollback avisando a las sesiones pero sin avisar al motor. Luego restaura los valores del fact y desde fuera deberiamos hacer retract del fact.
	//Gracias a este metodo podriamos hacer retract directamente del fact desde fuera sin hacer antes un rollback por lo que ganariamos en eficiencia.
	public boolean rollbackRetractable(Session s) throws NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		boolean retractable=false;
		int idactual = s.getID();
		int pos = this.getPositionInSessionValue(idactual);
		
		if (pos != -1)
		{	
			dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
			Value vOld=knad.buildValue(this);
			SessionValue svAntig = this.sessionValues.remove(pos);
			if(!initialValuesChanged()){
				if(getSessionValues().isEmpty()){
					retractable=true;
					Value v=knad.buildValue(this);
					
					if(!Auxiliar.equals(v, vOld))//Si al hacer el rollback hay algun cambio en el valor del fact se avisa, si no no hace falta
						avisaSession(vOld,v,getOrder(),s);

				}//else System.err.println("WARNING: RollBack:Hay otra/s sesion/es modificando el mismo fact. Numero de sesiones:"+getSessionValues().size());
			}else{
				//System.err.println("Fact cambiado por lo que no retract:"+this);
			}
			
			sessionValues.add(pos, svAntig);
		}
		return retractable;
	}	

	public abstract Object clone(IKnowledgeBaseInfo ik);
	
	public String getRANGENAME() 
	{
		int session = getLastSession();
		if (this.sessionValues.size() != 0) {
			for (int i = 0; i < sessionValues.size(); i++) {
				if (sessionValues.get(i).getSesion() == session)
					return sessionValues.get(i).getRANGENAME();
			}
		}
		//TODO: lanzar una exepcion o un warrning
		//Nunca deberia entrar aqui ya que siempre hay que tener al menos un sessionValue en el fact
		return super.getRANGENAME();
	}
	
	public void setRANGENAME(String rangeName) {
//		 Guardamos los valores iniciales 
		String antig = getRANGENAME();
		//FactInstance valoresAnterAntig =valoresAnteriores==null?null:valoresAnteriores.clone();
		//this.valoresAnteriores = new FactInstance(this.getIDTO(),this.getIDO(),this.getPROP(),this.getVALUE(),this.getVALUECLS(),this.getRANGENAME(),this.getQMIN(),this.getQMAX(),this.getOP(),this.getCLASSNAME());
		//valoresAnteriores.setSystemValue(this.getSystemValue());
		
		int session = SessionController.getInstance().getActualSession(ik).getID();
		// primero mirar si hay una session con el mismo nombre.
		boolean enc = false;
		for (int i = 0; i < sessionValues.size() && !enc; i++) {
			if (sessionValues.get(i).getSesion() == session) {// caso de tener un SessionValue de esasession, modificarlo-
				enc = true;
				sessionValues.get(i).setRANGENAME(rangeName);
				this.setLastSession(session);
			}
		}
		if (!enc) // en caso de no tener una sessionValue para la sessionActual
		{
			SessionValue s = new SessionValue(ik,getIDTO(),getVALUE(), getVALUECLS(), getQMIN(), getQMAX(), getOP(), getSystemValue(), isAppliedSystemValue(), getContribution(), rangeName, getInitialQ());
			this.setLastSession(session);
			s.setSession(session);
			this.sessionValues.add(s);
		}
		SessionController.getInstance().getActualSession(ik).addSessionable(this);
		//if(getSystemValue()==null)
		//	System.err.println("systemValue es null:"+this);
		
		pcs.firePropertyChange("RANGENAME", antig, getRANGENAME());
		//pcs.firePropertyChange("valoresAnteriores",valoresAnterAntig,valoresAnteriores);
	}
	
	public String getRdn(){
		System.err.println("ERROR: El metodo getRdn de Fact no esta implementado. No debe ser llamado.");
		Auxiliar.printCurrentStackTrace();
		return null;
	}
	
	public String getRdnValue(){
		System.err.println("ERROR: El metodo getRdnValue de Fact no esta implementado. No debe ser llamado.");
		Auxiliar.printCurrentStackTrace();
		return null;
	}

}