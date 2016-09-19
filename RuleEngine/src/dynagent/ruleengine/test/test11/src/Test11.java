package dynagent.ruleengine.test.test11.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.ITest;

public class Test11 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) {
		System.out.println("\n\n----------------------TEST 11:  PRUEBA DE CONSULTAS COMPLEJAS AL MOTOR--------------------");
		DocDataModel ddm=(DocDataModel)ik;
		// PRUEBA DE CONSULTAS COMPLEJAS AL MOTOR
		/*System.out.println("   - Consulta1: (instance (IDO ?i&:(and (neq ?i nil) (< ?i 0))))");
		LinkedList <dynagent.ruleengine.src.ruler.IPropertyDef> resConsult = ddm.getAllInstanceFacts("(instance (IDO ?i&:(and (neq ?i nil) (< ?i 0)))))");
		System.out.println("  resultado1="+resConsult);
		System.out.println("   - Consult2: (instance (IDO ?i&:(and (neq ?i nil) (and (> ?i -20)(< ?i 30)))))");
		resConsult = ddm.getAllInstanceFacts("(instance (IDO ?i&:(and (neq ?i nil) (and (> ?i -20)(< ?i 30))))))");
		System.out.println("  resultado2="+resConsult);*/
	}

}
