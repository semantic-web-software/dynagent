package dynagent.gui.adapter.old;
//package dynagent.gui.adapter;
//
//import java.sql.SQLException;
//import java.text.ParseException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Set;
//
//import javax.naming.NamingException;
//
//import dynagent.ruleengine.src.ruler.Fact;
//import org.jdom.JDOMException;
//
//import dynagent.server.process.IAsigned;
//import dynagent.gui.forms.utils.IOrderProperties;
//import dynagent.ruleengine.Constants;
//import dynagent.ruleengine.CreateIdProp;
//import dynagent.ruleengine.CreateIdo;
//import dynagent.ruleengine.Exceptions.CardinalityExceedException;
//import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
//import dynagent.ruleengine.Exceptions.IncompatibleValueException;
//import dynagent.ruleengine.Exceptions.NotFoundException;
//import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
//import dynagent.ruleengine.alias.IAlias;
//import dynagent.ruleengine.meta.api.BooleanValue;
//import dynagent.ruleengine.meta.api.Category;
//import dynagent.ruleengine.meta.api.DataProperty;
//import dynagent.ruleengine.meta.api.DataValue;
//import dynagent.ruleengine.meta.api.DocDataModel;
//import dynagent.ruleengine.meta.api.DoubleValue;
//import dynagent.ruleengine.meta.api.IChangeServerListener;
//import dynagent.ruleengine.meta.api.IHistoryDDBBListener;
//import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
//import dynagent.ruleengine.meta.api.IntValue;
//import dynagent.ruleengine.meta.api.KnowledgeAdapter;
//import dynagent.ruleengine.meta.api.ObjectProperty;
//import dynagent.ruleengine.meta.api.ObjectValue;
//import dynagent.ruleengine.meta.api.Property;
//import dynagent.ruleengine.meta.api.StringValue;
//import dynagent.ruleengine.meta.api.UnitValue;
//import dynagent.ruleengine.meta.api.Value;
//import dynagent.ruleengine.src.data.dao.Alias;
//import dynagent.ruleengine.src.data.dao.ColumnProperty;
//import dynagent.ruleengine.src.data.dao.OrderProperty;
//import dynagent.ruleengine.src.ruler.FactInstance;
//import dynagent.ruleengine.src.ruler.FactProp;
//import dynagent.ruleengine.src.ruler.IPropertyDef;
//import dynagent.ruleengine.src.sessions.DefaultSession;
//import dynagent.ruleengine.src.sessions.IChangePropertyListener;
//import dynagent.ruleengine.src.sessions.Session;
//import dynagent.ruleengine.src.sessions.SessionController;
//import dynagent.ruleengine.src.sessions.Sessionable;
//import dynagent.ruleengine.src.xml.QueryXML;
//import dynagent.server.application.action;
//import dynagent.server.communication.Changes;
//import dynagent.server.communication.IndividualData;
//import dynagent.server.communication.ObjectChanged;
//import dynagent.server.communication.docServer;
//import dynagent.server.exceptions.ApplicationException;
//import dynagent.server.exceptions.CommunicationException;
//import dynagent.server.exceptions.DataErrorException;
//import dynagent.server.exceptions.RemoteSystemException;
//import dynagent.server.exceptions.SystemException;
//
//import dynagent.server.knowledge.UserAccess;
//import dynagent.server.knowledge.access;
//import dynagent.server.knowledge.instance.instance;
//import dynagent.server.exceptions.InstanceLockedException;
//
//
//public class BusinessRestrictionAdapter implements IOrderProperties,IKnowledgeBaseInfo,Session{
//
//	private ArrayList<OrderProperty> orderPropertyList= new ArrayList<OrderProperty>();
//	/**
//	 * Atributo en el cual esta el server, en este caso no se usa, pero para otros
//	 * adaptadores si es necesario.
//	 */
//	private docServer server;
//	/**
//	 * Entero con el numero de la empresa en la que estamos trabajando.
//	 */
//	//private int bns;
//	/**
//	 * Atributo session.
//	 */
//	//private Session session;
//	/**
//	 * Contendrá un IKnowledgeBaseInfo para acceder al motor, este puede ser DocDataModel,
//	 * o bien, otro adaptador, ya que estos se pueden unir en cascada.
//	 */
//	private IKnowledgeBaseInfo kb;
//
//	/**
//	 * Mapa en el cual se almacena un par (entero, {@link DataAdapter}), el entero corresponde,
//	 * con el ido virtual que creamos en la adaptación, mientras que la clase {@link DataAdapter}
//	 * contiene la información correspondiente a todo lo relacionado con dicho individuo
//	 * virtual.
//	 */
//	private Map<Integer,DataAdapter> tbAdapter = new HashMap<Integer,DataAdapter>();
//	/**
//	 * Mapa correspondiente a la desadaptación de la clase de negocio, es decir, en este mapa
//	 * como clave tendremos el ido original de la BusinessRestriction, mientras que como valor
//	 * tendremos cada una de sus properties ya restringidas.
//	 */
//	private Map<Integer,LinkedList<Property>> tbUnAdapter = new HashMap<Integer,LinkedList<Property>>();
//	/**
//	 * Mapa en el que almacenamos la relacion idto-ido.
//	 */
//	private Map<Integer,Integer> tbIdtoIdo = new HashMap<Integer,Integer>();
//	/**
//	 * Mapa en el que almacenaremos la relación property virtual-property original.
//	 */
//	private Map<Integer,Integer> tbIdPropVIdPropO = new HashMap<Integer,Integer>();
//	/**
//	 * Mapa en el que tendremos una relación porperty-ido
//	 */
//	private Map<Integer,Integer> tbIdPropIdo = new HashMap<Integer,Integer>();
//	/**
//	 * Mapa con una relacion ObjectProperty-idProperty
//	 */
//	//private Map<Integer,Integer> tbOPIdProp= new HashMap<Integer,Integer>();
//	/**
//	 * Mapa con una relacion session-ido, ya que por cada nueva clase de negocio
//	 * se crea una nueva sesión.
//	 */
//	//private Map<Integer,Integer> tbSessionIdo=new HashMap<Integer,Integer>();
//	/**
//	 * Sesión principal en la que se inicio la primera creacion de la clase de negocio.
//	 */
//	//private Integer principalSess=null;
//	/**
//	 * UserRol
//	 */
//	private Integer userRol=null;
//	private Map<Integer, String> tbGroupNameG= new HashMap<Integer, String>();
//	/**
//	 * Entero con la información de la userTask.
//	 */
//	private Integer usertask=null;
//	/**
//	 * String en el que se almacena la información del usuario que esta logeado.
//	 */
//	private String user=null;
//	private IOrderProperties orderProperties=null;
//	private Iterator<Property> originalClassIt=null;
//	private KnowledgeAdapter ka=null;
//	private DefaultSession sessionBus;
//	private Session defSession;
//	private boolean BRBoolean=false;
//	private Session sessRoot;
//	private ArrayList<IHistoryDDBBListener> historyDDBBListeners;
//	//private Session sessAdapting;
//	/**
//	 * Constructor.
//	 * 
//	 * @param kb Interfaz para poder acceder al motor.
//	 * @param server 
//	 * @param bns Numero de la empresa que estamos usando.
//	 * @param df 
//	 * @param sessRoot 
//	 */
//	public BusinessRestrictionAdapter(IKnowledgeBaseInfo kb,IOrderProperties ord,docServer server,int bns, Session df, Session sessRoot){
//		this.kb=kb;
//		//this.bns=bns;
//		this.server=server;
//		this.orderProperties=ord;
//		this.defSession=df;
//		this.ka=new KnowledgeAdapter(kb);
//		this.BRBoolean=false;
//		this.sessRoot=sessRoot;
//		sessionBus = m_kba.createDefaultSession((Session) kb,null,Session.ADAPTER_SPECIALIZE_SESSION);
//		historyDDBBListeners=new ArrayList<IHistoryDDBBListener>();
//		SessionController.getInstance().getSessionsList().remove(this.sessionBus);
//		SessionController.getInstance().getSessionsList().add((Session) this);
//
//	}
//
//	/**
//	 * Lo primero que hacemos es llamar al motor con {@link DocDataModel#createPrototype(int, int, Integer, String, Integer, Session)}
//	 * , con esto conseguimos crear un prototipo de la clase Sofa, con el fin de iterar por sus properties. </br>
//	 * Creamos un nuevo ido virtual, mediante {@link CreateIdo#newIdoVirtual()}, el cual será
//	 * el ido de la nueva clase de negocio que estamos creando. A continuación, creamos una instancia
//	 * de DataAdapter para almacenar toda la información de las properties de la nueva clase de negocio.
//	 * </br>
//	 * Llegados a este punto, iteraremos sobre las properties, comprobando si es una DataProperty,
//	 * o bien una ObjectProperty, en cada uno de los casos la adaptaremos mediante las funciones: 
//	 * {@link #adapteDataProperty(DataProperty, DataAdapter, Integer, int, Integer, String, Integer, Integer)},
//	 * y {@link #adapteObjectProperty(ObjectProperty, DataAdapter, Integer, int, Integer, String, Integer, Integer)}.
//	 * </br>
//	 * Una vez adaptadas cada una de ellas, y almacenadas en el DataAdapter creado anteriormente, devolveremos el 
//	 * ido virtual creado para esta clase de negocio.
//	 * 
//	 * @param idto Entero de la clase que de la cual se va a crear una clase de negocio
//	 * 			   , es decir, si vamos a crear SofaCaribe de Sofa, este parámetro contendra
//	 * 			   el idto de Sofa.
//	 * @param userRol Entero donde va el userRol.
//	 * @param level Entero que indica el nivel del nuevo individuo a crear (FILTRO, PROTOTIPO, INDIVIDUO).
//	 * @param user String almacenando el usuario que esta logeado en estos momentos.
//	 * @param usertask Entero que almacena la información de la userTask.
//	 * @param sess Sesion actual.
//	 * @return Entero con el ido de la nueva clase de negocio, que luego pasará a ser el idto
//	 * 		   en los individuos que se creen sobre esta clase.
//	 * @throws NotFoundException
//	 * @throws IncoherenceInMotorException 
//	 * @throws ApplicationException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 */
//	private Integer createAdapting(int idto, Integer userRol, Integer level, String user, Integer usertask,Session sess) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//		// TODO REVISAR tema de las sessions:
//		//Integer proto=kb.createPrototype(idto, level, userRol, user, usertask,sess);
//		//sess.addSessionable(this);
//		//System.err.println("dynagent.gui.adapter.BusinessRestriction.createAdapting('Entra')");
//		//SessionController.setActual(sess);
//		cleanAll();
//		this.userRol=userRol;
//		this.user=user;
//		this.usertask=usertask;
//		//this.session=sess;
//		Integer idGroup=1;
//
//		this.BRBoolean=true;
//		SessionController.getInstance().setDDM(this);
//		sess.setCategory(Session.ADAPTER_SPECIALIZE_SESSION);
//		Integer idoBR=CreateIdo.getInstance().newIdoVirtual();
//		DataAdapter da= new DataAdapter(idoBR,idto);
//		originalClassIt=kb.getAllPropertyIterator(idto, userRol, user, usertask,sess);
//
//		while(originalClassIt.hasNext()){
//			Property p = originalClassIt.next();
//			if(p instanceof DataProperty){
//				adapteDataProperty((DataProperty)p,da,idoBR,idto, userRol,user,usertask,idGroup);
//			}else{
//				adapteObjectProperty((ObjectProperty)p,da,idoBR,idto, userRol,user,usertask,idGroup);
//			}
//			idGroup++;
//			if (idGroup==200)
//				idGroup++;
//		}
//		tbAdapter.put(idoBR, da);
//		//tbSessionIdo.put(sess.getID(), idoBR);
//		//principalSess=sess.getID();
//		tbIdtoIdo.put(idto,idoBR);
//		return idoBR;
//	}
//	/**
//	 * 
//	 * Primero iteramos sobre el filterList de la ObjectProperty, puesto que aqui es donde van a estar los filtros de a donde apunta
//	 * dicha ObjectProperty, por cada una de las clases a las que apunta dicha ObjectProperty, comprobamos si es especializada, en caso
//	 * de que así sea, se crearán filtros para cada una de ellas.
//	 * </br>
//	 * Estos filtros, los metemos en el valueList de la ObjectProperty y le ponemos como cardinalidades 1-1, de esta manera podemos editar
//	 * el rango al que apunta la ObjecProperty.
//	 * </br>
//	 * Ahora en un segundo paso, creamos dos DataProperty, uno de cadinalidad máxima y otro de cardinalidad mínima.
//	 * En estas DataProperty pondremos la cardinalidad inicial que traía la ObjectProperty, y lo almacenamos en el 
//	 * objeto DataAdapter, con el fin de que el usuario pueda editarlo y restringirlo a posteriori.
//	 * 
//	 * @param p ObjectProperty a adaptar.
//	 * @param da DataProperty donde almacenaremos la ObjectProperty adaptada.
//	 * @param idoBR ido de la clase de negocio.
//	 * @param idto idto de la clase original de la cual se esta realizando la restricción.
//	 * @param userRol Entero en el cual tenemos el userRol.
//	 * @param user String donde almacenamos el usuario logeado en este momento.
//	 * @param usertask Entero en el que se almacena la userTask.
//	 * @param idGroup Entero en el que tenemos el id del grupo, con el fin de agrupar el carmin y carmax de la ObjectProperty en
//	 * 				  dos DataProperty
//	 * @throws NotFoundException
//	 * @throws IncoherenceInMotorException 
//	 */
//	private void adapteObjectProperty(ObjectProperty p, DataAdapter da, Integer idoBR, int idto, Integer userRol, String user, Integer usertask, Integer idGroup) throws NotFoundException, IncoherenceInMotorException {
//		// TODO REVISAR: Revisado y funciona correctamente
//		//LinkedList<Integer> lrango=p.getRangoList();
//		//ObjectPropertyVirtual op=new ObjectPropertyVirtual(p);
//		//LinkedList<Value> lvalues=new LinkedList<Value>();
//		//LinkedList<ObjectValue> lv=op.getFilterList();
//
//		// Creacion de filtros para las clases a las que apunte la objectproperty, y estas
//		// sean especializadas.
//		//Iterator<Integer> itov=lrango.iterator();
//		/*while(itov.hasNext()){
//			Integer vfiltro=itov.next();
//			ObjectValue ovalue=new ObjectValue();
//			ovalue.setValue(vfiltro);
//			ovalue.setValueCls(kb.getClassOf(vfiltro));
//			lvalues.add(ovalue);
//			if(kb.getAccessOverObject(vfiltro, userRol, user, usertask).getSpecializeAccess()){
//				ArrayList<Integer> spec=kb.getSpecializedFilters(vfiltro,userRol,user,usertask,session);
//				Iterator<Integer> itspec=spec.iterator();
//
//				LinkedList<ObjectValue> lovspec=new LinkedList<ObjectValue>();
//				while (itspec.hasNext()){
//					Integer filtrospec=itspec.next();
//
//					ObjectValue ovfiltro=new ObjectValue();
//					ovfiltro.setValue(filtrospec);
//					ovfiltro.setValueCls(kb.getClassOf(filtrospec));
//					lovspec.add(ovfiltro);
//				}
//				lvalues.remove(ovalue);
//				lvalues.addAll(lovspec);
//			}
//		}
//		op.setValues(lvalues);
//		if(lrango.size()==1){
//			op.setCardMax(1);
//			op.setCardMin(1);
//		}
//		op.setNameGroup("Validos");
//		op.setIdGroup(idGroup);
//		tbGroupNameG.put(idGroup, op.getNameGroup());
//		op.setRequired(true);
//		op.setIdProp(CreateIdProp.getInstance().newIdProp());
//		op.setIdo(idoBR);
//		op.setIdto(idto);*/
//
//		if(!p.getEnumList().isEmpty()){
//			ObjectPropertyVirtual openum=new ObjectPropertyVirtual(p);
//			openum.cardMax=null;
//			openum.setIdGroup(idGroup);
//			//openum.setCardMax(p.getCardMax());
//			openum.setCardMin(null);
//			openum.setNameGroup(p.getName());
//			openum.setRequired(true);
//			openum.setIdProp(CreateIdProp.getInstance().newIdProp());
//			openum.setIdo(idoBR);
//			openum.setIdto(idto);
//			openum.setName("Opciones: ");
//			if (p.getIdo()==null){
//				da.setPropertyAdapter(openum, p.getIdProp(), openum.getIdo(), p.getIdto());
//			}else{
//				da.setPropertyAdapter(openum, p.getIdProp(), p.getIdo(), p.getIdto());
//			}
//			da.setType(openum, "ENUMERADOS");
//			OrderProperty ordp= new OrderProperty();
//			ordp.setProp(openum.getIdProp());
//			ordp.setIdto(openum.getIdto());
//			ordp.setOrder(1);
//			ordp.setGroup(openum.getIdGroup());
//			ordp.setSec(1);
//			orderPropertyList.add(ordp);
//
//			tbIdPropVIdPropO.put(openum.getIdProp(), p.getIdProp());
//			tbIdPropIdo.put(openum.getIdProp(), openum.getIdo());
//			tbGroupNameG.put(idGroup, openum.getNameGroup());
//		}else{
//			ObjectPropertyVirtual opsel=new ObjectPropertyVirtual(p);
//			opsel.setIdProp(CreateIdProp.getInstance().newIdProp());
//			//opsel.setCardMax(null);
//			opsel.setCardMin(null);
//			opsel.setIdGroup(0);
//			//opsel.setNameGroup("Asociaciones");
//			opsel.setRequired(true);
//			opsel.setIdo(idoBR);
//			opsel.setIdto(idto);
//			//opsel.setName(p.getName());
//			if (p.getIdo()==null){
//				da.setPropertyAdapter(opsel, p.getIdProp(), opsel.getIdo(), p.getIdto());
//			}else{
//				da.setPropertyAdapter(opsel, p.getIdProp(), p.getIdo(), p.getIdto());
//			}
//			da.setType(opsel, "NORMAL");
//			OrderProperty ordp= new OrderProperty();
//			ordp.setProp(opsel.getIdProp());
//			ordp.setIdto(opsel.getIdto());
//			ordp.setOrder(3);
//			ordp.setGroup(opsel.getIdGroup());
//			ordp.setSec(1);
//			orderPropertyList.add(ordp);
//
//			tbIdPropVIdPropO.put(opsel.getIdProp(), p.getIdProp());
//			tbIdPropIdo.put(opsel.getIdProp(), opsel.getIdo());
//			tbGroupNameG.put(0, opsel.getNameGroup());
//		}
//		/*if(p.getIdo()==null){
//			da.setPropertyAdapter(op,p.getIdProp(),op.getIdo(), p.getIdto());
//		}else{
//			da.setPropertyAdapter(op,p.getIdProp(), p.getIdo(), p.getIdto());
//		}
//
//		da.setType(op, "RANGO");
//		tbIdPropVIdPropO.put(op.getIdProp(), p.getIdProp());
//		tbIdPropIdo.put(op.getIdProp(), op.getIdo());*/
//		/*Iterator <Integer> itrango=lrango.iterator();
//		while(itrango.hasNext()){
//			tbOPIdProp.put(kb.getClassOf(itrango.next()), op.getIdProp());
//		}*/
//
//
////		DataPropertyVirtual dpcmin=new DataPropertyVirtual();
////		DataPropertyVirtual dpcmax=new DataPropertyVirtual();
////		dpcmin.setName("Card Min: ");
////		/*Category c=new Category();
////		c.setDataProperty();
////		dpcmin.setCategory(c);*/
////		dpcmin.setDataType(Constants.IDTO_INT);
////		dpcmin.setEnumList(new LinkedList<DataValue>());
////		dpcmin.setExcluList(new LinkedList<DataValue>());
////		dpcmin.setIdGroup(idGroup);
////		dpcmin.setIdo(idoBR);
////		Integer idPropV=CreateIdProp.getInstance().newIdProp();
////		dpcmin.setIdProp(idPropV);
////		dpcmin.setIdto(idto);
////		dpcmin.setNameGroup(kb.getClassName(p.getRangoList().getFirst()));
////		tbGroupNameG.put(idGroup, dpcmin.getNameGroup());
////		dpcmin.setRequired(true);
////		access ac= new access(true,false,true,false,false,false,false,false,false,false);
////		dpcmin.setTypeAccess(ac);
////		if(p.getCardMin()!=null){
////		IntValue iv= new IntValue();
////		iv.setValueMax(p.getCardMin());
////		iv.setValueMin(p.getCardMin());
////		LinkedList<Value> lvd= new LinkedList<Value>();
////		lvd.add(iv);
////		dpcmin.setValues(lvd);
////		}else{
////		LinkedList<Value> lvd= new LinkedList<Value>();
////		dpcmin.setValues(lvd);
////		}
//
////		dpcmax.setName("Card Max: ");
////		//dpcmax.setCategory(c);
////		dpcmax.setDataType(Constants.IDTO_INT);
////		dpcmax.setEnumList(new LinkedList<DataValue>());
////		dpcmax.setExcluList(new LinkedList<DataValue>());
////		dpcmax.setIdGroup(idGroup);
////		dpcmax.setIdo(idoBR);
////		idPropV=CreateIdProp.getInstance().newIdProp();
////		dpcmax.setIdProp(idPropV);
////		dpcmax.setIdto(idto);
////		dpcmax.setNameGroup(kb.getClassName(p.getRangoList().getFirst()));
////		tbGroupNameG.put(idGroup, dpcmax.getNameGroup());
////		dpcmax.setRequired(true);
//
////		dpcmax.setTypeAccess(ac);
////		if(p.getCardMax()!=null){
////		IntValue iv= new IntValue();
////		iv.setValueMax(p.getCardMax());
////		iv.setValueMin(p.getCardMax());
////		LinkedList<Value> lvd= new LinkedList<Value>();
////		lvd.add(iv);
////		dpcmax.setValues(lvd);
////		}else{
////		LinkedList<Value> lvd= new LinkedList<Value>();
////		dpcmax.setValues(lvd);
////		}
////		if(p.getIdo()==null){
////		da.setPropertyAdapter(dpcmin, p.getIdProp(), dpcmin.getIdo(), p.getIdto());
////		da.setPropertyAdapter(dpcmax, p.getIdProp(), dpcmax.getIdo(), p.getIdto());
////		}else{
////		da.setPropertyAdapter(dpcmin, p.getIdProp(), p.getIdo(), p.getIdto());
////		da.setPropertyAdapter(dpcmax, p.getIdProp(), p.getIdo(), p.getIdto());
////		}
//
////		da.setType(dpcmin, "CMIN");
////		da.setType(dpcmax, "CMAX");
////		OrderProperty opmin= new OrderProperty();
////		OrderProperty opmax= new OrderProperty();
////		opmin.setProp(dpcmin.getIdProp());
////		opmin.setIdto(dpcmin.getIdto());
////		opmin.setOrder(1);
////		opmin.setGroup(dpcmin.getIdGroup());
////		opmin.setSec(1);
////		opmax.setProp(dpcmax.getIdProp());
////		opmax.setIdto(dpcmax.getIdto());
////		opmax.setOrder(2);
////		opmax.setGroup(dpcmax.getIdGroup());
////		opmax.setSec(1);
////		orderPropertyList.add(opmin);
////		orderPropertyList.add(opmax);
////		tbIdPropVIdPropO.put(dpcmin.getIdProp(), p.getIdProp());
////		tbIdPropVIdPropO.put(dpcmax.getIdProp(), p.getIdProp());
////		tbIdPropIdo.put(dpcmax.getIdProp(), dpcmax.getIdo());
////		tbIdPropIdo.put(dpcmin.getIdProp(), dpcmin.getIdo());
//
//	}
//	/**
//	 * Esta función la usaremos para comprobar si un ido ya a sido adaptado, con el fin de que cuando pregunten properties
//	 * sobre él no llame a motor sino que lo busque en el DataAdapter correspondiente.
//	 * @param ido Entero con el ido original.
//	 * @return Entero con el ido virtual asociado.
//	 */
//	private Integer getIdoVirtual(int ido) {
//		Integer result=null;
//		Set<Integer> key=tbAdapter.keySet();
//		Iterator<Integer> it=key.iterator();
//		while (it.hasNext()){
//			result=it.next();
//			DataAdapter da=tbAdapter.get(result);
//			Property p=da.getProperty(Constants.IdPROP_RDN);
//			Integer idoOrignal=da.getOriginalIdoClass(p);
//			if(idoOrignal.equals(ido)){
//
//				return result;
//			}
//		}
//		return result;
//	}
//	/**
//	 * 
//	 * Por cada tipo de DataProperty que llege (DOUBLE, UNIT, INT), vamos a crear dos DataProperty con el fin de restringir su rango de valores.
//	 * Si estas properties ya traían una restricción inicial, ésta se mantendrá. Exceptuando el RDN, esta property no se adaptará.
//	 * 
//	 * @param p DataProperty a adaptar
//	 * @param da DataAdapter donde se almacenará la property adaptada.
//	 * @param idoBR ido virtual de la nueva clase de negocio.
//	 * @param idto idto de la clase de la cual se está creando una clase de negocio.
//	 * @param userRol Entero en el que se almacena el userRol.
//	 * @param user String en el cual se almacena el usuario que esta logeado en estos momentos.
//	 * @param usertask Entero con la userTask
//	 * @param idGroup Entero con el id del grupo para agrupar.
//	 */
//	private void adapteDataProperty(DataProperty p, DataAdapter da, Integer idoBR, int idto, Integer userRol, String user, Integer usertask, Integer idGroup) {
//		// TODO REVISAR:
//		int i=1;
//
//		if((p.getDataType()==Constants.IDTO_DOUBLE ||p.getDataType()==Constants.IDTO_UNIT || p.getDataType()==Constants.IDTO_INT) && p.getIdProp()!=Constants.IdPROP_RDN){
//			DataPropertyVirtual dpmin=new DataPropertyVirtual(p);
//			DataPropertyVirtual dpmax= new DataPropertyVirtual(p);
//			dpmin.setEnumList(new LinkedList<DataValue>());
//			dpmax.setEnumList(new LinkedList<DataValue>());
//			Integer idPropV=CreateIdProp.getInstance().newIdProp();
//
//			dpmin.setIdo(idoBR);
//			dpmin.setIdto(idto);
//			dpmin.setIdProp(idPropV);
//			//dpmin.setCardMax(null);
//			dpmin.setCardMin(null);
//			//dpmax.setCardMax(null);
//			dpmax.setCardMin(null);
//			idPropV=CreateIdProp.getInstance().newIdProp();
//			dpmax.setIdo(idoBR);
//			dpmax.setIdto(idto);
//			dpmax.setIdProp(idPropV);
//			if(!p.getEnumList().isEmpty()){
//				IntValue iv= (IntValue)p.getEnumList().getFirst();
//				Integer vmin=iv.getValueMin();
//				Integer vmax=iv.getValueMax();
//
//				IntValue ivmin= new IntValue();
//				ivmin.setValueMax(vmin);
//				ivmin.setValueMin(vmin);
//				LinkedList<Value> lv=new LinkedList<Value>();
//				lv.add(ivmin);
//				dpmin.setValues(lv);
//
//				IntValue ivmax= new IntValue();
//				ivmax.setValueMax(vmax);
//				ivmax.setValueMin(vmax);
//				lv=new LinkedList<Value>();
//				lv.add(ivmax);
//				dpmax.setValues(lv);
//			}
//			dpmin.setName("Valor Min:");
//			dpmax.setName("Valor Max:");
//			dpmin.setNameGroup(p.getName());
//			dpmax.setNameGroup(p.getName());
//			tbGroupNameG.put(idGroup, p.getName());
//			dpmin.setIdGroup(idGroup);
//			dpmax.setIdGroup(idGroup);
//			dpmin.setRequired(true);
//			dpmax.setRequired(true);
//			OrderProperty opmin=new OrderProperty();
//			OrderProperty opmax=new OrderProperty();
//			if (p.getIdo()!=null){
//				da.setPropertyAdapter(dpmax, p.getIdProp(), p.getIdo(), p.getIdto());
//				da.setPropertyAdapter(dpmin, p.getIdProp(), p.getIdo(), p.getIdto());
//			}else{
//				da.setPropertyAdapter(dpmax, p.getIdProp(), dpmax.getIdo(), p.getIdto());
//				da.setPropertyAdapter(dpmin, p.getIdProp(), dpmin.getIdo(), p.getIdto());
//			}
//
//			da.setType(dpmin, "MIN");
//			da.setType(dpmax, "MAX");
//			opmin.setProp(dpmin.getIdProp());
//			opmin.setIdto(dpmin.getIdto());
//			opmin.setOrder(1);
//			opmin.setGroup(dpmin.getIdGroup());
//			opmin.setSec(1);
//			opmax.setProp(dpmax.getIdProp());
//			opmax.setIdto(dpmax.getIdto());
//			opmax.setOrder(2);
//			opmax.setGroup(dpmax.getIdGroup());
//			opmax.setSec(1);
//			orderPropertyList.add(opmin);
//			orderPropertyList.add(opmax);
//			tbIdPropVIdPropO.put(dpmin.getIdProp(), p.getIdProp());
//			tbIdPropVIdPropO.put(dpmax.getIdProp(), p.getIdProp());
//			tbIdPropIdo.put(dpmax.getIdProp(), dpmax.getIdo());
//			tbIdPropIdo.put(dpmin.getIdProp(), dpmin.getIdo());
//
//			DataPropertyVirtual dpvalue=new DataPropertyVirtual(p);
//			idPropV=CreateIdProp.getInstance().newIdProp();
//
//			dpvalue.setIdo(idoBR);
//			dpvalue.setIdto(idto);
//			dpvalue.setIdProp(idPropV);
//			//dpvalue.setCardMax(null);
//			dpvalue.setCardMin(null);
//			dpvalue.setName("Valor Fijo:");
//			dpvalue.setNameGroup(p.getName());
//			tbGroupNameG.put(idGroup, p.getName());
//			dpvalue.setIdGroup(idGroup);
//
//			dpvalue.setRequired(true);
//
//			OrderProperty opvalue=new OrderProperty();
//
//			if (p.getIdo()!=null){
//				da.setPropertyAdapter(dpvalue, p.getIdProp(), p.getIdo(), p.getIdto());
//
//			}else{
//				da.setPropertyAdapter(dpvalue, p.getIdProp(), dpvalue.getIdo(), p.getIdto());
//
//			}
//
//			da.setType(dpvalue, "FIJO");
//
//			opvalue.setProp(dpvalue.getIdProp());
//			opvalue.setIdto(dpvalue.getIdto());
//			opvalue.setOrder(3);
//			opvalue.setGroup(dpvalue.getIdGroup());
//			opvalue.setSec(1);
//
//			orderPropertyList.add(opvalue);
//
//			tbIdPropVIdPropO.put(dpvalue.getIdProp(), p.getIdProp());
//
//			tbIdPropIdo.put(dpvalue.getIdProp(), dpvalue.getIdo());
//
//			if(p.getEnumList().size()>1){
//				LinkedList<DataValue> lenum=p.getEnumList();
//				Iterator <DataValue> itenum=lenum.iterator();
//				itenum.next();
//				while(itenum.hasNext()){
//					i++;
//					dpmin=new DataPropertyVirtual(p);
//					dpmax=new DataPropertyVirtual(p);
//					idPropV=CreateIdProp.getInstance().newIdProp();
//
//					dpmin.setIdo(idoBR);
//					dpmin.setIdto(idto);
//					dpmin.setIdProp(idPropV);
//					idPropV=CreateIdProp.getInstance().newIdProp();
//					dpmax.setIdo(idoBR);
//					dpmax.setIdto(idto);
//					dpmax.setIdProp(idPropV);					
//					IntValue iv= (IntValue)itenum.next();
//					Integer vmin=iv.getValueMin();
//					Integer vmax=iv.getValueMax();
//
//					IntValue ivmin= new IntValue();
//					ivmin.setValueMax(vmin);
//					ivmin.setValueMin(vmin);
//					LinkedList<Value> lv=new LinkedList<Value>();
//					lv.add(ivmin);
//					dpmin.setValues(lv);
//
//					IntValue ivmax= new IntValue();
//					ivmax.setValueMax(vmax);
//					ivmax.setValueMin(vmax);
//					lv=new LinkedList<Value>();
//					lv.add(ivmax);
//					dpmax.setValues(lv);
//
//					dpmin.setName("Valor Min:");
//					dpmax.setName("Valor Max:");
//					dpmin.setNameGroup(p.getName());
//					dpmax.setNameGroup(p.getName());
//					dpmin.setIdGroup(idGroup);
//					dpmax.setIdGroup(idGroup);
//					dpmin.setRequired(true);
//					dpmax.setRequired(true);
//
//					if (p.getIdo()!=null){
//						da.setPropertyAdapter(dpmax, p.getIdProp(), p.getIdo(), p.getIdto());
//						da.setPropertyAdapter(dpmin, p.getIdProp(), p.getIdo(), p.getIdto());
//					}else{
//						da.setPropertyAdapter(dpmax, p.getIdProp(), dpmax.getIdo(), p.getIdto());
//						da.setPropertyAdapter(dpmin, p.getIdProp(), dpmin.getIdo(), p.getIdto());
//					}
//
//					da.setType(dpmin, "MIN");
//					da.setType(dpmax, "MAX");
//					opmin.setProp(dpmin.getIdProp());
//					opmin.setIdto(dpmin.getIdto());
//					opmin.setOrder(1);
//					opmin.setGroup(dpmin.getIdGroup());
//					opmin.setSec(1);
//					opmax.setProp(dpmax.getIdProp());
//					opmax.setIdto(dpmax.getIdto());
//					opmax.setOrder(2);
//					opmax.setGroup(dpmax.getIdGroup());
//					opmax.setSec(1);
//					orderPropertyList.add(opmin);
//					orderPropertyList.add(opmax);
//					tbIdPropVIdPropO.put(dpmin.getIdProp(), p.getIdProp());
//					tbIdPropVIdPropO.put(dpmax.getIdProp(), p.getIdProp());
//					tbIdPropIdo.put(dpmax.getIdProp(), dpmax.getIdo());
//					tbIdPropIdo.put(dpmin.getIdProp(), dpmin.getIdo());
//				}
//			}
//		}else if(p.getIdProp()==Constants.IdPROP_RDN){
//			DataPropertyVirtual dpv=new DataPropertyVirtual(p);
//
//			dpv.setIdo(idoBR);
//			if (p.getIdo()!=null){
//				da.setPropertyAdapter(dpv, p.getIdProp(), p.getIdo(), p.getIdto());
//				tbIdPropVIdPropO.put(dpv.getIdProp(), p.getIdProp());
//
//
//				tbIdPropIdo.put(dpv.getIdProp(), dpv.getIdo());
//			}else{
//				da.setPropertyAdapter(dpv, p.getIdProp(), dpv.getIdo(), p.getIdto());
//				tbIdPropVIdPropO.put(dpv.getIdProp(), p.getIdProp());
//
//				tbIdPropIdo.put(dpv.getIdProp(), dpv.getIdo());
//
//			}
//		}
//
//	}
//	/**
//	 * Recogemos el acceso original del objeto y le añadimos el acceso de nuevo, para que se puedan crear las nuevas clases de negocio.
//	 * @throws IncoherenceInMotorException 
//	 * @throws ApplicationException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 */
//	public access getAccessOverObject(Integer id, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//		// TODO REVISAR:
//		access aoo= kb.getAccessOverObject(id, userRol, user, usertask);
//		if(aoo.getSpecializeAccess()){
//			access aoonew=new access(aoo.getViewAccess(),true,aoo.getSetAccess(),aoo.getRelAccess(),aoo.getUnrelAccess(),aoo.getDelAccess(),aoo.getConcreteAccess(),aoo.getSpecializeAccess(),aoo.getAbstractAccess(),aoo.getCommentAccess());
//			aoo=aoonew;
//		}
//		//System.err.println("Permisos adaptador:"+aoo);
//		return aoo;
//	}
//	/**
//	 * 
//	 * Función similar a {@link #getIdoVirtual(int)}.
//	 * 
//	 * @param ido Entero con el ido original.
//	 * @return boolean que indica si ese ido está o no adaptado.
//	 */
//	private boolean isIdoAdapter(Integer ido) {
//		boolean result=false;
//		Set<Integer> key=tbAdapter.keySet();
//		Iterator<Integer> it=key.iterator();
//		while (it.hasNext()){
//			DataAdapter da=tbAdapter.get(it.next());
//			Property p=da.getProperty(Constants.IdPROP_RDN);
//			Integer idoOriginal=da.getOriginalIdoClass(p);
//
//			if(idoOriginal.equals(ido)){
//				result=true;
//				return result;
//			}
//		}
//		return result;
//	}
//
//
//	/**
//	 * Delegamos la función a motor. {@link DocDataModel#getClassIterator()}.
//	 */
//	public Iterator<Integer> getClassIterator() throws NotFoundException {
//		return kb.getClassIterator();
//	}
//
//	/**
//	 * Función que no se usa en estos momentos. Se realizo esta función puesto que en un principio se pensaba pasar las properties
//	 * de la clase de negocio al server en forma de instance, pero al final se pasan mediante XML.
//	 * @param ido
//	 * @return
//	 */
//	//private instance createInstance(Integer ido){
//	/*DataAdapter da= tbAdapter.get(ido);
//		instance result=new instance(da.getIdtoTemp(),ido);
//		Iterator<Property> itp= da.getPropertyIterator();
//		while(itp.hasNext()){
//			Property p=itp.next();
//			p.setOrder(action.SET_CLASS);
//			result.addProperty(ido, p);
//		}
//		return result;*/
//	//return null;
//	//}
//
////	private Integer getIdtoAdapte(Integer ido) {
////	Set<Integer> keys=tbIdtoIdo.keySet();
////	Iterator<Integer> itk=keys.iterator();
////	while(itk.hasNext()){
////	Integer key=itk.next();
////	if(tbIdtoIdo.get(key).equals(ido)){
////	return key;
////	}
////	}
////	return null;
////	}
//
//
//
//	/**
//	 * Comprueba si la property es virtual o no, si lo es mira cual es su property original y delega a motor la función con la property original.
//	 * Si no delega la función al motor. {@link DocDataModel#getDatatype(int)}.
//	 * 
//	 * @param idProp Entero que identifica la property.
//	 * @throws NotFoundException 
//	 * @retun Entero que indica de que tipo es la property.
//	 * 
//	 */
//	public Integer getDatatype(int idProp) throws NotFoundException {
//		//TODO REVISAR:
//		if(tbIdPropVIdPropO.containsKey(idProp)){
//			return kb.getDatatype(tbIdPropVIdPropO.get(idProp));
//		}
//		return kb.getDatatype(idProp);
//	}
//
//
//
//
//
//
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#getQueryXML()}.
//	 */
//	public QueryXML getQueryXML() {
//		return kb.getQueryXML();
//	}
//
//
//
//
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#isSpecialized(int, int)}.
//	 * @throws NotFoundException 
//	 */
//	public boolean isSpecialized(int idto, int posSuperior) throws NotFoundException {
//		return kb.isSpecialized(idto, posSuperior);
//	}
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#isUnit(Integer)}.
//	 */
//
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#setRuler(Ruler)}.
//	 */
//
//
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#setServer(docServer)}.
//	 */
//	public void setServer(docServer server) {
//		kb.setServer(server);	
//	}
//
//
//	/**
//	 * 
//	 * Miramos si existe ya el ido en el mapa de idos desadaptados({@link #tbUnAdapter}),
//	 * si está, extraemos el linkedlist de properties asociada para añadirle esta nueva
//	 * property. Si no está en el mapa, nos creamos un nuevo linkedlist de properties.
//	 * </br>
//	 * Si el LinkedList de properties no esta vacio, debemos recorrerlo, con el fin de ver
//	 * si la property ya se encontraba, con lo que tendremos que eliminarla y añadir esta nueva
//	 * Por último añadimos el par (ido, LinkedList<Property>) al mapa.
//	 * 
//	 * @param ido Entero que identifica al individuo que posee la property.
//	 * @param dpuna Property desadaptada que queremos almacenar.
//	 */
//	private void setPropertyUnAdapte(Integer ido, Property dpuna) {
//		//TODO REVISAR:
//		LinkedList<Property> lp=null;
//		/*System.out.println("BusinessRestriction.setPropertyUnAdapte:");
//		System.out.println(dpuna);*/
//		System.out.println("PROERTY TBUA pasar:"+dpuna);
//		if(tbUnAdapter.containsKey(ido)){
//			lp=tbUnAdapter.get(ido);
//		}else{
//			lp=new LinkedList<Property>();
//		}
//		if(!lp.isEmpty()){
//			Iterator<Property> itp = lp.iterator();
//			while(itp.hasNext()){
//				Property p =itp.next();
//				System.out.println("PROERTY TBUA:"+p);
//				if(p.getIdProp().equals(dpuna.getIdProp())){
//					lp.remove(p);
//					break;
//				}
//			}
//		}
//		lp.add(dpuna);
//
//		tbUnAdapter.put(ido, lp);
//		imprimeUnAdapter();
//	}
//	/**
//	 * Función auxiliar para imprimir por pantalla los individuos que hay en ese momento
//	 * en el mapa de individuos desadaptados ({@link #tbUnAdapter}).
//	 *
//	 */
//	private void imprimeUnAdapter() {
//		System.out.println("******tbUnAdapter******");
//		Set<Integer> keys=tbUnAdapter.keySet();
//		Iterator<Integer> itkey=keys.iterator();
//		while(itkey.hasNext()){
//			Integer key=itkey.next();
//			System.out.println("--IDO="+key);
//			LinkedList<Property> lp=tbUnAdapter.get(key);
//			Iterator<Property> itp=lp.iterator();
//			while(itp.hasNext()){
//				Property p=itp.next();
//				System.out.println("    PROPERTY = "+p.idProp +" NAME="+p.getName());
//				System.out.println(p);
//			}
//
//		}
//	}
//
//	/**
//	 * 
//	 * Función por la cual extraemos una property concreta del mapa {@link #tbUnAdapter}.
//	 * 
//	 * @param idPropO Identificador de la property a extraer.
//	 * @return Property desadaptada.
//	 */
//	private Property getPropertyUnAdapte(Integer idPropO) {
//		Set<Integer> keys=tbUnAdapter.keySet();
//		Iterator<Integer> itkeys=keys.iterator();
//		while (itkeys.hasNext()){
//			Integer key=itkeys.next();
//			LinkedList<Property> punadapte=tbUnAdapter.get(key);
//			Iterator <Property> itp=punadapte.iterator();
//			while(itp.hasNext()){
//				Property p= itp.next();
//				if(p.getIdProp().equals(idPropO)){
//					return p;
//				}
//			}
//
//		}
//		return null;
//	}
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#specializeIn(int, int)}.
//	 */
//	public int specializeIn(int id, int idtoSpecialized) {
//		return kb.specializeIn(id, idtoSpecialized);
//	}
//
//
//	/**
//	 * Comprobamos si el idto que se le pasa tiene como acceso el acceso especialized, si es así,
//	 * llamamos a {@link #createAdapting(int, Integer, Integer, String, Integer, Session)}.
//	 * Si no delegamos la funcionalidad al motor. {@link DocDataModel#createPrototype(int, int, Integer, String, Integer, Session)}.
//	 * @throws ApplicationException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 * @throws IncoherenceInMotorException 
//	 */
//	public Integer createPrototype(int idto, int level, Integer userRol, String user, Integer usertask, Session sess) throws NotFoundException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, IncoherenceInMotorException {
//		//TODO Tratamiento de la excepcion y ver como salta.
//		Integer result=null;
//		access aclass=null;
//
//		//TODO getAccessOverObject deberia tener sesion
//		aclass = kb.getAccessOverObject(idto, userRol, user, usertask);
//		if(aclass.getSpecializeAccess()){
//			//System.err.println("dynagent.gui.adapter.BusinessRestriction.createPrototype('CreaAdaptacion')");
//			result=createAdapting(idto,userRol,level, user, usertask,sess);
//		}else{
//			//System.err.println("dynagent.gui.adapter.BusinessRestriction.createPrototype('CreaPrototipo')");
//			result=kb.createPrototype(idto, level, userRol, user, usertask,sess);
//		}
//
//
//		return result;
//	}
//	/**
//	 * Comprobamos si se trata de un individuo adaptado, si es asi extraemos las properties
//	 * de su DataAdapter asociado. Si no, delegamos la funcionalida al motor. {@link DocDataModel#getAllPropertyIterator(int, Integer, String, Integer, Session)}.
//	 * @throws ApplicationException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws IncoherenceInMotorException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 */
//	public Iterator<Property> getAllPropertyIterator(int ido, Integer userRol, String user, Integer usertask, Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//		//TODO REVISAR:
//		if(isIdoAdapter(ido)){
//			Integer idoVirtual=getIdoVirtual(ido);
//			DataAdapter da= tbAdapter.get(idoVirtual);
//			return da.getPropertyIterator();
//		}else if (tbAdapter.containsKey(ido)){
//			DataAdapter da= tbAdapter.get(ido);
//			return da.getPropertyIterator();
//
//		}
//		else if(isIdtoAdapter(ido)){
//			Integer idoVirtual=getIdoVirtual(ido);
//			DataAdapter da= tbAdapter.get(idoVirtual);
//			return da.getPropertyIterator();
//		}
//
//
//		access aclass=null;
//		Iterator<Property> itp=kb.getAllPropertyIterator(ido, userRol, user, usertask,sessionPadre);
//		Iterator<Integer> itidto=kb.getSuperior(ido);
//		Property prop=kb.getProperty(ido, Constants.IdPROP_RDN, userRol, user, usertask, sessionPadre);
//		Integer idoBR=prop.getIdto();
//		ArrayList<Property> list= new ArrayList<Property>();
//
//		if (itidto.hasNext()) {
//			Integer idto = itidto.next();
//			aclass = kb.getAccessOverObject(idto, userRol, user, usertask);
//			if (aclass.getSpecializeAccess() && !this.BRBoolean) {
//				while (itp.hasNext()) {
//					Property p = itp.next();
//					p.setIdo(idoBR);
//					p.setIdto(idto);
//					list.add(p);
//				}
//
//				// System.err.println("dynagent.gui.adapter.BusinessRestriction.createPrototype('CreaAdaptacion')");
//				// createAdapting(idto,userRol,null, user,
//				// usertask,sessionPadre);
//				editAdapting(list, userRol, user, usertask, sessionPadre,
//						idto, idoBR);
//			}
//
//		}
//
//
//		return itp;
//	}
//
//	private void editAdapting(ArrayList<Property> list, Integer userRol, String user, Integer usertask, Session sessionPadre, Integer idto, Integer idoBR) throws NotFoundException, IncoherenceInMotorException {
////		TODO REVISAR tema de las sessions:
//		//Integer proto=kb.createPrototype(idto, level, userRol, user, usertask,sess);
//		//sess.addSessionable(this);
//		//System.err.println("dynagent.gui.adapter.BusinessRestriction.editAdapting('Entra')");
//		//SessionController.setActual(sess);
//		cleanAll();
//		//this.session=sessionPadre;
//		Integer idGroup=1;
//		this.BRBoolean=true;
//		SessionController.getInstance().setDDM(this);
//		sessionPadre.setCategory(Session.ADAPTER_SPECIALIZE_SESSION);
//
//		DataAdapter da= new DataAdapter(idoBR,idto);
//
//		originalClassIt=list.iterator();
//
//		while(originalClassIt.hasNext()){
//			Property p = originalClassIt.next();
//			if(p instanceof DataProperty){
//				adapteDataProperty((DataProperty)p,da,idoBR,idto, userRol,user,usertask,idGroup);
//			}else{
//				adapteObjectProperty((ObjectProperty)p,da,idoBR,idto, userRol,user,usertask,idGroup);
//			}
//			idGroup++;
//			if (idGroup==200)
//				idGroup++;
//		}
//		tbAdapter.put(idoBR, da);
//		//tbSessionIdo.put(sess.getID(), idoBR);
//		//principalSess=sess.getID();
//		tbIdtoIdo.put(idto,idoBR);
//
//	}
//
//	private boolean isIdtoAdapter(int idto) {
//		boolean result=false;
//		Set<Integer> key=tbAdapter.keySet();
//		Iterator<Integer> it=key.iterator();
//		while (it.hasNext()){
//			DataAdapter da=tbAdapter.get(it.next());
//			Property p=da.getProperty(Constants.IdPROP_RDN);
//			Integer idoOriginal=da.getOriginalIdtoClass(p);
//
//			if(idoOriginal.equals(idto)){
//				result=true;
//				return result;
//			}
//		}
//		return result;
//	}
//
//	/**
//	 * 
//	 * Recogemos la porperty original del motor, y comprobamos que el linkedList del nuevo
//	 * rango este contenido en el de la property del motor, ya que la restricción es disminuir el rango
//	 * y no introducir nuevas clases en el rango que antes no estubieran.
//	 * 
//	 * @param idoO Entero que identifica al individuo original.
//	 * @param idPropO Entero que identifica la property original.
//	 * @param type Cadena que identifica de que tipo es la restricción.
//	 * @return booleano indicando si pasa la comprobación o no.
//	 * @throws NotFoundException
//	 * @throws CardinalityExceedException 
//	 */
//	private void checkFieldsObjectProp(Integer idoO, Integer idPropO, Value val, String type) throws  CardinalityExceedException {
//
//		if(type.equals("NORMAL")){
//			DataAdapter da= tbAdapter.get(idoO);
//			ObjectPropertyVirtual op=(ObjectPropertyVirtual)da.getProperty(idPropO);
//			if(op.getValues().size()>op.getCardMax()){
//				CardinalityExceedException ceE=new CardinalityExceedException("Solamente puede tener '"+op.getValues().size()+"' valores asociados",op);
//				ceE.setUserMessage("Solamente puede tener '"+op.getValues().size()+"' valores asociados");
//				throw ceE;
//			}
//		}
//
////		Property prop=kb.getProperty(idoO, idPropO, userRol, user, usertask, session);
//
////		if(type.equals("RANGO")){
////		ObjectProperty po=(ObjectProperty)prop;
////		LinkedList<Integer> lrangoclass=po.getRangoList();
////		if(!lrangoclass.containsAll(lrangonew)){
////		result=false;
////		return result;
////		}
////		}
//
//
//
//	}
//
//	/**
//	 * Función auxiliar para extrar la numeración de la cadena de las condiciones.
//	 * 
//	 * @param name Cadena de la forma "Cond1 Min:"
//	 * @return Entero que identifica el numero de la condición
//	 */
//	/*private int extractindex(String name) {
//		String stringIndex[]= name.split(" ");
//		String index[]=stringIndex[0].split("Cond");
//		int i=new Integer(index[1]).intValue();
//
//		return i;
//	}*/
//
//	/**
//	 * 
//	 * Aqui comporobamos primero que tipo de restricción es y dependiendo de la restricción
//	 * haremos las comporbaciones pertinentes.
//	 * </br>
//	 * Si es de cardinalidad, debemos comprobar que el min<max y que tanto el minimo como 
//	 * el máximo sean de rango menor al de la property original.
//	 * </br>
//	 * Si es de rango de valores, debemos comprobar que el min<max y que tanto el minimo como 
//	 * el máximo sean de rango menor al de la property original.
//	 * 
//	 * @param ido Entero con el ido del individuo original.
//	 * @param idProp Entero que identifica la property original.
//	 * @param type Cadena que identifica el tipo.
//	 * @param value Valor que se de la DataProperty.
//	 * @param group Entero que identifica el grupo de las DataProperties
//	 * @param idoa Entero del individuo sin adaptar.
//	 * @return booleano indicando si pasa la comprobación o no.
//	 * @throws NotFoundException
//	 * @throws IncompatibleValueException 
//	 */
//	private void checkFieldsDataProp(Integer ido, Integer idProp, String type, Value value, Integer group) throws IncompatibleValueException {
//		//TODO Comprobar pq no rula con CMIN y CMAX
//
//		if(type.equals("MIN")){
//			DataAdapter da= tbAdapter.get(ido);
//			Iterator<DataPropertyVirtual> itp=da.getAllDataPropertyVirtual();
//			while(itp.hasNext()){
//				DataPropertyVirtual dp= itp.next();
//				//System.err.println("MIN:"+dp);
//				if(dp.getIdGroup()==group &&  dp.getName().equals("Valor Fijo:")){
//					if (!dp.getValues().isEmpty()){
//						IncompatibleValueException ivE=new IncompatibleValueException("No se puede fijar un rango de valores si esta fijado un valor, elimine primero el valor fijado",dp);
//						ivE.setUserMessage("No se puede fijar un rango de valores si esta fijado un valor, elimine primero el valor fijado");
//						throw ivE;
//					}
//
//				}
//				if(dp.getIdGroup()==group &&  dp.getName().equals("Valor Max:")){
//					if(!dp.getValues().isEmpty()){
//						LinkedList<Value> lv=dp.getValues();
//						Value v=lv.getFirst();
//						if(value instanceof DoubleValue){
//							DoubleValue vmax=(DoubleValue)v;
//							DoubleValue vmin=(DoubleValue)value;
//							if(vmin.getValueMin()>vmax.getValueMin()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor máximo fijado es menor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor máximo fijado es menor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}else if(value instanceof IntValue){
//							IntValue vmax=(IntValue)v;
//							IntValue vmin=(IntValue)value;
//							if(vmin.getValueMin()>vmax.getValueMin()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor máximo fijado es menor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor máximo fijado es menor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}else{
//							UnitValue vmax=(UnitValue)v;
//							UnitValue vmin=(UnitValue)value;
//							if(vmin.getValueMin()>vmax.getValueMin()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor máximo fijado es menor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor máximo fijado es menor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}
//					}
//				}
//			}
//		}
//
//
//		if(type.equals("MAX")){
//			DataAdapter da= tbAdapter.get(ido);
//			Iterator<DataPropertyVirtual> itp=da.getAllDataPropertyVirtual();
//			while(itp.hasNext()){
//				DataPropertyVirtual dp= itp.next();
//				//System.err.println("MAX:"+dp);
//				if(dp.getIdGroup()==group &&  dp.getName().equals("Valor Fijo:")){
//					if (!dp.getValues().isEmpty()){
//						IncompatibleValueException ivE=new IncompatibleValueException("No se puede fijar un rango de valores si esta fijado un valor, elimine primero el valor fijado",dp);
//						ivE.setUserMessage("No se puede fijar un rango de valores si esta fijado un valor, elimine primero el valor fijado");
//						throw ivE;
//					}
//
//				}
//				if(dp.getIdGroup()==group &&  dp.getName().equals("Valor Min:")){
//					if(!dp.getValues().isEmpty()){
//						LinkedList<Value> lv=dp.getValues();
//						Value v=lv.getFirst();
//						if(value instanceof DoubleValue){
//							DoubleValue vmax=(DoubleValue)value;
//							DoubleValue vmin=(DoubleValue)v;
//							if(vmax.getValueMax()<vmin.getValueMax()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor mínimo fijado es mayor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor mínimo fijado es mayor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}else if(value instanceof IntValue){
//							IntValue vmax=(IntValue)value;
//							IntValue vmin=(IntValue)v;
//							if(vmax.getValueMax()<vmin.getValueMax()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor mínimo fijado es mayor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor mínimo fijado es mayor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}else{
//							UnitValue vmax=(UnitValue)value;
//							UnitValue vmin=(UnitValue)v;
//							if(vmax.getValueMax()<vmin.getValueMax()){
//								IncompatibleValueException ivE=new IncompatibleValueException("Error el valor mínimo fijado es mayor al valor que estas introduciendo",dp);
//								ivE.setUserMessage("Error el valor mínimo fijado es mayor al valor que estas introduciendo");
//								throw ivE;
//							}
//						}
//					}
//				}
//			}
//		}
//
//		if(type.equals("FIJO")){
//			DataAdapter da= tbAdapter.get(ido);
//			Iterator<DataPropertyVirtual> itp=da.getAllDataPropertyVirtual();
//			while(itp.hasNext()){
//				DataPropertyVirtual dp= itp.next();
//				//System.err.println("FIJO:"+dp);
//				if(dp.getIdGroup()==group && (dp.getName().equals("Valor Min:") || dp.getName().equals("Valor Max:"))){
//					if (!dp.getValues().isEmpty()){
//						IncompatibleValueException ivE=new IncompatibleValueException("No se puede fijar un valor si estas fijando un rango, elimine primero el rango de valores",dp);
//						ivE.setUserMessage("No se puede fijar un valor si estas fijando un rango, elimine primero el rango de valores");
//						throw ivE;
//					}
//
//				}
//
//			}
//		}
//
//
//		//Reestructurar el group para k no se le llame con el getClassName
//
////		if(type.equals("CMIN")){
////		LinkedList<Property> lpo=tbUnAdapter.get(idoa);
////		if(lpo!=null && !lpo.isEmpty()){
////		Iterator<Property> itpda=lpo.iterator();
////		Property pda=itpda.next();
////		if(pda instanceof ObjectProperty){
////		ObjectProperty opv=(ObjectProperty)pda;
////		if(opv.getName().equals(group)){
////		if(opv.getCardMax()!=null){
//
////		if(((IntValue)value).getValueMin()>opv.getCardMax()){
////		result=false;
////		return result;
//
////		}
////		}
//
//
////		}
////		}
////		}
////		}else if(type.equals("CMAX")){
////		LinkedList<Property> lpo=tbUnAdapter.get(idoa);
////		if(lpo!=null && !lpo.isEmpty()){
////		Iterator<Property> itpda=lpo.iterator();
////		Property pda=itpda.next();
////		if(pda instanceof ObjectProperty){
////		ObjectProperty opv=(ObjectProperty)pda;
////		if(opv.getName().equals(group)){
////		if(opv.getCardMin()!=null){
//
////		if(((IntValue)value).getValueMax()<opv.getCardMin()){
////		result=false;
////		return result;
//
////		}
////		}
//
//
////		}
////		}
////		}
////		}
////		if(value instanceof IntValue || value instanceof DoubleValue || value instanceof UnitValue ){
//
//
////		Property p=kb.getProperty(ido, idProp, userRol, user, usertask, session);
//
//
////		if(type.equals("MIN")){
////		if(i>=0){
////		if(!((DataProperty)p).getEnumList().isEmpty()){
////		LinkedList<DataValue> lv=((DataProperty)p).getEnumList();
////		if(((DataProperty)p).getDataType()==Constants.IDTO_INT){
////		IntValue ivo=(IntValue)lv.get(i-1);
////		IntValue iv=(IntValue)value;
////		if(ivo!=null && ivo.getValueMin()!=null && ivo.getValueMin()>iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}else if(((DataProperty)p).getDataType()==Constants.IDTO_DOUBLE){
////		DoubleValue ivo=(DoubleValue)lv.get(i-1);
////		DoubleValue iv=(DoubleValue)value;
////		if(ivo!=null && ivo.getValueMin()!=null && ivo.getValueMin()>iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}else{
////		UnitValue ivo=(UnitValue)lv.get(i-1);
////		UnitValue iv=(UnitValue)value;
////		if(ivo!=null && ivo.getValueMin()!=null && ivo.getValueMin()>iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}
////		}
////		LinkedList<Property> lp=tbUnAdapter.get(idoa);
////		if(lp!=null && !lp.isEmpty()){
////		Iterator<Property> itpda=lp.iterator();
////		while (itpda.hasNext()){
////		Property pda=itpda.next();
////		if(pda instanceof DataProperty){
////		DataProperty dpv=(DataProperty)pda;
////		if(dpv.getName().equals(group)){
////		if(!dpv.getEnumList().isEmpty()){
////		LinkedList<DataValue> dlv=dpv.getEnumList();
//
////		if(dpv.getDataType()==Constants.IDTO_INT){
////		IntValue ivdpv=(IntValue)dlv.get(i-1);
////		IntValue iv=(IntValue)value;
////		if(ivdpv.getValueMax()<iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}else if(dpv.getDataType()==Constants.IDTO_DOUBLE){
////		DoubleValue ivdpv=(DoubleValue)dlv.get(i-1);
////		DoubleValue iv=(DoubleValue)value;
////		if(ivdpv.getValueMax()<iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}else{
////		UnitValue ivdpv=(UnitValue)dlv.get(i-1);
////		UnitValue iv=(UnitValue)value;
////		if(ivdpv.getValueMax()<iv.getValueMin()){
////		result=false;
////		return result;
////		}
////		}
//
////		}
////		}
////		}
////		}
////		}
//
////		}
//
////		}else if(type.equals("MAX")){
////		if(i>=0){
////		if(!((DataProperty)p).getEnumList().isEmpty()){
////		LinkedList<DataValue> lv=((DataProperty)p).getEnumList();
////		if(((DataProperty)p).getDataType()==Constants.IDTO_INT){
////		IntValue ivo=(IntValue)lv.get(i-1);
////		IntValue iv=(IntValue)value;
////		if(ivo!=null && ivo.getValueMax()!=null && ivo.getValueMax()<iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}else if(((DataProperty)p).getDataType()==Constants.IDTO_DOUBLE){
////		DoubleValue ivo=(DoubleValue)lv.get(i-1);
////		DoubleValue iv=(DoubleValue)value;
////		if(ivo!=null && ivo.getValueMax()!=null && ivo.getValueMax()<iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}else{
////		UnitValue ivo=(UnitValue)lv.get(i-1);
////		UnitValue iv=(UnitValue)value;
////		if(ivo!=null && ivo.getValueMax()!=null && ivo.getValueMax()<iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}
//
////		}
////		LinkedList<Property> lp=tbUnAdapter.get(idoa);
////		if(lp!=null && !lp.isEmpty()){
////		Iterator<Property> itpda=lp.iterator();
////		while (itpda.hasNext()){
////		Property pda=itpda.next();
////		if(pda instanceof DataProperty){
////		DataProperty dpv=(DataProperty)pda;
////		if(dpv.getName().equals(group)){
////		if(!dpv.getEnumList().isEmpty()){
////		LinkedList<DataValue> dlv=dpv.getEnumList();
////		if(dpv.getDataType()==Constants.IDTO_INT){
////		IntValue ivdpv=(IntValue)dlv.get(i-1);
////		IntValue iv=(IntValue)value;
////		if(ivdpv.getValueMin()>iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}else if(dpv.getDataType()==Constants.IDTO_DOUBLE){
////		DoubleValue ivdpv=(DoubleValue)dlv.get(i-1);
////		DoubleValue iv=(DoubleValue)value;
////		if(ivdpv.getValueMin()>iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}else{
////		UnitValue ivdpv=(UnitValue)dlv.get(i-1);
////		UnitValue iv=(UnitValue)value;
////		if(ivdpv.getValueMin()>iv.getValueMax()){
////		result=false;
////		return result;
////		}
////		}
//
////		}
////		}
////		}
////		}
////		}
////		}
////		}
//
////		}
//
//
//
//	}
//
//
//
//
//
//	/**
//	 * 
//	 * Función que crea el XML con la información de la BuisinessRestriction para pasarselo
//	 * al server el cual sera el encargado del almacenarlo en la base de datos.
//	 * 
//	 * @param currentAdapteIdo Entero que identifica al individuo que se a adaptado.
//	 * @return Devuelve un XML.
//	 */
//	/*private Element createXMLBC(Integer currentAdapteIdo) {
//		Element result=null;
//		result=new Element("BUSINESSCLASSES");
//
//		LinkedList<Property> lp=tbUnAdapter.get(currentAdapteIdo);
//		Iterator<Property> itp=lp.iterator();
//		while(itp.hasNext()){
//			Property p=itp.next();
//
//			LinkedList<FactInstance> lpf=KnowledgeAdapter.traslatePropertyDefToInstanceFacts(p);
//			Iterator<FactInstance> itpf=lpf.iterator();	
//			while(itpf.hasNext()){
//				Element prop= new Element("BUSINESSCLASS");
//				FactInstance pf=itpf.next();
//				if(pf.getCLSREL() != null && !pf.getCLSREL().equals("null"))
//					prop.setAttribute("CLSREL",pf.getCLSREL().toString());
//
//				if(pf.getCLSRELB()!= null && !pf.getCLSRELB().equals("null"))
//					prop.setAttribute("CLSRELB",pf.getCLSRELB().toString());
//
//				if(pf.getComment() != null && !pf.getComment().equals("null"))
//					prop.setAttribute("COMMENT",pf.getComment().toString());
//
//				if(pf.getIDO()!= null && !pf.getIDO().equals("null"))
//					prop.setAttribute("IDO",pf.getIDO().toString());
//
//				if(pf.getIDOREL()!= null && !pf.getIDOREL().equals("null"))
//					prop.setAttribute("IDOREL",pf.getIDOREL().toString());
//
//				if(pf.getIDORELB()!= null && !pf.getIDORELB().equals("null"))
//					prop.setAttribute("IDORELB",pf.getIDORELB().toString());
//
//				if(pf.getIDTO()!= null && !pf.getIDTO().equals("null"))
//					prop.setAttribute("IDTO",pf.getIDTO().toString());
//
//				if(pf.getNAME()!= null && !pf.getNAME().equals("null"))
//					prop.setAttribute("NAME",pf.getNAME().toString());
//
//				if(pf.getPROP()!= null && !pf.getPROP().equals("null"))
//					prop.setAttribute("PROP",pf.getPROP().toString());
//
//				if(pf.getQMAX()!= null && !pf.getQMAX().equals("null"))
//					prop.setAttribute("QMAX",pf.getQMAX().toString());
//
//				if(pf.getQMIN()!= null && !pf.getQMIN().equals("null"))
//					prop.setAttribute("QMIN",pf.getQMIN().toString());
//
//				if(pf.getROL()!= null && !pf.getROL().equals("null"))
//					prop.setAttribute("ROL",pf.getROL().toString());
//
//				if(pf.getROLB()!= null && !pf.getROLB().equals("null"))
//					prop.setAttribute("ROLB",pf.getROLB().toString());
//
//				if(pf.getVALUE()!= null && !pf.getVALUE().equals("null"))
//					prop.setAttribute("VALUE",pf.getVALUE().toString());
//
//				if(pf.getVALUECLS()!= null && !pf.getVALUECLS().equals("null"))
//					prop.setAttribute("VALUECLS",pf.getVALUECLS().toString());
//
//				result.addContent(prop);
//			}
//
//		}
//		return result;
//		return null;
//	}*/
//
//
//
//
//	/**
//	 * Metodo que indica al individuo de algun cambio en alguna de sus properties.
//	 * Sacamos la property del DataAdapater, y le cambiamos su value por el nuevo que se nos
//	 * pasa por parámetro.
//	 */
//
//
//	/**
//	 * Delegamos la funcionalidad al motor. {@link DocDataModel#loadMetaData()}.
//	 * @throws IncoherenceInMotorException 
//	 * @throws ApplicationException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 */
//	public void loadMetaData() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException {
//
//		kb.loadMetaData();
//	}
//
//	/* TODO:
//	 * Commit sin hacer ya que no funcionaban las sessiones, hay echo algo pero dudo mucho
//	 * que funcione
//	 */
//	//public boolean commit(Session s) throws  NotFoundException {
////	TODO REVISAR:
//	/*Integer idSessPadre=session.getIDMadre();
//		Session sessPadre=SessionController.getInstance().getSession(idSessPadre);
//		Integer currentAdapteIdo=tbSessionIdo.get(session.getID());
//		//instance i=createInstance(currentAdapteIdo);
//		//TODO:Crear Facts
//		Element e=createXMLBC(currentAdapteIdo); 
//		DataAdapter currentDA=tbAdapter.get(currentAdapteIdo);
//		Integer currentAdapteIdto=currentDA.getIdtoTemp();
//		Integer idProp=tbOPIdProp.get(currentAdapteIdto);
//		Integer fatherAdapteIdo=tbSessionIdo.get(idSessPadre);
//		DataAdapter fatherDA=tbAdapter.get(fatherAdapteIdo);
//		Integer fatherAdapteIdto=fatherDA.getIdtoTemp();
//		System.out.println(e.toString());
//		contextAction ca=new contextAction(message.MSG_OBJECT_TRAN);
//		ca.setDataType(message.DATA_META);
//		ca.setContent(e);*/
//
//
//
//
////	if(p instanceof DataPropertyVirtual){
////	DataPropertyVirtual dp= (DataPropertyVirtual)p;
////	if(type.equals("MIN")){
////	Property puna=getPropertyUnAdapte(idPropO);
////	DataProperty dpuna=(DataProperty)puna;
////	if(dpuna==null){
////	Property itp=kb.getProperty(idtoO, idPropO, userRol, user, usertask,s);
////	DataProperty po= (DataProperty)itp;
////	dpuna=(DataProperty)po.clone();
////	dpuna.setEnumList(new LinkedList<DataValue>());
////	dpuna.setIdo(ido);
////	}
////	DataValue ivold;
////	DataValue ivnew;
////	if(dp.getDataType()==Constants.IDTO_INT){
////	ivnew=new IntValue();
////	ivold=new IntValue();
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	ivnew=new DoubleValue();
////	ivold=new DoubleValue();
////	}else{
////	ivnew=new UnitValue();
////	ivold=new UnitValue();
////	}
////	/*IntValue ivrestriction=new IntValue();
////	if(!dp.getValueList().isEmpty()){
////	ivrestriction=(IntValue)dp.getValueList().getFirst();
////	}*/
//
//
////	LinkedList<DataValue> ldv=null;
////	//int i=extractindex(dp.getName());
////	if(!dpuna.getEnumList().isEmpty()){
////	if(dp.getDataType()==Constants.IDTO_INT){
////	ivold=(IntValue)dpuna.getEnumList().getFirst();
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	ivold=(DoubleValue)dpuna.getEnumList().getFirst();
////	}else{
////	ivold=(UnitValue)dpuna.getEnumList().getFirst();
////	}
//
////	ldv=dpuna.getEnumList();
////	ldv.removeFirst();
////	}else{
////	ldv= new LinkedList<DataValue>();
////	if(dp.getDataType()==Constants.IDTO_INT){
////	((IntValue)ivold).setValueMax(null);
////	((IntValue)ivold).setValueMin(null);
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	((DoubleValue)ivold).setValueMax(null);
////	((DoubleValue)ivold).setValueMin(null);
////	}else{
////	((UnitValue)ivold).setValueMax(null);
////	((UnitValue)ivold).setValueMin(null);
////	}
//
////	}
////	if(dp.getDataType()==Constants.IDTO_INT){
////	((IntValue)ivnew).setValueMin(((IntValue)newValue).getValueMin());
////	((IntValue)ivnew).setValueMax(((IntValue)ivold).getValueMax());
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	((DoubleValue)ivnew).setValueMin(((DoubleValue)newValue).getValueMin());
////	((DoubleValue)ivnew).setValueMax(((DoubleValue)ivold).getValueMax());
////	}else{
////	((UnitValue)ivnew).setValueMin(((UnitValue)newValue).getValueMin());
////	((UnitValue)ivnew).setValueMax(((UnitValue)ivold).getValueMax());
////	}
//
//
////	ldv.add(ivnew);
////	dpuna.setEnumList(ldv);	
////	dpuna.setValues(new LinkedList<Value>());
////	boolean pass=checkFieldsDataProp(idoO,idPropO,type,ivnew, dp.getNameGroup(),ido);
////	if(!pass){
////	throw new CardinalityExceedException();
////	}else{
////	System.out.println("Property de Clase: "+dpuna.toString());
////	setPropertyUnAdapte(ido,dpuna);
////	}
////	}
////	else if(type.equals("MAX")){
////	Property puna=getPropertyUnAdapte(idPropO);
////	DataProperty dpuna=(DataProperty)puna;
////	if(dpuna==null){
////	Property itp=kb.getProperty(idtoO, idPropO, userRol, user, usertask,s);
////	DataProperty po= (DataProperty)itp;
////	dpuna=(DataProperty)po.clone();
////	dpuna.setEnumList(new LinkedList<DataValue>());
////	dpuna.setIdo(ido);
////	}
//
////	/*IntValue ivrestriction=new IntValue();
////	if(!dp.getValueList().isEmpty()){
////	ivrestriction=(IntValue)dp.getValueList().getFirst();
////	}*/
//
//
////	DataValue ivold;
////	DataValue ivnew;
////	if(dp.getDataType()==Constants.IDTO_INT){
////	ivnew=new IntValue();
////	ivold=new IntValue();
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	ivnew=new DoubleValue();
////	ivold=new DoubleValue();
////	}else{
////	ivnew=new UnitValue();
////	ivold=new UnitValue();
////	}
////	LinkedList<DataValue> ldv=null;
////	//int i=extractindex(dp.getName());
////	if(!dpuna.getEnumList().isEmpty()){
////	if(dp.getDataType()==Constants.IDTO_INT){
////	ivold=(IntValue)dpuna.getEnumList().getFirst();
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	ivold=(DoubleValue)dpuna.getEnumList().getFirst();
////	}else{
////	ivold=(UnitValue)dpuna.getEnumList().getFirst();
////	}
//
////	ldv=dpuna.getEnumList();
////	ldv.removeFirst();
////	}else{
////	ldv= new LinkedList<DataValue>();
////	if(dp.getDataType()==Constants.IDTO_INT){
////	((IntValue)ivold).setValueMax(null);
////	((IntValue)ivold).setValueMin(null);
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	((DoubleValue)ivold).setValueMax(null);
////	((DoubleValue)ivold).setValueMin(null);
////	}else{
////	((UnitValue)ivold).setValueMax(null);
////	((UnitValue)ivold).setValueMin(null);
////	}
//
////	}
//
////	if(dp.getDataType()==Constants.IDTO_INT){
////	((IntValue)ivnew).setValueMin(((IntValue)ivold).getValueMin());
////	((IntValue)ivnew).setValueMax(((IntValue)newValue).getValueMax());
//
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	((DoubleValue)ivnew).setValueMin(((DoubleValue)ivold).getValueMin());
////	((DoubleValue)ivnew).setValueMax(((DoubleValue)newValue).getValueMax());
//
////	}else{
////	((UnitValue)ivnew).setValueMin(((UnitValue)ivold).getValueMin());
////	((UnitValue)ivnew).setValueMax(((UnitValue)newValue).getValueMax());
//
////	}
//
//
////	ldv.add(ivnew);
////	dpuna.setEnumList(ldv);
////	dpuna.setValues(new LinkedList<Value>());
////	boolean pass=checkFieldsDataProp(idoO,idPropO,type,ivnew,dp.getNameGroup(),ido);
////	if(!pass){
////	throw new CardinalityExceedException();
////	}else{
////	System.out.println("Property de Clase: "+dpuna.toString());
////	setPropertyUnAdapte(ido,dpuna);
////	}
////	}else if(type.equals("FIJO")){
////	Property puna=getPropertyUnAdapte(idPropO);
////	DataProperty dpuna=(DataProperty)puna;
////	if(dpuna==null){
////	Property itp=kb.getProperty(idtoO, idPropO, userRol, user, usertask,s);
////	DataProperty po= (DataProperty)itp;
////	dpuna=(DataProperty)po.clone();
////	//dpuna.setEnumList(new LinkedList<DataValue>());
////	dpuna.setValues(new LinkedList<Value>());
////	dpuna.setValuesFixed(false);
//
////	dpuna.setIdo(ido);
//
////	}
////	access a=dpuna.getTypeAccess();
////	a.setSetAccess(false);
////	dpuna.setTypeAccess(a);
//
////	DataValue ivold;
////	DataValue ivnew;
////	if(dp.getDataType()==Constants.IDTO_INT){
////	ivnew=(IntValue)newValue;
//
////	}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
////	ivnew=(DoubleValue)newValue;
////	}else{
////	ivnew=(UnitValue)newValue;
////	}
////	LinkedList<Value> ldv=new LinkedList<Value>();
//
////	ldv.add(ivnew);
////	dpuna.setValues(ldv);
////	dpuna.setValuesFixed(true);
////	boolean pass=checkFieldsDataProp(idoO,idPropO,type,ivnew,dp.getNameGroup(),ido);
////	if(!pass){
////	throw new CardinalityExceedException();
////	}else{
////	System.out.println("Property de Clase: "+dpuna.toString());
////	setPropertyUnAdapte(ido,dpuna);
////	}
////	}
//
////	}else{
////	ObjectPropertyVirtual op=(ObjectPropertyVirtual)p;
////	if(type.equals("NORMAL")){
////	Property puna=getPropertyUnAdapte(idPropO);
////	ObjectProperty opuna=(ObjectProperty)puna;
////	if(opuna==null){
////	Property itp=kb.getProperty(idtoO, idPropO, userRol, user, usertask,s);
////	ObjectProperty po= (ObjectProperty)itp;
////	opuna=(ObjectProperty)po.clone();
//
////	opuna.setValues(new LinkedList<Value>());
//
////	opuna.setIdo(ido);
////	}
////	/*LinkedList<Integer> lrangonew= opuna.getRangoList();
////	LinkedList<Value> lv=op.getValues();
////	//LinkedList<ObjectValue> lf=op.getFilterList();
////	Iterator<Value> itov=lv.iterator();*/
//
////	/*while(itov.hasNext()){
////	Value ov=itov.next();
////	lrangonew.add(((ObjectValue)ov).getValueCls());
////	}
//
////	opuna.setRangoList(lrangonew);*/
////	LinkedList<Value> opvalue=opuna.getValues();
////	if (oldValue==null){
////	opvalue.add(newValue);	
////	}else{
////	opvalue.remove(oldValue);
////	opvalue.add(newValue);
////	}
//
////	opuna.setValues(opvalue);
////	//opuna.setFilterList(lf);
////	boolean pass=checkFieldsObjectProp(idoO,idPropO,type);
////	if(!pass){
////	throw new OperationNotPermitedException();
////	}else{
////	System.out.println("Property de Clase: "+opuna.toString());
////	setPropertyUnAdapte(ido,opuna);
////	}
////	}
////	}
//
//
//
//	/*try {
//			server.serverTransitionObject(new contextAction(bns,user,userRol,0,0,usertask,message.ACTION_MODIFY,i));
//
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (DataErrorException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (JDOMException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (SystemException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	 */
//	/*if(!principalSess.equals(session.getID())){
//			ObjectValue ov= new ObjectValue();
//			ov.setValue(currentAdapteIdo);
//			ov.setValueCls(currentAdapteIdto);
//			sessPadre.changeValue(fatherAdapteIdo, fatherAdapteIdto, idProp, currentAdapteIdto, ov, action.SET);
//		}*/
//	//imprimeUnAdapter();
//	//return false;
//	//}
//
//	/* TODO:
//	 * rollBack sin hacer ya que no funcionaban las sessiones, hay echo algo pero dudo mucho
//	 * que funcione
//	 */
//
//
//	public void addHistoryDDBBListener(IHistoryDDBBListener historyDDBBListener) {
//		this.historyDDBBListeners.add(historyDDBBListener);
//		kb.addHistoryDDBBListener(historyDDBBListener);
//	}
//
//	public void deleteObject(int id, Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//		//   
//		access aclass=null;
//
//		Iterator<Integer> itidto=kb.getSuperior(id);
//		if (itidto.hasNext()) {
//			Integer idto=itidto.next();
//			//ArrayList<Property> list= new ArrayList<Property>();
//			aclass = kb.getAccessOverObject(idto, userRol, user, usertask);
//			if(aclass.getSpecializeAccess()){
//				new FactInstance(idto,id,Constants.IdPROP_OBJECTDELETED,null,Constants.IDTO_BOOLEAN,null,null,null,null);
//			}else{
//				kb.deleteObject(id, sessionPadre);
//			}
//		}else 
//			kb.deleteObject(id, sessionPadre);
//	}
//
//	public Integer getAtributteGroup(int idProp) {
//		// TODO revisar
//		if(tbIdPropIdo.containsKey(idProp)){
//			DataAdapter da= tbAdapter.get(tbIdPropIdo.get(idProp));
//			Property pr=da.getProperty(idProp);
//			if(pr instanceof DataPropertyVirtual){
//				DataPropertyVirtual p = (DataPropertyVirtual)pr;
//				return p.getIdGroup();
//			}
//			else if(pr instanceof ObjectPropertyVirtual){
//				ObjectPropertyVirtual p = (ObjectPropertyVirtual)pr;
//				return p.getIdGroup();
//			}
//			return 0;
//		}else{
//			return kb.getAtributteGroup(idProp);
//		}
//	}
//
//	public Category getCategory(int idProp) throws NotFoundException {
//		//   
//		return kb.getCategory(idProp);
//	}
//
//	public String getClassName(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//		//TODO REVISAR:
//		//System.out.println("getClassName");
//
//		if(tbGroupNameG.containsKey(id)){
//			return tbGroupNameG.get(id);
//		}else if(tbAdapter.containsKey(id)){
//			Integer idto=null;
//			DataAdapter da= tbAdapter.get(id);
//			Property p= da.getProperty(Constants.IdPROP_RDN);
//			idto=da.getOriginalIdtoClass(p);
//			return getClassName(idto);					
//		}else{
//			return kb.getClassName(id);
//		}
//	}
//	/** 
//	 * Comprobamos si dicho individuo es un individuo adaptado o no, si lo es, busca en su DataAdapter la property RDN, y saca de hay 
//	 * la clase a donde pertenece.
//	 * Si no es adaptado, delega la función al motor. {@link DocDataModel#getClassOf(Integer)}.
//	 * 
//	 * @param ido Entero del individuo.
//	 * @return Entero que identifica a la clase a la que pertenece el individuo.
//	 * @throws ApplicationException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws IncoherenceInMotorException 
//	 * @throws NotFoundException 
//	 */
//	public Integer getClassOf(int ido) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException {
////		TODO REVISAR:
//		if(tbAdapter.containsKey(ido)){
//			Integer idto=null;
//			DataAdapter da= tbAdapter.get(ido);
//			Property p= da.getProperty(Constants.IdPROP_RDN);
//			idto=da.getOriginalIdtoClass(p);
//			return idto;					
//		}
//		return kb.getClassOf(ido);
//	}
//
//	public FactProp getFactProp(int idProp) {
//		//   
//		return kb.getFactProp(idProp);
//	}
//
//	public Iterator<Integer> getIndividualsOfLevel(int idto, int level) {
//		//   
//		return kb.getIndividualsOfLevel(idto, level);
//	}
//
//	public ArrayList<OrderProperty> getOrderProperties() {
//		//   
//		return kb.getOrderProperties();
//	}
//	/**
//	 * Comprobamos inicialmente si el ido que nos pasan por parametro esta ya adaptado, si es así, extraemos la property del DataAdapter asociado
//	 * , sino delegamos esta funcionalidad al motor. {@link DocDataModel#getProperty(int, Integer, int, Integer, Integer, Integer, Integer, String, Integer)}.
//	 * 
//	 * 
//	 * @param ido Ido del individuo.
//	 * @param idProp id de la property.
//	 * @param userRol Entero que identifica el userRol.
//	 * @param user String que contiene el usuario logeado en este momento.
//	 * @param usertask Entero que contiene la userTask.
//	 * @param s Session.
//	 * @return Property de dicho individuo.
//	 * @throws ApplicationException 
//	 * @throws CardinalityExceedException 
//	 * @throws IncompatibleValueException 
//	 * @throws IncoherenceInMotorException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 */
//
//	public Property getProperty(int id, int idProp, Integer userRol, String user, Integer usertask, Session s) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
////		TODO REVISAR:
//
//		if(tbAdapter.containsKey(id)){
//			DataAdapter da= tbAdapter.get(id);
//			return da.getProperty(idProp);
//		}
//		return kb.getProperty(id, idProp,userRol, user, usertask,s);
//	}
//
//
//
//	public Integer getPropertyInverse(int idProp) {
//
//		return kb.getPropertyInverse(idProp);
//	}
//	/**
//	 * Comprobamos si esa property es adaptada, si esta adaptada la extraemos del DataAdapter
//	 * y devolvemos su nombre mediante {@link Property#getName()}. Si no esta adaptada,
//	 * delegamos la funcionalidad al motor.
//	 * 
//	 * @param idProp Entero que identifica la property.
//	 * @return Cadena con el nombre de la property.
//	 */
//	public String getPropertyName(int idProp) throws NotFoundException {
////		TODO REVISAR:
//		if(tbIdPropIdo.containsKey(idProp)){
//			Integer ido= tbIdPropIdo.get(idProp);
//			DataAdapter da= tbAdapter.get(ido);
//			Property p= da.getProperty(idProp);
//			return p.getName();
//		}
//		return kb.getPropertyName(idProp);
//	}
//
//	public String getRdn(int ido) {
//
//		return kb.getRdn(ido);
//	}
//
//	public Iterator<Integer> getSpecialized(int id, Integer userRol, String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException {
//
//		return kb.getSpecialized(id, userRol, user, usertask, session);
//	}
//
//	public ArrayList<Integer> getSpecializedFilters(int ido, Integer userRol, String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//
//		return kb.getSpecializedFilters(ido, userRol, user, usertask, session);
//	}
//
//	public Iterator<Integer> getSuperior(int idto) throws NotFoundException {
//
//		return kb.getSuperior(idto);
//	}
//
//	public instance getTreeObject(int id, Integer userRol, String user, Integer userTask, Session sess) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//
//		return kb.getTreeObject(id, userRol, user, userTask, sess);
//	}
//
//	public HashMap<Integer, ArrayList<UserAccess>> getUsertaskOperationOver(int id, String user) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//		// TODO Aqui se trata la botonera de la tabla
//		return kb.getUsertaskOperationOver(id, user);
//	}
//
//	public boolean isDataProperty(int idProp) {
//
//		if(tbIdPropVIdPropO.containsKey(idProp)){
//			//System.err.println("Es DataProperty entra:"+kb.isDataProperty(tbIdPropVIdPropO.get(idProp)));
//			return kb.isDataProperty(tbIdPropVIdPropO.get(idProp));
//		}
//		System.err.println("Es DataProperty  :"+kb.isDataProperty(idProp));
//		return kb.isDataProperty(idProp);
//	}
//
//	public boolean isIDClass(int id) {
//
//		return kb.isIDClass(id);
//	}
//
//	public boolean isObjectProperty(int idProp) {
//		// TODO comprobar si funciona cuando se le pregunta por un objectproperyvirtual
//		if(tbIdPropVIdPropO.containsKey(idProp)){
//			//System.err.println("Es ObjectProperty entra :"+kb.isObjectProperty(tbIdPropVIdPropO.get(idProp)));
//			return kb.isObjectProperty(tbIdPropVIdPropO.get(idProp));
//		}
//		//System.err.println("Es ObjectProperty:"+kb.isDataProperty(idProp));
//		return kb.isObjectProperty(idProp);
//	}
//
//	public boolean isUnit(int cls) throws NotFoundException {
//
//		return kb.isUnit(cls);
//	}
//
//
//
//	public void setAsigned(IAsigned asigned) {
//
//		kb.setAsigned(asigned);
//
//	}
//	/**
//	 * Si se trata de un individuo adaptado, realizaremos lo que es toda la comprobación, y
//	 * desadaptación, sino lo delegemos a motor. {@link DocDataModel#setValue(int, int, Integer, Integer, Value, Value, Integer, String, Integer, Session).
//	 * </br></br>
//	 * Debemos diferenciar dos casos distintos, si la property es una DataPropertyVirtual, o bien, es un ObjectPropertyVirtual:</br>
//	 * Si es una DataPropertyVirtual:</br>
//	 * 1. Miramos el tipo de restricción que se a realizado, si es de tipo "MAX", "MIN", es decir, restricciones sobre rango
//	 * de valores de datos como Entero, Double, Unidad...Debemos coger el valuelist de la porperty que restringe el valor
//	 * maximo del rango y el valuelist de la property que restringe el valor minimo del rango y formar un DataValue (dependiendo
//	 * de si es INT,DOUBLE,UNIT será IntValue, DoubleValue, UnitValue) y insertarlo en el enumlist de la property original
//	 * Posteriormente llamamos a {@link #setPropertyUnAdapte(Integer, Property)} con la property original y el enumlist de esta
//	 * actualizado, antes de llamar a dicha función, habremos invocado esta otra {@link #checkFieldsDataProp(Integer, Integer, String, Value, int, String, int)},
//	 * para comprobar si los datos son correctos.
//	 * </br>
//	 * 2. Miramos si el tiepo de restrcción es de tipo "CMAX", "CMIN", que es restricción sobre cardinalidad minima y máxima.
//	 * Introducimos en la ObjectProperty original el newValue, con lo que si es tipo "CMIN" el newValue lo almacenamos en 
//	 * la cardinalidad minima ({@link ObjectProperty#setCardMin(Integer)}, y si es de tipo "CMAX", el newValue, lo almacenamos
//	 * en la cardinalidad máxima ({@link ObjectProperty#setCardMax(Integer)}. Posteriormente llamamos
//	 * a {@link #checkFieldsDataProp(Integer, Integer, String, Value, int, String, int)}, y si lo pasa, llamamos a {@link #setPropertyUnAdapte(Integer, Property)}
//	 * para almacenar la property desadaptada.
//	 * </br></br>
//	 * Si es una ObjectPropertyVirtual:</br>
//	 * Extraemos del valueList los valueClass de a las clases que apunta, posteriormente estos valueClass
//	 * se almacenan en un LinkedList para meterlos en el rangoList de la ObjectProperty original.</br>
//	 * A continuación, llamaremos a {@link #checkFieldsObjectProp(Integer, Integer, String, LinkedList)}, si pasa
//	 * la comprobación, lo insertamos en el mapa de property desadaptadas mediante la función {@link #setPropertyUnAdapte(Integer, Property)}.
//	 * @throws ApplicationException 
//	 * @throws IncoherenceInMotorException 
//	 * @throws InstanceLockedException 
//	 * @throws CommunicationException 
//	 * @throws RemoteSystemException 
//	 * @throws SystemException 
//	 * @throws ParseException 
//	 * @throws JDOMException 
//	 * @throws DataErrorException 
//	 * @throws NamingException 
//	 * @throws SQLException 
//	 */
//
//	public void setValueBR(int ido, int idProp, Value oldValue, Value newValue, Integer userRol, String user, Integer usertask, Session s) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//
//		if(tbAdapter.containsKey(ido)){
//			DataAdapter da= tbAdapter.get(ido);
//			Property p =da.getProperty(idProp);
//			String type= da.getType(p);
//			if (p!=null){
//				if(p instanceof DataPropertyVirtual){
//
//					DataPropertyVirtual dp=(DataPropertyVirtual)p;
//					LinkedList<Value> lv=dp.getValues();
//					if(oldValue!=null){
//						lv.remove(oldValue);
//						if(newValue!=null){
//							lv.add(newValue);
//						}
//						//dp.setValues(lv);
//					}else{
//						/*if(lv!=null){
//							lv.add(newValue);
//						}else{*/
//						//lv=new LinkedList<Value>();
//						lv.add(newValue);
//						//}
//						//dp.setValues(lv);
//					}
//					if(newValue!=null){
//						try{
//							checkFieldsDataProp(ido, idProp, type, newValue, dp.getIdGroup());
//						}catch(IncompatibleValueException ex){
//							lv.remove(newValue);
//							throw ex;
//						}
//					}
//					System.out.println("setVBR:"+dp.toString());
//
//
//				}else{
//					ObjectPropertyVirtual op=(ObjectPropertyVirtual)p;
//					LinkedList<Value> lv=op.getValues();
//					if(oldValue!=null){
//						lv.remove(oldValue);
//						if(newValue!=null){
//							lv.add(newValue);
//						}
//						//op.setValues(lv);
//					}else{
//						/*if(lv!=null){
//							lv.add(newValue);
//						}else{*/
//						//lv=new LinkedList<Value>();
//						lv.add(newValue);
//						/*}*/
//						//op.setValues(lv);
//					}
//					/*if(type.equals("NORMAL")){
//						s.addIchangeProperty(this.session);
//					}*/
//					if(newValue!=null){
//						try{
//							checkFieldsObjectProp(ido, idProp, newValue, type);
//						}catch(CardinalityExceedException ex){
//							lv.remove(newValue);
//							throw ex;
//						}
//					}
//					System.out.println("setVBR:"+op.toString());
//				}
//			}
//
//		}else{
//			System.out.println("SetValue para motor:");
//			kb.setValue(ido,idProp, oldValue, newValue, userRol, user, usertask, s);
//		}
//
//	}
//
//	public void addOrderPropertyList(ArrayList<OrderProperty> orderPropertyList) {
//
//		orderProperties.addOrderPropertyList(this.orderPropertyList);
//	}
//
//	public void buildOrderForm(ArrayList<Property> list) {
//
//		orderProperties.addOrderPropertyList(this.orderPropertyList);
//		orderProperties.buildOrderForm(list);
//		orderProperties.removeOrderPropertyList(orderPropertyList);
//	}
//
//	public int getOrder(int idProp, Integer idGroup) {
//
//		return orderProperties.getOrder(idProp, idGroup);
//	}
//
//	public int getOrderGroup(int idGroup) {
//
//		return idGroup;
//	}
//
//	public int getPriority(Property property, int idGroup) {
//
//		return orderProperties.getPriority(property, idGroup);
//	}
//
//	public void removeOrderPropertyList(ArrayList<OrderProperty> orderPropertyList) {
//
//		orderProperties.removeOrderPropertyList(orderPropertyList);
//
//	}
//
//	public void setValue(int ido, int idProp, Value oldValue, Value newValue, Integer userRol, String user, Integer usertask, Session s) throws CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//
////		TODO: Falta Exclusion mutua
//		//System.out.println("DENTRO DE SETVALUE PUBLICO");
//		setValueBR(ido, idProp, oldValue, newValue, userRol, user, usertask, s);
//		//IKnowledgeBaseInfo ik=SessionController.getInstance().getDDM();
//		//SessionController.getInstance().setDDM(this);
//		DefaultSession sess = m_kba.createDefaultSession(s,s.getUtask(),Session.ADAPTER_SPECIALIZE_SESSION);
//
//
//		SessionController.getInstance().setActual(sess);
//		try {
//
//
////			ArrayList<IChangePropertyListener> sessions = ((DefaultSession)s).getChangeProp(); 
////			Iterator<IChangePropertyListener> itrs=sessions.iterator();
////			while(itrs.hasNext()){
////			IChangePropertyListener se=itrs.next();
//
//
//			if(tbAdapter.containsKey(ido)){
//				DataAdapter da= tbAdapter.get(ido);
//				Property p =da.getProperty(idProp);
//				System.out.println("Property tbAdapter p:"+p.toString());
//
//				if(p instanceof ObjectPropertyVirtual){
//					ArrayList<Fact> lf=OPtoFact(p);
//					Iterator<Fact> itf=lf.iterator();
//					System.out.println("ListFact:"+lf.toString());
//					System.out.println("LLEGA :"+p.toString());
//					while(itf.hasNext()){
//						Fact f=itf.next();
//						//System.err.println("Fact mio:" + f.toString());
//						System.out.println("Fact mio:" + f.toString());
//						//sess.addSessionable(f);
//					}
//					/*if(newValue==null && oldValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)oldValue).getValueCls(), newValue, action.DEL);
//						}
//						if(oldValue==null && newValue!=null){
//							System.err.println("setValue:"+p.toString());
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)newValue).getValueCls(), newValue, action.NEW);
//						}
//						if(oldValue!=null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)newValue).getValueCls(), newValue, action.SET);
//						}*/
//				}else{
//					ArrayList<Fact> lf=DPtoFact(p);
//					Iterator<Fact> itf=lf.iterator();
//					System.out.println("ListFact:"+lf.toString());
//					System.out.println("LLEGA :"+p.toString());
//					while(itf.hasNext()){
//						Fact f=itf.next();
//						//System.err.println("Fact mio:" + f.toString());
//						System.out.println("Fact mio:" + f.toString());
//						//sess.addSessionable(f);
//					}
//					/*if(newValue==null && oldValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataPropertyVirtual)p).getDataType(), newValue, action.DEL);
//						}
//						if(oldValue==null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataPropertyVirtual)p).getDataType(), newValue, action.NEW);
//						}
//						if(oldValue!=null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataPropertyVirtual)p).getDataType(), newValue, action.SET);
//						}*/
//				}
//
//			}else{
//
//				Property p=kb.getProperty(ido, idProp, userRol, user, usertask, s);
//				System.out.println("PROPERTY MOTOR :"+p.toString());
//				/*					if(p instanceof ObjectProperty){
//						if(newValue==null && oldValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)oldValue).getValueCls(), newValue, action.DEL);
//						}
//						if(oldValue==null && newValue!=null){
//							System.err.println("setValue:"+p.toString());
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)newValue).getValueCls(), newValue, action.NEW);
//						}
//						if(oldValue!=null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((ObjectValue)newValue).getValueCls(), newValue, action.SET);
//						}
//					}else{
//						if(newValue==null && oldValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataProperty)p).getDataType(), newValue, action.DEL);
//						}
//						if(oldValue==null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataProperty)p).getDataType(), newValue, action.NEW);
//						}
//						if(oldValue!=null && newValue!=null){
//							se.changeValue(p.getIdo(),p.getIdto(), p.getIdProp(),((DataProperty)p).getDataType(), newValue, action.SET);
//						}
//
//					}*/
//			}
//
//			//}
//			// al final hacer un commit. si falla el commit -> lanzar una
//			// exepcion para deshacer lo que estaba haciendo el commit.
//
//			//ka.setIk(this);	
//			sess.commit();
//			//SessionController.getInstance().setDDM(ik);
//			//ka.setIk(kb);
//
//			SessionController.getInstance().setActual(null);
//		} catch (ApplicationException e) {
//			cancelSession(sess);
//			throw e;
//		} catch (IncoherenceInMotorException e) {
//			cancelSession(sess);
//			throw e;
//		} catch (CardinalityExceedException e) {
//			cancelSession(sess);
//			throw e;
//		} catch (IncompatibleValueException e) {
//			cancelSession(sess);
//			throw e;
//		} catch (NotFoundException e) {
//			cancelSession(sess);
//			throw e;
//		}
//	}
//
//
//	private ArrayList<Fact> DPtoFact(Property p) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//		ArrayList<Fact> lf=new ArrayList<Fact>();
//		/*Iterator<Value> itv= p.getValues().iterator();
//		//System.err.println("Session actual:"+SessionController.getActualSession());
//		while(itv.hasNext()){
//			Value v= itv.next();
//			String val="";
//			Integer vcls=null;
//
//			if(v instanceof StringValue){
//				val=((StringValue)v).getValue();
//
//			}
//
//			if (v instanceof ObjectValue){
//				val=String.valueOf(((ObjectValue)v).getValue());
//				vcls=((ObjectValue)v).getValueCls();
//			}
//			Fact f = new Fact(p.getIdto(),p.getIdo(),p.getIdProp(),val,vcls,null,null,null,getClassName(p.getIdto()));
//			lf.add(f);
//		}*/
//
//		DataProperty dp = ((DataPropertyVirtual)p).toDataProperty();
//		LinkedList<IPropertyDef> lip=ka.traslateDataPropertyValueToIPropertyDef(dp);
//		Iterator<IPropertyDef> itip=lip.iterator();
//		while(itip.hasNext()){
//			IPropertyDef ip=itip.next();
//			Fact f=ip.toFact();
//			lf.add(f);
//		}
//
//		return lf;
//	}
//
//	private ArrayList<Fact> OPtoFact(Property p) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//		ArrayList<Fact> lf=new ArrayList<Fact>();
//		/*Iterator<Value> itv= p.getValues().iterator();
//		//System.err.println("Session actual:"+SessionController.getActualSession());
//		while(itv.hasNext()){
//			Value v= itv.next();
//			String val="";
//			Integer vcls=null;
//			if (v instanceof ObjectValue){
//				val=String.valueOf(((ObjectValue)v).getValue());
//				vcls=((ObjectValue)v).getValueCls();
//			}
//			Fact f = new Fact(p.getIdto(),p.getIdo(),p.getIdProp(),val,vcls,null,null,null,getClassName(p.getIdto()));
//			lf.add(f);
//		}*/
//		ka.setIk(this);
//		ObjectProperty op = ((ObjectPropertyVirtual)p).toObjectProperty();
//		LinkedList<IPropertyDef> lip=ka.traslateObjectPropertyValueToIPropertyDef(op);
//		Iterator<IPropertyDef> itip=lip.iterator();
//		while(itip.hasNext()){
//			IPropertyDef ip=itip.next();
//			Fact f=ip.toFact();
//			lf.add(f);
//		}
//		ka.setIk(kb);
//		return lf;
//	}
//
//	private void cancelSession(Session sess) throws NotFoundException, ApplicationException{
//		sess.rollBack();
//		SessionController.getInstance().setActual(null);		
//	}
//
//	public int loadNewData(ArrayList<IPropertyDef> arg0, Integer arg1, String arg2, Integer arg3, Session arg4) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//
//		return kb.loadNewData(arg0, arg1, arg2, arg3, arg4);
//	}
//
//	public void addIchangeProperty(IChangePropertyListener a) {
//		sessionBus.addIchangeProperty(a);
//
//	}
//
//	public void addSessionable(Sessionable s) {
//		this.sessionBus.addSessionable(s);
//
//	}
//
//	public void changeValue(Integer ido, int idto, int idProp, int valueCls, Value value, Value valueOld, int operation) {
//		this.sessionBus.changeValue(ido, idto, idProp, valueCls, value, valueOld, operation);
//
//
//
//	}
//
//	public void childSessionClosed(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, ApplicationException {
//		//System.err.println("Entra en child de business");
//		if (!this.getSesionables().isEmpty())
//			this.commit();
//
//
//
//	}
//
//	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException {
//		if(this.BRBoolean){
//			Collection<DataAdapter> lda=tbAdapter.values();
//			Iterator<DataAdapter> itda=lda.iterator();
//			while(itda.hasNext()){
//				DataAdapter da= itda.next();
//				Iterator<Property> itp=da.getPropertyIterator();
//				DataProperty dpbr= new DataProperty();
//				Property p1=da.getProperty(2);
//				dpbr.setIdo(p1.getIdo());
//				dpbr.setIdto(p1.getIdto());
//				dpbr.setIdProp(Constants.IdPROP_BUSINESSCLASS);
//				dpbr.setDataType(Constants.IDTO_BOOLEAN);
//				LinkedList<Value> lv= new LinkedList<Value>();
//				BooleanValue vb= new BooleanValue();
//				vb.setBvalue(true);
//				lv.add(vb);
//				dpbr.setValues(lv);
//				setPropertyUnAdapte(dpbr.getIdo(),dpbr);
//
//				while(itp.hasNext()){
//
//					Property p =itp.next();
//					Integer idPropO=da.getOriginalIdProp(p);
//					Integer idoO=da.getOriginalIdoClass(p);
//					Integer idtoO=da.getOriginalIdtoClass(p);
//					String type= da.getType(p);
//					if(p instanceof DataPropertyVirtual){
//						DataPropertyVirtual dp= (DataPropertyVirtual)p;
//						if(type.equals("MIN")){
//							Property puna=getPropertyUnAdapte(idPropO);
//							DataProperty dpuna=(DataProperty)puna;
//							if(dpuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								dpuna=new DataProperty();
//								dpuna.setIdo(dp.getIdo());
//								dpuna.setIdto(idtoO);
//								dpuna.setIdProp(idPropO);			
//								dpuna.setDataType(((DataProperty)pmotor).getDataType());
//								dpuna.setName(pmotor.getName());
//							}
//							LinkedList<Value> lval= dp.getValues();
//
//
//							DataValue vnew;
//							DataValue vold;
//							if(dp.getDataType()==Constants.IDTO_INT){
//								if(lval.isEmpty()){
//									vnew=new IntValue();
//								}else{
//									vnew=(IntValue)lval.getFirst();
//								}
//
//								vold=new IntValue();
//
//							}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//								if(lval.isEmpty()){
//									vnew=new DoubleValue();
//								}else{
//									vnew=(DoubleValue)lval.getFirst();
//								}
//								vold=new DoubleValue();
//							}else{
//								if(lval.isEmpty()){
//									vnew=new UnitValue();
//								}else{
//									vnew=(UnitValue)lval.getFirst();
//								}
//								vold=new UnitValue();
//							}
//
//							LinkedList<DataValue> ldv=null;
//							//int i=extractindex(dp.getName());
//							if(!dpuna.getEnumList().isEmpty()){
//								if(dp.getDataType()==Constants.IDTO_INT){
//									vold=(IntValue)dpuna.getEnumList().getFirst();
//								}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//									vold=(DoubleValue)dpuna.getEnumList().getFirst();
//								}else{
//									vold=(UnitValue)dpuna.getEnumList().getFirst();
//								}
//
//								ldv=dpuna.getEnumList();
//								ldv.removeFirst();
//							}else{
//								ldv= new LinkedList<DataValue>();
//								if(dp.getDataType()==Constants.IDTO_INT){
//									((IntValue)vold).setValueMax(null);
//									((IntValue)vold).setValueMin(null);
//								}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//									((DoubleValue)vold).setValueMax(null);
//									((DoubleValue)vold).setValueMin(null);
//								}else{
//									((UnitValue)vold).setValueMax(null);
//									((UnitValue)vold).setValueMin(null);
//								}
//
//							}
//
//
//							if(dp.getDataType()==Constants.IDTO_INT){
//
//								((IntValue)vnew).setValueMax(((IntValue)vold).getValueMax());
//								if(((IntValue)vnew).getValueMax()!=null || ((IntValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//
//								((DoubleValue)vnew).setValueMax(((DoubleValue)vold).getValueMax());
//								if(((DoubleValue)vnew).getValueMax()!=null || ((DoubleValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}else{
//
//								((UnitValue)vnew).setValueMax(((UnitValue)vold).getValueMax());
//								if(((UnitValue)vnew).getValueMax()!=null || ((UnitValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}
//
//							//ldv.add(vnew);
//							dpuna.setEnumList(ldv);	
//							//dpuna.setValues(new LinkedList<Value>());
//							/*boolean pass=checkFieldsDataProp(idoO,idPropO,type,vnew, dp.getNameGroup(),ido);
//							if(!pass){
//								throw new CardinalityExceedException();
//							}else{*/
//							System.out.println("Property de Clase: "+dpuna.toString());
//							setPropertyUnAdapte(dpuna.getIdo(),dpuna);
//							//}
//
//
//						}else if(type.equals("MAX")){
//							Property puna=getPropertyUnAdapte(idPropO);
//							DataProperty dpuna=(DataProperty)puna;
//							if(dpuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								dpuna=new DataProperty();
//								dpuna.setIdo(dp.getIdo());
//								dpuna.setIdto(idtoO);
//								dpuna.setIdProp(idPropO);			
//								dpuna.setDataType(((DataProperty)pmotor).getDataType());
//								dpuna.setName(pmotor.getName());
//							}
//							LinkedList<Value> lval= dp.getValues();
//							DataValue vnew;
//							DataValue vold;
//							if(dp.getDataType()==Constants.IDTO_INT){
//								if(lval.isEmpty()){
//									vnew=new IntValue();
//								}else{
//									vnew=(IntValue)lval.getFirst();
//								}
//
//								vold=new IntValue();
//
//							}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//								if(lval.isEmpty()){
//									vnew=new DoubleValue();
//								}else{
//									vnew=(DoubleValue)lval.getFirst();
//								}
//								vold=new DoubleValue();
//							}else{
//								if(lval.isEmpty()){
//									vnew=new UnitValue();
//								}else{
//									vnew=(UnitValue)lval.getFirst();
//								}
//								vold=new UnitValue();
//							}
//
//							LinkedList<DataValue> ldv=null;
//							//int i=extractindex(dp.getName());
//							if(!dpuna.getEnumList().isEmpty()){
//								if(dp.getDataType()==Constants.IDTO_INT){
//									vold=(IntValue)dpuna.getEnumList().getFirst();
//								}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//									vold=(DoubleValue)dpuna.getEnumList().getFirst();
//								}else{
//									vold=(UnitValue)dpuna.getEnumList().getFirst();
//								}
//
//								ldv=dpuna.getEnumList();
//								ldv.removeFirst();
//							}else{
//								ldv= new LinkedList<DataValue>();
//								if(dp.getDataType()==Constants.IDTO_INT){
//									((IntValue)vold).setValueMax(null);
//									((IntValue)vold).setValueMin(null);
//								}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//									((DoubleValue)vold).setValueMax(null);
//									((DoubleValue)vold).setValueMin(null);
//								}else{
//									((UnitValue)vold).setValueMax(null);
//									((UnitValue)vold).setValueMin(null);
//								}
//
//							}
//
//
//							if(dp.getDataType()==Constants.IDTO_INT){
//
//								((IntValue)vnew).setValueMin(((IntValue)vold).getValueMin());
//
//								if(((IntValue)vnew).getValueMax()!=null || ((IntValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//
//								((DoubleValue)vnew).setValueMin(((DoubleValue)vold).getValueMin());
//								if(((DoubleValue)vnew).getValueMax()!=null || ((DoubleValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}else{
//
//								((UnitValue)vnew).setValueMin(((UnitValue)vold).getValueMin());
//								if(((UnitValue)vnew).getValueMax()!=null || ((UnitValue)vnew).getValueMin()!=null){
//									ldv.add(vnew);
//								}
//							}
//
//
//							dpuna.setEnumList(ldv);	
//							//dpuna.setValues(new LinkedList<Value>());
//							/*boolean pass=checkFieldsDataProp(idoO,idPropO,type,vnew, dp.getNameGroup(),ido);
//							if(!pass){
//								throw new CardinalityExceedException();
//							}else{*/
//							System.out.println("Property de Clase: "+dpuna.toString());
//							setPropertyUnAdapte(dpuna.getIdo(),dpuna);
//							//}
//						}else if(type.equals("FIJO")){
//							Property puna=getPropertyUnAdapte(idPropO);
//							DataProperty dpuna=(DataProperty)puna;
//							if(dpuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								dpuna=new DataProperty();
//								dpuna.setIdo(dp.getIdo());
//								dpuna.setIdto(idtoO);
//								dpuna.setIdProp(idPropO);			
//								dpuna.setDataType(((DataProperty)pmotor).getDataType());
//								dpuna.setName(pmotor.getName());
//							}
//
//
//							LinkedList<Value> lval= dp.getValues();
//							DataValue vnew;
//							LinkedList<Value> ldv=new LinkedList<Value>();
//
//
//
//							if(dp.getDataType()==Constants.IDTO_INT){
//								if (!lval.isEmpty()){
//									vnew=(IntValue)dp.getValues().getFirst();
//									ldv.add(vnew);
//									dpuna.setValuesFixed(true);
//								}else{
//									vnew=new IntValue();
//									((IntValue)vnew).setValueMax(null);
//									((IntValue)vnew).setValueMin(null);
//								}
//
//							}else if(dp.getDataType()==Constants.IDTO_DOUBLE){
//								if (!lval.isEmpty()){
//									vnew=(DoubleValue)dp.getValues().getFirst();
//									ldv.add(vnew);
//									dpuna.setValuesFixed(true);
//								}else{
//									vnew=new DoubleValue();
//									((DoubleValue)vnew).setValueMax(null);
//									((DoubleValue)vnew).setValueMin(null);
//								}
//
//							}else{
//								if (!lval.isEmpty()){
//									vnew=(UnitValue)dp.getValues().getFirst();
//									ldv.add(vnew);
//									dpuna.setValuesFixed(true);
//								}else{
//									vnew=new UnitValue();
//									((UnitValue)vnew).setValueMax(null);
//									((UnitValue)vnew).setValueMin(null);
//								}
//
//							}
//
//
//
//
//							dpuna.setValues(ldv);
//
//							/*boolean pass=checkFieldsDataProp(idoO,idPropO,type,ivnew,dp.getNameGroup(),ido);
//							if(!pass){
//								throw new CardinalityExceedException();
//							}else{*/
//							System.out.println("Property de Clase: "+dpuna.toString());
//							setPropertyUnAdapte(dpuna.getIdo(),dpuna);
//							//}
//						}else{
//							Property puna=getPropertyUnAdapte(idPropO);
//							DataProperty dpuna=(DataProperty)puna;
//							if(dpuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								dpuna=new DataProperty();
//								dpuna.setIdo(dp.getIdo());
//								dpuna.setIdto(idtoO);
//								dpuna.setIdProp(idPropO);			
//								dpuna.setDataType(((DataProperty)pmotor).getDataType());
//								dpuna.setName(pmotor.getName());
//
//							}
//							dpuna.setValues(dp.getValues());
//							setPropertyUnAdapte(dpuna.getIdo(),dpuna);
//						}
//					}else{
//						ObjectPropertyVirtual op=(ObjectPropertyVirtual)p;
//						if(type.equals("ENUMERADOS")){
//							Property puna=getPropertyUnAdapte(idPropO);
//							ObjectProperty opuna=(ObjectProperty)puna;
//							if(opuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								opuna=new ObjectProperty();
//								opuna.setIdo(op.getIdo());
//								opuna.setIdto(idtoO);
//								opuna.setIdProp(idPropO);
//								opuna.setName(pmotor.getName());
//							}
//							LinkedList<ObjectValue> lov=new LinkedList<ObjectValue>();
//							LinkedList<Value> lval= new LinkedList<Value>();
//							lval=op.getValues();
//							lov.addAll((Collection<? extends ObjectValue>) lval);
//							opuna.setEnumList(lov);
//							System.out.println("Property de Clase: "+opuna.toString());
//							setPropertyUnAdapte(opuna.getIdo(),opuna);
//
//						}else if(type.equals("NORMAL")){
//							Property puna=getPropertyUnAdapte(idPropO);
//							ObjectProperty opuna=(ObjectProperty)puna;
//							if(opuna==null){
//								Property pmotor=kb.getProperty(idoO, idPropO, userRol, user, usertask, defSession);
//								opuna=new ObjectProperty();
//								opuna.setIdo(op.getIdo());
//								opuna.setIdto(idtoO);
//								opuna.setIdProp(idPropO);
//								opuna.setName(pmotor.getName());
//
//							}
//							opuna.setValues(op.getValues());
//							opuna.setValuesFixed(true);
//							System.out.println("Property de Clase: "+opuna.toString());
//							setPropertyUnAdapte(opuna.getIdo(),opuna);
//						}
//					}
//
//				}
//
//			}
//			imprimeUnAdapter();
//
//			ArrayList<IPropertyDef> lf=new ArrayList<IPropertyDef>();
//
//			Set<Integer> keys=tbUnAdapter.keySet();
//			Iterator<Integer> itkey=keys.iterator();
//			while(itkey.hasNext()){
//				Integer key=itkey.next();
//				System.out.println("--IDO="+key);
//				LinkedList<Property> lp=tbUnAdapter.get(key);
//				Iterator<Property> itp=lp.iterator();
//				while(itp.hasNext()){
//					Property p=itp.next();
//					lf.addAll(ka.traslatePropertyBusinessRestricctionToIPropertyDef(p,action.NEW));
//
//				}
//
//			}
//
//			if (this.server != null)
//				System.out.println("Lista de Fact BR:");
//			Iterator<IPropertyDef> itpdf=lf.iterator();
//			while (itpdf.hasNext()){
//				System.out.println(itpdf.next().toString());
//			}
//			Iterator<Sessionable> it=this.getSesionables().iterator();
//			System.out.println("Sessionables****");
//			ArrayList<Sessionable> listadescarte=new ArrayList<Sessionable>();
//			while(it.hasNext()){
//				IPropertyDef ip=(IPropertyDef) it.next();
//				System.out.println(ip.toString()+"\n");
//				if (ip.getIDTO()<0 || (getLevelOf(ip.getIDO())!=null && getLevelOf(ip.getIDO()).intValue()==Constants.LEVEL_FILTER)){
//					listadescarte.add((Sessionable)ip);
//				}else{
//					lf.add(ip);
//				}
//			}
//
//			Changes changesServer=this.server.serverTransitionObject(null,this.user,null,new IndividualData(lf));
//
//			this.getSesionables().removeAll(listadescarte);
//			notifyHistoryDDBB(changesServer,lf);
//			sessionBus.getSesionables().removeAll(this.getSesionables());
//
//
//			System.out.println("******Commit BR*****");
//			cleanAll();
//			return false;
//		}else{
//			System.out.println("******Commit DDM*****");
//			System.out.println("****SessRoot id="+this.sessRoot.getID()+"*******");
//
//			this.sessRoot.getSesionables().addAll(this.getSesionables());
//			this.getSesionables().removeAll(this.getSesionables());
//			return this.sessRoot.commit();
//		}
//
//
//	}
//
//	//TODO Quitar de aqui cuando se reeestructure RuleEngine
//	private void notifyHistoryDDBB(Changes changesServer,ArrayList<IPropertyDef> facts/*int ido, int idto, String rdn, int operation*/) {
//		HashMap<Integer,String> idoRdnChangeMap=new HashMap<Integer, String>();
//		//Element changes = res.getChild("CHANGES");
//		ArrayList<ObjectChanged> objectIndexList=changesServer.getAObjectChanged();
//		//if(objectIndexList!=null){
//		Iterator<ObjectChanged> iter = objectIndexList.iterator();
//		while (iter.hasNext()) {
//			ObjectChanged nodeIdo = (ObjectChanged)iter.next();
//			int idProp = nodeIdo.getProp();//Integer.parseInt(nodeIdo.getAttributeValue("PROP"));
//			if(idProp==Constants.IdPROP_RDN){
//				String rdn = ((StringValue)nodeIdo.getNewValue()).getValue();//getAttributeValue("NEW_VALUE");
//				int oldIdo = nodeIdo.getOldIdo();
//				idoRdnChangeMap.put(oldIdo,rdn);
//			}
//		}
//
//
//		ArrayList<Integer> idosProcessed=new ArrayList<Integer>();
//		Iterator<IPropertyDef> itr=facts.iterator();
//		while(itr.hasNext()){
//			IPropertyDef fact=itr.next();
//			int ido=fact.getIDO();
//			if(!idosProcessed.contains(ido)){
//				int idto=fact.getIDTO();
//				try{
//					int operation=fact.getOrder();
//					String rdn=null;
//					boolean notify=true;
//					//TODO Ahi que obtener el rdn al borrar
//					if(operation==action.DEL_OBJECT/* && fact.getPROP()==Constants.IdPROP_RDN)*/){
//						//IPropertyDef factd=getInitialValueOfFactDeleted(ido, Constants.IdPROP_RDN);
//						rdn="";//factd.getVALUE();
//					}else if(operation==action.SET || operation==action.NEW){
//						if(!Constants.isIDTemporal(ido))//Tenemos que hacer esto porque la operation del fact es sobre ese fact solamente, no sobre el individuo
//							operation=action.SET;
//						rdn=idoRdnChangeMap.get(ido);
//						if(rdn==null){
//							DataProperty property=(DataProperty)getProperty(ido,Constants.IdPROP_RDN,null,user,null,this.defSession);
//							rdn=!property.getValues().isEmpty()?((StringValue)property.getValues().get(0)).getValue():"";
//						}
//					}else notify=false;
//
//					if(notify){
//						for (int i = 0 ; i < historyDDBBListeners.size();i++)
//						{
//							historyDDBBListeners.get(i).changeHistory(ido, idto, rdn, operation);
//							System.out.println("AVISO BUSINESS:"+operation);
//						}
//						idosProcessed.add(ido);
//
//					}
//				}catch(Exception ex){
//					ex.printStackTrace();
//				}
//			}
//		}
//		//}
//	}
//
//	private void cleanAll() {
//		tbAdapter=new HashMap<Integer, DataAdapter>();
//		tbGroupNameG= new HashMap<Integer, String>();
//		tbIdPropIdo= new HashMap<Integer, Integer>();
//		tbIdPropVIdPropO=new HashMap<Integer, Integer>();
//		tbIdtoIdo=new HashMap<Integer, Integer>();
//		//tbOPIdProp=new HashMap<Integer, Integer>();
//		//tbSessionIdo=new HashMap<Integer, Integer>();
//		tbUnAdapter=new HashMap<Integer, LinkedList<Property>>();
//		BRBoolean=false;
//		SessionController.getInstance().setDDM(this.kb);
//	}
//
//	public int getID() {
//		return sessionBus.getID();
//	}
//
//	public int getIDMadre() {
//		return sessionBus.getIDMadre();
//	}
//
//	public ArrayList<Sessionable> getSesionables() {
//		return sessionBus.getSesionables(); 
//	}
//
//	public Integer getUtask() {
//		return sessionBus.getUtask();
//	}
//
//	public void rollBack() throws ApplicationException, NotFoundException {
//		if (BRBoolean){
//			cleanAll();
//		}else{
//			this.sessRoot.rollBack();
//		}
//
//	}
//
//	public void setID(int id) {
//		this.sessionBus.setID(id);
//
//	}
//
//	public void setIDMadre(int madre) {
//		this.sessionBus.setIDMadre(madre);		
//	}
//
//	public void setUtask(Integer utask) {
//
//		sessionBus.setUtask(utask);
//	}
//
//	public void addChangeServerListener(IChangeServerListener changeServerListener) {
//		// TODO hay que implementar
//
//	}
//
//	public int getCategory() {
//		return sessionBus.getCategory();
//	}
//
//	public void setCategory(int category) {
//		sessionBus.setCategory(category);
//	}
//
//	public boolean checkCoherenceObject(int ido, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException {
//
//
//		return kb.checkCoherenceObject(ido, userRol, user, usertask);
//	}
//
//	public Integer getLevelOf(int ido) {
//
//		return kb.getLevelOf(ido);
//
//	}
//
//	public ArrayList<Alias> getListAlias() {
//
//		return kb.getListAlias();
//
//	}
//
//	public Integer getIdClass(String name) throws NotFoundException {
//		return kb.getIdClass(name);
//
//	}
//
//
//	public Integer getIdProperty(String nameProp) throws NotFoundException {
//		return kb.getIdProperty(nameProp);
//	}
//
//	public ArrayList<ColumnProperty> getColumnProperties() {
//		return kb.getColumnProperties();
//	}
//
//	public QueryXML getQueryXML(IAlias alias) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public int getMode() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	public void setMode(int mode) {
//		// TODO Auto-generated method stub
//		
//	}
//
//
//}
