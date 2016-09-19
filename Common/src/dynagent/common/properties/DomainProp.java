package dynagent.common.properties;

import org.jdom.Element;

public class DomainProp extends Domain {

	private int idProp;
	
	public DomainProp(Integer ido, Integer idto, Integer idProp) {
		super(ido, idto);
		this.idProp=idProp;
	}

	public int getIdProp() {
		return idProp;
	}

	public void setIdProp(int idProp) {
		this.idProp = idProp;
	}
	
	public String toString(){
		String result;
		result="\n    <DOMAIN IDO="+this.getIdo()+" IDTO="+this.getIdto()+" PROP="+this.getIdProp()+"/>";
		return result;
	}
	
	public Element toElement() {
		Element elem = new Element("DOMAIN_PROP");
		elem.setAttribute("IDO", String.valueOf(this.getIdo()));
		elem.setAttribute("IDTO", String.valueOf(this.getIdto()));
		elem.setAttribute("PROP", String.valueOf(this.getIdProp()));
		return elem;
	}
}
