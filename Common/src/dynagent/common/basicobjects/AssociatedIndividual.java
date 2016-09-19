package dynagent.common.basicobjects;

import java.util.ArrayList;

import dynagent.common.utils.Auxiliar;

public class AssociatedIndividual {

	private Integer idtoKey;
	private ArrayList<Integer> idtoSufix;
	private Integer associatedIdto;
	
	public AssociatedIndividual() {}
	
	public AssociatedIndividual(Integer idtoKey, ArrayList<Integer> idtoSufix, Integer associatedIdto) {
		this.idtoKey = idtoKey;
		this.idtoSufix = idtoSufix;
		this.associatedIdto = associatedIdto;
	}

	public Integer getAssociatedIdto() {
		return associatedIdto;
	}

	public void setAssociatedIdto(Integer associatedIdto) {
		this.associatedIdto = associatedIdto;
	}

	public Integer getIdtoKey() {
		return idtoKey;
	}

	public void setIdtoKey(Integer idtoKey) {
		this.idtoKey = idtoKey;
	}

	public ArrayList<Integer> getIdtoSufix() {
		return idtoSufix;
	}

	public void setIdtoSufix(ArrayList<Integer> idtoSufix) {
		this.idtoSufix = idtoSufix;
	}

	public String toString() {
		String dev = "idtoKey: " + idtoKey;
		dev += ", idtoSufix: " + Auxiliar.arrayIntegerToString(idtoSufix, ",");
		dev += ", associatedIdto: " + associatedIdto;
		return dev;
	}
	
}
