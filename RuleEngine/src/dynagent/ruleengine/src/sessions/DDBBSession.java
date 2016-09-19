package dynagent.ruleengine.src.sessions;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.ObjectChanged;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.docServer;
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
import dynagent.common.knowledge.IChangeServerListener;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IPropertyDef;
import dynagent.common.knowledge.KnowledgeAdapter;
import dynagent.common.knowledge.action;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.Session;
import dynagent.common.sessions.Sessionable;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.Utils;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.ruler.DatValue;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.ruler.FactAccess;
import dynagent.ruleengine.src.ruler.Individual;
import dynagent.ruleengine.src.ruler.JBossEngine;
import dynagent.ruleengine.src.ruler.Lock;
import dynagent.ruleengine.src.ruler.SessionRecord;
import dynagent.ruleengine.src.ruler.ERPrules.datarules.DataRules;

public class DDBBSession extends DefaultSession {
	
	DocDataModel ddm;
	
	//Listeners para envios a base de datos. Son estaticos porque nos interesa que cada sesion que envie a base de datos avise a todos los listeners independiendemente de la sesion en la que nos encontremos
	protected static ArrayList<IHistoryDDBBListener> historyDDBBListeners=new ArrayList<IHistoryDDBBListener>();
	protected static ArrayList<IChangeServerListener> changeServerListeners=new ArrayList<IChangeServerListener>();
	
	
	public DDBBSession(DocDataModel ddm,boolean checkCoherenceObjects, boolean runRules, boolean lockObjects, boolean deleteFilters, boolean reusable)
	{
		super(ddm,null,null,checkCoherenceObjects,runRules,lockObjects,deleteFilters,false);
		this.ddm=ddm;
		this.reusable=reusable;
	}
	
	
//	public void childSessionClosed(int id,boolean commit,boolean createNewMotor) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
//		System.err.println("commint:"+commit);
//		Auxiliar.printCurrentStackTrace(); 
//		if (/*this.getSesionables().size() != 0 && */commit)
//			 this.commit();
//		 else this.rollBack();
//	 }
	
	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 // cogemos todos los facts que han cambiado y los guardamos en una lista
		 // temporal, los facts que tienen OP distinto de null, no serin tenidos
		 // en cuenta
		 //System.err.println("\n ddm:  INICIO COMMIT: "+Auxiliar.getFechaActual());
		 double inicio,fin,tiempos;
		 inicio=System.currentTimeMillis();
		 
		 // TODO TRAS HACER COMMIT SI TODO VA BIEN,SE BORRAN LOS FACTS -RETRACT---
			
		 if(!reusable)
			 setState(this.FINISHING_STATE);
		 
		 boolean isLockObjects=SessionController.getInstance().getActualSession(ddm).isLockObjects();
		 boolean isCheckCoherenceByChild=SessionController.getInstance().getActualSession(ddm).isCheckCoherenceObjects();
		 
