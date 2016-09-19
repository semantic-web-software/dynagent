package dynagent.ruleengine;
/**
 * Esta clase es un Singleton que se encarga de devolverte IDs nuevos sin que se pisen, <br>
 * para crear las properties virtuales.
 * <br><br>
 * Ejemplo de uso: CreateIdProp.getInstance.newIdProp();
 * <br><br>
 * @author Dynagent - David
 *
 */
public class CreateIdProp {
	/**
	 * Atributo estetico con la instancia de un objeto de dicha clase.
	 */
	private static CreateIdProp instance=null;
	/**
	 * Indice de ido, ira decrementandose con cada llamada.
	 */
	private Integer idProp=-1;
	/**
	 * Constructor vacio y privado, con el fin de que solo se pueda crear <br>
	 * una instancia de la Clase.
	 *
	 */
	private CreateIdProp(){
		
	}
	/**
	 * Metodo con el cual accedemos a la instancia de la clase, si esta no existe la crea<br>
	 * por primera vez.<br><br>
	 * @return Instancia unica de la clase.
	 */
	public static CreateIdProp getInstance(){
		if (instance==null)
			instance=new CreateIdProp();
		return instance;
	}
	/**
	 * Metodo que devuelve un ID para la Property Virtual
	 * @return Identificado nuevo.
	 */
	public synchronized Integer newIdProp(){
		this.idProp--;
		return idProp;
	}
}
