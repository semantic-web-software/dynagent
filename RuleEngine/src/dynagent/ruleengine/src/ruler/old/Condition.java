/***
 * Condition.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */
package dynagent.ruleengine.src.ruler.old;

import java.util.LinkedList;

public class Condition {
	
	private String conditionText;
	private LinkedList classes = new LinkedList();
	
	public LinkedList getClasses() {
		return classes;
	}
	public void setClasses(LinkedList classes) {
		this.classes = classes;
	}
	public String getConditionText() {
		return conditionText;
	}
	public void setConditionText(String conditionText) {
		this.conditionText = conditionText;
	}
	
	public void evaluate(){
		
	}
	
}
