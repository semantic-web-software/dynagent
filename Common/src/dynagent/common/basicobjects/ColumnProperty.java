package dynagent.common.basicobjects;


public class ColumnProperty {

	Integer idtoParent;
	String idtoParentName;
	Integer idto;
	String idtoName;
	Integer idProp;
	String idPropName;
	Integer priority;
	Integer idPropF;
	String propFilter;
	String valueFilter;
	String idPropPath;
	
	public String getIdPropPath() {
		return idPropPath;
	}
	public void setIdPropPath(String idPropPath) {
		this.idPropPath = idPropPath;
	}
	public String getPropFilter() {
		return propFilter;
	}
	public void setPropFilter(String propFilter) {
		this.propFilter = propFilter;
	}
	public String getValueFilter() {
		return valueFilter;
	}
	public void setValueFilter(String valueFilter) {
		this.valueFilter = valueFilter;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer order) {
		this.priority = order;
	}
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}

	public String toString(){
		String msg = "(COLUMNPROPERTY (CLASSPARENT "+this.idtoParent+")(CLASS "+this.idto+")(PROP "+this.idProp+
		")(PROPPATH "+this.idPropPath+
		")(PROPFILTER "+this.idPropF+")(VALUEFILTER "+this.valueFilter+")(ORDER "+this.priority+"))";
		return msg;
	}
	//No tiene en cuenta el atributo order. Se usa para identificar ColumnProperty con todos los campos iguales excepto la priority
	public String getIdString(){
		String msg = idtoParent+":"+idto+":"+idProp+":"+idPropF+":"+valueFilter;
		return msg;
	}
	public Integer getIdtoParent() {
		return idtoParent;
	}
	public void setIdtoParent(Integer idtoParent) {
		this.idtoParent = idtoParent;
	}
	public Integer getIdPropF() {
		return idPropF;
	}
	public void setIdPropF(Integer idPropF) {
		this.idPropF = idPropF;
	}
	public String getIdPropName() {
		return idPropName;
	}
	public void setIdPropName(String idPropName) {
		this.idPropName = idPropName;
	}
	public String getIdtoName() {
		return idtoName;
	}
	public void setIdtoName(String idtoName) {
		this.idtoName = idtoName;
	}
	public String getIdtoParentName() {
		return idtoParentName;
	}
	public void setIdtoParentName(String idtoParentName) {
		this.idtoParentName = idtoParentName;
	}

	public ColumnProperty clone(){
		ColumnProperty col=new ColumnProperty();
		col.setIdProp(idProp);
		col.setIdPropF(idPropF);
		col.setIdPropName(idPropName);
		col.setIdto(idto);
		col.setIdtoName(idtoName);
		col.setIdtoParent(idtoParent);
		col.setIdtoParentName(idtoParentName);
		col.setPriority(priority);
		col.setPropFilter(propFilter);
		col.setValueFilter(valueFilter);
		col.setIdPropPath(idPropPath);

		return col;
	}

}
