package dynagent.common.communication;

import java.util.ArrayList;

import org.jdom.Element;

public class Changes {

	private ArrayList<ObjectChanged> aObjectChanged;
	
	public Changes() {
		aObjectChanged = new ArrayList<ObjectChanged>();
	}
	
	public ArrayList<ObjectChanged> getAObjectChanged() {
		return aObjectChanged;
	}
	public void addObjectChanged(ObjectChanged oi) {
		aObjectChanged.add(oi);
	}
	
	public void add(Changes c){
		ArrayList<ObjectChanged>carr= c.getAObjectChanged();
		aObjectChanged.addAll(carr);
	}
	/*public ObjectChanged getPropertyByIdo(int prop, int ido) {
		ObjectChanged oc = null;
		for (int i=0;i<aObjectChanged.size();i++) {
			ObjectChanged act = aObjectChanged.get(i);
			if (act.getProp()!=null && act.getProp()==prop && act.getNewIdo()==ido) {
				oc = act;
				break;
			}
		}
		return oc;
	}
	public ObjectChanged getPropertyByIdo(int ido) {
		ObjectChanged oc = null;
		for (int i=0;i<aObjectChanged.size();i++) {
			ObjectChanged act = aObjectChanged.get(i);
			if (act.getNewIdo()==ido) {
				oc = act;
				break;
			}
		}
		return oc;
	}*/
	public ObjectChanged getPropertyByIdo(Integer prop, int ido) {
		ObjectChanged oc = null;
		for (int i=0;i<aObjectChanged.size();i++) {
			ObjectChanged act = aObjectChanged.get(i);
			if (act.getNewIdo()==ido) {
				if (prop!=null && act.getProp()!=null) {
					if (act.getProp().equals(prop)) {
						oc = act;
						break;
					}
				} else if (prop==null) {
					oc = act;
					break;
				}
			}
		}
		return oc;
	}
	public String toString() {
		String changes = "CHANGES:\n";
		for (int i=0;i<aObjectChanged.size();i++)
			changes += aObjectChanged.get(i) + "\n";
		return changes;
	}

	public Element toElement() {
		Element root = new Element("CHANGES");
		
		for (int i=0;i<aObjectChanged.size();i++) {
			ObjectChanged oi = aObjectChanged.get(i);
			Element child = oi.toElement();
			root.addContent(child);
		}
		return root;
	}

}
