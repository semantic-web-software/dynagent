package dynagent.ruleengine.test.test18.src;

import dynagent.ruleengine.Null;
import dynagent.ruleengine.Exceptions.CardinalityExceedException;
import dynagent.ruleengine.Exceptions.IncoherenceInMotorException;
import dynagent.ruleengine.Exceptions.IncompatibleValueException;
import dynagent.ruleengine.Exceptions.NotFoundException;
import dynagent.ruleengine.Exceptions.OperationNotPermitedException;
import dynagent.ruleengine.meta.api.BooleanValue;
import dynagent.ruleengine.meta.api.DataProperty;
import dynagent.ruleengine.meta.api.DocDataModel;
import dynagent.ruleengine.meta.api.IKnowledgeBaseInfo;
import dynagent.ruleengine.meta.api.Property;
import dynagent.ruleengine.test.ITest;

public class Test18  implements ITest{
	public void run(IKnowledgeBaseInfo ik,Integer userRol,String user, Integer usertask) throws NotFoundException, IncoherenceInMotorException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException{
		   //prueba modificación comentario del valor de una propiedad
			System.out.println("\n\n----------------------TEST 18: PRUEBA MODIFICACIÓN/AÑADIR COMENTARIOS EN LOS CAMPOS EXTENDIDOS -CHECKBOCK EXTENDIDOS-");
			DocDataModel ddm=null;
			if( ik instanceof DocDataModel){
				ddm=(DocDataModel)ik;			
				System.out.println("dni has idProp="+ddm.getIdProperty("numero"));
				System.out.println("hipoteca has idProp="+ddm.getIdProperty("hipoteca"));
			
			    Integer idoprueba=11001;
			    Integer idPropHip=ddm.getIdProperty("hipoteca");
			    
			    if(ddm.getLevelOf(idoprueba)!=null&&idPropHip!=null)
			    {
				    Property p = ddm.getProperty(idoprueba,idPropHip ,userRol,user,usertask);
				    System.out.println(" before change"+p);
				
					if(p instanceof dynagent.ruleengine.meta.api.DataProperty){
						DataProperty dp = (DataProperty)p;
				    	BooleanValue bv = new BooleanValue();
				    	bv.setBvalue(true);
				    	bv.setComment("   modificación lunes al comentario en hipoteca");
				    	ddm.setValue(dp.getIdo(), dp.getIdProp(), null, bv, userRol, user, usertask);
				    	p = ddm.getProperty(idoprueba, ddm.getIdProperty("hipoteca"),userRol,user,usertask);
					    System.out.println("after change"+p);
					}
					else{
					    System.out.println(" No se encontró la propiedad hipoteca en el individuo  con ido="+idoprueba);
					 }
					    System.out.println("\n   instances with ido not null:\n  "+ddm.getRuleEngine().getAllInstanceFacts(null, new Null(Null.NOTNULL), null, null, null, null, null, null,null));
				 }
				     
			}
		}
	
	
	public void testModifyCommentValue(IKnowledgeBaseInfo ik, Integer userRol, String user, Integer usertask) throws NotFoundException, CardinalityExceedException, OperationNotPermitedException, IncompatibleValueException, IncoherenceInMotorException{
		   //prueba modificación comentario del valor de una propiedad
		    DocDataModel ddm=new DocDataModel();
		    System.out.println("dni has idProp="+ddm.getIdProperty("numero"));
		    System.out.println("hipoteca has idProp="+ddm.getIdProperty("hipoteca"));
		    Integer idoprueba=11002;
		    
		    Property p = ik.getProperty(idoprueba, ddm.getIdProperty("hipoteca"), userRol, user, usertask,ddm);
		   if(p instanceof dynagent.ruleengine.meta.api.DataProperty){
				DataProperty dp = (DataProperty)p;
		    	BooleanValue bv = new BooleanValue();
		    	bv.setBvalue(true);
		    	bv.setComment("   modificación lunes al comentario en hipoteca");
		    	ddm.setValue(dp.getIdo(), dp.getIdProp(), null, bv, userRol, user, usertask);
		    	
	    }
	   		   
		   try{ 
			   p = ik.getProperty(idoprueba, ddm.getIdProperty("hipoteca"), userRol, user, usertask,ddm);
			   System.out.println(" after "+p);
		   }catch(NotFoundException e)
		    {
		    	System.out.println(" No se encontró la propiedad hipoteca en el individuo  con ido="+idoprueba);
		    }
		}
}
