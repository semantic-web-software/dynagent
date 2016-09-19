package dynagent.ruleengine.test.test22.src;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.src.ruler.FactAccess;
import dynagent.ruleengine.test.ITest;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Null;
import dynagent.ruleengine.auxiliar.Auxiliar;
import dynagent.server.knowledge.access;


public class Test22 implements ITest{
		public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask)  {
			System.out.println("\n\n----------------------TEST 22: PRUEBAS PERMISOS-------------");
			DocDataModel ddm=(DocDataModel)ik;
			try{
				
				//Iterator<FactAccess> accesses=ddm.getAllAccessFactsIterator("(access (IDTO ?p&~nil)))");
				Iterator<FactAccess> accesses=ddm.getRuleEngine().getAllAccessFacts(null, new Null(Null.NOTNULL), null, null, null, null, null, null,null).iterator();
				
				ArrayList<Integer> lclases=new 	ArrayList<Integer>();
				while(accesses.hasNext()){
					lclases.add(accesses.next().getIDTO());
				}
				Iterator <Integer> usertasks=ddm.getSpecialized(Constants.IDTO_UTASK);
				while(usertasks.hasNext()){
				
					int utask=usertasks.next();
					
					System.out.println("===========================USERTASK="+utask+"====================");
										
					System.out.println("===========================================");
					
					ObjectProperty puserRol=(ObjectProperty)ddm.getProperty(utask, Constants.IdPROP_USERROL,null, Constants.USER_SYSTEM, null);
					for(int i=0;i<puserRol.getEnumList().size();i++){
						int userrol=puserRol.getEnumList().get(i).getValue();
						System.out.println("...............userRol="+userrol+"...................");
						for(int j=0;j<lclases.size();j++){
							int idto=lclases.get(i);
							System.out.println("\n.............TIPO DE OBJETO="+idto+"...................................");
							access acc=ddm.getAccessOverObject(idto, userrol, null, utask);
							if(acc.getOperation()!=0)
								System.out.println("=======getAccessOverObject(id, userRol, user, usertask)="+acc);
							HashMap<Integer,dynagent.server.knowledge.access> hm=ddm.getAllAccessOverObject(idto, userrol, null);
							
							System.out.println(Auxiliar.hashMapToString(hm, "usertask", "acceso"));
							Iterator<Integer> propiedades=(ddm.getAllIDsPropertiesOf(idto)).iterator();
							while (propiedades.hasNext()){
									int idProp=propiedades.next();			
									System.out.println("       - Access en la propiedad="+idProp+"  en la clase="+idto+"  en la utask="+utask);
									System.out.println(ddm.getPropertyAccessOf(idto, null, idProp,  null, null, utask));
							}
						}
						
					}
				}
			  	
			}catch(Exception e){e.printStackTrace();}
		}
}
