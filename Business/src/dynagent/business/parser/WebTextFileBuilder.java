package dynagent.business.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

public class WebTextFileBuilder extends TextFileParser {
	public void buildFile(){
		File sourceFile = new File(Constants.ROOT, "server/web/web.xml");
		File destinationFile = new File(Constants.ROOT, "temp/web.xml");
		if ( ! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha generado el fichero web.xml");
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
			if (line.contains("<!-- Init-param Block -->")){
				writeServletInitParamsBlock();
			}else if (line.contains("<!-- Ejb-ref Block -->")){
				writeEjbRefBlocks();
			}else{
				try {
					writer.write(line);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo web.xml");
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
	
	private void writeServletInitParamsBlock() {
		Indent.setIdent(1);
		try {
			writeLine("<init-param>");
			Indent.increase();
			writeLine("<description>Indica si el servlet tiene que reutilizar el mismo bean</description>");
			writeLine("<param-name>sharedBean</param-name>");
			writeLine("<param-value>"+Constants.SHARED_BEAN+"</param-value>");
			Indent.decrease();
			writeLine("</init-param>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque initParams en web.xml");
			e.printStackTrace();
		}
	}

	private void writeEjbRefBlocks() {
		if (Constants.DATABASES == null){
			return;
		}
		
		if (! Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			writeDefaultEjbRefBlocks();
		}
		
		if(Constants.SHARED_BEAN){
			writeEjbRefBlock(null);
		}else{
			Iterator<Integer> it = Constants.DATABASES.iterator();
			
			while (it.hasNext() && ! stop){
				int dbNumber = it.next();
				writeEjbRefBlock(dbNumber);
			}
		}
	}
	
	private void writeDefaultEjbRefBlocks(){
		Indent.setIdent(1);
		try {
			writeLine("<ejb-ref>");
			Indent.increase();
			writeLine("<ejb-ref-name>ejb/instanceLockEJB</ejb-ref-name>");
			writeLine("<ejb-ref-type>Entity</ejb-ref-type>");
			writeLine("<ejb-link>instanceLockEJB</ejb-link>");
			writeLine("<home>dynagent.server.ejb.instanceLockHome</home>");
			writeLine("<remote>dynagent.server.ejb.instanceLock</remote>");
			Indent.decrease();
			writeLine("</ejb-ref>");
			writeLine("<ejb-ref>");
			Indent.increase();
			writeLine("<ejb-ref-name>ejb/sessionBean</ejb-ref-name>");
			writeLine("<ejb-ref-type>Entity</ejb-ref-type>");
			writeLine("<ejb-link>sessionBean</ejb-link>");
			writeLine("<home>dynagent.server.ejb.ISessionHome</home>");
			writeLine("<remote>dynagent.server.ejb.ISession</remote>");
			Indent.decrease();
			writeLine("</ejb-ref>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque ejb-ref en web.xml");
			e.printStackTrace();
		}
	}

	private void writeEjbRefBlock(Integer dbNumber) {
		Indent.setIdent(1);
		try {
			writeLine("<ejb-ref>");
			Indent.increase();
			writeLine("<ejb-ref-name>ejb/miInstance" + (dbNumber!=null?dbNumber:"") + "</ejb-ref-name>");
			writeLine("<ejb-ref-type>Session</ejb-ref-type>");
			writeLine("<ejb-link>miInstance" + (dbNumber!=null?dbNumber:"") + "</ejb-link>");
			writeLine("<home>dynagent.server.ejb.InstanceHome</home>");
			writeLine("<remote>dynagent.server.ejb.Instance</remote>");
			Indent.decrease();
			writeLine("</ejb-ref>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque ejb-ref en web.xml");
			stop = true;
		}
	}
	/*
	 <ejb-ref>
		<ejb-ref-name>ejb/miInstance3</ejb-ref-name>
		<ejb-ref-type>Session</ejb-ref-type>
		<ejb-link>miInstance3</ejb-link>
		<home>dynagent.server.ejb.InstanceHome</home>
		<remote>dynagent.server.ejb.Instance</remote>
	</ejb-ref>
	 */
}
