/**
  * @author Hassan Ali Sleiman - hassansleiman@gmail.com
 */
package dynagent.ruleengine.src.sessions;



import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.text.Position;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.Reservation;
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
import dynagent.common.knowledge.IEmailListener;
import dynagent.common.knowledge.IExecuteActionListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.action;
import dynagent.common.properties.DomainProp;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.ExecuteActionRequest;
import dynagent.common.sessions.IChangePropertyListener;
import dynagent.common.sessions.ISessionStateListener;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.Utils;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.ruler.FactAccess;
import dynagent.ruleengine.src.ruler.Individual;
import dynagent.ruleengine.src.ruler.JBossEngine;
import dynagent.ruleengine.src.ruler.Lock;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.QuestionRequest;

public class DefaultSession implements Session{

	protected HashSet <Sessionable> sesionables = new HashSet<Sessionable>() ;
	protected int ID;
	protected int IDMadre;
	protected Integer utask;
	protected ArrayList <IChangePropertyListener> changeProp=new ArrayList <IChangePropertyListener>();
	protected HashSet <IMessageListener> messagesListener=new HashSet<IMessageListener>();
	protected ArrayList <ISessionStateListener> stateListener=new ArrayList <ISessionStateListener>();
	protected boolean checkCoherenceObjects;
	protected boolean runRules;
	protected boolean lockObjects;
	protected int state;
	protected boolean forceParent;
	protected boolean deleteFilters;
	protected LinkedList<ChangeProperty> historyChangeProperty;
	protected LinkedList<ChangeProperty> temporalChangeProperty;
	protected ArrayList<Integer> childrenClosed;
	//protected HashMap<Integer,Integer> idosLocked;
	protected boolean notifyChanges;
	protected String rulesGroup;//Indica bajo que grupo de reglas se ejecutara esta sesion. Lo obtiene de lo que tiene definido el padre pero es modificable
	boolean createNewMotor;//Indica si crear o no un nuevo motor en el rollback para que sea mas rapida la limpieza
	
	boolean possibleRunDDBB;//Indica si desbloquear individuos y hacer run en el grupo ddbrules si se cumplen las condiciones necesarias
	
	boolean reusable;//Indica si al hacer commit o rollback desaparece la sesion o se puede volver a usar
	IKnowledgeBaseInfo ik;
	
	public StackTraceElement[] trace;//Nos sirve para saber quien ha hecho dispose de la sesion. Solo para depuracion
	
	protected LinkedHashMap<String,Reservation> reservationList;
	
	protected HashMap<Integer,HashSet<Integer>> pendingLocks;
	
	protected HashMap<Integer,HashSet<Integer>> locks;
	
	protected HashMap<Integer,EmailRequest> emailRequestMap;
	
	protected HashSet<String> noticeMessages;
	
	private IExecuteActionListener executeActionListener;
	
	private ArrayList<ExecuteActionRequest> executeActionRequestList;
	
 	public int getIDMadre()
 	{
		return IDMadre;
	}
 	
	//TODO deleteFilters ya no se usa para nada ya que es la sesion la que, si el padre es DocDatamodel, borra los filtros si es necesario
 	public DefaultSession(IKnowledgeBaseInfo ik,Session sPadre,Integer utaske,/* String user,*/ boolean checkCoherenceObjects, boolean runRules, boolean lockObjects, boolean deleteFilters, boolean forceParent)
	{
 		this.ik=ik;
 		
 		if(ik==null){
 			System.err.println("Ik null");
 			Auxiliar.printCurrentStackTrace();
 		}
 		
		this.ID=SessionController.getInstance().getUNIC_ID();
		//System.err.println("NewSession:"+ID+" Parent:"+(sPadre!=null?sPadre.getID():"")+" Sessionables.padre:"+(sPadre!=null?sPadre.getSesionables().size():""));
		this.checkCoherenceObjects=checkCoherenceObjects;
		this.runRules=runRules;
		this.lockObjects=lockObjects;
		this.deleteFilters=deleteFilters;
		this.historyChangeProperty=new LinkedList<ChangeProperty>();
		this.temporalChangeProperty=new LinkedList<ChangeProperty>();
		this.emailRequestMap=new HashMap<Integer, EmailRequest>();
		this.noticeMessages=new HashSet<String>();
		this.forceParent=forceParent;
		this.childrenClosed=new ArrayList<Integer>();
		//this.idosLocked=new HashMap<Integer, Integer>();
		this.notifyChanges=true;
		int  i= 0 ;
		if(sPadre!=null&&SessionController.getInstance().getSession(sPadre.getID())==null)
		{
			Auxiliar.printCurrentStackTrace();
			System.err.println("EXCEPCION:"+this.ID+ "la madre = "+sPadre+"NO EXISTE EN LA LISTA DE SESIONES");
			//i = 0 /0;
		}
		
		if(sPadre!=null)
		{
			this.setIDMadre(sPadre.getID());
			this.setRulesGroup(sPadre.getRulesGroup());
		}
		else{
			setIDMadre(-1);
			setRulesGroup(Constants.RULESGROUP_rules);
		}
		changeProp = new ArrayList<IChangePropertyListener> (); 
		this.utask = utaske;
		/*this.user=user;*/
		SessionController.getInstance().add(this,ik);
		setState(this.USE_STATE);
		createNewMotor=false;
		reusable=false;
		possibleRunDDBB=true;
		reservationList=new LinkedHashMap<String, Reservation>();
		pendingLocks=new HashMap<Integer, HashSet<Integer>>();
		locks=new HashMap<Integer, HashSet<Integer>>();
		
		executeActionRequestList=new ArrayList<ExecuteActionRequest>();
	}
 	//TODO: eleminar ese constructor
	/*public DefaultSession(Session sPadre,Integer utaske)
	{
		this.ID=SessionController.getInstance().getUNIC_ID();
		if(sPadre!=null)
		{
			this.setIDMadre(sPadre.getID());
		}
		else
			setIDMadre(-1);
		changeProp = new ArrayList<IChangePropertyListener> (); 
		this.utask = utaske;
		SessionController.getInstance().Add(this);
	}*/

 	public void addIMessageListener(IMessageListener a)
	{
		this.messagesListener.add(a);
	}
 	
 	public void removeIMessageListener(IMessageListener a){
		this.messagesListener.remove(a);
	}
 	
 	/*Si history=true se envian tambien todos los cambios producidos antes de registrarnos*/
	public void addIchangeProperty(IChangePropertyListener a,boolean history) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException
	{
		this.changeProp.add(a);
		if(history)
			notifyHistoryChanges(a);
	}
	
	public void removeIchangeProperty(IChangePropertyListener a){
		this.changeProp.remove(a);
	}
	
