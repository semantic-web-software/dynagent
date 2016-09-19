/***
 * Property.java
 * @author Jose A. Zamora Aguilera
 */

package dynagent.common.properties;


import java.util.LinkedList;

import org.jdom.Element;

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
import dynagent.common.knowledge.PropertyValue;
import dynagent.common.knowledge.access;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;


public abstract class Property extends PropertyValue {
	
	public Integer cardMin;
	public Integer cardMax;
	public access typeAccess;
	public boolean valuesFixed=false;
	//TODO GENERALIZAR LOS TIPOS DE  ENUMLIST A VALUE  
	
	
	public boolean isValuesFixed() {
		return valuesFixed;
	}
	public void setValuesFixed(boolean valuesFixed) {
		this.valuesFixed = valuesFixed;
	}
	public Integer getCardMax() {
		return cardMax;
	}
	public void setCardMax(Integer cardMax) {
		this.cardMax = cardMax;
	}
	public Integer getCardMin() {
		return cardMin;
	}
	public void setCardMin(Integer cardMin) {
		this.cardMin = cardMin;
	}
	
	
	/**
	 *
	 * @return
	 */
//	TODO IMPLEMENTAR ESTE METODO GURADANDO ENUMLIST EN PROPERTY Y CAMBIARLE EL NOMBRE A GETENUMLIST
	public LinkedList<Value> getEnums(){
		if(this instanceof DataProperty){
			return Auxiliar.toValues(((DataProperty)this).getEnumList());
		}
		else if(this instanceof ObjectProperty){
			return Auxiliar.toValues(((ObjectProperty)this).getEnumList());
		}
		else return null;
	}
	
	
//	TODO IMPLEMENTAR ESTE METODO GURADANDO ExcluLIST EN PROPERTY Y CAMBIARLE EL NOMBRE A GETExcluLIST
	public LinkedList<Value> getExclus(){
		if(this instanceof DataProperty){
			return Auxiliar.toValues(((DataProperty)this).getExcluList());
		}
		else if(this instanceof ObjectProperty){
			return Auxiliar.toValues(((ObjectProperty)this).getExcluList());
		}
		else return null;
	}
	
//	TODO IMPLEMENTAR ESTE METODO GURADANDO ENUMLIST EN PROPERTY Y CAMBIARLE EL NOMBRE A setENUMLIST
	/*public void setEnums(LinkedList<Value> valores){
		for(int i=0;i<valores.size();i++){
			
		}
		
	}*/
	
	
	
	public access getTypeAccess() {
		return typeAccess;
	}
	public void setTypeAccess(access typeAccess) {
		this.typeAccess = typeAccess;
	}
	
		
	
	public abstract Property clone();

	
	public String toString(){
		System.out.println("    WARNING: Este método toString en Property está sobreescrito en ObjectProperty y DataProperty, no se espera su uso con objetos que solo sean Property");
		return null;
	}
	
	public Element toElement() {
		return null;
	}
	
	public String ValueListToString(LinkedList  vList){
		String result = null;
		for(int i=0;i<vList.size();i++){
			if(result==null){
				result=vList.get(i).toString();
			}
			else if(vList.get(i)!=null)
				result+=vList.get(i).toString();
		}
		return result;
	
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
		
		
	
	
	
}
