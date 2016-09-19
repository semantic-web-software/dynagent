
package dynagent.tools.parsers.uni.auxiliar;

/**
 * 
 * @author alvarez
 * Usamos esta clase para almacenar la lista de roles
 * de un fichero dynagent. (Objetos tipo Rol)
 *
 */

import java.util.*;

public class ListaRol {
	private ArrayList listaRoles = new ArrayList();
	
	/**
	 * 
	 * Método get del atributo listaRoles
	 * @return ArrayList : el atributo listaRoles
	 */
	
	public ArrayList getListaRoles(){
		return listaRoles;
	}
	
	/**
	 * Método set del atributo listaRoles
	 * @param l lista de roles
	 */
	
	public void setListaRoles(ArrayList l){
		listaRoles = l;
	}

	
	/**
	 * Añade un rol al atributo listaRoles
	 * @param r Rol a añadir a la lista
	 */
	
	public void addRol(Rol r){
		listaRoles.add(r);
	}
}
