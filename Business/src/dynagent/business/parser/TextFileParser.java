package dynagent.business.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import dynagent.business.util.Constants;
import dynagent.business.util.FileCopy;
import dynagent.business.util.Indent;
import dynagent.business.util.Zip;

public class TextFileParser extends FileParser {

	protected BufferedWriter writer;
	protected BufferedReader reader;
	protected boolean stop = false;
	
	/**
	 * Metodo que se va a encargar de orquestar el desempaquetado y empaquetado de los ficheros
	 * así como de modificar los ficheros que haya que modificar.
	 */
	@Override
	public boolean buildFiles() {
		generateAuxiliarFiles();
		
		// Comprobamos que no se haya producido ningún error al generar los ficheros.
		if (stop){
			return false;
		}
		
		/*
		 * Cambiamos el fichero mssql-ds.xml que se encuentra en:
		 * 		{JBoss_Dir}/server/default/deploy
		 */
		
		try {
			FileCopy.copy(new File(Constants.ROOT,"temp/mssql-ds.xml").getAbsolutePath(), new File(Constants.JBOSS, "server/default/deploy/mssql-ds.xml").getAbsolutePath());
		} catch (IOException e1) {
			System.err.println("[Business] Error: No se ha copiado correctamente el fichero mssql-ds.xml a su destino");
			e1.printStackTrace();
			return false;
		}
		
		/*
		 * Copiamos todas las páginas HTML y JSP a su destino.
		 */
		try {
			copyWebPages();
		} catch (IOException e){
			System.err.println("[Business] Error: No se han podido copiar correctamente las páginas de accesos a la aplicación");
		}
		
		/*
		 * Descomprimimos el fichero donde están contenidos:
		 * 		· dynagent-ejb.jar
		 * 		· dynagent-web.war
		 * Además, sustituimos el fichero:
		 * 		· META-INF/application.xml
		 */
		Zip.unZip(new File(Constants.JBOSS, "server/default/deploy/dynagent-ejb.ear"), new File(Constants.ROOT,"temp/dynagent-ejb.ear/"));
		try {
			FileCopy.copy(new File(Constants.ROOT,"server/application.xml").getAbsolutePath(), new File(Constants.ROOT,"temp/dynagent-ejb.ear/META-INF/application.xml").getAbsolutePath());
		} catch (IOException e) {
			System.err.println("[Business] Error: No se ha copiado correctamente el fichero application.xml a su destino");
			e.printStackTrace();
			return false;
		}
		
		/*
		 * De la carpeta en la que hemos sacado el contenido del comprimido,
		 * tenemos que descomprimir los dos ficheros anteriormente mencionados.
		 * Primero lo haremos con dynagent-web.war, donde tenemos que modificar
		 * el fichero:
		 * 		· WEB-INF/web.xml.
		 * Además, copiamos el fichero:
		 * 		· WEB-INF/jboss-web.xml
		 * Posteriormente, volvemos a comprimir el fichero.
		 */		
		File zipSourceFile = new File(Constants.ROOT,"temp/dynagent-ejb.ear/dynagent-web.war");
		File destDirectory = new File(Constants.ROOT,"temp/dynagent-web.war/");
		Zip.unZip(zipSourceFile, destDirectory);
		try {
			FileCopy.copy(new File(Constants.ROOT,"temp/web.xml").getAbsolutePath(), new File(destDirectory, "WEB-INF/web.xml").getAbsolutePath());
			FileCopy.copy(new File(Constants.ROOT,"server/web/jboss-web.xml").getAbsolutePath(), new File(destDirectory, "WEB-INF/jboss-web.xml").getAbsolutePath());
		} catch (IOException e1) {
			System.err.println("[Business] Error: No se ha copiado correctamente el fichero web.xml a su destino");
			e1.printStackTrace();
			return false;
		}
		Zip.zip(destDirectory, zipSourceFile);
		
		/*
		 * Vamos a descomprimir dynagent-ejb.jar y a modificar los ficheros:
		 * 		· META-INF/ejb-jar.xml
		 * 		· META-INF/jboss.xml 
		 * Además vamos a añadir el fichero:
		 * 		· META-INF/jbosscmp-ejb.xml
		 */
		File tmp = new File (Constants.ROOT,"temp/jar");
		tmp.mkdirs();
		try {
			Zip.unZip(new File(Constants.ROOT,"temp/dynagent-ejb.ear/dynagent-ejb.jar"), tmp);
			FileCopy.copy(new File(Constants.ROOT,"temp/ejb-jar.xml").getAbsolutePath(), new File(tmp, "META-INF/ejb-jar.xml").getAbsolutePath());
			FileCopy.copy(new File(Constants.ROOT,"temp/jboss.xml").getAbsolutePath(), new File(tmp, "META-INF/jboss.xml").getAbsolutePath());
			FileCopy.copy(new File(Constants.ROOT,"temp/jbosscmp-jdbc.xml").getAbsolutePath(), new File(tmp,"META-INF/jbosscmp-jdbc.xml").getAbsolutePath());
			Zip.zip(tmp, new File(Constants.ROOT,"temp/dynagent-ejb.ear/dynagent-ejb.jar"));
		} catch (IOException e) {
			System.err.println("[Business] Error: Fallo al intentar construir el comprimido dynagent-ejb.jar");
			e.printStackTrace();
			return false;
		}
		
		/*
		 * Y finalmente volvemos a comprimirlo todo y lo copiamos en el lugar
		 * que le corresponde.
		 * Además, borramos la carpeta temporal.
		 */
		
		Zip.zip(new File(Constants.ROOT,"temp/dynagent-ejb.ear/"), new File(Constants.JBOSS, "server/default/deploy/dynagent-ejb.ear"));
		
		deleteFolder(new File(Constants.ROOT,"temp/"));
		
		return true;
	}
	
