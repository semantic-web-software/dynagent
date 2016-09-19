package dynagent.ejbengine.ejb;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import org.jdom.Document;
import org.jdom.JDOMException;

import dynagent.common.basicobjects.License;
import dynagent.common.exceptions.DataErrorException;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.jdomParser;
import dynagent.ejbengine.engine.EngineFacade;
import dynagent.ejbengine.exception.EngineException;
import dynagent.ejbengine.util.DataBaseMapManager;
import dynagent.ejbengine.util.XMLProcessor;
import dynagent.server.dbmap.ClassInfo;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.server.services.QueryService2;


public class ServerEngineBean implements SessionBean {
	
	private static final long serialVersionUID = 6923327688943017039L;
	/** Mapa de la base de datos con la que estamos trabajando. */
	private DataBaseMap dataBaseMap;
	/** Objeto que nos permite conectarnos a la base de datos. */
	private FactoryConnectionDB fcdb;
	/** Usuario que de la sesión a la que se corresponde este bean */
	private String user;
	/** Motor con el que trabaja este bean. */
	private EngineFacade engine;
	/** Identificador del objeto principal con el que se está trabajando en motor. */
	private Integer mainIdo;
	/** Idto del objeto principal con el que se está trabajando en motor. */
	private Integer mainIdto;
	/** Almacena el error que se haya producido, si es que se ha producido alguno */
	private String error;
	/** Objeto que nos permite comunicarnos con el servidor. */
	private InstanceService instanceService;
	
