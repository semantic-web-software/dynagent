/***
 * Instance.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 * @description It represents an instance reg in database
 */

package dynagent.common.basicobjects;


import java.util.LinkedList;

import dynagent.common.Constants;
import dynagent.common.knowledge.FactInstance;
import dynagent.common.utils.Auxiliar;



public class Instance {
	
	private String IDTO;
	private String IDO;
	private String PROPERTY;
	private String VALUE;
	private String VALUECLS;
	private String QMIN;
	private String QMAX;
	private String OP;
	private String NAME;
	private boolean VIRTUAL=false;

	
	public Instance(){}
	
	
	public Instance(String idto, String name, String op){
		this.IDTO=idto;
		this.NAME=name;
		this.OP=op;
		
	}
	
	public Instance(String idto, String idprop, String value, String valuecls, String name,String op){
		this(idto, name, op);
		this.PROPERTY=idprop;
		this.VALUE=value;
		this.VALUECLS=valuecls;
		
	}
	
	public Instance(String idto, String ido,String idprop, String value, String valuecls,String qmin,String qmax,String op, String name){
		this.IDTO=idto;
		this.IDO=ido;
		this.PROPERTY=idprop;
		this.VALUE=value;
		this.VALUECLS=valuecls;
		this.QMIN=qmin;
		this.QMAX=qmax;
		this.OP=op;
		this.NAME=name;
	}

	public String getIDO() {
		return IDO;
	}

	public void setIDO(String ido) {
		IDO = ido;
	}


	public String getIDTO() {
		return IDTO;
	}

	public void setIDTO(String idto) {
		IDTO = idto;
	}

	

	public String getNAME() {
		return NAME;
	}

	public void setNAME(String name) {
		NAME = name;
	}

	public String getOP() {
		return OP;
	}

	public void setOP(String op) {
		OP = op;
	}

	public String getPROPERTY() {
		return PROPERTY;
	}

	public void setPROPERTY(String property) {
		PROPERTY = property;
	}

	public String getQMAX() {
		return QMAX;
	}

	public void setQMAX(String QMax) {
		QMAX = QMax;
	}

	public String getQMIN() {
		return QMIN;
	}

