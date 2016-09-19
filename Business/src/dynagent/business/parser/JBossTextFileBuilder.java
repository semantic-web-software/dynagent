package dynagent.business.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

/**
 * Clase que se encarga de crear el fichero temporal jboss.xml
 *
 */
public class JBossTextFileBuilder extends TextFileParser {

	public void buildFile(){
		File sourceFile = new File(Constants.ROOT, "server/ejb/jboss.xml");
		File destinationFile = new File(Constants.ROOT, "temp/jboss.xml");
		if ( ! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha generado el fichero jboss.xml");
			stop = true;
			return;
		}
		
		if (writer == null || reader == null){
			return;
		}
		
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
			if (line.contains("<!-- Session Block -->")){
				writeSessionBlocks();
			} else if (line.contains("<!-- Entity Block -->")){
				writeEntityBlocks();
			}else{
				try {
					writer.write(line);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo jboss.xml");
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
	}
	
	private void writeSessionBlocks() {
		if (Constants.DATABASES == null){
			return;
		}
		
		if(Constants.SHARED_BEAN){
			writeSessionBlock(null);
		}else{
			Iterator<Integer> it = Constants.DATABASES.iterator();
			
			while (it.hasNext() && ! stop){
				int dbNumber = it.next();
				writeSessionBlock(dbNumber);
			}	
		}
	}

	private void writeSessionBlock(Integer dbNumber) {
		Indent.setIdent(2);
		try {
			writeLine("<session>");
			Indent.increase();
			writeLine("<ejb-name>miInstance" + (dbNumber!=null?dbNumber:"") + "</ejb-name>");
			Indent.decrease();
			writeLine("</session>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en jboss.xml");
			stop = true;
		}
	}
	
	/**
	 * Quitar este bloque para deshabilitar el bean de bloqueos.
	 */
	private void writeEntityBlocks(){
		if (Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			// En PostgreSQL no se usa este bloque.
			return;
		}
		Indent.setIdent(2);
		try {
			writeLine("<entity>");
			Indent.increase();
			writeLine("<ejb-name>sessionBean</ejb-name>");
			writeLine("<jndi-name>ejb/ISession</jndi-name>");
			writeLine("<local-jndi-name>ejb/ISessionLocal</local-jndi-name>");
			Indent.decrease();
			writeLine("</entity>");
			writeLine("<entity>");
			Indent.increase();
			writeLine("<ejb-name>instanceLockEJB</ejb-name>");
			writeLine("<jndi-name>ejb/instanceLock</jndi-name>");
			writeLine("<local-jndi-name>ejb/instanceLockLocal</local-jndi-name>");
			Indent.decrease();
			writeLine("</entity>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque entity en jboss.xml");
			e.printStackTrace();
		}
	}
}
