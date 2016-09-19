package dynagent.ruleengine.src.ruler;

import dynagent.ruleengine.src.data.dao.Access;
import dynagent.ruleengine.src.data.dao.Instance;
import dynagent.ruleengine.src.data.dao.Properties;
import dynagent.ruleengine.src.data.dao.T_Herencias;

public class FactFactory {
	
	private static boolean byNull=true;
	private static FactFactory instance = null;
	
	private FactFactory(){
		
	}

	public boolean isByNull() {
		return byNull;
	}

	public void setByNull(boolean byNull) {
		this.byNull = byNull;
	}
	
	
	public static FactFactory getInstance(){
		if(instance==null)
			instance = new FactFactory();
		return instance;	
	}
	
	public Fact toFact(Instance in){
		
		Integer IDO = null,IDTO = null,PROP=null,VALUECLS=null;
		Double QMIN=null,QMAX=null;
		if(in.getIDO() != null)
			IDO=new Integer(in.getIDO()).intValue();
		else if(!byNull)
			IDO = new Integer(0).intValue();
		
		if(in.getIDTO() != null)
			IDTO=Integer.parseInt(in.getIDTO());
		else if(!byNull)
			IDTO = new Integer(0).intValue();
		if(in.getPROPERTY() != null)
			PROP=new Integer(in.getPROPERTY()).intValue();
		else if(!byNull)
			PROP = new Integer(0).intValue();
		
		if(in.getQMAX() != null)
			QMAX=new Double(in.getQMAX()).doubleValue();
		
		
		if(in.getQMIN() != null)
			QMIN=new Double(in.getQMIN()).doubleValue();
	
		
		if(in.getVALUECLS() != null)
			VALUECLS=new Integer(in.getVALUECLS()).intValue();
		else if(!byNull)
			VALUECLS = new Integer(0).intValue();
		
		
	
		return new Fact(IDTO,IDO,PROP,in.getVALUE(),VALUECLS,QMIN,QMAX,in.getOP(),in.getNAME());
		
	}
	
public Fact toFact(IPropertyDef in){
		
		Integer IDO = null;
		int PROP,IDTO,VALUECLS;
		Double QMIN=null,QMAX=null;
		if(in.getIDO() != null)
			IDO=new Integer(in.getIDO()).intValue();
		else if(!byNull)
			IDO = new Integer(0).intValue();
		
		if(in.getIDTO() != null)
			IDTO=in.getIDTO().intValue();
		else if(!byNull)
			IDTO = new Integer(0).intValue();
		
			PROP=new Integer(in.getPROP()).intValue();
		else if(!byNull)
			PROP = new Integer(0).intValue();
		
		if(in.getQMAX() != null)
			QMAX=new Double(in.getQMAX()).doubleValue();
		
		
		if(in.getQMIN() != null)
			QMIN=new Double(in.getQMIN()).doubleValue();
		
		
		if(in.getVALUECLS() != null)
			VALUECLS=new Integer(in.getVALUECLS()).intValue();
		else if(!byNull)
			VALUECLS = new Integer(0).intValue();
		
		return new Fact(IDTO,IDO,PROP,in.getVALUE(),VALUECLS,QMIN,QMAX,in.getOP(),in.getCLASSNAME());
		
	}
	
	
	public FactProp toFactProp(Properties p){

		FactProp f = new FactProp();
		
		f.setPROP(p.getPROP().intValue());
		if(f.getPROP()==null && !byNull)
			f.setPROP(0);
		f.setNAME(p.getNAME());
		f.setVALUE(p.getVALUE());
		f.setVALUECLS(p.getVALUECLS());
		if(f.getVALUECLS()==null && !byNull)
			f.setVALUECLS(0);
		f.setCAT(p.getCAT());
		if(f.getCAT()==null && !byNull)
			f.setCAT(0);
		f.setOP(p.getOP());
		f.setMASK(p.getMASK());
		f.setLENGTH(p.getLENGTH());
		f.setQMAX(p.getQMAX());
		if(f.getQMAX() != null && !byNull)
			f.setQMAX(new Float(0));
		f.setQMIN(p.getQMIN());
		if(f.getQMAX() != null && !byNull)
			f.setQMIN(new Float(0));
		f.setPROPIN(p.getPROPINV());

		return f;
	}
	
