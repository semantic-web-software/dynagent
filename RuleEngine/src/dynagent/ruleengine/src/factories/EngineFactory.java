package dynagent.ruleengine.src.factories;

import dynagent.common.Constants;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.ruler.IRuleEngine;
import dynagent.ruleengine.src.ruler.JBossEngine;
//import dynagent.ruleengine.src.ruler.JessEngine;

public class EngineFactory {
	
	private static EngineFactory instance = null;
	
	private EngineFactory(){
		
	}
	
	
	public static EngineFactory getInstance(){
		if(instance==null)
			instance = new EngineFactory();
		return instance;
	}
	
	public IRuleEngine createRuleEngine(String engine,IKnowledgeBaseInfo ik){
		/*if(engine.equals(Constants.RULERJESS))
			return new JessEngine(null);
		else*/ if(engine.equals(Constants.RULERJBOSS))
			return new JBossEngine(ik);
		else 
			return null;
	}
}
