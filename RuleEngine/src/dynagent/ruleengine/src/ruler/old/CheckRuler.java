package dynagent.ruleengine.src.ruler;


import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.src.factories.RuleEngineFactory;
import dynagent.server.database.DataBaseForRuler;
import dynagent.server.database.dao.DAOManager;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.gestorsDB.GestorsDBConstants;
import dynagent.server.services.InstanceService;

public class CheckRuler {
	
	public static IKnowledgeBaseInfo connectRuler(String databaseIP, FactoryConnectionDB factConnDB, InstanceService m_IS) { 
		IKnowledgeBaseInfo ik=null;
		try {
			ik = RuleEngineFactory.getInstance().createRuler(new DataBaseForRuler(factConnDB,factConnDB.getBusiness()), factConnDB.getBusiness(), 
														m_IS, Constants.RULER, Constants.USER_SYSTEM, Constants.RULER_SERVER, null, null);
			m_IS.setIk(ik);
			return ik;
		} catch(Exception e) {
	      	e.printStackTrace();
	      	return null;
	      	
  		}
	}
	
	
	public static FactoryConnectionDB setConnection(String snbusiness,String ip,String gestor){
		DAOManager.getInstance().setBusiness(snbusiness);
		FactoryConnectionDB fcdb = new FactoryConnectionDB(new Integer(snbusiness),true,ip,gestor);
		DAOManager.getInstance().setFactConnDB(fcdb);
		DAOManager.getInstance().setCommit(true);
		return fcdb;
	}
	
	
	
	public static IKnowledgeBaseInfo start(int business, String gestor) {
		//String databaseIP = "localhost";
		String databaseIP="192.168.1.3";
		FactoryConnectionDB fcdb = CheckRuler.setConnection(String.valueOf(business), databaseIP, gestor);/*new FactoryConnectionDB(business, true, null, gestor);*/
		InstanceService m_IS = new InstanceService(fcdb, null, false);
		IKnowledgeBaseInfo ik=CheckRuler.connectRuler(databaseIP, fcdb, m_IS);
		return ik;
		
	}
	
	
	public static void solicitaInfo(DocDataModel ddm){
		String resp;
		boolean salir=false;
		do{
			try {
				
				resp=Auxiliar.leeTexto("INTRODUZCA EL NAME DE LA CLASE (PARA CREAR) O EL ID DEL OBJETO (PARA EDITAR UNO EXISTENTE) CREAR(SALIR PARA TERMINAR");
				salir=resp.equalsIgnoreCase("SALIR");
				int ido;
				if(!salir){
					if(!Auxiliar.hasIntValue(resp)){
						ido= ddm.creaIndividualOfClass(resp, Constants.LEVEL_INDIVIDUAL);
					}
					else ido=new Integer(resp);
					CheckRuler.editaObjeto(ido, ddm);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}while(!salir);
	}

	
	
	
	
	public static void editaObjeto(int ido,DocDataModel ddm) throws NotFoundException, IncoherenceInMotorException{
		System.out.println("   Editamos objeto id="+ido);
		boolean salir=false;
		String prop;
		do{
			try {
				prop=Auxiliar.leeTexto("INTRODUZCA LA PROPIEDAD A LA QUE DESEA DARLE VALOR(SALIR PARA TERMINAR");
				salir=prop.equalsIgnoreCase("SALIR");
				if(!salir){
					if(prop.equalsIgnoreCase("ALL")){
						Iterator<Integer> props=ddm.getAllIDsPropertiesOf(ido).iterator();
						while(props.hasNext()){
							int idprop=props.next();
							String nameprop=ddm.getPropertyName(idprop);
							ddm.setValue(ido, nameprop,"any");
						}
					}
					else{
						String value=Auxiliar.leeTexto("INTRODUZCA EL VALOR QUE DESEA ASIGNAR (ANY CUALQUIERA) (SALIR PARA TERMINAR)");
						if(value.equalsIgnoreCase("del")||value.equalsIgnoreCase("delete")){
							value=Auxiliar.leeTexto("INTRODUZCA EL VALOR QUE DESEA BORRAR");
							ddm.delValue(ido,prop,value);
						}
						else{
							ddm.setValue(ido,prop,value);
						}
					}
						
				}
				//mostramos la info
				//ddm.mostrarInfoSobreId(ido);
				ddm.mostrarValoresDeID(ido);
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}while(!salir);
	}
	
	public static void main(String[] args) {
		try{			
			int business =6;
			String gestor = GestorsDBConstants.SQLServer;
			System.out.println("CONECCTRULER business="+business+"  gestorbd="+gestor+"\n");
			
			String res= Auxiliar.leeTexto("¿DESEA USAR COMO REGLAS ALLRULES del workspace de JaZamora en lugar de las reglas de bbdd? S/N");
			if (res.equals("S")|| res.equals("s")) {
				Constants.setDEBUG_RULESINRULEENGINE(true);	
			}
			
			
		DocDataModel ddm=(DocDataModel )start(business, gestor);
		CheckRuler.solicitaInfo(ddm);
			
		}catch(Exception e){
			System.out.println("EXCEP:"+e.getClass()+" mensaje:"+e.getMessage()+"\n");
			e.printStackTrace();
			System.exit(0);
		}
	}

}
