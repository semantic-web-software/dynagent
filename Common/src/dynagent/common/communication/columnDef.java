package dynagent.common.communication;

/**
 * Esta clase se encarga de la definición de las columnas.
 * <br>Las columnas sirven para registrar una instancia, donde los campos que tienen son:
 * identificador de objeto, clase, property, tipo, si es agregado y nombre de columna
 */

public class columnDef {
	private String id;  //en este id para los IDO se guarda su ID, en los ATTRIBUTE y WHERE lo que se guarda es el ID del nodo superior
	private String idFilter;
	//private int to;
    private int prop;
    private int tm;
    private Integer propF;
    private String valueF;
    private String name;
	private boolean rdnTmpNoSQ;
	
	public columnDef() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getIdFilter() {
		return idFilter;
	}
	public void setIdFilter(String idFilter) {
		this.idFilter = idFilter;
	}

	public int getProp() {
		return prop;
	}
	public void setProp(int prop) {
		this.prop = prop;
	}

	public int getTm() {
		return tm;
	}
	public void setTm(int tm) {
		this.tm = tm;
	}

	/*public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}*/

	public Integer getPropF() {
		return propF;
	}
	public void setPropF(Integer propF) {
		this.propF = propF;
	}

	public String getValueF() {
		return valueF;
	}
	public void setValueF(String valueF) {
		this.valueF = valueF;
	}

	public boolean isRdnTmpNoSQ() {
		return rdnTmpNoSQ;
	}
	public void setRdnTmpNoSQ(boolean rdnTmpNoSQ) {
		this.rdnTmpNoSQ = rdnTmpNoSQ;
	}

	public String toString() {
        return " ID " + id + ", IDFILTER " + idFilter + /*", TO " + to + */", PROP " + prop + ", TM " + tm + ", propF " + propF + ", valueF " + valueF + ", rdnTmpNoSQ " + rdnTmpNoSQ;
    }
}
