package dynagent.common.properties.values;


import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.exceptions.ApplicationException;
import dynagent.common.exceptions.CardinalityExceedException;
import dynagent.common.exceptions.CommunicationException;
import dynagent.common.exceptions.IncoherenceInMotorException;
import dynagent.common.exceptions.IncompatibleValueException;
import dynagent.common.exceptions.InstanceLockedException;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.exceptions.OperationNotPermitedException;
import dynagent.common.exceptions.RemoteSystemException;
import dynagent.common.exceptions.SystemException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.Domain;
import dynagent.common.properties.IDIndividual;
import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.utils.Auxiliar;

public class ObjectValue  extends Value implements IDIndividual{
	private int valueCls;
	private Integer value;
	
	
//	Para representar los resumenes (ejemplo 3 habitaciones, q=3  value=habitacion)
	
	private Integer Q;
	
	
	
	public ObjectValue(){}
	
	
	public ObjectValue(Integer value,int valuecls){
		this.value=value;
		this.valueCls=valuecls;
	}
	
	public ObjectValue(Domain domvalue){
		this.value=domvalue.getIdo();
		this.valueCls=domvalue.getIdto();
	}
	
	public ObjectValue(IDIndividual i){
		value=i.getIDOIndividual();
		valueCls=i.getIDTOIndividual();
	}
	
	public Integer getValue() {
		return value;
	}
	
	
	
	

	public void setValue(Integer value) {
		this.value = value;
	}
	
	public int getValueCls() {
		return valueCls;
	}

	public void setValueCls(int valueCls) {
		this.valueCls = valueCls;
	}

	public Integer getQ() {
		return Q;
	}

	public void setQ(Integer q) {
		Q = q;
	}
	
	
	public ObjectValue clone() {
		ObjectValue ob = new ObjectValue();
		ob.setValueCls(valueCls);
		ob.setValue(value);
		ob.setQ(Q);
		return ob;
	}
	
	public String toString(){
		String result;
		result="\n    <ObjectValue";
		result=result+"   Q="+this.getQ();
		result=result+"   value="+this.getValue();
		result=result+"   valueCls="+this.getValueCls();
		result=result+"  /ObjectValue>";
		return result;
	}
	
	public Element toElement() {
		Element objectValue = new Element("OBJECT_VALUE");
		if (value!=null)
			objectValue.setAttribute("VALUE",String.valueOf(value));
			objectValue.setAttribute("VALUE_CLS",String.valueOf(valueCls));
		if (Q!=null)
			objectValue.setAttribute("Q",String.valueOf(Q));
		return objectValue;
	}
	
	
	public boolean checkIsCompatibleWithNotException(Property pr,IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		return checkIsCompatibleWith(pr,ik,userTask,false);
	}
	
