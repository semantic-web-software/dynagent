package dynagent.common.knowledge;

import org.jdom.Element;

import dynagent.common.utils.Auxiliar;

public class SelectQuery {

	private String idObject;
	private int idProp;
	private String alias;
	private Integer propFilter;
	private String valueFilter;
	
	public SelectQuery(String idObject,int idProp, Integer propF, String valueF) {
		this.idObject = idObject;
		this.idProp = idProp;
		this.propFilter = propF;
		this.valueFilter = valueF;
	}

	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Integer getPropFilter() {
		return propFilter;
	}
	public void setPropFilter(Integer propFilter) {
		this.propFilter = propFilter;
	}

	public String getValueFilter() {
		return valueFilter;
	}
	public void setValueFilter(String valueFilter) {
		this.valueFilter = valueFilter;
	}

	public String getIdObject() {
		return idObject;
	}
	public void setIdObject(String idObject) {
		this.idObject = idObject;
	}

	public int getIdProp() {
		return idProp;
	}
	public void setIdProp(int idProp) {
		this.idProp = idProp;
	}

	public SelectQuery clone() {
		SelectQuery sq = new SelectQuery(this.idObject,this.idProp,this.propFilter,this.valueFilter);
		return sq;
	}
	
	public Element toElement(){
        Element res = new Element("SELECT_QUERY");
        res.setAttribute("ID_OBJECT", idObject);
        res.setAttribute("ID_PROP", String.valueOf(idProp));
        if (propFilter!=null)
        	res.setAttribute("PROP_FILTER", String.valueOf(propFilter));
        if (valueFilter!=null)
        	res.setAttribute("VALUE_FILTER", valueFilter);
        
        return res;
    }
	
	public String toString() {
		String str = "idObject " + idObject + ", idProp " + idProp + ", propFilter " + propFilter + ", valueFilter " + valueFilter;
		return str;
	}
	
	public boolean equals(Object o){
		SelectQuery c=(SelectQuery)o;
		return Auxiliar.equals(this.getIdObject(),c.getIdObject()) && Auxiliar.equals(this.getIdProp(),c.getIdProp()) &&
				Auxiliar.equals(this.getPropFilter(),c.getPropFilter()) && Auxiliar.equals(this.getValueFilter(),c.getValueFilter()) ;
	}
}