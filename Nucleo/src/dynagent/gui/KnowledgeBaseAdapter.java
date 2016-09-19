package dynagent.gui;

import gdev.gfld.GTableRow;

import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import javax.naming.NamingException;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.communication.communicator;
import dynagent.common.communication.docServer;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.exceptions.EngineException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.IHistoryDDBBListener;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.SelectQuery;
import dynagent.common.knowledge.UserAccess;
import dynagent.common.knowledge.access;
import dynagent.common.knowledge.instance;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.UnitValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.AccessAdapter;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.DebugLog;
import dynagent.common.utils.GIdRow;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.INoticeListener;
import dynagent.common.utils.IdObjectForm;
import dynagent.common.utils.QueryConstants;
import dynagent.common.utils.Utils;
import dynagent.common.xml.QueryXML;
import dynagent.gui.forms.utils.Column;
import dynagent.gui.forms.utils.IColumnProperties;
import dynagent.gui.forms.utils.IEssentialProperties;
import dynagent.gui.forms.utils.IGroupProperties;
import dynagent.gui.forms.utils.IOrderProperties;
import dynagent.gui.utils.ICardMed;
import dynagent.gui.utils.IListenerUtask;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class KnowledgeBaseAdapter {

	private IKnowledgeBaseInfo m_kb;
	private LinkedHashMap<Integer, ArrayList<Integer>> m_userTaskMap;
	private LinkedHashMap<Integer, ArrayList<Integer>> m_reportMap;
	private LinkedHashMap<Integer, ArrayList<Integer>> m_actionMap;
	private LinkedHashMap<Integer, ArrayList<Integer>> m_importMap;
	private LinkedHashMap<Integer, ArrayList<Integer>> m_exportMap;
	private HashMap<Integer,Integer> m_idtoUserTaskMap;
	private HashMap<Integer,Integer> m_idtoReportMap;
	private HashMap<Integer,Integer> m_idtoActionMap;
	private HashMap<Integer,Integer> m_idtoImportMap;
	private HashMap<Integer,Integer> m_idtoExportMap;
	private LinkedHashMap<Integer, HashSet<Integer>> m_reportParamMap;
	private HashMap<Integer, Integer> m_manualNotifications;
	private HashMap<String, Object> m_configParams;
	private String m_user,m_userRols;
	private instance m_instance;
	private Stack<instance> m_instanceStack;
	//private communicator m_comm;
	private Session m_defaultSession;
	private Session m_defaultSessionWithoutRules;
	private Session m_ddbbSession;
	private IOrderProperties orderProperties;
	private ICardMed carMedias;
	private IColumnProperties columnsTable;
	private IGroupProperties groupsComponents;
	private IEssentialProperties highlightedComponents;
	private IListenerUtask listenerMenu;
	private HashMap<String,Integer> m_idoStateMap;
	private communicator comm;

	public static final int ALL_MODE=1;
	public static final int TABLE_MODE=2;
	public static final int REPORT_MODE=3;

	public KnowledgeBaseAdapter(IKnowledgeBaseInfo kb,IOrderProperties orderProperties, ICardMed cardm, IColumnProperties columnsTable,IGroupProperties groupsComponents,IEssentialProperties highlightedComponents,IListenerUtask listenerMenu,String user, String userRols, communicator comm) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		m_kb=kb;
		this.orderProperties=orderProperties;
		m_user=user;
		m_userRols=userRols;
		m_instanceStack=new Stack<instance>();
		m_defaultSession=kb.getDefaultSession();
		m_defaultSessionWithoutRules=createDefaultSession(m_defaultSession,m_defaultSession.getUtask(),m_defaultSession.isCheckCoherenceObjects(),false,m_defaultSession.isLockObjects(),m_defaultSession.isDeleteFilters(),m_defaultSession.isForceParent());
		
		m_ddbbSession=kb.getRootSession();
		this.columnsTable=columnsTable;
		this.carMedias=cardm;
		this.groupsComponents=groupsComponents;
		this.highlightedComponents=highlightedComponents;
		this.listenerMenu=listenerMenu;
		this.comm=comm;
		buildListUserTasks();
		buildListState();
		buildListNotifications();
		buildListConfigParams();
	}

	private KnowledgeBaseAdapter(IKnowledgeBaseInfo kb){
		m_kb=kb;
		m_instanceStack=new Stack<instance>();
		m_defaultSession=kb.getDefaultSession();
		m_defaultSessionWithoutRules=createDefaultSession(m_defaultSession,m_defaultSession.getUtask(),m_defaultSession.isCheckCoherenceObjects(),false,m_defaultSession.isLockObjects(),m_defaultSession.isDeleteFilters(),m_defaultSession.isForceParent());
		
		m_ddbbSession=kb.getRootSession();
	}
	
	private void buildListConfigParams() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, ParseException, JDOMException {
		Integer idtoConfigParams=null;
		try{
			idtoConfigParams=getIdClass(Constants.CLS_CONFIG_PARAM);
		}catch(NotFoundException e){
			System.err.println("WARNING: Esta base de datos no es compatible con la configuracion de parametros");
			return;
		}
		
		Iterator<Integer> itrClassesConfigParam = m_kb.getSpecialized(idtoConfigParams).iterator();
		m_configParams=new HashMap<String, Object>();
		while(itrClassesConfigParam.hasNext()){
			int idtoConfigParam=itrClassesConfigParam.next();
			Iterator<Integer> itrConfigParams = m_kb.getIndividualsOfLevel(idtoConfigParam,Constants.LEVEL_INDIVIDUAL).iterator();
			while (itrConfigParams.hasNext()) {
				int idoConfigParam = itrConfigParams.next();
				DataProperty propertyRdn = getRDN(idoConfigParam, idtoConfigParam, null, null, m_defaultSession);
				String name=(String)getValueData(propertyRdn);
				Object value=null;
				if(m_kb.getClassName(idtoConfigParam).equals(Constants.CLS_BOOLEAN_CONFIG_PARAM)){
					String stringValue=getField(idoConfigParam, idtoConfigParam, getIdProp("activo"), null, null, m_defaultSession).getStringUniqueValue();
					if(stringValue!=null){
						value=Boolean.valueOf(stringValue);
					}
				}else if(m_kb.getClassName(idtoConfigParam).equals(Constants.CLS_NUMBER_CONFIG_PARAM)){
					String stringValue=getField(idoConfigParam, idtoConfigParam, getIdProp("valor_numerico"), null, null, m_defaultSession).getStringUniqueValue();
					if(stringValue!=null){
						value=Double.valueOf(stringValue);
					}
				}
				
				m_configParams.put(name, value);
			}
		}
		
	}
	
	public Object getConfigParam(String paramName){
		return m_configParams.get(paramName);
	}
	
	private void buildListNotifications() throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, ParseException, JDOMException {
		Integer idtoNotification=null;
		try{
			idtoNotification=getIdClass(Constants.CLS_NOTIFICATION);
		}catch(NotFoundException e){
			System.err.println("WARNING: Esta base de datos no es compatible con las notificaciones de email");
			return;
		}
		Iterator<Integer> itrNotification = m_kb.getIndividualsOfLevel(idtoNotification,Constants.LEVEL_INDIVIDUAL).iterator();
		m_manualNotifications=new HashMap<Integer, Integer>();
		while (itrNotification.hasNext()) {
			int idoNotification = itrNotification.next();
			ObjectProperty propertyEvent=getChild(idoNotification, idtoNotification, getIdProp(Constants.PROP_EVENT), null, null, m_defaultSession);
			boolean manual=false;
			Iterator<ObjectValue> itrEvent=getValues(propertyEvent);
			while(!manual && itrEvent.hasNext()){
				ObjectValue ov=itrEvent.next();
				ObjectProperty propertyEventType=getChild(ov.getIDOIndividual(), ov.getIDTOIndividual(), getIdProp(Constants.PROP_EVENT_TYPE), null, null, m_defaultSession);
				ObjectValue ovEventType=getValue(propertyEventType);
				String rdn = m_kb.getRdnIfExistInRuler(ovEventType.getIDOIndividual());
				manual=rdn.equalsIgnoreCase("MANUAL");
			}
			
			ObjectProperty propertyClass=getChild(idoNotification, idtoNotification, getIdProp(Constants.PROP_CLASS), null, null, m_defaultSession);
			Iterator<ObjectValue> itr=getValues(propertyClass);
			while(itr.hasNext()){
				ObjectValue ov=itr.next();
				int idtoClass=getIdClass((String)getValueData(getRDN(ov.getIDOIndividual(), ov.getIDTOIndividual(), null, null, m_defaultSession)));
				m_manualNotifications.put(idtoClass, idoNotification);
			}
		}
	}
	
	private void buildListState(){
		Iterator<Integer> itrState = m_kb.getIndividualsOfLevel(Constants.IDTO_ESTADOREALIZACION,Constants.LEVEL_INDIVIDUAL).iterator();
		m_idoStateMap=new HashMap<String, Integer>();
		while (itrState.hasNext()) {
			int ido = itrState.next();
			String rdn = m_kb.getRdnIfExistInRuler(ido);//Sabemos que el ido existe en motor ya que es un enumerado
			m_idoStateMap.put(rdn, ido);
		}
	}

//	public void init(Integer usertask,Integer userRol){
//	try {
//	((Adapter)m_kb).init(usertask,userRol,m_user);
//	} catch (NotFoundException e) {
//	e.printStackTrace();
//	m_comm.logError(e, e.getMessage());
//	}
//	}
	/*Construye referencias a los ids de las userTasks mapeadas por el id del area funcional
	 * a la que pertenecen*/
	private void buildListUserTasks() throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		m_reportMap=new LinkedHashMap<Integer, ArrayList<Integer>>();
		m_userTaskMap=new LinkedHashMap<Integer, ArrayList<Integer>>();
		m_actionMap=new LinkedHashMap<Integer, ArrayList<Integer>>();
		m_importMap=new LinkedHashMap<Integer, ArrayList<Integer>>();
		m_exportMap=new LinkedHashMap<Integer, ArrayList<Integer>>();
		m_idtoUserTaskMap=new HashMap<Integer, Integer>();
		m_idtoReportMap=new HashMap<Integer, Integer>();
		m_idtoActionMap=new HashMap<Integer, Integer>();
		m_idtoImportMap=new HashMap<Integer, Integer>();
		m_idtoExportMap=new HashMap<Integer, Integer>();
		m_reportParamMap=new LinkedHashMap<Integer, HashSet<Integer>>();

		Iterator itrAreasFuncionales = m_kb.getIndividualsOfLevel(Constants.IDTO_FUNCTIONAL_AREA,Constants.LEVEL_INDIVIDUAL).iterator();
		/*Iterator itrAreasFuncionales = m_kb.getSpecialized(helperConstant.CLASS_AREAFUNCIONAL);*/

		m_actionMap.put(null, new ArrayList<Integer>());//Acciones que no se restringen a un area funcional
		m_importMap.put(null, new ArrayList<Integer>());//Imports que no se restringen a un area funcional
		m_exportMap.put(null, new ArrayList<Integer>());//Exports que no se restringen a un area funcional
		while (itrAreasFuncionales.hasNext()) {
			Integer id = (Integer) itrAreasFuncionales.next();
			//System.err.println("Area funcional:"+id);
			m_userTaskMap.put(id, new ArrayList<Integer>());
			m_reportMap.put(id, new ArrayList<Integer>());
			m_actionMap.put(id, new ArrayList<Integer>());
			m_importMap.put(id, new ArrayList<Integer>());
			m_exportMap.put(id, new ArrayList<Integer>());
		}

		Iterator<Integer> itrClassesUserTask;

		itrClassesUserTask = m_kb.getSpecialized(Constants.IDTO_UTASK).iterator();

		while(itrClassesUserTask.hasNext()){
			int idtoUserTask=itrClassesUserTask.next();
			
			if(!isAbstractClass(idtoUserTask)){
				//System.err.println("UserTask:"+idtoUserTask);
				boolean isReport=m_kb.isSpecialized(idtoUserTask, Constants.IDTO_REPORT);
				boolean isAction=m_kb.isSpecialized(idtoUserTask, Constants.IDTO_ACTION);
				boolean isImport=m_kb.isSpecialized(idtoUserTask, Constants.IDTO_IMPORT);
				boolean isExport=m_kb.isSpecialized(idtoUserTask, Constants.IDTO_EXPORT);
				Iterator itrUserTask = m_kb.getIndividualsOfLevel(idtoUserTask,Constants.LEVEL_FILTER).iterator();
				while (itrUserTask.hasNext()) {
					Integer idoUserTask = (Integer) itrUserTask.next();
					if(isReport){
						m_idtoReportMap.put(idoUserTask, idtoUserTask);
						ObjectProperty paramProperty=null;
						try{
							paramProperty=getChild(idoUserTask, idtoUserTask, Constants.IdPROP_PARAMS, null, idtoUserTask, m_defaultSession);
						} catch (NotFoundException e) {
							//System.out.println("Report sin parametros");
						}
						if(paramProperty!=null){
							int idRange=getIdRange(paramProperty);
							int idtoRange=getClass(idRange);
							Iterator<ObjectProperty> itr=getChildren(idRange, idtoRange, null, idtoUserTask, m_defaultSession);
							m_reportParamMap.put(idoUserTask,new HashSet<Integer>());
							while(itr.hasNext()){
								ObjectProperty oProperty=itr.next();
								m_reportParamMap.get(idoUserTask).add(getClass(getIdRange(oProperty)));
							}
						}
					}
					else if(isAction)
						m_idtoActionMap.put(idoUserTask, idtoUserTask);
					else if(isImport)
						m_idtoImportMap.put(idoUserTask, idtoUserTask);
					else if(isExport)
						m_idtoExportMap.put(idoUserTask, idtoUserTask);
					else
						m_idtoUserTaskMap.put(idoUserTask, idtoUserTask);
					
					Iterator<Integer> itrAreasF = getAreasFuncionalesUserTask(idoUserTask,idtoUserTask,/*getUserRolRestrictive(getUserRols(idUserTask))*/null,m_defaultSession);
					if(!itrAreasF.hasNext()){//Si no tiene areas funcionales y es una accion la añadimos al id null
						if(isAction)
							m_actionMap.get(null).add(idoUserTask);
						else if(isImport)
							m_importMap.get(null).add(idoUserTask);
						else if(isExport)
							m_exportMap.get(null).add(idoUserTask);
					}
					while(itrAreasF.hasNext()){
						int idAreaFuncional=itrAreasF.next().intValue();
						ArrayList<Integer> list;
						if(isReport)
							list=m_reportMap.get(idAreaFuncional);
						else if(isAction)
							list=m_actionMap.get(idAreaFuncional);
						else if(isImport)
							list=m_importMap.get(idAreaFuncional);
						else if(isExport)
							list=m_exportMap.get(idAreaFuncional);
						else
							list=m_userTaskMap.get(idAreaFuncional);
						if (list != null)
							list.add(idoUserTask);
					}
				}
			}
		}
		//System.out.println("AreasFuncionales-UserTasks:"+m_userTaskMap.toString());
	}

	public Session getDDBBSession(){
		return m_ddbbSession;
	}

	public Session getDefaultSession(){
		return m_defaultSession;
	}
	
	public Session getDefaultSessionWithoutRules(){
		return m_defaultSessionWithoutRules;
	}

	public ArrayList<Integer> getAreasFuncionales(){
		Set<Integer> list=m_userTaskMap.keySet();
		if(list!=null)
			return new ArrayList<Integer>(list);
		return new ArrayList<Integer>();
	}

	//TODO Podria crear uno publico para que fuera accedido desde la aplicacion pero que use m_userTasks
	private Iterator<Integer> getAreasFuncionalesUserTask(int idoUserTask,int idtoUserTask,Integer userRol,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		return getIdObjectPropertyIterator(idoUserTask, idtoUserTask, Constants.IdPROP_MYFUNCTIONALAREA, userRol, m_user, getIdtoUserTask(idoUserTask),ses);
	}
	
	public ArrayList<Integer> getAreasFuncionales(int idoUserTask) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		ArrayList<Integer> areasFuncionales=new ArrayList<Integer>();
		Iterator<Integer> itr=m_userTaskMap.keySet().iterator();
		while(itr.hasNext()){
			int idAreaFuncional=itr.next();
			if(m_userTaskMap.get(idAreaFuncional).contains(idoUserTask))
				areasFuncionales.add(idAreaFuncional);
		}
		return areasFuncionales;
	}

	public Iterator<Integer> getIdoUserTasksReport(int idClassTarget,Integer idAreaFuncional) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		Iterator<Integer> itr=m_reportMap.get(idAreaFuncional).iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			int ido=itr.next();
			// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
			int idto=getClass(getIdRange((ObjectProperty)getProperty(ido,getIdtoUserTaskReport(ido),Constants.IdPROP_TARGETCLASS,null,null,m_defaultSessionWithoutRules)));
			if(idClassTarget==idto  || isSpecialized(idClassTarget, idto)){
				list.add(ido);
			}
		}
		return list.iterator();
	}
	
	/**
	 * Busca los reports que tienen como parametro alguna propiedad con un rango compatible con IdRange
	 * @param idRange
	 * @param idAreaFuncional
	 * @return
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 */
	public Iterator<Integer> getIdoUserTasksReportWithParam(int idRange,Integer idAreaFuncional) throws NotFoundException, IncoherenceInMotorException{
		Iterator<Integer> itr=m_reportMap.get(idAreaFuncional).iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			int ido=itr.next();
			HashSet<Integer> params=m_reportParamMap.get(ido);
			if(params!=null){
				for(Integer range:params){
					if(idRange==range  || isSpecialized(idRange, range)){
						list.add(ido);
					}
				}
			}
		}
		return list.iterator();
	}

	public Integer getIdtoUserTaskReport(int idoUserTaskReport){
		return m_idtoReportMap.get(idoUserTaskReport);
	}
	
	public Integer getIdoUserTaskReport(int idtoUserTask){
		Iterator<Integer> itr=m_idtoReportMap.keySet().iterator();
		Integer idoFound=null;
		while(idoFound==null && itr.hasNext()){
			int ido=itr.next();
			int idto=m_idtoReportMap.get(ido);
			if(idto==idtoUserTask)
				idoFound=ido;
		}
		return idoFound;
	}
	

	public ArrayList<Integer> getIdtoUserTasksDirectReport(int idClassTarget,Integer idAreaFuncional) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		ArrayList<Integer> directReports=new ArrayList<Integer>();
		ArrayList<Integer> listAreasFuncionales=new ArrayList<Integer>();
		
		if(idAreaFuncional==null)//Si no se le pasa area funcional se busca en todas las areas funcionales
			listAreasFuncionales.addAll(getAreasFuncionales());
		else listAreasFuncionales.add(idAreaFuncional);
		
		Iterator<Integer> itrAreasFuncionales=listAreasFuncionales.iterator();
		while(itrAreasFuncionales.hasNext()){
			Iterator<Integer> itrIdos=getIdoUserTasksReport(idClassTarget,itrAreasFuncionales.next());
			while(itrIdos.hasNext()){
				int idoUserTaskReport=itrIdos.next();
				int idtoUserTaskReport=getIdtoUserTaskReport(idoUserTaskReport);
				if(isDirectPrint(idoUserTaskReport))
					directReports.add(idtoUserTaskReport);
			}
		}
		
		if(directReports.size()>1)
			System.err.println("******** WARNING: Hay mas de un report "+directReports+" con impresion directa para la clase "+getLabelClass(idClassTarget,null)+" *********");
		
		return directReports;
	}
		
	public boolean isDirectPrint(int idoUserTaskReport) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		boolean directImpresion=false;
		int idtoUserTaskReport=getIdtoUserTaskReport(idoUserTaskReport);
		try{
			DataProperty propDirect=getField(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_DIRECTIMPRESION, null, idtoUserTaskReport, getDefaultSessionWithoutRules());
			Value value=propDirect.getUniqueValue();
			if(value!=null && ((BooleanValue)value).getBvalue()){
				directImpresion=true;
			}
		}catch(NotFoundException ex){
			//Nos sirve para solo coger los que tienen la property
		}
		
		return directImpresion;
	}
	
	public Iterator<Integer> getIdoUserTasksAction(int idClassSource,Integer idAreaFuncional) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Integer> listActions=new ArrayList<Integer>();
		if(idAreaFuncional==null){//Si no se le pasa area funcional se busca en todas las areas funcionales
			listActions.addAll(m_actionMap.get(null));
		}else{
			//Recorremos las acciones del areaFuncional del parametro y las que no tienen areaFuncional
			listActions.addAll(m_actionMap.get(idAreaFuncional));
			listActions.addAll(m_actionMap.get(null));
		}
		
		Iterator<Integer> itr=listActions.iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			int ido=itr.next();
			// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
			Integer idRange=getIdRange((ObjectProperty)getProperty(ido,getIdtoUserTaskAction(ido),Constants.IdPROP_SOURCECLASS,null,null,m_defaultSessionWithoutRules));
			if(idRange==null){
				System.err.println("ERROR:La property sourceClass de "+ ido +" no tiene rangoList"+" claseDominio:"+this.getLabelClass(this.getClass(ido), null));
			}
			int idto=getClass(idRange);
			if(idClassSource==idto || isSpecialized(idClassSource, idto)){
				list.add(ido);
			}
		}
		return list.iterator();
	}
	
