package dynagent.common.properties.values;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.DataProperty;
import dynagent.common.properties.Property;
import dynagent.common.utils.Auxiliar;
import dynagent.common.utils.Validation;

public abstract class DataValue extends Value {
	
	/*
	public String toString(){
		String result="   <DataValue";
		result=result+"   valueMin="+this.getValueMin();
		result=result+"   valueMax="+this.getValueMax();
		if(this.getUnit()!=null){
			result=result+"   unit="+this.getUnit();
		}
		if(this.getComment()!=null){
			result=result+"   comment="+this.getComment();
		}
		result=result+"   value="+this.getValue();
		result=result+">";
		return result;
	}
	*/
	
	public Element toElement() {
		return null;
	}
	
	public DataValue clone(){
		return null;
	}
	
	public boolean checkIsCompatibleWithNotException(Property pr, IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException{
		return checkIsCompatibleWith(pr,ik,userTask);
	}
	
	public boolean checkIsCompatibleWith(Property pr,IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException{
		return checkIsCompatibleWith(pr,ik,userTask,true);
	}
	
	private boolean checkIsCompatibleWith (Property  p,IKnowledgeBaseInfo ik,Integer userTask,boolean exception) throws IncompatibleValueException, NotFoundException{
	     boolean compatible = false;
	     boolean excluido=false;
	     String errormessage=null;
	     if(p instanceof DataProperty)
	     {
		     DataProperty pr=(DataProperty)p;
		     //los numéricos tendránun comportamiento especial a la hora de comprobar si están en enumList o excluLIst.
		     boolean numeric=pr.getDataType()==Constants.IDTO_DOUBLE||pr.getDataType()==Constants.IDTO_INT||pr.getDataType()==Constants.IDTO_UNIT;
		     
		     String mask=ik.getPropertyMask(p.getIdProp(), p.getIdto(), userTask);
		     if(mask!=null && (p.getIdo()==null || !Auxiliar.equals(ik.getLevelOf(p.getIdo()),Constants.LEVEL_FILTER))){ //REGULAR EXPRESION  
		    	if(mask.startsWith("Validation.")){		    		
		    		int index=mask.indexOf(".")+1;
		    		mask=mask.substring(index);
		    		compatible=Validation.checkWithFunction(mask, ((StringValue)this).getValue());
		    		
		    	}
		    	else{
		    		if(this instanceof IntValue){
		    			compatible=((IntValue)this).getValue_s().matches(mask);
		    		}else if(this instanceof DoubleValue){
		    			compatible=((DoubleValue)this).getValue_s().matches(mask);
		    		}else if(this instanceof StringValue){
		    			compatible=((StringValue)this).getValue().matches(mask);
		    		}
		    		//TODO Falta hacerlo para las fechas. El problema es que llega en long y nosotros necesitamos el formato de la fecha
		    	}		    	
		     }
		     // Si hay elementos en la lista de excluidos no debe ser compatible con ninguno de ellos 
		     if(pr.getEnumList().size()>0){
		    	 // Si hay elementos en la lista de enumerados tiene que ser compatible con al menos uno de ellos. 
		    	 if(!numeric){
		    		 compatible=pr.getEnumList().contains(this);
		    	 }
		    	 else{
				     for(int i = 0; i<pr.getEnumList().size() && !compatible; i++){
				    	 if(numeric){
				    		 compatible = this.isNumericalCompatibleWith(pr.getEnumList().get(i));
				    		 if(!compatible && ik!=null){
				    			 exceptionWhenNumericalIncompatible(pr, pr.getEnumList().get(i), ik, userTask);
				    		 }
				    	 }
				     }
		    	 }
			 }
		     if(pr.getExcluList().size() > 0){
		    	 if(!numeric){
		    		 excluido=pr.getExcluList().contains(this);
		    	 }
		    	 else{
		    		 for(int i = 0; i<pr.getExcluList().size()&&!excluido; i++){
			    		 excluido=this.isNumericalCompatibleWith(pr.getExcluList().get(i));
			    	}
		    	 } 
		     }
		     if(!excluido){
		    	 if(pr.getDataType()==Constants.IDTO_BOOLEAN){
		    		 compatible=(this instanceof BooleanValue);
		    	 }
		    	 else if(pr.getDataType()==Constants.IDTO_DATE||pr.getDataType()==Constants.IDTO_DATETIME||pr.getDataType()==Constants.IDTO_TIME){
		    		 compatible=(this instanceof TimeValue);
		    	 }
		    	 else if(pr.getDataType()==Constants.IDTO_INT){
		    		 compatible=(this instanceof IntValue);		 
		     	}
		     	else if(pr.getDataType()==Constants.IDTO_DOUBLE){
		     		compatible=(this instanceof DoubleValue);	 
	     		}
		     	else if(pr.getDataType()==Constants.IDTO_UNIT){
		     		compatible=(this instanceof UnitValue);	 
				}
		    	else if(pr.getDataType()==Constants.IDTO_MEMO||pr.getDataType()==Constants.IDTO_STRING||pr.getDataType()==Constants.IDTO_IMAGE||pr.getDataType()==Constants.IDTO_FILE){		    		
		    		compatible=true;//(this instanceof StringValue );
		    	}
		     }
		  }
		 else{
	    	 compatible=false;
	     }
	     if(!compatible && exception){
	    	 IncompatibleValueException ivE=new IncompatibleValueException ("dataValue.checkIsCompatibleWith: El valor ("+this+")  asignado al campo "+p.getName()+" no es válido p="+p);
	    	 	ivE.setUserMessage("El valor asignado al campo "+ik.getAliasOfProperty(p.getIdto(),p.getIdProp(),userTask)+" no es válido");
				throw ivE;
	     }
	     return compatible;
	}
	
	
	private void exceptionWhenNumericalIncompatible(DataProperty p,DataValue dataValue,IKnowledgeBaseInfo ik,Integer userTask) throws NotFoundException, IncompatibleValueException{
		String vmin = null,vmax=null;
	 	if(this instanceof DoubleValue /*&&vRef instanceof DoubleValue*/){
			DoubleValue nvRef=(DoubleValue)dataValue;
			if(nvRef.getValueMin()!=null)
				vmin=nvRef.getValueMin().toString();
			if(nvRef.getValueMax()!=null)
				vmax=nvRef.getValueMax().toString();
		}
		else if(this instanceof UnitValue /*&&vRef instanceof UnitValue*/){
			UnitValue nvRef=(UnitValue)dataValue;
			if(nvRef.getValueMin()!=null)
				vmin=nvRef.getValueMin().toString();
			if(nvRef.getValueMax()!=null)
				vmax=nvRef.getValueMax().toString();
		}
		else if(this instanceof IntValue /*&&vRef instanceof IntValue*/){
			IntValue nvRef=(IntValue)dataValue;
			if(nvRef.getValueMin()!=null)
				vmin=nvRef.getValueMin().toString();
			if(nvRef.getValueMax()!=null)
				vmax=nvRef.getValueMax().toString();
		}
		String usermessage=null;
		if(vmin!=null&&vmax!=null)
			usermessage="El valor asignado al campo "+ik.getAliasOfProperty(p.getIdto(),p.getIdProp(),userTask)+" tiene que estar comprendido entre "+vmin+" y "+vmax;
		else if(vmin!=null &&vmax==null)
			usermessage="El valor asignado al campo "+ik.getAliasOfProperty(p.getIdto(),p.getIdProp(),userTask)+" tiene que ser mayor/igual que "+vmin;
		else if(vmin==null &&vmax!=null)
			usermessage="El valor asignado al campo "+ik.getAliasOfProperty(p.getIdto(),p.getIdProp(),userTask)+" tiene que ser menor/igual que "+vmax;
		
		//System.err.println("value="+this+"  valueRef="+dataValue);
		IncompatibleValueException ivE=new IncompatibleValueException (usermessage);
		ivE.setUserMessage(usermessage);
		throw ivE;
	}
	
	
	/***
	 * Chequea si el dataValue es compatible con otro de referencia que se le pasa como parámetro.
		Entenderemos por compatible:
		-  si modela un valor numérico (float valueMin,float valueMax) que el rango de valores 
			sea compatible con el rango de valores del dataValue de referncia
		-  en cualquier otro caso (la información estaría en value) serán compatibles si coinciden
		los valores de value.
		@obs: el método no es simétrico: Ejempl: dv1={value=null,valueMin=8,valueMax=20}
	   	dv2={value=null,valueMin=0,valueMax=100}
	   	dv1.isCompatibleWith(dv2) devolverá true
	   	dv2.isCompatibleWithi(dv1) devolverá false.
	 * @param dvRef
	 * @return 
	 */
	public boolean isNumericalCompatibleWith (DataValue vRef){
		boolean compatible = true;//si los de referencia tienen min y max nulos significa que no hay cotas->son compatibles.
		if(this.equals(vRef)){;//si son iguales obv serán compatibles
			compatible=true;
		}else if(this instanceof DoubleValue /*&&vRef instanceof DoubleValue*/){
				DoubleValue nvRef=(DoubleValue)vRef;
				DoubleValue nv=(DoubleValue)this;
				
				if(nvRef.getValueMin()!=null){
					compatible=(nv.getValueMin()!=null&&(nv.getValueMin()>= nvRef.getValueMin()));
				}
				if(nvRef.getValueMax()!=null){
					compatible=compatible&&(nv.getValueMax()!=null&&(nv.getValueMax()<= nvRef.getValueMax()));
				}
		}
		else if(this instanceof UnitValue /*&&vRef instanceof UnitValue*/){
			UnitValue nvRef=(UnitValue)vRef;
			UnitValue nv=(UnitValue)this;
			
			//TODO UNIDADES CAMBIO A MISMA UNIDAD ANTES DE HACER LAS COMPARACIONES.
			if(nvRef.getValueMin()!=null){
				compatible=(nv.getValueMin()!=null&&(nv.getValueMin()>= nvRef.getValueMin()));
			}
			if(nvRef.getValueMax()!=null){
				compatible=compatible&&(nv.getValueMax()!=null&&(nv.getValueMax()<= nvRef.getValueMax()));
			}
		}
		else if(this instanceof IntValue /*&&vRef instanceof IntValue*/){
			IntValue nvRef=(IntValue)vRef;
			IntValue nv=(IntValue)this;
			
			if(nvRef.getValueMin()!=null){
				compatible=(nv.getValueMin()!=null&&(nv.getValueMin()>= nvRef.getValueMin()));
			}
			if(nvRef.getValueMax()!=null){
				compatible=compatible&&(nv.getValueMax()!=null&&(nv.getValueMax()<= nvRef.getValueMax()));
			}
		}
		return compatible;
	}
	
	
	/**
	 * Devuelve el valor numérico. SINO TIENE VALOR NUMÉRICO DEVUELVE CERO.
	 * ESTE METODO ESTA DISEÑADO PARA SER USADO DESDE METODOS QUE HOMOGENIZAN EL CALCULO DE REGLAS.
	 * @return
	 */
	public abstract Double getNumericValue();
		
			
		
		
		
	
	public boolean  equals (Object v){
		//ESTE MÉTODO ESTÁ SOBREESCRITO EN LAS CLASES QUE EXTIENDEN DATAVALUE. AQUÍ SE DEVUELVE SIEMPRE FALSE
		//PQ NO SE TRABAJA CON DATAVALUE NUNCA SINO SIEMPRE CON ALGUNOS DE SUS ESPECIALIZADOS
		return false;
	}
	
	
}
