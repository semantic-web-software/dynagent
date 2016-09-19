package dynagent.tools.importers.model;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Instance;

public class TableUtils {
	

	private static HashMap<String,Integer> classes;
	private static HashMap<String,Integer> individuals;
	private static HashMap<String,Integer> properties;
	private static HashMap<Integer,ArrayList<Integer>> hierarchies;
	private static HashMap<Integer, ArrayList<Instance>> instances;
	private static boolean built = false;
	
	public static HashMap<String, Integer> getClasses() {
		return classes;
	}
	public void setClasses(HashMap<String, Integer> classes) {
		this.classes = classes;
	}
	public static HashMap<Integer, ArrayList<Integer>> getHierarchies() {
		return hierarchies;
	}
	public void setHierarchies(HashMap<Integer, ArrayList<Integer>> hierarchies) {
		this.hierarchies = hierarchies;
	}
	public static HashMap<String, Integer> getIndividuals() {
		return individuals;
	}
	public void setIndividuals(HashMap<String, Integer> individuals) {
		this.individuals = individuals;
	}
	public static HashMap<Integer, ArrayList<Instance>> getInstances() {
		return instances;
	}
	public void setInstances(HashMap<Integer, ArrayList<Instance>> instances) {
		this.instances = instances;
	}
	public static HashMap<String, Integer> getProperties() {
		return properties;
	}
	public void setProperties(HashMap<String, Integer> properties) {
		this.properties = properties;
	}
	
	public static void buildTables(String ip, int business, String gestor) throws SQLException, NamingException{
		if(!built){
			DataBaseMapped dbm = new DataBaseMapped(ip, business, gestor);
			classes = dbm.getIDClasses();
			individuals = dbm.getIDIndividuals();
			properties = dbm.getIDProperties();
			hierarchies = dbm.getHierarchies();
			instances = dbm.getInstances();
			built = true;
		}
	}
	
	public static boolean isUserTask(String utask){
		
		Integer idutask = classes.get(utask);
		
		if(idutask==null){
			System.out.println("La clase "+utask+" no existe en la base de datos");
			return false;
		}
		
		ArrayList<Integer> parents = hierarchies.get(idutask);
		if(parents==null){
			System.out.println("La clase "+utask+" no tiene padres en la base de datos ");
			return false;
		}
		
		return parents.contains(new Integer(Constants.IDTO_UTASK));
		
	}
	

	

	
	
	public static boolean isUserRol(String utask, String urol){
		
		Integer idutask = classes.get(utask);
		if(idutask==null){
			System.out.println("La clase "+utask+" no existe en la base de datos");
			return false;
		}
		
		Integer idurol = individuals.get(urol);
		if(idurol==null){
			System.out.println("El individuo "+urol+" no existe en la base de datos");
			return false;
		}
		
		ArrayList<Instance> utaskIns = instances.get(idutask);
		if(utaskIns==null){
			System.out.println("La utask "+utask+" no tiene entradas en la tabla instances");
			return false;
		}
		
		Iterator itutasksIns = utaskIns.iterator();
		while(itutasksIns.hasNext()){
			Instance ins = (Instance) itutasksIns.next();
			if(ins.getPROPERTY().equals(Constants.IdPROP_USERROL) && ins.getVALUE().equals(idurol.toString()))
				return true;
		}
		
		return false;
	}
	

	
	public static boolean isIndividualOfClass(String individual, String clas){
		
		Integer idclas = classes.get(clas);
		
		if(idclas==null)
			System.out.println("La clase "+clas+" no existe en la base de datos");
		else{
			ArrayList<Instance> insCls = instances.get(idclas);
			System.out.println("INSTANCES: "+insCls);
			Integer idind = individuals.get(individual);
			if(idind==null)
				System.out.println("El individuo "+individual+" no existe en la base de datos");
			else{
				Iterator itInstances = insCls.iterator();
				while(itInstances.hasNext()){
					Instance ins = (Instance) itInstances.next();
					if(ins.getIDO()!=null && ins.getIDO().equals(idind.toString()))
						return true;
				}
			}
			
		}
			return false;
		
	}
	

}
