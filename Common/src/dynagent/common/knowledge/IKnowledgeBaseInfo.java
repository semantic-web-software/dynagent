/**
 * IKnowledgeBaseInfo.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 * @description Esta es la interfaz que propone Jose Antonio y que Alfonso solicita 
 * 				que acceda directamente al motor de reglas JESS.
 */
package dynagent.common.knowledge;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import javax.naming.NamingException;
import org.jdom.JDOMException;


import dynagent.common.basicobjects.CardMed;
import dynagent.common.basicobjects.ColumnProperty;
import dynagent.common.basicobjects.EssentialProperty;
import dynagent.common.basicobjects.Groups;
import dynagent.common.basicobjects.ListenerUtask;
import dynagent.common.basicobjects.OrderProperty;
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
import dynagent.common.process.IAsigned;
import dynagent.common.properties.Property;
import dynagent.common.properties.values.Value;
import dynagent.common.sessions.EmailRequest;
import dynagent.common.sessions.Session;
import dynagent.common.utils.IBatchListener;
import dynagent.common.utils.INoticeListener;
import dynagent.common.xml.QueryXML;

public interface IKnowledgeBaseInfo {
	
	public void loadMetaData() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, SQLException, NamingException;
	
	public void setServer(docServer server);
	
	public docServer getServer();
	
	public void setLocalServer(docServer server);
	
	public docServer getLocalServer();
	
	public void setAsigned(IAsigned asigned);

	/**
	 * Devuelve un iterador con los identificadores de los objetos adscritos a una clase (obtendrá tanto los individuos como los prototipos)
	 * @param idto: Identifiador tipo objeto
	 * @return: Iterator con los identificadores (ido) de los objetos adscritos a la clase idto
	 */
	public HashSet <Integer> getIndividualsOfLevel(int idto,int level);
	
	
	
	/**
	 * Obtiene el nombre de clase a partir de su identificador numérico
	 * @param: idto identificador de la clase
	 * @return: nombre de la clase
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 */
	public String getClassName(int id) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;;
	
	
	/**
	 * Obtiene el nombre de la propiedad a partir de su identificador
	 * @param idProp: identificador de la propiedad
	 * @return
	 */
	public String getPropertyName(int idProp) throws NotFoundException ;
	
	
	/**
	 * Obtiene la propiedad inversa (si existe) de una propiedad a partir de su de su identificador
	 * @param idProp: identificador de la propiedad
	 * @return: Integer: idPropInversa (si no existe devuelve null)
	 */
	public Integer getPropertyInverse(int idProp) ;
	
	
	
	/**
	 * Obtiene un iterador de enteros con las clases superiores de las que deriva una clase. 
	 * @param: idto identificador de la clase.
	 * @return: Iterator con los identificadores de las clases superiores.
	 * @throws IncoherenceInMotorException 
	 */
	public java.util.Iterator <Integer> getSuperior(int idto) throws NotFoundException, IncoherenceInMotorException;
	
	public HashSet<Integer>getDirectSuperior(int idto) throws NotFoundException, IncoherenceInMotorException;
	
	public HashSet<Integer>getDirectSpecialized(int idto) throws NotFoundException, IncoherenceInMotorException;
	
	/**
	 * getSpecialized: Obtiene los identificadores de las clases que son especializadas de una dada.
	 * @param: idto identificador de la clase
	 * @return: Lista de enteros con los identificadores de las clases que son especializadas de la dada.
	 * Obtiene las clases que son derivadas de i. Excluyendose ella misma.
	 * @throws IncoherenceInMotorException 
	 */
	
	public HashSet<Integer> getSpecialized (int id) throws NotFoundException, IncoherenceInMotorException;
	
	
	/**
	 * Construye filtros de las clases que especializan de ese id.
	 * @param ido
	 * @param userRol
	 * @param user
	 * @param usertask
	 * @param session
	 * @return
	 * @throws NotFoundException
	 * @throws IncoherenceInMotorException
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	 */public ArrayList<Integer> getSpecializedFilters (int ido,Integer userRol,String user,Integer usertask,Session session) 
	 	throws NotFoundException, IncoherenceInMotorException,ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;
	
	
	
	/**
	 * isSpecialized: Devuelve si una clases es especializada de otra
	 * @param: idto: identificador de la clase
	 * @param: idtoPosSuperior: identificador de la posible clase superior
	 * @return: boolean indicando si idto es una clase especializda de idtoPosSuperior
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	 */
	public boolean isSpecialized(int idto, int posSuperior) throws  IncoherenceInMotorException;
	
	
	
