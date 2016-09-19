package dynagent.common.communication;

import java.util.HashMap;

import java.util.Iterator;

public class HAttribValue {

	private HashMap<String,String> arrayAttribValue;
	
	public HAttribValue() {
		arrayAttribValue = new HashMap<String,String>();
	}
	
	public HAttribValue(String allIn) {
		String all = allIn.substring(new String("ATTRIBS:").length(), allIn.length());
		String[] spl = all.split("#");
		arrayAttribValue = new HashMap<String,String>();
		for (int i=0;i<spl.length-1;i++) {
			String attribValue = spl[i];
			int index = attribValue.indexOf("=");
			String attrib = attribValue.substring(0, index);
			String value = attribValue.substring(index+1, attribValue.length());
			arrayAttribValue.put(attrib, value);
		}
	}
	public void addAttribValue(String attrib, String value) {
		arrayAttribValue.put(attrib, value);
	}
	public String getAttribValue(String attrib) {
		return arrayAttribValue.get(attrib);
	}
	public String toString() {
		String dev = "ATTRIBS:";
		Iterator it = arrayAttribValue.keySet().iterator();
		while (it.hasNext()) {
			String attrib = (String)it.next();
			String value = arrayAttribValue.get(attrib);
			dev += attrib + "=" + value + "#";
		}
		return dev;
	}

}
