package dynagent.common.utils;

import java.util.ArrayList;
import java.util.HashMap;

public interface IBatchListener {

	/**
	 * Asigna los individuos a tratar por el batch
	 * @param sources List<Mapa<Ido,Idto>> con los individuos a tratar en el batch. Agrupados en una lista para procesar conjuntamente el mapa que hay en una posicion de la lista
	 */
	public void setSources(ArrayList<HashMap<Integer, Integer>> sources);
	
	public ArrayList<HashMap<Integer, Integer>> getSources();

}
