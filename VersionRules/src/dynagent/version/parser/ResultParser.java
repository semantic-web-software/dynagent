package dynagent.version.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Clase que se va a encargar de obtener la información necesaria de los ficheros
 * resultantes de analizar el contenido de los archivos que tenemos descargados en
 * nuestro Workspace.
 * 
 * Esta clase está muy ligada a la forma en que tiene de devolver la información el 
 * programa TortoiseSVN y puede requerir adaptaciones posteriores.
 */
public class ResultParser {
	
	private boolean parsed = false;
	private boolean modified;
	private String version;

	/**
	 * Analiza el fichero en la ruta especificada
	 * @param filePath fichero a analizar
	 */
	public ResultParser(String filePath){
		File file = new File(filePath);
		if (! file.exists()){
			return;
		}
		obtainReader(file);
	}
	
	public ResultParser(InputStream is){
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		parsed = true;
		parseReader(br);
	}
	
	/**
	 * Nos dirá si se ha realizado correctamente el parseo del fichero
	 * @return <code>true</code> si se ha parseado la información del fichero.
	 */
	public boolean getParsed(){
		return parsed;
	}
	/**
	 * Nos indica la revisión del fichero analizado.
	 * @return Cadena con el id de la revisión del fichero.
	 */
	public String getVersion(){
		return version;
	}
	
	/**
	 * Sirve para saber si el fichero ha sido modificado localmente.
	 * 
	 * @return <code>true</code> si el archivo ha sido modificado con respecto a la versión
	 * descargada desde el repositorio
	 */
	public boolean getModified(){
		return modified;
	}
	
	/**
	 * Se encarga de hacer de puente entre un fichero y la función de análisis
	 * de resultados principal <code>parseReader(BufferedReader br)</code>
	 * @param file Fichero a partir del cual se creará el Reader
	 */
	private void obtainReader(File file){
		FileReader fr;
		try {
			fr = new FileReader(file);
		} catch (FileNotFoundException e) {
			parsed = false;
			return;
		}
		
		BufferedReader br;
		
		br = new BufferedReader(fr);
		parseReader(br);
		
	}
	
	private void parseReader(BufferedReader br){
		String line;
		
		try {
			line = br.readLine();
		} catch (IOException e) {
			System.err.println("Error al leer el fichero de resultados de TortoiseSVN");
			parsed = false;
			return;
		}
		
		while(line != null){
			if (line.contains("Last committed")){
				String [] params = line.split(" ");
				version = params[params.length-1];
			}else if(line.contains("Local modifications found")){
				modified = true;
			}
			try {
				line = br.readLine();
			} catch (IOException e) {
				System.err.println("Error al leer el fichero de resultados de TortoiseSVN");
				parsed = false;
				return;
			}
		}
		
	}
}
