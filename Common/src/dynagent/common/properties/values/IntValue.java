package dynagent.common.properties.values;

import org.jdom.Element;

public class IntValue extends DataValue{
	private Integer valueMin;
	private Integer valueMax;

	public Integer getValueMax() {
		return valueMax;
	}
	
	public IntValue() {
	}
	public IntValue(Integer ivalue) {
		this.setValueMin(ivalue);
		this.setValueMax(ivalue);
	}
		
	public IntValue(Integer ivalueMin,Integer ivalueMax) {
		this.setValueMin(ivalueMin);
		this.setValueMax(ivalueMax);
	}
	public void setValueMax(Integer valueMax) {
		this.valueMax = valueMax;
	}
	public Integer getValueMin() {
		return valueMin;
	}
	public void setValueMin(Integer valueMin) {
		this.valueMin = valueMin;
	}
	
	public void setValue(Integer value) {
		this.valueMin = value;
		this.valueMax = value;
	}
	
	public IntValue clone() {
		IntValue iv = new IntValue();
		iv.setValueMin(valueMin);
		iv.setValueMax(valueMax);
		iv.setEqualToValue(equalToValue);
		return iv;
	}

	public String toString(){
		String result="   <IntValue";
		result=result+"   valueMin="+this.getValueMin();
		result=result+"   valueMax="+this.getValueMax();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("INT_VALUE","TRUE");
		if (valueMin!=null)
			dataValue.setAttribute("VALUE_MIN",String.valueOf(valueMin));
		if (valueMax!=null)
			dataValue.setAttribute("VALUE_MAX",String.valueOf(valueMax));
		return dataValue;
	}
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof IntValue){
			IntValue iv=(IntValue)v;
			eq=( (this.getValueMin()==iv.getValueMin()) || (this.getValueMin()!=null&&iv.getValueMin()!=null&&this.getValueMin().equals(iv.getValueMin())))
			  &&( (this.getValueMax()==iv.getValueMax()) || (this.getValueMax()!=null&&iv.getValueMax()!=null&&this.getValueMax().equals(iv.getValueMax()))); 
		}
		return eq;
	}
	@Override
	public Double getNumericValue() {
		Double val=0.0;
		if(this.getValueMin()!=null&&this.getValueMax()!=null&&this.getValueMin().intValue()==this.getValueMax().intValue()){
			val=this.getValueMax().doubleValue(); 
		}
		return val;
	}

	@Override
	public String getValue_s() {
	//Hacemos que sea double para que si luego se utiliza para buscar un fact pueda encontrarlo ya que en fact se guarda como double
	return new Double(this.valueMax).toString();
	}
	
}
