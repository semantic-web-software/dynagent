package dynagent.ruleengine.src.ruler.ERPrules.datarules;

import dynagent.common.utils.Auxiliar;

public class identidadStock {
		
	String medida1=null;
	String medida2=null;	
	
	String sAlmacen=null;
	String sProducto=null;
	String sTalla=null;
	String sColor=null;
	String sFormato=null;
	String sLote=null;
	String sSerie=null;
	
	String code="";
	
	public int hashCode(){
		return code.hashCode();
	}
	
	public void detalle(){
		System.err.println(" "+sAlmacen+","+sProducto+","+sTalla
				+","+sColor+","+sFormato+","+sLote+","+sSerie);
	}
		
	public void setProperty(  String prop, String rdn){
		//System.err.println("Set identiad "+ prop+","+rdn);
		if(prop.equals("lote")) setLote(rdn);			
		if(prop.equals("n._serie")) setSerie(rdn);			
		if(prop.equals("talla")) setTalla(rdn);
		if(prop.equals("medida1")) setMedida1(rdn);
		if(prop.equals("medida2")) setMedida2(rdn);		
		if(prop.equals("color")) setColor(rdn);			
	//	if(prop.equals("producto"))	setProducto(valor,idtoRango,rdn);
		this.code=this.toString();
	}
	
	public boolean equals(Object obj){
		if( obj instanceof identidadStock){
			identidadStock ob2=(identidadStock)obj;
			return 	Auxiliar.equals(this.sProducto,ob2.sProducto) && 
					Auxiliar.equals(this.sAlmacen,ob2.sAlmacen) &&
					Auxiliar.equals(this.sLote,ob2.sLote) &&
					Auxiliar.equals(this.sSerie,ob2.sSerie) &&
					Auxiliar.equals(this.sFormato,ob2.sFormato) &&
					Auxiliar.equals(this.sTalla,ob2.sTalla) &&
					Auxiliar.equals(this.medida1,ob2.medida1) &&
					Auxiliar.equals(this.medida2,ob2.medida2)&&
					Auxiliar.equals(this.sColor,ob2.sColor);
		}else
			return false;
	}
	
	public identidadStock clone(){
		identidadStock res=new identidadStock();
		res.setLote(sLote);			
		res.setSerie(sSerie);			
		res.setTalla(sTalla);
		res.setMedida1(medida1);
		res.setMedida2(medida2);		
		res.setColor(sColor);	
		res.setAlmacen(sAlmacen);
		res.setProducto(sProducto);
		return res;
	}
	
	public String toString(){
		String res="";
		if(sAlmacen!=null) res+=""+sAlmacen+'#';
		
		if(sSerie!=null)		res+= sSerie;
		else if(sLote!=null) 	res+= sLote;
		else{
			res+= sProducto;			
			if(medida1!=null) 	res+= "#A"+medida1;
			if(medida2!=null) 	res+= "#B"+medida2;
			if(sTalla!=null) 	res+= "#T"+sTalla;
			if(sColor!=null) 	res+= "#C"+sColor;
			if(sFormato!=null) 	res+= "#F"+sFormato;
		}
		//System.err.println("to string identiad "+ res);
		return res;		
	}
	
	public String getClaveProducto(){
		String res= sProducto;			
		if(medida1!=null) 	res+= "#A"+medida1;
		if(medida2!=null) 	res+= "#B"+medida2;
		if(sTalla!=null) 	res+= "#T"+sTalla;
		if(sColor!=null) 	res+= "#C"+sColor;
		if(sFormato!=null) 	res+= "#F"+sFormato;
				
		return res;	
	}
		
	public void setAlmacen(String rdn) {		
		this.sAlmacen= rdn;
	}
	private void setColor(String rdn) {
		this.sColor=rdn;
	}
	private void setFormato(String rdn) {
		this.sFormato=rdn;
	}
	private void setLote(String rdn) {
		this.sLote=rdn;		
	}
	public void setProducto(String rdn) {
		this.sProducto=rdn;
	}
	private void setSerie(String rdn) {		
		this.sSerie=rdn;
	}

	private void setTalla(String rdn) {		
		this.sTalla=rdn;
	}

	private void setMedida1(String medida1) {
		this.medida1=medida1;
	}
	private void setMedida2(String medida2) {
		this.medida2=medida2;
	}	
}
