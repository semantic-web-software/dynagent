package dynagent.version.api;

public interface ProcessorInterface {

	/**
	 * Se encarga de procesar el fichero especificado, de tal manera que,
	 * si es de reglas, lo que har� ser� insertar una regla m�s al final del
	 * fichero que nos permitir� saber la revisi�n de las reglas sobre la que
	 * estamos trabjando, y si dicho fichero de reglas ha sido modificado
	 * localmente. Es decir, si dicho fichero de reglas est� basado en la revisi�n
	 * indicado, pero ha sufrido alguna modificaci�n que no consta en el servidor
	 * de SVN.
	 * <br> Para que el cambio sea transparente al usuario, crearemos una carpeta
	 * temporal bajo el directorio en el que se encuentra el fichero en la que copiaremos
	 * los ficheros y realizaremos las modificaciones.
	 * @param filePath ruta completa al fichero sobre el que queremos trabajar.
	 */
	public void proccessFile(String filePath);
	
}
