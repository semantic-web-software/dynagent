package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.knowledge.IKnowledgeBaseInfo;


public class StockAlmacen extends DataRules{
		String almacen=null;
		String producto=null;
		Double cantidad=null;

		public StockAlmacen(String almacen,String producto,Double cantidad,IKnowledgeBaseInfo ik){
			super(ik);
			this.almacen = almacen;
			this.producto=producto;
			this.cantidad=cantidad;
		}

		public String getALMACEN() {
			return almacen;
		}

		public void setAlmacen(String almacen) {
			this.almacen = almacen;
		}

		public Double getCANTIDAD() {
			return cantidad;
		}

		public void setCantidad(Double cantidad) {
			this.cantidad = cantidad;
		}

		public String getPRODUCTO() {
			return producto;
		}

		public void setProducto(String producto) {
			this.producto = producto;
		}
		
		public Object clone(IKnowledgeBaseInfo ik) {
			return new StockAlmacen(almacen,producto,cantidad,ik);
		}
}