	public boolean checkIsCompatibleWith(Property pr,IKnowledgeBaseInfo ik,Integer userTask) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		return checkIsCompatibleWith(pr,ik,userTask,true);
	}
	
	/**
	 * OJO, SOLO CHEQUEARA COMPATIBILIDAD CON EL RANGO DIRECTO DECLARADO. NO MIRARA HERENCIAS. QUEDA PENDIENTE DE SI LA INFO 
	 * DE TODO EL RANGO ES INCRUSTADO EN LA OBJECTPROPERTY O SE USA EL METODO PASANDOLE EL RANGO EXTENDIDO COMO PARÁMETRO
	 * @param pr
	 * @return
	 * @throws IncompatibleValueException 
	 * @throws NotFoundException 
	 * @throws OperationNotPermitedException 
	 * @throws IncoherenceInMotorException 
	 * @throws CardinalityExceedException 
	 * @throws ApplicationException 
	 * @throws InstanceLockedException 
	 * @throws CommunicationException 
	 * @throws RemoteSystemException 
	 * @throws SystemException 
	 */
	private boolean checkIsCompatibleWith (Property  p, IKnowledgeBaseInfo ik, Integer userTask, boolean exception) throws IncompatibleValueException, NotFoundException, SystemException, RemoteSystemException, CommunicationException, InstanceLockedException, ApplicationException, CardinalityExceedException, IncoherenceInMotorException, OperationNotPermitedException{
		boolean compatible = false;
		String causa="";
		if(p instanceof ObjectProperty){
			ObjectProperty  pr=(ObjectProperty)  p;
			if(p.idProp.intValue()==489){
				System.out.println("OBV FATCURA:"+p);
			}
			// Si hay elementos en la lista de excluidos no puede coincidir con ninguno de sus valores
			if(pr.getExcluList().size() > 0 && pr.getExcluList().contains(this)){
			    compatible = false;
			    causa="Excluido";
			}
			// Si hay elementos en la lista de enumerados tiene que coincidir con alguno de sus valores
			else if(pr.getEnumList().size() > 0){
				if( pr.getEnumList().contains(this))
					compatible = true;
				else if(p.getIdProp()==Constants.IdPROP_TARGETCLASS)//TODO Esta opcion se permite para crear/editar un enumerado. Asi que habria que preguntar si value es prototype y si getClass(rangoList)==valueCls pero no tenemos aqui el docDataModel  
					compatible = true;
				causa="Enumlist";
			}
			else  if(pr.getRangoList().size() > 0 && this != null){
				// Si el rango lo conforman una o varios clases tiene que pertenecer a alguno de ellos (o a alguno de sus especializados:
				// se entiende que un objeto de una clase A también pertece a todas las clases de las que A es descendiente)

				Iterator<Integer> itr=pr.getRangoList().iterator();
				boolean found=false;
				while(!found && itr.hasNext()){
					int idRange=itr.next();
					int idtoRange=ik.getClassOf(idRange);
					if(idtoRange==this.getValueCls() || ik.getSpecialized(idtoRange).contains(this.getValueCls()))
						found=true;
				}
				compatible=found;
				causa="Rango";
			}
			else if(pr.getRangoList().isEmpty()){//Si no tiene rangoList suponemos que caso con cualquier rango. Esto ocurre con el source de las QuestionTask
				compatible=true;
			}
			if(!compatible) causa="Ninguna";
		}
		else{
			compatible=false;
			causa="Dataprop";
		}
		 if(!compatible && exception){
	    	 IncompatibleValueException ivE=new IncompatibleValueException ("ObjectValue.checkIsCompatibleWith: El valor ("+this+") asignado al campo "+p.getName()+" no es válido\n p="+p+" (causa:"+causa+")");
				ivE.setUserMessage("El valor asignado al campo "+ik.getAliasOfProperty(p.getIdto(),p.getIdProp(),userTask)+" no es válido"+" (causa:"+causa+")"); 
				throw ivE;
	     }
		return compatible;
	}
	
	
	/**
	 * 
	 */
	public boolean isCompatibleWith (ObjectProperty  prModel,LinkedList<Integer>allRange){
		boolean compatible = false;
		// Si hay elementos en la lista de enumerados tiene que coincidir con alguno de sus valores 
		if(prModel.getEnumList().size() > 0){
			if( prModel.getEnumList().contains(this))
				compatible = true;
			else{
				compatible = false;
			}
		}
		if(compatible == false){
			// Si hay elementos en la lista de excluidos no puede coincidir con ninguno de sus valores
			if(prModel.getExcluList().size() > 0 && prModel.getExcluList().contains(this)){
			    compatible = false;
			}
			// Si el rango lo conforman uno o varios clases tiene que pertenecer a alguno de ellos (o a alguno de sus especializados:
			// se entiende que un objeto de una clase A también pertece a todas las clases de las que A es descendiente)
			if(prModel.getRangoList().size() > 0 && this != null){
				
				compatible=allRange.contains(this.getValueCls());
			}
		}
		return compatible;
	}	
	
	
	public boolean equals (Object v){
		if(v==null) return false;
		
		if(v instanceof ObjectValue){
			ObjectValue ov=(ObjectValue)v;
			if( this.getValueCls()!=ov.getValueCls() ) return false;			
			if( this.getValue()==null && ov.getValue()==null) return true;			
			if( this.getValue()==null && ov.getValue()!=null  ||  this.getValue()!=null && ov.getValue()==null) return false;
						
			return this.getValue().equals(ov.getValue());
		}
		if( v instanceof Domain){
			Domain dv=(Domain)v;	
			if( this.getValueCls()!=dv.getIdto() ) return false;			
			if( this.getValue()==null && dv.getIdo()==null) return true;			
			if( this.getValue()==null && dv.getIdo()!=null  ||  this.getValue()!=null && dv.getIdo()==null) return false;
						
			return this.getValue().equals(dv.getIdo());		
		}
		return false;

	}


	@Override
	public String getValue_s() {
		String result=null;
		if(this.value!=null)
			 result=this.value.toString();
		return result;
	}
	
	@Override
	public int hashCode() {
		return 10000*this.getValue().intValue()+this.getValueCls();
	}


	public Integer getIDOIndividual() {
		return this.getValue();
	}

	public Integer getIDTOIndividual() {
		return this.getValueCls();
	}
	
	

	

}
