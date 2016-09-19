package dynagent.ruleengine.test.test21.src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;
import jess.ValueVector;
import dynagent.ruleengine.meta.modelImport.DataBaseMapped;

public class ParserJess {
	
	private HashMap<String, Integer> hc;
	private HashMap<String, Integer> hp;
	
	public static void main (String args[]){
		/*try {
			new ParserJess();			
		} catch (IOException e) {			
			e.printStackTrace();
		}*/
		try {
			new ParserJess("E:/DESARROLLO/Workspace/Cano/RuleEngine/src/dynagent/ruleengine/test/test21/src/");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ParserJess(int s){
		prueba();
	}
	
	public ParserJess(String ruta) throws IOException{	
		DataBaseMapped dbm = new DataBaseMapped("192.168.1.3", 7);
		hc = dbm.getIDClasses();
		hp = dbm.getIDProperties();
		BufferedReader a = new BufferedReader(new FileReader(ruta+"reglas.clp"));
		BufferedWriter out = new BufferedWriter(new FileWriter(ruta+"copia.clp"));
		String s = a.readLine();
		Integer value;
		// docDataModel.getIdClass(String)
		while (s != null) {
			if(s.contains("IDTO")){
				int n = s.indexOf("IDTO ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+5,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replaceFirst(result, String.valueOf(value));
				}
			}
			if(s.contains("VALUE ")){
				int n = s.indexOf("VALUE ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+6,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			if(s.contains("VALUECLS")){
				int n = s.indexOf("VALUECLS ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+9,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);
					if(value!=null)
						s=s.replaceFirst(result, String.valueOf(value));
				}
			}
			if(s.contains("CLSREL ")){
				int n = s.indexOf("CLSREL ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+7,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			if(s.contains("CLSRELB")){
				int n = s.indexOf("CLSRELB ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+8,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			if(s.contains("ROL")){
				int n = s.indexOf("ROL ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+4,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			if(s.contains("ROLB")){
				int n = s.indexOf("ROLB ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+5,n+p+1);
				if(!variable(result)){
					value=buscarHashtable(result);					
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			if(s.contains("PROP")){
				int n = s.indexOf("PROP ");
				int p = (s.substring(n+1)).indexOf(")");
				String result = s.substring(n+5,n+p+1);
				if(!variable(result)){
					value=buscarHashtableProperty(result);	
					if(value!=null)
						s=s.replace(result, String.valueOf(value));
				}
			}
			out.write(s);
			out.newLine();
			s = a.readLine();
		}		
		out.flush();
		out.close();
	}
	public boolean variable(String s){
		if( s.substring(0,1).equals("?"))
			return true;
		else return false;
	}
	
	public Integer buscarHashtable(String s){
		return hc.get(s);
	}
	
	public Integer buscarHashtableProperty(String s){
		return hp.get(s);
	}
	
	public void prueba(){
		try{	
			Rete r = new Rete();
			jess.Fact f2 = new jess.Fact("", r);
	        ValueVector v1 = new ValueVector();
	    	v1.add(new Value("IDO_0", RU.STRING));
	    	v1.add(new Value("F0", RU.STRING));        
	        f2.setSlotValue("1", new Value(v1, RU.LIST));
	    	ValueVector v2 = new ValueVector();
	    	v2.add(new Value("IDO_0", RU.STRING));
	    	v2.add(new Value("F0", RU.STRING));
	    	v2.add(new Value("IDO_1", RU.STRING));
	    	v2.add(new Value("F1", RU.STRING));
	    	f2.setSlotValue("2", new Value(v2, RU.LIST));
	    	r.assertFact(f2);
	    	
	    	int i=0;
	    	Value v3;
	    	while((v3=f2.get(i)).stringValue(r.getGlobalContext()).equals("")){
	    		System.out.println(i+": "+v3);  
	    		i++;
	    	}
		} catch (JessException e) {			
			e.printStackTrace();
		}
	}

}
