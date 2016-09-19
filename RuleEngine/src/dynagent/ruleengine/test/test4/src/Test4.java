package dynagent.ruleengine.test.test4.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.ITest;

public class Test4 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException {
	
		DocDataModel ddm=(DocDataModel)ik;
		System.out.println("\n\n----------------------TEST 4: PRUEBA DE ELIMINACION DE OBJETOS. Se creara prototipo de todas las utask y luego se eliminaran todos los prototipos creados.");
	    
		
		//PRUEBA CON FACT
		int ivalue=2;
		String svalue=String.valueOf(ivalue);
		System.out.println(".....Se crean los facts:");
		dynagent.ruleengine.src.ruler.IPropertyDef enlace=new dynagent.ruleengine.src.ruler.FactInstance(20, 2, 1, svalue,3, null, null,null,null);
		System.out.println(enlace);
		ddm.addFactToRuler(enlace);
		ivalue=-2;
		svalue=String.valueOf(ivalue);
		System.out.println("...insertamos un fact de prueba que asocia ido=-2 a clase 20");
		
		enlace=new dynagent.ruleengine.src.ruler.FactInstance(20, -2,  1, svalue,3, null, null, null,null);
		ddm.addIDOToIDTOWithLevel(-2, 20, 3);
		System.out.println(enlace);
		ddm.addFactToRuler(enlace);
	
		System.out.println("...boramos el objeto con id=-2");
		ddm.deleteObject(-2);
		LinkedList<Integer>allidos=new LinkedList<Integer>();
	    Iterator<Integer> itallUtasks=ddm.getSpecialized(Constants.IDTO_UTASK);
		//while(itallUtasks.hasNext();){
	    if(itallUtasks.hasNext()){
			Integer utask=itallUtasks.next();
			int idoUtask=ddm.createPrototype(utask, Constants.LEVEL_PROTOTYPE, null, Constants.USER_SYSTEM, null);
			System.out.println("...Se crea prototipo de la utask="+utask+" con ido="+idoUtask);
			allidos.add(idoUtask);	
		}

		//System.out.println("...Todos los  prototipos creados son"+allidos);
		//LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> factsWithIdoNeg=ddm.getAllInstanceFacts("(instance (IDO ?i&:(and (neq ?i nil) (< ?i 0)))))");
		LinkedList<dynagent.ruleengine.src.ruler.IPropertyDef> factsWithIdoNeg=ddm.getRuleEngine().getAllInstanceFacts(null, new Null(Null.NOTNULL), null, null, null, null,null,null,null);
			
		System.out.println("...Los facts sobre objetos (ido!=null) en motor son:"+factsWithIdoNeg.size());
		for(int i=0;i<factsWithIdoNeg.size();i++){
			//System.out.println(factsWithIdoNeg.get(i));
		}
		
		for(int i =0; i <allidos.size(); i++){
			System.out.println("...Se va a eliminar el prototipo con ido="+allidos.get(i));
			ddm.deleteObject(allidos.get(i));
		}
		System.out.println(".....Despu�s de borrar todos los prototipos creados");
		//factsWithIdoNeg=ddm.getAllInstanceFacts("(instance (IDO ?i&:(and (neq ?i nil) (< ?i 0)))))");
		factsWithIdoNeg=ddm.getRuleEngine().getAllInstanceFacts(null, new Null(Null.NOTNULL), null, null, null, null, null, null,null);
		System.out.println("..Los facts sobre objetos (ido!=null) en motor son: "+factsWithIdoNeg.size());
		for(int i=0;i<factsWithIdoNeg.size();i++){
		//	System.out.println(factsWithIdoNeg.get(i));
		}
	
	}
	
	
}
