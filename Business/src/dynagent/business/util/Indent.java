package dynagent.business.util;

/**
 * Clase que se va a encargar de almacenar el n�mero de tabulaciones que llevamos acumuladas
 * en cada momento mientras estamos escribiendo un fichero .xml.
 * 
 * Tener mucho cuidado de llamar a reset cada vez que se empieza a escribir en un nuevo fichero,
 * pues de lo contrario empezar�amos usando la �ltima tabulaci�n guardada para el fichero que ha
 * usado anteriormente esta clase.
 */
public class Indent {
	private static String indent = "";
	
	/**
	 * Resetea la sangr�a a la cadena vac�a, es decir, no se a�ade ning�n texto antes del texto
	 * que pongamos detr�s.
	 */
	public static void reset(){
		indent = "";
	}
	
	/**
	 * A�ade una tabulaci�n a la sangr�a.
	 */
	public static void increase(){
		indent += "\t";
	}
	
	/**
	 * Decrementa en una tabulaci�n la sangr�a. Si no hay tabulaci�n alguna almacenada, deja la
	 * cadena vac�a.
	 */
	public static void decrease(){
		indent = indent.replaceFirst("\t", "");
	}
	
	/**
	 * Nos da la sangr�a que se est� usando en ese momento.
	 * @return <code>String</code> que contiene la sangr�a que se est� usando.
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
