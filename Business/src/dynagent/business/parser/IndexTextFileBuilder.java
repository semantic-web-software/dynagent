package dynagent.business.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import dynagent.business.util.Constants;
import dynagent.business.util.FileCopy;
import dynagent.business.util.Indent;

public class IndexTextFileBuilder extends TextFileParser {
	
	private boolean tempExists = false;
	private boolean modified = false;
	
	public void buildFile(){
		//Comprobamos que exista el fichero, pues de lo contrario no vamos a hacer nada
		File source = new File(Constants.JBOSS, "server/default/deploy/jbossweb-tomcat55.sar/ROOT.war/dyna/index2.html");
		if (! source.exists()){
			System.err.println("[Business] Advertencia: No se ha generado el fichero index2.html");
			return;
		}
		//Creamos el fichero destino en una carpeta temporal
		File destination = new File(source.getParentFile(), "/temp/index2.html");
		if (destination.getParentFile().exists()){
			tempExists = true;
		}else{
			destination.getParentFile().mkdir();
		}
		
		//Creamos los objetos para tratar con los ficheros
		if (! openFile(source.getAbsolutePath(), destination.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha podido copiar index2.html");
			return;
		}
		
		if (writer == null || reader == null){
			return;
		}
		
		//Empezamos a iterar sobre los ficheros para copiar del origen al destino
		String line;
		try {
			line = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			close();
			stop = true;
			return;
		}
		Indent.reset();
		while (line != null && ! stop){
			if (line.contains("<param name=\"bns\"")){
				writeBNSBlocks();
			}else if (line.contains("<!-- <p a r a m")){
				//Estas líneas las borramos pues cuando encontramos el primer bns ya escribimos el bloque completo.
				;
			}else{
				try {
					writeLine(line);
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo index2.html");
					close();
					stop = true;
					e.printStackTrace();
				}				
			}
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
				close();
				stop = true;
				return;
			}
		}		
		close();
		
		if (! stop){
			try {
				FileCopy.copy(destination.getAbsolutePath(), source.getAbsolutePath());
			} catch (IOException e) {
				System.err.println("[] Error: No se ha podido copiar el fichero index2.html generado a la ubicación del original");
				e.printStackTrace();
			}
		}
		
		destination.delete();
		if (! tempExists){
			destination.getParentFile().delete();
		}
	}
	
	private void writeBNSBlocks() {
		Indent.setIdent(3);
		if (modified){
			return;
		}
		if (Constants.DATABASES == null){
			stop = true;
			return;
		}
		
		Iterator<Integer> it = Constants.DATABASES.iterator();
		boolean first = true;
		int dbNumber;
		
		while(it.hasNext() && ! stop){
			dbNumber = it.next();
			try {
				if(first){
					writeLine("<param name=\"bns\" value=\"" + dbNumber + "\">");
					first = false;
				}else{
					writeLine("<!-- <p a r a m name=\"bns\" value=\"" + dbNumber + "\"> -->");
				}
			} catch (IOException e) {
				System.err.println("[Business] Error al intentar escribir index2.html");
				stop = true;
			}
			
		}
		modified = true;
		Indent.reset();
	}
}