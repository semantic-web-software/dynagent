package dynagent.tools.importers.configxml;

import java.util.Iterator;
import java.util.LinkedList;

import org.jdom.Element;

import dynagent.server.ejb.FactoryConnectionDB;
import dynagent.server.services.InstanceService;
import dynagent.tools.importers.query.QUERYParser;

public class importQuerys extends ObjectConfig{
	private String path;
	private LinkedList<String> listquerys;
	public importQuerys(Element querysXML, String path, FactoryConnectionDB fcdb, InstanceService instanceService, ConfigData configImport,String pathImportOtherXml) throws Exception {
		super(querysXML,fcdb,instanceService,configImport,pathImportOtherXml);
		this.listquerys=new LinkedList<String>();
		this.path=path;
	}

	@Override
	public boolean configData() throws Exception {
		return configData(null);
	}
	@Override
	public void importData() throws Exception {
		importAllQuerys();
	}
	
	public boolean configData(String uniqueNameInclude) throws Exception {
		//path=getPathQuerys();
		return extractAllQuerys(uniqueNameInclude);
	}
	
	private void importAllQuerys() throws Exception{
		Iterator<String> itrp=this.listquerys.iterator();
		while(itrp.hasNext()){
			String nameQuery=itrp.next();
			QUERYParser.run(this.path, true, nameQuery, fcdb);
		}
	}

	private boolean extractAllQuerys(String uniqueNameInclude) throws Exception{
		Iterator itr = getChildrenXml().iterator();
		boolean success=true;
		while(itr.hasNext()){
			Element rElem = (Element)itr.next();
			try{
				String name = rElem.getAttributeValue(ConstantsXML.NAME);
				if (name!=null){
					if(uniqueNameInclude==null || uniqueNameInclude.equalsIgnoreCase(name))
					listquerys.add(name);
				}else{
					throw new ConfigException("Error: El atributo '"+ConstantsXML.NAME+"' es obligatorio en el nodo");
				}
			}catch(ConfigException ex){
				System.err.println(ex.getMessage());
				success=false;
			}catch(Exception ex){
				throw ex;
			}
		}
		
		if(uniqueNameInclude!=null && listquerys.isEmpty()){
			System.err.println("ERROR: La query '"+uniqueNameInclude+"' no existe en el archivo de configuracion");
			success=false;
		}
		return success;
	}
	
	/*public String getPathQuerys() throws Exception{
		String path=null;
		if (xml.getAttribute(ConstantsXML.PATH)!=null){
			path=xml.getAttributeValue(ConstantsXML.PATH).toString();
		}else{
			System.err.println("Error: El atributo '"+ConstantsXML.PATH+"' es obligatorio en el XML");
			throw new Exception();	
		}
		return path;
	}*/
}
