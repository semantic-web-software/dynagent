package dynagent.ruleengine.test.test12.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.ITest;
import dynagent.server.application.session;

public class Test12 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		System.out.println("\n\n----------------------TEST 12:  PRUEBA DE ELIMINACIÓN DE OBJETOS--------------------");
		DocDataModel ddm=(DocDataModel)ik;
		 for(Iterator<Integer> itallUtasks=ddm.getSpecialized(Constants.IDTO_UTASK);itallUtasks.hasNext();){
				Integer utask=itallUtasks.next();
				Iterator<Integer> idos=ddm.getIndividualsOfLevel(utask, Constants.LEVEL_PROTOTYPE);
				while(idos.hasNext()){
					Integer ido=idos.next();
					System.out.println("**************************   LA UTASK="+utask+"("+ik.getClassName(utask)+") tiene creado el prototipo="+ido);
					System.out.println("    ....se borrará dicho prototipo");
					ddm.deleteObject(ido);	
				}
		}
	}
}