	/**************************************************************************************************************************
	 *  Devuelve un iterador sobre  todas las propiedades del individuo (ó clase) cuya id se le pasa como parámetro y sobre las que
	 *  el usuario dentro de una userTask tiene al menos permiso de lectura.
	 *   Parámetros:
	 *     - int ido: identificador de la clase o del individuo del que se requieren sus propiedades.
	 *     - int idProp: identificador de la propiedad.
	 *    
	 *   Return: 
	 *      - Un Iterador de Objetos Property.
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public java.util.Iterator<Property> getAllPropertyIterator(Integer ido, int idto,Integer userRol,String user,Integer usertask, Session sessionPadre)
		throws NotFoundException, IncoherenceInMotorException,ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;;
	
	/**
	 * Devuelve las propiedades  de un  individuo/clase/filtro  cuya id se le pasa como parámetro.
	 * Chequea permisos y solo devuelve aquellas sobre las que al menos tiene permiso de lectura.
	 * Parámetros:
	 *    *  int id: identificador de la clase/individuo/filtro  del que se requiere la propiead.
	 *    *  int userRol: pérfil con el que está logado el  usuario.
	 *    *  int user: usuario logado
	 *    *  int userTask: identificador de la tarea en la que se encuentra.
	 *  Return: 
	 *     Un Iterador de Objetos Property.
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	public Property getProperty(Integer ido,int idto,int idProp,Integer userRol,String user,Integer usertask,Session s) 
		throws NotFoundException, IncoherenceInMotorException,ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;
	
	
	
	
	
	/** Especializa un objeto(prototipo, filtro, individuo)  que está adscrito a una clase a una clase especializada de ésta. 
	 * Podría incluso no estar inicialmente adscrito a ninguna clase (esto es, estar adscrito a la clase thing y no tendría propiedades ). 
	 * @param idto: identificador de la clase en la que vamos a especializar el prototipo.
	 * - Si en motor está adscrito a alguna clase habrá que cambiar esos fact para asignarle como idto el de la clase en que se quiere especializar y 
	 * además crearle las propiedades de la clase especializada que no tuviera previamente. 
	 */
	public int specializeIn(int id, int idtoSpecialized) ;
	
	
	
	
	
	
	
	
	/**
	 * Modifica, añade o elima según la operation deducida un Value a una Property.
	 * La operation que se deducirá será:  <br>
	 * &nbsp;  NEW (si oldValue=null) <br>
	 * &nbsp;  DEL (si newValue=null)  <br>
	 * &nbsp; SET (si oldValue!=null y  newValue!=null) <br>
	 * 
	 * @param: int ido  -identificador del objeto 
	 * @param: int idProp -identificador de la propiedad
	 * @param Integer rol: Identificador (si procede) del rol
	 * @param: Integer idoRel: identificador (si procede) de la relación
	 * @param: Value: viejo valor
	 * @param Value: nuevo valor
	 * @param: Integer userRol: identificador del userRol bajo el que se encuentra el usuario
	 * @param: String user: nombre(login) del usuario logado
	 * @param: Integer usertask: identificador de la usertask
	 * @throws CardinalityExceedException, OperationNotPermitedException,IncompatibleValueException
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 */

	  public void setValue(int ido,int idto,int idProp, Value oldValue,Value newValue, Integer userRol,String user,Integer usertask, Session s) 
	  	throws CardinalityExceedException, OperationNotPermitedException,IncompatibleValueException,NotFoundException,IncoherenceInMotorException,ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException;

	
	
	
	/**
	 * Obtiene el access sobre un objeto dentro de una usertask
	 * @param id: identificador del objeto
	 * @param userRol: Integer -perfil de usuario que tiene en ese momento el usuario logado
	 * @param user: String -Usuario logado
	 * @param usertask: Integer -Identificador de la usertask 
	 * @return
	 * @throws IncoherenceInMotorException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 */
	public  dynagent.common.knowledge.access getAccessOverObject(Integer id, Integer userRol,String user,Integer usertask) throws  NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;
	
