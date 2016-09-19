package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import java.util.Date;

import dynagent.common.knowledge.IKnowledgeBaseInfo;
import dynagent.common.properties.values.ObjectValue;

/**
 * Clase para representar informacion sobre articulo consumido en un proyecto (por compra en factura proveedor a un proyecto 
 * dado o por consumo interno que estar reflejado por su consumo en tareas.
 * @author Jose A. Zamora
 */


public class ArticuloProyecto extends DataRules{
	
		int IDOARTICULO=0;//identificador del articulo
		int IDTOARTICULO=0;//identificador del articulo		
		int proyecto=0;//identificador del articulo		
		Double cantidad=null;//cantidadad agregada del articulo
		Double precio=null;
		boolean INCURRIDAS=false;
		
		public ArticuloProyecto(int idoArticulo,int idtoArticulo, Double cantidad, Double precio, int proyecto,boolean incurrido, IKnowledgeBaseInfo ik) {
			super(ik);
			this.IDOARTICULO=idoArticulo;
			this.IDTOARTICULO=idtoArticulo;			
			this.cantidad=cantidad;
			this.precio=precio;
			this.proyecto=proyecto;
			this.INCURRIDAS=incurrido;
		}

		public int getIDOARTICULO() {
			return IDOARTICULO;
		}

		public int getIDTOARTICULO() {
			return IDTOARTICULO;
		}

		
		public int getPROYECTO() {
			return proyecto;
		}
		
		public boolean isINCURRIDO() {
			return INCURRIDAS;
		}

		public Object clone(IKnowledgeBaseInfo ik) {
			return new ArticuloProyecto(this.getIDOARTICULO(),this.getIDOARTICULO(),this.getCANTIDAD(),this.getPRECIO(), this.getPROYECTO(),this.isINCURRIDO(),ik);
		}

		public Double getCANTIDAD() {
			return cantidad;
		}

		public Double getPRECIO() {
			return precio;
		}
		
		public String toString(){
			return "(ArticuloProyecto proyecto="+proyecto+" idarticulo="+this.getIDOARTICULO()+" idtoarticulo:"+this.getIDTOARTICULO()+" cantidad:"+cantidad+" precio:"+precio+"  incurrido:"+this.isINCURRIDO()+" )"; 
		}

}
