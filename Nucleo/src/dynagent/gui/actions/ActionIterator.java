package dynagent.gui.actions;

import gdev.gen.AssignValueException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.naming.NamingException;

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
import dynagent.gui.Singleton;
import dynagent.gui.actions.commands.ActionCommandPath;
import dynagent.gui.actions.commands.DataTransferCommandPath;
import dynagent.gui.actions.commands.DelCommandPath;
import dynagent.gui.actions.commands.FindCommandPath;
import dynagent.gui.actions.commands.FindRelCommandPath;
import dynagent.gui.actions.commands.NewCommandPath;
import dynagent.gui.actions.commands.NewRelCommandPath;
import dynagent.gui.actions.commands.RelCommandPath;
import dynagent.gui.actions.commands.ReportCommandPath;
import dynagent.gui.actions.commands.SetCommandPath;
import dynagent.gui.actions.commands.SetCommonCommandPath;
import dynagent.gui.actions.commands.SetMultipleCommandPath;
import dynagent.gui.actions.commands.ViewCommandPath;
import dynagent.gui.actions.commands.ViewCommonCommandPath;
import dynagent.gui.actions.commands.ViewMultipleCommandPath;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;
import dynagent.ruleengine.src.sessions.DDBBSession;
import dynagent.ruleengine.src.sessions.DefaultSession;

public class ActionIterator implements IStepListener,ListIterator<commandPath>{

	protected ArrayList<commandPath> m_commandList;
	protected int m_currentStep;
	protected Session session;
	
	protected ActionIterator(ArrayList<commandPath> commandList,Session session){
		this.session=session;
		m_commandList=commandList;
		m_currentStep=-1;
	}
    
    public static ActionIterator createInstance(commandPath cPath,KnowledgeBaseAdapter kba)  throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
    	ArrayList<commandPath> commandList=new ArrayList<commandPath>();
    	ActionIterator action=null;
    	commandList.add(cPath);
    	
		//Le creamos una sesion que sera hija de la sesion que envia el commandPath
    	//Esta nueva sesion sera la utilizada para realizar la accion
    	Session session=null;
    	if(cPath instanceof ViewCommandPath || cPath instanceof ViewCommonCommandPath || cPath instanceof ViewMultipleCommandPath)
    		session=kba.createDefaultSession(cPath.getSession(),cPath.getIdtoUserTask(),true, false, false, true,cPath.getSession() instanceof DDBBSession);
    	else if(cPath instanceof FindCommandPath || cPath instanceof FindRelCommandPath)
    		session=kba.createDefaultSession(cPath.getSession(),cPath.getIdtoUserTask(),false, cPath.getSession().isRunRules(), false, false,cPath.getSession() instanceof DDBBSession);
    	else if(cPath instanceof ReportCommandPath)
    		session=kba.createDefaultSession(cPath.getSession(),cPath.getIdtoUserTask(),false, true, false, true,cPath.getSession() instanceof DDBBSession);
    	else if(cPath instanceof DataTransferCommandPath)
    		session=kba.createDefaultSession(cPath.getSession(),cPath.getIdtoUserTask(),false, true, false, true,cPath.getSession() instanceof DDBBSession);
    	else if(cPath instanceof ActionCommandPath || cPath instanceof DelCommandPath || cPath instanceof NewCommandPath || cPath instanceof NewRelCommandPath ||
    			cPath instanceof RelCommandPath || cPath instanceof SetCommandPath || cPath instanceof SetCommonCommandPath || cPath instanceof SetMultipleCommandPath)
    		session=kba.createDefaultSession(cPath.getSession(),cPath.getIdtoUserTask(),true, true, true, true,cPath.getSession() instanceof DDBBSession);
    	else
    		System.err.println("ERROR !!!! ActionIterator:createInstance hay que añadir el command "+cPath.getClass());
    	
    	boolean success=false;
		try{
			cPath.setSession(session);

			action= new ActionIterator(commandList,session);
			success=true;
		}finally{
			if(!success)
				session.rollBack();
		}
		return action;
    }

    public boolean setResultStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException, NumberFormatException, AssignValueException {
		return false;
	}

	public boolean hasNext() {
		if(m_currentStep<m_commandList.size()-1)
			return true;
		else return false;
	}

	public commandPath next() {
		m_currentStep++;
		return m_commandList.get(m_currentStep);
	}

	public void remove() throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Operacion no permitida");
	}

	public void add(commandPath cPath){
		m_commandList.add(nextIndex(),cPath);
	}

	public boolean hasPrevious() {
		if(m_currentStep>0)
			return true;
		else return false;
	}

	public int nextIndex() {
		return m_currentStep+1; 
	}

	public commandPath previous() {
		m_currentStep--;
		return m_commandList.get(m_currentStep);
	}

	public int previousIndex() {
		return m_currentStep-1;
	}

	public void set(commandPath cPath) throws UnsupportedOperationException{
		throw new UnsupportedOperationException("Operacion no permitida");
	}

	public void endSteps() throws NotFoundException, ApplicationException, InstanceLockedException, SystemException, RemoteSystemException, CommunicationException, OperationNotPermitedException, IncompatibleValueException, DataErrorException, IncoherenceInMotorException, CardinalityExceedException, ParseException, SQLException, NamingException, JDOMException{
		// Si no se le ha hecho ni rollback ni commit le hacemos rollback.
		//TODO:Esto ocurria al hacer next o prev en operationGui.doWorkEvent pero ya se hace en transitionControl. Ver si sigue siendo necesario
		if(!session.isFinished())
			session.rollBack();
		
		if(session.getKnowledgeBase().isDispose()){//Si el motor ya no existe hacemos dispose de KnowledgeBaseAdapter quitandolo tambien del Singleton
			Singleton.getInstance().removeKnowledgeBaseAdapter(session.getKnowledgeBase()).dispose();
		}
	}

	public boolean isLastStep() {
		return !this.hasNext();
	}
	
	public boolean isMultiStep(){
		return m_commandList.size()>1;
	}

	public boolean setCancelStep(KnowledgeBaseAdapter kba,IFormData form) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException {
		return true;
	}

	public Session getSession() {
		return session;
	}
	

}
