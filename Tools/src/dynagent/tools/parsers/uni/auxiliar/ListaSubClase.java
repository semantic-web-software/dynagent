package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author alvarez
 * Usamos esta clase para almacenar la lista de herencias
 * de un fichero dynagent. (Objetos tipo Rol)
 *
 */

public class ListaSubClase {
	private ArrayList listaSubClases = new ArrayList();
	
	/**
	 * 
	 * @return ArrayList: método get del atributo listaSubClases
	 */
	public ArrayList getListaSubclases(){
		return listaSubClases;
	}
	
	/**
	 * método set del atributo listaSubClases
	 * @param l lista de subclases
	 */
	
	public void setListaSubclases(ArrayList l){
		listaSubClases = l;
	}
	
	/**
	 * Añade una sunclase al atributo listaSubClases
	 * @param sc Subclase a añadir a la lista
	 */
	
	public void addSubClass(SubClase sc){
		listaSubClases.add(sc);
	}
	
	
	/**
	 * Método toString de la clase
	 */
	
	public String toString(){
		Iterator it = listaSubClases.iterator();
		String res=new String();
		while(it.hasNext()){
			String s = (String) (it.next().toString());
			res += s;
			res += '\n';	
		}
		return res;
		
	}
}