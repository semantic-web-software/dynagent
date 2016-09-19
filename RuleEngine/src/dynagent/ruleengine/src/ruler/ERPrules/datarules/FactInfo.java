package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class FactInfo extends DataRules {

	private Integer ido;
	private Integer idProp;

	public FactInfo(Integer ido, Integer idProp, IKnowledgeBaseInfo ik) {
		super(ik);
		this.ido=ido;
		this.idProp=idProp;
	}

	
	public Integer getIDO() {
		return ido;
	}


	public void setIDO(Integer ido) {
		this.ido = ido;
	}


	public Integer getPROP() {
		return idProp;
	}


	public void setPROP(Integer idProp) {
		this.idProp = idProp;
	}


	@Override
	public Object clone(IKnowledgeBaseInfo ik) {
		return new FactInfo(ido,idProp,ik);
	}

}
