package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;
import java.util.Iterator;

import dynagent.common.Constants;



/**
 * 
 * @author alvarez
 * Cn esta clase adaptamos los datos obtenidos de la BD
 * para hacerlos mas manejables, agrupando los padres de cada hijo
 */
public class SubClase {
	
	private String name="";;
	private ArrayList listaPadres = new ArrayList();
	
	
	/**
	 * Constructor vacio
	 *
	 */
	public SubClase(){
	}
	
	/**
	 * 
	 * @return String : El nombre de la clase que posee "padres"
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * 
	 * @param s Actualiza el campo name del objeto
	 */
	
	public void setName(String s){
		name = s;
	}
	
	/**
	 * 
	 * @return ArrayList : Los padres de una clase
	 */
	public ArrayList getListaPadres(){
		return listaPadres;
	}
	
	
	/**
	 * 
	 * @param l Actualiza el campo listaPadres de un objeto
	 */
	public void setListaPadres(ArrayList l){
		listaPadres = l;
	}
	
	
	/**
	 * Método toString de la clase
	 */
	public String toString(){
		String s = "La subclase "+ name +" tiene como padres a : ";
		Iterator it = listaPadres.iterator();
		while(it.hasNext()){
			String s2 = (String) it.next();
			s += s2;
			s += " ";
		}
		return s;
	}

}
