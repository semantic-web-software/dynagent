package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;

public class PropiedadClase {
	
	private String nombreProp;
	private ArrayList<String> rango = new ArrayList();
	private String mask = null;
	private ArrayList<String> categorias = new ArrayList();
	private int longitud=-1;
	private String op =null;
	private int qMax = -1;
	private int qMin = -1;
	private ArrayList enumerados = new ArrayList();
	private String propInv = null;
	
	
	public int getQMax() {
		return qMax;
	}
	public void setQMax(int max) {
		qMax = max;
	}
	public int getQMin() {
		return qMin;
	}
	public void setQMin(int min) {
		qMin = min;
	}
	public ArrayList<String> getCategorias() {
		return categorias;
	}
	public void setCategorias(ArrayList<String> categorias) {
		this.categorias = categorias;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public String getNombreProp() {
		return nombreProp;
	}
	public void setNombreProp(String nombreProp) {
		this.nombreProp = nombreProp;
	}
	public ArrayList<String> getRango() {
		return rango;
	}
	public void setRango(ArrayList<String> rango) {
		this.rango = rango;
	}
	
	
	
	public String getPropInv() {
		return propInv;
	}
	public void setPropInv(String propInv) {
		this.propInv = propInv;
	}
	public int getLongitud() {
		return longitud;
	}
	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}
	public void addCategoria (String s){
		categorias.add(s);
	}
	
	public void addRango (String s){
		rango.add(s);
	}
		
	public ArrayList getEnumerados() {
		return enumerados;
	}
	public void setEnumerados(ArrayList enumerados) {
		this.enumerados = enumerados;
	}
	
	public void addEnumerado(String s){
		enumerados.add(s);
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String toString(){
		return ""+nombreProp+", "+rango+","+categorias+", "+longitud+", "+mask+","+qMax+","+qMin;
	}
}
