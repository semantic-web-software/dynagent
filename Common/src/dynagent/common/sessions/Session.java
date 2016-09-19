package dynagent.common.sessions;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import javax.naming.NamingException;

import org.jdom.JDOMException;

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
import dynagent.common.properties.DomainProp;


public interface Session extends IChangePropertyListener,IMessageListener{
	/*public static final int APPLET_SESSION=1;
	public static final int INTERNAL_SESSION=2;
	public static final int SERVER_SESSION=3;
	public static final int ADAPTER_SPECIALIZE_SESSION=4;
	public static final int VIEW_MODE=5;
	public static final int SET_MODE=6;*/
	public static int USE_STATE=0;
	public static int FINISHING_STATE=1;
	public static int FINISH_STATE=2;
	
	public void childSessionClosed(int id,boolean commit,boolean createNewMotor,boolean possibleRunDDBB) throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
	public int getID();
	public int getIDMadre();
	public void addSessionable(Sessionable s);
	public HashSet <Sessionable> getSesionables();
	public void setIDMadre(int madre);
	public void setID(int id);
	public void addIchangeProperty(IChangePropertyListener a,boolean history) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	public void removeIchangeProperty(IChangePropertyListener a);
	//public void changeValue(Integer ido,int idto,int idProp,int valueCls,Value value,Value oldValue,int operation);
	public Integer getUtask();
	public void setUtask(Integer utask);	
	public boolean commit() throws ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
	public void rollBack()throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException;
	public boolean isCheckCoherenceObjects();
	public void setCheckCoherenceObjects(boolean checkCoherenceObjects);
	public boolean isLockObjects();
	public void setLockObjects(boolean lockObjects);
	public boolean isRunRules();
	public void setRunRules(boolean runRules);
	public void addIMessageListener(IMessageListener i);
	public void removeIMessageListener(IMessageListener i);
	public void sendMessage(String message) ;
	public boolean isFinished();
	public boolean isDeleteFilters();
	public void setDeleteFilters(boolean deleteFilters);
	public boolean soyHerederoDe(int idSessionPadre);
	public boolean somosHermanos(int idHermano);
	public boolean isForceParent();
	public void setForceParent(boolean forceParent);
	public void checkCoherence(boolean force) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, DataErrorException, ParseException, SQLException, NamingException, JDOMException;
	public boolean hasLockObject(int ido,int idto);
	public void lockObject(int ido,int idto) throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	public void unlockObjects() throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;
	public void addLocks(HashMap<Integer,HashSet<Integer>> locks);
	public void removeLocks(HashMap<Integer,HashSet<Integer>> locks);
	public void addISessionStateListener(ISessionStateListener a) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	public void removeISessionStateListener(ISessionStateListener a);
	public int getState();
	public String getRulesGroup();
	public void setRulesGroup(String rulesGroup);
	public boolean isNotifyChanges();
	public void setNotifyChanges(boolean notifyChanges) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;

	public IKnowledgeBaseInfo getKnowledgeBase();
	public boolean isReusable();
	public void setReusable(boolean reusable);
	public void dispose();
	
	public boolean isChildOfDDBBSession();
	
	public HashMap<DomainProp, Double> sendReservations(LinkedHashMap<String, Reservation> reservationList) throws SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException;
	public void cancelReservations() throws SystemException, RemoteSystemException, DataErrorException, InstanceLockedException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException;
	public void addReservations(LinkedHashMap<String, Reservation> oldReservationList);
	public void removeReservations(LinkedHashMap<String, Reservation> reservationList);
	public void removePendingLocks(HashMap<Integer, HashSet<Integer>> pendingLocks);
	public void addPendingLocks(HashMap<Integer, HashSet<Integer>> pendingLocks);
	public void sendPendingLocks() throws InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, ApplicationException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException;

	public void addEmailRequest(EmailRequest emailRequest);
	public void addEmailRequests(HashMap<Integer, EmailRequest> emailRequestMap);
	public void removeEmailRequests(HashMap<Integer, EmailRequest> emailRequestMap);
	
	public void addNoticeMessage(String noticeMessage);
	public void addNoticeMessages(HashSet<String> noticeMessages);
	public void removeNoticeMessages(HashSet<String> noticeMessages);
	
	public void notifyNoticeMessage(String message);

	public void setExecuteActionListener(IExecuteActionListener executeActionListener);
	public IExecuteActionListener getExecuteActionListener();
	public void requestExecuteAction(ExecuteActionRequest executionActionRequest);
	public void addRequestExecuteActionList(ArrayList<ExecuteActionRequest> executeActionRequestList);
	public void removeRequestExecuteActionList(ArrayList<ExecuteActionRequest> executeActionRequestList);
	public void notifyExecuteAction();
}
