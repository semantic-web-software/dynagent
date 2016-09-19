/***
 * MetaData.java
 * 
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description  		
 */



package dynagent.ruleengine.meta.api;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;
import dynagent.common.knowledge.Category;
import dynagent.common.knowledge.Clase;
import dynagent.common.properties.Property;
import dynagent.common.utils.Auxiliar;
import dynagent.ruleengine.src.ruler.FactProp;
public class MetaData {
	private int idIndividual=Constants.MIN_ID_INDIVIDUAL;
	private HashMap<String, Integer> hmIDClases = new HashMap<String, Integer> ();
	private HashMap<String, Integer> hmIDPropiedades = new HashMap<String, Integer> ();
	private HashMap <String,Integer> hmIndividuos;
	private int nbusiness;
	private HashMap<Integer,Clase>clases=new HashMap<Integer,Clase>();
	private HashMap<Integer,Clase> hmClases;
	private HashMap<Integer,FactProp> hmPropiedades;	
		
	
	public HashMap<String, Integer> getHmIDClases() {
		return hmIDClases;
	}


	public void setHmIDClases(HashMap<String, Integer> hmIDClases) {
		this.hmIDClases = hmIDClases;
	}


	public HashMap<String, Integer> getIDHmPropiedades() {
		return hmIDPropiedades;
	}


	public void setHmIDPropiedades(HashMap<String, Integer> hmIDPropiedades) {
		this.hmIDPropiedades = hmIDPropiedades;
	}


	public Integer getIdClass(String clase){
		if(hmIDClases.containsKey(clase)){
			return (hmIDClases.get(clase));
		}
		else{
			
			return null;
		}
	}
	
	
	public Integer getIdProp(String propiedad){
		if(hmIDPropiedades.containsKey(propiedad)){
			return (hmIDPropiedades.get(propiedad));
		}
		else{
			
			return null;
		}
	}
	
	
	public String getIdIndividual(String individual){
		if(hmIndividuos.containsKey(individual)){
			return (hmIndividuos.get(individual)).toString();
		}
		else{
			int ido=this.newIdo();
			hmIndividuos.put(individual, ido);
			return String.valueOf(ido);
		}
	}
	
	
	
	public  int  newIdo(){
		return idIndividual++;
	}
	
	
	public boolean isObjectProperty(Integer idProp){
			boolean resultado = false;
			if(this.getHmPropiedades().containsKey(idProp)){
				FactProp fp=this.getHmPropiedades().get(idProp);
				Category category = new Category(fp.getCAT());
				if(category.isObjectProperty())
					resultado = true;
				else
					resultado = false;
			}
			return resultado;
		
	}
	
	
	public Instance traslateInstanceToNumeric(Instance ins){
		Instance insCod=new Instance();
		insCod.setIDO(this.getIdIndividual(ins.getIDO()));
		insCod.setIDTO(getIdClass(ins.getIDTO()).toString());
		insCod.setNAME(ins.getNAME());
		insCod.setOP(ins.getOP());
		Integer idProp=getIdProp(ins.getPROPERTY());
		insCod.setPROPERTY(idProp.toString());
		insCod.setQMAX(ins.getQMAX());
		insCod.setQMIN(ins.getQMIN());
		insCod.setVALUECLS(getIdClass(ins.getVALUECLS()).toString());
		if(this.isObjectProperty(idProp))
			insCod.setVALUE(this.getIdIndividual(ins.getVALUE()));
		else{
			insCod.setVALUE(ins.getVALUE());
		}
		return insCod;
	}


	public int getIdIndividual() {
		return idIndividual;
	}


	public void setIdIndividual(int idIndividual) {
		this.idIndividual = idIndividual;
	}


	public HashMap<Integer, FactProp> getHmPropiedades() {
		return hmPropiedades;
	}


	public void setHmPropiedades(HashMap<Integer, FactProp> hmPropiedades) {
		this.hmPropiedades = hmPropiedades;
	}
	
	
	public String toString(){
		String result="\n\n===============================METADATA (BUSINESS="+this.getNbusiness()+")===============================";
		result+="\nMAPA DE IDS DE CLASES:\n";
		result+=this.getHmIDClases().toString();
		result+="\nMAPA DE IDS DE PROPIEDADES:\n";
		result+=this.getHmIDPropiedades();
		result+="\nPROPIEDADES:\n";
		result+=Auxiliar.hashMapToString(this.getHmPropiedades(),"","");
		result+="\nCLASES:\n";
		for(int i=0;i<this.clases.size();i++){
			if(this.clases!=null && this.clases.get(i)!=null)
				result+=this.clases.get(i).toString();
		}
		result+="\n==================================================================================================================";
		return result;
	}


	public HashMap<String, Integer> getHmIDPropiedades() {
		return hmIDPropiedades;
	}


	public int getNbusiness() {
		return nbusiness;
	}


	public void setNbusiness(int nbusiness) {
		this.nbusiness = nbusiness;
	}


	


	public HashMap<Integer, Clase> getClases() {
		return clases;
	}


	public void setClases(HashMap<Integer, Clase> clases) {
		this.clases = clases;
	}


	public HashMap<Integer, Clase> getHmClases() {
		return hmClases;
	}


	public void setHmClases(HashMap<Integer, Clase> hmClases) {
		this.hmClases = hmClases;
	}


	public HashMap<String, Integer> getHmIndividuos() {
		return hmIndividuos;
	}


	public void setHmIndividuos(HashMap<String, Integer> hmIndividuos) {
		this.hmIndividuos = hmIndividuos;
	}
	
	public Iterator<Property> getAllPropertyIterator(int idto) {
//		System.out.println("metaData.getallpropertyiterator idto="+idto);
		return this.getClases().get(idto).getAllProperties();
	}


	
	
	
	 public boolean hasProperty(String nameClase,String nameProp){
		 if(this.getHmIDPropiedades().get(nameProp)!=null&&this.getHmIDClases().get(nameClase)!=null){
			 return this.hasProperty(this.getHmIDClases().get(nameClase), this.getHmIDPropiedades().get(nameProp));
		 }
		 else return false;
	 }
	 
	 
	 public boolean hasProperty(int idto,int idprop){
		return this.getClase(idto).getIdsPropiedades().contains(idprop);
	}


	
	public Clase  getClase(int idclase) {
		return this.getClases().get(idclase);
	}
	
	
	
	public HashSet<String>  getAllClaseNames() {
		HashSet<String> clases=new HashSet<String>();
		Iterator it=this.getClases().values().iterator();
		while(it.hasNext()){
			Clase cls=(Clase)it.next();
			clases.add(cls.getName());
		}
		return clases;
	}
	
	public HashSet<String>  getAllPropertyNames() {
		HashSet<String> clases=new HashSet<String>();
		Iterator it=this.getHmPropiedades().values().iterator();
		while(it.hasNext()){
			FactProp fp=(FactProp)it.next();
			clases.add(fp.getNAME());
		}
		return clases;
	}

	

}
