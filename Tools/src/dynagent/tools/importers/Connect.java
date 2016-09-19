package dynagent.tools.importers;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;

import javax.naming.NamingException;

import org.jdom.JDOMException;

import dynagent.common.Constants;
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
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.database.DataBaseForRuler;
import dynagent.server.dbmap.DataBaseMap;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.ejb.ServerEngine;
import dynagent.server.services.InstanceService;

public class Connect {

	public static IKnowledgeBaseInfo connectRuler(FactoryConnectionDB factConnDB, InstanceService m_IS) throws SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, DataErrorException, NotFoundException, IncoherenceInMotorException, EngineException, IncompatibleValueException, CardinalityExceedException, OperationNotPermitedException, SQLException, NamingException, JDOMException, ParseException { 
		DataBaseMap dataBaseMap = new DataBaseMap(factConnDB, false);
		m_IS.setDataBaseMap(dataBaseMap);
		
		IKnowledgeBaseInfo ik = null;
		//try {
			ArrayList<String> rulesList=new ArrayList<String>();
			rulesList.add("query.dpkg");
			ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(factConnDB, dataBaseMap, factConnDB.getBusiness()), factConnDB.getBusiness(), 
					m_IS, Constants.RULER, Constants.USER_SYSTEM, rulesList, null, null, null, true);
			m_IS.setIk(ik);
		//} catch(Exception e) {
	    //  	e.printStackTrace();
  		//}
		return ik;
	}
	public static IKnowledgeBaseInfo connectRulerServer(FactoryConnectionDB factConnDB, InstanceService m_IS) throws NotFoundException, IncoherenceInMotorException, SQLException, NamingException { 
		IKnowledgeBaseInfo ik = null;
		//try {
			ik = new ServerEngine(factConnDB);
			m_IS.setIk(ik);
		//} catch(Exception e) {
	    //  	e.printStackTrace();
  		//}
		return ik;
	}
}