		 Session oldSession=SessionController.getInstance().getActualSession(ddm)!=this?SessionController.getInstance().getActualSession(ddm):null;//Comprobamos que no sea ella misma ya que setActual del final volveria a añadirla a sessionList
		 SessionController.getInstance().setActual(this,ddm);
		 try{
	
			 //System.out.println("COMMIT: Llamada a commit del DOCDATAMODEL"	+ this.getSesionables().size() + "Sessionables");
			 //System.err.println("********* Commit DocDataModel**********");
			 //getRuleEngine().getMotorSize();
			 //Date now=new Date(System.currentTimeMillis());
			 //System.err.println("\n        ************Antes todo *******    "+now);
			 
			 Integer idoUTask = null;
			 Integer idtoUTask = null;
			 if (this.getSesionables().size() > 0) {
				 LinkedList<dynagent.common.knowledge.IPropertyDef> facts = new LinkedList<dynagent.common.knowledge.IPropertyDef>();
				 Iterator it = this.getSesionables().iterator();
				 ArrayList<Integer> objectsToDelete = new ArrayList<Integer>();
				 ArrayList<Integer> prototypesDeleted = new ArrayList<Integer>();
				 ArrayList<FactInstance>factsObjectsToDelete=new ArrayList<FactInstance>();
				 ArrayList<Integer> objectsIsolated=new ArrayList<Integer>();
				 //array con los identificadores de objetos de los que habra comprobar que estan bien informados.
				 HashSet<Integer>idosToCheck=new HashSet<Integer>();
				 HashMap<Integer,Individual>idosIndividualToUpdate=new HashMap<Integer,Individual>();
				 final HashSet<Integer> idosToHoldInMotor=new HashSet<Integer>();//Idos creados que deben permanecer en motor
				 HashMap<Integer,String> idosWithDestinationSystem=new HashMap<Integer, String>();
				 ArrayList<FactInstance> factsNewFile=new ArrayList<FactInstance>();
				 HashSet<Integer> idosPrototypes=new HashSet<Integer>();
				 
				 HashMap<Integer,String> rdnMap=new HashMap<Integer,String>();
				 while (it.hasNext()) {
					 Object sig = it.next();
					 if(sig instanceof Individual){
						 Individual ind= (Individual)sig;
	
						 if(ind.getLEVEL()==Constants.LEVEL_PROTOTYPE){
							 idosPrototypes.add(ind.getIDO());
						 }
						 
						 if(ind.getSTATE().equals(Constants.INDIVIDUAL_STATE_DELETED)){
							 if(ind.getLEVEL()==Constants.LEVEL_INDIVIDUAL){
								 objectsToDelete.add(ind.getIDO());
								 FactInstance fd=new FactInstance(ind.getIDTO(),ind.getIDO(),Constants.IdPROP_OBJECTDELETED,null,Constants.IDTO_BOOLEAN,Constants.DATA_BOOLEAN,null,null,null,ind.getCLASSNAME());
								 fd.setOrder(action.DEL_OBJECT);
								 fd.setExistia_BD(true);
								 fd.setDestinationSystem(ind.getDestinationSystem());
								 factsObjectsToDelete.add(fd);
							 }else{
								 prototypesDeleted.add(ind.getIDO());
							 }
						 }else if(ddm.isSpecialized(ind.getIDTO(),Constants.IDTO_PARAMS)||ddm.isSpecialized(ind.getIDTO(),Constants.IDTO_AUX_PARAMS)||ind.getIDTO()==Constants.IDTO_QUESTION_TASK||ind.getIDTO()==Constants.IDTO_RESULT_BATCH||ddm.isSpecialized(ind.getCLASSNAME(),Constants.CLS_CLASIFICATION)){//Excluyo los que sean especializados de parametros o sean question ya que estos no deben ir a base de datos
							 prototypesDeleted.add(ind.getIDO());
						 }else if(ind.isISOLATED()){//Excluyo tambien los que estan aislados ya que no deben chequearse ni enviar a base de datos
							 objectsIsolated.add(ind.getIDO());
						 }
						 //System.err.println("Individual:"+ind);
						 //System.err.println("SessionesIndividual:"+ind.getSessionsRecord());
						 
						 // Individuos que estan en varias sesiones. Se almacena ya que se trataran actualizando el valor en esas sesiones y manteniendo los facts sin hacer retract.
						 // Tener en cuenta que puede estar cargado en alguna ventana pero sin tener fact individual porque no este siendo modificado por lo que para las multiventanas
						 // no funcionaria siempre correctamente
						 if(ind.getSessionsRecord().size()>1){
							 //System.err.println("ind.getSessionsRecord().size()>1 :"+ind+" con sesiones:"+ind.getSessionsRecord());
							 boolean found=false;
							 for(SessionRecord sessionRecord:ind.getSessionsRecord()){
								 if(sessionRecord.getIdSession()==ik.getDefaultSession().getID())
									 found=true;
							 }
							 if(found){
								 //System.err.println("idosIndividualUpdate "+ind);
								 idosIndividualToUpdate.put(ind.getIDO(),ind);
							 }
						 }
					 }
					 else 
					 if (sig instanceof dynagent.common.knowledge.IPropertyDef) {
						 dynagent.common.knowledge.IPropertyDef f = (dynagent.common.knowledge.IPropertyDef) sig;
						 if (f.getPROP()==Constants.IdPROP_TARGETCLASS){//getIDO()!=null && f.getVALUE()!=null && f.getVALUE().equals(Constants.IDO_REALIZADO)){
							 idoUTask = f.getIDO();
							 idtoUTask= f.getIDTO();
						 }
						 if(f.getPROP()==Constants.IdPROP_RDN){
							 rdnMap.put(f.getIDO(), f.getVALUE());
						 }
						 //excluimos los facts con prop<0 pq se usaran para calculos auxiliares en reglas.
						 //System.err.println("Fact docDataModel:"+f);
						 if(f.getOP() == null&&f.getIDO()!=null&&ddm.getLevelOf(f.getIDO()).intValue()!=Constants.LEVEL_FILTER &&!Auxiliar.pointToFilter(f,ddm)){
							 //NO INTERESA MANDAR AL SERVIDOR:
							 //- NI LOS FILTROS
							 //NI LOS VALORES QUE APUNTANT A UN FILTRO
							 //-NI LAS DEFINICIONES DE CLASES
							 int order=f.getOrder();
							 if (f.initialValuesChanged()){
								 if(order==action.SET&&f.getExistia_BD()){
									 FactInstance fm = f.toFactInstance();
									 fm.setInitialValues(f.getInitialValues().toFactInstance());
									 idosToCheck.add(f.getIDO());
									 if(fm.getVALUECLS().equals(Constants.IDTO_IMAGE) || fm.getVALUECLS().equals(Constants.IDTO_FILE))
										 factsNewFile.add(fm);
									 facts.add(fm);
	
								 }
								 else if(order==action.NEW){
									 FactInstance fm = f.toFactInstance();
									 idosToCheck.add(f.getIDO());
									 if(ddm.isSpecialized(f.getIDTO(), Constants.IDTO_CONFIGURATION) || ddm.isSpecialized(f.getIDTO(), Constants.IDTO_ENUMERATED))//Necesitamos que siempre permanezca en motor
										 idosToHoldInMotor.add(f.getIDO());
									 if(fm.getVALUECLS().equals(Constants.IDTO_IMAGE) || fm.getVALUECLS().equals(Constants.IDTO_FILE))
										 factsNewFile.add(fm);
									 facts.add(fm);
	
								 }else if(order==action.DEL ){
									 FactInstance fi = (FactInstance) f.getInitialValues();
									 fi.setAppliedSystemValue(f.isAppliedSystemValue());
									 fi.setSystemValue(f.getSystemValue());
									 facts.add(fi);
								 }
							 }
							 if(f.getDestinationSystem()!=null){
								 idosWithDestinationSystem.put(f.getIDO(), f.getDestinationSystem());
							 }
						 }
					 }
							 
					 
				 }
				 
				 /*No debe llamarse al commit de esta session directamente, por lo que de esto ya se encarga DefaultSession. Si lo hacemos aqui seria un problema al intentar deshacer un error
				  * if(possibleRunDDBB){
					unLockLocalSessionables(getSesionables(),true);//Con true para que al desbloquear se puedan disparar las reglas del grupo rules
					runRulesDDBB();//Run solo a las reglas del grupo bbddrules. Son las ultimas que tienen algo que decir antes del envio a base de datos
					unLockLocalSessionables(getSesionables(),false);//Con false para evitar que al desbloquear se pudiera disparar alguna regla del grupo rules
				 }*/
				 
				 if(!isCheckCoherenceByChild && isCheckCoherenceObjects()){
					 idosToCheck.removeAll(objectsToDelete);
					 idosToCheck.removeAll(objectsIsolated);
					 idosToCheck.removeAll(prototypesDeleted);
					 Iterator<Integer> itidoscheck=idosToCheck.iterator();
					 while(itidoscheck.hasNext()){
						 int ido=itidoscheck.next();
						 //LOS PERMISOS YA SE HAN CHECKEADO PREVIAMENTE EN LOS SET A MOTOR.
						 //TODO REVISAR USERROL LISTA SI SE PASA, DE MOMENTO SE PASAR NULO COMO USERROL
						 try{
							 ddm.checkCoherenceObject(ido,/*this.getUserRoles()*/null,ddm.getUser(),getUtask()/*Esto es nulo, pero tenerlo en cuenta por si deberia no serlo, aunque parece que nunca entra aqui de momento porque la sesion hija suele ser CheckCoherence*/);
						 }
						 catch(IncompatibleValueException e){
							 SessionController.getInstance().setActual(oldSession,ddm);
							 throw e;
						 }
						 catch(CardinalityExceedException e2){
							 SessionController.getInstance().setActual(oldSession,ddm);
							 throw e2;
						 }
	
					 }
				 }
				 				 
				 docServer localServer=ddm.getLocalServer();
				 docServer defServer=ddm.getServer();
				 String replicaSource=null;//relleno con replica ficticia para que server pase por setLocalIdo, asi crea si no existe una referencia como un ticket rectificado
				 boolean externalServer=localServer!=null && !defServer.equals(localServer);
				 
				 if(externalServer) replicaSource=Constants.GLOBAL_URL;
				 
				 // Los facts importantes representar solamente los facts que hay que tener en cuenta para mandar al servidor. Si un objeto ha sido borrado no mandamos todos sus
				 //facts con operacion deleted sino que mandamos unicamente uno que representa al objeto con la operacion DEL_OBJECT
				 ArrayList<dynagent.common.knowledge.IPropertyDef> factsImportantes = new ArrayList<dynagent.common.knowledge.IPropertyDef>();
				 for (int i = 0; i < facts.size(); i++) {
					 IPropertyDef f=facts.get(i);
					 boolean discard=false;
					 if(objectsToDelete.contains(f.getIDO()))//excluimos los facts de objetos que se han borrado pq ya se indica esa informacion en la lista de factsdeobjetos borrados
						 discard=true;
					 else if(objectsIsolated.contains(f.getIDO()))
						 discard=true;
					 else if(prototypesDeleted.contains(f.getIDO()))
						 discard=true;
					 else if ( f.getVALUE() != null	&& Auxiliar.hasIntValue(f.getVALUE()) && ddm.isObjectProperty(f.getPROP()) && prototypesDeleted.contains(Integer.valueOf(f.getVALUE()))) {
						 	//Esto ocurre cuando se borra un prototipo pero el enlace no es borrado por deleteObject porque es una inversa
						 	discard=true;
					 }else if(f.getIDO()<0 && !idosPrototypes.contains(f.getIDO())){//Si se trata de un prototipo y no existe el individual en esta sesion lo descartamos (fallaba con los tickets incrustados al hacer una operacion en otra usertask)
						 System.err.println("WARNING: Fact descartado al enviar a base de datos ya que no tiene individual en la sesion.\n"+f);
						 discard=true;
					 }
					 else{		 
						 Integer systemValue;
						 try {
							 systemValue=Integer.parseInt(f.getSystemValue());
						 } catch (NumberFormatException nfe){
							 systemValue=null;
						 }
						 if(systemValue!=null && ddm.isObjectProperty(f.getPROP()) && (objectsToDelete.contains(systemValue) || prototypesDeleted.contains(systemValue))){//Si el systemValue apunta a un valor borrado
							 if(f.getVALUE()==null && f.getQMIN()==null && f.getQMAX()==null)//Si tiene el valor borrado evitamos que se envie
								 discard=true;
							 else{//Quitamos del systemValue el valor borrado
								 f.setSystemValue(null);
								 f.setAppliedSystemValue(false);
							 }
						 }
					 }
					 
					 if(!discard){	
						 if(externalServer){
							 String rdn=rdnMap.get(f.getIDO());
							 if(rdn!=null && f instanceof FactInstance){
								 ((FactInstance)f).setRdn(rdn);
							 }
							 if(ddm.isObjectProperty(f.getPROP()) &&  f instanceof FactInstance){
								 Integer value=Integer.parseInt(f.getVALUE());							 
								 rdn=rdnMap.get(value);
								 ((FactInstance)f).setRdnValue(rdn);
							 }
						 }
						 factsImportantes.add(f);
					 }
				 }
				 factsImportantes.addAll(factsObjectsToDelete);
				 //System.out.println("Insertar en la base de datos instances="+ factsImportantes);
	
				 final HashMap<Integer,Integer> idosToLoad=new HashMap<Integer,Integer>();//Idos que se cargaran en motor al terminar la sesion
				 final HashMap<Integer,Integer> idosChanged=new HashMap<Integer, Integer>();
				 if(!factsImportantes.isEmpty()){
					 if (ddm.getServer()!= null){
						 String destination = null;
						 /*if (idoUTask!=null) {
							 Property p = this.getProperty(idoUTask, Constants.IdPROP_TARGETCLASS);
							 if (this.getClassName(p.getIdto()).equals(Constants.Task_PLANIFICACION)){
								 destination = "APS";
							 }
						 }*/
						 
						 for(FactInstance f:factsNewFile){
							 f.setVALUE(ddm.getServer().serverUploadFile(f.getVALUE(),f.getVALUECLS()));
						 }
						 
						/*System.err.println("\n\n============================================================");
						 System.err.println("\n\n=============SE MANDAN AL SERVER: numeroFacts="+factsImportantes.size()+" ==============");
						 for(int i=0;i<factsImportantes.size();i++){
							 System.err.println("Fact numero "+i+":\n"+factsImportantes.get(i));
						 }
						 System.err.println("\n\n============================================================");
						 */
						 //now=new Date(System.currentTimeMillis());
						 //System.err.println("\n        ************Antes envio *******    "+now);
						 String msguid="#ID:"+ddm.getUser()+"#"+ddm.getCreationDate()+"#"+childrenClosed.get(childrenClosed.size()-1);//Para que si se guardara bien en base de datos pero luego fallara algo de la sesion, evitar que se vuelvan a enviar los mismos datos a base de datos si el usuario le vuelve a dar a aceptar
						 						 
						 Changes changesServer=ddm.getLocalServer().serverTransitionObject(null,ddm.getUser(),/*destination,*/new IndividualData(factsImportantes,new ArrayList<Reservation>(reservationList.values()),ik),getID(), false, false, replicaSource, msguid);
						 
						 final HashMap<Integer,HashMap<Integer,Value>> propertiesChanged=new HashMap<Integer, HashMap<Integer,Value>>();
						 
						 //Nos registramos como ChangeServerListener porque necesitamos saber el ido que se le ha asignado en el server para asi poder cargarlo
						 IChangeServerListener changeServer=new IChangeServerListener(){

							public void changeServerValue(int ido, Integer oldIdo, Integer idProp, Value value, Value oldValue) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
								if(idosToHoldInMotor.contains(oldIdo)){
									idosToLoad.put(ido,ddm.getClassOf(oldIdo));
								//	idosToHoldInMotor.remove(oldIdo);
								}
								if(oldIdo!=null)
									idosChanged.put(oldIdo, ido);
								
								if(!propertiesChanged.containsKey(oldIdo)){
									propertiesChanged.put(oldIdo, new HashMap<Integer,Value>());
								}
								
								propertiesChanged.get(oldIdo).put(idProp, value);
							}
							 
						 };
//						 now=new Date(System.currentTimeMillis());
//						 System.err.println("\n        ************Antes notifyChangesServer *******    "+now);
						 addChangeServerListener(changeServer);
						 notifyChangesServer(changesServer);
						 this.removeChangeServerListener(changeServer);
						 //idosToLoad.addAll(idosToHoldInMotor);
//						 now=new Date(System.currentTimeMillis());
//						 System.err.println("\n        ************Antes notifyHistoryDDBB *******    "+now);
						 notifyHistoryDDBB(changesServer,factsImportantes,idtoUTask,oldSession);
						 
						 for(int ido:emailRequestMap.keySet()){
								EmailRequest emailRequest=emailRequestMap.get(ido);
								Integer idoUserTaskReport=null;
								Integer idtoUserTaskReport=emailRequest.getIdtoReport();
								if(idtoUserTaskReport!=null){
									idoUserTaskReport=ik.getIndividualsOfLevel(idtoUserTaskReport, Constants.LEVEL_FILTER).iterator().next();
								}

								if(idosChanged.containsKey(emailRequest.getIdo())){
									emailRequest.setIdo(idosChanged.get(emailRequest.getIdo()));
								}
								if(idosChanged.containsKey(emailRequest.getIdoDestinatario())){
									emailRequest.setIdoDestinatario(idosChanged.get(emailRequest.getIdoDestinatario()));
								}
								try{
									emailRequest.replaceBodyCode(propertiesChanged);
									ik.sendEmail(idoUserTaskReport,idtoUserTaskReport,emailRequest,true);
								}catch(Exception ex){
									ex.printStackTrace();
								}
							}
						 
						 notifyNoticeListener();
					 }
				 }
				 
				 unlockObjects();
				 //cancelReservations(); No cancelamos ya que en el mismo envio a serverTransitionObject se esta indicando las reservas a cancelar
				 
				 if(!reusable){
					//Actualizamos otros motores, si es necesario, con los facts que se han enviado a base de datos
					updateOtherKnowledgeBase(factsImportantes, idosChanged, idosToLoad);
					
					ik.dispose();//Este dispose se encarga de hacer dispose de esta sesion
					//unlockObjects();
				 }else{
	//				 now=new Date(System.currentTimeMillis());
	//				 System.err.println("\n        ************Antes Retract *******    "+now);
					 //if(this.session.getCategory()==Session.APPLET_SESSION){//Si estamos en el applet se borran los facts de la sesion
					 ArrayList<Sessionable> factssesionables=new ArrayList<Sessionable>();
					 factssesionables.addAll(this.getSesionables());//Para que al hacer retractFact no de concurrentModification
	
					 Thread counter=null;
						if(!createNewMotor){
							// Thread utilizada para calcular el tiempo que esta tardando la limpieza. Si es superior a una cantidad se indica que se cree un motor nuevo
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
						
					 HashSet<Integer> idosToRemove=new HashSet<Integer>();
					 HashMap<Integer,Individual> individualRollbackRetractable=new HashMap<Integer,Individual>();
					 HashMap<Integer,Lock> lockRollbackRetractable=new HashMap<Integer,Lock>(); 
					 HashSet<Integer> idosToForceRemove=new HashSet<Integer>(); 
					 HashSet<Fact> factsUpdated=new HashSet<Fact>();
					 Iterator<Sessionable> itrSessionables=factssesionables.iterator();
					 while(itrSessionables.hasNext()){
						 Object f=itrSessionables.next();
						 if(f instanceof Fact)
						 {
							 Fact fact=((Fact)f);
							 //FactInstance factI=((FactInstance)factsImportantes.get(i));
			
							 Integer ido=fact.getIDO();
							 if(ido!=null){
								 if(idosIndividualToUpdate.get(ido)==null){
									 
									// if(fact.getLEVEL()==Constants.LEVEL_INDIVIDUAL && fact.initialValuesChanged())
									//	 setInOtherKnowledgeBase(fact, idosChanged);//Actualizamos en otros motores, tenemos que hacerlo tanto con order new,set o del
									 
									 if(fact.getSessionValues().size()==1){
										 //System.err.println("Fact para retract:"+fact);
										 ddm.retractInfoFact(fact,!createNewMotor);
										 Integer idto=fact.getIDTO();
										 if(!ddm.isSpecialized(idto, Constants.IDTO_UTASK) && (!ddm.isSpecialized(idto, Constants.IDTO_ENUMERATED) || ddm.getLevelOf(ido)==Constants.LEVEL_PROTOTYPE))//Las userTask y enumerados nunca se borran porque son reutilizables
										 {	//Si se esta creando un enumerado tenemos que borrar el prototipo porque luego se cargara mas abajo el ido correcto
											 idosToRemove.add(ido);
										 }
									 }
									 /*else{
										 System.err.println("WARNING:Fact en commit del DocDataModel sin hacer retract ya que posee valor en otra sesion f:"+fact);
									 }*/
								 }else{
									 //Actualizamos el valor de este fact en la sesion donde se creo el individual
									 
									 //System.err.println("Antes update:"+fact);
									 
									 //Creamos un nuevo fact ya que al cambiarlo en base de datos debemos crearlo como si lo cargaramos con getFromServer.
									 //Esto es necesario porque por ejemplo valor inicial y existeBD cambian a lo que tiene ahora mismo el fact.
									 //Con action.DEL no lo hacemos porque no nos sirve de nada un fact con valores nulos
									 if(fact.getOrder()==action.NEW || fact.getOrder()==action.SET){
	
	
	//TODO FRAN REVISAR PROBLEMAS ISINCREMENTAL Y CUENTA CONTABLE PROPAGACION: No lo descomentamos, ya que si es incremental lo metemos como un fact normal y no hay problema, ya que se hace retract del fact incremental 
	//if(! (fact instanceof DatValue && ((DatValue)fact).isIncremental())){
											 FactInstance fInst=fact.toFactInstance();
											 fInst.setExistia_BD(true);
											 if(idosChanged.containsKey(fInst.getIDO()))
												 fInst.setIDO(idosChanged.get(fInst.getIDO()));
											 if(ddm.isObjectProperty(fInst.getPROP())){
												 //System.err.println("ANTES MODIF:"+fInst);
												 if(fInst.getVALUE()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getVALUE())))
													 fInst.setVALUE((idosChanged.get(Integer.valueOf(fInst.getVALUE()))).toString());
												 if(fInst.getSystemValue()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getSystemValue())))
													 fInst.setSystemValue(idosChanged.get(Integer.valueOf(fInst.getSystemValue())).toString());
												 /*if(fInst.getDestinationSystem()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getDestinationSystem())))
													 fInst.setDestinationSystem(idosChanged.get(Integer.valueOf(fInst.getDestinationSystem())).toString());*/
												 //System.err.println("DESPUES MODIF:"+fInst);
											 }
										 
											 
											 Session oldSess=SessionController.getInstance().getActualSession(ddm);
											 try{
												 //System.err.println("Session IndividualUpdate:"+idosIndividualToUpdate.get(ido).getSessionCreation());
												 //System.err.println("Fact actualizado en sesion por defecto:"+fact.getSessionValues()+" siendo el fact:"+fact);
												 SessionController.getInstance().setActual(SessionController.getInstance().getSession(idosIndividualToUpdate.get(ido).getSessionCreation()),ddm);
												 ddm.addFactToRuler(fInst);
											 }finally{
												 SessionController.getInstance().setActual(oldSess,ddm);
											 }
											 
// }
									 }
									 //setInOtherKnowledgeBase(fact, idosChanged);//Actualizamos en otros motores, tenemos que hacerlo tanto con order new,set o del
									 //System.err.println("Hace retract del fact actualizado siendo createNewMotor:"+createNewMotor+" para fact:"+fact+"\n con factHandle:"+fact.getFactHandle());
									 ddm.retractInfoFact(fact,!createNewMotor);
									 
									 factsUpdated.add(fact);
									 //fact.update(this.getID(), idosIndividualToUpdate.get(ido).getSessionCreation());
									 //System.err.println("Despues update:"+fInst);
								 }
							 }
						 }else{
							 if(f instanceof FactAccess){
								 //ddm.getRuleEngine().retractFactAccess((FactAccess)f);
								 ddm.retractInfoFactAccess((FactAccess)f, true);
							 }else if(f instanceof Individual){
								 if(((Individual)f).getSTATE()==Constants.INDIVIDUAL_STATE_DELETED){
									 idosToForceRemove.add(((Individual)f).getIDO());
									 //deleteInOtherKnowledgeBase((Individual)f, idosChanged);
								 }
								 else if(idosIndividualToUpdate.get(((Individual)f).getIDO())!=null){
									 if(((Individual)f).rollBackRetractable(this))
										individualRollbackRetractable.put(((Individual)f).getIDO(),((Individual)f));
									 else ((Individual)f).rollBack(this);
								 }/*else{
									 if(((Individual)f).getSTATE()==Constants.INDIVIDUAL_STATE_DELETED)
										 //deleteInOtherKnowledgeBase((Individual)f, idosChanged);
								 }*/
							 }else if(f instanceof Lock){
								 if(idosIndividualToUpdate.get(((Lock)f).getIDO())!=null){
									 if(((Lock)f).rollBackRetractable(this))
										 lockRollbackRetractable.put(((Lock)f).getIDO(),(Lock)f);
									 else ((Lock)f).rollBack(this);
								 }
							 }else if(f instanceof DataRules){
								 ddm.getRuleEngine().retractFactDataRules((DataRules)f);
							 }else
								 System.err.println("COMMIT DE UN OBJETO:" +f+ " no tratado" );
						 }
					 }
					 
					 if(!createNewMotor)//Lo cancelo aqui en vez de despues de retract de los objetos ya que lo que tarda es el retract de los fact
						counter.interrupt();
					 
					 //this.removeFromRuler(idosToRemove.iterator());
					 idosToRemove.addAll(idosToForceRemove);
					 //System.out.println("\n=========COMMIT VA A BORRAR LOS OBJETOS:" +idosToRemove);
					 Iterator<Integer> idos=idosToRemove.iterator();
					 //ArrayList<Integer> listIdosToUnlock=new ArrayList<Integer>();
					 while(idos.hasNext()){
						 int ido=idos.next();
						 //if(ddm.getLevelOf(ido)==Constants.LEVEL_INDIVIDUAL)
						//	  listIdosToUnlock.add(ido);
						 boolean retract=false;
						 if(idosToForceRemove.contains(ido)){
							 ddm.retractInfoObject(ido,false,!createNewMotor);
							 retract=true;
						 }else{
							 LinkedList<IPropertyDef> list=ddm.getRuleEngine().getInstanceFactsWhereIdo(ido);
							 if(list.isEmpty() || ddm.getListFactRetractable().containsAll(list)){
								 if(/*!ddm.existInMotor(ido) || */ddm.getLevelOf(ido)!=Constants.LEVEL_FILTER){
									 ddm.retractInfoObject(ido,false,!createNewMotor);
									 retract=true;
								 }else{//Si es un filtro solo lo borramos si no esta siendo apuntado por nadie
									  LinkedList<IPropertyDef> listPointed=ddm.getRuleEngine().getInstanceFactsWhereValueAndValueCls(ido+"",ddm.getClassOf(ido));
									  if(listPointed.isEmpty() || ddm.getListFactRetractable().containsAll(listPointed)){
										  ddm.retractInfoObject(ido,false,!createNewMotor);
										  retract=true;
									  }
								 }
							 }else if(ddm.getLevelOf(ido)!=Constants.LEVEL_FILTER){
								 System.err.println("WARNING: EL IDO"+ido+" DE LA CLASE:"+ddm.getClassName(ido)+" NO se ha podido eliminar de los mapas ya que tiene facts en otras sesiones");
								 //System.err.println(ruleEngine.getInstanceFactsWhereIdo(ido));
								 //server.logError(new Exception("El ido"+ido+" de la clase:"+getClassOf(ido)+" no se ha podido eliminar de los mapas al tener el fact:"+ruleEngine.getInstanceFactsWhereIdo(ido).getFirst()), "Error despues de registrar los cambios en base de datos. La operacion ha sido realizada correctamente pero deberia reiniciar la aplicacion ya que podria haber quedado inconsistente");
							 }
						 }
						 
						 if(!retract){
							 if(individualRollbackRetractable.containsKey(ido)){
								 //Si no se ha tenido que hacer retract del individuo hacemos el rollback del fact individual ya que lo pospusimos para no tener que hacer retract y rollback
								 individualRollbackRetractable.get(ido).rollBack(this);
							 }
							 if(lockRollbackRetractable.containsKey(ido)){
								 lockRollbackRetractable.get(ido).rollBack(this);
							 }
						  }
					 }
					 /*if(isLockObjects)
							server.unlockObjects(listIdosToUnlock, this.getUser());*/
					// unlockObjects();
					 //}
					 
					 //System.out.println("Doc Data model ha registrado los cambios en la base de datos");
		
		
					 //System.out.println("commit del doc data model -..-.-.-.-.-.-.-.-....-.-.-.-.-.-"+ this.getSesionables().size());
					 
					 // Actualizamos, si es necesario, otros motores con los facts que se han enviado a base de datos
					 updateOtherKnowledgeBase(factsImportantes, idosChanged, idosToLoad);
					 
					 //Cargamos en la sesion por defecto los idos creados que necesitan estar siempre cargados en la aplicacion. De momento pasa con los que heredan de configuration y los enumerados
					 Session oldSess=SessionController.getInstance().getActualSession(ddm);
					 SessionController.getInstance().setActual(ddm.getDefaultSession(),ddm);
					 Iterator<Integer> itr=idosToLoad.keySet().iterator();
					 while(itr.hasNext()){
						 int ido=itr.next();
						 //this.retractInfoObject(ido,false);
						 ddm.loadFromServerIfNotExist(ido,idosToLoad.get(ido));
					 }
					 SessionController.getInstance().setActual(oldSess,ddm);
					 if(createNewMotor){
						 ((JBossEngine)ddm.getRuleEngine()).cloneMotor(ddm.getListFactRetractable(),ddm.getDataModelAdapter());
						 ddm.getListFactRetractable().clear();
					 }
					 
					 // Avisamos a todas las sesiones del cambio de facts que tienen sessionValue en otras sesiones.
					 // Lo hacemos al final para que si se disparan reglas que sean con el motor ya limpio.
					 // Avisamos a todas las sesiones ya que puede estar siendo utilizado ese individuo aunque no tenga valor en ese fact
					 Iterator<Fact> itrFactsUpdated=factsUpdated.iterator();
					 while(itrFactsUpdated.hasNext()){
						 Fact fact=itrFactsUpdated.next();
						 Iterator<Session> itrSession=((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(ddm).clone()).values().iterator();
						 while(itrSession.hasNext()){
							 Session sess=itrSession.next();
							 int idSession=sess.getID();
							 if(idSession!=this.getID())
								 fact.avisaSession(fact.getOrder(),sess); 
						 }
					 }
					 getSesionables().removeAll(this.getSesionables());
				 }
				 //ruleEngineRun();
				 
	//			 now=new Date(System.currentTimeMillis());
	//			 System.err.println("\n        ************Fin commit *******    "+now);
			 }
		 }finally{
			 createNewMotor=false;
			 setState(this.USE_STATE);
			 notifyExecuteAction();
			 SessionController.getInstance().setActual(oldSession,ddm);
		 }
		 
		 