	public FactHierarchy toFactHierarchy(T_Herencias h){
		FactHierarchy f = new FactHierarchy();
		f.setIDTO(((Integer) h.getID_TO()).intValue());
		if(f.getIDTO()==null && !byNull)
			f.setIDTO(0);
		f.setIDTOSUP(((Integer) h.getID_TO_Padre()).intValue());
		if(f.getIDTOSUP()== null && !byNull)
			f.setIDTOSUP(0);
		return f;
	}
	
	public dynagent.ruleengine.src.ruler.FactAccess toFactAccess(Access a) {
		dynagent.ruleengine.src.ruler.FactAccess f = new dynagent.ruleengine.src.ruler.FactAccess();
		
		f.setACCESSTYPE(a.getACCESSTYPE());
		if(f.getACCESSTYPE() == null && !byNull)
			f.setACCESSTYPE(0);
		
		f.setTASK(a.getTASK());
		if(f.getTASK() == null && !byNull)
			f.setTASK(0);
		f.setUSERROL(a.getUSERROL());
		if(f.getUSERROL() == null && !byNull)
			f.setUSERROL(0);
		f.setUSER(a.getUSER());
		f.setVALUECLS(a.getVALUECLS());
		if(f.getVALUECLS() == null && !byNull)
			f.setVALUECLS(0);
	
		f.setPROP(a.getPROP());
		if(f.getPROP() == null && !byNull)
			f.setPROP(0);
		f.setIDTO(a.getIDTO());
		if(f.getIDTO() == null && !byNull)
			f.setIDTO(0);
		
		f.setIDO(a.getIDO());
		if(f.getIDO() == null && !byNull)
			f.setIDO(0);
		f.setVALUE(a.getVALUE());
	
		f.setDENNIED(a.getDENNIED());
	
		return f;
	}
	
	public FactInstance revertFact (Fact f){
		Integer ido, idto, valuecls, prop;
		Double qmax, qmin;
		
		if((f.getIDO()==0 && !byNull) || (f.getIDO() == null && byNull))
			ido = null;
		else
			ido = f.getIDO().intValue();
		
		if((f.getIDTO()==0 && !byNull) || (f.getIDTO() == null && byNull))
			idto = null;
		else
			idto = f.getIDTO().intValue();
		
		if((f.getPROP()==0 && !byNull) 
			prop = null;
		else
			prop = f.getPROP();
		
		if((f.getVALUECLS()==0 && !byNull) || (f.getVALUECLS() == null && byNull))
			valuecls= null;
		else
			valuecls = f.getVALUECLS().intValue();
		
		if( f.getQMAX() == null)
			qmax = null;
		else
			qmax = f.getQMAX().doubleValue();
		
		if(f.getQMIN() == null)
			qmin = null;
		else
			qmin = f.getQMIN().doubleValue();
		
		return new FactInstance(idto,ido,prop,f.getVALUE(),valuecls,qmin,qmax,f.getOP(),f.getCLASSNAME());
	}
	
	
	public FactAccess revertFactAccess (FactAccess f){
		Integer ido, idto, valuecls, prop, accessType, task, userRol;
		
		
		if(f.getIDO()==0)
			ido = null;
		else
			ido = f.getIDO().intValue();
		
		if(f.getIDTO()==0)
			idto = null;
		else
			idto = f.getIDTO().intValue();
		
		if(f.getPROP()==0)
			prop = null;
		else
			prop = f.getPROP().intValue();
		
		if(f.getVALUECLS()==0)
			valuecls= null;
		else
			valuecls = f.getVALUECLS().intValue();
		
		if(f.getACCESSTYPE() == 0)
			accessType = null;
		else
			accessType = f.getACCESSTYPE().intValue();
		
		if(f.getTASK() == 0)
			task = null;
		else
			task = f.getTASK().intValue();
		
		if(f.getUSERROL() == 0)
			userRol = null;
		else
			userRol = f.getUSERROL().intValue();
		
		FactAccess fa = new FactAccess();
		fa.setACCESSTYPE(accessType);
		fa.setDENNIED(f.getDENNIED());
		fa.setIDO(ido);
		fa.setIDTO(idto);
		fa.setPROP(prop);
		fa.setTASK(task);
		fa.setUSER(f.getUSER());
		fa.setUSERROL(userRol);
		fa.setVALUE(f.getVALUE());
		fa.setUSER(f.getUSER());
		fa.setVALUECLS(valuecls);
		return fa;
		
		
	}	

}