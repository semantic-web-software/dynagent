package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;


/**
 * 
 * @author alvarez
 * 
 * Almacena de un documento dynagent dependencias tales como herencia,
 * origen de propiedades, cardinalidad, etc. Utilizado en la clase Anasem
 *
 */
public class ProcesamientoFicheroAdvanced {
	
	
	/**
	 * Simplemente creamos las listas, ya que se rellenaran en anasem.g
	 * y simplemente se requiere su informacion. 
	 * Es una forma de manejar varias listas en una sola clase
	 * 
	 * listaSubclase contiene informacion análoga a la de la clase ProcesamientoFichero
	 * listaPropiedad contiene además de lo ya existente, la clase, rol o relacion que contiene dicha propiedad
	 * listaRolAtrib igual que listaPropiedad
	 * listaRol es semejante al de la clase ProcesamientoFichero
	 */
	public ArrayList listaSubclase = new ArrayList();
	public ArrayList listaPropiedad = new ArrayList();
	public ArrayList<RolAtrib> listaRolAtrib = new ArrayList();
	public ArrayList listaRol = new ArrayList();
	public ArrayList<PropiedadAtrib> listaPropAt = new ArrayList();
	public ArrayList listaPropCl = new ArrayList();
	public ArrayList listaIndividuos = new ArrayList();
	public ArrayList listaRolPointers = new ArrayList();

}
