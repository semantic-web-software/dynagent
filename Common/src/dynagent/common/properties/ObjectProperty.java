/***
 * Property.java
 * @author Jose A. Zamora Aguilera
 */

package dynagent.common.properties;

import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.knowledge.QuantityDetail;
import dynagent.common.properties.values.ObjectValue;
import dynagent.common.properties.values.Value;
import dynagent.common.utils.Auxiliar;

public class ObjectProperty extends Property {
	
	private Integer commonRange;
		
	
	/**
	 * rangoList: representa los conjuntos que conforman el rango de la property (Serían los conjuntos que participan con OR).
	 * Esta lista no tendrá elementos cuando los únicos valores posibles sean uno o ciertos individuos, en ese caso los valores
	 * posibles vienen dados en enumList
	 */
	
	private LinkedList<Integer> rangoList=new LinkedList<Integer>();

	private  LinkedList<QuantityDetail> QuantityDetailList = new LinkedList<QuantityDetail>();
	
	
	public LinkedList<Integer> getRangoList() {
		return rangoList;
	}
	public void setRangoList(LinkedList<Integer> rangoList) {
		this.rangoList = rangoList;
	}
	public  LinkedList<ObjectValue> enumList = new LinkedList<ObjectValue>();   
	public LinkedList<ObjectValue> excluList = new LinkedList<ObjectValue>();
	
	public LinkedList<ObjectValue> getEnumList() {
		return enumList;
	}
	public LinkedList<ObjectValue> getExcluList() {
		return excluList;
	}
	public void setExcluList(LinkedList<ObjectValue> excluList) {
		this.excluList = excluList;
	}
	
	


	public void setEnumList(LinkedList<ObjectValue> enumList) {
		this.enumList = enumList;
	}
	
	
		
	
	
	public ObjectProperty clone() {
		ObjectProperty p = new ObjectProperty();
		p.setName(getName());
		p.setIdProp(getIdProp());
		p.setIdo(getIdo());
		p.setIdto(getIdto());
		p.setCardMin(getCardMin());
		p.setCardMax(getCardMax());
		p.setTypeAccess(getTypeAccess());
		p.setCommonRange(this.getCommonRange());
		
		LinkedList<QuantityDetail> QuantityDetailListClone = new LinkedList<QuantityDetail>();
		for (int i=0;i<getQuantityDetailList().size();i++)
			QuantityDetailListClone.add(getQuantityDetailList().get(i).clone());
		p.setQuantityDetailList(QuantityDetailListClone);
		LinkedList<Integer> rangoListClone = new LinkedList<Integer>(); 
		for (int i=0;i<rangoList.size();i++)
			rangoListClone.add(rangoList.get(i));
		p.setRangoList(rangoListClone);
	
		LinkedList<Value> valueListClone = new LinkedList<Value> ();
		for (int i=0;i<this.values.size();i++)
			valueListClone.add(values.get(i).clone());
		p.setValues(valueListClone);
		LinkedList<ObjectValue> enumListClone = new LinkedList<ObjectValue> ();
		for (int i=0;i<enumList.size();i++)
			enumListClone.add(enumList.get(i).clone());
		p.setEnumList(enumListClone);
		
		LinkedList<ObjectValue> excluListClone = new LinkedList<ObjectValue> ();
		for (int i=0;i<excluList.size();i++)
			excluListClone.add(excluList.get(i).clone());
		p.setExcluList(excluListClone);
		return p;
	}
	
	
	public String quantityDetailListToString(LinkedList<QuantityDetail> qdlist){
		String result=null;
		for(int i=0;i<qdlist.size();i++){
			if(qdlist.get(i)!=null){
				result=result+qdlist.get(i).toString();
			}
		}
		return result;
	}
	public LinkedList<QuantityDetail> getQuantityDetailList() {
		return QuantityDetailList;
	}
	public void setQuantityDetailList(LinkedList<QuantityDetail> quantityDetailList) {
		QuantityDetailList = quantityDetailList;
	}
	