	/**
	 * Trata de crear un lector y un escritor de los ficheros indicados.
	 * 
	 * @param fromFilePath Ruta al fichero de origen que se quiere leer.
	 * @param toFilePath Ruta al fichero destino en el que se quiere escribir.
	 * Si no existe, lo crea. Si la ruta indicada es tan solo un directorio, crea
	 * el fichero destino con el mismo nombre que el de origen.
	 * @return <code>true</code> si no ha ocurrido ningún problema durante la creación
	 * y todo se ha hecho de manera correcta.
	 */
	protected boolean openFile(String fromFilePath, String toFilePath){
		File fromFile = new File(fromFilePath);
		if (! fromFile.exists() || ! fromFile.isFile()){
			return false;
		}
		
		File toFile = new File(toFilePath);
		if (toFile.isDirectory()){
			toFile = new File(toFile, fromFile.getName());
		}
		
		if (toFile.getParent() != null && ! toFile.getParentFile().exists()){
			toFile.getParentFile().mkdirs();
		}
		
		FileReader fr;
		try {
			fr = new FileReader(fromFile);
		} catch (FileNotFoundException e) {
			System.err.println("[Business] El fichero " + fromFile.getAbsolutePath() + " no existe.");
			e.printStackTrace();
			return false;			
		}
		reader = new BufferedReader(fr);
		
		FileWriter fw;
		try {
			fw = new FileWriter(toFile);
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar crear el fichero " + toFile.getAbsolutePath());
			e.printStackTrace();
			return false;
		}
		writer = new BufferedWriter(fw);
		return true;
	}
	
	/**
	 * Método que se encarga de cerrar los ficheros si es posible.
	 */
	protected void close(){
		try {
			reader.close();
			writer.close();
		} catch (IOException e1) {
			System.err.println("[Business] Error al intentar cerrar los lectores.");
			e1.printStackTrace();
		}
	}
	
	/**
	 * Método que se encarga de escribir el texto indicado añadiendo un salto de línea
	 * al final del mismo.
	 * @param text Cadena de texto que se desea escribir
	 * @throws IOException Si se produce algún error en la escritura.
	 */
	protected void writeLine(String text) throws IOException{
		if (text == null){
			return;
		}
		writer.write(Indent.getIndent() + text);
		writer.newLine();
	}
	
	private void generateAuxiliarFiles(){
		if(stop){
			stop = false;
		}
		EjbJarTextFileBuilder ejbBuilder = new EjbJarTextFileBuilder();
		ejbBuilder.buildFile();
		if (stop){
			return;
		}
		JBossTextFileBuilder jbossBuilder = new JBossTextFileBuilder();
		jbossBuilder.buildFile();
		if (stop){
			return;
		}
		WebTextFileBuilder webBuilder = new WebTextFileBuilder();
		webBuilder.buildFile();
		if (stop){
			return;
		}
		MssqlTextFileBuilder mssqlBuilder = new MssqlTextFileBuilder();
		mssqlBuilder.buildFile();
		if (stop){
			return;
		}
		
		JBossCmpJDBCTextFileBuilder jbosscmpBuilder = new JBossCmpJDBCTextFileBuilder();
		jbosscmpBuilder.buildFile();
		if (stop){
			return;
		}

		HtmlTextFileBuilder index2Builder = new HtmlTextFileBuilder("index2.html");
		index2Builder.buildFile();
		if (stop){
			return;
		}
		
		HtmlTextFileBuilder entryBuilder = new HtmlTextFileBuilder("entry.html");
		entryBuilder.buildFile();
	}
	
	/**
	 * Copia todos los ficheros tanto de html como de jsp a su ruta dentro del jboss.
	 * @throws IOException Si se produce algún error al copiar archivos.
	 */
	private void copyWebPages() throws IOException{
		File webPagesFile = new File(Constants.JBOSS, "server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/");
		
		FileCopy.copy(new File(Constants.ROOT,"temp/entry.html").getAbsolutePath(), new File(webPagesFile, "entry.html").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT,"webPages/index.html").getAbsolutePath(), new File(webPagesFile, "index.html").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT,"temp/index2.html").getAbsolutePath(), new File(webPagesFile, "index2.html").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT,"webPages/entry.jsp").getAbsolutePath(), new File(webPagesFile, "entry.jsp").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT,"webPages/index.jsp").getAbsolutePath(), new File(webPagesFile, "index.jsp").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT, "webPages/checkJava.vbs").getAbsolutePath(), new File(webPagesFile, "checkJava.vbs").getAbsolutePath());
		FileCopy.copy(new File(Constants.ROOT, "webPages/expired.html").getAbsolutePath(), new File(webPagesFile, "expired.html").getAbsolutePath());
	}
	
	private void deleteFolder(File folder){
		if (! folder.isDirectory()){
			return;
		}
		
		String[] files = folder.list();
		
		for (int i = 0 ; i < files.length ; i ++){
			File file = new File(folder, files[i]);
			
			if (file.isDirectory()){
				deleteFolder(file);
			}else if (file.exists()){
				file.delete();
			}
		}
		
		folder.delete();
		
		
	}
}
