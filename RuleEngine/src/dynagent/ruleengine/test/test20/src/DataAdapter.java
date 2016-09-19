package dynagent.ruleengine.test.test20.src;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import dynagent.ruleengine.meta.api.Property;

public class DataAdapter {
	private Integer idoTemp;
	private Integer idtoTemp;
	private Map<Property,InfoOriginal> propertyAdapter= new HashMap<Property,InfoOriginal>();
	public DataAdapter(Integer idoTemp, Integer idtoTemp){
		this.idoTemp=idoTemp;
		this.idtoTemp=idtoTemp;
	}
	public void setIdtoTemp(Integer idtoTemp) {
		this.idtoTemp = idtoTemp;
	}
	public void setPropertyAdapter(Property idPropTemp,Integer idProp,Integer idoClass, Integer idtoClass){
		InfoOriginal info= new InfoOriginal(idProp,idoClass,idtoClass);
		propertyAdapter.put(idPropTemp, info);
	}
	public Integer getOriginalIdoClass(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdoClass();
		return result;
	}
	public Integer getOriginalIdtoClass(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdtoClass();
		return result;
	}
	public Integer getOriginalIdProp(Property idPropTemp){
		Integer result=null;
		InfoOriginal info=propertyAdapter.get(idPropTemp);
		result=info.getIdProp();
		return result;
	}
	public Integer getIdoTemp() {
		return idoTemp;
	}
	public Iterator<Property> getPropertyIterator(){
		Set<Property> properties= propertyAdapter.keySet();
		Iterator <Property> it=properties.iterator();
		return it;
	}
	public Property getProperty(Integer idProp){
		Iterator <Property> it=this.getPropertyIterator();
		Property p=null;
		while(it.hasNext()&& (p=it.next()).getIdProp().equals(idProp));
		return p;
	}
	public void setIdoTemp(Integer idoTemp) {
		this.idoTemp = idoTemp;
	}
}
