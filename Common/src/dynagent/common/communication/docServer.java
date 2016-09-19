package dynagent.common.communication;

import java.awt.Window;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.naming.NamingException;
import javax.swing.ImageIcon;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

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
import dynagent.common.knowledge.selectData;
import dynagent.common.properties.DomainProp;

/**
 * Interfaz cuyos métodos sirven para obtener los datos de una consulta, una instancia o un proceso.
 */

public interface docServer {
	
    public selectData serverGetQuery(/*String user, int empresa, */Element root, Integer uTask, int mode) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, JDOMException, SQLException, NamingException;
    
    public IndividualData serverGetFactsInstanceOfQuery(/*String user, int empresa, */Element root, Integer uTask) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
    
    //public void serverDeleteQuery(Integer userRol, String user, Element root) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, IncompatibleValueException, CardinalityExceedException, NotFoundException, IncoherenceInMotorException, ApplicationException, OperationNotPermitedException;
    
    public HashMap<String,String> serverGetReport(Element queryWhere, String user, Integer userTask, String className, /*String nameProject,*/ boolean directImpresion, Integer idoFormat, boolean printSequence,boolean ejecuta_pre_seq) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException;
    
    public HashMap<String,String> serverGetPrePrintSequence(String className) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException;
    
    public IndividualData serverGetFactsInstance(HashMap<Integer,HashSet<Integer>> idtoIdos, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) throws SystemException, NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException;
    
    public IndividualData serverGetFactsInstance(int ido, int idto, String user, boolean lock, int levels, boolean lastStructLevel, boolean returnResults) throws SystemException, NotFoundException,IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException;
    
    public IndividualData serverGetFactsInstanceOfClass(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException;
    
	public IndividualData serverGetFactsInstanceOfClassSpecialized(int idto, String user, boolean lock, int levels, boolean lastStructLevel) throws SystemException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException;
	
    public selectData serverGetTasks(Integer userRol, String user) throws InstanceLockedException, NotFoundException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException;
    
    public Changes serverTransitionObject(Integer userRol, String user, IndividualData datosIndiv, Integer windowSession, boolean migration, boolean keepTableIds, String replicaOrigin, String msguid) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException;
	
	public Changes serverTransitionObject(String user, Document document, ArrayList<Reservation> aReservation, Integer windowSession, boolean migration, boolean preprocess, boolean keepTableIds, String replicaOrigin) throws InstanceLockedException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, OperationNotPermitedException, DataErrorException, SQLException, NamingException, JDOMException;
	
	//public void deleteObligated(Integer userRol, String user, ArrayList<Integer> aIdos) throws SQLException, NamingException, DataErrorException, NotFoundException, SystemException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, RemoteSystemException, CommunicationException, InstanceLockedException, JDOMException, ParseException, OperationNotPermitedException, InterruptedException;
    
	public void incrementValue(int ido, int idto, int idProp, int incr) throws NotFoundException, SystemException, IncoherenceInMotorException;
	
    public void lockObject(int ido, Integer idto, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void lockObject(HashMap<Integer,HashSet<Integer>> listIdo, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void unlockObjects(HashMap<Integer,HashSet<Integer>> listIdo, String user) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
    
    public void logError(String user, String debug, String error, String subject) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
    
    public void logError(Window window,Exception e, String subject);
    
	public IteratorQuery serverGetIteratorQuery(String sql,boolean update) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public ArrayList serverGetRules(ArrayList<String> rulesFiles);
	
	public ImageIcon getIcon(ImageObserver obs, String icon, int maxAncho,int maxAlto);
	
	public String serverGetRdn(int ido, int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public HashMap<Integer,String> serverGetRdn(HashMap<Integer,HashSet<Integer>> listIdo) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public String serverGetClassDescription(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public HashMap<Integer,String> serverGetPropertiesDescriptionOfClass(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public String serverGetPropertyDescription(int idProp) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
    
	public Integer sendDataTransition(Element xmlData) throws SystemException, RemoteSystemException, CommunicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, IOException, JDOMException;
	
	public String serverUploadFile(String filePath,int datatype) throws SystemException, RemoteSystemException;

	public HashMap<DomainProp, Double> reserve(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws SystemException, RemoteSystemException, DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	
	public void deleteReservation(ArrayList<Reservation> reservationList, String user, Integer windowSession) throws SystemException, RemoteSystemException, DataErrorException, NotFoundException, SQLException, NamingException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	
	public void serverReportsClasificator() throws SystemException, RemoteSystemException, CommunicationException, SQLException, NamingException, IOException, JDOMException;
	
	public HashMap<Integer,String> serverGetIndividualsDescriptionOfClass(int idto) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;
	
	public String serverGetIndividualDescription(int ido) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException;
	
	public void changeMode(String mode) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;

	public long serverGetCurrentTimeMillis() throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, NoSuchFieldException;
	
	public String getSubscription();
	
	public boolean sendEmail(int ido, int idto, String reportFileName, String email, String title, String message, int idoMiEmpresa, int idoDestinatario, boolean showError) throws DataErrorException, SQLException, NamingException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException;

	public boolean sendEmailWithServerLog(String email, String subject, String body) throws SystemException, RemoteSystemException, SQLException, NamingException, DataErrorException;
}
