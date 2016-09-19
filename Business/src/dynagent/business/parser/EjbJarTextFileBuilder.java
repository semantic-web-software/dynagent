package dynagent.business.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

public class EjbJarTextFileBuilder extends TextFileParser {
	
	public void buildFile(){
		File sourceFile = new File(Constants.ROOT, "server/ejb/ejb-jar.xml");
		File destinationFile = new File(Constants.ROOT, "temp/ejb-jar.xml");
		if ( ! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha generado el fichero ejb-jar.xml");
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
			}else if (line.contains("<!-- Container-transaction Block -->")){
				writeContainterTransactionBlocks();
			}else if (line.contains("<!-- Entity Block -->")){
				writeEntityBlocks();
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

	private void writeContainterTransactionBlocks() {
		if (Constants.DATABASES == null){
			return;
		}
		
		if(Constants.SHARED_BEAN){
			writeContainterTransactionBlock(null, "transitionObject","RequiresNew");
			writeContainterTransactionBlock(null, "report","Never");
				//writeContainterTransactionBlock("transitionDataMigration");
		}else{
			Iterator<Integer> it = Constants.DATABASES.iterator();
			
			while (it.hasNext() && ! stop){
				int dbNumber = it.next();
				writeContainterTransactionBlock(dbNumber, "transitionObject","RequiresNew");
				writeContainterTransactionBlock(null, "report","Never");
//				writeContainterTransactionBlock(dbNumber, "transitionDataMigration");
			}
		}
		
	}

	private void writeContainterTransactionBlock(Integer dbNumber, String name,String tran_type) {
		Indent.setIdent(2);
		try {
			writeLine("<container-transaction>");
			Indent.increase();
			writeLine("<method>");
			Indent.increase();
			writeLine("<ejb-name>miInstance" + (dbNumber!=null?dbNumber:"") + "</ejb-name>");
			writeLine("<method-name>" + name + "</method-name>");
			writeLine("<transaction-timeout>3000</transaction-timeout>");
			Indent.decrease();
			writeLine("</method>");
			writeLine("<trans-attribute>"+tran_type+"</trans-attribute>");
			Indent.decrease();
			writeLine("</container-transaction>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en ejb-jar.xml");
			stop = true;
		}
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
		String replica = Constants.REPLICA ? "YES" : "NO";
		
		try {
			writeLine("<session>");
			Indent.increase();
			writeLine("<description>This bean implements the back end of the address book application</description>");
			writeLine("<ejb-name>miInstance" + (dbNumber!=null?dbNumber:"") + "</ejb-name>");
			writeLine("<home>dynagent.server.ejb.InstanceHome</home>");
			writeLine("<remote>dynagent.server.ejb.Instance</remote>");
			writeLine("<ejb-class>dynagent.server.ejb.InstanceEJB</ejb-class>");
			writeLine("<session-type>Stateless</session-type>");
			writeLine("<transaction-type>Container</transaction-type>");
			writeLine("<transaction-scope>Distributed</transaction-scope>");
			/*if(dbNumber!=null){
				writeLine("<resource-ref>");
				Indent.increase();
				writeLine("<res-ref-name>jdbc/dyna" + dbNumber + "</res-ref-name>");
				writeLine("<res-type>javax.sql.DataSource</res-type>");
				writeLine("<res-auth>Container</res-auth>");
				Indent.decrease();
				writeLine("</resource-ref>");
			}*/
			writeLine("<env-entry>");
			Indent.increase();
			writeLine("<env-entry-name>BUSINESS_GLOBAL</env-entry-name>");
			writeLine("<env-entry-type>java.lang.String</env-entry-type>");
			writeLine("<env-entry-value>dynaGlobal</env-entry-value>");
			Indent.decrease();
			writeLine("</env-entry>");
			writeLine("<env-entry>");
			Indent.increase();
			writeLine("<env-entry-name>databaseHOST</env-entry-name>");
			writeLine("<env-entry-type>java.lang.String</env-entry-type>");
			writeLine("<env-entry-value>localhost</env-entry-value>");
			Indent.decrease();
			writeLine("</env-entry>");
			if(dbNumber!=null){
				writeLine("<env-entry>");
				Indent.increase();
				writeLine("<env-entry-name>BUSINESS</env-entry-name>");
				writeLine("<env-entry-type>java.lang.Integer</env-entry-type>");
				writeLine("<env-entry-value>" + dbNumber + "</env-entry-value>");
				Indent.decrease();
				writeLine("</env-entry>");
			}
			writeLine("<env-entry>");
			Indent.increase();
			writeLine("<env-entry-name>debugMode</env-entry-name>");
			writeLine("<env-entry-type>java.lang.Boolean</env-entry-type>");
			writeLine("<env-entry-value>False</env-entry-value>");
			Indent.decrease();
			writeLine("</env-entry>");
			writeLine("<env-entry>");
			Indent.increase();
			writeLine("<env-entry-name>gestorDB</env-entry-name>");
			writeLine("<env-entry-type>java.lang.String</env-entry-type>");
			writeLine("<env-entry-value>" + Constants.DBMANAGER + "</env-entry-value>");
			Indent.decrease();
			writeLine("</env-entry>");
			writeLine("<env-entry>");
			Indent.increase();
			writeLine("<env-entry-name>replica</env-entry-name>");
			writeLine("<env-entry-type>java.lang.String</env-entry-type>");
			writeLine("<env-entry-value>" + replica + "</env-entry-value>");
			Indent.decrease();
			writeLine("</env-entry>");
			Indent.decrease();
			writeLine("</session>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque session en ejb-jar.xml");
			stop = true;
		}
	}
	
	/**
	 * Quitar la escritura de este bloque para deshabilitar el beans de bloqueos.
	 */
	private void writeEntityBlocks(){
		if (Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			// PostgreSQL no utiliza este tipo de bloques.
			return;
		}
		Indent.setIdent(2);
		try {
			writeLine("<entity>");
			Indent.increase();
			writeLine("<display-name>Instance Lock</display-name>");
			writeLine("<ejb-name>instanceLockEJB</ejb-name>");
			writeLine("<local-home>dynagent.server.ejb.instanceLockHome</local-home>");
			writeLine("<local>dynagent.server.ejb.instanceLock</local>");
			writeLine("<ejb-class>dynagent.server.ejb.instanceLockBean</ejb-class>");
			writeLine("<persistence-type>Container</persistence-type>");
			writeLine("<prim-key-class>java.lang.String</prim-key-class>");
			writeLine("<reentrant>False</reentrant>");
			writeLine("<cmp-version>2.x</cmp-version>");
			writeLine("<abstract-schema-name>gangster</abstract-schema-name>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>ID</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>user</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<primkey-field>ID</primkey-field>");
			Indent.decrease();
			writeLine("</entity>");
			writeLine("<entity>");
			Indent.increase();
			writeLine("<display-name>SessionBean</display-name>");
			writeLine("<ejb-name>sessionBean</ejb-name>");
			writeLine("<local-home>dynagent.server.ejb.ISessionHome</local-home>");
			writeLine("<local>dynagent.server.ejb.ISession</local>");
			writeLine("<ejb-class>dynagent.server.ejb.SessionBean</ejb-class>");
			writeLine("<persistence-type>Container</persistence-type>");
			writeLine("<prim-key-class>java.lang.String</prim-key-class>");
			writeLine("<reentrant>False</reentrant>");
			writeLine("<cmp-version>2.x</cmp-version>");
			writeLine("<abstract-schema-name>sessionBean</abstract-schema-name>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>user</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>idSession</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>lastTime</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<cmp-field>");
			Indent.increase();
			writeLine("<field-name>remoteIp</field-name>");
			Indent.decrease();
			writeLine("</cmp-field>");
			writeLine("<primkey-field>idSession</primkey-field>");
			Indent.decrease();
			writeLine("</entity>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque entity en ejb-jar.xml");
			e.printStackTrace();
		}
	}
}