	public void setQMIN(String QMin) {
		QMIN = QMin;
	}



	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String value) {
		VALUE = value;
	}

	public String getVALUECLS() {
		return VALUECLS;
	}

	public void setVALUECLS(String valuecls) {
		VALUECLS = valuecls;
	}

	public FactInstance toFactInstance(){
		FactInstance f = new FactInstance();
		
		
		if(this.getIDO() != null)
			f.setIDO(new Integer(this.getIDO()).intValue());
		
		if(this.getIDTO() != null)
			f.setIDTO(new Integer(this.getIDTO()).intValue());
		
		f.setCLASSNAME(this.getNAME());
		
		if(this.getPROPERTY() != null)
			f.setPROP(new Integer(this.getPROPERTY()).intValue());
		
		if(this.getQMAX() != null)
			f.setQMAX(new Double(this.QMAX).doubleValue());
		
		if(this.getQMIN() != null)
			f.setQMIN(new Double(this.getQMIN()).doubleValue());
		
	
		f.setVALUE(this.getVALUE());
		
		//PARA LOS BOOLEANOS EXTENDIDOS: Si el operador es nulo (valor de propiedad de un individuo) y el q min y qmax está informado y value tb está informado es pq value tiene un comentario al valor
		if(this.getOP()==null&&this.getQMAX()!=null&&(this.getQMAX().equals(this.getQMIN()))){
			f.setVALUE(this.getVALUE());
			
		}

		if(this.getVALUECLS() != null)
			f.setVALUECLS(new Integer(this.getVALUECLS()));
		
		f.setExistia_BD(true);
		f.setOP(this.getOP());
		
		return f;
	}
	
	
	public FactInstance toIPropertyDef(){
		return this.toFactInstance();
	}
	/**
	 * @author zamora
	 * @return
	 */
	
	//TODO Cuando usemos la tabla O_Datos_Atrib deberemos insertar los 2 campos nuevos aqui tambien
	public O_Datos_Attrib  toODatosAtrib(){
			O_Datos_Attrib od=new O_Datos_Attrib ();
			if(this.getIDO()!=null)
				od.setIDO(new Integer(this.getIDO()));
			if(this.getIDTO()!=null)
				od.setIDTO(new Integer(this.IDTO));
			if(this.getPROPERTY()!=null)
				od.setPROPERTY(new Integer(this.getPROPERTY()));
			if(this.getQMAX()!=null)
				od.setQMAX(new Double(this.getQMAX()));
			if(this.getQMIN()!=null)
				od.setQMIN(new Double(this.getQMIN()));
			
			
			
			//od.setVALTEXTO(this.getVALUE());
			if(Auxiliar.hasIntValue(this.getVALUECLS()))
				od.setVALUECLS(new Integer(this.getVALUECLS()));
			else
				System.err.println(" WARNING:  Instance.toODatosAtrib  recibe ins con valuecls=null. ins="+this);
			if(this.getVALUE()!=null){
				if(!this.getVALUECLS().equals(String.valueOf(Constants.IDTO_STRING))&&Auxiliar.hasIntValue(this.getVALUE())){
					od.setVALNUM(new Integer(this.getVALUE()));
					
				}
				else{
					od.setVALTEXTO(this.getVALUE());
				}
			}
			return od;
		
		
	}
	
	
	
	
	public String toString() {
		String result="   <instance  IDTO="+this.IDTO+" IDO="+this.IDO+"    PROP="+this.PROPERTY+"   VALUE="+this.VALUE+"   VALUECLS="+this.VALUECLS;
		result+=" QMIN="+this.QMIN+"   QMAX="+this.QMAX+"   OP="+this.OP+"   NAME="+this.NAME +"	VIRTUAL="+this.VIRTUAL+">";
		return result;
	}
	
	public String toStringNotNull() {
		String result="   <instance";
		if (this.IDTO!=null)
			result=result+"  IDTO="+this.IDTO;
		if (this.IDO!=null)
			result=result+"  IDO="+this.IDO;
		if (this.PROPERTY!=null)
			result=result+"  PROPERTY="+this.PROPERTY;
		if (this.VALUE!=null)
			result=result+"  VALUE="+this.VALUE;
		if (this.VALUECLS!=null)
			result=result+"  VALUECLS="+this.VALUECLS;
		if (this.OP!=null)
			result=result+"  OP="+this.OP;
		if (this.QMIN!=null)
			result=result+"  QMIN="+this.QMIN;
		if (this.QMAX!=null)
			result=result+"  QMAX="+this.QMAX;
		if (this.NAME!=null)
			result=result+"  NAME="+this.NAME;
		result=result+" VIRTUAL="+this.VIRTUAL;
		
		return result+"  >";
	}

	/*
	public String getVALUENUM() {
		return VALUENUM;
	}

	public void setVALUENUM(String valuenum) {
		VALUENUM = valuenum;
	}
	*/
	public Instance clone(){
		Instance i= new Instance();
		i.setIDO(this.IDO);
		i.setIDTO(this.IDTO);
		i.setNAME(this.NAME);
		i.setOP(this.OP);
		i.setPROPERTY(this.PROPERTY);
		i.setQMAX(this.QMAX);
		i.setQMIN(this.QMIN);
		i.setVALUE(this.VALUE);
		i.setVALUECLS(this.VALUECLS);
		i.setVIRTUAL(this.VIRTUAL);
		
		return i;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (IDO == null) {
			if (other.IDO != null)
				return false;
		} else if (!IDO.equals(other.IDO))
			return false;
		if (IDTO == null) {
			if (other.IDTO != null)
				return false;
		} else if (!IDTO.equals(other.IDTO))
			return false;
		if (NAME == null) {
			if (other.NAME != null)
				return false;
		} else if (!NAME.equals(other.NAME))
			return false;
		if (OP == null) {
			if (other.OP != null)
				return false;
		} else if (!OP.equals(other.OP))
			return false;
		if (PROPERTY == null) {
			if (other.PROPERTY != null)
				return false;
		} else if (!PROPERTY.equals(other.PROPERTY))
			return false;
		if (QMAX == null) {
			if (other.QMAX != null)
				return false;
		} else if (!QMAX.equals(other.QMAX))
			return false;
		if (QMIN == null) {
			if (other.QMIN != null)
				return false;
		} else if (!QMIN.equals(other.QMIN))
			return false;
		if (VALUE == null) {
			if (other.VALUE != null)
				return false;
		} else if (!VALUE.equals(other.VALUE))
			return false;
		if (VALUECLS == null) {
			if (other.VALUECLS != null)
				return false;
		} else if (!VALUECLS.equals(other.VALUECLS))
			return false;
		if (VIRTUAL != other.VIRTUAL)
			return false;
		return true;
	}


	public boolean isVIRTUAL() {
		return VIRTUAL;
	}


	public void setVIRTUAL(boolean vIRTUAL) {
		VIRTUAL = vIRTUAL;
	}
	
	
}
