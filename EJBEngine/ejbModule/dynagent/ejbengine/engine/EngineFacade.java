package dynagent.ejbengine.engine;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.exceptions.RuleEngineException;
import dynagent.common.exceptions.ServerException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.BooleanValue;
import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.DoubleValue;
import dynagent.common.properties.values.IntValue;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.StringValue;
import dynagent.common.properties.values.TimeValue;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.Session;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.QueryConstants;
import dynagent.ejbengine.exception.EngineException;
import dynagent.ejbengine.util.XMLProcessor;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.MetaData;
import dynagent.server.ejb.ServerEngine;
import dynagent.server.services.InstanceService;

public class EngineFacade implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4324712025087281176L;
	/** Objeto que nos permite comunicarnos con el servidor. */
	private InstanceService instanceService;
	/** Objeto que nos permite conectarnos a base de datos. */
	private FactoryConnectionDB fcdb;
	/** Motor con el que estamos trabajando */
	private IKnowledgeBaseInfo knowledgeBaseInfo;
	/** Mapa de la base de datos con la que estamos trabajando */
	private DataBaseMap dataBaseMap;
	/** Usuario que ha pedido la creación del motor. */
	private String user;
	/** Ficheros de reglas con los que se ha creado el motor. */
	private ArrayList<String> rules;
	/** Ruta donde podemos encontrar los ficheros de reglas (directorio). */
	private final static String rulesPath = "../server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/bin/";
	/**
	 * Sesión activa con la que se añaden los datos al motor de reglas.<br>
	 * Si es <code>null</code> significa que no hay ninguna sesión activa.
	 */
	private Session session;
	private Session rootSession;
	
	public EngineFacade (FactoryConnectionDB fcdb, DataBaseMap dataBaseMap, InstanceService instanceService, String user, ArrayList<String> rules){
		this.dataBaseMap = dataBaseMap;
		this.fcdb = fcdb;
		this.user = user;
		this.instanceService = instanceService;
		
		this.rules = new ArrayList<String>();
		for (String rule : rules) {
			this.rules.add(rulesPath + rule);
		}
	}

	/**
	 * Construye el motor con el que va a trabajar este objeto, utilizando un
	 * hilo distinto para no entorpecer el flujo del hilo principal
	 * @throws EngineException 
	 */
	public void buildEngine() throws EngineException{
//		Thread engineBuilder = new Thread("Engine " + fcdb.getBusiness() + " Builder"){
//			
//			public void run(){
				RuleEngineFactory factory = RuleEngineFactory.getInstance();
				try {
					Element root = MetaData.getMetaData(user, false, fcdb, fcdb.getBusiness(), new ServerEngine(fcdb), dataBaseMap);
					knowledgeBaseInfo = factory.createRuler(root, fcdb.getBusiness(), instanceService, Constants.RULER, user, rules, null, null, null, true);
					knowledgeBaseInfo.getRootSession().setReusable(true);
				} catch (Exception e) {
					e.printStackTrace();
					EngineException ex=new EngineException("Error al crear el motor");
					ex.setStackTrace(e.getStackTrace());
					throw ex;
				}
//			}
//		};
//		
//		engineBuilder.start();
	}

	/**
	 * Crea una sesión y la almacena para que todos los datos que se añadan
	 * hasta que se indique el fin de sesión se encuentren en la misma.
	 */
	public void startSession(){
		// Esperamos a que se haya terminado de construir el motor de reglas
		// antes de intentar crear la sesión.
		while(knowledgeBaseInfo == null){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(rootSession==null || rootSession.isFinished()){
			rootSession=new DefaultSession(knowledgeBaseInfo, knowledgeBaseInfo.getRootSession(),null,true, true, true, true, true);
		}
		// Si no hay una sesión activa, creamos una nueva, de lo contrario, se
		// deja que el usuario use la ya existente.
		if (session == null || session.isFinished()){
			session=new DefaultSession(knowledgeBaseInfo, rootSession,null,true, true, true, false, false);
		}
	}

	/**
	 * Hace commit de la sesión activa y se deja de tener referencia a la misma.<br>
	 * Para que se puedan insertar datos después de llamar a este método es
	 * necesario iniciar una nueva sesión.
	 * 
	 * @return Devuelve <code>true</code> si se ha conseguido hacer commit con
	 *         éxito.
	 * @throws EngineException
	 *             Si se produce algún error mientras se trabaja con el motor.
	 */
	public boolean commitSession() throws EngineException{
		boolean success = false;
		try {
			// Hacemos commit de la sesión.
			session.commit();
			success = true;
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}finally{
			// Si no se ha conseguido hacer commit con éxito, se hace rollback
			// de la sesión.
			if(!success){
				try{
					session.rollBack();
				}catch(Exception ex){
					ex.printStackTrace();
					System.err.println("ERROR: No se ha podido hacer rollback de la session en EngineFacade.addData");
					EngineException exe=new EngineException("Error al tratar excepcion al hacer commit de la session");
					exe.setStackTrace(ex.getStackTrace());
					throw exe;
				}
			}
			// Desvinculamos la sesión que se ha usado, pues ya no nos es útil
			// para nada más.
			session = null;
		}
		return success;
	}
	
	/**
	 * Hace rollback de la sesión activa y de la rootSession y se deja de tener referencia a la misma.<br>
	 * Para que se puedan insertar datos después de llamar a este método es
	 * necesario iniciar una nueva sesión.
	 * 
	 * @return Devuelve <code>true</code> si se ha conseguido hacer rollback con
	 *         éxito.
	 * @throws EngineException
	 *             Si se produce algún error mientras se trabaja con el motor.
	 */
	public boolean cancelData() throws EngineException{
		boolean success = false;
		try {
			// Hacemos rollback de la sesión.
			cancelSession();
			if(rootSession!=null && !rootSession.isFinished()){
				rootSession.rollBack();
				rootSession = null;
			}
			success = true;
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
		return success;
	}
	
	/**
	 * Hace rollback de la sesión activa y se deja de tener referencia a la misma.<br>
	 * Para que se puedan insertar datos después de llamar a este método es
	 * necesario iniciar una nueva sesión.
	 * 
	 * @return Devuelve <code>true</code> si se ha conseguido hacer rollback con
	 *         éxito.
	 * @throws EngineException
	 *             Si se produce algún error mientras se trabaja con el motor.
	 */
	public boolean cancelSession() throws EngineException{
		boolean success = false;
		try {
			// Hacemos rollback de la sesión.
			if(session!=null && !session.isFinished()){
				session.rollBack();
				session = null;
			}
			success = true;
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
		return success;
	}

	/**
	 * Añade datos al motor usando la sesión activa.
	 * 
	 * @param id
	 *            Identificador del objeto.<br>
	 *            Puede ser:
	 *            <ul>
	 *            <li><b>IDTO de clase:</b> Si se quiere crear un objeto de
	 *            dicha clase.</li>
	 *            <li><b>IDO:</b> Si se quieren vincular propiedades a un objeto
	 *            ya existente en motor.</li>
	 *            </ul>
	 * @return Devuelve el identificador del objeto creado o el id del objeto al
	 *         que se le ha añadido información.<br>
	 *         Si se produce algún error, devuelve null.
	 * @param data
	 *            Mapa de idPropiedad-Valor que se quiere añadir al objeto.
	 * @throws EngineException
	 *             Si se produce algún fallo mientras se trabaja con el motor.
	 * @see {@link #startSession()}
	 */
	public Integer addData(int id, Map<Integer, String> data) throws EngineException {
		// Comprobamos que haya una sesión con la que podamos trabjar.
		if (session == null){
			throw new EngineException("No se pueden añadir datos porque no hay ninguna sesión activa");
		}
		
		Integer ido = id;
		try{
			int idto = knowledgeBaseInfo.getClassOf(id);
			LinkedHashMap<Integer,Value> mapPropValue=new LinkedHashMap<Integer, Value>();
			for(int idProp:data.keySet()){
				String value=data.get(idProp);
				Integer valueCls=null;
				if(knowledgeBaseInfo.isObjectProperty(idProp)){
					int idtoReferenced = QueryConstants.getIdto(Integer.parseInt(value));
					if (idtoReferenced > 0){
						valueCls = idtoReferenced;
					} else {
						valueCls = knowledgeBaseInfo.getClassOf(Integer.valueOf(value));
					}
					
					if(knowledgeBaseInfo.isIDClass(id)){
						Integer idtoAux=getSpecificClass(id, valueCls, Integer.valueOf(value), idProp);
						if(!Auxiliar.equals(idtoAux, idto)){
							if(knowledgeBaseInfo.isSpecialized(idtoAux, idto)){
								idto=idtoAux;
							}
						}
					}
				}else{
					valueCls=knowledgeBaseInfo.getDatatype(idProp);
				}
				
				
				Value newValue=buildValue(value, valueCls);
				mapPropValue.put(idProp, newValue);
			}
			
			if(knowledgeBaseInfo.isIDClass(id)){
				ido=knowledgeBaseInfo.createPrototype(idto, Constants.LEVEL_PROTOTYPE, null, user, null, session);
			}
			
			for(int idProp:mapPropValue.keySet()){
				Property prop=knowledgeBaseInfo.getProperty(ido, idto, idProp, null, user, null, session);
				Value oldValue=null;
				if(Auxiliar.equals(prop.getCardMax(),1) && !prop.getValues().isEmpty()){
					oldValue=prop.getValues().get(0);
				}
				knowledgeBaseInfo.setValue(ido, idto, idProp, oldValue, mapPropValue.get(idProp), null, user, null, session);
			}
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			e.printStackTrace();
			throw new EngineException(e);
		}
		return ido;
	}
	
	public ArrayList<String> getData(int ido,String className,String propName) throws EngineException {
		// Comprobamos que haya una sesión con la que podamos trabjar.
		if (session == null){
			throw new EngineException("No se pueden añadir datos porque no hay ninguna sesión activa");
		}
		
		ArrayList<String> data=new ArrayList<String>();
		try{
			int idto=knowledgeBaseInfo.getIdClass(className);
			Integer idProp = knowledgeBaseInfo.getIdProperty(propName);
			Property property=knowledgeBaseInfo.getProperty(ido, idto, idProp, null, user, null, session);
			Iterator<Value> itrValues=property.getValues().iterator();
			while(itrValues.hasNext()){
				Value value=itrValues.next();
				data.add(value.getValue_s());
			}
			
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
		return data;
	}
	
	/**
	 * Encuentra la clase específica por la que se relacionan dos tipos de
	 * objetos.
	 * 
	 * @param parentIdto
	 *            Clase de la familia de la que queremos encontrar la clase
	 *            específica.
	 * @param objectReferencedIdto
	 *            Clase del objeto que es apuntado y del que queremos saber qué
	 *            clase específica escoger para contenerlo.
	 * @param objectReferencedIdo
	 *            Identificador de objeto
	 * @param idProperty
	 *            Propiedad por la que apunta <b>parentIdto</b> a
	 *            <b>objectReferencedIdto</b>
	 * @return Identificador de la clase específica que se debe usar para
	 *         representar la relación entre los dos objetos.<br>
	 *         Devolverá <code>null</code> si no se relacionan.
	 * @throws EngineException
	 *             Si se produce algún error en las consultas al motor.
	 */
	public Integer getSpecificClass(Integer parentIdto, Integer objectReferencedIdto, Integer objectReferencedIdo, Integer idProperty) throws EngineException {
		Integer result = null;
		try {
			HashSet<Integer> idtos = knowledgeBaseInfo.getClassifiedIdtos(parentIdto, idProperty, new ObjectValue(objectReferencedIdo, objectReferencedIdto), user, null, null, session);
			if(!idtos.isEmpty()){
				result = idtos.iterator().next();
			}
			if (idtos.size() > 1){
				System.err.println("WARNING: Al buscar el objeto de la familia de " + parentIdto + " que puede contener objetos del tipo " + objectReferencedIdto + " se obtiene más de un resultado.");
				System.err.println("\t" + idtos.toString());
			}
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
		return result;
	}

	/**
	 * Guarda los datos que se tengan en ese momento en el motor.
	 * 
	 * @throws EngineException
	 *             Si se produce algún error en el motor.
	 */
	public void commitData() throws EngineException {
		boolean success=false;
		Session session=new DefaultSession(knowledgeBaseInfo, rootSession,null,true, true, true, false, true);
		try {
			session.commit();
			success=true;
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		} finally{
			if(!success){
				try{
					session.setForceParent(false);
					session.rollBack();
				}catch(Exception ex){
					ex.printStackTrace();
					System.err.println("ERROR: No se ha podido hacer rollback de la session en commitData");
					EngineException exe=new EngineException("Error al tratar excepcion al hacer rollback de la session");
					exe.setStackTrace(ex.getStackTrace());
					throw exe;
				}
			}
		}
	}

	/**
	 * Saca un objeto de motor con toda su información
	 * 
	 * @param ido
	 *            Identificador del objeto.
	 * @param idto
	 *            Identificador de la clase del objeto.
	 * @param levels
	 *            Niveles a consultar.
	 * @return Documento con la consulta
	 * @throws EngineException
	 */
	public Document getObject(int ido, int idto, int levels) throws EngineException{
		Map<Integer, EngineObject> objectsByIdo = new Hashtable<Integer, EngineObject>();
		Document result = null;
		try {
			getObjectRec(ido, idto, levels, objectsByIdo);
			result = XMLProcessor.getDocument(ido, objectsByIdo, dataBaseMap);
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
		return result;
	}

	private EngineObject getObjectRec(int ido, int idto, int depth, Map<Integer, EngineObject> objectsByIdo) throws ServerException, RuleEngineException, SQLException, NamingException, JDOMException, ParseException{
		// Creamos el objeto que va a contener la información de este objeto.
		EngineObject engineObject = new EngineObject(ido, idto);
		objectsByIdo.put(ido, engineObject);
		// Cogemos todas las propiedades del objeto para iterar sobre ellas.
		Iterator<Property> properties = knowledgeBaseInfo.getAllPropertyIterator(ido, idto, null, user, null, rootSession);
		while (properties.hasNext()) {
			Property property = properties.next();
			boolean isObjectProperty = property instanceof ObjectProperty;
			for (Value value : property.getValues()) {
				// Le indicamos al objeto un nuevo valor para la propiedad actual.
				String valueString = value.getValue_s();
				engineObject.addPropertyValue(property.getIdProp(), valueString);
				if (isObjectProperty && depth > 0){
					// Si es una ObjectProperty y quedan niveles por analizar,
					// miramos si el objeto referenciado se ha analizado. De no
					// ser así, analizamos el objeto.
					Integer idoReferenced = Integer.parseInt(valueString);
					ObjectValue objectValue = (ObjectValue) value;
					if (objectsByIdo.get(idoReferenced) == null){
						getObjectRec(idoReferenced, objectValue.getValueCls(), depth - 1, objectsByIdo);
					}
				}
			}
			if (isObjectProperty){
				engineObject.addObjectProperty(property.getIdProp());
			}
		}
		return engineObject;
	}

	private Value buildValue(String value,int valueCls){
		Value valueObject=null;
		if(value!=null){
			valueObject=buildDataValue(value,valueCls);
			if(valueObject==null)//Se trata de un ObjectValue	
				valueObject=buildObjectValue(Integer.parseInt(value),valueCls);
		}
		return valueObject;
	}
	
	
	private DataValue buildDataValue(Object value,int valueCls){
		DataValue dataV=null;
		String[] buf;
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
		return dataV;
	}
	
	private ObjectValue buildObjectValue(int value,int valueCls){
		ObjectValue objectV=new ObjectValue();
		objectV.setValue(value);
		objectV.setValueCls(valueCls);

		return objectV;
	}

	public void startEdition(int ido) throws EngineException {
		try{
			knowledgeBaseInfo.setLock(ido, true, user, session);
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public void stopEdition(int ido) throws EngineException {
		try{
			knowledgeBaseInfo.setLock(ido, false, user, session);
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
	public void loadObject(int ido,String className) throws EngineException{
		// Comprobamos que haya una sesión con la que podamos trabjar.
		if (session == null){
			throw new EngineException("No se pueden añadir datos porque no hay ninguna sesión activa");
		}
		
		try{
			int idto=knowledgeBaseInfo.getIdClass(className);
			knowledgeBaseInfo.loadIndividual(ido, idto, 3, true, true, null, user, null, session);
		} catch (ServerException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (RuleEngineException e) {
			System.err.println(e.getUserMessage());
			throw new EngineException(e,e.getUserMessage());
		} catch (Exception e) {
			throw new EngineException(e);
		}
	}
	
}
