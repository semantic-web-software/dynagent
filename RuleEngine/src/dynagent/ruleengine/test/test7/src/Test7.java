package dynagent.ruleengine.test.test7.src;

import java.util.HashMap;
import java.util.Iterator;

import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.Category;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.src.ruler.FactProp;
import dynagent.ruleengine.test.ITest;
import dynagent.ruleengine.Constants;

public class Test7 implements ITest {
	
	public boolean checkCoherencePropInv(HashMap<Integer,Integer> hm){
		boolean coherence=true;
		Iterator<Integer>it=hm.keySet().iterator();
		while(it.hasNext()&&coherence){
			int idProp=it.next();
			int idPropInv=hm.get(idProp);
			Integer idPropInvOfIn=hm.get(idPropInv);
			if(idPropInvOfIn==null||idProp!=idPropInvOfIn.intValue()){
				System.out.println("\n\n     WARNING: Incoherencia en la definición de las propiedades inversas, la inversa de idProp="+idProp+"      es idPropInv="+idPropInv);
				System.out.println("     mientras que la inversa de idProp="+idPropInv +"   es="+idPropInvOfIn);
				
				coherence=false;
			}		
		}
		return coherence;
	}
	

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
		System.out.println("\n\n----------------------TEST 7:    PROPIEDADES Y CLASES-----------------");
		DocDataModel ddm=(DocDataModel)ik;
		Iterator<FactProp> itP=ddm.getAllProperties();
		HashMap mapaPropInv=new HashMap();
		System.out.println("\n  PROPIEDADES: ****************");
		while(itP.hasNext()){
			FactProp fp=itP.next();
			Category cat=new Category(fp.getCAT());
			System.out.println("  -  Propiedad: "+fp.getNAME()+"   tiene category="+cat+"   "+fp);
			if(!cat.isDataProperty()&&!cat.isObjectProperty()){
				System.out.println("\n\n     WARNING: La propiedad "+fp.getNAME()+ "  no se ha declarado ni como objectProperty ni como DataProperty");
			}
			
			//PROPxPROPINV
			if(fp.getPROPIN()!=null){
				mapaPropInv.put(fp.getPROP(), fp.getPROPIN());
			}
		}
		//COHERENCIA PROP INVERSAS
		System.out.println(" \n    ----------PROPIEDADES INVERSAS:----------\n Mapa Inversas="+mapaPropInv);
		if(!this.checkCoherencePropInv(mapaPropInv)){
			System.out.println("\n     WARNING: MODELO MAL DEFINIDO O MAL IMPORTAD !incoherencia en las def de prop inversas");
		}
		
		System.out.println("\n  CLASES ****************");
		//ITERADOR SOBRE CLASES Y PROPIEDADES
		Iterator<Integer> itc = ik.getClassIterator();
		while(itc.hasNext()){
			int idto=itc.next();
			System.out.println("\n  -------- CLASE (idto="+idto+", name="+ddm.getClassName(idto)+"  tiene las propiedades:   ---------");
			Iterator<Property> itp = ddm.getAllPropertyIterator(idto,null,Constants.USER_SYSTEM,null);
			if(itp!=null){
				while(itp.hasNext()){
					Property prop = (Property)itp.next();
					System.out.println(prop);
				}
			}else{System.out.println("        0 propiedades");}
		}

	}
}
