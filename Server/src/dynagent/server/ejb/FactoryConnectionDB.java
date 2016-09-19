package dynagent.server.ejb;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Properties;

import dynagent.server.gestorsDB.GenerateConnection;

public class FactoryConnectionDB implements Serializable {
	
	private static final long serialVersionUID = -1086673473136730158L;
	private Integer business;
	private boolean standAloneApp;
	private String databaseIP;
	private Integer port;
	private String usuario;
	public String pwd;
	private String gestorDB;
	private GenerateConnection generateConnection;
	private ConnectionDB con;
	private boolean forceNewConnection;
	private int numberConnections=0;
	private boolean enUso;

	private ConnectionDB conAutoC;
	private boolean forceNewConnectionAutoC;
	private int numberConnectionsAutoC=0;
	private boolean enUsoAutoC;
	
	private int countSleep = ConnectionDB.defaultCountSleep;

	public FactoryConnectionDB(Integer business, boolean standAloneApp, String dbIP, String gestor) {
		this.business = business;
		this.standAloneApp = standAloneApp;
		this.gestorDB = gestor;
		this.generateConnection = new GenerateConnection(gestor);
		if(standAloneApp) {
            try {
                if( dbIP!=null )
                    databaseIP=dbIP;
            	Class.forName(generateConnection.getDriverClass());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
    		Properties prop = new Properties();
    		InputStream input = null;
    	 
    		try {	 
    			input = new FileInputStream("config.properties");	 
    			// load a properties file
    			prop.load(input);	 
    			pwd=prop.getProperty("dbpassword");
    			// get the property value and print it out
    			System.out.println(prop.getProperty("dbpassword"));	 
    		} catch (IOException ex) {
    			//ex.printStackTrace();
    		} finally {
    			if (input != null) {
    				try {
    					input.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}
		}

	}
	public void setCountSleep(int countSleep) {
		this.countSleep = countSleep;
		if (con!=null)
			con.setCountSleep(countSleep);
		if (conAutoC!=null)
			conAutoC.setCountSleep(countSleep);
	}
	public void restoreCountSleep() {
		countSleep = ConnectionDB.defaultCountSleep;
		setCountSleep(countSleep);
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
		if (this.standAloneApp) {
			if (con!=null) {
				//if (!enUso) {
					//con.setPassword(pwd);
				//} else {
					this.forceNewConnection = true;
				//}
			}
			if (conAutoC!=null) {
				//if (!enUsoAutoC) {
					//conAutoC.setPassword(pwd);
				//} else {
					this.forceNewConnectionAutoC = true;
				//}
			}
		}
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
		if (this.standAloneApp) {
			if (con!=null) {
				//if (!enUso) {
					//con.setUser(usuario);
				//} else {
					this.forceNewConnection = true;
				//}
			}
			if (conAutoC!=null) {
				//if (!enUsoAutoC) {
					//conAutoC.setUser(usuario);
				//} else {
					this.forceNewConnectionAutoC = true;
				//}
			}
		}
	}
	public void setPort(Integer port) {
		this.port = port;
		if (this.standAloneApp) {
			if (con!=null) {
				//if (!enUso) {
					//con.setPort(port);
				//} else {
					this.forceNewConnection = true;
				//}
			}
			if (conAutoC!=null) {
				//if (!enUsoAutoC) {
					//conAutoC.setPort(port);
				//} else {
					this.forceNewConnectionAutoC = true;
				//}
			}
		}
	}
	public void setDatabaseIP(String databaseIP) {
		this.databaseIP = databaseIP;
		if (this.standAloneApp) {
			if (con!=null) {
				//if (!enUso) {
					//con.setDatabaseIP(databaseIP);
				//} else {
					this.forceNewConnection = true;
					//}
			}
			if (conAutoC!=null) {
				//if (!enUsoAutoC) {
					//conAutoC.setDatabaseIP(databaseIP);
				//} else {
					this.forceNewConnectionAutoC = true;
				//}
			}
		}
	}
	public void setBusiness(int business) {
		this.business = business;
		if (this.standAloneApp) {
			if (con!=null) {
				//if (!enUso) {
					//con.setDatabaseIP(databaseIP);
				//} else {
					this.forceNewConnection = true;
					//}
			}
			if (conAutoC!=null) {
				//if (!enUsoAutoC) {
					//conAutoC.setDatabaseIP(databaseIP);
				//} else {
					this.forceNewConnectionAutoC = true;
				//}
			}
		}
	}
	public Integer getBusiness() {
		return business;
	}
	public String getDatabaseIP() {
		return databaseIP;
	}
	public Integer getPort() {
		return port;
	}
	
	/*private boolean sameConnection(boolean autoC) {
		boolean isSame = false;
		if (con.isAutoCommit()==autoC && 
				(business==null && con.getBusiness()==null || 
						con.getBusiness()!=null && business!=null && con.getBusiness().equals(business)) &&
				StringUtils.equals(con.getDatabaseIP(),databaseIP) &&
				StringUtils.equals(con.getPassword(),pwd) &&
				(port==null && con.getPort()==null || 
						con.getPort()!=null && port!=null && con.getPort().equals(port)) &&
						StringUtils.equals(con.getUser(),usuario))
			isSame = true;
		return isSame;
	}*/

	public ConnectionDB createConnection(boolean autoC) {
		ConnectionDB conRet = null;
		if (standAloneApp) {
			if (autoC) {
				enUsoAutoC = true;
				if (conAutoC==null || this.forceNewConnectionAutoC) {
					conAutoC = new ConnectionDB(business, autoC, standAloneApp, databaseIP, port, usuario, pwd, generateConnection);
					conAutoC.setCountSleep(countSleep);
					numberConnectionsAutoC++;
					this.forceNewConnectionAutoC = false;
				} else {
					if (enUsoAutoC)
						conAutoC.addConnection();
				}
				conRet = conAutoC;
			} else {
				enUso = true;
				if (con==null || this.forceNewConnection) {
					con = new ConnectionDB(business, autoC, standAloneApp, databaseIP, port, usuario, pwd, generateConnection);
					con.setCountSleep(countSleep);
					numberConnections++;
					this.forceNewConnection = false;
				} else {
					if (enUso)
						con.addConnection();
				}
				conRet = con;
			}
		} else {
			conRet = new ConnectionDB(business, autoC, standAloneApp, databaseIP, port, usuario, pwd, generateConnection);
			conRet.setCountSleep(countSleep);
		}
//		SessionController.getInstance().getActualSession().addSessionable(this);
        return conRet;
    }	

	public void close(ConnectionDB conDB) throws SQLException {
		if (conDB!=null) {
			if (this.standAloneApp) {
				if (conDB.isAutoCommit()) {
					if (numberConnectionsAutoC==1)
						enUsoAutoC = false;
					else if (numberConnectionsAutoC>1) {
						boolean closed = conDB.closeStandAlone();
						if (closed) {
							conDB = null;
							numberConnectionsAutoC--;
						}
					}
				} else {
					if (numberConnections==1)
						enUso = false;
					else if (numberConnections>1) {
						boolean closed = conDB.closeStandAlone();
						if (closed) {
							conDB = null;
							numberConnections--;
						}
					}
				}
			} else {
				conDB.forceClose();
			}
		}
	}
	//se podria usar un array por si no se ha hecho close tras usar mas de una conexion
	public void removeConnections() throws SQLException {
		if (this.standAloneApp) {
			if (con!=null) {
				con.forceClose();
				con = null;
			}
			if (conAutoC!=null) {
				conAutoC.forceClose();
				conAutoC = null;
			}
		}
	}
	public boolean isStandAloneApp() {
		return this.standAloneApp;
	}
	
	public void setStandAloneApp(boolean standAloneApp) throws SQLException {
		this.standAloneApp = standAloneApp;
		
		if (standAloneApp) {
			if (con!=null)
				this.forceNewConnection = true;
			if (conAutoC!=null)
				this.forceNewConnectionAutoC = true;
		} else
			removeConnections();
	}

	public void commit() throws SQLException {
		if (con!=null && !con.isClosed())
			con.commit();
	}
	public void rollback() throws SQLException {
		if (con!=null && !con.isClosed())
			con.rollback();
	}
	public String getGestorDB() {
		return gestorDB;
	}
	
	public String toString() {
		String result = "business: " + this.business + ", standAloneApp: " + this.standAloneApp + 
		", databaseIP: " + this.databaseIP + ", port: " + this.port + 
		", usuario: " + this.usuario + ", pwd: " + this.pwd + 
		", gestorDB: " + this.gestorDB + "\nforceNewConnection: " + this.forceNewConnection + 
		", numberConnections: " + this.numberConnections + ", enUso: " + this.enUso + 
		"\nforceNewConnectionAutoC: " + this.forceNewConnectionAutoC + ", numberConnectionsAutoC: " + this.numberConnectionsAutoC + 
		", enUsoAutoC: " + this.enUsoAutoC;
		return result;
	}
}
