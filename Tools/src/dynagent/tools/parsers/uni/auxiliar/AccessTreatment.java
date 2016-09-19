package dynagent.tools.parsers.uni.auxiliar;


import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Access;

public class AccessTreatment {
	
	public ArrayList<String> utasks;
	
	public ArrayList getUtasks(ArrayList<SubClase> scs){
		if(this.utasks==null)
			buildUtasks(scs);
		return this.utasks;
			
	}
	/**
	 * Construye una lista de nombres de las utasks de un fichero dynagent y se la asigna al parametro utasks
	 * @param scs Lista que contiene las subclases de un fichero dynagent
	 */
	private void buildUtasks(ArrayList<SubClase> scs){
		utasks = new ArrayList<String>();
		Iterator it = scs.iterator();
		while(it.hasNext()){
			SubClase sc = (SubClase) it.next();
			if(sc.getListaPadres().contains(Constants.CLS_UTASK)){
				utasks.add(sc.getName());
			}
		}
	}
	/**
	 * Devouelve todas las propiedades que existen en una usertask
	 * @param pas Lista de propiedades de clases de un fichero dynagent
	 * @param name Nombre de la userTask
	 * @return ArrayList : Devolvemos todas las propiedades que existen en una usertask en una lista
	 */
	public ArrayList<PropiedadAtrib> getPropertiesUtask(ArrayList<PropiedadAtrib> pas, String name){
		ArrayList<PropiedadAtrib> res = new ArrayList<PropiedadAtrib>();
		Iterator it = pas.iterator();
		while(it.hasNext()){
			PropiedadAtrib pa = (PropiedadAtrib) it.next();
			if(pa.getClaseCont().equals(name))
				res.add(pa);
		}
		return res;
	}
	
	
	// TODO Si hay varias operaciones devolverlas todas, no solo la primera...DONE
	
	public ArrayList<Integer> getOperation(ArrayList<PropiedadAtrib> pas){
		ArrayList<Integer> res = new ArrayList<Integer>();
		boolean enc = false;
		Iterator it = pas.iterator();
		while(it.hasNext() && !enc){
			PropiedadAtrib pa = (PropiedadAtrib) it.next();
			if(pa.getNombreProp().equals("operation") && (pa.getEnumerados().size()>0)){
				enc = true;
				Iterator it2 = pa.getEnumerados().iterator();
				while(it2.hasNext()){
					String operation = (String) it2.next();
					res.add(new Integer(Constants.getIdAccess(operation)));
				}
					
			}
		}
		return res;
	}
	

	public ArrayList getRangoTarget(ArrayList<PropiedadAtrib> pas, String prop){
		Iterator it = pas.iterator();
		while(it.hasNext()){
			PropiedadAtrib pa = (PropiedadAtrib) it.next();
			if(pa.getNombreProp().equals(prop) && pa.getRestricciones()!=null && pa.getRestricciones().size()>0){
				return pa.getRestricciones();
			}
			else if(pa.getNombreProp().equals(prop) && pa.getEnumerados() != null)
				return pa.getEnumerados();
		}
		
		return null;
	}
		
	/**
	 * Crea un Access rellenando solo los campos dennied, task, accesstype, idto y userrol
	 * @param userT : Usertask sobre la que vamos a indicar el permiso
	 * @param accesstype : operation que vamos a permitile al access
	 * @param idto : TargetClass
	 * @param idtRol : UserRol permitido para dicho Access
	 * @return
	 */
	public Access createAccess(int userT, int accesstype, int idto, int idtRol){
		Access acc = new Access();
		acc.setDENNIED(new Integer(0));
		acc.setIDTO(new Integer(idto));
		acc.setACCESSTYPE(new Integer(accesstype));
		acc.setTASK(new Integer(userT));
		
		if(idtRol != -1){
			//acc.setUSERROL(new Integer(idtRol));
		}
		
		return acc;
		
	}
	/**
	 * Crea un access con todos sus campos
	 * @param user
	 * @param idto
	 * @param idtRol
	 * @param idProp
	 * @param idtRel
	 * @param operation
	 * @param idtoURol
	 * @return
	 */
	
	public Access createAccess(int user, int idto, int idtRol, int idProp, int idtRel, String operation, int idtoURol) {
		Access acc = new Access();
		acc.setACCESSTYPE(new Integer(Constants.getIdAccess(operation)));
		acc.setVALUECLS(new Integer(idtRel));
		acc.setDENNIED(0);
		acc.setIDTO(idto);
		acc.setPROP(idProp);
		acc.setTASK(user);
	//	acc.setUSERROL(idtoURol);
		return acc;
	}
	
	

}
