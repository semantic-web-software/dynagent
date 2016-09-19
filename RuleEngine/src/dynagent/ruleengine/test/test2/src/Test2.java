package dynagent.ruleengine.test.test2.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.Constants;

import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.auxiliar.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.server.knowledge.instance.instance;

public class Test2 implements ITest{

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		
		if(SessionController.getInstance()==null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
		DocDataModel ddm=(DocDataModel)ik;		
		// PRUEBA DE CREACION DE UN PROTOTIPO CONCRETO
	    System.out.println("\n\n----------------------TEST 2: PRUEBA DE CREACION DE  PROTOTIPOS CONCRETOS: Creación de prototipos de los targetClass");
	    double inicio,fin,tiempos;
	    Iterator<Integer> utasks=ddm.getSpecialized(Constants.IDTO_UTASK);
	    while( utasks.hasNext()){
	    	int utask= utasks.next();
	    	Iterator<Integer>idos=ddm.getIndividualsOfLevel(utask, Constants.LEVEL_FILTER);
	    	while(idos.hasNext()){
	    		int ido=idos.next();
	    		ObjectProperty tgclass=(ObjectProperty)ddm.getProperty(ido, Constants.IdPROP_TARGETCLASS, userRol, user, usertask);
	    		System.out.println("\n- LA UTASK="+utask+"    tiene tgclass=  "+tgclass);
	    		for(int i=0;i<tgclass.getRangoList().size();i++){
	    			int id=tgclass.getRangoList().get(i);
	    			System.out.println("...creación filtro con las caracterítiscas de id="+id);
	    			inicio=System.currentTimeMillis();
	    	    	int  idproto=ddm.createPrototype(id,Constants.LEVEL_FILTER, userRol,user,usertask);
	    	    	fin=System.currentTimeMillis();
	    	    	tiempos=(fin-inicio)/1000.d;
	    			System.out.println(".......se ha creado en (segundos)"+tiempos+"   con ido="+idproto+"  y sus propiedades son:");
	    	    	System.out.println(Auxiliar.IteratorToString(ddm.getAllPropertyIterator(idproto, userRol, user, usertask)));
	    	    	//ddm.mostrarInfoSobreId(idproto,null,null,null);
	    	    	/*instance tree=ddm.getTreeObject(idproto, userRol, user, usertask);
	    	    	System.out.println("argumento para newEvent=\n"+tree);
	    	    	System.out.println("...borramos el prototipo creado: deleteObject(id="+tree.getIDO());
	    	    	ddm.deleteObject(tree.getIDO());
	    	    	ddm.newEvent(tree, null, Constants.USER_SYSTEM, null,false);
	    	    	instance treeintroduced=ddm.getTreeObject(tree.getIDO(),  userRol, user, usertask);
	    	    	System.out.println("arbol introducido por newEvent=\n"+treeintroduced);*/
	    	    }
	    	}
	    }
	}

}
