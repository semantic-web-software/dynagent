package dynagent.common.properties.values;

import java.util.Date;

import org.jdom.Element;

import dynagent.common.Constants;

public class TimeValue extends DataValue {
	/**
	 * Representa la cantidad en segundos desde un instante de referencia.
	 * Hasta el momento tenemos dos tipos de instantes de referencias (ver en constantes los instantes de 
	 * referencia)
	 * - begin1970: Para medir instantes de tiempo desde 1970 ( o anteriores con valores negativos)
	 * Lo que permite medir tanto dateTime, como dates
	 * 
	 * -   beginDay: Para medir los segundos transcurridos desde las 00:00. Util para medir
	 * horas guardando un float
	 * 
	 * Observación: También son posibles médidas de tiempo más precisas que segundos. Por ejemplo
	 * el timeValue con valores:
		{relativeSeconds=3600,001, referenceInstant="beginDay"} 
		representa las 01:00 horas y 1 milisegundo.
		
		Relación con tipos de dato: Al definir una dataProperty en nuestro modelo para expresar
		algún tipo de médida de tiempo disponemos de 3 tipos:
		dateTime,date,Time. Los dos primeros tipos tendrán como valores TimeValue con 
		referenceInstant=begin1970 y el último tendrá referenceInstant=beginDay
		
	 */
	
	
	
	private Long relativeSecondsMin;
	private Long relativeSecondsMax;
	private String referenceInstant;
	public Long getRelativeSecondsMin() {
		return relativeSecondsMin;
	}
	public TimeValue(){}
	
	public TimeValue(Date dat){
		this.relativeSecondsMax=((Date)dat).getTime()/Constants.TIMEMILLIS;
		this.relativeSecondsMin=((Date)dat).getTime()/Constants.TIMEMILLIS;
	}
	
	
	public TimeValue(Long relativeSeconds){
		this.relativeSecondsMax=relativeSeconds;
		this.relativeSecondsMin=relativeSeconds;
		
		
	}

	public TimeValue(Long relativeSecondsMin,Long relativeSecondsMax ){
		this.relativeSecondsMin=relativeSecondsMin;
		this.relativeSecondsMax=relativeSecondsMax;
	}
	
	public void setRelativeSecondsMin(Long relativeSeconds) {
		this.relativeSecondsMin = relativeSeconds;
	}

	public String getReferenceInstant() {
		return referenceInstant;
	}

	public void setReferenceInstant(String referenceInstant) {
		this.referenceInstant = referenceInstant;
	}
	
	public String toString(){
		String result="   <TimeValue";
		result=result+"   relativeSecondsMin="+this.getRelativeSecondsMin();
		result=result+"   relativeSecondsMax="+this.getRelativeSecondsMax();
		result=result+"   from="+this.getReferenceInstant();
		result=result+">";
		return result;
	}
	
	public Element toElement() {
		Element dataValue = new Element("DATA_VALUE");
		dataValue.setAttribute("TIME_VALUE","TRUE");
		//if (referenceInstant!=null)
			//dataValue.setText(referenceInstant);
		if (relativeSecondsMin!=null)
			dataValue.setAttribute("RELATIVE_SECONDS_MIN",String.valueOf(relativeSecondsMin));
		if (relativeSecondsMax!=null)
			dataValue.setAttribute("RELATIVE_SECONDS_MAX",String.valueOf(relativeSecondsMax));
		return dataValue;
	}
	
	
	public TimeValue clone() {
		TimeValue tv = new TimeValue();
		tv.setReferenceInstant(this.getReferenceInstant());
		tv.setRelativeSecondsMin(this.getRelativeSecondsMin());
		tv.setRelativeSecondsMax(this.getRelativeSecondsMax());
		return tv;
	}
	
	public boolean  equals (Object v){
		boolean eq=false;
		if(v instanceof TimeValue){
			TimeValue tv=(TimeValue)v;
			eq=( (this.getReferenceInstant()==tv.getReferenceInstant()) || (this.getReferenceInstant()!=null&&tv.getReferenceInstant()!=null&&this.getReferenceInstant().equals(tv.getReferenceInstant())))
			  &&( (this.getRelativeSecondsMin()==tv.getRelativeSecondsMin()) || (this.getRelativeSecondsMin()!=null&&tv.getRelativeSecondsMin()!=null&&this.getRelativeSecondsMin().equals(tv.getRelativeSecondsMin())))
					  &&( (this.getRelativeSecondsMax()==tv.getRelativeSecondsMax()) || (this.getRelativeSecondsMax()!=null&&tv.getRelativeSecondsMax()!=null&&this.getRelativeSecondsMax().equals(tv.getRelativeSecondsMax()))); 
		}
		return eq;
	}

	public Long getRelativeSecondsMax() {
		return relativeSecondsMax;
	}

	public void setRelativeSecondsMax(Long relativeSecondsMax) {
		this.relativeSecondsMax = relativeSecondsMax;
	}
	
	
	public void setRelativeSeconds(Long relativeSeconds) {
		this.relativeSecondsMax = relativeSeconds;
		this.relativeSecondsMin = relativeSeconds;		
	}
	
	
	public Double getNumericValue() {
		return new Double(relativeSecondsMax);		
	}

	@Override
	public String getValue_s() {
		
		return new Long(this.relativeSecondsMax).toString();
	}
	
	public Date getDate(){
		Date result=null;
		if(this.getRelativeSecondsMax()!=null)
			return new Date(this.getRelativeSecondsMax()*Constants.TIMEMILLIS);
		else return null;
		
		
	}
	
	

}
