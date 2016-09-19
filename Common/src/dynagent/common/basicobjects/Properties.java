/***
 * Properties.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.common.basicobjects;


public class Properties {
	
	private Integer PROP;
	private String NAME;
	private Integer CAT;
	private Integer VALUECLS;
	private Integer PROPINV;
	private Integer tableId;
	
	//Constructores
	public Properties(Integer prop,String name ){
		this.PROP=prop;
		this.NAME=name;
	}
	
	public Properties(Integer prop,String name, Integer cat){
		this.PROP=prop;
		this.NAME=name;
		this.CAT=cat;
	}
	public Properties( ){}

	public Integer getCAT() {
		return CAT;
	}
	public void setCAT(Integer cat) {
		CAT = cat;
	}
	public String getNAME() {
		return NAME;
	}
	public void setNAME(String name) {
		NAME = name;
	}
	public Integer getPROP() {
		return PROP;
	}
	public void setPROP(Integer prop) {
		PROP = prop;
	}

	public Integer getPROPINV() {
		return PROPINV;
	}

	public void setPROPINV(Integer propinv) {
		PROPINV = propinv;
	}

	public Integer getVALUECLS() {
		return VALUECLS;
	}

	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}
	
	public String toString(){
		String stringfact = "";
		
		stringfact += "\n\t (PROPERTIES ";
		stringfact += "( NAME " + this.getNAME() + " )";
			if(this.getPROP() == null)
			stringfact += "( PROP nil )";
		else
			stringfact += "( PROP " + this.getPROP() + " )";
		
		if(this.getCAT() == null)
			stringfact += "( CAT nil ))";
		else
			stringfact += "( CAT " + this.getCAT() + " ))";
		
		if(this.getVALUECLS() == null)
			stringfact += "( VALUECLS nil )";
		else{
			if(this.getVALUECLS()==null)
				stringfact += "( VALUECLS nil )";
			else
				stringfact += "( VALUECLS " + this.getVALUECLS() + " )";
		}
		
		if(this.getPROPINV() == null)
			stringfact += "(PROPIN nil ))";
		else
			stringfact += "(PROPINV " + this.getPROPINV() + " ))";
		
		return stringfact;
		
	}

	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}
}
