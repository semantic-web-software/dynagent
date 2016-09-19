package dynagent.business.util;

/**
 * Clase que se va a encargar de almacenar el número de tabulaciones que llevamos acumuladas
 * en cada momento mientras estamos escribiendo un fichero .xml.
 * 
 * Tener mucho cuidado de llamar a reset cada vez que se empieza a escribir en un nuevo fichero,
 * pues de lo contrario empezaríamos usando la última tabulación guardada para el fichero que ha
 * usado anteriormente esta clase.
 */
public class Indent {
	private static String indent = "";
	
	/**
	 * Resetea la sangría a la cadena vacía, es decir, no se añade ningún texto antes del texto
	 * que pongamos detrás.
	 */
	public static void reset(){
		indent = "";
	}
	
	/**
	 * Añade una tabulación a la sangría.
	 */
	public static void increase(){
		indent += "\t";
	}
	
	/**
	 * Decrementa en una tabulación la sangría. Si no hay tabulación alguna almacenada, deja la
	 * cadena vacía.
	 */
	public static void decrease(){
		indent = indent.replaceFirst("\t", "");
	}
	
	/**
	 * Nos da la sangría que se está usando en ese momento.
	 * @return <code>String</code> que contiene la sangría que se está usando.
	 */
	public static String getIndent(){
		return indent;
	}
	
	public static void setIdent(int tabNumber){
		reset();
		for(int i = 0 ; i < tabNumber ; i ++){
			indent += "\t";
		}
	}

}
