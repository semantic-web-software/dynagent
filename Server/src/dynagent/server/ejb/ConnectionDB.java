package dynagent.server.ejb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import dynagent.server.gestorsDB.GenerateConnection;

public class ConnectionDB/* implements Sessionable*/ {
	
   	private java.sql.Connection m_conn;
   	private boolean autoCommit = false;
   	private Integer business;
   	private boolean standAloneApp=false;
   	private String user="dynagent";
   	private String password=null;
   	private String databaseIP="localhost";
   	private Integer port=3306;
   	private GenerateConnection generateConnection;
   	private int conexionStandAloneReutilizada=0;

	public final static int defaultCountSleep=30;
	private int countSleep = ConnectionDB.defaultCountSleep;

	public final static int defaultTimeOut=10;
	
	public ConnectionDB(Integer business, boolean autoC, boolean standAloneApp, String dbIP, Integer port, String usuario, 
			String pwd, GenerateConnection generateConnection) {
		this.business = business;
        autoCommit = autoC;
        this.standAloneApp = standAloneApp;
        this.generateConnection = generateConnection;
        if( usuario!=null )
        	this.user = usuario;
        if( pwd!=null )
        	this.password = pwd;
        if (standAloneApp) {
            if( dbIP!=null )
	        	this.databaseIP = dbIP;
            if( port!=null )
            	this.port = port;
        }
        addConnection();
    }
	
	
	public void setCountSleep(int countSleep) {
		this.countSleep = countSleep;
	}

	public void addConnection() {
		conexionStandAloneReutilizada++;
	}

	public void setAutoCommit(boolean autoCommit) {
		this.autoCommit = autoCommit;
	}
	
	/*public java.sql.Connection getBusinessConn() throws NamingException,SQLException {
		int cont = 1;
		boolean valid = false;
		int validSeconds = 2;
        if (standAloneApp && !(m_conn==null || m_conn.isClosed())) {
        	if (m_conn.isValid(validSeconds))
        		valid = true;
        }
		while (m_conn==null || m_conn.isClosed() || standAloneApp && !valid) {
            if (standAloneApp) {
                String url = getMSSQL_DB_URL(generateConnection.getNameDB(this.business), databaseIP, port, user, password);
                //System.out.println("CONECTANDO URL "+url+" USER "+user+" PWD "+password+" STANDALONE");
                try {
                    if (!(m_conn==null || m_conn.isClosed())) {
                    	if (m_conn.isValid(validSeconds)) {
                    		valid = true;
                    		if (m_conn==null || m_conn.isClosed()) {
        	                	m_conn = DriverManager.getConnection(url, user, password);
        		                m_conn.setAutoCommit(autoCommit);
        		                //System.out.println("CONECTADO");
                    		}
                    	} else
                    		throw new SQLException();
                    } else {
	                	m_conn = DriverManager.getConnection(url, user, password);
		                m_conn.setAutoCommit(autoCommit);
		                //System.out.println("CONECTADO");
                    }
	            } catch(SQLException e) {
                	if (cont==countSleep)
            			throw e;
    				else {
    					try {
    						Thread.sleep(500);
    					} catch (InterruptedException e1) {
    						e1.printStackTrace();
    					}
    				}
          		}
            } else {
				DataSource ds = (DataSource) ic.lookup("java:jdbc/" + "dyna" + this.business);
                //System.out.println("CONECTANDO DB "+"dyna"+this.business);
				m_conn = ds.getConnection();
		    }
            cont++;
		}
        return m_conn;
	}*/
	public java.sql.Connection getBusinessConn() throws NamingException,SQLException {
		int cont = 1;
		while (m_conn==null || m_conn.isClosed()) {
            if (standAloneApp) {
                String url = getMSSQL_DB_URL(generateConnection.getNameDB(this.business), databaseIP, port);
                //System.out.println("CONECTANDO URL "+url+" USER "+user+" PWD "+password+" STANDALONE");
                try {
                	//System.out.println("default connection timeout " + DriverManager.getLoginTimeout());
                	DriverManager.setLoginTimeout(defaultTimeOut);
                	//System.out.println("new connection timeout " + DriverManager.getLoginTimeout());
                	m_conn = DriverManager.getConnection(url, user, password);
	                m_conn.setAutoCommit(autoCommit);
	                //System.out.println("CONECTADO");
	            } catch(SQLException e) {
                	if (cont==countSleep)
            			throw e;
    				else {
    					try {
    						Thread.sleep(500);
    					} catch (InterruptedException e1) {
    						e1.printStackTrace();
    					}
    				}
          		}
            } else {
				DataSource ds = (DataSource) new InitialContext().lookup("java:jdbc/" + "dyna" + this.business);
                //System.out.println("CONECTANDO DB "+"dyna"+this.business);
				m_conn = ds.getConnection();
		    }
            cont++;
		}
        return m_conn;
	}