	private void notifyChanges() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		//Creamos una nueva lista que es la que recorreremos para evitar la excepcion por modificacion concurrente de temporalChangeProperty
		//ya que los avisos de cambios pueden provocar nuevos cambios que se almacenan en temporalChangeProperty
		if(temporalChangeProperty==null){
			//TODO: ERROR: HA FALLADO EN VENTANA AGRUPACION TALLA COLOR
			Throwable e= new Throwable("Posible llamada a notificar tras un dispose");
			e.printStackTrace();
			return; 
		}
		LinkedList<ChangeProperty> list=new LinkedList<ChangeProperty>();
		list.addAll(temporalChangeProperty);
		temporalChangeProperty.clear();//Lo borramos ya por si se vuelve a llamar a este metodo que no vuelva a avisar de los mismos cambios de temporal
		if(list.size()>0){
			for (int i = 0 ; i < changeProp.size();i++)
			{
				changeProp.get(i).initChangeValue();
			}
			
//				Date now=new Date(System.currentTimeMillis());
//				System.err.println("\n        ************Antes changeValue ****"+this.ID+"***    "+now);
			
			Iterator<ChangeProperty> itr=list.iterator();
			while(itr.hasNext())
			{
				ChangeProperty change=itr.next();
				if(mustNotifyChanges(change)){
					for (int i = 0 ; i < changeProp.size();i++)
					{
						changeProp.get(i).changeValue(change.getIdo(), change.getIdto(), change.getIdProp(), change.getValueCls(), change.getValue(), change.getOldValue(), change.getLevel(), change.getOperation());
						//System.out.println("AVISO:"+operation);
					}
				}
			}
//			now=new Date(System.currentTimeMillis());
//			System.err.println("\n        ************Antes endChangeValue ****"+this.ID+"***    "+now);
			for (int i = 0 ; i < changeProp.size();i++)
			{
				changeProp.get(i).endChangeValue();
			}
//			now=new Date(System.currentTimeMillis());
//			System.err.println("\n        ************Despues endChangeValue ****"+this.ID+"***    "+now);
		}
	}
	
	private void notifyHistoryChanges(IChangePropertyListener a) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		//Creamos una nueva lista que es la que recorreremos para evitar la excepcion por modificacion concurrente de historyChangeProperty
		//ya que los avisos de cambios pueden provocar nuevos cambios que se almacenan en historyChangeProperty
		LinkedList<ChangeProperty> list=new LinkedList<ChangeProperty>();
		list.addAll(historyChangeProperty);
		if(list.size()>0){
			a.initChangeValue();
			Iterator<ChangeProperty> itr=list.iterator();
			while(itr.hasNext())
			{
				ChangeProperty change=itr.next();
				if(mustNotifyChanges(change))
					a.changeValue(change.getIdo(), change.getIdto(), change.getIdProp(), change.getValueCls(), change.getValue(), change.getOldValue(), change.getLevel(), change.getOperation());
			}
			a.endChangeValue();
		}
	}
	
	public boolean mustNotifyChanges(ChangeProperty change){
		boolean notify=true;
		
		if(Auxiliar.equals(change.getValue(), change.getOldValue())){
			notify=false;
		//Si no existe ya ese value en motor, siendo un filtro o prototipo, no avisamos del cambio ya que daria problemas al listener si intenta identificar ese objeto
		}else if(change.getValue()!=null && change.getValue() instanceof ObjectValue && Constants.isIDTemporal(((ObjectValue)change.getValue()).getValue()) && !ik.existInMotor(((ObjectValue)change.getValue()).getValue())){
			notify=false;
		//Si no existe ya ese ido en motor no avisamos del cambio ya que habra un aviso de desvinculacion que es el que interesaria al listener
		}else if(!ik.existInMotor(change.getIdo())){
			notify=false;
		}else{
			//Si ese ido esta borrado o aislado no avisamos del cambio ya que habra un aviso de desvinculacion que es el que interesaria al listener
			Individual individual=((DocDataModel)ik).getRuleEngine().getIndividualFact(change.getIdo());
			if(individual!=null && (individual.getSTATE()==Constants.INDIVIDUAL_STATE_DELETED || individual.isISOLATED())){
				notify=false;
			}
		}
		
		return notify;
	}
	
	//TODO: Eliminar el metodo
	public DefaultSession(int id)
	{
		this.ID =id;
		SessionController.getInstance().add(this,ik);
	}
	public void setIDMadre(int madre) {
		IDMadre = madre;
	}
	public int getID() {
		return ID;
	}
	
	public int setID_UNIC()
	{
		this.ID = (SessionController.getInstance().getUNIC_ID());
		return this.getID();
	}
	public void rollBack() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		if(!reusable)
			setState(this.FINISHING_STATE);

		Session oldSession=SessionController.getInstance().getActualSession(ik)!=this?SessionController.getInstance().getActualSession(ik):null;//Comprobamos que no sea ella misma ya que setActual del final volveria a añadirla a sessionList
		try{
			setNotifyChanges(false);
			//Caso de que esa session sea Padre de otra session --> lanzar exepcion
			//System.err.println("RollBack de la session :"+this.getID()+"que tiene:"+this.sesionables.size() + " sessionables");
			//System.err.println(SessionController.getInstance().getSessionsList());
			SessionController.getInstance().setActual(this,ik);
			Iterator<Session> itrS = SessionController.getInstance().getSessionsList(ik).values().iterator();
			while(itrS.hasNext()){
				Session sess=itrS.next();
				if(sess.getIDMadre() == this.ID && !childrenClosed.contains(sess.getID()) )
				{
					//System.err.println("sess:"+sess);
					//System.err.println("childrenClosed:"+childrenClosed);
					System.err.println("ERROR: INTENTO DE ROLLBACK DE UNA SESSION ("+this.ID+")  ANTES DE CERRAR LAS SESIONES HIJAS ");
					throw new ApplicationException(11000,"ERROR: intento de rollBack de una sesion ("+this.ID+") antes de cerrar sus sub-sesiones" +  sess);

				}	
			}
		//	if(SessionController.getInstance().getActualSession(ik)!=this)
		//	{
		//		throw new ApplicationException(11002,"ERROR: intento de rollBack de una sesion ("+this.ID+") que no es actual"+SessionController.getInstance().getActualSession(ik).getID());
	
		//	}
			//rollBack de los sessionables apuntados por la madre
//			System.err.println("rollBack de "+sesionables.size() +" sessionables");
//			Date now=new Date(System.currentTimeMillis());
//			System.err.println("\n        ************Antes todo rollback "+sesionables.size()+"*******    "+now);
			
			if(isProcessingRemoveMotor()){
				//System.err.println("No hay que limpiar la sesion "+getID()+", ya que se descarta el motor");
				unlockObjects();
				cancelReservations();
			}else{
				ArrayList<Sessionable> factssesionables=new ArrayList<Sessionable>();
				factssesionables.addAll(this.sesionables);//Para que al hacer retractFact no de concurrentModification
				HashSet<Integer> idosToRemove=new HashSet<Integer>(); 
				HashMap<Integer,Individual> individualRollbackRetractable=new HashMap<Integer,Individual>(); 
				HashMap<Integer,Lock> lockRollbackRetractable=new HashMap<Integer,Lock>(); 
				//boolean deleteFilters=false;
				//if(SessionController.getInstance().getSession(this.IDMadre)==ik){
					deleteFilters=true;
				//}
	//				if(ik!=null && ((DocDataModel)ik).getRuleEngine()!=null){
	//					System.err.println("Antes rollback");
	//					((DocDataModel)ik).getRuleEngine().getMotorSize();
	//				}
	//				int factfilter=0;
	//				int factprototype=0;
	//				int factindividual=0;
	//				int prototype=0;
	//				int individual=0;
					
					
					Thread counter=null;
					if(!createNewMotor){
						// Thread utilizada para calcular el tiempo que esta tardando el rollback. Si es superior a una cantidad se indica que se cree un motor nuevo
						counter=new Thread(){
	
							public void run() {
								try{
									sleep(Constants.TIMEMILLIS);
									createNewMotor=true;
								}catch(Exception ex){
									//ex.printStackTrace();
								}
							}
						};
						counter.start();
					}
					
				Iterator itr=factssesionables.iterator();
				while(itr.hasNext())
				{
					Object f=itr.next();
					if(f instanceof Fact){
						
						Fact fact=((Fact)/*factssesionables.get(i)*/f);
						
	//					if(fact.getIDO()!=null){
	//						if(ik.getLevelOf(fact.getIDO())==null)
	//							System.err.println("Fact sin level en motor:"+fact);
	//						if(ik.getLevelOf(fact.getIDO())==Constants.LEVEL_FILTER)
	//							factfilter++;
	//						else if(ik.getLevelOf(fact.getIDO())==Constants.LEVEL_PROTOTYPE)
	//							factprototype++;
	//						else factindividual++;
	//					}
						if(fact.rollbackRetractable(this)){
							//FactInstance factI=((FactInstance)factsImportantes.get(i));
							//if(ik.getLevelOf(fact.getIDO())!=Constants.LEVEL_FILTER || retractFilters){//TODO Solo se borran los filtros si se indica en el constructor ya que hay filtros reutilizables
								Integer ido=fact.getIDO();
								Integer idto=fact.getIDTO();
								//if(!ddm.isSpecialized(idto, Constants.IDTO_UTASK)){//Las userTask nunca se borran porque son reutilizables
								
								if(fact==null||ik==null)
									throw new SystemException(1,"DefaultSession: Alguno de estos dos valores esta a nulo: IK="+ik+" \n y Fact = ");
								
								if(ik.getLevelOf(ido)==null)
									System.err.println("ERROR: Fact sin level:"+fact);
								//if(ik.getLevelOf(ido)/*fact.getLEVEL()*/!=Constants.LEVEL_FILTER || (deleteFilters && !ik.isGenericFilter(ido))){
								if(ik.getLevelOf(ido)/*fact.getLEVEL()*/==Constants.LEVEL_FILTER){
									if(deleteFilters && !ik.isGenericFilter(ido) && !ik.isSpecialized(idto, Constants.IDTO_UTASK)){
										idosToRemove.add(ido);
									}//else System.err.println("WARNING:No se borra el filtro siendo deleteFilter:"+deleteFilters+" fact:"+f);
								}
								//}else System.err.println("WARNING:No se borra el filtro siendo deleteFilter:"+deleteFilters+" fact:"+f);
								//System.err.println("Fact para retract:"+fact);
								try{
									((DocDataModel)ik).retractInfoFact(fact,!createNewMotor);
								}catch(Exception ex){
									System.err.println("WARNING: Retract del fact no se ha podido realizar, posiblemente porque no este en motor, siendo el fact:"+fact);
									ex.printStackTrace();
								}
							//}
						}else{
							fact.rollBack(this);
						}
						
					}else if(f instanceof Individual){
						Individual ind=(Individual)f;
						/*if(ind.getLEVEL()==Constants.LEVEL_PROTOTYPE)
							prototype++;
						else individual++;*/
						
						if(!ik.existInMotor(ind.getIDO()) && ind.getSTATE()==Constants.INDIVIDUAL_STATE_DELETED){
							if(ind.getSessionsRecord().size()>1)
								ind.rollBack(this);
							else{
								//System.err.println("Warning: Hace retract directamente de este factindividual ya que no existe en motor:"+ind);
								((DocDataModel)ik).retractInfoIndividual(ind,!createNewMotor);
							}
						}else{
							if(ind.rollBackRetractable(this)){
								individualRollbackRetractable.put(ind.getIDO(),ind);
								idosToRemove.add(ind.getIDO());
							}else ind.rollBack(this);
						}
					}else if(/*factssesionables.get(i)*/f instanceof FactAccess){
						 //((DocDataModel)ik).getRuleEngine().retractFactAccess((FactAccess)f);
						((DocDataModel)ik).retractInfoFactAccess((FactAccess)f,true);
					}else if(/*factssesionables.get(i)*/f instanceof Lock){
						Lock lock=(Lock)f;
						
						if(lock.rollBackRetractable(this)){
							lockRollbackRetractable.put(lock.getIDO(),lock);
							idosToRemove.add(lock.getIDO());
						}else lock.rollBack(this);
					}else{
						 //System.err.println("WARNING:Sessionable de un tipo no tratado en Rollback f:"+f);
						((DocDataModel)ik).getRuleEngine().retract(f);
						if(f instanceof QuestionRequest){
							ik.getQuestionListener().cancelled(((QuestionRequest)f).getID(),((QuestionRequest)f).getRDN(),ik);
						}
					}
				}
				if(!createNewMotor)//Lo cancelo aqui en vez de despues de retract de los objetos ya que lo que tarda es el retract de los fact
					counter.interrupt();
	//			System.err.println("Factfilter:"+factfilter+" Factprototype:"+factprototype+" Factindividual:"+factindividual);
	//			System.err.println("prototype:"+prototype+" individual:"+individual);
	//			now=new Date(System.currentTimeMillis());
	//			System.err.println("\n        ************Despues rollback/retract *******    "+now);
					
	//				now=new Date(System.currentTimeMillis());
	//				System.err.println("\n        ************Despues retract *******    "+now);
	//				
					//System.out.println("\n=========ROLLBACK VA  A BORRAR LOS OBJETOS:" +idosToRemove);
					//this.removeFromRuler(idosToRemove.iterator());
					Iterator<Integer> idos=idosToRemove.iterator();
					//ArrayList<Integer> listIdosToUnlock=new ArrayList<Integer>();
					while(idos.hasNext()){
						  int ido=idos.next();
						  int level=ik.getLevelOf(ido);
						  //if(level==Constants.LEVEL_INDIVIDUAL)
						  //	listIdosToUnlock.add(ido);
						  LinkedList<IPropertyDef> list=((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereIdo(ido);
						  boolean retract=false;
						  if(list.isEmpty() || ((DocDataModel)ik).listFactRetractable.containsAll(list)){
							  if(level!=Constants.LEVEL_FILTER){
								  ((DocDataModel)ik).retractInfoObject(ido,false,!createNewMotor);
								  retract=true;
							  }else{//Si es un filtro solo lo borramos si no esta siendo apuntado por nadie
								  LinkedList<IPropertyDef> listPointed=((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereValueAndValueCls(ido+"",ik.getClassOf(ido));
								  if(listPointed.isEmpty() || ((DocDataModel)ik).listFactRetractable.containsAll(listPointed)){
									  ((DocDataModel)ik).retractInfoObject(ido,false,!createNewMotor);
									  retract=true;
								  }
							  }
								  
							  if(!retract){
								  if(individualRollbackRetractable.containsKey(ido)){
									  //Si no se ha tenido que hacer retract del individuo hacemos el rollback del fact individual ya que lo pospusimos para no tener que hacer retract y rollback
									  individualRollbackRetractable.get(ido).rollBack(this);
								  }
								  
								  if(lockRollbackRetractable.containsKey(ido)){
									  //Si no se ha tenido que hacer retract del individuo hacemos el rollback del fact individual ya que lo pospusimos para no tener que hacer retract y rollback
									  lockRollbackRetractable.get(ido).rollBack(this);
								  }
							  }
						  }//else System.err.println("EL IDO"+ido+" DE LA CLASE:"+((DocDataModel)ik).getClassOf(ido)+"NO se ha podido eliminar de las mapas y la lista de facts es : \n"+((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereIdo(ido));
						  else{
							  //TODO En esta zona del codigo, en teoria, nunca deberia entrar. De momento se deja para encontrar errores.
							  if(!retract){
								  if(individualRollbackRetractable.containsKey(ido)){
							  		  //System.err.println("WARNING: Se esta haciendo rollback de un individual retractable f:"+individualRollbackRetractable.get(ido));
									  //Si no se ha tenido que hacer retract del individuo hacemos el rollback del fact individual ya que lo pospusimos para no tener que hacer retract y rollback
									  individualRollbackRetractable.get(ido).rollBack(this);
								  }
								  
								  if(lockRollbackRetractable.containsKey(ido)){
									  //Si no se ha tenido que hacer retract del individuo hacemos el rollback del fact individual ya que lo pospusimos para no tener que hacer retract y rollback
									  lockRollbackRetractable.get(ido).rollBack(this);
								  }
							  }
						  }
					}
					/*if(lockObjects)
						((DocDataModel)ik).getServer().unlockObjects(listIdosToUnlock, ((DocDataModel)ik).getUser());*/
	//				now=new Date(System.currentTimeMillis());
	//				System.err.println("\n        ************Despues retractObject *******    "+now);
					unlockObjects();
					cancelReservations();
				//}
				//SessionController.getInstance().setActual(null);
	
					
	//				now=new Date(System.currentTimeMillis());
	//				System.err.println("\n        ************Despues unlock *******    "+now);
					
					if(createNewMotor && /*ik==SessionController.getInstance().getSession(this.getIDMadre())*/!forceParent){
						HashSet<Object> list=((DocDataModel)ik).listFactRetractable;
						((JBossEngine)((DocDataModel)ik).getRuleEngine()).cloneMotor(list, ((DocDataModel)ik).getDataModelAdapter());
						((DocDataModel)ik).listFactRetractable.removeAll(list);
					}
	
	//				now=new Date(System.currentTimeMillis());
	//				System.err.println("\n        ************Final todo rollback *******    "+now);
					
	//				if(ik!=null && ((DocDataModel)ik).getRuleEngine()!=null){
	//					System.err.println("Final rollback");
	//					((DocDataModel)ik).getRuleEngine().getMotorSize();
					//}
					/*if(SessionController.getInstance().getSession(this.getIDMadre())==ik)
						((DocDataModel)ik).getRuleEngine().printMotor();*/
			}
			
			if(SessionController.getInstance().containsSession(this.getIDMadre()))
				SessionController.getInstance().getSession(this.IDMadre).childSessionClosed(this.ID,false,createNewMotor,false);
			
		}finally{
			setNotifyChanges(true);
			setState(this.USE_STATE);
			createNewMotor=false;
			SessionController.getInstance().setActual(oldSession,ik);
		}
			//SessionController.getInstance().remove(this.ID,ik);
			
			//setState(this.FINISH_STATE);
			//notifyState(false);
			if(!reusable)
				dispose();
	}
	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException 
	{	
		//double inicio,fin,tiempos;
		 //inicio=System.currentTimeMillis();
		 //System.err.println("\n============  INICIO COMMIT ======="+new Date(System.currentTimeMillis()));
		setState(this.FINISHING_STATE);

		Session oldSession=SessionController.getInstance().getActualSession(ik)!=this?SessionController.getInstance().getActualSession(ik):null;//Comprobamos que no sea ella misma ya que setActual del final volveria a añadirla a sessionList
		SessionController.getInstance().setActual(this,ik);
		
		try{
			setNotifyChanges(false);
			//System.out.println("Commit de la session : " + this.ID +" con " +this.sesionables.size()+" sessionables. las sesiones actuales:"+SessionController.getInstance().getSessionsList());
			
			Iterator<Session> itrS = SessionController.getInstance().getSessionsList(ik).values().iterator();
			while(itrS.hasNext()){
				Session sess=itrS.next();
				if(sess.getIDMadre() == this.ID && !childrenClosed.contains(sess.getID()) )
				{
					//System.err.println("sess:"+sess);
					//System.err.println("childrenClosed:"+childrenClosed);
					System.err.println("ERROR: INTENTO DE COMMIT DE UNA SESSION ("+this.ID+")  ANTES DE CERRAR LAS SESIONES HIJAS :"+sess.getID());
					throw new SystemException(SystemException.ERROR_SISTEMA,"ERROR: intento de commit de una sesion ("+this.ID+")antes de cerrar sus sub-sesiones");
				}	
			}
			
			boolean childPossibleRunDDBB=possibleRunDDBB;//Bandera para evitar que la sesion padre tambien haga runRulesDDBB
			//Si el ancestro es una sesion que va a base de datos y se va a hacer commit de todos sus hijos porque son forceParent, desbloqueamos localmente del motor los individuos-prototipos que esten bloqueados
			if(possibleRunDDBB && isForceParent()){
				Session sess=this;
				HashSet<Sessionable> listSessionables=new HashSet<Sessionable>();//Lista con todos los facts de la sesion y sus ancestros
				listSessionables.addAll(this.getSesionables());
				while(sess.isForceParent()){
					sess=SessionController.getInstance().getSession(sess.getIDMadre());
					listSessionables.addAll(sess.getSesionables());
				}
				childPossibleRunDDBB=true;
				if(sess instanceof DDBBSession){
					unLockLocalSessionables(listSessionables,true);//Con true para que al desbloquear se puedan disparar las reglas del grupo rules
					runRulesDDBB();//Run solo a las reglas del grupo bbddrules. Son las ultimas que tienen algo que decir antes del envio a base de datos
					listSessionables.addAll(this.getSesionables());//Volvemos a añadir para tener los facts creados en runRulesDDBB
					unLockLocalSessionables(listSessionables,false);//Con false para evitar que al desbloquear se pudiera disparar alguna regla del grupo rules
					childPossibleRunDDBB=false;
					sendPendingLocks();//Enviamos los bloqueos a base de datos
				}
			}
			
//			//Si el padre es docDataModel desbloqueamos localmente del motor los individuos-prototipos que esten bloqueados
//			if(SessionController.getInstance().getSession(this.IDMadre) instanceof DDBBSession){
//				unLockLocalSessionables(sesionables,true);//Con true para que al desbloquar se puedan disparar las reglas del grupo rules
//				runRulesDDBB();//Run solo a las reglas del grupo bbddrules. Son las ultimas que tienen algo que decir antes del envio a base de datos
//				unLockLocalSessionables(sesionables,false);//Con false para evitar que al desbloquear se pudiera disparar alguna regla del grupo rules
//			}
	
			if(isCheckCoherenceObjects())
			{
	
				try{
					checkCoherence(false);
	
				}
				catch(IncompatibleValueException e){
					SessionController.getInstance().setActual(oldSession,ik);
					throw e;
				}
				catch(CardinalityExceedException e2){
					SessionController.getInstance().setActual(oldSession,ik);
					throw e2;
				}
			}
	
			//commit de todos los sessionables que hay dentro de esa session
			Iterator<Sessionable> itr=sesionables.iterator();
			while(itr.hasNext())
			{
						itr.next().commit(this);
						
			}
	
			// Avisar al padre de q he cerrado
			boolean success=false;
			//System.err.println("Antes de childSessionClosed: sesion:"+this.ID+" "+sesionables.size());
			HashMap<Integer,HashSet<Integer>> oldLocks=new HashMap<Integer, HashSet<Integer>>();
			oldLocks.putAll(locks);
			HashMap<Integer,HashSet<Integer>> oldPendingLocks=new HashMap<Integer, HashSet<Integer>>();
			oldPendingLocks.putAll(pendingLocks);
			LinkedHashMap<String,Reservation> oldReservationList=new LinkedHashMap<String,Reservation>();
			oldReservationList.putAll(reservationList);
			HashMap<Integer,EmailRequest> oldEmailRequestMap=new HashMap<Integer, EmailRequest>();
			oldEmailRequestMap.putAll(emailRequestMap);
			HashSet<String> oldNoticeMessages=new HashSet<String>();
			oldNoticeMessages.addAll(noticeMessages);
			ArrayList<ExecuteActionRequest> oldExecuteActionRequestList=new ArrayList<ExecuteActionRequest>();
			if(executeActionListener==null){
				oldExecuteActionRequestList.addAll(executeActionRequestList);
			}
			
			try{
				SessionController.getInstance().getSession(this.IDMadre).addLocks(locks);
				locks.clear();//Lo limpiamos para que no 
				SessionController.getInstance().getSession(this.IDMadre).addPendingLocks(pendingLocks);
				pendingLocks.clear();
				SessionController.getInstance().getSession(this.IDMadre).addReservations(reservationList);
				reservationList.clear();
				SessionController.getInstance().getSession(this.IDMadre).addEmailRequests(emailRequestMap);
				emailRequestMap.clear();
				SessionController.getInstance().getSession(this.IDMadre).addNoticeMessages(noticeMessages);
				noticeMessages.clear();
				if(executeActionListener==null){//Si tenemos listener en esta sesion no lo propagamos al padre ya que lo procesaremos aqui
					SessionController.getInstance().getSession(this.IDMadre).addRequestExecuteActionList(executeActionRequestList);
					executeActionRequestList.clear();
				}
				//if(SessionController.getInstance().getSession(this.IDMadre)!=null)
				SessionController.getInstance().getSession(this.IDMadre).childSessionClosed(this.ID,true,false,childPossibleRunDDBB);
				
	//			Si no es la session Madre, poner la session Madre como actual.
				if(!sesionables.isEmpty() && this.IDMadre !=-1 && !this.isForceParent()/*Si fuerza al padre este ya no existe por lo que no habria nada que avisar*/)
				{
					if(SessionController.getInstance().getSession(this.getIDMadre())!=null)
					{
						Session sessMadre=SessionController.getInstance().getSession(this.getIDMadre());
						
						//Evitamos que se notifiquen los cambios hasta que no hayamos hecho todos los avisaSession
						boolean restoreNotify=false;
						if(sessMadre.isNotifyChanges()){
							sessMadre.setNotifyChanges(false);
							restoreNotify=true;
						}
						try{
							Iterator<Sessionable> itrSessionables=sesionables.iterator();
							while(itrSessionables.hasNext())
							{							
								Sessionable f=itrSessionables.next();
								if(f instanceof Fact)
									((Fact)f).avisaSession(((Fact)f).getOrder(),sessMadre);
						
							}
						}finally{
							if(restoreNotify)
								sessMadre.setNotifyChanges(true);
						}
					}
				}//else// Session es session Root. 
				//	System.out.println("Imposible ya que lo tiene que hacer el IKnowledgeBaseInfo ");
				
				success=true;
			}finally{
				if(!success){
					locks=oldLocks;
					SessionController.getInstance().getSession(this.IDMadre).removeLocks(locks);
					pendingLocks=oldPendingLocks;
					SessionController.getInstance().getSession(this.IDMadre).removePendingLocks(pendingLocks);
					reservationList=oldReservationList;
					SessionController.getInstance().getSession(this.IDMadre).removeReservations(reservationList);
					emailRequestMap=oldEmailRequestMap;
					SessionController.getInstance().getSession(this.IDMadre).removeEmailRequests(emailRequestMap);
					noticeMessages=oldNoticeMessages;
					SessionController.getInstance().getSession(this.IDMadre).removeNoticeMessages(noticeMessages);
					if(executeActionListener==null){
						executeActionRequestList=oldExecuteActionRequestList;
						SessionController.getInstance().getSession(this.IDMadre).removeRequestExecuteActionList(executeActionRequestList);
					}
					//System.out.println("Antes de excepcion con sesionables:"+sesionables.size());
					//((DocDataModel)ik).getRuleEngine().getMotorSize();
					// rollback de todos los sessionables de esta sesion que habian sido propagados a la session padre
					//System.err.println("Antes de rollbackPropagation: sesion:"+this.ID+" "+sesionables.size());
					Iterator<Sessionable> itrSessionables=sesionables.iterator();
					while(itrSessionables.hasNext())
					{
						
						//Session sessionPadre=SessionController.getInstance().getSession(this.getIDMadre());
						//sesionables.get(i).rollBack(sessionPadre);
						Sessionable f=itrSessionables.next();
						f.rollBackOfPropagation(this);
						//sessionPadre.getSesionables().remove(sesionables.get(i));
	//					if(f instanceof Fact)
	//						((Fact)f).avisaSession(((Fact)f).getOrder(),this);
					}
					
					//temporalChangeProperty.removeAll(temporalChangeProperty);
					//notifyChanges=true;
					//System.err.println("WARNING:Rollback de los facts propagados");
					//System.err.println("Despues de rollbackPropagation: sesion:"+this.ID+" "+sesionables.size());
				}
			}
		}finally{
			setNotifyChanges(true);
			setState(this.USE_STATE);
			notifyExecuteAction();
			SessionController.getInstance().setActual(oldSession,ik);
		}
		
		//SessionController.getInstance().remove(this.ID,ik);
		
		//setState(this.FINISH_STATE);
		
		//notifyState(true);
		if(!reusable)
			dispose();
		
		//System.out.println("Despues de commit");
		//((DocDataModel)ik).getRuleEngine().getMotorSize();
		
		
		 //fin=System.currentTimeMillis();
		 //tiempos=(fin-inicio)/1000.0d;
		 //System.err.println("\n\n --time-->  FINAL COMMIT. tardo tiempo(segundos)="+tiempos+" ... hora actual="+new Date(System.currentTimeMillis()));
		
		return true;
	}
	
	protected void unLockLocalSessionables(HashSet<Sessionable> listSessionables,boolean runRules) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		DefaultSession session=new DefaultSession(ik,this,this.getUtask(),false,runRules,true,true,false);
		
		HashSet<Integer> idosUnLocked=new HashSet<Integer>();
		boolean success=false;
		try{
			Iterator<Sessionable> itr=listSessionables.iterator();
			while(itr.hasNext())
			{
				Sessionable sessionab=itr.next();
				
				if(sessionab instanceof Lock){
					Lock f=(Lock)sessionab;
					//System.err.println("Lock para intentar desbloquear:"+f);
					if(!idosUnLocked.contains(f.getIDO()) && f.getSTATE().equals(Constants.INDIVIDUAL_STATE_LOCK)){
						ik.setLock(f.getIDO(),false,Constants.USER_SYSTEM,session);
						idosUnLocked.add(f.getIDO());
					}
				}
			}
			
			if(!idosUnLocked.isEmpty() && runRules)//Si se ha desbloqueado alguno, puede ser que haya nuevos individuos creados por las reglas por lo que habria que desbloquearlos
				session.unLockLocalSessionables(session.getSesionables(),false);//Con false para evitar que se vuelvan a disparar reglas. Las que tenian que decir algo ya lo han dicho antes
			
			session.commit();
			success=true;
		}finally{
			if(!success){
				session.rollBack();
			}
		}
	}
	
	protected void runRulesDDBB() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		//System.err.println("\n\n ......HACEMOS RUN A BBDDRULES GROUP --------------->");
		DefaultSession session=new DefaultSession(ik,this,this.getUtask(),false,true,true,true,false);

		boolean success=false;
		try{
			SessionController.getInstance().setActual(session,ik);
			((DocDataModel)ik).ruleEngineRun(Constants.BDDRULESGROUP);
			//((DocDataModel)ik).ruleEngineRun(Constants.RULESGROUP_rules);
			session.commit();
			success=true;
		}finally{
			if(!success){
				session.rollBack();
			}
			SessionController.getInstance().setActual(this,ik);
		}
	}
	
	private HashSet<Integer> getIdosAncestorSessions(Session sess){
		HashSet<Integer>idosSessionPadre=new HashSet<Integer>();
		if(sess.getIDMadre()!=-1)
		{
			Session sessionParent=SessionController.getInstance().getSession(sess.getIDMadre());
			if(sessionParent.isCheckCoherenceObjects()){
				Iterator<Sessionable> itr=sessionParent.getSesionables().iterator();
				while(itr.hasNext()){
					Sessionable s=itr.next();
					if(s instanceof Fact)
						idosSessionPadre.add(((IPropertyDef)s).getIDO());
				}
				idosSessionPadre.addAll(getIdosAncestorSessions(sessionParent));
			}
		}
		
		return idosSessionPadre;
	}
	
	public void addSessionable(Sessionable s)
	{		
		this.sesionables.add(s);
		//System.out.println("->añadir Sessionable a :"+ this +" :"+s);
	}
	
	public String toString()
	{
		String resul = "[id = "+this.ID +" madre:"+this.IDMadre+"]";
		return resul;
	}
	
	public HashSet<Sessionable> getSesionables() {
		return sesionables;
	}
	
	public ArrayList <IChangePropertyListener> getChangeProp() {
		return this.changeProp;
	}
	public HashSet <IMessageListener> getMessagesListener() {
		return this.messagesListener;
	}
	
	public void childSessionClosed(int idChild,boolean commit,boolean createNewMotor,boolean possibleRunDDBB) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		//System.err.println("Soy la session con ID = "+ this.ID+"El hijo con ID =  "+idChild+" se ha cerrado");
		boolean success=false;
		boolean oldPossibleRunDDBB=this.possibleRunDDBB;
		try{
			this.possibleRunDDBB=possibleRunDDBB;
			childrenClosed.add(idChild);
			//TODO fallo formulario incrustado forceParent parece desconocer que tiene un formulario padre aunque es incrustado
			if(SessionController.getInstance().getSession(idChild).isForceParent()){
				 if(commit)
					 commit();
				 else{
					 this.createNewMotor=createNewMotor;
					 rollBack();
				 }
			 }
			success=true;
		}finally{
			if(!success){
				childrenClosed.remove(new Integer(idChild));
				this.possibleRunDDBB=oldPossibleRunDDBB;
			}
		}
	}
	
	
	public void setID(int id) {
		this.ID=id;		
	}
	
	public void initChangeValue() {
		// TODO Auto-generated method stub
		
	}
	
	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value oldValue, int level, int operation) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		ChangeProperty changeProp=new ChangeProperty();
		changeProp.changeValue(ido, idto, idProp, valueCls, value, oldValue, level, operation);
		temporalChangeProperty.add(changeProp);//Lo guardamos temporalmente para comunicarlos cuando se llame a notifyChange
		historyChangeProperty.add(changeProp);//Lo guardamos en el historial de cambios
		
		if(isNotifyChanges())
			notifyChanges();
	}
	
	public void endChangeValue() {
		// TODO Auto-generated method stub
		
	}
	
	public void setSessionable(HashSet t)
	{
		this.sesionables=t;
	}
	
	public Integer getUtask()
	{
		return utask;
	}
	
	public void setUtask(Integer utask)
	{
		this.utask = utask;
	}


	public boolean isCheckCoherenceObjects() {
		return checkCoherenceObjects;
	}


	public void setCheckCoherenceObjects(boolean checkCoherenceObjects) {
		this.checkCoherenceObjects = checkCoherenceObjects;
	}


	public boolean isLockObjects() {
		return lockObjects;
	}


	public void setLockObjects(boolean lockObjects) {
		this.lockObjects = lockObjects;
	}


	public boolean isRunRules() {
		return runRules;
	}


	public void setRunRules(boolean runRules) {
		this.runRules = runRules;
	}


	public void sendMessage(String message) {
		Iterator<IMessageListener> itm=this.messagesListener.iterator();
		if(messagesListener.isEmpty()){
			if(this.IDMadre !=-1)
			{
					if(SessionController.getInstance().getSession(this.getIDMadre())!=null)
					{
						Session sessMadre=SessionController.getInstance().getSession(this.getIDMadre());
						sessMadre.sendMessage(message);
					}
			}
					
		}else{
			while(itm.hasNext()){
				IMessageListener m=itm.next();
				m.sendMessage(message);
			}
		}
	}
	
	public Boolean sendQuestion(String message, boolean initialSelectionIsYes) {
		Iterator<IMessageListener> itm=this.messagesListener.iterator();
		Boolean response=null;
		if(messagesListener.isEmpty()){
			if(this.IDMadre !=-1)
			{
					if(SessionController.getInstance().getSession(this.getIDMadre())!=null)
					{
						Session sessMadre=SessionController.getInstance().getSession(this.getIDMadre());
						response=sessMadre.sendQuestion(message,initialSelectionIsYes);
					}
			}
					
		}else{
			while(itm.hasNext()){
				IMessageListener m=itm.next();
				Boolean responseAux=m.sendQuestion(message,initialSelectionIsYes);
				if(responseAux!=null){
					response=responseAux;
				}
			}
		}
		
		return response;
	}
	
	public boolean isFinished(){
		return state==this.FINISH_STATE;
	}


	public boolean isDeleteFilters() {
		return deleteFilters;
	}


	public void setDeleteFilters(boolean deleteFilters) {
		this.deleteFilters = deleteFilters;
	}
	
	public boolean soyHerederoDe(int idSessionPadre)
	{	
		if(idSessionPadre == this.getID())
			return true;
		if(this.getIDMadre()==-1)
			return  false;
		else
			if(this.getIDMadre()==idSessionPadre)
				return true;
			else
				return SessionController.getInstance().getSession(this.getIDMadre()).soyHerederoDe(idSessionPadre);
	
		
	}
	public boolean somosHermanos(int idHermano)
	{		
		return (!this.soyHerederoDe(idHermano) &&  !SessionController.getInstance().getSession(idHermano).soyHerederoDe(this.getID()));
	}


	public boolean isForceParent() {
		return forceParent;
	}


	public void setForceParent(boolean forceParent) {
		this.forceParent = forceParent;
	}


	//El parametro force obliga a que se chequee la coherencia de todos los idos encontrados aunque esten bloqueados
	public void checkCoherence(boolean force) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		
		/*HashSet<Integer>idosSessionPadre=getIdosAncestorSessions(this);*/
		
		HashSet<Integer>idosToCheck=new HashSet<Integer>();
		HashSet<Integer>idosIsolated=new HashSet<Integer>();
		HashSet<Integer>idosDeleted=new HashSet<Integer>();
		Iterator<Sessionable> itr=sesionables.iterator();
		while(itr.hasNext())
		{
			Sessionable sessionab=itr.next();
			if(sessionab instanceof Fact)
			{
				Fact f=(Fact)sessionab;
				if(f.getOP() == null&&f.getIDO()!=null&&!idosToCheck.contains(f.getIDO())&&f.getPROP()>0/*&&!idosSessionPadre.contains(f.getIDO())*/ &&ik.getLevelOf(f.getIDO()).intValue()!=Constants.LEVEL_FILTER &&!Auxiliar.pointToFilter(f,ik)){
					String state=((DocDataModel)ik).getLockState(f.getIDO());
					if(state.equals(Constants.INDIVIDUAL_STATE_READY) || force){//Solo chequeamos los desbloqueados. Los bloqueados ya se chequearan en las sesiones padre cuando esten desbloqueados
						boolean isolated=((DocDataModel)ik).isIsolated(f.getIDO());
						if(!isolated)//No chequeamos los que esten aislados
							idosToCheck.add(f.getIDO());
					}
					//if(!state.equals(Constants.INDIVIDUAL_STATE_READY) && !state.equals(Constants.INDIVIDUAL_STATE_DELETED))
					//	System.err.println("Fact a hacer el  check:" + f);
				}
			}
			else if(sessionab instanceof Individual){
				Individual f=(Individual)sessionab;
				if(f.getSTATE().equals(Constants.INDIVIDUAL_STATE_DELETED))
					idosDeleted.add(f.getIDO());
				else/* if(!idosSessionPadre.contains(f.getIDO()))*/{
					if(f.isISOLATED()){//No chequeamos los que esten aislados
						idosIsolated.add(f.getIDO());
					}
				}
			}
			else if(sessionab instanceof Lock){
				Lock f=(Lock)sessionab;
				if(f.getSTATE().equals(Constants.INDIVIDUAL_STATE_READY) || force)//Solo chequeamos los desbloqueados. Los bloqueados ya se chequearan en las sesiones padre cuando esten desbloqueados
					idosToCheck.add(f.getIDO());
				else if(idosToCheck.contains(f.getIDO()))
					idosToCheck.remove(f.getIDO());
			}
		}			
		idosToCheck.removeAll(idosDeleted);//Le quitamos los que han sido borrados ya que no tienen que ser chequeados
		idosToCheck.removeAll(idosIsolated);//Le quitamos los que estan aislados ya que no tienen que ser chequeados
		
		Iterator<Integer> itidoscheck=idosToCheck.iterator();
		((DocDataModel)ik).depthObjectProperty=false;
		try{
			while(itidoscheck.hasNext()){
				int ido=itidoscheck.next();
				//LOS PERMISOS YA SE HAN CHECKEADO PREVIAMENTE EN LOS SET A MOTOR.
				//TODO REVISAR USERROL LISTA SI SE PASA, DE MOMENTO SE PASAR NULO COMO USERROL
				ik.checkCoherenceObject(ido,null,((DocDataModel)ik).getUser(),getUtask(),this);
				
			}
		}finally{
			((DocDataModel)ik).depthObjectProperty=true;
		}
	}
	
		
	//Retorna true si ella o algun ancestro tiene bloqueado ese ido
	public boolean hasLockObject(int ido,int idto){
		boolean hasLockObject=locks.containsKey(idto) && locks.get(idto).contains(ido);
		if(!hasLockObject){
			if(this.IDMadre !=-1)
			{
				if(SessionController.getInstance().getSession(this.getIDMadre())!=null)
				{
					Session sessMadre=SessionController.getInstance().getSession(this.getIDMadre());
					hasLockObject=sessMadre.hasLockObject(ido,idto);
				}
			}
		}
		
		return hasLockObject;
	}


	//	Retorna true si otra session que no pertenece a la linea ancestral de esta tiene bloqueado ese ido
	public boolean hasExternalSessionLockObject(int ido,int idto){
		boolean hasLockObject=false;
		HashMap<Integer,Session> listSessions=SessionController.getInstance().getSessionsList(ik);
		//HashSet<Integer> setIdSession=new HashSet<Integer>();
		
		Iterator<Session> itr=SessionController.getInstance().getSessionsList().values().iterator();
		while(!hasLockObject && itr.hasNext()){
			Session session=itr.next();
			if(!listSessions.containsKey(session.getID())){
				hasLockObject=session.hasLockObject(ido,idto);
			}
		}
		return hasLockObject;
	}
	
	public void lockObject(int ido,int idto) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		if(lockObjects && !hasLockObject(ido,idto)){
			if(!hasExternalSessionLockObject(ido,idto)){//Si no esta bloqueado por otro arbol de sesiones
				//ik.getServer().lockObject(ido,idto,((DocDataModel)ik).getUser());
				
				HashSet<Integer> idos=pendingLocks.get(idto);
				if(idos==null){
					idos=new HashSet<Integer>();
					pendingLocks.put(idto, idos);
				}
				idos.add(ido);
				
				//this.idosLocked.put(ido,idto);
			}else{
				InstanceLockedException exception=new InstanceLockedException("No se puede bloquear el ido "+ido+" porque ya esta bloqueado por otra ventana de la aplicacion");
				exception.setUserMessage("No se puede modificar "+Utils.normalizeLabel(ik.getAliasOfClass(idto, getUtask()))+" '"+ik.getRdnIfExistInRuler(ido)+"' ya que se esta modificando en otra ventana de la aplicacion");
				throw exception;
			}
		}
			
	}
	
	public void sendPendingLocks() throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		if(!pendingLocks.isEmpty()){
			ik.getServer().lockObject(pendingLocks,((DocDataModel)ik).getUser());
			locks.putAll(pendingLocks);
			pendingLocks.clear();
		}
	}


	public void unlockObjects() throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException {
		if(!locks.isEmpty()){
			ik.getServer().unlockObjects(locks,((DocDataModel)ik).getUser());			
			locks=new HashMap<Integer, HashSet<Integer>>();
		}
	}


	public void addLocks(HashMap<Integer,HashSet<Integer>> locks) {
		for(Integer idto:locks.keySet()){
			HashSet<Integer> idos=this.locks.get(idto);
			if(idos==null){
				this.locks.put(idto,locks.get(idto));
			}else{
				idos.addAll(locks.get(idto));
			}
		}
	}


	public void removeLocks(HashMap<Integer,HashSet<Integer>> locks) {
		Iterator<Integer> itr=locks.keySet().iterator();
		while(itr.hasNext()){
			this.locks.remove(itr.next());
		}
	}
	
	public void addPendingLocks(HashMap<Integer,HashSet<Integer>> pendingLocks) {
		for(Integer idto:pendingLocks.keySet()){
			HashSet<Integer> idos=this.pendingLocks.get(idto);
			if(idos==null){
				this.pendingLocks.put(idto,pendingLocks.get(idto));
			}else{
				idos.addAll(pendingLocks.get(idto));
			}
		}
	}


	public void removePendingLocks(HashMap<Integer,HashSet<Integer>> pendingLocks) {
		Iterator<Integer> itr=pendingLocks.keySet().iterator();
		while(itr.hasNext()){
			this.pendingLocks.remove(itr.next());
		}
	}


	public boolean isNotifyChanges() {
		return notifyChanges;
	}


	public void setNotifyChanges(boolean notifyChanges) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		this.notifyChanges = notifyChanges;
		if(notifyChanges && !ik.isDispose())
			notifyChanges();
	}

	public void addISessionStateListener(ISessionStateListener a) {
		this.stateListener.add(a);
	}

	public void removeISessionStateListener(ISessionStateListener a) {
		this.stateListener.remove(a);
	}
	
	public void notifyState(boolean commit){
		Iterator<ISessionStateListener> itr=stateListener.iterator();
		while(itr.hasNext()){
			ISessionStateListener s=itr.next();
			s.sessionClosed(this, commit);
		}
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getRulesGroup() {
		return rulesGroup;
	}

	public void setRulesGroup(String rulesGroup) {
		this.rulesGroup = rulesGroup;
	}

	public IKnowledgeBaseInfo getKnowledgeBase() {
		return ik;
	}

	@Override
	public boolean equals(Object o) {
		Session s=(Session)o;
		return this.ID==s.getID();
	}

	public boolean isReusable() {
		return reusable;
	}

	public void setReusable(boolean reusable) {
		this.reusable = reusable;
	}
	
	public void dispose(){
		/*System.err.println("****dispose:"+this.ID);
		Auxiliar.printCurrentStackTrace();*/
		Exception ex=new Exception();
		trace=ex.getStackTrace();
		SessionController.getInstance().remove(this.ID,ik);
		setState(this.FINISH_STATE);
		notifyState(true);
		sesionables=null;
		//ik=null;//Necesario para que el nucleo sepa que esta dispose
		//changeProp=null;//Necesario porque hay algun caso en el que se hace dispose por otro lado mientras se esta haciendo el aviso de los changeProp
		messagesListener=null;
		stateListener=null;
		historyChangeProperty=null;
		temporalChangeProperty=null;
	}
	
	/**
	 * Comprueba que subiendo por los padres termina en una sesion que va a base de datos. Ademas si esta sesion ya va directamente a base de datos tambien devuelve true
	 * @return
	 */
	public boolean isChildOfDDBBSession(){
		Session sess=this;
		while(!(sess instanceof DDBBSession) && sess.getIDMadre()!=-1){
			sess=SessionController.getInstance().getSession(sess.getIDMadre());
		}
		return sess instanceof DDBBSession;
	}
	
	public Session getDDBBSession(){
		Session sess=this;
		while(!(sess instanceof DDBBSession) && sess.getIDMadre()!=-1){
			sess=SessionController.getInstance().getSession(sess.getIDMadre());
		}
		return (sess instanceof DDBBSession?sess:null);
	}

	@Override
	public HashMap<DomainProp, Double> sendReservations(LinkedHashMap<String,Reservation> reservationList) throws SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException{
		//System.err.println("Enviar reservas:"+reservationList);
		HashMap<DomainProp, Double> result=ik.getServer().reserve(new ArrayList<Reservation>(reservationList.values()), ik.getUser(),getDDBBSession().getID());
		this.reservationList.putAll(reservationList);
		return result;
	}

	@Override
	public void cancelReservations() throws SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException{
		if(!reservationList.isEmpty()){
			//System.err.println("Cancelar reservas:"+reservationList);
			ik.getServer().deleteReservation(new ArrayList<Reservation>(reservationList.values()), ik.getUser(), getDDBBSession().getID());
			reservationList=new LinkedHashMap<String, Reservation>();
		}
	}

	@Override
	public void addReservations(LinkedHashMap<String, Reservation> reservationList) {
		this.reservationList.putAll(reservationList);
	}

	@Override
	public void removeReservations(LinkedHashMap<String, Reservation> reservationList) {
		for(String id:reservationList.keySet()){
			this.reservationList.remove(id);
		}
	}
	
	private boolean isProcessingRemoveMotor(){
//		boolean force=this.forceParent;
//		Session sess=this;
//		while(force && !(sess instanceof DDBBSession) && sess.getIDMadre()!=-1){
//			force=sess.isForceParent();
//			sess=SessionController.getInstance().getSession(sess.getIDMadre());
//		}
//		return force && sess instanceof DDBBSession && !sess.isReusable();
		return false;
	}

	@Override
	public void addEmailRequests(HashMap<Integer, EmailRequest> emailRequestMap) {
		this.emailRequestMap.putAll(emailRequestMap);
	}

	@Override
	public void removeEmailRequests(HashMap<Integer, EmailRequest> emailRequestMap) {
		for(Integer ido:emailRequestMap.keySet()){
			this.emailRequestMap.remove(ido);
		}
	}

	@Override
	public void addEmailRequest(EmailRequest emailRequest) {
		this.emailRequestMap.put(emailRequest.getIdo(), emailRequest);
	}
	
	@Override
	public void removeNoticeMessages(HashSet<String> noticeMessages) {
		for(String message:noticeMessages){
			this.noticeMessages.remove(message);
		}
	}

	@Override
	public void addNoticeMessage(String noticeMessage) {
		this.noticeMessages.add(noticeMessage);
	}

	@Override
	public void addNoticeMessages(HashSet<String> noticeMessages) {
		this.noticeMessages.addAll(noticeMessages);
		
	}
	
	@Override
	public void notifyNoticeMessage(String message) {
		for(INoticeListener noticeListener:((DocDataModel)ik).getNoticeListeners()){
			 noticeListener.setText(message);
		 }
	}
	
	@Override
	public IExecuteActionListener getExecuteActionListener() {
		return executeActionListener;
	}

	@Override
	public void setExecuteActionListener(IExecuteActionListener executeActionListener) {
		this.executeActionListener = executeActionListener;
	}

	@Override
	public void requestExecuteAction(ExecuteActionRequest executeActionRequest) {
		executeActionRequestList.add(executeActionRequest);
	}
	
	@Override
	public void notifyExecuteAction(){
		if(executeActionListener!=null){
			for(ExecuteActionRequest executeAction:executeActionRequestList){
				executeActionListener.requestExecuteAction(executeAction);
			}
			executeActionRequestList.clear();
		}
	}

	@Override
	public void addRequestExecuteActionList(ArrayList<ExecuteActionRequest> executeActionRequestList) {
		this.executeActionRequestList.addAll(executeActionRequestList);
	}

	@Override
	public void removeRequestExecuteActionList(ArrayList<ExecuteActionRequest> executeActionRequestList) {
		this.executeActionRequestList.removeAll(executeActionRequestList);
	}
	
}
