package dynagent.tools.parsers.uni.auxiliar;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author alvarez
 * Uso semejante a SubClase, puesto que representamos las clases 
 * que juegan los roles de una manera más comoda, almacenandolos
 * en una lista.
 */
public class Rol {
	private String name="";
	private ArrayList listaJuegos = new ArrayList();
	private String op = null;
	private int qMax = -1;
	private int qMin = -1;
	private int qMaxInv = -1;
	private int qMinInv = -1;
	
	
	/**
	 * 
	 * @return String - El nombre del rol
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
	 * @return ArrayList - La lista de las clases que juegan el rol
	 */
	
	public ArrayList getListaJuegos(){
		return listaJuegos;
	}
	/**
	 * 
	 * @param l Actualiza el campo listaJuegos del objeto
	 */
	public void setListaJuegos(ArrayList l){
		listaJuegos = l;
	}
	
	
	
	public String getOp() {
		return op;
	}


	public void setOp(String op) {
		this.op = op;
	}

	
	

	public int getQMax() {
		return qMax;
	}


	public void setQMax(int max) {
		qMax = max;
	}


	public int getQMaxInv() {
		return qMaxInv;
	}


	public void setQMaxInv(int maxInv) {
		qMaxInv = maxInv;
	}


	public int getQMin() {
		return qMin;
	}


	public void setQMin(int min) {
		qMin = min;
	}


	public int getQMinInv() {
		return qMinInv;
	}


	public void setQMinInv(int minInv) {
		qMinInv = minInv;
	}


	/**
	 * 
	 * Método toString de la clase
	 */
	public String toString(){
		String s = "El rol "+ name +" juega un papel en las clases : ";
		Iterator it = listaJuegos.iterator();
		while(it.hasNext()){
			String s2 = (String) it.next();
			s += s2;
			s += " ";
		}
		return s;
	}

}
