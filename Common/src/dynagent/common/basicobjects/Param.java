package dynagent.common.basicobjects;

public class Param {

	private String name;
	private Integer valueCls;
	private Integer cardMin;
	private Integer cardMax;
	private String defaultValue;
	
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getValueCls() {
		return valueCls;
	}
	public void setValueCls(Integer valueCls) {
		this.valueCls = valueCls;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
