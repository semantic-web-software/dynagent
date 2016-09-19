package dynagent.common.basicobjects;

import dynagent.common.knowledge.FactInstance;

public class O_Datos_Attrib_Memo {
	private Integer IDTO;
	private Integer IDO;
	private Integer PROPERTY;
	private String MEMO;
	private Integer VALUECLS;
	private String SYS_VAL;
	private String DESTINATION;

	public O_Datos_Attrib_Memo() {}

	public O_Datos_Attrib_Memo(Integer IDTO, Integer IDO, Integer PROPERTY, String MEMO, Integer VALUECLS, String SYS_VAL, String DESTINATION) {
		this.IDTO = IDTO;
		this.IDO = IDO;
		this.PROPERTY = PROPERTY;
		this.MEMO = MEMO;
		this.VALUECLS = VALUECLS;
		this.SYS_VAL = SYS_VAL;
		this.DESTINATION = DESTINATION;
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
		if(this.getMEMO()!=null){
			f.setVALUE(this.getMEMO());
		}
		f.setVALUECLS(this.getVALUECLS());
		
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
	public String getMEMO() {
		return MEMO;
	}
	public void setMEMO(String memo) {
		MEMO = memo;
	}
	public Integer getVALUECLS() {
		return VALUECLS;
	}
	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}
	public String getDESTINATION() {
		return DESTINATION;
	}
	public void setDESTINATION(String destination) {
		DESTINATION = destination;
	}
	public String getSYS_VAL() {
		return SYS_VAL;
	}
	public void setSYS_VAL(String sys_val) {
		SYS_VAL = sys_val;
	}

	public O_Datos_Attrib_Memo clone() {
		return this.clone();
	}
}
