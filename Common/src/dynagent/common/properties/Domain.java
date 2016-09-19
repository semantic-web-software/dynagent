package dynagent.common.properties;

import dynagent.common.properties.values.ObjectValue;

/**
 * Clase genérica que contiene el identificador del objeto (IDO) y el identificador de la clase (IDTO).
 * Usado tras la llamada de "creaIndividualOfClass" de DocDataModel.
 * 
 * @author Darío
 */
public class Domain implements IDIndividual{

	/**
	 * Identificador del objeto.
	 */
	private Integer ido;
	/**
	 * Identificador de la clase.
	 */
	private Integer idto;
	
	/**
	 * Constructor por parámetros
	 * 
	 * @param ido Identificador del objeto.
	 * @param idto Identificador de la clase.
	 */
	public Domain(Integer ido, Integer idto){
		this.ido = ido;
		this.idto = idto;
	}
	
	public Domain(IDIndividual i){
		ido=i.getIDOIndividual();
		idto=i.getIDTOIndividual();
	}
	
	public Domain(ObjectValue ov){
		this.ido = ov.getValue();
		this.idto = ov.getValueCls();
	}
	
	public Integer getIdo() {
		return ido;
	}
	public void setIdo(Integer ido) {
		this.ido = ido;
	}
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	
	public String toString(){
		String result;
		result="\n    <DOMAIN IDO="+this.getIdo()+" IDTO="+this.getIdto()+"/>";
		return result;
	}

	public Integer getIDOIndividual() {
		return this.getIdo();
	}

	public Integer getIDTOIndividual() {
		return this.getIdto();
	}
	
}