//	Si hierarchy es true devolveria tambien las acciones cuyo targetClass apunte a un padre del idClassTarget, siempre que dicho padre sea abstracto
	public Iterator<Integer> getIdoUserTasksImport(int idClassTarget,boolean hierarchy) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		Iterator<Integer> itr=m_importMap.keySet().iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			Integer idoAreaFuncional=itr.next();
			Iterator<Integer> itrAction=m_importMap.get(idoAreaFuncional).iterator();
			while(itrAction.hasNext()){
				int ido=itrAction.next();
				//System.err.println((ObjectProperty)getProperty(ido,Constants.IdPROP_TARGETCLASS,null,null,m_defaultSession));
				// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
				int idRange=getIdRange((ObjectProperty)getProperty(ido,getIdtoUserTaskImport(ido),Constants.IdPROP_TARGETCLASS,null,null,m_defaultSessionWithoutRules));
				int idto=getClass(idRange);
				if(idClassTarget==idto || (hierarchy && isSpecialized(idClassTarget, idto) && isAbstractClass(idRange))){
					list.add(ido);
				}
			}
		}
		return list.iterator();
	}
	
	public Iterator<Integer> getIdoUserTasksExport(int idClassSource,Integer idAreaFuncional) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Integer> listActions=new ArrayList<Integer>();
		if(idAreaFuncional==null){//Si no se le pasa area funcional se busca en todas las areas funcionales
			listActions.addAll(m_exportMap.get(null));
		}else{
			//Recorremos las acciones del areaFuncional del parametro y las que no tienen areaFuncional
			listActions.addAll(m_exportMap.get(idAreaFuncional));
			listActions.addAll(m_exportMap.get(null));
		}
		
		Iterator<Integer> itr=listActions.iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			int ido=itr.next();
			// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
			Integer idRange=getIdRange((ObjectProperty)getProperty(ido,getIdtoUserTaskExport(ido),Constants.IdPROP_SOURCECLASS,null,null,m_defaultSessionWithoutRules));
			if(idRange==null)
				System.err.println("ERROR:La property sourceClass de "+ ido +" no tiene rangoList");
			int idto=getClass(idRange);
			if(idClassSource==idto || isSpecialized(idClassSource, idto)){
				list.add(ido);
			}
		}
		return list.iterator();
	}
	
	//Si hierarchy es true devolveria tambien las acciones cuyo targetClass apunte a un padre del idClassTarget, siempre que dicho padre sea abstracto
	public Iterator<Integer> getIdoUserTasksAction(int idClassTarget,boolean hierarchy,int idAreaFuncional) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Integer> listActions=new ArrayList<Integer>();
		//Recorremos las acciones del areaFuncional del parametro y las que no tienen areaFuncional 
		listActions.addAll(m_actionMap.get(idAreaFuncional));
		listActions.addAll(m_actionMap.get(null));
		
		Iterator<Integer> itr=listActions.iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		
		while(itr.hasNext()){
			int ido=itr.next();
			//System.err.println((ObjectProperty)getProperty(ido,Constants.IdPROP_TARGETCLASS,null,null,m_defaultSession));
			// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
			int idRange=getIdRange((ObjectProperty)getProperty(ido,getIdtoUserTaskAction(ido),Constants.IdPROP_TARGETCLASS,null,null,m_defaultSessionWithoutRules));
			int idto=getClass(idRange);
			if(idClassTarget==idto || (hierarchy && isSpecialized(idClassTarget, idto) && isAbstractClass(idRange))){
				list.add(ido);
			}
		}
		return list.iterator();
	}

	public Integer getIdtoUserTaskAction(int idoUserTaskAction){
		return m_idtoActionMap.get(idoUserTaskAction);
	}
	
	public Integer getIdtoUserTaskImport(int idoUserTaskImport){
		return m_idtoImportMap.get(idoUserTaskImport);
	}
	
	public Integer getIdtoUserTaskExport(int idoUserTaskExport){
		return m_idtoExportMap.get(idoUserTaskExport);
	}
	
	public Integer getIdoUserTaskAction(int idtoUserTask){
		Iterator<Integer> itr=m_idtoActionMap.keySet().iterator();
		Integer idoFound=null;
		while(idoFound==null && itr.hasNext()){
			int ido=itr.next();
			int idto=m_idtoActionMap.get(ido);
			if(idto==idtoUserTask)
				idoFound=ido;
		}
		return idoFound;
	}
	
	public Integer getIdoUserTaskImport(int idtoUserTask){
		Iterator<Integer> itr=m_idtoImportMap.keySet().iterator();
		Integer idoFound=null;
		while(idoFound==null && itr.hasNext()){
			int ido=itr.next();
			int idto=m_idtoImportMap.get(ido);
			if(idto==idtoUserTask)
				idoFound=ido;
		}
		return idoFound;
	}
	
	public Integer getIdoUserTaskExport(int idtoUserTask){
		Iterator<Integer> itr=m_idtoExportMap.keySet().iterator();
		Integer idoFound=null;
		while(idoFound==null && itr.hasNext()){
			int ido=itr.next();
			int idto=m_idtoExportMap.get(ido);
			if(idto==idtoUserTask)
				idoFound=ido;
		}
		return idoFound;
	}

	public Iterator<Integer> getIdoUserTasks(int idAreaFuncional){
		ArrayList<Integer> list=m_userTaskMap.get(idAreaFuncional);
		if(list!=null)
			return list.iterator();
		return new ArrayList<Integer>().iterator();
	}
	
	public boolean hasUserTasks(int idAreaFuncional,Integer userRol) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		boolean hasUserTasks=false;
		ArrayList<Integer> list=m_userTaskMap.get(idAreaFuncional);
		if(list!=null){
			Iterator<Integer> itr=list.iterator();
			//Miramos que al menos hay una userTask sobre la que tenemos permiso de view
			while(!hasUserTasks && itr.hasNext()){
				int idoUserTask=itr.next();
				int idtoUserTask=getIdtoUserTask(idoUserTask);
				ObjectProperty propTarget=getTarget(idoUserTask, idtoUserTask, userRol, m_defaultSessionWithoutRules);
				LinkedHashMap<Integer,ArrayList<UserAccess>> accessUserTasks=getAllAccessIndividual(getIdRange(propTarget), userRol, propTarget.getTypeAccess(), idtoUserTask);
				AccessAdapter tableAccess=new AccessAdapter(accessUserTasks,propTarget,false,false);
				if(!tableAccess.getUserTasksAccess(AccessAdapter.VIEW).isEmpty() && (!tableAccess.getUserTasksAccess(AccessAdapter.FIND).isEmpty() || !tableAccess.getUserTasksAccess(AccessAdapter.NEW_AND_REL).isEmpty()))
					hasUserTasks=true;
					
			}
		}
		
		return hasUserTasks;
	}

//	/*Devuelve las userTasks que apuntan como target a la clase indicada dentro de un area funcional*/
//	public ArrayList<Integer> getIdoUserTasks(/*int idAreaFuncional,*/int idClassTarget,Integer userRol){
//	ArrayList<Integer> listFound=new ArrayList<Integer>();

