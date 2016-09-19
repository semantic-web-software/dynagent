/***
 * TestDBMotor.java
 * @author: Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.test;

import dynagent.ruleengine.Constants;

import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.src.data.dao.DAOManager;
import dynagent.ruleengine.src.factories.IRulerFactory;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.database.DataBaseForRuler;


public class TestDBMotor {
	
	public static void cargaMotor(int nbusiness,Integer userRol,String user,Integer usertask){
		try{
			
			System.out.println("\n  Carga de Motor con los datos de la empresa="+nbusiness+"  para el usuario="+user);
			IKnowledgeBaseInfo ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(DAOManager.getInstance().getFactConnDB(),nbusiness), nbusiness, null,Constants.RULER,Constants.USER_SYSTEM);
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	public static void testCoherenciaModeloImportado(int nbusiness,Integer userRol,String user,Integer usertask) {
		try{
			System.out.println("\n  Carga de Motor con los datos de la empresa="+nbusiness);
			IKnowledgeBaseInfo ik = RuleEngineFactory.getInstance().createRuler(DAOManager.getInstance().getFactConnDB(), nbusiness, null,Constants.RULER,Constants.USER_SYSTEM);
			TestImportacion.run(ik,userRol,user,usertask);
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void test(int nbusiness,Integer userRol,String user,Integer usertask){
		try{
			
			System.out.println("  Tests de los metodos de docDataModel con  los datos de la empresa="+nbusiness+"  para el usuario="+user);
			IKnowledgeBaseInfo ik= RuleEngineFactory.getInstance().createRuler(DAOManager.getInstance().getFactConnDB(), nbusiness, null,Constants.RULER,Constants.USER_SYSTEM);
		   String resp=Auxiliar.leeTexto("  Desea ejecutar todos los test (S/N)?");
			if(resp.equalsIgnoreCase("S")){
				TestFactory.getInstance().runAll(ik,userRol,user,usertask);
			}
			else{
				System.out.println("  Introduzca el número de test que desea realizar"); 
				int ntest=Auxiliar.getNaturalNumber();
				TestFactory.getInstance().run(ik,ntest,userRol,user,usertask);
			}
		   
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
