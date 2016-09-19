package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class Preload extends DataRules{

	int ido;
	int idto;
	
	public Preload(int ido,int idto,IKnowledgeBaseInfo ik){
		super(ik);
		this.ido=ido;
		this.idto=idto;
	}
	
	public Object clone(IKnowledgeBaseInfo ik) {
		return new Preload(ido,idto,ik);
	}

	public int getIDO() {
		return ido;
	}

	public void setIDO(int ido) {
		this.ido = ido;
	}

	public int getIDTO() {
		return idto;
	}

	public void setIDTO(int idto) {
		this.idto = idto;
	}

}
