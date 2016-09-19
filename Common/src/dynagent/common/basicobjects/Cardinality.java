package dynagent.common.basicobjects;

public class Cardinality implements Cloneable{
	private String prop;
	private Integer idProp;
	private String className;
	private Integer idtoClass;
	private Integer cardMin;
	private Integer cardMax;
	
	public Cardinality(){}
	
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
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public Integer getIdProp() {
		return idProp;
	}
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	public Integer getIdtoClass() {
		return idtoClass;
	}
	public void setIdtoClass(Integer idtoClass) {
		this.idtoClass = idtoClass;
	}
	public String getProp() {
		return prop;
	}
	public void setProp(String prop) {
		this.prop = prop;
	}
	public String toString(){
		return "(CARDINALITY (PROP "+prop+")(CLASS "+className+")(CARDMIN "+cardMin+")(CARDMAX "+cardMax+"))";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Cardinality card=new Cardinality();
		card.setClassName(className);
		card.setIdtoClass(idtoClass);
		card.setProp(prop);
		card.setIdProp(idProp);
		card.setCardMin(cardMin);
		card.setCardMax(cardMax);
		return card;
	}
}
