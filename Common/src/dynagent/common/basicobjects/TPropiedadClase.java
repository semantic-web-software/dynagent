/***
 * TClase.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 * @description It represents an instance reg in database
 */

package dynagent.common.basicobjects;

public class TPropiedadClase {
	
	private int claseId;
	private int propiedadId;
	private boolean isPropiedadDato;
	private Integer tableId;
	
	public TPropiedadClase(){	
	}
	
	public TPropiedadClase(int claseId,int propiedadId,boolean isPropiedadDato) {
		super();
		this.claseId=claseId;
		this.propiedadId=propiedadId;
		this.setPropiedadDato(isPropiedadDato);
	}
	
	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public int getClaseId() {
		return claseId;
	}

	public void setClaseId(int claseId) {
		this.claseId = claseId;
	}

	public int getPropiedadId() {
		return propiedadId;
	}

	public void setPropiedadId(int propiedadId) {
		this.propiedadId = propiedadId;
	}

	public void setPropiedadDato(boolean isPropiedadDato) {
		this.isPropiedadDato = isPropiedadDato;
	}

	public boolean isPropiedadDato() {
		return isPropiedadDato;
	}
	
}
