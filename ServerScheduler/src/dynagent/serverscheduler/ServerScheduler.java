package dynagent.serverscheduler;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.communication.communicator;
import dynagent.common.communication.message;
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
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Email;
import dynagent.ruleengine.src.factories.RuleEngineFactory;

public class ServerScheduler {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws IncompatibleValueException 
	 * @throws OperationNotPermitedException 
	 * @throws CardinalityExceedException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws NumberFormatException 
	 * @throws DataErrorException 
	 * @throws ApplicationException 
	 * @throws RemoteSystemException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws SystemException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws SystemException, CommunicationException, InstanceLockedException, RemoteSystemException, ApplicationException, DataErrorException, NumberFormatException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException, MalformedURLException {
		Integer business=null;
		String user=null;
		String password=null;
		URL serverURL=null;
		URL jarURL=null;
		String ip=null;
		String port=null;
		String remitenteErrorsEmail=null;
		String remitenteErrorsEmailPassword=null;
		String errorsEmail=null;
		String rules=null;
		
		
		String id="";
		for(int i=0;i<args.length;i++){
			//System.out.println("\n debug Menu param="+args[i]);
			if(args[i].startsWith("-"))
				id=args[i];
			else{
				if(id.equalsIgnoreCase("-ip")){
					ip=args[i];
				}else if(id.equalsIgnoreCase("-port")){
					port=args[i];
				}else if(id.equalsIgnoreCase("-bns")){
					business=Integer.parseInt(args[i]);
				}else if(id.equalsIgnoreCase("-user")){
					user=args[i];
				}else if(id.equalsIgnoreCase("-password")){
					password=args[i];
				}else if(id.equalsIgnoreCase("-remitenteErrorsEmail")){
					remitenteErrorsEmail=args[i];
				}else if(id.equalsIgnoreCase("-remitenteErrorsEmailPassword")){
					remitenteErrorsEmailPassword=args[i];
				}else if(id.equalsIgnoreCase("-errorsEmail")){
					errorsEmail=args[i];
				}else if(id.equalsIgnoreCase("-rules")){
					rules=args[i];
				}
			}
		}
		
		serverURL = new URL("http://"+ip+":"+port+"/dyna/bin/");
		jarURL = new URL("http://"+ip+":"+port+"/dyna/bin/");
		System.out.println("DBGSCH previo sched");
		ServerScheduler main=new ServerScheduler(business, user, password, serverURL, jarURL, remitenteErrorsEmail, remitenteErrorsEmailPassword, errorsEmail, rules);
		main.processActions();
	}

	private String remitenteErrorsEmail;
	private String remitenteErrorsEmailPassword;
	private String errorsEmail;
	private IKnowledgeBaseInfo ik;
	private HashMap<Integer,HashSet<String>> mapActionExecutionHour;
	
	public ServerScheduler(int business,String user,String password, URL serverURL, URL jarURL, String remitenteErrorsEmail, String remitenteErrorsEmailPassword, String errorsEmail, String rules) throws SystemException, CommunicationException, InstanceLockedException, RemoteSystemException, NotFoundException, IncoherenceInMotorException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
		this.remitenteErrorsEmail=remitenteErrorsEmail;
		this.remitenteErrorsEmailPassword=remitenteErrorsEmailPassword;
		this.errorsEmail=errorsEmail;
		
		this.mapActionExecutionHour=new HashMap<Integer, HashSet<String>>();
		
