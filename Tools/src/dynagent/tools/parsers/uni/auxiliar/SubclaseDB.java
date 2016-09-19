package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * 
 * @author alvarez
 * Usaremos esta clase para mapear los registros de la tabla Herencias de 
 * la BD. Además añadiremos 2 campos String, correspondiente a los nombres de 
 * la clase del padre y del hijo
 */
public class SubclaseDB {
	
	private int idto = -1;
	private int idtoPadre = -1;
	private String name = "null";
	private String padre = "nullp";
	
	
	/**
	 * 
	 * @return int: El campo idto del objeto
	 */
	public int getIdto(){
		return idto;
	}
	/**
	 * 
	 * @param i actualiza el idto del objeto
	 */
	public void setIdto(int i){
		idto = i;
	}
	
	/**
	 * 
	 * @return int: El campo idtoPadre del objeto
	 */
	public int getIdtoPadre(){
		return idtoPadre;
	}
	
	/**
	 * 
	 * @param l Actualiza el idto del padre
	 */
	
	public void setIdtoPadre(int l){
		idtoPadre = l;
	}
	
	/**
	 * 
	 * @return String : el campo nombre del objeto
	 */
	
	public String getName(){
		return name;
	}
	
	
	/**
	 * 
	 * @param s Actualiza el campo name 
	 */
	public void setName(String s){
		name = s;
	}
	
	/**
	 * 
	 * @return String : el campo padre (el nombre del padre)
	 */
	public String getPadre(){
		return padre;
	}
	/**
	 * 
	 * @param s Actualiza el campo padre del objeto
	 */
	public void setPadre(String s){
		padre = s;
	}
	
	/**
	 * Método toString de la clase
	 * 
	 */
	public String toString(){
		String s = "La subclase "+ idto +" tiene como padre a : " + idtoPadre;
		return s;
	}

}
