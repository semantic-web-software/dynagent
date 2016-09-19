package dynagent.ruleengine.test.test16.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.ConceptLogger;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.src.ruler.Fact;
import dynagent.ruleengine.src.ruler.IPropertyDef;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;

public class Test16 implements ITest {
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		if(SessionController.getInstance()==null){
			SessionController.getInstance().setActual(new DefaultSession(null,null));
		}
		DocDataModel ddm=(DocDataModel)ik;
		//	PRUEBA DATAVALUECOMPATIBLE
		System.out.println("\n\n----------------------TEST 16 PRUEBA AREASFUNCIONALES Y USERTASK");
		
		Iterator<Integer> itrAreasFuncionales = ddm.getIndividualsIterator(Constants.IDTO_FUNCTIONAL_AREA);
		System.out.println("------------=========================    AREAS FUNCIONALES: =========================-----------");
		while(itrAreasFuncionales.hasNext()){
			int ido=itrAreasFuncionales.next();
			System.out.println("     -Area Fuctional:  ido= "+ido);
			Property pr = ddm.getProperty(ido, Constants.IdPROP_RDN,userRol,user,  usertask);
			System.out.println(pr);
		
		System.out.println("------------=========================    USERTASKS: =========================-----------");
		Iterator <Integer>itrClassesUserTask=ddm.getSpecialized(Constants.IDTO_UTASK);
		while(itrClassesUserTask.hasNext()){
			Integer idtoTargetClass=null;
			ObjectProperty pTc= null,myfa= null,userrol= null;
			Integer idto=itrClassesUserTask.next();
			System.out.println("//---- USERTASK idto= "+idto+"-------\\");
			Iterator<Property> it=ddm.getAllPropertyIterator(idto,userRol,user,  usertask);
			while(it.hasNext()){
				
				Property prop = (dynagent.ruleengine.meta.api.Property)it.next();
				System.out.println(prop);
				if(prop.getIdProp().intValue()==Constants.IdPROP_TARGETCLASS){
					pTc = (ObjectProperty)prop;
				}
				else if(prop.getIdProp().intValue()==Constants.IdPROP_MYFUNCTIONALAREA){
					myfa=(ObjectProperty)prop;
				}
				else if(prop.getIdProp().intValue()==Constants.IdPROP_USERROL){
					userrol=(ObjectProperty)prop;
				}

				//INSTANCIAS DE LAS USERTAKS
				Iterator<Integer> itrUserTask = ddm.getIndividualsIterator(idto);
				while(itrUserTask.hasNext()){
					 ido=itrUserTask.next();
					System.out.println("    Tiene como instancia ido="+ido+"  con las propiedades:");
					
					Iterator<Property> itr = ddm.getAllPropertyIterator(ido,userRol,user,  usertask);
				}
			}
			//COHERENCIA VALORES
			if(pTc==null){
				System.out.println("   WARNING: Propiedad targetClass no definida en la usertask= "+idto);
			}
			else if(pTc.getRangoList().size()==1){
				idtoTargetClass = pTc.getRangoList().get(0);
			}
			else{
				System.out.println("   WARNING: Propiedad targetClass que apunta a "+pTc.getRangoList().size()+"  clases");
			}
			//SI EXISTE TGREL HAY QUE COMPROBAR QUE REALMENTE LA CLASE APUNTADA POR TGREL ES UN PLAYER DE ROL idtoTargetRol en la RELATION idtoTArgetREl
		System.out.println("\n\n---------------PRUEBAS COHERENCIA:-------------------------------");
		Iterator <IPropertyDef>factwithvalues=ddm.getRuleEngine().getAllInstanceFacts(null, null, null, null, new Null(Null.NOTNULL), null, null, null,null).iterator();
		while(factwithvalues.hasNext()){
			IPropertyDef f=factwithvalues.next();
			if(Auxiliar.hasIntValue(f.getVALUE())){
				int value=new Integer(f.getVALUE()).intValue();
				int valuecls=f.getVALUECLS();
				if(ddm.getLevelOf(value)==null){
					System.out.println("\n\n     WARNING:  No existe ido="+value+"  en motor pero es apuntado por el fact="+f);
				}
			}
		}
	}
}

	}}