	/**
	 * Crea un prototipo de la clase cuyo idto se le pasa como parámetro. Si esta clase participa en una Relation (puede deducirse  a través de la definición de userTask 
	 *  correspondiente) también se creará un prototipo de esa Relation y se le asociarán a las clases también las propiedades de rol correspondiente. Por cada objectProperty
	 *  que se tenga también creará un filtro  de la clase a la que apunta.
	 * @param idto: identificador de la clase de la que se quiere crear el prototipo.
	 * @param level: nivel con el que se quiere crear el prototipo 
	 * @return level
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 */	 
	public Integer createPrototype(int idto, int level,Integer userRol, String user, Integer usertask,Session sess) 
		throws NotFoundException, IncoherenceInMotorException,ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;;
	
	
	
	
	/**
	 * Obtiene los tipos de accesos que tiene un usuario sobre un objeto concreto
	 * @param ido: identificador del objeto sobre el que se quieren conocer los tipos de acceso
	 * @param user: usuario logado 
	 
	public HashMap<Integer,dynagent.knowledge.access> getAllAccessOverObject(Integer ido,Integer userRol,String user);
	 * @throws IncoherenceInMotorException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws OperationNotPermitedException 
	*/
	
		 
	public dynagent.common.knowledge.instance getTreeObject(int id, Integer userRol, String user, Integer userTask,Session sess) 
		throws NotFoundException, IncoherenceInMotorException,ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;
	
	
	
	
	
	
	/**
	 *  Obtiene consultando al motor si una propiedad es DataProperty
	 *@param: identificador de la propiedad
	 *@return: boolean indicando si es dataProperty o no
	 */
	public boolean isDataProperty(int idProp) ;
	
	/**
	 * Obtiene consultando al motor si una propiedad es ObjectProperty
	 *@param: identificador de la propiedad
	 *@return: boolean indicando si es objectProperty o no
	 */
	public boolean isObjectProperty(int idProp) ;
	
	
	
	
	public HashMap<Integer,ArrayList<dynagent.common.knowledge.UserAccess>> getUsertaskOperationOver(int id,String user) throws  NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
	 
	 
	 /**
	  * Devuelve el tipo de dato de una DataProperty concreta a partir de su idProp. 
	  * @param idProp: identificador de la dataProperty
	  * @return: Integer con el identificador de la propiedad (devolverá nulo si no existe esa propiedad o no es dataproperty)
	 * @throws NotFoundException 
	 * @throws IncoherenceInMotorException 
	  */
	 public Integer getDatatype(int idProp) throws NotFoundException, IncoherenceInMotorException ;
	 
	 public Integer getClassOf(int ido) throws IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException;
	 
	 
	 public QueryXML getQueryXML();
	 public Category getCategory(int idProp) throws  NotFoundException;
	 
	 public boolean isUnit(int cls) throws  NotFoundException, IncoherenceInMotorException ;
	 
	 public boolean isIDClass(int id);
	 
	 public void deleteObject(int id, int idto, String rdn, Integer userRol,String user, Integer usertask, Session sessionPadre)
	 	throws NotFoundException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException;	 
	 
	 
	 /**
		 * Devuelve el identificador del grupo de atributos al que pertenece una propiedad en caso de pertener a alguno de ellos.
		 * Si no pertenece a ningún grupo devuelve null.
		 * @param idProp
		 * @return
		 */
		public Integer getAtributteGroup(int idProp);
		
		
		
		public ArrayList<OrderProperty> getOrderProperties();
		
		
		public String getRdnIfExistInRuler(int ido);

		public boolean checkCoherenceObject(int ido,Integer userRol,String user, Integer usertask, Session session) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
		
		public Integer getLevelOf(int ido);
		
		public ArrayList<CardMed> getListCM();
	
		public Integer getIdProperty(String nameProp) throws NotFoundException, IncoherenceInMotorException;
		
		public Integer getIdClass(String name) throws NotFoundException, IncoherenceInMotorException;
		
		public ArrayList<ColumnProperty> getColumnProperties();
		
		public ArrayList<Groups> getGroupsProperties();
		
		public boolean isRangeCompatible(int iddominio,int idProp,int idrange) throws IncoherenceInMotorException;
		
		public Integer getMaxPropertyCardinalityOfClass(int idto,int idprop);
		
		public Integer getMinPropertyCardinalityOfClass(int idto,int idprop);
		
		//public boolean userModifyObject(int ido, boolean modify) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException;
		//public void validIndividual(int ido,boolean valid) throws NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException;
		public void setLock(int ido,boolean locked,String user,Session sessionPadre) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		
		public void setLock(ArrayList<Integer> idos,boolean locked,String user,Session sessionPadre) throws NotFoundException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		/**
		 * Indica si una clase tiene una propiedad dada
		 * @param idto: identificador de la clase
		 * @param idprop: identificador de la propiedad que se quiere saber si existe en la clase idto
		 * @return: true si  si es propiedad de esa clase
		 */
		public boolean hasProperty(int idto,int idprop);
		
