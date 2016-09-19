package dynagent.common.properties;

/**
 * Interfaz genérica que contiene el identificador del objeto (IDO) y el identificador de la clase (IDTO).
 */
public interface IDIndividual {

	
	public Integer getIDOIndividual();

	public Integer getIDTOIndividual();
	
	public String toString();//para obligar a sobreescribir toString de object
	
}
