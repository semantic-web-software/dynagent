package dynagent.common.communication;

import org.jdom.Element;

import dynagent.common.properties.values.Value;

public class ObjectChanged {

	private Integer prop;
	private Integer oldIdo;
	private int newIdo;
	private Value oldValue;
	private Value newValue;
	public String clsname="";

	public int getNewIdo() {
		return newIdo;
	}

	public void setNewIdo(int newIdo) {
		this.newIdo = newIdo;
	}

	public Value getNewValue() {
		return newValue;
	}

	public void setNewValue(Value newValue) {
		this.newValue = newValue;
	}

	public Integer getOldIdo() {
		return oldIdo;
	}

	public void setOldIdo(Integer oldIdo) {
		this.oldIdo = oldIdo;
	}

	public Value getOldValue() {
		return oldValue;
	}

	public void setOldValue(Value oldValue) {
		this.oldValue = oldValue;
	}

	public Integer getProp() {
		return prop;
	}

	public void setProp(Integer prop) {
		this.prop = prop;
	}
	
	public Element toElement() {
		Element child = new Element("OBJECT_CHANGED");
		if (oldIdo!=null)
			child.setAttribute("OLD_IDO",String.valueOf(oldIdo));
		if(clsname!=null&&clsname.length()>0){
			child.setAttribute("CLS",clsname);
		}
		child.setAttribute("NEW_IDO",String.valueOf(newIdo));
		if (prop!=null) {
			child.setAttribute("PROP",String.valueOf(prop));
			if (oldValue!=null) {
				Element elemOldValue = new Element("OLD_VALUE");
				Element childElemOldValue = oldValue.toElement();
				elemOldValue.addContent(childElemOldValue);
				child.addContent(elemOldValue);
			}
			Element elemNewValue = new Element("NEW_VALUE");
			Element childElemNewValue = newValue.toElement();
			elemNewValue.addContent(childElemNewValue);
			child.addContent(elemNewValue);
		}
//		child.setAttribute("NEW_VALUE",newValue);
		return child;
	}
	
	public String toString() {
		String ipdStr = "OBJECT_INDEX -> PROP:" + prop + ", OLD_IDO:" + oldIdo + ", NEW_IDO:" + newIdo + ", " +
				"OLD_VALUE:" + oldValue + ", NEW_VALUE:" + newValue;
		return ipdStr;
	}	
	
}