		try{
			communicator comm=connectCommunicator(business, user, password, serverURL, jarURL);
			System.out.println("DBGSCH creado comm");
			this.ik=connectRuler(comm,rules);
			System.out.println("DBGSCH creado rules");
		}catch(java.lang.Throwable ex){
			ex.printStackTrace();
			sendErrorEmail(remitenteErrorsEmail, remitenteErrorsEmailPassword, errorsEmail, "Creando el motor", ex);
			System.exit(1);
		}
	}
	
	
	/**
	 Lee todas los parametros configuracion desde motor, cargados ya por defecto.
	 Para cada configuracion hago ik.getIdClass(rdn de la configuracion) para ver si es una clase o no (Los parametros de configuracion se llaman igual que la accion, de esta manera descarto las demas)
	 La hora de ejecucion se lee de valor numerico (se trata de un parametro numerico) que sera entre 0 y 23.
	 Para cada accion a ejecutar creo un hilo que despertará a la hora dicha en ese parametro.
	 Si hay dos a la misma hora las llamo una despues de la otra
	**/
	public void processActions() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		try{
			createActionExecutions();
			for(Integer hour:mapActionExecutionHour.keySet()){
				final int hourAux=hour;
				TimerTask task=new TimerTask(){
	
					@Override
					public void run() {
						for(String actionBatchName:mapActionExecutionHour.get(hourAux)){
							System.out.println("Ejecutando accion:"+actionBatchName);
							try{
								String params=null;
								if(actionBatchName.contains("#")){
									params=actionBatchName.substring(actionBatchName.indexOf("#")+1);
									actionBatchName=actionBatchName.substring(0, actionBatchName.indexOf("#"));
								}
								int idtoUserTask=ik.getIdClass(actionBatchName);
								BatchAction batchAction=new BatchAction(idtoUserTask, ik, params);
								LinkedHashMap<String, String> mapResult=batchAction.process();
								
	//							for(String rdn:mapResult.keySet()){
	//							int ido=kba.createPrototype(Constants.IDTO_RESULT_BATCH, Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, sessionNew);
	//							kba.setValue(ido, Constants.IdPROP_RDN, kba.buildValue(rdn, Constants.IDTO_STRING), null, userRol, idtoUserTask, sessionNew);
	//							kba.setValue(ido, Constants.IdPROP_RESULT, kba.buildValue(mapResult.get(rdn), Constants.IDTO_MEMO), null, userRol, idtoUserTask, sessionNew);
	//							
	//							kba.setValue(idoUserTask, Constants.IdPROP_TARGETCLASS, kba.buildValue(ido, Constants.IDTO_RESULT_BATCH), null, userRol, idtoUserTask, sessionNew);
							}catch(java.lang.Throwable ex){
								ex.printStackTrace();
								sendErrorEmail(remitenteErrorsEmail, remitenteErrorsEmailPassword, errorsEmail, actionBatchName, ex);
							}
						}
					}
					
				};
				
				GregorianCalendar calendar=new GregorianCalendar();
				if(calendar.get(Calendar.HOUR_OF_DAY)>hour){
					calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR)+1);
				}
				calendar.set(Calendar.HOUR_OF_DAY, hour);
				calendar.set(Calendar.MINUTE, 0);
				Timer timer=new Timer();
				long executionPeriod=1000*60*60*24;
				timer.schedule(task, calendar.getTime(), executionPeriod);
			}
		}catch(java.lang.Throwable ex){
			ex.printStackTrace();
			sendErrorEmail(remitenteErrorsEmail, remitenteErrorsEmailPassword, errorsEmail, "Preparando el timer", ex);
		}

	}
	
	private void createActionExecutions() throws NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		int idto=ik.getIdClass(Constants.CLS_NUMBER_CONFIG_PARAM);
		System.out.println("DBGSCH previo actions");
		HashSet<Integer> list=ik.getIndividualsOfLevel(idto, Constants.LEVEL_INDIVIDUAL);
		for(Integer ido:list){
			System.out.println("DBGSCH previo actions");
			DataProperty rdnProperty=(DataProperty)ik.getProperty(ido, idto, Constants.IdPROP_RDN, null, ik.getUser(), null, ik.getDefaultSession());
			String rdn=rdnProperty.getUniqueValue().getValue_s();
			DataProperty auto=(DataProperty)ik.getProperty(ido, idto, 819, null, ik.getUser(), null, ik.getDefaultSession());
			boolean continuar=false;
			if(auto!=null){
				Value v=auto.getUniqueValue();
				if(v!=null){
					String valor=v.getValue_s();
					if(valor!=null && valor.equals("AUTO")) continuar=true;
				}
			}
			if(continuar){
				try{
					System.out.println("DBGSCH action dentro "+rdn);
					String originalRdn=rdn;
					if(rdn.contains("#")){
						rdn=rdn.substring(0, rdn.indexOf("#"));
					}
					Integer idClass=ik.getIdClass(rdn);//Para ver si la configuracion se refiere a una accion o no ( Para estos casos llamamos la configuracion igual que la accion )
					DataProperty hourProperty=(DataProperty)ik.getProperty(ido, idto, ik.getIdProperty("valor_numerico"), null, ik.getUser(), null, ik.getDefaultSession());
					Integer hour=Double.valueOf(hourProperty.getUniqueValue().getValue_s()).intValue();
					if(!mapActionExecutionHour.containsKey(hour)){
						mapActionExecutionHour.put(hour, new HashSet<String>());
					}
					mapActionExecutionHour.get(hour).add(originalRdn);
				}catch(NotFoundException ex){
					//No se trata de una configuracion para acciones, por lo que no se procesa
					System.out.println(rdn+" No se trata de una configuracion para acciones, por lo que no se procesa");
				}
			}
		}
	}
	
	private void sendErrorEmail(String remitenteErrorsEmail, String remitenteErrorsEmailPassword, String errorsEmail, String actionBatchName, Throwable ex) {
		if(errorsEmail!=null){
			String title = "ERROR ACCIONES BATH: "+actionBatchName;
			String message = ex.getClass()+" mensaje:"+ex.getMessage()+" \n\n"+Arrays.toString(ex.getStackTrace());
			
			if(remitenteErrorsEmail!=null){
				Email.sendEmail(errorsEmail, title, message, remitenteErrorsEmail, remitenteErrorsEmailPassword, false);
			}else{
				Email.sendEmail(errorsEmail, title, message, false);
			}
		}
	}
	
	public IKnowledgeBaseInfo connectRuler(communicator comm,String rules) { 
		
		IKnowledgeBaseInfo ik = null;
		try {
			Element metaDataXML=comm.serverGetMetaData();
			ArrayList<String> rulesList=new ArrayList<String>();
			String[] rulesArray=rules.split(";");
			for(int i=0;i<rulesArray.length;i++){
				rulesList.add(rulesArray[i]);
			}
			ik = RuleEngineFactory.getInstance().createRuler(metaDataXML,comm.getBusiness(),comm,Constants.RULER,comm.getUser(),rulesList,null,null,null,true);
			
		} catch(Exception e) {
	      	e.printStackTrace();
  		}
		return ik;
	}
	
	public communicator connectCommunicator(int business, String user, String password, URL serverURL, URL jarURL) throws SystemException, CommunicationException, InstanceLockedException, RemoteSystemException{
		communicator comm=new communicator(null,null,null,serverURL,jarURL,60000,3,business,user,password,false);
		comm.setMode(Constants.BUSINESS_MODE);
		message eRes=comm.serverLogin(true);

		if( eRes==null ){
			return null;
		}
		if(!eRes.getSuccess()){
			System.out.println("Usuario o password incorrecto");
			return null;
		}else{
			return comm;
		}
	}

}
