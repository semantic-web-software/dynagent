package dynagent.server.ejb;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.ejb.EJBObject;
import javax.naming.NamingException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.basicobjects.License;
import dynagent.common.communication.Changes;
import dynagent.common.communication.IndividualData;
import dynagent.common.communication.Reservation;
import dynagent.common.communication.message;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.exceptions.MessageException;

public interface Instance extends EJBObject {
	
	public Element getMetaData(String user,boolean configurationMode,int business) throws RemoteException, MessageException;
	
	public IndividualData getObj(int tableId, int idto, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults, int business) throws RemoteException, MessageException;
	
	public IndividualData getObj(HashMap<Integer,HashSet<Integer>> id, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults, int business) throws RemoteException, MessageException;
	
	public IndividualData getObjOfClass(int idto, String user, boolean lock, int levels, boolean lastStructLevel, int business) throws RemoteException, MessageException;
	
	public IndividualData getObjOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel, int business) throws RemoteException, MessageException;
	
    public Integer transitionDataMigration(Element xmlData, int business) throws RemoteException, MessageException;
    	
    public Changes transitionObject(Integer userRol, String user, Object indData,String msguid, Integer windowSession, int bns, boolean migration, boolean keepTableIds, String replicaSource) throws RemoteException/*, InstanceLockedException, DataErrorException, OperationNotPermitedException*/;
	
    public selectData getTasks(Integer userRol, String user, int business) throws RemoteException, MessageException;
    
    public void lockObject(int ido, int idto, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;

    public void lockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void resetLocks(String user, int business) throws RemoteException, MessageException;
    
    public void resetReservations(String user, int business) throws RemoteException, MessageException;
    
    public void unlockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void unlockObject(int ido, int idto, String user, int business) throws RemoteException, MessageException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void logError(String user, String debug, String error, String subject, int business) throws RemoteException, MessageException;
	
	public String query(Element elem, Integer uTask, int mode, int business) throws RemoteException, MessageException;
	
	public List<List<String>> queryRules(String sql, int business,boolean update) throws RemoteException, MessageException;
	
	public HashMap<String,String> report(Element root, String user, int uTask, String className, /*String nameProject,*/ boolean directImpresion, Integer idoFormat, int business) throws RemoteException, MessageException;

	public HashMap<String,String> prePrint(String className, int business) throws RemoteException, MessageException;

	public message getUserInfo(String user, String pwd, boolean configurationMode, int business) throws RemoteException, MessageException;
    
    public License getLicense(int business) throws RemoteException, SystemException;
    
    public HashSet<String> getNumberOfSessions(int business) throws RemoteException, SystemException;
    
	public String getRdn(int ido, int idto, int business) throws RemoteException, SystemException;
	
	public HashMap<Integer,String> getRdn(HashMap<Integer,HashSet<Integer>> idos,int business) throws RemoteException, SystemException;
	
	public String getClassDescription(int idto, int business) throws RemoteException, SystemException;
	
	public String getPropertyDescription(int idProp, int business) throws RemoteException, SystemException;

	public HashMap<Integer,String> getPropertiesDescriptionOfClass(int idto, int business) throws RemoteException, SystemException;
	
	//MODIFIED Añadido para las pruebas del envio del JRXML
	public ArrayList<String> getJRXML(String user, Integer reportIdto, int business) throws RemoteException, MessageException;
	
	public void reportsClasificator(int business) throws RemoteException, SQLException, NamingException, IOException, JDOMException;
	
	public HashMap<DomainProp, Double> reserve(ArrayList<Reservation> reservationList, String user, Integer windowSession, int business) throws RemoteException, MessageException;
	
	public void deleteReservation(ArrayList<Reservation> reservationList, String user, Integer windowSession, int business) throws RemoteException, MessageException;

	public String getIndividualDescription(int ido, int business) throws RemoteException, SystemException;
	
	public HashMap<Integer,String> getIndividualsDescriptionOfClass(int idto, int business) throws RemoteException, SystemException;
	
	public void setLicense(int business,License license) throws RemoteException, SystemException;

	public boolean sendEmail(int ido, int idto, String reportFileName, String email, String subject, String body, int idoMiEmpresa, int idoDestinatario, int business) throws RemoteException;
	
	public boolean sendServerLogEmail(String email, String subject, String body, int business) throws RemoteException;
	
	public DataBaseMap getDataBaseMap(int business) throws RemoteException;
}
