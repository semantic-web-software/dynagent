package dynagent.ruleengine.src.ruler;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.KnowledgeAdapter;

public class FilterValue extends Fact {
	
	private String PROPNAME;


	public String getPROPNAME() {
		return PROPNAME;
	}

	public void setPROPNAME(String propname) {
		PROPNAME = propname;
	}

	public FilterValue(int ido,String clase,int idto,String propiedad, int idprop,String value,String rangename,int valuecls,Double qmin,Double qmax,IKnowledgeBaseInfo ik){
		this(ido, clase, idto, propiedad, idprop, value, rangename, valuecls, qmin, qmax, null, ik);
	}
	
	public FilterValue(int ido,String clase,int idto,String propiedad, int idprop,String value,String rangename,int valuecls,Double qmin,Double qmax,String op,IKnowledgeBaseInfo ik){
		//public Fact(Integer idto, Integer ido,  Integer prop,String value, Integer valueCls,  String rangename, Double qmin, Double qmax, String op, String classname,boolean existeBD,String systemValue,boolean appliedSystemValue,String destinationSystem,IKnowledgeBaseInfo ik)		
		 super(idto,ido,idprop,value,valuecls,rangename,qmin,qmax,op,clase,false,null,false,null,ik);
		 this.PROPNAME=propiedad;
		//TODO PASAR A QMIN,QMAX,.......
	}
		

	public Object clone(IKnowledgeBaseInfo ik) {
		return new FilterValue(this.getIDO(),this.getCLASSNAME(),this.getIDTO(),this.getPROPNAME(),this.getPROP(),this.getVALUE(),this.getRANGENAME(),this.getVALUECLS(),this.getQMIN(),this.getQMAX(),ik);
	}
	
	public String getVALOR(){
		return KnowledgeAdapter.getVALUE_s(this);
	}
	
	
	public String toString()
	{
		String stringfact="(FilterValue";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(IDO="+this.getIDO()+")";
		stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		stringfact += "(QMIN="+this.getQMIN()+")";
		stringfact += "(QMAX="+ this.getQMAX()+")";		
		stringfact += "(VALUE="+this.getVALUE()+")";
		stringfact += "(VALOR="+this.getVALOR()+")";		
		stringfact += "(OP="+this.getOP()+")";		
		return  stringfact;
	}
	
	
}
