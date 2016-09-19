package dynagent.ruleengine.src.ruler;
import dynagent.common.Constants;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.KnowledgeAdapter;

public class FilterNotValue extends Fact {
	
	private String PROPNAME;


	public String getPROPNAME() {
		return PROPNAME;
	}

	public void setPROPNAME(String propname) {
		PROPNAME = propname;
	}

	public FilterNotValue(int ido,String clase,int idto,String propiedad, int idprop,String value,String rangename,int valuecls,double qmin,double qmax,IKnowledgeBaseInfo ik){
		//public Fact(Integer idto, Integer ido,  Integer prop,String value, Integer valueCls,  String rangename, Double qmin, Double qmax, String op, String classname,boolean existeBD,String systemValue,boolean appliedSystemValue,String destinationSystem,IKnowledgeBaseInfo ik)		
		 super(idto,ido,idprop,value,valuecls,rangename,qmin,qmax,Constants.OP_NEGATION,clase,false,null,false,null,ik);
		 this.PROPNAME=propiedad;
	}
		

	public Object clone(IKnowledgeBaseInfo ik) {
		return new FilterNotValue(this.getIDO(),this.getCLASSNAME(),this.getIDTO(),this.getPROPNAME(),this.getPROP(),this.getVALUE(),this.getRANGENAME(),this.getVALUECLS(),this.getQMIN(),this.getQMAX(),ik);
	}
	
	public String getNOTVALUE(){
		return KnowledgeAdapter.getVALUE_s(this);
			
	}
	
	public String toString()
	{
		String stringfact="(FilterNotValue";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(IDO="+this.getIDO()+")";
		stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		stringfact += "(NOTVALUE="+this.getNOTVALUE()+")";
		return  stringfact;
	}
	
}






