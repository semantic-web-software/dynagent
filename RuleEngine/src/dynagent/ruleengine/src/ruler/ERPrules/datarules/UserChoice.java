package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class UserChoice extends DataRules{

	String NAME;
	String VALUE;
	String PARAM;
	String FLUJO;
	Integer IDO1=0;
	Integer IDO2=0;
	Integer IDTO1=0;
	Integer IDTO2=0;
	
	public UserChoice(String NAME,String VALUE,String PARAM,String FLUJO,IKnowledgeBaseInfo ik){
		super(ik);
		this.NAME=NAME;
		this.VALUE=VALUE;
		this.PARAM=PARAM;
		this.FLUJO=FLUJO;
	}
	
	public Object clone(IKnowledgeBaseInfo ik) {
		return new UserChoice(NAME,VALUE,PARAM,FLUJO,ik);
	}

	public String getNAME() {
		return NAME;
	}

	public void setNAME(String NAME) {
		String old=this.NAME;
		this.NAME = NAME;
		pcs.firePropertyChange("NAME", old, NAME);
	}

	public String getVALUE() {
		return VALUE;
	}

	public void setVALUE(String VALUE) {
		String old=this.VALUE;
		this.VALUE = VALUE;
		pcs.firePropertyChange("VALUE", old, VALUE);
	}

	public String getPARAM() {
		return PARAM;
	}

	public void setPARAM(String PARAM) {
		String old=this.PARAM;
		this.PARAM = PARAM;
		pcs.firePropertyChange("PARAM", old, PARAM);
	}
	
	public String getFLUJO() {
		return FLUJO;
	}

	public void setFLUJO(String FLUJO) {
		String old=this.FLUJO;
		this.FLUJO = FLUJO;
		pcs.firePropertyChange("FLUJO", old, FLUJO);
	}
	
	public void setIDO1(Integer IDO1) {
		this.IDO1=IDO1;
	}
	public Integer getIDO1() {
		return IDO1;
	}
	public void setIDO2(Integer IDO2) {
		this.IDO2=IDO2;
	}
	public Integer getIDO2() {
		return IDO2;
	}
	
	public void setIDTO1(Integer IDTO1) {
		this.IDTO1=IDTO1;
	}
	public Integer getIDTO1() {
		return IDTO1;
	}
	public void setIDTO2(Integer IDTO3) {
		this.IDTO2=IDTO2;
	}
	public Integer getIDTO2() {
		return IDTO2;
	}
}
