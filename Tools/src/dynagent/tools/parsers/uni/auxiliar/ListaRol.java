
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
	 * M�todo get del atributo listaRoles
	 * @return ArrayList : el atributo listaRoles
	 */
	
	public ArrayList getListaRoles(){
		return listaRoles;
	}
	
	/**
	 * M�todo set del atributo listaRoles
	 * @param l lista de roles
	 */
	
	public void setListaRoles(ArrayList l){
		listaRoles = l;
	}

	
	/**
	 * A�ade un rol al atributo listaRoles
	 * @param r Rol a a�adir a la lista
	 */
	
	public void addRol(Rol r){
		listaRoles.add(r);
	}
}
