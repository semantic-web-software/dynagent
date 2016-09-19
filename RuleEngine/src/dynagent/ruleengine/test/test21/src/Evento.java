package dynagent.ruleengine.test.test21.src;

import java.util.ArrayList;
import java.util.Iterator;

public class Evento {
	
	/* Estímulo de interés para los individuos del sistema. Los eventos pueden ser generados por la persona que usa el 
	 * sistema o eventos espontáneos generados automáticamente por la ocurrencia de otro evento o cambio de estado. 
	 * Los individuos reaccionan al evento en cuestión realizando una transición o cambio de estado.
	 * EVENTO asignaAgente(Inmueble, Agente, Vendedor)
	 * */
	
	private int ident;					// Identificador del evento
	private ArrayList<String> params; 	// Secuencia de parámetros del evento
	
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