	/**
	 * @see {@link ServerEngineHome#create(String, int, LinkedList)}
	 */
	public void ejbCreate(String gestor, String host, int port, int bns, String user, LinkedList<String> rules) throws CreateException{
		try {
			// Montamos la conexión a base de datos.
			System.err.println("EJBCreate business:"+bns);
			fcdb = new FactoryConnectionDB(bns, false, host, gestor);
			fcdb.setPort(port);
			// Conseguimos el Mapa del modelo y de la base de datos.
			DataBaseMapManager manager = DataBaseMapManager.getInstance(fcdb);
			dataBaseMap = manager.getDataBaseMap(bns);
			instanceService = new InstanceService(fcdb, null, false);
			instanceService.setDataBaseMap(dataBaseMap);
			engine = new EngineFacade(fcdb, dataBaseMap, instanceService, user, new ArrayList<String>(rules));
			engine.buildEngine();
			mainIdo = null;
			this.user = user;
			error = null;
		} catch (Exception e) {
			e.printStackTrace();
			CreateException ex=new CreateException("Error al intentar crear el bean");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	@Override
	public void ejbActivate() throws EJBException, RemoteException {}

	@Override
	public void ejbPassivate() throws EJBException, RemoteException {}

	@Override
	public void ejbRemove() throws EJBException, RemoteException {
		try {
			fcdb.removeConnections();
		} catch (SQLException e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error mientras se intentaban cerrar las conexiones a base de datos");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}

	@Override
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {}
	
	/**
	 * @throws RemoteException 
	 * @see {@link ServerEngine#save()}
	 */
	public void save() throws RemoteException{
		try {
			engine.commitData();
			mainIdo = null;
		} catch (EngineException e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar guardar en base de datos");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	public Document view(Integer idto, Integer tableId, String className) throws RemoteException{
		Document view = null;
		if(tableId==null){
			view = viewFromEngine(mainIdo,className);
		}else{
			view = viewFromDB(idto, tableId);
		}
		try {
			System.err.println("view:"+jdomParser.returnXML(view));
		} catch (JDOMException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Document resultDocument=view;
		try{
			XMLProcessor.fillRefNodes(view);
			try {
				System.err.println("fillRefNodes:"+jdomParser.returnXML(view));
			} catch (JDOMException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar guardar en base de datos");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}

		return resultDocument;
	}

	/**
	 * Añade los datos indicados al motor.<br>
	 * Si no se consiguen insertar todos los datos con éxito en el motor, no se
	 * insertará ninguno.
	 * 
	 * @param queryParams
	 *            Pares propiedad-valor que se quieren añadir al motor.
	 * @return Devuelve <code>true</code> si, y sólo si, se han conseguido
	 *         insertar con éxito <b>TODOS</b> los datos en el motor.
	 * @throws RemoteException 
	 */
	public void addData(Integer idParent,Integer idPropParent,int id,Map<String, String> queryParams) throws RemoteException{
		boolean success = false;
		try{
			// Comenzamos la sesión que se va a usar para insertar los datos. Esto
			// es especialmente útil porque, si falla el motor en algún momento
			// durante la inserción, se puede deshacer todo de una manera sencilla.
			engine.startSession();
			
			// TODO Implementar método para deducir el tipo de línea a usar
			// dependiendo del tipo de artículo usado.
			Map<Integer, String> properties = convertMapToNumeric(queryParams);

			Integer newIdo = engine.addData(id, properties);
			boolean isCreated = !Auxiliar.equals(newIdo,id);//Significaria que se ha creado un prototipo de ese id
			boolean lock = isCreated;
			if(isCreated){
				id=newIdo;
			}
			if(lock && properties.isEmpty()/*Si properties no estuviera vacio ya lo habria bloqueado ruleEngine*/){
				engine.startEdition(id);
			}
			
			if(idPropParent!=null){
				// Construimos el mapa para indicar que queremos añadir la línea
				// recién creada al pedido existente.
				properties = new Hashtable<Integer, String>();
				properties.put(idPropParent, String.valueOf(id));
				engine.addData(idParent!=null?idParent:mainIdo, properties);
			}
			
			if(lock){
				engine.stopEdition(id);
			}
			
			// Terminamos la sesión.
			success = engine.commitSession();
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			success = false;
			RemoteException ex=new RemoteException("Error al intentar añadir datos al motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		} finally{
			if(!success){
				try {
					engine.cancelSession();
				} catch (EngineException e) {
					System.err.println("No ha podido hacerse rollback de la session en ServerEngineBean.addData");
					e.printStackTrace();
					RemoteException ex=new RemoteException("Error al tratar excepcion al añadir datos al motor");
					ex.setStackTrace(e.getStackTrace());
					throw ex;
				}
			}
		}
	}

	public ArrayList<String> getData(int ido,String className,String propName) throws RemoteException {
		ArrayList<String> data;
		// Comenzamos la sesión que se va a usar para insertar los datos. Esto
		// es especialmente útil porque, si falla el motor en algún momento
		// durante la inserción, se puede deshacer todo de una manera sencilla.
		engine.startSession();
		boolean success=false;
		try{
			data=engine.getData(ido, className, propName);
			success=true;
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			RemoteException ex=new RemoteException("Error al intentar añadir datos al motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}finally{
			try{
				if(!success){
					engine.cancelSession();
				}
			} catch (EngineException e) {
				System.err.println("No ha podido hacerse rollback de la session en ServerEngineBean.addData");
				e.printStackTrace();
				RemoteException ex=new RemoteException("Error al tratar excepcion al añadir datos al motor");
				ex.setStackTrace(e.getStackTrace());
				throw ex;
			}
		}
		
		return data;
	}
	
	/**
	 * Consulta los datos de un objeto que existe en base de datos.
	 * 
	 * @param idto
	 *            Identificador de la clase del objeto
	 * @param tableId
	 *            Identificador del objeto de la clase.
	 * @return Documento con la información del objeto que se tiene en base de
	 *         datos.
	 * @throws RemoteException 
	 */
	private Document viewFromDB(int idto, int tableId) throws RemoteException {
		Map<Integer, Set<Integer>> objects = new Hashtable<Integer, Set<Integer>>();
		Set<Integer> tableIds = new HashSet<Integer>();
		tableIds.add(tableId);
		objects.put(idto, tableIds);
		QueryService2 queryService = new QueryService2(objects, dataBaseMap, fcdb, 2, true, false, user, instanceService);
		Document result = null;
		try {
			 result = queryService.getData();
		} catch (Exception e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar obtener datos de la base de datos");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		return result;
	}

	/**
	 * Obtiene los datos de un objeto que existe en motor.
	 * 
	 * @param ido
	 *            Identificador del objeto que se quiere obtener de motor.
	 * @return Documento XML con los datos del objeto buscado o null si no
	 *         existe dicho objeto en motor.
	 * @throws RemoteException 
	 */
	private Document viewFromEngine(int ido, String className) throws RemoteException{
		ClassInfo orderClass = dataBaseMap.getClass(className);
		Document result = null;
		try {
			result = engine.getObject(ido, orderClass.getIdto(), 3);
		} catch (EngineException e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar obtener datos del motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		return result;
	}

//	/**
//	 * Crea un pedido con el cliente especificado.
//	 * 
//	 * @param queryParams
//	 *            Parámetros enviados desde el cliente indicado el cliente que
//	 *            se ha de añadir al pedido.
//	 * @return Devuelve <code>true</code> si se ha conseguido añadir
//	 *         correctamente el cliente.
//	 */
//	
//	private boolean addCustomerToOrder(Hashtable<String, String> queryParams) {
//		boolean result = true;
//		Map<Integer, String> properties = new Hashtable<Integer, String>();
//		// Recorremos los parámetros para añadir la información que nos ha
//		// indicado el cliente.
//		for (String idPropertyString : queryParams.keySet()) {
//			Integer idProperty = Integer.parseInt(idPropertyString);
//			String propertyValue = queryParams.get(idPropertyString);
//			properties.put(idProperty, propertyValue);
//		}
//		try {
//			mainIdo = engine.addData(mainIdo, properties);
//		} catch (Exception e) {
//			e.printStackTrace();
//			result = false;
//		}
//		return result;
//	}
//
//	private boolean addNewLine(Hashtable<String, String> queryParams) {
//		// TODO Implementar método para deducir el tipo de línea a usar
//		// dependiendo del tipo de artículo usado.
//		boolean result = true;
//		if (mainIdo == null){
//			// No se ha creado un objeto principal, en este caso un pedido, con
//			// lo cual no se puede añadir información al mismo.
//			return false;
//		}
//		// Sólo para pruebas se va a crear siempre una LÍNEA_ARTÍCULOS_MATERIA
//		// suponiendo que se usa un género.
//		
//		ClassInfo lineClass = getLineClass(queryParams);
//		Integer lineIdProperty = dataBaseMap.getPropertyId(Constants.PROP_NAME_LINEA);
//		Map<Integer, String> properties = new Hashtable<Integer, String>();
//		// Recorremos los parámetros para añadir la información que nos ha
//		// indicado el cliente.
//		for (String idPropertyString : queryParams.keySet()) {
//			Integer idProperty = Integer.parseInt(idPropertyString);
//			String propertyValue = queryParams.get(idPropertyString);
//			properties.put(idProperty, propertyValue);
//		}
//		try {
//			int newLineIdo = engine.addData(lineClass.getIdto(), properties);
//			if(newLineIdo != -1){
//				// Construimos el mapa para indicar que queremos añadir la línea
//				// recién creada al pedido existente.
//				properties = new Hashtable<Integer, String>();
//				properties.put(lineIdProperty, String.valueOf(newLineIdo));
//				engine.addData(mainIdo, properties);
//			}
//		} catch (EngineException e) {
//			e.printStackTrace();
//			result = false;
//		}
//		return result;
//	}

//	private ClassInfo getLineClass(Hashtable<String, String> queryParams) {
//		// Vemos el rango que tiene la propiedad línea para la clase PEDIDO_VENTA
//		ClassInfo orderClass = dataBaseMap.getClass(Constants.CLASS_PEDIDO);
//		Integer lineIdProperty = dataBaseMap.getPropertyId(Constants.PROP_NAME_LINEA);
//		PropertyInfo property = orderClass.getProperty(lineIdProperty);
//		Integer idtoLinea = property.getPropertyTypes().iterator().next();
//		
//		Integer productIdProperty = dataBaseMap.getPropertyId(Constants.PROP_NAME_PRODUCTO);
//		String idoProductString = queryParams.get(productIdProperty.toString());
//		Integer idoProduct = Integer.parseInt(idoProductString);
//		Integer idtoProduct = QueryConstants.getIdto(idoProduct);
//		
//		Integer specificLineIdto;
//		ClassInfo lineClass;
//		try {
//			specificLineIdto = engine.getSpecificClass(idtoLinea, idtoProduct, idoProduct, productIdProperty);
//			lineClass = dataBaseMap.getClass(specificLineIdto);
//		} catch (EngineException e) {
//			e.printStackTrace();
//			lineClass = null;
//		}
//		
//		return lineClass;
//	}
	
	public void initObject(String className) throws RemoteException
	{
		boolean success = true;
		// Comenzamos la sesión que se va a usar para insertar los datos. Esto
		// es especialmente útil porque, si falla el motor en algún momento
		// durante la inserción, se puede deshacer todo de una manera sencilla.
		cancel();//Nos aseguramos que no hay ninguna sesion anterior por haber navegado el usuario con el anterior del navegador
		engine.startSession();
		try {
			ClassInfo orderClass = dataBaseMap.getClass(className);
			mainIdo = engine.addData(orderClass.getIdto(), new HashMap<Integer, String>());
			mainIdto = orderClass.getIdto();
			// Terminamos la sesión.
			engine.commitSession();
			success=true;
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			success = false;
			RemoteException ex=new RemoteException("Error al intentar inicializar el objeto principal en motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		} finally{
			if(!success){
				try{
					engine.cancelSession();
				}catch(Exception ex){
					ex.printStackTrace();
					System.err.println("No se ha podido hacer rollback de la session");
					RemoteException exe=new RemoteException("Error al tratar excepcion al inicializar el objeto principal en motor");
					exe.setStackTrace(ex.getStackTrace());
					throw exe;
				}
			}
		}
	}
	
	public void cancel() throws RemoteException{
		try {
			engine.cancelData();
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			RemoteException ex=new RemoteException("Error al intentar cancelar los datos de motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
	}
	
	public void loadObject(int ido,String className) throws RemoteException{
		boolean success = false;
		try{
			// Comenzamos la sesión que se va a usar para insertar los datos. Esto
			// es especialmente útil porque, si falla el motor en algún momento
			// durante la inserción, se puede deshacer todo de una manera sencilla.
			engine.startSession();
			
			engine.loadObject(ido, className);
			
			// Terminamos la sesión.
			success = engine.commitSession();
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			success = false;
			RemoteException ex=new RemoteException("Error al intentar añadir datos al motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		} finally{
			if(!success){
				try {
					engine.cancelSession();
				} catch (EngineException e) {
					System.err.println("No ha podido hacerse rollback de la session en ServerEngineBean.addData");
					e.printStackTrace();
					RemoteException ex=new RemoteException("Error al tratar excepcion al añadir datos al motor");
					ex.setStackTrace(e.getStackTrace());
					throw ex;
				}
			}
		}
	}

	public Integer getMainIdo() {
		return mainIdo;
	}
	
	public Integer getIdo(String className, String rdn, boolean caseInsensitive) throws RemoteException {
		Integer ido=null;
		try {
			ido=InstanceService.getIdo(fcdb, dataBaseMap, dataBaseMap.getClass(className).getIdto(), rdn, caseInsensitive);
		} catch (DataErrorException e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar obtener el ido de la clase "+className+" con rdn "+rdn);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		return ido;
	}
	
	public Integer newIdo(String className,Map<String, String> propertiesMap) throws RemoteException {
		Integer newIdo=null;
		boolean success = false;
		try{
			// Comenzamos la sesión que se va a usar para insertar los datos. Esto
			// es especialmente útil porque, si falla el motor en algún momento
			// durante la inserción, se puede deshacer todo de una manera sencilla.
			engine.startSession();
			
			Map<Integer, String> properties=convertMapToNumeric(propertiesMap);
			newIdo = engine.addData(dataBaseMap.getClass(className).getIdto(), properties);
			if(properties.isEmpty()/*Si properties no estuviera vacio ya lo habria bloqueado ruleEngine*/){
				engine.startEdition(newIdo);
			}
			
			//engine.stopEdition(newIdo);
			
			// Terminamos la sesión.
			success = engine.commitSession();
		} catch (EngineException e) {
			e.printStackTrace();
			error=e.getUserMessage();
			success = false;
			RemoteException ex=new RemoteException("Error al intentar crear un nuevo objeto en motor");
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		} finally{
			if(!success){
				try {
					engine.cancelSession();
				} catch (EngineException e) {
					System.err.println("No ha podido hacerse rollback de la session en ServerEngineBean.addData");
					e.printStackTrace();
					RemoteException ex=new RemoteException("Error al tratar excepcion al crear un nuevo objeto en motor");
					ex.setStackTrace(e.getStackTrace());
					throw ex;
				}
			}
		}
		
		return newIdo;
	}
	
	private Map<Integer, String> convertMapToNumeric(Map<String, String> propertiesMap){
	
		Map<Integer, String> properties=new LinkedHashMap<Integer, String>();
		
		if(propertiesMap==null){
			return properties;
		}
		
		// Recorremos los parámetros para añadir la información que nos ha
		// indicado el cliente.
		for (String idPropertyString : propertiesMap.keySet()) {
			Integer idProperty = null;
			try{
				idProperty = Integer.parseInt(idPropertyString);
			}catch(NumberFormatException ex){
				idProperty = dataBaseMap.getPropertyId(idPropertyString);//Aqui entraria cuando en vez de pasarle el idProp se le pase el nombre de la property
			}
			String propertyValue = propertiesMap.get(idPropertyString);
			properties.put(idProperty, propertyValue);
		}
		
		return properties;
	}
	
	/*
	 * Este metodo es case sensitive*/
	
	public ArrayList<Integer> getIdos(String className, Map<String,Object> mapPropertyValue, boolean caseInsensitive) throws RemoteException {
		ArrayList<Integer> idos=null;
		try {
			idos=InstanceService.getIdos(fcdb, dataBaseMap, dataBaseMap.getClass(className).getIdto(), mapPropertyValue, caseInsensitive);
		} catch (DataErrorException e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar obtener los idos de la clase "+className+" con property-valor "+mapPropertyValue);
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		return idos;
	}
	
	
	public License getLicense() throws RemoteException{
		License license=null;
		try{
			license=instanceService.serverGetLicense();
		} catch (Exception e) {
			e.printStackTrace();
			RemoteException ex=new RemoteException("Error al intentar obtener la licencia de la base de datos "+fcdb.getBusiness());
			ex.setStackTrace(e.getStackTrace());
			throw ex;
		}
		return license;
	}
	
	/* ---------------------------------------------- 
	 * Estos métodos no deberían estar. Se han hecho para compatibilidad con ServerWeb pero deben eliminarse cuando HTMLFacade no necesite estos datos*/
	
	public DataBaseMap getDataBaseMap() throws RemoteException{
		return dataBaseMap;
	}
	
	public String getUser() throws RemoteException{
		return user;
	}
	
	public String getError() throws RemoteException{
		return error;
	}
	
	public void setError(String error) throws RemoteException{
		this.error=error;
	}
	
	/* ---------------fin metodos a borrar-----------------------------------------------*/
}
