package dynagent.common.basicobjects;

import dynagent.common.utils.Auxiliar;


public class Replica_Data {

	private Integer id;
	private String action;
	private int IDTO;
	private int IDO;
	private Integer PROPERTY;
	private Integer VALNUM;
	private Integer OLDVALNUM;
	private String VALTEXTO;
	private Integer VALUECLS;
	private Double QMIN;
	private Double QMAX;
	private String SYS_VAL;
	private String DESTINATION;
	private Long date;
	
	public Replica_Data() {}

	public Replica_Data(Integer id, String action, int IDTO, int IDO, Integer PROPERTY, Integer VALNUM, Integer OLDVALNUM, String VALTEXTO, Integer VALUECLS, 
			Double QMIN, Double QMAX, String SYS_VAL, String DESTINATION, Long date) {
		this.id = id;
		this.action = action;
		this.IDTO = IDTO;
		this.IDO = IDO;
		this.PROPERTY = PROPERTY;
		this.VALNUM = VALNUM;
		this.OLDVALNUM = OLDVALNUM;
		this.VALTEXTO = VALTEXTO;
		this.VALUECLS = VALUECLS;
		this.QMIN = QMIN;
		this.QMAX = QMAX;
		this.SYS_VAL = SYS_VAL;
		this.DESTINATION = DESTINATION;
		this.date = date;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Integer getPROPERTY() {
		return PROPERTY;
	}
	public void setPROPERTY(Integer property) {
		PROPERTY = property;
	}
	public int getIDO() {
		return IDO;
	}
	public void setIDO(int ido) {
		IDO = ido;
	}
	public int getIDTO() {
		return IDTO;
	}
	public void setIDTO(int idto) {
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
	public Integer getOLDVALNUM() {
		return OLDVALNUM;
	}
	public void setOLDVALNUM(Integer OLDvalnum) {
		OLDVALNUM = OLDvalnum;
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
	public Replica_Data clone() {
		Replica_Data repClone = new Replica_Data(this.id, this.action, this.IDTO, this.IDO, this.PROPERTY, this.VALNUM, this.OLDVALNUM, this.VALTEXTO, this.VALUECLS, this.QMIN, this.QMAX, this.SYS_VAL, this.DESTINATION, this.date);
		return repClone;
	}
	public String toString() {
		return "ID: " + id + ", ACTION: " + action + ", IDTO: " + IDTO + ", IDO: " + IDO + ", PROPERTY: " + PROPERTY + ", VALNUM: " + VALNUM + ", VALTEXTO: " + VALTEXTO + ", VALUECLS: " + VALUECLS + ", QMIN: " + QMIN + ", QMAX: " + QMAX+", SYS_VAL: "+SYS_VAL+", DESTINATION: "+DESTINATION+", DATE: "+date;
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
		Replica_Data other=(Replica_Data)o;
		boolean equals=Auxiliar.equals(this.getIDTO(), other.getIDTO()) && Auxiliar.equals(this.getIDO(), other.getIDO()) && Auxiliar.equals(this.getPROPERTY(), other.getPROPERTY())
				&& Auxiliar.equals(this.getVALNUM(), other.getVALNUM()) && Auxiliar.equals(this.getOLDVALNUM(), other.getOLDVALNUM()) && Auxiliar.equals(this.getVALTEXTO(), other.getVALTEXTO()) && Auxiliar.equals(this.getVALUECLS(), other.getVALUECLS())
				&& Auxiliar.equals(this.getQMIN(), other.getQMIN()) && Auxiliar.equals(this.getQMAX(), other.getQMAX()) && Auxiliar.equals(this.getSYS_VAL(), other.getSYS_VAL()) && Auxiliar.equals(this.getDESTINATION(), other.getDESTINATION());
		return equals;
	}
}
