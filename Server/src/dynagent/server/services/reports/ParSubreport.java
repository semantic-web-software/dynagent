package dynagent.server.services.reports;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * Esta clase contiene la informacion necesaria para un subreport.
 * 
 * @author Dynagent - David
 *
 */
public class ParSubreport {
	/**
	 * Indice del subreport.
	 */
	private String index;
	/**
	 * Mapa que contiene los valores a sustituir en la clausula where de la consulta.
	 * <i>Ejemplo: (2, "Dynagent") - (num columna, nombre)</i>
	 */
	private Map<Integer,String> replaceValues=new HashMap<Integer,String>();
	
	public ParSubreport(){
		
	}
	public ParSubreport(String index, Map<Integer, String> replace){
		this.index=index;
		this.replaceValues=replace;
	}
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public Map<Integer, String> getReplaceValues() {
		return replaceValues;
	}
	public void setReplaceValues(Map<Integer, String> replaceValues) {
		this.replaceValues = replaceValues;
	}
	
	public String toString(){
		String result="";
		result+="SYSOUAMI: INDEX->"+this.index;
		result+="\nSYSOUAMI: ***** REPLACEVALUES ****\n";
		Set<Integer> keys=replaceValues.keySet();
		Iterator<Integer> itkeys=keys.iterator();
		while(itkeys.hasNext()){
			Integer key=itkeys.next();
			result+="\nSYSOUAMI: KEY->"+key+" VALUE->"+replaceValues.get(key)+"\n";
		}
		
		return result;
	}
}