//	/*ArrayList<Integer> list=m_userTaskMap.get(idAreaFuncional);*/
//	Iterator<Integer> itrAreasF=m_userTaskMap.keySet().iterator();
//	while(itrAreasF.hasNext()){
//	ArrayList<Integer> list=m_userTaskMap.get(itrAreasF.next());
//	if(list!=null){
//	Iterator<Integer> itr=list.iterator();
//	while(itr.hasNext()){
//	int idoUserTask=itr.next();
//	ObjectProperty property=getTarget(idoUserTask,userRol);
//	int idClass=property.getRangoList().getFirst();
//	if(idClass==idClassTarget)
//	listFound.add(idoUserTask);
//	}
//	}
//	}
//	return listFound;
//	}

	//TODO Sabemos que solo hay un ido por cada idto, pero en un futuro si cambia tendremos que modificar este metodo
	public Integer getIdoUserTask(int idtoUserTask){
		Iterator<Integer> itr=m_idtoUserTaskMap.keySet().iterator();
		Integer idoFound=null;
		while(idoFound==null && itr.hasNext()){
			int ido=itr.next();
			int idto=m_idtoUserTaskMap.get(ido);
			if(idto==idtoUserTask)
				idoFound=ido;
		}
		return idoFound;
	}	

	public Integer getIdtoUserTask(int idoUserTask){
		return m_idtoUserTaskMap.get(idoUserTask);
	}
	
	//TODO Esto se podria optimizar usando un mapeo targetClass-usertask en el constructor
	public ArrayList<Integer> getIdtoUserTasks(int idClassTarget,Integer idAreaFuncional,boolean superior,boolean specialized,boolean onlyAbstractForSpecialized) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		Iterator<Integer> itr=null;
		if(idAreaFuncional!=null)
			itr=m_userTaskMap.get(idAreaFuncional).iterator();
		else itr=m_idtoUserTaskMap.keySet().iterator();
		ArrayList<Integer> list=new ArrayList<Integer>();
		while(itr.hasNext()){
			Integer idoUserTask=itr.next();
			//int idto=m_idtoUserTaskMap.get(idoUserTask);
			// Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
			ObjectProperty prop=getChild(idoUserTask,getIdtoUserTask(idoUserTask),Constants.IdPROP_TARGETCLASS,null,null,m_defaultSessionWithoutRules);
			if(prop!=null)
			{
				//System.err.println("IdtoUserTask:"+prop.getIdto());
				int idRange=getIdRange(prop);
				int idto=getClass(getIdRange(prop));
				if(idClassTarget==idto || (specialized && isSpecialized(idClassTarget, idto) && (!onlyAbstractForSpecialized || isAbstractClass(idRange))) || (superior && isSpecialized(idto, idClassTarget))){
					list.add(prop.getIdto());
				}
			}else{
				//System.err.println("Propppp nula para idtoUserTask:"+m_idtoUserTaskMap.get(idoUserTask)+" idoUserTask:"+idoUserTask);
			}
		}
		return list;
	}

	public ArrayList<Integer> getUserRols(int idoUserTask,int idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Integer> list=new ArrayList<Integer>();
		Iterator<Integer> itr=getIdObjectPropertyIterator(idoUserTask, idtoUserTask, Constants.IdPROP_USERROL, null, m_user, idtoUserTask,ses);
		if(itr.hasNext()){
			list.add(itr.next());
		}
		return list;
	}

	/*De momento se queda con el primero pero deberia seguir alguna politica de eleccion*/
	public Integer getUserRolRestrictive(ArrayList<Integer> userRols){
		Iterator<Integer> itr=userRols.iterator();
		if(itr.hasNext()){
			return itr.next();
		}
		return null;
	}

	public Integer getGroup(int idProp,Integer idto,Integer idtoUserTask) throws NotFoundException{
		return groupsComponents.getIdGroup(idProp, idto, idtoUserTask);
	}

	public void deleteObject(int ido,int idto,String rdn,Integer userRol, Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		
		Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI, "delete", "ido:"+ido+" idto:"+idto+" rdn:"+rdn+" idtoUserTask:"+idtoUserTask);
		m_kb.deleteObject(ido,idto,rdn,userRol,m_user,idtoUserTask,ses);
	}

	public String getLabelAreaFuncional(int idAreaFuncional) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		//System.err.println("Area Funcional:"+idAreaFuncional);
		
		//Se usa defaultSession como sesion porque es informacion ya cargada en motor de la inicializacion
		return Utils.normalizeLabel((String)getValueData(getRDN(idAreaFuncional,Constants.IDTO_FUNCTIONAL_AREA,null,null,m_defaultSessionWithoutRules)));
	}

	public String getLabelUserTask(int idtoUserTask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		String label=m_kb.getAliasOfClass(getClass(idtoUserTask), idtoUserTask);
		return Utils.normalizeLabel(label.replaceFirst("Task_|rp@",""));
	}

	public String getLabelClass(int id, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		Integer idClass=getClass(id);
		if(idClass==null) return null;
		String label=m_kb.getAliasOfClass(getClass(id), utask);
		/*if(utask==null && m_kb.isSpecialized(id, Constants.IDTO_UTASK))
			return Utils.normalizeLabel(this.alias.getLabelUtask(getClass(id)));
		return Utils.normalizeLabel(this.alias.getLabelClass(getClass(id),utask));*/
		return Utils.normalizeLabel(label);
	}
	public String getLabelGroup(int idGroup, Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		String group=m_kb.getAliasOfGroup(idGroup, groupsComponents.getNameGroup(idGroup), utask);
		//return Utils.normalizeLabel(this.alias.getLabelGroup(idGroup, groupsComponents.getNameGroup(idGroup), utask));
		return Utils.normalizeLabel(group);
	}

	public String getLabelProperty(Integer prop,Integer idto,Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		String label=m_kb.getAliasOfProperty(idto, prop, utask);
		/*if (idto!=null && utask!=null && group!=null && group!=0){
			label= this.alias.getLabelProp(prop,getClass(idto),group,  utask);
		}else{
			label=this.alias.getLabelProp(prop,getClass(idto),null,  utask);
		}*/
		return Utils.normalizeLabel(label.replaceFirst("@rp@.++",""));
	}
	public String getLabelProperty(Property prop,Integer idto,Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return getLabelProperty(prop.getIdProp(),idto,utask);
	}
	
	public String getLabelProperty(PropertyValue prop,Integer idto,Integer utask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return getLabelProperty(prop.getIdProp(),idto,utask);
	}

//	public int getClassObject(int idObject){
//	Integer idto=m_kb.getClassOfObject(idObject);
//	return idto;
//	}

	public ObjectProperty getTarget(Integer idoUserTask,int idtoUserTask,Integer userRol,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return getObjectProperty(idoUserTask, idtoUserTask, Constants.IdPROP_TARGETCLASS, userRol, m_user, idtoUserTask ,ses);
	}

	public DataProperty getOwner(Integer idoUserTask,int idtoUserTask, Integer userRol,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		Iterator<DataProperty> itr= getDataPropertyIterator(idoUserTask, idtoUserTask, Constants.IdPROP_OWNER, userRol, m_user, idtoUserTask,ses);
		if(itr.hasNext())
			return itr.next();
		return null;
	}

	public ArrayList<Integer> getSpecializedFilters(int ido,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<Integer> list=null;

		list=m_kb.getSpecializedFilters(ido, userRol, m_user, idtoUserTask, ses);
		return list;
	}
	
	public HashSet<Integer> getSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return m_kb.getSpecialized(idto);
	}

	public HashSet<Integer> getDirectSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return m_kb.getDirectSpecialized(idto);
	}
	
	public boolean isSpecialized(int idto,int idtoParent) throws NotFoundException, IncoherenceInMotorException{
		return m_kb.isSpecialized(idto, idtoParent); 
	}

	public Iterator<Integer> getAncestors(int idto) throws NotFoundException, IncoherenceInMotorException {
		return m_kb.getSuperior(idto);
	}
	
	public HashSet<Integer> getParents(int idto) throws NotFoundException, IncoherenceInMotorException {
		return m_kb.getDirectSuperior(idto);
	}

	/*public Integer getOperationsTarget(int idTarget,int idAreaFuncional){
		Integer operations=0;
		Iterator<Integer> itr=getUserTasks(idAreaFuncional);
		while(itr.hasNext()){
			int idUserTask=itr.next();
			int id=getIdObject(getTarget(idUserTask));
			if(id==idTarget){
				//Mediante una OR vamos añadiendo todas las operaciones ya que estan
				//codificadas en binario con numeros que son potencia de 2
				operations|=getOperationUserTask(idUserTask);
			}	
		}
		return operations;
	}*/

	public Iterator<ObjectProperty> getChildren(Integer idObject, int idtoObject, Integer userRol, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return getObjectPropertyIterator(idObject,idtoObject,userRol,idtoUserTask, ses);
	}

	public ObjectProperty getChild(Integer idObject, int idtoObject, int idProp, Integer userRol, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, OperationNotPermitedException{
		return getObjectProperty(idObject,idtoObject,idProp,userRol,m_user,idtoUserTask,ses);
	}
	/*public ObjectProperty getChild(int idObject, int idChild, Integer userRol, Integer idUserTask){
		Iterator<ObjectProperty> itr=getObjectPropertyIterator(idObject,userRol,m_user,idUserTask);
		if(itr.hasNext()){
			ObjectProperty property=itr.next();
			int id=getIdObject(property);
			if(id==idChild)
				return property;
		}
		return null;
	}*/

	/*public String getLabelProperty(int idProp){
		return m_kb.getPropertyName(idProp);
	}*/

	public Iterator<Property> getProperties(Integer idObject,int idtoObject,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		if(m_instance!=null){
			return m_instance.getAllPropertyIterator(idObject!=null?idObject:idtoObject);
		}else{
			HashMap<Integer,Property> mapProperties=new HashMap<Integer, Property>();
			Iterator<Property> itr=m_kb.getAllPropertyIterator(idObject!=null && !idObject.equals(idtoObject)?idObject:null,idtoObject,userRol,m_user,idtoUserTask,ses);
			
			/*if(isAbstractClass(idObject!=null && !idObject.equals(idtoObject)?idObject:idtoObject)){
				System.err.println("ENTRAAAA en getProperties abstract");
				while(itr.hasNext()){
					Property prop=itr.next();
					mapProperties.put(prop.getIdProp(), prop);
				}
				Iterator<Integer> itrSpecialized=getSpecialized(idtoObject).iterator();
				while(itrSpecialized.hasNext()){
					int idtoSpecialized=itrSpecialized.next();
					Iterator<Property> itrPropsSpecialized=m_kb.getAllPropertyIterator(null,idtoSpecialized,userRol,m_user,idtoUserTask,ses);
					while(itrPropsSpecialized.hasNext()){
						Property prop=itrPropsSpecialized.next();
						if(!mapProperties.containsKey(prop.getIdProp())){
							Property propClone=prop.clone();
							propClone.setIdo(idObject);
							propClone.setIdto(idtoObject);
							mapProperties.put(prop.getIdProp(), propClone);
							System.err.println("AÑADEEE "+propClone);
						}else{
							//TODO Permisos menos restrictivos
						}
					}
				}
				itr=mapProperties.values().iterator();
			}*/
			return itr;
		}

	}

	public Property getProperty(Integer idObject,int idtoObject,int idProp, Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		if(m_instance!=null){
			return m_instance.getProperty(idObject!=null?idObject:idtoObject,idProp);
		}else{
			Property prop=null;
			//try{
				prop=m_kb.getProperty(idObject!=null && !idObject.equals(idtoObject)?idObject:null,idtoObject,idProp,userRol,m_user,idtoUserTask,ses);
			/*}catch(NotFoundException ex){
				if(isAbstractClass(idObject!=null && !idObject.equals(idtoObject)?idObject:idtoObject)){
					System.err.println("ENTRAAAA en getProperty abstract");
					Iterator<Integer> itrSpecialized=getSpecialized(idtoObject).iterator();
					while(itrSpecialized.hasNext()){
						int idtoSpecialized=itrSpecialized.next();
						if(m_kb.hasProperty(idtoSpecialized,idProp)){
							Property propSpecialized=m_kb.getProperty(null,idtoSpecialized,idProp,userRol,m_user,idtoUserTask,ses);
							prop=propSpecialized.clone();
							prop.setIdo(idObject);
							prop.setIdto(idtoObject);
							System.err.println("DEVUELVE "+prop);
						}
					}
				}
				if(prop==null)
					throw ex;
			}*/
			
			return prop;
			 
		}

	}

	public Iterator<DataProperty> getFields(int idObject,int idtoObject,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return getDataPropertyIterator(idObject,idtoObject,userRol,idtoUserTask, ses);
	}

	public DataProperty getField(int idObject, int idtoObject, int idProp, Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		Iterator<DataProperty> itr=getDataPropertyIterator(idObject,idtoObject,idProp,userRol,m_user,idtoUserTask,ses);
		if(itr.hasNext()){
			DataProperty property=itr.next();
			return property;
		}
		return null;
	}

	public DataProperty getRDN(int idObject, int idtoObject, Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return getField(idObject, idtoObject, Constants.IdPROP_RDN, userRol, idtoUserTask, ses);
	}

	/*public Integer getClassObject(int idObject){
		Iterator<Integer> itr= getObjectPropertyIterator(idObject, helperConstant.CLASS);
		if(itr.hasNext())
			return itr.next();
		return null;
	}*/

	/*Busca la userTask que posee el objectProperty con las caracteristicas dadas y devuelve su ObjectProperty target*/
	/*-------------------------HAY QUE COMPROBAR SI DE VERDAD ES UNICO O NO-------------------------------*/
	/*public ObjectProperty getObjectTargetFromId(int idObject,int operation,int idAreaFuncional){
		Iterator<Integer> itr=getUserTasks(idAreaFuncional);
		while(itr.hasNext()){
			int idUserTask=itr.next();
			ObjectProperty target=getTarget(idUserTask);
			if(getIdObject(target)==idObject){
				/*int operationUserTask=getOperationUserTask(idUserTask);*/
	/*			if(target.getTypeAccess()==operation)
					return target;
			}
		}
	}*/

	public Integer getIdoValue(ObjectProperty property){
		LinkedList<Value> valueList=property.getValues();
		if(valueList.size()>0)
			return ((ObjectValue)valueList.getFirst()).getValue();
		return null;
	}

	public Integer getIdtoValue(ObjectProperty property){
		LinkedList<Value> valueList=property.getValues();
		if(valueList.size()>0)
			return ((ObjectValue)valueList.getFirst()).getValueCls();
		return null;
	}
	
	public ObjectValue getValue(ObjectProperty property){
		LinkedList<Value> valueList=property.getValues();
		return (ObjectValue)valueList.getFirst();
	}

	public Integer getIdRange(ObjectProperty property){
		LinkedList<Integer> rangoList=property.getRangoList();
		if(rangoList.size()>0)
			return rangoList.getFirst();
		return null;
	}

	public Integer getIdRange(ObjectProperty property,int valueCls) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		Integer idRange=null;
		LinkedList<Integer> rangoList=property.getRangoList();
		Iterator<Integer> itr=rangoList.iterator();
		while(idRange==null && itr.hasNext()){
			int idRang=itr.next();
			if(getClass(idRang)==valueCls)
				idRange=idRang;
		}

		return idRange;
	}

	public Integer getIdoEnum(ObjectProperty property){
		LinkedList<ObjectValue> enumList=property.getEnumList();
		if(enumList.size()>0)
			return enumList.getFirst().getValue();
		return null;
	}

	//TODO cambiar pos por valueCls
	public Integer getIdoEnum(ObjectProperty property,int pos){
		LinkedList<ObjectValue> enumList=property.getEnumList();
		if(enumList.size()>=pos)
			return enumList.get(pos).getValue();
		return null;
	}

	public Integer getIdtoEnum(ObjectProperty property){
		LinkedList<ObjectValue> enumList=property.getEnumList();
		if(enumList.size()>0)
			return enumList.getFirst().getValueCls();
		return null;
	}
	
	/*public Iterator<Integer> getIdtoEnums(ObjectProperty property){
		ArrayList<Integer> list=new ArrayList<Integer>();

		LinkedList<ObjectValue> enumList=property.getEnumList();
		Iterator<ObjectValue> itr=enumList.iterator();
		while(itr.hasNext()){
			ObjectValue value=itr.next();
			list.add(value.getValue());
		}
		return list.iterator();
	}*/

