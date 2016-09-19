package dynagent.ruleengine.test.test10.src;

import java.util.Iterator;
import java.util.LinkedList;

import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.test.ITest;
import dynagent.ruleengine.Constants;

public class Test10 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws OperationNotPermitedException {
		System.out.println("\n\n----------------------TEST 10:  PRUEBA DE CAMBIOS DEL TIPO DE OBJETO--------------------");
		DocDataModel ddm=(DocDataModel)ik;
		/* PRUEBA DE POLIMORFISMO
		 * PARTE 1:
		 *  Cambio de tipo de Objeto ( SOFA2 ->  SOFACARIBE2)
			1) Crear un prototipo de sofa2
			2) Obtener todos los facts con información al respecto e imprimirla
				getAllInstanceIterator(ido)
			3) Cambiar este objeto creado (ido) al tipo SofaCAribe2
				changeTypeObject(ido,idtoSofaCaribe2);
			4) Obtener todos los facts con información sobre ido e imprimirla
				getAllInstanceIterator(ido).
		 */
		/*
		System.out.println("(1) CREACION DEL PROTOTIPO DE SOFA2");
		Integer idtosofa2;
		try {
			idtosofa2 = ddm.getIdClass("Persona");
		
			if(idtosofa2!=null){
				Integer ido = ik.createPrototype(idtosofa2, Constants.LEVEL_PROTOTYPE,null,null,null,ddm);
				System.out.println("(2) OBTENER LOS FACTS DE SOFA2 E IMPRIMIRLOS ");
				//LinkedList <dynagent.ruleengine.src.ruler.IPropertyDef> isofa2 = ddm.getAllInstanceFacts("(instance (IDO "+ido+")))");
				LinkedList <dynagent.ruleengine.src.ruler.IPropertyDef> isofa2 = ddm.getAllInstanceFacts(null, ido, null, null, null, null, null, null);
				Iterator itsofa2 = isofa2.iterator();
				while(itsofa2.hasNext()){
					System.out.println(((dynagent.ruleengine.src.ruler.Fact)itsofa2.next()).toInstanceString());
				}
				System.out.println("(3) CAMBIAR EL TIPO DE OBJETO SOFA2 A SOFACARIBE2 ");
				ddm.changeTypeObjectTo(ido, ddm.getIdClass("SOFACARIBE2"),null,null,null);
				System.out.println("(4) OBTENER LOS FACTS DE SOFACARIBE2 E IMPRIMIRLOS ");
				LinkedList <dynagent.ruleengine.src.ruler.IPropertyDef> isofac2 = ddm.getAllInstanceFacts("(instance (IDO "+ido+")))");
				Iterator itsofac2 = isofac2.iterator();
				while(itsofac2.hasNext()){
					System.out.println(((dynagent.ruleengine.src.ruler.Fact)itsofac2.next()).toInstanceString());
				}
				System.out.println("(5) CAMBIAR EL TIPO DE OBJETO SOFACARIBE2 A SOFA2");
				ddm.changeTypeObjectTo(ido, ddm.getIdClass("SOFA2"),null,null,null);
				System.out.println("(6) OBTENER LOS FACTS DE SOFA2 E IMPRIMIRLOS ");
				LinkedList <dynagent.ruleengine.src.ruler.IPropertyDef> isofac3 = ddm.getAllInstanceFacts("(instance (IDO "+ido+")))");
				Iterator itsofac3 = isofac3.iterator();
				while(itsofac3.hasNext()){
					System.out.println(((dynagent.ruleengine.src.ruler.Fact)itsofac3.next()).toInstanceString());
				}
			}
			else{
				System.out.println("......No se realizo la prueba por no encontrarse esa clase");
			}

		}catch (NotFoundException e) {
			e.printStackTrace();
		}
	
*/
	}
}
