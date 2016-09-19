package dynagent.ruleengine.test.test5.src;

import java.util.ArrayList;
import java.util.Iterator;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.auxiliar.Auxiliar;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.KnowledgeAdapter;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.server.knowledge.instance.instance;

public class Test5 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask)throws NotFoundException, IncoherenceInMotorException {
		System.out.println("\n\n----------------------TEST 5:   PRUEBA MÉTODOS AUXILIARES PARA REGLAS.REQUIERE INTERVENCIÓN");
		DocDataModel ddm=(DocDataModel)ik;
		KnowledgeAdapter ka = new KnowledgeAdapter(ddm);
		if(SessionController.getInstance()!=null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
	
		boolean salir=false;
		
		
		String resp=Auxiliar.leeTexto("¿DESA REALIZAR ESTE TEST?: REQUIERE INTERVENCIÓN (S/N)");
		if(!resp.equalsIgnoreCase("S")||resp.equalsIgnoreCase("SI")){
			System.out.println(".............. TEST IGNORADO");
		}
		else{
			this.solicitaInfo(ddm);	
		}
	}
	
	
	
	public void solicitaInfo(DocDataModel ddm){
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
					Test5.editaObjeto(ido, ddm);
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
	
	
}
