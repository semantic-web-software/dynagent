package dynagent.ruleengine.test.test21.src;

import java.util.ArrayList;
import java.util.Iterator;

public class Evento {
	
	/* Est�mulo de inter�s para los individuos del sistema. Los eventos pueden ser generados por la persona que usa el 
	 * sistema o eventos espont�neos generados autom�ticamente por la ocurrencia de otro evento o cambio de estado. 
	 * Los individuos reaccionan al evento en cuesti�n realizando una transici�n o cambio de estado.
	 * EVENTO asignaAgente(Inmueble, Agente, Vendedor)
	 * */
	
	private int ident;					// Identificador del evento
	private ArrayList<String> params; 	// Secuencia de par�metros del evento
	
	public Evento(int ident, ArrayList<String> params){
		this.ident=ident;
		this.params=params;
	}
	
	public int getident(){
		return ident;
	}
	
	public Iterator getList(){
		return params.iterator();
	}
}
