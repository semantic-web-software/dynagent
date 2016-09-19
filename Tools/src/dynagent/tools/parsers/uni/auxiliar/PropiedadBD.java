package dynagent.tools.parsers.uni.auxiliar;

import java.util.ArrayList;


/**
 * 
 * @author alvarez
 * Usamos esta clase para mapear los datos de la tabla properties de la BD
 * En concreto el nombre de la propiedad, su identificador y su categoria.
 *
 */
public class PropiedadBD {
	
	private String name;
	private int idprop;
	private int cat;
	private String mask;
	private int longitud=-1;
	private String op = null;
	private int cls = -1;
	private ArrayList<Integer> rango = new ArrayList(); 
	private int qMax = -1;
	private int qMin = -1;
	private String value = null;
	private String propInv = null;
	
	
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getQMax() {
		return qMax;
	}

	public void setQMax(int ax) {
		qMax = ax;
	}

	public int getQMin() {
		return qMin;
	}

	public void setQMin(int min) {
		qMin = min;
	}

	public int getCls() {
		return cls;
	}

	public void setCls(int cls) {
		this.cls = cls;
	}

	public String getMask() {
		return mask;
	}

	public void setMask(String mask) {
		this.mask = mask;
	}

	public ArrayList getRango() {
		return rango;
	}

	public void setRango(ArrayList rango) {
		this.rango = rango;
	}

	/**
	 * Método get del atributo idprop
	 * @return El atributo idprop
	 */
	
	public int getIdProp(){
		return idprop;
	}
	
	/**
	 * Método get del atributo name
	 * @return El atributo name
	 */
	
	public String getName(){
		return name;
	}
	
	/**
	 * Método get del atributo tipo
	 * @return El atributo tipo
	 */
	
	public int getCat(){
		return cat;
	}
	
	
	/**
	 * Método set del atributo idto
	 * @param idto El entero con el que se actualizará el atributo
	 */
	
	public void setIdProp(int idto){
		this.idprop = idto;
	}
	
	/**
	 * Método set del atributo name
	 * @param name La cadena con la que se actualizará el atributo
	 */
	
	public void setName (String name){
		this.name = name;
	}
	
	/**
	 * Método set del atributo tipo
	 * @param tipo El entero con el quse actualizará en atributo
	 */
	
	public void setCat(int tipo){
		this.cat = tipo;
	}
	
	/**
	 * Método toString de la clase
	 */
	public String toString(){
		return "" + idprop + " : " + name +" clase: "+cls+ " mask: "+mask+" longitud "+longitud+" op: "+op+" categoria: "+cat+" value: "+value; 
	}

	public int getLongitud() {
		return longitud;
	}

	public void setLongitud(int longitud) {
		this.longitud = longitud;
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}
	
	public void addRango(Integer i){
		rango.add(i);
	}

	public String getPropInv() {
		return propInv;
	}

	public void setPropInv(String propInv) {
		this.propInv = propInv;
	}

	

	
}
