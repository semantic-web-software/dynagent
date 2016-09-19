package dynagent.tools.importers.configxml;

import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.common.basicobjects.PropertyForClass;
import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;

public class importProperties extends ObjectConfig{
	
	private LinkedList<PropertyForClass> listProp;
	public importProperties(Element propXML,FactoryConnectionDB fcdb,InstanceService instanceService,ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(propXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listProp=new LinkedList<PropertyForClass>();
	}
	
	@Override
	public boolean configData() throws Exception {
		return extractAllProperties();
	}
	@Override
	public void importData() throws Exception {
		changeProperties();
	}
	
	private boolean extractAllProperties() throws Exception{
		Iterator itprop = getChildrenXml().iterator();
		boolean success=true;
		while(itprop.hasNext()){
			Element propElem = (Element)itprop.next();
			PropertyForClass propInClass = new PropertyForClass();
			propInClass.setExcluded(false);
			try{
				if(propElem.getName().equals(ConstantsXML.EXCLU_PROPERTY))
					propInClass.setExcluded(true);
				
				if(propElem.getAttribute(ConstantsXML.CLASS)!=null){
					String className=propElem.getAttributeValue(ConstantsXML.CLASS).toString();
					Integer idtoClass=Auxiliar.getIdtoClass(className, fcdb);
					if (idtoClass!=null){
						propInClass.setClassName(className);
						propInClass.setIdtoClass(idtoClass);
					}else{
						throw new ConfigException("Error: La clase '"+className+"' no existe en el modelo");
					}
				}else if(!propInClass.isExcluded()){
					throw new ConfigException("Error: El atributo '"+ConstantsXML.CLASS+"' es obligatorio en el nodo "+propElem.getName());
				}
				
				if (propElem.getAttribute(ConstantsXML.PROP)!=null){
					String propName=propElem.getAttributeValue(ConstantsXML.PROP).toString();
					Integer idProp=null;
					if(propInClass.getIdtoClass()!=null){
						idProp=Auxiliar.getIdProp(propName, propInClass.getIdtoClass(),fcdb);
					}else{
						idProp=Auxiliar.getIdProp(propName, fcdb);
					}
					
					if(idProp!=null){
						propInClass.setIdProp(idProp);
						propInClass.setProp(propName);
					}else{
						throw new ConfigException("Error: La property '"+propName+"' no existe o no pertenece a la clase");
					}
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.PROP+"' es obligatorio en el nodo");
				}
				
				listProp.add(propInClass);
				
				//Si hemos filtrado por clase, lo aplicamos tambien a sus especializados
				if(propInClass.getIdtoClass()!=null){
					Iterator<Integer> itr=Auxiliar.getIdtoSpecialized(propInClass.getIdtoClass()).iterator();
					while(itr.hasNext()){
						int idto=itr.next();
						PropertyForClass propSpecialized=(PropertyForClass)propInClass.clone();
						propSpecialized.setIdtoClass(idto);
						propSpecialized.setClassName(Auxiliar.getClassName(idto));
						
						listProp.add(propSpecialized);
					}
				}
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		return success;
	}
	private void changeProperties() throws Exception {
		Iterator<PropertyForClass> itprop=this.listProp.iterator();
		
		while(itprop.hasNext()){
			PropertyForClass p =itprop.next();
			//System.out.println("---> "+p.toString());
			Auxiliar.changePropertyForClass(p, fcdb);
			//System.out.println("---> OK");
		}
		
	}
}
