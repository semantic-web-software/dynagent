/***
 * RulerFactoryXML.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.factories;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.jdom.Element;
import org.jdom.JDOMException;

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
import dynagent.common.knowledge.IQuestionListener;
import dynagent.common.knowledge.IDataFromDataBase;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.IMessageListener;
import dynagent.common.utils.DebugLog;

public class RuleEngineFactory {

	
	private static RuleEngineFactory instance = null;

	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static RuleEngineFactory getInstance(){
        if(instance == null)
            instance= new RuleEngineFactory();
        return instance;
    }
    
	/**
	 * RuleEngineFactory
	 * @return an instance of the factory, default constructor.
	 */
    private RuleEngineFactory() {}
	

	/**
	 * createRuler
	 * @param source representa el origen de los datos con los que se inicializa el motor. Puede ser un Element(xml) o un poolDB(bbdd)
	 * @param printRules 
	 * @return a object that implements IKnowledgeBaseInfoExtended interface
	 * @throws Exception 
	 * @throws IOException 
	 * @throws IncoherenceInMotorException 
	 * @throws NotFoundException 
	 * @throws EngineException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 * @throws CardinalityExceedException 
	 * @throws IncompatibleValueException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws NamingException 
	 * @throws SQLException 
	 * @throws ParseException 
	 * @throws JDOMException 
	 * @throws DataErrorException 
	 * @throws OperationNotPermitedException 
	 */
	public IKnowledgeBaseInfo  createRuler(Object source, Integer business, docServer server,String engine,String user, ArrayList<String> rulesFiles, DebugLog debugLog, IMessageListener messageListener, IQuestionListener questionListener, boolean printRules) throws NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, SQLException, NamingException, DataErrorException, JDOMException, ParseException, OperationNotPermitedException{
        IRulerFactory r = null;
        if(source instanceof Element) {
	        r = new RulerFactoryXML((Element)source, business, server,engine, user, debugLog, messageListener, questionListener, rulesFiles, printRules);
        } else if(source instanceof IDataFromDataBase) {
	        r = new RulerFactoryBBDD((IDataFromDataBase)source, business, server, engine, user, rulesFiles);
        }
        return r.getIKnowledgeBaseInfo();
        
    }	
    
    
    
}