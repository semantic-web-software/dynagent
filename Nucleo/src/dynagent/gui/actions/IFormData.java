package dynagent.gui.actions;

import gdev.gen.AssignValueException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import javax.naming.NamingException;
import javax.swing.JComponent;

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
import dynagent.common.sessions.Session;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;

public interface IFormData {

	public JComponent getComponent();
	
	public LinkedHashMap<Integer,Integer> getResult();
	
	public boolean cancel() throws ApplicationException, NotFoundException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException;
	
	public void startEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	
	public void stopEdition(int ido,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	
	public void startEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	
	public void stopEdition(ArrayList<Integer> idos,Session sess) throws ApplicationException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, DataErrorException, NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, ParseException, SQLException, NamingException, JDOMException;
	
	public WindowComponent getDialog();
	
	public Session getSession();
	
	public String confirm() throws NotFoundException, IncoherenceInMotorException, IncompatibleValueException, CardinalityExceedException, ApplicationException, OperationNotPermitedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, NumberFormatException, AssignValueException;
	
	public KnowledgeBaseAdapter getKnowledgeBase();
}
