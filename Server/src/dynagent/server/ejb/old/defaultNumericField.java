package dynagent.ejb.old;
/*
package dynagent.ejb;
import org.jdom.Element;

public class defaultNumericField extends Object implements field {

	int tm;
	String id;
	Double value=null;

	public defaultNumericField( int tm, String id, Double value){
		this.tm= tm;
		this.id= id;			
		this.value= value;
	}

	public int getTAPOS(){
		if( id.indexOf("@")==-1 )
			return Integer.parseInt( id );
		
		return Integer.parseInt( id.substring(id.indexOf("@")+1) );
	}

	public int getSintax(){
		return tm;
	}

	public int getIntValue(){
		return value.intValue();
	}

	public void inizialiceRestriction(){;}
	public Object getValue(){
		return value;
	}

	public float getFloatValue(){
		return value.floatValue();
	}

	public String getIdForm(){
		return id;
	}

	public int getExternalCode(){
		return -1;
	}

	public String getValueToString(){
		return value.toString();
	}

	public org.jdom.Element getAva(){
		org.jdom.Element ava= new org.jdom.Element("AVA");
		ava.setAttribute("ID",getIdForm());
		ava.setAttribute("TA_POS", String.valueOf( getTAPOS() ));
		ava.setAttribute("OP","=");
		ava.setAttribute("VALUE",getValueToString());
		ava.setAttribute("ID_TM",String.valueOf(getSintax()));
		ava.setAttribute("USER_SELECTION","TRUE");
		return ava;
	}

	public boolean isNull(){
		return value==null;
	}
	public boolean isNull(Object val){
		return val==null;
	}

      	public boolean isNullable(){
		return true;
	}

	public void resetRestriction(){;}
	public void reset(){;}
      	public boolean hasChanged(){return false;}
	public String getLabel(){ return null;}
	public void commitValorInicial(){;}
	public void setValue(Object value){ 
		if( value instanceof Double )
			this.value= (Double)value;
	}
}*/