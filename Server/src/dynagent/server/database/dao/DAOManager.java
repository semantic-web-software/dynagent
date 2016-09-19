/***
 * DAOManager.java
 * @author Ildefonso Montero Perez - monteroperez@us.es
 */

package dynagent.server.database.dao;

import dynagent.server.ejb.FactoryConnectionDB;

public class DAOManager {

	static private DAOManager instance = null;
	public String table = "";
	private static String business = "";
	private static FactoryConnectionDB factConnDB;
	private static boolean commit = false;
	/**
	 * getInstance
	 * @return an instance of the DAOManager, this class is a Singleton class.
	 */
	public static DAOManager getInstance(){
        if(instance == null)
        	instance = new DAOManager();
        return instance;
    }
    
	/**
	 * DAOManager
	 * @return an instance of the DAO, default constructor.
	 */
    private DAOManager() {}
	
	/**
	 * DAOManager
	 * @return an public constructor of the DAO, not an instance.
	 */
    public DAOManager(FactoryConnectionDB factConnDB, String business) {
    	this.factConnDB = factConnDB;
    	this.business = business;
    }
	/**
	 * getTable()
	 * @return the type of the DAO
	 */
	public String getTable() {
		return table;
	}
	
	/**
	 * setTable()
	 * @param table represents the Table of the DAO
	 */
	public void setTable(String table) {
		this.table = table;
	}
	
	public static boolean isCommit() {
		return commit;
	}
	public static void setCommit(boolean commit) {
		//DAOManager.commit = commit;
		instance.commit = commit;
	}
	
	public static FactoryConnectionDB getFactConnDB() {
		return factConnDB;
	}
	public static void setFactConnDB(FactoryConnectionDB factConnDB) {
		//DAOManager.factConnDB = factConnDB;
		instance.factConnDB = factConnDB;
	}

	public void setBusiness(String b){
		this.business = b;
	}
	public String getBusiness() {
		return business;
	}

	public static void setInstance(DAOManager instance) {
		DAOManager.instance = instance;
	}

	/**
	 * getDAO
	 */
	public IDAO getDAO(String table){
        
		IDAO f = null;

			if(table.equals("O_Datos_Atrib"))
				f = new O_Datos_AttribDAO();
			else if(table.equals("instances"))
				f = new InstanceDAO();
			else if(table.equals("properties"))
				f = new PropertiesDAO();
			else if(table.equals("T_Herencias"))
				f = new T_HerenciasDAO();
			else if(table.equals("Access"))
				f = new AccessDAO();
			else if(table.equals("Clases"))
				f = new TClaseDAO();
			else if(table.equals("Replica_Configuration"))
				f = new ReplicaConfigurationDAO();
			//else if(table.equals("User"))   //(H)
				//f = new UserDAO();
			
			// ...
		f.setFactConnDB(this.factConnDB);	
		f.setBusiness(this.business);
		f.setCommit(this.commit);
        return f;
    }	
}
