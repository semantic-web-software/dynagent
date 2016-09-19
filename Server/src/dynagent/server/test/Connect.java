package dynagent.server.test;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.database.DataBaseForRuler;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class Connect {

	public static void connectRuler(FactoryConnectionDB factConnDB, InstanceService m_IS) { 
		try {
			IKnowledgeBaseInfo ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(fcdb,fcdb.getBusiness()), factConnDB.getBusiness(), 
														m_IS, Constants.RULER, Constants.USER_SYSTEM, "query.dpkg", null);
			m_IS.setIk(ik);
		} catch(Exception e) {
	      	e.printStackTrace();
  		}
	}
}
