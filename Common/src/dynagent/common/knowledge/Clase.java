/***
 * Clase.java
 * 
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description Esta clase representa toda la información sobre una clase del modelo de negocio.		
 */



package dynagent.common.knowledge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import dynagent.common.properties.ObjectProperty;
import dynagent.common.properties.Property;
import dynagent.common.utils.Auxiliar;

public class Clase {
	int idto;
	String name;
	//private int num_superiors;
	//private int num_specializeds;
	private int num_properties;
	private HashMap<Integer,Property> hmProperties;
	private HashSet<Integer> superiors;
	private HashSet<Integer> idsPropiedades;
	private HashSet<Integer> specializeds;
	private boolean abstractClass;
	
	public HashMap<Integer, Property> getHmProperties() {
		return hmProperties;
	}
	
	public Iterator<Property> getAllProperties(){
		HashSet<Property>hproperties=new HashSet<Property>();
		Iterator<Property> itp;
		 for(Iterator it=this.hmProperties.keySet().iterator();it.hasNext();){
			   Integer key=(Integer)it.next();
			   hproperties.add(this.hmProperties.get(key));
		    }
		 return hproperties.iterator();
	}
	
	
	public Iterator<Property> getObjectProperties(){
		HashSet<Property>hproperties=new HashSet<Property>();
		Iterator<Property> itp;
		 for(Iterator it=this.hmProperties.keySet().iterator();it.hasNext();){
			   Integer key=(Integer)it.next();
			   Property p=this.hmProperties.get(key);
			   if(p instanceof ObjectProperty)
				   hproperties.add(p);
		    }
		 return hproperties.iterator();
	}
	
	
	public void setHmProperties(HashMap<Integer, Property> hmProperties) {
		this.hmProperties = hmProperties;
	}
	public int getIdto() {
		return idto;
	}
	public void setIdto(int idto) {
		this.idto = idto;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getNum_properties() {
		return num_properties;
	}
	public void setNum_properties(int num_properties) {
		this.num_properties = num_properties;
	}
	
	
	
	
	public HashSet<Integer> getSpecializeds() {
		return specializeds;
	}
	public void setSpecializeds(HashSet<Integer> specializeds) {
		this.specializeds = specializeds;
	}
	public HashSet<Integer> getSuperiors() {
		return superiors;
	}
	public void setSuperiors(HashSet<Integer> superiors) {
		this.superiors = superiors;
	}
	
	
	
	public HashSet <Integer> getIdsPropiedades() {
		return idsPropiedades;
	}
	
	public boolean hasProperty(int idProp){
		if(this.getIdsPropiedades().contains(new Integer(idProp)))
			return true;
		else return false;
	}
	
	
	
	public Property getProperty(int idProp){
		this.getIdsPropiedades();
		return this.getHmProperties().get(idProp);
	}

	public void setIdsPropiedades(HashSet<Integer> idsPropiedades) {
		this.idsPropiedades = idsPropiedades;
	}

	public String toString(){
		String result="\n                  -------------- CLASE "+this.name+"  IDTO="+this.idto+"--------------" ;
		if(this.superiors!=null)
			result+="     CLASESUPERIORES("+this.getSuperiors().size()+")="+this.getSuperiors();
		if(this.specializeds!=null)
			result+="     CLASESHIJAS("+this.getSpecializeds().size()+")="+this.getSpecializeds();
		result=result+"   PROPIEDADES("+this.getNum_properties()+"):";
		result+=Auxiliar.hashMapToString(this.getHmProperties(),"","");
		result+="\n                ------------------------        ";  
		return result;
	}

	public boolean isAbstract() {
		return abstractClass;
	}

	public void setAbstract(boolean abstractClass) {
		this.abstractClass = abstractClass;
	}
	
	
}
