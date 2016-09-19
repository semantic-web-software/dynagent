/***
 * BusinessRule.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.src.ruler.old;

import java.util.Iterator;

public class BusinessRule {

	private Condition precondition;
	private Condition postcondition;
	private boolean fired;
	private String name;	
	
	public boolean isFired() {
		return fired;
	}
	public void setFired(boolean fired) {
		this.fired = fired;
	}
	public Condition getPostcondition() {
		return postcondition;
	}
	public void setPostcondition(Condition postcondition) {
		this.postcondition = postcondition;
	}
	public Condition getPrecondition() {
		return precondition;
	}
	public void setPrecondition(Condition precondition) {
		this.precondition = precondition;
		this.createName(precondition.getConditionText());
	}
	public void createName(String cond){
		this.name = (cond.substring(0, 10).toUpperCase()).replaceAll(" ","");
	}
	
	public String toRuleString(){
		
		String stringrule = "";
		
		stringrule += "\n\t( defrule "+this.name+" \"Business Rule\" ";
		Iterator itp = precondition.getClasses().iterator();
		while(itp.hasNext()){
			// TODO completar
		}
		
		return stringrule;
	}
	
	
	
}
