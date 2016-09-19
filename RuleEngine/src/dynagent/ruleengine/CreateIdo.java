package dynagent.ruleengine;
/**
 * Esta clase es un Singleton que se encarga de devolverte IDOs nuevos sin que se pisen, <br>
 * ya bien sea para Prototipo, Filtro y Virtual, ya que asigna IDOs negativos.
 * <br><br>
 * Ejemplo de uso: CreateIdo.getInstance.newIdo......();
 * <br><br>
 * @author Dynagent - David
 *
 */
public class CreateIdo {
	/**
	 * Atributo estetico con la instancia de un objeto de dicha clase.
	 */
	private static CreateIdo instance=null;
	/**
	 * Indice de ido, ira decrementandose con cada llamada.
	 */
	private Integer ido=-10000;
	/**
	 * Constructor vacio y privado, con el fin de que solo se pueda crear <br>
	 * una instancia de la Clase.
	 *
	 */
	private CreateIdo(){
		
	}
	/**
	 * Metodo con el cual accedemos a la instancia de la clase, si esta no existe la crea<br>
	 * por primera vez.<br><br>
	 * @return Instancia unica de la clase.
	 */
	public static CreateIdo getInstance(){
		if (instance==null)
			instance=new CreateIdo();
		return instance;
	}
	/**
	 * Metodo que devuelve un IDO para Prototipo
	 * @return Identificado nuevo.
	 */
	public synchronized Integer newIdoPrototype(){
		this.ido--;
		return ido;
	}
	/**
	 * Metodo que devuelve un IDO para Filtros
	 * @return Identificado nuevo.
	 */
	public synchronized Integer newIdoFilter(){
		this.ido--;
		return ido;
	}
	/**
	 * Metodo que devuelve un IDO para Virtual
	 * @return Identificado nuevo.
	 */
	public synchronized Integer newIdoVirtual(){
		this.ido--;
		return ido;
	}
	
}
