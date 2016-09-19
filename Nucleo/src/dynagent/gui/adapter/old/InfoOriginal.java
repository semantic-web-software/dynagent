package dynagent.gui.adapter.old;
/**
 * Clase en la cual se va a almacenar la información de las properties originales.
 * @author Dynagent - David
 *
 */
public class InfoOriginal {
	/**
	 * Identificador de la property original.
	 */
	
	private Integer idProp;
	/**
	 * Identificador de el individuo original.
	 */
	private Integer idoClass;
	/**
	 * Identificador de la clase original.
	 */
	private Integer idtoClass;
	/**
	 * Constructor con parámetos.<br><br>
	 * 
	 * @param idProp Identificador de la property original.
	 * @param idoClass Identificador del individuo original.
	 * @param idtoClass Identificador de la clase a la que pertenece el individuo original.
	 */
	private String type;
	public InfoOriginal(Integer idProp, Integer idoClass, Integer idtoClass) {
		this.idProp = idProp;
		this.idoClass = idoClass;
		this.idtoClass = idtoClass;
		this.type="NULL";
	}
	/**
	 * Método get sobre el ido original.
	 * @return Ido original.
	 */
	public Integer getIdoClass() {
		return idoClass;
	}
	/**
	 * Método set sobre el ido original.
	 * 
	 * @param idoClass Ido original.
	 */
	public void setIdoClass(Integer idoClass) {
		this.idoClass = idoClass;
	}
	/**
	 * Método get sobre el idProp original.
	 * @return IdProp original.
	 */
	public Integer getIdProp() {
		return idProp;
	}
	/**
	 * Método set sobre el idProp original.
	 * @param idProp idProp original.
	 */
	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}
	/**
	 * Método get sobre el idto original.
	 * @return Idto Original.
	 */
	public Integer getIdtoClass() {
		return idtoClass;
	}
	/**
	 * Método set sobre el idto original.
	 * @param idtoClass IdtoOriginal.
	 */
	public void setIdtoClass(Integer idtoClass) {
		this.idtoClass = idtoClass;
	}
	public boolean equals(Object o){
		if(this.idoClass.equals(((InfoOriginal)o).getIdoClass()) && this.idProp.equals(((InfoOriginal)o).getIdProp()) && this.idtoClass.equals(((InfoOriginal)o).getIdtoClass())){
			return true;
		}else{
			return false;
		}
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
