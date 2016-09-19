package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Date;

import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.values.ObjectValue;

/**
 * Clase para representar informacion sobre articulo consumido en un proyecto (por compra en factura proveedor a un proyecto 
 * dado o por consumo interno que estar reflejado por su consumo en tareas.
 * @author Jose A. Zamora
 */


public class VentasComercial extends DataRules{
	
		int IDOCOMISION=0;//identificador del articulo
		int IDTOCOMISION=0;//identificador del articulo		
		int idoagenteVenta=0;//identificador del agente
		int idoagenteCliente=0;//identificador del agente
		Double cantidad=null;//cantidadad agregada del articulo
		Double importeExclusiva=null;
		Double importeColaborador=null;
		Double importeBase=null;
		Date fechaMin=null;
		Date fechaMax=null;
		
		
		public Date getFECHAMIN() {
			return fechaMin;
		}

		
		public Date getFECHAMAX() {
			return fechaMax;
		}

		public VentasComercial(int idoComision,int idtoComision, Double cantidad, Double importeBase,Double importeColaborador, Double importeExclusiva, int idoagenteVenta, int idoagenteCliente,Date fechaMin, Date fechaMax, IKnowledgeBaseInfo ik) {
			super(ik);
			this.IDOCOMISION=idoComision;
			this.IDTOCOMISION=idtoComision;			
			this.cantidad=cantidad;
			this.importeExclusiva=importeExclusiva;
			this.importeColaborador=importeColaborador;
			this.idoagenteVenta=idoagenteVenta;
			this.idoagenteCliente=idoagenteCliente;
			this.fechaMax=fechaMax;
			this.fechaMin=fechaMin;
			this.importeBase=importeBase;
		}

		public int getIDOCOMISION() {
			return IDOCOMISION;
		}

		public int getIDTOCOMISION() {
			return IDTOCOMISION;
		}

		
		public int getIDOAGENTEVENTA() {
			return idoagenteVenta;
		}

		public int getIDOAGENTECLIENTE() {
			return idoagenteCliente;
		}

		public Object clone(IKnowledgeBaseInfo ik) {
			return new VentasComercial(this.getIDOCOMISION(),this.getIDTOCOMISION(),this.getCANTIDAD(),this.getIMPORTEBASE(),this.getIMPORTECOLABORADOR(), this.getIMPORTEEXCLUSIVA(),this.getIDOAGENTEVENTA(),this.getIDOAGENTECLIENTE(),this.getFECHAMIN(),this.getFECHAMAX(),ik);
		}

		public Double getCANTIDAD() {
			return cantidad;
		}

		public Double getIMPORTEEXCLUSIVA() {
			return importeExclusiva;
		}
		public Double getIMPORTEBASE() {
			return importeBase;
		}
		
		public Double getIMPORTECOLABORADOR() {
			return importeColaborador;
		}
		
		public String toString(){
			return "(ArticuloVendidoComercial idoagenteVenta="+this.getIDOAGENTEVENTA()+" idoagenteCliente="+this.getIDOAGENTECLIENTE()+" idarticulo="+this.getIDOCOMISION()+" idtoarticulo:"+this.getIDTOCOMISION()+" cantidad:"+cantidad+" importeExclusiva:"+importeExclusiva+" importeColaborador:"+importeColaborador+" )"; 
		}

}
