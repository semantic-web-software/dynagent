package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.values.Value;

public class QueryValue extends DataRules{

	Object value;
	String queryKey;
	
	public QueryValue(String queryKey,Object v,IKnowledgeBaseInfo ik){
		super(ik);
		this.value=v;
		this.queryKey=queryKey;
	}
	
	public Object clone(IKnowledgeBaseInfo ik) {
		return new QueryValue(queryKey,value,ik);
	}

	public Object getVALUE() {
		return value;
	}

	public void setVALUE(Object v) {
		this.value = v;
	}
	public String getQUERYKEY() {
		return queryKey;
	}

	public void setQUERYKEY(String k) {
		this.queryKey = k;
	}
}
