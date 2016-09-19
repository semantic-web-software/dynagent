package dynagent.common.properties.values;

import org.jdom.Element;

public class UnitValue  extends DoubleValue {
	
	private Integer unit;
	
	public UnitValue() {
	}
	
	public Integer getUnit(){
		return unit;
	}
	
	public void setUnit(Integer unit) {
		this.unit = unit;
	}
	
	public UnitValue clone() {
		UnitValue uv = new UnitValue();
		uv.setUnit(unit);
		uv.setValueMin(getValueMin());
		uv.setValueMax(getValueMax());
		return uv;
	}
		
	public String toString(){
		String result="   <UnitValue";
		result=result+"   valueMin="+this.getValueMin();
		result=result+"   valueMax="+this.getValueMax();
		result=result+"   unit="+this.getUnit();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("UNIT_VALUE","TRUE");
		if (unit!=null)
			dataValue.setAttribute("UNIT",String.valueOf(unit));
		if (this.getValueMin()!=null)
			dataValue.setAttribute("VALUE_MIN",String.valueOf(this.getValueMin()));
		if (this.getValueMax()!=null)
			dataValue.setAttribute("VALUE_MAX",String.valueOf(this.getValueMax()));
		return dataValue;
	}

	public DataValue changeValueToUnit(Integer newUnit){
		//TODO IMPLEMENTAR changeValueToUnit
		DataValue dvc = null;
		if(this.getUnit()!=null&&this.getUnit().intValue()!=newUnit.intValue()){
			//TODO cambio de unidad
		}
		return dvc;
	}
	
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof UnitValue){
			UnitValue dv=(UnitValue)v;
			eq=( (this.getValueMin()==dv.getValueMin()) || (this.getValueMin()!=null&&dv.getValueMin()!=null&&this.getValueMin().equals(dv.getValueMin())))
			  &&( (this.getValueMax()==dv.getValueMax()) || (this.getValueMax()!=null&&dv.getValueMax()!=null&&this.getValueMax().equals(dv.getValueMax())))
			  &&( (this.getUnit()==dv.getUnit()) || (this.getUnit()!=null&&dv.getUnit()!=null&&this.getUnit().equals(dv.getUnit())));
			
		}
		return eq;
	}
	
	@Override
	public Double getNumericValue() {
		Double val=0.0;
		if(this.getValueMin()!=null&&this.getValueMax()!=null&&this.getValueMin().doubleValue()==this.getValueMax().doubleValue()){
			val=this.getValueMax().doubleValue(); 
		}
		return val;
	}
	
}
