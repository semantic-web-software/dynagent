package dynagent.common.utils;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;

import org.jdom.Element;
import org.jdom.JDOMException;

import dynagent.common.Constants;

public class DebugLog {

	public static final int DEBUG_GUI=1;
	public static final int DEBUG_RULES=2;
	public static final int DEBUG_COMMUNICATIONS=3;
	
	public ArrayList<String> debugRulesFired=new ArrayList<String>();
	
	
	private ArrayList<Object> m_debugData=new ArrayList<Object>();
	private boolean enableDebugLog=true;
	
	public boolean getEnableDebug(){
		return enableDebugLog;
	}
	
	/**
	 * reglas en el orden que se disparan
	 * public ArrayList<String> getRULESFIRED(){
	 */
	public ArrayList<String> getRULESFIRED(){
		return debugRulesFired;
		
	}
	
	public ArrayList<String> getRULESFIREDSORTED(){
		ArrayList<String> result=new ArrayList<String>();
		ArrayList<String> aux=new ArrayList(this.getRULESFIRED());
		Collections.sort(aux,new Comparator<String>(){

			@Override
			public int compare(String o1, String o2) {
				return Constants.languageCollator.compare(o1,o2);
			}
			
		});
		for(int i=0;i<aux.size();i++){
			if(!result.contains(aux.get(i))){
				result.add(aux.get(i));
			}
		}
		return result;
	}
	

	
	public void setEnableDebug( boolean val ){
		enableDebugLog=val;
	}

	public void addDebugData(int type,String action,Exception data){
		if( data!=null ){
			outputStringAdaptor oa = new outputStringAdaptor();
			data.printStackTrace(new PrintStream(oa));
			addDebugData(type,action,oa.data);
		}
	}
	
	public void addRuleFired(String rulename){
		this.debugRulesFired.add(rulename);
	}
	
	public void addDebugData(int type,String action,Element data){
		if( enableDebugLog && data!=null ){
			try{
				addDebugData(type,action,jdomParser.returnXML(data));
			}catch(JDOMException e){;}
		}
	}
	
	public void addDebugData(int type,String action,String data){
		if( enableDebugLog ){
			if (m_debugData.size() > 1000)
				m_debugData.remove(0);
			if (data.length() > 700)
				data = data.substring(0, 700);
			
			m_debugData.add(formatData(type,action,data));
		}
	}

	public String getDebugData(){
		String res= m_debugData.size()==0 ? null:"";
		for(int i=0;i<m_debugData.size();i++){
			if( i>0 ) res+="\n";
			res+= (String)m_debugData.get(i);
		}
		return res;
	}
	
	public void clearDebugData(){
		m_debugData.clear();
	}
	
	private String formatData(int type,String action,String data){
		String formatedData=null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:s-S");
		java.util.Date tiempo = new Date(System.currentTimeMillis());
		String hour=sdf.format(tiempo);
		
		switch(type){
			case DEBUG_GUI:
				formatedData="<GUI action="+action+" hour="+hour+">"+data+"</GUI>";
				break;
			case DEBUG_RULES:
				formatedData="<RULE action="+action+" hour="+hour+">"+data+"</RULE>";
				break;
			case DEBUG_COMMUNICATIONS:
				formatedData="<COMMUNICATION action="+action+" hour="+hour+">"+data+"</COMMUNICATION>";
				break;
		}
		
		return formatedData;
	}
	
}



class outputStringAdaptor extends OutputStream {
	String data = "";
	public void close() {
		;
	}

	public void flush() {
		;
	}

	public void write(byte[] buf) {
		data = new String(buf);
	}

	public void write(byte[] b, int off, int len) {
		for (int i = off; i < len; i++) {
			data += String.valueOf((char) b[i]);
		}
	}

	public void write(int b) {
		data += String.valueOf((char) b);
	}
	
		 
	
	
}
