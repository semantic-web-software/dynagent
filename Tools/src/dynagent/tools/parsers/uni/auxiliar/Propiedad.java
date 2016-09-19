package dynagent.tools.parsers.uni.auxiliar;
import java.util.Iterator;

/**
 * 
 * @author alvarez
 * Usamos esta clase para almacenar los datos importantes de una propiedad
 * declarada en un fichero dynagent. En concreto (atributos): su nombre, su tipo,
 * la clase a la que pertenece, y la cardinalidad m�xima y minima
 *
 */
public class Propiedad {
	
	private String nombreProp=null;
	private String tipoPropiedad=null;
	private String claseCont=null;
	private int qMax=-1;
	private int qMin=-1;
	private String mask = null;
	
	/**
	 * M�todo get del atributo nombreProp
	 * @return El atributo nombreProp
	 */
	
	public String getNombreProp(){
		return nombreProp;
	}
	
	/**
	 * M�todo get del atributo tipoPropiedad
	 * @return El atributo tipoPropiedad
	 */
	
	public String getTipoPropiedad(){
		return tipoPropiedad;
	}
	
	/**
	 * M�todo get del atributo claseCont
	 * @return El atributo claseCont
	 */
	
	
	public String getClaseCont(){
		return claseCont;
	}
	
	/**
	 * M�todo get del atributo qMAx
	 * @return El atributo qMax
	 */
	
	public int getQMax(){
		return qMax;
	}
	
	/**
	 * M�todo get del atributo qMin
	 * @return El atributo qMin
	 */
	
	
	public int getQMin(){
		return qMin;
	}
	
	/**
	 * M�todo get del atributo mask
	 * @return El atributo mask
	 */
	public String getMask(){
		return mask;
	}
	
	
	
	/**
	 * M�todo set del atributo nombreProp
	 * @param s La cadena con la que se actualizara el atributo 
	 */
	
	public void setNombreProp(String s){
		nombreProp=s;
	}
	
	/**
	 * M�todo set del atributo tipoPropiedad
	 * @param s La cadena con la que se actualizara el atributo 
	 */
	
	public void setTipoPropiedad(String s){
		tipoPropiedad=s;
	}
	
	/**
	 * M�todo set del atributo claseCont
	 * @param s La cadena con la que se actualizara el atributo 
	 */
	
	public void setClaseCont(String s){
		claseCont=s;
	}
	
	/**
	 * M�todo set del atributo qMax
	 * @param n El entero con el que se actualizar� el fichero
	 */	
	
	public void setQMax(int n){
		qMax = n;
	}
	
	/**
	 * M�todo set del atributo qMin
	 * @param n El entero con el que se actualizar� el fichero
	 */	
	
	public void setQMin(int n){
		qMin = n;
	}
	
	public void setMask(String s){
		mask = s;
	}
	
	/**
	 *  M�todo toString de la clase
	 */
	
	public String toString(){
		String s = "La propiedad "+ nombreProp +" es de tipo " + tipoPropiedad + " y pertenece a la clase " + claseCont;
		return s;
	}

}
