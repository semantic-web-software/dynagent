package dynagent.ruleengine.test.test15.src;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.KnowledgeAdapter;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;

public class Test15 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws IncoherenceInMotorException {
		DocDataModel ddm=(DocDataModel)ik;
		KnowledgeAdapter ka = new KnowledgeAdapter(ddm);
		if(SessionController.getInstance()!=null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
		String MASK="(\\d{1,2}\\.?)(\\d{3}\\.?)(\\d{3})[a-zA-Z]";
		System.out.println("\n\n----------------------TEST 15:   PRUEBA CHEK VALORES Y CARDINALIDADES COMPATIBLES");
		boolean salir=false;
		
		do{
			try {
				String resp=Auxiliar.leeTexto("INTRODUZCA LA CLASE QUE QUE QUIERE TESTEAR");
				int idto;
				idto = ddm.getIdClass(resp);
				int ido= ddm.createPrototype(idto, Constants.LEVEL_INDIVIDUAL, userRol, user, usertask);
				resp=Auxiliar.leeTexto("INTRODUZCA LA PROPIEDAD A LA QUE DESEA DARLE VALOR(SALIR PARA TERMINAR");
				String value=Auxiliar.leeTexto("INTRODUZCA EL VALOR QUE DESEA ASIGNAR (SALIR PARA TERMINAR)");
				salir=resp.equalsIgnoreCase("SALIR")||value.equalsIgnoreCase("SALIR");
				if(!salir){
					ddm.addValue(ido, resp, value);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}while(!salir);
	}
	
}
