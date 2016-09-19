package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;
import java.util.Iterator;



/**
 * 
 * @author alvarez
 * Usamos esta clase para almacenar la lista de roles que existen
 * dentro de una relacion (Objetos tipo RolAtrib)
 * 
 */

public class ListaRolAtrib {
private ArrayList listaRoles = new ArrayList();

	/**
	 * 
	 * M�todo get del atributo listaRoles
	 * @return ArrayList : el atributo listaRoles
	 */

	public ArrayList getListaRolAtrib(){
		return listaRoles;
	}
	
	/**
	 * M�todo set del atributo listaRoles
	 * @param l lista de roles
	 */
	
	public void setListaRolAtrib(ArrayList l){
		listaRoles = l;
	}

	
	/**
	 * A�ade un rol al atributo listaRoles
	 * @param r Rol a a�adir a la lista
	 */
	
	public void addRolAtrib(RolAtrib r){
		listaRoles.add(r);
	}
	
	/**
	 * M�todo toString de la clase
	 */
	public String toString(){
		Iterator it = listaRoles.iterator();
		String res=new String();
		while(it.hasNext()){
			String s = (String) (it.next().toString());
			res += s;
			//res += '\n';	
		}
		return res;
		
	}
}
