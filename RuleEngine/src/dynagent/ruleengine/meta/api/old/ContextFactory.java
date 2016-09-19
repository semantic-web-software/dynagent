/***
 * ContextFactory.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api.old;

/**
 * ContextFactory
 */
public class ContextFactory {
	
	static private ContextFactory instance = null;
	public String type = "";
	
	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static ContextFactory getInstance(){
        if(instance == null)
            return new ContextFactory();
        else
            return instance;
    }
    
	/**
	 * ContextFactory
	 * @return an instance of the factory, default constructor.
	 */
    private ContextFactory() {}
	
	/**
	 * getType()
	 * @return the type of the object that can export 
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * setType()
	 * @param type represents the type of the factory
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * createContext
	 * @param type represents the type of the object that the factory creates
	 * @return a object that implements IContext interface
	 */
	public IContext createContext(String type){
        IContext c = null;
        if(type.equals("default")){
           c = new Context();
        }
        return c;
    }	
}