	public String toString(){
		String result="<ObjectProperty";
		result=result+"  name="+super.getName();
		result=result+"  ido="+super.getIdo();
		result=result+"  idto="+super.getIdto();
		result=result+"  idProp="+super.getIdProp();
		result=result+"  cardMin="+super.getCardMin();
		result=result+"  cardMax="+super.getCardMax();
		result=result+"  typeAccess="+super.getTypeAccess();
		result=result+"  rangoList="+this.getRangoList().toString();
		result=result+"  rangeCommon="+this.getCommonRange();
		if(values.size()>0)
			result=result+"\n   <valueList>    "+this.ValueListToString(this.getValues())+"\n   </valueList>";
		if(enumList.size()>0)
			result=result+"\n   <enumList>    "+this.ValueListToString(this.getEnumList())+"\n   </enumList>";
		if(excluList.size()>0)
			result=result+"\n   <excluList>    "+this.ValueListToString(this.getExcluList())+"\n   </excluList>";
		if(getQuantityDetailList().size()>0)
			result=result+"\n   <quantityDetailList>    "+this.quantityDetailListToString(this.getQuantityDetailList())+"\n   </quantityDetailList>";
		
		result=result+"\n</ObjectProperty>";
		
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
        
//      se añade OPROP, QUANTITY_DETAIL_LIST, RANGO_LIST, VALUE_LIST, ENUM_LIST, EXCLU_LIST
		property.setAttribute("OPROP","TRUE");
        if (QuantityDetailList.size()>0) {
        	Element elemQuantityDetailList = new Element("QUANTITY_DETAIL_LIST");
        	property.addContent(elemQuantityDetailList);
        	for (int i=0;i<QuantityDetailList.size();i++) {
        		QuantityDetail qd = QuantityDetailList.get(i);
        		Element quantityDetail = new Element("QUANTITY_LIST");
        		elemQuantityDetailList.addContent(quantityDetail);
        		if (qd.getValue()!=null)
        			quantityDetail.setAttribute("VALUE",String.valueOf(qd.getValue()));
        		if (qd.getValueCls()!=null)
        			quantityDetail.setAttribute("VALUE_CLS",String.valueOf(qd.getValueCls()));
        		if (qd.getCardinalityEspecifyMin()!=null)
        			quantityDetail.setAttribute("CARD_ESP_MIN",String.valueOf(qd.getCardinalityEspecifyMin()));
        		if (qd.getCardinalityEspecifyMax()!=null)
        			quantityDetail.setAttribute("CARD_ESP_MAX",String.valueOf(qd.getCardinalityEspecifyMax()));
        	}
        }
        if (rangoList.size()>0) {
        	String rangoListStr = "";
        	for (int i=0;i<rangoList.size();i++) {
        		if (rangoListStr.length()>0)
        			rangoListStr += ",";
        		rangoListStr += rangoList.get(i);
        	}
        	property.setAttribute("RANGO_LIST",rangoListStr);
        }
        if (super.getValues().size()>0) {
        	Element elemValueList = new Element("VALUES");
        	property.addContent(elemValueList);
        	for (int i=0;i<super.getValues().size();i++) {
        		ObjectValue ov = (ObjectValue)super.getValues().get(i);
        		Element objectValue = ov.toElement();
        		elemValueList.addContent(objectValue);
        	}
        }
        if (enumList.size()>0) {
        	Element elemEnumList = new Element("ENUM_LIST");
        	property.addContent(elemEnumList);
        	for (int i=0;i<enumList.size();i++) {
        		ObjectValue ov = enumList.get(i);
        		Element objectValue = ov.toElement();
        		elemEnumList.addContent(objectValue);
        	}
        }
        if (excluList.size()>0) {
        	Element elemExcluList = new Element("EXCLU_LIST");
        	property.addContent(elemExcluList);
        	for (int i=0;i<excluList.size();i++) {
        		ObjectValue ov = excluList.get(i);
        		Element objectValue = ov.toElement();
        		elemExcluList.addContent(objectValue);
        	}
        }
		return property;
	}
	public boolean equals(Object p){
		
		if (p instanceof ObjectProperty ){
			ObjectProperty op = (ObjectProperty)p;
			if (!Auxiliar.equals(op.getCardMax(),this.cardMax))
				return false;
			if (!Auxiliar.equals(op.getCardMin(),this.cardMin))
				return false;
			
			if (op.getEnumList().size()!=this.enumList.size() ||( op.getEnumList().size()==this.enumList.size() && !op.getEnumList().containsAll(this.enumList)))
				return false;
			if (op.getExcluList().size()!=this.excluList.size() ||(!op.getExcluList().containsAll(this.excluList)))
				return false;
			if (!Auxiliar.equals(op.getIdo(),this.ido))
				return false;
			if (!Auxiliar.equals(op.getIdProp(),this.idProp))
				return false;
			if (!Auxiliar.equals(op.getIdto(),this.idto))
				return false;
			
			if (!Auxiliar.equals(op.getName(),this.name))
				return false;
			if (!Auxiliar.equals(op.getTypeAccess(),this.typeAccess))
				return false;
			if (op.getValues().size()!=this.values.size() ||(!op.getValues().containsAll(this.values)))
				return false;
			
			if (!Auxiliar.equals(op.getCommonRange(),this.commonRange))
				return false;
			if (op.getQuantityDetailList().size()!=this.QuantityDetailList.size() ||(!op.getQuantityDetailList().containsAll(this.QuantityDetailList)))
				return false;
			if (op.getRangoList().size()!=this.rangoList.size() ||(!op.getRangoList().containsAll(this.rangoList)))
				return false;
			
			return true;
		}else{
			return false;
		}
		
	}
	public Integer getCommonRange() {
		return commonRange;
	}
	public void setCommonRange(Integer commonRange) {
		this.commonRange = commonRange;
	}

	
	
	
	
	
}
