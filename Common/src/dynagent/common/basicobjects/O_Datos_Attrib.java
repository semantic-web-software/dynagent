/***
 * O_Datos_Attrib.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.common.basicobjects;

import dynagent.common.knowledge.FactInstance;
import dynagent.common.utils.Auxiliar;


/**
 * ID_TO		int	
 * ROL			int	
 * ID_O			int	
 * PROPERTY		int	
 * VAL_NUM		real
 * VAL_TEXTO	nvarchar(250)
 * VALUE_CLS	int
 * VALUE_ROL	int
 * CLS_REL		int
 * IDO_REL		int
 * ROL_PEER		int
 * Q_MIN		float
 * Q_MAX		float
 */

public class O_Datos_Attrib {

	private Integer IDTO;
	private Integer IDO;
	private Integer PROPERTY;
	private Integer VALNUM;
	private String VALTEXTO;
	private Integer VALUECLS;
	private Double QMIN;
	private Double QMAX;
	private String SYS_VAL;
	private String DESTINATION;
	
	private boolean encrypt;
	

	public O_Datos_Attrib() {}

	public O_Datos_Attrib(Integer IDTO, Integer IDO, Integer PROPERTY, Integer VALNUM, String VALTEXTO, Integer VALUECLS, 
			Double QMIN, Double QMAX, String SYS_VAL, String DESTINATION) {
		this.IDTO = IDTO;
		this.IDO = IDO;
		this.PROPERTY = PROPERTY;
		this.VALNUM = VALNUM;
		this.VALTEXTO = VALTEXTO;
		this.VALUECLS = VALUECLS;
		this.QMIN = QMIN;
		this.QMAX = QMAX;
		this.SYS_VAL = SYS_VAL;
		this.DESTINATION = DESTINATION;
	}
	
	
	public boolean isEncrypt() {
		return encrypt;
	}

	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	public Integer getPROPERTY() {
		return PROPERTY;
	}
	public void setPROPERTY(Integer property) {
		PROPERTY = property;
	}
	
	public FactInstance toFactInstance(){
		FactInstance f = new FactInstance();	
		
		f.setIDO(this.getIDO());
		f.setIDTO(this.getIDTO());
		f.setPROP(this.getPROPERTY());
		f.setQMAX(this.getQMAX());
		f.setQMIN(this.getQMIN());
		if(this.getVALNUM()!=null){
			Integer valnum=this.getVALNUM().intValue();
			f.setVALUE(valnum.toString());
			if(this.getVALTEXTO()!=null){
				System.out.println("     WARNING: ValueNum="+this.getVALNUM()+"   y ValueText="+this.getVALTEXTO()+ "  en O_Datos_Attrib.toFact.  Solo se tendrá en cuenta valueNum");
			}
		}
		else if(this.getVALTEXTO()!=null){
			f.setVALUE(this.getVALTEXTO());
		}
		f.setVALUECLS(this.getVALUECLS());
		f.setIDO(this.getIDO());
		f.setIDTO(this.getIDTO());
		f.setPROP(this.getPROPERTY());
		f.setQMAX(this.getQMAX());
		f.setQMIN(this.getQMIN());
		f.setSystemValue(this.getSYS_VAL());
		f.setDestinationSystem(this.getDESTINATION());
		
		return f;
	}
	public Integer getIDO() {
		return IDO;
	}
	public void setIDO(Integer ido) {
		IDO = ido;
	}
	public Integer getIDTO() {
		return IDTO;
	}
	public void setIDTO(Integer idto) {
		IDTO = idto;
	}
	public Double getQMAX() {
		return QMAX;
	}
	public void setQMAX(Double qmax) {
		QMAX = qmax;
	}
	public Double getQMIN() {
		return QMIN;
	}
	public void setQMIN(Double qmin) {
		QMIN = qmin;
	}
	public Integer getVALNUM() {
		return VALNUM;
	}
	public void setVALNUM(Integer valnum) {
		VALNUM = valnum;
	}
	public String getVALTEXTO() {
		return VALTEXTO;
	}
	public void setVALTEXTO(String valtexto) {
		VALTEXTO = valtexto;
	}
	public Integer getVALUECLS() {
		return VALUECLS;
	}
	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}
	public O_Datos_Attrib clone() {
		return this.clone();
	}
	public String toString() {
		return "IDTO: " + IDTO + ", IDO: " + IDO + ", PROPERTY: " + PROPERTY + ", VALNUM: " + VALNUM + ", VALTEXTO: " + VALTEXTO + ", VALUECLS: " + VALUECLS + ", QMIN: " + QMIN + ", QMAX: " + QMAX+" SYS_VAL: "+SYS_VAL+" DESTINATION: "+DESTINATION;
	}

	public String getSYS_VAL() {
		return SYS_VAL;
	}

	public void setSYS_VAL(String sys_val) {
		SYS_VAL = sys_val;
	}

	public String getDESTINATION() {
		return DESTINATION;
	}

	public void setDESTINATION(String destination) {
		DESTINATION = destination;
	}
	
	@Override
	public boolean equals(Object o) {
		O_Datos_Attrib other=(O_Datos_Attrib)o;
		boolean equals=Auxiliar.equals(this.getIDTO(), other.getIDTO()) && Auxiliar.equals(this.getIDO(), other.getIDO()) && Auxiliar.equals(this.getPROPERTY(), other.getPROPERTY())
				&& Auxiliar.equals(this.getVALNUM(), other.getVALNUM()) && Auxiliar.equals(this.getVALTEXTO(), other.getVALTEXTO()) && Auxiliar.equals(this.getVALUECLS(), other.getVALUECLS())
				&& Auxiliar.equals(this.getQMIN(), other.getQMIN()) && Auxiliar.equals(this.getQMAX(), other.getQMAX()) && Auxiliar.equals(this.getSYS_VAL(), other.getSYS_VAL()) && Auxiliar.equals(this.getDESTINATION(), other.getDESTINATION());
		return equals;
	}
}
