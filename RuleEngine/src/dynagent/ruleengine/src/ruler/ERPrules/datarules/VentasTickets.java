package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Date;

import dynagent.common.knowledge.IKnowledgeBaseInfo;

public class VentasTickets extends DataRules{
		String NAMEQUERY=null;
		Date   FECHAMIN=null;
		Date   FECHAMAX=null;
		String TIENDA=null;
		String PRODUCTO=null;
		int VENTASACUMULADAS=0;
		int diasPrevios=0;
		Date dateQuery=null;

		public VentasTickets(String namequery, Date dateQuery,Date fechamin, Date fechamax,int diasPrevios, String tienda, String producto, int ventasacumuladas, IKnowledgeBaseInfo ik) {
			super(ik);
			NAMEQUERY = namequery;
			FECHAMIN = fechamin;
			FECHAMAX = fechamax;
			TIENDA = tienda;
			PRODUCTO = producto;
			VENTASACUMULADAS = ventasacumuladas;
			this.dateQuery = dateQuery;
			this.diasPrevios=diasPrevios;
		}

		public int getVENTASACUMULADAS() {
			return VENTASACUMULADAS;
		}

		public void setVENTASACUMULADAS(int VENTASACUMULADAS) {
			this.VENTASACUMULADAS = VENTASACUMULADAS;
		}

		public Date getFECHAMAX() {
			return FECHAMAX;
		}

		public void setFECHAMAX(Date FECHAMAX) {
			this.FECHAMAX = FECHAMAX;
		}

		public Date getFECHAMIN() {
			return FECHAMIN;
		}

		public void setFECHAMIN(Date FECHAMIN) {
			this.FECHAMIN = FECHAMIN;
		}

		public String getNAMEQUERY() {
			return NAMEQUERY;
		}

		public void setNAMEQUERY(String NAMEQUERY) {
			this.NAMEQUERY = NAMEQUERY;
		}

		public String getPRODUCTO() {
			return PRODUCTO;
		}

		public void setPRODUCTO(String PRODUCTO) {
			this.PRODUCTO = PRODUCTO;
		}

		public String getTIENDA() {
			return TIENDA;
		}

		public void setTIENDA(String TIENDA) {
			this.TIENDA = TIENDA;
		}

		public Date getDATEQUERY() {
			return dateQuery;
		}

		
		public int getDIASPREVIOS() {
			return diasPrevios;
		}

		public Object clone(IKnowledgeBaseInfo ik) {
			return new VentasTickets(getNAMEQUERY(), getDATEQUERY(), getFECHAMIN(), getFECHAMAX(),getDIASPREVIOS(), getTIENDA(), getPRODUCTO(), getVENTASACUMULADAS(), ik);
		}
}
