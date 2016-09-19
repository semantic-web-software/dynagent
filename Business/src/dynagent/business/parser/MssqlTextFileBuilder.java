package dynagent.business.parser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import dynagent.business.util.Constants;
import dynagent.business.util.Indent;

public class MssqlTextFileBuilder extends TextFileParser {
	public void buildFile(){
		File sourceFile = new File(Constants.ROOT, "server/mssql-ds.xml");
		File destinationFile = new File(Constants.ROOT, "temp/mssql-ds.xml");
		if ( ! openFile(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath())){
			System.err.println("[Business] Error: No se ha generado el fichero mssql-ds.xml");
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
			if (line.contains("<!-- Local Datasource Block -->")){
				writeLocalDatasourceBlocks();
			}else{
				try {
					writer.write(line);
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					System.err.println("[Business] Error al intentar copiar una línea al nuevo mssql-ds.xml");
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

	private void writeLocalDatasourceBlocks() {
		if (Constants.DBMANAGER == null){
			stop = true;
			return;
		}
		
		if (Constants.DBMANAGER.equals(Constants.DBM_MYSQL)){
			writeMySqlBlocks();
		}else if(Constants.DBMANAGER.equals(Constants.DBM_SQLSERVER)){
			writeSQLServerBlocks();
		}else if(Constants.DBMANAGER.equals(Constants.DBM_POSTGRESQL)){
			writePostgreSQLBlocks();
		}else{
			stop = true;
		}
	}

	private void writePostgreSQLBlocks() {
		writePostgreSQLDynaGlobalBlock();
		writePostgreSQLDynaSurveyBlock();
		
		if (Constants.DATABASES == null){
			stop = true;
			return;
		}
		
		Iterator<Integer> it = Constants.DATABASES.iterator();
		
		while (it.hasNext() && ! stop){
			int dbNumber = it.next();
			writePostgreSQLBlock(dbNumber);
		}
	}
	
	private void writePostgreSQLDynaGlobalBlock() {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dynaglobal</jndi-name>");
			writeLine("<connection-url>jdbc:postgresql://localhost:" + Constants.DB_PORT + "/dynaglobal</connection-url>");
			writeLine("<driver-class>org.postgresql.Driver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>"+Constants.PASSWORD+"</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>PostgreSQL</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}
	
	private void writePostgreSQLDynaSurveyBlock() {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dynasurvey</jndi-name>");
			writeLine("<connection-url>jdbc:postgresql://localhost:" + Constants.DB_PORT + "/dynasurvey</connection-url>");
			writeLine("<driver-class>org.postgresql.Driver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>"+Constants.PASSWORD+"</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>PostgreSQL</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}
	
	private void writePostgreSQLBlock(int dbNumber) {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dyna" + dbNumber + "</jndi-name>");
			writeLine("<connection-url>jdbc:postgresql://localhost:" + Constants.DB_PORT + "/dyna" + dbNumber + "</connection-url>");
			writeLine("<driver-class>org.postgresql.Driver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>"+Constants.PASSWORD+"</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>PostgreSQL</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}

	private void writeSQLServerBlocks() {
		writeSQLServerDynaGlobalBlock();
		
		if(Constants.DATABASES == null){
			stop = true;
			return;
		}
		
		Iterator<Integer> it = Constants.DATABASES.iterator();
		
		while (it.hasNext() && ! stop){
			int dbNumber = it.next();
			writeSQLServerBlock(dbNumber);
		}
	}

	private void writeSQLServerDynaGlobalBlock() {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dynaGlobal</jndi-name>");
			writeLine("<connection-url>jdbc:sqlserver://localhost:1433;DatabaseName=dynaGlobal</connection-url>");
			writeLine("<driver-class>com.microsoft.sqlserver.jdbc.SQLServerDriver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>root</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>MS SQLSERVER2000</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}

	private void writeSQLServerBlock(int dbNumber) {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dyna" + dbNumber + "</jndi-name>");
			writeLine("<connection-url>jdbc:sqlserver://localhost:1433;DatabaseName=dyna" + dbNumber + "</connection-url>");
			writeLine("<driver-class>com.microsoft.sqlserver.jdbc.SQLServerDriver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>root</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>MS SQLSERVER2000</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}

	private void writeMySqlBlocks() {
		writeMySQLDynaGlobalBlock();
		
		if(Constants.DATABASES == null){
			stop = true;
			return;
		}
		
		Iterator<Integer> it = Constants.DATABASES.iterator();
		
		while (it.hasNext() && ! stop){
			int dbNumber = it.next();
			writeMySQLBlock(dbNumber);
		}
		
	}

	private void writeMySQLDynaGlobalBlock() {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dynaGlobal</jndi-name>");
			writeLine("<connection-url>jdbc:mysql://localhost:" + Constants.DB_PORT + "/dynaGlobal</connection-url>");
			writeLine("<driver-class>om.mysql.jdbc.Driver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>root</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>mySQL</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}

	private void writeMySQLBlock(int dbNumber) {
		Indent.setIdent(1);
		try {
			writeLine("<local-tx-datasource>");
			Indent.increase();
			writeLine("<jndi-name>jdbc/dyna" + dbNumber + "</jndi-name>");
			writeLine("<connection-url>jdbc:mysql://localhost:" + Constants.DB_PORT + "/dyna" + dbNumber + "_dbo</connection-url>");
			writeLine("<driver-class>om.mysql.jdbc.Driver</driver-class>");
			writeLine("<user-name>dynagent</user-name>");
			writeLine("<password>root</password>");
			writeLine("<metadata>");
			Indent.increase();
			writeLine("<type-mapping>mySQL</type-mapping>");
			Indent.decrease();
			writeLine("</metadata>");
			Indent.decrease();
			writeLine("</local-tx-datasource>");
		} catch (IOException e) {
			System.err.println("[Business] Error al intentar escribir el bloque container-transaction en mssql-ds.xml");
			stop = true;
		}
	}
}
