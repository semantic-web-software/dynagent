package dynagent.business.parser;

import java.io.File;
import java.io.IOException;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

/**
 * Clase que se encarga de modificar los HTML plantilla para insertar los parámetros correctos.
 * 
 * @author David Harillo Sánchez
 *
 */
public class HtmlTextFileBuilder extends TextFileParser {
	
	private File sourceFile;
	private File destinationFile;
	
	/**
	 * Único constructor de la clase.
	 * 
	 * @param pageName Solo el nombre de la página HTML, por ejemplo <i>index.html</i>
	 */
	public HtmlTextFileBuilder (String pageName){
		sourceFile = new File (Constants.ROOT, "webPages/" + pageName);
		destinationFile = new File(Constants.ROOT, "temp/" + pageName);
	}
	
	public void buildFile(){
		if (sourceFile == null || destinationFile == null){
			stop = true;
			System.err.println("[Business] Error: No se han creado los ficheros.");
			return;
		}
		
		if (! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			stop = true;
			System.err.println("[Business] Error al intentar abrir los ficheros HTML");
		}
		
		if (writer == null || reader == null){
			System.err.println("[Business] Error: No se han abierto correctamente los ficheros HTML para su lectura/escritura.");
			stop = true;
			return;
		}
		
		Indent.reset();
		String line = readLine();
		while(line != null && ! stop){
			if (line.contains("<!-- Insertar parametros -->")){
				writeParams();
			}else{
				try {
					writer.write(line);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo fichero HTML");
					close();
					stop = true;
					e.printStackTrace();
				}
			}
			line = readLine();
		}
		
		close();
	}
	
	private void writeParams() {
		Indent.setIdent(3);
		try {
			writeLine("<param name=\"hideHistoryDDBB\" value=\"" + Constants.HIDEHISTORYDDBB + "\">");
			writeLine("<param name=\"licenseMode\" value=\"" + Constants.LICENSE_CODE + "\">");
			boolean first = true;
			for (Integer bns : Constants.DATABASES) {
				if (first){
					first = false;
					writeLine("<param name=\"bns\" value=\"" + bns + "\">");
					continue;
				}
				writeLine("<!-- <par am name=\"bns\" value=\"" + bns + "\"> -->");
			}
			writeLine("<param name=\"rules\" value=\"" + Constants.RULES + "\">");
			if (Constants.USER != null){
				writeLine("<param name=\"login\" value=\"" + Constants.USER + "\">");
			}
			if (Constants.PASSWORD != null){
				writeLine("<param name=\"password\" value=\"" + Constants.PASSWORD + "\">");
			}
			
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar copiar una línea al nuevo fichero HTML");
			e.printStackTrace();
		}
	}

	private String readLine(){
		if (reader == null){
			stop = true;
			System.err.println("[Business] Error: No se ha abierto correctamente el lector del archivo HTML.");
			return null;
		}
		
		String line = null;
		try {
			 line = reader.readLine();
		} catch (IOException e) {
			System.err.println("[Business] Error: Se ha producido un error durante la lectura del fichero HTML");
			e.printStackTrace();
			stop = true;
		}
		
		return line;
	}
}
