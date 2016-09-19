package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;

public class PropiedadAtrib {
	
	private String nombreProp = null;
	private String claseCont = null;
	private ArrayList<String> restricciones = new ArrayList();
	private ArrayList<String> enumerados = new ArrayList();
	private int qMin=-1;
	private int qMax=-1;
	private String op = null;
	
	
	
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getNombreProp() {
		return nombreProp;
	}
	public void setNombreProp(String nombreProp) {
		this.nombreProp = nombreProp;
	}
	public String getClaseCont() {
		return claseCont;
	}
	public void setClaseCont(String claseCont) {
		this.claseCont = claseCont;
	}
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
	public ArrayList<String> getRestricciones() {
		return restricciones;
	}
	public void setRestricciones(ArrayList<String> restricciones) {
		this.restricciones = restricciones;
	}

	public void addPropAtrib (String s){
		restricciones.add(s);
	}
	
	public void addEnum (String s){
		enumerados.add(s);
	}
	
	public String toString(){
		return "PROP:"+nombreProp+" ,CLASECONT:"+claseCont;
	}
	public ArrayList<String> getEnumerados() {
		return enumerados;
	}
	public void setEnumerados(ArrayList<String> enumerados) {
		this.enumerados = enumerados;
	}
	
	public boolean equals(PropiedadAtrib pat){
		boolean res = (this.nombreProp.equals(pat.nombreProp) && this.claseCont.equals(pat.claseCont)); 
		return res;
	}
}