	public java.sql.Connection getDataBaseConn(String db) throws NamingException,SQLException {
		int cont = 1;
		while (m_conn==null || m_conn.isClosed()) {
            if (standAloneApp) {
                String url = getMSSQL_DB_URL(db, databaseIP, port);
                //System.out.println("CONECTANDO URL "+url+" USER "+user+" PWD "+password+" STANDALONE");
                try {
                	DriverManager.setLoginTimeout(defaultTimeOut);
	                m_conn = DriverManager.getConnection(url, user, password);
	                m_conn.setAutoCommit(autoCommit);
                } catch(SQLException e) {
                	if (cont==countSleep)
            			throw e;
    				else {
    					try {
    						Thread.sleep(500);
    					} catch (InterruptedException e1) {
    						e1.printStackTrace();
    					}
    				}
          		}
            } else {
				DataSource ds = (DataSource) new InitialContext().lookup("java:jdbc/" + db);
                //System.out.println("CONECTANDO DB "+db);
				m_conn = ds.getConnection();
		    }
            cont++;
		}
        return m_conn;
	}
	
	/**
	 * Siempre crea una nueva conexion y no la almacena, por lo que el que llame a este metodo es el responsable de cerrarla
	 */
	public java.sql.Connection getDataBaseConnNotReusable(String db) throws NamingException,SQLException {
		int cont = 1;
		Connection conn=null;
		while (conn==null || conn.isClosed()) {
            if (standAloneApp) {
                String url = getMSSQL_DB_URL(db, databaseIP, port);
                //System.out.println("CONECTANDO URL "+url+" USER "+user+" PWD "+password+" STANDALONE");
                try {
                	DriverManager.setLoginTimeout(defaultTimeOut);
	                conn = DriverManager.getConnection(url, user, password);
	                conn.setAutoCommit(autoCommit);
                } catch(SQLException e) {
                	if (cont==countSleep)
            			throw e;
    				else {
    					try {
    						Thread.sleep(500);
    					} catch (InterruptedException e1) {
    						e1.printStackTrace();
    					}
    				}
          		}
            } else {
				DataSource ds = (DataSource) new InitialContext().lookup("java:jdbc/" + db);
                //System.out.println("CONECTANDO DB "+db);
				conn = ds.getConnection();
		    }
            cont++;
		}
        return conn;
	}
	
    private String getMSSQL_DB_URL( String db, String databaseIP, Integer port){
    	return generateConnection.getURL_JDBC(db, databaseIP, port);
    }
    
    public boolean isClosed() throws SQLException {
    	return (m_conn==null || m_conn.isClosed());
    }
	public boolean closeStandAlone() throws SQLException{
        //System.out.println("CERRANDO CONEXION");
		boolean closed = false;
		if (this.conexionStandAloneReutilizada==1) {
	        if(m_conn!=null && !m_conn.isClosed())
	            m_conn.close();
	        m_conn=null;
	        closed = true;
		}
		this.conexionStandAloneReutilizada--;
		return closed;
	}
	public void forceClose() throws SQLException{
        //System.out.println("CERRANDO CONEXION");
        if(m_conn!=null && !m_conn.isClosed())
            m_conn.close();
        m_conn=null;
	}
	public boolean isAutoCommit() {
		return autoCommit;
	}

	/*public void setDatabaseIP(String databaseIP) {
		this.databaseIP = databaseIP;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public void setUser(String user) {
		this.user = user;
	}*/
	
	public String toString() {
	   	return "autoCommit: " + this.autoCommit + ", business: " + this.business +
	   		", standAloneApp: " + this.standAloneApp + ", user: " + this.user + 
	   		", password: " + this.password + ", databaseIP: " + this.databaseIP + 
	   		", port: " + this.port + ", conexionStandAloneReutilizada: " + this.conexionStandAloneReutilizada;
	}

	
	public void commit() throws SQLException{
       	m_conn.commit();
    }
	public void rollback() throws SQLException{
       	m_conn.rollback();
    }
/*
    private void subCommit() throws SQLException{
        if(m_conn!=null && !m_conn.isClosed())
        	m_conn.commit();
    }
    private void subRollback() throws SQLException{
        if(m_conn!=null && !m_conn.isClosed())
        	m_conn.rollback();
	}
	public void rollBack() throws ApplicationException {
		try {
			subRollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void commit() throws ApplicationException {
		try {
			subCommit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}*/
	
	public boolean isReused(){
		return conexionStandAloneReutilizada>1;
	}
}
