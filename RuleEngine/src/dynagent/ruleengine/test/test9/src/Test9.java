package dynagent.ruleengine.test.test9.src;

import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.ITest;

public class Test9 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException {
		// PRUEBA HASDEPENDENTVALUE
		DocDataModel ddm=(DocDataModel)ik;
		System.out.println("* PRUEBA HASDEPENDENTVALUE (100)");
		/*System.out.println("(1) SIN MODIFICACION DE LOS FACTS");
		LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> factins = ddm.getRuleEngine().getAllInstanceFacts(null, null, 100, null, null, null, null, null);
		Iterator itf = factins.iterator();
		while(itf.hasNext()){
			dynagent.ruleengine.src.ruler.IPropertyDef fact = (dynagent.ruleengine.src.ruler.IPropertyDef)itf.next();
			System.out.println(fact.toInstanceString());
			if(ddm.hasDependentValue(fact))
				System.out.println("(has-dependent-value)");
			else
				System.out.println("(no-dependent-value)");
		}
		System.out.println("(2) MODIFICACION DE LOS FACTS: VALUE = -1");
		factins = ddm.getRuleEngine().getAllInstanceFacts(null, null, 100, null, null, null, null, null);
		itf = factins.iterator();
		while(itf.hasNext()){
			dynagent.ruleengine.src.ruler.IPropertyDef fact = (dynagent.ruleengine.src.ruler.IPropertyDef)itf.next();
			fact.setVALUE("-1");
			System.out.println(fact.toInstanceString());
			if(ddm.hasDependentValue(fact))
				System.out.println("(has-dependent-value)");
			else
				System.out.println("(no-dependent-value)");
		}*/

	}

}
