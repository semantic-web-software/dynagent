package dynagent.common.knowledge;

import java.util.*;

import dynagent.common.properties.values.Value;


public class ResultQuery implements Comparable{
	 int ido;//identificador del objeto resultado de la query
	 int idto;//identificador del tipo de objeto resultado de la query
	 HashMap<String,LinkedList<Value>> valoresXpropiedades=new HashMap<String,LinkedList<Value>>();
	 
	public ResultQuery(int ido,int idto,HashMap<String,LinkedList<Value>> valoresXpropiedades){
		this.ido=ido;
		this.idto=idto;
		this.valoresXpropiedades=valoresXpropiedades;
	}
	public int getIdo() {
		return this.ido;
	}
	
	public int getIDTO() {
		return this.idto;
	}
	public void setIdo(int ido) {
		this.ido = ido;
	}
	
	public HashMap<String,LinkedList<Value>> getValoresXpropiedades() {
		return valoresXpropiedades;
	}
	
	
	public String toString(){
		String result="<ResultQuery IDO="+ido+" >";
		Iterator<String> itProp=this.getValoresXpropiedades().keySet().iterator();
		while (itProp.hasNext()){
				String propiedad=itProp.next();
				result+="   <"+propiedad+" values"+this.valoresXpropiedades.get(propiedad)+">";
		}
		result+="\n<ResultQuery>";
		return result;
	}
	
	public LinkedList<Value> getValues(String nameProp){
		return  this.getValoresXpropiedades().get(nameProp);
	}
	
	/**
	 * DEvuelve valor si es único, si hay mas de uno devuelve null
	 * @param nameProp
	 * @return
	 */public Value getValue(String nameProp){
		LinkedList<Value> values=this.getValues(nameProp);
		
		if(values != null && values.size()==1){
			return  this.getValues(nameProp).get(0);	
		}
		
		return null;
	}
	 
	@Override
	public int compareTo(Object o) {
		return Integer.valueOf(this.getIdo()).compareTo(((ResultQuery)o).getIdo());
	}
	 
	 
	
}
