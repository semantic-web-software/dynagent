package dynagent.tools.parsers.uni.auxiliar;

/** Usamos esta clase para mapear en java los campos de la tabla clases
 * de la base de datos. No hay mucho que comentar puesto que solo estan los
 * m�todos get, los set y el toString.
 */



public class Clase {
	private String name;
	private int idto;
	
	
	/**
	 * Metodo get para el atributo idto
	 * @return int
	 */
	public int getIdto(){
		return idto;
	}
	/**
	 *  M�todo get para el atributo name
	 * @return String
	 */
	
	public String getName(){
		return name;
	}
	
	/**
	 *  M�todo set para el atributo idto
	 * @param idto
	 */
	public void setIdto(int idto){
		this.idto = idto;
	}
	/**
	 * M�todo set para el atributo name
	 * @param name
	 */
	public void setName (String name){
		this.name = name;
	}
	
	/**
	 * M�todo toString de la clase
	 * @return String
	 */
	public String toString(){
		return "" + idto + " : " + name; 
	}
}