		public void loadIndividual(int ido, int idto,int depth, boolean lock, boolean lastStructLevel,Integer userRol,String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
		public void loadIndividual(HashMap<Integer,HashSet<Integer>> idtoIdos, int depth, boolean lock, boolean lastStructLevel, Integer userRol,String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;

		public ArrayList<EssentialProperty> getEssentialProperties();
		
		public void setEssentialProperties(ArrayList<EssentialProperty> essentialProperties);
		
		public String getAliasOfProperty(int idto, int idProp, Integer usertask) throws NotFoundException;
				
		public String getAliasOfClass(int idto, Integer usertask) throws NotFoundException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, CardinalityExceedException;
		
		public String getAliasOfGroup(int group, String nameGroup, Integer usertask) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException;

		public void setValue(int ido, int idto,int idProp, LinkedList<Value> oldValues, LinkedList<Value> newValues, Integer userRol,
			String user, Integer usertask, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException;
		
		public boolean isGenericFilter(int ido) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, OperationNotPermitedException;
		
		public ArrayList<ListenerUtask> getListenerUtasks();

		public void setListenerUtasks(ArrayList<ListenerUtask> listenerUtasks);
		
		public boolean isPointed(int ido,int idto) throws NotFoundException, IncoherenceInMotorException;
		
		public boolean existInMotor(Integer id);
		
		public Integer setRange(int ido, int idto,int idProp, int valueCls,Integer userRol,String user,Integer userTask, int depth, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NumberFormatException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, NotFoundException, IncoherenceInMotorException, ParseException, SQLException, NamingException, JDOMException;
		
		public boolean isCompatibleWithFilter(int ido,instance instFilter,Integer userRol, String user, Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		
		public String getPropertyMask(int prop, Integer idto, Integer utask) throws NotFoundException;
		
		public Integer getPropertyLength(int prop, Integer idto, Integer utask);
		
		public HashSet<Integer> getAllIDsPropertiesOfClass(int idto) throws IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, OperationNotPermitedException;
		
		public Session getDefaultSession();
		
		public Session getRootSession();

		public IKnowledgeBaseInfo doClone() throws NotFoundException, IncoherenceInMotorException, EngineException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
		
		public void dispose();
		
		public boolean isDispose();
		
		public boolean isEnabled();
		
		public void setEnabled(boolean enabled);
		
		public String getUser();
		
		public void setQuestionListener(IQuestionListener listenerQuestion);
		
		public IQuestionListener getQuestionListener();
		
		public boolean isAbstract(int id) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, NotFoundException, OperationNotPermitedException;
		
		public void requestInformation(Integer ido,Integer idProp,Session session) throws NotFoundException, IncoherenceInMotorException, InstanceLockedException, OperationNotPermitedException, IncompatibleValueException, CardinalityExceedException, ApplicationException, SystemException, RemoteSystemException, CommunicationException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
		
		public HashSet<Integer>  loadNewData(ArrayList<IPropertyDef>instances, Integer userRol, String user,	Integer usertask,Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, dynagent.common.exceptions.CommunicationException, InstanceLockedException, DataErrorException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		
		public void addBatchListener(IBatchListener batchListener);
		
		public void removeBatchListener(IBatchListener batchListener);
		
		/**
		 * Devuelve una lista con los idtos hijos del idto pasado(o él mismo), que casan con el rango a asignar(value) en el idProp indicado 
		 * @param ido
		 * @param idto
		 * @param idProp
		 * @param value
		 * @param user
		 * @param userRol
		 * @param idtoUserTask
		 * @param ses
		 * @return
		 */
		public HashSet<Integer> getClassifiedIdtos(int idto,int idProp, Value value, String user, Integer userRol, Integer idtoUserTask, Session ses) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException;
		
		public Integer createFilter(int idto, Integer userRol, String user, Integer usertask, int depth, Session sessionPadre) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException;
		
		public void completeFilterLevels(int ido,int idto, Integer userRol, Integer userTask, int levels, Session sessionPadre) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		
		public void setPrintRules(boolean printRules);
		
		public boolean isPrintRules();
		
		public boolean sendEmail(Integer idoUserTaskReport, Integer idtoUserTaskReport, EmailRequest emailRequest, boolean showError) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
		
		public ArrayList<Integer> getGlobalUtasks();

		public void setGlobalUtasks(ArrayList<Integer> globalUtasks);
		
		public void addNoticeListener(INoticeListener noticeListener);
		
		public void removeNoticeListener(INoticeListener noticeListener);
}