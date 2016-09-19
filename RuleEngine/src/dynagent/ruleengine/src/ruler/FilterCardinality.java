package dynagent.ruleengine.src.ruler;


import dynagent.common.Constants;
import dynagent.common.exceptions.NotFoundException;
import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class FilterCardinality extends Fact {
	
	private String PROPNAME;

	public String getPROPNAME() {
		return PROPNAME;
	}

	public void setPROPNAME(String propname) {
		PROPNAME = propname;
	}

	public FilterCardinality(int ido,String clase,int idto,String propiedad, int idprop,Double  qmin, Double qmax,IKnowledgeBaseInfo ik){
		//public Fact(Integer idto, Integer ido,  Integer prop,String value, Integer valueCls,  String rangename, Double qmin, Double qmax, String op, String classname,boolean existeBD,String systemValue,boolean appliedSystemValue,String destinationSystem,IKnowledgeBaseInfo ik)		
		//impte valucls a 0 por problemas sesiones
		super(idto, ido, idprop, null, 0,null, qmin, qmax, Constants.OP_CARDINALITY,clase, false, null, false, null,ik);
		this.PROPNAME=propiedad;
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		return new FilterCardinality(this.getIDO(),this.getCLASSNAME(),this.getIDTO(),this.getPROPNAME(),this.getPROP(),this.getQMIN(),this.getQMAX(),ik);
	}
	
	
	public String toString()
	{
		String stringfact="(FilterCardinality";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(IDO="+this.getIDO()+")";
		stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		stringfact += "(QMIN="+this.getQMIN()+")";
		stringfact += "(QMAX="+ this.getQMAX()+")";
		
		
		return  stringfact;
	}
	
	
}
