package dynagent.common.knowledge;


/**
 * Esta clase representa una qualified cardinality restriction (QCR), esto es una restricción de rango y cardinalidad 
 * específica. 
 * @author zamora
 *
 */
public class QuantityDetail {
	private Object value;
	private Integer valueCls;
	private Integer cardinalityEspecifyMin;
	private Integer cardinalityEspecifyMax;
	
	
	public Integer getCardinalityEspecifyMax() {
		return cardinalityEspecifyMax;
	}
	public void setCardinalityEspecifyMax(Integer cardinalityEspecifyMax) {
		this.cardinalityEspecifyMax = cardinalityEspecifyMax;
	}
	public Integer getCardinalityEspecifyMin() {
		return cardinalityEspecifyMin;
	}
	public void setCardinalityEspecifyMin(Integer cardinalityEspecifyMin) {
		this.cardinalityEspecifyMin = cardinalityEspecifyMin;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public Integer getValueCls() {
		return valueCls;
	}
	public void setValueCls(Integer valueCls) {
		this.valueCls = valueCls;
	}
	
	public QuantityDetail clone() {
		QuantityDetail q = new QuantityDetail();
		q.setValue(value);
		q.setValueCls(valueCls);
		q.setCardinalityEspecifyMin(cardinalityEspecifyMin);
		q.setCardinalityEspecifyMax(cardinalityEspecifyMax);
		return q;
	}
	
	public String toString(){
		String result;
		result="   <QuantityDetail";
		result=result+"   qmin="+this.getCardinalityEspecifyMin();
		result=result+"   qmax="+this.getCardinalityEspecifyMax();
		result=result+"   valueCls="+this.getValueCls();
		result=result+"   value="+this.getValue();
		result=result+"  /QuantityDetail>";
		return result;
	}
	public boolean equals(Object qd){
		if (qd instanceof QuantityDetail){
			QuantityDetail quant=(QuantityDetail)qd;
			if(quant.getCardinalityEspecifyMax()!=this.cardinalityEspecifyMax)
				return false;
			if(quant.getCardinalityEspecifyMin()!=this.cardinalityEspecifyMin)
				return false;
			if(!quant.getValue().equals(this.value))
				return false;
			if(quant.getValueCls()!=this.valueCls)
				return false;
			
			return true;
		}else{
			return false;
		}
	}
	
}