//		 System.err.println("Final commit DocDataModel");
//		 getRuleEngine().getMotorSize();
		 //SessionController.getInstance().setActual(null,ik);
			
		 fin=System.currentTimeMillis();
		 tiempos=(fin-inicio)/Constants.TIMEMILLIS;
		 //System.err.println("\n\n ========== FIN COMMIT ddm. TIEMPO(segundos)="+tiempos+"  horaActual="+Auxiliar.getFechaActual()+"\n");
		 return false;
	 }


	private void notifyNoticeListener() {
		for(String message:noticeMessages){
			notifyNoticeMessage(message);
		}
		noticeMessages.clear();
		
	}


	private void updateOtherKnowledgeBase(ArrayList<IPropertyDef> facts,HashMap<Integer,Integer> idosChanged,HashMap<Integer,Integer> idosToLoad) throws InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException{
		Iterator<IPropertyDef> itrFacts=facts.iterator();
		HashSet<Integer> processedProp=new HashSet<Integer>();
		 while(itrFacts.hasNext()){
			 IPropertyDef fact=itrFacts.next();
			 if(fact.getPROP()==Constants.IdPROP_OBJECTDELETED){
				 deleteInOtherKnowledgeBase(fact.getIDO(), fact.getIDTO(), idosChanged);
			 }else{
//					if(idosIndividualToUpdate.get(fact.getIDO())!=null/* || idosToHoldInMotor.contains(fact.getIDO())*/){
				 if(!Constants.isIDTemporal(fact.getIDO())){//Actualizamos los que son de individuos de base de datos porque los prototipos es imposible que ya esten en otros motores
				 	setInOtherKnowledgeBase(fact, idosChanged, processedProp);
				 	processedProp.add(fact.getPROP());
				 }
				 //}
			 }
		 }
		 
		 if(!idosToLoad.isEmpty()){
			 Iterator<Integer> itr=idosToLoad.keySet().iterator();
			 while(itr.hasNext()){
				 int ido=itr.next();
				 loadInOtherKnowledgeBase(ido,idosToLoad.get(ido));
			 }
		 }
	}
	
	private void setInOtherKnowledgeBase(IPropertyDef fact,HashMap<Integer,Integer> idosChanged,HashSet<Integer> processedProp) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException{
		ArrayList<IKnowledgeBaseInfo> auxList=new ArrayList<IKnowledgeBaseInfo>(SessionController.getInstance().getKnowledgeBaseList());//Lo almacenamos en una lista para evitar la excepcion de concurrentModification que se puede producir al ser multiventana
		for(IKnowledgeBaseInfo ik:auxList){
			 if((ik!=ddm || this.isReusable()) && /*&& !ik.isDispose()*//*Esto no debe ocurrir nunca, hay que depurar por que esta pasando*/ ik.existInMotor(fact.getIDO())){
				 //boolean oldLockObjects=ik.getDefaultSession().isLockObjects();
				 //boolean oldRunRules=ik.getDefaultSession().isRunRules();
				 try{
					 //ik.getDefaultSession().setLockObjects(false);
					 //ik.getDefaultSession().setRunRules(false);
					 
					 if(ik.isDispose()){
						 if(ik.getServer()!=null){
							 Exception ex=new Exception("SETINOTHERKNOWLEDGEBASE con ik dispose siendo ik:"+ik+" Fact:"+fact);
							 ik.getServer().logError(null,ex, null);
						 }else{
							 Auxiliar.printCurrentStackTrace();
						 }
						 continue;
					 }
					 //System.err.println("WARNING: Fact que se propaga a todos los motores "+fact);
					 
					 Session sessionCreation=null;
					 //System.err.println("ik:"+ik+" ruleEngine:"+((DocDataModel)ik).getRuleEngine());
					 
					 Fact factToDelete=null;
					 if(!fact.isIncremental()){
						 if(fact.getOrder()==action.SET)
							 factToDelete=((DocDataModel)ik).getRuleEngine().getFact(fact.getInitialValues());
						 else factToDelete=((DocDataModel)ik).getRuleEngine().getFact(fact); 
						 
						 if(factToDelete==null && Auxiliar.equals(ddm.getProperty(fact.getIDO(), fact.getIDTO(), fact.getPROP(), null, ik.getUser(), null, false).getCardMax(),1)){
							 //Significara que es un fact que ha sido modificado en los dos motores. Esto solo ocurre cuando se hace setValueNotLock.
							 if(!processedProp.contains(fact.getPROP()) || fact.getOrder()!=action.DEL){
								 //Comprobamos que esa property no haya sido ya procesada, mirando en processedProp, para evitar que un delete de fact borre un new o set que hayamos ya propagado
								 //Si primero se ha llegado aqui con un del no hay problema, debe sobreescribirse con el set o new, pero al contrario no
								 LinkedList<IPropertyDef> list=((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereIdoAndIdProp(fact.getIDO(), fact.getPROP());
								 if(!list.isEmpty()){
									 factToDelete=(Fact)list.getFirst();//((DocDataModel)ik).getRuleEngine().getFact(list.getFirst());
								 }
							 }else{
								 //System.err.println("WARNING: Fact de order del con property ya procesada anteriormente, no se hace nada para no machacar el valor");
								 continue;
							 }
						 }
					 }else{
						 //Si es incremental tenemos que buscarlo ya que en otros motores puede tener valor ya que su edicion no se bloquea en base de datos
						 LinkedList<IPropertyDef> list=((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereIdoAndIdProp(fact.getIDO(), fact.getPROP());
						 if(!list.isEmpty()){
							 factToDelete=(Fact)list.getFirst();//((DocDataModel)ik).getRuleEngine().getFact(list.getFirst());
							 if(factToDelete.initialValuesChanged()){//Si tiene cambios no actualizamos el incremental ya que puede ser un problema para las reservas
								 //System.err.println("WARNING: Fact incremental no actualizado ya que tiene cambios en el motor de destino."+fact);
								 continue;
							 }
						 }
					 }
					 
					 if(factToDelete!=null){
						 //System.err.println("Lo coge de getLastSession");
						 sessionCreation=SessionController.getInstance().getSession(factToDelete.getLastSession());
						 ((DocDataModel)ik).retractInfoFact(factToDelete,true); 
					 }else{
						 //Entra aqui cuando el fact no existe en ese motor. Esto ocurre cuando en el motor actual se le da valor a un campo que antes no tenia valor
						 sessionCreation=SessionController.getInstance().getSession(((DocDataModel)ik).getRuleEngine().getIndividualFact(fact.getIDO()).getSessionCreation());
					 }
					 
					 Session oldSess=SessionController.getInstance().getActualSession(ik);
					 Fact factAdded=null;
					 try{
						 SessionController.getInstance().setActual(sessionCreation,ik);
						 
						 if(fact.getOrder()==action.NEW || fact.getOrder()==action.SET){
								
								
								//TODO FRAN REVISAR PROBLEMAS ISINCREMENTAL Y CUENTA CONTABLE PROPAGACION: No lo descomentamos, ya que si es incremental lo metemos como un fact normal y no hay problema, ya que se hace retract del fact incremental 
								//if(! (fact instanceof DatValue && ((DatValue)fact).isIncremental())){
								 FactInstance fInst=fact.toFactInstance();
								 fInst.setExistia_BD(true);
								 if(idosChanged.containsKey(fInst.getIDO()))
									 fInst.setIDO(idosChanged.get(fInst.getIDO()));
								 if(ik.isObjectProperty(fInst.getPROP())){
									 //System.err.println("ANTES MODIF:"+fInst);
									 if(fInst.getVALUE()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getVALUE())))
										 fInst.setVALUE((idosChanged.get(Integer.valueOf(fInst.getVALUE()))).toString());
									 if(fInst.getSystemValue()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getSystemValue())))
										 fInst.setSystemValue(idosChanged.get(Integer.valueOf(fInst.getSystemValue())).toString());
									 /*if(fInst.getDestinationSystem()!=null && idosChanged.containsKey(Integer.valueOf(fInst.getDestinationSystem())))
										 fInst.setDestinationSystem(idosChanged.get(Integer.valueOf(fInst.getDestinationSystem())).toString());
									  */
									 //System.err.println("DESPUES MODIF:"+fInst);
								 }
								 
								 ((DocDataModel)ik).addFactToRuler(fInst);
								
								 factAdded=((DocDataModel)ik).getRuleEngine().getFact(fInst);
								 
								 //System.err.println("Fact actualizado en sesion:"+sessionCreation+" siendo el fact añadido:"+factAdded);
//						 	}
						 }
					 
						 // Avisamos a todas las sesiones del cambio de facts que tienen sessionValue en otras sesiones.
						 // Lo hacemos al final para que si se disparan reglas que sean con el motor ya limpio.
						 // Avisamos a todas las sesiones ya que puede estar siendo utilizado ese individuo aunque no tenga valor en ese fact
						 Iterator<Integer> itrSession=((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(ik).clone()).keySet().iterator();
						 while(itrSession.hasNext()){
							 int idSession=itrSession.next();
							 if(idSession!=this.getID()){
								 //factAdded.avisaSession(factAdded.getOrder(),sessionCreation);
								 
								 dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
									Value v = null;
									Value vOld = null;
									Fact factToSession=factAdded!=null?factAdded:factToDelete;
									if(factToDelete!=null)
										vOld=knad.buildValue(factToDelete); 
//											}else{
//												FactInstance factSessionAnt=new FactInstance(sessionValAnt.getIDTO(),this.getIDO(),this.getPROP(),sessionValAnt.getVALUE(),sessionValAnt.getVALUECLS(),sessionValAnt.getRANGENAME(),sessionValAnt.getQMIN(),sessionValAnt.getQMAX(),sessionValAnt.getOP(),this.getCLASSNAME());
//												vOld=knad.buildValue(factSessionAnt);
//											}
									if(factAdded!=null)//FactAdded sera null cuando se ha hecho un delete, por lo que v=null
										v=knad.buildValue(factAdded);
								 /*sessionCreation.*/SessionController.getInstance().getSession(idSession).changeValue(factToSession.getIDO(), factToSession.getIDTO(), factToSession.getPROP(), factToSession.getVALUECLS(), v, vOld,factToSession.getLEVEL(),factToSession.getOrder());
							 }
						 }
						 
					 }finally{
						 SessionController.getInstance().setActual(oldSess,ik);
					 }

					 //setInOtherKnowledgeBase(fact, idosChanged);//Actualizamos en otros motores, tenemos que hacerlo tanto con order new,set o del
					 
				 }catch(Exception ex){
					 ex.printStackTrace();
					 ik.getServer().logError(null, ex, "Error al intentar actualizar en memoria el valor de "+Utils.normalizeLabel(ik.getAliasOfClass(fact.getIDTO(), getUtask())+". Posiblemente ese dato aparece en varias ventanas. Es recomendable REINICIAR LA APLICACION"));
				 }/*finally{
					 ik.getDefaultSession().setLockObjects(oldLockObjects);
					 ik.getDefaultSession().setRunRules(oldRunRules);
				 }*/
				 //SessionController.getInstance().setActual(oldSess,ik);
			 }
		 	 
		 }
	}
	
	private void deleteInOtherKnowledgeBase(int ido, int idto,HashMap<Integer,Integer> idosChanged) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException{
		ArrayList<IKnowledgeBaseInfo> auxList=new ArrayList<IKnowledgeBaseInfo>(SessionController.getInstance().getKnowledgeBaseList());//Lo almacenamos en una lista para evitar la excepcion de concurrentModification que se puede producir al ser multiventana
		for(IKnowledgeBaseInfo ik:auxList){
			 if(ik!=ddm && ik.existInMotor(ido)){
				 //System.err.println("WARNING: Individuo borrado que se propaga al motor ido:"+ido+" idto:"+idto); 
				 //boolean oldLockObjects=ik.getDefaultSession().isLockObjects();
				 //boolean oldRunRules=ik.getDefaultSession().isRunRules();
				 try{
					 //ik.getDefaultSession().setLockObjects(false);
					 //ik.getDefaultSession().setRunRules(false);
					 
					 //Session sessionUsed=null;
					 
					 //Hacemos retract de los fact que estan apuntando a ese individuo en ese motor
					 Iterator itApuntado = ((DocDataModel)ik).getRuleEngine().getInstanceFactsWhereValueAndValueCls(String.valueOf(ido), idto).iterator();
					 if (itApuntado.hasNext()) {
						 while(itApuntado.hasNext()){
							 dynagent.ruleengine.src.ruler.Fact factPointMe = (dynagent.ruleengine.src.ruler.Fact) itApuntado.next();
							 //sessionUsed=SessionController.getSession(factPointMe.getLastSession());
							 							 
							 dynagent.common.knowledge.KnowledgeAdapter knad = new dynagent.common.knowledge.KnowledgeAdapter(ik);
								Value v = null;
								Value vOld = knad.buildValue(factPointMe); 
//								}else{
//									FactInstance factSessionAnt=new FactInstance(sessionValAnt.getIDTO(),this.getIDO(),this.getPROP(),sessionValAnt.getVALUE(),sessionValAnt.getVALUECLS(),sessionValAnt.getRANGENAME(),sessionValAnt.getQMIN(),sessionValAnt.getQMAX(),sessionValAnt.getOP(),this.getCLASSNAME());
//									vOld=knad.buildValue(factSessionAnt);
//								}
								// Avisamos a todas las sesiones ya que puede estar siendo utilizado ese individuo aunque no tenga valor en ese fact
								 Iterator<Session> itrSession=((HashMap<Integer,Session>)SessionController.getInstance().getSessionsList(ik).clone()).values().iterator();
								 while(itrSession.hasNext()){
									 Session sess=itrSession.next();
									 int idSession=sess.getID();
									 if(idSession!=this.getID()){
										sess.changeValue(factPointMe.getIDO(), factPointMe.getIDTO(), factPointMe.getPROP(), factPointMe.getVALUECLS(), v, vOld,factPointMe.getLEVEL(),factPointMe.getOrder());
									 }
								 }
							 ((DocDataModel)ik).retractInfoFact(factPointMe,true);
						 }
					 }/*else{
						 sessionUsed=SessionController.getSession(((DocDataModel)ik).getRuleEngine().getIndividualFact(i.getIDO()).getSessionCreation());
					 }*/
					 
					 //ik.deleteObject(i.getIDO(), i.getIDTO(), i.getRDN(), null, ddm.getUser(), getUtask(), sessionUsed);
					 ((DocDataModel)ik).retractInfoObject(ido, false, true);
				 }catch(Exception ex){
					 ex.printStackTrace();
					 ik.getServer().logError(null, ex, "Error al intentar actualizar en memoria el valor de "+Utils.normalizeLabel(ik.getAliasOfClass(idto, getUtask())+". Es recomendable REINICIAR LA APLICACIoN."));
				 }/*finally{
					 ik.getDefaultSession().setLockObjects(oldLockObjects);
					 ik.getDefaultSession().setRunRules(oldRunRules);
				 }*/
			 }
		 }
	}
	
	private void loadInOtherKnowledgeBase(int ido,int idto) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException{
		 //System.err.println("WARNING: Individuo que se propaga a todos los motores ido:"+ido+" idto:"+idto);
		 ArrayList<IPropertyDef> lfactsserver=null;	
		 ArrayList<IKnowledgeBaseInfo> auxList=new ArrayList<IKnowledgeBaseInfo>(SessionController.getInstance().getKnowledgeBaseList());//Lo almacenamos en una lista para evitar la excepcion de concurrentModification que se puede producir al ser multiventana
		 for(IKnowledgeBaseInfo ik:auxList){
			 if(ik!=ddm){
				 while(!ik.isEnabled()){
					 try {
						Thread.sleep(Constants.TIMEMILLIS);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				 }
				 boolean oldLockObjects=ik.getDefaultSession().isLockObjects();
				 boolean oldRunRules=ik.getDefaultSession().isRunRules();
				 Session oldSession =SessionController.getInstance().getActualSession(ik);
				 try{
					 ik.getDefaultSession().setLockObjects(false);
					 ik.getDefaultSession().setRunRules(false);
					 SessionController.getInstance().setActual(ik.getDefaultSession(),ik);
					 if(lfactsserver==null)
						lfactsserver=ddm.getServer().serverGetFactsInstance(ido, idto,ddm.getUser(), false, 1,true, true).getAIPropertyDef();			
					
					 //TODO Tener en cuenta que si serverGetFactsInstance algun dia devuelve directamente Facts sera un problema ya que insertariamos el
					 //mismo fact en todos los motores por lo que el unico FactHandle que tendria seria el del ultimo motor
					 
					 //Estamos seguro que no existe ya en los otros motores porque es una creacion
					 ((DocDataModel)ik).loadNewData(lfactsserver, null, ddm.getUser(), getUtask(),true,false,false);
					 //ik.loadIndividual(ido, idto, 1, true, null, ddm.getUser(), getUtask(), ik.getDefaultSession());
					 
				 }catch(Exception ex){
					 ex.printStackTrace();
					 ik.getServer().logError(null, ex, "Error al intentar actualizar en memoria el objeto "+Utils.normalizeLabel(ik.getAliasOfClass(idto, getUtask())));
				 }finally{
					 ik.getDefaultSession().setLockObjects(oldLockObjects);
					 ik.getDefaultSession().setRunRules(oldRunRules);
					 SessionController.getInstance().setActual(oldSession,ik);
				 }
				 //SessionController.getInstance().setActual(oldSess,ik);
			 }
		 }
	}
	
//	 TODO: Deberia lanzar una exepcion en caso de que el RollBack es de una
	 // sessio hija, y deshacerlo todo en la bb.dd
//	 public void rollBack() throws ApplicationException, NotFoundException, OperationNotPermitedException, InstanceLockedException, IncompatibleValueException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
//
//		 //System.out.println("RollBack del Doc Data Model" + this.session.getID()	+ " con madre:" + this.session.getIDMadre());
//		 System.err.println("\n INFO: INICIO ROLLBACK");
//		 double inicio,fin,tiempos;
//		 inicio=System.currentTimeMillis();
//		 Session oldSession=SessionController.getInstance().getActualSession(ddm)!=this?SessionController.getInstance().getActualSession(ddm):null;//Comprobamos que no sea ella misma ya que setActual del final volveria a añadirla a sessionList
//		 
//		 for (int i = 0; i < SessionController.getInstance().getSessionsList(ddm).size(); i++) {
//			 if (SessionController.getInstance().getSessionsList(ddm).get(i).getIDMadre() == this.getID()) {
//
//				 throw new ApplicationException(11000,
//						 "ERROR: intento de rollBack de una sesion ("
//						 + this.getID() + ") con idMadre:  ("
//						 + this.getIDMadre()
//						 + ") antes de cerrar sus sub-sesiones");
//			 }
//		 }
//		 if (SessionController.getInstance().getActualSession(ddm) != this) {
//			 throw new ApplicationException(11002,
//					 "ERROR: intento de rollBack de una sesion ("
//					 + this.getID() + ") que no es actual");
//		 }
//		 // rollBack de los sessionables apuntados por la madre
//		 Iterator<Sessionable> itr=this.getSesionables().iterator();
//		 while(itr.hasNext())
//			 itr.next().rollBack(this);
////		 if (this.session.getIDMadre() != -1)
////		 SessionController.getInstance().setActual(
////		 SessionController.getInstance().getSession(
////		 this.session.getIDMadre()));
////		 else
////		 // Session es session Root.
////		 //System.out.println("RollBack de un IKnowledgeBaseInfo que llama a la persistencia");
//		 SessionController.getInstance().remove(this.getID(),ddm);
//		 SessionController.getInstance().setActual(oldSession,ddm);
//		 //ruleEngineRun();
//		 fin=System.currentTimeMillis();
//		 tiempos=(fin-inicio)/Constants.TIMEMILLIS;
//		 System.out.println(" --time-->  ROLLBACK TIEMPO(segundos)="+tiempos+"\n");
//
//	 }
	 
	public void rollBack() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		if(reusable){
			super.rollBack();
		}else{
			setState(this.FINISHING_STATE);
			try{
				ik.dispose();//Este dispose se encarga de hacer dispose de esta sesion
				unlockObjects();
				cancelReservations();
			}finally{
				//setNotifyChanges(true);
				setState(this.USE_STATE);
				createNewMotor=false;
			}
//			SessionController.getInstance().setActual(null,ik);
//			SessionController.getInstance().remove(this.ID,ik);
			setState(this.FINISH_STATE);
			//dispose();
			//notifyState(false);
		}
	}
	
	 private void notifyChangesServer(Changes changesServer) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException {
		 for(int i=0 ;i<changeServerListeners.size();i++) {
			 IChangeServerListener iChange = changeServerListeners.get(i);
			 ArrayList<ObjectChanged> aoc = changesServer.getAObjectChanged();
			 for(int j=0;j<aoc.size();j++) {
				 ObjectChanged oc = aoc.get(j);
				 iChange.changeServerValue(oc.getNewIdo(), oc.getOldIdo(), oc.getProp(), oc.getNewValue(), oc.getOldValue());
			 }
		 }
	 }

	 private void notifyHistoryDDBB(Changes changesServer,ArrayList<IPropertyDef> facts,/*int ido, int idto, String rdn, int operation*/Integer idtoUTask,Session sessionUsed) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException {
		 //System.err.println("Changes:"+changesServer);
		 HashMap<Integer,String> idoRdnChangeMap=new HashMap<Integer, String>();
		 HashMap<Integer,Integer> oldIdoNewidoChangeMap=new HashMap<Integer, Integer>();
		 //Element changes = res.getChild("CHANGES");
		 ArrayList<ObjectChanged> objectIndexList=changesServer.getAObjectChanged();
		 //if(objectIndexList!=null){
		 Iterator<ObjectChanged> iter = objectIndexList.iterator();
		 while (iter.hasNext()) {
			 ObjectChanged nodeIdo = (ObjectChanged)iter.next();
			 int oldIdo = nodeIdo.getOldIdo();
			 int newIdo = nodeIdo.getNewIdo();
			 oldIdoNewidoChangeMap.put(oldIdo,newIdo);
			 //System.err.println("oldIdoNewidoChangeMap:"+oldIdoNewidoChangeMap+" nodeIdo:"+nodeIdo);
			 Integer idProp = nodeIdo.getProp();//Integer.parseInt(nodeIdo.getAttributeValue("PROP"));
			 if(idProp!=null && idProp==Constants.IdPROP_RDN){
				 String rdn = ((StringValue)nodeIdo.getNewValue()).getValue();//getAttributeValue("NEW_VALUE");
				 idoRdnChangeMap.put(oldIdo,rdn);
			 }
		 }

		 /*selectNotStructurals(facts);*/

		 for (int i = 0 ; i < historyDDBBListeners.size();i++)
		 {
			 historyDDBBListeners.get(i).initChangeHistory();
			 //System.out.println("AVISO:"+operation);
		 }
		 
		 ArrayList<Integer> idosProcessed=new ArrayList<Integer>();
		 Iterator<IPropertyDef> itr=facts.iterator();
		 while(itr.hasNext()){
			 IPropertyDef fact=itr.next();
			 int ido=fact.getIDO();
			 //System.err.println("Fact antes de todo:"+fact);
			 if(!idosProcessed.contains(ido)){
				 int idto=fact.getIDTO();
				 int operation=fact.getOrder();
				 String rdn=null;
				 int newIdo=-1;
				 boolean notify=true;
				 if(operation==action.DEL_OBJECT/* && fact.getPROP()==Constants.IdPROP_RDN)*/){
					// System.err.println("DEL OBJECT DE :"+ido+" Fact:"+fact);
					 //IPropertyDef factd=getInitialValueOfFactDeleted(ido, Constants.IdPROP_RDN);
//					 IPropertyDef factd=ruleEngine.getInstanceFactsWhereIdoAndIdProp(ido, Constants.IdPROP_RDN).getFirst();
//					 rdn=factd.getVALUE();
					 Individual i = ddm.getRuleEngine().getIndividualFact(ido);
					 rdn=i.getRDN();
					 newIdo=ido;
				 }else if(operation==action.SET || operation==action.NEW || operation==action.DEL){
					 if(!Constants.isIDTemporal(ido))//Tenemos que hacer esto porque la operation del fact es sobre ese fact solamente, no sobre el individuo
						 operation=action.SET;
					 rdn=idoRdnChangeMap.get(ido);
					 newIdo=oldIdoNewidoChangeMap.get(ido)!=null?oldIdoNewidoChangeMap.get(ido):ido;
					 if(rdn==null){
						 DataProperty property=(DataProperty)ddm.getProperty(ido,idto,Constants.IdPROP_RDN,null,ddm.getUser(),null,false);
						 rdn=!property.getValues().isEmpty()?((StringValue)property.getValues().get(0)).getValue():"";
					 }
				 }else notify=false;

				 if(notify){
					 for (int i = 0 ; i < historyDDBBListeners.size();i++)
					 {
						 historyDDBBListeners.get(i).changeHistory(newIdo, idto, rdn, ido, operation, idtoUTask, sessionUsed);
						 //System.out.println("AVISO:"+operation);
					 }
					 idosProcessed.add(ido);

				 }
			 }
		 }
		 
		 for (int i = 0 ; i < historyDDBBListeners.size();i++)
		 {
			 historyDDBBListeners.get(i).endChangeHistory();
			 //System.out.println("AVISO:"+operation);
		 }
		 //}
	 }
	 
	 public static void addHistoryDDBBListener(IHistoryDDBBListener historyDDBBListener) {
		 historyDDBBListeners.add(historyDDBBListener);
	 }
	 
	 public static void removeHistoryDDBBListener(IHistoryDDBBListener historyDDBBListener) {
		 historyDDBBListeners.remove(historyDDBBListener);
	 }

	 public static ArrayList<IHistoryDDBBListener> getHistoryDDBBListeners(){
		 return historyDDBBListeners;
	 }
	 
	 public static void addChangeServerListener(IChangeServerListener changeServerListener) {
		 changeServerListeners.add(changeServerListener);
	 }
	 
	 public static void removeChangeServerListener(IChangeServerListener changeServerListener){
		 changeServerListeners.remove(changeServerListener);
	 }
	 
	 public static ArrayList<IChangeServerListener> getChangeServerListeners(){
		 return changeServerListeners;
	 }

}
