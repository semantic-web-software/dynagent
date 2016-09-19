/***
 * Property.java
 * @author Jose A. Zamora Aguilera
 */

package dynagent.common.properties;

import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.properties.values.DataValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;

public class DataProperty extends Property implements Comparable{
	private Integer length;
	private int dataType; 
	private String mask; 
	public  LinkedList<DataValue> enumList = new LinkedList<DataValue>();   
	public LinkedList<DataValue> excluList = new LinkedList<DataValue>();
	
	public LinkedList<DataValue> getEnumList() {
		return enumList;
	}
	public LinkedList<DataValue> getExcluList() {
		return excluList;
	}
	public void setExcluList(LinkedList<DataValue> excluList) {
		this.excluList = excluList;
	}
	
	public void setEnumList(LinkedList<DataValue> enumList) {
		this.enumList = enumList;
	}
	
	public int getDataType() {
		return dataType;
	}
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}
	
	public DataProperty clone() {
		DataProperty p = new DataProperty();
		p.setName(getName());
		p.setIdProp(getIdProp());
		p.setIdo(getIdo());
		p.setIdto(getIdto());
		p.setCardMin(getCardMin());
		p.setCardMax(getCardMax());
		p.setTypeAccess(getTypeAccess());
		p.setDataType(dataType);
		LinkedList<Value> valueListClone = new LinkedList<Value> ();
		for (int i=0;i<this.values.size();i++)
			valueListClone.add(values.get(i).clone());
		p.setValues(valueListClone);
		LinkedList<DataValue> enumListClone = new LinkedList<DataValue> ();
		for (int i=0;i<this.enumList.size();i++) {
	        enumListClone.add(this.enumList.get(i).clone());
		}
		p.setEnumList(enumListClone);
		
		LinkedList<DataValue> excluListClone = new LinkedList<DataValue> ();
		for (int i=0;i<this.excluList.size();i++) {
			excluListClone.add(this.getExcluList().get(i).clone());
		}
		p.setExcluList(excluListClone);

		return p;
	}
	public boolean equals(Object p){
		//System.out.println("EQUALS");
		if (p instanceof DataProperty ){
			DataProperty dp = (DataProperty)p;
			if (!Auxiliar.equals(dp.getCardMax(),this.cardMax)){
			//	System.out.println("CARDMAX");
				return false;}
			if (!Auxiliar.equals(dp.getCardMin(),this.cardMin)){
				//System.out.println("CARDMIN");
				return false;}
			if (dp.getDataType()!=this.dataType){
				//System.out.println("DATATYPE");
				return false;}
			if (dp.getEnumList().size()!=this.enumList.size() ||( dp.getEnumList().size()==this.enumList.size() && !dp.getEnumList().containsAll(this.enumList))){
				//System.out.println("ENUMLIST");
				return false;}
			if (dp.getExcluList().size()!=this.excluList.size() ||(!dp.getExcluList().containsAll(this.excluList))){
				//System.out.println("EXCLULIST");
				return false;}
			if (!Auxiliar.equals(dp.getIdo(),this.ido)){
				//System.out.println("IDO");
				return false;}
			if (!Auxiliar.equals(dp.getIdProp(),this.idProp)){
				//System.out.println("IDPROP");
				return false;}
			if (!Auxiliar.equals(dp.getIdto(),this.idto)){
				//System.out.println("IDTO");
				return false;}
			if (!Auxiliar.equals(dp.getName(),this.name)){
				//System.out.println("NAME");
				return false;}
			if (!Auxiliar.equals(dp.getTypeAccess(),this.typeAccess)){
				//System.out.println("TYPEACCESS");
				return false;}
			if (dp.getValues().size()!=this.values.size() ||(!dp.getValues().containsAll(this.values))){
				//System.out.println("VALUES");
				return false;}
			
			return true;
		}else{
			//System.out.println("NO ES DATAPROPERTY");
			return false;
		}
		
	}
	
	public String toString(){
		String result="<DataProperty";
		result=result+"  ido="+super.getIdo();
		result=result+"  idto="+super.getIdto();
		result=result+"  idProp="+super.getIdProp();
		result=result+"  name="+super.getName();
		result=result+"  cardMin="+super.getCardMin();
		result=result+"  cardMax="+super.getCardMax();
		result=result+"  typeAcces="+super.getTypeAccess();
		result=result+"  datatype="+this.getDataType();
		
		result=result+">";
		if(this.valuesFixed){
			result=result+"\n   !VALUESFIXED!";
		}
		if(this.values.size()>0)
			result=result+"\n   <valueList "+this.ValueListToString(this.getValues())+"\n   </valueList>";
		if(this.enumList.size()>0)
			result=result+"\n   <enumList "+this.ValueListToString(this.getEnumList())+"\n   </enumList>";
		if(this.excluList.size()>0)
			result=result+"\n   <excluList "+this.ValueListToString(this.getExcluList())+"\n   </excluList>";
		result=result+"\n</DataProperty>";

		
		return result;
				
	}
	public Element toElement() {
		Element property = new Element("PROPERTY");
        
        //datos comunes NAME, PROP, CARD_MIN, CARD_MAX, ACCESS
    	if (super.getIdo()!=null)
        	property.setAttribute("ID_O",String.valueOf(super.getIdo()));
        if (super.getIdto()!=null)
        	property.setAttribute("ID_TO",String.valueOf(super.getIdto()));
    	if (super.getName()!=null)
        	property.setAttribute("NAME",super.getName());
        if (super.getIdProp()!=null)
        	property.setAttribute("PROP",String.valueOf(super.getIdProp()));
        if (super.getCardMin()!=null)
        	property.setAttribute("CARD_MIN",String.valueOf(super.getCardMin()));
        if (super.getCardMax()!=null)
        	property.setAttribute("CARD_MAX",String.valueOf(super.getCardMax()));
        if (super.getTypeAccess()!=null)
        	property.setAttribute("ACCESS",String.valueOf(super.getTypeAccess().getOperation()));
        if (super.isValuesFixed())
        	property.setAttribute("VALUES_FIXED","TRUE");
        
    	//se añade DPROP, LENGTH, DATA_TYPE, VALUE_LIST, ENUM_LIST, EXCLU_LIST
    	property.setAttribute("DPROP","TRUE");
		if (length!=null)
			property.setAttribute("LENGTH",String.valueOf(length));
//    		if (dp.getDataType()!=null)
			property.setAttribute("DATA_TYPE",String.valueOf(dataType));
        if (super.getValues().size()>0) {
        	Element elemValueList = new Element("VALUES");
        	property.addContent(elemValueList);
        	for (int i=0;i<super.getValues().size();i++) {
        		DataValue dv = (DataValue)super.getValues().get(i);
        		Element dataValue = dv.toElement();
        		elemValueList.addContent(dataValue);
        	}
        }
        if (enumList.size()>0) {
        	Element elemEnumList = new Element("ENUM_LIST");
        	property.addContent(elemEnumList);
        	for (int i=0;i<enumList.size();i++) {
        		DataValue dv = enumList.get(i);
        		Element dataValue = dv.toElement();
        		elemEnumList.addContent(dataValue);
        	}
        }
        if (excluList.size()>0) {
        	Element elemExcluList = new Element("EXCLU_LIST");
        	property.addContent(elemExcluList);
        	for (int i=0;i<excluList.size();i++) {
        		DataValue dv = excluList.get(i);
        		Element dataValue = dv.toElement();
        		elemExcluList.addContent(dataValue);
        	}

        }
		return property;
	}
	
	public int compareTo(Object p) {
		
		//System.out.println("COMPARETO");
		if (p instanceof DataProperty ){
			DataProperty dp = (DataProperty)p;
			if (!Auxiliar.equals(dp.getCardMax(),this.cardMax)){
			//	System.out.println("CARDMAX");
				return -1;}
			if (!Auxiliar.equals(dp.getCardMin(),this.cardMin)){
				//System.out.println("CARDMIN");
				return -1;}
			if (dp.getDataType()!=this.dataType){
				//System.out.println("DATATYPE");
				return -1;}
			if (!Auxiliar.equals(dp.getEnumList(),this.enumList)){
				//System.out.println("ENUMLIST");
				return -1;}
			if (!Auxiliar.equals(dp.getExcluList(),this.excluList)){
				//System.out.println("EXCLULIST");
				return -1;}
			if (!Auxiliar.equals(dp.getIdo(),this.ido)){
			//	System.out.println("IDO");
				return -1;}
			if (!Auxiliar.equals(dp.getIdProp(),this.idProp)){
				//System.out.println("IDPROP");
				return -1;}
			if (!Auxiliar.equals(dp.getIdto(),this.idto)){
				//System.out.println("IDTO");
				return -1;}
			if (!Auxiliar.equals(dp.getName(),this.name)){
				//System.out.println("NAME");
				return -1;}
			if (!Auxiliar.equals(dp.getTypeAccess(),this.typeAccess)){
				//System.out.println("TYPEACCESS");
				return -1;}
			if (!Auxiliar.equals(dp.getValues(),this.values)){
				//System.out.println("VALUES");
				return -1;}
			
			return 0;
		}else{
			//System.out.println("NO ES DATAPROPERTY");
			return -1;
		}
		
		
	}
	
	
	
}
