package dynagent.server.gestorsDB;

import java.io.Serializable;

public class GenerateConnection implements Serializable{
	
	private static final long serialVersionUID = 2599477088689219971L;
	private String gestor;
	
	public GenerateConnection(String gestor) {
		this.gestor = gestor;
	}
	
	public String getDriverClass() {
		String dev = "";
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "com.mysql.jdbc.Driver";
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "org.postgresql.Driver";
		}
		return dev;
	}
	public String getURL_JDBC(String db, String databaseIP, Integer port) {
		String dev = "";
		if (gestor.equals(GestorsDBConstants.mySQL)) {
			dev = "jdbc:mysql://"+databaseIP+":"+port+"/"+db;
		} else if (gestor.equals(GestorsDBConstants.SQLServer)) {
			dev = "jdbc:sqlserver://"+databaseIP+";databaseName="+db;
		} else if (gestor.equals(GestorsDBConstants.postgreSQL)) {
			dev = "jdbc:postgresql://"+databaseIP+":"+port+"/"+db+"?charSet=UNICODE";
		}
		System.out.println(dev);
		return dev;
	}
	
	public String getNameDB(int business) {
		String dev = "dyna" + business;
		if (gestor.equals(GestorsDBConstants.mySQL))
			dev += "_dbo";
		
		return dev;
	}
	/*public String getNameDB(String db) {
		String dev = db;
		if (gestor.equals(GestorsDBConstants.mySQL))
			dev += "_dbo";
		
		return dev;
	}*/
}
