package dynagent.ruleengine.test.test3.src;

import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.ruleengine.Constants;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.BooleanValue;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.DoubleValue;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.IntValue;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.meta.api.StringValue;
import dynagent.ruleengine.meta.api.TimeValue;
import dynagent.ruleengine.meta.api.UnitValue;
import dynagent.ruleengine.meta.api.Value;
import dynagent.ruleengine.src.ruler.FactInstance;
import dynagent.ruleengine.test.ITest;

public class Test3 implements ITest {

	public void run(IKnowledgeBaseInfo ik, Integer userRol, String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException {
	    System.out.println("\n\n----------------------TEST 3: PRUEBA DE UNICO VALOR");
		DocDataModel ddm=(DocDataModel)ik;
		FactInstance fi=new FactInstance();
		Iterator<Integer> utasks=ddm.getSpecialized(Constants.IDTO_UTASK);
	    if( utasks.hasNext()){
	    	Integer idto= utasks.next();
	    	Integer idproto=ddm.createPrototype(idto,Constants.LEVEL_PROTOTYPE, userRol, user, usertask);
	    	Iterator<Property>itp= ddm.getAllPropertyIterator(idproto, null, Constants.USER_SYSTEM, null);
	    	while(itp.hasNext()){
	    		Property p=itp.next();
	    		Value newval = null;
	    		if(p instanceof DataProperty){
	    			System.out.println("-- DataProperty : "+ p);
					DataProperty dp=(DataProperty)p;
					if(ddm.isUnit(dp.getDataType())){
						UnitValue uv=new UnitValue();
						uv.setValueMax(50.0D);
						uv.setValueMin(70.0D);
						uv.setUnit(1313);
						
						newval=uv;
					}
					
					else if(dp.getDataType()==Constants.IDTO_STRING){
						StringValue sv=new StringValue();
						sv.setValue("valorprueba");
						
						newval=sv;
					}
					else if(dp.getDataType()==Constants.IDTO_INT){
						IntValue iv=new IntValue();
						iv.setValueMax(10);
						iv.setValueMin(20);
						
						newval=iv;
					}
					else if(dp.getDataType()==Constants.IDTO_DOUBLE){
						DoubleValue fv=new DoubleValue();
						fv.setValueMax(10.0D);
						fv.setValueMin(20.0D);
					
						newval=fv;
					}
					else if(dp.getDataType()==Constants.IDTO_BOOLEAN){
						BooleanValue bv=new BooleanValue();
						bv.setBvalue(true);
						bv.setComment("comentariobooleano");
					
						newval=bv;
					}
					else if(dp.getDataType()==Constants.IDTO_DATE||dp.getDataType()==Constants.IDTO_DATETIME||dp.getDataType()==Constants.IDTO_TIME){
						TimeValue tv=new TimeValue();
						//tv.setReferenceInstant(Constants.BEGIN1970);
						tv.setRelativeSecondsMin(13131313l);
						tv.setRelativeSecondsMax(13131313l);
						
						newval=tv;
					}
					if(newval!=null){
						//añadimos el valor
						ddm.setValue(p.getIdo(), p.getIdProp(),  null, newval, userRol, user, usertask);
						//borramos el valor
			    		ddm.setValue(p.getIdo(), p.getIdProp(),  newval, null, userRol, user, usertask);
			    		//y lo volvemos a añadir
			    		ddm.setValue(p.getIdo(), p.getIdProp(),  null, newval, userRol, user, usertask);
					}
	    		}
	    		
	    		
	    	
	    		
	    	}
	    }
	}

}
