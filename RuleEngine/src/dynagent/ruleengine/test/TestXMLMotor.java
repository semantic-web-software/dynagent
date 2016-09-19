import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.instance;


import dynagent.ruleengine.src.factories.IRulerFactory;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.ejb.FactoryConnectionDB;

import dynagent.server.services.InstanceService;
public class TestXMLMotor {
	public static void cargaMotor(int nbusiness,Integer userRol,String user,Integer usertask){
		try{
			
			Element metaDataXML=null;//instance.getMetaData(user, DAOManager.getInstance().getFactConnDB(), nbusiness);
			System.out.println("XML DE CARGA DEL MOTOR;\n");
			IKnowledgeBaseInfo m_kb=RuleEngineFactory.getInstance().createRuler(metaDataXML,nbusiness,null,Constants.RULER,user);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	
	public static void test(int nbusiness,Integer userRol,String user,Integer usertask){
		try{
			System.out.println("  Tests de los metodos de docDataModel con  los datos de la empresa="+nbusiness);
			String resp=Auxiliar.leeTexto("POR DEFECTO fichero de reglas es el de BBDD. DESEA QUE EN SU LUGAR CARGE LAS REGLAS DE  JobssQueries.drl(S/N)?");
			if(resp.equalsIgnoreCase("SI")||resp.equalsIgnoreCase("S")){
				Constants.setDEBUG_RULESINRULEENGINE(true);
			}
			Element metaDataXML=InstanceEJBAux.getMetaData(user, DAOManager.getInstance().getFactConnDB(), nbusiness);
			/*InstanceService docServerT=new InstanceService();*/
			//JESS O JBOSSRULES
			/*resp=Auxiliar.leeTexto("  Desea usar JBOSSRULES(S/N)?");
			if(resp.equalsIgnoreCase("S")){
				Constants.setRULER(Constants.RULERJBOSS);
			}*/
			IKnowledgeBaseInfo m_kb=RuleEngineFactory.getInstance().createRuler(metaDataXML,nbusiness,null,Constants.RULER,user);
			/*resp=Auxiliar.leeTexto("  Desea escribir en log(S/N)?");
			if(resp.equalsIgnoreCase("S")){
				Constants.setPrintLog(true);
			}*/
		
			
			
			resp=Auxiliar.leeTexto("  Desea ejecutar todos los test (S/N)?");
			if(resp.equalsIgnoreCase("S")){
				TestFactory.getInstance().runAll(m_kb, userRol,user,usertask);
			}
			else{
				System.out.println("  Introduzca el número de test que desea realizar"); 
				int ntest=Auxiliar.getNaturalNumber();
				TestFactory.getInstance().run(m_kb,ntest,userRol,user, usertask);
			}
			/*
			System.out.println("   -----------SE ha establecido hacer test 5 en TestXMLMotor.test");
			TestFactory.getInstance().run(m_kb,6,userRol,user, usertask);
			*/
		   
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		int nbusiness=12;
		String user="lola";
		String ip="192.168.1.3";
		FactoryConnectionDB fcdb=Menu.setConnection(""+nbusiness, ip);
		//TODO MACARENA en instanceEJBAUX que cuando usuario sea sistema no de problemas al buscar los USERROLES
		TestXMLMotor.test(nbusiness,null,user,null);
	}
}
