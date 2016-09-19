package dynagent.business.parser;

import java.io.File;
import java.io.IOException;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

public class JBossCmpJDBCTextFileBuilder extends TextFileParser {
	public void buildFile(){
		File sourceFile = new File(Constants.ROOT, "server/ejb/jbosscmp-jdbc.xml");
		File destinationFile = new File(Constants.ROOT, "temp/jbosscmp-jdbc.xml");
		if ( ! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha generado el fichero jbosscmp-jdbc.xml");
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
			if (line.contains("<!-- Defaults Block -->")){
				writeDefaultsBlocks();
			}else if (line.contains("<!-- Enterprise-Beans Block -->")){
				writeEnterpriseBeansBlocks();
			}else{
				try {
					writer.write(line);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo ejb-jar.xml");
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

	private void writeDefaultsBlocks() {
		if (Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			return;
		}
		Indent.setIdent(1);
		try {
			writeLine("<defaults>");
			writeLine("<datasource>java:/jdbc/dynaGlobal</datasource>");
			writeLine("</defaults>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque defaults en jbosscmp-jdbc.xml");
			e.printStackTrace();
		}
		
	}

	private void writeEnterpriseBeansBlocks() {
		if (Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			return;
		}
		Indent.setIdent(1);
		try {
			writeLine("<enterprise-beans>");
			Indent.increase();
			writeLine("<entity>");
			Indent.increase();
			writeLine("<ejb-name>instanceLockEJB</ejb-name>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>user</field-name>");
			writeLine("<column-name>login</column-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<table-name>locksID</table-name>");
			Indent.decrease();
			writeLine("</entity>");
			writeLine("<entity>");
			Indent.increase();
			writeLine("<ejb-name>sessionBean</ejb-name>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>user</field-name>");
			writeLine("<column-name>usuario</column-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>idSession</field-name>");
			writeLine("<column-name>idSession</column-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>lastTime</field-name>");
			writeLine("<column-name>lastTime</column-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>remoteIp</field-name>");
			writeLine("<column-name>remoteIp</column-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<table-name>Sessions</table-name>");
			Indent.decrease();
			writeLine("</entity>");
			Indent.decrease();
			writeLine("</enterprise-beans>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque enterprise-beans en jbosscmp-jdbc.xml");
			e.printStackTrace();
		}
	}
}
