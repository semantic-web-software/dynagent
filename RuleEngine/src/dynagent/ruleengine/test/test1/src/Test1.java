package dynagent.ruleengine.test.test1.src;

import java.util.Iterator;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.src.ruler.IRuleEngine;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.ruleengine.RuleEngineLogger;

public class Test1 implements ITest {
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) {
		if(SessionController.getInstance()==null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
		DocDataModel ddm=(DocDataModel)ik;
		try {
			
			ObjectValue v1=new ObjectValue();
			ObjectValue v2=new ObjectValue();
			System.out.println("Cd ambos tienen mismos nulos:  "+v1.equals(v2));
			v1.setValue(33);
			System.out.println("Cd value primero no es nulo y el del segundo es nulo: "+v1.equals(v2));
			v2.setValue(33);
			System.out.println("Ambos iguales  "+v1.equals(v2));
			
			
			
		//	ddm.getRuleEngine().printMotor();
			double inicio,fin,tiempos;
			System.out.println("\n\n-------------------TEST 0:  PRUEBA DE METODOS DE RULENGINE----------------------");
			IRuleEngine  ruleEngine=(IRuleEngine )ddm.getRuleEngine();
			//System.out.println(ddm.getMetaData());
			System.out.println("\n\n-------------------TEST 1:  PRUEBA DE CREACION DE PROTOTIPOS DE UTASK-----------------------");
			System.out.println("INICIO CREATEALLUTASKPROTOTYPES");
			inicio=System.currentTimeMillis();
			ddm.createAllUtaskPrototypes(userRol,user);
			fin=System.currentTimeMillis();
			System.out.println("FINAL CREATEALLUTASKPROTOTYPES");
			tiempos=(fin-inicio)/1000.0d;
			System.out.println("************FIN CREAR PROTOTIPOS DE TODAS LAS UTASK. Tiempo (segundos)"+tiempos);
			System.out.println("... numero de prototipos/filtros creados "+ddm.protos_filtroscrados.size());
			
			System.out.println("************GET LINKS BETWEENIDOS************");
    		inicio=System.currentTimeMillis();
    		ddm.printLinks(ddm.getLinksBetweenIdos());
    		fin=System.currentTimeMillis();
    		tiempos=(fin-inicio)/1000.0d;
    		System.out.println("************fin getLinksBetweenIdos. Tiempo (segundos)"+tiempos); 
    		System.out.println("\n  ===== MAPA DE LINKS CONCEPTUAL======");
    		inicio=System.currentTimeMillis();
    		ddm.printLinks(ddm.getLinksBetweenIdtos());
    		ddm.printLinks2(ddm.getLinksNameBetweenIdtos());
    		fin=System.currentTimeMillis();
    		tiempos=(fin-inicio)/1000.0d;
    		System.out.println("************FIN getLinksBetweenIdos. Tiempo (segundos)"+tiempos);
			
			// Obtenemos todas las clases que representan una UTASK (heredan de UTASK)
			
		    Iterator<Integer> itallUtasks=ddm.getSpecialized(Constants.IDTO_UTASK);
		    System.out.println(ddm.getSpecializedHS(Constants.IDTO_UTASK).size());
			for(Iterator itallUtask;itallUtasks.hasNext();){
				Integer utask=itallUtasks.next();
				//EL NIVEL DE LOS OBJETOS QUE SE PIDE DEBE COINCIDIR CON EL QUE SE LES ASIGO EN CREATEALLUTASKPROTOTYPES
				Iterator<Integer> idos=ddm.getIndividualsOfLevel(utask, Constants.LEVEL_FILTER);
				while(idos.hasNext()){
					Integer ido=idos.next();
					System.out.println("**************************  LA UTASK="+utask+"("+ik.getClassName(utask)+") tiene creado el prototipo="+ido);
					ddm.mostrarInfoSobreId(ido,null,null,null);
				}
			}
				} catch (NotFoundException e) {
			e.printStackTrace();
	} catch (Exception e2) {
		e2.printStackTrace();
}
	}
	
}
