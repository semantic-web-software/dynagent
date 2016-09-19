	package dynagent.ruleengine.test.test8.src;
	
	import java.util.Iterator;
import java.util.LinkedList;
import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.meta.api.BooleanValue;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.DoubleValue;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.IntValue;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.TimeValue;
import dynagent.ruleengine.meta.api.UnitValue;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;
import dynagent.server.knowledge.instance.instance;
	
	public class Test8 implements ITest {
	
		public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) {
			System.out.println("\n\n----------------------TEST 8:    TRADUCCIONES OBJETOS API-MODELO DATOS-----------------");	
			DocDataModel ddm=(DocDataModel)ik;
			System.out.println("\n------------!!Esta prueba debe realizarse con dyna4 que es donde está definida la clase CLASE_TEST!!");
			try
			{			
					int idto=ddm.getIdClass("Clase_Test");	
					int ido=ik.createPrototype(idto,Constants.LEVEL_PROTOTYPE, null,null,null,ddm);
					System.out.println("....Creación del prototipo de CLASETEST="+  idto+"  se ha creado con ido="+ido);
					instance tree=ddm.getTreeObject(ido, null, Constants.USER_SYSTEM,null);
					System.out.println("======TREE OBJECT DE IDO\n"+tree);
					Iterator<Property> it=ddm.getAllPropertyIterator(idto,null, Constants.USER_SYSTEM,null);
				//	SessionController.getInstance().setActual(ddm);
					LinkedList<Property>allPropertiesOftree=tree.getAllPropertiesInTreeObject();
					//Iterator<Property> it=allPropertiesOftree.iterator();
					SessionController.getInstance().setActual(ddm);
					//CADA PROPIEDAD EN FUNCIÓN DE SI ES OBJECTPROPERTY O DATAPROPERTY LE ASIGNO VALORES
					System.out.println("FIN 0");
					while(it.hasNext()){
						Property p=it.next();
						if(p instanceof DataProperty){
							System.out.println("-- DataProperty : "+ p);
							DataProperty dp=(DataProperty)p;
							if(ddm.isUnit(dp.getDataType())){
								UnitValue uv=new UnitValue();
								uv.setValueMax(50.0D);
								uv.setValueMin(70.0D);
								uv.setUnit(1313);
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, uv, null, Constants.USER_SYSTEM,null);
							//	SessionController.getInstance().setActual(ddm);
							}
							
							else if(dp.getDataType()==Constants.IDTO_STRING){
								StringValue sv=new StringValue();
								sv.setValue("valorprueba");
								ddm.setValue(p.getIdo(), p.getIdProp(),null, sv, null, Constants.USER_SYSTEM,null);
							//	SessionController.getInstance().setActual(ddm);
								System.out.println();
							}
							else if(dp.getDataType()==Constants.IDTO_INT){
								IntValue iv=new IntValue();
								iv.setValueMax(10);
								iv.setValueMin(20);
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, iv, null, Constants.USER_SYSTEM,null);
							//	SessionController.getInstance().setActual(ddm);
							}
							else if(dp.getDataType()==Constants.IDTO_DOUBLE){
								DoubleValue fv=new DoubleValue();
								fv.setValueMax(10.0D);
								fv.setValueMin(20.0D);
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, fv, null, Constants.USER_SYSTEM,null);
								//SessionController.getInstance().setActual(ddm);
							}
							else if(dp.getDataType()==Constants.IDTO_BOOLEAN){
								BooleanValue bv=new BooleanValue();
								bv.setBvalue(true);
								bv.setComment("comentariobooleano");
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, bv, null, Constants.USER_SYSTEM,null);
								//SessionController.getInstance().setActual(ddm);
							}
							else if(dp.getDataType()==Constants.IDTO_DATE||dp.getDataType()==Constants.IDTO_DATETIME||dp.getDataType()==Constants.IDTO_TIME){
								TimeValue tv=new TimeValue();
								tv.setReferenceInstant(Constants.BEGIN1970);
								tv.setRelativeSecondsMin(13131313l);
								tv.setRelativeSecondsMax(13131313l);
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, tv, null, Constants.USER_SYSTEM,null);
								//SessionController.getInstance().setActual(ddm);
							}
							else 
								System.err.println("Esa property no ha entrado en ningun IF"+p);
						}
						else if(p instanceof ObjectProperty){
							System.out.println("ObjectProperty : "+ p);
							ObjectProperty op=(ObjectProperty)p;
							int idoaux=10101;
							//SI ES PAR AÑADIREMOS DOS VALORES
							//si es IMPAR AÑADIREMOS VALORES RESUMIDOS
							ObjectValue ov=new ObjectValue();
							ov.setValueCls(op.getRangoList().getFirst());
							if(p.getIdProp()%2==0){
								ov.setValue(idoaux);
								idoaux++;
								ov.setValueCls(op.getRangoList().getFirst());
								ddm.setValue(p.getIdo(), p.getIdProp(),  null, ov, null, Constants.USER_SYSTEM,null);
								//SessionController.getInstance().setActual(ddm);
								ov.setValue(idoaux);
								idoaux++;
								ddm.setValue(p.getIdo(), p.getIdProp(), null, ov, null, Constants.USER_SYSTEM,null);
							//	SessionController.getInstance().setActual(ddm);
							}
							else if(p.getIdProp()%2!=0){
								ov.setQ(35);
								ddm.setValue(p.getIdo(), p.getIdProp(), null, ov, null, Constants.USER_SYSTEM,null);
						//		SessionController.getInstance().setActual(ddm);
							}
							else 
								System.err.println("Esa property no ha entrado en ningun IF");
						}
					}
					it=ddm.getAllPropertyIterator(ido,null, Constants.USER_SYSTEM,null);
					System.out.println("======TREE DESPUES DE HACER LOS SET  LAS PROPIEDADES DE  ido="+ido);
					while(it.hasNext()){
						System.out.println(it.next());
					}
					ddm.commit();
					//TODO HACER COMMIT, Y COMPARAR EL TREEOBJECT DEDUCIDO DEL COMMIT QUE IRIA A SERVER CON treeAfter;
				System.out.println("hay "+ddm.getSesionables().size()+ " sessionables en el docdatamodel");
			//	ddm.getRuler().getR().getR().executeCommand("(facts)");
			}catch(NotFoundException e){
				e.printStackTrace();
		
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
	
	}