//	TODO cambiar pos por valueCls
	public Integer getIdtoEnum(ObjectProperty property,int pos){
		LinkedList<ObjectValue> enumList=property.getEnumList();
		if(enumList.size()>=pos)
			return enumList.get(pos).getValueCls();
		return null;
	}

	// Hace lo mismo que getIdoValue pero devolviendo varios valores
	public Iterator<ObjectValue> getValues(ObjectProperty property){
		ArrayList<ObjectValue> list=new ArrayList<ObjectValue>();

		LinkedList<Value> valueList=property.getValues();
		Iterator<Value> itr=valueList.iterator();
		while(itr.hasNext()){
			list.add((ObjectValue)itr.next());
		}
		return list.iterator();
	}
	
	// Hace lo mismo que getIdoValue pero devolviendo varios valores
	public ArrayList<Integer> getIdoValues(ObjectProperty property){
		ArrayList<Integer> list=new ArrayList<Integer>();

		LinkedList<Value> valueList=property.getValues();
		Iterator<Value> itr=valueList.iterator();
		while(itr.hasNext()){
			ObjectValue value=(ObjectValue)itr.next();
			list.add(value.getValue());
		}
		return list;
	}
	
	//Hace lo mismo que getIdoEnum pero devolviendo varios valores
	public Iterator<ObjectValue> getEnums(ObjectProperty property){
		ArrayList<ObjectValue> list=new ArrayList<ObjectValue>();

		LinkedList<ObjectValue> enumList=property.getEnumList();
		Iterator<ObjectValue> itr=enumList.iterator();
		while(itr.hasNext()){
			list.add((ObjectValue)itr.next());
		}
		return list.iterator();
	}
	
	//Hace lo mismo que getIdoEnum pero devolviendo varios valores
	public Iterator<Integer> getIdoEnums(ObjectProperty property){
		ArrayList<Integer> list=new ArrayList<Integer>();

		LinkedList<ObjectValue> enumList=property.getEnumList();
		Iterator<ObjectValue> itr=enumList.iterator();
		while(itr.hasNext()){
			ObjectValue value=(ObjectValue)itr.next();
			list.add(value.getValue());
		}
		return list.iterator();
	}

	public Integer getValueObjectNoConcrete(ObjectProperty property,Integer idClass){
		LinkedList<Value> valueList=property.getValues();
		Iterator<Value> itr=valueList.iterator();
		while(itr.hasNext()){
			ObjectValue value=(ObjectValue)itr.next();
			if(value.getValueCls()==idClass)
				return value.getQ();
		}
		return null;
	}

	/*
	 * Devuelve un iterador con las properties que se utilizan para construir cada columna de una tabla. Estas properties
	 * columnas se obtienen a partir de los rangos, sin tener en cuenta los values de los objectProperty
	 * */
	public ArrayList<Column> getColumnsObject(int idObject,int idtoObject,int idtoTable,int idtoParent,Integer userRol,Integer idtoUserTask,Session ses,boolean filterMode,boolean structural,boolean onlyFirstLevel,HashMap<String,String> aliasMap) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return columnsTable.getColumns(idObject,idtoObject,idtoTable,idtoParent, userRol, idtoUserTask, ses, this, filterMode, false, structural, onlyFirstLevel, aliasMap);
		//return getTableObject(idObject,userRol,idtoUserTask,ses,false).iterator();
	}

	/*
	 * Devuelve un array con las properties que se necesitan para una tabla. Estas properties son los rdn del objeto principal y
	 * de sus objetos directos.
	 * 
	 * Si inTree=true devuelve todas las properties que forman el arbol hasta llegar a las properties columnas,tanto del rango como del value
	 * Si inTree=false devuelve solo las properties columnas. Y ademas lo hace a partir de los rangos, no mira los values de los objectProperty
	 * */

//	private ArrayList<Property> getTableObject(int idObject,Integer userRol,Integer idtoUserTask,Session ses,boolean inTree) throws NotFoundException{
//	ArrayList<Property> list=new ArrayList<Property>();	
//	Property propertyRDN=getRDN(idObject,userRol,idtoUserTask,ses);
//	if(propertyRDN!=null)
//	list.add(propertyRDN);
//	else System.err.println("Error:propertyRDN de una columna de la tabla es null, siendo ido:"+idObject);	

//	Iterator<Property> itrProp=getProperties(idObject,userRol,idtoUserTask,ses);
//	while(itrProp.hasNext()){
//	Property prop=itrProp.next();
//	if(prop instanceof ObjectProperty){
//	ObjectProperty property=(ObjectProperty)prop;
//	//Condicion para mostrar o no en la tabla
//	if(property.getCardMin()!=null && property.getCardMin()==1 && property.getCardMax()!=null && property.getCardMax()==1/* && property.getEnumList().isEmpty()*/){
//	Iterator<Integer> itrRanges=property.getRangoList().iterator();
//	boolean isAdded=false;
//	while(itrRanges.hasNext()){
//	int idRange=itrRanges.next();
//	//if(Constants.isIDTemporal(idRange)){// TODO esta comprobacion si siempre vinieran filtros no haria falta. En enumerados ahora vienen clases
//	Property propRDN=getRDN(idRange,userRol,idtoUserTask,ses);
//	System.out.println("getTableObject idRange:"+idRange+" property"+propRDN );
//	if(propRDN!=null){
//	list.add(propRDN);
//	isAdded=true;
//	}else System.err.println("Error:propRDNRange de una columna de la tabla es null, siendo ido:"+idRange);
//	//}
//	}
//	if(inTree){
//	Iterator<Value> itrValues=property.getValues().iterator();
//	while(itrValues.hasNext()){
//	ObjectValue objectValue=(ObjectValue)itrValues.next();
//	int value=objectValue.getValue();

//	Property propRDN=getRDN(value,userRol,idtoUserTask,ses);
//	System.out.println("getTableObject value:"+value+" property"+propRDN );
//	if(propRDN!=null){
//	list.add(propRDN);
//	isAdded=true;
//	}else System.err.println("Error:propRDNValues de una columna de la tabla es null, siendo ido:"+value);
//	}

//	if(isAdded)
//	list.add(property);
//	}

//	/*if(ranges.size()>0){
//	Integer idRange=ranges.get(0);
//	Property propRDN=getRDN(idRange,userRol,idtoUserTask,ses);
//	if(propRDN!=null)
//	list.add(propRDN);
//	}*/
//	}
//	}else{
//	DataProperty property=(DataProperty)prop;
//	//Condicion para mostrar o no en la tabla
//	if(property.getCardMin()!=null && property.getCardMin()==1/* && property.getCardMax()!=null && property.getCardMax()==1*//* && property.getEnumList().isEmpty()*/){
//	list.add(property);
//	}
//	}
//	}
//	return list;
//	}

//	private ArrayList<Property> getTableObject(int idObject,Integer userRol,Integer idtoUserTask,Session ses,boolean inTree) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException{
//		ArrayList<Property> list=new ArrayList<Property>();	
//		Property propertyRDN=getRDN(idObject,userRol,idtoUserTask,ses);
//		if(propertyRDN!=null)
//			list.add(propertyRDN);
//		else System.err.println("Error:propertyRDN de una columna de la tabla es null, siendo ido:"+idObject);	
//
//		//TODO Quitar, solo es para pruebas
//		if(getClass(idObject)==110){
//			Property property=getChild(idObject,278,userRol,idtoUserTask,ses);
//			if(property!=null)
//				list.add(property);
//
//			Property proper=getField(idObject,198,userRol,idtoUserTask,ses);
//			if(proper!=null)
//				list.add(proper);
//		}
//		if(getClass(idObject)==65){
//			Property propert=getField(idObject,116,userRol,idtoUserTask,ses);
//			System.err.println("Knolegd: "+propert);
//			if(propert!=null)
//				list.add(propert);
//		}
//
//		Iterator<ObjectProperty> itrProp=getChildren(idObject,userRol,idtoUserTask,ses);
//		while(itrProp.hasNext()){
//			ObjectProperty property=itrProp.next();
//			//Condicion para mostrar o no en la tabla
//			if(property.getCardMin()!=null && property.getCardMin()==1 && property.getCardMax()!=null && property.getCardMax()==1 && property.getEnumList().isEmpty()){
//				Iterator<Integer> itrRanges=property.getRangoList().iterator();
//				boolean isAdded=false;
//				while(itrRanges.hasNext()){
//					int idRange=itrRanges.next();
//					//if(Constants.isIDTemporal(idRange)){// TODO esta comprobacion si siempre vinieran filtros no haria falta. En enumerados ahora vienen clases
//					Property propRDN=getRDN(idRange,userRol,idtoUserTask,ses);
//					System.out.println("getTableObject idRange:"+idRange+" property"+propRDN );
//					if(propRDN!=null){
//						list.add(propRDN);
//						isAdded=true;
//					}else System.err.println("Error:propRDNRange de una columna de la tabla es null, siendo ido:"+idRange);
//					//}
//				}
//				if(inTree){
//					Iterator<Value> itrValues=property.getValues().iterator();
//					while(itrValues.hasNext()){
//						ObjectValue objectValue=(ObjectValue)itrValues.next();
//						int value=objectValue.getValue();
//
//						Property propRDN=getRDN(value,userRol,idtoUserTask,ses);
//						System.out.println("getTableObject value:"+value+" property"+propRDN );
//						if(propRDN!=null){
//							list.add(propRDN);
//							isAdded=true;
//						}else System.err.println("Error:propRDNValues de una columna de la tabla es null, siendo ido:"+value);
//					}
//
//					if(isAdded)
//						list.add(property);
//				}
//
//				/*if(ranges.size()>0){
//					Integer idRange=ranges.get(0);
//					Property propRDN=getRDN(idRange,userRol,idtoUserTask,ses);
//					if(propRDN!=null)
//						list.add(propRDN);
//				}*/
//			}
//		}
//		return list;
//	}

	public boolean isInverse(int ido,Property property) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		boolean isInverse=false;
		if(property instanceof ObjectProperty)
			isInverse=(property.getCardMax()!=null && property.getCardMax()==1) && ((ObjectProperty)property).getValues().contains(buildValue(ido, getClass(ido)));
		return isInverse;
	}

//	public ArrayList<SelectQuery> getSelectQuery(int idObject,int idtoObject,int idtoParent,Integer userRol,Integer idtoUserTask,Session ses,boolean filterMode,boolean structural,boolean onlyFirstLevel) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
//		ArrayList<SelectQuery> listSelect=new ArrayList<SelectQuery>();
//		ArrayList<Column> columList=getColumnsObject(idObject,idtoObject,idtoParent,userRol,idtoUserTask,ses,/*true,false,onlyFirstLevel*/filterMode,structural,onlyFirstLevel);
//		//System.err.println("getSelectQuery:"+propNameHash);
//		Iterator<Column> itr=columList.iterator();
//		while(itr.hasNext()){
//			Column col=itr.next();
//			Property property=col.getProperty();
//			int ido=property.getIdo();
//			//int idto=property.getIdto();
//			int idProp=property.getIdProp();
//			Integer propFilter=col.getFilterIdProp();
//			String valueFilter=col.getFilterValue();
//
//			SelectQuery select=new SelectQuery(String.valueOf(ido),idProp,propFilter,valueFilter);
//			select.setAlias(col.getName());
//			listSelect.add(select);
//		}
//		return listSelect;
//	}
	
	public void setState(int ido,int idto, String rdnValue,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		int valueCls=Constants.IDTO_ESTADOREALIZACION;
		int idProp=Constants.IdPROP_ESTADOREALIZACION;
		
		ObjectProperty property=getChild(ido, idto, idProp, userRol, idtoUserTask, ses);
		Integer valueIdo=getIdoValue(property);
		Integer valueOld=valueIdo;
		Integer valueNew=m_idoStateMap.get(rdnValue);
		if(valueNew==null)
			System.err.println("ERROR: El rdn de estado '"+rdnValue+"' no existe en motor");
		setValue(/*property,*/ido,idProp, buildValue(valueNew,/*idtoFilter*/valueCls),buildValue(valueOld,/*idtoFilter*/valueCls)/*, new session()*/,/*operation*/userRol,idtoUserTask,ses);
	}

//	public ArrayList<SelectQuery> getSelectQuery(int idObject,int idClass,Integer userRol,Integer idtoUserTask,Session ses){
//	//ArrayList<Property> list=new ArrayList<Property>();	
//	ArrayList<SelectQuery> listSelect=new ArrayList<SelectQuery>();
//	/*Property propertyRDN=getRDN(idObject,userRol,idtoUserTask,ses);
//	if(propertyRDN!=null){*/
//	SelectQuery select=new SelectQuery(idObject,idClass,/*propertyRDN.getIdProp()*/Constants.IdPROP_RDN);
//	listSelect.add(select);
//	//list.add(propertyRDN);
//	/*}*/	
//	System.out.println("TIENE llamada: ido:"+idObject+" idto:"+idClass);

//	//fillSelectQuery(idObject, idClass, userRol, idtoUserTask, listSelect, new HashSet<Integer>(),ses);
//	Iterator<ObjectProperty> itrProp=getChildren(idObject,userRol,idtoUserTask,ses);
//	while(itrProp.hasNext()){
//	ObjectProperty property=itrProp.next();
//	if(property.getCardMin()!=null && property.getCardMin()==1 && property.getCardMax()!=null && property.getCardMax()==1){
//	/*Integer ido=getIdoFilter(property);
//	Integer idto=getIdtoFilter(property);*/
//	int ido=getIdRange(property);
//	int idto=getClass(ido);
//	//if(ido!=null){
//	/*Property propertyRDN=getRDN(ido,userRol,idtoUserTask,ses);
//	if(propertyRDN!=null){*/
//	SelectQuery sel=new SelectQuery(ido,idto,/*propertyRDN.getIdProp()*/Constants.IdPROP_RDN);
//	listSelect.add(sel);
//	/*}*/
//	System.out.println("TIENE: ido:"+ido+" idto:"+idto);
//	}
//	}
//	return listSelect;
//	}

