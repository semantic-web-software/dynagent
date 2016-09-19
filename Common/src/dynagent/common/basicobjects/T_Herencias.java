/***
 * T_Herencias.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.common.basicobjects;

public class T_Herencias {

	private int ID_TO;
	private int ID_TO_Padre;
	public int getID_TO() {
		return ID_TO;
	}
	public void setID_TO(int id_to) {
		ID_TO = id_to;
	}
	public int getID_TO_Padre() {
		return ID_TO_Padre;
	}
	public void setID_TO_Padre(int padre) {
		ID_TO_Padre = padre;
	}
	
	public String toString(){
		return "IDTO: "+ID_TO+", IDTO PADRE: "+ID_TO_Padre;
	}
	
}
