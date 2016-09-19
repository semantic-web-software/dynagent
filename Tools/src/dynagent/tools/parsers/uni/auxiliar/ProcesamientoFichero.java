package dynagent.tools.parsers.uni.auxiliar;

/**
 * @author alvarez
 * 
 * Almacena de un documento en lenguaje dynagent las entradas 
 * más estáticas, sin comprobar dependencias, simplemente anota nombres
 * para evitar repeticiones y comprobar omisiones o errores a la hora de asignar
 * nombres. Usado en Anasint
 */

import java.util.*;
public class ProcesamientoFichero {
	
	
	/**
	 * - listaClase contiene las clases declaradas en un fichero dynagent
	 * - listaRelacion contiene las relaciones declaradas en un fichero dynagent
	 * - listaRol contiene los roles declarados en un fichero dynagent
	 * - listaRolesPorComprobar contiene los roles declarados dentro de una relacion
	 *   y que deben existir declarados fuera de ella
	 * - listaRelacionesPorComprobar contiene las relaciones usadas en los rol pointers,
	 *   y que deben haber sido declaradas en el fichero.
	 * - listaCLasesPorComprobar contiene las clases usadas como object property y que deben
	 *   haber sido declaradas en el fichero.
	 */
	
	
	public ArrayList listaClase = new ArrayList();
	public ArrayList listaRelacion = new ArrayList();
	public ArrayList listaRol = new ArrayList();
	public ArrayList listaPropiedad = new ArrayList(); 
	public ArrayList listaRolesPorComprobar = new ArrayList();
	public ArrayList listaRelacionesPorComprobar = new ArrayList();
	public ArrayList listaClasesPorComprobar = new ArrayList();
	public ArrayList listaRolPointer = new ArrayList();
	public ArrayList listaIndividuos = new ArrayList();
	
	
	/**
	 * Funcion auxiliar. Dada una lista de cadenas imprime su tamaño 
	 * y dichas cadenas separadas por espacio.
	 * @param l
	 */
	public void imprimeLista(ArrayList<String> l){
		System.out.println("El tamaño de la lista es: "+ l.size());
		Iterator it = l.iterator();
		while (it.hasNext()){
			String s = (String) it.next();
			System.out.print(s+" ");
		}
		System.out.println();
	}
	
	
	/** 
	 * Concatena dos listas, l1 y l2
	 * @param l1
	 * @param l2
	 * @return devuelve la lista concatenada
	 */
	public ArrayList unionListas (ArrayList l1, ArrayList l2){
	
		ArrayList lista = new ArrayList();
		Iterator it1 = l1.iterator();
		Iterator it2 = l2.iterator();
		while(it1.hasNext()){
			String s = (String) it1.next();
			lista.add(s);
		}
		while(it2.hasNext()){
			String s = (String) it2.next();
			lista.add(s);
		}
	 
	 	return lista;
	
	}
	
	/**
	 * Comprueba si en la lista1 existen elementos repetidos
	 * @param l1
	 */
	
	public ArrayList<String> compruebaRepeticiones(ArrayList l1){
		
		ArrayList res = new ArrayList();
		Iterator it = l1.iterator();
		int index = 0;
		while(it.hasNext()){
			String s = (String) it.next();
			int lastIndex = l1.lastIndexOf(s);
			if(index != lastIndex){
				res.add(s);
				System.out.println("Error: existe un nombre duplicado: "+s);
			}
			index++;
		}
		return res;
	}
	/**
	 * Comprueba la coherencia del fichero en el sentido de que todo lo que se ha usado
	 * ha sido declarado anterior o posteriormente
	 * 
	 * @param l1
	 * @param l2
	 * @param i indica si debe dar error o warning
	 * @param type para distinguir entre cadena, rol o relacion
	 * @return
	 */
	
	public ArrayList compruebaListas(ArrayList l1, ArrayList l2, int i, String type){	
		
		//imprimeLista(l1);
		//imprimeLista(l2);
		Iterator it = l1.iterator();
		ArrayList eliminaciones = new ArrayList();
		while (it.hasNext()){
			String s = (String) it.next();
			if (!l2.contains(s)){
				if(i==1){
					System.out.println("Error:  " + type + s +" se ha usado sin que se haya declarado ");
					eliminaciones.add(s);
				}
				else
					System.out.println("Warning: " + type + s + " no se esta usando para ningun rol y/o relacion ");
			}
		}
		
		return eliminaciones;
			
	}
}
