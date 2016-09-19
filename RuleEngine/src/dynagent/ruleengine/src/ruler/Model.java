package dynagent.ruleengine.src.ruler;

import dynagent.common.Constants;
import dynagent.common.basicobjects.IModel;
import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.knowledge.KnowledgeAdapter;

public class Model extends Fact implements IModel {	//TODO MODELFACT NO EXTIENDA FACT (ES ESTATICO Y NO NECESITA GASTAR TANTA MEMORIA COMO FACT, SUS UNICOS CAMPOS NECESARIOS SON LOS QUE SE USAN EN EL SIGUIENTE CONSTRUCTOR
	
	
	private String PROPNAME;

	
	public Model(String classname,int idto,String propiedad,int idprop, String value,String rangename, Integer valueCls,Double qmin, Double qmax, String op,IKnowledgeBaseInfo ik) {
		super(idto,null,idprop,value,valueCls,rangename,qmin,qmax,op,classname,false,null, false, null, ik);
		//super(idto,ido,prop,value,valueCls,rangename,qmin,qmax,op,classname,existeBD,systemValue, appliedSystemValue, destinationSystem, ik);
		this.setPROPNAME(propiedad);
		this.setRANGENAME(rangename);
	}
	

	public String getPROPNAME() {
		return PROPNAME;
	}

	public void setPROPNAME(String propname) {
		PROPNAME = propname;
	}
	

	public Object clone(IKnowledgeBaseInfo ik) {
		return new Model(this.getCLASSNAME(),this.getIDTO(),this.getPROPNAME(),this.getPROP(),this.getVALUE(),this.getRANGENAME(),this.getVALUECLS(),this.getQMIN(),this.getQMAX(),this.getOP(),ik);
	}
	
	public String getDEFAULTVALUE(){
		String result=null;
		KnowledgeAdapter a;
		
		if(this.getOP().equals(Constants.OP_DEFAULTVALUE)){
			result=KnowledgeAdapter.getVALUE_s(this);
		}
		
		return result;
	}
	
	
	public String getONEOFVALUE(){
		String result=null;
		KnowledgeAdapter a;
		
		if(this.getOP().equals(Constants.OP_ONEOF)){
			result=KnowledgeAdapter.getVALUE_s(this);
		}
		return result;
	}
	
	public String toString()
	{
		String stringfact="(Model";
		stringfact += "(CLASSNAME=" + this.getCLASSNAME()+")";
		stringfact += "(OP="+ this.getOP()+")";		
		stringfact += "(PROPNAME="+this.getPROPNAME() + ")";
		stringfact += "(RANGENAME=" + this.getRANGENAME()+")";
		stringfact += "(VALUE="+this.getVALUE()+")";
		stringfact += "(QMIN="+this.getQMIN()+")";
				
		
		return  stringfact;
	}
 
}

