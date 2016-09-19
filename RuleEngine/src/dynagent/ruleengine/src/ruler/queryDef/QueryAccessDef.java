package dynagent.ruleengine.src.ruler.queryDef;

public class QueryAccessDef {
	
	private Integer IDTO = null; // Object Class

	private Integer IDO = null; // Object Identifier
	private Integer PROP = null; // Property identifier
	private String VALUE = null;
	private Integer VALUECLS = null;

	private Integer TASK=null;
	private Integer USERROL=null;
	private String USER=null;
	private Integer ACCESSTYPE=null;
	private Integer DENNIED=null;
	
	private int searchByTask=-1;
	private int searchByIdto=-1;
	private int searchByIdo=-1;
	private int searchByProp=-1;
	private int searchByAccessType=-1;
	private int searchByValue=-1;
	private int searchByValueCls=-1;
	private int searchByUserrol=-1;
	private int searchByUser=-1;


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
	public Integer getPROP() {
		return PROP;
	}
	public void setPROP(Integer prop) {
		PROP = prop;
	}
	
	public Integer getTASK() {
		return TASK;
	}
	public void setTASK(Integer task) {
		TASK = task;
	}
	public String getUSER() {
		return USER;
	}
	public void setUSER(String user) {
		USER = user;
	}
	public Integer getUSERROL() {
		return USERROL;
	}
	public void setUSERROL(Integer userrol) {
		USERROL = userrol;
	}
	public String getVALUE() {
		return VALUE;
	}
	public void setVALUE(String value) {
		VALUE = value;
	}
	public Integer getVALUECLS() {
		return VALUECLS;
	}
	public void setVALUECLS(Integer valuecls) {
		VALUECLS = valuecls;
	}
		
	public Integer getACCESSTYPE() {
		return ACCESSTYPE;
	}
	public void setACCESSTYPE(Integer accesstype) {
		ACCESSTYPE = accesstype;
	}
	public Integer getDENNIED() {
		return DENNIED;
	}
	public void setDENNIED(Integer dennied) {
		DENNIED = dennied;
	}
	
	
	public int getSearchByAccessType() {
		return searchByAccessType;
	}
	public void setSearchByAccessType(int searchByAccessType) {
		this.searchByAccessType = searchByAccessType;
	}
	public int getSearchByIdo() {
		return searchByIdo;
	}
	public void setSearchByIdo(int searchByIdo) {
		this.searchByIdo = searchByIdo;
	}
	public int getSearchByIdto() {
		return searchByIdto;
	}
	public void setSearchByIdto(int searchByIdto) {
		this.searchByIdto = searchByIdto;
	}
	public int getSearchByProp() {
		return searchByProp;
	}
	public void setSearchByProp(int searchByProp) {
		this.searchByProp = searchByProp;
	}
	public int getSearchByTask() {
		return searchByTask;
	}
	public void setSearchByTask(int searchByTask) {
		this.searchByTask = searchByTask;
	}
	public int getSearchByUserrol() {
		return searchByUserrol;
	}
	public void setSearchByUserrol(int searchByUserrol) {
		this.searchByUserrol = searchByUserrol;
	}
	public int getSearchByValue() {
		return searchByValue;
	}
	public void setSearchByValue(int searchByValue) {
		this.searchByValue = searchByValue;
	}
	public int getSearchByValueCls() {
		return searchByValueCls;
	}
	public void setSearchByValueCls(int searchByValueCls) {
		this.searchByValueCls = searchByValueCls;
	}
		
	public int getSearchByUser() {
		return searchByUser;
	}
	public void setSearchByUser(int searchByUser) {
		this.searchByUser = searchByUser;
	}
	public String toString() {
		String stringfact = "\n\t (QueryAccessDef  " +"(DENNIED "+this.getDENNIED() +") (TASK " + this.getTASK() + ")"
				+ "(USERROL " + this.getUSERROL() + ")" + "(USER "
				+ this.getUSER() + ")" + "(ACCESSTYPE " +this.getACCESSTYPE()
				+ ")" + "(IDTO " + this.getIDTO() + ")" + "(IDO " + this.getIDO() + ")"
				+ "(PROP " + this.getPROP() + ")" + "(VALUE "
				+ this.getVALUE() + ")" + "(VALUECLS " + this.getVALUECLS()+
				"\nSearchByAccessType="+this.getSearchByAccessType()+"  SearchByIdo="+this.getSearchByIdo()+"  searchbyidto="+this.getSearchByIdto()+"   searchbyProp="+this.getSearchByProp()+"  searchByTask="+this.getSearchByTask()+"\n searchbyuser="+  this.getSearchByUser()+"" +
						"  searchbyuserRol="+ this.getSearchByUserrol()+"  searchbyvalue="+this.getSearchByValue()+"  searchbyvaluecls="+this.getSearchByValueCls()+ "))";
		return stringfact;
	}
	
	
}
