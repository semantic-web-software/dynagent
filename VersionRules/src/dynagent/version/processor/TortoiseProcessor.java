package dynagent.version.processor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import dynagent.version.api.ProcessorInterface;
import dynagent.version.parser.ResultParser;
import dynagent.version.parser.WriteInFile;
import dynagent.version.util.FileCopy;

/**
 * Clase que implementa el procesamiento de los ficheros de reglas basándose
 * en el cliente de SVN TortoiseSVN.
 * 
 * @since 29 de abril de 2010
 */
public class TortoiseProcessor implements ProcessorInterface {

	private File originalFile;
	private File copiedFile;
	private File temporalDirectory;
	
	public void proccessFile(String filePath) {
		originalFile = new File(filePath);
		if (! originalFile.isFile()){
			System.err.println("[VersionRules] Error en el procesamiento:");
			System.err.println("[VersionRules] \t\t" + filePath + " no es un fichero");
			return;
		}
		
		//Solo se van a procesar aquellos ficheros que sean de reglas, el resto no.
		if (! originalFile.getName().endsWith(".drl")){
			return;
		}
		
		createTemporalDirectory();
		//Si no conseguimos copiar el fichero, paramos la ejecución.
		if (! copyFile()){
			return;
		}
		
		addVersion();

	}

	/**
	 * Copia el fichero original al directorio temporal 
	 * @return <code>true</code> si se ha tenido éxito copiando el fichero, <code>false</code> en caso contrario.
	 */
	private boolean copyFile() {
		copiedFile = new File(temporalDirectory.getAbsolutePath() + "\\" + originalFile.getName());
		try {
			FileCopy.copy(originalFile.getAbsolutePath(), copiedFile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("[VersionRules] Error al intentar copiar el fichero " + originalFile.getAbsolutePath() + " al directorio temporal.");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Crea el directorio temporal donde se copian los ficheros en caso
	 * de que este no haya sido creado ya.
	 */
	private void createTemporalDirectory(){
		if (temporalDirectory != null){
			return;
		}
		temporalDirectory = new File(originalFile.getParentFile(), "temp");
		temporalDirectory.mkdir();
		
	}
	
	/**
	 * Comprueba la revisión a la que hace referencia el fichero indicado
	 * y añade la regla que la mostrará y nos dirá si el archivo ha sido modificado
	 * localmente. La regla será añadida al final del fichero.
	 * <br>Si se produce algún error, no se modificará el fichero.
	 */
	private void addVersion(){
		String rule = null;
		try {
			Process proc = Runtime.getRuntime().exec("SubWCRev " + originalFile.getAbsolutePath());
			InputStream is = proc.getInputStream();
			ResultParser result = new ResultParser(is);
			if (result.getParsed()){
				rule = createRule(result.getVersion(), result.getModified());
			}
		} catch (IOException e) {
			System.err.println("[VersionRules] Error al intentar comprobar la versión del fichero " + originalFile.getAbsolutePath());
			e.printStackTrace();
			return;
		}
		if(rule != null){
			WriteInFile.writeAtTheEnd(copiedFile.getAbsolutePath(), rule);
		}
	}
	
	/**
	 * Construye la regla que será incluida en el fichero de reglas.
	 * @param version
	 * @param modified
	 * @return
	 */
	private String createRule(String version, boolean modified){
		String fileName = originalFile.getName().replaceFirst(".drl", "");
		String versionRule = "\n\n" +
							 "rule \"VERSION " + fileName + "\"\n" +
							 "salience 100\n" +
							 "agenda-group \"advancedconfiguration\" when\n" +
							 "\tjb:JBossEngine()\n" +
							 "then\n" +
							 "\tSystem.err.println(\"" + fileName + " version: " + version + "; modified: " + modified + "\");\n" +
							 "end";
		return versionRule;
	}

}
