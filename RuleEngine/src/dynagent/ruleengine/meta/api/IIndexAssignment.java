package dynagent.ruleengine.meta.api;

public interface IIndexAssignment {
	/**
	 * Obtiene el proximo identificador numerico para una instant del tipo indicado en level.
	 * @param level: entero que representa el nivel del que se quiere crear un objeto: 0 model;1 filter;2prototype;3individual
	 * @return: el identificador.
	 */
	public int newIdo(int level);
}
