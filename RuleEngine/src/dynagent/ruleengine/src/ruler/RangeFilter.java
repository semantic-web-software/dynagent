package dynagent.ruleengine.src.ruler;


/***
 * Clase que representa el enlace con un filtro: independientemente de quien es su dominio (este puede ser un individuo/prototipo/filtro)
 */
import dynagent.common.Constants;
import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class RangeFilter extends Fact {
	
	private String PROPNAME;
	
	public String getPROPNAME() {
		return PROPNAME;
	}


	public void setPROPNAME(String propname) {
		PROPNAME = propname;
	}


	public RangeFilter(String classname,int idto,int ido, String propiedad,int idprop,String sidofiltervalue, String rangename,int valueCls,IKnowledgeBaseInfo ik) {
		super(idto, ido, idprop, sidofiltervalue, valueCls, rangename,null, null,Constants.OP_UNION, classname, false, null, false,  null,ik);
		//Fact(Integer idto, Integer ido,  Integer prop,String value, Integer valueCls,  String rangename, Double qmin, Double qmax, String op, String classname,boolean existeBD,String systemValue,boolean appliedSystemValue,String destinationSystem,IKnowledgeBaseInfo ik)
		this.PROPNAME=propiedad;
	}

	public Object clone(IKnowledgeBaseInfo ik) {
		return new RangeFilter(this.getCLASSNAME(),this.getIDTO(),this.getIDO(),this.getPROPNAME(),this.getPROP(),this.getVALUE(),this.getRANGENAME(),this.getVALUECLS(),ik);
	}
	
	public String toString()
	{
		String stringfact="(RangeFilter";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(IDO="+this.getIDO()+")";
		stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
		stringfact += "(VALUE="+this.getVALUE()+")";
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		return  stringfact;
	}
	
	public Integer getIDOVALUE() {
		if(super.getVALUE()!=null)
			return Integer.parseInt(super.getVALUE());
		else
			return null;
	}
	
}
