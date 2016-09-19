package dynagent.gui.adapter.old;

public class DataCase {
	private Integer idoClass;
	private Integer idoRel;
	private Integer idoPeer;
	public DataCase(Integer idoClass,Integer idoRel,Integer idoPeer){
		this.idoClass=idoClass;
		this.idoRel=idoRel;
		this.idoPeer=idoPeer;
	}
	public Integer getIdoClass() {
		return idoClass;
	}
	public void setIdoClass(Integer idoClass) {
		this.idoClass = idoClass;
	}
	public Integer getIdoPeer() {
		return idoPeer;
	}
	public void setIdoPeer(Integer idoPeer) {
		this.idoPeer = idoPeer;
	}
	
	public Integer getIdoRel() {
		return idoRel;
	}
	public void setIdoRel(Integer idoRel) {
		this.idoRel = idoRel;
	}
	
	public String toString(){
		String result;
		result="IDOCLASS= "+idoClass+" IDOREL= "+idoRel+" IDOPEER= "+idoPeer;
		return result;
	}

}