//	private void fillSelectQuery(int idObject,int idClass,Integer userRol,Integer idtoUserTask,ArrayList<SelectQuery> listSelect,HashSet<Integer> listProcessedChildren,Session ses){
//	listProcessedChildren.add(idObject);
//	Iterator<ObjectProperty> itrProp=getChildren(idObject,userRol,idtoUserTask,ses);
//	while(itrProp.hasNext()){
//	ObjectProperty property=itrProp.next();
//	/*Integer ido=getIdoFilter(property);
//	Integer idto=getIdtoFilter(property);*/
//	int ido=getIdRange(property);
//	int idto=getClass(ido);
//	if(Constants.isIDTemporal(ido)){
//	//if(ido!=null){
//	/*Property propertyRDN=getRDN(ido,userRol,idtoUserTask,ses);
//	if(propertyRDN!=null){*/
//	SelectQuery select=new SelectQuery(ido,idto,/*propertyRDN.getIdProp()*/Constants.IdPROP_RDN);
//	listSelect.add(select);
//	/*}*/
//	System.out.println("TIENE: ido:"+ido+" idto:"+idto);
//	if (!listProcessedChildren.contains(ido))
//	fillSelectQuery(ido,idto,userRol,idtoUserTask,listSelect,listProcessedChildren,ses);
//	}
//	//}
//	}
//	}

	public Integer getClass(int id) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException{
		return m_kb.getClassOf(id);
	}

	public ArrayList<SelectQuery> getSelectTask(Session ses){	
		int idUserTask=Constants.IDTO_UTASK;
		ArrayList<SelectQuery> listSelect=new ArrayList<SelectQuery>();
		/*Property propertyRDN=getRDN(idUserTask,null,null,ses);
		if(propertyRDN!=null){*/
		SelectQuery select=new SelectQuery(String.valueOf(idUserTask),/*propertyRDN.getIdProp()*/Constants.IdPROP_RDN,null,null);
		listSelect.add(select);
		/*}*/	
		/*ObjectProperty propertyTarget=getTarget(idUserTask,null);
		if(propertyTarget!=null){
			//int idto=getIdtoValue((ObjectProperty)propertyTarget);
			int ido=getIdoFilter(propertyTarget);
			int idto=getIdtoFilter(propertyTarget);
			Property propertyRDNTarget=getRDN(ido,null,null);
			SelectQuery select=new SelectQuery(ido,idto,propertyRDNTarget.getRol(),propertyRDNTarget.getIdProp(),propertyRDNTarget.getIdoRel());
			listSelect.add(select);
		}*/
		return listSelect;
	}

	public Element getQueryXML(/*int idObject,int idClass,String name,*/instance instance,ArrayList<SelectQuery> select,Integer userRol,Integer idtoUserTask,Integer limitResults) throws NotFoundException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		QueryXML query=m_kb.getQueryXML();
		query.setLimit(limitResults);
		/*return query.toQueryXML(idObject,idClass,name,m_user,select,m_kb);*/
		query.setSelect(select);
		Element element=null;

		element=query.toQueryXML(instance/*,userRol*/,idtoUserTask);

		return element;
	}

	/*	public int getLevelObject(int idObject){
		return m_kb.getLevel(idObject);
	}
	 */
	
	public instance getTreeObjectReport(int idObject,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		instance inst=null;
		int idto=-1;
		/*if(!Constants.isIDTemporal(idObject)){//Pedimos la property para que se cargue en motor y podamos saber su idto
			Property property=m_kb.getProperty(idObject, Constants.IdPROP_RDN, userRol, m_user, idtoUserTask, ses);
			idto=property.getIdto();
		}else */idto=getClass(idObject);
		
		inst=new instance(idto,idObject);
		//System.err.println("idto:"+idto+" idObject:"+idObject);
		ArrayList<Integer> usedRanges=new ArrayList<Integer>();
		Iterator<Property> itp=m_kb.getAllPropertyIterator(idObject, idto, userRol, m_user, idtoUserTask, ses);
		while(itp.hasNext()){
			Property p=itp.next();
			inst.addProperty(idObject, p);
			if(p instanceof ObjectProperty){
				Integer idRange=getIdRange((ObjectProperty)p);
				if(idRange==null)
					idRange=getIdtoEnum((ObjectProperty)p);
				else{
					if(usedRanges.contains(idRange))
						idRange=createRange(p.getIdo(), p.getIdto(),p.getIdProp(), idRange, userRol, idtoUserTask, 1, ses);
					usedRanges.add(idRange);
				}
				inst.addProperty(idRange, m_kb.getProperty(idRange, getClass(idRange), Constants.IdPROP_RDN, userRol, m_user, idtoUserTask, ses));
			}
		}
		
		return inst;
	}
	
	public instance getTreeObjectTable(int idObject,int idtoObject, String idTable,JTree columnsTree,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return getTreeObjectTable( idObject, idtoObject,  idTable, columnsTree, userRol, idtoUserTask, ses, true); 
	}
	
	public instance getTreeObjectTable(int idObject,int idtoObject, String idTable,JTree columnsTree,Integer userRol,Integer idtoUserTask,Session ses, boolean loadfromServer) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		instance inst=null;
		String rdn;
		
		IdObjectForm idObjectForm=new IdObjectForm(idTable);
		
		inst=new instance(idtoObject,idObject);

		
		DataProperty propertyRdn=(DataProperty)m_kb.getProperty(idObject, idtoObject, Constants.IdPROP_RDN, userRol, m_user, idtoUserTask, ses);
		rdn=(String)getValueData(propertyRdn);
		inst.setRdn(rdn, false);
				
		//Necesitamos pedir la property porque idObjectForm.getValueCls devuelve la clase y nosotros queremos el idoRange.
		//TODO Si valueCls fuera el idoRange directamete nos ahorrariamos esta consulta a motor
		int idRange=-1;
		int idtoRange=-1;
		int idtoTable=getClass(idObjectForm.getIdo());
		ObjectProperty propParent=getChild(idObjectForm.getIdo(), idtoTable, idObjectForm.getIdProp(), userRol, idtoUserTask, ses);
		if(propParent==null){//Si no lo hemos encontrado es porque se ha buscado en un instance que no lo tiene por lo que volvemos a buscar en el motor
			setInstance(null);
			try{
				propParent=getChild(idObjectForm.getIdo(), idtoTable, idObjectForm.getIdProp(), userRol, idtoUserTask, ses);
			}finally{
				clearInstance();
			}
		}
		idRange=getIdRange(propParent);
		idtoRange=idObjectForm.getValueCls();
		
		//Obtenemos el idParent, que es una copia del id de la tabla pero cambiando valueCls ya que le ponemos idoRange en vez de la clase
		IdObjectForm idObjectFormParent=new IdObjectForm();
		idObjectFormParent.setIdo(propParent.getIdo());
		idObjectFormParent.setIdProp(propParent.getIdProp());
		idObjectFormParent.setValueCls(idRange);
				
		String idParent=idObjectFormParent.getIdString();//Tiene el ido,idProp e idRange
		
		int ido=idObject;
		int idto=idtoObject;

		DefaultMutableTreeNode root=(DefaultMutableTreeNode)columnsTree.getModel().getRoot();
		Enumeration e=root.children();
		int c=0;
		while(e.hasMoreElements()){
			c++;
			DefaultMutableTreeNode node=(DefaultMutableTreeNode)e.nextElement();
			buildInstanceTable(inst, idParent, node, ido, idto, idRange, idtoRange, userRol, idtoUserTask, ses,loadfromServer);	
		}
		
		
		return inst;
	
	}
	
	
	private void buildInstanceTable(instance inst,String idParent,DefaultMutableTreeNode node,int ido,int idto,Integer idoFilter,Integer idtoFilter, Integer userRol, Integer idtoUserTask, Session sess, boolean loadfromServer) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		boolean isFilter=idoFilter!=null && ido==idoFilter;
		
		if(!loadfromServer && !m_kb.existInMotor(ido)) return;
		
		IdObjectForm userObject=(IdObjectForm)node.getUserObject();
		int idProp=userObject.getIdProp();
		if(hasProperty(idto, idProp)){
			if(node.isLeaf()){
				Property propIdo=getProperty(ido, idto, idProp, userRol, idtoUserTask, sess);
				inst.addProperty(propIdo.getIdo(), propIdo);
				
				String columnString=userObject.getIdString();
				if(userObject.getValueCls().equals(Constants.IDTO_IMAGE) && !propIdo.getValues().isEmpty())//Si es imagen mostramos la primera
					inst.addValueColumn(columnString, propIdo.getValues().getFirst());
				else inst.addValueColumn(columnString, propIdo.getUniqueValue());//Para el resto si tiene mas de un valor uniqueValue devuelve nulo porque no deberiamos tener una columna con cardinalidad>1
				inst.addIdoColumn(columnString, !isFilter?propIdo.getIdo():null);
				inst.addIdtoColumn(columnString, propIdo.getIdto());
				inst.addIdoFilterColumn(columnString, idoFilter);
				inst.addIdtoFilterColumn(columnString, idtoFilter);
				inst.addIdParentColumn(columnString, idParent);
			}else{				
				ObjectProperty propIdo=getChild(ido, idto, idProp, userRol, idtoUserTask, sess);
				Iterator<Value> itrValuesP=propIdo.getValues().iterator();
	   			Integer value=null;
	   			Integer valueCls=null;
	   			//do{
	   				if(!isFilter && itrValuesP.hasNext()){
	   					ObjectValue objectValue=(ObjectValue)itrValuesP.next();
	   					value=objectValue.getValue();
	   					valueCls=objectValue.getValueCls();
	   				}
   					
	   				if(value!=null || !propIdo.getRangoList().isEmpty()){
	   					Integer valueFilter=null;
	   					Integer valueClsFilter=null;
	   					if(!propIdo.getRangoList().isEmpty()){
	   						valueFilter=propIdo.getRangoList().getFirst();
	   						valueClsFilter=getClass(valueFilter);
	   					}
	   					
	   					if(value==null){
	   						value=valueFilter;
	   						valueCls=valueClsFilter;
	   					}
	   					
	   					IdObjectForm idObjectForm = new IdObjectForm();
			   			idObjectForm.setIdo(propIdo.getIdo());
			   			idObjectForm.setIdProp(propIdo.getIdProp());
			   			idObjectForm.setValueCls(valueCls);
			   			
			   			String idParentObj=idObjectForm.getIdString();
			   			int numChild=node.getChildCount();
						for(int i=0;i<numChild;i++){
							DefaultMutableTreeNode childNode=(DefaultMutableTreeNode)node.getChildAt(i);
							buildInstanceTable(inst, idParentObj, childNode, /*getIdRange(propRange),*/ value, valueCls, valueFilter, valueClsFilter, userRol, idtoUserTask, sess,loadfromServer);
						}
	   				}
	   			//}while(itrValuesP.hasNext());//TODO Necesitariamos iterar cuando una columna de la tabla admita mas de un valor. De momento nos quedamos con el primero
			}
			
		}
	}
		
	
	public instance getTreeObject(int idObject,Integer userRol,Integer idtoUserTask,Session ses,boolean filterMode) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		instance inst=null;
		
		inst=m_kb.getTreeObject(idObject,userRol,m_user,idtoUserTask,ses);
		inst.setFilterMode(filterMode);
		return inst;
	}

	public Object getValueData(DataProperty property){
		Object value=null;
		//System.out.println("propValueData:"+property);
		LinkedList<Value> valueList=property.getValues();
		if(!valueList.isEmpty()){
			if(valueList.size()==1){
				DataValue dataV=(DataValue)valueList.getFirst();
				value=getValueData(dataV);
			}else{
				value=new ArrayList();
				Iterator<Value> itr=valueList.iterator();
				while(itr.hasNext()){
					DataValue dataV=(DataValue)itr.next();
					((ArrayList)value).add(getValueData(dataV));
				}
			}
		}
		
		return value;
	}

	public Object getValueData(DataValue dataValue){
		Object value=null;
		if(dataValue instanceof StringValue)
			value= ((StringValue)dataValue).getValue();
		else if(dataValue instanceof DoubleValue){
			Double valueMin= ((DoubleValue)dataValue).getValueMin();
			Double valueMax= ((DoubleValue)dataValue).getValueMax();
			if(dataValue.isEqualToValue()){
				value= Auxiliar.equals(valueMin,valueMax)?valueMin:String.valueOf(valueMin)+":"+String.valueOf(valueMax);
			}else{
				value= String.valueOf(valueMin)+":"+String.valueOf(valueMax)+":"+Constants.OP_NEGATION;
			}
		}else if(dataValue instanceof IntValue){
			Integer valueMin= ((IntValue)dataValue).getValueMin();
			Integer valueMax= ((IntValue)dataValue).getValueMax();
			if(dataValue.isEqualToValue()){
				value= Auxiliar.equals(valueMin,valueMax)?valueMin:String.valueOf(valueMin)+":"+String.valueOf(valueMax);
			}else{
				value= String.valueOf(valueMin)+":"+String.valueOf(valueMax)+":"+Constants.OP_NEGATION;
			}
		}else if(dataValue instanceof BooleanValue){
			Boolean valueB= ((BooleanValue)dataValue).getBvalue();
			/*value= valueB==null?"":valueB.toString();*/
			String comment= ((BooleanValue)dataValue).getComment();
			value= valueB+":"+comment;
		}else if(dataValue instanceof TimeValue){
			Long relativeSecondsMin=((TimeValue)dataValue).getRelativeSecondsMin();
			long millisecondsMin=relativeSecondsMin*Constants.TIMEMILLIS;
			Long relativeSecondsMax=((TimeValue)dataValue).getRelativeSecondsMax();
			long millisecondsMax=relativeSecondsMax*Constants.TIMEMILLIS;
			value=millisecondsMin==millisecondsMax?millisecondsMin:String.valueOf(millisecondsMin)+":"+String.valueOf(millisecondsMax);
		}else if(dataValue instanceof UnitValue)
			value= ((UnitValue)dataValue).getValueMin();

		return value;
	}

//	public void setValueList(/*LinkedList valueList,*/ /*int id,*/ /*int prop,*/Property property, LinkedList valueList/*, session session*/) throws Exception{
//	// ValueList lo tengo que poner en el property
//	if(m_instance!=null)
//	m_instance.setValueList(/*valueList,*/ /*id,*/ property, valueList/*, session*/);
//	else m_kb.setValueList(/*id,*/ property, valueList/*, session*/);
//	}

	public void setValue(/*Property property,*/int ido, int idProp, Value value, Value valueOld,/*int operation*/Integer userRol,Integer idtoUserTask,Session ses)
	throws CardinalityExceedException,OperationNotPermitedException, IncompatibleValueException,NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException{
		if(m_instance!=null)
			m_instance.setValue(ido,idProp,valueOld,value);
		else{
			Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI, "setValue", "ido:"+ido+" idProp:"+idProp+" value:"+value+" oldValue:"+valueOld+" idtoUserTask:"+idtoUserTask);
			boolean success=false;
			try{
				m_kb.setValue(/*property,*/ido,getClass(ido),idProp,valueOld,value, /*operation*/userRol,m_user,idtoUserTask,ses);
				success=true;
			}finally{
				if(!success)
					Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI, "setValueException", "ido:"+ido+" idProp:"+idProp+" value:"+value+" oldValue:"+valueOld+" idtoUserTask:"+idtoUserTask);
			}
		}
	}

	public void setValue(int ido, int idProp, LinkedList<Value> values, LinkedList<Value> valuesOld, Integer userRol, Integer idtoUserTask, Session ses)
	throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException{
		if(m_instance!=null)
			m_instance.setValue(ido,idProp,valuesOld,values);
		else{
			Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI, "setValue", "ido:"+ido+" idProp:"+idProp+" value:"+values+" oldValue:"+valuesOld+" idtoUserTask:"+idtoUserTask);
			boolean success=false;
			try{
				m_kb.setValue(/*property,*/ido,getClass(ido),idProp,valuesOld,values, /*operation*/userRol,m_user,idtoUserTask,ses);
				success=true;
			}finally{
				if(!success)
					Singleton.getInstance().getDebugLog().addDebugData(DebugLog.DEBUG_GUI, "setValueException", "ido:"+ido+" idProp:"+idProp+" value:"+values+" oldValue:"+valuesOld+" idtoUserTask:"+idtoUserTask);
			}
		}
	}

	public Value buildValue(Object value,int valueCls){
		Value valueObject=null;
		if(value!=null){
			valueObject=buildDataValue(value,valueCls);
			if(valueObject==null)//Se trata de un ObjectValue	
				valueObject=buildObjectValue((Integer)value,valueCls);
		}
		return valueObject;
	}

