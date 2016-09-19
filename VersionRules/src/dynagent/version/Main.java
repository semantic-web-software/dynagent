package dynagent.version;

import java.io.File;
import java.util.LinkedList;

import dynagent.version.api.ProcessorInterface;
import dynagent.version.processor.ProcessorFactory;

public class Main {

	private static LinkedList<String> filesPaths;
	
	/**
	 * Hace una serie de comprobaciones sobre los datos de entrada antes de
	 * lanzar la ejecución del programa, tales como comprobar que se ha pasado
	 * la ruta del directorio sea accesible o descartar los ficheros excluidos.
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length <= 0){
			System.err.println("[VersionRules] Error: hay que indicar al menos un directorio de trabajo");
			return; //Mala invocación, se debe pasar almenos el directorio sobre el que se quiere trabajar
		}
		String dirPath = args[0];
		File dir = new File(dirPath);
		if (! dir.isDirectory()){
			System.err.println("[VersionRules] Error en la llamada al programa:");
			System.err.println("[VersionRules] \t\t" + dir.getAbsolutePath() + " no es un directorio");
			System.err.println("[VersionRules] \t\tForma de invocación: VersionRules Directorio_Trabajo [Fichero1_Excluido ... FicheroN_Excluido]");
			return; //Salimos si no es un directorio
		}
		
		LinkedList<String> excludedFiles = new LinkedList<String>();
		filesPaths = new LinkedList<String>();
		
		for (int i = 1 ; i < args.length ; i ++){
			excludedFiles.add(args[i]);
		}
		
		//Añadimos todos los ficheros que no se nos haya indicado que haya que excluir
		String [] fileList = dir.list();
		File file;
		for (String s : fileList){
			file = new File(dir, s);
			if (file.isFile() && ! excludedFiles.contains(s)){
				filesPaths.add(file.getAbsolutePath());
			}
		}
		
		//Lanzamos la ejecución del programa.
		proccess();
	}
	
	/**
	 * Se va a encargar de coordinar la ejecución de todos los pasos necesarios para
	 * poder alcanzar el resultado esperado.
	 */
	private static void proccess(){
		ProcessorInterface pi;
		for (String s : filesPaths){
			pi = ProcessorFactory.createProcessor();
			pi.proccessFile(s);
		}
	}

}
