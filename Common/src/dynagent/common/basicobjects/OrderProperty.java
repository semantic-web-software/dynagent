package dynagent.common.basicobjects;

public class OrderProperty {
	int prop;
	String propName;
	Integer group;
	Integer idto;
	String idtoName;
	int order;
	int sec;
	public Integer getGroup() {
		return group;
	}
	public void setGroup(Integer group) {
		this.group = group;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}
	public int getProp() {
		return prop;
	}
	public void setProp(int prop) {
		this.prop = prop;
	}
	public int getSec() {
		return sec;
	}
	public void setSec(int sec) {
		this.sec = sec;
	}
	public String toString(){
		return "(ORDERPROPERTY (SEC "+sec+")(PROP "+prop+")(ORDER "+order+")(GROUP "+group+")(IDTO "+idto+"))";
	}
	
	public OrderProperty clone(){
		OrderProperty orderProperty=new OrderProperty();
		orderProperty.setIdto(idto);
		orderProperty.setIdtoName(idtoName);
		orderProperty.setPropName(propName);
		orderProperty.setProp(prop);
		orderProperty.setSec(sec);
		orderProperty.setOrder(order);
		
		return orderProperty;
	}
	public String getIdtoName() {
		return idtoName;
	}
	public void setIdtoName(String idtoName) {
		this.idtoName = idtoName;
	}
	public String getPropName() {
		return propName;
	}
	public void setPropName(String propName) {
		this.propName = propName;
	}
	

}
