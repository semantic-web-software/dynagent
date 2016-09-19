package dynagent.ruleengine.test.test23.src;

import java.util.LinkedList;

import jess.JessException;
import dynagent.ruleengine.Constants;

import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.server.exceptions.ApplicationException;

public class Test23 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException {
		DocDataModel ddm=(DocDataModel)ik;
		SessionController.getInstance().setActual(ddm);
	
		DefaultSession s1 = new DefaultSession(SessionController.getInstance().getActualSession().getID());
		SessionController.getInstance().Add(s1);
		SessionController.getInstance().setActual(s1);
		
		
		try {
			ddm.getRuler().getR().getR().executeCommand("(facts)");
			
			System.out.println("definir y ejecutar la regla");
			//ddm.getRuler().getR().getR().executeCommand("(defrule myrule ?i <-(instance (CLSREL nil) (VALUE nil)) => (printout t \"modificado :\"?i crlf) (modify ?i (CLSREL 6666)))");
			ddm.getRuler().getR().getR().executeCommand("(defrule myrule ?i <-(instance (IDO ?x&~nil)) => (modify ?i (CLSREL 6666)))");
			ddm.getRuler().getR().getR().run();
		
			//System.out.println("Despues de ejecutar las reglas:");
			//ddm.getRuler().getR().getR().executeCommand("(facts)");
			
			/*s1.rollBack();
			
			System.out.println("Despues del rollBack");
			ddm.getRuler().getR().getR().executeCommand("(facts)");*/
			
			//s1.commit();			
			System.out.println("Despues del commit");
			//ddm.getRuler().getR().getR().executeCommand("(facts)");
			
			
			
		} catch (JessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
		
		
	System.out.println("--------------FIN Prueba 23---------------");
		}
	}
}
