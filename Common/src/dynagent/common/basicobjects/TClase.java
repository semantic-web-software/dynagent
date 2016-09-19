/***
 * TClase.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 * @description It represents an instance reg in database
 */

package dynagent.common.basicobjects;
import dynagent.common.knowledge.FactInstance;

public class TClase {
	
	private int IDTO;
	private String name;
	private boolean abstractClass;
	private Integer tableId;
	
	public TClase(){	
	}
	
	public TClase(int idto, String name, boolean abstractClass) {
		super();
		IDTO = idto;
		this.name = name;
		this.abstractClass = abstractClass;
	}
	
	public int getIDTO() {
		return IDTO;
	}
	public void setIDTO(int idto) {
		IDTO = idto;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
		
	public FactInstance toFact(){
		FactInstance f = new FactInstance();
		f.setIDTO(this.getIDTO());
		f.setCLASSNAME(this.getName());
		return f;
	}

	public boolean isAbstractClass() {
		return abstractClass;
	}

	public void setAbstractClass(boolean abstractClass) {
		this.abstractClass = abstractClass;
	}

	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}
	
}
