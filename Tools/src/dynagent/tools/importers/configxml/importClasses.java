package dynagent.tools.importers.configxml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.Constants;
import dynagent.common.basicobjects.Class;
import dynagent.common.basicobjects.Properties;
import dynagent.common.knowledge.Category;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importClasses extends ObjectConfig{
	
	LinkedList<Class> listClasses=new LinkedList<Class>();
	ArrayList<String> listNameClasses=new ArrayList<String>();
	
	
	public importClasses(Element classesXML, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(classesXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listClasses=new LinkedList<Class>();
	}
	
	@Override
	public boolean configData() throws Exception{
		return extractAllClasses();
	}
	@Override
	public void importData() throws Exception {
		changeClasses();
	}

	private void changeClasses() throws Exception {
		Iterator<Class> itClasses=this.listClasses.iterator();
		while(itClasses.hasNext()){
			Class cls= itClasses.next();
			//System.out.println("---> "+cls.toString());
			Auxiliar.changeClass(cls, fcdb);
			//System.out.println("---> OK");
		}
	}
	private boolean extractAllClasses() throws Exception{
		Iterator itaccesses = getChildrenXml().iterator();
		boolean success=true;
		while(itaccesses.hasNext()){
			Element accessesElement = (Element)itaccesses.next();
			Class cls = new Class();
			try{
				if(accessesElement.getName().equals(ConstantsXML.EXCLU_CLASS))
					cls.setExcluded(true);
				else throw new ConfigException("ERROR: Actualmente sólo esta soportado el uso del nodo "+ConstantsXML.EXCLU_CLASS+" dentro del nodo "+ConstantsXML.CLASSES_ROOT);
				
				if(accessesElement.getAttributeValue(ConstantsXML.NAME) != null){
					String name=accessesElement.getAttributeValue(ConstantsXML.NAME).toString();
					cls.setClassName(name);
					Integer idtoClass=null;
					if((idtoClass=Auxiliar.getIdtoClass(name, fcdb))==null){
						if(cls.isExcluded())
							throw new ConfigException("ERROR: La clase "+name+" no existe en el modelo");
					}else{
						if(!cls.isExcluded()){
							throw new ConfigException("ERROR: La clase "+name+" ya existe en el modelo");
						}else{
							//if(!this.configImport.containsTargetClass(name))
							//	cls.setIdtoClass(idtoClass);
							//else{ 
								//throw new ConfigException("ERROR: La clase "+name+" no puede ser excluida ya que ha sido definida como targetClass de una UserTask");
								continue;
							//}
						}
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio");
				}
				
				if(accessesElement.getAttributeValue(ConstantsXML.CLASS_PARENT) != null){
					String nameParent=accessesElement.getAttributeValue(ConstantsXML.CLASS_PARENT).toString();
					Integer idtoParent=Auxiliar.getIdtoClass(nameParent, fcdb);
					if(!cls.isExcluded()){
						if(idtoParent!=null)
							cls.setClassNameParent(nameParent);
						else{
							throw new ConfigException("Error: La clase "+nameParent+" no existe");
						}
					}else{
						throw new ConfigException("Error: No esta permitido asignarle valor al atributo "+ConstantsXML.CLASS_PARENT+" en un nodo "+ConstantsXML.EXCLU_CLASS);
					}
				}
				ArrayList<Properties> listProp=new ArrayList<Properties>();
				Iterator itrProperties=accessesElement.getChildren(ConstantsXML.PROPERTY).iterator();
				while(itrProperties.hasNext()){
					Element propertyElement=(Element)itaccesses.next();
					Properties property=new Properties();
					
					if(propertyElement.getAttributeValue(ConstantsXML.NAME) != null){
						String name=propertyElement.getAttributeValue(ConstantsXML.NAME).toString();
						property.setNAME(name);
						property.setPROP(Auxiliar.getIdProp(name,fcdb));
					}else{
						throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo "+ConstantsXML.PROPERTY);
					}
					
					if(propertyElement.getAttributeValue(ConstantsXML.VALUECLS)!=null){
						String valueClsName=propertyElement.getAttributeValue(ConstantsXML.VALUECLS).toString();
						
						Integer valueCls=Constants.getIdDatatype(valueClsName);
						if(valueCls!=null)
							property.setCAT(Category.iDataProperty);
						else{
							valueCls=Auxiliar.getIdtoClass(valueClsName, fcdb);
							property.setCAT(Category.iObjectProperty);
						} 
						
						if(valueCls!=null){
							if(property.getPROP()!=null){
								Integer valueClsBD=Auxiliar.getValueClsProp(property.getNAME(), fcdb);
								if(!valueClsBD.equals(valueCls)){
									throw new ConfigException("Error: Al existir ya la property "+property.getNAME()+" en el modelo. El valor del atributo "+ConstantsXML.VALUECLS+" debería ser "+valueClsBD);
								}
							}
							property.setVALUECLS(valueCls);
						}else{
							throw new ConfigException("Error: La clase '"+valueClsName+"' no existe en el modelo");
						}
						
					}else{
						if(property.getPROP()==null){//Si la property ya existe no es necesario definir el valueCls
							throw new ConfigException("Error: El atributo '"+ConstantsXML.VALUECLS+"' es obligatorio en el nodo");
						}
					}
					
//					if(propertyElement.getAttribute(ConstantsXML.CARMAX)!=null){
//						String carmax=propertyElement.getAttributeValue(ConstantsXML.CARMAX).toString();
//						property.setQMAX(Float.valueOf(carmax));
//					}
//					
//					if(propertyElement.getAttribute(ConstantsXML.CARMIN)!=null){
//						String carmin=propertyElement.getAttributeValue(ConstantsXML.CARMIN).toString();
//						property.setQMIN(Float.valueOf(carmin));
//					}
					listProp.add(property);
				}
				cls.setProperties(listProp);
				listNameClasses.add(cls.getClassName());
				listClasses.add(cls);
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}
	
	private boolean hasConfig(String className){
		return listNameClasses.contains(className);
	}
}