/***
 * TripleInstance.java
 */

package dynagent.ruleengine.meta.api.old;

public class TripleInstance{
	private int idto;
	private int valuecls;
	private int property;
	
	public int getIdto() {
		return idto;
	}
	public void setIdto(int idto) {
		this.idto = idto;
	}
	public int getProperty() {
		return property;
	}
	public void setProperty(int property) {
		this.property = property;
	}
	public int getValuecls() {
		return valuecls;
	}
	public void setValuecls(int valuecls) {
		this.valuecls = valuecls;
	}
}