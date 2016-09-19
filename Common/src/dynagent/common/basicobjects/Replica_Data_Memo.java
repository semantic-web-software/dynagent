package dynagent.common.basicobjects;


public class Replica_Data_Memo {
	
	private Integer id;
	private String action;
	private Integer IDTO;
	private Integer IDO;
	private Integer PROPERTY;
	private String MEMO;
	private Integer VALUECLS;
	private String SYS_VAL;
	private String DESTINATION;
	private Long date;

	public Replica_Data_Memo() {}

	public Replica_Data_Memo(Integer id, String action, Integer IDTO, Integer IDO, Integer PROPERTY, String MEMO, Integer VALUECLS, String SYS_VAL, String DESTINATION, Long date) {
		this.id = id;
		this.action = action;
		this.IDTO = IDTO;
		this.IDO = IDO;
		this.PROPERTY = PROPERTY;
		this.MEMO = MEMO;
		this.VALUECLS = VALUECLS;
		this.SYS_VAL = SYS_VAL;
		this.DESTINATION = DESTINATION;
		this.date = date;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getPROPERTY() {
		return PROPERTY;
	}
	public void setPROPERTY(Integer property) {
		PROPERTY = property;
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

	public Replica_Data_Memo clone() {
		return this.clone();
	}
}
