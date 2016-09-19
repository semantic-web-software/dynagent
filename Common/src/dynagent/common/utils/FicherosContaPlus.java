package dynagent.common.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

/**
 * Clase que representa genericamente el formato con el que se exportan y crean los ficheros de exportación de 
 * contabilidad a ContaPlus.
 * 
 * @author Darío Veledo García.
 */
public class FicherosContaPlus {

	public HashMap<String, Campo> campos;
	public LinkedList<String> nombreCampos;
	
	/**
	 * Constructor principal de la clase.
	 */
	public FicherosContaPlus(){
		campos = new HashMap<String, Campo>();
		nombreCampos = new LinkedList<String>();
	}
	
	/**
	 * Esta función se encarga de formatear los objetos que se le pasan como parámetro: les da una longitud determinada y en caso
	 * de ser un numérico, le pone tantos decimales como se indique. Si es nulo, crea el objeto antes de formatearlo.
	 * 
	 * @param field Objeto de tipo String o Double que se formateará
	 * @param length Longitud final de la cadena de salida.
	 * @param numDecimales Entero que representa el numero de decimales que se quieren visualizar en caso de ser un Double.
	 * 						Este campo debe ser null en caso de invocar sobre un String (IMPORTANTE).
	 * @return Un String con la cadena formateada.
	 */
	public String fillWithSpaces(Object field, int length, Integer numDecimales){
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH); 
		DecimalFormat form = (DecimalFormat)nf; 
		String pattern = "";
		
		if(field == null){
			if(numDecimales != null){ //Es un double
				return fillWithSpaces(new Double(0.0), length, numDecimales);
			}else{ //Es un String
				return fillWithSpaces("", length, numDecimales);
			}
		}else if(field != null){
			if(field instanceof Double){
				if(numDecimales == null)
					numDecimales = 0;
				switch (numDecimales) {
					case 0:
						pattern = "0"; //Minimo un digito entero
						break;
					case 2:
						pattern = "0.00";
						break;
					case 6:
						pattern = "0.000000";
						break;
					default:
						System.err.println("Error al seleccionar el número de decimales");
						break;
				}
				
				form.applyPattern(pattern);
				return String.format("%1$" + length + "s", form.format(field));
			}else if(field instanceof String){
				if(((String) field).length()>length) return ((String)field).substring(0,length);
				return String.format("%1$-" + length + "s", field.toString());
			}else if(field instanceof Integer){
				return String.format("%1$" + length + "d", field);
			}
		}
		
		return null;
	}
	
	/**
	 * Inserta un nuevo campo que se definirá en el model de fichero que se está creando.
	 * 
	 * @param nombre Nombre del campo.
	 * @param longitud Longitud del campo.
	 * @param decimales Número de decimales del campo.
	 * @param requerido Indica si el campo es requerido o no.
	 * @param tipoCampo Tipo del campo: Double, Boolean, Integer...
	 */
	public void insertarCampo(String nombre, Integer longitud, Integer decimales, Boolean requerido, Class tipoCampo){
		nombreCampos.add(nombre);
		campos.put(nombre, new Campo(longitud, decimales, requerido, tipoCampo,nombre));
	}
	
	/**
	 * Modifica el valor asignado al campo.
	 * 
	 * @param nombre Nombre del camboi que se quiere modificar.
	 * @param value Nuevo valor.
	 */
	public void modificarCampo(String nombre, Object value){
		campos.get(nombre).setValor(value);
	}
	
	
	
	public String getValor(String nombreCampo){
		if(campos.get(nombreCampo)==null)
			return null;
		else
			return campos.get(nombreCampo).getValor();
	}
	
	/**
	 * Recorre la lista de campos y genera la línea.
	 * 
	 * @return Una cadena con la línea generada con los datos leidos desde los campos.
	 */
	public String generarLinea(){
		String linea = "", nombreCampo;

		for (Iterator it = nombreCampos.iterator(); it.hasNext();) {
			nombreCampo = (String)it.next();
			Campo c = campos.get(nombreCampo);

			try {
				if(c.valor != null){

					linea += c.valor;
				}else{
					if(c.tipoCampo == String.class){
						linea += fillWithSpaces(new String(""), c.longitud, null);
					}else if(c.tipoCampo == Double.class){
						linea += fillWithSpaces(new Double(0), c.longitud, c.decimales);
					}else if(c.tipoCampo == Boolean.class){
						linea += fillWithSpaces(new String("F"), c.longitud, null);
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
		return linea;
	}


	/**
	 * Clase auxiliar que representa a cada uno de los campos que componen la línea de un fichero de exportación
	 * para ContaPlus
	 * 
	 * @author Darío Veledo García.
	 */
	public class Campo{
		public Integer longitud;
		public Integer decimales;
		public boolean requerido;
		public String nombreCampo;
		public String valor;
		public Class tipoCampo;
		
		/**
		 * Constructor de la clase. Crea un nuevo campo.
		 * 
		 * @param longitud Longitud del campo.
		 * @param decimales Número de decimales del campo.
		 * @param requerido Indica si el campo es requerido o no.
		 * @param tipoCampo Tipo del campo: Double, Boolean, Integer...
		 */
		public Campo(Integer longitud, Integer decimales, Boolean requerido, Class tipoCampo,String nombreCampo){
			this.longitud = longitud;
			if(decimales != null)
				this.decimales = decimales;
			this.requerido = requerido;
			this.tipoCampo = tipoCampo;
			this.nombreCampo=nombreCampo;
		}
		
		/**
		 * Modifica el valor asignado al campo.
		 * 
		 * @param valor Nuevo valor.
		 */
		public void setValor(Object valor){
			this.valor = fillWithSpaces(valor, longitud, decimales);
		}		
		
		public String getValor(){
			return this.valor;
		}
	}
}

