package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.ArrayList;

import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.utils.IndividualValues;

public class QuestionRequest extends DataRules{

	private int ID;
	private String RDN;
	private ArrayList<IndividualValues> INDIVIDUALSDATA;
	
	public QuestionRequest(String rdn,ArrayList<IndividualValues> INDIVIDUALSDATA,IKnowledgeBaseInfo ik){
		super(ik);
		this.ID=hashCode();
		this.RDN=rdn;
		this.INDIVIDUALSDATA=INDIVIDUALSDATA;
	}
	
	public String getRDN() {
		return RDN;
	}
	
	public void setRDN(String rdn) {
		RDN = rdn;
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		return new QuestionRequest(RDN,INDIVIDUALSDATA,ik);
	}

	public ArrayList<IndividualValues> getINDIVIDUALSDATA() {
		return INDIVIDUALSDATA;
	}

	public void setINDIVIDUALSDATA(
			ArrayList<IndividualValues> individualsdata) {
		INDIVIDUALSDATA = individualsdata;
	}

	public int getID() {
		return ID;
	}

	public void setID(int id) {
		ID = id;
	}
}
