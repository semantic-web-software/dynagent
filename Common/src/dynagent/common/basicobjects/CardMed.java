package dynagent.common.basicobjects;

public class CardMed {
	private Integer idtoParent=null;
	private Integer idto=null;
	private Integer idProp=null;
	private Integer cardmed=null;
	
	private String idtoParentName=null;
	private String idtoName=null;
	private String idPropName=null;

	
	public CardMed(Integer idtoParent, Integer idto, Integer idProp, Integer cardmed){
		this.idto=idto;
		this.idProp=idProp;
		this.idtoParent=idtoParent;
		this.cardmed=cardmed;
	}
	
	public CardMed(){
	
	}
	public Integer getCardmed() {
		return cardmed;
	}
	public void setCardmed(Integer cardmed) {
		this.cardmed = cardmed;
	}
	
	public Integer getIdto() {
		return idto;
	}
	public void setIdto(Integer idto) {
		this.idto = idto;
	}
	public String getIdtoName() {
		return idtoName;
	}
	public void setIdtoName(String idtoName) {
		this.idtoName = idtoName;
	}
	public Integer getIdtoParent() {
		return idtoParent;
	}
	public void setIdtoParent(Integer idtoParent) {
		this.idtoParent = idtoParent;
	}
	public String getIdtoParentName() {
		return idtoParentName;
	}
	public void setIdtoParentName(String idtoParentName) {
		this.idtoParentName = idtoParentName;
	}
	public String toString(){
		return "(CARDMED (IdtoP "+ this.idtoParent + ")(IdtoPN "+this.idtoParentName+")(IDTO "+this.idto+")(IDTON "+this.idtoName+")(PROP "+this.idProp+")(PROPN "+this.idPropName+")(CM "+this.cardmed+"))";
	}

	public void setIdProp(Integer idProp) {
		this.idProp = idProp;
	}

	public Integer getIdProp() {
		return idProp;
	}

	public void setIdPropName(String idPropName) {
		this.idPropName = idPropName;
	}

	public String getIdPropName() {
		return idPropName;
	}
}
