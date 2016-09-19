package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Date;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class TimeUserChange extends DataRules {

	private Integer ido;
	private String propname;
	private String clase;
	private Date timeChange;
	

	public TimeUserChange(Integer ido, String clase,String propiedad, IKnowledgeBaseInfo ik){
		super(ik);
		this.ido=ido;
		this.propname=propiedad;
		this.clase=clase;
		timeChange=new Date(System.currentTimeMillis());
	}


	@Override
	public Object clone(IKnowledgeBaseInfo ik) {
		return new TimeUserChange(this.getIDO(),this.getCLASSNAME(),this.getCLASSNAME(),ik);
	}


	public Integer getIDO() {
		return ido;
	}
	
	
	public String getPROPNAME() {
		return propname;
	}


	


	public String getCLASSNAME() {
		return clase;
	}


	

	public Date getTIMECHANGE() {
		return timeChange;
	}


	
}
