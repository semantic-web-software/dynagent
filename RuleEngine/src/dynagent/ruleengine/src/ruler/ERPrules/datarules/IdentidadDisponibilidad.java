package dynagent.ruleengine.src.ruler.ERPrules.datarules;

public class IdentidadDisponibilidad {
	int idtoDispon=0;
	
	int idtoRecurso=0;	
	int recurso=0;
	
	int idtoProducto;	
	int producto=0;	
	
	int idtoTalla=0;
	int talla=0;
	
	int idtoColor=0;
	int color=0;
	
	int idtoFormato=0;
	int formato=0;
	
	int idtoLote=0;
	int lote=0;
		
	int idtoSerie=0;
	int serie=0;	
	
	String sRecurso;
	String sProducto;
	String sTalla;
	String sColor;
	String sFormato;
	String sLote;
	String sSerie;
	
	String code="";
	
	public int hashCode(){
		return code.hashCode();
	}
	
	public void detalle(){
		System.err.println(" "+idtoDispon+","+idtoRecurso+","+recurso+","+idtoProducto+","+producto+","+idtoTalla+","+talla+","+idtoColor
				+","+color+","+idtoFormato+","+formato+","+idtoLote+","+lote+","+idtoSerie+","+serie+","+sRecurso+","+sProducto+","+sTalla
				+","+sColor+","+sFormato+","+sLote+","+sSerie);
	}
		
	public void setProperty(  String prop, int valor, int idtoRango, String rdn){
		//System.err.println("Set identiad "+ prop+","+rdn);
		if(prop.equals("lote")) setLote(valor,idtoRango,rdn);			
		if(prop.equals("n._serie")) setSerie(valor,idtoRango,rdn);			
		if(prop.equals("talla")) setTalla(valor,idtoRango,rdn);						
		if(prop.equals("color")) setColor(valor,idtoRango,rdn);			
	//	if(prop.equals("producto"))	setProducto(valor,idtoRango,rdn);
		this.code=this.toString();
	}
	
	public boolean equals(Object obj){
		if( obj instanceof IdentidadDisponibilidad){
			IdentidadDisponibilidad ob2=(IdentidadDisponibilidad)obj;
			return 	this.producto==ob2.producto && 
					this.recurso==ob2.recurso &&
					this.lote==ob2.lote &&
					this.serie==ob2.serie&&
					this.formato==ob2.formato&&
					this.talla==ob2.talla&&
					this.color==ob2.color;
		}else
			return false;
	}
	
	public String toString(){
		String res=sRecurso;
		if(serie!=0)		res+="#" + sSerie;
		else if(lote!=0) 	res+="#" + sLote;
		else 				res+="#"+sProducto;
		
		if(talla!=0) 	res+="#" + sTalla;
		if(color!=0) 	res+="#" + sColor;
		if(formato!=0) 	res+="#" + sFormato;	
		//System.err.println("to string identidad "+ res);
		return res;		
	}
	
	public int getRecurso() {
		return recurso;
	}
	
	public void setRecurso(int recurso, int idtoRecurso, String rdn) {
		this.recurso = recurso;
		this.idtoRecurso=idtoRecurso;
		this.sRecurso= rdn;
	}
	public int getColor() {
		return color;
	}
	private void setColor(int color, int idtoColor, String rdn) {
		this.color = color;
		this.idtoColor=idtoColor;
		this.sColor=rdn;
	}
	public int getFormato() {
		return formato;
	}
	private void setFormato(int formato,int idtoFormato, String rdn) {
		this.formato = formato;
		this.idtoFormato=idtoFormato;
		this.sFormato=rdn;
	}
	public int getLote() {
		return lote;		
	}
	private void setLote(int lote, int idtoLote, String rdn) {
		this.lote = lote;
		this.idtoLote=idtoLote;
		this.sLote=rdn;		
	}
	public int getProducto() {
		return producto;
	}
	public void setProducto(int producto, int idto, String rdn) {
		this.producto = producto;
		this.sProducto=rdn;
		this.idtoProducto=idto;
	}
	public int getSerie() {
		return serie;
	}
	private void setSerie(int serie,int idtoSerie, String rdn) {
		this.serie = serie;
		this.idtoSerie=idtoSerie;
		this.sSerie=rdn;
	}
	public int getTalla() {
		return talla;
	}
	private void setTalla(int talla,int idtoTalla, String rdn) {
		this.talla = talla;
		this.idtoTalla=idtoTalla;
		this.sTalla=rdn;
	}

	public int getIdtoDispon() {
		return idtoDispon;
	}
	

	public void setIdtoDispon(int idtoDispon) {
		this.idtoDispon = idtoDispon;
	}

	public int getIdtoProducto() {
		return idtoProducto;
	}

	public int getIdtoRecurso() {
		return idtoRecurso;
	}

	private void setIdtoRecurso(int idtoRecurso) {
		this.idtoRecurso = idtoRecurso;
	}

	public int getIdtoColor() {
		return idtoColor;
	}

	private void setIdtoColor(int idtoColor) {
		this.idtoColor = idtoColor;
	}

	public int getIdtoFormato() {
		return idtoFormato;
	}

	private void setIdtoFormato(int idtoFormato) {
		this.idtoFormato = idtoFormato;
	}

	public int getIdtoLote() {
		return idtoLote;
	}

	private void setIdtoLote(int idtoLote) {
		this.idtoLote = idtoLote;
	}

	public int getIdtoSerie() {
		return idtoSerie;
	}

	private void setIdtoSerie(int idtoSerie) {
		this.idtoSerie = idtoSerie;
	}

	public int getIdtoTalla() {
		return idtoTalla;
	}

	private void setIdtoTalla(int idtoTalla) {
		this.idtoTalla = idtoTalla;
	}

	private void setIdtoProducto(int idtoProducto) {
		this.idtoProducto = idtoProducto;
	}

}
