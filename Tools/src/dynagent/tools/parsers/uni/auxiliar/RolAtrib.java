package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;

public class RolAtrib {
	
	private String nombreRol;
	private String relacionCont;
	private int qMax = -1;
	private int qMin = -1;
	private ArrayList nuevosJuegos = new ArrayList();
	private boolean full = false;
	private String op = null;
	private int qMaxInv = -1;
	private int qMinInv = -1;
	private String peer = null;
	
	public String getNombreRol(){
		return nombreRol;
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

	public String getRelacionCont(){
		return relacionCont;
	}
	
	public void setNombreRol(String s){
		nombreRol = s;
	}
	
	public void setRelacionCont(String s){
		relacionCont = s;
	}
		
	public ArrayList getNuevosJuegos() {
		return nuevosJuegos;
	}

	public void setNuevosJuegos(ArrayList nuevosJuegos) {
		this.nuevosJuegos = nuevosJuegos;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public int getQMaxInv() {
		return qMaxInv;
	}

	public void setQMaxInv(int maxInv) {
		qMaxInv = maxInv;
	}

	public int getQMinInv() {
		return qMinInv;
	}

	public void setQMinInv(int minInv) {
		qMinInv = minInv;
	}
	
	public String getPeer() {
		return peer;
	}

	public void setPeer(String peer) {
		this.peer = peer;
	}

	public boolean isFull() {
		return full;
	}

	public void setFull(boolean full) {
		this.full = full;
	}

	public String toString(){
		String s = "El rol "+ nombreRol + " pertenece a la clase " +relacionCont + nuevosJuegos + '\n';
		return s;
	}

	public boolean equals(RolAtrib r){
		return this.nombreRol.equals(r.nombreRol) && this.relacionCont.equals(r.relacionCont);
	}
}
