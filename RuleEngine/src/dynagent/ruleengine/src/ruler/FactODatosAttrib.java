/***
 * FactProp.java
 * @author: Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.ruleengine.src.ruler;

import dynagent.ruleengine.src.data.dao.O_Datos_Attrib;

public class FactODatosAttrib extends Fact{
	
	private Integer fID_TO;
	private Integer fROL;
	private Integer fID_O;
	private Integer fPROPERTY;
	private Integer fVAL_NUM;
	private String fVAL_TEXTO;
	private Integer fVALUE_CLS;
	private Integer fVALUE_ROL;
	private Integer fCLS_REL;
	private Integer fIDO_REL;
	private Integer fROL_PEER;
	private Double fQ_MIN;
	private Double fQ_MAX;
	
	
	
	public Integer getFCLS_REL() {
		return fCLS_REL;
	}



	public void setFCLS_REL(Integer fcls_rel) {
		fCLS_REL = fcls_rel;
	}



	public Integer getFID_O() {
		return fID_O;
	}



	public void setFID_O(Integer fid_o) {
		fID_O = fid_o;
	}



	public Integer getFID_TO() {
		return fID_TO;
	}



	public void setFID_TO(Integer fid_to) {
		fID_TO = fid_to;
	}



	public Integer getFIDO_REL() {
		return fIDO_REL;
	}



	public void setFIDO_REL(Integer fido_rel) {
		fIDO_REL = fido_rel;
	}



	public Integer getFPROPERTY() {
		return fPROPERTY;
	}



	public void setFPROPERTY(Integer fproperty) {
		fPROPERTY = fproperty;
	}



	public Double getFQ_MAX() {
		return fQ_MAX;
	}



	public void setFQ_MAX(Double fq_max) {
		fQ_MAX = fq_max;
	}



	public Double getFQ_MIN() {
		return fQ_MIN;
	}



	public void setFQ_MIN(Double fq_min) {
		fQ_MIN = fq_min;
	}



	public Integer getFROL() {
		return fROL;
	}



	public void setFROL(Integer frol) {
		fROL = frol;
	}



	public Integer getFROL_PEER() {
		return fROL_PEER;
	}



	public void setFROL_PEER(Integer frol_peer) {
		fROL_PEER = frol_peer;
	}



	public Integer getFVAL_NUM() {
		return fVAL_NUM;
	}



	public void setFVAL_NUM(Integer fval_num) {
		fVAL_NUM = fval_num;
	}



	public String getFVAL_TEXTO() {
		return fVAL_TEXTO;
	}



	public void setFVAL_TEXTO(String fval_texto) {
		fVAL_TEXTO = fval_texto;
	}



	public Integer getFVALUE_CLS() {
		return fVALUE_CLS;
	}



	public void setFVALUE_CLS(Integer fvalue_cls) {
		fVALUE_CLS = fvalue_cls;
	}



	public Integer getFVALUE_ROL() {
		return fVALUE_ROL;
	}



	public void setFVALUE_ROL(Integer fvalue_rol) {
		fVALUE_ROL = fvalue_rol;
	}



	public O_Datos_Attrib toO_Datos_Attrib(){
		O_Datos_Attrib o = new O_Datos_Attrib();
		o.setIDO(this.getFID_O());
		o.setIDTO(this.getFID_TO());
		o.setPROPERTY(this.getFPROPERTY());
		o.setQMAX(this.getFQ_MAX());
		o.setQMIN(this.getFQ_MIN());
		o.setVALNUM(this.getFVAL_NUM());
		o.setVALTEXTO(this.getFVAL_TEXTO());
		o.setVALUECLS(this.getFVALUE_CLS());
		return o;
	}
}
