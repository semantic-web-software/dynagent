package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * 
 * @author alvarez
 * Usamos esta clase para almacenar la lista de propiedades
 * de un fichero dynagent. 
 *
 */
public class ListaPropiedad {
	
private ArrayList<Propiedad> listaPropiedades = new ArrayList();
	

	/**
	 * 
	 * M�todo get del atributo listaPropiedades
	 * @return ArrayList : el atributo listaPropiedades
	 */
	public ArrayList getListaPropiedades(){
		return listaPropiedades;
	}
	
	/**
	 * M�todo set del atriburo  listaPropiedades
	 * @param l lista de propiedades
	 */
	public void setListaPropiedades(ArrayList l){
		listaPropiedades = l;
	}

	/**
	 * A�ade una propiedad al atributo listaPropiedades
	 * @param p Propiedad a a�adir a la lista
	 */
	public void addPropiedad(Propiedad p){
		listaPropiedades.add(p);
	}
	
	
	/**
	 * M�todo toString de la clase
	 */
	
	public String toString(){
		Iterator it = listaPropiedades.iterator();
		String res=new String();
		while(it.hasNext()){
			String s = (String) (it.next().toString());
			res += s;
			res += '\n';	
		}
		return res;
		
	}

}
