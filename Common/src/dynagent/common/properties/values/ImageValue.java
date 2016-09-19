package dynagent.common.properties.values;

import org.jdom.Element;

public class ImageValue extends DataValue{
	private String value;
	
	public String getValue() {
		return value;
	}
	
	public ImageValue(){
		
	}
	public ImageValue(String value) {
		this.value = value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public ImageValue clone() {
		ImageValue sv = new ImageValue();
		sv.setValue(value);
		return sv;
	}

	public String toString(){
		String result="   <ImageValue";
		result=result+"   value="+this.getValue();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("IMAGE_VALUE","TRUE");
		if (value!=null)
			dataValue.setText(value);
		return dataValue;
	}
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof StringValue){
			StringValue dv=(StringValue)v;
			eq=(this.getValue()==dv.getValue()) || (this.getValue()!=null&&dv.getValue()!=null&&this.getValue().equals(dv.getValue()));
		}
		return eq;
	}
	@Override
	public Double getNumericValue() {
		return 0.d;
	}

	@Override
	public String getValue_s() {

		return value;
	}
	
}
