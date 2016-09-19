/***
 * KnowledgeBaseInfoFactory.java
 * @author Ildefonso Montero Pérez - monteroperez@us.es
 */

package dynagent.ruleengine.meta.api;


public class KnowledgeBaseInfoFactory {
	
	static private KnowledgeBaseInfoFactory instance = null;
	public String type = "";
	
	/**
	 * getInstance
	 * @return an instance of the factory, this class is a Singleton class.
	 */
	public static KnowledgeBaseInfoFactory getInstance(){
        if(instance == null)
            return new KnowledgeBaseInfoFactory();
        else
            return instance;
    }
    
	/**
	 * KnowledgeBaseInfoFactory
	 * @return an instance of the factory, default constructor.
	 */
    private KnowledgeBaseInfoFactory() {}
	
	/**
	 * getType()
	 * @return the type of the object that can export 
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * setType()
	 * @param type represents the type of the parser
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * createKnowledgeBaseInfo
	 * @param type represents the type of the object that the factory creates
	 * @return a object that implements IKnowledgeBaseInfoExtended interface
	 */
	public IKnowledgeBaseInfo createKnowledgeBaseInfoExtended(String type){
        IKnowledgeBaseInfo k = null;
        if(type.equals("extended")){
        }
        if(type.equals("default")){
           k = new DocDataModel();
        }
        return k;
    }	
}