//	public void updateUserTasks(selectData data){
//	Iterator iSel=data.getIterator();
//	while( iSel.hasNext() ){
//	instance ins=(instance)iSel.next();
//	try {
//	m_kb.newEvent(ins, null, m_user, null);//¿?Este metodo quizas no necesita userTask ni userRol
//	} catch (NotFoundException e) {
//	e.printStackTrace();
//	}
//	}
//	}

	public LinkedList setValueToList(Property property,String value,int valueCls){
		LinkedList<Object> valueList=new LinkedList<Object>();
		if(value!=null){
			if(property instanceof ObjectProperty){
				ObjectValue objectV=new ObjectValue();
				objectV.setValue(new Integer(value));
				objectV.setValueCls(valueCls);

				valueList.add(objectV);
			}else{
				DataValue dataV=buildDataValue(value,valueCls);
				valueList.add(dataV);
			}
		}
		return valueList;
	}

	public LinkedList addValueToList(Property property,String value,int valueCls){
		LinkedList<Value> valueList=property.getValues();
		if(property instanceof ObjectProperty){
			if(value!=null){
				ObjectValue objectV=new ObjectValue();
				objectV.setValue(new Integer(value));
				objectV.setValueCls(valueCls);		    
				valueList.add(objectV);
			}
		}else{
			if(value!=null){
				DataValue dataV=buildDataValue(value,valueCls);
				valueList.add(dataV);
			}
		}
		return valueList;
	}

	public LinkedList removeValueToList(Property property,Integer value,int valueCls){
		LinkedList<Value> valueList=new LinkedList<Value>();
		if(value!=null){
			LinkedList<Value> valueListOld=property.getValues();
			Iterator<Value> itrValue=valueListOld.iterator();
			if(property instanceof ObjectProperty){
				while(itrValue.hasNext()){
					ObjectValue objectV=(ObjectValue)itrValue.next();
					if(objectV.getValue()!=value.intValue())
						valueList.add(objectV);
				}
			}else{
				while(itrValue.hasNext()){
					DataValue dataV=(DataValue)itrValue.next();
					Object valueDataV=getValueData(dataV);
					if(valueDataV.equals(value)){
						valueList.add(dataV);
					}
				}
			}
		}
		return valueList;
	}

	private DataValue buildDataValue(Object value,int valueCls){
		DataValue dataV=null;
		String[] buf=null;
		switch(valueCls){
		case Constants.IDTO_FILE:
		case Constants.IDTO_IMAGE:
		case Constants.IDTO_MEMO:
		case Constants.IDTO_STRING:
			dataV=new StringValue();
			((StringValue)dataV).setValue((String)value);
			break;
		case Constants.IDTO_INT:
			dataV=new IntValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor int:"+value);
			((IntValue)dataV).setValueMin(buf[0].equals("null")?null:new Integer(buf[0]));
			if(buf.length>1)
				((IntValue)dataV).setValueMax(buf[1].equals("null")?null:new Integer(buf[1]));
			else ((IntValue)dataV).setValueMax(buf[0].equals("null")?null:new Integer(buf[0]));
			break;
		case Constants.IDTO_DOUBLE:
			dataV=new DoubleValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor double:"+value);
			((DoubleValue)dataV).setValueMin(buf[0].equals("null")?null:new Double(buf[0]));
			if(buf.length>1)
				((DoubleValue)dataV).setValueMax(buf[1].equals("null")?null:new Double(buf[1]));
			else ((DoubleValue)dataV).setValueMax(buf[0].equals("null")?null:new Double(buf[0]));
			break;
		case Constants.IDTO_BOOLEAN:
			dataV=new BooleanValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor booleano:"+value);
			((BooleanValue)dataV).setBvalue(buf[0].equals("null")?null:new Boolean(buf[0]));
			if(buf.length>1)
				((BooleanValue)dataV).setComment(buf[1].equals("null")?null:buf[1]);
			break;
		case Constants.IDTO_DATE:
			dataV=new TimeValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor date:"+value);
			((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0])/Constants.TIMEMILLIS);
			if(buf.length>1)
				((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1])/Constants.TIMEMILLIS);
			else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0])/Constants.TIMEMILLIS);
			break;
		case Constants.IDTO_DATETIME:
			dataV=new TimeValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor datetime:"+value);
			((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0])/Constants.TIMEMILLIS);
			if(buf.length>1)
				((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1])/Constants.TIMEMILLIS);
			else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0])/Constants.TIMEMILLIS);
			break;
		case Constants.IDTO_TIME:
			dataV=new TimeValue();
			buf=value.toString().split(":");
			//System.out.println("Asigna valor datetime:"+value);
			((TimeValue)dataV).setRelativeSecondsMin(new Long(buf[0])/Constants.TIMEMILLIS);
			if(buf.length>1)
				((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[1])/Constants.TIMEMILLIS);
			else ((TimeValue)dataV).setRelativeSecondsMax(new Long(buf[0])/Constants.TIMEMILLIS);
			break;
		}
		
		if(buf!=null && buf.length>2){
			dataV.setEqualToValue(!buf[2].equals(Constants.OP_NEGATION));
		}
		
		return dataV;
	}

	private ObjectValue buildObjectValue(int value,int valueCls){
		ObjectValue objectV=new ObjectValue();
		objectV.setValue(value);
		objectV.setValueCls(valueCls);

		return objectV;
	}

	public void setInstance(instance instance){
		m_instanceStack.push(instance);
		m_instance=instance;
	}

	public void clearInstance(){
		m_instanceStack.pop();
		m_instance=m_instanceStack.isEmpty()?null:m_instanceStack.peek();
	}

	public Integer createPrototype(int idClass,int level/*,session session*/, Integer userRol, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return m_kb.createPrototype(idClass,level,/*session,*/ userRol, m_user, idtoUserTask, ses);
	}
	
	public Integer createFilter(int idClass,Integer userRol, Integer idtoUserTask, int depth, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		return m_kb.createFilter(idClass, userRol, m_user, idtoUserTask, depth, ses);
	}

	/* 
	 public access getOperationsIndividual(int ido,int idto,Integer userRol){
		Integer operations=0;
		LinkedList valueList=new LinkedList();
		valueList.add(ido);
		session session=new session();

		ArrayList<Integer> userTasks=getUserTasks(idto, userRol);
		Iterator<Integer> itr=userTasks.iterator();
		while(itr.hasNext()){
			Integer idUserTask=itr.next();
			try{
				setValueList(valueList, idUserTask, Constants.IdPROP_TARGETCLASS);
				Integer operation=getOperationUserTask(idUserTask, userRol);
				if(operation!=null){
					operations+=operation;
				}
			}catch(Exception ex){
				System.out.println("UserTask "+idUserTask+" no permitida para ese individuo");
			}
		}
		access access=new access(operations);
		return access;
	}*/

	public access getAccessIndividual(int ido,Integer userRol, Integer idtoUserTask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException{
		return m_kb.getAccessOverObject(ido, userRol, m_user, idtoUserTask);
	}


	public LinkedHashMap<Integer,ArrayList<UserAccess>> getAllAccessIndividual(int ido,Integer userRol,access accessProperty,Integer idtoUserTask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{

		//******************* Mirando otras userTask que apunten***************//

		/*return m_kb.getAllAccessOverObject(ido, userRol, m_user);*/
//		HashMap<Integer, ArrayList<UserAccess>> userAccess;
//		try {
//		userAccess = m_kb.getUsertaskOperationOver(ido/*, userRol*/, m_user);
//		} catch (NotFoundException e) {
//		e.printStackTrace();
//		m_comm.logError(e,e.getSystemMessage());
//		return null;
//		} catch (IncoherenceInMotorException e) {
//		e.printStackTrace();
//		m_comm.logError(e,e.getSystemMessage());
//		return null;
//		}
//		System.err.println("a");
//		if(!userAccess.isEmpty()){
//		if(userAccess.get(0)==null)
//		System.out.println("Permisos: ido"+ido+" access:null");
//		else System.out.println("Permisos: ido"+ido+" access:"+userAccess.get(0).get(0).getAccess()+" userRol"+userRol);
//		}else System.out.println("Permisos: ido"+ido+" access:vacio");
//		if(idtoUserTask!=null && !userAccess.containsKey(idtoUserTask)){
//		/*if(accessProperty.getSetAccess()){
//		//Si tiene permiso set lo cambio por rel y unrel ya que es a lo que se corresponde en botoneraAccion
//		accessProperty=new access(accessProperty.getViewAccess(),accessProperty.getNewAccess(),false,true,true,accessProperty.getDelAccess(),accessProperty.getConcreteAccess(),accessProperty.getSpecializeAccess(),accessProperty.getAbstractAccess(),accessProperty.getCommentAccess());
//		}*/
//		UserAccess userAcc=new UserAccess(userRol!=null?userRol:0,accessProperty);
//		ArrayList<UserAccess> arrayUserAccess=new ArrayList<UserAccess>();
//		arrayUserAccess.add(userAcc);
//		userAccess.put(idtoUserTask, arrayUserAccess);
//		}
//		return userAccess;

		//***************************************************************************//

		//******************* Sin mirar otras userTask que apunten***************//
		LinkedHashMap<Integer, ArrayList<UserAccess>> userAccess=new LinkedHashMap<Integer, ArrayList<UserAccess>>();
		if(idtoUserTask!=null){
			if(!userAccess.containsKey(idtoUserTask)){
			
				ArrayList<UserAccess> list=new ArrayList<UserAccess>();
				access accUserTask=getAccessIndividual(ido, userRol, idtoUserTask);
				
	//			ArrayList<Integer> listIdtoUserTasks=getIdtoUserTasks(getClass(ido),null,false);
	//			if(!listIdtoUserTasks.contains(idtoUserTask)){//Si contuviera la userTask no hace falta entrar ya que nos quedamos con los permisos calculados
	//				Iterator<Integer> itr=listIdtoUserTasks.iterator();
	//				while(itr.hasNext()){
	//					int idtoOtherUTask=itr.next();
	//					if(/*idtoUserTask==null || */idtoUserTask!=idtoOtherUTask){
	//						access accOtherUTask=getAccessIndividual(ido, userRol, idtoOtherUTask);
	//						//De momento solo nos interesa el permiso de new de otras userTask, pero para ello debemos tener new en la userTask en la que estamos
	//						//Usando el new de otra userTask cuyo target sea el idto nuestro nos aseguramos que al crearlo va directo a base de datos sin pasar por la sesion principal
	//						if(accUserTask.getNewAccess() && accOtherUTask.getNewAccess()){
	//							accOtherUTask=new access(access.NEW);
	//							ArrayList<UserAccess> listOther=new ArrayList<UserAccess>();
	//							listOther.add(new UserAccess(userRol!=null?userRol:0,accOtherUTask));
	//							userAccess.put(idtoOtherUTask, listOther);
	//						}
	//					}
	//				}
	//			}
				
				list.add(new UserAccess(userRol!=null?userRol:0,accUserTask));
				userAccess.put(idtoUserTask, list);
				//System.err.println("OperationOver:"+m_kb.getUsertaskOperationOver(ido/*, userRol*/, m_user));
				/*if(accessProperty.getSetAccess())
					//Si tiene permiso set lo cambio por rel y unrel ya que es a lo que se corresponde en botoneraAccion
					accessProperty=new access(accessProperty.getViewAccess(),accessProperty.getNewAccess(),false,true,true,accessProperty.getDelAccess(),accessProperty.getConcreteAccess(),accessProperty.getSpecializeAccess(),accessProperty.getAbstractAccess(),accessProperty.getCommentAccess());
				}*/
	//			UserAccess userAcc=new UserAccess(userRol!=null?userRol:0,accessProperty);
	//			//ArrayList<UserAccess> arrayUserAccess=new ArrayList<UserAccess>();
	//			//arrayUserAccess.add(userAcc);
	//			//userAccess.put(idtoUserTask, arrayUserAccess);
	//			list.add(userAcc);
				//System.err.println("UserAccessss:"+userAccess);
			}
		}else{
			//IdtoUserTask sera null cuando se llame para un Data_Transfer
			ArrayList<UserAccess> list=new ArrayList<UserAccess>();
			access accUserTask=getAccessIndividual(ido, userRol, idtoUserTask);
			
			list.add(new UserAccess(userRol!=null?userRol:0,accUserTask));
			userAccess.put(idtoUserTask, list);
		}
		return userAccess;
		//**********************************************************************//
	}

	private Iterator<Integer> getIdObjectPropertyIterator(Integer ido, int idto, int idProp, Integer userRol, String user, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException {
		Iterator<Integer> itrList=null;	
		Property property;
		if(m_instance!=null)
			property= m_instance.getProperty(ido!=null?ido:idto, idProp);
		else property = m_kb.getProperty(ido!=null && !ido.equals(idto)?ido:null, idto, idProp, userRol, user, idtoUserTask, ses);

		if(property instanceof ObjectProperty)
			itrList=getIdoValues((ObjectProperty)property).iterator();
		else itrList=new ArrayList<Integer>().iterator();
		
		return itrList;
	}

	/*private Iterator<Integer> getIdObjectPropertyIterator(int id) {
		ArrayList<Integer> list=new ArrayList<Integer>();   
		Iterator itr=m_kb.getAllPropertyIterator(id);
		while(itr.hasNext()){
			Property prop=(Property)itr.next();
			String tipoProp=prop.getTypeProperty();// DataProperty,ObjectProperty
			if(tipoProp.equals("ObjectProperty")){
				LinkedList<ObjectValue> valueList = ((ObjectProperty)prop).getValueList();
				if(valueList.size()!=0){
					int size=valueList.size();
					for(int i=0;i<size;i++){
						ObjectValue value = valueList.getFirst();
						list.add((Integer)value.getValue());
					}
				}
			}
		}
		return list.iterator();
	}*/

	private ObjectProperty getObjectProperty(Integer ido,int idto,int idProp,Integer userRol, String user, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, ParseException, JDOMException, OperationNotPermitedException{   
		Property property;
		if(m_instance!=null)
			property=m_instance.getProperty(ido!=null?ido:idto, idProp);
		else property=getProperty(ido, idto, idProp, userRol, idtoUserTask, ses);

		return (ObjectProperty)property;
	}

	private Iterator<ObjectProperty> getObjectPropertyIterator(Integer ido, int idto, Integer userRol, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<ObjectProperty> list=new ArrayList<ObjectProperty>();   
		Iterator<Property> itr=getProperties(ido, idto, userRol, idtoUserTask, ses);

		while(itr.hasNext()){
			Property prop=itr.next();
			if(prop instanceof ObjectProperty){
				list.add((ObjectProperty)prop);
			}
		}
		return list.iterator();
	}

	private Iterator<DataProperty> getDataPropertyIterator(Integer ido,int idto, int idProp, Integer userRol, String user, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<DataProperty> list=new ArrayList<DataProperty>();   
		Property property;
		if(m_instance!=null)
			property=m_instance.getProperty(ido!=null?ido:idto, idProp);
		else property=m_kb.getProperty(ido!=null && !ido.equals(idto)?ido:null, idto, idProp, userRol, user, idtoUserTask, ses);

		if(property instanceof DataProperty){
			list.add((DataProperty)property);
		}
		return list.iterator();
	}

	private Iterator<DataProperty> getDataPropertyIterator(Integer ido,int idto, Integer userRol, Integer idtoUserTask, Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		ArrayList<DataProperty> list=new ArrayList<DataProperty>();   
		Iterator<Property> itr=getProperties(ido, idto, userRol, idtoUserTask, ses);

		while(itr.hasNext()){
			Property prop=itr.next();
			if(prop instanceof DataProperty){
				list.add((DataProperty)prop);
			}
		}
		return list.iterator();
	}


	private access getAccessProperty(Integer ido,int idto,int idProp,Integer userRol,String user,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		Property property;
		if(m_instance!=null)
			property=m_instance.getProperty(ido!=null?ido:idto, idProp);
		else property=m_kb.getProperty(ido!=null && !ido.equals(idto)?ido:null, idto, idProp, userRol, user, idtoUserTask, ses);

		return property.getTypeAccess();
	}

	// Este metodo esta en formFactory. Hay que decidir si estaria mejor ubicado aqui o alli
	//public ArrayList<ArrayList<Property>> getPropertiesInTabs(int idObject,ArrayList<Integer> userRols,String user,int idUserTask)

	// Comprueba si alguna de las properties del id se pueden editar
	public boolean isFull(Integer ido,int idto,Integer userRol,Integer idtoUserTask,Session ses) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
		boolean isFull=true;
		Iterator<Property> itr=getProperties(ido, idto, userRol, idtoUserTask, ses);
		if(!itr.hasNext())
			System.err.println("WARNING: El objeto con ido:"+ido+ "no tiene properties");
		while(isFull && itr.hasNext()){
			Property prop=itr.next();
			/*if(prop instanceof DataProperty)
				isFull=((DataProperty)prop).getEnumList().size()==1;
			else isFull=((ObjectProperty)prop).getEnumList().size()==1;*/
			if(prop.getTypeAccess().getViewAccess())
				isFull=!prop.getTypeAccess().getSetAccess();
		}

		return isFull;
	}
	
	/**
	 * Crea un GTableRow a partir de un instance que venga de base de datos, por lo que se espera que el valor de cada columna la tenga en instance.getValueSQ(column).
	 * Solo llamar para instances de consultas a base de datos
	 * @param inst
	 * @param mapIdColumnSelect
	 * @param idtoUserTask
	 * @return
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws OperationNotPermitedException
	 * @throws DataErrorException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 * @throws ParseException
	 */
	public GTableRow buildTableRow(instance inst,LinkedHashMap<String, SelectQuery> mapIdColumnSelect,Integer idtoUserTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{			
			
			GTableRow tableRow=new GTableRow(new GIdRow(inst.getIDO(),inst.getIdTo(),inst.getRdn()));

				Iterator<String> itrColumn=mapIdColumnSelect.keySet().iterator();
				//System.err.println("**************** Antes bucle**********"+System.currentTimeMillis());
				while(itrColumn.hasNext()){
					String column=itrColumn.next();
					SelectQuery select=mapIdColumnSelect.get(column);
					if(select!=null){//Si no tiene select puede ser una columna de type o add, las que no necesitan nada de base de datos
						String selectString=select.toString();
						
						Value valueObject=inst.getValueSQ(selectString);
						Object value=null;
						if(valueObject!=null){
							if(valueObject instanceof DataValue){
								value=getValueData((DataValue)valueObject);
								if(new IdObjectForm(column).getValueCls().equals(Constants.IDTO_IMAGE))
									value= Constants.smallImage + value;
							}else value=((ObjectValue)valueObject).getValue();
						}

						//int ido=Integer.valueOf(select.getIdObject());
						//int idto=getClass(ido);
						
						tableRow.setDataColumn(column, /*Utils.normalizeLabel(*/value/*)*//*!=null?value:""*/);

					}else if(column.equalsIgnoreCase("type")){
						String value=getLabelClass(inst.getIdTo(),idtoUserTask);
						tableRow.setDataColumn(column, value/*!=null?value:""*/);
					}
				}
				
				//System.err.println("**************** Despues bucle**********"+System.currentTimeMillis());

				return tableRow;
	}
	
	/**
	 * Crea un GTableRow a partir de un instance creado por getTreeObjectTable, por lo que se espera que el valor de cada columna la tenga en instance.getValueColumn(column).
	 * Solo llamar para instances de construidos por getTreeObjectTable
	 * @param inst
	 * @param listColumn
	 * @param idtoUserTask
	 * @return
	 * @throws SystemException
	 * @throws RemoteSystemException
	 * @throws CommunicationException
	 * @throws InstanceLockedException
	 * @throws ApplicationException
	 * @throws IncompatibleValueException
	 * @throws CardinalityExceedException
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws OperationNotPermitedException
	 * @throws DataErrorException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws JDOMException
	 * @throws ParseException
	 */
	public GTableRow buildTableRow(instance inst,ArrayList<String> listColumn,Integer idtoUserTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{			
		
		GTableRow tableRow=new GTableRow(new GIdRow(inst.getIDO(),inst.getIdTo(),inst.getRdn()));

			Iterator<String> itrColumn=listColumn.iterator();
			//System.err.println("**************** Antes bucle**********"+System.currentTimeMillis());
			while(itrColumn.hasNext()){
				String column=itrColumn.next();
				if(IdObjectForm.matchFormat(column)){//Si no casa puede ser una columna de type o add
					
					Value valueObject=inst.getValueColumn(column);
					Object value=null;
					if(valueObject!=null){
						if(valueObject instanceof DataValue){
							value=getValueData((DataValue)valueObject);
							if(new IdObjectForm(column).getValueCls().equals(Constants.IDTO_IMAGE) && !(new File((String)value).canRead()/*Para saber si es una ruta local o remota*/)){
								value= Constants.smallImage + value;//Si es remota le añadimos el small para coger la miniatura
							}
						}else value=((ObjectValue)valueObject).getValue();
					}
					//int ido=Integer.valueOf(select.getIdObject());
					//int idto=getClass(ido);
					
					tableRow.setDataColumn(column, /*Utils.normalizeLabel(*/value/*)*//*!=null?value:""*/);

					tableRow.setIdoMap(column, inst.getIdoColumn(column));
	       			tableRow.setIdtoMap(column, inst.getIdtoColumn(column));
	       			tableRow.setIdoFilterMap(column, inst.getIdoFilterColumn(column));
	       			tableRow.setIdtoFilterMap(column, inst.getIdtoFilterColumn(column));
	       			tableRow.setIdParentMap(column, inst.getIdParentColumn(column));
				}else if(column.equalsIgnoreCase("type")){
					String value=getLabelClass(inst.getIdTo(),idtoUserTask);
					tableRow.setDataColumn(column, value/*!=null?value:""*/);
				}
			}
			
			//System.err.println("**************** Despues bucle**********"+System.currentTimeMillis());

			return tableRow;
	}
	
	public Integer getIdClass(String name) throws NotFoundException, IncoherenceInMotorException {
		return m_kb.getIdClass(name);
	}
	
	public boolean hasProperty(int idto,int idProp)/* throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException*/{
		boolean hasProperty=m_kb.hasProperty(idto, idProp);
//		if(!hasProperty){
//			if(isAbstractClass(idto)){
//				Iterator<Integer> itrSpecialized=getSpecialized(idto).iterator();
//				while(!hasProperty && itrSpecialized.hasNext()){
//					int idtoSpecialized=itrSpecialized.next();
//					hasProperty=m_kb.hasProperty(idtoSpecialized, idProp);
//				}
//				System.err.println("ENTRAAAA en hasProperty para idto:"+idto+" idProp:"+idProp+" hasProperty:"+hasProperty);
//			}
//		}
		return hasProperty;
	}


	public void addHistoryDDBBListener(IHistoryDDBBListener listener){
		DDBBSession.addHistoryDDBBListener(listener);
	}
	
	public void removeHistoryDDBBListener(IHistoryDDBBListener listener){
		DDBBSession.removeHistoryDDBBListener(listener);
	}

	public void buildOrderForm(int ido,ArrayList<Property> list,Integer idtoUserTask) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		orderProperties.buildOrderForm(ido,list,idtoUserTask,this);
		groupsComponents.buildOrderForm(list, idtoUserTask, this);
	}

	public int getOrderGroup(int idGroup){
		return groupsComponents.getOrderGroup(idGroup);//orderProperties.getOrderGroup(idGroup);
	}

	//TODO idGroup no se usa porque en teoria una property pertenecera a un solo grupo,de momento se deja
	public int getOrder(int idProp,Integer idGroup){
		return orderProperties.getOrder(idProp,idGroup);
	}

	public int getPriority(Property property,int idGroup){
		return orderProperties.getPriority(property,idGroup);
	}

	public Category getCategoryProperty(int idProp) throws NotFoundException{
		return m_kb.getCategory(idProp);
	}

	public int getSizeMotor(){
		if(m_kb instanceof DocDataModel)
			return ((DocDataModel)m_kb).getRuleEngine().getMotorSize();
		else return 0;
	}
	
	public int getLevelObject(int id){
		return m_kb.getLevelOf(id);
	}
	
	public boolean checkCoherenceObject(int idObject,Integer userRol,String user,Integer idtoUserTask,Session sess) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return m_kb.checkCoherenceObject(idObject,userRol,user,idtoUserTask,sess);
	}
	
	public ICardMed getCardMedComponents() {
		return carMedias;
	}

	public void setCardMedComponents(ICardMed carM) {
		this.carMedias= carM;
	}
	
	public void setLockObject(int ido,boolean locked,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		m_kb.setLock(ido, locked, m_user, session);
	}
	
	public void setLockObject(ArrayList<Integer> idos,boolean locked,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		m_kb.setLock(idos, locked, m_user, session);
	}
	
	public void loadIndividual(int ido, int idto, Integer userRol, Integer idtoUserTask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		loadIndividual(ido, idto, 3, userRol, idtoUserTask, sessionPadre);
	}

	public void loadIndividual(int ido, int idto, int depth, Integer userRol, Integer idtoUserTask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		if(!Constants.isIDTemporal(ido))
			m_kb.loadIndividual(ido, idto, depth, false, sessionPadre.isRunRules(), userRol, m_user, idtoUserTask, sessionPadre);
	}
	
	public void loadIndividual(HashMap<Integer,HashSet<Integer>> idos, Integer userRol, Integer idtoUserTask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		loadIndividual(idos, 3, false, sessionPadre.isRunRules(), userRol, idtoUserTask, sessionPadre);
	}

	public void loadIndividual(HashMap<Integer,HashSet<Integer>> idos, int depth, boolean lock, boolean lastStructLevel, Integer userRol, Integer idtoUserTask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		m_kb.loadIndividual(idos, depth, lock, lastStructLevel, userRol, m_user, idtoUserTask, sessionPadre);
	}
	
	public boolean isEssentialProperty(Integer idtoUserTask, Integer idto, int idProp) {
		return highlightedComponents.isEssentialProperty(idtoUserTask, idto, idProp);
	}

	public boolean isListenerMenu(Integer idtoUserTask) {
		return listenerMenu.isListener(idtoUserTask);
	}
	
	public Iterator<Integer> getListenerUtasks(){
		return listenerMenu.getListenerUtasks();
	}
	
	public ListenerUtask getListenerUtask(int idtoUserTask){
		return listenerMenu.getListenerUtask(idtoUserTask);
	}
	
	public boolean isPointed(int ido, int idto) throws NotFoundException, IncoherenceInMotorException {
		return m_kb.isPointed(ido, idto);
	}
	
	public boolean isLoad(int ido){
		return m_kb.existInMotor(ido);
	}
	
	public Integer createRange(int ido,int idto,int idProp,int valueCls, Integer userRol, Integer idtoUserTask, int depth, Session session) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException, CardinalityExceedException, IncompatibleValueException, ParseException, SQLException, NamingException, JDOMException{
		return m_kb.setRange(ido, idto,idProp, valueCls, userRol, m_user, idtoUserTask, depth, session);
	}
	
	public Iterator<Integer> getIndividuals(int idto,int level,boolean hierarchy) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		ArrayList<Integer> individuals=new ArrayList<Integer>();
		individuals.addAll(m_kb.getIndividualsOfLevel(idto, level));
		if(hierarchy){
			Iterator<Integer> itrSpec=getSpecialized(idto).iterator();
			while(itrSpec.hasNext()){
				individuals.addAll(m_kb.getIndividualsOfLevel(itrSpec.next(), level));
			}
		}
		
		return individuals.iterator();
	}
	
	public boolean isCompatibleWithFilter(int ido,instance instFilter,Integer userRol, Integer userTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		return m_kb.isCompatibleWithFilter(ido, instFilter, userRol, m_user, userTask);
	}
	
	public String getPropertyNameWithoutAlias(int idProp) throws NotFoundException{
		return m_kb.getPropertyName(idProp);
	}
	
	public String getPropertyMask(int prop, Integer idto, Integer utask) throws NotFoundException{
		return m_kb.getPropertyMask(prop, idto, utask);
	}
	
	public Integer getPropertyLength(int prop, Integer idto, Integer utask){
		return m_kb.getPropertyLength(prop, idto, utask);
	}
	
	public ArrayList<Integer> getIdosFromServer(int idto,Integer idtoUserTask) throws DataErrorException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		selectData selectD=Auxiliar.getIdosFromServer(idto, new ArrayList<String>(), idtoUserTask, m_kb);
		ArrayList<Integer> listIdos=new ArrayList<Integer>();
		Iterator<instance> itr=selectD.getIterator();
		while(itr.hasNext()){
			int ido=itr.next().getIDO();
			listIdos.add(ido);
		}
		
		return listIdos;
		
	}
	
	public HashSet<Integer> getClassifiedIdtos(int idto,int idProp, Value value, Integer userRol, Integer idtoUserTask, Session ses) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		return m_kb.getClassifiedIdtos(idto, idProp, value, m_user, userRol, idtoUserTask, ses);
	}
	
	
	public void print(int ido, int oldido,int idto,int idoUserTaskReport,int idtoUserTaskReport,Integer userRol,Integer idtoUserTask) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, ParseException, JDOMException, NotFoundException{
		HashMap<Integer,Integer> map=new HashMap<Integer, Integer>();
		map.put(ido, idto);
		HashSet<Integer> newIdos=new HashSet<Integer>();
		if(oldido<0){
			newIdos.add(ido);
			System.out.print("PRINT NEW OBJECT: "+oldido+" new "+ido);
		}
		print(map, newIdos,idoUserTaskReport, idtoUserTaskReport, userRol, idtoUserTask);
	}
	
	public void print(HashMap<Integer,Integer> mapIdosIdtos,HashSet<Integer> newIdos,int idoUserTaskReport,int idtoUserTaskReport,Integer userRol,Integer idtoUserTask) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, ParseException, JDOMException, NotFoundException{
		//Creo una sesion sin runRules
		
		
		//idoUserTaskReport apunta al individuo report que define los parametros por defecto (si tiene impresion directa, numero copias, y los parametros de usuario, su tipo y valor por defecto, 
		//en particular la clase target, y otros como fechas, delegacion, etc
		
		//Para ejecutar el report creo un prototipo de la misma clase de parametros pero ya asignado los valores del individuo a mostrar (ejmplo una factura concreta)
		
		//las clases de parametros de report se crean al vuelo como especializado de parametro
		DefaultSession session=createDefaultSession(getDefaultSession(),idtoUserTaskReport,false,true,false,true,false);
		session.setRulesGroup(Constants.REPORTS_RULES);
		try{
			ObjectProperty propertyParams=null;
			try{
				propertyParams=getChild(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_PARAMS, userRol, idtoUserTask, session);
			} catch (NotFoundException e) {
				System.err.println("Report sin parametros. No se imprime.");
			}
			if(propertyParams!=null){
				int idoParams=createPrototype(getIdRange(propertyParams), /*Constants.LEVEL_FILTER*/Constants.LEVEL_PROTOTYPE, userRol, idtoUserTask, session);					
				int idtoParams=getClass(idoParams);
				Iterator<ObjectProperty> itr=getChildren(idoParams, idtoParams, userRol, idtoUserTaskReport, session);
				Property prop=null;
				// Nos quedamos con el primer idto ya que todos van a tener un padre comun, por lo que cualquiera me valdria para encontrar la property de params
				int idtoOfFirst=mapIdosIdtos.values().iterator().next(); 
				while(itr.hasNext() && prop==null){
					ObjectProperty oProperty=itr.next();
					int valueCls=getClass(getIdRange(oProperty));
					if(valueCls==idtoOfFirst || isSpecialized(idtoOfFirst, valueCls)){
						prop=oProperty;
					}
				}
				if(prop!=null){
					Integer cardMax=prop.getCardMax();
					if(cardMax==null || cardMax.intValue()>=mapIdosIdtos.size()){	
						Iterator<Integer> itrIdos=mapIdosIdtos.keySet().iterator();
						while(itrIdos.hasNext()){
							int ido=itrIdos.next();
							int idto=mapIdosIdtos.get(ido);
							setValue(prop.getIdo(), prop.getIdProp(), buildValue(ido, idto), null, userRol, idtoUserTaskReport, session);
						}
						subPrint(idoUserTaskReport, newIdos.size()>0,idtoUserTaskReport, prop, userRol, idtoUserTask, session);
					}else{
						//Si la cardinalidad maxima es menor que los idos a imprimir vamos imprimiendo uno a uno
						Iterator<Integer> itrIdos=mapIdosIdtos.keySet().iterator();
						while(itrIdos.hasNext()){
							int ido=itrIdos.next();
							int idto=mapIdosIdtos.get(ido);
							setValue(prop.getIdo(), prop.getIdProp(), buildValue(ido, idto), null, userRol, idtoUserTaskReport, session);
							subPrint(idoUserTaskReport, newIdos.size()>0,idtoUserTaskReport, prop, userRol, idtoUserTask, session);
							//Lo quitamos
							setValue(prop.getIdo(), prop.getIdProp(), null, buildValue(ido, idto), userRol, idtoUserTaskReport, session);
						}
					}
				}
			}
		}finally{
			session.rollBack();
		}
	}
	
	public communicator getLocalComm(){
		 docServer localServer=Singleton.getInstance().getComm();
		 if(localServer!=null && localServer instanceof communicator) return (communicator)localServer;
		 else return comm;		 		 
	}
	
	private void subPrint(int idoUserTaskReport,boolean operationWithNewIdos,Integer idtoUserTaskReport,Property prop,Integer userRol,Integer idtoUserTask,Session session) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException{
		String num_copiasStr=String.valueOf(getValueData(getField(idoUserTaskReport, idtoUserTaskReport, getIdProp(Constants.PROP_REPORT_NCOPIES), userRol, idtoUserTask, session)));
		//String impresora=String.valueOf(getValueData(getField(idoUserTaskReport, idtoUserTaskReport, getIdProp("impresora"), userRol, idtoUserTask, session)));
		
		int num_pasos_copia=1;
		Property propCopia=null;
		instance inst= getTreeObjectReport(prop.getIdo(), userRol, idtoUserTaskReport, session);
		if(num_copiasStr!=null){			
			Integer idPropCopia=getIdProp("copia@rp@ticket venta");
			if(idPropCopia!=null){
				propCopia=inst.getProperty(prop.getIdo(), getIdProp("copia@rp@ticket venta"));
				num_pasos_copia=Integer.parseInt(num_copiasStr);
			}			
		}
		boolean automatizar_copias=propCopia!=null;
		for(int nc=1;nc<=num_pasos_copia;nc++){											
			if(propCopia!=null) inst.setValue(prop.getIdo(), propCopia.getIdProp(), propCopia.getUniqueValue(), new IntValue(nc));
			
			ObjectProperty property=getChild(idoUserTaskReport, idtoUserTaskReport, Constants.IdPROP_REPORT_FORMAT, userRol, idtoUserTask, session);
			Integer idoFormat=null;
			if(property.getValues().size()==1)
				idoFormat=getIdoValue(property);
			//TODO set ip backupurl de comm y restablecer al final, segun nombre de report que idealmente esta parametrizado, asi como IP backup
			//preguntar a Fran como pasar parametros
			//get report procesa pre print sequence, y showreport post print sequence. CUando existe parametro copia preprint solo se ejecuta en primera copia, y post en todas (por ser pre abrir cajon y post el corte)
			HashMap<String,String> oidReport=getReport(getQueryXML(inst, new ArrayList<SelectQuery>(), userRol, idtoUserTaskReport,null), operationWithNewIdos,idoUserTaskReport, idtoUserTaskReport, getLabelClassWithoutAlias(idtoUserTaskReport), true, idoFormat, userRol, idtoUserTask, session,nc,automatizar_copias);
			
			if(oidReport!=null){
				communicator switchcom=operationWithNewIdos?getLocalComm():comm;
				switchcom.showReport("_blank",oidReport,automatizar_copias,nc==num_pasos_copia);
			}
		}
	}
	
	public HashMap<String, String> getReport(Element queryXML, boolean operationWithNewIdos,Integer idoUserTaskReport, Integer idtoUserTaskReport, String labelClassWithoutAlias, boolean directImpresion, Integer idoFormat, Integer userRol, Integer idtoUserTask, Session session, int copia_en_curso,boolean automatizar_copias) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		boolean printSequence=true;
		if(idoUserTaskReport!=null){
			printSequence=Boolean.valueOf(((String)getValueData(getField(idoUserTaskReport, idtoUserTaskReport, getIdProp(Constants.PROP_REPORT_EXEC_PRINTSEQUENCE), userRol, idtoUserTask, session))).split(":")[0]);
		}
		boolean ejecuta_preseq=!automatizar_copias || copia_en_curso==1;
		communicator switchcom=operationWithNewIdos?getLocalComm():comm;
		
		HashMap<String,String> oidReport=switchcom.serverGetReport(queryXML, idtoUserTaskReport, labelClassWithoutAlias, directImpresion, idoFormat, printSequence,ejecuta_preseq);
		
		if(oidReport!=null && directImpresion){	
			String copias= automatizar_copias?"1":String.valueOf(getValueData(getField(idoUserTaskReport, idtoUserTaskReport, getIdProp(Constants.PROP_REPORT_NCOPIES), userRol, idtoUserTask, session)));
			oidReport.put(QueryConstants.N_COPIES,copias);
		}		
		return oidReport;
	}

	/* Devuelve un arbol con el mapeo de las columnas de las tablas para la insercion de datos. El arbol muestra el camino para insertar los datos de una o varias columnas.
	 * Cada nodo del camino tiene esta formado por un IdObjectForm con ido e idProp. Cada columna acaba en un nodo hoja.
	 * El nodo raiz solo se utiliza para enganchar los caminos, no contiene informacion */
	public JTree getTreesOfTableColumns(ArrayList<String> columns){
		DefaultMutableTreeNode root=new DefaultMutableTreeNode();
		JTree tree=new JTree(root);
		for(String idColumn:columns){
			if(IdObjectForm.matchFormat(idColumn)){
				IdObjectForm idObjForm=new IdObjectForm(idColumn);
				String parentTree=idObjForm.getIdParent();//Son pares ido,idProp del arbol hasta llegar a la columna
				if(parentTree!=null){
					DefaultMutableTreeNode lastNode=null;
					for(String idoIdProp:parentTree.split("#")){
						String[] s=idoIdProp.split(",");
						String idObject=s[0];
						int idProp=new Integer(s[1]);
						IdObjectForm idObjFormParent=new IdObjectForm();
						idObjFormParent.setIdo(new Integer(idObject));
						idObjFormParent.setIdProp(idProp);
						DefaultMutableTreeNode node=new DefaultMutableTreeNode(idObjFormParent){
	
							@Override
							public boolean equals(Object node) {
								return this.getUserObject().equals(((DefaultMutableTreeNode) node).getUserObject());
							}
							
						};
						if(lastNode==null){
							boolean exist=false;
							
							Enumeration e=root.children();
							while(e.hasMoreElements()){
								DefaultMutableTreeNode n=(DefaultMutableTreeNode)e.nextElement();
								if(n.equals(node)){
									exist=true;
									node=n;
								}
							}
							if(!exist){//Si no existe significa
								root.add(node);
							}
						}else{
							if(lastNode.getIndex(node)==-1)
								lastNode.add(node);
						}
						lastNode=node;
					}
					DefaultMutableTreeNode leafNode=new DefaultMutableTreeNode(idObjForm);
					lastNode.add(leafNode);
				}else{
					//Aqui no comprobamos que ya exista en el bosque ya que nunca puede existir ya que es un nodo hoja y no puede estar repetido
					DefaultMutableTreeNode node=new DefaultMutableTreeNode(idObjForm);
					root.add(node);
				}
			}
		}
		return tree;
	}
	
	public KnowledgeBaseAdapter doClone() throws NotFoundException, IncoherenceInMotorException, EngineException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException{
		IKnowledgeBaseInfo ik=m_kb.doClone();
		KnowledgeBaseAdapter kba=new KnowledgeBaseAdapter(ik);
		kba.m_userTaskMap=m_userTaskMap;
		kba.m_reportMap=m_reportMap;
		kba.m_actionMap=m_actionMap;
		kba.m_importMap=m_importMap;
		kba.m_exportMap=m_exportMap;
		kba.m_idtoUserTaskMap=m_idtoUserTaskMap;
		kba.m_idtoReportMap=m_idtoReportMap;
		kba.m_idtoActionMap=m_idtoActionMap;
		kba.m_idtoImportMap=m_idtoImportMap;
		kba.m_idtoExportMap=m_idtoExportMap;

		kba.m_idoStateMap=m_idoStateMap;
		
		kba.m_user=m_user;
		kba.m_userRols=m_userRols;
		kba.orderProperties=orderProperties;
		kba.carMedias=carMedias;
		kba.columnsTable=columnsTable;
		kba.groupsComponents=groupsComponents;
		kba.highlightedComponents=highlightedComponents;
		kba.listenerMenu=listenerMenu;
		
		return kba;
	}
	
	public DefaultSession createDefaultSession(Session sPadre,Integer utask,boolean checkCoherenceObjects, boolean runRules, boolean lockObjects, boolean deleteFilters, boolean forceParent){
		return new DefaultSession(m_kb,sPadre,utask,checkCoherenceObjects, runRules, lockObjects, deleteFilters, forceParent);
	}
	
	public DefaultSession createDDBBSession(boolean checkCoherenceObjects, boolean runRules, boolean lockObjects, boolean deleteFilters, boolean reusable){
		return new DDBBSession((DocDataModel)m_kb,checkCoherenceObjects, runRules, lockObjects, deleteFilters, reusable);
	}
	
	public IKnowledgeBaseInfo getKnowledgeBase(){
		return m_kb;
	}
	
