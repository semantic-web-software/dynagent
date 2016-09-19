package dynagent.gui.actions;

import java.sql.SQLException;
import java.text.ParseException;

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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.gui.KnowledgeBaseAdapter;
import dynagent.gui.WindowComponent;
import dynagent.gui.actions.commands.commandPath;
import dynagent.gui.forms.utils.ActionException;

public class ActionStepFactory {

	private static ActionStepFactory instance = null;
	
	private ActionStepFactory(){
		
	}
	
	
//	public String type = "";
	
	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static ActionStepFactory getInstance(){
        if(instance == null){
            instance= new ActionStepFactory();
        }
        return instance;
    }
	
//	/**
//	 * getType()
//	 * @return the type of the object that can export 
//	 */
//	public String getType() {
//		return type;
//	}
//	
//	/**
//	 * setType()
//	 * @param type represents the type of the parser
//	 */
//	public void setType(String type) {
//		this.type = type;
//	}
//	

	public ActionIterator createActionStep(commandPath cPath, WindowComponent dialog, KnowledgeBaseAdapter kba) throws NotFoundException, IncoherenceInMotorException, ApplicationException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException, ActionException{
        ActionIterator itr = null;
        if((itr=ActionSourceTargetIterator.createInstance(cPath, kba,dialog))==null)
        	if((itr=ActionSpecializeIterator.createInstance(cPath,kba))==null)
        		if((itr=ActionQuestionTaskIterator.createInstance(cPath,kba))==null)
        			if((itr=ActionImportExportIterator.createInstance(cPath, kba,dialog))==null)
        				itr=ActionIterator.createInstance(cPath,kba);
        return itr;
    }	
}
