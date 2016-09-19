/**
 * FacadeFactory.java
 * @author Ildefonso Montero Perez - ildefonso.montero@gmail.com
 * @description It represents a factory of facade to obtain data.
 */
package dynagent.server.database;


import java.io.IOException;

import dynagent.common.Constants;
import dynagent.server.database.dao.DAOManager;



public class FacadeFactory {
	
	static private FacadeFactory instance = null;
	public String type = "";
	
	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static FacadeFactory getInstance(){
        if(instance == null)
            return new FacadeFactory();
        else
            return instance;
    }
    
	/**
	 * FacadeFactory
	 * @return an instance of the factory, default constructor.
	 */
    private FacadeFactory() {}
	
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
	 * @throws IOException 
	 */
	public IFacade createFacade(String type){
        IFacade f = null;
        if(type.equals("FILE")){
            f = new FileFacade();
        }else if(type.equals("DATABASE")){
        	//System.out.println("commitDB " + DAOManager.getInstance().isCommit());
        	//System.out.println("closeConnectionDB " + DAOManager.getInstance().isCloseConnection());
            
   			f = DatabaseFacadeFactory.getInstance().createFacade(DAOManager.getInstance().getBusiness(),DAOManager.getInstance().getFactConnDB(),DAOManager.getInstance().isCommit(),"default");
        }
        return f;
    }	
}
