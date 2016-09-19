package dynagent.ruleengine.test.test17.src;

import java.util.Iterator;
import java.util.LinkedList;

import jess.JessException;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.BooleanValue;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DataValue;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.IntValue;
import dynagent.ruleengine.meta.api.KnowledgeAdapter;
import dynagent.ruleengine.meta.api.ObjectProperty;
import dynagent.ruleengine.meta.api.ObjectValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.Value;
import dynagent.ruleengine.src.sessions.DefaultSession;
import dynagent.ruleengine.src.sessions.SessionController;
import dynagent.ruleengine.test.ITest;

public class Test17 implements ITest {

	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException {
		DocDataModel ddm=(DocDataModel)ik;
		System.out.println("\n\n----------------------TEST 17: PRUEBA MODIFICACIÓN/AÑADIR VALORES (SETVALUELIST)");	
		int idto=104,ido=1313130,idProp=2;
		try {
			StringValue v=new StringValue();
			StringValue v0=new StringValue();
			v0.setValue("value0");
			StringValue newsv=new StringValue();
			v.setValue("valorstring1");
			//Creamos una property con un valor y la añadimos a motor
			DataProperty p1=new DataProperty();
			p1.setIdo(ido);
			p1.setIdProp(idProp);
			p1.setIdto(idto);
			p1.getValues().add(v0);
			ddm.addIDOToIDTOWithLevel(ido, idto, Constants.LEVEL_INDIVIDUAL);
			KnowledgeAdapter ka=new KnowledgeAdapter(ik);
			ddm.addFactsToRuler(ka.traslatePropertyValueToIPropertyDef(p1));
			p1=(DataProperty)ddm.getProperty(ido, idProp, userRol,  Constants.USER_OWNERALLACCESS,usertask);
			System.out.println(" Property antes de las llamadas al metodo setValue: \n"+p1);
			
			//le añadimos un  valor.
			ddm.setValue(ido, idProp,  null,v , null,  Constants.USER_OWNERALLACCESS, null);
			p1=(DataProperty)ddm.getProperty(ido, idProp,  userRol,  Constants.USER_OWNERALLACCESS, usertask);
			System.out.println(" Property tras llamar a setValue para que haga un NEW \n"+p1);
			
			//modificamos el valor.
			newsv.setValue("modificadotringvalor");
			ddm.setValue(ido, idProp, v,newsv , null, Constants.USER_OWNERALLACCESS, null);
			p1=(DataProperty)ddm.getProperty(ido, idProp,  userRol,  Constants.USER_OWNERALLACCESS, usertask);
			System.out.println(" Property tras llamar a setValue para que haga un SET \n"+p1);
			
			System.out.println(" intentamos modificar un valor que no existe");
			StringValue malvalue=new StringValue();
			malvalue.setValue("valuequenotiene");
			ddm.setValue(ido, idProp, malvalue,newsv , userRol, Constants.USER_OWNERALLACCESS, usertask);
			
			p1=(DataProperty)ddm.getProperty(ido, idProp, null,  Constants.USER_OWNERALLACCESS, null);
			System.out.println(" Property tras llamar a setValue para que haga un SET con un oldvalue que no tiene en motor \n"+p1);
		
			//eliminamos el valor.
			ddm.setValue(ido, idProp,  newsv,null , null,  Constants.USER_OWNERALLACCESS, null);
			p1=(DataProperty)ddm.getProperty( ido, idProp,  userRol,  Constants.USER_OWNERALLACCESS, usertask);
			System.out.println(" Property tras llamar a setValue para que haga un DEL \n"+p1);
		
			ido=11001;
			//Comprobamos que existe el individuo en motor
			if(ddm.getLevelOf(ido)!=null)
			{
				Iterator it=ddm.getAllPropertyIterator(ido,null,null,null);
				System.out.println("\n    Antes prueba 17: Instance facts con ido no nulo: \n"+ddm.getAllNotModelInstanceFacts());
				if(it==null){
					System.out.println("El individuo con ido="+ido+" no tiene propiedades y/o no existe:");
				}
				else{
					while(it.hasNext()){
						Property p=(Property)it.next();
						LinkedList valueList=new LinkedList();
						if(p instanceof DataProperty){
							Value val=null;
							DataProperty dp=(DataProperty)p;
							if(dp.getDataType()==Constants.IDTO_STRING){
								StringValue sv=new StringValue();
								sv.setValue("valorModificado");
								val=sv;
							}
							else if(dp.getDataType()==Constants.IDTO_INT){
								IntValue iv=new IntValue();
								iv.setValueMin(7);
								iv.setValueMax(7);
								val=iv;
							}
							else if(dp.getDataType()==Constants.IDTO_BOOLEAN){
								BooleanValue bv=new BooleanValue();
								bv.setBvalue(false);
								val=bv;
							}
							if(val!=null){//Si se ha establecido un nuevo valor
								System.out.println("......se va a hacer un set en "+dp+"\n   con value="+val);
								ddm.setValue(dp.getIdo(), dp.getIdProp(), null, val, userRol, user, usertask);
							}
							else{
								System.out.println("......No se va a hacer un set en "+dp+"   pq valueList="+valueList);
							}
						}
						else if(p instanceof ObjectProperty){
							ObjectProperty op=(ObjectProperty)p;
							Value val=null;
							if(op.getRangoList().size()>0){
								Iterator itindirango=ddm.getIndividualsOfLevel(op.getRangoList().getFirst(), Constants.LEVEL_INDIVIDUAL);
								if(!itindirango.hasNext()){
									//si no lo ha encontrado con nivel de individuo prueba a buscarlo con nivel de modelo por si se 
									//son individuos que se dieron de alta desde el modelo owl
									 itindirango=ddm.getIndividualsOfLevel(op.getRangoList().getFirst(), Constants.LEVEL_MODEL);
								}
							
								if(itindirango.hasNext()){
									Integer primerInd=(Integer)itindirango.next();
									System.out.println("......se va a hacer un set en "+p+"\n   con el value="+primerInd);
									ObjectValue ov=new ObjectValue();
									ov.setValue(primerInd);
									ov.setValueCls(op.getRangoList().getFirst());
									val=ov;
									ddm.setValue(op.getIdo(), op.getIdProp(),null, val, userRol, user, usertask);
									
								}
							}
						}	
					}
					System.out.println("Después de las modificaciones:");
					System.out.println("\n    Después prueba 17: Instance facts con ido no nulo: \n"+ddm.getAllNotModelInstanceFacts());
					ddm.mostrarInfoSobreId(ido,null,null,null);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
}
}
