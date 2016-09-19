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
	 * @return ArrayList: m�todo get del atributo listaSubClases
	 */
	public ArrayList getListaSubclases(){
		return listaSubClases;
	}
	
	/**
	 * m�todo set del atributo listaSubClases
	 * @param l lista de subclases
	 */
	
	public void setListaSubclases(ArrayList l){
		listaSubClases = l;
	}
	
	/**
	 * A�ade una sunclase al atributo listaSubClases
	 * @param sc Subclase a a�adir a la lista
	 */
	
	public void addSubClass(SubClase sc){
		listaSubClases.add(sc);
	}
	
	
	/**
	 * M�todo toString de la clase
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