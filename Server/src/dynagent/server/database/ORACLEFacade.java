/**
 * @author Ildefonso Montero Perez
 * @description A file based facade to obtain data from an Oracle Database
 */

package dynagent.server.database;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;

import dynagent.common.knowledge.FactInstance;

// Completar este adaptador con la implementacion del resto de metodos

public class ORACLEFacade extends Facade {
	
	private Connection conn = null;
	
	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void create(String object, FactInstance value) {}

	public void delete(String object) {}

	public FactInstance get(String object) { return null; }
	
	public LinkedList getMultiple(String object) { return null; }

	public void open() {
		
		String PROPERTIES_FILE = "database.properties";
		FileInputStream fis;
		try {
			
			fis = new FileInputStream(PROPERTIES_FILE);
			Properties properties = new Properties();
			properties.load(fis);
			fis.close();
			
			conn = DriverManager.getConnection("jdbc:oracle:thin:@"+
												properties.getProperty("SERVER")+
												":"+
												properties.getProperty("PORT")+
												":"+
												properties.getProperty("DBNAME"),
												properties.getProperty("USER"),
												properties.getProperty("PASSWORD"));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set(String object, FactInstance value) {}
}
