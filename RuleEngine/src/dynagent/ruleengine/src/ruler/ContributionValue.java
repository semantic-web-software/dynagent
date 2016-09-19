package dynagent.ruleengine.src.ruler;

import java.util.HashMap;

public class ContributionValue /* extends Object*/{
	
	HashMap<String,Number> INITIALVALUE=null;
	Number CURRENTVALUE=null;
	
	public ContributionValue(HashMap<String,Number> initialValue,Number currentValue){
		this.INITIALVALUE=initialValue;
		this.CURRENTVALUE=currentValue;
	}
	
	public ContributionValue(){
		
	}
	
	public Number getCURRENTVALUE() {
		return CURRENTVALUE;
	}
	public void setCURRENTVALUE(Number currentValue) {
		this.CURRENTVALUE = currentValue;
	}
	public HashMap<String,Number> getINITIALVALUE() {
		return INITIALVALUE;
	}
	public void setINITIALVALUE(HashMap<String,Number> initialValue) {
		this.INITIALVALUE = initialValue;
	}
	
	public void addCantidadToCurrentValue(Number contribucion){
		if(this.getCURRENTVALUE()==null)
			this.setCURRENTVALUE(contribucion);
		else{
			this.setCURRENTVALUE(this.getCURRENTVALUE().doubleValue()+contribucion.doubleValue());
		}
	}

	public void addCantidadToInitialValue(String sido,Number contribucion){
		if(this.getINITIALVALUE()==null && contribucion==null) return;
		
		if(this.getINITIALVALUE()==null){
			HashMap<String,Number> initialvalue=new HashMap<String,Number>();
			initialvalue.put(sido, contribucion);
			this.setINITIALVALUE(initialvalue);
		}
		else{
			if( contribucion==null ) this.getINITIALVALUE().remove(sido);
			else this.getINITIALVALUE().put(sido, contribucion);
		}
	}
	
	public void addContribution(ContributionValue contributionValue){
		if(contributionValue.getINITIALVALUE()!=null){
			if(INITIALVALUE==null)
				INITIALVALUE=new HashMap<String, Number>();
			INITIALVALUE.putAll(contributionValue.getINITIALVALUE());
		}
		if(contributionValue.getCURRENTVALUE()!=null){
			if(CURRENTVALUE==null)
				CURRENTVALUE=0;
			CURRENTVALUE=CURRENTVALUE.doubleValue()+contributionValue.getCURRENTVALUE().doubleValue();
		}
	}
	
	public String toString()
	{
		String stringfact="";
		stringfact += "(INITIVALVALUE= "+this.getINITIALVALUE()+")";
		stringfact += "(CURRENTVALUE=" + this.getCURRENTVALUE()+")";
		return stringfact;
	}
}
