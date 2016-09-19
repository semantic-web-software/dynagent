/**
 * DatabaseFacadeFactory.java
 * @author Ildefonso Montero Perez - ildefonso.montero@gmail.com
 * @description It represents a factory of facade to obtain data from a database.
 */
package dynagent.server.database;

import dynagent.common.Constants;
import dynagent.server.ejb.FactoryConnectionDB;

public class DatabaseFacadeFactory {
	
	static private DatabaseFacadeFactory instance = null;
	public String type = "";

	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static DatabaseFacadeFactory getInstance(){
        if(instance == null)
            return new DatabaseFacadeFactory();
        else
            return instance;
    }
    
	/**
	 * DatabaseFacadeFactory
	 * @return an instance of the factory, default constructor.
	 */
    private DatabaseFacadeFactory() {  }
	
	/**
	 * getType()
	 * @return the type of the facade
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * setType()
	 * @param type represents the type of the facade
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * createFacade
	 * @param type represents the type of the object that the factory creates
	 * @return a object that implements IFacade interface
	 */
	public IFacade createFacade(String business,FactoryConnectionDB fcdb,boolean commit,String type){
        IFacade f = null;
        if(type.equals("default")){
        	//f = new DatabaseAdapter(Constants.getBusiness());
        	f = new DatabaseAdapter(fcdb,business,commit);
        }
        if(type.equals("ORACLE")){
            f = new ORACLEFacade();
        }else if(type.equals("MYSQL")){
            // TODO f = new MYSQLFacade();
        }else if(type.equals("SQLSERVER")){
            // TODO f = new SQLSERVERFacade();
        }
        return f;
    }
}
