package dynagent.common.properties.values;

import org.jdom.Element;

import dynagent.common.utils.Auxiliar;

public class DoubleValue extends DataValue{
	private Double valueMin;
	private Double valueMax;
	
	public DoubleValue(Double ivalueMin,Double ivalueMax) {
		this.setValueMin(ivalueMin);
		this.setValueMax(ivalueMax);
	}
	
	public DoubleValue(int ivalueMin,int ivalueMax) {
		this.setValueMin(new Double(ivalueMin));
		this.setValueMax(new Double(ivalueMax));
	}
	
	public DoubleValue(Double value) {
		this.setValueMin(value);
		this.setValueMax(value);
	}
	
	public DoubleValue() {
	}
	
	
	public Double getValueMax() {
		return valueMax;
	}
	public void setValueMax(Double valueMax) {
		this.valueMax = valueMax;
	}
	public Double getValueMin() {
		return valueMin;
	}
	public void setValue(Double value) {
		this.valueMin = value;
		this.valueMax = value;
	}
	
	public void setValueMin(Double valueMin) {
		this.valueMin = valueMin;
	}
	
	public DoubleValue clone() {
		DoubleValue fv = new DoubleValue(valueMin,valueMax);
		fv.setEqualToValue(equalToValue);
		return fv;
	}

	public String toString(){
		String result="   <DoubleValue";
		result=result+"   valueMin="+this.getValueMin();
		result=result+"   valueMax="+this.getValueMax();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("DOUBLE_VALUE","TRUE");
		if (valueMin!=null)
			dataValue.setAttribute("VALUE_MIN",String.valueOf(valueMin));
		if (valueMax!=null)
			dataValue.setAttribute("VALUE_MAX",String.valueOf(valueMax));
		return dataValue;
	}
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof DoubleValue){
			DoubleValue dv=(DoubleValue)v;
			eq=( (this.getValueMin()==dv.getValueMin()) || (this.getValueMin()!=null&&dv.getValueMin()!=null&&this.getValueMin().equals(dv.getValueMin())))
			  &&( (this.getValueMax()==dv.getValueMax()) || (this.getValueMax()!=null&&dv.getValueMax()!=null&&this.getValueMax().equals(dv.getValueMax())));
			  
		}
		return eq;
	}
	
	@Override
	public Double getNumericValue() {
		Double val=0.0;
		if(this.getValueMin()!=null&&this.getValueMax()!=null&&this.getValueMin().doubleValue()==this.getValueMax().doubleValue()){
			val=this.getValueMax(); 
		}
		return val;
	}
	@Override
	public String getValue_s() {
		 
		return this.valueMax.toString();
	}
	
	public Double getValue(){
		Double result=null;
		if(Auxiliar.equals(this.getValueMin(), this.getValueMax())){
			result=this.getValueMin();
		}
		return result;
		
		
		
		
		
	}
	

}
