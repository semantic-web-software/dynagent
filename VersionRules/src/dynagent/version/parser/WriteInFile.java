package dynagent.version.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase que se va a ocupar de escribir el texto que nos pasen al final del fichero
 * indicado.
 *
 */
public class WriteInFile {

	/**
	 * Método para añadir texto al final de un fichero.
	 * <br>Si el fichero no exitiera, no se realizaría ninguna acción.
	 * 
	 * @param filePath Ruta del fichero sobre el que se quiere escribir
	 * @param text Texto a escribir
	 */
	public static void writeAtTheEnd(String filePath, String text){
		File file = new File(filePath);
		if (! file.exists()){
			return;
		}
		
		try {
			FileWriter fw = new FileWriter(filePath, true);
			fw.write("\n");
			fw.write(text);
			fw.flush();
		} catch (IOException e) {
			System.err.println("No se pudo encontrar el fichero" + filePath);
		}
		
	}
	
}