//	public boolean isDispose(){
//		return m_kb.isDispose();
//	}
	
	public void dispose(){
		if(!m_kb.isDispose())
			m_kb.dispose();
		m_kb=null;
		m_defaultSessionWithoutRules.dispose();
		m_defaultSessionWithoutRules=null;
		m_defaultSession=null;
		m_ddbbSession=null;
		m_instance=null;
		m_instanceStack=null;
		Utils.forceGarbageCollector();
	}
	
	public boolean isDispose(){
		return (m_kb==null);
	}
	
	public boolean hasShowAction(int idoAction,int idtoAction,Integer idtoRangeSource,Integer idtoRangeTarget,Integer idtoUserTask,Integer userRol) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException, ParseException{
		boolean show=true;
		if(idtoRangeSource!=null){
			show=false;
			Iterator<Integer> itrUserTasks=getIdtoUserTasks(idtoRangeSource, null, true, false, true).iterator();
			while(itrUserTasks.hasNext() && !show){
				int idtoUTask=itrUserTasks.next();
				int idoUTask=getIdoUserTask(idtoUTask);
				
				ObjectProperty propertyTargetUTask=getChild(idoUTask,idtoUTask,Constants.IdPROP_TARGETCLASS, userRol, idtoUTask, getDefaultSession());
				int idRangeUTask=getIdRange(propertyTargetUTask);
				int idtoRangeUTask=getClass(idRangeUTask);
				
				show=getAccessIndividual(idRangeUTask, userRol, idtoUTask).getViewAccess();
			}
		}
		if(idtoRangeTarget!=null && (show || idtoRangeSource==null)){
			show=idtoRangeTarget.equals(Constants.IDTO_RESULT_BATCH);//Si es resultBatch permitimos que se muestre aunque no haya ninguna userTasks apuntandole ya que es un objeto que no persiste en base de datos
			Iterator<Integer> itrUserTasks=getIdtoUserTasks(idtoRangeTarget, null, true, false, true).iterator();
			while(itrUserTasks.hasNext() && !show){
				int idtoUTask=itrUserTasks.next();
				int idoUTask=getIdoUserTask(idtoUTask);
				
				ObjectProperty propertyTargetUTask=getChild(idoUTask,idtoUTask,Constants.IdPROP_TARGETCLASS, userRol, idtoUTask, getDefaultSession());
				int idRangeUTask=getIdRange(propertyTargetUTask);
				int idtoRangeUTask=getClass(idRangeUTask);
				
				show=getAccessIndividual(idRangeUTask, userRol, idtoUTask).getViewAccess();//getNewAccess();
				//if(show)
				//	System.err.println("ENCONTRADO SHOW de action:"+m_kba.getLabelUserTask(idoAction)+" con new en target de "+m_kba.getLabelUserTask(idoUTask));
			}
		}
		//Si no hay view sobre esa accion en idtoUserTask o si no hay new sobre el targetClass de las encontradas no mostramos la acción
		if(show && getAccessIndividual(idoAction, userRol, idtoUserTask).getViewAccess() && getAccessIndividual(idoAction, userRol, idtoAction).getViewAccess()/* && m_kba.getAccessIndividual(idTarget, userRol, idtoUserTask).getNewAccess()*/)
			return true;
		else return false;
	}
	
	public void setListenerQuestion(IQuestionListener listener){
		m_kb.setQuestionListener(listener);
	}
	
	public IQuestionListener getQuestionListener(){
		return m_kb.getQuestionListener();
	}
	
	public boolean isAbstractClass(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, OperationNotPermitedException{
		return m_kb.isAbstract(idto);
	}

	public String getLabelClassWithoutAlias(int idto) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException{
		return m_kb.getClassName(idto);
	}

	public void requestInformation(Integer ido, Integer idProp, Session session) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException {
		m_kb.requestInformation(ido, idProp, session);
	}
	
	public Integer getIdProp(String nameProp) throws NotFoundException, IncoherenceInMotorException{
		return m_kb.getIdProperty(nameProp);
	}

	public void addBatchListener(IBatchListener batchListener) {
		m_kb.addBatchListener(batchListener);
	}

	public void removeBatchListener(IBatchListener batchListener) {
		m_kb.removeBatchListener(batchListener);
	}
	
	public void addNoticeListener(INoticeListener noticeListener) {
		m_kb.addNoticeListener(noticeListener);
	}

	public void removeNoticeListener(INoticeListener noticeListener) {
		m_kb.removeNoticeListener(noticeListener);
	}
	
	public void completeFilterLevels(int ido,int idto, Integer userRol, Integer userTask, int levels, Session sess) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException{
		m_kb.completeFilterLevels(ido, idto, userRol, userTask, levels, sess);
	}

	public boolean sendEmail(EmailRequest emailRequest, boolean showError) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException {
		Integer idtoUserTaskReport=emailRequest.getIdtoReport();
		Integer idoUserTaskReport=null;
		if(idtoUserTaskReport!=null){
			idoUserTaskReport=getIdoUserTaskReport(idtoUserTaskReport);
		}
		return m_kb.sendEmail(idoUserTaskReport, idtoUserTaskReport, emailRequest, showError);
	}
	
	public Integer getNotification(int idto) throws NotFoundException, IncoherenceInMotorException{
		Integer idoNotification=null;
		if(m_manualNotifications!=null){
			idoNotification=m_manualNotifications.get(idto);
			if(idoNotification==null){
				Iterator<Integer> itr=m_manualNotifications.keySet().iterator();
				while(idoNotification==null && itr.hasNext()){
					int idtoToNotificate=itr.next();
					if(isSpecialized(idto,idtoToNotificate)){
						idoNotification=m_manualNotifications.get(idtoToNotificate);
					}
				}
			}
		}
		return idoNotification;
	}
	
	public communicator configServer(Integer idtoUserTask){
		if(idtoUserTask!=null && getIdoUserTask(idtoUserTask)!=null){//Solo modificamos si es una usertask, y no una accion o un report
			comm = !isGlobalUtask(idtoUserTask)?Singleton.getInstance().getComm():Singleton.getInstance().getGlobalComm();
			m_kb.setServer(comm);
			m_kb.setLocalServer(Singleton.getInstance().getComm());
		}
		
		return comm;
	}
	

	public boolean isGlobalUtask(Integer idtoUserTask) {
		return m_kb.getGlobalUtasks().contains(idtoUserTask);
	}
	
	public boolean hasGlobalUtasks() {
		return !m_kb.getGlobalUtasks().isEmpty();
	}

	public communicator getServer() {
		return comm;
	}
	
	public void setServer(communicator comm){
		this.comm = comm;
		m_kb.setServer(comm);
	}
	
	public boolean canSetUpColumnProperty(){
		return columnsTable.isPathVersion() && m_userRols!=null && m_userRols.contains(Constants.ADMIN_ROL);
	}
	
}
