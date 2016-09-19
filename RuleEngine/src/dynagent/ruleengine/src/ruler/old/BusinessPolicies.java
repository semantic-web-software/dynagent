/***
 * BusinessPolicies.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.src.ruler.old;

import java.util.LinkedList;

public class BusinessPolicies {
	
	private LinkedList<Object> businessRules = new LinkedList<Object>();

	public LinkedList<Object> getBusinessRules() {
		return businessRules;
	}

	public void setBusinessRules(LinkedList<Object> businessRules) {
		this.businessRules = businessRules;
	}
	
	public void addBusinessRule(BusinessRule b){
		this.businessRules.add(b);
	}
}
