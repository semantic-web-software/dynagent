package dynagent.common.properties.values;

import org.jdom.Element;

import dynagent.common.Constants;


public class BooleanValue extends DataValue {
	Boolean bvalue;
	private String comment;
	
	public BooleanValue(boolean bvalue) {
		this.setBvalue(bvalue);
	}
	
	public BooleanValue(boolean bvalue,String comment) {
		this.setBvalue(bvalue);
		this.comment=comment;
	}
	
	public BooleanValue() {
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getBvalue() {
		return bvalue;
	}

	public void setBvalue(Boolean bvalue) {
		this.bvalue = bvalue;
	}
	
	public BooleanValue clone() {
		BooleanValue bv = new BooleanValue(bvalue,comment);
		
		
		return bv;
	}
	
	public String toString(){
		String result="   <BooleanValue";
		result=result+"   booleanvalue="+this.getBvalue();
		result=result+"   comment="+this.getComment();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("BOOLEAN_VALUE","TRUE");
		if (bvalue!=null)
			dataValue.setAttribute("VALUE",String.valueOf(bvalue));
		if (comment!=null)
			dataValue.setText(comment);
		return dataValue;
	}
	
	public boolean isCompatibleWith (BooleanValue dvRef){
		boolean resultado = false;
		if(this.getBvalue()!= null && dvRef.getBvalue() != null){
			if(this.getBvalue().equals(dvRef.getBvalue())){
				resultado = true;
			}
		}
		return resultado;
	}
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof BooleanValue){
			BooleanValue bv=(BooleanValue)v;
			eq= (  (this.getBvalue()==bv.getBvalue())||(this.getBvalue()!=null&&bv.getBvalue()!=null&&this.getBvalue().equals(bv.getBvalue())))
			  &&( (this.getComment()==bv.getComment())||(this.getComment()!=null&&bv.getComment()!=null&&this.getComment().equals(bv.getComment())));
		}
		return eq;
	}

	@Override
	public Double getNumericValue() {
		Double val=0.0;
		if(this.getBvalue())
			val=1.0*(Constants.ID_BOOLEAN_TRUE);
		return val;
	}

	@Override
	public String getValue_s() {

		return this.bvalue.toString();
	}
}
