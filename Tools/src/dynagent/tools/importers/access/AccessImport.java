/***
 * AccessImport.java
 * @author  Jose Antonio Zamora -jazamora@ugr.es
 * @description: Esta clase se encarga de importar una serie de permisos a una 				
 */


package dynagent.tools.importers.access;



import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.naming.NamingException;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;
import dynagent.common.basicobjects.Instance;
import dynagent.tools.importers.model.DataBaseMapped;
import dynagent.tools.parsers.access.AccessString;

/**
 * Clase que facilita la importacion de permisos, con la salvedad de que muchos de sus métodos pueden ser usados en otros ambitos
 * debido a la utilidad de estos.
 * @author alvarez
 *
 */
public class AccessImport {
	
	private String ip;
	private int nbusiness;
	private String gestor;
	
	private static HashMap<String,Integer> classes;
	private static HashMap<String,Integer> individuals;
	private static HashMap<String,Integer> properties;
	private static HashMap<Integer,ArrayList<Integer>> hierarchies;
	private static HashMap<Integer, ArrayList<Instance>> instances;

	
	/**
	 * Constructor de la clase, recibe el ip de la base de datos así como el numero de negocio,
	 * haciendo set sobre los atributos ip y nbusiness y llamando al método privado buildTables
	 * que construye el resto de atributos de esta clase
	 * @param ip
	 * @param nbusiness
	 * @throws NamingException 
	 * @throws SQLException 
	 */
	
	public AccessImport(String ip,int nbusiness,String gestor) throws SQLException, NamingException{
		
		this.ip=ip;
		this.nbusiness = nbusiness;
		this.gestor = gestor;
		buildTables(ip, nbusiness, gestor);
			
	}
	
	private void buildTables(String ip, int business, String gestor) throws SQLException, NamingException{
		DataBaseMapped dbm = new DataBaseMapped(ip, business, gestor);
		classes = dbm.getIDClasses();
		individuals = dbm.getIDIndividuals();
		properties = dbm.getIDProperties();
		hierarchies = dbm.getHierarchies();
		instances = dbm.getInstances();	
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
			//System.out.println("PROPERTY: "+ins.getPROPERTY()+", "+Constants.IdPROP_USERROL);
			//System.out.println("VALUE: "+ins.getVALUE()+", "+idurol.toString());
			if(ins.getPROPERTY().equals(new Integer(Constants.IdPROP_USERROL).toString()) && ins.getVALUE().equals(idurol.toString()))
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
	
	public ArrayList<Access> run(ArrayList<AccessString> listaPermisos){
		
		ArrayList<Access> accesses = new ArrayList<Access>();
		Iterator it = listaPermisos.iterator();
		while(it.hasNext()){
			AccessString accs = (AccessString) it.next();
			Access acc = this.translateToAccess(accs);
			if(acc!=null)
				accesses.add(acc);
		}
		
		return accesses;
		
	}
	

	public Access translateToAccess(AccessString accs){
		
		Access acc = new Access();
		
		if(accs.getIDTO() != null && !accs.getIDTO().equals("TODAS")){
			Integer idto = classes.get(accs.getIDTO());
			if(idto==null){
				System.out.println("La clase "+accs.getIDTO()+" no existe en la BD");
				return null;
			}
			else
				acc.setIDTO(idto);
		}
		
		if(accs.getIDO()!= null && acc.getIDTO()!=null){
			if(isIndividualOfClass(accs.getIDO(), accs.getIDTO())){
				Integer i = individuals.get(accs.getIDO());
				acc.setIDO(i);
			}	
			else{
				System.out.println("El individuo "+accs.getIDO()+" no existe en la BD");
				return null;
			}
		}
		
		acc.setACCESSTYPE(Constants.getIdAccess(accs.getACCESSTYPE()));
		
		if(accs.getDENNIED().equals("dennied"))
			acc.setDENNIED(1);
		else
			acc.setDENNIED(0);
		
		if(accs.getVALUECLS()!=null){
			Integer idto = classes.get(accs.getVALUECLS());
			if(idto==null){
				System.out.println("La clase (para rango) "+accs.getVALUECLS()+" no existe en la BD");
				return null;
			}
			else
				acc.setIDTO(idto);
		}
		
		
		if(accs.getVALUE()!=null && !accs.getVALUECLS().equals("TODAS") && isIndividualOfClass(accs.getIDO(), accs.getVALUECLS())){
			Integer idto = individuals.get(accs.getVALUE());
			if(idto==null){
				System.out.println("La clase (para rango) "+accs.getVALUECLS()+" no existe en la BD");
				return null;
			}
			else
				acc.setIDTO(idto);
		}
		
		//TODO Comprobar correcto user
		
		
		
		if(accs.getTASK()!=null){
			if(!accs.getTASK().equals("TODAS") && isUserTask(accs.getTASK())){
				Integer idto = classes.get(accs.getTASK());
				if(idto==null){
					System.out.println("La utask "+accs.getTASK()+" no existe en la BD");
					return null;
				}
				else{
					acc.setTASK(idto);
				}
			}
		}
		
		if(accs.getUSERROL()!=null){
			if(!accs.getUSERROL().equals("TODAS") && !accs.getTASK().equals("TODAS")){
				if(isUserRol(accs.getTASK(), accs.getUSERROL())){
					acc.setUSERROL(individuals.get(accs.getUSERROL()));
				}
				else{
					System.out.println("El user rol "+accs.getUSERROL()+" no existe para la utask "+accs.getTASK());
					return null;
				}
			}
		}
		
		//TODO Comprobar que estas 3 definiciones son coherentes en instances
	
		if(accs.getPROP()!=null)	
			if(!accs.getPROP().equals("TODAS") && properties.get(accs.getPROP())!=null){
				acc.setPROP(properties.get(accs.getPROP()));
			}
			else if(accs.getPROP().equals("TODAS"));
			else{
				System.out.println("La propiedad "+accs.getPROP()+" no existe en la BD");
				return null;
			}
		
				
		
		return acc;
	}

}


